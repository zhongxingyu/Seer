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
 
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyActiveException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyDeactiveException;
 import de.escidoc.core.test.EscidocRestSoapTestsBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.interfaces.ResourceHandlerClientInterface;
 import de.escidoc.core.test.security.client.PWCallback;
 
 /**
  * Base class for testing the AA UserAccountHandler component.
  * 
  * @author TTE
  * 
  */
 public class UserAccountTestBase extends AaTestBase {
 
     public static final String XPATH_USER_ACCOUNT_XML_BASE =
         XPATH_USER_ACCOUNT + PART_XML_BASE;
 
     public static final String XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_TITLE =
         XPATH_USER_ACCOUNT_CURRENT_GRANTS + PART_XLINK_TITLE;
 
     public static final String XPATH_USER_ACCOUNT_CURRENT_GRANTS_XLINK_HREF =
         XPATH_USER_ACCOUNT_CURRENT_GRANTS + PART_XLINK_HREF;
 
     /**
      * The constructor.
      * 
      * @param transport
      *            The transport identifier.
      */
     public UserAccountTestBase(final int transport) {
         super(transport);
     }
 
     /**
      * Test activating an UserAccount.
      * 
      * @param id
      *            The id of the UserAccount.
      * @param taskParamXml
      *            The task parameter in an XML structure.
      * @throws Exception
      *             If anything fails.
      */
     protected String activate(final String id, final String taskParamXml)
         throws Exception {
 
         Object result = getUserAccountClient().activate(id, taskParamXml);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
             if (xmlResult.equals("")) {
                 xmlResult = null;
             }
             
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test updating the password of an UserAccount.
      * 
      * @param id
      *            The id of the UserAccount.
      * @param taskParamXml
      *            The task parameter in an XML structure.
      * @throws Exception
      *             If anything fails.
      */
     protected void updatePassword(final String id, final String taskParamXml)
         throws Exception {
 
         getUserAccountClient().updatePassword(id, taskParamXml);
     }
 
     /**
      * @return Returns the userAccountClient
      */
     @Override
     public ResourceHandlerClientInterface getClient() {
 
         return getUserAccountClient();
     }
 
     /**
      * Test deactivating an UserAccount.
      * 
      * @param id
      *            The id of the UserAccount.
      * @param taskParamXml
      *            The task parameter in an XML structure.
      * @throws Exception
      *             If anything fails.
      */
     protected String deactivate(final String id, final String taskParamXml)
         throws Exception {
 
         Object result = getUserAccountClient().deactivate(id, taskParamXml);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
            xmlResult = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
          
             if (xmlResult.equals("")) {
                 xmlResult = null;
             }
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test retrieving an UserAccount.
      * 
      * @param identifier
      *            An unique identifier (id, loginname, eSciDocUserHandle) of the
      *            UserAccount.
      * @return The xml representation of the UserAccount.
      * @throws Exception
      *             If anything fails.
      */
     @Override
     public String retrieve(final String identifier) throws Exception {
 
         return super.retrieve(identifier);
     }
 
     /**
      * Test retrieving the  UserAccount of the currrently logged in user.
      * 
       * @param handle
      *            The handle of the user who shall retrieve the grant.
      * @return The xml representation of the UserAccount.
      * @throws Exception
      *             If anything fails.
      */
     protected String retrieveCurrentUser(final String handle)
         throws Exception {
 
         String replacedHandle = handle;
         if (replacedHandle == null) {
             replacedHandle = PWCallback.DEFAULT_HANDLE;
         }
         PWCallback.setHandle(replacedHandle);
         Object result;
         try {
             result = getUserAccountClient().retrieveCurrentUser();
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
            xmlResult = EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
             if (xmlResult.equals("")) {
                 xmlResult = null;
             }
           
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test retrieving list of user accounts from the framework.
      * 
      * @param filter
      *            The filter criteria.
      * @return The retrieved resource.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveUserAccounts(final String filter) throws Exception {
 
         return handleXmlResult(
             getUserAccountClient().retrieveUserAccounts(filter));
     }
 
     /**
      * Test retrieving list of user accounts from the framework.
      * 
      * @param filter
      *            The filter criteria.
      * @return The retrieved resource.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveUserAccounts(final Map<String, String[]> filter)
         throws Exception {
 
         return handleXmlResult(
             getUserAccountClient().retrieveUserAccounts(filter));
     }
 
     /**
      * Inserts a unique loginname into the provided document by adding the
      * current timestamp to the contained loginname.
      * 
      * @param document
      *            The document.
      * @return The inserted login name.
      * @throws Exception
      *             If anything fails.
      */
     protected String insertUniqueLoginName(final Document document)
         throws Exception {
 
         assertXmlExists("No login-name found in template data. ", document,
             "/user-account/properties/login-name");
         final Node loginNameNode =
             selectSingleNode(document, "/user-account/properties/login-name");
         String loginname = loginNameNode.getTextContent().trim();
         loginname += System.currentTimeMillis();
 
         loginNameNode.setTextContent(loginname);
 
         return loginname;
     }
 
     /**
      * Inserts the given values into the provided document .
      * 
      * @param document
      *            The document.
      * @param name
      *            The name.
      * @param loginname
      *            The loginname.
      * @throws Exception
      *             If anything fails.
      */
     protected void insertUserAccountValues(final Document document, 
                                             final String name, 
                                             final String loginname)
                                                 throws Exception {
 
         assertXmlExists("No login-name found in template data. ", document,
             "/user-account/properties/login-name");
         assertXmlExists("No name found in template data. ", document,
         "/user-account/properties/name");
 
         if (loginname != null) {
             final Node loginNameNode =
                 selectSingleNode(document, "/user-account/properties/login-name");
             loginNameNode.setTextContent(loginname);
         }
 
         if (name != null) {
             final Node nameNode =
                 selectSingleNode(document, "/user-account/properties/name");
             nameNode.setTextContent(name);
         }
 
     }
 
     /**
      * Successfully creates an UserAccount.
      * 
      * @param templateName
      *            The name of the template.
      * @return Returns the UserAccount document.
      * @throws Exception
      *             If anything fails
      */
     public Document createSuccessfully(final String templateName)
         throws Exception {
         
         final Document toBeCreatedDocument =
             EscidocRestSoapTestsBase
                 .getTemplateAsDocument(TEMPLATE_USER_ACCOUNT_PATH,
                     templateName);
         insertUniqueLoginName(toBeCreatedDocument);
 
         final String toBeCreatedXml = toString(toBeCreatedDocument, false);
         assertXmlValidUserAccount(toBeCreatedXml);
         final String beforeCreationTimestamp = getNowAsTimestamp();
 
         String createdUserAccountXml = null;
         try {
             createdUserAccountXml = create(toBeCreatedXml);
         }
         catch (Exception e) {
             EscidocRestSoapTestsBase.failException(e);
         }
         return assertActiveUserAccount(createdUserAccountXml, toBeCreatedXml,
             beforeCreationTimestamp, beforeCreationTimestamp, false);
     }
 
     /**
      * Successfully retrieves an UserAccount.
      * 
      * @param id
      *            The id of the user account.
      * @return Returns the UserAccount document.
      * @throws Exception
      *             If anything fails
      */
     public Document retrieveSuccessfully(final String id) throws Exception {
 
         String retrievedXml = null;
         try {
             retrievedXml = retrieve(id);
         }
         catch (Exception e) {
             EscidocRestSoapTestsBase.failException(
                 "Retrieving user account failed. ", e);
         }
         assertXmlValidUserAccount(retrievedXml);
         return EscidocRestSoapTestsBase.getDocument(retrievedXml);
     }
 
     /**
      * Tests creating a user-account.
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
     protected void doTestCreateUserAccount(
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                     TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.create(userAccountXml);
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
      * Tests retrieving a user-account.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the user-account.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveUserAccount(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdUserAccountXml = 
             handleResult(userAccountClient.create(userAccountXml));
         String userId = getObjidValue(createdUserAccountXml);
         
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.retrieve(userId);
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
      * Tests retrieving a user-account.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param accountId
      *            id of account to retrieve.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveUserAccount(final String userHandle,
         final String accountId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.retrieve(accountId);
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
      * Tests updating a user-account.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the user-account.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateUserAccount(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdUserAccountXml = 
             handleResult(userAccountClient.create(userAccountXml));
         String userId = getObjidValue(createdUserAccountXml);
         createdUserAccountXml.replaceFirst("name>", "name>add");
         
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.update(userId, createdUserAccountXml);
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
      * Tests updating a user-account.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param accountId
      *            id of account to update.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateUserAccount(final String userHandle,
         final String accountId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml = 
             handleResult(userAccountClient.retrieve(accountId));
         userAccountXml.replaceFirst("name>", "name>add");
         
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.update(accountId, userAccountXml);
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
      * Tests deleting a user-account.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the user-account.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteUserAccount(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdUserAccountXml = 
             handleResult(userAccountClient.create(userAccountXml));
         String userId = getObjidValue(createdUserAccountXml);
         
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.delete(userId);
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
      * Tests deleting a user-account.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param accountId
      *            id of account to delete.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeleteUserAccount(final String userHandle,
         final String accountId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         try {
             PWCallback.setHandle(userHandle);
 
             userAccountClient.delete(accountId);
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
      * Tests activating a user-account.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the user-account.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestActivateUserAccount(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdUserAccountXml = 
             handleResult(userAccountClient.create(userAccountXml));
         String userId = getObjidValue(createdUserAccountXml);
         
         try {
             PWCallback.setHandle(userHandle);
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(createdUserAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.activate(userId, taskParamXml);
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
      * Tests activating a user-account.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param accountId
      *            id of account to activate.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestActivateUserAccount(final String userHandle,
         final String accountId, final Class< ? > expectedExceptionClass)
         throws Exception {
         try {
             String userAccountXml = 
                 handleResult(userAccountClient.retrieve(accountId));
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(userAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.deactivate(accountId, taskParamXml);
         } catch (AlreadyDeactiveException e) {}
 
         String userAccountXml = 
             handleResult(userAccountClient.retrieve(accountId));
 
         try {
             PWCallback.setHandle(userHandle);
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(userAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.activate(accountId, taskParamXml);
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
      * Tests deactivating a user-account.
      * 
      * @param creatorHandle
      *            The escidoc user handle that creates the user-account.
      * @param userHandle
      *            The escidoc user handle.
      * @param templateName
      *            templateName.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeactivateUserAccount(final String creatorHandle,
         final String userHandle,
         final String templateName, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         String userAccountXml =
             EscidocRestSoapTestsBase.getTemplateAsString(
                 TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
         PWCallback.setHandle(creatorHandle);
         String createdUserAccountXml = 
             handleResult(userAccountClient.create(userAccountXml));
         String userId = getObjidValue(createdUserAccountXml);
         
         try {
             PWCallback.setHandle(userHandle);
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(createdUserAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.deactivate(userId, taskParamXml);
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
      * Tests deactivating a user-account.
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param accountId
      *            id of account to deactivate.
      * @param expectedExceptionClass
      *            expectedExceptionClass.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestDeactivateUserAccount(final String userHandle,
         final String accountId, final Class< ? > expectedExceptionClass)
         throws Exception {
 
         try {
             String userAccountXml = 
                 handleResult(userAccountClient.retrieve(accountId));
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(userAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.activate(accountId, taskParamXml);
         } catch (AlreadyActiveException e) {}
 
         String userAccountXml = 
             handleResult(userAccountClient.retrieve(accountId));
         
         try {
             PWCallback.setHandle(userHandle);
             String lastModificationDate =
                 getLastModificationDateValue(
                     EscidocRestSoapTestsBase.getDocument(userAccountXml));
             String taskParamXml =
                 "<param last-modification-date=\"" 
                     + lastModificationDate + "\" />";
             userAccountClient.deactivate(accountId, taskParamXml);
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
      * Assert that the user account is active and has all required elements.<br/>
      * This method delegates to assertUserAccount. Additionally, it is asserted
      * that the active flag is set to true.
      * 
      * @param toBeAssertedXml
      *            The created/updated user account.
      * @param originalXml
      *            The template used to create/update the user account. If this
      *            parameter is <code>null</code>, no check with the original
      *            data is performed.
      * @param timestampBeforeCreation
      *            A timestamp before the creation has been started. This is used
      *            to check the creation date.
      * @param timestampBeforeLastModification
      *            A timestamp before the last modification has been started.
      *            This is used to check the last modification date.
      * @param assertCreationDate
      *            Flag to indicate if the creation-date and created-by values
      *            shall be asserted (<code>true</code>) or not (<code>false</code>).
      * @return Returns the document representing the provided xml data.
      * @throws Exception
      *             If anything fails.
      */
     public Document assertActiveUserAccount(
         final String toBeAssertedXml, final String originalXml,
         final String timestampBeforeCreation,
         final String timestampBeforeLastModification,
         final boolean assertCreationDate) throws Exception {
 
         final Document toBeAssertedDocument =
             assertUserAccount(toBeAssertedXml, originalXml,
                 timestampBeforeCreation, timestampBeforeLastModification,
                 assertCreationDate);
 
         assertXmlEquals("User account is not active. ", toBeAssertedDocument,
             XPATH_USER_ACCOUNT_ACTIVE, "true");
 
         return toBeAssertedDocument;
     }
 
     /**
      * Assert that the user account is deactive and has all required elements.<br/>
      * This method delegates to assertUserAccount. Additionally, it is asserted
      * that the active flag is set to false.
      * 
      * @param toBeAssertedXml
      *            The created/updated user account.
      * @param originalXml
      *            The template used to create/update the user account. If this
      *            parameter is <code>null</code>, no check with the original
      *            data is performed.
      * @param timestampBeforeCreation
      *            A timestamp before the creation has been started. This is used
      *            to check the creation date.
      * @param timestampBeforeLastModification
      *            A timestamp before the last modification has been started.
      *            This is used to check the last modification date.
      * @param assertCreationDate
      *            Flag to indicate if the creation-date and created-by values
      *            shall be asserted (<code>true</code>) or not (<code>false</code>).
      * @return Returns the document representing the provided xml data.
      * @throws Exception
      *             If anything fails.
      */
     public Document assertDeactiveUserAccount(
         final String toBeAssertedXml, final String originalXml,
         final String timestampBeforeCreation,
         final String timestampBeforeLastModification,
         final boolean assertCreationDate) throws Exception {
 
         final Document toBeAssertedDocument =
             assertUserAccount(toBeAssertedXml, originalXml,
                 timestampBeforeCreation, timestampBeforeLastModification,
                 assertCreationDate);
 
         assertXmlEquals("User account is not deactive. ", toBeAssertedDocument,
             XPATH_USER_ACCOUNT_ACTIVE, "false");
 
         return toBeAssertedDocument;
     }
 
     /**
      * Assert that the user account has all required elements.<br/>
      * 
      * <ul>
      * <li>It is asserted that the active flag exists, but the value of this
      * flag is not checked.</li>
      * </ul>
      * 
      * @param toBeAssertedXml
      *            The created/updated user account.
      * @param originalXml
      *            The template used to create/update the user account. If this
      *            parameter is <code>null</code>, no check with the original
      *            data is performed.
      * @param timestampBeforeCreation
      *            A timestamp before the creation has been started. This is used
      *            to check the creation date.
      * @param timestampBeforeLastModification
      *            A timestamp before the last modification has been started.
      *            This is used to check the last modification date.
      * @param assertCreationDate
      *            Flag to indicate if the creation-date and created-by values
      *            shall be asserted (<code>true</code>) or not (<code>false</code>).
      * @return Returns the document representing the provided xml data.
      * @throws Exception
      *             If anything fails.
      */
     public Document assertUserAccount(
         final String toBeAssertedXml, final String originalXml,
         final String timestampBeforeCreation,
         final String timestampBeforeLastModification,
         final boolean assertCreationDate) throws Exception {
 
         assertXmlValidUserAccount(toBeAssertedXml);
         Document toBeAssertedDocument =
             EscidocRestSoapTestsBase.getDocument(toBeAssertedXml);
 
         String[] rootValues =
             assertRootElement("Invalid root element. ", toBeAssertedDocument,
                 XPATH_USER_ACCOUNT, Constants.USER_ACCOUNT_BASE_URI,
                 timestampBeforeLastModification);
         final String id = rootValues[0];
 
         // assert resources (in case of REST)
         if (getTransport() == Constants.TRANSPORT_REST) {
             assertReferencingElement("Assert of resources failed. ",
                 toBeAssertedDocument, XPATH_USER_ACCOUNT_RESOURCES, null);
             assertReferencingElement(
                 "Assert of resource current-grants failed. ",
                 toBeAssertedDocument, XPATH_USER_ACCOUNT_RESOURCES + "/"
                     + "current-grants[@href=\"/aa/user-account/" + id
                     + "/resources/current-grants\"]", null);
         }
 
         // assert properties
         assertPropertiesElementUnversioned(
             "Asserting user account properties failed. ", toBeAssertedDocument,
             XPATH_USER_ACCOUNT_PROPERTIES, timestampBeforeCreation);
 
         // assert active flag
         assertXmlExists("Missing active flag. ", toBeAssertedDocument,
             XPATH_USER_ACCOUNT_ACTIVE);
 
         if (originalXml != null) {
             final Document originalDocument =
                 EscidocRestSoapTestsBase.getDocument(originalXml);
 
             if (assertCreationDate) {
                 final String expectedCreationDate =
                     getCreationDateValue(originalDocument);
                 if (expectedCreationDate != null) {
 
                     // creation-date
                     assertXmlEquals("Creation date mismatch, ",
                         toBeAssertedDocument, XPATH_USER_ACCOUNT_CREATION_DATE,
                         expectedCreationDate);
 
                     // created-by
                     assertCreatedBy("Created-by invalid", originalDocument,
                         toBeAssertedDocument);
                 }
             }
 
             // name
             assertXmlEquals("Name mismatch, ", originalDocument,
                 toBeAssertedDocument, XPATH_USER_ACCOUNT_NAME);
 
             // loginname
             assertXmlEquals("Loginname mismatch, ", originalDocument,
                 toBeAssertedDocument, XPATH_USER_ACCOUNT_LOGINNAME);
 
         }
 
         return toBeAssertedDocument;
     }
 
     protected String createOrganizationalUnit() throws Exception {
         Document ou =
             getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH,
                 "escidoc_ou_create.xml");
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String template = toString(ou, false);
         assertXmlValidOrganizationalUnit(template);
         String orgUnitXml = null;
         try {
             orgUnitXml =
                 handleXmlResult(getOrganizationalUnitClient().create(template));
         }
         catch (Exception e) {
             failException("Init: OU create failed.", e);
         }
         return orgUnitXml;
     }
 
     protected String createClosedOrganizationalUnit() throws Exception {
         String orgUnitXml = createOrganizationalUnit();
         final String ouId = getObjidValue(orgUnitXml);
         String lastModDate =
             getLastModificationDateValue(getDocument(orgUnitXml));
 
         // open ou
         try {
             getOrganizationalUnitClient().open(ouId,
                 "<param last-modification-date=\"" + lastModDate + "\" />");
         }
         catch (Exception e) {
             failException("Init: OU open failed. [" + ouId + "]", e);
         }
 
         // close ou
         try {
             orgUnitXml =
                 handleXmlResult(getOrganizationalUnitClient().retrieve(ouId));
             lastModDate = getLastModificationDateValue(getDocument(orgUnitXml));
             getOrganizationalUnitClient().close(ouId,
                 "<param last-modification-date=\"" + lastModDate + "\" />");
         }
         catch (Exception e) {
             failException("Init: OU close failed.", e);
         }
         return ouId;
     }
 
 }
