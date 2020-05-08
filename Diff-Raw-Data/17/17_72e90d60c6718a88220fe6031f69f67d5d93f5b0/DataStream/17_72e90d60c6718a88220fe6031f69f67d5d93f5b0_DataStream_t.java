 /*****************************************************************************
 
  *                                Waba Extras
 
  *
 
  * Version History
 
  * Date                Version  Programmer
 
  * ----------  -------  -------  ------------------------------------------
 
  * 17/10/1999  New      1.0.0    Rob Nielsen
 
  * Class created
 
  *
 
  ****************************************************************************/
 
 
 
 package extra.io;
 
 
 
 import waba.io.*;
 
 import waba.util.*;
 
 import waba.sys.*;
 
 
 
 /**
 
  * DataStream is a wrapper you can place around any Stream such as a
 
  * SerialPort, Catalog, or BufferStream which lets you read and write
 
  * standard Waba data types like ints, floats, and Strings in a simple
 
  * manner.  Here's an example
 
  * <pre>
 
  *   SerialPort port=new SerialPort(9600,0);
 
  *   DataStream ds=new DataStream(port);
 
  *   ds.writeString("Hello");
 
  *   int status=ds.readUnsignedByte();
 
  *   if (status==1)
 
  *   {
 
  *     ds.writeString("Pi");
 
  *     ds.writeFloat(3.14);
 
  *   }
 
  *   port.close();
 
  * </pre>
 
  */
 
 public class DataStream extends Stream
 
 {
 
   /** the underlying stream */
 
   protected Stream stream;
 
 
 
   /** a four byte array for reading and writing numbers */
 
   protected byte[] b=new byte[4];
 
 
 
   /** are we using big endian notation? */
 
   protected boolean bigEndian=true;
 
 
 
   /**
 
    * Constructs a new DataStream which sits upon the given stream using
 
    * big endian notation for multibyte values.
 
    * @param stream the base stream
 
    */
 
   public DataStream(Stream stream)
 
   {
 
     this(stream,true);
 
   }
 
 
 
   /**
 
    * Constructs a new DataStream which sits upon the given stream using
 
    * the given endian notation for multibyte values.
 
    * @param stream the base stream
 
    * @param bigEndian true for big endian, false for little endian
 
    */
 
   public DataStream(Stream stream,boolean bigEndian)
 
   {
 
     this.stream=stream;
 
     this.bigEndian=bigEndian;
 
   }
 
 
 
   /**
 
    * Sets whether numbers should be read and written in big endian format
 
    * (most significant byte first) or little endian format (least
 
    * significant byte first).  The default is true as this is the format
 
    * used by Java but if you are reading data from another source that uses
 
    * little endian you will need to set it to false.  If you are only
 
    * reading and writing your own data, this setting makes no difference,
 
    * assuming you set both the reading and writing ends the same.
 
    * @param bigEndian true for big endian, false for little endian
 
    */
 
   public void setBigEndian(boolean bigEndian)
 
   {
 
     this.bigEndian=bigEndian;
 
   }
 
 
 
   /**
 
    * Gets whether this DataStream is reading and writing numbers in
 
    * big endian format, or little endian format.
 
    * @return true for big endian, false for little endian
 
    */
 
   public boolean isBigEndian()
 
   {
 
     return bigEndian;
 
   }
 
 
 
   /**
 
 	 * Closes the stream. Returns true if the operation is successful
 
 	 * and false otherwise.
 
 	 */
 
 	public boolean close()
 
 	{
 
     return stream.close();
 
 	}
 
 
 
   /**
 
    * Reads an integer from the stream as four bytes.  The returned value
 
    * will range from -2147483648 to 2147483647.
 
    * @return the integer
 
    */
 
   public int readInt()
 
   {
 
     stream.readBytes(b,0,4);
 
 
 
     if (bigEndian)
 
       return (((b[0]&0xFF) << 24) | ((b[1]&0xFF) << 16) |
 
         ((b[2]&0xFF) << 8) | (b[3]&0xFF));
 
 
 
     return (((b[3]&0xFF) << 24) | ((b[2]&0xFF) << 16) |
 
       ((b[1]&0xFF) << 8) | (b[0]&0xFF));
 
   }
 
 
 
   /**
 
    * Writes an integer to the stream as four bytes.
 
    * @param i the integer to write
 
    */
 
   public void writeInt(int i)
 
   {
 
     for(int j=0;j<4;j++)
 
     {
 
       b[bigEndian?3-j:j]=(byte)(i&0xFF);
 
       i>>=8;
 
     }
 
     stream.writeBytes(b,0,4);
 
   }
 
 
 
   /**
 
    * Reads a short from the stream as two bytes.  The returned value will
 
    * range from -32768 to 32767.
 
    * @return the short
 
    */
 
   public short readShort()
 
   {
 
     return (short)readUnsignedShort();
 
   }
 
 
 
   /**
 
    * Writes an short to the stream as two bytes.  As there is no
 
    * short type in Waba but we often only want to only use two bytes in
 
    * storage.
 
    * an int is used but the upper two bytes are ignored.
 
    * @param i the short to write
 
    */
 
   public void writeShort(int i)
 
   {
 
     for(int j=0;j<2;j++)
 
     {
 
       b[bigEndian?1-j:j]=(byte)(i&0xFF);
 
       i>>=8;
 
     }
 
     stream.writeBytes(b,0,2);
 
   }
 
 
 
   /**
 
    * Reads an unsigned short from the stream as two bytes.  The returned
 
    * value will range from 0 to 65535.
 
    * @return the short
 
    */
 
   public int readUnsignedShort()
 
   {
 
     stream.readBytes(b,0,2);
 
     if (bigEndian)
 
       return (((b[0]&0xFF) << 8) | (b[1]&0xFF));
 
 
 
     return (((b[1]&0xFF) << 8) | (b[0]&0xFF));
 
   }
 
 
 
   /**
 
    * Reads a float value from the stream as four bytes in IEEE 754 format.
 
    * @return the float value
 
    */
 
   public float readFloat()
 
   {
 
     return Convert.toFloatBitwise(readInt());
 
   }
 
 
 
   /**
 
    * Writes a float value to the stream as four bytes in IEEE 754 format
 
    * @param f the float to write
 
    */
 
   public void writeFloat(float f)
 
   {
 
     writeInt(Convert.toIntBitwise(f));
 
   }
 
 
 
   /**
 
    * Reads a string stored in a pascal type format.  The first byte is
 
    * read as an unsigned byte.  If it is not 0xFF (255) the value is taken
 
    * as the length of the string.  If it is 0xFF, the next two bytes
 
    * are read and interpreted as a short giving the length of the string
 
    * to follow, which is then read.
 
    * @return the loaded string
 
    */
 
   public String readString()
 
   {
 
     int len=readUnsignedByte();
 
     if (len==0xFF)
 
       len=readShort();
 
     return readFixedString(len);
 
   }
 
 
 
   /**
 
    * Writes a string in a pascal type format.  If the length of the string
 
    * is less than 255, this length is written out as a byte.  If it is
 
    * greater or equal to 255 in length, 0xFF is written followed by a short
 
    * (two bytes) being the length.  The characters of the string follow.
 
    * @param s the string to write
 
    */
 
   public void writeString(String s)
 
   {
 
     int len=s!=null?s.length():0;
 
     if (len>=255)
 
     {
 
       writeByte((byte)0xFF);
 
       writeShort(len);
 
     }
 
     else
 
       writeByte((byte)len);
 
     writeFixedString(s,len);
 
   }
 
 
 
   /**
 
    * Reads a C-style string from the stream.  This is a NUL (0) terminated
 
    * series of characters.  This format is commonly used by other Palm
 
    * applications.
 
    * @returns the loaded String
 
    */
 
   public String readCString()
 
   {
 
     // if we've got a BufferStream under us, we can optimise the read.
 
     if (stream instanceof BufferStream)
 
     {
 
       byte[] b=((BufferStream)stream).readBytesUntil((byte)0);
 
       if (b==null)
 
         return "";
 
       readByte(); // clear the zero from the stream
 
       int size=b.length;
 
       char[] c=new char[size];
 
       for(int i=0;i<size;i++)
 
         c[i]=(char)b[i];
 
       b=null;
 
       return new String(c);
 
     }
 
 
 
     // otherwise we have to read one character at a time.
 
     int cnt=0;
 
     char[] c=new char[100];
 
     while (true)
 
     {
 
       stream.readBytes(b,0,1);
 
       if (b[0]==0)
 
         break;
 
       if (cnt>=c.length)
 
       {
 
         char[] temp=new char[c.length*2];
 
         Vm.copyArray(c,0,temp,0,cnt);
 
         c=temp;
 
       }
 
       c[cnt++]=(char)b[0];
 
     }
 
     return new String(c,0,cnt);
 
   }
 
 
 
   /**
 
    * Writes a C-style string to the stream.  This means that all the
 
    * characters of the string are written out, followed by a NUL (0)
 
    * character.  This format is commonly used by other Palm applications.
 
    * @param s the string to write
 
    */
 
   public void writeCString(String s)
 
   {
 
     if (s==null)
 
     {
 
       b[0]=0;
 
       stream.writeBytes(b,0,1);
 
       return;
 
     }
 
     int size=0;
 
     char[] c=s.toCharArray();
 
     size=c.length;
 
     byte[] b=new byte[size+1];
 
     for(int i=0;i<size;i++)
 
       b[i]=(byte)c[i];
 
     b[size]=0;
 
     stream.writeBytes(b,0,size+1);
 
   }
 
 
 
   /**
 
    * Reads a fixed length string from the stream.  The given number of
 
    * characters are read and converted to a String.
 
    * @param length the number of characters to read
 
    * @return the loaded string
 
    */
 
   public String readFixedString(int length)
 
   {
 
	  if(length == 0) return null;
	  
     char[] c=new char[length];
 
     byte[] b=new byte[length];
 
     stream.readBytes(b,0,length);
 
     for(int i=0;i<length;i++)
 
       c[i]=(char)b[i];
 
     return new String(c);
 
   }
 
 
 
   /**
 
    * Writes a fixed length string to the stream.  If the given string is
 
    * longer than the given length, it will be truncated and if it is shorter,
 
    * it will be padded with spaces.
 
    * @param s the string to write
 
    * @param length the length of the fixed string
 
    */
 
   public void writeFixedString(String s,int length)
 
   {
 
     writeFixedString(s,length,' ');
 
   }
 
 
 
   /**
 
    * Writes a fixed length string to the stream.  If the given string is
 
    * longer than the given length, it will be truncated and if it is shorter,
 
    * it will be padded the given pad character.
 
    * @param s the string to write
 
    * @param length the length of the fixed string
 
    * @param the character to pad if the string is shorter than the length
 
    */
 
   public void writeFixedString(String s,int length,char pad)
 
   {
 
     if (length==0)
 
       return;
 
     byte[] b=new byte[length];
 
     int slen=0;
 
     if (s!=null)
 
     {
 
       char[] c=s.toCharArray();
 
       slen=c.length;
 
       for(int i=0;i<slen&&i<length;i++)
 
         b[i]=(byte)c[i];
 
     }
 
     for(int i=slen;i<length;i++)
 
       b[i]=(byte)pad; // pad the rest
 
     stream.writeBytes(b,0,length);
 
   }
 
 
 
   /**
 
    * Reads a boolean from the stream as a byte.  True is returned if
 
    * the byte is not zero, false if it is.
 
    * @returns the boolean value read.
 
    */
 
   public boolean readBoolean()
 
   {
 
     stream.readBytes(b,0,1);
 
     return b[0]!=0;
 
   }
 
 
 
   /**
 
    * Writes a boolean to the stream as a byte.  True values are written as
 
    * 1 and false values as 0.
 
    * @param b the boolean to write
 
    */
 
   public void writeBoolean(boolean bool)
 
   {
 
     b[0]=(bool?(byte)1:(byte)0);
 
     stream.writeBytes(b,0,1);
 
   }
 
 
 
   /**
 
    * Reads a single byte from the stream.  The returned value will range
 
    * from -128 to 127.
 
    * @returns the read byte
 
    */
 
   public byte readByte()
 
   {
 
     stream.readBytes(b,0,1);
 
     return b[0];
 
   }
 
 
 
   /**
 
    * Writes a single byte to the stream.
 
    * @param b the byte to write
 
    */
 
   public void writeByte(byte by)
 
   {
 
     b[0]=by;
 
     stream.writeBytes(b,0,1);
 
   }
 
 
 
   /**
 
    * Writes a single byte to the stream.
 
    * @param b the byte to write (only least significant byte is written)
 
    */
 
   public void writeByte(int by)
 
   {
 
     b[0]=(byte)(by&0xFF);
 
     stream.writeBytes(b,0,1);
 
   }
 
 
 
   /**
 
    * Reads a single unsigned byte from the stream.  The returned value will
 
    * range from 0 to 255.
 
    * @returns the read byte.
 
    */
 
   public int readUnsignedByte()
 
   {
 
     stream.readBytes(b,0,1);
 
     return b[0]&0xFF;
 
   }
 
 
 
   /**
 
    * Skips reading the next n bytes in the stream
 
    * @param n the number of bytes to skip
 
    */
 
   public void skip(int n)
 
   {
 
     byte[] b=new byte[n];
 
     stream.readBytes(b,0,n);
 
   }
 
 
 
   /**
 
    * Reads bytes from the stream. Returns the
 
    * number of bytes actually read or -1 if an error prevented the
 
    * read operation from occurring.
 
    * @param buf the byte array to read data into
 
    * @param start the start position in the array
 
    * @param count the number of bytes to read
 
    */
 
   public int readBytes(byte buf[], int start, int count)
 
   {
 
     return stream.readBytes(buf,start,count);
 
   }
 
 
 
   /**
 
    * Writes bytes to the the stream. Returns the
 
    * number of bytes actually written or -1 if an error prevented the
 
    * write operation from occurring.
 
    * @param buf the byte array to write data from
 
    * @param start the start position in the byte array
 
    * @param count the number of bytes to write
 
    */
 
   public int writeBytes(byte buf[], int start, int count)
 
   {
 
     return stream.writeBytes(buf,start,count);
 
   }
 
 
 
   /**
 
    * Converts all characters in a string to upper case.
 
    * @param s the string to convert
 
    * @return an upper case version of the string
 
    */
 
   public static String toUpperCase(String s)
 
   {
 
     char[] c=s.toCharArray();
 
     for(int i=0;i<c.length;i++)
 
       if (c[i]>='a'&&c[i]<='z')
 
         c[i]-=32;
 
     return new String(c);
 
   }
 
 
 
   /**
 
    * Converts all characters in a string to lower case.
 
    * @param s the string to convert
 
    * @return a lower case version of the string
 
    */
 
   public static String toLowerCase(String s)
 
   {
 
     char[] c=s.toCharArray();
 
     for(int i=0;i<c.length;i++)
 
       if (c[i]>='A'&&c[i]<='Z')
 
         c[i]+=32;
 
     return new String(c);
 
   }
 
}
