 package cansocket;
 
 public class CanMessage
 {
   protected short id;
   protected int timestamp;
   protected byte body[];
 
   public static final int MSG_LEN = 8; 
 
   protected static final long firstTime = System.currentTimeMillis();
   protected static long lastTime = 0;
   protected static byte subMilli = 0;
 
   public CanMessage(short id, int timestamp, byte body[])
   {
     this.id = id;
     this.timestamp = timestamp;
     this.body = body;
   }
   
   public CanMessage(short id, byte body[])
   {
     long time = System.currentTimeMillis() - firstTime;
     if(time > lastTime)
       subMilli = 0;
 		lastTime = time;
 		timestamp = ((int) time << 4) | subMilli;
 		++subMilli;
 
     this.id = id;
     this.body = body;
   }
 
   public short getId()
   {
     return id;
   }
 
   public int getTimestamp()
   {
     return timestamp;
   }
 
   public byte[] getBody()
   {
     return body;
   }
 
   public byte getData8(int i)
   {
     return body[i];
   }
 
   public short getData16(int i)
   {
     i <<= 1;
    return (short) (body[i] << 8 | (body[i + 1] & 0xff));
   }
 
   public int getData32(int i)
   {
     i <<= 2;
    return (body[i] & 0xff) << 24 |
           (body[i + 1] & 0xff) << 16 |
           (body[i + 2] & 0xff) << 8 |
           (body[i + 3] & 0xff);
   }
 }
