 package com.threewks.thundr.http;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.regex.Pattern;
 
 import com.threewks.thundr.exception.BaseException;
 
 /**
  * Encodes URL elements as needed.
  * 
  * see http://tools.ietf.org/html/rfc2396 for details.
  */
 public class URLEncoder {
 	private static final String unreserved = "a-zA-Z0-9";
 	private static final String mark = "_!~*'()\\.\\-";
 	/** These values are all technically permitted in the path segment, however the appengine does not like them. */
 	private static final String pathChars = "&:@=+$,";
 	private static final Pattern acceptableQueryCharacters = Pattern.compile("[" + unreserved + mark + "]*");
 	private static final Pattern acceptablePathCharacters = Pattern.compile("[" + unreserved + mark + "]*");
 
 	/**
 	 * Encodes the given value to be used as a URL/URI path component.
 	 */
 	public static final String encodePathComponent(String value) {
 		return escapeUsingPattern(acceptablePathCharacters, value);
 	}
 
 	/**
 	 * Encodes the given value to be used as a URL/URI path slug - that is it removes non-alpha characters and introduces dashes.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public static final String encodePathSlugComponent(String value) {
		return value == null ? value : value.replaceAll("'", "").replaceAll("\\W", "-").replaceAll("-+", "-");
 	}
 
 	/**
 	 * Encodes the given value to be used as a URL/URI query component.
 	 */
 	public static final String encodeQueryComponent(String value) {
 		return escapeUsingPattern(acceptableQueryCharacters, value);
 	}
 
 	public static final String decodePathComponent(String value) {
 		return unescape(value);
 	}
 
 	private static String unescape(String value) {
 		try {
 			return URLDecoder.decode(value, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new BaseException(e);
 		}
 	}
 
 	private static String escapeUsingPattern(Pattern pattern, String value) {
 		if (value == null || value.length() == 0 || pattern.matcher(value).matches()) {
 			return value;
 		}
 		StringBuilder output = new StringBuilder();
 		for (int i = 0; i < value.length(); i++) {
 			String character = value.substring(i, i + 1);
 
 			if (!pattern.matcher(character).matches()) {
 				output.append("%");
 				output.append(Integer.toHexString((int) character.charAt(0)).toUpperCase());
 			} else {
 				output.append(character);
 			}
 		}
 		return output.toString();
 	}
 }
