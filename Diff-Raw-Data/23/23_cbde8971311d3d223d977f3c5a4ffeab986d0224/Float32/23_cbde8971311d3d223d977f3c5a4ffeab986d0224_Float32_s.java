 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types.standard;
 
 import org.xins.common.types.Type;
 import org.xins.common.types.TypeValueException;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Standard type <em>_float32</em>.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class Float32 extends Type {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public final static Float32 SINGLETON = new Float32();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Converts the specified non-<code>null</code> string value to an
     * <code>float</code>.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @return
     *    the <code>float</code> value.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static float fromStringForRequired(String string)
    throws IllegalArgumentException, TypeValueException {
       if (string == null) {
          throw new IllegalArgumentException("string == null");
       } else {
          try {
             return Float.parseFloat(string);
          } catch (NumberFormatException nfe) {
             throw new TypeValueException(SINGLETON, string);
          }
       }
    }
 
    /**
     * Converts the specified string value to an <code>Float</code> value.
     *
     * @param string
     *    the string to convert, can be <code>null</code>.
     *
     * @return
     *    the {@link Float}, or <code>null</code> if
     *    <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Float fromStringForOptional(String string)
    throws TypeValueException {
 
       if (string == null) {
          return null;
       }
 
       try {
          return Float.valueOf(string);
       } catch (NumberFormatException nfe) {
          throw new TypeValueException(SINGLETON, string);
       }
    }
 
    /**
     * Converts the specified <code>Float</code> to a string.
     *
     * @param value
     *    the value to convert, can be <code>null</code>.
     *
     * @return
     *    the textual representation of the value, or <code>null</code> if and
     *    only if <code>value == null</code>.
     */
    public static String toString(Float value) {
       if (value == null) {
          return null;
       } else {
          return toString(value.floatValue());
       }
    }
 
    /**
     * Converts the specified <code>float</code> to a string.
     *
     * @param value
     *    the value to convert.
     *
     * @return
     *    the textual representation of the value, never <code>null</code>.
     */
    public static String toString(float value) {
       return String.valueOf(value);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Float32</code>.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Float32() {
      this("_float32", Float.MIN_VALUE, Float.MAX_VALUE);
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
    protected Float32(String name, float minimum, float maximum) {
       super(name, java.lang.Float.class);
 
       _minimum = minimum;
       _maximum = maximum;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The minimum value that this Float32 can have.
     */
    private final float _minimum;
 
    /**
     * The maximum value that this Float32 can have.
     */
    private final float _maximum;
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected boolean isValidValueImpl(String value) {
       try {
          float number = Float.parseFloat(value);
          if (number < _minimum || number > _maximum) {
             return false;
          }
          return true;
       } catch (NumberFormatException nfe) {
          return false;
       }
    }
 
    protected Object fromStringImpl(String string) {
       return Float.valueOf(string);
    }
 
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
       MandatoryArgumentChecker.check("value", value);
       java.lang.Float f = (java.lang.Float) value;
       return f.toString();
    }
 }
