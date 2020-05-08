 package com.vitaltech.bioink;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import rajawali.RajawaliActivity;
 
 // user interface with master branch
 
 public class MainActivity extends RajawaliActivity {
 	private static final String TAG=MainActivity.class.getSimpleName();
 
 	public static final Boolean DEBUG=true;
 
 	private Button vizButton;
 	private Discovery discovery;
 	private BroadcastReceiver broadcastReceiver;
 	private IntentFilter intentFilter;
 	private BluetoothAdapter btAdapter;
 	private Boolean vizActive;
 	
 //	private kailean bluetooth		// FIXME
 //	private mario dataprocessing	// FIXME
 	private Scene scene;
 
 	// **** Start Lifecycle ****
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         if(DEBUG) Log.d(TAG, "__onCreate()__");
 
 		btAdapter=BluetoothAdapter.getDefaultAdapter();
 		if(btAdapter==null){
 			Toast.makeText(getApplicationContext(),"Bluetooth not available on this device",Toast.LENGTH_LONG).show();
 			Log.e(TAG, "Bluetooth not available on this device");
 			finish();
 		}
 
 		discovery=new Discovery(this, btAdapter);
         new Thread(new Runnable() { 
             public void run(){
             	if(DEBUG) Log.d(TAG,"start finding devices");
             	discovery.findDevices(btAdapter);
             }
         }).start();
 
 		vizActive = false;
         // INSTANTIATE VIZ SCENE
         scene = new Scene(this,1000);
 		scene.initScene();
 		scene.setSurfaceView(mSurfaceView);
 		super.setRenderer(scene);
 		// END VIZ SCENE
 
 		// TODO INSTANTIATE DATA PROCESSING
 		// FIXME dataprocessing(scene)
 		// END DATA PROCESSING
 
 		// TODO INSTANTIATE BLUETOOTH
 		// FIXME bluetooth(dataprocessing)
 		// END BLUETOOTH
 
 		// Catch Bluetooth radio events
 		intentFilter=new IntentFilter();
 		intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
 		broadcastReceiver=new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context contxt, Intent intent) {
 				if(DEBUG) Log.d(TAG, "broadcast received");
 				switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)){
 				case BluetoothAdapter.STATE_ON:
 					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => on");
 					changeRadioStatus("on");
 					vizButton.setText("Start Visualization");
 					break;
 				case BluetoothAdapter.STATE_OFF:
 					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => off");
 					changeRadioStatus("off");
 					vizButton.setText("Enable Bluetooth");
 					break;
 				case BluetoothAdapter.STATE_TURNING_OFF:
 				case BluetoothAdapter.STATE_TURNING_ON:
 				default:
 					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => changing");
 					changeRadioStatus("changing");
 					break;
 				}
 			}
 		};
 		connectButton();
     }
     
     private void connectButton(){
 		// Configure single Button system
 		this.vizButton=(Button)this.findViewById(R.id.vizButton);
         this.vizButton.setOnClickListener(
         	new OnClickListener() {
 				public void onClick(View v) {
 			        if(DEBUG) Log.d(TAG, "Viz Button pressed");
 			        if(vizButton.getText()=="Enable Bluetooth"){
 			            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
 			        }else{
 			            new Thread(new Runnable() { 
 			                public void run(){
 			                	if(DEBUG) Log.d(TAG,"start Bluetooth thread");
 					            // TODO Start bluetooth active thread here.
 			                }
 			            }).start(); // bluetooth
 
 			            new Thread(new Runnable() { 
 			                public void run(){
 			                	if(DEBUG) Log.d(TAG,"start data thread");
 						        // TODO Start data processing active thread here.
 			                }
 			            }).start(); // data processing
 
 //			            new Thread(new Runnable() { 
 //			                public void run(){
 //			                	if(DEBUG) Log.d(TAG,"start data thread");
 						            if (DEBUG) Log.d(TAG,"start viz");
 						            setContentView(mLayout);
 						            vizActive = true;
 
 						            // start data feeding thread for testing
 						            new Thread(new Runnable() {
 						            	public void run() { 
 						            		generateData(); 
 						            	}
 						            }).start();// debug data
 //			                }
 //			            }).start(); // visualization
 			        }
 				}
 			}
 		);
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             if(DEBUG) Log.d(TAG, "back keycode received");
             if(vizActive){
                 if(DEBUG) Log.d(TAG, "turning off visualization");
                 setContentView(R.layout.activity_main);
                 vizActive = false;
                 connectButton();
                 return true;
             }else{
                 if(DEBUG) Log.d(TAG, "ending app");
                 finish();
             }
         }
         return false;
     }
 
     @Override
     protected void onResume() { // Activity was partially visible
         if(DEBUG) Log.d(TAG, "__onResume()__");
         registerReceiver(broadcastReceiver, intentFilter);
     	super.onResume();
     	if(vizActive){
             if(DEBUG) Log.d(TAG, "return to visualization");
             setContentView(mLayout);
         }else{
             if(DEBUG) Log.d(TAG, "return to menu");
             setContentView(R.layout.activity_main);
             connectButton();
             if(btAdapter.isEnabled()){
 				changeRadioStatus("on");
 				vizButton.setText("Start Visualization");
 	        }else{
 				changeRadioStatus("off");
 				vizButton.setText("Enable Bluetooth");
 			}
     	}
     }
 
     // **** Activity is running at this point ****
     
     @Override
     public void onPause(){ // Activity was visible but now is now partially visible
         if(DEBUG) Log.d(TAG, "__onPause()__");
         unregisterReceiver(broadcastReceiver);
     	super.onPause();
     	// pause bluetooth traffic
     	// stop data analysis
     	// stop screen visualization
     }
     
 //    public void onDestroy() { // Activity was hidden but is now being stopped altogether
 //    	if(DEBUG) Log.d(TAG, "__onDestroy()__");
 //    	super.onStop();
 //    }
     // **** End Lifecycle ****    
 
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        getMenuInflater().inflate(R.menu.activity_main, (android.view.Menu) menu);
 //        return true;
 //    }
 
     private void changeRadioStatus(String stat){
     	if(vizActive){
     		Log.e(TAG, "Cannot update radio status while in visualization");
     	}else{
     		((TextView)this.findViewById(R.id.radioTextView)).setText("Radio is "+stat);
     	}
     }
 
     private void changePairedStatus(Integer paired){
     	if(vizActive){
     		Log.e(TAG, "Cannot update paired status while in visualization");
     	}else{
     		((TextView)this.findViewById(R.id.pairedTextView)).setText("Devices paired: "+paired.toString());
     	}
     }
 
     private void changeAudibleStatus(Integer audible){
     	if(vizActive){
     		Log.e(TAG, "Cannot update audible status while in visualization");
     	}else{
     		((TextView)this.findViewById(R.id.audibleTextView)).setText("Devices audible: "+audible.toString());
     	}
     }
 
     	
     private void generateData(){
     	scene.update("user1", DataType.HEARTRATE, 50);
     	scene.update("user1", DataType.TEMP, 97);
     	try {
 			Thread.sleep(4000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	scene.update("user1", DataType.TEMP, 105);
     	scene.update("user1", DataType.HEARTRATE,120);
     }
 }
