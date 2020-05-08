 package com.villainrom.otaupdater.activity;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.villainrom.otaupdater.R;
 
 public class TitleActivity extends Activity {
	private static final String TAG = TitleActivity.class.getSimpleName();
 
 	public static final int NOTIFY_SERVICE_ID = 1;
 	public static final int NOTIFY_CHECK_FAILED_ID = 2;
 
 	private final BroadcastReceiver showReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(android.content.Context context, Intent intent) {
 		}
 	};
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button check = (Button) findViewById(R.id.Check);
         check.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.i(TAG, "User forced check.");
 				sendBroadcast(new Intent("com.villainrom.otaupdater.CHECK"));
 			}
         });
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	
     	unregisterReceiver(showReceiver);
     }
 }
