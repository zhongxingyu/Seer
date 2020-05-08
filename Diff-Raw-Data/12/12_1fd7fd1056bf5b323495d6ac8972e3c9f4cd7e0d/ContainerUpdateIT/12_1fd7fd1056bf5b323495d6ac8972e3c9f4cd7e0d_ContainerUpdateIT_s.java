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
 package de.escidoc.core.test.om.container;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.remote.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.remote.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.common.fedora.TripleStoreTestBase;
 import org.apache.xpath.XPathAPI;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the mock implementation of the container resource.
  *
  * @author Michael Schneider
  */
 public class ContainerUpdateIT extends ContainerTestBase {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ContainerUpdateIT.class);
 
     private String theContainerXml;
 
     private String theContainerId;
 
     private String theItemId;
 
     public static final String XPATH_TRIPLE_STORE_DC_CREATOR = "/RDF/Description/creator";
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void setUp() throws Exception {
 
         this.theItemId = createItemFromTemplate("escidoc_item_198_for_create.xml");
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
         String replaced = xmlData.replaceAll("##ITEMID##", theItemId);
 
         this.theContainerXml = create(replaced);
         this.theContainerId = getObjidValue(this.theContainerXml);
 
     }
 
     @Test
     public void testUpdateCts() throws Exception {
 
         Document newContainer = EscidocAbstractTest.getDocument(theContainerXml);
         Node cts = selectSingleNode(newContainer, "/container/properties/content-model-specific");
         cts.appendChild(newContainer.createElement("nischt"));
         String newContainerXml = toString(newContainer, false);
         String xml = update(theContainerId, newContainerXml);
         selectSingleNodeAsserted(EscidocAbstractTest.getDocument(xml),
             "/container/properties/content-model-specific/nischt");
         assertXmlValidContainer(xml);
     }
 
     @Test
     public void testAddMember() throws Exception {
         String xml = theContainerXml;
         Document containerDoc = getDocument(xml);
         String lmdRetrieve1 = getLastModificationDateValue(containerDoc);
         String itemToAddID = createItemFromTemplate("escidoc_item_198_for_create.xml");
 
         ArrayList<String> ids = new ArrayList<String>();
         ids.add(itemToAddID);
 
         String resultXml =
             addMembers(theContainerId, getMembersTaskParam(getLastModificationDateValue2(containerDoc), ids));
 
         // check if item with id theItemId is member of theContainer
         String containerXml = retrieve(theContainerId);
         Document containerDocAfterAddMember = EscidocAbstractTest.getDocument(containerXml);
         Node addedMember =
             selectSingleNodeAsserted(containerDocAfterAddMember, "/container/struct-map/item[@href = '" + "/ir/item/"
                 + itemToAddID + "']");
         assertNotNull(addedMember);
 
         Document resultDoc = getDocument(resultXml);
         String lmdMethod = getLastModificationDateValue(resultDoc);
         assertXmlValidResult(resultXml);
 
         assertDateBeforeAfter(lmdRetrieve1, lmdMethod);
 
         String lmdRetrieve2 = getLastModificationDateValue(containerDocAfterAddMember);
 
         assertEquals("Last modification date of retrieve does not match the"
             + " last modification date of the latest task oriented method", lmdMethod, lmdRetrieve2);
     }
 
     /**
      * Successfully removing of container members.
      */
     @Test
     public void testRemoveMember() throws Exception {
 
         String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
         String createdContainerXml = create(xmlData);
         String subContainerId = getObjidValue(createdContainerXml);
 
         ArrayList<String> ids = new ArrayList<String>();
         ids.add(subContainerId);
 
         String resultXml =
             addMembers(theContainerId, getMembersTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), ids));
 
         Document resultDoc = getDocument(resultXml);
         String lmdMethod1 = getLastModificationDateValue(resultDoc);
 
         ids = new ArrayList<String>();
         ids.add(subContainerId);
         ids.add(theItemId);
         String resultXmlAfterRemoveMembers =
             removeMembers(theContainerId, getMembersTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), ids));
 
         assertXmlValidResult(resultXml);
         Document resultDocAfterRemoveMembers = getDocument(resultXmlAfterRemoveMembers);
         String lmdMethod2 = getLastModificationDateValue(resultDocAfterRemoveMembers);
         assertDateBeforeAfter(lmdMethod1, lmdMethod2);
 
         String containerXmlAfterRemoveMembers = retrieve(theContainerId);
         Document containerDocAfterRemoveMembers = getDocument(containerXmlAfterRemoveMembers);
         String lmdRetrieve2 = getLastModificationDateValue(containerDocAfterRemoveMembers);
 
         assertEquals("Last modification date of retrieve does not match the"
             + " last modification date of the latest task oriented method", lmdMethod2, lmdRetrieve2);
         NodeList itemMembers = selectNodeList(containerDocAfterRemoveMembers, "/container/struct-map/item");
         NodeList containerMembers = selectNodeList(containerDocAfterRemoveMembers, "/container/struct-map/container");
 
         assertEquals(itemMembers.getLength(), 0);
         assertEquals(containerMembers.getLength(), 0);
 
     }
 
     /**
      *
      * @throws Exception
      */
     @Test
     public void testIgnoreRemovingOfmemberWithWrongId() throws Exception {
         String container = retrieve(theContainerId);
         String containerWithoutMembers =
             getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest", "create_container_WithoutMembers_v1.1.xml");
 
         String createdContainerWithouMembers = create(containerWithoutMembers);
         String containerId = getObjidValue(createdContainerWithouMembers);
 
         Document containerDoc = getDocument(container);
 
         ArrayList<String> ids = new ArrayList<String>();
         ids.add(containerId);
 
         String resultXml =
             removeMembers(theContainerId, getMembersTaskParam(getLastModificationDateValue2(containerDoc), ids));
         Document resultDoc = getDocument(resultXml);
         String lmdMethod1 = getLastModificationDateValue(resultDoc);
         assertEquals(getLastModificationDateValue(containerDoc), lmdMethod1);
     }
 
     /**
      * Test to add 600 member to a Container (in one step).
      * 
      * @throws Exception
      */
     @Test
     public void addLargeNumberOfMembers01() throws Exception {
 
         for (int i = 0; i < 600; i++) {
             String itemToAddID = createItemFromTemplate("escidoc_item_198_for_create.xml");
 
            ArrayList<String> ids = new ArrayList<String>();
             ids.add(itemToAddID);

            addMembers(theContainerId, getMembersTaskParam(
                getLastModificationDateValue2(getDocument(this.theContainerXml)), ids));
         }
 
         String containerXml = retrieve(theContainerId);
 
         // TODO count members
     }
 
     /**
      * Test to add 100 member to a Container (each member in a single step).
      * 
      * @throws Exception
      */
     @Test
     public void addLargeNumberOfMembers02() throws Exception {
 
         DateTime lmd = getLastModificationDateValue2(getDocument(this.theContainerXml));
 
         for (int i = 0; i < 100; i++) {
             String itemToAddID = createItemFromTemplate("escidoc_item_198_for_create.xml");
 
             ArrayList<String> ids = new ArrayList<String>();
             ids.add(itemToAddID);
 
             String result = addMembers(theContainerId, getMembersTaskParam(lmd, ids));
             lmd = getLastModificationDateValue2(getDocument(result));
         }
 
         String containerXml = retrieve(theContainerId);
 
         // TODO count members
     }
 
     @Test
     public void testCreateItem() throws Exception {
 
         String xmlData = getItemTemplate("escidoc_item_198_for_create.xml");
         String item = createItem(theContainerId, xmlData);
 
         assertXmlValidItem(item);
 
         String memberItemId = getObjidValue(item);
 
         // check if item with memberItemId is a member of theContainer
         String containerXml = retrieve(theContainerId);
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         Node addedMember =
             selectSingleNodeAsserted(containerDoc, "/container/struct-map/item[@href = '" + "/ir/item/" + memberItemId
                 + "']");
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test(expected = XmlSchemaValidationException.class)
     public void testCreateItemWithInvalidItemXml() throws Exception {
 
         String xmlData = getItemTemplate("escidoc_item_198_for_create.xml");
         xmlData =
             xmlData.replaceFirst("xmlns:escidocItem=\"http://www.escidoc.de/schemas/item/0.10",
                 "xmlns:escidocItem=\"http://www.escidoc.de/schemas/item/0.8");
 
         createItem(theContainerId, xmlData);
     }
 
     @Test
     public void testCreateContainer() throws Exception {
 
         String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
         String container = createContainer(theContainerId, xmlData);
 
         assertXmlValidContainer(container);
 
         String memberContainerId = getObjidValue(container);
 
         // check if item with memberItemId is a member of theContainer
         String containerXml = retrieve(theContainerId);
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         Node addedMember =
             selectSingleNodeAsserted(containerDoc, "/container/struct-map/container[@href = '" + "/ir/container/"
                 + memberContainerId + "']");
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testCreateItemWithoutContainerId() throws Exception {
 
         String xmlData = getItemTemplate("escidoc_item_198_for_create.xml");
 
         createItem(null, xmlData);
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testCreateContainerWithoutContainerId() throws Exception {
 
         String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         createContainer(null, xmlData);
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testCreateItemWithoutItem() throws Exception {
 
         createItem(theContainerId, null);
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testCreateContainerWithoutContainer() throws Exception {
 
         createContainer(theContainerId, null);
     }
 
     /**
      * Test update of all NOT-read-only properties. Disabled because there are only read-only properties. TODO Decide
      * what to do with name and description.
      */
     @Ignore("Disabled because there are only read-only properties")
     @Test
     public void testUpdateProperties() throws Exception {
         // name description content-type-specific
 
         Document containerDoc = EscidocAbstractTest.getDocument(theContainerXml);
 
         String basePath = "/container/properties/";
         String testStringValue = "testing";
 
         // TODO check if values are new
         Node testContainer;
 
         // name
         testContainer = substitute(containerDoc, basePath + "name", testStringValue);
 
         // description
         testContainer = substitute(containerDoc, basePath + "description", testStringValue);
 
         // content-type-specific in testUpdateCts
 
         String testContainerXml = toString(testContainer, true);
         update(theContainerId, testContainerXml);
 
         Document updatedContainer = EscidocAbstractTest.getDocument(retrieve(theContainerId));
 
         assertEquals(testStringValue, selectSingleNode(updatedContainer, basePath + "name/text()").getNodeValue());
         assertEquals(testStringValue, selectSingleNode(updatedContainer, basePath + "description/text()")
             .getNodeValue());
     }
 
     /**
      * 
      * @throws Exception
      */
     @Ignore("test update read-only properties")
     @Test
     public void dotestUpdateReadonlyProperties() throws Exception {
         // xlink:href xlink:title xlink:type
         // creation-date
         // context (xlink:href" xlink:type xlink:title)
         // content-type (xlink:type xlink:href xlink:title)
         // status
         // creator (xlink:href xlink:title xlink:type)
         // lock-status lock-owner content-type-specific
         // current-version (number date version-status valid-status xlink:href
         // xlink:title xlink:type)
         // latest-version (number date xlink:href xlink:title xlink:type)
         // latest-revision (number date pid xlink:href xlink:title xlink:type)
 
         String basePath = "/container/properties/";
         String testStringValue = "testing";
         String testDateValue = "1970-01-01T01:00:00.000Z";
         String testIntegerValue = "-1";
         Class<?> ee = ReadonlyElementViolationException.class;
 
         // TODO check if values are new
         // TODO attribute selection/substitution
         Node testContainer;
         // testContainer = substitute(getDocument(theContainerXml), basePath +
         // "@xlink:href",
         // testStringValue);
         // try{
         // update(theContainerId, toString(testContainer, true));
         // }
         // catch(Exception e){
         // assertExceptionType(ae.getName() + " expected.", ae, e);
         // }
 
         // testContainer = substitute(getDocument(theContainerXml), basePath +
         // "@xlink:title",
         // testStringValue);
         // try{
         // update(theContainerId, toString(testContainer, true));
         // }
         // catch(Exception e){
         // assertExceptionType(ae.getName() + " expected.", ae, e);
         // }
 
         // testContainer = substitute(getDocument(theContainerXml), basePath +
         // "@xlink:type",
         // "extended");
         // try{
         // update(theContainerId, toString(testContainer, true));
         // }
         // catch(Exception e){
         // assertExceptionType(ae.getName() + " expected.", ae, e);
         // }
 
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePath + "name", testStringValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update name.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePath + "description", testStringValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update description.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePath + "creation-date", testDateValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update creation-date.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // context (xlink:href" xlink:type xlink:title)
         // content-type (xlink:type xlink:href xlink:title)
 
         // status
         testContainer = substitute(EscidocAbstractTest.getDocument(theContainerXml), basePath + "status", "withdrawn");
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update status.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // creator (xlink:href xlink:title xlink:type)
 
         // lock-status
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePath + "lock-status", "locked");
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update lock-status.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // lock-owner // TODO test with locked item
 
         // current-version (xlink:href
         // xlink:title xlink:type)
         String basePathCV = basePath + "/current-version/";
         // number
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePathCV + "number", testIntegerValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update number.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // date
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePathCV + "date", testDateValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update date.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // version-status
         testContainer =
 
         substitute(EscidocAbstractTest.getDocument(theContainerXml), basePathCV + "status", "withdrawn");
 
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update status.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // valid-status
 
         // testContainer =
         // substitute(getDocument(theContainerXml), basePathCV
         // + "valid-status", "invalid");
         // try {
         // update(theContainerId, toString(testContainer, true));
         // fail("No exception after update valid-status.");
         // }
         // catch (final Exception e) {
         // assertExceptionType(ee.getName() + " expected.", ee, e);
         // }
 
         // latest-version (number date xlink:href xlink:title xlink:type)
         // number
         String basePathLV = basePath + "/latest-version/";
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePathLV + "number", testIntegerValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update number.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // date
         testContainer =
             substitute(EscidocAbstractTest.getDocument(theContainerXml), basePathLV + "date", testDateValue);
         try {
             update(theContainerId, toString(testContainer, true));
             fail("No exception after update date.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ee.getName() + " expected.", ee, e);
         }
 
         // latest-revision (number date pid xlink:href xlink:title xlink:type)
 
     }
 
     /**
      * Update container with Version status "pending".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_UCO_1_1() throws Exception {
 
         String xml = retrieve(theContainerId);
         Document toBeUpdatedDocument = EscidocAbstractTest.getDocument(xml);
         assertXmlEquals("Unexpected status. ", toBeUpdatedDocument, XPATH_CONTAINER_STATUS, STATE_PENDING);
         assertXmlEquals("Unexpected current version status", toBeUpdatedDocument,
             XPATH_CONTAINER_CURRENT_VERSION_STATUS, STATE_PENDING);
 
         // change something
         Node n = selectSingleNode(toBeUpdatedDocument, XPATH_CONTAINER_PROPERTIES_CMS);
         final String nameNischt = "nischt";
         n.appendChild(toBeUpdatedDocument.createElement(nameNischt));
 
         String newContainerXml = toString(toBeUpdatedDocument, false);
         String beforeTimestamp = getLastModificationDateValue(toBeUpdatedDocument);
         xml = update(theContainerId, newContainerXml);
         assertXmlValidContainer(xml);
         Document updatedDocument = EscidocAbstractTest.getDocument(xml);
         // check last-modification-date, status, valid xml
         assertDateBeforeAfter(beforeTimestamp, getLastModificationDateValue(updatedDocument));
 
         assertXmlEquals("Unexpected status. ", updatedDocument, XPATH_CONTAINER_STATUS, STATE_PENDING);
         assertXmlEquals("Unexpected current version status", updatedDocument, XPATH_CONTAINER_CURRENT_VERSION_STATUS,
             STATE_PENDING);
 
         // check if added element exists
         assertXmlExists("Updated element not found", updatedDocument, XPATH_CONTAINER_PROPERTIES_CMS + "/" + nameNischt);
     }
 
     /**
      * Successfully test updating a container with a values containing entities referencen.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOmUCO_1_6() throws Exception {
 
         String xml = retrieve(theContainerId);
         assertXmlValidContainer(xml);
         Document toBeUpdatedDocument = EscidocAbstractTest.getDocument(xml);
 
         String id = getObjidValue(toBeUpdatedDocument);
         String updateTile = "' > < &";
         // Node updatedAdminDescDescription =
         // substitute(toBeUpdatedDocument,
         // "container/admin-descriptor/properties/description", updateTile);
 
         Node updateContentModelSpec =
             substitute(toBeUpdatedDocument, "container/properties/content-model-specific/xxx", updateTile);
 
         String updated = update(id, toString(updateContentModelSpec, false));
         assertXmlValidContainer(updated);
         Document updatedDoc = EscidocAbstractTest.getDocument(updated);
 
         // String admindescrDescriptionValue = selectSingleNode(updatedDoc,
         // "container/admin-descriptor/properties/description")
         // .getTextContent();
 
         String contentTypeSpecValue =
             selectSingleNode(updatedDoc, "container/properties/content-model-specific/xxx").getTextContent();
 
         // assertEquals("Titles are not equal", admindescrDescriptionValue,
         // updateTile);
 
         assertEquals("Cmses are not equal", contentTypeSpecValue, updateTile);
 
     }
 
     /**
      * Update container with Version status "submitted".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_UCO_1_2() throws Exception {
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), null));
 
         String testStatus = "submitted";
         String xml = retrieve(theContainerId);
         Document newContainer = EscidocAbstractTest.getDocument(xml);
         selectSingleNodeAsserted(newContainer, "/container/properties/version/status[text() = '" + testStatus + "']");
 
         // change something
         Node n = selectSingleNode(newContainer, "/container/properties/content-model-specific");
         n.appendChild(newContainer.createElement("nischt"));
 
         String newContainerXml = toString(newContainer, false);
 
         String beforeTimestamp = getLastModificationDateValue(newContainer);
         xml = update(theContainerId, newContainerXml);
         assertXmlValidContainer(xml);
         Document updatedContainer = EscidocAbstractTest.getDocument(xml);
         String afterTimestamp = getLastModificationDateValue(updatedContainer);
 
         // check last-modification-date, status, valid xml
         assertDateBeforeAfter(beforeTimestamp, afterTimestamp);
         selectSingleNodeAsserted(newContainer, "/container/properties/version/status[text() = '" + testStatus + "']");
     }
 
     /**
      * Tests successfully updating 'escidoc' metadata record of an item.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testUpdateEscidocMdRecord() throws Exception {
 
         // changed from retrieve/update metadata to retrieve/update with
         // metadata (2007-01-25, FRS)
 
         String mdRecordName = "escidoc";
         String newName = "new name";
         String newTitle = "new Title";
         String mdXPath =
             "/container/md-records/md-record[@name='" + mdRecordName + "']/publication/creator/person/family-name";
         String mdTitlePath = "/container/md-records/md-record[@name='" + mdRecordName + "']/publication/title";
 
         String lastModificationDateXPath = "/container/@last-modification-date";
 
         // String mdRecordXml = retrieveMetadataRecord(theItemId,
         // mdRecordName);
         String mdRecordXml = retrieve(theContainerId);
         String oldCreatorName =
             selectSingleNode(EscidocAbstractTest.getDocument(mdRecordXml), mdXPath).getTextContent();
         TripleStoreTestBase tripleStore = new TripleStoreTestBase();
         String result =
             tripleStore.requestMPT("<info:fedora/" + theContainerId + "> "
                 + "<http://purl.org/dc/elements/1.1/creator>" + " *", "RDF/XML");
         String oldCreatorDcName =
             selectSingleNode(EscidocAbstractTest.getDocument(result), XPATH_TRIPLE_STORE_DC_CREATOR).getTextContent();
         String oldTitle =
             selectSingleNode(EscidocAbstractTest.getDocument(theContainerXml), "/container/properties/name")
                 .getTextContent();
         assertTrue(oldCreatorName + " is not contained in DC 'creator' ", oldCreatorDcName.endsWith(oldCreatorName));
         Document newMdRecord = (Document) substitute(EscidocAbstractTest.getDocument(mdRecordXml), mdXPath, newName);
         newMdRecord = (Document) substitute(newMdRecord, mdTitlePath, newTitle);
         Node oldModDateNode = selectSingleNode(newMdRecord, lastModificationDateXPath);
 
         // String xml = updateMetadataRecord(theItemId, mdRecordName,
         // toString(
         // newMdRecord, false));
         final String updatedXml = update(theContainerId, toString(newMdRecord, false));
         Document updateDocument = EscidocAbstractTest.getDocument(updatedXml);
         Node newModDateNode = selectSingleNode(updateDocument, lastModificationDateXPath);
 
         assertDateBeforeAfter(oldModDateNode.getNodeValue(), newModDateNode.getNodeValue());
 
         assertXmlValidContainer(updatedXml);
         result =
             tripleStore.requestMPT("<info:fedora/" + theContainerId + "> "
                 + "<http://purl.org/dc/elements/1.1/creator>" + " *", "RDF/XML");
         String newCreatorDcName =
             selectSingleNode(EscidocAbstractTest.getDocument(result), XPATH_TRIPLE_STORE_DC_CREATOR).getTextContent();
         assertTrue(newName + " is not contained in DC 'creator' ", newCreatorDcName.endsWith(newName));
         String title = selectSingleNode(updateDocument, "/container/properties/name").getTextContent();
         assertTrue("title was not updated", title.equals(newTitle));
         String firstContainerVersion = retrieve(theContainerId + ":1");
         String firstVersionTitle =
             selectSingleNode(getDocument(firstContainerVersion), "/container/properties/name").getTextContent();
         assertTrue("title is wrong", firstVersionTitle.equals(oldTitle));
 
     }
 
     /**
      * Update container with Version status "released".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_UCO_1_3() throws Exception {
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), null));
 
         String pidParam;
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + this.theContainerId));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                     assignPidParam);
 
             assignObjectPid(this.theContainerId, pidParam);
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
             String latestVersion = getLatestVersionObjidValue(theContainerXml);
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + latestVersion));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(latestVersion))),
                     assignPidParam);
 
             assignVersionPid(latestVersion, pidParam);
         }
 
         release(theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
         String testStatus = "released";
         String xml = retrieve(theContainerId);
         Document newContainer = EscidocAbstractTest.getDocument(xml);
         selectSingleNodeAsserted(newContainer, "/container/properties/version/status[text() = '" + testStatus + "']");
 
         // change something
         Node n = selectSingleNode(newContainer, "/container/properties/content-model-specific");
         n.appendChild(newContainer.createElement("nischt"));
 
         String newContainerXml = toString(newContainer, false);
 
         String beforeTimestamp = getLastModificationDateValue(newContainer);
 
         xml = update(theContainerId, newContainerXml);
 
         Document updatedContainer = EscidocAbstractTest.getDocument(xml);
         String afterTimestamp = getLastModificationDateValue(updatedContainer);
 
         // check last-modification-date, status, valid xml
         assertDateBeforeAfter(beforeTimestamp, afterTimestamp);
         selectSingleNodeAsserted(newContainer, "/container/properties/version/status[text() = '" + testStatus + "']");
         assertXmlValidContainer(xml);
     }
 
     /**
      * Update container with Version status "pending" using lax mode.
      *
      * @throws Exception If anything fails.
      */
     @Ignore("Update container with Version status pending using lax mode")
     @Test
     public void testOM_UCO_1_5() throws Exception {
 
         final String createdXml = retrieve(theContainerId);
         assertNotNull("No data from retrieve", createdXml);
         final Document toBeUpdatedDocument = EscidocAbstractTest.getDocument(createdXml);
         final String createdLastModificationDate = getLastModificationDateValue(toBeUpdatedDocument);
 
         // change something
         Node n = selectSingleNode(toBeUpdatedDocument, XPATH_CONTAINER_PROPERTIES_CMS);
         final String nameNischt = "nischt";
         n.appendChild(toBeUpdatedDocument.createElement(nameNischt));
 
         // delete lax attributes
         // md-records
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORDS_XLINK_HREF);
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORDS_XLINK_TITLE);
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORDS_XLINK_TYPE);
         // md-record
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORD_XLINK_HREF);
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORD_XLINK_TITLE);
         deleteNodes(toBeUpdatedDocument, XPATH_CONTAINER_MD_RECORD_XLINK_TYPE);
 
         String toBeUpdatedXml = toString(toBeUpdatedDocument, false);
 
         String updatedXml = null;
         try {
             updatedXml = update(theContainerId, toBeUpdatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Updating pending container failed. ", e);
         }
         assertNotNull("No data from update", updatedXml);
         assertXmlValidContainer(updatedXml);
         Document updatedDocument = EscidocAbstractTest.getDocument(updatedXml);
         // check last-modification-date, status, valid xml
         assertDateBeforeAfter(createdLastModificationDate, getLastModificationDateValue(updatedDocument));
         assertXmlEquals("Unexpected status. ", updatedDocument, XPATH_CONTAINER_STATUS, STATE_PENDING);
         assertXmlEquals("Unexpected current version status", updatedDocument, XPATH_CONTAINER_CURRENT_VERSION_STATUS,
             STATE_PENDING);
 
         // check if added element exists
         assertXmlExists("Updated element not found", updatedDocument, XPATH_CONTAINER_PROPERTIES_CMS + "/" + nameNischt);
     }
 
     /**
      * Update non existing container.
      *
      * @throws Exception If anything fails.
      */
     @Test(expected = ContainerNotFoundException.class)
     public void testOM_UCO_2() throws Exception {
 
         update("escidoc:nonexist1", retrieve(theContainerId));
     }
 
     /**
      * Update with incorrect xml-representation of the container.
      *
      * @throws Exception If anything fails.
      */
     // commented out because there is no check of read-only elements
     // and attributes
     @Ignore("commented out because there is no check of read-only elements and attributes")
     @Test
     public void testOM_UCO_3_1() throws Exception {
         Document container = EscidocAbstractTest.getDocument(theContainerXml);
         container = (Document) substitute(container, "/container/admin-descriptor/@href", "nothing");
         try {
             update(theContainerId, toString(container, true));
             fail("No exception on update with invalid values in admin descriptor.");
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyAttributeViolationException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Update with incorrect xml-representation of the container.
      *
      * @throws Exception If anything fails.
      */
     // Commented out, because business logic does not check read-only properties
     @Ignore("Commented out because business logic does not check read-only properties")
     @Test
     public void testOM_UCO_3_2() throws Exception {
         Document container = EscidocAbstractTest.getDocument(theContainerXml);
 
         container =
             (Document) substitute(container, "/container/properties/context/@href", "/ir/context/escidoc:nonexist1");
 
         try {
             update(theContainerId, toString(container, true));
             fail("No exception on update with invalid values in admin descriptor.");
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyAttributeViolationException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Update with incorrect xml-representation of the container.
      *
      * @throws Exception If anything fails.
      */
     @Ignore("Update with incorrect xml-representation of the container")
     @Test
     public void testOM_UCO_3_3() throws Exception {
         //testUpdateTocNonExistingObjectRef();
     }
 
     /**
      * Update with incorrect xml-representation of the container.
      *
      * @throws Exception If anything fails.
      */
     // Commented out, because business logic does not check read-only properties
     @Ignore("Commented out because business logic does not check read-only properties")
     @Test
     public void UtestOM_UCO_3_4() throws Exception {
         dotestUpdateReadonlyProperties();
     }
 
     /**
      * Update without container ID.
      *
      * @throws Exception If anything fails.
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testOM_UCO_4_1() throws Exception {
 
         update(null, retrieve(theContainerId));
     }
 
     /**
      * Update without xml representation.
      *
      * @throws Exception If anything fails.
      */
     @Test(expected = MissingMethodParameterException.class)
     public void testOM_UCO_4_2() throws Exception {
 
         update(theContainerId, null);
     }
 
     /**
      * Resource is locked - update not allowed.
      *
      * @test.name Resource is locked - update not allowed.
      * @test.id OM_UCO_5_1
      * @test.input Container ID, XML representation of the container.
      * @test.expected Error message with reason(s) for failure.
      *
      * @test.status Not Implemented, no lock in first release
      *
      * @throws Exception
      * If anything fails.
      */
     @Test
     @Ignore("missing implementation")
     public void notestOM_UCO_5_1() throws Exception {
     }
 
     /**
      * Optimistic Locking Test. 
      * 
      * @throws Exception If anything fails.
      */
     @Test(expected = OptimisticLockingException.class)
     public void testOM_UCO_5_2() throws Exception {
 
         Document container =
             (Document) substitute(getDocument(theContainerXml), "/container/@last-modification-date",
                 "1970-01-01T01:00:00.000Z");
 
         update(theContainerId, toString(container, true));
     }
 
     /**
      * Revision status is "withdrawn".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_UCO_6() throws Exception {
 
         String xml =
             submit(theContainerId,
                 getStatusTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), null));
 
         // FIXME replace this code through releaseWithPid();
         String pidParam;
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + this.theContainerId));
             pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(xml)), assignPidParam);
 
             xml = assignObjectPid(this.theContainerId, pidParam);
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
             String latestVersion = getLatestVersionObjidValue(theContainerXml);
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + latestVersion));
             pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(xml)), assignPidParam);
 
             xml = assignVersionPid(latestVersion, pidParam);
         }
 
         xml = release(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = withdraw(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
 
         xml = retrieve(this.theContainerId);
 
         try {
             // remove try-catch if update of released containers is allowed
             update(theContainerId, xml);
             fail("No exception on update withdrawn container.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test successfully updating container with two existiing relations. No new relations will be added while update,
      * one existing relation will be deleted while update.
      */
     @Test
     public void testDeleteRelation() throws Exception {
 
         String container = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
         assertXmlValidContainer(container);
 
         String theContainerXml1 = create(container);
         assertXmlValidContainer(theContainerXml);
 
         Document document1 = EscidocAbstractTest.getDocument(theContainerXml1);
         String containerId1 = getObjidValue(document1);
 
         String theContainerXml2 = create(container);
 
         assertXmlValidContainer(theContainerXml2);
 
         Document document2 = EscidocAbstractTest.getDocument(theContainerXml2);
         String containerId2 = getObjidValue(document2);
 
         String href1 = "/ir/container/" + containerId1;
         String href2 = "/ir/container/" + containerId2;
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
 
         String xml = create(containerForCreateWithRelationsXml);
         Document container3 = EscidocAbstractTest.getDocument(xml);
         String createdContainerId3 = getObjidValue(container3);
         NodeList relations = selectNodeList(container3, "/container/relations/relation");
 
         Node xmlcontainerWithoutFirstRelation = deleteElement(container3, "/container/relations/relation[1]");
         String updatedXml = update(createdContainerId3, toString(xmlcontainerWithoutFirstRelation, true));
         assertXmlValidContainer(updatedXml);
         Node updatedcontainer = EscidocAbstractTest.getDocument(updatedXml);
 
         NodeList relationsAfterUpdate = selectNodeList(updatedcontainer, "/container/relations/relation");
         assertXmlValidContainer(xml);
         assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterUpdate.getLength() + 1);
 
     }
 
     /**
      * Test successfully updating container with two existing relations. One new relation will be added while update,
      * one existing relation will be deleted while update.
      */
     @Test
     public void testRelationsUpdate() throws Exception {
 
         String container0 = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         assertXmlValidContainer(container0);
         String theContainerXml1 = create(container0);
         assertXmlValidContainer(theContainerXml1);
 
         Document document1 = EscidocAbstractTest.getDocument(theContainerXml1);
         String createdContainerId1 = getObjidValue(document1);
 
         String theContainerXml2 = create(container0);
 
         assertXmlValidContainer(theContainerXml2);
 
         Document document2 = EscidocAbstractTest.getDocument(theContainerXml2);
         String createdContainerId2 = getObjidValue(document2);
 
         String theContainerXml3 = create(container0);
 
         assertXmlValidContainer(theContainerXml3);
 
         Document document3 = EscidocAbstractTest.getDocument(theContainerXml3);
         String createdContainerIdToAdd = getObjidValue(document3);
         String hrefToAdd = "/ir/container/" + createdContainerIdToAdd;
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
 
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
         Document containerForCreateWithRelations = EscidocAbstractTest.getDocument(containerForCreateWithRelationsXml);
 
         NodeList relations = selectNodeList(containerForCreateWithRelations, "/container/relations/relation");
 
         String xml = create(containerForCreateWithRelationsXml);
         Document container = EscidocAbstractTest.getDocument(xml);
         String createdContainerId3 = getObjidValue(container);
         String targetRelationToLeave =
             selectSingleNode(container, "/container/relations/relation[2]/@href").getTextContent();
 
         String targetToAdd = null;
         String targetRelationToRemove = null;
         Node targetHref = selectSingleNode(container, "/container/relations/relation[1]/@href");
         targetRelationToRemove = targetHref.getTextContent();
         targetHref.setNodeValue(hrefToAdd);
         targetToAdd = hrefToAdd;
 
         String updatedXml = update(createdContainerId3, toString(container, true));
         assertXmlValidContainer(updatedXml);
         Document updatedcontainer = EscidocAbstractTest.getDocument(updatedXml);
 
         NodeList relationsAfterUpdate = selectNodeList(updatedcontainer, "/container/relations/relation");
         String targetNew1 =
             selectSingleNode(updatedcontainer, "/container/relations/relation[1]/@href").getTextContent();
         String targetNew2 =
             selectSingleNode(updatedcontainer, "/container/relations/relation[2]/@href").getTextContent();
 
         assertXmlValidContainer(xml);
         assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterUpdate.getLength());
 
         if (targetNew2.equals(targetRelationToLeave)) {
 
             assertEquals("Relation target Id is wrong ", targetNew1, targetToAdd);
         }
         else {
             assertEquals("Relation target Id is wrong ", targetNew1, targetRelationToLeave);
 
         }
 
         // check response from retrieve
         xml = retrieve(createdContainerId3);
         updatedcontainer = getDocument(xml);
 
         relationsAfterUpdate = selectNodeList(updatedcontainer, "/container/relations/relation");
 
         // href/objid of relations after update
         targetNew1 = selectSingleNode(updatedcontainer, "/container/relations/relation[1]/@href").getTextContent();
         targetNew2 = selectSingleNode(updatedcontainer, "/container/relations/relation[2]/@href").getTextContent();
 
         assertXmlValidContainer(xml);
         // assert number of relations
         assertEquals("Number of relations is wrong ", relations.getLength(), relationsAfterUpdate.getLength());
 
         // assert ???
         if (targetNew2.equals(targetRelationToLeave)) {
             assertEquals("Relation target Id is wrong ", targetNew1, targetToAdd);
         }
         else {
             assertEquals("Relation target Id is wrong ", targetNew1, targetRelationToLeave);
         }
 
         // assert first relation is no longer there
         Node removedNode =
             selectSingleNode(updatedcontainer, "/container/relations/relation[@* = '" + targetRelationToRemove + "']");
         assertNull(removedNode);
         // assert third relation is there
         selectSingleNodeAsserted(updatedcontainer, "/container/relations/relation[@* = '" + targetToAdd + "']");
         // assert second relation is still there
         selectSingleNodeAsserted(updatedcontainer, "/container/relations/relation[@* = '" + targetRelationToLeave
             + "']");
 
     }
 
     /**
      * Test successfully updating Container with two relations removed.
      */
     @Test
     public void testRelationsRemoveUpdate() throws Exception {
         String containerTargetTemplate = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         String containerXml1 = create(containerTargetTemplate);
         assertXmlValidContainer(containerXml1);
         Document document1 = EscidocAbstractTest.getDocument(containerXml1);
         String containerId1 = getObjidValue(document1);
 
         String containerXml2 = create(containerTargetTemplate);
         assertXmlValidContainer(containerXml2);
         Document document2 = EscidocAbstractTest.getDocument(containerXml2);
         String containerId2 = getObjidValue(document2);
 
         String containerHref1 = "/ir/container/" + containerId1;
         String containerHref2 = "/ir/container/" + containerId2;
 
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", containerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", containerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", containerHref1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", containerHref2);
         Document containerForCreateWithRelations = EscidocAbstractTest.getDocument(containerForCreateWithRelationsXml);
 
         // create container with two relations
         String xml = create(containerForCreateWithRelationsXml);
         Document container = EscidocAbstractTest.getDocument(xml);
         String containerId = getObjidValue(container);
 
         // check for two relations
         selectSingleNodeAsserted(container, "/container/relations/relation[2]");
         selectSingleNodeAsserted(getDocument(retrieve(containerId)), "/container/relations/relation[2]");
 
         // remove relations and update
         Node containerWithoutRelations = deleteElement(container, "/container/relations");
         String containerWithoutRelationsXml = toString(containerWithoutRelations, false);
         xml = update(containerId, containerWithoutRelationsXml);
         Document updatedContainer = getDocument(xml);
 
         // check for no relation
         Node relation = selectSingleNode(updatedContainer, "/container/relations/relation[1]");
         assertNull("Found unexpected relation after update.", relation);
         relation =
             selectSingleNode(getDocument(retrieve(getObjidValue(updatedContainer))), "/container/relations/relation[1]");
         assertNull("Found unexpected relation in retrieve after update.", relation);
 
     }
 
     /**
      * Test declining updating of an container with a new relation, which has a non existing predicate.
      */
     @Test
     public void testRelationsUpdateWithWrongPredicate() throws Exception {
 
         String container = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         assertXmlValidContainer(container);
         String theContainerXml1 = create(container);
         assertXmlValidContainer(theContainerXml);
 
         Document document1 = EscidocAbstractTest.getDocument(theContainerXml1);
         String createdContainerId1 = getObjidValue(document1);
 
         String theContainerXml2 = create(container);
 
         assertXmlValidContainer(theContainerXml2);
 
         Document document2 = EscidocAbstractTest.getDocument(theContainerXml2);
         String createdContainerId2 = getObjidValue(document2);
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
 
         String xml = create(containerForCreateWithRelationsXml);
         Document container3 = EscidocAbstractTest.getDocument(xml);
         String createdContainerId3 = getObjidValue(container3);
         Node predicate = selectSingleNode(container3, "/container/relations/relation[1]/@predicate");
 
         predicate.setNodeValue("bla");
 
         try {
             update(createdContainerId3, toString(container3, true));
             fail("No exception occured on added an relation with non existing predicate " + "to the container");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("RelationPredicateNotFoundException.",
                 RelationPredicateNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining updating of an container with a new relation, which has a non existing target.
      */
     @Test
     public void testRelationsUpdateWithWrongTarget() throws Exception {
 
         String container = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         assertXmlValidContainer(container);
         String theContainerXml1 = create(container);
 
         assertXmlValidContainer(theContainerXml);
 
         Document document1 = EscidocAbstractTest.getDocument(theContainerXml1);
         String createdContainerId1 = getObjidValue(document1);
 
         String theContainerXml2 = create(container);
 
         assertXmlValidContainer(theContainerXml2);
 
         Document document2 = EscidocAbstractTest.getDocument(theContainerXml2);
         String createdContainerId2 = getObjidValue(document2);
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
 
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
 
         String xml = create(containerForCreateWithRelationsXml);
 
         Document container3 = EscidocAbstractTest.getDocument(xml);
         String createdContainerId3 = getObjidValue(container3);
         Node targetId = selectSingleNode(container3, "/container/relations/relation[1]/@href");
         targetId.setNodeValue("bla");
         try {
             update(createdContainerId3, toString(container3, true));
             fail("No exception occured on added an relation with non " + "existing target to the container");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("ReferencedResourceNotFoundException.",
                 ReferencedResourceNotFoundException.class, e);
         }
     }
 
     // The test is commented out because it is obsolete (rest/soap distinction)
 
     /**
      * Test declining updating of an item which relation id attributes are not equal
      */
     @Ignore("update item which relation id attributes are not equal - commented out because it is obsolete (rest/soap distinction)")
     @Test
     public void testRelationsUpdateWithUnequalIdsAttributes() throws Exception {
 
         String createdContainerId1 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
         String createdContainerId2 = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
 
         String containerForCreateWithRelationsXml =
             getContainerTemplate("create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
 
         String xml = create(containerForCreateWithRelationsXml);
         String createdContainerId3 = null;
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(xml);
         if (m3.find()) {
             createdContainerId3 = m3.group(1);
         }
         Document container = EscidocAbstractTest.getDocument(xml);
         Node targetId = selectSingleNode(container, "/container/relations/relation[1]/@objid");
         targetId.setNodeValue("bla");
 
         try {
             update(createdContainerId3, toString(container, true));
             fail("No exception occured on added an relation with unequal ids attributes " + "to the container");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("InvalidContentException.", InvalidContentException.class, e);
         }
     }
 
     /**
      * Test successfully deleting of one optional md-record from a container while update.
      */
     @Test
     public void testDeleteMdrecordWhileUpdate() throws Exception {
         Document xmlContainer =
             EscidocAbstractTest
                 .getDocument(getContainerTemplate("create_container_2_Md_Records_WithoutMembers_v1.1.xml"));
 
         NodeList mdrecords = selectNodeList(xmlContainer, "/container/md-records/md-record");
 
         assertXmlValidContainer(toString(xmlContainer, false));
 
         final String createdXml = create(toString(xmlContainer, false));
 
         assertXmlValidContainer(createdXml);
         Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
         String createdContainerId = getObjidValue(createdDocument);
         NodeList mdrecordsAfterCreate = selectNodeList(createdDocument, "/container/md-records/md-record");
         assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());
         Node containerWithOneMdRecord =
             deleteElement(createdDocument, "/container/md-records/md-record[@name = 'name1']");
         String containerWithOneMdRecordXml = toString(containerWithOneMdRecord, true);
 
         String updatedContainerWith1MdRecord = update(createdContainerId, containerWithOneMdRecordXml);
         Document udatedDocument = EscidocAbstractTest.getDocument(updatedContainerWith1MdRecord);
         NodeList mdrecordsAfterUpdate = selectNodeList(udatedDocument, "/container/md-records/md-record");
         assertEquals(mdrecordsAfterUpdate.getLength() + 1, mdrecordsAfterCreate.getLength());
 
     }
 
     /**
      * Test declining deleting of EscidocInternal meta data set from a container while update.
      */
     @Test
     public void testDeleteEscidocInternalMdSetWhileUpdate() throws Exception {
         Document containerDocument = EscidocAbstractTest.getDocument(theContainerXml);
         Node containerWithoutEscidocMdRecord =
             substitute(containerDocument, "/container/md-records/md-record[@name = 'escidoc']/@name", "bla");
         String containerWithoutEscidocMdRecordXml = toString(containerWithoutEscidocMdRecord, true);
         Class<?> ec = MissingMdRecordException.class;
         try {
             String xml = update(theContainerId, containerWithoutEscidocMdRecordXml);
             EscidocAbstractTest.failMissingException(ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
 
     }
 
     /**
      * Test successfully updating a container. The first update deletes one optional md-record, the second update add
      * the md-record with the same value of the attribute 'name' again.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAddExistingMdrecordWhileUpdate() throws Exception {
 
         Document xmlContainer =
             EscidocAbstractTest
                 .getDocument(getContainerTemplate("create_container_2_Md_Records_WithoutMembers_v1.1.xml"));
 
         NodeList mdrecords = selectNodeList(xmlContainer, "/container/md-records/md-record");
         // Node containerWithoutAdminDescriptor =
         // deleteElement(xmlContainer, "/container/admin-descriptor");
         // String containerWithoutAdminDescriptorXml =
         // toString(containerWithoutAdminDescriptor, false);
         assertXmlValidContainer(toString(xmlContainer, false));
 
         final String createdXml = create(toString(xmlContainer, false));
 
         assertXmlValidContainer(createdXml);
         Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
         String createdContainerId = getObjidValue(createdDocument);
         NodeList mdrecordsAfterCreate = selectNodeList(createdDocument, "/container/md-records/md-record");
         assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());
         Node containerWithOneMdRecord =
             deleteElement(createdDocument, "/container/md-records/md-record[@name = 'name1']");
         String containerWithOneMdRecordXml = toString(containerWithOneMdRecord, true);
 
         String updatedContainerWith1MdRecord = update(createdContainerId, containerWithOneMdRecordXml);
         Document updatedDocument = EscidocAbstractTest.getDocument(updatedContainerWith1MdRecord);
         NodeList mdrecordsAfterUpdate = selectNodeList(updatedDocument, "/container/md-records/md-record");
         assertEquals(mdrecordsAfterUpdate.getLength() + 1, mdrecordsAfterCreate.getLength());
 
         Element mdRecord =
             updatedDocument.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.3",
                 "escidocMetadataRecords:md-record");
         mdRecord.setAttribute("name", "name1");
         mdRecord.setAttribute("schema", "bla");
         Element mdRecordContent = updatedDocument.createElement("bla");
         mdRecord.appendChild(mdRecordContent);
         selectSingleNode(updatedDocument, "/container/md-records").appendChild(mdRecord);
         String containerWith2MdRecordXml = toString(updatedDocument, true);
         String dobleUpdated = update(createdContainerId, containerWith2MdRecordXml);
         Document doubleUpdatedDocument = EscidocAbstractTest.getDocument(dobleUpdated);
         NodeList mdrecordsAfterSecondUpdate = selectNodeList(doubleUpdatedDocument, "/container/md-records/md-record");
         assertEquals(mdrecordsAfterUpdate.getLength() + 1, mdrecordsAfterSecondUpdate.getLength());
 
     }
 
     /**
      * Test successfully adding of a new optional md-record to a container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAddNewMdrecordWhileUpdate() throws Exception {
         Document xmlContainer =
             EscidocAbstractTest.getDocument(getContainerTemplate("create_container_WithoutMembers_v1.1.xml"));
 
         NodeList mdrecords = selectNodeList(xmlContainer, "/container/md-records/md-record");
         // Node containerWithoutAdminDescriptor =
         // deleteElement(xmlContainer, "/container/admin-descriptor");
         // String containerWithoutAdminDescriptorXml =
         // toString(containerWithoutAdminDescriptor, false);
         assertXmlValidContainer(toString(xmlContainer, false));
 
         final String createdXml = create(toString(xmlContainer, false));
 
         assertXmlValidContainer(createdXml);
         Document createdDocument = EscidocAbstractTest.getDocument(createdXml);
         String createdContainerId = getObjidValue(createdDocument);
         NodeList mdrecordsAfterCreate = selectNodeList(createdDocument, "/container/md-records/md-record");
         assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());
 
         Element mdRecord =
             createdDocument.createElementNS("http://www.escidoc.de/schemas/metadatarecords/0.3",
                 "escidocMetadataRecords:md-record");
         mdRecord.setAttribute("name", "name1");
         mdRecord.setAttribute("schema", "bla");
         Element mdRecordContent = createdDocument.createElement("bla");
         mdRecord.appendChild(mdRecordContent);
         selectSingleNode(createdDocument, "/container/md-records").appendChild(mdRecord);
         String containerWith2MdRecordXml = toString(createdDocument, true);
         String updated = update(createdContainerId, containerWith2MdRecordXml);
         Document updatedDocument = EscidocAbstractTest.getDocument(updated);
         NodeList mdrecordsAfterUpdate = selectNodeList(updatedDocument, "/container/md-records/md-record");
         assertEquals(mdrecordsAfterUpdate.getLength() - 1, mdrecordsAfterCreate.getLength());
 
         // INFR-1013: test if the first version is still readable
         retrieve(createdContainerId + ":1");
     }
 
     /**
      * Test if the right excpetion is thrown if an Item within a Container with wrong Context is to create.
      * <p/>
      * See also Bug #579
      *
      * @throws Exception If the framework throws no Exception or the wrong Exception.
      */
     @Test
     public void testCreateItemWithDifferentContext() throws Exception {
 
         // prepare Item -------------------------------------------------------
         String tempItemXml = getItemTemplate("escidoc_item_198_for_create.xml");
         Document tempItemDoc = EscidocAbstractTest.getDocument(tempItemXml);
         tempItemDoc =
             (Document) substitute(tempItemDoc, "//properties/context/@href", "/ir/context/escidoc:persistent10");
         String itemXml = toString(tempItemDoc, false);
 
         // check prerequists: --------------------------------------------------
         // Check that the ContextId of the Item differs from the ContextId of
         // the Container.
         Document containerDoc = EscidocAbstractTest.getDocument(theContainerXml);
 
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
 
         Node contextNode = null;
         Node itemNode = null;
         String containerContextId = null;
         String itemContextId = null;
         contextNode = XPathAPI.selectSingleNode(containerDoc, "//properties/context/@href");
         containerContextId = getObjidFromHref(contextNode.getNodeValue());
 
         itemNode = XPathAPI.selectSingleNode(itemDoc, "//properties/context/@href");
         itemContextId = getObjidFromHref(itemNode.getNodeValue());
 
         assertNotEquals("Context Id has to differ for test", containerContextId, itemContextId);
 
         // now try to create an Item
         Class<?> ec = InvalidContextException.class;
         try {
             createItem(theContainerId, itemXml);
             EscidocAbstractTest.failMissingException(ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
 
     }
 
     /**
      * Test succesfully update metadata by just changing the namespace but NOT the prefix bound to that namespace.
      */
     @Test
     public void testUpdateMdRecordNamespace() throws Exception {
 
         Document resourceDoc = getDocument(this.theContainerXml);
         String versionNumber =
             selectSingleNodeAsserted(resourceDoc, "/container/properties/version/number/text()").getNodeValue();
         String modDate = selectSingleNodeAsserted(resourceDoc, "/container/@last-modification-date").getNodeValue();
 
         String resourceDocString = toString(resourceDoc, false);
         resourceDocString =
             resourceDocString.replace("xmlns=\"http://escidoc.mpg.de/metadataprofile/schema/0.1/\"",
                 "xmlns=\"http://just.for.test/namespace\"");
 
         String updatedResource = update(this.theContainerId, resourceDocString);
 
         // version number should be changed
         assertNotEquals("version number should be changed updating md-record namespace", versionNumber,
             selectSingleNode(getDocument(updatedResource), "/container/properties/version/number/text()")
                 .getNodeValue());
         // last modification timestamp must be changed
         assertNotEquals("last modification date should be changed updating md-record namespace", modDate,
             selectSingleNode(getDocument(updatedResource), "/container/@last-modification-date").getNodeValue());
 
     }
 
     // Test methods are obsolete, because they test the obsolete
     // interface methods
 
     /**
      * Test declining update of container with two existing relations. Adding
     of a
      * new relation to th container, which has the same target and predicate
     as one
      * of the existing containers relations is declining.
      */
     @Test
     @Ignore("was commented out, unknown wy")
     public void testRelationsAdd() throws Exception {
         String containerXml1 =
             create(getTemplateAsString(TEMPLATE_CONTAINER_PATH, "create_container_WithoutMembers_v1.1.xml"));
         String containerXml2 =
             create(getTemplateAsString(TEMPLATE_CONTAINER_PATH, "create_container_WithoutMembers_v1.1.xml"));
 
         String createdContainerId1 = null;
         String createdContainerId2 = null;
 
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
         Matcher m1 = PATTERN_OBJID_ATTRIBUTE.matcher(containerXml1);
         if (m1.find()) {
             createdContainerId1 = m1.group(1);
         }
         Matcher m2 = PATTERN_OBJID_ATTRIBUTE.matcher(containerXml2);
         if (m2.find()) {
             createdContainerId2 = m2.group(1);
         }
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
         String containerForCreateWithRelationsXml =
             getTemplateAsString(TEMPLATE_CONTAINER_PATH, "create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##", createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##", createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_HREF2##", href2);
 
         String xml = create(containerForCreateWithRelationsXml);
         String createdContainerId3 = null;
         Matcher m3 = PATTERN_OBJID_ATTRIBUTE.matcher(xml);
         if (m3.find()) {
             createdContainerId3 = m3.group(1);
         }
         Document container = getDocument(xml);
 
         Node withoutRelationObjId = deleteAttribute(container, "/container/relations/relation[1]/@objid");
         Node targetObjectId = selectSingleNode(withoutRelationObjId, "/container/relations/relation[1]/target/@objid");
 
         targetObjectId.setNodeValue(createdContainerId2);
         Node targetHref = selectSingleNode(withoutRelationObjId, "/container/relations/relation[1]/target/@href");
         targetHref.setNodeValue(href2);
         String xmlNeu = toString(withoutRelationObjId, true);
 
         try {
             update(createdContainerId3, toString(withoutRelationObjId, true));
             fail("No exception occured on container update with relations, which "
                 + "has the same target and predicate as one existing container " + "relation");
         }
         catch (final Exception e) {
             //            assertExceptionType("AlreadyExistException expected.", AlreadyExistsException.class, e);
             throw e;
         }
 
     }
 
 }
