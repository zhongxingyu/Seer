 package com.jason.ocbcapp;
 
 import java.io.InputStream;
 import java.util.Scanner;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * 
  * @author Jason 
  * Contains cross cutting concerns aka code that is used by many
  * classes.
  * 
  *         The code here MUST be guaranteed to be functional, it should not
  *         cause another part of the program to change.
  * 
  *         Cross cutting concerns:
  *         - String readStream(InputStream) 
  *         - void showToastMessage(Context,String) 
  *         - void showUnableToConnectMessage(Context)
  *         - Preferences getter and setters
  */
 public class CrossCutting {
 
     private static final String HAS_SETUP = "hasSetup";
     private static final String USER_TOKEN = "userToken";
     private static final String APP_TAG = MainActivity.APP_TAG;
    public static final String APP_PREFS_FILE_NAME = "OCBCPrefsFile";
 
     public static void showUnableToConnectMessage(Context context) {
         showToastMessage(context, "Unable to connect to server");
     }
 
     public static void showToastMessage(Context context, String msg) {
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
         t.show();
     }
 
     public static String readStream(InputStream inputStream) {
         // TODO Auto-generated method stub
         StringBuilder buf = new StringBuilder();
         Scanner sc = null;
         try {
 
             sc = new Scanner(inputStream);
             while (sc.hasNext()) {
                 buf.append(sc.next());
             }
 
         } catch (Exception e) {
             Log.e(APP_TAG, e.getMessage());
         }
         return buf.toString();
     }
 
     public static boolean getBooleanPreference(Context context,
             String prefName, Boolean defValue) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_FILE_NAME,
                 0);
         Boolean result = prefs.getBoolean(prefName, defValue);
         return result;
     }
 
     public static String getStringPreference(Context context,
             String prefName, String defValue) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_FILE_NAME,
                 0);
         String result = prefs.getString(prefName, defValue);
         return result;
     }
 
     public static void setBooleanPreference(Context context, String prefName, Boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_FILE_NAME, 0);
         Editor editor = prefs.edit();
         editor.putBoolean(prefName, value);
         editor.apply();
     }
 
     public static void setStringPreference(Context context, String prefName, String value) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_FILE_NAME, 0);
         Editor editor = prefs.edit();
         editor.putString(prefName, value);
         editor.apply();
     }
 
     public static String getUserTokenFromPreferences(Context context) {
         return getStringPreference(context, USER_TOKEN, "");
     }
 
     public static Boolean getHasSetupFromPreferences(Context context) {
         return getBooleanPreference(context, HAS_SETUP, false);
     }
 
     public static void setHasSetupPreference(Context context, Boolean value) {
         setBooleanPreference(context, HAS_SETUP, value);
     }
 
     public static void setUserTokenPreference(Context context, String value) {
         setStringPreference(context, USER_TOKEN, value);
     }
 }
