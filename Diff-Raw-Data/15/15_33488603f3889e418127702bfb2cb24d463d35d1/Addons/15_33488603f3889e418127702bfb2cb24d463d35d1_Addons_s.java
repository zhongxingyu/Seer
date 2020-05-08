 package com.t3hh4xx0r.addons;
 
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.HttpURLConnection;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Build;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import android.util.DisplayMetrics;
 
 import android.util.Log;
 import android.util.Slog;
 
 public class Addons extends PreferenceActivity {
 	public static String DATE = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(new Date());
 
 	private boolean DBG = true;
 	
 	private String TAG = "Addons";
 	//Constants for addons, ties to android:key value in addons.xml
         private static final String GOOGLE_APPS = "google_apps_addon";
 	private static final String SBC1 = "sbc_1";
 	private static final String OMFT = "omft";
 
         private static final String DOWNLOAD_DIR = "/sdcard/t3hh4xx0r/downloads/";
         public static final String EXTENDEDCMD = "/cache/recovery/extendedcommand";
 
 	public static String PREF_LOCATION;
 	private static String OUTPUT_NAME;
 	private static String DOWNLOAD_URL;
 	private static File extStorageDirectory = Environment.getExternalStorageDirectory();
 
 	private int DOWNLOAD_PROGRESS = 0;
 	private static final int FLASH_ADDON = 0;
 	private static final int FLASH_COMPLETE = 1;
 	private static final int INSTALL_ADDON = 2;
 	private static final int SOFT_INSTALL_ADDON = 3;
 	
 	private ProgressDialog pbarDialog;
 
 	private Preference mGoogleApps;
 	private Preference mSBC1;
 	private Preference mOMFT;
 
 	private boolean mAddonIsFlashable;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
                 super.onCreate(savedInstanceState);
 
                 addPreferencesFromResource(R.xml.addons);
 
                 PreferenceScreen prefSet = getPreferenceScreen();
 
 	        mGoogleApps = prefSet.findPreference(GOOGLE_APPS);
 		mSBC1 = prefSet.findPreference(SBC1);
 	        if ((!Build.MODEL.equals("Incredible"))) {
 			PreferenceCategory kernelCategory = (PreferenceCategory) findPreference("kernel_category");
 		        kernelCategory.removePreference(mSBC1);
 		}
 
                 mOMFT = prefSet.findPreference(OMFT);
 		switch (getResources().getDisplayMetrics().densityDpi) {
 		case DisplayMetrics.DENSITY_MEDIUM:
                         PreferenceCategory appsCategory = (PreferenceCategory) findPreference("apps_category");
                         appsCategory.removePreference(mOMFT);
 		break;
 		}
 
 		if(this.isSdCardPresent() && this.isSdCardWriteable());
 		else log("Cannot update app. Sdcard is not writeable or present.");
 	}
 	
 	private boolean isSdCardPresent(){
 		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
 	}
 	
 	private boolean isSdCardWriteable(){
 		return !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
 	}
 
 	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
      		boolean value;
      		if(this.isSdCardPresent() && this.isSdCardWriteable()){
 		        if (preference == mGoogleApps) {
 				DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/GAPPS.zip";
 				OUTPUT_NAME = "Gapps.zip";
 				mAddonIsFlashable = true;
 			} else if (preference == mSBC1) {
                                 OUTPUT_NAME = "OMFGBk-sbc_1.zip";
                                 DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFGBk-sbc-1.zip";
                                 mAddonIsFlashable = true;
                         } else if (preference == mOMFT) {
                                 OUTPUT_NAME = "OMFT.apk";
                                 DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFT.apk";
                                 mAddonIsFlashable = false;
 
 			}
 
 			File f = new File (DOWNLOAD_DIR + OUTPUT_NAME);
 			if (f.exists()) {
 				if (mAddonIsFlashable) {
                 	        	handler.sendEmptyMessage(FLASH_ADDON);
 				} else {	
 						handler.sendEmptyMessage(SOFT_INSTALL_ADDON);
 				}
                 	} else {
                         	new DownloadFileAsync().execute(DOWNLOAD_URL);
                 	}
      		}
      		else{
      			log("Cannot update app. Sdcard is not writeable or present.");
      		}	
 			return true;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		if (id == DOWNLOAD_PROGRESS) {
 			pbarDialog = new ProgressDialog(this);
 			pbarDialog.setMessage("Downloading ...");
 			pbarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			pbarDialog.setCancelable(false);
 			pbarDialog.show();
 			return pbarDialog;
 		}
 		return null;
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what){
 			case FLASH_ADDON:
 				flashPackage();
 				break;
 			case FLASH_COMPLETE:
 				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 				pbarDialog.dismiss();
 				break;
 			case INSTALL_ADDON:
 				installPackage();
 				break;
 			}
 			return;
 		}
 	};
 
 	public void installPackage() {
 
                 pbarDialog = new ProgressDialog(Addons.this);
                 pbarDialog.setMessage("Please Wait\n\nInstalling Addon...");
                 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                 pbarDialog.show();
 
                 Thread cmdThread = new Thread(){
                         @Override
                         public void run() {
                                 Looper.prepare();
 
                                 try{Thread.sleep(1000);}catch(InterruptedException e){ }
 
                                 final Runtime run = Runtime.getRuntime();
                                 DataOutputStream out = null;
                                 Process p = null;
 
                                 try {
                                         p = run.exec("su");
                                         out = new DataOutputStream(p.getOutputStream());
 					out.writeBytes("busybox mount -o rw,remount /system\n");
                                         out.writeBytes("busybox cp " + DOWNLOAD_DIR + OUTPUT_NAME + " /system/app/\n");
                                         out.writeBytes("busybox mount -o ro,remount /system\n");
                                         out.flush();
                                 } catch (IOException e) {
                                         e.printStackTrace();
                                         return;
                                 }
 
                                 handler.sendEmptyMessage(FLASH_COMPLETE);
                         }
                 };
                 cmdThread.start();
         }
 
 	public void flashPackage() {
 
 		pbarDialog = new ProgressDialog(Addons.this);
 		pbarDialog.setMessage("Please Wait\n\nPreparing Addon for flashing...");
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
 		pbarDialog.show();
 
 		Thread cmdThread = new Thread(){
 			@Override
 			public void run() {
 
                         File updateDirectory = new File ("/cache/recovery/");
                         if (!updateDirectory.isDirectory()) {
                                 updateDirectory.mkdir();
                         }
 				Looper.prepare();
 
 				try{Thread.sleep(1000);}catch(InterruptedException e){ }
 
 				final Runtime run = Runtime.getRuntime();
 				DataOutputStream out = null;
 				Process p = null;
 
 				try {
 	                                p = run.exec("su");
 					out = new DataOutputStream(p.getOutputStream());
 					out.writeBytes("busybox echo 'install_zip(\"" + DOWNLOAD_DIR + OUTPUT_NAME +"\");' > " + EXTENDEDCMD + "\n");
                                         out.writeBytes("reboot recovery\n");
 					out.flush();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return;
 				}
 
 				handler.sendEmptyMessage(FLASH_COMPLETE);		
 			}
 		};
 		cmdThread.start();
 	}
 
 	class DownloadFileAsync extends AsyncTask<String, String, String> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(DOWNLOAD_PROGRESS);
 		}
 
 		@Override
 		protected String doInBackground(String... aurl) {
 			int count;
 
 			File downloadDir = new File (DOWNLOAD_DIR);
 			if (!downloadDir.isDirectory()) {
 				downloadDir.mkdir();
 			}
 
 			try {
 				URL url = new URL(aurl[0]);
 				URLConnection conexion = url.openConnection();
 				conexion.connect();
 
 				int lenghtOfFile = conexion.getContentLength();
 
 				InputStream input = new BufferedInputStream(url.openStream());
 				OutputStream output = new FileOutputStream(downloadDir + "/" + OUTPUT_NAME);
 
 				byte data[] = new byte[1024];
 
 				long total = 0;
 
 				while ((count = input.read(data)) != -1) {
 					total += count;
 					publishProgress(""+(int)((total*100)/lenghtOfFile));
 					output.write(data, 0, count);
 				}
 
 				output.flush();
 				output.close();
 				input.close();
 			} catch (Exception e) {}
 			return null;
 
 		}
 
 		@Override
 		protected void onProgressUpdate(String... progress) {
 			pbarDialog.setProgress(Integer.parseInt(progress[0]));
 		}
 
 		@Override
 		protected void onPostExecute(String unused) {
 			removeDialog(DOWNLOAD_PROGRESS);
 
 			File f = new File (DOWNLOAD_DIR + OUTPUT_NAME);
                         if (f.exists()) {
                                 if (mAddonIsFlashable) {
                                         handler.sendEmptyMessage(FLASH_ADDON);
                                 } else {        
                                                 handler.sendEmptyMessage(SOFT_INSTALL_ADDON);
                                 }
 			} else {
 				finish();
 			}
 		}
 	}
 
 	public void softInstallPackage() {
 	
 	Intent intent = new Intent(Intent.ACTION_VIEW);
 	intent.setDataAndType(Uri.fromFile(new File(DOWNLOAD_DIR + OUTPUT_NAME)), "application/vnd.android.package-archive");
 	startActivity(intent);
 	}
 
 	private void log(String msg) {
 	    Log.d(TAG, msg);
   	}
 }
