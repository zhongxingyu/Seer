 /*
  * Copyright 2013 Christian Grach
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cmgapps.android.util;
 
 import static com.cmgapps.android.util.LogUtils.LOGE;
 import static com.cmgapps.android.util.LogUtils.LOGI;
 import static com.cmgapps.android.util.LogUtils.makeLogTag;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 
 import com.cmgapps.android.R;
 
 /**
  * <p>
  * Class that utilizes usage count and time to open a rate dialog.
  * </p>
  * <p>
  * Use {@link #incrementUseCount()} on your main activity
  * {@link Activity#onCreate(Bundle)} implementation.<br />
  * Then call {@link #checkForRating()} to check if the requirements are met to
  * show the dialog and finally call {@link #show()} to show the rating dialog
  * </p>
  *
  */
 public class CMGAppRater
 {
   private static final String TAG = makeLogTag("CMGAppRater");
   private static final String APP_RATE_FILE_NAME = "CMGAppRater";
   private static final int LAUNCHES_UNTIL_PROMPT = 5;
   private static final long DAYS_UNTIL_PROMPT = 5 * DateUtils.DAY_IN_MILLIS;
   private static final long DAYS_UNTIL_REMIND_AGAIN = 2 * DateUtils.DAY_IN_MILLIS;
 
   private static final String FIRST_USE = "first_use";
   private static final String USE_COUNT = "use_count";
   private static final String DECLINED_RATE = "declined_rate";
   private static final String TRACKING_VERSION = "tracking_version";
   private static final String REMIND_LATER_DATE = "remind_later_date";
   private static final String APP_RATED = "rated";
   private static final boolean RATER_DEBUG = false;
 
   private SharedPreferences mPref;
   private Context mContext;
   private boolean mDebug = false;
  private AlertDialog mDialog;
 
   private static CMGAppRater sInstance = new CMGAppRater();
 
   private CMGAppRater()
   {
   }
 
   /**
    * <p>
    * Sets the current <code>Context</code>.
    * </p>
    *
    * @param context
    *          the Context for the <code>CMGAppRater</code>.
    */
   public void setContext(Context context)
   {
     mContext = context;
     mPref = mContext.getSharedPreferences(APP_RATE_FILE_NAME, Context.MODE_PRIVATE);
   }
 
   /**
    * <p>
    * Create a {@link CMGAppRater} instance
    * </p>
    * <p>
    * <b>IMPORTANT:</b> call {@link #setContext(Context)} to load the required
    * {@link SharedPreferences} file.
    * </p>
    *
    * @return The {@link CMGAppRater} instance
    */
   public static CMGAppRater getInstance()
   {
     return sInstance;
   }
 
   /**
    * <p>
    * Sets the debug flag to display current <code>CmgAppRater</code> field
    * values on {@link #checkForRating()}
    * </p>
    *
    * @param debug
    *          true to display debug output
    */
   public void setDebug(boolean debug)
   {
     mDebug = debug;
   }
 
   /**
    * <p>
    * Call to check if the requirements to open the rating dialog are met
    * </p>
    * <p>
    * <b>NOTICE:</b> This method is thread safe
    * </p>
    *
    * @return true if requirements are met.
    */
   public synchronized boolean checkForRating()
   {
     checkContext();
 
     if (mDebug)
       LOGI(TAG, "Rater Content:" + toString());
 
     if (RATER_DEBUG)
       return true;
 
     if (mPref.getBoolean(DECLINED_RATE, false))
       return false;
 
     if (mPref.getBoolean(APP_RATED, false))
       return false;
 
     if (System.currentTimeMillis() < (mPref.getLong(FIRST_USE, 0L) + DAYS_UNTIL_PROMPT))
       return false;
 
     if (mPref.getInt(USE_COUNT, 0) <= LAUNCHES_UNTIL_PROMPT)
       return false;
 
     if (System.currentTimeMillis() < (mPref.getLong(REMIND_LATER_DATE, 0L) + DAYS_UNTIL_REMIND_AGAIN))
       return false;
 
     return true;
   }
 
   /**
    * <p>
    * Increments the usage count.
    * </p>
    * <p>
    * <b>NOTICE:</b> This method is thread safe
    * </p>
    */
   public synchronized void incrementUseCount()
   {
     checkContext();
 
     Editor editor = mPref.edit();
     int version_code = 0;
 
     try
     {
       version_code = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
     }
     catch (NameNotFoundException exc)
     {
       LOGE(TAG, "PackageName not found: " + mContext.getPackageName());
     }
 
     int tracking_version = mPref.getInt(TRACKING_VERSION, -1);
 
     if (tracking_version == -1)
     {
       tracking_version = version_code;
       editor.putInt(TRACKING_VERSION, tracking_version);
     }
 
     if (tracking_version == version_code)
     {
 
       if (mPref.getLong(FIRST_USE, 0L) == 0L)
         editor.putLong(FIRST_USE, System.currentTimeMillis());
 
       editor.putInt(USE_COUNT, mPref.getInt(USE_COUNT, 0) + 1);
     }
     else
     {
       editor.putInt(TRACKING_VERSION, version_code).putLong(FIRST_USE, System.currentTimeMillis()).putInt(USE_COUNT, 1)
           .putBoolean(DECLINED_RATE, false).putLong(REMIND_LATER_DATE, 0L).putBoolean(APP_RATED, false);
     }
 
     PreferenceEditorHelper.commit(editor);
   }
 
   /**
    * <p>
    * Shows a default {@link AlertDialog}
    * </p>
    */
   public void show()
   {
     checkContext();
    
    if (mDialog != null && mDialog.isShowing())
      return;
    
     final Editor editor = mPref.edit();
     final PackageManager pm = mContext.getPackageManager();
 
     String appName = null;
     try
     {
       ApplicationInfo ai = pm.getApplicationInfo(mContext.getPackageName(), 0);
       appName = (String) pm.getApplicationLabel(ai);
     }
    catch (NameNotFoundException e)
    {
      LOGE(TAG, "Application name can not be found");
    }
    
    if (appName == null)
     {
       appName = "App";
     }
 
    mDialog = new AlertDialog.Builder(mContext)
         .setTitle(R.string.dialog_cmgrate_title)
        .setMessage(mContext.getString(R.string.dialog_cmgrate_message,(String) appName))
         .setCancelable(false)
         .setIcon(mContext.getApplicationInfo().icon)
         .setPositiveButton(mContext.getString(R.string.dialog_cmgrate_ok),
             new DialogInterface.OnClickListener()
             {
               @Override
               public void onClick(DialogInterface dialog, int id)
               {
                 editor.putBoolean(APP_RATED, true);
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                     + mContext.getPackageName()));
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 mContext.startActivity(intent);
 
                 PreferenceEditorHelper.commit(editor);
                 dialog.dismiss();
               }
             }).setNegativeButton(R.string.dialog_cmgrate_no, new DialogInterface.OnClickListener()
         {
           @Override
           public void onClick(DialogInterface dialog, int id)
           {
             PreferenceEditorHelper.commit(editor.putBoolean(DECLINED_RATE, true));
             dialog.dismiss();
           }
         }).setNeutralButton(R.string.dialog_cmgrate_later, new DialogInterface.OnClickListener()
         {
           @Override
           public void onClick(DialogInterface dialog, int id)
           {
             PreferenceEditorHelper.commit(editor.putLong(REMIND_LATER_DATE, System.currentTimeMillis()));
             dialog.dismiss();
           }
         }).show();
     PreferenceEditorHelper.commit(editor);
   }
 
   private static String ratePreferenceToString(SharedPreferences pref)
   {
     StringBuilder builder = new StringBuilder("[");
     addKeyValueString(builder, DECLINED_RATE, pref.getBoolean(DECLINED_RATE, false));
     addKeyValueString(builder, APP_RATED, pref.getBoolean(APP_RATED, false));
     addKeyValueString(builder, TRACKING_VERSION, pref.getInt(TRACKING_VERSION, -1));
     addKeyValueString(builder, FIRST_USE,
         SimpleDateFormat.getDateTimeInstance().format(new Date(pref.getLong(FIRST_USE, 0L))));
     addKeyValueString(builder, USE_COUNT, pref.getInt(USE_COUNT, 0));
     addKeyValueString(builder, REMIND_LATER_DATE,
         SimpleDateFormat.getDateTimeInstance().format(new Date(pref.getLong(REMIND_LATER_DATE, 0L))));
     builder.replace(builder.length() - 2, builder.length(), "]");
     return builder.toString();
   }
 
   private static void addKeyValueString(final StringBuilder builder, String key, Object value)
   {
     builder.append(key).append(": ").append(value).append(", ");
   }
 
   private void checkContext()
   {
     if (mContext == null)
       throw new RuntimeException("Context not set. Use setContext(Context).");
   }
 
   /**
    * <p>
    * Get the {@link SharedPreferences} file contents
    * </p>
    */
   @Override
   public String toString()
   {
     checkContext();
     return ratePreferenceToString(mPref);
   }
 }
