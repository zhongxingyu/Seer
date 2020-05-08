 package com.movisens.xs.android.cognition.pvt;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.Expose;
 import com.movisens.xs.android.cognition.CognitiveActivity;
 import com.movisens.xs.android.cognitive.library.R;
 
 /**
  * Code from: http://dub.washington.edu/projects/pvttouch This is the main
  * Activity that runs a flight of trials. Trials are returned in the result to
  * the PVT activity which called us.
  */
 public class PVT extends CognitiveActivity {
 
 	// default delays, overridden by settings settings
 	public static int ibis = 5; // number of interstimulus intervals (ISI)
 	public static int testDuration = 1000 * 30; // length of test from start to
 												// finish (ms)
 	public static int minDelay = 1000; // minimum wait time before dot appears
 										// (ms)
 	public static int maxDelay = 4000; // max wait time before dot appears (ms)
 	public static int countdownDelay = 1000; // delay between numbers in the
 												// countdown (ms)
 	public static int restartDelay = 1000; // delay after a trial is complete
 											// before next trial (ms)
 	public static int gameOverDelay = 2000; // amount of time to wait while
 											// displaying end message (ms)
 	public static int anticipateDelay = 100; // if button is pressed before this
 												// amount of time, they were too
 												// lucky (ms)
 	public static int deadlineDelay = 10000; // if they haven't pressed it yet,
 												// they're probably dead.
 
 	public static final boolean showTimer = true;
 	public static final boolean showFeedback = true;
 
 	private long tempStart = 0;
 	private List<Integer> ibiDelays;
 
 	/**
 	 * States for the test
 	 */
 	public static enum PVTState {
 		INSTRUCTIONS, COUNTDOWN3, COUNTDOWN2, COUNTDOWN1, USER_WAIT, SHOW_STIMULUS, FALSE_START, DEADLINE, SHOW_SCORE, GAME_OVER, CLEANUP_AND_FINISH,
 	}
 
 	// variables
 	private PVTState nextState;
 	private PVTRun mPVTRun;
 	private Deadline mDeadline;
 	private Handler handler;
 	private boolean deadlineReached = false;
 	private int testNum;
 
 	// layout objects
 	private RelativeLayout stimulus;
 	private TextView timeText;
 	private TextView readyText;
 	private TextView numText;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.pvt);
 
 		nextState = PVTState.INSTRUCTIONS;
 
 		handler = new Handler();
 
 		mPVTRun = new PVTRun();
 		mDeadline = new Deadline();
 
 		testNum = 1;
 
 		stimulus = (RelativeLayout) findViewById(R.id.stimulus);
 		timeText = (TextView) findViewById(R.id.time);
 		readyText = (TextView) findViewById(R.id.ready_message);
 		numText = (TextView) findViewById(R.id.test_num);
 
 		fillParameters(getIntent(), this);
 
 		float delayRange = maxDelay - minDelay;
 		float ibiDelay = delayRange / (ibis - 1);
 		ibiDelays = new LinkedList<Integer>();
 		for (int i = 0; i < ibis; i++) {
 			ibiDelays.add(Math.round(minDelay + (i * ibiDelay)));
 		}
 		Collections.shuffle(ibiDelays);
 
		handler.post(mPVTRun);
 	}
 
 	private void fillParameters(Intent intent, Context context) {
 		try {
 			fillInt(minDelay, "minDelay");
 			fillInt(maxDelay, "maxDelay");
 		} catch (Exception e) {
 			Toast.makeText(context, "Invalid Parameters: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e) {
 		super.onTouchEvent(e);
 
 		if (e.getAction() == MotionEvent.ACTION_DOWN
 				&& nextState != PVTState.GAME_OVER) {
 			mPVTRun.buttonClick();
 		}
 
 		return true;
 	}
 
 	private class PVTRun implements Runnable {
 
 		private long tempEnd = 0;
 		List<PVTResult> trials;
 		PVTResult tempTrial;
 
 		TimerRefresher mTimer;
 
 		public PVTRun() {
 			trials = new ArrayList<PVTResult>();
 			mTimer = new TimerRefresher();
 		}
 
 		// called when a click is registered
 		public void buttonClick() {
 			tempEnd = System.currentTimeMillis();
 			handler.removeCallbacks(mDeadline);
 			switch (nextState) {
 			case SHOW_STIMULUS: // false start, "too early."
 				handler.removeCallbacks(mPVTRun);
 				nextState = PVTState.FALSE_START;
 				handler.post(mPVTRun);
 				break;
 			case SHOW_SCORE: // fair start
 				handler.post(mPVTRun);
 				break;
 			case INSTRUCTIONS:
 				nextState = PVTState.COUNTDOWN3;
 				handler.post(mPVTRun);
 				break;
 			default:
 				break;
 			}
 		}
 
 		// main method (state machine) is run when handler calls it
 		public void run() {
 			// Log.d(TAG, "state: " + nextState.name());
 			synchronized (this) {
 				switch (nextState) {
 				default:
 				case INSTRUCTIONS:
 					readyText.setVisibility(View.VISIBLE);
 					readyText.setText(getText(R.string.pvt_instructions));
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					break;
 				case COUNTDOWN3:
 					readyText.setVisibility(View.VISIBLE);
 					readyText.setText(getText(R.string.pvt_countdown) + "3");
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					nextState = PVTState.COUNTDOWN2;
 					handler.postDelayed(mPVTRun, countdownDelay);
 					break;
 				case COUNTDOWN2:
 					readyText.setVisibility(View.VISIBLE);
 					readyText.setText(getText(R.string.pvt_countdown) + "2");
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					nextState = PVTState.COUNTDOWN1;
 					handler.postDelayed(mPVTRun, countdownDelay);
 					break;
 				case COUNTDOWN1:
 					readyText.setVisibility(View.VISIBLE);
 					readyText.setText(getText(R.string.pvt_countdown) + "1");
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					nextState = PVTState.USER_WAIT;
 					handler.postDelayed(mPVTRun, countdownDelay);
 					break;
 				case USER_WAIT: // user waits for stimulus
 					int waitTime = ibiDelays.get(testNum - 1);
 					tempTrial = new PVTResult();
 
 					numText.setText("Trial " + testNum);
 					readyText.setVisibility(View.INVISIBLE);
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					nextState = PVTState.SHOW_STIMULUS;
 					tempTrial.timeStamp = System.currentTimeMillis();
 					tempTrial.length = waitTime;
 					deadlineReached = false;
 					handler.postDelayed(mPVTRun, waitTime); // wait a random
 															// amount of time
 					break;
 				case SHOW_STIMULUS: // show stimulus and begin counting
 					stimulus.setVisibility(View.VISIBLE);
 					readyText.setVisibility(View.INVISIBLE);
 					timeText.setText("");
 					if (showFeedback) {
 						timeText.setVisibility(View.VISIBLE);
 					}
 					nextState = PVTState.SHOW_SCORE;
 					handler.postDelayed(mDeadline, deadlineDelay);
 					if (showTimer) {
 						handler.post(mTimer);
 					}
 					tempStart = System.currentTimeMillis();
 					break;
 				case FALSE_START:
 					readyText.setVisibility(View.INVISIBLE);
 					timeText.setText(getString(R.string.pvt_early));
 					if (showFeedback) {
 						timeText.setVisibility(View.VISIBLE);
 					}
 					tempTrial.score = ((int) (tempEnd - (tempTrial.timeStamp + tempTrial.length)));
 					trials.add(tempTrial);
 					tempTrial = null;
 					nextState = PVTState.USER_WAIT;
 					handler.postDelayed(mPVTRun, restartDelay);
 					break;
 				case DEADLINE:
 					tempEnd = tempStart + deadlineDelay + 1;
 					deadlineReached = true;
 					// drop through to SHOW_SCORE
 				case SHOW_SCORE: // when response is detected
 					testNum++;
 					if (showTimer) {
 						handler.removeCallbacks(mTimer);
 					}
 					long tempScore = tempEnd - tempStart;
 					if (showFeedback) {
 						timeText.setVisibility(View.VISIBLE);
 					}
 					readyText.setVisibility(View.INVISIBLE);
 					tempTrial.score = (int) tempScore;
 					trials.add(tempTrial);
 					tempTrial = null;
 					if (deadlineReached) {
 						timeText.setText(getString(R.string.pvt_late));
 					} else {
 						timeText.setText(tempScore + "ms");
 					}
 					if (testNum > ibis) {
 						nextState = PVTState.GAME_OVER;
 					} else {
 						nextState = PVTState.USER_WAIT;
 					}
 					handler.postDelayed(mPVTRun, restartDelay);
 					break;
 				case GAME_OVER:
 					readyText.setVisibility(View.VISIBLE);
 					readyText.setText(getString(R.string.pvt_end));
 					stimulus.setVisibility(View.INVISIBLE);
 					timeText.setVisibility(View.INVISIBLE);
 					nextState = PVTState.CLEANUP_AND_FINISH;
 					handler.postDelayed(mPVTRun, gameOverDelay);
 					break;
 				case CLEANUP_AND_FINISH:
 					Gson gson = new GsonBuilder()
 							.excludeFieldsWithoutExposeAnnotation().create();
 					Intent intent = new Intent();
 					intent.putExtra("value", gson.toJson(trials));
 					setResult(RESULT_OK, intent);
 					finish();
 					break;
 				}
 			}
 		}
 	}
 
 	private class TimerRefresher implements Runnable {
 		@Override
 		public void run() {
 			long diff = System.currentTimeMillis() - tempStart;
 			timeText.setText(diff + "ms");
 			handler.removeCallbacks(this);
 			handler.postDelayed(this, 0);
 		}
 	}
 
 	private class Deadline implements Runnable {
 		@Override
 		public void run() {
 			nextState = PVTState.DEADLINE;
 			handler.post(mPVTRun);
 		}
 	}
 
 	public class PVTResult {
 		public long timeStamp;
 		@Expose
 		public int length;
 		@Expose
 		public int score;
 	}
 }
