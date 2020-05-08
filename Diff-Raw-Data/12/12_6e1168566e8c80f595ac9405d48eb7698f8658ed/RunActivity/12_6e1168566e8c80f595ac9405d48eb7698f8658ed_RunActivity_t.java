 package com.example.cmsc495pacecalculator;
 
 
 import java.util.Date;
 
 import com.example.cmsc495pacecalculator.R;
 import com.example.cmsc495pacecalculator.R.id;
 import com.example.cmsc495pacecalculator.R.layout;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class RunActivity extends Activity {
 public static int lapsCompleted = 0;
 public static TextView TimerText;
 public static ProgressTracker tracker;
 public static boolean continues = false;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_run);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		continues = false;
 		lapsCompleted = 0;
 		tracker = new ProgressTracker();
 		TimerText = (TextView) findViewById(R.id.textView1);
 		final Thread timeKeeperThread = new Thread(new TimeKeeper());
 		timeKeeperThread.start();
 		
 		
 		
 		
 		
 		
  Button button = (Button) findViewById(R.id.button2);
 	    
 	    
 	    
 	    button.setOnClickListener(new View.OnClickListener() {
 	    	
 	    	
 	        public void onClick(View v) {
 	        	lapsCompleted += 1;
 	        	TextView LapCounterTextView = (TextView) findViewById(R.id.textView3);
	        	LapCounterTextView.setText(String.valueOf(lapsCompleted)+ " Laps Completed\n" + Double.valueOf(Double.valueOf(lapsCompleted) / 4) + " Miles Ran");
 	        	
 	        	tracker.addNewLap((long)TimeKeeper.timer.getCurrentTime());
 	        	
 	        	
 	        		
 					TimeKeeper.kill = true;
 					while(!continues);
 				
 	        	Intent myIntent = new Intent(RunActivity.this, FinishActivity.class);
 	        	//myIntent.putExtra("key", value); //Optional parameters
 	        	RunActivity.this.startActivity(myIntent);
 	            
 	            
 	            
 	        }
 	        
 	        
 	    });
 	    
  Button button1 = (Button) findViewById(R.id.button1);
 	    
 	    
 	    
 	    button1.setOnClickListener(new View.OnClickListener() {
 	    	
 	    	
 	        public void onClick(View v) {
 	        	
 	        	lapsCompleted += 1;
 	        	TextView LapCounterTextView = (TextView) findViewById(R.id.textView3);
	        	LapCounterTextView.setText(String.valueOf(lapsCompleted)+ " Laps Completed\n" + Double.valueOf(Double.valueOf(lapsCompleted) / 4) + " Miles Ran");
 	        	
 	        	tracker.addNewLap((long)TimeKeeper.timer.getCurrentTime());
 	        	
 	            
 	            
 	            
 	        }
 	        
 	        
 	    });
 		
 		
 	}
 	
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.main, menu);
 //		return true;
 //	}
 	
 }
