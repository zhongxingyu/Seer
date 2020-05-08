 /*
 Android java library for asynchronous http requests with sessions support 
 Copyright (c) 2013 Michael Nedokushev <grouzen.hexy@gmail.com>
 http://grouzen.com/
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package com.grouzen.android.serenity;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.os.Bundle;
 import android.util.Log;
 
 public class HttpConnection {
 	
 	private static final String TAG = HttpConnection.class.getCanonicalName();
 	
 	private HttpConnectionMethod method;
 	
 	private HttpClient client;
 	
 	private CookieStore cookies;
 	
 	private String url;
 	
 	public HttpConnection(String url, HttpConnectionMethod method) {
 		this.method = method;
 		this.url = url;
 		this.client = new DefaultHttpClient();
 		this.cookies = new BasicCookieStore();
 	}
 	
 	public HttpConnection(String url) {
 		this(url, HttpConnectionMethod.POST);
 	}
 	
 	public HttpConnection() {
 		this(null, HttpConnectionMethod.POST);
 	}
 	
 	public CookieStore getCookies() {
 		return cookies;
 	}
 	
 	public void setMethod(HttpConnectionMethod method) {
 		this.method = method;
 	}
 	
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 	public HttpResponse send(Bundle params) 
 			throws ClientProtocolException, IOException {
 		switch(method) {
 		case POST:
 			return post(url, params);
 		case GET:
 			return get(url, params);
 		default:
 			return null;
 		}
 	}
 	
 	public HttpResponse send() 
 			throws ClientProtocolException, IOException {
 		return send(null);
 	}
 	
 	private HttpResponse execute(HttpUriRequest request) 
 			throws ClientProtocolException, IOException {
 		HttpResponse response;
 		HttpContext localContext = new BasicHttpContext();
 		
 		localContext.setAttribute(ClientContext.COOKIE_STORE, cookies);
		
 		response = client.execute(request, localContext);
 		
 		return response;
 	}
 
     private ArrayList<NameValuePair> convertParams(Bundle params) {
         ArrayList<NameValuePair> entity = new ArrayList<NameValuePair>();
 
         for(String k : params.keySet()) {
             entity.add(new BasicNameValuePair(k, params.getString(k)));
         }
 
         return entity;
     }
 
 	private HttpResponse get(String url, Bundle params) 
 			throws ClientProtocolException, IOException {
         String urlParametrized = url;
 
         if(params != null) {
             urlParametrized += "?" + URLEncodedUtils.format(convertParams(params), "utf-8");
         }
 
 		return execute(new HttpGet(urlParametrized));
 	}
 	
 	private HttpResponse post(String url, Bundle params) 
 			throws ClientProtocolException, IOException {
 		HttpPost method = new HttpPost(url);
 		
 		if(params != null) {
 			try {
 				ArrayList<NameValuePair> entity = convertParams(params);
 
				method.setEntity(new UrlEncodedFormEntity(entity));
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return execute(method);
 	}
 
 }
