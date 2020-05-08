 package cryolite.util;
 
 public class Utils {
 
 	public static int readInt(byte[] b, int offset) {
 		return 
 			(b[offset + 0] & 0xff) << 24 |
 			(b[offset + 1] & 0xff) << 16 |
 			(b[offset + 2] & 0xff) <<  8 |
 			(b[offset + 3] & 0xff) <<  0;
 	}
 	
	public static void writeInt(byte[] b, int c, int offset) {
 		b[offset + 0] = (byte) (c >>> 24);
 		b[offset + 1] = (byte) (c >>> 16);
 		b[offset + 2] = (byte) (c >>>  8);
 		b[offset + 3] = (byte) (c >>>  0);
 	}
 
 	public static int readInt(byte[] b) {
 		return 
 			(b[0] & 0xff) << 24 |
 			(b[1] & 0xff) << 16 |
 			(b[2] & 0xff) <<  8 |
 			(b[3] & 0xff) <<  0;
 	}
 	
 	public static void writeInt(byte[] b, int c) {
 		b[0] = (byte) (c >>> 24);
 		b[1] = (byte) (c >>> 16);
 		b[2] = (byte) (c >>>  8);
 		b[3] = (byte) (c >>>  0);
 	}
 }
