 package com.gzaas.android.widget;
 
 import android.app.AlertDialog;
 import android.content.Context;
 
 /**
  * Simple alert dialog helper.
  */
 public class AlertDialogHelper {
 	
 	/**
 	 * @param context The aplication context.
 	 * @param resTitle Thew resoure string of the title.
	 * @param resMessage The resource string of the message.
 	 * @return Instance of a alert dialog with title and meesage.
 	 */
 	public static AlertDialog create(Context context, int resTitle, int resMessage) {
 		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 		alertDialog.setTitle(resTitle);
		alertDialog.setMessage(context.getString(resMessage));
 		return alertDialog;
 	}
 
 }
