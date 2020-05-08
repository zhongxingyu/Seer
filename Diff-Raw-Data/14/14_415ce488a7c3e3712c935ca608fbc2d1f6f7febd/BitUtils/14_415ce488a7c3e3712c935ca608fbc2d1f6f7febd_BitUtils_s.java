 /*
  * The MIT License
  *
  * Copyright 2013 Georgios Migdos <cyberpython@gmail.com>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.gmigdos.bitutils;
 
 import java.io.PrintStream;
 
 /**
  *
  * @author Georgios Migdos <cyberpython@gmail.com>
  */
 public class BitUtils {
 
     /**
      * Converts a byte array to its 8-bit byte binary representation. Bytes are
      * left padded with 0s if necessary
      *
      * @param bytes the byte array
      *
      * @return A string representing this byte array's values
      */
     public static String bytesToBinaryString(byte[] bytes) {
         StringBuilder sb = new StringBuilder();
         for (byte b : bytes) {
             sb.append(String.format("%1$8s", Integer.toBinaryString(b & 0xFF)).replaceAll(" ", "0"));
             sb.append(" ");
         }
         sb.delete(sb.length() - 1, sb.length());
         return sb.toString();
     }
 
     /**
      * Converts a byte array to its 2-digit hexadecimal representation.
      *
      * @param bytes the byte array
      *
      * @return A string representing this byte array's values
      */
     public static String bytesToHexString(byte[] bytes) {
         StringBuilder sb = new StringBuilder();
         for (byte b : bytes) {
             sb.append(String.format("%1$2s", Integer.toHexString(b & 0xFF)).replaceAll(" ", "0").toUpperCase());
             sb.append(" ");
         }
         sb.delete(sb.length() - 1, sb.length());
         return sb.toString();
     }
 
     /**
      * Prints to the specified {@link PrintStream} the given byte array
      * contents. Each byte's value is interpreted as unsigned [0-255].
      *
      * @param bytes the byte array
      * @param out the stream to print to
      *
      */
     public static void printBytesInUnsignedDecimalForm(byte[] bytes, PrintStream out) {
         StringBuilder sb = new StringBuilder();
         for (byte b : bytes) {
             sb.append((short) (0xFF & b));
             sb.append(" ");
         }
         sb.delete(sb.length() - 1, sb.length());
         out.println(sb.toString());
     }
 
     /**
      * Prints to the specified {@link PrintStream} the given byte array contents
      * in their 8-bit per byte binary representation.
      *
      * @param bytes the byte array
      * @param out the stream to print to
      *
      */
     public static void printBytesInBinaryForm(byte[] bytes, PrintStream out) {
         out.print(bytesToBinaryString(bytes));
         out.println();
     }
 
     /**
      * Prints to the specified {@link PrintStream} the given byte array contents
      * in their 2-digit hexadecimal representation.
      *
      * @param bytes the byte array
      * @param out the stream to print to
      *
      */
     public static void printBytesInHexForm(byte[] bytes, PrintStream out) {
         out.print(bytesToHexString(bytes));
         out.println();
     }
 
     /**
      * Sets the bit at the specified index of b to 1. The index is calculated
      * from right to left - i.e. the least significant bit is considered to be
      * the rightmost (index = 0), while the most significant bit is considered
      * to be the leftmost (index = b.length*8 -1) if the whole array is treated
      * as a series of consecutive bits.
      *
      * @param b the byte array
      * @param index the index of the bit to set to 1
      *
      * @return the byte array with the bit at the specified index set to 1
      *
      * @throws IndexOutOfBoundsException
      */
     public static byte[] setBit(byte[] b, int index) throws IndexOutOfBoundsException {
         int maxIndex = b.length * 8 - 1;
         if ((index < 0) || (index > maxIndex)) {
             throw new IndexOutOfBoundsException("Invalid bit index: " + index + " for " + b.length + "-byte array.");
         }
         int byteOffset = b.length - (index / 8) - 1;
         int bitOffset = index % 8;
         byte mask = (byte) (0xFF & (1 << bitOffset));
         b[byteOffset] = (byte) (0xFF & (b[byteOffset] | mask));
         return b;
     }
 
     /**
      * Sets the bit at the specified index of b to 0. The index is calculated
      * from right to left - i.e. the least significant bit is considered to be
      * the rightmost (index = 0), while the most significant bit is considered
      * to be the leftmost (index = b.length*8 -1) if the whole array is treated
      * as a series of consecutive bits.
      *
      * @param b the byte array
      * @param index the index of the bit to set to 0
      *
      * @return the byte array with the bit at the specified index set to 0
      *
      * @throws IndexOutOfBoundsException
      */
     public static byte[] unsetBit(byte[] b, int index) {
         int maxIndex = b.length * 8 - 1;
         if ((index < 0) || (index > maxIndex)) {
             throw new IndexOutOfBoundsException("Invalid bit index: " + index + " for " + b.length + "-byte array.");
         }
         int byteOffset = b.length - (index / 8) - 1;
         int bitOffset = index % 8;
         byte mask = (byte) ~(0xFF & (1 << bitOffset));
         b[byteOffset] = (byte) (0xFF & (b[byteOffset] & mask));
         return b;
     }
 
     /**
      * Extracts the given number of bits starting at the given bit index.
      * The result is returned as a byte array left-padded with 0s as needed.
      * The index is calculated from right to left - i.e. the least significant 
      * bit is considered to be the rightmost (index = 0), while the most 
      * significant bit is considered  to be the leftmost (index = b.length*8 -1)
      * if the whole array is treated as a series of consecutive bits.
      * 
      * @param src the original byte array
      * @param index the first bit to extract
      * @param numOfBits the number of bits to extract
      * 
      * @return a byte array containing the extracted bits
      * 
      * @throws IllegalArgumentException if the starting bit index is lower than 
      *                                  0 or larger than the source byte arrays 
      *                                  bit count. Also, if the number of bits
      *                                  to extract is lower or equal to 0 or
      *                                  when added to the starting index exceeds
      *                                  the source byte array's bit count.
      * 
      */
     public static byte[] extract(byte[] src, int index, int numOfBits)
             throws IllegalArgumentException, IndexOutOfBoundsException {
 
         if (numOfBits <= 0) {
             throw new IllegalArgumentException("The number of bits cannot be "
                     + "negative or zero. Given: " + numOfBits);
         }
         if ((index < 0) || (index > src.length * 8 - 1)) {
             throw new IllegalArgumentException("The starting bit cannot be "
                     + "negative or larger than " + (src.length * 8 - 1)
                     + ". Given: " + index);
         }
         if (index+numOfBits>src.length*8) {
             throw new IllegalArgumentException("Invalid number of bits: "
                     + numOfBits);
         }
 
         byte[] result;
 
         int numOfBitsToShiftBy = index % 8;
         int numOfBitsToLeftShiftBy = 8 - numOfBitsToShiftBy;
         int numOfSpanningBits = numOfBits % 8;
        int numOfBytes = (numOfBits / 8);
 
         if (numOfBitsToShiftBy > 0) {
            numOfBytes++;
            if (numOfSpanningBits - 1 > numOfBitsToLeftShiftBy) {
                numOfBytes++;
            }
             result = new byte[numOfBytes];
             int offset = src.length - (index / 8) - 1;
             int shiftMask = (0xFF >>> numOfBitsToLeftShiftBy);
             int resultIndex;
             int srcIndex;
             for (int i = 0; i < numOfBytes; i++) {
                 resultIndex = numOfBytes - i - 1;
                 srcIndex = offset - i;
                 result[resultIndex] = (byte) (((0xFF & src[srcIndex]) >>> numOfBitsToShiftBy));
                 if (srcIndex > 0) {
                     result[resultIndex] = (byte) (((shiftMask & src[--srcIndex]) << numOfBitsToLeftShiftBy) | result[resultIndex]);
                 }
             }
             int msbMask = ~(0xFF << numOfSpanningBits);
             result[0] = (byte) (result[0] & msbMask);
 
 
         } else {
             int mask = (numOfSpanningBits == 0 ? 0xFF : (0xFF >>> (8 - numOfSpanningBits)));
            if (numOfSpanningBits > 0) {
                numOfBytes++;
            }
             result = new byte[numOfBytes];
             System.arraycopy(src, src.length - index / 8 - numOfBytes, result, 0, numOfBytes);
             result[0] = (byte) (result[0] & mask);
         }
         return result;
     }
    
 }
