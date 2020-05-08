 package com.domomtica.JarviseRemote;
 import com.domotica.JarviseRemote.R;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.view.Gravity;
 import android.widget.Toast;
 
 
 public class NotificationMessageTask extends AsyncTask<String, String, String> {
 	    String toastMessage;
 
 	    @Override
 	    protected String doInBackground(String... params) {
 	        toastMessage = params[0];
 	        return toastMessage;
 	    }
 
 	    protected void OnProgressUpdate(String... values) { 
 	        super.onProgressUpdate(values);
 	    }
 	   // This is executed in the context of the main GUI thread
 	    protected void onPostExecute(String result){
 	    	Notification notification = new Notification(R.drawable.logo, result, System.currentTimeMillis());
 	    	Intent intent = new Intent(AllControlli.thisActivity, MainActivity.class); 
 	    	PendingIntent pIntent = PendingIntent.getActivity(AllControlli.thisActivity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT); 
	    	notification.setLatestEventInfo(AllControlli.thisActivity, "JarviseRemote",result, pIntent);
 	    	NotificationManager mNotificationManager =
 	    		    (NotificationManager) AllControlli.thisActivity.getSystemService(Context.NOTIFICATION_SERVICE);
 	    		// mId allows you to update the notification later on.
 	    		mNotificationManager.notify(1, notification); 
 
 
 	    }}
