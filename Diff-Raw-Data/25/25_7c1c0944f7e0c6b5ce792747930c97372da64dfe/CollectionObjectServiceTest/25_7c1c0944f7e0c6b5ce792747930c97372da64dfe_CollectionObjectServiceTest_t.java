 /**
  * This document is a part of the source code and related artifacts
  * for CollectionSpace, an open source collections management system
  * for museums and related institutions:
  *
  * http://www.collectionspace.org
  * http://wiki.collectionspace.org
  *
  * Copyright © 2009 Regents of the University of California
  *
  * Licensed under the Educational Community License (ECL), Version 2.0.
  * You may not use this file except in compliance with this License.
  *
  * You may obtain a copy of the ECL 2.0 License at
  * https://source.collectionspace.org/collection-space/LICENSE.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.collectionspace.services.client.test;
 
 import java.util.List;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.collectionspace.services.client.CollectionObjectClient;
 import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;
 import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
 import org.collectionspace.services.collectionobject.OtherNumberList;
 import org.jboss.resteasy.client.ClientResponse;
 
 import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
 import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
 import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * CollectionObjectServiceTest, carries out tests against a
  * deployed and running CollectionObject Service.
  * 
  * $LastChangedRevision$
  * $LastChangedDate$
  */
 public class CollectionObjectServiceTest extends AbstractServiceTest {
 
     private final Logger logger =
             LoggerFactory.getLogger(CollectionObjectServiceTest.class);
     // Instance variables specific to this test.
     private CollectionObjectClient client = new CollectionObjectClient();
     private String knownResourceId = null;
 
     /*
      * This method is called only by the parent class, AbstractServiceTest
      */
     @Override
     protected String getServicePathComponent() {
         return client.getServicePathComponent();
     }
 
     // ---------------------------------------------------------------
     // CRUD tests : CREATE tests
     // ---------------------------------------------------------------
     // Success outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class)
     public void create(String testName) throws Exception {
 
         // Perform setup, such as initializing the type of service request
         // (e.g. CREATE, DELETE), its valid and expected status codes, and
         // its associated HTTP method name (e.g. POST, DELETE).
         setupCreate(testName);
 
         // Submit the request to the service and store the response.
         String identifier = createIdentifier();
         MultipartOutput multipart =
                 createCollectionObjectInstance(client.getCommonPartName(), identifier);
         ClientResponse<Response> res = client.create(multipart);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         //
         // Specifically:
         // Does it fall within the set of valid status codes?
         // Does it exactly match the expected status code?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
 
         // Store the ID returned from this create operation
         // for additional tests below.
         knownResourceId = extractId(res);
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": knownResourceId=" + knownResourceId);
         }
     }
 
     /* (non-Javadoc)
      * @see org.collectionspace.services.client.test.ServiceTest#createList()
      */
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"create"})
     public void createList(String testName) throws Exception {
         for (int i = 0; i < 3; i++) {
             create(testName);
         }
     }
 
     // Failure outcomes
     // Placeholders until the three tests below can be uncommented.
     // See Issue CSPACE-401.
     @Override
     public void createWithEmptyEntityBody(String testName) throws Exception {
     }
 
     @Override
     public void createWithMalformedXml(String testName) throws Exception {
     }
 
     @Override
     public void createWithWrongXmlSchema(String testName) throws Exception {
     }
 
 
     /*
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "testSubmitRequest"})
     public void createWithEmptyEntityBody(String testName) throwsException {
     
     // Perform setup.
     setupCreateWithEmptyEntityBody(testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getServiceRootURL();
     String mediaType = MediaType.APPLICATION_XML;
     final String entity = "";
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "testSubmitRequest"})
     public void createWithMalformedXml(String testName) throws Exception {
     
     // Perform setup.
     setupCreateWithMalformedXml(testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getServiceRootURL();
     String mediaType = MediaType.APPLICATION_XML;
     final String entity = MALFORMED_XML_DATA; // Constant from base class.
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "testSubmitRequest"})
     public void createWithWrongXmlSchema(String testName) throws Exception {
     
     // Perform setup.
     setupCreateWithWrongXmlSchema(testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getServiceRootURL();
     String mediaType = MediaType.APPLICATION_XML;
     final String entity = WRONG_XML_SCHEMA_DATA;
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
      */
     // ---------------------------------------------------------------
     // CRUD tests : READ tests
     // ---------------------------------------------------------------
     // Success outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"create"})
     public void read(String testName) throws Exception {
 
         // Perform setup.
         setupRead(testName);
 
         // Submit the request to the service and store the response.
         ClientResponse<MultipartInput> res = client.read(knownResourceId);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
 
         MultipartInput input = (MultipartInput) res.getEntity();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Reading Common part ...");
        }
         CollectionobjectsCommon collectionObject =
                 (CollectionobjectsCommon) extractPart(input,
                 client.getCommonPartName(), CollectionobjectsCommon.class);
         Assert.assertNotNull(collectionObject);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Reading Natural History part ...");
        }
        CollectionobjectsNaturalhistory conh =
                (CollectionobjectsNaturalhistory) extractPart(input,
                getNHPartName(), CollectionobjectsNaturalhistory.class);
        Assert.assertNotNull(conh);
     }
 
     // Failure outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"read"})
     public void readNonExistent(String testName) throws Exception {
 
         // Perform setup.
         setupReadNonExistent(testName);
 
         // Submit the request to the service and store the response.
         ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     // ---------------------------------------------------------------
     // CRUD tests : READ_LIST tests
     // ---------------------------------------------------------------
     // Success outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"createList", "read"})
     public void readList(String testName) throws Exception {
 
         // Perform setup.
         setupReadList(testName);
 
         // Submit the request to the service and store the response.
         ClientResponse<CollectionobjectsCommonList> res = client.readList();
         CollectionobjectsCommonList list = res.getEntity();
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
 
         // Optionally output additional data about list members for debugging.
         boolean iterateThroughList = false;
         if (iterateThroughList && logger.isDebugEnabled()) {
             List<CollectionobjectsCommonList.CollectionObjectListItem> items =
                     list.getCollectionObjectListItem();
             int i = 0;
 
             for (CollectionobjectsCommonList.CollectionObjectListItem item : items) {
                 logger.debug(testName + ": list-item[" + i + "] csid=" +
                         item.getCsid());
                 logger.debug(testName + ": list-item[" + i + "] objectNumber=" +
                         item.getObjectNumber());
                 logger.debug(testName + ": list-item[" + i + "] URI=" +
                         item.getUri());
                 i++;
 
             }
         }
     }
 
     // Failure outcomes
     // None at present.
     // ---------------------------------------------------------------
     // CRUD tests : UPDATE tests
     // ---------------------------------------------------------------
     // Success outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"read"})
     public void update(String testName) throws Exception {
 
         // Perform setup.
         setupUpdate(testName);
 
         ClientResponse<MultipartInput> res =
                 client.read(knownResourceId);
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": read status = " + res.getStatus());
         }
         Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);
 
         if (logger.isDebugEnabled()) {
             logger.debug("got object to update with ID: " + knownResourceId);
         }
         MultipartInput input = (MultipartInput) res.getEntity();
         CollectionobjectsCommon collectionObject =
                 (CollectionobjectsCommon) extractPart(input,
                 client.getCommonPartName(), CollectionobjectsCommon.class);
         Assert.assertNotNull(collectionObject);
 
         // Update the content of this resource.
         collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
         collectionObject.setObjectName("updated-" + collectionObject.getObjectName());
         if (logger.isDebugEnabled()) {
             logger.debug("updated object");
             logger.debug(objectAsXmlString(collectionObject,
                 CollectionobjectsCommon.class));
         }
 
         // Submit the request to the service and store the response.
         MultipartOutput output = new MultipartOutput();
         OutputPart commonPart = output.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
         commonPart.getHeaders().add("label", client.getCommonPartName());
 
         res = client.update(knownResourceId, output);
         int statusCode = res.getStatus();
         // Check the status code of the response: does it match the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
 
 
         input = (MultipartInput) res.getEntity();
         CollectionobjectsCommon updatedCollectionObject =
                 (CollectionobjectsCommon) extractPart(input,
                 client.getCommonPartName(), CollectionobjectsCommon.class);
         Assert.assertNotNull(updatedCollectionObject);
 
         Assert.assertEquals(updatedCollectionObject.getObjectName(),
                 collectionObject.getObjectName(),
                 "Data in updated object did not match submitted data.");
 
     }
 
     // Failure outcomes
     // Placeholders until the three tests below can be uncommented.
     // See Issue CSPACE-401.
     @Override
     public void updateWithEmptyEntityBody(String testName) throws Exception {
     }
 
     @Override
     public void updateWithMalformedXml(String testName) throws Exception {
     }
 
     @Override
     public void updateWithWrongXmlSchema(String testName) throws Exception {
     }
 
     /*
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "update", "testSubmitRequest"})
     public void updateWithEmptyEntityBody(String testName) throws Exception {
     
     // Perform setup.
     setupUpdateWithEmptyEntityBody(testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getResourceURL(knownResourceId);
     String mediaType = MediaType.APPLICATION_XML;
     final String entity = "";
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "update", "testSubmitRequest"})
     public void updateWithMalformedXml() throws Exception {
 
     // Perform setup.
     setupUpdateWithMalformedXml(testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getResourceURL(knownResourceId);
     final String entity = MALFORMED_XML_DATA;
     String mediaType = MediaType.APPLICATION_XML;
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     @Override
     @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
     dependsOnMethods = {"create", "update", "testSubmitRequest"})
     public void updateWithWrongXmlSchema(String testName) throws Exception {
     
     // Perform setup.
     setupUpdateWithWrongXmlSchema(String testName);
 
     // Submit the request to the service and store the response.
     String method = REQUEST_TYPE.httpMethodName();
     String url = getResourceURL(knownResourceId);
     String mediaType = MediaType.APPLICATION_XML;
     final String entity = WRONG_XML_SCHEMA_DATA;
     int statusCode = submitRequest(method, url, mediaType, entity);
 
     // Check the status code of the response: does it match
     // the expected response(s)?
     if(logger.isDebugEnabled()){
     logger.debug(testName + ": url=" + url +
     " status=" + statusCode);
     }
     Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
     invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
     Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
      */
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"update", "testSubmitRequest"})
     public void updateNonExistent(String testName) throws Exception {
 
         // Perform setup.
         setupUpdateNonExistent(testName);
 
         // Submit the request to the service and store the response.
         //
         // Note: The ID used in this 'create' call may be arbitrary.
         // The only relevant ID may be the one used in updateCollectionObject(), below.
         MultipartOutput multipart =
                 createCollectionObjectInstance(client.getCommonPartName(),
                 NON_EXISTENT_ID);
         ClientResponse<MultipartInput> res =
                 client.update(NON_EXISTENT_ID, multipart);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     // ---------------------------------------------------------------
     // CRUD tests : DELETE tests
     // ---------------------------------------------------------------
     // Success outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
     public void delete(String testName) throws Exception {
 
         // Perform setup.
         setupDelete(testName);
 
         // Submit the request to the service and store the response.
         ClientResponse<Response> res = client.delete(knownResourceId);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     // Failure outcomes
     @Override
     @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTest.class,
     dependsOnMethods = {"delete"})
     public void deleteNonExistent(String testName) throws Exception {
 
         // Perform setup.
         setupDeleteNonExistent(testName);
 
         // Submit the request to the service and store the response.
         ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
         int statusCode = res.getStatus();
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug(testName + ": status = " + statusCode);
         }
         Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                 invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
         Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     }
 
     // ---------------------------------------------------------------
     // Utility tests : tests of code used in tests above
     // ---------------------------------------------------------------
     /**
      * Tests the code for manually submitting data that is used by several
      * of the methods above.
      */
     @Test(dependsOnMethods = {"create", "read"})
     public void testSubmitRequest() throws Exception {
 
         // Expected status code: 200 OK
         final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();
 
         // Submit the request to the service and store the response.
         String method = ServiceRequestType.READ.httpMethodName();
         String url = getResourceURL(knownResourceId);
         int statusCode = submitRequest(method, url);
 
         // Check the status code of the response: does it match
         // the expected response(s)?
         if (logger.isDebugEnabled()) {
             logger.debug("testSubmitRequest: url=" + url +
                     " status=" + statusCode);
         }
         Assert.assertEquals(statusCode, EXPECTED_STATUS);
 
     }
 
     // ---------------------------------------------------------------
     // Utility methods used by tests above
     // ---------------------------------------------------------------
     private MultipartOutput createCollectionObjectInstance(String commonPartName,
             String identifier) {
         return createCollectionObjectInstance(commonPartName,
                 "objectNumber-" + identifier,
                 "objectName-" + identifier);
     }
 
     private MultipartOutput createCollectionObjectInstance(String commonPartName,
             String objectNumber, String objectName) {
         CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
         OtherNumberList onList = new OtherNumberList();
         List<String> ons = onList.getOtherNumber();
         ons.add("urn:org.collectionspace.id:24082390");
         ons.add("urn:org.walkerart.id:123");
         collectionObject.setOtherNumbers(onList);
         collectionObject.setObjectNumber(objectNumber);
         collectionObject.setObjectName(objectName);
         collectionObject.setAge(""); //test for null string
         collectionObject.setBriefDescription("Papier mache bird mask with horns, " +
                 "painted red with black and yellow spots. " +
                 "Puerto Rico. ca. 8&quot; high, 6&quot; wide, projects 10&quot; (with horns).");
         MultipartOutput multipart = new MultipartOutput();
         OutputPart commonPart = multipart.addPart(collectionObject,
                 MediaType.APPLICATION_XML_TYPE);
         commonPart.getHeaders().add("label", commonPartName);
 
         if (logger.isDebugEnabled()) {
             logger.debug("to be created, collectionobject common");
             logger.debug(objectAsXmlString(collectionObject,
                 CollectionobjectsCommon.class));
         }
 
        CollectionobjectsNaturalhistory conh = new CollectionobjectsNaturalhistory();
         conh.setNhString("test-string");
         conh.setNhInt(999);
         conh.setNhLong(9999);
         OutputPart nhPart = multipart.addPart(conh, MediaType.APPLICATION_XML_TYPE);
         nhPart.getHeaders().add("label", getNHPartName());
 
         if (logger.isDebugEnabled()) {
             logger.debug("to be created, collectionobject nhistory");
             logger.debug(objectAsXmlString(conh,
                CollectionobjectsNaturalhistory.class));
         }
         return multipart;
 
     }
 
     private String getNHPartName() {
        return "collectionobjects_naturalhistory";
     }
 }
