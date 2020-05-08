 package com.nebkat.smsalarm;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class SetupActivity extends PreferenceActivity {
     private static final String PREFERENCES_DESCRIPTION_DIALOG_SHOWN = "description_dialog_shown";
     public static final String PREFERENCES_ACTIVATION_SMS = "activation_sms";
     public static final String PREFERENCES_VIBRATE = "vibrate";
     public static final String PREFERENCES_DURATION = "duration";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
 
         final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         if (!preferences.getBoolean(PREFERENCES_DESCRIPTION_DIALOG_SHOWN, false)) {
             new AlertDialog.Builder(this).setTitle(R.string.description_dialog_title)
                     .setMessage(R.string.description_dialog_message)
                     .setPositiveButton(R.string.description_dialog_ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             preferences.edit().putBoolean(PREFERENCES_DESCRIPTION_DIALOG_SHOWN, true).commit();
                         }
                     }).show();
         }

        Preference testPreference = findPreference("test");
        Intent testIntent = new Intent(this, AlarmDialogActivity.class);
        testIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        testIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        testPreference.setIntent(testIntent);
     }
 }
