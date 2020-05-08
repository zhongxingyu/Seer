package com.example.lejosbluetooth; 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	public final static String TAG = "APP MSG";
 	private Button buttonStart = null;
 	private Button buttonTerminate = null;
 
 	private CustomView drawView=null;
 	
 	
	 
 	TextView textView = null;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 	buttonStart = (Button) findViewById(R.id.buttonStart);
 		buttonTerminate = (Button) findViewById(R.id.buttonTerminateLejos);
 		drawView = (CustomView)findViewById(R.id.customView);
 		 textView = (TextView) findViewById(R.id.textView);
 		
 		//initializes sound infrastructure
 		SoundPlayer.singleton().init(getApplicationContext());
 		
 
 		buttonStart.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				
 				int action = event.getAction();
 				switch(action)
 				{
 				case  MotionEvent.ACTION_DOWN:
 					Controller.singleton().startSweep();
 					return true;
 				case  MotionEvent.ACTION_UP:
 					Controller.singleton().stopSweep();
 					return true;
 				
 				}
 				return false;
 			}
 		});
 		
	
 		
 		buttonTerminate.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Controller.singleton().terminateServer();
 				
 			}
 		});
 		
 		
 		Controller.singleton().setDrawView(this.drawView);
 		Controller.singleton().setTextView(this.textView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		int id = item.getItemId();
 		
 		switch(id)
 		{
 		 case R.id.action_connect:
 			 item.setEnabled(false);
  			BluetoothAsyncTask.singleton().execute(textView);
 			return true;
 		
 		
 		}
 		
 		
 		return false;
 	}
 
 }
