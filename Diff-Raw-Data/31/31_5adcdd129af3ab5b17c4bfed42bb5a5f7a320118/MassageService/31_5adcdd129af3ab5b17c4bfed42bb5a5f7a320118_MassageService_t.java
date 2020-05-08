 package org.orzlabs.android.massage;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.util.Log;
 
 public class MassageService extends Service {
 	public static int vibMode;
 	private Vibrator vib;
 	private boolean isAlive;
 	// TODO to Enum
 	public static final int RANDOM = 0;
 	public static final int CONTINUOUS = 1;
 
 	public static long vibratingTime = 1000L;
 
 	private static final String TAG = "MassageService";
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Context context = getBaseContext();
 		if (vib == null) {
 			vib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
 		}
 		isAlive = true;
 		Log.v(TAG,"onCretate");
 	}
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				while (isAlive) {
 					long vibratingTime = getVibratingTime();
					Log.v(TAG, "vibrate start " + vibratingTime + "msec.");
 					vib.vibrate(vibratingTime);
					Log.v(TAG, "vibrate end " + vibratingTime + "msec.");
 					customSleep(vibratingTime);
					Log.v(TAG, "vibrating " + vibratingTime + "msec.");
 					vib.cancel();
 				}
 			}
 			private void customSleep(long mVibratingTime) {
 				try {
 					long sleepBonus = 0;
 
 					if (vibMode == RANDOM) {
 						sleepBonus = (long) (Math.random() * 200L);
 					} else {
 						sleepBonus = 1000L - vibratingTime;
 					}
 					long sleepTime = mVibratingTime + sleepBonus;
					Log.v(TAG, "sleep start " + sleepTime + "msec.");
 					Thread.sleep(sleepTime);
					Log.v(TAG, "sleep end " + sleepTime + "msec.");
 				} catch (InterruptedException e) {
 					Log.e(TAG, e.getMessage(),e);
 				}
 			}
 			private long getVibratingTime() {
 				if (vibMode == RANDOM) {
 					double rand = Math.random();
 					return (long) (rand * 700);
 				}
 				return 400L;
 			}
 		};
 		t.start();
 	}
 	@Override
 	public void onDestroy() {
 		isAlive = false;
 		vib.cancel();
 	}
 	@Override
 	public IBinder onBind(Intent pIntent) {
 
 		return null;
 	}
 
 }
