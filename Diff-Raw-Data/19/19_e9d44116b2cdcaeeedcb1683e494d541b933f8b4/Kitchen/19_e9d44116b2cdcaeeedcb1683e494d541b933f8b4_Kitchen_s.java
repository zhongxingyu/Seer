 package com.example.onesec;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.example.onesec.impl.cake.Cake;
 import com.example.onesec.impl.database.KitchenCakeDbHelper;
 import com.example.onesec.impl.database.KitchenContract.CakeEntry;
 import com.example.onesec.impl.database.KitchenContract.SecondEntry;
 import com.example.onesec.impl.database.KitchenSecondDbHelper;
 import com.example.onesec.impl.second.Second;
 import com.example.onesec.impl.util.Utilities;
 
 
 public class Kitchen {
 	public static Long saveSecondToLocalDb(Context context, Second second){
 		if (!second.isReadyForSave()){
 			return (long) -1;
 		}
 		
 		// Prepare the Second DB for insert
 		KitchenSecondDbHelper mDbHelper = new KitchenSecondDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = generateContentValuesForSecond(second);
 
 		// Insert the new row, returning the primary key value of the new row
 		long newRowId;
 		newRowId = db.insert(
 		         SecondEntry.TABLE_NAME,
 		         SecondEntry.COLUMN_NAME_NULLABLE,
 		         values);
 		// note: COLUMN_NAME_NULLABLE=null means a row won't be inserted when there are no data values
 		
 		return newRowId;
 	}
 	
 	public static Long saveCakeToLocalDb(Context context, Cake cake){
 		if (!cake.isReadyForSave()){
 			return (long) -1;
 		}
 		
 		// Prepare the Cake DB for insert
 		KitchenCakeDbHelper mDbHelper = new KitchenCakeDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = generateContentValuesForCake(cake);
 
 		// Insert the new row, returning the primary key value of the new row
 		long newRowId;
 		newRowId = db.insert(
 		         CakeEntry.TABLE_NAME,
 		         CakeEntry.COLUMN_NAME_NULLABLE,
 		         values);
 		// note: COLUMN_NAME_NULLABLE=null means a row won't be inserted when there are no data values
 		
 		return newRowId;
 	}
 	
 	private static ContentValues generateContentValuesForSecond(Second second){
 		
 		ContentValues values = new ContentValues();
 		values.put(SecondEntry.COLUMN_NAME_SECOND_ID, second.getId());
 		values.put(SecondEntry.COLUMN_NAME_DATE, Utilities.dateToString(second.getDate()));
 		values.put(SecondEntry.COLUMN_NAME_VIDEO_PATH, second.getVideoUri().getPath());
 		values.put(SecondEntry.COLUMN_NAME_THUMBNAIL_PATH, second.getThumbnailUri().getPath());
 		return values;
 	}
 	
 	private static ContentValues generateContentValuesForCake(Cake cake){
 		ContentValues values = new ContentValues();
 		values.put(CakeEntry.COLUMN_NAME_CAKE_ID, cake.getId());
 		values.put(CakeEntry.COLUMN_NAME_TITLE, cake.getTitle());
 		values.put(CakeEntry.COLUMN_NAME_DATE, Utilities.dateToString(cake.getDate()));
 		values.put(CakeEntry.COLUMN_NAME_VIDEO_PATH, cake.getVideoUri().getPath());
 		values.put(CakeEntry.COLUMN_NAME_THUMBNAIL_PATH, cake.getThumbnailUri().getPath());
 		values.put(CakeEntry.COLUMN_NAME_BATTER_PATH, cake.getBatterUri().getPath());
 		return values;
 	}
 	
 	public static Second getSecondById(Context context, Long rowId){
 		KitchenSecondDbHelper mDbHelper = new KitchenSecondDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = {
 			SecondEntry._ID,
 		    SecondEntry.COLUMN_NAME_SECOND_ID,
 		    SecondEntry.COLUMN_NAME_DATE,
 		    SecondEntry.COLUMN_NAME_VIDEO_PATH,
 		    SecondEntry.COLUMN_NAME_THUMBNAIL_PATH
 		    };
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder = null;
//		    SecondEntry.COLUMN_NAME_UPDATED + " DESC";
 
 		Cursor c = db.query(
		    SecondEntry.TABLE_NAME,  // The table to query
 		    projection,                               // The columns to return
		    SecondEntry._ID+"=?",		  // The columns for the WHERE clause
		    new String[]{ Long.toString(rowId) },                         		   // The values for the WHERE clause
 		    null,                                     // don't group the rows
 		    null,                                     // don't filter by row groups
 		    sortOrder                                 // The sort order
 		    );
 		
 		if (c.moveToFirst()) {
 			return new Second(c);
 		}
 		return null;
 	}
 	
 	public static Second getSecondByUid(Context context, String uid){
 		KitchenSecondDbHelper mDbHelper = new KitchenSecondDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = {
 			SecondEntry._ID,
 		    SecondEntry.COLUMN_NAME_SECOND_ID,
 		    SecondEntry.COLUMN_NAME_DATE,
 		    SecondEntry.COLUMN_NAME_VIDEO_PATH,
 		    SecondEntry.COLUMN_NAME_THUMBNAIL_PATH
 		    };
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder = null;
 //		    SecondEntry.COLUMN_NAME_UPDATED + " DESC";
 
 		Cursor c = db.query(
 		    SecondEntry.TABLE_NAME,  // The table to query
 		    projection,                               // The columns to return
 		    SecondEntry.COLUMN_NAME_SECOND_ID+"=?",		  // The columns for the WHERE clause
 		    new String[]{ uid },                         		   // The values for the WHERE clause
 		    null,                                     // don't group the rows
 		    null,                                     // don't filter by row groups
 		    sortOrder                                 // The sort order
 		    );
 		
 		if (c.moveToFirst()) {
 			Log.v("getsecbyuid", "new second(c)");
 			return new Second(c);
 		}
 		Log.v("getsecbyuid", "null");
 		return null;
 	}
 	
 	
	
 	public static Cursor getSecondsCursor(Context context) {
 		KitchenSecondDbHelper mDbHelper = new KitchenSecondDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = {
 			SecondEntry._ID,
 		    SecondEntry.COLUMN_NAME_SECOND_ID,
 		    SecondEntry.COLUMN_NAME_DATE,
 		    SecondEntry.COLUMN_NAME_VIDEO_PATH,
 		    SecondEntry.COLUMN_NAME_THUMBNAIL_PATH
 		    };
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder = SecondEntry._ID + " DESC";
 		
 		Cursor c = db.query(
 			    SecondEntry.TABLE_NAME,  	// The table to query
 			    projection,				 	// The columns to return
 			    null,		  			 	// The columns for the WHERE clause
 			    null,					 	// The values for the WHERE clause
 			    null,					 	// don't group the rows
 			    null,					 	// don't filter by row groups
 			    sortOrder				 	// The sort order
 			    );
 		
 		return c;
 	}
 	
 	public static Cursor getCakesCursor(Context context) {
 		KitchenCakeDbHelper mDbHelper = new KitchenCakeDbHelper(context);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = {
 			CakeEntry._ID,
 		    CakeEntry.COLUMN_NAME_CAKE_ID,
 		    CakeEntry.COLUMN_NAME_TITLE,
 		    CakeEntry.COLUMN_NAME_DATE,
 		    CakeEntry.COLUMN_NAME_VIDEO_PATH,
 		    CakeEntry.COLUMN_NAME_THUMBNAIL_PATH
 		    };
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder = CakeEntry._ID + " DESC";
 		
 		Cursor c = db.query(
 			    CakeEntry.TABLE_NAME,  		// The table to query
 			    projection,                 // The columns to return
 			    null,		  				// The columns for the WHERE clause
 			    null,                       // The values for the WHERE clause
 			    null,						// don't group the rows
 			    null,						// don't filter by row groups
 			    sortOrder					// The sort order
 			    );
 		
 		return c;
 	}
 }
