 package com.inc.im.serptracker.adapters;
 
 import java.util.ArrayList;
 
 import com.inc.im.serptracker.data.Keyword;
 import com.inc.im.serptracker.data.UserProfile;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * All interaction read/write/update/create logic to SQLite is done in this
  * class
  * 
  * @see documentation file for database schema
  * 
  */
 
 public class DbAdapter {
 
 	private SQLiteDatabase mDb;
 	private static Context mCtx;
 	private DatabaseHelper mDbHelper;
 
 	private static final String DATABASE_NAME = "appdata";
 	// private static final int DATABASE_VERSION = 2;
 	private static final String TABLE_PROFILE = "profile";
 	private static final String KEY_PROFILE_TABLE_ID = "_id";
 	private static final String KEY_PROFILE_TABLE_URL = "url";
 
 	private static final String TABLE_KEYWORDS = "profile_keywords";
 	private static final String KEY_KEYWORDS_TABLE_ID = "_id";
 	private static final String KEY_KEYWORDS_TABLE_KEYWORD = "keyword";
 	private static final String KEY_KEYWORDS_TABLE_POSTION = "position";
 	private static final String KEY_KEYWORDS_TABLE_PARENTID = "parentid";
 
 	private static final String TABLE_EXTRA = "profile_keywords_extra";
 	private static final String TABLE_ID = "_id";
 	private static final String KEY_EXTRA_PARENTID = "parentid";
 	private static final String KEY_EXTRA_ANCHOR = "anchor";
 	private static final String KEY_EXTRA_URL = "url";
 
 	private static final String PROFILE_TABLE_CREATE = "CREATE TABLE "
 			+ TABLE_PROFILE + " (" + KEY_PROFILE_TABLE_ID
 			+ " INTEGER PRIMARY KEY, " + KEY_PROFILE_TABLE_URL
 			+ " TEXT NOT NULL);";
 
 	private static final String KEYWORDS_TABLE_CREATE = "CREATE TABLE "
 			+ TABLE_KEYWORDS + " (" + KEY_KEYWORDS_TABLE_ID
 			+ " INTEGER PRIMARY KEY, " + KEY_KEYWORDS_TABLE_KEYWORD
 			+ " TEXT NOT NULL, " + KEY_KEYWORDS_TABLE_POSTION + " INTEGER, "
 			+ KEY_KEYWORDS_TABLE_PARENTID + " INTEGER NOT NULL);";
 
 	private static final String EXTRA_TABLE_CREATE = String
 			.format("CREATE TABLE %s ( %s INTEGER PRIMARY KEY, %s INTEGER NOT NULL,  %s TEXT NOT NULL, %s TEXT NOT NULL);",
 					TABLE_EXTRA, TABLE_ID, KEY_EXTRA_PARENTID,
 					KEY_EXTRA_ANCHOR, KEY_EXTRA_URL);
 
 	public DbAdapter(Context ctx) {
 		DbAdapter.mCtx = ctx;
 	}
 
 	public DbAdapter open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 
 		if (mDbHelper != null)
 			mDbHelper.close();
 	}
 
 	public void addExtraToKeyword(Keyword k) {
 
 		// dont add extras if keyword extra doesent exist, -1 = not ranked, -2 =
 		// error, 0 = just added
 		if (k == null || k.newRank == -1 || k.newRank == -2 || k.newRank == 0)
 			return;
 
 		deleteFromExtrasTableById(k.id);
 
 		Log.d("MY", "ADD extra:" + k.keyword + " anchor:" + k.anchorText
 				+ " url:" + k.url);
 
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_EXTRA_PARENTID, k.id);
 		initialValues.put(KEY_EXTRA_ANCHOR, k.anchorText);
 		initialValues.put(KEY_EXTRA_URL, k.url);
 
 		if (mDb.insert(TABLE_EXTRA, null, initialValues) == -1)
 			Log.e("MY", "extra insert failed: " + k.keyword);
 
 	}
 
 	public void deleteAllFromExtrasTable(int userprofileId) {
 
 		ArrayList<UserProfile> all = loadAllProfiles();
 
 		open();
 
 		for (UserProfile u : all)
 			if (u.id == userprofileId)
 				for (Keyword k : u.keywords)
 					deleteFromExtrasTableById(k.id);
 
 		close();
 	}
 
 	private Boolean deleteFromExtrasTableById(int keywordId) {
 
 		int rowsAff = mDb.delete(TABLE_EXTRA, KEY_EXTRA_PARENTID + "="
 				+ keywordId, null);
 
 		return rowsAff != 0 ? true : false;
 
 	}
 
 	// private Boolean deleteFromExtrasTableByParentParentId(int id) {
 	//
 	// int rowsAff = mDb.delete(TABLE_EXTRA, KEY_EXTRA_PARENTID + "="
 	// + keywordId, null);
 	//
 	// return rowsAff != 0 ? true : false;
 	//
 	// }
 
 	public Boolean updateProfile(UserProfile profile) {
 
 		if (profile == null || profile.url == null || profile.id == 0
 				|| profile.keywords.size() <= 0)
 			return false;
 
 		Log.i("MY", "UPDATE PROFILE " + profile.url);
 
 		Boolean headerUpdateIsSuccess = false;
 		Boolean keywordUpdateIsSuccess = false;
 		Boolean keywordRankResetIsSuccess = false;
 
 		int idToUpdate = profile.id;
 
 		open();
 
 		// insert header
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_PROFILE_TABLE_URL, profile.url);
 		int numOfRowsAf = mDb.update(TABLE_PROFILE, initialValues,
 				KEY_PROFILE_TABLE_ID + " = " + idToUpdate, null);
 
 		headerUpdateIsSuccess = numOfRowsAf != 1 ? false : true;
 
 		// delete old keywords
 		numOfRowsAf = mDb.delete(TABLE_KEYWORDS, KEY_KEYWORDS_TABLE_PARENTID
 				+ " = " + idToUpdate, null);
 
 		for (Keyword keyword : profile.keywords) {
 
 			// insert keywords
 			ContentValues initialValuesKeywords = new ContentValues();
 			initialValuesKeywords.put(KEY_KEYWORDS_TABLE_KEYWORD,
 					keyword.keyword);
 			initialValuesKeywords.put(KEY_KEYWORDS_TABLE_PARENTID, idToUpdate);
 
 			Long l = mDb.insert(TABLE_KEYWORDS, null, initialValuesKeywords);
 			keywordUpdateIsSuccess = l == -1 ? false : true;
 
 			// reset keyword ranking values
 			ContentValues val = new ContentValues();
 			val.put(KEY_KEYWORDS_TABLE_POSTION, 0);
 			int rowsAff = mDb.update(TABLE_KEYWORDS, val,
 					KEY_KEYWORDS_TABLE_PARENTID + " = " + profile.id, null);
 			keywordRankResetIsSuccess = rowsAff != 0 ? true : false;
 
 			addExtraToKeyword(keyword);
 
 		}
 
 		close();
 
 		return (headerUpdateIsSuccess && keywordUpdateIsSuccess && keywordRankResetIsSuccess);
 
 	}
 
 	public Boolean insertProfile(UserProfile profile) {
 
 		if (profile == null)
 			return false;
 
 		Log.i("MY", "INSERT PROFILE " + profile.url);
 
 		Boolean headerInsertIsSuccess = false;
 		Boolean keywordInsertIsSuccess = false;
 
 		Long parentId = 0l;
 
 		open();
 
 		if (profile != null && profile.url != null) {
 
 			// insert header
 			ContentValues initialValues = new ContentValues();
 			initialValues.put(KEY_PROFILE_TABLE_URL, profile.url);
 			parentId = mDb.insert(TABLE_PROFILE, null, initialValues);
 
 			headerInsertIsSuccess = parentId == -1 ? false : true;
 
 			// insert keywords
 			for (Keyword keyword : profile.keywords) {
 
 				ContentValues initialValuesKeywords = new ContentValues();
 				initialValuesKeywords.put(KEY_KEYWORDS_TABLE_KEYWORD,
 						keyword.keyword);
 				initialValuesKeywords
 						.put(KEY_KEYWORDS_TABLE_PARENTID, parentId);
 
 				Long l = mDb
 						.insert(TABLE_KEYWORDS, null, initialValuesKeywords);
 
 				keywordInsertIsSuccess = l == -1 ? false : true;
 
 			}
 		}
 
 		close();
 
 		return (headerInsertIsSuccess && keywordInsertIsSuccess);
 	}
 
 	public Boolean deleteProfile(UserProfile profile) {
 
 		if (profile == null)
 			return false;
 
 		Log.i("MY", "DELETE PROFILE " + profile.url);
 
 		open();
 
 		// delete profile
 		int numOfRowsAfProfile = mDb.delete(TABLE_PROFILE, KEY_PROFILE_TABLE_ID
 				+ " = " + profile.id, null);
 
 		// delete all keywords
 		int numOfRowsAfKeywords = mDb.delete(TABLE_KEYWORDS,
 				KEY_KEYWORDS_TABLE_PARENTID + " = " + profile.id, null);
 
 		for (Keyword k : profile.keywords)
 			deleteFromExtrasTableById(k.id);
 
 		close();
 
 		Boolean profileDeleteSuccess = numOfRowsAfProfile != 0 ? true : false;
 		Boolean keywordDeleteSuccess = numOfRowsAfKeywords != 0 ? true : false;
 
 		return (profileDeleteSuccess && keywordDeleteSuccess);
 	}
 
 	public Boolean updateKeywordRank(Keyword k, int newRank) {
 
 		if (k == null || newRank == 0)
 			return false;
 
 		open();
 
 		ContentValues val = new ContentValues();
 		val.put(KEY_KEYWORDS_TABLE_POSTION, newRank);
 
 		// save new rank
 		int numOfRowsAf = mDb.update(TABLE_KEYWORDS, val, KEY_KEYWORDS_TABLE_ID
 				+ " = " + k.id, null);
 
 		// save url/anchor
 		addExtraToKeyword(k);
 
 		close();
 
 		return numOfRowsAf != 0 ? true : false;
 
 	}
 
 	public ArrayList<UserProfile> loadAllProfiles() {
 
 		ArrayList<UserProfile> profiles = new ArrayList<UserProfile>();
 
 		open();
 
 		// creating extra table if not exists because had that
 		// implemented/canceled in previous version
 		mDb.execSQL("CREATE TABLE IF NOT EXISTS "
 				+ String.format(
 						"%s ( %s INTEGER PRIMARY KEY, %s INTEGER NOT NULL,  %s TEXT NOT NULL, %s TEXT NOT NULL);",
 						TABLE_EXTRA, TABLE_ID, KEY_EXTRA_PARENTID,
 						KEY_EXTRA_ANCHOR, KEY_EXTRA_URL));
 
 		Cursor profileHeaderCur = mDb.query(TABLE_PROFILE, null, null, null,
 				null, null, null);
 
 		if (profileHeaderCur != null && profileHeaderCur.getCount() != 0) {
 
 			profileHeaderCur.moveToFirst();
 
 			do {
 
 				int profileId = profileHeaderCur.getInt(profileHeaderCur
 						.getColumnIndex(KEY_PROFILE_TABLE_ID));
 				String profileUrl = profileHeaderCur.getString(profileHeaderCur
 						.getColumnIndex(KEY_PROFILE_TABLE_URL));
 
 				// we have name and ID now lets get the keywords
 				ArrayList<Keyword> keywords = new ArrayList<Keyword>();
 
 				Cursor keywordsCur = mDb.query(TABLE_KEYWORDS, null,
 						KEY_KEYWORDS_TABLE_PARENTID + " = " + profileId, null,
 						null, null, null);
 
 				if (keywordsCur != null && keywordsCur.getColumnCount() > 0
 						&& keywordsCur.getCount() > 0) {
 					// profile has keywords
 
 					keywordsCur.moveToFirst();
 
 					do {
 
 						try {
 							int id = keywordsCur.getInt(keywordsCur
 									.getColumnIndex(KEY_KEYWORDS_TABLE_ID));
 
 							String keyword = keywordsCur
 									.getString(keywordsCur
 											.getColumnIndex(KEY_KEYWORDS_TABLE_KEYWORD));
 
 							int rank = keywordsCur
 									.getInt(keywordsCur
 											.getColumnIndex(KEY_KEYWORDS_TABLE_POSTION));
 
 							String anchor = getExtraAnchorById(id);
 							String url = getExtraUrlById(id);
 
 							keywords.add(new Keyword(id, keyword, rank, anchor,
 									url));
 
 						} catch (Exception e) {
 							Log.e("MY", "dbatapter parsing from cursor error: "
 									+ e.toString());
 						}
 
 					} while (keywordsCur.moveToNext());
 
 					// v 2.02 fix
 					if (keywordsCur != null || !keywordsCur.isClosed())
 						keywordsCur.close();
 
 					// one profile done, not let's get next one
 					profiles.add(new UserProfile(profileId, profileUrl,
 							keywords));
 
 				}
 
 			} while (profileHeaderCur.moveToNext());
 
 			// v 2.02 fix
 			if (profileHeaderCur != null || !profileHeaderCur.isClosed())
 				profileHeaderCur.close();
 
 		}
 
 		close();
 
 		return profiles;
 
 	}
 
 	private String getExtraUrlById(int id) {
 
 		String result = "error getExtraUrlById";
 
 		Cursor cur = mDb.query(TABLE_EXTRA, null,
 				KEY_EXTRA_PARENTID + "=" + id, null, null, null, null);
 
 		if (cur != null && cur.getCount() > 0 && cur.getColumnCount() > 0) {
 			cur.moveToFirst();
 			result = cur.getString(cur.getColumnIndex(KEY_EXTRA_URL));
 
			// v 2.02 fix
			if (!cur.isClosed())
				cur.close();

 		}
 
 		return result;
 
 	}
 
 	private String getExtraAnchorById(int id) {
 
 		String result = "error getExtraAnchorById";
 
 		Cursor cur = mDb.query(TABLE_EXTRA, null,
 				KEY_EXTRA_PARENTID + "=" + id, null, null, null, null);
 
 		if (cur != null && cur.getCount() > 0 && cur.getColumnCount() > 0) {
 			cur.moveToFirst();
 
 			result = cur.getString(cur.getColumnIndex(KEY_EXTRA_ANCHOR));
 
			// v 2.02 fix
			if (!cur.isClosed())
				cur.close();
 		}
 
 		return result;
 	}
 
 	public void trunkTables() {
 
 		open();
 
 		mDb.execSQL("drop table if exists " + TABLE_PROFILE);
 		mDb.execSQL("drop table if exists " + TABLE_KEYWORDS);
 		mDb.execSQL("drop table if exists " + TABLE_EXTRA);
 
 		mDb.execSQL(PROFILE_TABLE_CREATE);
 		mDb.execSQL(KEYWORDS_TABLE_CREATE);
 		mDb.execSQL(EXTRA_TABLE_CREATE);
 
 		close();
 
 	}
 
 	/**
 	 * @param inputSite
 	 *            - inserted or edited page url
 	 * @param keyword
 	 *            - all keywords on seperate rows
 	 * @param id
 	 *            - if is update then id != 0
 	 * @return - if was success
 	 */
 	public Boolean insertOrUpdate(String inputSite, String keyword, int id) {
 
 		String[] keywords = null;
 
 		if (keyword.contains("\n"))
 			keywords = keyword.split("\\n");
 		else if (keyword.contains(","))
 			keywords = keyword.split(",");
 		else if (keyword.contains(";"))
 			keywords = keyword.split(";");
 		else
 			keywords = new String[] { keyword };
 
 		// generate array list
 		ArrayList<Keyword> keywordsArrayList = new ArrayList<Keyword>();
 		for (String s : keywords)
 			keywordsArrayList.add(new Keyword(s));
 
 		Boolean result = false;
 
 		if (id == 0)
 			result = insertProfile(new UserProfile(inputSite, keywordsArrayList));
 		else
 			result = updateProfile(new UserProfile(id, inputSite,
 					keywordsArrayList));
 
 		return result;
 
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, 2);
 		}
 
 		public DatabaseHelper(Context context, String name,
 				CursorFactory factory, int version) {
 			super(context, name, factory, version);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// Called when the database is created for the first time. This is
 			// where the creation of tables and the initial population of the
 			// tables should happen.
 
 			Log.i("MY",
 					"PROFILE TABLE CREATE + KEYWORDS TABLE CREATE + EXTRA TABLE CREATE");
 			db.execSQL(PROFILE_TABLE_CREATE);
 			db.execSQL(KEYWORDS_TABLE_CREATE);
 			db.execSQL(EXTRA_TABLE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// Called when the database needs to be upgraded. The implementation
 			// should use this method to drop tables, add tables, or do anything
 			// else it needs to upgrade to the new schema version.
 
 			if (db.getVersion() == oldVersion) {
 				db.setVersion(newVersion);
 
 				db.execSQL("drop table if exists " + TABLE_PROFILE);
 				db.execSQL("drop table if exists " + TABLE_KEYWORDS);
 
 				db.execSQL(PROFILE_TABLE_CREATE);
 				db.execSQL(KEYWORDS_TABLE_CREATE);
 			}
 		}
 
 	}
 
 }
