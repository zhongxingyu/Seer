 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class MD5Checksum {
 	// Real's Java-How-to
 	public static byte[] createChecksum(InputStream fis) throws IOException {
 		byte[] buffer = new byte[1024];
 		MessageDigest complete;
 		try {
 			complete = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			return null;
 		}
 		int numRead;
 
 		do {
 			numRead = fis.read(buffer);
 			if (numRead > 0) {
 				complete.update(buffer, 0, numRead);
 			}
 		} while (numRead != -1);
 
 		fis.close();
 		return complete.digest();
 	}
 
 	public static String getMD5Checksum(InputStream is) throws IOException {
 		byte[] b = createChecksum(is);
 		String result = "";
 
 		for (int i = 0; i < b.length; i++) {
 			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
 		}
 		return result;
 	}
}
