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
 
 import de.escidoc.core.common.exceptions.remote.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.UserAccountNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.UserGroupNotFoundException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.aa.GrantClient;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.apache.http.HttpMessage;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 /**
  * Base Class for GrantTests (userGrants and groupGrants).
  * 
  * @author TTE
  * 
  */
 public class GrantTestBase extends UserAccountTestBase {
 
     public static final String NAME_GRANT = "grant";
     
     protected int handlerCode;
     
     public static final String NAME_GRANTS = "grants";
 
     public static final String PARTS_GRANTS_GRANT =
         "/" + NAME_RESOURCES + "/" + NAME_GRANTS + "/" + NAME_GRANT;
 
     public static final String XPATH_GRANT_LAST_MODIFICATION_DATE =
         XPATH_GRANT + PART_LAST_MODIFICATION_DATE;
 
     public static final String NAME_REVOCATION_REMARK = "revocation-remark";
 
     public static final String XPATH_GRANT_REVOCATION_REMARK =
         XPATH_GRANT_PROPERTIES + "/" + NAME_REVOCATION_REMARK;
 
     public static final String XPATH_GRANT_CREATION_DATE =
         XPATH_GRANT_PROPERTIES + "/" + NAME_CREATION_DATE;
 
     public static final String NAME_REVOCATION_DATE = "revocation-date";
 
     public static final String XPATH_GRANT_REVOCATION_DATE =
         XPATH_GRANT_PROPERTIES + "/" + NAME_REVOCATION_DATE;
 
     public static final String XPATH_CURRENT_GRANTS_GRANT =
         "/" + NAME_CURRENT_GRANTS + "/" + NAME_GRANT;
 
     public static final String XPATH_CURRENT_GRANTS_GRANT_PROPERTIES =
         XPATH_CURRENT_GRANTS_GRANT + "/" + NAME_PROPERTIES;
 
     public static final String NAME_REVOKED_BY = "revoked-by";
 
     public static final String XPATH_GRANT_REVOKED_BY =
         XPATH_GRANT_PROPERTIES + "/" + NAME_REVOKED_BY;
 
     public static final String XPATH_GRANT_OBJID = XPATH_GRANT + PART_OBJID;
 
     public static final String XPATH_GRANT_XLINK_HREF =
         XPATH_GRANT + PART_XLINK_HREF;
 
     public static final String XPATH_GRANT_XLINK_TITLE =
         XPATH_GRANT + PART_XLINK_TITLE;
 
     private Pattern grantPattern = Pattern.compile("</[^:]*?:grant>");
     
     private Matcher grantMatcher = grantPattern.matcher("");
     
     protected String defaultUserAccountOrGroupId = null;
     
     protected static boolean isUserAccountTest = false;
         
     private GrantClient client = null;
 
     protected ResourceNotFoundException notFoundException = null;
 
     /**
      * @param transport
      *            The transport identifier.
      * @param handlerCode
      *            handlerCode.
      * @throws Exception e
      */
     public GrantTestBase(
             final int transport,
             final int handlerCode) throws Exception {
         super(transport);
         if (handlerCode == USER_ACCOUNT_HANDLER_CODE) {
             defaultUserAccountOrGroupId = TEST_USER_ACCOUNT_ID;
             notFoundException = new UserAccountNotFoundException();
             isUserAccountTest = true;
         } else if (handlerCode == USER_GROUP_HANDLER_CODE) {
             defaultUserAccountOrGroupId = TEST_USER_GROUP_ID;
             notFoundException = new UserGroupNotFoundException();
             isUserAccountTest = false;
         } else {
             throw new Exception(
                     "only UserAccountHandler or UserGroupHandler allowed");
         }
         this.handlerCode = handlerCode;
         client = (GrantClient) getClient(handlerCode);
     }
     /**
      * 
      * @param transport transport
      * @param handlerCode handlerCode
      * @param id user or group id
      * @throws Exception e
      */
     public GrantTestBase(
         final int transport,
         final int handlerCode,
         final String id) throws Exception {
     super(transport);
     if (handlerCode == USER_ACCOUNT_HANDLER_CODE) {
         notFoundException = new UserAccountNotFoundException();
     } else if (handlerCode == USER_GROUP_HANDLER_CODE) {
         notFoundException = new UserGroupNotFoundException();
     } else {
         throw new Exception(
                 "only UserAccountHandler or UserGroupHandler allowed");
     }
     this.handlerCode = handlerCode;
     defaultUserAccountOrGroupId = id;
     client = (GrantClient) getClient(handlerCode);
 }
 
     /**
      * Test creating a Grant for an UserAccount or UserGroup.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup.
      * @param grantXml
      *            The xml representation of the Grant.
      * @return The xml representation of the created Grant.
      * @throws Exception
      *             If anything fails.
      */
     protected String createGrant(final String id, final String grantXml)
         throws Exception {
         Object result = client.createGrant(id, grantXml);
         String xmlResult = null;
         if (result instanceof HttpMessage) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
            
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test retrieving the current Grants of an UserAccount or UserGroup.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup.
      * @return The xml representation of the current Grants.
      * @throws Exception
      *             If anything fails.
      */
     protected String retrieveCurrentGrants(final String id) throws Exception {
 
         Object result = client.retrieveCurrentGrants(id);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
           
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test retrieving a Grant of an UserAccount or UserGroup.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup.
      * @param grantId
      *            The id of the Grant.
      * @return The xml representation of the Grant.
      * @throws Exception
      *             If anything fails.
      */
     protected String retrieveGrant(final String id, final String grantId)
         throws Exception {
 
         Object result = client.retrieveGrant(id, grantId);
         String xmlResult = null;
         if (result instanceof HttpMessage) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
        
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test retrieving a list of Grants matching the given filter-criteria.
      * 
      * @param filter filter-criteria as CQL query.
      * @return The xml representation of the Grant-List.
      * @throws Exception
      *             If anything fails.
      */
     protected String retrieveGrants(final Map<String, String[]> filter)
         throws Exception {
 
         Object result = client.retrieveGrants(filter);
         String xmlResult = null;
         if (result instanceof HttpMessage) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
          
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test revoking a Grant of an UserAccount or UserGroup.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup.
      * @param grantId
      *            The id of the Grant.
      * @param taskParamXml
      *            The task parameter in an XML structure.
      * @param handle
      *            The handle of the user who shall revoke the grant.
      * @return String 
      * @throws Exception
      *             If anything fails.
      */
     protected String revokeGrant(
         final String id, final String grantId, 
         final String taskParamXml, final String handle)
         throws Exception {
 
         String replacedHandle = handle;
         if (replacedHandle == null) {
             replacedHandle = PWCallback.DEFAULT_HANDLE;
         }
         PWCallback.setHandle(replacedHandle);
         Object result;
         try {
             result =
                 client.revokeGrant(id, grantId, taskParamXml);
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
            
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test revoking a Grant of an UserAccount or UserGroup.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup.
      * @param taskParamXml
      *            The task parameter in an XML structure.
      * @return String 
      * @throws Exception
      *             If anything fails.
      */
     protected String revokeGrants(
         final String id, final String taskParamXml)
         throws Exception {
 
         Object result =
             client.revokeGrants(id, taskParamXml);
         String xmlResult = null;
         if (result instanceof HttpResponse) {
             HttpResponse method = (HttpResponse) result;
             xmlResult = EntityUtils.toString(method.getEntity(), HTTP.UTF_8);
             assertHttpStatusOfMethod("", method);
           
         }
         else if (result instanceof String) {
             xmlResult = (String) result;
         }
         return xmlResult;
     }
 
     /**
      * Test revoking all grants of a user.
      * 
      * @param id
      *            The id of the UserAccount or UserGroup 
      *            to revoke all grants for.
      * @return The <code>HttpMessage</code> object.
      * @throws Exception
      *             If anything fails.
      */
     public String revokeAllGrants(final String id)
         throws Exception {
 
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         Object result = client.revokeGrants(id, 
            "<param><filter />"
                 + "<revocation-remark>some remark</revocation-remark></param>");
         String resultStr = handleResult(result);
         assertNumberOfGrants(id, 0);
         return resultStr;
     }
 
     /**
      * Successfully creates a grant from the provided template for the default
      * user account, i.e. for the account with the id
      * <code>defaultUserAccountOrGroupId</code>.
      * 
      * @param templateName
      *            The name of the template.
      * @return Returns the grant document.
      * @throws Exception
      *             If anything fails.
      */
     public Document createGrantSuccessfully(final String templateName)
         throws Exception {
 
         return createGrantSuccessfully(defaultUserAccountOrGroupId, templateName);
     }
 
     /**
      * Successfully creates a grant from the provided template.
      * 
      * @param id
      *            The id of the user account or user group 
      *            for that the grant shall be
      *            created.
      * @param templateName
      *            The name of the template.
      * @return Returns the grant document.
      * @throws Exception
      *             If anything fails.
      */
     public Document createGrantSuccessfully(
         final String id, final String templateName) throws Exception {
 
         final String toBeCreatedXml =
             getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH,
                 templateName);
 
         String createdXml = null;
         try {
             createdXml = createGrant(id, toBeCreatedXml);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(e);
         }
         assertGrant(createdXml, toBeCreatedXml, id, startTimestamp,
             startTimestamp, false);
 
         return EscidocRestSoapTestBase.getDocument(createdXml);
     }
 
     /**
      * Successfully creates a given number of grants.
      * 
      * @param id
      *            The id of the user account or user group 
      *            for that the grant shall be
      *            created.
      * @param number
      *            The number of grants to be created.
      * @return Returns the grant documents.
      * @throws Exception
      *             If anything fails.
      */
     public Map<String, Node> createGrantsSuccessfully(
         final String id, final int number) throws Exception {
 
         final int expectedLength = 3;
         final Map<String, Node> expectedGrants =
             new HashMap<String, Node>(expectedLength);
         for (int i = 0; i < expectedLength; i++) {
             final String templateName;
             if (i == 0) {
                 templateName = "escidoc_grant_for_create.xml";
             }
             else {
                 templateName = "escidoc_grant" + (i + 1) + "_for_create.xml";
             }
 
             final Document createdDocument =
                 createGrantSuccessfully(id, templateName);
             final String objid = getObjidValue(createdDocument);
             expectedGrants.put(objid, createdDocument);
         }
         return expectedGrants;
     }
 
     /**
      * Successfully retrieves the grant object with the provided id.
      * 
      * @param grantId
      *            The id of the grant to retrieve.
      * @return Returns the grant document.
      * @throws Exception
      *             If anything fails.
      */
     public Document retrieveGrantSuccessfully(final String grantId)
         throws Exception {
         String updatedGrantXml = null;
         try {
             updatedGrantXml = retrieveGrant(defaultUserAccountOrGroupId, grantId);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(e);
         }
         assertXmlValidGrants(updatedGrantXml);
         return EscidocRestSoapTestBase.getDocument(updatedGrantXml);
     }
 
     /**
      * Successfully retrieves the grants with the provided user-id.
      * 
      * @param id
      *            The id of the user or group to retrieve the grants for.
      * @param expectedLength
      *            The expected number of grants.
      * @return Returns the grant document.
      * @throws Exception
      *             If anything fails.
      */
     public Map<String, Node> retrieveGrantsSuccessfully(final String id, 
                                             final int expectedLength)
         throws Exception {
         final Map<String, Node> expectedGrants =
             new HashMap<String, Node>(expectedLength);
         String currentGrantsXml = null;
         try {
             currentGrantsXml = retrieveCurrentGrants(id);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(e);
         }
         assertXmlValidGrants(currentGrantsXml);
         final Document retrievedDocument =
             EscidocRestSoapTestBase.getDocument(currentGrantsXml);
         NodeList grants =
             selectNodeList(retrievedDocument, XPATH_CURRENT_GRANTS_GRANT);
         final int length = grants.getLength();
         assertEquals("Unexpected number of current grants. ", expectedLength,
             length);
         for (int i = 0; i < grants.getLength(); i++) {
             String nodeXml = toString(grants.item(i), false);
             expectedGrants.put(getObjidValue(nodeXml), grants.item(i));
         }
         return expectedGrants;
     }
 
     /**
      * Tests creating a grant.<br>
      * The grant is created using the template escidoc_grant_for_create.xml
      * 
      * @param handle
      *            The escidoc user handle.
      * @param id
      *            The user id or group id for that the grant shall be
      *            created.
      * @param scope
      *            The scope to the object on that the role shall be granted. <br>
      *            If this value is not provided, the value from the template is
      *            used.
      * @param role
      *            The role.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String grantXml
      * @throws Exception
      *             If anything fails.
      */
     public String doTestCreateGrant(
         final String handle, final String id, final String scope, 
         final String role,
         final Class expectedExceptionClass) throws Exception {
         String replacedHandle = handle;
         if (replacedHandle == null) {
             replacedHandle = PWCallback.DEFAULT_HANDLE;
         }
         int numberOfGrants = getGrantCount(id);
         String replacedRole = role;
         String replacedScope = scope;
         if (getTransport() == Constants.TRANSPORT_SOAP) {
             replacedRole = replacedRole.replaceFirst(".*\\/", "");
             if (replacedScope != null) {
                 replacedScope = replacedScope.replaceFirst(".*\\/", "");
             }
         }
         String grantXml =
             getTemplateAsFixedGrantString(TEMPLATE_USER_ACCOUNT_PATH,
                 "escidoc_replaceable_grant_for_create.xml");
         grantXml = grantXml.replaceAll(
                 "\\$\\{rolehref\\}", replacedRole);
         if (replacedScope == null) {
             grantXml = grantXml.replaceAll("(?s)<[^>]*?assigned-on.*?>", "");
         } else {
             grantXml = grantXml.replaceAll("\\$\\{scopehref\\}", replacedScope);
         }
         PWCallback.setHandle(replacedHandle);
         try {
             grantXml = createGrant(id, grantXml);
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
             assertNumberOfGrants(id, ++numberOfGrants);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestBase.failException(e);
             }
             else {
                 EscidocRestSoapTestBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return grantXml;
     }
 
     /**
      * Tests creating a grant.<br>
      * The grant is created using the template escidoc_grant_for_create.xml
      * 
      * @param handle
      *            The escidoc user handle.
      * @param id
      *            The user id or group id for that the grant shall be
      *            retreived.
      * @param grantId
      *            the id of the grant to retrieve.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @return String grantXml
      * @throws Exception
      *             If anything fails.
      */
     protected String doTestRetrieveGrant(
         final String handle, final String id, final String grantId, 
         final Class expectedExceptionClass) throws Exception {
         String replacedHandle = handle;
         if (replacedHandle == null) {
             replacedHandle = PWCallback.DEFAULT_HANDLE;
         }
         PWCallback.setHandle(replacedHandle);
         String grantXml = null;
         try {
             grantXml = retrieveGrant(id, grantId);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestBase.failException(e);
             }
             else {
                 EscidocRestSoapTestBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         }
         finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
         return grantXml;
     }
 
     /**
      * Tests revoking a grant.<br>
      * 
      * @param userHandle
      *            The escidoc user handle.
      * @param id
      *            The user id or group id for that the grant shall be
      *            revoked.
      * @param grantId
      *            The grant id of the grant to revoke.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRevokeGrant(
         final String userHandle, final String id, final String grantId,
         final Class expectedExceptionClass) throws Exception {
         String taskParamXml = null;
         try {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
             String grantXml = retrieveGrant(id, grantId);
             Document document =
                 EscidocRestSoapTestBase.getDocument(grantXml);
             String lastModificationDate =
                 getLastModificationDateValue(document);
             taskParamXml =
                 "<param last-modification-date=\""
                     + lastModificationDate
                     + "\" >"
                     + "<revocation-remark>Some revocation\n "
                     + "remark</revocation-remark>"
                     + "</param>";
         } catch (Exception e) {
             throw new Exception("couldnt retrieve grant");
         }
 
         try {
             revokeGrant(id, grantId, taskParamXml, userHandle);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                     .failMissingException(expectedExceptionClass);
             }
         }
         catch (Exception e) {
             if (expectedExceptionClass == null) {
                 EscidocRestSoapTestBase.failException(e);
             }
             else {
                 EscidocRestSoapTestBase.assertExceptionType(
                     expectedExceptionClass, e);
             }
         }
     }
 
     /**
      * Tests retrieving an object with role.
      * 
      * @param userOrGroupId
      *            id of the user or group.
      * @param roleHref
      *            href of role to assign.
      * @param scopeHref
      *            href of scope to assign with role.
      * @param handle
      *            handle of user.
      * @param handlerCodeInt
      *            code of handler to call.
      * @param idToRetrieve
      *            idToRetrieve.
      * @param retrieveVersion
      *            true if version 1 should be retrieved.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestRetrieveWithRole(
         final String userOrGroupId, final String roleHref,
         final String scopeHref, final String handle, 
         final int handlerCodeInt, final String idToRetrieve, 
         final boolean retrieveVersion,
         final Class expectedExceptionClass)
         throws Exception {
 
         String grantId = null;
         try {
             if (roleHref != null) {
                 //create grant for user/group and scope
                 String grantXml = doTestCreateGrant(
                     null, userOrGroupId, scopeHref, roleHref, null);
                 Document document =
                     EscidocRestSoapTestBase.getDocument(grantXml);
                 grantId = getObjidValue(document);
             }
                     
             //test retrieving the object
             try {
                 PWCallback.setHandle(handle);
                 retrieve(handlerCodeInt, idToRetrieve);
                 if (expectedExceptionClass != null) {
                     EscidocRestSoapTestBase
                     .failMissingException(expectedExceptionClass);
                 }
             } catch (Exception e) {
                 if (expectedExceptionClass != null) {
                     EscidocRestSoapTestBase.assertExceptionType(
                             expectedExceptionClass, e);
                 } else {
                     EscidocRestSoapTestBase.failException(
                             "retrieving object failed. ", e);
                 }
             } finally {
                 PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
             }
             
             if (retrieveVersion) {
                 //test successfully retrieving a version of the object
                 try {
                     PWCallback.setHandle(handle);
                     retrieve(handlerCodeInt, idToRetrieve + ":1");
                     if (expectedExceptionClass != null) {
                         EscidocRestSoapTestBase
                         .failMissingException(expectedExceptionClass);
                     }
                 } catch (Exception e) {
                     if (expectedExceptionClass != null) {
                         EscidocRestSoapTestBase.assertExceptionType(
                                 expectedExceptionClass, e);
                     } else {
                         EscidocRestSoapTestBase.failException(
                                 "retrieving object failed. ", e);
                     }
                 } finally {
                     PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
                 }
             }
         } finally {
             if (roleHref != null) {
                 doTestRevokeGrant(null, userOrGroupId, grantId, null);
             }
         }
         
     }
 
     /**
      * Tests retrieving an object with role.
      * 
      * @param userOrGroupId
      *            id of the user or group.
      * @param roleHref
      *            href of role to assign.
      * @param scopeHref
      *            href of scope to assign with role.
      * @param handle
      *            handle of user.
      * @param handlerCodeInt
      *            code of handler to call.
      * @param idToUpdate
      *            idToRetrieve.
      * @param replaceString
      *            string to replace for update.
      * @param replacement
      *            replacement.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestUpdateWithRole(
         final String userOrGroupId, final String roleHref,
         final String scopeHref, final String handle, 
         final int handlerCodeInt, final String idToUpdate,
         final String replaceString, final String replacement,
         final Class expectedExceptionClass)
         throws Exception {
 
         String grantId = null;
         try {
             if (roleHref != null) {
                 //create grant for user/group and scope
                 String grantXml = doTestCreateGrant(
                     null, userOrGroupId, scopeHref, roleHref, null);
                 Document document =
                     EscidocRestSoapTestBase.getDocument(grantXml);
                 grantId = getObjidValue(document);
             }
 
             String result = null;
             result = retrieve(handlerCodeInt, idToUpdate);
             result =
                 result.replaceAll(replaceString,
                         replaceString + replacement);
 
             //test updating the object
             try {
                 PWCallback.setHandle(handle);
                 result = update(handlerCodeInt, idToUpdate, result);
                 if (expectedExceptionClass != null) {
                     EscidocRestSoapTestBase
                     .failMissingException(expectedExceptionClass);
                 }
             } catch (Exception e) {
                 if (expectedExceptionClass != null) {
                     EscidocRestSoapTestBase.assertExceptionType(
                             expectedExceptionClass, e);
                 } else {
                     EscidocRestSoapTestBase.failException(
                             "updating object failed. ", e);
                 }
             } finally {
                 PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
             }
         } finally {
             if (roleHref != null) {
                 doTestRevokeGrant(null, userOrGroupId, grantId, null);
             }
         }
     }
 
     /**
      * Tests locking/unlocking an object with role.
      * 
      * @param userOrGroupId
      *            id of the user or group.
      * @param roleHref
      *            href of role to assign.
      * @param scopeHref
      *            href of scope to assign with role.
      * @param handle
      *            handle of user.
      * @param handlerCodeInt
      *            code of handler to call.
      * @param idToLockUnlock
      *            idToRetrieve.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestLockUnlockWithRole(
         final String userOrGroupId, final String roleHref,
         final String scopeHref, final String handle, 
         final int handlerCodeInt, final String idToLockUnlock,
         final Class expectedExceptionClass)
         throws Exception {
 
         if (handlerCodeInt != ITEM_HANDLER_CODE 
                 && handlerCodeInt != CONTAINER_HANDLER_CODE) {
             throw new Exception("handler with code " 
                     + handlerCodeInt + " not allowed");
         }
 
         //create grant for user/group and scope
         doTestCreateGrant(
             null, userOrGroupId, scopeHref, roleHref, null);
                 
         String result = retrieve(handlerCodeInt, idToLockUnlock);
         Document document =
             EscidocRestSoapTestBase.getDocument(result);
         String taskParam = 
             getTaskParam(getLastModificationDateValue(document));
         
         //test locking the object
         try {
             PWCallback.setHandle(handle);
             
             //lock
             if (handlerCodeInt == ITEM_HANDLER_CODE) {
                 getItemClient().lock(idToLockUnlock, taskParam);
             } else if (handlerCodeInt == CONTAINER_HANDLER_CODE) {
                 getContainerClient().lock(idToLockUnlock, taskParam);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                 .failMissingException(expectedExceptionClass);
             }
         } catch (Exception e) {
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase.assertExceptionType(
                         expectedExceptionClass, e);
             } else {
                 EscidocRestSoapTestBase.failException(
                         "locking object failed. ", e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
 
         result = retrieve(handlerCodeInt, idToLockUnlock);
         document =
             EscidocRestSoapTestBase.getDocument(result);
         taskParam = 
             getTaskParam(getLastModificationDateValue(document));
         //unlock
         try {
             PWCallback.setHandle(handle);
 
             if (handlerCodeInt == ITEM_HANDLER_CODE) {
                 getItemClient().unlock(idToLockUnlock, taskParam);
             } else if (handlerCodeInt == CONTAINER_HANDLER_CODE) {
                 getContainerClient().unlock(idToLockUnlock, taskParam);
             }
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                 .failMissingException(expectedExceptionClass);
             }
         } catch (Exception e) {
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase.assertExceptionType(
                         expectedExceptionClass, e);
             } else {
                 EscidocRestSoapTestBase.failException(
                         "unlocking object failed. ", e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Tests locking/unlocking an object with role.
      * 
      * @param userOrGroupId
      *            id of the user or group.
      * @param roleHref
      *            href of role to assign.
      * @param scopeHref
      *            href of scope to assign with role.
      * @param handle
      *            handle of user.
      * @param handlerCodeInt
      *            code of handler to call.
      * @param containerId
      *            id of container where to add member.
      * @param expectedExceptionClass
      *            The class of the expected exception or <code>null</code> in
      *            case of expected success.
      * @throws Exception
      *             If anything fails.
      */
     protected void doTestAddMemberWithRole(
         final String userOrGroupId, final String roleHref,
         final String scopeHref, final String handle, 
         final int handlerCodeInt, final String containerId,
         final Class expectedExceptionClass)
         throws Exception {
 
         //create grant for user/group and scope
         doTestCreateGrant(
             null, userOrGroupId, scopeHref, roleHref, null);
                 
         //1. create item or container to add as member
         String xml = null;
         if (handlerCodeInt == ITEM_HANDLER_CODE) {
             xml = 
                 prepareItem(
                     PWCallback.DEFAULT_HANDLE, 
                     STATUS_PENDING, 
                     null,
                     false, false);
         } 
         else if (handlerCodeInt == CONTAINER_HANDLER_CODE) {
             xml = 
                 prepareContainer(
                     PWCallback.DEFAULT_HANDLE, 
                     STATUS_PENDING, null,
                     false, false);
         }
         else {
             throw new Exception("handler with code " 
                     + handlerCodeInt + " not allowed");
         }
         create(handlerCodeInt, xml);
         
         //2. retrieve container for lastModificationDate
         String containerXml = retrieve(
                 CONTAINER_HANDLER_CODE, containerId);
         Document document =
             EscidocRestSoapTestBase.getDocument(containerXml);
         String lastModificationDate = getTheLastModificationDate(document);
         String taskParam = 
             "<param last-modification-date=\"" 
             + lastModificationDate 
             + "\"><id>" 
             + getObjidValue(xml)
             + "</id></param>";
 
         //test adding member to container
         String result = null;
         try {
             PWCallback.setHandle(handle);
             
             //add as member
             getContainerClient().addMembers(containerId, taskParam);
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase
                 .failMissingException(expectedExceptionClass);
             }
         } catch (Exception e) {
             if (expectedExceptionClass != null) {
                 EscidocRestSoapTestBase.assertExceptionType(
                         expectedExceptionClass, e);
             } else {
                 EscidocRestSoapTestBase.failException(
                         "adding member to object failed. ", e);
             }
         } finally {
             PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         }
     }
 
     /**
      * Assert that the grant has all required elements (with the expected
      * values).
      * 
      * @param toBeAssertedXml
      *            The created/updated grant.
      * @param originalXml
      *            The template used to create/update the grant. If this
      *            parameter is <code>null</code>, no check with the original
      *            data is performed.
      * @param id
      *            The id of the user account or user group 
      *            to that the grant belongs to.
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
     public Document assertGrant(
         final String toBeAssertedXml, final String originalXml,
         final String id, final String timestampBeforeCreation,
         final String timestampBeforeLastModification,
         final boolean assertCreationDate) throws Exception {
 
         assertXmlValidGrants(toBeAssertedXml);
         Document toBeAssertedDocument =
             EscidocRestSoapTestBase.getDocument(toBeAssertedXml);
 
         String baseUri = null;
         
         if (handlerCode == USER_GROUP_HANDLER_CODE) {
             
             baseUri = Constants.USER_GROUP_BASE_URI;
         } else {
             baseUri = Constants.USER_ACCOUNT_BASE_URI;;
         }
         assertRootElement("Invalid grant root element. ", toBeAssertedDocument,
             XPATH_GRANT, baseUri + "/" + id
                 + PARTS_GRANTS_GRANT, timestampBeforeLastModification);
 
         // properties
         // this assers creation-date and created-by
         assertPropertiesElement("Asserting OU properties failed. ",
             toBeAssertedDocument, XPATH_GRANT_PROPERTIES,
             timestampBeforeCreation);
 
         // revocation-date and revoked-by (if one of both exists)
         final Node revokedByNode =
             selectSingleNode(toBeAssertedDocument, XPATH_GRANT_PROPERTIES + "/"
                 + "revoked-by");
         final Node revocationDateNode =
             selectSingleNode(toBeAssertedDocument, XPATH_GRANT_REVOCATION_DATE);
         if (revokedByNode == null) {
             assertNull("Unexpected revocation date (no revoked-by). ",
                 revocationDateNode);
         }
         else {
             assertNotNull("Missing revocation date (revoked by exists). ",
                 revocationDateNode);
         }
 
         // assert last modification date matches either creation date or
         // revocation date
         if (revocationDateNode != null) {
             // assert with revocation date
             assertXmlEquals(
                 "Last modification date does not match revocation date. ",
                 toBeAssertedDocument, XPATH_GRANT_LAST_MODIFICATION_DATE,
                 revocationDateNode.getTextContent());
         }
         else {
             // assert with creation date
             assertXmlEquals(
                 "Last modification date does not match creation date. ",
                 toBeAssertedDocument, XPATH_GRANT_LAST_MODIFICATION_DATE,
                 getCreationDateValue(toBeAssertedDocument));
         }
 
         // assert role reference
         assertReferencingElement("Asserting role rerference failed. ",
             toBeAssertedDocument, XPATH_GRANT_ROLE, Constants.ROLE_BASE_URI);
 
         // assert object reference (if exists)
         final Node objectReferenceNode =
             selectSingleNode(toBeAssertedDocument, XPATH_GRANT_OBJECT);
         if (objectReferenceNode != null) {
             assertReferencingElement(
                 "Object reference is not a valid referencing element. ",
                 toBeAssertedDocument, XPATH_GRANT_OBJECT, null);
         }
 
         if (originalXml != null) {
             final Document originalDocument =
                 EscidocRestSoapTestBase.getDocument(originalXml);
 
             if (assertCreationDate) {
                 final String expectedCreationDate =
                     getCreationDateValue(originalDocument);
                 if (expectedCreationDate != null) {
 
                     // creation-date
                     assertXmlEquals("Creation date mismatch, ",
                         toBeAssertedDocument, XPATH_GRANT_CREATION_DATE,
                         expectedCreationDate);
 
                     // created-by
                     assertCreatedBy("Created-by invalid", originalDocument,
                         toBeAssertedDocument);
                 }
             }
 
             // grant-remark
             assertXmlEquals("Grant remark mismatch, ", originalDocument,
                 toBeAssertedDocument, XPATH_GRANT_GRANT_REMARK);
 
             // revocation-remark
             assertXmlEquals("Revocation remark mismatch, ", originalDocument,
                 toBeAssertedDocument, XPATH_GRANT_REVOCATION_REMARK);
 
             // role reference
             if (Constants.TRANSPORT_REST == getTransport()) {
                 // in case of rest, href is mandatory but the other xlink
                 // attributes are discarded, i.e. they may be contained in XML
                 // but will be ignored. Therefore, only href is compared with
                 // original values.
                 assertXmlEquals("Role reference mismatch, ", originalDocument,
                     toBeAssertedDocument, XPATH_GRANT_ROLE_XLINK_HREF);
             }
             else {
                 assertXmlEquals("Role reference mismatch, ", originalDocument,
                     toBeAssertedDocument, XPATH_GRANT_ROLE);
             }
 
             // object reference
             if (Constants.TRANSPORT_REST == getTransport()) {
                 // href mandatory, only
                 assertXmlEquals("Object reference href mismatch, ",
                     originalDocument, toBeAssertedDocument,
                     XPATH_GRANT_OBJECT_XLINK_HREF);
             }
             else {
                 assertXmlEquals("Object reference objid mismatch, ",
                     originalDocument, toBeAssertedDocument,
                     XPATH_GRANT_OBJECT_OBJID);
             }
         }
 
         return toBeAssertedDocument;
     }
 
     /**
      * Assert that the user or group with given id 
      * has the expected number of grants.
      * 
      * @param id
      *            The id of the user or group 
      *            the number of grants has to be checked.
      * @param expectedNumberOfGrants
      *            expectedNumberOfGrants.
      * @throws Exception
      *             If anything fails.
      */
     private void assertNumberOfGrants(
             final String id, final int expectedNumberOfGrants) throws Exception {
         assertEquals(
                 "number of grants is not as expected", 
                     expectedNumberOfGrants, getGrantCount(id));
     }
     
     /**
      * retrieve the number of current grants of the user or group with given id.
      * 
      * @param id
      *            The id of the user or group 
      *            the number of grants has to be checked.
      * @return int numerOfGrants
      * @throws Exception
      *             If anything fails.
      */
     private int getGrantCount(
             final String id) throws Exception {
        String grantsXml = retrieveCurrentGrants(id);
        grantMatcher.reset(grantsXml);
        int matches = 0;
        while (grantMatcher.find()) {
            matches++;
         }
        return matches;
     }
     /**
      * @param client the client to set
      */
     protected void setClient(final GrantClient client) {
         this.client = client;
     }
     
 }
