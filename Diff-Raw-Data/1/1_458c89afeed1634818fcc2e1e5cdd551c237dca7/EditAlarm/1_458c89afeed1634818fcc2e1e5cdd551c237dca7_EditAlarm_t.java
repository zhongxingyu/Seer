 package com.fernferret.android.fortywinks;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 /**
  * The activity that allows adding/editing of alarms
  * @author Jimmy Theis
  *
  */
 public class EditAlarm extends Activity {
     
     private PreferenceView mThreshold;
     private PreferenceView mFollowups;
     private PreferenceView mInterval;
     
     private EditText mThresholdInput;
     private EditText mFollowupsInput;
     private EditText mIntervalInput;
     
     private LinearLayout mPreferenceList;
     
     private Button mSaveButton;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_alarm);
         
         mPreferenceList = (LinearLayout)findViewById(R.id.edit_alarm_preferences_list);
         
         mSaveButton = (Button)findViewById(R.id.edit_alarm_save);
         
         mThreshold = new PreferenceView(this);
         mFollowups= new PreferenceView(this);
         mInterval = new PreferenceView(this);
         
         mPreferenceList.addView(mThreshold);
         mPreferenceList.addView(mFollowups);
         mPreferenceList.addView(mInterval);
         
         mThreshold.setLeftText("Threshold");
         mThreshold.setRightText("30");
         
         mThresholdInput = new EditText(this);
         mFollowupsInput = new EditText(this);
         mIntervalInput = new EditText(this);
         
         mFollowups.setLeftText("Follow Ups");
         mFollowups.setRightText("4");
         
         mInterval.setLeftText("Interval");
         mInterval.setRightText("5-8");
         
         mThreshold.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO: Fixme
                 AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
                 builder.setView(mThresholdInput);
                 Dialog dialog = builder.create();
                 dialog.show();
             }
         });
         
         mFollowups.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO: Fixme
                 AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
                 builder.setView(mFollowupsInput);
                 Dialog dialog = builder.create();
                 dialog.show();
             }
         });
         
         mInterval.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO: Fixme
                 AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
                 builder.setView(mIntervalInput);
                 Dialog dialog = builder.create();
                 dialog.show();
             }
         });
         
         mSaveButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO Save the alarm and finish this activity
             }
         });
     }
     
     private Alarm getAlarm() {
         Alarm result = new Alarm();
         // TODO: Populate with values from views
         return result;
     }
 }
