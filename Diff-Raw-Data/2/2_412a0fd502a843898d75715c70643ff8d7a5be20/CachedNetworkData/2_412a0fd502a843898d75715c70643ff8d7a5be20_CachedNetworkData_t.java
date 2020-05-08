 package com.coffeeandpower.cache;
 
 import java.util.Observable;
 
 import android.location.Location;
 import android.util.Log;
 
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.cont.DataHolder;
 import com.google.android.maps.GeoPoint;
 
 public class CachedNetworkData extends Observable{
 
 	private boolean isActive;
 	private boolean hasData;
 	
 	private Location userLocationWhenDataCollected = new Location("userDataLocation");
 	
 	private String type;
 	
 	private DataHolder cachedData;
 		
 	public CachedNetworkData(String myType) {
 		isActive = false;
 		hasData = false;
 		this.type = myType;
 	}
 	
 	public void activate() {
 		this.isActive = true;
 	}
 	
 	public void deactivate() {
 		this.isActive = false;
 	}
 	
 	public boolean isActive() {
 		return isActive;
 	}
 	
 	public void setNewData(DataHolder newData, double[] userLocation) {
 				
		if (newData.getResponseMessage().equals("HTTP 200 OK")) {
 			cachedData = newData;
 			//This gets called so often it makes a mess
 			//Log.d("CachedNetworkData","Setting user location to: " + userLocation[0] + ", " + userLocation[1]);
 			userLocationWhenDataCollected.setLatitude(userLocation[0]);
 			userLocationWhenDataCollected.setLongitude(userLocation[1]);
 			if (Constants.debugLog)
 				Log.d("CachedNetworkData","Sending notifyObservers with received data from API call: " + type + "...");
 	                    
                         // Send notify for nearby venues
 			hasData = true;
                         setChanged();
                         notifyObservers(new CachedDataContainer(cachedData));
             	} else {
             		if (Constants.debugLog)
         			Log.d("CachedNetworkData","Skipping notifyObservers for API call: " + type);
                     	    
             	}
 	}
 	
 	/*
 	 * returns distance in meters from a given lat/lon
 	 */
 	public double dataDistanceFrom(double[] llArray) {
 		
 		
 		Location testLoc = new Location("testLoc");
 		testLoc.setLatitude(llArray[0]);
 		testLoc.setLongitude(llArray[1]);
 		
 		return userLocationWhenDataCollected.distanceTo(testLoc);
 		
 	}
 	
 	public void sendCachedData() {
 		if (hasData) {
 			if (Constants.debugLog)
 				Log.d("CachedNetworkData","Sending cached data for API: " + this.type + "...");
 			setChanged();   // Not sure if this is necessary
 			notifyObservers(new CachedDataContainer(cachedData));
 		}
 	}
 	
 	public DataHolder getData() {
 		return cachedData;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public boolean hasData() {
 		return this.hasData;
 	}
 	
 
 }
