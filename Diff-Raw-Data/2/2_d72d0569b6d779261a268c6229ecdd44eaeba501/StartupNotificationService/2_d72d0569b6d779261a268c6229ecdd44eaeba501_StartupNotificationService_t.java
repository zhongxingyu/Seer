 package com.timetracker.service;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.timetracker.domain.persistance.DatabaseHelper;
 import com.timetracker.ui.TaskService;
 
 /**
  * @author Anton Chernetskij
  */
 public class StartupNotificationService extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.i(StartupNotificationService.class.getSimpleName(), "Adding task notification");
         DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
         TaskService taskService = new TaskService(context, helper, null);
         taskService.showCurrentTaskNotification();

        MailReportService.scheduleReport(context);
     }
 }
