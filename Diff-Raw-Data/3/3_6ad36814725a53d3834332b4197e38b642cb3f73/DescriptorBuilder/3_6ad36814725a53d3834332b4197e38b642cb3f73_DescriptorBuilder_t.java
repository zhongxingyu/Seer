 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.service;
 
 import java.net.MalformedURLException;
 import java.util.StringTokenizer;
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 
 /**
  * Builder that can build a <code>Descriptor</code> object based on a set of
  * properties.
  *
  * <h3>Examples</h3>
  *
  * <p>The following example is the definition of a single backend at
  * <code>http://somehost/</code>, identified by the property name
  * <code>"backend"</code>, the time-out is set to 20 seconds:
  *
  * <blockquote><code>backend=service, http://somehost/, 20000</code></blockquote>
  *
  * <p>The next example is the definition of 4 backends, of which one will be
  * chosen randomly. This setting is identified by the property name
  * <code>"capi.sso"</code>:
  *
  * <blockquote><code># The root definition "capi.sso"
  * <br>capi.sso=group, random, target1, target2, target3, target4
  * <br>
  * <br># Total time-out is 12.5 seconds, no connection time-out and no socket
  * <br># time-out
  * <br>capi.sso.target1=service, http://somehost/, 12500
  * <br>
  * <br># Total time-out is 12.5 seconds, connection time-out is 4 seconds and
  * <br># no socket time-out
  * <br>capi.sso.target2=service, http://othrhost/, 12500, 4000
  * <br>
  * <br># Total time-out is 12.5 seconds, connection time-out is 4 seconds,
  * <br># socket time-out is 2 seconds
  * <br>capi.sso.target3=service, http://othrhost:2001/, 12500, 4000, 2000
  * <br>
  * <br># Total time-out is not set, connection time-out is not set and socket
  * <br># time-out is 2 seconds
  * <br>capi.sso.target4=service, http://othrhost:2002/, 0, 0, 2000</code></blockquote>
  *
  * <p>The last example defines 2 backends at a more preferred location and 1
  * at a less-preferred location. Normally one of the 2 backends at the
  * preferred location will be chosen randomly, but if none is available, then
  * the backend at the less preferred location will be tried. The time-out for
  * all backends in 8 seconds. The name of the property is <code>"ldap"</code>:
  *
  * <blockquote><code>ldap=group, ordered, loc1, host2a
  * <br>ldap.loc1=group, random, host1a, host1b
  * <br>ldap.host1a=service, ldap://host1a/, 8000
  * <br>ldap.host1b=service, ldap://host1b/, 8000
  * <br>ldap.host2a=service, ldap://host2a/, 8000</code></blockquote>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class DescriptorBuilder extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Delimiter between tokens within a property value. This is the comma
     * character <code>','</code>.
     */
    public static final char DELIMITER = ',';
 
    /**
     * Delimiters between tokens within a property value.
     */
    private static final String DELIMITER_AS_STRING = String.valueOf(DELIMITER);
 
    /**
     * Name identifying an actual target descriptor.
     */
    public static final String TARGET_DESCRIPTOR_TYPE = "service";
 
    /**
     * Name identifying a group of descriptors.
     */
    public static final String GROUP_DESCRIPTOR_TYPE = "group";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Tokenizes the specified string. The {@link #DELIMITER_AS_STRING} will be
     * used as the token delimiter. Every token will be one element in the
     * returned {@link String} array.
     *
     * @param s
     *    the {@link String} to tokenize, cannot be <code>null</code>.
     *
     * @return
     *    the list of tokens as a {@link String} array, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>.
     */
    private static String[] tokenize(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Create a StringTokenizer
       StringTokenizer tokenizer = new StringTokenizer(s, DELIMITER_AS_STRING);
 
       // Create a new array to store the tokens in
       int count = tokenizer.countTokens();
       String[] tokens = new String[count];
 
       // Copy all tokens into the array
       for (int i = 0; i < count; i++) {
          tokens[i] = tokenizer.nextToken().trim();
       }
 
       return tokens;
    }
 
    /**
     * Builds a <code>Descriptor</code> based on the specified set of
     * properties.
     *
     * @param properties
     *    the properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the base for the property names, cannot be <code>null</code>.
     *
     * @return
     *    the {@link Descriptor} that was built, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if the property named <code>propertyName</code> cannot be found in
     *    <code>properties</code>, or if a referenced property cannot be found.
     *
     * @throws InvalidPropertyValueException
     *    if the property named <code>propertyName</code> is found in
     *    <code>properties</code>, but the format of this property or the
     *    format of a referenced property is invalid.
     */
    public static Descriptor build(PropertyReader properties,
                                   String         propertyName)
    throws IllegalArgumentException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties,
                                      "propertyName", propertyName);
       return build(properties, propertyName, null);
    }
 
    /**
     * Builds a <code>Descriptor</code> based on the specified set of
     * properties, specifying base property and reference.
     *
     * @param properties
     *    the properties to read from, should not be <code>null</code>.
     *
     * @param baseProperty
     *    the name of the base property, should not be <code>null</code>.
     *
     * @param reference
     *    the name of the reference, relative to the base property, can be
     *    <code>null</code>.
     *
     * @return
     *    the {@link Descriptor} that was built, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>properties == null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property cannot be found.
     *
     * @throws InvalidPropertyValueException
     *    if the property named <code>propertyName</code> is found in
     *    <code>properties</code>, but the format of this property or the
     *    format of a referenced property is invalid.
     */
    private static Descriptor build(PropertyReader properties,
                                    String         baseProperty,
                                    String         reference)
    throws NullPointerException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException {
 
       // Determine the property name
       String propertyName = reference == null
                           ? baseProperty
                           : baseProperty + '.' + reference;
 
       // Get the value of the property
       String value = properties.get(propertyName);
       if (value == null) {
          throw new MissingRequiredPropertyException(propertyName);
       }
 
       // Tokenize the value
       String[] tokens = tokenize(value);
       int tokenCount = tokens.length;
       if (tokenCount < 3) {
          throw new InvalidPropertyValueException(propertyName, value, "Expected at least 3 tokens.");
       }
 
       // Determine the type
       String descriptorType = tokens[0];
 
       // Parse target descriptor
       if (TARGET_DESCRIPTOR_TYPE.equals(descriptorType)) {
          if (tokenCount < 3 || tokenCount > 5) {
             throw new InvalidPropertyValueException(propertyName, value, "Expected URL and time-out.");
          }
 
          // Determine URL
          String url = tokens[1];
 
          // Determine the total time-out (mandatory)
          int timeOut;
          try {
             timeOut = Integer.parseInt(tokens[2]);
          } catch (NumberFormatException nfe) {
             throw new InvalidPropertyValueException(propertyName, value, "Unable to parse total time-out as a 32-bit integer number.");
          }
          if (timeOut < 0) {
             throw new InvalidPropertyValueException(propertyName, value, "Total time-out is negative.");
          }
 
          // Determine the connection time-out (optional)
          int connectionTimeOut;
          if (tokenCount > 3) {
             try {
                connectionTimeOut = Integer.parseInt(tokens[3]);
             } catch (NumberFormatException nfe) {
                throw new InvalidPropertyValueException(propertyName, value, "Unable to parse connection time-out as a 32-bit integer number.");
             }
             if (connectionTimeOut < 0) {
                throw new InvalidPropertyValueException(propertyName, value, "Connection time-out is negative.");
             }
          } else {
             connectionTimeOut = 0;
          }
 
          // Determine the socket time-out (optional)
          int socketTimeOut;
          if (tokenCount > 4) {
             try {
                socketTimeOut = Integer.parseInt(tokens[4]);
             } catch (NumberFormatException nfe) {
                throw new InvalidPropertyValueException(propertyName, value, "Unable to parse socket time-out as a 32-bit integer number.");
             }
             if (socketTimeOut < 0) {
                throw new InvalidPropertyValueException(propertyName, value, "Socket time-out is negative.");
             }
          } else {
             socketTimeOut = 0;
          }
 
          try {
             return new TargetDescriptor(url, timeOut, connectionTimeOut, socketTimeOut);
          } catch (MalformedURLException exception) {
             Log.log_1300(exception, url);
             throw new InvalidPropertyValueException(propertyName, value, "Malformed URL.");
          }
 
       // Parse group descriptor
       } else if (GROUP_DESCRIPTOR_TYPE.equals(descriptorType)) {
 
          GroupDescriptor.Type groupType = GroupDescriptor.getType(tokens[1]);
          if (groupType == null) {
             throw new InvalidPropertyValueException(propertyName, value, "Unrecognized group descriptor type.");
          }
 
          int memberCount = tokenCount - 2;
         if (memberCount < 2) {
            throw new InvalidPropertyValueException(propertyName, value, "Group descriptor member count is " + memberCount + ", while minimum is 2.");
         }
          Descriptor[] members = new Descriptor[memberCount];
          for (int i = 0; i < memberCount; i++) {
             members[i] = build(properties, baseProperty, tokens[i + 2]);
          }
          return new GroupDescriptor(groupType, members);
 
       // Unrecognized descriptor type
       } else {
          throw new InvalidPropertyValueException(propertyName, value, "Expected valid descriptor type: either \"" + TARGET_DESCRIPTOR_TYPE + "\" or \"" + GROUP_DESCRIPTOR_TYPE + "\".");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>DescriptorBuilder</code>.
     */
    private DescriptorBuilder() {
       // empty
 
       // NOTE: No tracing is performed, since this constructor is never used
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
