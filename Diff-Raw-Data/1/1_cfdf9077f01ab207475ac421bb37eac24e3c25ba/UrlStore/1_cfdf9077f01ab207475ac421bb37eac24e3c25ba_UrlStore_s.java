 package org.nosreme.app.urlhelper;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class UrlStore {
 	   private static final String URLSTORE_TABLE_NAME = "urls";
 	   private static final String HANDLER_TABLE_NAME = "handlers";
 	     
 	   static class DbHelper extends SQLiteOpenHelper {
         private static final String DATABASE_NAME = "urlstore.db";
         private static final int DATABASE_VERSION = 5;
         
         private static final String URLSTORE_UPDATE_VERSION_4 =
         		"ALTER TABLE " + URLSTORE_TABLE_NAME + " ADD COLUMN expanded DEFAULT 0;" +
                 "ALTER TABLE " + URLSTORE_TABLE_NAME + " ADD COLUMN comment TEXT;";
         
         private static final String HANDLER_TABLE_CREATE =
                 /* handler table stores a list of activities which
                  * we've seen, with a preference ordering.
                  */
                 "CREATE TABLE " + HANDLER_TABLE_NAME + " (" +
                     "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "optionsKey TEXT UNIQUE," +  /* Encoded list of handlers */
                     "packageName TEXT," +
                     "name TEXT" +
                 ");";
         private static final String URLSTORE_TABLE_CREATE =
                 "CREATE TABLE " + URLSTORE_TABLE_NAME + " (" +
                     "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "url TEXT," +
                     "orig_url TEXT," +
                     "time INTEGER," +
                     "seen INTEGER," +
                     "expanded INTEGER," +
                     "comment TEXT" + 
                 ");\n";
 
         DbHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
         
          @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(URLSTORE_TABLE_CREATE);
             db.execSQL(HANDLER_TABLE_CREATE);
             ContentValues values = new ContentValues();
             values.put("url", "http://www.nosreme.org");
             values.put("orig_url", "http://www.nosreme.org");
             values.put("time", System.currentTimeMillis());
             values.put("expanded", 0);
             values.put("seen", 0);
 			db.insert(URLSTORE_TABLE_NAME, "URL", values);
         }
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
         	if (oldVersion < 3)
         	{
         		/* No supported upgrade */
                 // Kills the table and existing data
                 db.execSQL("DROP TABLE IF EXISTS " + URLSTORE_TABLE_NAME);
                 db.execSQL("DROP TABLE IF EXISTS " + HANDLER_TABLE_NAME);
 
                 // Recreates the database with a new version
                 onCreate(db);
                 return;
         	}
         	if (oldVersion < 4)
         	{
         	    db.execSQL(URLSTORE_UPDATE_VERSION_4);
         	}
         	if (oldVersion < 5)
         	{
         		/* The handler table definition changed. */
         		db.execSQL("DROP TABLE IF EXISTS " + HANDLER_TABLE_NAME);
         		db.execSQL(HANDLER_TABLE_CREATE);
         	}
 
         }
     }
     
     private DbHelper dbhelper;
     
     public UrlStore(Context context)
     {
     	dbhelper = new DbHelper(context);
     }
     
     private String[] cols = new String[] { "_id", "url", "seen", "expanded", "time" };
     private String[] handlerCols = new String[] { "_id", "optionsKey", "packageName", "name" };
     
     public Cursor getUrlCursor()
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
     	db = dbhelper.getReadableDatabase();
     	
     	Cursor cursor = db.query(URLSTORE_TABLE_NAME, cols, null, null, null, null, null);
     	
     	return cursor;
     }
     public Cursor getUnexpanded()
     {
     	SQLiteDatabase db;
     	db = dbhelper.getReadableDatabase();
     	
     	Cursor cursor = db.query(URLSTORE_TABLE_NAME, cols, "expanded = 0", null, null, null, null);
     	
     	return cursor;
     }
     public void addUrl(String url)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put("url", url);  
         values.put("orig_url", url);  
         values.put("time", System.currentTimeMillis());
         values.put("seen", 0);
 		db.insert(URLSTORE_TABLE_NAME, "URL", values);
 		db.close();
     }
     public int removeUrl(long id)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
 		int result = db.delete(URLSTORE_TABLE_NAME, "_id = " + Long.toString(id), null);
 		db.close();
 		
 		return result;
     }
     public int removeAllUrls()
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
 		int result = db.delete(URLSTORE_TABLE_NAME, null, null);
 		db.close();
 		
 		return result;
     }
     public String getUrl(long id) 
     {
     	SQLiteDatabase db = dbhelper.getReadableDatabase();
     	Cursor cursor = db.query(URLSTORE_TABLE_NAME, cols, "_id = " + Long.toString(id), null, null, null, null);
     	cursor.moveToFirst();
     	return cursor.getString(1);
     }
     public void setUrl(long id, String newVal)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
     	ContentValues values = new ContentValues();
         values.put("url", newVal);  
 
 		db.update(URLSTORE_TABLE_NAME, values, "_id = " + Long.toString(id), null); 
 		db.close();
 		
 		return;    
     }
     public void setUrlExpansion(long id, String newVal)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
     	ContentValues values = new ContentValues();
         values.put("url", newVal);  
         values.put("expanded", 1);
 
 		db.update(URLSTORE_TABLE_NAME, values, "_id = " + Long.toString(id), null); 
 		db.close();
 		
 		return;    
     }
     
     /* Look up a handler list. */
     public Cursor findHandlerSet(String handlerString)
     {
     	SQLiteDatabase db = dbhelper.getReadableDatabase();
     	Cursor cursor = db.query(HANDLER_TABLE_NAME, handlerCols, "optionsKey = ?", new String[] { handlerString }, null, null, null);
     	cursor.moveToFirst();
     	
     	return cursor;
     }
     
     /* Set the default handler */
     public void setHandlerSet(String handlerString, String packageName, String name)
     {
     	SQLiteDatabase db = dbhelper.getWritableDatabase();
 
     	ContentValues values = new ContentValues();
         values.put("optionsKey", handlerString);  
         values.put("packageName", packageName);
         values.put("name", name);
 
 		db.insert(HANDLER_TABLE_NAME, "optionsKey", values);
 		db.close();
     
     }
 }
 
 
