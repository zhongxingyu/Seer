 package org.routy.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.routy.exception.NoInternetConnectionException;
 import org.routy.exception.NoLocationProviderException;
 import org.routy.log.Log;
 import org.routy.model.AppConfig;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 /**
  * A service class that gives access to location obtaining tasks.  Instances of this 
  * class will usually be used to get the user's current location.  The class handles 
  * all the under-the-covers mechanics of getting a location from Android.  Implement 
  * the onLocationResult(...) method in order to be notified when a location has been 
  * obtained.
  * 
  * 
  * @author jtran
  *
  */
 public abstract class LocationService {
 
 	private final String TAG = "LocationService";
 	private final double accuracy;
 	private final LocationManager manager;
 	
 	private long startTimeMs;
 	
 	
 	public LocationService(LocationManager locManager, double accuracy) {
 		this.manager = locManager;
 		this.accuracy = accuracy;
 	}
 	
 	
 	/**
 	 * Called whenever a location update has been received.
 	 * 
 	 * @param location
 	 */
 	public abstract void onLocationResult(Location location);
 	
 	
 	public abstract void onLocationSearchTimeout();
 	
 	
 	/**
 	 * Gets the BEST fix on the user's current location based on 
 	 * coarse and fine locations provided by Android.  Uses the 
 	 * last known location in case other apps/services on the phone 
 	 * have recently gotten a location fix within the accuracy threshold.
 	 * 
 	 * @throws	NoInternetConnectionException if there are no location providers (network or GPS) enabled
 	 */
 	public void getCurrentLocation() throws NoLocationProviderException {
 		Log.v(TAG, "getting current location");
 		
 		Location lastKnownLoc = getLastKnownLocation();
 		if (lastKnownLoc != null) {
 			Log.v(TAG, "Last known location was good...using it.");
 			onLocationResult(lastKnownLoc);
 		} else {
 			startTimeMs = System.currentTimeMillis();
 			
 			// Keep requesting location updates until the error is below the threshold
 			boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 			boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			
 			if (networkEnabled || gpsEnabled) {
 				if (gpsEnabled) {
 					Log.v(TAG, "gps enabled");
 		        	manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
 		        }
 		        
 		        if (networkEnabled) {
 		        	Log.v(TAG, "network enabled");
 		        	manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
 		        }
 			} else {
 				Log.e(TAG, "No network or gps provider.");
 				throw new NoLocationProviderException("No location providers enabled.");
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * Attempts to get the last known location (within the required expiration time and minimum accuracy) from the Android system.
 	 * 
 	 * @return		the last known location or null if there is none or if none of them meet the requirements
 	 */
 	private Location getLastKnownLocation() {
 		long bestTimeMs = 0;
 		
 		Location lastKnownLocation = null;
 		
 		List<String> providers = manager.getProviders(true);
 		for (String provider : providers) {
 			Location candidate = manager.getLastKnownLocation(provider);
 			
 			if (candidate != null) {
 				// Last loc update is within our expiration time and is newer than any we've found before
 				if (((new Date()).getTime() - candidate.getTime()) <= AppConfig.LOCATION_EXPIRE_TIME_MS && candidate.getTime() > bestTimeMs) {
 					
 					// Last loc has an accuracy within our threshold
 					if (candidate.hasAccuracy() && candidate.getAccuracy() <= accuracy) {
 						lastKnownLocation = candidate;
 						bestTimeMs = candidate.getTime();
 					}
 				}
 			}
 		}
 		
 		return lastKnownLocation;
 	}
 
 
 	/**
 	 * The listener that {@link LocationService} uses to be notified of location updates.
 	 */
 	private LocationListener listener = new LocationListener() {
 		
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.v(TAG, "provider status changed");
 		}
 		
 		@Override
 		public void onProviderEnabled(String provider) {
 
 		}
 		
 		@Override
 		public void onProviderDisabled(String provider) {
 
 		}
 		
 		@Override
 		public void onLocationChanged(Location location) {
 			if ((System.currentTimeMillis() - startTimeMs) > AppConfig.LOCATION_FETCH_TIMEOUT_MS) {
 				Log.v(TAG, "user locating timed out");
 				stop();
 				onLocationSearchTimeout();
 			} else {
 				Log.v(TAG, "location changed via " + location.getProvider());
 				if (location.hasAccuracy()) {
 					Log.v(TAG, "location has accuracy: " + location.getAccuracy());
 				} else {
 					Log.v(TAG, "location has no accuracy.");
 				}
 				
 				if (location.getAccuracy() <= accuracy) {
 					Log.v(TAG, "Got a good fix");
 					stop();
 					onLocationResult(location);
 				} else {
 					Log.v(TAG, "Location has a bad accuracy: " + location.getAccuracy());
 				}
 			}
 		}
 	};
 	
 	
 	/**
 	 * Releases all of this location service's holds on any system location resources.<br/>
 	 * (ie. un-registers {@link LocationListener} objects from the {@link LocationManager})
 	 */
 	public void stop() {
 		Log.v(TAG, "Stopping Location Service");
 		manager.removeUpdates(listener);
 	}
 	
 	
 	public boolean isGpsEnabled() {
 		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 	}
 }
