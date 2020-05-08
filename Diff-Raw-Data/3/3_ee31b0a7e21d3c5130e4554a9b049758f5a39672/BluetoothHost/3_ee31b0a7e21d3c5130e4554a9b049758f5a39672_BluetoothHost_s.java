 package com.example.insync;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import android.os.Bundle;
 import android.net.Uri;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.ContentValues;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 
 public class BluetoothHost extends Activity {
 	private File fp;
 	private BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
 	private Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_bluetooth_host);
 
 		Bundle extras = getIntent().getExtras();
 		fp = new File(extras.getString("filepath"));
 
 		final TextView uriTV = (TextView) findViewById(R.id.uriDisplayTV);
 		uriTV.setText("Your selected song: " + fp);
 
 		final Button sendbutton = (Button) findViewById(R.id.sendfilebutton);
 		sendbutton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				sendFile();
 			}
 		});
 
 		listConnectedDevices();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.bluetooth_host, menu);
 		return true;
 	}
 
 	public void listConnectedDevices(){
 		List<String> s = new ArrayList<String>();
 		for(BluetoothDevice bt : pairedDevices){
 			s.add(bt.getAddress());
 		}
 
 		final TextView btDevAddTV = (TextView) findViewById(R.id.connectedBTdevTV);
 		btDevAddTV.append("\n"+s.toString());
 	}
 
 	public void sendFile(){
 		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
 		
 		if (btAdapter == null) {
 			// Device does not support Bluetooth
 			// Inform user that we're done. 
 				return;
 			}
 		
 		// bring up Android chooser
 		Intent intent = new Intent();
 		intent.setAction(Intent.ACTION_SEND);
 		intent.setType("text/plain");
 		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fp) );
		//...
		startActivity(intent);	
 		
 		//list of apps that can handle our intent
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> appsList = pm.queryIntentActivities( intent, 0);
 
 		if(appsList.size() > 0) {
 			// proceed
 			//select bluetooth
 			String packageName = null;
 			String className = null;
 			
 			for(ResolveInfo info: appsList){
 			  packageName = info.activityInfo.packageName;
 			  if( packageName.equals("com.android.bluetooth")){
 			     className = info.activityInfo.name;
 			     break;// found
 			  }
 			}
 			
 			//set our intent to launch Bluetooth
 			intent.setClassName(packageName, className);
 			startActivity(intent);
 		}
 		
 		
 		
 		/*
 		for(BluetoothDevice bt : pairedDevices){
 			ContentValues values = new ContentValues();
 			values.put(BluetoothShare.URI, "content://" + fp);
 
 			//Send Bluetooth stuff for each Address
 			values.put(BluetoothShare.DESTINATION, bt.getAddress());
 			values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
 			Long ts = System.currentTimeMillis();
 			values.put(BluetoothShare.TIMESTAMP, ts);
 				
 		}
 		 */
 
 	}
 
 }
