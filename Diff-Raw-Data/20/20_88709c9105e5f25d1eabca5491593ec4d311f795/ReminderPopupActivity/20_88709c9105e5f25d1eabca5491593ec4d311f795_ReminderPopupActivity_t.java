 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.NotificationManager;
 import android.content.ContentUris;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kamosoft.happycontacts.contacts.ContactUtils;
 import com.kamosoft.happycontacts.dao.DbAdapter;
 import com.kamosoft.happycontacts.model.ContactFeast;
 import com.kamosoft.happycontacts.model.ContactFeasts;
 import com.kamosoft.utils.AndroidUtils;
 
 /**
  * @author tom
  */
 public class ReminderPopupActivity
     extends Activity
     implements Constants, DateFormatConstants
 {
     private final int HOW_TO_CONTACT_DIALOG_ID = 1;
 
     private final int TEL_CHOOSER_DIALOG_ID = 2;
 
     private final int SMS_CHOOSER_DIALOG_ID = 3;
 
     private final int EMAIL_CHOOSER_DIALOG_ID = 4;
 
     private DbAdapter mDb;
 
     private Iterator<Map.Entry<Long, ContactFeast>> contacts;
 
     private boolean mKeepNotif = false;
 
     private String mDate;
 
     private ContactFeast mCurrentContactFeast;
 
     private SharedPreferences mPrefs;
 
     private TextView mFeastCounter;
 
     private TextView mNameDayTitle;
 
     private TextView mHappyFeastText;
 
     private ContactFeasts mContactFeasts;
 
     private int mIndex;
 
     private int mCurrentYear;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         requestWindowFeature( Window.FEATURE_NO_TITLE );
 
         super.onCreate( savedInstanceState );
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: start onCreate()" );
         }
         setContentView( R.layout.reminder );
 
         // Have the system blur any windows behind this one.
         getWindow().setFlags( WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND );
 
         mDb = new DbAdapter( this );
 
         mPrefs = getSharedPreferences( APP_NAME, 0 );
 
         Date date = new Date();
         String day = dayDateFormat.format( date );
         mDate = fullDateFormat.format( date );
 
         Calendar cal = Calendar.getInstance();
         mCurrentYear = cal.get( Calendar.YEAR );
 
         mContactFeasts = DayMatcherService.testDayMatch( this, day, mDate );
 
         /* loop on contacts */
         contacts = mContactFeasts.getContactList().entrySet().iterator();
 
         mNameDayTitle = (TextView) findViewById( R.id.nameday_title );
         mFeastCounter = (TextView) findViewById( R.id.feast_counter );
         mHappyFeastText = (TextView) findViewById( R.id.happyfeast_name );
         if ( mContactFeasts.getContactList().size() <= 1 )
         {
             mFeastCounter.setVisibility( View.GONE );
         }
 
         nextOrExit();
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: end onCreate()" );
         }
     }
 
     @Override
     protected Dialog onCreateDialog( int id )
     {
         switch ( id )
         {
             /* First dialog which show the available methods to contact */
             case HOW_TO_CONTACT_DIALOG_ID:
                 AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setTitle( getString( R.string.contact_method_dialog_title,
                                             mCurrentContactFeast.getContactName() ) );
 
                 final String[] contactMethods = getResources().getStringArray( R.array.contactmethods_items );
 
                 /* need to determine the available contacts method from the contact */
                 final ArrayList<String> availableContactMethods = new ArrayList<String>();
 
                 if ( mCurrentContactFeast.hasPhone() )
                 {
                     availableContactMethods.add( contactMethods[CALL_ITEM_INDEX] );
                     availableContactMethods.add( contactMethods[SMS_ITEM_INDEX] );
                 }
                 if ( mCurrentContactFeast.hasEmail() )
                 {
                     availableContactMethods.add( contactMethods[EMAIL_ITEM_INDEX] );
                 }
                 builder.setItems( availableContactMethods.toArray( new String[] {} ),
                                   new DialogInterface.OnClickListener()
                                   {
                                       public void onClick( DialogInterface dialog, int item )
                                       {
                                           String contactMethod = availableContactMethods.get( item );
                                           if ( contactMethod.equals( contactMethods[CALL_ITEM_INDEX] ) )
                                           {
                                               /* if contact has only one phone number, 
                                                * we direct launch the compose tel activity */
                                               if ( mCurrentContactFeast.getPhones().size() == 1 )
                                               {
                                                   composeTel( mCurrentContactFeast.getPhones().get( 0 ) );
                                                   updateCurrentContactFeast();
                                                   nextOrExit();
                                               }
                                               else
                                               {
                                                   showDialog( TEL_CHOOSER_DIALOG_ID );
                                               }
                                           }
                                           else if ( contactMethod.equals( contactMethods[SMS_ITEM_INDEX] ) )
                                           {
                                               /* if contact has only one phone number, 
                                                * we direct launch the compose sms activity */
                                               if ( mCurrentContactFeast.getPhones().size() == 1 )
                                               {
                                                   composeSms( mCurrentContactFeast.getPhones().get( 0 ) );
                                                   updateCurrentContactFeast();
                                                   nextOrExit();
                                               }
                                               else
                                               {
                                                   showDialog( SMS_CHOOSER_DIALOG_ID );
                                               }
                                           }
                                           else if ( contactMethod.equals( contactMethods[EMAIL_ITEM_INDEX] ) )
                                           {
                                               /* if contact has only one email, 
                                                * we direct launch the mail app */
                                               if ( mCurrentContactFeast.getEmails().size() == 1 )
                                               {
                                                   composeMail( mCurrentContactFeast.getEmails().get( 0 ) );
                                                   updateCurrentContactFeast();
                                                   nextOrExit();
                                               }
                                               else
                                               {
                                                   showDialog( EMAIL_CHOOSER_DIALOG_ID );
                                               }
                                           }
                                       }
                                   } );
                 return builder.create();
 
                 /* dialog which show the available phone number of the contact */
             case TEL_CHOOSER_DIALOG_ID:
                 builder = new AlertDialog.Builder( this );
                 builder.setItems( mCurrentContactFeast.getPhones().toArray( new String[] {} ),
                                   new DialogInterface.OnClickListener()
                                   {
                                       public void onClick( DialogInterface dialog, int item )
                                       {
                                           composeTel( mCurrentContactFeast.getPhones().get( item ) );
                                           updateCurrentContactFeast();
                                           nextOrExit();
                                       }
                                   } );
                 return builder.create();
 
                 /* dialog which show the available email adress of the contact */
             case SMS_CHOOSER_DIALOG_ID:
                 builder = new AlertDialog.Builder( this );
                 builder.setItems( mCurrentContactFeast.getPhones().toArray( new String[] {} ),
                                   new DialogInterface.OnClickListener()
                                   {
                                       public void onClick( DialogInterface dialog, int item )
                                       {
                                           composeSms( mCurrentContactFeast.getPhones().get( item ) );
                                           updateCurrentContactFeast();
                                           nextOrExit();
                                       }
                                   } );
                 return builder.create();
 
             case EMAIL_CHOOSER_DIALOG_ID:
                 builder = new AlertDialog.Builder( this );
                 builder.setItems( mCurrentContactFeast.getEmails().toArray( new String[] {} ),
                                   new DialogInterface.OnClickListener()
                                   {
                                       public void onClick( DialogInterface dialog, int item )
                                       {
                                           composeMail( mCurrentContactFeast.getEmails().get( item ) );
                                           updateCurrentContactFeast();
                                           nextOrExit();
                                       }
                                   } );
                 return builder.create();
         }
         return null;
     }
 
     private String getMailBody()
     {
         if ( mCurrentContactFeast.getNameDay().equals( BDAY_HINT ) )
         {
             return mPrefs.getString( PREF_MAIL_BIRTHDAY_BODY_TEMPLATE,
                                      getString( R.string.default_mail_birthday_body_template ) );
         }
         return mPrefs.getString( PREF_MAIL_BODY_TEMPLATE, getString( R.string.default_mail_body_template ) );
     }
 
     private String getMailSubject()
     {
         if ( mCurrentContactFeast.getNameDay().equals( BDAY_HINT ) )
         {
             return mPrefs.getString( PREF_MAIL_BIRTHDAY_SUBJECT_TEMPLATE,
                                      getString( R.string.default_mail_birthday_subject_template ) );
         }
         return mPrefs.getString( PREF_MAIL_SUBJECT_TEMPLATE, getString( R.string.default_mail_subject_tempate ) );
     }
 
     private String getSmsBody()
     {
         if ( mCurrentContactFeast.getNameDay().equals( BDAY_HINT ) )
         {
             return mPrefs.getString( PREF_SMS_BIRTHDAY_BODY_TEMPLATE,
                                      getString( R.string.default_sms_birthday_body_template ) );
         }
         return mPrefs.getString( PREF_SMS_BODY_TEMPLATE, getString( R.string.default_sms_body_template ) );
     }
 
     private void composeMail( String emailAddress )
     {
         AndroidUtils.composeMail( this, emailAddress, getMailSubject(), getMailBody() );
     }
 
     private void composeSms( String phoneNumber )
     {
         AndroidUtils.composeSms( this, phoneNumber, getSmsBody() );
     }
 
     private void composeTel( String phoneNumber )
     {
         AndroidUtils.composeTel( this, phoneNumber );
     }
 
     private void updateFeastCounter()
     {
         if ( mContactFeasts.getContactList().size() > 1 )
         {
             mFeastCounter
                 .setText( getString( R.string.feast_counter, ++mIndex, mContactFeasts.getContactList().size() ) );
         }
     }
 
     /**
      * Boucle sur les contacts a afficher
      */
     private void nextOrExit()
     {
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: nextOrExit ?" );
         }
         if ( !contacts.hasNext() )
         {
             // no more contact -> exit
             if ( Log.DEBUG )
             {
                 Log.v( "ReminderPopupActivity: no more contact in the list, we exit" );
             }
             exit();
             return;
         }
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: next" );
         }
         Map.Entry<Long, ContactFeast> contact = contacts.next();
         if ( contact == null )
         {
             // null contact !        
             Log.e( "ReminderPopupActivity: null contact found in the list, we exit" );
             exit();
             return;
         }
 
         updateFeastCounter();
 
         /* have to remove the dialogs because their content can change between different contacts */
         removeDialog( EMAIL_CHOOSER_DIALOG_ID );
         removeDialog( HOW_TO_CONTACT_DIALOG_ID );
         removeDialog( TEL_CHOOSER_DIALOG_ID );
 
         /* display the contact */
         setContentForContact( contact.getKey(), contact.getValue() );
     }
 
     private void setContentForContact( final Long contactId, final ContactFeast contactFeast )
     {
         mCurrentContactFeast = contactFeast;
         contactFeast.setContactId( contactId );
 
         if ( contactFeast.getNameDay().equals( BDAY_HINT ) )
         {
             /* its a birthday */
             if ( contactFeast.getBirthdayYear() != null )
             {
                 /* year is provided, we can calculate the age */
                 int birtdayYear = Integer.parseInt( contactFeast.getBirthdayYear() );
                 mNameDayTitle.setText( getString( R.string.age, Integer.valueOf( mCurrentYear - birtdayYear ) ) );
             }
             else
             {
                 mNameDayTitle.setText( R.string.age_unknow );
             }
 
             mHappyFeastText.setText( R.string.happybirthday_name );
         }
         else
         {
             /* its a name day */
             mNameDayTitle.setVisibility( View.VISIBLE );
             mNameDayTitle.setText( getString( R.string.nameday, contactFeast.getNameDay() ) );
             mHappyFeastText.setText( R.string.happyfeast_name );
         }
 
         TextView contactNameTextView = (TextView) findViewById( R.id.contact_name );
         contactNameTextView.setText( contactFeast.getContactName() );
 
         Bitmap photo = ContactUtils.loadContactPhoto( this, contactId );
         ImageView imageView = (ImageView) findViewById( R.id.contact_photo );
         imageView.setBackgroundResource( android.R.drawable.picture_frame );
         imageView.setImageBitmap( photo );
 
         Button callButton = (Button) findViewById( R.id.call_button );
         callButton.setOnClickListener( new View.OnClickListener()
         {
             public void onClick( View v )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "ReminderPopup: call clicked" );
                 }
                 populateContactFeast( contactId, contactFeast );
                 if ( contactFeast.isContactable() )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.v( "ReminderPopup: " + contactFeast.getContactName() + " is contactable" );
                     }
                     showDialog( HOW_TO_CONTACT_DIALOG_ID );
                 }
                 else
                 {
                     /* no supported contact method, we display the contact */
                     if ( Log.DEBUG )
                     {
                         Log.v( "ReminderPopup: " + contactFeast.getContactName() + " is not contactable" );
                     }
                     Toast.makeText( ReminderPopupActivity.this, R.string.contact_method_not_supported,
                                     Toast.LENGTH_LONG ).show();
                    Uri displayContactUri = ContentUris.withAppendedId( ContactUtils.getContentUri(),
                                                                        contactId.intValue() );
                     Intent intent = new Intent( Intent.ACTION_VIEW, displayContactUri );
                     intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                     startActivity( intent );
                     /* insert to black list for this date */
                     boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), mDate );
                     if ( !res )
                     {
                         Log.e( "Error insertBlackList with year " + mDate );
                     }
                     nextOrExit();
                 }
             }
         } );
 
         Button laterButton = (Button) findViewById( R.id.later_button );
         laterButton.setOnClickListener( new View.OnClickListener()
         {
             public void onClick( View v )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "ReminderPopup: later clicked" );
                 }
                 // we keep notification in place if 'later' is clicked
                 //Toast.makeText( ReminderPopupActivity.this, R.string.toast_later, Toast.LENGTH_LONG ).show();
                 mKeepNotif = true;
                 nextOrExit();
             }
         } );
 
         Button neverButton = (Button) findViewById( R.id.never_button );
         neverButton.setOnClickListener( new View.OnClickListener()
         {
             public void onClick( View v )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "ReminderPopup: never clicked" );
                 }
                 boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), null );
                 if ( !res )
                 {
                     Log.e( "Error insertBlackList" );
                 }
                 else
                 {
                     Toast.makeText( ReminderPopupActivity.this,
                                     getString( R.string.toast_blacklisted, contactFeast.getContactName() ),
                                     Toast.LENGTH_LONG ).show();
                 }
                 nextOrExit();
             }
         } );
 
         Button exitButton = (Button) findViewById( R.id.nottoday_button );
         exitButton.setOnClickListener( new View.OnClickListener()
         {
             public void onClick( View v )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "ReminderPopup: exit clicked" );
                 }
                 boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), mDate );
                 if ( !res )
                 {
                     Log.e( "Error insertBlackList with year " + mDate );
                 }
                 nextOrExit();
 
             }
         } );
     }
 
     private void updateCurrentContactFeast()
     {
        boolean res = mDb.updateContactFeast( mCurrentContactFeast.getContactId(),
                                              mCurrentContactFeast.getContactName(), mDate );
         if ( !res )
         {
             Log.e( "Error insertBlackList with year " + mDate );
         }
     }
 
     private void populateContactFeast( Long contactId, ContactFeast contactFeast )
     {
        if ( !contactFeast.hasPhone() )
         {
             contactFeast.setPhones( ContactUtils.getContactPhones( this, contactId ) );
 
             if ( Log.DEBUG )
             {
                 Log.v( "ReminderPopup: " + contactFeast.getContactName() + " has " + contactFeast.getPhones().size()
                     + " phones" );
                 for ( String phone : contactFeast.getPhones() )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.v( "ReminderPopup: " + contactFeast.getContactName() + " - add a phone " + phone );
                     }
                 }
             }
         }
        if ( !contactFeast.hasEmail() )
         {
             contactFeast.setEmails( ContactUtils.getContactEmails( this, contactId ) );
 
             if ( Log.DEBUG )
             {
                 if ( Log.DEBUG )
                 {
                     Log.v( "ReminderPopup: " + contactFeast.getContactName() + " has "
                         + contactFeast.getEmails().size() + " emails" );
                 }
                 for ( String email : contactFeast.getEmails() )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.v( "ReminderPopup: " + contactFeast.getContactName() + " - add a email " + email );
                     }
                 }
             }
         }
     }
 
     private void exit()
     {
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: start exit" );
         }
         if ( !mKeepNotif )
         {
             NotificationManager nm = (NotificationManager) getSystemService( Activity.NOTIFICATION_SERVICE );
             nm.cancel( R.string.app_name );
             Toast.makeText( ReminderPopupActivity.this, R.string.toast_finish, Toast.LENGTH_LONG ).show();
         }
         finish();
     }
 
     @Override
     protected void onPause()
     {
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: onPause()" );
         }
         super.onPause();
     }
 
     @Override
     protected void onResume()
     {
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: onResume()" );
         }
         super.onResume();
         mDb.open( false );
     }
 
     @Override
     protected void onStop()
     {
         super.onStop();
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: start onStop" );
         }
         if ( mDb != null )
         {
             mDb.close();
         }
         if ( Log.DEBUG )
         {
             Log.v( "ReminderPopupActivity: end onStop" );
         }
     }
 }
