 package com.speakwrite.api;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.Scanner;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 import com.speakwrite.api.JobDownloadRequest.DownloadType;
 
 public class Client {
 	
 	public static String API_BASE_URL = "https://service.speak-write.com/integration/api/v1/";
 	
 	public Client() {
 		
 	}
 	
 	public CompletedJobsResponse getCompletedJobs(CompletedJobsRequest request)	throws Exception {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(API_BASE_URL + "completedjobs.ashx");
 		
 		List<NameValuePair> nvps = GetBaseParams(request);
 		if(request.maxAge != null) {
 			nvps.add(new BasicNameValuePair("maxAge", request.maxAge.toString()));
 		}
 		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 
 		HttpResponse response = httpClient.execute(httpPost);
 		return ReadJson(CompletedJobsResponse.class, response);
 	}
 	
 	public JobUploadResponse uploadJob(JobUploadRequest request) throws Exception {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpPost filePost = new HttpPost(API_BASE_URL + "submitjob.ashx");
 		FileBody bin = new FileBody(request.audioFile);
 		MultipartEntity reqEntity = new MultipartEntity();
 		reqEntity.addPart("applicationId", new StringBody(request.applicationId));
 		reqEntity.addPart("accountNumber", new StringBody(request.accountNumber));
 		reqEntity.addPart("pin", new StringBody(request.pin));
 		reqEntity.addPart("audioFile", bin);
 		filePost.setEntity(reqEntity);
 		HttpResponse response = httpClient.execute(filePost);
 		return ReadJson(JobUploadResponse.class, response);
 	}
 	
 	public JobDownloadResponse download(JobDownloadRequest request) throws Exception {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		HttpPost httpPost = new HttpPost(API_BASE_URL + "download.ashx");
 		
 		List<NameValuePair> nvps = GetBaseParams(request);
 		if(request.fileName == null || request.fileName == "") {
 			if(request.customFileName == null || request.customFileName == "") {
 				throw new IllegalArgumentException("Must supply either fileName or customFileName");
 			}
 			nvps.add(new BasicNameValuePair("customFileName", request.customFileName));
 		} 
 		else {
 			nvps.add(new BasicNameValuePair("filename", request.fileName));	
 		}		
		nvps.add(new BasicNameValuePair("filetype", request.type == DownloadType.Document ? "document" : "audio-source"));
 		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 
 		HttpResponse response = httpClient.execute(httpPost);
 		
 		HttpEntity entity = response.getEntity();
 		InputStream result = entity.getContent();
 		
 		File destinationFile = new File(request.destinationFileName);
 		
 		BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(destinationFile));
 		byte[] buffer = new byte[32*1024];
 		int bytesRead = 0;
 		while((bytesRead = result.read(buffer)) != -1) {
 			fOut.write(buffer, 0, bytesRead);
 		}
 		fOut.close();
 		EntityUtils.consume(entity);
 		
 		JobDownloadResponse downloadResponse = new JobDownloadResponse();
 		downloadResponse.success = true;
 		return downloadResponse;
 	}
 	
 	private static <T> T ReadJson(Class<T> clazz, HttpResponse response) throws Exception {
 		HttpEntity entity = response.getEntity();
 		InputStream result = entity.getContent();
 		String json = new Scanner(result, "UTF-8").useDelimiter("\\A").next();
 		System.out.println(json);
 		T jsonObject = new Gson().fromJson(json, clazz);
 		EntityUtils.consume(entity);
 		return jsonObject;
 	}
 	
 	private static List<NameValuePair> GetBaseParams(BaseApiRequest request) {
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair("applicationId", request.applicationId));
 		nvps.add(new BasicNameValuePair("accountNumber", request.accountNumber));
 		nvps.add(new BasicNameValuePair("pin", request.pin));
 		return nvps;
 	}
 }
