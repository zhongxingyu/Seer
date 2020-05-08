 package framework.db;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 
 
 public class DBHelper {
 	private static final String LOG_TAG = "Framework:Db:DBhelper";
 	
 	
     public static final String KEY_ID = BaseColumns._ID;
     public static final String KEY_NAME = "name";
    
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     
     private static PreferenceUtils mPref;
    
     private static final String DATABASE_NAME = "MatrixDB";
     //private static final int DATABASE_VERSION = 1;
    
 
     private final Context mCtx;
     private boolean opened = false;
     private static DBHelper instance;
     private static DatabaseSchemaEvolution dbSchemaEvolution;
     
     public static DBHelper getInstance()
     {
     	if (instance == null)
     	{
     		throw new IllegalArgumentException("The database isnot setup. Please refer to logs for any errors");
     	}
     	
     	return instance;
     }
     
     public static void initilizeDB(final Context ctx, int oldVersion, int newVersion)
     {
     	
         instance = new DBHelper(ctx, oldVersion, newVersion);
     }
     
     public static void initilizeDB(final Context ctx, int newVersion)
     {
     	mPref = new PreferenceUtils(ctx);
     	Integer i = mPref.getInteger(ctx, "db_OldVersion") ;
     	if (i == null || i == 0)
     	{
     		instance = new DBHelper(ctx, 1, newVersion);
     		mPref.setInteger(ctx, "db_OldVersion", 1);
     	}
     	else {
     		instance = new DBHelper(ctx, i, newVersion);
     	}
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
     	final String[] mTableScripts;
         DatabaseHelper(Context context, String[] tables, int version) {
             super(context, DATABASE_NAME, null, version);
             mTableScripts = null;
             
 //            if (tables != null && tables.length > 0)
 //            {
 //            	mTableScripts = new String[tables.length];
 //            }
 //            else
 //            {
 //            	Log.e(LOG_TAG, "Unable to initilize database, no table scripts have been provided");
 //            	mTableScripts = null;
 //            }
         }
    
         public void onCreate(SQLiteDatabase db) {
         		Log.i(LOG_TAG, "-- creating new database---");
 //            	for (String table : mTableScripts)
 //            	{
 //            		try
 //            		{
 //            			db.execSQL(table);
 //            			
 //            		}catch (SQLException e)
 //            		{
 //            			Log.e(LOG_TAG, "Error in creating db table - " + table + " reason: " + e.getMessage());
 //            		}
 //            	}
 
         }
    
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             //db.execSQL("DROP TABLE IF EXISTS "+PLAYER_TABLE);
             Log.w(LOG_TAG, "-- Upgrading database---");
             //onCreate(db);
         }
     }
    
     public DBHelper(Context ctx, int oldVersion, int newVersion) {
         mCtx = ctx;
        if (oldVersion == 1 && newVersion == 1)
         {
         	//mDbHelper = new DatabaseHelper(mCtx, tableScripts, oldVersion);
         	dbSchemaEvolution = new DatabaseSchemaEvolution(ctx, DATABASE_NAME, null, oldVersion);
         }
         else if (newVersion > oldVersion)
         {
         	//mDbHelper = new DatabaseHelper(mCtx, tableScripts, oldVersion);
         	dbSchemaEvolution = new DatabaseSchemaEvolution(ctx, DATABASE_NAME, null, oldVersion);
         	//mDbHelper.onUpgrade(getDB(), oldVersion, newVersion);
         	dbSchemaEvolution.onUpgrade(getDB(), oldVersion, newVersion);
         	mPref.setInteger(ctx, "db_OldVersion", newVersion);
         }
         else if (oldVersion == newVersion)
         {
         	//mDbHelper = new DatabaseHelper(mCtx, tableScripts, oldVersion);
         	dbSchemaEvolution = new DatabaseSchemaEvolution(ctx, DATABASE_NAME, null, oldVersion);
         }
         
         else if (newVersion < oldVersion)
         {
         	throw new IllegalArgumentException("new version of database(" + newVersion +") is less than old version (" + oldVersion + ")");
         }
     }
     
 
    
     private SQLiteDatabase openDB() {
         if(!opened)
             mDb = dbSchemaEvolution.getWritableDatabase();
         opened = true;
         return mDb;
     }
     
     public SQLiteDatabase getDB() { return openDB(); }
    
     private void closeDB() {
         if(opened)
         	dbSchemaEvolution.close();
         opened = false;
        }
 }
