 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.util;
 
 /**
  * This <code>Base64</code> is a utility class for encoding using the Base64
  * encoding. </p>
  * <p/>
  * The Base64 encoding is designed to represent arbitrary sequences of octets in
  * a form that need to be humanly readable. The encoding and decoding algorithms
  * are simple, but the encoded data are consitently only about 33% larger than
  * the unencoded data. </p>
  * <p/>
  * For more complete information about the Base64 encoding, please read <a
  * href="http://www.ietf.org/rfc/rfc1521.txt" target="_top">MIME (Multipurpose
  * Internet Mail Extensions) Part One</a> (Section 5.2). </p>
  */
 public class Base64 {
     private static final byte[] BASE64_ALPHABET_ARRAY = {
             'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
             'N',
             'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
             'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
             'n',
             'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
             '+', '/'
     };
     private static final byte[] BASE64_FOR_URL_ALPHABET_ARRAY = {
             'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
             'N',
             'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
             'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
             'n',
             'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
             '-', '_'
     };
     private static final byte PAD = '=';
 
     /**
      * Encodes the specified <code>bytes</code> using the Base64 encoding. </p>
      *
      * @param bytes the bytes to be encoded.
      * @return the Base64-encoded bytes.
      * @see #encode(String)
      */
     public static byte[] encode(byte[] bytes) {
         return encode(bytes, BASE64_ALPHABET_ARRAY, true);
     }
 
     /**
      * Encodes the specified <code>string</code> using the Base64 encoding. </p>
      *
      * @param string the string to be encoded.
      * @return the Base64-encoded string.
      * @see #encode(byte[])
      */
     public static String encode(String string) {
         if (string == null) {
             return null;
         }
         return new String(encode(string.getBytes()));
     }
 
     /**
      * Encodes the specified <code>bytes</code> using the Base64 encoding for
      * URL usage. </p>
      *
      * @param bytes the bytes to be encoded.
      * @return the Base64-encoded bytes for URL usage.
      */
     public static byte[] encodeForURL(byte[] bytes) {
         return encode(bytes, BASE64_FOR_URL_ALPHABET_ARRAY, false);
     }
 
     private static byte[] encode(
             byte[] bytes, byte[] alphabetArray, boolean usePadding) {
 
         if (bytes == null) {
             return null;
         } else if (bytes.length == 0) {
             return bytes;
         }
         int _length = bytes.length;
         int _remainder = _length % 3;
         byte[] _bytes;
         if (usePadding) {
             _bytes = new byte[(_length + 2) / 3 * 4];
         } else {
             _bytes =
                     new byte[
                             ((_length + 2) / 3 * 4) -
                                     (_remainder != 0 ? 3 - _remainder : 0)];
         }
         _length -= _remainder;
         int _group;
         int _i;
         int _index = 0;
         for (_i = 0; _i < _length;) {
             _group =
                     (bytes[_i++] & 0xFF) << 16 |
                             (bytes[_i++] & 0xFF) << 8 |
                             bytes[_i++] & 0xFF;
             _bytes[_index++] = alphabetArray[_group >>> 18];
             _bytes[_index++] = alphabetArray[_group >>> 12 & 0x3F];
             _bytes[_index++] = alphabetArray[_group >>> 6 & 0x3F];
             _bytes[_index++] = alphabetArray[_group & 0x3F];
         }
         switch (_remainder) {
             case 0:
                 break;
             case 1:
                 _group = (bytes[_i] & 0xFF) << 4;
                 _bytes[_index++] = alphabetArray[_group >>> 6];
                 if (usePadding) {
                     _bytes[_index++] = alphabetArray[_group & 0x3F];
                     _bytes[_index++] = PAD;
                     _bytes[_index] = PAD;
                 } else {
                     _bytes[_index] = alphabetArray[_group & 0x3F];
                 }
                 break;
             case 2:
                 _group = ((bytes[_i++] & 0xFF) << 8 | (bytes[_i] & 0xFF)) << 2;
                 _bytes[_index++] = alphabetArray[_group >>> 12];
                 _bytes[_index++] = alphabetArray[_group >>> 6 & 0x3F];
                 if (usePadding) {
                     _bytes[_index++] = alphabetArray[_group & 0x3F];
                     _bytes[_index] = PAD;
                 } else {
                     _bytes[_index] = alphabetArray[_group & 0x3F];
                 }
                 break;
             default:
                 // this should never happen.
         }
         return _bytes;
     }
 
 	//
 	// Base64 decoding utility, extracted from Apache Commons Codec 1.8
 	//
 
 	private static final int DECODE_SIZE = 3;
 	private static final int BYTES_PER_ENCODED_BLOCK = 4;
 	private static final int BITS_PER_ENCODED_BYTE = 6;
 	private static final int MASK_8BITS = 0xff;
 	private static final int EOF = -1;
	private static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName("UTF-8");
 
 	private static final byte[] DECODE_TABLE = {
 			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
 			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
 			-1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54,
 			55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
 			5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
 			24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
 			35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
 	};
 
 	public static byte[] decode(final String pArray) {
 		return decode(getBytesUtf8(pArray));
 	}
 
 	public static byte[] decode(final byte[] pArray) {
 		if (pArray == null || pArray.length == 0) {
 			return pArray;
 		}
 		final Context context = new Context();
 		decode(pArray, 0, pArray.length, context);
 		decode(pArray, 0, EOF, context); // Notify decoder of EOF.
 		final byte[] result = new byte[context.pos];
 		readResults(result, 0, result.length, context);
 		return result;
 	}
 
 	private static void decode(final byte[] in, int inPos, final int inAvail, final Context context) {
 		if (context.eof) {
 			return;
 		}
 		if (inAvail < 0) {
 			context.eof = true;
 		}
 		for (int i = 0; i < inAvail; i++) {
 			final byte[] buffer = ensureBufferSize(DECODE_SIZE, context);
 			final byte b = in[inPos++];
 			if (b == PAD) {
 				// We're done.
 				context.eof = true;
 				break;
 			} else {
 				if (b >= 0 && b < DECODE_TABLE.length) {
 					final int result = DECODE_TABLE[b];
 					if (result >= 0) {
 						context.modulus = (context.modulus+1) % BYTES_PER_ENCODED_BLOCK;
 						context.ibitWorkArea = (context.ibitWorkArea << BITS_PER_ENCODED_BYTE) + result;
 						if (context.modulus == 0) {
 							buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 16) & MASK_8BITS);
 							buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 8) & MASK_8BITS);
 							buffer[context.pos++] = (byte) (context.ibitWorkArea & MASK_8BITS);
 						}
 					}
 				}
 			}
 		}
 
 		// Two forms of EOF as far as base64 decoder is concerned: actual
 		// EOF (-1) and first time '=' character is encountered in stream.
 		// This approach makes the '=' padding characters completely optional.
 		if (context.eof && context.modulus != 0) {
 			final byte[] buffer = ensureBufferSize(DECODE_SIZE, context);
 
 			// We have some spare bits remaining
 			// Output all whole multiples of 8 bits and ignore the rest
 			switch (context.modulus) {
 //              case 0 : // impossible, as excluded above
 				case 1 : // 6 bits - ignore entirely
 					// TODO not currently tested; perhaps it is impossible?
 					break;
 				case 2 : // 12 bits = 8 + 4
 					context.ibitWorkArea = context.ibitWorkArea >> 4; // dump the extra 4 bits
 					buffer[context.pos++] = (byte) ((context.ibitWorkArea) & MASK_8BITS);
 					break;
 				case 3 : // 18 bits = 8 + 8 + 2
 					context.ibitWorkArea = context.ibitWorkArea >> 2; // dump 2 bits
 					buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 8) & MASK_8BITS);
 					buffer[context.pos++] = (byte) ((context.ibitWorkArea) & MASK_8BITS);
 					break;
 				default:
 					throw new IllegalStateException("Impossible modulus "+context.modulus);
 			}
 		}
 	}
 
 	private static byte[] ensureBufferSize(final int size, final Context context){
 		if ((context.buffer == null) || (context.buffer.length < context.pos + size)){
 			return resizeBuffer(context);
 		}
 		return context.buffer;
 	}
 
 	private static byte[] resizeBuffer(final Context context) {
 		if (context.buffer == null) {
 			context.buffer = new byte[8192];
 			context.pos = 0;
 			context.readPos = 0;
 		} else {
 			final byte[] b = new byte[context.buffer.length * 2];
 			System.arraycopy(context.buffer, 0, b, 0, context.buffer.length);
 			context.buffer = b;
 		}
 		return context.buffer;
 	}
 
 	private static int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
 		if (context.buffer != null) {
 			final int len = Math.min(available(context), bAvail);
 			System.arraycopy(context.buffer, context.readPos, b, bPos, len);
 			context.readPos += len;
 			if (context.readPos >= context.pos) {
 				context.buffer = null; // so hasData() will return false, and this method can return -1
 			}
 			return len;
 		}
 		return context.eof ? EOF : 0;
 	}
 
 	private static int available(final Context context) {
 		return context.buffer != null ? context.pos - context.readPos : 0;
 	}
 
 	private static byte[] getBytesUtf8(final String string) {
		return string.getBytes(UTF_8);
 	}
 
 	private static class Context {
 
 		int ibitWorkArea;
 		long lbitWorkArea;
 		byte[] buffer;
 		int pos;
 		int readPos;
 		boolean eof;
 		int currentLinePos;
 		int modulus;
 
 		Context() {}
 
 		@SuppressWarnings("boxing") // OK to ignore boxing here
 		@Override
 		public String toString() {
 			return String.format("%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, " +
 					"modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(), java.util.Arrays.toString(buffer),
 					currentLinePos, eof, ibitWorkArea, lbitWorkArea, modulus, pos, readPos);
 		}
 	}
 }
