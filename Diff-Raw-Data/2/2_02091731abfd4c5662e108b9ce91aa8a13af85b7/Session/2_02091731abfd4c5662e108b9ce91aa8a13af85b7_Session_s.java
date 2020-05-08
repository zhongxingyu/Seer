 package com.phdroid.smsb.storage.dao;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.telephony.SmsMessage;
 import com.phdroid.smsb.SmsPojo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * CRUD operations manager.
  */
 public class Session {
 	private ContentResolver contentResolver;
 
 	public ContentResolver getContentResolver() {
 		return contentResolver;
 	}
 
 	public Session(ContentResolver contentResolver) {
 		this.contentResolver = contentResolver;
 	}
 
 	public List<SmsPojo> getSmsList() {
 		List<SmsPojo> items = new ArrayList<SmsPojo>();
 		Cursor cursor = contentResolver.query(SmsContentProvider.CONTENT_URI, null, null, null, null);
 		try {
 			int size = cursor.getCount();
 			if (size == 0) {
 				return items;
 			}
 			for (int i = 0; i < size; i++) {
 				cursor.move(i);
 				SmsMessageEntry item = new SmsMessageEntry(cursor);
 				items.add(item);
 			}
 		} finally {
 			if (cursor != null && !cursor.isClosed()) {
 				cursor.close();
 			}
 		}
 		return items;
 	}
 
 	public SmsPojo getSms(long id) {
 		Cursor cursor = contentResolver.query(SmsContentProvider.getItemUri(id), null, null, null, null);
 		try {
 			int size = cursor.getCount();
 			if (size == 1) {
 				cursor.moveToFirst();
 				return new SmsMessageEntry(cursor);
 			} else {
 				return null;
 			}
 		} finally {
 			if (cursor != null && !cursor.isClosed()) {
 				cursor.close();
 			}
 		}
 	}
 
 	public boolean delete(SmsPojo sms) {
 		int res = contentResolver.delete(SmsContentProvider.getItemUri(sms), null, null);
 		return res == 1;
 	}
 
 	public boolean update(SmsPojo sms) {
 		SmsMessageEntry smsEntry = (SmsMessageEntry) sms;
 		int res = contentResolver.update(SmsContentProvider.getItemUri(sms), smsEntry.toContentValues(), null, null);
 		return res == 1;
 	}
 
 	public SmsMessageSenderEntry insertOrSelectSender(String sender) {
         //todo:check for transactions support in SQLite
         String[] values = {sender};
         Cursor c = null;
         try {
             c = contentResolver.query(SenderContentProvider.CONTENT_URI, null, SmsMessageSenderEntry.VALUE + " = :1", values, null);
             if (c.getCount() != 0) {
                 c.moveToFirst();
                 return new SmsMessageSenderEntry(c);
 
             } else {
                 SmsMessageSenderEntry s = new SmsMessageSenderEntry(sender);
                 contentResolver.insert(SenderContentProvider.CONTENT_URI, s.toContentValues());
                 c = contentResolver.query(SenderContentProvider.CONTENT_URI, null, SmsMessageSenderEntry.VALUE + " = :1", values, null);
                 c.moveToFirst();
                 return new SmsMessageSenderEntry(c);
 
             }
         } finally {
             if (c != null) {
                 c.close();
             }
         }
     }
 
     public SmsMessageSenderEntry getSenderById(int id) {
         //todo: replace by URI build
         String[] values = {(new Integer(id)).toString()};
         Cursor c = null;
         try {
             c = contentResolver.query(SenderContentProvider.CONTENT_URI, null, SmsMessageSenderEntry._ID + " = :1", values, null);
             c.moveToFirst();
             return new SmsMessageSenderEntry(c);
         } finally {
             if (c != null) {
                 c.close();
             }
         }
     }
 
     public SmsMessageEntry insertMessage(SmsMessage message) {
         String senderText = message.getOriginatingAddress();
         SmsMessageSenderEntry sender = this.insertOrSelectSender(senderText);
 
         SmsMessageEntry res = new SmsMessageEntry(sender, message);
        this.contentResolver.insert(SenderContentProvider.CONTENT_URI, res.toContentValues());
         return res;
     }
 }
