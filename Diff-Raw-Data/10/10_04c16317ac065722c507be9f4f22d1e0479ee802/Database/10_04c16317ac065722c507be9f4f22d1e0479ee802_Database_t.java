 package com.shoutbreak.service;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import com.shoutbreak.Shout;
 import com.shoutbreak.Vars;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 public class Database {
 
     private HashMap<String, String> _userSettings;
    private boolean _userSettingsAreStale;
     private Context _context;
     private SQLiteDatabase _db;
     private OpenHelper _openHelper;
     private static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
     
     public Database(Context context) {
     	_context = context;
    	_userSettingsAreStale = true;
     	_openHelper = new OpenHelper(_context);
 		open();
     }
     
     public void open() {
     	this._db = _openHelper.getWritableDatabase();
     	this._db.setLockingEnabled(true);    	
     }
     
     public void close() {
     	this._db.close();
     }
     
     public static String getDateAsISO8601String(Date date) {
       String result = ISO8601FORMAT.format(date);
       //convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00 
       //- note the added colon for the Timezone
       result = result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);
       return result;
     }
     
     public CellDensity getDensityAtCell(CellDensity cell) {
     	CellDensity result = new CellDensity();
     	result.isSet = false;
     	String sql = "SELECT density, last_updated FROM " + Vars.DB_TABLE_DENSITY + " WHERE cell_x = ? AND cell_y = ?";
     	Cursor cursor = null;
     	try {
     		cursor = _db.rawQuery(sql, new String[] { Integer.toString(cell.cellX), Integer.toString(cell.cellY) });
     		if (cursor.moveToFirst()) {
     			String lastUpdated = cursor.getString(1);
     			long lastUpdatedMillisecs = Date.parse(lastUpdated);
     			long diff = (new Date().getTime()) - lastUpdatedMillisecs;
     			if (diff < Vars.DENSITY_EXPIRATION) {
     				result.density = cursor.getFloat(0);
     				result.isSet = true;
     				return result;
     			} 
     		}
     	} catch (Exception ex) {
     		Log.e(getClass().getSimpleName(), "getUserSettings"); 		
     	} finally {
     		if (cursor != null && !cursor.isClosed()) {
         		cursor.close();
         	}
     	}
     	return result;
     }
     
     public Long saveCellDensity(CellDensity cellDensity) {
     	String sql = "INSERT INTO " + Vars.DB_TABLE_DENSITY + " (cell_x, cell_y, density, last_updated) VALUES (?, ?, ?, ?)";
     	SQLiteStatement insert = this._db.compileStatement(sql);
     	insert.bindLong(1, cellDensity.cellX);
     	insert.bindLong(2, cellDensity.cellY);
     	insert.bindDouble(3, cellDensity.density);
     	insert.bindString(4, getDateAsISO8601String(new Date()));
     	try {
     		return insert.executeInsert();
     	} catch (Exception ex) {
     		Log.e(getClass().getSimpleName(), "saveUserSetting");	
     	} finally {
     		insert.close();	
     	}
     	return 0l;
     }    
     
     public HashMap<String, String> getUserSettings() {
    	if (_userSettingsAreStale) {
     		_userSettings = new HashMap<String, String>();
     		Cursor cursor = null;
     		try {
 	    		cursor = this._db.query(Vars.DB_TABLE_USER_SETTINGS, null, null, null, null, null, null, null);
 	    		while (cursor.moveToNext()) {
 	    			_userSettings.put(cursor.getString(0), cursor.getString(1));
 	    		}
	    		_userSettingsAreStale = false;
 	    	} catch (Exception ex) {
 	    		Log.e(getClass().getSimpleName(), "getUserSettings"); 		
 	    	} finally {
 	    		if (cursor != null && !cursor.isClosed()) {
 	        		cursor.close();
 	        	}
 	    	}
     	}
     	return _userSettings;
     }
     
     public Long saveUserSetting(String key, String value) {
    	_userSettingsAreStale = true;
     	String sql = "INSERT INTO " + Vars.DB_TABLE_USER_SETTINGS + " (setting_key, setting_value) VALUES (?, ?)";
     	SQLiteStatement insert = this._db.compileStatement(sql);
     	insert.bindString(1, key); // 1-indexed
     	insert.bindString(2, value);
     	try {
     		return insert.executeInsert();
     	} catch (Exception ex) {
     		Log.e(getClass().getSimpleName(), "saveUserSetting");	
     	} finally {
     		insert.close();	
     	}
     	return 0l;
     }
     
     public Long addShoutToInbox(Shout shout) {
     	//(shout_id TEXT, timestamp TEXT, time_received INTEGER, txt TEXT, is_outbox INTEGER, re TEXT, vote INTEGER, hit INTEGER, open INTEGER, ups INTEGER, downs INTEGER, pts INTEGER, approval INTEGER)
     	String sql = "INSERT INTO " + Vars.DB_TABLE_SHOUTS + " (shout_id, timestamp, time_received, txt, is_outbox, re, vote, hit, open, ups, downs, pts, approval, state_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     	SQLiteStatement insert = this._db.compileStatement(sql);
     	insert.bindString(1, shout.id); // 1-indexed
     	insert.bindString(2, shout.timestamp);
     	insert.bindLong(3, shout.time_received);
     	insert.bindString(4, shout.text);
     	insert.bindLong(5, shout.is_outbox ? 0 : 1);
     	insert.bindString(6, shout.re);
     	insert.bindLong(7, shout.vote);
     	insert.bindLong(8, shout.hit);
     	insert.bindLong(9, shout.open ? 0 : 1);
     	insert.bindLong(10, shout.ups);
     	insert.bindLong(11, shout.downs);
     	insert.bindLong(12, shout.pts);
     	insert.bindLong(13, shout.approval);
     	insert.bindLong(14, shout.state_flag);
     	try {
     		return insert.executeInsert();
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);
     	} finally {
     		insert.close();	
     	}
     	return 0l;
     }
     
     public boolean reflectVote(String shoutID, int vote) {
     	boolean result = false;
     	String sql = "UPDATE " + Vars.DB_TABLE_SHOUTS + " SET ups = ups + 1, vote = ? WHERE shout_id = ?";
     	if (vote < 0) {
     		sql = "UPDATE " + Vars.DB_TABLE_SHOUTS + " SET downs = downs + 1, vote = ? WHERE shout_id = ?";
     	}
         SQLiteStatement update = this._db.compileStatement(sql);
         update.bindLong(1, vote);
         update.bindString(2, shoutID);
     	try {
     		update.execute();
     		result = true;
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);
     	} finally {
     		update.close();	
     	}
         return result;
     }
     
     public boolean updateScore(Shout shout) {
     	boolean result = false;
     	SQLiteStatement update;
     	// do we have hit count?
     	if (shout.hit != Vars.NULL_HIT) {
         	String sql = "UPDATE " + Vars.DB_TABLE_SHOUTS + " SET ups = ?, downs = ?, hit = ?, pts = ?, open = ? WHERE shout_id = ?";
         	update = this._db.compileStatement(sql);
         	update.bindLong(1, shout.ups);
         	update.bindLong(2, shout.downs);
         	update.bindLong(3, shout.hit);
         	update.bindLong(4, shout.pts);
         	int isOpen = (shout.open) ? 1 : 0;
         	update.bindLong(5, isOpen);
         	update.bindString(6, shout.id);
     	} else {
     		String sql = "UPDATE " + Vars.DB_TABLE_SHOUTS + " SET pts = ?, approval = ?, open = ? WHERE shout_id = ?";
         	update = this._db.compileStatement(sql);
         	update.bindLong(1, shout.pts);
         	update.bindLong(2, shout.approval);
         	int isOpen = (shout.open) ? 1 : 0;
         	update.bindLong(3, isOpen);
         	update.bindString(4, shout.id);
     	}
     	try {
     		update.execute();
     		result = true;
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);
     	} finally {
     		update.close();	
     	}
         return result;
     }
     
     public ArrayList<Shout> getShouts(int start, int amount) {
     	// shout_id TEXT, timestamp TEXT, time_received INTEGER, txt TEXT, is_outbox INTEGER, re TEXT, vote INTEGER, hit INTEGER, open INTEGER, ups INTEGER, downs INTEGER, pts INTEGER, approval INTEGER, state_flag INTEGER
     	ArrayList<Shout> results = new ArrayList<Shout>();
     	//String sql = "SELECT * FROM " + Vars.DB_TABLE_SHOUTS ; // OFFSET ? 
     	String sql = "SELECT * FROM " + Vars.DB_TABLE_SHOUTS + " ORDER BY time_received DESC LIMIT ? OFFSET ? ";
     	Cursor cursor = null;
     	try {
     		cursor = _db.rawQuery(sql, new String[] { Integer.toString(amount), Integer.toString(start) });
     		//cursor = _db.rawQuery(sql, new String[]);
     		while (cursor.moveToNext()) {
     			Shout s = new Shout();
     			s.id = cursor.getString(0);
     			s.timestamp = cursor.getString(1);
     			s.time_received = cursor.getLong(2);
     			s.text = cursor.getString(3);
     			s.is_outbox = cursor.getInt(4) == 1 ? true : false;
     			s.re = cursor.getString(5);
     			s.vote = cursor.getInt(6);
     			s.hit = cursor.getInt(7);
     			s.open = cursor.getInt(8) == 1 ? true : false;
     			s.ups = cursor.getInt(9);
     			s.downs = cursor.getInt(10);
     			s.pts = cursor.getInt(11);
     			s.approval = cursor.getInt(12);
     			s.state_flag = cursor.getInt(13); 
     			s.calculateScore();
     			results.add(s);
     		}
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);		
     	} finally {
     		if (cursor != null && !cursor.isClosed()) {
         		cursor.close();
         	}
     	}
     	return results;
     }
     
     public Shout getShout(String shoutID) {
     	String sql = "SELECT * FROM " + Vars.DB_TABLE_SHOUTS + " WHERE shout_id = ?";
     	Cursor cursor = null;
     	try {
     		cursor = _db.rawQuery(sql, new String[] { shoutID });
     		if (cursor.moveToNext()) {
     			Shout s = new Shout();
     			s.id = cursor.getString(0);
     			s.timestamp = cursor.getString(1);
     			s.time_received = cursor.getLong(2);
     			s.text = cursor.getString(3);
     			s.is_outbox = cursor.getInt(4) == 1 ? true : false;
     			s.re = cursor.getString(5);
     			s.vote = cursor.getInt(6);
     			s.hit = cursor.getInt(7);
     			s.open = cursor.getInt(8) == 1 ? true : false;
     			s.ups = cursor.getInt(9);
     			s.downs = cursor.getInt(10);
     			s.pts = cursor.getInt(11);
     			s.approval = cursor.getInt(12);
     			s.state_flag = cursor.getInt(13);
     			s.calculateScore();
     			return s;
     		}
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);		
     	} finally {
     		if (cursor != null && !cursor.isClosed()) {
         		cursor.close();
         	}
     	}
     	return null;
     }
     
     public boolean markShoutAsRead(String shoutID) {
     	boolean result = false;
     	SQLiteStatement update;
     	String sql = "UPDATE " + Vars.DB_TABLE_SHOUTS + " SET state_flag = ? WHERE shout_id = ?";
     	update = this._db.compileStatement(sql);
     	update.bindString(1, Vars.SHOUT_STATE_READ + "");
     	update.bindString(2, shoutID);
     	try {
     		update.execute();
     		result = true;
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);
     	} finally {
     		update.close();	
     	}
        	return result;  	
     }
     
     public boolean deleteShout(String shoutID) {
     	boolean result = false;
     	SQLiteStatement update;
     	String sql = "DELETE FROM " + Vars.DB_TABLE_SHOUTS + " WHERE shout_id = ?";
     	update = this._db.compileStatement(sql);
     	update.bindString(1, shoutID);
     	try {
     		update.execute();
     		result = true;
     	} catch (Exception ex) {
     		ErrorManager.manage(ex);
     	} finally {
     		update.close();	
     	}
        	return result;
     }
         
     private static class OpenHelper extends SQLiteOpenHelper {
     	
     	OpenHelper(Context context) {
     		super(context, Vars.DB_NAME, null, Vars.DB_VERSION);
     	}
     	
     	@Override
     	public void onCreate(SQLiteDatabase db) {
     		db.execSQL("CREATE TABLE " + Vars.DB_TABLE_USER_SETTINGS + " (setting_key TEXT, setting_value TEXT)");
     		db.execSQL("CREATE TABLE " + Vars.DB_TABLE_DENSITY + " (cell_x INTEGER, cell_y INTEGER, density REAL, last_updated TEXT)");
     		db.execSQL("CREATE TABLE " + Vars.DB_TABLE_SHOUTS + " (shout_id TEXT, timestamp TEXT, time_received INTEGER, txt TEXT, is_outbox INTEGER, re TEXT, vote INTEGER, hit INTEGER, open INTEGER, ups INTEGER, downs INTEGER, pts INTEGER, approval INTEGER, state_flag INTEGER)");
     	}
     	
     	@Override
     	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     		Log.w(getClass().getSimpleName(), "onUpgrade");
     		db.execSQL("DROP TABLE IF EXISTS " + Vars.DB_TABLE_USER_SETTINGS);
     		db.execSQL("DROP TABLE IF EXISTS " + Vars.DB_TABLE_DENSITY);
     		db.execSQL("DROP TABLE IF EXISTS " + Vars.DB_TABLE_SHOUTS);
     		onCreate(db);
     	}
     	
     }
 }
