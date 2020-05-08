 package com.macrohuang.fileq.util;
 
 /**
  * 
  * @author macro
  *
  */
 public class NumberBytesConvertUtil {
 	private static final int[] INT_BASE = new int[] { 0xff000000, 0x00ff0000, 0x0000ff00, 0x000000ff };
 	private static final long[] LONG_BASE = new long[] { 0xff00000000000000L, 0x00ff000000000000L, 0x0000ff0000000000L,
 			0x000000ff00000000L, 0x00000000ff000000L, 0x0000000000ff0000L, 0x000000000000ff00L, 0x00000000000000ffL };
 	private static final int SIZE_OF_INT = 4;
 	private static final int SIZE_OF_LONG = 8;
 
 	public static byte[] int2ByteArr(int a) {
 		byte[] result = new byte[SIZE_OF_INT];
 		for (int i = 0; i < SIZE_OF_INT; i++) {
 			result[i] = (byte) ((a & INT_BASE[i]) >> ((SIZE_OF_INT - 1 - i) * 8));
 		}
 		return result;
 	}
 
 	/**
 	 * Convert some bytes (max to 4) to an integer.
 	 * 
 	 * @param bytes
 	 *            The big-end encoding bytes of an integer.
 	 * @return
 	 */
 	public static int byteArr2Int(byte[] bytes) {
 		if (bytes == null || bytes.length == 0) {
 			throw new IllegalArgumentException("Required at least one byte, but receive null or empty.");
 		}
 		if (bytes[0] > 0x7f) {
 			throw new NumberFormatException("An integer's biggest byte can't be more than 0x7f");
 		}
 		int result = 0;
 		for (int i = 0; i < bytes.length; i++) {
 			result |= bytes[i] << ((bytes.length - 1 - i) * 8) & INT_BASE[INT_BASE.length - bytes.length + i];
 		}
 		return result;
 	}
 
 	public static byte[] long2ByteArr(long a) {
 		byte[] result = new byte[SIZE_OF_LONG];
 		for (int i = 0; i < SIZE_OF_LONG; i++) {
 			result[i] = (byte) ((a & LONG_BASE[i]) >> ((SIZE_OF_LONG - 1 - i) * 8));
 		}
 		return result;
 	}
 
 	/**
 	 * Convert some bytes (max to 8) to a long integer.
 	 * 
 	 * @param bytes
 	 *            The big-end encoding bytes of a long integer.
 	 * @return
 	 */
 	public static long byteArr2Long(byte[] bytes) {
 		if (bytes == null || bytes.length == 0 || bytes.length > SIZE_OF_LONG) {
 			throw new IllegalArgumentException("Required at least one byte, at most " + SIZE_OF_LONG + " bytes.");
 		}
 		if (bytes[0] > 0x7f) {
 			throw new NumberFormatException("An long integer's biggest byte can't be more than 0x7f");
 		}
 		long result = 0;
 		for (int i = 0; i < bytes.length; i++) {
			result |= bytes[i] << ((bytes.length - 1 - i) * 8) & LONG_BASE[LONG_BASE.length - bytes.length + i];
 		}
 		return result;
 	}
 }
