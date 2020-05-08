 package org.scoutant.tf.util;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 
 /**
  *	An adaptation from http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
  */
 public class AppRater {
     private final static String APP_PNAME = "org.scoutant.tf";
     private final static int DAYS_UNTIL_PROMPT = 3;
     private final static int LAUNCHES_UNTIL_PROMPT = 10;
 
     public static void app_launched(Context context) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( context);
         if ( prefs.getBoolean( "dontshowagain", false)) return ;
         
         SharedPreferences.Editor editor = prefs.edit();
         
         // Get date of first launch
         long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
         if (date_firstLaunch == 0) {
             date_firstLaunch = System.currentTimeMillis();
             editor.putLong( "date_firstlaunch", date_firstLaunch);
         }
 
         // Increment launch counter
         long launch_count = prefs.getLong("launch_count", 0) + 1;
         editor.putLong( "launch_count", launch_count);
 
         if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
             if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                 showRate(context, editor);
             }
         }

         editor.commit();
     }   
     
     public static void showRate(final Context context, final SharedPreferences.Editor editor) {
 		new AlertDialog.Builder(context)
 		.setTitle( "Vous utilisez Trafic Futé...")
 		.setMessage( "Cocher une note dans l'Android Market?")
 		.setCancelable(true)
 		.setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
                editor.putBoolean("dontshowagain", true).commit();
                 context.startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                 }
 			})
 		.setNeutralButton( "Plus tard", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				}
 			})
 		.setNegativeButton( "Annuler", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
                    editor.putBoolean("dontshowagain", true).commit();
 				}
 			})
 		.create()
 		.show();		
     }
 }
