 package org.routy;
 
 import java.util.Locale;
 
 import org.routy.exception.GpsNotEnabledException;
 import org.routy.exception.NoLocationProviderException;
 import org.routy.fragment.OneButtonDialog;
 import org.routy.fragment.TwoButtonDialog;
 import org.routy.listener.FindUserLocationListener;
 import org.routy.model.AddressModel;
 import org.routy.model.GooglePlace;
 import org.routy.model.GooglePlacesQuery;
 import org.routy.task.FindUserLocationTask;
 import org.routy.task.GooglePlacesQueryTask;
 import org.routy.view.DestinationRowView;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Address;
 import android.media.AudioManager;
 import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class OriginActivity extends FragmentActivity {
 
 	private static final String TAG = "OriginActivity";
 	private static final int ENABLE_GPS_REQUEST = 1;
 
 	private FragmentActivity context;
 	private AddressModel addressModel;
 
 	private EditText originAddressField;
 	private Address origin;
 	private boolean originValidated;		// true if the origin was obtained using geolocation (not user entry)
 
 	// shared prefs for origin persistence
 	private SharedPreferences originActivityPrefs;
 
 	private SoundPool sounds;
 	private int bad;
 	private int speak;
 	private int click;
 	private AudioManager audioManager;
 	private float volume;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_origin);
 		
 		// Audio stuff
 		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
 		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
 		speak = sounds.load(this, R.raw.routyspeak, 1);  
 		bad = sounds.load(this, R.raw.routybad, 1);
 		click = sounds.load(this, R.raw.routyclick, 1);
		
		sounds.setOnLoadCompleteListener(new OnLoadCompleteListener() {

      @Override
      public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (sampleId == speak) {
          volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
          volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
          Log.v(TAG, "volume is: " + volume);
          soundPool.play(sampleId, volume, volume, 1, 0, 1);
        }
      }
		  
		});
 
 		// Initializations
 		context 			= this;
 		addressModel = AddressModel.getSingleton();
 		
 		originAddressField 	= (EditText) findViewById(R.id.origin_address_field);
 		originAddressField.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				originValidated = false;
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				// nothing
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// nothing
 			}
 		});
 		
 		origin				= null;
 		originActivityPrefs = getSharedPreferences("origin_prefs", MODE_PRIVATE);
 		originValidated		= false;
 
 		restoreSavedOrigin(savedInstanceState);
 		
 		showInstructions();
 
 	}
 
 
 	private void showInstructions() {		
 		// First-time user dialog cookie
 		boolean noobCookie = originActivityPrefs.getBoolean("noob_cookie", false);
 		if (!noobCookie){
 			showNoobDialog();
 			userAintANoobNomore();
 		}
 	}
 
 
 	private void restoreSavedOrigin(Bundle savedInstanceState) {
 		// get stored origin address from shared prefs first...if there isn't one, THEN try savedInstanceState
 		originValidated = originActivityPrefs.getBoolean("origin_validated", false);
 		
 		if (!originValidated) {
 			Log.v(TAG, "origin was not validated last time...just restoring the string");
 			String savedOriginString = originActivityPrefs.getString("saved_origin_string", null);
 			originAddressField.setText(savedOriginString);
 			
 		} else {
 			String savedOriginJson = originActivityPrefs.getString("saved_origin", null);
 			if (savedOriginJson != null && savedOriginJson.length() > 0) {
 				Log.v(TAG, "building origin address from json: " + savedOriginJson);
 				origin = Util.readAddressFromJson(savedOriginJson);
 				
 				if (origin != null) {
 					Log.v(TAG, "got the restored origin address from sharedprefs");
 					
 					Bundle extras = origin.getExtras();
 					String addressStr = null;
 					if (extras != null) {
 						addressStr = extras.getString("formatted_address");
 					} else {
 						addressStr = String.format("%f, %f", origin.getLatitude(), origin.getLongitude());
 					}
 					
 					originAddressField.setText(addressStr);
 					
 					SharedPreferences.Editor ed = originActivityPrefs.edit();
 					ed.remove("saved_origin");
 					ed.commit();
 				}
 			}
 			
 			if (origin == null && savedInstanceState != null) {
 				// Get the origin as an Address object
 				origin = (Address) savedInstanceState.get("origin_address");
 				if (origin != null) {
 					Log.v(TAG, "got the restored origin address from instance state");
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Displays an {@link AlertDialog} with one button that dismisses the dialog. Dialog displays helpful first-time info.
 	 * 
 	 * @param message
 	 */
 	private void showNoobDialog() {
 		OneButtonDialog dialog = new OneButtonDialog(getResources().getString(R.string.origin_noob_title), getResources().getString(R.string.origin_noob_instructions)) {
 			@Override
 			public void onButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		};
 		dialog.show(context.getSupportFragmentManager(), TAG);
 	}
 	
 	/**
 	 *  If the user sees the first-time instruction dialog, they won't see it again next time.
 	 */
 	private void userAintANoobNomore() {
 		SharedPreferences.Editor ed = originActivityPrefs.edit();
 		ed.putBoolean("noob_cookie", true);
 		ed.commit();	
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ENABLE_GPS_REQUEST:
 			locate();
 			break;
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_origin, menu);
 		return true;
 	}
 
 
 	/**
 	 * Called when the "Find Me" button is tapped.
 	 * 
 	 * @param view
 	 */
 	public void findUserLocation(View view) {
 		Log.v(TAG, "locating user");
 		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);  
 
 		locate();
 	}
 	
 	
 	/**
 	 * Kicks off a {@link FindUserLocationTask} to try and obtain the user's location.
 	 */
 	void locate() {
 		new FindUserLocationTask(this, new FindUserLocationListener() {
 			
 			@Override
 			public void onUserLocationFound(Address userLocation) {
 				if (userLocation != null) {
 					Log.v(TAG, "got user location: " + userLocation.getAddressLine(0));
 					
 					origin = userLocation;
 					
 					Bundle extras = origin.getExtras();
 					if (extras == null) {
 						Log.e(TAG, "origin extras is null");
 					}
 					extras.putInt("valid_status", DestinationRowView.VALID);
 					origin.setExtras(extras);
 					
 					String addressStr = origin.getExtras().getString("formatted_address");
 					
 					originAddressField.setText(addressStr);
 					originValidated = true;
 				}
 			}
 			
 			@Override
 			public void onTimeout(GpsNotEnabledException e) {
 				Log.e(TAG, "getting user location timed out and gps was " + (e == null ? "not enabled" : "enabled"));
 				
 				showErrorDialog(getResources().getString(R.string.locating_fail_error));
 			}
 			
 			@Override
 			public void onFailure(Throwable t) {
 				Log.e(TAG, "failed getting user location");
 				try {
 					throw t;
 				} catch (NoLocationProviderException e) {
 					Log.e(TAG, "GPS was disabled, going to ask the user to enable it and then try again");
 					showEnableGpsDialog();
 				} catch (Throwable e) {
 					Log.e(TAG, "don't know why we couldn't obtain a location...");
 					Log.e(TAG, e.getMessage());
 					showErrorDialog(getResources().getString(R.string.locating_fail_error));
 				}
 			}
 		}).execute(0);
 	}
 
 
 	/**
 	 * Validates the origin address.  If it's good, it gets packaged into an Intent and sent to 
 	 * the DestinationActivity screen.
 	 * 
 	 * @param view
 	 */
 	public void goToDestinationsScreen(View view) {
 		// validate the origin address, store it, and move on to the destinations screen
 		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);  
 
 		final String locationQuery = originAddressField.getText().toString();
 		
 		if (!originValidated && (locationQuery == null || locationQuery.length() == 0)) {
 			showErrorDialog(getResources().getString(R.string.no_origin_address_error));
 		} else {
 			if (!originValidated) {
 				Log.v(TAG, "origin was user-entered and needs to be validated");
 				
 				// use GooglePlacesQueryTask to do this...
 				new GooglePlacesQueryTask(this) {
 					
 					@Override
 					public void onResult(GooglePlace place) {
 						// make an Address out of the Google place and start the DestinationActivity
 						origin = new Address(Locale.getDefault());
 						origin.setFeatureName(place.getName());
 						origin.setLatitude(place.getLatitude());
 						origin.setLongitude(place.getLongitude());
 						
 						Bundle extras = new Bundle();
 						extras.putString("formatted_address", place.getFormattedAddress());
 						extras.putInt("valid_status", DestinationRowView.VALID);
 						origin.setExtras(extras);
 						
 						originAddressField.setText(place.getFormattedAddress());	// TODO set the text in the edittext field
 						originValidated = true;
 						
 						startDestinationActivity();
 					}
 					
 					@Override
 					public void onFailure(Throwable t) {
 						showErrorDialog("Routy couldn't understand \"" + locationQuery + "\".  Please try something a little different.");		// TODO extract to strings.xml
 					}
 					
 					@Override
 					public void onNoSelection() {
 						// TODO do nothing?
 						
 					}
 				}.execute(new GooglePlacesQuery(locationQuery, null, null));
 				
 			} else {
 				startDestinationActivity();
 			}
 		}
 	}
 
 
 	private void startDestinationActivity() {
 		if (origin != null) {
 			// Origin address is good...move on to Destinations
 			Intent destinationIntent = new Intent(getBaseContext(), DestinationActivity.class);
 			destinationIntent.putExtra("origin", origin);	// Android Address is Parcelable, so no need for Bundle
 			startActivity(destinationIntent);
 		} else {
 			Log.e(TAG, "tried to start the destination activity with a null origin address");
 		}
 	}
 
 	
 	/**
 	 *  Saves the validated origin in shared preferences, saves user time when using Routy next.
 	 *//*
 	private void saveOriginInSharedPrefs() {
 		// TODO onDestroy needs to save the origin address OBJECT in sharedprefs
 		
 		SharedPreferences.Editor ed = originActivityPrefs.edit();
 		ed.putString("saved_origin_address", "");
 		ed.putString("saved_origin_string", originAddressField.getText().toString());
 		ed.commit();	
 	}*/
 
 
 	/**
 	 * Displays an {@link AlertDialog} with one button that dismisses the dialog.  Use this to display error messages 
 	 * to the user.
 	 * 
 	 * @param message
 	 */
 	private void showErrorDialog(String message) {
 		volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(bad, volume, volume, 1, 0, 1);
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
 	private void showEnableGpsDialog() {
 		TwoButtonDialog dialog = new TwoButtonDialog(getResources().getString(R.string.error_message_title), getResources().getString(R.string.enable_gps_prompt)) {
 
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
 				showErrorDialog(getResources().getString(R.string.locating_fail_error));
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
 	}
 	
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		if (!originValidated && originActivityPrefs != null) {
 			// save the origin input string
 			Log.v(TAG, "origin not validated, saving just the input string");
 			SharedPreferences.Editor ed = originActivityPrefs.edit();
 			ed.putString("saved_origin_string", originAddressField.getText().toString());
 			ed.putBoolean("origin_validated", originValidated);
 			ed.commit();
 			
 		} else if (origin != null && originActivityPrefs != null) {
 			// store origin in shared prefs
 			Log.v(TAG, "saving origin to SharedPreferences");
 			String json = Util.writeAddressToJson(origin);
 			SharedPreferences.Editor ed = originActivityPrefs.edit();
 			ed.putString("saved_origin", json);
 			Log.v(TAG, "saving json: " + json);
 			ed.putBoolean("origin_validated", originValidated);
 			ed.commit();
 		}
 		
 		
 	}
 	
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return false;
 	}
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putParcelable("origin_address", origin);
 		outState.putBoolean("origin_validated", originValidated);
 		Log.v(TAG, "saved the origin object");
 	}
 }
