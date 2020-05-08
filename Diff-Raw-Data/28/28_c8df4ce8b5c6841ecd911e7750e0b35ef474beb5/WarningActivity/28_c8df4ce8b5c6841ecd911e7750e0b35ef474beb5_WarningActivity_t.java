 package tw.h4.game.escape.pages;
 
 import java.lang.ref.WeakReference;
 import java.util.Locale;
 
 import tw.h4.game.escape.Debugger;
 import tw.h4.game.escape.GamePreference;
 import tw.h4.game.escape.R;
 import tw.h4.game.escape.disaster.Disaster;
 import tw.h4.game.escape.disaster.DisasterEngine;
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.TextView;
 
 public class WarningActivity extends Activity {
 	private static final String TAG = "WarningActivity";
 
 	private static CounterHandler mHandler = null;
 	private static final int UPDATE_TIME = 0x100;
 	private int reminderTime = 0;
 	private TextView txtTimer;
 
 	private static class CounterHandler extends Handler {
 		private WeakReference<WarningActivity> mTarget = null;
 
 		public CounterHandler(WarningActivity target) {
 			mTarget = new WeakReference<WarningActivity>(target);
 		}
 
 		public void handleMessage(Message msg) {
 			WarningActivity targetObj = mTarget.get();
 			if (null == targetObj)
 				return;
 			switch (msg.what) {
 			case UPDATE_TIME:
 				// TODO: update time counter
 				targetObj.showTimer();
 				targetObj.reminderTime--;
 				if (0 == targetObj.reminderTime) {
 					removeCallbacks(targetObj.action);
 				} else {
 					targetObj.startTimer(1000);
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_warning);
 		mHandler = new CounterHandler(this);
 		txtTimer = (TextView) findViewById(R.id.warn_time);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		long currTime = System.currentTimeMillis();
 		GamePreference pref = GamePreference.getInstance(WarningActivity.this);
 		int timeSpeed = pref.getTimeSpeed();
 		long prevElapsedTime = pref.getPrevElapsedTime();
 		long prevRecodeTime = pref.getPrevRecordTime();
 		int type = pref.getEventType();
 		Disaster dstr = (new DisasterEngine(this)).getDisaster(type);
 
 		long escapeTime = dstr.getEscapeTime();
 		Debugger.d(TAG, "onResume");
 		if (-1 == prevRecodeTime) {
			prevRecodeTime = currTime;
 			prevElapsedTime = 0;
	        pref.setPrevRecordTime(currTime);
	        pref.setPrevElapsedTime(0);
 		} else if (currTime > prevRecodeTime + (escapeTime - prevElapsedTime)
 		        / timeSpeed) {
 			// TODO: game over
 			Debugger.d(TAG, "Game over");
 			return;
 		}
 		reminderTime = (int) ((escapeTime - prevElapsedTime) / 1000);
 		showTimer();
 		reminderTime--;
 		startTimer(1000 - (prevElapsedTime % 1000));
 	}
 
 	protected void onPause() {
 		mHandler.removeCallbacks(action);
		// TODO: save elapsed time
		long currTime = System.currentTimeMillis();
        GamePreference pref = GamePreference.getInstance(WarningActivity.this);
        int timeSpeed = pref.getTimeSpeed();
        long prevElapsedTime = pref.getPrevElapsedTime();
        long prevRecodeTime = pref.getPrevRecordTime();
        pref.setPrevRecordTime(currTime);
        pref.setPrevElapsedTime(prevElapsedTime + (currTime - prevRecodeTime)
                / timeSpeed);
        
 		super.onPause();
 	}
 
 	private Runnable action = new Runnable() {
 		@Override
 		public void run() {
 			Message msg = new Message();
 			msg.what = UPDATE_TIME;
 			mHandler.sendMessage(msg);
 		}
 	};
 
 	private void startTimer(long interval) {
 		mHandler.postDelayed(action, interval);
 	}
 
 	public void showTimer() {
 		int hr = reminderTime / 3600;
 		int min = (reminderTime % 3600) / 60;
 		int sec = reminderTime % 60;
 		String timeString = String.format(Locale.US, "%02d:%02d:%02d", hr, min,
 		        sec);
 		txtTimer.setText(timeString);
 	}
 }
