 package edu.mit.csail.sensors;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import edu.mit.csail.ada.Global;
 
 public class GPS {
 	private static LocationManager locationManager;
     private static LocationListener locationListener;
     private static List<Location> locList = new ArrayList<Location>(Global.LOOKBACK_NUM);
     private static double aveSpeed = Global.INVALID_FEATURE;
     private static int listSize = 0;
     private static boolean isWorking = false;
     
 	public static void init(){
 		locationManager = (LocationManager)Global.context.getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new GPSLocationListener();	
 	}
 	
 	public static void start(int sampling_interval_sec){
 		isWorking = true;
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, sampling_interval_sec * 1000, 0, locationListener);
 		//locationManager.
 	}
 	
 	public static boolean isWorking(){
 		return isWorking;
 	}
 	
 	static class GPSLocationListener implements LocationListener 
     {
         public void onLocationChanged(Location loc) 
         {		
         	if (listSize == Global.LOOKBACK_NUM){
         		locList.remove(0);
         		--listSize;
         	}
         	System.out.println("GPS received: " + loc.getLatitude() + "," + loc.getLongitude());
         	
         	locList.add(loc);
         	++listSize;
         	updateSpeed();
         } 
         public void onProviderDisabled(String provider) 
         {}
         public void onProviderEnabled(String provider) 
         {}
         public void onStatusChanged(String provider, int status, Bundle extras) 
         {}
     }
 	
 	public static double getFeature(){
 		return aveSpeed;
 	}
 	
 	public static double updateSpeed(){
 		
 		if (locList.size() == 0){
 			aveSpeed = Global.INVALID_FEATURE;
 			return aveSpeed;
 		}
 		
 		double tmpSpeed = 0.0;
 		for (int i = 0; i < locList.size(); ++i){
 			tmpSpeed += locList.get(i).getSpeed();
 		}
 		
 		aveSpeed = tmpSpeed / (locList.size() * 1.0);
 		return aveSpeed;
 	}
 	
 	public static void stop(){
 		isWorking = false;
 		listSize = 0;
		aveSpeed = Global.INVALID_FEATURE;
 		locationManager.removeUpdates(locationListener);
 	}
 }
