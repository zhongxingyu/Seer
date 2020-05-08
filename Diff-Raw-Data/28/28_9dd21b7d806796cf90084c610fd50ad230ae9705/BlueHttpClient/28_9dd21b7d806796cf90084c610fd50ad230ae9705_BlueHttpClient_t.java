 package com.kalidu.codeblue;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class BlueHttpClient extends DefaultHttpClient {
 
 	JSONObject httpGet(String url) {
 		JSONObject result = new JSONObject();
 		HttpGet request = new HttpGet();
 		try {
 			request.setURI(new URI(url));
 		} catch (URISyntaxException e) {
 			Log.e("Http", "URISyntaxException");
 			return result;
 		}
 		result = this.executeRequest(request);
 		return result;
 
 	}
 	
 	JSONObject httpPost(String url, List<NameValuePair> params){
		// Every API post needs the token value, so add it here
		params.add(new BasicNameValuePair("token", MainActivity.getPreferences().getString("token", "")));
 		JSONObject result = new JSONObject();
 		HttpPost post = new HttpPost(url);
 		try {
 			post.setEntity(new UrlEncodedFormEntity(params));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		result = this.executeRequest(post);
 		return result;
 	}
 	
 	JSONObject executeRequest(HttpUriRequest request){
 		HttpResponse response = null;
 		JSONObject result = new JSONObject();
 		try {
 			// Execute the request
 			response = this.execute(request);
 			// Turn the response into a string
 			String response_string = convertStreamToString(response.getEntity().getContent());
 			Log.i("HTTP_Response", response_string);
 			try {
 				// Parse the response string into a JSON object
 				result = new JSONObject(response_string);
 			} catch (JSONException e) {
 				Log.e("JSONException", "JSONException in BlueHttpClient");
 			}
 			
 		} catch (ClientProtocolException e) {
 			Log.e("Http", "ClientProtocolException");
 			// TODO Auto-generated catch block
 		} catch (IOException e) {
 			Log.e("Http", "IOException");
 			e.printStackTrace();
 			// TODO Auto-generated catch block
 		}
 		return result;
 	}
 	
 	String convertStreamToString(java.io.InputStream is) {
 	    try {
 	        return new java.util.Scanner(is).useDelimiter("\\A").next();
 	    } catch (java.util.NoSuchElementException e) {
 	        return "";
 	    }
 	}
 }
