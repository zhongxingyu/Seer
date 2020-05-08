 package com.patdivillyfitness.runcoach;
 
 import com.patdivillyfitness.runcoach.activity.WarmUpActivity;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.widget.Toast;
 
 public class AppLaunchChecker {
    private final static String APP_TITLE = Constants.APP_NAME;
    private final static String APP_PNAME = Constants.APP_PACKAGE;
    
    private final static int DAYS_UNTIL_PROMPT = 5;
    private final static int LAUNCHES_UNTIL_PROMPT = 5;
    
    public static boolean checkFirstOrRateLaunch(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.APP_LAUNCHER_CHECKER_PREF, 0);
       if (prefs.getBoolean(Constants.DONT_ASK_AGAIN, false)) { return false; }
        
        SharedPreferences.Editor editor = prefs.edit();       
        long launch_count = prefs.getLong(Constants.LAUNCH_COUNT, 0) + 1;
        editor.putLong(Constants.LAUNCH_COUNT, launch_count);
 
        Long date_firstLaunch = prefs.getLong(Constants.FIRST_LAUNCH_DATE, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(Constants.FIRST_LAUNCH_DATE, date_firstLaunch);
        }
        
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + 
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        
        editor.commit();
        if (launch_count==1)
           return true;
        else
           return false;
    }   
    
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor)
    {
 
       AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
 
       alertDialog.setTitle(String.format(mContext.getString(R.string.rate_app), APP_TITLE));
       alertDialog.setMessage(String.format(mContext.getString(R.string.rate_app_text), APP_TITLE));
 
       alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.rate), new DialogInterface.OnClickListener()
          {
 
             public void onClick(DialogInterface dialog, int id)
             {
                //              mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                Toast.makeText(mContext, mContext.getString(R.string.google_play_launched), Toast.LENGTH_LONG).show();
                dialog.dismiss();
             }
          });
 
       alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.remind), new DialogInterface.OnClickListener()
          {
 
             public void onClick(DialogInterface dialog, int id)
             {
 
                dialog.dismiss();
 
             }
          });
 
       alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.no), new DialogInterface.OnClickListener()
          {
 
             public void onClick(DialogInterface dialog, int id)
             {
 
                if (editor != null)
                {
                   editor.putBoolean(Constants.DONT_ASK_AGAIN, true);
                   editor.commit();
                }
                dialog.dismiss();
 
             }
          });
 
       alertDialog.show();
    }
 }
