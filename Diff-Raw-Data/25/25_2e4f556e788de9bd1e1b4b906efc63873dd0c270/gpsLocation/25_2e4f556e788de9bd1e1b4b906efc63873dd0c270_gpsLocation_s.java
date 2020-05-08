 package com.datakom;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.util.Log;
 
public class GpsLocation implements LocationListener {
 	private double longitude;
 	private double latitude;
 	
 	@Override
 	public void onLocationChanged(Location loc) {
 		// Pick out the latitude resp longitude
 		loc.getLatitude();
 		loc.getLongitude();
 		
 		this.longitude = loc.getLongitude();
 		this.latitude = loc.getLatitude();
 		
 		Log.e(getClass().getSimpleName(), ": Longitude " + this.longitude);
 		Log.e(getClass().getSimpleName(), ": Latitude " + this.latitude);
 		
 	}
 	
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		Log.e(getClass().getSimpleName(), ": GPS disabled. Can not get coordinates. ");		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		Log.e(getClass().getSimpleName(), ": GPS enabled.");
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 }
