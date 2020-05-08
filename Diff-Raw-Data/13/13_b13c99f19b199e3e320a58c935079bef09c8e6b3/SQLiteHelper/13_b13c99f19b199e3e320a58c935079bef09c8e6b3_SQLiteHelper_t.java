 package com.randallma.whatsthehomework;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SQLiteHelper extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "wth.db";
 	private static final int DATABASE_VERSION = 1;
 	public static final String COLUMN_ID = "_id";
 
 	public static final String TABLE_ASSIGNMENTS = "assignments";
 	public static final String COLUMN_DESCRIPTION = "description";
 	public static final String COLUMN_DATE_DUE = "date_due";
 	public static final String COLUMN_DATE_ASSIGNED = "date_assigned";
 	public static final String COLUMN_IMAGE = "image_uri";
 	public static final String COLUMN_SCHOOL_CLASS_ID = "school_class_id";
 	public static final String COLUMN_ARCHIVED = "archived";
 
 	public static final String TABLE_SCHOOL_CLASSES = "school_classes";
 	public static final String COLUMN_TITLE = "title";
 
 	public static final String TABLE_SCHOOL_CLASSES_CREATE = "create table "
 			+ TABLE_SCHOOL_CLASSES + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_TITLE
 			+ " char not null);";
 
 	public static final String TABLE_ASSIGNMENTS_CREATE = "create table "
 			+ TABLE_ASSIGNMENTS + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_DESCRIPTION
			+ " text, " + COLUMN_DATE_DUE + " char," + COLUMN_DATE_ASSIGNED
			+ " char," + COLUMN_IMAGE + " char," + COLUMN_SCHOOL_CLASS_ID
			+ " integer," + COLUMN_ARCHIVED
			+ " boolean default 0, foreign key(" + COLUMN_SCHOOL_CLASS_ID
			+ ") references " + TABLE_SCHOOL_CLASSES + "(" + COLUMN_ID + "));";
 
 	public SQLiteHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(TABLE_SCHOOL_CLASSES_CREATE);
		System.out.println(TABLE_ASSIGNMENTS_CREATE);
 		db.execSQL(TABLE_ASSIGNMENTS_CREATE);
 		db.execSQL("insert into school_classes values(null, 'Example Post');");
 		db.execSQL("insert into assignments values(null, 'This is an example post.', 'Wednesday, January 1, 3000', 'Wednesday, January 1, 3000', null, 1, 0);");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(SQLiteHelper.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		db.execSQL("drop table if exists " + TABLE_ASSIGNMENTS);
 		onCreate(db);
 	}
 }
