 package Impek.Gen;
 
 import Impek.Gen.ImpekActivity;
 import android.location.*;
 import android.os.Bundle;
 import android.content.*;
 
 public class GeoLocation {
 
 	static LocationManager locationManager;
 	static Location location;
 	static LocListener listener;
 	
 	public static void setup_GeoLocation()
 	{
 		location = null;
 		locationManager = (LocationManager) ImpekActivity.curr.getSystemService(Context.LOCATION_SERVICE);
 		listener = new GeoLocation.LocListener();
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 30, listener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 30, listener);
 	}
 	
	public static void single_update()
 	{
 		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null);
 		//locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
	}
 	
 	static double getLattitude() throws NoLocationError
 	{
 		Location ret = null;
 		
 		if(location != null)
 			ret = location;
 		
 		else
 		{
 			if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null || locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			{
 				if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 					ret = null;
 				else if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null)
 					ret = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 				else
 					locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			}
 			else if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime() < locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime())
 				ret = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			else
 				ret = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 		
 		if(ret == null)
 			throw new NoLocationError();
 		
 		return ret.getLatitude();
 	}
 	
 	static double getLongitude() throws NoLocationError
 	{
 		Location ret = null;
 		
 		if(location != null)
 			ret = location;
 		
 		else
 		{
 			if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null || locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			{
 				if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 					ret = null;
 				else if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null)
 					ret = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 				else
 					locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			}
 			else if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime() < locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime())
 				ret = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			else
 				ret = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 		
 		if(ret == null)
 				throw new NoLocationError();
 		
 		return ret.getLongitude();
 	}
 	
 	static double getAccuracy() throws NoLocationError
 	{
 		if(location == null)
 			throw new NoLocationError();
 		return location.getAccuracy();
 	}
 	
 	static Location getLocation() throws NoLocationError
 	{
 		if(location == null)
 			throw new NoLocationError();
 		return location;
 	}
 	
 	public static class LocListener implements LocationListener
 	{
 
 		public void onLocationChanged(Location loc) {
 			((ImpekActivity) ImpekActivity.curr).update(); 
 			location = loc;
 		}
 
 		public void onProviderDisabled(String arg0) {
 			
 		}
 
 		public void onProviderEnabled(String arg0) {
 			
 		}
 
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 			
 		}
 		
 	}
 	
 	static class NoLocationError extends Exception
 	{
 		private static final long serialVersionUID = 1L;
 		
 	}
 	
 }
