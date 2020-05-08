 package app.timer;
 
 import java.util.Calendar;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.widget.Chronometer;
 
 /**
  * The main application class where the chronometer is situated.
  * This class delegates responsibilities to the rest of the application 
  * @author ako
  *
  */
 public class TimerApplication extends Application implements OnSharedPreferenceChangeListener  {
 
 	// Array used for choosing among beeptimes
 	private int [] beepTimeArray = {15,30,60,120,180,300}; 	
 	// The global Chronometer in the application
 	private Chronometer global_chrono;
 	
 	// Fields covering most of the preferences
 	SharedPreferences prefs;
 	private int formats;
 	private boolean sound;
 	private boolean realTime;
 	private int beepTime;
	static MediaPlayer soundPlayer; 
 
 	/**
 	 * This method is only called once at the initiation of the app
 	 * 
 	 */
 	public void onCreate() {
 		super.onCreate();
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
		soundPlayer = MediaPlayer.create(getBaseContext(), R.raw.silvaclock);
 		realTime = true;
 		global_chrono = new Chronometer(getBaseContext());
 		initiateClock();
 	}
 	
 	/**
 	 * This method is called every time a preference is changed.
 	 * Some of the changes are dealt with here other screen related stuff
 	 * in the onResume in TimerActivity
 	 */
 	@SuppressWarnings("deprecation")
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		realTime = prefs.getBoolean("real_time", true);
 		
 		if(!realTime && ((key.equals("synchronize.hour")) || key.equals("synchronize.minute"))) {
 			global_chrono.setBase(SystemClock.elapsedRealtime() - 
 					(prefs.getInt("synchronize.hour", 0)*3600000 + prefs.getInt("synchronize.minute", 0)*60000));
 		}
 		else if(realTime) {
 			initiateClock();
 		}
 	}
 	
 	public Chronometer getGlobalChronometer() {
 		return global_chrono;
 	}
 	
 	/**
 	 * This method packs a chronometerlistener with all the preference chosen by the user
 	 * The SpecificChronometerTickListener has the responsibility to assemble a listener
 	 * of all the different parameters 
 	 */
 	public void editListener() {
 		beepTime = beepTimeArray[Integer.parseInt(prefs.getString("beep_interval", "2"))];
 		formats = Integer.parseInt(prefs.getString("format", "2"));
 		sound = !prefs.getBoolean("mute" , false);
 		global_chrono.setOnChronometerTickListener(new SpecificChronometerTickListener(getBaseContext(), beepTime, sound, formats));
 	}
 	
 	public boolean getRealTimeMode() {
 		return realTime;
 	}
 	
 	/**
 	 * The initiation of the clock is in real Time
 	 */
 	public void initiateClock() {
 		Calendar rightNow = Calendar.getInstance();
 		rightNow = Calendar.getInstance();
 		global_chrono.setBase(SystemClock.elapsedRealtime() - 
 				(rightNow.get(Calendar.HOUR_OF_DAY)*3600000 + rightNow.get(Calendar.MINUTE)*60000 + rightNow.get(Calendar.SECOND)*1000));		
 		global_chrono.start();
 	}
 }
