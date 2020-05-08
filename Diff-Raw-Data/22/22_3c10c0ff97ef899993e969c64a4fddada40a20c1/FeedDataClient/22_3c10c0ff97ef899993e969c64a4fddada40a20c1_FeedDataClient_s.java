 package com.chalmers.feedlr.services;
 
 import com.chalmers.feedlr.services.FeedDataService.TwitterServiceBinder;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 
 public class FeedDataClient {
 	
 	private Context context;
 	
 	private FeedDataService feedService;
 
 	private boolean isBound;
 
 	public FeedDataClient(Context context) {
 		this.context = context;
 		isBound = false;		
 	}
 	
 	public void startService() {
 		Intent intent = new Intent(context, FeedDataService.class);
 		context.startService(intent);
 	}
 	public void stopService() {
 		Intent intent = new Intent(context, FeedDataService.class);
 		context.stopService(intent);
 	}

 	public void bindService() {
 		Intent intent = new Intent(context, FeedDataService.class);
 		isBound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
 	}

 	public void unbindService() {
 		if(isBound) {
 			context.unbindService(connection);
 			isBound = false;
 		}
 	}
 	
	public void update() {
		if(!isBound) return;
		feedService.updateData();
	}
	
 	private ServiceConnection connection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			TwitterServiceBinder binder = (TwitterServiceBinder) service;
 	        feedService = binder.getService();
 	        isBound = true;
 		}

 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			feedService = null;
 		}
 	};
 }
