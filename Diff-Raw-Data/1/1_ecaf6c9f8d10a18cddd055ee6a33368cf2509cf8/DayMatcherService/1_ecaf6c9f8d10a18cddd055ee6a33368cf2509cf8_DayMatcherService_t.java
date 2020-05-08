 /**
  * 
  */
 package com.kamosoft.happycontacts;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.provider.Contacts.People;
 
 import com.kamosoft.happycontacts.alarm.AlarmController;
 import com.kamosoft.happycontacts.dao.DbAdapter;
 import com.kamosoft.happycontacts.dao.HappyContactsDb;
 import com.kamosoft.happycontacts.model.ContactFeast;
 import com.kamosoft.happycontacts.model.ContactFeasts;
 import com.kamosoft.utils.AndroidUtils;
 
 /**
  * @author tom
  *
  */
 public class DayMatcherService
     extends Service
 {
     @Override
     public void onCreate()
     {
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: start onCreate()" );
         }
 
         /*
          * Look for names matching today date
          */
         ContactFeasts contactFeastToday = DayMatcherService.testDayMatch( this );
 
         if ( !contactFeastToday.getContactList().isEmpty() )
         {
             /* lancer les notify event */
             Notifier.notifyEvent( this );
         }
 
         /*
          * schedule next alarm
          */
         AlarmController.startAlarm( this );
 
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: end onCreate()" );
         }
         /*
          * release the wake lock
          */
         AlarmController.releaseStaticLock( this );
 
         stopSelf();
     }
 
     /**
      * search for today date
      * @param context
      * @return
      */
     public static ContactFeasts testDayMatch( Context context )
     {
         SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM" );
         Date date = new Date();
         String day = dateFormat.format( date );
         SimpleDateFormat fullDateFormat = new SimpleDateFormat( "dd/MM/yyyy" );
         String fullDate = fullDateFormat.format( date );
         return testDayMatch( context, day, fullDate );
     }
 
     /**
      * search for defined date
      * @param context
      * @param day
      * @param fullDate
      * @return
      */
     public static ContactFeasts testDayMatch( Context context, String day, String fullDate )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: start testDayMatch()" );
         }
 
         /*
          * init and open database
          */
         DbAdapter mDb = new DbAdapter( context );
         mDb.open( true );
 
         /*
          * Look for names matching today date
          */
         ContactFeasts contactFeastToday = new ContactFeasts( day );
 
         Cursor cursor = mDb.fetchNamesForDay( day );
         if ( cursor.getCount() == 0 )
         {
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: day " + day + " no feast found" );
             }
             cursor.close();
             mDb.close();
             return contactFeastToday;
         }
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: found " + cursor.getCount() + " feast(s) for today" );
         }
 
         Map<String, ContactFeast> names = new HashMap<String, ContactFeast>();
         while ( cursor.moveToNext() )
         {
             String name = cursor.getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Feast.NAME ) );
             if ( name == null )
             {
                 Log.e( "skipping mDb.fetchNamesForDay(" + day + ") returned name=" + name );
                 continue;
             }
             ContactFeast contactFeast = new ContactFeast( name, null );
             names.put( AndroidUtils.replaceAccents( name ).toUpperCase(), contactFeast );
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: day " + day + " feast : " + contactFeast.getContactName() );
             }
         }
         cursor.close();
 
         /* FIXME pour le screenshot */
         names.put( "JESSICA", new ContactFeast( "Jessica", null ) );
 
         /*
          * now we have to scan contacts
          */
         String[] projection = new String[] { People._ID, People.NAME, People.DISPLAY_NAME };
         cursor = context.getContentResolver().query( People.CONTENT_URI, projection, null, null, People.NAME + " ASC" );
 
         if ( cursor != null )
         {
             while ( cursor.moveToNext() )
             {
                 Long contactId = cursor.getLong( cursor.getColumnIndexOrThrow( People._ID ) );
                 String contactName = cursor.getString( cursor.getColumnIndexOrThrow( People.NAME ) );
                 String displayName = cursor.getString( cursor.getColumnIndexOrThrow( People.DISPLAY_NAME ) );
 
                 if ( contactId == null || contactName == null )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.v( "DayMatcher: skipping Contact with displayName=" + displayName + ", name=" + contactName
                             + ", contactId=" + contactId );
                     }
                     continue;
                 }
 
                 for ( String subName : contactName.split( " " ) )
                 {
                     String subNameUpper = AndroidUtils.replaceAccents( subName ).toUpperCase();
                     if ( names.containsKey( subNameUpper ) )
                     {
                         if ( mDb.isBlackListed( contactId, fullDate ) )
                         {
                             if ( Log.DEBUG )
                             {
                                 Log.v( "DayMatcher: already wished this year " + contactName + " is ignored" );
                             }
                             continue;
                         }
 
                         /* find one !! */
                         if ( Log.DEBUG )
                         {
                             Log.v( "DayMatcher: day contact feast found for \"" + contactName + "\", id=\"" + contactId
                                 + "\"" );
                         }
                         ContactFeast contactFeast = names.get( subNameUpper );
                         //duplicate the contact fest and set the name
                         ContactFeast newContactFeast = new ContactFeast( contactName, contactFeast.getLastWishYear() );
                         contactFeastToday.addContact( contactId, newContactFeast );
                     }
                 }
             }
             cursor.close();
         }
         if ( Log.DEBUG )
         {
             if ( contactFeastToday.getContactList().isEmpty() )
             {
                 Log.v( "DayMatcher: no matching contact found" );
             }
         }
         mDb.close();
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: end testDayMatch()" );
         }
         return contactFeastToday;
     }
 
     /**
      * @see android.app.Service#onBind(android.content.Intent)
      */
     @Override
     public IBinder onBind( Intent intent )
     {
         return null;
     }
 }
