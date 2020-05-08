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
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.AbstractParser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.persistency.db.DatabaseConnection;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class Utilities {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(Utilities.class.getName());
 
 	/**
 	 *
 	 */
 	public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, int cookieAge) throws Exception {
 		Cookie cookie = null;
 		// after confirming credentials
 		cookie = new Cookie(cookieName, cookieValue);
 		cookie.setMaxAge(cookieAge);
 		response.addCookie(cookie);
 	}
 
 	/**
 	 * Returns true if the given collection of strings contains the given string where the case
 	 * of either is ignored
 	 * @param collection collection of {@link String}s
 	 * @param string string to find
 	 * @return true if the string is in the collection with no regard to case
 	 */
 	public static boolean containsStringIgnoreCase(Collection collection, String string) {
 		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
 			String s = (String) iterator.next();
 			if (s.equalsIgnoreCase(string)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	public static String convertEncoding(String text, String fromEncoding, String toEncoding) {
 		if (!StringUtils.hasText(text)) return text;
 		if (!StringUtils.hasText(fromEncoding)) {
 			logger.warning("No character encoding specified to convert from, using UTF-8");
 			fromEncoding = "UTF-8";
 		}
 		if (!StringUtils.hasText(toEncoding)) {
 			logger.warning("No character encoding specified to convert to, using UTF-8");
 			toEncoding = "UTF-8";
 		}
 		try {
 			text = new String(text.getBytes(fromEncoding), toEncoding);
 		} catch (Exception e) {
 			// bad encoding
 			logger.warning("Unable to convert value " + text + " from " + fromEncoding + " to " + toEncoding, e);
 		}
 		return text;
 	}
 
 	/**
 	 *
 	 */
 	public static WikiUser currentUser(HttpServletRequest request) {
 		// first look for user in the session
 		WikiUser user = (WikiUser)request.getSession().getAttribute(JAMWikiServlet.PARAMETER_USER);
 		if (user != null) return user;
 		// look for user cookie
 		String userInfo = Utilities.retrieveCookieValue(request, JAMWikiServlet.USER_COOKIE);
 		if (!StringUtils.hasText(userInfo)) return null;
 		StringTokenizer tokens = new StringTokenizer(userInfo, JAMWikiServlet.USER_COOKIE_DELIMITER);
 		if (tokens.countTokens() != 2) return null;
 		String login = tokens.nextToken();
 		String encryptedPassword = tokens.nextToken();
 		try {
 			user = WikiBase.getHandler().lookupWikiUser(login, encryptedPassword, true);
 		} catch (Exception e) {
 			// FIXME - safe to ignore?
 		}
 		if (user != null) request.getSession().setAttribute(JAMWikiServlet.PARAMETER_USER, user);
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public static String decodeURL(String url) {
 		try {
 			url = URLDecoder.decode(url, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.severe("Failure while decoding url " + url + " with charset UTF-8", e);
 		}
 		// convert underscores to spaces
 		url = StringUtils.replace(url, "_", " ");
 		return url;
 	}
 
 	/**
 	 * Converts arbitrary string into string usable as file name.
 	 */
 	public static String encodeSafeFileName(String name) {
 		// replace spaces with underscores
 		name = StringUtils.replace(name, " ", "_");
 		// URL encode the rest of the name
 		try {
 			name = URLEncoder.encode(name, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.severe("Failure while encoding " + name + " with charset UTF-8", e);
 		}
 		return name;
 	}
 
 	/**
 	 *
 	 */
 	public static String encodeURL(String url) {
 		// convert spaces to underscores
 		url = StringUtils.replace(url, " ", "_");
 		try {
 			url = URLEncoder.encode(url, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.severe("Failure while encoding url " + url + " with charset UTF-8", e);
 		}
 		// FIXME - un-encode colons.  handle this better.
 		url = StringUtils.replace(url, "%3A", ":");
 		return url;
 	}
 
 	/**
 	 * Replace any occurrences of <, >, ", ', or & with their HTML equivalents.
 	 */
 	public static String escapeHTML(String input) {
 		if (!StringUtils.hasText(input)) return input;
 		// for obvious reasons ampersand must be replaced first
 		input = StringUtils.replace(input, "&", "&amp;");
 		input = StringUtils.replace(input, ">", "&gt;");
 		input = StringUtils.replace(input, "<", "&lt;");
 		input = StringUtils.replace(input, "\"", "&quot;");
 		input = StringUtils.replace(input, "'", "&#39;");
 		return input;
 	}
 
 	/**
 	 * Given an article name, return the appropriate comments topic article name.
 	 */
 	public static String extractCommentsLink(String name) {
 		if (name == null || name.startsWith(WikiBase.NAMESPACE_SPECIAL)) {
 			return null;
 		}
 		if (Utilities.isCommentsPage(name)) {
 			return name;
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_CATEGORY)) {
 			return WikiBase.NAMESPACE_CATEGORY_COMMENTS + name.substring(WikiBase.NAMESPACE_CATEGORY.length());
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_IMAGE)) {
 			return WikiBase.NAMESPACE_IMAGE_COMMENTS + name.substring(WikiBase.NAMESPACE_IMAGE.length());
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_USER)) {
 			return WikiBase.NAMESPACE_USER_COMMENTS + name.substring(WikiBase.NAMESPACE_USER.length());
 		}
 		return WikiBase.NAMESPACE_COMMENTS + name;
 	}
 
 	/**
 	 * Given an article name, return the appropriate topic article name.
 	 */
 	public static String extractTopicLink(String name) {
 		if (name == null) {
 			return null;
 		}
 		if (!Utilities.isCommentsPage(name)) {
 			return name;
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_COMMENTS)) {
 			return name.substring(WikiBase.NAMESPACE_COMMENTS.length());
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_CATEGORY_COMMENTS)) {
 			return WikiBase.NAMESPACE_CATEGORY + name.substring(WikiBase.NAMESPACE_CATEGORY_COMMENTS.length());
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_IMAGE_COMMENTS)) {
 			return WikiBase.NAMESPACE_IMAGE + name.substring(WikiBase.NAMESPACE_IMAGE_COMMENTS.length());
 		}
 		if (name.startsWith(WikiBase.NAMESPACE_USER_COMMENTS)) {
 			return WikiBase.NAMESPACE_USER + name.substring(WikiBase.NAMESPACE_USER_COMMENTS.length());
 		}
 		return name;
 	}
 
 	/**
 	 * Returns any trailing . , ; : characters on the given string
 	 * @param text
 	 * @return empty string if none are found
 	 */
 	public static String extractTrailingPunctuation(String text) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = text.length() - 1; i >= 0; i--) {
 			char c = text.charAt(i);
 			if (c == '.' || c == ';' || c == ',' || c == ':' || c == ')' || c == '(' || c == ']' || c == '[') {
 				buffer.append(c);
 			} else {
 				break;
 			}
 		}
 		if (buffer.length() == 0) return "";
 		buffer = buffer.reverse();
 		return buffer.toString();
 	}
 
 	/**
 	 *
 	 */
 	public static File getClassLoaderFile(String filename) throws Exception {
 		// note that this method is used when initializing logging, so it must
 		// not attempt to log anything.
 		Method method = null;
 		ClassLoader loader = null;
 		URL url = null;
 		File file = null;
 		try {
 			// first try to use the standard class loader path
 			method = Thread.class.getMethod("getContextClassLoader", null);
 			loader = (ClassLoader)method.invoke(Thread.currentThread(), null);
 		} catch (Exception e) {
 			// that didn't work... try something else
 		}
 		if (loader == null) {
 			// attempt the the class loader that loaded this class
 			loader = Utilities.class.getClassLoader();
 		}
 		if (loader == null) {
 			throw new Exception("Unable to find class loader");
 		}
 		url = loader.getResource(filename);
 		if (url != null) {
 			file = FileUtils.toFile(url);
 		} else {
 			url = ClassLoader.getSystemResource(filename);
 			if (url == null) {
 				throw new Exception("Unable to find " + filename);
 			}
 			file = FileUtils.toFile(url);
 		}
 		if (file == null | !file.exists()) {
 			throw new Exception("Found invalid class loader root " + file);
 		}
 		return file;
 	}
 
 	/**
 	 * Attempt to get the class loader root directory by retrieving a file
 	 * that MUST exist in the class loader root and then returning its
 	 * parent directory.
 	 */
 	public static File getClassLoaderRoot() throws Exception {
 		File file = Utilities.getClassLoaderFile("ApplicationResources.properties");
 		if (!file.exists()) {
 			throw new Exception("Unable to find class loader root");
 		}
 		return file.getParentFile();
 	}
 
 	/**
 	 * Get messages for the given locale
 	 * @param locale locale
 	 * @return
 	 */
 	public static String getMessage(String key, Locale locale) {
 		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
 		return messages.getString(key);
 	}
 
 	/**
 	 *
 	 */
 	public static String getMessage(String key, Locale locale, Object[] params) {
 		MessageFormat formatter = new MessageFormat("");
 		formatter.setLocale(locale);
 		String message = Utilities.getMessage(key, locale);
 		formatter.applyPattern(message);
 		return formatter.format(params);
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isAdmin(HttpServletRequest request) {
 		WikiUser user = currentUser(request);
 		return (user != null && user.getAdmin());
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isCommentsPage(String topicName) {
 		if (topicName.startsWith(WikiBase.NAMESPACE_COMMENTS)) {
 			return true;
 		}
 		if (topicName.startsWith(WikiBase.NAMESPACE_CATEGORY_COMMENTS)) {
 			return true;
 		}
 		if (topicName.startsWith(WikiBase.NAMESPACE_IMAGE_COMMENTS)) {
 			return true;
 		}
 		if (topicName.startsWith(WikiBase.NAMESPACE_USER_COMMENTS)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isFirstUse() {
 		return !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isUpgrade() {
 		if (Utilities.isFirstUse()) return false;
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		WikiVersion currentVersion = new WikiVersion(WikiVersion.CURRENT_WIKI_VERSION);
 		return (oldVersion.before(currentVersion));
 	}
 
 	/**
 	 * Determine if the given string is an IP address.
 	 */
 	public static boolean isIpAddress(String ipAddress) {
 		// note that a regular expression would be the easiest way to handle
 		// this, but regular expressions don't handle things like "number between
 		// 0 and 255" very well, so use a heavier approach
 		// if no text, obviously not valid
 		if (!StringUtils.hasText(ipAddress)) return false;
 		// must contain three periods
 		if (StringUtils.countOccurrencesOf(ipAddress, ".") != 3) return false;
 		// ip addresses must be between seven and 15 characters long
 		if (ipAddress.length() < 7 || ipAddress.length() > 15) return false;
 		// verify that the string is "0-255.0-255.0-255.0-255".
 		StringTokenizer tokens = new StringTokenizer(ipAddress, ".");
 		String token = null;
 		int number = -1;
 		while (tokens.hasMoreTokens()) {
 			token = tokens.nextToken();
 			try {
 				number = Integer.parseInt(token);
 				if (number < 0 || number > 255) return false;
 			} catch (Exception e) {
 				// not a number
 				return false;
 			}
 		}
 		// all tests passed, it's an IP address
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public static ParserOutput parse(ParserInput parserInput, String content, String topicName) throws Exception {
 		if (content == null) {
 			return null;
 		}
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseHTML(content, topicName);
 	}
 
 	/**
 	 *
 	 */
 	public static ParserOutput parsePreSave(ParserInput parserInput, String content) throws Exception {
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parsePreSave(content);
 	}
 
 	/**
 	 *
 	 */
 	private static AbstractParser parserInstance(ParserInput parserInput) throws Exception {
 		String parserClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
 		logger.fine("Using parser: " + parserClass);
		Class clazz = Class.forName(parserClass, true, Thread.currentThread().getContextClassLoader());
 		Class[] parameterTypes = new Class[1];
		parameterTypes[0] = Class.forName("org.jamwiki.parser.ParserInput", true, Thread.currentThread().getContextClassLoader());
 		Constructor constructor = clazz.getConstructor(parameterTypes);
 		Object[] initArgs = new Object[1];
 		initArgs[0] = parserInput;
 		return (AbstractParser)constructor.newInstance(initArgs);
 	}
 
 	/**
 	 *
 	 */
 	public static String parserRedirectContent(String topicName) throws Exception {
 		AbstractParser parser = parserInstance(null);
 		return parser.buildRedirectContent(topicName);
 	}
 
 	/**
 	 *
 	 */
 	public static ParserOutput parserOutput(String content) throws Exception {
 		ParserInput parserInput = new ParserInput();
 		parserInput.setMode(ParserInput.MODE_SEARCH);
 		return Utilities.parsePreSave(parserInput, content);
 	}
 
 	/**
 	 *
 	 */
 	public static ParserOutput parseSlice(HttpServletRequest request, String virtualWiki, String topicName, int targetSection) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null || topic.getTopicContent() == null) {
 			return null;
 		}
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setTopicName(topicName);
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setMode(ParserInput.MODE_SLICE);
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseSlice(topic.getTopicContent(), topicName, targetSection);
 	}
 
 	/**
 	 *
 	 */
 	public static ParserOutput parseSplice(HttpServletRequest request, String virtualWiki, String topicName, int targetSection, String replacementText) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null || topic.getTopicContent() == null) {
 			return null;
 		}
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setTopicName(topicName);
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setMode(ParserInput.MODE_SPLICE);
 		AbstractParser parser = parserInstance(parserInput);
 		return parser.parseSplice(topic.getTopicContent(), topicName, targetSection, replacementText);
 	}
 
 	/**
 	 *
 	 */
 	public static Iterator processMultipartRequest(HttpServletRequest request) throws Exception {
 		// Create a factory for disk-based file items
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setRepository(new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH)));
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		upload.setSizeMax(Environment.getLongValue(Environment.PROP_FILE_MAX_FILE_SIZE));
 		return upload.parseRequest(request).iterator();
 	}
 
 	/**
 	 * Read a file and return its contents as a String.
 	 */
 	public static String readFile(String filename) throws Exception {
 		File file = new File(filename);
 		if (file.exists()) {
 			// file passed in as full path
 			return FileUtils.readFileToString(file, "UTF-8");
 		}
 		// look for file in resource directories
 		Class[] parameterTypes = null;
 		Method method = Thread.class.getMethod("getContextClassLoader", parameterTypes);
 		Object[] args = null;
 		ClassLoader loader = (ClassLoader)method.invoke(Thread.currentThread(), args);
 		URL url = loader.getResource(filename);
 		file = FileUtils.toFile(url);
 		if (file == null || !file.exists()) {
 			throw new FileNotFoundException("File " + filename + " is not available for reading");
 		}
 		return FileUtils.readFileToString(file, "UTF-8");
 	}
 
 	/**
 	 *
 	 */
 	public static final void removeCookie(HttpServletResponse response, String cookieName) {
 		Cookie cookie = new Cookie(cookieName, null);
 		cookie.setMaxAge(0);
 		// FIXME - need path to be the server base
 		//cookie.setPath("/");
 		response.addCookie(cookie);
 	}
 
 	/**
 	 *
 	 */
 	public static String retrieveCookieValue(HttpServletRequest request, String cookieName) {
 		Cookie[] cookies = request.getCookies();
 		if (cookies == null) return null;
 		Cookie cookie = null;
 		for (int i=0; i < cookies.length; i++) {
 			if (cookies[i].getName().equals(cookieName)) {
 				cookie = cookies[i];
 				break;
 			}
 		}
 		if (cookie == null) return null;
 		return cookie.getValue();
 	}
 
 	/**
 	 * Verify that a directory exists and is writable.
 	 *
 	 * @param name The full name (including the path) for the directory being tested.
 	 * @return A WikiMessage object containing any error encountered, otherwise
 	 *  <code>null</code>.
 	 */
 	public static WikiMessage validateDirectory(String name) {
 		File directory = new File(name);
 		if (!directory.exists() || !directory.isDirectory()) {
 			return new WikiMessage("error.directoryinvalid", name);
 		}
 		String filename = "jamwiki-test-" + System.currentTimeMillis() + ".txt";
 		File file = new File(name, filename);
 		String text = "Testing";
 		String read = null;
 		try {
 			// attempt to write a temp file to the directory
 			FileUtils.writeStringToFile(file, text, "UTF-8");
 		} catch (Exception e) {
 			return new WikiMessage("error.directorywrite", name, e.getMessage());
 		}
 		try {
 			// verify that the file was correctly written
 			read = FileUtils.readFileToString(file, "UTF-8");
 			if (read == null || !text.equals(read)) throw new IOException();
 		} catch (Exception e) {
 			return new WikiMessage("error.directoryread", name, e.getMessage());
 		}
 		try {
 			// attempt to delete the file
 			FileUtils.forceDelete(file);
 		} catch (Exception e) {
 			return new WikiMessage("error.directorydelete", name, e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	public static boolean validateName(String name) {
 		if (!StringUtils.hasText(name)) return false;
 		if (name.toLowerCase().trim().startsWith(WikiBase.NAMESPACE_SPECIAL.toLowerCase())) return false;
 		// try to remove invalid characters
 		String cleaned = StringUtils.deleteAny(name, "\n\r\"><{}#/\\=[]");
 		return name.equals(cleaned);
 	}
 
 	/**
 	 * Validate that vital system properties, such as database connection settings,
 	 * have been specified properly.
 	 */
 	public static Vector validateSystemSettings(Properties props) {
 		Vector errors = new Vector();
 		// test directories
 		WikiMessage baseDirError = Utilities.validateDirectory(props.getProperty(Environment.PROP_BASE_FILE_DIR));
 		if (baseDirError != null) {
 			errors.add(baseDirError);
 		}
 		WikiMessage fullDirError = Utilities.validateDirectory(props.getProperty(Environment.PROP_FILE_DIR_FULL_PATH));
 		if (fullDirError != null) {
 			errors.add(fullDirError);
 		}
 		// test database
 		String databaseType = props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE);
 		if (databaseType != null && databaseType.equals("DATABASE")) {
 			String driver = props.getProperty(Environment.PROP_DB_DRIVER);
 			String url = props.getProperty(Environment.PROP_DB_URL);
 			String userName = props.getProperty(Environment.PROP_DB_USERNAME);
 			String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
 			if (!DatabaseConnection.testDatabase(driver, url, userName, password)) {
 				errors.add(new WikiMessage("error.databaseconnection"));
 			}
 		}
		// verify valid parser class
		boolean validParser = true;
		String parserClass = props.getProperty(Environment.PROP_PARSER_CLASS);
		String abstractParserClass = "org.jamwiki.parser.AbstractParser";
		if (parserClass == null || parserClass.equals(abstractParserClass)) validParser = false;
		try {
			Class parent = Class.forName(parserClass, true, Thread.currentThread().getContextClassLoader());
			Class child = Class.forName(abstractParserClass, true, Thread.currentThread().getContextClassLoader());
			if (!child.isAssignableFrom(parent)) validParser = false;
		} catch (Exception e) {
			validParser = false;
		}
		if (!validParser) errors.add(new WikiMessage("error.parserclass", parserClass));
 		return errors;
 	}
 }
