 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.conica.DailyCapture;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TimePicker;
 
 public class RecordEditActivity extends PreferenceActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{
 	private String TAG = "capture";
 	
 	private Context mContext;
 
 	private CheckBoxPreference mAlarmPref;
 	private EditTextPreference mNamePref;
 	private EditTextPreference mDespPref;
 	private ListPreference mPeriodPref;
 	private Preference mTimePref;
 	private Preference mCountPref;
 	private Preference mCreatedatePref;
 	
 	private MainApp mMainApp;
 	
 	private RecordsDBAdapter mRecordsDBAdapter;
 	
     private long mRowId = -1;
 	private String mName = "";
 	private String mDesp = "";
 	private boolean mAlarmOn = false;
 	private int mPeriodType = -1;
 	private SimpleDate mDate = null;
 	private int mPeriod = -1;
 	private SimpleDate mTime = null;
 	private int mCount;
 	private SimpleDate mCreateDate = null;
 	
 	private boolean mEditMode = false;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.addrecord_preference);
         
         mContext = this;      
 		
         mRecordsDBAdapter = MainApp.gRecordsDBAdapter; 
         initalPrefs();
         setCtrlBar();
                 
         Bundle extras = getIntent().getExtras();
 		mRowId = extras != null ? extras.getLong(RecordsDBAdapter.KEY_RECORD_ROWID): -1;
 		
 		if(mRowId != -1){
 			mEditMode = true;
 			populateFields();
 		}
     }
     
     private void initalPrefs(){
         mAlarmPref = (CheckBoxPreference)findPreference("alarmPref");
         mNamePref = (EditTextPreference)findPreference("namePref");
         mDespPref = (EditTextPreference)findPreference("despPref");
         mPeriodPref = (ListPreference)findPreference("periodPref");
         mTimePref = findPreference("timePref");
         mCountPref = findPreference("countPref");
         mCreatedatePref = findPreference("creatDatePref");
     
         mNamePref.setText("");
         mDespPref.setText("");
         mPeriodPref.setValueIndex(0);
         mAlarmPref.setChecked(mAlarmOn);
         mCount = 0;
         mCountPref.setSummary(Integer.toString(mCount));
         Calendar c = Calendar.getInstance();
 		mCreateDate = new SimpleDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.HOUR), c.get(Calendar.MINUTE));
         mCreatedatePref.setSummary(mCreateDate.getDateDisplayString() + " " + mCreateDate.getTimeDisplayString());
         
         // add entries for periodPref
         mMainApp = (MainApp)getApplicationContext();      
         ((ListPreference)mPeriodPref).setEntries(mMainApp.gAlarmPeriod);
         ((ListPreference)mPeriodPref).setEntryValues(mMainApp.gAlarmPeriod);
         
         // handle for name setting
         mNamePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				mName = newValue.toString();
 				mNamePref.setText(mName);
 				mNamePref.setSummary(mName);
 				return false;
 			}
 		});
         
         // handle for description setting
         mDespPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				mDesp = newValue.toString();
 				mDespPref.setText(mDesp);
 				mDespPref.setSummary(mDesp);
 				return false;
 			}
 		});
         
         // handle for date setting; especially for "Specific Period" and "Specific Date"
         mPeriodPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				String pt = newValue.toString();
 				mPeriodPref.setSummary(pt);
 				for(int i = 0; i<mMainApp.gAlarmPeriod.length; i++ ){
 					if(pt.equals(mMainApp.gAlarmPeriod[i])){
 						mPeriodType = i;
 					}
 				}
 				
 				if(mPeriodType == 9){
 					if(mDate == null){
 						Calendar c = Calendar.getInstance();
 						new DatePickerDialog(mContext, (RecordEditActivity)mContext, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
 					}else{
 						new DatePickerDialog(mContext, (RecordEditActivity)mContext, mDate.year, mDate.month, mDate.day).show();
 					}
 				}else if(mPeriodType == 8){
 					final Dialog periodDialog = new Dialog(mContext);
 					periodDialog.setContentView(R.layout.alarmperiod);
 					periodDialog.setTitle("Please input a number for setting period");
 					
 					Button ok = (Button)periodDialog.findViewById(R.id.period_ok_bt);
 					Button cancel = (Button)periodDialog.findViewById(R.id.period_cancel_bt);			
 					final EditText periodet = (EditText)periodDialog.findViewById(R.id.period_no);
 					//if(period > 0)
 						//periodet.setText(Integer.toString(period));
 					
 					ok.setOnClickListener(new OnClickListener() {					
 						public void onClick(View v) {	
 							String p = periodet.getText().toString();
 							if(isNumber(p) == true){
 								mPeriod = Integer.parseInt(p);
 								mPeriodPref.setSummary("Specific Period: " + mPeriod);
 							}else{
 								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 								builder.setMessage("Please input a number")
 								       .setCancelable(false)
 								       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 								           public void onClick(DialogInterface dialog, int id) {
 								                
 								           }
 								       });
 								AlertDialog alert = builder.create();
 								alert.show();
 							}	
 							periodDialog.cancel();
 						}
 					});
 					
 					cancel.setOnClickListener(new OnClickListener() {					
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							periodDialog.cancel();
 						}
 					}); 
 
 					
 					periodDialog.show();
 				}
 				return false;
 			}
 		});
         
     }
     
     private void setCtrlBar(){
         getListView().setItemsCanFocus(true);
 
         // Grab the content view so we can modify it.
         FrameLayout content = (FrameLayout) getWindow().getDecorView()
                 .findViewById(android.R.id.content);
 
         // Get the main ListView and remove it from the content view.
         ListView lv = getListView();
         content.removeView(lv);
 
         // Create the new LinearLayout that will become the content view and
         // make it vertical.
         LinearLayout ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
 
         // Have the ListView expand to fill the screen minus the save/cancel
         // buttons.
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                 LayoutParams.MATCH_PARENT,
                 LayoutParams.WRAP_CONTENT);
         lp.weight = 1;
         ll.addView(lv, lp);
 
         // Inflate the buttons onto the LinearLayout.
         View v = LayoutInflater.from(this).inflate(
                 R.layout.save_cancel_alarm, ll);
 
         // Attach actions to each button.
         Button savebt = (Button) v.findViewById(R.id.alarm_save);
         savebt.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     // Enable the alarm when clicking "Done"
                 	createNewRecord();
                 	setAlarm();
                     finish();
                 }
         });
         savebt = (Button) v.findViewById(R.id.alarm_cancel);
         savebt.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     finish();
                 }
         });
 
         // Replace the old content view with our new one.
         setContentView(ll);    
     }
 
     // handle for time setting
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             Preference preference) {
         if (preference == mTimePref) { 
         	if(mTime == null)
         		new TimePickerDialog(this, this, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE),
                     false).show();
         	else
             	new TimePickerDialog(this, this, mTime.day, mTime.minute,
                         false).show();	
         }else if (preference == mAlarmPref) {  	
         	mAlarmOn = ((CheckBoxPreference)mAlarmPref).isChecked();
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		mTime = new SimpleDate(hourOfDay, minute);
 		mTimePref.setSummary(mTime.getTimeDisplayString());
 	}
 
 	// handle for specific date setting
 	public void onDateSet(DatePicker view, int year, int monthOfYear,
 			int dayOfMonth) {
 		mDate = new SimpleDate(year, monthOfYear, dayOfMonth);
 		mPeriodPref.setSummary("Specific Date: " + mDate.getDateDisplayString());
 	}
 	
 	public boolean isNumber(String s){
 		return s.matches("\\d+");
 	}			
 	
 	public void createNewRecord(){
 		String errormsg = "";
 		if(mName.length()== 0){
 			errormsg = "Please naming your new record";
 		}else if(mRecordsDBAdapter.checkRecordByName(mName) == true){
 			if(!mEditMode)
 				errormsg = "Record with name " + mName + " exists. Please choose another name for your new record";
 		}else if(mAlarmOn == true && mPeriodType == -1){
 			errormsg = "You have turned alarm on. Please choose your alarm repeat period";
 		}
 		else if(mAlarmOn == true && mPeriodType == 8 && mPeriod == -1){
 			errormsg = "Please specify your alarm repeat period";
 		}
 		else if(mAlarmOn == true && mPeriodType == 9 && mDate == null){
 			errormsg = "Please specify your alarm repeat date";
 		}
 		else if(mAlarmOn == true && mTime == null){
 			errormsg = "Please specify your alarm repeat time";
 		}
 		
 		if(errormsg.length() > 0){
 			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 			builder.setMessage(errormsg)
 			       .setCancelable(false)
 			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {		                
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			alert.show();
 		}else{	
 			Calendar c = Calendar.getInstance();
 			if(mDate == null){
 	        	mDate = new SimpleDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 			}
 			if(mTime == null){
 	        	mTime = new SimpleDate(c.get(Calendar.HOUR), c.get(Calendar.MINUTE));
 			}
 				
 			if(mEditMode)
				mRecordsDBAdapter.updateRecord(mRowId,mName,mDesp,mAlarmOn,mPeriodType,mPeriod,mDate.getDateString(),mTime.getTimeString());
 			else{
 				long id = mRecordsDBAdapter.insertRecord(mName,mDesp,mAlarmOn,mPeriodType,mPeriod,mDate.getDateString(),mTime.getTimeString(), 0 ,"",mCreateDate.getFullString());
 				if(id >= 0 && mRecordsDBAdapter.IsPhotoTableExist(id) == false){
 					mRecordsDBAdapter.createNewPhotoTable(id);
 				}
 			}
 		}	
 
 	}
 	
 	private void populateFields(){
 		Cursor note = mRecordsDBAdapter.fetchRecordById(mRowId);
 		
         startManagingCursor(note);
         mName = note.getString(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_NAME));
         mDesp = note.getString(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_DESP));
         mPeriodType = note.getInt(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_PERIODTYPE));
         mPeriod = note.getInt(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_PERIOD));
 		mDate = SimpleDate.getDate(note.getString(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_DATE)));
 		mTime = SimpleDate.getTime(note.getString(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_TIME)));
         mAlarmOn = note.getInt(note.getColumnIndexOrThrow(RecordsDBAdapter.KEY_RECORD_ALARMON))!=0;
         mCount=note.getInt(note.getColumnIndex(RecordsDBAdapter.KEY_RECORD_COUNT));
         mCreateDate = SimpleDate.getFullDate(note.getString(note.getColumnIndex(RecordsDBAdapter.KEY_RECORD_CREATDATE)));
         
         mNamePref.setSummary(mName);
         mNamePref.setText(mName);
         
         mDespPref.setSummary(mDesp);
         mDespPref.setText(mDesp);
         
         mAlarmPref.setChecked(mAlarmOn);
         if(mAlarmOn){   
         	mPeriodPref.setValueIndex(mPeriodType);
 	        if(mPeriodType == 8){
 	        	if(mPeriod > 0){
 	        		mPeriodPref.setSummary(mMainApp.gAlarmPeriod[mPeriodType] + ": " + mPeriod);
 	        	}
 	        }else if(mPeriodType == 9){
 	        	if(mDate != null)
 	        	mPeriodPref.setSummary(mMainApp.gAlarmPeriod[mPeriodType] + ": " + mDate.getDateDisplayString());
 	        }else{
 	        	mPeriodPref.setSummary(mMainApp.gAlarmPeriod[mPeriodType]);
 	        }
 	        
 	        if(mTime != null){
 	        	mTimePref.setSummary( mTime.getTimeDisplayString());
 	        }
         }
         
         mCountPref.setSummary(Integer.toString(mCount));
         mCreatedatePref.setSummary(mCreateDate.getDateDisplayString() + " " + mCreateDate.getTimeDisplayString());
 	}
 	
 	public void setAlarm(){
 		Intent intent = new Intent(RecordEditActivity.this, AlarmStartRingActivity.class);
         PendingIntent sender = PendingIntent.getBroadcast(RecordEditActivity.this, 0, intent, 0);
 
         // We want the alarm to go off 30 seconds from now.
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.add(Calendar.SECOND, 5);
 
         // Schedule the alarm!
         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
         am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
 	}
 	
 	
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 }
