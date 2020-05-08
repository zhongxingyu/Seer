 package ch.almana.android.stechkarte.model;
 
 import java.util.Calendar;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import ch.almana.android.stechkarte.provider.DB;
 import ch.almana.android.stechkarte.provider.DB.Holidays;
 
 public class HolidayAccess {
 
	public static void updateYearly(Context ctx, Timestamp timestamp) {
		long time;
		if (timestamp == null) {
			time = System.currentTimeMillis();
		} else {
			time = timestamp.getTimestamp();
		}
 		Cursor c = null;
 		try {
 			ContentResolver resolver = ctx.getContentResolver();
 			c = resolver.query(Holidays.CONTENT_URI, Holidays.DEFAULT_PROJECTION, Holidays.SELCTION_UPDATE_YEARLY_HOLIDAYS, new String[] { Long.toString(time) },
 				Holidays.DEFAULT_SORTORDER);
 			while (c != null && c.moveToNext()) {
 				Holiday h = new Holiday(c);
 				Calendar cal = h.getStartAsCalendar();
 				long t = cal.getTimeInMillis();
 				int i = 0;
 				while (t < time) {
 					i++;
 					cal.add(Calendar.YEAR, 1);
 					t = cal.getTimeInMillis();
 				}
 				h.setStart(t);
 				cal = h.getEndAsCalendar();
 				cal.add(Calendar.YEAR, i);
 				h.setEnd(cal.getTimeInMillis());
 				resolver.update(Holidays.CONTENT_URI, h.getValues(), DB.SELECTION_BY_ID, new String[] { Long.toString(h.getId()) });
 			}
 		} finally {
 			if (c != null && !c.isClosed()) {
 				c.close();
 			}
 		}
 		
 	}
 
 }
