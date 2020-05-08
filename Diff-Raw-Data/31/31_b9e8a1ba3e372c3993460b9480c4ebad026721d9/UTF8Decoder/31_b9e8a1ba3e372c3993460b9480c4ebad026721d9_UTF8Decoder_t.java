 /*
  * UTF8Decoder.java
 * Copyright (C) 2002, Klaus Rennecke.
  * Created on 10. Juli 2002, 04:06
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
 
 /**
  *
  * @author  marion@users.sourceforge.net
  */
 public class UTF8Decoder {
     private byte buffer[];
     
     /** Offset into buffer. */
     private int off;
     
     /** End of buffer. */
     private int end;
     
     /** Byte value table. */
     private static int value[];
     
     /** Code length table. */
     private static int length[];
     
     /** Creates a new instance of UTF8Decoder */
     public UTF8Decoder() {
         initTables();
     }
 
     public void setBuffer(byte buffer[]) {
         setBuffer(buffer, 0, buffer.length);
     }
     
     public void setBuffer(byte buffer[], int off, int len) {
         this.buffer = buffer;
         this.off = off;
         this.end = off + len;
     }
     
     public int[] decodeUCS4(int buffer[], int off, int len) {
         if (buffer == null) {
             buffer = new int[len];
             off = 0; // ignored
         }
         while (--len >= 0) {
             buffer[off++] = decodeUCS4();
         }
         return buffer;
     }
     
     public char[] decodeUCS2(char buffer[], int off, int len) {
         if (buffer == null) {
             buffer = new char[len];
             off = 0; // ignored
         }
         while (--len >= 0) {
             buffer[off++] = (char)decodeUCS4();
         }
         return buffer;
     }
     
     public String decodeString(int len) {
         return new String(decodeUCS2(null, 0, len));
     }
     
     public final int decodeUCS4() {
         int datum = buffer[off++] & 0xff;
         int result = value[datum];
         switch (length[datum]) {
             case 6:
                 result = result << 6 | (buffer[off++] & 0x3f); 
             case 5:
                 result = result << 6 | (buffer[off++] & 0x3f); 
             case 4:
                 result = result << 6 | (buffer[off++] & 0x3f); 
             case 3:
                 result = result << 6 | (buffer[off++] & 0x3f); 
             case 2:
                 result = result << 6 | (buffer[off++] & 0x3f); 
             case 1:
                 return result;
             default:
                 throw new IllegalArgumentException
                     ("format error: illegal byte: "+datum);
         }
     }
     
     private static void initTables() {
         if (length != null) {
             return;
         }
         synchronized (UTF8Decoder.class) {
             if (length != null) {
                 return;
             }
             length = new int[256];
             value = new int[256];
             for (int i = 0; i <= 0x7f; i++) {
                 length[i] = 1;
                 value[i] = i;
             }
             for (int i = 0x80; i < 0xc0; i++) {
                 length[i] = 0; // invalid
                 value[i] = 0;
             }
             for (int i = 0xc0; i <= 0xdf; i++) {
                 length[i] = 2;
                 value[i] = i & 0x1f;
             }
             for (int i = 0xe0; i <= 0xef; i++) {
                 length[i] = 3;
                 value[i] = i & 0xf;
             }
             for (int i = 0xf0; i <= 0xf7; i++) {
                 length[i] = 4;
                 value[i] = i & 0x7;
             }
             for (int i = 0xf8; i <= 0xfb; i++) {
                 length[i] = 5;
                 value[i] = i & 0x3;
             }
             for (int i = 0xfc; i <= 0xfd; i++) {
                 length[i] = 6;
                 value[i] = i & 0x1;
             }
             for (int i = 0xfe; i <= 0xff; i++) {
                 length[i] = 0; // invalid
                 value[i] = 0;
             }
         }
     }
 }
