 package de.Psychologie.socialintelligence;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
 * @class Alarm_Activity
 * @brief Zeigt den Audio/Visuellen Wecker an.
 * @author Christian Steusloff, Jens Wiemann, Franz Kuntke und Patrick Wuggazer
 * @date 16/06/2013
 * @file Alarm_Activity.java
 
 */ 
 public class Alarm_Activity extends Activity {
 
 	private static final int SNOOZE_ID = 111;
 	private static final int HITHERE_ID = 1;
 	private CountDownTimer waitTimer;
 	private PowerManager.WakeLock wl;
 	private MediaPlayer mMediaPlayer;
 	private SharedPreferences prefs;
 	private boolean vibrate = true;
 	private Vibrator vib;
 	private NotificationManager notificationManager = null;
	private boolean pauseOK=true;
 	
 	/**
 	 * @param savedInstanceState
 	 * @brief Erzeugt eine Fullscreen View mit angeschaltetem Bildschirm, Klingelton und Vibration so wie in den Einstellungen hinterlegt.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);   
 		setContentView(R.layout.activity_alarm);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 		// Stay unlocked if not pw-secured
 		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
 
 		// aktive Nachricht l�schen
 		cancelNotification();
 		
 		TextView text = (TextView) findViewById(R.id.textview_alarm);
 		Calendar c = Calendar.getInstance();		
 		text.setText(FormatHandler.withNull(c.get(Calendar.HOUR_OF_DAY))+":"+FormatHandler.withNull(c.get(Calendar.MINUTE))+" Uhr\nExperiment Umfrage");
 		
 		
 		Button btn_action = (Button) findViewById(R.id.btn_action_front);
 		btn_action.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_green_chooser));
 		btn_action.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				pauseOK=true;
 				closeAll();
 				startActivity(new Intent(Alarm_Activity.this,PopPollActivity.class));
          		overridePendingTransition(0, 0);
 				finish();
 			}
 		});
 
 		Button btn_sleep = (Button) findViewById(R.id.btn_snooze_front);
 		btn_sleep.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.button_red_chooser));
 		btn_sleep.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				pauseOK=true;
 				closeAll();
 				setSnooze();
 				finish();
 			}
 		});
 		// Play selected Ringtone
 		prefs = PreferenceManager.getDefaultSharedPreferences(Alarm_Activity.this);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Lights are ON");
 		wl.acquire();
 
 		mMediaPlayer = new MediaPlayer();
 		try {
 			mMediaPlayer.reset();
 			String path = prefs.getString("ringtone",RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(), RingtoneManager.TYPE_ALARM).toString());
 			mMediaPlayer.setDataSource(path);		    
 			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
 			mMediaPlayer.setLooping(true);
 			mMediaPlayer.prepare();
 			mMediaPlayer.start();
 
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		vibrate = prefs.getBoolean("vibrate", true);
 		
 		waitTimer = new CountDownTimer(60000, 1000) {
 
 			public void onTick(long millisUntilFinished) {
 				if (vibrate){
 					vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 					// Vibrate for 500 milliseconds
 					vib.vibrate(500);
 				}
 			}
 
 			public void onFinish() {
 				pauseOK=true;
 				closeAll();
 				setSnooze();
 				finish();
 			}
 		}.start();
 	}
 	
 	/**
 	 * @brief Snoozezeit wird gesetzt
 	 */
 	private void setSnooze(){
 		pauseOK=true;
 		//Snoozezeit aus den Settings auslesen, sonst 5 Minuten
 		String time= prefs.getString("Sleeptime", "5");
 		int snoozetime = Integer.parseInt(time);
 		// Datenbank Verbindung aufbauen
 		SQLHandler db = new SQLHandler(this);
 		Alarm pollAlarm = new Alarm(this);
 		// prfen, ob Snoozetime nicht grer ist als die Zeit bis zum nchsten Alarm
 		int checkDifference = pollAlarm.getDifferenceToNextAlarm();
 		if(checkDifference > 0 && snoozetime > checkDifference){
 			// Umfrage speichern
 			Calendar cal = Calendar.getInstance();
             String date = FormatHandler.withNull(cal.get(Calendar.DAY_OF_MONTH)) + "." + FormatHandler.withNull((cal.get(Calendar.MONTH)+1)) + "." + cal.get(Calendar.YEAR);
             String alarmTime = pollAlarm.getCurrentAlarmTime();
 
             pollAlarm.setNextAlarm();
             db.setSnoozeActiv(false);
             //Werte in die DB eintragen
             db.setPollEntry(date, alarmTime);
             Toast.makeText(getApplicationContext(), getResources().getString(R.string.txtPopPollBreak), Toast.LENGTH_LONG).show();
 		} else {
 			pollAlarm.setSnooze(snoozetime);
 			db.setSnoozeActiv(true);
 			// Meldung
 			Toast.makeText(Alarm_Activity.this,getResources().getString(R.string.txtPopPollSnooze), Toast.LENGTH_LONG).show();
 			// Notification setzen
 			setNotification();
 		}
 	}
 	
 	/**
 	 * @brief Notification setzen, wenn Snooze gedrückt wurde
 	 */
 	private void setNotification(){
 		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		
 		// Meldung (im Durchlauf) definieren
 		int icon          = R.drawable.ic_stat_notify;
 		CharSequence text = "Schlummerfunktion aktiv!";
 		long time         = System.currentTimeMillis();
 		
 		// Meldung setzen
 		Notification notification = new Notification(icon, text, time);
 		
 		// Meldung schliessen
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		
 		// Meldungstext, wenn gewaehlt
 		Context context = getApplicationContext();
 		CharSequence contentTitle = "Umfrage";
 		CharSequence contentText  = "Bitte beantworten Sie die Umfrage.";
 		
 		Intent notificationIntent = new Intent(this, PopPollActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, 1);
 	
 		// Ton hinzufuegen
 		//notification.defaults |= Notification.DEFAULT_SOUND;
 		
 		// Vibration beoetigt zusaetzliches Recht
 		//notification.defaults |= Notification.DEFAULT_VIBRATE;
 		
 		// Licht
 		//notification.defaults |= Notification.DEFAULT_LIGHTS;
 
 		
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 		//notification.setLatestEventInfo(context, contentTitle, contentText, null);
 		
 		// NotificationManager bekommt Meldung
 		notificationManager.notify(HITHERE_ID, notification);
 	}
 	
 	// Forbid closing the view
 	/**
 	 * @brief Schließen der View durch tippen auf den Bildschirm wird verhindert
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
 	}
 	
 	private void cancelNotification(){
 		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.cancel(SNOOZE_ID);	
 	}
 	@Override
 	public void onDestroy(){
 		closeAll();
 		super.onDestroy();
     }

	@Override
	public void onStart(){
		pauseOK=false;
		super.onStart();
    }
	
 	/*
 	@Override
 	public void onStop(){
 		if (!pauseOK){
			Log.e("Alarm Stop","Snooze gesetzt");
 			pauseOK = true;
 			setSnooze();
 		closeAll();
 		finish();
		}
 		super.onStop();
 	}
 	*/
 	
 	private void closeAll(){
 		if (vib!=null)
 			vib.cancel();
 		if (waitTimer != null) {
 			waitTimer.cancel();
 			waitTimer = null;
 		}
 		try {
 			if (mMediaPlayer != null) {
 				if (mMediaPlayer.isPlaying()) {
 					mMediaPlayer.stop();
 				}
 				mMediaPlayer.release();
 				mMediaPlayer = null;
 			}
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		}
 		try {
 			wl.release();
 		} catch (Throwable th) {
 			// ignoring this exception, probably wakeLock was
 			// already released
 		}
 	}
 
 }
