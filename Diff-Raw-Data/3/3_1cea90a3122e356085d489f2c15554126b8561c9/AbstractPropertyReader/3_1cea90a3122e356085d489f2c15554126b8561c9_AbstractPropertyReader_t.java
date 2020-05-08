 /*
  * $Id$
  */
 package org.xins.common.collections;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Base for <code>PropertyReader</code> implementations that use an underlying
  * <code>Properties</code> object.
  *
  * @version $Revision$
 * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
 * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public abstract class AbstractPropertyReader
 implements PropertyReader {
 
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
     * Constructs a new <code>AbstractPropertyReader</code>.
     *
     * @param map
     *    the map containing the data of this <code>PropertyReader</code>,
     *    cannot be <code>null</code>.
     */
    public AbstractPropertyReader(Map map) {
       _properties = map;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The mappings from property keys to values.
     */
    private final Map _properties;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
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
       MandatoryArgumentChecker.check("name", name);
       Object value = _properties.get(name);
       return (String) value;
    }
 
    /**
     * Gets an iterator that iterates over all the property names. The
     * {@link Iterator} will return only {@link String} instances.
     *
     * @return
     *    the {@link Iterator} that will iterate over all the names, never
     *    <code>null</code>.
     */
    public Iterator getNames() {
       return _properties.keySet().iterator();
    }
 
    /**
     * Returns the number of entries.
     *
     * @return
     *    the size, always &gt;= 0.
     *
     * @since XINS 0.202
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
     */
    protected Map getPropertiesMap() {
       return _properties;
    }
 }
