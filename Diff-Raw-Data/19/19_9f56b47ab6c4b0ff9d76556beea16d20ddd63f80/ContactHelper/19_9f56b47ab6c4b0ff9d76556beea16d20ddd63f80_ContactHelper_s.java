 package com.androidproductions.ics.sms.data;
 
 import android.content.ContentUris;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.support.v4.content.CursorLoader;
 import android.util.LruCache;
 
 import com.androidproductions.ics.sms.R;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ContactHelper {
 	private final Context mContext;
 	private Long mId;
 
     public ContactHelper(Context context, Long id)
     {
         mContext = context;
         mId = id;
     }
 
     public void setId(Long id)
     {
         mId = id;
     }
 
     public ContactHelper(Context context)
     {
         mContext = context;
     }
 
     public Bitmap getContactImage(LruCache<Long,Bitmap> cache)
     {
         Bitmap image = cache.get(mId);
         if (image != null)
             return image;
         image = _getContactImage();
         if (image == null)
         {
             image = cache.get(0L);
             return image == null ? getDefaultBitmap() : image;
         }
         cache.put(mId,image);
         return image;
     }
 
     public Bitmap getProfileContactImage(LruCache<Long,Bitmap> cache)
     {
        Bitmap image = cache.get(mId);
         if (image != null)
             return image;
         image = _getProfileContactImage();
         if (image == null)
         {
             image = cache.get(0L);
             return image == null ? getDefaultBitmap() : image;
         }
        cache.put(mId,image);
         return image;
     }
 
     private Bitmap getDefaultBitmap() {
         return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture);
     }
 
     public Bitmap getContactImage()
     {
         Bitmap img = _getContactImage();
         return img == null ? getDefaultBitmap() : img;
     }
 
     public Bitmap getProfileContactImage()
     {
         Bitmap img = _getContactImage();
         return img == null ? getDefaultBitmap() : img;
     }
 
     private Bitmap _getContactImage()
     {
         try
         {
             Uri mContactLookupUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, mId);
             if (mContactLookupUri != null)
             {
                 InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(),mContactLookupUri);
                 if (input != null)
                 {
                     Bitmap img = BitmapFactory.decodeStream(input);
                     input.close();
                     return img;
                 }
             }
         }
         catch(Exception ex)
         {
         }
         return null;
     }
 
     private Bitmap _getProfileContactImage()
     {
         try
         {
             Uri mContactLookupUri = ContactsContract.Profile.CONTENT_URI;
             if (mContactLookupUri != null)
             {
                 InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(),mContactLookupUri);
                 if (input != null)
                 {
                     Bitmap img = BitmapFactory.decodeStream(input);
                     input.close();
                     return img;
                 }
             }
         }
         catch(Exception ex)
         {
         }
         return null;
     }
 
     public String getName()
     {
         final Cursor contact = mContext.getContentResolver().query(
             ContactsContract.Contacts.CONTENT_URI,
             null,
             ContactsContract.Contacts._ID + "=?",    // filter entries on the basis of the contact id
             new String[]{String.valueOf(mId)},    // the parameter to which the contact id column is compared to
             null);
         if (contact != null)
         {
             String name = null;
             if(contact.moveToFirst()) {
                 name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
             }
             contact.close();
             return name;
         }
         return null;
     }
 
     public String getContactName(final String phoneNumber)
     {
         String name = null;
         Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
         if (uri != null)
         {
             Cursor c = mContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},null,null,null);
             if (c != null)
             {
                 if (c.moveToFirst())
                 {
                     name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                     mId = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
                 }
                 c.close();
             }
         }
         return name;
     }
 
     public Uri getContactUri()
     {
         if (mId == null) return null;
         return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,mId);
     }
 
     public List<Contact> getAllPhoneNumbers()
     {
         List<Contact> res = new ArrayList<Contact>();
         Cursor phones = getPhoneCursor().loadInBackground();
         if (phones != null)
         {
             int nameField = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
             int numberField = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
             while (phones.moveToNext())
             {
                 res.add(new Contact(phones.getString(nameField),phones.getString(numberField)));
             }
             phones.close();
         }
         return res;
     }
 
     public CursorLoader getPhoneCursor()
     {
         String[] projection = new String[]{
             ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                 ContactsContract.CommonDataKinds.Phone.NUMBER,
                 ContactsContract.CommonDataKinds.Phone._ID,
                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                 ContactsContract.CommonDataKinds.Phone.TYPE,
                 ContactsContract.CommonDataKinds.Phone.LABEL
         };
         return new CursorLoader(mContext, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
             projection,
             null, null,
             ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
     }
 }
