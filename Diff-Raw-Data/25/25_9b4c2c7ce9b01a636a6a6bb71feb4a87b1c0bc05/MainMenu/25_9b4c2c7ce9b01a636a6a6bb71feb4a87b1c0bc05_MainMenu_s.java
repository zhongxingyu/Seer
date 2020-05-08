 package com.mobvcasting.localreport2012;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainMenu extends Activity implements OnClickListener {
 
 	Button videoButton, audioButton;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         initUI();
         
         setMessage(MESSAGE);
         
         /*
         //2 second delay before starting countdown 
         //for design and testing purposes
         final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
           @Override
           public void run() {
                   startCountdown(TIME);
           }
         }, 2000);
    	*/
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       initUI();
     }
 	
 
 	public void initUI() {
         setContentView(R.layout.activity_main_menu);
         
         videoButton = (Button) this.findViewById(R.id.button1);
         audioButton = (Button) this.findViewById(R.id.button2);
         
         videoButton.setOnClickListener(this);
         audioButton.setOnClickListener(this);
 	}
     
     
 
 	@Override
 	public void onClick(View v) {
 		if (v == audioButton) {
     		startActivity(new Intent(this, AudioCall.class));
 		} else if (v == videoButton) {
     		startActivity(new Intent(this, VideoCapture.class));			
 		}
 	}
     
     /* 
      * current countdown is just 30 seconds everytime
      * but startCountdown method takes in an int in seconds
      */
 	 public void startCountdown(int time){
         final TextView countdown = (TextView) this.findViewById(R.id.timer); 
     	new CountDownTimer(time, ONE_SECOND) {
 
     	     public void onTick(long millisUntilFinished) {
     	    	 countdown.setText("Time remaining: " + millisUntilFinished / ONE_SECOND);
     	     }
 
     	     public void onFinish() {
     	         countdown.setText("Time's up!");
     	     }
     	  }.start();
     }
 	
 	//method to allow us to set message on main menu screen
 	public void setMessage(String message){
 		TextView messageView = (TextView) this.findViewById(R.id.messageView);
 		messageView.setText(message);
 	}
 	
 	//private constants to make code easier to change for final app timer
 	private static int TIME = 30000;
     private static int ONE_SECOND = 1000;
     private static String MESSAGE = "example message sent from server";
 }
