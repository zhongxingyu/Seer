 /*
  * Copyright 2013 Walter Hugo Palladino
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.whp.android.location;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 import com.whp.android.Log;
 
 /**
  * @author Walter Hugo Palladino
  * @since 26/12/2013
  * 
  */
 public class CustomLocationListener implements LocationListener, OnLocationChangeListener {
 
 	private static final String TAG = CustomLocationListener.class.getCanonicalName ();
 
 	public static final String LOCATION_LISTENER_BROADCAST_ACTION = "LOCATION_LISTENER_BROADCAST_ACTION";
	public static final String LOCATION_NEW = "LOCATION_NEW";
	public static final String LOCATION_PREV = "LOCATION_PREV";
 
 	private static final int MAX_SAMPLE_TIME = 2 * 60 * 1000;
 	private static final long MAX_ACCURACY_DELTA = 200;
 
 	private static final float MIN_ACCURACY = 50;
 	private float minAccuracy = MIN_ACCURACY;
 
 	private Context context;
 
 	private LocationData previousBestLocation;
 	private LocationData lastBestLocation;
 
 	/**
 	 * @return the lastBestLocation
 	 */
 	public LocationData getLastBestLocation() {
 		return lastBestLocation;
 	}
 
 	public CustomLocationListener (Context context) {
 		Log.d (TAG, "CustomLocationListener");
 		this.context = context;
 		this.previousBestLocation = null;
 
 	}
 
	@Override
 	public void onLocationChanged(final Location loc) {
 		Log.d (TAG, "onLocationChanged");
 
 		Log.d (TAG,
 				"Accuracy : " + loc.getAccuracy () + " Latitude :" + loc.getLatitude () + " Longitude : "
 						+ loc.getLongitude () + " Altitude : " + loc.getAltitude ());
 
 		if (loc.getAccuracy () > getMinAccuracy ()) {
 			return;
 		}
 
 		LocationData newLocation = new LocationData (loc);
 
 		if (LocationUtils.isBetterLocation (newLocation, previousBestLocation, MAX_SAMPLE_TIME, MAX_ACCURACY_DELTA)) {
 
 			Log.d (TAG, "New Location Acquired");
 
 			this.previousBestLocation = this.lastBestLocation;
 			this.lastBestLocation = newLocation;
 
 			Log.d (TAG, lastBestLocation.toString ());
 
 			if ((this.previousBestLocation != null) && (this.lastBestLocation != null)) {
 				double distance = LocationUtils.getDistanceBetweenLocations (this.lastBestLocation,
 						this.previousBestLocation);
 				Log.d (TAG, "distance (Km) : " + distance);
 			}
 
 			execute (this.previousBestLocation, this.lastBestLocation);
 
 		}
 	}
 
 	public void onProviderDisabled(String provider) {
 		Log.d (TAG, "onProviderDisabled : " + provider);
 	}
 
 	public void onProviderEnabled(String provider) {
 		Log.d (TAG, "onProviderEnabled : " + provider);
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		Log.d (TAG, "onStatusChanged : " + provider + " status " + status);
 	}
 
 	@Override
 	public void execute(LocationData prevLocation, LocationData newLocation) {
 		Log.d (TAG, "execute");
 
 		Intent intent;
 		intent = new Intent (LOCATION_LISTENER_BROADCAST_ACTION);
 
		intent.putExtra(LOCATION_NEW, newLocation);
		intent.putExtra(LOCATION_PREV, prevLocation);
 
 		this.context.sendBroadcast (intent);
 
 	}
 
 	/**
 	 * @return the minAccuracy
 	 */
 	public float getMinAccuracy() {
 		return minAccuracy;
 	}
 
 	/**
 	 * @param minAccuracy
 	 *            the minAccuracy to set
 	 */
 	public void setMinAccuracy(float minAccuracy) {
 		this.minAccuracy = minAccuracy;
 	}
 
 }
