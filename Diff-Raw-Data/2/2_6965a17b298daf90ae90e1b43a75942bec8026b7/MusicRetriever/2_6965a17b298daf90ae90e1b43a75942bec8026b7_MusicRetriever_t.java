 package com.code_pig.pocketfma;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.code_pig.pocketfma.MusicService.State;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * Handles calls to the FMA API and retrieves a playable object.
  **/
 public class MusicRetriever {
 	private static final String TAG = "MusicRetriever";
 	
 	// Hard coded values
 	private static final String LAME_DRIVERS = "http://developer.echonest.com/api/v4/playlist/dynamic?api_key=3OIWAEJ4N8PCSACPE&bucket=tracks&bucket=id:fma&artist=lame+drivers&format=json&type=artist-radio&limit=true";
 	private static final String FMA_URI = "http://freemusicarchive.org/api/get/tracks.json?";
 	private static final String FMA_API_KEY = "LI1AWSUABA89HLQM";
 
 	// JSON nodes in EchoNest API response
 	private static final String NODE_RESPONSE = "response";
 	private static final String NODE_SESSION_ID = "session_id";
 	private static final String NODE_SONGS = "songs";
 	private static final String NODE_TRACKS = "tracks";
 	private static final String NODE_DATASET = "dataset";
 	private static final String NODE_FOREIGN_ID = "foreign_id";
 
 	// Retrieved track
 	private String sessionID = null;
 	private String playURL = null;
 
 	// Reference to MusicService
 	MusicService service;
 	
 	public MusicRetriever(){
 	}
 	
 	public void onInit() {
 		// All API calls are done in sequence here
 		String trackURL = null;
 		trackURL = parseFMAResponse(getResponse(parseEchoNestResponse(getResponse(LAME_DRIVERS))));
 		setPlayURL(trackURL);
 	}
 	
 	// Processes a JSON String response from the EchoNest API
 	private String parseEchoNestResponse(String response) {
 		String foreignID = null; //TODO error checking
 		try {
 			// Set foreign ID and session ID
 			JSONObject JSONResponse = new JSONObject(response);
 			if (getSessionID() == null) {
 				sessionID = JSONResponse.getJSONObject(NODE_RESPONSE).getString(NODE_SESSION_ID);
 			}
 			foreignID = JSONResponse.getJSONObject(NODE_RESPONSE).getJSONArray(NODE_SONGS).getJSONObject(0).getJSONArray("tracks").getJSONObject(0).getString(NODE_FOREIGN_ID);
 			Log.i(MusicRetriever.class.getName(), "foreignID = " + foreignID + "\n" + "sessionID = " + sessionID);
 		} catch (JSONException e) {
 			Log.e(MusicRetriever.class.toString(), "JSON error: " + e.toString());
 		}
 		return FMA_URI + "api_key=" + FMA_API_KEY + "&id=" + foreignID;
 	}
 	
 	// Processes a JSON String response from the FMA API
 	private String parseFMAResponse(String response) {
 		String trackURL = null; //TODO error checking
 		try {
 			JSONObject JSONResponse = new JSONObject(response);
			trackURL = JSONResponse.getJSONArray(NODE_DATASET).getJSONObject(2).getString("track_url") + "/download";
 		} catch (JSONException e) {
 			Log.e(MusicRetriever.class.getName(), "Error occured in FMA Response: " + e.toString());
 		}
 		return trackURL;
 	}
 	
 	// Returns a JSON String given an API URL
 	private String getResponse(String url) {
 		Log.i(TAG, "Getting response for URL :: " + url);
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 		Log.i(TAG, "Successful httpGet :: " + httpGet.toString());
 		try {
 			HttpResponse response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			Log.i(TAG, "HTTP Response status line :: " + statusCode);
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				Log.e(MusicRetriever.class.toString(),
 						"Failed to download file");
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
 	}
 	
 	public String getSessionID() {
 		return sessionID;
 	}
 
 	public void setSessionID(String sessionID) {
 		this.sessionID = sessionID;
 	}
 
 	public void setPlayURL(String playURL) {
 		this.playURL = playURL;
 	}
 	
 	public String getPlayURL() {
 		return playURL;
 	}
 }
