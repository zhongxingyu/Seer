 package com.Good.Geo;
 
 import java.util.concurrent.locks.*;
 
 import android.content.Context;
 import android.location.*;
 import android.os.Bundle;
 
 import com.Good.Geo.Geotag;
 
 public class GeoLocation {
 
 	static private Geotag tag;
 	static private LocationManager locationManager;
 	static private LocListener listener;
 	static Lock lock;
 	
 	public static void setup_GeoLocation()
 	{
 		tag = null;
 		listener = new LocListener();
 		locationManager = (LocationManager) com.Good.MainActivity.curr.getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, listener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
 	}
 
 	
	public static Geotag getGeotag() throws NoClue
 	{
 		lock.lock();
 		Geotag ret = tag;
 		lock.unlock();
		
		if(ret == null)
			throw new NoClue();
		
 		return ret;
 	}
 	
 	public static class LocListener implements LocationListener
 	{
 
 		public void onLocationChanged(Location location) {
 			lock.lock();
 			tag.setLattitude(location.getLatitude());
 			tag.setLongitude(location.getLongitude());
 			tag.setAltitude(location.getAltitude());
 			lock.unlock();
 			return;
 		}
 
 		public void onProviderDisabled(String arg0) {
 			return;
 		}
 
 		public void onProviderEnabled(String arg0) {
 			return;
 		}
 
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 			return;
 		}
 		
 	}
 	
	static class NoClue extends Exception
	{
		private static final long serialVersionUID = 773107550741108174L;
	}
 }
