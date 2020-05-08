 /** 
  *   
  *  This class is the second activity of the app and does the heavy lifting.
  *
  *  It loads all the stations for a city and allows the user to see what they are currently playing
  *  
  *  
  *  
  *  @author shayan javed (shayanj@gmail.com)
  *  TODO:
     -Prettier backgrounds
     -Just generally less ugly
     
     -For list of stations, make sure you show which are already favorited
     -Implement favoriting/unfavoriting a station
  
     TODO BUG:
     -When going back to MainActivity and then searching by location, not setting the default checkbox
     (Probably because it's not uppercase)
     
  
  */
 
 package radio.app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.ColorDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ActionBar;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.Menu;
 import android.support.v4.view.MenuItem;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CityActivity extends FragmentActivity {
 	
 	/******************
 	 * PROPERTIES
 	 ******************/
 	
 	// The context
 	Context context;
 
 	// Strings for location, name, artist, song
 	private String _location = "";
 	private String _selectedChannelName = "";
 
 	// The TextViews
 	TextView _locationTV, _stationInfoTV, _currentArtistTV, _currentSongTV;
 
 	// Checkbox for default city and favorite station
 	CheckBox _defaultCityCheckbox, _favoriteCheckbox;
 
 	// Time
 	Time now;
 
 	// Shared Preferences (for default city)
 	public final String PREFERENCE_FILENAME = "CityActivityPreferences";
 	private SharedPreferences _settings;
 
 	// Database for storing favorites
 	private FavoritesDataSource _dataSource;
 	
 	/****************************************************
 	 * METHODS FOR CREATING, PAUSING, RESUMING, DESTROYING
 	 ***************************************************/
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// set layout 
 		setContentView(R.layout.city);
 
 		// get the city from the Bundle passed in
 		String city = getIntent().getExtras().getString("CITY_NAME");
 		if (city != null) {
			_location = city.trim();
 			this._locationTV = (TextView) findViewById(R.id._location);
 			this._locationTV.setText(_location);
 		}
 
 		// Action Bar! (From ActionBarSherlock)
 		final ActionBar ab = getSupportActionBar();
 		ab.setTitle(city);
 		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // standard navigation
 		// background
 		ab.setBackgroundDrawable(new ColorDrawable(android.R.color.primary_text_dark));//getResources().getDrawable(R.drawable.ad_action_bar_gradient_bak));
 		
 		//getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ad_action_bar_gradient_bak));
 		
 		// Check if internet connection is available
 		if (!isNetworkAvailable()) {
 			showToast("No internet connection!", true);
 			return;
 		}
 
 		/** Open the favorites database **/
 		_dataSource = new FavoritesDataSource(this);
 		_dataSource.open();
 		
 		/** Setup the ListView by populating it with the list of radio stations **/
 		// TODO: For some reason not necessary to call here - is it because onResume() gets called automatically?
 		/*AsyncTask<String, Void, String[]> readStationsTask = new ReadStationsTask(
 				this).execute(); */
 
 		Log.v("CHANNELS LOADED:", this._selectedChannelName);
 		// the Time object
 		now = new Time();
 
 		// Show if it's the default city or not
 		_settings = getSharedPreferences(MainActivity.PREFERENCE_FILENAME,
 				MODE_PRIVATE);
 		_defaultCityCheckbox = (CheckBox) findViewById(R.id._defaultCheckbox);
 		
 		if (_settings.contains("DEFAULT")) { // Default City Stored
 			String defaultCity = _settings.getString("DEFAULT", "");
 			// if city is the same as the default city, set it to checked
			// TODO: Also check for numbers (Zipcode)
			if (_location.equalsIgnoreCase(defaultCity.trim())) // TODO: Not working for some reason
 				_defaultCityCheckbox.setChecked(true);
 		}
 
 	} // End of OnCreate
 
 	/** onPause and onResume methods **/
 	@Override
 	protected void onResume() {
 		_dataSource.open();
 		// refresh the stations
 		refresh();
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
 	
 	/************************
 	 * ACTION BAR METHODS
 	 ***********************/
 	
 	/** Action Bar items (loaded as menu items) **/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.layout.city_header, menu);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	/** Selecting Action Bar items **/
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.ab_refresh:		// refresh the song
 				this.refresh();
 				return true;
 			case R.id.ab_search:		// perform a new search
 				// TODO:
 				// Look at performing new search
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	
 	/************************
 	 * STATION METHODS
 	 ************************/
 	
 	/** Updates the default city via the check box **/
 	public void setDefaultCity(View view) {
 		SharedPreferences.Editor prefEditor = _settings.edit();
 
 		if (this._defaultCityCheckbox.isChecked()) { // set the default city to
 														// the current one
 			prefEditor.putString("DEFAULT", this._location.toUpperCase());
 			prefEditor.commit();
 		} else { // remove it as default
 			prefEditor.remove("DEFAULT");
 			prefEditor.commit();
 		}
 	}
 	
 	/** Updates the current song via button click **/
 	public void refresh() {
 		// progress bar
 		CityActivity.this.setProgressBarIndeterminate(true);
 		CityActivity.this.setProgressBarIndeterminateVisibility(true);
 
 		if (!isNetworkAvailable()) {
 			// Alert!
 			showDialog("No Internet Connection! Enable WiFi or 3G");
 			return;
 		}
 
 		/** Setup the ListView by populating it with the list of radio stations **/
 		AsyncTask<String, Void, String[]> readStationsTask = new ReadStationsTask(
 				this).execute();
 	}
 
 	/** Allows the user to set the current station as a favorite **/
 	public void setFavoriteStation(View view) {
 		// TODO:
 		// Get the name
 		//TextView nameView = (TextView)view.get
 		
 		/*if (this._favoriteCheckbox.isChecked()) { // set as favorite
 			this._dataSource.createFavorite(Favorite.Type.STATION, this._selectedChannelName);
 		} else { // remove as favorite
 			Favorite f = new Favorite();
 			f.setName(this._selectedChannelName);
 			f.setType(Favorite.Type.STATION);
 			this._dataSource.deleteFavorite(f);
 		}*/
 	}
 
 	/** Inner class used just for listening to the ListView **/
 	public class MyOnItemClickListener implements OnItemClickListener {
 
 		// When item selected start new station activity
 		public void onItemClick(AdapterView<?> parent, View view, int pos,
 				long id) {
 				
 			_selectedChannelName = parent.getItemAtPosition(pos).toString();
 			Log.v("Clicked List:", _selectedChannelName);
 			
 			Intent intent = new Intent(CityActivity.this, StationActivity.class);
 
 			Bundle parameters = new Bundle();
 			parameters.putString("STATION_NAME", _selectedChannelName);
 			intent.putExtras(parameters);
 
 			CityActivity.this.startActivity(intent); 
 		}
 	}
 
 	/** Reads the JSON about the channel **/
 	public String readJSON(String info, boolean station) {
 		// Format the info properly (for city)
 		info = info.trim();
 		// replace spaces with proper html?
 		info = info.replaceAll(" ", "&nbsp;");
 	
 		StringBuilder builder = new StringBuilder();
 	
 		// Create a HTTPClient
 		HttpClient client = new DefaultHttpClient();
 	
 		// Open a GET connection to the Yes.com api
 		HttpGet httpGet;
 		if (station) {
 			httpGet = new HttpGet("http://api.yes.com/1/station?name=" + info);
 		} else { // get list of stations instead for the provided city/zipcode
 			httpGet = new HttpGet("http://api.yes.com/1/stations?loc=" + info);
 		}
 	
 		try {
 			// Read all the lines one-by-one
 			HttpResponse response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 	
 				Log.e(CityActivity.class.toString(),
 						"RESPONSE BUILT: " + builder.toString());
 	
 			} else {
 				Log.e(CityActivity.class.toString(), "Failed to download file");
 				//showToast("Website seems to be down - please try again later.", false);
 				//finish();
 				return null;
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			this._stationInfoTV.setText("Exception " + e.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 			this._stationInfoTV.setText("Exception " + e.toString());
 		}
 		return builder.toString();
 	}
 
 	/***
 	 * AsyncTask class for reading all the stations
 	 */
 	private class ReadStationsTask extends AsyncTask<String, Void, String[]> {
 		// The dialog bar to show indeterminate progress
 		private ProgressDialog dialog;
 		private Context theContext;
 
 		// constructor
 		public ReadStationsTask(Activity activity) {
 			theContext = activity;
 			CityActivity.this.setProgressBarIndeterminate(true);
 			CityActivity.this.setProgressBarIndeterminateVisibility(true);
 			// start the progress dialog
 			dialog = new ProgressDialog(theContext);
 		}
 
 		@Override
 		protected String[] doInBackground(String... params) {
 			// perform long running operation operation
 			// update the list of stations
 			String channelJSON = readJSON(_location, false);
 			Log.v(CityActivity.class.getName(), "channelJSON: " + channelJSON);
 			
 			if (channelJSON == null) // no info - probably server is down
 				return null;
 			
 			try {
 				// create the json object from the string
 				JSONObject jsonobj = new JSONObject(channelJSON);
 
 				// get the array of stations
 				JSONArray jsonArray = new JSONArray(
 						jsonobj.getString("stations"));
 
 				/** TODO: Perhaps part below should be in postExecute()? **/
 
 				// Create the array of strings which will be used for populating
 				// the spinner
 				String[] stations = new String[jsonArray.length()];
 
 				// If not stations found
 				if (stations.length == 0) {
 					showToast("No stations found!", false);
 					finish(); // TODO: Properly show dialog
 				}
 
 				// go through each station
 				for (int i = 0; i < jsonArray.length(); i++) {
 					JSONObject jsonObject = jsonArray.getJSONObject(i);
 					// get the station name
 					String name = jsonObject.getString("name");
 					// get the description
 					String desc = properFormat(jsonObject.getString("desc"));
 
 					// add to the string array
 					stations[i] = name + " " + desc;
 				}
 
 				return stations;
 
 			} catch (Exception e) {
 				Log.e(CityActivity.class.getName(),
 						"JSON Exception: " + e.toString());
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String[] result) {
 			// execution of result of Long time consuming operation
 			// dismiss the dialog
 			dialog.dismiss();
 	
 			try {
 				// get list of all the stations from the task
 				String[] stations = new String[1];
 
 				// get() causes a block on the UI Thread!
 				stations = this.get();
 
 				if (stations != null) {
 					// set selected channel to the first one
 					CityActivity.this._selectedChannelName = stations[0];
 					Log.v("SELECTED CHANNEL:", CityActivity.this._selectedChannelName);
 					
 					// Populate the ListView
 					ListView lv = (ListView) findViewById(R.id._stationsList);
 					CustomArrayAdapter cadapter = new CustomArrayAdapter( 
 							CityActivity.this,
 							stations);
 		
 					lv.setAdapter(cadapter);
 					// now set the listview to listen for changes
 					lv.setOnItemClickListener(new MyOnItemClickListener());
 					//lv.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
 					/*lv.setOnItemClickListener(new OnItemClickListener() {
 					    @Override
 					    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
 					        Log.i("Clicked!!", "onListItemClick: " + position);
 
 					        }
 
 					    }
 					);*/
 				}
 				else 
 					throw new Exception("");
 			} catch (Exception e) {
 				Log.e("Reading Stations", "Cannot execute AsyncTask: " + e.toString());
 				
 				Context context = getApplicationContext();
 				int duration = Toast.LENGTH_LONG; // long
 				Toast toast = Toast.makeText(context, "Website seems to be down - please try again later.", duration);
 				toast.show();
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// Things to be done before execution of long running operation. For
 			// example showing ProgessDialog
 
 			// set the properties of the progressdialog
 			// make sure it's indeterminate
 			this.dialog.setMessage("Loading list of stations");
 			this.dialog.setIndeterminate(true);
 			this.dialog.show();
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			// Things to be done while execution of long running operation is in
 			// progress. For example updating ProgessDialog
 		}
 	}
 
 	/*************************************
 	 * HELPER METHODS
 	 ************************************/
 	
 	/** Creates an alert dialog **/
 	private void showDialog(CharSequence text) {
 		// build a dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		// set the message
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
 
 	/** Checks if internet connection is available **/
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 
 	// Formats the HTML Strings properly
 	// TODO: (&lt; &gt;)?
 	private String properFormat(String str) {
 		String proper = str.replaceAll("&amp;", "&");
 		proper = proper.replaceAll("&quot;", "\"");
 		proper = proper.replaceAll("&apos;", "\'");
 	
 		return proper;
 	}
 
 	/** End of AsyncTask class **/
 
 }
