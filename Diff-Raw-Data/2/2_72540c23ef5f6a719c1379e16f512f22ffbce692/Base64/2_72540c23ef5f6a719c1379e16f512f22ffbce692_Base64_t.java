 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types.standard;
 
 import org.xins.common.types.Type;
 import org.xins.common.types.TypeValueException;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Standard type <em>_base64</em>.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class Base64 extends Type {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public final static Base64 SINGLETON = new Base64();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
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
          try {
             return org.xins.common.text.Base64.decode(string);
          } catch (Exception ex) {
             throw new TypeValueException(SINGLETON, string);
          }
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
          return org.xins.common.text.Base64.decode(string);
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
          return org.xins.common.text.Base64.encodeBytes(value);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Float32</code>.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Base64() {
       this("base64", 0, Integer.MAX_VALUE);
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
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The minimum number of bytes this Base64 can have.
     */
    private final int _minimum;
 
    /**
     * The maximum number of bytes this Base64 can have.
     */
    private final int _maximum;
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected boolean isValidValueImpl(String value) {
       try {
          byte[] number = org.xins.common.text.Base64.decode(value);
          if (number.length < _minimum || number.length > _maximum) {
             return false;
          }
          return true;
       } catch (Exception ex) {
          return false;
       }
    }
 
    protected Object fromStringImpl(String string) {
       return Float.valueOf(string);
    }
 
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
       MandatoryArgumentChecker.check("value", value);
       byte[] b = (byte[]) value;
       return new String(b);
    }
 }
