 /** This is the starting activity for the app.
  *  It shows a textbox where the user can search by name, zipcode, etc. There is also a button for just using the
  *  current location.
  *  
  *  If the default location is set, it will directly go to the second activity.
  *  
  *  It also shows the favourite stations/songs of the user.
  *  
  *  TODO:
  *  -Create custom ListView/ListAdapter (use in both this and StationActivity.java). Needs to be done for this class
  *  -Show favourite stations (cities?)
  *  	* Should be able to click on station to open up StationView.java or something
  	-Add autocomplete option (by cities)
  	-Custom background
 	-Have main app name with search box (should lead to different activity) 					[DONE]
     -Allow location by GPS 																		[PARTIALLY DONE]
     
     -Add ImageButtons for search (the magnifying glass and the location icon from Google Maps) 	[DONE!]
  	 	-Add onfocus, onclick images
  */
 
 package radio.app;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import radio.app.CityActivity.MyOnItemClickListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 //import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 //import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class MainActivity extends Activity {
 	
 	/******************
 	 * PROPERTIES
 	 ******************/
 	private EditText _searchText;  											// Search text box
 	public static final String PREFERENCE_FILENAME = "RadioAppPreferences"; // Shared preferences name
 	private ListView _favoritesLV; 											// ListView for showing favorites
 	private FavoritesDataSource _dataSource; 								// Database for storing favorites
 	// GPS
 	private LocationManager locationManager;
 	private double _latitude, _longitude;
 	
 	/****************************************************
 	 * METHODS FOR CREATING, PAUSING, RESUMING, DESTROYING
 	 ***************************************************/
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// No Title - Temporary TODO
		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// set the custom title
 		// setCustomTitle();
 
 		// Change to main layout
 		setContentView(R.layout.main);
 
 		// Retrieve widgets
 		_searchText  = (EditText) findViewById(R.id.searchBox); // search box
 		_favoritesLV = (ListView) findViewById(R.id.favoritesLV); // favorites
 
 		// Set the listener for the Search Textbox - 
 		// on pressing enter should start a search
 		TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
 			public boolean onEditorAction(TextView exampleView, int actionId,
 					KeyEvent event) {
 				// if(actionId == EditorInfo.IME_NULL){
 				startSearch(_searchText);// match this behavior to your 'Send'
 											// (or Confirm) button
 				// }
 				return true;
 			}
 		};
 		_searchText.setOnEditorActionListener(exampleListener);
 
 		// Location Manager for getting latitude/longitude
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		
 		// TODO: Open it again on onResume?
 		// temporary - to delete a whole database
 		//this.deleteDatabase("favorites.db");
 		
 		/** Open the favorites database **/
 		_dataSource = new FavoritesDataSource(this);
 		_dataSource.open();
 		
 		// Show the favorites
 		updateFavorites();
 		
 		/**
 		 * Check Shared preferences - if there is a default city already stored,
 		 * just show the stations for that
 		 **/
 		SharedPreferences settings = getSharedPreferences(
 				MainActivity.PREFERENCE_FILENAME, MODE_PRIVATE);
 
 		if (settings.contains("DEFAULT")) { // Default City Stored
 			String defaultCity = settings.getString("DEFAULT", "");
 			// start activity with that city
 			search(defaultCity);
 
 		}
 	} // End of OnCreate
 
 	/** onPause, onResume  and onDestroy methods **/
 	@Override
 	protected void onResume() {
 		_dataSource.open();
 		// update favorites
 		updateFavorites();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		_dataSource.close();
 		super.onPause();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		_dataSource.close();
 		super.onDestroy();
 	}
 	
 	/**********************************
 	 * METHODS FOR HANDLING FAVORITE STATIONS
 	 *********************************/
 	
 	/** Updates the favorites list **/
 	private void updateFavorites() {
 		// get all the favorites
 		List<Favorite> values = _dataSource.getAllFavorites();
 
 		// Populate the ListView
 		FavoritesArrayAdapter adapter = new FavoritesArrayAdapter(
 				MainActivity.this,
 				values);
 		_favoritesLV.setAdapter(adapter);
 		
 		// now set the listview to listen for changes
 		_favoritesLV.setOnItemClickListener(new MyOnItemClickListener());
 		//_favoritesLV.setOnItemSelectedListener(new FavoriteSelectedListener());
 	}
 	
 	/** Allows the user to unfavorite a station **/
 	public void setFavoriteStation(View view) {
 		// TODO:
 		// Convert to 
 		TextView nameView = (TextView)view;
 		/*
 		if (this._favoriteCheckbox.isChecked()) { // set as favorite
 			this._dataSource.createFavorite(Favorite.Type.STATION, this._selectedChannelName);
 		} else { // remove as favorite
 			Favorite f = new Favorite();
 			f.setName(this._selectedChannelName);
 			f.setType(Favorite.Type.STATION);
 			this._dataSource.deleteFavorite(f);
 		}*/
 		
 		// refresh
 		this.updateFavorites();
 	}
 	
 	/** Inner class used just for listening to the Favorites ListView **/
 	public class MyOnItemClickListener implements OnItemClickListener {
 
 		// When item selected start new station activity
 		public void onItemClick(AdapterView<?> parent, View view, int pos,
 				long id) {
 			
 			// Get the selected favorite
 			Favorite fave = (Favorite)parent.getItemAtPosition(pos);
 			Log.i("Selected FavoriteArrayAdapter:", fave.getName());
 			// TODO: start the station activity
 			Intent intent = new Intent(MainActivity.this, StationActivity.class);//RadioApp.class);
 
 			Bundle parameters = new Bundle();
 			parameters.putString("STATION_NAME", fave.getName());
 			intent.putExtras(parameters);
 
 			MainActivity.this.startActivity(intent); 
 			
 		}
 	}
 	
 	/************************
 	 * METHODS FOR SEARCHING
 	 ***********************/
 	
 	/** Method called when search button is pressed **/
 	public void startSearch(View v) {
 		// read text to search
 		String toSearch = _searchText.getText().toString();
 
 		// if empty, return nothing - alert for now TODO:
 		if (toSearch.equals("")) {
 			showToast("Nothing entered!", true);
 			return;
 		}
 
 		// else, do a search
 		this.search(toSearch);
 	}
 
 	/** Method called when gps search button is pressed **/
 	public void searchByGPS(View v) {
 		/*****
 		 * TODO
 		 * TEMPORARY: Convert Latitude/Longitude to city
 		 */
 		// get the latitude/longitude
 		String locationProvider = LocationManager.NETWORK_PROVIDER;
 		Location lastKnownLocation = locationManager
 				.getLastKnownLocation(locationProvider);
 		try {
 			_latitude = lastKnownLocation.getLatitude();
 			_longitude = lastKnownLocation.getLongitude();
 			Log.v("GPS", "Latitude: " + _latitude + ", Longitude: "
 					+ _longitude);
 		} catch (Exception e) {
 			Log.e("GPS", "Could not get lat/lon of location");
 
 		}
 
 		Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
 		List<Address> addresses;
 		String locality = "";
 		try {
 			addresses = gcd.getFromLocation(this._latitude, this._longitude, 1);
 			if (addresses.size() > 0) {
 				locality = addresses.get(0).getLocality();// Log.e("GPS LOCALITY",
 															// addresses.get(0).getLocality());
 				this.search(locality);
 			} else {
 				showToast("Error obtaining location.", true);
 				return;
 			}
 
 		} catch (IOException e) {
 			showToast("Error obtaining location.", true);
 			return;
 		}
 	}
 
 	/** Method for sending search to the second activity **/
 	private void search(String location) {
 		// check internet connection
 		if (!this.isNetworkAvailable()) {
 			// showDialog("No Internet connection! Please turn on wi-fi or your data connection.");
 			showToast(
 					"No Internet connection! Please turn on wi-fi or your data connection.",
 					false);
 			return;
 		}
 
 		// Send info to other city
 		Intent intent = new Intent(this, CityActivity.class);//RadioApp.class);
 		/** Add Bundle here (City, location, etc.): **/
 
 		Bundle parameters = new Bundle();
 		parameters.putString("CITY_NAME", location);
 		intent.putExtras(parameters);
 
 		this.startActivity(intent);
 		//this.onPause();
 	}
 
 	
 	/*************************************
 	 * HELPER METHODS
 	 ************************************/
 
 	/** For a custom title at the top **/
 	private void setCustomTitle() {
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		// custom title
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 				R.layout.window_title);
 
 		// set the custom texview and icon
 		TextView title = (TextView) findViewById(R.id.title);
 		ImageView icon = (ImageView) findViewById(R.id.windowSearchButton);
 		title.setText("Search");
 		icon.setImageResource(R.drawable.icon);
 	}
 	
 	/** Checks if internet connection is available **/
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 
 	/** Creates an alert dialog with the passed in text **/
 	private void showDialog(CharSequence text) {
 		// build a dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setMessage(text).setCancelable(false)
 		/*
 		 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		 * public void onClick(DialogInterface dialog, int id) {
 		 * MyActivity.this.finish(); } })
 		 */
 		// set only the negative button
 				.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	/** Helper method for showing a Toast notification **/
 	private void showToast(CharSequence text, boolean timeShort) {
 		Context context = getApplicationContext();
 		int duration = timeShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG; // long
 																			// or
 																			// short?
 
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 
 }
