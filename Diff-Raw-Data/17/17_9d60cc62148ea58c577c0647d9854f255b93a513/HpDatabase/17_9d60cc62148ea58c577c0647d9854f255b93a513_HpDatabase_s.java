 package com.thoughtworks.hp.datastore;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import com.thoughtworks.hp.R;
 import com.thoughtworks.hp.models.Product;
 
 public class HpDatabase extends SQLiteOpenHelper {
 
     private static final String DATABASE_NAME = "HP";
 
     private static final int DATABASE_VERSION = 1;
     private static final String TAG = "com.thoughtworks.hp.datastore.HpDatabase";
     private final Context mContext;
 
     public HpDatabase(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mContext = context;
     }
 
     @Override
     public void finalize() {
         super.close();
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         Log.e(TAG, "Creating HP database for first time");
         String[] creationSqls = mContext.getString(R.string.create_sqls).split(";");
         try {
            executeManySqlStatements(db, creationSqls);
             db.setTransactionSuccessful();
         } catch(SQLException e) {
             Log.e(TAG, "Error occurred while creating database. Error is " + e.getMessage());
         } finally {
             db.endTransaction();
         }
     }
 
    private void executeManySqlStatements(SQLiteDatabase db, String[] creationSqls) {
        for (String eachStatement : creationSqls) {
             if(eachStatement.trim().length() > 0)
                 db.execSQL(eachStatement);
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.d(TAG, "Upgrading database from version "
                 + oldVersion + " to " + newVersion
                 + ", which will destroy all old data");
         // recreate database from scratch once again.
         onCreate(db);
     }
 }
