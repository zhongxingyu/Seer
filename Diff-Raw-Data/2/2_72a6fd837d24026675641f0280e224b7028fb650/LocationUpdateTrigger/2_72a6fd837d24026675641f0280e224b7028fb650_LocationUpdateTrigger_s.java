 package com.alphadog.tribe.services;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.alphadog.tribe.helpers.LocationHelper;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class LocationUpdateTrigger {
 	
 	private LocationManager locationManager;
 	private boolean gpsSupported = false;
 	private boolean networkSupported = false;
 	private LocationResultExecutor locationResult;
 	private long timetoWaitForRealTimeUpdate;
 	private LocationHelper locationHelper;
 	
 	public static final int LOCATION_NOTIFICATION = 1;
 	
 	public LocationUpdateTrigger(Context context, LocationResultExecutor resultExecutor) {
 		//By default we'll wait for a minute to get real time updates for location
 		//before reverting back to the last known location.
 		this(context, 60000, resultExecutor);
 	}
 
 	public LocationUpdateTrigger(Context context, long timetoWaitForRealTimeUpdate, LocationResultExecutor resultExecutor) {
 		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		this.locationHelper = new LocationHelper(locationManager);
 		this.timetoWaitForRealTimeUpdate = timetoWaitForRealTimeUpdate;
 		this.locationResult = resultExecutor;
 		Log.d("LocationUpdateTrigger", "We'll wait for "+ timetoWaitForRealTimeUpdate + "milliseconds before we go with the last known location");
 	}
 
 	//listener to use with gps
 	private LocationListener gpsLocationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			Log.d("LocationUpdateTrigger", "We'd like to think we got a fix on the location from GPS! : " + location);
 			if(lastKnowLocationTimerTask != null)
 				lastKnowLocationTimerTask.cancel();
 			locationManager.removeUpdates(this);
 			locationManager.removeUpdates(networkLocationListener);
 			locationResult.executeWithUpdatedLocation(location);
 		}
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 		public void onProviderEnabled(String provider) {
 		}
 		public void onProviderDisabled(String provider) {
 		}
 	};
 
 	//listener to use with network
 	private LocationListener networkLocationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			Log.d("LocationUpdateTrigger", "We'd like to think we got a fix on the location from Network! : " + location);
 			if(lastKnowLocationTimerTask != null)
 				lastKnowLocationTimerTask.cancel();
 			locationManager.removeUpdates(this);
 			locationManager.removeUpdates(gpsLocationListener);
 			locationResult.executeWithUpdatedLocation(location);
 		}
 		public void onStatusChanged(String provider, int status, Bundle extras) {			
 		}
 		public void onProviderEnabled(String provider) {
 		}
 		public void onProviderDisabled(String provider) {
 		}
 	};	
 	
 	private TimerTask lastKnowLocationTimerTask = new TimerTask() {
 		@Override
 		public void run() {
 			Log.d("LocationUpdateTrigger", "Seems like neither the GPS not the Network could give a fix on location so we are going with last known.");
 			//Since we have to eventually wait for timer task to run
 			//we should cancel the updates from network and gps and 
 			//go with last known location
 			locationManager.removeUpdates(networkLocationListener);
 			locationManager.removeUpdates(gpsLocationListener);
 			
 			Location lastKnownLocation = locationHelper.getBestLastKnownLocation();
 
             locationResult.executeWithUpdatedLocation(lastKnownLocation);
 		}
 	};
 	
 	public void fetchLatestLocation() {
 		Log.d("LocationUpdateTrigger", "Will fetch the current location either using network or GPS provider");
 		
 		gpsSupported = locationHelper.isGPSSupported();
 		networkSupported = locationHelper.isNetworkSupported();
 
 		//We have no way to query the locations as nothing is supported
 		if(!networkSupported && !gpsSupported)
 		{
 			Log.i("Location Update", "No location information available!");
 			return;
 		}
 		
 		if(gpsSupported) {
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
 		}
 		
		if(gpsSupported) {
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
 		}
 		
 		//It'll wait specified seconds before it'll trigger itself and cancel the real time
 		//update listeners.
 		new Timer().schedule(lastKnowLocationTimerTask, timetoWaitForRealTimeUpdate);
 	}
 
 	public static abstract class LocationResultExecutor {
 		public abstract void executeWithUpdatedLocation(Location location);
 	}
 
 }
