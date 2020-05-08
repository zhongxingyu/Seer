 package my.codeandroid.itisvid.db;
 
 import my.codeandroid.itisvid.ItisVidConstants;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 /**
  * @author bernie
  * 
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final String TABLE_NAME = "fav_cams";
 
 	public DatabaseHelper(Context context, String name, CursorFactory factory,
 			int version) {
 		super(context, name, factory, version);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + "_id"
 				+ " INTEGER PRIMARY KEY," + FavCam.CAM_NAME + " TEXT,"
 				+ FavCam.CAM_URL + " TEXT" + ");");
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(ItisVidConstants.LOGTAG, "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS notes");
 		onCreate(db);
 
 	}
 
 	public Cursor getFavourites() {
 
 		// Get the database and run the query
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
 
 		return c;
 	}
 
	public String getSelectedCamUrl(long id) {
 		SQLiteDatabase db = getReadableDatabase();
		Log.i(ItisVidConstants.LOGTAG, "Retrieving entry:" + id);
		Cursor c = db.query(TABLE_NAME, null, "_id=" + id, null, null, null, null, null);
 		c.moveToFirst();
		Log.d(ItisVidConstants.LOGTAG, "retrieved cam url: " + c.getString(2));
 		return c.getString(2);
 	}
 
 	public void insertFavourites(ContentValues initialValues) {
 		ContentValues values;
 		if (initialValues != null) {
 			values = new ContentValues(initialValues);
 		} else {
 			values = new ContentValues();
 		}
		Log.d(ItisVidConstants.LOGTAG, "retrieved cam url: " + values.toString());
 		SQLiteDatabase db = getWritableDatabase();
 		long rowId = db.insert(TABLE_NAME, FavCam.CAM_NAME, values);
 		if (rowId > 0) {
 			Log.i(ItisVidConstants.LOGTAG, "Data entered: " + rowId);
 		}
 
 	}
 
	public int delete(long id) {
 		SQLiteDatabase db = getWritableDatabase();
		Log.i(ItisVidConstants.LOGTAG, "Deleting entry:" + id);
		int count = db.delete(TABLE_NAME, "_id=" + id, null);
 		Log.i(ItisVidConstants.LOGTAG, "Data deleted (number of rows): "
 				+ count);
 		return count;
 	}
 
 }
