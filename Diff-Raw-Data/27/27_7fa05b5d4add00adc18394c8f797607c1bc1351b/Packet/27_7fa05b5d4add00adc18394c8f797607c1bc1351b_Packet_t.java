import java.sql.Date;

 public class Packet
 {
   private char packetType;
   private String senderID;
   private double seqNumber;
   private Date interval;
 
   public Packet(char packetType, String senderID, double seqNumber, Date interval)
   {
     this.packetType = packetType;
     this.senderID = senderID;
     this.seqNumber = seqNumber;
     this.interval = interval;
   }
 
   public char getPacketType(){
     return packetType;
   }
 
   public String getSenderID(){
     return senderID;
   }
 
   public double getSeqNum(){
     return seqNumber;
   }
 
   public Date getInterval(){
     return interval;
   }
 }
