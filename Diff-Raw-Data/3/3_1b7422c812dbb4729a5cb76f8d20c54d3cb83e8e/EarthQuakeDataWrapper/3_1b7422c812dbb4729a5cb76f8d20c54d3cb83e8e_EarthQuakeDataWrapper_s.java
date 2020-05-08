 package com.barefoot.pocketshake.storage;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.barefoot.pocketshake.data.EarthQuake;
 import com.barefoot.pocketshake.service.ReferencePointCalculator;
 import com.barefoot.pocketshake.storage.EarthQuakeDatabase.EarthquakeCursor;
 
 public class EarthQuakeDataWrapper {
 	
 	private EarthQuakeDatabase db;
 	final private static String LOG_TAG = "EarthQuakeDataWrapper";
 	private static ArrayList<EarthQuake> cachedEarthQuakeFeed = new ArrayList<EarthQuake>();
 	private Context context;
 	private ReferencePointCalculator refCalculator;
 	
 	public EarthQuakeDataWrapper(Context context) {
 		this.context = context;
 		db = new EarthQuakeDatabase(context);
 		refCalculator = new ReferencePointCalculator(context);
 	}
 	
 	public synchronized void refreshFeedCache(boolean force) {
 		Log.i(LOG_TAG, "Refreshing the cache feed from db. The force flag value is ["+force+"] and the size of cache elements is :: " + cachedEarthQuakeFeed.size());
 		if(cachedEarthQuakeFeed.size() == 0 || force) {
 			EarthquakeCursor allEarthquakes = db.getEarthquakes(getCurrentMinIntensity());
 			EarthQuake currentEarthquake = null;
 			try {
 				if(allEarthquakes != null && allEarthquakes.moveToFirst()) {
					cachedEarthQuakeFeed.clear();
 					do {
 						currentEarthquake = allEarthquakes.getEarthQuake();
 						if(!withinRadius(currentEarthquake))
 							continue;
 						cachedEarthQuakeFeed.add(currentEarthquake);
 					} while(allEarthquakes.moveToNext());
 				}
 			} finally {
 				allEarthquakes.close();
 			}
 		}
 	}
 	
 	private boolean withinRadius(EarthQuake currentEarthquake) {
 		Location userLocation = refCalculator.getReferencePoint();
 		if( userLocation == null) 
 			return true;
 		Location quakeLocation = new Location("Quake Location");
 		quakeLocation.setLatitude(currentEarthquake.getMicroLongitudes()/10E5);
 		quakeLocation.setLongitude(currentEarthquake.getMicroLatitudes()/10E5);
 		return ((quakeLocation.distanceTo(userLocation)/1000) < getCurrentRadius());
 	}
 
 	private int getCurrentMinIntensity() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 		return sharedPref.getInt("intensity_setting", 0);
 	}
 	
 	private int getCurrentRadius() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 		return sharedPref.getInt("radius_value", 12000);
 	}
 
 	public ArrayList<EarthQuake> getEarthQuakes() {
 		Log.i(LOG_TAG, "Returning arraylist containing earthquake details");
 		return cachedEarthQuakeFeed;
 	}
 	
 	 public EarthQuake getQuakeAtPosition(int index) {
 		 if (index <= cachedEarthQuakeFeed.size()) {
 			 return cachedEarthQuakeFeed.get(index);
 		 }
 		 return null;
 	 }
 	
 	public synchronized void clearCache() {
 		Log.i(LOG_TAG, "Clearing the earthquake cache!!");
 		cachedEarthQuakeFeed.clear();
 	}
 	
 	protected void finalize() throws Throwable {
 	    try {
 	    	Log.i(LOG_TAG, "Closing Database connection");
 	        db.close();        // close database
 	    } finally {
 	        super.finalize();
 	    }
 	}
 }
 
 
