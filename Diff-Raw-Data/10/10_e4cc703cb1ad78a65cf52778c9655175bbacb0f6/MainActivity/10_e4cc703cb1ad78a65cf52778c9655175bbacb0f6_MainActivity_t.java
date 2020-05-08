 package com.dylam.mathlete;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	int min = 0;
 	int max = 100;
 	int correct_count = 0;
 	String TAG = "mathlete";
 	int a;
 	int b;
 	TextView number1;
 	TextView number2;
 	Random r;
 	EditText user_input;
 	ProgressBar mCountdownBar; 
 	int maxCountdownTime = 5;  // in seconds?
 	int currentCountdownTime = maxCountdownTime;
 	final int mCountdownBarMax = 100;
 	final int maxTimeInMillis = maxCountdownTime * 1000;
 	CountDownTimer timer;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Generate numbers
 		r = new Random();
 		number1 = (TextView)findViewById(R.id.number_1);
 		number2 = (TextView)findViewById(R.id.number_2);
 		
 		currentCountdownTime = 0;
 		
 		// Set up input key listener.
 		user_input = (EditText)findViewById(R.id.user_answer);
 		user_input.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					on_submit(null);
 					handled = true;
 				}
 				
 				return handled;
 			}
 		});
 
 		mCountdownBar = (ProgressBar)findViewById(R.id.countdownBar);
 		mCountdownBar.setMax(maxTimeInMillis);
 		mCountdownBar.setProgress(maxTimeInMillis/2); 
 		currentCountdownTime = maxCountdownTime;
 
 		generateNumbers();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	private void generateNumbers() {
 		a = r.nextInt(this.max - this.min);
 		b = r.nextInt(this.max - this.min);
 		
 		number1.setText(Integer.toString(a));
 		number2.setText(Integer.toString(b));
 		
 		// Start a new timer
 		if (timer != null) {
 			timer.cancel();
 		}
 		
 		startCountdown();
 	}
 	
 	public void on_submit(View v) {
 		// Get input from user
		String input= ((EditText) findViewById(R.id.user_answer)).getText().toString();
		int answer = 0;
		
		if (input.length() == 0) {
			return;
		} else {
			answer = Integer.parseInt(input);
		}
 		
 		// Check results
 		String result = "";
 		if (a + b == answer) {
 			result = "Correct!";
 			generateNumbers();
 		} else {
 			result = "Incorrect!";
 		}
 
 		Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
 		
 		// Clear input
 		user_input.setText("");
 	}
 
 	private void startCountdown() {
 		timer = new CountDownTimer(maxTimeInMillis, 1000) {
 
 			@Override
 			public void onFinish() {
 				// TODO Auto-generated method stub
 				mCountdownBar.setProgress(0);
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// Update countdown bar.
 				mCountdownBar.setProgress((int) millisUntilFinished);
 			}
 		}.start();
 	}
 
 }
