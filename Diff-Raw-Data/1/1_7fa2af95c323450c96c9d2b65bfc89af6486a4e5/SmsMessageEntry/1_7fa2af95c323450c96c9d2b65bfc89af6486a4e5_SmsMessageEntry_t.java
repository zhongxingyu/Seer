 package com.phdroid.smsb.storage.dao;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.telephony.SmsMessage;
 import com.phdroid.smsb.SmsPojo;
 
 /**
  * DAO for SmsMessage
  */
 public class SmsMessageEntry extends SmsPojo {
     public static final String _ID = "_id";
     public static final String SENDER_ID = "sender_id";
     public static final String SENDER = "sender";
     public static final String MESSAGE = "message";
     public static final String RECEIVED = "received";
     public static final String READ = "read";
     public static final String USER_FLAG_NOT_SPAM = "not_spam_user"; //did user say this sms is NOT spam
 
     private int id;
     private int senderId;
 	private String message;
 	private long received;
     private boolean read;
 	private boolean markedNotSpamByUser;
 	private String sender;
 
 	public SmsMessageEntry() {
 	}
 
     public SmsMessageEntry(Cursor c) {
         this.id = c.getInt(c.getColumnIndex(SmsMessageEntry._ID));
         this.senderId = c.getInt(c.getColumnIndex(SmsMessageEntry.SENDER_ID));
 	    this.sender = c.getString(c.getColumnIndex(SmsMessageEntry.SENDER));
         this.message = c.getString(c.getColumnIndex(SmsMessageEntry.MESSAGE));
         this.received = c.getInt(c.getColumnIndex(SmsMessageEntry.RECEIVED));
         this.read = c.getInt(c.getColumnIndex(SmsMessageEntry.READ)) == 1;
         this.markedNotSpamByUser = c.getInt(c.getColumnIndex(SmsMessageEntry.USER_FLAG_NOT_SPAM)) == 1;
 	}
 
     SmsMessageEntry(SmsMessageSenderEntry sender, SmsMessage message) {
         this.senderId = sender.getId();
	    this.sender = sender.getValue();
         this.message = message.getMessageBody();
 		this.received = message.getTimestampMillis();
         this.read = false;
 	}
 
 	public ContentValues toContentValues() {
 		ContentValues values = new ContentValues();
 		values.put(SENDER_ID, this.getSenderId());
 		values.put(MESSAGE, this.getMessage());
 		values.put(RECEIVED, this.getReceived());
         values.put(READ, this.isRead());
 		values.put(USER_FLAG_NOT_SPAM, this.isMarkedNotSpamByUser());
 		return values;
 	}
 
     public int getId() {
         return id;
     }
 
     public int getSenderId() {
 		return senderId;
 	}
 
 	public void setSenderId(int senderId) {
 		this.senderId = senderId;
 	}
 
 	public String getSender() {
 		return sender;
 	}
 
 	public boolean isRead(){
         return read;
     }
 
     public void setRead(boolean r){
         read = r;
     }
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public long getReceived() {
 		return received;
 	}
 
 	public void setReceived(long received) {
 		this.received = received;
 	}
 
 	public boolean isMarkedNotSpamByUser() {
 		return markedNotSpamByUser;
 	}
 
 	public void setMarkedNotSpamByUser(boolean markedNotSpamByUser) {
 		this.markedNotSpamByUser = markedNotSpamByUser;
 	}
 
     @Override
     public String toString(){
         return getMessage();
     }
 }
