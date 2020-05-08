 package com.silare.nightowl;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.DialogFragment;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MessagesActivity extends FragmentActivity
 {
 	private EditText phoneNumber;
 	private EditText message;
 	private Button sendSMS;
 	private TextView dateSpinner;
 	private TextView timeSpinner;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_messages);
 		
 		phoneNumber = (EditText) findViewById(R.id.phone_number);
 		message = (EditText) findViewById(R.id.message);
 		
 		sendSMS = (Button) findViewById(R.id.send_sms);
 		sendSMS.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				sendSMS(phoneNumber.getText().toString(), message.getText()
 						.toString());
 			}
 		});
 		
 		initializeDateSpinner();
 		initializeTimeSpinner();
 	}
 	
 	
 	private void initializeDateSpinner()
 	{
 		dateSpinner = (TextView) findViewById(R.id.date_spinner);
		dateSpinner.setText(SMSSendReceiver.calendarDateString(Calendar
				.getInstance()));
 		
 		View.OnTouchListener dateSpinnerOnTouch = new View.OnTouchListener()
 		{
 			public boolean onTouch(View v, MotionEvent event)
 			{
 				if (event.getAction() == MotionEvent.ACTION_UP)
 				{
 					showDatePickerDialog(v);
 				}
 				return true;
 			}
 		};
 		View.OnKeyListener dateSpinnerOnKey = new View.OnKeyListener()
 		{
 			public boolean onKey(View v, int keyCode, KeyEvent event)
 			{
 				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
 				{
 					showDatePickerDialog(v);
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 		};
 		
 		dateSpinner.setOnTouchListener(dateSpinnerOnTouch);
 		dateSpinner.setOnKeyListener(dateSpinnerOnKey);
 	}
 	
 	
 	private void initializeTimeSpinner()
 	{
 		timeSpinner = (TextView) findViewById(R.id.time_spinner);
		timeSpinner.setText(SMSSendReceiver.calendarTimeString(Calendar
				.getInstance()));
 		
 		View.OnTouchListener timeSpinnerOnTouch = new View.OnTouchListener()
 		{
 			public boolean onTouch(View v, MotionEvent event)
 			{
 				if (event.getAction() == MotionEvent.ACTION_UP)
 				{
 					showTimePickerDialog(v);
 				}
 				return true;
 			}
 		};
 		View.OnKeyListener timeSpinnerOnKey = new View.OnKeyListener()
 		{
 			public boolean onKey(View v, int keyCode, KeyEvent event)
 			{
 				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
 				{
 					showDatePickerDialog(v);
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 		};
 		
 		timeSpinner.setOnTouchListener(timeSpinnerOnTouch);
 		timeSpinner.setOnKeyListener(timeSpinnerOnKey);
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.messages, menu);
 		return true;
 	}
 	
 	
 	public void sendSMS(String phoneNumber, String message)
 	{
 		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		Calendar time = Calendar.getInstance();
 		time.setTimeInMillis(System.currentTimeMillis());
 		time.add(Calendar.SECOND, 10);
 		Intent intent = new Intent(null, Uri.parse("timer:" + time
 				+ phoneNumber + message), this, SMSSendReceiver.class);
 		
 		// Put information in intent.
 		intent.putExtra("time", time);
 		intent.putExtra("phoneNumber", phoneNumber);
 		intent.putExtra("message", message);
 		
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
 				intent, Intent.FLAG_ACTIVITY_NEW_TASK);
 		alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
 				pendingIntent);
 		Log.d("SendSMS",
 				"[" + SMSSendReceiver.calendarDateString(time) + " :: "
 						+ SMSSendReceiver.calendarTimeString(time)
 						+ time.get(Calendar.SECOND) + "]" + " " + phoneNumber
 						+ " << \"" + message + "\"");
 	}
 	
 	
 	public void showDatePickerDialog(View v)
 	{
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "Date");
 	}
 	
 	
 	public void showTimePickerDialog(View v)
 	{
 		DialogFragment newFragment = new TimePickerFragment();
 		newFragment.show(getFragmentManager(), "Time");
 	}
 }
