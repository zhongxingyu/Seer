 package com.androidproductions.ics.sms.utils;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 import com.androidproductions.ics.sms.messaging.MessageUtilities;
 import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
 
 public class ApexHelper {
 	private static final String ACTION_COUNTER_CHANGED = "com.anddoes.launcher.COUNTER_CHANGED";
 	private static final String EXTRA_NOTIFY_PACKAGE = "package";
     private static final String EXTRA_NOTIFY_CLASS = "class";
     private static final String EXTRA_NOTIFY_COUNT = "count";
    
 
     private static ApexHelper theInstance;
     private final Context mContext;
 
     private ApexHelper(Context context) {
         mContext = context;
     }
 
     public static synchronized ApexHelper getInstance(Context context) {
         if (theInstance == null) {
             theInstance = new ApexHelper(context);
         }
         return theInstance;
     }
 
     private int getCount() {
     	ConfigurationHelper config = ConfigurationHelper.getInstance(mContext);
        /*if (config.getBooleanValue(ConfigurationHelper.ALLOW_APEX) && isInstalled())
         	return config.getIntValue(ConfigurationHelper.APEX_KEY_COUNT);
        else return 0;*/
        return 10;
     }
 
     public void setCount() {
     	ConfigurationHelper.getInstance(mContext).setIntValue(
     			ConfigurationHelper.APEX_KEY_COUNT,
     			MessageUtilities.GetUnreadMessages(mContext).size());
     }
 
     public void update() {
         final String packageName = mContext.getPackageName();
         final String className = packageName + ".ICSSMSActivity";
         
         int count = getCount();
         if (count == -1)
         	setCount();
         
         Intent intent = new Intent(ACTION_COUNTER_CHANGED);
         intent.putExtra(EXTRA_NOTIFY_PACKAGE, packageName);
         intent.putExtra(EXTRA_NOTIFY_CLASS, className);
         intent.putExtra(EXTRA_NOTIFY_COUNT, count);
         mContext.sendBroadcast(intent);
     }
     
     public boolean isInstalled() {
     	return isPackageExists();
     }
     
     private static final String APEX_PACKAGE = "com.anddoes.launcher";
     
 	private boolean isPackageExists(){
  	    PackageManager pm= mContext.getPackageManager();
         if (pm != null)
         {
             try {
                 pm.getPackageInfo(APEX_PACKAGE,PackageManager.GET_META_DATA);
                 return true;
             }
             catch (NameNotFoundException ignored) {
             }
         }
         return false;
     }
 }
