 package com.riverspart.gps;
 
import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 import com.riverspart.gcm.GCMSessionGMTB;
 
 public class GPSLocationListener implements LocationListener {
     @Override
     public void onLocationChanged(Location location) {
         if (location != null) {
 //	        Log.d("LOCATION CHANGED", location.getLatitude() + "");
 //	        Log.d("LOCATION CHANGED", location.getLongitude() + "");
 	        
 	        if(GPSSessionGMTB.currentGPSConfiguration!=null && GPSSessionGMTB.currentGPSConfiguration.getAllowShareLocation()) {
 	        	Location lastLocation = GPSSessionGMTB.lastUpdateLocation;
 	        	GPSConfiguration gpsConfig = GPSSessionGMTB.currentGPSConfiguration;
 	    		if(lastLocation!=null) {
 	    			if(
 	    					(
 	    							gpsConfig.getUpdateType() == GPSConfigurationGMTB.UPDATE_TYPE_DISTANCE
 	    							&& location.distanceTo(lastLocation) < gpsConfig.getDistanceUpdate()
 							) || (
 									gpsConfig.getUpdateType() == GPSConfigurationGMTB.UPDATE_TYPE_TIME
 									// Convert from second to milisecond by multiplied by 1000
 									&& (location.getTime() - lastLocation.getTime() < 1000 * gpsConfig.getTimeUpdate() )
 							)
 					) {
 	    				return;
 	    			}
 	    		}
 	    		sendToServer(location);
 	        }
         }
     }
 
     private void sendToServer(Location location) {
     	
     	new GPSUploadLocationTask().execute(new Object[]{GCMSessionGMTB.currentGCMConfiguration, GPSSessionGMTB.currentGPSConfiguration, location});
     }
     
 	@Override
 	public void onProviderDisabled(String arg0) {
 		
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
