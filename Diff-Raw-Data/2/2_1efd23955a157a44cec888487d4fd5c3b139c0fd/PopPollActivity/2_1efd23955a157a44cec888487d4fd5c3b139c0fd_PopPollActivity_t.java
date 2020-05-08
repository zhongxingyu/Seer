 package de.Psychologie.socialintelligence;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.simonvt.numberpicker.NumberPicker;
 import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
 * @class PopPollActivity
 * @brief Umfrage-Activity
 * @author Christian Steusloff, Jens Wiemann, Franz Kuntke und Patrick Wuggazer
 * @date 16/06/2013
 * @file PopPollActivity.java
 */ 
 @SuppressLint("SimpleDateFormat")
 public class PopPollActivity extends Activity {
 	
 	private static final int SNOOZE_ID = 111;
 	private final int minPerHour = 60; 
 	private Button snooze_button;
 	private Button ok_button;
 	private Button cancel_button;
     private NumberPicker hourPicker;
 	private NumberPicker minutePicker;
 	/**
 	 * @brief Eingabefeld für die Anzahl der Komtakte
 	 */
 	private EditText countContact;
     private Alarm pollAlarm;
 	private Calendar cal;
 	private SharedPreferences prefs;
 	private SQLHandler db; 
 	private NotificationManager notificationManager = null;
 
 	private boolean action_done = false;
 	private int difHour;
 	private int difMinute;
 	/**
 	 * @brief formats the time to "09:00"
 	 */
 	private static final NumberPicker.Formatter NUMBER_FORMATTER = new NumberPicker.Formatter() {
 		@Override
 		public String format(int value) {
 			return FormatHandler.withNull(value);
 		}
 	};
 	/**
 	 * @brief Erstellt Umfrage
 	 */
 	@SuppressWarnings("deprecation")
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ActivityRegistry.register(this);
 		setContentView(R.layout.activity_pop_poll);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 		
 
 		// Kalender Instanze setzen
 		cal = Calendar.getInstance();
 		
 		prefs = PreferenceManager.getDefaultSharedPreferences(PopPollActivity.this);
 		// Datenbank Verbindung aufbauen
 		db = new SQLHandler(PopPollActivity.this);
 		
 		// App startet -> hinterlegten Klingelton abspielen
 		// Meldung etc. Wenn Handy gesperrt �ffnet sich zwar die App, aber User bekommt nix von mit :-)
 		
 		pollAlarm = new Alarm(this);
 		hourPicker = (NumberPicker) findViewById(R.id.hourPicker);
 		hourPicker.setFormatter(NUMBER_FORMATTER);
 		minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
 		
 		
 		minutePicker.setFormatter(NUMBER_FORMATTER);
 		snooze_button = (Button) findViewById(R.id.snooze_button);
 		snooze_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red_chooser));
 		ok_button=(Button) findViewById(R.id.ok_button);
 		ok_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_green_chooser));
 		cancel_button = (Button) findViewById(R.id.cancel_button);
         cancel_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red_chooser));
 		// Eingabefeld Kontaktpersonen
 		countContact=(EditText) findViewById(R.id.countContact);
 		countContact.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				enableOrDisableButton();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 			
 		// Blendet Tastatur aus, sobald au�erhalb des Feldes zur Eingabe geklickt wird.
 		LinearLayout completeView = (LinearLayout) findViewById(R.id.LinearLayout1);
 		completeView.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(countContact.getWindowToken(), 0);
 			}
 		});
 
 
 
         TextView txtPopPollInfo = (TextView) findViewById(R.id.txtPopPollInfo);
 		
 		String lastAlarm = db.getLastAlarm();
 		Log.v("lastAlarm",lastAlarm);
 		if(lastAlarm.compareTo("00:00:00") == 0){
 			txtPopPollInfo.setText(Html.fromHtml(getResources().getString(R.string.txtPopPollInfo0)));
 		} else {
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
 			Date lastTime = cal.getTime();
 			try {
 				lastTime = sdf.parse(lastAlarm);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			Date nowTime = cal.getTime();
 			
 			if(lastTime.compareTo(nowTime) < 0){
 				// heute
 				txtPopPollInfo.setText(Html.fromHtml(getResources().getString(R.string.txtPopPollInfo1, lastAlarm.substring(0, 5))));
 			} else{
 				// gestern
 				txtPopPollInfo.setText(Html.fromHtml(getResources().getString(R.string.txtPopPollInfo2, lastAlarm.substring(0, 5))));
 			}
 		}
 	
 		// Zeitdifferenz
 		int lastHour = Integer.valueOf(lastAlarm.substring(0,2));
 		int lastMinute = Integer.valueOf(lastAlarm.substring(3,5));
 		//Log.v("oldHour",String.valueOf(lastHour));
 		//Log.v("oldMin",String.valueOf(lastMinute));
 		//Log.v("newHour",String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
 		//Log.v("newMin",String.valueOf(cal.get(Calendar.MINUTE)));
 	
 		Calendar oldAlarm = Calendar.getInstance();
 		oldAlarm.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), lastHour, lastMinute);
 		
 		long diffInMS;
 		if (cal.getTimeInMillis() < oldAlarm.getTimeInMillis())
 			diffInMS = cal.getTimeInMillis() +(86400000-oldAlarm.getTimeInMillis());
 		else
 			diffInMS = cal.getTimeInMillis() - oldAlarm.getTimeInMillis();
 		// If oldAlarm was the day before e.g. old= 23:59 new= 1:00 diff=1:01 and NOT diff=22:59
 		// must add 24h (86400000ms)
 		
 		long diffInMin = (long) (diffInMS/60000);
 		//Log.v("DiffInMin","="+String.valueOf(diffInMin));
 		
 		difHour = (int) (diffInMin/minPerHour);
 		difMinute = (int) (diffInMin%minPerHour);
 		
 		// Zeitauswahl
 		hourPicker.setMaxValue(difHour);
 		hourPicker.setMinValue(0);
 		//hourPicker.setFocusable(true);
 		//hourPicker.setFocusableInTouchMode(true);
 		
 		if(difHour == 0){
 			minutePicker.setMaxValue(difMinute);
 		} else {
 			minutePicker.setMaxValue(59);
 		}
 		minutePicker.setMinValue(0);
 		//minutePicker.setFocusable(true);
 		//minutePicker.setFocusableInTouchMode(true);
 		minutePicker.setOnValueChangedListener(new OnValueChangeListener(){
 
 			@Override
 			public void onValueChange(NumberPicker picker, int oldVal,
 					int newVal) {
 				enableOrDisableButton();				
 			}
 			
 		});
 		hourPicker.setOnValueChangedListener(new OnValueChangeListener(){
 
 			@Override
 			public void onValueChange(NumberPicker picker, int oldVal,
 					int newVal) {
 				if(hourPicker.getValue() == difHour){
 					minutePicker.setMaxValue(difMinute);
 				} else {
 					minutePicker.setMaxValue(59);
 				}
 				enableOrDisableButton();				
 			}
 			
 		});
 		
 		// Wenn Snooze gedr�ck wird
 		snooze_button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				setSnooze();
 				ActivityRegistry.finishAll();
 				action_done=true;
 			}
 		});
 		
 		ok_button.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//Gesamtdauer der Kontakte
 				int hour = hourPicker.getValue();
 				int minute = minutePicker.getValue();
 				//Anzahl der Kontakte
 				int contacts = Integer.parseInt(countContact.getText().toString());
 				Calendar cal = Calendar.getInstance();
 				//Zeitpunkt der Antwort
				String answerTime = FormatHandler.withNull(cal.get(Calendar.HOUR_OF_DAY))+":"+FormatHandler.withNull(cal.get(Calendar.MINUTE))+":00";
 				//Datum
 				String date = FormatHandler.withNull(cal.get(Calendar.DAY_OF_MONTH)) + "." + FormatHandler.withNull((cal.get(Calendar.MONTH)+1))+"."+cal.get(Calendar.YEAR);
 				
 				//Alarmzeit
 				String alarmTime=pollAlarm.getCurrentAlarmTime();
 				//String lastAlarmTime = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":00";
 				//nächsten Alarm setzen
 				pollAlarm.setNextAlarm();
 				db.setSnoozeActiv(false);
 				// letzen Alarm anpassen
 				db.setLastAlarm(answerTime);
 				action_done=true;
 				db.setPollEntry(date, alarmTime, answerTime, false, contacts, hour, minute);
 				// Meldung
 				Toast.makeText(getApplicationContext(),getResources().getString(R.string.txtPopPollOK), Toast.LENGTH_LONG).show();
 				ActivityRegistry.finishAll();			
 			}
 		});
 		
 		cancel_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(PopPollActivity.this);
                 builder.setTitle(getResources().getString(R.string.txtPopPollBreakTitle));
                 builder.setMessage(getResources().getString(R.string.txtPopPollBreakText))
                         .setCancelable(false)
                         .setPositiveButton(getResources().getString(R.string.txtYes), new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 // Umfrage speichern
                                 String date = FormatHandler.withNull(cal.get(Calendar.DAY_OF_MONTH)) + "." + FormatHandler.withNull((cal.get(Calendar.MONTH)+1)) + "." + cal.get(Calendar.YEAR);
                                 String alarmTime = pollAlarm.getCurrentAlarmTime();
 
                                 pollAlarm.setNextAlarm();
                                 db.setSnoozeActiv(false);
                 				action_done=true;
                                 //Werte in die DB eintragen
                                 db.setPollEntry(date, alarmTime);
                                 Toast.makeText(getApplicationContext(), getResources().getString(R.string.txtPopPollBreak), Toast.LENGTH_LONG).show();
                                 // Alle Activitys beenden
                                 ActivityRegistry.finishAll();
                             }
                         })
                         .setNegativeButton(getResources().getString(R.string.txtNo), new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         });
                 AlertDialog alert = builder.create();
                 alert.show();
             }
         });
 		
 		
 		
 		// Wenn OK gedr�ckt
 		// letzten Alarm (ist dieser momentane Alarm) setzen
 		// Calendar cal = Calendar.getInstance();
 		// String lastAlarmTime = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":00";
 		// db.setLastAlarm(lastAlarmTime);
 		//db.setPollEntry(date, alarmTime, answerTime, abort, contacts, hour, minute)
 		
 		// Wenn Abbrechen gedr�ckt
 		//db.setPollEntry(date, alarmTime)
 
 		
 	}
 	@Override
 	
 	/**
 	 * @brief Notification wird gelöscht und Snooze wird auf inaktiv gesetzt
 	 */
 	public void onStart(){
 		//Delete notification
 		cancelNotification();
         db.setSnoozeActiv(false);
         action_done = false;
 		super.onStart();
 	}
 	/**
 	 * @brief Wenn die Umfrage nicht beantwortet wurde wird Snooze gesetzt und pausiert
 	 */
 	@Override
 	protected void onPause(){
 		if (!action_done)
 			setSnooze();		
 		super.onPause();
 		
 	}
 	
 	/**
 	 * @brief Snoozezeit wird gesetzt TODO: Auslagern, da es in Alarm_Activity.java enthalten
 	 * 
 	 */
 	private void setSnooze(){
 		//Snoozezeit aus den Settings auslesen, sonst 5 Minuten
 		String time= prefs.getString("Sleeptime", "5");
 		int snoozetime = Integer.parseInt(time);
 		// prfen, ob Snoozetime nicht grer ist als die Zeit bis zum nchsten Alarm
 		int checkDifference = pollAlarm.getDifferenceToNextAlarm();
 		if(checkDifference > 0 && snoozetime > checkDifference){
 			// Umfrage speichern
             String date = FormatHandler.withNull(cal.get(Calendar.DAY_OF_MONTH)) + "." + FormatHandler.withNull((cal.get(Calendar.MONTH)+1)) + "." + cal.get(Calendar.YEAR);
             String alarmTime = pollAlarm.getCurrentAlarmTime();
 
             pollAlarm.setNextAlarm();
             db.setSnoozeActiv(false);
 			action_done=true;
             //Werte in die DB eintragen
             db.setPollEntry(date, alarmTime);
             Toast.makeText(getApplicationContext(), getResources().getString(R.string.txtPopPollBreak), Toast.LENGTH_LONG).show();
             // Alle Activitys beenden
             ActivityRegistry.finishAll();
 		} else {
 			pollAlarm.setSnooze(snoozetime);
 			db.setSnoozeActiv(true);
 			// Meldung
 			Toast.makeText(getApplicationContext(),getResources().getString(R.string.txtPopPollSnooze), Toast.LENGTH_LONG).show();
 			// Notification setzen
 			setNotification();
 		}
 	}
 	/**
 	 * @brief Notification wird gesetzt
 	 */
 	@SuppressWarnings("deprecation")
 	private void setNotification(){
 		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		
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
 		
 		Intent notificationIntent = new Intent(this, this.getClass());
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 1, notificationIntent, 1);
 	
 		// Ton hinzuf�gen
 		//notification.defaults |= Notification.DEFAULT_SOUND;
 		
 		// Vibration ben�tigt zus�tzliches Recht
 		//notification.defaults |= Notification.DEFAULT_VIBRATE;
 		
 		// Licht
 		//notification.defaults |= Notification.DEFAULT_LIGHTS;
 
 		
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 		
 		// NotificationManager bekommt Meldung
 		notificationManager.notify(SNOOZE_ID, notification);
 	}
 	/**
 	 * @brief Notification wird gelöscht
 	 */
 	private void cancelNotification(){
 		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.cancelAll();
 	}
 	
 	
 	/**
 	 * @brief Der Weiter Knopf wird aktiviert oder deaktiviert
 	 */
 	private void enableOrDisableButton(){
 		Integer parsi = 0;
 		Integer min = minutePicker.getValue();
 		Integer hour = hourPicker.getValue();
 		String toparse = countContact.getText().toString();
 		
 		if (toparse.length()==0){
 			return;
 		} else {
 			parsi = Integer.parseInt(toparse);
 		}
 
 		//Entweder ist Kontakte>0 und Dauer>0 	//Oder Kontakte=0 und Dauer = 0
 		if((parsi > 0) && ((hour>0)||(min>0))||
 		  ((parsi == 0) && ((hour==0) && (min==0)))){
 			ok_button.setEnabled(true);
 		} else if(parsi == 0){
 			minutePicker.setValue(0);
 			hourPicker.setValue(0);
 			ok_button.setEnabled(true);
 		} else {
 			ok_button.setEnabled(false);
 		}
 	}
 }
