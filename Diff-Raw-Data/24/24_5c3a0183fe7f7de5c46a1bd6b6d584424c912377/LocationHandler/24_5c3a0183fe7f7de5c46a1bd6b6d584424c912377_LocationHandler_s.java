 package accenture.networkandroid;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 /** Not used
  * 
  * @author Audun Sorheim
  *
  */
 public class LocationHandler {
 
 	private Location currentLocation;
 	private LocationManager locationManager;
 
 	public LocationHandler(Activity activity) {
 
 		//	 Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
 		Log.e("LocationManager",
 				"LM is not null ?:" + locationManager.equals(null));
 
 		currentLocation = locationManager
 		.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 	}
 
 	// Define a listener that responds to location updates
 	LocationListener locationListener = new LocationListener() {
 
//		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 
 		}
 
//		@Override
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
//		@Override
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
//		@Override
 		public void onLocationChanged(Location location) {
 			makeUseOfNewLocation(location);		
 		}
 	};
 
 
 	public Location getCurrentLocation() {
 		return currentLocation;
 	}
 
 	public void makeUseOfNewLocation(Location location) {
 		if (location == null) {
 			Log.e("Location", "Location is null");
 		} else {
 			Log.e("NetworkAndroidActivity",
 					"Latitude: " + location.getLatitude() + " Longitude: "
 					+ location.getLongitude());
 			currentLocation = location;
 		}
 
 	}
 }
