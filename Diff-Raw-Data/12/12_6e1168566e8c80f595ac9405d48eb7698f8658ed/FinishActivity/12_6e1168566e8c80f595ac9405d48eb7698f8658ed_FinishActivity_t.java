 package com.example.cmsc495pacecalculator;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 
 import com.example.cmsc495pacecalculator.R;
 import com.example.cmsc495pacecalculator.R.layout;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class FinishActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_finish);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		double finalTime = TimeKeeper.timer.finalTime * 1000;
 		
 		String tempString = String.valueOf(Double.valueOf(finalTime));
 		
 		int seconds = (int) (finalTime / 1000) % 60 ;
 		int minutes = (int) ((finalTime / (1000*60)) % 60);
 		int hours   = (int) ((finalTime / (1000*60*60)) % 24);
 		java.text.DecimalFormat nft = new  
 				java.text.DecimalFormat("#00.###");  
 				nft.setDecimalSeparatorAlwaysShown(false);  
 		String finalString = nft.format(Double.valueOf(hours)) + ":" + nft.format(Double.valueOf(minutes)) + ":" + nft.format(Double.valueOf(seconds));
 		
 		
 		
 		
 		TextView lapResults = (TextView) findViewById(R.id.textView1);
 		lapResults.setText(RunActivity.tracker.getStats());
 		TextView paceResults = (TextView) findViewById(R.id.textView2);
 		DecimalFormat df = new DecimalFormat("#.##");
 		if(Double.valueOf(RunActivity.tracker.getLapsRan()) != 0)
 		paceResults.setText("Your pace was " + df.format(calculatePace(Double.valueOf(Double.valueOf(RunActivity.tracker.getLapsRan()) / 4), RunActivity.tracker.getLastTime())) + " minutes per mile.");
 		paceResults.append("\nYour final time was " +
 		finalString + "\nYou Ran a total of " + RunActivity.tracker.getLapsRan() + " Laps"
 				);
 
 		
  Button button = (Button) findViewById(R.id.button1);
 	    
 	    
 	    
 	    button.setOnClickListener(new View.OnClickListener() {
 	    	
 	    	
 	        public void onClick(View v) {
 	        	
 	        	
 	        
 	        	Intent myIntent = new Intent(FinishActivity.this, BeginActivity.class);
 	        	//myIntent.putExtra("key", value); //Optional parameters
 	        	FinishActivity.this.startActivity(myIntent);
 	            
 	        }
 	        
 	        
 	    });
 		
 		
 	}
 	
 	
 	public static double calculatePace(double miles, long time){
 		double pace = (miles / Double.valueOf((Double.valueOf(time) / (1000*60*60))));
 		
 		
 		return 60 / pace;
 	}
 	
 	
 	
 	
 	
 	
 }
