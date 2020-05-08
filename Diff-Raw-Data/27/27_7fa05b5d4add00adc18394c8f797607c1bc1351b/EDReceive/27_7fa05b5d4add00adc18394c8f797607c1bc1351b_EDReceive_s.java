 import java.util.ArrayList;
 import java.util.Calendar.*;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 public class EDReceive
 {
   List<Sender> senders;
 
   public EDReceive()
   {
     senders = new ArrayList<Sender>();
   }
 
   private Sender senderExist( long senderID )
   {
     Sender sender = null;
     Iterator<Sender> it = senders.iterator();
 
     while( it.hasNext() && (null == sender ) )
     {
       Sender s = it.next();
 
       if( s.getID() == senderID )
         sender = s;
     }
 
     return sender;
   }
 
   public long getSeqNum( long senderID )
   {
     long sequenceNumber = Sender.INVALID;
     Sender s = senderExist( senderID );
 
     if( null != s )
       sequenceNumber = s.getSequenceNumber();
 
     return sequenceNumber;
   }
 
   public boolean updateSeqNum( long senderID, long sequenceNumber )
   {
     Sender s = senderExist( senderID );
 
     if( null != s )
     {
       s.setSequenceNum( sequenceNumber );
       return true;
     }
 
     return false;
   }
 
   public boolean addSender( long senderID )
   {
     Sender s = new Sender( senderID );
     senders.add( s );
     return true;
   }
 
   public class Sender
   {
     private long senderID;
     private long sequenceNumber;
     private Date timestamp;
 
     public static final long INVALID = -1;
 
     public Sender( long senderID, long sequenceNumber, Date timestamp )
     {
      this.senderID = SenderID;
       this.sequenceNumber = sequenceNumber;
       this.timestamp = timestamp;
     }
 
     public Sender( long senderID )
     {
 //      this( senderID, Sender.INVALID, Calendar.getInstance().getTime() ); // TODO: fix this and then uncomment it. 
 //commented because of error:
 //
 //EDReceive.java:80: cannot find symbol
 //symbol  : variable Calendar
 //location: class EDReceive.Sender
 //      this( senderID, Sender.INVALID, Calendar.getInstance().getTime() );
     }
 
     public long getID()
     {
       return senderID;
     }
 
    public long getsequenceNumber()
     {
       return sequenceNumber;
     }
 
     public void setSequenceNum( long sequenceNumber )
     {
       this.sequenceNumber = sequenceNumber;
     }
   }
 }
