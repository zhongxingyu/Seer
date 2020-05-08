 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.facebook;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.os.AsyncTask;
 
 import com.kamosoft.happycontacts.Constants;
 import com.kamosoft.happycontacts.Log;
 import com.kamosoft.happycontacts.R;
 import com.kamosoft.happycontacts.contacts.ContactUtils;
 import com.kamosoft.happycontacts.contacts.PhoneContact;
 import com.kamosoft.happycontacts.model.SocialNetworkUser;
 import com.kamosoft.utils.AndroidUtils;
 import com.nloko.simplyfacebook.net.FacebookRestClient;
 
 /**
  * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
  *
  * @since 3 mai 2010
  * @version $Id$
  */
 public class FacebookContactSync
     extends AsyncTask<Void, String, List<SocialNetworkUser>>
     implements Constants
 {
     private FacebookActivity mContext;
 
     public FacebookContactSync( FacebookActivity context )
     {
         mContext = context;
     }
 
     /**
      * @see android.os.AsyncTask#doInBackground(Params[])
      */
     @Override
     protected List<SocialNetworkUser> doInBackground( Void... voids )
     {
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: Start getUserInfo" );
         }
         FacebookRestClient client =
             new FacebookRestClient( FACEBOOK_API_KEY, mContext.getSharedPreferences( APP_NAME, 0 ).getString( "uid",
                                                                                                               null ),
                                     mContext.getSharedPreferences( APP_NAME, 0 ).getString( "session_key", null ),
                                     mContext.getSharedPreferences( APP_NAME, 0 ).getString( "secret", null ) );
 
         FacebookApi api = new FacebookApi( client );
 
         ArrayList<SocialNetworkUser> users;
         try
         {
             users = api.getUserInfo( api.getFriends() );
         }
         catch ( Exception e )
         {
             Log.e( "FacebookContactSync: Error during sync : " + e.getMessage() == null ? e.getClass().getName()
                             : e.getMessage() );
             return Collections.<SocialNetworkUser> emptyList();
         }
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: End getUserInfo" );
         }
         if ( users == null || users.isEmpty() )
         {
             Log.e( "FacebookContactSync: no facebook friends found" );
             return users;
         }
 
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: Start loading contacts phones" );
         }
         ArrayList<PhoneContact> phoneContacts = ContactUtils.loadPhoneContacts( mContext );
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: end loading contacts phones" );
         }
 
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: Start matching with contacts" );
         }
         for ( SocialNetworkUser user : users )
         {
            if ( user.birthday == null )
             {
                 if ( Log.DEBUG )
                 {
                     Log.d( "FacebookContactSync: skipping user " + user.name + " no birthday provided" );
                 }
                 continue;
             }
             if ( Log.DEBUG )
             {
                 Log.d( "FacebookContactSync: searching contact for user " + user.name );
             }
             publishProgress( mContext.getString( R.string.sync_friends, user.name ) );
 
             String friendName = AndroidUtils.replaceAccents( user.name );
             for ( PhoneContact phoneContact : phoneContacts )
            {              
                if ( phoneContact.name == null || phoneContact.name.length() == 0 )
                {                    
                     continue;
                 }
                 if ( nameMatch( AndroidUtils.replaceAccents( phoneContact.name ), friendName ) )
                 {
                     if ( Log.DEBUG )
                     {
                         Log.d( "FacebookContactSync: *** " + phoneContact.name + " match with " + user.name + " ***" );
                     }
                     /* user FB trouv dans les contacts */
                     user.setContactId( phoneContact.id );
                     user.setContactName( phoneContact.name );
                     break;
                 }
             }
         }
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: Stop matching with contacts" );
         }
        publishProgress( mContext.getString( R.string.inserting_results )  );
         return users;
     }
 
     /**
      * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
      */
     @Override
     protected void onPostExecute( List<SocialNetworkUser> result )
     {
         mContext.finishSync( result );
     }
 
     /**
      * @see android.os.AsyncTask#onProgressUpdate(Progress[])
      */
     @Override
     protected void onProgressUpdate( String... values )
     {
         if ( Log.DEBUG )
         {
             Log.v( "FacebookContactSync: " + values[0] );
         }
         mContext.getProgressDialog().setMessage( values[0] );
     }
 
     private boolean nameMatch( String contactName, String userName )
     {
         if ( contactName == null || contactName.length() == 0 || userName == null || userName.length() == 0 )
         {
             return false;
         }
         String[] contactSubNames = contactName.split( " " );
         String[] userSubNames = userName.split( " " );
         boolean subNameFound = false;
         for ( String userSubName : userSubNames )
         {
             for ( String contactSubName : contactSubNames )
             {
                 if ( userSubName.equalsIgnoreCase( contactSubName ) )
                 {
                     subNameFound = true;
                     break;
                 }
             }
             if ( !subNameFound )
             {
                 return false;
             }
             subNameFound = false;
         }
         return true;
     }
 }
