 /*
  * $Id$
  *
  * Copyright 2003-2008 Online Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types.standard;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.types.ItemList;
 import org.xins.common.types.TypeValueException;
 
 /**
  * Standard type <em>_list</em>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.5.0.
  */
 public final class List extends org.xins.common.types.List {
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public static final List SINGLETON = new List();
 
    /**
     * Constructs a new <code>List</code>.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private List() {
       super("_list", Text.SINGLETON);
    }
 
    public ItemList createList() {
       return new Value();
    }
 
    /**
     * Constructs a <code>List.Value</code> from the specified string
     * which is guaranteed to be non-<code>null</code>.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @return
     *    the {@link List.Value} object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForRequired(String string)
    throws IllegalArgumentException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("string", string);
 
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Constructs a <code>List.Value</code> from the specified string.
     *
     * @param string
     *    the string to convert, can be <code>null</code>.
     *
     * @return
     *    the {@link List.Value}, or <code>null</code> if
     *    <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForOptional(String string)
    throws TypeValueException {
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Converts the specified <code>List.Value</code> to a string.
     *
     * @param value
     *    the value to convert, can be <code>null</code>.
     *
     * @return
     *    the textual representation of the value, or <code>null</code> if and
     *    only if <code>value == null</code>.
     */
    public static String toString(Value value) {
 
       // Short-circuit if the argument is null
       if (value == null) {
          return null;
       }
       return SINGLETON.toString((ItemList) value);
    }
 
    public String getDescription() {
       return "An ampersand separated list of text.";
    }
 
    /**
     * Inner class that represents a list of String.
     */
    public static final class Value extends ItemList {
 
       /**
        * Add a new element in the list.
        *
        * @param value
        *    the new value to add, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>value == null</code>.
        */
       public void add(String value) throws IllegalArgumentException {
          MandatoryArgumentChecker.check("value", value);
          addItem(value);
       }
 
       /**
        * Get an element from the list.
        *
        * @param index
        *    The position of the required element.
        *
        * @return
        *    The element at the specified position, cannot be <code>null</code>.
        */
       public String get(int index) {
          return (String) getItem(index);
       }

      /**
       * Converts this <code>List.Value</code> to a string.
       *
       * @return
       *    the textual representation of the value.
       */
      public String toString() {
         return SINGLETON.toString((ItemList) this);
      }
    }
 }
