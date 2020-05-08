 package com.squirrel.sync;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 import com.squirrel.domain.Reminder;
 import com.squirrel.location.GeofenceCreator;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 class ReminderSyncerTask extends AsyncTask<Void, Void, Void>
 {
     private final Context context;
     private final ReminderApiService apiService;
     private final ReminderJsonMapper jsonMapper;
     private final ReminderStore reminderStore;
 
     public ReminderSyncerTask(Context context)
     {
         this.context = context;
         this.apiService = new ReminderApiService(context);
         this.jsonMapper = new ReminderJsonMapper();
         this.reminderStore = new ReminderStore(context);
     }
 
     @Override
     protected Void doInBackground(Void... voids)
     {
         try
         {
            ArrayList<Reminder> reminders = new ArrayList<Reminder>();

             JSONArray jArray = this.apiService.getRemindersJson();
 
             for(int i=0; i< jArray.length(); i++)
             {
                 JSONObject reminderJson = jArray.getJSONObject(i);
 
                 Reminder reminder = this.jsonMapper.CreateReminderFromJson(reminderJson);
 
                 reminders.add(reminder);
             }
 
             this.reminderStore.putReminders(reminders);
             new GeofenceCreator(context).CreateFromReminders(reminders);
         }
        catch (Exception e)
         {
             e.printStackTrace();
         }
 
         return null;
     }
 
     protected void onProgressUpdate() {
     }
 
     protected void onPostExecute() {
     }
 }
