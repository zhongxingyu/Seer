 package com.barefoot.pocketshake.storage;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.barefoot.pocketshake.data.EarthQuake;
 import com.barefoot.pocketshake.filters.FilterStore;
 import com.barefoot.pocketshake.storage.EarthQuakeDatabase.EarthquakeCursor;
 
 public class EarthQuakeDataWrapper {
 	
 	private EarthQuakeDatabase db;
 	final private static String LOG_TAG = "EarthQuakeDataWrapper";
 	private static ArrayList<EarthQuake> cachedEarthQuakeFeed = new ArrayList<EarthQuake>();
 	private Context context;
 	private FilterStore filterStore;
 	
 	
 	public EarthQuakeDataWrapper(Context context) {
 		this.context = context;
 		db = new EarthQuakeDatabase(context);
 		filterStore = new FilterStore(context);
 	}
 	
 	public synchronized void refreshFeedCache(boolean force) {
 		Log.i(LOG_TAG, "Refreshing the cache feed from db. The force flag value is ["+force+"] and the size of cache elements is :: " + cachedEarthQuakeFeed.size());
 		if(cachedEarthQuakeFeed.size() == 0 || force) {
 			cachedEarthQuakeFeed.clear();
 			//minimum intensity filter is done as part of filtering process however doing it 
 			//here to ensure a faster processing and lesser battery usage. Not ideal way though.
 			EarthquakeCursor allEarthquakes = db.getEarthquakes(getCurrentMinIntensity());
 			EarthQuake currentEarthquake = null;
 			try {
 				if(allEarthquakes != null && allEarthquakes.moveToFirst()) {
 					do {
 						currentEarthquake = allEarthquakes.getEarthQuake();
 						if(filterStore.pass(currentEarthquake))
 							cachedEarthQuakeFeed.add(currentEarthquake);
 					} while(allEarthquakes.moveToNext());
 				}
 			} finally {
				if(!allEarthquakes.isClosed())
					allEarthquakes.close();
 			}
 		}
 	}
 	
 	private int getCurrentMinIntensity() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 		return sharedPref.getInt("intensity_setting", 0);
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
 
 
