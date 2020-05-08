 /*******************************************************************************
  * This file is part of Jane.
  * 
  *     Jane is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     Jane is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with Jane.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *     Copyright (c) 2011 Vasile Jureschi <vasile.jureschi@gmail.com>.
  *     All rights reserved. This program and the accompanying materials
  *     are made available under the terms of the GNU Public License v3.0
  *     which accompanies this distribution, and is available at
  *     
  *    http://www.gnu.org/licenses/gpl-3.0.html
  * 
  *     Contributors:
  *        Vasile Jureschi <vasile.jureschi@gmail.com> - initial API and implementation
  ******************************************************************************/
 
 package org.unchiujar.jane.activities;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 
 import org.unchiujar.jane.R;
 import org.unchiujar.jane.location.Waypoint;
 import org.unchiujar.jane.location.WaypointManager;
 import org.unchiujar.jane.location.WaypointManager.MarkerMessage;
 import org.unchiujar.jane.location.WaypointManager.MarkerMessage.State;
 import org.unchiujar.jane.services.LocationService;
 import org.unchiujar.jane.utils.LocationUtilities;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.WindowManager.LayoutParams;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 /**
  * Main activity for Jane application.
  * 
  * @author Vasile Jureschi
  * @see LocationService
  */
 public class WaypointMap extends FragmentActivity implements Observer {
 	/** Logger tag. */
 	private static final String TAG = WaypointMap.class.getName();
 	/** Initial map zoom. */
 	private static final int INITIAL_ZOOM = 17;
 	/** Interval between zoom checks for the zoom and pan handler. */
 	public static final int ZOOM_CHECKING_DELAY = 500;
 	/** Constant used for saving the accuracy value between screen rotations. */
 	private static final String BUNDLE_ACCURACY = "org.unchiujar.jane.accuracy";
 	/** Constant used for saving the latitude value between screen rotations. */
 	private static final String BUNDLE_LATITUDE = "org.unchiujar.jane.latitude";
 	/** Constant used for saving the longitude value between screen rotations. */
 	private static final String BUNDLE_LONGITUDE = "org.unchiujar.jane.longitude";
 	/** Constant used for saving the zoom level between screen rotations. */
 	private static final String BUNDLE_ZOOM = "org.unchiujar.jane.zoom";
 
 	/**
 	 * Intent named used for starting the location service
 	 * 
 	 * @see LocationService
 	 */
 	private static final String SERVICE_INTENT_NAME = "org.com.unchiujar.LocationService";
 
 	/** Dialog displayed while loading the explored points at application start. */
 	private ProgressDialog mloadProgress;
 
 	/**
 	 * Location service intent.
 	 * 
 	 * @see LocationService
 	 */
 	private Intent mLocationServiceIntent;
 
 	/** Current device latitude. Updated on every location change. */
 	private double mCurrentLat;
 	/** Current device longitude. Updated on every location change. */
 	private double mCurrentLong;
 	/** Current location accuracy . Updated on every location change. */
 	private double mCurrentAccuracy;
 
 	/**
 	 * Flag signaling if the application is visible. Used to stop overlay
 	 * updates if the map is currently not visible.
 	 */
 	private boolean mVisible = true;
 	/**
 	 * Flag signaling if the user is walking or driving. It is passed to the
 	 * location service in order to change location update frequency.
 	 * 
 	 * @see LocationService
 	 */
 	private boolean mDrive;
 
 	/** Messenger for communicating with service. */
 	private Messenger mService = null;
 	/** Flag indicating whether we have called bind on the service. */
 	private boolean mIsBound;
 
 	/** Target we publish for clients to send messages to IncomingHandler. */
 	private final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	private SharedPreferences mSettings;
 	private SupportMapFragment mMapFragment;
 
 	/**
 	 * Handler of incoming messages from service.
 	 */
 	private class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case LocationService.MSG_LOCATION_CHANGED:
 				if (msg.obj != null) {
 					Log.d(TAG, ((Location) msg.obj).toString());
 
 					mCurrentLat = ((Location) msg.obj).getLatitude();
 					mCurrentLong = ((Location) msg.obj).getLongitude();
 					mCurrentAccuracy = ((Location) msg.obj).getAccuracy();
 					// TODO redraw the overlay ???
 					// redrawOverlay();
 
 				} else {
 					Log.d(TAG, "Null object received");
 				}
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	/**
 	 * Class for interacting with the main interface of the service.
 	 */
 	private ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mService = new Messenger(service);
 			Log.d(TAG, "Location service attached.");
 			// register client
 			sendMessage(LocationService.MSG_REGISTER_CLIENT);
 			// register interface
 			sendMessage(LocationService.MSG_REGISTER_INTERFACE);
 
 			// send walk or drive mode
 			sendMessage(mDrive ? LocationService.MSG_DRIVE
 					: LocationService.MSG_WALK);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * android.content.ServiceConnection#onServiceDisconnected(android.content
 		 * .ComponentName)
 		 */
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			// Called when the connection with the service has been
 			// unexpectedly disconnected / process crashed.
 			mService = null;
 			Log.d(TAG, "Disconnected from location service");
 		}
 	};
 
 	/**
 	 * Drive or walk preference listener. A listener is necessary for this
 	 * option as the location service needs to be notified of the change in
 	 * order to change location update frequency. The preference is sent when
 	 * the activity comes into view and rebinds to the location service.
 	 */
 	private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 
 		@Override
 		public void onSharedPreferenceChanged(
 				SharedPreferences sharedPreferences, String key) {
 			Log.d(TAG, "Settings changed :" + sharedPreferences + " " + key);
 			mDrive = mSettings.getBoolean(Preferences.DRIVE_MODE, false);
 		}
 	};
 	private WaypointManager waypointManager;
 	private GoogleMap mMap;
 
 	// ==================== LIFECYCLE METHODS ====================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
 		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
 		mloadProgress = ProgressDialog.show(this, "",
 				"Loading. Please wait...", true);
 		mSettings.registerOnSharedPreferenceChangeListener(mPrefListener);
 		setContentView(R.layout.main);
 
 		// get map handle
 
 		mMap = ((SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.mapview)).getMap();
 
 		waypointManager = new WaypointManager();
 
 		waypointManager.addObserver(this);
 		// TODO set initial/remembered map zoom
 
 		Marker marker = mMap.addMarker(new MarkerOptions()
 				.position(new LatLng(37.7750, 122.4183)).title("San Francisco")
 				.snippet("Population: 776733"));
 		Log.d(TAG, "onCreate completed: Activity created");
 		mLocationServiceIntent = new Intent(SERVICE_INTENT_NAME);
 		startService(mLocationServiceIntent);
 		loadFileFromIntent();
 		// check we still have access to GPS info
 		checkConnectivity();
 
 	}
 
 	/**
 	 * Loads a gpx data from a file path send through an intent.
 	 */
 	private void loadFileFromIntent() {
 		Intent intent = getIntent();
 		if (intent != null) {
 
 			Uri data = intent.getData();
 
 			if (data != null) {
 
 				final String filePath = data.getEncodedPath();
 
 				final ProgressDialog progress = new ProgressDialog(this);
 
 				progress.setCancelable(false);
 				progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 				progress.setMessage(getString(R.string.importing_locations));
 				progress.show();
 
 				Runnable importer = new Runnable() {
 
 					@Override
 					public void run() {
 						progress.dismiss();
 						// TODO add file import support
 						throw new UnsupportedOperationException(
 								"file import not supported");
 					}
 				};
 				new Thread(importer).start();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// restore accuracy and coordinates from saved state
 		mCurrentAccuracy = savedInstanceState.getDouble(BUNDLE_ACCURACY);
 		mCurrentLat = savedInstanceState.getDouble(BUNDLE_LATITUDE);
 		mCurrentLong = savedInstanceState.getDouble(BUNDLE_LONGITUDE);
 		// TODO set zoom on load
 
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// save accuracy and coordinates
 		outState.putDouble(BUNDLE_ACCURACY, mCurrentAccuracy);
 		outState.putDouble(BUNDLE_LATITUDE, mCurrentLat);
 		outState.putDouble(BUNDLE_LONGITUDE, mCurrentLong);
 		// MapView mapView = (MapView) findViewById(R.id.mapview);
 		// TODO save zoom level on app going to background
 		// outState.putInt(BUNDLE_ZOOM, mapView.getZoomLevel());
 		super.onSaveInstanceState(outState);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "onStart completed: Activity started");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// register zoom && pan mZoomPanHandler
 		// mZoomPanHandler.postDelayed(mZoomChecker, ZOOM_CHECKING_DELAY);
 		// set the visibility flag to start overlay updates
 		mVisible = true;
 
 		// TOOD maybe redraw ?
 
 		mloadProgress.cancel();
 		Log.d(TAG, "onResume completed.");
 		// bind to location service
 		doBindService();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// mZoomPanHandler.removeCallbacks(mZoomChecker);
 		mVisible = false;
 		// unbind from service as the activity does
 		// not display location info (is hidden or stopped)
 		doUnbindService();
 		Log.d(TAG, "onPause completed.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.d(TAG, "onStop completed.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onRestart()
 	 */
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		Log.d(TAG, "onRestart completed.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		Log.d(TAG, "onDestroy completed.");
 	}
 
 	// ================= END LIFECYCLE METHODS ====================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.menu, menu);
 		menu.findItem(R.id.where_am_i).setIcon(
 				android.R.drawable.ic_menu_mylocation);
		
		menu.findItem(R.id.add_waypoint).setIcon(
				android.R.drawable.star_on);
		
 		menu.findItem(R.id.settings).setIcon(
 				android.R.drawable.ic_menu_preferences);
 		menu.findItem(R.id.help).setIcon(android.R.drawable.ic_menu_help);
 		menu.findItem(R.id.exit).setIcon(
 				android.R.drawable.ic_menu_close_clear_cancel);
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.where_am_i:
 			Log.d(TAG,
 					"XXXXX NOT IMPLEMENTED XXXXX Moving to current location...");
 			// TODO implement moving to current location
 			// TODO redraw overlay
 			return true;
 		case R.id.help:
 			Log.d(TAG, "Showing help...");
 			Intent helpIntent = new Intent(this, Help.class);
 			startActivity(helpIntent);
 			return true;
 		case R.id.exit:
 			Log.d(TAG, "Exit requested...");
 			doUnbindService();
 			// cleanup
 			stopService(mLocationServiceIntent);
 			finish();
 			return true;
 		case R.id.settings:
 			Intent settingsIntent = new Intent(this, Preferences.class);
 			startActivity(settingsIntent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Checks GPS and network connectivity. Displays a dialog asking the user to
 	 * start the GPS if not started and also displays a toast warning it no
 	 * network connectivity is available.
 	 */
 	private void checkConnectivity() {
 
 		boolean isGPS = ((LocationManager) getSystemService(LOCATION_SERVICE))
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		if (!isGPS) {
 			createGPSDialog().show();
 		}
 		displayConnectivityWarning();
 	}
 
 	/**
 	 * Displays a toast warning if no network is available.
 	 */
 	private void displayConnectivityWarning() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		boolean connected = false;
 		for (NetworkInfo info : connectivityManager.getAllNetworkInfo()) {
 			if (info.getState() == NetworkInfo.State.CONNECTED
 					|| info.getState() == NetworkInfo.State.CONNECTING) {
 				connected = true;
 				break;
 			}
 		}
 
 		if (!connected) {
 			Toast.makeText(getApplicationContext(),
 					R.string.connectivity_warning, Toast.LENGTH_LONG).show();
 
 		}
 	}
 
 	/**
 	 * Creates the GPS dialog displayed if the GPS is not started.
 	 * 
 	 * @return the GPS Dialog
 	 */
 	private Dialog createGPSDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.gps_dialog).setCancelable(false);
 
 		final AlertDialog alert = builder.create();
 
 		alert.setButton(DialogInterface.BUTTON_POSITIVE,
 				getString(R.string.start_gps_btn),
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						alert.dismiss();
 						startActivity(new Intent(
 								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 					}
 				});
 
 		alert.setButton(DialogInterface.BUTTON_NEGATIVE,
 				getString(R.string.continue_no_gps),
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						alert.dismiss();
 					}
 				});
 		return alert;
 	}
 
 	/**
 	 * Binds to the location service. Called when the activity becomes visible.
 	 */
 	private void doBindService() {
 		bindService(mLocationServiceIntent, mConnection,
 				Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 		Log.d(TAG, "Binding to location service");
 	}
 
 	/**
 	 * Unbinds from the location service. Called when the activity is stopped or
 	 * closed.
 	 */
 	private void doUnbindService() {
 		if (mIsBound) {
 			// test if we have a valid service registration
 			if (mService != null) {
 				sendMessage(LocationService.MSG_UNREGISTER_INTERFACE);
 			}
 
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mIsBound = false;
 			Log.d(TAG, "Unbinding map from location service.");
 		}
 	}
 
 	private void sendMessage(int message) {
 		// TODO check message
 		try {
 			Message msg = Message.obtain(null, message);
 			msg.replyTo = mMessenger;
 			mService.send(msg);
 		} catch (RemoteException e) {
 			// NO-OP
 			// Nothing special to do if the service
 			// has crashed.
 		}
 
 	}
 
 	
 	private TreeMap<Integer, Marker> markers = new TreeMap<Integer, Marker>();
 
 	@Override
 	public void update(Observable observable, Object data) {
 		Log.d(TAG, "Message received" + data.toString());
 		Waypoint waypoint = ((MarkerMessage) data).getWaypoint();
 		State state = ((MarkerMessage) data).getState();
 
 		switch (state) {
 		case DELETE_ALL:
 			for (Marker marker : markers.values()) {
 				//remove from map
 				marker.remove();
 			}
 			markers.clear();
 		case ADD_MULTIPLE:
 			ArrayList<Waypoint> waypoints = waypointManager.getWaypoints();
 			// iterate through all the waypoints and if there is
 			// a waypoint that does not have its index in the markers
 			// list, create a marker for it and add it to the list
 			for (Waypoint point : waypoints) {
 
 				if (markers.get(point.getIndex()) == null)
 					markers.put(point.getIndex(),
 							createMarkerFromWaypoint(point));
 			}
 			break;
 		case ADD:
 			markers.put(waypoint.getIndex(), createMarkerFromWaypoint(waypoint));
 			break;
 		case DELETE:
 			// remove from map
 			markers.get(waypoint.getIndex()).remove();
 			// remove from list
 			markers.remove(waypoint.getIndex());
 			break;
 		case REACH:
 			// TODO change marker colour
 			Log.e(TAG, "Marker reached  colour change not implemented");
 			break;
 		default:
 			Log.e(TAG,
 					"Unknown message received from observable"
 							+ data.toString());
 			break;
 		}
 	}
 
 	private Marker createMarkerFromWaypoint(Waypoint waypoint) {
 		return mMap.addMarker(new MarkerOptions().position(
 				LocationUtilities.locationToLatLng(waypoint)).title(
 				waypoint.getInfo()));
 	}
 }
