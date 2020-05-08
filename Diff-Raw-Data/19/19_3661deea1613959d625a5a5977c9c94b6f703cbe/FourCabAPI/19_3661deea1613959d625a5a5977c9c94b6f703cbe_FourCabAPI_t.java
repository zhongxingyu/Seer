 package com.fourcab;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.util.Log;
 
 public class FourCabAPI {
 	private static final String TAG = FourCabAPI.class.getSimpleName();
 	private static final String API = "http://fourcab.grumpypanda.net";
 	private String mAuthToken;
 
 	public FourCabAPI(Context context) {
     	SharedPreferences prefs = context.getSharedPreferences(Constants.FOURCAB_PREFS, 0);
     	String token = prefs.getString(Constants.FOURSQUARE_TOKEN_KEY, null);
 		this.mAuthToken = token;
 	}
 	
 	private JSONObject executeHttpGet(String uri) {
 		Log.v("jason", "executeHttpGet: " + uri);
 		HttpGet req = new HttpGet(uri);
 
 		HttpClient client = new DefaultHttpClient();
 		try {
 			HttpResponse resLogin = client.execute(req);
 			BufferedReader r = new BufferedReader(new InputStreamReader(resLogin
 					.getEntity().getContent()));
 			StringBuilder sb = new StringBuilder();
 			String s = null;
 			while ((s = r.readLine()) != null) {
 				sb.append(s);
 			}
 
 			return new JSONObject(sb.toString());
 		} catch(Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private JSONObject executeHttpPost(String uri, String params) throws Exception {
 		Log.v("jason", "uri = " + uri);
		Log.v("jason", params);
 		HttpPost req = new HttpPost(uri);
 		HttpClient client = new DefaultHttpClient();
 		
 		req.setEntity(new StringEntity(params));
 		req.setHeader("Content-Type", "application/json");
 
 		HttpResponse resCheckin = client.execute(req);
 		BufferedReader r = new BufferedReader(new InputStreamReader(resCheckin
 				.getEntity().getContent()));
 		StringBuilder sb = new StringBuilder();
 		String s = null;
 		while ((s = r.readLine()) != null) {
 			sb.append(s);
 		}
 		Log.v("jason", "response = " + sb);
 		return new JSONObject(sb.toString());
 	}
 
 
 	public JSONObject checkIn(double lat, double lng, double dstLat, double dstLng) {
 		try {
 			JSONObject holder = new JSONObject();
 			holder.put("foursquareOauthToken", mAuthToken);
 			JSONObject pickup = new JSONObject();
 			pickup.put("lat", lat);
 			pickup.put("lng", lng);
 			holder.put("pickup", pickup);
 			JSONObject dropoff = new JSONObject();
			dropoff.put("lat", dstLat);
			dropoff.put("lng", dstLng);
 			holder.put("dropoff", dropoff);
 
			return executeHttpPost(API + "/api/checkin/", holder.toString(2));
 		} catch (JSONException e) {
 			Log.e(TAG, "JSONException: ", e);
 		} catch (Exception e) {
 			Log.e(TAG, "Exception: ", e);
 		}
 		return null;
 	}
 	
 	public JSONObject getRides() {
 		try {
 			JSONObject holder = new JSONObject();
 			holder.put("foursquareOauthToken", mAuthToken);
 
			return executeHttpPost(API + "/api/rides/", holder.toString(2));
 		} catch (JSONException e) {
 			Log.e(TAG, "JSONException: ", e);
 		} catch (Exception e) {
 			Log.e(TAG, "Exception: ", e);
 		}
 		return null;
 	}
 }
