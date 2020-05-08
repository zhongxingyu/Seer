 package com.bigpupdev.synodroid.server;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.security.SecureRandom;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 
 import org.json.JSONObject;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.protocol.DSMHandlerFactory;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllHostNameVerifier;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllTrustManager;
 import com.bigpupdev.synodroid.utils.ServiceHelper;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.app.Notification;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.util.Log;
 
 public class UploadIntentService extends IntentService{
 	public static String URL = "URL";
 	public static String DEBUG = "DEBUG";
 	public static String DSM_VERSION = "DSM_VERSION";
 	public static String COOKIES = "COOKIES";
 	public static String DIRECTORY = "DIRECTORY";
 	public static String PATH = "PATH";
 	private int UL_ID = 43;
 	private static final String PREFERENCE_GENERAL = "general_cat";
 	private static final String PREFERENCE_AUTO_DSM = "general_cat.auto_detect_DSM";
 	
 	int progress = 0;
 
 	static {
 		SSLContext sc;
 		try {
 			sc = SSLContext.getInstance("TLS");
 			sc.init(null, new TrustManager[] { new AcceptAllTrustManager() }, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 			HttpsURLConnection.setDefaultHostnameVerifier(new AcceptAllHostNameVerifier());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	/** 
 	 * A constructor is required, and must call the super IntentService(String)
 	 * constructor with a name for the worker thread.
 	 */
 	public UploadIntentService() {
 		super("UploadIntentService");
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
 		String dsm_version = intent.getStringExtra(DSM_VERSION);
 		String cookie = intent.getStringExtra(COOKIES);
 		String shared = intent.getStringExtra(DIRECTORY);
 		Uri uri = Uri.parse(intent.getStringExtra(URL));
 		String path = intent.getStringExtra(PATH);
 		boolean dbg = intent.getBooleanExtra(DEBUG, false);
 		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
 		boolean autoDetect = preferences.getBoolean(PREFERENCE_AUTO_DSM, true);
 		
 		DSMVersion vers = DSMVersion.titleOf(dsm_version);
 		if (vers == null) {
 			vers = DSMVersion.VERSION2_2;
 		}
 		DSMHandlerFactory dsm = DSMHandlerFactory.getFactory(vers, null, dbg, autoDetect);
 		
 		String url = dsm.getDSHandler().getMultipartUri();
 		byte[] content = null;
 		try {
 			content = dsm.getDSHandler().generateMultipart(uri, shared);
 		} catch (Exception e1) {
 			if (dbg) Log.e(Synodroid.DS_TAG, "UploadIntentService: Error while building multipart.", e1);
 		}
 		
 		if (content != null){
 			Notification notification = ServiceHelper.getNotificationProgress(this, uri.getPath(), progress, UL_ID, R.drawable.dl_upload);
 
 			HttpURLConnection conn = null;
 			JSONObject respJSO = null;
 			int retry = 0;
 			int MAX_RETRY = 2;
 			try {
 				while (retry <= MAX_RETRY) {
 					try {
 						// Create the connection
 						conn = ServiceHelper.createConnection(url, "", "POST", dbg, cookie, path);
 						conn.setRequestProperty("Connection", "keep-alive");
 						conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + dsm.getDSHandler().getBoundary());
 						conn.setFixedLengthStreamingMode(content.length);
 						
 						// Write the multipart
 						OutputStream out = null;
 						try{
 							out = conn.getOutputStream();
 							int offset = 0;
 							int size = 1024;
 							int lenBytes = Math.min(size, content.length);
 							long lastUpdate = 0;
 							while (content.length > offset){
 								lenBytes = Math.min(content.length-offset, size);
 								out.write(content, offset, lenBytes);
 								offset += lenBytes;
 								progress = (int) ((float) offset / (float )content.length * 100);
 								if (((lastUpdate + 250) < System.currentTimeMillis()) || offset == content.length){
 									lastUpdate = System.currentTimeMillis();
 					                ServiceHelper.updateProgress(this, notification, progress, UL_ID);
 								}
								out.flush();
 								   
 							}
 						}
 						finally{
 							if (out != null) {
 								out.close();
 							}
 							out = null;
 						}
 						
 						// Now read the reponse and build a string with it
 						BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 						StringBuffer sb = new StringBuffer();
 						String line;
 						while ((line = br.readLine()) != null) {
 							sb.append(line);
 						}
 						br.close();
 			
 						if (conn.getResponseCode() == -1) {
 							retry++;
 							if (dbg) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 						} else {
 							if (dbg) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 							respJSO = new JSONObject(sb.toString());
 							boolean success = respJSO.getBoolean("success");
 							// If successful then build details list
 							if (!success) {
 								ServiceHelper.showNotificationError(this, uri.getPath(), getString(R.string.upload_failed), R.drawable.dl_error);
 							}
 							return;
 						}
 					} catch (Exception e) {
 						if (dbg) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 						retry ++;
 					}
 				}
 			}
 			finally {
 				if (conn != null) {
 					conn.disconnect();
 				}
 				conn = null;
 				ServiceHelper.cancelNotification(this, UL_ID);
 			}
 		}
 		ServiceHelper.showNotificationError(this, uri.getPath(), getString(R.string.upload_failed), R.drawable.dl_error);
 	}
 }
