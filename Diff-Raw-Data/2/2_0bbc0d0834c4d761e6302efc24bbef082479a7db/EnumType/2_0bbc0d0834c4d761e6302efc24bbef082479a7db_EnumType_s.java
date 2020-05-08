 /*
  * $Id$
  */
 package org.xins.types;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Enumeration type. An enumeration type only accepts a limited set of
  * possible values.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<A href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</A>)
  */
 public abstract class EnumType extends Type {
 
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
     * Creates a new <code>EnumType</code> instance. The name of the type needs
     * to be specified. The value class (see {@link Type#getValueClass()}) is
     * set to {@link String String.class}.
     *
     * <p />The items this type accepts should be passed. If
     * <code>items == null</code>, then this type will contain no items. This
     * is the same as passing a zero-size {@link EnumItem} array.
     *
     * <p />Note that the <code>items</code> array may contain
     * <code>null</code> values. These will be ignored.
     *
     * @param name
     *    the name of the type, not <code>null</code>.
     *
     * @param items
     *    the items for the type, or <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    protected EnumType(String name, EnumItem[] items)
    throws IllegalArgumentException {
       super(name, String.class);
 
       _namesToValues = new HashMap();
       _valuesToNames = new HashMap();
 
       // TODO: Use ArrayMap ?
 
       int count = items == null ? 0 : items.length;
       String[] values = new String[count];
       int actualItems = 0;
       for (int i = 0; i < count; i++) {
          EnumItem item = items[i];
          if (item != null) {
             String itemName  = item.getName();
             String itemValue = item.getValue();
 
             _namesToValues.put(itemName,  itemValue);
             _valuesToNames.put(itemValue, itemName);
             values[actualItems++] = itemValue;
          }
       }
 
       _values = new String[actualItems];
       System.arraycopy(values, 0, _values, 0, actualItems);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Map that links symbolic names to enumeration values.
     */
    private final Map _namesToValues;
 
    /**
     * Map that links enumeration values to their symbolic names.
     */
    private final Map _valuesToNames;
 
    /**
     * The list of accepted values.
     */
    private final String[] _values;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected final boolean isValidValueImpl(String value) {
       for (int i = 0; i < _values.length; i++) {
         if (_values.equals(value)) {
             return true;
          }
       }
       return false;
    }
 
    protected final Object fromStringImpl(String value) {
       return value;
    }
 
    public final String getByName(String name) {
       return (String) _namesToValues.get(name);
    }
 
    public final String getByValue(String value) {
       return (String) _valuesToNames.get(value);
    }
 }
