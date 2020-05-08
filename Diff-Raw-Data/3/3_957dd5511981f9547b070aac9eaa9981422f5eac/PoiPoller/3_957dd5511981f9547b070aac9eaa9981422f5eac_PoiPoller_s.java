 package com.advsofteng.app1;
 
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.Toast;
 
 /**
  * A class to poll in the  background at regular intervals, looking for points of interest
  * @author twhume
  *
  */
 
 public class PoiPoller extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		/* Last-reported locations live in SharedPreferences, so they can be shared between
 		 * Activities and BroadcastReceivers. If there's nothing there, don't do a post and
 		 * tell the user we couldn't report in
 		 */
 		
 		SharedPreferences prefs = context.getSharedPreferences(NearMeActivity.TAG, Context.MODE_PRIVATE);
 		Log.i(NearMeActivity.TAG, "poll triggered for " + prefs.getString(PreferencesActivity.KEY_ID, null));
 		
 		/* If we have no GPS, there's nothing we can usefully ask for - so tell the user and return */
 		
 		if (prefs.getString("time", null)==null) {
 			Log.i(NearMeActivity.TAG, "no GPS yet, don't report");
 			Toast toast=Toast.makeText(context, context.getString(R.string.no_gps_error), 2000);
 			toast.setGravity(Gravity.TOP, -30, 50);
 			toast.show();
 			return;
 		}
 		
 		/* Pull variables we need to poll - lat, long and radius - out of shared preferences */
 
 		int radius = prefs.getInt(PreferencesActivity.KEY_RADIUS, 0);
 		String tmpStr = prefs.getString(PreferencesActivity.KEY_LNG,null);
 		double lng = Float.MIN_VALUE;
 		if (tmpStr!=null) lng = Double.parseDouble(tmpStr);
 
 		tmpStr = prefs.getString(PreferencesActivity.KEY_LAT,null);
 		double lat = Float.MIN_VALUE;
 		if (tmpStr!=null) lat = Double.parseDouble(tmpStr);
 
 		String types = prefs.getString("types", "");
 		
 		try {
 
    			String myUrl = NearMeActivity.ENDPOINT
    					+ "/nearme/"
    					+ prefs.getString(PreferencesActivity.KEY_ID, "")
    					+ "/"
    					+ lat
    					+ "/"
    					+ lng
    					+ "/"
    					+ radius;
 			
    			/* types are an optional parameter */
    			
    			if (types.length()>0) {
 				myUrl = myUrl + "?t=" + types;
    			}
 				
 			Log.d(NearMeActivity.TAG, "get="+myUrl);
 
 			/* Create a new HTTPClient to do our GET for us */
 
 			HttpGet get = new HttpGet(myUrl);   					
 			HttpClient client = new DefaultHttpClient();
 			HttpResponse response = client.execute(get);
 			String responseBody = HttpHelper.getResponseBody(response);
 				
 			/* get the Poi objects out of the string returned using Gson to deserialise
 			 * ref: http://benjii.me/2010/04/deserializing-json-in-android-using-gson/
 			 */
 				
 			Gson gson = new Gson();
 			ArrayList<Poi> newPois = new ArrayList<Poi>();
 			boolean gotNewPoi = false; /* flag: did we get anything new this time around? */
 
 		    JsonParser parser = new JsonParser();
 		    JsonArray array = parser.parse(responseBody).getAsJsonArray();
 			NearMeApplication app = (NearMeApplication) context.getApplicationContext();
 		    
 		    for(JsonElement counter : array)
 		    {	
 		    	// run through the JsonArray converting each entry into an actual Poi object in the Poi ArrayList
 		    	Poi p = gson.fromJson(counter, Poi.class);
 		    	if (!app.getPois().contains(p)) gotNewPoi = true;
 		    	newPois.add(p);
 		    }
 
 		    /* Save our current set of POIs into the shared state of the app */
 		    
 			app.setPois(newPois);
 			Log.d(NearMeActivity.TAG,"PoiPoller received " + newPois.size() + " POIs");
 		    
 		    /* If we received any new Pois as part of this update, then alert the user
 		     * by vibrating, and signal the NearMeActivity to refresh its map view
 		     */
 		    
 		    if (gotNewPoi) {
 		    	Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 		    	vib.vibrate(300);
 		    	Intent i = new Intent("refresh-map");
 		    	context.sendBroadcast(i);
 		    }
 			
 		} catch (Exception e) {
 			Log.i( NearMeActivity.TAG, "get to getPOI failed, " + e.getMessage());
 		}
 		Log.i(NearMeActivity.TAG, "poll done");
 	}
 }
