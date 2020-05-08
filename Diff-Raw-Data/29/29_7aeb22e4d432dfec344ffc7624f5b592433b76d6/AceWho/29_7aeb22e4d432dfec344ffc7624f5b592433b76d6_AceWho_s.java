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
 
 /** describe who we are giving access to
  * @author douglm - rpi.edu
  */
 public class AceWho implements WhoDefs, Comparable {
   private String who;
 
   private int whoType;
 
   private boolean notWho;
 
   /**
    */
   public AceWho() {
   }
 
   /**
    * @param who
    * @param whoType
    * @param notWho
    */
   public AceWho(String who,
                 int whoType,
                 boolean notWho) {
     this.who = who;
     this.notWho = notWho;
     this.whoType = whoType;
   }
 
   /** Set who this entry is for
    *
    * @param val
    */
   public void setWho(String val) {
     who = val;
   }
 
   /** Get who this entry is for
    *
    * @return String who
    */
   public String getWho() {
     return who;
   }
 
   /** Set the who/not who flag
    *
    * @param val
    */
   public void setNotWho(boolean val) {
     notWho = val;
   }
 
   /**
    * @return boolean who/not who flag
    */
   public boolean getNotWho() {
     return notWho;
   }
 
   /** Set type of who
    *
    * @param val
    */
   public void setWhoType(int val) {
     whoType = val;
   }
 
   /**
    * @return boolean  type of who
    */
   public int getWhoType() {
     return whoType;
   }
 
   /**
    * @param name
    * @return boolean true if the name matches
    */
   public boolean whoMatch(String name) {
     if ((name == null) && (getWho() == null)) {
       return !getNotWho();
     }
 
     if ((name == null) || (getWho() == null)) {
       return getNotWho();
     }
 
     boolean match = name.equals(getWho());
     if (getNotWho()) {
       match = !match;
     }
 
     return match;
   }
 
   /* ====================================================================
    *                 De/Encoding methods
    * ==================================================================== */
 
   /** Encode this object as a sequence of char. privs must have been set.
    *
    * @param acl   EncodedAcl
    * @throws AccessException
    */
   public void encode(EncodedAcl acl) throws AccessException {
     if (notWho) {
       acl.addChar(notWhoFlag);
     } else {
       acl.addChar(whoFlag);
     }
 
     acl.addChar(whoTypeFlags[whoType]);
 
     acl.encodeString(who);
   }
 
   /**
    * @param acl
    * @throws AccessException
    */
   public void decode(EncodedAcl acl) throws AccessException {
     char c = acl.getChar();
 
     if (c == notWhoFlag) {
       notWho = true;
     } else if (c == whoFlag) {
       notWho = false;
     } else {
       throw AccessException.badACE("who/notWho flag");
     }
 
     c = acl.getChar();
 
     getWhoType:{
       for (whoType = 0; whoType < whoTypeFlags.length; whoType++) {
         if (c == whoTypeFlags[whoType]) {
           break getWhoType;
         }
       }
 
       throw AccessException.badACE("who type");
     }
 
     setWho(acl.getString());
   }
 
   /** Provide a string representation for user display - this should probably
    * use a localized resource and be part of a display level. It also requires
    * the Privilege objects
    *
    * @return String representation
    */
   public String toUserString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append(whoTypeNames[whoType]);
     if (notWho) {
       sb.append("NOT ");
     }
 
     sb.append(whoTypeNames[whoType]);
 
    if ((whoType == whoTypeUser) ||
        (whoType == whoTypeGroup) ||
        (whoType == whoTypeHost)) {
       sb.append("=");
       sb.append(getWho());
     }
 
     return sb.toString();
   }
 
   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(Object o) {
     if (this == o) {
       return 0;
     }
 
     if (!(o instanceof AceWho)) {
       return 1;
     }
 
     AceWho that = (AceWho)o;
 
     if (notWho != that.notWho) {
       if (notWho) {
         return -1;
       }
       return 1;
     }
 
     if (whoType < that.whoType) {
       return -1;
     }
 
     if (whoType > that.whoType) {
       return 1;
     }
 
     return compareWho(who, that.who);
   }
 
   public int hashCode() {
     int hc = 7;
 
     if (who != null) {
       hc *= who.hashCode();
     }
 
     if (notWho) {
       hc *= 2;
     }
 
     return hc *= whoType;
   }
 
   public boolean equals(Object o) {
     return compareTo(o) == 0;
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("AceWho{who=");
     sb.append(who);
     sb.append(", notWho=");
     sb.append(notWho);
     sb.append(", whoType=");
     sb.append(whoTypeNames[whoType]);
     sb.append("(");
     sb.append(whoType);
     sb.append(")");
 
     sb.append("}");
 
     return sb.toString();
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private int compareWho(String who1, String who2) {
     if ((who1 == null) && (who2 == null)) {
       return 0;
     }
 
     if (who1 == null) {
       return -1;
     }
 
     if (who2 == null) {
       return 1;
     }
 
     return who1.compareTo(who2);
   }
 }
