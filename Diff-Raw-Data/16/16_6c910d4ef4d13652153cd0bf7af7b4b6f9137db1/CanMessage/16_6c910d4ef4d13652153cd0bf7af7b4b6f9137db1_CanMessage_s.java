 package cansocket;
 
 import java.io.*;
 
 public class CanMessage
 {
     /* match the C structure */
     protected int fifo_tag;
     protected int timestamp;	// 25us timestamp counter
     protected short id;		// id,rtr bit,length-of-body packed int 16bits
     protected byte body[];	// 8 bytes of body
 
     /* the really interesting id */
     protected int id11;		// 11-bit id
     protected int rtr;		// RTR bit
     protected int len;		// number of valid bytes in body
 
     // unused id for a stop sentinel
     public static final int STOP_ID = 0x07ff;
 
     /* size in bytes of can message components */
     public static final int MSG_TS = 4; 	// length of timestamp
     public static final int MSG_ID = 2; 	// length of id
     public static final int MSG_BODY = 8; 	// length of body
     // total size includes 4 for fifo heading
     public static final int MSG_SIZE = 4 + MSG_TS + MSG_ID + MSG_BODY; 
 
     protected static final long firstTime = System.currentTimeMillis();
     protected static long lastTime = 0;
     protected static byte subMilli = 0;
 
     /* Construct a can message from given id, timestamp, body.
      * This is the packed 16-bit id containing (id11,rtr,len)
      */
     public CanMessage(short id16, int timestamp, byte body[])
     {
 	this.id = id16;
 	this.timestamp = timestamp;
 	this.body = body;
 
 	id11 = (int) (id16 >> 5);          // 11-bit id
 	rtr = (byte) ((id16 & 0x10) >> 4); // RTR bit
 	len = (byte) (id16 & 0xf);         // number of valid bytes in body
     }
 
     /* Construct a can message from given (timestamp,id,rtr,len,body)
      * This id is the 11-bit id, which will be packed into the 16-bit
      * id along with rtr and len.
      */
     public CanMessage(int timestamp, int id11, int rtr, int len, byte body[])
     {
 	this.timestamp = timestamp;
 	this.body = body;
 
 	this.id11 = id11;
 	this.rtr = (byte) rtr;
 	this.len = (byte) len;
 
 	// my compiler requires the result to be int
 	int temp = (id11 << 5) | (rtr << 4) | (len & 0xf);
 	this.id = (short) temp;
     }
 
     /**************************************** debugging */
     public CanMessage( short id )
     {
 	this (id, 99, new byte[MSG_BODY]);
     }
     /**************************************/
 
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
 
     /* Construct a can message from an array of bytes, which is
      * how it arrives on a udp socket.
      * Actually, its a fifo_msg that arrives so we strip the fifo_msg
      * header and extract the Can message.
      */
     public CanMessage( byte buf[] )
     {
 	DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
 
 	try
 	{
	    fifo_tag = dis.readInt();
 	    timestamp = dis.readInt();
	    id = (short) ((dis.readByte() & 0xff) | (dis.readByte() << 8));
 	    body = new byte[MSG_BODY];
 	    dis.read( body );
 	} catch(IOException e) {
 	    // never happens.
 	}
 
 	// cuisinart the bits
	id11 = id & 0x7ff; // 11-bit id
	rtr = (id >>> 11) & 0x1;   // RTR bit
	len = (id >>> 12) & 0xf;           // number of valid bytes in body
     }
 
     public byte[] toByteArray()
     {
 	ByteArrayOutputStream bos = new ByteArrayOutputStream(MSG_SIZE);
 	DataOutputStream dos = new DataOutputStream(bos);
 
 	try
 	{
 	    dos.writeInt(fifo_tag);
 	    dos.writeInt(timestamp);
 	    dos.writeShort((id11 << 5) | (rtr << 4) | len);
 	    dos.write(body);
 	} catch(IOException e) {
 	    // never happens.
 	}
 
 	return bos.toByteArray();
     }
 
     // this returns the 16-bit id that has id,rtr,len packed into it
     public short getId() {
 	return id;
     }
 
     // this returns the 11-bit Can bus id
     public int getId11() {
 	return id11;
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
 	StringBuffer buf = new StringBuffer( "0x" );
 
 	buf.append( Integer.toHexString (timestamp) + " " );
 	buf.append( Integer.toHexString (id11) + " " );
 	buf.append( Integer.toHexString (rtr) + " " );
 	buf.append( Integer.toHexString (len) + " " );
 	for (int i = 0; i < len; i++) {
 	    buf.append( hexByte (body[i]) + " " );
 	}
 
 	return (new String( buf.toString() ));
     }
 
     /* format a byte into 2 hex digits
      */
     public static String hexByte( byte byt )
     {
 	StringBuffer buf = new StringBuffer( " " );
 	int index;
 
 	index = byt & 0xff;
 	if( index < 0x10 ) buf.append( "0" );
 	buf.append( Integer.toHexString (index) );
 
 	return( new String( buf ));
     }
 
 } // end class CanMessage
