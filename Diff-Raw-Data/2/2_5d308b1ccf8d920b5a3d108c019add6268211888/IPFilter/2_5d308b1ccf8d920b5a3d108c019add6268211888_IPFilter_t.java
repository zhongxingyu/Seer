 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.ParseException;
 
 /**
  * Authorization filter for IP addresses. An <code>IPFilter</code> object is
  * created and used as follows:
  *
  * <blockquote><code>IPFilter filter = IPFilter.parseFilter("10.0.0.0/24");
  * <br>if (filter.isAuthorized("10.0.0.1")) {
  * <br>&nbsp;&nbsp;&nbsp;// IP is granted access
  * <br>} else {
  * <br>&nbsp;&nbsp;&nbsp;// IP is denied access
  * <br>}</code></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  * @author Peter Troon (<a href="mailto:peter.troon@nl.wanadoo.com">peter.troon@nl.wanadoo.com</a>)
  */
 public final class IPFilter
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The character that delimits the 4 sections of an IP address.
     */
    private static final String IP_ADDRESS_DELIMETER = ".";
 
    /**
     * The character that delimits the IP address and the mask of the provided
     * filter.
     */
    private static final char IP_MASK_DELIMETER = '/';
 
    /**
     * The character that is used to determine whether the provided expression
     * IP contains zero's.
     */
    private static final char ZERO_CHAR = '0';
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates an <code>IPFilter</code> object for the specified filter
     * expression. The expression consists of a base IP address and a bit
     * count. The bit count indicates how many bits in an IP address must match
     * the bits in the base IP address. 
     *
     * @param expression
     *    the filter expression, cannot be <code>null</code> and must match the
     *    form:
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes, and <em>n</em> is a number between <em>0</em> and
     *    <em>32</em>, no leading zeroes.
     *
     * @return
     *    the created IP Filter object.
     *
     * @throws IllegalArgumentException
     *    if <code>expression == null</code>.
     *
     * @throws ParseException
     *    if <code>expression</code> does not match the specified format.
     */
    public static final IPFilter parseIPFilter(String expression)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("expression", expression);
 
       // Find the slash ('/') character
       int slashPosition = expression.indexOf(IP_MASK_DELIMETER);
       if (slashPosition < 0 || slashPosition == expression.length() - 1) {
          throw new ParseException("The string \"" + expression + "\" is not a valid IP filter expression.");
       }
 
       // Get the IP part
       String ip = expression.substring(0, slashPosition);
       if (isValidIP(ip) == false) {
          throw new ParseException("The string \"" + expression + "\" is not a valid IP filter expression.");
       }
 
       // Get the mask part
       int mask = parseMask(expression.substring(slashPosition + 1));
 
       // Create and return an IPFilter object
       return new IPFilter(ip, mask);
    }
 
    /**
     * Parses the specified mask.
     *
     * @param maskString
     *    the mask string, may not be <code>null</code>.
     *
     * @return
     *    an integer representing the value of the mask, between 0 and 32.
     *
     * @throws ParseException
     *    if the specified string is not a mask between 0 and 32, with no
     *    leading zeroes.
     */
    private static final int parseMask(String maskString)
    throws ParseException {
 
       // Convert to an int
       int mask;
       try {
          mask = Integer.parseInt(maskString);
 
       // Catch conversion exception
       } catch (NumberFormatException nfe) {
          throw new ParseException("The mask string \"" + maskString + "\" is not a valid number.");
       }
 
       // Number must be between 0 and 32
       if (mask < 0 || mask > 32) {
          throw new ParseException("The mask string \"" + maskString + "\" is not a number between 0 and 32.");
       }
 
       // Disallow a leading zero
      if (maskString.length() >= 2 && maskString.charAt(0) == '0') {
          throw new ParseException("The mask string \"" + maskString + "\" starts with a leading zero.");
       }
 
       return mask;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates an <code>IPFilter</code> object for the specified filter
     * expression. The expression consists of a base IP address and a bit
     * count. The bit count indicates how many bits in an IP address must match
     * the bits in the base IP address. 
     *
     * @param expression
     *    the filter expression, cannot be <code>null</code> and must match the
     *    form:
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes, and <em>n</em> is a number between <em>0</em> and
     *    <em>32</em>, no leading zeroes.
     */
    private IPFilter(String baseIP, int mask) {
       _expression = baseIP + '/' + mask;
       _baseIP     = baseIP;
       _mask       = mask;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The expression of this filter, cannot be <code>null</code>.
     */
    private final String _expression;
 
    /**
     * The base IP address. Never <code>null</code>.
     */
    private final String _baseIP;
 
    /**
     * The mask of this filter. Can only have a value between 0 and 32.
     */
    private final int _mask;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the filter expression.
     *
     * @return
     *    the original filter expression, never <code>null</code>.
     */
    public final String getExpression() {
       return _expression;
    }
 
    /**
     * Returns the base IP address.
     *
     * @return
     *    the base IP address, in the form 
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes; never <code>null</code>.
     */
    public final String getBaseIP() {
       return _baseIP;
    }
 
    /**
     * Returns the mask.
     *
     * @return
     *    the mask, between 0 and 32.
     */
    public final int getMask() {
       return _mask;
    }
 
    /**
     * Determines if the specified IP address is authorized.
     *
     * @param ip
     *    the IP address of which must be determined if it is authorized,
     *    cannot be <code>null</code> and must match the form:
     *    <code><em>a</em>.<em>a</em>.<em>a</em>.<em>a</em>/<em>n</em></code>,
     *    where <em>a</em> is a number between 0 and 255, with no leading
     *    zeroes.
     *
     * @return
     *    <code>true</code> if the IP address is authorized to access the
     *    protected resource, otherwise <code>false</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null</code>.
     *
     * @throws ParseException
     *    if <code>ip</code> does not match the specified format.
     */
    public final boolean isAuthorized(String ip)
    throws IllegalArgumentException, ParseException {
 
       MandatoryArgumentChecker.check("ip", ip);
 
       String[] ipFields = getIPFields(ip);
 
       if (ipFields == null) {
          throw new ParseException("The provided IP " + ip + " is invalid.");
       }
 
       return determineAuthorized(ipFields);
    }
 
    /**
     * Returns a textual representation of this filter. The implementation of
     * this method returns the filter expression passed.
     *
     * @return
     *    a textual presentation, never <code>null</code>.
     */
    public final String toString() {
       return getExpression();
    }
 
    /**
     * Determines whether the provided IP address is of a valid format.
     *
     * @param ip
     *    the IP address, may not be <code>null</code>.
     *
     * @return
     *    boolean with the value <code>true</code> if the IP is valid,
     *    otherwise false.
     */
    private static boolean isValidIP(String ip) {
 
       // NOTE: This method depends on the reliability of the getIPFields()
       //       method.
 
       String[] ipFields = getIPFields(ip);
       boolean validIP = ipFields == null ? false : true;
       return validIP;
    }
 
    /**
     * Determines whether the provided mask is of a valid format.
     *
     * @param mask
     *    the mask, may not be <code>null</code>.
     *
     * @return
     *    boolean with the value <code>true</code> if the mask is valid,
     *    otherwise false.
     *
     * @throws NullPointerException
     *    when <code>mask == null</code>.
     */
    private static boolean isValidMask(String mask)
    throws NullPointerException {
       int maskLength = mask.length();
 
       if (maskLength < 1 || maskLength > 2) {
          return false;
       }
 
       if (maskLength == 2 && mask.charAt(0) == ZERO_CHAR) {
          return false;
       }
 
       return isAllowedValue(mask, 32);
    }
 
    /**
     * Determines whether the provided IP section (the part of the IP address)
     * is of a valid format. This means that it has to be possible to convert
     * the provided IP section to an integer between 0 and 255.
     *
     * @param ipSection
     *    the IP section, may not be <code>null</code>.
     *
     * @return
     *    boolean with the value <code>true</code> if the IP section is
     *    valid, otherwise false.
     *
     * @throws NullPointerException
     *    when <code>ipSection == null</code>.
     */
    private static boolean isValidIPSection(String ipSection)
    throws NullPointerException {
       int sectionLength = ipSection.length();
 
       if (sectionLength < 1 || sectionLength > 3) {
          return false;
       }
 
       if (sectionLength > 1 && ipSection.charAt(0) == ZERO_CHAR) {
          return false;
       }
 
       return isAllowedValue(ipSection, 255);
    }
 
    /**
     * Determines whether the contents of the provided string are valid, thus.
     * can be translated into an integer value that lies between zero and the
     * specified maximum allowed value.
     *
     * @param value
     *    the value to be checked, should not be <code>null</code>; if it is
     *    than the behaviour is undefined.
     *
     * @param maxAllowedValue
     *    the maximum allowed integer value.
     *
     * @return
     *    boolean with the value <code>true</code> if the provided value
     *    is valid, otherwise false.
     */
    private static boolean isAllowedValue(String value, int maxAllowedValue) {
       boolean validValue = true;
       int intValue = -1;
 
       try {
          intValue = Integer.parseInt(value);
       } catch (NumberFormatException nfe) {
          validValue = false;
       }
 
       if (intValue < 0 || intValue > maxAllowedValue) {
          validValue = false;
       }
 
       return validValue;
    }
 
    /**
     * Creates an array with the several fields (parts separated by a dot) of 
     * the provided IP. If the provided IP is invalid <code>null</code> is
     * returned.
     *
     * @param ip
     *    the IP address, may not be <code>null</code>.
     *
     * @return
     *    an array with the strings representing the value of each IP field
     *    or <code>null</code> if the provided IP is invalid.
     *
     * @throws NullPointerException
     *    when <code>ip == null</code>.
     */
    private static String[] getIPFields(String ip)
    throws NullPointerException {
       StringTokenizer tokenizer = new StringTokenizer(ip, IP_ADDRESS_DELIMETER);
       String[] ipFields = new String[4];
       String currIPSection = null;
       boolean validIP = true;
 
       for (int i = 0;i < 4 && validIP == true; i++) {
          if (tokenizer.hasMoreTokens()) {
              currIPSection = tokenizer.nextToken();
              validIP = isValidIPSection(currIPSection);
              ipFields[i] = currIPSection;
          }
          else {
             validIP = false;
          }
       }
 
       if (tokenizer.hasMoreTokens()) {
          validIP = false;
       }
 
       if (validIP == false) {
          ipFields = null;
       }
 
       return ipFields;
    }
 
    /**
     * Determines what the IP address is of the provided expression.
     *
     * @param expression
     *    the expression, may not be <code>null</code>.
     *
     * @return
     *    A string with the IP address of this expression.
     *
     * @throws NullPointerException
     *    when <code>expression == null</code>.
     */
    private String determineIP(String expression)
    throws NullPointerException {
       String ip = null;
       int slashPosition = expression.indexOf(IP_MASK_DELIMETER);
 
       if (slashPosition < 0 || slashPosition == expression.length() - 1) {
          throw new InternalError("The IP address within the provided filter " + expression + " could not be determined.");
       } else {
          ip = expression.substring(0, slashPosition);
       }
 
       return ip;
    }
 
    /**
     * Determines whether the IP address that is represented by the string
     * array is authorized.
     *
     * @param ipFields
     *    the array containing the values of each IP section separated by the
     *    dots, may not be <code>null</code>.
     *
     * @return
     *    a boolean with the value <code>true</code> if the IP address is 
     *    authorized, otherwise <code>false</code>.
     */
    private boolean determineAuthorized(String[] ipFields) {
       // NOTE: This method depends on the reliability of the determineIP(),
       //       getIPFields() and getIPBinaryValue() methods.
       String filterIp = determineIP(_expression);
       String[] ipFilterFields = getIPFields(filterIp);
 
       String filterIpBinary = getIPBinaryValue(ipFilterFields);
       String ipBinary = getIPBinaryValue(ipFields);
 
       if (filterIpBinary.length() != 32 || ipBinary.length() != 32) {
          return false;
       }
 
       String filterIpBinaryCompare = filterIpBinary.substring(0, _mask);
       String ipBinaryCompare = ipBinary.substring(0, _mask);
 
       return filterIpBinaryCompare.equals(ipBinaryCompare);
    }
 
    /**
     * Determines the binary value of the IP address that is represented by
     * the string array that contains the values of the fields of the IP
     * address.
     *
     * @param ipFields
     *    the array containing the values of each IP section separated by the
     *    dots, may not be <code>null</code>.
     *
     * @return
     *    String with the binary value of the IP that was provided through the
     *    the string array with the values of each IP section.
     *
     * @throws NullPointerException
     *    when <code>ipFields == null</code>.
     */
    private String getIPBinaryValue(String[] ipFields)
    throws NullPointerException {
       int ipFieldLength = ipFields.length;
       StringBuffer buffer = new StringBuffer(32);
       String currFieldBinaryString = null;
       int currFieldInteger = 0;
 
       for (int i = 0;i < ipFieldLength; i++) {
          currFieldInteger = Integer.parseInt(ipFields[i]);
          currFieldBinaryString = Integer.toBinaryString(currFieldInteger);
          buffer.append(currFieldBinaryString);
       }
 
       return buffer.toString();
    }
 
 }
