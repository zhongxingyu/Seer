 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.groupinform.activities;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import org.odk.collect.android.utilities.FileUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Window;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ImageView.ScaleType;
 
 import com.couchbase.libcouch.CouchDB;
 import com.couchbase.libcouch.CouchInstaller;
 import com.couchbase.libcouch.CouchService;
 import com.couchbase.libcouch.ICouchClient;
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.logic.InformOnlineState;
 import com.radicaldynamic.groupinform.services.DatabaseService;
 import com.radicaldynamic.groupinform.services.InformOnlineService;
 import com.radicaldynamic.groupinform.utilities.FileUtilsExtended;
 
 /**
  * Application initialization: registration, login, database installation & init 
  */
 public class LauncherActivity extends Activity
 {
     private static final String t = "LauncherActivity: ";
     
     // Dialog constants
     private static final int DIALOG_COUCH_ERROR = 1;
     private static final int DIALOG_EXTERNAL_STORAGE_UNAVAILABLE = 2;
     private static final int DIALOG_UNABLE_TO_CONNECT_OFFLINE_DISABLED = 3;
     private static final int DIALOG_UNABLE_TO_CONNECT_OFFLINE_ENABLED = 4;
     private static final int DIALOG_UNABLE_TO_REGISTER = 5;    
     private static final int DIALOG_UPGRADE_FAILED = 6;
     
     // Intent status codes
     private static final String KEY_REINIT_IOSERVICE = "key_reinit_ioservice";
     
     private static final int BROWSER_ACTIVITY = 1;
     
     private ProgressDialog mProgressDialog;
     private Toast mSplashToast;    
 
     private final ICouchClient mCouchCallback = new ICouchClient.Stub() {
         @Override
         public void couchStarted(String host, int port) 
         {
             if (mProgressDialog != null) {
                 mProgressDialog.dismiss();
             }
             
             // Persistent service
             startService(new Intent(LauncherActivity.this, CouchService.class));
             
             Collect.getInstance().getDbService().setLocalDatabaseInfo(host, port);
             startActivityForResult(new Intent(LauncherActivity.this, BrowserActivity.class), BROWSER_ACTIVITY);
         }
 
         @Override
         public void installing(int completed, int total) 
         {
             if (mProgressDialog == null) {
                 mProgressDialog = new ProgressDialog(LauncherActivity.this);
                 mProgressDialog.setTitle(" ");
                 mProgressDialog.setCancelable(false);
                 mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                 mProgressDialog.show();
             }            
             
             mProgressDialog.setTitle("Initializing Database");
             mProgressDialog.setProgress(completed);
             mProgressDialog.setMax(total);
         }
 
         @Override
         public void exit(String error) 
         {
             Log.v(Collect.LOGTAG, "CouchDB error: " + error);
             showDialog(DIALOG_COUCH_ERROR);
         }
     };
 
     // Service handling for our connection to databases provided by Couch
     private ServiceConnection mDatabaseConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service)
         {
             Collect.getInstance().setDbService(((DatabaseService.LocalBinder) service).getService());
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             Collect.getInstance().setDbService(null);
         }
     };    
     
     // Service handling for our connection to Inform Online
     private ServiceConnection mOnlineConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service)
         {
             Collect.getInstance().setIoService(((InformOnlineService.LocalBinder) service).getService());
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             Collect.getInstance().setIoService(null);
         }
     };
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         
         if (resultCode == RESULT_CANCELED)
             return;
         
         switch (requestCode) {
         // "Exit" if the user returns from BrowserActivity
         case BROWSER_ACTIVITY:
             finish();
             break; 
         }        
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         // If SD card error, quit
         if (!FileUtilsExtended.storageReady())
             showDialog(DIALOG_EXTERNAL_STORAGE_UNAVAILABLE);
 
         Intent intent = getIntent();
         
         if (intent == null) {            
         } else {
             if (intent.getBooleanExtra(KEY_REINIT_IOSERVICE, false)) {
                 if (Collect.getInstance().getIoService() instanceof InformOnlineService)
                     Collect.getInstance().getIoService().reinitializeService();
             }
         }
         
         displaySplash();
 
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);       
         setContentView(R.layout.launcher);
         
         startService(new Intent(this, InformOnlineService.class));
         startService(new Intent(this, DatabaseService.class));
         
         new InitializeApplicationTask().execute(getApplicationContext());
     }
 
     public Dialog onCreateDialog(int id)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         Dialog dialog = null;
         
         switch (id) {
         case DIALOG_COUCH_ERROR:
             builder
             .setMessage("Error Initializing or Starting Database")
             .setPositiveButton("Try Again?", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     startCouch();
                 }
             })
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     LauncherActivity.this.moveTaskToBack(true);
                 }
             });
             
             dialog = builder.create();
             break;
         
         case DIALOG_EXTERNAL_STORAGE_UNAVAILABLE:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_alert)
                 .setMessage(getString(R.string.no_sd_error));
             
             builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     finish();
                 }
             });
             
             dialog = builder.create();
             break;
             
         // Registered    
         case DIALOG_UNABLE_TO_CONNECT_OFFLINE_DISABLED:
             String msg;            
     
             if (Collect.getInstance().getInformOnlineState().hasReplicatedFolders())
                 msg = getString(R.string.tf_connection_error_registered_with_db_msg);
             else    
                 msg = getString(R.string.tf_connection_error_registered_without_db_msg);
             
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_alert)        
                 .setTitle(R.string.tf_unable_to_connect)
                 .setMessage(msg);
     
             builder.setPositiveButton(getText(R.string.tf_retry), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     restartActivity(true);
                 }
             });
             
             if (Collect.getInstance().getInformOnlineState().hasReplicatedFolders()) {                
                 builder.setNeutralButton(getText(R.string.tf_go_offline), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         startCouch();
                     }
                 });    
             }
     
             builder.setNegativeButton(getText(R.string.tf_exit_inform), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     finish();
                 }
             });
     
             dialog = builder.create();            
             break;
            
         // Registered
         case DIALOG_UNABLE_TO_CONNECT_OFFLINE_ENABLED:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_info)        
                 .setTitle(R.string.tf_offline_mode_enabled)
                 .setMessage(getString(R.string.tf_offline_mode_enabled_msg));
     
             builder.setPositiveButton(getText(R.string.tf_continue), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                    restartActivity(false);        
                 }
             });
     
             dialog = builder.create();       
             break;
             
         case DIALOG_UNABLE_TO_REGISTER:
             builder
                 .setCancelable(false)
                 .setIcon(R.drawable.ic_dialog_alert)        
                 .setTitle(R.string.tf_unable_to_connect)
                 .setMessage(getString(R.string.tf_connection_error_unregistered_msg));
     
             builder.setPositiveButton(getText(R.string.tf_retry), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     restartActivity(true);
                 }
             });
     
             builder.setNegativeButton(getText(R.string.tf_exit_inform), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     finish();
                 }
             });
     
             dialog = builder.create();            
             break;
             
         case DIALOG_UPGRADE_FAILED:
             builder
             .setMessage("Error Upgrading Application")
             .setPositiveButton("Try Again?", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     restartActivity(true);
                 }
             })
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     LauncherActivity.this.moveTaskToBack(true);
                 }
             });
             
             dialog = builder.create();            
             break;
         }
         
         return dialog;
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.ListActivity#onDestroy()
      * 
      * Recall:
      * Because onPause() is the first of the three [killable methods], it's the only one that's guaranteed to be called 
      * before the process is killed — onStop() and onDestroy() may not be. Therefore, you should use onPause() to write 
      * any persistent data (such as user edits) to storage. 
      */
     @Override
     protected void onDestroy()
     {        
         // Unbind from our services
         if (Collect.getInstance().getCouchService() instanceof ServiceConnection) {
             try {
                 Log.d(Collect.LOGTAG, t + "unbinding from CouchService");
                 unbindService(Collect.getInstance().getCouchService());                
             } catch (IllegalArgumentException e) {
                 Log.w(Collect.LOGTAG, t + "CouchService not registered: " + e.toString());
             }
         }
 
         if (Collect.getInstance().getDbService() instanceof DatabaseService) {
             try {
                 Log.d(Collect.LOGTAG, t + "unbinding from DatabaseService");
                 unbindService(mDatabaseConnection);
             } catch (IllegalArgumentException e) { 
                 Log.w(Collect.LOGTAG, t + "DatabaseService not registered: " + e.toString());    
             }
         }
 
         if (Collect.getInstance().getIoService() instanceof InformOnlineService) {
             try {
                 Log.d(Collect.LOGTAG, t + "unbinding from InformOnlineService");
                 unbindService(mOnlineConnection);
             } catch (IllegalArgumentException e) {
                 Log.w(Collect.LOGTAG, t + "InformOnlineService not registered: " + e.toString());
             }
         }
         
         super.onDestroy();
     }
 
     @Override
     protected void onPause()
     {
         super.onPause();    
     }
     
     @Override
     protected void onResume()
     {   
         super.onResume();
         
         if (Collect.getInstance().getIoService() == null)
             bindService(new Intent(LauncherActivity.this, InformOnlineService.class), mOnlineConnection, Context.BIND_AUTO_CREATE);
         
         if (Collect.getInstance().getDbService() == null)
             bindService(new Intent(LauncherActivity.this, DatabaseService.class), mDatabaseConnection, Context.BIND_AUTO_CREATE);
     }
     
     public class InitializeApplicationTask extends AsyncTask<Object, Void, Void> 
     {
         private boolean pinged = false;
         private boolean registered = false;
         private boolean upgradeFailed = false;
         
         @Override
         protected Void doInBackground(Object... args)
         {  
             // Timer
             int seconds = 0;
             
             // Prepare environment for new Couch packaging if need be
             try {
                 couchPackageUpgradePath();
             } catch (IOException e) {
                 Log.e(Collect.LOGTAG, t + "upgrade path failed at some point " + e.toString());
                 e.printStackTrace();
                 upgradeFailed = true;
             }
             
             // Create directories
             FileUtils.createFolder(FileUtilsExtended.EXTERNAL_CACHE);
             FileUtils.createFolder(FileUtilsExtended.EXTERNAL_DB);
             FileUtils.createFolder(FileUtilsExtended.EXTERNAL_FILES);
             
             // The InformOnlineService will perform ping and check-in immediately (no need to duplicate here)
             while (true) {
                 // Either break out if we were successful in connecting or we have waited too long
                 if ((Collect.getInstance().getIoService() instanceof InformOnlineService && 
                         Collect.getInstance().getIoService().isInitialized()) || seconds > 30) 
                     break;
                 
                     /*
                      * If we have waited longer than 10 seconds we may need to start forcing the issue.
                      * 
                      * This might happen because we have restarted to retry the connection but the 
                      * services have not restarted and we are waiting on them to complete their usual
                      * 10 minute delay.
                      * 
                      * FIXME: this should check every 10 seconds at the 10 seconds mark and after...
                      */
                 if (Collect.getInstance().getIoService() instanceof InformOnlineService && seconds > 10)
                     Collect.getInstance().getIoService().goOnline();
                 
                 try {
                     Thread.sleep(1000);
                     seconds++;
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
             
             pinged = Collect.getInstance().getIoService().isRespondingToPings();
             registered = Collect.getInstance().getIoService().isRegistered();
 
             return null;
         }
     
         @Override
         protected void onPreExecute()
         {
         }
     
         @Override
         protected void onPostExecute(Void nothing) 
         {
             if (upgradeFailed) {
                 showDialog(DIALOG_UPGRADE_FAILED);
                 return;
             }            
             
             if (pinged) {
                 if (registered) {
                     startCouch();
                 } else {
                     startActivity(new Intent(getApplicationContext(), ClientRegistrationActivity.class));
                     finish();
                 }
             } else {
                 if (registered) {
                     if (Collect.getInstance().getInformOnlineState().isOfflineModeEnabled())
                         showDialog(DIALOG_UNABLE_TO_CONNECT_OFFLINE_ENABLED);
                     else
                         showDialog(DIALOG_UNABLE_TO_CONNECT_OFFLINE_DISABLED);
                 } else {
                     showDialog(DIALOG_UNABLE_TO_REGISTER);
                 }
             }
         }
         
         /*
          * Test for the old way of packaging Couch (e.g., sdcard-based install) and perform steps
          * to bring the environment up-to-date for the new Couch install
          */
         private void couchPackageUpgradePath() throws IOException
         {
             /*
              * Only copy the actual Couch databases (view index directories named .dbname_design and the
              * contents thereof will be automatically regenerated 
              */
             class DbFilesFilter implements FilenameFilter 
             {
                 @Override
                 public boolean accept(File dir, String filename) {
                     if (filename.startsWith("db_") && filename.endsWith(".couch"))
                         return true;
                     
                     return false;
                 }
             }
             
             // Are the old Couch & Erlang directories present?
             if (!new File(FileUtilsExtended.EXTERNAL_COUCH).exists() && !new File(FileUtilsExtended.EXTERNAL_ERLANG).exists())
                 return;
                 
             Log.i(Collect.LOGTAG, t + "old CouchDB environment detected; about to execute upgrade path");
 
             // Get a list of Couch database files
             FilenameFilter filter = new DbFilesFilter();
             String [] dbFiles = new File(FileUtilsExtended.EXTERNAL_COUCH, "/var/lib/couchdb").list(filter);
 
             // Move database and design files to the new location
             if (dbFiles == null) {
                 Log.d(Collect.LOGTAG, t + "no databases to move");
             } else {
                 for (String file : dbFiles) {
                     Log.d(Collect.LOGTAG, t + "about to copy " + file + " to new location");
                     File f = new File(FileUtilsExtended.EXTERNAL_COUCH, "/var/lib/couchdb/" + file);
                     org.apache.commons.io.FileUtils.copyFileToDirectory(f, new File(FileUtilsExtended.EXTERNAL_DB));
                 }
             }
 
             // Remove couchdb, erlang and other files on sdcard data storage
             CouchInstaller.deleteDirectory(new File(FileUtilsExtended.EXTERNAL_COUCH));
             CouchInstaller.deleteDirectory(new File(FileUtilsExtended.EXTERNAL_ERLANG));
             CouchInstaller.deleteDirectory(new File(FileUtilsExtended.EXTERNAL_FILES));
 
             // Remove couchdb & erlang on internal data storage
             CouchInstaller.appNamespace = LauncherActivity.this.getApplication().getPackageName();
             CouchInstaller.deleteDirectory(new File(CouchInstaller.dataPath() + "/couchdb"));
             CouchInstaller.deleteDirectory(new File(CouchInstaller.dataPath() + "/erlang"));
         }
     }
     
     private void displaySplash()
     {
         // Don't show the splash screen if this app appears to be registered
         if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(InformOnlineState.DEVICE_ID, null) instanceof String) {
             return;
         }
         
         // Fetch the splash screen Drawable
         Drawable image = null;
     
         try {
             // Attempt to load the configured default splash screen
             // The following code only works in 1.6+
             // BitmapDrawable bitImage = new BitmapDrawable(getResources(), FileUtils.SPLASH_SCREEN_FILE_PATH);
             BitmapDrawable bitImage = new BitmapDrawable(FileUtilsExtended.EXTERNAL_FILES + File.separator + FileUtilsExtended.SPLASH_SCREEN_FILE);
     
             if (bitImage.getBitmap() != null
                     && bitImage.getIntrinsicHeight() > 0
                     && bitImage.getIntrinsicWidth() > 0) {
                 image = bitImage;
             }
         } catch (Exception e) {
             // TODO: log exception for debugging?
         }
     
         // TODO: rework
         if (image == null) {
             // no splash provided...
 //            if (FileUtils.storageReady() && !((new File(FileUtils.DEFAULT_CONFIG_PATH)).exists())) {
                 // Show the built-in splash image if the config directory 
                 // does not exist. Otherwise, suppress the icon.
                 image = getResources().getDrawable(R.drawable.gc_color_preview);
 //            }
             
             if (image == null) 
                 return;
         }
     
         // Create ImageView to hold the Drawable...
         ImageView view = new ImageView(getApplicationContext());
     
         // Initialise it with Drawable and full-screen layout parameters
         view.setImageDrawable(image);
         
         int width = getWindowManager().getDefaultDisplay().getWidth();
         int height = getWindowManager().getDefaultDisplay().getHeight();
         
         FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height, 0);
         
         view.setLayoutParams(lp);
         view.setScaleType(ScaleType.CENTER);
         view.setBackgroundColor(Color.WHITE);
     
         // And wrap the image view in a frame layout so that the full-screen layout parameters are honoured
         FrameLayout layout = new FrameLayout(getApplicationContext());
         layout.addView(view);        
 
         // Create the toast and set the view to be that of the FrameLayout
         mSplashToast = Toast.makeText(getApplicationContext(), "splash screen", Toast.LENGTH_LONG);
         mSplashToast.setView(layout);
         mSplashToast.setGravity(Gravity.CENTER, 0, 0);
         mSplashToast.show();
     }
     
     // Restart this activity, optionally requesting a complete restart
     private void restartActivity(boolean fullRestart)
     {
         Intent i = new Intent(getApplicationContext(), LauncherActivity.class);
         
         // If the user wants a full restart then request reinitialization of the IO service
         if (fullRestart)
             i.putExtra(KEY_REINIT_IOSERVICE, true);
         
         startActivity(i);
         finish();
     }
 
     private void startCouch() 
     {
         Collect.getInstance().setCouchService(CouchDB.getService(getBaseContext(), null, "release-1.0.2-1", mCouchCallback));
     }
 }
