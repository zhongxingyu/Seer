 //liuchong@baixing.com
 package com.baixing.util;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Toast;
 
 import com.baixing.activity.BaiduMapActivity;
 import com.baixing.activity.BaseActivity;
 import com.baixing.activity.BaseFragment;
import com.baixing.activity.EntryApplication;
 import com.baixing.broadcast.NotificationIds;
 import com.baixing.entity.Ad;
 import com.baixing.entity.Ad.EDATAKEYS;
 import com.baixing.tracking.Tracker;
 import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.tracking.TrackConfig.TrackMobile.Key;
 import com.baixing.view.fragment.FeedbackFragment;
 import com.baixing.widget.CommentsDialog;
 import com.quanleimu.activity.R;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class ViewUtil {
 	
 	public static void postShortToastMessage(final View parent, final String message, long delay)
 	{
 		if (parent == null || message == null)
 		{
 			return;
 		}
 		
 		final long delayTime = delay > 0 ? delay : 0;
 		
 		parent.postDelayed(new Runnable() {
 
 			public void run() {
 //				Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT).show();
 				Toast t = Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT);
 				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 				t.show();
 			}
 			
 		}, delayTime);
 	}
 	
 	public static void postShortToastMessage(final View parent,final int resId, long delay)
 	{
 		if (parent == null)
 		{
 			return;
 		}
 		
 		final long delayTime = delay > 0 ? delay : 0;
 		
 		parent.postDelayed(new Runnable() {
 
 			public void run() {
 				Toast t = Toast.makeText(parent.getContext(), resId, Toast.LENGTH_SHORT);
 				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 				t.show();
 			}
 			
 		}, delayTime);
 	}
 	
 	public static void removeNotification(Context context, final int notificationid)
 	{
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
 		wk.acquire(1000);
 		
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.cancel(notificationid);
 	}
 	
 	public static void putOrUpdateNotification(final Context context, final int notificationId, String notificationType, String title, String msg, final Bundle extras, final boolean isPersistant)
 	{
 		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
 		wk.acquire(1000);
 		
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		String tickerText = msg;//message.getMessage();
 		int icon = R.drawable.push_icon;
 
 		String contentTitle = title == null ? "" : title;
 		String contentText = msg;
 		
 		Notification notification = new Notification(icon, tickerText,
 				System.currentTimeMillis());
 		notification.defaults |= Notification.DEFAULT_SOUND;
 		notification.defaults |= Notification.DEFAULT_VIBRATE;
 		if (isPersistant){
 			notification.flags |= Notification.FLAG_NO_CLEAR;
 		}
 		else{
 			notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		}
 		
 		Intent notificationIntent = notificationType == null ? new Intent() : new Intent(notificationType);
 		if(notificationType.equals(Intent.ACTION_VIEW)){
 			if(extras != null){
 				String data = extras.getString("data");
 				JSONObject obj;
 				try {
 					obj = new JSONObject(data);
 					String url = obj.getString("url");
 					notificationIntent.setData(Uri.parse(url));
 					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
 					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 					mNotificationManager.notify(notificationId, notification);
 					return;
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		if (extras != null)
 		{
 			notificationIntent.putExtras(extras);
 		}
 		if(notificationId == NotificationIds.NOTIFICATION_ID_BXINFO){
 			notificationIntent.putExtra("pagejump", true);
			EntryApplication.pushViewed = false;
 		}
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
 
 		notification.setLatestEventInfo(context, contentTitle, contentText,
 				contentIntent);
 		
 		mNotificationManager.notify(notificationId, notification);
 	}
 	
 	public static void startMapForAds(Context context, Ad ad) {
 		
 		if (ad == null)
 		{
             Toast.makeText(context, "无信息无法显示地图", 1).show();
 			return;
 		}
 		
 //		if(keepSilent) { //FIXME:
 //            Toast.makeText(getActivity(), "当前无法显示地图", 1).show();
 //            return;
 //        }
 		
 		final BaseActivity baseActivity = (BaseActivity)context; //FIXME: should not be activity.
 		if (baseActivity != null){
 			if (Build.VERSION.SDK_INT >  16)//Fix baidu map SDK crash on android4.2 device.
 			{
 				final String latV = ad.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LAT);
 				final String lonV = ad.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LON);
 				String query = null;
 				if(latV != null && !latV.equals("false") && !latV.equals("") && !latV.equals("0") && lonV != null && !lonV.equals("false") && !lonV.equals("") && !lonV.equals("0"))
 				{
 					query = latV + "," + lonV;
 				}
 				else
 				{
 					String area = ad.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
 					String address = ad.getMetaValueByKey("具体地点");
 					if (address != null && address.trim().length() > 0)
 					{
 						query = address.trim();
 					}
 					else if (area != null && area.trim().length() > 0)
 					{
 						query = area.trim();
 					}
 				}
 				
 				if (query != null)
 				{
 					Intent intent = new Intent(Intent.ACTION_VIEW);
 					intent.setData(Uri.parse("http://maps.google.com/?q=" + query));
 					context.startActivity(intent);
 				}
 			}
 			else
 			{
 				Bundle bundle = new Bundle();
 				bundle.putSerializable("detail", ad);
 				baseActivity.getIntent().putExtras(bundle);
 				
 				baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
 				baseActivity.startActivity(baseActivity.getIntent());
 			}
 			
 		} else {
             Toast.makeText(context, "显示地图失败", Toast.LENGTH_SHORT).show();
         }
 	}
 	
 	static public Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight)
 	{
 		Float width  = Float.valueOf(srcBmp.getWidth());
 		Float height = Float.valueOf(srcBmp.getHeight());
 		Float ratio = width/height;
 		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);
 
 		return thumbnail;
 	}
 	
 	static public ProgressDialog showSimpleProgress(Context context){
 		ProgressDialog pd = 
 				ProgressDialog.show(context, context.getString(R.string.dialog_title_info), context.getString(R.string.dialog_message_waiting));
 		pd.setCancelable(true);
         pd.setCanceledOnTouchOutside(true);
         return pd;
 	}
 	
 	static public void hideSimpleProgress(ProgressDialog pd){
 		if(pd != null){
 			pd.dismiss();
 		}
 	}
 	
 	static public void showToast(final Context context, final String msg, final boolean longDuration){
 		if (context == null || msg == null) {
 			return;
 		}
 		
 		Runnable task = new Runnable(){
 			@Override
 			public void run(){
 				Toast t = Toast.makeText(context, msg, longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
 				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 				t.show();
 			}
 		};
 		
 		if (context instanceof Activity) {
 			((Activity) context).runOnUiThread(task);
 		} else {
 			task.run();
 		}
 		
 	}
 	
 	static private boolean commentsDlgShowed = false; 
 	static public void showCommentsPromptDialog(final BaseActivity activity){
 		if(commentsDlgShowed) return;
 		commentsDlgShowed = true;
         AlertDialog.Builder builder = new AlertDialog.Builder(activity);
         builder.setTitle("百姓网好用么？")
         		.setMessage("感谢您使用了这么久百姓网，不知道您的感受如何？您的反馈是我们不断为您改进的动力")
                 .setPositiveButton("很爽，赞一下", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                     	dialogInterface.dismiss();
                     	(new CommentsDialog(activity)).show();
                     	Tracker.getInstance().event(BxEvent.REVIEW_ACTION).append(Key.ACTION, "appMarket").end();
                     }
                 })
                 .setNegativeButton("不爽，我要告状", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                     	dialog.dismiss();
                     	Bundle arg = new Bundle();
                     	arg.putString(BaseFragment.ARG_COMMON_TITLE, "反馈信息");
                     	activity.pushFragment(new FeedbackFragment(), arg, false);
                     	Tracker.getInstance().event(BxEvent.REVIEW_ACTION).append(Key.ACTION, "feedback").end();
                     }
                 }).create().show();        
 	}
 }
