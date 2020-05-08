 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types.standard;
 
 import java.io.UnsupportedEncodingException;
 
 import org.xins.common.types.Type;
 import org.xins.common.types.TypeValueException;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Standard type <em>_base64</em>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.1.0
  */
 public class Base64 extends Type {
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public static final Base64 SINGLETON = new Base64();
 
    /**
     * The encoding used to convert a String to a byte[] and vice versa.
     */
    private static final String STRING_ENCODING = "US-ASCII";
 
    /**
     * The minimum number of bytes this Base64 can have.
     */
    private final int _minimum;
 
    /**
     * The maximum number of bytes this Base64 can have.
     */
    private final int _maximum;
 
    /**
    * Constructs a new <code>Float32</code>.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Base64() {
       this("_base64", 0, Integer.MAX_VALUE);
    }
 
    /**
    * Constructs a new <code>Float32</code> object (constructor for
     * subclasses).
     *
     * @param name
     *    the name of this type, cannot be <code>null</code>.
     *
     * @param minimum
    *    the minimum for the value.
     *
     * @param maximum
    *    the maximum for the value.
     */
    protected Base64(String name, int minimum, int maximum) {
       super(name, byte[].class);
 
       _minimum = minimum;
       _maximum = maximum;
    }
 
    /**
    * Converts the specified non-<code>null</code> string value to an
     * <code>byte[]</code>.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @return
     *    the <code>byte[]</code> value.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static byte[] fromStringForRequired(String string)
    throws IllegalArgumentException, TypeValueException {
       if (string == null) {
          throw new IllegalArgumentException("string == null");
       } else {
          return fromStringForOptional(string);
       }
    }
 
    /**
    * Converts the specified string value to an <code>byte[]</code> value.
     *
     * @param string
     *    the string to convert, can be <code>null</code>.
     *
     * @return
     *    the byte[], or <code>null</code> if
     *    <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static byte[] fromStringForOptional(String string)
    throws TypeValueException {
 
       if (string == null) {
          return null;
       }
 
       try {
          byte[] encoded = string.getBytes(STRING_ENCODING);
          if (!org.apache.commons.codec.binary.Base64.isArrayByteBase64(encoded)) {
             throw new TypeValueException(SINGLETON, string);
          }
          return org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
       } catch (Exception ex) {
          throw new TypeValueException(SINGLETON, string);
       }
    }
 
    /**
    * Converts the specified <code>byte[]</code> to a string.
     *
     * @param value
     *    the value to convert, can be <code>null</code>.
     *
     * @return
    *    the textual representation of the value, or <code>null</code> if and
     *    only if <code>value == null</code>.
     */
    public static String toString(byte[] value) {
       if (value == null) {
          return null;
       } else {
          try {
             return new String(org.apache.commons.codec.binary.Base64.encodeBase64(value), STRING_ENCODING);
          } catch (UnsupportedEncodingException uee) {
             return null;
          }
       }
    }
 
    /**
     * Determines if the specified <code>String</code> value is considered
     * valid for this type (implementation method).
     *
     * <p>This method is called from {@link #isValidValue(String)}. When
     * called from that method, it is guaranteed that the argument is not
     * <code>null</code>.
     *
     * @param string
     *    the <code>String</code> value that should be checked for validity,
     *    never <code>null</code>.
     *
     * @return
     *    <code>true</code> if and only if the specified <code>String</code>
     *    value is valid, <code>false</code> otherwise.
     */
    protected boolean isValidValueImpl(String string) {
       try {
          byte[] encoded = string.getBytes(STRING_ENCODING);
          if (!org.apache.commons.codec.binary.Base64.isArrayByteBase64(encoded)) {
             return false;
          }
          byte[] number = org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
          if (number.length < _minimum || number.length > _maximum) {
             return false;
          }
          return true;
       } catch (Exception ex) {
          // TODO: Log
          return false;
       }
    }
 
    /**
     * Converts from a <code>String</code> to an instance of the value class
     * for this type (implementation method).
     *
     * <p>This method is not required to check the validity of the specified
     * value (since {@link #isValidValueImpl(String)} should have been called
     * before) but if it does, then it may throw a {@link TypeValueException}.
     *
     * @param string
     *    the string to convert to an instance of the value class, guaranteed
     *    to be not <code>null</code> and guaranteed to have been passed to
     *    {@link #isValidValueImpl(String)} without getting an exception.
     *
     * @return
     *    an instance of the value class, cannot be <code>null</code>.
     *
     * @throws TypeValueException
     *    if <code>string</code> is considered to be an invalid value for this
     *    type.
     */
    protected Object fromStringImpl(String string) throws TypeValueException {
       try {
          byte[] encoded = string.getBytes(STRING_ENCODING);
          return org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
       } catch (Exception ex) {
          throw new TypeValueException(SINGLETON, string, ex.getMessage());
       }
    }
 
    /**
     * Generates a string representation of the specified value for this type.
     * The specified value must be an instance of the value class for this type
     * (see {@link #getValueClass()}). Also, it may have to fall within a
     * certain range of valid values, depending on the type.
     *
     * @param value
     *    the value, cannot be <code>null</code>.
     *
     * @return
     *    the string representation of the specified value for this type,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>value == null</code>.
     *
     * @throws ClassCastException
     *    if <code>getValueClass().isInstance(value) == false</code>.
     *
     * @throws TypeValueException
     *    if the specified value is not in the allowed range.
     */
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("value", value);
 
       // Convert the argument to a byte array (may throw ClassCastException)
       byte[] b = (byte[]) value;
 
       // Try encoding the byte array as a Base64 string
       try {
          return new String(org.apache.commons.codec.binary.Base64.encodeBase64(b), STRING_ENCODING);
       } catch (UnsupportedEncodingException uee) {
          String message = "Encoding " + STRING_ENCODING + " not supported.";
          throw new TypeValueException(SINGLETON, new String(b), message);
       }
    }
 
    public String getDescription() {
       return "Binary format coded using base64 enconding.";
    }
 }
