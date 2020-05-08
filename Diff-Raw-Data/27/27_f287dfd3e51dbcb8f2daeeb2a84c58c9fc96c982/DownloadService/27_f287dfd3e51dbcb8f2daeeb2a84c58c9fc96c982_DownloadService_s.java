 package com.brent.nprdroid;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.support.v4.content.LocalBroadcastManager;
 
 public class DownloadService extends IntentService {
 	public static final String urlFileName = "file name from url";
 	private static final int FOREGROUND_NOTIFICATION_ID = 10;
 	private static final String TAG = "DownloadService";
 	private Notification.Builder mBuilder;
 	SharedPreferences pref;
 	public static final String downloadDone = "downloadDone"; 
 	private static Intent downloadBroadcast = new Intent(downloadDone);
 
 	public DownloadService() {
 		super("DownloadService");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.i(TAG, "onHandleIntent");
 		// Delete the files currently in the directory
 		String sdPath = getExternalFilesDir(null).getAbsolutePath() + "/";
 		File sdPathFile = new File(sdPath);
 		File[] files = sdPathFile.listFiles();
 		if (files.length > 0) {
 			for (File file : files) {
 				file.delete();
 			}
 		}
 		mBuilder = new Notification.Builder(this);
 		mBuilder.setContentTitle("NPR stories download")
 	    .setTicker("Starting NPR stories download")
 	    .setSmallIcon(android.R.drawable.stat_sys_download);
 		startForeground(FOREGROUND_NOTIFICATION_ID, mBuilder.getNotification());
 		int index = 1;
 		ArrayList<String> urls = intent.getStringArrayListExtra("urls");
 		for (String url : urls) {
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(url);
 			try {
 				HttpResponse execute = client.execute(httpGet);
 				InputStream content = execute.getEntity().getContent();
 				byte[] buffer = new byte[1024];
 				int length;
 				String fileNameFromUrl = url.split("/")[8];
 //				String fileName = index > 9 ? "" + index + ".mp3" : "0" + index + ".mp3";
 				File audioFile = new File(getExternalFilesDir(null), fileNameFromUrl);
 				FileOutputStream out = new FileOutputStream(audioFile);
 				while ((length = content.read(buffer)) > 0) {
 					out.write(buffer, 0, length);
 				}				
 				mBuilder.setProgress(25, index, false)
 				.setTicker("Downloading " + fileNameFromUrl)
 				.setContentText(fileNameFromUrl);
 				startForeground(FOREGROUND_NOTIFICATION_ID, mBuilder.getNotification());
 				++index;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		mBuilder.setContentText("NPR stories download complete")
 		.setProgress(0, 0, false) //remove the progress bar
 		.setTicker("NPR stories download complete")
 		.setSmallIcon(android.R.drawable.stat_sys_download_done);
 		startForeground(FOREGROUND_NOTIFICATION_ID, mBuilder.getNotification());
 		pref = getSharedPreferences("NPRDownloadPreferences", Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = pref.edit();	
 		editor.putInt("listPosition", 0);	//save this position so its list item can be changed later
 		editor.commit();
		LocalBroadcastManager.getInstance(this).sendBroadcast(downloadBroadcast);
 	}
 
 }
