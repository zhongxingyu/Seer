 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.sss.util.xml.tagdefs;
 
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 
 /** Define Carddav tags for XMlEmit
  *
  * @author Mike Douglass douglm@rpi.edu
  */
 public class CarddavTags {
   /** Namespace for these tags
    */
   public static final String namespace = "urn:ietf:params:xml:ns:carddav";
 
   /** Tables of QNames indexed by name
    */
   public final static HashMap<String, QName> qnames = new HashMap<String, QName>();
 
   /** */
   public static final QName addressbook = makeQName("addressbook");
 
   /** */
   public static final QName addressbookDescription = makeQName("addressbook-description");
 
   /** */
   public static final QName addressbookHomeSet = makeQName("addressbook-home-set");
 
   /** */
   public static final QName addressbookCollectionLocationOk =
               makeQName("addressbook-collection-location-ok");
 
   /**   */
   public static final QName addressbookMultiget = makeQName("addressbook-multiget");
 
   /** */
   public static final QName addressbookQuery = makeQName("addressbook-query");
 
   /** */
   public static final QName addressData = makeQName("address-data");
 
   /** */
   public static final QName allprop = makeQName("allprop");
 
   /**   */
   public static final QName filter = makeQName("filter");
 
   /**   */
   public static final QName isNotDefined = makeQName("is-not-defined");
 
   /**   */
   public static final QName limit = makeQName("limit");
 
   /**   */
   public static final QName maxResourceSize = makeQName("max-resource-size");
 
   /**   */
   public static final QName nresults = makeQName("nresults");
 
   /**   */
   public static final QName paramFilter = makeQName("param-filter");
 
   /** */
   public static final QName principalAddress = makeQName("principal-address");
 
   /**   */
   public static final QName prop = makeQName("prop");
 
   /**   */
   public static final QName propFilter = makeQName("prop-filter");
 
   /** */
  public static final QName supportedAddressData = makeQName("supported--address-data");
 
   /** */
   public static final QName supportedCollation = makeQName("supported-collation");
 
   /** */
   public static final QName validAddressData = makeQName("valid-address-data");
 
   /**   */
   public static final QName textMatch = makeQName("text-match");
 
   private static QName makeQName(String name) {
     QName q = new QName(namespace, name);
     qnames.put(name, q);
 
     return q;
   }
 }
 
