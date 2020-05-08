 package com.samportnow.bato.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class ThoughtsDataSource
 {
 	private SQLiteDatabase mDatabase;
 	private ThoughtsSQLiteOpenHelper mHelper;
 	
 	public ThoughtsDataSource(Context context)
 	{
 		mHelper = new ThoughtsSQLiteOpenHelper(context);
 	}
 	
	public void open() throws SQLException
 	{
 		mDatabase = mHelper.getWritableDatabase();
 	}
 	
 	public void close()
 	{
 		mHelper.close();
 	}
 	
 	public long createThought(long created, String activity, int feeling, String thought, int negativeType)
 	{
 		ContentValues values = new ContentValues();
 		
 		values.put(ThoughtsSQLiteOpenHelper.COLUMN_CREATED, created);
 		values.put(ThoughtsSQLiteOpenHelper.COLUMN_ACTIVITY, activity);
 		values.put(ThoughtsSQLiteOpenHelper.COLUMN_FEELING, feeling);
 		values.put(ThoughtsSQLiteOpenHelper.COLUMN_THOUGHT, thought);
 		values.put(ThoughtsSQLiteOpenHelper.COLUMN_NEGATIVE_TYPE, negativeType);
 		
 		return mDatabase.insert(ThoughtsSQLiteOpenHelper.TABLE_THOUGHTS, null, values);
 	}
 	
 	public List<String> getActivities()
 	{
 		Cursor cursor = 
 			mDatabase.query(
 				true,
 				ThoughtsSQLiteOpenHelper.TABLE_THOUGHTS,
 				new String[] { ThoughtsSQLiteOpenHelper.COLUMN_ACTIVITY },
 				null, null, null, null,
 				ThoughtsSQLiteOpenHelper.COLUMN_ACTIVITY + " ASC",
				null, null);
 		
 		ArrayList<String> activities = new ArrayList<String>(cursor.getCount());
 		
 		while (cursor.moveToNext())
 			activities.add(cursor.getString(0));
 		
 		return activities;
 	}
 	
 	public List<String> getThoughts()
 	{
 		Cursor cursor = 
 			mDatabase.query(
 				true,
 				ThoughtsSQLiteOpenHelper.TABLE_THOUGHTS,
 				new String[] { ThoughtsSQLiteOpenHelper.COLUMN_THOUGHT },
 				null, null, null, null,
 				ThoughtsSQLiteOpenHelper.COLUMN_THOUGHT + " ASC",
				null, null);
 		
 		ArrayList<String> thoughts = new ArrayList<String>(cursor.getCount());
 		
 		while (cursor.moveToNext())
 			thoughts.add(cursor.getString(0));
 		
 		return thoughts;
 	}
 }
