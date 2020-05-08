 package net.vrallev.android.base;
 
 import android.app.Activity;
 import android.app.Application;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Handler;
 
 import net.vrallev.android.base.util.AndroidServices;
 import net.vrallev.android.base.util.DisplayUtils;
 import net.vrallev.android.base.util.SettingsMgr;
 
 /**
  * 
  * @author Ralf Wondratschek
  *
  */
 public class App extends Application {
 
     public static final boolean DEBUG_CONNECTED = Debug.isDebuggerConnected();
 	
 	private static App instance;
 	private static Handler guiHandler;
 	private static SettingsMgr settingsMgr;
 	
 	/**
 	 * @return The only instance at runtime.
 	 */
 	public static App getInstance() {
 		return instance;
 	}
 
 	/**
 	 * @return A {@link android.os.Handler}, which is prepared for the GUI Thread.
 	 */
 	public static Handler getGuiHandler() {
 		return guiHandler;
 	}
 
     public static void setGuiHandler(Handler handler) {
         guiHandler = handler;
     }
 
 	/**
 	 * @return A singleton to get access to the {@link android.content.SharedPreferences}.
 	 */
 	public static SettingsMgr getSettingsMgr() {
 		return settingsMgr;
 	}
 
     public static void setSettingsMgr(SettingsMgr settingsMgr) {
         App.settingsMgr = settingsMgr;
     }
 
     @Override
 	public void onCreate() {
 		instance = this;
 
 		AndroidServices.init(getApplicationContext());
         DisplayUtils.init(getApplicationContext());
 
         settingsMgr = createSettingsMgr();
 		guiHandler = createGuiHandler();
 		
 		super.onCreate();
 	}
 
     protected SettingsMgr createSettingsMgr() {
         return new SettingsMgr(this);
     }
 
     protected Handler createGuiHandler() {
         return new Handler();
     }
 
    public static abstract class ActivityLifecycleCallbacksAdapter implements ActivityLifecycleCallbacks {
         @Override
         public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
         @Override
         public void onActivityStarted(Activity activity) {}
         @Override
         public void onActivityResumed(Activity activity) {}
         @Override
         public void onActivityPaused(Activity activity) {}
         @Override
         public void onActivityStopped(Activity activity) {}
         @Override
         public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
         @Override
         public void onActivityDestroyed(Activity activity) {}
     }
 }
