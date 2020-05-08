 package org.dyndns.pawitp.salayatrammap.map;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.dyndns.pawitp.salayatrammap.R;
 import org.dyndns.pawitp.salayatrammap.Utils;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 
 public class TramDbHelper {
 	
     public static final String KEY_ROWID = "_id";
     public static final String KEY_NAME_TH = "name_th";
     public static final String KEY_NAME_EN = "name_en";
     public static final String KEY_X = "x";
     public static final String KEY_Y = "y";
     public static final String KEY_TRAM_GREEN = "tram_green";
     public static final String KEY_TRAM_BLUE = "tram_blue";
     public static final String KEY_TRAM_RED = "tram_red";
     
     private static final String TABLE_STOPS = "stops";
     
     private static final String DATABASE_NAME = "tram.db";
    private static final int DATABASE_VERSION = 3;
     
     private SQLiteDatabase mDb;
     private boolean mUpgrading = false;
     
     private final Context mContext;
 
 	public TramDbHelper(Context context) {
 		mContext = context;
 	}
 	
 	public void open() {
 		if (mDb != null && mDb.isOpen()) {
 			return;
 		}
 		
 		try {
 			String path = mContext.getDatabasePath(DATABASE_NAME).getPath();
         	mDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
 		}
 		catch (SQLiteException e) { // file not found
 			mDb = mContext.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null); // create the folder if not exist
 			copyDatabase();
 		}
 		
 		if (mDb.getVersion() != DATABASE_VERSION) {
 			copyDatabase();
 		}
 	}
 	
 	public void close() {
 		if (mDb != null) {
 			mDb.close();
 			mDb = null;	
 		}
 	}
 	
 	// limit is dx + dy NOT pythagorus
 	public Cursor findNearestStop(int x, int y, int limit) {
 		String query = String.format("SELECT _id, name_th, name_en, x, y, abs(%d - x) + abs(%d - y) AS d FROM stops WHERE d < %d ORDER BY d ASC LIMIT 1", x, y, limit);
 		Cursor cursor = mDb.rawQuery(query, null);
 		
 		cursor.moveToFirst();
 		
 		return cursor;
 	}
 
 	public Cursor getStopInfo(int stopId) {
 		Cursor cursor = mDb.query(TABLE_STOPS, new String[] { KEY_ROWID, KEY_NAME_TH, KEY_NAME_EN, KEY_X, KEY_Y } , KEY_ROWID + "=" + stopId, null, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 	
 	// term uses sqlite FTS3
 	public Cursor getSuggestions(String term) {
 		term = "*" + term.replace(" ", "*") + "*";
 		String query = String.format("SELECT _id, _id AS %s, name_en AS %s, name_th AS %s FROM stops WHERE stops MATCH ?", SearchManager.SUGGEST_COLUMN_INTENT_DATA, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2);
 		Cursor cursor = mDb.rawQuery(query, new String[] { term });
 		return cursor;
 	}
 	
 	public Cursor getAllSuggestions() {
 		String query = String.format("SELECT _id, _id AS %s, name_en AS %s, name_th AS %s FROM stops", SearchManager.SUGGEST_COLUMN_INTENT_DATA, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2);
 		Cursor cursor = mDb.rawQuery(query, null);
 		return cursor;
 	}
 	
 	public int getFirstSearchResult(String term) {
 		term = "*" + term.replace(" ", "*") + "*";
 		Cursor cursor = mDb.rawQuery("SELECT _id FROM stops WHERE stops MATCH ? LIMIT 1", new String[] { term });
 		cursor.moveToFirst();
 		int ret = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
 		cursor.close();
 		
 		return ret;
 	}
 	
 	private void copyDatabase() {
 		try {
 			if (mUpgrading) {
 				// Recursive upgrade!?
 				throw new IllegalStateException("Recursive upgrade");
 			}
 			
 			// Replace database with one from the apk
 			mUpgrading = true;
 			
 			close();
 			
 			InputStream is = mContext.getResources().openRawResource(R.raw.database);
 			Utils.writeInputStreamToFile(is, mContext.getDatabasePath(DATABASE_NAME));
 			
 			open();
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 }
