 package my.websecurity.util;
 
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.DESKeySpec;
 
 /**
  * <p>
  * 加密工具类<br/>
  * 默认使用UTF-8编码<br/>
  * hex string 为小写字母
  * </p>
  * <p>
  * 可逆算法
  * <ul>
  * <li>DES：(可逆):64字节 = 16位16进制</li>
  * </ul>
  * 摘要算法
  * <ul>
  * <li>MD5:128字节 = 32位16进制</li>
  * <li>SHA-1:160字节 = 40位16进制</li>
  * <li>SHA-256:256字节 = 64位16进制</li>
  * <li>SHA-384:384字节 = 96位16进制</li>
  * <li>SHA-512:512字节 = 128位16进制</li>
  * </ul>
  * </p>
  * 
  * @author xiegang
  * @since 2011-11-06
  *
  */
 public class EncryptUtils {
 	/**
 	 * 哈希元数据
 	 */
 	private final static char[] HEXDIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 	
 	/**
 	 * 默认编码
 	 */
 	private final static String DEFAULT_CHARSET = "UTF-8";
 	
 	/** 加密算法DES */
 	public final static String ALGORITHM_DES = "DES";
 	
 	/** 摘要算法MD5 */
 	public final static String ALGORITHM_MD5 = "MD5";
 	/** 摘要算法SHA-1 */
 	public final static String ALGORITHM_SHA1 = "SHA-1";
 	/** 摘要算法SHA-256 */
 	public final static String ALGORITHM_SHA256 = "SHA-256";
 	/** 摘要算法SHA-384 */
 	public final static String ALGORITHM_SHA384 = "SHA-384";
 	/** 摘要算法SHA-512 */
 	public final static String ALGORITHM_SHA512 = "SHA-512";
 	
 	/**
 	 * 转换字节数组为16进制字串(HEX)
 	 * 
 	 * @param b 字节数组
 	 * @return 16进制字串
 	 */
 	public static String byte2hex(byte[] b) {
 		StringBuffer hexStr = new StringBuffer();
 		for (int i = 0; i < b.length; i++) {
 			int n = b[i];
 			if (n < 0) {
 				n = 256 + n;
 			}
 			int d1 = n / 16;
 			int d2 = n % 16;
 			hexStr.append(HEXDIGITS[d1]).append(HEXDIGITS[d2]);
 		}
 		return hexStr.toString();
 	}
 	
 	/**
 	 * 转换16进制字串(HEX)为字节数组
 	 * 
 	 * @param str 16进制字串
 	 * @return 字节数组
 	 */
 	public static byte[] hex2byte(String str) {
 		byte[] strBytes = str.getBytes();
 		int strlen = strBytes.length;
 		byte[] out = new byte[strlen / 2];
 		
 		for(int i= 0; i < strlen; i += 2) {
 			out[i/2] = (byte)Integer.parseInt(new String(strBytes, i, 2), 16);
 		}
 		return out;
 	}
 	
 	/**
 	 * MD5加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static byte[] MD5Encode(byte[] origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_MD5);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 使用默认字符集对字符串进行MD5加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static String MD5Encode(String origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_MD5);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 对字符串进行MD5加密
 	 * 
 	 * @param origin
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String MD5Encode(String origin, String charset) throws UnsupportedEncodingException {
 		try {
 			return messageDigestDncode(origin, charset, ALGORITHM_MD5);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * SHA1加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static byte[] SHA1Encode(byte[] origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA1);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 使用默认字符集对字符串进行SHA-1加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static String SHA1Encode(String origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA1);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 对字符串进行SHA-1加密
 	 * 
 	 * @param origin
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String SHA1Encode(String origin, String charset) throws UnsupportedEncodingException {
 		try {
 			return messageDigestDncode(origin, charset, ALGORITHM_SHA1);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * SHA256加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static byte[] SHA256Encode(byte[] origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA256);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 使用默认字符集对字符串进行SHA-256加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static String SHA256Encode(String origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA256);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 对字符串进行SHA-256加密
 	 * 
 	 * @param origin
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String SHA256Encode(String origin, String charset) throws UnsupportedEncodingException {
 		try {
 			return messageDigestDncode(origin, charset, ALGORITHM_SHA256);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * SHA384加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static byte[] SHA384Encode(byte[] origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA384);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 使用默认字符集对字符串进行SHA-384加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static String SHA384Encode(String origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA384);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 对字符串进行SHA-384加密
 	 * 
 	 * @param origin
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String SHA384Encode(String origin, String charset) throws UnsupportedEncodingException {
 		try {
 			return messageDigestDncode(origin, charset, ALGORITHM_SHA384);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * SHA512加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static byte[] SHA512Encode(byte[] origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA512);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 使用默认字符集对字符串进行SHA-512加密
 	 * 
 	 * @param origin
 	 * @return
 	 */
 	public static String SHA512Encode(String origin) {
 		try {
 			return messageDigestDncode(origin, ALGORITHM_SHA512);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 对字符串进行SHA-512加密
 	 * 
 	 * @param origin
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String SHA512Encode(String origin, String charset) throws UnsupportedEncodingException {
 		try {
 			return messageDigestDncode(origin, charset, ALGORITHM_SHA512);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Java MessageDigest Encode
 	 * 
 	 * @param origin
 	 * @param algorithm 加密算法
 	 * @return
 	 * @throws NoSuchAlgorithmException 不存在的加密算法
 	 */
 	public static byte[] messageDigestDncode(byte[] origin, String algorithm) throws NoSuchAlgorithmException {
 		MessageDigest sha1 = MessageDigest.getInstance(algorithm);
 		sha1.reset();
 		return sha1.digest(origin);
 	}
 	
 	/**
 	 * Java MessageDigest Encode
 	 * @param origin
 	 * @param charset 待加密字符串的编码
 	 * @param algorithm
 	 * @return
 	 * @throws NoSuchAlgorithmException 不存在的加密算法
 	 * @throws UnsupportedEncodingException 不存在的编码类型
 	 */
 	public static String messageDigestDncode(String origin, String charset, String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		return byte2hex(messageDigestDncode(origin.getBytes(charset), algorithm));
 	}
 	
 	/**
 	 * Java MessageDigest Encode<br/>
 	 * 使用默认编码UTF-8
 	 * @param origin
 	 * @param algorithm
 	 * @return
 	 * @throws NoSuchAlgorithmException 不存在的加密算法
 	 */
 	public static String messageDigestDncode(String origin, String algorithm) throws NoSuchAlgorithmException {
 		String result = origin;
 		try {
 			result =  byte2hex(messageDigestDncode(origin.getBytes(DEFAULT_CHARSET), algorithm));
 		} catch (UnsupportedEncodingException e) {
 			// will not happen
 		}
 		return result;
 	}
 	
 	/**
 	 * DES加密<br/>
 	 * 
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @return
 	 * @throws Exception 
 	 */
 	public static byte[] DESEncrypt(byte[] origin, byte[] key) throws Exception {
 		Key keySpec = DESKey(key);
 		return cipherEncrypt(origin, keySpec, ALGORITHM_DES);
 	}
 	
 	/**
 	 * DES加密<br/>
 	 * 使用默认编码
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @return
 	 * @throws Exception 
 	 */
 	public static String DESEncrypt(String origin, String key) throws Exception {
 		byte[] result = DESEncrypt(origin.getBytes(DEFAULT_CHARSET), key.getBytes(DEFAULT_CHARSET));
 		return byte2hex(result);
 	}
 	
 	/**
 	 * DES加密<br/>
 	 * 
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @param charset 编码
 	 * @return
 	 * @throws UnsupportedEncodingException
 	 * @throws Exception 
 	 */
 	public static String DESEncrypt(String origin, String key, String charset) throws UnsupportedEncodingException, Exception {
 		byte[] result = DESEncrypt(origin.getBytes(charset), key.getBytes(charset));
 		return byte2hex(result);
 	}
 	
 	/**
 	 * DES解密<br/>
 	 * 
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @return
 	 * @throws Exception 
 	 */
 	public static byte[] DESDecrypt(byte[] origin, byte[] key) throws Exception {
 		Key keySpec = DESKey(key);
 		return cipherDecrypt(origin, keySpec, ALGORITHM_DES);
 	}
 	
 	/**
 	 * DES解密<br/>
 	 * 使用默认编码
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @return
 	 * @throws Exception 
 	 */
 	public static String DESDecrypt(String origin, String key) throws Exception {
 		byte[] result = DESDecrypt(hex2byte(origin), key.getBytes(DEFAULT_CHARSET));
 		return new String(result);
 	}
 	
 	/**
 	 * DES解密<br/>
 	 * 
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @param charset
 	 * @return
 	 * @throws UnsupportedEncodingException
 	 * @throws Exception 
 	 */
 	public static String DESDecrypt(String origin, String key, String charset) throws UnsupportedEncodingException, Exception {
 		byte[] result = DESDecrypt(hex2byte(origin), key.getBytes(charset));
 		return new String(result);
 	}
 	
 	/**
 	 * Java cipher 加密<br/>
 	 *  
 	 * @param origin 明文
 	 * @param key 密钥
 	 * @param algorithm 算法
 	 * @return 密文
 	 * @throws Exception 
 	 */
 	public static byte[] cipherEncrypt(byte[] origin, Key key, String algorithm) throws Exception {
 		Cipher cipher = Cipher.getInstance(algorithm);
 		cipher.init(Cipher.ENCRYPT_MODE, key);
 		return cipher.doFinal(origin);
 	}
 	
 	/**
 	 * Java cipher解密<br/>
 	 * 
 	 * @param origin 密文
 	 * @param key 密钥
 	 * @param algorithm 算法
 	 * @return 明文
 	 * @throws Exception 
 	 */
 	public static byte[] cipherDecrypt(byte[] origin, Key key, String algorithm) throws Exception {
 		Cipher cipher = Cipher.getInstance(algorithm);
 		cipher.init(Cipher.DECRYPT_MODE, key);
 		return cipher.doFinal(origin);
 
 	}
 	
 	/**  
 	 * 根据传入的字符串的key生成DES key对象  
 	 *   
 	 * @param key  
 	 * @throws InvalidKeyException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws InvalidKeySpecException 
 	 */
 	private static Key DESKey(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
 		KeySpec keySpec = new DESKeySpec(key);
 		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
 		return keyFactory.generateSecret(keySpec);
 	}
 }
