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
  * @author Robby Grodin 2012
  **/
 public class MusicRetriever {
 	private static final String TAG = "MusicRetriever";
 	
 	// API Keys
 	private static final String FMA_API_KEY = "LI1AWSUABA89HLQM";
 	private static final String ECHO_NEST_API_KEY = "3OIWAEJ4N8PCSACPE";
 	
 	// Hard coded values
 	private static final String CREATE_QUERY = "http://developer.echonest.com/api/v4/playlist/dynamic/create?api_key=" + ECHO_NEST_API_KEY + "&bucket=tracks&bucket=id:fma&format=json&type=artist-radio&limit=true&artist=";
 	private static final String RESTART_QUERY = "http://developer.echonest.com/api/v4/playlist/dynamic/restart?api_key=" + ECHO_NEST_API_KEY + "&type=artist-radio&session_id="; //needs session ID and artist
 	private static final String NEXT_QUERY = "http://developer.echonest.com/api/v4/playlist/dynamic/next?api_key=" + ECHO_NEST_API_KEY + "&format=json&session_id="; //needs session ID
 	private static final String FMA_URI = "http://freemusicarchive.org/api/get/tracks.json?";
 
 
 	// JSON nodes in EchoNest API response
 	private static final String NODE_RESPONSE = "response";
 	private static final String NODE_SESSION_ID = "session_id";
 	private static final String NODE_SONGS = "songs";
 	private static final String NODE_DATASET = "dataset";
 	private static final String NODE_FOREIGN_ID = "foreign_id";
 	private static final String NODE_STATUS = "status";
 	private static final String NODE_MESSAGE = "message";
 	private static final String SUCCESS_MESSAGE = "success";
 
 	// Retrieved track
 	private String sessionID = null;
 	private String foreignID = null;
 	private String playURL = null;
 	private boolean isFirstQuery = true;
 	
 	// Reference to MusicService
 	MusicService service;
 	
 	/**
 	 * Determines which API calls to make, and makes them. Once this task is completed
 	 * the URL for the next track to be played can be retrieved.
 	 * @param query search parameter set by user
 	 */
 	public void onInit(String query) {
 		Log.i(TAG, "onInit() called, query = " + query);
 		// All API calls are done in sequence here
 		if (query != null) {
 			if (isFirstQuery) { // New session
 				setSessionID(getResponse(CREATE_QUERY + query));
 				isFirstQuery = false;
 			} else { // Restarting session
 				restartSession(getResponse(RESTART_QUERY + sessionID + "&artist=" + query));
 			}
 		}
 		setForeignID(getResponse(NEXT_QUERY + sessionID));
		setNextPlayURL(getResponse(FMA_URI + "api_key=" + FMA_API_KEY + "&track_id=" + foreignID));
 	}
 	
 	/**
 	 * Parses the response from FMA API for track data
 	 * @param response JSON style response from FMA
 	 */
 	private void setNextPlayURL(String response) {
 		Log.i(TAG, "returnNextPlayURL() called");
 		try {
 			JSONObject JSONResponse = new JSONObject(response);
			playURL = JSONResponse.getJSONArray(NODE_DATASET).getJSONObject(0).getString("track_url") + "/download";
 		} catch (JSONException e) {
 			Log.e(MusicRetriever.class.getName(), "Error occured in FMA Response: " + e.toString());
 		}
 	}
 
 	/**
 	 * Parses the response from EchoNest API for Dynamic/Create
 	 * @param response JSON style response from EchoNest
 	 */
 	private void setSessionID(String response) {
 		Log.i(TAG, "returnSessionID() called");
 		try {
 			JSONObject JSONResponse = new JSONObject(response);
 			sessionID = JSONResponse.getJSONObject(NODE_RESPONSE).getString(NODE_SESSION_ID);
 			isFirstQuery = false;
 		} catch (JSONException e) {
 			Log.e(TAG, "JSON error in parseCreateEchoNestResponse() :: " + e.toString());
 		}
 	}
 	
 	/**
 	 * Parses the response from EchoNest API for Dynamic/Next
 	 * @param response JSON style response from EchoNest
 	 */
 	private void setForeignID(String response) {
 		Log.i(TAG, "returnForeignID() called");
 		try {
 			// Set foreign ID and session ID
 			JSONObject JSONResponse = new JSONObject(response);
 			foreignID = JSONResponse.getJSONObject(NODE_RESPONSE).getJSONArray(NODE_SONGS).getJSONObject(0).getJSONArray("tracks").getJSONObject(0).getString(NODE_FOREIGN_ID);
			foreignID = foreignID.substring(10); // Remove "fma:track:"
 			Log.i(TAG, "foreignID retrieved :: " + foreignID);
 		} catch (JSONException e) {
 			Log.e(TAG, "JSON error: " + e.toString());
 		}
 	}
 	
 	/**
 	 * Interprets the response from EchoNest API for Dynamic/Restart
 	 * @param response JSON style response from EchoNest
 	 */
 	private void restartSession(String response) {
 		Log.i(TAG, "restartSession() called");
 		try {
 			JSONObject JSONResponse = new JSONObject(response);
 			String message = JSONResponse.getJSONObject(NODE_RESPONSE).getJSONObject(NODE_STATUS).getString(NODE_MESSAGE);
 			if(message == SUCCESS_MESSAGE) {
 				Log.i(TAG, "Session successfully restarted");
 			} else {
 				Log.e(TAG, "ERROR" + message + "(from EchoNest)");
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "JSON error: " + e.toString());
 		}
 	}
 	
 	/**
 	 * Processes API calls over http
 	 * @param url API query
 	 * @return API response in String format
 	 */
 	private String getResponse(String url) {
 		Log.i(TAG, "getResponse() called, url = " + url);
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
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				Log.e(TAG, "Failed to download file");
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
 	}
 	
 	/**
 	 * @return next URL to be played
 	 */
 	public String getPlayURL() {
 		return playURL;
 	}
 }
