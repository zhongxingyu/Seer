 package Util;
 
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 public class ZlibHelper {
	public static byte[] Decompress(byte[] input,int offset, int count,int originLength) throws DataFormatException{
 		Inflater decompresser = new Inflater();
 		decompresser.setInput(input, offset, count);
		byte[] buffer = new byte[originLength];
 		int resultLength = decompresser.inflate(buffer);
 		decompresser.end();
 		byte[] result = new byte[resultLength];
 		System.arraycopy(buffer, 0, result, 0, resultLength);
 		return result;
 	}
 }
