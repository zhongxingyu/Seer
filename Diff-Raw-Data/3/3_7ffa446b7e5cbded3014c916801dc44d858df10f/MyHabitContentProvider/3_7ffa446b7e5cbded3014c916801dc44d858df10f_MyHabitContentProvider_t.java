 package com.synaptian.smoketracker.habits.contentprovider;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.TextUtils;
 import com.synaptian.smoketracker.habits.database.HabitDatabaseHelper;
 import com.synaptian.smoketracker.habits.database.HabitTable;
 import com.synaptian.smoketracker.habits.database.GoalTable;
 import com.synaptian.smoketracker.habits.database.EventTable;
 
 public class MyHabitContentProvider extends ContentProvider {
 
   // database
   private HabitDatabaseHelper database;
 
   // Used for the UriMacher
   private static final int HABITS = 10;
   private static final int HABIT_ID = 20;
   private static final int GOALS = 30;
   private static final int GOAL_ID = 40;
 
   private static final String AUTHORITY = "com.synaptian.smoketracker.habits.contentprovider";
 
   private static final String HABITS_PATH = "habits";
   public static final Uri HABITS_URI = Uri.parse("content://" + AUTHORITY + "/" + HABITS_PATH);
 
   private static final String GOALS_PATH = "goals";
   public static final Uri GOALS_URI = Uri.parse("content://" + AUTHORITY + "/" + GOALS_PATH);
 
   public static final String HABIT_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/habits";
   public static final String HABIT_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/habit";
   
   public static final String GOAL_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/goals";
   public static final String GOAL_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/goal";
 
   private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
   static {
 	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH, HABITS);
 	    sURIMatcher.addURI(AUTHORITY, HABITS_PATH + "/#", HABIT_ID);
 	    sURIMatcher.addURI(AUTHORITY, GOALS_PATH, GOALS);
 	    sURIMatcher.addURI(AUTHORITY, GOALS_PATH + "/#", GOAL_ID);
   }
 
   @Override
   public boolean onCreate() {
     database = new HabitDatabaseHelper(getContext());
     return false;
   }
 
   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 
     // Using SQLiteQueryBuilder instead of query() method
     SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
     int uriType = sURIMatcher.match(uri);
     switch (uriType) {
     case HABIT_ID:
         queryBuilder.appendWhere(HabitTable.COLUMN_ID + "=" + uri.getLastPathSegment());
     case HABITS:
        queryBuilder.setTables(HabitTable.TABLE_HABIT + " LEFT OUTER JOIN " + EventTable.TABLE_EVENT);
         break;
     case GOAL_ID:
         queryBuilder.appendWhere(GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_ID + "=" + uri.getLastPathSegment());
     case GOALS:
         queryBuilder.appendWhere(GoalTable.TABLE_GOAL + "." + GoalTable.COLUMN_ID + "=" + HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID);
         queryBuilder.setTables(GoalTable.TABLE_GOAL + "," + HabitTable.TABLE_HABIT);
         break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
 
     SQLiteDatabase db = database.getWritableDatabase();
     Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
     // Make sure that potential listeners are getting notified
     cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
     return cursor;
   }
 
   @Override
   public String getType(Uri uri) {
     return null;
   }
 
   @Override
   public Uri insert(Uri uri, ContentValues values) {
     int uriType = sURIMatcher.match(uri);
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     Uri returnUri;
     long id = 0;
     switch (uriType) {
     case HABITS:
         id = sqlDB.insert(HabitTable.TABLE_HABIT, null, values);
         returnUri = Uri.parse(HABITS_PATH + "/" + id);
         break;
     case GOALS:
         id = sqlDB.insert(GoalTable.TABLE_GOAL, null, values);
         returnUri = Uri.parse(GOALS_PATH + "/" + id);
         break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     getContext().getContentResolver().notifyChange(uri, null);
     return returnUri;
   }
 
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
     int uriType = sURIMatcher.match(uri);
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     int rowsDeleted = 0;
     switch (uriType) {
     case HABITS:
         rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, selection, selectionArgs);
         break;
       case HABIT_ID:
         String id = uri.getLastPathSegment();
         if (TextUtils.isEmpty(selection)) {
           rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, HabitTable.COLUMN_ID + "=" + id, null);
         } else {
           rowsDeleted = sqlDB.delete(HabitTable.TABLE_HABIT, HabitTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
         }
         break;
       case GOALS:
         rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, selection, selectionArgs);
         break;
       case GOAL_ID:
         id = uri.getLastPathSegment();
         if (TextUtils.isEmpty(selection)) {
           rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, GoalTable.COLUMN_ID + "=" + id, null);
         } else {
           rowsDeleted = sqlDB.delete(GoalTable.TABLE_GOAL, GoalTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
         }
         break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     getContext().getContentResolver().notifyChange(uri, null);
     return rowsDeleted;
   }
 
   @Override
   public int update(Uri uri, ContentValues values, String selection,
       String[] selectionArgs) {
 
     int uriType = sURIMatcher.match(uri);
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     int rowsUpdated = 0;
     switch (uriType) {
     case HABITS:
       rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, selection, selectionArgs);
       break;
     case HABIT_ID:
       String id = uri.getLastPathSegment();
       if (TextUtils.isEmpty(selection)) {
         rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, HabitTable.COLUMN_ID + "=" + id, null);
       } else {
         rowsUpdated = sqlDB.update(HabitTable.TABLE_HABIT, values, HabitTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
       }
       break;
     default:
       throw new IllegalArgumentException("Unknown URI: " + uri);
     }
     getContext().getContentResolver().notifyChange(uri, null);
     return rowsUpdated;
   }
 }
