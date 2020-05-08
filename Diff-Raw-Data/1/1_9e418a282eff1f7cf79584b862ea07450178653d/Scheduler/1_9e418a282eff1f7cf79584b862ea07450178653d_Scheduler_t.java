 package clock.sched;
 
 import clock.outsources.GDataHandler;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Scheduler implements LocationListener {
 	
 	private class DeviceLocation
 	{
 		public double longtitude;
 		public double latitude;
 		
 		public String getGoogleFormattedLoction()
 		{
 			return latitude + "," + longtitude;
 		}
 	}
 	
 	Context context;
 	DeviceLocation deviceLocation;
 	GDataHandler googleHandler;
 	
 	public Scheduler(Context context)
 	{
 		this.context = context;
 		this.deviceLocation = new DeviceLocation();
 		this.googleHandler = new GDataHandler();
 		
 		//$$ for now i test it via constructor, the call is the first thing in CalenderView:
 		doYourThing();
 	}
 	
 	private void doYourThing()
 	{
 		String currentLocation = this.getLocation();
 		int duration = googleHandler.getDuration(currentLocation, "");
 	}
 	
 	private String getLocation()
 	{
 		String formattedLocation = "";
 
 		LocationManager lm = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,1.0f, this);
 		
 		try 
 		{
 			//Start GPS service if needed
 			if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
 			{
 				//TODO: this is wrong because it force the user to start GPS, instead, we should ask the user to do so.
 				context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 			}
 			Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			deviceLocation.longtitude = l.getLongitude();
 			deviceLocation.latitude = l.getLatitude();
 			formattedLocation = deviceLocation.getGoogleFormattedLoction();
 		}
 		catch(Exception ex)
 		{
 			Log.d("Scheduler", "Error while trying to get last known location");
 		}
 	
 		return formattedLocation;
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
