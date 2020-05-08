 package org.jbs.happysad;
 
 import static android.provider.BaseColumns._ID;
 import static org.jbs.happysad.Constants.EMO;
 import static org.jbs.happysad.Constants.LAT;
 import static org.jbs.happysad.Constants.LONG;
 import static org.jbs.happysad.Constants.MSG;
 import static org.jbs.happysad.Constants.TABLE_NAME;
 import static org.jbs.happysad.Constants.TIME;
 import android.app.Activity;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class More extends Activity implements OnKeyListener, OnClickListener {
 	private static final String TAG = "there's more screen";
 
 	private static String[] FROM = { _ID, LAT, LONG, EMO, MSG, TIME, };
 	private static String ORDER_BY = TIME + " DESC";
 	private HappyData updates;
 	int emotion = -1;
 	String extradata;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		//basic stuff
 		Log.d(TAG, "entering oncreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.more);
 		//figure out whether they clicked happy or sad
 		Intent sender = getIntent();
 		extradata = sender.getExtras().getString("Clicked");
 		//emotion is an int, Clicked gets you a string
 		emotion = sender.getExtras().getInt("Emotion");
 		
 		//for now, we're showing "happy" or "sad" depending on what the previous click was.
 		TextView t = (TextView) findViewById(R.id.more_text);
 		t.setText(extradata);
 
 		//Setting up the layout etc
 		EditText textField = (EditText)findViewById(R.id.more_textbox);
 		textField.setOnKeyListener(this);
 		TextView locationView = (TextView) findViewById(R.id.location);
 		locationView.setText("unknown");
 
 		//now we're getting a handle on the database
 		updates = new HappyData(this);
 		
 		//setting up buttons
 		View submitButton = findViewById(R.id.more_to_dash);
 		submitButton.setOnClickListener(this);
 
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		Log.d(TAG, "creating a new location listner");
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location
 				// provider.
 				makeUseOfNewLocation(location);
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 			}
 
 			public void onProviderEnabled(String provider) {
 				
 			}
 
 			public void onProviderDisabled(String provider) {
 			}
 		};
 
 		// Register the listener with the Location Manager to receive location
 		// updates
 		Log.d(TAG, "Registration  of listener");
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,	0, locationListener);
 		
 		//SAHAR STORE FROM HERE!!
 		
 		try {
			Location location = new Location(LocationManager.GPS_PROVIDER);
 			double longitude = location.getLongitude();
 			double latitude = location.getLatitude();
 			makeUseOfNewLocation(location);
 		}
 		catch (Exception e){
 		// Remove the listener you previously added
 			Log.d(TAG, "Error");
 		
 		}
 		locationManager.removeUpdates(locationListener);
 	}
 
 	private void makeUseOfNewLocation(Location location) {
 		Log.d(TAG, "entering makeuseofnewlocatoin");
 		int x = 0;
 		System.out.println(x);
 		double longitude = location.getLongitude();
 		double latitude = location.getLatitude();
 		TextView locationView = (TextView) findViewById(R.id.location);
 		locationView.setText("unknown");
 		locationView.setText("lat = " + latitude + " long = " + longitude);
 		locationView.invalidate();
 	}
 
 	public void onClick(View v) {
 		Log.d(TAG, "clicked" + v.getId());
 		System.out.println(TAG + "clicked" + v.getId());
 		switch (v.getId()) {
 		case R.id.more_to_dash:
 			Intent i = new Intent(this, Dashboard.class);
 
 
 			String userstring = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
 			try {
 				saveUpdate(userstring); 
 			    
 			} finally {
 				updates.close(); 
 			}
 			i.putExtra("textboxmessage", userstring);
 			i.putExtra("happysaddata", extradata);
 			Log.d(TAG, "adding " + userstring + " to intent");
 			startActivity(i);
 
 			break;
 		}
 	}
 	
 	private void saveUpdate(String msg){
 		SQLiteDatabase db = updates.getWritableDatabase();
 		ContentValues values = basicValues(emotion);
 		values.put(MSG, msg);
 		db.insertOrThrow(TABLE_NAME, null, values);
 		Log.d(TAG, "saved update to db");
 		updates.close();
 	}
 
 	// got following code from
 	// http://stackoverflow.com/questions/2004344/android-edittext-imeoptions-done-track-finish-typing
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 
 		if ((event.getAction() == KeyEvent.ACTION_DOWN)
 				&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 			// Done pressed! Do something here.
 			EditText t = (EditText) v;
 			Log.d(TAG, "text entered: " + t.getText());
 			this.onClick(findViewById(R.id.more_to_dash));
 			// Intent i = new Intent(this, prompt.class);
 			// startActivity(i);
 
 		}
 		// Returning false allows other listeners to react to the press.
 		return false;
 	}
 
 	
 	private ContentValues basicValues(int emo){
 		//for basic updates	
 			
 			ContentValues values = new ContentValues();
 		    values.put(TIME, System.currentTimeMillis());
 		    //values.put(LAT, <latitude>);
 		    //values.put(LONG, <longitude>);
 		    values.put(EMO, emo);
 		    return values;
 		 
 		}
 
 }
