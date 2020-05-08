 package edu.calpoly.ai.skynest;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MainActivity extends SherlockFragmentActivity{
 	
 	private final static String PROX_ALERT_INTENT = "edu.calpoly.ai.skynest.ProximityAlert";
 	private final static long POINT_RADIUS = 200; // in Meters
 	public final static String PREF_FILE = "Preferences";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (hasHomeLocation()) {
 			Intent intent = new Intent(PROX_ALERT_INTENT);
         	PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
 
         	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         	lm.addProximityAlert(getHomeLatitude(), getHomeLongitude(),
             	POINT_RADIUS, -1, proximityIntent);
             	
        		IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT); 
        		registerReceiver(new LocationBroadcastReceiver(), filter);
 		}
 		else {
			//startMapsActivity();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = this.getSupportMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 			case R.id.menu_maps:
 				startMapsActivity();
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 	
 	private boolean hasHomeLocation(){
		SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
 		if (sp != null &&
 			sp.contains(MapsActivity.HOME_LAT) &&
 			sp.contains(MapsActivity.HOME_LNG))
 			return true;
 		return false;
 	}
 	
 	private long getHomeLatitude(){
 		SharedPreferences sp = this.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
 		if(sp != null && sp.contains(MapsActivity.HOME_LAT))
 			return sp.getLong(MapsActivity.HOME_LAT, (long) 0);
 		else return 0;
 	}
 	
 	private long getHomeLongitude(){
 		SharedPreferences sp = this.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
 		if(sp != null && sp.contains(MapsActivity.HOME_LNG))
 			return sp.getLong(MapsActivity.HOME_LNG, (long) 0);
 		else return 0;
 	}
 	
 	private void startMapsActivity(){
 		Intent myIntent = new Intent(getApplicationContext(), MapsActivity.class);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(myIntent);
 	}
 
 }
