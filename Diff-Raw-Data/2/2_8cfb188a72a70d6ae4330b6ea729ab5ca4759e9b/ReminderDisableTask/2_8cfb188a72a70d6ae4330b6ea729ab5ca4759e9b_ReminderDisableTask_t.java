 package com.squirrel.sync;
 
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 
 import com.squirrel.domain.Reminder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ReminderDisableTask extends AsyncTask<Intent, Integer, Void>
 {
     private Context context;
     private NotificationManager notificationManager;
     private ReminderRepository reminderRepository;
 
     public ReminderDisableTask(Context context)
     {
         this.context = context;
         this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         this.reminderRepository = new ReminderRepository(this.context);
     }
 
     @Override
     protected Void doInBackground(Intent... intents)
     {
         int reminderIdToDisable = Integer.parseInt(intents[0].getDataString().substring(5));
 
         try
         {
             List<Reminder> reminders = this.reminderRepository.getReminders();
 
             this.disableReminderWithId(reminders, reminderIdToDisable);
 
             this.reminderRepository.putReminders(reminders);

            new ReminderSyncerTask(context).execute();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
         notificationManager.cancel(reminderIdToDisable);
 
         return null;
     }
 
     private void disableReminderWithId(List<Reminder> reminders, int reminderId)
     {
         for(Reminder reminder : reminders)
         {
             if(reminder.id == reminderId)
             {
                 reminder.enabled = false;
             }
         }
     }
 }
