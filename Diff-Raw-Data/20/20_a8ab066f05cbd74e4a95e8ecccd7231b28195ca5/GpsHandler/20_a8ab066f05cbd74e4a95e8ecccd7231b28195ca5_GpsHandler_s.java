 /*******************************************************************************
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @contributor(s): Freerider Team 2 (Group 3, IT2901 Spring 2013, NTNU)
  * @version: 2.0
  * 
  * Copyright 2013 Freerider Team 2
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package no.ntnu.idi.socialhitchhiking.utility;
 
 import java.util.List;
 
 import no.ntnu.idi.socialhitchhiking.findDriver.FindDriver;
 import no.ntnu.idi.socialhitchhiking.map.MapActivityCreateOrEditRoute;
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 /**
  * Starts a GPS listener and returns current location to FindDriver class
  * @author Thomas
  * @param <T>
  *
  */
 public class GpsHandler<T> {
 	private LocationManager locManager;
 	private LocationListener locListener;
 	Activity activity;
 	public GpsHandler(Activity parent) {
 		locManager = (LocationManager)parent.getSystemService(Context.LOCATION_SERVICE);
 		locListener = new GpsListener();
 		activity = parent;
 	}
 	public void findLocation() {
 		List<String> providers = locManager.getProviders(true);
 		Location l = null;
         
        for (int i=providers.size()-1; i>=0; i--) {
                 l = locManager.getLastKnownLocation(providers.get(i));
                if (l != null) break;
         }
         if(l != null){
         	if(activity instanceof FindDriver) {
 				((FindDriver)activity).gotLocation(l);
 			}
 			else
 			{
 				((MapActivityCreateOrEditRoute)activity).gotLocation(l);
 			}
 			
 			try {
 				this.finalize();
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
         else
         {
         	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
         }
 		
 	}
 	public void abortGPS() {
 		locManager.removeUpdates(locListener);
 	}
 	public boolean gpsEnabled(){
 		if (locManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 class GpsListener implements LocationListener {
 	@Override
 	public void onLocationChanged(Location location) {
 		if(location != null) {
 			locManager.removeUpdates(locListener);
 			Log.e("Lat",Double.toString(location.getLatitude()));
 			if(activity instanceof FindDriver) {
 				((FindDriver)activity).gotLocation(location);
 			}
 			else
 			{
 				((MapActivityCreateOrEditRoute)activity).gotLocation(location);
 			}
 			
 			try {
 				this.finalize();
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
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
 }
