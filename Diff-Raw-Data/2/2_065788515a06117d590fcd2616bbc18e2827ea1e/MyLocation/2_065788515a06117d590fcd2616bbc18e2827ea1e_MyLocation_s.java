 package com.ese2013.mensaunibe.model.api;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Log;
 
 import com.ese2013.mensaunibe.App;
 import com.ese2013.mensaunibe.BaseMapActivity;
 import com.ese2013.mensaunibe.MensaActivity;
 import com.ese2013.mensaunibe.MensaListAdapter;
 import com.ese2013.mensaunibe.R;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 
 /**
  * @author group7
  * @author Sandor Torok
  */
 
 public class MyLocation implements 
 LocationListener,
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener {
 
 	private static final String TAG = "MyLocation";
 	// A request to connect to Location Services
 	private static LocationRequest mLocationRequest;
 
 	// Stores the current instantiation of the location client in this object
 	private static LocationClient mLocationClient;
 
 	private static ActionBarActivity mFragActivity;
 
 	boolean mUpdatesRequested = false;
 
 	private MensaListAdapter mMensaListAdapter;
     private static MyLocation mySingelton = null;
     
     private MyLocation(){
 
     	// Create a new global location parameters object
     	mLocationRequest = LocationRequest.create();
     	
 		/*
 		 * Set the update interval
 		 */
 		mLocationRequest.setInterval(AppUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
 
 		// Use high accuracy
 		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 
 		// Set the interval ceiling to one minute
 		mLocationRequest.setFastestInterval(AppUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
 
 		// Turn on location updates
 		mUpdatesRequested = true;
 
 		/*
 		 * Create a new location client, using the enclosing class to
 		 * handle callbacks.
 		 */
 		mLocationClient = new LocationClient(App.getAppContext(), this, this);
 		
     }
  
 	public static  MyLocation getInstance(){
 		if (mySingelton == null)
             mySingelton = new MyLocation();
 		 return mySingelton;
 	}
 
 	/*
 	 * Called when the Activity is no longer visible at all.
 	 * Stop updates and disconnect.
 	 */
 	public void callOnStop() {
 		// If the client is connected
 		if (mLocationClient.isConnected()) {
 			stopPeriodicUpdates();
 		}
 		// After disconnect() is called, the client is considered "dead".
 		mLocationClient.disconnect();
 	}
 	
 	/*
 	 * Called when the Activity is going into the background.
 	 * Parts of the UI may be visible, but the Activity is inactive.
 	 */
 	public void callOnPause() {
 		if (mMensaListAdapter != null) mMensaListAdapter.locationReady(false);
 		if(mFragActivity instanceof BaseMapActivity)((BaseMapActivity) mFragActivity).locationReady(false);
 	}
 
 	/*
 	 * Called when the Activity is restarted, even before it becomes visible.
 	 */
 	public void callOnStart() {
 		/*
 		 * Connect the client. Don't re-start any requests here;
 		 * instead, wait for onResume()
 		 */
 		mLocationClient.connect();
 	}
 	/*
 	 * Called when the system detects that this Activity is now visible.
 	 */
 	public void callOnResume() {
 		mUpdatesRequested = true;
 	}
 
 	/*
 	 * Handle results returned to this Activity by other Activities started with
 	 * startActivityForResult(). In particular, the method onConnectionFailed() in
 	 * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
 	 * start an Activity that handles Google Play services problems. The result of this
 	 * call returns here, to onActivityResult.
 	 */
 	public void callOnActivityResult(int requestCode, int resultCode, Intent intent) {
 
 		// Choose what to do based on the request code
 		switch (requestCode) {
 
 		// If the request code matches the code sent in onConnectionFailed
 		case AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
 
 			switch (resultCode) {
 			// If Google Play services resolved the problem
 			case Activity.RESULT_OK:
 
 				// Log the result
 				Log.d(TAG, App.getAppContext().getString(R.string.resolved));
 				break;
 
 				// If any other result was returned by Google Play services
 			default:
 				// Log the result
 				Log.d(TAG, App.getAppContext().getString(R.string.no_resolution));
 				break;
 			}
 
 			// If any other request code was received
 		default:
 			// Report that this Activity received an unknown requestCode
 			Log.d(TAG,
 					App.getAppContext().getString(R.string.unknown_activity_request_code, requestCode));
 			break;
 		}
 	}
 
 	/**
 	 * Verify that Google Play services is available before making a request.
 	 *
 	 * @return true if Google Play services is available, otherwise false
 	 */
 	private boolean servicesConnected() {
 
 		// Check that Google Play services is available
 		int resultCode =
 				GooglePlayServicesUtil.isGooglePlayServicesAvailable(App.getAppContext());
 
 		// If Google Play services is available
 		if (ConnectionResult.SUCCESS == resultCode) {
 			// In debug mode, log the status
 			Log.d(TAG, App.getAppContext().getString(R.string.play_services_available));
 
 			// Continue
 			return true;
 			// Google Play services was not available for some reason
 		} else {
 			// Display an error dialog
 			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, mFragActivity, 0);
 			if (dialog != null) {
 				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 				errorFragment.setDialog(dialog);
 				errorFragment.show(mFragActivity.getSupportFragmentManager(), AppUtils.APPTAG);
 			}
 			return false;
 		}
 	}
 
 	public Location getLocation() {
 		// If Google Play Services is available
 		if (servicesConnected()) {
 			// Get the current location
 			Location currentLocation = mLocationClient.getLastLocation();
 			return currentLocation;
 		}
 		return null;
 	}
 
 	/*
 	 * Called by Location Services when the request to connect the
 	 * client finishes successfully. At this point, you can
 	 * request the current location or start periodic updates
 	 */
 	@Override
 	public void onConnected(Bundle bundle) {
 		Log.d(TAG, App.getAppContext().getString(R.string.connected));
 		if (mUpdatesRequested) {
 			startPeriodicUpdates();
 		}
 		if (mMensaListAdapter != null){
 			mMensaListAdapter.locationReady(true);
 			mMensaListAdapter.notifyDataSetChanged();
 			mMensaListAdapter.locationReady(false);
 		}
 		/*if(mFragActivity instanceof MapActivity){
 			((MapActivity) mFragActivity).locationReady(true);
 			((MapActivity) mFragActivity).notifyDataSetChanged();
 			((MapActivity) mFragActivity).locationReady(false);
 		}*/
 	}
 
 	/*
 	 * Called by Location Services if the connection to the
 	 * location client drops because of an error.
 	 */
 	@Override
 	public void onDisconnected() {
 		Log.d(TAG, App.getAppContext().getString(R.string.disconnected));
 		if (mMensaListAdapter != null) {mMensaListAdapter.locationReady(false);}
		if(mFragActivity instanceof BaseMapActivity){mMensaListAdapter.locationReady(false);}
 	}
 
 
 	/*
 	 * Called by Location Services if the attempt to
 	 * Location Services fails.
 	 */
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 
 		/*
 		 * Google Play services can resolve some errors it detects.
 		 * If the error has a resolution, try sending an Intent to
 		 * start a Google Play services activity that can resolve
 		 * error.
 		 */
 		if (connectionResult.hasResolution()) {
 			try {
 
 				// Start an Activity that tries to resolve the error
 				connectionResult.startResolutionForResult(
 						mFragActivity,
 						AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
 				/*
 				 * Thrown if Google Play services canceled the original
 				 * PendingIntent
 				 */
 
 			} catch (IntentSender.SendIntentException e) {
 
 				// Log the error
 				e.printStackTrace();
 			}
 		} else {
 
 			// If no resolution is available, display a dialog to the user with the error.
 			showErrorDialog(connectionResult.getErrorCode());
 		}
 	}
 
 	/**
 	 * Report location updates to the UI.
 	 *
 	 * @param location The updated location.
 	 */
 	@Override
 	public void onLocationChanged(Location location) {
 
 		Log.d(TAG, App.getAppContext().getString(R.string.location_updated));
 
 		if (mMensaListAdapter != null){
 			mMensaListAdapter.locationReady(true);
 			mMensaListAdapter.notifyDataSetChanged();
 		}
 	}
 
 	/**
 	 * In response to a request to start updates, send a request
 	 * to Location Services
 	 */
 	private void startPeriodicUpdates() {
 		mLocationClient.requestLocationUpdates(mLocationRequest, this);
 	}
 
 	/**
 	 * In response to a request to stop updates, send a request to
 	 * Location Services
 	 */
 	private void stopPeriodicUpdates() {
 		mLocationClient.removeLocationUpdates(this);
 	}
 
 	/**
 	 * Show a dialog returned by Google Play services for the
 	 * connection error code
 	 *
 	 * @param errorCode An error code returned from onConnectionFailed
 	 */
 	private void showErrorDialog(int errorCode) {
 
 		// Get the error dialog from Google Play services
 		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 				errorCode,
 				mFragActivity,
 				AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
 		// If Google Play services can provide an error dialog
 		if (errorDialog != null) {
 
 			// Create a new DialogFragment in which to show the error dialog
 			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 
 			// Set the dialog in the DialogFragment
 			errorFragment.setDialog(errorDialog);
 
 			// Show the error dialog in the DialogFragment
 			errorFragment.show(mFragActivity.getSupportFragmentManager(), AppUtils.APPTAG);
 		}
 	}
 
 	/**
 	 * Define a DialogFragment to display the error dialog generated in
 	 * showErrorDialog.
 	 */
 	public static class ErrorDialogFragment extends DialogFragment {
 
 		// Global field to contain the error dialog
 		private Dialog mDialog;
 
 		/**
 		 * Default constructor. Sets the dialog field to null
 		 */
 		public ErrorDialogFragment() {
 			super();
 			mDialog = null;
 		}
 
 		/**
 		 * Set the dialog to display
 		 *
 		 * @param dialog An error dialog
 		 */
 		public void setDialog(Dialog dialog) {
 			mDialog = dialog;
 		}
 
 		/*
 		 * This method must return a Dialog to the DialogFragment.
 		 */
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			return mDialog;
 		}
 	}
 
 	public void setAdapter(MensaListAdapter mensaListAdapter) {
 		mMensaListAdapter = mensaListAdapter;
 		
 	}
 
 	public void setActivity(MensaActivity mensaActivity) {
 		mFragActivity = mensaActivity;
 		
 	}
 	public void setActivity(BaseMapActivity mapActivity){
 		mFragActivity = mapActivity;
 	}
 }
