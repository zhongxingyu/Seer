 package info.ss12.audioalertsystem.alert;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 public class GPSAlert extends AbstractAlert
 {
 	Context context;
 	private LocationListener mlocListener;
 	private LocationManager mlocManager;
 	public static Double CUR_LATITUDE = null;
 	public static Double CUR_LONGITUDE = null;
 	public GPSAlert(Context context)
 	{
 		this.context = context;
 		super.setAlertType("GPS Alert");
 		super.sendAlert();
 		startGps();
 	}
 	
 	public void startGps()
 	{
 		mlocManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
 		mlocListener = new MyLocationListeners();
 		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mlocListener);
 		mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, mlocListener);
 	}
 	
 	public void stopGps()
 	{
 		mlocManager.removeUpdates(mlocListener);
 	}
 	
 	public class MyLocationListeners implements LocationListener
 	{
 
 		@Override
 		public void onLocationChanged(Location loc)
 		{
 			Log.d("GPS ALERT", "LocationChanged");
 			CUR_LATITUDE = new Double(loc.getLatitude());
 			CUR_LONGITUDE = new Double(loc.getLongitude());
			Toast.makeText(context, CUR_LATITUDE + " "+ CUR_LONGITUDE, Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		public void onProviderDisabled(String provider)
 		{
 			Toast.makeText(context,"Gps Disabled", Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		public void onProviderEnabled(String provider)
 		{
 			Toast.makeText(context,"Gps Enabled", Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras)
 		{
 			
 		}
 		
 	}
 }
