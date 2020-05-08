 package com.tuit.ar.api;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 
 import android.os.Handler;
 
 import com.tuit.ar.api.request.Options;
 
 
 public class TwitterRequest {
 	static public enum Method { GET, POST };
 	private Runnable runnable = new Runnable() {
 		@Override
 		public void run() {
 			finishedRequest();
 		}
 	};
 	private Handler handler = new Handler();
 	private Options url = null;
 	private int statusCode;
 	private String response;
 
 	public Options getUrl() { return url; }
 	public void setUrl(Options url) { this.url = url; } 
 	public int getStatusCode() { return statusCode; }
 	public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
 	public String getResponse() { return response; }
 	public void setResponse(String response) { this.response = response; }
 
 	private void finishedRequest() {
 		Twitter.getInstance().finishedRequest(this);
 	}
 
 	public TwitterRequest(final Options url, final ArrayList <NameValuePair> nvps,
 			final Method method) throws Exception {
 		setUrl(url);
 		(new Thread() {
 			public void run() {
 				DefaultHttpClient http = new DefaultHttpClient();
 
 				String full_url = "http" + (Twitter.isSecure ? "s" :"") + "://" + Twitter.BASE_URL + url.toString() + ".json";
 				HttpRequestBase request;
 				if (method == Method.POST) {
 					request = new HttpPost(full_url);
 					try {
 						((HttpPost) request).setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
 					String queryString = "";
 					if (nvps != null && nvps.size() > 0) {
 						try {
 							queryString += "?" + (new UrlEncodedFormEntity(nvps, HTTP.UTF_8)).toString();
 						} catch (UnsupportedEncodingException e) {
 							e.printStackTrace();
 						}
 					}
 					request = new HttpGet(full_url + queryString);
 				}
 
 				String username = Twitter.getInstance().getUsername();
 				String password = Twitter.getInstance().getPassword();
 				if (username != null && password != null) {
 					BasicCredentialsProvider credentials = new BasicCredentialsProvider();
 					credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
 					http.setCredentialsProvider(credentials);
 				}
 
 				try {
 					HttpResponse response = http.execute(request);
 					HttpEntity resEntity = response.getEntity();
 					setStatusCode(response.getStatusLine().getStatusCode());
 					
 					//if (resEntity != null && statusCode >= 200 && statusCode < 300)
 					{
 						ByteArrayOutputStream output = new ByteArrayOutputStream();
 						resEntity.writeTo(output);
 						byte[] bytes = output.toByteArray();
 						setResponse(new String(bytes));
 					}
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				handler.post(runnable);
 			}
 		}).start();
 	}
 }
