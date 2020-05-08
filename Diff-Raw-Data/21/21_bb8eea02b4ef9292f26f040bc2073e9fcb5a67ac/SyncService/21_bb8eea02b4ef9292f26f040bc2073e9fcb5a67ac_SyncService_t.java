 package edu.ucla.nesl.datacollector.service;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.BatteryManager;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 import edu.ucla.nesl.datacollector.Const;
 import edu.ucla.nesl.datacollector.DataArchive;
 import edu.ucla.nesl.datacollector.R;
 import edu.ucla.nesl.datacollector.tools.Base64;
 import edu.ucla.nesl.datacollector.tools.MySSLSocketFactory;
 import edu.ucla.nesl.datacollector.tools.NetworkUtils;
 import edu.ucla.nesl.datacollector.tools.ZipUtils;
 
 public class SyncService extends IntentService {
 
 	private static final String PORT = "9443";
 	private static int SERVICE_RESTART_INTERVAL = 5 * 60; // seconds
 	//private static int SERVICE_RESTART_INTERVAL = 5; // seconds
 
 	private String extPath = Environment.getExternalStorageDirectory().getPath() + "/" + DataArchive.archiveDir;
 
 	private Handler handler = new Handler();
 
 	private Set<String> streamName = new HashSet<String>();
 
 	public SyncService() {
 		super("SyncService");
 	}
 
 	private Context context = this;
 	private Thread thread;
 
 	private PowerManager.WakeLock mWakeLock;
 
 	private String serverip;
 	private String username;
 	private String password;
 
 	@Override
 	public void onCreate() {
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
 		mWakeLock.setReferenceCounted(false);
 
 		super.onCreate();
 	}
 
 	private boolean isWiFiConnected() {
 		Context context = getApplicationContext();
 		int networkStatus = NetworkUtils.getConnectivityStatus(context);
 		return networkStatus == NetworkUtils.TYPE_WIFI;
 	}
 
 	private boolean isBatteryCharging() {
 		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 		Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
 		int batteryStatus = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
 		return batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 
 		if (isWiFiConnected() && isBatteryCharging()) {
 			SharedPreferences settings = context.getSharedPreferences(Const.PREFS_NAME, 0);
 			serverip = settings.getString(Const.PREFS_SERVER_IP, null);
 			username = settings.getString(Const.PREFS_USERNAME, null);
 			password = settings.getString(Const.PREFS_PASSWORD, null);
 
 			if (serverip != null && username != null && password != null) {
 				CharSequence text = getText(R.string.foreground_sync_service_started);
 				Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
 				PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
 				notification.setLatestEventInfo(context, text, text, contentIntent);
 				startForeground(R.string.foreground_sync_service_started, notification);
 
 				acquireWakeLock();
 
 				try {
 					startSync();
 					cancelNotification();
 					cancelServiceSchedule();
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 					createNotification("Server Authentication Problem.");
 				} catch (IOException e) {
 					e.printStackTrace();
 					createNotification("Server Connection Problem.");
 					// Schedule next check.
 					if ( isWiFiConnected() && isBatteryCharging() && !isServiceScheduled()) {
 						scheduleStartService();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					createNotification("JSON Exception.");
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 					createNotification("Server Response Problem: " + e);
 					// Schedule next check.
 					if ( isWiFiConnected() && isBatteryCharging() && !isServiceScheduled()) {
 						scheduleStartService();
 					}
 				}
 
 				releaseWakeLock();
 				stopForeground(true);
 			}
 		} else {
 			cancelNotification();
 			cancelServiceSchedule();
 		}
 	}
 
 	private void cancelNotification() {
 		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.cancel(0);
 	}
 
 	private boolean isServiceScheduled() {
 		return PendingIntent.getBroadcast(this, 0, new Intent(this, SyncService.class), PendingIntent.FLAG_NO_CREATE) != null;
 	}
 
 	private void scheduleStartService() {
 		Calendar cal = Calendar.getInstance();
 
 		Intent intent = new Intent(this, SyncService.class);
 		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
 
 		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 		alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + SERVICE_RESTART_INTERVAL*1000, pintent);
 	}
 
 	private void cancelServiceSchedule() {
 		Intent intent = new Intent(this, SyncService.class);
 		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
 		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 		alarm.cancel(pintent);
 	}
 
 	private void createNotification(String message) {
 		PendingIntent pintent = PendingIntent.getActivity(
 				getApplicationContext(),
 				0,
 				new Intent(),
 				PendingIntent.FLAG_UPDATE_CURRENT);
 
 		Notification noti = new NotificationCompat.Builder(this)
 		.setContentTitle("DataCollector Error")
 		.setContentText(message)
 		.setContentIntent(pintent)
 		.setSmallIcon(R.drawable.ic_launcher)
 		.build();
 
 		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		noti.flags |= Notification.FLAG_AUTO_CANCEL;
 		notificationManager.notify(0, noti);
 	}
 
 	class ReadyCsvZipFileFilter implements FileFilter {
 		@Override
 		public boolean accept(File file) {
 			String regex = ".*\\.csv" + DataArchive.READY_FILE_EXT + DataArchive.ZIP_FILE_EXT + "$";
 			if (file.getName().matches(regex)) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private void startSync() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
 		updateStreamNameSet();
 
 		File dir = new File(extPath);
 		File[] files = dir.listFiles(new ReadyCsvZipFileFilter());
 		Arrays.sort(files);
 		for (File file : files) {
 			Log.d(Const.TAG, "cur file: " + file.getName());
 			String name = file.getName().split("_")[0];
 			if (!streamName.contains(name)) {
 				if (!createStream(name, file)) {
 					continue;
 				}
 			} 
 			uploadFile(name, file);
 			
 			file.delete();
 		}
 	}
 
 	private void uploadFile(String name, File file) throws ClientProtocolException, IllegalAccessException, IOException {
 		postZip("streams/" + name + ".zip", file);
 	}
 
 	private boolean createStream(String name, File zipFile) throws ClientProtocolException, IOException, IllegalAccessException {
 
 		// Unzip the file
 		File unzipLocation = new File(extPath + "/unzipped");
 		if (!unzipLocation.exists()) {
 			unzipLocation.mkdirs();
 		}
 		try {
 			ZipUtils.unzip(zipFile.getAbsolutePath(), unzipLocation.getAbsolutePath());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return false;
 		}
		File unzipFile = new File(extPath + "/unzipped/" + zipFile.getName().replace(DataArchive.READY_FILE_EXT + DataArchive.ZIP_FILE_EXT, ""));
 		if (!unzipFile.exists()) {
			Log.e(Const.TAG, "Unzipped file: " + unzipFile.getAbsolutePath() + " doesn't exist.");
 			return false;
 		}
 
 		// Read in the first line
 		BufferedReader br = null;
 		String line = null;
 		try {
 			br = new BufferedReader(new FileReader(unzipFile.getAbsolutePath()));
 			line = br.readLine();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		// Delete the unzipped file.
 		unzipFile.delete();
 
 		if (line == null) {
 			return false;
 		}
 
 		// Determine stream channels.
 		JSONObject jsonStream = new JSONObject();
 		try {
 			JSONArray channels = new JSONArray();
 			String[] split = line.split(",");
 			for (int i = 1; i < split.length; i++) {
 				String str = split[i].trim();
 				JSONObject ch = new JSONObject();
 				ch.put("name", "ch" + i);
 				try {
 					Integer.valueOf(str);
 					ch.put("type", "int");
 				} catch (NumberFormatException e) {
 					try {
 						Double.valueOf(str);
 						ch.put("type", "float");
 					} catch (NumberFormatException e1) {
 						ch.put("type", "text");
 					}
 				}
 				channels.put(ch);
 			}
 			if (channels.length() <= 0) {
 				return false;
 			}
 			jsonStream.put("name", name);
 			jsonStream.put("tags", "datacolletor");
 			jsonStream.put("channels", channels);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		// Create stream on server.
 		postJson("streams", jsonStream);
 
 		streamName.add(name);
 
 		return true;
 	}
 
 	private void updateStreamNameSet() throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
 		List<JSONObject> listStreams = getJsonListFromServer("streams");
 		streamName.clear();
 		for (JSONObject json : listStreams) {
 			streamName.add(json.getString("name"));
 		}
 	}
 
 	private List<JSONObject> getJsonListFromServer(String apiend) throws ClientProtocolException, IOException, JSONException, IllegalAccessException {
 		String url = "https://" + serverip + ":" + PORT + "/api/" + apiend;
 
 		/*if (tags != null) {
 			List<NameValuePair> params = new LinkedList<NameValuePair>();
 			params.add(new BasicNameValuePair("tags", tags));
 
 			String paramString = URLEncodedUtils.format(params, "utf-8");
 			url += "?" + paramString;
 		}*/
 
 		HttpClient httpClient = getNewHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 
 		// Add authorization
 		httpGet.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
 
 		HttpResponse response = httpClient.execute(httpGet);
 		InputStream is = response.getEntity().getContent();
 		long length = response.getEntity().getContentLength();
 		byte[] buffer = new byte[(int)length];
 		is.read(buffer);
 		String content = new String(buffer);
 
 		if (response.getStatusLine().getStatusCode() != 200) {
 			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");
 		}
 
		Log.i(Const.TAG, "Server Content: " + content);
 
 		JSONTokener tokener = new JSONTokener(content);
 		JSONArray array = new JSONArray(tokener);
 		List<JSONObject> jsonList = new ArrayList<JSONObject>();
 		for (int i = 0; i < array.length(); i++) {
 			JSONObject json = array.getJSONObject(i);
 			jsonList.add(json);
 		}
 
 		return jsonList;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	private void acquireWakeLock() {
 		if (!mWakeLock.isHeld()) {
 			mWakeLock.acquire();
 		}
 	}
 
 	private void releaseWakeLock() {
 		if (mWakeLock.isHeld()) {
 			mWakeLock.release();
 		}
 	}
 
 	private void postToast(final String msg) {
 		handler.post(new Runnable() {            
 			@Override
 			public void run() {
 				Toast.makeText(SyncService.this, msg, Toast.LENGTH_LONG).show();                
 			}
 		});
 	}
 
 	private HttpClient getNewHttpClient() {
 		try {
 			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 			trustStore.load(null, null);
 
 			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
 			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
 			HttpParams params = new BasicHttpParams();
 			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 
 			SchemeRegistry registry = new SchemeRegistry();
 			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 			registry.register(new Scheme("https", sf, 443));
 
 			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
 
 			return new DefaultHttpClient(ccm, params);
 		} catch (Exception e) {
 			return new DefaultHttpClient();
 		}
 	}
 
 	private void postZip(String apiend, File zipFile) throws IllegalAccessException, ClientProtocolException, IOException {
 		final String url = "https://" + serverip + ":" + PORT + "/api/" + apiend;
 
 		HttpClient httpClient = getNewHttpClient();
 		HttpPost httpPost = new HttpPost(url);
 
 		// Set content
 		InputStream in = new BufferedInputStream(new FileInputStream(zipFile));
 		httpPost.setEntity(new InputStreamEntity(in, zipFile.length()));
 		httpPost.setHeader("Content-Type", "application/zip");		
 
 		// Add authorization
 		httpPost.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
 
 		HttpResponse response = httpClient.execute(httpPost);
 		in.close();
 
 		InputStream is = response.getEntity().getContent();
 		long length = response.getEntity().getContentLength();
 
 		byte[] buffer = new byte[(int)length];
 		is.read(buffer);
 		String content = new String(buffer);
 		
 		if (response.getStatusLine().getStatusCode() != 200) {
 			String msg = "HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")";
 			if (response.getStatusLine().getStatusCode() == 400 && content.contains("Empty data.")) {
 				Log.e(Const.TAG, msg);
 			} else {
 				throw new IllegalAccessException(msg);
 			}
 		}
 
 		Log.i(Const.TAG, "Server Content: " + content);
 	}
 
 	private void postJson(String apiend, JSONObject json) throws ClientProtocolException, IOException, IllegalAccessException {
 		final String url = "https://" + serverip + ":" + PORT + "/api/" + apiend;
 
 		HttpClient httpClient = getNewHttpClient();
 		HttpPost httpPost = new HttpPost(url);
 
 		// Add authorization
 		httpPost.setHeader("Authorization", "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));
 
 		httpPost.setHeader("Content-Type", "application/json");		
 		try {
 			httpPost.setEntity(new StringEntity(json.toString()));
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 			return;
 		}
 
 		HttpResponse response = httpClient.execute(httpPost);
 		InputStream is = response.getEntity().getContent();
 		long length = response.getEntity().getContentLength();
 
 		byte[] buffer = new byte[(int)length];
 		is.read(buffer);
 		String content = new String(buffer);
 
 		if (response.getStatusLine().getStatusCode() != 200) {
 			throw new IllegalAccessException("HTTP Server Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "(" + content + ")");			
 		}
 
 		Log.i(Const.TAG, "Server Content: " + content);
 	}
 
 }
