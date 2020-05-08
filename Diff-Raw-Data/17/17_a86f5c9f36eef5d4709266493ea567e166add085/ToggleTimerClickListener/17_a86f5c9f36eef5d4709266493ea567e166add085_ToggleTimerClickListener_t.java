 package com.futilities.mindtimer;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ToggleTimerClickListener implements OnClickListener {
     private static final int TIMER_NOT_STARTED = 0;
     private static final int TIMER_ACTIVE = 1;
     private static final int TIMER_PAUSED = 2;
     Toast mToast;
     private long mId;
     private long mStartTimeMillis;
     private Timer mTimer;
     private int mSecDuration;
     private long mAlarmDingAt;
     private Context mCtx;
     private UpdateTimeTask mTimerTask;
     private int mTimerState;
     private float mTimerSecondsElapsed; // seconds timer has been running for
 
     public ToggleTimerClickListener(Context context, long id, int secDuration) {
         mCtx = context;
         mId = id;
         mSecDuration = secDuration;
         mTimerState = TIMER_NOT_STARTED;
     }
 
     /**
      * @return the mTimerTask
      */
     public UpdateTimeTask getTimerTask() {
         return mTimerTask;
     }
 
     class UpdateTimeTask extends TimerTask {
         public void run() {
             if (mCtx == null) {
                 Log.v("MindTimer", "canceling because mctx is null");
                 this.cancel();
                 return;
             }
 
             mTimerSecondsElapsed += .5;
 
             long millis = SystemClock.elapsedRealtime() - mStartTimeMillis;
             int seconds = (int) (millis / 1000);
 
             Log.i("MindTimer", String.format(
                     "Seconds elapsed in TimerTask (updates ui) %d", seconds));
 
             if (SystemClock.elapsedRealtime() > mAlarmDingAt) {
                 this.cancel();
             }
 
             float percentComplete = (float) mTimerSecondsElapsed / mSecDuration;
 
             float degrees = percentComplete * 360;
 
             mHandler.sendMessage(mHandler.obtainMessage(0, degrees));
 
         }
     }
 
     @Override
     public void onClick(View v) {
         Log.i("MindTimer", "clicked id " + mId);
 
         // TODO toggle running state alarm manager; save progress made and
         // cancel alarm
         // Upon restarting the alarm get the saved progress
 
         if (mTimerState == TIMER_ACTIVE) { // then pause
             mTimerState = TIMER_PAUSED;
 
             // change ui state of button to play, make the progress pie yellow
             // get view
             ImageView d = (ImageView) ((MindTimer) mCtx).getListView()
                     .findViewWithTag(mId);
             d.setImageResource(R.drawable.slowpoke_play_button);
             
             mTimer.cancel();
 
         } else if (mTimerState == TIMER_NOT_STARTED || mTimerState == TIMER_PAUSED) { // then commence
             mTimerState = TIMER_ACTIVE;
             
             if(mTimerState == TIMER_PAUSED){
                 // recommence;
                 // alarm will now ding at now plus the duration minus the seconds elapsed 
                 mAlarmDingAt = SystemClock.elapsedRealtime();
                 
             }
             else{
                 
             }
             
             // When the alarm goes off, we want to broadcast an Intent to our
             // BroadcastReceiver. Here we make an Intent with an explicit class
             // name to have our own receiver (which has been published in
             // AndroidManifest.xml) instantiated and called, and then create an
             // IntentSender to have the intent executed as a broadcast.
             Intent intent = new Intent(v.getContext(), MindTimerAlarm.class);
             PendingIntent sender = PendingIntent.getBroadcast(v.getContext(),
                     0, intent, 0);
 
             // Schedule the alarm!
             mStartTimeMillis = SystemClock.elapsedRealtime();
             mAlarmDingAt = mStartTimeMillis + (mSecDuration * 1000);
             AlarmManager am = (AlarmManager) v.getContext().getSystemService(
                     Activity.ALARM_SERVICE);
             am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mAlarmDingAt, sender);
 
             mTimer = new Timer();
             mTimerTask = new UpdateTimeTask();
             mTimer.schedule(mTimerTask, 500, 500);
 
             // Tell the user about what we did.
             if (mToast != null) {
                 mToast.cancel();
             }
 
             mToast = Toast.makeText(v.getContext(),
                     R.string.one_shot_scheduled, Toast.LENGTH_LONG);
             mToast.show();
             
             
         }
 
     }
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
 
             // get view
             ImageView d = (ImageView) ((MindTimer) mCtx).getListView()
                     .findViewWithTag(mId);
 
             if (d != null) {
                 d.setImageResource(R.drawable.slowpoke_pause_button);
 
                 // get pie progress view and update it
                 ShapeDrawable drabble = new PieShapeDrawable(-90,
                         (Float) msg.obj, 0, 0, 50, 50, 0xff74AC23);
                
                LayerDrawable bg = (LayerDrawable) d.getBackground();
                
                int layerId = bg.getId(1);
                
                bg.setDrawableByLayerId(layerId, drabble);
                
                d.setBackgroundDrawable(bg);
             }
 
         }
     };
 
 }
