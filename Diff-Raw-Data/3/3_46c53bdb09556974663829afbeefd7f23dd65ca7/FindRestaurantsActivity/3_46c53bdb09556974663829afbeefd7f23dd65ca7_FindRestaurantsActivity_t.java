 package com.example.restaurantreviewapplication;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class FindRestaurantsActivity extends Activity {
 
 	private ImageButton searchNearby;
 	private ImageButton searchButton;
 	private EditText zipCodeText;
 	private EditText keywordText;
 	private UserApplication app;
 	private Boolean buttonClick;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	
 	private ProgressDialog dialog;
 	//private Server2 serverConnection;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		app = (UserApplication)getApplication();
 		//serverConnection = new Server2(this);
 		setContentView(R.layout.activity_find_restaurants);
 		setupViews();
 		
 		dialog = new ProgressDialog(FindRestaurantsActivity.this);
 		dialog.setMessage("Loading...");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_find_restaurants, menu);
 		return true;
 	}
 	
 	private void setupViews(){
 		searchNearby = (ImageButton) findViewById(R.id.searchNearby);
 		searchButton = (ImageButton) findViewById(R.id.searchButton);
 		zipCodeText = (EditText) findViewById(R.id.zipCodeText);
 		keywordText = (EditText) findViewById(R.id.keywordText);
 	}
 	
 	// needs work	
 	public void searchNearbyButtonHandler(View V){
 		
 		dialog.show();
 		
 		//searchNearby.setText("Waiting on Location");
 		buttonClick = true;
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 		    public void onLocationChanged(Location location) {
 		      // Called when a new location is found by the network location provider.
 		    	if(buttonClick){
 		    		makeUseOfNewLocation(location);
 		    		buttonClick = false;
 		    	}
 		      
 		    }
 
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 		    public void onProviderEnabled(String provider) {}
 
 		    public void onProviderDisabled(String provider) {}
 		  };
 
 		// Register the listener with the Location Manager to receive location updates
 		String p = locationManager.getBestProvider(new Criteria(), true);
 		if(p == null) {
 			Toast.makeText(getApplicationContext(),
 					"No Location Services Available", Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 		locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), 0, 0, locationListener);
 	}
 
 	public void searchButtonHandler(View V){
 				
 		//searchButton.setText("Searching...");
 		String keyword;
 		int zip = 0;
 		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
 		
 		keyword = keywordText.getText().toString();
 
 		if (!(keyword.trim().length() == 0)) {
 			
 			app.setSearchText(keyword);
 			
 			dialog.show();
 			
 			String rawzip = zipCodeText.getText().toString();
 
 			if (!(rawzip.trim().length() == 0)) {
 				zip = Integer.parseInt(rawzip);
				app.setSearchText(keyword + " nearby " + zip);
 			}
 			//serverConnection.getRestaurants(zip, keyword);
 			restaurants = Server.getRestaurantsByZipKeyword(zip, keyword);
 			if(restaurants != null){
 				//searchButton.setText("Search");
 				app.setRestaurants(restaurants);
 				Intent intent = new Intent(this, ListRestaurantsActivity.class);
 				startActivity(intent);
 				
 				dialog.dismiss();
 			}else{
 				Toast.makeText(getApplicationContext(),
 						"No Restaurants Found.", Toast.LENGTH_SHORT)
 						.show();
 				//searchButton.setText("Search");
 			}
 		}else{
 			//pop up please enter keyword
 			Toast.makeText(getApplicationContext(),
 					"Please enter keyword.", Toast.LENGTH_SHORT)
 					.show();
 			//searchButton.setText("Search");
 		}	
 	}
 	
 	protected void makeUseOfNewLocation(Location location) {
 		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
 		restaurants = Server.getRestaurantsByLocation(location);
 
 		//this is for testing
 		//app.a = location;
 		
 		//searchNearby.setText("Search Nearby");
 		
 		if (restaurants != null) {
			app.setSearchText("nearby");
 			app.setRestaurants(restaurants);
 
 			Intent intent = new Intent(this, ListRestaurantsActivity.class);
 			startActivity(intent);
 			
 			dialog.dismiss();
 			
 			// Remove the listener you previously added
 			locationManager.removeUpdates(locationListener);
 		} else {
 			// pop up message saying no restaurants found
 
 		}
 		
 	}
 
 //	// Dummy data for testing.  Replace with call to server
 //	public void getRestaurants(int zip, String keyword) {
 //		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
 //		int i;
 //
 //		Restaurant a;
 //
 //		
 //		for (i = 0; i < 10; i++) {
 //			a = new Restaurant("Restaurant " + i, i*2 + " Main St", "407-555-1212", "GPS Coords");
 //			restaurants.add(a);
 //		}
 //		
 //		app.setRestaurants(restaurants);
 ////		return restaurants;
 //	}
 }
