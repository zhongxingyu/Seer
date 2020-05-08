 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types.standard;
 
 import org.xins.common.types.Type;
 import org.xins.common.types.TypeValueException;
 
 /**
  * Standard type <em>_text</em>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<A href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</A>)
  *
  * @since XINS 1.0.0
  */
 public final class Text extends Type {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public final static Text SINGLETON = new Text();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Converts the specified non-<code>null</code> string value to a
     * <code>String</code>. This is in fact a no-op, the method will just
     * return the input value. This method exists to be in line with the
     * interfaces of the other standard type classes.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @return
     *    the original {@link String}.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
    *
    * @throws TypeValueException   
    *    if the specified string does not represent a valid value for this
    *    type.
     */
    public static String fromStringForRequired(String string)
   throws IllegalArgumentException, TypeValueException {
       if (string == null) {
          throw new IllegalArgumentException("string == null");
       } else {
          return string;
       }
    }
 
    /**
     * Converts the specified string value to a <code>String</code>
     * value.
     *
     * @param string
     *    the string to convert, can be <code>null</code>.
     *
     * @return
     *    the original {@link String}, can be <code>null</code>.
    *
    * @throws TypeValueException   
    *    if the specified string does not represent a valid value for this
    *    type.
     */
   public static String fromStringForOptional(String string)
   throws TypeValueException {
       return string;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Text</code>.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Text() {
       super("_text", java.lang.String.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected Object fromStringImpl(String string) {
       return string;
    }
 
    public String toString(Object value)
   throws IllegalArgumentException, TypeValueException, ClassCastException {
       return fromStringForRequired((String) value);
    }
 }
