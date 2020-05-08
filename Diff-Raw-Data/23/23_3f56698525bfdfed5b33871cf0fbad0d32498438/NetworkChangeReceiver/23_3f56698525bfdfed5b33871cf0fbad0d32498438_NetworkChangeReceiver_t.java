 package com.nguyenmp.gauchowifi;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 
 public class NetworkChangeReceiver extends BroadcastReceiver {
 	private static final int NOTIFICATION_ID = 1;
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		System.out.println("Received Connection Changed");
 		
 		//Get shared preferences
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		
 		//Remove previous unencrypted credential stores
 		prefs.edit().remove(LoginFragment.KEY_USERNAME).remove(LoginFragment.KEY_PASSWORD).commit();
 		
 		//Use encrypted stores instead
 		ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(context, prefs);
 		
 		//If a username and password exists in the preferences
 		if (prefs.getBoolean("auto_log_in_enabled", true) && obscuredPrefs.contains(LoginFragment.KEY_USERNAME_BASE64) && obscuredPrefs.contains(LoginFragment.KEY_PASSWORD_BASE64)) {
 			//Launch handler thread because we have all the information now
 			LoginHandler handler = new LoginHandler(context);
 			HandledThread thread = new BroadcastHandlerThread(intent, context, obscuredPrefs.getString(LoginFragment.KEY_USERNAME_BASE64, ""), obscuredPrefs.getString(LoginFragment.KEY_PASSWORD_BASE64, ""));
 			thread.setHandler(handler);
 			thread.setPriority(Thread.MIN_PRIORITY);
 			thread.start();
 		}
 	}
 	
 	public static boolean hasCaptivePortal() throws ClientProtocolException, IOException {
 		URL url = new URL("http://clients1.google.com/generate_204");
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		
 		if (conn.getResponseCode() == 204) {
 			System.out.println("204 Generator Succeeded.  No Captive Portal detected.");
 			return false;
 		}
 		System.out.println("Captive Portal detected!");
 		return true;
 	}
 	
 	public static void logout() throws IOException {
 		try {
 			URL url = new URL("http://login.wireless.ucsb.edu/logout.html");
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setInstanceFollowRedirects(false);
 			conn.setRequestMethod("POST");
 			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			conn.setRequestProperty("Content-Length", "0");
 			
 			conn.setDoInput(false);
 			conn.setDoOutput(false);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	private static int login(String unencodedUsername, String unencodedPassword) throws IOException {
 		URL url;
 		
 		try {
 			url = new URL("https://login.wireless.ucsb.edu/login.html");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		
 		//Prepare post content
 		String urlParams = String.format("username=%s&password=%s&buttonClicked=%s",
 						URLEncoder.encode(unencodedUsername, "UTF-8"),
 						URLEncoder.encode(unencodedPassword, "UTF-8"),
 						"4");
 		
 		//Open connection with post properties
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setInstanceFollowRedirects(false);
 		conn.setRequestMethod("POST");
 		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		conn.setRequestProperty("Content-Length", Integer.toString(urlParams.length()));
 		
 		//Sets that we do input and output streams
 		conn.setDoInput(true);
 		conn.setDoOutput(true);
 		
 		//Do output
 		DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
 		outputStream.writeBytes(urlParams);
 		outputStream.flush();
 		outputStream.close();
 		
 		//Read the response
 		BufferedReader inputStream = new BufferedReader(new InputStreamReader((conn.getInputStream())));
 		char[] buffer = new char[1024];
 		int bytesRead;
 		StringBuilder builder = new StringBuilder();
 		while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
 			builder.append(buffer, 0, bytesRead);
 		}
 		
 		//Return the value of the location header
 		return parseLocationHeader(builder.toString());
 	}
 
 	private static String parseStatusCode(int statusCode) {
 		String resultMessage = null;
 		switch (statusCode) {
 		case 0:
 			resultMessage = "Login successful for UCSB Wireless Web.";
 			break;
 		case 1:
 			resultMessage = "You are already logged in. No further action is required on your part.";
 			break;
 		case 2:
 			resultMessage = "You are not configured to authenticate against web portal. No further action is required on your part.";
 			break;
 		case 3:
 			resultMessage = "The username specified cannot be used at this time. Perhaps the user is already logged into the system?";
 			break;
 		case 4:
 			resultMessage = "Wrong username and password. Please try again.";
 			break;
 		case 5:
 			resultMessage = "The User Name and Password combination you have entered is invalid or your account has been administratively disabled.  Please try again.";
 			break;
 		default:
 			resultMessage = "Unknown error with error code: " + statusCode;
 			break;
 		}
 		
 		return resultMessage;
 	}
 
 	private static int parseLocationHeader(String locationHeaderValue) throws MalformedURLException {
 		if (locationHeaderValue != null && locationHeaderValue.contains("statusCode=")) {
 //				String redirectUrl = locationHeaderValue;
 			String query = locationHeaderValue;
 			
 			//Get the err_flag parameter
 			int start = query.indexOf("statusCode=") + "statusCode=".length();
 			int end = query.indexOf('&', start);
 			
 			if (end == -1)
 				end = query.indexOf('\"', start);
 			
 			//If err_flag is the last element, then just finish at the end of the string
 			if (end == -1)
 				end = query.length();
 			
 			if (start > 0) {
 				int errorCode = Integer.parseInt(query.substring(start, end));
 				return errorCode;
 			}
 			
 		} else if (locationHeaderValue.contains("Login Successful")) {
 			return 0;
 		}
 	
 		return -1;
 	}
 	
 	private static class BroadcastHandlerThread extends HandledThread {
 		private final Intent mIntent;
 		private final Context mContext;
 		private final String mUsername, mPassword;
 		
 		private BroadcastHandlerThread(Intent intent, Context context, String username, String password) {
 			mIntent = intent;
 			mContext = context;
 			mUsername = username;
 			mPassword = password;
 		}
 		
 		public void run() {
 			boolean noConnectivity = mIntent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 			
 			System.out.println("No connectivity: " + noConnectivity);
 			
 			if (!noConnectivity) {
 				//There is connectivity and connection is changing
 				ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo info = connManager.getActiveNetworkInfo();
				
				if (info != null) {
					//Sometimes, the connection changes rapidly so we 
					//get the intent that there was connectivity but 
					//it was imediately cut off.  For example, enter 
					//a building and then imediately turn off wifi as 
					//you connect to the access point.
					
					System.out.println("Network Type:" + info.getTypeName());
					try {
						if (info.getType() == ConnectivityManager.TYPE_WIFI && hasCaptivePortal()) {
							//Only log in if we are connect to WIFI and we have a captive portal
							dispatchMessage(login(mUsername, mPassword));
						}
					} catch (IOException e) {
						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	private static class LoginHandler extends Handler {
 		private final Context mContext;
 		
 		LoginHandler(Context context) {
 			mContext = context;
 		}
 		
 		public void handleMessage(Message message) {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 			
 			if (prefs.getBoolean("notification_enabled", true) && message.obj instanceof Integer) {
 				String statusMessage = parseStatusCode((Integer) message.obj);
 				NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(mContext);
 				notifBuilder.setSmallIcon(android.R.drawable.arrow_down_float);
 				Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
 				notifBuilder.setLargeIcon(largeIcon);
 				notifBuilder.setContentTitle("UCSB Wireless Web");
 				notifBuilder.setContentText(statusMessage);
 				notifBuilder.setOngoing(false);
 				notifBuilder.setAutoCancel(false);
 				
 				Intent logoutIntent = new Intent(Intent.ACTION_VIEW);
 				logoutIntent.setData(Uri.parse("http://login.wireless.ucsb.edu/logout.html"));
 				
 				PendingIntent pendingLogoutIntent = PendingIntent.getActivity(mContext, 0, logoutIntent, 0);
 				notifBuilder.setContentIntent(pendingLogoutIntent);
 				
 				Notification notif = notifBuilder.build();
 				NotificationManager notifMan = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
 				notifMan.notify(NOTIFICATION_ID, notif);
 			}
 		}
 	}
 }
