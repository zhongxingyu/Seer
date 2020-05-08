 package com.abid_mujtaba.fetchheaders.models;
 
 import com.abid_mujtaba.fetchheaders.Resources;
 
 import javax.mail.Flags;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Model representing a single Email object
  */
 
 public class Email
 {
     private Message mMessage;
     private Date mDate;
     private String mFrom;
     private String mSubject;
     private boolean mSeen;
 
     private boolean mToBeDeleted = false;
 
 
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM d - h:m a");
 
 
     public Email(Message message)   // An Email object is constructed as a wrapper around a Message object
     {
         mMessage = message;
 
         try
         {
             mDate = message.getSentDate();
             mSubject = message.getSubject();
             mSeen = message.isSet(Flags.Flag.SEEN);
 
             InternetAddress from = (InternetAddress) message.getFrom()[0];        // We get the first from Address, usually there is only one. We cast it as InternetAddress since that gives us more methods
 
             mFrom = from.getPersonal();
 
             if (mFrom == null) { mFrom = from.getAddress(); }       // If no personal name is associated with the sender we store its email address
         }
         catch (MessagingException e) { Resources.Loge("Exception while attempting to connect to mail server", e); }
     }
 
 
     public boolean seen() { return mSeen; }
 
     public String subject() { return mSubject; }
 
     public String from() { return mFrom; }
 
     public String date() { return sDateFormat.format(mDate); }
 
     public Message message() { return mMessage; }
 
     @Override
     public String toString()
     {
         return String.format("<Email - Date: %s - From: %s - Subject: %s", date(), from(), subject());
     }
 
     public void toggleDeletion() { mToBeDeleted = ! mToBeDeleted; }     // Toggles the ToBeDeleted flag
 
     public boolean isToBeDeleted() { return mToBeDeleted; }
 }
