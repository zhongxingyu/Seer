 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
 * Item in a list or a set type.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  *
  * @see List
  */
 public class ItemList
 extends Object {
 
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
     * Creates a new <code>ItemList</code>.
     * The list will be able to contain several instances of an object.
     */
    public ItemList() {
       this(false);
    }
 
    /**
     * Creates a new <code>ItemList</code>.
     *
     * @param setType
     *    if <code>true</code> an object can be added only once in the list,
     *    if <code>false</code> an object can be added several times in the list.
     */
    public ItemList(boolean setType) {
       _list = new java.util.ArrayList(10);
       _setType = setType;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The list that contains the items. Cannot <code>null</code>.
     */
    private final java.util.List _list;
 
    /**
     * Indicates whether this list accepts equal objects.
     */
    private final boolean _setType;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Adds an item to the list. The item is added at the end of the list.
     *
     * @param value
     *    the value of the item to add in the list, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>value == null</code>.
     */
    protected final void addItem(Object value) throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("value", value);
 
       // A set can not have the same value twice
       if (_setType && _list.contains(value)) {
          return;
       }
       _list.add(value);
    }
 
    /**
     * Gets the item at the specified index as an <code>Object</code>.
     *
     * @param index
     *    the position of the required item, it should be >= 0 and < getSize().
     *
     * @return
     *    the item, not <code>null</code>.
     */
    protected final Object getItem(int index) {
       return _list.get(index);
    }
 
    /**
     * Gets the number of items included in the list.
     *
     * @return
     *    the size of the list.
     */
    public int getSize() {
       return _list.size();
    }
 }
