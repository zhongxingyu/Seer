 package com.advsofteng.app1;
 
 import android.app.Activity;
 import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.TextView;
//import android.location.*;
 
 /**
  * 
  * @author alandonohoe
  * This App displays the current time and the devices current GPS co-ordinates.
  * if GPS data is unavailable, it displays "GPS not available".
  */
 public class AdvSoftEngApp1Activity extends Activity {
 	
 	// class members
 	private LocationManager manager;
 	private LocationListener listener;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Digital Clock has been handled by a self-contained widget in the 
         // layout/main.xml file
         
         // Below is the GPS code
         
         // get handle to the GPS TextView
         final TextView tvGPS = (TextView) findViewById(R.id.textViewGPS);
         
        // tvGPS.setText("HELLO HELLELELKELKJDLKJDKLJKLD");
         
         // set up LocationManager services
         manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         
         
 		listener = new LocationListener() {
 			
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				// TODO Auto-generated method stub
				
 			}
 			
 			@Override
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 				// put code here to deal with GPS being down...
 			}
 			
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onLocationChanged(Location location) {
 				// TODO Auto-generated method stub
 				String strGPS = new String();
 				strGPS = "Longitude = " + location.getLongitude() + "\n";
 				strGPS += "Latitude = " + location.getLatitude();
 				
				//Double dbl = location.getLongitude();
				//strGPS.
				//tvGPS.setText():
 				
 				
 			}
 		};
         
         
         
         manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
         
        // gps = new GPSObject(tvGPS, true, true);
         
         
         
         
         
         
     }
 }
