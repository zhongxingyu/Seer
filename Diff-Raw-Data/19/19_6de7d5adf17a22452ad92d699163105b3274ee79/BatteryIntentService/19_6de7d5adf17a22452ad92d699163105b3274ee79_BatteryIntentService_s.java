 package com.example.wsn03;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class BatteryIntentService extends IntentService {
 	private String THIS = "BatteryIntenService";
 
 	// ctor
 	public BatteryIntentService(){
 		super( "BatteryIntentService" );
 		Log.d( MainActivity.TAG, THIS + "::ctor");
 	}
 			
 	// at least for debugging commonunication
 	private Messenger messenger;
 
 	// the battery Intent listener
 	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context context, Intent intent){
 			Log.d(MainActivity.TAG, THIS + "::BroadcastReceiver::onReceive()" );
 			
 			// TODO check, what is this?
 			context.unregisterReceiver( this );
 			
 //			int status = intent.getIntExtra( BatteryManager.EXTRA_STATUS, -1);
 //			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
 					
 			// is plugged
 			int chargePlug = intent.getIntExtra( BatteryManager.EXTRA_PLUGGED, -1);
 			
 			// charging via USB
 			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
 			
 			// charging via AC
 			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
 
 			// TODO figure out
 //			boolean wifiCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;
 
 			
 			// evaluation
 			
 			boolean isCharging = usbCharge || acCharge;
 			
 					
 			int rawLevel = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, -1);
 			int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE, -1);
 			int level = -1;
 			if( (0 <= rawLevel) && (0 < scale) ){
 				level = (rawLevel * 100) / scale; 
 			}
 
 			Message msg = Message.obtain();
 
 			Bundle data = new Bundle();
 			data.putString("result", String.valueOf( level ));
 			msg.setData( data );
 				 	
 			try{
 				messenger.send( msg );
 			}catch( RemoteException re ){
 				re.printStackTrace();
 			}
 
 			// write level to the database
 			MainActivity.DB.batterySave( level, isCharging );
 		}
 	};
 //*/
 	
 	private Timer timer = new Timer();
 	
 	// on Intent receive, a Messenger and BroadcastReceiver will be generated
 	// to parse the intent's battery information
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d( MainActivity.TAG, THIS + "::onHandleIntent()" );
 		
 		// apply the generated filter to finde battery flags
		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver( batteryLevelReceiver, batteryLevelFilter );
 		
 		messenger = (Messenger) intent.getExtras().get( "handler_battery" );
 		timer.schedule( new TimerTask(){
 			@Override
 			public void run(){
				Message msg = Message.obtain();
				Bundle data = new Bundle();
 /* TODO: clean communication paths
 //				data.putString("result_battery", String.valueOf( System.currentTimeMillis() )); // XXX
 				data.putString("result_battery", String.valueOf( level )); // XXX
 				msg.setData( data );
 				try{
 					messenger.send(msg);
 				}catch(RemoteException re){
 					re.printStackTrace();
 				}
 //*/
 			}
 		}, 100, 3000);		
 
 	}
 };
 
