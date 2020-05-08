 /*
  * SWFEncoder.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
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
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.UnsupportedCharsetException;
 import java.util.Stack;
 
 /**
  * SWFEncoder wraps an OutputStream with a buffer to reduce the amount of
  * memory required to encode a movie and to improve efficiency by writing
  * data to a file or external source in blocks.
  */
 public final class SWFEncoder {
     /** The default size, in bytes, for the internal buffer. */
     public static final int BUFFER_SIZE = 4096;
 
     /** Bit mask applied to bytes when converting to unsigned integers. */
     private static final int BYTE_MASK = 255;
     /** Number of bits in an int. */
     private static final int BITS_PER_INT = 32;
     /** Offset to add to number of bits when calculating number of bytes. */
     private static final int ROUND_TO_BYTES = 7;
     /** Right shift to convert number of bits to number of bytes. */
     private static final int BITS_TO_BYTES = 3;
     /** Left shift to convert number of bytes to number of bits. */
     private static final int BYTES_TO_BITS = 3;
     /** Number of bits to shift when aligning a value to the second byte. */
     private static final int TO_BYTE1 = 8;
     /** Number of bits to shift when aligning a value to the third byte. */
     private static final int TO_BYTE2 = 16;
     /** Number of bits to shift when aligning a value to the fourth byte. */
     private static final int TO_BYTE3 = 24;
 
 
     /** The underlying input stream. */
     private final transient OutputStream stream;
     /** The buffer for data read from the stream. */
     private final transient byte[] buffer;
     /** The index in bytes to the current location in the buffer. */
     private transient int index;
     /** The offset in bits to the location in the current byte. */
     private transient int offset;
     /** The character encoding used for strings. */
     private transient String encoding;
     /** Stack for storing file locations. */
     private final transient Stack<Integer>locations;
     /** The position of the buffer relative to the start of the stream. */
     private transient int pos;
 
     /**
      * Create a new SWFEncoder for the underlying InputStream with the
      * specified buffer size.
      *
      * @param streamOut the stream from which data will be written.
      * @param length the size in bytes of the buffer.
      */
     public SWFEncoder(final OutputStream streamOut, final int length) {
         stream = streamOut;
         buffer = new byte[length];
         encoding = "UTF-8";
         locations = new Stack<Integer>();
         pos = 0;
     }
 
     /**
      * Create a new SWFEncoder for the underlying InputStream using the
      * default buffer size.
      *
      * @param streamOut the stream from which data will be written.
      */
     public SWFEncoder(final OutputStream streamOut) {
         stream = streamOut;
         buffer = new byte[BUFFER_SIZE];
         encoding = "UTF-8";
         locations = new Stack<Integer>();
         pos = 0;
     }
 
     /**
      * Write the data currently stored in the buffer to the underlying stream.
      * @throws IOException if an error occurs while writing the data to the
      * stream.
      */
     public void flush() throws IOException {
         stream.write(buffer, 0, index);
         stream.flush();
 
         int diff;
         if (offset != 0) {
             diff = 1;
             buffer[0] = buffer[index];
         } else {
             diff = 0;
         }
 
         for (int i = diff; i < buffer.length; i++) {
             buffer[i] = 0;
         }
 
         pos += index;
         index = 0;
     }
 
     /**
      * Remember the current position.
      */
     public void mark() {
         locations.push(pos + index);
     }
 
     /**
      * Discard the last saved position.
      */
     public void unmark() {
         locations.pop();
     }
 
     /**
      * Compare the number of bytes read since the last saved position. The last
      * saved position is discarded.
      *
      * @param expected the expected number of bytes read.
      *
      * @throws IOException if the number of bytes read is different from the
      * expected number.
      */
     public void unmark(final int expected) throws IOException {
         if (bytesWritten() != expected) {
             throw new CoderException(locations.peek(), expected,
                     bytesWritten() - expected);
         }
         locations.pop();
     }
 
     /**
      * Get the number of bytes read from the last saved position.
      *
      * @return the number of bytes read since the mark() method was last called.
      */
     public int bytesWritten() {
         int count;
         if (pos == 0) {
             count = index - locations.peek();
         } else {
             count = (pos + index) - locations.peek();
         }
         return count;
     }
 
     /**
      * Sets the character encoding scheme used when encoding or decoding
      * strings.
      *
      * If the character set encoding is not supported by the Java environment
      * then an UnsupportedCharsetException will be thrown. If the character set
      * cannot be identified then an IllegalCharsetNameException will be thrown.
      *
      * @param charSet
      *            the name of the character set used to encode strings.
      */
     public void setEncoding(final String charSet) {
         if (!Charset.isSupported(charSet)) {
             throw new UnsupportedCharsetException(charSet);
         }
         encoding = charSet;
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
 
     public int strlen(final String string) {
         try {
             return string.getBytes(encoding).length + 1;
         } catch (final UnsupportedEncodingException e) {
             throw new AssertionError(e);
         }
     }
 
     /**
      * Changes the location to the next byte boundary.
      */
     public void alignToByte() {
         if (offset > 0) {
             index += 1;
             offset = 0;
         }
     }
 
     /**
      * Write a value to bit field.
      *
      * @param value
      *            the value.
      * @param numberOfBits
      *            the (least significant) number of bits that will be written.
      * @throws IOException if there is an error writing data to the underlying
      * stream.
      */
     public void writeBits(final int value, final int numberOfBits)
                 throws IOException {
 
         int ptr = (index << 3) + offset + numberOfBits;
 
         if (ptr >= (buffer.length << 3)) {
             flush();
         }
 
         final int val = ((value << (BITS_PER_INT - numberOfBits)) >>> offset)
                 | (buffer[index] << TO_BYTE3);
         int base = BITS_PER_INT - (((offset + numberOfBits
                 + ROUND_TO_BYTES) >>> BITS_TO_BYTES) << BYTES_TO_BITS);
         base = base < 0 ? 0 : base;
 
         int pointer = (index << 3) + offset;
 
         for (int i = 24; i >= base; i -= 8) {
             buffer[index++] = (byte) (val >>> i);
         }
 
         if (offset + numberOfBits > BITS_PER_INT) {
             buffer[index] = (byte) (value
                     << (8 - (offset + numberOfBits - BITS_PER_INT)));
         }
 
         pointer += numberOfBits;
         index = pointer >>> BITS_TO_BYTES;
         offset = pointer & 7;
     }
 
     /**
      * Write a byte.
      *
      * @param value
      *            the value to be written - only the least significant byte will
      *            be written.
      * @throws IOException if there is an error writing data to the underlying
      * stream.
      */
     public void writeByte(final int value) throws IOException {
         if (index == buffer.length) {
             flush();
         }
         buffer[index++] = (byte) value;
     }
 
     /**
      * Write an array of bytes.
      *
      * @param bytes
      *            the array to be written.
      *
      * @return the number of bytes written.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public int writeBytes(final byte[] bytes) throws IOException {
         if (index + bytes.length < buffer.length) {
             System.arraycopy(bytes, 0, buffer, index, bytes.length);
             index += bytes.length;
         } else {
             flush();
             stream.write(bytes, 0, bytes.length);
             pos += bytes.length;
         }
         return bytes.length;
     }
 
     /**
      * Write a string using the default character set defined in the encoder.
      *
      * @param str
      *            the string.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public void writeString(final String str) throws IOException {
         try {
             writeBytes(str.getBytes(encoding));
             buffer[index++] = 0;
         } catch (final java.io.UnsupportedEncodingException e) {
             throw new AssertionError(e);
         }
     }
 
     /**
      * Write a 16-bit integer.
      *
      * @param value
      *            an integer containing the value to be written.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public void writeShort(final int value) throws IOException {
         if (index + 2 > buffer.length) {
             flush();
         }
         buffer[index++] = (byte) value;
         buffer[index++] = (byte) (value >>> TO_BYTE1);
     }
 
     /**
      * Write a 32-bit integer.
      *
      * @param value
      *            an integer containing the value to be written.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public void writeInt(final int value) throws IOException {
         if (index + 4 > buffer.length) {
             flush();
         }
         buffer[index++] = (byte) value;
         buffer[index++] = (byte) (value >>> TO_BYTE1);
         buffer[index++] = (byte) (value >>> TO_BYTE2);
         buffer[index++] = (byte) (value >>> TO_BYTE3);
     }
 
     /**
      * Write a 32-bit unsigned integer, encoded in a variable number of bytes.
      *
      * @param value
      *            an integer containing the value to be written.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public void writeVarInt(final int value) throws IOException {
         if (index + 5 > buffer.length) {
             flush();
         }
 
         int val = value;
        if (val > 127) {
            while (val > 127) {
                buffer[index++] = (byte) ((val & 0x007F) | 0x0080);
                val = val >>> 7;
            }
        } else {
            buffer[index++] = (byte) (value & 0x007F);
         }
     }
 
     /**
      * Write a single-precision floating point number.
      *
      * @param value
      *            the value to be written.
      * @throws IOException if there is an error reading data from the underlying
      * stream.
      */
     public void writeHalf(final float value) throws IOException {
         //CHECKSTYLE:OFF
         final int intValue = Float.floatToIntBits(value);
         final int sign = (intValue >> 16) & 0x00008000;
         final int exponent = ((intValue >> 23) & BYTE_MASK)
                         - (127 - 15);
         int mantissa = intValue & 0x007fffff;
 
         if (exponent <= 0) {
             if (exponent < -10) {
                 writeShort(0);
             } else {
                 mantissa = (mantissa | 0x00800000) >> (1 - exponent);
                 writeShort((sign | (mantissa >> 13)));
             }
         } else if (exponent == 0xff - (127 - 15)) {
             if (mantissa == 0) { // Inf
                 writeShort(sign | 0x7c00);
             } else { // NAN
                 mantissa >>= 13;
                 writeShort((sign | 0x7c00 | mantissa
                         | ((mantissa == 0) ? 1 : 0)));
             }
         } else {
             if (exponent > 30) { // Overflow
                 writeShort((sign | 0x7c00));
             } else {
                 writeShort((sign | (exponent << 10) | (mantissa >> 13)));
             }
         }
         //CHECKSTYLE:ON
     }
 }
