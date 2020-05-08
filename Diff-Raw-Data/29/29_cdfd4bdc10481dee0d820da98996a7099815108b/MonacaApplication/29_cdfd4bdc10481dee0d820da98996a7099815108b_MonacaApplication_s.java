 package mobi.monaca.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.component.SpinnerDialog;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentationBuilder;
 import mobi.monaca.framework.psedo.GCMIntentService;
 import mobi.monaca.framework.task.GCMRegistrationIdSenderTask;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.MonacaConst;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Application;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 
 /** This class manage the application's global state and variable. */
 public class MonacaApplication extends Application {
 	private static final String TAG = MonacaApplication.class.getSimpleName();
 	protected static List<MonacaPageActivity> pages = null;
 	protected static Map<String, MenuRepresentation> menuMap = null;
 	protected static MonacaApplication self = null;
 	private SpinnerDialog monacaSpinnerDialog;
     protected static InternalSettings settings = null;
 
 	protected JSONObject appJson;
 
 	private BroadcastReceiver registeredReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String regId = intent.getStringExtra(GCMIntentService.KEY_REGID);
 			sendGCMRegisterIdToAppAPI(regId);
 			unregisterReceiver(this);
 		}
 	};
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public void onCreate() {
 		MyLog.i(TAG, "onCreate()");
 		super.onCreate();
 		self = this;
 
 		registerReceiver(registeredReceiver, new IntentFilter(GCMIntentService.ACTION_GCM_REGISTERED));
 		createMenuMap();
 	}
 
 	protected void loadAppJson() {
 		try {
 			InputStream stream = getResources().getAssets().open("app.json");
 			byte[] buffer = new byte[stream.available()];
 			stream.read(buffer);
 			appJson = new JSONObject(new String(buffer, "UTF-8"));
 			return;
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IllegalArgumentException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 		appJson = new JSONObject();
 	}
 
 	public JSONObject getAppJson() {
 		if (appJson == null) {
 			loadAppJson();
 		}
 		return appJson;
 	}
 
 	protected void createMenuMap() {
 		menuMap = new MenuRepresentationBuilder(getApplicationContext()).buildFromAssets(this, "www/app.menu");
 	}
 
 	public void showMonacaSpinnerDialog(UIContext uiContext, JSONArray args) throws Exception {
 		// dismiss old one if any
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.dismiss();
 		}
 		
 		try {
 			monacaSpinnerDialog = new SpinnerDialog(uiContext, args);
 			monacaSpinnerDialog.setCancelable(true);
 			monacaSpinnerDialog.setOnDismissListener(new OnDismissListener() {
 				
 				@Override
 				public void onDismiss(DialogInterface dialog) {
 					monacaSpinnerDialog = null;
 				}
 			});
 			
 			monacaSpinnerDialog.show();
 		} catch (Exception e) {
 			Log.e("MONACA", e.getMessage());
 			throw e;
 		}
 	}
 
 	public void dismissMonacaSpinnerDialog() {
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.dismiss();
 			
 			monacaSpinnerDialog = null;
 			MyLog.v(TAG, "dismissMonacaSpinnerDialog");
 		}
 	}
 	
 	
 	public void hideMonacaSpinnerDialog(){
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.hide();
 			MyLog.v(TAG, "Hide MonacaSpinnerDialog()");
 		}
 	}
 	
 	public void showMonacaSpinnerDialogIfAny(){
 		if (monacaSpinnerDialog != null) {
 			MyLog.v(TAG, "Show Hidden MonacaSpinnerDialog()");
 			monacaSpinnerDialog.show();
 		}
 	}
 
 	public void updateSpinnerTitle(String title) {
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.updateTitleText(title);
 		}
 	}
 
 	public static boolean allowAccess(String url) {
 
 		if (url.startsWith("file://")) {
 			Context context = self.getApplicationContext();
 
 			try {
 				url = new URI(url).normalize().toString();
 			} catch (Exception e) {
 				MyLog.e(TAG, e.getMessage());
 				return false;
 			}
 
 			if (url.startsWith("file:///android_asset/")) {
 				return true;
 			}
 
 			if (url.startsWith("file://" + context.getApplicationInfo().dataDir)) {
 				return !url.startsWith("file://" + context.getApplicationInfo().dataDir + "/shared_prefs/");
 			}
 
 			// allow access to SD card (some app need access to photos in SD
 			// card)
 			if (url.startsWith("file:///mnt/")) {
 				return true;
 			}
 			if (url.startsWith("file://" + Environment.getExternalStorageDirectory().getPath())) {
 				return true;
 			}
 			return false;
 		}
 
 		return true;
 	}
 
 	/** Add a MonacaPageActivity instance to the page list. */
 	public static void addPage(MonacaPageActivity page) {
 		if (pages == null) {
 			pages = new ArrayList<MonacaPageActivity>();
 		}
 
 		pages.add(page);
 	}
 
 	/** Remove a MonacaPageActivity instance from the page list. */
 	public static void removePage(MonacaPageActivity page) {
 		if (pages != null) {
 			pages.remove(page);
 		}
 	}
 
 	/** Get either MenuRepresentation or null from menu name. */
 	public static MenuRepresentation findMenuRepresentation(String name) {
 		MyLog.v(TAG, "findMenuRepresentation. name:" + name + ", menuMap:" + menuMap);
 		if (menuMap != null) {
 			return menuMap.containsKey(name) ? menuMap.get(name) : null;
 		}
 		return null;
 	}
 
 	/** Get all MonacaPageActivity instances in this application. */
 	public static List<MonacaPageActivity> getPages() {
 		return pages != null ? pages : new ArrayList<MonacaPageActivity>();
 	}
 	
 	/** Get Monaca's internal settings object */
     public static InternalSettings getInternalSettings() {
         if (settings == null) {
             try {
                 settings = new InternalSettings(self.getPackageManager().getApplicationInfo(self.getPackageName(), PackageManager.GET_META_DATA).metaData);
             } catch (Exception e) {
                Log.d(self.getClass().getSimpleName(), "InternalSettings initialization fail", e);
                 settings = new InternalSettings(new Bundle());
             }
             
         }
         return settings;
     }
 
 	@Override
 	public void onTerminate() {
 		MyLog.i(TAG, "onTerminate()");
 		pages = null;
 		menuMap = null;
 		self = null;
 
 		super.onTerminate();
 	}
 
 	public String getPushProjectId() {
 		String pushProjectId = "";
 		if (getAppJson().has("pushNotification")) {
 			try {
 				JSONObject pathNotification = appJson.getJSONObject("pushNotification");
 				pushProjectId = pathNotification.getString("pushProjectId");
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return pushProjectId;
 	}
 
 	public void sendGCMRegisterIdToAppAPI(String regId) {
 		new GCMRegistrationIdSenderTask(this, MonacaConst.getPushRegistrationAPIUrl(this, getPushProjectId()), regId) {
 			@Override
 			protected void onSucceededRegistration(JSONObject resultJson) {
 			}
 
 			@Override
 			protected void onFailedRegistration(JSONObject resultJson) {
 			}
 
 			@Override
 			protected void onClosedTask() {
 			}
 		}.execute();
 	}
     
 }
