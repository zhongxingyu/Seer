 package edu.calpoly.ai.skynest;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.Gravity;
 import android.widget.Toast;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 
 public class MapsActivity extends SherlockFragmentActivity implements LocationListener {
 
 	/** The interactive Google Map fragment. */
 	private GoogleMap m_vwMap;
 	
 	/** The Location Manager for the map. Used to obtain location status, etc. */
 	private LocationManager m_locManager;
 	
 	/** Request codes for starting new Activities. */
 	private static final int ENABLE_GPS_REQUEST_CODE = 1;
 	
 	/** Markers for the home and work locations */
 	private double home_lat, home_lng;
 
 	/** The radius of a Circle drawn on the map, in meters. */
 	private static final int CIRCLE_RADIUS = 1;
 	
 	/** Last known lat and lng */
 	private double lat, lng;
 	
 	/** SharedPreferences keys */
 	public static final String HOME_LAT = "HOME_LAT";
 	public static final String HOME_LNG = "HOME_LNG";
 	
 	/** Set Home Prompt */
 	public static final String s_setHome = "Please set your home location";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_maps);
         m_vwMap = ((SupportMapFragment)
         		getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
         initLocationData();
         if(m_vwMap != null){
         	m_vwMap.getUiSettings().setRotateGesturesEnabled(true);
         	m_vwMap.getUiSettings().setCompassEnabled(true);
         	m_vwMap.setMyLocationEnabled(true);
         	m_vwMap.getUiSettings().setMyLocationButtonEnabled(true);
         	m_vwMap.getUiSettings().setZoomControlsEnabled(true);
         }
         checkHomeLocation();
         retrieveHomeLocation();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = this.getSupportMenuInflater();
 		inflater.inflate(R.menu.maps, menu);
 		return true;
 	}
 	
 	/**
      * Initializes all Location-related data.
      */
     private void initLocationData() {
     	m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     }
 
     @Override
 	public void onLocationChanged(Location arg0) {
 		lat = arg0.getLatitude();
 		lng = arg0.getLongitude();
 		LatLng l = new LatLng(lat, lng);
 		m_vwMap.animateCamera(CameraUpdateFactory.newLatLng(l));
 		m_vwMap.addCircle(new CircleOptions()
 			    .center(l)
 			    .radius(CIRCLE_RADIUS) // In meters
 				.fillColor(Color.CYAN)
 				.strokeColor(Color.BLUE));
 	}
     
     public void saveHomeLocation() {
     	SharedPreferences sp = this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);
 		Editor e = sp.edit();
 		e.putLong("HOME_LAT", (long) home_lat);
 		e.putLong("HOME_LNG", (long) home_lng);
 		e.commit();
     }
     
     public void retrieveHomeLocation() {
     	if (hasHomeLocation()) {
     		SharedPreferences sp = this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);
 			home_lat = sp.getLong(HOME_LAT, (long) 0);
 			home_lng = sp.getLong(HOME_LNG, (long) 0);
 			m_vwMap.addMarker(new MarkerOptions()
 				.position(new LatLng(home_lat, home_lng))
 				.title("Home"));
     	}
     }
 	
 	public void setHomeLocation(Location location) {
 		if (location != null){
 			home_lat = location.getLatitude();
 			home_lng = location.getLongitude();
 		}
 		else {
 			// set to most recent location
 			home_lat = lat;
 			home_lng = lng;
 		}
 		m_vwMap.addMarker(new MarkerOptions()
 		.position(new LatLng(home_lat, home_lng))
 		.title("Home"));
 		saveHomeLocation();
 	}
 	
 	private boolean hasHomeLocation(){
 		SharedPreferences sp = this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);
 		return (sp != null &&
 			sp.contains(MapsActivity.HOME_LAT) &&
 			sp.contains(MapsActivity.HOME_LNG));
 	}
 	
 	private void checkHomeLocation(){
 		if (!hasHomeLocation()) {
			Toast toast = new Toast(getApplicationContext());
 			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setText(s_setHome);
 			toast.show();
 		}
 		return;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		Intent i;
 		switch(item.getItemId()){
 			case R.id.menu_enableGPS:
 				i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivityForResult(i, MapsActivity.ENABLE_GPS_REQUEST_CODE);
 				break;
 			case R.id.menu_disableGPS:
 				i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivityForResult(i, MapsActivity.ENABLE_GPS_REQUEST_CODE);
 				break;
 			case R.id.set_home:
 				setHomeLocation(m_locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu){
 		boolean enabled = m_locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		if (enabled) {
 			menu.findItem(R.id.menu_enableGPS).setVisible(false);
 			menu.findItem(R.id.menu_disableGPS).setVisible(true);
 		}
 		else {
 			menu.findItem(R.id.menu_enableGPS).setVisible(true);
 			menu.findItem(R.id.menu_disableGPS).setVisible(false);
 		}
 		return true;
 	}
 	
 	@Override
 	public void onActivityResult(int req, int res, Intent i){
 		super.onActivityResult(req, res, i);
 		if(req == ENABLE_GPS_REQUEST_CODE)
 			supportInvalidateOptionsMenu();
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
 
	public static void addProximityAlert(long homeLatitude, long homeLongitude,
			long pointRadius, int i, PendingIntent proximityIntent) {
		/*if (m_locManager != null){
			m_locManager.addProximityAlert(homeLatitude, homeLongitude,
				pointRadius, i, proximityIntent);
		}*/
		
	}

 }
