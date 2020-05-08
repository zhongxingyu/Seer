 /**
  * 
  */
 package com.example.itake;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /**
  * @author Wolfpack16 (Alex Kane)
  *
  */
 public class AlarmTimer extends Activity
 {
 	TimePicker myTimePicker;
 	Calendar Alarm;
 	Button SetAlarm;
 	Button CancelAlarm;
     TextView textAlarmPrompt;
     TimePickerDialog timePickerDialog;
     iTakeDatabase DBhelper;
     SQLiteDatabase db;
     
     ArrayList<PendingIntent> intentArray;
     
     private static int RQS_1 = 0; //Request Code for Intents
 
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {      
     	super.onCreate(savedInstanceState);
         DBhelper = new iTakeDatabase(this);
         intentArray = new ArrayList<PendingIntent>();
         
         setContentView(R.layout.main);
         
         AlarmRecreate();
     }
         
     @Override
     protected void onStart() 
     {
         super.onStart();
         textAlarmPrompt = (TextView) findViewById(R.id.alarmprompt);
         
         SetAlarm = (Button) findViewById(R.id.setAlarm);
         CancelAlarm = (Button) findViewById(R.id.cancelAlarm);    
 
         SetAlarm.setOnClickListener(new Button.OnClickListener()
         {
         	@Override
         	public void onClick(View arg0) 
         	{
         		// TODO Auto-generated method stub
         		textAlarmPrompt.setText("");
                 openTimePickerDialog(false);
         	}
         });
 
         CancelAlarm.setOnClickListener(new Button.OnClickListener()
         {
         	@Override
         	public void onClick(View arg0) 
         	{
         		// TODO Auto-generated method stub
         		if (intentArray.size() != 0)
         		{
 	        		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
 	        		alarmManager.cancel(intentArray.get(0));
 	        		
 	        		// Tell the user about what we did.
 	        		Toast.makeText(getBaseContext(), "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
         		}
         		else
         		{
         			Toast.makeText(getBaseContext(), "No Alarms Exist!", Toast.LENGTH_SHORT).show();
         		}
         	}
         });
         // The activity is about to become visible.
     }
     @Override
     protected void onRestart()
     {
     	super.onRestart();
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
     
     private void openTimePickerDialog(boolean is24r) 
     {
         Calendar calendar = Calendar.getInstance();
 
         timePickerDialog = new TimePickerDialog(AlarmTimer.this,
         		onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                 calendar.get(Calendar.MINUTE), is24r);
         
         timePickerDialog.setTitle("Set Alarm Time");
         timePickerDialog.show();
     }
 
     OnTimeSetListener onTimeSetListener = new OnTimeSetListener() 
     {
         @Override
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
         {
             Calendar calNow = Calendar.getInstance();
             Calendar calset = (Calendar) calNow.clone();
 
             calset.set(Calendar.HOUR_OF_DAY, hourOfDay);
             calset.set(Calendar.MINUTE, minute);
             calset.set(Calendar.SECOND, 0);
             calset.set(Calendar.MILLISECOND, 0);
 
             if (calset.compareTo(calNow) <= 0) 
             {
                 // Today Set time passed, count to tomorrow
                 calset.add(Calendar.DATE, 1);
             }
             setalarm(calset);
             dataSave(calset);
         }
     };    
 
     public void setalarm(Calendar targetCal)
     {   
     	try
     	{
 	        Intent intent = new Intent(getBaseContext(), AlarmOnReceive.class);
 	        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
 	        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
 	        
 	        Toast.makeText(getBaseContext(), "Alarm Created!", Toast.LENGTH_SHORT).show();
     	}
     	catch(Exception e)
         {
             System.out.println("Alarm Set Error: " + e.getLocalizedMessage());
             Toast.makeText(getBaseContext(), "Unable to Set Alarm. Please try again!", Toast.LENGTH_SHORT).show();
         }
     }
     
     // Save Alarm Time Data in Database
     public void dataSave(Calendar alarmtime)
     {
     	try
         {          
             //put DB in write mode
             db = DBhelper.getWritableDatabase();          		
 
             //insert variables into DB
             DBhelper.alarm_createRow(String.valueOf(alarmtime.getTimeInMillis()), String.valueOf(AlarmManager.INTERVAL_DAY));  
            
             // Check Alarm Outputs
             /*
             Cursor c = db.query("ALARMDATA", new String[] {"alarm_id", "time_data"}, null, null, null, null, null);
             
             int numRows = c.getCount();
             c.moveToFirst();
             for (int i = 0; i < numRows; ++i) 
             {
             	data += String.valueOf(c.getLong(0)) + c.getString(1);
                 c.moveToNext();
             }
             c.close();
 			
             Toast.makeText(getBaseContext(), data, Toast.LENGTH_LONG).show(); 
             */
             
             //close DB
             db.close();
             RQS_1++;
         }
         catch(Exception e)
         {
             System.out.println("Database Save Error: " + e.getLocalizedMessage());
         }
     }
     
     public void AlarmRecreate()
     {
     	// Recreate Alarms if any exist
         try
         {
 	        Cursor c = DBhelper.alarm_GetAllRows();
 	        
 	        int Id = 0;
 	        long time = 0;
 	     //   long Interval = 0;
 	        AlarmManager alarmManager;
 	        Intent intent;
 	        PendingIntent pendingIntent;
 	        
 	        int numRows = c.getCount();	        
 	        c.moveToFirst();
 	        
 	        for (int i = 0; i < numRows; ++i) 
 	        {
 	        	Id = c.getInt(0);
 	        	time = Long.parseLong(c.getString(1));
 	        //	Interval = Long.parseLong(c.getString(2));
 
 	        	intent = new Intent(getBaseContext(), AlarmOnReceive.class);
 	            pendingIntent = PendingIntent.getBroadcast(getBaseContext(), Id, intent, 0);
 	            
 	            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pendingIntent);
 	            
 	            c.moveToNext();
 	        }
 	        RQS_1 = Id + 1;
 	        
 	        Toast.makeText(getBaseContext(), String.valueOf(numRows) + " Alarms Recreated!", Toast.LENGTH_SHORT).show();
         }
         catch(Exception e)
         {
             System.out.println("Alarm Recreation Error: " + e.getLocalizedMessage());
         }
     }
 }
