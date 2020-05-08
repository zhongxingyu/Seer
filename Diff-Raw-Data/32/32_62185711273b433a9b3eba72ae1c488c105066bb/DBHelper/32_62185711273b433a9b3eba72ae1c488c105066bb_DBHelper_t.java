 package com.example.android_ab;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.text.TextUtils;
 import android.util.Log;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 final class DBHelper extends SQLiteOpenHelper {
     private static final String TAG = "DBHelper";
     private static final String DATABASE_FILE_NAME = "android_ab.db";
     private static final int DATABASE_VERSION = 1;
 
     DBHelper(Context context) {
         super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(SQL_CREATE_EXPERIMENTS_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.v(TAG, String.format("Upgrading from version %d to %d",
                 oldVersion, newVersion) );
         db.execSQL(SQL_DROP_EXPERIMENTS_TABLE);
         onCreate(db);
     }
 
     @Override
     public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.v(TAG, String.format("Downgrading from version %d to %d",
                 oldVersion, newVersion) );
         onUpgrade(db, oldVersion, newVersion);
     }
 
     private static final String TABLE_NAME = "experiments";
     private static final String COLUMN_ID = "_id";
     private static final String COLUMN_EXPERIMENT_KEY = "experiment_key";
     private static final String COLUMN_BUCKET_NAME = "bucket_name";
     private static final String COLUMN_VERSION = "version";
     private static final String COLUMN_EXPIRATION_DATE = "expiration_date";
     private static final String SQL_CREATE_EXPERIMENTS_TABLE = String.format(
             "CREATE TABLE %s ("
                    +"%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +"%s TEXT UNIQUE NOT NULL, "
                    +"%s TEXT, "
                    +"%s TEXT, "
                     +"%s TEXT);",
             TABLE_NAME, COLUMN_ID, COLUMN_EXPERIMENT_KEY,
             COLUMN_BUCKET_NAME, COLUMN_VERSION, COLUMN_EXPIRATION_DATE);
     private static final String SQL_DROP_EXPERIMENTS_TABLE = String.format(
             "DROP TABLE IF EXISTS %s;", TABLE_NAME);
     private static final String SQL_DELETE_ALL_EXPERIMENTS = String.format(
             "DELETE FROM %s;", TABLE_NAME);
     private static final String SQL_INSERT_EXPERIMENTS = String.format(
            "INSERT INTO %s (%s, %s, %s, %s) VALUES ('%%s', '%%s', '%%s', '%%s');",
             TABLE_NAME, COLUMN_EXPERIMENT_KEY,
             COLUMN_BUCKET_NAME, COLUMN_VERSION, COLUMN_EXPIRATION_DATE);
     private static final String SQL_SELECT_EXPERIMENT = String.format(
            "SELECT * FROM %s WHERE %s = '%%s' AND %s >= date('now');",
             TABLE_NAME, COLUMN_EXPERIMENT_KEY, COLUMN_EXPIRATION_DATE);
 
     public synchronized void removeAllExperiments() {
         final SQLiteDatabase db = getWritableDatabase();
         db.execSQL(SQL_DELETE_ALL_EXPERIMENTS);
     }
 
    public synchronized void addExperiments(JSONArray experiments) {
         JSONObject experiment;
         String[] queries = new String[experiments.length()];
         for(int i = 0; i < experiments.length(); i++){
             try {
                 experiment = experiments.getJSONObject(i);
                 queries[i] = String.format(SQL_INSERT_EXPERIMENTS,
                         experiment.getString("experiment_key"),
                         experiment.getString("bucket_name"),
                         experiment.getString("version"),
                         experiment.getString("expiration_date"));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
         final SQLiteDatabase db = getWritableDatabase();
        String query = TextUtils.join(";", queries);
         db.execSQL(query);
     }
 
     public synchronized Experiment getExperiment(String experimentKey) {
         final SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format(SQL_SELECT_EXPERIMENT, experimentKey), new String[]{});
         Experiment experiment = null;
         cursor.moveToFirst();
         if(cursor.getCount() > 0){
             experiment = new Experiment();
             experiment.mExperimentKey = cursor.getString(cursor.getColumnIndex(COLUMN_EXPERIMENT_KEY));
             experiment.mBucketName = cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_NAME));
             experiment.mVersion = cursor.getString(cursor.getColumnIndex(COLUMN_VERSION));
             experiment.mExpirationDate = cursor.getString(cursor.getColumnIndex(COLUMN_EXPIRATION_DATE));
         }
         return experiment;
     }
 }
