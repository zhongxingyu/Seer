 package com.dotcom.nextup.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.dotcom.nextup.R;
 import com.dotcom.nextup.categorymodels.Category;
 import com.dotcom.nextup.categorymodels.CheckIn;
 import com.dotcom.nextup.categorymodels.CheckInManager;
 import com.dotcom.nextup.classes.FoursquareLocationManager;
 import com.dotcom.nextup.classes.TokenManager;
 import com.dotcom.nextup.classes.Venue;
 import com.dotcom.nextup.oauth.AndroidOAuth;
 import com.google.android.maps.GeoPoint;
 
 public class Intermediate extends Activity {
 	ArrayList<Venue> nearby_locations = null;
 	Venue nearest_location = null;
 	ArrayAdapter<CharSequence> adapter;
 	ArrayList<CharSequence> spinner_locations = null;
 
 	ArrayList<Category> categories = new ArrayList<Category>();
 
 	private AndroidOAuth oa;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	public GeoPoint currentLocation = null;
 	public GeoPoint lastLocation;
 	public String currentLocationName;
 	public String lastLocationName;
 	private String token;
 	private String code;
 	private static Boolean receivedLocationUpdate = false;
 	private static Boolean receivedLastLocationUpdate = false;
 	private static Boolean checkinsUpdated = false;
 	private static Boolean locationRegistered = false;
 	private static Boolean codeStored = false;
 	private CheckInManager checkInManager;
 	private ArrayList<CheckIn> checkIns;
 	private SharedPreferences pref;
 	private Context context;
 	private int currentSelectedVenue = -1;
 	
 	private Runnable fourSquare;
 	private Runnable locationListening;
 	private Runnable fourSquareAndLocation;
 	private boolean foursquare_thread_done = false;
 	private boolean location_thread_done = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.intermediate2);
 		context = this;
 		nearby_locations = new ArrayList<Venue>();
 		oa = new AndroidOAuth(this);
 		checkInManager = new CheckInManager();
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		fourSquare = new Runnable() {
 			@Override
 			public void run() {
 				connectWithFourSquare();
 			}
 		};
 		
 		locationListening = new Runnable() {
 			@Override
 			public void run() {
 				Looper.prepare();
 				findCurrentLocation();
 			}
 		};
 		
 		fourSquareAndLocation = new Runnable() {
 			@Override
 			public void run() {
 				locationUpdateWithFoursquare();
 			}
 		};
 
 		// does foursquare stuff, can work independently
 		Thread foursquare_thread = new Thread(null, fourSquare, "Foursquare thread");
 		foursquare_thread.start();
 		
 		// does location stuff, can work independently
 		Thread location_thread = new Thread(null, locationListening, "Location thread");
 		location_thread.start();
 		
 		// can only do stuff after foursquare and location stuff finished
 		Thread foursquare_and_location_thread = new Thread(null, fourSquareAndLocation, "Location + Foursquare thread");
 		foursquare_and_location_thread.start();
 	}
 
 /*
 	public void onResume() {
 		 super.onResume();
 	     locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	     
 	     if (this.checkIns == null || this.checkIns.size() == 0)
 			try {
 				initializeCheckIns();
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
 	     if (currentLocation != null && currentLocationName == null && !receivedLastLocationUpdate)
 			try {
 				updateLocationInfo();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			updateSpinner();
 	}
 */
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(locationListener);
 	}
 	
 	/* ---------------- UI CODE BELOW -------------------*/
 
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			Log.v("Intermediate", "running returnRes");
 			TextView textview = (TextView) findViewById(R.id.Intermediate2Text);
 			textview.setText("Nearby locations:");
 			updateSpinner();
 		}
 	};
 	
 	@SuppressWarnings("unchecked")
 	public void updateSpinner() {
 		Spinner spinner = (Spinner)findViewById(R.id.Intermediate2Spinner);
 		spinner_locations = new ArrayList<CharSequence>();
 		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinner_locations);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		spinner.setOnItemSelectedListener(spinnerListener);
 		
 		if (nearby_locations != null && nearby_locations.size() > 0) {
 			adapter.notifyDataSetChanged();
 			for (int i = 0; i < nearby_locations.size(); i++)
 				adapter.add(nearby_locations.get(i).getName());
 			
 			Button next = (Button)findViewById(R.id.Intermediate2Button);
 			next.setVisibility(View.VISIBLE);
 			next.setClickable(true);
 		}
 		if (nearby_locations == null || nearby_locations.size() == 0) {
 			adapter.notifyDataSetChanged();
 			adapter.add("No nearby locations found. You may not have reception.");
 			Button next = (Button)findViewById(R.id.Intermediate2Button);
 			next.setVisibility(View.INVISIBLE);
 			next.setClickable(false);
 		}
 		adapter.notifyDataSetChanged();
 	}
 	
 	OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
 		@Override
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
 				long id) {
 			Intermediate.this.currentSelectedVenue = position;
 			Button button = (Button)findViewById(R.id.Intermediate2Button);
 			button.requestFocus();
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 			
 		}
 	};
 	
 	public void toHome(View view) throws IOException {
 		Intent gotoHome = new Intent(this, Home.class);
 		int numCats = 0;
 		
 		if (currentSelectedVenue != -1) {
 			Venue selected;
 			ArrayList<Category> cats;
 			if (nearby_locations != null && nearby_locations.size() > 0) {
 				selected = nearby_locations.get(currentSelectedVenue);
 				
 				// wrong - what we want here is the categories that the user should go to NEXT,
 				// NOT the categories of the place they're currently at
 				cats = selected.getCategories();
 	
 			} else {
 				cats = new ArrayList<Category>();
 				cats.add(new Category("restaurant"));
 				cats.add(new Category("Middle Eastern"));
 				cats.add(new Category("cafe"));
 				
 				selected = new Venue(
 						"Moody's Falafel Palace", 
 						"http://www.yelp.com/biz/moodys-falafel-palace-cambridge",
 						"http://farm4.static.flickr.com/3046/2595970267_aeaf1b35dc.jpg",
 						new GeoPoint((int)(42.3654358 * 1E6), (int)(-71.1042283 * 1E6)),
 						500,
 						cats);
 			}
 			
 			numCats = cats.size();
 			
 			/* Put Name */
 			gotoHome.putExtra("name", selected.getName());
 			 
 			/* Put the number of categories */
 			gotoHome.putExtra("numCats", numCats);
 			
 			/* Put each category as format category + index in list (for ex. category1, category2, etc.) */
 			for (int j = 0; j < numCats; j++) {
 				String key = "Category" + new Integer(j).toString();
 				gotoHome.putExtra(key, (Parcelable)cats.get(j));
 			}
 			
 			/*Put the location just for shits and giggles */
 			gotoHome.putExtra("location", selected.getLatlong().getLatitudeE6() + "," + selected.getLatlong().getLongitudeE6());
 		
 			startActivity(gotoHome);
 		}		
 	}
 	
 	/* -------------TOKEN/CODE STUFF BELOW------------*/
 	
 	private void connectWithFourSquare() {
 		Log.v("Intermediate", "foursquare thread starting");
 		
 		if (code == null && codeStored == false)
 			code = TokenManager.getCode(getIntent(), this, pref);
 		
 		if (code != null)
 			codeStored = true;
 		
 		if (codeStored && this.checkIns == null && !code.equals("-1"))
 			try {
 				initializeCheckIns();
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
 		
 		Log.v("Intermediate", "foursquare thread finishing");
 		foursquare_thread_done = true;
 	}
 	
 	private void initializeCheckIns() throws JSONException {
 			this.token = TokenManager.getToken(context, codeStored, code, pref, oa);
 			this.checkIns = TokenManager.getCheckIns(context, token, checkinsUpdated);
 			if (this.checkIns.size() > 0) {
 				Intermediate.checkinsUpdated = TokenManager.updateHistograms(context, checkinsUpdated, checkIns, pref);
 				this.lastLocation = FoursquareLocationManager.getLastLocation(this.checkIns, this.checkInManager);
 				this.lastLocationName = FoursquareLocationManager.getLastLocationName(this.checkIns, this.checkInManager);
 			}
 	}
 	
 
 	/* ----------------LOCATION CODE BELOW --------------------- */
 	
 	private void locationUpdateWithFoursquare() {
 		while (!foursquare_thread_done || !location_thread_done) {
 			try {
 				Log.v("Intermediate", "final thread waiting for other two threads");
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		if (!receivedLocationUpdate) {
 			try {
 				updateLocationInfo();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			receivedLocationUpdate = true;
 		}
 		runOnUiThread(returnRes);
 	}
 	
 	private void findCurrentLocation() {
 		//initialize location listener
 		
 		if (!locationRegistered) {
 			try {
 				initializeLocationListener();
 				getLastKnownLocation();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		location_thread_done = true;
 	}
 	
 	private void getLastKnownLocation() {
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		Location temp = null;
 		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null) {
 				receivedLastLocationUpdate = true;
 			}
 		}
 		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
 			temp = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			currentLocation = new GeoPoint((int)(temp.getLatitude() * 1E6), (int)(temp.getLongitude() * 1E6));
 			if (currentLocationName == null)
 				receivedLastLocationUpdate = true;
 		}
 		
 		locationRegistered = true;
 	}
 	
 	private void initializeLocationListener() throws JSONException {
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		locationListener = new LocationListener() {
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				currentLocation = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
 				runOnUiThread(returnRes);
 			}
 		};
 		
 		locationRegistered = true;		
 	}
 	
 	public void updateLocationInfo() throws JSONException {
 		JSONArray nearest = FoursquareLocationManager.getCurrentLocationDataFromFoursquare(currentLocation, token);
 		nearby_locations = FoursquareLocationManager.getNearbyLocationsFromFoursquare(nearest, nearby_locations);
 		nearest_location = FoursquareLocationManager.getNearestLocationFromFoursquare(nearby_locations);
 	}
 
 }
