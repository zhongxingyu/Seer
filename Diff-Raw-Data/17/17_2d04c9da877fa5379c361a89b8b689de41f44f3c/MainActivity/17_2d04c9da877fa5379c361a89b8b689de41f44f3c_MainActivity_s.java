 package com.gpslistener;
 
 import com.gpslistener.fragments.ShowDateFragment.OnDateSelectedListener;
 import com.gpslistener.helpers.Constants;
 import com.gpslistener.helpers.DatabaseTask;
 import com.gpslistener.helpers.GPSListenerDbHelper;
 import com.gpslistener.helpers.HttpFetchLocationTask;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 
 public class MainActivity extends Activity implements AsyncResponse, OnDateSelectedListener {
 	
 	private LocationManager manager;
 	private gpslistenerLocationListener listener = new gpslistenerLocationListener();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		manager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		
 		//To register the listener to be call backed when the Async Task returns data
 		HttpFetchLocationTask.delegate = this;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onResume() {
 	    super.onResume();  // Always call the superclass method first
 	    
 	    if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 	    	manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 50, listener);
 	    }
 	    else if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 	    	manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, listener);
 	    }
 	}
 	
 	@Override
 	public void onPause() {
 	    super.onPause();  // Always call the superclass method first
 	    manager.removeUpdates(listener);
 	}
 	
 	@Override
 	public void onTaskCompleted(Object value) 
 	{
 		GPSListenerDbHelper dbHelper = new GPSListenerDbHelper(getApplicationContext());
 		Object params[] = {value, dbHelper};
 		new DatabaseTask().execute(params);
 	}
 	
 	public void onDateSelected(String selectedDate)
 	{
 		Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
 		intent.putExtra("date", selectedDate);
 		startActivity(intent);
 	}
 	
 	public class gpslistenerLocationListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			double lat = location.getLatitude();
 			double lon = location.getLongitude();
 			
 			/*Bundle locationDataBundle = new Bundle();
 			locationDataBundle.putDouble("com.gpslistener.locationData.latitude", lat);
 			locationDataBundle.putDouble("com.gpslistener.locationData.longtitude", lon);
 			Intent intent = new Intent(getApplicationContext(), FetchLocationService.class);
 			intent.putExtra("com.gpslistener.locationData", locationDataBundle);
 			startService(intent);*/
			new HttpFetchLocationTask().execute(Constants.getNEARBY_SEARCH_URI(lat, lon, "false", 100));
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 
 		}
 
 	}
 
 }
