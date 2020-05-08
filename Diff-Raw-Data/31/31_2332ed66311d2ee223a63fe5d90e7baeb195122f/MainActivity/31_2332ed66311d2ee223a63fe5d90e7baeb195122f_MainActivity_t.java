 package com.example.tracker;
 
 import java.util.ArrayList;
 import Client.TestClient;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.TextView;
 
 public class MainActivity extends Activity implements View.OnClickListener{
 
 	private String TAG = this.getClass().getSimpleName();
 	private Intent trackerService = null;
 	private Button startButton = null;
 	private Button stopButton = null;
	private TextView status = null;
 	
 	/** Flag to track the service status */
 	private boolean serviceStatus = false;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	Log.i(TAG, "onCreat() is called"); 
         super.onCreate(savedInstanceState);
         
         /** Create the Intent for trackerService */
         trackerService = new Intent(this, TrackerService.class);
         
         setContentView(R.layout.activity_main);
         
         /** Bind the button listener here */
         startButton = (Button)findViewById(R.id.startbutton);
         stopButton = (Button)findViewById(R.id.stopbutton);
        status = (TextView)findViewById(R.id.status);
         startButton.setOnClickListener(this);
         stopButton.setOnClickListener(this);
        status.setText("Stopped!");
    
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		/** Ensure stop the trackerService */
 		stopService(trackerService);
 		super.onDestroy();
 	}
 
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch(arg0.getId()) {
 			case R.id.startbutton:
 				Log.i(TAG, "startbutton clicked.");
 				/** Start the trackerService */
 				if(serviceStatus == false) {
 					startService(trackerService);
 					serviceStatus = true;
 				}
				status.setText("Started!");
 				
 //				ArrayList<String> list = new ArrayList<String>();
 //				list.add("bc6ef7035ddb4763");
 //				list.add("aaaaaaaa");
 //				String ipAddress = "10.0.2.2";
 //				new TestClient(ipAddress, list);
 				
 				break;
 			case R.id.stopbutton:
 				Log.i(TAG, "stopbuttion clicked.");
 				/** Stop the trackerService */
 				if(serviceStatus == true) {
 					stopService(trackerService);
 					serviceStatus = false;
 				}
				status.setText("Stopped!");
 				break;
 			default:
 					break;
 		}	
 	}
 	
 	private boolean isOnline() {
 	    ConnectivityManager cm =
 	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    if (netInfo != null && netInfo.isConnected()) {
 	        return true;
 	    }
 	    return false;
 	}
 }
