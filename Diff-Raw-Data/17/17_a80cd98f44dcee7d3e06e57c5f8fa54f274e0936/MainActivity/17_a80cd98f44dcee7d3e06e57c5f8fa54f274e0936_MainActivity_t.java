 package edu.kettering.blackbox;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Intent;
 import android.view.Menu;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private static final int REQUEST_ENABLE_BT = 1;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        TextView textView = (TextView)findViewById(R.id.textView1);
         // set up bluetooth
         BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBluetoothAdapter == null) {
        	//TODO Device does not support bluetooth        	
         	textView.setText("Bluetooth not supported");
         	return;
         }
         // make sure bluetooth is enabled, otherwise ask user to enable it
        try {
	        if (!mBluetoothAdapter.isEnabled()) {
	        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	        }
        } catch (Exception e) {
        	textView.setText(e.toString());
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch (requestCode) {
     	case REQUEST_ENABLE_BT:
     		if (resultCode == RESULT_OK) {
     			//TODO catch bt error, or user did not enable bt
     			Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
     		}
     		break;
     	default:
     		
     	}
     }
     
 }
