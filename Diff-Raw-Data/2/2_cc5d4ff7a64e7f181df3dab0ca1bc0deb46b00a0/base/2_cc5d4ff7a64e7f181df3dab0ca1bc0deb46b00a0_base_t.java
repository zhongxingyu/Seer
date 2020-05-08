 package carnero.cmny;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.widget.RemoteViews;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class base {
 	// settings
	public static String formatSms = "info@rb\\.cz:[a-zA-Z ]+ B:([0-9]{2})\\.([0-9]{2})\\.([0-9]{4}): ([0-9 ]+(,[0-9]*)?)#";
 	public static String formatDate = "yyyy.MM.dd";
 	public static String currencyBefore = "";
 	public static String currencyAfter = " Kč";
 	public static long refreshInterval = (15 * 60 * 1000); // fifteen mins
 
 	public static int BLACK = 0;
 	public static int WHITE = 1;
 	private SharedPreferences prefs = null;
 	private static Pattern patternMsg = Pattern.compile(formatSms, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 	private static SimpleDateFormat dateOut = new SimpleDateFormat(formatDate);
 
 	public void refresh(int skin, Context context) {
 		refresh(skin, context, null, null);
 	}
 
 	public void refresh(int skin, Context context, String text) {
 		refresh(skin, context, null, text);
 	}
 
 	public void refresh(int skin, Context context, int[] ids) {
 		refresh(skin, context, ids, null);
 	}
 
 	public void refresh(int skin, Context context, int[] ids, String intentText) {
 		RemoteViews views = null;
 		ArrayList<String> msgs = new ArrayList<String>();
 		Float stateMoney = null;
 		Calendar stateDate = null;
 		boolean stateChanged = false;
 
 		if (context == null) {
 			return;
 		}
 		
 		// load last known values
 		prefs = context.getSharedPreferences("cmny.prefs", 0);
 		if (prefs.contains("money")) {
 			stateMoney = prefs.getFloat("money", 0);
 		}
 		if (prefs.contains("date")) {
 			stateDate = Calendar.getInstance();
 			stateDate.setTimeInMillis(prefs.getLong("date", 0));
 		}
 
 		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
 		if (skin == WHITE) {
 			views = new RemoteViews("carnero.cmny", R.layout.layout_white);
 		} else {
 			views = new RemoteViews("carnero.cmny", R.layout.layout_black);
 		}
 
 		try {
 			if (intentText == null) { // no message received, read from database
 				int count = 0;
 
 				Cursor cursor = context.getContentResolver().query(
 						Uri.parse("content://sms/inbox"),
 						new String[] {"_id", "thread_id", "address", "person", "date", "body"},
 						null,
 						null,
 						"date desc"
 				);
 
 				if (cursor != null) {
 					try {
 						count = cursor.getCount();
 
 						if (count > 0) {
 							cursor.moveToFirst();
 
 							do {
 								msgs.add(cursor.getString(cursor.getColumnIndex("body")));
 							} while (cursor.moveToNext() != false);
 						}
 					} finally {
 						cursor.close();
 					}
 				}
 			} else { // we have new message, get it from intent
 				msgs.add(intentText);
 			}
 			
 			// parse last value
 			for (String msgContent : msgs) {
 				Matcher matcherMsg = patternMsg.matcher(msgContent);
 				while (matcherMsg.find()) {
 					String d = matcherMsg.group(1);
 					String m = matcherMsg.group(2);
 					String Y = matcherMsg.group(3);
 					String v = matcherMsg.group(4);
 					
 					if (d != null && m != null && Y != null && v != null) {
 						Calendar datePre = Calendar.getInstance();
 						datePre.set(Integer.parseInt(Y), (Integer.parseInt(m) - 1), Integer.parseInt(d));
 						
 						if (stateDate == null || stateDate.before(datePre)) {
 							stateMoney = new Float(v.replaceAll("[ ]+", "").replaceAll("[,]+", "."));
 							stateDate = datePre;
 							stateChanged = true;
 						}
 					}
 				}
 			}
 			
 			// display money
 			if (stateMoney != null && stateDate != null) {
 				views.setTextViewText(R.id.value, currencyBefore + String.format(Locale.getDefault(), "%.2f", stateMoney) + currencyAfter);
 				views.setTextViewText(R.id.date, dateOut.format(stateDate.getTime()));
 			} else {
 				views.setTextViewText(R.id.value, null);
 				views.setTextViewText(R.id.date, null);
 			}
 			
 			// set pendingintent on click
 			Intent intentWid = null;
 			PendingIntent intentPending = null;
 			AlarmManager alarmManager = null;
 
 			if (skin == WHITE) {
 				// touch
 				intentWid = new Intent(context, cmny_white.class);
 				intentWid.setAction("cmnyTouch");
 				intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 				views.setOnClickPendingIntent(R.id.widget, intentPending);
 
 				// update
 				intentWid = new Intent(context, cmny_white.class);
 				intentWid.setAction("cmnyUpdate");
 				intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 
 				alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 				alarmManager.cancel(intentPending);
 				alarmManager.setInexactRepeating(AlarmManager.RTC, (System.currentTimeMillis() + refreshInterval), refreshInterval, intentPending);
 			} else {
 				// touch
 				intentWid = new Intent(context, cmny_black.class);
 				intentWid.setAction("cmnyTouch");
 				intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 				views.setOnClickPendingIntent(R.id.widget, intentPending);
 
 				// update
 				intentWid = new Intent(context, cmny_black.class);
 				intentWid.setAction("cmnyUpdate");
 				intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 
 				alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 				alarmManager.cancel(intentPending);
 				alarmManager.setInexactRepeating(AlarmManager.RTC, (System.currentTimeMillis() + refreshInterval), refreshInterval, intentPending);
 			}
 
 			// refresh widgets
 			if (ids != null && ids.length > 0) {
 				final int idsCnt = ids.length;
 
 				for (int i = 0; i < idsCnt; i++) {
 					manager.updateAppWidget(ids[i], views);
 				}
 			} else {
 				if (skin == WHITE) {
 					final ComponentName component = new ComponentName(context, cmny_white.class);
 					manager.updateAppWidget(component, views);
 				} else {
 					final ComponentName component = new ComponentName(context, cmny_black.class);
 					manager.updateAppWidget(component, views);
 				}
 			}
 			
 			// save last value
 			if (stateChanged == true && stateMoney != null && stateDate != null) {
 				final SharedPreferences.Editor prefsEdit = prefs.edit();
 				
 				prefsEdit.putFloat("money", stateMoney);
 				prefsEdit.putLong("date", stateDate.getTimeInMillis());
 				
 				prefsEdit.commit();
 			}
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 }
