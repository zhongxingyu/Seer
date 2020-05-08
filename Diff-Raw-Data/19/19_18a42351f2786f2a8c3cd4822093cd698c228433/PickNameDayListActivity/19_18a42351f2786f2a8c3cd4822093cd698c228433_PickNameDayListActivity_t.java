 /**
  * Copyright (C) 2010 Kamosoft
  */
 package com.kamosoft.happycontacts;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kamosoft.happycontacts.dao.DbAdapter;
 import com.kamosoft.happycontacts.dao.HappyContactsDb;
 import com.kamosoft.utils.AndroidUtils;
 
 /**
  * After select the contact in PickContactListActivity, the user has to select a nameday from this Activity
  * @author tom
  * created 8 mars 2010
  */
 public class PickNameDayListActivity
     extends ListActivity
     implements Constants, TextWatcher, OnClickListener
 {
     private DbAdapter mDb;
 
     private String mContactName;
 
     private Long mContactId;
 
     private Cursor mCursor;
 
     private EditText mEditText;
 
     private Class<?> mNextActivity;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         if ( Log.DEBUG )
         {
             Log.v( "PickNameDayListActivity: start onCreate" );
         }
         super.onCreate( savedInstanceState );
         setContentView( R.layout.namedaylist );
 
         mContactId = getIntent().getExtras().getLong( CONTACTID_INTENT_KEY );
         mContactName = getIntent().getExtras().getString( CONTACTNAME_INTENT_KEY );
 
         /* optionnal extras for other way to work */
         mNextActivity = (Class<?>) getIntent().getExtras().getSerializable( NEXT_ACTIVITY_INTENT_KEY );
         String pickNameDayLabel = getIntent().getExtras().getString( PICK_NAMEDAY_LABEL_INTENT_KEY );
 
         TextView pickNameDayTextView = (TextView) findViewById( R.id.pick_nameday );
         if ( pickNameDayLabel == null )
         {
             pickNameDayTextView.setText( getString( R.string.pick_nameday, mContactName ) );
         }
         else
         {
             pickNameDayTextView.setText( pickNameDayLabel );
         }
 
         mEditText = (EditText) findViewById( R.id.autocomplete );
         mEditText.addTextChangedListener( this );
 
         mDb = new DbAdapter( this );
        
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: start onCreate" );
        }
    }
    
    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: start onResume" );
        }
        super.onResume();

         mDb.open( false );
 
         fillList( null );
         if ( Log.DEBUG )
         {
            Log.v( "PickNameDayListActivity: end onResume" );
         }
     }
 
     @Override
     protected void onStop()
     {
         if ( Log.DEBUG )
         {
             Log.v( "PickNameDayListActivity: start onStop" );
         }
         super.onStop();
         if ( mDb != null )
         {
             mDb.close();
         }
         if ( Log.DEBUG )
         {
             Log.v( "PickNameDayListActivity: end onStop" );
         }
     }
 
     private void fillList( String text )
     {
         if ( text == null || text.length() == 0 )
         {
             mCursor = mDb.fetchAllNameDay();
         }
         else
         {
             mCursor = mDb.fetchNameDayLike( text );
         }
         startManagingCursor( mCursor );
 
         String[] from = { HappyContactsDb.NameDay.NAME_DAY };
         int[] to = { android.R.id.text1 };
         SimpleCursorAdapter simpleAdapter =
             new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to );
         setListAdapter( simpleAdapter );
     }
 
     @Override
     protected void onListItemClick( ListView l, View v, int position, long id )
     {
         super.onListItemClick( l, v, position, id );
         onNameDayClick( position );
     }
 
     /**
      * return true if the contactName can normally be detected by application without being whitelised
      * @param contactName
      * @param nameDay
      * @return
      */
     private boolean canMatchNormally( String contactName, String nameDay )
     {
         nameDay = AndroidUtils.replaceAccents( nameDay ).toUpperCase();
         contactName = AndroidUtils.replaceAccents( contactName ).toUpperCase();
         for ( String subName : contactName.split( " " ) )
         {
             if ( subName.equals( nameDay ) )
             {
                 return true;
             }
         }
         return false;
     }
 
     private void onNameDayClick( int position )
     {
         mCursor.moveToPosition( position );
         String nameDay = mCursor.getString( mCursor.getColumnIndexOrThrow( HappyContactsDb.NameDay.NAME_DAY ) );
         if ( mNextActivity == null )
         {
             if ( canMatchNormally( mContactName, nameDay ) )
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder( this );
                 builder.setMessage( getString( R.string.can_match_normally, mContactName, nameDay ) );
                 builder.setNeutralButton( R.string.link_other, this ).setNegativeButton( R.string.cancel, this );
                 builder.create().show();
             }
             else
             {
                 mDb.insertWhiteList( mContactId, mContactName, nameDay );
                 Toast.makeText( this, getString( R.string.whitelist_added, mContactName, nameDay ), Toast.LENGTH_SHORT ).show();
                 returnToWhiteList();
             }
         }
         else
         {
             Intent intent = new Intent( this, DateListActivity.class );
             intent.putExtra( NAME_INTENT_KEY, nameDay );
             startActivity( intent );
         }
     }
 
     /**
      * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
      */
     public void afterTextChanged( Editable arg0 )
     {
         //nothing
     }
 
     /**
      * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
      */
     public void beforeTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
     {
         //nothing
     }
 
     /**
      * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
      */
     public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
     {
         String text = mEditText.getText().toString();
         fillList( text );
     }
 
     /**
      * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
      */
     public void onClick( DialogInterface dialog, int id )
     {
         switch ( id )
         {
             case DialogInterface.BUTTON_NEUTRAL:
                 dialog.dismiss();
                 mEditText.setText( null );
                 /* hide the keyboard */
                 InputMethodManager inputManager =
                     (InputMethodManager) this.getSystemService( Context.INPUT_METHOD_SERVICE );
                 inputManager.hideSoftInputFromWindow( this.getCurrentFocus().getWindowToken(),
                                                       InputMethodManager.HIDE_NOT_ALWAYS );
                 break;
 
             case DialogInterface.BUTTON_NEGATIVE:
                 dialog.dismiss();
                 returnToWhiteList();
                 break;
         }
     }
 
     private void returnToWhiteList()
     {
         Intent intent = new Intent( this, WhiteListActivity.class );
         intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
         startActivity( intent );
     }
 }
