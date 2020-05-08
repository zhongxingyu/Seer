 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package edu.rpi.cmt.access;
 
 import edu.rpi.sss.util.ObjectPool;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /** Object to represent an ace for a calendar entity or service.
  *
  * <p>The compareTo method orders the Aces according to the notWho, whoType and
  * who components. It does not take the actual privileges into account. There
  * should only be one entry per the above combination and the latest one on the
  * path should stand.
  *
  *  @author Mike Douglass   douglm@rpi.edu
  */
 public class Ace implements PrivilegeDefs, WhoDefs, Comparable<Ace> {
   boolean debug;
 
   private AceWho who;
 
   /** allowed/denied/undefined indexed by Privilege index
    */
   private PrivilegeSet how;
 
   /** Privilege objects defining the access. Used when manipulating acls
    */
   Collection<Privilege> privs;
 
   private boolean inherited;
 
   private String inheritedFrom;
 
   private static ObjectPool<String> inheritedFroms = new ObjectPool<String>();
 
   /** Constructor
    */
   public Ace() {
     this(false);
   }
 
   /** Constructor
    *
    * @param debug
    */
   public Ace(boolean debug) {
     this.debug = debug;
   }
 
   /** Constructor
    *
    * @param who
    * @param notWho
    * @param whoType
    * @param p
    */
   public Ace(String who,
              boolean notWho,
              int whoType,
              Privilege p) {
     this.who = AceWho.getAceWho(who, whoType, notWho);
     //addPriv(p);
     getPrivs().add(p);
     setHow(PrivilegeSet.makePrivileges(p));
   }
 
   /** Constructor
    *
    * @param who
    * @param notWho
    * @param whoType
    */
   public Ace(String who,
              boolean notWho,
              int whoType) {
     this.who = AceWho.getAceWho(who, whoType, notWho);
   }
 
   /** Set who this entry is for
    *
    * @param val
    */
   public void setWho(AceWho val) {
     who = val;
   }
 
   /** Get who this entry is for
    *
    * @return AceWho who
    */
   public AceWho getWho() {
     return who;
   }
 
   /**
    * @param val PrivilegeSet of allowed/denied/undefined indexed by Privilege index
    */
   public void setHow(PrivilegeSet val) {
     how = val;
   }
 
   /**
    *
    * @return PrivilegeSet array of allowed/denied/undefined indexed by Privilege index
    */
   public PrivilegeSet getHow() {
     if (how == null) {
       how = new PrivilegeSet();
     }
 
     return how;
   }
 
   /**
    *
    * @param val Collection of Privilege objects defining the access. Used when manipulating acls
    */
   public void setPrivs(Collection<Privilege> val) {
     privs = val;
   }
 
   /**
    *
    * @return Collection of Privilege objects defining the access. Used when manipulating acls
    */
   public Collection<Privilege> getPrivs() {
     if (privs == null) {
       privs = new ArrayList<Privilege>();
     }
     return privs;
   }
 
   /**
    *
    * @param val Privilege to add to Collection
    */
   public void addPriv(Privilege val) {
     getPrivs().add(val);
     //getHow().setPrivilege(val);
     setHow(PrivilegeSet.addPrivilege(getHow(), val));
   }
 
   /** An ace is inherited if it is merged in from further up the path.
    *
    * @param val
    */
   public void setInherited(boolean val) {
     inherited = val;
   }
 
   /**
    * @return boolean
    */
   public boolean getInherited() {
     return inherited;
   }
 
   /** Path defining from where we inherited access.
    *
    * @param val
    */
   public void setInheritedFrom(String val) {
     inheritedFrom = inheritedFroms.get(val);
   }
 
   /**
    * @return String
    */
   public String getInheritedFrom() {
     return inheritedFrom;
   }
 
   /** Return the merged privileges for all aces which match the name and whoType.
    *
    * @param acl
    * @param name
    * @param whoType
    * @return PrivilegeSet    merged privileges if we find a match else null
    * @throws AccessException
    */
   public static PrivilegeSet findMergedPrivilege(Acl acl,
                                                  String name,
                                                  int whoType) throws AccessException {
     PrivilegeSet privileges = null;
     for (Ace ace: acl.getAces()) {
       if ((whoType == ace.who.getWhoType()) &&
           ((whoType == AceWho.whoTypeUnauthenticated) ||
            (whoType == AceWho.whoTypeOwner) ||
             ace.getWho().whoMatch(name))) {
         privileges = PrivilegeSet.mergePrivileges(privileges, ace.getHow(),
                                                   ace.getInherited());
       }
     }
 
     return privileges;
   }
 
   /* ====================================================================
    *                 Decoding methods
    * ==================================================================== */
 
   /** Skip over an ace
    *
    * @param acl
    * @throws AccessException
    */
   public void skip(EncodedAcl acl) throws AccessException {
     acl.skipString();
     while (acl.getChar() != ' ') {
     }
   }
 
   /* * Search through the acl for a who that matches the given name and type..
    *
    * <p>That is, if we have a whoFlag and the String matches or a
    * notWhoFlag and the string does not match then return the length of
    * the entire 'who' section.
    *
    * <p>NOTE - I'm not sure of some of the semantics of the NOT thing. It's
    * pretty clear that matching a user is more specific than matching a
    * group but what's the inverted meaning?
    *
    * <p>If getPrivileges is true the Collection of privilege objects
    * defining the ace will be returned. This is needed for acl
    * manipulation rather than evaluation.
    *
    * @param acl
    * @param getPrivileges
    * @param name
    * @param whoType
    * @return boolean true if we find a match
    * @throws AccessException
    * /
   public boolean decode(EncodedAcl acl,
                         boolean getPrivileges,
                         String name, int whoType) throws AccessException {
     acl.rewind();
 
     while (acl.hasMore()) {
       getWho().decode(acl);
 
       if ((whoType != who.getWhoType()) || !who.whoMatch(name)) {
         skipHow(acl);
       } else {
         decodeHow(acl, getPrivileges);
         return true;
       }
     }
 
     return false;
   }
   */
 
   /** Get the next ace in the acl.
    *
    * <p>If .getPrivileges is true the Collection of privilege objects
    * defining the ace will be returned. This is needed for acl
    * manipulation rather than evaluation.
    *
    * @param acl
    * @param getPrivileges
    * @throws AccessException
    */
   public void decode(EncodedAcl acl,
                      boolean getPrivileges) throws AccessException {
     who = AceWho.decode(acl);
     decodeHow(acl, getPrivileges);
   }
 
   /* ====================================================================
    *                 Encoding methods
    * ==================================================================== */
 
   /** Encode this object as a sequence of char. privs must have been set.
    *
    * @param acl   EncodedAcl
    * @throws AccessException
    */
   public void encode(EncodedAcl acl) throws AccessException {
     getWho().encode(acl);
 
     for (Privilege p: privs) {
       p.encode(acl);
     }
 
     if (inherited) {
       acl.addChar(PrivilegeDefs.inheritedFlag);
       acl.encodeString(inheritedFrom);
     }
 
     acl.addChar(' ');  // terminate privs.
   }
 
   /** Provide a string representation for user display - this should probably
    * use a localized resource and be part of a display level. It also requires
    * the Privilege objects
    *
    * @return String representation
    */
   public String toUserString() {
     StringBuilder sb = new StringBuilder("(");
 
     sb.append(getWho().toUserString());
     sb.append(" ");
 
     for (Privilege p: privs) {
       sb.append(p.toUserString());
       sb.append(" ");
     }
     sb.append(")");
 
     return sb.toString();
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   public int compareTo(Ace that) {
     if (this == that) {
       return 0;
     }
 
     int res = getWho().compareTo(that.getWho());
     if (res == 0) {
       res = getHow().compareTo(that.getHow());
     }
 
     return res;
   }
 
   public int hashCode() {
     int hc = 7;
 
     if (who != null) {
       hc *= who.hashCode();
     }
 
     return hc *= getHow().hashCode();
   }
 
   public boolean equals(Object o) {
     return compareTo((Ace)o) == 0;
   }
 
   public String toString() {
     StringBuilder sb = new StringBuilder();
 
     sb.append("Ace{");
     sb.append(getWho().toString());
     if (how != null) {
       sb.append(", how=");
       sb.append(how);
     }
 
     if (getInherited()) {
       sb.append(", inherited from \"");
       sb.append(getInheritedFrom());
       sb.append("\"");
     }
 
     sb.append(", \nprivs=[");
 
     for (Privilege p: privs) {
       sb.append(p.toString());
       sb.append("\n");
     }
     sb.append("]");
 
     sb.append("}");
 
     return sb.toString();
   }
 
   /*
   private void skipHow(EncodedAcl acl) throws AccessException {
     Privileges.skip(acl);
   }
   */
 
   private void decodeHow(EncodedAcl acl,
                          boolean getPrivileges) throws AccessException {
     int pos = acl.getPos();
     setHow(PrivilegeSet.fromEncoding(acl));
     if (getPrivileges) {
       acl.setPos(pos);
       setPrivs(Privileges.getPrivs(acl));
     }
 
     // See if we got an inherited flag
     acl.back();
     if (acl.getChar() == PrivilegeDefs.inheritedFlag) {
       inherited = true;
       inheritedFrom = acl.getString();
       if (acl.getChar() != ' ') {
         throw new AccessException("malformedAcl");
       }
     }
   }
 }
 
