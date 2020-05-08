 package tb14.walkbasehackathon;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import tb14.walkbasehackathon.DTO.Location;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 import com.walkbase.location.WBLocation;
 import com.walkbase.location.WBLocationManager;
 import com.walkbase.location.listeners.WBLocationListener;
 
 public class MainActivity extends Activity implements WBLocationListener{
 	private final String TAG = "Main_Activity";
 	private MediaPlayer mediaPlayer;
 	private WBLocationManager locationmanager;
 	
 	private Location latestLocation;
 	private ArrayAdapter<Location> adapter;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		mediaPlayer = MediaPlayer.create(getApplicationContext(),
 				R.raw.imperial);
 		locationmanager = WBLocationManager.getWBLocationManager();
 		locationmanager.setApiKey("9ew2ucuohe67381nbwbfbw9sbb9");
 		locationmanager.setWBLocationListener(this);
 		latestLocation = new Location();
 
 		//ActionBar gets initiated
 		ActionBar actionbar = getActionBar();
 		//Tell the ActionBar we want to use Tabs.
 		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		//initiating both tabs and set text to it.
 		ActionBar.Tab InfoTab = actionbar.newTab().setText("Info");
 		ActionBar.Tab SavedLocationsTab = actionbar.newTab().setText("Saved Locations");
 		ActionBar.Tab MagicTab = actionbar.newTab().setText("MAGIC!");
 
 		//create the two fragments we want to use for display content
 		Fragment InfoFragment = new Tab_Info();
 		Fragment SavedLocationFragment = new Tab_SavedLocations();
 		Fragment MagicFragment = new Tab_Magic();
 
 		//set the Tab listener. Now we can listen for clicks.
 		InfoTab.setTabListener(new OurTabListener(InfoFragment));
 		SavedLocationsTab.setTabListener(new OurTabListener(SavedLocationFragment));
 		MagicTab.setTabListener(new OurTabListener(MagicFragment));
 
 		//add the two tabs to the actionbar
 		actionbar.addTab(InfoTab);
 		actionbar.addTab(SavedLocationsTab);
 		actionbar.addTab(MagicTab);
 		
 		
		LocationUpdater locationupdater = new LocationUpdater();
		locationupdater.setAlarm(this);
 
 
 	}
 
 
 
 	public void getLastKnownLocation(View v) {
 		locationmanager.fetchLastKnownUserLocation(v.getContext());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void lastKnownLocationWasRetrieved(WBLocation wbLocation) {
 		Log.v(TAG, "Last known location retrieved");
 		double flon = 22.293625;
 		double flat = 60.450202;
 
 		populateLocationFromWBLocation(wbLocation);
 		Log.v(TAG,"Latitude: "+String.valueOf(latestLocation.getLatitude() ));
 		Log.v(TAG,"Longitude: "+String.valueOf(latestLocation.getLongitude() ));
 		double distance = gps2m(flat, flon, latestLocation.getLatitude(),
 				latestLocation.getLongitude());
 
 		Message msg = new Message();
 		Date date = latestLocation.getTimestamp();
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy HH:mm:ss",Locale.getDefault());
 		msg.obj = Double.toString(distance)+" "+dateFormat.format(date);
 
 		// TODO Auto-generated method stub
 	}
 
 	private void populateLocationFromWBLocation(WBLocation wbLocation) {
 		latestLocation.setLatitude(wbLocation.getLatitude());
 		latestLocation.setLongitude(wbLocation.getLongitude());
 		latestLocation.setAccuracy(wbLocation.getAccuracy());
 		Long timeStampInMicroSeconds = wbLocation.getTimestamp()/1000;	
 
 		latestLocation.setTimestamp(new Date(timeStampInMicroSeconds));
 
 	}
 
 
 	@Override
 	public void errorFetchingLastKnownLocation(String arg0, int arg1) {
 		Log.v(TAG, "error fetching last known location");
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void successfullyStartedTheLiveLocationFeed() {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void liveLocationFeedWasClosed() {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void liveLocationWasUpdated(WBLocation arg0) {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	@Override
 	public void errorFetchingLiveLocationFeed(String arg0, int arg1) {
 		// NOT FUNCTIONING = NOT USING
 	}
 
 	private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
 		double pk = (180 / 3.14169);
 
 		double a1 = lat_a / pk;
 		double a2 = lng_a / pk;
 		double b1 = lat_b / pk;
 		double b2 = lng_b / pk;
 		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
 		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
 		double t3 = Math.sin(a1) * Math.sin(b1);
 		double tt = Math.acos(t1 + t2 + t3);
 
 		return 6366000 * tt;
 	}
 
 
 
 }
