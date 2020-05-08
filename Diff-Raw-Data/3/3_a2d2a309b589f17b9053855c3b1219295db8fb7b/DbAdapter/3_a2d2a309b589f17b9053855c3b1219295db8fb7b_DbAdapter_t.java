 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.dao;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 
 import com.kamosoft.happycontacts.Constants;
 import com.kamosoft.happycontacts.Log;
 import com.kamosoft.happycontacts.R;
 import com.kamosoft.happycontacts.model.ContactFeast;
 import com.kamosoft.happycontacts.model.ContactFeasts;
 import com.kamosoft.happycontacts.model.SocialNetworkUser;
 import com.kamosoft.utils.AndroidUtils;
 import com.kamosoft.utils.DateUtils;
 import com.kamosoft.utils.ProgressDialogHandler;
 
 /**
  * @author tom
  * 
  */
 public class DbAdapter
     implements Constants
 {
     private DatabaseHelper mDbHelper;
 
     private SQLiteDatabase mDb;
 
     private final Context mCtx;
 
     /* use -1000 value for uid to distinguish from facebook sync result */
     public static final String GOOGLE_CONTACT_UID = "-1000";
 
     private static class DatabaseHelper
         extends SQLiteOpenHelper
     {
         private final Context mContext;
 
         /**
          * Handler for updating a progress dialog while 
          * creating or updating the database
          */
         private ProgressDialogHandler mHandler;
 
         DatabaseHelper( Context context, ProgressDialogHandler handler )
         {
             super( context, HappyContactsDb.DATABASE_NAME, null, HappyContactsDb.DATABASE_VERSION );
             mContext = context;
             mHandler = handler;
         }
 
         public boolean needUpgrade()
         {
             SQLiteDatabase db = mContext.openOrCreateDatabase( HappyContactsDb.DATABASE_NAME, 0, null );
             boolean needUpgrade = HappyContactsDb.DATABASE_VERSION > db.getVersion();
             db.close();
             return needUpgrade;
         }
 
         @Override
         public void onCreate( SQLiteDatabase db )
         {
             Log.v( "Creating database start..." );
 
             try
             {
                 /* get file content */
                 String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_create );
                 /* parsing sql */
                 String[] sqlStatements = sqlCode.split( ";" );
                 int nbStatements = sqlStatements.length;
                 ProgressDialogHandler handler = mHandler;
                 /* execute code */
                 for ( int i = 0; i < nbStatements; i++ )
                 {
                     db.execSQL( sqlStatements[i] );
 
                     if ( handler != null )
                     {
                         /* update handler */
                         handler.updateProgress( i, nbStatements );
                     }
                 }
                 if ( handler != null )
                 {
                     /* send last message to the handler */
                     handler.updateProgress( 100 );
                 }
                 Log.v( "Creating database done..." );
             }
             catch ( IOException e )
             {
                 // Should never happen!
                 Log.e( "Error reading sql file " + e.getMessage(), e );
                 throw new RuntimeException( e );
             }
             catch ( SQLException e )
             {
                 Log.e( "Error executing sql code " + e.getMessage(), e );
                 throw new RuntimeException( e );
             }
         }
 
         @Override
         public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
         {
             if ( oldVersion != 45 )
             {
                 Log.v( "Upgrading database from version " + oldVersion + " to " + newVersion
                     + ", which will destroy all old data" );
                 try
                 {
                     /* get file content */
                     String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_update );
                     /* execute code */
                     for ( String sqlStatements : sqlCode.split( ";" ) )
                     {
                         db.execSQL( sqlStatements );
                     }
                     Log.v( "Updating database done." );
                 }
                 catch ( IOException e )
                 {
                     // Should never happen!
                     Log.e( "Error reading sql file " + e.getMessage() );
                     throw new RuntimeException( e );
                 }
                 catch ( SQLException e )
                 {
                     Log.e( "Error executing sql code " + e.getMessage() );
                     throw new RuntimeException( e );
                 }
                 onCreate( db );
             }
             else
             {
                 Log.v( "Upgrading database from version " + oldVersion + " to " + newVersion
                     + ", which only apply some fixes" );
                 try
                 {
                     /* get file content */
                     String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_fixes );
                     /* execute code */
                     for ( String sqlStatements : sqlCode.split( ";" ) )
                     {
                         db.execSQL( sqlStatements );
                     }
                     Log.v( "Fixing database done." );
                 }
                 catch ( IOException e )
                 {
                     // Should never happen!
                     Log.e( "Error reading sql file " + e.getMessage() );
                     throw new RuntimeException( e );
                 }
                 catch ( SQLException e )
                 {
                     Log.e( "Error executing sql code " + e.getMessage() );
                     throw new RuntimeException( e );
                 }
             }
         }
     }
 
     public DbAdapter( Context ctx )
     {
         mCtx = ctx;
         mDbHelper = new DatabaseHelper( mCtx, null );
     }
 
     /**
      * Create or update the database in a thread, in order to allow to display a progress bar
      * @param context
      * @param handler
      * @param checkUpgrade
      */
     public static void createOrUpdate( final Context context, final ProgressDialogHandler handler )
     {
         Thread thread = new Thread()
         {
             @Override
             public void run()
             {
                 /* a simple call to getReadableDatabase() proceed to db create or upgrade */
                 DatabaseHelper db = new DatabaseHelper( context, handler );
                 if ( !db.needUpgrade() )
                 {
                     /* no need to upgrade */
                     return;
                 }
                 handler.startProgress();
                 db.getReadableDatabase();
                 db.close();
                 handler.stopProgress();
             }
         };
         thread.start();
     }
 
     public DbAdapter open( boolean readOnly )
         throws SQLException
     {
         mDb = readOnly ? mDbHelper.getReadableDatabase() : mDbHelper.getWritableDatabase();
         return this;
     }
 
     public void resetDbConnection()
     {
         Log.i( "resetting database connection (close and re-open)." );
         cleanup();
         mDb = SQLiteDatabase.openDatabase( Environment.getDataDirectory() + DataManager.dbPath, null,
                                            SQLiteDatabase.OPEN_READWRITE );
     }
 
     public void cleanup()
     {
         if ( ( mDb != null ) && mDb.isOpen() )
         {
             mDb.close();
         }
     }
 
     public boolean isOpen()
     {
         return mDb == null ? false : mDb.isOpen();
     }
 
     public void close()
     {
         mDbHelper.close();
     }
 
     /**
      * Return the birthdays
      * @return
      */
     public Cursor fetchAllBirthdays()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchAllBirthdays()" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.COLUMNS, null, null,
                                    null, null, HappyContactsDb.Birthday.CONTACT_NAME );
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchAllBirthdays()" );
         }
         return cursor;
     }
 
     /**
      * Return the birthdays for a given day
      * @param day format dd/MM
      * @return
      */
     public Cursor fetchBirthdayForDay( String day )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchBirthdayForDay(" + day + ")" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.COLUMNS,
                                    HappyContactsDb.Birthday.BIRTHDAY_DATE + "='" + day + "'", null, null, null,
                                    HappyContactsDb.Birthday.CONTACT_NAME );
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchBirthdayForDay()" );
         }
         return cursor;
     }
 
     /**
      * @param contactId
      * @return String the birthday date or null
      */
     public boolean hasBirthday( Long contactId )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start hasBirthday(" + contactId + ")" );
         }
         Cursor cursor = mDb
             .query( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.COLUMNS,
                     HappyContactsDb.Birthday.CONTACT_ID + "='" + contactId + "'", null, null, null, null );
         boolean hasBirhtday = cursor.getCount() > 0;
         cursor.close();
         return hasBirhtday;
     }
 
     public String[] getBirthday( Long contactId )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start getBirthday(" + contactId + ")" );
         }
         Cursor cursor = mDb
             .query( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.COLUMNS,
                     HappyContactsDb.Birthday.CONTACT_ID + "='" + contactId + "'", null, null, null, null );
         String day, year;
         if ( cursor.getCount() > 0 )
         {
             cursor.moveToFirst();
             day = cursor.getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_DATE ) );
             year = cursor.getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_YEAR ) );
         }
         else
         {
             if ( Log.DEBUG )
             {
                 Log.v( "DbAdapter: getBirthday() not found" );
             }
             cursor.close();
             return null;
         }
         cursor.close();
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end getBirthday with date=" + day + " year=" + year );
         }
         return new String[] { day, year };
     }
 
     /**
      * @param contactId
      * @return String the birthday date or null
      */
     //    public Date getBirthday( Long contactId )
     //    {
     //        if ( Log.DEBUG )
     //        {
     //            Log.v( "DbAdapter: start getBirthday(" + contactId + ")" );
     //        }
     //        Cursor cursor =
     //            mDb.query( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.COLUMNS,
     //                       HappyContactsDb.Birthday.CONTACT_ID + "='" + contactId + "'", null, null, null, null );
     //        Date birthdayDate = null;
     //        if ( cursor.getCount() > 0 )
     //        {
     //            cursor.moveToFirst();
     //            String day = cursor.getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_DATE ) );
     //            String year = cursor.getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_YEAR ) );
     //            cursor.close();
     //            String date = day + "/" + year;
     //
     //            try
     //            {
     //                birthdayDate = fullDateFormat.parse( date );
     //            }
     //            catch ( ParseException e )
     //            {
     //                Log.e( "unable to parse date " + date );
     //            }
     //        }
     //        cursor.close();
     //        if ( Log.DEBUG )
     //        {
     //            Log.v( "DbAdapter: end getBirthday with date " + birthdayDate );
     //        }
     //        return birthdayDate;
     //    }
 
     /**
      * insert a birthday 
      * @return
      */
     public long insertBirthday( Long contactId, String contactName, String birthday, String birthyear )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start insertBirthday(" + contactId + ", " + contactName + ", " + birthday + ", "
                 + birthyear );
         }
         ContentValues initialValues = new ContentValues();
         initialValues.put( HappyContactsDb.Birthday.CONTACT_ID, contactId );
         initialValues.put( HappyContactsDb.Birthday.CONTACT_NAME, contactName );
 
         initialValues.put( HappyContactsDb.Birthday.BIRTHDAY_DATE, birthday );
         initialValues.put( HappyContactsDb.Birthday.BIRTHDAY_YEAR, birthyear );
 
         return mDb.insert( HappyContactsDb.Birthday.TABLE_NAME, null, initialValues );
     }
 
     public boolean deleteBirthday( long id )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteBirthday(" + id + ")" );
         }
         return mDb.delete( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.ID + "=" + id, null ) > 0;
     }
 
     public boolean deleteBirthdayWithContactId( Long contactId )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteBirthdayWithContactId(" + contactId + ")" );
         }
         return mDb.delete( HappyContactsDb.Birthday.TABLE_NAME, HappyContactsDb.Birthday.CONTACT_ID + "=" + contactId,
                            null ) > 0;
     }
 
     public boolean updateBirthday( Long contactId, String contactName, String birthday, String birthyear )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start updateBirthday(" + contactId + ", " + contactName + ", " + birthday + ", "
                 + birthyear );
         }
         ContentValues values = new ContentValues();
         values.put( HappyContactsDb.Birthday.BIRTHDAY_DATE, birthday );
         values.put( HappyContactsDb.Birthday.BIRTHDAY_YEAR, birthyear );
 
         return mDb.update( HappyContactsDb.Birthday.TABLE_NAME, values, HappyContactsDb.Birthday.CONTACT_ID + "="
             + contactId, null ) > 0;
         //        if ( !this.deleteBirthdayWithContactId( contactId ) )
         //        {
         //            Log.e( "Error while deleting " + contactId + ", " + contactName + ", " + birthday + ", " + birthyear );
         //            return false;
         //        }
         //        if ( !this.insertBirthday( contactId, contactName, birthday, birthyear ) )
         //        {
         //            Log.e( "Error while inserting " + contactId + ", " + contactName + ", " + birthday + ", " + birthyear );
         //            return false;
         //        }
         //        if ( Log.DEBUG )
         //        {
         //            Log.v( "DbAdapter: end success updateBirthday" );
         //        }
         //        return true;
     }
 
     public boolean deleteAllBirthday()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteAllBirthday()" );
         }
         return mDb.delete( HappyContactsDb.Birthday.TABLE_NAME, null, null ) > 0;
     }
 
     public ArrayList<SocialNetworkUser> fetchFacebookSyncResults()
     {
         return fetchSyncResults( HappyContactsDb.SyncResult.USER_ID + "!=" + GOOGLE_CONTACT_UID + "" );
     }
 
     public ArrayList<SocialNetworkUser> fetchGoogleSyncResults()
     {
         return fetchSyncResults( HappyContactsDb.SyncResult.USER_ID + "=" + GOOGLE_CONTACT_UID + "" );
     }
 
     /**
      * Return the sync results
      * @return
      */
     private ArrayList<SocialNetworkUser> fetchSyncResults( String whereClause )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchAllSyncResult()" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.SyncResult.TABLE_NAME, HappyContactsDb.SyncResult.COLUMNS,
                                    whereClause, null, null, null, HappyContactsDb.SyncResult.USER_NAME );
 
         int userIdColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.USER_ID );
         int userNameColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.USER_NAME );
         int birthdayColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.BIRTHDAY_DATE );
         int contactIdColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.CONTACT_ID );
         int contactNameColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.CONTACT_NAME );
 
         ArrayList<SocialNetworkUser> userList = new ArrayList<SocialNetworkUser>();
 
         while ( cursor.moveToNext() )
         {
             String userId = cursor.getString( userIdColumnId );
             String userName = cursor.getString( userNameColumnId );
             String birthday = cursor.getString( birthdayColumnId );
             Long contactId = cursor.getLong( contactIdColumnId );
             String contactName = cursor.getString( contactNameColumnId );
             SocialNetworkUser user = new SocialNetworkUser();
             user.uid = userId;
             user.name = userName;
             user.birthday = birthday;
             user.setContactId( contactId );
             user.setContactName( contactName );
             userList.add( user );
         }
         cursor.close();
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchAllSyncResult()" );
         }
         return userList;
     }
 
     public boolean deleteFacebookSyncResults()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteAllSyncResults()" );
         }
         return mDb.delete( HappyContactsDb.SyncResult.TABLE_NAME, HappyContactsDb.SyncResult.USER_ID + "!="
             + GOOGLE_CONTACT_UID + "", null ) > 0;
     }
 
     public boolean deleteGoogleSyncResults()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteAllSyncResults()" );
         }
         return mDb.delete( HappyContactsDb.SyncResult.TABLE_NAME, HappyContactsDb.SyncResult.USER_ID + "="
             + GOOGLE_CONTACT_UID + "", null ) > 0;
     }
 
     public boolean updateSyncResult( SocialNetworkUser user )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start updateSyncResult user " + user.toString() );
         }
         ContentValues initialValues = new ContentValues();
         initialValues.put( HappyContactsDb.SyncResult.USER_ID, user.uid );
         initialValues.put( HappyContactsDb.SyncResult.USER_NAME, user.name );
         initialValues.put( HappyContactsDb.SyncResult.BIRTHDAY_DATE, user.birthday );
         initialValues.put( HappyContactsDb.SyncResult.CONTACT_ID, user.getContactId() );
         initialValues.put( HappyContactsDb.SyncResult.CONTACT_NAME, user.getContactName() );
         return mDb.update( HappyContactsDb.SyncResult.TABLE_NAME, initialValues, HappyContactsDb.SyncResult.USER_ID
             + "=" + user.uid + "", null ) > 0;
     }
 
     public boolean insertNextEvents( LinkedHashMap<String, ContactFeasts> nexEvents )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start insertNextEvents" );
         }
         /* delete before, events are only corrects for the current date */
         deleteNextEvents();
 
         String today = DateUtils.getToday( mCtx );
         boolean res = false;
         for ( Map.Entry<String, ContactFeasts> entry : nexEvents.entrySet() )
         {
             String when = entry.getKey();
             ContactFeasts contactFeasts = entry.getValue();
             if ( contactFeasts.getContactList().isEmpty() )
             {
                 /* special characters for telling there is no events */
                 ContentValues initialValues = new ContentValues();
                 initialValues.put( HappyContactsDb.NextEvents.CONTACT_ID, "$$$" );
                 initialValues.put( HappyContactsDb.NextEvents.CONTACT_NAME, "$$$" );
                 initialValues.put( HappyContactsDb.NextEvents.BIRTHDAY_YEAR, "$$$" );
                 initialValues.put( HappyContactsDb.NextEvents.NAMEDAY, "$$$" );
                 initialValues.put( HappyContactsDb.NextEvents.EVENT_WHEN, when );
                 initialValues.put( HappyContactsDb.NextEvents.CHECKED_DATE, today );
 
                 res = mDb.insert( HappyContactsDb.NextEvents.TABLE_NAME, null, initialValues ) > 0;
                 if ( !res )
                 {
                     Log.e( "Error while insert nexEvents for empty user" );
                     return res;
                 }
             }
             else
             {
                 for ( Map.Entry<Long, ContactFeast> entry2 : contactFeasts.getContactList().entrySet() )
                 {
                     ContactFeast user = entry2.getValue();
                     ContentValues initialValues = new ContentValues();
                     initialValues.put( HappyContactsDb.NextEvents.CONTACT_ID, user.getContactId() );
                     initialValues.put( HappyContactsDb.NextEvents.CONTACT_NAME, user.getContactName() );
                     initialValues.put( HappyContactsDb.NextEvents.BIRTHDAY_YEAR, user.getBirthdayYear() );
                     initialValues.put( HappyContactsDb.NextEvents.NAMEDAY, user.getNameDay() );
                     initialValues.put( HappyContactsDb.NextEvents.EVENT_WHEN, when );
                     initialValues.put( HappyContactsDb.NextEvents.CHECKED_DATE, today );
 
                     res = mDb.insert( HappyContactsDb.NextEvents.TABLE_NAME, null, initialValues ) > 0;
                     if ( !res )
                     {
                         Log.e( "Error while insert nexEvents for user " + user.toString() );
                         return res;
                     }
                 }
             }
         }
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end insertNextEvents" );
         }
         return true;
     }
 
     /**
      * Return the nextEvents
      * @param day format dd/MM
      * @return
      */
     public LinkedHashMap<String, ContactFeasts> fetchTodayNextEvents()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchTodayNextEvents" );
         }
         String today = DateUtils.getToday( mCtx );
         Cursor cursor = mDb.query( HappyContactsDb.NextEvents.TABLE_NAME, HappyContactsDb.NextEvents.COLUMNS,
                                    HappyContactsDb.NextEvents.CHECKED_DATE + "='" + today + "'", null, null, null,
                                   /* sort the text with its numeric value */
                                   "cast("+HappyContactsDb.NextEvents.EVENT_WHEN+" as int)" );
 
         LinkedHashMap<String, ContactFeasts> events = null;
         if ( cursor.getCount() > 0 )
         {
             events = new LinkedHashMap<String, ContactFeasts>();
             int contactIdColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.NextEvents.CONTACT_ID );
             int contactNameColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.NextEvents.CONTACT_NAME );
             int birthdayYearColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.NextEvents.BIRTHDAY_YEAR );
             int eventWhenColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.NextEvents.EVENT_WHEN );
             int nameDayColumnId = cursor.getColumnIndexOrThrow( HappyContactsDb.NextEvents.NAMEDAY );
 
             while ( cursor.moveToNext() )
             {
                 String birthdayYear = cursor.getString( birthdayYearColumnId );
 
                 Long contactId = cursor.getLong( contactIdColumnId );
                 String contactName = cursor.getString( contactNameColumnId );
 
                 String eventWhen = cursor.getString( eventWhenColumnId );
                 String nameDay = cursor.getString( nameDayColumnId );
                 ContactFeast contactFeast = new ContactFeast( nameDay, contactName );
                 contactFeast.setContactId( contactId );
                 contactFeast.setBirthdayYear( birthdayYear );
                 if ( events.containsKey( eventWhen ) )
                 {
                     /* check if its a real event */
                     if ( birthdayYear == null || !birthdayYear.equals( "$$$" ) )
                     {
                         events.get( eventWhen ).addContact( contactId, contactFeast );
                     }
                 }
                 else
                 {
                     ContactFeasts contactFeasts = new ContactFeasts();
                     /* check if its a real event */
                     if ( birthdayYear == null || !birthdayYear.equals( "$$$" ) )
                     {
                         contactFeasts.addContact( contactId, contactFeast );
                     }
                     events.put( eventWhen, contactFeasts );
                 }
             }
         }
         cursor.close();
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchTodayNextEvents()" );
         }
         return events;
     }
 
     public boolean deleteNextEvents()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteNextEvents()" );
         }
         return mDb.delete( HappyContactsDb.NextEvents.TABLE_NAME, null, null ) > 0;
     }
 
     public boolean insertFacebookSyncResults( List<SocialNetworkUser> users )
     {
         return insertSyncResults( users, true );
     }
 
     public boolean insertGoogleSyncResults( List<SocialNetworkUser> users )
     {
         return insertSyncResults( users, false );
     }
 
     private boolean insertSyncResults( List<SocialNetworkUser> users, boolean fromFaceBook )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start insertSyncResults" );
         }
         /* delete all previous sync results */
         if ( fromFaceBook )
         {
             deleteFacebookSyncResults();
         }
         else
         {
             deleteGoogleSyncResults();
         }
         boolean res = false;
         for ( SocialNetworkUser user : users )
         {
             ContentValues initialValues = new ContentValues();
             initialValues.put( HappyContactsDb.SyncResult.USER_ID, fromFaceBook ? user.uid : GOOGLE_CONTACT_UID );
             initialValues.put( HappyContactsDb.SyncResult.USER_NAME, user.name );
             initialValues.put( HappyContactsDb.SyncResult.BIRTHDAY_DATE, user.birthday );
             initialValues.put( HappyContactsDb.SyncResult.CONTACT_ID, user.getContactId() );
             initialValues.put( HappyContactsDb.SyncResult.CONTACT_NAME, user.getContactName() );
 
             res = mDb.insert( HappyContactsDb.SyncResult.TABLE_NAME, null, initialValues ) > 0;
             if ( !res )
             {
                 Log.e( "Error while insert syncResults for user " + user.toString() );
                 return res;
             }
         }
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end insertSyncResults" );
         }
         return true;
     }
 
     /**
      * Return the names for a given day
      * @param day format dd/MM
      * @return
      */
     public Cursor fetchAllNameDay()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchAllNameDay()" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.NameDay.TABLE_NAME, new String[] {
             HappyContactsDb.NameDay.ID,
             HappyContactsDb.NameDay.NAME_DAY }, null, null, null, null, HappyContactsDb.NameDay.NAME_DAY );
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchAllNameDay()" );
         }
         return cursor;
     }
 
     public Cursor fetchNameDayLike( String constraint )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchNameDayLike()" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.NameDay.TABLE_NAME, new String[] {
                                        HappyContactsDb.NameDay.ID,
                                        HappyContactsDb.NameDay.NAME_DAY }, HappyContactsDb.NameDay.NAME_DAY
                                        + " like \"" + constraint + "%\"",
                                    null, null, null, HappyContactsDb.NameDay.NAME_DAY );
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchNameDayLike()" );
         }
         return cursor;
     }
 
     /**
      * Return the names for a given day
      * @param day format dd/MM
      * @return
      */
     public Cursor fetchNamesForDay( String day )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchNameForDay()" );
         }
         /* use order by name */
         Cursor cursor = mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] {
                                        HappyContactsDb.Feast.ID,
                                        HappyContactsDb.Feast.NAME }, HappyContactsDb.Feast.DAY + "='" + day + "'",
                                    null, null, null,
                                    HappyContactsDb.Feast.NAME );
 
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchNameForDay()" );
         }
         return cursor;
     }
 
     /**
      * Returns the days for a given name
      * @param name
      * @return
      */
     public Cursor fetchDayForName( String name )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchDayForName()" );
         }
         /* 
          * order by date 
          * tips use substr() to have month then day
          */
         Cursor cursor = mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] {
                                        HappyContactsDb.Feast.ID,
                                        HappyContactsDb.Feast.DAY },
                                    HappyContactsDb.Feast.NAME + " like '" + name + "'", null, null, null,
                                    "substr(" + HappyContactsDb.Feast.DAY + ",4,2)||substr(" + HappyContactsDb.Feast.DAY
                                        + ",1,2)" );
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchDayForName()" );
         }
         return cursor;
     }
 
     /**
      * use a matrixCursor to avoid duplicate names (due to multiple source for the database)
      * @param cursor
      * @return
      */
     //    private Cursor avoidDuplicate( Cursor cursor, String idColumnName, String columnName )
     //    {
     //        if ( Log.DEBUG )
     //        {
     //            Log.v( "DbAdapter: start avoidDuplicate()" );
     //        }
     //        ArrayList<String> columns = new ArrayList<String>();
     //        MatrixCursor matrixCursor = new MatrixCursor( new String[] { idColumnName, columnName } );
     //        int columnIndex = cursor.getColumnIndex( columnName );
     //        int idColumnIndex = cursor.getColumnIndex( idColumnName );
     //        cursor.moveToFirst();
     //        do
     //        {
     //            String columnValue = cursor.getString( columnIndex );
     //            if ( columns.contains( columnValue ) )
     //            {
     //                continue;
     //            }
     //            Long id = cursor.getLong( idColumnIndex );
     //            matrixCursor.newRow().add( id ).add( columnValue );
     //            columns.add( columnValue );
     //        }
     //        while ( cursor.moveToNext() );
     //        cursor.close();
     //        if ( Log.DEBUG )
     //        {
     //            Log.v( "DbAdapter: end avoidDuplicate()" );
     //        }
     //        return matrixCursor;
     //    }
 
     /**
      * @param contactId
      * @param contactName
      * @param date
      * @return
      */
     public boolean insertBlackList( long contactId, String contactName, String date )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call insertBlackList()" );
         }
         ContentValues initialValues = new ContentValues();
         initialValues.put( HappyContactsDb.BlackList.CONTACT_ID, contactId );
         initialValues.put( HappyContactsDb.BlackList.CONTACT_NAME, contactName );
         if ( date != null )
         {
             initialValues.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
         }
         long result = mDb.insert( HappyContactsDb.BlackList.TABLE_NAME, null, initialValues );
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end insertBlackList() row inserted=" + result );
         }
         return result > 0;
     }
 
     public boolean deleteNextEvent( long contactId )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteEvent()" );
         }
         return mDb.delete( HappyContactsDb.NextEvents.TABLE_NAME, HappyContactsDb.NextEvents.CONTACT_ID + "="
             + contactId, null ) > 0;
     }
 
     public boolean deleteBlackList( long id )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteBlackList()" );
         }
         return mDb.delete( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.ID + "=" + id, null ) > 0;
     }
 
     public boolean deleteAllBlackList()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteAllBlackList()" );
         }
         return mDb.delete( HappyContactsDb.BlackList.TABLE_NAME, null, null ) > 0;
     }
 
     public boolean deleteAllWhiteList()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteAllWhiteList()" );
         }
         return mDb.delete( HappyContactsDb.WhiteList.TABLE_NAME, null, null ) > 0;
     }
 
     /**
      * @return all lines
      */
     public Cursor fetchAllBlackList()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call fetchAllBlackList()" );
         }
         return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS, null, null, null,
                           null, HappyContactsDb.BlackList.CONTACT_NAME );
     }
 
     public Cursor fetchWhiteListForNameDay( String nameDay )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call fetchWhiteListForNameDay() " + nameDay );
         }
         return mDb.query( HappyContactsDb.WhiteList.TABLE_NAME, HappyContactsDb.WhiteList.COLUMNS,
                           HappyContactsDb.WhiteList.NAME_DAY + " = \"" + nameDay + "\"", null, null, null, null );
     }
 
     public boolean insertWhiteList( Long contactId, String contactName, String nameDay )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call insertWhiteList(" + contactId + ", " + contactName + "," + nameDay + ") " );
         }
         ContentValues initialValues = new ContentValues();
         initialValues.put( HappyContactsDb.WhiteList.CONTACT_ID, contactId );
         initialValues.put( HappyContactsDb.WhiteList.CONTACT_NAME, contactName );
         initialValues.put( HappyContactsDb.WhiteList.NAME_DAY, nameDay );
         return mDb.insert( HappyContactsDb.WhiteList.TABLE_NAME, null, initialValues ) > 0;
     }
 
     public boolean deleteWhiteList( long id )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call deleteWhiteList()" );
         }
         return mDb.delete( HappyContactsDb.WhiteList.TABLE_NAME, HappyContactsDb.WhiteList.ID + "=" + id, null ) > 0;
     }
 
     /**
      * @return all lines
      */
     public Cursor fetchAllWhiteList()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call fetchAllWhiteList()" );
         }
         return mDb.query( HappyContactsDb.WhiteList.TABLE_NAME, HappyContactsDb.WhiteList.COLUMNS, null, null, null,
                           null, null );
     }
 
     /**
      * @return only the lines with lastWishDate=null, meaning the contact is black listed all the time.
      */
     public Cursor fetchAllTimeBlackListed()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: call fetchAllTimeBlackListed()" );
         }
         return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                           HappyContactsDb.BlackList.LAST_WISH_DATE + " is null", null, null, null,
                           HappyContactsDb.BlackList.CONTACT_NAME );
     }
 
     public Cursor fetchBlackList( long contactId )
         throws SQLException
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start fetchBlackList()" );
         }
         Cursor mCursor = mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                                     HappyContactsDb.BlackList.CONTACT_ID + "=" + contactId, null, null, null, null,
                                     null );
         if ( mCursor != null )
         {
             mCursor.moveToFirst();
         }
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end fetchBlackList()" );
         }
         return mCursor;
     }
 
     public boolean isBlackListed( long contactId, String date )
         throws SQLException
     {
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: start isBlackListed()" );
         }
         Cursor c = fetchBlackList( contactId );
         if ( c == null )
         {
             return false;
         }
         if ( c.getCount() == 0 )
         {
             c.close();
             return false;
         }
         if ( date != null )
         {
             /* check if its black listed for this year only */
             String lastWishedDate = c.getString( c.getColumnIndexOrThrow( HappyContactsDb.BlackList.LAST_WISH_DATE ) );
             c.close();
             return ( lastWishedDate == null || lastWishedDate.equals( date ) );
         }
         c.close();
         if ( Log.DEBUG )
         {
             Log.v( "DbAdapter: end isBlackListed()" );
         }
         return true;
     }
 
     public boolean updateContactFeast( long contactId, String contactName, String date )
     {
         if ( Log.DEBUG )
         {
             Log.v( "Dbadapter: call updateContactFeast for contact " + contactName + " with date " + date );
         }
         if ( isBlackListed( contactId, null ) )
         {
             ContentValues args = new ContentValues();
             args.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
             return mDb.update( HappyContactsDb.BlackList.TABLE_NAME, args, HappyContactsDb.BlackList.CONTACT_ID + "="
                 + contactId, null ) > 0;
         }
         else
         {
             return insertBlackList( contactId, contactName, date );
         }
     }
 }
