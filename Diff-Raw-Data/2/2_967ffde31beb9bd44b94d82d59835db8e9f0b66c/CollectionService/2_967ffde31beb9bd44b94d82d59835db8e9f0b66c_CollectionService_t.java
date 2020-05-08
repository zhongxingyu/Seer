 package com.quackware.tric.service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.quackware.tric.MyApplication;
 import com.quackware.tric.stats.Stats;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 public class CollectionService extends Service {
 
 	private Handler mHandler;
 	private ArrayList<ArgRunnable> mRunnableList;
 	
 	private final IBinder mBinder = new CollectionBinder();
 	
 	private static String[] TRICS = {
 		"AppStats.NumberOfDownloadedAppsInstalled",
 		"AppStats.NumberOfTotalAppsInstalled",
 		"AppStats.NumberOfTotalAppsUninstalled",
 		"AppStats.NumberOfAppsRunning",
 		"PhoneStats.TotalPhoneRam",
 		"PhoneStats.TotalPhoneCpu",
 		"PhoneStats.TotalPhoneUptime",
 		"PhoneStats.TotalPhoneUptimeNoSleep",
 		"SocialStats.NumberOfFacebookFriends",
 		"SocialStats.NumberOfFacebookWallPosts",
 		"TrafficStats.NumberOfMobileMegabytesReceived",
		"TrafficStats.NumberOfMobileMegabytesTransmitted"
 	};
 
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		mHandler = new Handler();
 		mRunnableList = new ArrayList<ArgRunnable>();
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 	
 	@Override
 	public boolean onUnbind(Intent intent)
 	{
 		return false;
 	}
 	
 	public class CollectionBinder extends Binder
 	{
 		public CollectionService getService()
 		{
 			return CollectionService.this;
 		}
 	}
 	
 	public void cancelStatsCollection(ArrayList<Stats> pStats)
 	{
 		//Iterator for safe deletion while iterating through list.
 		Iterator<ArgRunnable> it = mRunnableList.iterator();
 		
 		for(Stats s : pStats)
 		{
 			while(it.hasNext())
 			{
 				ArgRunnable a = it.next();
 				if(a.mStats.equals(s))
 				{
 					mHandler.removeCallbacks(a);
 					it.remove();
 				}
 			}
 		}
 	}
 	
 	public void refreshStatsInfo(Stats pStats)
 	{
 		ArrayList<Stats> statsArray = new ArrayList<Stats>();
 		statsArray.add(pStats);
 		cancelStatsCollection(statsArray);
 		launch(statsArray);
 	}
 	
 	public void beginCollection()
 	{
 		ArrayList<Stats> statsToRun = new ArrayList<Stats>();
 		for(int i = 0; i < TRICS.length; i++) {
 			try 
 			{
 				Class<?> c = Class.forName("com.quackware.tric.stats." + TRICS[i]);
 				Stats stat = (Stats)c.newInstance();
 				if (stat.getNeedsContext())
 				{
 					stat.setContext(this);
 				}
 				statsToRun.add(stat);	
 			}
 			catch (Exception ex)
 			{
 				
 			}
 		}
 		launch(statsToRun);
 		MyApplication.addStats(statsToRun);
 	}
 	
 	private void launch(ArrayList<Stats> pStats)
 	{
 		//Add check to see if we should actually launch based on preferences for collecting.
 		for(Stats s : pStats)
 		{
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
 			if(prefs.getBoolean("checkbox_collect_" + s.getName(), true))
 			{
 				ArgRunnable a = new ArgRunnable(s);
 				mHandler.post(a);
 				mRunnableList.add(a);
 			}
 		}
 	}
 	
 	class ArgRunnable implements Runnable
 	{
 		private Stats mStats;
 		public ArgRunnable(Stats pStats)
 		{
 			mStats = pStats;
 		}
 		public void run() 
 		{
 			if(mStats.getType().equals("FacebookStats") && MyApplication.getFacebook() != null)
 			{
 				MyApplication.getFacebook().extendAccessTokenIfNeeded(MyApplication.getInstance(), null);
 				if(!MyApplication.getFacebook().isSessionValid())
 				{
 					Toast.makeText(getApplicationContext(), "Not logged into Facebook, not collecting info",Toast.LENGTH_LONG).show();
 					mHandler.postDelayed(this,mStats.getDefaultCollectionInterval()*1000*60);
 					return;
 				}
 			}
 			if(!mStats.refreshStats())
 			{
 				//Not asynchronous, we can go ahead and insert now.
 				MyApplication.getDatabaseHelper().insertNewStat(mStats);
 			}
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
 			int collectionInterval = Integer.parseInt(prefs.getString("edittext_collectinterval_" + mStats.getName(), "" + mStats.getDefaultCollectionInterval()));
 			mHandler.postDelayed(this,collectionInterval*1000*60);
 		}
 		
 	}
 }
