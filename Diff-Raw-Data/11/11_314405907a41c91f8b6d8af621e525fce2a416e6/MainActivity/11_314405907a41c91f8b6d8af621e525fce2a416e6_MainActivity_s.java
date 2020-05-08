 package com.khaotixinc.barhopper;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import net.simonvt.menudrawer.MenuDrawer;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.khaotixinc.barhopper.menu.Item;
 import com.khaotixinc.barhopper.menu.MenuAdapter;
 
 public class MainActivity extends FragmentActivity {
 
 	private MenuDrawer menuDrawer;
 
 	private ListView menuListView;
 	private MenuAdapter menuAdapter;
 
 	private String defaultSearch;
 	private float defaultRating;
 
 	private float lastRating;
 
 	private static String MY_LOCATION = "My Location";
 
 	LocationManager locationManager;
 	LocationListener locationListener;
 
 	MainActivity activity = this;
 	Intent intent;
 
 	boolean busy = false;
 	boolean lastSoftKeyboard = false;
 	boolean startFromMap = false;
 
 	JSONBarManager manager;
 
 	GeocodeManager geoSync;
 	private boolean geoSyncing = false;
 	private boolean usingLocation = false;
 
 	private Runnable updateRunnable;
 
 	private boolean showImproveDialog = true;
 	private boolean loadingDialogVisible = false;
 
 	private LoadingDialog dialog = new LoadingDialog();
 
 	private boolean changingActivity = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		dialog.setCancelable(false);
 
 		SharedPreferences settings = getSharedPreferences("BH_PREFERENCES", 0);
 
 		showImproveDialog = settings.getBoolean("IMPROVE_DIALOG", true);
 
 		Intent intent = getIntent();
 
 		startFromMap = intent.getBooleanExtra("FROM_MAP_ACTIVITY", false);
 
 		// Adding a left menu drawer to the activity
 		menuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT);
 
 		// Make the menu appear by dragging it from anywhere on the screen
 		menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
 
 		// sets the content for the drawer
 		menuDrawer.setContentView(R.layout.activity_main);
 
 		menuAdapter = new MenuAdapter(this);
 
 		// Populate the menu list
 		menuAdapter.addItems(getString(R.string.menu_category_search),
 				MenuAdapter.ItemType.CATEGORY);
 		menuAdapter.addItems(getString(R.string.menu_item_search),
 				MenuAdapter.ItemType.ITEM);
 		menuAdapter.addItems(getString(R.string.menu_category_show),
 				MenuAdapter.ItemType.CATEGORY);
 		menuAdapter.addItems(getString(R.string.menu_item_like),
 				MenuAdapter.ItemType.ITEM);
 		menuAdapter.addItems(getString(R.string.menu_item_dislike),
 				MenuAdapter.ItemType.ITEM);
 
 		// sets the menu
 		menuListView = new ListView(this);
 		menuListView.setAdapter(menuAdapter);
 		menuListView.setOnItemClickListener(menuItemClickListener);
 
 		menuListView.setPadding(25, 25, 25, 25);
 		menuListView.setDivider(null);
 
 		menuDrawer.setMenuView(menuListView);
 
 		// Set the layout to it's default options
 		defaultSearch = getString(R.string.default_address);
 		defaultRating = ((RatingBar) findViewById(R.id.ratingBar1)).getRating();
 		lastRating = defaultRating;
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		locationListener = new LocationListener() {
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 			}
 		};
 
 		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
 		textView.setAdapter(new AutoCompleteAdapter(this,
 				android.R.layout.simple_dropdown_item_1line, this));
 		textView.setOnItemClickListener(autoCompleteItemClickListener);
 		textView.setTextColor(Color.GRAY);
 		textView.setOnFocusChangeListener(autoCompleteFocusChangeListener);
 		
 		
 		Button button = (Button) findViewById(R.id.button1);
 		button.setBackgroundColor(Color.GRAY);
 		button.setTextColor(Color.DKGRAY);
 
 		textView.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				Button button = (Button) findViewById(R.id.button1);
 
 				if (s.toString().trim().length() == 0
 						|| s.toString().equals(
 								getString(R.string.default_address))) {
 					button.setClickable(false);
 					button.setBackgroundColor(Color.GRAY);
 					button.setTextColor(Color.DKGRAY);
 				} else {
 					button.setClickable(true);
 					button.setBackgroundColor(Color.BLACK);
 					button.setTextColor(Color.WHITE);
 				}
 			}
 		});
 
 		manager = new JSONBarManager(this);
 
 	}
 
 	@Override
 	protected void onStop() {
 		SharedPreferences settings = getSharedPreferences("BH_PREFERENCES", 0);
 
 		SharedPreferences.Editor editor = settings.edit();
 
 		editor.putBoolean("IMPROVE_DIALOG", showImproveDialog);
 
 		editor.commit();
 
 		super.onStop();
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		TextView textView = (TextView) findViewById(R.id.autoCompleteTextView1);
 		Button button = (Button) findViewById(R.id.button1);
 		button.setClickable(true);
 
 		textView.setText(savedInstanceState.getString("SEARCH"));
 
 		if (textView.getText().toString()
 				.equals(getString(R.string.default_address))) {
 			textView.setTextColor(Color.GRAY);
 			button.setBackgroundColor(Color.GRAY);
 			button.setTextColor(Color.DKGRAY);
 			button.setClickable(false);
 		} else if (textView.getText().toString()
 				.equals(getString(R.string.default_location))) {
 			textView.setTextColor(Color.BLUE);
 			button.setClickable(true);
 		} else {
 			textView.setTextColor(Color.BLACK);
 			button.setClickable(true);
 		}
 	}
 
 	private AdapterView.OnItemClickListener autoCompleteItemClickListener = new AdapterView.OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			String selection = (String) parent.getItemAtPosition(position);
 			AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
 			textView.setText(selection);
 
 			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
 					.hideSoftInputFromWindow(
 							textView.getApplicationWindowToken(), 0);
 
 		}
 	};
 
 	private AutoCompleteTextView.OnFocusChangeListener autoCompleteFocusChangeListener = new AutoCompleteTextView.OnFocusChangeListener() {
 
 		@Override
 		public void onFocusChange(View view, boolean hasFocus) {
 			TextView textView = (TextView) view;
 
 			if (hasFocus) {
 				if (textView.getText().toString()
 						.equals(getString(R.string.default_location))
 						|| textView.getText().toString()
 								.equals(getString(R.string.default_address))) {
 
 					textView.setText("");
 					textView.setTextColor(Color.BLACK);
 				}
 
 				lastSoftKeyboard = true;
 			} else {
 				if (textView.getText().toString().equals("")) {
 					textView.setText(getString(R.string.default_address));
 					textView.setTextColor(Color.GRAY);
 				} else if (textView.getText().toString()
 						.equals(getString(R.string.default_location))) {
 					textView.setTextColor(Color.BLUE);
 				}
 
 				lastSoftKeyboard = false;
 			}
 		}
 
 	};
 
 	private AdapterView.OnItemClickListener menuItemClickListener = new AdapterView.OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			if (menuAdapter.getItem(position) instanceof Item) {
 				Item item = (Item) menuAdapter.getItem(position);
 
 				if (item.getName().equals(getString(R.string.menu_item_search))) {
 					resetSearchDefaults();
 				}
 			}
 
 			menuDrawer.closeMenu();
 		}
 	};
 
 	public void onMenuToggleClick(View view) {
 		menuDrawer.toggleMenu();
 
 	}
 
 	public void onSearchRequestClick(View view) {
 
 		TextView address = (TextView) findViewById(R.id.autoCompleteTextView1);
 
 		if (address.getText().toString()
 				.equals(getString(R.string.default_address))
 				|| address.getText().toString().equals("")) {
 			address.requestFocus();
 			return;
 		}
 
 		busy = true;
 
 		if (intent != null)
 			intent = null;
 		
 		intent = new Intent(this, MapActivity.class);
 
 		geoSync = new GeocodeManager();
 		geoSync.setMainActivity(this);
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				TextView address = (TextView) findViewById(R.id.autoCompleteTextView1);
 				Location searchLocation;
 
 				lastRating = ((RatingBar) findViewById(R.id.ratingBar1))
 						.getRating();
 
 				Bundle bundle = new Bundle();
 
 				String addText = address.getText().toString();
 
 				// if search uses "my location", get current location
 				if (addText.toLowerCase(Locale.ENGLISH).equals(MY_LOCATION.toLowerCase(Locale.ENGLISH))) {
 
 					searchLocation = locationManager
 							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 					if (searchLocation == null)
 						searchLocation = locationManager
 								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 					bundle.putParcelable("EXTRA_MY_LOCATION", searchLocation);
 					bundle.putBoolean("EXTRA_SENSOR", true);
 
 					bundle.putFloat("EXTRA_RATING", lastRating);
 
 					intent.putExtra("BUNDLE_EXTRA", bundle);
 					intent.putExtra("FROM_MAIN_ACTIVITY", true);
 
 					geoSyncing = false;
 
 					usingLocation = true;
 
 					busy = false;
 
 					startFromMap = true;
 
 				} else {
 
 					busy = false;
 
 					geoSync.reset();
 
 					// activate geosync, wait for it to finish, tell it to call
 					// the stuff below
 					geoSync.setURL(createGoogleGeoURL(addText));
 
 					geoSync.execute();
 
 					geoSyncing = true;
 
 					changingActivity = true;
 
 					runOnUiThread(updateRunnable);
 				}
 			}
 		}).start();
 
 		while (busy)
 			runOnUiThread(updateRunnable);
 
 		if (usingLocation) {
 			startActivity(intent);
 		}
 	}
 
 	public void onCurrentLocationClick(View view) {
 
 		if (showImproveDialog) {
 			final LocationManager locationManager = (LocationManager) this
 					.getSystemService(Context.LOCATION_SERVICE);
 			final WifiManager wifiManager = (WifiManager) this
 					.getSystemService(Context.WIFI_SERVICE);
 
 			if (!locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER)
 					&& !wifiManager.isWifiEnabled()) {
 				// TODO pop up modal message about "improving accuracy"
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setNeutralButton("Settings",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 								
 								startActivity(settingsIntent);
 
 							}
 						})
 						.setNegativeButton("Skip",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO
 										// Do nothing
 									}
 								}).setTitle(R.string.dialog_accuracy_title)
 						.setMessage(R.string.dialog_accuracy_description);
 
 				AlertDialog dialog = builder.create();
 
 				dialog.show();
 			}
 
 		}
 
 		TextView address = (TextView) findViewById(R.id.autoCompleteTextView1);
 
 		address.setText(MY_LOCATION);
 		address.setTextColor(Color.BLUE);
 		address.clearFocus();
 
 		Button button = (Button) findViewById(R.id.button1);
 		button.setClickable(true);
 		button.setBackgroundColor(Color.BLACK);
 
 		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
 				.hideSoftInputFromWindow(address.getApplicationWindowToken(), 0);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		// getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private void resetSearchDefaults() {
 		TextView search = ((TextView) findViewById(R.id.autoCompleteTextView1));
 		search.setText(defaultSearch);
 		search.setTextColor(Color.GRAY);
 
 		Button button = (Button) findViewById(R.id.button1);
 		button.setClickable(false);
 
 		RatingBar bar = ((RatingBar) findViewById(R.id.ratingBar1));
 		bar.setRating(defaultRating);
 
 		geoSync = null;
 		geoSyncing = false;
 		busy = false;
 
 		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
 				.hideSoftInputFromWindow(
 						findViewById(R.id.autoCompleteTextView1)
 								.getApplicationWindowToken(), 0);
 
 		runOnUiThread(updateRunnable);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// register locationListener, using wifi or mobile networking for
 		// location updates
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 
 		// register locationListener, using the GPS for location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, locationListener);
 
 		TextView address = (TextView) findViewById(R.id.autoCompleteTextView1);
 
 		address.clearFocus();
 		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
 				.hideSoftInputFromWindow(address.getApplicationWindowToken(), 0);
 
 		if (updateRunnable == null)
 			updateRunnable = new Runnable() {
 
 				@Override
 				public void run() {
 					updateView();
 				}
 			};
 
 		runOnUiThread(updateRunnable);
 
 		Object lastGeoSync = getLastCustomNonConfigurationInstance();
 		if (lastGeoSync != null) {
 			geoSync = ((GeocodeManager) lastGeoSync);
 			geoSync.setMainActivity(this);
 
 			if (geoSync.isBusy())
 				geoSyncing = true;
 
 			busy = false;
 		} else {
 			geoSync = new GeocodeManager();
 			geoSync.setMainActivity(this);
 			geoSyncing = false;
 			busy = false;
 			changingActivity = false;
 		}
 
 		runOnUiThread(updateRunnable);
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// TODO: Check for an available network. if no network, ask user to turn
 		// it on
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// pause any GPS-running in the background
 		locationManager.removeUpdates(locationListener);
 	}
 
 	public String createGoogleURL(String search) {
 		String URL, input, key;
 
 		URL = getString(R.string.root_autocomplete_url);
 
 		key = "";
 		input = search;
 		boolean sensor = false;
 
 		ApplicationInfo appInfo;
 		try {
 			appInfo = getPackageManager().getApplicationInfo(getPackageName(),
 					PackageManager.GET_META_DATA);
 			Bundle metaBundle = appInfo.metaData;
 			key = metaBundle
 					.getString(getString(R.string.google_api_version_name));
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		URL += "input=" + Uri.encode(input);
 		URL += "&key=" + Uri.encode(key);
 		URL += "&sensor=" + Uri.encode(Boolean.toString(sensor));
 
 		return URL;
 	}
 
 	public String createGoogleGeoURL(String address) {
 		String URL;
 
 		URL = getString(R.string.root_geo_url);
 
 		URL += "address=" + Uri.encode(address);
 		URL += "&sensor=" + Uri.encode("false");
 
 		return URL;
 	}
 
 	public ArrayList<String> downloadAutoCompleteList(String httpURL) {
 		return manager.downloadAndGetAutoCompleteList(httpURL);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		try {
 			TextView searchBar = (TextView) findViewById(R.id.autoCompleteTextView1);
 			savedInstanceState.putString("SEARCH", searchBar.getText()
 					.toString());
 			savedInstanceState.putBoolean("KEYBOARD", lastSoftKeyboard);
 		} catch (Exception e) {
 			Log.e("onSaveInstance", "something went wrong with putString");
 		}
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	public void onGeoSyncCompleted(LatLng coordinates) {
 		if (coordinates != null) {
 			lastRating = ((RatingBar) findViewById(R.id.ratingBar1))
 					.getRating();
 
 			Bundle bundle = new Bundle();
 
 			Location addressLocation = new Location(
 					LocationManager.NETWORK_PROVIDER);
 			LatLng addLatLng = coordinates;
 			addressLocation.setLatitude(addLatLng.latitude);
 			addressLocation.setLongitude(addLatLng.longitude);
 
 			bundle.putParcelable("EXTRA_MY_LOCATION", addressLocation);
 			bundle.putBoolean("EXTRA_SENSOR", false);
 
 			bundle.putFloat("EXTRA_RATING", lastRating);
 
 			if (intent != null)
 				intent = null;
 
 			intent = new Intent(this, MapActivity.class);
 
 			intent.putExtra("BUNDLE_EXTRA", bundle);
 			intent.putExtra("FROM_MAIN_ACTIVITY", true);
 
 			geoSyncing = false;
 
 			changingActivity = true;
 
 			runOnUiThread(updateRunnable);
 
 			startFromMap = true;
 
 			startActivity(intent);
 		} else {
 			runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					Toast.makeText(activity,
 							"Could not find location. Try another one.",
 							Toast.LENGTH_SHORT).show();
 				}
 			});
 
 			geoSyncing = false;
 			startFromMap = false;
 			loadingDialogVisible = false;
 			changingActivity = false;
 
 			runOnUiThread(updateRunnable);
 
 		}
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 
 		if (startFromMap) {
 			startFromMap = false;
 
 			resetSearchDefaults();
 		}
 	}
 
 	@Override
 	public Object onRetainCustomNonConfigurationInstance() {
 		if (geoSyncing)
 			geoSync.setMainActivity(null);
 		else
 			geoSync = null;
 
 		return geoSync;
 	}
 
 	public void updateView() {
 
 		if (geoSyncing || busy) {
 			if (!loadingDialogVisible) {
 				loadingDialogVisible = true;
 				dialog.show(getSupportFragmentManager(), "loading_main");
 			}
 		} else {
 			if (!changingActivity)
 				if (loadingDialogVisible) {
 					loadingDialogVisible = false;
 					dialog.dismissAllowingStateLoss();
 				}
 
 		}
 	}
 	
 }
