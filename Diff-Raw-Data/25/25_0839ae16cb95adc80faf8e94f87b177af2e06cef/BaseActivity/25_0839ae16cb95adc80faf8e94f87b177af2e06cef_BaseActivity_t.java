 package com.MetroSub.activity;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.util.Log;
import android.view.ContextThemeWrapper;
 import com.MetroSub.MainApp;
import com.MetroSub.R;
 import com.MetroSub.database.QueryHelper;
 import com.MetroSub.datamine.GtfsFeed;
 import com.MetroSub.datamine.RetrieveFeedTask;
 import com.google.protobuf.ByteString;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kush Patel
  * Date: 6/30/13
  * Time: 8:36 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BaseActivity extends Activity {
 
     private static final String TAG = "BaseActivity";
 
     protected ActionBar mActionBar;
     private MainApp mainApp = MainApp.getAppInstance();
 
     protected QueryHelper mQueryHelper;
     protected GtfsFeed mGtfsFeed;
 
     private Timer mRetrieveFeedTimer;
 
     protected final long RETRIEVE_FEED_TASK_DELAY = 60 * 1000;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (!getMainApp().getNetworkConnectionStatus()) {
             showAlertDialog();
         }
 
         mQueryHelper = getMainApp().getQueryHelper();
         mGtfsFeed = getMainApp().getGtfsFeed();
 
         // Periodically fetch new GTFS feed in the background .. after every 1 minute
         mRetrieveFeedTimer = new Timer();
         mRetrieveFeedTimer.scheduleAtFixedRate(new RetrieveFeedTimerTask(), RETRIEVE_FEED_TASK_DELAY, RETRIEVE_FEED_TASK_DELAY);
 
         mActionBar = getActionBar();
         //mActionBar.setTitle(ACTION_BAR_TITLE);
         mActionBar.setDisplayShowCustomEnabled(true);
         //mActionBar.setDisplayShowTitleEnabled(false);
         //mActionBar.setDisplayShowHomeEnabled(false);
         //mActionBar.setCustomView(R.layout.action_bar);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mRetrieveFeedTimer.cancel();
     }
 
     public MainApp getMainApp() {
         return mainApp;
     }
 
     private class RetrieveFeedTimerTask extends TimerTask {
         public void run() {
             updateFeedData();
         }
     }
 
     protected void updateFeedData() {
         if (mainApp.isNetworkAvailable()) {
             try {
                 RetrieveFeedTask task = new RetrieveFeedTask();
                 Log.d(TAG, "Retrieving feed...");
                 ByteString data = task.execute().get();
                 mGtfsFeed.updateGtfsFeed(data);
             } catch (Exception e) {
                 Log.e(TAG, "Unable to retrieve GTFS data: " + e.getMessage());
             }
         }
     }
 
     public String getApplicationName() {
         final PackageManager packageManager = getApplicationContext().getPackageManager();
         ApplicationInfo applicationInfo;
         try {
             applicationInfo = packageManager.getApplicationInfo(this.getPackageName(), 0);
         } catch (final PackageManager.NameNotFoundException e) {
             applicationInfo = null;
         }
         return (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo).toString() : "(unknown)");
     }
 
     public void showAlertDialog() {
 
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                    new ContextThemeWrapper(this, AlertDialog.THEME_HOLO_LIGHT));
        alertDialogBuilder.setTitle("Connection Error");
        alertDialogBuilder.setMessage("Please restart the application after connecting to the internet.");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // exit the activity/app
                         BaseActivity.this.finish();
                     }
                 });
 
         AlertDialog alertDialog = alertDialogBuilder.create();
         alertDialog.show();
     }
 }
