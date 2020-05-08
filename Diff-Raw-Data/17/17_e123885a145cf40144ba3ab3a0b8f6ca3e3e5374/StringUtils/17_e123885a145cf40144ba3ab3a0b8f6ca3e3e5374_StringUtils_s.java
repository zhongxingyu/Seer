 package newsrack.util;
 
 import java.io.File;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import newsrack.archiver.HTMLFilter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * The class <code>StringUtils</code> provides string and other functionality
  * which does not go anywhere else in the system.
  *
  * @author  Subramanya Sastry
  * @version 1.0 06/05/04
  */
 public final class StringUtils
 {
    private static Log _log = LogFactory.getLog(StringUtils.class);
 
 		/* A public static string that stores the representation for a TAB */
 	public static final String TAB = "   ";
 
 	/** This method prints an error message onto standard error.
      * At a later time, this might throw exceptions or do other
 	  * interesting things.  For now, just print message to stderr.
 	  *
      * @param msg      The error message to be printed 
      */
 	public static void error(String msg)
 	{
 		if (_log.isErrorEnabled()) _log.error("ERROR: " + msg);
 //		System.err.println("ERROR: " + msg);
 	}
 
 	private static void INFO(String msg)
 	{
 		if (_log.isInfoEnabled()) _log.info(msg);
 //		System.err.println("ERROR: " + msg);
 	}
 
 	/**
 	  * This method checks whether a name specified in a NewsRack profile
 	  * is valid.  Refer to specifications as to what constitutes a valid
 	  * NewsRack name.
 	  *
 	  * @param type  The NewsRack type (concept, category, file name, etc.)
 	  *              whose name is being validated
 	  * @param name  The name to validate
      */
 	public static void validateName(String type, String name) throws Exception
 	{
 		int n = name.length();
 		if (n == 0) {
 			throw new Exception("Empty name for " + type);
 		}
 
 		boolean isConcept = type.equals("concept");
 		char[] cs = name.toCharArray();
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 				// White space cannot be part of concept names!
 			if (isConcept && Character.isWhitespace(c)) {
 				throw new Exception(name + " encountered as " + type + "name.  White-space character '" + c + "' at position " + i + " is not part of a valid concept name in NewsRack.");
 			}
 				// No special characters allowed!
 			if (   ((i == 0) && !Character.isUnicodeIdentifierStart(c) && (c != '_'))
 				 || ((i > 0)  && !(   Character.isUnicodeIdentifierPart(c) 
 				 				       || (c == '-') || (c == '_') || (c == '.')
 				 				       || Character.isWhitespace(c))
 				    )
 				)
 			{
 				throw new Exception(name + " encountered as " + type + "name. Character '" + c + "' at position " + i + " is not part of a valid name in NewsRack.  Identifiers have to start with letters/numbers and have either letters, numbers, whitespace, _, -, . as its other characters!");
 			}
 		}
 	}
 
 	/**
 	 * This method makes the string 's' friendly to the underlying OS
 	 * So, let us be conservative and assume that the underlying OS can
 	 * only accepts ASCII!
 	 *
 	 * @param s  The string to be filtered
     */
 	public static String getOSFriendlyName(String s)
 	{
 		int          n  = s.length();
 		char[]       cs = s.toCharArray();
 		StringBuffer ns = new StringBuffer();
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c == '.')) ns.append(c);
 			else if (c == '_') ns.append("__");
 			else if (c == '-') ns.append("_");
 			else if (c == ' ') ns.append("-");
 			else ns.append("$" + Integer.toHexString(c));		// append the unicode value of the character  
 		}
 		return ns.toString();
 	}
 
 	/**
 	 * This method makes the string 's' friendly to the underlying OS as
 	 * well as to Java.  Let us be conservative and assume that the
 	 * underlying OS can only accepts ASCII!
 	 *
 	 * @param s  The string to be filtered
     */
 	public static String getJavaAndOSFriendlyName(String s)
 	{
 		int          n  = s.length();
 		char[]       cs = s.toCharArray();
 		StringBuffer ns = new StringBuffer();
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) ns.append(c);
 			else if (c == '_') ns.append("__");
 			else if (c == '-') ns.append("_");
 			else ns.append("$" + Integer.toHexString(c));		// append the unicode value of the character  
 		}
 		return ns.toString();
 	}
 
 	/**
 	 * This method makes the string 's' friendly to Java -- the name has
 	 * be able to be the name of a Java class.
 	 *
 	 * @param s  The string to be filtered
     */
 	public static String getJavaFriendlyName(String s)
 	{
 		int          n  = s.length();
 		char[]       cs = s.toCharArray();
 		StringBuffer ns = new StringBuffer();
 		for (int i = 0; i < n; i++) {
 			char c = cs[i];
 			if (i > 0) {
 				if (c == '_') ns.append("__");
 				else if (Character.isJavaIdentifierPart(c)) {
 					ns.append(c);
 				}
 				else {
 					ns.append("_" + (int)c);
 				}
 			}
 			else {
 				if (Character.isJavaIdentifierStart(c)) {
 					ns.append(c);
 				}
 				else {
 					ns.append("_" + (int)c);
 				}
 			}
 		}
 		return ns.toString();
 	}
 
 	/**
 	 * This method returns the base name for the URL -- in the process it replaces
 	 * all query references "?" to "_"
 	 *
 	 * @param url  URL whose base name is needed
 	 */
 	public static String getBaseFileName(String url)
 	{
 		String baseName = url.substring(url.lastIndexOf(File.separatorChar) + 1);
 			// replace all '?' and '&' by URL-friendly encoding
 		return baseName.replace('?', '_').replaceAll("&amp;", "_AMP_").replaceAll("&", "_AMP_").trim();
 	} 
 
 	public static String md5(String s)
 	{
 		try {
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 				// FIXME: synchronization required?
 			synchronized (md5) {
 				md5.update(s.getBytes("UTF-8"));
 				byte raw[] = md5.digest();
 				StringBuffer hexString = new StringBuffer();
 				for (byte element : raw) {
 					if ((0xFF & element) < 16) hexString.append('0');	// need 2 bytes each, and Integer.toHexString does not provide leading '0's
 					hexString.append(Integer.toHexString(0xFF & element));
 				}
 				return hexString.toString();
 			}
 		}
 		catch(Exception e) {
 			_log.error("md5", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Parses a date string in RFC822 format and returns a java.util.Date object
 	 * @param d   The RFC822 date format string
 	 */
 	public static Date get_RFC822_Date(String d)
 	{
 		return new javax.mail.internet.MailDateFormat().parse(d, new java.text.ParsePosition(-1));
 	}
 
 	private static final SimpleDateFormat[] _buggyFormat_dfs = {
 		new SimpleDateFormat("EEE, d MMM yyyy"), // Hindu 
 		new SimpleDateFormat("yyyy-MM-dd"),		  // Pantoto
 	};
 
 	/**
 	 * Parses a date string in date formats that are not compliant with
 	 * RSS specification or some other protocol and returns a java.util.Date
 	 * object
 	 * @param d   The date string in whatever format it is
 	 */
 	public static Date get_BUGGY_FORMATS_Date(String d)
 	{
 		if (d == null)
 			return null;
 
 		for (SimpleDateFormat element : _buggyFormat_dfs) {
 			try {
 				synchronized (element) {
 					Date x = element.parse(d);
 					if (x != null) {
 	//					INFO("Date string " + d + " parsed with date format " + _buggyFormat_dfs[i].toLocalizedPattern());
 						return x;
 					}
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 
 		error("Could not parse date: " + d);
 		return null;
 	}
 
 		// This ignores the time zones, but I dont know
 		// how to get SimpleDateFormat to parse the time zones
 		// So, ignoring them for now.
 	private static final SimpleDateFormat[] _dc_dfs = {
 		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S"),
 		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
 		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"),
 		new SimpleDateFormat("yyyy-MM-dd"),
 		new SimpleDateFormat("yyyy-MM"),
 		new SimpleDateFormat("yyyy")
 	};
 
 	/**
 	 * Parses a date string in Dublin Core "dc:date" format and returns
 	 * a java.util.Date object. For details about the format, visit http://www.w3.org/ ...
 	 *
 	 * @param d   The DC:date date format string
 	 */
 	public static Date get_DC_Date(String d)
 	{
 		if (d == null)
 			return null;
 
 		for (SimpleDateFormat element : _dc_dfs) {
 			try {
 				synchronized (element) {
 					Date x = element.parse(d);
 					if (x != null) {
 	//					INFO("Date string " + d + " parsed with date format " + _dc_dfs[i].toLocalizedPattern());
 						return x;
 					}
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 
 		error("Could not parse date: " + d);
 		return null;
 	}
 
 	/**
 	 * Returns a date string in DC:date (yyyy-MM-dd'T'HH:mm) format
 	 *
 	 * @param d   The java.util.Date object
 	 */
 	public static String get_DC_Date(Date d)
 	{
 		synchronized (_dc_dfs[2]) {
 			return _dc_dfs[2].format(d);
 		}
 	}
 
 	public static String truncateHTMLString(String str, int maxLen) throws Exception
 	{
       int strLen = str.length();
       if (strLen <= maxLen) {
 		  return str;
       }
 		else {
          StringBuffer sbText = HTMLFilter.getFilteredTextFromString(str);
          int          k      = sbText.lastIndexOf(" ", maxLen - 5);
          String       newStr = filterForXMLOutput(sbText.substring(0, k)) + " ...";
          if (_log.isDebugEnabled()) {
             _log.debug("#### ORIG - " + str);
             _log.debug("#### Processed - " + sbText);
             _log.debug("#### k - " + k); 
             _log.debug("#### NEW - " + newStr);
          }
          if (_log.isInfoEnabled()) _log.info("### Shortened description from " + strLen + " characters to " + maxLen + " charactors");
 			return newStr;
 		}
 	}
 
 	/**
 	  * This method replaces XML entities with their corresponding
 	  * escaped forms, "&" with "&amp;", and so on.  This filtering
 	  * makes the string suitable to be in an XML file.
 	  *
 	  * @param s  The string to be filtered
 	  * @return   The filtered string
      */
 	public static String filterForXMLOutput(String s)
 	{
 		if (s == null) {
 			error("Got null string: " + s);
 			(new Exception()).printStackTrace();
 			return null;
 		}
 		return s.replaceAll("&", "&amp;")
 		        .replaceAll("\"", "&quot;")
 				  .replaceAll("\'", "&apos;")
 				  .replaceAll("<", "&lt;")
 				  .replaceAll(">", "&gt;")
 				  .replaceAll("&amp;amp;", "&amp;")
 				  .replaceAll("&amp;apos;", "&apos;")
 				  .replaceAll("&amp;quot;", "&quot;");
 	}
 
 	public static String getDomainForUrl(String url)
 	{
 		url = url.replace("http://", "").replace("https://", "");
		return url.substring(0, url.indexOf("/")).replace("www.", "");
 	}
 
 	// FIXME: Shouldn't this be part of a separate token / text / content / stemming analyzer package?
 	public static String pluralize(final String s)
 	{
 	// I18N FIXME: Works only for English, and also very crudely at that!
 
 		if (s.endsWith("s") || s.endsWith("o") || s.endsWith("x") || s.endsWith("ch"))
 			return s + "es";
 		else if (s.endsWith("y"))
 			return s.substring(0, s.length()-1) + "ies";
 		else if (s.endsWith("f"))
 			return s.substring(0, s.length()-1) + "ves";
 		else if (s.endsWith("fe"))
 			return s.substring(0, s.length()-2) + "ves";
 		else {
 			final char end = s.charAt(s.length()-1);
 			if ((end >= 'a' && end <= 'z') || (end >= 'A' && end <= 'Z'))
 				return s + "s";
 			else
 				return null;
 		}
 	}
 
    public static Date getDate(int y, int m, int d)
    {
 		GregorianCalendar cal = new GregorianCalendar();	
       cal.set(y, m, d);
 		return cal.getTime();
    }
 
    public static Date getDate(String y, String m, String d)
    {
       return getDate(Integer.valueOf(y), Integer.valueOf(m), Integer.valueOf(d));
    }
 
    public static Date getDateOffsetByDays(Date start, int numDays)
    {
       return new Date(start.getTime() + numDays*24*60*60*1000);
    }
 
    /**
     * The current thread is put to sleep for n seconds
     */
    public static void sleep(int n)
    {
       try {
          Thread.sleep(1000*n);
       }
       catch (InterruptedException e) {
          Thread.currentThread().interrupt();
       }
    }
 
 	public static void main(String args[]) throws Exception
 	{
 		if (args.length > 0) {
 			validateName(args[0], args[1]);
 			INFO("Name for type " + args[0] + ":" +  args[1] + " is " + getJavaAndOSFriendlyName(args[1]));
 		}
 	}
 }
