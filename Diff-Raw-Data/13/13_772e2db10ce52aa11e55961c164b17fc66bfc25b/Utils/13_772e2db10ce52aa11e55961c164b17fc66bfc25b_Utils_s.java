 package com.howfun.android.lover;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.util.Log;
 
 public final class Utils {
 
    public static void log(String tag, String info) {
      Log.e("Lover >>>>>>>>>" + tag, "-------->" + info);
    }
 
    public static void showMessageDlg(Context context, int stringId) {
       new AlertDialog.Builder(context).setIcon(R.drawable.icon).setTitle(
             R.string.app_name).setMessage(stringId).setPositiveButton(
             android.R.string.ok, null).show();
    }
 }
