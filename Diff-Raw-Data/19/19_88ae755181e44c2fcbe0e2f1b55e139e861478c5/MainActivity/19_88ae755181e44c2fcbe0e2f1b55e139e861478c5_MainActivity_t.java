 package com.example.solarpanelmanager;
 
 import net.minidev.json.JSONObject;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.example.bluetooth.Callback;
 import com.example.bluetooth.SnapshotHandler;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// BUTTON: Connect to existing device
 		final Button buttonConnect = (Button) findViewById(R.id.button_connect_device);
 		buttonConnect.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivity(i);
 			}
 		});
 
 		// Add a new device
 		final Button buttonAddDevice = (Button) findViewById(R.id.button_add_device);
 		buttonAddDevice.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		// Settings
 		final Button buttonSettings = (Button) findViewById(R.id.button_settings);
 		buttonSettings.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		// About
 		final Button buttonAbout = (Button) findViewById(R.id.button_about);
 		buttonAbout.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		// Do a basic call to the device for testing purposes.
 		SnapshotHandler call = new SnapshotHandler(new Callback() {
 
 			@Override
 			public void onComplete(JSONObject json) {
 				System.out.println("---Finished---");
 				Toast.makeText(MainActivity.this, json.toJSONString(), Toast.LENGTH_LONG).show();
 			}
 		});
 		call.performAction();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
