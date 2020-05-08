 package io.trigger.forge.android.modules.fixes;
 
import android.app.Activity;
import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager.LayoutParams;
 import io.trigger.forge.android.core.ForgeApp;
 import io.trigger.forge.android.core.ForgeParam;
 import io.trigger.forge.android.core.ForgeTask;
 
 public class API {
 	
	private static void log(String tag, Object msg) {
		Log.i(tag, ""+ msg);
	}
	private static void log(Object message){
		log("default tag", ""+ message);
	}
	
 	public static void lockScreenSize (final ForgeTask task){
 		try {	
			Activity activity = ForgeApp.getActivity();
			final Window window = activity.getWindow();
			final LayoutParams params = window.getAttributes();
			log("input mode before",params.softInputMode);
 			params.softInputMode = LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
			log("input mode after",params.softInputMode);
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					window.setAttributes(params);
					task.success("Yay screen size is fixed");
				}
			});
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 			task.error(e);
 		}
 	}
 
 }
