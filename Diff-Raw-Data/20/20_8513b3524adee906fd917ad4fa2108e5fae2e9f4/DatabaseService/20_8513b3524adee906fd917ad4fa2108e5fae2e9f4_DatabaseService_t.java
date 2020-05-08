 package org.metawatch.manager.emailhack;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class DatabaseService extends Service {
 
 	private static final String CONTENT_URI = "content://com.android.email.provider/message";
 	private static final String DB_PATH = "/data/data/com.android.email/databases/EmailProvider.db";
 	
 	private static final String GET_MAILBOXES_CMD = "select distinct displayName from Mailbox order by displayName";
 	private static final String GET_UNREAD_CMD = "select sum(unreadCount) from Mailbox";
 	
 	private static boolean initialized = false;
 	private static int running = 0;
 	private static Object mutex = new Object();
 	
 	public static String[] getMailboxes() {
 		return dbExec(GET_MAILBOXES_CMD);
 	}
 	
 	public static boolean isRunning() {
 		return running > 0;
 	}
 	
 	public static int getUnreadCount(Context context) {
 		synchronized(mutex) {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			return prefs.getInt("unreadCount", -1);
 		}
 	}
 	public static boolean isCacheOld(Context context) {
 		synchronized(mutex) {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			long timestamp = prefs.getLong("cacheTimestamp", 0);
 			int interval = Integer.parseInt(prefs.getString("alarmInterval", "15")) * 60000;
 			return (System.currentTimeMillis() - timestamp >= interval);
 		}
 	}
 	public static void refreshUnreadCount(Context context, boolean forceWidgetUpdate) {
 		synchronized(mutex) {
 			String[] output = dbExec(GET_UNREAD_CMD);
 			
 			int newCount = -1;
 			try {
 				newCount = Integer.parseInt(output[0]);
 			} catch (Exception ex) {
 				if (Main.log) Log.e(Main.TAG, "Exception while parsing unread count, output was: " + Arrays.toString(output), ex);
 			}
 			
 			if (Main.log) Log.d(Main.TAG, "Unread count: " + newCount);
 			
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			int oldCount = prefs.getInt("unreadCount", -1);
 			if (oldCount >= 0 && newCount > oldCount) {
 				if (prefs.getBoolean("notify", true))
 				ManagerApi.sendNotification(context, newCount);
 			}
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putInt("unreadCount", newCount);
 			if (newCount != -1) editor.putLong("cacheTimestamp", System.currentTimeMillis());
 			editor.commit();
 			
 			if (forceWidgetUpdate || oldCount != newCount) {
 				if (Main.log) Log.d(Main.TAG, "Updating widget...");
				ManagerApi.updateWidget(context, false, true);
 			}
 		}
 	}
 	
 	public static void setAlarm(Context context, String intervalString) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
 		PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, DatabaseService.class), 0);
 		
 		if (Main.log) Log.d(Main.TAG, "Removing any existing alarm...");
 		am.cancel(pi);
 		
 		int interval = Integer.parseInt(
 				(intervalString == null ?
 						prefs.getString("alarmInterval", "15") :
 						intervalString)) * 60000;
 		if (interval > 0) {
 			if (Main.log) Log.d(Main.TAG, "Setting alarm (interval " + interval + ")...");
 			am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pi);
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		if (!initialized) {
 			if (Main.log) Log.d(Main.TAG, "Registering observer...");
 			getContentResolver().registerContentObserver(Uri.parse(CONTENT_URI), false, new DbContentObserver());
 			
 			setAlarm(this, null);
 			
 			initialized = true;
 		} else {
 			if (Main.log) Log.d(Main.TAG, "Already initialized.");
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		running--;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		running++;
 		if (isCacheOld(this)) { 
 			new Thread() {
 				public void run() {
 					refreshUnreadCount(DatabaseService.this, true);
 				}
 			}.start();
 		}
 		return START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	///////////////////////////////////////////////////////////////////////////
 	
 	private static synchronized String[] dbExec(String sql) {
 		if (Main.log) Log.d(Main.TAG, "Executing SQL: " + sql);
 		ArrayList<String> output = new ArrayList<String>();
 		Process p = null;
 		try {
 			p = Runtime.getRuntime().exec(new String[] {
 				"su",
 				"-c", 
 				"sqlite3 " + DB_PATH + " \"" + sql + "\""
 			});
 			
 			int exitValue = p.waitFor();
 			if (exitValue == 0) {
 				InputStream stdout = p.getInputStream();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
 				String s;
 				while ((s = reader.readLine()) != null) {
 					output.add(s);
 				}
 				reader.close();
 			} else {
 				throw new Exception("Non-zero exit status " + exitValue);
 			}
 		} catch (Exception ex) {
 			if (Main.log) {
 				Log.e(Main.TAG, "Exception while reading database", ex);
 				try {
 					String s;
 					InputStream stderr = p.getErrorStream();
 					BufferedReader reader = new BufferedReader(new InputStreamReader(stderr));
 					while ((s = reader.readLine()) != null) {
 						Log.e(Main.TAG, "STDERR: " + s);
 					}
 				} catch (Exception ex2) {
 					Log.e(Main.TAG, "Exception while reading STDERR", ex2);
 				}
 			}
 		}
 		
 		return output.toArray(new String[0]);
 	}
 
     private class DbContentObserver extends ContentObserver implements Runnable {
     	private Thread timeoutThread = null;
     	
 		public DbContentObserver() {
 			super(null);
 		}
 
 		public void run() {
 			if (Main.log) Log.d(Main.TAG, "Waiting...");
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				if (Main.log) Log.d(Main.TAG, "Stopped!");
 				return;
 			}
 
 			if (Main.log) Log.d(Main.TAG, "Refreshing count...");
 			DatabaseService.refreshUnreadCount(DatabaseService.this, false);
 			
 			timeoutThread = null;
 		}
     	
 		@Override
 		public void onChange(boolean selfChange) {
 			super.onChange(selfChange);
 			
 			if (Main.log) Log.d(Main.TAG, "Content changed!");
 			if (timeoutThread != null && timeoutThread.isAlive()) {
 				timeoutThread.interrupt();
 			}
 			
 			timeoutThread = new Thread(this);
 			timeoutThread.start();
 		}
 	}
 }
