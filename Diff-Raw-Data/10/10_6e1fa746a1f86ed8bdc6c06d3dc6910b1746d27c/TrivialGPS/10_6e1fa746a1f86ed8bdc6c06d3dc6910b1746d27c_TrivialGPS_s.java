 package com.droidworks.examples.trivialgps;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 
 /**
 * Sample Application to demonstrate how to use some of the
 * android Location API's
 * 
 * Author: Jason Hudgins <jason@droidworks.com>
 */
 public class TrivialGPS extends MapActivity {
 	
 	public static final String UPDATE_LOC = "com.test.TrivialGPS.LOC_UPDATE";
 	private static final String MAP_API_KEY = "foo";
 	
 	private MapController mapController;
 	private MapView mapView;
 	private LocationManager locationManager;
 	
     /** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle icicle) {
 	   super.onCreate(icicle);
 
 	   // create a map view
 	   mapView = new MapView(this, MAP_API_KEY);
 	   mapController = mapView.getController();
 	   mapController.setZoom(22);
 	   setContentView(mapView);
 
 	   // get a hangle on the location manager
 	   locationManager =
 	     (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
	   locationManager.requestLocationUpdates("gps", 0, 0, 
 			   new LocationUpdateHandler());
 	}
         
     // this inner class is the intent reciever that recives notifcations
     // from the location provider about position updates, and then redraws
     // the MapView with the new location centered.
     public class LocationUpdateHandler implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location loc) {
         	Double lat = loc.getLatitude()*1E6;
         	Double lng = loc.getLongitude()*1E6;
         	GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());
         	mapController.setCenter(point);
         	
             setContentView(mapView);
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {}
 
 		@Override
 		public void onProviderEnabled(String provider) {}
 
 		@Override
 		public void onStatusChanged(String provider, int status, 
 				Bundle extras) {}
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 }
