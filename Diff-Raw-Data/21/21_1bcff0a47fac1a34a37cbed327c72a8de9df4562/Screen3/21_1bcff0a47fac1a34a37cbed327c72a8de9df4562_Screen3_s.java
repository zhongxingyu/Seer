 package com.c1.charityworkout;
 
 import android.app.Activity;
 import android.gesture.GestureOverlayView;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class Screen3 extends Activity implements OnClickListener,
 		OnTouchListener {
 
 	// Variables for Swipe Gesture
 	float startX, endX;
 	GestureOverlayView main;
 
 	// Variables for Timer
	long currentTime = 0, newTime = 0, pauseTime = 0, secondsCalc = 0;
 	int minTimer = 0;
 	Button bStart, bStop;
	String seconds, minutes = "00";
 	Thread timer;
 	Boolean startW = false;
 	TextView workoutText;
 
 	// Variables unsorted
 	ImageView imgView;
 	private int y;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.screen_3);
 		rendering();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		while (secondsCalc / 60 > 1) {
 			minTimer++;
 		}
 
 	}
 
 	private void startTimer() {
 		timer = new Thread() {
 			public void run() {
 				currentTime = System.currentTimeMillis();
 				while (startW == true) {
 					try {
 						sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} finally {
 						newTime = ((System.currentTimeMillis() - currentTime) / 1000)
 								+ pauseTime;
						if (newTime % 60 == 0) {
 							minTimer++;
 							minutes = Integer.toString(minTimer);
 							if (minutes.length() == 1) {
 								minutes = "0" + minutes;
 							}
						}

						secondsCalc = newTime - (60 * minTimer);
 						seconds = Long.toString(secondsCalc);
 						if (seconds.length() == 1) {
 							seconds = "0" + seconds;
 						}
 						workoutText.post(new Runnable() {
 							public void run() {
 								if (startW == true) {
 									workoutText
 											.setText(minutes + ":" + seconds);
 								}
 							}
 						});
 					}
 				}
 			}
 		};
 		timer.start();
 	}
 
 	private void rendering() {
 		// TODO Auto-generated method stub
 		workoutText = (TextView) findViewById(R.id.textView);
 		y = com.c1.charityworkout.MainActivity.x;
 		imgView = (ImageView) findViewById(R.id.imageView2);
 		bStart = (Button) findViewById(R.id.start);
 		bStart.setOnClickListener(this);
 		bStop = (Button) findViewById(R.id.stop);
 		bStop.setOnClickListener(this);
 		Drawable image2 = getResources().getDrawable(y);
 		imgView.setImageDrawable(image2);
 		main = (GestureOverlayView) findViewById(R.id.gestureOverlayView1);
 		main.setOnTouchListener(this);
 		startX = 0;
 		endX = 0;
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			startX = event.getX();
 			break;
 		case MotionEvent.ACTION_UP:
 			endX = event.getX();
 		}
 
 		if (startX != 0 && endX != 0) {
 			if (endX - startX > 120) {
 				workoutText.setText("rechts");
 			}
 			if (endX - startX < -120) {
 				workoutText.setText("links");
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId()) {
 		case R.id.start:
 			if (startW != true) {
 				startW = true;
 				startTimer();
 			}
 			break;
 		case R.id.stop:
 			if (startW != false) {
 				startW = false;
 
 				pauseTime = newTime;
				workoutText.setText(minutes + ":" + seconds + " [PAUSED]");
 			}
 			break;
 		}
 	}
 
 }
