 package nl.rnplus.olv.data;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Database currently holds logs and notifications.
  * 
  * Log database by: Xperimental
  * Notification database by: Renze Nicolai
  */
 public class LiveViewDbHelper extends SQLiteOpenHelper
 {
 
     private static final String DB_NAME = "liveview.db";
 
     private static final int DB_VERSION = 2;
 
     public LiveViewDbHelper(Context context)
     {
         super(context, DB_NAME, null, DB_VERSION);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
      * .SQLiteDatabase)
      */
     @Override
     public void onCreate(SQLiteDatabase db)
     {
         db.execSQL(LiveViewData.Log.SCHEMA);
         db.execSQL(LiveViewData.Notifications.SCHEMA);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
      * .SQLiteDatabase, int, int)
      */
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {
         if (oldVersion != DB_VERSION)
         {
        	try {
	            db.execSQL("DROP TABLE " + LiveViewData.Log.TABLE);
	            db.execSQL("DROP TABLE " + LiveViewData.Notifications.TABLE);
        	} catch(Exception e) {
        		Log.e("LiveViewDbHelper", "Could not delete all tables. "+e.toString());
        	}
             onCreate(db);
         }
     }
 
     public static void logMessage(Context context, String message)
     {
         LiveViewDbHelper helper = new LiveViewDbHelper(context);
         SQLiteDatabase db = helper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(LiveViewData.Log.TIMESTAMP, System.currentTimeMillis());
         values.put(LiveViewData.Log.MESSAGE, message);
         db.insert(LiveViewData.Log.TABLE, LiveViewData.Log._ID, values);
         db.close();
     }
     
     public static void addNotification(Context context, String title, String content, Integer type, Long timestamp)
     {
     	Log.w("DEBUG", "addNotification.");
         LiveViewDbHelper helper = new LiveViewDbHelper(context);
         SQLiteDatabase db = helper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(LiveViewData.Notifications.TIMESTAMP, timestamp);
         values.put(LiveViewData.Notifications.TITLE, title);
         values.put(LiveViewData.Notifications.CONTENT, content);
         values.put(LiveViewData.Notifications.TYPE, type);
         values.put(LiveViewData.Notifications.READ, 0);
         db.insert(LiveViewData.Notifications.TABLE, LiveViewData.Notifications._ID, values);
         db.close();
     }
     
     public static void setNotificationRead(Context context, Integer id)
     {
     	Log.w("DEBUG", "setNotificationRead");
         LiveViewDbHelper helper = new LiveViewDbHelper(context);
         SQLiteDatabase db = helper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(LiveViewData.Notifications.READ, 1);
         db.update(LiveViewData.Notifications.TABLE,  values, LiveViewData.Notifications._ID + "="+id, null);
         db.close();
     }
     
     public static void setAllNotificationsRead(Context context)
     {
     	Log.w("DEBUG", "setAllNotificationsRead");
         LiveViewDbHelper helper = new LiveViewDbHelper(context);
         SQLiteDatabase db = helper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(LiveViewData.Notifications.READ, 1);
         db.update(LiveViewData.Notifications.TABLE,  values, "1", null);
         db.close();
     }
     
 	public static Cursor getAllNotifications(Context context)
 	    {
 			Log.w("DEBUG", "getAllNotifications");
 	    	LiveViewDbHelper helper = new LiveViewDbHelper(context);
 	    	SQLiteDatabase db = helper.getReadableDatabase();
 	    	Cursor cur=db.rawQuery("SELECT * FROM "+LiveViewData.Notifications.TABLE+" ORDER BY "+LiveViewData.Notifications._ID+" DESC",new String [] {});    
 	    	//db.close();
 	     return cur;
 	    }
 	
 	public static Cursor deleteAllNotifications(Context context)
     {
 		Log.w("DEBUG", "deleteAllNotifications");
     	LiveViewDbHelper helper = new LiveViewDbHelper(context);
     	SQLiteDatabase db = helper.getWritableDatabase();  
     	db.delete(LiveViewData.Notifications.TABLE, null, null);
     	db.close();
      return null;
     }
 
 }
