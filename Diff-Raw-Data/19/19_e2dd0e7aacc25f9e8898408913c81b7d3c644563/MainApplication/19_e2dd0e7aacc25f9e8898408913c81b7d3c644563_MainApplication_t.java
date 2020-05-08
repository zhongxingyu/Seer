 package de.unidue.wiwi.tdr.kn3.rasp_home;
 
 import android.app.Application;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class MainApplication extends Application {
 
 	public static final String RH_TAG = "RH";
 	public static Intent positioningService;
 	public static Intent communicationService;
 	public static SharedPreferences pref;
 	public static DatabaseClass database;
 	public static CommunicationClass com;
 	public static PositioningClass pos;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(RH_TAG, "Start Application");
 		positioningService = new Intent(this, PositioningService.class);
 		communicationService = new Intent(this, CommunicationService.class);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		pref = PreferenceManager.getDefaultSharedPreferences(this);
 		database = new DatabaseClass();
 		com = new CommunicationClass(this);
		//TODO Static IP
		com.client.SetServerIpPort("132.252.250.102:8091");
		com.client.SetUserPass("admin:admin");
 		pos = PositioningClass.loadPositions(this, "pref_positioning_calibrate");
 		if (pref.getBoolean("pref_positioning_tracking", false)) {
 			startService(positioningService);
 		}
 		startService(communicationService);
 	}
 
 }
