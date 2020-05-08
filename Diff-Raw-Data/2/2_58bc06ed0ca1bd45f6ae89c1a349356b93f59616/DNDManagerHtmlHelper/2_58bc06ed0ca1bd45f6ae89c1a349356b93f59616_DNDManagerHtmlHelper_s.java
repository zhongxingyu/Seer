 package com.webshrub.moonwalker.androidapp;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class DNDManagerHtmlHelper {
     public static final String CONTACT_LOG_FLAG_KEY = "contactLogFlag";
     public static final String CONTACT_LOG_FLAG_DEFAULT_VALUE = "off";
     public static final String DELETE_SMS_FLAG_KEY = "deleteSMSFlag";
    public static final String DELETE_SMS_FLAG_DEFAULT_VALUE = "off";
     public static final String DELETE_SENT_SMS_FLAG_KEY = "deleteSentSMSFlag";
     public static final String DELETE_SENT_SMS_FLAG_DEFAULT_VALUE = "off";
     public static final String SMS_FORMAT_KEY = "smsFormat";
     public static final String SMS_FORMAT_DEFAULT_VALUE = "COMP TEL NO ~~number~~;~~datetime~~;~~text~~";
 
     public static boolean getContactLogFlag(Context context) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         String contactLogFLag = sharedPreferences.getString(CONTACT_LOG_FLAG_KEY, CONTACT_LOG_FLAG_DEFAULT_VALUE);
         return !contactLogFLag.equals(CONTACT_LOG_FLAG_DEFAULT_VALUE);
     }
 
     public static boolean getDeleteDNDManagerItemFlag(Context context) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         String deleteSMSFLag = sharedPreferences.getString(DELETE_SMS_FLAG_KEY, DELETE_SMS_FLAG_DEFAULT_VALUE);
         return !deleteSMSFLag.equals(DELETE_SMS_FLAG_DEFAULT_VALUE);
     }
 
     public static boolean getDeleteSentSMSFlag(Context context) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         String deleteSentSMSFLag = sharedPreferences.getString(DELETE_SENT_SMS_FLAG_KEY, DELETE_SENT_SMS_FLAG_DEFAULT_VALUE);
         return !deleteSentSMSFLag.equals(DELETE_SENT_SMS_FLAG_DEFAULT_VALUE);
     }
 
     public static String getMessageFormat(Context context) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         return sharedPreferences.getString(SMS_FORMAT_KEY, SMS_FORMAT_DEFAULT_VALUE);
     }
 }
