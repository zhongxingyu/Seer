 package com.makrijah.geotrack;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * Simple SplashScreen class
  * @author makrijah
  *
  */
 public class SplashActivity extends FragmentActivity {
 
 	private SplashLocator locator;
 	private double latitude;
 	private double longitude;
 	private Settings settings;
 	public static final int RETURN_GPS_OK = 1;
 	public static final int RETURN_GPS_NOT_OK = 0;
 	private Handler timeHandler;
 	private Runnable time;
 	private final long maxTryingTime = 90000; 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setFullscreen();
 
 		setContentView(R.layout.splash);
 
 		settings = (Settings) getLastCustomNonConfigurationInstance();
 		if (settings==null) settings = new Settings();
 		if (!settings.started) startGPS(); 
 
 		//Register broadcastreceiver
 		if (locator == null) locator = new SplashLocator();
 		final IntentFilter filter = new IntentFilter();
 		filter.addAction("com.makrijah.geotrack.positions");
 		registerReceiver(locator, filter);
 		
		timeHandler = new Handler();				
		time = new Runnable() {			
			public void run() {
				popUpNoGPS();
			}
		};		
		timeHandler.postDelayed(time, maxTryingTime);		
 	}
 
 	/**
 	 * Pops up a message, that no GPS signal can be received.
 	 */
 	private void popUpNoGPS(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.gpsAlert);		
 		builder.setMessage(R.string.gpsNoSignal)
 		.setCancelable(true)
 		.setNeutralButton("Quit", new DialogInterface.OnClickListener() {			
 			public void onClick(DialogInterface dialog, int which) {
 				quit();
 				dialog.cancel();
 				
 			}
 		});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 	
	/**
	 * Quits this activity. Called when GPS signal is not received although 
	 * GPS is enabled in the system. 
	 */
 	private void quit(){
		unregisterReceiver(locator);
 		this.finish();
 	}
 	
 	/**
 	 * Keep the configuration
 	 */
 	@Override
 	public Object onRetainCustomNonConfigurationInstance(){
 		return settings;
 	}
 	
 	/**
 	 * Starts GPS service (LocationService)
 	 */
 	private void startGPS(){
 		startService(new Intent(getApplicationContext(), LocationService.class));
 		settings.started = true;
 	}
 	
 	/**
 	 * Set the Splashscreen to fullscreen
 	 */
 	private void setFullscreen(){		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	}
 	
 	/**
 	 * Disables menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//getMenuInflater().inflate(R.menu.activity_splash, menu);
 		return true;
 	}
 
 	/**
 	 * Returns OK to calling activity
 	 * @param latitude latitude of a received location
 	 * @param longitude longitude
 	 */
 	private void gpsOK(double latitude, double longitude){
 		Intent returnIntent = new Intent();
 		returnIntent.putExtra("latitude", latitude);
 		returnIntent.putExtra("longitude", longitude);
 		setResult(RETURN_GPS_OK, returnIntent);		
 		finish();
 	}
 
 	/**
 	 * Returns failure to calling activity
 	 */
 	private void gpsNotOK(){
 		Intent returnIntent = new Intent();
 		setResult(RETURN_GPS_NOT_OK, returnIntent);
 		finish();
 	}
 	
 	/**
 	 * Disables back button
 	 */
 	@Override
 	public void onBackPressed(){
 		
 	}
 
 
 	/**
 	 * Private inner BroadcastReceiver for LocationService
 	 * Can access members of outer class
 	 * @author makrijah
 	 *
 	 */
 	private class SplashLocator extends Locator{
 
 		/**
 		 * Called on receiving a broadcast
 		 */
 		@Override
 		public void onReceive(Context context, Intent intent){
 			Bundle msg = intent.getExtras();
 			if (msg!=null){
 				if (msg.getBoolean("gpsEnabled")){
 					if (msg.getBoolean("gotGPS")){
 						latitude = msg.getDouble("latitude");
 						longitude = msg.getDouble("longitude");
						timeHandler.removeCallbacks(time);
 						gpsOK(latitude, longitude);
 					}
 				}
 				else {
 					gpsNotOK();
 				}
 			}
 		}
 
 	}
 	
 	/**
 	 * Inner class for saving last configuration
 	 * @author makrijah
 	 *
 	 */
 	private class Settings{
 		public boolean started = false;
 	}
 }
