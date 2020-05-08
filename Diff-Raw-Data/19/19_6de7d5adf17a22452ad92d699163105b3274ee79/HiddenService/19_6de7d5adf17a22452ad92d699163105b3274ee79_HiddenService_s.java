 package com.example.wsn03;
 
 import android.annotation.SuppressLint;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 @SuppressLint("HandlerLeak")
 public class HiddenService extends Service {
 	private String THIS = "HiddenService";
 	
 	// battery data
 	private Handler handler_battery = new Handler(){
 		
 // FIXME: communication paths - remove or use for debugging
 		@Override
 		public void handleMessage( Message msg ){
 			String result = msg.getData().getString("result_battery");
 			super.handleMessage(msg);
 		}
 	};
 	private Intent intent_battery;
 	
 	// wifi data
 // TODO
 
 	// 3g data
 // TODO
 	
 	// bluetooth data
 // TODO
 	
 	
 	@Override
 	public int onStartCommand( Intent intent, int flags, int startId ){
 		Log.d(MainActivity.TAG, this.THIS + "::onStartCommand()");
 		
 		
 // TODO timed start
 		// timed start of battery IntentService
 		intent_battery = new Intent( this, BatteryIntentService.class );
 		intent_battery.putExtra("handler_battery", new Messenger(this.handler_battery) );
 		this.startService( intent_battery );
 

 		// TODO start threads for wifi, 3g, bluetooth
 //		Thread thread_wifi = new Thread( runnable_wifi );
 //		thread_wifi.start();
 		
 		// TODO start threads for wifi, 3g, bluetooth
 //		Thread thread_wifi = new Thread( runnable_3g );
 //		thread_3g.start();
 
 		// TODO start threads for wifi, 3g, bluetooth
 //		Thread thread_wifi = new Thread( runnable_bluetooth );
 //		thread_bluetooth.start();
 
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		Log.d(MainActivity.TAG, this.THIS + "::onBind()");
 // TODO Auto-generated method stub
 		return null;
 	}
 };
