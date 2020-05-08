 package me.toddpickell.baristalog;
 
 import java.util.List;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DataManager {
 	
 	static final int DATABASE_VERSION = 1;
 	private Context context;
 	private SQLiteDatabase db;
 	private LogDao logDao;
 	
 
 	public DataManager(Context context) {
 		this.context = context;
 		SQLiteOpenHelper openHelper = new OpenHelper(this.context);
 		db = openHelper.getWritableDatabase();
 		logDao = new LogDao(db);
 	}
 
 
 	public LogNote getLogNote(long lognoteId) {
 		LogNote lognote = logDao.get(lognoteId);
 		return lognote;
 	}
 
 
 	public List<LogNote> getLogNoteHeaders() {	
 		return logDao.getAll();
 	}
 
 
 	public List<LogNote> getLogNotesByDevice(String device) {
 		//if string for device name has a space need to remove for column name
		device = device.replaceAll("\\s","_");
 		List<LogNote> lognotes = logDao.getAllLogsByDevice(device);
 		return lognotes;
 	}
 
 
 	public List<LogNote> getLogNotesByBlend(String blend) {
 		List<LogNote> lognotes = logDao.getAllLogsByBlend(blend);
 		return lognotes;
 	}
 
 
 	public long saveLogNote(LogNote lognote) {
 		Long noteId = 0L;
 		try {
 			db.beginTransaction();
 			noteId = logDao.save(lognote);
 			db.setTransactionSuccessful();
 			
 		} catch (SQLException e) {
 			Log.e("DATA_MANAGER", "Error saving log note (transaction rolled back)", e);
 			noteId = 0L;
 			
 		} finally {
 			db.endTransaction();
 		}
 		return noteId;
 	}
 
 
 	public boolean deleteLogNote(long lognoteId) {
 		boolean result = false;
 		try {
 			db.beginTransaction();
 			LogNote lognote = getLogNote(lognoteId);
 			if (lognote != null) {
 				logDao.delete(lognote);
 			}
 			db.setTransactionSuccessful();
 			result = true;
 			
 		} catch (SQLException e) {
 			Log.e("DATA_MANAGER", "Error deleting log note (transaction rolled back)", e);
 			
 		} finally {
 			db.endTransaction();
 		}
 		return result;
 	}
 
 }
