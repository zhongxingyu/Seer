 package com.alexrnl.commons.utils;
 
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.error.TopLevelError;
 
 
 /**
  * Utility methods for {@link String}.<br />
  * @author Alex
  */
 public final class StringUtils {
 	/** Logger */
 	private static Logger				lg					= Logger.getLogger(StringUtils.class.getName());
 	
 	/** The name of the MD5 algorithm */
 	private static final String			MD5_ALGORITHM_NAME	= "MD5";
 	/** The MD5 message digest algorithm */
 	private static final MessageDigest	MD5_MESSAGE_DIGEST;
 	/**
 	 * Load the MD5 algorithm
 	 */
 	static {
 		try {
 			MD5_MESSAGE_DIGEST = MessageDigest.getInstance(MD5_ALGORITHM_NAME);
 		} catch (final NoSuchAlgorithmException e) {
 			lg.severe("Error while computing MD5, no MD5 algorithm found: "
 					+ ExceptionUtils.display(e));
 			throw new MD5Error(e);
 		}
 	}
 
 	/**
 	 * Constructor #1.<br />
 	 * Default private constructor.
 	 */
 	private StringUtils () {
 		super();
 	}
 	
 	/**
 	 * Check if the string specified is <code>null</code> or empty.
 	 * @param s
 	 *        the string to check.
 	 * @return <code>true</code> if the string is <code>null</code> or empty.
 	 */
 	public static boolean nullOrEmpty (final String s) {
 		return s == null || s.isEmpty();
 	}
 	
 	/**
 	 * Check if the string specified is neither <code>null</code> nor empty.
 	 * @param s
 	 *        the string to check.
 	 * @return <code>true</code> if the string is neither <code>null</code> nor empty.
 	 */
 	public static boolean neitherNullNorEmpty (final String s) {
 		return s != null && !s.isEmpty();
 	}
 	
 	/**
 	 * Return the string specified, or its replacement if it is <code>null</code>.
 	 * @param s
 	 *        the string to return.
 	 * @param replace
 	 *        its replacement, is the first string is <code>null</code>.
 	 * @return <code>s</code>, or <code>replace</code> if <code>s</code> is <code>null</code>.
 	 */
 	public static String replaceIfNull (final String s, final String replace) {
 		return s == null ? replace : s;
 	}
 	
 	/**
 	 * Return the string specified, or an empty {@link String}, if it is <code>null</code>.
 	 * @param s
 	 *        the string to return.
 	 * @return an empty String if the parameter is <code>null</code>, else the string.
 	 */
 	public static String emptyIfNull (final String s) {
 		return replaceIfNull(s, "");
 	}
 	
 	/**
 	 * Separate a list of object with a given separator.<br />
 	 * @param separator
 	 *        the separator.
 	 * @param s
 	 *        the objects to display.
 	 * @return the objects separated by the separator specified.
 	 */
 	public static String separateWith (final String separator, final Object... s) {
 		final StringBuilder builder = new StringBuilder();
 		
 		for (final Object object : s) {
 			builder.append(object).append(separator);
 		}
 		
 		return builder.substring(0, builder.length() - separator.length()).toString();
 	}
 	
 	/**
 	 * Computes the MD5 of a string.<br />
 	 * Uses the JVM's default charset.
 	 * @param text
 	 *        the text to hash.
 	 * @return the MD5 hash of the text.
 	 */
 	public static String getMD5 (final String text) {
 		return getMD5(text, Charset.defaultCharset());
 	}
 	
 	/**
 	 * Computes the MD5 of a string.
 	 * @param text
 	 *        the text to hash.
 	 * @param charset the charset.
 	 * @return the hash of the text.
 	 */
 	public static String getMD5 (final String text, final Charset charset) {
 		final StringBuilder buffer = new StringBuilder();
 		MD5_MESSAGE_DIGEST.reset();
 		MD5_MESSAGE_DIGEST.update(text.getBytes(charset));
 		
 		final byte[] digest = MD5_MESSAGE_DIGEST.digest();
 		for (final byte element : digest) {
 			int value = element;
 			if (value < 0) {
				value += Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
 			}
 			// Add a zero in case of 'short' hash.
			final String hex = Integer.toHexString(value);
			if (hex.length() == 1) {
				buffer.append(0);
 			}
			buffer.append(hex);
 		}
 		return buffer.toString();
     }
 	
 	/**
 	 * Error while processing MD5 hash.<br />
 	 * @author Alex
 	 */
	public static final class MD5Error extends TopLevelError {
 		/** The serial version UID */
 		private static final long	serialVersionUID	= 7373353736343009842L;
 
 		/**
 		 * Constructor #1.<br />
 		 * Default constructor.
 		 */
 		private MD5Error () {
 			super();
 		}
 		
 		/**
 		 * Constructor #2.<br />
 		 * @param message
 		 *        the message of the error.
 		 * @param cause
 		 *        the cause of the error.
 		 */
 		private MD5Error (final String message, final Throwable cause) {
 			super(message, cause);
 		}
 		
 		/**
 		 * Constructor #3.<br />
 		 * @param message
 		 *        the message of the error.
 		 */
 		private MD5Error (final String message) {
 			super(message);
 		}
 		
 		/**
 		 * Constructor #4.<br />
 		 * @param cause
 		 *        the cause of the error.
 		 */
 		private MD5Error (final Throwable cause) {
 			super(cause);
 		}
 
 	}
 }
