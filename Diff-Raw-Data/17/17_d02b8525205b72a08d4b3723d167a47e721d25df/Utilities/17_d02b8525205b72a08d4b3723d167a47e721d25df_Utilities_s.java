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
 
 import java.lang.reflect.Method;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class Utilities {
 
 	private static final Logger logger = Logger.getLogger(Utilities.class);
 
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
 	 * Create the root path for a specific WIKI without the server name.
 	 * This is useful for local redirection or local URL's (relative URL's to the server).
 	 * @param request The HttpServletRequest
 	 * @param virtualWiki The name of the current virtual Wiki
 	 * @return the root path for this viki
 	 */
 	public static String createLocalRootPath(HttpServletRequest request, String virtualWiki) {
 		String contextPath = "";
 		contextPath += request.getContextPath();
 		if (virtualWiki == null || virtualWiki.length() < 1) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		return contextPath + "/" + virtualWiki + "/";
 	}
 
 	/**
 	 * Create the root path for a specific WIKI with a specific server
 	 * @param request The HttpServletRequest
 	 * @param virtualWiki The name of the current virtual Wiki
 	 * @param server the specific server given for the path.
 	 *			   If it is set to "null" or an empty string, it will take
 	 *			   the servername from the given request.
 	 * @return the root path for this viki
 	 */
 	public static String createRootPath(HttpServletRequest request, String virtualWiki, String server) {
 		String contextPath = "";
 		if (server == null || server.trim().equals("")) {
 			contextPath = "http://" + request.getServerName();
 		} else {
 			contextPath = "http://" + server;
 		}
 		if (request.getServerPort() != 80) {
 			contextPath += ":" + request.getServerPort();
 		}
 		contextPath += request.getContextPath();
 		if (virtualWiki == null || virtualWiki.length() < 1) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		return contextPath + "/" + virtualWiki + "/";
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
 	 * Converts back file name encoded by encodeSafeFileName().
 	 */
 	public static String decodeSafeFileName(String name) {
 		// URL decode the rest of the name
 		try {
 			name = URLDecoder.decode(name, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.error("Failure while decoding " + name + " with charset UTF-8", e);
 		}
 		// replace spaces with underscores
 		name = StringUtils.replace(name, " ", "_");
 		return name;
 	}
 
 	/**
 	 *
 	 */
 	public static String decodeURL(String url) {
 		try {
 			url = URLDecoder.decode(url, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.error("Failure while decoding url " + url + " with charset UTF-8", e);
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
 			logger.error("Failure while encoding " + name + " with charset UTF-8", e);
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
 			logger.error("Failure while encoding url " + url + " with charset UTF-8", e);
 		}
 		// FIXME - un-encode colons.  handle this better.
 		url = StringUtils.replace(url, "%3A", ":");
 		return url;
 	}
 
 	/**
 	 * Replace any occurrences of <, >, ", ', or & with their HTML equivalents.
 	 */
 	public static String escapeHTML(String input) {
 		// for obvious reasons ampersand must be replaced first
 		input = StringUtils.replace(input, "&", "&amp;");
 		input = StringUtils.replace(input, ">", "&gt;");
 		input = StringUtils.replace(input, "<", "&lt;");
 		input = StringUtils.replace(input, "\"", "&quot;");
 		input = StringUtils.replace(input, "'", "&apos;");
 		return input;
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
 			if (c == '.' || c == ';' || c == ',' || c == ':') {
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
 	 * Localised
 	 */
 	public static String formatDate(Date date) {
 		return DateFormat.getDateInstance().format(date);
 	}
 
 	/**
 	 *
 	 */
 	public static String formatDateTime(Date date) {
 		return DateFormat.getDateTimeInstance().format(date);
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
 	public static String getMessage(String key, Locale locale, Object param1) {
 		Object[] objects = new Object[1];
 		objects[0] = param1;
 		return Utilities.getMessage(key, locale, objects);
 	}
 
 	/**
 	 *
 	 */
 	public static String getMessage(String key, Locale locale, Object param1, Object param2) {
 		Object[] objects = new Object[2];
 		objects[0] = param1;
 		objects[1] = param2;
 		return Utilities.getMessage(key, locale, objects);
 	}
 
 	/**
 	 *
 	 */
 	public static String getMessage(String key, Locale locale, Object[] objects) {
 		MessageFormat formatter = new MessageFormat("");
 		formatter.setLocale(locale);
 		String message = Utilities.getMessage(key, locale);
 		formatter.applyPattern(message);
 		return formatter.format(objects);
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
 	public static boolean isFirstUse() {
 		return !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isUpgrade() {
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		WikiVersion currentVersion = new WikiVersion(WikiBase.WIKI_VERSION);
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
 		StringBuffer output = new StringBuffer();
 		InputStreamReader reader = null;
 		try {
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
 			InputStream stream = loader.getResourceAsStream(filename);
 			if (stream == null) {
 				throw new FileNotFoundException("File " + filename + " is not available for reading");
 			}
 			reader = new InputStreamReader(stream);
 			char[] buf = new char[4096];
 			int c;
 			while ((c = reader.read(buf, 0, buf.length)) != -1) {
 				output.append(buf, 0, c);
 			}
 			return output.toString();
 		} finally {
 			try {
 				if (reader != null) reader.close();
 			} catch (Exception e) {}
 		}
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
 	 *
 	 */
 	public static String toUTF8(String text) {
 		if (!StringUtils.hasText(text)) return text;
 		try {
 			text = new String(text.getBytes("ISO-8859-1"), "UTF-8");
 		} catch (Exception e) {
 			// bad encoding
 			logger.info("Unable to convert value " + text + " from ISO-8859-1 to UTF-8", e);
 		}
 		return text;
 	}
 }
