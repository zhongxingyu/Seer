 /*
  * $Id: AbstractPropertyReader.java,v 1.27 2007/09/18 08:45:08 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.text.TextUtils;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ToXML;
 
 /**
  * Base for <code>PropertyReader</code> implementations that use an underlying
  * <code>Map</code> instance.
  *
  * <p>Since XINS 3.0, this class implements the {@link ToXML} interface.
  *
  * @version $Revision: 1.27 $ $Date: 2007/09/18 08:45:08 $
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.0.0
  */
 public abstract class AbstractPropertyReader
 extends Object
 implements PropertyReader, ToXML {
 
    /**
     * The mappings from property keys to values. Never <code>null</code>.
     */
    private final HashMap<String,String> _properties;
 
    /**
     * Constructs a new <code>AbstractPropertyReader</code>.
     *
     * @param map
     *    the map containing the data of this <code>PropertyReader</code>,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>map == null</code>
     *    or (since XINS 3.0) if the map contains a non-<code>value</code>
     *    key or a value that is not a {@link String} instance.
     *
     * @since XINS 1.4.0
     */
    protected AbstractPropertyReader(Map map)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("map", map);
 
       // Initialize fields
       _properties = new HashMap<String,String>();
       for (Object oEntry : map.entrySet()) {
          Map.Entry entry = (Map.Entry) oEntry;
          Object      key = entry.getKey();
          Object    value = entry.getValue();
 
          // Skip if either key or value is null
          if (key == null || value == null) {
             continue;
          }
 
          // Make sure key and value are String objects
          if (! (key instanceof String)) {
             throw new IllegalArgumentException("map contains a key of class " + key.getClass().getName() + ", while only java.lang.String is allowed.");
          } else if (! (value instanceof String)) {
             throw new IllegalArgumentException("map contains a value of class " + value.getClass().getName() + ", while only java.lang.String is allowed.");
          }
 
          // Add the entry
          _properties.put((String) key, (String) value);
       }
    }
 
    /**
     * Gets the value of the property with the specified name.
     *
     * @param name
     *    the name of the property, cannot be <code>null</code>.
     *
     * @return
     *    the value of the property, or <code>null</code> if it is not set.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String get(String name) throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       // Retrieve the value
       return _properties.get(name);
    }
 
    /**
     * Gets an iterator that iterates over all the property names. The
     * {@link Iterator} will return only {@link String} instances.
     *
     * @return
     *    the {@link Iterator} that will iterate over all the names, never
     *    <code>null</code>.
     *
     * @deprecated
     *    Since XINS 3.0. Use {@link #names()} instead, which is type-safe
     *    and supports the
     *    <a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/foreach.html">Java 5 foreach-operator</a>.
     */
    @Deprecated
   public Iterator<String> getNames() {
       return names().iterator();
    }
 
    /**
     * Gets all property names, as a <code>Collection</code> of
     * <code>String</code> objects.
     *
     * @return
     *    all property names, never <code>null</code>
     *    (but may be an empty {@link Collection}).
     *
     * @since XINS 3.0
     */
    public Collection<String> names() {
       return _properties.keySet();
    }
 
    /**
     * Returns the number of entries.
     *
     * @return
     *    the size, always &gt;= 0.
     */
    public int size() {
       return _properties.size();
    }
 
    /**
     * Returns the <code>Map</code> that contains the properties.
     *
     * @return
     *    the {@link Map} used to store the properties in, cannot be
     *    <code>null</code>.
     *
     * @since XINS 1.4.0
     */
    protected Map getPropertiesMap() {
       return _properties;
    }
 
    /**
     * Compares this object with the specified argument for equality.
     *
     * <p>The implementation of this method in class
     * <code>AbstractPropertyReader</code> uses
     * {@link PropertyReaderUtils#equals(PropertyReader,Object)}.
     *
     * @param obj
     *    the object to compare with, can be <code>null</code>.
     *
     * @return
     *    <code>true</code> if <code>this</code> and <code>obj</code> are considered equal,
     *    <code>false</code> if they are considered different.
     */
    public boolean equals(Object obj) {
       return PropertyReaderUtils.equals(this, obj);
    }
 
    /**
     * Returns a hash code value for this object.
     *
     * <p>The implementation of this method in class
     * <code>AbstractPropertyReader</code> uses
     * {@link PropertyReaderUtils#hashCode(PropertyReader)}.
     *
     * @return
     *    a hash code value for this object.
     */
    public int hashCode() {
       return PropertyReaderUtils.hashCode(this);
    }
 
    /**
     * Converts this object to an XML <code>Element</code>.
     *
     * @return
     *    this property set, as an XML {@link Element},
     *    never <code>null</code>.
     *
     * @since XINS 3.0
     */
    public Element toXML() {
       return PropertyReaderUtils.toXML(this);
    }
 
    /**
     * Returns a string representation of the object.
     *
     * @return
     *    a text representation of this object, as a character {@link String},
     *    never <code>null</code>.
     *
     * @since XINS 3.0
     */
    public String toString() {
       return PropertyReaderUtils.toString(this);
    }
 }
