 package com.kuxhausen.projectmeet;
 
 import java.util.Calendar;
 import java.util.regex.Pattern;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class NewMeetingActivity extends Activity implements OnClickListener {
 
 	Button addPeople, setDuration, setTimeStart, setTimeEnd, setDayStart,
 			setDayEnd;
 	RadioGroup contactMethod;
 	CheckBox autoSelect;
 	EditText meetingName;
 	
 	Meeting m = new Meeting(); 
 	
 
 	static final int DURATION_DIALOG_ID = 0;
 	static final int START_TIME_DIALOG_ID = 1;
 	static final int STOP_TIME_DIALOG_ID = 2;
 	static final int START_DATE_DIALOG_ID = 3;
 	static final int STOP_DATE_DIALOG_ID = 4;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_newmeeting);
 
 		setDuration = (Button) findViewById(R.id.durationButton);
 		setDuration.setOnClickListener(this);
 
 		setTimeStart = (Button) findViewById(R.id.startTimeButton);
 		setTimeStart.setOnClickListener(this);
 
 		setTimeEnd = (Button) findViewById(R.id.stopTimeButton);
 		setTimeEnd.setOnClickListener(this);
 
 		setDayStart = (Button) findViewById(R.id.startDayButton);
 		setDayStart.setOnClickListener(this);
 
 		setDayEnd = (Button) findViewById(R.id.stopDayButton);
 		setDayEnd.setOnClickListener(this);
 
 		addPeople = (Button) findViewById(R.id.addPeopleButton);
 		addPeople.setOnClickListener(this);
 		
 		autoSelect = (CheckBox)findViewById(R.id.autoSelect);
 		meetingName = (EditText)findViewById(R.id.meetingNameEditText);
 		contactMethod = (RadioGroup)findViewById(R.id.contactMethodGroup);
 
 		final Calendar c = Calendar.getInstance();
 		m.year = c.get(Calendar.YEAR);
 		m.month = c.get(Calendar.MONTH);
 		m.startDay = c.get(Calendar.DAY_OF_MONTH);
		m.startHour = c.get(Calendar.HOUR_OF_DAY);
 		m.startMinute = c.get(Calendar.MINUTE);
 
 		m.stopHour = m.startHour + m.duration / 60;
		m.stopMinute = m.startMinute + m.duration % 60;
 		m.stopDay = m.startDay;
 
 		updateDisplay();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DURATION_DIALOG_ID:
 			return new TimePickerDialog(this, mDurationSetListener,
 					m.duration / 60, m.duration % 60, true);
 		case START_TIME_DIALOG_ID:
 			return new TimePickerDialog(this, mStartTimeSetListener,
 					m.startHour, m.startMinute, false);
 		case STOP_TIME_DIALOG_ID:
 			return new TimePickerDialog(this, mStopTimeSetListener, m.stopHour,
 					m.stopMinute, false);
 		case START_DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mStartDateSetListener, m.year,
 					m.month, m.startDay);
 		case STOP_DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mStopDateSetListener, m.year,
 					m.month, m.stopDay);
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
 		switch (id) {
 		case DURATION_DIALOG_ID:
 			((TimePickerDialog) dialog)
 					.updateTime(m.duration / 60, m.duration % 60);
 			break;
 		case START_TIME_DIALOG_ID:
 			((TimePickerDialog) dialog).updateTime(m.startHour, m.stopMinute);
 			break;
 		case STOP_TIME_DIALOG_ID:
 			((TimePickerDialog) dialog).updateTime(m.stopHour, m.stopMinute);
 			break;
 		case START_DATE_DIALOG_ID:
 			((DatePickerDialog) dialog).updateDate(m.year, m.month, m.startDay);
 			break;
 		case STOP_DATE_DIALOG_ID:
 			((DatePickerDialog) dialog).updateDate(m.year, m.month, m.stopDay);
 			break;
 		}
 	}
 
 	private void updateDisplay() {
 		setDuration.setText("Event length : " + (m.duration / 60) + ":"
 				+ pad(m.duration % 60));
 		setTimeStart.setText("" + pad(m.startHour) + ":" + pad(m.startMinute));
 		setTimeEnd.setText("" + pad(m.stopHour) + ":" + pad(m.stopMinute));
 		setDayStart.setText(new StringBuilder()
 				// Month is 0 based so add 1
 				.append(m.month + 1).append("-").append(m.startDay).append("-")
 				.append(m.year).append(" "));
 		setDayEnd.setText(new StringBuilder()
 				// Month is 0 based so add 1
 				.append(m.month + 1).append("-").append(m.stopDay).append("-")
 				.append(m.year).append(" "));
 
 	}
 
 	private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			m.year = year;
 			m.month = monthOfYear;
 			m.startDay = dayOfMonth;
 			updateDisplay();
 		}
 	};
 	private DatePickerDialog.OnDateSetListener mStopDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			m.year = year;
 			m.month = monthOfYear;
 			m.stopDay = dayOfMonth;
 			updateDisplay();
 		}
 	};
 
 	private TimePickerDialog.OnTimeSetListener mDurationSetListener = new TimePickerDialog.OnTimeSetListener() {
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			m.duration = hourOfDay * 60 + minute;
 			updateDisplay();
 		}
 	};
 	private TimePickerDialog.OnTimeSetListener mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			m.startHour = hourOfDay;
 			m.startMinute = minute;
 			updateDisplay();
 		}
 	};
 	private TimePickerDialog.OnTimeSetListener mStopTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			m.stopHour = hourOfDay;
 			m.stopMinute = minute;
 			updateDisplay();
 		}
 	};
 
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.addPeopleButton:
 			
 			Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
 			Account[] accounts = AccountManager.get(this).getAccounts();
 			loop : for (Account account : accounts) {
 			    if (emailPattern.matcher(account.name).matches()) {
 			        m.hostEmail = account.name;
 			        break loop;
 			    }
 			} 
 			
 		    
 			
 			TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
 			m.hostNumber = telephonyManager.getLine1Number();
 			
 			m.meetingName = meetingName.getText().toString();
 			
 			m.autoSelectBestTime = autoSelect.isChecked();
 			
 			if(((RadioButton)contactMethod.getChildAt(1)).isChecked())	
 				m.preferSMS = true;
 			else
 				m.preferSMS = false;
 			
 			Intent i = new Intent(this, InvitePeopleActivity.class);
 			i.putExtra("theMeeting", m);
 			startActivity(i);
 			break;
 		case R.id.durationButton:
 			showDialog(DURATION_DIALOG_ID, null);
 			break;
 		case R.id.startTimeButton:
 			showDialog(START_TIME_DIALOG_ID, null);
 			break;
 		case R.id.stopTimeButton:
 			showDialog(STOP_TIME_DIALOG_ID, null);
 			break;
 		case R.id.startDayButton:
 			showDialog(START_DATE_DIALOG_ID, null);
 			break;
 		case R.id.stopDayButton:
 			showDialog(STOP_DATE_DIALOG_ID, null);
 			break;
 		}
 	}
 }
