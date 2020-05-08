 package com.hakkepakke.lufteapp;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GpsData extends Activity {
 
 	SQLiteDatabase db;
 	double latitude;
 	double longitude;
 	TextView editLocation;
 	LocationManager locationManager;
 	LocationListener locationListener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gps_data);
 		
 		 editLocation = (TextView) findViewById(R.id.kords);
 		 //editLocation.setVisibility(View.GONE);
 		
 		db = openOrCreateDatabase("gpsDataDB", MODE_PRIVATE,null);
 		db.execSQL("CREATE TABLE IF NOT EXISTS gpsDataa(longitude BIGINT, latitude BIGINT, isHome INTEGER, name STRING);");
 	    
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.gps_data, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    /*
 	     * Starts listening to updates when application resumes
 	     */
 	    if(!isNetworkAvailable())	{
 			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			    @Override
 			    public void onClick(DialogInterface dialog, int which) {
 			        switch (which){
 			        case DialogInterface.BUTTON_POSITIVE:
 			   	     Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
 				     startActivity(intent);
 			            break;
 
 			        case DialogInterface.BUTTON_NEGATIVE:
 			            //No button clicked
 			        	//DO nothing
 			            break;
 			        }
 			    }
 			};
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			
 			builder.setMessage(getString(R.string.set_internet)).setPositiveButton(getString(R.string.yes), dialogClickListener)
 			    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
 	    }
 		 locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 	    {
 			 locationListener = new MyLocationListener();  
 			 locationManager.requestLocationUpdates(  
 			 LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		 }
 	    else	{
 	    	enableGPS();
 	    }
 		 
 	}
 	
 	@Override 
 	protected void onPause() {
 		//Stops listening for GPS when application is paused
 		if(locationListener != null){	//It cant be removed, if it is not initialised
 			locationManager.removeUpdates(locationListener);
 		}
 		super.onPause();
 	}
 	
 	public void enableGPS()
 	{
 		/*
 		 * If the GPS is not enabled the functions will not work.
 		 * This method will ask the user if he/she wants to enable
 		 * GPS or not.  If the user wants to enable GPS the user is sent to
 		 * the settings menu for GPS.
 		 */
 		// Get Location Manager and check for GPS & Network location services
 		 if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
 		       !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        switch (which){
 				        case DialogInterface.BUTTON_POSITIVE:
 				   	     Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					     startActivity(intent);
 				            break;
 	
 				        case DialogInterface.BUTTON_NEGATIVE:
 				            //No button clicked
 				        	//DO nothing
 				            break;
 				        }
 				    }
 				};
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(getString(R.string.enable_gps)).setPositiveButton(getString(R.string.yes), dialogClickListener)
 				    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
 		 }
 	}
 	
 	public float compareHomeCurrent(View view)
 	{
 		/*
 		 * Compares the distance between current position 
 		 * and the home position and returns distance in meters.
 		 */
 		
 		//Tries to find out if home exists.
 		Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa WHERE isHome = 1;", null);
 		
 		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 		{
 			enableGPS();
 		}
 		else if(latitude == 0 && longitude == 0)	//GPS not yet found position
 		{
 	    	 Toast.makeText(getApplicationContext(), 
 	    	 getString(R.string.gps_position_wait),
 		     Toast.LENGTH_LONG).show();
 		}
 		else if(cursor.getCount() > 0)	//If home position exists.
 		{
 			if(cursor.moveToFirst())
 			{
 			double longitudeHome = Double.parseDouble((cursor.getString(0)));
 			double latitudeHome = Double.parseDouble(cursor.getString(1));
 			float[] results = new float[1];
 			Location.distanceBetween(latitudeHome, longitudeHome, latitude, longitude, results);
 			 editLocation.setText(getString(R.string.dist1)+" "+ (int) results[0] + " "+ getString(R.string.dist2));
 			 return results[0];
 			}
 		}
 		else
 		{
 	    	 Toast.makeText(getApplicationContext(), 
 	    	 getString(R.string.no_home),
 		     Toast.LENGTH_LONG).show();
 		}
 		return 0;
 
 	}
 	
 	public void setHome(View view) 
 	{
 		/*
 		 * Makes a new home for the user, but also checks if the
 		 * user already has a home.
 		 * 
 		 */
 		if(latitude != 0 && longitude != 0)	//If lat and lon already found.
 		{
 			Cursor cursor = db.rawQuery("SELECT * FROM gpsDataa WHERE isHome = 1;", null);
 			if(cursor.getCount() > 0)
 			{
 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        switch (which){
 				        case DialogInterface.BUTTON_POSITIVE:
 							db.execSQL("DELETE FROM gpsDataa WHERE isHome = 1;");
 							storeMapdataInDatabase(1);
 				            break;
 	
 				        case DialogInterface.BUTTON_NEGATIVE:
 				            //No button clicked
 				        	//DO nothing
 				            break;
 				        }
 				    }
 				};
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(getString(R.string.new_pos)).setPositiveButton(getString(R.string.yes), dialogClickListener)
 				    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
 			}
 			else
 			{
 				storeMapdataInDatabase(1);
 			}
 		}
 		else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
 		{
 			enableGPS();
 		}
 		else
 		{
 	    	 Toast.makeText(getApplicationContext(), 
 	    			 getString(R.string.find_pos),
 		     Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public void setCurrent(View view)
 	{
 		/*
 		 * Sets current GPS position, but ONLY
 		 * if the user is more than 100 metres from home.
 		 * This will ensure the user gets a "luftetur" or a walk.
 		 */
 		float distanceFromHome = compareHomeCurrent(view);
 		if(distanceFromHome >= 100) 
 		{
 			storeMapdataInDatabase(0);
 		}
 		else if(latitude == 0 && longitude == 0)	//GPS not yet found position
 		{
 	    	 Toast.makeText(getApplicationContext(), 
 	    	 getString(R.string.gps_position_wait),
 		     Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			float howMuchLeft = 100 - distanceFromHome;
 		       Toast.makeText(getApplicationContext(), 
 		    		   getString(R.string.dist_home1) + howMuchLeft + getString(R.string.dist_home2), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 		
 		void storeMapdataInDatabase(int isHome)
 		{
 			/*
 			 * Function will try to get GPS data and address 
 			 * and insert it into the database.
 			 * If GPS not enabled, the user will be asked to enable GPS before it works.
 			 */
 			if((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
 			{
 				if(latitude != 0 && longitude != 0)		//Has not acquired GPS data yet.
 				{
 					try {	
 						String address = getAddressGoogleQuery();
 						db.execSQL("INSERT INTO gpsDataa VALUES('"+longitude + "','" + latitude +
 								"','" + isHome + "','" + address + "');");
 					}
 					catch(NullPointerException e) {
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 			    	 Toast.makeText(getApplicationContext(), 
 			    			 getString(R.string.find_pos),
 				     Toast.LENGTH_LONG).show();
 				}
 			}
 			
 			else	{		//If GPS has been disabled while using the app.
 				enableGPS();
 			}
 		}
 		
 		
 		private String getAddressGoogleQuery() {
 			/*
 			 * Might have to restart to get it to work.
 			 * Functions gets the address, city and country
 			 * with the use of latitude and longitude. 
 			 * Also needs internet to work.
 			 */
 			
 			/*If internet not avavible it will simply return unknown
 			*Later when connected to the internet it will 
 			*Get the address.
 			*/
 			if(!isNetworkAvailable()){
 				return "unknown";
 			}
 			
 			Geocoder geocoder;
 			List<Address> addresses = null;
 			geocoder = new Geocoder(this, Locale.getDefault());
 			try {
 				addresses = geocoder.getFromLocation(latitude, longitude, 1);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			String address = addresses.get(0).getAddressLine(0);
 			String city = addresses.get(0).getAddressLine(1);
 			String country = addresses.get(0).getAddressLine(2);
 
 			return (city + "," + address + "," +country);
 		}
 	 
 		/*
 		 * Checks whether or not we got internet!
 		 */
 		private boolean isNetworkAvailable() {
 		    ConnectivityManager connectivityManager 
 		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 		}
 		
 		/*
 		 * Listenerclass to get latitude and longitude
 		 */
 		
 		private class MyLocationListener implements LocationListener {
 
 		    @Override
 		    public void onLocationChanged(Location loc) {
 		        longitude = loc.getLongitude();
 		        latitude = loc.getLatitude();
 		    }
 		    
 		    /*
 		     * No need for these extra methods.
 		     */
 		    public void onProviderDisabled(String provider) {}
 
 		    public void onProviderEnabled(String provider) {}
 
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 		}
 }
 
