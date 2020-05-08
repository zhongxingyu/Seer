 package com.android.tecla.addon;
 
 
 import ca.idrc.tecla.framework.Persistence;
 import ca.idrc.tecla.framework.TeclaStatic;
 
 import android.app.ActivityManager;
 import android.app.Application;
 import android.app.KeyguardManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.KeyguardManager.KeyguardLock;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.inputmethod.InputMethodManager;
 
 public class TeclaApp extends Application {
 
 	public static final String CLASS_TAG = "TeclaApp";
 	
 	public static final int WAKE_LOCK_TIMEOUT = 5000;
 
 	private static TeclaApp sInstance;
 	public static Persistence persistence;
 	public static TeclaIME ime;
 	public static TeclaAccessibilityService a11yservice;
 
 	private PowerManager power_manager;
 	private KeyguardManager keyguard_manager;
 	private WakeLock wake_lock;
 	private KeyguardLock keyguard_lock;
 	private AudioManager audio_manager;
 	private ActivityManager activity_manager;
 	private InputMethodManager ime_manager;
 
 	private Boolean screen_on;
 
 	public static TeclaApp getInstance() {
 		return sInstance;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Application#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		
 		init(getApplicationContext());
 	}
 	
 	private void init(Context context) {
 		TeclaStatic.logD(CLASS_TAG, "Application context created!");
 
 		sInstance = this;
 		persistence = new Persistence(this);
 
 		power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wake_lock = power_manager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK |
 				PowerManager.ON_AFTER_RELEASE, TeclaStatic.TAG);
 		keyguard_manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
 		keyguard_lock = keyguard_manager.newKeyguardLock(CLASS_TAG);
 		audio_manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		activity_manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
 		ime_manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
 		screen_on = isScreenOn();
 		
 		if (screen_on) {
 			TeclaStatic.logD(CLASS_TAG, "Screen on");
 		} else {
 			TeclaStatic.logD(CLASS_TAG, "Screen off");
 		}
 
 		//Intents & Intent Filters
 		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
 		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
 		
 	}
 	
 //	public void startOnboarding() {
 //		Intent intent = new Intent(this, OnboardingDialog.class);
 //		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //		startActivity(intent);
 //	}
 //	
 	
 	public void pickIme() {
 		ime_manager.showInputMethodPicker();
 	}
 	
 	public Boolean isTeclaA11yServiceRunning() {
 	    for (RunningServiceInfo service : activity_manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (TeclaAccessibilityService.class.getName().equals(service.service.getClassName())) {
 	            return true;
 	        }
 	    }
 	    return false;
 	}
 
 	public Boolean isSupportedIMERunning() {
 	    for (RunningServiceInfo service : activity_manager.getRunningServices(Integer.MAX_VALUE)) {
 	        if (TeclaIME.class.getName().equals(service.service.getClassName())) {
 	            return true;
 	        }
 	    }
 	    return false;
 	}
 
 	public static void setIMEInstance (TeclaIME ime_instance) {
 		ime = ime_instance;
 	}
 	
 	public void answerCall() {
 		// Simulate a press of the headset button to pick up the call
 		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);             
 		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
 		sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
 
 		// froyo and beyond trigger on buttonUp instead of buttonDown
 		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
 		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
 		sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
 
 	}
 
 	public boolean isScreenOn() {
 		return power_manager.isScreenOn();
 	}
 
 	// All intents will be processed here
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
 				screen_on = false;
 				TeclaStatic.logD(CLASS_TAG, "Screen off");
 			}
 			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
 				screen_on = true;
 				//TODO: If event from Tecla, unlock screen!
 				TeclaStatic.logD(CLASS_TAG, "Screen on");
 			}
 		}
 
 	};
 	
 	/**
 	 * Hold wake lock until releaseWakeLock() is called.
 	 */
 	public void holdWakeLock() {
 		holdWakeLock(0);
 	}
 
 	/**
 	 * Hold wake lock for the number of seconds specified by length
 	 * @param length the number of seconds to hold the wake lock for
 	 */
 	public void holdWakeLock(long length) {
 		if (length > 0) {
 			TeclaStatic.logD(CLASS_TAG, "Aquiring temporal wake lock...");
 			wake_lock.acquire(length);
 		} else {
 			TeclaStatic.logD(CLASS_TAG, "Aquiring wake lock...");
 			wake_lock.acquire();
 		}
 		pokeUserActivityTimer();
 	}
 
 	public void releaseWakeLock () {
 		TeclaStatic.logD(CLASS_TAG, "Releasing wake lock...");
 		wake_lock.release();
 	}
 
 	public void releaseKeyguardLock() {
 		TeclaStatic.logD(CLASS_TAG, "Releasing keyguard lock...");
 		keyguard_lock.reenableKeyguard();
 	}
 
 	public void holdKeyguardLock() {
 		TeclaStatic.logD(CLASS_TAG, "Acquiring keyguard lock...");
 		keyguard_lock.disableKeyguard();
 	}
 
 	/**
 	 * Wakes and unlocks the screen for a minimum of {@link WAKE_LOCK_TIMEOUT} miliseconds
 	 */
 	public void wakeUnlockScreen() {
 		holdKeyguardLock();
 		holdWakeLock(WAKE_LOCK_TIMEOUT);
 	}
 
 	public void pokeUserActivityTimer () {
 		power_manager.userActivity(SystemClock.uptimeMillis(), true);
 	}
 
 	public void useSpeakerphone() {
 		audio_manager.setMode(AudioManager.MODE_IN_CALL);
 		audio_manager.setSpeakerphoneOn(true);
 	}
 
 	public void stopUsingSpeakerPhone() {
 		audio_manager.setMode(AudioManager.MODE_NORMAL);
 		audio_manager.setSpeakerphoneOn(false);
 	}
 	
 	public String byte2Hex(int bite) {
 		return String.format("0x%02x", bite);
 	}
 
 //	private void logRunningServices() {
 //		for (RunningServiceInfo service_info : activity_manager.getRunningServices(Integer.MAX_VALUE)) {
 //			TeclaStatic.logD(CLASS_TAG, service_info.service.getClassName());
 //		}
 //	}
 //
 }
