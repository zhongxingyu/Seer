 package com.example.insync;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import android.os.Bundle;
 import android.bluetooth.*;
 import android.content.Intent;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class CreateSession extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_session);
 		enableBluetooth();
 		
 		final Button fCButton = (Button) findViewById(R.id.chooseFileButton);
 		
 		fCButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 	             intent.setType("file/*");
 	             final int PICKFILE_RESULT_CODE = 1;
 	             startActivityForResult(intent,PICKFILE_RESULT_CODE);
 			}
 		});
 	}
 	
 	 @Override
 	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	  // TODO Auto-generated method stub
 		 final int PICKFILE_RESULT_CODE = 1;
 	  switch(requestCode){
 	  case PICKFILE_RESULT_CODE:
 	   if(resultCode==RESULT_OK){
 	    String FilePath = data.getData().getPath();
 	    final TextView textFile = (TextView) findViewById(R.id.fileNameTextView); 
 	    textFile.setText("MP3 File Selected: " + FilePath);
 	   }
 	   break;
 	   
 	  }
 	 }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_session, menu);
 		return true;
 	}
 
 	public void enableBluetooth(){
 		BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
 
 		//Enable Bluetooth
 		if(!bA.isEnabled()){
 			bA.enable();
 			final TextView btCheck = (TextView) findViewById(R.id.bluetoothCheck);
 			btCheck.setText("Turning Bluetooth on to detect other Android devices");		
 		}
 
 		//List all paired Bluetooth Devices
 		Set<BluetoothDevice> pairedDevices = bA.getBondedDevices();
 		List<String> s = new ArrayList<String>();
 		for(BluetoothDevice bt : pairedDevices){
 			s.add(bt.getName());
 		}
 
 		//Update TextView with list of Bluetooth Devices
 		final TextView btDevTV = (TextView) findViewById(R.id.bluetoothTV);
 		btDevTV.append("\n"+s.toString());
 
 	}
 
 }
