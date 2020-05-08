 package eu.attech.gpstracker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.WindowManager.LayoutParams;
 
 /**
  * 
  * @author Okermller Patrick
  * @web www.attech.eu
  * @email info@attech.eu
  * 
  */
 
 public class GPSTracker extends Activity {
 
 	// Create Local Variables
 	private GPS gps;
 	private Server server;
 
 	/*
 	 * Create an Object from GPS and use the Methods for calculating (Speed,
 	 * ...)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		gps = new GPS((LocationManager) getSystemService(LOCATION_SERVICE),
 				this);
 		server = new Server(this);
 		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep the
 																// Screen on
 		setContentView(R.layout.main);
 	}
 
 	/* Create the Menu for the app from the .xml file */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		return true;
 	}
 
 	/* Is called when the user touched one Menu item */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.close:
 			exit();
 			return true;
 		case R.id.logout:
 			server.logout();
 			return true;
 		case R.id.menu_settings:
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/* Remove the Update Hamdler, if the app was Paused */
 	@Override
 	protected void onPause() {
		gps.getLocationManager().removeUpdates(gps); // We would not to remove
														// the GPS if the
														// Display is off
 		super.onPause();
 	}
 
 	/* Enable GPS Search if the app was Resumed */
 	@Override
 	protected void onResume() {
 		gps.startSearchGPS();
 		super.onResume();
 	}

 	/* Is the Last Method wich is called */
 	public void exit() {
 		gps.getLocationManager().removeUpdates(gps);
 		System.exit(0);
 	}
 }
