 package com.ucb.dcm.data;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import com.ucb.dcm.MainActivity;
 import com.ucb.dcm.net.ExecuteURLDownload;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 /**
  * Created by kurtguenther on 6/6/13.
  */
 public class DataService {
     private static final String TAG = "DataService";
 
     ////////
     // Static accessor
     ////////
 
     private static DataService mSharedService;
     public static final String JSON_URL = "http://www.delclosemarathon.com/dcm15/schedules/viewjson";
 
     public static DataService getSharedService()
     {
         return mSharedService;
     }
 
     public Context context;
 
     public static void Initialize(Context context)
     {
         DataService api = new DataService();
         api.context = context;
         mSharedService = api;
     }
 
     //TODO need logic for when to update
     public boolean shouldUpdate(){
         return getVenues().size() == 0;
     }
 
 //    public boolean refreshData(){
 //        Venue.deleteAll();
 //        Show.deleteAll();
 //    }
 
     ////////
     // Downloading the data
     ////////
 
     private class UpdateServerListener implements ExecuteURLDownload.ExecuteURLDownloadListener{
 
         ProgressDialog dialog;
 
         public UpdateServerListener(ProgressDialog dialog){
             this.dialog = dialog;
         }
 
         @Override
         public void onSuccess(JSONObject result) {
             Log.v(TAG,"Schedule download complete.  Beginning processing.");
 
             dialog.setMessage("Processing Venues.");
             AsyncTask<JSONObject, Integer, String> dbUpdate = new AsyncTask<JSONObject, Integer, String>() {
                 @Override
                 protected String doInBackground(JSONObject... params) {
                     JSONObject js = params[0];
 
                     Activity aaa = (Activity) context;
 
                     aaa.runOnUiThread(new Runnable() {
                         public void run() {
                             dialog.setMessage("Processing Venues.");
                         }
                     });
 
                     //dialog.setMessage("Processing Venues.");
                     processVenues(js);
 
                     aaa.runOnUiThread(new Runnable() {
                         public void run() {
                             dialog.setMessage("Processing Shows.");
                         }
                     });
 
                     //dialog.setMessage("Processing Shows.");
                     processShows(js);
                     aaa.runOnUiThread(new Runnable() {
                         public void run() {
                             dialog.setMessage("Processing Schedules.");
                         }
                     });
                     //dialog.setMessage("Processing Schedules.");
                     processSchedules(js);
                     aaa.runOnUiThread(new Runnable() {
                         public void run() {
                             dialog.hide();
                         }
                     });
 
                     aaa.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             ((MainActivity)context).onScheduleDownloaded();
                         }
                     });
 
                     return null;
                 }
 
             };
 
             dbUpdate.execute(result);
 
         }
 
         @Override
         public void onError(JSONObject result) {
             Toast t = Toast.makeText(context, "Error downloading the schedule", Toast.LENGTH_LONG);
             t.show();
         }
     }
 
     public void updateFromServer(ExecuteURLDownload.ExecuteURLDownloadListener listener){
         try {
             Log.v(TAG,"Requesting schedule from server: " + JSON_URL);
             HttpURLConnection jsonFile = (HttpURLConnection) new URL(JSON_URL).openConnection();
 
             ProgressDialog dialog = ProgressDialog.show(this.context, "Updating the schedule", "Fetching from the server", true);
 
            new ExecuteURLDownload(new UpdateServerListener(dialog)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void processVenues(JSONObject results){
         try{
             JSONArray shows = results.getJSONArray("Venues");
             for(int i = 0; i < shows.length(); i++){
                 JSONObject jsonVenue = shows.getJSONObject(i).getJSONObject("Venue");
                 Venue venue = Venue.fromJson(jsonVenue);
                 venue.insert(DBHelper.getSharedService().getWritableDatabase());
             }
 
         }
         catch(JSONException je){
             je.printStackTrace();
         }
     }
 
     public void processSchedules(JSONObject results){
         SQLiteDatabase db = DBHelper.getSharedService().getWritableDatabase();
         try{
             JSONArray shows = results.getJSONArray("Schedules");
 
             db.beginTransaction();
             for(int i = 0; i < shows.length(); i++){
                 JSONObject jsonPerf = shows.getJSONObject(i).getJSONObject("Schedule");
                 Performance perf = Performance.fromJson(jsonPerf);
                 perf.insert(db);
             }
             db.setTransactionSuccessful();
         }
         catch(JSONException je){
             je.printStackTrace();
         }
         finally {
             db.endTransaction();
         }
     }
 
     public void processShows(JSONObject results){
         SQLiteDatabase db = DBHelper.getSharedService().getWritableDatabase();
         try{
             db.beginTransaction();
             JSONArray shows = results.getJSONArray("Shows");
             for(int i = 0; i < shows.length(); i++){
                 JSONObject jsonShow = shows.getJSONObject(i).getJSONObject("Show");
                 Show show = Show.fromJson(jsonShow);
                 show.insert(DBHelper.getSharedService().getWritableDatabase());
             }
             db.setTransactionSuccessful();
         }
         catch(JSONException je){
             je.printStackTrace();
         }
         finally {
             db.endTransaction();
         }
     }
 
     ////////
     // Get methods
     ////////
 
     public ArrayList<Show> getShows(String filterString){
         //TODO filtering
         return Show.getAll(DBHelper.getSharedService().getWritableDatabase(), "sort_name");
     }
 
     public ArrayList<Show> getFavorites(){
         Show s = new Show();
         ArrayList<Show> retVal = new ArrayList<Show>();
         Cursor c = DBHelper.getSharedService().getWritableDatabase().query(s.getTableName(), s.getColumnNames(), "favorite = 1", null, null, null, "sort_name");
 
         while(c.moveToNext())
         {
             retVal.add(new Show(c));
         }
 
         return retVal;
     }
 
     public ArrayList<Venue> getVenues(){
         return Venue.getAll(DBHelper.getSharedService().getWritableDatabase(), "id");
     }
 
 }
