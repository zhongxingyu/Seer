 package miniRSAChat;
 
 /**
  * @author Jie Qi, Jiuzheng Chen
  * @version April 20, 2012
  * This class provides RSA algorithm implementation and encrypt/decrypt on it. 
  */
 
 public class RSA {
 	// Public key contains N and e
 	public static class PublicKey {
 		public final long N;
 		public final long e;
 		// Constructs a PublicKey
 		PublicKey(long N, long e) {
 			this.N = N;
 			this.e = e;
 		}
 	}
 	
 	// Private key contains N and d
 	public static class PrivateKey {
 		public final long N;
 		public final long d;
 		// Constructs a PrivateKey
 		PrivateKey(long N, long d) {
 			this.N = N;
 			this.d = d;
 		}
 	}
 
 	/**
 	 * Key pair of public and private key
 	 */
 	public static class RSAKeyPair {
 		public final PublicKey publicKey;
 		public final PrivateKey privateKey;
 		RSAKeyPair(PublicKey publicKey, PrivateKey privateKey) {
 			this.publicKey = publicKey;
 			this.privateKey = privateKey;
 		}
 	}
 	
 	/**
 	 * Encrpyt char plaintext to long ciphertext with public key e in RSA algorithm.
 	 * @param publicKey Public key used in RSA to encrpyt.
 	 * @param ch Plaintext
 	 * @return Encrypted ciphertext.
 	 */
 	public static long encryptChar(PublicKey publicKey, char ch) {
 		long chValue = (long)ch;
 		long resultValue = 1;
 		// encryption in mode N
 		for (int i = 0; i < publicKey.e; i++) {
 			resultValue *= chValue;
 			resultValue = resultValue % publicKey.N;
 		}
 		return resultValue;
 	}
 	
 	/**
 	 * Encrypt plaintext string to long ciphertext with public key e in RSA
 	 * @param publicKey Public key used in RSA to encrpyt
 	 * @param plaintext Plaintext to encrpyt.
 	 * @return Encrypted ciphertext.
 	 */
 	public static long[] encrpyt(PublicKey publicKey, String plaintext) {
 		long[] ciphertext = new long[plaintext.length()];
 		char[] plainChars = plaintext.toCharArray();
 		// encryption char by char
 		for (int i = 0; i < plainChars.length; i++) {
 			ciphertext[i] = encryptChar(publicKey, plainChars[i]);
 		}
 		return ciphertext;
 	}
 
 	/**
 	 * Decrypt long ciphertext to plaintext char with private key in RSA
 	 * @param privateKey Private key used in RSA to decrpyt
 	 * @param ciphertext Input ciphertext to deprypt.
 	 * @return Decrypted plaintext.
 	 */
 	public static char decrypt(PrivateKey privateKey, long ciphertext) {
 		long cipherValue = ciphertext;
 		long resultValue = 1;
 		for (int i = 0; i < privateKey.d; i++) {
 			resultValue *= cipherValue;
 			resultValue = resultValue % privateKey.N;
 		}
 		char result = (char)resultValue;
 		return result;
 	}
 	
 	/**
 	 * Decrypt long array of ciphertext to string plaintext with private key in RSA
 	 * @param privateKey Private key used in RSA to decrpyt
 	 * @param ciphertext Input ciphertext to deprypt.
 	 * @return Decrypted plaintext.
 	 */
 	public static String decrypt(PrivateKey privateKey, long[] ciphertext) {
 		String plaintext = "";
 		for (int i = 0; i < ciphertext.length; i++) {
 			plaintext += String.valueOf(decrypt(privateKey, ciphertext[i]));
 		}
 		return plaintext;
 	}
 
 	/**
 	 * Check if a number is a prime number
 	 * @param number
 	 * @return True if the number is prime number,
 	 *         False if not.
 	 */
 	private static boolean isPrime(long number) {
 		if (number % 2 == 0) return false;
		for (long i = 3; i < Math.sqrt(number); i+= 2) {
 			if (number % i == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Get the prime number in the position of given index
 	 * @param index Position of prime number.
 	 * @return The index-th prime number in ascending oder.
 	 */
 	private static long primeNumber(int index) {
 		long primeNumber = 1;
 		long count = 0;
 		while (count < index) {
 			primeNumber++;
 			if (isPrime(primeNumber)) {
 				count++;
 			}
 		}
 		return primeNumber;
 	}
 	
 	/**
 	 * Recursively computes the GCD of two numbers a and b
 	 * Use Stein's binary GCD algorithm, and following is an updated version of code in wiki
 	 * @see http://en.wikipedia.org/wiki/Binary_GCD_algorithm
 	 */
 	private static long getGCD(long a, long b) {
 		// Simple cases
 		if (a == 0) {
 			return b;
 		}
 		if (b == 0) {
 			return a;
 		}
 		if (a == b) {
 			return a;
 		}
 		// look for factors of 2
 		if (a % 2 == 0 && b % 2 == 0) {
 			return 2 * getGCD(a >> 1, b >> 1);
 		}
 		if (a % 2 != 0 && b % 2 == 0) {
 			return getGCD(a, b >> 1);
 		}
 		if (a % 2 == 0 && b % 2 != 0) {
 			return getGCD(a >> 1, b);
 		}
 		if (a % 2 != 0 && b % 2 != 0) {
 			return getGCD(Math.min(a, b), Math.abs(a - b));
 		}
 		return 1;
 	}
 	
 	/**
 	 * Pick a random integer that is coprime to and no larger than the given input 
 	 * @param input Given long integer
 	 * @return a random integer coprime to input
 	 */
 	public static long coprime(long input) {
 		long coprimeNumber = 0;
 		do {
 			coprimeNumber = Math.abs(Global.rand.nextLong());
 			// the coprime number is no larger than the given input
 			coprimeNumber %= input; 
 		} while (getGCD(coprimeNumber, input) != 1);
 		return coprimeNumber;
 	}
 	
 	/**
 	 * Find the inverse of input in field m
 	 * @param input A given integer
 	 * @param m A given integer, which will be (p - 1) * (q - 1)
 	 * @return The inverse of input.
 	 */
 	public static long modInverse(long input, long m) {
 		int i = 1;
 		// (d * e) = 1 (mod ((p - 1)(q - 1)))
 		// input * result = K * m + 1
 		while ((i * m + 1) % input != 0) {
 			i++;
 		}
 		return (i * m + 1)/input;
 	}
 
 	/**
 	 * Generate a private key-public key pair
 	 * @return generated key pair
 	 */
 	public static RSAKeyPair genKeyPair() {
 		long N = 0;
 		long e = 0;
 		long d = 0;
 		
 		// Generate two different random numbers within primeThreshold
 		// Those two numbers are to be used as index of prime number
 		int randIndex1 = Global.rand.nextInt(Global.primeThreshold) + 1;
 		int randIndex2 = 0;
 		do {
 			randIndex2 = Global.rand.nextInt(Global.primeThreshold) + 1;
 		}
 		while (randIndex2 == randIndex1);
 		// Get a prime number pair for RSA
 		long p = primeNumber(randIndex1);
 		long q = primeNumber(randIndex2);	
 		N = p * q;
 		long m = (p - 1) * (q - 1);		
 		// Randomly choose e, which is coprime to m
 		e = coprime(m);
 		d = modInverse(e, m);
 		// Generate new key pair
 		RSAKeyPair newKeyPair = new RSAKeyPair(new PublicKey(N, e), new PrivateKey(N, d));
 		return newKeyPair;
 		
 	}
 	
 	// Crack a private key by inputting its public key
 	public static PrivateKey bruteForce(PublicKey publicKey) {
 		
 		long N = publicKey.N;
 		long e = publicKey.e;
 		PrivateKey privateKey = null;
 		Encryptor encryptor = new Encryptor(publicKey);
 		Decryptor decryptor;
 		
 		int iteration = 1;
 		
 		while(iteration < N) {
 			iteration++;
 			long prime = primeNumber(iteration);
 			if(N % prime == 0) {
 				long p = prime;
 				long q = N / prime;
 				long m = (p - 1) * (q - 1);
 				int i = 1;
 				while ((i * m + 1) % e != 0) {
 					i++;
 				}
 				long d = (i * m + 1) / e;
                 System.out.println("p is found out to be: " + p);
                 System.out.println("q is found out to be: " + q);
                 System.out.println("d is found out to be: " + d);
 				privateKey = new PrivateKey(N, d);
 				decryptor = new Decryptor(privateKey);
 				if("Hello!".equals(decryptor.decrypt(encryptor.encrypt("Hello!")))){
 					break;
 				}
 			}
 		}
 		return privateKey;
 	}
 	
 	/**
 	 * A wrapper for RSA class, whose instance provide encrypt method
 	 */
 	public static class Encryptor {
 		private PublicKey publicKey = null;
 		// Constructor
 		Encryptor(PublicKey publicKey) {
 			this.publicKey = publicKey;
 		}
 		
 		/**
 		 * Encrypt message to send in byte with RSA
 		 * @param plainText
 		 * @return Encrypted message in byte array.
 		 */
 		public byte[] encrypt(String plaintext) {
 			long[] ciphertextLong = RSA.encrpyt(publicKey, plaintext);
 			byte[] ciphertext = new byte[ciphertextLong.length * 8];
 			for (int i = 0; i < ciphertextLong.length; i++) {
 				// Store leftmost 8 bytes.
 				ciphertext[8 * i] = (byte)(ciphertextLong[i] >> 56);
 				ciphertext[8 * i + 1] = (byte)(ciphertextLong[i] >> 48);
 				ciphertext[8 * i + 2] = (byte)(ciphertextLong[i] >> 40);
 				ciphertext[8 * i + 3] = (byte)(ciphertextLong[i] >> 32);
 				ciphertext[8 * i + 4] = (byte)(ciphertextLong[i] >> 24);
 				ciphertext[8 * i + 5] = (byte)(ciphertextLong[i] >> 16);
 				ciphertext[8 * i + 6] = (byte)(ciphertextLong[i] >> 8);
 				ciphertext[8 * i + 7] = (byte)(ciphertextLong[i]);
 			}
 			return ciphertext;
 		}
 		
 		/**
 		 * Encrypt char plaintext to long ciphertext.
 		 * @param plaintext Plaintext to be encrypted.
 		 * @return Long ciphertext
 		 */
 		public long encrypt(char plaintext) {
 			return RSA.encryptChar(publicKey, plaintext);
 		}
 	}
 	
 	/**
 	 * A wrapper for RSA class, whose instance provide decrypt method
 	 *
 	 */
 	public static class Decryptor {
 		private PrivateKey privateKey = null;
 		Decryptor(PrivateKey privateKey) {
 			this.privateKey = privateKey;
 		}
 
 		/**
 		 * Transferred message is in byte, decrypt it in byte with RSA
 		 * @param ciphertext
 		 * @return string decrypted plaintext.
 		 */
 		public String decrypt(byte[] ciphertext) {
 			long[] ciphertextLong = new long[ciphertext.length / 8];
 			for (int i = 0; i < ciphertextLong.length; i++) {
 				// A byte has only 8 bits
 				ciphertextLong[i] = (ciphertext[i * 8] & 0x00000000000000ffl) << 56 |
 						            (ciphertext[i * 8 + 1] & 0x00000000000000ffl) << 48|
 						            (ciphertext[i * 8 + 2] & 0x00000000000000ffl) << 40 |
 						            (ciphertext[i * 8 + 3] & 0x00000000000000ffl) << 32 |
 						            (ciphertext[i * 8 + 4] & 0x00000000000000ffl) << 24 |
 						            (ciphertext[i * 8 + 5] & 0x00000000000000ffl) << 16 |
 						            (ciphertext[i * 8 + 6] & 0x00000000000000ffl) << 8 |
 						            (ciphertext[i * 8 + 7] & 0x00000000000000ffl);
 			}
 			return RSA.decrypt(privateKey, ciphertextLong);
 		}
 				
 		/**
 		 * Decrypt long array of ciphertext to string plaintext. 
 		 * @param ciphertext Ciphertext to be decrypt.
 		 * @return Decrypted string plaintext.
 		 */
 		public String decrypt(long[] ciphertext) {
 			return RSA.decrypt(privateKey, ciphertext);
 		}
 		
 		/**
 		 * @param ciphertext Long int message ciphertext.
 		 * @return Decrypted plaintext char.
 		 */
 		public char decrypt(long ciphertext) {
 			return RSA.decrypt(privateKey, ciphertext);
 		}
 	}
 }
