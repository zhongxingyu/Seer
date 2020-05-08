 /**
  * 
  */
 package com.example.itake;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * @author Wolfpack16 (R. Alex Kane)
  *
  */
 public class AlarmReceiver extends Activity 
 {
 	private MediaPlayer mMediaPlayer; 
 	private Vibrator phoneVibrate;
 	private AudioManager audioManager;
 
 	public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
         		WindowManager.LayoutParams.FLAG_FULLSCREEN);       
     }
 	
 	@Override
     protected void onStart() 
     {
         super.onStart();
         // The activity is about to become visible.
         
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AlarmReceiver.this);
         alertDialogBuilder.setTitle("Medicine Alert")
         				  .setMessage("Insert Medication(s) Name Here")
         				  .setCancelable(false); // User has to respond to dialog call
         
         alertDialogBuilder.setNegativeButton("Taken", new DialogInterface.OnClickListener() 
         {                   
         	public void onClick(DialogInterface dialog, int which) 
         	{
         		if(phoneVibrate != null)
         		{
         			phoneVibrate.cancel();
         		}
         		else
         		{
 	        		mMediaPlayer.stop();
         		}
                 AlarmReceiver.this.finish();
             } //end onClick.
         }); // end alertDialog.setButton.
         
         alertDialogBuilder.setPositiveButton("Remind Me Later", new DialogInterface.OnClickListener() 
         {                   
         	public void onClick(DialogInterface dialog, int which) 
         	{
         		if(phoneVibrate != null) // If phoneVibrate was set
         		{
         			phoneVibrate.cancel();
         		}
         		else // Else, Assume Alarm Sound was set
         		{
 	        		mMediaPlayer.stop();
         		}
                 AlarmReceiver.this.finish();
             } //end onClick.
         }); // end alertDialog.setButton.
         alertDialogBuilder.show();  
         playSound(AlarmReceiver.this, getAlarmUri());
     }
     @Override
     protected void onResume() 
     {
         super.onResume();
         // The activity has become visible (it is now "resumed").
     }
     @Override
     protected void onPause() 
     {
         super.onPause();
         // Another activity is taking focus (this activity is about to be "paused").
     }
     @Override
     protected void onStop() 
     {
         super.onStop();
         // The activity is no longer visible (it is now "stopped")
     }
     @Override
     protected void onDestroy() 
     {
         super.onDestroy();
         // The activity is about to be destroyed.
     }
 
 	private void playSound(AlarmReceiver alarmReceiver, Uri alert) 
     {
         mMediaPlayer = new MediaPlayer();
         try // Attempt Alarm Play
         {
             mMediaPlayer.setDataSource(getBaseContext(), alert);
             audioManager = (AudioManager) alarmReceiver.getSystemService(Context.AUDIO_SERVICE);
             if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) // If Volume isn't 0
             {
             	mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
             	mMediaPlayer.setVolume(audioManager.getStreamVolume(AudioManager.STREAM_RING), 
             			audioManager.getStreamVolume(AudioManager.STREAM_RING));
                 mMediaPlayer.setLooping(true);
                 mMediaPlayer.prepare();
                 mMediaPlayer.start();
             }
             else // If Volume is 0, vibrate
             {
             	// Get instance of Vibrator from current Context
             	phoneVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 
             	// Start without a delay
             	// Vibrate for 100 milliseconds
             	// Sleep for 1000 milliseconds
             	long[] pattern = {0, 1000, 1000};
 
             	// The '0' here means to repeat indefinitely
             	// '-1' would play the vibration once
             	phoneVibrate.vibrate(pattern, 0);
             }
         } 
         catch (IOException e) 
         {
             System.out.println("OOPS");
         }
     }
 
     //Get an alarm sound. Try for an alarm. If none set, try notification, 
     //Otherwise, ringtone.
     private Uri getAlarmUri() 
     {
         Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
         if (alert == null) 
         {
             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
             if (alert == null) 
             {
                 alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
             }
         }
         return alert;
     }
     
     // Update Alarm Time Data in Database
    /* public void alarmUpdate(int Id)
     {
     	iTakeDatabase DBhelper;
         SQLiteDatabase db;
         DBhelper = new iTakeDatabase(this);
     	try
         {             		
             //put DB in write mode
             db = DBhelper.getWritableDatabase();          		
 
             //insert variables into DB
             DBhelper.alarm_updateRow(Id, String.valueOf(alarmtime.getTimeInMillis() + Interval), 
             		String.valueOf(Interval)); 
             		
             Intent intent = new Intent(getBaseContext(), AlarmOnReceive.class);
         	PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
         	AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), 
         			AlarmManager.INTERVAL_DAY, pendingIntent);
 
             //close DB
             db.close();
         }
         catch(Exception e)
         {
             System.out.println("Database Update Error: " + e.getLocalizedMessage());
         }
     }*/
 }
