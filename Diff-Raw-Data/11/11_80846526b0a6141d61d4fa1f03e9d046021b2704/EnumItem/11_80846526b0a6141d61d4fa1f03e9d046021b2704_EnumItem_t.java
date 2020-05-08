 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Item in an enumeration type.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  *
  * @see EnumType
  */
 public class EnumItem extends Object {
 
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
     * Creates a new <code>EnumItem</code>.
     *
     * @param name
     *    the symbolic (friendly) name for the enumeration value, not
     *    <code>null</code>.
     *
     * @param value
     *    the actual value of the enumeration item, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || value == null</code>.
     */
    public EnumItem(String name, String value)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("name", name, "value", value);
 
       _name  = name;
       _value = value;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The symbolic (friendly) name for the enumeration value. Cannot be
     * <code>null</code>.
     */
    private final String _name;
 
    /**
     * The actual value of this enumeration item. Cannot <code>null</code>.
     */
    private final String _value;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the symbolic (friendly) name for the enumeration value.
     *
     * @return
     *    the symbolic name, not <code>null</code>.
     */
    public final String getName() {
       return _name;
    }
 
    /**
     * Gets the value for this enumeration item.
     *
     * @return
     *    the actual value of this enumeration item, not <code>null</code>.
     */
    public String getValue() {
       return _value;
    }
 
    /**
     * Returns the hash code for this object.
     *
     * @return
     *    the hash code.
     */
    public int hashCode() {
       return (_name == null ? 0 : _name.hashCode()) ^
              (_value == null ? 0 : _value.hashCode());
    }
 
    /**
     * Checks if this object is considered equal to the specified object. In
     * order for it to qualify as equals, it needs to be an instance of the
     * very same class as this object (not even a subclass), and it needs to
     * have the same name and value.
     *
     * @param obj
     *    the object to compare, or <code>null</code>.
     *
     * @return
     *    <code>true</code> if <code>obj</code> is considered equal to this
     *    instance, or <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
 
      // Check null and the class
      if (obj == null || !(obj.getClass().equals(getClass()))) {
          return false;
       }
 
       // Check name and value
       EnumItem other = (EnumItem) obj;
       return (_name == null ? other.getName() == null : _name.equals(other.getName()))  &&
          (_value == null ? other.getValue() == null : _value.equals(other.getValue()));
    }
 
    /**
     * Returns a textual representation of this object.
     *
     * @return
     *    a textual representation of this object, never <code>null</code>.
     */
    public String toString() {
       return _value;
    }
 }
