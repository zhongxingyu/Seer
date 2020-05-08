 package com.github.browep.wegu;
 
 import android.app.*;
import android.content.Intent;
 import android.os.Bundle;
import android.provider.SyncStateContract;
 import android.view.View;
 import android.widget.*;
 import android.widget.Button;
 import com.github.browep.wegu.util.Utils;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.widget.CheckBox;
 
 /**
  * Created by IntelliJ IDEA.
  * User: paul
  * Date: 1/20/11
  * Time: 10:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AlarmSetter extends WeguActivity {
     private static final int MILLIS_IN_THE_FUTURE = 5 * 1000;
     private int minutes;
     private int hours;
     private boolean[] days = new boolean[8];
 
 
     private AlarmSetter self = this;
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // initialize from stored or default to now
         hours = dao.getIntPreference(Constants.HOUR_OF_DAY);
         if(hours < 0)
             hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 
         minutes = dao.getIntPreference(Constants.MINUTE_OF_DAY);
         if(minutes < 0)
             minutes = Calendar.getInstance().get(Calendar.MINUTE);
         days = dao.getDays();
 
         // we have to set the content view before we try and find the days of the week as they wont be there if we dont
         setContentView(R.layout.alarm_setter);
 
         for (int i : new int[]{7,1,2,3,4,5,6} ) {
 
             final int id = i;
             final CheckBox checkBox;
 
             checkBox = new CheckBox(getApplicationContext());
             checkBox.setChecked(days[i]);
 
             checkBox.setText(Constants.DAY_MAP_NAMES[i]);
             checkBox.setId(Constants.CHECKBOX_PREPEND + i);
             checkBox.setOnClickListener(new View.OnClickListener(){
                 public void onClick(View view) {
                     days[id] = checkBox.isChecked();
                 }
             });
             if(!(i == 1 || i== 7))
                 ((LinearLayout) findViewById(R.id.days_of_week_layout)).addView(checkBox);
             else
                 ((LinearLayout) findViewById(R.id.sat_sun_layout)).addView(checkBox);
 
         }
 
 
         Button button = (Button) findViewById(R.id.start_alarm);
         button.setOnClickListener(setAlarmClickListener);
 
         Button timeButton = (Button) findViewById(R.id.time_display);
         timeButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(Constants.TIME_DIALOG_ID);
             }
         });
 
         // update the time display
         updateTimeDisplay();
 
 
     }
 
     private void updateTimeDisplay() {
         // time button
         Button timeButton = (Button) findViewById(R.id.time_display);
 
         timeButton.setText(Utils.getDisplayHourOfDay(hours) + ":" + String.format("%02d",minutes) + " " + Utils.getAMorPM(hours));
     }
 
 
     private  View.OnClickListener setAlarmClickListener = new View.OnClickListener() {
         public void onClick(View view) {
             // check to make sure at least one day has ben checked
             List<String> daysAdded = daysAddedStringList(days);
             if(daysAdded.isEmpty()){
                 Utils.longToastMessage(getApplicationContext(),"You must select at least one day for the alarm go off.");
                 return;
             }
 
             for (int i = 0; i < Constants.DAY_MAP.length; i++) {
                 dao.setBooleanPreference(Constants.DAY_PREPEND + String.valueOf(i), days[i]);
             }
 
             StringBuilder sb = new StringBuilder("Alarm will go off on ");
             sb.append(Utils.join(daysAdded,", "));
             dao.setIntPreference(Constants.HOUR_OF_DAY, hours);
             dao.setIntPreference(Constants.MINUTE_OF_DAY, minutes);
             sb.append(" at " ).append(Utils.getDisplayHourOfDay(hours)).append(":").append(String.format("%02d",minutes)).append(" ").append(Utils.getAMorPM(hours));
             Utils.reallyLongToastMessage(getApplicationContext(), sb.toString());
 
             // cancel any old ones
             AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
             am.cancel(Utils.getAlarmPendingIntent(getApplicationContext()));
 
             // set the new one
//            for (int i = 0; i < Constants.DAY_MAP.length; i++) {
//                if (days[i]) {
//                    Intent intent = new Intent(AlarmSetter.this, AlarmReceiver.class);
//                    PendingIntent sender = PendingIntent.getBroadcast(AlarmSetter.this,
//                            0, intent, 0);
//                    am.setRepeating(AlarmManager.RTC_WAKEUP, Utils.timestampOfNextOccurenceOfDayAtTime(i, hours, minutes), Constants.WEEK_INTERVAL_MILLIS, sender);
//                }
//            }
            // We want the alarm to go off 30 seconds from now.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 12);

            Intent intent = new Intent(AlarmSetter.this, AlarmReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmSetter.this,
                    0, intent, 0);
            // Schedule the alarm!
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), Constants.WEEK_INTERVAL_MILLIS, sender);
 
             finish();
 
         }
     } ;
 
 
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case Constants.TIME_DIALOG_ID:
 
                 return new TimePickerDialog(this,
                         mTimeSetListener, hours, minutes, false);
         }
         return null;
     }
 
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
     new TimePickerDialog.OnTimeSetListener() {
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
             minutes = minute;
             hours = hourOfDay;
             updateTimeDisplay();
         }
     };
 
 }
