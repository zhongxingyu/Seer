 /*
  * $Id$
  */
 package org.xins.types;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Exception thrown to indicate a value is invalid for a certain type.
  *
  * @version $Revision$ $Date$
 * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class TypeValueException extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>TypeValueException</code>.
     *
     * @param type
     *    the type, not <code>null</code>.
     *
     * @param value
     *    the value, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null || value == null</code>.
     */
    public TypeValueException(Type type, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("type", type, "value", value);
 
       // Store the arguments
       _type  = type;
       _value = value;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The concerning parameter type. This field is never <code>null</code>.
     */
    private final Type _type;
 
    /**
     * The value that is considered invalid. This field is never <code>null</code>.
     */
    private final String _value;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Retrieves the type.
     *
     * @return
     *    the type, never <code>null</code>.
     */
    public final Type getType() {
       return _type;
    }
 
    /**
     * Retrieves the value that was considered invalid.
     *
     * @return
     *    the value that was considered invalid, not <code>null</code>.
     */
    public final String getValue() {
       return _value;
    }
 }
