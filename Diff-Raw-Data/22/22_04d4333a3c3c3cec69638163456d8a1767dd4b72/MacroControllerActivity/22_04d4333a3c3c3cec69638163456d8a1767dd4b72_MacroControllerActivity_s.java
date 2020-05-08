 package com.example.smarttouchassistant;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
 
 import com.example.smarttouchassistant.BluetoothConnectionService.LocalBinder;
 
 public class MacroControllerActivity extends Activity {
 	private static final String MACRO_HEADER = "macro#";
 	BluetoothConnectionService btService;
     boolean isBound = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_macro_controller);
 		
 		Intent serviceIntent = new Intent(this, BluetoothConnectionService.class);
         bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE); 
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// Get the message from the intent
 		Intent intent = getIntent();
 		LinearLayout layout = (LinearLayout) findViewById(R.id.macroLayout);
 		
 		int buttonYPosition = 0;
 		for(String s: MacroActivity.SHORTCUTS) {
			final String message = intent.getStringExtra(s);
 			Button b = new Button(this);
 					
 			b.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					btService.sendMessage(MACRO_HEADER + message);
 				}
 
 
 			});
 
			b.setText(message);
 			b.setWidth(400);
 			b.setHeight(40);
 			b.setX(0);
 			b.setY(buttonYPosition);
 			layout.addView(b);
 			buttonYPosition += 50;
 		}
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
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.macro_controller, menu);
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
