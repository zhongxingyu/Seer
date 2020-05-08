 package org.mustard.util;
 
 import java.util.List;
 
import org.mustard.android.MustardApplication;

 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 public class LocationUtil {
 	
 	public static Location getMostRecentLastKnownLocation(LocationManager locationManager) {
 		Location l = null;
 		List<String> providers = locationManager.getProviders(true);
 		long time=0;
		if(MustardApplication.DEBUG)Log.i("LocationUtil","Found " + providers.size() + " providers");
 		for (String provider : providers) {
			if(MustardApplication.DEBUG) Log.i("LocationUtil","Found " + provider + " ");
 			Location location = locationManager.getLastKnownLocation(provider);
 			if (location != null)
				if(location.getTime()>time) {
					time=location.getTime();
 					l=location;
				}
 		}
 		return l;
 	}
 	
 	public static double getDistance(Location location, Location location2) {
 		return getDistance(location.getLatitude(),location.getLongitude(),location2.getLatitude(),location2.getLongitude());
 	}
 	
 	public static double getDistance(double latitude,double longitude, double lat2,double lon2) {
 		double EARTH_RADIUS = 636600;
 
 		double deltalat = lat2 - latitude;
 		double deltalon = lon2 - longitude;
 
 		double a = Math.sin(deltalat / 2) * Math.sin(deltalat / 2) + Math.cos(latitude) * Math.cos(lat2) * Math.sin(deltalon / 2) * Math.sin(deltalon / 2);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 		double distance = EARTH_RADIUS*c;
 		
 		return distance;
 	}
 
 }
