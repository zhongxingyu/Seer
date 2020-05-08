 package vub.lhoste.ttsnotifier;
 
 import java.io.IOException;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Process;
 import android.preference.PreferenceManager;
 import android.provider.Contacts;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.telephony.gsm.SmsMessage;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.tts.TTS;
 
 public class TTSNotifierService extends Service {
 
 	private static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
 	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
 	private static final String ACTION_BATTERY_LOW = "android.intent.action.ACTION_BATTERY_LOW";
 	private static final String ACTION_MEDIA_BAD_REMOVAL = "android.intent.action.ACTION_MEDIA_BAD_REMOVAL";
 	private static final String ACTION_BOOT_COMPLETED = "android.intent.action.ACTION_BOOT_COMPLETED";
 	private static final String ACTION_PROVIDER_CHANGED = "android.intent.action.ACTION_PROVIDER_CHANGED";
 	private static final String ACTION_MEDIA_MOUNTED = "android.intent.action.ACTION_MEDIA_MOUNTED";
 	private static final String ACTION_MEDIA_UNMOUNTED = "android.intent.action.ACTION_MEDIA_UNMOUNTED";
 	private static final String ACTION_PICK_WIFI_NETWORK = "android.net.wifi.PICK_WIFI_NETWORK";
 	private static final String ACTION_WIFI_STATE_CHANGE = "android.net.wifi.STATE_CHANGE";
 	private static final String ACTION_SUPPLICANT_CONNECTION_CHANGE_ACTION  = "android.net.wifi.supplicant.CONNECTION_CHANGE";
 
 	private static final int LONG_THREADWAIT = 1000;
 	private static final int MEDIUM_THREADWAIT = 300;
 	private static final int SHORT_THREADWAIT = 50;
 
 	private volatile static TTS myTts = null;
 	private volatile MediaPlayer myRingTonePlayer = null;
 	private volatile boolean stopRingtone = false;
 	private volatile boolean ttsReady = false;
 
 	private Context context;
 	private ServiceHandler mServiceHandler;
 	private Looper mServiceLooper;
 	private SharedPreferences mPrefs;
 	private AudioManager mAudioManager;
 	private TelephonyManager mTelephonyManager;
 	private Thread mRingtoneThread;
 
 	// State
 	private boolean silentMode = false;
 	private volatile int oldStreamRingtoneVolume = 0;
 	private volatile int oldStreamMusicVolume = 0;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Log.v("TTSNotifierService", "onCreate()");
 		context = getApplicationContext();
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 		mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
 		HandlerThread thread;
 		if (mPrefs.getBoolean("cbxRunWithHighPriority", false))
 			thread = new HandlerThread("HandleIntentTTSNotifier", Process.THREAD_PRIORITY_URGENT_AUDIO);
 		else
 			thread = new HandlerThread("HandleIntentTTSNotifier", Process.THREAD_PRIORITY_BACKGROUND);
 		thread.start();
 		mServiceLooper = thread.getLooper();
 		mServiceHandler = new ServiceHandler(mServiceLooper);
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		Log.v("TTSNotifierService", "onStart()");
 		if (myTts == null)
 			myTts = new TTS(context, ttsInitListener, true);
 		Message msg = mServiceHandler.obtainMessage();
 		msg.arg1 = startId;
 		msg.obj = intent;
 		mServiceHandler.sendMessage(msg);
 	} 
 
 	@Override
 	public void onDestroy() {
 		stopRingtone = true;
 		ttsReady = false;
 		if (myTts != null) 
 			myTts.shutdown();
 		myTts = null;
 		if (myRingTonePlayer != null)
 			myRingTonePlayer.release();
 		myRingTonePlayer = null;
 		mServiceLooper.quit();
 	} 
 
 	private final class ServiceHandler extends Handler {
 
 		public ServiceHandler(Looper serviceLooper) {
 			super(serviceLooper);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			Log.v("TTSNotifierService", "handleMessage()");
 			
 			Intent intent = (Intent) msg.obj;
 			String action = intent.getAction();
 			
 			readState();
 			storeAndUpdateVolume();
 
 			boolean cbxEnable = mPrefs.getBoolean("cbxEnable", false);
 			boolean cbxObeySilentMode = mPrefs.getBoolean("cbxObeySilentMode", true);
 
 			if (action == null) return;
 			if (!cbxEnable) return;
 			if (cbxObeySilentMode && silentMode) return;
 
 			// When calling ignore other notifications
 			if (!ACTION_PHONE_STATE.equals(action) && (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE))
 				return;
 			
 			Log.v("TTSNotifierService", "Action: " + action);
 
 			if (ACTION_PHONE_STATE.equals(action)) {
 				handleACTION_PHONE_STATE(intent);
 			} else if (ACTION_SMS_RECEIVED.equals(action)) {
 				handleACTION_SMS_RECEIVED(intent);
 			} else if (ACTION_BATTERY_LOW.equals(action)) {
 				handleACTION_BATTERY_LOW(intent);
 			} else if (ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
 				handleACTION_MEDIA_BAD_REMOVAL(intent);
 			} else if (ACTION_BOOT_COMPLETED.equals(action)) {
 				handleACTION_BOOT_COMPLETED(intent);
 			} else if (ACTION_PROVIDER_CHANGED.equals(action)) {
 				handleACTION_PROVIDER_CHANGED(intent);
 			} else if (ACTION_MEDIA_MOUNTED.equals(action)) {
 				handleACTION_MEDIA_MOUNTED(intent);
 			} else if (ACTION_MEDIA_UNMOUNTED.equals(action)) {
 				handleACTION_MEDIA_UNMOUNTED(intent);
 			} else if (ACTION_PICK_WIFI_NETWORK.equals(action)) {
 				handleACTION_PICK_WIFI_NETWORK(intent);
 			} else if (ACTION_WIFI_STATE_CHANGE.equals(action)) {
 				handleACTION_WIFI_STATE_CHANGE(intent);
 			} else if (ACTION_SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
 				handleSUPPLICANT_CONNECTION_CHANGE_ACTION(intent);
 			}
 			// ACTION_PHONE_STATE is different because we are using a thread there
 			if (!ACTION_PHONE_STATE.equals(action))
 				restoreVolume();
 		}
 	}
 
 	private void readState() {
 		if (mAudioManager != null)
 			silentMode = mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
 	}
 	
 	private void storeAndUpdateVolume() {
		if (mPrefs.getBoolean("cbxChangeVolume", false) && mAudioManager != null) {
 			int intOptionsTTSVolume = Math.min(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), Math.max(0, Integer.parseInt(mPrefs.getString("intOptionsTTSVolume", "14"))));
 			oldStreamMusicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 			oldStreamRingtoneVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
			while (mAudioManager.getStreamVolume(AudioManager.STREAM_RING) > 1)
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_LOWER, 0);
 			while (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < intOptionsTTSVolume)
 				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
 			while (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > intOptionsTTSVolume)
 				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
 		}
 	}
 	
 	private void restoreVolume() {
 		if (mPrefs.getBoolean("cbxChangeVolume", false) && mAudioManager != null) {
 			while (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > oldStreamMusicVolume)
 				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
 			while (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < oldStreamMusicVolume)
 				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
 			while (mAudioManager.getStreamVolume(AudioManager.STREAM_RING) < oldStreamRingtoneVolume)
 				mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);			
 		}
 	}
 
 	private void waitForSpeechInitialised() {
 		while (!ttsReady) {
 			try {
 				Thread.sleep(MEDIUM_THREADWAIT);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void waitForSpeechFinished() {
 		try {
 			Thread.sleep(MEDIUM_THREADWAIT);
 		} catch (InterruptedException e) { }
 		while (myTts.isSpeaking()) {
 			try {
 				Thread.sleep(MEDIUM_THREADWAIT);
 			} catch (InterruptedException e) { }
 		}
 		try {
 			Thread.sleep(MEDIUM_THREADWAIT);
 		} catch (InterruptedException e) { }
 	}
 
 	private void waitForRingTonePlayed() {
 		do {
 			try {
 				Thread.sleep(MEDIUM_THREADWAIT);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		} while (myRingTonePlayer.isPlaying());
 	}
 
 	private void waitForRingToneThreadStopped() {
 		while (stopRingtone) {
 			try {
 				Thread.sleep(SHORT_THREADWAIT);
 			} catch (InterruptedException e) { 
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void playRingtone(boolean waitForFinish) throws IllegalStateException, IOException {
 		Log.v("TTSNotifierService", "playRingtone()" + myRingTonePlayer);
 		if (myRingTonePlayer == null) return;
 		myRingTonePlayer.start();
 		if (waitForFinish)
 			waitForRingTonePlayed();
 	}
 	
 	private void speak(String str, boolean waitForFinish) {
 		Log.v("TTSNotifierService", "speak():" + myTts);
 		if (myTts == null) return;
 		waitForSpeechInitialised();
 		myTts.speak(str, 0, null);
 		if (waitForFinish)
 			waitForSpeechFinished();
 	}
 
 	private void handleACTION_PHONE_STATE(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableIncomingCall", true)) return;
 		final String txtOptionsIncomingCall;
 		// Preferences
 		if (mPrefs.getBoolean("cbxOptionsIncomingCallUserDefinedText", false))
 			txtOptionsIncomingCall = mPrefs.getString("txtOptionsIncomingCall", "Phone call from %s");
 		else
 			txtOptionsIncomingCall = "Phone call from %s";
 		final boolean cbxOptionsIncomingCallUseTTSRingtone = mPrefs.getBoolean("cbxOptionsIncomingCallUseTTSRingtone", false);
 		final String txtOptionsIncomingCallRingtone = mPrefs.getString("txtOptionsIncomingCallRingtone", Settings.System.DEFAULT_RINGTONE_URI.toString());
 		final int intOptionsIncomingCallMinimalRingCountBeforeTTS = Integer.parseInt(mPrefs.getString("intOptionsIncomingCallMinimalRingCountBeforeTTS", "2"));
 		final int intOptionsIncomingCallRingCountAfterTTS = Integer.parseInt(mPrefs.getString("intOptionsIncomingCallRingCountAfterTTS", "2"));
 		final int intOptionsIncomingCallTTSRepeats = Integer.parseInt(mPrefs.getString("intOptionsIncomingCallTTSRepeats", "2"));
 		final int intOptionsIncomingCallDelayBetweenRingtones = Integer.parseInt(mPrefs.getString("intOptionsIncomingCallDelayBetweenRingtones", "200"));
 		final String phoneNr = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
 		// Logic
 		if (mRingtoneThread != null)
 			stopRingtone = true;
 		if (myTts != null)
 			myTts.stop();
 		if (myRingTonePlayer != null) {
 			myRingTonePlayer.stop();
 			try {
 				myRingTonePlayer.prepare();
 			} catch (IllegalStateException e) {
 				myRingTonePlayer = null;
 			} catch (IOException e) {
 				myRingTonePlayer = null;
 			}
 		}
 		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
 			waitForRingToneThreadStopped();
 			mRingtoneThread = new Thread() {
 				public void run() {
 					try {
 						if (cbxOptionsIncomingCallUseTTSRingtone) {
 							if (myRingTonePlayer == null)
 								myRingTonePlayer = MediaPlayer.create(context, Uri.parse(txtOptionsIncomingCallRingtone));
 							int ringtoneState = 0;
 							int nrTTS = 0;
 							int ringCounter = 1;
 							while (!stopRingtone) {
 								Log.v("TTSNotifierService", "CALL STATE :" + ringtoneState);
 								switch (ringtoneState) {
 								case 0:
 									playRingtone(true);
 									break;
 								case 1:
 									if (nrTTS >= intOptionsIncomingCallTTSRepeats) {
 										ringCounter += 1;
 										ringtoneState = -1;
 										try {
 											Thread.sleep(intOptionsIncomingCallDelayBetweenRingtones);
 										} catch (InterruptedException e) { }
 									} else if (nrTTS == 0) {
 										if (!ttsReady || ringCounter < intOptionsIncomingCallMinimalRingCountBeforeTTS) {
 											ringCounter += 1;
 											ringtoneState = -1;
 											try {
 												Thread.sleep(intOptionsIncomingCallDelayBetweenRingtones);
 											} catch (InterruptedException e) { }
 										}
 									} else {
 										if (!ttsReady || ringCounter < intOptionsIncomingCallRingCountAfterTTS) {
 											ringCounter += 1;
 											ringtoneState = -1;
 											try {
 												Thread.sleep(intOptionsIncomingCallDelayBetweenRingtones);
 											} catch (InterruptedException e) { }
 										}
 									}
 									break;
 								case 2:
 									speak(String.format(txtOptionsIncomingCall, getContactNameFromNumber(phoneNr)), true);
 									nrTTS += 1;
 									break;
 								case 3:
 									ringtoneState = -1;
 									ringCounter = 1;
 									try {
 										Thread.sleep(intOptionsIncomingCallDelayBetweenRingtones);
 									} catch (InterruptedException e) { }
 									break;
 								}
 								ringtoneState += 1;
 							}
 						} else {
 							speak(String.format(txtOptionsIncomingCall, getContactNameFromNumber(phoneNr)), true);
 						}
 					} catch (IllegalStateException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					mRingtoneThread = null;
 					stopRingtone = false;
 				}
 			};
 			mRingtoneThread.start();
 		} else {
 			restoreVolume();
 		}
 	}
 
 	private void handleACTION_SMS_RECEIVED(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableIncomingSMS", true)) return;
 		String txtOptionsIncomingSMS;
 		// Preferences
 		if (mPrefs.getBoolean("cbxOptionsIncomingSMSOnlyAnnouncePerson", true))
 			txtOptionsIncomingSMS = "New text message from %s";
 		else if (mPrefs.getBoolean("cbxOptionsIncomingSMSUserDefinedText", true))
 			txtOptionsIncomingSMS = mPrefs.getString("txtOptionsIncomingSMS", "New text message from %s about %s");
 		else
 			txtOptionsIncomingSMS = "New text message from %s about %s";
 		// Logic
 		SmsMessage[] messages = getMessagesFromIntent(intent);
 		if (messages == null) return;
 		SmsMessage sms = messages[0];
 		if (sms.getMessageClass() != SmsMessage.MessageClass.CLASS_0 && !sms.isReplace()) {
 			String body;
 			if (messages.length == 1) {
 				body = messages[0].getDisplayMessageBody();
 			} else {
 				StringBuilder bodyText = new StringBuilder();
 				for (int i = 0; i < messages.length; i++) {
 					bodyText.append(messages[i].getMessageBody());
 				}   
 				body = bodyText.toString();
 			}
 			String address = messages[0].getDisplayOriginatingAddress();
 			speak(String.format(txtOptionsIncomingSMS, getContactNameFromNumber(address), body), true);
 		}
 	}
 
 	public void handleACTION_BATTERY_LOW(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableBatteryLow", true)) return;
 		String txtOptionsBatteryLowWarningText;
 		// Preferences
 		if (mPrefs.getBoolean("cbxOptionsBatteryLowWarningUserDefinedText", true))
 			txtOptionsBatteryLowWarningText = mPrefs.getString("txtOptionsBatteryLowWarningText", "Your battery is low");
 		else
 			txtOptionsBatteryLowWarningText = "Your battery is low";
 		speak(txtOptionsBatteryLowWarningText, true);
 	}
 
 	public void handleACTION_MEDIA_BAD_REMOVAL(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableBadMediaRemoval", true)) return;
 		String txtOptionsMediaBadRemovalText;
 		if (mPrefs.getBoolean("cbxOptionsMediaBadRemovalUserDefinedText", true))
 			txtOptionsMediaBadRemovalText = mPrefs.getString("txtOptionsMediaBadRemovalText", "Media removed before it was unmounted");
 		else
 			txtOptionsMediaBadRemovalText = "Media removed before it was unmounted";
 		speak(txtOptionsMediaBadRemovalText, true);
 	}
 
 	public void handleACTION_BOOT_COMPLETED(Intent intent) { }
 
 	public void handleACTION_PROVIDER_CHANGED(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableProviderChanged", true)) return;
 		String txtOptionsProviderChangedText;
 		if (mPrefs.getBoolean("cbxOptionsProviderChangedUserDefinedText", true))
 			txtOptionsProviderChangedText = mPrefs.getString("txtOptionsProviderChangedText", "Phone provider changed");
 		else
 			txtOptionsProviderChangedText = "Phone provider changed";
 		speak(txtOptionsProviderChangedText, true);
 	}
 
 	public void handleACTION_MEDIA_MOUNTED(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableMediaMounted", true)) return;
 		if (true) return; // Disabled because TTS needs the SD card
 		String txtOptionsMediaMountedText;
 		if (mPrefs.getBoolean("cbxOptionsMediaMountedUserDefinedText", true))
 			txtOptionsMediaMountedText = mPrefs.getString("txtOptionsMediaMountedText", "Media mounted");
 		else
 			txtOptionsMediaMountedText = "Media mounted";
 		speak(txtOptionsMediaMountedText, true);
 	}
 
 	public void handleACTION_MEDIA_UNMOUNTED(Intent intent) {
 		if (!mPrefs.getBoolean("cbxEnableMediaUnMounted", true)) return;
 		String txtOptionsMediaUnMountedText;
 		if (mPrefs.getBoolean("cbxOptionsMediaUnMountedUserDefinedText", true))
 			txtOptionsMediaUnMountedText = mPrefs.getString("txtOptionsMediaUnMountedText", "Media unmounted");
 		else
 			txtOptionsMediaUnMountedText = "Media unmounted";
 		speak(txtOptionsMediaUnMountedText, true);
 	}
 
 	public void handleACTION_PICK_WIFI_NETWORK(Intent intent) {
 		// Preferences
 		boolean cbxEnableWifiDiscovered = mPrefs.getBoolean("cbxEnableWifiDiscovered", true);
 		String txtOptionsWifiDiscovered;
 		if (mPrefs.getBoolean("cbxOptionsWifiDiscoveredUserDefinedText", false))
 			txtOptionsWifiDiscovered = mPrefs.getString("txtOptionsWifiDiscovered", "Wyfy Signal in Range");
 		else
 			txtOptionsWifiDiscovered = "Wyfy Signal in Range";
 		// Logic
 		if (!cbxEnableWifiDiscovered) return;
 		speak(txtOptionsWifiDiscovered, true);	
 	}
 
 	private void handleACTION_WIFI_STATE_CHANGE(Intent intent) {
 		// Preferences
 		boolean cbxEnableWifiConnect = mPrefs.getBoolean("cbxEnableWifiConnect", true);
 		String txtOptionsWifiConnected;
 		if (mPrefs.getBoolean("cbxOptionsWifiConnectedUserDefinedText", false))
 			txtOptionsWifiConnected = mPrefs.getString("txtOptionsWifiConnected", "Wyfy connected");
 		else
 			txtOptionsWifiConnected = "Wyfy connected";
 		NetworkInfo ni = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 		if (ni == null) return;
 		if (cbxEnableWifiConnect && ni.getState() == NetworkInfo.State.CONNECTED)
 			speak(txtOptionsWifiConnected, true);
 	}
 
 	private void handleSUPPLICANT_CONNECTION_CHANGE_ACTION(Intent intent) {
 		// Preferences
 		boolean cbxEnableWifiDisconnect = mPrefs.getBoolean("cbxEnableWifiDisconnect", true);
 		String txtOptionsWifiDisconnected;
 		if (mPrefs.getBoolean("cbxOptionsWifiDisconnectedUserDefinedText", true))
 			txtOptionsWifiDisconnected = mPrefs.getString("txtOptionsWifiDisconnected", "Wyfy disconnected");
 		else
 			txtOptionsWifiDisconnected = "Wyfy disconnected";
 		// Logic
 		if (cbxEnableWifiDisconnect && !intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true))
 			speak(txtOptionsWifiDisconnected, true);
 	}
 
 	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
 		@Override
 		public void onInit(int version) {
 			Log.v("TTSNotifierService", "TTS INIT DONE");
 			ttsReady = true;
 		}
 	};
 
 
 	public static boolean isTtsInstalled(Context ctx) {
 		PackageManager pm = ctx.getPackageManager();
 		Intent intent = new Intent("android.intent.action.USE_TTS");
 		intent.addCategory("android.intent.category.TTS");
 		ResolveInfo info = pm.resolveService(intent, 0); 
 		if (info == null) {
 			return false;
 		}   
 		return true;
 	}
 
 	private String getContactNameFromNumber(String number) {
 		String[] projection = new String[] {
 				Contacts.Phones.DISPLAY_NAME,
 				Contacts.Phones.NUMBER };
 		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
 		Cursor c = getContentResolver().query(contactUri, projection, null,
 				null, null);
 		if (c.moveToFirst()) {
 			String name = c.getString(c
 					.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
 			return name;
 		}
 		return "Unknown";
 	}
 
 	private SmsMessage[] getMessagesFromIntent(Intent intent)
 	{
 		SmsMessage retMsgs[] = null;
 		Bundle bdl = intent.getExtras();
 		try{
 			Object pdus[] = (Object [])bdl.get("pdus");
 			retMsgs = new SmsMessage[pdus.length];
 			for(int n=0; n < pdus.length; n++)
 			{
 				byte[] byteData = (byte[])pdus[n];
 				retMsgs[n] =
 					SmsMessage.createFromPdu(byteData);
 			}        
 		}
 		catch(Exception e)
 		{
 			Log.e("GetMessages", "fail", e);
 		}
 		return retMsgs;
 	}
 
 	public static void beginStartingService(Context context, Intent intent) {
 		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 		if (isTtsInstalled(context))
 			context.startService(intent);
 		else
 			Toast.makeText(context, "TTSNotifier: TTS not installed! Install it from the market!", Toast.LENGTH_LONG).show();
 	}
 }
