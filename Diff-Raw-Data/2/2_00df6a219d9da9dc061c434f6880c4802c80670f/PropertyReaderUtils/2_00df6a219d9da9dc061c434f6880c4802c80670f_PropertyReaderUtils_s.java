 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.io.InputStream;
 import java.io.IOException;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.TextUtils;
 import org.xins.common.text.URLEncoding;
 
 /**
  * Utility functions for dealing with <code>PropertyReader</code> objects.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.0.0
  *
  * @see PropertyReader
  */
 public final class PropertyReaderUtils {
 
    /**
     * An empty and unmodifiable <code>PropertyReader</code> instance. This
     * field is not <code>null</code>.
     *
     * @since XINS 1.1.0
     */
    public static final PropertyReader EMPTY_PROPERTY_READER =
       new ProtectedPropertyReader(new Object());
 
    /**
     * Secret key object used when dealing with
     * <code>ProtectedPropertyReader</code> instances.
     */
    private static final Object SECRET_KEY = new Object();
 
    /**
     * Constructs a new <code>PropertyReaderUtils</code> object. This
     * constructor is marked as <code>private</code>, since no objects of this
     * class should be constructed.
     */
    private PropertyReaderUtils() {
       // empty
    }
 
    /**
     * Gets the property with the specified name and converts it to a
     * <code>boolean</code>.
     *
     * @param properties
     *    the set of properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the name of the property to read, cannot be <code>null</code>.
     *
     * @param fallbackDefault
     *    the fallback default value, returned if the value of the property is
     *    either <code>null</code> or <code>""</code> (an empty string).
     *
     * @return
     *    the value of the property.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws InvalidPropertyValueException
     *    if the value of the property is neither <code>null</code> nor
     *    <code>""</code> (an empty string), nor <code>"true"</code> nor
     *    <code>"false"</code>.
     */
    public static boolean getBooleanProperty(PropertyReader properties,
                                             String         propertyName,
                                             boolean        fallbackDefault)
    throws IllegalArgumentException,
           InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties",   properties,
                                      "propertyName", propertyName);
 
       // Query the PropertyReader
       String value = properties.get(propertyName);
 
       // Fallback to the default, if necessary
       if (TextUtils.isEmpty(value)) {
          return fallbackDefault;
       }
 
       // Parse the string
       if ("true".equals(value)) {
          return true;
       } else if ("false".equals(value)) {
          return false;
       } else {
          throw new InvalidPropertyValueException(propertyName, value);
       }
    }
 
    /**
     * Gets the property with the specified name and converts it to an
     * <code>int</code>.
     *
     * @param properties
     *    the set of properties to read from, cannot be <code>null</code>.
     *
     * @param propertyName
     *    the name of the property to read, cannot be <code>null</code>.
     *
     * @return
     *    the value of the property, as an <code>int</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || propertyName == null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if the specified property is not set, or if it is set to an empty
     *    string.
     *
     * @throws InvalidPropertyValueException
     *    if the conversion to an <code>int</code> failed.
     */
    public static int getIntProperty(PropertyReader properties,
                                     String         propertyName)
    throws IllegalArgumentException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties",   properties,
                                      "propertyName", propertyName);
 
       // Query the PropertyReader
       String value = properties.get(propertyName);
 
       // Make sure the value is set
       if (value == null || value.length() == 0) {
          throw new MissingRequiredPropertyException(propertyName);
       }
 
       // Parse the string
       try {
          return Integer.parseInt(value);
       } catch (NumberFormatException exception) {
          throw new InvalidPropertyValueException(propertyName, value);
       }
    }
 
    /**
     * Retrieves the specified property and throws a
     * <code>MissingRequiredPropertyException</code> if it is not set.
     *
     * @param properties
     *    the set of properties to retrieve a specific proeprty from, cannot be
     *    <code>null</code>.
     *
     * @param name
     *    the name of the property, cannot be <code>null</code>.
     *
     * @return
     *    the value of the property, guaranteed not to be <code>null</code> and
     *    guaranteed to contain at least one character.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || name == null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if the value of the property is either <code>null</code> or an empty
     *    string.
     */
    public static String getRequiredProperty(PropertyReader properties,
                                             String         name)
    throws IllegalArgumentException,
           MissingRequiredPropertyException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties,
                                      "name",       name);
 
       // Retrieve the value
       String value = properties.get(name);
 
       // The property is required
       if (value == null || value.length() < 1) {
          throw new MissingRequiredPropertyException(name);
       }
 
       return value;
    }
 
    /**
     * Constructs a <code>PropertyReader</code> from the specified input
     * stream.
     *
     * <p>The parsing done is similar to the parsing done by the
     * {@link Properties#load(InputStream)} method. Empty values will be
     * ignored.
     *
     * @param in
     *    the input stream to read from, cannot be <code>null</code>.
     *
     * @return
     *    a {@link PropertyReader} instance that contains all the properties
     *    defined in the specified input stream.
     *
     * @throws IllegalArgumentException
     *    if <code>in == null</code>.
     *
     * @throws IOException
     *    if there was an I/O error while reading from the stream.
     */
    public static PropertyReader createPropertyReader(InputStream in)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("in", in);
 
       // Parse the input stream using java.util.Properties
       Properties properties = new Properties();
       properties.load(in);
 
       // Convert from java.util.Properties to PropertyReader
       ProtectedPropertyReader r = new ProtectedPropertyReader(SECRET_KEY);
       Enumeration names = properties.propertyNames();
       while (names.hasMoreElements()) {
          String key   = (String) names.nextElement();
          String value = properties.getProperty(key);
 
          if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
             r.set(SECRET_KEY, key, value);
          }
       }
 
       return r;
    }
 
    /**
     * Serializes the specified <code>PropertyReader</code> to a
     * <code>FastStringBuffer</code>. For each entry, both the key and the
     * value are encoded using the URL encoding (see {@link URLEncoding}).
     * The key and value are separated by a literal equals sign
     * (<code>'='</code>). The entries are separated using
     * an ampersand (<code>'&amp;'</code>).
     *
     * <p>If the value for an entry is either <code>null</code> or an empty
     * string (<code>""</code>), then nothing is added to the buffer for that
     * entry.
     *
     * @param properties
     *    the {@link PropertyReader} to serialize, can be <code>null</code>.
     *
     * @param buffer
     *    the buffer to write the serialized data to, cannot be
     *    <code>null</code>.
     *
     * @param valueIfEmpty
     *    the string to append to the buffer in case
     *    <code>properties == null || properties.size() == 0</code>; if this
     *    argument is <code>null</code>, however, then nothing will be appended
     *    in the mentioned case.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || buffer == null</code>.
     *
     * @deprecated since XINS 2.0, use {@link #toString(PropertyReader, String)}
     */
    public static void serialize(PropertyReader   properties,
                                 FastStringBuffer buffer,
                                 String           valueIfEmpty)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("buffer", buffer);
 
       // Catch special case: No properties available.
       if (properties == null || properties.size() == 0) {
          if (valueIfEmpty != null) {
             buffer.append(valueIfEmpty);
          }
          return;
       }
 
       // Loop over all properties
       Iterator names = properties.getNames();
       boolean first = true;
       while (names.hasNext()) {
 
          // Get the name and value
          String name  = (String) names.next();
          String value = properties.get(name);
 
          // If the value is null or an empty string, then output nothing
          if (value == null) {
             continue;
          }
 
          // Append an ampersand, except for the first entry
          if (!first) {
             buffer.append('&');
          } else {
             first = false;
          }
 
          // Append the key and the value, separated by an equals sign
          buffer.append(URLEncoding.encode(name));
          buffer.append('=');
          buffer.append(URLEncoding.encode(value));
       }
    }
 
    /**
     * Returns the String representation of the specified <code>PropertyReader</code>.
     * For each entry, both the key and the value are encoded using the URL
     * encoding (see {@link URLEncoding}).
     * The key and value are separated by a literal equals sign
     * (<code>'='</code>). The entries are separated using an ampersand
     * (<code>'&amp;'</code>).
     *
     * <p>If the value for an entry is either <code>null</code> or an empty
     * string (<code>""</code>), then nothing is added to the String for that
     * entry.
     *
     * @param properties
     *    the {@link PropertyReader} to serialize, cannot be <code>null</code>.
     *
     * @return
     *    the String representation of the specified <code>PropertyReader</code>.
     *
     * @since XINS 2.0.
     */
    public static String toString(PropertyReader properties) {
       return toString(properties, null, null, null, -1);
    }
 
    /**
     * Serializes the specified <code>PropertyReader</code> to a
     * <code>String</code>. For each entry, both the key and the
     * value are encoded using the URL encoding (see {@link URLEncoding}).
     * The key and value are separated by a literal equals sign
     * (<code>'='</code>). The entries are separated using
     * an ampersand (<code>'&amp;'</code>).
     *
     * <p>If the value for an entry is either <code>null</code> or an empty
     * string (<code>""</code>), then nothing is added to the String for that
     * entry.
     *
     * @param properties
     *    the {@link PropertyReader} to serialize, can be <code>null</code>.
     *
     * @param valueIfEmpty
     *    the string to append to the buffer in case
     *    <code>properties == null || properties.size() == 0</code>.
     *
     * @return
     *    the String representation of the PropertyReader or the valueIfEmpty, never <code>null</code>.
     *    If all parameters are <code>null</code> then an empty String is returned.
     */
    public static String toString(PropertyReader properties,
                                  String         valueIfEmpty) {
       return toString(properties, valueIfEmpty, null, null, -1);
    }
 
 
    /**
     * Returns the <code>String</code> representation for the specified
     * <code>PropertyReader</code>.
     *
     * @param properties
     *    the {@link PropertyReader} to construct a String for, or <code>null</code>.
     *
     * @param valueIfEmpty
     *    the value to return if the specified set of properties is either
     *    <code>null</code> or empty, can be <code>null</code>.
     *
     * @param prefixIfNotEmpty
     *    the prefix to add to the value if the <code>PropertyReader</code>
     *    is not empty, can be <code>null</code>.
     *
     * @param suffix
     *    the suffix to add to the value, can be <code>null</code>. The suffix
     *    will be added even if the PropertyReaderis empty.
     *
     * @return
     *    the String representation of the PropertyReader with the different artifacts, never <code>null</code>.
     *    If all parameters are <code>null</code> then an empty String is returned.
     *
     * @since XINS 2.0
     */
    public static String toString(PropertyReader properties, String valueIfEmpty,
          String prefixIfNotEmpty, String suffix) {
       return toString(properties, valueIfEmpty, prefixIfNotEmpty, suffix, -1);
    }
 
    /**
     * Returns the <code>String</code> representation for the specified
     * <code>PropertyReader</code>.
     *
     * @param properties
     *    the {@link PropertyReader} to construct a String for, or <code>null</code>.
     *
     * @param valueIfEmpty
     *    the value to return if the specified set of properties is either
     *    <code>null</code> or empty, can be <code>null</code>.
     *
     * @param prefixIfNotEmpty
     *    the prefix to add to the value if the <code>PropertyReader</code>
     *    is not empty, can be <code>null</code>.
     *
     * @param suffix
     *    the suffix to add to the value, can be <code>null</code>. The suffix
     *    will be added even if the PropertyReaderis empty.
     *
     * @param maxValueLength
     *    the maximum of characters to set for the value, if the value is longer
     *    than this limit '...' will be added after the limit.
     *    If the value is -1, no limit will be set.
     *
     * @return
     *    the String representation of the PropertyReader with the different artifacts, never <code>null</code>.
     *    If all parameters are <code>null</code> then an empty String is returned.
     *
     * @since XINS 2.0
     */
    public static String toString(PropertyReader properties, String valueIfEmpty,
          String prefixIfNotEmpty, String suffix, int maxValueLength) {
 
       // If the property set if null, return the fallback
       if (properties == null) {
          if (suffix != null) {
             return suffix;
          } else {
             return valueIfEmpty;
          }
       }
 
       Iterator names = properties.getNames();
 
       // If there are no parameters, then return the fallback
       if (!names.hasNext()) {
          if (suffix != null) {
             return suffix;
          } else {
             return valueIfEmpty;
          }
       }
 
       StringBuffer buffer = new StringBuffer(299);
 
       boolean first = true;
       do {
 
          // Get the name and value
          String name  = (String) names.next();
          String value = properties.get(name);
 
          // If the value is null or an empty string, then output nothing
          if (value == null || value.length() == 0) {
             continue;
          }
 
          // Append an ampersand, except for the first entry
          if (!first) {
             buffer.append('&');
          } else {
             first = false;
             if (prefixIfNotEmpty != null) {
                buffer.append(prefixIfNotEmpty);
             }
          }
 
          // Append the key and the value, separated by an equals sign
          buffer.append(URLEncoding.encode(name));
          buffer.append('=');
          String encodedValue;
          if (maxValueLength == -1 || value.length() <= maxValueLength) {
             encodedValue = URLEncoding.encode(value);
          } else {
             encodedValue = URLEncoding.encode(value.substring(0, maxValueLength)) + "...";
          }
          buffer.append(encodedValue);
       } while (names.hasNext());
 
       if (suffix != null) {
          buffer.append('&');
          buffer.append(suffix);
       }
 
       return buffer.toString();
    }
 
    /**
     * Compares a <code>PropertyReader</code> instance with another object for
     * equality.
     *
     * @param pr
     *    the <code>PropertyReader</code>, can be <code>null</code>.
     *
     * @param toCompare
     *    the object to compare the <code>PropertyReader</code> with,
     *    can be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the objects are considered to be equal,
     *    <code>false</code> if they are considered different.
     *
     * @since XINS 2.1
     */
    public static final boolean equals(PropertyReader pr, Object toCompare) {
 
       // Test for identity equality
       if (pr == toCompare) {
          return true;
       }
 
       // If either one is null, then they are not equal (otherwise they would
       // both be null in which case they are identity equal)
       if (pr == null || toCompare == null) {
          return false;
       }
 
       // The 2nd object must implement the PropertyReader interface
       if (! (toCompare instanceof PropertyReader)) {
          return false;
       }
 
       // Size must be the same
       PropertyReader pr2 = (PropertyReader) toCompare;
       if (pr.size() != pr2.size()) {
          return false;
       }
 
       // Loop over all key/value pairs
       Iterator keys = pr.getNames();
       while (keys.hasNext()) {
          String key    = (String) keys.next();
          String value1 = pr.get(key);
          String value2 = pr2.get(key);
          if (value1 == null && value2 != null) {
             return false;
         } else if (! value1.equals(value2)) {
             return false;
          }
       }
 
       // No differences found
       return true;
    }
 
    /**
     * Computes a hash code value for the specified <code>PropertyReader</code>
     * object.
     *
     * @param pr
     *    the <code>PropertyReader</code> instance to compute a hash code value
     *    for, cannot be <code>null</code>.
     *
     * @return
     *    the hash code value.
     *
     * @throws NullPointerException
     *    if <code>pr == null</code>.
     *
     * @since XINS 2.1
     */
    public static final int hashCode(PropertyReader pr)
    throws NullPointerException {
 
       int hash = 0;
 
       // Loop over all key/value pairs
       Iterator keys = pr.getNames();
       while (keys.hasNext()) {
          String key   = (String) keys.next();
          String value = pr.get(key);
 
          // XOR the hash code value with the key string hash code
          if (key != null) {
             hash ^= key.hashCode();
          }
 
          // XOR the hash code value with the key string hash code
          if (value != null) {
             hash ^= value.hashCode();
          }
       }
 
       return hash;
    }
 }
