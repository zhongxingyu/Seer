 package com.com.goldmanalpha.dailydo.db;
 
import android.app.Application;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
import com.goldmanalpha.dailydo.DailyDoApp;
 import com.goldmanalpha.dailydo.model.DoableBase;
 import com.goldmanalpha.dailydo.model.DoableItem;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public abstract class TableBase {
 
     final String TableName;
 
     public TableBase(String tableName) {
         TableName = tableName;
     }
 
     protected String databaseCreateSql() {
         return "create table " + TableName + " "
                 + "(id integer primary key, "
                 + "dateCreated  TIMESTAMP NOT NULL DEFAULT current_timestamp, "
                 + "dateModified  TIMESTAMP NOT NULL DEFAULT current_timestamp, ";
 
     }
 
     protected String LogTag = this.getClass().getName();
 
     protected String databaseUpgradeSql(int newVersion) {
         return null;
     }
 
     public void onCreate(SQLiteDatabase database) {
        String sql = databaseCreateSql();

        if (sql != null) {
            Log.d(LogTag, sql);
            database.execSQL(sql);
        }
     }
 
     public void onUpgrade(SQLiteDatabase database, int oldVersion,
                           int newVersion) {
 
         Log.i(this.getClass().getName(), "upgrading from version "
                 + oldVersion + " to " + newVersion
         );
 
         for (int i = oldVersion + 1; i <= newVersion; i++) {
             Log.i(LogTag, "applying version " + i);
 
             String sql = databaseUpgradeSql(i);
 
             if (sql != null) {
                 database.execSQL(sql);
             } else {
                 Log.v(LogTag, "no update");
             }
         }
 
     }
 
 }
 
