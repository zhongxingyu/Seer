 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.widget.Toast;
 
 import com.kamosoft.happycontacts.alarm.AlarmController;
 import com.kamosoft.happycontacts.contacts.ContactUtils;
 import com.kamosoft.happycontacts.contacts.PhoneContact;
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
     implements Constants, DateFormatConstants
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
 
     public static void displayDayMatch( Context context )
     {
         /*
          * Look for names matching today date
          */
         ContactFeasts contactFeastToday = DayMatcherService.testDayMatch( context );
 
         if ( !contactFeastToday.getContactList().isEmpty() )
         {
             Notifier.notifyEvent( context );
             Toast.makeText( context, R.string.toast_events, Toast.LENGTH_LONG ).show();
         }
         else
         {
             Toast.makeText( context, R.string.toast_no_events, Toast.LENGTH_LONG ).show();
         }
     }
 
     /**
      * search for today date
      * @param context
      * @return
      */
     public static ContactFeasts testDayMatch( Context context )
     {
         Date date = new Date();
         String day = dayDateFormat.format( date );
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
             Log.v( "DayMatcher: start testDayMatch( " + day + ", " + fullDate + " )" );
         }
 
         /*
          * init and open database
          */
         DbAdapter mDb = new DbAdapter( context );
         mDb.open( true );
 
         /*
          * Look for names matching today date
          */
         ContactFeasts contactFeastsToday = new ContactFeasts();
 
         checkNameDays( context, mDb, contactFeastsToday, day, fullDate );
 
         checkBirthdays( context, mDb, contactFeastsToday, day, fullDate );
 
         if ( Log.DEBUG )
         {
             if ( contactFeastsToday.getContactList().isEmpty() )
             {
                 Log.v( "DayMatcher: no matching contact found for name days or birthdays" );
             }
         }
 
         mDb.close();
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: end testDayMatch()" );
         }
         return contactFeastsToday;
     }
 
     /**
      * @param context
      * @param mDb
      * @param contactFeastsToday
      * @param day
      * @param fullDate
      */
     public static void checkBirthdays( Context context, DbAdapter mDb, ContactFeasts contactFeastsToday, String day,
                                        String fullDate )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: start check Birthdays( )" );
         }
         Cursor cursor = mDb.fetchBirthdayForDay( day );
 
         if ( cursor.getCount() == 0 )
         {
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: day " + day + " no birthday found" );
             }
             cursor.close();
             return;
         }
 
         while ( cursor.moveToNext() )
         {
             Long contactId = cursor.getLong( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_ID ) );
             String contactName = cursor
                 .getString( cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_NAME ) );
             if ( mDb.isBlackListed( contactId, fullDate ) )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "DayMatcher: already wished this year " + contactName + " is ignored" );
                 }
                 continue;
             }
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: adding birthday for " + contactName );
             }
             ContactFeast contactFeast = new ContactFeast( BDAY_HINT, contactName );
             contactFeast.setBirthdayDate( cursor.getString( cursor
                 .getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_DATE ) ) );
             contactFeast.setBirthdayYear( cursor.getString( cursor
                 .getColumnIndexOrThrow( HappyContactsDb.Birthday.BIRTHDAY_YEAR ) ) );
             contactFeast.setContactId( contactId );
 
             contactFeastsToday.addContact( contactId, contactFeast );
         }
         cursor.close();
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: end check Birthdays( )" );
         }
     }
 
     public static void checkNameDays( Context context, DbAdapter mDb, ContactFeasts contactFeastsToday, String day,
                                       String fullDate )
     {
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: start check NameDays( )" );
         }
         if ( context.getSharedPreferences( APP_NAME, 0 ).getBoolean( PREF_DISABLE_NAMEDAY, false ) )
         {
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: namedays are disabled" );
             }
            return;
         }
         Cursor cursor = mDb.fetchNamesForDay( day );
         if ( cursor.getCount() == 0 )
         {
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: day " + day + " no feast found" );
             }
             cursor.close();
             return;
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
                 if ( Log.DEBUG )
                 {
                     Log.v( "skipping mDb.fetchNamesForDay(" + day + ") returned name=" + name );
                 }
                 continue;
             }
             ContactFeast contactFeast = new ContactFeast( name, name );
             String nameUpper = AndroidUtils.replaceAccents( name ).toUpperCase();
             names.put( nameUpper, contactFeast );
             if ( Log.DEBUG )
             {
                 Log.v( "DayMatcher: day " + day + " feast : " + contactFeast.getContactName() );
             }
 
             /* check white list */
             Cursor whiteListCursor = mDb.fetchWhiteListForNameDay( name );
             if ( whiteListCursor != null )
             {
                 if ( whiteListCursor.getCount() > 0 )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.v( "DayMatcher: have found " + whiteListCursor.getCount() + " white listed contacts" );
                     }
                     int contactIdIndex = whiteListCursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.CONTACT_ID );
                     int contactNameIndex = whiteListCursor
                         .getColumnIndexOrThrow( HappyContactsDb.WhiteList.CONTACT_NAME );
                     while ( whiteListCursor.moveToNext() )
                     {
                         Long contactId = whiteListCursor.getLong( contactIdIndex );
                         String contactName = whiteListCursor.getString( contactNameIndex );
                         if ( mDb.isBlackListed( contactId, fullDate ) )
                         {
                             if ( Log.DEBUG )
                             {
                                 Log.v( "DayMatcher: already wished this year " + contactName
                                     + " from whitelist is ignored" );
                             }
                             continue;
                         }
                         /* found from white list ! */
                         if ( Log.DEBUG )
                         {
                             Log.v( "DayMatcher: add " + contactId + " " + contactName
                                 + " from whitelist to the ContactFeastToday" );
                         }
                         contactFeastsToday.addContact( contactId, new ContactFeast( name, contactName ) );
 
                     }
                 }
                 whiteListCursor.close();
             }
         }
         cursor.close();
 
         /*
          * now we have to scan contacts
          */
         ArrayList<PhoneContact> phoneContacts = ContactUtils.loadPhoneContacts( context );
 
         if ( phoneContacts != null && !phoneContacts.isEmpty() )
         {
             for ( PhoneContact phoneContact : phoneContacts )
             {
                 String phoneContactName = AndroidUtils.replaceAccents( phoneContact.name ).toUpperCase();
                 for ( String subName : phoneContactName.split( " " ) )
                 {
                     if ( names.containsKey( subName ) )
                     {
                         if ( mDb.isBlackListed( phoneContact.id, fullDate ) )
                         {
                             if ( Log.DEBUG )
                             {
                                 Log.v( "DayMatcher: already wished this year " + phoneContact.name + " is ignored" );
                             }
                             continue;
                         }
 
                         /* find one !! */
                         if ( Log.DEBUG )
                         {
                             Log.v( "DayMatcher: day contact feast found for \"" + phoneContact.name + "\", id=\""
                                 + phoneContact.id + "\"" );
                         }
                         ContactFeast contactFeast = names.get( subName );
                         /* duplicate the contact feast and set the name */
                         ContactFeast newContactFeast = new ContactFeast( contactFeast.getNameDay(), phoneContact.name );
                         newContactFeast.setContactId( phoneContact.id );
                         contactFeastsToday.addContact( phoneContact.id, newContactFeast );
                     }
                 }
             }
             cursor.close();
         }
         if ( Log.DEBUG )
         {
             Log.v( "DayMatcher: end check NameDays( )" );
         }
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
