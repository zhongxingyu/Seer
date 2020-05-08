 package com.seawolfsanctuary.tmt.database;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.tmt.Helpers;
 
 public class Journey {
 	private DatabaseHelper DBHelper;
 	private SQLiteDatabase db;
 
 	private final Context context;
 
 	private static final String DATABASE_NAME = "tmt";
 	private static final String DATABASE_TABLE = "journeys";
 	private static final int DATABASE_VERSION = 2;
 
 	public static final String KEY_ROWID = "_id";
 
 	public static final String KEY_FROM_STATION = "from_station";
 	public static final String KEY_FROM_DAY = "from_day";
 	public static final String KEY_FROM_MONTH = "from_month";
 	public static final String KEY_FROM_YEAR = "from_year";
 	public static final String KEY_FROM_HOUR = "from_hour";
 	public static final String KEY_FROM_MINUTE = "from_minute";
 
 	public static final String KEY_TO_STATION = "to_station";
 	public static final String KEY_TO_DAY = "to_day";
 	public static final String KEY_TO_MONTH = "to_month";
 	public static final String KEY_TO_YEAR = "to_year";
 	public static final String KEY_TO_HOUR = "to_hour";
 	public static final String KEY_TO_MINUTE = "to_minute";
 
 	public static final String KEY_CLASS = "class";
 	public static final String KEY_HEADCODE = "headcode";
 
 	public static final String KEY_STATS = "use_for_stats";
 
 	private static final String DATABASE_CREATE = "create table "
 			+ DATABASE_TABLE + " (" + KEY_ROWID
 			+ " integer primary key autoincrement, " + KEY_FROM_STATION
 			+ " text not null, " + KEY_FROM_DAY + " int not null, "
 			+ KEY_FROM_MONTH + " int not null, " + KEY_FROM_YEAR
 			+ " int not null, " + KEY_FROM_HOUR + " int not null, "
 			+ KEY_FROM_MINUTE + " int not null, "
 
 			+ KEY_TO_STATION + " text not null, " + KEY_TO_DAY
 			+ " int not null, " + KEY_TO_MONTH + " int not null, "
 			+ KEY_TO_YEAR + " int not null, " + KEY_TO_HOUR + " int not null, "
 			+ KEY_TO_MINUTE + " int not null, " + KEY_CLASS
 			+ " text not null, " + KEY_HEADCODE + " text not null, "
 			+ KEY_STATS + " int not null default '1' " + ");";
 
 	public Journey(Context c) {
 		this.context = c;
 		DBHelper = new DatabaseHelper(context);
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			System.out.println("Creating database...");
 			db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int currentVersion,
 				int newestVersion) {
 			System.out.println("Upgrading " + DATABASE_TABLE
 					+ " table from version " + currentVersion + " to "
 					+ newestVersion + "...");
 
 			if (currentVersion < 2) {
 				System.out.println("Adding column: " + KEY_STATS + "...");
 				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
 						+ KEY_STATS + " int not null default '1' " + ";");
 			}
 
 			System.out.println(DATABASE_TABLE + " is now at version "
 					+ newestVersion + "(schema version: " + DATABASE_VERSION
 					+ ")");
 		}
 	}
 
 	public Journey open() throws SQLException {
 		System.out.println("Opening database...");
 		db = DBHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		System.out.println("Closing database...");
 		DBHelper.close();
 	}
 
 	public long insertJourney(String from_station, int from_year,
 			int from_month, int from_day, int from_hour, int from_minute,
 			String to_station, int to_year, int to_month, int to_day,
 			int to_hour, int to_minute, String classNo, String headcode,
 			boolean useForStats) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_FROM_STATION, from_station);
 		initialValues.put(KEY_FROM_YEAR, from_year);
 		initialValues.put(KEY_FROM_MONTH, from_month);
 		initialValues.put(KEY_FROM_DAY, from_day);
 		initialValues.put(KEY_FROM_HOUR, from_hour);
 		initialValues.put(KEY_FROM_MINUTE, from_minute);
 
 		initialValues.put(KEY_TO_STATION, to_station);
 		initialValues.put(KEY_TO_YEAR, to_year);
 		initialValues.put(KEY_TO_MONTH, to_month);
 		initialValues.put(KEY_TO_DAY, to_day);
 		initialValues.put(KEY_TO_HOUR, to_hour);
 		initialValues.put(KEY_TO_MINUTE, to_minute);
 
 		initialValues.put(KEY_CLASS, classNo);
 		initialValues.put(KEY_HEADCODE, headcode);
 
 		initialValues.put(KEY_STATS, useForStats);
 
 		System.out.println("Writing entry...");
 		return db.insert(DATABASE_TABLE, null, initialValues);
 	}
 
 	public boolean deleteJourney(long rowId) {
 		System.out.println("Deleting entry " + rowId + "...");
 		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public Cursor getAllJourneys() {
 		System.out.println("Fetching all entries...");
 		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID,
 				KEY_FROM_STATION, KEY_FROM_DAY, KEY_FROM_MONTH, KEY_FROM_YEAR,
 				KEY_FROM_HOUR, KEY_FROM_MINUTE,
 
 				KEY_TO_STATION, KEY_TO_DAY, KEY_TO_MONTH, KEY_TO_YEAR,
 				KEY_TO_HOUR, KEY_TO_MINUTE,
 
 				KEY_CLASS, KEY_HEADCODE, KEY_STATS }, null, null, null, null,
 				"" + KEY_FROM_YEAR + "," + KEY_FROM_MONTH + "," + KEY_FROM_DAY
 						+ "," + KEY_FROM_HOUR + "," + KEY_FROM_MINUTE + "");
 	}
 
 	public Cursor getAllJourneysReverse() {
 		System.out.println("Fetching all entries, in reverse order...");
 		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID,
 				KEY_FROM_STATION, KEY_FROM_DAY, KEY_FROM_MONTH, KEY_FROM_YEAR,
 				KEY_FROM_HOUR, KEY_FROM_MINUTE,
 
 				KEY_TO_STATION, KEY_TO_DAY, KEY_TO_MONTH, KEY_TO_YEAR,
 				KEY_TO_HOUR, KEY_TO_MINUTE,
 
 				KEY_CLASS, KEY_HEADCODE, KEY_STATS }, null, null, null, null,
 				"" + KEY_FROM_YEAR + " DESC," + KEY_FROM_MONTH + " DESC,"
 						+ KEY_FROM_DAY + " DESC," + KEY_FROM_HOUR + " DESC,"
 						+ KEY_FROM_MINUTE + " DESC");
 	}
 
 	public Cursor getAllStatsJourneys() {
 		System.out.println("Fetching all entries enabled for stats...");
 		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID,
 				KEY_FROM_STATION, KEY_FROM_DAY, KEY_FROM_MONTH, KEY_FROM_YEAR,
 				KEY_FROM_HOUR, KEY_FROM_MINUTE,
 
 				KEY_TO_STATION, KEY_TO_DAY, KEY_TO_MONTH, KEY_TO_YEAR,
 				KEY_TO_HOUR, KEY_TO_MINUTE,
 
 				KEY_CLASS, KEY_HEADCODE, KEY_STATS }, KEY_STATS + " = 1", null,
 				null, null, "" + KEY_FROM_YEAR + "," + KEY_FROM_MONTH + ","
 						+ KEY_FROM_DAY + "," + KEY_FROM_HOUR + ","
 						+ KEY_FROM_MINUTE + "");
 	}
 
 	public Cursor getJourney(long rowId) throws SQLException {
 		System.out.println("Fetching entry " + rowId + "...");
 		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
 				KEY_ROWID, KEY_FROM_STATION, KEY_FROM_DAY, KEY_FROM_MONTH,
 				KEY_FROM_YEAR, KEY_FROM_HOUR, KEY_FROM_MINUTE,
 
 				KEY_TO_STATION, KEY_TO_DAY, KEY_TO_MONTH, KEY_TO_YEAR,
 				KEY_TO_HOUR, KEY_TO_MINUTE,
 
 				KEY_CLASS, KEY_HEADCODE, KEY_STATS }, KEY_ROWID + "=" + rowId,
 				null, null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 
 	public boolean updateJourney(long rowId, String from_station,
 			int from_year, int from_month, int from_day, int from_hour,
 			int from_minute, String to_station, int to_year, int to_month,
 			int to_day, int to_hour, int to_minute, String classNo,
 			String headcode, boolean useForStats) {
 		System.out.println("Updating entry " + rowId + "...");
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(KEY_FROM_STATION, from_station);
 		updatedValues.put(KEY_FROM_YEAR, from_year);
 		updatedValues.put(KEY_FROM_MONTH, from_month);
 		updatedValues.put(KEY_FROM_DAY, from_day);
 		updatedValues.put(KEY_FROM_HOUR, from_hour);
 		updatedValues.put(KEY_FROM_MINUTE, from_minute);
 
 		updatedValues.put(KEY_TO_STATION, to_station);
 		updatedValues.put(KEY_TO_YEAR, to_year);
 		updatedValues.put(KEY_TO_MONTH, to_month);
 		updatedValues.put(KEY_TO_DAY, to_day);
 		updatedValues.put(KEY_TO_HOUR, to_hour);
 		updatedValues.put(KEY_TO_MINUTE, to_minute);
 
 		updatedValues.put(KEY_CLASS, classNo);
 		updatedValues.put(KEY_HEADCODE, headcode);
 		updatedValues.put(KEY_STATS, useForStats);
 
 		return db.update(DATABASE_TABLE, updatedValues,
 				KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	public ArrayList<Boolean> importFromCSV(String dataFile) {
 		ArrayList<Boolean> statuses = new ArrayList<Boolean>();
 		ArrayList<String> saved_entries = loadSavedEntries(dataFile);
 		ArrayList<String[]> parsed_entries = parseEntries(saved_entries);
 
 		Journey db_journeys = new Journey(this.context);
 		db_journeys.open();
 
 		for (String[] entry : parsed_entries) {
 			try {
 				System.out.println("Importing...");
 
 				boolean useForStats = false;
				if (entry[14] == "1") {
 					useForStats = true;
 				}
 
 				db_journeys.insertJourney(entry[0], Integer.parseInt(entry[1]),
 						Integer.parseInt(entry[2]), Integer.parseInt(entry[3]),
 						Integer.parseInt(entry[4]), Integer.parseInt(entry[5]),
 						entry[6], Integer.parseInt(entry[7]),
 						Integer.parseInt(entry[8]), Integer.parseInt(entry[9]),
 						Integer.parseInt(entry[10]),
 						Integer.parseInt(entry[11]), entry[12], entry[13],
 						useForStats);
 				System.out.println("Success!");
 				statuses.add(true);
 			} catch (Exception e) {
 				System.out.println("Error!");
 				statuses.add(false);
 			}
 		}
 
 		db_journeys.close();
 		File f = new File(dataFile);
 		f.renameTo(new File(dataFile + ".imported"));
 
 		return statuses;
 	}
 
 	public ArrayList<String> loadSavedEntries(String dataFile) {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)
 				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 
 			try {
 				String line = null;
 				ArrayList<String> array = new ArrayList<String>();
 
 				File f = new File(dataFile);
 				BufferedReader reader = new BufferedReader(new FileReader(f));
 
 				while ((line = reader.readLine()) != null) {
 					String str = new String(line);
 					array.add(str);
 				}
 				reader.close();
 				return array;
 
 			} catch (Exception e) {
 				System.out.println("Error reading old routes: "
 						+ e.getMessage());
 				return new ArrayList<String>();
 			}
 
 		} else {
 			return new ArrayList<String>();
 		}
 	}
 
 	private ArrayList<String[]> parseEntries(ArrayList<String> entries) {
 		ArrayList<String[]> data = new ArrayList<String[]>();
 
 		try {
 			for (Iterator<String> i = entries.iterator(); i.hasNext();) {
 				String str = (String) i.next();
 				String[] elements = str.split(",");
 				String[] entry = new String[elements.length];
 
 				for (int j = 0; j < entry.length; j++) {
 					entry[j] = Helpers.trimCSVSpeech(elements[j]);
 				}
 
 				data.add(entry);
 			}
 		} catch (Exception e) {
 			Toast.makeText(this.context,
 					"Error parsing old routes: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return data;
 	}
 
 	public Boolean exportToCSV() {
 		boolean mExternalStorageWritable = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageWritable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageWritable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need to know is we can neither read nor write
 			mExternalStorageWritable = false;
 		}
 
 		if (mExternalStorageWritable) {
 			try {
 
 				File f = new File(Helpers.exportDirectoryPath + "/routes.csv");
 				if (f.exists()) {
 					f.delete();
 				}
 				f.createNewFile();
 				FileWriter writer = new FileWriter(f, true);
 				String msep = "\",\"";
 
 				ArrayList<String[]> allJourneys = new ArrayList<String[]>();
 				Journey db_journeys = new Journey(context);
 				db_journeys.open();
 				Cursor c = db_journeys.getAllJourneys();
 				if (c.moveToFirst()) {
 					do {
 						String useForStats = "0";
 						if (c.getInt(15) == 1) {
 							useForStats = "1";
 						}
 
 						String line = "";
 						line = "\"" + c.getString(1) + msep + c.getInt(4)
 								+ msep + c.getInt(3) + msep + c.getInt(2)
 								+ msep + c.getInt(5) + msep + c.getInt(6)
 								+ msep + c.getString(7) + msep + c.getInt(10)
 								+ msep + c.getInt(9) + msep + c.getInt(8)
 								+ msep + c.getInt(11) + msep + c.getInt(12)
 								+ msep + c.getString(13) + msep
 								+ c.getString(14) + msep + useForStats + "\"";
 
 						writer.write(line);
 						writer.write(System.getProperty("line.separator"));
 					} while (c.moveToNext());
 				}
 				writer.close();
 				db_journeys.close();
 
 				return true;
 
 			} catch (Exception e) {
 				return false;
 			}
 
 		} else {
 			return false;
 		}
 	}
 
 	private static String classFromLocoNo(String locoNumber) {
 		String result = locoNumber.trim();
 		if (result.length() > 3 || result.contains("/") || result.contains("-")) {
 			// we need to pick out 2- or 3-char class number
 			System.out.println("Found loco number: \"" + result + "\"");
 
 			String validResult = result.split("\\D")[0];
 			System.out.println("First valid group: \"" + validResult + "\"");
 			result = validResult;
 			System.out.println("Class/Loco No: \"" + result + "\"");
 
 			if (result.length() == 2) {
 				System.out.println("2-digit class found from loco: \"" + result
 						+ "\"");
 			} else if (result.length() == 3) {
 				System.out.println("3-digit class found from loco: \"" + result
 						+ "\"");
 			} else if (result.length() == 5) {
 				System.out.println("Taking 2-digit class from loco: \""
 						+ result + "\"");
 				result = result.substring(0, 2);
 			} else if (result.length() == 6) {
 				System.out.println("Taking 3-digit class from loco: \""
 						+ result + "\"");
 				result = result.substring(0, 3);
 			} else {
 				System.out.println("Taking plain result: \"" + result + "\"");
 			}
 
 			System.out.println("Result: \"" + result + "\"");
 		} else {
 			System.out.println("Class number only: \"" + result + "\"");
 		}
 		return result;
 	}
 
 	public static ArrayList<String> classesStringToArrayList(String rawClasses) {
 		ArrayList<String> classes = new ArrayList<String>();
 		String[] splitClasses = rawClasses.split("([|+&,])|( / )|( - )");
 		for (String c : splitClasses) {
 			c = c.trim();
 			if (c.length() > 0) {
 				System.out.println("Processing class: \"" + c + "\"");
 				c = classFromLocoNo(c);
 				classes.add(c);
 			} else {
 				System.out.println("Ignoring: \"" + c + "\"");
 			}
 		}
 		return classes;
 	}
 }
