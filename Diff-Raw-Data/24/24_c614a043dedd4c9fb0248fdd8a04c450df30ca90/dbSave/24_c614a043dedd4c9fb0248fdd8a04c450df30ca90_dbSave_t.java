 package com.Litterfeldt.AStory.dbConnector;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.Litterfeldt.AStory.models.SaveState;
 
 public class dbSave {
     public static void setSave(Context c,SaveState s){
         dbConnector.getInstance(c).write().execSQL("DELETE * FROM "+dbConnector.TABLE_NAME_SAVED_STATE+";");
         ContentValues initialValues = new ContentValues();
         initialValues.put(dbConnector.COLUMN_SAVED_STATE_BOOK_ID,s.bookId());
         initialValues.put(dbConnector.COLUMN_SAVED_STATE_CHAPTER_ID,s.chapterId());
         initialValues.put(dbConnector.COLUMN_SAVED_STATE_CURRENT_TIME_POS,s.time_pos());
         dbConnector.getInstance(c).write().insert(dbConnector.TABLE_NAME_BOOK_IMG, null, initialValues);
 
     }
     public static SaveState getSave(Context c){
        Cursor cursor = dbConnector.getInstance(c).read().query(dbConnector.TABLE_NAME_SAVED_STATE,
                 new String[]{dbConnector.COLUMN_SAVED_STATE_BOOK_ID,
                         dbConnector.COLUMN_SAVED_STATE_CHAPTER_ID,
                         dbConnector.COLUMN_SAVED_STATE_CURRENT_TIME_POS},
                 null,null,null,null,null);
         boolean notEmpty = cursor.moveToLast();
         if (notEmpty){
             return new SaveState(cursor.getInt(0),cursor.getInt(1),cursor.getInt(2));
         }else{
             return null;
         }
     }
 }
