 package cse.bgu.finalandroidproject.theamazingrace;
 
 import java.io.IOException;
 import java.security.SecureRandom;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class PlayGame extends android.support.v4.app.FragmentActivity 
 		implements	 OnMapClickListener, OnMapLongClickListener, OnCameraChangeListener{
 
     /**
      * Note that this may be null if the Google Play services APK is not available.
      */
     private GoogleMap mMap;
     private TextView mTapTextView;
     private TextView mCameraTextView;
     private Button checkMyLocation;
     private TextView tScore;
     private TextView eScore;
     private TextView eTime;
     private Chronometer gTimer;
     // UI handler codes.
     private TextView mLatLng;
     private TextView mAddress;
     private Button mFineProviderButton;
     private Button mBothProviderButton;
     private LocationManager mLocationManager;
     private Handler mHandler;
     private boolean mGeocoderAvailable;
     private boolean mUseFine;
     private boolean mUseBoth;
 
     // Keys for maintaining UI states after rotation.
     private static final String KEY_FINE = "use_fine";
     private static final String KEY_BOTH = "use_both";
     // UI handler codes.
     private static final int UPDATE_ADDRESS = 1;
     private static final int UPDATE_LATLNG = 2;
 
     private static final int TEN_SECONDS = 10000;
     private static final int TEN_METERS = 10;
     private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int THRESHOLD = 10;
     
     
     // mission variables
     private String question;
     private String answers[] = new String[4];
     private Location currntPoint = new Location("currntPoint");
     private int score = 0;
     
     //new *****************************************************
     private Button bAnswer1;
     private Button bAnswer2;
     private Button bAnswer3;
     private Button bAnswer4;
     private Button bContinue;
     private TextView dScore;
     private TextView dQuestion;
     private int answersStatus[] = {3, 3, 3, 3};
     private int bonus[] = {10, 5,  2,  0};
     private int counter = 0;
     private boolean flag = false;
     
 /*    Challenge newChallenge[] = {new Challenge (new LatLng(30, 34), "Whats my name?", "Alon", new String[] {"Tal", "Oscar", "Gil"}),
     							new Challenge (new LatLng(30, 34), "What is the color of Napolion's white horse?", "White", new String[] {"Black", "Red", "Blue"}), 
     							new Challenge (new LatLng(30, 34), "How meny meters are in one nautical mile?", "1852", new String[] {"1609", "1734", "1586"}),
     							new Challenge (new LatLng(30, 34), "Where are we?", "All the answers are correct", new String[] {"bilding 95", "lab 105", "B.G.U"})}; 
 */    
     MySQLiteOpenHelper db = new MySQLiteOpenHelper(this);
     List<Challenge> myList;
     Iterator<Challenge> myIterator=null;
     
     
     // end new ************************************************
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play_game);
 		myList = db.getEntireGame();
         mTapTextView = (TextView) findViewById(R.id.tap_text);
         mCameraTextView = (TextView) findViewById(R.id.camera_text);
         checkMyLocation = (Button) findViewById(R.id.checkMyLocation);
         tScore = (TextView) findViewById(R.id.label_score);
         gTimer = (Chronometer) findViewById(R.id.chronometer1);
 		setUpMapIfNeeded();
 
 		
         // Restore apps state (if exists) after rotation.
         if (savedInstanceState != null) {
             mUseFine = savedInstanceState.getBoolean(KEY_FINE);
             mUseBoth = savedInstanceState.getBoolean(KEY_BOTH);
         } else {
             mUseFine = false;
             mUseBoth = false;
         }
         mLatLng = (TextView) findViewById(R.id.latlng);
         mAddress = (TextView) findViewById(R.id.address);
         // Receive location updates from the fine location provider (GPS) only.
         mFineProviderButton = (Button) findViewById(R.id.provider_fine);
         // Receive location updates from both the fine (GPS) and coarse (network) location
         // providers.
         mBothProviderButton = (Button) findViewById(R.id.provider_both);
 
         // The isPresent() helper method is only available on Gingerbread or above.
         mGeocoderAvailable =
                 Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent();
 
         // Handler for updating text fields on the UI like the lat/long and address.
         mHandler = new Handler() {
             public void handleMessage(Message msg) {
                 switch (msg.what) {
                     case UPDATE_ADDRESS:
                         mAddress.setText((String) msg.obj);
                         break;
                     case UPDATE_LATLNG:
                         mLatLng.setText((String) msg.obj);
                         break;
                 }
             }
         };
         // Get a reference to the LocationManager object.
         mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
         
 /*        // loading the first challenge
         //////////////////////////////////////////////////////////////////
         myList.add(newChallenge[0]);
         myList.add(newChallenge[1]);
         myList.add(newChallenge[2]);
         myList.add(newChallenge[3]);
 */        
         //Challenge curChallenge = myList.iterator().next();
         if (myIterator==null) 
 			myIterator = myList.iterator();
         gTimer.setBase(SystemClock.elapsedRealtime());
         gTimer.start();
 		
         suffleAnswers(myIterator.next());
         
         /////////////////////////////////////////////////////////////////
 		
 	}
 
     // Restores UI states after rotation.
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean(KEY_FINE, mUseFine);
         outState.putBoolean(KEY_BOTH, mUseBoth);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         setUpMapIfNeeded();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         // Check if the GPS setting is currently enabled on the device.
         // This verification should be done during onStart() because the system calls this method
         // when the user returns to the activity, which ensures the desired location provider is
         // enabled each time the activity resumes from the stopped state.
         LocationManager locationManager =
                 (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
         if (!gpsEnabled) {
             // Build an alert dialog here that requests that the user enable
             // the location services, then when the user clicks the "OK" button,
             // call enableLocationSettings()
             new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
         }
     }
 
     // Method to launch Settings
     private void enableLocationSettings() {
         Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         startActivity(settingsIntent);
     }
 
     // Stop receiving location updates whenever the Activity becomes invisible.
     @Override
     protected void onStop() {
         super.onStop();
         mLocationManager.removeUpdates(listener);
     }
 
     // Set up fine and/or coarse location providers depending on whether the fine provider or
     // both providers button is pressed.
     private void setup() {
         Location gpsLocation = null;
         Location networkLocation = null;
         mLocationManager.removeUpdates(listener);
         mLatLng.setText(R.string.unknown);
         mAddress.setText(R.string.unknown);
         // Get fine location updates only.
         if (mUseFine) {
             mFineProviderButton.setBackgroundResource(R.drawable.button_active);
             mBothProviderButton.setBackgroundResource(R.drawable.button_inactive);
             // Request updates from just the fine (gps) provider.
             gpsLocation = requestUpdatesFromProvider(
                     LocationManager.GPS_PROVIDER, R.string.not_support_gps);
             // Update the UI immediately if a location is obtained.
             if (gpsLocation != null) updateUILocation(gpsLocation);
         } else if (mUseBoth) {
             // Get coarse and fine location updates.
             mFineProviderButton.setBackgroundResource(R.drawable.button_inactive);
             mBothProviderButton.setBackgroundResource(R.drawable.button_active);
             // Request updates from both fine (gps) and coarse (network) providers.
             gpsLocation = requestUpdatesFromProvider(
                     LocationManager.GPS_PROVIDER, R.string.not_support_gps);
             networkLocation = requestUpdatesFromProvider(
                     LocationManager.NETWORK_PROVIDER, R.string.not_support_network);
 
             // If both providers return last known locations, compare the two and use the better
             // one to update the UI.  If only one provider returns a location, use it.
             if (gpsLocation != null && networkLocation != null) {
                 updateUILocation(getBetterLocation(gpsLocation, networkLocation));
             } else if (gpsLocation != null) {
                 updateUILocation(gpsLocation);
             } else if (networkLocation != null) {
                 updateUILocation(networkLocation);
             }
         }
     }
 
     /**
      * Method to register location updates with a desired location provider.  If the requested
      * provider is not available on the device, the app displays a Toast with a message referenced
      * by a resource id.
      *
      * @param provider Name of the requested provider.
      * @param errorResId Resource id for the string message to be displayed if the provider does
      *                   not exist on the device.
      * @return A previously returned {@link android.location.Location} from the requested provider,
      *         if exists.
      */
     private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
         Location location = null;
         if (mLocationManager.isProviderEnabled(provider)) {
             mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
             location = mLocationManager.getLastKnownLocation(provider);
         } else {
             Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
         }
         return location;
     }
 
     // Callback method for the "fine provider" button.
     public void useFineProvider(View v) {
         mUseFine = true;
         mUseBoth = false;
         setup();
     }
 
     // Callback method for the "both providers" button.
     public void useCoarseFineProviders(View v) {
         mUseFine = false;
         mUseBoth = true;
         setup();
     }
 
     private void doReverseGeocoding(Location location) {
         // Since the geocoding API is synchronous and may take a while.  You don't want to lock
         // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
         (new ReverseGeocodingTask(this)).execute(new Location[] {location});
     }
 
     private void updateUILocation(Location location) {
         // We're sending the update to a handler which then updates the UI with the new
         // location.
         Message.obtain(mHandler,
                 UPDATE_LATLNG,
                 location.getLatitude() + ", " + location.getLongitude()).sendToTarget();
 
         // Bypass reverse-geocoding only if the Geocoder service is available on the device.
         if (mGeocoderAvailable) doReverseGeocoding(location);
     }
 
     private final LocationListener listener = new LocationListener() {
 
         @Override
         public void onLocationChanged(Location location) {
             // A new location update is received.  Do something useful with it.  Update the UI with
             // the location update.
             updateUILocation(location);
         }
 
         @Override
         public void onProviderDisabled(String provider) {
         }
 
         @Override
         public void onProviderEnabled(String provider) {
         }
 
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
         }
     };
 
     /** Determines whether one Location reading is better than the current Location fix.
       * Code taken from
       * http://developer.android.com/guide/topics/location/obtaining-user-location.html
       *
       * @param newLocation  The new Location that you want to evaluate
       * @param currentBestLocation  The current Location fix, to which you want to compare the new
       *        one
       * @return The better Location object based on recency and accuracy.
       */
     protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
         if (currentBestLocation == null) {
             // A new location is always better than no location
             return newLocation;
         }
 
         // Check whether the new location fix is newer or older
         long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
         boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
         boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
         boolean isNewer = timeDelta > 0;
 
         // If it's been more than two minutes since the current location, use the new location
         // because the user has likely moved.
         if (isSignificantlyNewer) {
             return newLocation;
         // If the new location is more than two minutes older, it must be worse
         } else if (isSignificantlyOlder) {
             return currentBestLocation;
         }
 
         // Check whether the new location fix is more or less accurate
         int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
         boolean isLessAccurate = accuracyDelta > 0;
         boolean isMoreAccurate = accuracyDelta < 0;
         boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
         // Check if the old and new location are from the same provider
         boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                 currentBestLocation.getProvider());
 
         // Determine location quality using a combination of timeliness and accuracy
         if (isMoreAccurate) {
             return newLocation;
         } else if (isNewer && !isLessAccurate) {
             return newLocation;
         } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
             return newLocation;
         }
         return currentBestLocation;
     }
 
     /** Checks whether two providers are the same */
     private boolean isSameProvider(String provider1, String provider2) {
         if (provider1 == null) {
           return provider2 == null;
         }
         return provider1.equals(provider2);
     }
 
     // AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
     // we do not want to invoke it from the UI thread.
     private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
         Context mContext;
 
         public ReverseGeocodingTask(Context context) {
             super();
             mContext = context;
         }
 
         @Override
         protected Void doInBackground(Location... params) {
             Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
 
             Location loc = params[0];
             List<Address> addresses = null;
             try {
                 addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
             } catch (IOException e) {
                 e.printStackTrace();
                 // Update address field with the exception.
                 Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
             }
             if (addresses != null && addresses.size() > 0) {
                 Address address = addresses.get(0);
                 // Format the first line of address (if available), city, and country name.
                 String addressText = String.format("%s, %s, %s",
                         address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                         address.getLocality(),
                         address.getCountryName());
                 // Update address field on UI.
                 Message.obtain(mHandler, UPDATE_ADDRESS, addressText).sendToTarget();
             }
             return null;
         }
     }
 
     /**
      * Dialog to prompt users to enable GPS on the device.
      */
     private class EnableGpsDialogFragment extends DialogFragment {
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return new AlertDialog.Builder(getActivity())
                     .setTitle(R.string.enable_gps)
                     .setMessage(R.string.enable_gps_dialog)
                     .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             enableLocationSettings();
                         }
                     })
                     .create();
         }
     }
     /**
      * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #mMap} is not null.
      * <p>
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView
      * MapView}) will show a prompt for the user to install/update the Google Play services APK on
      * their device.
 
     private static final String DEBUG_TAG = "HttpExample";
 
 	//create functions for http get and post
 
 	// When user clicks button, calls AsyncTask.
 	// Before attempting to fetch the URL, makes sure that there is a network connection.
 	// Gets the URL from the UI's text field.
 
 	/**
 	 * Checks for available connectivity return true if available else return false
 	 * @param context :: Context()
 	 * @return
      * <p>
      * A user can return to this Activity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the Activity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 setUpMap();
             }
         }
     }
 
     /**
      * This is where we can add markers or lines, add listeners or move the camera. In this case, we
      * just add a marker near Africa.
      * <p>
      * This should only be called once and when we are sure that {@link #mMap} is not null.
      */
     private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
         mMap.setOnMapClickListener(this);
         mMap.setOnMapLongClickListener(this);
         mMap.setOnCameraChangeListener(this);
     }
     
     
     
     /**
      * When the player want to know if he arrived to the right checkpoint he should press 
      * the "check my location !!!" button.
      * This should check if he arrived to the right place
      */
     public void checkMyLocation(View view){
     	if (flag) {
     		if (checkArea()){ //we are in the right location
     			// if false alert the client and return
     			flag = false;
     			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
     			View popupView = layoutInflater.inflate(R.layout.popup_window, null);  
     			final PopupWindow popupWindow = new PopupWindow(popupView, 500/*LayoutParams.WRAP_CONTENT*/, 800/*LayoutParams.WRAP_CONTENT*/); 
 
     			//setContentView(R.layout.popup_window);
     			bAnswer1 = (Button) popupView.findViewById(R.id.radioButton1);
     			bAnswer2 = (Button) popupView.findViewById(R.id.radioButton2);
     			bAnswer3 = (Button) popupView.findViewById(R.id.radioButton3);
     			bAnswer4 = (Button) popupView.findViewById(R.id.radioButton4);
     			bContinue = (Button) popupView.findViewById(R.id.cont);
     			dScore = (TextView) popupView.findViewById(R.id.score);
     			dQuestion = (TextView) popupView.findViewById(R.id.question);
     			bAnswer1.setText(answers[0]);
     			bAnswer2.setText(answers[1]);
     			bAnswer3.setText(answers[2]);
     			bAnswer4.setText(answers[3]);
     			dQuestion.setText("Your question is: " + question);
     			dScore.setText("Your current score is: " + score);
 
     			popupWindow.showAsDropDown(checkMyLocation, 100, 100 );
     			
     			bContinue.setOnClickListener(new View.OnClickListener() {
 
     				@Override
     				public void onClick(View v) {
     					if (answersStatus[0] == 3) {
     						popupWindow.dismiss();
     						nextPoint();
     					}
     					else {
     						dQuestion.setText("You haven't answered the question! The question is: " + question);
     					}
     				}
     			});
 
     		}
     		else { //not in the right location yet
     			Toast.makeText(this, "Not there yet, keep going!", Toast.LENGTH_LONG).show();
     		}
     	}
     }
     
     private boolean checkArea() {
     	Location myLocation = new Location("myLoc");
     	double distance = 0;        
         Location gpsLocation = requestUpdatesFromProvider(
                 LocationManager.GPS_PROVIDER, R.string.not_support_gps);
         Location networkLocation = requestUpdatesFromProvider(
                 LocationManager.NETWORK_PROVIDER, R.string.not_support_network);
 
         // If both providers return last known locations, compare the two and use the better
         // one to update the UI.  If only one provider returns a location, use it.
         if (gpsLocation != null && networkLocation != null) {
             myLocation=getBetterLocation(gpsLocation, networkLocation);
         } else if (gpsLocation != null) {
             myLocation=gpsLocation;
         } else if (networkLocation != null) {
             myLocation=networkLocation;
         } else{
         	myLocation.setLatitude(0.0);
         	myLocation.setLongitude(0.0);
         }
 
 
     	if(myLocation==null)
     		Log.d("myLocationis:", "null");
     	
     	distance = myLocation.distanceTo(currntPoint);
     	if (distance < THRESHOLD)
     		return true;
     	else
     		return false;
 	}
 
 	@Override
     public void onMapClick(LatLng point) {
         mTapTextView.setText("tapped, point=" + point);
     }
 
     @Override
     public void onMapLongClick(LatLng point) {
         mTapTextView.setText("long pressed, point=" + point);
     }
 
     @Override
     public void onCameraChange(final CameraPosition position) {
         mCameraTextView.setText(position.toString());
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_play_game, menu);
 		return true;
 	}
 	
 	public void resetAnswer() {
 		 int i;
 		 for (i = 0; i < 4; i++) {
 			 answersStatus[i] = 3;
 		 }
 	 }
 
 	public void suffleAnswers(Challenge curruntChallenge){
 		Random r = new SecureRandom();
 		int temp, i;
         temp = r.nextInt(4);
         question = curruntChallenge.getChallenge();
         answers[temp] = curruntChallenge.getRight_answer();
         answersStatus[temp] = 2;
         for (i = 0; i < 3; i++) {
         	temp = r.nextInt(4);
         	if (answersStatus[temp] == 3) {
         		answersStatus[temp] = 0;
         		answers[temp] = curruntChallenge.getWrong_answers(i);        		
         	}
         	else
         		i--;
         }
         flag = true;
         
     	currntPoint.setLatitude(curruntChallenge.getCheckpoint().latitude);
     	currntPoint.setLongitude(curruntChallenge.getCheckpoint().longitude);
         mMap.addMarker(new MarkerOptions().position(new LatLng(currntPoint.getLatitude(), currntPoint.getLongitude())).title("Your destination"));	
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
 	}
 
 	public void nextPoint() {
 		tScore.setText("Your score: " + score);
 		if (myIterator.hasNext()) {
 			if (myIterator==null) 
 				myIterator = myList.iterator();
 	        suffleAnswers(myIterator.next());
 		}
 			
 		else {
 			Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
 			gTimer.stop();
 			String fTime = calcTime();
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 			View popupView = layoutInflater.inflate(R.layout.end_game, null);  
 			final PopupWindow popupWindow = new PopupWindow(popupView, 500/*LayoutParams.WRAP_CONTENT*/, 800/*LayoutParams.WRAP_CONTENT*/); 
 			eScore = (TextView) popupView.findViewById(R.id.endScore);
 			eTime = (TextView) popupView.findViewById(R.id.endTime);
 			eTime.setTextColor(Color.BLUE);
 			eScore.setTextColor(Color.CYAN);
 			eTime.setText("You finshed the game in: " + fTime);
 			eScore.setText("With score of: " + score);
 
 			popupWindow.showAsDropDown(checkMyLocation, 100, 100 );
 			}		
 	}
 	
 	public String calcTime() {
 		int elapsedMillis = (int)((SystemClock.elapsedRealtime() - gTimer.getBase())/1000);
 		int sec = (elapsedMillis%60)%10;
 		int tenSec = (elapsedMillis%60)/10;
 		int min = ((elapsedMillis/60)%60)%10;
 		int tenMin = ((elapsedMillis/60)%60)/10;
 		int hr = elapsedMillis/3600;
 		String ans = hr + ":" + tenMin + min + ":" + tenSec + sec;
 		return ans;
 		
 	}
 	
 	public void answerClick(View view) {
 		int ans;
 		switch (view.getId()) {
 		case R.id.radioButton1:
 			ans = 0;
 			break;
 		case R.id.radioButton2:
 			ans = 1;
 			break;
 		case R.id.radioButton3:
 			ans = 2;
 			break;
 		default:
 			ans = 3;
 			break;
 		}
 
 		if (answersStatus[ans] == 0) {
 			counter++;
 			answersStatus[ans] = 1;
 			dQuestion.setText("Wrong answer! Please try again. The question is: " + question);	
 		}
 		else if (answersStatus[ans] == 1){
 			dQuestion.setText("You already tried this answer! Please try again. The question is: " + question);
 		}
 		else if (answersStatus[ans] == 2){
 			dQuestion.setText("Correct! You won " + bonus[counter] + " points! Now lets continue to the next checkpoint...");
 			score = score + bonus[counter];
 			dScore.setText("Your current score is: " + score + " points!");
 			counter = 0;
 			resetAnswer();
 		}
 	}	
 }
