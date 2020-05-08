 package com.example.airportstatus;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.example.airportstatus.models.Favorite;
 
 public class AirportStatusActivity extends Activity implements OnNavigationListener {
 
 	Button btnGo;
 	AutoCompleteTextView tvAirportCode;
 	LocationManager locationManager;
 	LocationListener locationListener;
 	SharedPreferences locationPrefs;
	FavoritesAdapter favoritesListAdapter;
 	
	public static final String AIRPORT_CODE = "airport_code";
 	public static final String PREFS_NAME = "AirportStatusPrefs";
 	public static final String PREFS_LATITUDE = "LAT";
 	public static final String PREFS_LONGITUDE= "LON";
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_airport_status);
         setupLocationStorage();
         setupLocationListener();
         
         setupButton();
         setupTextView();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.airport_status, menu);
         
         // Set up the action bar to show a dropdown list.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         
         ArrayList<String> dropdownValues = Favorite.getAllCodes();
         dropdownValues.add(0, getResources().getString(R.string.txtFavoritesPlaceholder));
         
         // Specify a SpinnerAdapter to populate the dropdown list.
        favoritesListAdapter = new FavoritesAdapter(actionBar.getThemedContext(), dropdownValues);
        favoritesListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(favoritesListAdapter, this);
         
         return true;
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	locationManager.removeUpdates(locationListener);
     	
     }
     
     public void onClick(View v) {
     	String textEntered = tvAirportCode.getText().toString();
     	String code = AirportCodes.IATA_CODES.get(textEntered);
     	if (code != null) {
 	    	Toast.makeText(this, "Searching for " + code + "...", Toast.LENGTH_SHORT).show();
 	    	Intent i = new Intent(this, QueryActivity.class);
	    	i.putExtra(AIRPORT_CODE, code);
 	    	startActivity(i);
     	} else {
     		Toast.makeText(this,  "Could not find airport code " + textEntered, Toast.LENGTH_SHORT).show();
     	}
     }
 
     @SuppressLint("InlinedApi")
     private void setupButton() {
     	btnGo = (Button) findViewById(R.id.btnGo);
         btnGo.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
     }
 
     private void setupTextView() {
     	ArrayList<String> codes = new ArrayList<String> (AirportCodes.IATA_CODES.keySet());
     	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_dropdown_item_1line, codes);
     	tvAirportCode = (AutoCompleteTextView) findViewById(R.id.tvAirportCode);
     	tvAirportCode.setAdapter(adapter);
     }
     
     private void setupLocationStorage() {
         locationPrefs = getSharedPreferences(PREFS_NAME, 0);    	
     }
     
     private void setupLocationListener() {
     	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     	locationListener = new LocationListener() {
     		@Override
     		public void onLocationChanged(Location location) {
     			SharedPreferences locationPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
     			SharedPreferences.Editor editor = locationPrefs.edit();
     			editor.putFloat(PREFS_LATITUDE, (float) location.getLatitude());
     			editor.putFloat(PREFS_LONGITUDE, (float) location.getLongitude());
     			editor.commit();
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
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				// TODO Auto-generated method stub
 			}
     	};
     	
     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
     }
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			String airportCode = favoritesListAdapter.getItem(itemPosition);
		if (airportCode.length() == 3) {
			Intent i = new Intent(this, QueryActivity.class);
			i.putExtra(AIRPORT_CODE, airportCode);
			startActivity(i);
			return true;
		}
 		return false;
 	}
     
     
 }
