 package com.xengine.android.data.cache;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import com.xengine.android.data.db.XDBTable;
 import com.xengine.android.data.db.XSQLiteHelper;
 
 /**
  * 内存中的数据源。
  * 基于账号分类，基于id区分的数据存储。
  * 为XBaseAdapterDataSource提供了数据库支持。
  * Created by jasontujun.
  * Date: 11-12-17
  * Time: 上午1:01
  */
 public abstract class XBaseAdapterIdUsernameDBDataSource<T>
         extends XBaseAdapterIdUsernameDataSource<T> implements XWithDatabase<T> {
 
     private XSQLiteHelper dbHelper = XSQLiteHelper.getInstance();
 
     @Override
     public XSQLiteHelper getDatabaseHelper() {
         return dbHelper;
     }
 
     @Override
     public void addToDatabase() {
         XDBTable<T> table = getDatabaseTable();
         if (!dbHelper.isTableExist(table))
             dbHelper.createTable(table);
 
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         for (int i = 0; i < size(); i++) {
             ContentValues cv = table.getContentValues(get(i));
             db.insert(table.getName(), null, cv);
         }
         db.close();
     }
 
     @Override
     public void saveToDatabase() {
         XDBTable<T> table = getDatabaseTable();
         if (dbHelper.isTableExist(table)) {
             dbHelper.dropTable(table);
         }
         dbHelper.createTable(table);
 
         SQLiteDatabase db = dbHelper.getWritableDatabase();
 
         for (int i = 0; i < size(); i++) {
             ContentValues cv = table.getContentValues(get(i));
             db.insert(table.getName(), null, cv);
         }
         db.close();
     }
 
     @Override
     public void loadFromDatabase() {
         XDBTable<T> table = getDatabaseTable();
         dbHelper.createIfNotExist(table);
         SQLiteDatabase db = dbHelper.getReadableDatabase();
 
         clear();
         Cursor cur = db.rawQuery("SELECT * FROM " + table.getName(), null);
         if (cur.moveToFirst()) {
             while (!cur.isAfterLast()) {
                 T item = table.getFilledInstance(cur);
//                add(item);// 太低效
                itemList.add(item);
                 cur.moveToNext();
             }
         }
         cur.close();
         db.close();
     }
 }
