 package com.sk.util.http;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.sk.service.exception.ServiceException;
 
 public class HttpConnection {
 
 	private HttpClient httpClient;
 	private HttpPost httpPost;
 
 	public HttpConnection(HttpClient httpClient, HttpPost httpPost) {
 		this.httpClient = httpClient;
 		this.httpPost = httpPost;
 	}
 
 	public HttpResult execute() throws ClientProtocolException, IOException {
 		HttpResponse httpResponse = httpClient.execute(httpPost);
 
 		HttpResult httpResult = new HttpResult();
 		httpResult.setHttpEntity(new BufferedHttpEntity(httpResponse.getEntity()));
 		httpResult.setHttpStatus(httpResponse.getStatusLine().getStatusCode());
 
 		return httpResult;
 	}
 
 	public void setData(String data) {
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair("data", data));
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 		} catch (UnsupportedEncodingException e) {
 			throw new ServiceException(e);
 		}
 	}
 
 }
