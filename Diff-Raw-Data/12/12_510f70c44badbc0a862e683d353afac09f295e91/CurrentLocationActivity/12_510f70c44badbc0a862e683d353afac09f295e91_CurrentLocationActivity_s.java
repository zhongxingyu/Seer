 package com.barview;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.barview.constants.BarviewConstants;
 import com.barview.rest.NearbyBarFetcher;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class CurrentLocationActivity extends MapActivity implements LocationListener {
 
 	MapView mapView;
 	
 	MapController mapController;
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.currentlocation);
 		
 		mapView = (MapView) findViewById(R.id.currloc_mapview);
 		mapView.setBuiltInZoomControls(true);
 		
 		
 		// Get a handle for the controller of our MapView.
 		mapController = mapView.getController();
 	}
 	
 	/**
 	 * Override the onResume callback so we can turn the GPS updates back on.  The updates
 	 * are stopped whenever we leave this Activity.
 	 */
 	public void onResume() {
 		super.onResume();
 		Log.i("onResume", "In onResume, about to start up the LocationManager again.");
 		
 		// Set up the Location Manager
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 50.0f, this);
 		
 		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		int lat = (int) (location.getLatitude() * BarviewConstants.GEOPOINT_MULT);
		int lng = (int) (location.getLongitude() * BarviewConstants.GEOPOINT_MULT);
		GeoPoint p = new GeoPoint(lat, lng);
		mapController.animateTo(p);
 	}
 	
 	/**
 	 * Shut off GPS updates whenever we leave this Activity.  This is necessary so we don't
 	 * kill the battery if/when the user leaves the application.
 	 */
 	public void onPause() {
 		super.onPause();
 		Log.i("onPause", "In onPause, about to shut down LocationManager updates.");
 		
 		// Stop the Location Manager
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.removeUpdates(this);
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void onLocationChanged(Location location) {
 		if (location != null) {
 			double lat = location.getLatitude();
 			double lng = location.getLongitude();
 			int latInt = (int) (location.getLatitude() * BarviewConstants.GEOPOINT_MULT);
 			int lngInt = (int) (location.getLongitude() * BarviewConstants.GEOPOINT_MULT);
 			GeoPoint p = new GeoPoint(latInt, lngInt);
 			mapController.animateTo(p);
 			
 			// Grab any nearby bars and annotate them on the map
 			NearbyBarFetcher fetcher = new NearbyBarFetcher();
 			fetcher.setMapView(mapView);
 			fetcher.setMe(this);
 			fetcher.execute(lat, lng);
 		}
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
