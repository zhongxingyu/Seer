 package com.bigpupdev.synodroid.server;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.protocol.DSMHandlerFactory;
 import com.bigpupdev.synodroid.utils.ServiceHelper;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.content.Intent;
 import android.os.Environment;
 import android.util.Log;
 public class DownloadOriginalIntentService extends IntentService{
 	public static String TASKID = "TASKID";
 	public static String DEBUG = "DEBUG";
 	public static String DSM_VERSION = "DSM_VERSION";
 	public static String COOKIES = "COOKIES";
 	public static String PATH = "PATH";
 	public static String ORIGINAL_LINK = "ORIGINAL_LINK";
 	private int DOL_ID = 44;
 	
 	int progress = 0;
 
 	/** 
 	 * A constructor is required, and must call the super IntentService(String)
 	 * constructor with a name for the worker thread.
 	 */
 	public DownloadOriginalIntentService() {
 		super("DownloadOriginalIntentService");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	/**
 	 * The IntentService calls this method from the default worker thread with
 	 * the intent that started the service. When this method returns, IntentService
 	 * stops the service, as appropriate.
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		int taskid = intent.getIntExtra(TASKID, -1);
 		String dsm_version = intent.getStringExtra(DSM_VERSION);
 		String original_link = intent.getStringExtra(ORIGINAL_LINK);
 		String cookie = intent.getStringExtra(COOKIES);
 		String path = intent.getStringExtra(PATH);
 		boolean dbg = intent.getBooleanExtra(DEBUG, false);
 		
 		
 		DSMVersion vers = DSMVersion.titleOf(dsm_version);
 		if (vers == null) {
 			vers = DSMVersion.VERSION2_2;
 		}
 		DSMHandlerFactory dsm = DSMHandlerFactory.getFactory(vers, null, dbg);
 		
 		String url = dsm.getDSHandler().getMultipartUri();
 		String content = null;
 		try {
 			content = dsm.getDSHandler().buildOriginalFileString(taskid);
 		} catch (Exception e1) {
			if (dbg) Log.e(Synodroid.DS_TAG, "Failed building Original file string.", e1);
 		}
 		
 		String[] temp = original_link.split("/");
 		String fileName = temp[(temp.length) - 1];
 		
 		Notification notification = ServiceHelper.getNotificationProgress(this, fileName, progress, DOL_ID, R.drawable.dl_download);
 		
 		if (content != null){
 			HttpURLConnection con = null;
 			int retry = 0;
 			int MAX_RETRY = 2;
 			try {
 				while (retry <= MAX_RETRY) {
 					// Create the connection
 					con = ServiceHelper.createConnection(url, content, "GET", dbg, cookie, path);
 					// Add the parameters
 					OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
 					wr.write(content);
 					// Send the request
 					wr.flush();
 	
 					int contentLength = con.getContentLength();
 					
 					BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
 					
 			        byte[] buf = new byte[1024];
 			        int count = 0;
 			        int downloadedSize = 0;
 			        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
 			        long lastUpdate = 0;
 					while ((count = bis.read(buf)) != -1){
 			        	out.write(buf, 0, count);
 			        	downloadedSize += count;
 			        	progress = (int) (((float) downloadedSize/ ((float )contentLength)) * 100);
 			        	if (((lastUpdate + 250) < System.currentTimeMillis()) || downloadedSize == contentLength){
 			        		ServiceHelper.updateProgress(this, notification, progress, DOL_ID);
 			        	}
 			        }
 					
 					File out_path = Environment.getExternalStorageDirectory();
 					out_path = new File(out_path, "download");
 					File file = new File(out_path, fileName);
 					try {
 						// Make sure the Pictures directory exists.
 						out_path.mkdirs();
 						OutputStream os = new FileOutputStream(file);
 						os.write(out.toByteArray());
 						os.close();
 						ServiceHelper.showNotificationInfo(this, fileName, getString(R.string.action_download_original_saved), R.drawable.dl_finished);
 						return;
 					} catch (Exception e) {
 						// Unable to create file, likely because external storage is
 						// not currently mounted.
 						try{
							if (dbg) Log.e(Synodroid.DS_TAG, "Error writing " + file + " to SDCard.", e);
 						}catch (Exception ex){/*DO NOTHING*/}
 					}
 				}
 			}
 			// Unexpected exception
 			catch (Exception ex) {
				if (dbg) Log.e(Synodroid.DS_TAG, "Unexpected error", ex);
 					retry++;
 			}
 			// Finally close everything
 			finally {
 				if (con != null) {
 					con.disconnect();
 				}
 				ServiceHelper.cancelNotification(this, DOL_ID);
 				
 			}
 			ServiceHelper.showNotificationError(this, fileName, getString(R.string.action_download_original_failed), R.drawable.dl_error);
 		}
 	}
 }
