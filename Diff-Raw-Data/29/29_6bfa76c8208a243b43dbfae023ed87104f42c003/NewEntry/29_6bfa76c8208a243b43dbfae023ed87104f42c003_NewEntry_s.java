 package edu.ucsb.cs.cs185.hw4;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.NumberPicker;
 
 public class NewEntry extends Activity
 {
 	private static final String FROM_YEAR = "FROM_YEAR";
 	private static final String FROM_MONTH = "FROM_MONTH";
 	private static final String FROM_DAY = "FROM_DAY";
 	private static final String FROM_HOUR= "FROM_HOUR";
 	private static final String FROM_MINUTE = "FROM_MINUTE";
 	
 	private static final String DURATION_MINUTE = "DURATION_MINUTE";
 	private static final String DURATION_HOUR = "DURATION_HOUR";
 	
 	private int m_from_Year = 2012, m_from_Month = 4, m_from_Day = 27;
 	private int m_from_Hour = 23, m_from_Minute = 59;
 	private int m_to_Year = 2012, m_to_Month = 4, m_to_Day = 27;
 	private int m_to_Hour = 23, m_to_Minute = 59;
 	private int m_duration_Hour = 0, m_duration_Minute = 0;
 	
 	
 	private DatePicker datePicker;
 	private TimePicker timePicker;
 	private NumberPicker hourPicker;
 	private NumberPicker minutePicker;
 	
 	private Button b_set;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newentry);
         
         //Get extra information if available
         //Went over this in Disc
         Bundle extras = getIntent().getExtras(); 
         if(extras != null) {
         	m_from_Year = extras.getChar(FROM_YEAR);
         	m_from_Month = extras.getChar(FROM_MONTH);
         	m_from_Day = extras.getChar(FROM_DAY);
         	m_from_Hour = extras.getChar(FROM_HOUR);
         	m_from_Minute = extras.getChar(FROM_MINUTE);
         	
         	m_duration_Hour = extras.getChar(DURATION_HOUR);
         	m_duration_Minute = extras.getChar(DURATION_MINUTE);
         }
         
         datePicker = (DatePicker) findViewById(R.id.datePicker);
         datePicker.init(m_from_Year, m_from_Month, m_from_Day, null);
 		datePicker.setMinDate(1072944000000L); // 1/1/2004 00:00 in epochs
 		datePicker.setMaxDate(1335553140000L); // 4/27/2012 11:59 in epochs
 		
         timePicker = (TimePicker) findViewById(R.id.timePicker);
         timePicker.setCurrentHour(m_from_Hour);
         timePicker.setCurrentMinute(m_from_Minute);
         
         /*
          * Below are for duration fields
          */
         hourPicker = (NumberPicker) findViewById(R.id.hourPicker);
         hourPicker.setMinValue(0);
         hourPicker.setMaxValue(47);
         hourPicker.setValue(m_duration_Hour); 
 
         minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
         minutePicker.setMinValue(0);
         minutePicker.setMaxValue(59);
         minutePicker.setValue(m_duration_Minute);
         
         b_set = (Button) findViewById(R.id.buttonSet);
         b_set.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				buttonSetOnClick(v);
 			}
 		});
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.actionbarnewentry, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.home:
             	finishEntry(false);
                 break;
             case R.id.settings:
             	/*
             	 * http://www.helloandroid.com/tutorials/how-display-alertdialog-your-android-application
             	 */
             	AlertDialog settings_dialog = new AlertDialog.Builder(NewEntry.this).create();
             	settings_dialog.setMessage("There are no settings yet!");
             	settings_dialog.setButton("OK", new DialogInterface.OnClickListener() {
             	      public void onClick(DialogInterface dialog, int which) {
            	 
             	       //nothing to do
            	 
             	    } });
             	settings_dialog.show();
             	break;
             case R.id.help:
             	/*
             	 * http://www.helloandroid.com/tutorials/how-display-alertdialog-your-android-application
             	 */
             	AlertDialog help_dialog = new AlertDialog.Builder(NewEntry.this).create();
             	help_dialog.setMessage("Name: Aaron Shepard. \nSoftware Version: Unknown. \nExtra Info: This project deserves an A+!");
             	
             	help_dialog.setButton("OK", new DialogInterface.OnClickListener() {
           	      public void onClick(DialogInterface dialog, int which) {
          	 
           	       //nothing to do
          	 
           	    } });
             	
             	help_dialog.show();
             	break;
             default:
                 return false;
         }
         
         return true;
     }
     
     public void buttonSetOnClick(View view)
     {
     	m_from_Day = datePicker.getDayOfMonth();
     	m_from_Month = datePicker.getMonth() + 1;
     	m_from_Year = datePicker.getYear();
     	m_from_Hour = timePicker.getCurrentHour();
     	m_from_Minute = timePicker.getCurrentMinute();
 
     	m_duration_Hour = hourPicker.getValue();
     	m_duration_Minute = minutePicker.getValue();
 
     	
     	finishEntry(true);
     }
     
     private void finishEntry(boolean send_data) {
     	Intent intent = this.getIntent();
     	
     	if(send_data) {
 			intent.putExtra(FROM_YEAR, m_from_Year);
 			intent.putExtra(FROM_MONTH, m_from_Month);
 			intent.putExtra(FROM_DAY, m_from_Day);
 			intent.putExtra(FROM_HOUR, m_from_Hour);
 			intent.putExtra(FROM_MINUTE, m_from_Minute);
			intent.putExtra(DURATION_MINUTE, m_duration_Hour);
			intent.putExtra(DURATION_HOUR, m_duration_Minute);
 			this.setResult(RESULT_OK, intent);
     	} else {
     		this.setResult(RESULT_CANCELED, intent);
     	}
     	finish();
     }
     
 }
