 package com.example.siteGuardian;
 
 import android.app.Application;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.BaseColumns;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: a.spodarenko
  * Date: 2/13/13
  * Time: 1:02 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CheckSiteService extends Service {
 
     private String url;
     private Integer delayInSeconds;
     private Timer timer;
     private SQLiteDatabase db;
     private Application app;
 
     public String URL_KEY;
     public String TIMEOUT_KEY;
     private SharedPreferences pref;
     private Random random;
     private static final Integer DEFAULT_TIMEOUT = 3;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         random = new Random();
         app = getApplication();
         URL_KEY = getString(R.string.url_key);
         TIMEOUT_KEY = getString(R.string.time_to_refresh_key);
         pref = PreferenceManager.getDefaultSharedPreferences(this);
         url = pref.getString(URL_KEY,null);
         String tmpDelay = pref.getString(TIMEOUT_KEY, null);
         if(tmpDelay !=null)
              delayInSeconds = Integer.valueOf(tmpDelay);
         else
             delayInSeconds = DEFAULT_TIMEOUT;
         TimerTask timerTask = new SiteChecker();
         timer = new Timer();
         timer.schedule(timerTask, 0, delayInSeconds * 1000);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         timer.cancel();
     }
 
     private class SiteChecker extends TimerTask {
 
         @Override
         public void run() {
             ContentValues values = new ContentValues();
             values.put(SiteGuardSQLHelper.TIMESTAMP_COLUMN,new Date().getTime());
             values.put(BaseColumns._ID, random.nextInt());
             if(is_online(url)){
                values.put(SiteGuardSQLHelper.RESULT_STATUS_COLUMN,"OK");
             }  else {
                 values.put(SiteGuardSQLHelper.RESULT_STATUS_COLUMN,"DOWN");
             }
             SharedObjectManager.getInstance().getDb(app).insertOrThrow(SiteGuardSQLHelper.STATUS_TABLE_NAME,null,values);
         }
 
             public Boolean is_online(String url) {
                if(url == null){
                    return false;
                }
                 ConnectivityManager cm = (ConnectivityManager)
                         getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo networkInfo = cm.getActiveNetworkInfo();
 
                 if (networkInfo == null || !networkInfo.isConnected()) {
                     return false;
                 }
 
 
                 HttpClient client = new DefaultHttpClient();
                 HttpGet request = new HttpGet(url);
                 HttpResponse response;
                 try {
                     response = client.execute(request);
                 } catch (ClientProtocolException e) {
                     return false;
                 } catch (IOException e) {
                     return false;
                 }
                 int stat_code = response.getStatusLine().getStatusCode();
                 if(stat_code != 200)
                     return false;
                 else
                     return true;
             }
 
     }
 }
