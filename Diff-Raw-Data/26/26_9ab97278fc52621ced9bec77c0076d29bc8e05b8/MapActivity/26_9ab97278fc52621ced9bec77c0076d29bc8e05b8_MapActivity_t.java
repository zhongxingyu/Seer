 package ru.allgage.geofriend;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MapActivity extends FragmentActivity {
 	private GoogleMap mMap;
 	LocationManager locationManager;
 	LocationListener listener;
 	String status = "Test";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		 
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 		mMap = mapFragment.getMap();
 
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		
 		if(mMap != null) {
 			//new UpdateTask(mMap).execute();
 		}
 		
 		listener = new LocationListener() {
 
 		    @Override
 		    public void onLocationChanged(Location location) {
 		        new SendTask().execute(location.getLatitude(), location.getLongitude(), status);
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
 		};
 		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 		        600000,          // 600-second interval.
 		        0,             // 10 meters.
		        listener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 0, listener);
 		toCallAsynchronous();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 	    return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.item1:
 	        Intent intent = new Intent(this, StatusActivity.class);
 	        startActivityForResult(intent, 1);
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (data == null) return;
 	    status = data.getStringExtra("status");
	    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    if(loc != null)
	    	new SendTask().execute(loc.getLatitude(), loc.getLongitude(), status);
 	}
 	
 	public void toCallAsynchronous() {
 	    final Handler handler = new Handler();
 	    Timer timer = new Timer();
 	    TimerTask doAsynchronousTask = new TimerTask() {       
 	        @Override
 	        public void run() {
 	            handler.post(new Runnable() {
 	                public void run() {
 	                	
 	                	new UpdateTask(mMap).execute();
 
 	                }
 	            });
 	        }
 	    };
 	    timer.schedule(doAsynchronousTask, 0, 20000); //execute in every 10000 ms
 	}
 
 }
 
