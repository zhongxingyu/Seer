 package com.quackware.tric;
 
 import java.util.ArrayList;
 
 import com.quackware.tric.service.CollectionService;
 import com.quackware.tric.service.CollectionService.CollectionBinder;
 import com.quackware.tric.stats.Stats;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 
 public class MyApplication extends Application
 {
 	private static MyApplication instance;
 	//Holds a list of Stats that have ever been instantiated
 	private static ArrayList<Stats> mGlobalStatsList = new ArrayList<Stats>();
 	
 	private static CollectionService mService;
 	private static boolean mBound = false;
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		instance = this;
 		startService();
 	}
 	
 	private void startService()
 	{
 		Intent intent = new Intent(this,CollectionService.class);
 		bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
		//startService(new Intent(this,CollectionService.class));
 	}
 	
 	public void stopService()
 	{
 		if(mBound)
 		{
 			unbindService(mConnection);
 			mBound = false;
 		}
 	}
 	
 	private ServiceConnection mConnection = new ServiceConnection()
 	{
 
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			CollectionBinder binder = (CollectionBinder)service;
 			mService = binder.getService();
 			mBound = true;
 			
 			mService.beginCollection();
 		}
 
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 			mService = null;
 		}
 		
 	};
 	
 	public static CollectionService getService()
 	{
 		return mService;
 	}
 	
 	public static MyApplication getInstance()
 	{
 		return instance;
 	}
 	
 	public static void addStats(Stats...pStats)
 	{
 		for(Stats s : pStats)
 		{
 			if(!mGlobalStatsList.contains(s))
 				mGlobalStatsList.add(s);
 		}
 	}
 	
 	public static ArrayList<Stats> getStats()
 	{
 		return mGlobalStatsList;
 	}
 	
 	public static Stats getStatsByName(String name)
 	{
 		for(int i = 0;i<mGlobalStatsList.size();i++)
 		{
 			if(mGlobalStatsList.get(i).getName().equals(name))
 				return mGlobalStatsList.get(i);
 		}
 		return null;
 	}
 
 }
