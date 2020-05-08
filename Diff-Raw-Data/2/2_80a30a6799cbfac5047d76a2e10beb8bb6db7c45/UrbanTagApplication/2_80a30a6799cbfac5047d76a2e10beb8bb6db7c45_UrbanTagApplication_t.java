 package com.ubikod.urbantag;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.ubikod.capptain.android.sdk.CapptainApplication;
 import com.ubikod.urbantag.model.DatabaseHelper;
 import com.ubikod.urbantag.model.Tag;
 import com.ubikod.urbantag.model.TagManager;
 
 public class UrbanTagApplication extends CapptainApplication
 {
   private static final String ACTION_FETCH_TAGS_LIST = "tag/get/all";
 
   @Override
   protected void onApplicationProcessCreate()
   {
     Log.i(UrbanTag.TAG, "Launching App !");
     NotificationHelper notifHelper = new NotificationHelper(this);
     notifHelper.notifyAppliRunning();
 
     // re initiate wifi message display
     SharedPreferences pref = getApplicationContext().getSharedPreferences("URBAN_TAG_PREF",
       Context.MODE_PRIVATE);
     pref.edit().putBoolean("notifiedWifi", false).commit();
 
     AsyncTask<Void, Void, List<Tag>> updateTagList = new AsyncTask<Void, Void, List<Tag>>()
     {
 
       @Override
       protected List<Tag> doInBackground(Void... v)
       {
         List<Tag> res = new ArrayList<Tag>();
         HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet(UrbanTag.API_URL + ACTION_FETCH_TAGS_LIST);
        Log.i(UrbanTag.TAG, "Fetching tags list on : " + request.getURI().toString());
         try
         {
           HttpResponse response = client.execute(request);
           BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
             .getContent()));
 
           String textResponse = "";
           String line = "";
           while ((line = rd.readLine()) != null)
           {
             textResponse += line;
           }
           Log.i(UrbanTag.TAG, "Received :" + textResponse);
           JSONArray jsonTagsArray = new JSONArray(textResponse);
 
           for (int i = 0; i < jsonTagsArray.length(); i++)
           {
             Tag t = TagManager.createTag(jsonTagsArray.getJSONObject(i));
             if (t != null)
             {
               res.add(t);
             }
           }
         }
         catch (ClientProtocolException cpe)
         {
           Log.e(UrbanTag.TAG, cpe.getMessage());
         }
         catch (IOException ioe)
         {
           Log.e(UrbanTag.TAG, ioe.getMessage());
         }
         catch (JSONException je)
         {
           Log.e(UrbanTag.TAG, je.getMessage());
         }
 
         return res;
       }
 
       @Override
       protected void onPostExecute(List<Tag> list)
       {
         TagManager tagManager = new TagManager(new DatabaseHelper(getApplicationContext(), null));
         if (list.size() > 0)
           tagManager.update(list);
       }
     };
 
     updateTagList.execute();
   }
 }
