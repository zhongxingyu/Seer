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
 package de.escidoc.core.test.aa;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidScopeException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.GrantNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RoleNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyRevokedException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.EscidocTestBase;
 import de.escidoc.core.test.TaskParamFactory;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 /**
  * Test suite for the Grants of an UserAccount or UserGroup.
  *
  * @author Torsten Tetteroo
  *         <p/>
  *         changes for schema version 0.3: <ul> <li>Removed UM_CG_6 (duplicate to UM_CG_7)</li> <li>Removed UM_CG_8 and
  *         UM_CG_10 (either href or object id in link element)</li> <li>Replaced UM_CG_9 and UM_CG_11 by
  *         UM_CG_9-rest/UM_CG_9-soap and UM_CG_11-rest/UM_CG_11-soap</li> <li>Replaced UM_CG_12 by
  *         UM_CG_12-rest/UM_CG_12-soap</li> </ul>
  */
 @RunWith(Parameterized.class)
 public class GrantIT extends GrantTestBase {
 
     /**
      * Initializes test-class with data.
      *
      * @return Collection with data.
      */
     @Parameterized.Parameters
     public static Collection<Object[]> data() {
         return Arrays.asList(new Object[][] { { USER_GROUP_HANDLER_CODE, null }, { USER_ACCOUNT_HANDLER_CODE, null } });
     }
 
     public GrantIT(final int handlerCode, final String userIdOrUserGroupId) throws Exception {
         super(handlerCode);
     }
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void initialize() throws Exception {
         revokeAllGrants(defaultUserAccountOrGroupId);
     }
 
     /**
      * Clean up after servlet test.
      *
      * @throws Exception If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
     }
 
     /**
      * Successfully create grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1() throws Exception {
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
     }
 
     /**
      * Successfully create grant without reference to object.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_2() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsFixedGrantDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // delete the object element
         deleteElement(toBeCreatedDocument, XPATH_GRANT_OBJECT);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant referencing a new context.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_3() throws Exception {
 
         // create context
         String path = TEMPLATE_CONTEXT_PATH;
         path += "/rest";
         Document toBeCreatedContextDocument = EscidocAbstractTest.getTemplateAsDocument(path, "context_create.xml");
         substitute(toBeCreatedContextDocument, "/context/properties/name", getUniqueName("PubMan Context "));
         String createdContextXml = null;
         try {
             final String toBeCreatedContextXml = toString(toBeCreatedContextDocument, false);
             createdContextXml = create(CONTEXT_HANDLER_CODE, toBeCreatedContextXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Creating context to be referenced in grant failed. ", e);
         }
         final String contextId = getObjidValue(createdContextXml);
         final String contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
 
         // create grant on context
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         // set reference to created context
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, contextHref);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant with scope on container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_4() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         //create container
         String containerXml = prepareContainer(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, false, false);
         Document containerDocument = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDocument);
 
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_COLLABORATOR);
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.CONTAINER_BASE_URI + "/" + containerId);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant with scope on item and component.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_5() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         String itemXml = prepareItem(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, false, false);
         Document document = EscidocAbstractTest.getDocument(itemXml);
 
         // save ids
         String itemId = getObjidValue(document);
         String publicComponentId = extractComponentId(document, VISIBILITY_PUBLIC);
 
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_COLLABORATOR);
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.ITEM_BASE_URI + "/" + itemId);
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
 
         toBeCreatedDocument = getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_COLLABORATOR);
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.ITEM_BASE_URI + "/" + itemId + "/"
             + Constants.SUB_COMPONENT + "/" + publicComponentId);
         toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant with scope on scope.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_6() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_STATISTICS_EDITOR);
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.STATISTIC_SCOPE_BASE_URI + "/"
             + STATISTIC_SCOPE_ID);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant with scope on user-group.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_7() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_USER_GROUP_INSPECTOR);
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.USER_GROUP_BASE_URI + "/"
             + USER_GROUP_WITH_USER_LIST_ID);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Successfully create grant with scope on content-relation.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg1_8() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         String contentRelationXml =
             prepareContentRelation(PWCallback.DEFAULT_HANDLE, STATUS_PENDING, null, null, false, false);
         // substitute the role
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, ROLE_HREF_CONTENT_RELATION_MODIFIER);
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.CONTENT_RELATION_BASE_URI + "/"
             + getObjidValue(contentRelationXml));
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
     }
 
     /**
      * Test declining creation of Grant with providing unknown UserAccount id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2() throws Exception {
 
         String grantXml = getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         try {
             createGrant(UNKNOWN_ID, grantXml);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing id of a resource of another resource type.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_2() throws Exception {
 
         String grantXml = getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         try {
             createGrant(CONTEXT_ID, grantXml);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing scope object-type that is not allowed by role
      * (organizational-unit).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_3() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.ORGANIZATIONAL_UNIT_BASE_URI + "/"
             + ORGANIZATIONAL_UNIT_ID);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException(InvalidScopeException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(InvalidScopeException.class, e);
             String testString = "has objectType organizational-unit";
             assertMatches(((InvalidScopeException) e).getHttpStatusMsg() + " does not match " + testString, testString,
                 ((InvalidScopeException) e).getHttpStatusMsg());
         }
     }
 
     /**
      * Test declining creation of Grant with providing scope object-type that is not allowed by role (user-account).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_4() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.USER_ACCOUNT_BASE_URI + "/"
             + TEST_USER_ACCOUNT_ID);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException(InvalidScopeException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(InvalidScopeException.class, e);
             String testString = "has objectType user-account";
             assertMatches(((InvalidScopeException) e).getHttpStatusMsg() + " does not match " + testString, testString,
                 ((InvalidScopeException) e).getHttpStatusMsg());
         }
     }
 
     /**
      * Test declining creation of Grant with providing scope object-type that is not allowed by role (role).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_5() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, ROLE_HREF_DEPOSITOR);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException(InvalidScopeException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(InvalidScopeException.class, e);
             String testString = "has objectType role";
             assertMatches(((InvalidScopeException) e).getHttpStatusMsg() + " does not match " + testString, testString,
                 ((InvalidScopeException) e).getHttpStatusMsg());
         }
     }
 
     /**
      * Test declining creation of Grant with providing scope object-type that is not allowed by role (grant).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_6() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.USER_ACCOUNT_BASE_URI + "/"
             + TEST_USER_ACCOUNT_ID + "/" + Constants.SUB_GRANT + "/" + grantId);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException(InvalidScopeException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(InvalidScopeException.class, e);
             String testString = "has objectType grant";
             assertMatches(((InvalidScopeException) e).getHttpStatusMsg() + " does not match " + testString, testString,
                 ((InvalidScopeException) e).getHttpStatusMsg());
         }
     }
 
     /**
      * Test declining creation of Grant with providing scope object-type that is not allowed by role (content-type).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg2_7() throws Exception {
 
         Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         // substitute the object element
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.CONTENT_MODEL_BASE_URI + "/"
             + CONTENT_TYPE_ID);
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException(InvalidScopeException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(InvalidScopeException.class, e);
             String testString = "has objectType content-model";
             assertMatches(((InvalidScopeException) e).getHttpStatusMsg() + " does not match " + testString, testString,
                 ((InvalidScopeException) e).getHttpStatusMsg());
         }
     }
 
     /**
      * Test declining creation of Grant without providing UserAccount id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg3() throws Exception {
 
         String grantXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         try {
             createGrant(null, grantXml);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test declining creation of Grant with corrupted XML data.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg4() throws Exception {
 
         try {
             createGrant(defaultUserAccountOrGroupId, "<Corrupt XML data");
             EscidocAbstractTest.failMissingException(XmlCorruptedException.class);
         }
         catch (final Exception e) {
             // FIXME: temporarily, the authorization throws wrong exception.
             // therefore both are checked here. This is the correct assertion.
             // assertExceptionType(XmlCorruptedException.class, e);
 
             if (e.getClass().getName().equals(InvalidXmlException.class.getName())) {
                 // success
             }
             else {
                 EscidocAbstractTest.assertExceptionType(XmlCorruptedException.class, e);
             }
         }
     }
 
     /**
      * Test declining creation of Grant without providing XML data.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg5() throws Exception {
 
         try {
             createGrant(defaultUserAccountOrGroupId, null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing XML data without a mandatory element.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg7() throws Exception {
 
         Document grantDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         deleteElement(grantDocument, XPATH_GRANT_ROLE);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toString(grantDocument, false));
             EscidocAbstractTest.failMissingException(XmlSchemaValidationException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(XmlSchemaValidationException.class, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing reference to unknown role.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg13() throws Exception {
 
         final Class ec = RoleNotFoundException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, Constants.ROLE_BASE_URI + "/" + UNKNOWN_ID);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating grant with unknown role not declined. ", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating grant with unknown role not declined, properly. ", ec, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing role reference to an object that is not a role.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg13_2() throws Exception {
 
         final Class ec = RoleNotFoundException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, Constants.ROLE_BASE_URI + "/" + CONTEXT_ID);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest
                 .failMissingException("Creating grant with role-reference to context not declined. ", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(
                 "Creating grant with role-reference to context not declined, properly. ", ec, e);
         }
     }
 
     /**
      * Test declining creating duplicate grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg14() throws Exception {
 
         createGrantSuccessfully("escidoc_grant_for_create.xml");
 
         // recreate the same grant
         final String toBeCreatedXml =
             getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating duplicate grant not declined.",
                 AlreadyExistsException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating duplicate grant not declined, properly.",
                 AlreadyExistsException.class, e);
         }
     }
 
     /**
      * Test successfully recreating a revoked grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg15() throws Exception {
 
         // create a grant and revoke it
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         String lastModificationDate = getLastModificationDateValue(createdDocument);
         String taskParamXML =
             TaskParamFactory.getRevokeGrantTaskParam(new DateTime(lastModificationDate), "Some revocation\n remark");
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grant failed (test initialization).", e);
         }
 
         // recreate the same grant
         final String toBeCreatedXml =
             getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Recreating revoked grant failed.", e);
         }
         assertGrant(createdXml, toString(createdDocument, false), defaultUserAccountOrGroupId, startTimestamp,
             startTimestamp, false);
     }
 
     /**
      * Test declining creation of Grant with providing reference to unknown object.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg16() throws Exception {
 
         final Class ec = XmlCorruptedException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.CONTEXT_BASE_URI + "/" + UNKNOWN_ID);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating grant with reference to unknown object not declined. ",
                 ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating grant with reference to unknown object not declined,"
                 + " properly. ", ec, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing reference to unsupported object.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg17() throws Exception {
 
         final Class ec = XmlCorruptedException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF,
             Constants.STATISTIC_AGGREGATION_DEFINITIONS_BASE_URI + "/" + EscidocTestBase.TEST_AGGREGATION_DEFINITION_ID);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating grant with reference to unsupported object"
                 + " not declined. ", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating grant with reference to unsupported object"
                 + " not declined, properly. ", ec, e);
         }
     }
 
     /**
      * Test successfully retrieving a Grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg1() throws Exception {
 
         final Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         final String grantId = getObjidValue(createdDocument);
         Document retrievedDocument = retrieveGrantSuccessfully(grantId);
         assertXmlEquals("Wrong data received. ", createdDocument, retrievedDocument);
     }
 
     /**
      * Test declining retrieving a Grant with providing an unknown UserAccount id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg2() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         try {
             retrieveGrant(UNKNOWN_ID, grantId);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining retrieving a Grant with providing an id of an existing resource of another type.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg2_2() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         try {
             retrieveGrant(CONTEXT_ID, grantId);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining retrieving a Grant without providing the user id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg3() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         try {
             retrieveGrant(null, grantId);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a Grant with providing unknown grant id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg4() throws Exception {
 
         try {
             retrieveGrant(defaultUserAccountOrGroupId, UNKNOWN_ID);
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a Grant with providing id of another UserAccount.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg4_2() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         try {
             if (handlerCode == USER_GROUP_HANDLER_CODE) {
                 retrieveGrant(USER_GROUP_WITH_USER_LIST_ID, grantId);
             }
             else {
                 retrieveGrant(TEST_USER_ACCOUNT_ID1, grantId);
             }
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a Grant with providing id of an existing resource of another type.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg4_3() throws Exception {
 
         createGrantSuccessfully("escidoc_grant_for_create.xml");
 
         try {
             if (handlerCode == USER_GROUP_HANDLER_CODE) {
                 retrieveGrant(USER_GROUP_WITH_USER_LIST_ID, CONTEXT_ID);
             }
             else {
                 retrieveGrant(TEST_USER_ACCOUNT_ID1, CONTEXT_ID);
             }
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a Grant without providing the grant id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARg5() throws Exception {
 
         try {
             retrieveGrant(defaultUserAccountOrGroupId, null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Successfully revoking an existing Grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg1() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         String lastModificationDate = getLastModificationDateValue(createdDocument);
         String taskParamXML =
             TaskParamFactory.getRevokeGrantTaskParam(new DateTime(lastModificationDate), "Some revocation\n remark");
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
 
         Document updatedDocument = retrieveGrantSuccessfully(grantId);
         assertXmlLastModificationDateUpdate("Modification date not updated", createdDocument, updatedDocument);
         assertXmlExists("Missing revoked-by", updatedDocument, XPATH_GRANT_REVOKED_BY);
         assertXmlExists("Missing revocation-date", updatedDocument, XPATH_GRANT_REVOCATION_DATE);
         assertXmlExists("Missing revocation remark", updatedDocument, XPATH_GRANT_REVOCATION_REMARK);
         assertXmlEquals("Unexpected revocation remark, ", updatedDocument, XPATH_GRANT_REVOCATION_REMARK,
             "Some revocation\n remark");
     }
 
     /**
      * Test declining revoking a Grant with providing unknown UserAccount id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg2() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         final DateTime lastModificationDate = getLastModificationDateValue2(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             revokeGrant(UNKNOWN_ID, grantId, taskParamXML, null);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining revoking a Grant with providing id of an existing resource of another type..
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg2_2() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         final DateTime lastModificationDate = getLastModificationDateValue2(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             revokeGrant(CONTEXT_ID, grantId, taskParamXML, null);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining revoking a Grant that does not exist.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg3() throws Exception {
 
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(new DateTime("2007-01-01T01:01:01.000Z"), null);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, UNKNOWN_ID, taskParamXML, null);
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining revoking a Grant with providing id of another resource as the grant id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg3_2() throws Exception {
 
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(new DateTime("2007-01-01T01:01:01.000Z"), null);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, CONTEXT_ID, taskParamXML, null);
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining revoking a Grant without providing the grant id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg4() throws Exception {
 
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(new DateTime("2007-01-01T01:01:01.000Z"), null);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, null, taskParamXML, null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test declining revoking a Grant without providing the user id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg5() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         final DateTime lastModificationDate = getLastModificationDateValue2(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             revokeGrant(null, grantId, taskParamXML, null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Declining revoking a revoked Grant.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg6() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         DateTime lastModificationDate = getLastModificationDateValue2(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
 
         String retrievedGrantXml = null;
         try {
             retrievedGrantXml = retrieveGrant(defaultUserAccountOrGroupId, grantId);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertNotNull("No data from retrieve. ", retrievedGrantXml);
         Document retrievedDocument = EscidocAbstractTest.getDocument(retrievedGrantXml);
         lastModificationDate = getLastModificationDateValue2(retrievedDocument);
         taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
             EscidocAbstractTest.failMissingException(AlreadyRevokedException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(AlreadyRevokedException.class, e);
         }
     }
 
     /**
      * Test declining revoking a Grant without providing the task parameters.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg7() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
 
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, null, null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test successfully revoking a Grant without providing the last-modification-date.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg7_1() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam("some remark");
         try {
             revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
     }
 
     /**
      * Test successfully revoking a Grant with providing wrong last-modification-date.
      *
      * @throws Exception If anything fails.
      */
     public void testAARvg7_2() throws Exception {
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam("2008-01-01", null);
         revokeGrant(defaultUserAccountOrGroupId, grantId, taskParamXML, null);
     }
 
     /**
      * Test declining revoking a Grant with providing id of another UserAccount.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg8() throws Exception {
 
         Document createdDocument = createGrantSuccessfully("escidoc_grant_for_create.xml");
         String grantId = getObjidValue(createdDocument);
         final DateTime lastModificationDate = getLastModificationDateValue2(createdDocument);
         String taskParamXML = TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate, null);
 
         try {
             if (handlerCode == USER_GROUP_HANDLER_CODE) {
                 revokeGrant(USER_GROUP_WITH_USER_LIST_ID, grantId, taskParamXML, null);
             }
             else {
                 revokeGrant(TEST_USER_ACCOUNT_ID1, grantId, taskParamXML, null);
             }
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
     }
 
     /**
      * Test revoking Grants with providing ids of existing grants.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg9() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke 2 grants
         Set<String> grantsToRemove = new HashSet<String>();
         int grantCount = 0;
         for (String grantId : expectedGrants.keySet()) {
             grantsToRemove.add(grantId);
             grantCount++;
             if (grantCount == 2) {
                 break;
             }
         }
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(grantsToRemove, "Some revocation\n remark");
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParam);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grants failed. ", e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, 1);
     }
 
     /**
      * Test revoking Grants with providing empty filter.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg10() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke all grants
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(null, "Some revocation\n remark");
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParam);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grants failed. ", e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, 0);
     }
 
     /**
      * Test revoking Grants with providing no filter.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg11() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke all grants
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(null, "Some revocation\n remark");
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParam);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grants failed. ", e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, 0);
     }
 
     /**
      * Test revoking Grants with providing empty param-element.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg12() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke all grants
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(null, null);
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParam);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grants failed. ", e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, 0);
     }
 
     /**
      * Test revoking Grants with providing empty filter list.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg13() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke no grants
         String taskParamXML =
             TaskParamFactory.getRevokeGrantsTaskParam(new HashSet<String>(), "Some revocation\n remark");
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParamXML);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Revoking grants failed. ", e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
     }
 
     /**
      * Test revoking Grants with providing ids of non-existing grants.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg14() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke 2 existing grants and 2 grants that do not exist
         //check Transactional Functionality
         Set<String> toRevoke = new HashSet<String>();
 
         int count = 0;
         for (String grantId : expectedGrants.keySet()) {
             if (count < 2) {
                 toRevoke.add(grantId);
             }
             count++;
         }
         toRevoke.add("nonexistingid1");
         toRevoke.add("nonexistingid2");
 
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(toRevoke, "Some revocation\n remark");
 
         try {
             revokeGrants(defaultUserAccountOrGroupId, taskParam);
             EscidocAbstractTest.failMissingException(GrantNotFoundException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(GrantNotFoundException.class, e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
     }
 
     /**
      * Test revoking Grants with providing id of non-existing userAccount.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARvg15() throws Exception {
         //create grants
         int numberOfGrants = 3;
         Map<String, Node> expectedGrants = createGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
 
         //retrieve grants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, expectedGrants.size());
 
         //revoke 2 existing grants and 2 grants that do not exist
         //check Transactional Functionality
         Set<String> toRevoke = new HashSet<String>();
         int count = 0;
         for (String grantId : expectedGrants.keySet()) {
             if (count < 2) {
                 toRevoke.add(grantId);
             }
             count++;
         }
         String taskParam = TaskParamFactory.getRevokeGrantsTaskParam(toRevoke, "Some revocation\n remark");
 
         try {
             revokeGrants("nonexistinguser", taskParam);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
 
         //retrieveGrants
         retrieveGrantsSuccessfully(defaultUserAccountOrGroupId, numberOfGrants);
     }
 
     /**
      * Test successfully retrieving current Grants. <br> In this scenario, no grant has been revoked.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu1() throws Exception {
 
         final int expectedLength = 3;
         final Map<String, Node> expectedGrants = new HashMap<String, Node>(expectedLength);
         for (int i = 0; i < expectedLength; i++) {
             final String templateName;
             if (i == 0) {
                 templateName = "escidoc_grant_for_create.xml";
             }
             else {
                 templateName = "escidoc_grant" + (i + 1) + "_for_create.xml";
             }
 
             final Document createdDocument = createGrantSuccessfully(defaultUserAccountOrGroupId, templateName);
             final String objid = getObjidValue(createdDocument);
             expectedGrants.put(objid, createdDocument);
         }
 
         String currentGrantsXml = null;
         try {
             currentGrantsXml = retrieveCurrentGrants(defaultUserAccountOrGroupId);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidGrants(currentGrantsXml);
         final Document retrievedDocument = EscidocAbstractTest.getDocument(currentGrantsXml);
         getLastModificationDateValue(retrievedDocument);
         NodeList grants = selectNodeList(retrievedDocument, XPATH_CURRENT_GRANTS_GRANT);
         final int length = grants.getLength();
         assertEquals("Unexpected number of current grants. ", expectedLength, length);
         for (int i = 0; i < expectedLength; i++) {
             final Node toBeAsserted = grants.item(i);
             final String toBeAssertedId = getObjidValue(toBeAsserted, "");
             final String msg = "Asserting " + (i + 1) + ". grant. [" + toBeAssertedId + "]";
             final Node expected = expectedGrants.get(toBeAssertedId);
             assertNotNull(msg + "Unexpected grant [" + toBeAssertedId + "]", expected);
             final String xpathToBeAssertedGrant = XPATH_CURRENT_GRANTS_GRANT + "[" + (i + 1) + "]";
             assertXmlEquals(msg + "Title mismatch. ", expected, XPATH_GRANT_XLINK_TITLE, toBeAsserted,
                 xpathToBeAssertedGrant + PART_XLINK_TITLE);
             assertXmlEquals(msg + "Href mismatch. ", expected, XPATH_GRANT_XLINK_HREF, toBeAsserted,
                 xpathToBeAssertedGrant + PART_XLINK_HREF);
             assertXmlEquals(msg + "Properties mismatch. ", expected, XPATH_GRANT_PROPERTIES, toBeAsserted,
                 xpathToBeAssertedGrant + "/" + NAME_PROPERTIES);
         }
     }
 
     /**
      * Test successfully retrieving current Grants. <br> In this scenario, a grant has been revoked.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu2() throws Exception {
         final int expectedLength = 2;
         final Map<String, Node> expectedGrants = new HashMap<String, Node>(expectedLength);
         for (int i = 0; i < (expectedLength + 1); i++) {
             final String templateName;
             if (i == 0) {
                 templateName = "escidoc_grant_for_create.xml";
             }
             else {
                 templateName = "escidoc_grant" + (i + 1) + "_for_create.xml";
             }
             final Document createdDocument = createGrantSuccessfully(defaultUserAccountOrGroupId, templateName);
 
             final String objid = getObjidValue(createdDocument);
 
             if (i == 1) {
                 // revoke one of the grants
                 final DateTime lastModificationDate1 = getLastModificationDateValue2(createdDocument);
                 String taskParam1Xml =
                     TaskParamFactory.getRevokeGrantTaskParam(lastModificationDate1, "Some revocation\n remark");
 
                 try {
                     revokeGrant(defaultUserAccountOrGroupId, objid, taskParam1Xml, null);
                 }
                 catch (final Exception e) {
                     EscidocAbstractTest.failException("Revoking grant failed. ", e);
                 }
             }
             else {
                 expectedGrants.put(objid, createdDocument);
             }
         }
 
         String currentGrantsXml = null;
         try {
             currentGrantsXml = retrieveCurrentGrants(defaultUserAccountOrGroupId);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidGrants(currentGrantsXml);
         final Document retrievedDocument = EscidocAbstractTest.getDocument(currentGrantsXml);
         getLastModificationDateValue(retrievedDocument);
         NodeList grants = selectNodeList(retrievedDocument, XPATH_CURRENT_GRANTS_GRANT);
         final int length = grants.getLength();
         assertEquals("Unexpected number of current grants. ", expectedLength, length);
         for (int i = 0; i < expectedLength; i++) {
             final Node toBeAsserted = grants.item(i);
             final String toBeAssertedId = getObjidValue(toBeAsserted, "");
             final String msg = "Asserting " + (i + 1) + ". grant. [" + toBeAssertedId + "]";
             final Node expected = expectedGrants.get(toBeAssertedId);
             assertNotNull(msg + "Unexpected grant [" + toBeAssertedId + "]", expected);
             final String xpathToBeAssertedGrant = XPATH_CURRENT_GRANTS_GRANT + "[" + (i + 1) + "]";
             assertXmlEquals(msg + "Title mismatch. ", expected, XPATH_GRANT_XLINK_TITLE, toBeAsserted,
                 xpathToBeAssertedGrant + PART_XLINK_TITLE);
             assertXmlEquals(msg + "Href mismatch. ", expected, XPATH_GRANT_XLINK_HREF, toBeAsserted,
                 xpathToBeAssertedGrant + PART_XLINK_HREF);
             assertXmlEquals(msg + "Properties mismatch. ", expected, XPATH_GRANT_PROPERTIES, toBeAsserted,
                 xpathToBeAssertedGrant + "/" + NAME_PROPERTIES);
         }
     }
 
     /**
      * Test successfully retrieving current Grants. <br> check for assigned-on element of system-administrator. Issue:
      * https://www.escidoc.org/jira/browse/INFR-903
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu2_1() throws Exception {
         final int expectedLength = 4;
         final Map<String, Node> expectedGrants = new HashMap<String, Node>(expectedLength);
         for (int i = 0; i < expectedLength; i++) {
             final String templateName;
             if (i == 0) {
                 templateName = "escidoc_grant_for_create.xml";
             }
             else {
                 templateName = "escidoc_grant" + (i + 1) + "_for_create.xml";
             }
             final Document createdDocument = createGrantSuccessfully(defaultUserAccountOrGroupId, templateName);
 
             final String objid = getObjidValue(createdDocument);
 
             expectedGrants.put(objid, createdDocument);
         }
 
         String currentGrantsXml = null;
         try {
             currentGrantsXml = retrieveCurrentGrants(defaultUserAccountOrGroupId);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidGrants(currentGrantsXml);
         final Document retrievedDocument = EscidocAbstractTest.getDocument(currentGrantsXml);
         getLastModificationDateValue(retrievedDocument);
         NodeList grants = selectNodeList(retrievedDocument, XPATH_CURRENT_GRANTS_GRANT);
         final int length = grants.getLength();
         assertEquals("Unexpected number of current grants. ", expectedLength, length);
         for (int i = 0; i < expectedLength; i++) {
             final Node toBeAsserted = grants.item(i);
             final String toBeAssertedId = getObjidValue(toBeAsserted, "");
             final String msg = "Asserting " + (i + 1) + ". grant. [" + toBeAssertedId + "]";
             final Node expected = expectedGrants.get(toBeAssertedId);
             assertNotNull(msg + "Unexpected grant [" + toBeAssertedId + "]", expected);
             final String xpathToBeAssertedGrant = XPATH_CURRENT_GRANTS_GRANT + "[" + (i + 1) + "]";
             Node roleNode = selectSingleNode(toBeAsserted, xpathToBeAssertedGrant + "/properties/role/@href");
             if (roleNode.getNodeValue().contains(ROLE_HREF_SYSTEM_ADMINISTRATOR)) {
                 assertNull("assigned-on must be null for system-administrator", selectSingleNode(toBeAsserted,
                     xpathToBeAssertedGrant + "/properties/assigned-on"));
             }
             else {
                 assertNotNull("assigned-on must be set for role", selectSingleNode(toBeAsserted, xpathToBeAssertedGrant
                     + "/properties/assigned-on"));
             }
         }
     }
 
     /**
      * Test declining retrieving current grants with providing an unknown UserAccount id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu3() throws Exception {
 
         try {
             retrieveCurrentGrants(UNKNOWN_ID);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining retrieving current grants with providing an id of an existing resource of another type.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu3_2() throws Exception {
 
         try {
             retrieveCurrentGrants(CONTEXT_ID);
             EscidocAbstractTest.failMissingException(notFoundException.getClass());
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(notFoundException.getClass(), e);
         }
     }
 
     /**
      * Test declining retrieving current Grants without providing the user id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAARgu4() throws Exception {
 
         try {
             retrieveCurrentGrants(null);
             EscidocAbstractTest.failMissingException(MissingMethodParameterException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(MissingMethodParameterException.class, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing XML data without specifying role href.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg9_rest() throws Exception {
 
         Document grantDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         deleteAttribute(grantDocument, XPATH_GRANT_ROLE, NAME_HREF);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toString(grantDocument, false));
             EscidocAbstractTest.failMissingException(XmlSchemaValidationException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(XmlSchemaValidationException.class, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing XML data without specifying object href.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg11_rest() throws Exception {
 
         Document grantDocument =
             getTemplateAsFixedGrantDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         deleteAttribute(grantDocument, XPATH_GRANT_OBJECT, NAME_HREF);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toString(grantDocument, false));
             EscidocAbstractTest.failMissingException(XmlSchemaValidationException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(XmlSchemaValidationException.class, e);
         }
     }
 
     /**
      * Successfully create grant with set read only values (REST).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg12_rest() throws Exception {
 
         final Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH,
                 "escidoc_grant_for_create_rest_read_only.xml");
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         final Document createdDocument =
             assertGrant(createdXml, null, defaultUserAccountOrGroupId, startTimestamp, startTimestamp, false);
 
         // grant-remark
         assertXmlEquals("Grant remark mismatch, ", toBeCreatedDocument, createdDocument, XPATH_GRANT_GRANT_REMARK);
 
         // revocation-remark
         assertXmlNotExists("Unexpected revocation remark, ", createdDocument, XPATH_GRANT_REVOCATION_REMARK);
 
         // role reference
         assertXmlEquals("Role reference title unexpected, ", createdDocument, XPATH_GRANT_ROLE_XLINK_TITLE,
             "Administrator");
 
         // object reference
         assertXmlEquals("Object reference mismatch, href mismatch, ", toBeCreatedDocument, createdDocument,
             XPATH_GRANT_OBJECT_XLINK_HREF);
         assertXmlEquals("Object reference mismatch, title mismatch, ", createdDocument, XPATH_GRANT_OBJECT_XLINK_TITLE,
             "Test Collection");
     }
 
     /**
      * Test declining creation of Grant with providing reference to role with invalid href (base of href is not the base
      * of role hrefs).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg13_3_rest() throws Exception {
 
         final Class<?> ec = RoleNotFoundException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
 
         String roleHref = selectSingleNodeAsserted(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF).getTextContent();
         roleHref = roleHref.replaceFirst(Constants.ROLE_BASE_URI, Constants.ORGANIZATIONAL_UNIT_BASE_URI);
         substitute(toBeCreatedDocument, XPATH_GRANT_ROLE_XLINK_HREF, roleHref);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating grant with invalid object href not declined. ", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating grant with invalid object href not declined,"
                 + " properly. ", ec, e);
         }
     }
 
     /**
      * Test declining creation of Grant with providing reference to object with invalid href (object id and object type
      * mismatch in href).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAACg16_2_rest() throws Exception {
 
         final Class<?> ec = XmlCorruptedException.class;
 
         Document toBeCreatedDocument =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH, "escidoc_grant_for_create.xml");
         substitute(toBeCreatedDocument, XPATH_GRANT_OBJECT_XLINK_HREF, Constants.ORGANIZATIONAL_UNIT_BASE_URI + "/"
             + CONTEXT_ID);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             createGrant(defaultUserAccountOrGroupId, toBeCreatedXml);
             EscidocAbstractTest.failMissingException("Creating grant with invalid object href not declined. ", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("Creating grant with invalid object href not declined,"
                 + " properly. ", ec, e);
         }
     }
 
 }
