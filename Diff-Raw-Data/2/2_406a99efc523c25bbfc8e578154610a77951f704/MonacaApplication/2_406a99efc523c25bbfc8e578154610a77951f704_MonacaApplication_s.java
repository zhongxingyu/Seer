 package mobi.monaca.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.component.SpinnerDialog;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentationBuilder;
 import mobi.monaca.framework.psedo.GCMIntentService;
 import mobi.monaca.framework.task.GCMRegistrationIdSenderTask;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.MonacaConst;
 import mobi.monaca.utils.MonacaDevice;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.Application;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 
 /** This class manage the application's global state and variable. */
 public class MonacaApplication extends Application {
 	private static final String TAG = MonacaApplication.class.getSimpleName();
 	protected static List<MonacaPageActivity> pages = null;
 	protected static Map<String, MenuRepresentation> menuMap = null;
 	private SpinnerDialog monacaSpinnerDialog;
     protected static InternalSettings settings = null;
 
     protected AppJsonSetting appJsonSetting;
 
     /**
      * if this is false, this app never use bootloader in any cases
      */
     protected boolean enablesBootloader = true;
 
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
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
 	@Override
 	public void onCreate() {
 		MyLog.i(TAG, "onCreate()");
 		if (Build.VERSION.SDK_INT  >= 12) {
 			CookieManager.setAcceptFileSchemeCookies(true);
 		}
 		CookieSyncManager.createInstance(this);
 		super.onCreate();
 
 		registerReceiver(registeredReceiver, new IntentFilter(GCMIntentService.ACTION_GCM_REGISTERED));
 		createMenuMap();
 	}
 
 	public void loadAppJsonSetting() {
 		JSONObject appJson = null;
 		try {
 			InputStream stream = getResources().getAssets().open("app.json");
 			byte[] buffer = new byte[stream.available()];
 			stream.read(buffer);
 			appJson = new JSONObject(new String(buffer, "UTF-8"));
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IllegalArgumentException e) {
 			MyLog.e(TAG, e.getMessage());
 		}catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		if (appJson == null) {
 			appJson = new JSONObject();
 		}
 
 		appJsonSetting = new AppJsonSetting(appJson);
 
 		boolean disableCookie = appJsonSetting.getDisableCookie();
 		CookieManager.getInstance().setAcceptCookie(!disableCookie);
 
 		if (!disableCookie) {
 			CookieSyncManager.getInstance().startSync();
 			String assetUrl = appJsonSetting.shouldExtractAssets() || needToUseBootloader() ? "file:///data/" : "file:///android_asset/www/";
 			//MyLog.d(TAG, projectUrl);
 			CookieManager.getInstance().setCookie(assetUrl, "MONACA_CLOUD_DEVICE_ID=" + MonacaDevice.getDeviceId(this));
 			CookieSyncManager.getInstance().sync();
 
 			CookieManager.getInstance().setCookie(assetUrl, "Domain=" + appJsonSetting.getMonacaCloudDomain());
 			CookieSyncManager.getInstance().sync();
 
 			CookieManager.getInstance().setCookie(assetUrl, "path=" + appJsonSetting.getMonacaCloudPath());
 			CookieSyncManager.getInstance().sync();
 
 			CookieManager.getInstance().setCookie(assetUrl, "secure");
 			CookieSyncManager.getInstance().sync();
 		}
 	}
 
 	public AppJsonSetting getAppJsonSetting() {
 		if (appJsonSetting == null) {
 			loadAppJsonSetting();
 		}
 		return appJsonSetting;
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
                     // this causes duplication of dialog
                     // if (monacaSpinnerDialog != null) {
                     // monacaSpinnerDialog = null;
                     // }
                 }
             });
             monacaSpinnerDialog.setOnCancelListener(new OnCancelListener() {
                 @Override
                 public void onCancel(DialogInterface dialog) {
                    if (monacaSpinnerDialog != null) {
                        monacaSpinnerDialog = null;
                    }
                 }
             });
             monacaSpinnerDialog.show();
         } catch (Exception e) {
             Log.e("MONACA", e.getMessage());
             throw e;
         }
 
     }
 
     public void dismissMonacaSpinnerDialog() {
         if (monacaSpinnerDialog != null) {
             if (monacaSpinnerDialog.isShowing()) {
                 monacaSpinnerDialog.dismiss();
             }
             monacaSpinnerDialog = null;
         }
     }
 	
 	
 	public void hideMonacaSpinnerDialog(){
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.hide();
 		}
 	}
 	
 	public void showMonacaSpinnerDialogIfAny(){
 		if (monacaSpinnerDialog != null) {
 			monacaSpinnerDialog.show();
 		}
 	}
 
 	public void updateSpinnerTitle(String title) {
 		if (monacaSpinnerDialog != null && monacaSpinnerDialog.isShowing()) {
 			monacaSpinnerDialog.updateTitleText(title);
 		}
 	}
 
 	public boolean allowAccess(String url) {
 
 		if (url.startsWith("file://")) {
 			Context context = this.getApplicationContext();
 
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
 		if (menuMap != null) {
 			return menuMap.containsKey(name) ? menuMap.get(name) : menuMap.get("default");
 		}
 		return null;
 	}
 
 	/** Get all MonacaPageActivity instances in this application. */
 	public static List<MonacaPageActivity> getPages() {
 		return pages != null ? pages : new ArrayList<MonacaPageActivity>();
 	}
 	
 	/** Get Monaca's internal settings object */
     public InternalSettings getInternalSettings() {
         if (settings == null) {
             try {
                 settings = new InternalSettings(this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData);
             } catch (Exception e) {
                 MyLog.d(this.getClass().getSimpleName(), "InternalSettings initialization fail", e);
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
 
 		super.onTerminate();
 	}
 
 	public String getPushProjectId() {
		return appJsonSetting.getPushProjectId();
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
 
 	public boolean enablesBootloader() {
 		return enablesBootloader;
 	}
 
 	/**
 	 * should this app use bootloader when initializing
 	 * @return
 	 */
 	public boolean needToUseBootloader() {
 		return enablesBootloader && LocalFileBootloader.needToUseLocalFileBootloader();
 	}
 }
