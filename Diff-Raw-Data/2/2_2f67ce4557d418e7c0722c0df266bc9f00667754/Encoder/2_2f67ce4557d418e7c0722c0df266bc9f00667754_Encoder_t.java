 /*
  * Enoder.java
  * Transform
  *
  * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform.coder;
 
 import java.io.UnsupportedEncodingException;
 
 /**
  * Encoder provides a set of method for encoding data that is not byte-ordered,
  * specifically bit fields and strings.
  */
 public class Encoder extends Coder {
     /**
      * Calculates the minimum number of bits required to encoded an unsigned
      * integer in a bit field.
      *
      * @param value
      *            the unsigned value to be encoded.
      *
      * @return the number of bits required to encode the value.
      */
     public static int unsignedSize(final int value) {
 
         final int val = (value < 0) ? -value - 1 : value;
         int counter = 32;
         int mask = 0x80000000;
 
         while (((val & mask) == 0) && (counter > 0)) {
             mask >>>= 1;
             counter -= 1;
         }
         return counter;
     }
 
     /**
      * Calculates the minimum number of bits required to encoded a signed
      * integer in a bit field.
      *
      * @param value
      *            the signed value to be encoded.
      *
      * @return the number of bits required to encode the value.
      */
     public static int size(final int value) {
         int counter = 32;
         int mask = 0x80000000;
         final int val = (value < 0) ? -value - 1 : value;
 
         while (((val & mask) == 0) && (counter > 0)) {
             mask >>>= 1;
             counter -= 1;
         }
         return counter + 1;
     }
 
     /**
      * Returns the minimum number of bits required to encode all the signed
      * values in an array as a set of bit fields with the same size.
      *
      * @param values
      *            an array of signed integers.
      *
      * @return the minimum number of bits required to encode each of the values.
      */
     public static int maxSize(final int... values) {
 
         int max = 0;
         int size;
 
         for (final int value : values) {
             size = size(value);
             max = (max > size) ? max : size;
         }
         return max;
     }
 
     /**
      * Creates an Encoder with the buffer used to encode data set to the
      * specified size.
      *
      * @param size
      *            the number of bytes in the internal buffer.
      */
     public Encoder(final int size) {
         super();
         setData(new byte[size]);
     }
 
     /**
      * Write a value to bit field.
      *
      * @param value
      *            the value.
      * @param numberOfBits
      *            the (least significant) number of bits that will be written.
      */
     public final void writeBits(final int value, final int numberOfBits) {
 
         final int val = ((value << (32 - numberOfBits)) >>> offset)
                 | (data[index] << 24);
         int base = 32 - (((offset + numberOfBits + 7) >>> 3) << 3);
         base = base < 0 ? 0 : base;
 
         final int mark = getPointer();
 
         for (int i = 24; i >= base; i -= 8) {
             data[index++] = (byte) (val >>> i);
         }
 
        if (offset + numberOfBits > 32) {
             data[index] = (byte) (value << (8 - offset));
         }
 
         setPointer(mark + numberOfBits);
     }
 
     /**
      * Write a 16-bit field.
      *
      * The internal pointer must aligned on a byte boundary. The value is
      * written as if it was a 16-bit integer with big-ending byte ordering.
      *
      * @param value
      *            the value to be written - only the least significant 16-bits
      *            will be written.
      */
     public final void writeB16(final int value) {
         data[index++] = (byte) (value >>> 8);
         data[index++] = (byte) value;
     }
 
     /**
      * Write a byte.
      *
      * @param value
      *            the value to be written - only the least significant byte will
      *            be written.
      */
     public final void writeByte(final int value) {
         data[index++] = (byte) value;
     }
 
     /**
      * Write an array of bytes.
      *
      * @param bytes
      *            the array to be written.
      *
      * @return the number of bytes written.
      */
     public final int writeBytes(final byte[] bytes) {
         System.arraycopy(bytes, 0, data, index, bytes.length);
         index += bytes.length;
         return bytes.length;
     }
 
     /**
      * Calculates the length of a string when encoded using the specified
      * character set.
      *
      * @param string
      *            the string to be encoded.
      *
      * @return the number of bytes required to encode the string plus 1 for a
      *         terminating null character.
      */
 
     public final int strlen(final String string) {
         try {
             return string.getBytes(encoding).length + 1;
         } catch (final UnsupportedEncodingException e) {
             throw new AssertionError(e);
         }
     }
 
     /**
      * Write a string using the default character set defined in the encoder.
      *
      * @param str
      *            the string.
      */
     public final void writeString(final String str) {
         try {
             writeBytes(str.getBytes(encoding));
             data[index++] = 0;
         } catch (final java.io.UnsupportedEncodingException e) {
             throw new AssertionError(e);
         }
     }
 
     /**
      * Write a string using the specified character set.
      *
      * @param str
      *            the string.
      *
      * @param charset
      *            the name of the character set.
      */
     public final void writeString(final String str, final String charset) {
         try {
             writeBytes(str.getBytes(charset));
             data[index++] = 0;
         } catch (final java.io.UnsupportedEncodingException e) {
             throw new AssertionError(e);
         }
     }
 }
