 package net.analogyc.wordiary.models;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * Aggregator for all the database queries
  */
 public class DBAdapter {
 
 	private DataBaseHelper dbHelper;
 	private SQLiteDatabase database;
 	private SharedPreferences preferences;
 
 	/**
 	 * You must call open() on this object to use other methods
 	 */
 	public DBAdapter(Context context) {
 		dbHelper = new DataBaseHelper(context);
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 
 	/**
 	 * Returns an open, writable database, or creates a new instance
 	 */
 	private SQLiteDatabase getConnection() {
 		if (database == null) {
 			database = dbHelper.getWritableDatabase();
 		}
 
 		return database;
 	}
 
 	/**
 	 * Close databaseHelper
 	 */
 	public void close() {
 		if (database != null) {
 			database.close();
 			database = null;
 		}
 	}
 	
 
 	/**
 	 * Get all the entries in the db
 	 *
 	 * @return Cursor that contains all entries ordered by date
 	 */
 	public Cursor getAllEntries() {
 		String query = "SELECT * FROM " + Entry.TABLE_NAME + " ORDER BY " + Entry.COLUMN_NAME_CREATED + " DESC";
 		return getConnection().rawQuery(query, null);
 	}
 
 	/**
 	 * Get all the entries in the db
 	 *
 	 * @return Cursor that contains all entries ordered by date
 	 */
 	public Cursor getAllEntriesWithImage() {
 		String query = "SELECT " + Day.TABLE_NAME + "." + Day.COLUMN_NAME_FILENAME + ", " +
 				Entry.TABLE_NAME + "." + Entry.COLUMN_NAME_MESSAGE + ", " +
 				Entry.TABLE_NAME + "." + Entry._ID + ", " +
 				Entry.TABLE_NAME + "." + Entry.COLUMN_NAME_CREATED +
 				" FROM " + Entry.TABLE_NAME + " LEFT OUTER JOIN " + Day.TABLE_NAME +
 				" ON " + Entry.TABLE_NAME + "." + Entry.COLUMN_NAME_DAY_ID +
 				" = " +
 				Day.TABLE_NAME + "." + Day._ID +
 				" ORDER BY " + Entry.TABLE_NAME + "." + Entry.COLUMN_NAME_CREATED + " DESC";
 		return getConnection().rawQuery(query, null);
 	}
 
 	/**
 	 * Get the selected entry
 	 *
 	 * @param id entry's id
 	 * @return a Cursor that contains the selected entry, or null
 	 */
 	public Cursor getEntryById(int id) {
 		String query = "SELECT * FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + id + " LIMIT 1";
 		return getConnection().rawQuery(query, null);
 	}
 	
 	/**
 	 * Get the  entries of the selected day
 	 *
 	 * @param id entry's id
 	 * @return a Cursor that contains the selected entry, or null
 	 */
 	public Cursor getEntryByDay(int id) {
 		String query = 	"SELECT * FROM " + Entry.TABLE_NAME +
 						" WHERE " + Entry.COLUMN_NAME_DAY_ID + " = " + id +
 						" ORDER BY "+ Entry._ID+" DESC";
 		return getConnection().rawQuery(query, null);
 	}
 
 
 	/**
 	 * Add a new entry
 	 *
 	 * @param text the message of the entry
 	 * @param mood the correspondent mood
 	 */
 	public void addEntry(String text, int mood) {
 		//create the current timestamp
 		Date now = new Date(System.currentTimeMillis());
 		String DATE_FORMAT = "yyyyMMddHHmmss";
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
 		String query =	"SELECT * " +
 						"FROM " + Day.TABLE_NAME +
 						" WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + sdf.format(now).substring(0, 8) + "%'" +
 						" LIMIT 1";
 
 		Cursor c = getConnection().rawQuery(query, null);
 		int photo;
 		if (c.moveToFirst()) {
 			photo = c.getInt(0);
 		} else {
 			addPhoto("");
 			query =	"SELECT * " +
 					"FROM " + Day.TABLE_NAME +
 					" WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + sdf.format(now).substring(0, 8) + "%'" +
 					" LIMIT 1";
 
 			c = getConnection().rawQuery(query, null);
 			c.moveToFirst();
 			photo = c.getInt(0);
 
 		}
 		c.close();
 		//insert the entry
 		query = "INSERT INTO " + Entry.TABLE_NAME + " ( " +
 				Entry.COLUMN_NAME_MESSAGE + " , " +
 				Entry.COLUMN_NAME_MOOD + " , " +
 				Entry.COLUMN_NAME_DAY_ID + " , " +
 				Entry.COLUMN_NAME_CREATED +
 				") VALUES ( ?,?,?,? )";
 		getConnection().execSQL(query, new Object[]{text, mood, photo, sdf.format(now)});
 	}
 
 	/**
 	 * Delete an entry
 	 *
 	 * @param id the message id
 	 */
 	public void deleteEntryById(int id) {
 		//delete the entry
 		Cursor entry = getEntryById(id);
 		entry.moveToFirst();
 		int day_id = entry.getInt(1);
 		entry.close();
 		Cursor day = getDayById(day_id);
 		Cursor day_entries = getEntryByDay(day_id);
 		day.moveToFirst();
 		String filename = day.getString(1);
 		if(filename.equals("") && day_entries.getCount() <= 1){
 			deleteDay(day_id, false);
 		}
 		day.close();
 		day_entries.close();
 		String query = "DELETE FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + id;
 		getConnection().execSQL(query);
 	}
 	
 	/**
 	 * Delete a day, this method maintains the consistency of the data stored, so a day can be deleted only if
 	 *  it has no entry
 	 *
 	 * @param id the day id
 	 * @param consistency true if method has to make sure about data consistency
 	 * @return 0 if the selected day is correctly deleted from db, the number of relative entries
 	 *  otherwise (in this case db isn't modified)
 	 */
 	public int deleteDay(int id, boolean consistency) {
 		Cursor c = getEntryByDay(id);
 		int count = c.getCount();
 		if(count <= 0 || !consistency){
 			//delete the entry
 			String query = "DELETE FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + id;
 			getConnection().execSQL(query);
 		}
 		c.close();
 		return count;
 	}
 	
 	/**
 	 * Get the  entries of the selected day
 	 *
 	 * @param id entry's id
 	 * @return a Cursor that contains the selected day, or null
 	 */
 	public Cursor getDayByEntry(int id) {
 		Cursor c = this.getEntryById(id);
 		c.moveToFirst();
 		int day = c.getInt(1);
 		c.close();
 		return getDayById(day);
 	}
 	
 	/**
 	 * Delete the selected photo
 	 *
 	 * @param int the day id
 	 * @return the filename deleted
 	 * 
 	 */
 	public void deletePhoto(int id) {
		int entries = this.deleteDay(id, true);
		
		if(entries > 0){
			//delete the filename
			String query =	"UPDATE " + Day.TABLE_NAME + " " +
							"SET " + Day.COLUMN_NAME_FILENAME + " = ''" +
							"WHERE " + Day._ID + " = "+ id;
			getConnection().execSQL(query);
		}
 	}
 	
 
 
 	/**
 	 * Add a new photo
 	 *
 	 * @param filename the path of the photo
 	 */
 	public void addPhoto(String filename) {
 		//create the current timestamp
 		Date now = new Date(System.currentTimeMillis());
 		String DATE_FORMAT = "yyyyMMddHHmmss";
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
 		String date = sdf.format(now);
 
 		//get the id of the day
 		String query =	"SELECT * " +
 						" FROM " + Day.TABLE_NAME +
 						" WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + date.substring(0, 8) + "%'"+
 						" LIMIT 1";
 
 		Cursor c = getConnection().rawQuery(query, null);
 
 		if(c.getCount() > 0) {
 			c.moveToFirst();
 			query = "UPDATE " + Day.TABLE_NAME + " " +
 					"SET " + Day.COLUMN_NAME_FILENAME + " = ?" +
 					"WHERE " + Day._ID + " = ?";
 			getConnection().execSQL(query, new Object[] {filename, c.getInt(0)});
 		} else {
 			//insert the entry
 			query = "INSERT INTO " + Day.TABLE_NAME + " ( " +
 					Day.COLUMN_NAME_FILENAME + " , " +
 					Day.COLUMN_NAME_CREATED +
 					") VALUES (?, ?)";
 			getConnection().execSQL(query, new Object[] {filename, date});
 		}
 	}
 
 	/**
 	 * Get a photo by inserting the
 	 *
 	 * @param day Day in format yyyyMMdd
 	 * @return The database row, one or none
 	 */
 	public Cursor getPhotoByDay(String day) {
 		String query =	"SELECT * " +
 						"FROM " + Day.TABLE_NAME + " " +
 						"WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + day + "%' "+
 						"LIMIT 1";
 
 		return getConnection().rawQuery(query, null);
 	}
 
 
 	/**
 	 * Get all the days ordered by date (DESC)
 	 *
 	 * @return Cursor containing the days
 	 */
 	public Cursor getAllDays() {
 		String query = "SELECT * FROM " + Day.TABLE_NAME + " ORDER BY " + Day._ID + " DESC";
 		return getConnection().rawQuery(query, null);
 	}
 	
 	/**
 	 * Get all the days ordered by date (DESC)
 	 *
 	 * @return Cursor containing the days
 	 */
 	public Cursor getAllPhotos() {
 		String query = 	"SELECT * FROM " + Day.TABLE_NAME + 
 						" WHERE " + Day.COLUMN_NAME_FILENAME + "<> ''" +
 						" ORDER BY " + Day._ID + " DESC";
 		return getConnection().rawQuery(query, null);
 	}
 
 	/**
 	 * Get the selected entry
 	 *
 	 * @param id entry's id
 	 * @return a Cursor that contains the selected entry, or null
 	 */
 	public Cursor getDayById(int id) {
 		String query = "SELECT * FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + id + " LIMIT 1";
 		return getConnection().rawQuery(query, null);
 	}
 
 	/**
 	 * Modify the mood of the selected entry
 	 *
 	 * @param entryId entry id
 	 * @param moodId filename of the mood
 	 */
 	public void updateMood(int entryId, String moodId) {
 		String query = 	"UPDATE " + Entry.TABLE_NAME +
 						" SET "+ Entry.COLUMN_NAME_MOOD +" =  ?" + 
 						" WHERE " + Entry._ID + " = ?";
 		getConnection().execSQL(query, new Object[]{moodId,entryId});
 	}
 	
 	
 	/**
 	 * Modify the message of the selected entry
 	 *
 	 * @param entryId entry id
 	 * @param message The message to insert
 	 */
 	public void updateMessage(int entryId, String message) {
 		String query = 	"UPDATE " + Entry.TABLE_NAME +
 						" SET "+ Entry.COLUMN_NAME_MESSAGE +" =  ?" + 
 						" WHERE " + Entry._ID + " = ?";
 		getConnection().execSQL(query, new Object[]{message,entryId});
 	}
 	
 	/**
 	 * Verify if the selected entry can be modified
 	 *
 	 * @param entryId entry id
 	 * @return boolean true if is editable, false otherwise
 	 */
 	public boolean isEditableEntry(int entryId){
 		 int grace_period = Integer.parseInt(preferences.getString("grace_period", "1"));
 		//create the current timestamp
 		Date now = new Date(System.currentTimeMillis());
 		String DATE_FORMAT = "yyyyMMddHHmmss";
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
 
 		String query = "SELECT * FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + entryId +" LIMIT 1";
 		Cursor c =getConnection().rawQuery(query, null);
 		c.moveToFirst();
 		Date created;
 		try {
 			created = sdf.parse(c.getString(4));
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return false;
 		}
 		c.close();
 
 		long now_mil = now.getTime();
 		long created_mil = created.getTime();
 		
 		long diff = now_mil - created_mil;
 		return diff < grace_period * 60 * 60 * 1000;
 
 	}
 	
 	/**
 	 * Verify if the selected entry can be modified
 	 *
 	 * @param entryId entry id
 	 * @return boolean true if is editable, false otherwise
 	 */
 	public boolean isEditableDay(int dayId){
 		//create the current timestamp
 		Date now = new Date(System.currentTimeMillis());
 		String DATE_FORMAT = "yyyyMMddHHmmss";
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
 
 		String query = "SELECT * FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + dayId +" LIMIT 1";
 		Cursor c =getConnection().rawQuery(query, null);
 		c.moveToFirst();
 		String created_date = c.getString(2).substring(0, 8);
 		c.close();
 		String current_date = sdf.format(now).substring(0, 8);
 		return created_date.equals(current_date);
 	}
 
 	/**
 	 * Gets the next or the previous image
 	 *
 	 * @param currentDay The id of the current day opened
 	 * @param backwards If it should look for the previous image
 	 * @return The cursor containing the single row or zero rows, with as only column the ID
 	 */
 	public Cursor getNextDay(int currentDay, boolean backwards) {
 		String query = "SELECT " + Day._ID + " FROM " + Day.TABLE_NAME + " " +
 				"WHERE " + Day._ID + " " + (backwards ? "<" : ">") + " ? " +
 				"AND " + Day.COLUMN_NAME_FILENAME + " <> ? " +
 				"ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
 				"LIMIT 1";
 		return getConnection().rawQuery(query, new String[] {Integer.toString(currentDay), "" });
 	}
 	
 	/**
 	 * Gets the next or the previous entry id
 	 *
 	 * @param currentDay The id of the current day opened
 	 * @param backwards If it should look for the previous image
 	 * @return The cursor containing the single row or zero rows, with as only column the ID
 	 */
 	public Cursor getNextEntry(int currentEntry, boolean backwards) {
 		String query = 	"SELECT " + Entry._ID + " FROM " + Entry.TABLE_NAME + " " +
 						"WHERE " + Entry._ID + " " + (backwards ? "<" : ">") + currentEntry +
 						"ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
 						"LIMIT 1";
 		Cursor result = getConnection().rawQuery(query, null);
 		if(result.getCount() <=0){
 			result.close();
 			return getEntryById(currentEntry);
 		}
 		else{
 			return result;
 		}
 	}
 	
 	/**
 	 * Determine if entry has a next ( or the previous) entry
 	 *
 	 * @param currentDay The id of the current day opened
 	 * @param backwards If it should look for the previous image
 	 * @return true is it has a next or previous, false otherwise
 	 */
 	public boolean hasNextEntry(int currentEntry, boolean backwards) {
 		String query = 	"SELECT " + Entry._ID + " FROM " + Entry.TABLE_NAME + " " +
 						"WHERE " + Entry._ID + " " + (backwards ? "<" : ">") + currentEntry +
 						"ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
 						"LIMIT 1";
 		Cursor result = getConnection().rawQuery(query, null);
 		boolean hasNext = result.getCount() > 0;
 		result.close();
 		return hasNext;
 	}
 	
 	
 }
