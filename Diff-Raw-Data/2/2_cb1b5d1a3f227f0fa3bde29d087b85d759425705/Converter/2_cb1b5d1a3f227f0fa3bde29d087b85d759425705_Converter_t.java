 package zone.util;
 
 public class Converter {
 	public static void short2Array(short value, byte[] bytes, int offset) {
 		bytes[offset + 1] = (byte)(value >> 8 & 0xff);
 		bytes[offset + 0] = (byte)(value & 0xff);
 	}
 	
 	public static short array2Short(byte[] bytes, int offset) {
 		return new Integer(((bytes[offset + 1] & 0xFF) << 8) + (bytes[offset + 0] & 0xFF)).shortValue();
 	}
 	
 	
 	public static void int2Array(int value, byte[] bytes, int offset) {
 		bytes[offset + 3] = (byte)(value >> 24);
 		bytes[offset + 2] = (byte)(value >> 16 & 0xff);
 		bytes[offset + 1] = (byte)(value >> 8 & 0xff);
 		bytes[offset + 0] = (byte)(value & 0xff);
 	}
 	
 	public static int array2Int(byte[] bytes, int offset) {
 		return (bytes[offset + 3] << 24) + ((bytes[offset + 2] & 0xFF) << 16)
 			+ ((bytes[offset + 1] & 0xFF) << 8) + (bytes[offset + 0] & 0xFF);
 	}
 	
 	public static void long2Array(long value, byte[] bytes, int offset) {
 		bytes[offset + 7] = (byte)(value >> 56 & 0xff);
 		bytes[offset + 6] = (byte)(value >> 48 & 0xff);
 		bytes[offset + 5] = (byte)(value >> 40 & 0xff);
 		bytes[offset + 4] = (byte)(value >> 32 & 0xff);
 		bytes[offset + 3] = (byte)(value >> 24 & 0xff);
 		bytes[offset + 2] = (byte)(value >> 16 & 0xff);
 		bytes[offset + 1] = (byte)(value >> 8 & 0xff);
 		bytes[offset + 0] = (byte)(value & 0xff);
 	}
 	
 	public static long array2Long(byte[] bytes, int offset) {
 		return (((long) bytes[offset + 7] & 0xFF) << 56) + (((long) bytes[offset + 6] & 0xFF) << 48)
 		+ (((long) bytes[offset + 5] & 0xFF) << 40) + (((long) bytes[offset + 4] & 0xFF) << 32)
		+ ((bytes[offset + 3] & 0xFF) << 24) + ((bytes[offset + 2] & 0xFF) << 16)
 		+ ((bytes[offset + 1] & 0xFF) << 8) + (bytes[offset + 0] & 0xFF);
 	}
 	
 	public static void double2Array(double value, byte[] bytes, int offset) {
 		long2Array(Double.doubleToLongBits(value), bytes, offset);
 	}
 	
 	public static double array2Double(byte[] bytes, int offset) {
 		return Double.longBitsToDouble(array2Long(bytes, offset));
 	}
 }
