 package com.strangebrew.elsinore;
 
 //import android.app.Notification;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.strangebrew.elsinore.R;
 import com.strangebrew.elsinore.content.Data;
 import com.strangebrew.elsinore.content.Device;
 import com.strangebrew.elsinore.content.PID;
 import com.strangebrew.elsinore.content.Temp;
 
 import android.app.Notification;
 
 import android.app.ActivityManager;
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BackgroundServer extends IntentService {
 
 	public BackgroundServer() {
 		super("BackgroundServer");
 		// TODO Auto-generated constructor stub
 	}
 
 	public BackgroundServer(String name) {
 		super(name);
 
 	}
 
 	public static final String PARAM_IN_MSG = "imsg";
 	public static final String PARAM_OUT_MSG = "omsg";
 	private static final String KEY_SERVER_NAME_PREFERENCE = "pref_key_server_name";
 	private static final String KEY_SERVER_PORT_PREFERENCE = "pref_key_server_port";
 
 	public static final int MSG_SET_VALUE = 1;
 
 	private NotificationManager mNM;
 
 	// Unique Identification Number for the Notification.
 	// We use it on Notification start, and to cancel it.
 	private int NOTIFICATION = R.string.local_service_started;
 	protected String port;
 	protected String server;
 
 	/**
 	 * Class for clients to access. Because we know this service always runs in
 	 * the same process as its clients, we don't need to deal with IPC.
 	 */
 	public class LocalBinder extends Binder {
 		BackgroundServer getService() {
 			return BackgroundServer.this;
 		}
 	}
 
 	/*
 	 * @Override public void onCreate() { // startup the Background Query Server
 	 * 
 	 * 
 	 * mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 	 * 
 	 * // Display a notification about us starting. We put an icon in the status
 	 * bar.
 	 * 
 	 * showNotification();
 	 * 
 	 * }
 	 */
 
 	/*
 	 * @Override public int onStartCommand(Intent intent, int flags, int
 	 * startId) {
 	 * 
 	 * Log.i("LocalService", "Received start id " + startId + ": " +
 	 * intent.getStringExtra(PARAM_IN_MSG));
 	 * 
 	 * 
 	 * //onHandleIntent(intent); // getData();
 	 * //ControllerListFragment.handler.sendEmptyMessage(0); // We want this
 	 * service to continue running until it is explicitly // , so return sticky.
 	 * super.onStartCommand(intent, flags, startId); return
 	 * START_STICKY_COMPATIBILITY; }
 	 */
 
 	@Override
 	public IBinder onBind(Intent intent) {
 
 		return mBinder;
 	}
 
 	// This is the object that receives interactions from clients. See
 	// RemoteService for a more complete example.
 	private final IBinder mBinder = new LocalBinder();
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	public void showNotification() {
 		// In this sample, we'll use the same text for the ticker and the
 		// expanded notification
 		CharSequence text = getText(R.string.local_service_started);
 
 		// Set the icon, scrolling text and timestamp
 		Notification notification = new NotificationCompat.Builder(
 				getBaseContext()).setContentTitle("BGServer")
 				.setContentText(text).build();
 
 		// The PendingIntent to launch our activity if the user selects this
 		// notification
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				new Intent(this, MainActivity.class), 0);
 
 		// Send the notification.
 		mNM.notify(NOTIFICATION, notification);
 	}
 
 	/*
 	 * private Runnable sendUpdatesToUI = new Runnable() { public void run(){
 	 * 
 	 * this.adapter.setList(list); this.adapter.notifyDataSetChanged(); }
 	 * 
 	 * };
 	 */
 	private String url;
 
 	@SuppressWarnings("deprecation")
 	private void DisplayLoggingInfo() {
 		if (Data.ITEMS.size() > 0) {
 			Data.ITEMS.get(1);
 
 			Bundle b = new Bundle();
 			String[] tempValues = new String[2];
 
 			for (Device d : Data.ITEMS) {
 				tempValues[0] = Double.toString(d.temperature);
 				tempValues[1] = d.scale;
 				b.putStringArray(d.name, tempValues);
 			}
 
 		}
 	}
 
 	public double getTemp() {
 		return 10.0;
 	}
 
 	public void getData() {
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 
 		OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 			public void onSharedPreferenceChanged(SharedPreferences prefs,
 					String key) {
 				// Wait for the server name or port number to change
 				if (key.equals(KEY_SERVER_NAME_PREFERENCE)) {
 					server = prefs
 							.getString(KEY_SERVER_NAME_PREFERENCE, server);
 				} else if (key.equals(KEY_SERVER_PORT_PREFERENCE)) {
 					port = prefs.getString(KEY_SERVER_PORT_PREFERENCE, port);
 				}
 			}
 		};
 
 		prefs.registerOnSharedPreferenceChangeListener(listener);
 
 		server = prefs.getString(KEY_SERVER_NAME_PREFERENCE, server);
 		port = prefs.getString(KEY_SERVER_PORT_PREFERENCE, port);
 		if (server == null) {
 			return;
 		}
 		if (port != null && !port.equals("")) {
 			server = server + ":" + port;
 		}
 		Log.i("server", "Server: " + server);
 		// loop and get the data until we're broken
 		try {
 
 			DefaultHttpClient httpclient = new DefaultHttpClient(
 					new BasicHttpParams());
 			final HttpParams httpParams = httpclient.getParams();
 			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
 			HttpConnectionParams.setSoTimeout(httpParams, 5000);
 			// construct the URL
 
 			if (server.contains("http://")) {
 				url = server;
 			} else {
 				url = "http://" + server;
 			}
 			Log.i("URL", "Server: " + url);
 			if (!server.equals("") || !url.equals("")) {
 
 				// System.out.println("URL " + url + "/getstatus");
 				HttpGet httpget = new HttpGet(url + "/getstatus");
 				// Depends on your web service
 				httpget.setHeader("Content-type", "application/json");
 
 				InputStream inputStream = null;
 				String result = null;
 				JSONObject inObject = null;
 				HttpResponse response = httpclient.execute(httpget);
 				// System.out.println("R");
 				HttpEntity entity = response.getEntity();
 
 				inputStream = entity.getContent();
 				// json is UTF-8 by default i beleive
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(inputStream, "UTF-8"), 8);
 				StringBuilder sb = new StringBuilder();
 
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				result = sb.toString();
 
 				// System.out.println(result);
 
 				inObject = new JSONObject(result);
 
 				// iterate the list for objects
 				Iterator<?> keys = inObject.keys();
 
 				while (keys.hasNext()) {
 					String key = (String) keys.next();
 					if (inObject.get(key) instanceof JSONObject) {
 						iDataCheck((JSONObject) inObject.get(key), key);
 					}
 				}
 
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					// e1.printStackTrace();
 				}
 			}
 		} catch (UnknownHostException e) {
 
 			Log.i("Host Error", "Host " + url + " could not be found");
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				// e1.printStackTrace();
 			}
 
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		} catch (IllegalArgumentException iae) {
 			System.out.println("IAE: " + url);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				// e1.printStackTrace();
 			}
 		}
 
 		Log.i("Server", "Finished getting data: " + Data.ITEMS.size());
 	}
 
 	private PID getPID(String iName) {
 		if (Data.ITEMS.size() == 0) {
 			return null;
 		}
 		Iterator<Device> iterator = Data.ITEMS.iterator();
 
 		if (Data.ITEMS.get(0).getClass() == PID.class
 				&& Data.ITEMS.get(0).name.equalsIgnoreCase(iName))
 			return (PID) iterator.next();
 		while (iterator.hasNext()) {
 			Device temp = iterator.next();
 			if (temp.getClass() == PID.class
 					&& temp.name.equalsIgnoreCase(iName))
 				return (PID) temp;
 		}
 
 		return null;
 	}
 
 	private Temp getTemp(String iName) {
 		if (Data.ITEMS.size() == 0) {
 			return null;
 		}
 		Iterator<Device> iterator = Data.ITEMS.iterator();
 
 		if (Data.ITEMS.get(0).getClass() == Temp.class
 				&& Data.ITEMS.get(0).name.equalsIgnoreCase(iName))
 			return (Temp) iterator.next();
 		while (iterator.hasNext()) {
 			Device temp = iterator.next();
 			if (temp.getClass() == Temp.class
 					&& temp.name.equalsIgnoreCase(iName))
 				return (Temp) temp;
 		}
 
 		return null;
 	}
 
 	private void iDataCheck(JSONObject iData, String device) {
 
 		try {
 			try {
 				// check for a GPIO value
 				iData.getInt("gpio");
 
 				// this is the default mannerism, we have a PID
 
 				// we have a temperature now
 
 				device = device.toUpperCase(Locale.CANADA);
 				device = device.replaceAll("_", " ");
 
 				PID tPID = this.getPID(device);
 				if (tPID == null) {
 					// Log.i("No device", "adding: " +
 					// Integer.toString(ITEMS.size()+1) + device);
 
 					Data.addItem(new PID(
 							Integer.toString(Data.ITEMS.size() + 1), device));
 					tPID = this.getPID(device);
 					// Log.i("added", "new PID: " + tPID.name);
 				}
 
 				tPID.temperature = Double.parseDouble(iData.getString("temp")) + 1;
 
 				tPID.scale = iData.getString("scale");
 
 				// check the GPIO
 				if (iData.getInt("gpio") == -1) {
 					return;
 				}
 
 				// check to see if we need to feedback to the server
 
 				if (tPID.feedback) {
 					// Log.i("POST", "Creating params");
 					String urlParameters = "dutycycle=" + tPID.dutycycle
 							+ "&cycletime=" + tPID.cycletime + "&mode="
 							+ tPID.mode + "&setpoint=" + tPID.setpoint + "&k="
 							+ tPID.k_param + "&i=" + tPID.i_param + "&p="
 							+ tPID.p_param;
 					// Log.i("Post", "Params: " + url + "/?" + urlParameters);
 					urlParameters = url + "/?" + urlParameters;
 					// Create a new HttpClient and Post Header
 					HttpClient httpclient = new DefaultHttpClient();
 					HttpPost httppost = new HttpPost(url);
 
 					try {
 						// Add your data
 						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 								2);
 						nameValuePairs.add(new BasicNameValuePair("form",
 								tPID.name));
 						nameValuePairs.add(new BasicNameValuePair("mode",
 								tPID.mode));
 						nameValuePairs.add(new BasicNameValuePair("dutycycle",
 								Double.toString(tPID.dutycycle)));
 						nameValuePairs.add(new BasicNameValuePair("cycletime",
 								Double.toString(tPID.cycletime)));
 						nameValuePairs.add(new BasicNameValuePair("setpoint",
 								Double.toString(tPID.setpoint)));
 						nameValuePairs.add(new BasicNameValuePair("d", Double
 								.toString(tPID.p_param)));
 						nameValuePairs.add(new BasicNameValuePair("i", Double
 								.toString(tPID.i_param)));
 						nameValuePairs.add(new BasicNameValuePair("k", Double
 								.toString(tPID.k_param)));
 						httppost.setEntity(new UrlEncodedFormEntity(
 								nameValuePairs));
 
 						// Execute HTTP Post Request
 						HttpResponse response = httpclient.execute(httppost);
 
 					} catch (ClientProtocolException e) {
 						// TODO Auto-generated catch block
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 					}
 					tPID.feedback = false;
 
 				} else {
 
 					// after this we should have good Data for the PID
 					tPID.mode = iData.getString("mode");
 					tPID.dutycycle = iData.getDouble("duty");
 					tPID.cycletime = iData.getDouble("cycle");
 					tPID.elapsedTime = iData.getLong("elapsed");
 					tPID.k_param = iData.getDouble("k");
 					tPID.i_param = iData.getDouble("i");
 					if(iData.has("d")) {
 						tPID.p_param = iData.getDouble("d");
 					}
 					else if(iData.has("p")) {
 						tPID.p_param = iData.getDouble("p");
 					}
 					tPID.setpoint = iData.getDouble("setpoint");
 					Log.i("TEMP", "Temp is: " + tPID.temperature);
					tPID.elapsed = iData.getLong("elapsed");
 					Data.ITEMS.remove(tPID);
 					Data.ITEMS.add(tPID);
 				}
 
 			} catch (JSONException e) {
 				// just probing the temperature
 				// we have a temperature now
 				device = device.toUpperCase(Locale.CANADA);
 				device = device.replaceAll("_", " ");
 
 				Temp tTemp = this.getTemp(device);
 
 				if (tTemp == null) {
 					// Log.i("No device", "adding: " +
 					// Integer.toString(ITEMS.size()+1) + device);
 
 					Data.addItem(new Temp(
 							Integer.toString(Data.ITEMS.size() + 1), device));
 					tTemp = this.getTemp(device);
 				}
 
 				tTemp.scale = iData.getString("scale");
 				tTemp.temperature = Double.parseDouble(iData.getString("temp"));
				tTemp.elapsed = (double) iData.getLong("elapsed")/1000000;
 				Data.ITEMS.remove(tTemp);
 				Data.ITEMS.add(tTemp);
 			}
 
 		} catch (JSONException je) {
 			Log.i("error", device + je.getMessage());
 			je.printStackTrace();
 		} catch (NumberFormatException ne) {
 			ne.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 
 		Log.i("BGServer", "Handling Intent");
 		getData();
 		// processing done here….
 		Intent broadcastIntent = new Intent();
 		broadcastIntent
 				.setAction(com.strangebrew.elsinore.MainActivity.ResponseReceiver.ACTION_RESP);
 		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 		broadcastIntent.putExtra(PARAM_OUT_MSG, "update");
 		sendBroadcast(broadcastIntent);
 
 	}
 }
