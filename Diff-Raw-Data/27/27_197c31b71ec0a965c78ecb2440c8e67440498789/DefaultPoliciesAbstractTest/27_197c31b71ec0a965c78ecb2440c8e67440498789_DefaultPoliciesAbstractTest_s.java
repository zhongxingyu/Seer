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
 
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.StagingFileNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.security.AuthorizationException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.aa.GrantClient;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import java.util.ArrayList;
 
 /**
  * Test suite for the default policies.
  * Test-Set for Grant-test:
  * create new user (userId)
  * create 2 new groups (groupId and groupId1, groupId1 belongs to groupId))
  * add testuser1 to group1
  * let testuser and testuser1 create one container and one item in status pending
  * both users have no grants assigned
  * 
  * 
  * @author TTE
  * 
  */
 public class DefaultPoliciesAbstractTest extends GrantTestBase {
 
     private static UserGroupTestBase userGroupTestBase = null;
     
     protected static final String HANDLE = PWCallback.TEST_HANDLE;
 
     private static int methodCounter = 0;
     
     private static String itemId = null;
 
     private static String itemHref = null;
 
     private static String itemId1 = null;
 
     private static String itemHref1 = null;
 
     private static String containerId = null;
 
     private static String containerHref = null;
 
     private static String containerId1 = null;
 
     private static String containerHref1 = null;
 
     private static String publicComponentId = null;
 
     private static String publicComponentHref = null;
 
     private static String publicComponentId1 = null;
 
     private static String publicComponentHref1 = null;
 
     private static String userId = null;
 
     private static String groupId = null;
 
     private static String groupId1 = null;
 
     /**
      * The constructor.
      * 
      * @param transport
      *            The transport identifier.
      * 
      * @throws Exception
      *             If anything fails.
      */
     public DefaultPoliciesAbstractTest(final int transport) throws Exception {
         super(transport, USER_ACCOUNT_HANDLER_CODE);
         userGroupTestBase = new UserGroupTestBase(transport);
     }
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void initialize() throws Exception {
         if (methodCounter == 0) {
             prepare();
         }
         PWCallback.setHandle(HANDLE);
     }
 
     /**
      * Clean up after servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
         methodCounter++;
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
     }
 
     /**
      * insert data into system for the tests.
      * 
      * @test.name prepare
      * @test.id PREPARE
      * @test.input
      * @test.inputDescription
      * @test.expected
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     protected void prepare() throws Exception {
         //create sysadmin grant for testusers
         revokeAllGrants(TEST_USER_ACCOUNT_ID);
         doTestCreateGrant(null, TEST_USER_ACCOUNT_ID, 
             null, ROLE_HREF_SYSTEM_ADMINISTRATOR, null);
         revokeAllGrants(TEST_USER_ACCOUNT_ID1);
         doTestCreateGrant(null, TEST_USER_ACCOUNT_ID1, 
             null, ROLE_HREF_SYSTEM_ADMINISTRATOR, null);
         try {
             //create user to attach test-grants
             String userXml = prepareUserAccount(
                 PWCallback.DEFAULT_HANDLE, STATUS_ACTIVE);
             Document userDocument =
                 EscidocRestSoapTestBase.getDocument(userXml);
             userId = getObjidValue(userDocument);
 
             //create groups to attach test-grants
             String groupXml = prepareUserGroup(
                 PWCallback.DEFAULT_HANDLE);
             Document groupDocument =
                 EscidocRestSoapTestBase.getDocument(groupXml);
             groupId = getObjidValue(groupDocument);
             String lastModificationDate = 
                 getLastModificationDateValue(groupDocument);
 
             groupXml = prepareUserGroup(
                 PWCallback.DEFAULT_HANDLE);
             groupDocument =
                 EscidocRestSoapTestBase.getDocument(groupXml);
             groupId1 = getObjidValue(groupDocument);
             String lastModificationDate1 = 
                 getLastModificationDateValue(groupDocument);
 
             //add group1 to group
             ArrayList<String []> selectors = new ArrayList<String[]>();
             String [] selector = new String [3];
             selector[0] = "user-group";
             selector[1] = "internal";
             selector[2] = groupId1;
 
             selectors.add(selector);
             String taskParam = 
                 userGroupTestBase
                     .getAddSelectorsTaskParam(
                             selectors, lastModificationDate);
             userGroupTestBase.doTestAddSelectors(
                         null, groupId, taskParam, null);
                 
             //add testuser1 to group1
             selectors = new ArrayList<String[]>();
             selector = new String [3];
             selector[0] = "user-account";
             selector[1] = "internal";
             selector[2] = TEST_USER_ACCOUNT_ID1;
 
             selectors.add(selector);
             taskParam = 
                 userGroupTestBase
                     .getAddSelectorsTaskParam(
                             selectors, lastModificationDate1);
             userGroupTestBase.doTestAddSelectors(
                     null, groupId1, taskParam, null);
             
             //testuser create container in status pending
             String containerXml = 
                 prepareContainer(
                     PWCallback.TEST_HANDLE, 
                     STATUS_PENDING, null,
                     false, false);
             Document containerDocument =
                 EscidocRestSoapTestBase.getDocument(containerXml);
             containerId = getObjidValue(containerDocument);
             containerHref = Constants.CONTAINER_BASE_URI + "/" + containerId;
 
             //testuser1 create container in status pending
             containerXml = 
                 prepareContainer(
                     PWCallback.TEST_HANDLE1, 
                     STATUS_PENDING, null,
                     false, false);
             containerDocument =
                 EscidocRestSoapTestBase.getDocument(containerXml);
             containerId1 = getObjidValue(containerDocument);
             containerHref1 = Constants.CONTAINER_BASE_URI + "/" + containerId1;
 
             //testuser create item in status pending
             // in context /ir/context/escidoc:persistent3
             String itemXml = 
                 prepareItem(
                     PWCallback.TEST_HANDLE, 
                     STATUS_PENDING, 
                     null,
                     false, false);
             Document document =
                 EscidocRestSoapTestBase.getDocument(itemXml);
             
             //save ids
             itemId = getObjidValue(document);
             itemHref = Constants.ITEM_BASE_URI + "/" + itemId;
             publicComponentId = extractComponentId(document, VISIBILITY_PUBLIC);
             publicComponentHref = 
                 itemHref 
                     + "/" + Constants.SUB_COMPONENT 
                     + "/" + publicComponentId;
             
             //testuser1 create item in status pending
             // in context /ir/context/escidoc:persistent3
             itemXml = 
                 prepareItem(
                     PWCallback.TEST_HANDLE1, 
                     STATUS_PENDING, 
                     null,
                     false, false);
             document =
                 EscidocRestSoapTestBase.getDocument(itemXml);
             
             //save ids
             itemId1 = getObjidValue(document);
             itemHref1 = Constants.ITEM_BASE_URI + "/" + itemId1;
             publicComponentId1 = extractComponentId(document, VISIBILITY_PUBLIC);
             publicComponentHref1 = 
                 itemHref1 
                     + "/" + Constants.SUB_COMPONENT 
                     + "/" + publicComponentId1;
             
         } finally {
             //revoke sysadmin grant for testuser
             revokeAllGrants(TEST_USER_ACCOUNT_ID);
             revokeAllGrants(TEST_USER_ACCOUNT_ID1);
         }
 
     }
     
     /**
      * Test declining retrieving an unknown item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveUnknownItemDecline() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, null, false,
             null, ItemNotFoundException.class);
     }
 
     /**
      * Test declining retrieving an item in state pending without specifying the
      * version id.
      * 
      * @test.name Retrieve Pending Item - No version number
      * @test.id AA_CUA-10-rest
      * @test.input: Id of existing item, no version number
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issue http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=475
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemPendingDecline() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_PENDING,
             false, null, AuthorizationException.class);
     }
 
     /**
      * Test declining retrieving an item in state pending with specifying the
      * version id.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemPendingDeclineWithVersionNumber()
         throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_PENDING,
             false, "1", AuthorizationException.class);
     }
 
     /**
      * Test declining retrieving an item in state submitted.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemSubmittedDecline() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_SUBMITTED,
             false, "1", AuthorizationException.class);
     }
 
     /**
      * Test successfully retrieving an item in state released.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemReleased() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_RELEASED,
             false, null, null);
     }
 
     /**
      * Test successfully retrieving an item version in version status released
      * and in public-status withdrawn.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemWithdrawnVersionReleased() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_WITHDRAWN,
             true, "3", null);
     }
 
     /**
      * Test successfully retrieving an item in public-status withdrawn, version
      * status not released.
      * 
      * @test.issue http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=598
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveItemWithdrawn() throws Exception {
 
         doTestRetrieveItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_WITHDRAWN,
             true, null, null);
     }
 
     /**
      * Test successfully retrieving the latest released version 
      * of an item in status pending.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void _testRetrieveItemReleasedUpdated() throws Exception {
 
         String xml = doTestRetrieveItem(
                 HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_RESUBMITTED,
             true, null, null);
         final Document retrievedDocument =
             EscidocRestSoapTestBase.getDocument(xml);
         String thisVersion = selectSingleNode(
             retrievedDocument, 
             "/item/properties/version/number/text()").getNodeValue();
         String latestVersion = selectSingleNode(
             retrievedDocument, 
             "/item/properties/latest-version/number/text()").getNodeValue();
         assertNotEquals("latest-release was not returned", 
             latestVersion, thisVersion);
     }
 
     /**
      * Test declining submitting an unknown item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testSubmitUnknownItemDecline() throws Exception {
 
         doTestSubmitItem(HANDLE, PWCallback.DEFAULT_HANDLE, null,
             AuthorizationException.class);
     }
 
     /**
      * Test declining submitting an item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testSubmitItemDecline() throws Exception {
 
         doTestSubmitItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_PENDING,
             AuthorizationException.class);
     }
 
     /**
      * Test declining releasing an unknown item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testReleaseUnknownItemDecline() throws Exception {
 
         doTestReleaseItem(HANDLE, PWCallback.DEFAULT_HANDLE, null,
             AuthorizationException.class);
     }
 
     /**
      * Test declining releasing an item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testReleaseItemDecline() throws Exception {
 
         doTestReleaseItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_SUBMITTED,
             AuthorizationException.class);
     }
 
     /**
      * Test declining updating an unknown item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testUpdateUnknownItemDecline() throws Exception {
 
         doTestUpdateItem(HANDLE, PWCallback.DEFAULT_HANDLE, null, null,
             AuthorizationException.class);
     }
 
     /**
      * Test declining updating an item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testUpdateItemDecline() throws Exception {
 
         doTestUpdateItem(HANDLE, PWCallback.DEFAULT_HANDLE, null, null,
             AuthorizationException.class);
     }
 
     /**
      * Test declining withdrawing an unknown item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testWithdrawUnknownItemDecline() throws Exception {
 
         doTestWithdrawItem(HANDLE, PWCallback.DEFAULT_HANDLE, null,
             AuthorizationException.class);
     }
 
     /**
      * Test declining withdrawing an item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testWithdrawItemDecline() throws Exception {
 
         doTestWithdrawItem(HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_RELEASED,
             AuthorizationException.class);
     }
 
     /**
      * Test successfully retrieving a container 
      * in public-status withdrawn, version
      * status not released.
      * 
      * @test.issue http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=598
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveContainerWithdrawn() throws Exception {
 
         doTestRetrieveContainer(
                 HANDLE, PWCallback.DEFAULT_HANDLE, STATUS_WITHDRAWN,
             true, null, null);
     }
 
     /**
      * Test declining creating a StagingFile by an anonymous user.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testCreateStagingFileDecline() throws Exception {
 
         doTestCreateStagingFile(HANDLE, AuthorizationException.class);
     }
 
     /**
      * Test declining retrieving an user account by an anonymous user.<br>
      * The retrieval uses the user account id.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveUserAccountDecline() throws Exception {
 
         doTestRetrieveUserAccount(HANDLE, null, "byId",
             PWCallback.DEFAULT_HANDLE, STATUS_ACTIVE,
             AuthorizationException.class);
     }
 
     /**
      * Test declining retrieving an user account by an anonymous user.<br>
      * The retrieval uses the user account login name.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveUserAccountByLoginNameDecline() throws Exception {
 
         doTestRetrieveUserAccount(HANDLE, null, "byLoginName",
             PWCallback.DEFAULT_HANDLE, STATUS_ACTIVE,
             AuthorizationException.class);
     }
 
     /**
      * Test declining retrieving an user account by an anonymous user.<br>
      * The retrieval uses an handle (PWCallback.DEPOSITOR_HANDLE).
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveUserAccountByHandle() throws Exception {
 
         doTestRetrieveUserAccount(HANDLE, PWCallback.DEPOSITOR_HANDLE, null, null,
             null, AuthorizationException.class);
     }
 
     /**
      * Test successfully retrieving a content model by an anonymous user.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveContentModel() throws Exception {
 
         doTestRetrieveContentModel(HANDLE, CONTENT_TYPE_ID, null);
     }
 
     /**
      * Test successfully retrieving children of a 
      * organizational unit by an anonymous user.
      * 
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=597
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveOrgUnitChildren() throws Exception {
         doTestRetrieveOrganizationalUnitChildren(
                 HANDLE, 
                 STATUS_OPEN, 
                 new String[]{STATUS_NEW, STATUS_OPEN}, 
                 null);
     }
 
     /**
      * Test declining retrieving a container by providing an id of a existing
      * resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve Container - Wrong Id
      * @test.id AA_Def-1-a
      * @test.input Id of an existing resource of another type.
      * @test.expected: ContainerNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1a() throws Exception {
 
         try {
             retrieve(CONTAINER_HANDLER_CODE, CONTEXT_ID);
             EscidocRestSoapTestBase
                 .failMissingException(ContainerNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 ContainerNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a content model by providing an id of a
      * existing resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve Content Model - Wrong Id
      * @test.id AA_Def-1-b
      * @test.input Id of an existing resource of another type.
      * @test.expected: ContentModelNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1b() throws Exception {
 
         try {
             retrieve(CONTENT_MODEL_HANDLER_CODE, CONTEXT_ID);
             EscidocRestSoapTestBase
                 .failMissingException(ContentModelNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 ContentModelNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a context by providing an id of a existing
      * resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve Context - Wrong Id
      * @test.id AA_Def-1-c
      * @test.input Id of an existing resource of another type.
      * @test.expected: ContextNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1c() throws Exception {
 
         try {
             retrieve(CONTEXT_HANDLER_CODE, CONTENT_TYPE_ID);
             EscidocRestSoapTestBase
                 .failMissingException(ContextNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 ContextNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving an item by providing an id of a existing
      * resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve Item - Wrong Id
      * @test.id AA_Def-1-d
      * @test.input Id of an existing resource of another type.
      * @test.expected: ItemNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1d() throws Exception {
 
         try {
             retrieve(ITEM_HANDLER_CODE, CONTEXT_ID);
             EscidocRestSoapTestBase
                 .failMissingException(ItemNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 ItemNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving an organizational unit by providing an id of a
      * existing resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve Organizational Unit - Wrong Id
      * @test.id AA_Def-1-e
      * @test.input Id of an existing resource of another type.
      * @test.expected: OrganizationalUnitNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1e() throws Exception {
 
         try {
             retrieve(ORGANIZATIONAL_UNIT_HANDLER_CODE, CONTEXT_ID);
             EscidocRestSoapTestBase
                 .failMissingException(OrganizationalUnitNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 OrganizationalUnitNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving a role by providing an id of a existing
      * resource of another resource type. This test is not possibleas intented,
      * because the default user is never allowed to retrieve a role.
      * 
      * @test.name Default Policies - Retrieve Role - Wrong Id
      * @test.id AA_Def-1-f
      * @test.input Id of an existing resource of another type.
      * @test.expected: RoleNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     // public void testAaDef1f() throws Exception {
     //        
     // try {
     // retrieve(ROLE_HANDLER_CODE, CONTEXT_ID);
     // failMissingException(RoleNotFoundException.class);
     // }
     // catch (Exception e) {
     // assertExceptionType(RoleNotFoundException.class, e);
     // }
     // }
     /**
      * Test declining retrieving a staging file by providing an id of a existing
      * resource of another resource type.
      * 
      * @test.name Default Policies - Retrieve STaging File - Wrong Id
      * @test.id AA_Def-1-g
      * @test.input Id of an existing resource of another type.
      * @test.expected: StagingFileNotFoundException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef1g() throws Exception {
 
         try {
             retrieve(STAGING_FILE_HANDLER_CODE, CONTEXT_ID);
             EscidocRestSoapTestBase
                 .failMissingException(StagingFileNotFoundException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 StagingFileNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining retrieving an user account by providing an id of a
      * existing resource of another resource type. <br>
      * This test is not possibleas intented, because the subject's handle is
      * fetched before an user-account attribute is fetched. This leads to a
      * redirect to login as in any other attempt to retrieve an user-account.
      * 
      * @test.name Default Policies - Retrieve User Account - Wrong Id
      * @test.id AA_Def-1-h
      * @test.input Id of an existing resource of another type.
      * @test.expected: UserAccountNotFoundException
      * @test.status Rejected
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
 
     /**
      * Test declining querying the semantic store.<br/>
      * 
      * @test.name Default Policies - Query Semantic Store
      * @test.id AA_Def-2
      * @test.input Id of an existing resource of another type.
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testAaDef2() throws Exception {
 
         final String param =
             getTaskParametrSpo("&lt;info:fedora/escidov:user1"
                 + "&gt;  * * ooo", "N-Triples");
         PWCallback.setHandle(PWCallback.ANONYMOUS_HANDLE);
         try {
             spo(param);
             EscidocRestSoapTestBase
                 .failMissingException(AuthorizationException.class);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 AuthorizationException.class, e);
         }
     }
 
     /**
      * Tests declining retrieving a context in status created.
      * 
      * @test.name Default Policies - Retrieve Created UserAccount
      * @test.id AA_Def-3
      * @test.input
      *          <ul>
      *          <li>Id of existing context in public-status "created".</li>
      *          <li>User is not administrator of the context.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=378
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef3() throws Exception {
 
         // create context using System-Administrator
         PWCallback.resetHandle();
 
         String path = TEMPLATE_CONTEXT_PATH;
         if (getTransport() == Constants.TRANSPORT_REST) {
             path += "/rest";
         }
         else if (getTransport() == Constants.TRANSPORT_SOAP) {
             path += "/soap";
         }
         final Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(path,
                 "context_create.xml");
         substitute(toBeCreatedDocument, XPATH_CONTEXT_PROPERTIES_NAME,
             getUniqueName("Some Context "));
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = null;
         try {
             createdXml = create(CONTEXT_HANDLER_CODE, toBeCreatedXml);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(
                 "Test init: Context creation failed.", e);
         }
         final String id = getObjidValue(createdXml);
 
         // retrieve context
 
         Class<AuthorizationException> ec = AuthorizationException.class;
 
         PWCallback.setHandle(HANDLE);
         try {
             retrieve(CONTEXT_HANDLER_CODE, id);
             EscidocRestSoapTestBase.failMissingException(
                 "Retrieving created context not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Retrieving created context not declined, properly.", ec, e);
         }
 
     }
 
     /**
      * Tests declining retrieving an aggregation definition.
      * 
      * @test.name Default Policies - Retrieve Aggregation definition
      * @test.id AA_Def-4
      * @test.input
      *          <ul>
      *          <li>Id.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef4() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
         try {
             retrieve(AGGREGATION_DEFINITION_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Retrieving aggregation definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Retrieving aggregation definition not declined, properly.",
                 ec, e);
         }
     }
 
     /**
      * Tests declining retrieving a report definition.
      * 
      * @test.name Default Policies - Retrieve Report Definition
      * @test.id AA_Def-5
      * @test.input
      *          <ul>
      *          <li>Id.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef5() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
         try {
             retrieve(REPORT_DEFINITION_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Retrieving report definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Retrieving report definition not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests successfully retrieving a report.
      * 
      * @test.name Default Policies - Retrieve Report
      * @test.id AA_Def-6
      * @test.input
      *          <ul>
      *          <li>Task parameter Xml.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef6() throws Exception {
 
         final Document parameterDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(
                 TEMPLATE_REP_PARAMETERS_PATH, "escidoc_report_parameters1.xml");
         fixLinkAttributes(
                 parameterDocument, XPATH_REPORT_PARAMETERS_REPORT_DEFINITION);
         String parameterXml = toString(parameterDocument, false);
         parameterXml = parameterXml.replaceAll("repdef3", "repdef1");
 
         try {
             retrieve(REPORT_HANDLER_CODE, parameterXml);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(e);
         }
     }
 
     /**
      * Tests declining retrieving a scope.
      * 
      * @test.name Default Policies - Retrieve Scope
      * @test.id AA_Def-7
      * @test.input
      *          <ul>
      *          <li>Id of existing scope.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef7() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
         try {
             retrieve(SCOPE_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Retrieving scope not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Retrieving scope not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests declining deleting an aggregation definition.
      * 
      * @test.name Default Policies - Delete Aggregation definition
      * @test.id AA_Def-8
      * @test.input
      *          <ul>
      *          <li>Id.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef8() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
 
         try {
             delete(AGGREGATION_DEFINITION_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Deleting aggregation definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Deleting aggregation definition not declined, properly.", ec,
                 e);
         }
     }
 
     /**
      * Tests declining deleting a report definition.
      * 
      * @test.name Default Policies - Delete Report Definition
      * @test.id AA_Def-9
      * @test.input
      *          <ul>
      *          <li>Id.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef9() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
 
         try {
             delete(REPORT_DEFINITION_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Deleting report definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Deleting report definition not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests declining deleting a scope.
      * 
      * @test.name Default Policies - Delete Scope
      * @test.id AA_Def-10
      * @test.input
      *          <ul>
      *          <li>Id of existing scope.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef10() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
         try {
             delete(SCOPE_HANDLER_CODE, "1");
             EscidocRestSoapTestBase.failMissingException(
                 "Deleting scope not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Deleting scope not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests declining creating an aggregation definition.
      * 
      * @test.name Default Policies - Create Aggregation definition
      * @test.id AA_Def-11
      * @test.input
      *          <ul>
      *          <li>Valid Xml representation of an aggregation definition.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef11() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
 
         final Document toBeCreatedDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_AGG_DEF_PATH,
                 "escidoc_aggregation_definition2.xml");
         fixLinkAttributes(toBeCreatedDoc, XPATH_AGGREGATION_DEFINITION);
         fixLinkAttributes(toBeCreatedDoc, XPATH_AGGREGATION_DEFINITION_SCOPE);
         String toBeCreatedXml = toString(toBeCreatedDoc, false);
 
         try {
             create(AGGREGATION_DEFINITION_HANDLER_CODE, toBeCreatedXml);
             EscidocRestSoapTestBase.failMissingException(
                 "Creating aggregation definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating aggregation definition not declined, properly.", ec,
                 e);
         }
     }
 
     /**
      * Tests declining creating a report definition.
      * 
      * @test.name Default Policies - Create Report Definition
      * @test.id AA_Def-12
      * @test.input
      *          <ul>
      *          <li>Valid Xml representation of a report definition.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef12() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
 
         final Document toBeCreatedDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_REP_DEF_PATH,
                 "escidoc_report_definition1.xml");
         fixLinkAttributes(toBeCreatedDoc, XPATH_REPORT_DEFINITION);
         fixLinkAttributes(toBeCreatedDoc, XPATH_REPORT_DEFINITION_SCOPE);
         String toBeCreatedXml = toString(toBeCreatedDoc, false);
 
         try {
             create(REPORT_DEFINITION_HANDLER_CODE, toBeCreatedXml);
             EscidocRestSoapTestBase.failMissingException(
                 "Creating report definition not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating report definition not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests declining creating a scope.
      * 
      * @test.name Default Policies - Create Scope
      * @test.id AA_Def-13
      * @test.input
      *          <ul>
      *          <li>Valid Xml representation of a scope.</li>
      *          <li>User is anonymous.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @test.issues http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=383
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDef13() throws Exception {
 
         final Class<AuthorizationException> ec = AuthorizationException.class;
 
         final String toBeCreatedXml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_SCOPE_PATH,
                 "escidoc_scope1.xml");
 
         try {
             create(SCOPE_HANDLER_CODE, toBeCreatedXml);
             EscidocRestSoapTestBase.failMissingException(
                 "Creating scope not declined.", ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating scope not declined, properly.", ec, e);
         }
     }
 
     /**
      * Tests creating a user-grant with no scope.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnItem
      * @test.input
      *          <ul>
      *          <li>grant created by test-user for scope null.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantWithoutScopeDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                     null, ROLE_HREF_MD_EDITOR, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests creating a user-grant with scope on an item the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnItem
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnItem() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests creating a user-grant with scope on an container the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnContainer
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnContainer() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests creating a user-grant with scope on an component the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnComponent
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnComponent() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests creating a user-grant for Role User-Inspector 
      * with scope on own account.
      * 
      * @test.name Default Policies - Create User-Inspector Grant
      * @test.id testAaDefCreateUserInspectorGrantForOwnUserAccount
      * @test.input
      *          <ul>
      *          <li>grant created by test-user for own account.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateUserInspectorGrantForOwnUserAccount()
                                                     throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 Constants.USER_ACCOUNT_BASE_URI 
                 + "/" + TEST_USER_ACCOUNT_ID, 
                 ROLE_HREF_USER_ACCOUNT_INSPECTOR, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests declining creating a user-grant 
      * with scope on an item the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnItem
      * @test.input
      *          <ul>
      *          <li>item created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnItemDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 itemHref1, ROLE_HREF_COLLABORATOR, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests declining creating a user-grant 
      * with scope on an container the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnContainer
      * @test.input
      *          <ul>
      *          <li>container created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnContainerDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 containerHref1, ROLE_HREF_COLLABORATOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests declining creating a user-grant 
      * with scope on an component the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGrantOnComponent
      * @test.input
      *          <ul>
      *          <li>component created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGrantOnComponentDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 publicComponentHref1, ROLE_HREF_COLLABORATOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
 
     }
 
     /**
      * Tests declining creating a user-grant for Role User-Inspector 
      * with scope not on own account.
      * 
      * @test.name Default Policies - Create Grant fro Role User-Inspector
      * @test.id testAaDefCreateUserInspectorGrantForDifferentUserAccountDecline
      * @test.input
      *          <ul>
      *          <li>grant created by test-user with scope on test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateUserInspectorGrantForDifferentUserAccountDecline()
                                                             throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 Constants.USER_ACCOUNT_BASE_URI 
                 + "/" + TEST_USER_ACCOUNT_ID1, 
                 ROLE_HREF_USER_ACCOUNT_INSPECTOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests declining creating a user-grant for Role Not User-Inspector 
      * with scope on own account.
      * 
      * @test.name Default Policies - Create Grant for Role Not User-Inspector
      * @test.id testAaDefCreateNonUserInspectorGrantForOwnUserAccountDecline
      * @test.input
      *          <ul>
      *          <li>grant created by test-user with scope on test-user.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateNonUserInspectorGrantForOwnUserAccountDecline()
                                                             throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 Constants.USER_ACCOUNT_BASE_URI + "/" 
                 + TEST_USER_ACCOUNT_ID, ROLE_HREF_ADMINISTRATOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests creating a group-grant with scope on an item the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnItem
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnItem() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests creating a group-grant with scope on an container the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnContainer
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnContainer() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests creating a group-grant with scope on an component the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnComponent
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnComponent() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests creating a group-grant for Role User-Inspector 
      * with scope on own account.
      * 
      * @test.name Default Policies - Create User-Inspector Grant
      * @test.id testAaDefCreateUserInspectorGroupGrantForOwnUserAccount
      * @test.input
      *          <ul>
      *          <li>grant created by test-user for own account.</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateUserInspectorGroupGrantForOwnUserAccount()
                                                     throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 Constants.USER_ACCOUNT_BASE_URI 
                 + "/" + TEST_USER_ACCOUNT_ID, 
                 ROLE_HREF_USER_ACCOUNT_INSPECTOR, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining creating a group-grant 
      * with scope on an item the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnItemDecline
      * @test.input
      *          <ul>
      *          <li>item created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnItemDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 itemHref1, ROLE_HREF_COLLABORATOR, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining creating a group-grant 
      * with scope on an container the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnContainerDecline
      * @test.input
      *          <ul>
      *          <li>container created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnContainerDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 containerHref1, ROLE_HREF_COLLABORATOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining creating a group-grant 
      * with scope on an component the user created.
      * 
      * @test.name Default Policies - Create Grant
      * @test.id testAaDefCreateGroupGrantOnComponentDecline
      * @test.input
      *          <ul>
      *          <li>component created by test-user1.</li>
      *          <li>grant created by test-user for object owned by test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateGroupGrantOnComponentDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 publicComponentHref1, 
                 ROLE_HREF_COLLABORATOR, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining creating a group-grant for Role User-Inspector 
      * with scope not on own account.
      * 
      * @test.name Default Policies - Create Group Grant for Role User-Inspector
      * @test.id testAaDefCreateUserInspectorGrantForDifferentUserAccountDecline
      * @test.input
      *          <ul>
      *          <li>grant created by test-user with scope on test-user1.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateUserInspectorGroupGrantForDifferentUserAccountDecline()
                                                             throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 Constants.USER_ACCOUNT_BASE_URI 
                 + "/" + TEST_USER_ACCOUNT_ID1, 
                 ROLE_HREF_USER_ACCOUNT_INSPECTOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining creating a user-grant for Role Not User-Inspector 
      * with scope on own account.
      * 
      * @test.name Default Policies - Create Grant for Role Not User-Inspector
      * @test.id testAaDefCreateNonUserInspectorGrantForOwnUserAccountDecline
      * @test.input
      *          <ul>
      *          <li>grant created by test-user with scope on test-user.</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefCreateNonUserInspectorGroupGrantForOwnUserAccountDecline()
                                                             throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 Constants.USER_ACCOUNT_BASE_URI 
                 + "/" + TEST_USER_ACCOUNT_ID, 
                 ROLE_HREF_MD_EDITOR, 
                 AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a user-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnItem
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnItem() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, userId, grantId, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a user-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnContainer
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnContainer() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, userId, grantId, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a user-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnComponent
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnComponent() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, userId, grantId, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a user-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnItemDecline
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnItemDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, userId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a user-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnContainerDecline
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnContainerDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, userId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a user-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGrantOnComponentDecline
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGrantOnComponentDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, userId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests revoking a group-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnItem
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnItem() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, groupId, grantId, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a group-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnContainer
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnContainer() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, groupId, grantId, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a group-grant the user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnComponent
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnComponent() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE, groupId, grantId, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a group-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnItemDecline
      * @test.input
      *          <ul>
      *          <li>item created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnItemDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, groupId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a group-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnContainerDecline
      * @test.input
      *          <ul>
      *          <li>container created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnContainerDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 containerHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, groupId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests revoking a group-grant another user created.
      * 
      * @test.name Default Policies - Revoke Grant
      * @test.id testAaDefRevokeGroupGrantOnComponentDecline
      * @test.input
      *          <ul>
      *          <li>component created by test-user.</li>
      *          <li>grant created by test-user for own object.</li>
      *          <li>grant revoked by test1-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRevokeGroupGrantOnComponentDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, groupId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRevokeGrant(PWCallback.TEST_HANDLE1, groupId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests retrieving a grant of himself.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGroupGrant
      * @test.input
      *          <ul>
      *          <li>grant for test-user created by systemadministrator.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGrantHimself() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, TEST_USER_ACCOUNT_ID, 
                 Constants.CONTEXT_BASE_URI + "/" + CONTEXT_ID, 
                             ROLE_HREF_MD_EDITOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, TEST_USER_ACCOUNT_ID, 
                 grantId, null);
         } finally {
             revokeAllGrants(TEST_USER_ACCOUNT_ID);
         }
     }
 
     /**
      * Tests retrieving a grant user created.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGrant
      * @test.input
      *          <ul>
      *          <li>grant created by test-user.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGrant() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE, userId, 
                 itemHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, userId, 
                 grantId, null);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests declining retrieving a grant 
      * user did not create.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGrantDecline
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGrantDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_ACCOUNT_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, userId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, userId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(userId);
         }
     }
 
     /**
      * Tests retrieving a group-grant of a group user is member.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGroupGrant
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test1-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGroupGrant() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, groupId1, 
                 itemHref1, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE1, groupId1, 
                 grantId, null);
         } finally {
             revokeAllGrants(groupId1);
         }
     }
 
     /**
      * Tests retrieving a group-grant user created.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGroupGrant1
      * @test.input
      *          <ul>
      *          <li>grant created by test-user.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGroupGrant1() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.TEST_HANDLE1, TEST_USER_GROUP_ID, 
                 itemHref1, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE1, TEST_USER_GROUP_ID, 
                 grantId, null);
         } finally {
             revokeAllGrants(TEST_USER_GROUP_ID);
         }
     }
 
     /**
      * Tests retrieving a group-grant of a group where another group is member.
      * user is member of the other group
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveHierarchyGroupGrant
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test1-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveHierarchyGroupGrant() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, groupId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE1, groupId, 
                 grantId, null);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests declining retrieving a group-grant 
      * of a group user is not member.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGroupGrantDecline
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGroupGrantDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, groupId1, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, groupId1, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId1);
         }
     }
 
     /**
      * Tests declining retrieving a group-grant 
      * user did not create.
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveGroupGrantDecline
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveGroupGrantDecline1() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, TEST_USER_GROUP_ID, 
                 itemHref1, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, TEST_USER_GROUP_ID, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(TEST_USER_GROUP_ID);
         }
     }
 
     /**
      * Tests declining retrieving a group-grant of a group where another group is member.
      * testuser1 is member of the other group, but not testuser
      * 
      * @test.name Default Policies - retrieve Grant
      * @test.id testAaDefRetrieveHierarchyGroupGrant
      * @test.input
      *          <ul>
      *          <li>grant created by systemadministrator.</li>
      *          <li>grant retrieved by test-user</li>
      *          </ul>
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefRetrieveHierarchyGroupGrantDecline() throws Exception {
         try {
             super.setClient((GrantClient) getClient(USER_GROUP_HANDLER_CODE));
             String grantXml = doTestCreateGrant(
                 PWCallback.DEFAULT_HANDLE, groupId, 
                 publicComponentHref, ROLE_HREF_COLLABORATOR, null);
             String grantId = getObjidValue(grantXml);
             doTestRetrieveGrant(PWCallback.TEST_HANDLE, groupId, 
                 grantId, AuthorizationException.class);
         } finally {
             revokeAllGrants(groupId);
         }
     }
 
     /**
      * Tests evaluating requests for anonymous user
      * 
      * @test.name Default Policies - evaluate
      * @test.id testAaDefAnonymousEvaluateAnonymous
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefAnonymousEvaluateAnonymous() throws Exception {
         try {
             PWCallback.setAnonymousHandle();
             String requestsXml =
                 EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_REQUESTS_PATH, "requests2.xml");
             assertXmlValidRequests(requestsXml);
             String responseXml = handleXmlResult(getPolicyDecisionPointClient().evaluate(
                 requestsXml));
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests declining evaluating requests for anonymous user
      * 
      * @test.name Default Policies - evaluate
      * @test.id testAaDefAnonymousEvaluateUserDecline
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefAnonymousEvaluateUserDecline() throws Exception {
         try {
             PWCallback.setAnonymousHandle();
             String requestsXml =
                 EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_REQUESTS_PATH, "requests1.xml");
             assertXmlValidRequests(requestsXml);
             String responseXml = handleXmlResult(getPolicyDecisionPointClient().evaluate(
                 requestsXml));
             EscidocRestSoapTestBase
             .failMissingException(AuthorizationException.class);
         } catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 AuthorizationException.class, e);
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests evaluating requests for anonymous user
      * 
      * @test.name Default Policies - evaluate
      * @test.id testAaDefAnonymousEvaluateAnonymous
      * @test.expected: OK
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefUserEvaluateAnonymous() throws Exception {
         try {
             revokeAllGrants(TEST_USER_ACCOUNT_ID);
             PWCallback.setHandle(PWCallback.TEST_HANDLE);
             String requestsXml =
                 EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_REQUESTS_PATH, "requests2.xml");
             assertXmlValidRequests(requestsXml);
             String responseXml = handleXmlResult(getPolicyDecisionPointClient().evaluate(
                 requestsXml));
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests declining evaluating requests for anonymous user
      * 
      * @test.name Default Policies - evaluate
      * @test.id testAaDefAnonymousEvaluateUserDecline
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefUserEvaluateUser() throws Exception {
         try {
             revokeAllGrants(TEST_USER_ACCOUNT_ID);
             PWCallback.setHandle(PWCallback.TEST_HANDLE);
             String requestsXml =
                 EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_REQUESTS_PATH, "requests1.xml");
             assertXmlValidRequests(requestsXml);
             String responseXml = handleXmlResult(getPolicyDecisionPointClient().evaluate(
                 requestsXml));
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests declining evaluating requests for anonymous user
      * 
      * @test.name Default Policies - evaluate
      * @test.id testAaDefAnonymousEvaluateUserDecline
      * @test.expected: AuthorizationException
      * @test.status Implemented
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAaDefUserEvaluateUserDecline() throws Exception {
         try {
             revokeAllGrants(TEST_USER_ACCOUNT_ID);
             PWCallback.setHandle(PWCallback.TEST_HANDLE);
             String requestsXml =
                 EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_REQUESTS_PATH, "requests.xml");
             assertXmlValidRequests(requestsXml);
             String responseXml = handleXmlResult(getPolicyDecisionPointClient().evaluate(
                 requestsXml));
             EscidocRestSoapTestBase
             .failMissingException(AuthorizationException.class);
         } catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 AuthorizationException.class, e);
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
 }
