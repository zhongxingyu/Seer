 package com.niktorious.alarmix;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 
 
 public class WakeUpActivity extends Activity
 {
     private MediaPlayer m_mediaPlayer;
     private Alarm       m_alarm;
     private boolean     m_fSnooze = false;
     
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.wakeup);
         
         // Hook up the Snooze and Dismiss buttons
         Button butSnooze  = (Button) findViewById(R.id.butSnooze);
         Button butDismiss = (Button) findViewById(R.id.butDismiss);
         
         butSnooze.setOnClickListener(new OnClickListener() {
             public void onClick(View v)
             {
                 handleClickSnooze();
             }
         });
         
         butDismiss.setOnClickListener(new OnClickListener() {
             public void onClick(View v)
             {
                 handleClickDismiss(true);
             }
         });
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         
         // Get the alarm from the intent
         AlarmixApp app = (AlarmixApp) getApplicationContext();
         
         if (m_alarm == null)
         {
             int nId = getIntent().getIntExtra("alarmId", -1);
             
             if (nId == -1)
             {
                 Activity parent = getParent();
                 if (parent != null)
                 {
                     Intent i = parent.getIntent();
                     if (i != null)
                     {
                         nId = i.getIntExtra("alarmId", -1);
                     }
                 }
             }
             m_alarm = app.getAlarmById(nId);
         }
         
         // Make sure the screen is properly unlocked
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                              WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                              WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   |
                              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON   );
 
         // Set up the MediaPlayer
         m_mediaPlayer = new MediaPlayer();
         m_mediaPlayer.reset();
         m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
         m_mediaPlayer.setLooping(true);
         
         // Set up the random number generator and seed it with the current time
         Random generator = new Random(Calendar.getInstance().getTimeInMillis());
         
         // Select and set the random song that will be used for the alarm
         try
         {
             int ix = -1;
             String strPath = new String();
             File file = null;
             if (app.getModel().lstMediaPaths.size() > 0)
             {
                 ix = generator.nextInt(app.getModel().lstMediaPaths.size());
                 strPath = app.getModel().lstMediaPaths.get(ix);
                 file = new File(strPath);
             }
             
             if (file != null && file.exists())
             {
                 m_mediaPlayer.setDataSource(strPath);
                 
                 // Set up the display to show the current song
                 TextView tvMarqueeTitle = (TextView) findViewById(R.id.tvMarqueeTitle);
                 tvMarqueeTitle.setText(new String(strPath));
                 tvMarqueeTitle.setSelected(true);
             }
             else
             {
                 Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                 if(alert == null){
                     // alert is null, using backup
                     alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                     if(alert == null){  // I can't see this ever being null (as always have a default notification) but just in case
                         // alert backup is null, using 2nd backup
                         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
                     }
                 }
                 
                 m_mediaPlayer.setDataSource(this, alert);
                 
                 // Set up the display to hide the song name
                 TextView tvMarqueeTitle = (TextView) findViewById(R.id.tvMarqueeTitle);
                 tvMarqueeTitle.setText("");
                 tvMarqueeTitle.setSelected(true);
             }
         }
         catch (IllegalArgumentException e)
         {
             e.printStackTrace();
         }
         catch (SecurityException e)
         {
             // not much we can do here
             e.printStackTrace();
         }
         catch (IllegalStateException e)
         {
             // not much we can do here
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // not much we can do here
             e.printStackTrace();
         }
         
         try
         {
             m_mediaPlayer.prepare();
         }
         catch (IllegalStateException e)
         {
             // not much we can do here
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         m_mediaPlayer.start();
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
     }
     
     @Override
     protected void onPause()
     {
         super.onPause();
         if (!m_fSnooze)
         {
             handleClickDismiss(false);
             clean();
         }
     }
     
     @Override
     protected void onDestroy()
     {
         super.onDestroy();
         clean();
     }
     
     public void onConfigurationChanged(Configuration newConfig)
     {
         // Do nothing: just want to make sure that onPause isn't called when the orientation changes
         super.onConfigurationChanged(newConfig);
     }
     
     // Helpers
     private void handleClickSnooze()
     {
         m_fSnooze = true;
         
         // Set a new alarm 10 min from now
         AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         Intent alarmIntent = new Intent(this, AlarmReceiver.class);
         alarmIntent.putExtra("alarmId", m_alarm.nId);
         
         // Create the corresponding PendingIntent object
         PendingIntent alarmPI = PendingIntent.getBroadcast(this, m_alarm.nId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         
         // Register the alarm with the alarm manager
         Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 10);
         alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmPI);
         
         goToHome();
         finish(); // calling finish() will call clean() on the way out
     }
     
     private void handleClickDismiss(boolean fGoHome)
     {
         // do we need to set a new alarm? is this a one-shot alarm?
         if (!m_alarm.isOneShot())
         {
             // Set our alarm using the AlarmManager
             AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
             Intent alarmIntent = new Intent(this, AlarmReceiver.class);
             alarmIntent.putExtra("alarmId", m_alarm.nId);
             
             // Create the corresponding PendingIntent object
             PendingIntent alarmPI = PendingIntent.getBroadcast(this, m_alarm.nId, alarmIntent, 0);
             
             // Register the alarm with the alarm manager
             Calendar cal = m_alarm.getNextCalendar();
             alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmPI);
         }
         else
         {
             // Remove the one-shot alarm from the alarm list
             AlarmixApp app = (AlarmixApp) getApplicationContext();
             app.deleteAlarmById(m_alarm.nId);
             
             // Update the external list of alarms
             app.saveAlarmList(this, app.getModel().lstAlarms);
         }
         
         if (fGoHome)
         {
             goToHome();
             finish(); // calling finish() will call clean() on the way out
         }
     }
     
     // Clean up: make sure everything that we acquired is properly released
     private void clean()
     {
         m_alarm = null;
         if (m_mediaPlayer != null) m_mediaPlayer.release();
         WakeLocker.release();
     }
     
     // Bring up the homescreen
     private void goToHome()
     {
         // Go to the home screen when this activity ends
         Intent homeIntent= new Intent(Intent.ACTION_MAIN);
         homeIntent.addCategory(Intent.CATEGORY_HOME);
         homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(homeIntent);
     }
 }
