 /*
  * Copyright 2012 Sergio Garcia Villalonga (yayalose@gmail.com)
  *
  * This file is part of PalmaBici.
  *
  *    PalmaBici is free software: you can redistribute it and/or modify
  *    it under the terms of the Affero GNU General Public License version 3
  *    as published by the Free Software Foundation.
  *
  *    PalmaBici is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    Affero GNU General Public License for more details
  *    (https://www.gnu.org/licenses/agpl-3.0.html).
  *    
  */
 
 package com.poguico.palmabici.syncronizers;
 
 import java.util.ArrayList;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.poguico.palmabici.SynchronizableActivity;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class LocationSynchronizer {
 	private static Location 		  location;
 	private static LocationManager  manager;
 	private static LocationListener listener;
 	
 	private static ArrayList<SynchronizableActivity> synchronizable_activities;
 	
 	public static void init(Context context) {
 		manager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		listener = new LocationListener() {
 
 			@Override
 			public void onLocationChanged(Location location) {
 				LocationSynchronizer.location = location;
 				updateViews();
 			}
 			
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 			
 			@Override
 			public void onProviderEnabled(String provider) {}
 			
 			@Override
 			public void onProviderDisabled(String provider) {
 				if ((provider.equals(LocationManager.GPS_PROVIDER) &&
 						!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) ||
 					(provider.equals(LocationManager.NETWORK_PROVIDER) &&
 						!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
 					location = null;
 					updateViews();
 				}					
 			}
 		};
 		
 		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 0, listener);
 		manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 0, listener);
 		
 		if (location == null)
 			location =  manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		
 		if (location == null)
 			location =  manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 				
 		synchronizable_activities = new ArrayList<SynchronizableActivity>();
 	}
 	
 	private static void updateViews () {
 		for (SynchronizableActivity activity : synchronizable_activities)
 			activity.onLocationSynchronization();
 	}
 	
 	public static void addSynchronizableActivity(SynchronizableActivity activity) {
 		synchronizable_activities.add(activity);
 	}
 	
 	public static Location getLocation () {
 		return location;
 	}
 }
