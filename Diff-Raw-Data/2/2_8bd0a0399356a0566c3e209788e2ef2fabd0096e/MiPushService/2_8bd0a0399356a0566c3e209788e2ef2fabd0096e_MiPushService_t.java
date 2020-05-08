 package com.xiaomi.mipush;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.xiaomi.mipush.sdk.MiPushClient;
 
 public class MiPushService {
 	
 	public static final String TOPIC_BROADCAST = "topic_all";
 
 	private static final String TAG = MiPushService.class.getSimpleName();
 
 	private static final String MiPush_APP_ID = "1001139";
 	private static final String MiPush_APP_TOKEN = "800100193139";
 	
 	private static Context context;
 
 	public static void initialize(Context context, MiPushCallback callback) {
 		Log.d(TAG, "initializing");
 		MiPushService.context = context;
 		callback.setCategory(null);
 		callback.setContext(context);
 		MiPushClient.initialize(context, MiPush_APP_ID, MiPush_APP_TOKEN,
 				callback);
 	}
 	
 	public static void subscribe(Context context, String topic) {
 		if (topic != null) {
 			Log.d(TAG, "subscribe topic: " + topic);
 			MiPushClient.subscribe(context, topic, null);
 		}
 	}
 	
 	public static void unsubscribe(Context context, String topic) {
 		if (topic != null) {
 			Log.d(TAG, "unsubscribe topic: " + topic);
 			MiPushClient.unsubscribe(context, topic, null);
 		}
 	}
 	
 	public static void setAlias(Context context, String alias) {
 		if (alias != null) {
 			Log.d(TAG, "set alias: " + alias);
			MiPushClient.setAlias(context, alias, null);
 		}
 	}
 
 }
