 package com.cs371m.austinrecycle;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.provider.Settings;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.graphics.Color;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private static final String TAG = "MainActivity.java";
 
 	private EditText _materialEditText;
 	private ImageButton _searchButton;
 	private AutoCompleteTextView _locationAutoCompleteTextView;
 	private ListView _listView;
 
 	private MaterialListAdapter _adapter;
 	private String[] _materialNames;
 	private TypedArray _icons;
 	private ArrayList<MaterialItem> _materialItemArray;
 
 	private Geocoder _geocoder;
 	private double _currentLat;
 	private double _currentLong;
 	private LocationManager _locationManager;
 
 	private ProgressDialog _progressDialog;
 	private AlertDialog _materialListDialog;
 
 	private PlacesTask _placesTask;
 
 	/**
 	 * onCreate
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		_materialItemArray = new ArrayList<MaterialItem>();
 		_geocoder = new Geocoder(this);
 
 		// Setup _materialEditText to show MaterialDialog when clicked
 		_materialEditText = (EditText) MainActivity.this.findViewById(R.id.materials_editText);
 		_materialEditText.setKeyListener(null);
 		_materialEditText.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				_materialEditText.setText("");
 				_materialItemArray.clear();
 				showMaterialDialog();
 			}
 		});
 
 		// Setup actions when the search button is clicked
 		_searchButton = (ImageButton) MainActivity.this.findViewById(R.id.search_button);
 		_searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Users must select at least ONE material
 				if(_materialEditText.getText().toString().equals("")) {
 					Toast.makeText(MainActivity.this, "Please select at least ONE material", Toast.LENGTH_SHORT).show();
 				}
 				// If users leave the location blank, use GPS to find current location of the device
 				else if(_locationAutoCompleteTextView.getText().toString().equals("")) {
 					showLocationDialog();
 				}
 				else {
 					// Get the latitude and longitude of current location
 					try {
 						String currentAddress = _locationAutoCompleteTextView.getText().toString();
 						List<Address> returnedAddress = _geocoder.getFromLocationName(currentAddress, 1);
 						_currentLat = returnedAddress.get(0).getLatitude();
 						_currentLong = returnedAddress.get(0).getLongitude();
 					}
 					catch(IOException e) {
 						Log.e(TAG, "Error occured in Geocoder: ", e);
 					}
 
 					// SelectedMaterial is converted to lower case and replaced whitespace with underscore to match the database field name
 					String selectedMaterial = _materialEditText.getText().toString().toLowerCase().replace(' ', '_');
 
 					// Convert to String array to pass as parameter
 					String[] selectedMaterialArray = selectedMaterial.split(",");
 
 					// Needs to create a new task every time
 					new NetworkRequestTask().execute(selectedMaterialArray);	
 				}
 			}
 		});
 
 		// Location AutoComplete using suggestions from Google Location API
 		_locationAutoCompleteTextView = (AutoCompleteTextView) MainActivity.this.findViewById(R.id.location_autoCompleteTextView);
 		_locationAutoCompleteTextView.setThreshold(2);
 		_locationAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				_placesTask = new PlacesTask();
 				_placesTask.execute(s.toString());
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		_locationAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 				_placesTask.cancel(true);
 				_locationAutoCompleteTextView.dismissDropDown();
 			}
 		});
 	}
 
 	/**
 	 * onResume() is called after onCreate()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	/**
 	 * onStop() will be called when the orientation is changed
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.d(TAG, "onStop");
 		if(_materialListDialog != null) {
 			_materialListDialog.dismiss();
 		}
 		if(_progressDialog != null) {
 			_progressDialog.dismiss();
 		}
 		if(_placesTask == null) {
 			_placesTask = new PlacesTask();
 		}
 	}
 
 	/**
 	 * onPause
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case(R.id.action_about):
 			showAbout();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void showAbout() {
 		AlertDialog.Builder aboutDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 		aboutDialogBuilder.setTitle("About Austin Recycling");
		aboutDialogBuilder.setMessage("About the app...\nblah...blah...blah");
 		
 		AlertDialog aboutDialog = aboutDialogBuilder.create();
 		aboutDialog.show();
 	}
 
 	/**
 	 * Display list of materials
 	 * Selected materials will be removed from the list
 	 */
 	private void showMaterialDialog() {
 		Log.d(TAG, "entering popChooseMaterialDialog");
 
 		final AlertDialog.Builder materialDialogBuilder = new AlertDialog.Builder(this);
 		materialDialogBuilder.setTitle("Please select materials");
 
 		// Set the view of Alert Dialog to custom ListView
 		LayoutInflater inflater = MainActivity.this.getLayoutInflater();
 		final View popupLayout = inflater.inflate(R.layout.material_list_view, null);
 		_listView = (ListView) popupLayout.findViewById(R.id.material_listView);
 		_listView.setBackgroundColor(Color.WHITE);
 		materialDialogBuilder.setView(popupLayout);
 
 		// Dialog CANCEL button
 		materialDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				_materialEditText.setText("");
 				dialog.dismiss();
 			}
 		});
 
 		// Dialog DONE button
 		materialDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 
 		// Read material names and icons from arrays.xml
 		_materialNames = MainActivity.this.getResources().getStringArray(R.array.list_material_name);
 		_icons = MainActivity.this.getResources().obtainTypedArray(R.array.list_material_icon);
 
 		// Store MaterialItem into ArrayList
 		for(int i=0; i<_materialNames.length; ++i) {
 			//			_materialItemArray.add(new MaterialItem(_icons.getResourceId(i, 0), _materialNames[i], false));
 			_materialItemArray.add(new MaterialItem(_icons.getResourceId(i, 0), _materialNames[i]));
 		}
 		_icons.recycle();
 
 		// Setup custom adapter
 		_adapter = new MaterialListAdapter(this, R.layout.material_list_item, _materialItemArray);
 		_listView.setAdapter(_adapter);
 		_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 				Log.d(TAG, "position clicked: " + position);
 				// Grab the clicked item's name before removing the item from
 				String clickedMaterial = _materialItemArray.get(position).getName();
 				String oldString = _materialEditText.getText().toString();
 
 				// Remove clicked item from the adapter's data array and update
 				// => It gets removed from the dialog
 				_materialItemArray.remove(position);
 				_adapter.notifyDataSetChanged();
 
 				// Format into comma separated string
 				String newString = oldString.equals("") ? clickedMaterial : oldString + "," + clickedMaterial;
 				_materialEditText.setText(newString);
 			}
 		});
 
 		_materialListDialog = materialDialogBuilder.create();
 		_materialListDialog.show();
 	}
 
 	/**
 	 * Check GPS status
 	 * @return true if GPS is on
 	 * @return false if GPS is off
 	 */
 	private boolean checkGpsStatus() {
 		ContentResolver contentResolver = MainActivity.this.getBaseContext().getContentResolver();
 		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
 		return gpsStatus;
 	}
 
 	/**
 	 * Get current location using GPS and display the address to _locationAutoCompleteTextView
 	 */
 	private void getCurrentLocation() {
 		_locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setPowerRequirement(Criteria.POWER_HIGH);
 		String best = _locationManager.getBestProvider(criteria, true);
 		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 10, new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				Log.d(TAG, "onLocationChanged");
 				_currentLat = location.getLatitude();
 				_currentLong = location.getLongitude();
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				Log.d(TAG, "onProviderDisabled");
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				Log.d(TAG, "onProviderEnabled");
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				Log.d(TAG, "onStatusChanged");
 			}
 		});
 
 		Location location = _locationManager.getLastKnownLocation(best);
 		_currentLat = location.getLatitude();
 		_currentLong = location.getLongitude();
 
 		try {
 			List<Address> returnedAddress = _geocoder.getFromLocation(_currentLat, _currentLong, 1);
 			Address currentAddress = returnedAddress.get(0);
 			_locationAutoCompleteTextView.setText(currentAddress.getAddressLine(0) + ", "
 					+ currentAddress.getAddressLine(1) + ", "
 					+ currentAddress.getAddressLine(2));
 		}
 		catch (IOException e) {
 			Log.e(TAG, "Error getting current location", e);
 		}
 	}
 
 	/**
 	 * Build locationDialog
 	 */
 	private void showLocationDialog() {
 		final AlertDialog.Builder locationDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 		locationDialogBuilder.setTitle("Use current location");
 		locationDialogBuilder.setMessage("You did not enter an address. Would you like to use your current location?");
 		locationDialogBuilder.setNegativeButton("No", new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		locationDialogBuilder.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if(checkGpsStatus()) {
 					MainActivity.this.getCurrentLocation();
 				}
 				else {
 					dialog.dismiss();
 					showGpsDialog();
 				}
 			}
 		});
 
 		final AlertDialog locationDialog = locationDialogBuilder.create();
 		locationDialog.show();
 	}
 
 	/**
 	 * Build GPS Settings dialog
 	 */
 	private void showGpsDialog() {
 		final AlertDialog.Builder gpsDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 		gpsDialogBuilder.setTitle("Turn on GPS");
 		gpsDialogBuilder.setMessage("Please turn on your GPS and try again.");
 		gpsDialogBuilder.setNegativeButton("Try again", new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		gpsDialogBuilder.setPositiveButton("Go to Settings", new AlertDialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Log.d(TAG, "GPS Settings");
 				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 				MainActivity.this.recreate();
 			}
 		});
 		final AlertDialog gpsDialog = gpsDialogBuilder.create();
 		gpsDialog.show();
 	}
 
 	/**
 	 * Build progress dialog
 	 * Display when searching for facilities
 	 */
 	private void showProgressDialog() {
 		_progressDialog = new ProgressDialog(MainActivity.this);
 		_progressDialog.setTitle("Searching");
 		_progressDialog.setMessage("Searching for locations...");
         _progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         _progressDialog.show();
 	}
 
 	/**
 	 * Class to run HTTP network requests in a worker thread. Necessary to
 	 * keep the UI interactive.
 	 * 
 	 * Types specified are <Argument Type, Progress Update Type, Return Type>
 	 */
 	private class NetworkRequestTask extends AsyncTask<String, Float, ArrayList<FacilityItem>> {
 		private static final String TAG = "MainActivity.NetworkRequestTask";
 
 		@Override
 		protected ArrayList<FacilityItem> doInBackground(String... materials) {
 			Log.d(TAG, "begin doInBackground");
 			Model m = new Model(_currentLat, _currentLong);
 			Log.d(TAG, "end doInBackground");
 			return m.getFacilities(materials);
 		}
 		
 		 @Override
 		    protected void onPreExecute() {
 				showProgressDialog();
 		    }
 		 
 		/** 
 		 * Invoked in asynchronously in MainActivity when the network request 
 		 * has finished and doInBackground returns its result.
 		 */
 		@Override
 		protected void onPostExecute(ArrayList<FacilityItem> facilities) {
 			Log.d(TAG, "begin onPostExecute");
 			_progressDialog.dismiss();
 			// Starting the ResultListActivity
 			Intent resultIntent = new Intent(MainActivity.this, ResultListActivity.class);
 			resultIntent.putParcelableArrayListExtra("RETURNED_RESULT", (ArrayList<? extends Parcelable>) facilities);
 			resultIntent.putExtra("CURRENT_LAT", _currentLat);
 			resultIntent.putExtra("CURRENT_LONG", _currentLong);
 			MainActivity.this.startActivity(resultIntent);
 			Log.d(TAG, "end onPostExecute");
 		}
 	}
 
 	/**
 	 * Asynchronously get place suggestions from Google
 	 */
 	private class PlacesTask extends AsyncTask<String, Void, ArrayList<String>> {
 		private static final String TAG = "MainActivity.PlacesTask";
 		private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
 		private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
 		private static final String OUT_JSON = "/json";
 		private static final String API_KEY = "AIzaSyCnLUmKZNvy5P7R2p1RJw2fd4VGNRbcJBU";
 
 		protected ArrayList<String> doInBackground(String... input) {
 			Log.d(TAG, "Async PlacesTask doInBackground(): ");
 
 			ArrayList<String> resultList = null;
 			HttpURLConnection conn = null;
 			StringBuilder jsonResults = new StringBuilder();
 			try {
 				StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
 				sb.append("?sensor=true");
 				sb.append("&components=country:us");
 				sb.append("&input=" + URLEncoder.encode(input[0], "utf8"));
 				sb.append("&country=austin");
 				sb.append("&types=geocode");
 				sb.append("&key=" + API_KEY);
 
 				URL url = new URL(sb.toString());
 				conn = (HttpURLConnection) url.openConnection();
 				InputStreamReader in = new InputStreamReader(conn.getInputStream());
 
 				// Load the results into a StringBuilder
 				int read;
 				char[] buff = new char[1024];
 				while ((read = in.read(buff)) != -1) {
 					jsonResults.append(buff, 0, read);
 				}
 			}
 			catch (MalformedURLException e) {
 				Log.e(TAG, "Error processing Places API URL", e);
 			}
 			catch (IOException e) {
 				Log.e(TAG, "Error connecting to Places API", e);
 			}
 			finally {
 				if (conn != null) {
 					conn.disconnect();
 				}
 			}
 
 			try {
 				// Create a JSON object hierarchy from the results
 				JSONObject jsonObj = new JSONObject(jsonResults.toString());
 				JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
 
 				// Extract the Place descriptions from the results
 				resultList = new ArrayList<String>(predsJsonArray.length());
 				for (int i = 0; i < predsJsonArray.length(); i++) {
 					resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
 				}
 			}
 			catch (JSONException e) {
 				Log.e(TAG, "Cannot process JSON results", e);
 			}
 
 			return resultList;
 		}
 
 		protected void onPostExecute(ArrayList<String> resultList) {
 			Log.d(TAG, "Async PlacesTask onPostExecute(): ");
 			LocationAutoCompleteAdapter locAdapter = new LocationAutoCompleteAdapter(MainActivity.this, R.layout.location_list_item, resultList);
 			_locationAutoCompleteTextView.setAdapter(locAdapter);
 			_locationAutoCompleteTextView.showDropDown();
 		}
 	}
 }
