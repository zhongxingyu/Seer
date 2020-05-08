 /*
  * UTF8Encoder.java
  * Copyright (C) 2002, Klaus Rennecke.
  * Created on 10. Juli 2002, 01:10
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.  */
 
 package net.sourceforge.fraglets.codec;
 
 import java.io.UnsupportedEncodingException;
 
 /**
  * An UTF-8 encoder able to encode UCS-4 in addition to UCS-2. See
  * standards ISO/IEC 10646-1:1993 and RFC2279, RFC2781.
  * @author  marion@users.sourceforge.net
  */
 public class UTF8Encoder {
     
     /** Holds value of property buffer. */
     private byte[] buffer = new byte[12];
     
     /** Holds value of property size. */
     private int size = 0;
     
     /** Creates a new instance of UCS4Codec */
     public UTF8Encoder() {
     }
     
     /** Encode a string of UCS-4 values in UTF-8. */
     public void encodeUCS4(int data[], int off, int len) {
         while (--len >= 0) {
             encodeUCS4(data[off++]);
         }
     }
     
     /** Encode a string  of UCS-2 values in UTF-8. */
     public void encodeUCS2(char data[], int off, int len) {
         while (--len >= 0) {
             int datum = data[off++];
             if ((datum & 0xf800) == 0xd800) {
                 if (datum > 0xdbff) {
                     throw new IllegalArgumentException
                         ("invalid surrogate high-half: "+datum);
                 } else if (--len < 0) {
                     throw new IllegalArgumentException
                         ("truncated surrogate");
                 }
                 int high = (datum & 0x3ff) << 10;
                 datum = data[off++];
                 if (datum < 0xdc00 || datum > 0xdfff) {
                     throw new IllegalArgumentException
                         ("invalid surrogate low-half: "+datum);
                 }
                 int low = datum & 0x3ff;
                datum = (high | (datum & 0x3ff)) + 0x10000;
             }
             encodeUCS4(datum);
         }
     }
     
     /** Encode a string in UTF-8. */
     public void encodeString(String data) {
         char copy[] = data.toCharArray();
         encodeUCS2(copy, 0, copy.length);
     }
     
     /** Encode a UCS-4 datum in UTF-8. */
     public final void encodeUCS4(int datum) {
         if (datum < 0) {
             throw new IllegalArgumentException
                 ("argument out of range: "+datum);
         } else if (datum <= 0x7f) {
             growBuffer(1);
             buffer[size++] = (byte)(datum & 0xff);
         } else if (datum <= 0x7ff) {
             growBuffer(2);
             buffer[size++] = (byte)(0xc0 | (datum >> 6));
             buffer[size++] = (byte)(0x80 | (datum & 0x3f));
         } else if (datum <= 0xffff) {
             growBuffer(3);
             buffer[size++] = (byte)(0xe0 | (datum >> 12));
             buffer[size++] = (byte)(0x80 | ((datum >> 6) & 0x3f));
             buffer[size++] = (byte)(0x80 | (datum & 0x3f));
         } else if (datum <= 0x1fffff) {
             growBuffer(4);
             buffer[size++] = (byte)(0xf0 | (datum >> 18));
             buffer[size++] = (byte)(0x80 | ((datum >> 12) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 6) & 0x3f));
             buffer[size++] = (byte)(0x80 | (datum & 0x3f));
         } else if (datum <= 0x3ffffff) {
             growBuffer(5);
             buffer[size++] = (byte)(0xf8 | (datum >> 24));
             buffer[size++] = (byte)(0x80 | ((datum >> 18) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 12) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 6) & 0x3f));
             buffer[size++] = (byte)(0x80 | (datum & 0x3f));
         } else {
             growBuffer(6);
             buffer[size++] = (byte)(0xfc | (datum >> 30));
             buffer[size++] = (byte)(0x80 | ((datum >> 24) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 18) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 12) & 0x3f));
             buffer[size++] = (byte)(0x80 | ((datum >> 6) & 0x3f));
             buffer[size++] = (byte)(0x80 | (datum & 0x3f));
         }
     }
     
     /** Getter for property buffer.
      * @return Value of property buffer.
      */
     public byte[] getBuffer() {
         return this.buffer;
     }
     
     /** Setter for property buffer.
      * @param buffer New value of property buffer.
      */
     public void setBuffer(byte[] buffer) {
         this.buffer = buffer;
     }
     
     /** Setter for properties buffer and size.
      * @param buffer New value of property buffer.
      * @param size New value of property size.
      */
     public void setBuffer(byte[] buffer, int size) {
         if (size > buffer.length) {
             throw new IndexOutOfBoundsException
                 ("size too big, "+size+">"+buffer.length);
         }
         setBuffer(buffer);
         setSize(size);
     }
     
     /** Getter for property size.
      * @return Value of property size.
      */
     public int getSize() {
         return this.size;
     }
     
     /** Setter for property size.
      * @param size New value of property size.
      */
     public void setSize(int size) {
         if (size > buffer.length) {
             throw new IndexOutOfBoundsException
                 ("size too big, "+size+">"+buffer.length);
         }
         this.size = size;
     }
     
     /** Return a copy of the current buffer, trimmed to the current size. */
     public byte[] toByteArray() {
         byte result[] = new byte[getSize()];
         if (result.length > 0) {
             System.arraycopy(getBuffer(), 0, result, 0, result.length);
         }
         return result;
     }
     
     /** Grow the current buffer so that it fits size+amount. */
     protected final void growBuffer(int amount) {
         if (size + amount > buffer.length) {
             int more = Math.max(size + amount, (size + size >> 2));
             byte grow[] = new byte[more];
             if (buffer != null && buffer.length > 0) {
                 System.arraycopy(buffer, 0, grow, 0, size);
             }
             buffer = grow;
         }
     }
 }
