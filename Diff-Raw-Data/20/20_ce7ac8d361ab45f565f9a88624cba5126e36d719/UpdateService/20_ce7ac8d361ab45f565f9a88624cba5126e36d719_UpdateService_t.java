 package ikeagold.zimniy.giolight;
 
 import ikeagold.zimniy.giolight.GioLightUpdActivity;
 import ikeagold.zimniy.giolight.R;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.util.Log;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class UpdateService extends Service {
 
 	public String glvc;
 	public String glvn;
 	public String str1;
 	public String str2;
 	public String furlt;
 	public String furl;
 
 	public static void schedule(Context context) {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		boolean Sync = prefs.getBoolean("synckey", false);
 		if (Sync == false) {
 			// Nothing
 		} else {
 			Intent intent = new Intent(context, UpdateService.class);
 			PendingIntent pending = PendingIntent.getService(context, 0,
 					intent, 0);
 			AlarmManager alarm = (AlarmManager) context
 					.getSystemService(Context.ALARM_SERVICE);
 			alarm.cancel(pending);
			alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime() + 5000,
					AlarmManager.INTERVAL_HOUR, pending);
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		handleCommand();
 		return START_REDELIVER_INTENT;
 	}
 
 	// Update Thread
 	private void handleCommand() {
 		new Thread() {
 			public void run() {
 				SharedPreferences prefs = PreferenceManager
 						.getDefaultSharedPreferences(getBaseContext());
 				boolean Sync;
 				Sync = prefs.getBoolean("synckey", false);
 				if (Sync == false)
 					return;
 				long lastUpdate = prefs.getLong("lastUpdateCheck", 0);
 				String sint = prefs.getString("updateInterval", "24");
 				int interval = Integer.parseInt(sint);
 				if (lastUpdate + (interval * 60 * 60) > System
 						.currentTimeMillis())
 					return;
 				try {
 					if (isInternetOn()) {
 						update();
 					} else {
 						// Nothing
 					}
 				} catch (Exception e) {
 					Log.e("WTF",
 							"Exception during handleCommand():\n"
 									+ Log.getStackTraceString(e));
 				} finally {
 					stopSelf();
 				}
 			}
 		}.start();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	public void update() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		boolean Test = prefs.getBoolean("testkey", false);
 		if (Test == false) {
 			str1 = DownloadText("http://gio-light.googlecode.com/hg/version.txt");
 			glvn = str1;
 			NewRom();
 		} else {
 			str2 = DownloadText("http://gio-light.googlecode.com/hg/version.testing.txt");
 			glvn = str2;
 			NewRom();
 		}
 	}
 
 	private void NewRomNotification() {
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
 		int icon = R.drawable.ic_launcher;
 		CharSequence contentTitle = "GioLightUpd";
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, contentTitle, when);
 		Context context = getApplicationContext();
 		CharSequence contentText = "  ROM";
 		Intent notificationIntent = new Intent(this, GioLightUpdActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				notificationIntent, 0);
 		notification.setLatestEventInfo(context, contentTitle, contentText,
 				contentIntent);
 		final int HELLO_ID = 1;
 		mNotificationManager.notify(HELLO_ID, notification);
 	}
 
 	private void NewRom() {
 		if (glvc.trim().equalsIgnoreCase(glvn.trim())) {
 		} else {
 			if (Integer.parseInt(glvc.trim()) < Integer.parseInt(glvn.trim())) {
 				NewRomNotification();
 			} else {
 			}
 		}
 	}
 
 	private InputStream OpenHttpConnection(String urlString) throws IOException {
 		InputStream in = null;
 		int response = -1;
 		URL url = new URL(urlString);
 		URLConnection conn = url.openConnection();
 		if (!(conn instanceof HttpURLConnection))
 			throw new IOException("Not an HTTP connection");
 		try {
 			HttpURLConnection httpConn = (HttpURLConnection) conn;
 			httpConn.setAllowUserInteraction(false);
 			httpConn.setInstanceFollowRedirects(true);
 			httpConn.setRequestMethod("GET");
 			httpConn.connect();
 			response = httpConn.getResponseCode();
 			if (response == HttpURLConnection.HTTP_OK) {
 				in = httpConn.getInputStream();
 			}
 		} catch (Exception ex) {
 			throw new IOException("Error connecting");
 		}
 		return in;
 	}
 
 	private String DownloadText(String URL) {
 		int BUFFER_SIZE = 2000;
 		InputStream in = null;
 		try {
 			in = OpenHttpConnection(URL);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return "";
 		}
 		InputStreamReader isr = new InputStreamReader(in);
 		int charRead;
 		String str = "";
 		char[] inputBuffer = new char[BUFFER_SIZE];
 		try {
 			while ((charRead = isr.read(inputBuffer)) > 0) {
 				// Convert chars 2 String
 				String readString = String
 						.copyValueOf(inputBuffer, 0, charRead);
 				str += readString;
 				inputBuffer = new char[BUFFER_SIZE];
 			}
 			in.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 		return str;
 	}
 
 	// Check isInternetOn
 	public final boolean isInternetOn() {
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
 				|| connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
 			return true;
 		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
 			return false;
 		}
 		return false;
 	}
 
 }
