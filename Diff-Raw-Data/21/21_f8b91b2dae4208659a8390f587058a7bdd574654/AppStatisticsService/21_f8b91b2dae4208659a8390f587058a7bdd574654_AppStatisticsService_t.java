 package com.samteladze.delta.statistics;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 import com.samteladze.delta.statistics.datamodel.StatisticsFormat;
 import com.samteladze.delta.statistics.utils.CommunicationManager;
 import com.samteladze.delta.statistics.utils.FileManager;
 import com.samteladze.delta.statistics.utils.LogManager;
 
 public class AppStatisticsService extends WakefulIntentService
 {
 	// Unique request code for PendingIntent (used to distinguish intents)
 	private static final int INTENT_REQUEST_CODE = 1;
 	
 	public AppStatisticsService()
 	{
 		super("AppStatisticsService");
 	}
 
 	@Override
 	protected void doWakefulWork(Intent intent)
 	{
 		Context context = this.getApplicationContext();
 		
 		LogManager.Log(AppStatisticsService.class.getSimpleName(), "Starting the service");
 		
         AppStatisticsProvider appStatisticsProvider = new AppStatisticsProvider(context);
         appStatisticsProvider.CollectStatistics(); 
         
        FileManager.SaveStatistics(appStatisticsProvider.GetStatistics(StatisticsFormat.UserFriendly), FileManager.APP_STAT_FILE_PATH, true);
         
         // Send statistics to server. Set another alarm if statistics was not sent
         if (!CommunicationManager.SendAppStatisticsToServer())
         {
         	// Get AlarmManager
      		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      		
      		// Create an Intent to start OnAlarmReceiver
      		Intent alarmIntent = new Intent(context, OnAlarmReceiver.class);
      		// Create a PendingIntent from alarmIntent with a unique request code
      		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, INTENT_REQUEST_CODE, alarmIntent, 0);
      		
      		// Set one time alarm that will invoke OnAlarmReceiver
      		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
 						  	 SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
 						  	 alarmPendingIntent); 
      		
      		LogManager.Log(AppStatisticsService.class.getSimpleName(), "Single alarm was scheduled");
         }
         else
         {
     		LogManager.Log(AppStatisticsService.class.getSimpleName(), "Attempting to clean log");
     		FileManager.CleanFile(LogManager.LOG_FILE_PATH);
         }
 	}
 }
