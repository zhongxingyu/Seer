 package com.samteladze.delta.statistics;
 
 import java.util.Date;
 
 import android.content.Context;
 import android.content.Intent;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 import com.samteladze.delta.statistics.datamodel.StatisticsFormat;
 import com.samteladze.delta.statistics.utils.FileManager;
 import com.samteladze.delta.statistics.utils.LogManager;
 
 public class NetStatisticsService extends WakefulIntentService
 {	
 	public NetStatisticsService()
 	{
 		super("NetStatisticsService");
 	}
 
 	@Override
 	protected void doWakefulWork(Intent intent)
 	{		
 		Context context = this.getApplicationContext();
 		
 		LogManager.Log(NetStatisticsService.class.getSimpleName(), "Starting the service");
 		
 		NetStatisticsProvider netStatProvider = new NetStatisticsProvider(context);
 		netStatProvider.CollectStatistics(new Date());
 		
		FileManager.SaveStatistics(netStatProvider.GetStatistics(StatisticsFormat.Machine), FileManager.NET_STAT_FILE_PATH, false);
		FileManager.SaveStatistics(netStatProvider.GetStatistics(StatisticsFormat.UserFriendly), FileManager.USER_NET_STAT_FILE_PATH, false);
 	}
 }
