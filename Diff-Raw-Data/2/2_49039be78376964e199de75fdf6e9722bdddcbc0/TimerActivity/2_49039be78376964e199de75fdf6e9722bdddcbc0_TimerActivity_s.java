 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class TimerActivity extends Activity 
 {	
     static final int NUMBER_DIALOG_ID = 0; // Dialog variable
     TextView mTimeDisplay, mElapsedTime, mWorkoutDescription;
     private int mHour, mMin, mSec;
     private long startTime;
     NumberPicker mNumberPicker;
     Button mStart, mStop, mReset, mSetTimer;
     TabHost tabHost;
     boolean firstTimeCountdown = true;
     boolean firstTimeAlarm = true;
     long id;
     Time timer = new Time();
     AudibleTime sound;
     
     
 	// Timer to update the elapsedTime display
     private final long mFrequency = 100;    // milliseconds
     private final int TICK_WHAT = 2; 
 	private Handler mHandler = new Handler() {
         public void handleMessage(Message m) {
         	updateElapsedTime();
         	sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
         }
     };
     
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.timer_tab);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 	    //create model object
 	    WorkoutModel model = new WorkoutModel(this);
 	  	//get the id passed from previous activity (workout lists)
 	  	id = getIntent().getLongExtra("ID", -1);
 	  	//if ID is invalid, go back to home screen
 	  	if(id < 0)
 	  	{
 	  		getParent().setResult(RESULT_CANCELED);
 	  		finish();
 	  	}
 
 	  	//open model to put data into database
 	  	model.open();
 	  	WorkoutRow row = model.getByID(id);
 		model.close();
 
 		mElapsedTime = (TextView)findViewById(R.id.ElapsedTime);
         mWorkoutDescription = (TextView)findViewById(R.id.workout_des_time);
         mStart = (Button)findViewById(R.id.StartButton);
         mStop = (Button)findViewById(R.id.StopButton);
         mSetTimer = (Button)findViewById(R.id.SetTimer);
         
         mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), mFrequency);
 
         mWorkoutDescription.setText(row.description);
 
     
 		// Opens Dialog on click
 		mSetTimer.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v) 
 			{
 				showDialog(NUMBER_DIALOG_ID);
 	        }
 	    });
 	}
 
 	private NumberPickerDialog.OnNumberSetListener mNumberSetListener =
 			new NumberPickerDialog.OnNumberSetListener() {
 				public void onNumberSet(int selectedHour, int selectedMin, int selectedSec) {
 					clearAllTimer();
 					mHour = selectedHour;
 					mMin = selectedMin;
 					mSec = selectedSec;
 					showStartButton();
 				}
 
 		    };
 
     /**
      * Clears the timer; sets everything to 0
      */
 	public void clearAllTimer(){
 		mHour = 0;
 		mMin = 0;
 		mSec = 0;
 		startTime = 0;
 		firstTimeCountdown = true;
 		firstTimeAlarm = true;
 		timer.reset();
 		updateElapsedTime();
 	}
 
 	private void clearInput(){
 		mHour = 0;
 		mMin = 0;
 		mSec = 0;
 	}
 
 	private void updateElapsedTime() {
 		mElapsedTime.setText(getFormattedElapsedTime());
 	}
 
 	/**
 	 * Gets the start time for the timer in milliseconds
 	 * @return start time in milliseconds
 	 */
 	public long getStartTime(){
 		startTime = (mHour * 3600000) + (mMin * 60000) + (mSec * 1000);
  	    return startTime;
     }
 
 	private String formatElapsedTime(long start) {
 		long hours = 0;
 		long minutes = 0;
 		long seconds = 0;
 		long tenths = 0;
 		StringBuilder sb = new StringBuilder();
 		if(!checkForEnd(start)){	
 			if (start < 1000) {
 				tenths = start / 100;
 			} 
 
 			else if (start < 60000) 
 			{
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 
 			else if (start < 3600000) 
 			{
 				minutes = start / 60000;
 				start -= minutes * 60000;
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 
 			else
 			{
 				hours = start / 3600000;
 				start -= hours * 3600000;
 				minutes = start / 60000;
 				start -= minutes * 60000;
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 		}
 
 		sb.append(hours).append(":")
 			.append(formatDigits(minutes)).append(":")
 			.append(formatDigits(seconds)).append(".")
 			.append(tenths);
 
 		return sb.toString();		
 	}
 
 
 	private boolean checkForEnd(long time) {
 		if(time < 0){	
 			mSetTimer.setVisibility(View.VISIBLE);
 			mStop.setVisibility(View.GONE);
 			clearInput();
 			timer.reset();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the current elapsed time in 0:00:00.00 format
 	 * @return
 	 */
 	public String getFormattedElapsedTime() {
 		return formatElapsedTime(getStartTime() - getElapsedTime());
 	}
 
 	private long getElapsedTime() {
 		return timer.getElapsedTime();
 	}
 
 	public void onStartClicked(View V) {
 		timer.start();
 		showStopButton();
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).setEnabled(false);
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(2).setEnabled(false);
 		mSetTimer.setVisibility(View.GONE);
 	}
 
 	public void onStopClicked(View v) {
 		timer.stop();
 		showStartButton();
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).setEnabled(true);
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(2).setEnabled(true);
 		mSetTimer.setVisibility(View.VISIBLE);
 	}
 
 	public void onFinishedClicked(View v) {
 		Intent result = new Intent();
 		result.putExtra("time", getFormattedElapsedTime());
 		getParent().setResult(RESULT_OK, result);
 		finish();
 	}
 
 	private void showStopButton() {
 		mStart.setVisibility(View.GONE);
 		mStop.setVisibility(View.VISIBLE);
 	}
 
 	private void showStartButton() {
 		mStart.setVisibility(View.VISIBLE);
 		mStop.setVisibility(View.GONE);
 	}
 
 
 	private String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 
 	// Override creating the Dialog
 	@Override
 	protected Dialog onCreateDialog(int id) 
 	{
 		return new NumberPickerDialog(this, mNumberSetListener, 2, 0);
 	}
 }
