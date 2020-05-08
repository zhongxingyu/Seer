 package de.hska.shareyourspot.android.helper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 public class AlertHelper
 {
 	private int title;
 	private int message;
	public AlertDialog.Builder alertDialogBuilder;
 	
 	public AlertHelper(Context context,int passwordsnotmatchingtitle, int passwordsnotmatchingtext)
 	{
 		this.alertDialogBuilder = new  AlertDialog.Builder(context);
 		this.title = passwordsnotmatchingtitle;
 		this.message = passwordsnotmatchingtext;
 	}
 	
 	public AlertHelper(Context context,int passwordsnotmatchingtitle, int passwordsnotmatchingtext, String button)
 	{
 		this.alertDialogBuilder = new  AlertDialog.Builder(context);
 		this.title = passwordsnotmatchingtitle;
 		this.message = passwordsnotmatchingtext;
 		this.alertDialogBuilder.setPositiveButton(button, null);
 	}
 	
 	
 	public void fireAlert()
 	{
 		alertDialogBuilder.setTitle(this.title);
 		alertDialogBuilder.setMessage(this.message);
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 }
 	
 	
 //	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 //	alertDialogBuilder.setTitle(R.string.passwordsNotMatchingTitle);
 //	alertDialogBuilder.setMessage(R.string.passwordsNotMatchingText);
 //	AlertDialog alertDialog = alertDialogBuilder.create();
 //	alertDialog.show();
 
