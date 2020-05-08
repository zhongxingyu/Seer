 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cmt.access;
 
 import edu.rpi.cmt.access.Access.AccessCb;
 import edu.rpi.cmt.access.Access.AccessStatsEntry;
 import edu.rpi.sss.util.ObjectPool;
 import edu.rpi.sss.util.Util;
 
 import org.apache.log4j.Logger;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.TreeMap;
 
 /** Immutable object to represent an acl for a calendar entity or service.
  *
  * <p>The objects represented by Privileges will assume transient states
  * during processing.
  *
  * <p>An ACL is a set of ACEs which are stored as an encoded character
  * array. These aces should be sorted to facilitate merging and to
  * allow us to possibly only process as much of the acl as is necessary.
  *
  * <p>For example, owner access should come first, it's first in the test and
  * we can avoid decoding an ace which doesn't include any owner access.
  *
  * <p>The whoTypexxx declarations in Ace define the order of Ace types. In
  * addition, any aces that contain names should be in ascending alphabetic
  * order.
  *
  * <p>In the list of Ace there can only be one entry per AceWho so we can
  * represent the list as a SortedMap. Replacement then becomes easy.
  *
  *  @author Mike Douglass   douglm - rpi.edu
  */
 public class Acl extends EncodedAcl implements PrivilegeDefs {
   static boolean debug;
 
   private TreeMap<AceWho, Ace> aces;
 
   private static ObjectPool<PrivilegeSet> privSets = new ObjectPool<PrivilegeSet>();
 
   private static boolean usePool = false;
 
   private static AccessStatsEntry evaluations =
     new AccessStatsEntry("evaluations");
 
   /** Create a new Acl
    *
    * @param aces
    */
   public Acl(final Collection<Ace> aces) {
     debug = getLog().isDebugEnabled();
 
     this.aces = new TreeMap<AceWho, Ace>();
 
     for (Ace ace: aces) {
       this.aces.put(ace.getWho(), ace);
     }
   }
 
   /** Get the access statistics
    *
    * @return Collection of stats
    */
   public static Collection<AccessStatsEntry> getStatistics() {
     Collection<AccessStatsEntry> stats = new ArrayList<AccessStatsEntry>();
 
     stats.add(evaluations);
     stats.addAll(Ace.getStatistics());
     stats.addAll(EvaluatedAccessCache.getStatistics());
 
     return stats;
   }
 
   /** Immutable object created as a result of evaluating access to an entity for
    * a principal
    */
   public static class CurrentAccess implements Serializable,
                                                Comparable<CurrentAccess> {
     /** The Acl used to evaluate the access. We should not necessarily
      * make this available to the client.
      */
     private Acl acl;
 
     private char[] aclChars;
 
     private PrivilegeSet privileges = null;
 
     /** Was it succesful */
     private boolean accessAllowed;
 
     /**
      *
      */
     public CurrentAccess() {
     }
 
     /**
      * @param privs
      */
     public CurrentAccess(final PrivilegeSet privs) {
       privileges = privs;
     }
 
     /**
      * @param accessAllowed
      */
     public CurrentAccess(final boolean accessAllowed) {
       this.accessAllowed = accessAllowed;
     }
 
     /** The Acl used to evaluate the access. We should not necessarily
      * make this available to the client.
      *
      * @return acl
      */
     public Acl getAcl() {
       return acl;
     }
 
     /**  Allowed access for each privilege type
      * @see PrivilegeDefs
      *
      * @return privileges
      */
     public PrivilegeSet getPrivileges() {
       return privileges;
     }
 
     /** Is access allowed to this?
      *
      * @return boolean
      */
     public boolean getAccessAllowed() {
       return accessAllowed;
     }
 
    @Override
     public int compareTo(final CurrentAccess that) {
       if (this == that) {
         return 0;
       }
 
       int res = Util.compare(aclChars, that.aclChars);
 
       if (res != 0) {
         return res;
       }
 
       res = Util.cmpObjval(privileges, that.privileges);
 
       if (res != 0) {
         return res;
       }
 
       return Util.cmpBoolval(accessAllowed, that.accessAllowed);
     }
 
     @Override
     public int hashCode() {
       int hc = 7;
 
       if (aclChars != null) {
         hc *= aclChars.hashCode();
       }
 
       if (privileges != null) {
         hc *= privileges.hashCode();
       }
 
       return hc;
     }
 
     @Override
     public boolean equals(final Object o) {
       return compareTo((CurrentAccess)o) == 0;
     }
 
     @Override
     public String toString() {
       StringBuilder sb = new StringBuilder("CurrentAccess{");
       sb.append("acl=");
       sb.append(acl);
 
       sb.append("accessAllowed=");
       sb.append(accessAllowed);
       sb.append("}");
 
       return sb.toString();
     }
   }
 
   /** We use this for things like user home access.
    *
    */
   public static CurrentAccess defaultNonOwnerAccess =
     new CurrentAccess(PrivilegeSet.makeDefaultNonOwnerPrivileges());
 
   /** We use this for superuser access.
    *
    * @param ca
    * @return new CurrentAccess with access allowed
    */
   public static CurrentAccess forceAccessAllowed(final CurrentAccess ca) {
     CurrentAccess newCa = new CurrentAccess(true);
 
     newCa.acl = ca.acl;
     newCa.aclChars = ca.aclChars;
     newCa.privileges = ca.privileges;
 
     return newCa;
   }
 
   /** Evaluating an ACL
    *
    * <p>The process of evaluating access is as follows:
    *
    * <p>For an unauthenticated (guest) user we look for an entry with an
    * unauthenticated 'who' field. If none exists access is denied othewise the
    * indicated privileges are used to determine access.
    *
    * <p>If the principal is authenticated there are a number of steps in the process
    * which are executed in the following order:
    *
    * <ol>
    * <li>If the principal is the owner then use the given access or the default.</li>
    *
    * <li>If there are specific ACEs for the user use the merged access. </li>
    *
    * <li>Find all group entries for the given user's groups. If there is more than
    * one combine them with the more permissive taking precedence, e.g
    * write allowed overrides write denied
    * <p>If any group entries were found we're done.</li>
    *
    * <li>if there is an 'other' entry (i.e. not Owner) use that.</li>
    *
    * <li>if there is an authenticated entry use that.</li>
    *
    * <li>Otherwise apply defaults - for the owner full acccess, for any others no
    * access</li>
    *
    * @param cb
    * @param who
    * @param owner
    * @param how
    * @param aclChars
    * @param filter    if not null specifies maximum access
    * @return CurrentAccess   access + allowed/disallowed
    * @throws AccessException
    */
   public static CurrentAccess evaluateAccess(final AccessCb cb,
                                              final AccessPrincipal who,
                                              final AccessPrincipal owner,
                                              final Privilege[] how,
                                              final char[] aclChars,
                                              final PrivilegeSet filter)
           throws AccessException {
     String aclString = new String(aclChars);
     PrivilegeSet howPriv = PrivilegeSet.makePrivilegeSet(how);
 
     CurrentAccess ca = EvaluatedAccessCache.get(owner.getPrincipalRef(),
                                                 who.getPrincipalRef(),
                                                 howPriv, filter,
                                                 aclString);
 
     if (ca != null) {
       return ca;
     }
 
     ca = evaluateAccessInt(cb, who, owner, how, aclChars, filter);
 
     if (ca == null) {
       return null;
     }
 
     EvaluatedAccessCache.put(owner.getPrincipalRef(),
                              who.getPrincipalRef(),
                              howPriv, filter,
                              aclString,
                              ca);
 
     return ca;
   }
 
   private static CurrentAccess evaluateAccessInt(final AccessCb cb,
                                                  final AccessPrincipal who,
                                                  final AccessPrincipal owner,
                                                  final Privilege[] how,
                                                  final char[] aclChars,
                                                  final PrivilegeSet filter)
             throws AccessException {
     evaluations.count++;
 
     boolean authenticated = !who.getUnauthenticated();
     boolean isOwner = false;
     CurrentAccess ca = new CurrentAccess();
 
     Acl acl = decode(aclChars);
     ca.acl = acl;
     ca.aclChars = aclChars;
 
     if (authenticated) {
       isOwner = who.equals(owner);
     }
 
     StringBuilder debugsb = null;
 
     if (debug) {
       debugsb = new StringBuilder("Check access for '");
       if (aclChars == null) {
         debugsb.append("NULL");
       } else {
         debugsb.append(new String(aclChars));
       }
       debugsb.append("'\n");
 
       if (authenticated) {
         debugsb.append("   with authenticated principal ");
         debugsb.append(who.getPrincipalRef());
       } else {
         debugsb.append("   unauthenticated ");
       }
       debugsb.append(" isOwner = ");
       debugsb.append(isOwner);
       debugsb.append("'\n");
     }
 
     if (aclChars == null) {
       return ca;
     }
 
     getPrivileges: {
       if (!authenticated) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb, null,
                                                 Ace.whoTypeUnauthenticated);
 
         if (ca.privileges == null) {
           // All might be available
           ca.privileges = Ace.findMergedPrivilege(acl, cb, null, Ace.whoTypeAll);
         }
 
         if (ca.privileges != null) {
           if (debug) {
             debugsb.append("... For unauthenticated got: " + ca.privileges);
             debugsb.append("'\n");
           }
 
           break getPrivileges;
         }
       }
 
       if (isOwner) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb, null, Ace.whoTypeOwner);
         if (ca.privileges == null) {
           ca.privileges = PrivilegeSet.makeDefaultOwnerPrivileges();
         }
 
         if (debug) {
           debugsb.append("... For owner got: " + ca.privileges);
           debugsb.append("'\n");
         }
 
         break getPrivileges;
       }
 
       // Not owner - look for user
       ca.privileges = Ace.findMergedPrivilege(acl, cb,
                                               who.getPrincipalRef(),
                                               Ace.whoTypeUser);
 
       // Treat resources, tickets, hosts and venues like user
       // XXX This assumes the account name is distinguishable.
       if (ca.privileges == null) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb,
                                                 who.getPrincipalRef(),
                                                 Ace.whoTypeResource);
       }
       if (ca.privileges == null) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb,
                                                 who.getPrincipalRef(),
                                                 Ace.whoTypeTicket);
       }
       if (ca.privileges == null) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb,
                                                 who.getPrincipalRef(),
                                                 Ace.whoTypeVenue);
       }
       if (ca.privileges == null) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb,
                                                 who.getPrincipalRef(),
                                                 Ace.whoTypeHost);
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("... For user got: " + ca.privileges);
           debugsb.append("'\n");
         }
 
         break getPrivileges;
       }
 
       // No specific user access - look for group access
 
       if (who.getGroupNames() != null) {
         for (String group: who.getGroupNames()) {
           if (debug) {
             debugsb.append("...Try access for group " + group);
             debugsb.append("'\n");
           }
           PrivilegeSet privs = Ace.findMergedPrivilege(acl, cb, group,
                                                        Ace.whoTypeGroup);
           if (privs != null) {
             ca.privileges = PrivilegeSet.mergePrivileges(ca.privileges, privs,
                                                          false);
           }
         }
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("...For groups got: " + ca.privileges);
           debugsb.append("'\n");
         }
 
         break getPrivileges;
       }
 
       // "authenticated" access set?
       if (authenticated) {
         ca.privileges = Ace.findMergedPrivilege(acl, cb, null,
                                                 Ace.whoTypeAuthenticated);
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("...For authenticated got: " + ca.privileges);
           debugsb.append("'\n");
         }
 
         break getPrivileges;
       }
 
       // "other" access set?
       ca.privileges = Ace.findMergedPrivilege(acl, cb, null, Ace.whoTypeOther);
 
       if (ca.privileges == null) {
         // All might be available
         ca.privileges = Ace.findMergedPrivilege(acl, cb, null, Ace.whoTypeAll);
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("...For other got: " + ca.privileges);
           debugsb.append("'\n");
         }
 
         break getPrivileges;
       }
     } // getPrivileges
 
     if (isOwner) {
       // Owner always has read/write acl privilege
 
       char racl = ca.privileges.getPrivilege(privReadAcl);
       char wacl = ca.privileges.getPrivilege(privWriteAcl);
 
       if (((racl != allowed) && (racl != allowedInherited)) ||
           ((wacl != allowed) && (wacl != allowedInherited))) {
         ca.privileges = PrivilegeSet.mergePrivileges(ca.privileges,
                                                      PrivilegeSet.ownerAclPrivileges,
                                                      false);
       }
     }
 
     if (ca.privileges == null) {
       if (debug) {
         debugMsg(debugsb.toString() + "...Check access denied (noprivs)");
       }
       return ca;
     }
 
     ca.privileges = PrivilegeSet.setUnspecified(ca.privileges, isOwner);
 
     if (filter != null) {
       ca.privileges = PrivilegeSet.filterPrivileges(ca.privileges, filter);
     }
 
     if (usePool) {
       ca.privileges = privSets.get(ca.privileges);
     }
 
     if (how.length == 0) {
       // Means any access will do
 
       ca.accessAllowed = ca.privileges.getAnyAllowed();
       if (debug) {
         if (ca.accessAllowed) {
           debugMsg(debugsb.toString() + "...Check access allowed (any requested)");
         } else {
           debugMsg(debugsb.toString() + "...Check access denied (any requested)");
         }
       }
 
       return ca;
     }
 
     /* Check each requested access right. If denied, fail immediately, otherwise
      * continue to the next request right.
      */
 
     for (int i = 0; i < how.length; i++) {
       char priv = ca.privileges.getPrivilege(how[i].getIndex());
 
       if ((priv != allowed) && (priv != allowedInherited)) {
         if (debug) {
           debugsb.append("...Check access denied (!allowed) ");
           debugsb.append(ca.privileges);
           debugMsg(debugsb.toString());
         }
         return ca;
       }
     }
 
     /* Caller specified some access rights they wanted and all of them are
      * granted.
      */
 
     if (debug) {
       debugMsg(debugsb.toString() + "...Check access allowed");
     }
 
     ca.accessAllowed = true;
     return ca;
   }
 
   /** Return the ace collection for previously decoded access
    *
    * @return Collection ace collection for previously decoded access
    * @throws AccessException
    */
   public Collection<Ace> getAces() throws AccessException {
     if (aces == null) {
       return null;
     }
 
     return Collections.unmodifiableCollection(aces.values());
   }
 
   /** Remove access for a given 'who' entry
    *
    * @param who
    * @return null if unchanged otehrwise new Acl
    * @throws AccessException
    */
   public Acl removeWho(final AceWho who) throws AccessException {
    if (aces == null) {
       return null;
     }
 
    /* Prescan looking for who */
    boolean contains = false;

    for (Ace a: getAces()) {
      if (who.equals(a.getWho())) {
        contains = true;
        break;
      }
    }

    if (!contains) {
    	return null;
    }

     Collection<Ace> aces = new ArrayList<Ace>();
 
     for (Ace a: getAces()) {
       if (!who.equals(a.getWho())) {
         aces.add(a);
       }
     }
 
     return new Acl(aces);
   }
 
   /* ====================================================================
    *                 Decoding methods
    * ==================================================================== */
 
 
   /** Given an encoded acl convert to an ordered sequence of fully expanded
    * ace objects.
    *
    * @param val String val to decode
    * @return decoded Acl
    * @throws AccessException
    */
   public static Acl decode(final String val) throws AccessException {
     return decode(val.toCharArray());
   }
 
   /** Given an encoded acl convert to an ordered sequence of fully expanded
    * ace objects.
    *
    * @param val char[] val to decode
    * @return decoded Acl
    * @throws AccessException
    */
   public static Acl decode(final char[] val) throws AccessException {
     return decode(val, null);
   }
 
 
   /** Given an encoded acl convert to an ordered sequence of fully expanded
    * ace objects.
    *
    * @param val char[] val to decode
    * @param path
    * @return decoded Acl
    * @throws AccessException
    */
   public static Acl decode(final char[] val, final String path) throws AccessException {
     EncodedAcl eacl = new EncodedAcl();
     eacl.setEncoded(val);
 
     Collection<Ace> aces = new ArrayList<Ace>();
 
     while (eacl.hasMore()) {
       Ace ace = Ace.decode(eacl, path);
 
       aces.add(ace);
     }
 
     return new Acl(aces);
   }
 
   /** Given an encoded acl create a new merged version. This process
    * should be carried out moving up from the end of the path to the root as
    * entries will only be added to the merged list if the notWho + whoType + who
    * do not match.
    *
    * <p>The inherited flag will be set on all merged Ace objects.
    *
    * <p>For example, if we have the path structure
    * <pre>
    *     /user                 owner=sys,access=write-content owner
    *        /jeb               owner=jeb,access=write-content owner
    *           /calendar       owner=jeb    no special access
    *           /rocalendar     owner=jeb    read owner
    * </pre>
    * then, while evaluating the access for rocalendar we start at rocalendar
    * and move up the tree. The "read owner" access on rocalendar overrides any
    * access we find further up the tree, e.g. "write-content owner"
    *
    * <p>While evaluating the access for calendar we start at calendar
    * and move up the tree. There is no overriding access so the final access is
    * "write-content owner" inherited from /user/jeb
    *
    * <p>Also note the encoded value will not reflect the eventual Acl.
    *
    * <p>And what did that mean? I think I meant that we can derive the acl for
    * an entity from the merged result.
    *
    * @param val char[] val to decode and merge
    * @param path   path of current entity to flag the inheritance
    * @return merged Acl
    * @throws AccessException
    */
   public Acl merge(final char[] val, final String path) throws AccessException {
     Collection<Ace> newAces = new ArrayList<Ace>();
 
     newAces.addAll(getAces());
 
     Acl encAcl = decode(val, path);
 
     domerge:
     for (Ace a: encAcl.getAces()) {
       for (Ace ace: newAces) {
         if (a.getWho().equals(ace.getWho())) {
           // Take the child entry
           continue domerge;
         }
       }
 
       // Not in child - add from the parent
       newAces.add(a);
     }
 
     return new Acl(newAces);
   }
 
   /* * Given a decoded acl merge it into this objects ace list. This process
    * should be carried out moving up from the end of the path to the root as
    * entries will only be added to the merged list if the notWho + whoType + who
    * do not match.
    *
    * <p>The inherited flag will be set on all merged Ace objects.
    * <p>XXX Note that reuse of Acls for merges invalidates the inherited flag.
    * I think it's only used for display and acl modification purposes so
    * shouldn't affect normal access control checks.
    *
    * <p>Also note the encoded value will not reflect the eventual Acl.
    *
    * @param val Acl to merge
    * @throws AccessException
    * /
   public void merge(Acl val) throws AccessException {
     Collection<Ace> valAces = val.getAces();
 
     if (valAces == null) {
       return;
     }
 
     for (Ace ace: valAces) {
       ace.setInherited(true);
 
       if (!aces.contains(ace)) {
         aces.add(ace);
       }
     }
   }*/
 
   /* ====================================================================
    *                 Encoding methods
    * ==================================================================== */
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will be skipped.
    *
    * @return char[] encoded value
    * @throws AccessException
    */
   public char[] encode() throws AccessException {
     startEncoding();
 
     if (aces == null) {
       return null;
     }
 
     for (Ace ace: aces.values()) {
       if (ace.getInheritedFrom() == null) {
         ace.encode(this);
       }
     }
 
     return getEncoding();
   }
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will be skipped. Returns null for no aces
    *
    * @return String encoded value or null
    * @throws AccessException
    */
   public String encodeStr() throws AccessException {
     char[] encoded = encode();
     if (encoded == null) {
        return null;
     }
 
     return new String(encoded);
   }
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will NOT be skipped.
    *
    * @return char[] encoded value
    * @throws AccessException
    */
   public char[] encodeAll() throws AccessException {
     startEncoding();
 
     if (aces == null) {
       return null;
     }
 
     for (Ace ace: aces.values()) {
       ace.encode(this);
     }
 
     return getEncoding();
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   /** Provide a string representation for user display - this should
    * use a localized resource and be part of a display level.
    *
    * @return String representation
    */
   public String toUserString() {
     StringBuilder sb = new StringBuilder();
 
     try {
       decode(getEncoded());
 
       for (Ace ace: aces.values()) {
         sb.append(ace.toString());
         sb.append(" ");
       }
     } catch (Throwable t) {
       error(t);
       sb.append("Decode exception " + t.getMessage());
     }
 
     return sb.toString();
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
 
     sb.append("Acl{");
     if (!empty()) {
       sb.append("encoded=[");
 
       rewind();
       while (hasMore()) {
         sb.append(getChar());
       }
       sb.append("] ");
 
       rewind();
 
       try {
         if (aces == null) {
           decode(getEncoded());
         }
 
         for (Ace ace: aces.values()) {
           sb.append("\n");
           sb.append(ace.toString());
         }
       } catch (Throwable t) {
         error(t);
         sb.append("Decode exception " + t.getMessage());
       }
     }
     sb.append("}");
 
     return sb.toString();
   }
 
   protected static Logger getLog(final Class cl) {
     if (log == null) {
       log = Logger.getLogger(EncodedAcl.class);
     }
 
     return log;
   }
 
   /** For testing
    *
    * @param args
    */
   public static void main(final String[] args) {
     try {
       Acl acl = decode(args[0]);
 
       System.out.println(acl.toString());
     } catch (Throwable t) {
       t.printStackTrace();
     }
   }
 }
 
