 package com.example.intentservice04;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.IntentService;
import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class HiddenService extends IntentService {
 	// communcation to MainActivity
 	private Messenger messenger;
 
 	// obtaining some data
 	private Timer timer = new Timer();
 
 	public HiddenService(){
 		super("HiddenService");
 		Log.i(MainActivity.TAG, "HiddenService::ctor");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent){
 		Log.i(MainActivity.TAG, "HiddenService::onHandleIntent()");
 		// access the Intent
 		messenger = (Messenger) intent.getExtras().get("handler");
 
 		timer.schedule( new TimerTask(){
 
 			@Override
 			public void run() {
 				// call handler every 3 secs
 				Message msg = Message.obtain();
 
 				Bundle data = new Bundle();
				data.putString("result", "timestamp: " + System.currentTimeMillis() );
 				msg.setData( data );
 				
 				try{
 					messenger.send( msg );
 				}catch( RemoteException re ){
 					re.printStackTrace();
 				}
 			}
 			
 		}, 100, 3000);
 	}
	
	/*
	 * declare the IntentService STICKY
	 * (non-Javadoc)
	 * @see android.app.IntentService#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i(MainActivity.TAG, "HiddenService::onStartCommand()");
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}
 }
