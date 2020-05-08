 package com.ladinc.showrss.utilities;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 
 public class LoadingDialog {
 
 	private ProgressDialog dialog;
 	
 	public LoadingDialog(Context packageContextName, String message)
 	{
 		dialog = new ProgressDialog(packageContextName);
 		dialog.setMessage(message);
 		dialog.setCancelable(false);
 	}
 	
 	public void showLoadingDialog() 
 	{
 		if (dialog != null)
 			dialog.show();
 	}
 	
 	public void hideLoadingDialog() 
 	{
		if (dialog.isShowing())
			dialog.dismiss();
 	}
 	
 	public void toggleLoadingDialog(){
 		if (dialog != null)
 		{
 			if (dialog.isShowing()) 
 				dialog.dismiss();
 			else
 				dialog.show();			
 		}
 	}
 	
 	public void setMessage(String message)
 	{
 		dialog.setMessage(message);
 	}
 }
