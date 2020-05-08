 package carnero.netmap.common;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Geolocation component operating with NETWORK provider only
  */
 public class Geo {
 
 	private LocationManager mManager;
 	private Location mLastLocation;
 	final private GeoListener mListener = new GeoListener();
 	final private ArrayList<SimpleGeoReceiver> mReceivers = new ArrayList<SimpleGeoReceiver>();
 
 	public Geo(Context context) {
 		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 
 		loadLastLoc();
 
 		List<String> providers = mManager.getAllProviders();
		if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			mListener.provider = LocationManager.NETWORK_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);
 		} else if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
 			mListener.provider = LocationManager.PASSIVE_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);
 		} else {
 			Log.e(Constants.TAG, "Neither NETWORK nor PASSIVE provider available.");
 		}
 	}
 
 	public void addReceiver(SimpleGeoReceiver receiver) {
 		mReceivers.add(receiver);
 
 		if (mLastLocation != null) {
 			receiver.onLocationChanged(mLastLocation);
 		}
 	}
 
 	public void removeReceiver(SimpleGeoReceiver receiver) {
 		if (mReceivers.contains(receiver)) {
 			mReceivers.remove(receiver);
 		}
 	}
 
 	/**
 	 * Release resources before abandoning object
 	 */
 	public void release() {
 		mReceivers.clear();
 
 		if (mManager != null && mListener != null) {
 			mManager.removeUpdates(mListener);
 		}
 	}
 
 	/**
 	 * Load last known location and use it if newer, or missing
 	 */
 	private void loadLastLoc() {
 		Location location = mManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		if (location == null) {
 			location = mManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
 		}
 
 		if (mLastLocation == null || mLastLocation.getTime() < location.getTime()) {
 			mLastLocation = location;
 		}
 	}
 
 	// class
 
 	public class GeoListener implements LocationListener {
 
 		public String provider;
 
 		public void onLocationChanged(Location location) {
 			for (SimpleGeoReceiver receiver : mReceivers) {
 				receiver.onLocationChanged(location);
 			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			// empty
 		}
 
 		public void onProviderEnabled(String provider) {
 			// empty
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// empty
 		}
 	}
 }
