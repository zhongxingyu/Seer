 package blackdoor.util;
 
 public class Misc {
 	
 	/**
 	 * Performs an XOR operation on two arrays of bytes, byte by byte.
 	 * The returned array will be the same length as the longest parameter array,
 	 * the shorter array will be padded with 0's.
 	 * @param array1
 	 * @param array2
 	 * @return array1 XOR array2.
 	 */
 	public static byte[] XOR(byte[] array1, byte[] array2){
 		int length;
 		boolean trueIfOne;
 		if(array1.length >= array2.length){
 			length = array1.length;
 			trueIfOne = true;
 		}
 		else {
 			length = array2.length;
 			trueIfOne = false;
 		}
 		byte[] array3 = new byte[length];
 		int i = 0;
 		if(trueIfOne){
 			for(byte a:array2){
 				array3[i] = (byte) (a ^ array1[i++]);
 			}
 			System.arraycopy(array1, i, array3, i, length-i);
 		}
 		else{
 			for(byte a:array1){
 				array3[i] = (byte) (a ^ array2[i++]);
 			}
 			System.arraycopy(array2, i, array3, i, length-i);
 		}
 		return array3;
 	}
 }
