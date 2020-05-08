 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import org.wahtod.wififixer.PrefConstants.Pref;
 import org.wahtod.wififixer.ScreenStateHandler.OnScreenStateChangedListener;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 
 public class WifiFixerService extends Service implements
 	OnScreenStateChangedListener {
 
     // IDs For notifications
     private static final int NOTIFID = 31337;
     // Screen State SharedPref key
     public static final String SCREENOFF = "SCREENOFF";
 
     // *****************************
     final static String APP_NAME = "WifiFixerService";
     private final static String EMPTYSTRING = "";
 
     // Flags
     private static boolean registered = false;
 
     // logging flag, local for performance
     private static boolean logging = false;
 
     // Locks and such
 
     private static boolean shouldrun = true;
 
     // Version
     private static int version = 0;
 
     private WakeLock wakelock;
     private WFConnection wifi;
     private static ScreenStateHandler screenstateHandler;
     /*
      * Cache context for notifications
      */
     private Context notifcontext;
 
     static boolean screenstate;
 
     /*
      * Preferences
      */
     static PrefUtil prefs;
 
     private static void refreshWidget(final Context context) {
 	Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 	/*
 	 * Why would anyone possibly want more than 3? Hell, why would anyone
 	 * want 3?
 	 */
 	int[] widgetids = { 0, 1, 2 };
 	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetids);
 	intent.setClass(context, FixerWidget.class);
 	context.sendBroadcast(intent);
     }
 
     private void cleanup() {
 	wakelock.lock(false);
 	screenstateHandler.unregister(this);
     }
 
     private void getPackageInfo() {
 	PackageManager pm = getPackageManager();
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo(getString(R.string.packagename),
 		    0);
 	    // ---display the versioncode--
 	    version = pi.versionCode;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * If own package isn't found, something is horribly wrong.
 	     */
 	}
     }
 
     private void handleStart(final Intent intent) {
 
 	/*
 	 * If shouldrun is false we're being restarted so also start main tick
 	 */
 	if (!shouldrun) {
 	    shouldrun = true;
 	    /*
 	     * Start Main tick
 	     */
 	    // hMain.sendEmptyMessage(MAIN);
 	}
 	if (intent != null && logging)
 	    LogService.log(this, APP_NAME,
 		    getString(R.string.normal_startup_or_reload));
     }
 
     @Override
     public IBinder onBind(Intent intent) {
 	if (logging)
 	    LogService.log(this, APP_NAME, getString(R.string.onbind_intent)
 		    + intent.toString());
 	return null;
     }
 
     @Override
     public void onCreate() {
 	super.onCreate();
 	/*
 	 * Cache context for notifications
 	 */
 	notifcontext = this;
 
 	// Initialize WakeLock
 	wakelock = new WakeLock(this) {
 
 	    @Override
 	    public void onAcquire() {
 		if (logging)
 		    LogService.log(getBaseContext(), APP_NAME,
 			    getString(R.string.acquiring_wake_lock));
 		super.onAcquire();
 	    }
 
 	    @Override
 	    public void onRelease() {
 		if (logging)
 		    LogService.log(getBaseContext(), APP_NAME,
 			    getString(R.string.releasing_wake_lock));
 		super.onRelease();
 	    }
 
 	};
 
 	getPackageInfo();
 
 	if (logging) {
 	    LogService.log(this, APP_NAME,
 		    getString(R.string.wififixerservice_build) + version);
 	}
 
 	/*
 	 * Load Preferences
 	 */
 	preferenceInitialize(this);
 
 	/*
 	 * If BG data is off, we should quit.
 	 */
 	WFConnection.checkBackgroundDataSetting(this);
 
 	/*
 	 * Set initial screen state
 	 */
 	setInitialScreenState(this);
 
 	/*
 	 * Refresh Widget
 	 */
 	refreshWidget(this);
 
 	/*
 	 * Initialize Wifi Connection class
 	 */
 	wifi = new WFConnection(this, prefs);
 
 	/*
 	 * Start Service watchdog alarm
 	 */
 	if (!ServiceAlarm.alarmExists(this))
 	    ServiceAlarm.setAlarm(this, true);
 	/*
 	 * Set registered flag true so unregister code runs later
 	 */
 	registered = true;
 
 	if (logging)
 	    LogService.log(this, APP_NAME, getString(R.string.oncreate));
 
     }
 
     @Override
     public void onDestroy() {
 	super.onDestroy();
 	shouldrun = false;
 	unregisterReceivers();
 	if (prefs.getFlag(Pref.STATENOT_KEY))
 	    wifi.setStatNotif(false);
 	if (logging)
 	    LogService.log(this, APP_NAME, getString(R.string.ondestroy));
 	cleanup();
 
     }
 
     private void onScreenOff() {
 
 	/*
 	 * Set shared pref state for non-Eclair clients
 	 */
	PrefUtil.writeBoolean(this, SCREENOFF, true);
 	screenstate = false;
     }
 
     private void onScreenOn() {
 
 	/*
 	 * Set shared pref state
 	 */
	PrefUtil.writeBoolean(this, SCREENOFF, false);
 
 	screenstate = true;
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
 	handleStart(intent);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 	handleStart(intent);
 	return START_STICKY;
     }
 
     @Override
     public void onScreenStateChanged(boolean state) {
 	if (state)
 	    onScreenOn();
 	else
 	    onScreenOff();
 	/*
 	 * Notify instance of WFConnection
 	 */
 	wifi.onScreenStateChanged(state);
     }
 
     private void preferenceInitialize(final Context context) {
 	prefs = new PrefUtil(this) {
 	    @Override
 	    public void log() {
 		if (logging) {
 		    LogService.log(getBaseContext(), APP_NAME, getBaseContext()
 			    .getString(R.string.loading_settings));
 		    for (Pref prefkey : Pref.values()) {
 			if (getFlag(prefkey))
 			    LogService.log(getBaseContext(), APP_NAME, prefkey
 				    .key());
 		    }
 
 		}
 	    }
 
 	    @Override
 	    public void postValChanged(final Pref p) {
 		switch (p) {
 
 		case WIFILOCK_KEY:
 		    if (wifi != null && prefs.getFlag(Pref.WIFILOCK_KEY)) {
 			// generate new lock
 			wifi.wifiLock(true);
 		    } else if (wifi != null
 			    && !prefs.getFlag(Pref.WIFILOCK_KEY)) {
 			wifi.wifiLock(false);
 		    }
 		    break;
 
 		case LOG_KEY:
 		    logging = getFlag(Pref.LOG_KEY);
 		    ServiceAlarm.setLogTS(getBaseContext(), logging, 0);
 		    if (logging) {
 			LogService.log(getBaseContext(), LogService.DUMPBUILD,
 				EMPTYSTRING);
 			log();
 		    }
 		    break;
 
 		case NETNOT_KEY:
 		    /*
 		     * Disable notification if pref changed to false
 		     */
 		    if (!getFlag(Pref.NETNOT_KEY))
 			NotifUtil.addNetNotif(getBaseContext(), EMPTYSTRING,
 				EMPTYSTRING);
 
 		    break;
 
 		case STATENOT_KEY:
 		    /*
 		     * Notify WFConnection instance to create/destroy ongoing
 		     * status notification
 		     */
 		    wifi.setStatNotif(getFlag(Pref.STATENOT_KEY));
 		    break;
 		}
 
 		/*
 		 * Log change of preference state
 		 */
 		if (logging)
 		    LogService.log(getBaseContext(), APP_NAME,
 			    getString(R.string.prefs_change) + p.key()
 				    + getString(R.string.colon) + getFlag(p));
 	    }
 
 	    @Override
 	    public void preLoad() {
 
 		/*
 		 * Set defaults. Doing here instead of activity because service
 		 * may be started first due to boot intent.
 		 */
 		PreferenceManager.setDefaultValues(context, R.xml.preferences,
 			false);
 
 		/*
 		 * Sets default for Supplicant Fix pref on < 2.0 to true
 		 */
 
 		if (!readBoolean(context, PrefConstants.SUPFIX_DEFAULT)) {
 		    writeBoolean(context, PrefConstants.SUPFIX_DEFAULT, true);
 		    int ver;
 		    try {
 			ver = Integer.valueOf(Build.VERSION.RELEASE.substring(
 				0, 1));
 		    } catch (NumberFormatException e) {
 			ver = 0;
 		    }
 		    if (logging)
 			LogService.log(getBaseContext(), APP_NAME,
 				getBaseContext().getString(R.string.version)
 					+ ver);
 		    if (ver < 2) {
 			writeBoolean(context, Pref.SUPFIX_KEY.key(), true);
 		    }
 
 		}
 	    }
 
 	    @Override
 	    public void specialCase() {
 		postValChanged(Pref.LOG_KEY);
 		postValChanged(Pref.WIFILOCK_KEY);
 
 	    }
 	};
 
 	prefs.loadPrefs();
 	NotifUtil.cancel(NOTIFID, notifcontext);
 	wakelock.lock(false);
 
     }
 
     private void setInitialScreenState(final Context context) {
 	screenstateHandler = new ScreenStateHandler(this);
 	screenstate = ScreenStateHandler.getScreenState(context);
 	ScreenStateHandler.setOnScreenStateChangedListener(this);
     }
 
     private void unregisterReceivers() {
 	if (registered) {
 	    prefs.unRegisterReciever();
 	    screenstateHandler.unregister(this);
 	    wifi.cleanup();
 	    registered = false;
 	}
     }
 
 }
