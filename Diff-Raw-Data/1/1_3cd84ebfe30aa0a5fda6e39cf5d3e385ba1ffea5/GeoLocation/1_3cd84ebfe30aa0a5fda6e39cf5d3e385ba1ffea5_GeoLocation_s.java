 package com.app.getconnected.network;
 
 import com.app.getconnected.rest.RESTRequest;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.app.getconnected.gps.Location;
 import java.util.HashMap;
import java.util.concurrent.ExecutionException;
 
 /**
  * @author 	Jorian Plat <jorianplat@hotmail.com>
  * @version 1.0			
  * @since	2013-10-16
  */
 public class GeoLocation implements Location {
 
 	private HashMap<String, Double> location = new HashMap<String, Double>();
 	private String url = "https://maps.googleapis.com/maps/api/geocode/json"; 
 	
 	public GeoLocation(String address) {	
 		RESTRequest request = new RESTRequest(url);
 		request.putString("address", address);
 		request.putString("region", "nl");
 		request.putString("components", "country:nl");
 		request.putString("sensor", "true");
 		
 		try {
 			String result = request.execute().get();
 			
 			setLocation(new JSONObject(result));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Parse the JSON and put the latitude and longitude in a HashMap.
 	 * @param json				JSONObject received from the Google GeoCoding API
 	 * @throws JSONException	
 	 */
 	private void setLocation(JSONObject jsonObject) throws JSONException {
 		JSONArray results = jsonObject.getJSONArray("results");
 		JSONObject resultsObject = results.getJSONObject(0);
 		JSONObject geometry = resultsObject.getJSONObject("geometry");
 		JSONObject location = geometry.getJSONObject("location");
 		
 		this.location.put("lat", location.getDouble("lat"));
 		this.location.put("lng", location.getDouble("lng"));
 	}
 	
 	/**
 	 * Check whether location is valid
 	 * @return boolean	True if location is valid; false if not 
 	 */
 	public boolean isValidLocation() {
 		return location.get("lat") != null && location.get("lng") != null;
 	}
 	
 	/**
 	 * Get the latitude
 	 * @return double	The latitude of the address
 	 */
 	public double getLatitude() {
 		return location.get("lat");
 	}
 	
 	/**
 	 * Get the longitude
 	 * @return double	The longitude of the address
 	 */
 	public double getLongitude() {
 		return location.get("lng");
 	}	
 	
 }
