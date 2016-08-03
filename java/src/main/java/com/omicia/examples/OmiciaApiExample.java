package com.omicia.examples;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Scanner;

public class OmiciaApiExample {
	static final String OMICIA_API_ENDPOINT = "api.omicia.com";

	// your API key as provided by Omicia
	static final String OMICIA_API_LOGIN = "";
	static final String OMICIA_API_PASSWORD = "";

	// an Opal project ID
	static final String PROJECT_ID = "";

	// an Opal clinical report ID
	static final String REPORT_ID = "";

	// sex must be one of 'male', 'female', or 'unspecified'
	static final String SEX = "unspecified";


	public static void main(String[] args) throws Exception {

		// check arguments
		if (args.length != 1) {
			throw new RuntimeException("usage: <file to upload>");
		}

		System.out.println("uploading a genome");
		uploadGenome(PROJECT_ID, args[0]);
		System.out.println();

		System.out.println("downloading a report");
		downloadReport(REPORT_ID);
		System.out.println();

		System.out.println("done! :)");
	}

	public static void uploadGenome(String projectID, String filePath) throws Exception {

		// check input file for readability
		File inputFile = new File(filePath);

		if (!inputFile.exists() || !inputFile.canRead()) {
			throw new RuntimeException("unable to read input file '"+inputFile.getCanonicalPath()+"'");
		}

		// create an http client
		HttpClient httpClient = HttpClientBuilder.create().build();

		// build the URI fo the request
		URI uri = new URIBuilder()
			.setScheme("http").setHost(OMICIA_API_ENDPOINT)
			.setPath("/projects/"+projectID+"/genomes")
			.setParameter("genome_label", "Test R. Person")
			.setParameter("genome_sex", SEX)
			.setParameter("assembly_version", "hg19")
			.setParameter("format", getFormatFromFile(inputFile.getName()))
		.build();

		// build the authorization header
		String authorization = OMICIA_API_LOGIN+":"+OMICIA_API_PASSWORD;
		authorization = Base64.getEncoder().encodeToString(authorization.getBytes());

		// build the PUT request
		HttpPut request = new HttpPut(uri);
		request.setHeader("Authorization", "Basic "+authorization);
		request.setEntity(new FileEntity(inputFile));

		// execute the request
		System.out.println("executing request:");
		System.out.println(request.getRequestLine());
		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();

		// read the results
		System.out.println("response received:");
		String output = readStream(entity.getContent());
		System.out.println(output);
	}

	public static void downloadReport(String reportID) throws Exception {

		// create an http client
		HttpClient httpClient = HttpClientBuilder.create().build();

		// build the URI fo the request
		URI uri = new URIBuilder()
			.setScheme("http").setHost(OMICIA_API_ENDPOINT)
			.setPath("/reports/"+reportID)
			.setParameter("extended", "False")
		.build();

		// build the authorization header
		String authorization = OMICIA_API_LOGIN+":"+OMICIA_API_PASSWORD;
		authorization = Base64.getEncoder().encodeToString(authorization.getBytes());

		// build the GET request
		HttpGet request = new HttpGet(uri);
		request.setHeader("Authorization", "Basic "+authorization);

		// execute the request
		System.out.println("executing request:");
		System.out.println(request.getRequestLine());
		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();

		// read the results
		System.out.println("response received:");
		String output = readStream(entity.getContent());
		System.out.println(output);
	}

	private static String getFormatFromFile(String name) {
		if (name.endsWith(".vcf")) {
			return "vcf";
		} else if (name.endsWith(".vcf.gz")) {
			return "vcf.gz";
		} else if (name.endsWith(".vcf.bz2")) {
			return "vcf.bz2";
		} else {
			throw new RuntimeException("unknown file format");
		}
	}

	private static String readStream(InputStream stream) {
		Scanner scanner = new Scanner(stream).useDelimiter("\\A");
		return scanner.next();
	}
}