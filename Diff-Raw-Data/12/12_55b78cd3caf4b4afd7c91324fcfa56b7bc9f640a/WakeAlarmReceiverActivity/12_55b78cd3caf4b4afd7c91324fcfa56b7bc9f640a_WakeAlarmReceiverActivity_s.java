 package edu.uci.asleepawake;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class WakeAlarmReceiverActivity extends Activity {
 	private MediaPlayer mMediaPlayer;
 	SharedPreferences sp;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_wake_alarm_receiver);
 		
 		if(SurveyAlarmReceiverActivity.instance != null){
 			try {
 				SurveyAlarmReceiverActivity.instance.finish();
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
 		}
 		
 		final int intentID = getIntent().getIntExtra("intentID", 0);
 	    System.out.println("Wake Intent ID: "+intentID);
 		
 	    sp = PreferenceManager.getDefaultSharedPreferences(this);
 	    final String sleepLogged = sp.getString("sleepLogged", "");
 	    
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				WakeAlarmReceiverActivity.this);
 		if(sleepLogged.equals("YES")){
 			alertDialogBuilder.setMessage("Time to take off your sleep watch and put it in a safe place");
 		} else {
 			alertDialogBuilder.setMessage("Please log your sleep and wake time");			
 		}
 		alertDialogBuilder
 				.setTitle("Good morning!")
 //				.setMessage("Time to take off your sleep watch and put it in a safe place")
 				.setCancelable(false)
 				.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								mMediaPlayer.stop();
 								
 						        Intent intent = new Intent(WakeAlarmReceiverActivity.this, WakeAlarmReceiverActivity.class);
 //						        intent.putExtra("intentID", 12345);
 						        PendingIntent pendingIntent = PendingIntent.getActivity(WakeAlarmReceiverActivity.this,
 						            intentID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 						        AlarmManager am = 
 						            (AlarmManager)WakeAlarmReceiverActivity.this.getSystemService(ALARM_SERVICE);
 						        am.cancel(pendingIntent);
 								
 						        if(sleepLogged.equals("YES")){
 								Intent backToMainIntent = new Intent(WakeAlarmReceiverActivity.this, MainActivity.class);
 //							    backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 								WakeAlarmReceiverActivity.this.startActivity(backToMainIntent);
 						        } else {
 									Intent formIntent = new Intent(WakeAlarmReceiverActivity.this, Form.class);
 									WakeAlarmReceiverActivity.this.startActivity(formIntent);						        	
 						        }
 							}
 						})
 				.setNegativeButton("Snooze",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								mMediaPlayer.stop();
 //						        Intent intent = new Intent(AlarmReceiverActivity.this, FirstAlarmDismissed.class);
 //						        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //						        AlarmReceiverActivity.this.startActivity(intent);
 								
 						        //Create an offset from the current time in which the alarm will go off.
 						        Calendar cal = Calendar.getInstance();
 						        cal.add(Calendar.MINUTE, 10);
 
 						        //Create a new PendingIntent and add it to the AlarmManager
 						        Intent intent = new Intent(WakeAlarmReceiverActivity.this, WakeAlarmReceiverActivity.class);
 						        System.out.println("Wake Intent ID: "+intentID);
 						        intent.putExtra("intentID", intentID);
 						        PendingIntent pendingIntent = PendingIntent.getActivity(WakeAlarmReceiverActivity.this,
 						        		intentID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 						        AlarmManager am = 
 						            (AlarmManager)WakeAlarmReceiverActivity.this.getSystemService(ALARM_SERVICE);
 						        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
 						                pendingIntent);
 								
 								finish();
 							}
 						});
 
 		AlertDialog alert = alertDialogBuilder.create();
 		alert.show();
 
 		playSound(this, getAlarmUri());
 	}
 
 	private void playSound(Context context, Uri alert) {
 		mMediaPlayer = new MediaPlayer();
 		try {
 			mMediaPlayer.setDataSource(context, alert);
 			final AudioManager audioManager = (AudioManager) context
 					.getSystemService(Context.AUDIO_SERVICE);
 			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
 				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
 				mMediaPlayer.prepare();
 				mMediaPlayer.start();
 			}
 		} catch (IOException e) {
 			System.out.println("OOPS");
 		}
 	}
 
 	// Get an alarm sound. Try for an alarm. If none set, try notification,
 	// Otherwise, ringtone.
 	private Uri getAlarmUri() {
 		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 		if (alert == null) {
 			alert = RingtoneManager
 					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 			if (alert == null) {
 				alert = RingtoneManager
 						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
 			}
 		}
 		return alert;
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.wake_alarm_receiver, menu);
 		return true;
 	}
 
 }
