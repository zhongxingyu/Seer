 package utils;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class FileUtils {
 
 	public static String readNullTerminatedString(InputStream is)
 			throws IOException {
 		StringBuilder sb = new StringBuilder();
 		while (true) {
 			char c = (char) is.read();
 			if (c == '\0') {
 				return sb.toString();
 			}
 			sb.append(c);
 		}
 	}
 
 	public static long readBleLong(DataInputStream is, int bytes) throws IOException {
 		long num = 0;
 		for (int i = 0; i < bytes; i++) {
 			int b = is.read();
 			if (b != 0x00) {
 				num += ((long) b) << (i * 8);
 			}
 		}
 		if (num < 0) {
			throw new RuntimeException("oveflow " + Long.toHexString(num));
 		}
 		return num;
 	}
 
 	public static int readBleInt(DataInputStream is, int bytes) throws IOException {
 		return (int) readBleLong(is, bytes);
 	}
 
 }
