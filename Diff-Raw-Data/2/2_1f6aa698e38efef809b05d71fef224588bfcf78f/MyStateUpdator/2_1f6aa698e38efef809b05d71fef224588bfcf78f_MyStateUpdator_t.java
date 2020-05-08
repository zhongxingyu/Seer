 package fr.ydelouis.overflowme.receiver;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EReceiver;
 import com.googlecode.androidannotations.annotations.SystemService;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.support.ConnectionSource;
 
 import fr.ydelouis.overflowme.R;
 import fr.ydelouis.overflowme.api.entity.User;
 import fr.ydelouis.overflowme.entity.Notif;
 import fr.ydelouis.overflowme.loader.MeLoader;
 import fr.ydelouis.overflowme.loader.NotifLoader;
 import fr.ydelouis.overflowme.model.DatabaseHelper;
 import fr.ydelouis.overflowme.model.MeStore;
 import fr.ydelouis.overflowme.model.NotifDao;
 import fr.ydelouis.overflowme.util.NotifManager;
 import fr.ydelouis.overflowme.util.PrefManager;
 
 @EReceiver
 public class MyStateUpdator extends BroadcastReceiver
 {
 	public static final String ACTION_UPDATE = "action_update";
 	public static final String EVENT_MYSTATEUPDATED = "fr.ydelouis.overflowme.event.MYSTATE_UPDATED";
 	private static final int MINUTE = 60*1000;
 	
 	private static PendingIntent scheduledIntent;
 	private static boolean working = false;
 	
 	@SystemService
 	protected ConnectivityManager connectivityManager;
 	@SystemService
 	protected AlarmManager alarmManager;
 	@Bean
 	protected MeLoader meLoader;
 	@Bean
 	protected NotifLoader notifLoader;
 	@Bean
 	protected MeStore meStore;
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 		if(ACTION_UPDATE.equals(action))
 			update(context);
 		else {
 			scheduleUpdate(context);
 			update(context);
 		}
 	}
 	
 	@Background
 	protected void update(Context context) {
 		if(working)
 			return;
		if(connectivityManager.getActiveNetworkInfo() == null)
			return;
 		if(!connectivityManager.getActiveNetworkInfo().isConnected())
 			return;
 		working = true;
 		User lastSeenMe = meStore.getLastSeenMe();
 		meLoader.load();
 		notifLoader.load();
 		if(lastSeenMe != null)
 			sendNotification(context, lastSeenMe);
 		context.sendBroadcast(new Intent(EVENT_MYSTATEUPDATED));
 		working = false;
 	}
 	
 	private void sendNotification(Context context, User lastSeenMe) {
 		if(!PrefManager.getBoolean(context, R.string.pref_notifs_onOff, true))
 			return;
 		
 		int reputationChange = 0;
 		if(PrefManager.getBoolean(context, R.string.pref_notifs_reputationChange, true)) {
 			User meNow = meStore.getMe();
 			User lastMe = meStore.getLastSeenMe();
 			reputationChange = meNow.getReputation() - lastMe.getReputation();
 		}
 		
 		List<Notif> unreadNotifs = new ArrayList<Notif>();
 		if(PrefManager.getBoolean(context, R.string.pref_notifs_notifs, true)) {
 			long lastSeenDate = meStore.getLastSeenDate();
 			try {
 				ConnectionSource conSrc = OpenHelperManager.getHelper(context, DatabaseHelper.class).getConnectionSource();
 				NotifDao notifDao = DaoManager.createDao(conSrc, Notif.class);
 				List<Notif> notifs = notifDao.queryForAll();
 				for(Notif notif : notifs) {
 					if(notif.isUnread(lastSeenDate))
 						unreadNotifs.add(notif);
 				}
 			} catch (SQLException e) {}
 		}
 		NotifManager.notify(context, reputationChange, unreadNotifs);
 	}
 	
 	private void scheduleUpdate(Context context) {
 		if(scheduledIntent == null) {
 			Intent intent = new Intent(context, MyStateUpdator_.class);
 			intent.setAction(ACTION_UPDATE);
 			scheduledIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
 		}
 		cancelUpdate();
 		
 		long interval = Integer.valueOf(PrefManager.getString(context, R.string.pref_notifs_frequencyUpdate, "30"));
 		interval *= MINUTE;
 		if(interval != 0)
 			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, interval, interval, scheduledIntent);
 	}
 	
 	private void cancelUpdate() {
 		alarmManager.cancel(scheduledIntent);
 	}
 }
