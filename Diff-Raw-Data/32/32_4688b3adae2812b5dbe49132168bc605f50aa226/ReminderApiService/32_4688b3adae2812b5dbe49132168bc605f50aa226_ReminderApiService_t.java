 package com.squirrel.sync;
 
 import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
 
 import com.squirrel.auth.IdTokenStore;
 import com.squirrel.domain.Reminder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ReminderApiService
 {
    private static String baseUri = "http://%ssquirrel-beta.com/api/remindermobile/";
     private JsonArrayBuilder arrayBuilder;
     private IdTokenStore tokenStore;
     private HttpClient httpClient;
     private ReminderJsonMapper jsonMapper;
 
     public ReminderApiService(Context context)
     {
         this.arrayBuilder = new JsonArrayBuilder();
         this.tokenStore = new IdTokenStore(context);
         this.httpClient = new DefaultHttpClient();
         this.jsonMapper = new ReminderJsonMapper();
 
        setUri(context);

     }
 
     public ArrayList<Reminder> syncReminders(List<Reminder> localReminders) throws IOException, JSONException
     {
         ArrayList<Reminder> updatedReminders = new ArrayList<Reminder>();
 
         String idToken = this.tokenStore.getToken();
 
         if(idToken != null)
         {
             HttpPost post = new HttpPost(baseUri);
 
             post.setHeader("idToken", idToken);
 
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
             String disabledReminderIds = this.buildDisabledRemindersJsonString(localReminders);
 
             post.setEntity(new StringEntity(disabledReminderIds));
             post.setHeader("Accept", "application/json");
             post.setHeader("Content-type", "application/json");
 
             HttpResponse response = this.httpClient.execute(post);
 
             JSONArray array = this.arrayBuilder.build(response);
 
             updatedReminders = this.jsonMapper.createRemindersFromJson(array);
         }
 
         return updatedReminders;
     }
 
     private String buildDisabledRemindersJsonString(List<Reminder> reminders)
     {
         List<Integer> disabledReminderIds = new ArrayList<Integer>();
 
         for(Reminder reminder : reminders)
         {
             if(!reminder.enabled)
             {
                 disabledReminderIds.add(reminder.id);
             }
         }
 
         String json = new JSONArray(disabledReminderIds).toString();
 
         return json;
     }

    private void setUri(Context context)
    {
        PackageInfo packageInfo = null;
        try
        {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if ((packageInfo != null && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0))
        {
            this.baseUri = String.format(this.baseUri, "staging.");
        }
        else
        {
            this.baseUri = String.format(this.baseUri, "");
        }
    }
 }
