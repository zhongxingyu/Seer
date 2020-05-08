 package com.emcewen.ultimateplexremote;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.app.Activity;
 import android.content.Intent;
 
 public class ManualActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_manual);
 	}
 	
 	@Override
 	protected void onDestroy() {
 	  // Unregister since the activity is about to be closed.
 	  super.onDestroy();
 	}
 	
     @Override
     protected void onPause() {
         // TODO Auto-generated method stub
         super.onPause();
     }
     @Override
     protected void onResume() {
         // TODO Auto-generated method stub
         super.onResume();
         //Setup the rescan button listener
         Button rescanBtn = (Button) findViewById(R.id.btnRescan);
         rescanBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	rescanButton(v);
             }
         });
         
         //Setup the connect button listener
         Button connectBtn = (Button) findViewById(R.id.btnConnect);
        connectBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	connectButton(v);
             }
         });
         
     }
 
 	protected void connectButton(View v) {
 		
 	}
 
 	protected void rescanButton(View v) {
 		Intent entryIntent = new Intent(this,EntryActivity.class);
 		finish();
 		startActivity(entryIntent);
 		overridePendingTransition(0,0);
 	}
 }
