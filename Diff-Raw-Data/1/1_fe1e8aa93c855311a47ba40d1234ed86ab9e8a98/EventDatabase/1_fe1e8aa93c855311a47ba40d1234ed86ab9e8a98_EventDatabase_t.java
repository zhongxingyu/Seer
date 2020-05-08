 /* 
 Copyright 2012 Javran Cheng
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package org.evswork.whatsdonetoday;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class EventDatabase {
 	private Context mContext;
 	private EventDatabaseOpenHelper mDatabaseOpenHelper;
 	private SQLiteDatabase dbReadable = null, dbWritable = null;
 	
 	EventDatabase(Context ctxt) {
 		mContext = ctxt;
 		mDatabaseOpenHelper = new EventDatabaseOpenHelper(mContext);
 	}
 	
 	public SQLiteDatabase getDatabase (boolean writable) {
 		if (writable) {
 			if (dbWritable == null)
 				dbWritable = mDatabaseOpenHelper.getWritableDatabase();
 			return dbWritable;
 		} else {
 			if (dbReadable == null)
 				dbReadable = mDatabaseOpenHelper.getReadableDatabase();
 			return dbReadable;
 		}
 	}
 	
 	public synchronized void close() {
 		if (dbWritable != null)
 			dbWritable.close();
 		if (dbReadable != null)
 			dbReadable.close();
 		mDatabaseOpenHelper.close();
 	}
 	
 	public void insertEvent(Event event) {
 		String worthStr = String.valueOf(event.worth);
 		String dateTimeStr = event.dateTime.toString();
 		SQLiteDatabase db = getDatabase(true);
 		db.execSQL("INSERT INTO Event VALUES(NULL, ? , ? , ? )", 
 			new String []{ event.description, worthStr, dateTimeStr });
 		
 		// retrieve last id
 		Cursor cur = db.rawQuery("SELECT last_insert_rowid()", null);
 		cur.moveToFirst();
 		String eventId = cur.getString(0);
 		cur.close();
 		
 		// insert tags;
 		for (Tag t : event.tags) {
 			db.execSQL("INSERT INTO RelEventTag VALUES(?, ?) ",
 				new String [] { eventId, String.valueOf(t.id) } );
 		}
 	}
 	
 	public void updateEvent(Event event) {
 		String worthStr = String.valueOf(event.worth);
 		String dateTimeStr = event.dateTime.toString();
 		String idStr = String.valueOf(event.eventId);
 		SQLiteDatabase db = getDatabase(true);
 		db.execSQL("UPDATE Event SET description = ?, worth = ?, time = ? WHERE _id == ? ",
 			new String [] { event.description, worthStr, dateTimeStr, idStr});
 		
 		// remove all tags
 		db.execSQL("DELETE FROM RelEventTag WHERE event_id == ? ", new String[] {idStr});
 		// insert tags;
 		for (Tag t : event.tags) {
 			db.execSQL("INSERT INTO RelEventTag VALUES(?, ?) ",
 				new String [] { idStr , String.valueOf(t.id) } );
 		}
 	}
 	
 	public void removeEventById(long eventId) {
 		SQLiteDatabase db = getDatabase(true);
 		String idStr = String.valueOf(eventId);
 		// remove all tags
 		db.execSQL("DELETE FROM RelEventTag WHERE event_id == ? ", new String[] {idStr});
 		// remove event
 		db.execSQL("DELETE FROM Event WHERE _id == ? ", new String[] {idStr});
 		
 	}
 	
 	public void removeTagById(long tagId) {
 		SQLiteDatabase db = getDatabase(true);
 		String idStr = String.valueOf(tagId);
 		String [] arg = new String [] {idStr};
 		// remove all tags
 		db.execSQL("DELETE FROM RelEventTag WHERE tag_id == ? ", arg);
 		// remove tag_id if exists in welcometag
 		db.execSQL("DELETE FROM WelcomeTag WHERE tag_id == ? ", arg);
 		// remove event
 		db.execSQL("DELETE FROM Tag WHERE _id == ? ", arg);
 	}
 	
 	public void addNewTag(String tagName) {
 		getDatabase(true).execSQL("INSERT INTO Tag VALUES (NULL, ? ) ", new String[] {tagName});
 	}
 	
 
 	public void modifyTagById(long id, String tagName) {
 		getDatabase(true).execSQL("UPDATE Tag SET title = ? WHERE _id == ? ", new String[] {tagName, "" + id});
 	}
 	
 	public int getDayWorthCount(DateTime dateTime) {
 		Cursor cur = getDatabase(false).rawQuery(
 			"SELECT SUM(worth) FROM Event WHERE date(time) == ? ", new String [] { dateTime.getDateString() });
 		cur.moveToFirst();
 		int retval = cur.getInt(0);
 		cur.close();
 		return retval;
 	}
 	
 	public int getDayWorthCountByTagId(long id, DateTime dt) {
 		String idStr = String.valueOf(id);
 		Cursor cur = getDatabase(false).rawQuery(
 			"SELECT SUM(worth) FROM RelEventTag, Event "+
 			"WHERE RelEventTag.event_id == Event._id " +
 				"AND RelEventTag.tag_id == ? AND date(Event.time) == ? ", new String [] { idStr , dt.getDateString() });
 		cur.moveToFirst();
 		int retval = cur.getInt(0);
 		cur.close();
 		return retval;
 	}
 	
 
 
 	public void addToWelcomeTag(long id) {
 		String [] arg = new String [] { String.valueOf(id) };
 		SQLiteDatabase db = getDatabase(true);
 		Cursor c = db.rawQuery("SELECT tag_id FROM WelcomeTag WHERE tag_id == ? ", arg);
 		if (c.moveToFirst())
 		{
 			// has inserted already
 			// nothing to do.
 		}
 		else
 		{
 			db.execSQL("INSERT INTO WelcomeTag VALUES(?) ", arg);
 		}
 		c.close();
 		
 	}
 	
 	public void removeFromWelcomeTag(long id) {
 		String [] arg = new String [] { String.valueOf(id) };
 		getDatabase(true).execSQL("DELETE FROM WelcomeTag WHERE tag_id == ? ", arg);
 	}
 
 	
 	public Cursor getCursorOfDates() {
 		return getDatabase(false)
 			.rawQuery("SELECT DISTINCT date(time),CAST(strftime('%Y%m%d',time) AS INTEGER) AS _id, SUM(worth) FROM event GROUP BY _id ORDER BY _id DESC ", null);
 	}
 	
 	public Cursor getCursorOfEvents() {
 		return getDatabase(false)
 			.rawQuery(
 				"SELECT _id, description, time, worth FROM Event ORDER BY time ", null);
 	}
 	
 	public Cursor getCursorOfEventsByDate(String dateStr) {
 		return getDatabase(false)
 			.rawQuery(
 				"SELECT _id, description, time, worth " + 
 				"FROM Event " +
 				"WHERE date(time) LIKE ? " +
 				"ORDER BY time ", new String[] {dateStr} );
 	}
 	
 	public Cursor getCursorOfTagsByEvent(String eventId) {
 		return getDatabase(false)
 			.rawQuery(
 				"SELECT tag_id AS _id, title " +
 				"FROM Tag, RelEventTag " +
 				"WHERE RelEventTag.event_id == ? AND RelEventTag.tag_id == Tag._id " +
 				"ORDER BY _id ", 
 				new String [] {eventId} );
 		
 	}
 	
 	public Cursor getCursorOfTags() {
 		return getDatabase(false)
 			.rawQuery(
 				// count worth of event which has a tag
 				"SELECT Tag._id AS _id, title, SUM(worth) "+ 
 					"FROM Event, Tag, RelEventTag " +
 					"WHERE Event._id == RelEventTag.event_id AND Tag._id == RelEventTag.tag_id " + 
 					"GROUP BY tag_id " + 
 				"UNION " +
 				// fake a tag for those events which do not have any tag
 				"SELECT 2147483647, 'No Tag Events', SUM(worth) " + 
 					"FROM ( "+
 						"SELECT _id, worth " +
 							"FROM Event " +
 						"EXCEPT " +
 						"SELECT event_id AS _id, worth " +
 						"FROM Event, RelEventTag " + 
 						"WHERE Event._id == RelEventTag.event_id" +
 					") " +
 				"ORDER BY _id ", null);
 	}
 	
 	public Cursor getCursorOfTagsSimple() {
 		return getDatabase(false)
 			.rawQuery(
 				"SELECT _id, title " + 
 					"FROM Tag " +
 				"ORDER BY _id " , null );
 	}
 	
 	public Cursor getCursorOfWelcomeScreen() {
 		return getDatabase(false)
 			.rawQuery(
 				"SELECT _id, title " + 
 					"FROM Tag, WelcomeTag " +
 					"WHERE Tag._id == WelcomeTag.tag_id " + 
 				"ORDER BY _id ", null);
 		
 	}
 	
 	public Cursor getCursorOfEventsByTag(String tagId) {
 		SQLiteDatabase db = getDatabase(false);
 		if ("2147483647".equals(tagId)) {
 			// find events which have no tag
 			return db.rawQuery(
 				"SELECT _id, description, time, worth " +
 					"FROM Event " +
 				"EXCEPT " +
 				"SELECT _id, description, time, worth " +
 					"FROM Event, RelEventTag " + 
 					"WHERE Event._id == RelEventTag.event_id",	
 				null);
 		} else
 		{
 			return db.rawQuery(
 				"SELECT _id, description, time, worth "+ 
 					"FROM Event, RelEventTag " +
 					"WHERE Event._id == RelEventTag.event_id AND RelEventTag.tag_id == ? " + 
 					"ORDER BY _id", 
 				new String[] { tagId });
 		}
 	}
 	
 //	public String runTest() {
 //		SQLiteDatabase db = getDatabase(true);
 //		mDatabaseOpenHelper.onUpgrade(db, 0, 0);
 //		
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'AAA', 1,  '2012-04-25 23:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'BBB', 2,  '2012-04-25 01:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'CCC', 4,  '2012-04-25 08:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'BBA', 8,  '2012-04-24 23:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'BBD', 16, '2012-04-22 23:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'BBC', 32, '2012-04-23 23:36:25' );");
 //		db.execSQL("INSERT INTO Event VALUES(NULL, 'BBG', 64, '2012-04-21 23:36:25' );");
 //		db.execSQL("INSERT INTO Tag VALUES(NULL, 'aa');");
 //		db.execSQL("INSERT INTO Tag VALUES(NULL, 'bb');");
 //		db.execSQL("INSERT INTO Tag VALUES(NULL, 'cc');");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(1,1);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(1,2);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(2,3);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(2,2);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(3,1);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(3,3);");
 //		db.execSQL("INSERT INTO RelEventTag VALUES(3,2);");
 //	
 //		Cursor cur = db.rawQuery(
 //				"SELECT time, description, _id FROM Event",
 //				null);
 //		StringBuilder sb = new StringBuilder();
 //		if (cur.moveToFirst()) {
 //			do {
 //				sb.append('[').append(cur.getString(0)).
 //				append('|').append(cur.getString(1)).append(']');
 //				
 //				Cursor tagCur = getCursorOfTagsByEvent(cur.getString(2));
 //				if (tagCur.moveToFirst())
 //				{
 //					do {
 //						sb.append('<').append(tagCur.getString(1)).append('>');
 //					} while (tagCur.moveToNext());
 //				}
 //				tagCur.close();
 //				
 //				sb.append('\n');
 //			} while (cur.moveToNext());
 //		}
 //		return sb.toString();
 //		
 //	}
 	
 	private static class EventDatabaseOpenHelper extends SQLiteOpenHelper {
 		private final static String DATABASE_NAME = "event_database";
 		
 		private final static int DB_VER_1 = 3;
 		private final static int DB_VER_2 = 4;
 		
 		private final static int DATABASE_VERSION = DB_VER_2;
 		private final static String SQL_CREATE_TABLE_EVENT = 
 			"CREATE TABLE Event( " +
 				"_id 		INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				"description	VARCHAR(64), " +
 				"worth		INTEGER, " +
 				"time		VARCHAR(32) " +
 			") ";
 		private final static String SQL_CREATE_TABLE_TAG = 	
 			"CREATE TABLE Tag( " +
 			"	_id		INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			"	title		VARCHAR(32) UNIQUE " +
 			") ";
 			
 		private final static String SQL_CREATE_TABLE_RELEVENTTAG =
 			"CREATE TABLE RelEventTag( " +
 			"	event_id	INTEGER, " +
 			"	tag_id		INTEGER, " +
 			"	PRIMARY KEY (event_id, tag_id), " +
 			"	FOREIGN KEY (event_id) REFERENCES Event(_id), " +
 			"	FOREIGN KEY (tag_id) REFERENCES Tag(_id) " +
 			") ";
 		
 		private final static String SQL_CREATE_TABLE_WELCOMETAG = 
 			"CREATE TABLE WelcomeTag( " + 
 			"	tag_id		INTEGER, " +
 			"	PRIMARY KEY (tag_id), " + 
 			"	FOREIGN KEY (tag_id) REFERENCES Tag(_id) " +
 			") ";
 		
 		private final static String SQL_DROP_TABLE_EVENT = 
 			"DROP TABLE IF EXISTS Event";
 		private final static String SQL_DROP_TABLE_TAG = 
 			"DROP TABLE IF EXISTS Tag"; 
 		private final static String SQL_DROP_TABLE_RELEVENTTAG = 
 			"DROP TABLE IF EXISTS RelEventTag";
 
 		public EventDatabaseOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
    		db.execSQL(SQL_CREATE_TABLE_WELCOMETAG);
 			db.execSQL(SQL_CREATE_TABLE_EVENT);
 			db.execSQL(SQL_CREATE_TABLE_TAG);
 			db.execSQL(SQL_CREATE_TABLE_RELEVENTTAG);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			if (newVersion == DB_VER_2 && oldVersion == DB_VER_1) {
 				db.execSQL(SQL_CREATE_TABLE_WELCOMETAG);
 				Log.d("AndTest", "database update done Ver1=>Ver2.");
 				
 			} else
 			{	
 				db.execSQL(SQL_DROP_TABLE_EVENT);
 				db.execSQL(SQL_DROP_TABLE_TAG);
 				db.execSQL(SQL_DROP_TABLE_RELEVENTTAG);
 				onCreate(db);
 			}
 		}
 		
 	}
 
 
 }
