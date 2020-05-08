 package com.advsofteng.app1;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.*;
 import android.os.Bundle;
 import android.widget.TextView;
 
 /**
  * 
  * @author alandonohoe
  * 
  * This App displays the current time and the device's current GPS co-ordinates.
  * if GPS data is unavailable, it displays "GPS not available".
  */
public class AdvSoftEngApp1Activity extends Activity {
 	
 	// class members
 	private LocationManager manager;
 	private LocationListener listener;
 	private String strGPS;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         ////////////////////////////////////////////////////
         // Digital Clock has been handled by a self-contained 
         // widget in the layout/main.xml file
         
         /////////////////////////
         // Below is the GPS code
         
         // get handle to the GPS TextView
         final TextView tvGPS = (TextView) findViewById(R.id.textViewGPS);
         
         // check to see if view found
         //  if(tvGPS == null)
         // 		finish(); // ??? - not too sure what to do here... quit activity??
        
         ////////////////////////////////////////////////////////////////////////
         // set up LocationManager services
         manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         
         // if(manager == null)
         // 	finish(); // ??? - not too sure what to do here... quit activity??
         ////////////////////////////////////////////////////////////////////////
         
         ////////////////////////////////////////////////////////////////////////
         // get initial location before any updates, from the last known location
         Location loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         
         //TODO- TAKE THIS OUT....
         setGPSText(loc, tvGPS);
         
         if(loc != null)
         {
         	// print initial location data to TextView
         	//TODO - TAKE THIS OUT
         	setGPSText(loc, tvGPS);
         }
         else
         	tvGPS.setText(getString(R.string.no_gps_error));
       
 		
 		//
 		/////////////////////////////////////////////////////////////////////////
 		
 		// create the listener object to receive GPS updates...
 		listener = new LocationListener() {
 			
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				if((LocationProvider.OUT_OF_SERVICE == status)||(LocationProvider.TEMPORARILY_UNAVAILABLE == status)) 
 				{	// if there's no service, print error string found in resources...
 					tvGPS.setText(getString(R.string.no_gps_error));
 				}
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				// update GPS TextView's data
 				//TODO - TAKE THIS OUT...
 				setGPSText(location, tvGPS);
 				
 			}
 			////////////////////////////////////////////////////
 			// - these overriden functions, as yet undefined...
 			@Override
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 				// put code here to deal with GPS being down...
 			}
 			
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		}; // end of ListenerLocation constructor....
             
 		// final location initialization. 
         manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            
         }
     
     // updates TextView tv text with location's longitude and latitude data.
     // 
     
     private void setGPSText(Location location, TextView tv)
     {
     	// check for valid TextView and Location objects....
     	if((null == tv) || (null == location))
     		return;
     	
     	// else.. continue...
 		strGPS = "Longitude = " + location.getLongitude() + "\n";
 		strGPS += "Latitude = " + location.getLatitude();
 		
 		// set the GPSTextView
 		tv.setText(strGPS);
     }
     
     
 }
 
 
