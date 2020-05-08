 package com.appirio.mobile;
 
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 
 import com.salesforce.androidsdk.R;
 import com.salesforce.androidsdk.ui.LoginActivity;
 import com.salesforce.androidsdk.ui.SalesforceDroidGapActivity;
 
 public class AMSalesforceDroidGapActivity extends SalesforceDroidGapActivity {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		//super.setIntegerProperty("splashscreen", R.drawable.aau_load);

 		super.onCreate(savedInstanceState);
 	}
 
 	private static final String FEEDBACK_PREFS = "feedback_prefs";
 	private static final String ASK_FEEDBACK_ON_PREF = "AskFeedbackOn";
 	private static final int ASK_FEEDBACK_AFTER_DAYS = 3;
 	
 	private boolean isConnected() {
 	    boolean haveConnectedWifi = false;
 	    boolean haveConnectedMobile = false;
 
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 	    for (NetworkInfo ni : netInfo) {
 	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 	            if (ni.isConnected())
 	                haveConnectedWifi = true;
 	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 	            if (ni.isConnected())
 	                haveConnectedMobile = true;
 	    }
 	    return haveConnectedWifi || haveConnectedMobile;
 	}
 	
 	@Override
 	public void onResume() {
 		this.appView.loadUrl("javascript:aauMobile.init.appActivation();");
 		
 		if(!isConnected()) {
 		  	AlertDialog.Builder adb = new AlertDialog.Builder(this);
 		  	
 		  	adb.setTitle("Error!");
 		  	adb.setMessage("This device is not currently connected to the internet, please restart the application when a connection is available");
 		  	
 		  	new AlertDialog.Builder(this)
   		  	  .setTitle("Error")
    		  	  .setMessage("This device is not currently connected to the internet, please restart the application when a connection is available")
    		  	  .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
    		  		  public void onClick(DialogInterface dialog,
    		  		    int which) {
    				  	endActivity();
    		  		  }
 		  	}).show();
 		} else {
 			
 			// If the user has the app installed for more than 3 days, ask for feedback
 			SharedPreferences settings = getSharedPreferences(
 					FEEDBACK_PREFS,
 					Context.MODE_PRIVATE);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putString(LoginActivity.SERVER_URL_CURRENT_SELECTION,
 			  getString(R.string.sf_default_url));
 			
 			long askFeedbackOn = settings.getLong(ASK_FEEDBACK_ON_PREF, 0);
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DAY_OF_MONTH, ASK_FEEDBACK_AFTER_DAYS);
 
 			if(askFeedbackOn == 0) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, calendar.getTimeInMillis());
 			} else if(askFeedbackOn != -1) {
 				if(askFeedbackOn < System.currentTimeMillis()) {
 					Builder dialogBuilder = new Builder(this);
 					
 					dialogBuilder.setMessage("Would you like to provide feedback on this app?");
 					dialogBuilder.setTitle("Feedback");
 					
 					FeedbackDialogListener listener = new FeedbackDialogListener(this, editor);
 					
 					dialogBuilder.setNegativeButton("Don't ask again", listener);
 					dialogBuilder.setNeutralButton("Not now", listener);
 					dialogBuilder.setPositiveButton("Yes!", listener);
 					
 					dialogBuilder.show();
 				}
 			}
 			
 			editor.commit();
 		}
 		
 		
 		super.onStart();
 	}
 	
 	class FeedbackDialogListener implements DialogInterface.OnClickListener {
 
 		private Context ctx;
 		private SharedPreferences.Editor editor;
 		
 		public FeedbackDialogListener(Context ctx, SharedPreferences.Editor editor) {
 			this.ctx = ctx;
 			this.editor = editor;
 		}
 		
 		public void onClick(DialogInterface dialog, int which) {
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DAY_OF_MONTH, ASK_FEEDBACK_AFTER_DAYS);
 			
 			if(which == DialogInterface.BUTTON_NEGATIVE) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, -1);
 			} else if(which == DialogInterface.BUTTON_NEUTRAL) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, calendar.getTimeInMillis());
 			} else if (which == DialogInterface.BUTTON_POSITIVE) {
 				editor.putLong(ASK_FEEDBACK_ON_PREF, -1);
 				
 				 startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ctx.getPackageName())));
 			}
 			
 			editor.commit();
 		}
 		
 	}
 
 
 }
