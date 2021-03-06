 /**
  * Part of the CCNx Java Library.
  *
  * Copyright (C) 2008, 2009 Palo Alto Research Center, Inc.
  *
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License version 2.1
  * as published by the Free Software Foundation. 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details. You should have received
  * a copy of the GNU Lesser General Public License along with this library;
  * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
  * Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.ccnx.ccn.impl.support;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 import java.nio.charset.UnsupportedCharsetException;
 import java.util.ArrayList;
 
 import org.bouncycastle.util.encoders.Base64;
 import org.ccnx.ccn.config.SystemConfiguration;
 
 /**
  * Miscellaneous utility routines for CCN, mostly data comparison and conversion.
  */
 public class DataUtils {
 	
 	public static final int BITS_PER_BYTE = 8;
 	public static final String EMPTY = "";	
 
 	/**
 	 * Useful when we move over to 1.6, and can avoid UnsupportedCharsetExceptions this way.
 	 */
 	public static Charset UTF8_CHARSET;
 	
 	static {
 		try {
 			UTF8_CHARSET = Charset.forName("UTF-8");
 			if (null == UTF8_CHARSET) {
 				// This shouldn't happen, but be noisy about it if it does...
 				throw new UnsupportedCharsetException("Attempt to retrieve the UTF-8 charset returned null! Significant configuration error!");
 			}
 		} catch (Exception e) { // Should be UnsupportedCharsetException or IllegalCharsetNameException
 			Log.severe("Unknown encoding UTF-8! This is a significant configuration problem.");
 			throw new RuntimeException("Cannot find UTF-8 encoding. Significant configuration error");
 		}
 	}
 		
 	public static class Tuple<A, B> {
 		
 		A _first;
 		B _second;
 		
 		public Tuple(A first, B second) {
 			_first = first;
 			_second = second;
 		}
 		
 		public A first() { return _first; }
 		public B second() { return _second; }
 		public void setFirst(A first) { _first = first; }
 		public void setSecond(B second) { _second = second; }
 	}
 
 	public static <T extends Comparable<T>> int compare(T left, T right) {
 		int result = 0;
 		if (null != left) {
 			if (null == right) 
 				return 1; // sort nothing before something
 			result = left.compareTo(right);
 		} else {
 			if (null != right)
 				result = -1; // sort nothing before something
 			// else fall through and compare publishers
 			else
 				result = 0; // null == null
 		}
 		return result;
 	}
 	
 	/**
	 * Perform a lexigraphical comparison of byte arrays in canonical CCN ordering
 	 * @param left
 	 * @param right
 	 * @return < 0 if left comes before right, 0 if they are equal, > 0 if left comes after right
 	 */
 	public static int compare(byte [] left, byte [] right) {
 		int result = 0;
 		if (null != left) {
 			if (null == right) {
 				result = 1;
 			} else {
 				// If a is shorter than b then a comes before b
 				if (left.length < right.length) {
 					result = -1;
 				} else if (left.length > right.length) {
 					result = 1;
 				} else {
 					// They have equal lengths - compare byte by byte
 					for (int i=0; i < left.length; ++i) {
 						if ((short)(left[i] & 0xff) < (short)(right[i] & 0xff)) {
 							result = -1;
 							break;
 						} else if ((short)(left[i] & 0xff) > (short)(right[i] & 0xff)) {
 							result = 1;
 							break;
 						}
 					}
 				}
 			}
 		} else {
 			if (null != right)
 				result = -1; // sort nothing before something
 			// else fall through and compare publishers
 			else
 				result = 0; // null == null
 		}
 		return result;
 	}
 	
 	/**
 	 * @see compare(byte[], byte[])
 	 */
 	public static int compare(ArrayList<byte []> left, ArrayList<byte []> right) {
 
 		int result = 0;
 		if (null != left) {
 			if (null == right) {
 				result = 1;
 			} else {
 				// here we have the comparison.
 				int minlen = (left.size() < right.size()) ? left.size() : right.size();
 				for (int i=0; i < minlen; ++i) {
 					result = compare(left.get(i), right.get(i));
 					if (0 != result) break;
 				}
 				if (result == 0) {
 					// ok, they're equal up to the minimum length
 					if (left.size() < right.size()) {
 						result = -1;
 					} else if (left.size() > right.size()) {
 						result = 1;
 					}
 					// else they're equal, result = 0
 				}
 			}
 		} else {
 			if (null != right)
 				result = -1; // sort nothing before something
 			// else fall through and compare publishers
 			else
 				result = 0; // null == null
 		}
 		return result;
 	}
 
 	/**
 	 * Used to print non ASCII components for logging, etc.
 	 * 
 	 * @param bytes
 	 * @return the data as a BigInteger String
 	 */
 	public static String printBytes(byte [] bytes) {
 		if (bytes == null) {
 			return "";
 		}
 		BigInteger bi = new BigInteger(1, bytes);
 		return bi.toString(SystemConfiguration.DEBUG_RADIX);
 	}
 	
 	/**
 	 * Used to print components to be interpreted as hexadecimal such as segments
 	 * @param bytes
 	 * @return the data as a Hexadecimal String
 	 */
 	public static String printHexBytes(byte [] bytes) {
 		if ((null == bytes) || (bytes.length == 0)) {
 			return "<empty>";
 		}
 		BigInteger bi = new BigInteger(1, bytes);
 		return bi.toString(16);
 	}
 	
 	/**
 	 * A place to centralize interfaces to base64 encoding/decoding, as the classes
 	 * we use change depending on what ships with Java.
 	 */
 	
 	public static byte [] base64Decode(byte [] input) throws IOException {
 		return Base64.decode(input);
 	}
 	
 	public static byte [] base64Encode(byte [] input) {
 		return Base64.encode(input);
 	}
 
 	/**
 	 * byte array compare
 	 * @param left
 	 * @param right
 	 * @return true if equal
 	 */
 	public static boolean arrayEquals(byte[] left, byte[] right) {
 		if (left == null) {
 			return ((right == null) ? true : false);
 		}
 		if (right == null) {
 			return ((left == null) ? true : false);
 		}
 		if (left.length != right.length)
 			return false;
 		for (int i = 0; i < left.length; i++) {
 			if (left[i] != right[i])
 				return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * byte array compare
 	 * @param left
 	 * @param right
 	 * @param length
 	 * @return true if equal
 	 */
 	public static boolean arrayEquals(byte[] left, byte[] right, int length) {
 		if (left == null) {
 			return ((right == null) ? true : false);
 		}
 		if (right == null) {
 			return ((left == null) ? true : false);
 		}
		if (left.length < length || right.length < length)
			return false;
		for (int i = 0; i < length; i++) {
 			if (left[i] != right[i])
 				return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Check if a byte array starts with a certain prefix.
 	 * 
 	 * Used to check for binary prefixes used to mark certain ContentName components for special purposes.
 	 * 
 	 * @param prefix bytes to look for, if null this method always returns true.
 	 * @param data data to inspect. If null this method always returns false.
 	 * @return true if data starts with prefix.
 	 */
 	public static boolean isBinaryPrefix(byte [] prefix,
 										 byte [] data) {
 		if ((null == prefix) || (prefix.length == 0))
 			return true;
 		if ((null == data) || (data.length < prefix.length))
 			return false;
 		for (int i=0; i < prefix.length; ++i) {
 			if (prefix[i] != data[i])
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Recursively delete a directory and all its contents.
 	 * If given File does not exist, this method returns with no error 
 	 * but if it exists as a file not a directory, an exception will be thrown.
 	 * Similar to org.apache.commons.io.FileUtils.deleteDirectory
 	 * but avoids dependency on that library for minimal use.
 	 * @param directory
 	 * @throws IOException if "directory" is a file
 	 */
 	public static void deleteDirectory(File directory) throws IOException {
 		if (!directory.exists()) {
 			return;
 		}
 		if (!directory.isDirectory()) {
 			throw new IOException(directory.getPath() + " is not a directory");
 		}
 		for (File child : directory.listFiles()) {
 			if (child.isDirectory()) {
 				deleteDirectory(child);
 			} else {
 				child.delete();
 			}
 		}
 		directory.delete();
 	}
 
 	/**
 	 * This was used in early content demos; keep it around as it may be generally useful.
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static byte[] getBytesFromFile(File file) throws IOException {
 	    InputStream is = new FileInputStream(file);
 	
 	    // Get the size of the file
 	    long length = file.length();
 	
 	    if (length > Integer.MAX_VALUE) {
 	        // File is too large
 	    }
 	
 	    // Create the byte array to hold the data
 	    byte[] bytes = new byte[(int)length];
 	
 	    // Read in the bytes
 	    int offset = 0;
 	    int numRead = 0;
 	    while (offset < bytes.length
 	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
 	        offset += numRead;
 	    }
 	
 	    // Ensure all the bytes have been read in
 	    if (offset < bytes.length) {
 	        throw new IOException("Could not completely read file "+file.getName());
 	    }
 	
 	    // Close the input stream and return bytes
 	    is.close();
 	    return bytes;
 	}
 	
 	/**
 	 * Read a stream (usually small) completely in to a byte array. Used to get all of the
 	 * bytes out of one or more content objects for decoding or other processing, where the
 	 * content needs to be handed to something else as a unit.
 	 */
 	public static byte [] getBytesFromStream(InputStream input) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		byte [] buf = new byte[1024];
 		int byteCount = 0;
 		byteCount = input.read(buf);
 		while (byteCount > 0) {
 			baos.write(buf, 0, byteCount);
 			byteCount = input.read(buf);
 		}
 		return baos.toByteArray();
 	}
 	
 	/**
 	 * Wrap up handling of UTF-8 encoding in one place (as much as possible), because
 	 * an UnsupportedEncodingException in response to a request for UTF-8 signals
 	 * a significant configuration error; we should catch it and signal a RuntimeException
 	 * in one place and let the rest of the code not worry about it.
 	 */
 	public static String getUTF8StringFromBytes(byte [] stringBytes) {
 		try {
 			// Version taking a Charset not available till 1.6. 
 			return new String(stringBytes, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			Log.severe("Unknown encoding UTF-8! This is a significant configuration problem.");
 			throw new RuntimeException("Unknown encoding UTF-8! This is a significant configuration problem.");
 		}
 	}
 	
 	/**
 	 * Wrap up handling of UTF-8 encoding in one place (as much as possible), because
 	 * an UnsupportedEncodingException in response to a request for UTF-8 signals
 	 * a significant configuration error; we should catch it and signal a RuntimeException
 	 * in one place and let the rest of the code not worry about it.
 	 */
 	public static byte [] getBytesFromUTF8String(String stringData) {
 		try {
 			// Version taking a Charset not available till 1.6. 
 			return stringData.getBytes("UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			Log.severe("Unknown encoding UTF-8! This is a significant configuration problem.");
 			throw new RuntimeException("Unknown encoding UTF-8! This is a significant configuration problem.");
 		}
 	}
 	
 	/**
 	 * Lexicographically compare two byte arrays, looking at a limited number of bytes.
 	 * @param arr1
 	 * @param arr2
 	 * @param count Maximum number of bytes to inspect.
 	 * @return < 0 if left comes before right, 0 if they are equal, > 0 if left comes after right
 	 */
 	public static int bytencmp(byte[] arr1, byte[] arr2, int count) {
 		if (null == arr1) {
 			if (null == arr2)
 				return 0;
 			return 1;
 		}
 		if (null == arr2)
 			return -1;
 		
 		int cmpcount = Math.min(Math.min(count, arr1.length), arr2.length);
 		for (int i=0; i < cmpcount; ++i) {
 			if (arr1[i] < arr2[i])
 				return -1;
 			if (arr2[i] > arr1[i])
 				return 1;
 		}
 		if (cmpcount == count)
 			return 0;
 		// OK, they match up to the length of the shortest one, which is shorter
 		// than count. Whichever is shorter is less.
 		if (arr1.length > arr2.length)
 			return 1;
 		if (arr1.length < arr2.length)
 			return -1;
 		return 0;
 	}
 }
