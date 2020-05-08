 package edu.ucsb.cs290.touch.to.chat;
 
 import android.content.Context;
 import android.content.Intent;
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 
 	@Override
 	protected void onError(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void onMessage(Context arg0, Intent arg1) {
 		Intent i = new Intent(getApplicationContext(), KeyManagementService.class);
 		i.setAction(KeyManagementService.MESSAGE_RECEIVED);
 		i.putExtras(arg1.getExtras());
 		getApplicationContext().startService(i);	
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, String arg1) {
 		Intent i = new Intent(getApplicationContext(), KeyManagementService.class);
 		i.setAction(KeyManagementService.UPDATE_REG);
 		i.putExtra("regID", arg1);
 		getApplicationContext().startService(i);
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		// TODO Auto-generated method stub
 
 	}
 }
