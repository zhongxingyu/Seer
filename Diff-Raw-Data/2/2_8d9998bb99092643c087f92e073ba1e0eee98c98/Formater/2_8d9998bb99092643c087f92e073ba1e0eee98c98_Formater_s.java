 package ch.almana.android.stillmeter.helper;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.Cursor;
 import ch.almana.android.stillmeter.model.BreastModel.Position;
 import ch.almana.android.stillmeter.provider.db.DB.Session;
 import ch.almana.android.stilltimer.R;
 
 public class Formater {
 
 	private static final SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 
 	public static String timeElapsed(long time) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(time);
 		StringBuilder sb = new StringBuilder();
		int h = cal.get(Calendar.HOUR);
 		if (h > 0) {
 			sb.append(h).append(":");
 		}
 		int min = cal.get(Calendar.MINUTE);
 		if (min < 10) {
 			sb.append("0");
 		}
 		sb.append(min).append(":");
 		int sec = cal.get(Calendar.SECOND);
 		if (sec < 10) {
 			sb.append("0");
 		}
 		sb.append(sec);
 		return sb.toString();
 	}
 
 	public static String formatTime(Date time) {
 		return timeFormat.format(time);
 	}
 
 	public static String formatDate(Date time) {
 		return dateFormat.format(time);
 	}
 
 	public static String formatDateTime(Date time) {
 		return timeDateFormat.format(time);
 	}
 
 	public static CharSequence sessionTime(Cursor cursor) {
 		StringBuilder sb = new StringBuilder();
 		Date time = new Date(cursor.getLong(Session.INDEX_TIME_START));
 		sb.append(Formater.formatTime(time));
 		time.setTime(cursor.getLong(Session.INDEX_TIME_END));
 		sb.append(" - ").append(Formater.formatTime(time));
 		return sb.toString();
 	}
 
 	public static CharSequence translatedBreast(Context ctx, String string) {
 		String breast = "";
 		switch (Position.valueOf(string)) {
 		case left:
 			breast = ctx.getString(R.string.left);
 			break;
 		case right:
 			breast = ctx.getString(R.string.right);
 			break;
 		case none:
 			breast = ctx.getString(R.string.none);
 			break;
 		}
 		return breast;
 	}
 }
