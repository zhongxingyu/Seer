 package com.example.instinctiveintervalidentification;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private static final String TAG = "MainActvity";
 	private static final String[] intervals = { "Unison", "m2", "M2", "m3",
 			"M3", "P4", "A4", "P5", "m6", "M6", "m7", "M7", "Octave", "m9", "M9", "m10"};
 
 	/*UI objects*/
 	private static TextView mCountdown;
 	private static RadioGroup mRadioGroup;
 	private static Button mPlayInterval;
 	private static TextView mNumCorrect;
 	private static TextView mNumWrong;
 	
 	
 	private static IntervalPlayer mIntervalPlayer;
 
 	//Answer variables
 	private int correctInterval;
 	private int correctDirection;
 	
 	//Stats
 	private int numCorrect;
 	private int numWrong;
 
 
 	private CountDownTimer mCountDownTimer;
 	private boolean timerRunning;
 	
 	//preferences
 	private int timelimit;
 	private int wavetype;
 	private int max_interval; //0 = unison, 12 = octave, 13 = m9 etc...
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Log.v(TAG, "MainActivity onCreate");
 		/* Fetch UI Objects */
 		mRadioGroup = (RadioGroup) findViewById(R.id.up_or_down_radio_group);
 		mCountdown = (TextView) findViewById(R.id.countdown);
 		mPlayInterval = (Button) findViewById(R.id.play_interval);
 		mNumCorrect = (TextView) findViewById(R.id.num_correct);
 		mNumWrong = (TextView) findViewById(R.id.num_wrong);
 		
 		//Initialize Private members
 		mIntervalPlayer = new IntervalPlayer(1);
 		
 		numCorrect = 0;
 		numWrong = 0;
 		
 		//Set preference
 		SharedPreferences SP = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		timelimit = Integer.parseInt(SP.getString("timelimit", "5"));
		wavetype = Integer.parseInt(SP.getString("waveform", "0"));
 		max_interval = Integer.parseInt(SP.getString("interval", "12"));
 		mIntervalPlayer.setWaveType(wavetype);
 		
 		//Set up GridView of Interval options
 		GridView gridview = (GridView) findViewById(R.id.gridview);
 		gridview.setAdapter(new RadioGridAdapter(this));
 		// gridview.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				Log.v(TAG, "Interval Chosen: " + id);
 				int radioButtonID = mRadioGroup.getCheckedRadioButtonId();
 				View radioButton = mRadioGroup.findViewById(radioButtonID);
 				int direction = mRadioGroup.indexOfChild(radioButton);
 				Log.v(TAG , "Direction Chosen: " + direction);
 
 				
 				if ((id == correctInterval && direction  == correctDirection) ||
 						(id == correctInterval && correctInterval == 0)) {
 					Toast.makeText(getApplicationContext(), "Correct!",
 							Toast.LENGTH_SHORT).show();
 					numCorrect++;
 				} else {
 					Toast.makeText(getApplicationContext(), "Wrong!",
 							Toast.LENGTH_SHORT).show();
 					numWrong++;
 				}
 				mRadioGroup.clearCheck();
 				mCountDownTimer.cancel();
 				timerRunning = false;
 				mCountdown.setText("" + timelimit);
 				mNumCorrect.setText("" + numCorrect);
 				mNumWrong.setText("" + numWrong);
 				mPlayInterval.setEnabled(true);
 			}
 		});
 
 		timerRunning = false;
 		
 
 	}
 
 	public void playInterval(View theButton) {
 		if (!timerRunning) {
 			mPlayInterval.setEnabled(false);
 			Note baseNote = Note.getRandom(Note.A3, Note.A5);// for now,
 																// starting
 																// notes are
 																// between
 																// A3 and A5
 			Random rand = new Random();
 			int interval = rand.nextInt(1 + max_interval);// for now, 0(unison) to 12(octave)
 			correctInterval = interval;
 			int direction = rand.nextInt(2);// 0 = up, 1 = down;
 			correctDirection = direction;
 
 			mIntervalPlayer.playInterval(baseNote, interval, direction);
 
 			mCountDownTimer = new CountDownTimer(timelimit*1000, 100) {
 
 				public void onTick(long millisUntilFinished) {
 					mCountdown.setText("" + millisUntilFinished / 1000);
 				}
 
 				public void onFinish() {
 					timerRunning = false;
 					Toast.makeText(getApplicationContext(), "Out of Time!",
 							Toast.LENGTH_SHORT).show();
 					mCountdown.setText(Integer.toString(timelimit));
 					mPlayInterval.setEnabled(true);
 					numWrong++;
 					mNumWrong.setText("" + numWrong);
 				}
 			};
 
 			Handler handler = new Handler();
 			handler.postDelayed(new Runnable() {
 				public void run() {
 					timerRunning = true;
 					mCountDownTimer.start();
 				}
 			}, 2000);
 		}
 	}
 
 	/*
 	 * public abstract class MyCountDownTimer extends CountDownTimer { private
 	 * boolean isRunning; public MyCountDownTimer(long millisInFuture, long
 	 * countDownInterval) { super(millisInFuture, countDownInterval); isRunning
 	 * = false; }
 	 * 
 	 * }
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		//Log.v("MainActivity", Integer.toString(item.getItemId()));
 		if (item.getItemId() == R.id.menu_settings) {
 			Intent i = new Intent(getBaseContext(), AppPreferences.class);
 			startActivity(i);
 		}
 		return true;
 	}
 
 	
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		SharedPreferences SP = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		timelimit = Integer.parseInt(SP.getString("timelimit", "5"));
 		wavetype = Integer.parseInt(SP.getString("waveform", "0"));
 		max_interval = Integer.parseInt(SP.getString("interval", "12"));
 		mIntervalPlayer.setWaveType(wavetype);
 		mCountdown.setText("" + timelimit);
 	}
 
 
 
 	private class RadioGridAdapter extends BaseAdapter {
 		RadioGroup mRadioGroup;
 		private Context mContext;
 
 		RadioGridAdapter(Context c) {
 			mContext = c;
 			mRadioGroup = new RadioGroup(c);
 		}
 
 		public int getCount() {
 			return intervals.length;
 		}
 
 		public Object getItem(int position) {
 			return intervals[position];
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) mContext
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			View gridView;
 			if (convertView == null) {
 				gridView = inflater.inflate(R.layout.cell, null);
 
 				// set value into Button
 				Button ButtonView = (Button) gridView
 						.findViewById(R.id.interval_button);
 				Log.v(TAG, "interval : " + intervals[position]);
 				ButtonView.setText(intervals[position]);
 				ButtonView.setFocusable(false);
 				ButtonView.setFocusableInTouchMode(false);
 				ButtonView.setClickable(false);
 
 			} else {
 				gridView = (View) convertView;
 			}
 
 			return gridView;
 		}
 	}
 
 }
