 package main;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class Program {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws NoSuchAlgorithmException {
 		// TODO Auto-generated method stub
 		System.out.println("Hello World!");
 
 		long result = MD5_Hash(45L);
 		System.out.println(result);
 	}
 
 	static long MD5_Hash(long arg) {
 		MessageDigest md = null;
 		try {
 			md = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 
 		md.update(longToByteArr(arg));
 		byte byteData[] = md.digest();
 
 		return byteArrToLong(byteData);
 	}
 
 	/**
 	 * Combines x and y. X||Y
 	 */
 	static long combine28bit(long x, long y){
 		long a = x & 0xfffffff;
 		return ((a << 28) + y);
 	}
 
 	static long byteArrToLong(byte arg[]) {
 		long value = 0;
 		for (int i = 0; i < arg.length; i++) {
 			value += ((long) arg[i] & 0xffL) << (8 * i);
 		}
 		return value;
 	}
 	
 	static byte[] longToByteArr(long x){
 		byte[] res = new byte[8];
		for(int i = 0; i < 8; i ++){
 			res[i] = (byte) (x & (0xff << (8 * i)) >> (8 * i));
 		}
 		return res;
 	}
 }
