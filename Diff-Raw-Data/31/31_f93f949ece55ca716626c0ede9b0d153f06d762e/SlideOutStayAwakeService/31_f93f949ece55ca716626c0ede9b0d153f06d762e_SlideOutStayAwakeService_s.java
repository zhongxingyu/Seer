 /**
  * SlideOutStayAwakeService - Keep device from sleeping while hardware keyboard
  * is out.
  *
  * Written by Kristian Berge Nessa <kristian at cavatina dot no>
  * for Cavatina Software AS, placed in the public domain.
  */
 package no.cavatina.slideoutstayawake;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 
 public class SlideOutStayAwakeService extends Service {
 
 	private static final String TAG = SlideOutStayAwakeService.class.getSimpleName();
 	private static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";
 
	private PowerManager.WakeLock wl;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(TAG, "onCreate()");
 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);

 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BCAST_CONFIGCHANGED);
 		this.registerReceiver(mBroadcastReceiver, filter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy()");
 		if (wl != null && wl.isHeld())
 			wl.release();
 	}
 
	public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent myIntent) {
 
 			if (myIntent.getAction().equals(BCAST_CONFIGCHANGED)) {
 
 				Log.d(TAG, "received->" + BCAST_CONFIGCHANGED);
 				switch (getResources().getConfiguration().hardKeyboardHidden) {
 				case Configuration.HARDKEYBOARDHIDDEN_NO:
 					Log.d(TAG, "Keyboard out!");
					if (!wl.isHeld())
						wl.acquire();
 					break;
 				case Configuration.HARDKEYBOARDHIDDEN_YES:
 					Log.d(TAG, "Keyboard in!");
					if (wl.isHeld())
						wl.release();
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 }
