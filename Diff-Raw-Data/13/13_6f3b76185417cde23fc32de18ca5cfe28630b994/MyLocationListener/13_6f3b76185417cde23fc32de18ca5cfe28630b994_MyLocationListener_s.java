 package com.csc591.view;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class MyLocationListener implements LocationListener
 {
 	public interface onMyLocationChangeHandler{
 		public void onMyLocationChanged(Location location);		
 	}
 	
 	public onMyLocationChangeHandler parentActivity;
 	
     private static double latitude =0.0;
     private static double longitude = 0.0;
     private static double altitude = 0.0; 
     private static double speed = 0.0;
 
     public static double getLat()
     {
         return latitude;
     }
 
     public static double getLon() 
     {
         return longitude;
     }
 
     public static double getAlt()
     {
         return altitude;
     }
 
     public static double getSpeed()
     {
         return speed;
     }
 
     @Override
     public void onLocationChanged(Location location) 
     {
         latitude = location.getLatitude();
         longitude = location.getLongitude();
         altitude = location.getAltitude();
         speed = location.getSpeed(); 
         
        parentActivity.onMyLocationChanged(location);
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     	//Toast.makeText(, "Device GPS is disabled",Toast.LENGTH_SHORT ).show();
     }
     @Override
     public void onProviderEnabled(String provider) {}
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {}
 }
