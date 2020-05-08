 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: DirectoryManager.java,v 1.2 2006-03-21 20:18:20 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.am.sdk.ldap;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPUrl;
 import netscape.ldap.util.DN;
 import netscape.ldap.util.RDN;
 
 import com.iplanet.am.sdk.AMConstants;
 import com.iplanet.am.sdk.AMDirectoryManager;
 import com.iplanet.am.sdk.AMDirectoryWrapper;
 import com.iplanet.am.sdk.AMEntryExistsException;
 import com.iplanet.am.sdk.AMEventManagerException;
 import com.iplanet.am.sdk.AMException;
 import com.iplanet.am.sdk.AMHashMap;
 import com.iplanet.am.sdk.AMInvalidDNException;
 import com.iplanet.am.sdk.AMNamingAttrManager;
 import com.iplanet.am.sdk.AMObject;
 import com.iplanet.am.sdk.AMObjectClassManager;
 import com.iplanet.am.sdk.AMObjectListener;
 import com.iplanet.am.sdk.AMOrganization;
 import com.iplanet.am.sdk.AMOrganizationalUnit;
 import com.iplanet.am.sdk.AMPreCallBackException;
 import com.iplanet.am.sdk.AMRole;
 import com.iplanet.am.sdk.AMSDKBundle;
 import com.iplanet.am.sdk.AMSearchFilterManager;
 import com.iplanet.am.sdk.AMSearchResults;
 import com.iplanet.am.sdk.AMStoreConnection;
 import com.iplanet.am.sdk.AMTemplate;
 import com.iplanet.am.sdk.AMUser;
 import com.iplanet.am.sdk.AMUserEntryProcessed;
 import com.iplanet.am.sdk.DirectoryManagerInterface;
 import com.iplanet.am.util.Debug;
 import com.iplanet.am.util.Locale;
 import com.iplanet.am.util.OrderedSet;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.ldap.Attr;
 import com.iplanet.services.ldap.AttrSet;
 import com.iplanet.services.ldap.ModSet;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.iplanet.ums.AccessRightsException;
 import com.iplanet.ums.AssignableDynamicGroup;
 import com.iplanet.ums.CreationTemplate;
 import com.iplanet.ums.DynamicGroup;
 import com.iplanet.ums.EntryAlreadyExistsException;
 import com.iplanet.ums.EntryNotFoundException;
 import com.iplanet.ums.FilteredRole;
 import com.iplanet.ums.Guid;
 import com.iplanet.ums.InvalidSearchFilterException;
 import com.iplanet.ums.ManagedRole;
 import com.iplanet.ums.OrganizationalUnit;
 import com.iplanet.ums.PeopleContainer;
 import com.iplanet.ums.PersistentObject;
 import com.iplanet.ums.SchemaManager;
 import com.iplanet.ums.SearchControl;
 import com.iplanet.ums.SearchResults;
 import com.iplanet.ums.SearchTemplate;
 import com.iplanet.ums.SizeLimitExceededException;
 import com.iplanet.ums.SortKey;
 import com.iplanet.ums.StaticGroup;
 import com.iplanet.ums.TemplateManager;
 import com.iplanet.ums.TimeLimitExceededException;
 import com.iplanet.ums.UMSException;
 import com.iplanet.ums.UMSObject;
 import com.iplanet.ums.cos.COSManager;
 import com.iplanet.ums.cos.COSNotFoundException;
 import com.iplanet.ums.cos.COSTemplate;
 import com.iplanet.ums.cos.DirectCOSDefinition;
 import com.iplanet.ums.cos.ICOSDefinition;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 /**
  * A class which manages all the major Directory related operations. Contains
  * functionality to create, delete and manange directory entries.
  * 
  * This class should not be used directly when caching mode is on.
  * 
  */
 public class DirectoryManager implements AMConstants, 
             DirectoryManagerInterface {
 
     // Define default naming attribute name for different types of object
     private static final String DEFAULT_USER_NAMING_ATTR = "uid";
 
     private static final String DEFAULT_RESOURCE_NAMING_ATTR = "uid";
 
     private static final String DEFAULT_ROLE_NAMING_ATTR = "cn";
 
     private static final String DEFAULT_GROUP_NAMING_ATTR = "cn";
 
     private static final String DEFAULT_ORG_NAMING_ATTR = "o";
 
     private static final String DEFAULT_ORG_UNIT_NAMING_ATTR = "ou";
 
     private static final String DEFAULT_PEOPLE_CONTAINER_NAMING_ATTR = "ou";
 
     private static final String DEFAULT_GROUP_CONTAINER_NAMING_ATTR = "ou";
 
     private static final String DEFAULT_DYNAMIC_GROUP_NAMING_ATTR = "cn";
 
     private static final String DEFAULT_FILTERED_ROLE_NAMING_ATTR = "cn";
 
     private static final String DEFAULT_ASSIGNABLE_DYNAMIC_GROUP_NAMING_ATTR = 
         "cn";
 
     private static final String LDAP_CONNECTION_ERROR_CODES = 
         "com.iplanet.am.ldap.connection.ldap.error.codes.retries";
 
     private static HashSet retryErrorCodes = new HashSet();
 
     // Creation Template Names
     private static final String USER_CREATION_TEMPLATE = "BasicUser";
 
     private static final String GROUP_CREATION_TEMPLATE = "BasicGroup";
 
     private static final String MANAGED_ROLE_CREATION_TEMPLATE = 
         "BasicManagedRole";
 
     private static final String RESOURCE_CREATION_TEMPLATE = "BasicResource";
 
     private static final String FILTERED_ROLE_CREATION_TEMPLATE = 
         "BasicFilteredRole";
 
     private static final String ASSIGANABLE_DYNAMIC_GROUP_CREATION_TEMPLATE = 
         "BasicAssignableDynamicGroup";
 
     private static final String DYNAMIC_GROUP_CREATION_TEMPLATE = 
         "BasicDynamicGroup";
 
     private static final String ORGANIZATION_CREATION_TEMPLATE = 
         "BasicOrganization";
 
     private static final String PEOPLE_CONTAINTER_CREATION_TEMPLATE = 
         "BasicPeopleContainer";
 
     private static final String ORGANIZATIONAL_UNIT_CREATION_TEMPLATE = 
         "BasicOrganizationalUnit";
 
     private static final String GROUP_CONTAINER_CREATION_TEMPLATE = 
         "BasicGroupContainer";
 
     // TemplateManager handle. Keep handle to avoid getManager() calls which
     // are synchronized
     private static TemplateManager templateMgr = null;
 
     // Default Object Classes
     private static final String DEFAULT_USER_OBJECT_CLASS = "inetorgperson";
 
     private static final String DEFAULT_RESOURCE_OBJECT_CLASS = 
         "inetcalresource";
 
     private static final String DEFAULT_ROLE_OBJECT_CLASS = 
         "nsmanagedroledefinition";
 
     private static final String DEFAULT_FILTERED_ROLE_OBJECT_CLASS = 
         "nsfilteredroledefinition";
 
     private static final String DEFAULT_ORGANIZATION_OBJECT_CLASS = 
         "organization";
 
     private static final String DEFAULT_ORGANIZATIONAL_UNIT_OBJECT_CLASS = 
         "organizationalunit";
 
     private static final String DEFAULT_GROUP_OBJECT_CLASS = 
         "iplanet-am-managed-group";
 
     private static final String DEFAULT_DYNAMIC_GROUP_OBJECT_CLASS = 
         "groupofurls";
 
     private static final String DEFAULT_ASSIGNABLE_DYNAMIC_GROUP_OBJECT_CLASS = 
         "iplanet-am-managed-assignable-group";
 
     private static final String DEFAULT_GROUP_CONTAINER_OBJECT_CLASS = 
         "iplanet-am-managed-group-container";
 
     private static final String DEFAULT_PEOPLE_CONTAINER_OBJECT_CLASS = 
         "nsManagedPeopleContainer";
 
     // Search Template Names
     private static final String USER_SEARCH_TEMPLATE = "BasicUserSearch";
 
     private static final String ROLE_SEARCH_TEMPLATE = "BasicManagedRoleSearch";
 
     private static final String FILTERED_ROLE_SEARCH_TEMPLATE = 
         "BasicFilteredRoleSearch";
 
     private static final String GROUP_SEARCH_TEMPLATE = "BasicGroupSearch";
 
     private static final String DYNAMIC_GROUP_SEARCH_TEMPLATE = 
         "BasicDynamicGroupSearch";
 
     private static final String ORGANIZATION_SEARCH_TEMPLATE = 
         "BasicOrganizationSearch";
 
     private static final String PEOPLE_CONTAINER_SEARCH_TEMPLATE = 
         "BasicPeopleContainerSearch";
 
     private static final String ORGANIZATIONAL_UNIT_SEARCH_TEMPLATE = 
         "BasicOrganizationalUnitSearch";
 
     private static final String ASSIGNABLE_DYNAMIC_GROUP_SEARCH_TEMPLATE = 
         "BasicAssignableDynamicGroupSearch";
 
     private static final String GROUP_CONTAINER_SEARCH_TEMPLATE = 
         "BasicGroupContainerSearch";
 
     private static final String RESOURCE_SEARCH_TEMPLATE = 
         "BasicResourceSearch";
 
     /*
      * Default Search Filters
      * 
      * private static final String DEFAULT_USER_SEARCH_FILTER =
      * "(objectclass=inetorgperson)";
      * 
      * private static final String DEFAULT_ROLE_SEARCH_FILTER =
      * "(objectclass=nsmanagedroledefinition)";
      * 
      * private static final String DEFAULT_FILTERED_ROLE_SEARCH_FILTER =
      * "(&(objectclass=nsfilteredroledefinition)(!(cn=" +
      * AMConstants.CONTAINER_DEFAULT_TEMPLATE_ROLE + ")))";
      * 
      * private static final String DEFAULT_GROUP_SEARCH_FILTER =
      * "(objectclass=groupofuniquenames)";
      * 
      * private static final String
      * DEFAULT_ASSIGNABLE_DYNAMIC_GROUP_SEARCH_FILTER =
      * "(objectclass=iplanet-am-managed-assignable-group)";
      * 
      * private static final String DEFAULT_DYNAMIC_GROUP_SEARCH_FILTER =
      * "(objectclass=groupofurls)";
      * 
      * private static final String DEFAULT_ORGANIZATION_SEARCH_FILTER =
      * "(objectclass=organization)";
      * 
      * private static final String DEFAULT_PEOPLE_CONTAINER_SEARCH_FILTER =
      * "(objectclass=nsManagedPeopleContainer)";
      * 
      * private static final String DEFAULT_ORGANIZATIONAL_UNIT_SEARCH_FILTER =
      * "(objectclass=organizationalunit)";
      * 
      * private static final String DEFAULT_GROUP_CONTAINER_SEARCH_FILTER =
      * "(objectclass=iplanet-am-managed-group-container)";
      * 
      */
 
     public static Debug debug = CommonUtils.debug;
 
     public static boolean isUserPluginInitialized = false; // first time flag
 
     private static AMUserEntryProcessed userEntry = null;
 
     private String[] aName = { "objectclass" };
 
     private SearchControl scontrol = new SearchControl();
 
     // A handle to Singleton instance
     private static DirectoryManager instance;
 
     private static AMEventManager eventManager;
 
     private static Map listeners = new HashMap();
 
     protected DCTree dcTree = new DCTree();
 
     protected Compliance compl = new Compliance();
 
     protected SSOToken internalToken;
 
     private AMDirectoryManager amdm;
 
     static {
         // amdm = AMDirectoryWrapper.getInstance();
         String retryErrs = SystemProperties.get(LDAP_CONNECTION_ERROR_CODES);
         if (retryErrs != null) {
             StringTokenizer stz = new StringTokenizer(retryErrs, ",");
             while (stz.hasMoreTokens()) {
                 retryErrorCodes.add(stz.nextToken().trim());
             }
         }
     }
 
     /**
      * Ideally this constructor should be private, since we are extending this
      * class, it needs to be public. This constructor should not be used to
      * create an instance of this class.
      * 
      * <p>
      * Use <code>AMDirectoryWrapper.getInstance()</code> to create an
      * instance.
      */
     public DirectoryManager() {
         // Start the event service threads here. This would be the right place
         // to start them
         internalToken = CommonUtils.getInternalToken();
         scontrol.setSearchScope(SearchControl.SCOPE_BASE);
     }
 
     public static synchronized DirectoryManager getInstance() {
         if (instance == null) {
             debug.message("DirectoryManager.getInstance(): Creating a new "
                     + "Instance of DirectoryManager()");
             instance = new DirectoryManager();
         }
         return instance;
     }
 
     // *************************************************************************
     // Some local utility methods related to the operations performed. Generic
     // UMSException & LDAPException Processing:
     // TODO: Refactor these to some other class
     // *************************************************************************
     protected String getEntryName(UMSException e) {
         DN dn = getExceptionDN(e);
         String entryName = "";
         if (dn != null) {
             entryName = ((RDN) dn.getRDNs().firstElement()).getValues()[0];
         }
         return entryName;
     }
 
     private DN getExceptionDN(UMSException e) {
         DN dn = null;
         String msg = e.getMessage();
         if (msg != null) {
             // This is hack??
             int index = msg.indexOf("::");
             if (index != -1) {
                 String errorDN = msg.substring(0, index);
                 if (DN.isDN(errorDN)) {
                     dn = new DN(errorDN);
                 }
             }
         }
         return dn;
     }
 
     private String getEntryNotFoundMsgID(int objectType) {
         switch (objectType) {
         case AMObject.ROLE:
         case AMObject.MANAGED_ROLE:
         case AMObject.FILTERED_ROLE:
             return "465";
         case AMObject.GROUP:
         case AMObject.DYNAMIC_GROUP:
         case AMObject.STATIC_GROUP:
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return "466";
         case AMObject.ORGANIZATION:
             return "467";
         case AMObject.USER:
             return "468";
         case AMObject.ORGANIZATIONAL_UNIT:
             return "469";
         case AMObject.PEOPLE_CONTAINER:
             return "470";
         case AMObject.GROUP_CONTAINER:
             return "471";
         default:
             return "461";
         }
     }
 
     private String getEntryExistsMsgID(int objectType) {
         switch (objectType) {
         case AMObject.ROLE:
         case AMObject.MANAGED_ROLE:
         case AMObject.FILTERED_ROLE:
             return "472";
         case AMObject.GROUP:
         case AMObject.DYNAMIC_GROUP:
         case AMObject.STATIC_GROUP:
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return "473";
         case AMObject.ORGANIZATION:
             return "474";
         case AMObject.USER:
             return "475";
         case AMObject.ORGANIZATIONAL_UNIT:
             return "476";
         case AMObject.PEOPLE_CONTAINER:
             return "477";
         case AMObject.GROUP_CONTAINER:
             return "483";
         default:
             return "462";
         }
     }
 
     /**
      * Method which does some generic processing of the UMSException and throws
      * an appropriate AMException
      * 
      * @param SSOToken
      *            the SSOToken of the user performing the operation
      * @param ue
      *            the UMSException thrown
      * @param defaultErrorCode -
      *            the default error code of the localized message to be used if
      *            a generic error occurs
      * @throws AMException
      *             a suitable AMException with specific message indicating the
      *             error.
      */
     private void processInternalException(SSOToken token, UMSException ue,
             String defaultErrorCode) throws AMException {
         try {
             LDAPException lex = (LDAPException) ue.getRootCause();
             int errorCode = lex.getLDAPResultCode();
             // Check for specific error conditions
             switch (errorCode) {
             case LDAPException.CONSTRAINT_VIOLATION: // LDAP Constraint
                                                         // Voilated
                 throw new AMException(token, "19", ue);
             case LDAPException.TIME_LIMIT_EXCEEDED:
                 throw new AMException(token, "3", ue);
             case LDAPException.SIZE_LIMIT_EXCEEDED:
                 throw new AMException(token, "4", ue);
             case LDAPException.NOT_ALLOWED_ON_RDN:
                 throw new AMException(token, "967", ue);
             case LDAPException.ADMIN_LIMIT_EXCEEDED:
                 throw new AMException(token, "968", ue);
             default:
                 throw new AMException(token, defaultErrorCode, ue);
             }
         } catch (Exception ex) { // Cannot obtain the specific error
             if (ex instanceof AMException) {
                 throw ((AMException) ex);
             } else {
                 throw new AMException(token, defaultErrorCode);
             }
         }
     }
 
     // *************************************************************************
     // Some other Private methods
     // *************************************************************************
     /**
      * Gets the user post plugin instance. Returns a null if plugin not
      * configured could not be loaded. TODO: REMOVE after few releases.
      * Supported through AMCallBack
      */
     public static AMUserEntryProcessed getUserPostPlugin() {
         if (!isUserPluginInitialized) {
             // TODO: REMOVE after Portal moves to new API's
             String implClassName = SystemProperties
                     .get(USER_ENTRY_PROCESSING_IMPL);
             if ((implClassName != null) && (implClassName.length() != 0)) {
                 try {
                     userEntry = (AMUserEntryProcessed) Class.forName(
                             implClassName).newInstance();
                     if (debug.messageEnabled()) {
                         debug.message("DirectoryManager.getUserPostPlugin: "
                                 + "Class " + implClassName + " instantiated.");
                     }
                 } catch (ClassNotFoundException c) {
                     debug.error("DirectoryManager.getUserPostPlugin(): "
                             + "Class not found: " + implClassName, c);
                 } catch (InstantiationException ie) {
                     debug.error("DirectoryManager.getUserPostPlugin(): "
                             + "Unable to instantiate: " + implClassName, ie);
                 } catch (IllegalAccessException le) {
                     debug.error("DirectoryManager.getUserPostPlugin(): "
                             + "IllegalAccessException: " + implClassName, le);
                 }
             }
             isUserPluginInitialized = true;
         }
         return userEntry;
     }
 
     // *************************************************************************
     // All public methods related to DS Operations.
     // *************************************************************************
     /**
      * Checks if the entry exists in the directory.
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            The DN of the entry that needs to be checked
      * @return true if the entryDN exists in the directory, false otherwise
      */
     public boolean doesEntryExists(SSOToken token, String entryDN) {
         try {
             UMSObject.getObject(internalToken, new Guid(entryDN));
         } catch (UMSException ue) {
             /*
              * The very first time when 'Agents' gets selected from the
              * Navigation menu of IS console, there will be no
              * ou=agents,ROOT_SUFFIX in the directory. Only it gets created when
              * a new agent gets created. So do not log this message.
              */
 
             if (entryDN.indexOf("agents") < 0) {
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManager.doesProfileExist(): + "
                             + "Exception caught: ", ue);
                 }
             }
             return false;
         }
         return true;
     }
 
     /**
      * Gets the type of the object given its DN.
      * 
      * @param SSOToken
      *            token a valid SSOToken
      * @param dn
      *            DN of the object whose type is to be known.
      * 
      * @throws AMException
      *             if the data store is unavailable or if the object type is
      *             unknown
      * @throws SSOException
      *             if ssoToken is invalid or expired.
      */
     public int getObjectType(SSOToken token, String dn) throws AMException,
             SSOException {
         return (getObjectType(token, dn, null));
     }
 
     /**
      * Returns the type of the object given its DN.
      * 
      * @param SSOToken
      *            token a valid SSOToken
      * @param dn
      *            DN of the object whose type is to be known.
      * 
      * @throws AMException
      *             if the data store is unavailable or if the object type is
      *             unknown
      * @throws SSOException
      *             if ssoToken is invalid or expired.
      */
     public int getObjectType(SSOToken token, String dn, Map cachedAttributes)
             throws AMException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.getObjectType() Getting object "
                     + "type for: " + dn);
         }
 
         if (!DN.isDN(dn)) {
             throw new AMInvalidDNException(AMSDKBundle.getString("157"), "157");
         }
 
         SSOTokenManager.getInstance().validateToken(token);
         Set objectClasses = null;
 
         // Check if object classes are cached, if not get from directory
         if (cachedAttributes == null
                 || (objectClasses = (Set) cachedAttributes.get("objectclass"))
                                                                        == null)
         {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.getObjectType() Making "
                         + "LDAP call to get objectclass attributes");
             }
             Set attrNames = new HashSet(1);
             attrNames.add("objectclass");
 
             Map attributes = getAttributes(token, dn, attrNames,
                     AMObject.UNDETERMINED_OBJECT_TYPE);
             if (attributes.size() == 1) {
                 objectClasses = (Set) attributes.get("objectclass");
             }
         }
         // Determine the object type
         if (objectClasses != null) {
             Iterator itr = objectClasses.iterator();
             int possibleOT = -1;
             while (itr.hasNext()) {
                 String tStr = (String) itr.next();
                 int objectType = AMObjectClassManager.getObjectType(tStr);
                 if (objectType == AMObject.ROLE) {
                     possibleOT = objectType;
                     continue;
                 } else if (objectType != AMObject.UNKNOWN_OBJECT_TYPE) {
                     return objectType;
                 }
             }
             if (possibleOT != -1) {
                 return possibleOT;
             }
             throw new AMException(AMSDKBundle.getString("156"), "156");
         }
         throw new AMException(AMSDKBundle.getString("151"), "151");
     }
 
     /**
      * Gets the attributes for this entryDN from the corresponding DC Tree node.
      * The attributes are fetched only for Organization entries in DC tree mode.
      * 
      * @param token
      *            a valid SSOToken
      * @param po
      *            the PersistentObject
      * @return an AttrSet of values or null if not found
      * @throws AMException
      *             if error encountered in fetching the DC node attributes.
      */
     public Map getDCTreeAttributes(SSOToken token, String entryDN,
             Set attrNames, boolean byteValues, int objectType)
             throws AMException, SSOException {
         String rootDN = AMStoreConnection.rootSuffix; // Already an RFC String
         if (dcTree.isRequired() && (objectType == AMObject.ORGANIZATION)
                 && (!CommonUtils.formatToRFC(entryDN).equalsIgnoreCase(rootDN)))
         {
             String dcNode = dcTree.getCanonicalDomain(internalToken, entryDN);
             if (dcNode != null) {
                 String names[] = (attrNames == null ? null
                         : (String[]) attrNames.toArray(new String[attrNames
                                 .size()]));
                 AttrSet dcAttrSet = dcTree.getDomainAttributes(internalToken,
                         entryDN, names);
                 return CommonUtils.attrSetToMap(dcAttrSet, byteValues);
             }
         }
         return null;
     }
 
     /**
      * Checks for Compliance related attributes if applicable. The check can be
      * over-ridden by setting the ignoreCompliance to true
      * 
      * @param attrSet
      *            the attrSet to verify
      * @param ignoreCompliance
      *            if true the check will not take place in Compliance mode.
      * @throws AMException
      */
     private void checkComplianceAttributes(AttrSet attrSet,
             boolean ignoreCompliance) throws AMException {
         if (!ignoreCompliance && compl.isComplianceUserDeletionEnabled()) { 
             // Verify for deleted user
             compl.verifyAttributes(attrSet);
         }
     }
 
     public Map getAttributes(SSOToken token, String entryDN, int profileType)
             throws AMException, SSOException {
         boolean ignoreCompliance = true;
         boolean byteValues = false;
         return getAttributes(token, entryDN, ignoreCompliance, byteValues,
                 profileType);
     }
 
     public Map getAttributes(SSOToken token, String entryDN, Set attrNames,
             int profileType) throws AMException, SSOException {
         boolean ignoreCompliance = true;
         boolean byteValues = false;
         return getAttributes(token, entryDN, attrNames, ignoreCompliance,
                 byteValues, profileType);
     }
 
     public Map getAttributesByteValues(SSOToken token, String entryDN,
             int profileType) throws AMException, SSOException {
         // fetch byte values
         boolean byteValues = true;
         boolean ignoreCompliance = true;
         return getAttributes(token, entryDN, ignoreCompliance, byteValues,
                 profileType);
     }
 
     public Map getAttributesByteValues(SSOToken token, String entryDN,
             Set attrNames, int profileType) throws AMException, SSOException {
         // fetch byte values
         boolean byteValues = true;
         boolean ignoreCompliance = true;
         return getAttributes(token, entryDN, attrNames, ignoreCompliance,
                 byteValues, profileType);
     }
 
     /**
      * Gets all attributes corresponding to the entryDN. This method obtains the
      * DC Tree node attributes and also performs compliance related verification
      * checks in compliance mode. Note: In compliance mode you can skip the
      * compliance checks by setting ignoreCompliance to "false".
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the DN of the entry whose attributes need to retrieved
      * @param ignoreCompliance
      *            a boolean value specificying if compliance related entries
      *            need to ignored or not. Ignored if true.
      * @return a Map containing attribute names as keys and Set of values
      *         corresponding to each key.
      * @throws AMException
      *             if an error is encountered in fetching the attributes
      */
     public Map getAttributes(SSOToken token, String entryDN,
             boolean ignoreCompliance, boolean byteValues, int profileType)
             throws AMException, SSOException {
         try {
             // Obtain attributes from directory
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
 
             AttrSet attrSet = po.getAttributes(po.getAttributeNames());
 
             // Perform Compliance related checks
             checkComplianceAttributes(attrSet, ignoreCompliance);
 
             AMHashMap attributes = (AMHashMap) CommonUtils.attrSetToMap(
                     attrSet, byteValues);
             Map dcAttributes = getDCTreeAttributes(token, entryDN, null,
                     byteValues, profileType);
             attributes.copy(dcAttributes);
             return attributes;
         } catch (IllegalArgumentException ie) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.getProfileAttribute(): "
                         + "Unable to get attributes: ", ie);
             }
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("330", locale), "330");
         } catch (UMSException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.getProfileAttribute(): "
                         + "Unable to get attributes: ", e);
             }
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("330", locale), "330");
         }
     }
 
     /**
      * Gets the specific attributes corresponding to the entryDN. This method
      * obtains the DC Tree node attributes and also performs compliance related
      * verification checks in compliance mode. Note: In compliance mode you can
      * skip the compliance checks by setting ignoreCompliance to "false".
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the DN of the entry whose attributes need to retrieved
      * @param attrNames
      *            a Set of names of the attributes that need to be retrieved.
      *            The attrNames should not be null.
      * @param ignoreCompliance
      *            a boolean value specificying if compliance related entries
      *            need to ignored or not. Ignored if true.
      * @return a Map containing attribute names as keys and Set of values
      *         corresponding to each key.
      * @throws AMException
      *             if an error is encountered in fetching the attributes
      */
     public Map getAttributes(SSOToken token, String entryDN, Set attrNames,
             boolean ignoreCompliance, boolean byteValues, int profileType)
             throws AMException, SSOException {
         if (attrNames == null) {
             return getAttributes(token, entryDN, ignoreCompliance, byteValues,
                     profileType);
         }
 
         try {
             // Convert the attrNames to String[]
             String names[] = (String[]) attrNames.toArray(new String[attrNames
                     .size()]);
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
 
             // Perform compliance related checks
             AttrSet attrSet;
             if (!ignoreCompliance && compl.isComplianceUserDeletionEnabled()) {
                 //check for deleted user by getting complaince attributes
                 attrSet = compl.verifyAndGetAttributes(po, names);
             } else {
                 attrSet = po.getAttributes(names);
             }
             AMHashMap attributes = (AMHashMap) CommonUtils.attrSetToMap(
                     attrSet, byteValues);
 
             // Obtain DC tree attributes if applicable
             // Deepa, why are getting all the attributes from DC tree also,
             // Should this be just what are missing? Please verify
             Map dcAttributes = getDCTreeAttributes(token, entryDN, attrNames,
                     byteValues, profileType);
             attributes.copy(dcAttributes);
             return attributes;
         } catch (UMSException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.getProfileAttribute(): "
                         + "Unable to get attributes: ", e);
             }
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("330", locale), "330");
         }
     }
 
     public String getOrgSearchFilter(String entryDN) {
         // The search filter calls here should return a global search
         // filter (not default) if a search template cannot be found for this
         // entryDN. It is a hack as entryDN may not be orgDN, but right now
         // only solution.
         String orgSearchFilter = AMSearchFilterManager.getSearchFilter(
                 AMObject.ORGANIZATION, entryDN, null, true);
         String orgUnitSearchFilter = AMSearchFilterManager.getSearchFilter(
                 AMObject.ORGANIZATIONAL_UNIT, entryDN, null, true);
 
         StringBuffer sb = new StringBuffer();
         sb.append("(|").append(orgSearchFilter).append(orgUnitSearchFilter);
         sb.append(")");
         return sb.toString();
     }
 
     /**
      * Gets the Organization DN for the specified entryDN. If the entry itself
      * is an org, then same DN is returned.
      * <p>
      * <b>NOTE:</b> This method will involve serveral directory searches, hence
      * be cautious of Performance hit
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the entry whose parent Organization is to be obtained
      * @return the DN String of the parent Organization
      * @throws AMException
      *             if an error occured while obtaining the parent Organization
      */
     public String getOrganizationDN(SSOToken token, String entryDN)
             throws AMException {
         if (entryDN.equals("") || !DN.isDN(entryDN)) {
             debug.error("DirectoryManager.getOrganizationDN() Invalid DN: "
                     + entryDN);
             throw new AMException(token, "157");
         }
 
         DN dnObject = new DN(entryDN);
         String organizationDN = null;
         while (organizationDN == null || organizationDN.length() == 0) {
             String childDN = dnObject.toString();
             organizationDN = verifyAndGetOrgDN(token, entryDN, childDN);
             dnObject = dnObject.getParent();
         }
         return organizationDN;
     }
 
     /**
      * Returns attributes from an external data store.
      * 
      * @param token
      *            Single sign on token of user
      * @param entryDN
      *            DN of the entry user is trying to read
      * @param attrNames
      *            Set of attributes to be read
      * @param profileType
      *            Integer determining the type of profile being read
      * @return A Map of attribute-value pairs
      * @throws AMException
      *             if an error occurs when trying to read external datastore
      */
     public Map getExternalAttributes(SSOToken token, String entryDN,
             Set attrNames, int profileType) throws AMException {
         SSOToken internalToken = CommonUtils.getInternalToken();
         String eDN;
         if (profileType == AMObject.USER) {
             eDN = (new DN(entryDN)).getParent().toString();
         } else {
             eDN = entryDN;
         }
         String orgDN;
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         orgDN = amdm.getOrganizationDN(internalToken, eDN);
         return AMCallBackImpl.getAttributes(token, entryDN, attrNames, orgDN);
     }
 
     /**
      * Adds or remove static group DN to or from member attribute
      * 'iplanet-am-static-group-dn'
      * 
      * @param token
      *            SSOToken
      * @param members
      *            set of user DN's
      * @param staticGroupDN
      *            DN of the static group
      * @param toAdd
      *            true to add, false to remove
      * @throws AMException
      *             if there is an internal problem with AM Store.
      */
     public void updateUserAttribute(SSOToken token, Set members,
             String staticGroupDN, boolean toAdd) throws AMException {
 
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.updateUserAttribute(): groupDN:"
                     + staticGroupDN + ", toAdd: " + toAdd + " members: "
                     + members);
         }
 
         Attr attr = new Attr(STATIC_GROUP_DN_ATTRIBUTE, staticGroupDN);
         Iterator itr = members.iterator();
         while (itr.hasNext()) {
             String userDN = (String) itr.next();
             try {
                 PersistentObject po = UMSObject.getObjectHandle(token,
                         new Guid(userDN));
                 if (toAdd) {
                     po.modify(attr, ModSet.ADD);
                 } else {
                     po.modify(attr, ModSet.DELETE);
                 }
                 po.save();
             } catch (UMSException e) {
                 debug.error("DirectoryManager.updateUserAttribute(): Failed"
                         + " while trying to set the static groupDN "
                         + staticGroupDN + " for user: " + userDN, e);
                 throw new AMException(token, "351", e);
             }
         }
     }
 
     // *************************************************************************
     // All un-modified methods from DirectoryManager. (Comments only for
     // reference)
     // *************************************************************************
     private void makeNamingFirst(AttrSet attrSet, String namingAttr,
             String namingValue) {
         int index = attrSet.indexOf(namingAttr);
         if (index == -1) {
             attrSet.add(new Attr(namingAttr, namingValue));
         } else {
             Attr attr = attrSet.elementAt(index);
             attr.removeValue(namingValue);
             String[] values = attr.getStringValues();
             attr = new Attr(namingAttr, namingValue);
             attr.addValues(values);
             attrSet.replace(attr);
         }
     }
 
     /**
      * When an object is being created and attribute sets are being passed UMS
      * does not overrid objectclasses in the attribute set, with the ones from
      * creation template. This method takes care of that.
      * 
      * @param ct
      * @param aSet
      */
     private AttrSet combineOCs(CreationTemplate ct, AttrSet aSet) {
         // UMS creation template will not append default user
         // objectclasses if the "objectclass" attribute is present
         // so we need to append those default objectclass here
         Attr attr = aSet.getAttribute("objectclass");
         // if (attr != null) {
         // TO: To write a separate method for attrSet combine object class
         // values. Need to avoid conversion from string array to sets.
 
         // get default user objectclass from creation template
         Attr defAttr = ct.getAttribute("objectclass");
         Set addOCs = (attr != null) ? CommonUtils.stringArrayToSet(attr
                 .getStringValues()) : new HashSet();
         Set ctOCs = CommonUtils.stringArrayToSet(defAttr.getStringValues());
         Set finalOCs = CommonUtils.combineOCs(addOCs, ctOCs);
         aSet.remove("objectclass");
         Attr finalOCAttr = new Attr("objectclass", (String[]) finalOCs
                 .toArray(new String[finalOCs.size()]));
         aSet.add(finalOCAttr);
         // }
         return aSet;
     }
 
     /**
      * Method to create a user entry
      */
     private void createUser(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMEntryExistsException, AMException {
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
 
         // Invoke the Pre Processing plugin
         String entryDN = AMNamingAttrManager.getNamingAttr(AMObject.USER) + "="
                 + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.USER, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.USER), profileName);
         // Invoke the user password validation plugin
         AMUserPasswordValidationImpl pluginImpl = 
             new AMUserPasswordValidationImpl(token, orgDN);
         try {
             pluginImpl.validate(CommonUtils.attrSetToMap(attrSet));
         } catch (AMException ame) {
             debug.error("DirectoryManager.createUser(): Invalid "
                     + "characters for user", ame);
             throw ame;
         }
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicUser", new Guid(orgDN), TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
 
         // User user = new User(creationTemp, attrSet);
         PersistentObject user = new PersistentObject(creationTemp, attrSet);
         try {
             parentObj.addChild(user);
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createUser(): Insufficient "
                         + "Access rights to create user", e);
             }
             throw new AMException(token, "460");
         } catch (EntryAlreadyExistsException ee) {
             if (compl.isComplianceUserDeletionEnabled()) { // COMPLIANCE
                 // If the existing entry is marked for deletion, then
                 // the error message should be different.
                 compl.checkIfDeletedUser(token, user.getDN());
             }
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createUser() User "
                         + "already exists: ", ee);
             }
             throw new AMEntryExistsException(token, "328", ee);
         } catch (UMSException ue) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createUser(): Internal "
                         + "Error occurred. Unable to create User Entry", ue);
             }
             processInternalException(token, ue, "324");
         }
 
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, user.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.USER, false);
 
         // TODO: REMOVE after Portal moves to new API's
         AMUserEntryProcessed postPlugin = getUserPostPlugin();
         if (postPlugin != null) {
             Map attrMap = CommonUtils.attrSetToMap(attrSet);
             postPlugin.processUserAdd(token, user.getDN(), attrMap);
         }
         AMEmailNotification mailerObj = new AMEmailNotification(user.getDN());
         mailerObj.setUserCreateNotificationList();
         mailerObj.sendUserCreateNotification(attributes);
     }
 
     /**
      * Method to create a user entry
      */
     private void createEntity(SSOToken token, PersistentObject parentObj,
             int objectType, Map attributes, String profileName)
             throws UMSException, AMEntryExistsException, AMException {
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
 
         // Invoke the Pre Processing plugin
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager.getNamingAttr(objectType),
                 profileName);
         String ctName = getCreationTemplateName(objectType);
         if (ctName == null) {
             // Create a user if no CT defined.
             ctName = "BasicUser";
         }
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(ctName,
                 new Guid(orgDN), TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
 
         PersistentObject user = new PersistentObject(creationTemp, attrSet);
         try {
             parentObj.addChild(user);
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntity():"
                         + " Insufficient Access rights to create entity", e);
             }
             throw new AMException(token, "460");
         } catch (EntryAlreadyExistsException ee) {
             if (compl.isComplianceUserDeletionEnabled()) { // COMPLIANCE
                 // If the existing entry is marked for deletion, then
                 // the error message should be different.
                 compl.checkIfDeletedUser(token, user.getDN());
             }
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntity() Entity "
                         + "already exists: ", ee);
             }
             throw new AMEntryExistsException(token, "462", ee);
         } catch (UMSException ue) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntity(): Internal "
                         + "Error occurred. Unable to create User Entry", ue);
             }
             processInternalException(token, ue, "324");
         }
     }
 
     private void createResource(PersistentObject parentObj, Map attributes,
             String profileName) throws UMSException, AMException {
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.RESOURCE), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicResource", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
 
         com.iplanet.ums.Resource resource = new com.iplanet.ums.Resource(
                 creationTemp, attrSet);
         parentObj.addChild(resource);
     }
 
     private void createRole(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         // Invoke the Pre Processing plugin
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager.getNamingAttr(AMObject.ROLE) + "="
                 + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.ROLE, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.ROLE), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicManagedRole", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.ManagedRole role = new com.iplanet.ums.ManagedRole(
                 creationTemp, attrSet);
         parentObj.addChild(role);
 
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, role.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.ROLE, false);
     }
 
     private void createOrganization(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         // Invoke the Pre Processing plugin. Note: we need to obtain
         // the parent org of this organization to obtain the
         // plugin classes for the parent org.
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager
                 .getNamingAttr(AMObject.ORGANIZATION)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl
                 .preProcess(token, entryDN, orgDN, null, attributes,
                         AMCallBackImpl.CREATE, AMObject.ORGANIZATION, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.ORGANIZATION), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         com.iplanet.ums.Organization org = null;
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicOrganization", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
 
         // COMPLIANCE: DCTREE
         if (dcTree.isRequired()) {
             AttrSet[] attrSetArray = dcTree.splitAttrSet(parentObj.getDN(),
                     attrSet);
             org = new com.iplanet.ums.Organization(creationTemp,
                     attrSetArray[0]);
             // create the DC node first. If it fails then the org node will not
             // be created at all. No clean up needed afterwards then.
             dcTree.createDomain(token, new Guid(entryDN), attrSet);
         } else {
             org = new com.iplanet.ums.Organization(creationTemp, attrSet);
         }
         try {
             parentObj.addChild(org);
         } catch (UMSException ue) {
             // clean up DC node
             if (dcTree.isRequired()) {
                 dcTree.removeDomain(token, entryDN);
             }
             if (compl.isComplianceUserDeletionEnabled()) { // COMPLIANCE
                 // If the existing entry is marked for deletion, then
                 // the error message should be different.
                 compl.checkIfDeletedOrg(token, org.getDN());
             }
             throw ue;
         }
 
         if (compl.isAdminGroupsEnabled(org.getDN())) {
             compl.createAdminGroups(token, org);
         }
 
         // If Realms is enabled and is configured in backward compatibitly
         // mode, the corresponding realm must also be created.
         if (ServiceManager.isCoexistenceMode()
                 && ServiceManager.isRealmEnabled()) {
             try {
                 // Check if realm exisits, this throws SMSException
                 // if realm does not exist
                 new OrganizationConfigManager(token, entryDN);
             } catch (SMSException smse) {
                 // Organization does not exist, create it
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManager::createOrganization "
                             + "creating realm: " + org.getDN());
                 }
                 try {
                     OrganizationConfigManager ocm = 
                         new OrganizationConfigManager(token, orgDN);
                     ocm.createSubOrganization(profileName, null);
                 } catch (SMSException se) {
                     if (debug.messageEnabled()) {
                         debug.message("DirectoryManager::createOrganization "
                                 + "unable to create realm: " + org.getDN(), se);
                     }
                 }
             }
         }
 
         // If in legacy mode, add the default services
         if (ServiceManager.isCoexistenceMode()) {
             try {
                 OrganizationConfigManager ocm = new OrganizationConfigManager(
                         token, entryDN);
                 OrganizationConfigManager.loadDefaultServices(token, ocm);
             } catch (SMSException smse) {
                 // Unable to load default services
                 if (debug.warningEnabled()) {
                     debug.warning("DirectoryManager::createOrganization "
                             + "Unable to load services: " + org.getDN());
                 }
             }
         }
 
         // Invoke Post processing impls. Note: orgDN is parent org
         AMCallBackImpl.postProcess(token, org.getDN(), orgDN, null, attributes,
                 AMCallBackImpl.CREATE, AMObject.ORGANIZATION, false);
     }
 
     private void createGroup(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         // Invoke the Pre Processing plugin
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
 
         String entryDN = AMNamingAttrManager.getNamingAttr(AMObject.GROUP)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.GROUP, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.GROUP), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicGroup", new Guid(orgDN), TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.StaticGroup sgroup = new com.iplanet.ums.StaticGroup(
                 creationTemp, attrSet);
         parentObj.addChild(sgroup);
 
         Attr um = attrSet.getAttribute(UNIQUE_MEMBER_ATTRIBUTE);
         if (um != null) {
             String[] values = um.getStringValues();
             Set members = new HashSet();
             for (int i = 0; i < values.length; i++) {
                 members.add(values[i]);
             }
             updateUserAttribute(token, members, sgroup.getDN(), true);
         }
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, sgroup.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.GROUP, false);
     }
 
     private void createAssignDynamicGroup(SSOToken token,
             PersistentObject parentObj, Map attributes, String profileName)
             throws UMSException, AMException {
         // Invoke the Pre Processing plugin
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager.getNamingAttr(AMObject.GROUP)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE,
                 AMObject.ASSIGNABLE_DYNAMIC_GROUP, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.ASSIGNABLE_DYNAMIC_GROUP), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicAssignableDynamicGroup", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.AssignableDynamicGroup adgroup = 
             new com.iplanet.ums.AssignableDynamicGroup(creationTemp, attrSet);
         adgroup.setSearchFilter("(memberof=" + entryDN + ")");
         adgroup.setSearchScope(netscape.ldap.LDAPv2.SCOPE_SUB);
         adgroup.setSearchBase(new Guid(orgDN));
         parentObj.addChild(adgroup);
 
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, adgroup.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE,
                 AMObject.ASSIGNABLE_DYNAMIC_GROUP, false);
     }
 
     private void createDynamicGroup(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         // Invoke the Pre Process plugin
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager.getNamingAttr(AMObject.GROUP)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.DYNAMIC_GROUP,
                 false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.DYNAMIC_GROUP), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicDynamicGroup", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.DynamicGroup dgroup = new com.iplanet.ums.DynamicGroup(
                 creationTemp, attrSet);
         String filter = dgroup.getSearchFilter();
         if (LDAPUrl.defaultFilter.equalsIgnoreCase(filter)) {
             dgroup.setSearchFilter(AMSearchFilterManager.getSearchFilter(
                     AMObject.USER, orgDN));
         }
         dgroup.setSearchScope(netscape.ldap.LDAPv2.SCOPE_SUB);
         dgroup.setSearchBase(new Guid(orgDN));
         parentObj.addChild(dgroup);
 
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, dgroup.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.DYNAMIC_GROUP,
                 false);
     }
 
     private void createPeopleContainer(PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.PEOPLE_CONTAINER), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicPeopleContainer", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.PeopleContainer pc = new PeopleContainer(creationTemp,
                 attrSet);
         parentObj.addChild(pc);
     }
 
     private void createOrganizationalUnit(SSOToken token,
             PersistentObject parentObj, Map attributes, String profileName)
             throws UMSException, AMException {
         // Invoke the Pre Post Plugins
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager
                 .getNamingAttr(AMObject.ORGANIZATIONAL_UNIT)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE,
                 AMObject.ORGANIZATIONAL_UNIT, false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.ORGANIZATIONAL_UNIT), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicOrganizationalUnit", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         com.iplanet.ums.OrganizationalUnit ou = 
             new com.iplanet.ums.OrganizationalUnit(creationTemp, attrSet);
         parentObj.addChild(ou);
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, ou.getDN(), orgDN, null, attributes,
                 AMCallBackImpl.CREATE, AMObject.ORGANIZATIONAL_UNIT, false);
     }
 
     private void createGroupContainer(PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.GROUP_CONTAINER), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicGroupContainer", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
 
         OrganizationalUnit gc = new OrganizationalUnit(creationTemp, attrSet);
         parentObj.addChild(gc);
     }
 
     private void createFilteredRole(SSOToken token, PersistentObject parentObj,
             Map attributes, String profileName) throws UMSException,
             AMException {
         // Invoke the Pre Processing plugin
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, parentObj.getDN());
         String entryDN = AMNamingAttrManager
                 .getNamingAttr(AMObject.FILTERED_ROLE)
                 + "=" + profileName + "," + parentObj.getDN();
         attributes = AMCallBackImpl.preProcess(token, entryDN, orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.FILTERED_ROLE,
                 false);
 
         AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
         makeNamingFirst(attrSet, AMNamingAttrManager
                 .getNamingAttr(AMObject.FILTERED_ROLE), profileName);
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate creationTemp = tempMgr.getCreationTemplate(
                 "BasicFilteredRole", new Guid(orgDN),
                 TemplateManager.SCOPE_ANCESTORS);
         attrSet = combineOCs(creationTemp, attrSet);
         if (!attrSet.contains(FilteredRole.FILTER_ATTR_NAME)) {
             Attr attr = new Attr(FilteredRole.FILTER_ATTR_NAME,
                     AMSearchFilterManager.getSearchFilter(
                             AMObject.USER, orgDN));
             attrSet.add(attr);
         }
 
         com.iplanet.ums.FilteredRole frole = new com.iplanet.ums.FilteredRole(
                 creationTemp, attrSet);
         parentObj.addChild(frole);
         // Invoke Post processing impls
         AMCallBackImpl.postProcess(token, frole.getDN(), orgDN, null,
                 attributes, AMCallBackImpl.CREATE, AMObject.FILTERED_ROLE,
                 false);
     }
 
     /**
      * Create an entry in the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryName
      *            name of the entry (naming value), e.g. "sun.com", "manager"
      * @param objectType
      *            Profile Type, ORGANIZATION, AMObject.ROLE, AMObject.USER, etc.
      * @param parentDN
      *            the parent DN
      * @param attrSet
      *            the initial attribute set for creation
      */
     public void createEntry(SSOToken token, String entryName, int objectType,
             String parentDN, Map attributes) throws AMEntryExistsException,
             AMException {
         try {
             if (entryName == null || entryName.equals("")) {
                 throw new AMException(token, "320");
             } else if (parentDN == null) {
                 throw new AMException(token, "322");
             }
             // tmpDN to be used only when validating since the method
             // expects a DN.
             String tmpDN = AMNamingAttrManager.getNamingAttr(objectType) + "="
                     + entryName + "," + parentDN;
             validateAttributeUniqueness(tmpDN, objectType, true, attributes);
             // Get handle to the parent object
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     parentDN));
 
             switch (objectType) {
             case AMObject.USER:
                 createUser(token, po, attributes, entryName);
                 break;
             case AMObject.MANAGED_ROLE:
             case AMObject.ROLE: // same as MANAGED ROLE
                 createRole(token, po, attributes, entryName);
                 break;
             case AMObject.ORGANIZATION:
                 createOrganization(token, po, attributes, entryName);
                 break;
             case AMObject.STATIC_GROUP:
             case AMObject.GROUP:
                 createGroup(token, po, attributes, entryName);
                 break;
             case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
                 createAssignDynamicGroup(token, po, attributes, entryName);
                 break;
             case AMObject.DYNAMIC_GROUP:
                 createDynamicGroup(token, po, attributes, entryName);
                 break;
             case AMObject.PEOPLE_CONTAINER:
                 createPeopleContainer(po, attributes, entryName);
                 break;
             case AMObject.ORGANIZATIONAL_UNIT:
                 createOrganizationalUnit(token, po, attributes, entryName);
                 break;
             case AMObject.GROUP_CONTAINER:
                 createGroupContainer(po, attributes, entryName);
                 break;
             case AMObject.FILTERED_ROLE:
                 createFilteredRole(token, po, attributes, entryName);
                 break;
             case AMObject.RESOURCE:
                 createResource(po, attributes, entryName);
                 break;
             case AMObject.UNDETERMINED_OBJECT_TYPE:
             case AMObject.UNKNOWN_OBJECT_TYPE:
                 throw new AMException(token, "326");
             default: // Supported generic type
                 createEntity(token, po, objectType, attributes, entryName);
             }
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntry() "
                         + "Insufficient access rights to create entry: "
                         + entryName, e);
             }
             throw new AMException(token, "460");
         } catch (EntryAlreadyExistsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntry() Entry: "
                         + entryName + "already exists: ", e);
             }
             String msgid = getEntryExistsMsgID(objectType);
             String name = getEntryName(e);
             Object args[] = { name };
             throw new AMException(AMSDKBundle.getString(msgid, args), msgid,
                     args);
         } catch (UMSException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createEntry() Unable to "
                         + "create entry: " + entryName, e);
             }
             throw new AMException(token, "324", e);
         }
     }
 
     private void processPreDeleteCallBacks(SSOToken token, String entryDN,
             Map attributes, String organizationDN, int objectType,
             boolean softDelete) throws AMException, SSOException {
         // Call pre-processing user impls
         if (objectType != AMObject.PEOPLE_CONTAINER
                 && objectType != AMObject.GROUP_CONTAINER) {
             String parentOrgDN = organizationDN;
             if (objectType == AMObject.ORGANIZATION
                     || objectType == AMObject.ORGANIZATIONAL_UNIT) {
                 // Get the parent oganization for this org.
                 DN rootDN = new DN(AMStoreConnection.rootSuffix);
                 DN currentOrgDN = new DN(organizationDN);
                 if (!rootDN.equals(currentOrgDN)) {
                     String parentDN = (new DN(organizationDN)).getParent()
                             .toString();
                     if (amdm == null)
                         amdm = AMDirectoryWrapper.getInstance();
                     parentOrgDN = amdm.getOrganizationDN(internalToken,
                             parentDN);
                 }
             }
             if (attributes == null) { // Not already retrieved
                 attributes = getAttributes(token, entryDN, objectType);
             }
             AMCallBackImpl.preProcess(token, entryDN, parentOrgDN, attributes,
                     null, AMCallBackImpl.DELETE, objectType, softDelete);
         }
     }
 
     private void processPostDeleteCallBacks(SSOToken token, String entryDN,
             Map attributes, String organizationDN, int objectType,
             boolean softDelete) throws AMException {
         // Invoke post processing impls
         if (objectType != AMObject.PEOPLE_CONTAINER
                 && objectType != AMObject.GROUP_CONTAINER) {
             String parentOrgDN = organizationDN;
             if (objectType == AMObject.ORGANIZATION
                     || objectType == AMObject.ORGANIZATIONAL_UNIT) {
                 // Get the parent oganization for this org.
                 DN rootDN = new DN(AMStoreConnection.rootSuffix);
                 DN currentOrgDN = new DN(organizationDN);
                 if (!rootDN.equals(currentOrgDN)) {
                     String parentDN = (new DN(organizationDN)).getParent()
                             .toString();
                     if (amdm == null)
                         amdm = AMDirectoryWrapper.getInstance();
                     parentOrgDN = amdm.getOrganizationDN(internalToken,
                             parentDN);
                 }
             }
             AMCallBackImpl.postProcess(token, entryDN, parentOrgDN, attributes,
                     null, AMCallBackImpl.DELETE, objectType, softDelete);
         }
     }
 
     /**
      * Remove an entry from the directory.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            dn of the profile to be removed
      * @param objectType
      *            profile type
      * @param recursive
      *            if true, remove all sub entries & the object
      * @param softDelete
      *            Used to let pre/post callback plugins know that this delete is
      *            either a soft delete (marked for deletion) or a purge/hard
      *            delete itself, otherwise, remove the object only
      */
     public void removeEntry(SSOToken token, String entryDN, int objectType,
             boolean recursive, boolean softDelete) throws AMException,
             SSOException {
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.removeEntry(): Removing: "
                     + entryDN + " & recursive: " + recursive);
         }
         if (recursive) {
             // will list all entries in the sub-tree and delete them
             // one by one.
             removeSubtree(token, entryDN, softDelete);
         } else {
             removeSingleEntry(token, entryDN, objectType, softDelete);
         }
 
         // If Organization is deleted, and if realms in enabled and is
         // configured in backward compatibitly mode, the corresponding
         // realm must also be deleted.
         if (objectType == AMObject.ORGANIZATION
                 && ServiceManager.isCoexistenceMode()
                 && ServiceManager.isRealmEnabled()) {
             try {
                 // Check if realm exisits, this throws SMSException
                 // if realm does not exist
                 OrganizationConfigManager ocm = new OrganizationConfigManager(
                         token, entryDN);
                 // Since the above did not throw an exception, the
                 // realm must be deleted
                 ocm.deleteSubOrganization(null, recursive);
             } catch (SMSException smse) {
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManager::removeEntry unable to "
                             + "delete corresponding realm: " + entryDN);
                 }
             }
         }
     }
 
     /**
      * Private method to delete a single entry
      */
     private void removeSingleEntry(SSOToken token, String entryDN,
             int objectType, boolean softDelete) throws AMException,
             SSOException {
 
         Map attributes = null;
         AMEmailNotification mailer = null;
         String eDN = entryDN;
         if (objectType == AMObject.USER) {
             eDN = ((new DN(entryDN)).getParent()).toRFCString();
         }
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, eDN);
         try {
 
             if (objectType == AMObject.USER) {
                 // Extract a delete notification list
                 mailer = new AMEmailNotification(entryDN);
                 mailer.setUserDeleteNotificationList();
             }
 
             if ((getUserPostPlugin() != null)
                     || (mailer != null && mailer
                             .isPresentUserDeleteNotificationList())) {
                 // Obtain the attributes needed to send notification and also
                 // call backs as these won't be available after deletion
                 attributes = getAttributes(token, entryDN, objectType);
             }
 
             processPreDeleteCallBacks(token, entryDN, attributes, orgDN,
                     objectType, softDelete);
 
             // if (recursive) {
             // deleteSubtree(token, entryDN, softDelete);
             // } else {
             if (dcTree.isRequired()) {
                 String rfcDN = CommonUtils.formatToRFC(entryDN);
                 dcTree.removeDomain(internalToken, rfcDN);
             }
             Guid guid = new Guid(entryDN);
             UMSObject.removeObject(token, guid);
             // }
         } catch (AccessRightsException e) {
             debug.error("DirectoryManager.removeEntry() Insufficient access"
                     + " rights to remove entry: " + entryDN, e);
             throw new AMException(token, "460");
         } catch (EntryNotFoundException e) {
             String entry = getEntryName(e);
             debug.error("DirectoryManager.removeEntry() Entry not found: "
                     + entry, e);
             String msgid = getEntryNotFoundMsgID(objectType);
             Object args[] = { entry };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString(msgid, args, locale),
                     msgid, args);
         } catch (UMSException e) {
             debug.error("DirectoryManager.removeEntry() Unable to remove: "
                     + " Internal error occurred: ", e);
             throw new AMException(token, "325", e);
         }
         processPostDeleteCallBacks(token, entryDN, attributes, orgDN,
                 objectType, softDelete);
 
         if (objectType == AMObject.USER) {
             AMUserEntryProcessed postPlugin = getUserPostPlugin();
             if (postPlugin != null) {
                 // TODO: Remove after deprecating interface
                 postPlugin.processUserDelete(token, entryDN, attributes);
             }
             if (mailer != null && mailer.isPresentUserDeleteNotificationList())
             {
                 mailer.sendUserDeleteNotification(attributes);
             }
         }
     }
 
     /**
      * Private method used by "removeEntry" to delete an entire subtree
      */
     private void removeSubtree(SSOToken token, String entryDN,
             boolean softDelete) throws AMException, SSOException {
         int type = AMObject.UNKNOWN_OBJECT_TYPE;
         try {
             Guid guid = new Guid(entryDN);
             PersistentObject po = UMSObject
                     .getObjectHandle(internalToken, guid);
 
             // first get all the children of the object
             SearchControl control = new SearchControl();
             control.setSearchScope(SearchControl.SCOPE_SUB);
             String searchFilter = 
                 "(|(objectclass=*)(objectclass=ldapsubEntry))";
 
             List list = new ArrayList();
             // get number of RDNs in the entry itself
             int entryRDNs = (new DN(entryDN)).countRDNs();
             // to count maximum level of RDNs in the search return
             int maxRDNCount = entryRDNs;
             // go through all search results, add DN to the list, and
             // set the maximun RDN count, will be used to remove DNs
             SearchResults children = po.getChildren(searchFilter, control);
             while (children.hasMoreElements()) {
                 PersistentObject object = children.next();
                 DN dn = new DN(object.getDN());
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManager.removeEntry(): "
                             + "found child: " + object.getDN());
                 }
                 int count = dn.countRDNs();
                 if (count > maxRDNCount) {
                     maxRDNCount = count;
                 }
                 list.add(dn);
             }
 
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.removeEntry(): max " + "RDNs: "
                         + maxRDNCount);
             }
 
             // go through all search results, delete entries from the
             // bottom up, starting from entries whose's RDN count
             // equals the maxRDNCount
             // TODO : If the list has too many entries, then the multiple
             // iteration in the inner for loop may be the bottleneck.
             // One enhancement to the existing algorithm is to store all
             // the entries by level in a different List. Per Sai's comments
             int len = list.size();
             for (int i = maxRDNCount; i >= entryRDNs; i--) {
                 for (int j = 0; j < len; j++) {
                     DN dn = (DN) list.get(j);
                     // check if we need delete it now
                     if (dn.countRDNs() == i) {
                         // remove the entry
                         if (debug.messageEnabled()) {
                             debug.message("DirectoryManager."
                                     + "removeEntry(): del " + dn.toRFCString());
                         }
                         String rfcDN = dn.toRFCString();
                         type = AMObject.UNKNOWN_OBJECT_TYPE;
                         try {
                             type = getObjectType(internalToken, rfcDN);
                         } catch (AMException ae) {
                             // Not a managed type, just delete it.
                             Guid g = new Guid(rfcDN);
                             UMSObject.removeObject(token, g);
                         }
                         // Managed type. Might need pre/post callbacks.
                         // Do a non-recursive delete
                         if (type != AMObject.UNKNOWN_OBJECT_TYPE
                                 && type != AMObject.UNDETERMINED_OBJECT_TYPE) {
                             try {
                                 removeSingleEntry(token, rfcDN, type,
                                         softDelete);
                             } catch (AMPreCallBackException amp) {
                                 debug.error("DirectoryManager.removeSubTree: " +
                                         "Aborting delete of: " + rfcDN + 
                                         " due to pre-callback exception", amp);
                             }
                         }
 
                         // remove the deleted entry from the list
                         list.remove(j);
                         // move back pointer, as current element is removed
                         j--;
                         // reduce list length
                         len--;
                     }
                 }
             }
 
         } catch (AccessRightsException e) {
             debug.error("DirectoryManager.removeEntry() Insufficient access"
                     + " rights to remove entry: " + entryDN, e);
             throw new AMException(token, "460");
         } catch (EntryNotFoundException e) {
             String entry = getEntryName(e);
             debug.error("DirectoryManager.removeEntry() Entry not found: "
                     + entry, e);
             String msgid = getEntryNotFoundMsgID(type);
             Object args[] = { entry };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString(msgid, args, locale),
                     msgid, args);
         } catch (UMSException e) {
             debug.error("DirectoryManager.removeEntry() Unable to remove: "
                     + " Internal error occurred: ", e);
             throw new AMException(token, "325", e);
         }
     }
 
     /**
      * Remove group admin role
      * 
      * @param token
      *            SSOToken of the caller
      * @param dn
      *            group DN
      * @param recursive
      *            true to delete all admin roles for all sub groups or sub
      *            people container
      */
     public void removeAdminRole(SSOToken token, String dn, boolean recursive)
             throws SSOException, AMException {
         SSOTokenManager.getInstance().validateToken(token);
 
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.removeAdminRole() dn: " + dn
                     + " recursive: " + recursive);
         }
         // first find out the admin role dn for the group
         DN ldapDN = new DN(dn);
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(token, ldapDN.getParent()
                 .toString());
         String newdn = dn.replace(',', '_');
         String roleNameAttr = AMNamingAttrManager.getNamingAttr(AMObject.ROLE);
         String roleDN = (new StringBuffer().append(roleNameAttr).append("=")
                 .append(newdn).append(",").append(orgDN)).toString();
 
         Set adminRoles = Collections.EMPTY_SET;
         if (recursive) {
             String roleSearchFilter = AMSearchFilterManager.getSearchFilter(
                     AMObject.ROLE, orgDN);
             StringBuffer sb = new StringBuffer();
             sb.append("(&").append(roleSearchFilter).append("(");
             sb.append(roleNameAttr).append("=*").append(newdn).append("))");
             adminRoles = search(token, orgDN, sb.toString(),
                     SearchControl.SCOPE_ONE);
         } else {
             adminRoles = new HashSet();
             adminRoles.add(roleDN);
         }
 
         Iterator iter = adminRoles.iterator();
         while (iter.hasNext()) {
             String adminRoleDN = (String) iter.next();
             // remove all members from the role
             try {
                 ManagedRole roleObj = (ManagedRole) UMSObject.getObject(token,
                         new Guid(adminRoleDN));
                 roleObj.removeAllMembers();
                 // removeEntry(token, adminRoleDN, AMObject.ROLE, false, false);
                 AMStoreConnection amsc = new AMStoreConnection(internalToken);
                 AMRole role = amsc.getRole(adminRoleDN);
                 role.delete(recursive);
             } catch (Exception e) {
                 if (debug.messageEnabled()) {
                     debug.message(
                             "DirectoryManager.removeAdminRole() Unable to "
                                     + " admin roles:", e);
                 }
             }
         }
     }
 
     /**
      * convert search results to a set of DNS
      */
     private Set searchResultsToSet(SearchResults results) throws UMSException {
         Set set = new OrderedSet();
         if (results != null) {
             while (results.hasMoreElements()) {
                 PersistentObject one = results.next();
                 set.add(one.getGuid().toString());
             }
         }
         return set;
     }
 
     /**
      * Searches the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the entry to start the search with
      * @param searchFilter
      *            search filter
      * @param searchScope
      *            search scope, BASE, ONELEVEL or SUBTREE
      * @return Set set of matching DNs
      */
     public Set search(SSOToken token, String entryDN, String searchFilter,
             int searchScope) throws AMException {
         Set resultSet = Collections.EMPTY_SET;
         try {
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
             SearchControl control = new SearchControl();
             control.setSearchScope(searchScope);
             SearchResults results = po.search(searchFilter, control);
             resultSet = searchResultsToSet(results);
         } catch (UMSException ue) {
             LDAPException lex = (LDAPException) ue.getRootCause();
             int errorCode = lex.getLDAPResultCode();
             if (retryErrorCodes.contains("" + errorCode)) {
                 throw new AMException(token, Integer.toString(errorCode));
             }
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.search(token:, entryDN: "
                         + entryDN + ", searchFilter: " + searchFilter
                         + "searchScope: " + searchScope + " error occurred: ",
                         ue);
             }
             processInternalException(token, ue, "341");
         }
         return resultSet;
     }
 
     /**
      * convert search results to a AMSearchResults object TODO: Refactor code
      */
     private AMSearchResults getSearchResults(SearchResults results,
             SortKey skey, String[] attrNames, Collator collator,
             boolean getAllAttrs) throws UMSException {
         TreeMap tm = null;
         TreeSet tmpTreeSet = null;
         if (skey != null) {
             tm = new TreeMap(collator);
             tmpTreeSet = new TreeSet();
         }
 
         Set set = new OrderedSet();
 
         Map map = new HashMap();
         int errorCode = AMSearchResults.SUCCESS;
         try {
             if (results != null) {
                 while (results.hasMoreElements()) {
                     PersistentObject po = results.next();
                     String dn = po.getGuid().toString();
                     if (tm != null) {
                         Attr attr = po.getAttribute(skey.attributeName);
                         if (attr != null) {
                             String attrValue = attr.getStringValues()[0];
                             Object obj = tm.get(attrValue);
                             if (obj == null) {
                                 tm.put(attrValue, dn);
                             } else if (obj instanceof java.lang.String) {
                                 TreeSet tmpSet = new TreeSet();
                                 tmpSet.add(obj);
                                 tmpSet.add(dn);
                                 tm.put(attrValue, tmpSet);
                             } else {
                                 ((TreeSet) obj).add(dn);
                             }
                         } else {
                             tmpTreeSet.add(dn);
                         }
                     } else {
                         set.add(dn);
                     }
 
                     AttrSet attrSet = new AttrSet();
                     if (attrNames != null) {
                         // Support for multiple return values
                         attrSet = po.getAttributes(attrNames, true);
                     } else {
                         /*
                          * Support for multiple return values when attribute
                          * names are not passed as part of the return
                          * attributes. This boolean check is to make sure user
                          * has set the setAllReturnAttributes flag in
                          * AMSearchControl in order to get all attributes or
                          * not.
                          */
                         if (getAllAttrs) {
                             attrSet = po.getAttributes(po.getAttributeNames(),
                                     true);
                         }
                     }
                     map.put(dn, CommonUtils.attrSetToMap(attrSet));
                 }
             }
         } catch (SizeLimitExceededException slee) {
             errorCode = AMSearchResults.SIZE_LIMIT_EXCEEDED;
         } catch (TimeLimitExceededException tlee) {
             errorCode = AMSearchResults.TIME_LIMIT_EXCEEDED;
         }
 
         Integer count = (Integer) results
                 .get(SearchResults.VLVRESPONSE_CONTENT_COUNT);
         int countValue;
         if (count == null) {
             countValue = AMSearchResults.UNDEFINED_RESULT_COUNT;
         } else {
             countValue = count.intValue();
         }
 
         if (tm != null) {
             Object[] values = tm.values().toArray();
             int len = values.length;
             if (skey.reverse) {
                 for (int i = len - 1; i >= 0; i--) {
                     Object obj = values[i];
                     if (obj instanceof java.lang.String) {
                         set.add(obj);
                     } else {
                         set.addAll((Collection) obj);
                     }
                 }
             } else {
                 for (int i = 0; i < len; i++) {
                     Object obj = values[i];
                     if (obj instanceof java.lang.String) {
                         set.add(obj);
                     } else {
                         set.addAll((Collection) obj);
                     }
                 }
             }
 
             Iterator iter = tmpTreeSet.iterator();
             while (iter.hasNext()) {
                 set.add(iter.next());
             }
         }
 
         AMSearchResults searchResults = new AMSearchResults(countValue, set,
                 errorCode, map);
 
         return searchResults;
     }
 
     // RENAME from searchUsingSearchControl => search()
     /**
      * Search the Directory
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the entry to start the search with
      * @param searchFilter
      *            search filter
      * @param SearchControl
      *            search control defining the VLV indexes and search scope
      * @return Set set of matching DNs
      */
     public AMSearchResults search(SSOToken token, String entryDN,
             String searchFilter, SearchControl searchControl,
             String attrNames[]) throws AMException {
         AMSearchResults amResults = null;
         try {
             SortKey[] skeys = searchControl.getSortKeys();
             SortKey skey = null;
             if (skeys != null && skeys.length > 0
                     && skeys[0].attributeName != null) {
                 skey = skeys[0];
             }
             String userLocale = CommonUtils.getUserLocale(token);
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.search() search with "
                         + "searchcontrol locale = " + userLocale);
             }
             Collator collator = Collator.getInstance(Locale
                     .getLocale(userLocale));
 
             SearchControl sc;
             if (skey != null) {
                 sc = new SearchControl();
                 sc.setMaxResults(searchControl.getMaxResults());
                 sc.setSearchScope(searchControl.getSearchScope());
                 sc.setTimeOut(searchControl.getTimeOut());
             } else {
                 sc = searchControl;
             }
 
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
             SearchResults results;
             if (attrNames == null) {
                 if (skey == null) {
                     results = po.search(searchFilter, sc);
                 } else {
                     String[] tmpAttrNames = { skey.attributeName };
                     results = po.search(searchFilter, tmpAttrNames, sc);
                 }
             } else {
                 if (skey == null) {
                     results = po.search(searchFilter, attrNames, sc);
                 } else {
                     String[] tmpAttrNames = new String[attrNames.length + 1];
                     System.arraycopy(attrNames, 0, tmpAttrNames, 0,
                             attrNames.length);
                     tmpAttrNames[attrNames.length] = skey.attributeName;
                     results = po.search(searchFilter, tmpAttrNames, sc);
                 }
             }
             amResults = getSearchResults(results, skey, attrNames, collator, sc
                     .isGetAllReturnAttributesEnabled());
         } catch (UMSException ue) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.search() with search "
                         + "control entryDN: " + entryDN + " Search Filter: "
                         + searchFilter + " Error occurred: ", ue);
             }
             processInternalException(token, ue, "341");
         }
         return amResults;
     }
 
     /**
      * Get members for roles, dynamic group or static group
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the role or group
      * @param objectType
      *            objectType of the target object, AMObject.ROLE or
      *            AMObject.GROUP
      * @return Set Member DNs
      */
     public Set getMembers(SSOToken token, String entryDN, int objectType)
             throws AMException {
         try {
             SearchResults results;
             switch (objectType) {
             case AMObject.ROLE:
             case AMObject.MANAGED_ROLE:
                 ManagedRole role = (ManagedRole) UMSObject.getObject(token,
                         new Guid(entryDN));
                 results = role.getMemberIDs();
                 return searchResultsToSet(results);
             case AMObject.FILTERED_ROLE:
                 FilteredRole filteredRole = (FilteredRole) UMSObject.getObject(
                         token, new Guid(entryDN));
                 results = filteredRole.getMemberIDs();
                 return searchResultsToSet(results);
             case AMObject.GROUP:
             case AMObject.STATIC_GROUP:
                 StaticGroup group = (StaticGroup) UMSObject.getObject(token,
                         new Guid(entryDN));
                 results = group.getMemberIDs();
                 return searchResultsToSet(results);
             case AMObject.DYNAMIC_GROUP:
                 DynamicGroup dynamicGroup = (DynamicGroup) UMSObject.getObject(
                         token, new Guid(entryDN));
                 results = dynamicGroup.getMemberIDs();
                 return searchResultsToSet(results);
             case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
                 // TODO: See if it works after removing this workaround
                 // fake object to get around UMS problem.
                 // UMS AssignableDynamicGroup has a class resolver, it is
                 // added to resolver list in static block. So I need to
                 // construct a dummy AssignableDynamicGroup
                 AssignableDynamicGroup adgroup = 
                     (AssignableDynamicGroup) UMSObject.getObject(
                                                 token, new Guid(entryDN));
                 results = adgroup.getMemberIDs();
                 return searchResultsToSet(results);
             default:
                 throw new AMException(token, "114");
             }
         } catch (EntryNotFoundException e) {
             debug.error(
                     "DirectoryManager.getMembers() entryDN " + entryDN
                             + " objectType: " + objectType
                             + " Unable to get members: ", e);
             String msgid = getEntryNotFoundMsgID(objectType);
             String entryName = getEntryName(e);
             Object args[] = { entryName };
             throw new AMException(AMSDKBundle.getString(msgid, args), msgid,
                     args);
         } catch (UMSException e) {
             debug.error(
                     "DirectoryManager.getMembers() entryDN " + entryDN
                             + " objectType: " + objectType
                             + " Unable to get members: ", e);
             LDAPException le = (LDAPException) e.getRootCause();
             if (le != null
                     && (le.getLDAPResultCode() ==
                             LDAPException.SIZE_LIMIT_EXCEEDED 
                         || le.getLDAPResultCode() == 
                             LDAPException.ADMIN_LIMIT_EXCEEDED)) 
             {
                 throw new AMException(token, "505", e);
             }
             throw new AMException(token, "454", e);
         }
     }
 
     /**
      * Renames an entry. Currently used for only user renaming
      * 
      * @param token
      *            the sso token
      * @param objectType
      *            the type of entry
      * @param entryDN
      *            the entry DN
      * @param newName
      *            the new name (i.e., if RDN is cn=John, the value passed should
      *            be "John"
      * @param deleteOldName
      *            if true the old name is deleted otherwise it is retained.
      * @return new <code>DN</code> of the renamed entry
      * @throws AMException
      *             if the operation was not successful
      */
     public String renameEntry(SSOToken token, int objectType, String entryDN,
             String newName, boolean deleteOldName) throws AMException {
         try {
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
             String newRDN = AMNamingAttrManager.getNamingAttr(objectType) + "="
                     + newName;
             po.rename(newRDN, deleteOldName);
             return po.getDN();
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.renameEntry(): User does "
                         + "not have sufficient access rights ", e);
             }
             throw new AMException(token, "460");
         } catch (EntryNotFoundException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.renameEntry(): Entry not "
                         + "found: ", e);
             }
             String msgid = getEntryNotFoundMsgID(objectType);
             String entryName = getEntryName(e);
             Object args[] = { entryName };
             throw new AMException(AMSDKBundle.getString(msgid, args), msgid,
                     args);
         } catch (UMSException ume) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.renameEntry(): Unable to "
                         + "rename entry: ", ume);
             }
             throw new AMException(token, "360");
         }
     }
 
     // TODO: Need to see if the split attributes to a another way of doing
     // this instead of passing an array. Need to see if the domain status can
     // also be set along with other attributes. Also DCTree code needs to use
     // Maps instead of attrSet.
     private Map setDCTreeAttributes(SSOToken token, String entryDN,
             Map attributes, int objectType) throws AMException {
         if (objectType == AMObject.ORGANIZATION && dcTree.isRequired()
                 && !entryDN.equals(AMStoreConnection.rootSuffix)) {
             AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
             String status = attrSet.getValue(INET_DOMAIN_STATUS_ATTRIBUTE);
             if (status != null) {
                 dcTree.updateDomainStatus(token, entryDN, status);
             }
             // split up the attrs to be set on DC node and organization node.
             AttrSet[] attrArray = dcTree.splitAttrSet(entryDN, attrSet);
             attrSet = attrArray[0];
             attributes = CommonUtils.attrSetToMap(attrSet);
             AttrSet domAttrSet = attrArray[1];
             dcTree.setDomainAttributes(token, entryDN, domAttrSet);
         }
         return attributes;
     }
 
     private void processPostModifyCallBacks(SSOToken token, String entryDN,
             Map oldAttributes, Map attributes, String organizationDN,
             int objectType) throws AMException {
         if (objectType != AMObject.PEOPLE_CONTAINER
                 && objectType != AMObject.GROUP_CONTAINER) {
             String parentOrgDN = organizationDN;
             if (objectType == AMObject.ORGANIZATION
                     || objectType == AMObject.ORGANIZATIONAL_UNIT) {
                 // Get the parent oganization for this org.
                 // Get the parent oganization for this org.
                 DN rootDN = new DN(AMStoreConnection.rootSuffix);
                 DN currentOrgDN = new DN(organizationDN);
                 if (!rootDN.equals(currentOrgDN)) {
                     String parentDN = (new DN(organizationDN)).getParent()
                             .toString();
                     if (amdm == null)
                         amdm = AMDirectoryWrapper.getInstance();
                     parentOrgDN = amdm.getOrganizationDN(internalToken,
                             parentDN);
                 }
             }
             AMCallBackImpl.postProcess(token, entryDN, parentOrgDN,
                     oldAttributes, attributes, AMCallBackImpl.MODIFY,
                     objectType, false);
         }
     }
 
     private Map processPreModifyCallBacks(SSOToken token, String entryDN,
             Map oldAttributes, Map attributes, String organizationDN,
             int objectType) throws AMException, SSOException {
         if (objectType != AMObject.PEOPLE_CONTAINER
                 && objectType != AMObject.GROUP_CONTAINER) {
             String parentOrgDN = organizationDN;
             if (objectType == AMObject.ORGANIZATION
                     || objectType == AMObject.ORGANIZATIONAL_UNIT) {
                 // Get the parent oganization for this org.
                 DN rootDN = new DN(AMStoreConnection.rootSuffix);
                 DN currentOrgDN = new DN(organizationDN);
                 if (!rootDN.equals(currentOrgDN)) {
                     String parentDN = (new DN(organizationDN)).getParent()
                             .toString();
                     if (amdm == null)
                         amdm = AMDirectoryWrapper.getInstance();
                     parentOrgDN = amdm.getOrganizationDN(internalToken,
                             parentDN);
                 }
             }
             if (oldAttributes == null) {
                 Set attrNames = attributes.keySet();
                 oldAttributes = getAttributes(token, entryDN, attrNames,
                         objectType);
             }
             attributes = AMCallBackImpl.preProcess(token, entryDN, parentOrgDN,
                     oldAttributes, attributes, AMCallBackImpl.MODIFY,
                     objectType, false);
         }
         return attributes;
     }
 
     private void modifyPersistentObject(PersistentObject po, Attr attr,
             boolean isAdd, boolean isDelete) {
         if (isAdd) { // Add attribute
             po.modify(attr, ModSet.ADD);
         } else if (isDelete) { // Remove attribute
             po.modify(attr, ModSet.DELETE);
         } else { // Replace attribute
             po.modify(attr, ModSet.REPLACE);
         }
     }
 
     private void modifyAndSaveEntry(SSOToken token, String entryDN,
             Map stringAttributes, Map byteAttributes, boolean isAdd)
             throws AccessRightsException, EntryNotFoundException, UMSException {
         PersistentObject po = UMSObject.getObjectHandle(token,
                 new Guid(entryDN));
         // Add string attributes
         if (stringAttributes != null && !stringAttributes.isEmpty()) {
             Iterator itr = stringAttributes.keySet().iterator();
             while (itr.hasNext()) {
                 String attrName = (String) (itr.next());
                 Set set = (Set) (stringAttributes.get(attrName));
                 String attrValues[] = (set == null ? null : (String[]) set
                         .toArray(new String[set.size()]));
                 Attr attr = new Attr(attrName, attrValues);
                 /*
                  * 2005-02-17 Aravindan: AMObjectImpl.removeAttributes(...) sets
                  * the values to be Collections.EMPTY_SET.
                  */
                 modifyPersistentObject(po, attr, isAdd,
                         (set == AMConstants.REMOVE_ATTRIBUTE));
             }
         }
 
         // Add byte attributes
         if (byteAttributes != null && !byteAttributes.isEmpty()) {
             Iterator itr = byteAttributes.keySet().iterator();
             while (itr.hasNext()) {
                 String attrName = (String) (itr.next());
                 byte[][] attrValues = (byte[][]) (byteAttributes.get(attrName));
                 Attr attr = new Attr(attrName, attrValues);
                 modifyPersistentObject(po, attr, isAdd, false);
             }
         }
         po.save();
     }
 
     // TODO: method rename from setProfileAttributes to setAttributes
     /**
      * Method Set the attributes of an entry.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param objectType
      *            profile type
      * @param attrSet
      *            attributes to be set
      */
     public void setAttributes(SSOToken token, String entryDN, int objectType,
             Map stringAttributes, Map byteAttributes, boolean isAdd)
             throws AMException, SSOException {
         Map oldAttributes = null;
         AMEmailNotification mailer = null;
         validateAttributeUniqueness(entryDN, objectType, false,
                 stringAttributes);
         String eDN = entryDN;
         if (objectType == AMObject.USER) {
             eDN = (new DN(entryDN)).getParent().toString();
         }
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(internalToken, eDN);
         try {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.setAttributes() entryDN: "
                         + entryDN);
             }
 
             if (objectType == AMObject.USER) { // Create user modification list
                 // Invoke the user password validation plugin. Note: the
                 // validation is done only for String attributes
                 AMUserPasswordValidationImpl pluginImpl = 
                     new AMUserPasswordValidationImpl(token, orgDN);
                 try {
                     pluginImpl.validate(stringAttributes);
                 } catch (AMException ame) {
                     debug.error("DirectoryManager.setAttributes(): Invalid "
                             + "characters for user", ame);
                     throw ame;
                 }
 
                 // Create a mailter instance
                 mailer = new AMEmailNotification(entryDN);
                 mailer.setUserModifyNotificationList();
             }
 
             if ((getUserPostPlugin() != null)
                     || (mailer != null && mailer
                             .isPresentUserModifyNotificationList())) {
                 Set attrNames = stringAttributes.keySet();
                 oldAttributes = getAttributes(token, entryDN, attrNames,
                         objectType);
             }
 
             // Call pre-processing user impls & get modified attributes
             // Note currently only String attributes supported
             stringAttributes = processPreModifyCallBacks(token, entryDN,
                     oldAttributes, stringAttributes, orgDN, objectType);
             // Set DCTree attributes
             setDCTreeAttributes(token, entryDN, stringAttributes, objectType);
             // modify and save the entry
             modifyAndSaveEntry(token, entryDN, stringAttributes,
                     byteAttributes, isAdd);
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.setAttributes() User does "
                         + "not have sufficient access rights: ", e);
             }
             throw new AMException(token, "460");
         } catch (EntryNotFoundException ee) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.setAttributes() Entry not "
                         + "found: ", ee);
             }
             String msgid = getEntryNotFoundMsgID(objectType);
             String entryName = getEntryName(ee);
             Object args[] = { entryName };
             throw new AMException(AMSDKBundle.getString(msgid, args), msgid,
                     args);
         } catch (UMSException e) {
             if (debug.warningEnabled())
                 debug.warning("DirectoryManager.setAttributes() Internal "
                         + "error occurred", e);
             processInternalException(token, e, "452");
         }
         processPostModifyCallBacks(token, entryDN, oldAttributes,
                 stringAttributes, orgDN, objectType);
 
         if (objectType == AMObject.USER) {
             AMUserEntryProcessed postPlugin = getUserPostPlugin();
             if (postPlugin != null) { // Invoke pre processing impls
                 postPlugin.processUserModify(token, entryDN, oldAttributes,
                         stringAttributes);
             }
             if (mailer != null && mailer.isPresentUserModifyNotificationList())
             {
                 mailer.sendUserModifyNotification(token, stringAttributes,
                         oldAttributes);
             }
         }
     }
 
     // ##########Group and role related APIs
 
     /**
      * Returns an array containing the dynamic group's scope, base dn, and
      * filter.
      */
     public String[] getGroupFilterAndScope(SSOToken token, String entryDN,
             int profileType) throws SSOException, AMException {
         String[] result = new String[3];
         int scope;
         String base;
         String gfilter;
         try {
             DynamicGroup dg = (DynamicGroup) UMSObject.getObject(token,
                     new Guid(entryDN));
             scope = dg.getSearchScope();
             base = dg.getSearchBase().getDn();
             gfilter = dg.getSearchFilter();
             result[0] = Integer.toString(scope);
             result[1] = base;
             result[2] = gfilter;
 
         } catch (EntryNotFoundException e) {
             debug.error("AMGroupImpl.searchUsers", e);
             String msgid = getEntryNotFoundMsgID(profileType);
             String expectionEntryName = getEntryName(e);
             Object args[] = { expectionEntryName };
             throw new AMException(AMSDKBundle.getString(msgid, args), msgid,
                     args);
         } catch (UMSException e) {
             debug.message("AMGroupImpl.searchUsers", e);
             throw new AMException(AMSDKBundle.getString("341"), "341");
         }
         return result;
     }
 
     /**
      * Sets the filter for a dynamic group in the datastore.
      * 
      * @param token
      * @param entryDN
      * @param filter
      * @throws AMException
      * @throws SSOException
      */
     public void setGroupFilter(SSOToken token, String entryDN, String filter)
             throws AMException, SSOException {
         try {
             com.iplanet.ums.DynamicGroup dynamicGroup = 
                 (com.iplanet.ums.DynamicGroup) UMSObject.getObject(
                                                 token, new Guid(entryDN));
             dynamicGroup.setSearchFilter(filter);
             dynamicGroup.save();
         } catch (UMSException ume) {
             debug.message("AMDynamicGroup.setSearchFilter() - Unable to "
                     + "setFilter()", ume);
             throw new AMException(token, "352", ume);
         }
     }
 
     /**
      * @param token
      * @param target
      * @param members
      * @param operation
      * @param profileType
      * @throws UMSException
      * @throws AMException
      */
     private void modifyRoleMembership(SSOToken token, String target,
             Set members, int operation, int profileType) throws UMSException,
             AMException {
         ManagedRole role;
         try {
             role = (ManagedRole) UMSObject.getObject(token, new Guid(target));
         } catch (ClassCastException e) {
             debug.message(
                     "DirectoryManager.modifyRoleMembership() - Unable to "
                             + "modify role membership", e);
             throw new AMException(token, "350");
         }
 
         // Since this target cannot be an Org. Get the parent
         String parentDN = role.getParentGuid().getDn();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(token, parentDN);
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             members = AMCallBackImpl.preProcessModifyMemberShip(token, target,
                     orgDN, members, operation, profileType);
             if (members == null || members.isEmpty()) {
                 return;
             }
         }
         switch (operation) {
         case ADD_MEMBER:
             Guid[] membersGuid = CommonUtils.toGuidArray(members);
             role.addMembers(membersGuid);
             // COMPLIANCE: if admin role then perform iplanet
             // compilance related operations if needed.
             if (compl.isAdminGroupsEnabled(parentDN)) {
                 compl.verifyAndLinkRoleToGroup(token, membersGuid, target);
             }
             break;
         case REMOVE_MEMBER:
             // UMS does not have Role.removerMembers : TBD
             Object[] entries = members.toArray();
             for (int i = 0; i < entries.length; i++) {
                 role.removeMember(new Guid((String) entries[i]));
             }
             // COMPLIANCE: if admin role then perform iplanet
             // compilance related operations if needed.
             if (compl.isAdminGroupsEnabled(parentDN)) {
                 compl.verifyAndUnLinkRoleToGroup(token, members, target);
             }
             break;
         default:
             throw new AMException(token, "114");
         }
         // Make call backs to the plugins to let them know modification to
         // role membership.
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             // Here the new members are just the ones added not the complete Set
             AMCallBackImpl.postProcessModifyMemberShip(token, target, orgDN,
                     members, operation, profileType);
         }
     }
 
     private void modifyGroupMembership(SSOToken token, String target,
             Set members, int operation, int profileType) throws UMSException,
             AMException {
 
         StaticGroup group = (StaticGroup) UMSObject.getObject(token, new Guid(
                 target));
 
         // Make call backs to the plugins to let them know modification
         // to role membership.
         // Since this target cannot be an Org. Get the parent
         String parentDN = group.getParentGuid().getDn();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(token, parentDN);
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             members = AMCallBackImpl.preProcessModifyMemberShip(token, target,
                     orgDN, members, operation, profileType);
             if (members == null || members.isEmpty()) {
                 return;
             }
         }
 
         switch (operation) {
         case ADD_MEMBER:
             group.addMembers(CommonUtils.toGuidArray(members));
             updateUserAttribute(token, members, target, true);
 
             break;
         case REMOVE_MEMBER:
             // UMS does not have Role.removerMembers : TBD
             Object[] entries = members.toArray();
             for (int i = 0; i < entries.length; i++) {
                 group.removeMember(new Guid((String) entries[i]));
             }
             updateUserAttribute(token, members, target, false);
 
             break;
         default:
             throw new AMException(token, "114");
         }
 
         // Make call backs to the plugins to let them know modification to
         // role membership.
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             // Here the new members are just the ones added not the complete Set
             AMCallBackImpl.postProcessModifyMemberShip(token, target, orgDN,
                     members, operation, profileType);
         }
     }
 
     private void modifyAssignDynamicGroupMembership(SSOToken token,
             String target, Set members, int operation, int profileType)
             throws UMSException, AMException {
         // fake object to get around UMS problem.
         // UMS AssignableDynamicGroup has a class resolver, it is
         // added to resolver list in static block. So I need to
         // construct a dummy AssignableDynamicGroup
         AssignableDynamicGroup adgroup = (AssignableDynamicGroup) UMSObject
                 .getObject(token, new Guid(target));
 
         // Make call backs to the plugins to let them know modification
         // to role membership.
         // Since this target cannot be an Org. Get the parent
         String parentDN = adgroup.getParentGuid().getDn();
         if (amdm == null)
             amdm = AMDirectoryWrapper.getInstance();
         String orgDN = amdm.getOrganizationDN(token, parentDN);
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             members = AMCallBackImpl.preProcessModifyMemberShip(token, target,
                     orgDN, members, operation, profileType);
             if (members == null || members.isEmpty()) {
                 return;
             }
         }
         switch (operation) {
         case ADD_MEMBER:
             Guid[] membersGuid = CommonUtils.toGuidArray(members);
             adgroup.addMembers(CommonUtils.toGuidArray(members));
             if (compl.isAdminGroupsEnabled(AMStoreConnection.rootSuffix)) {
                 compl.verifyAndLinkGroupToRole(token, membersGuid, target);
             }
             break;
         case REMOVE_MEMBER:
             Object[] entries = members.toArray();
             for (int i = 0; i < entries.length; i++) {
                 adgroup.removeMember(new Guid((String) entries[i]));
             }
             // COMPLIANCE: if admin group then perform iplanet
             // compliance related operations if needed.
             if (compl.isAdminGroupsEnabled(AMStoreConnection.rootSuffix)) {
                 compl.verifyAndUnLinkGroupToRole(token, members, target);
             }
             break;
         default:
             throw new AMException(token, "114");
         }
 
         // Make call backs to the plugins to let them know modification to
         // role membership.
         if (AMCallBackImpl.isExistsPrePostPlugins(orgDN)) {
             // Here the new members are just the ones added not the complete Set
             AMCallBackImpl.postProcessModifyMemberShip(token, target, orgDN,
                     members, operation, profileType);
         }
     }
 
     private AMException generateMemberShipException(SSOToken token,
             String target, int objectType, EntryNotFoundException e) {
         DN errorDN = getExceptionDN(e);
         DN targetDN = new DN(target);
         if (errorDN == null) {
             debug.error("DirectoryManager.modMemberShip", e);
             Object args[] = { target };
             String locale = CommonUtils.getUserLocale(token);
             return new AMException(AMSDKBundle.getString("461", args, locale),
                     "461", args);
         }
         String entryName = 
             ((RDN) errorDN.getRDNs().firstElement()).getValues()[0];
 
         String errorCode = null;
         if (errorDN.equals(targetDN)) {
             switch (objectType) {
             case AMObject.ROLE:
             case AMObject.MANAGED_ROLE:
                 errorCode = "465";
                 break;
             case AMObject.GROUP:
             case AMObject.STATIC_GROUP:
             case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
                 errorCode = "466";
                 break;
             }
         } else {
             errorCode = "468";
         }
         debug.error("DirectoryManager.modMemberShip() - Entry not found "
                 + target, e);
         Object args[] = { entryName };
         return new AMException(AMSDKBundle.getString(errorCode, args),
                 errorCode, args);
 
     }
 
     /**
      * Modify member ship for role or static group
      * 
      * @param token
      *            SSOToken
      * @param members
      *            Set of member DN to be operated
      * @param target
      *            DN of the target object to add the member
      * @param type
      *            type of the target object, AMObject.ROLE or AMObject.GROUP
      * @param operation
      *            type of operation, ADD_MEMBER or REMOVE_MEMBER
      * @param updateUserEntry
      *            If true then call the updatUserAttribute when modifying group
      *            membership
      */
     public void modifyMemberShip(SSOToken token, Set members, String target,
             int type, int operation) throws AMException {
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.modifyMemberShip: targetDN = <"
                     + target + ">, Members: " + members + ", object Type = "
                     + type + "Operation = " + operation);
         }
         Iterator itr = members.iterator();
         while (itr.hasNext()) {
             String userDN = (String) itr.next();
             if (userDN.equals("") || !DN.isDN(userDN)) {
                 debug.error("DirectoryManager.modifyMemberShip() Invalid DN: "
                         + userDN);
                 throw new AMException(token, "157");
             }
         }
         try {
             switch (type) {
             case AMObject.ROLE:
             case AMObject.MANAGED_ROLE:
                 modifyRoleMembership(token, target, members, operation, type);
                 break;
             case AMObject.GROUP:
             case AMObject.STATIC_GROUP:
                 modifyGroupMembership(token, target, members, operation, type);
                 break;
             case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
                 modifyAssignDynamicGroupMembership(token, target, members,
                         operation, type);
                 break;
             default:
                 throw new AMException(token, "114");
             }
         } catch (AccessRightsException e) {
             debug.error("DirectoryManager.modMemberShip() - Insufficient "
                     + "access rights: ", e);
             throw new AMException(token, "460");
         } catch (EntryNotFoundException e) {
             throw generateMemberShipException(token, target, type, e);
         } catch (UMSException e) {
             debug.message("DirectoryManager.modMemberShip() - Unable to "
                     + "modify membership", e);
             throw new AMException(token, "350", e);
         }
     }
 
     // *************************************************************************
     // Service Related Functionality
     // *************************************************************************
     /**
      * Get registered services for an organization
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the org
      * @return Set set of service names
      */
     public Set getRegisteredServiceNames(SSOToken token, String entryDN)
             throws AMException {
         try {
             Set attrNames = new HashSet(1);
             attrNames.add(SERVICE_STATUS_ATTRIBUTE);
 
             // User dsame privileged user to get the registered
             // services. Admins of all levels will need this access
             Map attributes = getAttributes(internalToken, entryDN, attrNames,
                     AMObject.UNDETERMINED_OBJECT_TYPE);
             Set resultSet = Collections.EMPTY_SET;
             if (attributes.size() == 1) {
                 resultSet = (Set) attributes.get(SERVICE_STATUS_ATTRIBUTE);
             }
 
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.getRegisteredServiceNames()"
                         + " Registered Service Names for entryDN: " + entryDN
                         + " are: " + resultSet);
             }
             return resultSet;
         } catch (Exception e) {
             debug.error("DirectoryManager.getRegisteredService", e);
             throw new AMException(token, "455");
         }
     }
 
     /**
      * Register a service for an org or org unit policy to a profile
      * 
      * @param token
      *            token
      * @param orgDN
      *            DN of the org
      * @param serviceName
      *            Service Name
      */
     public void registerService(SSOToken token, String orgDN,
             String serviceName) throws AMException, SSOException {
         try {
             // This returns a valid set only if the service has
             // Dynamic attributes
             Set attrNames = getServiceAttributesWithQualifier(token,
                     serviceName);
             if ((attrNames != null) && !attrNames.isEmpty()) {
                 PersistentObject po = UMSObject.getObjectHandle(token,
                         new Guid(orgDN));
                 DirectCOSDefinition dcos = createCOSDefinition(serviceName,
                         attrNames);
                 COSManager cm = COSManager.getCOSManager(token, po.getGuid());
                 cm.addDefinition(dcos);
             }
         } catch (AccessRightsException e) {
             debug.error("DirectoryManager.registerService() Insufficient "
                     + "access rights to register service: " + serviceName, e);
             throw new AMException(token, "460");
         } catch (EntryAlreadyExistsException e) {
            // Log it as warning. Definition already exists. That's OK.
            if (debug.warningEnabled()) {
                debug.warning("DirectoryManager.registerService() Service " +
                    serviceName + " already registered", e);
            }
             Object args[] = { serviceName };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("464", args, locale),
                     "464", args);
         } catch (SMSException e) {
             debug.error("DirectoryManager.registerService() Unable to "
                     + "register service: " + serviceName, e);
             throw new AMException(token, "914");
         } catch (UMSException e) {
             debug.error("DirectoryManager.registerService() Unable to "
                     + "register service: " + serviceName, e);
             throw new AMException(token, "914", e);
         }
     }
 
     /**
      * Method to get the attribute names of a service with CosQualifier. For
      * example: Return set could be ["iplanet-am-web-agent-allow-list
      * merge-schemes", "iplanet-am-web-agent-deny-list merge-schemes"] This only
      * returns Dynamic attributes
      */
     private Set getServiceAttributesWithQualifier(SSOToken token,
             String serviceName) throws SMSException, SSOException {
         ServiceSchemaManager ssm = new ServiceSchemaManager(serviceName, token);
         ServiceSchema ss = null;
         try {
             ss = ssm.getSchema(SchemaType.DYNAMIC);
         } catch (SMSException sme) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.getServiceNames(): No "
                         + "schema defined for SchemaType.DYNAMIC type");
             }
         }
         if (ss == null) {
             return Collections.EMPTY_SET;
         }
 
         Set attrNames = new HashSet();
         Set attrSchemaNames = ss.getAttributeSchemaNames();
         Iterator itr = attrSchemaNames.iterator();
         while (itr.hasNext()) {
             String attrSchemaName = (String) itr.next();
             AttributeSchema attrSchema = ss.getAttributeSchema(attrSchemaName);
             String name = attrSchemaName + " " + attrSchema.getCosQualifier();
             attrNames.add(name);
         }
         return attrNames;
     }
 
     /**
      * Create a COS Definition based on serviceID & attribute set & type. For
      * policy attribute, will set cosattribute to "override" For other
      * attribute, will set cosattribute to "default"
      */
     private DirectCOSDefinition createCOSDefinition(String serviceID,
             Set attrNames) throws UMSException {
         // new attribute set
         AttrSet attrs = new AttrSet();
         // set naming attribute to the serviceID
         Attr attr = new Attr(ICOSDefinition.DEFAULT_NAMING_ATTR, serviceID);
         attrs.add(attr);
         // add cosspecifier
         attr = new Attr(ICOSDefinition.COSSPECIFIER, "nsrole");
         attrs.add(attr);
         // add cosattribute
         attr = new Attr(ICOSDefinition.COSATTRIBUTE);
         Iterator iter = attrNames.iterator();
         while (iter.hasNext()) {
             String attrName = (String) iter.next();
             attr.addValue(attrName);
         }
         attrs.add(attr);
 
         return new DirectCOSDefinition(attrs);
     }
 
     // Rename from removeService to unRegisterService
     /**
      * Un register service for a AMro profile.
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose service is to be removed
      * @param objectType
      *            profile type
      * @param serviceName
      *            Service Name
      * @param template
      *            AMTemplate
      * @param type
      *            Template type
      */
     public void unRegisterService(SSOToken token, String entryDN,
             int objectType, String serviceName, AMTemplate template, int type)
             throws AMException {
         if (type == AMTemplate.DYNAMIC_TEMPLATE) {
 
             // TODO:change "cn" to fleasible naming attribute for AMObject.ROLE
             try {
                 PersistentObject po = UMSObject.getObjectHandle(token,
                         new Guid(entryDN));
 
                 COSManager cm = null;
                 // COS Definition to obtaint depends on different profile type
                 switch (objectType) {
                 case AMObject.ROLE:
                 case AMObject.FILTERED_ROLE:
                     cm = COSManager.getCOSManager(token, po.getParentGuid());
                     break;
                 case AMObject.ORGANIZATION:
                 case AMObject.ORGANIZATIONAL_UNIT:
                 case AMObject.PEOPLE_CONTAINER:
                     cm = COSManager.getCOSManager(token, po.getGuid());
                     break;
                 default:
                     // entry other than AMObject.ROLE,FILTERED_ROLE,ORG,PC
                     // does not have COS
                     throw new AMException(token, "450");
                 }
 
                 DirectCOSDefinition dcos;
                 try {
                     dcos = (DirectCOSDefinition) cm.getDefinition(serviceName);
                 } catch (COSNotFoundException e) {
                     if (debug.messageEnabled()) {
                         debug.message("DirectoryManager.unRegisterService() "
                                 + "No COSDefinition found for service: "
                                 + serviceName);
                     }
                     Object args[] = { serviceName };
                     String locale = CommonUtils.getUserLocale(token);
                     throw new AMException(AMSDKBundle.getString("463", args,
                             locale), "463", args);
                 }
 
                 // Remove the COS Definition and Template
                 dcos.removeCOSTemplates();
                 cm.removeDefinition(serviceName);
             } catch (AccessRightsException e) {
                 debug.error("DirectoryManager.unRegisterService() "
                         + "Insufficient Access rights to unRegister service: ",
                         e);
                 throw new AMException(token, "460");
             } catch (UMSException e) {
                 debug.error("DirectoryManager.unRegisterService: "
                         + "Unable to unregister service ", e);
                 throw new AMException(token, "855", e);
             }
         } else if (type == AMTemplate.ORGANIZATION_TEMPLATE) {
             // delete the Template if there are appropriate rights.
             try {
                 template.delete();
             } catch (SSOException ex) {
                 debug.error("DirectoryManager.unRegisterService: "
                         + "Unable to unregister service ", ex);
                 throw new AMException(token, "855");
             }
         }
     }
 
     /**
      * Get the AMTemplate DN (COSTemplateDN)
      * 
      * @param token
      *            SSOToken
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param serviceName
      *            Service Name
      * @param type
      *            the template type, AMTemplate.DYNAMIC_TEMPLATE
      * @return String DN of the AMTemplate
      */
     public String getAMTemplateDN(SSOToken token, String entryDN,
             int objectType, String serviceName, int type) throws AMException {
         String roleDN = null;
         // TBD : get template on flexible naming attribute
         try {
             // get COS Definition depends on different profile type
             switch (objectType) {
             case AMObject.ROLE:
             case AMObject.FILTERED_ROLE:
                 roleDN = entryDN;
                 PersistentObject po = UMSObject.getObjectHandle(token,
                         new Guid(entryDN));
                 return ("cn=\"" + roleDN + "\",cn=" + serviceName + "," + po
                         .getParentGuid().toString());
             case AMObject.ORGANIZATION:
             case AMObject.ORGANIZATIONAL_UNIT:
             case AMObject.PEOPLE_CONTAINER:
                 roleDN = "cn=" + CONTAINER_DEFAULT_TEMPLATE_ROLE + ","
                         + entryDN;
                 return ("cn=\"" + roleDN + "\",cn=" + serviceName + "," 
                         + entryDN);
             default:
                 // entry other that AMObject.ROLE & FILTERED_ROLE & ORG & PC
                 // does not have COS
                 throw new AMException(token, "450");
             }
         } catch (UMSException e) {
             debug.error("DirectoryManager.getAMTemplateDN() Unable to get "
                     + "AMTemplate DN for service: " + serviceName
                     + " entryDN: " + entryDN, e);
             throw new AMException(token, "349", e);
         }
     }
 
     /**
      * Create an AMTemplate (COSTemplate)
      * 
      * @param token
      *            token
      * @param entryDN
      *            DN of the profile whose template is to be set
      * @param serviceName
      *            Service Name
      * @param attrSet
      *            attributes to be set
      * @param priority
      *            template priority
      * @return String DN of the newly created template
      */
     public String createAMTemplate(SSOToken token, String entryDN,
             int objectType, String serviceName, Map attributes, int priority)
             throws AMException {
         // TBD, each time a Org/PC is created, need to create default role
         COSManager cm = null;
         DirectCOSDefinition dCOS = null;
         String roleDN = null;
 
         // TBD, change "cn" to flesible naming attrsibute for AMObject.ROLE
         try {
             PersistentObject po = UMSObject.getObjectHandle(token, new Guid(
                     entryDN));
             // get COS Definition depends on different profile type
             switch (objectType) {
             case AMObject.ROLE:
             case AMObject.FILTERED_ROLE:
                 roleDN = entryDN;
                 cm = COSManager.getCOSManager(token, po.getParentGuid());
                 dCOS = (DirectCOSDefinition) cm.getDefinition(serviceName);
                 break;
             case AMObject.ORGANIZATION:
             case AMObject.ORGANIZATIONAL_UNIT:
             case AMObject.PEOPLE_CONTAINER:
                 roleDN = "cn=" + CONTAINER_DEFAULT_TEMPLATE_ROLE + ","
                         + entryDN;
                 cm = COSManager.getCOSManager(token, po.getGuid());
                 dCOS = (DirectCOSDefinition) cm.getDefinition(serviceName);
                 break;
             default:
                 // entry other that AMObject.ROLE & FILTERED_ROLE & ORG & PC
                 // does not have COS
                 throw new AMException(token, "450");
             }
             // add template priority
             AttrSet attrSet = CommonUtils.mapToAttrSet(attributes);
             if (priority != AMTemplate.UNDEFINED_PRIORITY) {
                 Attr attr = new Attr("cospriority");
                 attr.addValue("" + priority);
                 attrSet.add(attr);
             }
             COSTemplate template = createCOSTemplate(serviceName, attrSet,
                     roleDN);
             dCOS.addCOSTemplate(template);
             return template.getGuid().toString();
         } catch (COSNotFoundException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.createAMTemplate() "
                         + "COSDefinition for service: " + serviceName
                         + " not found: ", e);
             }
             Object[] args = { serviceName };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("459", locale), "459",
                     args);
         } catch (EntryAlreadyExistsException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager.createAMTemplate: template "
                         + "already exists for " + serviceName, e);
             }
             String params[] = { serviceName };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("854", params, locale),
                     "854", params);
         } catch (AccessRightsException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManager.createAMTemplate() "
                         + "Insufficient access rights to create template for: "
                         + serviceName + " & entryDN: " + entryDN, e);
             }
             throw new AMException(token, "460");
         } catch (UMSException e) {
             if (debug.warningEnabled()) {
                 debug.warning(
                         "DirectoryManager.createAMTemplate() Unable to create "
                                 + "AMTemplate for: " + serviceName
                                 + " & entryDN: " + entryDN, e);
             }
             Object[] args = { serviceName };
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("459", locale), "459",
                     args, e);
         } catch (Exception e) {
             if (debug.warningEnabled())
                 debug.warning("DirectoryManager.createAMTemplate", e);
             throw new AMException(token, "451");
         }
     }
 
     /**
      * create COS Template from attribute set for a service, this will involve
      * UMS Creation template for COSTemplate
      * 
      * @param serviceID
      *            Service name
      * @param attrSet
      *            the attribute set
      * @param entryDN
      *            DN of the role
      * @return COSTemplate COS Template created
      */
     private COSTemplate createCOSTemplate(String serviceID, AttrSet attrset,
             String entryDN) throws UMSException {
 
         TemplateManager tempMgr = TemplateManager.getTemplateManager();
         CreationTemplate basicCOSTemplate = tempMgr.getCreationTemplate(
                 "BasicCOSTemplate", null);
 
         // Now need to add the service object for the "serviceID" to the
         // required attribute set of the cos creatation template
         // need to use schema manager and service manager (TBD)
         // But for now just add "extensibleObject" to it
         COSTemplate cosTemplate = new COSTemplate(basicCOSTemplate, "\""
                 + entryDN + "\"");
         cosTemplate.addTemplateAttribute("objectclass", "extensibleObject");
 
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.newCOSTemplate: cn = " + entryDN
                     + " COSTemplate = " + cosTemplate);
         }
         int size = attrset.size();
         for (int i = 0; i < size; i++) {
             Attr attr = attrset.elementAt(i);
             cosTemplate.modify(attr, ModSet.ADD);
         }
 
         return cosTemplate;
     }
 
     /**
      * Gets the naming attribute after reading it from the corresponding
      * creation template. If not found, a default value will be used
      */
     public String getNamingAttr(int objectType, String orgDN) {
         try {
             // Intitalize TemplateManager if not already initialized
             if (templateMgr == null) {
                 templateMgr = TemplateManager.getTemplateManager();
             }
 
             String templateName = getCreationTemplateName(objectType);
             if (templateName == null) {
                 debug.warning(
                         "AMNamingAttrMgr.getNamingAttr(objectType, orgDN): (" + 
                         objectType + "," + orgDN + ")Could not " + 
                         "determine creation template name. Returning <empty> " +
                         "value");
                 return "";
             }
 
             Guid orgGuid = ((orgDN == null) ? null : new Guid(orgDN));
             CreationTemplate creationTemp = templateMgr.getCreationTemplate(
                     templateName, orgGuid, TemplateManager.SCOPE_ANCESTORS);
             // get search filter attribute
             String namingAttr = creationTemp.getNamingAttribute();
             if (namingAttr == null) {
                 debug.error("AMNamingAttrManager.getNamingAttr()"
                         + " Naming attribute for Object Type:" + objectType
                         + " Org DN: " + orgDN + " is null");
             } else if (debug.messageEnabled()) {
                 debug.message("AMNamingAttrManager.getNamingAttr(): Naming "
                         + "attribute for Object type= " + objectType + ": "
                         + namingAttr);
             }
             return namingAttr;
         } catch (UMSException ue) {
             // The right thing would be to propagate this exception back
             String defaultAttr = getDefaultNamingAttr(objectType);
             debug.warning("Unable to get the naming attribute for "
                     + objectType + " Using default " + defaultAttr);
             return defaultAttr;
         }
     }
 
     /**
      * Gets the default naming attribute corresponding to an object type
      */
     private String getDefaultNamingAttr(int objectType) {
         switch (objectType) {
         case AMObject.USER:
             return DEFAULT_USER_NAMING_ATTR;
         case AMObject.ROLE:
             return DEFAULT_ROLE_NAMING_ATTR;
         case AMObject.FILTERED_ROLE:
             return DEFAULT_FILTERED_ROLE_NAMING_ATTR;
         case AMObject.GROUP:
             return DEFAULT_GROUP_NAMING_ATTR;
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return DEFAULT_ASSIGNABLE_DYNAMIC_GROUP_NAMING_ATTR;
         case AMObject.DYNAMIC_GROUP:
             return DEFAULT_DYNAMIC_GROUP_NAMING_ATTR;
         case AMObject.ORGANIZATION:
             return DEFAULT_ORG_NAMING_ATTR;
         case AMObject.PEOPLE_CONTAINER:
             return DEFAULT_PEOPLE_CONTAINER_NAMING_ATTR;
         case AMObject.ORGANIZATIONAL_UNIT:
             return DEFAULT_ORG_UNIT_NAMING_ATTR;
         case AMObject.GROUP_CONTAINER:
             return DEFAULT_GROUP_CONTAINER_NAMING_ATTR;
         case AMObject.RESOURCE:
             return DEFAULT_RESOURCE_NAMING_ATTR;
         default:
             debug.warning("AMNamingAttrMgr.getDefaultNamingAttr(): Unknown "
                     + "object type is passed. Returning <empty> value");
             return ""; // This should not occur
         }
     }
 
     /**
      * Get the name of the creation template to use for specified object type.
      */
     public String getCreationTemplateName(int objectType) {
         String ret = (String) CommonUtils.creationtemplateMap.get(Integer
                 .toString(objectType));
         if (ret != null) {
             return ret;
         }
         switch (objectType) {
         case AMObject.USER:
             return USER_CREATION_TEMPLATE;
         case AMObject.ROLE:
             return MANAGED_ROLE_CREATION_TEMPLATE;
         case AMObject.FILTERED_ROLE:
             return FILTERED_ROLE_CREATION_TEMPLATE;
         case AMObject.GROUP:
             return GROUP_CREATION_TEMPLATE;
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return ASSIGANABLE_DYNAMIC_GROUP_CREATION_TEMPLATE;
         case AMObject.DYNAMIC_GROUP:
             return DYNAMIC_GROUP_CREATION_TEMPLATE;
         case AMObject.ORGANIZATION:
             return ORGANIZATION_CREATION_TEMPLATE;
         case AMObject.PEOPLE_CONTAINER:
             return PEOPLE_CONTAINTER_CREATION_TEMPLATE;
         case AMObject.ORGANIZATIONAL_UNIT:
             return ORGANIZATIONAL_UNIT_CREATION_TEMPLATE;
         case AMObject.GROUP_CONTAINER:
             return GROUP_CONTAINER_CREATION_TEMPLATE;
         case AMObject.RESOURCE:
             return RESOURCE_CREATION_TEMPLATE;
         default:
             debug.warning("AMNamingAttrMgr.getCreationTemplateName(): "
                     + "Unknown object type is passed. Returning null value");
             return null;
         }
     }
 
     public String getObjectClassFromDS(int objectType) {
         // TODO: Try to obtain the Object Class from Creation Templates
         // If not found, try to extract it from search filter
 
         // Obtain the Object Class from the global search filter for this object
         // If not found, return the default
         String searchFilter = AMSearchFilterManager
                 .getGlobalSearchFilter(objectType); // The search filter is
                                                     // already in lower case
 
         // Parse the Search filter to obtain the object class
         // Note: The object class that is picked up is the value of the first
         // object class specified in the filter.
         String pattern = "objectclass="; // Pattern to look for
         int index = searchFilter.indexOf(pattern);
         String objectClass = null;
         if (index != -1) {
             int startIndex = index + pattern.length();
             int endIndex = searchFilter.indexOf(')', startIndex);
             if (endIndex != -1) {
                 objectClass = searchFilter.substring(startIndex, endIndex);
             }
         } else {
             objectClass = getDefaultObjectClass(objectType);
         }
         return objectClass.toLowerCase();
     }
 
     /**
      * Gets the default object type corresponding to an object type
      */
     private static String getDefaultObjectClass(int objectType) {
         switch (objectType) {
         case AMObject.USER:
             return DEFAULT_USER_OBJECT_CLASS;
         case AMObject.ROLE:
             return DEFAULT_ROLE_OBJECT_CLASS;
         case AMObject.FILTERED_ROLE:
             return DEFAULT_FILTERED_ROLE_OBJECT_CLASS;
         case AMObject.GROUP:
             return DEFAULT_GROUP_OBJECT_CLASS;
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return DEFAULT_ASSIGNABLE_DYNAMIC_GROUP_OBJECT_CLASS;
         case AMObject.DYNAMIC_GROUP:
             return DEFAULT_DYNAMIC_GROUP_OBJECT_CLASS;
         case AMObject.ORGANIZATION:
             return DEFAULT_ORGANIZATION_OBJECT_CLASS;
         case AMObject.PEOPLE_CONTAINER:
             return DEFAULT_PEOPLE_CONTAINER_OBJECT_CLASS;
         case AMObject.ORGANIZATIONAL_UNIT:
             return DEFAULT_ORGANIZATIONAL_UNIT_OBJECT_CLASS;
         case AMObject.GROUP_CONTAINER:
             return DEFAULT_GROUP_CONTAINER_OBJECT_CLASS;
         case AMObject.RESOURCE:
             return DEFAULT_RESOURCE_OBJECT_CLASS;
         default:
             return ""; // This should not occur. Throw an exception here.
         }
     }
 
     /**
      * Returns the set of attributes (both optional and required) needed for an
      * objectclass based on the LDAP schema
      * 
      * @param objectclass
      * @return
      */
     public Set getAttributesForSchema(String objectclass) {
         try {
             SchemaManager sm = SchemaManager.getSchemaManager(internalToken);
             return new HashSet(sm.getAttributes(objectclass));
         } catch (UMSException ue) {
             return Collections.EMPTY_SET;
         }
     }
 
     public String getSearchFilterFromTemplate(int objectType, String orgDN,
             String searchTemplateName) {
         SearchTemplate searchTemp = null;
         String filter;
         try {
             String searchTempName = ((searchTemplateName == null) ?
                     getSearchTemplateName(objectType)
                     : searchTemplateName);
 
             if (searchTempName == null) {
                 debug.warning(
                         "AMSearchFilterManager.getSearchFilterFromTemplate(): "+
                         "Search template name is null Unable to retrieve " +
                         "search filter. Returning <empty> value.");
                 return "";
             }
 
             TemplateManager mgr = TemplateManager.getTemplateManager();
             Guid orgGuid = ((orgDN == null) ? null : new Guid(orgDN));
             searchTemp = mgr.getSearchTemplate(searchTempName, orgGuid,
                     TemplateManager.SCOPE_TOP);
 
             // Get the Original search filter
             // Check to see if the filter starts with "(" and ends with ")"
             // In which case there is no need to add opening and closing braces.
             // otherwise add the opening and closing braces.
             filter = searchTemp.getSearchFilter();
         } catch (UMSException ue) {
             filter = "(objectclass=*)";
         }
         if (!filter.startsWith("(") || !filter.endsWith(")")) {
             filter = "(" + filter + ")";
         }
 
         // Perform any required filter modifications that need to be cached
         // filter = modifyFilter(filter, objectType);
 
         if (debug.messageEnabled()) {
             debug.message("AMSearchFilterManager."
                     + "getSearchFilterFromTemplate() SearchTemplate Name = "
                     + searchTemp.getName() + ", objectType = " + objectType
                     + ", orgDN = " + orgDN + ", Obtained Filter = "
                     + searchTemp.getSearchFilter() + ", Modified Filter = "
                     + filter);
         }
 
         return filter;
 
     }
 
     /**
      * Get the name of the search template to use for specified object type.
      */
     private static String getSearchTemplateName(int objectType) {
         String st = (String) CommonUtils.searchtemplateMap.get(Integer
                 .toString(objectType));
         if (st != null) {
             return st;
         }
         switch (objectType) {
         case AMObject.USER:
             return USER_SEARCH_TEMPLATE;
         case AMObject.ROLE:
             return ROLE_SEARCH_TEMPLATE;
         case AMObject.FILTERED_ROLE:
             return FILTERED_ROLE_SEARCH_TEMPLATE;
         case AMObject.GROUP:
             return GROUP_SEARCH_TEMPLATE;
         case AMObject.DYNAMIC_GROUP:
             return DYNAMIC_GROUP_SEARCH_TEMPLATE;
         case AMObject.ORGANIZATION:
             return ORGANIZATION_SEARCH_TEMPLATE;
         case AMObject.PEOPLE_CONTAINER:
             return PEOPLE_CONTAINER_SEARCH_TEMPLATE;
         case AMObject.ORGANIZATIONAL_UNIT:
             return ORGANIZATIONAL_UNIT_SEARCH_TEMPLATE;
         case AMObject.ASSIGNABLE_DYNAMIC_GROUP:
             return ASSIGNABLE_DYNAMIC_GROUP_SEARCH_TEMPLATE;
         case AMObject.GROUP_CONTAINER:
             return GROUP_CONTAINER_SEARCH_TEMPLATE;
         case AMObject.RESOURCE:
             return RESOURCE_SEARCH_TEMPLATE;
         default:
             // TODO: Should throw an exception here
             // This should'nt occur; A right thing would be to throw exception
             debug.warning("AMSearchFilterManager.getSearchTemplateName(): "
                     + "Unknown object type is passed. Returning null value");
             return null;
         }
     }
 
     /**
      * Validate attribute uniqueness
      * 
      * @param newEntry
      *            true if create a new user
      * @throws AMException
      *             if attribute uniqueness is violated
      */
     void validateAttributeUniqueness(String entryDN, int profileType,
             boolean newEntry, Map modMap) throws AMException {
         boolean attrExists = false;
         if (modMap == null || modMap.isEmpty()) {
             return;
         }
 
         try {
             if (profileType == AMTemplate.DYNAMIC_TEMPLATE
                     || profileType == AMTemplate.ORGANIZATION_TEMPLATE
                     || profileType == AMTemplate.POLICY_TEMPLATE) {
                 // no namespace validation for these objects
                 return;
             }
             DN userDN = new DN(entryDN);
 
             String[] rdns = userDN.explodeDN(false);
             int size = rdns.length;
 
             if (size < 2) {
                 return;
             }
 
             String orgDN = rdns[size - 1];
 
             AMStoreConnection amsc = new AMStoreConnection(CommonUtils
                     .getInternalToken());
             DN rootDN = new DN(AMStoreConnection.rootSuffix);
             DN thisDN = new DN(orgDN);
 
             for (int i = size - 2; i >= 0; i--) {
                 if (debug.messageEnabled()) {
                     debug.message("AMObjectImpl.validateAttributeUniqueness: " +
                             "try DN = " + orgDN);
                 }
 
                 int type = -1;
 
                 if (!rootDN.isDescendantOf(thisDN)) {
                     try {
                         type = amsc.getAMObjectType(orgDN);
                     } catch (AMException ame) {
                         if (debug.warningEnabled()) {
                             debug.warning(
                                  "AMObjectImpl.validateAttributeUniqueness: " +
                                  "Unable to determine object type of " + orgDN +
                                  " :Attribute uniqueness check aborted", ame);
                         }
                         return;
                     }
                 }
 
                 Set list = null;
                 AMObject amobj = null;
 
                 if (type == AMObject.ORGANIZATION) {
                     AMOrganization amorg = amsc.getOrganization(orgDN);
                     list = amorg.getAttribute(UNIQUE_ATTRIBUTE_LIST_ATTRIBUTE);
                     amobj = amorg;
                 } else if (type == AMObject.ORGANIZATIONAL_UNIT) {
                     AMOrganizationalUnit amorgu = amsc
                             .getOrganizationalUnit(orgDN);
                     list = amorgu.getAttribute(UNIQUE_ATTRIBUTE_LIST_ATTRIBUTE);
                     amobj = amorgu;
                 }
                 if ((list != null) && !list.isEmpty()) {
                     if (debug.messageEnabled()) {
                         debug.message(
                                 "AMObjectImpl.validateAttributeUniqueness: " +
                                 "list = " + list);
                     }
 
                     /*
                      * After adding the uniquness attributes 'ou,cn' to the
                      * list, creating a role with the same name as the existing
                      * user say 'amadmin' fails with 'Attribute uniqueness
                      * violation' The filter (|(cn='attrname')) is used for all
                      * objects. Fixed the code to look for 'Role' profile types
                      * and set the filter as
                      * (&(objectclass=ldapsubentry)
                      * (objectclass=nsroledefinition)
                      * (cn='attrname'))
                      * 
                      * The same issue happens when a group is created with
                      * existing user name. Fixed the code to look for 'Group'
                      * profile types and set the filter as
                      * (&(objectClass=groupofuniquenames)
                      * (objectClass=iplanet-am-managed-group)(cn='attrname'))
                      * The logic in the while loop is iterate through the
                      * attribute unique list and check if the list contains the
                      * naming attribute of the object we are trying to create.
                      * If the naming attribute is in the list,then look if the
                      * profile type of the object we are trying to create is
                      * 'role' or 'group', add appropriate objectclasses and the
                      * entry rdn to the search filter. This filter is used to
                      * search the iDS and determine the attribute uniqueness
                      * violation. The boolean variable 'attrExists' is set to
                      * false initially. This variable is set to true when the
                      * profile type is 'role' or 'group'. The check for this
                      * boolean variable decides the number of matching closing
                      * parens of the three different types of filters.
                      */
 
                     Iterator iter = list.iterator();
                     StringBuffer filterSB = new StringBuffer();
                     StringBuffer newEntrySB = new StringBuffer();
                     filterSB.append("(|");
 
                     while (iter.hasNext()) {
                         String[] attrList = getAttrList((String) iter.next());
                         Set attr = getAttrValues(attrList, modMap);
                         for (int j = 0; j < attrList.length; j++) {
                             String attrName = attrList[j];
                             if (attrName.equals(AMNamingAttrManager
                                     .getNamingAttr(profileType))
                                     && newEntry) {
                                 if ((profileType == AMObject.ROLE)
                                         || (profileType == 
                                                     AMObject.MANAGED_ROLE)
                                         || (profileType == 
                                                     AMObject.FILTERED_ROLE)) {
 
                                     newEntrySB.append("(&");
                                     newEntrySB.append(
                                             "(objectclass=ldapsubentry)");
                                     newEntrySB.append(
                                             "(objectclass=nsroledefinition)");
                                     attrExists = true;
                                 } else if ((profileType == AMObject.GROUP)
                                         || (profileType 
                                                 == AMObject.STATIC_GROUP)
                                         || (profileType == 
                                              AMObject.ASSIGNABLE_DYNAMIC_GROUP)
                                         || (profileType == 
                                              AMObject.DYNAMIC_GROUP)) {
 
                                     newEntrySB.append("(&");
                                     newEntrySB.append(
                                       "(objectclass=iplanet-am-managed-group)");
                                     newEntrySB.append(
                                             "(objectclass=groupofuniquenames)");
                                     attrExists = true;
                                 } else if (profileType == AMObject.ORGANIZATION)
                                 {
                                     newEntrySB.append("(&(!");
                                     newEntrySB.append("(objectclass=");
                                     newEntrySB
                                             .append(SMSEntry.OC_REALM_SERVICE);
                                     newEntrySB.append("))");
                                     attrExists = true;
                                 }
 
                                 filterSB.append("(").append(rdns[0])
                                         .append(")");
 
                             }
 
                             if (attr != null && !attr.isEmpty()) {
                                 Iterator itr = attr.iterator();
 
                                 while (itr.hasNext()) {
                                     filterSB.append("(").append(attrName);
                                     filterSB.append("=").append(
                                             (String) itr.next());
                                     filterSB.append(")");
                                 }
                             } // if
                         }
                     }
                     if (filterSB.length() > 2) {
                         if (attrExists) {
                             // pre-pend the creation filter part to the filter
                             // This is being done so that the filter is
                             // correctly created as
                             // (&(<creation-filter)(|(<attr filter>)))
                             newEntrySB.append(filterSB.toString()).append("))");
                             filterSB = newEntrySB;
                         } else {
                             filterSB.append(")");
                         }
 
                         if (debug.messageEnabled()) {
                             debug.message(
                                     "AMObjectImpl.validateAttributeUniqueness: "
                                        + "filter = " + filterSB.toString());
                         }
 
                         Set users = amobj.search(AMConstants.SCOPE_SUB,
                                 filterSB.toString());
                         // Check if the entry that is "violating" uniqueness is
                         // the same as the one you are checking for.
                         // In that case,ignore the violation
                         if (users != null && users.size() == 1) {
                             String dn = (String) users.iterator().next();
                             DN dnObject = new DN(dn);
                             if (dnObject.equals(new DN(entryDN))) {
                                 return;
                             }
                         }
                         if ((users != null) && !users.isEmpty()) {
                             throw new AMException(AMSDKBundle.getString("162"),
                                     "162");
                         }
                     }
                 }
 
                 orgDN = rdns[i] + "," + orgDN;
                 thisDN = new DN(orgDN);
             }
         } catch (SSOException ex) {
             if (debug.warningEnabled()) {
                 debug.warning("Unable to validate attribute uniqneness", ex);
             }
         }
     }
 
     /**
      * Private method to convert a comma separated string of attribute names to
      * an Array
      * 
      * @param newEntry
      * @throws AMException
      */
     private String[] getAttrList(String attrNames) {
         StringTokenizer tzer = new StringTokenizer(attrNames, ",");
         int size = tzer.countTokens();
         String[] attrList = new String[size];
         for (int i = 0; i < size; i++) {
             String tmps = tzer.nextToken();
             attrList[i] = tmps.trim();
         }
         return attrList;
     }
 
     /**
      * Private method to get a set of values for all attributes that exist in
      * the stringModMap of this object, from a given list of attribute names
      * 
      * @param newEntry
      * @throws AMException
      */
     private Set getAttrValues(String[] attrList, Map modMap) {
         Set retSet = new HashSet();
         int size = attrList.length;
         for (int i = 0; i < size; i++) {
             Set tmpSet = (Set) modMap.get(attrList[i]);
             if (tmpSet != null && !tmpSet.isEmpty()) {
                 retSet.addAll(tmpSet);
             }
         }
         return (retSet);
     }
 
     public Set getTopLevelContainers(SSOToken token) throws AMException,
             SSOException {
 
         String userDN = token.getPrincipal().getName();
         AMStoreConnection amsc = new AMStoreConnection(internalToken);
         AMUser auser = amsc.getUser(userDN);
         Set set = new HashSet();
 
         Set roleDNs = auser.getRoleDNs();
         Iterator iter = roleDNs.iterator();
 
         while (iter.hasNext()) {
             String roleDN = (String) iter.next();
 
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManager."
                         + "getTopLevelContainers: roleDN=" + roleDN);
             }
 
             AMRole role = amsc.getRole(roleDN);
             set.addAll(role.getAttribute(ROLE_MANAGED_CONTAINER_DN_ATTRIBUTE));
         }
 
         if (set.isEmpty()) {
             String filter = "(|"
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(AMObject.ORGANIZATION)
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(AMObject.ORGANIZATIONAL_UNIT)
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(AMObject.PEOPLE_CONTAINER)
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(AMObject.DYNAMIC_GROUP)
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(
                                     AMObject.ASSIGNABLE_DYNAMIC_GROUP)
                     + AMSearchFilterManager
                             .getGlobalSearchFilter(AMObject.GROUP) + ")";
 
             set = search(token, AMStoreConnection.rootSuffix, 
                                 filter, SCOPE_SUB);
         }
 
         HashSet resultSet = new HashSet();
         iter = set.iterator();
 
         while (iter.hasNext()) {
             String containerDN = (String) iter.next();
             DN cDN = new DN(containerDN);
             Iterator iter2 = resultSet.iterator();
             HashSet tmpSet = new HashSet();
             boolean toAdd = true;
 
             while (iter2.hasNext()) {
                 String resultDN = (String) iter2.next();
                 DN rDN = new DN(resultDN);
 
                 if (cDN.isDescendantOf(rDN)) {
                     toAdd = false;
                     tmpSet.add(resultDN);
 
                     break;
                 } else if (!rDN.isDescendantOf(cDN)) {
                     tmpSet.add(resultDN);
                 }
             }
 
             if (toAdd) {
                 tmpSet.add(containerDN);
             }
 
             resultSet = tmpSet;
         }
 
         if (debug.messageEnabled()) {
             debug.message("DirectoryManager.getTopLevelContainers");
             iter = resultSet.iterator();
 
             StringBuffer tmpBuffer = new StringBuffer();
 
             while (iter.hasNext()) {
                 String tmpDN = (String) iter.next();
                 tmpBuffer.append(tmpDN).append("\n");
             }
 
             debug.message("containerDNs\n" + tmpBuffer.toString());
         }
 
         return resultSet;
     }
 
     /**
      * Gets the Organization DN for the specified entryDN. If the entry itself
      * is an org, then same DN is returned.
      * 
      * @param token
      *            a valid SSOToken
      * @param entryDN
      *            the entry whose parent Organization is to be obtained
      * @param childDN
      *            the immediate entry whose parent Organization is to be
      *            obtained
      * @return the DN String of the parent Organization
      * @throws AMException
      *             if an error occured while obtaining the parent Organization
      */
     public String verifyAndGetOrgDN(SSOToken token, String entryDN,
             String childDN) throws AMException {
         if (entryDN.equals("") || !DN.isDN(entryDN)) {
             debug.error("DirectoryManager.verifyAndGetOrgDN() Invalid DN: "
                     + entryDN);
             throw new AMException(token, "157");
         }
 
         String organizationDN = null;
         boolean errorCondition = false;
         try {
             PersistentObject po = UMSObject.getObjectHandle(internalToken,
                     new Guid(childDN));
 
             String searchFilter = getOrgSearchFilter(entryDN);
             SearchResults result = po.search(searchFilter, aName, scontrol);
 
             if (result.hasMoreElements()) { // found the Organization
                 // This loop/iteration of the searchresult is to avoid
                 // forceful abandon and to avoid multiple
                 // ABANDON logged in directory server access logs.
                 while (result.hasMoreElements()) {
                     result.next();
                 }
                 organizationDN = po.getGuid().toString().toLowerCase();
             }
         } catch (InvalidSearchFilterException e) {
             errorCondition = true;
             debug.error("DirectoryManager.verifyAndGetOrgDN(): Invalid "
                     + "search filter, unable to get Parent Organization: ", e);
         } catch (UMSException ue) {
             errorCondition = true;
             if (debug.warningEnabled()) {
                 debug.warning(
                         "DirectoryManager.verifyAndGetOrgDN(): Unable to "
                                 + "Obtain Parent Organization", ue);
             }
             LDAPException lex = (LDAPException) ue.getRootCause();
             int errorCode = lex.getLDAPResultCode();
             if (retryErrorCodes.contains("" + errorCode)) {
                 throw new AMException(token, Integer.toString(errorCode));
             }
         }
 
         if (errorCondition) {
             String locale = CommonUtils.getUserLocale(token);
             throw new AMException(AMSDKBundle.getString("124", locale), "124");
         }
         return organizationDN;
     }
 
     // Registering for notification
     public void addListener(SSOToken token, AMObjectListener listener,
             Map configMap) throws AMEventManagerException {
         // Validate SSOToken
         try {
             SSOTokenManager.getInstance().validateToken(token);
         } catch (SSOException ssoe) {
             throw (new AMEventManagerException(ssoe.getMessage(), "902"));
         }
 
         // Add to listeners
         synchronized (listeners) {
             listeners.put(listener, configMap);
             // Check if event service has been started
             if (eventManager == null) {
                 eventManager = new AMEventManager(listeners);
                 eventManager.start();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#assignService(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map, java.lang.String,
      *      java.lang.String)
      */
     public void assignService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#create(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      java.lang.String)
      */
     public AMIdentity create(SSOToken token, IdType type, String name,
             Map attrMap, String amOrgName) throws IdRepoException, SSOException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#delete(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.lang.String)
      */
     public void delete(SSOToken token, IdType type, String name,
             String orgName, String amsdkDN) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getAssignedServices(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      java.lang.String, java.lang.String)
      */
     public Set getAssignedServices(SSOToken token, IdType type, String name,
             Map mapOfServiceNamesAndOCs, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set,
      *      java.lang.String, java.lang.String)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.lang.String)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getMembers(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.idm.IdType)
      */
     public Set getMembers(SSOToken token, IdType type, String name,
             String amOrgName, IdType membersType, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getMemberships(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public Set getMemberships(SSOToken token, IdType type, String name,
             IdType membershipType, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getServiceAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Set, java.lang.String, java.lang.String)
      */
     public Map getServiceAttributes(SSOToken token, IdType type, String name,
             String serviceName, Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getSupportedOperations(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public Set getSupportedOperations(SSOToken token, IdType type,
             String amOrgName) throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#getSupportedTypes(
      *      com.iplanet.sso.SSOToken,
      *      java.lang.String)
      */
     public Set getSupportedTypes(SSOToken token, String amOrgName)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#isExists(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String)
      */
     public boolean isExists(SSOToken token, IdType type, String name,
             String amOrgName) throws SSOException, IdRepoException {
         // TODO Auto-generated method stub
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#isActive(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.lang.String)
      */
     public boolean isActive(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws SSOException,
             IdRepoException {
         // TODO Auto-generated method stub
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#modifyMemberShip(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set,
      *      com.sun.identity.idm.IdType, int, java.lang.String)
      */
     public void modifyMemberShip(SSOToken token, IdType type, String name,
             Set members, IdType membersType, int operation, String amOrgName)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#modifyService(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map, java.lang.String,
      *      java.lang.String)
      */
     public void modifyService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#removeAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set,
      *      java.lang.String, java.lang.String)
      */
     public void removeAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#search(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdSearchControl, java.lang.String)
      */
     public IdSearchResults search(SSOToken token, IdType type, String pattern,
             IdSearchControl ctrl, String amOrgName) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#search(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean, int, int, java.util.Set, java.lang.String)
      */
     public IdSearchResults search(SSOToken token, IdType type, String pattern,
             Map avPairs, boolean recursive, int maxResults, int maxTime,
             Set returnAttrs, String amOrgName) throws IdRepoException,
             SSOException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#setAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean, java.lang.String, java.lang.String)
      */
     public void setAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.iplanet.am.sdk.DirectoryManagerInterface#unassignService(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Map, java.lang.String, java.lang.String)
      */
     public void unassignService(SSOToken token, IdType type, String name,
             String serviceName, Map attrMap, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         // TODO Auto-generated method stub
 
     }
 }
