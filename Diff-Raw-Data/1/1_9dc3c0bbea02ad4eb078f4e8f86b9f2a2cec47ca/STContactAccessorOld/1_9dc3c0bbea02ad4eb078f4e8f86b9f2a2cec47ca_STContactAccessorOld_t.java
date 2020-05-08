 package com.scarredions.stab;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.provider.Contacts;
 
 @SuppressWarnings("deprecation")
 public class STContactAccessorOld extends STContactAccessor {
 
     /**
      * Selection for query to contacts for auto-complete
      */
     public static final String[] CONTACTS_PROJECTION = new String[] {
         Contacts.People._ID,
         Contacts.People.DISPLAY_NAME
     };        
     
     @Override
     public Cursor managedQuery(Activity activity) {
         return activity.managedQuery(
                 Contacts.People.CONTENT_URI,
                 CONTACTS_PROJECTION,
                 null,
                 null,
                 Contacts.People.DISPLAY_NAME);
     }
 
     @Override
     public Bitmap loadContactPhotoFromId(Context context, String contactId) {
         InputStream contactPhotoStream = Contacts.People.openContactPhotoInputStream(
                 context.getContentResolver(),
                 ContentUris.withAppendedId(
                         Contacts.People.CONTENT_URI,
                         Double.valueOf(contactId).longValue()));
         return BitmapFactory.decodeStream(contactPhotoStream);
     }
 
     @Override
     public int getIdColumnIndex(Cursor cursor) {
         return cursor.getColumnIndex(Contacts.People._ID);
     }
 
     @Override
     public int getNameColumnIndex(Cursor cursor) {
         return cursor.getColumnIndex(Contacts.People.DISPLAY_NAME);
     }
 
     @Override
     public String getDisplayNameColumnName() {
         return Contacts.People.DISPLAY_NAME;
     }
 
     @Override
     public Cursor query(ContentResolver content, String constraint, String[] args) {
         return content.query(
                 Contacts.People.CONTENT_URI, 
                 CONTACTS_PROJECTION,
                 constraint, 
                 args, 
                 Contacts.People.DISPLAY_NAME);        
     }
 
 }
