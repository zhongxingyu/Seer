 /* Author: Clinton Olson
  * Email: clint.olson2@gmail.com
  * Last Change: June 20, 2013
  * License: ???
  */
 
 /*** TODO
  *   1- implement all lifecycle methods
  *   2- implement UI elements as Fragments
  *   3- error checking
  *   4- state saving
  *   5- connection with ServerComMgr
  *   6- implement HelpActivity transition
  *   7- data storage/passing between activities
  */
 
 package org.leifolson.withinreach;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 //import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 //import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class MenuActivity extends Activity {
 	
 	// setting up some variables to hold menu choices
 	// may need to store this information differently
 	// or find a way to pass it back to the WithinReachActivity
 	// at a later time
 	// its possible we dont even need many of these
 	private int timeConstraint = 15;
 	private int modeCode = 7;
 	private boolean walkToggled = true;
 	private boolean bikeToggled = true;
 	private boolean transitToggled = true;
 	private final int MAX_TIME = 90;
 	
 	//the lat/long will be passed from the WithinReachActivity
 	private double latitude = 0.0; 
 	private double longitude = 0.0;
 	
 	// the menu has the following UI elements
 	// these will most likely be changed to fragments at a later time
 	private TextView seekTime;
 	private SeekBar timeSeekBar;
 	private ToggleButton walkToggleButton;
 	private ToggleButton bikeToggleButton;
 	private ToggleButton transitToggleButton;
 	private Button menuButton;
 
 	// I can probably put some wrapper functions in here to abstract away
 	// all the details of setting up the listeners for UI elements
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 		
 		// getting a reference to the seekbar and its corresponding textview
 		seekTime = (TextView)findViewById(R.id.time_text);
 		timeSeekBar = (SeekBar)findViewById(R.id.time_seekbar);
 		
 		timeSeekBar.setMax(MAX_TIME);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null)
 		{
 			latitude = extras.getDouble("latitude");
 			longitude = extras.getDouble("longitude");
 		}
 		
 		// getting a reference to the toggle buttons
 		walkToggleButton = (ToggleButton)findViewById(R.id.toggle_button_walk);
 		bikeToggleButton = (ToggleButton)findViewById(R.id.toggle_button_bike);
 		transitToggleButton = (ToggleButton)findViewById(R.id.toggle_button_transit);
 		
 		// set options to default values
 		walkToggleButton.setChecked(true);
 		bikeToggleButton.setChecked(true);
 		transitToggleButton.setChecked(true);
 		
 		timeSeekBar.setProgress(15);
 		seekTime.setText(Integer.toString(timeSeekBar.getProgress()));
 		
 		// setting up click listener for the seekbar
 		timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
 				if(fromUser){ 
 					// change textview to new time selection
 					seekTime.setText(progress + " mins");
 					
 					// store time constraint value
 					timeConstraint = progress;
 				}
 			}
 			
 			// nothing to implement here but must be present
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 			
 
 		});
 		
 		// setting up toggle button listeners
 		walkToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				walkToggled = isChecked;
 			}
 		});
 		
 		bikeToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
 				bikeToggled = isChecked;
 			}
 		});
 		
 		transitToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
 				transitToggled = isChecked;
 			}
 		});
 		
 		// getting a reference to the done button
 		menuButton = (Button) findViewById(R.id.menu_done_button);
 		
 		// setting up the onclick listener to handle the button click
 		// this is just a way to launch the WithinReachActivity for now
 		menuButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View view) {
 				// takes us back to the WithinReachActivity
 				// will need to do some communication with the server before
 				// invoking the WithinReachActivity
 				
 				invokeServerComMgr();
 				
 			}
 		});
 		
 	}
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		
 	}
 	
 	@Override
 	protected void onStop(){
 		super.onStop();
 	}
 	
 	@Override
 	protected void onPause(){
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 	}
 	
 	
 //	@Override
 //	public void onConfigurationChanged(Configuration newConfig){
 //		super.onConfigurationChanged(newConfig);
 //		System.out.println("Config CHANGED *****");
 //		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
 //			seekTime.setText(Integer.toString(timeSeekBar.getProgress()));
 //			System.out.println("Orientation land CHANGED *****");
 //		}
 //		
 //		if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
 //			seekTime.setText(Integer.toString(timeSeekBar.getProgress()));
 //			System.out.println("Orientation port CHANGED *****");
 //		}
 //	}
 	
 	// when the user clicks the "Done!" button, this method will be called,
 	// launching the main WithinReach activity
 	public void invokeServerComMgr(){
 		//Setting up a handler to handle the server response. If the server gives back a message of 1, then this handler
 		//will call 
 		Handler asyncHandler = new Handler()
 		{
 		    public void handleMessage(Message msg){
 		        super.handleMessage(msg);
 		        //What did that async task say?
 		        switch (msg.what)
 		        {
 		            case 1:
 		                returnToWithinReachActivity();
 		                break;                      
 		        }
 		    }
 		};  
 
 		modeCode = 0;
 		if (walkToggled)
 			modeCode += 1;
 		if (bikeToggled)
 			modeCode += 2;
 		if (transitToggled)
 			modeCode += 4;
 		
 		ServerInvoker invoker = new ServerInvoker(this, asyncHandler, latitude, longitude, modeCode, timeConstraint);
 		invoker.invokeServerComMgr();
 	}
 	
 	public void onNewIntent(Intent t) 
 	{
 		Bundle extras = t.getExtras();
 		if (extras != null)
 		{
 			latitude = extras.getDouble("latitude");
 			longitude = extras.getDouble("longitude");
 		}
 		
 	}
 
 	public void returnToWithinReachActivity()
 	{
 		Intent launchWithinReach = new Intent(this, WithinReachActivity.class);
		launchWithinReach.putExtra("serverCallDone", 1);
 		launchWithinReach.putExtra("timeConstraint", timeConstraint);
 		launchWithinReach.putExtra("modeCode", modeCode);
 		startActivity(launchWithinReach);
 	}
 }
