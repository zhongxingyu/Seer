 
 package foss.jonasl.svtplayer;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DB {
 
     private static final String DB_NAME = "svtplayer.db";
     private static final int DB_VERSION = 1;
     private static final String TABLE_PVR = "pvr";
 
     public static final String PVR_ID = "id";
     public static final String PVR_URL = "url";
     public static final String PVR_TITLE = "title";
     public static final String PVR_DATE = "date";
     public static final String PVR_ADDED = "added";
     public static final String PVR_WIDTH = "width";
     public static final String PVR_HEIGHT = "height";
     public static final String PVR_BITRATE = "bitrate";
     public static final String PVR_LENGTH = "length";
     public static final String PVR_STATE = "state";
     public static final String PVR_RATE = "rate";
     public static final String PVR_PROGRESS = "progress";
 
     public static final int PVR_STATE_ERROR_FATAL = -3;
     public static final int PVR_STATE_ERROR_SPACE = -2;
     public static final int PVR_STATE_ERROR_RETRYING = -1;
     public static final int PVR_STATE_PAUSED = 0;
     public static final int PVR_STATE_PAUSING = 1;
     public static final int PVR_STATE_PENDING = 2;    
     public static final int PVR_STATE_RECORDING = 3;
     public static final int PVR_STATE_REMUXING = 4;
     public static final int PVR_STATE_DELETED = 99;
 
     private static DB sInstance = null;
 
     private SQLiteDatabase mDb = null;
 
     public static synchronized DB instance() {
         return sInstance;
     }
 
     public static synchronized void init(Context context) {
         sInstance = new DB(context);
     }
 
     private DB(Context context) {
         OpenHelper openHelper = new OpenHelper(context);
         this.mDb = openHelper.getWritableDatabase();
     }
 
     public static String getPVRStateName(int state) {
         switch (state) {
             case PVR_STATE_ERROR_FATAL:
                 return "ERROR_FATAL";
             case PVR_STATE_ERROR_SPACE:
                 return "ERROR_SPACE";
             case PVR_STATE_ERROR_RETRYING:
                 return "ERROR_RETRYING";
             case PVR_STATE_PENDING:
                 return "PENDING";
             case PVR_STATE_PAUSING:
                 return "PAUSING";
             case PVR_STATE_PAUSED:
                 return "PAUSED";
             case PVR_STATE_RECORDING:
                 return "RECORDING";
             case PVR_STATE_REMUXING:
                 return "REMUXING";
             default:
                 return "!!UNKNOWN!!";
         }
     }
 
     public boolean pvrDelete(long id) {
         boolean res = mDb.delete(TABLE_PVR, PVR_ID + "=" + id, null) > 0;
         if (res) {
             L.d(id + " deleted");
         }
         return res;
     }
 
     public long pvrInsert(String url, String title, long date, int width, int height, int bitrate,
             int length) {
         ContentValues vals = new ContentValues();
         vals.put(PVR_URL, url);
         vals.put(PVR_TITLE, title);
         vals.put(PVR_DATE, date);
         vals.put(PVR_ADDED, new Date().getTime());
         vals.put(PVR_WIDTH, width);
         vals.put(PVR_HEIGHT, height);
         vals.put(PVR_BITRATE, bitrate);
         vals.put(PVR_LENGTH, length);
         vals.put(PVR_STATE, PVR_STATE_PENDING);
         vals.put(PVR_RATE, 0);
         vals.put(PVR_PROGRESS, 0);
 
         long res = mDb.insert(TABLE_PVR, null, vals);
         if (res >= 0) {
             pvrInsterted(res);
         }
         return res;
     }
 
     public boolean pvrUpdateState(long id, int state) {
         ContentValues args = new ContentValues();
         args.put(PVR_STATE, state);
         boolean res = mDb.update(TABLE_PVR, args, PVR_ID + "=" + id, null) > 0;
         if (res) {
             pvrUpdated(id);
         }
         return res;
     }
 
     public boolean pvrUpdateRateProgress(long id, int rate, int progress) {
         ContentValues args = new ContentValues();
         args.put(PVR_RATE, rate);
         args.put(PVR_PROGRESS, progress);
         boolean res = mDb.update(TABLE_PVR, args, PVR_ID + "=" + id, null) > 0;
         if (res) {
             pvrUpdated(id);
         }
         return res;
     }
 
     public Cursor pvrGetEntry(long id) {
         Cursor cur = mDb.query(true, TABLE_PVR, new String[] {
                 PVR_ID, PVR_URL, PVR_TITLE, PVR_DATE, PVR_ADDED, PVR_WIDTH, PVR_HEIGHT,
                 PVR_BITRATE, PVR_LENGTH, PVR_STATE, PVR_RATE, PVR_PROGRESS
         }, PVR_ID + "=" + id, null, null, null, null, null);
         return cur;
     }
 
     public Cursor pvrGetEntries() {
         Cursor cur = mDb.query(true, TABLE_PVR, new String[] {
                 PVR_ID, PVR_URL, PVR_TITLE, PVR_DATE, PVR_ADDED, PVR_WIDTH, PVR_HEIGHT,
                 PVR_BITRATE, PVR_LENGTH, PVR_STATE, PVR_RATE, PVR_PROGRESS
         }, null, null, null, null, PVR_ID + " asc", null);
         return cur;
     }
 
     private void pvrUpdated(long id) {
         dumpPVR(id, "U");
     }
 
     private void pvrInsterted(long id) {
         dumpPVR(id, "I");
     }
 
     private void dumpPVR(long id, String prefix) {
         Cursor cur = pvrGetEntry(id);
         cur.moveToFirst();
         StringBuilder b = new StringBuilder();
         b.append(prefix);
         b.append("");
         b.append(" id: " + cur.getString(cur.getColumnIndexOrThrow(DB.PVR_ID)));
         b.append(" title: " + cur.getString(cur.getColumnIndexOrThrow(DB.PVR_TITLE)));
         b.append(" state: " + getPVRStateName(cur.getInt(cur.getColumnIndexOrThrow(DB.PVR_STATE))));
         b.append(" rate: " + cur.getString(cur.getColumnIndexOrThrow(DB.PVR_RATE)));
         b.append(" progress: " + cur.getString(cur.getColumnIndexOrThrow(DB.PVR_PROGRESS)));
         L.d (b.toString());
     }
 
     private static class OpenHelper extends SQLiteOpenHelper {
         OpenHelper(Context context) {
             super(context, DB_NAME, null, DB_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             StringBuilder b = new StringBuilder();
             b.append("CREATE TABLE ");
             b.append(TABLE_PVR);
             b.append(" (");
             b.append(PVR_ID + " INTEGER PRIMARY KEY, ");
             b.append(PVR_URL + " TEXT NOT NULL, ");
             b.append(PVR_TITLE + " TEXT NOT NULL, ");
             b.append(PVR_DATE + " INTEGER NOT NULL, ");
             b.append(PVR_ADDED + " INTEGER NOT NULL, ");
             b.append(PVR_WIDTH + " INTEGER NOT NULL, ");
             b.append(PVR_HEIGHT + " INTEGER NOT NULL, ");
             b.append(PVR_BITRATE + " INTEGER NOT NULL, ");
             b.append(PVR_LENGTH + " INTEGER NOT NULL, ");
             b.append(PVR_STATE + " INTEGER NOT NULL, ");
             b.append(PVR_RATE + " INTEGER NOT NULL, ");
             b.append(PVR_PROGRESS + " INTEGER NOT NULL");
             b.append(")");
             db.execSQL(b.toString());
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_PVR);
             onCreate(db);
         }
     }
 }
