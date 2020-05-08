 package com.example.smarttouchassistant;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.NavUtils;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.example.smarttouchassistant.BluetoothConnectionService.LocalBinder;
 
 public class NumpadActivity extends Activity {
 	private static final String NUMPAD_HEADER = "numpad#";
     BluetoothConnectionService btService;
     boolean isBound = false;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_numpad);
 		Intent intent = new Intent(this, BluetoothConnectionService.class);
         bindService(intent, myConnection, Context.BIND_AUTO_CREATE);   
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		LocalBroadcastManager.getInstance(this).registerReceiver(
 	            mMessageReceiver, new IntentFilter("foregroundSwitch"));
 	}
 	//Bind service to BT connection	
 	private ServiceConnection myConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 	        LocalBinder binder = (LocalBinder) service;
 	        btService = binder.getService();
 	        isBound = true;
 	    }
 	    
 	    public void onServiceDisconnected(ComponentName arg0) {
 	        isBound = false;
 	    }    	    
 	};
 	
 	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
 	    @Override
 	    public void onReceive(Context context, Intent intent) {
 	        String newForeground = intent.getStringExtra("foreground");
 	        Log.d("DEBUG", this.toString() + " received foreground switch request to " + newForeground);
 	        if(!newForeground.equals("numpad")) {
 	        	Intent next = null;
 		        if(newForeground.equals("mouse")) {
 		        	next = new Intent(context, MouseActivity.class);
 		        } else if(newForeground.equals("controller")) {
 		        	next = new Intent(context, ControllerActivity.class);
 		        } else if(newForeground.equals("macro")) {
 		        	next = new Intent(context, MacroActivity.class);
 		        } else if(newForeground.equals("multimedia")) {
 		        	next = new Intent(context, MultiMediaActivity.class);
 		        }		        
 		        startActivity(next); 
 		        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
 	        }	        
 	    }
 	};
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 	
 	public void inputNumber(View view) {
 		String message = null;
 		
     	switch (view.getId()) {
 	    	case R.id.tap0: 
 	    		message = "0";
 	    		Log.d("DEBUG", "0 tapped");
 	    		break;
 	    	case R.id.tap1: 
 	    		message = "1";
 	    		Log.d("DEBUG", "1 tapped");
 	    		break;
 	    	case R.id.tap2:
 	    		message = "2";
 	    		Log.d("DEBUG", "2 tapped");
 				break;
 	    	case R.id.tap3: 
 	    		message = "3";
 	    		Log.d("DEBUG", "3 tapped");
 				break;
 	    	case R.id.tap4: 
 	    		message = "4";
 	    		Log.d("DEBUG", "4 tapped");
 				break;
 	    	case R.id.tap5: 
 	    		message = "5";
 	    		Log.d("DEBUG", "5 tapped");
 				break;
 	    	case R.id.tap6: 
 	    		message = "6";
 	    		Log.d("DEBUG", "6 tapped");
 	    		break;
 	    	case R.id.tap7:
 	    		message = "7";
 	    		Log.d("DEBUG", "7 tapped");
 				break;
 	    	case R.id.tap8: 
 	    		message = "8";
 	    		Log.d("DEBUG", "8 tapped");
 				break;
 	    	case R.id.tap9: 
 	    		message = "9";
 	    		Log.d("DEBUG", "9 tapped");
 				break;
 	    	case R.id.tapAdd: 
 	    		message = "+";
 	    		Log.d("DEBUG", "+ tapped");
 				break;
 	    	case R.id.tapMinus: 
 	    		message = "-";
 	    		Log.d("DEBUG", "- tapped");
 				break;
 	    	case R.id.tapMultiply: 
 	    		message = "*";
 	    		Log.d("DEBUG", "* tapped");
 				break;
 	    	case R.id.tapDivide: 
 	    		message = "/";
 	    		Log.d("DEBUG", "/ tapped");
 				break;
 	    	case R.id.tapDot: 
 	    		message = ".";
 	    		Log.d("DEBUG", ". tapped");
 				break;
	    	case R.id.tapClear: 
	    		message = "esc";
	    		Log.d("DEBUG", "CLEAR tapped");
 				break;
     	}	
     	btService.sendMessage(NUMPAD_HEADER + message);		 	
     }
 	
 	@Override
 	public void onPause() {
 	    super.onPause();  // Always call the superclass method first
 
 	    Log.d("DEBUG", "Numpad paused");
 	}
 	
 	@Override
 	public void onResume() {
 	    super.onResume();  // Always call the superclass method first
 
 	    Log.d("DEBUG", "Numpad resumed");
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.numpad, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
