 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.events;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.kamosoft.happycontacts.Constants;
 import com.kamosoft.happycontacts.Log;
 import com.kamosoft.happycontacts.R;
 import com.kamosoft.happycontacts.contacts.ContactUtils;
 import com.kamosoft.happycontacts.model.ContactFeast;
 
 /**
  * @author tom
  *
  */
 public class EventArrayAdapter
     extends ArrayAdapter<ContactFeast>
     implements Constants
 {
     private Context mContext;
 
     private int mCurrentYear;
 
     /**
      * @param context
      * @param resource
      * @param textViewResourceId
      * @param objects
      */
     public EventArrayAdapter( Context context, int textViewResourceId, ArrayList<ContactFeast> users )
     {
         super( context, textViewResourceId, users );
         mContext = context;
         Calendar cal = Calendar.getInstance();
         mCurrentYear = cal.get( Calendar.YEAR );
     }
 
     /**
      * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
      */
     @Override
     public View getView( int position, View convertView, ViewGroup parent )
     {
         View view = convertView;
         if ( view == null )
         {
             LayoutInflater layoutInflater =
                 (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
             view = layoutInflater.inflate( R.layout.event_element, null );
         }
 
         ContactFeast user = getItem( position );
         if ( user != null )
         {
             TextView userNameText = (TextView) view.findViewById( R.id.contact_name );
             userNameText.setText( user.getContactName() );
 
             TextView eventType = (TextView) view.findViewById( R.id.event_type );
 
             if ( user.getNameDay().equals( BDAY_HINT ) )
             {
                 /* its a birthday */
                 if ( user.getBirthdayYear() != null )
                 {
                     /* year is provided, we can calculate the age */
                     int birtdayYear = Integer.parseInt( user.getBirthdayYear() );
                     eventType.setText( mContext.getString( R.string.age, Integer.valueOf( mCurrentYear - birtdayYear ) ) );
                 }
                 else
                 {
                     eventType.setText( R.string.age_unknow );
                 }
             }
             else
             {
                 /* its a nameday */
                 eventType.setText( mContext.getString( R.string.nameday, user.getNameDay() ) );
             }
             /* photo */
             try
             {
                 Bitmap photo = ContactUtils.loadContactPhoto( mContext, user.getContactId() );
                 ImageView imageView = (ImageView) view.findViewById( R.id.contact_photo );
                imageView.setBackgroundResource( android.R.drawable.picture_frame );
                 imageView.setImageBitmap( photo );
             }
             catch ( Exception e )
             {
                 Log.e( "impossible de charger la photo de " + user.toString() );
             }
         }
         return view;
     }
 }
