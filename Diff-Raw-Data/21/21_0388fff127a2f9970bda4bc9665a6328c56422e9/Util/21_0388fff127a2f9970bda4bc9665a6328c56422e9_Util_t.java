 
 package common;
 
 import com.jme.math.Vector3f;
 
 /**
  *
  * @author jonsturk
  */
 public class Util {
 
 	public static final byte CONTROLLED_PACKET_MASK = (byte)0x80; 
 	public static final byte CONTROLLED_PACKET_UNMASK = (byte)0x7F; 
 	public static final int PACKET_SIZE = 65000;
 	public static final byte[] nullbytes = new byte[1];
 	
 	
 	/**
 	 * reads 4 bytes (off, off+1, off+2 and off+3) from b and composits them 
 	 * to an int. The most significant byte will be at b[off].
 	 * @param b a byte array 
 	 * @param off offset to start reading the integer
 	 * @return the composited integer
 	 */
 	public static int getInt(byte[] b, int off) {
 		int val = 0;
 		int mult = 1;
 		for (int i=off+3; i>=off; i--){
 			int toadd = b[i];
 			if ( toadd < 0){
 				toadd = 256 + toadd;
 			}
 			val += toadd * mult;
 
 			mult *= 256;
 		}
 
 		return val;
 	}
 	
 	/**
 	 * reads 4 bytes from b and composits them into a float.
 	 * @param b a byte array
 	 * @param off the offset to start reading at
 	 * @return the composited float
 	 */
 	public static float getFloat(byte[] b, int off) {
 		return Float.intBitsToFloat(getInt(b, off));
 	}
 	
 	/**
 	 * Fills v with three floats read from off, off+4 and off+8.
 	 * @param b array to read bytes from
 	 * @param off offset to start reading at
 	 * @param v vector to fill 
 	 */
 	public static Vector3f getVector3f(byte[] b, int off, Vector3f v) {
 		v.x = getFloat(b, off);
 		v.y = getFloat(b, off+4);
 		v.z = getFloat(b, off+8);
 		return v;
 	}
	
	public static String getString(byte[] b, int off, int len) {
		return new String(b, off, len);
	}
	
	public static String getString(byte[] b, int off) {
		return new String(b, off, b.length - off);
	}
 
 	/**
 	 * reads 8 bytes from b and composits them 
 	 * to a long. The most significant byte will be at b[off].
 	 * @param b a byte array 
 	 * @param off offset to start reading the long
 	 * @return the composited long
 	 */
 	public static long getLong(byte[] b, int off) {
 		long val = 0;
 		long mult = 1;
 		for (int i=off+7; i>=off; i--){
 				int toadd = b[i];
 				if ( toadd < 0){
 						toadd = 256 + toadd;
 				}
 				val += toadd * mult;
 
 				mult *= 256;
 		}
 
 		return val;
 	}
 
 	/**
 	 * splits an int to 4 bytes and puts them in dst, starting at offset. The 
 	 * most significant byte is placed at dst[offset].
 	 * @param i the integer to put
 	 * @param dst the target array
 	 * @param offset the offset at which to start putting the values
 	 */
 	public static void put(int i, byte[] dst, int offset) {
 		dst[offset+0] = (byte)( (i >> 24) % 256 );
 		dst[offset+1] = (byte)( (i >> 16) % 256 );
 		dst[offset+2] = (byte)( (i >>  8) % 256 );
 		dst[offset+3] = (byte)( (i      ) % 256 );
 	}
 	
 	/**
 	 * splits a float to 4 bytes and puts them in dst, starting at offset.
 	 * @param f the float to put
 	 * @param dst the target array
 	 * @param offset the offset at which to start putting the values
 	 */
 	public static void put(float f, byte[] dst, int offset) {
 		int i = Float.floatToRawIntBits(f);
 		put(i, dst, offset);
 	}
 	
 	/**
 	 * Takes the floats from v and puts them at offset, offset+4 and offset+8
 	 * @param v vector of floats
 	 * @param dst target array
 	 * @param offset the offset for x
 	 */
 	public static void put(Vector3f v, byte[] dst, int offset) {
 		put(v.x, dst, offset);
 		put(v.y, dst, offset+4);
 		put(v.z, dst, offset+8);
 	}
 
 	/**
 	 * splits a long to 8 bytes and puts them in dst, starting at offset. The 
 	 * most significant byte is placed at dst[offset].
 	 * @param l the long to put
 	 * @param dst the target array
 	 * @param offset the offset at which to start putting the values
 	 */
 	public static void put(long l, byte[] dst, int offset) {
 		dst[offset+0] = (byte)( (l >> 56) % 256 );
 		dst[offset+1] = (byte)( (l >> 48) % 256 );
 		dst[offset+2] = (byte)( (l >> 40) % 256 );
 		dst[offset+3] = (byte)( (l >> 32) % 256 );
 		dst[offset+4] = (byte)( (l >> 24) % 256 );
 		dst[offset+5] = (byte)( (l >> 16) % 256 );
 		dst[offset+6] = (byte)( (l >>  8) % 256 );
 		dst[offset+7] = (byte)( (l      ) % 256 );
 	}
 
 	/**
 	 * Copies the values from src to dst, starting at dst[offset]. Will copy
 	 * the full length of src.
 	 * @param src the array to copy from
 	 * @param dst the array to copy to
 	 * @param offset the first position in dst
 	 * @throws ArrayIndexOutOfBoundsException if offset+src.length > dst.length
 	 * @throws NullPointerException if src or dst is null
 	 */
 	public static void put(byte[] src, byte[] dst, int offset) {
 		System.arraycopy(src, 0, dst, offset, src.length);
 	}
 	
 	public static String hex(byte b) {
 		StringBuilder sb = new StringBuilder();
 		hex(b, sb);
 		return sb.toString();
 	}
 	
 	public static void hex(byte b, StringBuilder sb) {
 		char[] c = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
 		int i = b;
 		
 		if ( i < 0){
 			i = 256 + i;
 		}
 		
 		int j = i/16;
 		
 		sb.append( c[j] );
 		sb.append( c[i - j*16] );
 	}
 	
 	public static String hex(byte[] b) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (byte bb : b) {
 			hex(bb, sb);
 		}
 		return sb.toString();
 	}
 }
