 package edu.stanford.mdocent;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.HTTP;
import com.google.gson.*;
 
 import android.provider.Settings.Secure;
 import android.util.Log;
 
 
 public class DBInteract {
 
 	private static String serverURL = "http://samo.stanford.edu:8787";
 	private static final String TAG = "DBInteract";
 	//private static String cookie = "";
 
 	private static JsonObject postData(JsonObject jo, String urlEnd){
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
 		HttpResponse response;
 		try {
 
 			HttpPost post = new HttpPost(serverURL + urlEnd);
 			StringEntity se = new StringEntity( jo.toString());  
 			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 			post.setEntity(se);
 			response = httpclient.execute(post);
 
 			if(response!=null){
 				String str = inputStreamToString(response.getEntity().getContent()).toString();
				Gson gson = new Gson();
				JsonObject recvObj = gson.fromJson(str, JsonObject.class);
 				return recvObj;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Log.v(TAG, "Http post failed. Posted: " + jo.toString() + " to url: "+ urlEnd);
 		return null;
 	}
 	
 	private static JsonArray getData(String urlEnd){
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
 		HttpResponse response;
 		try {
 			HttpGet get = new HttpGet(serverURL + urlEnd); 
 			response = httpclient.execute(get);
 
 			if(response!=null){
 				String str = inputStreamToString(response.getEntity().getContent()).toString();
				Gson gson = new Gson();
				JsonElement recvElem = gson.fromJson(str, JsonElement.class);
 				if (recvElem.isJsonObject()) {
 					Log.v(TAG, "Error in HttpGet. Received: " + recvElem.toString() + " url: "+ urlEnd);
 					return null;
 				}
 				return recvElem.getAsJsonArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Log.v(TAG, "Http get failed. Get url: "+ urlEnd);
 		return null;
 	}
 	
 
 	public static boolean postLoginData(String username, String password) { 
 		
 		JsonObject sndObject = new JsonObject();
 		try {
 			sndObject.addProperty("userName", username);
 			sndObject.addProperty("pass", password);
 			JsonObject rcvObj = postData(sndObject, "/login");
 			if(rcvObj != null) {
 				Log.v(TAG, rcvObj.toString());
 				if(rcvObj.get("success")!=null){
 					Log.v(TAG, "Authenticated " + rcvObj.toString());
 					return true;
 				}
 				else return false;
 			}
 		}
 		catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return false;
 	} 
 	
 	public static boolean postSignupData(String username, String password, String confirm) { 
 		if(!password.equals(confirm)){
 			return false;
 		}
 		JsonObject sndObject = new JsonObject();
 		try {
 			sndObject.addProperty("userName", username);
 			sndObject.addProperty("pass", password);
 			sndObject.addProperty("passConf", confirm);
 			JsonObject rcvObj = postData(sndObject, "/user");
 			if(rcvObj != null) {
 				Log.v(TAG, rcvObj.toString());
 				if(rcvObj.get("success")!=null){
 					Log.v(TAG, "Authenticated " + rcvObj.toString());
 					return true;
 				}
 				else return false;
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return false;
 	}
 	
 	//For now only returns true if json array is present. eventually needs to return elements of array
 	public static boolean tourKeywordSearch(String searchStr) { 	
 		QueryString qs = new QueryString("tourName", searchStr);
 		try {
 			JsonArray rcvArray = getData("/tours/?" + qs);
 			if(rcvArray != null) {
 				Log.v(TAG, rcvArray.toString());
 				if(rcvArray.isJsonArray()){
 					Log.v(TAG, "Authenticated " + rcvArray.toString());
 					return true;
 				}
 				else return false;
 			}
 		}
 		catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return false;
 	} 
 
 	private static StringBuilder inputStreamToString(InputStream is) {
 		String line = "";
 		StringBuilder total = new StringBuilder();
 		// Wrap a BufferedReader around the InputStream
 		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 		// Read response until the end
 		try {
 			while ((line = rd.readLine()) != null) { 
 				total.append(line); 
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// Return full string
 		return total;
 	}
 
 
 }
