 package com.finlay.geomonsters;
 
 import java.net.MalformedURLException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import io.socket.SocketIO;
 
 import com.finlay.geomonsters.R;
 import com.finlay.geomonsters.battle.BattleActivity;
 import com.finlay.geomonsters.battle.ResourceManager;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main Activity that app begins at
  * @author James
  *
  */
 public class MainActivity extends Activity implements LocationListenerParent {
 
 	private static final String TAG = "MainActivity";
 
 	private static final String URL = "http://204.191.142.13:8000/";
 
 	private static final int UPDATE_DELAY = 1000; 
 
 	private Button forceButton = null;
 	private Button waitButton = null;
 	private Button loadEncounterButton = null;
 	private TextView theTextView = null;
 
 	private SocketIO socket;
 	private WeatherManager weatherManager = null;
 	private Weather weatherData = null;
 	private Timer timer;
 
 	// TODO: These are for the 'force button,' so should eventually get rid of
 	private LocationManager locationManager;
 	private MyLocationListener locationListener;
 
 	private MainActivity _activity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.v(TAG, "onCreated");
 		super.onCreate(savedInstanceState);
 		_activity = this;
 
 		// set app to fullscreen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// layout
		setContentView(R.layout.activity_main);
 
 		// Socket, Location Manager, Weather Manager
 		locationListener = new MyLocationListener(this);
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		socket = new SocketIO();
 		weatherManager = new WeatherManager(this);
 
 		// layout items
 		theTextView = (TextView) findViewById(R.id.txtMessage);
 		forceButton = (Button) findViewById(R.id.btnGetLocation);
 		waitButton = (Button) findViewById(R.id.btnWaitLocation);
 		loadEncounterButton = (Button) findViewById(R.id.btnLoadEncounter);
 
 		// TODO get rid of this. For now, reset the encounters file whenever created
 		ConfigManager.ResetConfigFiles(getApplicationContext());
 
 		forceButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Log.v(TAG, "Force Click");
 
 				// Ensure connected
 				if (!isNetworkAvailable()) {
 					setMessage("No internet connection.");
 					return;
 				}
 				
 				forceButton.setText("...");
 				
 				// Connect to server
 				connectSocket();
 
 				// Best provider
 				Criteria criteria = new Criteria();
 				String bestProvider = locationManager.getBestProvider(criteria, false);
 
 				// Request location updates
 				locationManager.requestLocationUpdates(bestProvider, 10000, 0, locationListener);
 
 				forceButton.setEnabled(false);
 				loadEncounterButton.setEnabled(false);
 			}
 		});
 
 		waitButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Log.v(TAG, "Wait click");
 				waitButton.setText("...");
 
 				// Start Encounter Service
 				// TODO Service should be started at boot?
 				_activity.startService(new Intent(_activity, EncounterService.class));
 
 				waitButton.setEnabled(false);
 			}
 
 		});
 
 		loadEncounterButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Log.v(TAG, "Load encounter clicked");
 				
 				// Ensure connection
 				if (!isNetworkAvailable()) {
 					setMessage("No internet connection.");
 					return;
 				}				
 				
 				// Pull encounter
 				String encounter = ConfigManager.PullEncounter(getApplicationContext());
 				if (encounter.equals("")) return;
 				
 				// Use location & time from gathered string to query encounter
 				String[] location = encounter.split(",");
 				String latitude = location[0];
 				String longitude = location[1];
 				// TODO We cannot get historical weatherdata accurately. So we will just use the current time :(
 				
 				// Pop used encounter from queue
 				ConfigManager.PopEncounter(getApplicationContext());
 				
 				// Server & weather
 				connectSocket();
 				weatherManager.execute(longitude, latitude);
 				while (!socket.isConnected()) ;
 				
 				// Send query to server
 				sendLocation(longitude, latitude);
 			}			
 		});
 
 		// Change the text value of the loadEncounterButton to the number of encounters available
 		timer = new Timer();
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						loadEncounterButton.setText("" + ConfigManager.EncounterCount(getApplicationContext()));
 					}
 				});
 			}
 		}, 0, UPDATE_DELAY);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.v(TAG, "onStop");
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.v(TAG, "onDestroy");
 		socket.disconnect();
 		super.onDestroy();
 	}
 
 	public void launchBattle(String s) {
 		
 		// Wait for weather data
 		Log.v(TAG, "Wait for weatherData..");
 		while (weatherData == null) ;
 
 		try {
 			Intent intent = new Intent(this, BattleActivity.class);
 			// TODO: Send more than just enemy name through. Ex: Lvl, Attributes, etc.
 			intent.putExtra("ENEMYNAME", ResourceManager.getCreatureEncounter(getResources(), s, weatherData));
 
 			// disconnect from server
 			socket.disconnect();
 
 			// start the battle activity
 			startActivity(intent);
 
 		} catch (Exception e) {
 			Log.e(TAG, "launchBattle: " + e.getMessage());
 		}
 	}
 
 	public void appendMessage(final String s) {
 		// append string as new line to the TextView.
 		// Since TextView is part of UI, we need to have another thread queue the action
 		runOnUiThread(new Runnable() {
 			public void run() {
 				theTextView.append(s + "\n");
 			}
 		});
 	}
 	public void setMessage(final String s) {
 		// append string as new line to the TextView.
 		// Since TextView is part of UI, we need to have another thread queue the action
 		runOnUiThread(new Runnable() {
 			public void run() {
 				theTextView.setText(s + "\n");
 			}
 		});
 	}
 
 	public void LocationChanged(Location loc) {
 		// LocationManager has received GPS location
 		// This is only used with the 'Force Location' button
 
 		String longitude ="" + loc.getLongitude();
 		String latitude = "" + loc.getLatitude();
 
 		appendMessage("Location changed: " + longitude + ", " + latitude);
 
 		weatherManager.execute(longitude, latitude);
 		sendLocation(longitude, latitude);
 	}
 
 	public void connectSocket() {
 		// Connect to the socket
 		if (!socket.isConnected()) {
 			try {
 				socket.connect(URL, new MyIOCallback(this));
 			} catch (MalformedURLException e) {
 				Log.e(TAG, e.getMessage());
 			}
 		}
 	}
 	public void sendLocation(String longitude, String latitude) {
 		// Creates JSON object containing given location and sends
 		// to the connected server.
 
 		if (!socket.isConnected()) {
 			Log.w(TAG, "Socket is not connected. Could not send location.");
 			return;
 		}
 
 		try {
 
 			JSONObject json = new JSONObject();
 			json.putOpt("longitude", longitude);
 			json.putOpt("latitude", latitude);
 
 			socket.emit("user position", json);
 
 		} catch (JSONException e) {
 			Log.e(TAG, "sendLocation: " + e.getMessage());
 		}
 	}
 	public void setWeatherData(Weather weather) {
 		Log.v(TAG, "Weather data received.");
 		weatherData = weather;
 		appendMessage("Weather data received.");
		if (weatherData == null) Log.v(TAG, "Returned null :(");
 	}
 
 	//TODO: Once battle is done, get rid of this? We probably only want
 	// the service getting encounters.
 	@Override
 	public void locationFound(Location loc) {
 		locationManager.removeUpdates(locationListener);
 		// Make toast
 		String s = "F: " + loc.getLatitude() + ", " + loc.getLongitude();
 		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
 
 		weatherManager.execute(""+loc.getLongitude(), ""+loc.getLatitude(), true);
 		// ensure server connected
 		while (!socket.isConnected()) ;
 		sendLocation("" + loc.getLongitude(), "" + loc.getLatitude());
 	}
 	
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		
 		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 	}
 }
 
