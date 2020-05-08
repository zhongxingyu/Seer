 package org.routy;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Locale;
 
 import org.routy.exception.AmbiguousAddressException;
 import org.routy.exception.NoLocationProviderException;
 import org.routy.exception.RoutyException;
 import org.routy.fragment.LoadingDialog;
 import org.routy.fragment.OneButtonDialog;
 import org.routy.fragment.TwoButtonDialog;
 import org.routy.model.AppProperties;
 import org.routy.service.AddressService;
 import org.routy.service.LocationService;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class OriginActivity extends FragmentActivity {
 
 	private static final String TAG = "OriginActivity";
 	private static final int ENABLE_GPS_REQUEST = 1;
 
 	private FragmentActivity context;
 
 	private LocationService locationService;
 	private AddressService addressService;
 
 	private LocationManager locationManager;
 	private EditText originAddressField;
 	private Button findUserButton;
 	private boolean locating;
 
 	// shared prefs for origin persistence
 	private SharedPreferences originActivityPrefs;
 
 	private SoundPool sounds;
 	private int bad;
 	private int speak;
 	private int click;
   AudioManager audioManager;
   float volume;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
     volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
 		speak = sounds.load(this, R.raw.routyspeak, 1);  
 
 		bad = sounds.load(this, R.raw.routybad, 1);
 		click = sounds.load(this, R.raw.routyclick, 1);
 
 		setContentView(R.layout.activity_origin);
 		
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
     volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(speak, volume, volume, 1, 0, 1);
 
 		// Initializations
 		context 			= this;
 		originAddressField 	= (EditText) findViewById(R.id.origin_address_field);
 		findUserButton 		= (Button) findViewById(R.id.find_user_button);
 		resetLocateButton();
 		locationManager 	= (LocationManager) getSystemService(LOCATION_SERVICE);
 		addressService 		= new AddressService(new Geocoder(this, Locale.getDefault()), false);		// TODO make getting sensor true/false dynamic
 		locationService 	= initLocationService();
 		originActivityPrefs = getSharedPreferences("origin_prefs", MODE_PRIVATE);
 
 		// Get persisted origin from shared prefs
 		String storedOrigin = originActivityPrefs.getString("saved_origin_string", null);
 
 		// If there wasn't a stored origin, set the hint text
 		if (null == storedOrigin){
 			originAddressField.setHint(R.string.origin_hint);
 			// Else set the EditText
 		} else {
 			originAddressField.setText(storedOrigin);
 		}
 	}
 
 
 	/**
 	 * Initializes an instance of {@link LocationService} that can be used to obtain the user's current location.
 	 * @return
 	 */
 	LocationService initLocationService() {
 		return new LocationService(locationManager, AppProperties.LOCATION_ACCURACY_THRESHOLD_M) {
 
 			@Override
 			public void onLocationResult(Location location) {
 				// Reverse geocode the location into an address and populate the TextEdit
 				Date locationUpdated = new Date();
 				locationUpdated.setTime(location.getTime());
 				Log.v(TAG, "Location: " + 
 						"\nLat: " + location.getLatitude() + 
 						"\nLong: " + location.getLongitude() + 
 						"\nProvider: " + location.getProvider() + 
 						"\nAccuracy: " + location.getAccuracy() +
 						"\nTime: " + locationUpdated);
 
 				Address address = null;
 
 				try {
 					address = addressService.getAddressForLocation(location);
 				} catch (AmbiguousAddressException e) {
 					if (e.getAddresses().size() > 0) {
 						address = e.getFirstAddress();
 					}
 				} catch (RoutyException e) {
 					// Display an error to the user...it was already logged
 					Log.e(TAG, "Error reverse geocoding user's location.");
 					showErrorDialog(getResources().getString(R.string.default_error_message));
 				} catch (IOException e) {
 					// TODO Check if they have internet service.  If they don't, tell them.  If they do, show default error message.
 					showErrorDialog("TEMPORARY MSG: Either you don't have and internet connection, or something internally went wrong.");
 				}
 
 				if (address != null) {
 					StringBuilder addressStr = new StringBuilder();
 
 					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
 						addressStr.append(address.getAddressLine(i));
 						addressStr.append(", ");
 					}
 					addressStr.append(address.getAddressLine(address.getMaxAddressLineIndex()));
 
 					Log.v(TAG, "Address: " + addressStr.toString());
 					originAddressField.setText(addressStr.toString());		// Sets the current location (obtained from sensors) in the EditText so we can validate when "Done" is clicked
 				} else {
 					Log.e(TAG, "Couldn't reverse geocode the address.");
 					showErrorDialog(getString(R.string.locating_fail_error));
 				}
 				resetLocateButton();
 			}
 
 
 			@Override
 			public void onLocationSearchTimeout() {
 				Log.e(TAG, "Getting user location timed out.");
 				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 					showErrorDialog(getResources().getString(R.string.locating_fail_error));
 					resetLocateButton();
 				} else {
 					Log.e(TAG, "GPS was not enabled...going to ask the user if they want to enable it.");
 					showEnableGpsDialog(getResources().getString(R.string.enable_gps_prompt));
 				}
 
 			}
 		};
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ENABLE_GPS_REQUEST:
 			try {
 				locationService.getCurrentLocation();
 			} catch (NoLocationProviderException e) {
 				showErrorDialog(getResources().getString(R.string.no_location_provider_error));
 				resetLocateButton();
 			}
 			break;
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_origin, menu);
 		return true;
 	}
 
 
 	private void resetLocateButton() {
 		findUserButton.setText(R.string.find_user_prompt);
 		locating = false;
 	}
 
 
 	/**
 	 * Called when the locate button is tapped.
 	 * 
 	 * @param view
 	 */
 	public void findUserLocation(View view) {
 		Log.v(TAG, "locating user");
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
     volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);  
 
 		if (!locating) {
 			findUserButton.setText(R.string.stop_locating);
 			//showLoadingDialog();
 			try {
 				locating = true;
 				locationService.getCurrentLocation();
 			} catch (NoLocationProviderException e) {
 				Log.e(TAG, e.getMessage());
 				showEnableGpsDialog(getResources().getString(R.string.enable_gps_prompt));
 			}
 		} else {
 			locationService.stop();
 			resetLocateButton();
 		}
 	}
 
 
 	/**
 	 * Validates the origin address.  If it's good, it gets packaged into an Intent and sent to 
 	 * the DestinationActivity screen.
 	 * 
 	 * @param view
 	 */
 	public void goToDestinationsScreen(View view) {
 		// validate the origin address, store it, and move on to the destinations screen
 		Log.v(TAG, "Origin entered: " + originAddressField.getText());
 
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
     volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);  
 
 		if (originAddressField.getText() == null || originAddressField.getText().length() == 0) {
 			showErrorDialog(getResources().getString(R.string.no_origin_address_error));
 		} else {
 			// Validate the given address string
 			Address originAddress = null;
 			try {
 				originAddress = addressService.getAddressForLocationString(originAddressField.getText().toString());
 			} catch (AmbiguousAddressException e) {
 				Log.d(TAG, "Got more than one result for the given origin address.  We'll use the first one.");
 				originAddress = e.getFirstAddress();
 			} catch (RoutyException e) {
 				// Display an error to the user...it was already logged
 				Log.e(TAG, "Error getting an Address object for origin address.");
 				showErrorDialog(getResources().getString(R.string.default_error_message));
 			} catch (IOException e) {
 				// TODO Check if they have internet service.  If they don't, tell them.  If they do, show default error message.
 
 				showErrorDialog("TEMPORARY MSG: Either you don't have and internet connection, or something internally went wrong.");
 			}
 
 			if (originAddress != null) {
 				Log.v(TAG, "Validated origin address: " + originAddress.getFeatureName());
 
 				saveOriginInSharedPrefs();
 
 				// Origin address is good...move on to Destinations
 				Intent destinationIntent = new Intent(getBaseContext(), DestinationActivity.class);
 				destinationIntent.putExtra("origin", originAddress);	// Android Address is Parcelable, so no need for Bundle
 				startActivity(destinationIntent);
 			} else {
 				//    			Toast.makeText(this, getString(R.string.origin_failed_validate), Toast.LENGTH_LONG).show();	// XXX temp
 				showErrorDialog(getResources().getString(R.string.bad_origin_address_error));
 			}
 		}
 	}
 
 	/**
 	 *  Saves the validated origin in shared preferences, saves user time when using Routy next.
 	 */
 	private void saveOriginInSharedPrefs() {
 		SharedPreferences.Editor ed = originActivityPrefs.edit();
 		ed.putString("saved_origin_string", originAddressField.getText().toString());
 		ed.commit();	
 	}
 
 
 	/**
 	 * Displays a {@link LoadingDialog}.  Use this to entertain the user while we find their location.
 	 * 
 	 */
 	private void showLoadingDialog() {
 		LoadingDialog dialog = new LoadingDialog(getResources().getString(R.string.default_loading_message));
 		dialog.show(context.getSupportFragmentManager(), TAG);
 	}
 
 
 	/**
 	 * Displays an {@link AlertDialog} with one button that dismisses the dialog.  Use this to display error messages 
 	 * to the user.
 	 * 
 	 * @param message
 	 */
 	private void showErrorDialog(String message) {
 		OneButtonDialog dialog = new OneButtonDialog(getResources().getString(R.string.error_message_title), message) {
 			@Override
 			public void onButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		};
 		dialog.show(context.getSupportFragmentManager(), TAG);
 	}
 
 
 	/**
 	 * Displays an alert asking the user if they would like to go to the device's Location Settings to enable GPS.
 	 * 
 	 * @param message
 	 */
 	private void showEnableGpsDialog(String message) {
 		TwoButtonDialog dialog = new TwoButtonDialog(getResources().getString(R.string.error_message_title), message) {
 
 			@Override
 			public void onRightButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 
 				// Show the "Location Services" settings page
 				Intent gpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				Log.v(TAG, "ENABLE_GPS_REQUEST = " + ENABLE_GPS_REQUEST);
 				context.startActivityForResult(gpsIntent, ENABLE_GPS_REQUEST);
 
 			}
 
 			@Override
 			public void onLeftButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		};
 		dialog.show(context.getSupportFragmentManager(), TAG);
 	}
 
 	@Override
 	protected void onResume() {   
 		super.onResume(); 
 
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
 
 		speak = sounds.load(this, R.raw.routyspeak, 1);  
 
 		bad = sounds.load(this, R.raw.routybad, 1);
 		click = sounds.load(this, R.raw.routyclick, 1);
 	}
 
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		if(sounds != null) { 
 			sounds.release(); 
 			sounds = null; 
 		} 
 
 		locationService.stop();
 	}
 }
