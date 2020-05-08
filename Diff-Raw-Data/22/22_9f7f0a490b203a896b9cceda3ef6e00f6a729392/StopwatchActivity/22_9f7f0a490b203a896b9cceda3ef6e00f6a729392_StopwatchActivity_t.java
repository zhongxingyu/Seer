 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.text.method.ScrollingMovementMethod;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class StopwatchActivity extends Activity implements
 		OnGlobalLayoutListener {
 	private TextView mWorkoutDescription, mStateLabel, mWorkoutName;
 	private Button mStartStop, mReset, mFinish;
 	private final long mFrequency = 100;
 	private final int TICK_WHAT = 2;
 	private long id;
 	private boolean cdRun;
 	private Time stopwatch = new Time();
 	private MediaPlayer mp;
 	private boolean active = true;
 
 	/**
 	 * Handler object that updates time display on the button
 	 */
 	private Handler mHandler = new Handler() {
 		public void handleMessage(Message m) {
 			if (!cdRun)
 			updateElapsedTime();
 			sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
 		}
 	};
 	
 	
 	/**
 	 * onCreate method that sets up the display of the page
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.stopwatch_tab);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		// count down is false
 		cdRun = false;
 
 		WorkoutModel model = new WorkoutModel(this);
 		id = getIntent().getLongExtra("ID", -1);
 		if (id < 0) {
 			startActivity(new Intent(this, CrossFitrActivity.class));
 		}
 
 		model.open();
 		WorkoutRow workout = model.getByID(id);
 		model.close();
 
 		Typeface roboto = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-Light.ttf");
 
 		mStateLabel = (TextView) findViewById(R.id.state_label);
 		mStateLabel.setTypeface(roboto);
 
 		mWorkoutDescription = (TextView) findViewById(R.id.workout_des_time);
 		mWorkoutDescription.setMovementMethod(new ScrollingMovementMethod());
 		mWorkoutDescription.setTypeface(roboto);
 		mWorkoutDescription.setText(workout.description);
 
 		mWorkoutName = (TextView) findViewById(R.id.workout_name_time);
 		mWorkoutName.setText(workout.name);
 		mWorkoutName.setTypeface(roboto);
 
 		mStartStop = (Button) findViewById(R.id.start_stop_button);
 		ViewTreeObserver vto = mStartStop.getViewTreeObserver();
 		vto.addOnGlobalLayoutListener(this);
 		mStartStop.setTypeface(roboto);
 
 		mReset = (Button) findViewById(R.id.reset_button);
 		mReset.setTypeface(roboto);
 		mReset.setEnabled(false);
 
 		mFinish = (Button) findViewById(R.id.finish_workout_button);
 		mFinish.setTypeface(roboto);
 		mFinish.setEnabled(false);
 
 		mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT),
 				mFrequency);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	/**
 	 * when time button is clicked. start/stop
 	 * @param V
 	 */
 	public void onStartStopClicked(View V) {
 		
 		if(!stopwatch.isRunning()){
 			// disable tab buttons
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(0).setEnabled(false);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget().getChildTabViewAt(2).setEnabled(false);
 			 
 			// play countdown sound
 			playSound(R.raw.countdown_3_0);
 			new CountDownTimer(3000, 100) {
 				
 				// while time is ticking
 				public void onTick(long millisUntilFinished) {
 					mStartStop.setText("" + ((millisUntilFinished / 1000)+1));
 					mStartStop.setEnabled(false);
 					mStateLabel.setText("Press To Stop");
 					mStateLabel.setTextColor(-65536);
 					mReset.setEnabled(false);
 					mFinish.setEnabled(false);
 					cdRun = true;
 				}
 
 				// when count down is done
 				public void onFinish() {
 					playSound(R.raw.bell_ring);
 					//mStartStop.setText("Go!");
 					stopwatch.start();
 					cdRun = false;
 					mStartStop.setEnabled(true);
 				}
 			}.start();
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
 
 	/**
 	 * when reset button is clicked
 	 * @param v
 	 */
 	public void onResetClicked(View v) {
 		stopwatch.reset();
 		mFinish.setEnabled(false);
 		mReset.setEnabled(false);
 	}
 
 	/**
 	 * when finish workout button is clicked
 	 * @param v
 	 */
 	public void onFinishClicked(View v) {
 		Intent result = new Intent();
 		result.putExtra("time", stopwatch.getElapsedTime());
 		getParent().setResult(RESULT_OK, result);
 		finish();
 	}
 	
 	/** 
 	 * if count down is not running update time
 	 */
 	public void updateElapsedTime() {
 		//if (!cdRun)
 			mStartStop.setText(getFormattedElapsedTime());
 	}
 
 	/**
 	 * formatting time display
 	 * @param now  takes in lond time value to display
 	 * @return String  with time formatted numbers
 	 */
 	public static String formatElapsedTime(long now) {
 		long hours = 0, minutes = 0, seconds = 0, tenths = 0;
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
 			sb.append(hours).append(":").append(formatDigits(minutes))
 					.append(":").append(formatDigits(seconds)).append(".")
 					.append(tenths);
 		} else {
 			sb.append(formatDigits(minutes)).append(":")
 					.append(formatDigits(seconds)).append(".").append(tenths);
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * put 0 in front of the single digit numbers
 	 * @param num
 	 * @return
 	 */
 	private static String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 
 	public String getFormattedElapsedTime() {
 		return formatElapsedTime(stopwatch.getElapsedTime());
 	}
 
 
 	/**
 	 * Resizes mStartStop dynamically for smaller screen sizes
 	 */
 	public void onGlobalLayout() {
 		if (1 < mStartStop.getLineCount()) {
 			mStartStop.setTextSize(TypedValue.COMPLEX_UNIT_PX,
 					mStartStop.getTextSize() - 2);
 		}
 	}
 	
 	/**
 	 * method to play sound file
 	 * @param r  raw int value of the file that wants to be played
 	 */
 	private void playSound(int r) {
 		//Release any resources from previous MediaPlayer
 		 if (mp != null) {
 		 mp.release();
 		 }
 		
 		 if(active){
 			 // Create a new MediaPlayer to play this sound
 			 mp = MediaPlayer.create(this, r);
 			 mp.start();
 		 }
 	}
 	
 	public void onBackPressed() {
         super.onBackPressed();
        if (mp != null) {
			 mp.release();
		 }
         active = false;
 	 }
 }
