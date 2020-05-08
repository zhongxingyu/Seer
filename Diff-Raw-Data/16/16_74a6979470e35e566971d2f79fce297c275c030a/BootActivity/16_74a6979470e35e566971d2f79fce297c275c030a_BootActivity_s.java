 package de.Psychologie.socialintelligence;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 @SuppressLint("SimpleDateFormat")
 public class BootActivity extends Activity{
 	private Alarm alarm;
 	private SQLHandler db; 
 	private Calendar cal;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Log.v("test","BootActivity");
 		
 		// Alarm holen
 		alarm = new Alarm(this);
 		// Datenbank Verbindung aufbauen
 		db = new SQLHandler(this);
 		// Kalenderinstanze
 		cal = Calendar.getInstance();
 		
 		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
 		// Aktuelle Zeit
 		Date now = cal.getTime();
 		// letzter Alarm
 		Date lastAlarm = null;
 		// nchster Alarm
 		Date nextAlarm = null;
 		try {
 			lastAlarm = df.parse(db.getLastAlarm());
 			nextAlarm = df.parse(db.getNextAlarm());
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 			
 		// wurde eine Umfrage verpasst in der Zeit als das Handy aus war?
 		if(nextAlarm != null && lastAlarm != null){
 			if((now.after(nextAlarm) && now.after(lastAlarm)) && nextAlarm.after(lastAlarm)){
 				// Umfrage wurde verpasst
 				startActivity(new Intent(this,PopPollActivity.class));
 				finish();
 			} else {
 				// Alarm wieder einstellen
 				alarm.setNextAlarm();
 			}
		} 
 	}
 }
