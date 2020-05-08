 package org.lds.community.CallingWorkFlow.domain;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.util.ArrayList;
 import java.util.List;
 
 @Singleton
 public class WorkFlowDB {
     /* Used for debugging and logging */
     private static final String TAG = "WorkFlowDB";
 
     /**
      * The database that the provider uses as its underlying data store
      */
     private static final String DATABASE_NAME = "callingWorkFlow.db";
 
     /**
      * The database version
      */
     private static final int DATABASE_VERSION = 2;
     public static final String CALLING_VIEW_ITEM_JOIN =
 	    "SELECT p.*, c.*, w.*, m.* " +
         "  FROM " + Position.TABLE_NAME + " p, " +
                     Calling.TABLE_NAME + " c, " +
                     Member.TABLE_NAME + " m, " +
                     WorkFlowStatus.TABLE_NAME + " w " +
         " WHERE p." + PositionBaseRecord.POSITION_ID + " = c." + CallingBaseRecord.POSITION_ID +
         " AND m." + Member.INDIVIDUAL_ID + " = c." + Calling.INDIVIDUAL_ID +
         " AND w." + WorkFlowStatus.STATUS_NAME + " = c." + Calling.STATUS_NAME;
 
     private DatabaseHelper dbHelper;
 
     @Inject
     public WorkFlowDB(Context context) {
         dbHelper = new DatabaseHelper( context );
     }
 
     public List<WorkFlowStatus> getWorkFlowStatuses() {
         return getData( WorkFlowStatus.TABLE_NAME, WorkFlowStatus.class );
     }
 
     public void updateWorkFlowStatus(List<WorkFlowStatus> statuses) {
         updateData( WorkFlowStatus.TABLE_NAME, statuses );
     }
 
     public void updatePositions(List<Position> positions) {
         updateData( Position.TABLE_NAME, positions);
     }
 
     public List<Position> getPositions() {
         return getData( Position.TABLE_NAME, Position.class );
     }
 
     /*
       * /callings/pending/<sinceDate>
       */
     public List<CallingViewItem> getPendingCallings() {
         return getCallings( false );
     }
 
     /*
       * /callings/completed/<sinceDate>
       */
     public List<CallingViewItem> getCompletedCallings() {
         return getCallings( true );
     }
 
     public boolean addCalling( Calling calling ) {
         long result = -1;
         SQLiteDatabase db = dbHelper.getDb();
         result = db.insert(Calling.TABLE_NAME, null, calling.getContentValues());
         return result > 0;
     }
 
     public List<CallingViewItem> getCallings(boolean completed) {
         String completedDbValue = completed ? "1" : "0";
         String SQL = CALLING_VIEW_ITEM_JOIN +
                " AND w." + WorkFlowStatusBaseRecord.IS_COMPLETE + "=" + completedDbValue +
                " AND c." + Calling.MARKED_FOR_DELETE + "=0";
 
         Cursor results = null;
         List<CallingViewItem> callings = new ArrayList<CallingViewItem>();
         try {
             SQLiteDatabase db = dbHelper.getDb();
             results = db.rawQuery(SQL, null);
             results.moveToFirst();
 
             while( !results.isAfterLast() ) {
                 CallingViewItem calling = new CallingViewItem();
                 calling.setContent(results);
                 callings.add(calling);
                 results.moveToNext();
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeCursor(results);
         }
         return callings;
     }
 
 
     /**
      * Updates an existing  calling, or adds it new if it isn't in the db.
      * @param calling
      * @return
      */
     public boolean updateCalling( Calling calling ) {
         SQLiteDatabase db = dbHelper.getDb();
         long result = db.replace(Calling.TABLE_NAME, null, calling.getContentValues());
         return result > 0;
     }
     /**
      * Updates the given callings in the db.
      */
     public void saveCallings(List<Calling> callings) {
         SQLiteDatabase db = dbHelper.getDb();
         String whereClause;
         String[] whereArgs = new String[2];
         whereClause = getWhereForColumns( Calling.INDIVIDUAL_ID, Calling.POSITION_ID );
         for( Calling calling : callings) {
             whereArgs[0] = String.valueOf(calling.getIndividualId());
             whereArgs[1] = String.valueOf(calling.getPositionId());
             int result = db.update(CallingBaseRecord.TABLE_NAME, calling.getContentValues(), whereClause, whereArgs);
             Log.d(TAG, "Updated calling: " + calling.toString() + " Result=" + result );
         }
     }
 
     public void deleteCalling(Calling calling) {
         SQLiteDatabase db = dbHelper.getDb();
         String whereClause;
         String[] whereArgs = new String[2];
         whereClause = getWhereForColumns( Calling.INDIVIDUAL_ID, Calling.POSITION_ID );
         whereArgs[0] = String.valueOf(calling.getIndividualId());
         whereArgs[1] = String.valueOf(calling.getPositionId());
         int result = db.delete(CallingBaseRecord.TABLE_NAME, whereClause, whereArgs);
         Log.d(TAG, "Deleted calling: " + calling.toString() + " Result=" + result );
     }
 
     public List<CallingViewItem> getCallingsToSync() {
         return getCallingsToSync( "c." + Calling.IS_SYNCED + "=0" );
     }
 
     public List<CallingViewItem> getCallingsToDelete() {
         return getCallingsToSync( "c." + Calling.MARKED_FOR_DELETE + "=1" );
     }
 
     private List<CallingViewItem> getCallingsToSync( String criteria ) {
         List<CallingViewItem> callings = new ArrayList<CallingViewItem>();
         Cursor results = null;
         try {
             SQLiteDatabase db = dbHelper.getDb();
             String SQL = CALLING_VIEW_ITEM_JOIN + " AND " + criteria;
             results = db.rawQuery( SQL, null );
             results.moveToFirst();
             while(!results.isAfterLast()) {
                 CallingViewItem calling = new CallingViewItem();
                 calling.setContent( results );
                 callings.add( calling );
                 results.moveToNext();
             }
         } catch (Exception e) {
             Log.w( TAG, "Exception getCallingsToSync: " + e.toString() );
         } finally {
             closeCursor(results);
         }
         return callings;
     }
 
     /**
      * This method nukes all existing callings in the db and replaces them with the list that is passed in. Should probably
      * not be used.
      *
      * @param callings
      */
     public void updateCallings(List<? extends Calling> callings) {
         updateData( Calling.TABLE_NAME, callings );
     }
 
 
     /*
       * /wardlist
       */
     public List<Member> getWardList() {
         return getData( Member.TABLE_NAME, Member.class );
     }
 
     public void updateWardList( List<Member> memberList ) {
         updateData( Member.TABLE_NAME, memberList );
     }
 
     // generic/helper methods
     private <T extends BaseRecord>List<T> getData(String tableName, Class<T> clazz ) {
         List<T> resultList = new ArrayList<T>( );
         Cursor results = null;
         try {
             SQLiteDatabase db = dbHelper.getDb();
             results = db.query(tableName, null, null, null, null, null, null);
             results.moveToFirst();
             while( !results.isAfterLast() ) {
                 T record = clazz.newInstance();
                 record.setContent(results);
                 resultList.add(record);
                 results.moveToNext();
             }
         } catch (Exception e) {
             Log.w(TAG, "getData() Exception: " + e.toString());
         } finally {
             closeCursor( results );
         }
         return resultList;
     }
 
     private void updateData( String tableName, List<? extends BaseRecord> data ) {
         SQLiteDatabase db = null;
         try {
             db = dbHelper.getDb();
             db.beginTransaction();
             db.delete(tableName, null, null);
 //            DatabaseUtils.InsertHelper insertHelper = new DatabaseUtils.InsertHelper( db, tableName);
 
             for( BaseRecord dataRow : data ) {
 //                insertHelper.insert( dataRow.getContentValues() );
                 db.insert( tableName, null, dataRow.getContentValues() );
             }
             db.setTransactionSuccessful();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             db.endTransaction();
 
         }
     }
 
     public void closeDB() {
         SQLiteDatabase db = dbHelper.getDb();
         if( db != null && db.isOpen() ) {
             db.close();
         }
     }
 
     private void closeCursor(Cursor results) {
         if( results != null ) {
             results.close();
         }
     }
 
     public static String getWhereForColumns(String... columnNames) {
         final String AND = " AND ";
         StringBuilder whereClause = new StringBuilder();
         for( String columnName : columnNames ) {
             whereClause.append( columnName + "=?" ).append( AND );
         }
         // remove extra ", " from the last element
         if( whereClause.length() > AND.length() ) {
             whereClause.delete( whereClause.length() - AND.length(), whereClause.length() );
         }
         return whereClause.toString();
     }
 
     /**
      * This method is only here to allow testing to run sql against the db. Application code should use the
      * other methods to get the data they need.
      *
      * @return
      */
     public SQLiteDatabase getDbReference() {
         return dbHelper.getDb();
     }
 
 
     public boolean hasData( String tableName ) {
         return DatabaseUtils.queryNumEntries( dbHelper.getDb(), tableName ) > 0;
     }
 
     static class DatabaseHelper extends SQLiteOpenHelper {
 
         private SQLiteDatabase db;
         DatabaseHelper(Context context) {
 
             /* Calls the super constructor, requesting the default cursor factory. */
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             db = super.getWritableDatabase();
         }
 
         /**
          * Creates the underlying database with table name and column names taken from the
          * NotePad class.
          */
         @Override
         public void onCreate(SQLiteDatabase db) {
             // todo - setup INDEXES
             this.db = db;
             db.execSQL( WorkFlowStatusBaseRecord.CREATE_SQL );
             db.execSQL( MemberBaseRecord.CREATE_SQL );
             db.execSQL( PositionBaseRecord.CREATE_SQL );
             db.execSQL( CallingBaseRecord.CREATE_SQL );
 	        db.execSQL("PRAGMA foreign_keys = ON;");
         }
 
         public SQLiteDatabase getDb() {
             if (db == null || !db.isOpen()) {
                 db = this.getWritableDatabase();
             }
             return db;
         }
 
         public void close() {
             if( db != null && db.isOpen() ) {
                 db.close();
             }
         }
 
 	    @Override
 	    public void onOpen(SQLiteDatabase db) {
 	        super.onOpen(db);
 	        if (!db.isReadOnly()) {
 	            /* Enable foreign key constraints */
 	            db.execSQL("PRAGMA foreign_keys=ON;");
 	        }
 	    }
 
         /**
          *
          * Demonstrates that the provider must consider what happens when the
          * underlying datastore is changed. In this sample, the database is upgraded the database
          * by destroying the existing data.
          * A real application should upgrade the database in place.
          */
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             // for now - do nothing
         }
     }
 }
