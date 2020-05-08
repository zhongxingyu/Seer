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
 package de.escidoc.core.test.om.container.rest;
 
 import static org.junit.Assert.fail;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.util.List;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.escidoc.core.test.om.container.ContainerTestBase;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 /**
  * Container tests with REST transport.
  * 
  * @author MSC
  * 
  */
 public class ContainerCreateRestTest extends ContainerTestBase {
 
     /**
      * Constructor.
      * 
      */
     public ContainerCreateRestTest() {
         super(Constants.TRANSPORT_REST);
     }
 
     @Test
     public void testOM_CCO_1_1() throws Exception {
 
         String container =
             getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         assertXmlValidContainer(container);
         final String theContainerXml = create(container);
         if (log.isDebugEnabled()) {
             log.debug("container  " + theContainerXml);
         }
         assertXmlValidContainer(theContainerXml);
 
         Document document =
             EscidocRestSoapTestBase.getDocument(theContainerXml);
         String containerId = getObjidValue(document);
 
         assertXmlEquals("href value is wrong", document,
             XPATH_CONTAINER_XLINK_HREF, "/ir/container/" + containerId);
         Node containerTitle =
             selectSingleNode(document, XPATH_CONTAINER_XLINK_TITLE);
         assertNotNull(containerTitle);
         // assertFalse("container title is not set", containerTitle
         // .getTextContent().equals(""));
         assertXmlEquals("href value is wrong", document,
             "/container/md-records/@href ", "/ir/container/" + containerId
                 + "/md-records");
         assertXmlEquals("href value is wrong", document,
             "/container/md-records/md-record[1]/@href ", "/ir/container/"
                 + containerId + "/md-records/md-record/escidoc");
         assertXmlEquals("href value is wrong", document,
             "/container/properties/@href ", "/ir/container/" + containerId
                 + "/properties");
         assertXmlEquals("href value is wrong", document,
             "/container/properties/version/@href ", "/ir/container/"
                 + containerId + ":1");
         assertXmlEquals("href value is wrong", document,
             "/container/properties/latest-version/@href ", "/ir/container/"
                 + containerId + ":1");
         Node creatorId =
             selectSingleNode(document, "/container/properties/created-by/@href");
         assertNotNull(creatorId.getTextContent());
 
         Node latestRelease =
             selectSingleNode(document, "/container/properties/latest-release");
         assertNull(latestRelease);
         Node itemRef =
             selectSingleNode(document,
                 "/container/struct-map/member-ref-list/item-ref");
         assertNull(itemRef);
         Node containerRef =
             selectSingleNode(document,
                 "/container/struct-map/member-ref-list/container-ref");
         assertNull(containerRef);
 
         Node modifiedDate =
             selectSingleNode(document, "/container/@last-modification-date");
         assertNotNull(modifiedDate);
         assertFalse("modified date is not set", modifiedDate
             .getTextContent().equals(""));
         assertXmlEquals("status value is wrong", document,
             "/container/properties/public-status", "pending");
         assertXmlEquals("pid value is wrong", document,
             "/container/properties/pid", "hdl:123/container456");
         Node createdDate =
             selectSingleNode(document, "/container/properties/creation-date");
         assertNotNull(createdDate);
         assertFalse("created date is not set", createdDate
             .getTextContent().equals(""));
         assertXmlEquals("lock-status value is wrong", document,
             "/container/properties/lock-status", "unlocked");
         assertXmlEquals("current version number is wrong", document,
             "/container/properties/version/number", "1");
         assertXmlEquals("current version status is wrong", document,
             "/container/properties/version/status", "pending");
         // assertXmlEquals("current version valid-status is wrong", document,
         // "/container/properties/version/valid-status", "valid");
         assertXmlEquals("current version date is wrong", document,
             "/container/properties/version/date", modifiedDate.getTextContent());
         assertXmlEquals("latest version number is wrong", document,
             "/container/properties/latest-version/number", "1");
         assertXmlEquals("latest version date is wrong", document,
             "/container/properties/latest-version/date", modifiedDate
                 .getTextContent());
         Node creator =
             selectSingleNode(document, "/container/properties/created-by");
         assertNotNull(creator);
         Node creatorTitle =
             selectSingleNode(document,
                 "/container/properties/created-by/@title");
         assertNotNull(creatorTitle.getTextContent());
     }
 
     /**
      * Test successfully creating container.
      * 
      * @test.name container with correct obligatory data and correct optional
      *            data
      * @test.id OM_CCO_1-2
      * @test.input Container XML
      * @test.inputDescription Container XML with correct obligatory data (as
      *                        described in OM_CCO_1-1) and optional data: <br>
      *                        member href (container-ref) <br>
      *                        member objid (container-ref) <br>
      *                        admin-descriptor according to admin-descriptor.xsd
      * 
      * 
      * @test.expected Conainer XML now containing the parameters which are set
      *                by the system: (as described in OM_CCO_1-1)
      */
     @Test
     public void testOM_CCO_1_2() throws Exception {
 
         String xmlData1 =
             getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
 
         assertXmlValidContainer(xmlData1);
         final String theContainerXml1 = create(xmlData1);
         assertXmlValidContainer(theContainerXml1);
 
         String subContainerId1 = getObjidValue(theContainerXml1);
 
         final String theContainerXml2 = create(xmlData1);
         assertXmlValidContainer(theContainerXml2);
 
         String subContainerId2 = getObjidValue(theContainerXml2);
 
         String xmlData =
             getContainerTemplate("create_container_v1.1-forContainer.xml");
 
         String xmlWithContainer1 =
             xmlData.replaceAll("##CONTAINERID1##", subContainerId1);
         String xmlWithContainer2 =
             xmlWithContainer1.replaceAll("##CONTAINERID2##", subContainerId2);
         Document document =
             EscidocRestSoapTestBase.getDocument(xmlWithContainer2);
         NodeList members =
             selectNodeList(document,
                 "/container/struct-map/member-ref-list/member/container-ref/@href");
         final String theContainerXml = create(xmlWithContainer2);
         assertXmlValidContainer(theContainerXml);
         final Document createdDocument =
             EscidocRestSoapTestBase.getDocument(theContainerXml);
         NodeList membersAfterCreate =
             selectNodeList(createdDocument,
                 "/container/struct-map/member-ref-list/member/container-ref/@href");
 
         List<String> membersList = nodeList2List(members);
         List<String> membersListAfterCreate = nodeList2List(membersAfterCreate);
 
         assertListContentEqual(
             "Member list does not contain the same IDs as struct map.",
             membersList, membersListAfterCreate);
 
         Node itemRef =
             selectSingleNode(createdDocument, "/container/struct-map/item");
         assertNull(itemRef);
     }
 
     /**
      * Test declining creating container with missing Context href.
      * 
      * @test.name Container with missing obd (Context href)
      * @test.id OM_CCO_2-3
      * @test.input Container XML
      * @test.inputDescription Container XML with correct obligatory data as
      *                        described in OM_CCO_1-1 except Context href
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_2_3() throws Exception {
 
         Document container =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
         Node containerWithMissingContextHref =
             deleteAttribute(container, "/container/properties/context",
                 XLINK_HREF_TEMPLATES);
         String containerWithMissingContextHrefXml =
             toString(containerWithMissingContextHref, false);
 
         try {
             create(containerWithMissingContextHrefXml);
             fail("No exception occured on create with missing context href.");
 
         }
         catch (Exception e) {
             Class< ? > ec = XmlCorruptedException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec.getName()
                 + " expected.", ec, e);
             return;
         }
 
     }
 
     /**
      * Test declining creating container with Context href with wrong syntax.
      * 
      * @test.name Container with wrong objid (Context href)
      * @test.id OM_CCO_2-2
      * @test.input Container XML
      * @test.inputDescription Container XML with correct obligatory data as
      *                        described in OM_CCO_1-1 except Context href
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_2_2() throws Exception {
         Document container =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
         Node containerWithWrongContextHref =
             substitute(container, "/container/properties/context/@href",
                 "/ir/bla");
         String containerWithWrongContextHrefXml =
             toString(containerWithWrongContextHref, false);
         try {
             create(containerWithWrongContextHrefXml);
         }
         catch (ContextNotFoundException e) {
             return;
         }
         fail("Not expected exception");
 
     }
 
     /**
      * Test declining creating container with non existing context.
      * 
      * @test.name Container with wrong obd (Context non existing)
      * @test.id OM_CCO_3
      * @test.input Container XML
      * @test.inputDescription Container XML with correct obligatory data as
      *                        described in OM_CCO_1-1 except Context href
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_3() throws Exception {
         Document container =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
         Node containerWithWrongContextId =
             substitute(container, "/container/properties/context/@href",
                 "/ir/context/bla");
         String containerWithWrongContextIdXml =
             toString(containerWithWrongContextId, false);
         try {
             create(containerWithWrongContextIdXml);
         }
         catch (ContextNotFoundException e) {
             return;
         }
         fail("Not expected exception");
 
     }
 
     /**
      * Test declining creating container with id of the context, which responses
      * to another object type than context.
      * 
      * @test.name Container with wrong obd (Context with wrong object id)
      * @test.id OM_CCO_4
      * @test.input Container XML
      * @test.inputDescription Container XML with correct obligatory data as
      *                        described in OM_CCO_1-1 except Context
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_4() throws Exception {
         Document container =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
         Node containerWithWrongContextObjectType =
             substitute(container, "/container/properties/context/@href",
                 "/ctm/context/escidoc:persistent4");
         String containerWithWrongContextObjectTypeXml =
             toString(containerWithWrongContextObjectType, false);
         try {
             create(containerWithWrongContextObjectTypeXml);
         }
         catch (ContextNotFoundException e) {
             return;
         }
         fail("Not expected exception");
 
     }
 
     /**
      * Test declining creating container without any md-record.
      * 
      * @test.name container with missing obligatory datat (Escidoc Internal
      *            Metadata Set)
      * @test.id OM_CCO_5
      * @test.input Incorrect Container XML
      * @test.inputDescription Container XML correct obligatory data espect
      *                        Escidoc Internal Metadata Set.
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_5() throws Exception {
         Document container =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
 
         Node attributeMdRecordName =
             selectSingleNode(container,
                 "/container/md-records/md-record[1]/@name");
         String nameValue = attributeMdRecordName.getTextContent();
         Node containerWithoutEscidocMetadata = null;
         if (nameValue.equals("escidoc")) {
             containerWithoutEscidocMetadata =
                 deleteElement(container, "/container/md-records/md-record[1]");
         }
         String containerWithoutEscidocMetadataXml =
             toString(containerWithoutEscidocMetadata, true);
 
         try {
             create(containerWithoutEscidocMetadataXml);
         }
         catch (InvalidXmlException e) {
             return;
         }
         fail("Not expected exception");
     }
 
     /**
      * Test declining creating container with missing Escidoc Internal Metadata
      * Set.
      * 
      * @test.name CI with missing obd (Escidoc Internal Metadata Set)
      * @test.id OM_CI_2-3
      * @test.input Item XML (no content components)
      * @test.inputDescription Item XML correct obligatory data (no List of
      *                        Content Components) as described in OM_CI_1-1
      *                        except Escidoc Internal Metadata Set
      * @test.expected Error message
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOMCi2e() throws Exception {
         Document xmlContainer =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
 
         // Node attribute =
         // selectSingleNode(xmlItemWithoutComponents,
         // "/item/md-records/md-record/[@name = 'escidoc']/@name");
         // "/item/md-records/md-record/publication");
         Node xmlContainerWithoutInternalMetadata =
             substitute(xmlContainer,
                 "/container/md-records/md-record[@name = 'escidoc']/@name",
                 "bla");
         String xmlContainerWithoutInternalMetadataXml =
             toString(xmlContainerWithoutInternalMetadata, true);
 
         Class< ? > ec = MissingMdRecordException.class;
         try {
             create(xmlContainerWithoutInternalMetadataXml);
             EscidocRestSoapTestBase.failMissingException(ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test successfully creating container with two relations.
      * 
      * @throws Exception
      *             Thrown if anything fails.
      */
     @Test
     public void testRelations() throws Exception {
         String containerXml1 =
             create(getContainerTemplate(
                 "create_container_WithoutMembers_v1.1.xml"));
         assertXmlValidContainer(containerXml1);
         String containerXml2 =
             create(getContainerTemplate(
                 "create_container_WithoutMembers_v1.1.xml"));
         assertXmlValidContainer(containerXml2);
         Document document1 =
             EscidocRestSoapTestBase.getDocument(containerXml1);
         String createdContainerId1 = getObjidValue(document1);
         Document document2 =
             EscidocRestSoapTestBase.getDocument(containerXml2);
         String createdContainerId2 = getObjidValue(document2);
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
         String containerForCreateWithRelationsXml =
             getContainerTemplate(
                 "create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##",
                 createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##",
                 createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF2##", href2);
         Document containerForCreateWithRelations =
             EscidocRestSoapTestBase
                 .getDocument(containerForCreateWithRelationsXml);
 
         NodeList relations =
             selectNodeList(containerForCreateWithRelations,
                 "/container/relations/relation");
 
         String xml = create(containerForCreateWithRelationsXml);
 
         NodeList relationsAfterCreate =
             selectNodeList(EscidocRestSoapTestBase.getDocument(xml),
                 "/container/relations/relation");
 
         assertXmlValidContainer(xml);
         assertEquals("Number of relations is wrong ", relations.getLength(),
             relationsAfterCreate.getLength());
 
     }
 
     /**
      * Test declining creating container with relations, whose targets
      * references non existing resources.
      * 
      * @throws Exception
      */
     @Test
     public void testRelationsWithWrongTarget() throws Exception {
 
         String createdContainerId1 = "bla1";
         String createdContainerId2 = "bla2";
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
         String containerForCreateWithRelationsXml =
             getContainerTemplate(
                 "create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##",
                 createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##",
                 createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF2##", href2);
 
         try {
             create(containerForCreateWithRelationsXml);
             fail("No exception occured on container create with relations, which "
                 + " references non existing targets.");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "ReferencedResourceNotFoundException expected.",
                 ReferencedResourceNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining creating container with relations, whose target ids
      * containing a version number.
      * 
      * @throws Exception
      */
     @Test
     public void testRelationsWithTargetContainingVersionNumber()
         throws Exception {
         String createdContainerId1 = "escidoc:123:2";
         String createdContainerId2 = "escidoc:123:3";
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
         String containerForCreateWithRelationsXml =
             getContainerTemplate(
                 "create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##",
                 createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##",
                 createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF2##", href2);
         try {
             create(containerForCreateWithRelationsXml);
             fail("No exception occured on container crate with relations, which "
                 + " target ids containing a version number.");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "InvalidContentException expected.",
                 InvalidContentException.class, e);
         }
     }
 
     /**
      * Test declining creating container with relations with non existing
      * predicate.
      * 
      * @throws Exception
      */
     @Test
     public void testRelationsWithWrongPredicate() throws Exception {
 
         String createdContainerId1 =
             createContainerFromTemplate(
                 "create_container_WithoutMembers_v1.1.xml");
         String createdContainerId2 =
             createContainerFromTemplate(
                 "create_container_WithoutMembers_v1.1.xml");
 
         String href1 = "/ir/container/" + createdContainerId1;
         String href2 = "/ir/container/" + createdContainerId2;
 
         String containerForCreateWithRelationsXml =
             getContainerTemplate(
                 "create_container_WithoutMembers_v1.1_WithRelations.xml");
 
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID1##",
                 createdContainerId1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll("##CONTAINER_ID2##",
                 createdContainerId2);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF1##", href1);
         containerForCreateWithRelationsXml =
             containerForCreateWithRelationsXml.replaceAll(
                 "##CONTAINER_HREF2##", href2);
         Document containerForCreateWithRelations =
             EscidocRestSoapTestBase
                 .getDocument(containerForCreateWithRelationsXml);
         Node relationPredicate =
             selectSingleNode(containerForCreateWithRelations,
                 "/container/relations/relation[1]/@predicate");
         relationPredicate.setNodeValue("http://www.bla.de#bla");
 
         String containerXml = toString(containerForCreateWithRelations, true);
 
         try {
             create(containerXml);
             fail("No exception occured on container create with relations, which "
                 + " references non existing predicate.");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "RelationPredicateNotFoundException expected.",
                 RelationPredicateNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining creation of Container with providing reference to context
      * with invalid href (substring context not in href).
      * 
      * @test.name Create Container - Context referenced with invalid href.
      * @test.id OM_CCO_13_1_rest
      * @test.input Container XML representation
      * @test.inputDescription: <ul>
      *                         <li>context referenced with an href that
      *                         specifies another resource type</li>
      *                         </ul>
      * @test.expected: ContextNotFoundException
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_13_1_rest() throws Exception {
 
         final Class< ? > ec = ContextNotFoundException.class;
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
 
         String href =
             selectSingleNodeAsserted(toBeCreatedDocument,
                 XPATH_CONTAINER_CONTEXT_XLINK_HREF).getTextContent();
         href =
             href.replaceFirst(Constants.CONTEXT_BASE_URI,
                 Constants.ORGANIZATIONAL_UNIT_BASE_URI);
         substitute(toBeCreatedDocument, XPATH_CONTAINER_CONTEXT_XLINK_HREF,
             href);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         try {
             create(toBeCreatedXml);
             EscidocRestSoapTestBase.failMissingException(
                 "Creating Container with invalid object href not declined. ",
                 ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating Container with invalid object href not declined,"
                     + " properly. ", ec, e);
         }
     }
 
     /**
      * Test declining creation of Container with providing reference to
      * content-model with invalid href (substring content-model not in href).
      * 
      * @test.name Create Container - content-model referenced with invalid href.
      * @test.id OM_CCO_13_2_rest
      * @test.input Container XML representation
      * @test.inputDescription: <ul>
      *                         <li>content-model referenced with an href that
      *                         specifies another resource type</li>
      *                         </ul>
      * @test.expected: ContentModelNotFoundException
      * 
      * @test.status Implemented
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_CCO_13_2_rest() throws Exception {
 
         final Class< ? > ec = ContentModelNotFoundException.class;
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
 
         String href =
             selectSingleNodeAsserted(toBeCreatedDocument,
                 XPATH_CONTAINER_CONTENT_TYPE_XLINK_HREF).getTextContent();
         href =
             href.replaceFirst(Constants.CONTENT_MODEL_BASE_URI,
                 Constants.ORGANIZATIONAL_UNIT_BASE_URI);
         substitute(toBeCreatedDocument,
             XPATH_CONTAINER_CONTENT_TYPE_XLINK_HREF, href);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         try {
             create(toBeCreatedXml);
             EscidocRestSoapTestBase.failMissingException(
                 "Creating Container with invalid object href not declined. ",
                 ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating Container with invalid object href not declined,"
                     + " properly. ", ec, e);
         }
     }
 
     /**
      * Test declinig creating an container without specifying the content model
      * id, using data provided for issue 365.
      * 
      * @test.name Create Container - Missing Content Model Id - Issue 365.
      * @test.id AA_CI-issue-365
      * @test.input Container XML representation with empty content model element
      *             (without href/objid).
      * @test.expected: MissingAttributeValueException
      * @test.status Implemented
      * @test.issue 
      *             http://www.escidoc-project.de/issueManagement/show_bug.cgi?id=
      *             365
      * 
      * @throws Exception
      *             Thrown if anythinf fails.
      */
     @Test
     public void testOM_CCO_issue365() throws Exception {
 
        final Class< ? > ec = XmlCorruptedException.class;
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_WithoutMembers_v1.1.xml"));
         deleteElement(toBeCreatedDocument, XPATH_CONTAINER_CONTENT_MODEL);
         addAfter(toBeCreatedDocument, XPATH_CONTAINER_CONTEXT,
             createElementNode(toBeCreatedDocument, SREL_NS_URI, "srel",
                 NAME_CONTENT_MODEL, null));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, true);
 
         try {
             create(toBeCreatedXml);
             EscidocRestSoapTestBase
                 .failMissingException(
                     "Creating container with empty content-model element not declined.",
                     ec);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "Creating container with empty content-model element not declined"
                     + ", properly", ec, e);
         }
 
     }
 
     /**
      * Test successfully creating of an container with 2 md-records.
      * 
      * @throws Exception
      */
     @Test
     public void testCreateContainerWith2Mdrecords() throws Exception {
         Document xmlContainer =
             EscidocRestSoapTestBase
                 .getDocument(getContainerTemplate(
                     "create_container_2_Md_Records_WithoutMembers_v1.1.xml"));
         NodeList mdrecords =
             selectNodeList(xmlContainer, "/container/md-records/md-record");
         String containerWithoutAdminDescriptorXml =
             toString(xmlContainer, false);
         assertXmlValidContainer(containerWithoutAdminDescriptorXml);
 
         final String createdXml = create(containerWithoutAdminDescriptorXml);
 
         assertXmlValidContainer(createdXml);
         final Document createdDocument =
             EscidocRestSoapTestBase.getDocument(createdXml);
 
         NodeList mdrecordsAfterCreate =
             selectNodeList(createdDocument, "/container/md-records/md-record");
         assertEquals(mdrecords.getLength(), mdrecordsAfterCreate.getLength());
 
     }
 
 }
