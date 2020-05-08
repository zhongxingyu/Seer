 package edu.berkeley.eecs.ruzenafit.activity;
 
 
 //import edu.berkeley.sph.ehs.calfitd.ICalFitdService;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import edu.berkeley.eecs.ruzenafit.R;
 import edu.berkeley.eecs.ruzenafit.service.WorkoutTrackerService;
import edu.berkeley.eecs.ruzenafit.util.Constants;
 
 
 public class WorkoutTrackerActivity extends Activity {
 	private static Context mContext;
     //The primary interface we will be calling on the service.
 //	ICalFitdService mService = null;
 	// notification variables
 	public static NotificationManager notificationManager;
 	public static final int NOTIFICATION_ID = 1;	
 	
 	public double mTotal_kCal = 0.0; 
 	static private int MENU_ABOUT = 1;
 	TextView pSetting;
 	
 	// TODO: Change this to be a list of two items: "Resume Data Logging",
 	// and "TURN OFF Data Logging".
 	private final CharSequence[] turnoffITEMS = {
 			"resume data logging0", 
 			"resume data logging1", 
 			"resume data logging2",
 			"resume data logging3",
 			"resume data logging4",
 			"resume data logging5",
 			"resume data logging6",
 			"resume data logging7",
 			"resume data logging8",
 			"resume data logging9",
 			"TURN OFF data logging"};
 	private static int whichitem = 0;
 	private static int turnoff_log = 10;
 	private ToggleButton tbutton;	
 	
 //	private static int okoff = 0;
 	
 	
 	// TODO: This button should do some basic sanity-checking
 	// to make sure that your facebook email is set, and that your
 	// privacy setting is set.
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.old_main);
 
         mContext = this;
 //		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
         // using startService allows the service to survive this activity
 //    	Toast.makeText(getApplicationContext(), "Starting service...", Toast.LENGTH_SHORT).show();                		
 		startService(new Intent(mContext, WorkoutTrackerService.class));
 		Log.i(getClass().getSimpleName(), "startService!!!");
 
         // set status of the toggle 
         final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.togglebutton);
         tbutton = togglebutton;
         if (WorkoutTrackerService.getStatus()==1)
             togglebutton.setChecked(true);
         else 
         	togglebutton.setChecked(false);
 
         // TODO: Add a forewarning when clicking the main button:
         // "WARNING:  kCal measurements WILL NOT BE ACCURATE if you do not have this phone
         // strapped to your waist or in your pocket."
         togglebutton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // Perform action on clicks
                 if (togglebutton.isChecked() && (WorkoutTrackerService.getStatus()==0)) {			// turn on logging, don't need to do this if service status is already running
 	            	Toast.makeText(getApplicationContext(), "Starting datalog...", Toast.LENGTH_SHORT).show();
 	            	WorkoutTrackerService.startLog();
 	    	        Log.i(getClass().getSimpleName(), "startlog!!!");                	
 //                	NotificationOn();
                 } else if ((togglebutton.isChecked() == false) && (WorkoutTrackerService.getStatus()==1)) {	// turn off logging, don't need to do this if service status is not running
                 	// ask which action from list
     				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     				builder.setTitle("Really stop?");
     				builder.setCancelable(false);
     				builder.setItems(turnoffITEMS, new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog, int item) {
     						whichitem = item;
 //							okoff = 0;
     						if (whichitem != turnoff_log) {
     							// cancel... leave button checked...
     							tbutton.setChecked(true);
     						} else {
     							// turn off logging
 //    							okoff = 1;
     		   	            	Toast.makeText(getApplicationContext(), "Stopping datalog...", Toast.LENGTH_SHORT).show();
     		   	            	WorkoutTrackerService.stoplog();
     		                    Log.i(getClass().getSimpleName(), "stoplog!!!");                        	
 //    		                    NotificationOff();
     		                    tbutton.setChecked(false);
     						}
     					}
     				});
     				
     				AlertDialog alert = builder.create();
     				alert.show();
 
 /*    				// need this in case user hits "back" key to get out of the dialog
     				if (okoff == 1)
     		        	togglebutton.setChecked(false);
     				else 
     		        	togglebutton.setChecked(true);
 */    		        	
                 }
             }
         });
         
         final Button button4 = (Button) findViewById(R.id.helpbutton);
         button4.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
            		// start the About view
     			startActivity(new Intent(mContext, AboutActivity.class));
             }
         });
 
 		// get IMEI to uniquely identify users' posts to the server
 		TelephonyManager mTelephonyMgr =   
 				(TelephonyManager)getSystemService(TELEPHONY_SERVICE);    
 		String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE  			
 		if (TextUtils.isEmpty(imei)) {
 			imei = "0";
 		}
 		((TextView) findViewById(R.id.imei)).setText(imei);
     }
 
 
     
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.i(getClass().getSimpleName(), "on stop!!!");		
 	}
 
 	
 	@Override
 	protected void onDestroy() {
 		// if not logging data, stop server upon exiting CalFit
 		if (WorkoutTrackerService.getStatus()==0) {
 			stopService(new Intent(mContext, WorkoutTrackerService.class));
 			Log.i(getClass().getSimpleName(), "stopService!!!");
 		}		
 		super.onDestroy();
 		Log.i(getClass().getSimpleName(), "on destoy!!!");		
 	}
 	
 
 	/* Creates the menu items */
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    menu.add(0, MENU_ABOUT, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
 	    return true;
 	}
 
 	/* Handles item selections */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		 if (item.getItemId() == MENU_ABOUT) {
        		// start the About view
 			startActivity(new Intent(this, AboutActivity.class));
 	        return true;
 		} else {
 	    	return false;
 	    }
 	}
 
 
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 
 		// set status of the toggle 
         final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.togglebutton);
         tbutton = togglebutton;
         if (WorkoutTrackerService.getStatus()==1)
             togglebutton.setChecked(true);
         else 
         	togglebutton.setChecked(false);
 	}
 
 
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		pSetting = (TextView) findViewById(R.id.pset);
 		
		SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAMESPACE, 0);
		String p = preferences.getString(Constants.PRIVACY_SETTING, "Privacy not set");
		
		if(p.equals("lowPrivacy"))
 		pSetting.setText("Low Privacy");
		else
		if(p.equals("mediumPrivacy"))
 		pSetting.setText("Medium Privacy");
		else
		if(p.equals("highPrivacy"))
 		pSetting.setText("High Privacy");
		else
			pSetting.setText("FIZZAILED");
 		// set status of the toggle 
         final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.togglebutton);
         tbutton = togglebutton;
         if (WorkoutTrackerService.getStatus()==1)
             togglebutton.setChecked(true);
         else 
         	togglebutton.setChecked(false);
 	}
 
 	
 	
 }
 
 
