 package com.advsofteng.app1;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpParams;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 
 // handles user set POI data
 public class GetPOIActivity extends Activity { 
 	
 	////// Member variables /////////
 	// CheckBoxes that user selects to get data on from server
 	private CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
 	private SeekBar seekBarRadius; // seekBar that user sets the radius of POIs nearby.
 	private TextView tvRadius; // displays the radius data
 	private Integer intRadius; // user set radius
 	private int intMaxRadius = 1000; // maximum radius in metres
 	private Button btnGetPOIdata; // - sends all the data to the server and returns to main screen...
 	private Resources resources1; // - used to access app wide resources.
 	private SharedPreferences prefs;
 	
 	// flag used in saving and loading app wide prefs.
 	//ref: http://sites.google.com/site/jalcomputing/home/mac-osx-android-programming-tutorial/saving-instance-state
 	private Boolean isSavedInstanceState= false; 
 	
 	// string holding the server address
 	private static  String ENDPOINT =null; 
 	private static final String tagPOI = "GetPOIActivity"; // used for debugging
 	//
 	///////////////////////////////////
 	
 	// Called at the start of the full lifetime. 
 	@Override 
 	public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState); 
 	// Initialize activity.
 	
 	setContentView(R.layout.get_poi);
 	
 	// initialise class member data
 	resources1 = getResources();
	prefs = getPreferences(MODE_PRIVATE);
 	
 	// set server address.
 	ENDPOINT = getResources().getString(R.string.serverAddress);
 
 	
 	//
 	if(null != savedInstanceState){ // get saved state from bundle after a soft kill
 	    try {
 	    	InitialiseCheckBoxsAndBtns(savedInstanceState);
 	        Log.i(tagPOI,"RestoredState.");
 	    }
 	    catch(Exception e){
 	        Log.i(tagPOI,"FailedToRestoreState",e);
 	    }
 	}
 	else { // get saved state from preferences on first pass after a hard kill
 	    	InitialiseCheckBoxsAndBtns(null);
 	       Log.i(tagPOI,"gettingPrefs");
 	}
 	//
 	//////////
 	
 	
 	///////
 	// initialise button to respond to clicks with correct action
 	btnGetPOIdata = (Button) findViewById(R.id.getPOIDataButton);
 	btnGetPOIdata.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
     	  
     	   
     	   //TODO: check with Tom about how the server wants the data on POIs..???
     	   //bool / int POI, how to represent pub and restaurant request... for example???
     	   
     	   /*
     	   // have all data here.... boolean - how to work with database POI request???
     	   checkBox1.isChecked();
     	   checkBox2.isChecked();
     	   checkBox3.isChecked();
     	   checkBox4.isChecked();
     	   checkBox5.isChecked();
     	  
     	   // and each checkBox's text... pub, park, etc...
     	   resources1.getText((R.string.poi_checkbox1));
     	   resources1.getText((R.string.poi_checkbox2));
     	   resources1.getText((R.string.poi_checkbox3));
     	   resources1.getText((R.string.poi_checkbox4));
     	   resources1.getText((R.string.poi_checkbox5));
     	   
     	   // and user set radius data...
     	   // intRadius;
     	    */
    			try {
   				//Attempt 1: have tried with and WITHOUT the ctx context - 
   				//TODO: need to add a check that we actually have received GPS data
   				//
   				Context ctx = getApplicationContext();
   				SharedPreferences prefMainApp = ctx.getSharedPreferences(AdvSoftEngApp1Activity.TAG, Context.MODE_PRIVATE); 
    	   			
   				if(null != prefMainApp.getString("latitude", null)) // if we actually get a latitude value back.. then 
    				{												 //  the GPS is working proceed... else....
    					
   				
   					HttpGet get = new HttpGet(ENDPOINT + "/" + prefMainApp.getString("latitude", "") 
   	   												+ "/" + prefMainApp.getString("longitude", "")
    	   												+ "/" + intRadius.toString()
    	   												+ "/" + "one");
    	   												
   	   			
    					//testing to see if we are getting long and lat data... we are not... 
   					Log.i(tagPOI, "Long from prefs = "+ prefMainApp.getString("longitude", ""));
   					Log.i(tagPOI, "lat from prefs = " + prefMainApp.getString("latitude", ""));
   					Log.i(tagPOI, "Mainprefs = " + prefMainApp.toString());
 
 
    					// log not working!!! - so have to display the data on the checkbox text for now...
    					//checkBox2.setText("strLat = " + strLat);
    					//checkBox3.setText("strLong = " + strLong);
    					//checkBox2.setText("strLat = " );//+ String.valueOf(AdvSoftEngApp1Activity.currentLatitude));
    					//checkBox3.setText("strLong = " ); //+ String.valueOf(AdvSoftEngApp1Activity.currentLongitude));
 
    					//
    					////////////////////
 
    					////////////
    					// TESTING BLOCK -REPLACE "49.56" AND "-12.257" WITH correct values for long and lat
    					// when ACCESSING APPWIDE PREFS IS SORTED...
    					// just using these data here to get some request back from the server...
 
    					/* Create a new HTTPClient to do our POST for us */
    					HttpClient client = new DefaultHttpClient();
 
    					/*
   	   			HttpGet get = new HttpGet(ENDPOINT + "/" + "49.56"
 								+ "/" + "-12.257"
 								+ "/" + intRadius.toString()
 								+ "/" + "1");
    					 */
 
    					HttpResponse response = client.execute(get);
 
    					// need to get params out of the response... how???
    					// 1) ??
    					HttpParams params = response.getParams();
    					// Log not working!! - use the text boxes again temp. to display data...
    					// checkBox4.setText("HTTP response = " + params.toString());
 
    					//TODO: remove this testing code once we know we are getting the correct data back from the HTTP request.
    					// 2) ??
    					//Log.i(tagPOI, "get done, response="+response.getStatusLine().getStatusCode());
    					// 3) ??
    					//Log.i(tagPOI, "HTTP response = " + response.toString());
    				} // end of if(null!= prefMainApp.getString("latitude", "")) 
    				else { // we have NOT got GPS data.....
    					//btnGetPOIdata.setText(resources1.getString( R.string.no_gps_error));
    					//CharSequence strMsg1 = resources1.getStringArray( R.string.no_gps_error);
   					Toast toast=Toast.makeText(ctx, "No GPS At Present", 2000);
    					toast.setGravity(Gravity.TOP, -30, 50);
    					toast.show();
    				}
    				
    			} catch (Exception e) {
    				Log.i(AdvSoftEngApp1Activity.TAG, "get to getPOI failed, " + e.getMessage());
    				
    			}
    			
    			
    			///
    			////////////////
     	
    			//TODO: uncomment "finish()" method call after testing is done... 
    			// for the moment - press return on phone/emulator to return to main screen...
        finish(); // return to previous screen...
    			
        } } );
 	//
 	/////////////
 	}
 	
 	// create and instantiate and override implemented seekbar methods...
 	private SeekBar.OnSeekBarChangeListener seekBarListen = new SeekBar.OnSeekBarChangeListener() {
 		
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			
 			// update member variable and textview with new radius data...
 			intRadius = progress;
 			
 			tvRadius = (TextView) findViewById(R.id.tvRadius);
 			tvRadius.setText(resources1.getText((R.string.tvRadiusText)) + Integer.toString(intRadius) + " m");
 			
 		}
 	};
 	
 	// Called after onCreate has finished, use to restore UI state 
 	@Override 
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		
 	super.onRestoreInstanceState(savedInstanceState); 
 	// Restore UI state from the savedInstanceState.
 	// This bundle has also been passed to onCreate.
 	
 	if(savedInstanceState != null){
 		
 		intRadius = savedInstanceState.getInt((String)resources1.getText(R.string.tvRadiusText), 0);
 	}
 	
 	}
 	
 	 
 	private void InitialiseCheckBoxsAndBtns(Bundle lastInstanceState)
 	{
 		
 		// CheckBox setup...
 		checkBox1 = (CheckBox) findViewById(R.id.CheckBox1);
 		checkBox2 = (CheckBox) findViewById(R.id.CheckBox2);
 		checkBox3 = (CheckBox) findViewById(R.id.CheckBox3);
 		checkBox4 = (CheckBox) findViewById(R.id.CheckBox4);
 		
 		
 		// set text....
 		checkBox1.setText(resources1.getText(R.string.poi_checkbox1));
 		checkBox2.setText(resources1.getText(R.string.poi_checkbox2));
 		checkBox3.setText(resources1.getText(R.string.poi_checkbox3));
 		checkBox4.setText(resources1.getText(R.string.poi_checkbox4));
 		
 		
 		/////////////////
 		//seekBar and corresponding label set up
 		seekBarRadius = (SeekBar) findViewById(R.id.seekBarRadius);
 		seekBarRadius.setOnSeekBarChangeListener(seekBarListen);
 		seekBarRadius.setMax(intMaxRadius);
 
 		
 		///////////////////////////////////////////////////////
 		// everything below is called when this activity is paused and then resumed,
 		// rather than when starting the activity from scratch
 		boolean booleanFlag = false; // used as a temp boolean value holder below
 		if(null == lastInstanceState)
 		{
 			//get checkbox1's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox1)), booleanFlag);
 			checkBox1.setChecked(booleanFlag);
 			
 			//get checkbox2's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox2)), booleanFlag);
 			checkBox2.setChecked(booleanFlag);
 			
 			//get checkbox3's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox3)), booleanFlag);
 			checkBox3.setChecked(booleanFlag);
 			
 			//get checkbox4's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox4)), booleanFlag);
 			checkBox4.setChecked(booleanFlag);
 			
 			// get radius value...
 			intRadius = prefs.getInt((String)resources1.getText(R.string.tvRadiusText), 0);
 		}
 		else //If lastInstanceState != null, then we are to use preferences to load data...
 		{
 			if(null != lastInstanceState)
 				intRadius = lastInstanceState.getInt((String)resources1.getText(R.string.tvRadiusText), 0);		
 		}
 		
 		seekBarRadius.setProgress(intRadius);
 
 		//radius label
 		tvRadius = (TextView) findViewById(R.id.tvRadius);
 		tvRadius.setText(resources1.getText((R.string.tvRadiusText)) + Integer.toString(intRadius) + " m");
 		
 		//seekBar and corresponding label set up
 		/////////////////////////////////////////
 		
 	}
 
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		
 		isSavedInstanceState= true;
 		
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox1)), checkBox1.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox2)), checkBox2.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox3)), checkBox3.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox4)), checkBox4.isChecked());
 		
 		savedInstanceState.putInt((String)resources1.getText(R.string.tvRadiusText), intRadius);
 		
 		super.onSaveInstanceState(savedInstanceState);
 
 	}
 
     protected void onStart() {
         Log.i(tagPOI,"onStart");
         
         InitialiseCheckBoxsAndBtns(null);
         super.onStart();
     }
     protected void onResume() {
         Log.i(tagPOI,"onResume");
     	isSavedInstanceState= false;
         super.onResume();
     }
     protected void onStop() {
     	
     	if (!isSavedInstanceState){ // this is a HARD KILL, write to prefs
       
             SharedPreferences.Editor editor = prefs.edit();
             
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox1), checkBox1.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox2), checkBox2.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox3), checkBox3.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox4), checkBox4.isChecked());
             editor.putInt((String)resources1.getText(R.string.tvRadiusText), intRadius);
             
             editor.commit();
             Log.i(tagPOI,"savedPrefs");
             
         }
         else {
             Log.i(tagPOI,"DidNotSavePrefs");
         }
         super.onStop();
     }
 
 }
