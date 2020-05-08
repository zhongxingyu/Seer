 package io.trigger.forge.android.modules.fixes;
 
 import android.view.Window;
 import android.view.WindowManager.LayoutParams;
 import io.trigger.forge.android.core.ForgeApp;
 import io.trigger.forge.android.core.ForgeParam;
 import io.trigger.forge.android.core.ForgeTask;
 
 public class API {
 	
 	public static void lockScreenSize (final ForgeTask task){
 		try {	
			LayoutParams params = ForgeApp.getActivity()
								.getWindow()
								.getAttributes();
 			params.softInputMode = LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
			task.success();
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 			task.error(e);
 		}
 	}
 
 }
