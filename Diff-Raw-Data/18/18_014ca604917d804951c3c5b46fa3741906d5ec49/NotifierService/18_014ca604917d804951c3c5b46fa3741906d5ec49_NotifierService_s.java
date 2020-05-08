 package com.ioabsoftware.gameraven;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.ioabsoftware.gameraven.networking.Session;
 
 public class NotifierService extends IntentService {
 	
 	public static final String NOTIF_TAG = "GR_NOTIF";
 	public static final int AMP_NOTIF_ID = 1;
 	public static final int PM_NOTIF_ID = 2;
 	public static final int TT_NOTIF_ID = 3;
 	
 	
 	public NotifierService() {
 		super("GameRavenNotifierWorker");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d("notif", "starting onhandleintent");
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		String username = prefs.getString("defaultAccount", SettingsMain.NO_DEFAULT_ACCOUNT);
 		
 		// service does nothing if there is no default account set or there is no generated salt
 	    if (!username.equals(SettingsMain.NO_DEFAULT_ACCOUNT) && prefs.getString("secureSalt", null) != null) {
 			HashMap<String, String> cookies = new HashMap<String, String>();
 			String password = new SecurePreferences(getApplicationContext(), AllInOneV2.ACCOUNTS_PREFNAME, 
 													prefs.getString("secureSalt", null), false).getString(username);;
 			Log.d("notif", username);
 			String basePath = Session.ROOT + "/boards";
 			String loginPath = Session.ROOT + "/user/login.html";
 			try {
 				Response r = Jsoup.connect(basePath).method(Method.GET)
 						.cookies(cookies).timeout(10000).execute();
 
 				cookies.putAll(r.cookies());
 
 				Log.d("notif", "first connection finished");
 				Document pRes = r.parse();
 
 				String loginKey = pRes.getElementsByAttributeValue("name",
 						"key").attr("value");
 
 				HashMap<String, String> loginData = new HashMap<String, String>();
 				// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
 				loginData.put("EMAILADDR", username);
 				loginData.put("PASSWORD", password);
 				loginData.put("path", basePath);
 				loginData.put("key", loginKey);
 
 				Log.d("notif", username + ", " + loginPath
 						+ ", " + loginKey);
 
 				r = Jsoup.connect(loginPath).method(Method.POST)
 						.cookies(cookies).data(loginData).timeout(10000)
 						.execute();
 
 				cookies.putAll(r.cookies());
 
 				Log.d("notif", "second connection finished");
 
 				r = Jsoup.connect(Session.ROOT + "/boards/myposts.php?lp=-1")
 						.method(Method.GET).cookies(cookies).timeout(10000)
 						.execute();
 
 				Log.d("notif", "third connection finished");
 
 				if (r.statusCode() != 401) {
 					Log.d("notif", "status is good");
 					pRes = r.parse();
 					Log.d("notif", pRes.title());
 
 					NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 					
 					if (prefs.getBoolean("notifsAMPEnable", false)) {
 						Element lPost = pRes.select("td.lastpost").first();
 						if (lPost != null) {
 							// 4/25 8:23PM
 							// 1/24/2012
 							String lTime = lPost.text();
 							lTime = lTime.replace("Last:", "");
 							Log.d("notif", "time is " + lTime);
 							Date newDate;
 							if (lTime.contains("AM") || lTime.contains("PM"))
 								newDate = new SimpleDateFormat(
 										"MM'/'dd hh':'mmaa", Locale.US)
 										.parse(lTime);
 							else
 								newDate = new SimpleDateFormat(
 										"MM'/'dd'/'yyyy", Locale.US)
 										.parse(lTime);
 
 							long newTime = newDate.getTime();
 							long oldTime = prefs.getLong("notifsLastPost", 0);
 							if (newTime > oldTime) {
 								showNotif("New posts in AMP list found for " + username, AMP_NOTIF_ID, notifManager);
 								
 								Log.d("notif", "time is newer");
 								prefs.edit().putLong("notifsLastPost", newTime)
 										.apply();
 							}
 						}
 					}
 					
 					if (prefs.getBoolean("notifsPMEnable", false)) {
 						Element pmInboxLink = pRes.select("div.masthead_user").first().select("a[href=/pm/]").first();
 						if (pmInboxLink != null) {
 							String text = pmInboxLink.text();
 							int count = 0;
 							
 							if (text.contains("(")) {
 								count = Integer.parseInt(text.substring(text.indexOf('(') + 1, text.indexOf(')')));
 								int prevCount = prefs.getInt("notifsUnreadPMCount", 0);
 								if (count > prevCount) {
 									String msg;
 									if (count > 1)
										msg = "1 new PM found for " + username;
 									else
										msg = count + "new PMs found for " + username;
 									
 									showNotif(msg, PM_NOTIF_ID, notifManager);
 								}
 							}
 
 							prefs.edit().putInt("notifsUnreadPMCount", count).apply();
 						}
 					}
 					
 					if (prefs.getBoolean("notifsTTEnable", false)) {
 						Element trackedLink = pRes.select("div.masthead_user").first().select("a[href=/boards/tracked]").first();
 						if (trackedLink != null) {
 							String text = trackedLink.text();
 							int count = 0;
 							
 							if (text.contains("(")) {
 								count = Integer.parseInt(text.substring(text.indexOf('(') + 1, text.indexOf(')')));
 								int prevCount = prefs.getInt("notifsUnreadTTCount", 0);
 								if (count > prevCount) {
 									String msg;
 									if (count > 1)
										msg = "1 unread tracked topic found for " + username;
									else
 										msg = count + " unread tracked topic found for " + username;
 									
 									showNotif(msg, TT_NOTIF_ID, notifManager);
 								}
 							}
 
 							prefs.edit().putInt("notifsUnreadTTCount", count).apply();
 						}
 					}
 				}
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(this,
 						"There was an error checking for new messages",
 						Toast.LENGTH_SHORT).show();
 				Log.d("notif", "exception raised in notifierservice");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void showNotif(String msg, int id, NotificationManager notifManager) {
 		Notification.Builder ampNotifBuilder = new Notification.Builder(this)
 				.setSmallIcon(R.drawable.ic_notif_small)
 				.setContentTitle("GameRaven")
 				.setContentText(msg);
 		Intent notifIntent = new Intent(this, AllInOneV2.class);
 		PendingIntent pendingNotif = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_ONE_SHOT);
 		ampNotifBuilder.setContentIntent(pendingNotif);
 		ampNotifBuilder.setAutoCancel(true);
 		ampNotifBuilder.setDefaults(Notification.DEFAULT_ALL);
 		
 		notifManager.notify(NOTIF_TAG, id, ampNotifBuilder.getNotification());
 	}
 	
 	public static void dismissAMPNotif(Context c) {
 		((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIF_TAG, AMP_NOTIF_ID);
 	}
 	
 	public static void dismissPMNotif(Context c) {
 		((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIF_TAG, PM_NOTIF_ID);
 	}
 	
 	public static void dismissTTNotif(Context c) {
 		((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIF_TAG, TT_NOTIF_ID);
 	}
 	
 	public static void dismissAllNotifs(Context c) {
 		NotificationManager notifManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
 		notifManager.cancel(NOTIF_TAG, AMP_NOTIF_ID);
 		notifManager.cancel(NOTIF_TAG, PM_NOTIF_ID);
 		notifManager.cancel(NOTIF_TAG, TT_NOTIF_ID);
 	}
 
 }
