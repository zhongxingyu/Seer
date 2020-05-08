 package de.frizzware.pacrun;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 
 public class LocationService implements LocationListener {
 	private Context mContext;
 	private LocationManager mLocationMgr;
 	private  ArrayList<Location> mLocations = new ArrayList<Location>();
 	private LocationHandler mHandler;
 	
 	/**
 	 * Overall distance
 	 */
 	private double distance = 0;
 	private Date startDate;
 	private boolean running = false;
 	
     // flag for network status
     boolean isNetworkEnabled = false;
     // flag for GPS status
     boolean isGPSEnabled = false;
 	
 	 // The minimum distance to change Updates in meters
     private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
     // The minimum time between updates in milliseconds
     private static final long MIN_TIME_BW_UPDATES = 1000 * 30; // 1 minute
 	
 	public LocationService(Context ctx, LocationHandler handler) {
 		mContext = ctx;
		
 		mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
 		
 	}
 	
 	public void onLocationChanged(Location location) {
 		mLocations.add(location);
 		if (mLocations.size() > 2) {
 			Location last = mLocations.get(mLocations.size() - 2);
 			distance += last.distanceTo(location);
 		}
 		if (mHandler != null)
 			mHandler.onLocationChanged(location);
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
 	
 	public void start() {
         // getting GPS status
 		isGPSEnabled = mLocationMgr
                 .isProviderEnabled(LocationManager.GPS_PROVIDER);
         // getting network status
         isNetworkEnabled = mLocationMgr
                 .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 		
 		if (isGPSEnabled) {
 			running = true;
 			startDate = new Date();
 			mLocationMgr.requestLocationUpdates(
                 LocationManager.GPS_PROVIDER,
                 MIN_TIME_BW_UPDATES,
                 MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
 		} else {
 			showSettingsAlert();
 		}
 	}
 	
 	public void stop() {
 		running = false;
 		mLocationMgr.removeUpdates(this);
 	}
 	
 	public float getSpeed() {
 		if (!mLocations.isEmpty()) {
 			Location loc = mLocations.get(mLocations.size()-1);
 			if(loc.hasSpeed())
 				return loc.getSpeed();
 			else if (mLocations.size() > 2) {
 				Location last1 = mLocations.get(mLocations.size() - 1);
 				Location last2 = mLocations.get(mLocations.size() - 2);
 				return last1.distanceTo(last2)/MIN_TIME_BW_UPDATES;
 			}
 		}
 		return -1;
 	}
 	
 	public Location getCurrentLocation() {
 		if (!mLocations.isEmpty())
 			return mLocations.get(mLocations.size()-1);
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public double getDistance() {
 		return distance;
 	}
 	
 	public long getDuration() {
 		return new Date().getTime() - startDate.getTime();
 	}
 	
 	public boolean getRunning() {
 		return running;
 	}
 	
 	public double getAverageSpeed() {
 		return getDistance()/getDuration();
 	}
 	
     /**
      * Function to show settings alert dialog
      * On pressing Settings button will lauch Settings Options
      * */
     private void showSettingsAlert(){
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
  
         // Setting Dialog Title
         alertDialog.setTitle("GPS is settings");
  
         // Setting Dialog Message
         alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
         // On pressing Settings button
         alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,int which) {
                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 mContext.startActivity(intent);
             }
         });
  
         // on pressing cancel button
         alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             dialog.cancel();
             }
         });
  
         // Showing Alert Message
         alertDialog.show();
     }
     
     interface LocationHandler {
     	public void onLocationChanged(Location location);
     }
 }
