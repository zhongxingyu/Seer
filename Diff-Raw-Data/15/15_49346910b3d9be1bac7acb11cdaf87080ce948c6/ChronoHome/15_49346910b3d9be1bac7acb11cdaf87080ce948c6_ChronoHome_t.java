 package com.milkish.chrono;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import com.milkish.chrono.R;
 
 public class ChronoHome extends Activity{
 	public static final String CLASSIO_VERSION = "1";
 	public static final String PREFS_NAME = "Milk_Prefs";
 	
 	Context mContext = this.getBaseContext();
 	private Handler mHandler;
 	private Runnable clockTask;
 	private SharedPreferences storedSettings;
 	private boolean continentalTime;
 	private boolean paused = false;
 	private FrameLayout wrapper;
 	private TextView clock;
 	private TextView seconds;
 	private TextView day;
 	private TextView date;
 	private TextView hr24;
 	private EditText et;
 	private Button tryBut;
 	private int currentBackgroundIndex = 0;
 	private int[] backgrounds = {
     		R.drawable.background1,
     		R.drawable.background2,
     		R.drawable.background3,
     		R.drawable.background4
     };
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Log.i("classio version: ", CLASSIO_VERSION);
 		
 		//go full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         
         //setup view
         setContentView(R.layout.main);
         
         //load font 
 		Typeface digital = Typeface.createFromAsset(getAssets(), "MilkWatch.ttf");
 		
 		//grab display items
 		wrapper = (FrameLayout) this.findViewById(R.id.wrapper);
 		clock = (TextView) this.findViewById(R.id.clock);
 		seconds = (TextView) this.findViewById(R.id.seconds);
 		day = (TextView) this.findViewById(R.id.day);
 		date = (TextView) this.findViewById(R.id.date);
 		hr24 = (TextView) this.findViewById(R.id.hr24);
 		et = (EditText) this.findViewById(R.id.adjust);
 		tryBut = (Button) this.findViewById(R.id.tryBut);
 		
 		//apply font
 		clock.setTypeface(digital);
 		seconds.setTypeface(digital);
 		day.setTypeface(digital);
 		date.setTypeface(digital);
 		
 		//grab preferences
 	    storedSettings = getSharedPreferences(PREFS_NAME, 0);
 	    continentalTime = storedSettings.getBoolean("continentalTime", false);
 	    currentBackgroundIndex = storedSettings.getInt("background", 1);
 	    //restore hour display setting
 	    updateHourDisplayIndicator();
	  	wrapper.setBackgroundResource(backgrounds[currentBackgroundIndex]);

 		//create handler to do the looping
 		mHandler = new Handler();
 		
 		//create runnable for the handler
 		clockTask = new Runnable() {
 			public void run() {
 				Date d = new Date();
 				
 				DateFormat clockF;
 				DateFormat secondsF = new SimpleDateFormat("ss");
 				DateFormat dayF = new SimpleDateFormat("EE");
 				DateFormat dateF = new SimpleDateFormat("MM-dd");
 				
 				int hour = d.getHours();
 				
 				if(continentalTime){
 					// backtick ( ` ) is the character for non
 					// monospace colon ( : )
 					if(hour < 10){
 						clockF = new SimpleDateFormat(" H`mm");
 					} else {
 						clockF = new SimpleDateFormat("H`mm");
 					}
 				} else{
 					if(hour > 12){ hour = hour-12; }
 					if( hour < 10 && hour > 0){
 						clockF = new SimpleDateFormat(" h`mm");
 					}else{
 						clockF = new SimpleDateFormat("h`mm");
 					}
 				}
 				
 				
 				StringBuffer dateStr = new StringBuffer().append(
 						dateF.format(d));
 				if(dateStr.charAt(0) == '0'){
 					dateStr.setCharAt(0, ' ');
 				}
 				if(dateStr.charAt(3) == '0'){
 					dateStr.setCharAt(3, ' ');
 				}
 				
 				
 				clock.setText(clockF.format(d));
 				seconds.setText(secondsF.format(d));
 				day.setText(dayF.format(d).subSequence(0, 2));
 				date.setText(dateStr.toString());
 
 				//run the handler again in .1 sec if not paused
 				if(paused == false){ mHandler.postDelayed(this, 100); }
 			}
 		};
 		
 		/* I set this up when I need to move around
 		 * display elements interactively since the
 		 * visual editor is pretty crappy for anything
 		 * complex. see onCLick() .
 		 * 
 		 * tryBut.setOnClickListener(this);
 		 */
 		
 		//after onCreate(), onResume() is automatically called
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		//un-pause the handler
 		this.paused = false;
 		//run the handler
 		mHandler.postDelayed(clockTask, 10);
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		//when the user leaves the app we want 
 		//to pause it to conserve resources
 		paused = true;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//the inflater builds java objects out of xml
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.watch_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		//this switches the title for the menu button
 		//that toggles between 12 and 24 hour display
 		MenuItem mi = (MenuItem) menu.findItem(R.id.changeHourDisplay);
 		
 		if(continentalTime){
 			mi.setTitle(R.string.MenuButton12);
 		} else{
 			mi.setTitle(R.string.MenuButton24);
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.changeHourDisplay:
 	        switchHourDisplay();
 	        return true;
 	    case R.id.changeBackground:
 	    	changeBackground();
 	    	return true;
 	    case R.id.aboutButton:
 	    	showAbout();
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	
 	public void switchHourDisplay() {
 		continentalTime = !continentalTime;
 		//Save the hour display preference
 		SharedPreferences.Editor editor = storedSettings.edit();
 	    editor.putBoolean("continentalTime", continentalTime);
 	    editor.commit();
 	    updateHourDisplayIndicator();
 	}
 	
 	public void updateHourDisplayIndicator() {
 		if(continentalTime){
 	    	hr24.setVisibility(View.VISIBLE);
 	    }else{
 	    	hr24.setVisibility(View.INVISIBLE);
 	    }
 	}
 	
 	public void changeBackground() {
 		currentBackgroundIndex += 1;
 		if(currentBackgroundIndex >= backgrounds.length){
			currentBackgroundIndex = 0;
 		}
 	  	wrapper.setBackgroundResource(backgrounds[currentBackgroundIndex]);
 	  	SharedPreferences.Editor editor = storedSettings.edit();
 	    editor.putInt("background", currentBackgroundIndex);
 	    editor.commit();  
 	}
 	
 	public void showAbout() {
 		//Inflate the xml to be used in the alert
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.about,
                 (ViewGroup) findViewById(R.id.aboutRoot));
 		
 		//Create the alert builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		//give it some props
 		builder.setCancelable(true);
 		builder.setInverseBackgroundForced(true);
 		//builder.setIcon(R.drawable.dialog_question);
 		builder.setTitle(R.string.AboutPupupTitle);
 		
 		//insert the inflated xml
 		builder.setView(layout);
 		
 		//setup some buttons
 		builder.setPositiveButton("Awesome", new DialogInterface.OnClickListener() {
 		  @Override
 		  public void onClick(DialogInterface dialog, int which) {
 		    dialog.dismiss();
 		  }
 		});
 		builder.setNegativeButton("Radical", new DialogInterface.OnClickListener() {
 		  @Override
 		  public void onClick(DialogInterface dialog, int which) {
 		    dialog.dismiss();
 		  }
 		});
 		//put it all together
 		AlertDialog alert = builder.create();
 		//display it
 		alert.show();	
 	}
 	
 	public void showInterfaceHelpers(){
 		/* This method only exists in this project
 		 * to move around display objects interactively
 		 * in development.
 		 */
 		
 		
 		
 		tryBut.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				float scale = mContext.getResources().getDisplayMetrics().density;
 				CharSequence s = et.getText();
 				String[] dim = s.toString().split(",");
 				int[] dimi = {0,0,0,0,0,0,0,0};
 				for(int i =0; i < dim.length; i++){
 					int a = (int)((float)Integer.parseInt(dim[i]) * scale);
 					dimi[i] = a;
 				}
 				//TextView a = (TextView) this.findViewById(R.id.hr24);
 				clock.setPadding( dimi[0], dimi[1], dimi[2], dimi[3] );
 				seconds.setPadding( dimi[4], dimi[5], dimi[6], dimi[7] );
 				
 			}
 		});
 	}
 	
 	
 };
