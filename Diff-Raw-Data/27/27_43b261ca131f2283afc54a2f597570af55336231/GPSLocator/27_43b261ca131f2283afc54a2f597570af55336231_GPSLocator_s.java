 package com.app.getconnected.gps;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Settings;
 
 public class GPSLocator implements com.app.getconnected.gps.Location, LocationListener {
 	
 	private double latitude = 0;
 	private double longitude = 0;
 	private Context context;
 
 	public GPSLocator(Context context) {
 		this.context = context;
 		
 		enableGPS();
 		
 		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 
 	    Criteria criteria = new Criteria();
 	    String provider = locationManager.getBestProvider(criteria, false);
 	    Location location = locationManager.getLastKnownLocation(provider);
 
 	    if (location != null) {
 	      System.out.println("Provider " + provider + " has been selected.");
 	      onLocationChanged(location);
 	    } else {
 	      System.out.println("Location not available");
 	    }
 	    
 	}
 	
 	public void enableGPS() {
 		String provider = Settings.Secure.getString(context.getContentResolver(), 
 	    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
 
 	    if(provider.contains("gps")) {
 	        return;
 	    }
 
 	    final Intent intent = new Intent();
 	    intent.setClassName("com.android.settings", 
 	    		"com.android.settings.widget.SettingsAppWidgetProvider");
 	    intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 	    intent.setData(Uri.parse("3"));
 	    context.sendBroadcast(intent);
 }
 	
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public double getLongitude() {
 		return longitude;
 	}
 	
 	public boolean isValidLocation() {
		return true;
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		latitude = location.getLatitude();
 	    longitude = location.getLongitude();
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
