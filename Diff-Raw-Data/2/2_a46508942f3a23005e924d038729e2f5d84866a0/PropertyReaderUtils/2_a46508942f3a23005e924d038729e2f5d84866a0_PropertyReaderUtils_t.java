 /*
  * $Id$
  */
 package org.xins.common.collections;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Utility functions for dealing with <code>PropertyReader</code> objects.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public final class PropertyReaderUtils
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
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
    public static final boolean getBooleanProperty(PropertyReader properties,
                                                   String         propertyName,
                                                   boolean        fallbackDefault)
    throws IllegalArgumentException,
           InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       // Query the PropertyReader
       String value = properties.get(propertyName);
 
       // Fallback to the default, if necessary
       if (value == null || value.length() == 0) {
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
     * Gets the property with the specified name and converts it to a
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
    * @throws InvalidPropertyValueException
     *    if the conversion to an <code>int</code> failed.
     */
    public static final int getIntProperty(PropertyReader properties,
                                           String         propertyName)
    throws IllegalArgumentException,
           InvalidPropertyValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);
 
       // Query the PropertyReader
       String value = properties.get(propertyName);
 
       // Parse the string
       try {
          return Integer.parseInt(value);
       } catch (NumberFormatException exception) {
          throw new InvalidPropertyValueException(propertyName, value);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>PropertyReaderUtils</code> object. This
     * constructor is marked as <code>private</code>, since no objects of this
     * class should be constructed.
     */
    private PropertyReaderUtils() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
