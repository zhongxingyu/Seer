 package com.gethotdrop.service;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 public class UpdateService extends Service {
 	public LocationManager locManager;
 	Cache cache;
 	private long minTime = (1000) * 60 * (1 / 2);
 	private float minDistance = 15F;
 	private String provider = LocationManager.NETWORK_PROVIDER;
 
 	/**
 	 * Service Events
 	 */
 	@Override
 	public void onCreate() {
 		
 		Log.e("service", "This is a service");
 		locManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 		// get singletons
 		cache = Cache.getInstance();
 		// location update
 		long minTime = (1000) * 60 * (1 / 2);
 		float minDistance = 15F;
 		String provider = LocationManager.NETWORK_PROVIDER;
 		locManager.requestLocationUpdates(provider, minTime, minDistance,
 				locListener);
 
 		// data update
 		m_interval = 1000 * 60 * (2 / 10);
 		update = new Updater(locManager, cache, m_handler, m_interval, this);
 		update.run();
 	}
	
 	@Override
 	public IBinder onBind(Intent intent) {
 		
 		// TODO Auto-generated method stub
 		return null;
 		
 	}
 
 	/**
 	 * Location Listener
 	 */
 	public LocationListener locListener = new LocationListener() {
 
 		@Override
 		public void onLocationChanged(Location arg0) {
 			update.reset();
 		}
 
 		@Override
 		public void onProviderDisabled(String arg0) {
 		}
 
 		@Override
 		public void onProviderEnabled(String arg0) {
 		}
 
 		@Override
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		}
 	};
 	
 	/**
 	 * Refresh Heartbeat Timer
 	 */
 	long m_interval;
 	Handler m_handler = new Handler();
 	Updater update;
 	
 	
 	
 	
 }
