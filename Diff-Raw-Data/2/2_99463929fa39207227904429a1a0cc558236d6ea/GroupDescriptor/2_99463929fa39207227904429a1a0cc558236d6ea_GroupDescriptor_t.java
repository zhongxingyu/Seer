 // See the COPYRIGHT file for redistribution and use restrictions.
 package org.znerd.jcaller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 /**
  * Descriptor for a group of services. Each <code>GroupDescriptor</code> has
  * at least 2 members.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class GroupDescriptor extends Descriptor {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Serial version UID. Used for serialization.
     */
    private static final long serialVersionUID = -2945764325774744627L;
 
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
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>GroupDescriptor</code>. The members to be
     * included must be passed. The array of members cannot contain any
     * <code>null</code> elements. It may contain duplicates, though.
     *
     * <p>The array of members cannot be empty, but needs to contain at
     * least 2 descriptors.
     *
     * @param type
     *    the type of group, cannot be <code>null</code>.
     *
     * @param members
     *    list of members of the group, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type                == null
     *          || members             == null
     *          || members.length    &lt; 2
     *          || members[<em>n</em>] == null</code>
     *    (where <code>0 &lt;= <em>n</em> &lt; members.length</code>).
     */
    public GroupDescriptor(Type type, Descriptor[] members)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("type", type, "members", members);
       int size = members.length;
       if (size < 2) {
          throw new IllegalArgumentException("members.length (" + size + ") < 2");
       }
       for (int i = 0; i < size; i++) {
          Descriptor d = members[i];
          if (d == null) {
             throw new IllegalArgumentException("members[" + i + "] == null");
          }
       }
 
       // Store information
       _type    = type;
       _members = new Descriptor[size]; // TODO: Use ArrayUtils.clone(Object[])
       System.arraycopy(members, 0, _members, 0, size);
 
       // Recursively add all TargetDescriptor instances to the Map
       _targetsByCRC = initTargetsByCRC(members);
    }
 
    /**
     * Presents this object as a text string.
     *
     * @return
     *    this object as a {@link String}, never <code>null</code>.
     */
    @Override
    public String toString() {
       String s = _type._description + " group { " + _members[0];
       for (int i = 1; i < _members.length; i++) {
          s += "; " + _members[i];
       }
       s += " }";
       return s;
    }
 
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
 
    /**
     * Recursively adds all <code>TargetDescriptor</code> instances found in
     * the specified list of <code>Descriptor</code>'s to the internal map from
     * CRC-32 checksum to <code>TargetDescriptor</code>.
     *
     * @param members
     *    the set of {@link Descriptor} instances, cannot be <code>null</code>.
     *
     * @return
     *    a {@link Map} that maps from CRC checksum to
     *    {@link TargetDescriptor}, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>members == null</code>.
     */
    private Map<Integer,TargetDescriptor> initTargetsByCRC(Descriptor[] members)
    throws NullPointerException {
 
       Map<Integer,TargetDescriptor> map = new HashMap<Integer,TargetDescriptor>();
 
       for (Descriptor member : members) {
          for (TargetDescriptor target : member.targets()) {
             map.put(target.getCRC(), target);
          }
       }
 
       return map;
    }
 
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
    private final Map<Integer,TargetDescriptor> _targetsByCRC;
 
    /**
     * Checks if this descriptor denotes a group of descriptors.
     *
     * @return
     *    <code>true</code>, since this descriptor denotes a group.
     */
    @Override
    public boolean isGroup() {
       return true;
    }
 
    @Override
    public Collection<TargetDescriptor> targets() {
 
       // Create a copy of the set of TargetDescriptors
       List<TargetDescriptor> list = new ArrayList<TargetDescriptor>(_targetsByCRC.values());
 
       // Shuffle the list if appropriate
       if (RANDOM_TYPE.equals(_type)) {
          Collections.shuffle(list);
       }
 
       return list;
    }
 
    /**
     * Counts the total number of target descriptors in/under this descriptor.
     *
     * @return
     *    the total number of target descriptors, always &gt;= 2.
     */
    @Override
    public int getTargetCount() {
       return _targetsByCRC.size();
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
    @Override
    public TargetDescriptor getTargetByCRC(int crc) {
      return _targetsByCRC.get(new Integer(crc));
    }
 
    /**
     * Type of a group.
     *
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     */
    public static final class Type implements Serializable {
 
       /**
        * Serial version UID. Used for serialization.
        */
       private static final long serialVersionUID = -3418705789523454836L;
 
       /**
        * Constructs a new <code>Type</code> with the specified description.
        *
        * @param description
        *    the description for this type.
        */
       Type(String description) {
          _description = description;
       }
 
       /**
        * The description for this type.
        */
       private final String _description;
 
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
 }
