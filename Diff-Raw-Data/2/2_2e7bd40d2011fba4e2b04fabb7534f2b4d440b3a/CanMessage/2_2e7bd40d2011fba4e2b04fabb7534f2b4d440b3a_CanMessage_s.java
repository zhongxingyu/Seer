 package cansocket;
 
 import java.io.*;
 
 public class CanMessage extends NetMessage
 {
 	public static final short fifo_tag = FMT_CAN;
     /* match the C structure */
     protected int timestamp;	// 25us timestamp counter
     protected short id;		// id,rtr bit,length-of-body packed int 16bits
     protected byte body[];	// 8 bytes of body
 
     /* the really interesting id */
 	public int getId11() { return (id >>> 5) & 0x7ff; }	// 11-bit id
 	public int getRtr() { return (id >>> 4) & 1; }	// RTR bit
 	public int getLen() { return id & 0xf; }   // number of valid bytes in body
 
     // unused id for a stop sentinel
     public static final int STOP_ID = 0x07ff;
 
     /* size in bytes of can message components */
     public static final int MSG_TS = 4; 	// length of timestamp
     public static final int MSG_ID = 2; 	// length of id
     public static final int MSG_BODY = 8; 	// length of body
     // total size
     public static final int MSG_SIZE = MSG_TS + MSG_ID + MSG_BODY; 
 
     protected static final long firstTime = System.currentTimeMillis();
     protected static long lastTime = 0;
     protected static byte subMilli = 0;
 	
     /* Construct a can message from a data input stream,
 	 * which is wrapping the payload of a network packet.
 	 * The header (version, size, type) has already been read.
      */
 	public CanMessage(DataInputStream dis) throws IOException
 	{
 		this.timestamp = dis.readInt();
 		this.id = dis.readShort();
 		this.body = new byte[8];
 		if (getLen() > 0)					// is there data?
 			dis.read(body);
 	}
 
     /* Construct a can message from given id, timestamp, body.
      * This is the packed 16-bit id containing (id11,rtr,len)
      */
     public CanMessage(int id16, int timestamp, byte body[])
     {
 	this.id = (short)id16;
 	this.timestamp = timestamp;
 	this.body = body;
     }
 
     /* Construct a can message from given (timestamp,id,rtr,len,body)
      * This id is the 11-bit id, which will be packed into the 16-bit
      * id along with rtr and len.
      */
     public CanMessage(int timestamp, int id11, int rtr, int len, byte body[])
     {
 	this.timestamp = timestamp;
 	this.body = body;
 
 	// my compiler requires the result to be int
 	int temp = (id11 << 5) | (rtr << 4) | (len & 0xf);
 	this.id = (short) temp;
     }
 
     /* construct a can message from given id and body
      * and a ??? timestamp
      */
 
     /******
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
     *******************/
 	
     public byte[] toByteArray()
     {
 	ByteArrayOutputStream bos = new ByteArrayOutputStream(HEADER_SIZE+MSG_SIZE);
 	DataOutputStream dos = new DataOutputStream(bos);
 	putMessage(dos);
 	return bos.toByteArray();
 	}
 
 	public void putMessage(DataOutputStream dos)
 	{
 
 	try
 	{
 		// header: (move to NetMessage?)
 		dos.writeShort(FC_PROT_VER);
 		dos.writeShort(MSG_SIZE);
 	    dos.writeShort(fifo_tag);
 
 	    dos.writeInt(timestamp);
 	    dos.writeShort(id);
 	    dos.write(body);
 	    for(int i = body.length; i < 8; ++i)
 		dos.writeByte(0);
 	} catch(IOException e) {
 	    // never happens.
 	}
 
     }
 
     // this returns the 16-bit id that has id,rtr,len packed into it
     public short getId() {
 	return id;
     }
 
     public int getTimestamp() {
 	return timestamp;
     }
 
     public byte[] getBody() {
 	return body;
     }
 
     public byte getData8(int i) {
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
 
     /* print can message to standard output
      */
     public void print()
     {
 	System.out.println (this);
     }
 
     /* format can message to a string
      * All numeric values are shown in hex
      */
     public String toString()
     {
 	int len = getLen();
 	StringBuffer buf = new StringBuffer( "0x" );
 
 	buf.append( Integer.toHexString (getId11()) );
 	buf.append(" ").append( Integer.toString (getRtr()) );
 	buf.append(" ").append( Integer.toString (len) );
 	for (int i = 0; i < len; i++)
	    buf.append( " " ).append(Integer.toHexString(body[i]>>4)).append(Integer.toHexString(body[i]&15));
 
 	return buf.toString();
     }
 
 } // end class CanMessage
