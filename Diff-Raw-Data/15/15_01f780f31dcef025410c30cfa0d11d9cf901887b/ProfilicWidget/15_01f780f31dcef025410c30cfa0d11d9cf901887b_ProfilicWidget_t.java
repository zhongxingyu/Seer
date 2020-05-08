 package com.mridang.profilic;
 
 import java.lang.reflect.Method;
 import java.util.Random;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class ProfilicWidget extends DashClockExtension {
 
 	/* This is the instance of the receiver that deals with profile status */
 	private ToggleReceiver objProfilesReceiver;
 
 	/*
 	 * This class is the receiver for getting hotspot toggle events
 	 */
 	private class ToggleReceiver extends BroadcastReceiver {
 
 		/*
 		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
 		 */
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			onUpdateData(1);
 
 		}
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
 	 */
 	@Override
 	protected void onInitialize(boolean booReconnect) {
 
 		super.onInitialize(booReconnect);
 
 		if (objProfilesReceiver != null) {
 			try {
 
 				Log.d("ProfilicWidget", "Unregistered any existing status receivers");
 				unregisterReceiver(objProfilesReceiver);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		objProfilesReceiver = new ToggleReceiver();
 		registerReceiver(objProfilesReceiver, new IntentFilter("android.app.profiles.PROFILES_STATE_CHANGED"));
 		Log.d("ProfilicWidget", "Registered the status receiver");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("ProfilicWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "4a2e84b0");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		Log.d("ProfilicWidget", "Getting the currently activated profile");
		ExtensionData edtInformation = new ExtensionData();		
		setUpdateWhenScreenOn(true);
 
 		try {
 
 			Log.d("ProfilicWidget", "Checking if profiles are on");
 			if (Settings.System.getInt(getContentResolver(), "system_profiles_enabled", 1) == 1) { 
 
 				Log.d("ProfilicWidget", "Profiles are activated");
 				Object o = getSystemService("profile");
 				try {
 
 					Log.d("ProfilicWidget", "Fetching the profile name and ringer mode");
 					Class<?> ProfileManager = Class.forName("android.app.ProfileManager");
 					Class<?> Profile = Class.forName("android.app.Profile");
 
 					Method getActiveProfile = ProfileManager.getDeclaredMethod("getActiveProfile", null);
 					Method getName = Profile.getDeclaredMethod("getName", null);
 
 					switch (((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode()) {
 					case AudioManager.RINGER_MODE_SILENT:
 						Log.d("ProfilicWidget", "Ringer is off");
 						edtInformation.visible(true);
 						edtInformation
 						.status(getString(R.string.current_profile)
 								+ " \u2014 " + (String) getName
 								.invoke(getActiveProfile
 										.invoke(o)));
 						edtInformation.expandedBody(getString(R.string.ringer_silent));
 						edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
 						break;
 
 					case AudioManager.RINGER_MODE_VIBRATE:
 						Log.d("ProfilicWidget", "Vibration is on");
 						edtInformation.visible(true);
 						edtInformation
 						.status(getString(R.string.current_profile)
 								+ " \u2014 " + (String) getName
 								.invoke(getActiveProfile
 										.invoke(o)));
 						edtInformation.expandedBody(getString(R.string.ringer_vibrate));
 						edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
 						break;
 
 					case AudioManager.RINGER_MODE_NORMAL:
 						Log.d("ProfilicWidget", "Ringer is normal");
 						edtInformation.visible(true);
 						edtInformation
 						.status(getString(R.string.current_profile)
 								+ " \u2014 " + (String) getName
 								.invoke(getActiveProfile
 										.invoke(o)));
 						edtInformation.expandedBody(getString(R.string.ringer_normal));
 						edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
 						break;
 					}
 
 				} catch (NoSuchMethodException e) {
					edtInformation.visible(false);
					Log.e("ProfilicWidget", "Possibly not using Cyanogenmod", e);
 					Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
 				} catch (ClassNotFoundException e) {
					edtInformation.visible(false);
					Log.e("ProfilicWidget", "Possibly not using Cyanogenmod", e);
 					Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
 				} catch (Exception e) {
 					Log.e("ProfilicWidget", "Encountered an error", e);
 					BugSenseHandler.sendException(e);
 				}
 
 			} else {
 				Log.d("ProfilicWidget", "Profiles are disabled");
 			}
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 
 					for (PackageInfo pkgPackage : mgrPackages.getInstalledPackages(0)) {
 
 						intExtensions = intExtensions + (pkgPackage.applicationInfo.packageName.startsWith("com.mridang.") ? 1 : 0); 
 
 					}
 
 					if (intExtensions > 1) {
 
 						edtInformation.visible(true);
 						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
 						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
 						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 
 					}
 
 				}
 
 			} else {
 				setUpdateWhenScreenOn(true);
 			}
 
 		} catch (Exception e) {
			edtInformation.visible(false);
 			Log.e("HotspotWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("ProfilicWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 
 		if (objProfilesReceiver != null) {
 
 			try {
 
 				Log.d("ProfilicWidget", "Unregistered the status receiver");
 				unregisterReceiver(objProfilesReceiver);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		Log.d("ProfilicWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
