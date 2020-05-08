 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
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
 
 public class TimerActivity extends Activity implements OnGlobalLayoutListener {
 	static final int NUMBER_DIALOG_ID = 0; // Dialog variable
 	private int mHour, mMin, mSec;
 	private long startTime, id;
 	private final long mFrequency = 100; // milliseconds
 	private final int TICK_WHAT = 2;
 	private boolean cdRun;
 	NumberPicker mNumberPicker;
 	Button mSetTimer, mFinish, mStartStop;
 	TextView mWorkoutDescription, mStateLabel, mWorkoutName;
 	Time timer = new Time();
 	private MediaPlayer mp;
 
 	private Handler mHandler = new Handler() {
 		public void handleMessage(Message m) {
 			if(!cdRun)
 			updateElapsedTime();
 			sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
 			
 		}
 	};
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.timer_tab);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		cdRun = false;
 		// create model object
 		WorkoutModel model = new WorkoutModel(this);
 		// get the id passed from previous activity (workout lists)
 		id = getIntent().getLongExtra("ID", -1);
 		// if ID is invalid, go back to home screen
 		if (id < 0) {
 			getParent().setResult(RESULT_CANCELED);
 			finish();
 		}
 
 		// open model to put data into database
 		model.open();
 		WorkoutRow workout = model.getByID(id);
 		model.close();
 
 		Typeface roboto = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-Light.ttf");
 
 		mStateLabel = (TextView) findViewById(R.id.state_label);
 		mStateLabel.setTypeface(roboto);
 		mStateLabel.setText("");
 
 		mWorkoutDescription = (TextView) findViewById(R.id.workout_des_time);
		//mWorkoutDescription.setMovementMethod(new ScrollingMovementMethod());
 		mWorkoutDescription.setTypeface(roboto);
 		String workoutDesc = workout.description;
 		workoutDesc.replace(",", "\n");
 		mWorkoutDescription.setText(workoutDesc);
 
 		mWorkoutName = (TextView) findViewById(R.id.workout_name_time);
 		mWorkoutName.setText(workout.name);
 		mWorkoutName.setTypeface(roboto);
 
 		mStartStop = (Button) findViewById(R.id.start_stop_button);
 		ViewTreeObserver vto = mStartStop.getViewTreeObserver();
 		vto.addOnGlobalLayoutListener(this);
 		mStartStop.setTypeface(roboto);
 		//mStartStop.setText("0:00:00.0");
 		mStartStop.setEnabled(false);
 
 		mSetTimer = (Button) findViewById(R.id.SetTimer);
 		mSetTimer.setTypeface(roboto);
 
 		mFinish = (Button) findViewById(R.id.finish_workout_button);
 		mFinish.setTypeface(roboto);
 		mFinish.setEnabled(false);
 
 		mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT),
 				mFrequency);
 
 		// Opens Dialog on click
 		mSetTimer.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(NUMBER_DIALOG_ID);
 			}
 		});
 	}
 
 	private NumberPickerDialog.OnNumberSetListener mNumberSetListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int selectedHour, int selectedMin,
 				int selectedSec) {
 			if (selectedHour == 0 && selectedMin == 0 && selectedSec == 0) {
 				clearInput();
 			} else {
 				clearAllTimer();
 				mHour = selectedHour;
 				mMin = selectedMin;
 				mSec = selectedSec;
 				mStartStop.setEnabled(true);
 				setDisplayBackgroundColor(0);
 			}
 		}
 
 	};
 
 	/**
 	 * Clears the timer; sets everything to 0
 	 */
 	public void clearAllTimer() {
 		mHour = 0;
 		mMin = 0;
 		mSec = 0;
 		startTime = 0;
 		mStateLabel.setText("Press To Start");
 		mStateLabel.setTextColor(Color.GREEN);
 
 		timer.reset();
 		updateElapsedTime();
 	}
 
 	private void clearInput() {
 		mHour = 0;
 		mMin = 0;
 		mSec = 0;
 	}
 
 	private void updateElapsedTime() {
 		//if(!cdRun)
 		mStartStop.setText(getFormattedElapsedTime());
 	}
 
 	/**
 	 * Gets the start time for the timer in milliseconds
 	 * 
 	 * @return start time in milliseconds
 	 */
 	public long getStartTime() {
 		startTime = (mHour * 3600000) + (mMin * 60000) + (mSec * 1000);
 		return startTime;
 	}
 
 	private String formatElapsedTime(long start) {
 		long hours = 0;
 		long minutes = 0;
 		long seconds = 0;
 		long tenths = 0;
 		StringBuilder sb = new StringBuilder();
 		if (!checkForEnd(start)) {
 			if (start < 1000) {
 				tenths = start / 100;
 			}
 
 			else if (start < 60000) {
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 
 			else if (start < 3600000) {
 				minutes = start / 60000;
 				start -= minutes * 60000;
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 
 			else {
 				hours = start / 3600000;
 				start -= hours * 3600000;
 				minutes = start / 60000;
 				start -= minutes * 60000;
 				seconds = start / 1000;
 				start -= seconds * 1000;
 				tenths = start / 100;
 			}
 		}
 
 		sb.append(hours).append(":").append(formatDigits(minutes)).append(":")
 				.append(formatDigits(seconds)).append(".").append(tenths);
 
 		return sb.toString();
 	}
 
 	private boolean checkForEnd(long time) {
 		if (time < 0) {
 			playSound(R.raw.boxing_bellx3);
 			clearInput();
 			timer.reset();
 			mStateLabel.setText("");
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(1).setEnabled(true);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(2).setEnabled(true);
 			setDisplayBackgroundColor(2);
 			mSetTimer.setEnabled(true);
 			mStartStop.setEnabled(false);
 			mFinish.setEnabled(true);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the current elapsed time in 0:00:00.00 format
 	 * 
 	 * @return
 	 */
 	public String getFormattedElapsedTime() {
 		return formatElapsedTime(getStartTime() - getElapsedTime());
 	}
 
 	private long getElapsedTime() {
 		return timer.getElapsedTime();
 	}
 
 	public void onStartStopClicked(View V) {
 		if (!timer.isRunning()) {
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(1).setEnabled(false);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(2).setEnabled(false);
 			
 			playSound(R.raw.countdown_3_0);
 			 
 			new CountDownTimer(3000, 100) {
 
 				public void onTick(long millisUntilFinished) {
 					mStartStop.setText("" + (millisUntilFinished / 1000 + 1));
 					mStartStop.setEnabled(false);
 					setDisplayBackgroundColor(2);
 					mStateLabel.setText("");
 					mSetTimer.setEnabled(false);
 					mFinish.setEnabled(false);
 					cdRun = true;
 				}
 
 				public void onFinish() {
 					playSound(R.raw.bell_ring);
 					//mStartStop.setText("Go!");
 					setDisplayBackgroundColor(1);
 					mStateLabel.setText("Press To Stop");
 					mStateLabel.setTextColor(Color.RED);
 					timer.start();
 					cdRun = false;
 					mStartStop.setEnabled(true);
 				}
 			}.start();
 			
 		} else {
 			timer.stop();
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(1).setEnabled(true);
 			((TimeTabWidget) getParent()).getTabHost().getTabWidget()
 					.getChildTabViewAt(2).setEnabled(true);
 			mStateLabel.setText("Press To Start");
 			mStateLabel.setTextColor(Color.GREEN);
 			setDisplayBackgroundColor(0);
 			mSetTimer.setEnabled(true);
 			mFinish.setEnabled(true);
 		}
 	}
 
 	public void onFinishedClicked(View v) {
 		Intent result = new Intent();
 		result.putExtra("time", getStartTime());
 		getParent().setResult(RESULT_OK, result);
 		finish();
 	}
 
 	private String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		return new NumberPickerDialog(this, mNumberSetListener, 2, 0);
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
 	 * method to change background color
 	 * @param color
 	 */
 	private void setDisplayBackgroundColor(int color){
 		if(color == 0){
 			mStartStop.setBackgroundResource(R.drawable.tabata_display_go);
 		}
 		else if(color == 1){
 			mStartStop.setBackgroundResource(R.drawable.tabata_display_rest);
 		}
 		else if(color == 2){
 			mStartStop.setBackgroundResource(R.drawable.background_main);
 		}
 			
 	}
 	
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
