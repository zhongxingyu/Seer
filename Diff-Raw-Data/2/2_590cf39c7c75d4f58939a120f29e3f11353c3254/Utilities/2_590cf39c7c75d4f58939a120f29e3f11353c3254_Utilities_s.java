 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.utils;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This class provides a variety of basic utility methods that are not
  * dependent on any other classes within the org.jamwiki package structure.
  */
 public class Utilities {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(Utilities.class.getName());
 
 	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
 	private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";
 	private static final Pattern VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
 	private static final Pattern VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
 
 	/**
 	 *
 	 */
 	private Utilities() {
 	}
 
 	/**
 	 * Convert a string value from one encoding to another.
 	 *
 	 * @param text The string that is to be converted.
 	 * @param fromEncoding The encoding that the string is currently encoded in.
 	 * @param toEncoding The encoding that the string is to be encoded to.
 	 * @return The encoded string.
 	 */
 	public static String convertEncoding(String text, String fromEncoding, String toEncoding) {
 		if (StringUtils.isBlank(text)) {
 			return text;
 		}
 		if (StringUtils.isBlank(fromEncoding)) {
 			logger.warn("No character encoding specified to convert from, using UTF-8");
 			fromEncoding = "UTF-8";
 		}
 		if (StringUtils.isBlank(toEncoding)) {
 			logger.warn("No character encoding specified to convert to, using UTF-8");
 			toEncoding = "UTF-8";
 		}
 		try {
 			text = new String(text.getBytes(fromEncoding), toEncoding);
 		} catch (UnsupportedEncodingException e) {
 			// bad encoding
 			logger.warn("Unable to convert value " + text + " from " + fromEncoding + " to " + toEncoding, e);
 		}
 		return text;
 	}
 
 	/**
 	 * Decode a value that has been retrieved from a servlet request.  This
 	 * method will replace any underscores with spaces.
 	 *
 	 * @param url The encoded value that is to be decoded.
 	 * @param decodeUnderlines Set to <code>true</code> if underlines should
 	 *  be automatically converted to spaces.
 	 * @return A decoded value.
 	 */
 	public static String decodeTopicName(String url, boolean decodeUnderlines) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		return (decodeUnderlines) ? StringUtils.replace(url, "_", " ") : url;
 	}
 
 	/**
 	 * Decode a value that has been retrieved directly from a URL or file
 	 * name.  This method will URL decode the value and then replace any
 	 * underscores with spaces.  Note that this method SHOULD NOT be called
 	 * for values retrieved using request.getParameter(), but only values
 	 * taken directly from a URL.
 	 *
 	 * @param url The encoded value that is to be decoded.
 	 * @param decodeUnderlines Set to <code>true</code> if underlines should
 	 *  be automatically converted to spaces.
 	 * @return A decoded value.
 	 */
 	public static String decodeAndEscapeTopicName(String url, boolean decodeUnderlines) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		String result = url;
 		try {
 			result = URLDecoder.decode(result, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// this should never happen
 			throw new IllegalStateException("Unsupporting encoding UTF-8");
 		}
 		return Utilities.decodeTopicName(result, decodeUnderlines);
 	}
 
 	/**
 	 * Convert a delimited string to a list.
 	 *
 	 * @param delimitedString A string consisting of the delimited list items.
 	 * @param delimiter The string used as the delimiter.
 	 * @return A list consisting of the delimited string items, or <code>null</code> if the
 	 *  string is <code>null</code> or empty.
 	 */
 	public static List<String> delimitedStringToList(String delimitedString, String delimiter) {
 		if (delimiter == null) {
 			throw new IllegalArgumentException("Attempt to call Utilities.delimitedStringToList with no delimiter specified");
 		}
 		if (StringUtils.isBlank(delimitedString)) {
 			return null;
 		}
 		return Arrays.asList(StringUtils.splitByWholeSeparator(delimitedString, delimiter));
 	}
 
 	/**
 	 * Encode a value for use a topic name.  This method will replace any
 	 * spaces with underscores.
 	 *
 	 * @param url The decoded value that is to be encoded.
 	 * @return An encoded value.
 	 */
 	public static String encodeTopicName(String url) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		return StringUtils.replace(url, " ", "_");
 	}
 
 	/**
 	 * Encode a topic name for use in a URL.  This method will replace spaces
 	 * with underscores and URL encode the value, but it will not URL encode
 	 * colons.
 	 *
 	 * @param url The topic name to be encoded for use in a URL.
 	 * @return The encoded topic name value.
 	 */
 	public static String encodeAndEscapeTopicName(String url) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		String result = Utilities.encodeTopicName(url);
 		try {
 			result = URLEncoder.encode(result, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// this should never happen
 			throw new IllegalStateException("Unsupporting encoding UTF-8");
 		}
 		// un-encode colons
 		result = StringUtils.replace(result, "%3A", ":");
 		// un-encode forward slashes
 		result = StringUtils.replace(result, "%2F", "/");
 		return result;
 	}
 
 	/**
 	 * Search through content, starting at a specific position, and search for the
 	 * first position after a matching end tag for a specified start tag.  For instance,
 	 * if called with a start tag of "<b>" and an end tag of "</b>", this method
 	 * will operate as follows:
 	 *
 	 * "01<b>567</b>23" returns 12.
 	 * "01<b>56<b>01</b>67</b>23" returns 22.
 	 *
 	 * @param content The string to be searched.
 	 * @param start The position within the string to start searching from.
 	 * @param startToken The opening tag to match.
 	 * @param endToken The closing tag to match.
 	 * @return -1 if no matching end tag is found, or the index within the string of the first
 	 *  character immediately following the end tag.
 	 */
 	public static int findMatchingEndTag(CharSequence content, int start, String startToken, String endToken) {
 		return Utilities.findMatchingTag(content.toString(), start, startToken, endToken, false);
 	}
 
 	/**
 	 * Search through content, starting at a specific position, and search backwards for the
 	 * first position before a matching start tag for a specified end tag.  For instance,
 	 * if called with an end tag of "</b>" and a start tag of "<b>", this method
 	 * will operate as follows:
 	 *
 	 * "01<b>567</b>23" returns 1.
 	 * "01234567</b>23" returns -1.
 	 *
 	 * @param content The string to be searched.
 	 * @param start The position within the string to start searching from.
 	 * @param startToken The opening tag to match.
 	 * @param endToken The closing tag to match.
 	 * @return -1 if no matching start tag is found, or the index within the string of the first
 	 *  character immediately preceding the start tag.
 	 */
 	public static int findMatchingStartTag(CharSequence content, int start, String startToken, String endToken) {
 		return Utilities.findMatchingTag(content.toString(), start, startToken, endToken, true);
 	}
 
 	/**
 	 * Find a matching start/end tag.
 	 */
 	private static int findMatchingTag(String content, int start, String startToken, String endToken, boolean reverse) {
 		if (StringUtils.isBlank(content) || start >= content.length()) {
 			return -1;
 		}
 		int pos = start;
 		int count = 0;
 		String substring = null;
 		boolean atLeastOneMatch = false;
 		while (pos >= 0 && pos < content.length()) {
 			substring = (reverse) ? content.substring(0, pos + 1) : content.substring(pos);
 			if (!reverse && substring.startsWith(startToken)) {
 				count++;
 				atLeastOneMatch = true;
 				pos += startToken.length();
 			} else if (!reverse && substring.startsWith(endToken)) {
 				count--;
 				pos += endToken.length();
 			} else if (reverse && substring.endsWith(endToken)) {
 				count++;
 				atLeastOneMatch = true;
 				pos -= endToken.length();
 			} else if (reverse && substring.endsWith(startToken)) {
 				count--;
 				pos -= startToken.length();
 			} else {
 				pos = (reverse) ? (pos - 1) : (pos + 1);
 			}
 			if (atLeastOneMatch && count == 0) {
 				return pos;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * This method is a wrapper for Class.forName that will attempt to load a
 	 * class from both the current thread context class loader and the default
 	 * class loader.
 	 *
 	 * @param className The full class name that is to be initialized with the
 	 *  <code>Class.forName</code> call.
 	 * @throws ClassNotFoundException Thrown if the class cannot be initialized
 	 *  from any class loader.
 	 */
 	public static void forName(String className) throws ClassNotFoundException {
 		try {
 			// first try using the current thread's class loader
 			Class.forName(className, true, Thread.currentThread().getContextClassLoader());
 			return;
 		} catch (ClassNotFoundException e) {
 			logger.info("Unable to load class " + className + " using the thread class loader, now trying the default class loader");
 		}
 		Class.forName(className);
 	}
 
 	/**
 	 * Given a message key and locale return a locale-specific message.
 	 *
 	 * @param key The message key that corresponds to the formatted message
 	 *  being retrieved.
 	 * @param locale The locale for the message that is to be retrieved.
 	 * @return A formatted message string that is specific to the locale.
 	 */
 	public static String formatMessage(String key, Locale locale) {
 		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
 		return messages.getString(key);
 	}
 
 	/**
 	 * Given a message key, locale, and formatting parameters, return a
 	 * locale-specific message.
 	 *
 	 * @param key The message key that corresponds to the formatted message
 	 *  being retrieved.
 	 * @param locale The locale for the message that is to be retrieved.
 	 * @param params An array of formatting parameters to use in the message
 	 *  being returned.
 	 * @return A formatted message string that is specific to the locale.
 	 */
 	public static String formatMessage(String key, Locale locale, Object[] params) {
 		MessageFormat formatter = new MessageFormat("");
 		formatter.setLocale(locale);
 		String message = Utilities.formatMessage(key, locale);
 		formatter.applyPattern(message);
 		return formatter.format(params);
 	}
 
 	/**
 	 * Return the current ClassLoader.  First try to get the current thread's
 	 * ClassLoader, and if that fails return the ClassLoader that loaded this
 	 * class instance.
 	 *
 	 * @return An instance of the current ClassLoader.
 	 */
 	private static ClassLoader getClassLoader() {
 		ClassLoader loader = null;
 		try {
 			loader = Thread.currentThread().getContextClassLoader();
 		} catch (SecurityException e) {
 			logger.debug("Unable to retrieve thread class loader, trying default");
 		}
 		if (loader == null) {
 			loader = Utilities.class.getClassLoader();
 		}
 		return loader;
 	}
 
 	/**
 	 * Given a file name for a file that is located somewhere in the application
 	 * classpath, return a File object representing the file.
 	 *
 	 * @param filename The name of the file (relative to the classpath) that is
 	 *  to be retrieved.
 	 * @return A file object representing the requested filename.  Note that the
 	 *  file name is not guaranteed to match the filename passed to this method
 	 *  since (for example) the file might be found in a JAR file and thus will
 	 *  need to be copied to a temporary location for reading.
 	 * @throws FileNotFoundException Thrown if the classloader can not be found or if
 	 *  the file can not be found in the classpath.
 	 */
 	public static File getClassLoaderFile(String filename) throws FileNotFoundException {
 		// note that this method is used when initializing logging, so it must
 		// not attempt to log anything.
 		File file = null;
 		ClassLoader loader = Utilities.getClassLoader();
 		// Windows machines will have "\" in the path, convert to "/"
 		filename = filename.replace('\\', '/');
 		URL url = loader.getResource(filename);
 		if (url == null) {
 			url = ClassLoader.getSystemResource(filename);
 		}
 		if (url == null) {
 			throw new FileNotFoundException("Unable to find " + filename);
 		}
 		file = FileUtils.toFile(url);
 		if (file == null || !file.exists()) {
 			InputStream is = null;
 			FileOutputStream os = null;
 			try {
 				// url exists but file cannot be read, so perhaps it's not a "file:" url (an example
 				// would be a "jar:" url).  as a workaround, copy the file to a temp file and return
 				// the temp file.
 				String tempFilename = RandomStringUtils.randomAlphanumeric(20);
 				file = File.createTempFile(tempFilename, null);
 				is = loader.getResourceAsStream(filename);
 				os = new FileOutputStream(file);
 				IOUtils.copy(is, os);
 			} catch (IOException e) {
 				throw new FileNotFoundException("Unable to load file with URL " + url);
 			} finally {
 				IOUtils.closeQuietly(is);
 				IOUtils.closeQuietly(os);
 			}
 		}
 		return file;
 	}
 
 	/**
 	 * Attempt to get the class loader root directory.  This method works
 	 * by searching for a file that MUST exist in the class loader root
 	 * and then returning its parent directory.
 	 *
 	 * @return Returns a file indicating the directory of the class loader.
 	 * @throws FileNotFoundException Thrown if the class loader can not be found,
 	 *  which may occur if this class is deployed without the jamwiki-war package.
 	 */
 	public static File getClassLoaderRoot() throws FileNotFoundException {
 		// The file hard-coded here MUST exist in the class loader directory.
 		File file = Utilities.getClassLoaderFile("sql/sql.ansi.properties");
 		if (!file.exists()) {
 			throw new FileNotFoundException("Unable to find class loader root");
 		}
		return file.getParentFile();
 	}
 
 	/**
 	 * Utility method for retrieving a key from a map, case-insensitive.  This method
 	 * first tries to return an exact match, and if that fails it returns the first
 	 * case-insensitive match.
 	 *
 	 * @param map The map being examined.
 	 * @param key The key for the value being retrieved.
 	 * @return A matching value for the key, or <code>null</code> if no match is found.
 	 */
 	public static <V> V getMapValueCaseInsensitive(Map<String, V> map, String key) {
 		if (map == null) {
 			return null;
 		}
 		if (map.containsKey(key)) {
 			return map.get(key);
 		}
 		for (String mapKey : map.keySet()) {
 			if (StringUtils.equalsIgnoreCase(mapKey, key)) {
 				return map.get(mapKey);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Given a request, determine the server URL.
 	 *
 	 * @return A Server URL of the form http://www.example.com/
 	 */
 	public static String getServerUrl(HttpServletRequest request) {
 		return request.getScheme() + "://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
 	}
 
 	/**
 	 * Given a String representation of a class name (for example, org.jamwiki.db.AnsiDataHandler)
 	 * return an instance of the class.  The constructor for the class being instantiated must
 	 * not take any arguments.
 	 *
 	 * @param className The name of the class being instantiated.
 	 * @return A Java Object representing an instance of the specified class.
 	 */
 	public static Object instantiateClass(String className) {
 		if (StringUtils.isBlank(className)) {
 			throw new IllegalArgumentException("Cannot call instantiateClass with an empty class name");
 		}
 		logger.debug("Instantiating class: " + className);
 		try {
 			Class clazz = ClassUtils.getClass(className);
 			Class[] parameterTypes = new Class[0];
 			Constructor constructor = clazz.getConstructor(parameterTypes);
 			Object[] initArgs = new Object[0];
 			return constructor.newInstance(initArgs);
 		} catch (ClassNotFoundException e) {
 			throw new IllegalStateException("Invalid class name specified: " + className, e);
 		} catch (NoSuchMethodException e) {
 			throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
 		} catch (InvocationTargetException e) {
 			throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
 		} catch (InstantiationException e) {
 			throw new IllegalStateException("Specified class could not be instantiated: " + className, e);
 		}
 	}
 
 	/**
 	 * Utility method for determining common elements in two Map objects.
 	 */
 	public static <K, V> Map<K, V> intersect(Map<K, V> map1, Map<K, V> map2) {
 		if (map1 == null || map2 == null) {
 			throw new IllegalArgumentException("Utilities.intersection() requires non-null arguments");
 		}
 		Map<K, V> result = new HashMap<K, V>();
 		for (K key : map1.keySet()) {
 			if (ObjectUtils.equals(map1.get(key), map2.get(key))) {
 				result.put(key, map1.get(key));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Given a string, determine if it is a valid HTML entity (such as &trade; or
 	 * &#160;).
 	 *
 	 * @param text The text that is being examined.
 	 * @return <code>true</code> if the text is a valid HTML entity.
 	 */
 	public static boolean isHtmlEntity(String text) {
 		if (text == null) {
 			return false;
 		}
 		// see if it was successfully converted, in which case it is an entity
 		return (!text.equals(StringEscapeUtils.unescapeHtml(text)));
 	}
 
 	/**
 	 * Determine if the given string is a valid IPv4 or IPv6 address.  This method
 	 * uses pattern matching to see if the given string could be a valid IP address.
 	 *
 	 * @param ipAddress A string that is to be examined to verify whether or not
 	 *  it could be a valid IP address.
 	 * @return <code>true</code> if the string is a value that is a valid IP address,
 	 *  <code>false</code> otherwise.
 	 */
 	public static boolean isIpAddress(String ipAddress) {
 		if (StringUtils.isBlank(ipAddress)) {
 			return false;
 		}
 		Matcher m1 = Utilities.VALID_IPV4_PATTERN.matcher(ipAddress);
 		if (m1.matches()) {
 			return true;
 		}
 		Matcher m2 = Utilities.VALID_IPV6_PATTERN.matcher(ipAddress);
 		return m2.matches();
 	}
 
 	/**
 	 * Convert a list to a delimited string.
 	 *
 	 * @param list The list to convert to a string.
 	 * @param delimiter The string to use as a delimiter.
 	 * @return A string consisting of the delimited list items, or <code>null</code> if the
 	 *  list is <code>null</code> or empty.
 	 */
 	public static String listToDelimitedString(List<String> list, String delimiter) {
 		if (delimiter == null) {
 			throw new IllegalArgumentException("Attempt to call Utilities.delimitedStringToList with no delimiter specified");
 		}
 		if (list == null || list.isEmpty()) {
 			return null;
 		}
 		String result = "";
 		for (String item : list) {
 			if (result.length() > 0) {
 				result += delimiter;
 			}
 			result += item;
 		}
 		return result;
 	}
 
 	/**
 	 * Utility method for reading a file from a classpath directory and returning
 	 * its contents as a String.
 	 *
 	 * @param filename The name of the file to be read, either as an absolute file
 	 *  path or relative to the classpath.
 	 * @return A string representation of the file contents.
 	 * @throws FileNotFoundException Thrown if the file cannot be found or if an I/O exception
 	 *  occurs.
 	 */
 	public static String readFile(String filename) throws IOException {
 		File file = new File(filename);
 		if (file.exists()) {
 			// file passed in as full path
 			return FileUtils.readFileToString(file, "UTF-8");
 		}
 		// look for file in resource directories
 		file = getClassLoaderFile(filename);
 		return FileUtils.readFileToString(file, "UTF-8");
 	}
 
 	/**
 	 * Strip all HTML tags from a string.  For example, "A <b>bold</b> word" will be
 	 * returned as "A bold word".  This method treats an tags that are between brackets
 	 * as HTML, whether it is valid HTML or not.
 	 *
 	 * @param value The value that will have HTML stripped from it.
 	 * @return The value submitted to this method with all HTML tags removed from it.
 	 */
 	public static String stripMarkup(String value) {
 		return StringUtils.trim(value.replaceAll("<[^>]+>", ""));
 	}
 }
