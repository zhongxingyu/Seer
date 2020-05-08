 package com.todesking.hongodbm;
 
import java.nio.charset.Charset;
 
 public class BinaryUtils {
 	public static byte[] encodeLong(long value) {
 		return new byte[] {
 			(byte) (0xff & (value >> 56)),
 			(byte) (0xff & (value >> 48)),
 			(byte) (0xff & (value >> 40)),
 			(byte) (0xff & (value >> 32)),
 			(byte) (0xff & (value >> 24)),
 			(byte) (0xff & (value >> 16)),
 			(byte) (0xff & (value >> 8)),
 			(byte) (0xff & value) };
 	}
 
 	public static byte[] encodeInt(int value) {
 		return new byte[] {
 			(byte) (0xff & (value >> 24)),
 			(byte) (0xff & (value >> 16)),
 			(byte) (0xff & (value >> 8)),
 			(byte) (0xff & value) };
 	}
 
 	public static int decodeInt(byte[] value) {
 		if (value.length != 4)
 			throw new IllegalArgumentException();
 		int result = 0;
 		for (int i = 0; i < 4; i++)
 			result |= (value[i] & 0xFF) << (24 - 8 * i);
 		return result;
 	}
 
 	public static long decodeLong(byte[] value) {
 		if (value.length != 8)
 			throw new IllegalArgumentException();
 		long result = 0L;
 		for (int i = 0; i < 8; i++)
 			result |= ((long) value[i] & 0xFF) << (56 - 8 * i);
 		return result;
 	}
 
	private static final Charset UTF8 = Charset.forName("UTF-8");

 	public static byte[] encodeString(String value) {
		return value.getBytes(UTF8);
 	}
 
 	public static String decodeString(byte[] value) {
		return new String(value, UTF8);
 	}
 
 	public static int compare(byte[] l, byte[] r) {
 		for (int i = 0; i < l.length && i < r.length; i++) {
 			if (l[i] < r[i])
 				return -1;
 			else if (l[i] > r[i])
 				return 1;
 		}
 		return l.length == r.length ? 0 : l.length < r.length ? -1 : 1;
 	}
 }
