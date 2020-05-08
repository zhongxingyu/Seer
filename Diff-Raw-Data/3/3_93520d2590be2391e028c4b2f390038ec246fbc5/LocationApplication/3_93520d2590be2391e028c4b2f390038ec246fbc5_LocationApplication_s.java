 package com.towson.wavyleaf;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class LocationApplication extends Application{
 
 	private static final long TWO_MINUTES = 120*1000; //IN MS
 	private static final long THIRTY_SECONDS = 30*1000; //IN MS
 	private Location location;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	private boolean isSearching = false;
 	
 	public void init(){
 	
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		
 		//Location Listener
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 		    public void onLocationChanged(Location loc) {
 		      // Called when a new location is found by the network location provider.
 		    	if(isAccurateLocation(loc)){
 		    		if(isBetterLocation(loc,location)){
 		    			setLocation(loc);
 		    		}
 		    	}
 		    }
 	
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 	
 		    public void onProviderEnabled(String provider) {}
 	
 		    public void onProviderDisabled(String provider) {}
 		  };
 	
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, THIRTY_SECONDS, 0, locationListener);
 		
 		isSearching = true;
 	}
 	
 	public void stop(){
		locationManager.removeUpdates(locationListener);
 		isSearching = false;
 	}
 	
 	public Location getLocation(){
 		if(isAccurateLocation(this.location))
 			return location;
 		else
 			return null;
 	}
 	
 	public void setLocation(Location loc){
 		this.location = loc;
 		
 //		Intent intent = new Intent(this, Report.class);
 //		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //		this.startActivity(intent);
 	}
 	
 	private boolean isAccurateLocation(Location location){
 		
 		if(location == null)
 			return false;
 		
 		boolean isRecent = (location.getTime() + TWO_MINUTES) > System.currentTimeMillis();
 		
 		if(isRecent){
 			//if recent, it is accurate
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 	//from developer.android
 	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 	    if (currentBestLocation == null) {
 	        // A new location is always better than no location
 	        return true;
 	    }
 
 	    // Check whether the new location fix is newer or older
 	    long timeDelta = location.getTime() - currentBestLocation.getTime();
 	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 	    boolean isNewer = timeDelta > 0;
 
 	    // If it's been more than two minutes since the current location, use the new location
 	    // because the user has likely moved
 	    if (isSignificantlyNewer) {
 	        return true;
 	    // If the new location is more than two minutes older, it must be worse
 	    } else if (isSignificantlyOlder) {
 	        return false;
 	    }
 
 	    // Check whether the new location fix is more or less accurate
 	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 	    boolean isLessAccurate = accuracyDelta > 0;
 	    boolean isMoreAccurate = accuracyDelta < 0;
 	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 	    // Check if the old and new location are from the same provider
 	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
 	            currentBestLocation.getProvider());
 
 	    // Determine location quality using a combination of timeliness and accuracy
 	    if (isMoreAccurate) {
 	        return true;
 	    } else if (isNewer && !isLessAccurate) {
 	        return true;
 	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
 	        return true;
 	    }
 	    return false;
 	}
 	
 	/** Checks whether two providers are the same */
 	private boolean isSameProvider(String provider1, String provider2) {
 	    if (provider1 == null) {
 	      return provider2 == null;
 	    }
 	    return provider1.equals(provider2);
 	}
 	
 	public boolean isSearching(){
 		return isSearching;
 	}
 	
 }
