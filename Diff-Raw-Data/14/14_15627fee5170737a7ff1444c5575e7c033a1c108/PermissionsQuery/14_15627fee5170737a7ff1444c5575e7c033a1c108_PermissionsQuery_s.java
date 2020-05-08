 package de.escidoc.core.aa.filter;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import de.escidoc.core.aa.business.interfaces.UserAccountHandlerInterface;
 import de.escidoc.core.aa.business.interfaces.UserGroupHandlerInterface;
 import de.escidoc.core.aa.business.persistence.RoleGrant;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.resources.DbResourceCache;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 
 /**
  * Encapsulate the additional work which has to be done to get the permission
  * filter queries mainly for Lucene filtering.
  * 
  * @spring.bean id="filter.PermissionsQuery"
  * @author Andr&eacute; Schenk
  */
 public class PermissionsQuery extends DbResourceCache {
     /**
      * Logging goes there.
      */
     private static final AppLogger LOG = new AppLogger(
         PermissionsQuery.class.getName());
 
     private TripleStoreUtility tripleStoreUtility = null;
 
     private UserAccountHandlerInterface userAccountHandler = null;
 
     private UserGroupHandlerInterface userGroupHandler = null;
 
     /**
      * Create a new LucenePermissions object.
      * 
      * @throws IOException
      *             not thrown here
      */
     public PermissionsQuery() throws IOException {
     }
 
     /**
      * This method is not used here.
      * 
      * @param id
      *            resource id
      */
     @Override
     protected void deleteResource(final String id) {
     }
 
     /**
      * Get the list of all child containers for all containers the user is
      * granted to.
      * 
      * @param userGrants
      *            user grants of the user
      * @param userGroupGrants
      *            user group grants of the user
      * 
      * @return list of all child containers
      */
     public Set<String> getHierarchicalContainers(
         final Set<String> userGrants, final Set<String> userGroupGrants) {
         Set<String> result = new HashSet<String>();
 
         try {
             for (String grant : userGrants) {
                 List<String> childContainers =
                     tripleStoreUtility.getAllChildContainers(grant);
 
                 result.add(grant);
                 if (childContainers != null) {
                     result.addAll(childContainers);
                 }
             }
             for (String grant : userGroupGrants) {
                 List<String> childContainers =
                     tripleStoreUtility.getAllChildContainers(grant);
 
                 result.add(grant);
                 if (childContainers != null) {
                     result.addAll(childContainers);
                 }
             }
         }
         catch (TripleStoreSystemException e) {
             LOG.error("getting child containers from database failed", e);
         }
         return result;
     }
 
     /**
      * Get the list of all child OUs for all OUs the user is granted to.
      * 
      * @param userGrants
      *            user grants of the user
      * @param userGroupGrants
      *            user group grants of the user
      * 
      * @return list of all child OUs
      */
     public Set<String> getHierarchicalOUs(
         final Set<String> userGrants, final Set<String> userGroupGrants) {
         Set<String> result = new HashSet<String>();
 
         try {
             for (String grant : userGrants) {
                 List<String> childOUs =
                     tripleStoreUtility.getAllChildOUs(grant);
 
                 result.add(grant);
                 if (childOUs != null) {
                     result.addAll(childOUs);
                 }
             }
             for (String grant : userGroupGrants) {
                 List<String> childOUs =
                     tripleStoreUtility.getAllChildOUs(grant);
 
                 result.add(grant);
                 if (childOUs != null) {
                     result.addAll(childOUs);
                 }
             }
         }
         catch (TripleStoreSystemException e) {
             LOG.error("getting child OUs from database failed", e);
         }
         return result;
     }
 
     /**
      * Get the resource type from the given HREF.
      * 
      * @param href
      *            HREF to an eSciDoc resource
      * 
      * @return resource type for that HREF
      */
     private ResourceType getResourceTypeFromHref(final String href) {
         ResourceType result = null;
 
         if (href != null) {
             if (href.startsWith(Constants.CONTAINER_URL_BASE)) {
                 result = ResourceType.CONTAINER;
             }
             else if (href.startsWith(Constants.CONTEXT_URL_BASE)) {
                 result = ResourceType.CONTEXT;
             }
             else if (href.startsWith(Constants.ITEM_URL_BASE)) {
                 result = ResourceType.ITEM;
             }
             else if (href.startsWith(Constants.ORGANIZATIONAL_UNIT_URL_BASE)) {
                 result = ResourceType.OU;
             }
         }
         return result;
     }
 
     /**
      * Get all grants directly assigned to the given user for the given resource
      * type.
      * 
      * @param resourceType
      *            resource type
      * @param userId
      *            user id
      * @param optimize
      *            ignore all grants which are not granted to the same resource
      *            type as the given resource type
      * 
      * @return all direct grants for the user
      */
     public Set<String> getUserGrants(
         final ResourceType resourceType, final String userId,
         final boolean optimize) {
         Set<String> result = new HashSet<String>();
 
         if ((userId != null) && (userId.length() > 0)) {
             try {
                 final Map<String, Map<String, List<RoleGrant>>> currentRoleGrantMap =
                     userAccountHandler.retrieveCurrentGrantsAsMap(userId);
 
                 if (currentRoleGrantMap != null) {
                     for (String role : currentRoleGrantMap.keySet()) {
                         final Map<String, List<RoleGrant>> currentGrantMap =
                             currentRoleGrantMap.get(role);
 
                         for (String objectId : currentGrantMap.keySet()) {
                             final List<RoleGrant> currentGrants =
                                 currentGrantMap.get(objectId);
 
                             for (RoleGrant grant : currentGrants) {
                                 final String objectHref = grant.getObjectHref();
 
                                 if (!optimize || (objectHref == null)) {
                                     result.add(objectId);
                                     break;
                                 }
                                 else {
                                     final ResourceType grantType =
                                         getResourceTypeFromHref(objectHref);
 
                                     if (grantType == resourceType) {
                                         result.add(objectId);
                                         break;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             catch (Exception e) {
                 LOG.error("getting the user grants from AA failed", e);
             }
         }
         return result;
     }
 
     /**
      * Get all group grants assigned to the given user.
      * 
      * @param userId
      *            user id
      * @param optimize
      *            ignore all grants which are not granted to the same resource
      *            type as the given resource type
      * 
      * @return all group grants for the user
      */
     public Set<String> getUserGroupGrants(
         final String userId, final boolean optimize) {
         Set<String> result = new HashSet<String>();
 
         if ((userId != null) && (userId.length() > 0)) {
             try {
                 Set<String> groupIds =
                     userGroupHandler.retrieveGroupsForUser(userId);
 
                 if (groupIds != null) {
                     for (String groupId : groupIds) {
                         final Map<String, Map<String, List<RoleGrant>>> currentRoleGrantMap =
                             userGroupHandler
                                 .retrieveCurrentGrantsAsMap(groupId);
 
                         if (currentRoleGrantMap != null) {
                             for (String role : currentRoleGrantMap.keySet()) {
                                 final Map<String, List<RoleGrant>> currentGrantMap =
                                     currentRoleGrantMap.get(role);
 
                                 for (String objectId : currentGrantMap.keySet()) {
                                     final List<RoleGrant> currentGrants =
                                         currentGrantMap.get(objectId);
 
                                     for (RoleGrant grant : currentGrants) {
                                         final String objectHref =
                                             grant.getObjectHref();
 
                                         if (!optimize || (objectHref == null)) {
                                             result.add(objectId);
                                             break;
                                         }
                                         else {
                                             final ResourceType grantType =
                                                 getResourceTypeFromHref(objectHref);
 
                                             if (grantType == resourceType) {
                                                 result.add(objectId);
                                                 break;
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             catch (Exception e) {
                 LOG.error("getting the user group grants from AA failed", e);
             }
         }
         return result;
     }
 
     /**
      * Injects the data source.
      * 
      * @spring.property ref="escidoc-core.DataSource"
      * @param myDataSource
      *            data source from Spring
      */
     public void setMyDataSource(final DataSource myDataSource) {
         super.setDataSource(myDataSource);
     }
 
     /**
      * Injects the TripleStore utility.
      * 
      * @spring.property ref="business.TripleStoreUtility"
      * @param tripleStoreUtility
      *            TripleStoreUtility from Spring
      */
     public void setTripleStoreUtility(
         final TripleStoreUtility tripleStoreUtility) {
         this.tripleStoreUtility = tripleStoreUtility;
     }
 
     /**
      * Injects the user account handler.
      * 
      * @spring.property ref="business.UserAccountHandler"
      * @param userAccountHandler
      *            user account handler from Spring
      */
     public void setUserAccountHandler(
         final UserAccountHandlerInterface userAccountHandler) {
         this.userAccountHandler = userAccountHandler;
     }
 
     /**
      * Injects the user group handler.
      * 
      * @spring.property ref="business.UserGroupHandler"
      * @param userGroupHandler
      *            user group handler from Spring
      */
     public void setUserGroupHandler(
         final UserGroupHandlerInterface userGroupHandler) {
         this.userGroupHandler = userGroupHandler;
     }
 
     /**
      * This method is not used for Lucene filtering.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      */
     @Override
     protected void storeResource(
         final String id, final String restXml, final String soapXml) {
     }
 }
