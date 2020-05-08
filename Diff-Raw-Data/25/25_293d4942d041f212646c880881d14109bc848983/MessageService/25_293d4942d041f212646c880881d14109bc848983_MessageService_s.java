 /**
  * @author fatih and rishi
  * 
  * Processes the received message and performs actions accordingly. 
  * Actions involve :
  * 		Merge New Manifest File
  * 		Download / Install Apps
  * 		Uninstall Apps (with App data)
  * 		Update Apps
  * 		Start Apps
  * 		Stop Apps
  * 		Start Status Monitor
  */
 
 package edu.buffalo.cse.phonelab.c2dm;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.os.Environment;
 import android.os.SystemClock;
 import android.util.Log;
 import edu.buffalo.cse.phonelab.manifest.PhoneLabApplication;
 import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
 import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
 import edu.buffalo.cse.phonelab.phonelabservices.PeriodicCheckReceiver;
 import edu.buffalo.cse.phonelab.statusmonitor.StatusMonitorReceiver;
 import edu.buffalo.cse.phonelab.utilities.DownloadFile;
 import edu.buffalo.cse.phonelab.utilities.InformServer;
 import edu.buffalo.cse.phonelab.utilities.Locks;
 import edu.buffalo.cse.phonelab.utilities.Util;
 
 public class MessageService extends IntentService {
 	
 	/**
 	 * Class constructor
 	 */
 	public MessageService() {
 		super("MessageService");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Message Service is started!");
 
 		String payload = intent.getExtras().getString("payload");
 		try {
 			JSONObject jsonObject = new JSONObject(payload);
 			String message = (String) jsonObject.get("message");
 			if (message.equals("new_manifest")) {
 				if (DownloadFile.downloadToDirectory(this, Util.MANIFEST_DOWNLOAD_URL + Util.getDeviceId(this), Util.NEW_MANIFEST_DIR)) {
 					PhoneLabManifest newManifest = new PhoneLabManifest(Util.NEW_MANIFEST_DIR, getApplicationContext());
 					if (newManifest.getManifest()) {
 						PhoneLabManifest currentManifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
 						if (currentManifest.getManifest()) {
 							handleExistingManifest(currentManifest, newManifest);
 							try {
 								currentManifest.saveDocument();
 							} catch (TransformerException e) {
 								Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessageAndLocation());
 							}
 						} else {
 							try {
 								FileOutputStream fos = openFileOutput(Util.CURRENT_MANIFEST_DIR, Context.MODE_PRIVATE);
 								FileInputStream newInputStream = openFileInput(Util.NEW_MANIFEST_DIR);
 								byte[] buf = new byte[1024];
 								int len;
 								while ((len = newInputStream.read(buf)) > 0){
 									fos.write(buf, 0, len);
 								}
 								newInputStream.close();
 								fos.close();
 
 								Log.i("PhoneLab-" + getClass().getSimpleName(), "New manifest transfered to Current manifest!");
 								PhoneLabManifest currentManifest2 = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
 								if (currentManifest2.getManifest()) {
 									handleNewManifest(currentManifest2);
 									try {
 										currentManifest2.saveDocument();
 									} catch (TransformerException te) {
 										Log.e("PhoneLab-" + getClass().getSimpleName(), te.getMessageAndLocation());
 									}
 								}
 							} catch (FileNotFoundException fnfe) {
 								Log.e("PhoneLab-" + getClass().getSimpleName(), fnfe.toString());
 							} catch (IOException ioe) {
 								Log.e("PhoneLab-" + getClass().getSimpleName(), ioe.toString());
 							}
 						}
 					}
 
 					//Remove manifest from where it is downloaded
 					removeFileInternalStorage (Util.NEW_MANIFEST_DIR);
 				}
 			} else if (message.equals("get_device_info")) {
 				Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
 				registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
 				registrationIntent.putExtra("sender", Util.C2DM_EMAIL);
 				startService(registrationIntent);
 			} else if (message.equals("get_device_status")) { 	// Send device status to server
 				UploadDeviceStatus uploadDeviceStatus = new UploadDeviceStatus();
 				uploadDeviceStatus.uploadDeviceStatus(getApplicationContext(), Util.DEVICE_STATUS_UPLOAD_URL + Util.getDeviceId(getApplicationContext()));
 			} else if (message.equals("flash")) {
 
 			} else if (message.equals("upload_manifest")) { 	// Send updated manifest to the server
 				
 			} else if (message.equals("uninstall_all_apps")) {
 				uninstallAllApps();
 			} else if (message.equals("remove_manifest")) { 	// delete all apps and the manifest
 				uninstallAllApps();
 				if (deleteFile(Util.CURRENT_MANIFEST_DIR)) {
 					Log.i("PhoneLab-" + getClass().getSimpleName(), "Manifest successfully deleted");
 				} else {
 					Log.e("PhoneLab-" + getClass().getSimpleName(), "Manifest couldn't be deleted");
 				}
 			} else if (message.equals("start_status_monitoring")) { // start SM
 				startStatusMonitor();
 			} else if (message.equals("stop_status_monitoring")) { 	// stop SM
 				try {
 					Intent i1 = new Intent(this, StatusMonitorReceiver.class);
 					PendingIntent pi1 = PendingIntent.getBroadcast(this, 0, i1, PendingIntent.FLAG_NO_CREATE);
 					if (pi1 != null) {
 						Log.i("PhoneLab-" + getClass().getSimpleName(), "Stopping Status Monitoring");
 						AlarmManager amgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
 						amgr.cancel(pi1);
 						pi1.cancel();
 					} else {
 						Log.w("PhoneLab-" + getClass().getSimpleName(), "No Status Monitoring Alarm is currently set");
 					}
 				} catch (Exception e) { 
 					Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				}
 			} else if (message.equals("start_periodic_checking")) { // start checking
 				AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 				Intent newIntent = new Intent(getApplicationContext(), PeriodicCheckReceiver.class);
 				PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 				mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pending);
 				
 			} else if (message.equals("stop_periodic_checking")) { // stop checking
 				AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 				Intent newIntent = new Intent(getApplicationContext(), PeriodicCheckReceiver.class);
 				PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 				mgr.cancel(pending);				
 			} else if (message.equals("remove_logcat_pid")) {//this will remove logcat pid from shared preferences, next time new one wil be created 
 				SharedPreferences settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
 				Editor editor = settings.edit();
 				editor.putInt(Util.SHARED_PREFERENCES_DATA_LOGGER_PID, -1);
 				editor.commit();
 			}
 		} catch (JSONException jsone) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "C2DM Message cannot be parsed to JSON object!");
 		} catch (Exception e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 
 		}
 
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Message Service is done!");
 		
 		Locks.releaseWakeLock();
 	}
 
 	/**
 	 * If there is no old manifest this method will take care of the operations 
 	 * @param currentManifest current Manifest
 	 */
 	private void handleNewManifest(PhoneLabManifest currentManifest) {
 		//Handle applications
 		try {
 		 ArrayList<PhoneLabApplication> applications = currentManifest.getApplicationsByConstraints(null);
 			for (PhoneLabApplication app:applications) {
 				if (app.getAction().equals("update")) {
 					if (!updateApplication(app)) {
 						currentManifest.removeApplication(app.getPackageName());
 					}
 				} else if (app.getAction().equals("uninstall")) {
 					if (removeapplication(app)) {
 						currentManifest.removeApplication(app.getPackageName());
 					}
 				} else if (app.getAction().equals("install")) {
 					if (!installApplication(app)) {
 						currentManifest.removeApplication(app.getPackageName());
 					}
 				} 
 			} 
 		} catch (XPathExpressionException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 		}
 		//Handle Status Monitor
 		try {
 			ArrayList<PhoneLabParameter> parameters = currentManifest.getStatParamaterByConstraints(null);
 			if (parameters.size() > 0) {
 				startStatusMonitor();
 			}
 		} catch (XPathExpressionException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 		}
 	}
 
 	/**
 	 * If there is existing manifest, this method will take care of the operations by comparing with the old one
 	 * @param currentManifest current manifest
 	 * @param newManifest new manifest
 	 */
 	private void handleExistingManifest(PhoneLabManifest currentManifest, PhoneLabManifest newManifest) {
 		//Handle applications
 		try {
 			ArrayList<PhoneLabApplication> applications = newManifest.getApplicationsByConstraints(null);
 			for (PhoneLabApplication app:applications) {
 				HashMap<String, String> constraints = new HashMap<String, String>();
 				constraints.put("package_name", app.getPackageName());
 				ArrayList<PhoneLabApplication> currentApplication = currentManifest.getApplicationsByConstraints(constraints);
 				if (currentApplication != null && currentApplication.size() == 1) {//App exists
 					if (app.getAction().equals("update")) {
 						if (updateApplication(app)) {
 							currentManifest.updateApplication(app);
 						}
 					} else if (app.getAction().equals("uninstall")) {
 						if (removeapplication(app)) {
 							currentManifest.removeApplication(app.getPackageName());
 						}
					} 
 				} else {//No such app exists, must be install
 					if (app.getAction().equals("install")) {
 						if (installApplication(app)) {
 							currentManifest.addApplication(app);
 						}
 					}
 				}
 			} 
 		} catch (XPathExpressionException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 		}
 		//Handle Status Monitor
 		try {
 			ArrayList<PhoneLabParameter> parameters = newManifest.getStatParamaterByConstraints(null);
 			if (parameters.size() > 0) {
 				for (PhoneLabParameter param:parameters) {
 					HashMap<String, String> constraints = new HashMap<String, String>();
 					constraints.put("name", param.getName());
 					ArrayList<PhoneLabParameter> currentParameters = currentManifest.getStatParamaterByConstraints(constraints);
 					if (currentParameters != null && currentParameters.size() == 1) {//Parameter exists
 						currentManifest.updateStatParameter(param);
 					} else {//Parameter does not exist
 						currentManifest.addStatParameters(param);
 					}
 				}
 
 				try {
 					currentManifest.saveDocument();
 				} catch (TransformerException e) {
 					Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				}
 
 				startStatusMonitor();
 			}
 		} catch (XPathExpressionException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 		}
 	}
 
 
 	/**
 	 * This method will uninstall all the applications from both manifest and device
 	 */
 	private void uninstallAllApps() {
 		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
 		if (manifest.getManifest()) {
 			try {
 				ArrayList<PhoneLabApplication> applications = manifest.getAllApplications();
 				for (PhoneLabApplication app:applications) {
 					removeapplication(app);
 					try {
 						manifest.removeApplication(app.getPackageName());
 					} catch (XPathExpressionException e) {
 						Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 					}
 				}
 			} catch (XPathExpressionException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 			}
 
 			try {
 				manifest.saveDocument();
 			} catch (TransformerException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 			}
 		}
 	}
 	
 	/**
 	 * Removes a file whose path is given.
 	 * @param fileName just the name of the file stored in internal storage 
 	 * @return true if success, false o.w
 	 */
 	private boolean removeFileInternalStorage (String fileName) {
 		if (this.deleteFile(fileName)) {
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "File " + fileName + " deleted successfully");
 			return true;
 		} else {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "File " + fileName + " couldn't be deleted");
 			return false;
 		}
 	}
 	
 	/**
 	 * Removes a file whose path is given.
 	 * @param path to the file to be removed
 	 */
 	private boolean removeFile (String path) {
 		File file = new File(path);
 		
 		if (file.delete()) {
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "File at " + path + " deleted successfully");
 			return true;
 		} else {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "File at " + path + " couldn't be deleted");
 			if(file.isDirectory()){
 				//Directories need to be empty
 				Log.e("PhoneLab-" + getClass().getSimpleName(),  path + " leads to a directory. Needs to be empty to be deleted.");
 			}
 			return false;
 		}
 	}
 	
 	/**
 	 * This method install a new application to the phone
 	 * @param app PhoneLabApplication to install
 	 * @return true if successful, otherwise false
 	 */
 	private boolean installApplication (PhoneLabApplication app) {
 		if (DownloadFile.downloadToDirectory(this, Util.APP_DOWNLOAD_URL + app.getAppID() + ".apk", Environment.getExternalStorageDirectory() + "/" + app.getAppID())) {
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "Installing " + app.getName() + " now...");
 			try {
 				Process process = Runtime.getRuntime().exec("pm install " + Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 
 				String line = null;
 				//Success to install APK
 				BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
 				BufferedReader buf_e = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 				while((line = buf_i.readLine()) != null){
 					if(line.compareTo("Success") == 0){
 						if (app.getType().equals("background")) {//start it in the background
 							startingapplication(app);
 						} else if (app.getType().equals("interactive")) {//notify user
 							new Util().nofityUser(this, "PhoneLab", "" + app.getName() + " is installed on your device");
 						}
 						Log.i("PhoneLab-" + getClass().getSimpleName(), app.getName() + " installed successfully");
 
 						//Remove .apk from where it is downloaded
 						removeFile (Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 						InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.SUCCESS);
 						return true;
 					}
 				}
 				buf_i.close();
 
 				//failure to install APK
 				while((line = buf_e.readLine()) != null){
 					if(line.split(" ")[0].compareTo("Failure") == 0){
 						Log.wtf("PhoneLab-" + getClass().getSimpleName(), app.getName() + " couldn't be installed");
 						InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 					}
 				}
 
 				buf_e.close();
 				process.waitFor();
 			} catch (IOException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 			} catch (InterruptedException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 			}
 
 			//Remove .apk from where it is downloaded
 			removeFile (Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 		} else {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "Download .apk failed");
 			InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 		}
 
 		return false;
 	}
 
 	/**
 	 * This method will update the current installed application. If the application doesn't exist, it will just install the new app. 
 	 * @param app PhoneLabApplication to update
 	 * @return true if successful, otherwise false
 	 */
 	private boolean updateApplication(PhoneLabApplication app) {
 		if (DownloadFile.downloadToDirectory(this, Util.APP_DOWNLOAD_URL + app.getName() + ".apk", Environment.getExternalStorageDirectory() + "/" + app.getAppID())) {
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "Updating " + app.getName() + " now...");
 			try {
 				Process process = Runtime.getRuntime().exec("pm -r install " + Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 
 				String line = null;
 				//Success to install APK
 				BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
 				BufferedReader buf_e = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 				while((line = buf_i.readLine()) != null){
 					if(line.compareTo("Success") == 0){
 						if (app.getType().equals("background")) {//start it in the background
 							startingapplication(app);
 						} else if (app.getType().equals("interactive")) {//notify user
 							new Util().nofityUser(this, "PhoneLab", app.getName() + " is updated");
 						}
 						Log.i("PhoneLab-" + getClass().getSimpleName(), app.getName() + " updated successfully");
 
 						//Remove .apk from where it is downloaded
 						removeFile (Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 						InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.SUCCESS);
 						return true;
 					}
 				}
 				buf_i.close();
 
 				//failure to install APK
 				while((line = buf_e.readLine()) != null){
 					if(line.split(" ")[0].compareTo("Failure") == 0){
 						Log.e("PhoneLab-" + getClass().getSimpleName(), app.getName() + " couldn't be updated");
 						InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 
 					}
 				}
 				buf_e.close();
 				process.waitFor();
 			} catch (IOException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 			} catch (InterruptedException e) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 				InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 			}
 
 			//Remove .apk from where it is downloaded
 			removeFile (Environment.getExternalStorageDirectory() + "/" + app.getAppID());
 		} else {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "Download .apk failed");
 			InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.INSTALL,InformServer.FAILURE);
 		}
 
 		return false;
 	}
 
 	/**
 	 * This method uninstall the application
 	 * @param app PhoneLabApplication to uninstall
 	 * @return true if successful, otherwise false
 	 */
 	private boolean removeapplication(PhoneLabApplication app) {
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "Removing " + app.getName() + " now...");
 		try {
 			Process process = Runtime.getRuntime().exec("pm uninstall " + app.getPackageName());
 			String line = null;
 			BufferedReader buf_i = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while((line = buf_i.readLine()) != null){
 				if(line.compareTo("Success") == 0){
 					//Success to uninstall APK
 					new Util().nofityUser(this, "PhoneLab", app.getName() + " is uninstalled");
 					Log.i("PhoneLab-" + getClass().getSimpleName(), app.getName() + " uninstalled successfully");
 					InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.UNINSTALL,InformServer.SUCCESS);
 					return true;
 
 				} else {
 					//failure to uninstall APK
 					Log.e("PhoneLab-" + getClass().getSimpleName(), app.getName() + " couldn't be uninstalled");
 					InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.UNINSTALL,InformServer.FAILURE);
 				}
 			}
 			buf_i.close();
 
 			process.waitFor();
 		} catch (IOException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 			InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.UNINSTALL,InformServer.FAILURE);
 		} catch (InterruptedException e) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
 			InformServer.sendMessage(getApplicationContext(), app.getAppID(),InformServer.UNINSTALL,InformServer.FAILURE);
 		}
 
 		return false;
 	}
 
 	/**
 	 * This method starts status monitoring
 	 * It is called if there is any change on the new manifest
 	 * @param currentManifest 
 	 */
 	private void startStatusMonitor() {
 		AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		Intent newIntent = new Intent(getApplicationContext(), StatusMonitorReceiver.class);
 		PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pending);
 	}
 
 	/**
 	 * Used to send manually start intents using android.intent.action.MAIN. 
 	 * @param app the phonelab Application
 	 * @param dbAdapter the hook to device database          
 	 * @author rishi baldawa
 	 */
 	private void startingapplication(PhoneLabApplication app) {
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "Starting " + app.getName() + " now...");
 
 		if (app.getType().equals("interactive")) {
 			Intent startAppIntent = new Intent(Intent.ACTION_MAIN);
 			PackageManager manager = getPackageManager();
 			try {
 				startAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
 				startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 				startActivity(startAppIntent);
 			} catch( Exception e ) {
 				Log.e("PhoneLab-" + getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
 			}
 		} else if (app.getType().equals("background")) {
 			Intent intent = new Intent(app.getIntentName());
 			startService(intent);
 		}
 
 		return;
 	}
 
 
 	/**
 	 * Used to send manually stop intents using android.intent.action.MAIN. 
 	 * @param app the phonelab Application
 	 * @author rishi baldawa
 	 */
 	@SuppressWarnings("unused")
 	private void stoppingapplication(PhoneLabApplication app) {
 		Log.i("PhoneLab-" + getClass().getSimpleName(), "Stopping " + app.getName() + " now...");
 		Intent stopAppIntent = new Intent(Intent.ACTION_MAIN);
 		PackageManager manager = getPackageManager();
 		try {
 			stopAppIntent = manager.getLaunchIntentForPackage(app.getPackageName());
 			stopAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 			stopService(stopAppIntent);
 		} catch( Exception e ) {
 			Log.e("PhoneLab-" + getClass().getSimpleName(), "The package " + app.getPackageName() + " couldn't be found for the app : " + app.getName());
 		}
 
 		return;
 	}
 }
 
