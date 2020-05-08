 package edu.mines.caseysoto.schoolschedulercaseysoto;
 
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
 
 
 public class SchedulerContentProvider extends ContentProvider
 {
   private SchedulerDatabaseHelper database;
 
   // Used for the UriMacher
   private static final int COURSES = 10;
   private static final int COURSES_ID = 11;
   private static final int HOMEWORK = 20;
   private static final int HOMEWORK_ID = 22;
 
   private static final String AUTHORITY = "edu.mines.caseysoto.schoolschedulercaseysoto.coursecontentprovider";
 
   private static final String BASE_PATH = "courses";
   private static final String BASE_PATH_H = "homework";
   public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH );
   public static final Uri CONTENT_URI_H = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH_H );
 
   public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/courses";
   public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/course";
   
   public static final String CONTENT_TYPE_H = ContentResolver.CURSOR_DIR_BASE_TYPE + "/homework"; // TODO
   public static final String CONTENT_ITEM_TYPE_H = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/homework"; // TODO
   
   private static final UriMatcher sURIMatcher = new UriMatcher( UriMatcher.NO_MATCH );
 
   static
   {
 	  //Courses
     sURIMatcher.addURI( AUTHORITY, BASE_PATH, COURSES );
     sURIMatcher.addURI( AUTHORITY, BASE_PATH + "/#", COURSES_ID );
     
     //HW
    sURIMatcher.addURI( AUTHORITY, BASE_PATH, HOMEWORK );
    sURIMatcher.addURI( AUTHORITY, BASE_PATH + "/#", HOMEWORK_ID );
   }
 
   @Override
   public boolean onCreate()
   {
     database = new SchedulerDatabaseHelper( getContext() );
     return false;
   }
 
   @Override
   public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
   {
     // Using SQLiteQueryBuilder instead of query() method
     SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 
     // Check if the caller has requested a column which does not exist
     int tableName = checkColumns( projection );
 
     // Set the table
     if(tableName == COURSES){
     	queryBuilder.setTables( CourseTable.TABLE_NAME );
     } else if(tableName == HOMEWORK){
     	queryBuilder.setTables( HomeworkTable.TABLE_NAME );
     }
     
 
     int uriType = sURIMatcher.match( uri );
     switch( uriType )
     {
       case COURSES:
         break;
       case HOMEWORK:
     	  break;
       case COURSES_ID:
         // Adding the ID to the original query
         queryBuilder.appendWhere( CourseTable.COLUMN_ID + "=" + uri.getLastPathSegment() );
         break;
       case HOMEWORK_ID:
           // Adding the ID to the original query
           queryBuilder.appendWhere( HomeworkTable.COLUMN_ID + "=" + uri.getLastPathSegment() );
           break;
       default:
         throw new IllegalArgumentException( "Unknown URI: " + uri );
     }
 
     SQLiteDatabase db = database.getWritableDatabase();
     Cursor cursor = queryBuilder.query( db, projection, selection, selectionArgs, null, null, sortOrder );
 
     // Make sure that potential listeners are getting notified
     cursor.setNotificationUri( getContext().getContentResolver(), uri );
 
     return cursor;
   }
 
   @Override
   public String getType( Uri uri )
   {
     return null;
   }
 
   @Override
   public Uri insert( Uri uri, ContentValues values )
   {
     int uriType = sURIMatcher.match( uri );
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     //int rowsDeleted = 0;
     long id = 0;
     switch( uriType )
     {
       case COURSES:
         id = sqlDB.insert( CourseTable.TABLE_NAME, null, values );
         break;
       case HOMEWORK:
           id = sqlDB.insert( HomeworkTable.TABLE_NAME, null, values );
           break;
       default:
         throw new IllegalArgumentException( "Unknown URI: " + uri );
     }
     getContext().getContentResolver().notifyChange( uri, null );
     return Uri.parse( BASE_PATH + "/" + id );
   }
 
   @Override
   public int delete( Uri uri, String selection, String[] selectionArgs )
   {
     int uriType = sURIMatcher.match( uri );
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     int rowsDeleted = 0;
     String id = uri.getLastPathSegment();
 
     switch( uriType )
     {
       case COURSES:
         rowsDeleted = sqlDB.delete( CourseTable.TABLE_NAME, selection, selectionArgs );
         break;
       case HOMEWORK:
           rowsDeleted = sqlDB.delete( HomeworkTable.TABLE_NAME, selection, selectionArgs );
     	  break;
       case COURSES_ID:
         if( TextUtils.isEmpty( selection ) )
         {
           rowsDeleted = sqlDB.delete( CourseTable.TABLE_NAME, CourseTable.COLUMN_ID + "=" + id, null );
         }
         else
         {
           rowsDeleted = sqlDB
               .delete( CourseTable.TABLE_NAME, CourseTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs );
         }
         break;
       case HOMEWORK_ID:
           if( TextUtils.isEmpty( selection ) )
           {
             rowsDeleted = sqlDB.delete( HomeworkTable.TABLE_NAME, HomeworkTable.COLUMN_ID + "=" + id, null );
           }
           else
           {
             rowsDeleted = sqlDB
                 .delete( HomeworkTable.TABLE_NAME, HomeworkTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs );
           }
           break;
       default:
         throw new IllegalArgumentException( "Unknown URI: " + uri );
     }
     getContext().getContentResolver().notifyChange( uri, null );
     return rowsDeleted;
   }
 
   @Override
   public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs )
   {
 
     int uriType = sURIMatcher.match( uri );
     SQLiteDatabase sqlDB = database.getWritableDatabase();
     int rowsUpdated = 0;
     switch( uriType )
     {
       case COURSES:
         rowsUpdated = sqlDB.update( CourseTable.TABLE_NAME, values, selection, selectionArgs );
         break;
       case COURSES_ID:
         String id = uri.getLastPathSegment();
         if( TextUtils.isEmpty( selection ) )
         {
           rowsUpdated = sqlDB.update( CourseTable.TABLE_NAME, values, CourseTable.COLUMN_ID + "=" + id, null );
         }
         else
         {
           rowsUpdated = sqlDB.update( CourseTable.TABLE_NAME, values, CourseTable.COLUMN_ID + "=" + id + " and " + selection,
               selectionArgs );
         }
         break;
       default:
         throw new IllegalArgumentException( "Unknown URI: " + uri );
     }
     getContext().getContentResolver().notifyChange( uri, null );
     return rowsUpdated;
   }
 
   private int checkColumns( String[] projection )
   {
 	  int tableName = 0;
 	  
     String[] availableCourses = { CourseTable.COLUMN_ID, CourseTable.COLUMN_NAME};
     String[] availableHomeworks = { HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME, HomeworkTable.COLUMN_DATE, HomeworkTable.COLUMN_DESCRIPTION, HomeworkTable.COLUMN_COURSE_NAME };
     
     if( projection != null )
     {
       HashSet<String> requestedColumns = new HashSet<String>( Arrays.asList( projection ) );
       HashSet<String> availableColumnsCourses = new HashSet<String>( Arrays.asList( availableCourses ) );
       HashSet<String> availableColumnsHomework = new HashSet<String>( Arrays.asList( availableHomeworks ) );
       // Check if all columns which are requested are available
       if( !availableColumnsCourses.containsAll( requestedColumns )  &&  !availableColumnsHomework.containsAll( requestedColumns ) )
       {
         throw new IllegalArgumentException( "Unknown columns in projection" );
       } else if( availableColumnsCourses.containsAll( requestedColumns ) ){
     	  tableName = COURSES;
       } else {
     	  tableName = HOMEWORK;
       }
     }
     return tableName;
   }  
 }
