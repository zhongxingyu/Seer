 package com.cs371m.austinrecycle;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Model {
     String TAG = "Model";
     HttpURLConnection _conn;
     
     // Must know user's location to sort facilities
     double _user_lat;
     double _user_long;
     
     public Model(double user_lat, double user_long)
     {
     	// Save user location
     	_user_lat = user_lat;
     	_user_long = user_long;
    	Log.d(TAG, "Model user location initialized to {" + _user_lat + ", " + _user_long + "}");
     }
     
     /**
      * Makes an HTTP request to the given url_string and returns the JSON string
      * response. Returns null if it fails.
      */
     private String getResponse(String url_string)
     {
         try
         {
        	// Initialize connection object
             URL url = new URL(url_string);
             _conn = (HttpURLConnection) url.openConnection();
//            try
//            {
//                _conn.setReadTimeout(10000);
//                _conn.setConnectTimeout(15000);
//                _conn.setRequestMethod("GET");
//                _conn.setDoInput(true);         // Yes we can receive responses
//                _conn.setDoOutput(true);        // Yes we can send requests
//            }
//            catch (Exception e)
//            {
//                Log.e(TAG, "Error initializing connection", e);
//            }
             InputStream in = new BufferedInputStream(_conn.getInputStream());
             
             return convertStreamToString(in);
         }
         catch (Exception e) 
         {
             Log.e(TAG, "Error getting response", e);
             return null;
         }
     }
     
     /**
      * Forms the appropriate URL based on the given materials list, makes the
      * request, and returns the response.
      */
     public ArrayList<FacilityItem> getFacilities(String[] materials)
     {
         Log.d(TAG, "entering getFacilities()");
         // Create HTTP Request URL from materials
         String url_string = "https://data.austintexas.gov/resource/qzi7-nx8g.json";
         for (int i = 0; i < materials.length; ++i)
         {
             if (i == 0) url_string += "?" + materials[i] + "=Yes";
             else url_string += "&" + materials[i] + "=Yes";
         }
         // Make the request over the network and get the response
         String response = getResponse(url_string);
         
         // Parse the JSON String response into a JSONArray of JSONObjects
         ArrayList<FacilityItem> facilities = new ArrayList<FacilityItem>();
         try 
         {
             JSONArray resp_array = new JSONArray(response);
             
             for(int i = 0; i < resp_array.length(); ++i)
             {
                 JSONObject obj_i = resp_array.getJSONObject(i);
                 Iterator<?> itr = obj_i.keys();
                 ArrayList<String> accepts = new ArrayList<String>();
                 while(itr.hasNext()) {
                 	String key = (String) itr.next();
                 	if(obj_i.getString(key).equals("Yes")) {
                 		accepts.add(key);
                 	}
                 }
                 
                 String name = obj_i.getString("business_name");
                 String phone_num = obj_i.getString("phone");
                 
                 String addr_obj_string = obj_i.getString("address");
                                 
                 JSONObject addr_obj = new JSONObject(addr_obj_string);
                 String addr_human = addr_obj.getString("human_address");
                 
                 String addr_lat = addr_obj.getString("latitude");
                 String addr_long = addr_obj.getString("longitude");
                 
                 FacilityItem facility_i = new FacilityItem(name, addr_lat, addr_long, addr_human, phone_num, accepts);
                 facilities.add(facility_i);
             }
         }
         catch (JSONException e)
         {
             Log.e(TAG, "Error parsing response", e);
         }
 
         // Sort facilities by distance from the user
         Collections.sort(facilities, new FacilityComparator());
         
         return facilities;
     }
     
     /**
      * Return a String of the given input stream. Trick works by using the
      * scanner such that it returns the whole stream as one token string.
      */
     private String convertStreamToString(InputStream is) {
         java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
         return s.hasNext() ? s.next() : "";
     }
     
     /**
      * Sorts two FacilityItems based on distance from user's location
      */
     private class FacilityComparator implements Comparator<FacilityItem> {
     	public int compare(FacilityItem item1, FacilityItem item2) {
     		Double lat1 = Double.valueOf(item1.getAddrLat());
     		Double long1 = Double.valueOf(item1.getAddrLong());
     		Double lat2 = Double.valueOf(item2.getAddrLat());
     		Double long2 = Double.valueOf(item2.getAddrLong());
     		
     		// Calculate relative distance by treating lat and long as if they
     		// were just an x and y axis
     		Double dist1 = Math.sqrt(Math.pow(lat1 - _user_lat, 2) + 
     								 Math.pow(long1 - _user_long, 2));
     		Double dist2 = Math.sqrt(Math.pow(lat2 - _user_lat, 2) + 
 					 				 Math.pow(long2 - _user_long, 2));
     		
     		// Compare and return comparator relation
     		if(dist1 == dist2) return 0;
     		else if(dist1 < dist2) return -1;
     		else return 1;
     	}
     }
 }
