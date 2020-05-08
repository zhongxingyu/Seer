 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 /**
  * Descriptor for a group of services. Each <code>GroupDescriptor</code> has
  * at least 2 members.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class GroupDescriptor extends Descriptor {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = GroupDescriptor.class.getName();
 
    /**
     * The fully-qualified name of this inner class <code>Type</code>.
     */
    private static final String TYPE_CLASSNAME = GroupDescriptor.Type.class.getName();
 
    /**
     * The fully-qualified name of the inner class <code>RandomIterator</code>.
     */
    private static final String RANDOM_ITERATOR_CLASSNAME = GroupDescriptor.RandomIterator.class.getName();
 
    /**
     * The fully-qualified name of this inner class
     * <code>OrderedIterator</code>.
     */
    private static final String ORDERED_ITERATOR_CLASSNAME = GroupDescriptor.OrderedIterator.class.getName();
 
    /**
     * The identifier of the <em>random</em> group type.
     */
    public static final String RANDOM_TYPE_ID = "random";
 
    /**
     * The identifier of the <em>ordered</em> group type.
     */
    public static final String ORDERED_TYPE_ID = "ordered";
 
    /**
     * The <em>random</em> group type.
     */
    public static final Type RANDOM_TYPE = new Type(RANDOM_TYPE_ID);
 
    /**
     * The <em>ordered</em> group type.
     */
    public static final Type ORDERED_TYPE = new Type(ORDERED_TYPE_ID);
 
    /**
     * Pseudo-random number generator.
     */
    private static final Random RANDOM = new Random();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Gets a group type by identifier.
     *
     * @param identifier
     *    the identifier for the group, cannot be <code>null</code>.
     *
     * @return
     *    the type with the specified identifier, or <code>null</code> if there
     *    is no matching type.
     *
     * @throws IllegalArgumentException
     *    if <code>identifier == null</code>.
     */
    public static Type getType(String identifier)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("identifier", identifier);
 
       // Match
       if (RANDOM_TYPE_ID.equals(identifier)) {
          return RANDOM_TYPE;
       } else if (ORDERED_TYPE_ID.equals(identifier)) {
          return ORDERED_TYPE;
       } else {
          return null;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>GroupDescriptor</code>. The members to be
     * included must be passed. The array of members cannot contain any
     * <code>null</code> elements. It may contain duplicates, though.
     *
     * <p>Since XINS 1.1.0, the array of members cannot be empty, but needs to
     * contain at least 2 descriptors.
     *
     * @param type
     *    the type of group, cannot be <code>null</code>.
     *
     * @param members
     *    list of members of the group, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null
     *          || members == null
     *          || members.length &lt; 2
     *          || members[<em>n</em>] == null</code>
     *    (where <code>0 &lt;= <em>n</em> &lt; members.length</code>).
     */
    public GroupDescriptor(Type type, Descriptor[] members)
    throws IllegalArgumentException {
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("type", type, "members", members);
       int size = members.length;
       for (int i = 0; i < size; i++) {
          Descriptor d = members[i];
          if (d == null) {
             throw new IllegalArgumentException("members[" + i + "] == null");
          }
       }
 
       // Store information
       _type    = type;
       _members = new Descriptor[size];
       System.arraycopy(members, 0, _members, 0, size);
 
       // Recursively add all TargetDescriptor instances to the Map
       _targetsByCRC = new HashMap();
       addTargetsByCRC(members);
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, null);
    }
 
    /**
     * Recursively adds all <code>TargetDescriptor</code> instances found in
     * the specified list of <code>Descriptor</code>'s to the internal map from
     * CRC-32 checksum to <code>TargetDescriptor</code>.
     *
     * @param members
     *    the set of {@link Descriptor} instances, cannot be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>members == null || <em>group</em>.</code>{@link #_members}<code> == null</code>,
     *    where <em>group</em> is any {@link GroupDescriptor} instance found in
     *    <code>members</code> (at any level).
     */
    private final void addTargetsByCRC(Descriptor[] members)
    throws NullPointerException {
 
       int size = members.length;
       for (int i = 0; i < size; i++) {
          Descriptor d = members[i];
 
          // If this is a TargetDescriptor, put it in the map
          if (d instanceof TargetDescriptor) {
             TargetDescriptor target = (TargetDescriptor) d;
             _targetsByCRC.put(new Integer(target.getCRC()), target);
             _targetCount++;
 
          // Otherwise it is assumed to be a GroupDescriptor, recurse
          } else {
             GroupDescriptor group = (GroupDescriptor) d;
             addTargetsByCRC(group._members);
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The type of this group. Cannot be <code>null</code>.
     */
    private final Type _type;
 
    /**
     * The members of this group. Cannot be <code>null</code>.
     */
    private final Descriptor[] _members;
 
    /**
     * All contained <code>TargetDescriptor</code> instances, by CRC-32. This
     * {@link Map} is used by {@link #getTargetByCRC(int)} to lookup a
     * {@link TargetDescriptor} by CRC-32 checksum.
     *
     * <p>This field is initialized by the constructor and can never be
     * <code>null</code>.
     */
    private final Map _targetsByCRC;
 
    /**
     * The total number of targets in this group. The value of this field is
     * always &gt;= 1.
     */
    private int _targetCount;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if this descriptor denotes a group of descriptors.
     *
     * @return
     *    <code>true</code>, since this descriptor denotes a group.
     */
    public boolean isGroup() {
       return true;
    }
 
    /**
     * Iterates over all leaves, the target descriptors.
     *
     * <p>The returned {@link Iterator} will not support
     * {@link Iterator#remove()}. The iterator will only return
     * {@link TargetDescriptor} instances, no instances of other classes and
     * no <code>null</code> values.
     *
     * <p>Also, this iterator is guaranteed to return {@link #getTargetCount()}
     * instances of class {@link TargetDescriptor}.
     *
     * @return
     *    iterator over the leaves, the target descriptors, in this
     *    descriptor, in the correct order, never <code>null</code>.
     */
    public Iterator iterateTargets() {
 
       final String THIS_METHOD = "iterateTargets()";
 
       if (_type == RANDOM_TYPE) {
          return new RandomIterator();
       } else if (_type == ORDERED_TYPE) {
          return new OrderedIterator();
       } else {
          final String SUBJECT_CLASS  = CLASSNAME;
          final String SUBJECT_METHOD = THIS_METHOD;
          final String DETAIL         = "Unknown type: " + _type + '.';
          throw Utils.logProgrammingError(CLASSNAME, THIS_METHOD, SUBJECT_CLASS, SUBJECT_METHOD, DETAIL);
       }
    }
 
    /**
     * Counts the total number of target descriptors in/under this descriptor.
     *
     * @return
     *    the total number of target descriptors, always &gt;= 2.
     */
    public int getTargetCount() {
       return _targetCount;
    }
 
    /**
     * Returns the type of this group.
     *
     * @return
     *    the type of this group, not <code>null</code>.
     */
    public Type getType() {
       return _type;
    }
 
    /**
     * Returns the members of this group.
     *
     * @return
     *    the members of this group as a new array, not <code>null</code>.
     */
    public Descriptor[] getMembers() {
       int size = _members.length;
       Descriptor[] array = new Descriptor[size];
       System.arraycopy(_members, 0, array, 0, size);
       return array;
    }
 
    /**
     * Returns the <code>TargetDescriptor</code> that matches the specified
     * CRC-32 checksum.
     *
     * @param crc
     *    the CRC-32 checksum.
     *
     * @return
     *    the {@link TargetDescriptor} that matches the specified checksum, or
     *    <code>null</code>, if none could be found in this descriptor.
     */
    public TargetDescriptor getTargetByCRC(int crc) {
       return (TargetDescriptor) _targetsByCRC.get(new Integer(crc));
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Type of a group.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    public static final class Type extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Type</code> with the specified description.
        *
        * @param description
        *    the description for this type.
        */
       private Type(String description) {
 
          // TRACE: Enter constructor
          Log.log_1000(TYPE_CLASSNAME, null);
 
          _description = description;
 
          // TRACE: Leave constructor
          Log.log_1002(TYPE_CLASSNAME, null);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The description for this type.
        */
       private final String _description;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns a textual representation of this object.
        *
        * <p>The implementation of this method returns the description for this
        * type. However, this is not guaranteed to remain like this.
        *
        * @return
        *    a textual representation of this object, never <code>null</code>.
        */
       public String toString() {
          return _description;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Random iterator over the leaf target descriptors contained in this
     * group descriptor. Needed for the implementation of
     * {@link #iterateTargets()}.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private final class RandomIterator
    extends Object
    implements Iterator {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>RandomIterator</code>.
        */
       private RandomIterator() {
 
          // TRACE: Enter constructor
          Log.log_1000(RANDOM_ITERATOR_CLASSNAME, null);
 
          // Copy all members to _remaining
          int size = _members.length;
          _remaining = new ArrayList(size);
          for (int i = 0; i < size; i++) {
             _remaining.add(_members[i]);
          }
 
          // Pick a member randomly
          int index = Math.abs(RANDOM.nextInt() % size);
          Descriptor member = (Descriptor) _remaining.remove(index);
 
          // Initialize the current iterator to link to that member's services
          _currentIterator = member.iterateTargets();
 
          // TRACE: Leave constructor
          Log.log_1002(RANDOM_ITERATOR_CLASSNAME, null);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The set of remaining descriptors. One is removed from a random index
        * each time {@link #next()} is called.
        *
        * <p>This field will be set to <code>null</code> as soon as there are
        * no more remaining members. Still {@link #_currentIterator} could have
        * more elements.
        */
       private List _remaining;
 
       /**
        * Current iterator of one of the members.
        *
        * <p>This field will be set to <code>null</code> as soon as there are
        * no more remaining services to be iterated over.
        */
       private Iterator _currentIterator;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Checks if there is a next element.
        *
        * @return
        *    <code>true</code> if there is a next element, <code>false</code>
        *    if there is not.
        */
       public boolean hasNext() {
          return (_currentIterator != null);
       }
 
       /**
        * Returns the next element.
        *
        * @return
        *    the next element, never <code>null</code>.
        *
        * @throws NoSuchElementException
        *    if there is no new element.
        */
       public Object next() throws NoSuchElementException {
 
          // Check preconditions
          if (_currentIterator == null) {
             throw new NoSuchElementException();
          }
 
          // Get the next service
          Object o = _currentIterator.next();
 
          // Check if this member/iterator has any more
          if (! _currentIterator.hasNext()) {
 
             // If there are no remaining members, set _currentIterator to null
             if (_remaining == null) {
                _currentIterator = null;
 
             } else {
                // Pick one of the remaining members
                int size = _remaining.size();
                int index = (size == 1) ? 0 : Math.abs(RANDOM.nextInt() % size);
                Descriptor member = (Descriptor) _remaining.remove(index);
                _currentIterator = member.iterateTargets();
 
                // If there are now no additional remaining members, set
                // _remaining to null
                if (size == 1) {
                   _remaining = null;
                }
             }
          }
 
          return o;
       }
 
       /**
        * Removes the element last returned by <code>next()</code> (unsupported
        * operation).
        *
        * @throws UnsupportedOperationException
        *    always thrown, since this operation is unsupported.
        */
       public void remove() throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
       }
    }
 
    /**
     * Ordered iterator over the leaf target descriptors contained in this
     * group descriptor. Needed for the implementation of
     * {@link #iterateTargets()}.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private final class OrderedIterator
    extends Object
    implements Iterator {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>OrderedIterator</code>.
        */
       private OrderedIterator() {
 
          // TRACE: Enter constructor
          Log.log_1000(ORDERED_ITERATOR_CLASSNAME, null);
 
          // Copy all members to _remaining
          _currentIndex = 0;
 
          // Initialize the current iterator to link to that member's services
          _currentIterator = _members[0].iterateTargets();
 
          // TRACE: Leave constructor
          Log.log_1002(ORDERED_ITERATOR_CLASSNAME, null);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The current index into the list of members. Will be set to a negative
        * value if there are no more members.
        */
       private int _currentIndex;
 
       /**
        * Current iterator of one of the members.
        *
        * <p>This field will be set to <code>null</code> as soon as there are
        * no more remaining services to be iterated over.
        */
       private Iterator _currentIterator;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Checks if there is a next element.
        *
        * @return
        *    <code>true</code> if there is a next element, <code>false</code>
        *    if there is not.
        */
       public boolean hasNext() {
          return (_currentIterator != null) && _currentIterator.hasNext();
       }
 
       /**
        * Returns the next element.
        *
        * @return
        *    the next element, never <code>null</code>.
        *
        * @throws NoSuchElementException
        *    if there is no new element.
        */
       public Object next() throws NoSuchElementException {
 
          // Check preconditions
          if (_currentIterator == null) {
             throw new NoSuchElementException();
          }
 
          // Get the next service
          Object o = _currentIterator.next();
 
          // Check if this member/iterator has any more
          if (! _currentIterator.hasNext()) {
 
             // If there are no remaining members, set _currentIterator to null
             if (_currentIndex < 0) {
                _currentIterator = null;
 
             } else {
                _currentIndex++;
 
                if (_currentIndex < _members.length) {
                   _currentIterator = _members[_currentIndex].iterateTargets();
                } else {
                   _currentIndex = -1;
                }
             }
          }
 
          return o;
       }
 
       /**
        * Removes the element last returned by <code>next()</code> (unsupported
        * operation).
        *
        * @throws UnsupportedOperationException
        *    always thrown, since this operation is unsupported.
        */
       public void remove() throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
       }
    }
 }
