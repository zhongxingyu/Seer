 package com.github.grimpy.botifier;
 
 
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.service.notification.NotificationListenerService;
 import android.service.notification.StatusBarNotification;
 import android.util.Log;
 
 
 
 @TargetApi(18)
 public class BotifierNotificationService extends NotificationListenerService implements NotificationInterface{
 	private static String TAG = "Botifier";
 	private BotifierManager mBotifyManager;
 	private Handler mHandler;
 
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.i(TAG, "Manager started");
 		mBotifyManager = new BotifierManager(this);
 	    mHandler = new Handler(){
 	    	public void handleMessage(Message msg){
 	    		String cmd = BotifierManager.CMD_NOTIFICATION_ADDED;
 	    		if (msg.arg1 == 1) {
 	    			cmd = BotifierManager.CMD_NOTIFICATION_REMOVED;
 	    		}
 	    		StatusBarNotification stn = (StatusBarNotification)msg.obj;
	    		Intent i = new Intent(cmd);
	    		String description = stn.getNotification().tickerText.toString();
	    		String text = Botification.extractTextFromNotification(BotifierNotificationService.this, stn.getNotification());
 	    		Botification bot = new Botification(stn.getId(), stn.getPackageName(), stn.getTag(), description, text);
 	    		i.putExtra("botification", bot);
 	    		sendBroadcast(i);
 	    		//Looper.myLooper().quit();
 		    }       
 		};
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mBotifyManager.destroy();
 	}
 	
 	public void sendCmd(final StatusBarNotification stn, final String cmd){
 		mHandler.sendEmptyMessage(0);
 		Looper.loop();
 		Looper.myLooper().quitSafely();
 	}
 
 	
 	@Override
 	public void onNotificationPosted(StatusBarNotification statusnotification) {
 		Log.i(TAG, "new notification received");
 		Message msg = new Message();
 		msg.obj = statusnotification;
 		msg.arg1 = 0;
 		mHandler.sendMessage(msg);
 	}
 
 	@Override
 	public void onNotificationRemoved(StatusBarNotification statusnotification) {
 		Log.d(TAG, "Cleaning up notifications");
 		Message msg = new Message();
 		msg.obj = statusnotification;
 		msg.arg1 = 1;
 		mHandler.sendMessage(msg);
 	}
 	
 	public void cancelNotification(Botification not){
 		cancelNotification(not.mPkg, not.mTag, not.mId);
 	}
 	
 	
 }
