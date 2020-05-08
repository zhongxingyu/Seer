 /*
  * Fast Infoset ver. 0.1 software ("Software")
  *
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  *
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  *
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  *
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  *
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */
 
 
 package com.sun.xml.fastinfoset.algorithm;
 
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jvnet.fastinfoset.EncodingAlgorithmException;
 
 
 /**
  *
  * An encoder for handling boolean values.  Suppports the builtin BOOLEAN encoder.
  *
  * @author Alan Hudson
  * @author Paul Sandoz
  *
  * @version
  *
  */
 
 public class BooleanEncodingAlgorithm extends BuiltInEncodingAlgorithm {
     
     /** Table for setting a particular bit of a byte */
     private static final int[] BIT_TABLE = {
         1 << 7,
         1 << 6,
         1 << 5,
         1 << 4,
         1 << 3,
         1 << 2,
         1 << 1,
         1 << 0};
                 
     public int getPrimtiveLengthFromOctetLength(int octetLength) throws EncodingAlgorithmException {
         // Cannot determine the number of boolean values from just the octet length
         throw new UnsupportedOperationException();
     }
 
     public int getOctetLengthFromPrimitiveLength(int primitiveLength) {
         if (primitiveLength < 5) {
             return 1;
         } else {
             final int div = primitiveLength / 8;
             return (div == 0) ? 2 : 1 + div;             
         }         
     }
                 
     public final Object decodeFromBytes(byte[] b, int start, int length) throws EncodingAlgorithmException {
         final int blength = getPrimtiveLengthFromOctetLength(length, b[start]);
         boolean[] data = new boolean[blength];
 
         decodeFromBytesToBooleanArray(data, 0, blength, b, start, length);
         return data;
     }                
                 
     public final Object decodeFromInputStream(InputStream s) throws IOException {
         final List booleanList = new ArrayList();
 
         int value = s.read();
         if (value == -1) {
             throw new EOFException();            
         }
         final int unusedBits = (value >> 4) & 0xFF;
                    
         int bitPosition = 4;
         int bitPositionEnd = 8;
         int valueNext = 0;
         do {
             valueNext = s.read();
             if (valueNext == -1) {
                 bitPositionEnd -= unusedBits;
             }
             
             while(bitPosition < bitPositionEnd) {
                 booleanList.add(
                         new Boolean((value & BIT_TABLE[bitPosition++]) > 0));
             }
             
             value = valueNext;
         } while (value != -1);
         
         return generateArrayFromList(booleanList);
     }
                 
     public void encodeToOutputStream(Object data, OutputStream s) throws IOException {
         if (!(data instanceof boolean[])) {
             throw new IllegalArgumentException("'data' not an instance of boolean[]");
         }
 
         boolean array[] = (boolean[])data;
         final int alength = array.length;
         
         final int mod = (alength + 4) % 8;
         final int unusedBits = (mod == 0) ? 0 : 8 - mod;
         
         int bitPosition = 4;
         int value = unusedBits << 4;
         int astart = 0;
         while (astart < alength) {
             if (array[astart++]) {
                 value |= BIT_TABLE[bitPosition];
             }
             
             if (++bitPosition == 8) {
                 s.write(value);
                 bitPosition = value = 0;
             }
         }
         
         if (bitPosition != 8) {
             s.write(value);
         }
     }
                                 
     public final Object convertFromCharacters(char[] ch, int start, int length) {
         if (length == 0) {
             return new boolean[0];
         }
 
         final CharBuffer cb = CharBuffer.wrap(ch, start, length);
         final List booleanList = new ArrayList();
 
         matchWhiteSpaceDelimnatedWords(cb,
             new WordListener() {
                 public void word(int start, int end) {
                     if (cb.charAt(start) == 't') {
                         booleanList.add(Boolean.TRUE);
                     } else {
                         booleanList.add(Boolean.FALSE);
                     }
                 }
             }
         );
 
         return generateArrayFromList(booleanList);
     }
 
     public final void convertToCharacters(Object data, StringBuffer s) {
         if (data == null) {
             return;
         }
 
         final boolean[] value = (boolean[]) data;
         if (value.length == 0) {
             return;
         }
 
         // Insure conservately as all false
         s.ensureCapacity(value.length * 5);
 
         final int end = value.length - 1;
         for (int i = 0; i <= end; i++) {
             if (value[i]) {
                 s.append("true");
             } else {
                 s.append("false");
             }
             if (i != end) {
                 s.append(' ');
             }
         }
     }
 
     public int getPrimtiveLengthFromOctetLength(int octetLength, int firstOctet) throws EncodingAlgorithmException {
         final int unusedBits = (firstOctet >> 4) & 0xFF;
         if (octetLength == 1) {
            if (unusedBits > 3) {
                throw new EncodingAlgorithmException("The number of unused bits is too large (should be < 4)");
            }
            return 4 - unusedBits;
         } else {
            if (unusedBits > 7) {
               throw new EncodingAlgorithmException("The number of unused bits is too large (should be < 4)");
            }
            return octetLength * 8 - 4 - unusedBits;
         } 
     }
     
     public final void decodeFromBytesToBooleanArray(boolean[] bdata, int bstart, int blength, byte[] b, int start, int length) {
         int value = b[start++] & 0xFF;
         int bitPosition = 4;
         final int bend = bstart + blength;
         while (bstart < bend) {
             if (bitPosition == 8) {
                 value = b[start++] & 0xFF;
                 bitPosition = 0;
             }
             
             bdata[bstart++] = (value & BIT_TABLE[bitPosition++]) > 0;
         }
     }
                 
     public void encodeToBytes(Object array, int astart, int alength, byte[] b, int start) {
         if (!(array instanceof boolean[])) {
             throw new IllegalArgumentException("'data' not an instance of boolean[]");
         }
 
         encodeToBytesFromBooleanArray((boolean[])array, astart, alength, b, start);
     }
     
     public void encodeToBytesFromBooleanArray(boolean[] array, int astart, int alength, byte[] b, int start) {
         final int mod = (alength + 4) % 8;
         final int unusedBits = (mod == 0) ? 0 : 8 - mod;
         
         int bitPosition = 4;
         int value = unusedBits << 4;
         final int aend = astart + alength;
         while (astart < aend) {
             if (array[astart++]) {
                 value |= BIT_TABLE[bitPosition];
             }
             
             if (++bitPosition == 8) {
                 b[start++] = (byte)value;
                 bitPosition = value = 0;
             }
         }
         
         if (bitPosition > 0) {
             b[start] = (byte)value;
         }
     }
 
 
     /**
      *
      * Generate a boolean array from a list of Booleans.
      *
      * @param array The array
      *
      */
     private final boolean[] generateArrayFromList(List array) {
         boolean[] bdata = new boolean[array.size()];
         for (int i = 0; i < bdata.length; i++) {
             bdata[i] = ((Boolean)array.get(i)).booleanValue();
         }
 
         return bdata;
     }
            
 }
 
