 package com.pepsdev.timedlamp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class TimedLamp extends Activity {
     public static final String EXTRA_TIMEOUT = "com.pepsdev.timedlamp.Timeout";
     public static final String ACTION_ILLUMINATE = "com.pepsdev.timedlamp.illuminate";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
 
         setContentView(R.layout.main);
 
         Button button = (Button)findViewById(R.id.about_button);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Toast.makeText(TimedLamp.this,
                     R.string.about, Toast.LENGTH_LONG).show();
             }
         });
 
         tv = (TimedLampView)findViewById(R.id.backlamp_view);
         tv.setOnTiretteListener(new TimedLampView.OnTiretteListener() {
             @Override
             public void tiretted() {
                 lightItUp();
             }
             @Override
             public void unTiretted() {
                 switchOff();
 
                 synchronized (wl) {
                     if (wl.isHeld())
                         wl.release();
                 }
             }
         });
 
         final Intent callingIntent = getIntent();
         if (callingIntent.hasExtra(EXTRA_TIMEOUT)) {
             Log.d(TAG, "has EXTRA_TIMEOUT" + callingIntent.getIntExtra(EXTRA_TIMEOUT,
                                                      DEFAULT_TIMEOUT));
             lightItUp();
             startCountDown(callingIntent.getIntExtra(EXTRA_TIMEOUT,
                                                      DEFAULT_TIMEOUT));
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (!tv.isCountDownStarted()) {
             tv.reset();
         }
         wl.acquire();
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         if (tv.isCountDownStarted()) {
             // if brightness is not max_brightness it would mean that the user
             // changed the screen brightness in between
             if (getBrightness() == MAX_BRIGHTNESS) {
                 setBrightness(restoreBrightness);
             }
            synchronized (this) {
                 if (wl.isHeld())
                     wl.release();
             }
             tv.stopCountDown();
         }
     }
 
     public void startCountDown() {
         tv.startCountDown((int)tv.getTiretteDuration());
     }
 
     public void startCountDown(int coundDown) {
         tv.startCountDown(coundDown);
     }
 
     private void lightItUp() {
         tv.lightItUp();
         restoreBrightness = getBrightness();
         setBrightness(MAX_BRIGHTNESS);
     }
 
     private void switchOff() {
         setBrightness(DIM_BRIGHTNESS);
     }
 
     // 0 < brightness < 255
     private void setBrightness(int brightness) {
         Settings.System.putInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS, brightness);
 
         android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
         lp.screenBrightness = brightness / 255f; // 0.0 - 1.0
         getWindow().setAttributes(lp);
     }
 
     private int getBrightness() {
         int brightness;
         try {
             brightness = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.SCREEN_BRIGHTNESS);
         } catch (Settings.SettingNotFoundException e)  {
             // safe value
             brightness = MAX_BRIGHTNESS / 2;
         }
         return brightness;
     }
 
     private static final int DEFAULT_TIMEOUT = 60 * 1000;
     private static final int MAX_BRIGHTNESS = 255;
     private static final int DIM_BRIGHTNESS = 30;
 
     private static final String TAG = "com.pepsdev.timedlamp.TimedLamp";
 
     private int restoreBrightness;
     public TimedLampView tv;
 
     private PowerManager.WakeLock wl;
 }
 
