 package nl.rnplus.olv.data;
 
 /**
  * LiveViewDbHelper2.java
  * @Author Renze Nicolai
  * Replacement for LiveViewDbHelper.java
  */
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 public class LiveViewDbHelper {
   
  private static final String SCRIPT_CREATE_TABLE_ALERT_ITEMS =
   "create table " + LiveViewDbConstants.TABLE_ALERT_ITEMS + " ("
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID + " integer primary key, " 
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE + " text, "
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT + " text not null, "
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE + " integer, "
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP + " integer, "
   + LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD + " integer "
   +");";
  
  private SQLiteHelper sqLiteHelper;
  private SQLiteDatabase sqLiteDatabase;
 
  private Context context;
  
  public LiveViewDbHelper(Context c){
   context = c;
  }
  
  public LiveViewDbHelper openToRead() throws android.database.SQLException {
   sqLiteHelper = new SQLiteHelper(context, LiveViewDbConstants.DATABASE_NAME, null, LiveViewDbConstants.DATABASE_VERSION);
   sqLiteDatabase = sqLiteHelper.getReadableDatabase();
   return this; 
  }
  
  public LiveViewDbHelper openToWrite() throws android.database.SQLException {
   sqLiteHelper = new SQLiteHelper(context, LiveViewDbConstants.DATABASE_NAME, null, LiveViewDbConstants.DATABASE_VERSION);
   sqLiteDatabase = sqLiteHelper.getWritableDatabase();
   return this; 
  }
  
  public void close(){
   sqLiteHelper.close();
  }
  
  public long insertAlert(String title, String content, int type, long timestamp){
   ContentValues contentValues = new ContentValues();
   contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT, content);
   contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE, title);
   contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE, type);
   contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP, timestamp);  
   contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD, 1);  
   return sqLiteDatabase.insert(LiveViewDbConstants.TABLE_ALERT_ITEMS, null, contentValues);
  }
  
  public void setAlertRead(int id) {
 	 ContentValues contentValues = new ContentValues();
 	 contentValues.put(LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD, 0);  
 	 String where = LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID + " = " + String.valueOf(id);
 	 sqLiteDatabase.update(LiveViewDbConstants.TABLE_ALERT_ITEMS, contentValues, where, null);
  }
  
  public int deleteAllAlerts(){
   return sqLiteDatabase.delete(LiveViewDbConstants.TABLE_ALERT_ITEMS, null, null);
  }
  
 public String getAlertsAsString(){
 	String[] columns = new String[]{LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE, LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP, LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD};
 	Cursor cursor = sqLiteDatabase.query(LiveViewDbConstants.TABLE_ALERT_ITEMS, columns, null, null, null, null, null);
 	String result = "";
 	int index_CONTENT = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT);
 	int index_TITLE = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE);
 	@SuppressWarnings("unused")
 	int index_TYPE = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE);
 	@SuppressWarnings("unused")
 	int index_TIMESTAMP = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP);
 	for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()){
 		try {
 			String type = "(Unknown)";
 			if (cursor.getInt(index_TYPE) == LiveViewDbConstants.ALERT_ANDROID) {
 				type = "(Notification)";
 			}
 			if (cursor.getInt(index_TYPE) == LiveViewDbConstants.ALERT_SMS) {
 				type = "(SMS)";
 			}
 			if (cursor.getInt(index_TYPE) == LiveViewDbConstants.ALERT_NOTE) {
 				type = "(Note)";
 			}
 			result = result + type + " " + cursor.getString(index_TITLE) + ": " + cursor.getString(index_CONTENT) + "\n";
 		} catch (Exception e) {
 			Log.e("db helper", "Error: "+e.getMessage());
 		}
 	}
 	return result;
 }
 
 public Cursor getAlertsOfType(int type){
 	String[] columns = new String[]{LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE, LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE, LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP, LiveViewDbConstants.COLUMN_ALERT_ITEMS_UNREAD};
 	String where = null;
 	if (type!=LiveViewDbConstants.ALERT_ALL) {
 		where = LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE + " = "+ String.valueOf(type);
 	}
	Cursor cursor = sqLiteDatabase.query(LiveViewDbConstants.TABLE_ALERT_ITEMS, columns, where, null, null, null, LiveViewDbConstants.COLUMN_ALERT_ITEMS_ID+" DESC");
 	//int index_CONTENT = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_CONTENT);
 	//int index_TITLE = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TITLE);
 	//int index_TYPE = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TYPE);
 	//int index_TIMESTAMP = cursor.getColumnIndex(LiveViewDbConstants.COLUMN_ALERT_ITEMS_TIMESTAMP);
 	//cursor.getString(index_TITLE)
 	return cursor;
 }
 
 public int getIntColumnFromCursor(Cursor cursor, String Column) {
 	return cursor.getInt(cursor.getColumnIndex(Column));
 }
 
 public String getStringColumnFromCursor(Cursor cursor, String Column) {
 	return cursor.getString(cursor.getColumnIndex(Column));
 }
  
  public class SQLiteHelper extends SQLiteOpenHelper {
 
   public SQLiteHelper(Context context, String name,
     CursorFactory factory, int version) {
    super(context, name, factory, version);
   }
 
   @Override
   public void onCreate(SQLiteDatabase db) {
    db.execSQL(SCRIPT_CREATE_TABLE_ALERT_ITEMS);
   }
 
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       if (oldVersion != newVersion)
       {
       	try {
 	            db.execSQL("DROP TABLE " + LiveViewDbConstants.TABLE_ALERT_ITEMS);
       	} catch(Exception e) {
       		Log.e("LiveViewDbHelper", "Could not delete all tables. "+e.toString());
       	}
           onCreate(db);
       }
   } 
  }
 }
