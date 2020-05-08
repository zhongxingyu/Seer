 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package common;
 
 /**
  *
  * @author jonsturk
  */
 public class Util {
 
 	public static final int PACKET_SIZE = 65000;
 	
 	public static int getInt(byte[] b, int off){
 
 		int val = 0;
 		int mult = 1;
		for (int i=off+3; i>=off; i--)
		{
 			int toadd = b[i];
			if ( toadd < 0)
			{
 				toadd = 256 + toadd;
 			}
 			val += toadd * mult;
 
 			mult *= 256;
 		}
 
 		return val;
 	}
 
 	public static long getLong(byte[] b, int off){
 		long val = 0;
 		int mult = 1;
		for (int i=off+7; i>=off; i--)
		{
 				int toadd = b[i];
				if ( toadd < 0)
				{
 						toadd = 256 + toadd;
 				}
 				val += toadd * mult;
 
 				mult *= 256;
 		}
 
 		return val;
 	}
 
 	public static void put(int i, byte[] dst, int offset) {
 		dst[offset+0] = (byte)( (i >> 24) % 256 );
 		dst[offset+1] = (byte)( (i >> 16) % 256 );
 		dst[offset+2] = (byte)( (i >>  8) % 256 );
 		dst[offset+3] = (byte)( (i	  ) % 256 );
 	}
 
 	public static void put(long l, byte[] dst, int offset) {
 		dst[offset+0] = (byte)( (l >> 56) % 256 );
 		dst[offset+1] = (byte)( (l >> 48) % 256 );
 		dst[offset+2] = (byte)( (l >> 40) % 256 );
 		dst[offset+3] = (byte)( (l >> 32) % 256 );
 		dst[offset+4] = (byte)( (l >> 24) % 256 );
 		dst[offset+5] = (byte)( (l >> 16) % 256 );
 		dst[offset+6] = (byte)( (l >>  8) % 256 );
 		dst[offset+7] = (byte)( (l	  ) % 256 );
 	}
 
 	public static void put(byte[] src, byte[] dst, int offset) {
 		System.arraycopy(src, 0, dst, offset, src.length);
 	}
 }
