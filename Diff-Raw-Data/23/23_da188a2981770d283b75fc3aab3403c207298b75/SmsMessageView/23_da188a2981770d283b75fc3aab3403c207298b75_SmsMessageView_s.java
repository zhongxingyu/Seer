 package com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly;
 
 import android.content.ContentUris;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.provider.ContactsContract;
 
 import com.androidproductions.libs.sms.SmsMessage;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
 
 import java.io.InputStream;
 import java.util.Calendar;
 
 public class SmsMessageView extends SmsMessage implements IMessageView{
     public final Context mContext;
     private String Name;
     int Type;
     public int Read;
     private int Locked;
     public long ThreadId;
     public Uri uri;
     public int SummaryCount;
     private long ContactID;
 
     public SmsMessageView(final Context con, final Cursor c)
     {
         super(c.getString(c.getColumnIndex("body")),
                 c.getString(c.getColumnIndex("address")),
                 c.getLong(c.getColumnIndex("date")),
                 c.getLong(0));
         mContext = con;
         Type = c.getInt(c.getColumnIndex("type"));
         ThreadId = c.getLong(c.getColumnIndex("thread_id"));
         SummaryCount = 0;
         Read = c.getInt(c.getColumnIndex("read"));
         Locked = c.getInt(c.getColumnIndex("locked"));
         uri = ContentUris.withAppendedId(SmsUri.BASE_URI, Id);
         //findName();
     }
 
     public SmsMessageView(final Context con, final Cursor c,final long contact)
     {
         this(con, c);
         ContactID = contact;
     }
 
     public SmsMessageView(final Context context, final String address, final String message, final long time) {
         super(message,address,time);
         mContext = context;
         //findName();
     }
 
     public boolean IsIncoming()
     {
         return Type == 1;
     }
 
     public String getContactName()
     {
         if (Name == null)
             findName();
         return Name;
     }
 
     @Override
     public Bitmap getContactPhoto() {
         if (IsIncoming())
             return getContactImage(ContactID);
         else
             return getContactImage(999999L);
     }
 
     @Override
     public Bitmap getConversationContactImage() {
         return getContactImage(ContactID);
     }
 
     Bitmap getContactImage(final long id)
     {
         Bitmap image = ImageCache.getItem(id);
         if (image != null)
             return image;
         image = _getContactImage(id == 999999L ?
                 ContactsContract.Profile.CONTENT_URI :
                 ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
         if (image == null)
             return ImageCache.getDefault();
         ImageCache.putItem(id, image);
         return image;
     }
 
     private Bitmap _getContactImage(final Uri uri)
     {
         try
         {
             final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                     mContext.getContentResolver() ,uri);
             if (input != null)
             {
                 final Bitmap img = BitmapFactory.decodeStream(input);
                 input.close();
                 return img;
             }
         }
         catch(Exception ex)
         {
         }
         return null;
     }
 
     final void findName() {
         Name = Addresses[0];
         try
         {
             final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(Addresses[0]));
             final Cursor c = mContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},null,null,null);
             if (c != null)
             {
                 if (c.moveToFirst())
                 {
                     Name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                     ContactID = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
                 }
                 c.close();
             }
         }
         catch(Exception e){ e.printStackTrace(); }
     }
 
     public String GetDateString() {
         final java.util.Date date = new java.util.Date(Date);
         final java.text.DateFormat dateFormat =
                 android.text.format.DateFormat.getDateFormat(mContext);
         final java.text.DateFormat timeFormat =
                 android.text.format.DateFormat.getTimeFormat(mContext);
         return dateFormat.format(date) + " " + timeFormat.format(date);
     }
 
     public String GetShortDateString() {
         final java.util.Date date = new java.util.Date(Date);
         final Calendar c = Calendar.getInstance();
         c.add(Calendar.DATE, -1);  // number of days to add
         if (date.before(c.getTime()))
             return android.text.format.DateFormat.getDateFormat(mContext).format(date);
         else
             return android.text.format.DateFormat.getTimeFormat(mContext).format(date);
     }
 
     public String getAddress() {
         return Addresses[0];
     }
 
     public boolean isUnread() {
         return Read != 1;
     }
 
     public boolean isLocked() {
         return Locked == 1;
     }
 
     public IMessageView getPrevious() {
        final Cursor c = mContext.getContentResolver().query(SmsUri.SENT_URI, null, "thread_id = ?",
                new String[] { String.valueOf(getThreadId()) }, "date DESC");
         if (c != null) {
             if (c.moveToFirst())
             {
                 final SmsMessageView message = new SmsMessageView(mContext, c);
                 c.close();
                 return message;
             }
             c.close();
         }
         return null;
     }
 
     public boolean sendingFailed() {
         return Type == 5;
     }
 
     public Uri getUri() {
         return uri;
     }
 
     @Override
     public long getThreadId() {
        return super.getThreadId();
     }
 }
