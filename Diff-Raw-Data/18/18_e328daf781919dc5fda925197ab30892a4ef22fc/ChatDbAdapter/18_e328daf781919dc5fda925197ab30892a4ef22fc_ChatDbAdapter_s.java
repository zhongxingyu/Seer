 package pl.edu.agh.mobile.adhoccom;
 
import pl.edu.agh.mobile.adhoccom.chatMessage.Message;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class ChatDbAdapter {
 	private static final String DB_NAME = "adhoccommdb";
 	private static final String MSG_TABLE_NAME = "messages";
 	private static final int DB_VERSION = 3;
 	private static final String DB_CREATE_QUERY = 
 		"create table messages (_id integer primary key autoincrement, " +
         "sender text not null, body text not null, date integer not null, chatgroup text not null);";
 	private static final String TAG = "ChatDbAdapter";
 	
 	public static final String ID_COLLUMN = "_id";
 	public static final String SENDER_COLLUMN = "sender";
 	public static final String BODY_COLLUMN = "body";
 	public static final String DATE_COLLUMN = "date";
 	public static final String GROUP_COLUMN = "chatgroup";
 	
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     private Context mCtx;
 	
 	private class DatabaseHelper extends SQLiteOpenHelper {
 
 		public DatabaseHelper(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DB_CREATE_QUERY);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS messages");
             onCreate(db);
 		}
 	}
 	
     public ChatDbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
     
     public ChatDbAdapter open() {
     	mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
     
 	public void close() {
 		mDbHelper.close();
 	}
     
 	public Cursor fetchAllMessages() throws SQLException {
 		return mDb.query(MSG_TABLE_NAME, new String[] {ID_COLLUMN, SENDER_COLLUMN,
                 BODY_COLLUMN}, null, null, null, null, ID_COLLUMN);
 	}
 	
 	public Cursor fetchMessages(int limit) throws SQLException {
 		return mDb.query(MSG_TABLE_NAME, new String[] {ID_COLLUMN, SENDER_COLLUMN,
                 BODY_COLLUMN}, null, null, null, null, ID_COLLUMN + " DESC", Integer.toString(limit));
 	}
 	
 	public Cursor featchMessages(int limit, String group) {
 		// TODO sort by date
 		return mDb.query(MSG_TABLE_NAME, 
 				new String[] {ID_COLLUMN, SENDER_COLLUMN, BODY_COLLUMN, DATE_COLLUMN}, 
 				"chatgroup = ?", 
 				new String[]{group}, 
 				null, 
 				null, 
 				ID_COLLUMN + " DESC", 
 				Integer.toString(limit));
 	}
 	
 	public Cursor fetchMessage(long id) {
 		Cursor mCursor =
             mDb.query(true, MSG_TABLE_NAME, new String[] {ID_COLLUMN,
                     SENDER_COLLUMN, BODY_COLLUMN, DATE_COLLUMN, GROUP_COLUMN}, ID_COLLUMN + "=" + id, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 	}
 	
 	public long addNewMessage(Message msg) {
 		ContentValues initialValues = new ContentValues();
         initialValues.put(SENDER_COLLUMN, msg.getSender());
         initialValues.put(BODY_COLLUMN, msg.getBody());
         initialValues.put(GROUP_COLUMN, msg.getGroupName());
         initialValues.put(DATE_COLLUMN, msg.getDate());
 
         return mDb.insert(MSG_TABLE_NAME, null, initialValues);
 	}
 }
