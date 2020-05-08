 package com.mani.emergency;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class GPSActivity extends Activity implements LocationListener{
 	private LocationManager locationManager;
 	private String provider;
 	private Location location;
 	DatabaseHandler db = new DatabaseHandler(this);
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    // Get the location manager
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // Define the criteria how to select the locatioin provider -> use
 	    // default
 	    Criteria criteria = new Criteria();
 	    provider = locationManager.getBestProvider(criteria, false);
 	    location = locationManager.getLastKnownLocation(provider);
 
 	    // Initialize the location fields
 	    if (location != null) {
 	      System.out.println("Provider " + provider + " has been selected.");
 	      onLocationChanged(location);
 	    } else {
 	    	System.out.println("Location not available");
 	    }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		locationManager.requestLocationUpdates(provider, 400, 1, this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		List<Contact> contacts = db.getAllContacts();       
 	    for (Contact cn : contacts) {
 			PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, ShakerService.class), 0);                
 		    SmsManager sms = SmsManager.getDefault();
		    sms.sendTextMessage(cn.getPhoneNumber(), null, getString(R.string.message)+location.getLatitude()+","+location.getLongitude(), pi, null);
 	    }
 		finish();
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		Toast.makeText(this, "Disabled provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		Toast.makeText(this, "Enabled new provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 
 	}
 }
