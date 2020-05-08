 package com.TDT.room_identification;
 
 import com.TDT.room_identification.*;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LocationGuess extends Activity {
 
 	private boolean isRunning = false;
 	private LocationGuessThread c = new LocationGuessThread();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_location_guess);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_location_guess, menu);
         return true;
     }
     
     
     
     public void startStopGuessing(View view) {
     	/* Handle the button logging (Start/Stop) */
     	Button editText = (Button) findViewById(R.id.StartStopGuessing);
     	TextView statusText = (TextView) findViewById(R.id.CurrentLocationLabel);
     	
     	if(editText.getText().equals("Start Guessing")) {
     		c.prep();
     		new Thread(c).start();
     		isRunning = true;
     		Toast.makeText(LocationGuess.this,"Beginning Guessing Process...", Toast.LENGTH_SHORT).show();
     		statusText.setText("");
    		editText.setText("Stop Logging");
    	} else if (editText.getText().equals("Stop Logging")) {
     		isRunning = false;
     		c.stop();
     		Toast.makeText(LocationGuess.this, "Stopping Guessing Process...", Toast.LENGTH_SHORT).show();
     		statusText.setText("(off)");
    		editText.setText("Start Logging");
     	}
     }
 }
