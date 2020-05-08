 
 /* TODO
  * 1. Make notifications a separate class?????
  */
 
 package com.samteladze.delta.statistics;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.IPackageStatsObserver;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageStats;
 import android.os.RemoteException;
 import android.provider.Settings.Secure;
 import android.widget.RemoteViews;
 
 import com.samteladze.delta.statistics.datamodel.*;
 import com.samteladze.delta.statistics.utils.*;
 
 public class AppStatisticsProvider
 {
 	private static final int START_NOTIFICATION_ID = 1;
 	public static final int FINISH_NOTIFICATION_ID = 2;
 	
 	private NotificationManager _mgrNotification;
 	private Notification _progressNotification;
 	
 	private Context _context;
 	private ArrayList<AppStatistics> _statistics;
 	private String _deviceID;
 	
 	public AppStatisticsProvider(Context context)
 	{
 		_context = context;
 		_statistics = new ArrayList<AppStatistics>();
 		_deviceID = Constants.StatisticsNone;
 		_mgrNotification = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
 		_progressNotification = null;
 	}
 		
 	public void CollectStatistics()
 	{
 		// Get PackageManager and list of all the installed applications
 		final PackageManager packageManager = _context.getPackageManager();
         List<ApplicationInfo> installedApplications = 
         		packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
         
         final Semaphore countCodeSizeSemaphore = new Semaphore(1, true);
         
         int progress = 0;
         int maxProgress = installedApplications.size();
         ConstructProgressNotification(maxProgress);
               
         for (ApplicationInfo appInfo : installedApplications)
         {        	
         	if ((progress % 20) == 1)
         	{
         		UpdateProgressNotification(progress, installedApplications.size());
         	}
         	
         	if (!IsSystemApp(appInfo) || IsUpdatedSystemApp(appInfo))
         	{
         		try
 				{
 					countCodeSizeSemaphore.acquire();
 				} 
         		catch (InterruptedException e)
 				{
 					e.printStackTrace(System.err);
 				}
         		
         		final AppStatistics AppStatistics = new AppStatistics();
         		
         		AppStatistics.packageName = appInfo.packageName;    		
         		AppStatistics.appName = appInfo.loadLabel(packageManager).toString();
         		       		
         		try
         		{       		
 	        		Method getPackageSizeInfo = packageManager.getClass().getMethod(
 	    		            "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
 	    	        	
 	        		getPackageSizeInfo.invoke(packageManager, appInfo.packageName,
 	    		            new IPackageStatsObserver.Stub() 
 	    		        	{    		        	    
 	    		        		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
 	    		                    throws RemoteException 
 	    	                    {
 	    		        			AppStatistics.codeSize = pStats.codeSize;
 	    		        			countCodeSizeSemaphore.release();
 	    		                }
 	    		            }); 
         		}
         		catch (Exception e)
         		{
         			e.printStackTrace(System.err);
         			
         			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
         		}
         		
         		try 
         		{
         			PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);					
 					
         			AppStatistics.versionName = packageInfo.versionName;	        		
         			AppStatistics.versionCode = packageInfo.versionCode;
         			AppStatistics.firstIntallTime = packageInfo.firstInstallTime;
         			AppStatistics.lastUpdateTime = packageInfo.lastUpdateTime;
 				} 
         		catch (Exception e) 
         		{
         			e.printStackTrace(System.err);
         			
         			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 				}   
         		
         		_statistics.add(AppStatistics);
         	}        	
         	
         	++progress;
         } 
         
         _deviceID = Secure.getString(_context.getContentResolver(), Secure.ANDROID_ID);	
         
         UpdateProgressNotification(progress, maxProgress);
         CancelNotification(START_NOTIFICATION_ID);
         CreateFinishNotification();
         
         LogManager.Log(AppStatisticsProvider.class.getSimpleName(), "Statistics was collected");
 	}
 	
 	public String GetStatistics(StatisticsFormat format)
 	{
 		String statisticsStr = "";
 		
 		statisticsStr += GetDeviceID(format);
 		statisticsStr += GetNumberOfApps(format);
 		
 		for (AppStatistics appStatistics : _statistics)
 		{
 			statisticsStr += appStatistics.Format(format);
 		}
 		
 		if (format == StatisticsFormat.Machine)
 		{
 			statisticsStr += Constants.LayoutEndSection;
 		}
 		
 		return statisticsStr;
 	}
 	
 	private String GetNumberOfApps(StatisticsFormat format)
 	{
 		String generatedStr = "";
 		
 		if (format == StatisticsFormat.UserFriendly)
 		{
 			generatedStr += Constants.StatisticsNumberOfAppsText + _statistics.size() + Constants.LayoutNextLine;
 			generatedStr += Constants.LayoutSeparator;
 		}
 		else if (format == StatisticsFormat.Machine)
 		{
 			generatedStr += _statistics.size() + Constants.LayoutNextLine;
 			generatedStr += Constants.LayoutEndSection;
 		}
 		
 		return generatedStr;
 	}
 	
 	private String GetDeviceID(StatisticsFormat format)
 	{
 		String generatedStr = "";
 		
 		if (format == StatisticsFormat.UserFriendly)
 		{
 			generatedStr += Constants.StatisticsDeviceIDText + _deviceID + Constants.LayoutNextLine;
 			generatedStr += Constants.LayoutSeparator;
 		}
 		else if (format == StatisticsFormat.Machine)
 		{
 			generatedStr += _deviceID + Constants.LayoutNextLine;
 			generatedStr += Constants.LayoutEndSection;
 		}
 		
 		return generatedStr;
 	}
 	
 	private boolean IsSystemApp(ApplicationInfo appInfo) 
 	{
 	    return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);	    
 	}
 	
 	private boolean IsUpdatedSystemApp(ApplicationInfo appInfo) 
 	{
 	    return ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);	    
 	}
 	
 	public String DeviceID()
 	{
 		if (_deviceID.equals(Constants.StatisticsNone))
 		{
 			 _deviceID = Secure.getString(_context.getContentResolver(), Secure.ANDROID_ID);
 		}
 		
 		return _deviceID;
 	}
 
 	private void ConstructProgressNotification(int maxProgress)
 	{
 		// Configure the intent
         Intent intent = new Intent();
         final PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, intent, 0);
         
         // Create notification
         _progressNotification = new Notification(R.drawable.icon_border, Constants.NotificationProgressText, System.currentTimeMillis());
         _progressNotification.flags |= Notification.FLAG_ONGOING_EVENT;
         _progressNotification.flags |= Notification.FLAG_NO_CLEAR;
         _progressNotification.contentView = new RemoteViews(_context.getPackageName(), R.layout.collection_progress);
         _progressNotification.contentIntent = pendingIntent;
         _progressNotification.contentView.setImageViewResource(R.id.collection_progress_status_icon, R.drawable.icon_border);
         _progressNotification.contentView.setTextViewText(R.id.collection_progress_status_text, Constants.NotificationProgressText);
         _progressNotification.contentView.setProgressBar(R.id.collection_progress_status_progress, maxProgress, 0, false);
         
         _mgrNotification.notify(START_NOTIFICATION_ID, _progressNotification);
 	}
 	
 	private void UpdateProgressNotification(int progress, int maxProgress)
 	{
 		if (_progressNotification == null)
 		{
 			LogManager.Log(AppStatisticsProvider.class.getSimpleName(), "ERROR! Could not update notification progress");
 			return;
 		}
 		
 		_progressNotification.contentView.setProgressBar(R.id.collection_progress_status_progress, maxProgress, progress, false);
 		_mgrNotification.notify(START_NOTIFICATION_ID, _progressNotification);
 	}
 	
 	private void CreateFinishNotification()
 	{
 		// Configure the intents
         Intent contentIntent = new Intent(_context, DeltaStatisticsActivity.class);
         final PendingIntent contentPendingIntent = PendingIntent.getActivity(_context, 0, contentIntent, 0);
         Intent deleteIntent = new Intent(_context, OnNotificationDeleteReceiver.class);
         final PendingIntent deletePendingIntent = PendingIntent.getBroadcast(_context, 0, deleteIntent, 0);
         
         // Create notification
         Notification fisnishNotification = new Notification(R.drawable.icon_border, Constants.NotificationFinishText, System.currentTimeMillis());
        fisnishNotification.flags = _progressNotification.flags | Notification.FLAG_AUTO_CANCEL;
         fisnishNotification.contentView = new RemoteViews(_context.getPackageName(), R.layout.collection_finished);
         fisnishNotification.contentIntent = contentPendingIntent;
         fisnishNotification.deleteIntent = deletePendingIntent;
         fisnishNotification.contentView.setImageViewResource(R.id.collection_finished_status_icon, R.drawable.icon_border);
         fisnishNotification.contentView.setTextViewText(R.id.collection_finished_status_text, Constants.NotificationFinishText);
         fisnishNotification.contentView.setTextViewText(R.id.collection_finished_header_text, Constants.NotificationAppNameText);
         
         _mgrNotification.notify(FINISH_NOTIFICATION_ID, fisnishNotification);
 	}
 	
 	private void CancelNotification(int notificationID)
 	{
 		_mgrNotification.cancel(notificationID);
 	}
 }
