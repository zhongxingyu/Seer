 package net.tailriver.agoraguide;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class Favorite {
 	private static final String PREFERENCE_NAME = "2012_favorite";
 
 	public static final boolean isFavorite(EntrySummary summary) {
 		return getSharedPreferences().contains(summary.getId());
 	}
 
 	public static void setFavorite(EntrySummary summary, boolean newState) {
 		SharedPreferences pref = getSharedPreferences();
 		int requestCode = pref.getInt(summary.getId(), -1);
 		if (!summary.getCategory().isAllday()) {
 			for (TimeFrame t : TimeFrame.getAll(summary)) {
 				Calendar cal = t.getStart();
 				long notificationWhen = cal.getTimeInMillis();
 				if (newState) {
 					cal.add(Calendar.MINUTE, -15);
 					long alarmWhen = cal.getTimeInMillis();
 					requestCode = ScheduleAlarm.setAlarm(summary, alarmWhen, notificationWhen);
 				} else {
 					if (requestCode > -1) {
 						ScheduleAlarm.cancelAlarm(summary, notificationWhen);
 					}
 				}
 			}
 		}
 		if (newState) {
 			pref.edit().putInt(summary.getId(), requestCode).commit();				
 		} else {
 			pref.edit().remove(summary.getId()).commit();			
 		}
 	}
 
 	public static Collection<EntrySummary> values() {
 		Collection<EntrySummary> collection = new HashSet<EntrySummary>();
 		for (String key : getSharedPreferences().getAll().keySet()) {
			collection.add(EntrySummary.get(key));
 		}
 		return collection;
 	}
 
 	public static void clear() {
 		for (EntrySummary summary : values()) {
 			Favorite.setFavorite(summary, false);
 		}
 		getSharedPreferences().edit().clear().commit();
 	}
 
 	private static final SharedPreferences getSharedPreferences() {
 		Context context = AgoraActivity.getStaticApplicationContext();
 		return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
 	}
 }
