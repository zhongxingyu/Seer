 package com.example.itake;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class iTakeDatabase extends SQLiteOpenHelper
 {
     class Row extends Object 
     {
         public int alarm_Id;
         public String time_data;
         public String alarm_Interval;
     }
     
     private static final String DATABASE_NAME = "iTake.db";
 
     private static final String ALARM_DATABASE_TABLE = "ALARMDATA";
 
     private static final int DATABASE_VERSION = 1;
 
     private static final String DATABASE_CREATE = "CREATE TABLE ALARMDATA ("
     	+ "alarm_id INTEGER PRIMARY KEY AUTOINCREMENT, " // Alarm ID Number
     	+ "time_data TEXT NOT NULL, " // Time for Alarm to begin
     	+ "alarm_Interval TEXT NOT NULL);"; // Alarm Interval
 
     private SQLiteDatabase db;
 
     public iTakeDatabase(Context ctx) 
     {
     	super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
     }
     
     @Override
 	public void onCreate(SQLiteDatabase db) 
 	{
 		// TODO Auto-generated method stub
 		db.execSQL(DATABASE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
 	{
 		// TODO Auto-generated method stub	
 		db = this.getWritableDatabase();
 		db.execSQL("DROP TABLE IF EXISTS " + ALARM_DATABASE_TABLE);
         onCreate(db);
         db.close();
 	}
 
     public void alarm_createRow(String time, String Interval) 
     {
     	db = this.getWritableDatabase();
         ContentValues initialValues = new ContentValues();
         initialValues.put("time_data", time);
         initialValues.put("alarm_Interval", Interval);
         db.insert(ALARM_DATABASE_TABLE, null, initialValues);
         db.close();
     }
 
     public void alarm_deleteRow(int rowId) 
     {
     	db = this.getWritableDatabase();
         db.delete(ALARM_DATABASE_TABLE, "alarm_id=" + rowId, null);
         db.close();
     }
 
     public List<Row> alarm_fetchAllRows() 
     {
         ArrayList<Row> ret = new ArrayList<Row>();
         try 
         {
             Cursor c = db.query(ALARM_DATABASE_TABLE, new String[] {"alarm_id", "time_data", "alarm_Interval"}, 
             		null, null, null, null, null);
             int numRows = c.getCount();
             c.moveToFirst();
             for (int i = 0; i < numRows; ++i) 
             {
                 Row row = new Row();
                 row.alarm_Id = c.getInt(0);
                 row.time_data = c.getString(1);
                 row.alarm_Interval = c.getString(2);
                 ret.add(row);
                 c.moveToNext();
             }
             c.close();
         } 
         catch (SQLException e) 
         {
             Log.e("Exception on query", e.toString());
         }
         return ret;
     }
 
     public Row alarm_fetchRow(int rowId) 
     {
         Row row = new Row();
         Cursor c = db.query(true, ALARM_DATABASE_TABLE, new String[] {"time_data", "alarm_Interval"},
         		"alarm_id=" + rowId, null, null, null, null, null);
         if (c.getCount() > 0) 
         {
             c.isFirst();
             row.time_data = c.getString(1);
             row.alarm_Interval = c.getString(2);
             return row;
         } 
         else 
         {
             row.time_data = null;
             row.alarm_Interval = null;
         }
         c.close();
         return row;
     }
 
     public void alarm_updateRow(int rowId, String time, String Interval) 
     {
     	db = this.getWritableDatabase();
         ContentValues args = new ContentValues();
         args.put("time_data", time);
         args.put("alarm_Interval", Interval);
         db.update(ALARM_DATABASE_TABLE, args, "alarm_id=" + rowId, null);
         db.close();
     }
  
     public Cursor alarm_GetAllRows() 
     {
         try 
         {
            return db.query(ALARM_DATABASE_TABLE, new String[] {"alarm_id", "time_data", "alarm_Interval"}, 
             		null, null, null, null, null);
         } 
         catch (SQLException e) 
         {
             Log.e("Exception on query", e.toString());
             return null;
         }
     }
 }
