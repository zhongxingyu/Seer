 package com.ioabsoftware.DroidFAQs;
 
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
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.ioabsoftware.DroidFAQs.Networking.Session;
 import com.ioabsoftware.gameraven.R;
 
 public class NotifierService extends IntentService {
 	
 	private static final int NOTIF_ID = 1;
 	
 	public NotifierService() {
 		super("GameRavenNotifierWorker");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d("notif", "starting onhandleintent");
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		String username = prefs.getString("defaultAccount", "N/A");
 		
 	    if (!username.equals("N/A")) {
 			HashMap<String, String> cookies = new HashMap<String, String>();
 			String password = new AccountPreferences(getApplicationContext(), AllInOneV2.ACCOUNTS_PREFNAME, 
 													 AllInOneV2.SALT, false).getString(username);;
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
 					Element lPost = pRes.select("td.lastpost").first();
 					if (lPost != null) {
 						// 4/25 8:23PM
 						// 1/24/2012
 						String lTime = lPost.text();
 						Log.d("notif", "time is " + lTime);
 						Date newDate;
 						if (lTime.contains("AM") || lTime.contains("PM"))
 							newDate = new SimpleDateFormat("MM'/'dd hh':'mmaa",
 									Locale.US).parse(lTime);
 						else
 							newDate = new SimpleDateFormat("MM'/'dd'/'yyyy",
 									Locale.US).parse(lTime);
 
 						long newTime = newDate.getTime();
 						long oldTime = prefs.getLong("notifsLastPost", 0);
						if (newTime > oldTime) {
 							Log.d("notif", "time is newer");
 							prefs.edit().putLong("notifsLastPost", newTime)
 									.commit();
 
 							NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(
 									this)
 									.setSmallIcon(R.drawable.ic_notif_small)
 									.setContentTitle("GameRaven")
 									.setContentText("New posts found, click to view");
 
 							Intent ampCheck = new Intent(this, AllInOneV2.class);
 							ampCheck.putExtra("forceAMP", true);
 
 							// The stack builder object will contain an artificial back stack for the started Activity.
 							// This ensures that navigating backward from the Activity leads out of
 							// your application to the Home screen.
 							TaskStackBuilder stackBuilder = TaskStackBuilder
 									.create(this);
 							// Adds the back stack for the Intent (but not the Intent itself)
 							stackBuilder.addParentStack(AllInOneV2.class);
 							// Adds the Intent that starts the Activity to the top of the stack
 							stackBuilder.addNextIntent(ampCheck);
 
 							PendingIntent pendingAMPCheck = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 
 							notifBuilder.setContentIntent(pendingAMPCheck);
 							notifBuilder.setAutoCancel(true);
 							notifBuilder.setDefaults(Notification.DEFAULT_ALL);
 
 							NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 							// mId allows you to update the notification later on.
 							notifManager.notify(NOTIF_ID, notifBuilder.build());
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
 
 }
