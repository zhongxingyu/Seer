 package eu.attech.gpstracker;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
import android.util.Log;
 
 public class Server {
 
 	protected static final int NOTIFICATION_ID = 0;
 	private Activity activity;
 	private String version;
 
 	// private NotificationManager mNotificationManager;
 	// private int NOTFICATION_ID;
 
 	public Server(Activity activity) {
 		this.activity = activity;
 	}
 
 	/*
 	 * This Method connect the User to the Server (Checks for the right Password
 	 * and Username)
 	 */
 	public void loginToServer() {
		Log.d("AAA", "Not Implementet yet");
 		login("OKA", "TEST");
 		activity.finish();
 		activity.startActivity(new Intent(activity, GPSTracker.class));
 	}
 
 	/* Check if the Network is connected (required for the First start) */
 	public boolean checkIfNetworkIsOn() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) activity
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 
 	/* This Method logout the User from the Server */
 	public void logout() {
 		try {
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()) {
 				File login = new File(root, "gpstracker/login.dat");
 				FileWriter loginwriter = new FileWriter(login);
 				BufferedWriter write = new BufferedWriter(loginwriter);
 				write.write("");
 				write.close();
 				activity.finish();
 				activity.startActivity(new Intent(activity, StartPage.class));
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	/*
 	 * true : User is logged in false : User is not logged in
 	 */
 	public boolean IsLoggedIn() {
 		try {
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()) {
 				File login = new File(root, "gpstracker/login.dat");
 				FileReader reader = new FileReader(login);
 				if (reader.read() < 0) {
 					reader.close();
 					return false;
 				} else {
 					reader.close();
 					return true;
 				}

 			}

 		} catch (IOException e) {
 		}
 
 		return false;
 	}
 
 	/* IF it is true then the user is logged in */
 	public void login(String user, String pass) {
 		try {
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()) {
 				File login = new File(root, "gpstracker/login.dat");
 				FileWriter loginwriter = new FileWriter(login);
 				BufferedWriter write = new BufferedWriter(loginwriter);
 				write.write("" + user + "\n" + pass);
 				write.close();
 
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	/* Get the actual Version for the App from WebServer */
 	public void getActualVersion(String clientVersionS) {
 
 		final String clientVersion = clientVersionS;
 		final String url = "http://app.attech.eu/download/GPS-Tracker/version.html";
 
 		new Thread() {
 			public void run() {
 				int BUFFER_SIZE = 2000;
 				InputStream in = null;
 				Message msg = Message.obtain();
 				msg.what = 1;
 				try {
 					in = openHttpConnection(url);
 
 					if (in == null) {
 						System.err
 								.println("Could not get Version, Server down?");
 					} else {
 						InputStreamReader isr = new InputStreamReader(in);
 						int charRead;
 						version = "";
 						char[] inputBuffer = new char[BUFFER_SIZE];
 
 						while ((charRead = isr.read(inputBuffer)) > 0) {
 							// ---convert the chars to a String---
 							String readString = String.copyValueOf(inputBuffer,
 									0, charRead);
 							version += readString;
 							inputBuffer = new char[BUFFER_SIZE];
 						}
 						Bundle b = new Bundle();
 						b.putString("serverVersion", version);
 						b.putString("clientVersion", clientVersion);
 						msg.setData(b);
 						in.close();
 						messageHandler.sendMessage(msg);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	/*
 	 * The Handler for the Network Operation Check if a new Version is available
 	 * on the Server
 	 */
 	private Handler messageHandler = new Handler() {
 
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			switch (msg.what) {
 			case 1:
 
 				String s = msg.getData().getString("serverVersion");
 				char a = s.charAt(0);
 				char b = s.charAt(1);
 				char c = s.charAt(2);
 				String s1 = "" + a + b + c;
 				int serverVersion = Integer.parseInt(s1);
 
 				String s2 = msg.getData().getString("clientVersion");
 
 				char[] ch = s2.toCharArray();
 				String s3 = "";
 				char a2;
 				char b2;
 				char c2;
 				if (ch.length < 3) {
 					if (ch.length < 2) {
 						a2 = s2.charAt(0);
 						s3 = "" + a2;
 					} else {
 						a2 = s2.charAt(0);
 						b2 = s2.charAt(1);
 						s3 = "" + a2 + b2;
 					}
 				} else {
 
 					a2 = s2.charAt(0);
 					b2 = s2.charAt(1);
 					c2 = s2.charAt(2);
 					s3 = "" + a2 + b2 + c2;
 				}
 				int clientVersion = Integer.parseInt(s3);
 				if (serverVersion > clientVersion) {
 					downloadNewApp();
 				}
 
 				break;
 			}
 		}
 	};
 
 	/* Opens the HTTP Connection */
 	private InputStream openHttpConnection(String urlStr) {
 		InputStream in = null;
 		int resCode = -1;
 
 		try {
 			URL url = new URL(urlStr);
 			URLConnection urlConn = url.openConnection();
 
 			if (!(urlConn instanceof HttpURLConnection)) {
 				throw new IOException("URL is not an Http URL");
 			}
 
 			HttpURLConnection httpConn = (HttpURLConnection) urlConn;
 			httpConn.setAllowUserInteraction(false);
 			httpConn.setInstanceFollowRedirects(true);
 			httpConn.setRequestMethod("GET");
 			httpConn.connect();
 
 			resCode = httpConn.getResponseCode();
 			if (resCode == HttpURLConnection.HTTP_OK) {
 				in = httpConn.getInputStream();
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 
 		}
 
 		return in;
 	}
 
 	/*
 	 * If threre is an new Version, Create a Notification and If it is clicked
 	 * then the New Version will be Downloaded
 	 */
 	@SuppressWarnings("deprecation")
 	public void downloadNewApp() {
 
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mManager = (NotificationManager) activity
 				.getSystemService(ns);
 
 		int icon = R.drawable.ic_launcher;
 		CharSequence tickerText = "New Version Available";
 		long when = System.currentTimeMillis();
 		Context context = activity.getApplicationContext();
 		CharSequence contextTitle = "New Version Available";
 		CharSequence contextText = "Click here to Download new Version.";
 		Intent notificationIntent = new Intent(
 				Intent.ACTION_VIEW,
 				Uri.parse("http://app.attech.eu/download/GPS-Tracker/GPS-Tracker.apk"));
 		PendingIntent contentIntent = PendingIntent.getActivity(activity, 0,
 				notificationIntent, 0);
 		Notification not = new Notification(icon, tickerText, when);
 		not.setLatestEventInfo(context, contextTitle, contextText,
 				contentIntent);
 		mManager.notify(1, not);
 	}
 
 }
