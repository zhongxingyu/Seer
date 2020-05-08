 package com.hypermurea.hslpushdroid.gcm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 import com.google.inject.Inject;
 import com.hypermurea.hslpushdroid.DisruptionNotifier;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	
 	private static final String TAG ="hslpushdroid.GCMBaseIntentService";
 	
     @Inject protected static DisruptionNotifier disruptionNotifier;
 
 	@Override
 	protected void onError(Context arg0, String arg1) {
		Log.e(TAG, "onError invoked");
 	}
 
 	@Override
 	protected void onMessage(Context ctx, Intent intent) {
 		Log.e(TAG, "onMessage invoked");
 		
 		final String TRAFFIC_DISRUPTION_DESCRIPTION = "desc";
 		wakeUpNotification(ctx, "HSLPush", intent.getExtras().getString(TRAFFIC_DISRUPTION_DESCRIPTION));
 	}
 	
 	private void wakeUpNotification(Context ctx, String from, String message) {
 		disruptionNotifier.wakeDeviceAndNotify(ctx,  from, message);
 	}
 
 	@Override
 	protected void onRegistered(Context ctx, String registrationId) {
 		broadcastRegistrationId(ctx, registrationId);
 	}
 	
 	public static void broadcastRegistrationId(Context ctx, String registrationId) {
 		Intent intent = new Intent();
 		intent.setAction(GCMRegistrationService.REGISTERED_ACTION);
 		intent.putExtra(GCMRegistrationService.REGISTRATION_ID, registrationId);
 		Log.d(TAG, "registrationId acquired: " + registrationId);
 		ctx.sendBroadcast(intent);
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 		Log.e(TAG, "onUnregistered invoked");
 	}
 
 }
