 package ru.kor_inc.andy;
  
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import java.sql.*;
 import android.util.*;
 import java.text.*;
  
  
 public class DbTool{
  
  private static final String TAG = "kor_ka Log";
  
  DBHelper dbHelper;
  ContentValues cv;
  final int DB_VERSION = 2;
   
   
     public void DataToLog(String time, String date, String numeric, String telo){
 //Made to test my ability to create classes and send data between them 
         Log.d(TAG, time+" | "+ date +" | "+numeric+" | "+ telo);
     }
   
     public void WriteToSql(String currentTable, String time, String date, String numeric, String telo, Context context){
      dbHelper = new DBHelper(context);
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      cv = new ContentValues();
       
      cv.put("time",time);
      cv.put("date",date);
      cv.put("numeric",numeric);
      cv.put("telo",telo);
       
      db.insert(currentTable, null, cv);
      db.close();
     }
      
      public void delRec(String currentTable, long id, Context context) {
         DBHelper dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         db.delete(currentTable, "_id" + " = " + id, null);
         db.close();
         //просто мне нужен push!!
      }
       
     public void ReadFromSqlToLog(String currentTable, Context context){
         dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Cursor c = db.query(currentTable, null, null, null, null, null, null);
  
 // ставим позицию курсора на первую строку выборки 
         // если в выборке нет строк, вернется false 
         if (c.moveToFirst()) {
  
 // определяем номера столбцов по имени в выборке 
             int idColIndex = c.getColumnIndex("id");
             int dateColIndex = c.getColumnIndex("date");
             int numericColIndex = c.getColumnIndex("numeric");
             int timeColIndex = c.getColumnIndex("time");
             int teloColIndex = c.getColumnIndex("telo");
          
             do { 
                 // получаем значения по номерам столбцов и пишем все в лог
  
                 Log.d(TAG, "ID = " + c.getInt(idColIndex) + " | " + c.getString(timeColIndex) + " | " + c.getString(dateColIndex)+" | " + c.getString(numericColIndex)+" | " + c.getString(teloColIndex)); 
                 // переход на следующую строку 
                 // а если следующей нет (текущая -последняя), то false - выходим из цикла
             } while (c.moveToNext());
          
             c.close();
             db.close();
         }else{
             Log.d(TAG, "как то пусто в твоей таблице...");
             c.close();
             db.close();
         }
     }
  
  
      
     public Cursor getCursor(String currentTable,Context context){
         dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Cursor c = db.query(currentTable, null, null, null, null, null, null); 
         db.close();
         return c;
     }
      
     public Cursor getCursorFilterByDate(String currentTable,Context context, String dateFrom, String dateTo){
         dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Cursor c = db.query(currentTable, null, "date BETWEEN ? AND ?", new String[] {
                                 dateFrom, dateTo }, null, null, null);     
         db.close();
         return c;
     }
      
     public Cursor getCursorWithGroupBy(String currentTable, Context context, String groupBy ) {
         dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Cursor c = db.query(currentTable, null, null, null, groupBy, null, null);
         db.close();
         return c;
          
     }
      
     public Cursor getCursorWithGroupByAndSum(String currentTable, Context context, String groupBy, String columnToSum) {
         dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         //fix dates (add "zero" dates)
         //TODO get dateFrom and dateTo from calling activity (from it's filter)
         db.delete("dateFix",null,null);
         ContentValues cvDateFix = new ContentValues();
       	Date d = new Date(System.currentTimeMillis());
      	Date dateFrom = d.vauleOf("01-01-2013");
       	Date dateTo = d.valueOf("31-12-2013");
       	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
       	do {
 		String dateFix = sdf.format(dateFrom);
      		cvDateFix.put("date",dateFix);
      		cvDateFix.put("numeric", 0);
      		db.insert("dateFix", null, cvDateFix);
      		dateFrom.setDate(dateFrom.getDate()+1);
       	} while(dateFrom.getDate() < dateTo.getDate());
      	
         Cursor c = db.query(currentTable + " INNER JOIN dateFix ON " +currentTable+".date=dateFix.date "+currentTable+".numeric=daFix.numeric" , new String[] {groupBy, "sum("+columnToSum+") as "+columnToSum }, null, null, groupBy, null, columnToSum+" DESC");
      	db.close();
         return c;
          
     }
  
     public void clear (String currentTable, Context context){
         DBHelper dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         db.delete(currentTable,null,null);
         Log.d(TAG, "ты убил их! как ты мог :0");
         db.close();
     } 
      
     public void update (String currentTable, Context context, String idUpd, ContentValues cvUpd){
         DBHelper dbHelper = new DBHelper(context);
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         db.update(currentTable, cvUpd, "_id = ?", new String[] { idUpd });
         Log.d(TAG, "ты убил их! как ты мог :0");
         db.close();
     } 
  
      
     class DBHelper extends SQLiteOpenHelper{
  
         public DBHelper(Context context){ 
         // конструктор суперкласса 
         super(context, "firstAnyDynamicDataTable", null, DB_VERSION); }
  
         @Override
         public void onCreate(SQLiteDatabase db){
         Log.d(TAG, "--- onCreate database ---");
         // создаем таблицу с полями 
         db.execSQL("create table firstAnyDynamicDataTable (" + "_id integer primary key autoincrement," + "time text," + "date date," + "numeric text," + "telo text" + ");");
         db.execSQL("create table secondAnyDynamicDataTable (" + "_id integer primary key autoincrement," + "time text," + "date date," + "numeric text," + "telo text" + ");");              
         db.execSQL("create table dateFix (" + "date date," + "numeric text" + ");");                 
  
         }
  
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
              if (oldVersion == 1 && newVersion == 2) {
                  db.execSQL("create table secondAnyDynamicDataTable (" + "_id integer primary key autoincrement," + "time text," + "date date," + "numeric text," + "telo text" + ");");                 
              }
         } 
     }
  
      
 }
