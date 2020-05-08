 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TabataActivity extends Activity {
 	private static final int TOTAL_TIME = 30000 * 8;
 	// View elements in stopwatch.xml
 	private TextView mWorkoutDescription, mStateLabel, mWorkoutName;
 	private Button mStartStop, mReset, mFinish;
 	private Time tabata = new Time();
 	private boolean newStart, cdRun, goStop;
 	private long id;
 	private MediaPlayer mp;
 
 	// Timer to update the elapsedTime display
 	private final long mFrequency = 100; // milliseconds
 	private final int TICK_WHAT = 2;
 	private Handler mHandler = new Handler() {
 		public void handleMessage(Message m) {
 			if(!cdRun)
 			updateElapsedTime();
 			sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.tabata_tab);
 		cdRun = false;
 	  	//open model to put data into database
 	  	//get the id passed from previous activity (workout lists)
 	  	id = getIntent().getLongExtra("ID", -1);
 	  	//if ID is invalid, go back to home screen
 	  	if(id < 0)
 	  	{
 	  		getParent().setResult(RESULT_CANCELED);
 	  		finish();
 	  	}
 	  	
 		newStart = true;
 		
 		 //create model object
 	    WorkoutModel model = new WorkoutModel(this);
 
 	  	model.open();
 	  	WorkoutRow workout = model.getByID(id);
 		model.close();
 		
 		Typeface roboto = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Light.ttf");
 	  	
 	  	mStateLabel = (TextView)findViewById(R.id.state_label);
 		mStateLabel.setTypeface(roboto);
 		mStateLabel.setText("Press To Start");
 		mStateLabel.setTextColor(-16711936);
 		
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
 		mReset.setEnabled(false);
         
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
 		if(!tabata.isRunning()){
 			newStart = false;
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(false);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).setEnabled(false);
 			
 			playSound(R.raw.countdown_3_0);
 			 
 			new CountDownTimer(3000, 100) {
 
 				public void onTick(long millisUntilFinished) {
 					mStartStop.setText("" + (millisUntilFinished / 1000 + 1));
 					mStartStop.setEnabled(false);
 					mStateLabel.setText("");
 					mReset.setEnabled(false);
 					mFinish.setEnabled(false);
					cdRun = true;
 				}
 
 				public void onFinish() {
 					goStop = true;
 					playSound(R.raw.bell_ring);
 					//mStartStop.setText("Go!");
 					tabata.start();
 					mStateLabel.setText("Press To Stop");
 					mStateLabel.setTextColor(-65536);
 					cdRun = false;
 					mStartStop.setEnabled(true);
 				}
 			}.start();
 		}
 		else{
 			tabata.stop();
 			newStart = false;
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(true);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).setEnabled(true);
 			mStateLabel.setText("Press To Start");
 			mStateLabel.setTextColor(-16711936);
 			mReset.setEnabled(true);
 			mFinish.setEnabled(true);
 		}
 	}
 
 	public void onResetClicked(View v) {
 		newStart = true;
 		tabata.reset();
 		mReset.setEnabled(false);
 		mFinish.setEnabled(false);
 	}
 	
 	public void onFinishClicked(View v) {
 		Intent result = new Intent();
 		result.putExtra("time", getFormattedElapsedTime());
 		getParent().setResult(RESULT_OK, result);
 		finish();
 	}
 
 	/**
 	 * method to do when 8 sets are done
 	 */
 	private void endTabata() {
 		newStart = true;
 		playSound(R.raw.boxing_bellx3);
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(true);
 		((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).setEnabled(true);
 		tabata.reset();
 		mFinish.setEnabled(true);
 	}
 
 	public void updateElapsedTime() {
 		//if(!cdRun)
 		mStartStop.setText(getFormattedElapsedTime());
 	}
 
 	private String formatElapsedTime(long now, int set) {
 		long seconds = 0, tenths = 0;
 		StringBuilder sb = new StringBuilder();
 
 		if(newStart){
 			now = 20000;			
 		}
 		
 		if (now < 1000) {
 			tenths = now / 100;
 		} else if (now < 60000) {
 			seconds = now / 1000;
 			now -= seconds * 1000;
 			tenths = now / 100;
 		}
 
 
 		sb.append("SET : ").append(set).append("\n").append(formatDigits(seconds)).append(".").append(tenths);
 
 		return sb.toString();
 	}
 
 	private String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 
 	public String getFormattedElapsedTime() {
 		long time = tabata.getElapsedTime();
 		
 		int set = 1 + ((int)time / 30000);
 		long diff = TOTAL_TIME - time;
 		long remain = diff % 30000;
 		
 		int green = Color.GREEN;
 		int red = Color.RED;
 		
 		//reset at end of set 8 workout. no last 10 sec break
 		if(diff <= 10000){
 			set = 1;
 			this.endTabata();
 		}
 		
 		// if logic to display sets and time for tabata
 		if(remain > 10000 ){
 			if(!goStop){
				playSound(R.raw.bell_ring);
 				goStop = true;
 			}
 			this.setDisplayBackgroundColor(green);
 			return formatElapsedTime(20000 - (time % 30000), set);
 		}else if(remain == 10000){
 			return formatElapsedTime(0, set);
 		}else{
 			if(goStop){
				if(diff > 20000){
 					playSound(R.raw.air_horn);
 				}
 				goStop = false;
 			}
 			this.setDisplayBackgroundColor(red);
 			return formatElapsedTime(30000 - (time % 30000), set);
 		}
 	}
 	
 
 	/**
 	 * method to change background color
 	 * @param color
 	 */
 	public void setDisplayBackgroundColor(int color){
 		if(color == Color.GREEN){
 			mStartStop.setBackgroundResource(R.drawable.tabata_display_go);
 		}
 		else
 			mStartStop.setBackgroundResource(R.drawable.tabata_display_rest);
 	}
 	
 	/**
 	 * method to play sound file 
 	 * @param r
 	 */
 	private void playSound(int r) {
 		//Release any resources from previous MediaPlayer
 		 if (mp != null) {
 		 mp.release();
 		 }
 		
 		 // Create a new MediaPlayer to play this sound
 		 mp = MediaPlayer.create(this, r);
 		 mp.start();
 	}
 }
