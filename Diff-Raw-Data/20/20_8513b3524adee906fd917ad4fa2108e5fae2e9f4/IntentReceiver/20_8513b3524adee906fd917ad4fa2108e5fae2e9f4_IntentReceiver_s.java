 package org.metawatch.manager.emailhack;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 public class IntentReceiver extends BroadcastReceiver  {
 
 	static List<String> shown_widgets = null;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 		if (Main.log) Log.d(Main.TAG, "onReceive() " + action);
 		
 		if (action == null) {
 			return;
 		}
 		
 		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
 			if (!DatabaseService.isRunning()) context.startService(new Intent(context, DatabaseService.class));
 			
 		} else if (action.equals("org.metawatch.manager.REFRESH_WIDGET_REQUEST")) {
 			Bundle bundle = intent.getExtras();
 
 			boolean getPreviews = bundle.containsKey("org.metawatch.manager.get_previews");
 			if (getPreviews)
 				if (Main.log) Log.d(Main.TAG, "get_previews");
 
 			if (bundle.containsKey("org.metawatch.manager.widgets_desired")) {
 				if (Main.log) Log.d(Main.TAG, "widgets_desired");
 				shown_widgets = Arrays.asList(bundle.getStringArray("org.metawatch.manager.widgets_desired"));
 			}
 
			ManagerApi.updateWidget(context, getPreviews);
 		}
 		
 	}
 
 }
