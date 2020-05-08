 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.aa.business;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger; import org.slf4j.LoggerFactory;
 import org.springframework.security.userdetails.UserDetails;
 
 import de.escidoc.core.aa.business.cache.PoliciesCache;
 import de.escidoc.core.aa.business.filter.PermissionsQuery;
 import de.escidoc.core.aa.business.filter.RoleGrantFilter;
 import de.escidoc.core.aa.business.filter.UserAccountFilter;
 import de.escidoc.core.aa.business.interfaces.PolicyDecisionPointInterface;
 import de.escidoc.core.aa.business.interfaces.UserAccountHandlerInterface;
 import de.escidoc.core.aa.business.interfaces.UserGroupHandlerInterface;
 import de.escidoc.core.aa.business.persistence.EscidocRole;
 import de.escidoc.core.aa.business.persistence.EscidocRoleDaoInterface;
 import de.escidoc.core.aa.business.persistence.RoleGrant;
 import de.escidoc.core.aa.business.persistence.ScopeDef;
 import de.escidoc.core.aa.business.persistence.UserAccount;
 import de.escidoc.core.aa.business.persistence.UserAccountDaoInterface;
 import de.escidoc.core.aa.business.persistence.UserAttribute;
 import de.escidoc.core.aa.business.persistence.UserGroup;
 import de.escidoc.core.aa.business.persistence.UserGroupDaoInterface;
 import de.escidoc.core.aa.business.persistence.UserGroupMember;
 import de.escidoc.core.aa.business.persistence.UserLoginData;
 import de.escidoc.core.aa.business.persistence.UserPreference;
 import de.escidoc.core.aa.business.renderer.interfaces.UserAccountRendererInterface;
 import de.escidoc.core.aa.business.stax.handler.GrantStaxHandler;
 import de.escidoc.core.aa.business.stax.handler.RevokeStaxHandler;
 import de.escidoc.core.aa.business.stax.handler.UserAccountPropertiesStaxHandler;
 import de.escidoc.core.aa.business.stax.handler.UserAttributeReadHandler;
 import de.escidoc.core.aa.business.stax.handler.UserPreferenceReadHandler;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FilterInterface;
 import de.escidoc.core.common.business.filter.DbRequestParameters;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidScopeException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.GrantNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.PreferenceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RoleNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.UserAccountNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.UserAttributeNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.UserGroupNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyActiveException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyDeactiveException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyRevokedException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.service.BeanLocator;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
 import de.escidoc.core.common.util.stax.handler.filter.FilterHandler;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ExplainXmlProvider;
 import de.escidoc.core.common.util.xml.stax.StaxParser;
 import de.escidoc.core.common.util.xml.stax.handler.LinkStaxHandler;
 import de.escidoc.core.common.util.xml.stax.handler.OptimisticLockingStaxHandler;
 
 /**
  * Implementation for the account handle.
  *
  * @author Michael Schneider
  */
 public class UserAccountHandler
     implements UserAccountHandlerInterface {
 
     /**
      * The logger.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountHandler.class);
 
     private static final String XPATH_GRANT_ASSIGNED_ON = '/'
         + XmlUtility.NAME_GRANT + '/' + XmlUtility.NAME_PROPERTIES + '/'
         + XmlUtility.NAME_ASSIGNED_ON;
 
     private static final String XPATH_GRANT_ROLE = '/' + XmlUtility.NAME_GRANT
         + '/' + XmlUtility.NAME_PROPERTIES + '/' + XmlUtility.NAME_ROLE;
 
     private static final Pattern GROUP_FILTER_PATTERN = Pattern
         .compile("(?s)\"{0,1}(" + Constants.FILTER_GROUP + '|'
             + Constants.FILTER_PATH_USER_ACCOUNT_GROUP_ID
             + ")(\"*\\s*([=<>]+)\\s*\"*|\"*\\s*(any)\\s*\"*"
             + "|\"*\\s*(cql.any)\\s*\"*)" + "([^\\s\"\\(\\)]*)\"{0,1}");
 
     private static final String MSG_WRONG_HREF =
         "Referenced object href is wrong, object has another type.";
 
     private static final String MSG_GRANT_RESTRICTION_VIOLATED =
         "Grants can be created on containers, content models, contexts"
             + ", items, components, organizational units, user-accounts, "
             + "user-groups and scopes, only. No resource of one "
             + "of these types with the provided id exists.";
 
     private static final String MSG_USER_NOT_FOUND_BY_ID =
         "User with provided id does not exist.";
 
     private static final String MSG_USER_NOT_FOUND_BY_IDENTITY_INFO =
         "User with provided user identity does not exist.";
 
     private static final String MSG_GROUP_NOT_FOUND_BY_ID =
         "User-Group with provided id does not exist.";
 
     private static final String MSG_UNEXPECTED_EXCEPTION =
         "Unexpected exception in ";
 
     private static final String MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS =
         "Unexpected exception during evaluating access rights.";
 
     private static final String MSG_XML_SCHEMA_ENSURE =
         "Should be ensured by XML Schema.";
 
     private static final int MAX_FIELD_LENGTH = 245;
 
     private UserAccountDaoInterface dao;
 
     private UserGroupDaoInterface userGroupDao;
 
     private ObjectAttributeResolver objectAttributeResolver;
 
     private UserAccountRendererInterface renderer;
 
     private EscidocRoleDaoInterface roleDao;
 
     private PolicyDecisionPointInterface pdp;
 
     private UserGroupHandlerInterface userGroupHandler;
 
     private PermissionsQuery permissionsQuery;
 
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return user-account data as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieve(java.lang.String)
      */
     @Override
     public String retrieve(final String userId)
         throws UserAccountNotFoundException, SystemException {
 
         final UserAccount userAccount = dao.retrieveUserAccount(userId);
         assertUserAccount(userId, userAccount);
 
         return renderer.render(userAccount);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @return user-account data as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveCurrentUser(java.lang.String)
      */
     @Override
     public String retrieveCurrentUser() throws UserAccountNotFoundException,
         SystemException {
         if (StringUtils.isEmpty(UserContext.getId())) {
             throw new UserAccountNotFoundException("No user logged in");
         }
         final UserAccount userAccount =
             dao.retrieveUserAccount(UserContext.getId());
         assertUserAccount(UserContext.getId(), userAccount);
 
         return renderer.render(userAccount);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param xmlData
      *            data for user-account
      * @return user-data as xml
      * @throws UniqueConstraintViolationException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws OrganizationalUnitNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @throws InvalidStatusException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #create(java.lang.String)
      */
     @Override
     public String create(final String xmlData)
         throws UniqueConstraintViolationException, XmlCorruptedException,
         OrganizationalUnitNotFoundException, SystemException,
         InvalidStatusException {
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(xmlData);
 
         final UserAccount userAccount = new UserAccount();
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_USER_ACCOUNT);
 
         final UserAccountPropertiesStaxHandler propertiesHandler =
             new UserAccountPropertiesStaxHandler(userAccount, this.dao, true);
         sp.addHandler(propertiesHandler);
 
         try {
             sp.parse(in);
         }
         catch (final UniqueConstraintViolationException e) {
             throw e;
         }
         catch (final InvalidStatusException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName() + ".create: "
                     + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         // A created user account is in state active
         userAccount.setActive(Boolean.TRUE);
 
         setModificationValues(userAccount);
         setCreationValues(userAccount);
 
         // FIXME: The default password "PubManR2" is set to the created
         // user to test creation of user accounts (discussed with MPDL, Natasa
         // Bulatovic). Once the identity management has been integrated, this
         // should be removed, because after that, user passwords will not be
         // used, anymore.
         userAccount.setPassword("PubManR2");
 
         dao.save(userAccount);
 
         return renderer.render(userAccount);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #delete(java.lang.String)
      */
     @Override
     public void delete(final String userId)
         throws UserAccountNotFoundException, SystemException {
 
         dao.delete(retrieveUserAccountById(userId));
 
         sendUserAccountUpdateEvent(userId);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param xmlData
      *            xml with updated data
      * @return updated user as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws UniqueConstraintViolationException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws OrganizationalUnitNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @throws InvalidStatusException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #update(java.lang.String, java.lang.String)
      */
     @Override
     public String update(final String userId, final String xmlData)
         throws UserAccountNotFoundException,
         UniqueConstraintViolationException, XmlCorruptedException,
         MissingAttributeValueException, OptimisticLockingException,
         OrganizationalUnitNotFoundException, SystemException,
         InvalidStatusException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(xmlData);
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_USER_ACCOUNT);
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         final UserAccountPropertiesStaxHandler propertiesHandler =
             new UserAccountPropertiesStaxHandler(userAccount, this.dao, false);
         sp.addHandler(propertiesHandler);
 
         try {
             sp.parse(in);
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final UniqueConstraintViolationException e) {
             throw e;
         }
         catch (final InvalidStatusException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName() + ".parse: "
                     + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         setModificationValues(userAccount);
 
         dao.update(userAccount);
 
         return renderer.render(userAccount);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param taskParam
      *            password
      * @throws UserAccountNotFoundException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws AuthenticationException
      *             e
      * @throws AuthorizationException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface#updatePassword(String,
      *      String)
      */
     @Override
     public void updatePassword(final String userId, final String taskParam)
         throws UserAccountNotFoundException, InvalidStatusException,
         XmlCorruptedException, MissingMethodParameterException,
         OptimisticLockingException, AuthenticationException,
         AuthorizationException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         if (!userAccount.getActive()) {
             throw new InvalidStatusException(
                 "Password must not be updated on inactive user-account!");
         }
 
         final TaskParamHandler handler =
             XmlUtility.parseTaskParam(taskParam, true);
 
         final String password = handler.getPassword();
         if (password == null || "".equals(password)) {
             throw new MissingMethodParameterException(
                 "Password must not be null or empty!");
         }
         userAccount.setPassword(password);
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(taskParam);
         final StaxParser sp = new StaxParser("param");
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
         try {
             sp.parse(in);
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         setModificationValues(userAccount);
         dao.update(userAccount);
         sendUserAccountUpdateEvent(userId);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return resources as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveResources(java.lang.String)
      */
     @Override
     public String retrieveResources(final String userId)
         throws UserAccountNotFoundException, SystemException {
 
         return renderer.renderResources(retrieveUserAccountById(userId));
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return grants as map
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see UserAccountHandlerInterface
      *      #retrieveCurrentGrantsAsMap(java.lang.String)
      */
     @Override
     public Map<String, Map<String, List<RoleGrant>>> retrieveCurrentGrantsAsMap(
         final String userId) throws UserAccountNotFoundException,
         SystemException {
 
         retrieveUserAccountById(userId);
         final List<RoleGrant> currentGrants = fetchCurrentGrants(userId);
         if (currentGrants == null || currentGrants.isEmpty()) {
             return null;
         }
 
         final Iterator<RoleGrant> iter = currentGrants.iterator();
         final Map<String, Map<String, List<RoleGrant>>> ret =
             new HashMap<String, Map<String, List<RoleGrant>>>();
         while (iter.hasNext()) {
             final RoleGrant grant = iter.next();
             final String roleId = grant.getEscidocRole().getId();
             Map<String, List<RoleGrant>> grantsOfRole = ret.get(roleId);
             if (grantsOfRole == null) {
                 grantsOfRole = new HashMap<String, List<RoleGrant>>();
                 ret.put(roleId, grantsOfRole);
             }
             String key = grant.getObjectId();
             if (key == null) {
                 // For grants of unlimited roles an empty string is used
                 // as the key.
                 key = "";
             }
             List<RoleGrant> grantsOfObject = grantsOfRole.get(key);
             if (grantsOfObject == null) {
                 grantsOfObject = new ArrayList<RoleGrant>();
                 grantsOfRole.put(key, grantsOfObject);
             }
             grantsOfObject.add(grant);
         }
         return ret;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return grants as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveCurrentGrants(java.lang.String)
      */
     @Override
     public String retrieveCurrentGrants(final String userId)
         throws UserAccountNotFoundException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final List<RoleGrant> currentGrants = fetchCurrentGrants(userId);
         final HashMap<String, RoleGrant> grantsMap =
             new HashMap<String, RoleGrant>();
         final List<Object[]> argumentList = new ArrayList<Object[]>();
         final List<RoleGrant> filteredCurrentGrants =
             new ArrayList<RoleGrant>();
 
         // AA-filter
         for (final RoleGrant roleGrant : currentGrants) {
             grantsMap.put(roleGrant.getId(), roleGrant);
             final Object[] args = { userId, roleGrant.getId() };
             argumentList.add(args);
         }
         try {
             final List<Object[]> returnList =
                 pdp.evaluateMethodForList("user-account", "retrieveGrant",
                     argumentList);
             for (final Object[] obj : returnList) {
                 filteredCurrentGrants.add(grantsMap.get(obj[1]));
             }
         }
         catch (final MissingMethodParameterException e) {
             throw new SystemException(MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
         }
         catch (final ResourceNotFoundException e) {
             throw new SystemException(MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
         }
 
         return renderer.renderCurrentGrants(userAccount, filteredCurrentGrants);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param filter
      *            role grant filter
      * 
      * @return list of filtered role grants
      * @throws InvalidSearchQueryException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveGrants(java.util.Map)
      */
     @Override
     public String retrieveGrants(final Map<String, String[]> filter)
         throws InvalidSearchQueryException, SystemException {
 
         final SRURequestParameters parameters = new DbRequestParameters(filter);
 
         final String query = parameters.getQuery();
         final int limit = parameters.getMaximumRecords();
         final int offset = parameters.getStartRecord();
         final boolean explain = parameters.isExplain();
 
         final String result;
         if (explain) {
             final Map<String, Object> values = new HashMap<String, Object>();
 
             values.put("PROPERTY_NAMES",
                 new RoleGrantFilter(null).getPropertyNames());
             result =
                 ExplainXmlProvider.getInstance().getExplainRoleGrantXml(values);
         }
         else if (limit == 0) {
             result =
                 renderer.renderGrants(new ArrayList<RoleGrant>(0),
                     Integer.toString(0), Integer.toString(offset),
                     Integer.toString(limit), parameters.getRecordPacking());
         }
         else {
             final int needed = offset + limit;
             final List<RoleGrant> permittedRoleGrants =
                 new ArrayList<RoleGrant>();
 
             final List<RoleGrant> tmpRoleGrants =
                 dao.retrieveGrants(query, 0, 0, this.userGroupHandler);
             if (tmpRoleGrants != null && !tmpRoleGrants.isEmpty()) {
                 final List<String> userIds = new ArrayList<String>();
                 final List<String> groupIds = new ArrayList<String>();
                 for (final RoleGrant roleGrant : tmpRoleGrants) {
                     if (roleGrant.getUserId() != null
                         && !userIds.contains(roleGrant.getUserId())) {
                         userIds.add(roleGrant.getUserId());
                     }
                     else if (roleGrant.getGroupId() != null
                         && !groupIds.contains(roleGrant.getGroupId())) {
                         groupIds.add(roleGrant.getGroupId());
                     }
                 }
 
                 try {
                     List<String> tmpUsersPermitted = new ArrayList<String>();
                     List<String> tmpGroupsPermitted = new ArrayList<String>();
                     if (!userIds.isEmpty()) {
                         tmpUsersPermitted =
                             pdp.evaluateRetrieve("user-account", userIds);
                     }
                     if (!groupIds.isEmpty()) {
                         tmpGroupsPermitted =
                             pdp.evaluateRetrieve("user-group", groupIds);
                     }
                     if (!tmpUsersPermitted.isEmpty()
                         || !tmpGroupsPermitted.isEmpty()) {
                         for (final RoleGrant roleGrant : tmpRoleGrants) {
                             if (roleGrant.getUserId() != null) {
                                 if (tmpUsersPermitted.contains(roleGrant
                                     .getUserId())) {
                                     permittedRoleGrants.add(roleGrant);
                                 }
                             }
                             else if (roleGrant.getGroupId() != null
                                 && tmpGroupsPermitted.contains(roleGrant
                                     .getGroupId())) {
                                 permittedRoleGrants.add(roleGrant);
                             }
                         }
                     }
                 }
                 catch (final MissingMethodParameterException e) {
                     throw new SystemException(
                         MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
                 }
                 catch (final ResourceNotFoundException e) {
                     throw new SystemException(
                         MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
                 }
             }
 
             final List<RoleGrant> offsetRoleGrants;
             final int numberPermitted = permittedRoleGrants.size();
             if (offset >= 0 && offset < numberPermitted) {
                 offsetRoleGrants = new ArrayList<RoleGrant>(limit);
                 for (int i = offset; i < numberPermitted && i < needed; i++) {
                     offsetRoleGrants.add(permittedRoleGrants.get(i));
                 }
             }
             else {
                 offsetRoleGrants = new ArrayList<RoleGrant>(0);
             }
             result =
                 renderer.renderGrants(offsetRoleGrants,
                     Integer.toString(numberPermitted),
                     Integer.toString(offset), Integer.toString(limit),
                     parameters.getRecordPacking());
         }
         return result;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param grantId
      *            grantId
      * @return grant as xml
      * @throws GrantNotFoundException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveGrant(java.lang.String, java.lang.String)
      */
     @Override
     public String retrieveGrant(final String userId, final String grantId)
         throws GrantNotFoundException, UserAccountNotFoundException,
         SystemException {
 
         return renderer.renderGrant(retrieveGrantByIds(userId, grantId));
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param taskParam
      *            taskParam
      * @throws AlreadyActiveException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #activate(java.lang.String, java.lang.String)
      */
     @Override
     public void activate(final String userId, final String taskParam)
         throws AlreadyActiveException, UserAccountNotFoundException,
         XmlCorruptedException, MissingAttributeValueException,
         OptimisticLockingException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         // TODO: validation missing, check if needed or if it shall be skipped
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_PARAM);
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         try {
             sp.parse(XmlUtility.convertToByteArrayInputStream(taskParam));
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName() + ".activate: "
                     + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         // check active flag and change value
         if (userAccount.getActive()) {
             throw new AlreadyActiveException();
         }
         userAccount.setActive(Boolean.TRUE);
 
         setModificationValues(userAccount);
 
         dao.update(userAccount);
 
         sendUserAccountUpdateEvent(userId);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param taskParam
      *            taskParam
      * @throws AlreadyDeactiveException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #deactivate(java.lang.String, java.lang.String)
      */
     @Override
     public void deactivate(final String userId, final String taskParam)
         throws AlreadyDeactiveException, UserAccountNotFoundException,
         XmlCorruptedException, MissingAttributeValueException,
         OptimisticLockingException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         // TODO: validation missing, check if needed or if it shall be skipped
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_PARAM);
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         try {
             sp.parse(XmlUtility.convertToByteArrayInputStream(taskParam));
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".deactivate: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         // check ative flag and change value
         if (!userAccount.getActive()) {
             throw new AlreadyDeactiveException();
         }
         userAccount.setActive(Boolean.FALSE);
 
         setModificationValues(userAccount);
 
         dao.update(userAccount);
 
         sendUserAccountUpdateEvent(userId);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param grantXML
      *            grantXml
      * @return created grant as xml
      * @throws AlreadyExistsException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws InvalidScopeException
      *             e
      * @throws RoleNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #createGrant(java.lang.String, java.lang.String)
      */
     @Override
     public String createGrant(final String userId, final String grantXML)
         throws AlreadyExistsException, UserAccountNotFoundException,
         InvalidScopeException, RoleNotFoundException, XmlCorruptedException,
         SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final RoleGrant grant = new RoleGrant();
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(grantXML);
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_GRANT);
 
         final LinkStaxHandler roleLinkHandler =
             new LinkStaxHandler(XPATH_GRANT_ROLE, XmlUtility.BASE_ROLE,
                 RoleNotFoundException.class);
         sp.addHandler(roleLinkHandler);
         final LinkStaxHandler objectLinkHandler =
             new LinkStaxHandler(XPATH_GRANT_ASSIGNED_ON);
         sp.addHandler(objectLinkHandler);
 
         final GrantStaxHandler grantHandler = new GrantStaxHandler(grant);
         sp.addHandler(grantHandler);
 
         try {
             sp.parse(in);
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final RoleNotFoundException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".createGrant: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         final Date creationDate = new Date();
         grant.setCreationDate(creationDate);
         grant.setUserAccountByCreatorId(getAuthenticatedUser(this.dao));
 
         final String roleId = roleLinkHandler.getObjid();
         final EscidocRole role = roleDao.retrieveRole(roleId);
         if (role == null) {
             throw new RoleNotFoundException(StringUtility.format(
                 "Role with provided id not found", roleId));
         }
         grant.setEscidocRole(role);
 
         final String objectId = objectLinkHandler.getObjid();
 
         // check if referenced object exists and determine its object type
         // it is assumed, that each resource stored in fedora has an object
         // type stored in the triple store.
 
         // FIXME: inject Triplestoreutility
 
         if (objectId != null) {
             final Map<String, String> objectAttributes;
             try {
                 objectAttributes =
                     objectAttributeResolver.resolveObjectAttributes(objectId);
             }
             catch (final Exception e) {
                 throw new SystemException(e);
             }
 
             if (objectAttributes == null) {
                 throw new XmlCorruptedException(StringUtility.format(
                     MSG_GRANT_RESTRICTION_VIOLATED, objectId));
             }
             final String objectType =
                 objectAttributes.get(ObjectAttributeResolver.ATTR_OBJECT_TYPE);
             String objectTitle =
                 objectAttributes.get(ObjectAttributeResolver.ATTR_OBJECT_TITLE);
 
             // check if objectType may be scope
             boolean checkOk = false;
             if (role.getScopeDefs() != null && objectType != null) {
                 for (final ScopeDef scopeDef : role.getScopeDefs()) {
                     if (scopeDef.getAttributeObjectType() != null
                         && scopeDef.getAttributeObjectType().equals(objectType)) {
                         checkOk = true;
                         break;
                     }
                 }
             }
             if (!checkOk) {
                 throw new InvalidScopeException("objectId " + objectId
                     + " has objectType " + objectType
                     + " and may not be scope for role " + role.getRoleName());
             }
 
             // see issue 358. The title of an object stored in fedora may
             // not be explicitly stored in the triple store.
             // Therefore, a default title will be set, if it is null, here.
             if (objectTitle == null) {
                 objectTitle =
                     StringUtility.convertToUpperCaseLetterFormat(objectType)
                         + " " + objectId;
             }
             else if (objectTitle.length() > MAX_FIELD_LENGTH) {
                 objectTitle = objectTitle.substring(0, MAX_FIELD_LENGTH);
             }
 
             // get the href of the object.
             final String objectHref = XmlUtility.getHref(objectType, objectId);
 
             // In case of REST it has to be checked if the provided href points
             // to the correct href.
             if (objectLinkHandler.getHref() != null
                 && !objectLinkHandler.getHref().equals(objectHref)) {
                 // FIXME: exception should be a resource not found exception
                 // but this changes the interface. To prevent problems on
                 // application side, currently an XmlCorruptedException is
                 // thrown.
                 throw new XmlCorruptedException(StringUtility.format(
                     MSG_WRONG_HREF, objectLinkHandler.getHref(), objectType));
             }
 
             // check if grant already exists
             if (dao.retrieveCurrentGrant(userAccount, role, objectId) != null) {
                 throw new AlreadyExistsException(StringUtility.format(
                     "Grant already exists", userId, role.getId(), objectId));
             }
 
             // set object values in grant
             grant.setObjectId(objectId);
             grant.setObjectTitle(objectTitle);
             grant.setObjectHref(objectHref);
         }
 
         grant.setUserAccountByUserId(userAccount);
 
         dao.save(grant);
 
         sendUserAccountUpdateEvent(userId);
 
         return renderer.renderGrant(grant);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param grantId
      *            grantId
      * @param taskParam
      *            taskParam
      * @throws UserAccountNotFoundException
      *             e
      * @throws GrantNotFoundException
      *             e
      * @throws AlreadyRevokedException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #revokeGrant(java.lang.String, java.lang.String, java.lang.String)
      */
     @Override
     public void revokeGrant(
         final String userId, final String grantId, final String taskParam)
         throws UserAccountNotFoundException, GrantNotFoundException,
         AlreadyRevokedException, XmlCorruptedException,
         MissingAttributeValueException, SystemException {
 
         final RoleGrant grant = retrieveGrantByIds(userId, grantId);
 
         // TODO: validation missing, check if needed or if it shall be skipped
 
         final StaxParser sp = new StaxParser(XmlUtility.NAME_PARAM);
 
         final GrantStaxHandler grantHandler = new GrantStaxHandler(grant);
         sp.addHandler(grantHandler);
 
         final RevokeStaxHandler revokeStaxHandler =
             new RevokeStaxHandler(grant, this.dao);
         sp.addHandler(revokeStaxHandler);
 
         try {
             sp.parse(XmlUtility.convertToByteArrayInputStream(taskParam));
         }
         catch (final AlreadyRevokedException e) {
             throw e;
         }
         catch (final InvalidXmlException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName() + ".parse: "
                     + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         dao.update(grant);
 
         sendUserAccountUpdateEvent(userId);
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param taskParam
      *            taskParam
      * @throws UserAccountNotFoundException
      *             e
      * @throws GrantNotFoundException
      *             e
      * @throws AlreadyRevokedException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws SystemException
      *             e
      * @throws AuthorizationException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #revokeGrants(java.lang.String, java.lang.String)
      */
     @Override
     // False positive: Inefficient use of keySet iterator instead of entrySet iterator
     @edu.umd.cs.findbugs.annotations.SuppressWarnings
     public void revokeGrants(final String userId, final String taskParam)
         throws UserAccountNotFoundException, GrantNotFoundException,
         AlreadyRevokedException, XmlCorruptedException,
         MissingAttributeValueException, SystemException, AuthorizationException {
 
         // check if user exists
         retrieveUserAccountById(userId);
 
         // get all current grants of user
         final List<RoleGrant> grants = fetchCurrentGrants(userId);
         // build HashMap with grantId
         final HashMap<String, RoleGrant> grantsHash =
             new HashMap<String, RoleGrant>();
         for (final RoleGrant grant : grants) {
             grantsHash.put(grant.getId(), grant);
         }
 
         // Parse taskParam
         final de.escidoc.core.common.util.stax.StaxParser fp =
             new de.escidoc.core.common.util.stax.StaxParser();
 
         final TaskParamHandler tph = new TaskParamHandler(fp);
         tph.setCheckLastModificationDate(false);
         fp.addHandler(tph);
         final FilterHandler fh = new FilterHandler(fp);
         fp.addHandler(fh);
         try {
             fp.parse(new ByteArrayInputStream(taskParam
                 .getBytes(XmlUtility.CHARACTER_ENCODING)));
         }
         catch (final InvalidContentException e) {
             throw new XmlCorruptedException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         final Map<String, Object> filters = fh.getRules();
 
         final Collection<String> grantIds;
         if (filters.isEmpty()) {
             // if no filters are provided, remove all current grants
             grantIds = new HashSet<String>();
             for (final String grantId : grantsHash.keySet()) {
                 grantIds.add(grantId);
             }
         }
         else {
             // get ids of grants to revoke
             grantIds = (Collection<String>) filters.get(Constants.DC_IDENTIFIER_URI);
         }
 
         if (grantIds == null || grantIds.isEmpty()) {
             return;
         }
 
         // check if all grants that shall get revoked are currentGrants
         for (final String grantId : grantIds) {
             if (!grantsHash.containsKey(grantId)) {
                 throw new GrantNotFoundException("Grant with id " + grantId
                     + " is no current grant of user " + userId);
             }
         }
 
         // AA-filter grants to revoke
         final List<Object[]> argumentList = new ArrayList<Object[]>();
         for (final String grantId : grantIds) {
             final Object[] args = { userId, grantId };
             argumentList.add(args);
         }
         try {
             final List<Object[]> returnList =
                 pdp.evaluateMethodForList("user-account", "revokeGrant",
                     argumentList);
             if (returnList.size() < grantIds.size()) {
                 // user is not allowed to revoke at least one of the grants
                 // so throw AuthorizationException
                 throw new AuthorizationException(
                     "You are not allowed to revoke at least "
                         + "one of the specified grants");
             }
         }
         catch (final MissingMethodParameterException e) {
             throw new SystemException(MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
         }
         catch (final ResourceNotFoundException e) {
             throw new SystemException(MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
         }
 
         final UserAccount authenticateUser =
             getAuthenticatedUser(this.dao);
         try {
             for (final String grantId : grantIds) {
                 // set revoke-date, -user and -remark
                 final RoleGrant roleGrant = grantsHash.get(grantId);
                 roleGrant.setUserAccountByRevokerId(authenticateUser);
                 roleGrant.setRevocationDate(new Date());
                 roleGrant.setRevocationRemark(tph.getRevokationRemark());
                 // update grant
                 dao.update(roleGrant);
             }
 
         } catch (final Exception e) {
             throw new SqlDatabaseSystemException(e);
         }
 
         sendUserAccountUpdateEvent(userId);
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return List of userHandles
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see UserAccountHandlerInterface
      *      #retrieveUserHandles(java.lang.String)
      */
     @Override
     public List<UserLoginData> retrieveUserHandles(final String userId)
         throws UserAccountNotFoundException, SystemException {
 
         final List<UserLoginData> ret =
             dao.retrieveUserLoginDataByUserId(userId);
         if (ret == null || ret.isEmpty()) {
             assertUserAccount(userId, dao.retrieveUserAccountById(userId));
         }
         return ret;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param filter
      *            userAccountFilter
      * @return list of filtered user-accounts
      * @throws InvalidSearchQueryException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveUserAccounts(java.util.Map)
      */
     @Override
     public String retrieveUserAccounts(final Map<String, String[]> filter)
         throws InvalidSearchQueryException, SystemException {
 
         Map<String, String[]> castedFilter = filter;
 
         // check if filter for groupId is provided
         // if yes, get users for group and add ids to filter
         // then remove groupId from filter
         castedFilter = fixCqlGroupFilter(castedFilter);
 
         final SRURequestParameters parameters =
             new DbRequestParameters(castedFilter);
 
         final String query = parameters.getQuery();
         final int limit = parameters.getMaximumRecords();
         final int offset = parameters.getStartRecord();
         final boolean explain = parameters.isExplain();
 
         final String result;
         if (explain) {
             final Map<String, Object> values = new HashMap<String, Object>();
 
             values.put("PROPERTY_NAMES",
                 new UserAccountFilter(null).getPropertyNames());
             result =
                 ExplainXmlProvider.getInstance().getExplainUserAccountXml(
                     values);
         }
         else if (limit == 0) {
             result =
                 renderer.renderUserAccounts(new ArrayList<UserAccount>(0),
                     parameters.getRecordPacking());
         }
         else {
             final int currentLimit = offset + limit;
             int currentOffset = 0;
             final List<UserAccount> permittedUserAccounts =
                 new ArrayList<UserAccount>();
             final int size = permittedUserAccounts.size();
             while (size <= currentLimit) {
 
                 final List<UserAccount> tmpUserAccounts =
                     dao
                         .retrieveUserAccounts(query, currentOffset,
                             currentLimit);
                 if (tmpUserAccounts == null || tmpUserAccounts.isEmpty()) {
                     break;
                 }
                 Iterator<UserAccount> userAccountIter =
                     tmpUserAccounts.iterator();
                 final List<String> ids =
                     new ArrayList<String>(tmpUserAccounts.size());
                 while (userAccountIter.hasNext()) {
                     final UserAccount userAccount = userAccountIter.next();
                     ids.add(userAccount.getId());
                 }
 
                 try {
                     final List<String> tmpPermitted =
                         pdp.evaluateRetrieve("user-account", ids);
                     final int numberPermitted = tmpPermitted.size();
                     if (numberPermitted == 0) {
                         break;
                     }
                     else {
                         int permittedIndex = 0;
                         String currentPermittedId =
                             tmpPermitted.get(permittedIndex);
                         userAccountIter = tmpUserAccounts.iterator();
                         while (userAccountIter.hasNext()) {
                             final UserAccount userAccount =
                                 userAccountIter.next();
                             if (currentPermittedId.equals(userAccount.getId())) {
                                 permittedUserAccounts.add(userAccount);
                                 ++permittedIndex;
                                 if (permittedIndex < numberPermitted) {
                                     currentPermittedId =
                                         tmpPermitted.get(permittedIndex);
                                 }
                                 else {
                                     break;
                                 }
                             }
                         }
                     }
                 }
                 catch (final MissingMethodParameterException e) {
                     throw new SystemException(
                         MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
                 }
                 catch (final ResourceNotFoundException e) {
                     throw new SystemException(
                         MSG_UNEXPECTED_EXCEPTION_ACCESS_RIGHTS, e);
                 }
                 currentOffset += currentLimit;
             }
 
             final List<UserAccount> offsetUserAccounts;
             final int numberPermitted = permittedUserAccounts.size();
             if (offset < numberPermitted) {
                 offsetUserAccounts = new ArrayList<UserAccount>(limit);
                 for (int i = offset; i < numberPermitted && i < currentLimit; i++) {
                     offsetUserAccounts.add(permittedUserAccounts.get(i));
                 }
             }
             else {
                 offsetUserAccounts = new ArrayList<UserAccount>(0);
             }
             result =
                 renderer.renderUserAccounts(offsetUserAccounts,
                     parameters.getRecordPacking());
         }
         return result;
     }
 
     /**
      * replaces group-id-filter with resolved userIds.
      * 
      * @param filter
      *            cql-filter
      * @throws InvalidSearchQueryException
      *             e
      * @throws SystemException
      *             e
      * @return Map with replaced cql-query (groupId replaced with userIds)
      */
     private Map<String, String[]> fixCqlGroupFilter(
         final Map<String, String[]> filter) throws InvalidSearchQueryException,
         SystemException {
 
         Map<String, String[]> returnFilter = filter;
         final Object[] queryPartsObject =
             filter.get(Constants.SRU_PARAMETER_QUERY);
         if (queryPartsObject != null) {
             final String[] queryParts = new String[queryPartsObject.length];
             for (int i = 0; i < queryPartsObject.length; i++) {
                 if (queryPartsObject[i] != null) {
                     queryParts[i] = queryPartsObject[i].toString();
                 }
             }
             boolean groupFilterFound = false;
             for (int i = 0; i < queryParts.length; i++) {
                 final Matcher matcher =
                     GROUP_FILTER_PATTERN.matcher(queryParts[i]);
                 if (matcher.find()) {
                     groupFilterFound = true;
                     final Matcher groupFilterMatcher =
                         GROUP_FILTER_PATTERN.matcher(queryParts[i]);
                     final StringBuffer result = new StringBuffer("");
                     while (groupFilterMatcher.find()) {
                         if (groupFilterMatcher.group(6).matches(".*?%.*")) {
                             throw new InvalidSearchQueryException(
                                 "Wildcards not allowed in group-filter");
                         }
                         if (groupFilterMatcher.group(3) != null && groupFilterMatcher
                             .group(3).matches(">|<|<=|>=|<>")
                             || groupFilterMatcher.group(4) != null
                             || groupFilterMatcher.group(5) != null) {
                             throw new InvalidSearchQueryException(
                                 "non-supported relation in group-filter");
                         }
                         // get users for group
                         final StringBuilder replacement =
                             new StringBuilder(" (");
                         try {
                             final Set<String> userIds =
                                 retrieveUsersForGroup(groupFilterMatcher
                                     .group(6));
                             // write user-cql-query
                             // and replace group-expression with it.
                             if (userIds != null && !userIds.isEmpty()) {
                                 for (final String userId : userIds) {
                                     if (replacement.length() > 2) {
                                         replacement.append(" or ");
                                     }
                                     replacement.append('\"');
                                     replacement
                                         .append(Constants.FILTER_PATH_ID);
                                     replacement
                                         .append("\"=").append(userId)
                                         .append(' ');
                                 }
                             }
                             else {
                                 throw new UserGroupNotFoundException("");
                             }
                         }
                         catch (final UserGroupNotFoundException e) {
                             if(LOGGER.isWarnEnabled()) {
                                 LOGGER.warn("Error on getting users for group.");
                             }
                             if(LOGGER.isDebugEnabled()) {
                                 LOGGER.debug("Error on getting users for group.", e);
                             }
                             // if group has no users or group not found,
                             // write nonexisting user in query
                             replacement.append('\"');
                             replacement.append(Constants.FILTER_PATH_ID);
                             replacement
                                 .append("\"=").append("nonexistinguser")
                                 .append(' ');
                         }
 
                         replacement.append(") ");
                         groupFilterMatcher.appendReplacement(result,
                             replacement.toString());
                     }
                     groupFilterMatcher.appendTail(result);
                     queryParts[i] = result.toString();
                 }
             }
             if (groupFilterFound) {
                 final Map<String, String[]> filter1 =
                     new HashMap<String, String[]>();
                 for (final Entry<String, String[]> entry : filter.entrySet()) {
                     if (entry.getValue() != null) {
                         // noinspection RedundantCast
                         filter1.put(entry.getKey(),
                             new String[((Object[]) entry.getValue()).length]);
                         // noinspection RedundantCast
                         for (int j = 0; j < ((Object[]) entry.getValue()).length; j++) {
                            filter1.get(entry.getKey())[j] = entry.getValue()[j];
                         }
                     }
                     else {
                         filter1.put(entry.getKey(), null);
                     }
                 }
                 filter1.put(Constants.SRU_PARAMETER_QUERY, queryParts);
                 returnFilter = filter1;
             }
         }
         return returnFilter;
     }
 
     /**
      * Retrieve all users that belong to the given group. Groups are gone
      * through hierarchically.
      * 
      * @param groupId
      *            id of group
      * 
      * @return set of userIds (hierarchy)
      * @throws UserGroupNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveUsersForGroup(java.lang.String)
      */
     private Set<String> retrieveUsersForGroup(final String groupId)
         throws UserGroupNotFoundException, SystemException {
         // may not return null but empty list!!
         final Set<String> userIds = new HashSet<String>();
 
         // Try getting the userGroup
         final UserGroup userGroup = userGroupDao.retrieveUserGroup(groupId);
         if (userGroup == null) {
             throw new UserGroupNotFoundException(StringUtility.format(
                 MSG_GROUP_NOT_FOUND_BY_ID, groupId));
         }
 
         final Set<UserGroupMember> members = userGroup.getMembers();
 
         // Get users that are integrated via their userId
         for (final UserGroupMember member : members) {
             if (member.getType().equals(
                 Constants.TYPE_USER_GROUP_MEMBER_INTERNAL)
                 && member.getName().equals(
                     Constants.NAME_USER_GROUP_MEMBER_USER_ACCOUNT)) {
                 userIds.add(member.getValue());
             }
         }
 
         // Get users that are integrated via their user-attributes
         final String ouAttributeName;
         try {
             ouAttributeName =
                 EscidocConfiguration.getInstance().get(
                     EscidocConfiguration.ESCIDOC_CORE_AA_OU_ATTRIBUTE_NAME);
         }
         catch (final IOException e) {
             throw new SystemException(e);
         }
 
         final Set<HashMap<String, String>> attributesSet =
             new HashSet<HashMap<String, String>>();
         for (final UserGroupMember member : members) {
             if (member.getType().equals(
                 Constants.TYPE_USER_GROUP_MEMBER_USER_ATTRIBUTE)) {
                 final HashMap<String, String> attributeHash =
                     new HashMap<String, String>();
                 attributeHash.put(member.getName(), member.getValue());
                 attributesSet.add(attributeHash);
                 // check if attribute-name is ou-attribute
                 // if yes, resolve children-path-list
                 if (ouAttributeName != null && ouAttributeName.length() != 0
                     && member.getName().equals(ouAttributeName)) {
                     final List<String> initialList = new ArrayList<String>();
                     initialList.add(member.getValue());
                     final List<String> pathList =
                         getOrgUnitChildrenPathList(member.getValue(),
                             initialList);
                     for (final String ouId : pathList) {
                         final HashMap<String, String> ouAttributeHash =
                             new HashMap<String, String>();
                         ouAttributeHash.put(ouAttributeName, ouId);
                         attributesSet.add(ouAttributeHash);
                     }
                 }
             }
         }
         if (!attributesSet.isEmpty()) {
             final List<UserAttribute> userAttributes =
                 dao.retrieveAttributes(attributesSet);
             for (final UserAttribute userAttribute : userAttributes) {
                 userIds.add(userAttribute.getUserAccountByUserId().getId());
             }
         }
 
         // Get users that are integrated via other groups
         for (final UserGroupMember member : members) {
             if (member.getType().equals(
                 Constants.TYPE_USER_GROUP_MEMBER_INTERNAL)
                 && member.getName().equals(
                     Constants.NAME_USER_GROUP_MEMBER_USER_GROUP)) {
                 userIds.addAll(retrieveUsersForGroup(member.getValue()));
             }
         }
 
         return userIds;
     }
 
     /**
      * Compute the child paths of the actual organizational unit.
      * 
      * @param orgUnitId
      *            the orgUnitId where pathList has to get retrieved.
      * @param totalList
      *            total list of all Children.
      * @return List of child-orgUnits
      * @throws SystemException
      *             If anything fails while computing the paths.
      */
     private List<String> getOrgUnitChildrenPathList(
         final String orgUnitId, final List<String> totalList)
         throws SystemException {
 
         List<String> addableList = totalList;
         final List<String> orgUnitIds =
             tripleStoreUtility.getChildren(orgUnitId);
         if (orgUnitIds != null && !orgUnitIds.isEmpty()) {
             addableList.addAll(orgUnitIds);
             for (final String childOrgUnitId : orgUnitIds) {
                 addableList =
                     getOrgUnitChildrenPathList(childOrgUnitId, addableList);
             }
         }
         return addableList;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param handle
      *            the handle
      * @return UserDetails object
      * @throws MissingMethodParameterException
      *             e
      * @throws AuthenticationException
      *             e
      * @throws AuthorizationException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveUserDetails(java.lang.String)
      */
     @Override
     public UserDetails retrieveUserDetails(final String handle)
         throws MissingMethodParameterException, AuthenticationException,
         AuthorizationException, UserAccountNotFoundException, SystemException {
         final UserDetails ret = dao.retrieveUserDetails(handle);
         // FIXME: use this as the authentication service?
         // In this case, additional values have to be set in the user details
         // and in case of user not found an authentication exception has to be
         // thrown.?
 
         if (ret == null) {
             throw new UserAccountNotFoundException(StringUtility.format(
                 "User not authenticated by provided handle", handle));
         }
         return ret;
     }
 
     /**
      * Gets the grant for the provided user id and grant id.
      * 
      * @param userId
      *            The id of the user account.
      * @param grantId
      *            The id of the grant.
      * @return Returns the fetched grant object.
      * @throws SqlDatabaseSystemException
      *             Thrown in case of an internal database error.
      * @throws UserAccountNotFoundException
      *             Thrown if the user account does not exists.
      * @throws GrantNotFoundException
      *             Thrown if the grant does not exists for the user.
      */
     private RoleGrant retrieveGrantByIds(
         final String userId, final String grantId)
         throws SqlDatabaseSystemException, UserAccountNotFoundException,
         GrantNotFoundException {
 
         final RoleGrant grant = dao.retrieveGrant(userId, grantId);
         if (grant == null) {
             if (dao.retrieveUserAccountById(userId) == null) {
                 throw new UserAccountNotFoundException();
             }
             throw new GrantNotFoundException(StringUtility.format(
                 "Grant not found", userId, grantId));
         }
         return grant;
     }
 
     /**
      * Gets the attribute for the provided user id and attribute id.
      * 
      * @param userId
      *            The id of the user account.
      * @param attributeId
      *            The id of the attribute.
      * @param forReadOnly
      *            if this attribute is used for read only.
      * @return Returns the fetched attribute object.
      * @throws SqlDatabaseSystemException
      *             Thrown in case of an internal database error.
      * @throws UserAccountNotFoundException
      *             Thrown if the user account does not exists.
      * @throws UserAttributeNotFoundException
      *             Thrown if the attribute does not exists for the user.
      * @throws ReadonlyElementViolationException
      *             Thrown if the attribute is external and may not get changed.
      */
     private UserAttribute retrieveAttributeById(
         final String userId, final String attributeId, final boolean forReadOnly)
         throws SqlDatabaseSystemException, UserAccountNotFoundException,
         UserAttributeNotFoundException, ReadonlyElementViolationException {
 
         final UserAttribute attribute =
             dao.retrieveAttribute(userId, attributeId);
         if (attribute == null) {
             if (dao.retrieveUserAccountById(userId) == null) {
                 throw new UserAccountNotFoundException();
             }
             throw new UserAttributeNotFoundException(StringUtility.format(
                 "Attribute not found", userId, attributeId));
         }
         if (!(forReadOnly || attribute.getInternal())) {
             throw new ReadonlyElementViolationException(
                 "Attribute is external and may not get changed");
 
         }
         return attribute;
     }
 
     /**
      * Sends userAccountUpdateEvent to AA.
      * 
      * @param userId
      *            The id of the updated user account.
      * 
      * @throws UserAccountNotFoundException
      *             Thrown if an user account with the provided id cannot be
      *             found.
      * @throws SqlDatabaseSystemException
      *             In case of a database error.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     private static void sendUserAccountUpdateEvent(final String userId)
         throws SqlDatabaseSystemException, UserAccountNotFoundException,
         WebserverSystemException {
 
         PoliciesCache.clearUserPolicies(userId);
         PoliciesCache.clearUserGroups(userId);
     }
 
     /**
      * Sends userAttributeUpdateEvent to AA.
      * 
      * @param userId
      *            The id of the updated user account.
      * 
      * @throws UserAccountNotFoundException
      *             Thrown if an user account with the provided id cannot be
      *             found.
      * @throws SqlDatabaseSystemException
      *             In case of a database error.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     private void sendUserAttributeUpdateEvent(final String userId)
         throws SqlDatabaseSystemException, UserAccountNotFoundException,
         WebserverSystemException {
 
         PoliciesCache.clearUserGroups(userId);
         retrieveUserAccountById(userId).touch();
     }
 
     /**
      * Retrieves the user account data identified by the provided user id and
      * asserts that an user has been found. Before the user account is
      * retrieved, it is asserted that an user id has been provided.
      * 
      * @param userId
      *            The id of the user to retrieve the data for.
      * @return Returns the identified <code>UserAccount</code> object.
      * @throws UserAccountNotFoundException
      *             Thrown if an user account with the provided id cannot be
      *             found.
      * @throws SqlDatabaseSystemException
      *             In case of a database error.
      */
     private UserAccount retrieveUserAccountById(final String userId)
         throws UserAccountNotFoundException, SqlDatabaseSystemException {
 
         final UserAccount user = dao.retrieveUserAccountById(userId);
         if (user == null) {
             throw new UserAccountNotFoundException(StringUtility.format(
                 MSG_USER_NOT_FOUND_BY_ID, userId));
         }
         return user;
     }
 
     /**
      * Asserts that the user account is provided, i.e. it is not
      * <code>null</code>.
      * 
      * @param userId
      *            The user id for which the account should be provided (should
      *            exist).
      * @param user
      *            The user account to assert.
      * @throws UserAccountNotFoundException
      *             Thrown if assertion fails.
      */
     private static void assertUserAccount(final String userId, final UserAccount user)
         throws UserAccountNotFoundException {
 
         if (user == null) {
             throw new UserAccountNotFoundException(StringUtility.format(
                 MSG_USER_NOT_FOUND_BY_IDENTITY_INFO, userId));
         }
     }
 
     /**
      * Fetches the current grants of the user account identified by the provided
      * id.
      * 
      * @param userId
      *            The id of the user account.
      * @return Returns a <code>List</code> containing the grants of the user
      *         account that are currently valid. If the user does not have a
      *         grant, an empty <code>List</code> is returned.
      * @throws SqlDatabaseSystemException
      *             Thrown in case of an internal database error.
      */
     private List<RoleGrant> fetchCurrentGrants(final String userId)
         throws SqlDatabaseSystemException {
 
         final List<RoleGrant> grants = dao.retrieveGrantsByUserId(userId);
         final List<RoleGrant> currentGrants =
             new ArrayList<RoleGrant>(grants.size());
         if (!grants.isEmpty()) {
             for (final RoleGrant grant : grants) {
                 if (grant.getRevocationDate() == null) {
                     currentGrants.add(grant);
                 }
             }
         }
         return currentGrants;
     }
 
     /**
      * Injects the user account data access object.
      * 
      * @param dao
      *            The data access object.
      */
     public void setDao(final UserAccountDaoInterface dao) {
         this.dao = dao;
     }
 
     /**
      * Injects the user group data access object.
      * 
      * @param userGroupDao
      *            The data access object.
      */
     public void setUserGroupDao(final UserGroupDaoInterface userGroupDao) {
         this.userGroupDao = userGroupDao;
     }
 
     /**
      * Injects the user group data access object.
      * 
      * @param objectAttributeResolver
      *            The objectAttributeResolver.
      */
     public void setObjectAttributeResolver(
         final ObjectAttributeResolver objectAttributeResolver) {
         this.objectAttributeResolver = objectAttributeResolver;
     }
 
     /**
      * Injects the role data access object.
      * 
      * @param roleDao
      *            The role data access object.
      */
     public void setRoleDao(final EscidocRoleDaoInterface roleDao) {
         this.roleDao = roleDao;
     }
 
     /**
      * Injects the TripleStore utility.
      *
      * @param tripleStoreUtility
      *            TripleStoreUtility from Spring
      */
     public void setTripleStoreUtility(
         final TripleStoreUtility tripleStoreUtility) {
         this.tripleStoreUtility = tripleStoreUtility;
     }
 
     /**
      * Injects the renderer.
      * 
      * @param renderer
      *            The renderer to inject.
      */
     public void setRenderer(final UserAccountRendererInterface renderer) {
         this.renderer = renderer;
     }
 
     /**
      * Injects the permissions query generator.
      * 
      * @param permissionsQuery
      *            The permissions query generator to inject.
      */
     public void setPermissionsQuery(final PermissionsQuery permissionsQuery) {
         this.permissionsQuery = permissionsQuery;
     }
 
     /**
      * Sets the creation date and the created-by user in the provided
      * <code>UserAccount</code> object.<br/>
      * The values are set with the values of modification date and modifying
      * user of the provided user account.<br/>
      * Before calling this method, the last modification date and the modifying
      * user must be set.
      * 
      * @param userAccount
      *            The <code>UserAccount</code> object to modify.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private static void setCreationValues(final UserAccount userAccount)
         throws SystemException {
 
         // initialize creation-date value
         userAccount.setCreationDate(userAccount.getLastModificationDate());
 
         // initialize created-by values
         userAccount.setUserAccountByCreatorId(userAccount
             .getUserAccountByModifiedById());
     }
 
     /**
      * Sets the last modification date and the modified-by user in the provided
      * <code>UserAccount</code> object.<br/>
      * The last modification date is set to the current time, and the modified
      * by user to the user account of the current, authenticated user.
      * 
      * @param userAccount
      *            The <code>UserAccount</code> object to modify.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void setModificationValues(final UserAccount userAccount)
         throws SystemException {
 
         userAccount
             .setLastModificationDate(new Date());
         userAccount.setUserAccountByModifiedById(getAuthenticatedUser(this.dao));
     }
 
     /**
      * Gets the user account of the authenticated user.<br>
      * The authenticated user is retrieved from the <code>UserContext</code>.
      * 
      * @param dao
      *            The data access object to use.
      * @return Returns the fetched user account.
      * @throws SqlDatabaseSystemException
      *             Thrown in case of a database error.
      * @throws WebserverSystemException
      *             Thrown if the account of the authenticated user cannot be
      *             found.
      */
     public static UserAccount getAuthenticatedUser(
         final UserAccountDaoInterface dao) throws SqlDatabaseSystemException,
         WebserverSystemException {
 
         final UserAccount userAccount =
             dao.retrieveUserAccountById(UserContext.getId());
         if (userAccount == null) {
             throw new WebserverSystemException(StringUtility.format(
                 "Account of authenticated user not found", UserContext.getId()));
         }
         return userAccount;
     }
 
     /**
      * Injects the policy decision point bean.
      * 
      * @param pdp
      *            The {@link PolicyDecisionPoint}.
      */
     public void setPdp(final PolicyDecisionPointInterface pdp) {
         this.pdp = pdp;
     }
 
     /**
      * Injects the userGroupHandler bean.
      * 
      * @param userGroupHandler
      *            The {@link UserGroupHandler}.
      */
     public void setUserGroupHandler(
         final UserGroupHandlerInterface userGroupHandler) {
         this.userGroupHandler = userGroupHandler;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @return list of preferences as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrievePreferences(java.lang.String)
      */
     @Override
     public String retrievePreferences(final String userId)
         throws UserAccountNotFoundException, SystemException {
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserPreference> currentPreferences =
             userAccount.getUserPreferencesByUserId();
         return renderer.renderPreferences(userAccount, currentPreferences);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param name
      *            name of the preference to retrieve
      * @return preference as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws PreferenceNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrievePreference(java.lang.String)
      */
     @Override
     public String retrievePreference(final String userId, final String name)
         throws UserAccountNotFoundException, PreferenceNotFoundException,
         SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserPreference> currentPreferences =
             userAccount.getUserPreferencesByUserId();
 
         String result = null;
         for (final UserPreference preference : currentPreferences) {
             final String preferenceName = preference.getName();
             if (preferenceName.equals(name)) {
                 result = renderer.renderPreference(userAccount, preference);
             }
         }
         if (result == null) {
             throw new PreferenceNotFoundException("Preference with name "
                 + name + " not found");
         }
 
         return result;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param preferenceXML
      *            preference as xml
      * @return created preference as xml
      * @throws AlreadyExistsException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws PreferenceNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #createPreference(java.lang.String, java.lang.String)
      */
     @Override
     public String createPreference(
         final String userId, final String preferenceXML)
         throws AlreadyExistsException, UserAccountNotFoundException,
         PreferenceNotFoundException, XmlCorruptedException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(preferenceXML);
         final StaxParser sp = new StaxParser(Elements.ELEMENT_USER_PREFERENCE);
 
         final UserPreferenceReadHandler uprh = new UserPreferenceReadHandler();
         sp.addHandler(uprh);
 
         try {
             sp.parse(in);
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".createPreference: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         final UserPreference preference = new UserPreference();
         final Map<String, String> preferences = uprh.getPreferences();
         final Set<String> preferenceNames = preferences.keySet();
         // there is only one entry
         // TODO ensure by xml schema that is true
         if (preferenceNames.size() > 1) {
             throw new XmlCorruptedException("Only one preference allowed. "
                 + MSG_XML_SCHEMA_ENSURE);
         }
         final Iterator<String> it = preferenceNames.iterator();
         final String preferenceName = it.next();
         final String preferenceValue = preferences.get(preferenceName);
         preference.setUserAccountByUserId(userAccount);
         preference.setName(preferenceName);
         preference.setValue(preferenceValue);
 
         final Set<UserPreference> userPreferences =
             userAccount.getUserPreferencesByUserId();
         // TODO check for same preference already set by getting preference by
         // PrimKey(userId,name)
         for (final UserPreference userPreference : userPreferences) {
             if (preferenceName.equals(userPreference.getName())) {
                 throw new AlreadyExistsException("Preference " + preferenceName
                     + " already exists for user " + userId);
             }
         }
         userPreferences.add(preference);
 
         // update user in policy cache; rights may depend on preferences
         sendUserAccountUpdateEvent(userId);
 
         userAccount.touch();
 
         return renderer.renderPreference(userAccount, preference);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param preferenceName
      *            name of preference to update
      * @param preferenceXML
      *            preference as xml
      * @return updated preference as xml
      * @throws AlreadyExistsException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws PreferenceNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             If the give last modification timestamp does not match the
      *             current one.
      * @throws MissingAttributeValueException
      *             If there is no last modificate date attribute.
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #updatePreference(java.lang.String, java.lang.String)
      */
     @Override
     public String updatePreference(
         final String userId, final String preferenceName,
         final String preferenceXML) throws AlreadyExistsException,
         UserAccountNotFoundException, PreferenceNotFoundException,
         XmlCorruptedException, SystemException, OptimisticLockingException,
         MissingAttributeValueException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserPreference> userPreferences =
             userAccount.getUserPreferencesByUserId();
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(preferenceXML);
         final StaxParser sp = new StaxParser(Elements.ELEMENT_USER_PREFERENCE);
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         final UserPreferenceReadHandler uprh = new UserPreferenceReadHandler();
         sp.addHandler(uprh);
 
         try {
             sp.parse(in);
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".createPreference: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         final Map<String, String> preferences = uprh.getPreferences();
         final Set<String> preferenceNames = preferences.keySet();
         // there is only one entry
         // TODO ensure by xml schema that is true
         if (preferenceNames.size() > 1) {
             throw new XmlCorruptedException("Only one preference allowed. "
                 + MSG_XML_SCHEMA_ENSURE);
         }
         final Iterator<String> it = preferenceNames.iterator();
         final String xmlPreferenceName = it.next();
         if (!xmlPreferenceName.equals(preferenceName)) {
             throw new XmlCorruptedException(
                 "Given preference name does not match "
                     + "preference name inside the xml representation.");
         }
 
         // TODO check for existence of preference by getting preference by
         // PrimKey(userId,name)
         final Iterator<UserPreference> prefIt = userPreferences.iterator();
         UserPreference preference = null;
         while (prefIt.hasNext()) {
             final UserPreference curPref = prefIt.next();
             if (preferenceName.equals(curPref.getName())) {
                 preference = curPref;
             }
         }
         if (preference == null) {
             // FIXME NotFoundException ?(FRS)
             throw new AlreadyExistsException("Preference " + preferenceName
                 + " does not exist for user " + userId);
         }
         preference.setValue(preferences.get(preferenceName));
         // unnecessary: userPreferences.add(preference);
 
         // update user in policy cache; rights may depend on preferences
         sendUserAccountUpdateEvent(userId);
 
         userAccount.touch();
 
         return renderer.renderPreference(userAccount, preference);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            userId
      * @param preferenceName
      *            name of preference to update
      * @throws UserAccountNotFoundException
      *             e
      * @throws PreferenceNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #deletePreference(java.lang.String, java.lang.String)
      */
     @Override
     public void deletePreference(
         final String userId, final String preferenceName)
         throws UserAccountNotFoundException, PreferenceNotFoundException,
         SystemException {
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserPreference> userPreferences =
             userAccount.getUserPreferencesByUserId();
 
         // PrimKey(userId,name)
         for (final UserPreference userPreference : userPreferences) {
             if (userPreference.getName().equals(preferenceName)) {
                 userPreferences.remove(userPreference);
                 // update user in policy cache; rights may depend on preferences
                 sendUserAccountUpdateEvent(userId);
                 userAccount.touch();
                 return;
             }
         }
         throw new PreferenceNotFoundException("The preference '"
             + preferenceName + "' does not exist for user '" + userId + "'.");
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            The userId.
      * @param preferencesXML
      *            The xml.
      * @return updated XML
      * @throws UserAccountNotFoundException
      *             If
      * @throws XmlCorruptedException
      *             If
      * @throws SystemException
      *             If
      * @throws OptimisticLockingException
      *             If the give last modification timestamp does not match the
      *             current one.
      * @throws MissingAttributeValueException
      *             If there is no last modificate date attribute.
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #createPreference(java.lang.String, java.lang.String)
      */
     @Override
     public String updatePreferences(
         final String userId, final String preferencesXML)
         throws UserAccountNotFoundException, XmlCorruptedException,
         SystemException, OptimisticLockingException,
         MissingAttributeValueException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(preferencesXML);
         final StaxParser sp = new StaxParser(Elements.ELEMENT_USER_PREFERENCES);
 
         final UserPreferenceReadHandler uprh = new UserPreferenceReadHandler();
         sp.addHandler(uprh);
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         try {
             sp.parse(in);
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".updatePreference: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         // delete all existing preferences
         // FIXME name/value may be defined as primary key
         final Set<UserPreference> currentPreferences =
             userAccount.getUserPreferencesByUserId();
         // Iterator<UserPreference> curPrefsIterator =
         // currentPreferences.iterator();
         // while (curPrefsIterator.hasNext()) {
         // UserPreference preference = curPrefsIterator.next();
         // dao.delete(preference);
         // }
         currentPreferences.clear();
 
         // add all given preferences
         final Map<String, String> preferences = uprh.getPreferences();
         for (final Entry<String, String> e : preferences.entrySet()) {
             final UserPreference preference = new UserPreference();
             final String preferenceName = e.getKey();
             final String preferenceValue = e.getValue();
             preference.setUserAccountByUserId(userAccount);
             preference.setName(preferenceName);
             preference.setValue(preferenceValue);
 
             // dao.save(preference);
             // FIXME ? set does not prevent dublicate keys but dublicate objects
             // (FRS)
             currentPreferences.add(preference);
         }
 
         // update user in policy cache; rights may depend on preferences
         sendUserAccountUpdateEvent(userId);
 
         userAccount.touch();
 
         // TODO create XML via renderPreference
         return renderer.renderPreferences(userAccount, currentPreferences);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            id of user
      * @param attributeXML
      *            xml with attribute to create
      * @return String xml with created attribute
      * @throws AlreadyExistsException
      *             e
      * @throws UserAccountNotFoundException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #createAttribute(java.lang.String, java.lang.String)
      */
     @Override
     public String createAttribute(final String userId, final String attributeXML)
         throws AlreadyExistsException, UserAccountNotFoundException,
         XmlCorruptedException, SystemException {
 
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(attributeXML);
         final StaxParser sp = new StaxParser(Elements.ELEMENT_USER_ATTRIBUTE);
 
         final UserAttributeReadHandler uarh = new UserAttributeReadHandler();
         sp.addHandler(uarh);
 
         try {
             sp.parse(in);
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".createAttribute: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         final UserAttribute attribute = new UserAttribute();
         final Map<String, String> attributes = uarh.getAttributes();
         final Set<String> attributeNames = attributes.keySet();
         // there is only one entry
         // TODO ensure by xml schema that is true
         if (attributeNames.size() > 1) {
             throw new XmlCorruptedException("Only one attribute allowed. "
                 + MSG_XML_SCHEMA_ENSURE);
         }
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
 
         final Iterator<String> it = attributeNames.iterator();
         final String attributeName = it.next();
         final String attributeValue = attributes.get(attributeName);
         attribute.setUserAccountByUserId(userAccount);
         attribute.setName(attributeName);
         attribute.setValue(attributeValue);
         attribute.setInternal(true);
 
         final Set<UserAttribute> userAttributes =
             userAccount.getUserAttributesByUserId();
 
         for (final UserAttribute userAttribute : userAttributes) {
             if (attributeName.equals(userAttribute.getName())
                 && attributeValue.equals(userAttribute.getValue())) {
                 throw new AlreadyExistsException("Attribute " + attributeName
                     + " with value " + attributeValue
                     + " already exists for user " + userId);
             }
         }
         dao.save(attribute);
         sendUserAttributeUpdateEvent(userId);
         return renderer.renderAttribute(attribute);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            id of user
      * @return String attributes as xml
      * @throws UserAccountNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveAttributes(java.lang.String)
      */
     @Override
     public String retrieveAttributes(final String userId)
         throws UserAccountNotFoundException, SystemException {
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserAttribute> currentAttributes =
             userAccount.getUserAttributesByUserId();
         return renderer.renderAttributes(userAccount, currentAttributes);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            id of user
      * @param name
      *            name of attribute
      * @return String xml with user-attributes
      * @throws UserAccountNotFoundException
      *             e
      * @throws UserAttributeNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveAttribute(java.lang.String, java.lang.String)
      */
     @Override
     public String retrieveNamedAttributes(final String userId, final String name)
         throws UserAccountNotFoundException, UserAttributeNotFoundException,
         SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final Set<UserAttribute> currentAttributes =
             userAccount.getUserAttributesByUserId();
 
         final Set<UserAttribute> selectedAttributes =
             new HashSet<UserAttribute>();
         if (currentAttributes != null) {
             for (final UserAttribute attribute : currentAttributes) {
                 final String attributeName = attribute.getName();
                 if (attributeName.equals(name)) {
                     selectedAttributes.add(attribute);
                 }
             }
         }
         return renderer.renderAttributes(userAccount, selectedAttributes);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            id of user
      * @param attributeId
      *            id of attribute
      * @return String xml with user-attribute
      * @throws UserAccountNotFoundException
      *             e
      * @throws UserAttributeNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #retrieveAttribute(java.lang.String, java.lang.String)
      */
     @Override
     public String retrieveAttribute(
         final String userId, final String attributeId)
         throws UserAccountNotFoundException, UserAttributeNotFoundException,
         SystemException {
         final UserAttribute attribute;
         try {
             attribute = retrieveAttributeById(userId, attributeId, true);
         }
         catch (final ReadonlyElementViolationException e) {
             throw new SystemException(e);
         }
         return renderer.renderAttribute(attribute);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            id of user
      * @param attributeId
      *            id of attribute
      * @param attributeXML
      *            xml with attribute
      * @return String xml with updated attribute
      * @throws UserAccountNotFoundException
      *             e
      * @throws UserAttributeNotFoundException
      *             e
      * @throws ReadonlyElementViolationException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             If the give last modification timestamp does not match the
      *             current one.
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #updateAttribute(java.lang.String, java.lang.String)
      */
     @Override
     public String updateAttribute(
         final String userId, final String attributeId, final String attributeXML)
         throws UserAccountNotFoundException, OptimisticLockingException,
         ReadonlyElementViolationException, UserAttributeNotFoundException,
         XmlCorruptedException, SystemException {
 
         final UserAccount userAccount = retrieveUserAccountById(userId);
         final UserAttribute userAttribute =
             retrieveAttributeById(userId, attributeId, false);
         final ByteArrayInputStream in =
             XmlUtility.convertToByteArrayInputStream(attributeXML);
         final StaxParser sp = new StaxParser(Elements.ELEMENT_USER_ATTRIBUTE);
         final UserAttributeReadHandler uarh = new UserAttributeReadHandler();
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(
                 userAccount.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
         sp.addHandler(uarh);
         try {
             sp.parse(in);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final Exception e) {
             final String msg =
                 MSG_UNEXPECTED_EXCEPTION + getClass().getName()
                     + ".updateAttribute: " + e.getClass().getName();
             throw new SystemException(msg, e);
         }
 
         final Map<String, String> attributes = uarh.getAttributes();
         final Set<String> attributeNames = attributes.keySet();
         // there is only one entry
         // TODO ensure by xml schema that is true
         if (attributeNames.size() > 1) {
             throw new XmlCorruptedException("Only one attribute allowed. "
                 + MSG_XML_SCHEMA_ENSURE);
         }
         final Iterator<String> it = attributeNames.iterator();
         final String xmlAttributeName = it.next();
         if (!xmlAttributeName.equals(userAttribute.getName())) {
             throw new XmlCorruptedException(
                 "Given attribute name does not match "
                     + "attribute name inside the xml representation.");
         }
         userAttribute.setValue(attributes.get(xmlAttributeName));
         sendUserAttributeUpdateEvent(userId);
         return renderer.renderAttribute(userAttribute);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param userId
      *            The userId.
      * @param attributeId
      *            The attributeId.
      * @throws UserAccountNotFoundException
      *             e
      * @throws UserAttributeNotFoundException
      *             e
      * @throws ReadonlyElementViolationException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.aa.service.interfaces.UserAccountHandlerInterface
      *      #deleteAttribute(java.lang.String, java.lang.String)
      */
     @Override
     public void deleteAttribute(final String userId, final String attributeId)
         throws UserAccountNotFoundException, UserAttributeNotFoundException,
         ReadonlyElementViolationException, SystemException {
 
         final UserAttribute userAttribute =
             retrieveAttributeById(userId, attributeId, false);
 
         dao.delete(userAttribute);
         sendUserAttributeUpdateEvent(userId);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param parameters
      *            parameter map
      * 
      * @return filter sub query with permission rules
      * 
      * @throws SystemException
      *             e
      * @throws InvalidSearchQueryException
      *             e
      * @throws AuthenticationException
      *             e
      * @throws AuthorizationException
      *             e
      */
     @Override
     public String retrievePermissionFilterQuery(
         final Map<String, String[]> parameters)
         throws InvalidSearchQueryException, SystemException {
         final Utility utility = Utility.getInstance();
         final Set<ResourceType> resourceTypes =
             EnumSet.noneOf(ResourceType.class);
         final String[] types = parameters.get("index");
 
         if (types != null) {
             final Collection<String> hashedTypes = new HashSet<String>();
 
             hashedTypes.addAll(Arrays.asList(types));
 
             final Map<String, Map<String, Map<String, Object>>> objectTypeParameters =
                 BeanLocator.locateIndexingHandler().getObjectTypeParameters();
 
             for (final Entry<String, Map<String, Map<String, Object>>> entry : objectTypeParameters
                 .entrySet()) {
                 final Map<String, Map<String, Object>> index = entry.getValue();
 
                 for (final String indexName : index.keySet()) {
                     if (hashedTypes.contains(indexName)) {
                         resourceTypes.add(ResourceType
                             .getResourceTypeFromUri(entry.getKey()));
                     }
                 }
             }
         }
         // noinspection RedundantCast
         return Utility.prepareReturnXml(
             null,
             "<filter>"
                 + permissionsQuery.getFilterQuery(resourceTypes,
                     utility.getCurrentUserId(), new FilterInterface() {
                         @Override
                         public String getRoleId() {
                             final String[] parameter = parameters.get("role");
 
                             return parameter != null && parameter.length > 0 ? parameter[0] : null;
                         }
 
                         @Override
                         public String getUserId() {
                             final String[] parameter = parameters.get("user");
 
                             return parameter != null && parameter.length > 0 ? parameter[0] : null;
                         }
                         
                         @Override
                         public String toString() {
                             return "[userId=" + getUserId() + ",roleId=" + getRoleId() + ']';
                         }
                     }) + "</filter>");
     }
 }
