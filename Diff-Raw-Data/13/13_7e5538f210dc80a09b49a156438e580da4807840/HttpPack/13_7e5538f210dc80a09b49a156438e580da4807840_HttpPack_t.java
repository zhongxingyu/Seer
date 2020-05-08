 package com.example.lib;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.example.spanishtalk.SpanishTalkApplication;
 
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.util.Log;
 
 
 
 public class HttpPack {
 	public static boolean hasConnected() {
 		Context t = SpanishTalkApplication.context;
 		final ConnectivityManager connMgr = (ConnectivityManager) t
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		final android.net.NetworkInfo wifi = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 		final android.net.NetworkInfo mobile = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
		if ( mobile.isAvailable()) {
 			return true;
 		}
 		return false;
 
 	}
 	
 	
 	public static String getResponse(HttpResponse response) {
 		StringBuilder builder = new StringBuilder();
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(response
 					.getEntity().getContent()));
 			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
 				builder.append(s);
 			}
 
 			return builder.toString();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 
 	public static JSONObject getJsonByResponse(HttpResponse response) {
 		StringBuilder builder = new StringBuilder();
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(response
 					.getEntity().getContent()));
 			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
 				builder.append(s);
 			}
 
 			return new JSONObject(builder.toString());
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static JSONArray getJsonArrayByResponse(HttpResponse response) {
 		StringBuilder builder = new StringBuilder();
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(response
 					.getEntity().getContent()));
 			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
 				builder.append(s);
 			}
 
 			return new JSONArray(builder.toString());
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static List<NameValuePair> buildParams(Map<String, String> paramsMap) {
 		if (paramsMap == null || paramsMap.size() == 0) {
 			return null;
 		}
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		for (Map.Entry<String, String> map : paramsMap.entrySet()) {
 			params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
 		}
 		return params;
 	}
 	
 	
 	public static String getCookieByResponse(HttpResponse response) {
 		Header[] header_list = response.getAllHeaders();
 		Log.d(" -------- Current Header start --------", "----");
 		for (Header h : header_list) {
 			Log.d(h.getName(), h.getValue());
 		}
 		Log.d(" -------- Current Header end --------", "----");
 		
 		Header[] cookies = response.getHeaders("SET-COOKIE");
 		String cookie_store = null;
 		for (Header cookie : cookies) {
 			cookie_store = cookie.getValue();
 		}
 		return cookie_store;
 	}	
 	
 	
 	public static HttpResponse sendPost(String url, Map<String, String> params) {
 		CookieStore cookieStore = new BasicCookieStore();  
         HttpContext localContext = new BasicHttpContext();
         
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 		List<NameValuePair> user_pairs = HttpPack.buildParams(params);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 		
 		SessionManagement session = new SessionManagement();
 		if (session.getCookie() != null) {
 			// Log.d("Test Cookie: ", session.getCookie());
 			httppost.setHeader("Cookie", session.getCookie());
 		}
 		
 		try {
 			// httppost.setHeader("Accept", "application/json");
 			httppost.setHeader("User-Agent", "android");
 			httppost.setEntity(new UrlEncodedFormEntity(user_pairs, HTTP.UTF_8));
 			
 			HttpResponse response = httpclient.execute(httppost, localContext);
 			
 			return response;
 		
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Tag", "Unsupported encoding error: " + e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (ClientProtocolException e) {
 			Log.e("Tag", "Client Protocol error: " + e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			Log.e("Tag", "Could not get HTML: " + e.getMessage());
 			return null;
 		}
 
 	}
 	
 	
 	public static HttpResponse sendRequest(String url) {
         
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 		
 		SessionManagement session = new SessionManagement();
 		if (session.getCookie() != null) {
 			// Log.d("Test Cookie: ", session.getCookie());
 			httpget.setHeader("Cookie", session.getCookie());
 		}
 		
 		try {
 			httpget.setHeader("Accept", "application/json");
 			httpget.setHeader("User-Agent", "android");
 			HttpResponse response = httpclient.execute(httpget);
 			
 			if (response == null) {
 				return null;
 			}
 
 			return response;
 
 		} catch (UnsupportedEncodingException e) {
 			Log.d("Encoding : ", e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (ClientProtocolException e) {
 			Log.d("ClientProtocolException : ", e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			Log.d("IOException : ", e.getMessage());
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 	
 	
 	
 	
 	public static HttpResponse sendDelete(String url) {
 		CookieStore cookieStore = new BasicCookieStore();  
         HttpContext localContext = new BasicHttpContext();
         
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpDelete httpdelete = new HttpDelete(url);
 		
 		
 		SessionManagement session = new SessionManagement();
 		if (session.getCookie() != null) {
 			httpdelete.setHeader("Cookie", session.getCookie());
 		}
 		
 		try {
 			httpdelete.setHeader("User-Agent", "android");
 			
 			HttpResponse response = httpclient.execute(httpdelete, localContext);
 			
 			if (response == null) {
 				return null;
 			}
 			
 			return response;
 		
 		} catch (UnsupportedEncodingException e) {
 			Log.e("Tag", "Unsupported encoding error: " + e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (ClientProtocolException e) {
 			Log.e("Tag", "Client Protocol error: " + e.getMessage());
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			Log.e("Tag", "Could not get HTML: " + e.getMessage());
 			return null;
 		}
 
 	}
 }
