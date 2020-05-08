 package com.nick.countdownTimer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class CountdownActivity extends Activity   {
 	
 	public static TextView countdown_text;
 	public static Button finishButton;
 	
 	public static int hours;
 	public static int minutes;
 	public static int seconds;
 	
 	public static boolean timerFinished;
 	private Timer countdown = null;
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.countdown);
 	        countdown_text = (TextView) findViewById(R.id.countdown_text);
 	        finishButton= (Button) findViewById(R.id.finish_button);
 	        
 	         hours = getIntent().getIntExtra("hours", 0);
 	         minutes = getIntent().getIntExtra("minutes", 0);
 	         seconds = getIntent().getIntExtra("seconds", 0);
 	        
	        long  time = ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds*1000));
 
 	        countdown = new Timer(time,1000, this);
 	        timerFinished = false;
 	        
 	        countdown.start();
 	         
 	        finishButton.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View arg0) {
 					if(!timerFinished)
 						confirmCancel();
 					else
 						end();
 				}	
 	        });
 		}
 
 	@Override
 	public void onBackPressed() {
 		if(!timerFinished){
 			confirmCancel();
 		}
 		else{
 			end();
 		}
 	}
 	
 	public void confirmCancel(){
 		AlertDialog.Builder confirmation = new AlertDialog.Builder(this);
 	    confirmation.setMessage("Are you sure you want to cancel the countdown?")
 	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                    end();
 	               }
 	           })
 	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                    dialog.cancel();
 	               }
 	           });
 	    AlertDialog alert = confirmation.create();
 	    alert.show();
 	}
 	
 	private void end(){
 		countdown.cancel();
		Timer.r.stop();
		Timer.v.cancel();
		finish();
 		
 		
 	}
 }
 
