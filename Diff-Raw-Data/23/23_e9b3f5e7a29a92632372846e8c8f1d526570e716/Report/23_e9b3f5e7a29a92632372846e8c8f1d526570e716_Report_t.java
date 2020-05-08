 package com.towson.wavyleaf;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.format.Time;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.CancelableCallback;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 /*
 * This class requires the Google Play Services apk, and provides access to Google Maps v2.
 * It is available on all phones with OpenGL v2.0 and Android 2.2+.  Requirements set in manifest
 * This also uses abs
 * 
 * SETUP:  http://developer.android.com/google/play-services/setup.html
 * Sdk Manager:  Extras >> Google Play Services
 * 
 * Sample Code is *then* located in sdk/extras/google/google_play_services/libproject/google-play-services_lib
 */
 
 public class Report extends SherlockFragmentActivity {
 	
 	private static final int LEGAL = 1, NO_GPS = 4; // Used for calling dialogs. arbitrary numbers
 	private static final int EDIT_REQUEST = 1338;
 	private boolean gpsEnabled = false;
 	private boolean playAPKEnabled = false;
 	private boolean editedCoordinatesInOtherActivitySoDontGetGPSLocation = false;
 	private boolean mapHasMarker = false; // onResume keeps adding markers to map, this should stop it
 	protected CameraPosition userCurrentPosition;
 	protected CheckBox cb;
 	protected EditText notes, etarea;
 	protected GoogleMap mMap;
 	protected Location currentEditableLocation; // Used by edit feature
 	protected LocationManager mLocationManager;
 	protected RadioGroup rg;
 	protected Spinner sp;
 	protected TextView tvlat, tvlong, tvpicnotes, tvper, tvper_summary, tvcoor, tvarea, tvarea_summary;
 	protected ToggleButton b1, b2, b3, b4, b5, b6;
 	private UiSettings mUiSettings;
 	private LocationApplication locationData;	
 	private Timer updateLocationTimer;
 	
 	private static final int ONE_MINUTE = 1000*60; //in ms
 	private static final int FIVE_SECONDS = 1000*5; //in ms
 	private static final int TEN_METERS = 10; //in m
 	
 	// private static final int CAMERA = 3;
 	// private static final int GALLERY_REQUEST = 1339;
 	// private static final int CAMERA_REQUEST = 1337;
 	// protected ImageButton ib;
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.layout_report);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		init();
 		
 		// Most setup methods are in onResume()
 	}
 	
 	protected void init() {
 		getWindow().setBackgroundDrawable(null);
 		Typeface tf_light = Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf");
 		Typeface tf_bold = Typeface.createFromAsset(getAssets(), "fonts/roboto_bold.ttf");
 		
 		tvlat = (TextView) findViewById(R.id.tv_latitude);
 		tvlong = (TextView) findViewById(R.id.tv_longitude);
 		tvpicnotes = (TextView) findViewById(R.id.tv_picturenotes);
 		tvper = (TextView) findViewById(R.id.tv_percentageseen);
 		tvper_summary = (TextView) findViewById(R.id.tv_percentageseen_summary);
 		tvcoor = (TextView) findViewById(R.id.tv_coordinates);
 		tvarea = (TextView) findViewById(R.id.tv_areainfested);
 		tvarea_summary = (TextView) findViewById(R.id.tv_areainfested_summary);
 		notes = (EditText) findViewById(R.id.notes);
 		etarea = (EditText) findViewById(R.id.et_areainfested);
 		b1 = (ToggleButton) findViewById(R.id.bu_1);
 		b2 = (ToggleButton) findViewById(R.id.bu_2);
 		b3 = (ToggleButton) findViewById(R.id.bu_3);
 		b4 = (ToggleButton) findViewById(R.id.bu_4);
 		b5 = (ToggleButton) findViewById(R.id.bu_5);
 		b6 = (ToggleButton) findViewById(R.id.bu_6);
 		cb = (CheckBox) findViewById(R.id.cb_confirm);
 		rg = (RadioGroup) findViewById(R.id.toggleGroup);
 		sp = (Spinner) findViewById(R.id.sp_areainfested);
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		locationData = (LocationApplication) getApplication();
 		currentEditableLocation = locationData.getLocation();
 		
 		updateLocationTimer = new Timer();
 		TimerTask updateLocationTask = new TimerTask(){			
 			@Override
 			public void run() {
 				checkLocation();
 			}
 			
 		};
 		updateLocationTimer.scheduleAtFixedRate(updateLocationTask , 0, FIVE_SECONDS);
 		
 		// Listener for EditText in Area Infested
 		etarea.addTextChangedListener(new TextWatcher() {
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				if (etarea.getText().length() == 0) {
 					tvarea_summary.setText("");
 				}
 				else if (etarea.getText().toString().contains("-")) {	//negative number sign
 					etarea.getEditableText().clear();
 					Toast.makeText(getApplicationContext(), "Negative values not allowed", Toast.LENGTH_SHORT).show();
 				}
 				else {
 					tvarea_summary.setText(etarea.getText() + " " + sp.getSelectedItem().toString());
 				}
 			}
 			public void afterTextChanged(Editable s) {}
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 		});
 		
 		// Listener for Spinner in Area Infested
 		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override public void onNothingSelected(AdapterView<?> arg0) {}
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				if (etarea.getText().length() != 0)
 					tvarea_summary.setText(etarea.getText() + " " + sp.getSelectedItem());
 			}
 			
 	    });
 		
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.areainfested_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		sp.setAdapter(adapter);
 		
 		// Just to be safe
 		cb.setChecked(false);
 		
 		//set all the beautiful typefaces
 		tvlat.setTypeface(tf_light);
 		tvlong.setTypeface(tf_light);
 		tvcoor.setTypeface(tf_bold);
 		tvarea.setTypeface(tf_bold);
 		tvarea_summary.setTypeface(tf_bold);
 		tvper.setTypeface(tf_bold);
 		tvper_summary.setTypeface(tf_bold);
 		tvpicnotes.setTypeface(tf_bold);
 		cb.setTypeface(tf_light);
 		b1.setTypeface(tf_light);
 		b2.setTypeface(tf_light);
 		b3.setTypeface(tf_light);
 		b4.setTypeface(tf_light);
 		b5.setTypeface(tf_light);
 		b6.setTypeface(tf_light);
 		
 		if(!locationData.isSearching())
 			findUsersLocation();
 		
 //		ib = (ImageButton) findViewById(R.id.report_imagebutton);
 		// Listener for camera button
 //		ib.setOnClickListener(new OnClickListener() {
 //            public void onClick(View v) {
 //                takePicture();
 //            }
 //        });
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		// User edited coordinates, so don't get them again from gps
 		if (!editedCoordinatesInOtherActivitySoDontGetGPSLocation) {
 			if(!isAccurateLocation(currentEditableLocation))
 				refresh();
 			else{
 				updateUILocation(currentEditableLocation);
 				setUpMapIfNeeded();
 			}
 //			// Check for GPS
 //			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 //			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 //			playAPKEnabled = doesDeviceHaveGooglePlayServices();
 //			
 //			if (!gpsEnabled) { // If GPS is disabled
 //				buildAlertMessageNoGps();
 //			} else if(gpsEnabled) {
 //				if (playAPKEnabled) {
 //					setUpMapIfNeeded();
 //					wheresWaldo();
 //				}
 //			}
 		}
 	}
 	
 	protected void refresh() {
 		// Check for GPS
 		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		playAPKEnabled = doesDeviceHaveGooglePlayServices();
 		
 		currentEditableLocation = locationData.getLocation();
 		
 		if(!isAccurateLocation(currentEditableLocation)){ // If location isn't accurate
 		
 				
 			if (!gpsEnabled) { // If GPS is disabled
 				buildAlertMessageNoGps();
 			} else if(gpsEnabled) {
 				if (playAPKEnabled) {
 					setUpMapIfNeeded();
 					wheresWaldo();
 				}
 			}
 			
 			updateLocationTimer = new Timer();
 			TimerTask updateLocationTask = new TimerTask(){			
 				@Override
 				public void run() {
 					checkLocation();
 				}
 				
 			};
 			updateLocationTimer.scheduleAtFixedRate(updateLocationTask , 0, FIVE_SECONDS);
 			
 		}else
 			setUpMapIfNeeded();
 	}
 
 	protected boolean doesDeviceHaveGooglePlayServices() {
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 		//If user doesn't have the apk, then it prompts them to download it
 		if (!(resultCode == ConnectionResult.SUCCESS)) {
 			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1).show();
 			return false;
 		} else
 			return true;
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	getSupportMenuInflater().inflate(R.menu.menu_report, menu);
     	return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
         case android.R.id.home:
         		Intent mainIntent = new Intent(this, Main.class);
         		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         		startActivity(mainIntent);
         		finish();
         		return true;
         case R.id.menu_submit:
 //        	Toast.makeText(getApplicationContext(), String.valueOf(currentEditableLocation.getTime()), Toast.LENGTH_SHORT).show();
     		if (isAccurateLocation(currentEditableLocation)){//if LocationListener is accurate
             	// If all fields are filled out, minus Notes/Area infested
     			if (verifyFields() == true) {
     				Toast.makeText(getApplicationContext(), "Sighting recorded", Toast.LENGTH_SHORT).show();
             		createJSONObject();
            		// Restore preferences
            		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            	    boolean tripEnabled = sp.getBoolean("TRIP_ENABLED",false);
            	    if(!tripEnabled){
            			locationData.stop();	
            	    }
            		updateLocationTimer.cancel();
             		finish();
             	}
     		} else if (requestUpdatesFromProvider() == null) // If no GPS
     			Toast.makeText(getApplicationContext(), "Cannot submit without GPS signal", Toast.LENGTH_SHORT).show();
     		else {
             	// If all fields are filled out, minus Notes/Area infested
     			if (verifyFields() == true) {
     				Toast.makeText(getApplicationContext(), "Sighting recorded", Toast.LENGTH_SHORT).show();
             		createJSONObject();
             		// Restore preferences
             		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
             	    boolean tripEnabled = sp.getBoolean("TRIP_ENABLED",false);
             	    if(!tripEnabled){
             			locationData.stop();	
             	    }
             		updateLocationTimer.cancel();
             		finish();
             	}
     		}
     		return true;
         case R.id.menu_refresh:
         	mMap.clear();
         	mapHasMarker = false;
         	refresh();
         	Toast.makeText(getApplicationContext(), "Location refreshed", Toast.LENGTH_SHORT).show();
         	return true;
         case R.id.menu_legal:
         	showDialog(LEGAL);
         	return true;
         }
 		return super.onOptionsItemSelected(item);
 	}
 	
 	// Method only allows one ToggleButton to be set to true at a time
 	// Call to this method is defined in xml for each togglebutton
 	public void onToggle(View view) {
 		
 		//loop through all children in radiogroup.  In this case, two lin layouts
 		for (int i = 0; i < rg.getChildCount(); i++) {
 			View child = rg.getChildAt(i);
 			
 			//if child is lin layout (we already know all children are lin layouts)
 			if (child instanceof LinearLayout) {
 				
 				//then loop through three toggles
 				for (int j = 0; j < ((ViewGroup)child).getChildCount(); j++) {
 					final ToggleButton tog = (ToggleButton) ((ViewGroup)child).getChildAt(j);
 					
 					//have only one togglebutton selected at one time
 					if (tog != view)
 						tog.setChecked(false);
 				}
 			}
 		}
 		
 		// Determine text to set to textview
 		switch (view.getId()) {
 			case R.id.bu_1:
 				tvper_summary.setText("0%");
 				etarea.setText("0");
 				break;
 			case R.id.bu_2:
 				tvper_summary.setText("1-10%");
 				etarea.setText("");
 				break;
 			case R.id.bu_3:
 				tvper_summary.setText("10-25%");
 				etarea.setText("");
 				break;
 			case R.id.bu_4:
 				tvper_summary.setText("25-50%");
 				etarea.setText("");
 				break;
 			case R.id.bu_5:
 				tvper_summary.setText("50-75%");
 				etarea.setText("");
 				break;
 			case R.id.bu_6:
 				tvper_summary.setText("75-100%");
 				etarea.setText("");
 				break;
 			default:
 				tvper_summary.setText("");
 		}
 	}
 	
 	
 	// Get value of toggle selected
 	public String getSelectedToggleButton() {
 		String str = "";
 		for (int i = 0; i < rg.getChildCount(); i++) {
 			View child = rg.getChildAt(i);
 			if (child instanceof LinearLayout) {
 				for (int j = 0; j < ((ViewGroup)child).getChildCount(); j++) {
 					final ToggleButton tog = (ToggleButton) ((ViewGroup)child).getChildAt(j);
 					if (tog.isChecked())
 						str = tog.getText().toString();
 				}
 			}
 		}		
 		return str;
 	}
 	
 	public void onEdit(View view) {
 		if (hasCoordinates()) {
 			Intent editIntent = new Intent(this, Report_Mapview.class);
 			editIntent.putExtra("location", currentEditableLocation);
 			startActivityForResult(editIntent, EDIT_REQUEST);
 		} else
 			Toast.makeText(getApplicationContext(), "Editing coordinates requires GPS signal", Toast.LENGTH_SHORT).show();
 	}
 	
 	private void setUpMapIfNeeded() {
 		if (mMap == null) {
 			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview_checkin)).getMap();
 			if (mMap != null)
 				setUpMap(); // Weird method chaining, but this is what google example code does
 		}
 	}
 	
 	private void setUpMap() {
         //updateMyLocation();
         mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
         mUiSettings = mMap.getUiSettings();
         mUiSettings.setCompassEnabled(false);
         mUiSettings.setMyLocationButtonEnabled(false);
         mUiSettings.setAllGesturesEnabled(false);
     }
 	
 	private void updateMyLocation() {
 		mMap.setMyLocationEnabled(true);
 	}
 	
 	private void updateUILocation(Location location) {
 		goToCurrentPosition(location);
 		setEditTexts(location.getLatitude(), location.getLongitude());
 		if (!mapHasMarker){
 			setUpMapIfNeeded();
 			setCurrentPositionMarker(location);
 		}
 		else {
 			mMap.clear();
 			setUpMapIfNeeded();
 			setCurrentPositionMarker(location);
 		}
 	}
 	
 	// Method won't be called unless Play APK in installed
 	public void wheresWaldo() {
 		Location gpsLocation = requestUpdatesFromProvider();
 		if (gpsLocation == null)
 			Toast.makeText(getApplicationContext(), "No GPS signal", Toast.LENGTH_SHORT).show();
 		else if (gpsLocation != null) {
 			// Set global location variable so if user selects edit, it has something to pass
 			currentEditableLocation = gpsLocation;
 			updateUILocation(gpsLocation);
 		}
 	}
 	
 	// Same method as wheresWaldo() without toast. Call this method when checking location in a thread.
 	public void wheresCarmenSandiego() {
 		Location gpsLocation = requestUpdatesFromProvider();
 		if (gpsLocation != null) {
 			// Set global location variable so if user selects edit, it has something to pass
 			currentEditableLocation = gpsLocation;
 			updateUILocation(gpsLocation);
 		}
 	}
 	
 	private boolean isAccurateLocation(Location location){
 		
 		if(location == null)
 			return false;
 		
 		boolean isRecent = (location.getTime() + ONE_MINUTE) > System.currentTimeMillis();
 		
 		//Toast.makeText(this, String.valueOf(location.getTime()) + " current: " + String.valueOf(System.currentTimeMillis()), Toast.LENGTH_SHORT).show();
 		
 		if(isRecent){
 			//if recent, it is accurate
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 	private Location requestUpdatesFromProvider() {
 		currentEditableLocation = locationData.getLocation();
 		
         if(isAccurateLocation(currentEditableLocation))
         	return currentEditableLocation;
 		
 		Location location = null;
 		return location;
 	}
 	
 	private void buildAlertMessageNoGps() {
 		showDialog(NO_GPS);
 	}
 	
 	protected void setEditTexts(double latitude, double longitude) {
 		tvlat.setText("Latitude:\t\t\t" + latitude);
 		tvlong.setText("Longitude:\t\t" + longitude);
 	}
 	
 	public void setCurrentPositionMarker(Location location) {
 		// Create new LatLng object
         LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
 		
 		// Creating a marker
         MarkerOptions markerOptions = new MarkerOptions();
         markerOptions.position(latLng);
 
         // Placing a marker on the touched position
         mMap.addMarker(markerOptions);
         mapHasMarker = true;
 	}
 	
 	public void goToCurrentPosition(Location location) {
 		if (!checkReady()) {
 			return;
 		}
 		
 		// Taken from google sample code
 		userCurrentPosition =
 	            new CameraPosition.Builder()
 						.target(new LatLng(location.getLatitude(), location.getLongitude()))
 	                    .zoom(18f) //arbitrary
 	                    .bearing(0)
 	                    .tilt(35) //arbitrary
 	                    .build();
 		
 		changeCamera(CameraUpdateFactory.newCameraPosition(userCurrentPosition));
 	}
 	
 	// Part of the sample code
 	private boolean checkReady() {
 		if (mMap == null) {
 			Toast.makeText(this, "Map not loaded yet", Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		return true;
 	}
 	
 	private void changeCamera(CameraUpdate update) {
 		changeCamera(update, null);
 	}
 	
 	private void changeCamera(CameraUpdate update, CancelableCallback callback) {
 		mMap.animateCamera(update, callback);
 //		mMap.moveCamera(update); //for the less fun people
     }
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch(id) {
 			case LEGAL:
 				return new AlertDialog.Builder(this)
 				.setTitle("Legal Notice")
 				.setMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext()))
 				.setPositiveButton("Got it", null)
 				.setNegativeButton("Cancel", null)
 				.create();
 				
 			case NO_GPS:
 				return new AlertDialog.Builder(this)
 				.setTitle(getResources().getString(R.string.gps_is_disabled))
 				.setMessage(getResources().getString(R.string.show_location_settings))
 				.setCancelable(false)
 				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 					}
 				})
 				.create();
 				
 //			case CAMERA:
 //				return new AlertDialog.Builder(this)
 //				.setItems(R.array.camera_array, new DialogInterface.OnClickListener() {
 //					public void onClick(DialogInterface dialog, int item) {
 //						if (item == 0) { // Take picture
 //							Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
 //			                startActivityForResult(cameraIntent, CAMERA_REQUEST); 
 //						} else if (item == 1) { // Choose from gallery
 //							Intent intent = new Intent();
 //							intent.setType("image/*");
 //							intent.setAction(Intent.ACTION_GET_CONTENT);
 //							startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
 //						}
 //					}
 //				})
 //				.create();
 		}
 		return super.onCreateDialog(id);
 		//http://stackoverflow.com/questions/3326366/what-context-should-i-use-alertdialog-builder-in
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK) {
 			editedCoordinatesInOtherActivitySoDontGetGPSLocation = true;
         	Location fixedLocation = data.getExtras().getParcelable("location");
         	
         	// Current marker is expired, remove that crap
         	mMap.clear();
         	mapHasMarker = !mapHasMarker;
         	setUpMapIfNeeded();
         	updateUILocation(fixedLocation);
         	
         	// Update location to be send with JSON report
         	currentEditableLocation.setLatitude(fixedLocation.getLatitude());
         	currentEditableLocation.setLongitude(fixedLocation.getLongitude());
         	
         	// Since user edited their coordinates, they obviously know it's right
         	cb.setChecked(true);
         	
         	Toast.makeText(getApplicationContext(), "New position set", Toast.LENGTH_SHORT).show();
 		}
 		
 //		else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
 //            Bitmap bm = (Bitmap) data.getExtras().get("data"); 
 //            ib.setImageBitmap(bm);
 //            
 //		} else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
 //			Uri selectedImage = data.getData();
 //            InputStream imageStream = null;
 //			
 //            try { imageStream = getContentResolver().openInputStream(selectedImage); }
 //			catch (FileNotFoundException e) {}
 //            
 //			Bitmap img = BitmapFactory.decodeStream(imageStream);
 //			ib.setImageBitmap(img);
     }
 	
 	/** Verify that required fields are filled
 	 * @return boolean stating if all fields are filled out **/
 	private boolean verifyFields() {
 		boolean result = false;
 		
 		if (isToggleSelected()) {
 			if (hasCoordinates()) {
 				if (cb.isChecked()) {
 					result = true;
 				} else
 					Toast.makeText(getApplicationContext(), "Verify your coordinates with the checkbox", Toast.LENGTH_SHORT).show();
 			} else
 				Toast.makeText(getApplicationContext(), "Error determining position", Toast.LENGTH_SHORT).show();
 		} else
 			Toast.makeText(getApplicationContext(), "Select a percentage", Toast.LENGTH_SHORT).show();
 		
 		return result;
 	}
 	
 	// See if any toggle buttons are selected
 	// Not sure if this method is required when there are two others like it... but it's 3am. So yes it is.
 	public boolean isToggleSelected() {
 		boolean result = false;
 		
 		for (int i = 0; i < rg.getChildCount(); i++) {
 			View child = rg.getChildAt(i);
 			if (child instanceof LinearLayout) {
 				for (int j = 0; j < ((ViewGroup)child).getChildCount(); j++) {
 					final ToggleButton tog = (ToggleButton) ((ViewGroup)child).getChildAt(j);
 					if (tog.isChecked())
 						result = true;
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	// See if user has coordinates
 	public boolean hasCoordinates() {
 		boolean result = false;
 		
 		if (!(currentEditableLocation == null))
 			result = true;
 		
 		return result;
 	}
 	
 	private String shortenAreaType() {
 		String str = sp.getSelectedItem().toString();
 		if (str.equals("Hectares"))
 			str = "HA";
 		else if (str.equals("Square Metres"))
 			str = "SM";
 		else if (str.equals("Acres"))
 			str = "SA";
 		else
 			str = "SF";
 		
 		return str;
 	}
 
 	protected double getAreaText() {
 		if (etarea.getText().toString().trim().equals("") || (etarea.getText().toString().trim().equals(null)))
 			return -1;
 		else
 			return Double.parseDouble(etarea.getText().toString());
 	}
 	
 	private void createJSONObject() {
 		Time now = new Time();
 		now.setToNow();
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		JSONObject report = new JSONObject();
 		
 		try {
 			report.put(UploadData.ARG_USER_ID, sp.getString(Settings.KEY_USER_ID, "null"));
 			report.put(UploadData.ARG_PERCENT, getSelectedToggleButton());
 			report.put(UploadData.ARG_AREAVALUE, getAreaText());
 			report.put(UploadData.ARG_AREATYPE, shortenAreaType());
 			report.put(UploadData.ARG_LATITUDE, currentEditableLocation.getLatitude());
 			report.put(UploadData.ARG_LONGITUDE, currentEditableLocation.getLongitude());
 			report.put(UploadData.ARG_NOTES, notes.getText());
 			report.put(UploadData.ARG_DATE, now.year + "-" + (now.month + 1) + "-" + now.monthDay + " " + now.hour + ":" + now.minute + ":" + now.second);
 			//bitmap would go here
 			
 		} catch (JSONException e) {
 			Toast.makeText(getApplicationContext(), "Data not saved, try again", Toast.LENGTH_SHORT).show();
 		}
 		
 		new UploadData(this, UploadData.TASK_SUBMIT_POINT).execute(report);
 	}
 	
 //	protected void takePicture() {
 //		startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), CAMERA_REQUEST);
 //	}
 
 
 	private void checkLocation()
 	{
 		//This method is called directly by the timer
 		//and runs in the same thread as the timer.
 
 		//We call the method that will work with the UI
 		//through the runOnUiThread method.
 		this.runOnUiThread(Timer_UI_Thread);
 	}
 
 
 	private Runnable Timer_UI_Thread = new Runnable() {
 		public void run() {
 		
 		//This method runs in the same thread as the UI.    	       
 		locationData = (LocationApplication) getApplication();
 		currentEditableLocation = locationData.getLocation();
 		
 		if (currentEditableLocation != null){
 			updateUILocation(currentEditableLocation);
 			setUpMapIfNeeded();
 		}else
 			wheresCarmenSandiego();
 	
 		}
 	};
 	
 	private void findUsersLocation(){
 		locationData = (LocationApplication) getApplication();
 		locationData.init();
 	}
 	
 	// http://stackoverflow.com/questions/11251901/check-whether-database-is-empty
 	protected boolean isDBEmpty() {
 		DatabaseListJSONData m_dbListData = new DatabaseListJSONData(this);
 		SQLiteDatabase db = m_dbListData.getWritableDatabase();
 		
 		Cursor cur = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_NAME, null);
 		if (cur.moveToFirst())
 			return false;
 		else
 			return true;
 	}
 
 }
 
 
