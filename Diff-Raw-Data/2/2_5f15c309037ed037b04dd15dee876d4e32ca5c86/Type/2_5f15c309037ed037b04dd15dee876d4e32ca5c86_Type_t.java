 /*
  * $Id$
  */
 package org.xins.types;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Type for a function parameter.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<A href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</A>)
  */
 public abstract class Type extends Object {
 
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
     * Creates a new <code>Type</code> instance. Both the name of the type and
     * the value class must be specified. The value class in the class (or
     * interface) that values for this type should be instances of. If
     * <code>null</code> is specified as the value class, then that is the same
     * as specifying <code>Object.class</code> as the value class.
     *
     * @param name
     *    the name of the type, not <code>null</code>.
     *
     * @param valueClass
     *    the class or interface that values should be instances of, or
     *    <code>null</code> if any class is valid.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    protected Type(String name, Class valueClass)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       // Store the arguments
       _name       = name;
       _valueClass = valueClass == null ? Object.class : valueClass;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this type. Never <code>null</code>.
     */
    private final String _name;
 
    /**
     * The class for all values. Never <code>null</code>.
     */
    private final Class _valueClass;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Retrieves the name of this type.
     *
     * @return
     *    the name of this type, never <code>null</code>.
     */
    public final String getName() {
       return _name;
    }
 
    /**
     * Retrieves the value class. All values for this type are instances of
     * this class.
     *
     * @return
     *    the class values should be instances of, never <code>null</code>.
     */
    public final Class getValueClass() {
       return _valueClass;
    }
 
    /**
     * Checks if the specified value is valid for this type.
     *
     * <p />This method first checks if <code>value == null</code> and if it is
     * not, then it returns the result of a call to
     * {@link #isValidValueImpl(String)}. Note that <code>null</code> values are
     * <em>always</em> considered to be valid.
     *
     * @param value
     *    the value that should be checked for validity, can be
     *    <code>null</code>.
     *
     * @return
     *    <code>true</code> if and only if the specified value is valid,
     *    <code>false</code> otherwise.
     */
    public final boolean isValidValue(String value) {
       if (value == null) {
          return true;
       } else {
          return isValidValueImpl(value);
       }
    }
 
    /**
     * Actually checks if the specified value is valid for this type. This
     * method is called from {@link #isValidValue(String)}. It is guaranteed that
     * the argument is not <code>null</code>.
     *
     * <p />The implementation of this method in class {@link Type} returns
     * <code>true</code>.
     *
     * @param value
     *    the value that should be checked for validity, never
     *    <code>null</code>.
     *
     * @return
     *    <code>true</code> if and only if the specified value is valid,
     *    <code>false</code> otherwise.
     */
    protected boolean isValidValueImpl(String value) {
       return true;
    }
 
    /**
     * Converts from a <code>String</code> to an instance of the value class
     * for this type.
     *
     * <p />This method returns <code>null</code> if <code>string ==
     * null</code>. Otherwise it first calls {@link #isValidValueImpl(String)}
     * to check if the value is in principle valid. If it is, then
     * {@link #fromStringImpl(String)} is called. If the result of that call is
     * <em>not</em> an instance of the value class, then an
     * {@link InternalError} is thrown. Notice that this error is also thrown
     * if {@link #fromStringImpl(String)} returns <code>null</code>.
     *
     * @param string
     *    the string to convert to an instance of the value class, can be
     *    <code>null</code>.
     *
     * @return
     *    an instance of the value class, will be <code>null</code> if and only
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     *
     * @throws InternalError
     *    if <code>fromStringImpl(string) == null
     *          || !getValueClass().isInstance(fromString(string))</code>.
     */
    public final Object fromString(String string)
    throws TypeValueException, InternalError {
 
       if (string == null) {
          return null;
       }
 
       if (!isValidValueImpl(string)) {
          throw new TypeValueException(this, string);
       }
 
       Object value = fromStringImpl(string);
 
       if (_valueClass.isInstance(value) == false) {
          throw new InternalError("The specified value returned by " + getClass().getName() + " is not an instance of " + _valueClass.getName() + '.');
       }
 
       return value;
    }
 
    /**
     * Converts from a <code>String</code> to an instance of the value class
     * for this type (actual implementation method).
     *
     * @param string
     *    the string to convert to an instance of the value class, guaranteed
     *    to be not <code>null</code> and guaranteed to have been passed to
     *    {@link #isValidValueImpl(String)} without getting an exception.
     *
     * @return
     *    an instance of the value class, cannot be <code>null</code>.
     */
   protected abstract Object fromStringImpl(String string);
 
    public final String toString() {
       return _name;
    }
 }
