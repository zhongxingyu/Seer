 package com.challengeearth.cedroid.model;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.challengeearth.cedroid.helpers.IJSONParsable;
 
 /**
  * The Domain Model Representation of a Challenge. This Challenge is simplified, and is not equal
  * to the challenge model on the Server. It contains the image directly and also the progress
  * and the latitude and longitude of the startposition
  * 
  * @author Stefan Staub
  *
  */
 public class Challenge implements IJSONParsable{
 
 	/**
 	 * The base url for images
 	 */
	//private static final String IMAGE_BASE_URI = "http://160.85.232.31:8080/com.challengeEarth_ChallengeEarth_war_1.0-SNAPSHOT/";
	private static final String IMAGE_BASE_URI ="http://ec2-46-137-18-40.eu-west-1.compute.amazonaws.com/ChallengeEarth-1.0-SNAPSHOT/";
 	private static final String TAG = "Challenge";
 	
 	/**
 	 * The progress of this challenge
 	 */
 	private int progress;
 	
 	/**
 	 * Determines if the challenge is active 
 	 */
 	private boolean active;
 	private double latitude;
 	private double longitude;
 	
 	private long id;
 	private String title;
 	private String imageUrl;
 	private String description;
 	
 	
 	public int getProgress() {
 		return progress;
 	}
 	
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setProgress(int progress) {
 		this.progress = progress;
 	}
 	
 	public boolean isActive() {
 		return active;
 	}
 	
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	public String getImageUrl() {
 		return imageUrl;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	@Override
 	public void parse(JSONObject json) throws JSONException {
 		this.id = json.has("challengeId")?json.getLong("challengeId"):null;
 		this.title = json.has("title")?json.getString("title"):"";
 		this.description = json.has("description")?json.getString("description"):"";
 		
 		//Parse startposition
 		if(json.has("startPosition")) parseStartposition(json.getJSONObject("startPosition"));
 		if(json.has("achievmentDesc")) {
 			JSONObject image = json.getJSONObject("achievmentDesc").getJSONObject("image");
 			this.imageUrl = IMAGE_BASE_URI + image.getString("filePath") +  "/" + image.getString("fileName");
 		}
 		
 		Log.d(TAG, "Challenge parsed: "  + this.toString());
 	}
 	
 	private void parseStartposition(JSONObject json) throws JSONException {
 		//Parse startposition
 		this.latitude = json.has("latitude")?json.getDouble("latitude"):null;
 		this.longitude = json.has("longitude")?json.getDouble("longitude"):null;
 	}
 	
 	@Override
 	public JSONObject getJson() throws JSONException {
 		// Not used
 		Log.w(TAG, "JSON serialisation not implemented for this Class, returning null");
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return TAG + "{id: " + this.id + " startposition: " + this.latitude + "," + this.longitude + " title: " + this.title + "}"; 
 	}
 	
 	
 	
 }
