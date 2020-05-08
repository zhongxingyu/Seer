 package com.alphabetbloc.clinic.services;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.odk.clinic.android.activities.DashboardActivity;
 import org.odk.clinic.android.activities.DownloadPatientActivity;
 import org.odk.clinic.android.activities.ListPatientActivity;
 import org.odk.clinic.android.database.ClinicAdapter;
 
 import android.app.ActivityManager;
 import android.app.Service;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 /**
  * 
  * @author Louis Fazen (louis.fazen@gmail.com) (excerpts from curioustechizen
  *         from stackoverflow)
  * 
  *         This checks the signal strength, data connectivity and user activity
  *         before refreshing the patient list as background service.
  */
 public class SignalStrengthService extends Service {
 
 	private ActivityManager mActivityManager;
 	private TelephonyManager mTelephonyManager;
 	private PhoneStateListener mPhoneStateListener;
 	private static final String TAG = "SignalStrengthService";
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// We don't want to bind; return null.
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Log.e(TAG, "SignalStrengthService Created");
 		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		mPhoneStateListener = new PhoneStateListener() {
 			@Override
 			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
 				int asu = signalStrength.getGsmSignalStrength();
 				// if (asu >= 8 && asu < 32) {
 				// TODO: bring this back in with the ASU
 				// TODO: change the activity that is started...
 				Log.e(TAG, "asu=" + asu);
 				if (networkAvailable()) {
 					Log.e(TAG, "network available");
 					if (refreshClientsNow()) {
 						Log.e(TAG, "refresh now okay");
 						Intent id = new Intent(getApplicationContext(), DownloadPatientActivity.class);
 						id.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 						startActivity(id);
 					}
 					// }
 				}
 				super.onSignalStrengthsChanged(signalStrength);
 			}
 
 			@Override
 			public void onServiceStateChanged(ServiceState serviceState) {
 				// do something
 				Log.d("louis.fazen", "Service State changed! New state = " + serviceState.getState());
 				super.onServiceStateChanged(serviceState);
 			}
 		};
 		super.onCreate();
 	}
 
 	/*
 	 * int iconLevel = -1; if (asu <= 2 || asu == 99) iconLevel = 0; // 0 or 99
 	 * = no signal else if (asu >= 12) iconLevel = 4; // very good signal else
 	 * if (asu >= 8) iconLevel = 3; // good signal else if (asu >= 5) iconLevel
 	 * = 2; // poor signal else iconLevel = 1; // <5 is very poor signal
 	 */
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.e(TAG, "SignalStrengthService Started");
 		boolean max = intent.getBooleanExtra("maximum", false);
 		Log.e(TAG, "boolean extra =" + max);
 		// Register the listener, which effectively starts the service
 		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	private boolean networkAvailable() {
 		boolean dataNetwork = false;
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		if (activeNetworkInfo != null)
 			dataNetwork = true;
 		return dataNetwork;
 	}
 
 	private void updateService() {
 		// if 2G, then update to 3G?
 		int nt = mTelephonyManager.getNetworkType();
 		if (nt < 3)
 			Log.d(TAG, "network type =" + nt);
 
 		/*
 		 * switch (nt) { case 1: return GPRS; case 2: return EDGE; case 3:
 		 * return UMTS; case 8: return HSDPA; case 9: return HSUPA; case 10:
 		 * return HSPA; default: return UNKNOWN; }
 		 */
 
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d(TAG, "Shutting down the Service" + TAG);
 		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
 		super.onDestroy();
 	}
 
 	private boolean refreshClientsNow() {
 		RunningAppProcessInfo info = null;
 		boolean refreshClients = true;
 		String collectPackage = "org.odk.collect.android";
 		String clinicPackage = "org.odk.clinic.android";
 		String createPatientClass = "org.odk.clinic.android.activities.CreatePatientActivity";
 		String listPatientClass = "org.odk.clinic.android.activities.ListPatientActivity";
 
 		if (mActivityManager == null)
 			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		List<RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
 		Iterator<RunningAppProcessInfo> i = l.iterator();
 		while (i.hasNext()) {
 			info = i.next();
 			// If Collect is visible, set refreshClients to false
 			if (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				
				if (info.processName.equals(collectPackage)) 
 					refreshClients = false;
				
 				if (info.processName.equals(clinicPackage)) {
 					ComponentName foreGround = getActivityForApp(info);
					// if (isStillActive(info, foreGround)) { 
					if (foreGround.getClassName().equals(createPatientClass))
						refreshClients = false;
					else if (foreGround.getClassName().equals(listPatientClass) && ListPatientActivity.mListType == DashboardActivity.LIST_SIMILAR_CLIENTS)
						refreshClients = false;
 
 				}
 			}
 
 		}
 		return refreshClients;
 	}
 
 	private RunningAppProcessInfo getForegroundApp() {
 		RunningAppProcessInfo result = null, info = null;
 
 		if (mActivityManager == null)
 			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		// mContext.getSystemService(Context.ACTIVITY_SERVICE);
 		List<RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
 		Iterator<RunningAppProcessInfo> i = l.iterator();
 		while (i.hasNext()) {
 			info = i.next();
 			if (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
 				result = info;
 				break;
 			}
 		}
 		return result;
 	}
 
 	private ComponentName getActivityForApp(RunningAppProcessInfo target) {
 		ComponentName result = null;
 		ActivityManager.RunningTaskInfo info;
 
 		if (target == null)
 			return null;
 
 		if (mActivityManager == null)
 			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		// mContext.getSystemService(Context.ACTIVITY_SERVICE);
 		List<ActivityManager.RunningTaskInfo> l = mActivityManager.getRunningTasks(9999);
 		Iterator<ActivityManager.RunningTaskInfo> i = l.iterator();
 
 		while (i.hasNext()) {
 			info = i.next();
 			if (info.baseActivity.getPackageName().equals(target.processName)) {
 				result = info.topActivity;
 				break;
 			}
 		}
 
 		return result;
 	}
 
 	private boolean isStillActive(RunningAppProcessInfo process, ComponentName activity) {
 		// activity can be null in cases, where one app starts another. for
 		// example, astro
 		// starting rock player when a move file was clicked. we dont have an
 		// activity then,
 		// but the package exits as soon as back is hit. so we can ignore the
 		// activity
 		// in this case
 		if (process == null)
 			return false;
 
 		RunningAppProcessInfo currentFg = getForegroundApp();
 		ComponentName currentActivity = getActivityForApp(currentFg);
 
 		if (currentFg != null && currentFg.processName.equals(process.processName) && (activity == null || currentActivity.compareTo(activity) == 0))
 			return true;
 
 		Log.i("RefreshClientServive", "isStillActive returns false - CallerProcess: " + process.processName + " CurrentProcess: " + (currentFg == null ? "null" : currentFg.processName) + " CallerActivity:" + (activity == null ? "null" : activity.toString())
 				+ " CurrentActivity: " + (currentActivity == null ? "null" : currentActivity.toString()));
 		return false;
 	}
 
 	private boolean isRunningService(String processname) {
 		// if(processname==null || processname.isEmpty())
 		if (processname == null || processname.length() < 1)
 			return false;
 
 		RunningServiceInfo service;
 
 		if (mActivityManager == null)
 			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		// mContext.getSystemService(Context.ACTIVITY_SERVICE);
 		List<RunningServiceInfo> l = mActivityManager.getRunningServices(9999);
 		Iterator<RunningServiceInfo> i = l.iterator();
 		while (i.hasNext()) {
 			service = i.next();
 			if (service.process.equals(processname))
 				return true;
 		}
 
 		return false;
 	}
 
 }
