 package mobi.monaca.framework.psedo;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.task.GCMUnregistrationTask;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.gcm.GCMPushDataset;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
import android.os.Build;
 import android.os.Bundle;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	private static final String TAG = GCMIntentService.class.getSimpleName();
 
 	public static final String ACTION_GCM_REGISTERED = "gcm_registered";
 	public static final String KEY_REGID = "gcm_registered_regid";
	
    @Override
    protected String[] getSenderIds(Context context) {
        String[] ids = new String[1];
        ids[0] = ((MonacaApplication)getApplication()).getAppJsonSetting().getSenderId();
        return ids;
    }
	
 	@Override
 	protected void onError(Context arg0, String arg1) {
 		MyLog.d(TAG, "onError :" + arg1);
 	}
 
 	@Override
 	protected void onMessage(Context arg0, Intent arg1) {
 		MyLog.d(TAG, "onMessage");
 		Bundle b = arg1.getExtras();
 		if (b == null) {
 			return;
 		}
 		Context context = arg0.getApplicationContext();
 		String message = b.getString("message");
 		String pushProjectId = b.getString("push_project_id");
 		String extraJsonString = b.getString("extra_json");
 		GCMPushDataset data = new GCMPushDataset(pushProjectId, extraJsonString);
 
 		int id = (int)System.currentTimeMillis();
 		String title = b.getString("title") != null ? b.getString("title") : getString(R.string.app_name) + " Received Push";
 		Intent intent = new Intent(context, mobi.monaca.framework.MonacaNotificationActivity.class);
 		intent.putExtra(GCMPushDataset.KEY, data);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent.setAction("dummy_action" + id);// prevent from being discarded by system
 		PendingIntent pending = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		Notification notification = new Notification();
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notification.icon = R.drawable.icon;
 		notification.tickerText = title;
 		notification.setLatestEventInfo(context, title, message, pending);
 
 		NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.notify(id, notification);
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, String arg1) {
 		MyLog.d(TAG, "onReceive :" +arg0 );
 		Intent i = new Intent();
 		i.setAction(ACTION_GCM_REGISTERED);
 		i.putExtra(KEY_REGID, arg1);
 		sendBroadcast(i); // received by MonacaApplication
 		//((MonacaApplication)getApplication()).sendGCMRegisterIdToAppAPI(arg1);//cannnot use this for handler dead thread sending
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		MyLog.d(TAG, "onUnregistered :" + arg1);
 
 		new GCMUnregistrationTask((MonacaApplication)getApplication(), arg1).execute();
 	}
 }
