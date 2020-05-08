 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package bitstream;
 
 import java.io.*;
 import java.util.ArrayList;
 
 /**
  * A class that allows individual bits to be read or written to a stream.
  * NOTE: The flushing operation writes 0s to the end of the bit buffer.
  * @author Andre Wiggins
  */
 public class BitStream implements Flushable, Closeable {
 
   private InputStream in;
   private OutputStream out;
   
   // buffer to write bits to before writing to file
   public int bitbuffer;
   // number of bits left that need to be added to make a complete byte
   private int bits_left;
   
   // 'r' -> read, 'w' -> write
   private char mode;
   // number of bits added to the bit buffer to make it a complete byte
   // on the last call to flush()
   private byte flushpadding;
   // boolean to determine if read is at the End Of the File
   private boolean readEOF;
 
   /**
    * Constructor to create a BitStream instance that reads
    * from the InputStream
    * @param input the InputStream to read from
    */
   public BitStream(InputStream input)
   {
     this.in = input;
     this.out = null;
 
     this.bitbuffer = 0;
     this.bits_left = 0;
     
     this.mode = 'r';
     this.flushpadding = -1;
   }
   
   /**
    * Constructor to create a BitSream instance that writes
    * to the passed-in OutputStream
    * @param output the OutputStream to write to
    */
   public BitStream(OutputStream output)
   {
     this.out = output;
     this.in = null;
 
     this.bitbuffer = 0;
     this.bits_left = 0;
     
     this.mode = 'w';
     this.flushpadding = -1;
     
     this.readEOF = false;
   }
   
   /**
    * Returns the mode of this instance of BitStream. If the BitStream
    * instance is created by passing in an InputStream, then the mode
    * is 'r' for "read". If the BitStream instance is created by 
    * passing in an OutputStream, then the mode is 'w' for "write". 
    * @return the mode of the current BitStream instance
    */
   public char getMode()
   {
     return this.mode;
   }
 
   /**
    * Returns the number of bits required to make the bit buffer a full
    * byte for flushing. Flush() pads the end of the buffer with 0s so it
    * can be written to the OutputStream. This function returns the number
    * of 0s used to pad the last flush() call. Returns -1 if flush() has not
    * been called or if the BitStream is in read mode.
    * @return the number of 0s used to pad the bit buffer on the last flush()
    * call. Returns -1 if flush() has not been called or if the BitStream 
    * is in read mode.
    */
   public byte getFlushPadding()
   {
     return this.flushpadding;
   }
   
   /**
    * Returns if the the Stream has reached the End Of the File
    * @return a boolean of whether the Stream is at the End Of File
    */
   public boolean isEOF()
   {
     return this.readEOF;
   }
   
   /**
    * Write the passed in bit value (true or false) to the OutputStream the 
    * BitStream instantiated with
    * @param val the boolean to write (true = 1, false = 0
    * @throws IOException thrown if the BitStream is instantiated with an 
    * InputStream instead of an OutputStream. Also thrown if an IOException 
    * arises while writing the bits
    */
   public void writeBit(boolean val) throws IOException
   {
     if (out == null)
       throw new IOException("BitStream instance not in 'write' mode. Construct instance with OutputStream to be in 'write' mode.");
     
     int intval = (val == true) ? 1 : 0;
     
     bitbuffer <<= 1;
     bitbuffer |= intval;
     bits_left++;
 
     if (bits_left == 8)
     {
       out.write(bitbuffer);
       bitbuffer = 0;
       bits_left = 0;
     }
 
   }
   
   /**
    * Write a bit to the OutputStream. If the integer value == 0, then 
    * it writes a 0, else, a 1 is written
    * @param val the integer value to write
    * @throws IOException thrown if writeBit() throws an IOException. See
    * writeBit documentation
    */
   public void writeBit(int val) throws IOException
   {
     boolean boolval = (val == 1) ? true : false;
     writeBit(boolval);
   }
 
   /**
    * Write an array of boolean values to the OutputStream. true = 1, false = 0
    * @param vals array of booleans to write
    * @throws IOException thrown if writeBit() throws an IOException. See
    * writeBit() documentation
    */
   public void writeBits(boolean[] vals) throws IOException
   {
     for (int i = 0; i < vals.length; i++)
       writeBit(vals[i]);
   }
 
   /**
    * Write an array of integer values to the OutputStream. If value == 0, 0 is 
    * written, else a 1 is written
    * @param vals an array of integer values to write
    * @throws IOException thrown if writeBit() throws an IOException. See
    * writeBit() documentation
    */
   public void writeBits(int[] vals) throws IOException
   {
     for (int i = 0; i < vals.length; i++)
       writeBit(vals[i]);
   }
 
   /**
    * Writes a strings of 1s and 0s to the bit buffer. If it reads a 0 in
    * the file its writes a 0. Anything else, its writes a 1.
    * @param vals the String of 1s and 0s
    * @throws IOException throws an IOException if writeBit throws an 
    * IOException. (See WriteBit() documentation)
    */
   public void writeBits(String vals) throws IOException
   {
     for (int i = 0; i < vals.length(); i++)
       writeBit((vals.charAt(i) == '0') ? false : true);
   }
   
   /**
    * Writes the remaining bits in the buffer to the InputStream, adding the necessary
    * number of bits to the buffer so it makes a complete byte. 0s are added as
    * padding to the buffer. The number of bits written to make the buffer a full
    * byte can be accessed by getFlushPadding().
    * @throws IOException thrown if writeBit() throws an IOException. See
    * writeBit() documentation.
    */
   public void flush() throws IOException
   {
     byte endpadding = 0;
 
     while(bits_left != 0)
     {
       writeBit(false);
       endpadding++;
     }
 
     this.flushpadding = endpadding;
   }
 
   /**
    * Loads the next byte into the read buffer. 
    * @throws IOException thrown if the BitStream is instantiated with an 
    * OutputStream instead of an Input Stream. Also thrown if an IOException 
    * arises while reading the next byte
    */
   private void loadBits() throws IOException
   {
     if (in == null)
       throw new IOException("BitStream instance not in 'read' mode. Construct instance with OutputStream to be in 'read' mode.");
     
     bitbuffer = in.read();
     if (bitbuffer < 0)
     {
       readEOF = true;
       bits_left = 0;
     }
     else
     {
       readEOF = false;
       bits_left = 8;
     }
   }
 
   /**
    * Read the next bit from the buffer
    * @return the next bit in the buffer (1 = true, 0 = false)
    * @throws IOException
    */
   public boolean readBit() throws IOException
   {
     if (isEOF())
       throw new IOException("End of File Readed!");
 
     if (bits_left == 0)
       loadBits();
     
     int nextbit = bitbuffer & 128;
     bitbuffer <<= 1;
     bits_left--;
     
     if (bits_left == 0)
       loadBits();
 
     return (nextbit == 128) ? true : false;
   }
 
   /**
    * Read the next `n` bits from the buffer.
    * @param n the number of bits to read from the buffer
    * @return a boolean array of the bits read from the buffer
    * @throws IOException thrown if readBit() throws an IOException.
    * See readBit() documentation.
    */
   public boolean[] readBits(int n) throws IOException
   {
     boolean[] readbits = new boolean[n];
 
     for (int i = 0; i < n; i++)
       readbits[i] = readBit();
 
     return readbits;
   }
 
   /**
    * Skip over the next bit in the buffer
    * @throws IOException thrown if readBit() throws an IOException.
    * See readBit() documentation.
    */
   public void skipByte() throws IOException
   {
     readBit();
   }
 
   /**
    * Skips over the next `n` bits in the buffer
    * @param n number of bits to skip over
    * @throws IOException thrown if readBit() throws an IOException.
    * See readBit() documentation.
    */
   public void skipBytes(int n) throws IOException
   {
     for (int i = 0; i < n; i++)
       this.skipByte();
   }
 
   /**
    * Flushes and closes the currently open buffer. The number of bits
    * required to flush the buffer can be accessed from getFlushPadding().
    * (See flush() documentation)
    * @throws IOException thrown if writeBit() throws an IOException
    * (see writeBit() documentation) or if closing the Output/Input Stream
    * throws an IOException
    */
   public void close() throws IOException
   {
     if (in != null)
       in.close();
 
     if (out != null)
     {
       this.flush();
       out.flush();
       out.close();
     }
   }
 
   /**
    * Convenience function to convert an ArrayList of booleans to an
    * ArrayList of integers. (true = 1, false = 0)
    * @param boolvals ArrayList of booleans to convert
    * @return the new ArrayList of integers
    */
   public static ArrayList<Integer> boolToIntArray(ArrayList<Boolean> boolvals)
   {
     ArrayList<Integer> intvals = new ArrayList<Integer>();
     for (int i = 0; i < boolvals.size(); i++)
       intvals.add((boolvals.get(i)) ? 1 : 0);
 
     return intvals;
   }
 
   /**
    * Convenience function to convert an ArrayList of integers to an
    * ArrayList of booleans. (0 = false, any other number = true)
    * @param intvals ArrayList of integers to convert
    * @return the new ArrayList of booleans
    */
   public static ArrayList<Boolean> intToBoolArray(ArrayList<Integer> intvals)
   {
     ArrayList<Boolean> boolvals = new ArrayList<Boolean>();
     for (int i = 0; i < intvals.size(); i++)
      boolvals.add((intvals.get(i) == 1) ? true : false);
 
     return boolvals;
   }
 
   
   /**
    * Returns the byte representation (1s and 0s) of an integer as a String. 
    * @param val the integer to make a byte
    * @return string representing the 1s and 0s representation of the integer
    * passed in.
    */
   public static String intToByteString(int val)
   {
     String binString = Integer.toBinaryString(val);
     int len = binString.length();
     for (int i = 0; i < 8 - len; i++)
       binString = "0" + binString;
     
     return binString;
   }
   
   /**
    * Returns a boolean array of a byte. 1 = true, 0 = false
    * @param byte1 the byte to make a boolean array
    * @return the boolean array of byte1
    */
   public static boolean[] byteToBooleanArray(int byte1)
   {
     boolean[] boolvals = new boolean[8];
     
     for (int i = 0; i < 8; i++)
     {
       boolvals[i] = ((byte1 & 128) == 128) ? true : false;
       byte1 <<= 1;
     }
     
     return boolvals;
   }
 }
