 package cmu.waci.logger;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import android.annotation.TargetApi;
 import android.app.Notification;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Binder;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.text.format.Time;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class InteractivityService extends Service {
 	InteractivityView mView;
 	static LinkedList<Time> mActs;
 	private DVFSControl dvfs;
 	static boolean savePowerOn = false;
 	private boolean mRunning;
 	
 	public void onCreate() {
 		super.onCreate();
 		System.out.println("h");
 		mActs = new LinkedList<Time>();
 		mView = new InteractivityView(this);
 		dvfs = new DVFSControl();
 
 		mRunning = true;
 		
 		Notification n = new Notification();
 		startForeground(1111, n);
 
 		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
 				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
 				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
 						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
 						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
 						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
 				PixelFormat.TRANSLUCENT);
 
 		final GestureDetector gestureDetector = new GestureDetector(this,
 				new InteractivityListener());
 		View.OnTouchListener gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				Time t = new Time();
 				t.setToNow();
 				mActs.add(t);
				// System.out.println("good1");
 				// System.out.println(mActs.size());
 
 				long m = mActs.getFirst().toMillis(false);
 				long cur = t.toMillis(true);
 				// System.out.println("true: " + t.toMillis(true) + " false: " +
 				// t.toMillis(false));
 				// System.out.println(cur-m);
 
 				return false;// gestureDetector.onTouchEvent(event);
 			}
 		};
 
 		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
 
 		mView.setOnTouchListener(gestureListener);
 		wm.addView(mView, params);
 
 		Thread thr = new Thread(null, doWork, "Interactivity");
 		thr.start();
 
 	}
 	
 	/*
     public void onToggleClicked(View view) {
         // Is the toggle on?
         boolean on = ((ToggleButton) view).isChecked();
         
         if (on) {
             // Enable CPU scaling
         	savePowerOn = true;
         } else {
             // Disable CPU scaling
         	savePowerOn = false;
         }
     }
 */
 	Runnable doWork = new Runnable() {
 		@SuppressWarnings("deprecation")
 		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 		public void run() {
             Notification note=new Notification.Builder(getApplicationContext())
     		.setContentTitle("Optimizing")
     		.setSmallIcon(R.drawable.ic_launcher)
             .getNotification();
     
             startForeground(1338, note);
 			
 			
 			System.out.println("wat");
 			ArrayList<Integer> freqModes = dvfs.getFrequencyScaleModes();
 
 		/*	
 			while (mRunning) {
 				removeOld(30000);
 				if (savePowerOn) {
 					if (mActs.size() < 2)
 						dvfs.setCPUFrequency(freqModes.get(0));
 					else if (mActs.size() < 10)
 						dvfs.setCPUFrequency(freqModes.get(1));
 					else if (mActs.size() < 15)
 						dvfs.setCPUFrequency(freqModes.get(2));
 					else if (mActs.size() < 20)
 						dvfs.setCPUFrequency(freqModes.get(4));
 					else if (mActs.size() < 30)
 						dvfs.setCPUFrequency(freqModes.get(6));
 					else
 						dvfs.setCPUFrequency(freqModes.get(10));
 				}
 				System.out.println("freq: " + dvfs.getCPUFrequency() +
 						 " presses: " + mActs.size());
 				SystemClock.sleep(5000);
 			}
 */
 			//util
 			while(mRunning) {
 				
 				if(savePowerOn){
 					float pctAcc=0;
 					for(int i=0;i<10;i++) {
 						pctAcc+=CPUInfo.getCPUUtilizationPct();
 						SystemClock.sleep(500);
 					}
 					pctAcc /= 10;
 					
 					if(pctAcc<33)	
 						dvfs.setCPUFrequency(freqModes.get(1));
 					else if(pctAcc<66)
 						dvfs.setCPUFrequency(freqModes.get(4));
 					else
						dvfs.setCPUFrequency(freqModes.get(8));
 				}
 				
 			}
 			stopForeground(true);
 			mActs = null;
 			InteractivityService.this.stopSelf();
 		}
 	};
 	
 
 	private void removeOld(long age) {
 		Time t = new Time();
 		t.setToNow();
 		long cur = t.toMillis(false);
 		while (mActs.peek() != null) {
 			// System.out.println("cur: " + cur + " old: " +
 			// mActs.peek().toMillis(false));
 			if (mActs.peek().toMillis(false) + age < cur) {
 				mActs.remove();
 			} else {
 				break;
 			}
 		}
 
 	}
 
 	public void onDestroy() {
 		mRunning = false;
 	}
 	
 	class InteractivityView extends View {
 
 		public InteractivityView(Context context) {
 			super(context);
 		}
 
		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			// return super.onTouchEvent(event);
			// System.out.println("good2");
			// Toast.makeText(getContext(),"onTouchEvent",
			// Toast.LENGTH_LONG).show();
 			return false;
		}
 
 	}
 
 	class InteractivityListener extends SimpleOnGestureListener {
 
 		public boolean onTouchEvent(MotionEvent e) {
 			System.out.println("good");
 			Toast.makeText(getBaseContext(), "HELLLLLLLOOOOOOO WORLD! ", 0)
 					.show();
 			return false;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return mBinder;
 	}
 
 	private final IBinder mBinder = new InteractBinder();
 
 	public class InteractBinder extends Binder {
 		InteractivityService getService() {
 			return InteractivityService.this;
 		}
 	}
 }
