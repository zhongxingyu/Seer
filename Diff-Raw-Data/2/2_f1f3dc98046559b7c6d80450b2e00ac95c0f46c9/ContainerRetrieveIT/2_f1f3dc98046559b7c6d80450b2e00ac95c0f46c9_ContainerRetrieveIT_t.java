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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.test.EscidocAbstractTest;
 
 /**
  * Test the mock implementation of the container resource.
  *
  * @author Michael Schneider
  */
 public class ContainerRetrieveIT extends ContainerTestBase {
 
     private String theContainerXml;
 
     protected String theContainerId;
 
     private String theItemId;
 
     private String path = TEMPLATE_CONTAINER_PATH;
 
     /**
      * Test successfully retrieving of container.
      *
      * @throws Exception Thrown if retrieve fails.
      */
     @Test
     public void testRetrieveResources() throws Exception {
 
         String xmlData =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         String theItemXml = handleXmlResult(getItemClient().create(xmlData));
 
         this.theItemId = getObjidValue(theItemXml);
         xmlData =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_v1.1-forItem.xml");
 
         String theContainerXml = create(xmlData.replaceAll("##ITEMID##", theItemId));
         String theContainerId = getObjidValue(theContainerXml);
 
         String resourcesXml = retrieveResources(theContainerId);
         assertXmlValidContainer(resourcesXml);
 
     }
 
     /**
      * Test retrieving (virtual-)resource by name.
      * 
      * @throws Exception
      */
     @Test
     public void retrieveResourcesByName() throws Exception {
 
         String xml = null;
         xml = retrieveResource(this.theContainerId, "version-history");
         assertXmlValidVersionHistory(xml);
 
         xml = retrieveResource(this.theContainerId, "parents");
         // FIXME
         //assertXmlValidVersionHistory(xml);
 
         xml = retrieveResource(this.theContainerId, "members");
         // FIXME
         //assertXmlValidMembers(xml);
 
         xml = retrieveResource(this.theContainerId, "relations");
         // FIXME
         //assertXmlValidRelations(xml);
 
         // dissemination (defined by content-model)
         // FIXME used content-model has not sdef etc.
         // xml = retrieveResource(this.theContainerId, "blafasel");
     }
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void setUp() throws Exception {
         this.path += "/rest";
 
         this.theItemId = createItem();
         String xmlData = EscidocAbstractTest.getTemplateAsString(this.path, "create_container_v1.1-forItem.xml");
 
         this.theContainerXml = create(xmlData.replaceAll("##ITEMID##", theItemId));
         this.theContainerId = getObjidValue(this.theContainerXml);
     }
 
     /**
      * Clean up after servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Override
     @After
     public void tearDown() throws Exception {
         super.tearDown();
 
         try {
             delete(this.theContainerId);
         }
         catch (final Exception e) {
             // do nothing
         }
     }
 
     /**
      * Retrieve members: correct input (success case).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_RFLMC_1_2() throws Exception {
 
         Document containerDoc = EscidocAbstractTest.getDocument(this.theContainerXml);
         String versionNumber =
             selectSingleNode(containerDoc, "/container/properties/version/number/text()").getNodeValue();
 
         List<String> smMembersList = getStructMapMembers(this.theContainerXml);
 
         // create a second version
         addMembers(theContainerId, "<param last-modification-date=\"" + getLastModificationDateValue(containerDoc)
             + "\" >\n<id>" + createItem() + "</id>\n</param>");
 
         // check retrieveMembers method (latest version)
         String memberListXml = retrieveMembers(this.theContainerId, new HashMap<String, String[]>());
         List<String> mlMembersList = getMemberListMembers(memberListXml);
 
         assertListContentEqual("Member list does not contain the same IDs as struct map.", mlMembersList,
            getStructMapMembers(retrieve(this.theContainerId)));
         assertXmlValidSrwResponse(memberListXml);
 
         // check retrieveMembers method with version suffix (first version)
         String memberListXmlFirst = retrieveMembers(this.theContainerId + ":1", new HashMap<String, String[]>());
         List<String> mlMembersListFirst = getMemberListMembers(memberListXmlFirst);
 
         assertListContentEqual("Member list does not contain the same IDs as struct map.", mlMembersListFirst,
             smMembersList);
         assertXmlValidSrwResponse(memberListXmlFirst);
 
         // check retrieveMembers method with version suffix (latest version)
         String memberListXmlLatest =
             retrieveMembers(this.theContainerId + ":" + versionNumber, new HashMap<String, String[]>());
         List<String> mlMembersListLatest = getMemberListMembers(memberListXmlLatest);
 
         assertListContentEqual("Member list does not contain the same IDs as struct map.", mlMembersListLatest,
             getStructMapMembers(this.theContainerId + ":" + versionNumber));
         assertXmlValidSrwResponse(memberListXmlLatest);
 
     }
 
     /**
      * Retrieve members: nonexisting container id.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_RFLMC_2() throws Exception {
         try {
             retrieveMembers("escidoc:nonexist1", new HashMap<String, String[]>());
             fail("No exception on retrieve members of nonexisting container.");
         }
         catch (final Exception e) {
             Class<?> ec = ContainerNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected on retrieve members.", ec, e);
         }
     }
 
     /**
      * Retrieve members: container id not provided.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_RFLMC_4_1() throws Exception {
         try {
             retrieveMembers(null, new HashMap<String, String[]>());
             fail("No exception on retrieve members with container id = null.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected on retrieve members.", ec, e);
         }
     }
 
     @Test
     public void testCompareRetrieveContainerByTwoMethods_IssueINFR_657() throws Exception {
         String container1Xml = retrieve(theContainerId);
 
         Map<String, String[]> parameters = new HashMap<String, String[]>();
 
         parameters.put(FILTER_PARAMETER_QUERY, new String[] { "\"/id\"=" + theContainerId });
         String container2Xml = retrieveContainers(parameters);
         Document container1Document = getDocument(container1Xml);
         Document container2Document = getDocument(container2Xml);
         String lmdInContainer1 =
             selectSingleNode(container1Document, "/container/@last-modification-date").getNodeValue();
         String versionDateInContainer1 =
             selectSingleNode(container1Document, "/container/properties/version/date").getTextContent();
         assertEquals("last modification date and version date are not equal", versionDateInContainer1, lmdInContainer1);
         String lmdInContainer2 =
             selectSingleNode(container2Document, XPATH_SRW_CONTAINER_LIST_CONTAINER + "/@last-modification-date")
                 .getNodeValue();
         String versionDateInContainer2 =
             selectSingleNode(container2Document, XPATH_SRW_CONTAINER_LIST_CONTAINER + "/properties/version/date")
                 .getTextContent();
         assertEquals("last modification date and version date are not equal", versionDateInContainer2, lmdInContainer2);
 
     }
 
     /**
      * Test successfully retrieving of container.
      */
     @Test
     public void testOM_RC_1_1() throws Exception {
         String containerXml = retrieve(theContainerId);
 
         assertXmlValidContainer(containerXml);
     }
 
     /**
      * Test declining retrieving of container with non existing id.
      */
     @Test
     public void testOM_RC_2() throws Exception {
 
         try {
             retrieve("bla");
             fail("No exception occurred on retrieve with non existing id.");
         }
         catch (final Exception e) {
             Class<?> ec = ContainerNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test declining retrieving of container with missing id.
      */
     @Test
     public void testOM_RC_3() throws Exception {
 
         try {
             retrieve(null);
             fail("No exception occurred on retrieve with missing id.");
         }
         catch (final Exception e) {
             // Class<?> ec = ContainerNotFoundException.class;
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test retrieving all Containers from the repository. The list of Containers is afterwards checked with retrieving
      * each Container separately. Note: This test checks not if one of the Containers is missing in the List!
      *
      * @throws Exception If one of the Container in the list is not retrievable.
      */
     @Test
     public void testRetrieveContainers() throws Exception {
 
         String xml = retrieveContainers(new HashMap<String, String[]>());
 
         assertXmlValidSrwResponse(xml);
 
         NodeList nodes = null;
         nodes = selectNodeList(EscidocAbstractTest.getDocument(xml), XPATH_SRW_CONTAINER_LIST_CONTAINER + "/@href");
 
         assertContainers(nodes);
     }
 
     @Test
     public void testRetrievePendingContainers() throws Exception {
         doTestFilterContainersStatus("pending", false);
     }
 
     @Test
     public void testRetrievePendingVersionContainers() throws Exception {
         doTestFilterContainersStatus("pending", true);
     }
 
     @Test
     public void testRetrieveSubmittedContainers() throws Exception {
         doTestFilterContainersStatus("submitted", false);
     }
 
     @Test
     public void testRetrieveSubmittedVersionContainers() throws Exception {
         doTestFilterContainersStatus("submitted", true);
     }
 
     @Test
     public void testRetrieveMembers() throws Exception {
         // make list from containers struct map
         Document container = EscidocAbstractTest.getDocument(retrieve(theContainerId));
         NodeList smMembers = selectNodeList(container, "/container/struct-map/*/@href");
 
         // make list from containers member list
         String xml = retrieveMembers(theContainerId, new HashMap<String, String[]>());
         NodeList mlMembers =
             selectNodeList(EscidocAbstractTest.getDocument(xml), XPATH_SRW_RESPONSE_OBJECT + "*/@href");
         List<String> smMembersList = nodeList2List(smMembers);
         List<String> mlMembersList = nodeList2List(mlMembers);
 
         assertListContentEqual("Member list does not contain the same IDs as struct map.", mlMembersList, smMembersList);
         assertXmlValidSrwResponse(xml);
     }
 
     @Test
     public void testRetrievePendingMembers() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\"" + FILTER_URI_PUBLIC_STATUS + "\"=pending" });
 
         String xml = retrieveMembers(theContainerId, filterParams);
 
         assertXmlValidSrwResponse(xml);
 
         Document xmlDoc = EscidocAbstractTest.getDocument(xml);
         NodeList memberIds = selectNodeList(xmlDoc, XPATH_SRW_RESPONSE_OBJECT + "*/@href");
 
         for (int i = memberIds.getLength() - 1; i >= 0; i--) {
             String id = memberIds.item(i).getNodeValue();
             selectSingleNodeAsserted(xmlDoc, XPATH_SRW_RESPONSE_OBJECT + "*[@href = '" + id
                 + "']/properties/public-status[text() = 'pending']");
         }
     }
 
     @Test
     public void testRetrieveSubmittedItemMembers() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\"" + FILTER_URI_PUBLIC_STATUS + "\"=submitted and "
             + "\"" + RDF_TYPE_NS_URI + "\"=\"" + RESOURCES_NS_URI + "Item\"" });
 
         String xml = retrieveMembers(theContainerId, filterParams);
 
         assertXmlValidSrwResponse(xml);
 
         Document xmlDoc = EscidocAbstractTest.getDocument(xml);
         NodeList memberIds = selectNodeList(xmlDoc, XPATH_SRW_RESPONSE_OBJECT + "*/@href");
 
         for (int i = memberIds.getLength() - 1; i >= 0; i--) {
             String id = memberIds.item(i).getNodeValue();
             selectSingleNodeAsserted(xmlDoc, "/member-list/*[@href = '" + id
                 + "']/properties/public-status[text() = 'submitted']");
         }
     }
 
     @Test
     public void testRetrievePendingContainerMembers() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_QUERY, new String[] { "\"" + FILTER_URI_PUBLIC_STATUS + "\"=pending and "
             + "\"" + RDF_TYPE_NS_URI + "\"=\"" + RESOURCES_NS_URI + "Container\"" });
 
         String xml = retrieveMembers(theContainerId, filterParams);
 
         assertXmlValidSrwResponse(xml);
 
         Document xmlDoc = EscidocAbstractTest.getDocument(xml);
         NodeList memberIds = selectNodeList(xmlDoc, XPATH_SRW_RESPONSE_OBJECT + "*/@objid");
         for (int i = memberIds.getLength() - 1; i >= 0; i--) {
             String id = memberIds.item(i).getNodeValue();
             selectSingleNodeAsserted(xmlDoc, "/member-list/container[@href = '" + id
                 + "']/properties/public-status[text() = 'pending']");
         }
     }
 
     /**
      * Check if all Members of a Container are part of the retrieveMembers() representation. See also Bug #638, where
      * only the Items could be retrieved and not Container.
      *
      * @throws Exception e
      */
     @Test
     public void testRetrievingMembers() throws Exception {
 
         int maxContainer = 1;
         int maxItem = 1;
 
         // creating Container --------------------------------------------------
         String containerId = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
         // create multiple Resource (item/Container) ---------------------------
         Vector<String> ids = new Vector<String>();
         for (int i = 0; i < maxItem; i++) {
             String id = createItemFromTemplate("escidoc_item_198_for_create.xml");
             ids.add(id);
         }
         for (int i = 0; i < maxContainer; i++) {
             String id = createContainerFromTemplate("create_container_WithoutMembers_v1.1.xml");
             ids.add(id);
         }
 
         Document containerDoc = getDocument(retrieve(containerId));
         String lmd = getLastModificationDateValue(containerDoc);
 
         String taskParam = createAddMemberTaskParam(ids, lmd);
         addMembers(containerId, taskParam);
 
         // check if retrieveMembers contains exactly the kind of objects -------
         String memberListXml = retrieveMembers(containerId, new HashMap<String, String[]>());
         List<String> members = getMemberListMembers(memberListXml);
 
         // converting hrefs to objids
         for (int i = 0; i < members.size(); i++) {
             String objid = getObjidFromHref(members.get(i));
             members.set(i, objid);
         }
 
         // compare the members with ids
         for (int i = 0; i < ids.size(); i++) {
             if (!members.contains(ids.get(i))) {
                 throw new Exception("Added Member '" + ids.get(i) + "' not part of the member list.");
             }
         }
     }
 
     /**
      * Create a Container with whitespaces in md-records attribute name. This has to be fail with a schema exception.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_MdRecords() throws Exception {
 
         Class<?> ec = XmlSchemaValidationException.class;
 
         String nameWS = "MD-Records Descriptor Name with whitespaces";
 
         Document context = EscidocAbstractTest.getTemplateAsDocument(this.path, "create_container.xml");
         substitute(context, "/container/md-records/md-record[2]/@name", nameWS);
         String template = toString(context, false);
 
         try {
             create(template);
             fail(ec + " expected but no error occurred!");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Create a Container with more than the allowed number of characters in md-records attribute name. The length is
      * limited to 64 charachter. This has to be fail with a schema exception.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testOM_MdRecords2() throws Exception {
 
         Class<?> ec = XmlSchemaValidationException.class;
 
         String nameLong =
             "MD-Records_Attribute_Name_without_whitespaces_but_"
                 + "extra_long_to_reach_the_64_character_limit_of_fedora_other" + "_things_are_not_tested";
 
         Document context = EscidocAbstractTest.getTemplateAsDocument(this.path, "create_container.xml");
         substitute(context, "/container/md-records/md-record[2]/@name", nameLong);
         String template = toString(context, false);
 
         try {
             create(template);
             fail(ec + " expected but no error occurred!");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test retrieving struct-map of a container.
      * <p/>
      * Bugzilla #585 (http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=585)
      *
      * @throws Exception Thrown if retrieving fails.
      */
     @Test
     public void testRetrievingStructMap() throws Exception {
 
         String containerXml = retrieve(theContainerId);
 
         assertXmlExists("struct-map", containerXml, "/container/struct-map");
 
         String structMap = retrieveStructMap(theContainerId);
         // assertXmlStructMap(structMap);
     }
 
     /***************************************************************************
      * private methods
      * **********************************************************************
      */
 
     /**
      * Prepare the TaskParam for addMember.
      *
      * @param members              Vector with id of member candidates.
      * @param lastModificationDate The last modification date of the resource (Container).
      * @return TaskParam
      */
     private String createAddMemberTaskParam(final Vector<String> members, final String lastModificationDate) {
 
         String taskParam = "<param last-modification-date=\"" + lastModificationDate + "\" ";
         taskParam += ">\n";
 
         for (int i = 0; i < members.size(); i++) {
             taskParam += "<id>" + members.get(i) + "</id>\n";
         }
         taskParam += "</param>";
 
         return taskParam;
     }
 
     /**
      * Get the ids of the StructMap members.
      */
     private List<String> getStructMapMembers(final String xml) throws Exception {
         Document container = EscidocAbstractTest.getDocument(xml);
         NodeList smMembers = selectNodeList(container, "/container/struct-map/*/@href");
         return nodeList2List(smMembers);
     }
 
     /**
      * Get the ids of the memberList members.
      *
      * @param xml XML escidoc member-list.
      * @return Objids exctracted from the member list.
      */
     private List<String> getMemberListMembers(final String xml) throws Exception {
         // make list from containers member list
         NodeList mlMembers =
             selectNodeList(EscidocAbstractTest.getDocument(xml), XPATH_SRW_RESPONSE_OBJECT + "*/@href");
         return nodeList2List(mlMembers);
     }
 
     private void doTestFilterContainersStatus(final String reqStatus, final boolean versionStatus) throws Exception {
 
         String filterName = FILTER_URI_PUBLIC_STATUS;
         String filterResultXPath = "/container/properties/public-status/text()";
         if (versionStatus) {
             filterName = "" + FILTER_URI_VERSION_STATUS;
             filterResultXPath = "/container/properties/version/status/text()";
         }
 
         String list = null;
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
         StringBuffer filter = new StringBuffer("\"" + filterName + "\"=" + reqStatus);
 
         if (versionStatus) {
             filter.append(" and " + "\"" + FILTER_URI_PUBLIC_STATUS + "\"=released");
         }
         filterParams.put(FILTER_PARAMETER_QUERY, new String[] { filter.toString() });
         list = retrieveContainers(filterParams);
         assertXmlValidSrwResponse(list);
 
         NodeList nodes =
             selectNodeList(EscidocAbstractTest.getDocument(list), XPATH_SRW_CONTAINER_LIST_CONTAINER + "/@href");
 
         for (int count = nodes.getLength() - 1; count >= 0; count--) {
             Node node = nodes.item(count);
             String nodeValue = getIdFromHrefValue(node.getNodeValue());
 
             try {
                 String container = retrieve(nodeValue);
                 String containerStatus =
                     selectSingleNode(EscidocAbstractTest.getDocument(container), filterResultXPath).getNodeValue();
                 assertEquals(reqStatus, containerStatus);
             }
             catch (final ContainerNotFoundException e) {
                 if (reqStatus.equals(STATUS_WITHDRAWN)) {
                     EscidocAbstractTest.assertExceptionType(ItemNotFoundException.class, e);
                 }
                 else {
                     fail("No container could be retrieved with id " + nodeValue + " returned by retrieveContainerRefs.");
                 }
             }
 
         }
     }
 
     private void assertContainers(NodeList nodes) throws Exception {
         if (nodes.getLength() == 0) {
             fail("No containers found.");
         }
 
         for (int count = nodes.getLength() - 1; count >= 0; count--) {
             Node node = nodes.item(count);
             String nodeValue = node.getNodeValue();
             try {
                 nodeValue = getIdFromURI(nodeValue);
                 String container = retrieve(nodeValue);
                 assertXmlValidContainer(container);
             }
             catch (final de.escidoc.core.common.exceptions.remote.system.FedoraSystemException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw e;
             }
         }
     }
 
     /**
      * Create Item from template.
      *
      * @return objid of Item.
      * @throws Exception Thrown if creation or id extraction failed.
      */
     private String createItem() throws Exception {
 
         // create an item and save the id
         String xmlData =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest/", "escidoc_item_198_for_create.xml");
 
         String theItemXml = handleXmlResult(getItemClient().create(xmlData));
         return getObjidValue(theItemXml);
     }
 
     private void doTestFilterMembersUserRole(final String id, final String reqUser, final String reqRole)
         throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
         String filter = "";
 
         if (reqUser != null) {
             filter += "\"user\"=" + reqUser;
         }
         if (reqRole != null) {
             if (filter.length() > 0) {
                 filter += " and ";
             }
             filter += "\"role\"=" + reqRole;
         }
         filterParams.put(FILTER_PARAMETER_QUERY, new String[] { filter });
 
         NodeList items = null;
         NodeList containers = null;
 
         String list = retrieveMembers(id, filterParams);
         items = selectNodeList(EscidocAbstractTest.getDocument(list), "/member-list/member/item/@href");
         containers = selectNodeList(EscidocAbstractTest.getDocument(list), "/member-list/member/container/@href");
 
         for (int count = containers.getLength() - 1; count >= 0; count--) {
             Node node = containers.item(count);
             String nodeValue = node.getNodeValue();
             nodeValue = getIdFromURI(nodeValue);
             try {
                 retrieve(nodeValue);
             }
             catch (final ContainerNotFoundException e) {
                 throw e;
             }
 
         }
         for (int count = items.getLength() - 1; count >= 0; count--) {
             Node node = items.item(count);
             String nodeValue = node.getNodeValue();
             nodeValue = getIdFromURI(nodeValue);
             try {
                 handleXmlResult(getItemClient().retrieve(nodeValue));
 
             }
             catch (final ItemNotFoundException e) {
                 throw e;
             }
 
         }
     }
 
     /**
      *
      * @param memberIds
      * @return
      */
     private String getMemberRefList(List<String> memberIds) {
 
         String result =
             "<member-ref-list:member-ref-list><member-ref-list:member>" + "<member-ref-list:item-ref  "
                 + XLINK_TYPE_ESCIDOC + "=\"simple\" " + XLINK_HREF_ESCIDOC + "=\"/ir/item/" + theItemId + "\" objid=\""
                 + theItemId + "\"/>";
 
         if (memberIds != null) {
             // FIXME this methods does nothing useful
             Iterator<String> it = memberIds.iterator();
             while (it.hasNext()) {
                 String id = it.next();
             }
         }
 
         result += "</member-ref-list:member>" + "</member-ref-list:member-ref-list>";
 
         return result;
     }
 
     /**
      * Test successfully retrieving md-record.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testRetrieveMdRecord() throws Exception {
         retrieveMdRecord(true, "escidoc");
     }
 
     /**
      * Test decline retrieving md-record without container ID.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithoutItemID() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(false, "escidoc");
         }
         catch (final Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test decline retrieving md-record with no name.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithoutName() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, null);
         }
         catch (final Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test decline retrieving md-record with empty name.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testRetrieveMdRecordWithEmptyName() throws Exception {
         Class ec = MissingMethodParameterException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, "");
         }
         catch (final Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test successfully retrieving md-record with non existing name.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testRetrieveMdRecordNonExistingName() throws Exception {
         Class ec = MdRecordNotFoundException.class;
         String msg = "Expected " + ec.getName();
         try {
             retrieveMdRecord(true, "blablub");
         }
         catch (final Exception e) {
             assertExceptionType(msg, ec, e);
         }
     }
 
     /**
      * Test successfully retrieving an explain response.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testExplainRetrieveContainers() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_EXPLAIN, new String[] { "" });
 
         String result = null;
 
         try {
             result = retrieveContainers(filterParams);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidSrwResponse(result);
     }
 
     /**
      * Test successfully retrieving an explain response.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testExplainRetrieveMembers() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_EXPLAIN, new String[] { "" });
 
         String result = null;
 
         try {
             result = retrieveMembers(theContainerId, filterParams);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidSrwResponse(result);
     }
 
     /**
      * Test successfully retrieving an explain response.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testExplainRetrieveTocs() throws Exception {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         filterParams.put(FILTER_PARAMETER_EXPLAIN, new String[] { "" });
 
         String result = null;
 
         try {
             result = retrieveTocs(theContainerId, filterParams);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException(e);
         }
         assertXmlValidSrwResponse(result);
     }
 
     /**
      * Test if the objid is handles right.
      * <p/>
      * see issue INFR-773
      * <p/>
      * The tests creates an Container with one Item as Member and uses then on the Item handler the objid of the Item
      * with and without version suffix. The framework has to answer with ContainerNotFoundException in all cases.
      *
      * @throws Exception If framework behavior is not as expected.
      */
     @Test
     public void testWrongObjid01() throws Exception {
 
         // create container
         String containerTemplXml = getContainerTemplate("create_container.xml");
         containerTemplXml = containerTemplXml.replaceAll("escidoc:persistent3", "escidoc:ex1");
         String containerXml = create(containerTemplXml);
         String containerId = getObjidValue(containerXml);
 
         String itemTemplXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest/", "item_without_component.xml");
 
         String itemXml = createItem(containerId, itemTemplXml);
         String itemId = getObjidValue(itemXml);
 
         try {
             retrieve(itemId);
         }
         catch (final Exception e) {
             assertExceptionType("Wrong exception", ContainerNotFoundException.class, e);
         }
 
         try {
             retrieve(itemId + ":1");
         }
         catch (final Exception e) {
             assertExceptionType("Wrong exception", ContainerNotFoundException.class, e);
         }
 
         try {
             retrieve(itemId + ":a");
         }
         catch (final Exception e) {
             assertExceptionType("Wrong exception", ContainerNotFoundException.class, e);
         }
     }
 
     /**
      * Test https://www.escidoc.org/jira/browse/INFR-916
      *
      * It should be possible to get the container member list even if the
      * container id contains the version number. Of course ths will only work
      * for the latest version.
      *
      * @throws Exception
      *             If framework behavior is not as expected.
      */
     @Test
     public void testIssue916() throws Exception {
         // create container
         String containerId =
             getObjidValue(create(getContainerTemplate("create_container.xml").replaceAll("escidoc:persistent3",
                 "escidoc:ex1")));
 
         // create item
         createItem(containerId, EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest/",
             "item_without_component.xml"));
 
         // retrieve container members without version number
         String members1Xml = retrieveMembers(containerId, new HashMap<String, String[]>());
 
         assertXmlValidSrwResponse(members1Xml);
 
         Document members1 = getDocument(members1Xml);
         NodeList members1List = selectNodeList(members1, XPATH_SRW_RESPONSE_OBJECT + "/*/@href");
 
         // retrieve container members with version number
         String members2Xml = retrieveMembers(containerId + ":2", new HashMap<String, String[]>());
 
         assertXmlValidSrwResponse(members2Xml);
 
         Document members2 = getDocument(members2Xml);
         NodeList members2List = selectNodeList(members2, XPATH_SRW_RESPONSE_OBJECT + "/*/@href");
 
         // compare both lists
         assertListContentEqual("Member lists differ.", nodeList2List(members1List), nodeList2List(members2List));
     }
 
     /**
      * Creates an Item and retrieves the md-record by given name.
      *
      * @param resourceId If the retrieve should be done with resource ID.
      * @param name       The name of the md-record to be retrieved.
      * @throws Exception If an error occurs.
      */
     private void retrieveMdRecord(final boolean resourceId, final String name) throws Exception {
         if (!resourceId) {
             this.theContainerId = null;
         }
 
         String retrievedMdRecord = retrieveMetadataRecord(this.theContainerId, name);
         assertCreatedMdRecord(name, this.theContainerId, "container", retrievedMdRecord, this.theContainerXml,
             startTimestamp);
     }
 }
