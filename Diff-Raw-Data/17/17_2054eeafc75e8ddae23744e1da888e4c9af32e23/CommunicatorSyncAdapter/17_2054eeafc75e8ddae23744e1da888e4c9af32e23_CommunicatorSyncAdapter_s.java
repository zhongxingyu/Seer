 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.trentorise.smartcampus.communicator.syncadapter;
 
 import java.util.List;
 
 import android.accounts.Account;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SyncResult;
 import android.os.Bundle;
 import android.util.Log;
 import eu.trentorise.smartcampus.android.common.GlobalConfig;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.data.CommunicatorHelper;
 import eu.trentorise.smartcampus.communicator.custom.data.Constants;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.storage.sync.SyncData;
 import eu.trentorise.smartcampus.storage.sync.SyncStorage;
 
 /**
  * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
  * platform ContactOperations provider.
  */
 public class CommunicatorSyncAdapter extends AbstractThreadedSyncAdapter {
     private static final String TAG = "CommunicatorSyncAdapter";
 
     private final Context mContext;
 
     public CommunicatorSyncAdapter(Context context, boolean autoInitialize) {
         super(context, autoInitialize);
         mContext = context;
         CommunicatorHelper.init(mContext);
         try {
 			CommunicatorHelper.start(true);
 		} catch (Exception e) {
 			Log.e(TAG,"Failed to instantiate SyncAdapter: "+e.getMessage());
 		}
     }
 
     @Override
     public void onPerformSync(Account account, Bundle extras, String authority,
         ContentProviderClient provider, SyncResult syncResult) {
     	 try {
  			Log.e(TAG, "Trying synchronization");
 			SyncStorage storage = CommunicatorHelper.getSyncStorage();
 			SyncData data = storage.synchronize(CommunicatorHelper.getAuthToken(), GlobalConfig.getAppUrl(mContext), Constants.SYNC_SERVICE);
 			if (data.getUpdated() != null && !data.getUpdated().isEmpty() && data.getUpdated().containsKey(eu.trentorise.smartcampus.communicator.model.Notification.class.getCanonicalName()))
 					onDBUpdate(data.getUpdated().get(eu.trentorise.smartcampus.communicator.model.Notification.class.getCanonicalName()));
 		}  catch (SecurityException e) {
 			handleSecurityProblem();
 		} catch (Exception e) {
 			Log.e(TAG, "on PerformSynch Exception: "+ e.getMessage());
 		}
     }
     
     private void handleSecurityProblem() {
         Intent i = new Intent("eu.trentorise.smartcampus.START");
         i.setPackage(mContext.getPackageName());
 
         CommunicatorHelper.getAccessProvider().invalidateToken(mContext, null);
         
         NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
         
         int icon = R.drawable.stat_notify_error;
         CharSequence tickerText = mContext.getString(eu.trentorise.smartcampus.ac.R.string.token_expired);
         long when = System.currentTimeMillis();
         CharSequence contentText =  mContext.getString(eu.trentorise.smartcampus.ac.R.string.token_required);
         PendingIntent contentIntent = PendingIntent.getActivity( mContext, 0, i, 0);
 
         Notification notification = new Notification(icon, tickerText, when);
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notification.setLatestEventInfo( mContext, tickerText, contentText, contentIntent);
         
         mNotificationManager.notify(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NOTIFICATION_ID, notification);
 	}
     
     private void onDBUpdate(List<Object> list) {
         Intent i = new Intent("eu.trentorise.smartcampus.START");
         i.setPackage(mContext.getPackageName());
 
         NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        
         int icon = R.drawable.ic_n;
         
         CharSequence tickerText = extractTitle(list);
         long when = System.currentTimeMillis();
         CharSequence contentText = extractText(list);
         PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, i, 0);
 
         Notification notification = new Notification(icon, tickerText, when);
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notification.setLatestEventInfo(mContext, tickerText, contentText, contentIntent);
         
         mNotificationManager.notify(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NOTIFICATION_ID, notification);
 	}
 
 	private CharSequence extractTitle(List<Object> list) {
 		return format(list, eu.trentorise.smartcampus.communicator.R.string.notification_title, eu.trentorise.smartcampus.communicator.R.string.notification_title_multi);
 	}
 	private CharSequence extractText(List<Object> list) {
 		return format(list, eu.trentorise.smartcampus.communicator.R.string.notification_text, eu.trentorise.smartcampus.communicator.R.string.notification_text_multi);
 	}
 	private CharSequence format(List<Object> list, int res, int resMulti) {
 		String txt = "";
 		if (list.size() == 1) {
			String title = ((eu.trentorise.smartcampus.communicator.model.Notification)list.get(0)).getTitle();
 			txt = mContext.getString(eu.trentorise.smartcampus.communicator.R.string.notification_title) + " "+title;
 		}
 		else txt = list.size() + " " + mContext.getString(eu.trentorise.smartcampus.communicator.R.string.notification_title_multi);
 		return txt;
 	}
 }
