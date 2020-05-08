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
 
 /** describe who we are giving access to
  * @author douglm - rpi.edu
  */
 public interface WhoDefs extends Serializable {
   /** Who defines a principal, NotWho means the principal must not be
      defined by the 'who',e.g NOT IN group bgroup
    */
   public static final char whoFlag = 'W';
   /** */
   public static final char notWhoFlag = 'N';
 
   /** */
   public static final char whoFlagOwner = 'O';
   /** */
   public static final char whoFlagUser = 'U';
   /** */
   public static final char whoFlagGroup = 'G';
   /** */
   public static final char whoFlagHost = 'H';
   /** */
   public static final char whoFlagUnauthenticated = 'X';
   /** */
   public static final char whoFlagAuthenticated = 'A';
   /** */
   public static final char whoFlagOther = 'Z';
   /** */
   public static final char whoFlagAll = 'L';
 
   /** Define for whom we are checking access */
 
   /** The entity owner */
   public static final int whoTypeOwner = 0;
 
   /** A named user */
   public static final int whoTypeUser = 1;
 
   /** A named group */
   public static final int whoTypeGroup = 2;
 
   /** A named host */
   public static final int whoTypeHost = 3;  // Named host
 
   /** An unauthenticated user */
   public static final int whoTypeUnauthenticated = 4;  // Unauthenticated user
 
   /** An authenticated user */
   public static final int whoTypeAuthenticated = 5;  // Authenticated user
 
   /** Somebody other than the owner */
   public static final int whoTypeOther = 6;
 
   /** Anywho */
   public static final int whoTypeAll = 7; // Unauth + auth
 
   /** indexed by whoType */
   public static final char[] whoTypeFlags = {
     whoFlagOwner,
     whoFlagUser,
     whoFlagGroup,
     whoFlagHost,
     whoFlagUnauthenticated,
     whoFlagAuthenticated,
     whoFlagOther,
     whoFlagAll
   };
 
   /** indexed by whoType - flag who types that require a name*/
   public static final boolean[] whoTypeNamed = {
     false,              // whoTypeOwner,
     true,               // whoTypeUser,
     true,              // whoTypeGroup,
     true,               // whoTypeHost,
     false,    // whoTypeUnauthenticated,
     false,      // whoTypeAuthenticated
     false,              // whoFlagOther
     false,                // whoFlagAll
   };
 
   /** String name of each who type. These are keys to the resources for locale
    * specific displays
    *
    */
   public static final String[] whoTypeNames = {
     "owner",              // whoTypeOwner,
     "user",               // whoTypeUser,
     "group",              // whoTypeGroup,
     "host",               // whoTypeHost,
     "unauthenticated",    // whoTypeUnauthenticated,
     "authenticated",      // whoTypeAuthenticated
     "other",              // whoFlagOther
     "all",                // whoFlagAll
   };
 }
