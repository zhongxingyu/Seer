 package com.teamblobby.studybeacon;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.loopj.android.http.*;
 import com.teamblobby.studybeacon.datastructures.Beacon;
 
 import org.json.*;
 
 public class APIClient {
 	
 	public static final String TAG = "APIClient";
 	
     public static final String BASE_URL = "http://leostein.scripts.mit.edu/StudyBeacon/";
 	
     private static AsyncHttpClient client = new AsyncHttpClient();
 	
     public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
         client.get(getAbsoluteUrl(url), params, responseHandler);
 	}
 	
 	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
 	    client.post(getAbsoluteUrl(url), params, responseHandler);
 	}
 
 	private static String getAbsoluteUrl(String relativeUrl) {
 	    return BASE_URL + relativeUrl;
 	}
 	
 	
     ////////////////////////////////
 	// Interface to doing a query.
 	
 	public final static String QUERY_URL = "query.py";
 	
 	// These strings are for the format of the query
 	public final static String COURSE_STR = "course";
 	public final static String LAT_MIN_STR = "LatE6Min";
 	public final static String LAT_MAX_STR = "LatE6Max";
 	public final static String LON_MIN_STR = "LonE6Min";
 	public final static String LON_MAX_STR = "LonE6Max";
 	
 	// These strings are for the format of the response
 	public final static String LAT_STR = "LatE6";
 	public final static String LON_STR = "LonE6";
 	public final static String DETAILS_STR = "Details";
 	public final static String CONTACT_STR = "Contact";
 	public final static String COUNT_STR = "count";
 	public final static String CREATED_STR = "Created";
 	public final static String EXPIRES_STR = "Expires";
 	
 	
 	public static void query(int LatE6Min, int LatE6Max, int LonE6Min, int LonE6Max, String courses[],
 			final SBAPIHandler handler)
 	//throws JSONException
 	{
 		RequestParams params = new RequestParams();
 		
 		params.put(LAT_MIN_STR,Integer.toString(LatE6Min));
 		params.put(LAT_MAX_STR,Integer.toString(LatE6Max));
 		params.put(LON_MIN_STR,Integer.toString(LonE6Min));
 		params.put(LON_MAX_STR,Integer.toString(LonE6Max));
 		
 		// TODO This does not make multiple entries for multiple courses!
 		for (String course : courses)
 			params.put(COURSE_STR, course);
 		
 		Log.d(TAG,"Query string " + params.toString());
 		
 		get(QUERY_URL, params, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray response) {
 
 				// the response should be an array of objects
 				
 				ArrayList<Beacon> beacons = new ArrayList<Beacon>();
 				
 				try {
 				
 				for(int i =0; i < response.length(); i++) {
 					
 					JSONObject bObj = response.getJSONObject(i);
 					
 					GeoPoint point = new GeoPoint(bObj.getInt(LAT_STR), bObj.getInt(LON_STR));
 					// TODO Check that this parsing works
 					DateFormat df = DateFormat.getDateInstance();
 					Date created = df.parse(bObj.getString(CREATED_STR));
 					Date expires = df.parse(bObj.getString(EXPIRES_STR));
 					
 					beacons.add(new Beacon(bObj.getString(COURSE_STR), point, bObj.getInt(COUNT_STR),
 							bObj.getString(DETAILS_STR), bObj.getString(CONTACT_STR),
 							created, expires));
 					
 				}
 					
 				// Call the handler's function (This is not within the UI thread? is this bad?)
 				handler.onQuery(beacons);
 				
 				}
 				
 				catch (Exception e) {
 					// TODO do something here??
					Log.d(TAG,e.getMessage());
 				}
 				
 			}
 		});
 		
 	}
 	
 }
