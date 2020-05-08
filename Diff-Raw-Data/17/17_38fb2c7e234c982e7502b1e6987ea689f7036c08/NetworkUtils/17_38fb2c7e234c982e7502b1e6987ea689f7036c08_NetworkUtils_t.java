 package com.yairkukielka.feedhungry.toolbox;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.android.volley.AuthFailureError;
 import com.android.volley.Request.Method;
 import com.android.volley.Response;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.toolbox.JsonArrayRequest;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.yairkukielka.feedhungry.network.JsonCustomRequest;
 
 public class NetworkUtils {
 	public static final String AUTORIZATION_HEADER = "Authorization";
 	public static final String OAUTH_HEADER_PART = "OAuth ";
 
 	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String GZIP = "gzip";
 	public static final String CONTENT_TYPE_PART = "application/json; charset=utf-8";
 
 	/**
 	 * Creates a JsonArrayRequest GET Request
 	 * 
 	 * @param url
 	 * @param successListener
 	 * @param errorListener
 	 * @param accessToken
 	 * @return
 	 */
 	public static JsonArrayRequest getJsonArrayRequest(String url, Response.Listener<JSONArray> successListener,
 			Response.ErrorListener errorListener, final String accessToken) {
 		return new JsonArrayRequest(url, successListener, errorListener) {
 			@Override
 			public Map<String, String> getHeaders() throws AuthFailureError {
 				Map<String, String> params = new HashMap<String, String>();
 				params.put(AUTORIZATION_HEADER, OAUTH_HEADER_PART + accessToken);
				params.put(CONTENT_TYPE_HEADER, GZIP);
 				return params;
 			}
 		};
 	}
 
 	/**
 	 * Creates a JsonObjectRequest GET Request
 	 * 
 	 * @param url
 	 * @param successListener
 	 * @param errorListener
 	 * @param accessToken
 	 * @return
 	 */
 	public static JsonObjectRequest getJsonObjectRequest(String url, Response.Listener<JSONObject> successListener,
 			Response.ErrorListener errorListener, final String accessToken) {
 		return new JsonObjectRequest(url, null, successListener, errorListener) {
 			@Override
 			public Map<String, String> getHeaders() throws AuthFailureError {
 				Map<String, String> params = new HashMap<String, String>();
 				params.put(AUTORIZATION_HEADER, OAUTH_HEADER_PART + accessToken);
				params.put(CONTENT_TYPE_HEADER, GZIP);
 				return params;
 			}
 		};
 	}
 
 	/**
 	 * Craetes a JsonObject Request with the method passed in the method
 	 * parameter
 	 * 
 	 * @param url
 	 * @param jsonRequest
 	 * @param successListener
 	 * @param errorListener
 	 * @param accessToken
 	 * @param method
 	 * @return
 	 */
 	public static JsonObjectRequest getJsonRequestWithMethod(int method, String url, JSONObject jsonRequest,
 			Listener<JSONObject> successListener, ErrorListener errorListener, final String accessToken) {
 		return new JsonObjectRequest(method, url, jsonRequest, successListener, errorListener) {
 			@Override
 			public Map<String, String> getHeaders() throws AuthFailureError {
 				Map<String, String> params = new HashMap<String, String>();
 				params.put(AUTORIZATION_HEADER, OAUTH_HEADER_PART + accessToken);
				params.put(CONTENT_TYPE_HEADER, GZIP);
 				return params;
 			}
 		};
 	}
 
 	/**
 	 * Craetes a JsonObject POST Request
 	 * 
 	 * @param url
 	 * @param jsonRequest
 	 * @param successListener
 	 * @param errorListener
 	 * @param accessToken
 	 */
 	public static JsonObjectRequest getJsonPostRequest(String url, JSONObject jsonRequest,
 			Listener<JSONObject> successListener, ErrorListener errorListener, final String accessToken) {
 		int method = Method.POST;
 		return getJsonRequestWithMethod(method, url, jsonRequest, successListener, errorListener, accessToken);
 	}
 	
 	public static JsonCustomRequest getJsonCustomPostRequest(String url, JSONObject jsonRequest,
 			Listener<JSONObject> successListener, ErrorListener errorListener, final String accessToken) {
 		int method = Method.POST;
 		return getJsonCustomRequest(method, url, jsonRequest, successListener, errorListener, accessToken);
 	}
 	
 	public static JsonCustomRequest getJsonCustomRequest(int method, String url, JSONObject jsonRequest,
 			Listener<JSONObject> successListener, ErrorListener errorListener, final String accessToken) {
 		return new JsonCustomRequest(method, url, jsonRequest, successListener, errorListener) {
 			@Override
 			public Map<String, String> getHeaders() throws AuthFailureError {
 				Map<String, String> params = new HashMap<String, String>();
 				params.put(AUTORIZATION_HEADER, OAUTH_HEADER_PART + accessToken);
				params.put(CONTENT_TYPE_HEADER, GZIP);
 				return params;
 			}
 		};
 	}
 }
