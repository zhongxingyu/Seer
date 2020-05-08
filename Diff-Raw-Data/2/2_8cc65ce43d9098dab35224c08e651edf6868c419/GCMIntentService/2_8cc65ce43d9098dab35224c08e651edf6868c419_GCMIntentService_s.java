 package edu.ucsb.cs290.touch.to.chat;
 
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.content.LocalBroadcastManager;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 
 	private void handleRegistration(String regID) {
 		//if (KeyManagementService.getStatic() == null) {
 			this.getSharedPreferences("touchToTextPreferences.xml",
 					MODE_PRIVATE).edit().putBoolean("GCM ready", true).commit();
 //		} else {
 //			Intent i = new Intent(getApplicationContext(),
 //					KeyManagementService.class);
 //			i.setAction(KeyManagementService.UPDATE_REG);
 //			LocalBroadcastManager.getInstance(getApplicationContext())
 //					.sendBroadcastSync(i);
 //		}
 	}
 
 	@Override
 	protected void onError(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void onMessage(Context arg0, Intent arg1) {
 		Intent i = new Intent(getApplicationContext(), KeyManagementService.class);
 		i.setAction(KeyManagementService.MESSAGE_RECEIVED);
 		i.putExtras(arg1.getExtras());
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(arg1);
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 		handleRegistration(arg1);
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 
 	}
 }
