 package edu.dartmouth.cs.tractable;
 
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.RatingBar;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class ManualInputActivity extends Activity {
 
 	// Exercise entry
 	public BathroomSessionHelper mEntry;
 	
 	public LocationManager mLocationManager;
 	private double latitude;
 	private double longitude;
 	
 	public String mBathroomName;
 	private HashMap<String, String> bathroomMap;
 	
 	public TextView bathroomNameView;
 
 	
 	public static final int LIST_ITEM_ID_DATE = 0;
 	public static final int LIST_ITEM_ID_TIME = 1;
 	public static final int LIST_ITEM_ID_DURATION = 2;
 	public Uri manualInputURI;
 
 	// skeleton
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		
 		registerReceiver(locationReceiver, new IntentFilter("bio_location"));
 		
 		// Populate bathroom name hashmap
 		bathroomMap = new HashMap<String, String>();
 		bathroomMap.put("newhamp-2-4-ap", "New Hampshire Hall First Floor");
 		bathroomMap.put("sudikoff-1-9-ap", "Sudikoff Basement");
		bathroomMap.put("vail-6-4-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-4-14-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-3-13-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-4-15-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("dana-3-1-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-3-12-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-2-14-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-2-1-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-3-2-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-2-10-ap", "Life Sciences Center Second Floor");
		bathroomMap.put("lsb-3-1-ap", "Life Sciences Center Second Floor");
 
 		// Setting the UI layout
 		setContentView(R.layout.manualinput);
 
 		// Initialize the ExerciseEntryHelper()
 		mEntry = new BathroomSessionHelper();
 		
 		// get the duration from the timer and save it into the entry
 		int duration = getIntent().getIntExtra(Globals.KEY_DURATION, -1);
 		Log.e("tractable", "in manualinputactivity duration = " + String.valueOf(duration));
 		mEntry.setDuration(duration);
 		
 		
 		// Location manager for getting wifi location
 		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		
 		// Get the bathroom text view
 		bathroomNameView = (TextView) findViewById(R.id.bathroom_name);
 	}
 	
 	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle data = intent.getExtras();
 			String wifiData = data.getString("key_bio_location", "");
 			mBathroomName = getBathroomName(wifiData.split(";")[0].split(",")[1]);
 			bathroomNameView.setText(mBathroomName);
 		}
 	};
 	
 	private String getBathroomName(String wifiName) {
 		return bathroomMap.get(wifiName);
 	}
 
 	// "Save" button is clicked
 	public void onSaveClicked(View v) {
 		
 		setBuilding();
 		setBathroomQuality();
 		setExperienceQuality();
 		setComment();
 		setLocation();
 
 		// Insert the exercise entry into database
 		mEntry.insertToDB(this);
 
 		Toast.makeText(getApplicationContext(), "Entry Saved", Toast.LENGTH_SHORT).show();
 		// Close the activity and go back to start page
 		Intent intent = new Intent(this, MainActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 	//skeleton
 	// "Cancel" button is clicked
 	public void onCancelClicked(View v) {
 		// Pop up a toast, discard the input and close the activity directly
 		// Making a "toast" informing the user changes are canceled.
 		Toast.makeText(getApplicationContext(),
 				"Canceled",
 				Toast.LENGTH_SHORT).show();
 		// Close the activity and go back to start page
 		Intent intent = new Intent(this, MainActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 
 	// Setters for mEntry fields
 	public void setLocation() {
 		
 		String provider = mLocationManager.NETWORK_PROVIDER;
 		Location l = mLocationManager.getLastKnownLocation(provider);
 		latitude = l.getLatitude();
 		longitude = l.getLongitude();
 		LatLng latlng = new LatLng(latitude, longitude);
 		mEntry.setLocation(latlng);
 		
 	}
 	
 	public void setBuilding() {
 		mEntry.setBuilding(mBathroomName);
 	}
 	
 	public void setBathroomQuality() {
 		RatingBar rat = (RatingBar) findViewById(R.id.ratingBathroom);
 		double stars = rat.getRating();
 		mEntry.setBathroomQuality(stars);
 	}
 	
 	public void setExperienceQuality() {
 		SeekBar seek = (SeekBar) findViewById(R.id.seekExperience);
 		int progress = seek.getProgress();
 		mEntry.setExperienceQuality(progress);
 	}
 
 	public void setComment() {
 		EditText et = (EditText) findViewById(R.id.editComment);
 		String comment = et.getText().toString();
 		mEntry.setComment(comment);
 	}
 }
