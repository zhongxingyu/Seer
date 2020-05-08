 package ru.moskva.fm;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 
 
 public class ListenRadio extends Activity implements OnClickListener {
 
     private static final String TAG = "Moskva.fm";
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         TimePicker timePicker = (TimePicker) findViewById(R.id.time_picker);
 
         Calendar c = Calendar.getInstance();
 
         c.add(Calendar.MINUTE, -5);
 
         Date date = c.getTime();
 
         timePicker.setCurrentHour(date.getHours());
         timePicker.setCurrentMinute(date.getMinutes());
         timePicker.setIs24HourView(true);
 
         View translation_button = findViewById(R.id.translation_button);
         translation_button.setOnClickListener(this);
 
         View play_button = findViewById(R.id.play_button);
         play_button.setOnClickListener(this);
 
         View stop_button = findViewById(R.id.stop_button);
         stop_button.setOnClickListener(this);
 
        View test_button = findViewById(R.id.test_button);
        test_button.setOnClickListener(this);

         updateButtonStates();
 
     }
 
     @Override
     public void onResume() {
 
         super.onResume();
 
         updateButtonStates();
     }
 
 
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.translation_button:
                 playNow();
                 break;
             case R.id.play_button:
                 playFrom();
                setButtonState(true);
                 break;
             case R.id.stop_button:
                 stopService(new Intent(this, PlayService.class));
                 setButtonState(false);
                 break;
            case R.id.test_button:
                break;
         }
     }
 
     private void playNow() {
         Calendar c = Calendar.getInstance();
 
         c.add(Calendar.MINUTE, -5);
 
         startPlay(c.getTime());
     }
 
     private void playFrom() {
         DatePicker datePicker = (DatePicker) findViewById(R.id.date_picker);
 
         TimePicker timePicker = (TimePicker) findViewById(R.id.time_picker);
 
         Date fromDate = new Date(
                 datePicker.getYear() - 1900,
                 datePicker.getMonth(),
                 datePicker.getDayOfMonth(),
                 timePicker.getCurrentHour(),
                 timePicker.getCurrentMinute());
 
         startPlay(fromDate);
     }
 
     private void startPlay(Date from) {
 
         Intent i = new Intent(this, PlayService.class);
 
         Calendar c = Calendar.getInstance();
 
         c.setTime(from);
 
         c.add(Calendar.MINUTE, -1);
 
         i.putExtra("datetime", c.getTime().getTime());
 
         startService(i);
 
     }
 
     private void updateButtonStates() {
 
         boolean isStarted = PlayService.isStarted;
 
         setButtonState(isStarted);
 
     }
 
     private void setButtonState(boolean state) {
 
         View translation_button = findViewById(R.id.translation_button);
 
         View play_button = findViewById(R.id.play_button);
 
         View stop_button = findViewById(R.id.stop_button);
 
         translation_button.setEnabled(!state);
 
         play_button.setEnabled(!state);
 
         stop_button.setEnabled(state);
 
     }
 
 }
