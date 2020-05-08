 package ch.almana.android.stechkarte.provider.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.util.Log;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.provider.StechkarteProvider;
 
 public interface DB {
 
 	public static final String DATABASE_NAME = "stechkarte";
 
 	public static final String NAME_ID = "_id";
 	public static final int INDEX_ID = 0;
 
 	// FIXME insert lastupdated as long
 	// timestamp index in timestamps
 
 	public class OpenHelper extends SQLiteOpenHelper {
 
 		private static final int DATABASE_VERSION = 7;
 
 		private static final String CREATE_TIMESTAMPS_TABLE = "create table if not exists " + DB.Timestamps.TABLE_NAME + " (" + DB.NAME_ID
 				+ " integer primary key, "
 				+ Timestamps.NAME_TIMESTAMP_TYPE + " int," + Timestamps.NAME_TIMESTAMP + " long, " + Timestamps.NAME_DAYREF + " long);";
 
 		private static final String CREATE_DAYS_TABLE = "create table if not exists " + Days.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ Days.NAME_DAYREF
 				+ " long, " + Days.NAME_HOURS_WORKED + " real, " + Days.NAME_HOURS_TARGET + " real," + Days.NAME_HOLIDAY + " real, " + Days.NAME_HOLIDAY_LEFT
 				+ " real, "
 				+ Days.NAME_OVERTIME + " real, " + Days.NAME_ERROR + " int, " + Days.NAME_FIXED + " int, " + Days.NAME_LAST_UPDATED + " long, "
				+ Days.NAME_MONTHREF + " long);";
 
 		private static final String CREATE_MONTH_TABLE = "create table if not exists " + Months.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ Months.NAME_MONTHREF
 				+ " long, " + Months.NAME_HOURS_WORKED + " real, " + Months.NAME_HOURS_TARGET + " real," + Months.NAME_HOLIDAY + " real, "
 				+ Months.NAME_HOLIDAY_LEFT + " real, "
 				+ Months.NAME_OVERTIME + " real, " + Months.NAME_ERROR + " int, " + Months.NAME_LAST_UPDATED + " long);";
 
 		private static final String CREATE_WEEK_TABLE = "create table if not exists " + Weeks.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ Weeks.NAME_WEEKREF
 				+ " long, " + Weeks.NAME_HOURS_WORKED + " real, " + Weeks.NAME_HOURS_TARGET + " real," + Weeks.NAME_HOLIDAY + " real, "
 				+ Weeks.NAME_HOLIDAY_LEFT + " real, "
 				+ Weeks.NAME_OVERTIME + " real, " + Weeks.NAME_ERROR + " int, " + Weeks.NAME_LAST_UPDATED + " long);";
 
 		private static final String LOG_TAG = Logger.LOG_TAG;
 
 		public OpenHelper(Context context) {
 			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_TIMESTAMPS_TABLE);
 			db.execSQL(CREATE_DAYS_TABLE);
 			db.execSQL(CREATE_MONTH_TABLE);
 			db.execSQL(CREATE_WEEK_TABLE);
 			db.execSQL("create index ts_idx on " + Timestamps.TABLE_NAME + " (" + Timestamps.NAME_TIMESTAMP + "); ");
 			db.execSQL("create unique index dayref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_DAYREF + "); ");
 			db.execSQL("create index ts_dayref_idx on " + Timestamps.TABLE_NAME + " (" + Timestamps.NAME_DAYREF + "); ");
 			db.execSQL("create unique index months_monthref_idx on " + Months.TABLE_NAME + " (" + Months.NAME_MONTHREF + "); ");
 			db.execSQL("create index days_monthref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_MONTHREF + "); ");
 			db.execSQL("create index days_weekref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_WEEKREF + "); ");
 			db.execSQL("create unique index weeks_weekref_idx on " + Weeks.TABLE_NAME + " (" + Weeks.NAME_WEEKREF + "); ");
 			Log.i(LOG_TAG, "Created table " + DB.Timestamps.TABLE_NAME);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			switch (oldVersion) {
 			case 1:
 				Log.w(LOG_TAG, "Upgrading to DB Version 2...");
 				db.execSQL(CREATE_DAYS_TABLE);
 				db.execSQL("alter table " + Timestamps.TABLE_NAME + " add column " + Timestamps.NAME_DAYREF + " long;");
 				// nobreak
 			case 2:
 				Log.w(LOG_TAG, "Upgrading to DB Version 3...");
 				db.execSQL("alter table " + Days.TABLE_NAME + " add column " + Days.NAME_FIXED + " int;");
 				// nobreak
 			case 3:
 				Log.w(LOG_TAG, "Upgrading to DB Version 4...");
 				db.execSQL("create unique index dayref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_DAYREF + "); ");
 
 			case 4:
 				Log.w(LOG_TAG, "Upgrading to DB Version 5...");
 				db.execSQL("alter table " + Days.TABLE_NAME + " add column " + Days.NAME_LAST_UPDATED + " long;");
 				// nobreak
 
 			case 5:
 				Log.w(LOG_TAG, "Upgrading to DB Version 6...");
 				db.execSQL(CREATE_MONTH_TABLE);
 				db.execSQL("alter table " + Days.TABLE_NAME + " add column " + Days.NAME_MONTHREF + " long;");
 				db.execSQL("create index ts_idx on " + Timestamps.TABLE_NAME + " (" + Timestamps.NAME_TIMESTAMP + "); ");
 				db.execSQL("create unique index months_monthref_idx on " + Months.TABLE_NAME + " (" + Months.NAME_MONTHREF + "); ");
 				db.execSQL("create index days_monthref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_MONTHREF + "); ");
 				db.execSQL("create index ts_dayref_idx on " + Timestamps.TABLE_NAME + " (" + Timestamps.NAME_DAYREF + "); ");
 				// nobreak
 
 			case 6:
 				Log.w(LOG_TAG, "Upgrading to DB Version 7...");
 				db.execSQL(CREATE_WEEK_TABLE);
 				db.execSQL("alter table " + Days.TABLE_NAME + " add column " + Days.NAME_WEEKREF + " long;");
 				db.execSQL("create index days_weekref_idx on " + Days.TABLE_NAME + " (" + Days.NAME_WEEKREF + "); ");
 				db.execSQL("create unique index weeks_weekref_idx on " + Weeks.TABLE_NAME + " (" + Weeks.NAME_WEEKREF + "); ");
 				// nobreak
 
 			default:
 				Log.w(LOG_TAG, "Finished DB upgrading!");
 				break;
 			}
 		}
 
 	}
 
 	public interface Timestamps {
 
 		static final String TABLE_NAME = "timestamps";
 
 		public static final String CONTENT_ITEM_NAME = "timestamp";
 		public static String CONTENT_URI_STRING = "content://" + StechkarteProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_TIMESTAMP_TYPE = "type";
 		public static final String NAME_TIMESTAMP = "timestamp";
 		public static final String NAME_DAYREF = "dayRef";
 
 		public static final int INDEX_TIMESTAMP = 1;
 		public static final int INDEX_TIMESTAMP_TYPE = 2;
 		public static final int INDEX_DAYREF = 3;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_TIMESTAMP, NAME_TIMESTAMP_TYPE };
 		public static final String[] DEFAULT_PROJECTION = colNames;
 
 		public static final String DEFAUL_SORTORDER = NAME_TIMESTAMP + " DESC";
 
 		static final String REVERSE_SORTORDER = NAME_TIMESTAMP + " ASC";
 
 	}
 
 	public interface Days {
 		static final String TABLE_NAME = "days";
 
 		public static final String CONTENT_ITEM_NAME = "day";
 		public static String CONTENT_URI_STRING = "content://" + StechkarteProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_DAYREF = "dayRef";
 		public static final String NAME_HOURS_WORKED = "hoursWorked";
 		public static final String NAME_HOURS_TARGET = "hoursTarget";
 		public static final String NAME_HOLIDAY = "holiday";
 		public static final String NAME_HOLIDAY_LEFT = "holidayLeft";
 		public static final String NAME_OVERTIME = "overtime";
 		public static final String NAME_ERROR = "error";
 		public static final String NAME_FIXED = "fixed";
 		public static final String NAME_LAST_UPDATED = "lastUpdated";
 		public static final String NAME_MONTHREF = "monthRef";
 		public static final String NAME_WEEKREF = "weekRef";
 
 		public static final int INDEX_DAYREF = 1;
 		public static final int INDEX_HOURS_WORKED = 2;
 		public static final int INDEX_HOURS_TARGET = 3;
 		public static final int INDEX_HOLIDAY = 4;
 		public static final int INDEX_HOLIDAY_LEFT = 5;
 		public static final int INDEX_OVERTIME = 6;
 		public static final int INDEX_ERROR = 7;
 		public static final int INDEX_FIXED = 8;
 		public static final int INDEX_LAST_UPDATED = 9;
 		public static final int INDEX_MONTHREF = 10;
 		public static final int INDEX_WEEKREF = 11;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_DAYREF, NAME_HOURS_WORKED, NAME_HOURS_TARGET, NAME_HOLIDAY, NAME_HOLIDAY_LEFT,
 				NAME_OVERTIME,
 				NAME_ERROR, NAME_FIXED, NAME_LAST_UPDATED, NAME_MONTHREF, NAME_WEEKREF };
 		public static final String[] DEFAULT_PROJECTION = colNames;
 
 		public static final String DEFAULT_SORTORDER = NAME_DAYREF + " DESC";
 		public static final String REVERSE_SORTORDER = NAME_DAYREF + " ASC";
 
 		static final String[] PROJECTTION_DAYREF = new String[] { NAME_DAYREF };
 
 	}
 
 	public interface Months {
 		static final String TABLE_NAME = "months";
 
 		public static final String CONTENT_ITEM_NAME = "month";
 		public static String CONTENT_URI_STRING = "content://" + StechkarteProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_MONTHREF = "monthRef";
 		public static final String NAME_HOURS_WORKED = "hoursWorked";
 		public static final String NAME_HOURS_TARGET = "hoursTarget";
 		public static final String NAME_HOLIDAY = "holiday";
 		public static final String NAME_HOLIDAY_LEFT = "holidayLeft";
 		public static final String NAME_OVERTIME = "overtime";
 		public static final String NAME_ERROR = "error";
 		public static final String NAME_LAST_UPDATED = "lastUpdated";
 
 		public static final int INDEX_MONTHREF = 1;
 		public static final int INDEX_HOURS_WORKED = 2;
 		public static final int INDEX_HOURS_TARGET = 3;
 		public static final int INDEX_HOLIDAY = 4;
 		public static final int INDEX_HOLIDAY_LEFT = 5;
 		public static final int INDEX_OVERTIME = 6;
 		public static final int INDEX_ERROR = 7;
 		public static final int INDEX_LAST_UPDATED = 8;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_MONTHREF, NAME_HOURS_WORKED, NAME_HOURS_TARGET, NAME_HOLIDAY, NAME_HOLIDAY_LEFT,
 				NAME_OVERTIME,
 				NAME_ERROR, NAME_LAST_UPDATED };
 		public static final String[] DEFAULT_PROJECTION = colNames;
 
 		public static final String DEFAULT_SORTORDER = NAME_MONTHREF + " DESC";
 		public static final String REVERSE_SORTORDER = NAME_MONTHREF + " ASC";
 
 		static final String[] PROJECTTION_MONTHREF = new String[] { NAME_MONTHREF };
 
 	}
 
 	public interface Weeks {
 		static final String TABLE_NAME = "weeks";
 
 		public static final String CONTENT_ITEM_NAME = "week";
 		public static String CONTENT_URI_STRING = "content://" + StechkarteProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StechkarteProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_WEEKREF = "weekrefRef";
 		public static final String NAME_HOURS_WORKED = "hoursWorked";
 		public static final String NAME_HOURS_TARGET = "hoursTarget";
 		public static final String NAME_HOLIDAY = "holiday";
 		public static final String NAME_HOLIDAY_LEFT = "holidayLeft";
 		public static final String NAME_OVERTIME = "overtime";
 		public static final String NAME_ERROR = "error";
 		public static final String NAME_LAST_UPDATED = "lastUpdated";
 
 		public static final int INDEX_WEEKREF = 1;
 		public static final int INDEX_HOURS_WORKED = 2;
 		public static final int INDEX_HOURS_TARGET = 3;
 		public static final int INDEX_HOLIDAY = 4;
 		public static final int INDEX_HOLIDAY_LEFT = 5;
 		public static final int INDEX_OVERTIME = 6;
 		public static final int INDEX_ERROR = 7;
 		public static final int INDEX_LAST_UPDATED = 8;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_WEEKREF, NAME_HOURS_WORKED, NAME_HOURS_TARGET, NAME_HOLIDAY, NAME_HOLIDAY_LEFT,
 				NAME_OVERTIME,
 				NAME_ERROR, NAME_LAST_UPDATED };
 		public static final String[] DEFAULT_PROJECTION = colNames;
 
 		public static final String DEFAULT_SORTORDER = NAME_WEEKREF + " DESC";
 		public static final String REVERSE_SORTORDER = NAME_WEEKREF + " ASC";
 
 		static final String[] PROJECTTION_MONTHREF = new String[] { NAME_WEEKREF };
 
 	}
 
 }
