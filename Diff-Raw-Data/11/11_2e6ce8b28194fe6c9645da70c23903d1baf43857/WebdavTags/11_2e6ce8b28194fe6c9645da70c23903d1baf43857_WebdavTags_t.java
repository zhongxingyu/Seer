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
 
 /** Define Webdav tags for XMlEmit
  *
  * @author Mike Douglass douglm@rpi.edu
  */
 public class WebdavTags {
   /** Namespace for these tags
    */
   public static final String namespace = "DAV:";
 
   /** Tables of QNames indexed by name
    */
   public final static HashMap<String, QName> qnames = new HashMap<String, QName>();
 
   /** */
   public static final QName _abstract = makeQName("abstract");
 
   /** */
   public static final QName ace = makeQName("ace");
 
   /** */
   public static final QName acl = makeQName("acl");
 
   /** */
   public static final QName aclPrincipalPropSet = makeQName("acl-principal-prop-set");
 
   /** */
   public static final QName aclRestrictions = makeQName("acl-restrictions");
 
   /** */
   public static final QName all = makeQName("all");
 
   /** */
   public static final QName allowedPrincipal = makeQName("allowed-principal");
 
   /** */
   public static final QName allprop = makeQName("allprop");
 
   /** */
   public static final QName alternateURISet = makeQName("alternate-URI-set");
 
   /** */
   public static final QName applyToPrincipalCollectionSet =
           makeQName("apply-to-principal-collection-set");
 
   /** */
   public static final QName authenticated = makeQName("authenticated");
 
   /** */
   public static final QName bind = makeQName("bind");
 
   /** */
   public static final QName collection = makeQName("collection");
 
   /** */
   public static final QName creationdate = makeQName("creationdate");
 
   /** */
   public static final QName currentUserPrincipal = makeQName("current-user-principal");
 
   /** */
   public static final QName currentUserPrivilegeSet = makeQName("current-user-privilege-set");
 
   /** */
   public static final QName deny = makeQName("deny");
 
   /** */
   public static final QName denyBeforeGrant = makeQName("deny-before-grant");
 
   /** */
   public static final QName description = makeQName("description");
 
   /** */
   public static final QName displayname = makeQName("displayname");
 
   /** */
   public static final QName error = makeQName("error");
 
   /** */
   public static final QName expandProperty = makeQName("expand-property");
 
   /** */
   public static final QName getcontentlanguage = makeQName("getcontentlanguage");
 
   /** */
   public static final QName getcontentlength = makeQName("getcontentlength");
 
   /** */
   public static final QName getcontenttype = makeQName("getcontenttype");
 
   /** */
   public static final QName getetag = makeQName("getetag");
 
   /** */
   public static final QName getlastmodified = makeQName("getlastmodified");
 
   /** */
   public static final QName grant = makeQName("grant");
 
   /** */
   public static final QName grantOnly = makeQName("grant-only");
 
   /** */
   public static final QName group = makeQName("group");
 
   /** */
   public static final QName groupMemberSet = makeQName("group-member-set");
 
   /** */
   public static final QName groupMembership = makeQName("group-membership");
 
   /** */
   public static final QName href = makeQName("href");
 
   /** */
   public static final QName inherited = makeQName("inherited");
 
   /** */
   public static final QName inheritedAclSet = makeQName("inherited-acl-set");
 
   /** */
   public static final QName invert = makeQName("invert");
 
   /** */
   public static final QName limitedNumberOfAces = makeQName("limited-number-of-aces");
 
   /** */
   public static final QName lockdiscovery = makeQName("lockdiscovery");
 
   /** */
   public static final QName lockentry = makeQName("lockentry");
 
   /** */
   public static final QName lockscope = makeQName("lockscope");
 
   /** */
   public static final QName locktype = makeQName("locktype");
 
   /** */
   public static final QName match = makeQName("match");
 
   /** */
   public static final QName missingRequiredPrincipal = makeQName("missing-required-principal");
 
   /** */
   public static final QName mkcol = makeQName("mkcol");
 
   /** */
   public static final QName multistatus = makeQName("multistatus");
 
   /** */
   public static final QName needPrivileges = makeQName("need-privileges");
 
   /** */
   public static final QName noAbstract = makeQName("no-abstract");
 
   /** */
   public static final QName noAceConflict = makeQName("no-ace-conflict");
 
   /** */
   public static final QName noInheritedAceConflict = makeQName("no-inherited-ace-conflict");
 
   /** */
   public static final QName noInvert = makeQName("no-invert");
 
   /** */
   public static final QName noProtectedAceConflict = makeQName("no-protected-ace-conflict");
 
   /** */
   public static final QName notSupportedPrivilege = makeQName("not-supported-privilege");
 
   /** */
   public static final QName owner = makeQName("owner");
 
   /** */
   public static final QName principal = makeQName("principal");
 
   /** */
   public static final QName principalCollectionSet = makeQName("principal-collection-set");
 
   /** */
   public static final QName principalMatch = makeQName("principal-match");
 
   /** */
   public static final QName principalProperty = makeQName("principal-property");
 
   /** */
   public static final QName principalPropertySearch = makeQName("principal-property-search");
 
   /** */
   public static final QName principalSearchProperty = makeQName("principal-search-property");
 
   /** */
   public static final QName principalSearchPropertySet = makeQName("principal-search-property-set");
 
   /** */
   public static final QName principalURL = makeQName("principal-URL");
 
   /** */
   public static final QName privilege = makeQName("privilege");
 
   /** */
   public static final QName prop = makeQName("prop");
 
   /** */
   public static final QName property = makeQName("property");
 
   /** */
   public static final QName propertySearch = makeQName("property-search");
 
   /** */
   public static final QName propertyUpdate = makeQName("propertyupdate");
 
   /** */
   public static final QName propfind = makeQName("propfind");
 
   /** */
   public static final QName propname = makeQName("propname");
 
   /** */
   public static final QName propstat = makeQName("propstat");
 
   /** */
   public static final QName _protected = makeQName("protected");
 
   /** */
   public static final QName read = makeQName("read");
 
   /** */
   public static final QName readAcl = makeQName("read-acl");
 
   /** */
   public static final QName readCurrentUserPrivilegeSet = makeQName(
                                                 "read-current-user-privilege-set");
 
   /** */
   public static final QName recognizedPrincipal = makeQName("recognized-principal");
 
   /** */
   public static final QName remove = makeQName("remove");
 
   /** */
   public static final QName report = makeQName("report");
 
   /** */
   public static final QName requiredPrincipal = makeQName("required-principal");
 
   /** */
   public static final QName resource = makeQName("resource");
 
   /** */
   public static final QName resourceMustBeNull = makeQName("resource-must-be-null");
 
   /** */
   public static final QName resourcetype = makeQName("resourcetype");
 
   /** */
   public static final QName response = makeQName("response");
 
   /** */
   public static final QName responseDescription = makeQName("responsedescription");
 
   /** */
   public static final QName self = makeQName("self");
 
   /** */
   public static final QName set = makeQName("set");
 
   /** */
   public static final QName source = makeQName("source");
 
   /** */
   public static final QName status = makeQName("status");
 
   /** */
   public static final QName supportedPrivilege = makeQName("supported-privilege");
 
   /** */
   public static final QName supportedPrivilegeSet = makeQName("supported-privilege-set");
 
   /** */
   public static final QName supportedReport = makeQName("supported-report");
 
   /** */
   public static final QName supportedReportSet = makeQName("supported-report-set");
 
   /** */
   public static final QName syncCollection = makeQName("sync-collection");
 
   /** */
   public static final QName syncResponse = makeQName("sync-response");
 
   /** */
   public static final QName syncToken = makeQName("sync-token");
 
   /** */
   public static final QName supportedlock = makeQName("supportedlock");
 
   /** */
   public static final QName unauthenticated = makeQName("unauthenticated");
 
   /** */
   public static final QName unbind = makeQName("unbind");
 
   /** */
   public static final QName unlock = makeQName("unlock");
 
   /** */
   public static final QName validSyncToken = makeQName("valid-sync-token");
 
   /** */
   public static final QName whoami = makeQName("whoami");
 
   /** */
   public static final QName write = makeQName("write");
 
   /** */
   public static final QName writeAcl = makeQName("write-acl");
 
   /** */
   public static final QName writeContent = makeQName("write-content");
 
   /** */
   public static final QName writeProperties = makeQName("write-properties");
 
  private static QName makeQName(final String name) {
     QName q = new QName(namespace, name);
     qnames.put(name, q);
 
     return q;
   }
 }
 
