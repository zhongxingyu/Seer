 package com.marakana.yamba;
 
 import java.util.List;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 public class UpdaterService extends Service {
 	static final String TAG = "UpdaterService";
 	private boolean runFlag = false;
 	static final int DELAY = 60000; // 1 min
 	private Updater updater;
 	private YambaApplication yamba;
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		this.updater = new Updater();
 		this.yamba = (YambaApplication) getApplication();
 		Log.d(TAG, "UpdaterService onCreate");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		this.runFlag = true;
 		this.updater.start();
 		this.yamba.setServiceRunning(true);
 		
 		Log.d(TAG, "UpdaterService onStartCommand");				
 		return START_STICKY;
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		this.runFlag = false;
 		this.updater.interrupt();
 		this.updater = null;
 		this.yamba.setServiceRunning(false);
 		
 		Log.d(TAG, "UpdaterService onDestroy");		
 	}	
 	
 	/**
 	 * Thread is getting the timeline updates
 	 */
 	private class Updater extends Thread
 	{
 		List<Twitter.Status> timeline;
 		
 		public Updater() {
 			super("UpdaterService-Updater");
 		}
 
 		@Override
 		public void run() {
 			UpdaterService updaterService = UpdaterService.this;
 			
 			while (updaterService.runFlag)
 			{
 				try {
 					//get timeline from server
 					try{
 						timeline = yamba.getTwitter().getFriendsTimeline();
						
						//loop in the timeline and print the messages
						if ( timeline!=null )
						{
							for (Twitter.Status status: timeline  )
							{
								Log.d(TAG,"Status: " + status.user.name + " - " +  status.text);
							}
						}						
 					}
 					catch (TwitterException e) {
 						Log.d(TAG, "Error on getting the timeline: ", e);
 					}
 					
 					Log.d(TAG, "UpdaterService, updater ran.");		
 					Thread.sleep(DELAY);
 				} catch (InterruptedException e) {
 					updaterService.runFlag = false;
 				}
 				
 			}
 		}
 	}
 
 }
