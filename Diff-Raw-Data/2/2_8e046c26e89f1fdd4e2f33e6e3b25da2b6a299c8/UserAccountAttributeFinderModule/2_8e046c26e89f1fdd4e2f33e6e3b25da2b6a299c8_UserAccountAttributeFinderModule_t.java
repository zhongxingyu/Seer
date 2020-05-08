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
 package de.escidoc.core.aa.business.xacml.finder;
 
 import com.sun.xacml.EvaluationCtx;
 import com.sun.xacml.attr.AttributeDesignator;
 import com.sun.xacml.attr.AttributeValue;
 import com.sun.xacml.attr.BagAttribute;
 import com.sun.xacml.attr.StringAttribute;
 import com.sun.xacml.cond.EvaluationResult;
 import de.escidoc.core.aa.business.SecurityHelper;
 import de.escidoc.core.aa.business.authorisation.Constants;
 import de.escidoc.core.aa.business.authorisation.CustomEvaluationResultBuilder;
 import de.escidoc.core.aa.business.authorisation.FinderModuleHelper;
 import de.escidoc.core.aa.business.persistence.RoleGrant;
 import de.escidoc.core.aa.business.persistence.UserAccount;
 import de.escidoc.core.aa.business.persistence.UserAccountDaoInterface;
 import de.escidoc.core.aa.business.persistence.UserAttribute;
 import de.escidoc.core.aa.business.persistence.UserLoginData;
 import de.escidoc.core.common.business.aa.authorisation.AttributeIds;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.application.notfound.UserAccountNotFoundException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.list.ListSorting;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Implementation of an XACML attribute finder module that is responsible for the attributes related to an user
  * account.<br> This finder module supports both XACML subject attributes and XACML resource attributes.<br> The
  * attribute values are fetched from the xml representation of the user account.
  * <p/>
  * Supported Attributes:<br> -info:escidoc/names:aa:1.0:resource:user-account:handle<br> the handle of the user-account,
  * single value attribute -info:escidoc/names:aa:1.0:resource:user-account:login-name<br> the login-name of the
  * user-account, single value attribute -info:escidoc/names:aa:1.0:resource:user-account:name<br> the name of the
  * user-account, single value attribute -info:escidoc/names:aa:1.0:resource:user-account:created-by<br> the user who
  * created the user-account, single value attribute -info:escidoc/names:aa:1.0:resource:user-account:modified-by<br> the
  * user who modified the user-account, single value attribute -info:escidoc/names:aa:1.0:resource:user-account:organizational-unit<br>
  * the organizational-unit of the user-account, multi value attribute -info:escidoc
  * /names:aa:1.0:resource:user-account:organizational-unit-with-children<br> the organizational-unit tree of the
  * user-account, multi value attribute -info:escidoc/names:aa:1.0:resource:user-account:group-membership<br> the ids of
  * the groups the user is member(hierarchical), multi value attribute -info:escidoc/names:aa:1.0:resource:user-account:id<br>
  * the id of the user-account, single value attribute -info:escidoc/names:aa:1.0:subject:handle<br> the handle of the
  * current user, single value attribute -info:escidoc/names:aa:1.0:subject:login-name<br> the login-name of the current
  * user, single value attribute -info:escidoc/names:aa:1.0:subject:name<br> the name of the current user, single value
  * attribute -info:escidoc/names:aa:1.0:subject:created-by<br> the user who created the current user, single value
  * attribute -info:escidoc/names:aa:1.0:subject:modified-by<br> the user who modified the current user, single value
  * attribute -info:escidoc/names:aa:1.0:subject:organizational-unit<br> the organizational-unit of the current user,
  * multi value attribute -info:escidoc/names:aa:1.0:subject:organizational-unit-with-children<br> the
  * organizational-unit tree of the current user, multi value attribute -info:escidoc/names:aa:1.0:subject:group-membership<br>
  * the ids of the groups the current user is member(hierarchical), multi value attribute
  * -info:escidoc/names:aa:1.0:subject:role-grant:&lt;role-id&gt;:assigned-on<br> the id of the object the grant is
  * assigned on (scope of the grant), multi value attribute
  *
  * @author Torsten Tetteroo
  */
 @Service("eSciDoc.core.aa.UserAccountAttributeFinderModule")
 public class UserAccountAttributeFinderModule extends AbstractAttributeFinderModule {
 
     /**
      * Pattern used to parse the attribute id and fetch the resolvable part, the last part of the resolvable part and
      * the tail.
      */
     private static final String USER_ACCOUNT_ATTRS =
         "(handle|login-name|name|" + "created-by|modified-by|"
            + "organizational-unit-with-children|organizational-unit|" + "group-membership|role-grant)";
 
     private static final String ROLE_GRANT_ATTRS = "(assigned-on)";
 
     private static final Pattern PATTERN_PARSE_ROLE_GRANT_ROLE =
         Pattern.compile("((" + AttributeIds.SUBJECT_ATTR_PREFIX + '|' + AttributeIds.USER_ACCOUNT_ATTR_PREFIX + ')'
             + USER_ACCOUNT_ATTRS + "):(.*?):" + ROLE_GRANT_ATTRS);
 
     private static final Pattern PATTERN_PARSE_USER_ACCOUNT_ATTRIBUTE_ID =
         Pattern.compile("((" + AttributeIds.SUBJECT_ATTR_PREFIX + '|' + AttributeIds.USER_ACCOUNT_ATTR_PREFIX + ')'
             + USER_ACCOUNT_ATTRS + ")(:.*){0,1}");
 
     private static final Pattern PATTERN_SUBJECT_ATTRIBUTE_PREFIX = Pattern.compile(AttributeIds.SUBJECT_ATTR_PREFIX);
 
     private static final Pattern PATTERN_USER_ACCOUNT_ATTRIBUTE_PREFIX =
         Pattern.compile(AttributeIds.USER_ACCOUNT_ATTR_PREFIX);
 
     private static final Pattern PATTERN_IS_SUBJECT_ATTRIBUTE_ID =
         Pattern.compile(AttributeIds.SUBJECT_ATTR_PREFIX + ".*");
 
     /**
      * Attributes can have USER_ACCOUNT_ATTR_PREFIX
      * (info:escidoc/names:aa:1.0:resources:user-account:) or
      * SUBJECT_ATTR_PREFIX (info:escidoc/names:aa:1.0:subject:) if
      * USER_ACCOUNT_ATTR_PREFIX is used, userId is resolved from
      * resourceId-Attribute. if SUBJECT_ATTR_PREFIX is used, userId is resolved
      * from xacml-subject-attribute
      *
      * Internally this class only uses USER_ACCOUNT_ATTR_PREFIX -->
      * SUBJECT_ATTR_PREFIX is changed to USER_ACCOUNT_ATTR_PREFIX before
      * resolving.
      */
 
     /**
      * This attribute matches the current eSciDoc user handles of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_HANDLE = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "handle";
 
     /**
      * This attribute matches the internal id of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_ID = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "id";
 
     /**
      * This attribute matches the login name of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_LOGIN_NAME = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "login-name";
 
     /**
      * This attribute matches the name of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_NAME = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "name";
 
     /**
      * This attribute matches the creator of the user (identified by the resource-id).
      */
     private static final String ATTR_CREATED_BY = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "created-by";
 
     /**
      * This attribute matches the last modifier of the user (identified by the resource-id).
      */
     private static final String ATTR_MODIFIED_BY = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "modified-by";
 
     /**
      * This attribute matches the group-membership of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_GROUP_MEMBERSHIP = AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "group-membership";
 
     /**
      * This attribute matches the organizational unit of the user (identified by the resource-id).
      */
     private static final String ATTR_USER_ORGANIZATIONAL_UNIT =
         AttributeIds.USER_ACCOUNT_ATTR_PREFIX + XmlUtility.NAME_ORGANIZATIONAL_UNIT;
 
     /**
      * This attribute matches the organizational unit of the user (identified by the resource-id) (also children are
      * resolved).
      */
     private static final String ATTR_USER_ORGANIZATIONAL_UNIT_WITH_CHILDREN =
         AttributeIds.USER_ACCOUNT_ATTR_PREFIX + XmlUtility.NAME_ORGANIZATIONAL_UNIT + "-with-children";
 
     /**
      * This attribute matches the scope of a role of the user.
      */
     private static final Pattern ATTR_USER_ROLE_SCOPE =
         Pattern.compile(AttributeIds.USER_ACCOUNT_ATTR_PREFIX + "role-grant:(.*?):assigned-on");
 
     @Autowired
     @Qualifier("security.SecurityHelper")
     private SecurityHelper securityHelper;
 
     @Autowired
     @Qualifier("persistence.UserAccountDao")
     private UserAccountDaoInterface userAccountDao;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected UserAccountAttributeFinderModule() {
     }
 
     /**
      * Since this class will retrieve subject attributes and resource attributes, it will return a set only containing
      * two values, indicating that it supports Designators of type SUBJECT_TARGET and RESOURCE_TARGET.
      *
      * @return Set only containing the value SUBJECT_TARGET and RESOURCE_TARGET.
      * @see AttributeFinderModule #getSupportedDesignatorTypes()
      */
     @Override
     public Set getSupportedDesignatorTypes() {
         final Set<Integer> set = new HashSet<Integer>();
         set.add(AttributeDesignator.SUBJECT_TARGET);
         set.add(AttributeDesignator.RESOURCE_TARGET);
         return set;
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     protected boolean assertAttribute(
         final String attributeIdValue, final EvaluationCtx ctx, final String resourceId, final String resourceObjid,
         final String resourceVersionNumber, final int designatorType) throws EscidocException {
 
         // the super class method only supports RESOURCE attributes. We have to
         // override it, to support SUBJECT Attributes, too.
 
         // make sure this is an Subject or Resource attribute
         if (designatorType != AttributeDesignator.SUBJECT_TARGET
             && designatorType != AttributeDesignator.RESOURCE_TARGET) {
 
             return false;
         }
 
         // make sure attribute is in escidoc-internal format for
         // subject attribute or resource user-account attributes
         return PATTERN_PARSE_USER_ACCOUNT_ATTRIBUTE_ID.matcher(attributeIdValue).find();
 
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     protected Object[] resolveLocalPart(
         final String attributeIdValue, final EvaluationCtx ctx, final String resourceId, final String resourceObjid,
         final String resourceVersionNumber) throws EscidocException {
 
         // determine the id of the user account and to simplify the further
         // work, replace INTERNAL_SUBJECT_ATTRIBUTE_PREFIX by
         // INTERNAL_RESOURCE_USER_ACCOUNT_ATTRIBUTE_PREFIX in case of an
         // subject attribute
         final String internalAttributeIdValue;
         final String userAccountId;
         boolean isSubjectAttribute = false;
         if (PATTERN_IS_SUBJECT_ATTRIBUTE_ID.matcher(attributeIdValue).find()) {
             isSubjectAttribute = true;
             userAccountId = FinderModuleHelper.retrieveSingleSubjectAttribute(ctx, Constants.URI_SUBJECT_ID, true);
             if (userAccountId == null) {
                 final StringBuilder errorMsg = new StringBuilder("The subject (user) of the request cannot be ");
                 errorMsg.append("identified, the ");
                 errorMsg.append(Constants.URI_SUBJECT_ID);
                 errorMsg.append(" may not have been set.");
                 throw new WebserverSystemException(errorMsg.toString());
             }
             final Matcher matcher = PATTERN_SUBJECT_ATTRIBUTE_PREFIX.matcher(attributeIdValue);
             internalAttributeIdValue = matcher.replaceFirst(AttributeIds.USER_ACCOUNT_ATTR_PREFIX);
         }
         else {
             userAccountId = FinderModuleHelper.getResourceId(ctx);
             if (FinderModuleHelper.isNewResourceId(userAccountId)) {
                 return null;
             }
             internalAttributeIdValue = attributeIdValue;
         }
         // ask cache for previously cached results
         EvaluationResult result =
             (EvaluationResult) getFromCache(resourceId, resourceObjid, resourceVersionNumber, internalAttributeIdValue,
                 ctx);
 
         String resolvedAttributeIdValue = null;
         if (result == null) {
             // check if attributes of an anonymous user shall be retrieved
             if (UserContext.isIdOfAnonymousUser(userAccountId)) {
                 // The anonymous user does not have an account, for each
                 // attribute the value of the anonymous identifier is returned.
                 result = CustomEvaluationResultBuilder.createSingleStringValueResult(UserContext.ANONYMOUS_IDENTIFIER);
                 // the resolved id is set to the complete id, as no further
                 // resolving is possible.
                 resolvedAttributeIdValue = internalAttributeIdValue;
             }
             else {
                 if (ATTR_USER_HANDLE.equals(internalAttributeIdValue)) {
                     final Set userHandles = retrieveUserHandle(ctx, userAccountId, internalAttributeIdValue);
                     result = new EvaluationResult(new BagAttribute(Constants.URI_XMLSCHEMA_STRING, userHandles));
                     resolvedAttributeIdValue = ATTR_USER_HANDLE;
                 }
                 else if (ATTR_USER_ROLE_SCOPE.matcher(internalAttributeIdValue).matches()) {
                     result = fetchRoleScopes(userAccountId, internalAttributeIdValue);
                     resolvedAttributeIdValue = internalAttributeIdValue;
                 }
                 else if (ATTR_USER_GROUP_MEMBERSHIP.equals(internalAttributeIdValue)) {
                     result = fetchUserGroups(userAccountId);
                     resolvedAttributeIdValue = internalAttributeIdValue;
                 }
                 else {
                     // Fetch the user account and return the appropriate value
                     final UserAccount userAccount;
                     try {
                         userAccount = retrieveUserAccount(ctx, userAccountId);
                     }
                     catch (final UserAccountNotFoundException e) {
                         if (isSubjectAttribute) {
                             throw new UserAccountNotFoundException(StringUtility.format(
                                 "Account of subject not found.", userAccountId, e.getMessage()), e);
                         }
                         else {
                             throw e;
                         }
                     }
                     final Pattern p = PATTERN_PARSE_USER_ACCOUNT_ATTRIBUTE_ID;
                     final Matcher idMatcher = p.matcher(internalAttributeIdValue);
                     if (idMatcher.find()) {
                         resolvedAttributeIdValue = idMatcher.group(1);
                         if (userAccount != null) {
                             if (ATTR_USER_ID.equals(resolvedAttributeIdValue)) {
                                 final String nextResourceId = userAccount.getId();
                                 result = CustomEvaluationResultBuilder.createSingleStringValueResult(nextResourceId);
                             }
                             else if (ATTR_USER_LOGIN_NAME.equals(resolvedAttributeIdValue)) {
                                 result =
                                     CustomEvaluationResultBuilder.createSingleStringValueResult(userAccount
                                         .getLoginname());
                             }
                             else if (ATTR_USER_NAME.equals(resolvedAttributeIdValue)) {
                                 result =
                                     CustomEvaluationResultBuilder.createSingleStringValueResult(userAccount.getName());
                             }
                             else if (ATTR_CREATED_BY.equals(resolvedAttributeIdValue)) {
                                 result =
                                     CustomEvaluationResultBuilder.createSingleStringValueResult(userAccount
                                         .getUserAccountByCreatorId().getId());
                             }
                             else if (ATTR_MODIFIED_BY.equals(resolvedAttributeIdValue)) {
                                 result =
                                     CustomEvaluationResultBuilder.createSingleStringValueResult(userAccount
                                         .getUserAccountByModifiedById().getId());
                             }
                             else if (ATTR_USER_ORGANIZATIONAL_UNIT.equals(resolvedAttributeIdValue)) {
                                 result = fetchUserAccountOus(userAccount, false);
                             }
                             else if (ATTR_USER_ORGANIZATIONAL_UNIT_WITH_CHILDREN.equals(resolvedAttributeIdValue)) {
                                 result = fetchUserAccountOus(userAccount, true);
                             }
                         }
                     }
                 }
             }
         }
         if (result == null) {
             return null;
         }
         if (isSubjectAttribute) {
             // revert previously Subject -> Resource change
             final Matcher matcher = PATTERN_USER_ACCOUNT_ATTRIBUTE_PREFIX.matcher(resolvedAttributeIdValue);
             resolvedAttributeIdValue = matcher.replaceFirst(AttributeIds.SUBJECT_ATTR_PREFIX);
         }
         putInCache(resourceId, resourceObjid, resourceVersionNumber, attributeIdValue, ctx, result);
         return new Object[] { result, resolvedAttributeIdValue };
     }
 
     /**
      * Fetches the value of the attribute {@code ATTR_USER_OU} for the provided user account.
      *
      * @param userAccount The user account to fetch the value from.
      * @param getChildren if also children of userAccountous are to be fetched.
      * @return Returns the attribute value in an {@code EvaluationResult}.
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      */
     private EvaluationResult fetchUserAccountOus(final UserAccount userAccount, final boolean getChildren)
         throws SystemException {
 
         final String ouAttributeName =
             EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_AA_OU_ATTRIBUTE_NAME);
         if (ouAttributeName == null || ouAttributeName.length() == 0) {
             return CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         final List<UserAttribute> attributes = userAccountDao.retrieveAttributes(userAccount, ouAttributeName);
         final EvaluationResult result;
         if (attributes == null || attributes.isEmpty()) {
             result = CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         else {
             final List<StringAttribute> results = new ArrayList<StringAttribute>();
             final Collection<String> ouIds = new ArrayList<String>();
             for (final UserAttribute attribute : attributes) {
                 results.add(new StringAttribute(attribute.getValue()));
                 if (getChildren) {
                     ouIds.add(attribute.getValue());
                 }
             }
             if (getChildren) {
                 final List<String> childOus = tripleStoreUtility.getChildrenPath(ouIds, new ArrayList<String>());
                 if (childOus != null) {
                     for (final String childOu : childOus) {
                         results.add(new StringAttribute(childOu));
                     }
                 }
             }
 
             result = new EvaluationResult(new BagAttribute(Constants.URI_XMLSCHEMA_STRING, results));
         }
         return result;
     }
 
     /**
      * Fetches the groupIds where user is member for the provided user account.
      *
      * @param userAccountId The id of the user account to fetch the value from.
      * @return Returns the attribute value in an {@code EvaluationResult}.
      * @throws EscidocException e
      */
     private EvaluationResult fetchUserGroups(final String userAccountId) throws EscidocException {
 
         final EvaluationResult result;
 
         final Set<String> userGroups = securityHelper.getUserGroups(userAccountId);
 
         if (userGroups == null || userGroups.isEmpty()) {
             result = CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         else {
             final Iterator<String> groupIdsIter = userGroups.iterator();
             final List<StringAttribute> results = new ArrayList<StringAttribute>(userGroups.size());
             while (groupIdsIter.hasNext()) {
                 results.add(new StringAttribute(groupIdsIter.next()));
             }
             result = new EvaluationResult(new BagAttribute(Constants.URI_XMLSCHEMA_STRING, results));
         }
         return result;
     }
 
     /**
      * Fetches the scopes of the role identified in the attribute for the provided user account.
      *
      * @param userAccountId The id of the user account to fetch the value from.
      * @param attributeId   The name of the attribute.
      * @return Returns the attribute value in an {@code EvaluationResult}.
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      */
     private EvaluationResult fetchRoleScopes(final String userAccountId, final CharSequence attributeId)
         throws SystemException {
 
         // get role to fetch
         final Matcher roleMatcher = PATTERN_PARSE_ROLE_GRANT_ROLE.matcher(attributeId);
         String roleName = null;
         if (roleMatcher.find()) {
             roleName = roleMatcher.group(4);
         }
         if (roleName == null || roleName.length() == 0) {
             return CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
 
         Set<String> userGroups = null;
         try {
             userGroups = securityHelper.getUserGroups(userAccountId);
         }
         catch (UserAccountNotFoundException e) {
             // The caller doesn't expect to get an exception from here if
             // the user doesn't exist.
         }
         final Map<String, HashSet<String>> criterias = new HashMap<String, HashSet<String>>();
         final HashSet<String> roles = new HashSet<String>();
         roles.add(roleName);
         final HashSet<String> users = new HashSet<String>();
         users.add(userAccountId);
         criterias.put(de.escidoc.core.common.business.Constants.FILTER_PATH_USER_ID, users);
         criterias.put(de.escidoc.core.common.business.Constants.FILTER_PATH_ROLE_ID, roles);
         if (userGroups != null && !userGroups.isEmpty()) {
             criterias.put(de.escidoc.core.common.business.Constants.FILTER_PATH_GROUP_ID, (HashSet<String>) userGroups);
         }
         final List<RoleGrant> roleGrants = userAccountDao.retrieveGrants(criterias, null, ListSorting.ASCENDING);
         final EvaluationResult result;
         if (roleGrants != null) {
             final List<StringAttribute> results = new ArrayList<StringAttribute>();
             for (final RoleGrant roleGrant : roleGrants) {
                 if (roleGrant.getRevocationDate() == null) {
                     results.add(new StringAttribute(roleGrant.getObjectId()));
                 }
             }
             result = new EvaluationResult(new BagAttribute(Constants.URI_XMLSCHEMA_STRING, results));
         }
         else {
             result = CustomEvaluationResultBuilder.createEmptyEvaluationResult();
         }
         return result;
     }
 
     /**
      * Retrieves the handles of the user.
      *
      * @param ctx              The evaluation context, which will be used as key for the cache.
      * @param userAccountId    The id of the user account to get the handles for.
      * @param attributeIdValue The value of the attribute id that is currently resolved.
      * @return Returns the eSciDoc user handle of the subject (current user).
      * @throws WebserverSystemException     Thrown in case of an internal error.
      * @throws UserAccountNotFoundException Thrown if no user account with provided id is found.
      */
     private Set retrieveUserHandle(final EvaluationCtx ctx, final String userAccountId, final String attributeIdValue)
         throws WebserverSystemException, UserAccountNotFoundException {
 
         Set<AttributeValue> result =
             (Set<AttributeValue>) getFromCache(XmlUtility.NAME_HANDLE, null, null, userAccountId, ctx);
         if (result == null) {
             final List userHandles;
             try {
                 userHandles = getUserAccountDao().retrieveUserLoginDataByUserId(userAccountId);
                 if (userHandles == null || userHandles.isEmpty()) {
                     assertUserAccount(userAccountId, getUserAccountDao().retrieveUserAccountById(userAccountId));
                 }
             }
             catch (final UserAccountNotFoundException e) {
                 throw e;
             }
             catch (final Exception e) {
                 final String errorMsg = StringUtility.format("Retrieving of attribute failed", attributeIdValue);
                 throw new WebserverSystemException(errorMsg, e);
             }
             result = new HashSet<AttributeValue>();
             if (userHandles != null && !userHandles.isEmpty()) {
                 for (final Object userHandle : userHandles) {
                     final UserLoginData userLoginData = (UserLoginData) userHandle;
                     result.add(new StringAttribute(userLoginData.getHandle()));
                 }
             }
             putInCache(XmlUtility.NAME_HANDLE, null, null, userAccountId, ctx, result);
         }
         return result;
     }
 
     /**
      * Retrieve User Account from the system.
      *
      * @param ctx           The evaluation context, which will be used as key for the cache.
      * @param userAccountId The user account id.
      * @return Returns the {@code UserAccount} identified by the provided id.
      * @throws WebserverSystemException     Thrown in case of an internal error.
      * @throws UserAccountNotFoundException Thrown if no user account with provided id exists.
      */
     private UserAccount retrieveUserAccount(final EvaluationCtx ctx, final String userAccountId)
         throws WebserverSystemException, UserAccountNotFoundException {
 
         UserAccount userAccount = (UserAccount) getFromCache(XmlUtility.NAME_ID, null, null, userAccountId, ctx);
         if (userAccount == null) {
             try {
                 userAccount = getUserAccountDao().retrieveUserAccount(userAccountId);
             }
             catch (final Exception e) {
                 throw new WebserverSystemException(StringUtility.format(
                     "Exception during retrieval of the user account", e.getMessage()), e);
             }
         }
 
         assertUserAccount(userAccountId, userAccount);
 
         putInCache(XmlUtility.NAME_ID, null, null, userAccountId, ctx, userAccount);
         return userAccount;
     }
 
     /**
      * Asserts that the user account is provided, i.e. it is not {@code null}.
      *
      * @param userId      The user id for which the account should be provided (should exist).
      * @param userAccount The user account to assert.
      * @throws UserAccountNotFoundException Thrown if assertion fails.
      */
     private static void assertUserAccount(final String userId, final UserAccount userAccount)
         throws UserAccountNotFoundException {
 
         if (userAccount == null) {
             throw new UserAccountNotFoundException(StringUtility.format("User with provided id does not exist", userId));
         }
     }
 
     /**
      * Gets the user account dao.
      *
      * @return Returns the user account dao bean.
      * @throws WebserverSystemException Thrown in case of an internal system error during bean initialization.
      */
     private UserAccountDaoInterface getUserAccountDao() {
 
         return this.userAccountDao;
     }
 }
