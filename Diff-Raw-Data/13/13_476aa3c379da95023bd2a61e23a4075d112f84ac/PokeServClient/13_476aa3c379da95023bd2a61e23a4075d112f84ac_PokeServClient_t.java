 package com.bettername.thepokemonone.data;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class PokeServClient {
	  //private static final String BASE_URL = "http://192.168.1.140:8080/PokeServ/rest/";
	private static final String BASE_URL = "https://monsteradventure.fwd.wf/api/";
 	  private static AsyncHttpClient client = new AsyncHttpClient();
 
 	  public static void get(String url, AsyncHttpResponseHandler responseHandler) {
 		  client.get(getAbsoluteUrl(url), responseHandler);
 	  }
 
 	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		  client.addHeader("Content-Type","application/json");
 	      client.post(getAbsoluteUrl(url), params, responseHandler);
 	  }
 
 	  private static String getAbsoluteUrl(String relativeUrl) {
 		  System.out.println(BASE_URL + relativeUrl);
 	      return BASE_URL + relativeUrl;
 	  }
 	}
