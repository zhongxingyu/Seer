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
 
 import de.escidoc.core.test.EscidocRestSoapTestsBase;
 import de.escidoc.core.test.common.client.servlet.ClientBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.aa.RoleClient;
 import de.escidoc.core.test.common.client.servlet.aa.UserAccountClient;
 import de.escidoc.core.test.common.client.servlet.aa.UserGroupClient;
 import de.escidoc.core.test.common.client.servlet.aa.UserManagementWrapperClient;
 import de.escidoc.core.test.common.client.servlet.cmm.ContentModelClient;
 import de.escidoc.core.test.common.client.servlet.om.ContainerClient;
 import de.escidoc.core.test.common.client.servlet.om.ContentRelationClient;
 import de.escidoc.core.test.common.client.servlet.om.ContextClient;
 import de.escidoc.core.test.common.client.servlet.om.ItemClient;
 import de.escidoc.core.test.common.client.servlet.om.SemanticStoreClient;
 import de.escidoc.core.test.common.client.servlet.om.interfaces.SubmitReleaseReviseWithdrawClientInterface;
 import de.escidoc.core.test.common.client.servlet.oum.OrganizationalUnitClient;
 import de.escidoc.core.test.common.client.servlet.sm.AggregationDefinitionClient;
 import de.escidoc.core.test.common.client.servlet.sm.PreprocessingClient;
 import de.escidoc.core.test.common.client.servlet.sm.ReportClient;
 import de.escidoc.core.test.common.client.servlet.sm.ReportDefinitionClient;
 import de.escidoc.core.test.common.client.servlet.sm.ScopeClient;
 import de.escidoc.core.test.common.client.servlet.sm.StatisticDataClient;
 import de.escidoc.core.test.common.client.servlet.st.StagingFileClient;
 import de.escidoc.core.test.om.OmTestBase;
 import de.escidoc.core.test.security.client.PWCallback;
 import de.escidoc.core.test.sm.SmTestBase;
 import de.escidoc.core.test.st.StagingFileTestBase;
 import junit.framework.TestCase;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 /**
  * Base class for AA tests.
  * 
  * @author TTE
  * 
  */
 public class AaTestBase extends EscidocRestSoapTestsBase {
 
     protected static final int CONTAINER_HANDLER_CODE = 1;
 
     protected static final int CONTENT_MODEL_HANDLER_CODE = 2;
 
     protected static final int CONTEXT_HANDLER_CODE = 3;
 
     protected static final int ITEM_HANDLER_CODE = 4;
 
     protected static final int ORGANIZATIONAL_UNIT_HANDLER_CODE = 5;
 
     protected static final int ROLE_HANDLER_CODE = 6;
 
     protected static final int SEMANTIC_STORE_HANDLER_CODE = 7;
 
     protected static final int STAGING_FILE_HANDLER_CODE = 8;
 
     protected static final int USER_ACCOUNT_HANDLER_CODE = 9;
 
     protected static final int USER_MANAGEMENT_WRAPPER_CODE = 10;
 
     protected static final int AGGREGATION_DEFINITION_HANDLER_CODE = 11;
 
     protected static final int REPORT_DEFINITION_HANDLER_CODE = 12;
 
     protected static final int REPORT_HANDLER_CODE = 13;
 
     protected static final int SCOPE_HANDLER_CODE = 14;
 
     protected static final int STATISTIC_DATA_HANDLER_CODE = 15;
 
     protected static final int USER_GROUP_HANDLER_CODE = 16;
 
     protected static final String STATUS_PENDING = "pending";
 
     protected static final String STATUS_SUBMITTED = "submitted";
 
     protected static final String STATUS_RELEASED = "released";
 
     protected static final String STATUS_WITHDRAWN = "withdrawn";
 
     protected static final String STATUS_RESUBMITTED = "resubmitted";
 
     protected static final String STATUS_RELEASED_UPDATED = "releasedupdated";
 
     protected static final String STATUS_ACTIVE = "active";
 
     protected static final String STATUS_DEACTIVE = "deactive";
 
     protected static final String STATUS_NEW = "new";
 
     protected static final String STATUS_OPEN = "open";
 
     protected static final String STATUS_CLOSE = "close";
 
     protected static final String VISIBILITY_PRIVATE = "private";
 
     protected static final String VISIBILITY_PUBLIC = "public";
 
     protected static final String VISIBILITY_AUDIENCE = "audience";
     
     //ROLE-HREFS
     private static final String ROLE_HREF_PREFIX = "/aa/role/";
 
     public static final String ROLE_HREF_ADMINISTRATOR = 
         ROLE_HREF_PREFIX + "escidoc:role-administrator";
 
     public static final String ROLE_HREF_AUDIENCE = 
         ROLE_HREF_PREFIX + "escidoc:role-audience";
 
     public static final String ROLE_HREF_AUTHOR = 
         ROLE_HREF_PREFIX + "escidoc:role-author";
 
     public static final String ROLE_HREF_COLLABORATOR = 
         ROLE_HREF_PREFIX + "escidoc:role-collaborator";
 
     public static final String ROLE_HREF_COLLABORATOR_MODIFIER = 
         ROLE_HREF_PREFIX + "escidoc:role-collaborator-modifier";
 
     public static final String ROLE_HREF_GRANT_TEST_1 = 
         ROLE_HREF_PREFIX + "escidoc:role-grant-test1";
 
     public static final String ROLE_HREF_GRANT_TEST_2 = 
         ROLE_HREF_PREFIX + "escidoc:role-grant-test2";
 
     public static final String ROLE_HREF_GRANT_TEST_3 = 
         ROLE_HREF_PREFIX + "escidoc:role-grant-test3";
 
     public static final String ROLE_HREF_GRANT_TEST_4 = 
         ROLE_HREF_PREFIX + "escidoc:role-grant-test4";
 
     public static final String ROLE_HREF_GRANT_TEST_5 = 
         ROLE_HREF_PREFIX + "escidoc:role-grant-test5";
 
     public static final String 
         ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_MEMBERS = 
         ROLE_HREF_PREFIX 
         + "escidoc:role-collaborator-modifier-container-add-remove-members";
 
     public static final String 
         ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_DIRECT_MEMBERS = 
         ROLE_HREF_PREFIX 
         + "escidoc:role-collaborator-modifier-container-update-direct-members";
 
     public static final String 
         ROLE_HREF_COLLABORATOR_MODIFIER_ADD_REMOVE_ANY_MEMBERS = 
         ROLE_HREF_PREFIX 
         + "escidoc:role-collaborator-modifier-container-add-remove-any-members";
 
     public static final String 
         ROLE_HREF_COLLABORATOR_MODIFIER_UPDATE_ANY_MEMBERS = 
         ROLE_HREF_PREFIX 
         + "escidoc:role-collaborator-modifier-container-update-any-members";
 
     public static final String ROLE_HREF_DEPOSITOR = 
         ROLE_HREF_PREFIX + "escidoc:role-depositor";
 
     public static final String ROLE_HREF_INGESTER = 
         ROLE_HREF_PREFIX + "escidoc:role-ingester";
 
     public static final String ROLE_HREF_MD_EDITOR = 
         ROLE_HREF_PREFIX + "escidoc:role-md-editor";
 
     public static final String ROLE_HREF_MODERATOR = 
         ROLE_HREF_PREFIX + "escidoc:role-moderator";
 
     public static final String ROLE_HREF_USER_GROUP_ADMIN = 
         ROLE_HREF_PREFIX + "escidoc:role-user-group-administrator";
 
     public static final String ROLE_HREF_USER_GROUP_INSPECTOR = 
         ROLE_HREF_PREFIX + "escidoc:role-user-group-inspector";
 
     public static final String ROLE_HREF_USER_ACCOUNT_ADMIN = 
         ROLE_HREF_PREFIX + "escidoc:role-user-account-administrator";
 
     public static final String ROLE_HREF_USER_ACCOUNT_INSPECTOR = 
         ROLE_HREF_PREFIX + "escidoc:role-user-account-inspector";
 
     public static final String ROLE_HREF_CONTEXT_ADMIN = 
         ROLE_HREF_PREFIX + "escidoc:role-context-administrator";
 
     public static final String ROLE_HREF_CONTEXT_MODIFIER = 
         ROLE_HREF_PREFIX + "escidoc:role-context-modifier";
 
     public static final String ROLE_HREF_PRIVILEGED_VIEWER = 
         ROLE_HREF_PREFIX + "escidoc:role-privileged-viewer";
 
     public static final String ROLE_HREF_CONTENT_RELATION_MANAGER = 
         ROLE_HREF_PREFIX + "escidoc:role-content-relation-manager";
 
     public static final String ROLE_HREF_CONTENT_RELATION_MODIFIER = 
         ROLE_HREF_PREFIX + "escidoc:role-content-relation-modifier";
 
     public static final String ROLE_HREF_STATISTICS_EDITOR = 
         ROLE_HREF_PREFIX + "escidoc:role-statistics-editor";
 
     public static final String ROLE_HREF_STATISTICS_READER = 
         ROLE_HREF_PREFIX + "escidoc:role-statistics-reader";
 
     public static final String ROLE_HREF_SYSTEM_ADMINISTRATOR = 
         ROLE_HREF_PREFIX + "escidoc:role-system-administrator";
 
     public static final String ROLE_HREF_OU_ADMINISTRATOR = 
         ROLE_HREF_PREFIX + "escidoc:role-ou-administrator";
 
     public static final String ROLE_HREF_SYSTEM_INSPECTOR = 
         ROLE_HREF_PREFIX + "escidoc:role-system-inspector";
 
     protected final ContainerClient containerClient;
 
     protected final ContentModelClient contentTypeClient;
 
     protected final ContextClient contextClient;
 
     protected final ItemClient itemClient;
 
     protected final OrganizationalUnitClient organizationalUnitClient;
 
     protected final RoleClient roleClient;
 
     protected final SemanticStoreClient semanticStoreClient;
 
     protected final StagingFileClient stagingFileClient;
 
     protected final UserAccountClient userAccountClient;
 
     protected final UserGroupClient userGroupClient;
 
     protected final UserManagementWrapperClient userManagementWrapperClient;
 
     protected final AggregationDefinitionClient aggregationDefinitionClient;
 
     protected final ReportDefinitionClient reportDefinitionClient;
 
     protected final ReportClient reportClient;
 
     protected final ScopeClient scopeClient;
 
     protected final StatisticDataClient statisticDataClient;
 
     protected final PreprocessingClient preprocessingClient;
     
     protected final ContentRelationClient contentRelationClient;
     
     protected final OrganizationalUnitHelper orgUnitHelper;
 
     private SmTestBase smTestBase = null;
     
     private final String testUploadFile = "UploadTest.zip";
 
     private final String testUploadFileMimeType = "application/zip";
 
     private final Pattern SCOPE_PATTERN =
         Pattern.compile(".*<[^>]*?scope[^>]*?objid=\"(.*?)\".*", Pattern.DOTALL
             + Pattern.MULTILINE);
 
     /**
      * The constructor.
      * 
      * @param transport
      *            The transport identifier.
      */
     public AaTestBase(final int transport) {
 
         super(transport);
 
         PWCallback.resetHandle();
         this.containerClient = new ContainerClient(transport);
         this.contentTypeClient = new ContentModelClient(transport);
         this.contextClient = new ContextClient(transport);
         this.itemClient = new ItemClient(transport);
         this.organizationalUnitClient = new OrganizationalUnitClient(transport);
         this.roleClient = new RoleClient(Constants.TRANSPORT_REST);
         this.semanticStoreClient = new SemanticStoreClient(transport);
         this.stagingFileClient =
             new StagingFileClient(Constants.TRANSPORT_REST);
         this.userAccountClient = new UserAccountClient(transport);
         this.userGroupClient = new UserGroupClient(transport);
         this.userManagementWrapperClient =
             new UserManagementWrapperClient(transport);
         this.aggregationDefinitionClient =
             new AggregationDefinitionClient(transport);
         this.reportDefinitionClient = new ReportDefinitionClient(transport);
         this.reportClient = new ReportClient(transport);
         this.scopeClient = new ScopeClient(transport);
         this.statisticDataClient = new StatisticDataClient(transport);
         this.preprocessingClient = new PreprocessingClient(transport);
         this.contentRelationClient = new ContentRelationClient(transport);
         this.orgUnitHelper = new OrganizationalUnitHelper(transport);
         this.smTestBase = new SmTestBase(transport);
     }
 
     /**
      * Set up test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     @Override
     public void setUp() throws Exception {
 
         super.setUp();
     }
 
     /**
      * Clean up after test.<br>
      * This method resets the current user handle in PWCallback.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @After
     @Override
     public void tearDown() throws Exception {
 
         super.tearDown();
         PWCallback.resetHandle();
     }
 
     /**
      * @return Returns the containerClient.
      */
     public ContainerClient getContainerClient() {
         return containerClient;
     }
 
     /**
      * @return Returns the contextClient.
      */
     public ContextClient getContextClient() {
         return contextClient;
     }
 
     /**
      * @return Returns the itemClient.
      */
     public ItemClient getItemClient() {
         return itemClient;
     }
 
     /**
      * @return Returns the organizationalUnitClient.
      */
     public OrganizationalUnitClient getOrganizationalUnitClient() {
         return organizationalUnitClient;
     }
 
     /**
      * @return Returns the roleClient.
      */
     public RoleClient getRoleClient() {
         return roleClient;
     }
 
     /**
      * @return Returns the semanticStoreClient.
      */
     public SemanticStoreClient getSemanticStoreClient() {
         return semanticStoreClient;
     }
 
     /**
      * @return Returns the stagingFileClient.
      */
     @Override
     public StagingFileClient getStagingFileClient() {
         return stagingFileClient;
     }
 
     /**
      * @return Returns the userAccountClient.
      */
     public UserAccountClient getUserAccountClient() {
         return userAccountClient;
     }
 
     /**
      * @return Returns the userAccountClient.
      */
     public UserManagementWrapperClient getUserManagementWrapperClient() {
         return userManagementWrapperClient;
     }
 
     /**
      * @return Returns the userGroupClient.
      */
     public UserGroupClient getUserGroupClient() {
         return userGroupClient;
     }
 
     /**
      * @return Returns the scopeClient.
      */
     public ScopeClient getScopeClient() {
         return scopeClient;
     }
 
     /**
      * @return Returns the aggregationDefinitionClient.
      */
     public AggregationDefinitionClient getAggregationDefinitionClient() {
         return aggregationDefinitionClient;
     }
 
     /**
      * @return Returns the reportDefinitionClient.
      */
     public ReportDefinitionClient getReportDefinitionClient() {
         return reportDefinitionClient;
     }
 
     /**
      * @return Returns the reportClient.
      */
     public ReportClient getReportClient() {
         return reportClient;
     }
 
     /**
      * @return Returns the contentRelationClient.
      */
     public ContentRelationClient getContentRelationClient() {
         return contentRelationClient;
     }
 
     /**
      * @return Returns the statisticDataClient.
      */
     public StatisticDataClient getStatisticDataClient() {
         return statisticDataClient;
     }
 
     public String create(final int handlerCode, final String xml)
         throws Exception {
 
         Object result = getClient(handlerCode).create(xml);
         return handleResult(result);
     }
 
     public void delete(final int handlerCode, final String id) throws Exception {
 
         Object result = getClient(handlerCode).delete(id);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             httpRes.getEntity().consumeContent();
             assertHttpStatusOfMethod("", httpRes);
         }
     }
 
     public String retrieve(final int handlerCode, final String id)
         throws Exception {
 
         Object result = getClient(handlerCode).retrieve(id);
         return handleResult(result);
     }
 
     public String update(
         final int handlerCode, final String id, final String xml)
         throws Exception {
 
         Object result = getClient(handlerCode).update(id, xml);
         return handleResult(result);
     }
 
     public String submit(
         final int handlerCode, final String id, final String xml)
         throws Exception {
 
         Object result =
             ((SubmitReleaseReviseWithdrawClientInterface) getClient(handlerCode))
                 .submit(id, xml);
         return handleResult(result);
     }
 
     public String release(
         final int handlerCode, final String id, final String xml)
         throws Exception {
 
         Object result =
             ((SubmitReleaseReviseWithdrawClientInterface) getClient(handlerCode))
                 .release(id, xml);
         return handleResult(result);
     }
 
     public String releaseWithPid(
         final int handlerCode, final String id, final String creatorUserHandle)
         throws Exception {
 
         Object result =
             ((SubmitReleaseReviseWithdrawClientInterface) getClient(handlerCode))
                 .releaseWithPid(id, creatorUserHandle);
         return handleResult(result);
     }
 
     public String withdraw(
         final int handlerCode, final String id, final String xml)
         throws Exception {
 
         Object result =
             ((SubmitReleaseReviseWithdrawClientInterface) getClient(handlerCode))
                 .withdraw(id, xml);
         return handleResult(result);
     }
 
     /**
      * Handles the result of a base service access.
      * 
      * @param result
      *            The result to handle.
      * @return Returns the xml response.
      * @throws Exception
      *             Thrown if anything fails.
      */
     protected String handleResult(final Object result) throws Exception {
 
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             xmlResult = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);
             httpRes.getEntity().consumeContent();
             assertHttpStatusOfMethod("", httpRes);
            
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Gets the corresponding client for the provided handler code.
      * 
      * @param handlerCode
      *            The code identifying the handler.
      * @return Returns the client.
      * @throws Exception
      *             Thrown if anything fails.
      */
     protected ClientBase getClient(final int handlerCode) throws Exception {
 
         ClientBase client = null;
         switch (handlerCode) {
 
             case CONTAINER_HANDLER_CODE:
                 client = containerClient;
                 break;
             case CONTENT_MODEL_HANDLER_CODE:
                 client = contentTypeClient;
                 break;
             case CONTEXT_HANDLER_CODE:
                 client = contextClient;
                 break;
             case ITEM_HANDLER_CODE:
                 client = itemClient;
                 break;
             case ORGANIZATIONAL_UNIT_HANDLER_CODE:
                 client = organizationalUnitClient;
                 break;
             case ROLE_HANDLER_CODE:
                 client = roleClient;
                 break;
             case SEMANTIC_STORE_HANDLER_CODE:
                 client = semanticStoreClient;
                 break;
             case STAGING_FILE_HANDLER_CODE:
                 client = stagingFileClient;
                 break;
             case USER_ACCOUNT_HANDLER_CODE:
                 client = userAccountClient;
                 break;
             case USER_GROUP_HANDLER_CODE:
                 client = userGroupClient;
                 break;
             case USER_MANAGEMENT_WRAPPER_CODE:
                 client = userManagementWrapperClient;
                 break;
             case AGGREGATION_DEFINITION_HANDLER_CODE:
                 client = aggregationDefinitionClient;
                 break;
             case REPORT_DEFINITION_HANDLER_CODE:
                 client = reportDefinitionClient;
                 break;
             case REPORT_HANDLER_CODE:
                 client = reportClient;
                 break;
             case SCOPE_HANDLER_CODE:
                 client = scopeClient;
                 break;
             case STATISTIC_DATA_HANDLER_CODE:
                 client = statisticDataClient;
                 break;
             default:
                 throw new Exception("Unknown handler code [" + handlerCode
                     + "]");
         }
 
         return client;
     }
 
     /**
      * Test creating a StagingFile.
      * 
      * @param binaryContent
      *            The binary content of the staging file.
      * @param mimeType
      *            The mime type of the data.
      * @param filename
      *            The name of the file.
      * @return The <code>HttpResponse</code> object.
      * @throws Exception
      *             If anything fails.
      */
     protected HttpResponse createStagingFile(
         final InputStream binaryContent, final String mimeType,
         final String filename) throws Exception {
 
         Object result =
             getStagingFileClient().create(binaryContent, mimeType, filename);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             httpRes.getEntity().consumeContent();
             return httpRes;
         }
         else {
             TestCase.fail("Unsupported result type ["
                 + result.getClass().getName() + "]");
             return null;
         }
     }
 
     /**
      * Test deactivating an UserAccount.
      * 
      * @param id
      *            The id of the user account.
      * @param xml
      *            The task param xml.
      * @throws Exception
      *             If anything fails.
      */
     protected void deactivateUserAccount(final String id, final String xml)
         throws Exception {
 
         Object result = getUserAccountClient().deactivate(id, xml);
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
             httpRes.getEntity().consumeContent();
             assertHttpStatusOfMethod("", httpRes);
         }
     }
 
     /**
      * @return Returns the testUploadFile.
      */
     protected String getUploadFile() {
         return testUploadFile;
     }
 
     /**
      * @return Returns the testUploadFileMimeType.
      */
     protected String getUploadFileMimeType() {
         return testUploadFileMimeType;
     }
 
     /**
      * Prepares an item for a test.<br>
      * The item is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status to set for the item. If this is <code>null</code>,
      *            no item is created and <code>null</code> is returned.
      * @param contextId
      *         context to create container in
      * @param createVersionsBefore
      *            If this flag is set to <code>true</code>, before each
      *            status change, the item is updated to create a new version.
      * @param createVersionsAfter
      *            If this flag is set to <code>true</code>, after each status
      *            change, the item is updated to create a new version, if this
      *            is allowed. Currently, this is not allowed for objects in
      *            state release or withdrawn.
      * @return Returns the XML representation of the created item. In case of
      *         withdrawn item, the released item is returned.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareItem(
         final String creatorUserHandle, final String status,
         final String contextId,
         final boolean createVersionsBefore, final boolean createVersionsAfter)
         throws Exception {
 
         try {
             if (status == null) {
                 return null;
             }
             PWCallback.setHandle(creatorUserHandle);
             String createdXml = null;
             try {
                 createdXml = create(ITEM_HANDLER_CODE, prepareItemData(contextId));
             }
             catch (Exception e) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             assertNotNull(createdXml);
 
             if (!STATUS_PENDING.equals(status)) {
                 Document document =
                     EscidocRestSoapTestsBase.getDocument(createdXml);
                 final String objidValue = getObjidValue(document);
                 if (createVersionsBefore) {
                     createdXml =
                         createdXml.replaceAll("semiconductor surfaces",
                             "semiconductor surfaces u");
                     createdXml = update(ITEM_HANDLER_CODE, objidValue, createdXml);
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                 }
                 submit(ITEM_HANDLER_CODE, objidValue,
                     getTaskParam(getLastModificationDateValue(document)));
                 createdXml = retrieve(ITEM_HANDLER_CODE, objidValue);
                 if (createVersionsAfter) {
                     createdXml =
                         createdXml.replaceAll("semiconductor surfaces",
                             "semiconductor surfaces u");
                     createdXml = update(ITEM_HANDLER_CODE, objidValue, createdXml);
                 }
                 if (!STATUS_SUBMITTED.equals(status)) {
                     if (createVersionsBefore) {
                         createdXml =
                             createdXml.replaceAll("semiconductor surfaces",
                                 "semiconductor surfaces u");
                         createdXml =
                             update(ITEM_HANDLER_CODE, objidValue, createdXml);
                     }
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                     releaseWithPid(
                             ITEM_HANDLER_CODE, objidValue, creatorUserHandle);
                     createdXml = retrieve(ITEM_HANDLER_CODE, objidValue);
                     if (!STATUS_RELEASED.equals(status)) {
                         if (createVersionsBefore) {
                             createdXml =
                                 createdXml.replaceAll("semiconductor surfaces",
                                     "semiconductor surfaces u");
                             createdXml =
                                 update(ITEM_HANDLER_CODE, objidValue, createdXml);
                         }
                         if (STATUS_WITHDRAWN.equals(status)) {
                             document = 
                                 EscidocRestSoapTestsBase.getDocument(createdXml);
                             final String taskParam =
                                 getWithdrawTaskParam(
                                     getLastModificationDateValue(document),
                                     "Some withdraw comment");
                             withdraw(ITEM_HANDLER_CODE, getObjidValue(document),
                                 taskParam);
                             createdXml = retrieve(ITEM_HANDLER_CODE, objidValue);
                         }
                     }
                 }
             }
 
             return createdXml;
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Prepares a container for a test.<br>
      * The container is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status to set for the item. If this is <code>null</code>,
      *            no item is created and <code>null</code> is returned.
      * @param contextId
      *            id of context to create container in.
      * @param createVersionsBefore
      *            If this flag is set to <code>true</code>, before each
      *            status change, the item is updated to create a new version.
      * @param createVersionsAfter
      *            If this flag is set to <code>true</code>, after each status
      *            change, the item is updated to create a new version, if this
      *            is allowed. Currently, this is not allowed for objects in
      *            state release or withdrawn.
      * @return Returns the XML representation of the created container. In case
      *         of withdrawn container, the released container is returned.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareContainer(
         final String creatorUserHandle, final String status,
         final String contextId,
         final boolean createVersionsBefore, final boolean createVersionsAfter)
         throws Exception {
 
         try {
             if (status == null) {
                 return null;
             }
 
             PWCallback.setHandle(creatorUserHandle);
             String createdXml = null;
             try {
                 createdXml = create(CONTAINER_HANDLER_CODE, 
                                 prepareContainerData(contextId));
             }
             catch (Exception e) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             assertNotNull(createdXml);
 
             if (!STATUS_PENDING.equals(status)) {
                 Document document =
                     EscidocRestSoapTestsBase.getDocument(createdXml);
                 final String objidValue = getObjidValue(document);
                 if (createVersionsBefore) {
                     createdXml =
                         createdXml.replaceAll("the title", "the title - updated");
                     createdXml =
                         update(CONTAINER_HANDLER_CODE, objidValue, createdXml);
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                 }
                 submit(CONTAINER_HANDLER_CODE, objidValue,
                     getTaskParam(getLastModificationDateValue(document)));
                 createdXml = retrieve(CONTAINER_HANDLER_CODE, objidValue);
                 if (createVersionsAfter) {
                     createdXml =
                         createdXml.replaceAll("the title", "the title - updated");
                     createdXml =
                         update(CONTAINER_HANDLER_CODE, objidValue, createdXml);
                 }
                 if (!STATUS_SUBMITTED.equals(status)) {
                     if (createVersionsBefore) {
                         createdXml =
                             createdXml.replaceAll("the title",
                                 "the title - updated");
                         createdXml =
                             update(CONTAINER_HANDLER_CODE, objidValue, createdXml);
                     }
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                     releaseWithPid(CONTAINER_HANDLER_CODE, objidValue,
                         creatorUserHandle);
                     createdXml = retrieve(CONTAINER_HANDLER_CODE, objidValue);
                     if (!STATUS_RELEASED.equals(status)) {
 
                         // we need to give the indexer some time, otherwise
                         // the indexer will fail to index the data of the released
                         // item.
                         // Thread.sleep(240000);
 
                         if (createVersionsBefore) {
                             createdXml =
                                 createdXml.replaceAll("the title",
                                     "the title - updated");
                             createdXml =
                                 update(CONTAINER_HANDLER_CODE, objidValue,
                                     createdXml);
                         }
                         document = 
                             EscidocRestSoapTestsBase.getDocument(createdXml);
                         final String taskParam =
                             getWithdrawTaskParam(
                                 getLastModificationDateValue(document),
                                 "Some withdraw comment");
                         withdraw(CONTAINER_HANDLER_CODE, getObjidValue(document),
                             taskParam);
                         createdXml = retrieve(CONTAINER_HANDLER_CODE, objidValue);
                     }
                 }
             }
 
             return createdXml;
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Prepares an organizational unit for a test.<br>
      * The organizational unit is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status to set for the item. If this is <code>null</code>,
      *            no item is created and <code>null</code> is returned.
      * @param parentIds
      *            parentIds of this orgUnit
      * @return Returns the XML representation of the created organizational
      *         unit.
      * @throws Exception
      *             If anything fails.
      */
     public String prepareOrgUnit(
         final String creatorUserHandle, final String status,
         final String[] parentIds) throws Exception {
 
         try {
             if (status == null) {
                 return null;
             }
 
             PWCallback.setHandle(creatorUserHandle);
             String createdXml = null;
             try {
                 createdXml =
                     create(ORGANIZATIONAL_UNIT_HANDLER_CODE,
                         prepareOrgUnitData(parentIds));
             }
             catch (Exception e) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             assertNotNull(createdXml);
 
             if (!STATUS_NEW.equals(status)) {
                 Document document =
                     EscidocRestSoapTestsBase.getDocument(createdXml);
                 final String objidValue = getObjidValue(document);
                 getOrganizationalUnitClient().open(objidValue,
                     orgUnitHelper.getTheLastModificationParam(false, objidValue));
                 createdXml = retrieve(
                         ORGANIZATIONAL_UNIT_HANDLER_CODE, objidValue);
                 if (!STATUS_OPEN.equals(status)) {
                     getOrganizationalUnitClient().close(
                         objidValue,
                         orgUnitHelper
                             .getTheLastModificationParam(false, objidValue));
                     createdXml =
                         retrieve(ORGANIZATIONAL_UNIT_HANDLER_CODE, objidValue);
                 }
             }
 
             return createdXml;
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Prepares an user-account for a test.<br>
      * The user-account is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status to set for the user-account.
      * @return Returns the XML representation of the created item.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareUserAccount(
         final String creatorUserHandle, final String status) throws Exception {
 
         try {
             PWCallback.setHandle(creatorUserHandle);
             String createdXml = null;
             try {
                 createdXml =
                     create(USER_ACCOUNT_HANDLER_CODE, prepareUserAccountData());
             }
             catch (Exception e) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             assertNotNull(createdXml);
 
             if (!STATUS_ACTIVE.equals(status)) {
                 Document document =
                     EscidocRestSoapTestsBase.getDocument(createdXml);
                 String objidValue = getObjidValue(document);
                 deactivateUserAccount(getObjidValue(document),
                     getTaskParam(getLastModificationDateValue(document)));
                 createdXml = retrieve(USER_ACCOUNT_HANDLER_CODE, objidValue);
             }
 
             return createdXml;
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Prepares an user-group for a test.<br>
      * The user-group is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @return Returns the XML representation of the created user-group.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareUserGroup(
         final String creatorUserHandle) throws Exception {
 
         PWCallback.setHandle(creatorUserHandle);
         String createdXml = null;
         try {
             createdXml =
                 create(USER_GROUP_HANDLER_CODE, prepareUserGroupData());
         }
         catch (Exception e) {
             EscidocRestSoapTestsBase.failException(e);
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         assertNotNull(createdXml);
 
         return createdXml;
     }
 
     /**
      * Prepares a content-relation for a test.<br>
      * The content-relation is created and set into the specified state.
      * 
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status to set for the item. If this is <code>null</code>,
      *            no item is created and <code>null</code> is returned.
      * @param subjectHref
      *            href to the subject of the content-relation.
      * @param objectHref
      *            href to the object of the content-relation.
      * @param createVersionsBefore
      *            If this flag is set to <code>true</code>, before each
      *            status change, the content-relation is updated to create a new version.
      * @param createVersionsAfter
      *            If this flag is set to <code>true</code>, after each status
      *            change, the content-relation is updated to create a new version, if this
      *            is allowed. Currently, this is not allowed for objects in
      *            state released or withdrawn.
      * @return Returns the XML representation of the created content-relation. In case of
      *         withdrawn content-relation, the released content-relation is returned.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareContentRelation(
         final String creatorUserHandle, 
         final String status, 
         final String subjectHref, 
         final String objectHref,
         final boolean createVersionsBefore, 
         final boolean createVersionsAfter)
         throws Exception {
 
         try {
             if (status == null) {
                 return null;
             }
             PWCallback.setHandle(creatorUserHandle);
             String createdXml = null;
             try {
                 createdXml = handleResult(
                 contentRelationClient.create(
                         prepareContentRelationData(subjectHref, objectHref)));
             }
             catch (Exception e) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             assertNotNull(createdXml);
 
             if (!STATUS_PENDING.equals(status)) {
                 Document document =
                     EscidocRestSoapTestsBase.getDocument(createdXml);
                 final String objidValue = getObjidValue(document);
                 if (createVersionsBefore) {
                     createdXml =
                         createdXml.replaceAll("Demo content relation",
                             "Demo content relation u");
                     createdXml = handleResult(
                     contentRelationClient.update(objidValue,
                             prepareContentRelationData(subjectHref, objectHref)));
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                 }
                contentRelationClient.submit(objidValue,
                        getTaskParam(getLastModificationDateValue(document)));
                createdXml = handleResult(
                contentRelationClient.retrieve(objidValue));
                 if (createVersionsAfter) {
                     createdXml =
                         createdXml.replaceAll("Demo content relation",
                             "Demo content relation u");
                     createdXml = update(ITEM_HANDLER_CODE, objidValue, createdXml);
                 }
                 if (!STATUS_SUBMITTED.equals(status)) {
                     if (createVersionsBefore) {
                         createdXml =
                             createdXml.replaceAll("Demo content relation",
                                 "Demo content relation u");
                         createdXml = handleResult(contentRelationClient.update(
                                                     objidValue, createdXml));
                     }
                     document = EscidocRestSoapTestsBase.getDocument(createdXml);
                     contentRelationClient.release(objidValue, 
                             getTaskParam(getLastModificationDateValue(document)));
                     createdXml = handleResult(
                             contentRelationClient.retrieve(objidValue));
                 }
             }
 
             return createdXml;
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Prepares the data for a container.
      * 
      * @param contextId
      *         context to create container in
      * @return Returns the xml representation of a container. Depending on the
      *         current set transport of the class, the data is created by either
      *         using the template file
      *         create_container_WithoutMembers_Restv1.1.xml or by using the
      *         template file create_container_WithoutMembers_Soapv1.1.xml
      * @throws Exception
      *             If anything fails.
      */
     private String prepareContainerData(final String contextId) throws Exception {
 
         Document xmlContainer =
             EscidocRestSoapTestsBase.getTemplateAsDocument(
                 TEMPLATE_CONTAINER_PATH + "/" + getTransport(false),
                 "create_container_WithoutMembers_v1.1.xml");
         if (contextId != null && !contextId.equals("")) {
             if (getTransport() == Constants.TRANSPORT_REST) {
                 String contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
                 substitute(xmlContainer, 
                     "/container/properties/context/@href", contextHref);
             } else {
                 substitute(xmlContainer, 
                     "/container/properties/context/@objid", contextId);
             }
         }
         // deleteElement(xmlContainer, "/container/admin-descriptor");
         return toString(xmlContainer, false);
     }
 
     /**
      * Prepares the data for an item.
      * 
      * @param contextId
      *         context to create container in
      * @return Returns the xml representation of an item. Depending on the
      *         current set transport of the class, the data is created by either
      *         using the template file escidoc_item_198_for_createREST.xml or by
      *         using the template file escidoc_item_198_for_createSOAP.xml
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareItemData(final String contextId) throws Exception {
 
         final String templateName;
         if (getTransport() == Constants.TRANSPORT_REST) {
             templateName =
                 "escidoc_item_198_for_create_one_component_privateREST.xml";
         }
         else {
             templateName =
                 "escidoc_item_198_for_create_one_component_privateSOAP.xml";
         }
         Document itemDoc = EscidocRestSoapTestsBase.getTemplateAsDocument(
             TEMPLATE_AA_ITEM_PATH, templateName);
         if (contextId != null && !contextId.equals("")) {
             if (getTransport() == Constants.TRANSPORT_REST) {
                 String contextHref = Constants.CONTEXT_BASE_URI + "/" + contextId;
                 substitute(itemDoc, 
                     "/item/properties/context/@href", contextHref);
             } else {
                 substitute(itemDoc, 
                     "/item/properties/context/@objid", contextId);
             }
         }
         return toString(itemDoc, false);
     }
 
     /**
      * Prepares the data for an item.
      * 
      * @param subjectHref
      *         subjectHref for contentRelation
      * @param objectHref
      *         objectHref for contentRelation
      * @return Returns the xml representation of a contentRelation.
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareContentRelationData(
             final String subjectHref, final String objectHref) throws Exception {
 
         final String contentRelationXml = 
             getExampleTemplate("content-relation-01.xml");
         Document contentRelationDoc = 
             EscidocRestSoapTestsBase.getDocument(contentRelationXml);
         if (getTransport() == Constants.TRANSPORT_REST) {
             if (subjectHref != null) {
                 substitute(contentRelationDoc, 
                         "/content-relation/subject/@href", subjectHref);
             }
             if (objectHref != null) {
                 substitute(contentRelationDoc, 
                         "/content-relation/object/@href", objectHref);
             }
         } else {
             if (subjectHref != null) {
                 substitute(contentRelationDoc, 
                         "/content-relation/subject/@objid", 
                         subjectHref.replaceFirst(".*/(.*)", "$1"));
             }
             if (objectHref != null) {
                 substitute(contentRelationDoc, 
                         "/content-relation/object/@objid", 
                         objectHref.replaceFirst(".*/(.*)", "$1"));
             }
         }
         return toString(contentRelationDoc, false);
     }
 
     /**
      * Prepares the data for an user-account.
      * 
      * @return Returns the xml representation of an user-account. The data is
      *         created by using the template file
      *         escidoc_useraccount_for_create.xml
      * @throws Exception
      *             If anything fails.
      */
     private String prepareUserAccountData() throws Exception {
 
         
         Document document =
             EscidocRestSoapTestsBase
             .getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH,
                     "escidoc_useraccount_for_create.xml");
         assertXmlExists("No login-name found in template data. ", document,
             XPATH_USER_ACCOUNT_LOGINNAME);
         final Node loginNameNode =
             selectSingleNode(document, XPATH_USER_ACCOUNT_LOGINNAME);
         String loginname = loginNameNode.getTextContent().trim();
         loginname += System.currentTimeMillis();
         loginNameNode.setTextContent(loginname);
         String userAccountXml = toString(document, true);
         return userAccountXml;
     }
 
     /**
      * Prepares the data for an user-group.
      * 
      * @return Returns the xml representation of an user-group. The data is
      *         created by using the template file
      *         escidoc_usergroup_for_create.xml
      * @throws Exception
      *             If anything fails.
      */
     private String prepareUserGroupData() throws Exception {
 
         Document document =
             getTemplateAsFixedUserGroupDocument(TEMPLATE_USER_GROUP_PATH,
                 "escidoc_usergroup_for_create.xml");
         insertUniqueLabel(document);
 
         return toString(document, false);
     }
 
     /**
      * Prepares the data for an organizational unit.
      * 
      * @return Returns the xml representation of an organizational unit.
      * @param parentIds
      *            parentIds of this orgUnit
      * @throws Exception
      *             If anything fails.
      */
     protected String prepareOrgUnitData(final String[] parentIds)
         throws Exception {
 
         final String templateName = "escidoc_ou_create.xml";
         final Document toBeCreatedDocument =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         setUniqueValue(toBeCreatedDocument, XPATH_ORGANIZATIONAL_UNIT_TITLE);
 
         if (parentIds != null && parentIds.length > 0) {
             final int length = parentIds.length;
             final String[] parentValues = new String[length * 2];
             for (int i = 0; i < length; i++) {
                 parentValues[i] = parentIds[i];
                 parentValues[i + length] = null;
             }
             orgUnitHelper.insertParentsElement(toBeCreatedDocument,
                 XPATH_ORGANIZATIONAL_UNIT_MD_RECORDS, parentValues, false);
         }
         final String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         return toBeCreatedXml;
     }
 
     /**
      * Inserts a unique label into the provided document by adding the
      * current timestamp to the contained label.
      * 
      * @param document
      *            The document.
      * @return The inserted login name.
      * @throws Exception
      *             If anything fails.
      */
     protected String insertUniqueLabel(final Document document)
         throws Exception {
 
         assertXmlExists("No label found in template data. ", document,
             "/user-group/properties/label");
         final Node labelNode =
             selectSingleNode(document, "/user-group/properties/label");
         String label = labelNode.getTextContent().trim();
         label += System.currentTimeMillis();
 
         labelNode.setTextContent(label);
 
         return label;
     }
 
     /**
      * Tests retrieving an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, no
      *            item is created and it is tried to retrieve an unknown item.
      * @param createVersions
      *            If this flag is set to <code>true</code>, before each
      *            status change, the item is updated to create a new version.
      * @param versionNumber
      *            The version that shall be retrieved. If this is
      *            <code>null</code>, only the object id is sent to the
      *            retrieve service. Otherwise, objectid:versionNumber is sent as
      *            the identifier of the resource.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String item xml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestRetrieveItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final boolean createVersions,
         final String versionNumber, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml =
             prepareItem(
                 creatorUserHandle, status, null, createVersions, false);
 
         String retrievedXml = null;
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 retrievedXml =
                     retrieve(ITEM_HANDLER_CODE, createResourceId(
                         getObjidValue(EscidocRestSoapTestsBase
                             .getDocument(createdXml)), versionNumber));
             }
             else {
                 retrievedXml = retrieve(ITEM_HANDLER_CODE, UNKNOWN_ID);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         return retrievedXml;
     }
 
     /**
      * Tests retrieving a content of an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, no
      *            item is created and it is tried to retrieve an unknown item.
      * @param createVersionsBefore
      *            If this flag is set to <code>true</code>, before each
      *            status change, the item is updated to create a new version.
      * @param createVersionsAfter
      *            If this flag is set to <code>true</code>, after each status
      *            change, the item is updated to create a new version, if this
      *            is allowed. Currently, this is not allowed for objects in
      *            state release or withdrawn.
      * @param versionNumber
      *            The version that shall be retrieved. If this is
      *            <code>null</code>, only the object id is sent to the
      *            retrieve service. Otherwise, objectid:versionNumber is sent as
      *            the identifier of the resource.
      * @param visibility
      *            the visibility of the component that shall be retrieved.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveContent(
         final String userHandle, final String creatorUserHandle,
         final String status, final boolean createVersionsBefore,
         final boolean createVersionsAfter, final String versionNumber,
         final String visibility, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         final String createdXml =
             prepareItem(
                 creatorUserHandle, status, null, createVersionsBefore,
                 createVersionsAfter);
 
         final Document createdDocument =
             EscidocRestSoapTestsBase.getDocument(createdXml);
         final String componentId =
             extractComponentId(createdDocument, visibility);
 
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 String resourceId = 
                     createResourceId(getObjidValue(EscidocRestSoapTestsBase
                     .getDocument(createdXml)), versionNumber);
                 SimpleDateFormat dateFormat =
                     new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
                 dateFormat.format(new Date(System.currentTimeMillis()));
                 System.out.println(
                     "MIH: " 
                     + dateFormat.format(new Date(System.currentTimeMillis())) 
                     + " getting content of component:" 
                     + componentId + ", item:" + resourceId);
                 ((ItemClient) getClient(ITEM_HANDLER_CODE)).retrieveContent(
                     resourceId, componentId);
             }
             else {
                 ((ItemClient) getClient(ITEM_HANDLER_CODE)).retrieveContent(
                     UNKNOWN_ID, UNKNOWN_ID);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a content of an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param itemId
      *            the itemId
      * @param componentId
      *            the componentId to retrieve
      * @param versionNumber
      *            The version that shall be retrieved. If this is
      *            <code>null</code>, only the object id is sent to the
      *            retrieve service. Otherwise, objectid:versionNumber is sent as
      *            the identifier of the resource.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveContent(
         final String userHandle, final String itemId, final String componentId,
         final String versionNumber, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             String itemWithVersion = itemId;
             if (versionNumber != null) {
                 itemWithVersion += ":" + versionNumber;
             }
             ((ItemClient) getClient(ITEM_HANDLER_CODE)).retrieveContent(
                 itemWithVersion, componentId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a container.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the container. If this is <code>null</code>,
      *            no container is created and it is tried to retrieve an unknown
      *            container.
      * @param createVersions
      *            If this flag is set to <code>true</code>, before each
      *            status change, the container is updated to create a new
      *            version.
      * @param versionNumber
      *            The version that shall be retrieved. If this is
      *            <code>null</code>, only the object id is sent to the
      *            retrieve service. Otherwise, objectid:versionNumber is sent as
      *            the identifier of the resource.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String container xml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestRetrieveContainer(
         final String userHandle, final String creatorUserHandle,
         final String status, final boolean createVersions,
         final String versionNumber, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml =
             prepareContainer(
                 creatorUserHandle, status, null, createVersions, false);
 
         String retrievedXml = null;
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 retrievedXml =
                     retrieve(CONTAINER_HANDLER_CODE, createResourceId(
                         getObjidValue(EscidocRestSoapTestsBase
                             .getDocument(createdXml)), versionNumber));
             }
             else {
                 retrievedXml = retrieve(CONTAINER_HANDLER_CODE, UNKNOWN_ID);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         return retrievedXml;
     }
 
     /**
      * Tests creating a container.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String containerXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateContainer(
         final String userHandle, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String toBeCreatedXml = prepareContainerData(null);
 
         String xml = null;
         try {
             PWCallback.setHandle(userHandle);
             xml = create(CONTAINER_HANDLER_CODE, toBeCreatedXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return xml;
     }
 
     /**
      * Tests creating an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateItem(
         final String userHandle, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String itemWithoutComponents = prepareItemData(null);
 
         return doTestCreateItem(userHandle, expectedExceptionClass,
             itemWithoutComponents);
     }
 
     /**
      * Tests creating an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @param templatePath
      *            The path to the directory containing the template.
      * @param templateName
      *            The name of the template to use.
      * @return Returns the xml representation of the created item.
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateItem(
         final String userHandle, final Class< ? > expectedExceptionClass,
         final String templatePath, final String templateName) throws Exception {
 
         final String itemWithoutComponents =
             EscidocRestSoapTestsBase.getTemplateAsString(templatePath,
                 templateName);
 
         return doTestCreateItem(userHandle, expectedExceptionClass,
             itemWithoutComponents);
     }
 
     /**
      * Tests creating an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @param xml
      *            The xml representation.
      * @return Returns the xml representation of the created item.
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateItem(
         final String userHandle, final Class< ? > expectedExceptionClass,
         final String xml) throws Exception {
 
         String createdXml = null;
         try {
             PWCallback.setHandle(userHandle);
             createdXml = create(ITEM_HANDLER_CODE, xml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return createdXml;
     }
 
     /**
      * Creates the resource id for the provided object id and version number.
      * 
      * @param objid
      *            The object id.
      * @param versionNumber
      *            The version number. this may be <code>null</code>. In this
      *            case, the objid is returned, only.
      * @return Returns objid[:versionNumber]
      */
     protected String createResourceId(
         final String objid, final String versionNumber) {
 
         if (versionNumber == null) {
             return objid;
         }
         else {
             return objid + ":" + versionNumber;
         }
     }
 
     /**
      * Tests updating an item.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, it is
      *            tried to update an unknown item.
      * @param versionNumber
      *            TODO
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestUpdateItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final String versionNumber,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         String createdXml;
         if (status != null) {
             createdXml =
                 prepareItem(creatorUserHandle, status, null, false, false);
         }
         else {
             createdXml =
                 prepareItem(creatorUserHandle, STATUS_PENDING, null, false,
                     false);
         }
 
         String toBeUpdatedXml =
             createdXml.replaceAll("semiconductor surfaces",
                 "semiconductor surfaces u");
 
         String updatedXml = null;
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 updatedXml =
                     update(ITEM_HANDLER_CODE, createResourceId(
                         getObjidValue(EscidocRestSoapTestsBase
                             .getDocument(toBeUpdatedXml)), versionNumber),
                         toBeUpdatedXml);
             }
             else {
                 updatedXml =
                     update(ITEM_HANDLER_CODE, UNKNOWN_ID, toBeUpdatedXml);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         return updatedXml;
     }
 
     /**
      * Tests deleting an item.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, no
      *            item is created and it is tried to delete an unknown item.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         final String createdXml =
             prepareItem(creatorUserHandle, status, null, false, false);
 
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 delete(ITEM_HANDLER_CODE,
                     getObjidValue(EscidocRestSoapTestsBase
                         .getDocument(createdXml)));
             }
             else {
                 delete(ITEM_HANDLER_CODE, UNKNOWN_ID);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests submitting an item.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, it is
      *            tried to submit an unknown item.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestSubmitItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (status != null) {
             createdXml =
                 prepareItem(creatorUserHandle, status, null, false, false);
         }
         else {
             createdXml =
                 prepareItem(creatorUserHandle, STATUS_PENDING, null, false,
                     false);
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 submit(ITEM_HANDLER_CODE, getObjidValue(document),
                     getTaskParam(getLastModificationDateValue(document)));
             }
             else {
                 submit(ITEM_HANDLER_CODE, UNKNOWN_ID,
                     getTaskParam(getLastModificationDateValue(document)));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests releasing an item.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, it is
      *            tried to release an unknown item.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestReleaseItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml =
             prepareItem(creatorUserHandle, status, null, false, false);
         if (createdXml == null) {
             createdXml =
                 prepareItem(
                     creatorUserHandle, STATUS_SUBMITTED, null, false,
                     false);
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 releaseWithPid(ITEM_HANDLER_CODE, getObjidValue(document),
                     userHandle);
             }
             else {
                 release(ITEM_HANDLER_CODE, UNKNOWN_ID,
                     getTaskParam(getLastModificationDateValue(document)));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests withdrawing an item.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, it is
      *            tried to withdraw an unknown item.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestWithdrawItem(
         final String userHandle, final String creatorUserHandle,
         final String status, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (status != null) {
             createdXml =
                 prepareItem(creatorUserHandle, status, null, false, false);
         }
         else {
             createdXml =
                 prepareItem(creatorUserHandle, STATUS_RELEASED, null, false,
                     false);
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 withdraw(ITEM_HANDLER_CODE, getObjidValue(document),
                     getWithdrawTaskParam(
                         getLastModificationDateValue(document),
                         "Some withdraw comment"));
             }
             else {
                 withdraw(ITEM_HANDLER_CODE, UNKNOWN_ID, getWithdrawTaskParam(
                     getLastModificationDateValue(document),
                     "Some withdraw comment"));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests creating a content-relation.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return Returns the xml representation of the created content-relation.
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateContentRelation(
         final String userHandle, 
         final Class< ? > expectedExceptionClass) throws Exception {
 
         String createdXml = null;
         try {
             PWCallback.setHandle(userHandle);
             createdXml = handleResult(contentRelationClient.create(
                     prepareContentRelationData(null, null)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return createdXml;
     }
 
     /**
      * Tests updating a content-relation.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the item. If this is <code>null</code>, it is
      *            tried to update an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param versionNumber
      *            TODO
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return updated content-relation xml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestUpdateContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final String versionNumber,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             if (status != null) {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, status, null, null,  false, false);
             }
             else {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, STATUS_PENDING, null, null, false,
                         false);
             }
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
 
         String toBeUpdatedXml =
             createdXml.replaceAll("Demo content relation",
                 "Demo content relation u");
 
         String updatedXml = null;
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 updatedXml = handleResult(
                         contentRelationClient.update(createResourceId(
                         getObjidValue(EscidocRestSoapTestsBase
                                 .getDocument(toBeUpdatedXml)), versionNumber), 
                                 toBeUpdatedXml));
             }
             else {
                 updatedXml = handleResult(
                         contentRelationClient.update(UNKNOWN_ID, 
                                 toBeUpdatedXml));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         return updatedXml;
     }
 
     /**
      * Tests deleting a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, no
      *            content-relation is created and it is tried to delete an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             createdXml =
                 prepareContentRelation(
                         creatorUserHandle, status, null, null, false, false);
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
 
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 contentRelationClient.delete(
                         getObjidValue(EscidocRestSoapTestsBase
                         .getDocument(createdXml)));
             }
             else {
                 contentRelationClient.delete(UNKNOWN_ID);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a content-relation.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param creatorUserHandle
      *            The escidoc user handle of the creator.
      * @param status
      *            The status of the content-relation. 
      *            If this is <code>null</code>, no
      *            content-relation is created 
      *            and it is tried to retrieve an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param createVersions
      *            If this flag is set to <code>true</code>, before each
      *            status change, the content-relation is updated to create a new version.
      * @param versionNumber
      *            The version that shall be retrieved. If this is
      *            <code>null</code>, only the object id is sent to the
      *            retrieve service. Otherwise, objectid:versionNumber is sent as
      *            the identifier of the resource.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String content-relation xml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestRetrieveContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final boolean createVersions,
         final String versionNumber, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             createdXml =
                 prepareContentRelation(
                     creatorUserHandle, status, null, null, createVersions, false);
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
 
         String retrievedXml = null;
         try {
             PWCallback.setHandle(userHandle);
             if (createdXml != null) {
                 retrievedXml = handleResult(contentRelationClient.retrieve(
                         createResourceId(
                         getObjidValue(EscidocRestSoapTestsBase
                                 .getDocument(createdXml)), versionNumber)));
             }
             else {
                 retrievedXml = handleResult(contentRelationClient.retrieve(
                         UNKNOWN_ID));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         return retrievedXml;
     }
 
     /**
      * Tests submitting a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestSubmitContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             createdXml =
                 prepareContentRelation(
                         creatorUserHandle, STATUS_PENDING, null, null, false,
                     false);
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             contentRelationClient.submit(getObjidValue(document), 
                     getTaskParam(getLastModificationDateValue(document)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests releasing a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, it is
      *            tried to release an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestReleaseContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             createdXml =
                 prepareContentRelation(
                         creatorUserHandle, STATUS_SUBMITTED, null, null, false, false);
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             contentRelationClient.release(getObjidValue(document), 
                     getTaskParam(getLastModificationDateValue(document)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests withdrawing a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, it is
      *            tried to withdraw an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestReviseContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             createdXml =
                 prepareContentRelation(
                         creatorUserHandle, STATUS_SUBMITTED, null, null, false,
                     false);
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             contentRelationClient.revise(getObjidValue(document),
                     getWithdrawTaskParam(
                         getLastModificationDateValue(document),
                         "Some withdraw comment"));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests locking a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, it is
      *            tried to lock an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestLockContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             if (status != null) {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, status, null, null, false, false);
             }
             else {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, STATUS_SUBMITTED, null, null, false,
                         false);
             }
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         final Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
 
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 contentRelationClient.lock(getObjidValue(document),
                         getTaskParam(getLastModificationDateValue(document)));
             }
             else {
                 contentRelationClient.lock(UNKNOWN_ID,
                         getTaskParam(getLastModificationDateValue(document)));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests unlocking a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, it is
      *            tried to unlock an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUnlockContentRelation(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             if (status != null) {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, status, null, null, false, false);
             }
             else {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, STATUS_SUBMITTED, null, null, false,
                         false);
             }
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
         String objId = getObjidValue(document);
         createdXml = handleResult(
                 contentRelationClient.lock(objId,
                         getTaskParam(getLastModificationDateValue(document))));
         document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
         
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 contentRelationClient.unlock(objId,
                         getTaskParam(getLastModificationDateValue(document)));
             }
             else {
                 contentRelationClient.unlock(UNKNOWN_ID,
                         getTaskParam(getLastModificationDateValue(document)));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests unlocking a content-relation.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the content-relation. If this is <code>null</code>, it is
      *            tried to assign objectPid to an unknown content-relation.
      * @param contentRelationId
      *            The contentRelationId 
      *            (if <code>null</code>, contentRelation will be created).
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestContentRelationAssignObjectPid(
         final String userHandle, final String creatorUserHandle,
         final String status, final String contentRelationId, 
         final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXml;
         if (contentRelationId == null) {
             if (status != null) {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, status, null, null, false, false);
             }
             else {
                 createdXml =
                     prepareContentRelation(
                             creatorUserHandle, STATUS_SUBMITTED, null, null, false,
                         false);
             }
         } else {
             createdXml = 
                 handleResult(contentRelationClient.retrieve(contentRelationId));
         }
         Document document =
             EscidocRestSoapTestsBase.getDocument(createdXml);
         
         try {
             PWCallback.setHandle(userHandle);
             if (status != null) {
                 contentRelationClient.assignObjectPid(getObjidValue(document),
                         getTaskParam(getLastModificationDateValue(document)));
             }
             else {
                 contentRelationClient.assignObjectPid(UNKNOWN_ID,
                         getTaskParam(getLastModificationDateValue(document)));
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Test creating a StagingFile.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestCreateStagingFile(
         final String userHandle, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         PWCallback.setHandle(userHandle);
         InputStream fileInputStream =
             StagingFileTestBase.getFileInputStream(getUploadFile());
 
         try {
             createStagingFile(fileInputStream, getUploadFileMimeType(),
                 getUploadFile());
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Test retrieving an UserAccount using the user id.
      * 
      * @param userHandle
      *            The eSciDoc user handle.
      * @param identifier
      *            The identifier addressing the user account to fetch.<br>
      *            If this parameter is not provided, a new user account will be
      *            created by the specified creator and set to the specified
      *            state. This account is the addressed as defined by the
      *            parameter identifierSelection.
      * @param identifierSelection
      *            This parameter specifies with what kind of identifier the
      *            account shall be addressed.<br>
      *            It is only used, if no user account identifier has been
      *            provided. Valid values are
      *            <ul>
      *            <li>byId</li>
      *            <li>byLoginName</li>
      *            <li>byHandle</li>.
      *            </ul>
      *            Note: Addressing by handle is currently not supported by this
      *            method.
      * @param creatorUserHandle
      *            The eSciDoc user handle of the creator.
      * @param status
      *            The status of the item.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveUserAccount(
         final String userHandle, final String identifier,
         final String identifierSelection, final String creatorUserHandle,
         final String status, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String accountIdentifier = identifier;
         if (accountIdentifier == null) {
             String createdXml = prepareUserAccount(creatorUserHandle, status);
             if ("byId".equals(identifierSelection)) {
                 accountIdentifier =
                     getObjidValue(EscidocRestSoapTestsBase
                         .getDocument(createdXml));
             }
             else if ("byLoginName".equals(identifierSelection)) {
                 accountIdentifier =
                     selectSingleNodeAsserted(
                         EscidocRestSoapTestsBase.getDocument(createdXml),
                         XPATH_USER_ACCOUNT_LOGINNAME).getTextContent();
             }
             else if ("byHandle".equals(identifierSelection)) {
                 // FIXME: implement
                 throw new IllegalArgumentException(
                     "byHandle currently not supported");
             }
             else {
                 throw new IllegalArgumentException(
                     "Unsupported account identifier [" + identifierSelection
                         + "]");
             }
         }
 
         try {
             PWCallback.setHandle(userHandle);
             retrieve(USER_ACCOUNT_HANDLER_CODE, accountIdentifier);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a content model.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param contentTypeId
      *            The id of the content type.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveContentModel(
         final String userHandle, final String contentTypeId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             retrieve(CONTENT_MODEL_HANDLER_CODE, contentTypeId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a scope.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param scopeId
      *            The id of the scope.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveScope(
         final String userHandle, final String scopeId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             retrieve(SCOPE_HANDLER_CODE, scopeId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a aggregation-definition.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param aggregationDefinitionId
      *            The id of the aggregation-definition.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveAggregationDefinition(
         final String userHandle, final String aggregationDefinitionId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             retrieve(AGGREGATION_DEFINITION_HANDLER_CODE,
                 aggregationDefinitionId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a report-definition.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param reportDefinitionId
      *            The id of the report-definition.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveReportDefinition(
         final String userHandle, final String reportDefinitionId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             retrieve(REPORT_DEFINITION_HANDLER_CODE, reportDefinitionId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a report.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param reportDefinitionId
      *            The id of the report-definition.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveReport(
         final String userHandle, final String templateName,
         final String reportDefinitionId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String reportParametersXml = 
             smTestBase.getTemplateAsFixedReportParametersString(
             TEMPLATE_REP_PARAMETERS_PATH, templateName);
 
         if (reportDefinitionId != null) {
             Document document =
                 EscidocRestSoapTestsBase.getDocument(reportParametersXml);
             substituteId(document, XPATH_REPORT_PARAMETERS_REPORT_DEFINITION,
                 reportDefinitionId);
             reportParametersXml = toString(document, false);
         }
         try {
             PWCallback.setHandle(userHandle);
             reportClient.retrieve(reportParametersXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests creating a scope.<br>
      * The scope is created using the template escidoc_scope3.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String scope-id
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateScope(
         final String userHandle, final String templateName,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         String objidValue = null;
         String scopeXml =
             smTestBase.getTemplateAsFixedScopeString(
                 TEMPLATE_SCOPE_PATH, templateName);
 
         try {
             PWCallback.setHandle(userHandle);
             String result = create(SCOPE_HANDLER_CODE, scopeXml);
             Document document = EscidocRestSoapTestsBase.getDocument(result);
             objidValue = getObjidValue(getTransport(), document);
 
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return objidValue;
     }
 
     /**
      * Tests creating a aggregation definition.<br>
      * The aggregation definition is created using the template
      * escidoc_aggregation_definition1.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param scopeId
      *            The scope-id the template has to get substituted with.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String aggregation-definition-id
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateAggregationDefinition(
         final String userHandle, final String templateName,
         final String scopeId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String objidValue = null;
         String aggregationDefinitionXml =
             smTestBase.getTemplateAsFixedAggregationDefinitionString(
                 TEMPLATE_AGG_DEF_PATH, templateName);
 
         if (scopeId != null) {
             Document document =
                 EscidocRestSoapTestsBase.getDocument(aggregationDefinitionXml);
             substituteId(document, XPATH_AGGREGATION_DEFINITION_SCOPE, scopeId);
             aggregationDefinitionXml = toString(document, false);
         }
 
         try {
             PWCallback.setHandle(userHandle);
             String result =
                 create(AGGREGATION_DEFINITION_HANDLER_CODE,
                     aggregationDefinitionXml);
             Document document = EscidocRestSoapTestsBase.getDocument(result);
             objidValue = getObjidValue(getTransport(), document);
 
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return objidValue;
     }
 
     /**
      * Tests creating a report definition.<br>
      * The report definition is created using the template
      * escidoc_report_definition1.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param scopeId
      *            The scope-id the template has to get substituted with.
      * @param aggregationDefinitionId
      *            The aggregation-definition-Id the template has to get substituted
      *            with.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String report-definition-id
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateReportDefinition(
         final String userHandle, final String templateName,
         final String scopeId, final String aggregationDefinitionId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         String idWithoutSpecialSigns = 
             aggregationDefinitionId.replaceAll("\\:", "");
         String objidValue = null;
         String reportDefinitionXml =
             smTestBase.getTemplateAsFixedReportDefinitionString(
                 TEMPLATE_REP_DEF_PATH, templateName);
 
         if (scopeId != null) {
             Document document =
                 EscidocRestSoapTestsBase.getDocument(reportDefinitionXml);
             substituteId(document, XPATH_REPORT_DEFINITION_SCOPE, scopeId);
             reportDefinitionXml = toString(document, false);
             reportDefinitionXml =
                 reportDefinitionXml.replaceFirst("(?s)<sql>.*?</sql>",
                     "<sql>select month from _" + idWithoutSpecialSigns
                         + "_page_statistics;</sql>");
         }
 
         try {
             PWCallback.setHandle(userHandle);
             String result =
                 create(REPORT_DEFINITION_HANDLER_CODE, reportDefinitionXml);
             Document document = EscidocRestSoapTestsBase.getDocument(result);
             objidValue = getObjidValue(getTransport(), document);
 
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return objidValue;
     }
 
     /**
      * Tests creating a statisticdata-record.<br>
      * The statistic-data is created using the template
      * escidoc_statistic_data1.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param scopeId
      *            The scope-id the template has to get substituted with.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestCreateStatisticData(
         final String userHandle, final String templateName,
         final String scopeId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String statisticDataXml =
             toString(EscidocRestSoapTestsBase.getTemplateAsDocument(
                 TEMPLATE_STAT_DATA_PATH, templateName), false);
 
         if (scopeId != null) {
             Document document =
                 EscidocRestSoapTestsBase.getDocument(statisticDataXml);
             substitute(document, XPATH_STATISTIC_DATA_SCOPE_OBJID, scopeId);
             statisticDataXml = toString(document, false);
         }
 
         try {
             PWCallback.setHandle(userHandle);
             create(STATISTIC_DATA_HANDLER_CODE, statisticDataXml);
 
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests updating a scope.<br>
      * The scope is updated using the template escidoc_scope2.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param scopeId
      *            The scope-id the template has to get substituted with.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateScope(
         final String userHandle, final String templateName,
         final String scopeId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String scopeXml =
             smTestBase.getTemplateAsFixedScopeString(
                 TEMPLATE_SCOPE_PATH, templateName);
 
         if (scopeId != null) {
             Document document = EscidocRestSoapTestsBase.getDocument(scopeXml);
             substituteId(document, XPATH_SCOPE, scopeId);
             scopeXml = toString(document, false);
         }
 
         try {
             PWCallback.setHandle(userHandle);
             update(SCOPE_HANDLER_CODE, scopeId, scopeXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests updating a report-definition.<br>
      * The report-definition is updated using the template
      * escidoc_report_definition2.xml
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            The name of the template to use.
      * @param reportDefinitionId
      *            The reportDefinitionId the template has to get substituted
      *            with.
      * @param aggregationDefinitionId
      *            The aggregationDefinitionId the template has to get
      *            substituted with.
      * @param scopeId
      *            The scope-id the template has to get substituted with.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateReportDefinition(
         final String userHandle, final String templateName,
         final String reportDefinitionId, final String aggregationDefinitionId,
         final String scopeId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String idWithoutSpecialSigns = 
             aggregationDefinitionId.replaceAll("\\:", "");
         String reportDefinitionXml =
             smTestBase.getTemplateAsFixedReportDefinitionString(
                 TEMPLATE_REP_DEF_PATH, templateName);
 
         if (scopeId != null) {
             Document document =
                 EscidocRestSoapTestsBase.getDocument(reportDefinitionXml);
             substituteId(document, XPATH_REPORT_DEFINITION_SCOPE, scopeId);
             substituteId(document, XPATH_REPORT_DEFINITION,
                 reportDefinitionId);
             reportDefinitionXml = toString(document, false);
             reportDefinitionXml =
                 reportDefinitionXml.replaceFirst("(?s)select.*?;",
                     "select month from _" + idWithoutSpecialSigns
                         + "_page_statistics;");
         }
 
         try {
             PWCallback.setHandle(userHandle);
             update(REPORT_DEFINITION_HANDLER_CODE, reportDefinitionId,
                 reportDefinitionXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests deleting a scope.<br>
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param scopeId
      *            The scopeId to delete.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteScope(
         final String userHandle, final String scopeId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             delete(SCOPE_HANDLER_CODE, scopeId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests deleting a aggregation-definition.<br>
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param aggregationDefinitionId
      *            The aggregationDefinitionId to delete.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteAggregationDefinition(
         final String userHandle, final String aggregationDefinitionId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             delete(AGGREGATION_DEFINITION_HANDLER_CODE, aggregationDefinitionId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests deleting a report-definition.<br>
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param reportDefinitionId
      *            The reportDefinitionId to delete.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteReportDefinition(
         final String userHandle, final String reportDefinitionId,
         final Class< ? > expectedExceptionClass) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             delete(REPORT_DEFINITION_HANDLER_CODE, reportDefinitionId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a list of scopes.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedScope
      *            only these scopes may be in the list.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveScopeList(
         final String userHandle, final String expectedScope) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             String xml = handleResult(
                     scopeClient.retrieveScopes("<param><filter/></param>"));
             xml = xml.replaceAll("(?s).*?(<[^>]*?scope[\\s|>].*)", "$1");
             xml = xml.replaceAll("(?s)(.*)</[^>]*?scope[\\s|>].*", "$1");
             String[] parts = xml.split("<[^>]*?scope[^-]");
             Matcher scopeMatcher = SCOPE_PATTERN.matcher("");
             for (int i = 1; i < parts.length; i++) {
                 scopeMatcher.reset("<scope " + parts[i]);
                 if (scopeMatcher.matches()) {
                     if (!scopeMatcher.group(1).equals(expectedScope)) {
                         throw new Exception("Non expected Scope with id "
                             + scopeMatcher.group(i) + " found");
                     }
                 }
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a list of aggregation-definitions.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedScope
      *            only these scopes may be in the list.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveAggregationDefinitionList(
         final String userHandle, final String expectedScope) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             String xml =
                 handleResult(aggregationDefinitionClient
                     .retrieveAggregationDefinitions("<param><filter/></param>"));
             String[] parts = xml.split("<[^>]*?aggregation-definition[\\s|>].*?>");
             Matcher scopeMatcher = SCOPE_PATTERN.matcher("");
             for (int i = 1; i < parts.length; i++) {
                 scopeMatcher.reset(parts[i]);
                 if (scopeMatcher.matches()) {
                     if (!scopeMatcher.group(1).equals(expectedScope)) {
                         throw new Exception("Non expected Scope with id "
                             + scopeMatcher.group(i) + " found");
                     }
                 }
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving a list of report-definitions.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param expectedScope
      *            only these scopes may be in the list.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveReportDefinitionList(
         final String userHandle, final String expectedScope) throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
             String xml =
                 handleResult(reportDefinitionClient
                         .retrieveReportDefinitions(new HashMap<String, String[]>()));
             String[] parts = xml.split("<[^>]*?report-definition[\\s|>].*?>");
             Matcher scopeMatcher = SCOPE_PATTERN.matcher("");
             for (int i = 1; i < parts.length; i++) {
                 scopeMatcher.reset(parts[i]);
                 if (scopeMatcher.matches()) {
                     if (!scopeMatcher.group(1).equals(expectedScope)) {
                         throw new Exception("Non expected Scope with id "
                             + scopeMatcher.group(i) + " found");
                     }
                 }
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving children of an organizational unit.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param parentStatus
      *            status of the parent org-unit to create.
      * @param childStati
      *            stati of the children org-unit to create.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveOrganizationalUnitChildren(
         final String userHandle, final String parentStatus,
         final String[] childStati, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String createdXmlParent =
             prepareOrgUnit(PWCallback.DEFAULT_HANDLE, parentStatus, null);
         Document document =
             EscidocRestSoapTestsBase.getDocument(createdXmlParent);
         final String objidValueParent = getObjidValue(document);
 
         if (childStati != null && childStati.length > 0) {
             for (int i = 0; i < childStati.length; i++) {
                 String createdXmlChild =
                     prepareOrgUnit(PWCallback.DEFAULT_HANDLE, childStati[i],
                         new String[] { objidValueParent });
                 document =
                     EscidocRestSoapTestsBase.getDocument(createdXmlChild);
             }
         }
         try {
             PWCallback.setHandle(userHandle);
 
             handleXmlResult(organizationalUnitClient
                 .retrieveChildObjects(objidValueParent));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests creating an organizational unit.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestCreateOu(
         final String userHandle, 
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         try {
             PWCallback.setHandle(userHandle);
 
             organizationalUnitClient.create(ouXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests deleting an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteOu(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.delete(ouId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveOu(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.retrieve(ouId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving children of an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveChildOus(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.retrieveChildObjects(ouId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests retrieving parents of an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveParentOus(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.retrieveParentObjects(ouId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests updating an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateOu(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         createdOuXml = createdOuXml.replaceAll(
                 "(<[^\\/>]*?)title>", "$1title>replaced");
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.update(ouId, createdOuXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests opening an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestOpenOu(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         String lastModificationDate =
             getLastModificationDateValue(
                 EscidocRestSoapTestsBase.getDocument(createdOuXml));
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.open(ouId, getTheLastModificationParam(
                                 true, ouId, "comment", lastModificationDate));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests closing an organizational-unit.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestCloseOu(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String ouXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdOuXml = 
             handleResult(organizationalUnitClient.create(ouXml));
         String ouId = getObjidValue(createdOuXml);
         String lastModificationDate =
             getLastModificationDateValue(
                 EscidocRestSoapTestsBase.getDocument(createdOuXml));
         organizationalUnitClient.open(ouId, getTheLastModificationParam(
             true, ouId, "comment", lastModificationDate));
         
         try {
             PWCallback.setHandle(userHandle);
             organizationalUnitClient.close(
                 ouId, getTheLastModificationParam(true, ouId,
                 "Closed organizational unit '" + ouId + "'."));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests creating an context.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @return String contextXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCreateContext(
         final String userHandle, 
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         Document context =
             EscidocRestSoapTestsBase.getTemplateAsDocument(
                 TEMPLATE_CONTEXT_PATH + "/" + getTransport(false),
                 templateName);
         substitute(context, "/context/properties/name",
                 getUniqueName("PubMan Context "));
         try {
             PWCallback.setHandle(userHandle);
 
             String contextXml = 
                 handleResult(
                         contextClient.create(toString(context, false)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
             return contextXml;
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return null;
     }
 
     /**
      * Tests updating an context.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the context.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @return String contextXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestUpdateContext(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String contextXml = doTestCreateContext(creatorHandle, templateName, null);
         String contextId = getObjidValue(contextXml);
         contextXml = 
             contextXml.replaceAll(
                     "(<[^\\/>]*?)name>", "$1name>replaced");
         
         try {
             PWCallback.setHandle(userHandle);
             contextXml = handleResult(contextClient.update(contextId, contextXml));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
             return contextXml;
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return null;
     }
 
     /**
      * Tests retrieving an context.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the context.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @return String contextXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestRetrieveContext(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String contextXml = doTestCreateContext(creatorHandle, templateName, null);
         String contextId = getObjidValue(contextXml);
         
         try {
             PWCallback.setHandle(userHandle);
             contextXml = handleResult(contextClient.retrieve(contextId));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
             return contextXml;
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return null;
     }
 
     /**
      * Tests deleting an context.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the context.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteContext(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String contextXml = doTestCreateContext(creatorHandle, templateName, null);
         String contextId = getObjidValue(contextXml);
         
         try {
             PWCallback.setHandle(userHandle);
             contextClient.delete(contextId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests opening an context.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @return String contextXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestOpenContext(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String contextXml = doTestCreateContext(creatorHandle, templateName, null);
         String contextId = getObjidValue(contextXml);
         String lastModificationDate =
             getLastModificationDateValue(
                 EscidocRestSoapTestsBase.getDocument(contextXml));
         
         try {
             PWCallback.setHandle(userHandle);
             contextXml = handleResult(
                     contextClient.open(contextId, getTheLastModificationParam(
                                 true, contextId, 
                                 "comment", lastModificationDate)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
             return contextXml;
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return null;
     }
 
     /**
      * Tests closing an context.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the org-unit.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @return String contextXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestCloseContext(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String contextXml = doTestCreateContext(
                 creatorHandle, templateName, null);
         String contextId = getObjidValue(contextXml);
         String lastModificationDate =
             getLastModificationDateValue(
                 EscidocRestSoapTestsBase.getDocument(contextXml));
         contextXml = handleResult(
                 contextClient.open(contextId, getTheLastModificationParam(
                             true, contextId, 
                             "comment", lastModificationDate)));
         lastModificationDate =
             getLastModificationDateValue(
                 EscidocRestSoapTestsBase.getDocument(contextXml));
         
         try {
             PWCallback.setHandle(userHandle);
             contextXml = handleResult(contextClient.close(
                 contextId, getTheLastModificationParam(true, contextId,
                 "Closed context '" + contextId + "'.", lastModificationDate)));
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
             return contextXml;
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return null;
     }
 
     /**
      * Tests preprocessing statistic-data.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param aggregationDefinitionId
      *            aggregationDefinitionId to preprocess.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestPreprocessStatisticData(
         final String userHandle, final String aggregationDefinitionId,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String preprocessingInformationXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                     TEMPLATE_PREPROCESSING_INFO_PATH,
                 templateName);
         try {
             PWCallback.setHandle(userHandle);
 
             preprocessingClient.preprocess(
                     aggregationDefinitionId, 
                     preprocessingInformationXml);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestsBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestsBase.failException(e);
             }
             else {
                 EscidocRestSoapTestsBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests logging out an user. Before logging out, the user is logged in.
      * 
      * @param loginname
      *            The login name of the user to log in and log out.
      * @param password
      *            The password of the user.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestLogout(final String loginname, final String password)
         throws Exception {
 
         String userHandle = null;
         try {
             userHandle = login(loginname, password, true);
         }
         catch (Exception e) {
             EscidocRestSoapTestsBase.failException(
                 "INIT: Log in of the user failed.", e);
         }
         assertNotNull(userHandle);
 
         PWCallback.setHandle(userHandle);
 
         try {
             logout(userHandle);
         }
         catch (Exception e) {
             EscidocRestSoapTestsBase.failException(
                 "Logging out of the user failed.", e);
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Extracts the id of the context found in
      * the provided document.
      * 
      * @param document
      *            The document holding the xml representation of an item.
      * @return Returns the extracted id.
      * @throws Exception
      *             If anything fails.
      */
     protected String extractContextId(
         final Document document)
         throws Exception {
 
         final String contextId;
         contextId =
             getObjidValue(document, OmTestBase.XPATH_ITEM_CONTEXT);
 
         return contextId;
     }
 
     /**
      * Extracts the id of the component with the specified visibility found in
      * the provided document.
      * 
      * @param itemDocument
      *            The document holding the xml representation of an item.
      * @param visibility
      *            if the id of a component shall be
      *            retrieved, that has given visibility. 
      * @return Returns the extracted id.
      * @throws Exception
      *             If anything fails.
      */
     protected String extractComponentId(
         final Document itemDocument, final String visibility)
         throws Exception {
 
         final String componentId;
         componentId =
             getObjidValue(itemDocument, OmTestBase.XPATH_ITEM_COMPONENTS
                 + "/" + NAME_COMPONENT
                 + "[properties/visibility=\"" + visibility + "\"]");
 
         return componentId;
     }
 
     /**
      * Extracts the ids of the components of the provided document.
      * 
      * @param itemDocument
      *            The document holding the xml representation of an item.
      * @return Returns the extracted ids as string-array.
      * @throws Exception
      *             If anything fails.
      */
     protected String[] extractComponentIds(final Document itemDocument)
         throws Exception {
 
         String[] componentIds =
             getObjidValues(itemDocument, OmTestBase.XPATH_ITEM_COMPONENTS + "/"
                 + NAME_COMPONENT + "[properties/visibility=\"public\"]");
 
         return componentIds;
     }
 
     /**
      * Executs spo query to semantic store.
      * 
      * @param queryParam
      *            The Xml data defining the query
      * @return Returns the result of the method call.
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     protected String spo(final String queryParam) throws Exception {
         return handleXmlResult(getSemanticStoreClient().spo(queryParam));
     }
 
     /**
      * Retrieve a Template as a Document and fixes the "link" attributes in case
      * of SOAP.<br>
      * The used parser is NOT namespace aware!
      * 
      * @param path
      *            The Path of the Template.
      * @param templateName
      *            The name of the template.
      * @return The String representation of the Template.
      * @throws Exception
      *             If anything fails.
      */
     public Document getTemplateAsFixedUserGroupDocument(
         final String path, final String templateName) throws Exception {
 
 //        return fixLinkAttributes(EscidocRestSoapTestsBase
 //            .getTemplateAsDocument(path, templateName),
 //            XPATH_USER_GROUP_SELECTORS);
         return EscidocRestSoapTestsBase
             .getTemplateAsDocument(path, templateName);
     }
 
     /**
      * Gets the document for the provided grant xml data and fixes the "link"
      * attributes in case of SOAP.<br>
      * The used parser is NOT namespace aware!
      * 
      * @param grantXml
      *            The grant xml data to get the fixed document from.
      * @return Returns the fixed document.
      * @throws Exception
      *             If anything fails.
      */
     public Document getFixedGrantDocument(final String grantXml)
         throws Exception {
 
         return fixGrantDocument(EscidocRestSoapTestsBase.getDocument(grantXml));
     }
 
     /**
      * Retrieve a Template as a String and fixes the "link" attributes in case
      * of SOAP.<br>
      * The used parser is NOT namespace aware!
      * 
      * @param path
      *            The Path of the Template.
      * @param templateName
      *            The name of the template.
      * @return The String representation of the Template.
      * @throws Exception
      *             If anything fails.
      */
     public String getTemplateAsFixedGrantString(
         final String path, final String templateName) throws Exception {
 
         final Document document =
             getTemplateAsFixedGrantDocument(path, templateName);
         return toString(document, false);
     }
 
     /**
      * Retrieve a Template as a Document and fixes the "link" attributes in case
      * of SOAP.<br>
      * The used parser is NOT namespace aware!
      * 
      * @param path
      *            The Path of the Template.
      * @param templateName
      *            The name of the template.
      * @return The String representation of the Template.
      * @throws Exception
      *             If anything fails.
      */
     public Document getTemplateAsFixedGrantDocument(
         final String path, final String templateName) throws Exception {
 
         final Document document =
             EscidocRestSoapTestsBase.getTemplateAsDocument(path, templateName);
         fixGrantDocument(document);
         return document;
     }
 
     /**
      * Fixes the "link" attributes in case of SOAP.
      * 
      * @param document
      *            The document to fix.
      * @return Returns the provided (fixed) document.
      * @throws Exception
      *             If anything fails.
      */
     public Document fixGrantDocument(final Document document) throws Exception {
 
         fixLinkAttributes(document, XPATH_GRANT_ROLE);
         fixLinkAttributes(document, XPATH_GRANT_OBJECT);
         return document;
     }
 
     /**
      * Test logging out an user.
      * 
      * @param userHandle
      *            The eSciDOc user handle that shall be sent in the cookie of
      *            the logout request.
      * @return The response of the logout.
      * @throws Exception
      *             If anything fails.
      */
     protected String logout(final String userHandle) throws Exception {
 
         Object result = getUserManagementWrapperClient().logout(userHandle);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
            xmlResult = getResponseBodyAsUTF8(httpRes);
             assertHttpStatusOfMethod("", httpRes);
             assertNoRedirect(httpRes);
             assertLogoutCookies(httpRes);
            
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test logging out an user.
      * 
      * @param redirectUrl
      *            The target to that the user shall be redirected after being
      *            logged out. This may be <code>null</code> (no redirect).
      * @param userHandle
      *            The eSciDOc user handle that shall be sent in the cookie of
      *            the logout request.
      * @param encodeRedirectUrlSlashes
      *            Flag indicating that the slashes contained in the redirectUrl
      *            shall be encoded (<code>true</code>) or shall not be
      *            encoded (<code>false</code>).
      * @return The response of the logout.
      * @throws Exception
      *             If anything fails.
      */
     protected String logout(
         final String redirectUrl, final String userHandle,
         final boolean encodeRedirectUrlSlashes) throws Exception {
 
         final Object result =
             getUserManagementWrapperClient().logout(redirectUrl, userHandle,
                 encodeRedirectUrlSlashes);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result;
            xmlResult = getResponseBodyAsUTF8(httpRes);
             assertHttpStatus("", HttpServletResponse.SC_SEE_OTHER, httpRes);
             assertRedirect(httpRes, redirectUrl);
             assertLogoutCookies(httpRes);
          
         }
         else {
             fail("Unexpected result, expected result of type HttpResponse.");
         }
         return xmlResult;
     }
 
     /**
      * Asserts that vthe expected cookies exists in a logout response.
      * 
      * @param method
      *            The {@link HttpResponse} to be asserted.
      */
     private void assertLogoutCookies(final HttpResponse httpRes) {
 
         final Header[] cookieHeaders = httpRes.getHeaders("Set-Cookie");
         assertNotNull(cookieHeaders);
         assertEquals("Unexpected number of cookies (to delete cookies)", 2,
             cookieHeaders.length);
         assertNotNull(cookieHeaders[0]);
         assertEquals(
             "Unexpected cookie (to delete cookie)",
             "escidocCookie=null; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/",
             cookieHeaders[0].getValue());
         assertNotNull(cookieHeaders[1]);
         assertEquals("Unexpected cookie (to delete cookie)",
             "JSESSIONID=null; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/",
             cookieHeaders[1].getValue());
     }
 
     /**
      * Asserts the redirect values.
      * 
      * @param method
      *            The {@link HttpResponse} to be asserted.
      * @param expectedRedirectUrl
      *            The expected redirect url.
      */
     private void assertRedirect(
         final HttpResponse httpResponse, final String expectedRedirectUrl) {
 
         final Header locationHeader = httpResponse.getFirstHeader("Location");
         assertNotNull(locationHeader);
         assertEquals("Unexpected redirect location", expectedRedirectUrl,
             locationHeader.getValue());
     }
 
     /**
      * Asserts, that no redirect occurred for the provided {@link HttpResponse}.
      * 
      * @param method
      *            The {@link HttpResponse} to be asserted.
      */
     private void assertNoRedirect(final HttpResponse httpRes) {
         assertNull("Unexpected loction header", httpRes
             .getFirstHeader("Location"));
     }
 }
