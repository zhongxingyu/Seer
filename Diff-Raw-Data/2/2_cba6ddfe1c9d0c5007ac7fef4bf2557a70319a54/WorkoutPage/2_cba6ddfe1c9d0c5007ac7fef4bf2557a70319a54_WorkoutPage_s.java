 package com.c1.charityworkout;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Window;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WorkoutPage extends Activity implements OnClickListener {
 
 	// Variables for Saving stats
 	private static final String FILENAME = "history.txt";
 
 	// Variables for Timer & Other stats
 	private long currentTime = 0, pauseTime = 0, secondsCalc = 0;
 	public static long newTime = 0;
 	private int minTimer = 0;
 	private float donationPerKm;
 	private Button bStart, bStop;
 	private String seconds = "00", minutes = "00", pauseMessage,
 			stopWarningMsg, stopMessage, timerText, totalDistance,
 			averageSpeed, amountDonated, choice, workout;
 	private Thread timer;
 	private static Boolean startW = false;
 	private TextView timerView, distanceView, speedView, amountView;
 	private Boolean threadFinished = true;
 	private SharedPreferences getAmount;
 
 	// Variables of banner
 	private ImageView imgView;
 	private int banner;
 	private Drawable image2;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.screen_3);
 		rendering();
 	}
 
 	private void rendering() {
 		// TODO Auto-generated method stub
 		getBundle();
 		getPreferences();
 		timerView = (TextView) findViewById(R.id.timerView);
 		distanceView = (TextView) findViewById(R.id.distanceView);
 		amountView = (TextView) findViewById(R.id.amountView);
 		speedView = (TextView) findViewById(R.id.speedView);
 		imgView = (ImageView) findViewById(R.id.imageView2);
 		bStart = (Button) findViewById(R.id.start);
 		bStart.setOnClickListener(this);
 		bStop = (Button) findViewById(R.id.stop);
 		bStop.setOnClickListener(this);
 		image2 = getResources().getDrawable(banner);
 		imgView.setImageDrawable(image2);
 		pauseMessage = getResources().getString(R.string.pauseWorkout);
 		stopWarningMsg = getResources().getString(R.string.stopWarning);
 		stopMessage = getResources().getString(R.string.stopWorkout);
 		timerText = minutes + ":" + seconds;
 	}
 
 	private void getPreferences() {
 		// TODO Auto-generated method stub
 		getAmount = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		String donationPerKmString = null;
 		if (choice.equals("Running")) {
 			donationPerKmString = getAmount.getString("donRunning", "5");
 		} else if (choice.equals("Cycling")) {
 			donationPerKmString = getAmount.getString("donCycling", "5");
 		}
 		donationPerKm = Float.parseFloat(donationPerKmString);
 	}
 
 	private void getBundle() {
 		// TODO Auto-generated method stub
 		Bundle gotBundle = getIntent().getExtras();
 		choice = gotBundle.getString("choice");
 		if (choice.equals("Running")) {
 			banner = R.drawable.runningbanner;
 			workout = "Running";
 
 		} else if (choice.equals("Cycling")) {
 			banner = R.drawable.cyclingbanner;
 			workout = "Cycling";
 		}
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
 						threadFinished = false;
 						newTime = ((System.currentTimeMillis() - currentTime) / 1000)
 								+ pauseTime;
 						secondsCalc = newTime - (60 * minTimer);
 						if (secondsCalc / 60 >= 1) {
 							minTimer++;
 							minutes = Integer.toString(minTimer);
 							if (minutes.length() == 1) {
 								minutes = "0" + minutes;
 								secondsCalc = secondsCalc - 60;
 							}
 						}
 						seconds = Long.toString(secondsCalc);
 						if (seconds.length() == 1) {
 							seconds = "0" + seconds;
 						}
 						timerView.post(new Runnable() {
 							public void run() {
 								if (startW == true) {
 									timerText = minutes + ":" + seconds;
 									timerView.setText(timerText);
 								}
 							}
 						});
 						distanceView.post(new Runnable() {
 							public void run() {
 								totalDistance = GoogleMapFragment.totalDistance;
 
 								if (totalDistance != null) {
 									totalDistance = totalDistance.substring(0,
 											totalDistance.indexOf(".") + 3);
 									distanceView
 											.setText(totalDistance + "  km");
 								} else {
 									distanceView.setText("0.00 km");
 								}
 							}
 						});
 						speedView.post(new Runnable() {
 							public void run() {
 								averageSpeed = GoogleMapFragment.averageSpeedString;
 
 								if (averageSpeed != null) {
 									averageSpeed = averageSpeed.substring(0,
 											averageSpeed.indexOf(".") + 2);
 									speedView.setText(averageSpeed + " km/u");
 								} else {
 									speedView.setText("0.0 km/h");
 								}
 							}
 						});
 						amountView.post(new Runnable() {
 							public void run() {
 								
 									if (averageSpeed != null) {
 
 										int amountOfKm = Integer.parseInt(totalDistance
 												.substring(0, totalDistance
 														.indexOf(".")));
 										float totalAmount = amountOfKm
 												* donationPerKm;
 										amountDonated = Float
 												.toString(totalAmount);
 										amountDonated = ""
 												+ amountDonated
 														.substring(
 																0,
 																amountDonated
																		.indexOf(".") + 2);
 										amountView.setText(amountDonated);
 									} else {
 										amountDonated = "0.00";
 										amountView.setText(amountDonated);
 									}
 								} 
 						});
 						threadFinished = true;
 					}
 				}
 			}
 		};
 		timer.start();
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId()) {
 
 		case R.id.start:
 			if (GoogleMapFragment.gpsReady == true) {
 				if (startW != true) {
 					startW = true;
 					GoogleMapFragment.locTrack = true;
 					startTimer();
 					timerView.setText(timerText);
 					Toast.makeText(this, "Workout started", 1000).show();
 				}
 			} else {
 				Toast.makeText(this, "Please wait for GPS Fix", 2000).show();
 			}
 			break;
 		case R.id.stop:
 			if (startW != false) {
 				startW = false;
 				GoogleMapFragment.locTrack = false;
 				pauseTime = newTime;
 				Toast.makeText(WorkoutPage.this, stopWarningMsg, 2000).show();
 				timerView.setText(timerText + " [" + pauseMessage + "]");
 			} else {
 
 				timerView.setText(timerText + " [" + stopMessage + "]");
 				while (threadFinished != true) {
 					// wait for thread to finish before resetting values.
 				}
 				pauseTime = 0;
 				minTimer = 0;
 				getResult();
 			}
 			break;
 		}
 	}
 
 	private void getResult() {
 		// TODO Auto-generated method stub
 		if (averageSpeed != null && totalDistance != null && timerText != null
 				&& amountDonated != null) {
 			writeResults();
 			Intent resultPage = new Intent(WorkoutPage.this,
 					WorkoutHistory.class);
 			startActivity(resultPage);
 			finish();
 		}
 	}
 
 	private void writeResults() {
 		// TODO Auto-generated method stub
 		String string = workout + " - " + averageSpeed + " - " + totalDistance
 				+ " - " + timerText + " - " + amountDonated + "\r\n";
 		try {
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
 					openFileOutput(FILENAME, Context.MODE_APPEND));
 			outputStreamWriter.write(string);
 			outputStreamWriter.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
