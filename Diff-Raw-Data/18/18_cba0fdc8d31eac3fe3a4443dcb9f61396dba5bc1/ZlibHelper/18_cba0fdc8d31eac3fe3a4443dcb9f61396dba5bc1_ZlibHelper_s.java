 package Util;
 
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 public class ZlibHelper {
	private static byte[] buffer = new byte[1024*10000];
	public static byte[] Decompress(byte[] input,int offset, int count) throws DataFormatException{
 		Inflater decompresser = new Inflater();
 		decompresser.setInput(input, offset, count);
 		int resultLength = decompresser.inflate(buffer);
 		decompresser.end();
 		byte[] result = new byte[resultLength];
 		System.arraycopy(buffer, 0, result, 0, resultLength);
 		return result;
 	}
 }
