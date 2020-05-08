 /*
  * $Id$
  */
 package org.xins.util.sd;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Descriptor for a group of services.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.105
  */
 public abstract class GroupDescriptor extends Descriptor {
 
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
     * Constructs a new <code>GroupDescriptor</code>. The members to be
     * included must be passed. The array of members may be empty, but it
     * cannot contain any <code>null</code> elements. It may contain
     * duplicates, though.
     *
     * @param members
     *    list of members of the group, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>members == null || members<em>n</em> == null</code> (where
     *    <code>0 &lt;= <em>n</em> &lt; members.length</code>).
     */
    GroupDescriptor(Descriptor[] members) {
 
       // Check preconditions
       MandatoryArgumentChecker.check("members", members);
       int count = members.length;
       for (int i = 0; i < count; i++) {
          Descriptor d = members[i];
          if (d == null) {
             throw new IllegalArgumentException("members[" + i + "] == null");
          }
       }
 
       // Store members
       _members = new Descriptor[count];
       System.arraycopy(members, 0, _members, 0, count);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The members of this group. Cannot be <code>null</code>.
     */
    private final Descriptor[] _members;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if this service descriptor denotes a group.
     *
     * @return
     *    <code>true</code>, since this descriptor denotes a group.
     */
    public boolean isGroup() {
       return true;
    }
 
    /**
     * Returns the members of this group.
     *
     * @return
     *    the members of this group as a new array, not <code>null</code>.
     */
    public Descriptor[] getMembers() {
       int count = _members.length;
       Descriptor[] array = new Descriptor[count];
      System.arraycopy(members, 0, _members, array, 0, count);
    }
 }
