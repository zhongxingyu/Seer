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
 
 import java.io.Serializable;
 import java.util.Collection;
 
 /** Class used when determining access.
  *
  * @author Mike Douglass
  *
  */
 public interface AccessPrincipal extends Serializable, Comparable {
   /** The kind of Principal returned by getKind() */
 
   /** user */
   public static final int principalUser = 0;
 
   /** group */
   public static final int principalGroup = 1;
 
   /** host */
  public static final int principalHost = 2;
 
   /** Return the kind of Principal, e.g. user/group etc
    * @return int kind
    */
   public int getKind();
 
   /** Set the unauthenticated state.
    *
    * @param val
    */
   public void setUnauthenticated(boolean val);
 
   /**
    * @return  boolean authenticated state
    */
   public boolean getUnauthenticated();
 
   /**
    * @return boolean true for a guest principal
    */
   public boolean isUnauthenticated();
 
   /**
    * @param val
    */
   public void setAccount(String val);
 
   /**
    * @return  String account name
    */
   public String getAccount();
 
   /** Set of groupNames of which principal is a member. These are not just those
    * of which the principal is a direct member but also those it is a member of
    * by virtue of membership of other groups. For example <br/>
    * If the principal is a member of groupA and groupA is a member of groupB
    * the groupB should appear in the list.
    *
    * @param val        Set of String
    */
   public void setGroupNames(Collection<String> val);
 
   /** Get the group names of which principal is a member.
    *
    * @return Set    of String
    */
   public Collection<String> getGroupNames();
 }
