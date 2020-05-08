 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class StopwatchActivity extends Activity {
 	private TextView mWorkoutDescription, mStateLabel, mWorkoutName;
 	private Button mStartStop, mReset, mFinish;
     private final long mFrequency = 100;
     private final int TICK_WHAT = 2;
 	private long id;
 	private Time stopwatch = new Time();
    
 	private Handler mHandler = new Handler() {
         public void handleMessage(Message m) {
         	updateElapsedTime();
         	sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.stopwatch_tab);
 
 	    WorkoutModel model = new WorkoutModel(this);
 	  	id = getIntent().getLongExtra("ID", -1);
 	  	if(id < 0)
 	  	{
 	  		startActivity(new Intent(this, CrossFitrActivity.class));
 	  	}
 
 	  	model.open();
 	  	WorkoutRow workout = model.getByID(id);
 	  	model.close();
 	  	
 	  	Typeface roboto = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Light.ttf");
 	  	
 	  	mStateLabel = (TextView)findViewById(R.id.state_label);
 		mStateLabel.setTypeface(roboto);
 		
 		mWorkoutDescription = (TextView)findViewById(R.id.workout_des_time);
 		mWorkoutDescription.setMovementMethod(new ScrollingMovementMethod());
 		mWorkoutDescription.setTypeface(roboto);
 		mWorkoutDescription.setText(workout.description);
 		
 		mWorkoutName = (TextView)findViewById(R.id.workout_name_time);
 		mWorkoutName.setText(workout.name);
 		mWorkoutName.setTypeface(roboto);
 		
 		mStartStop = (Button)findViewById(R.id.start_stop_button);
 		mStartStop.setTypeface(roboto);
 		
 		mReset = (Button)findViewById(R.id.reset_button);
 		mReset.setTypeface(roboto);
         
         mFinish = (Button)findViewById(R.id.finish_workout_button);
         mFinish.setTypeface(roboto);
         mFinish.setEnabled(false);
         
         mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), mFrequency);
     }
     
    @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
     public void onStartStopClicked(View V) {
 		if(!stopwatch.isRunning()){
 			stopwatch.start();
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(false);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(2).setEnabled(false);
 			mStateLabel.setText("Press To Stop");
 			mStateLabel.setTextColor(-65536);
 			mFinish.setEnabled(false);
 			mReset.setEnabled(false);
 		}
 		else{
 			stopwatch.stop();
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(true);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(2).setEnabled(true);
 			mStateLabel.setText("Press To Start");
 			mStateLabel.setTextColor(-16711936);
 			mFinish.setEnabled(true);
 			mReset.setEnabled(true);
 			mFinish.setEnabled(true);
 		}
 	}
     
     public void onResetClicked(View v) {
     	stopwatch.reset();
     	mFinish.setEnabled(false);
     }
     
     public void onFinishClicked(View v) {
 		Intent result = new Intent();
 		result.putExtra("time", getElapsedTime());
 		getParent().setResult(RESULT_OK, result);
 		finish();
 	}
     
     public void updateElapsedTime() {
    		mStartStop.setText(getFormattedElapsedTime());
     }
     
 	public static String formatElapsedTime(long now) {
 		long hours=0, minutes=0, seconds=0, tenths=0;
 		StringBuilder sb = new StringBuilder();
 
 		if (now < 1000) {
 			tenths = now / 100;
 		} else if (now < 60000) {
 			seconds = now / 1000;
 			now -= seconds * 1000;
 			tenths = now / 100;
 		} else if (now < 3600000) {
 			hours = now / 3600000;
 			now -= hours * 3600000;
 			minutes = now / 60000;
 			now -= minutes * 60000;
 			seconds = now / 1000;
 			now -= seconds * 1000;
 			tenths = now / 100;
 		}
 
 		if (hours > 0) {
 			sb.append(hours).append(":")
 				.append(formatDigits(minutes)).append(":")
 				.append(formatDigits(seconds)).append(".")
 				.append(tenths);
 		} else {
 			sb.append(formatDigits(minutes)).append(":")
 			.append(formatDigits(seconds)).append(".")
 			.append(tenths);
 		}
 
 		return sb.toString();
 	}
 
 	private static String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 
 	public String getFormattedElapsedTime() {
 		return formatElapsedTime(getElapsedTime());
 	}
 
 	public long getElapsedTime() {
 		return stopwatch.getElapsedTime();
 
 	}
 }
