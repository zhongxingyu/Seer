 package com.feedme.activity;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.JournalDao;
 import com.feedme.model.Journal;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:27 PM
  */
 public class AddBreastFeedActivity extends Activity {
 
     private Button entryDate;
     private Button startTime;
     private Button endTime;
 
     private int mYear;
     private int mMonth;
     private int mDay;
 
     private int startHour;
     private int startMinute;
     private int startSecond;
 
     private int endHour;
     private int endMinute;
     private int endSecond;
 
     static final int DATE_DIALOG_ID = 0;
     static final int STARTTIME_DIALOG_ID = 1;
     static final int ENDTIME_DIALOG_ID = 2;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.add_breast_feed_entry);
         final JournalDao journalDao = new JournalDao(getApplicationContext());
 
         //get baby id
         Bundle b = getIntent().getExtras();
         final int babyId = b.getInt("babyId");
 
        //populate left/right spinner
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.entrySide, android.R.layout.simple_spinner_item );
         adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
         Spinner s = (Spinner) findViewById( R.id.entrySide );
         s.setAdapter( adapter );
 
        // button listener for add child button
         final EditText entryOunces = (EditText) findViewById(R.id.entryOunces);
         final Spinner entrySide = (Spinner) findViewById(R.id.entrySide);
         Button addEntryButton = (Button) findViewById(R.id.addEntryButton);
 
         entryDate = (Button) findViewById(R.id.entryDate);
         startTime = (Button) findViewById(R.id.addStartTime);
         endTime = (Button) findViewById(R.id.addEndTime);
 
         // add a click listener to the button
         entryDate.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 showDialog(DATE_DIALOG_ID);
             }
         });
 
         // add a click listener to the button
         startTime.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(STARTTIME_DIALOG_ID);
             }
         });
 
         // add a click listener to the button
         endTime.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(ENDTIME_DIALOG_ID);
             }
         });
 
         // get the current date
         final Calendar c = Calendar.getInstance();
 
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
 
         startHour = c.get(Calendar.HOUR_OF_DAY);
         startMinute = c.get(Calendar.MINUTE);
         startSecond = c.get(Calendar.SECOND);
 
         endHour = c.get(Calendar.HOUR_OF_DAY);
         endMinute = c.get(Calendar.MINUTE);
         endSecond = c.get(Calendar.SECOND);
 
         // display the current date
         updateDateDisplay();
         updateStartDisplay();
         updateEndDisplay();
         
         addEntryButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 /**
                  * CRUD Operations
                  * */
                 // Inserting entry
                 Log.d("Insert: ", "Inserting ..");
                 journalDao.addEntry(new Journal(entryDate.getText().toString(),
                         startTime.getText().toString(),
                         endTime.getText().toString(),
                         entrySide.getSelectedItem().toString(),
                        "",
                         babyId));
 
                 Intent intent = new Intent(v.getContext(), ViewEntriesActivity.class);
                intent.putExtra("babyId", babyId);
                 startActivityForResult(intent, 2);
 
               }
         });
 
     }
 
     @Override
     protected Dialog onCreateDialog(int id)
     {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
             case STARTTIME_DIALOG_ID:
                 return new TimePickerDialog(this,
                         startTimeListener, startHour, startMinute, false);
             case ENDTIME_DIALOG_ID:
                 return new TimePickerDialog(this,
                         endTimeListener, endHour, endMinute, false);
         }
         return null;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(AddBreastFeedActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(AddBreastFeedActivity.this,
                         SettingsActivity.class));
                 break;
         }
         return true;
     }
 
 
     // updates the date we display in the TextView
     private void updateDateDisplay()
     {
         entryDate.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(mMonth + 1).append("-")
                         .append(mDay).append("-")
                         .append(mYear).append(" "));
     }
 
     // updates the date we display in the TextView
     private void updateStartDisplay()
     {
         startTime.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(startHour + 1).append(":")
                         .append(startMinute).append(":")
                         .append(startSecond));
     }
 
     // updates the date we display in the TextView
     private void updateEndDisplay()
     {
         endTime.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(endHour + 1).append(":")
                         .append(endMinute).append(":")
                         .append(endSecond));
     }
 
     // the callback received when the user "sets" the date in the dialog
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener()
             {
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth)
                 {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     updateDateDisplay();
                 }
             };
     
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener startTimeListener =
             new TimePickerDialog.OnTimeSetListener() {
                 public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                 {
                     startHour = hourOfDay;
                     startMinute = minute;
                     updateStartDisplay();
                 }
             };
     
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener endTimeListener =
             new TimePickerDialog.OnTimeSetListener() {
                 public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                 {
                     endHour = hourOfDay;
                     endMinute = minute;
                     updateEndDisplay();
                 }
             };
     }
