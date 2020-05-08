 package com.brousalis;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
import com.google.android.maps.*;
 
 public class ShowMap extends MapActivity {
 	
 	private static final int HTC_SENSE_ENTER = 0;
 	// SharedPreference Strings
 	public static final String SAVED_MAP_STATE = "SavedMapState";
 	public static final String SAVED_MAP_LAT = "SavedMapLat";
 	public static final String SAVED_MAP_LONG = "SavedMapLong";
 	public static final String SAVED_MAP_ZOOM = "SavedMapZoom";
 	public static final String SAVED_DEFAULT_ZOOM = "DefaultZoom";
 	public static final String SAVED_ZOOM_ON_CENTER = "ZoomOnCenter";
 	public static final String REGISTERED_DEVICE = "RegisteredDevice";
 	public static Boolean BETA_MODE = false;
 	private static String UNIQUE_ID = "";
 
 	// Default Values
 	// DEFAULT_MAP_ZOOM moved to prefs - 9/29/10 estokes
 	// It is still needed here, see CSN01 (CSN = Code Side Note)
 	public static final int DEFAULT_MAP_ZOOM = 17;
 	// TODO: Figure out some good value to init these to
 	public static final int DEFAULT_MAP_LAT = 0;
 	public static final int DEFAULT_MAP_LONG = 0;
 	
 	public static Drawable bubble;
 	public static ShowMap thisActivity;
 
 	/**
 	 * The Standard Location manager for an Android Device
 	 */
 	private LocationManager _locMgr;
 
 	/**
 	 * The Map controller that allows us to retrieve or send information to our
 	 * _mapView
 	 */
 	private MapController _mapController;
 
 	/**
 	 * MapView which will display trails to users
 	 */
 	private MapView _mapView;
 
 	/**
 	 * The settings object where both custom settings are stored as well as
 	 * settings set in the options dialog
 	 */
 	private SharedPreferences _settings;
 
 	private DataHandler _dataHandler;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		bubble = this.getResources().getDrawable(R.drawable.dot_clear);
 		thisActivity = ShowMap.this;
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.main);
 		this._mapView = (MapView) findViewById(R.id.mapView);
 		this._mapView.setBuiltInZoomControls(true);
 
 		this._mapController = this._mapView.getController();
 
 		this._locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		BETA_MODE = Boolean.parseBoolean(this.getString(R.string.beta));
 		if(!BetaChecker.isUpToDate( BETA_MODE, this.getString(R.string.beta_check_url) + this.getString(R.string.beta_version))) {
 			showOutOfDateDialog();
 		}
 		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		UNIQUE_ID = mTelephonyMgr.getDeviceId();
 
 		this._settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		Log.w("MTM", "MTM Settings: " + this._settings.getBoolean(REGISTERED_DEVICE, false));
 		String idCheckResult = BetaChecker.checkUser( this.getString(R.string.register_device_url), UNIQUE_ID);
 		Boolean validUser = idCheckResult.equals("registered");
 		Boolean bannedUser = idCheckResult.equals("banned");
 		if (bannedUser && BETA_MODE) {
 			showBannedUserDialog();
 		}
 		// This next line is commented out for the early beta, this allows caching of items, but will negate
 		// the ability to ban people
 		// if(!this._settings.getBoolean(REGISTERED_DEVICE, false)) {
 		else if (!validUser && BETA_MODE) {
 			showNewBetaUserDialog(this.getString(R.string.register_device_url));
 		}
		// }
		Log.w("MTM", "MTM: onCreate()");
		
 	}
 
 	private void showOutOfDateDialog() {
 		final BetaDialog updateNeeded = new BetaDialog(ShowMap.this, R.layout.out_of_date);
 		TextView changelog = (TextView)updateNeeded.findViewById(R.id.changelog);
 		changelog.setText(BetaChecker.getHTTPData(this.getString(R.string.beta_user_log_url)));
 		updateNeeded.setSubmitAction(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				takeUserToNewDownload();
 				updateNeeded.setContentView(R.layout.install_version);
 				Button cancel = (Button)updateNeeded.findViewById(R.id.beta_user_cancel);
 				cancel.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						updateNeeded.cancel();
 					}});
 				updateNeeded.setOnDismissListener(new OnDismissListener() {
 					@Override
 					public void onDismiss(DialogInterface dialog) {
 						finish();
 					}
 				});
 			}
 		});
 		updateNeeded.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				finish();
 			}
 		});
 		updateNeeded.setCancelAction(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				updateNeeded.cancel();
 			}
 		});
 		updateNeeded.show();
 	}
 	
 	/**
 	 * Download the newest version from the internet, then change the view
 	 */
 	public void takeUserToNewDownload() {
 		Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(this.getString(R.string.beta_download_url) + BetaChecker.getHTTPData(this.getString(R.string.beta_user_latest_version)) + ".apk"));  
 		startActivity(viewIntent);
 	}
 	private void showBannedUserDialog() {
 		final BetaDialog bannedUser = new BetaDialog(ShowMap.this, R.layout.banned_user);
 		bannedUser.setOnDismissListener(new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				finish();
 			}
 		});
 		bannedUser.show();
 	}
 
 	private void showNewBetaUserDialog(String registerUrl) {
 		final BetaDialog newUser = new BetaDialog(ShowMap.this, R.layout.new_beta_user);
 		final String registrationUrl = registerUrl;
 		final String betaVersion = this.getString(R.string.beta_version);
 		final EditText name = (EditText) newUser.findViewById(R.id.beta_user_name);
 		final EditText network = (EditText) newUser.findViewById(R.id.beta_network);
 		final Button submitButton = (Button) newUser.findViewById(R.id.beta_user_submit);
 		newUser.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				finish();
 			}
 		});
 		newUser.setCancelAction(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				newUser.cancel();
 			}
 		});
 		
 		submitButton.setEnabled(false);
 		submitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				BetaChecker.registerUser(registrationUrl, UNIQUE_ID, name.getEditableText().toString(), android.os.Build.VERSION.RELEASE, network.getEditableText().toString(), android.os.Build.BRAND, android.os.Build.DEVICE, android.os.Build.MANUFACTURER, betaVersion);
 				registerDeviceLocally();
 				newUser.dismiss();
 			}
 		});
 		final ArrayList<EditText> textFields = new ArrayList<EditText>();
 		name.setSingleLine();
 		network.setSingleLine();
 		
 		name.setHintTextColor(Color.LTGRAY);
 		network.setHintTextColor(Color.LTGRAY);
 		
 		textFields.add(name);
 		textFields.add(network);
 		
 		name.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				areAllBetaFieldsFilledOut(textFields, submitButton);
 				return false;
 			}
 		});
 		network.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				areAllBetaFieldsFilledOut(textFields, submitButton);
 				return false;
 			}
 		});
 		network.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 	        @Override
 	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == HTC_SENSE_ENTER) {
 	            	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 	            	areAllBetaFieldsFilledOut(textFields, submitButton);
 	            	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 	                return true;
 	            }
 	            return false;
 	        }
 	    });
 		newUser.show();
 	}
 	
 	/**
 	 * Check to make sure all fields are filled out, if they are, enable the Submit Button passed in
 	 * @param fields The Fields to analyze
 	 * @param submitButton The button to enable/disable
 	 * @return Returns true if all fields are filled out, False if they are not
 	 */
 	private final boolean areAllBetaFieldsFilledOut(ArrayList<EditText> fields, Button submitButton) {
 		for(EditText field : fields) {
 			if(field.getEditableText().length() <= 0) {
 				submitButton.setEnabled(false);
 				return false;
 			}
 		}
 		submitButton.setEnabled(true);
 		return true;
 	}
 	
 	public void registerDeviceLocally() {
 		SharedPreferences.Editor editor = this._settings.edit();
 		editor.putBoolean(REGISTERED_DEVICE, true);
 		editor.commit();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		Log.w("MTM", "MTM: conFigChanged()");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.w("MTM", "MTM: onPause()");
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// Always save the location, regardless if the user wants to go here
 		SharedPreferences.Editor editor = this._settings.edit();
 		editor.putInt(SAVED_MAP_LAT, this._mapView.getMapCenter()
 				.getLatitudeE6());
 		editor.putInt(SAVED_MAP_LONG, this._mapView.getMapCenter()
 				.getLongitudeE6());
 		editor.putInt(SAVED_MAP_ZOOM, this._mapView.getZoomLevel());
 		editor.commit();
 		Log.w("MTM", "MTM: onStop()");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.w("MTM", "MTM: onDestroy()");
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// drawTrail();
 		Log.w("MTM", "MTM: onResume()");
 	}
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		Log.w("MTM", "MTM: onRestart()");
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		// Get the GPS Location if it's available, otherwise use the network
 		// Location.
 		// By doing this, we don't force users to open up the GPS on program
 		// start.
 		// We'll also load here rather than oncreate to ensure settings are
 		// loaded.
 		this.initLocation();
 		this.initializeParser();
 		this.drawTrail();
 		Log.w("MTM", "MTM: onStart()");
 	}
 	
 	/**
 	 * Initialize the XML Parser and Parse the data from the url.
 	 */
 	private void initializeParser() {
 		this._dataHandler = new DataHandler();
 		this._dataHandler.parseDocument();
 	}
 
 	/**
 	 * Resolves all connections for all trails pulled down
 	 */
 	private void drawTrail() {
 		HashSet<Trail> trails = this._dataHandler.getParsedTrails();
 		this._mapView.getOverlays().clear();
 		for (Trail t : trails) {
 			t.resolveConnections();
 			this._mapView.getOverlays().addAll(t.getTrailPoints());
 			this._mapView.getOverlays().add(t);
 			
 		}
 		this._mapView.invalidate();
 	}
 
 	/**
 	 * This method MUST be here. Google's TOS requires it. Still not sure if we
 	 * have to turn this to true if we're displaying custom trail routes...
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Fired when the options menu is created
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	/**
 	 * Fired when an options menu item is selected.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_quit:
 			this.finish();
 			break;
 		case R.id.menu_settings:
 			Intent settings = new Intent(this, TrailPrefs.class);
 			this.startActivity(settings);
 			break;
 		case R.id.menu_center:
 			this.centerMapOnCurrentLocation(_settings.getBoolean(
 					SAVED_ZOOM_ON_CENTER, true));
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * Initialize the Location. This happens when the app starts from cold or if
 	 * the user does not have save location
 	 */
 	private void initLocation() {
 		GeoPoint p;
 		this._settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 
 		if (this._settings.getBoolean(SAVED_MAP_STATE, false)) {
 			p = new GeoPoint(this._settings.getInt(SAVED_MAP_LAT,
 					DEFAULT_MAP_LAT), this._settings.getInt(SAVED_MAP_LONG,
 					DEFAULT_MAP_LONG));
 
 			this._mapController.animateTo(p);
 			// Zoom to the Saved Map zoom. If none is present, get the Saved
 			// Default zoom from the prefs.
 			// If that's not there, Use the DefaultMapZoom. That last one should
 			// NEVER happen
 			this._mapController.setZoom(this._settings.getInt(SAVED_MAP_ZOOM,
 					Integer.parseInt(this._settings.getString(
 							SAVED_DEFAULT_ZOOM,
 							Integer.toString(DEFAULT_MAP_ZOOM)))));
 			this._mapView.setSatellite(true);
 			this._mapView.invalidate();
 		} else {
 			this.centerMapOnCurrentLocation(false);
 		}
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
 		super.dispatchTouchEvent(ev);
 
 		return true;
 	};
 
 	/**
 	 * Centers the map on the current location (GPS, Cell or WiFi, whichever is
 	 * more accurate and present)
 	 * 
 	 * @param zoomOnCenter
 	 *            Should the zoom level be changed to what is defined in prefs
 	 */
 	private void centerMapOnCurrentLocation(boolean zoomOnCenter) {
 		GeoPoint p;
 		Location networkLoc = _locMgr
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		Location gpsLoc = _locMgr
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		Location recentLoc = (gpsLoc != null) ? gpsLoc : networkLoc;
 		// CSN01
 		// We can't just pull this from prefs, as this function is shared
 		// It's sometimes called by the initializer, not the "center me" button.
 		if (zoomOnCenter) {
 			this._mapController.setZoom(Integer.parseInt(_settings.getString(
 					SAVED_DEFAULT_ZOOM, DEFAULT_MAP_ZOOM + "")));
 		}
 
 		if (recentLoc != null) {
 			p = new GeoPoint((int) (recentLoc.getLatitude() * 1E6),
 					(int) (recentLoc.getLongitude() * 1E6));
 			this._mapController.animateTo(p);
 			this._mapView.setSatellite(true);
 			this._mapView.invalidate();
 		}
 	}
 }
