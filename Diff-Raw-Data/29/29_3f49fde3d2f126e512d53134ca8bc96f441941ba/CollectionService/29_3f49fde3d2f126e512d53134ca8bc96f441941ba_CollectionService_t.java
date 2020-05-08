 package com.quackware.tric.service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.quackware.tric.MyApplication;
 import com.quackware.tric.database.DatabaseHelper;
 import com.quackware.tric.stats.Stats;
 import com.quackware.tric.stats.PhoneStats.*;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 
 public class CollectionService extends Service {
 
 	private Handler mHandler;
 	private DatabaseHelper mDatabaseHelper;
 	
 	private ArrayList<ArgRunnable> mRunnableList;
 	
 	private final IBinder mBinder = new CollectionBinder();
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		mHandler = new Handler();
 		mDatabaseHelper = new DatabaseHelper(this);
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
 	
 	public void cancelStatsCollection(Stats...pStats)
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
 	
 	public void refreshStatsInfo(Stats...pStats)
 	{
 		cancelStatsCollection(pStats);
 		launch(pStats);
 	}
 	
 	public void beginCollection()
 	{
 		TotalPhoneUptime tpu = new TotalPhoneUptime();
 		TotalPhoneUptimeNoSleep tpuns = new TotalPhoneUptimeNoSleep();
 		launch(tpu,tpuns);
 		MyApplication.addStats(tpu,tpuns);
 	}
 	
 	private void launch(Stats...pStats)
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
 			mStats.refreshStats();
 			mDatabaseHelper.insertNewStat(mStats);
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
			int collectionInterval = Integer.parseInt(prefs.getString("edittext_collectinterval_" + mStats.getName(), "" + mStats.getDefaultCollectionInterval()));
 			mHandler.postDelayed(this,collectionInterval*1000*60);
 		}
 		
 	}
 }
