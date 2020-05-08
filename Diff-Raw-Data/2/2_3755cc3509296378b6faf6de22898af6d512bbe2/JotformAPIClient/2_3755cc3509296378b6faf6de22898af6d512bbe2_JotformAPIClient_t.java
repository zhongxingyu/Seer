 package com.jotform.api;
 
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class JotformAPIClient {
 
 	private static final String BASE_URL = "http://api.jotform.com/";
 	private AsyncHttpClient client;
 	private String apiKey;
 
 	public JotformAPIClient(String apiKey) {
 		client = new AsyncHttpClient();
 		this.apiKey = apiKey;
 	}
 	
 	public JotformAPIClient() {
 		client = new AsyncHttpClient();
 		this.apiKey = "";
 	}
 
 	public void get(String url, RequestParams params,
 			AsyncHttpResponseHandler responseHandler) {
 
 		client.addHeader("apiKey", apiKey);
 		client.get(getAbsoluteUrl(url), params, responseHandler);
 	}
 
 	public void post(String url, RequestParams params,
 			AsyncHttpResponseHandler responseHandler) {
 		client.post(getAbsoluteUrl(url), params, responseHandler);
 	}
 	
 	public void getApiKey(String username, String password, AsyncHttpResponseHandler responseHandler){
 		
 		RequestParams params = new RequestParams();
 		
 		params.put("username", username);
 		params.put("password", password);
 		params.put("appName", "Android");
 		
		post("login", params, responseHandler);
 	}
 
 	public void getUser(AsyncHttpResponseHandler responseHandler) {
 		get("user", null, responseHandler);
 	}
 
 	public void getForms(AsyncHttpResponseHandler responseHandler) {
 		get("user/forms", null, responseHandler);
 	}
 
 	public void getFormQuestions(
 			long formId,
 			AsyncHttpResponseHandler responseHandler) {
 		
 		get("form/" + String.valueOf(formId) + "/questions", null, responseHandler);
 	}
 
 	public void getSubmissions(AsyncHttpResponseHandler responseHandler) {
 		
 		get("user/submissions", null, responseHandler);
 	}
 	
 	public void getSubmissions(
 			Integer limit,
 			String orderBy,
 			JSONObject filter,
 			AsyncHttpResponseHandler responseHandler){
 		
 		RequestParams params = new RequestParams();
 		
 		if (limit != null) {
 			params.put("limit", String.valueOf(limit));
 		}
 
 		if (orderBy != null) {
 			params.put("order_by", orderBy);
 		}
 		
 		// Make sure all filter parameters and values are String formatted as :
 		// filter = {
 		// 		"id": "236344132991249332",
 		// 		"form_id": "31564842891967",
 		// 		"ip": "176.42.170.199",
 		// 		"created_at": "2013-06-06 12:08:52",
 		// 		"status": "ACTIVE",
 		// 		"new": "0",
 		// 		"flag": "0",
 		// 		"updated_at": "2013-06-24 08:17:44"
 		// }
 		if (filter != null) {
 
 			Iterator<String> keys = filter.keys();
 			while (keys.hasNext()) {
 
 				String key = keys.next();
 				
 				try {
 				
 					filter.put(key, String.valueOf(filter.getJSONObject(key)));
 					
 				} catch (JSONException e) {
 					
 				}
 			}
 
 			params.put("filter", filter.toString());
 		}
 		
 		get("user/submissions", params, responseHandler);
 	}
 
 	public void getFormSubmissions(long formId,
 			AsyncHttpResponseHandler responseHandler) {
 		RequestParams params = new RequestParams();
 		params.put("qid_enabled", "true");
 		get("form/" + String.valueOf(formId) + "/submissions", params,
 				responseHandler);
 	}
 
 	public void getFormSubmissions(
 			long formId,
 			Integer limit,
 			String orderBy,
 			JSONObject filter,
 			AsyncHttpResponseHandler responseHandler) {
 
 		try {
 			filter.put("form_id", formId);
 		} catch (JSONException e) {
 			
 		}
 		
 		getSubmissions(limit, orderBy, filter, responseHandler);
 	}
 
 	private String getAbsoluteUrl(String relativeUrl) {
 		return BASE_URL + relativeUrl;
 	}
 }
