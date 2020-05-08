 package edu.ucla.cens.acousticapp;
 
 //import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 //import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.app.PendingIntent;
 import android.app.AlarmManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.media.AudioFormat;
 import android.net.Uri;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.widget.Toast;
 
 import edu.ucla.cens.systemsens.IPowerMonitor;
 import edu.ucla.cens.systemsens.IAdaptiveApplication;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 //import android.util.Log;
 
 import edu.ucla.cens.systemlog.ISystemLog;
 import edu.ucla.cens.systemlog.Log;
 
 public class AcousticAppService extends Service 
 {
 	public static final int INT_MIN=CONSTS.INT_MIN;
 	public static final int INT_MAX=CONSTS.INT_MAX;
 	public static final int INTERVAL = CONSTS.INTERVAL;
 
 	/** Time unit constants */
 	public static final int ONE_SECOND = 1000;
 	public static final int ONE_MINUTE = 60 * ONE_SECOND;
 	public static final int ONE_HOUR = 60 * ONE_MINUTE;
 	public static final int ONE_DAY = 24 * ONE_HOUR;
 
 	public static final int DAY_NO = 24;
 	public static final int WEEK_NO = 7;
 
 	public static final int SAMPLERATE =8000;
 
 	private static final int DEFAULT_ALARM_INTERVAL = INTERVAL * ONE_SECOND;
 	private static final int DEFAULT_RECORDER_INTERVAL = INT_MAX * ONE_SECOND;
 	private static final int DEFAULT_RECORDER_MAX = INT_MIN * ONE_SECOND;
 	private static final int DEFAULT_RECORDER_MIN = INT_MAX * ONE_SECOND;
 	private static final int DEFAULT_FRAME_COUNT = 60;
 	//private static final Boolean MEDIACHECK_B = true;
 
 	int count=0;
 
 	CircularQueue hour_q;
 	CircularQueue day_q;
 	CircularQueue week_q;
 	int hour_count=0;
 	int day_count=0;
 	int week_count=0;
 
 	/** Name of the service used logging tag */
 	private static final String TAG = "AcousticAppService";
 	private static final String DATA_TAG = "AcousticAppData";
 
 	/** Version of this service */
 	public static final String VER = "1.0";
 
 	/** Name of this application */
 	public static final String APP_NAME = "AcousticApp";
 
 	/** Work unit names */
 	public static final String TIME_UNIT_NAME = "acoustictime";
 	public String versionNo = "1.0";
 
 	/** Types of messages used by this service */
 	private static final int LOC_UPDATE_MSG = 1;///////////////////
 
 	/** Action strings for alarm events */
 	private static final String RECORD_ALARM_ACTION = "record_alarm";
 
 	/** Default timers in milliseconds */
 	//private static final int DEFAULT_ALARM_SCANNING_INTERVAL = 10 * ONE_SECOND;
 	private static final int DEFAULT_POWERCYCLE_HORIZON = 10 * ONE_MINUTE;
 
 	ArrayList<Double> totalWork;
 	ArrayList<String> unitNames;
 
 	/** State variable indicating if the services is running or not */
 	private boolean mRun;
 
 	/** State variable indicating if the GPS location is being used */
 	private boolean acousticRunning;////////////////////
 
 	/** PowerMonitor object */
 	private IPowerMonitor mPowerMonitor;
 	private boolean mPowerMonitorConnected = false;
 
 	/** CPU wake lock */
 	private PowerManager.WakeLock mCpuLock;
 	/** Alarm Manager object */
 	private AlarmManager mAlarmManager;
 	/** Pending Intent objects */
 	private PendingIntent mScanSender;
 	private long alarmID;
 
 	/** Acoustic manager object */
 	private AcousticManager acousticManager;
 
 	/** Scanning interval variable */
 	private int mAlarmScanInterval;
 	private SharedPreferences mSettings;
 	private SharedPreferences.Editor mEditor;
 
 	Recorder recorderInstance;
 	Thread record_thread; 
 	Thread acousticManager_Thread;
 
 	int frameSize_int = DEFAULT_RECORDER_INTERVAL;
 	int frameSizeMax_int = DEFAULT_RECORDER_MAX;
 	int frameSizeMin_int = DEFAULT_RECORDER_MIN;
 	int frameInterval_int = DEFAULT_ALARM_INTERVAL;
 	int frameLength_int = -1;
 	int frameSize = frameSize_int;
 	//int frameInterval = frameInterval_int;
 
 	Boolean feature_energy=CONSTS.ENERGY_B;
 	Boolean feature_zcr=CONSTS.ZCR_B;
 	Boolean feature_raw=CONSTS.RAW_B;
 	Boolean feature_spl=CONSTS.SPL_B;
 	Boolean feature_adv=CONSTS.ADV_B;
 
 	Feature feature = new Feature();
 
 	int ttime_int;
 	String deviceName="not set";
 
 	private NotificationManager notificationManager;
 	private Notification notification;
 	PendingIntent contentIntent;
 	Context context_notif;
 	BroadcastReceiver power_receiver;
 	IntentFilter power_filter;
 
 	TelephonyManager telephonyManager;
 	PhoneStateListener phoneStateListener;
 
 	String fDirPath=Environment.getExternalStorageDirectory().getAbsolutePath() +"/AcousticAppData";
 	Boolean uploadThread_b = false;
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() 
 	{
 		super.onCreate();
 		Log.setAppName(APP_NAME);
 
 		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
 		mEditor = mSettings.edit();
 
 		mEditor.putBoolean("status", true);
 		mEditor.putBoolean("record_status", false);
 		mEditor.putInt("spl_value", -1);
 		mEditor.commit();
 
 		if(!CONSTS.BASIC)
 		{
 			day_q= new CircularQueue(DAY_NO);
 			week_q= new CircularQueue(WEEK_NO);
 			loadday();
 			loadweek();
 		}
 
 		try
 		{
 			versionNo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
 		}
 		catch (NameNotFoundException e)
 		{
 			Log.e(TAG, e.getMessage());
 		}
 
 		bindService(new Intent(ISystemLog.class.getName()),	Log.SystemLogConnection, Context.BIND_AUTO_CREATE);
 		bindService(new Intent(IPowerMonitor.class.getName()), mPowerMonitorConnection, Context.BIND_AUTO_CREATE);
 
 		// Hacking the interval of SystemSens. This needs to be fixed
 		//mHistory = new CircularQueue(DEFAULT_POWERCYCLE_HORIZON/(2*ONE_MINUTE));
 
 		// mLocManager = (LocationManager)
 		// getSystemService(Context.LOCATION_SERVICE);
 		acousticManager = new AcousticManager();
 
 
 		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
 		deviceName= telephonyManager.getDeviceId();
 
 		// mSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
 
 		feature.setEnergy(feature_energy);
 		feature.setZcr(feature_zcr);
 		feature.setRaw(feature_raw);
 		feature.setSpl(feature_spl);
 		feature.setAdv(feature_adv);
 
 		if(CONSTS.SETTINGFILE)
 		{
 			try
 			{
 				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/AcousticApp/settings.txt");
 				//log("Path"+ file.getAbsolutePath());
 				if(file.exists())
 				{
 					FileInputStream fis = new FileInputStream(file); 
 					InputStreamReader isr = new InputStreamReader(fis);
 					char[] inputBuffer = new char[fis.available()];
 					isr.read(inputBuffer);
 					String json_string =  new String(inputBuffer);
 					isr.close();
 					fis.close();
 					//log("JSON: "+json_string);
 					JSONObject jObject=new JSONObject(json_string);
 					if(jObject.getString("appname").equals("AcousticApp"))
 					{
 						frameSizeMax_int = jObject.getInt("recordmax") * ONE_SECOND;
 						frameSizeMin_int = jObject.getInt("recordmin") * ONE_SECOND;
 						frameInterval_int = jObject.getInt("alarminterval") * ONE_SECOND;
 
 						mEditor.putInt("alarm_interval", frameInterval_int);
 						mEditor.commit();
 
 						try
 						{
 							feature.setEnergy(jObject.getBoolean("feature_energy"));
 						}
 						catch(JSONException je)
 						{
 							feature.setEnergy(false);
 							Log.e(TAG, "Settings: Energy Not set");
 						}
 
 						try
 						{
 							feature.setZcr(jObject.getBoolean("feature_zcr"));
 						}
 						catch(JSONException je)
 						{
 							feature.setZcr(false);
 							Log.e(TAG, "Settings: ZCR Not set");
 						}
 
 						try
 						{
 							feature.setRaw(jObject.getBoolean("feature_raw"));
 						}
 						catch(JSONException je)
 						{
 							feature.setRaw(feature_raw);
 							Log.e(TAG, "Settings: Raw Not set");
 						}
 
 						try
 						{
 							feature.setSpl(jObject.getBoolean("feature_spl"));
 						}
 						catch(JSONException je)
 						{
 							feature.setSpl(false);
 							Log.e(TAG, "Settings: SPL Not set");
 						}
 
 						try
 						{
 							feature.setAdv(jObject.getBoolean("feature_adv"));
 						}
 						catch(JSONException je)
 						{
 							feature.setAdv(false);
 							Log.e(TAG, "Settings: Advanced Features Not set");
 						}
 
 						Log.i(TAG, "Settings File Successfully loaded");
 
 					}
 					else
 					{
 						throw new Exception();
 					}
 				}
 				else
 				{
 					Log.i(TAG,"Settings File not found, using defaults");
 					resetValues();
 				}
 			}
 			catch(Exception e)
 			{
 				Log.e(TAG,"Settings File error, using defaults");
 				resetValues();
 			}
 
 		}
 
 		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
 		mCpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, APP_NAME);
 		mCpuLock.setReferenceCounted(false);
 
 		// Set up alarms for repeating events.
 		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 		// Alarm scan Intent objects
 		Intent scanAlarmIntent = new Intent(AcousticAppService.this,AcousticAppService.class);
 		scanAlarmIntent.setAction(RECORD_ALARM_ACTION);
 		alarmID = System.currentTimeMillis();
 		scanAlarmIntent.putExtra("alarmID", alarmID);
 		mScanSender = PendingIntent.getService(AcousticAppService.this,0, scanAlarmIntent, 0);
 
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		notification = new Notification(R.drawable.notif_icon_green ,"Acoustic Service On",System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_NO_CLEAR; 
 		contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, AcousticAppActivity.class), 0);
 		context_notif=this;
 		//resetToDefault();
 
 		//		mEditor.putInt("alarm_interval", frameInterval_int);
 		//		mEditor.commit();
 
 		long now = SystemClock.elapsedRealtime();
 		//mAlarmManager.setInexactRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, frameInterval_int, mScanSender);
 		Log.i(TAG, "Starting AcousticApp Service");
 		//mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now, mScanSender);
 
 		//Handle Phone calls, switches off recorder when on call
 		phoneStateListener = new PhoneStateListener() 
 		{
 			@Override
 			public void onCallStateChanged(int state, String number) {
 				String currentPhoneState = null;
 				switch (state) {
 				case TelephonyManager.CALL_STATE_RINGING:
 					Log.i(TAG, "Device is ringing");// Call from " + number);
 					if(recorderInstance!=null && recorderInstance.isRecording())
 					{
 						Log.i("AcousticAppControl", "Device is ringing, stopping recorder");// Call from " + number);					
 						//recorderInstance.setRecording(false);
 					}
 					break;
 				case TelephonyManager.CALL_STATE_OFFHOOK:
 					Log.i(TAG,"Device call state is currently Off Hook.");
 					if(recorderInstance!=null && recorderInstance.isRecording())
 					{
 						Log.i("AcousticAppControl", "Device is off Hook, stopping recorder");// Call from " + number);					
 						recorderInstance.setRecording(false);         
 					}
 					break;
 				case TelephonyManager.CALL_STATE_IDLE:
 					Log.i(TAG,"Device call state is currently Idle");
 					Log.i("AcousticAppControl", "Device is currently idle");// Call from " + number);					
 					break;
 				}
 			}
 		};
 		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
 
 		if(CONSTS.BATTERY)
 		{
 			BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
 				int scale = -1;
 				int level = -1;
 				int voltage = -1;
 				int temp = -1;
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 					scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 					temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
 					voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
 					Log.i("AcousticAppBattery", "Level:"+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
 				}
 			};
 			IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 			registerReceiver(batteryReceiver, filter);
 		}
 	}
 
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		if (!mPowerMonitorConnected) 
 		{ 
 			Log.i(TAG,"Rebinding to PowerMonitor"); 
 			bindService(new Intent(IPowerMonitor.class.getName()), mPowerMonitorConnection, Context.BIND_AUTO_CREATE); 
 		}
 
 		if (!Log.isConnected()) 
 		{
 			Log.i(TAG, "Connecting to Log");
 			bindService(new Intent(ISystemLog.class.getName()),Log.SystemLogConnection, Context.BIND_AUTO_CREATE);
 		}
 
 		if(CONSTS.UPLOAD && feature_raw)
 		{
 			power_receiver = new BroadcastReceiver()
 			{
 				public void onReceive(Context context, Intent intent) 
 				{
 					int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
 
 					if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) 
 					{
 						Log.i(TAG,"Device plugged in");
 						uploadRawData();
 					} 
 					else if (plugged == 0) 
 					{
 						Log.i(TAG,"Device on Battery power");
 						// on battery power
 					} 
 					else {
 						// intent didnt include extra info
 					}
 				}
 			}; 
 
 			power_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);	
 			registerReceiver(power_receiver, power_filter);
 
 
 		}
 
 
 		Toast.makeText(getApplicationContext(), "AcousticService Started!", Toast.LENGTH_SHORT).show();
 
 		if (intent != null) 
 		{ 
 			String action = intent.getAction();
 
 			notification.setLatestEventInfo(this, "AcousticApp", "Service On",contentIntent );
 			notificationManager.notify(1, notification);
 
 			if (action != null) 
 			{ 
 				Log.i(TAG,"Received ALARM: " + action); 
 				if(action.equals(RECORD_ALARM_ACTION)) 
 				{
 
 					frameInterval_int =  mSettings.getInt("alarm_interval",INTERVAL) * ONE_SECOND;
 					frameSizeMin_int =  mSettings.getInt("min",INT_MIN) * ONE_SECOND;
 					frameSizeMax_int =  mSettings.getInt("max",INT_MAX) * ONE_SECOND;
 					frameLength_int =  mSettings.getInt("frameLength",32);
 
 					feature_energy =  mSettings.getBoolean("energy", feature_energy);
 					feature_zcr =  mSettings.getBoolean("zcr", feature_zcr);
 					feature_raw =  mSettings.getBoolean("raw", feature_raw);
 					feature_spl =  mSettings.getBoolean("spl", feature_spl);
 					feature_adv =  mSettings.getBoolean("adv", feature_adv);
 					count=0;
 
 					Thread t = new Thread()
 					{
 						public void run()
 						{
 							acousticManager.start();
 						}
 					};
 					t.start();
 					if(CONSTS.EXTENDED)
 					{
 						int alarmcount = mSettings.getInt("alarmcount", 0);
 						alarmcount++;
 						Log.i("AcousticAppAlarm", "Alaram Count:"+alarmcount);
 						if(alarmcount%CONSTS.EXTENDED_AWAKE == 0)
 						{
 							long now = SystemClock.elapsedRealtime();
 							mAlarmManager.cancel(mScanSender);
 							mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now+frameInterval_int * (CONSTS.EXTENDED_SLEEP+1), frameInterval_int * CONSTS.EXTENDED_SLEEP, mScanSender);
 							Log.i("AcousticAppAlarm", "Setting to 30");
 						}
 						else if(alarmcount%CONSTS.EXTENDED_AWAKE == 1)
 						{
 							long now = SystemClock.elapsedRealtime();
 							mAlarmManager.cancel(mScanSender);
 							mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now+frameInterval_int, frameInterval_int, mScanSender);
 							Log.i("AcousticAppAlarm", "Resetting back");
 						}
 						mEditor.putInt("alarmcount", alarmcount);
 						mEditor.commit();
 					}
 
 					//					if (!mCpuLock.isHeld())
 					//						mCpuLock.acquire(); // Released by WiFi receiver					
 					//acousticManager.start();
 					//long now = SystemClock.elapsedRealtime(); 
 					//mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + frameInterval,mScanSender); 
 					//Log.i(TAG,"ALarm Set for interval : "+frameInterval);					
 				}
 				else if(action.equals("boot_alarm")) 
 				{ 
 					if(mSettings.getBoolean("status", false))
 					{
 						Log.i(TAG,"Received BOOT ALARM: "); 
 						frameInterval_int =  mSettings.getInt("alarm_interval",INTERVAL) * ONE_SECOND;
 						frameSizeMin_int =  mSettings.getInt("min",INT_MIN) * ONE_SECOND;
 						frameSizeMax_int =  mSettings.getInt("max",INT_MAX) * ONE_SECOND;
 						frameLength_int =  mSettings.getInt("frameLength",32);
 
 						feature_energy =  mSettings.getBoolean("energy", feature_energy);
 						feature_zcr =  mSettings.getBoolean("zcr", feature_zcr);
 						feature_raw =  mSettings.getBoolean("raw", feature_raw);
 						feature_spl =  mSettings.getBoolean("spl", feature_spl);
 						feature_adv =  mSettings.getBoolean("adv", feature_adv);
 
 						count=0;
 						
 						mEditor.putInt("alarmcount", 0);
 						mEditor.commit();
 
 						//hour_q= new CircularQueue((3600 / (frameInterval_int/ONE_SECOND)));
 						//hour_count=0;
 
 						long now = SystemClock.elapsedRealtime();
 						mAlarmManager.cancel(mScanSender);
 						mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, frameInterval_int, mScanSender);
 						Log.i("AcousticAppControl", "Setting Alarm for "+frameInterval_int);
 
 						Log.i(TAG, "Loading bundle, Boot alarm");
 						log("Boot : frame Interval: "+frameInterval_int + ", FrameMin: "+frameSizeMin_int+ ", FrameMax: "+frameSizeMax_int+ ", Frame: "+frameSize);
 					}
 				}
 				//Log.i(TAG, "-----------------------------------b4 start : "+acousticManager_Thread.isAlive());
 				/*if(acousticManager_Thread==null)// | !acousticManager_Thread.isAlive())
 				{
 					acousticManager_Thread = new Thread(acousticManager);
 					acousticManager_Thread.start();
 					Log.i(TAG, "-----------------------------------is alive : ");
 				}
 				else
 				{
 
 					Log.i(TAG, "-----------------------------------is not alive : ");
 					acousticManager.start();
 				}*/
 
 			}
 			else
 			{
 				Log.i(TAG,"Received NULL ALARM: "); 
 				Bundle b = intent.getExtras();
 				if(b!=null)
 				{
 					frameInterval_int = b.getInt("frameInterval") * ONE_SECOND;
 					frameSizeMin_int = b.getInt("recordmin") * ONE_SECOND;
 					frameSizeMax_int = b.getInt("recordmax") * ONE_SECOND;
 					frameLength_int = b.getInt("frameLength");
 
 					feature_energy = b.getBoolean("energy");
 					feature_zcr = b.getBoolean("zcr");
 					feature_spl = b.getBoolean("spl");
 					feature_raw = b.getBoolean("raw");
 					feature_adv = b.getBoolean("adv");
 
 					count=0;
 
 					//For keep track of count of alarms
 					mEditor.putInt("alarmcount", 0);
 					mEditor.commit();
 
 
 					//Health
 					mEditor.putInt("rTotal", 0);
 					mEditor.putInt("qTotal", 0);
 					mEditor.putFloat("lastProc", -1);
 					mEditor.commit();
 
 					long now = SystemClock.elapsedRealtime();
 					mAlarmManager.cancel(mScanSender);
 					Log.i("AcousticAppControl", "Setting Alarm for "+frameInterval_int);
 
 					mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, frameInterval_int, mScanSender);
 					//Log.i(TAG, "Loading bundle, resetting alarm as per GUI");
 					log("GUI : frame Interval: "+frameInterval_int + ", FrameMin: "+frameSizeMin_int+ ", FrameMax: "+frameSizeMax_int+ ", Frame: "+frameSize);
 
 					//hour_q= new CircularQueue((int)(3600 / (frameInterval_int/ONE_SECOND)));
 					//hour_count=0;
 
 					//					mEditor.putInt("alarm_interval", frameInterval_int);
 					//					mEditor.commit();				
 				}
 				//acousticManager.start();
 			}
 		}
 
 		// super.onStart(intent, startId);
 
 	}
 
 	@Override
 	public void onDestroy() 
 	{
 		Log.i(TAG,"onDestroy Service");
 		mEditor.putBoolean("status", false);
 		mEditor.putString("workdone", acousticManager.getSeconds()+"");
 		mEditor.commit();
		
		acousticRunning = false;
		acousticManager.stop();
		
 		notificationManager.cancel(1);
 		//		mEditor.putInt("alarm_interval", frameInterval_int);
 		//		mEditor.commit();
 
 		// Cancel the pending alarms
 		mAlarmManager.cancel(mScanSender);
 
 		if(power_receiver!=null)
 		{
 			unregisterReceiver(power_receiver);
 		}
 
 
 		//cancel phone state Listener
 		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
 
 
 		unbindService(mPowerMonitorConnection);
 		unbindService(Log.SystemLogConnection);
 	}
 
 	public void startRecording()
 	{
 
 		Log.i("AcousticAppControl", "Started for "+frameSize+" , version no is"+versionNo);					
 
 		notificationManager.cancel(1);	
 		notification = new Notification(R.drawable.notif_icon_green ,"Service Running",System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_NO_CLEAR; 
 		notification.setLatestEventInfo(context_notif, "AcousticApp", "Acoustic Service Running",contentIntent );
 		notificationManager.notify(1, notification);
 
 		recorderInstance = new Recorder(this, SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, frameLength_int, deviceName, versionNo, feature); 
 		record_thread =new Thread(recorderInstance); 
 		//recorderInstance.setFileName(new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/test.raw"));
 		//log("Final file name : "+recorderInstance.getFileName().getAbsolutePath()); 
 
 		if(frameSizeMax_int == frameSizeMin_int)
 		{
 			frameSize = frameSizeMax_int;
 		}
 		else if(count % DEFAULT_FRAME_COUNT == 0)
 		{
 			frameSize =  frameSizeMin_int +  (int)(Math.random() * (frameSizeMax_int - frameSizeMin_int));
 			log("Calculating new Random Value");
 			//frameInterval = frameInterval_int - frameSize;
 		}
 		count++;
 		count %= DEFAULT_FRAME_COUNT;
 		log("frame Interval: "+frameInterval_int + ", FrameMin: "+frameSizeMin_int+ ", FrameMax: "+frameSizeMax_int+ ", Frame: "+frameSize);
 
 		long tp2;
 		long tp1 = SystemClock.elapsedRealtime();
 
 		record_thread.start();
 		recorderInstance.setRecording(true); 
 
 		synchronized (this) 
 		{ 
 			try 
 			{ 
 				if(acousticManager.isLimited)
 				{
 					Log.i(TAG,"Started Recording (limited) for " + acousticManager.limited_frameSize);
 					//this.wait(acousticManager.limited_frameSize);
 					int i;
 					for(i=0;i<acousticManager.limited_frameSize && recorderInstance!=null && recorderInstance.isRecording();i+=10)
 					{
 						if(!mSettings.getBoolean("status", true))
 							break;
 						this.wait(10);
 					}
 					acousticManager.isLimited = false;
 				}
 				else
 				{	
 					Log.i(TAG,"Started Recording for " + frameSize);
 					//this.wait(frameSize);
 					int i;
 					for(i=0;i<frameSize && recorderInstance!=null && recorderInstance.isRecording();i+=250)
 					{
 						if(!mSettings.getBoolean("status", true))
 							break;
 						this.wait(250);
 					}
 					//					if(mSettings.getBoolean("calib_b", false))
 					//					{
 					//						Log.i(TAG, "CALIB TRUE");				
 					//						for(;(i<frameInterval_int-10000) && mSettings.getBoolean("calib_b", false);i+=1000)
 					//						{
 					//							Log.i(TAG, "i:"+i);
 					//							if(!mSettings.getBoolean("status", true))
 					//								break;
 					//							this.wait(1000);
 					//						}
 					//						
 					//					}
 					//this.wait(frameSize);
 				}
 			} 
 			catch (InterruptedException e) 
 			{ 
 				Log.e(TAG,"Exception in sync");
 				e.printStackTrace(); 
 			}
 		}
 		tp2=SystemClock.elapsedRealtime();
 		Log.i("AcousticAppControl","Time end: "+(tp2-tp1));
 		tp1=tp2;
 
 		recorderInstance.setRecording(false); 
 		mEditor.putBoolean("record_status", false);
 		mEditor.commit();
 		Log.i(TAG,"Stopped Recording");
 		Log.i("AcousticAppControl", "Stopped Recording");
 		notificationManager.cancel(1);
 		notification = new Notification(R.drawable.notif_icon_yellow ,"Service Idle",System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_NO_CLEAR; 
 		notification.setLatestEventInfo(context_notif, "AcousticApp", "Acoustic Service Idle",contentIntent );
 		notificationManager.notify(1, notification);
 		
 		try 
 		{ 
 			if(record_thread!=null)
 				record_thread.join();
 			//recorderInstance=null;
 			//record_thread=null;
 			tp2=SystemClock.elapsedRealtime();
 			log("Time join: "+(tp2-tp1));
 			tp1=tp2;
 		} 
 		catch (InterruptedException e) 
 		{
 
 			Log.e(TAG,"Exception aftr join");
 			e.printStackTrace(); 
 		}
 	}
 
 	public void addhistory(int inp)
 	{
 		int frameTime = (int)(3600 / (frameInterval_int/ONE_SECOND));
 		if(hour_q == null)
 		{
 			hour_q= new CircularQueue(frameTime);
 			hour_count=0;
 		}
 		else if(frameTime != hour_q.getSize())
 		{
 			hour_q= new CircularQueue(frameTime);
 			hour_count=0;
 		}
 		log("hour queue size : "+hour_q.getSize());
 
 
 		if(inp>20)
 			hour_q.add(1);
 		else
 			hour_q.add(0);
 		hour_count++;
 
 		log("-Minutes : hour_Count : "+hour_count);
 
 		//hour_q.display("HOUR");
 
 		int percent = hour_q.getSum()*100/frameTime;
 		mEditor.putInt("hour", percent);
 		mEditor.commit();
 
 		if(hour_count >= frameTime)
 		{
 			hour_count=0;
 			day_q.add(percent);
 			//day_q.display("DAY");
 			day_count = mSettings.getInt("day_count", 0);
 			day_count++;
 
 			int day_percent = (int)day_q.getSum();
 			if(day_percent>0)
 				day_percent/=DAY_NO;
 			log("day % "+day_percent);
 			saveday();
 			mEditor.putInt("day", day_percent);
 			mEditor.commit();
 
 			if(day_count >= DAY_NO)
 			{
 				day_count=0;
 				week_q.add(day_percent);
 				//week_q.display("WEEK");
 				week_count = mSettings.getInt("week_count", 0);
 				week_count++;
 
 				int week_percent = (int)week_q.getSum();
 				if(week_percent>0)
 					week_percent/=WEEK_NO;
 				log("week % "+week_percent);
 				saveday();
 				saveweek();
 				mEditor.putInt("week", week_percent);
 				mEditor.commit();
 			}
 		}
 	}
 
 	private void saveday()
 	{
 		if(day_q == null)
 			return;
 		int[] temp_arr = day_q.getArray();
 		for(int i=0; i<temp_arr.length;i++)
 		{
 			mEditor.putInt("d"+i,temp_arr[0]);
 		}
 		mEditor.putInt("day_count", day_count);
 
 		mEditor.commit();
 	}
 
 	private void saveweek()
 	{
 		if(week_q == null)
 			return;
 		int[] temp_arr = week_q.getArray();
 		for(int i=0; i<temp_arr.length;i++)
 		{
 			mEditor.putInt("w"+i,temp_arr[0]);
 		}
 		mEditor.putInt("week_count", week_count);
 		mEditor.commit();
 	}
 
 	public void loadday()
 	{
 		int[] temp_arr= new int[DAY_NO];
 		for(int i=0;i<temp_arr.length;i++)
 		{
 			temp_arr[i] = mSettings.getInt("d"+i, -1);
 		}
 		if(day_q != null)
 		{
 			day_q.loadValues(temp_arr);
 		}
 	}
 
 	public void loadweek()
 	{
 		int[] temp_arr= new int[WEEK_NO];
 		for(int i=0;i<temp_arr.length;i++)
 		{
 			temp_arr[i] = mSettings.getInt("w"+i, -1);
 		}
 		if(week_q != null)
 		{
 			week_q.loadValues(temp_arr);
 		}
 	}
 
 	private final IAdaptiveApplication mAdaptiveControl= new IAdaptiveApplication.Stub()
 	{
 		public String getName()
 		{
 			return APP_NAME;
 		}
 
 		public List<String> identifyList()
 		{
 			unitNames = new ArrayList<String>(1);
 			unitNames.add(TIME_UNIT_NAME);
 			log("In IDENTIFY LIST---------------------");
 			return unitNames;
 		}
 
 		public List<Double> getWork()
 		{
 			totalWork = new ArrayList<Double>();
 			double workDone = acousticManager.getSeconds();
 
 			log("In GET WORK---------------------");
 			Log.i(TAG,"Work in getwork : "+workDone);
 			mEditor.putString("workdone", workDone+"");
 			mEditor.commit();
 			totalWork.add(workDone);
 			//mHistory.add(workDone);
 			//Log.i(TAG, "Added " + workDone + " to history queue: "+ mHistory.getSum());
 			return totalWork;
 		}
 
 		public void setWorkLimit(List workLimit)
 		{
 			double acoustictimeLimit = (Double) workLimit.get(0);
 
 			Log.i(TAG, "RECEIVED work limit: " + acoustictimeLimit);
 			Log.i(TAG,"RECEIVED work limit: " + acoustictimeLimit);
 			log("In SET WORK LIMIT---------------------");
 			//TODO: put constant
 			//acousticManager.setLimit(3);
 			acousticManager.setLimit(acoustictimeLimit);
 		}
 
 
 	};
 
 
 	//    private final Handler mHandler = new Handler()
 	//    {
 	//        @Override
 	//        public void handleMessage(Message msg)
 	//        {
 	//            if (msg.what == LOC_UPDATE_MSG)
 	//            {
 	//                //mLastKnownLoc = mTempKnownLoc;
 	//
 	//                /*
 	//                Calendar cal = Calendar.getInstance();
 	//                String timeStr = mSDF.format(cal.getTime());
 	//
 	//
 	//                Log.i(DATA_TAG, timeStr 
 	//                        + ", " +  mLastKnownLoc.getLatitude()
 	//                        + ", " +  mLastKnownLoc.getLongitude());
 	//                */
 	//
 	//                acousticManager.stop();
 	//
 	//
 	//                if (mCpuLock.isHeld())
 	//                    mCpuLock.release();
 	//
 	//            }
 	//
 	//        }
 	//    };
 
 	private ServiceConnection mPowerMonitorConnection = new ServiceConnection() 
 	{ 
 		public void onServiceConnected(ComponentName className,IBinder service) 
 		{ 
 			mPowerMonitor =IPowerMonitor.Stub.asInterface(service); 
 			try 
 			{
 				mPowerMonitor.register(mAdaptiveControl, DEFAULT_POWERCYCLE_HORIZON); 
 			}
 			catch (RemoteException re) 
 			{ 
 				Log.e(TAG,"Could not register AdaptivePower object.", re); 
 			}
 			mPowerMonitorConnected= true; 
 		}
 
 		public void onServiceDisconnected(ComponentName className) 
 		{ 
 			try
 			{
 				mPowerMonitor.unregister(mAdaptiveControl); 
 			} 
 			catch (RemoteException re)
 			{ 
 				Log.e(TAG, "Could not unregister AdaptivePower object.",re); 
 			}
 			mPowerMonitor = null; 
 			mPowerMonitorConnected = false; 
 		} 
 	};
 
 	void resetValues()
 	{
 		frameSize_int = DEFAULT_RECORDER_INTERVAL;
 		frameSizeMax_int = DEFAULT_RECORDER_MAX;
 		frameSizeMin_int = DEFAULT_RECORDER_MIN;
 		frameInterval_int = DEFAULT_ALARM_INTERVAL;
 	}
 
 	public void uploadRawData()
 	{
 		if(!uploadThread_b)
 		{
 			new Thread()
 			{
 				public void run()
 				{
 					if(isMounted())
 					{
 						Log.i(TAG, "Uploader: Upload Thread called: ");
 						uploadThread_b = true;
 						uploadRawData_worker();
 						uploadThread_b=false;
 						Log.i(TAG, "Uploader: Upload Thread exited");
 					}
 					else
 					{
 						Log.i(TAG, "Uploader: Skipped since menory card open");
 					}
 				}
 			}.start();
 		}
 	}
 
 	public void uploadRawData_worker()
 	{
 		//Log.i(TAG,"raw1:"+feature_raw);
 		//Log.i(TAG,"raw2:"+UPLOAD_B);
 		if(feature_raw && CONSTS.UPLOAD)
 		{
 			try
 			{
 				//z("Connected:"+isConnected(this));
 				if(isConnected(this) && isMounted())
 				{
 					File dir = new File(fDirPath);
 					File[] filelist = dir.listFiles();
 					int count_prev=0;
 					//Log.i(TAG,"List Length:"+filelist.length+"  element1:"+filelist[0].getName());
 					//Log.i(TAG,"Connected:"+isConnected(this));
 					while(filelist != null && isConnected(this) && isMounted())
 					{
 						if(filelist.length>=1)
 						{
 							Arrays.sort(filelist);
 							if(filelist[0].getName().startsWith("p"))
 							{	
 								Log.i(TAG, "Uploader: File Still being Processed: "+filelist[0].getName());
 								return;
 							}
 							Log.i(TAG, "Uploading File : "+filelist[0].getName());
 							if(upload(filelist[0]))
 							{
 								Log.i(TAG,"Upload Successful, bytes uploaded :"+ filelist[0].length());
 								filelist[0].delete();
 								count_prev=0;
 							}
 							else
 							{
 								if((count_prev++) >= 5)
 								{
 									Log.i(TAG,"File upload failed more than 5 times, Aborting...");
 									return;
 								}
 								Log.i(TAG,"File upload failed, retrying... Attempt :"+count_prev);
 							}
 						}
 						else
 						{
 							Log.i(TAG, "Uploader: All Files uploaded");
 							return;
 						}
 						filelist = dir.listFiles();
 					}
 				}
 				Log.i(TAG, "Uploader: All Files uploaded");		
 			}
 			catch(Exception e)
 			{
 				Log.i(TAG, "Exception in upload worker");
 			}
 		}
 
 	}
 
 	public Boolean upload(File file_inp)
 	{
 		HttpClient httpClient;
 		HttpPost postRequest;
 		MultipartEntity reqEntity;
 		ResponseHandler<String> responseHandler;
 		FileBody fileBody;
 
 		httpClient = new DefaultHttpClient();
 		postRequest = new HttpPost("http://systemlog2.cens.ucla.edu/systemlog/logs/putblob/");
 		responseHandler = new BasicResponseHandler();
 
 		// Indicate that this information comes in parts (text and file)
 		reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 		fileBody = new FileBody(file_inp, "audio/wav");
 		reqEntity.addPart("file", fileBody);
 
 		try 
 		{
 			// The rest of the mundane data
 			reqEntity.addPart("imei", new StringBody(deviceName));
 			reqEntity.addPart("logger", new StringBody("acousticapp"));
 			reqEntity.addPart("timestamp", new StringBody(file_inp.getName()));
 
 			// Prepare to ship it!
 			postRequest.setEntity(reqEntity);
 			String response = httpClient.execute(postRequest, responseHandler);
 			if(response.contains("saved file"))
 			{
 				Log.i(TAG, "File Uploaded : "+file_inp.getName());
 				return true;
 			}
 		}
 		catch (UnsupportedEncodingException e) {
 			Log.e(TAG,"Rawupload ex1 : "+e);
 		}
 		catch (ClientProtocolException e) {
 			Log.e(TAG,"Rawupload ex2 : "+e);
 		}
 		catch (IOException e) {
 			Log.e(TAG,"Rawupload ex3 : "+e);
 		}
 		Log.e(TAG, "File Upload Failed : "+file_inp.getName());
 
 		return false;
 	}
 
 	public boolean isConnected(Context context) 
 	{
 		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
 		return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
 	}
 
 	public boolean isMounted()
 	{
 		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 	}
 
 	class AcousticManager// implements Runnable
 	{
 		private double mLimit = Double.NaN;
 
 		private double mTotal;
 		private double mCurTotal;
 		private double mStart;
 		private double mCount;
 
 		private boolean isLimited = false;
 		int limited_frameSize=0;
 
 		public AcousticManager()
 		{
 			String mTotal_str = mSettings.getString("workdone","0.0");
 			mTotal =  Double.parseDouble(mTotal_str) * ONE_SECOND;
 			log("Loaded mtotal :  "+mTotal);
 			mCurTotal = 0.0;
 			mCount = 0.0;
 		}
 
 		public void setLimit(double workLimit)
 		{
 			mLimit = workLimit * ONE_SECOND;
 			mCurTotal = 0.0;
 
 			/*
             if (Double.isNaN(workLimit))
             {
                 AcousticAppService.this.resetToDefault();
                 return;
             }
 
 
             //double used = mCurTotal * DEFAULT_POWERCYCLE_HORIZON/ONE_MINUTE;
             double used = mHistory.getSum();
             log("Estimated usage per horizon: " + used);
             log("Current limit per horizon: " + workLimit);
             Log.i(TAG, "Current scanning interval: " +(frameSize/ONE_SECOND) + " seconds");
 
             if ((workLimit != used) && (workLimit != 0))
             {
                 double ratio = used/workLimit;
                 log(TAG, "Multiplying interval by " + ratio);
 
                 Double newFrameSize = frameSize * 
                 ////Double newInterval = mGpsScanInterval * ratio;
                 LocationTrackerService.this.setGPSInterval(
                         newInterval.intValue());
 
             }
             else
             {
                 Log.i(TAG, "No need to change scanning interval.");
             }
 			 */
 
 		}
 
 		public void run() {
 			start();
 		}
 
 		public void start()
 		{
 			if ( (!Double.isNaN(mLimit) && (mCurTotal > mLimit)) || mLimit ==0 )
 			{
 				Log.i("AcousticAppControl", "Ran out of AcousticApp budget.");
 
 				notification = new Notification(R.drawable.notif_icon ,"Ran out of Budget",System.currentTimeMillis());
 				notification.flags |= Notification.FLAG_NO_CLEAR; 
 				notification.setLatestEventInfo(context_notif, "AcousticApp", "Service Disable: Out of Budget",contentIntent );
 				notificationManager.notify(1, notification);
 				acousticRunning = false;
 				return;
 			}
 			else if( !Double.isNaN(mLimit) && (mCurTotal + frameSize> mLimit))
 			{
 				limited_frameSize = (int)(mLimit - mCurTotal);
 				Log.i("AcousticAppControl", "Limiting Running time to AcousticApp: "+ limited_frameSize);
 				isLimited = true;
 			}
 
 			notification = new Notification(R.drawable.notif_icon_green ,"Service Running",System.currentTimeMillis());
 			notification.flags |= Notification.FLAG_NO_CLEAR; 
 			notification.setLatestEventInfo(context_notif, "AcousticApp", "Acoustic Service Running",contentIntent );
 			notificationManager.notify(1, notification);
 
 			log("START curTotal: " + mCurTotal + ", mTotal: " + mTotal+ ", mLimit: " + mLimit);
 			if (!mCpuLock.isHeld())
 				mCpuLock.acquire(); // Released by WiFi receiver
 
 			//Log.i(TAG, "------------------------in start calling stop");
 			acousticRunning = true;
 			mStart= SystemClock.elapsedRealtime(); 
 			mEditor.putBoolean("record_status", true);
 			mEditor.commit();
 
 
 			startRecording();
 			stop();
 		}
 
 		public void stop()
 		{
 			//Log.i(TAG, "------------------------in stop");
 			if (mCpuLock.isHeld())
 				mCpuLock.release(); // Released by WiFi receiver
 
 			mEditor.putBoolean("record_status", false);
 			mEditor.commit();
 
 			if (acousticRunning)
 			{
 				//mLocManager.removeUpdates(LocationTrackerService.this);
 				log("Stopping AManager.");
 
 				double current =  SystemClock.elapsedRealtime();
 				mTotal += (current - mStart);
 				mCurTotal += (current - mStart);
 				mStart = current;
 				acousticRunning = false;
 
 				if(!CONSTS.BASIC)
 				{
 					int percent = mSettings.getInt("minute", -1);
 					addhistory(percent);
 				}
 
 				log("STOP curTotal: " + mCurTotal + ", mTotal: " + mTotal+ ", mLimit: " + mLimit);
 				log("Updated mCurTotal to " + mCurTotal);
 			}
 		}
 
 		public double getSeconds()
 		{
 			if (acousticRunning)
 			{
 				double current =  SystemClock.elapsedRealtime();
 				mTotal += (current - mStart);
 				mCurTotal += (current - mStart);
 				mStart = current;
 			}
 
 			return  mTotal/ONE_SECOND;
 		}
 
 		public double getCount()
 		{
 			return mCount;
 
 		}
 	}
 
 	public void z(String inp)
 	{
 		Log.i(TAG,inp+"");
 	}
 
 	public void log(String inp)
 	{
 		if(CONSTS.DEBUG)
 			android.util.Log.i(TAG,inp+"");
 	}
 }
