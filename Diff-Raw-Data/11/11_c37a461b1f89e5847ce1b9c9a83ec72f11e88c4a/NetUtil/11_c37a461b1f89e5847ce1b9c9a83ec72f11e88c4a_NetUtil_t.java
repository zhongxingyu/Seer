 package com.profbingo.android;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * Utility class that provides multiple network communication methods
  * 
  * @author ericstokes
  * 
  */
 public class NetUtil {
 
 	public static final String SITE_ROOT = "http://profbingo.heroku.com";
 
 	/**
 	 * Returns SHA hashed string
 	 * 
 	 * @param str
 	 *            raw string
 	 * @return
 	 */
 	public static String hashStringSHA(String str) {
 		try {
 			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
 			digest.update(str.getBytes());
 			byte messageDigest[] = digest.digest();
 
 			// Create Hex String
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++) {
 				String h = Integer.toHexString(0xFF & messageDigest[i]);
 				while (h.length() < 2)
 					h = "0" + h;
 				hexString.append(h);
 			}
 			return hexString.toString();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return "HASH ERROR";
 	}
 
 	/**
 	 * Get the return data from a specific HTTP connection.
 	 * 
 	 * @param url
 	 *            The url to request data from.
 	 * @return The resulting string from the request.
 	 */
 	public static String getHTTPData(String url) {
 		URLConnection connection;
 		String httpResult = "";
 		try {
 			connection = new URL(url).openConnection();
 			connection.connect();
 			InputStream inputStream = connection.getInputStream();
 			StringBuffer buffer = new StringBuffer();
 			byte[] b = new byte[4096];
 			for (int n; (n = inputStream.read(b)) != -1;) {
 				buffer.append(new String(b, 0, n));
 			}
 			httpResult = buffer.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return httpResult;
 	}
 
 	public static JSONObject postJsonData(JSONObject data, String url) {
 		// Create a new HttpClient and Post Header
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost post = new HttpPost(url);
 		try {
 
 			String jsonString = data.toString();
 			Log.d("PB", "Posting JSON Object: " + jsonString);
 			Log.d("PB", "Posting URL: " + url);
 
 			// Build Post Object
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 			nameValuePairs.add(new BasicNameValuePair("data", jsonString));
 			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// Execute HTTP Post Request
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			String responseBody = httpclient.execute(post, responseHandler);
 			JSONObject result = new JSONObject(responseBody).getJSONObject("data");
 			return result;
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONObject();
 	}
 
 	public static JSONObject postJsonData(HashMap<String, String> data, String url) {
 		return postJsonData(new JSONObject(data), url);
 
 	}
 
 	public static String logIn(String email, String pass) {
 		String authCode = "";
 		try {
 
 			JSONObject json = new JSONObject();
 			json.put("email", email);
			json.put("password", hashStringSHA(pass + email));
 
 			JSONObject jsonResult = NetUtil.postJsonData(json, SITE_ROOT + "/login");
 			Log.d("PB", "HTTP login post returned: " + jsonResult);
 
			if(jsonResult.get("result").equals("FAIL")){
				Log.d("PB", "Login Failed, no authcode key found in the JSON result");
				return authCode;
			}
			
 			authCode = jsonResult.getString("authcode");
 
 		} catch (Exception e) {
			Log.d("PB", "Error?");
 			e.printStackTrace();
 
 			// NO Auth Code Found.. Login Fail.
 		}
 
 		return authCode;
 	}
 
 }
