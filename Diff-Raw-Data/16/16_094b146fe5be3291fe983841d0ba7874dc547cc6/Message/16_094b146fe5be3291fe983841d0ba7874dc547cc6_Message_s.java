 package edu.berkeley.cs.cs162;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TreeSet;
 
class Message {
     public String receiver;
     public TreeSet<String> receivingUsers;
     public ChatUser sender;
     public String senderName;
     public String text;
     public Date date;
     public int sqn;
 
     Message(Date date, ChatUser sender, String receiver, int sqn, String text) {
         this.receiver = receiver;
         this.sender = sender;
         this.senderName = sender.getUserName();
         this.text = text;
         this.date = date;
         this.sqn = sqn;
     }
 
     Message(ChatUser sender, String receiver, int sqn, String text) {
         this(Calendar.getInstance().getTime(), sender, receiver, sqn, text);
     }
 
     Message(Date date, String sender, String receiver, int sqn, String text) {
         this.receiver = receiver;
         this.senderName = sender;
         this.text = text;
         this.date = date;
         this.sqn = sqn;
     }
 
     Message(Date date, String sender, String receiver, TreeSet<String> receivingUsers, int sqn, String text) {
         this.receiver = receiver;
         this.receivingUsers = receivingUsers;
         this.senderName = sender;
         this.text = text;
         this.date = date;
         this.sqn = sqn;
     }
 
     Message(String receiver, int sqn, String text) {
         this(null, receiver, sqn, text);
     }
    
     /* Returns message in printable string format. SRC DST TIMESTAMP_UNIXTIME MSG SQN. */
     public String toString() {
         return senderName + " " + receiver + " " + Long.toString(date.getTime() / 1000L) + " " + Integer.toString(sqn) + " " + text;
     }
 }
