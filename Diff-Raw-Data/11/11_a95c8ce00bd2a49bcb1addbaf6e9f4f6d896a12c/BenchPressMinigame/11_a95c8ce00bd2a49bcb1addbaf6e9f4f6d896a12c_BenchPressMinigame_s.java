 package com.gradugation;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BenchPressMinigame extends Activity {
 	// The number of reps needed in order to "pass" this minigame
 	static int REPS_REQUIRED = 8;
 	// The number of credits earned by completing this minigame
 	static int CREDITS_EARNED = 3;
 	
 	String characterType;
 	
 	CountDownTimer timer;
 	TextView seconds_text, reps_text;
 	int clicks = 0;
 	int reps = 0;
 	ImageView image;
 	
 	boolean game_finished = false;
 	
 	private AlertDialog.Builder builder;
 	private AlertDialog dialog2;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.minigame_bench_press);
 		
 		seconds_text = (TextView) findViewById(R.id.bench_press_time);
 		reps_text = (TextView) findViewById(R.id.bench_press_reps);
 		image = (ImageView) findViewById(R.id.bench_press_image);
 		
 		characterType = getIntent().getStringExtra("character_type").toUpperCase();
 		if (characterType == null) characterType = "ATHLETE";
 		
 		if (characterType.equals("ENGINEER")) image.setImageResource(R.drawable.bench_press_engineer1);
 		else if (characterType.equals("ATHLETE")) image.setImageResource(R.drawable.bench_press_athlete1);
 		else if (characterType.equals("GRADUGATOR")) image.setImageResource(R.drawable.bench_press_gradugator1);
 		else if (characterType.equals("PREMED")) image.setImageResource(R.drawable.bench_press_premed1);
 
 		timer = new CountDownTimer(10500, 1000) {
 			public void onTick(long millisUntilFinished) {
 				seconds_text.setText("Time Left: " + millisUntilFinished / 1000 + " secs");
 			}
 			
 			public void onFinish() {
 				seconds_text.setText("Time Left: 0 secs");
 				game_finished = true;
 				
 				Intent output = new Intent();
 				if (reps >= REPS_REQUIRED) {
 					// Code to add CREDITS_EARNED number of credits to the character
 					// Athlete gets a credit bonus for this minigame
 					if (characterType.equals("ATHLETE")) {
 						Toast.makeText(BenchPressMinigame.this, getString(R.string.bench_press_success, reps, 
 								CREDITS_EARNED + 1), Toast.LENGTH_LONG).show();
 						output.putExtra(Event.BENCH_PRESS_REQUEST_CODE+"", CREDITS_EARNED + 1);
 					}
 					else {
 						Toast.makeText(BenchPressMinigame.this, getString(R.string.bench_press_success, reps, 
 								CREDITS_EARNED), Toast.LENGTH_LONG).show();
 						output.putExtra(Event.BENCH_PRESS_REQUEST_CODE+"", CREDITS_EARNED);
 					}
 				}
 				else {
 					Toast.makeText(BenchPressMinigame.this, getString(R.string.bench_press_failure, reps), 
 							Toast.LENGTH_LONG).show();
 					output.putExtra(Event.BENCH_PRESS_REQUEST_CODE+"", 0);	
 				}
 				setResult(RESULT_OK, output);
 				// Code to make continue button no longer disabled
 			}
 			
 		};
 		
 		builder = new AlertDialog.Builder(this);
         builder.setMessage(getString(R.string.bench_press_instructions1));
         builder.setCancelable(false);
         builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                 
 
                 //@Override
 
                 public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         
                 		builder = new AlertDialog.Builder(BenchPressMinigame.this);
                         builder.setMessage(getString(R.string.bench_press_instructions2, REPS_REQUIRED));
                         builder.setCancelable(false);
                         builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
                                 
 
                                 //@Override
 
                                 public void onClick(DialogInterface dialog, int which) {
                                         dialog.dismiss();
                                         timer.start();
                                 }
                         });
                         
                         dialog2 = builder.create();
                         dialog2.show();
                 }
         });
         
         dialog2 = builder.create();
         dialog2.show();
 	}
 	
 	public void benchPress(View view) {
 		if (!game_finished) {
 			clicks++;
 			reps = clicks / 4;
 			
 			reps_text.setText("Reps: " + reps);
 			
			if (characterType.equals("Engineer")) {
 				if (clicks % 4 == 0) {
 					image.setImageResource(R.drawable.bench_press_engineer3);
 				}
 				else if (clicks == 2 || clicks % 2 == 1) {
 					image.setImageResource(R.drawable.bench_press_engineer2);
 				}
 				else {
 					image.setImageResource(R.drawable.bench_press_engineer1);
 				}
 			}
			else if (characterType.equals("Athlete")) {
 				if (clicks % 4 == 0) {
 					image.setImageResource(R.drawable.bench_press_athlete3);
 				}
 				else if (clicks == 2 || clicks % 2 == 1) {
 					image.setImageResource(R.drawable.bench_press_athlete2);
 				}
 				else {
 					image.setImageResource(R.drawable.bench_press_athlete1);
 				}
 			}
			else if (characterType.equals("Gradugator")) {
 				if (clicks % 4 == 0) {
 					image.setImageResource(R.drawable.bench_press_gradugator3);
 				}
 				else if (clicks == 2 || clicks % 2 == 1) {
 					image.setImageResource(R.drawable.bench_press_gradugator2);
 				}
 				else {
 					image.setImageResource(R.drawable.bench_press_gradugator1);
 				}
 			}
			else if (characterType.equals("PreMed")) {
 				if (clicks % 4 == 0) {
 					image.setImageResource(R.drawable.bench_press_premed3);
 				}
 				else if (clicks == 2 || clicks % 2 == 1) {
 					image.setImageResource(R.drawable.bench_press_premed2);
 				}
 				else {
 					image.setImageResource(R.drawable.bench_press_premed1);
 				}
 			}
 		}
 	}
 	
 	public void ContinueGame(View view) {
 		if (game_finished) {
 			finish();
 		}
 		else {
 			Toast.makeText(this, "The minigame is still going on so this button is disabled", 
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public void onPause() {
 		super.onPause();
 		dialog2.dismiss();
 		timer.cancel();
 	}
 }
