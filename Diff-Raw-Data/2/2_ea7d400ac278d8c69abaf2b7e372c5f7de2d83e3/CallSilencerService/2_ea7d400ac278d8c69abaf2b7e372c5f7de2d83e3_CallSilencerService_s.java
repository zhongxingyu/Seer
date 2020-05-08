 package edu.ucsb.ece251.charlesmunger.roomdetector;
 
 import com.google.inject.Inject;
 
 import roboguice.service.RoboIntentService;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class CallSilencerService extends RoboIntentService {
 
 	private static final String TAG = "CallSilencerService";
 	public static final String EXTRA_STATE = "EXTRA_STATE";
 	@Inject AudioManager am;
 	
 	public CallSilencerService() {
 		super(TAG);
 	}
 
 	@Override
 	public void onHandleIntent(Intent intent) {
 		switch(intent.getIntExtra(EXTRA_STATE, -1)) {
 		case TelephonyManager.CALL_STATE_IDLE: idle(); break;
 		case TelephonyManager.CALL_STATE_RINGING: ringing(); break;
 		case TelephonyManager.CALL_STATE_OFFHOOK: offHook(); break;
 		default: Log.w(TAG, "Unexpected state");
 		}
 	}
 
 	private void offHook() {
 		Log.d(TAG, "Off hook");
		am.setStreamMute(AudioManager.STREAM_RING, true);
 	}
 
 	private void idle() {
 		Log.d(TAG, "Restoring ringer state");
 		am.setStreamMute(AudioManager.STREAM_RING,false);
 	}
 
 	private void ringing() {
 		Log.d(TAG, "muting ringer stream");
 		am.setStreamMute(AudioManager.STREAM_RING, true);
 	}
 }
