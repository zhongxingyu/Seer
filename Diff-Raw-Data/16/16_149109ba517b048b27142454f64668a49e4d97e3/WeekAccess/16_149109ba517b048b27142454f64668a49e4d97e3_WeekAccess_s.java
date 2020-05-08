 package ch.almana.android.stechkarte.model;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.provider.IAccess;
 import ch.almana.android.stechkarte.provider.db.DB;
 import ch.almana.android.stechkarte.provider.db.DB.Days;
 import ch.almana.android.stechkarte.provider.db.DB.Weeks;
 import ch.almana.android.stechkarte.utils.Settings;
 
 public class WeekAccess implements IAccess {
 	private static final String LOG_TAG = Logger.LOG_TAG;
 
	private static SimpleDateFormat weekRefDateFormat = new SimpleDateFormat("yyyyMM");
 
 	private final Context context;
 
 	private boolean doNotRecalculate;
 
 	private static WeekAccess instance;
 	public static final float HOURS_IN_MILLIES = 1000f * 60f * 60f;
 
 	public static void initInstance(Context context) {
 		instance = new WeekAccess(context);
 	}
 
 	public static WeekAccess getInstance() {
 		return instance;
 	}
 
 	public WeekAccess(Context context) {
 		super();
 		this.context = context;
 	}
 
 	public Context getContext() {
 		return context;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int count = getContext().getContentResolver().delete(uri, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues initialValues) {
 		Uri ret = getContext().getContentResolver().insert(uri, initialValues);
 		getContext().getContentResolver().notifyChange(ret, null);
 		return ret;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		return getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		int count = getContext().getContentResolver().update(uri, values, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	public void insert(Week week) {
 		Uri uri = insert(Weeks.CONTENT_URI, week.getValues());
 		long id = ContentUris.parseId(uri);
 		if (id > 0) {
 			week.setId(id);
 		}
 	}
 
 	public boolean hasWeekRef(long weekref) {
 		Cursor c = null;
 		try {
 			c = query(Weeks.CONTENT_URI, Weeks.PROJECTTION_MONTHREF, Weeks.NAME_WEEKREF + "=" + weekref, null, Weeks.DEFAULT_SORTORDER);
 			return c.moveToFirst();
 		} finally {
 			if (c != null) {
 				c.close();
 				c = null;
 			}
 
 		}
 	}
 
 	@Override
 	public Cursor query(String selection) {
 		return query(selection, Weeks.DEFAULT_SORTORDER);
 	}
 
 	public Cursor query(String selection, String sortOrder) {
 		return query(Weeks.CONTENT_URI, Weeks.DEFAULT_PROJECTION, selection, null, sortOrder);
 	}
 
 	public Week getOrCreateWeek(long weekref) {
 		Week m;
 		Cursor c = null;
 		try {
 			c = query(Weeks.NAME_WEEKREF + "=" + weekref);
 			if (c.moveToFirst()) {
 				m = new Week(c);
 			} else {
 				m = new Week(weekref);
 			}
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 		return m;
 	}
 
 	public void insertOrUpdate(Week week) {
 		if (week.getId() > -1) {
 			update(week);
 		} else {
 			insert(week);
 		}
 	}
 
 	public void update(Week week) {
 		update(Weeks.CONTENT_URI, week.getValues(), DB.NAME_ID + "=" + week.getId(), null);
 	}
 
 	private Week getWeekBefore(Week currentWeek) {
 		Week m = null;
 		Cursor c = query(Weeks.NAME_WEEKREF + "<" + currentWeek.getWeekRef(), Weeks.DEFAULT_SORTORDER);
 		if (c.moveToFirst()) {
 			m = new Week(c);
 		}
 		c.close();
 		return m;
 	}
 
 	public void recalculate(long weekRef) {
 		if (doNotRecalculate) {
 			return;
 		}
 		if (weekRef < 1) {
 			return;
 		}
 		Week m = getOrCreateWeek(weekRef);
 		recalculate(m);
 	}
 
 	public void recalculate(Week week) {
 		if (doNotRecalculate) {
 			return;
 		}
 		week.setLastUpdated(System.currentTimeMillis());
 
 		long weekRef = week.getWeekRef();
 		Week previousWeek = getWeekBefore(week);
 		if (previousWeek == null) {
 			previousWeek = new Week(0);
 		}
 		Log.i(LOG_TAG, "Recalculating " + weekRef + " with prev week " + previousWeek.getWeekRef());
 		float worked = 0;
 		float target = 0;
 		float holiday = 0;
 
 		Cursor c = week.getDays();
 		boolean error = false;
 		Day day = null;
 		while (c.moveToNext()) {
 			day = new Day(c);
 			worked = worked + day.getHoursWorked();
 			target = target + day.getHoursTarget();
 			holiday = holiday + day.getHolyday();
 			error = error ? error : day.isError();
 
 		}
 		if (!c.isClosed()) {
 			c.close();
 		}
 
 		week.setError(error);
 		week.setHoursWorked(worked);
 		week.setHoursTarget(target);
 		week.setHolyday(holiday);
 		if (day != null) {
 			week.setOvertime(day.getOvertime());
 			week.setHolydayLeft(day.getHolydayLeft());
 		}
 		Log.w(Logger.LOG_TAG, "Rebuild week: " + weekRef + " w:" + worked + " target " + target);
 		week.setLastUpdated(System.currentTimeMillis());
 		insertOrUpdate(week);
 	}
 
 	public static long getWeekRefFromDayRef(long dayRef) {
 		try {
 			return getWeekRefFromTimestamp(DayAccess.timestampFromDayRef(dayRef));
 		} catch (ParseException e) {
 			Log.w(Logger.LOG_TAG, "Cannot parse " + dayRef + " as daref", e);
 			return -1;
 		}
 	}
 
 	public static long getWeekRefFromTimestamp(long timestamp) {
 		return getWeekRefFromDate(new Date(timestamp));
 	}
 
 	public static long getWeekRefFromDate(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		int firstDayOfWeek = Settings.getInstance().getFirstDayOfWeek();
 		if (firstDayOfWeek > -1) {
			c.setFirstDayOfWeek(firstDayOfWeek);
 		}
		//return c.get(Calendar.WEEK_OF_YEAR);
		return Long.parseLong( weekRefDateFormat.format(c.getTime()) );
 	}
 
 	// public static long getTimestampFromWeekRef(long weekref) throws
 	// ParseException {
 	// try {
 	// return weekRefDateFormat.parse(weekref + "").getTime();
 	// } catch (ParseException e) {
 	// Log.w(LOG_TAG, "Cannot parse " + weekref + " as weekref", e);
 	// return 0;
 	// }
 	// }
 
 	public Week getOldestUpdatedWeek(long since) {
 		Cursor cursor = query(Weeks.NAME_LAST_UPDATED + " > " + since, Weeks.NAME_WEEKREF + " ASC");
 		if (cursor.moveToFirst()) {
 			return new Week(cursor);
 		} else {
 			return null;
 		}
 	}
 //
 //	public void recalculateWeekFromTimestamp(Timestamp timestamp, IProgressWrapper progressWrapper) {
 //		// TODO Auto-generated method stub
 //		// FIXME dfydfasd
 //	}
 
 	public void setDoNotRecalculate(boolean b) {
 		this.doNotRecalculate = b;
 	}
 
 	public void rebuildFromDayRef(long dayref) {
 		SortedSet<Long> weekRefs = new TreeSet<Long>();
 		String selection = null;
 		selection = DB.Days.NAME_DAYREF + ">=" + dayref;
 
 		DayAccess dayAccess = DayAccess.getInstance();
 		Cursor c = dayAccess.query(selection, Days.REVERSE_SORTORDER);
 
 		while (c.moveToNext()) {
 			Day d = new Day(c);
 			long weekref = WeekAccess.getWeekRefFromDayRef(d.getDayRef());
 			if (weekRefs.add(weekref)) {
 				Log.i(LOG_TAG, "Added week " + weekref + " for recalculation");
 			}
 		}
 
 		for (Iterator<Long> iterator = weekRefs.iterator(); iterator.hasNext();) {
 			Long weekRef = iterator.next();
 			recalculate(weekRef);
 		}
 	}
 }
