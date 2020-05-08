 
 package axirassa.util;
 
 import java.security.SecureRandom;
 
 /**
  * Lightweight singleton class for generating random strings of a given length;
  * powered by {@link SecureRandom}.
  * 
  * @author wiktor
  * 
  */
 public class RandomStringGenerator {
	public static RandomStringGenerator getInstance () {
 		if (instance == null)
 			instance = new RandomStringGenerator();
 		return instance;
 	}
 
 
 	private static RandomStringGenerator instance;
 	private final SecureRandom random = new SecureRandom();
 
 
	private RandomStringGenerator () {
 	}
 
 
	public static String makeRandomString (int length) {
 		return getInstance().randomString(length);
 	}
 
 
	public static String makeRandomStringToken (int length) {
 		return getInstance().randomStringToken(length);
 	}
 
 
 	/**
 	 * @return a string of the given length containing random bytes (except
 	 *         0x00)
 	 */
	public String randomString (int length) {
 		byte[] buffer = new byte[length];
 
 		for (int i = 0; i < buffer.length; i++)
 			if (buffer[i] == 0)
 				buffer[i] = randomNonZeroByte();
 
		random.nextBytes(buffer);
 		return new String(buffer);
 	}
 
 
	public byte randomNonZeroByte () {
 		return (byte) (random.nextInt(254) + 1);
 	}
 
 
	public String randomStringToken (int length) {
 		byte[] buffer = new byte[length];
 		int index = 0;
 		while (index < length) {
 			long value = random.nextLong();
 
 			if (value < 0)
 				value = -value;
 
 			String str = Long.toString(value, 32);
 			byte[] byteString = str.getBytes();
 
 			for (int i = 0; i < byteString.length && index < length; i++, index++)
 				buffer[index] = byteString[i];
 		}
 
 		return new String(buffer);
 	}
 }
