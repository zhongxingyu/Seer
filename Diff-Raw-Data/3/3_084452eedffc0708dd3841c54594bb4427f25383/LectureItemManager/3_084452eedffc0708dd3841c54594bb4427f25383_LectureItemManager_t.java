 package de.tum.in.tumcampus.models;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import de.tum.in.tumcampus.Const;
 
 /**
  * Lecture item Manager, handles database stuff, internal imports
  */
 public class LectureItemManager extends SQLiteOpenHelper {
 
 	/**
 	 * Database connection
 	 */
 	private SQLiteDatabase db;
 
 	/**
 	 * Last insert counter
 	 */
 	public static int lastInserted = 0;
 
 	/**
 	 * Additional information for exception messages
 	 */
 	public String lastInfo = "";
 
 	/**
 	 * Constructor, open/create database, create table if necessary
 	 * 
 	 * <pre>
 	 * @param context Context
 	 * @param database Filename, e.g. database.db
 	 * </pre>
 	 */
 	public LectureItemManager(Context context, String database) {
 		super(context, database, null, Const.dbVersion);
 
 		db = getWritableDatabase();
 		onCreate(db);
 	}
 
 	/**
 	 * Import lecture items from sd-card directory
 	 * 
 	 * @throws Exception
 	 */
 	public void importFromInternal() throws Exception {
 		File[] files = new File(Utils.getCacheDir("lectures")).listFiles();
 
 		int count = Utils.dbGetTableCount(db, "lectures_items");
 
 		db.beginTransaction();
 		try {
 			for (File file : files) {
 				String filename = file.getName();
 				if (filename.toLowerCase().endsWith(".csv")) {
 					lastInfo = filename;
 					importCsv(file, "ISO-8859-1");
 				}
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		// update last insert counter
 		lastInserted += Utils.dbGetTableCount(db, "lectures_items") - count;
 	}
 
 	/**
 	 * Import lecture items from a CSV file
 	 * 
 	 * Header format: TERMIN_TYP, TITEL, ORT, LV_NUMMER, DATUM, VON, BIS,
 	 * WOCHENTAG, ANMERKUNG, URL
 	 * 
 	 * <pre>
 	 * @param file CSV File
 	 * @param encoding Charset, e.g. ISO-8859-1
 	 * @throws Exception
 	 * </pre>
 	 */
 	public void importCsv(File file, String encoding) throws Exception {
 		List<String[]> list = Utils
 				.readCsv(new FileInputStream(file), encoding);
 
 		if (list.size() == 0) {
 			return;
 		}
 		Vector<String> headers = new Vector<String>(Arrays.asList(list.get(0)));
 
 		for (int i = 1; i < list.size(); i++) {
 			String[] row = list.get(i);
 
 			// skip canceled events on import
 			int terminTypId = headers.indexOf("TERMIN_TYP");
 			if (row.length > terminTypId
 					&& row[terminTypId].contains("abgesagt")) {
 				continue;
 			}
 
 			String name = row[headers.indexOf("TITEL")];
 			String location = row[headers.indexOf("ORT")];
 			String lectureId = row[headers.indexOf("LV_NUMMER")];
 
 			String module = "";
 			if (name.contains("(") && name.contains(")")) {
 				module = name.substring(name.indexOf("(") + 1,
 						name.indexOf(")"));
 				name = name.substring(0, name.indexOf("(")).trim();
 			}
 
 			String datum = row[headers.indexOf("DATUM")];
 			String von = row[headers.indexOf("VON")];
 			String bis = row[headers.indexOf("BIS")];
 
 			Date start = Utils.getDateTimeDe(datum + " " + von);
 			Date end = Utils.getDateTimeDe(datum + " " + bis);
 
 			String id = row[headers.indexOf("LV_NUMMER")] + "_"
 					+ String.valueOf(start.getTime());
 
 			String seriesId = row[headers.indexOf("LV_NUMMER")] + "_"
 					+ row[headers.indexOf("WOCHENTAG")] + "_"
 					+ row[headers.indexOf("VON")];
 
 			String note = "";
 			int noteId = headers.indexOf("ANMERKUNG");
 			if (row.length > noteId) {
 				note = row[noteId];
 			}
 			String url = "";
 			int urlId = headers.indexOf("URL");
 			if (urlId != -1 && row.length > urlId) {
 				url = row[headers.indexOf("URL")];
 			}
 			replaceIntoDb(new LectureItem(id, lectureId, start, end, name,
 					module, location, note, url, seriesId));
 		}
 	}
 
 	/**
 	 * Get all lecture items from the database
 	 * 
 	 * @return Database cursor (name, location, _id)
 	 */
 	public Cursor getCurrentFromDb() {
 		return db.rawQuery("SELECT name, location, id as _id "
 				+ "FROM lectures_items WHERE datetime('now', 'localtime') "
				+ "BETWEEN start AND end AND "
				+ "lectureId NOT IN ('holiday', 'vacation') LIMIT 1", null);
 	}
 
 	/**
 	 * Get all upcoming and unfinished lecture items from the database
 	 * 
 	 * @return Database cursor (name, note, location, weekday, start_de, end_de,
 	 *         start_dt, end_dt, url, lectureId, _id)
 	 */
 	public Cursor getRecentFromDb() {
 		return db
 				.rawQuery(
 						"SELECT name, note, location, "
 								+ "strftime('%w', start) as weekday, "
 								+ "strftime('%H:%M', start) as start_de, "
 								+ "strftime('%H:%M', end) as end_de, "
 								+ "strftime('%d.%m.%Y', start) as start_dt, "
 								+ "strftime('%d.%m.%Y', end) as end_dt, "
 								+ "url, lectureId, id as _id "
 								+ "FROM lectures_items WHERE end > datetime('now', 'localtime') AND "
 								+ "start < date('now', '+7 day') ORDER BY start",
 						null);
 	}
 
 	/**
 	 * Get all lecture items for a special lecture from the database
 	 * 
 	 * <pre>
 	 * @param lectureId Lecture ID
 	 * @return Database cursor (name, note, location, weekday, start_de,
 	 * 		   end_de, start_dt, end_dt, url, location, _id)
 	 * </pre>
 	 */
 	public Cursor getAllFromDb(String lectureId) {
 		return db.rawQuery("SELECT name, note, location, "
 				+ "strftime('%w', start) as weekday, "
 				+ "strftime('%d.%m.%Y %H:%M', start) as start_de, "
 				+ "strftime('%H:%M', end) as end_de, "
 				+ "strftime('%d.%m.%Y', start) as start_dt, "
 				+ "strftime('%d.%m.%Y', end) as end_dt, "
 				+ "url, lectureId, id as _id "
 				+ "FROM lectures_items WHERE lectureId = ? ORDER BY start",
 				new String[] { lectureId });
 	}
 
 	/**
 	 * Checks if the lectures_items table is empty
 	 * 
 	 * @return true if no lecture items are available, else false
 	 */
 	public boolean empty() {
 		boolean result = true;
 		Cursor c = db.rawQuery("SELECT id FROM lectures_items LIMIT 1", null);
 		if (c.moveToNext()) {
 			result = false;
 		}
 		c.close();
 		return result;
 	}
 
 	/**
 	 * Replace or Insert a lecture item in the database
 	 * 
 	 * <pre>
 	 * @param l LectureItem object
 	 * @throws Exception
 	 * </pre>
 	 */
 	public void replaceIntoDb(LectureItem l) throws Exception {
 		Utils.log(l.toString());
 
 		if (l.id.length() == 0) {
 			throw new Exception("Invalid id.");
 		}
 		if (l.lectureId.length() == 0) {
 			throw new Exception("Invalid lectureId.");
 		}
 		if (l.name.length() == 0) {
 			throw new Exception("Invalid name.");
 		}
 		if (l.seriesId.length() == 0) {
 			throw new Exception("Invalid id.");
 		}
 
 		db.execSQL(
 				"REPLACE INTO lectures_items (id, lectureId, start, end, "
 						+ "name, module, location, note, url, seriesId) VALUES "
 						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
 				new String[] { l.id, l.lectureId,
 						Utils.getDateTimeString(l.start),
 						Utils.getDateTimeString(l.end), l.name, l.module,
 						l.location, l.note, l.url, l.seriesId });
 	}
 
 	/**
 	 * Delete lecture item from database
 	 * 
 	 * <pre>
 	 * @param id Lecture item ID
 	 * </pre>
 	 */
 	public void deleteItemFromDb(String id) {
 		db.execSQL("DELETE FROM lectures_items WHERE id = ?",
 				new String[] { id });
 	}
 
 	/**
 	 * Delete lecture items from database
 	 * 
 	 * <pre>
 	 * @param id Lecture ID
 	 * </pre>
 	 */
 	public void deleteLectureFromDb(String id) {
 		db.execSQL("DELETE FROM lectures_items WHERE lectureId = ?",
 				new String[] { id });
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// create table if needed
 		db.execSQL("CREATE TABLE IF NOT EXISTS lectures_items ("
 				+ "id VARCHAR PRIMARY KEY, lectureId VARCHAR, start VARCHAR, "
 				+ "end VARCHAR, name VARCHAR, module VARCHAR, location VARCHAR, "
 				+ "note VARCHAR, url VARCHAR, seriesId VARCHAR)");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		onCreate(db);
 	}
 }
