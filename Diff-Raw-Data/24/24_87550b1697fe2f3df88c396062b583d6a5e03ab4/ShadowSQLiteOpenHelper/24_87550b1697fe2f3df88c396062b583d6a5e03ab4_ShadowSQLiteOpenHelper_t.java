 package com.xtremelabs.robolectric.shadows;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 
 import com.xtremelabs.robolectric.util.Implementation;
 import com.xtremelabs.robolectric.util.Implements;
 import com.xtremelabs.robolectric.util.RealObject;
 
 /**
  * Shadow for {@code SQLiteOpenHelper}.  Provides basic support for retrieving
  * databases and partially implements the subclass contract.  (Currently,
  * support for {@code #onUpgrade} is missing).
  *
  */
 @Implements(SQLiteOpenHelper.class)
 public class ShadowSQLiteOpenHelper {
 	
 	@RealObject private SQLiteOpenHelper realHelper;
 	private static SQLiteDatabase db;
 	
 	public void __constructor__(Context context, String name,
 				CursorFactory factory, int version) {
 		// clear out static data
 		if ( db != null ) {
 			db.close();
 		}
 		db = null;
 	}
 	
 	@Implementation
 	public synchronized void close() {
 		if ( db != null ) {
 			db.close();
 		}
 		db = null;
 	}
 	
 	@Implementation
 	public synchronized SQLiteDatabase getReadableDatabase() {
 		if ( db == null ) {
			SQLiteDatabase newDB = SQLiteDatabase.openDatabase("path", null, 0);
			realHelper.onCreate( newDB );
			db = newDB;
 		}
		
		realHelper.onOpen( db );
 		return db;
 	}
 	
 	@Implementation
 	public synchronized SQLiteDatabase getWritableDatabase() {
 		if ( db == null ) {
			SQLiteDatabase newDB = SQLiteDatabase.openDatabase("path", null, 0);
			realHelper.onCreate( newDB );
			db = newDB;
 		}

		realHelper.onOpen( db );
 		return db;
 	}
 
 }
