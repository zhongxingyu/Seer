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
 package de.escidoc.core.test.om.item;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyExistsException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the mock implementation of the item resource.
  * 
  * @author MSC
  * 
  */
 @RunWith(value = Parameterized.class)
 public class ItemContentRelationsTest extends ItemTestBase {
     private String itemId = null;
 
     private String itemXml = null;
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public ItemContentRelationsTest(final int transport) {
         super(transport);
     }
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Override
     @Before
     public void setUp() throws Exception {
 
         super.setUp();
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         itemXml = create(itemWithoutComponents);
         this.itemId = getObjidValue(itemXml);
 
         // Node itemObjiId = selectSingleNode(getDocument(itemXml),
         // "/item/@objid");
         // String itemId = itemObjiId.getTextContent();
         // this.itemId = itemId;
     }
 
    // FIXME reactivate after 1.3
    //@Test
     public void testIssueInfr1007() throws Exception {
         addRelation(
             itemId,
             "http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf");
         addRelation(itemId, "http://escidoc.org/examples/test1");
         addRelation(itemId, "http://escidoc.org/examples/#test2");
 
         String relationsElementXml = retrieveRelations(this.itemId);
         Document relationsElementDocument =
             EscidocRestSoapTestBase.getDocument(relationsElementXml);
         selectSingleNodeAsserted(relationsElementDocument, "/relations");
         selectSingleNodeAsserted(
             relationsElementDocument,
             "/relations/relation[@predicate = 'http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf']");
         selectSingleNodeAsserted(relationsElementDocument,
             "/relations/relation[@predicate = 'http://escidoc.org/examples/test1']");
         selectSingleNodeAsserted(relationsElementDocument,
             "/relations/relation[@predicate = 'http://escidoc.org/examples/#test2']");
         assertXmlValidItem(relationsElementXml);
     }
 
     /**
      * Tets successfully adding a new relation to the item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelation() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml1 = create(itemWithoutComponents);
 
         String xml2 = create(itemWithoutComponents);
         String targetId1 = getObjidValue(xml1);
         String targetId2 = getObjidValue(xml2);
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId1);
         targets.add(targetId2);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameterWithUmlaut(lastModDate, targets);
         addContentRelations(this.itemId, taskParam);
         String itemWithRelations = retrieve(this.itemId);
         assertXmlValidItem(itemWithRelations);
         Document itemWithRelationsDocument =
             EscidocRestSoapTestBase.getDocument(itemWithRelations);
 
         NodeList relationTargets = null;
         if (Constants.TRANSPORT_REST == getTransport()) {
             relationTargets =
                 selectNodeList(itemWithRelationsDocument,
                     "/item/relations/relation/@href");
         }
         else {
             relationTargets =
                 selectNodeList(itemWithRelationsDocument,
                     "/item/relations/relation/@objid");
 
         }
         boolean contains1 = false;
         boolean contains2 = false;
 
         for (int i = relationTargets.getLength() - 1; i >= 0; i--) {
             String id = relationTargets.item(i).getNodeValue();
             if (id.matches(".*" + targetId1 + "$")) {
                 contains1 = true;
             }
             if (id.matches(".*" + targetId2 + "$")) {
                 contains2 = true;
             }
 
         }
 
         assertTrue("added relation targetId1 is not container the "
             + "+ relation list ", contains1);
         assertTrue("added relation targetId2 is not container the "
             + "+ relation list ", contains2);
 
         // and retrieve relations only and check
         String relationsElementXml = retrieveRelations(this.itemId);
         selectSingleNodeAsserted(EscidocRestSoapTestBase
             .getDocument(relationsElementXml), "/relations");
         assertXmlValidRelations(relationsElementXml);
 
         NodeList relations =
             selectNodeList(EscidocRestSoapTestBase
                 .getDocument(itemWithRelations), "/item/relations/relation");
         assertEquals("Number of relations is wrong ", relations.getLength(), 2);
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithoutId() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml1 = create(itemWithoutComponents);
         String xml2 = create(itemWithoutComponents);
         String targetId1 = getObjidValue(xml1);
         String targetId2 = getObjidValue(xml2);
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId1);
         targets.add(targetId2);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
         try {
             addContentRelations(null, taskParam);
             fail("No exception when add content relation without source id.");
         }
         catch (Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithoutTaskParam() throws Exception {
         try {
             addContentRelations(this.itemId, null);
             fail("No exception when add content relation without source id.");
         }
         catch (Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining adding of an relation with a non existing target to the
      * item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithNonExistingTarget() throws Exception {
         String targetId = "bla";
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         try {
             addContentRelations(this.itemId, taskParam);
             fail("No exception occurred on added an relation with non "
                 + "existing target to the item");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "ReferencedResourceNotFoundException.",
                 ReferencedResourceNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining adding of an relation with a non existing predicate to the
      * item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithNonExistingPredicate() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml1 = create(itemWithoutComponents);
 
         String xml2 = create(itemWithoutComponents);
         String targetId1 = getObjidValue(xml1);
         String targetId2 = getObjidValue(xml2);
         Vector<String> targets = new Vector<String>();
         targets.add(targetId1);
         targets.add(targetId2);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets, "bla");
 
         try {
             addContentRelations(this.itemId, taskParam);
             fail("No exception occurred on added an relation with non "
                 + "existing predicate to the item");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "RelationPredicateNotFoundException.",
                 RelationPredicateNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining adding of an relation with a target id containing a
      * version number.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithTargetContainingVersionNumber()
         throws Exception {
 
         Vector<String> targets = new Vector<String>();
         targets.add("escidoc:123:1");
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         try {
             addContentRelations(this.itemId, taskParam);
             fail("No exception occurred on added an relation with target"
                 + " containing version number to the item");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "InvalidContentException.", InvalidContentException.class, e);
         }
     }
 
     // Test method is obsolete, because it tests the obsolete
     // interface methods
     // /**
     // * Tets successfully adding an existing "inactive" relation to the item.
     // * @throws Exception
     // */
     //     @Test
     // public void testAddExistingInvalidRelation() throws Exception {
     // Document xmlItem = getTemplateAsDocument(TEMPLATE_ITEM_PATH,
     // "escidoc_item_198_for_create" + getTransport(true) + ".xml");
     // Node xmlItemWithoutComponents = deleteElement(xmlItem,
     // "/item/components");
     // String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
     //
     // String xml = create(itemWithoutComponents);
     //
     // String targetId = selectSingleNode(getDocument(xml),
     // "/item/@objid").getTextContent();
     //        
     // Vector targets = new Vector();
     // targets.add(targetId);
     // String lastModDate = getTheLastModificationParam(this.itemId);
     // String taskParam = getTaskParametrForAddRelations(lastModDate, targets);
     // String addedRelations = addContentRelations(this.itemId, taskParam);
     // String relationId = selectSingleNode(getDocument(addedRelations),
     // "/param/relation[1]/@objid").getTextContent();
     // String xmlWithRelation = retrieve(this.itemId);
     // Document item = getDocument(xmlWithRelation);
     // Node xmlItemWithoutFirstRelations = deleteElement(item,
     // "/item/relations");
     // String updatedXml = update(this.itemId, toString(
     // xmlItemWithoutFirstRelations, true));
     // lastModDate = getTheLastModificationParam(this.itemId);
     // taskParam = getTaskParametrForAddRelations(lastModDate, targets);
     // addedRelations = addContentRelations(this.itemId, taskParam);
     // String itemXml = retrieve(this.itemId);
     // String retrivedRelationId = selectSingleNode(getDocument(itemXml),
     // "/item/relations/relation[1]/@objid").getTextContent();
     // assertEquals("relation ids are not equal", relationId,
     // retrivedRelationId);
     // }
     /**
      * Test declining adding of an existing relation to the item.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddExistingRelation() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml = create(itemWithoutComponents);
         String targetId = getObjidValue(xml);
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.itemId, taskParam);
         lastModDate = getTheLastModificationParam(this.itemId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             addContentRelations(this.itemId, taskParam);
             fail("No exception occurred on added an existing relation to the item");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "AlreadyExistException expected.",
                 AlreadyExistsException.class, e);
         }
 
     }
 
     /**
      * Test successfully removing an existing relation.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveRelation() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml = create(itemWithoutComponents);
         String targetId = getObjidValue(xml);
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.itemId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.itemId);
 
         taskParam = getTaskParameter(lastModDate, targets);
         removeContentRelations(this.itemId, taskParam);
         String itemWithoutContentRelations = retrieve(this.itemId);
         Document itemWithoutContentRelationsDoc =
             EscidocRestSoapTestBase.getDocument(itemWithoutContentRelations);
         Node relations =
             selectSingleNode(itemWithoutContentRelationsDoc,
                 "/item/relations/relation");
         assertNull("relations may not exist", relations);
     }
 
     /**
      * Test declining removing of a already deleted relation.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveDeletedRelation() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml = create(itemWithoutComponents);
         String targetId = getObjidValue(xml);
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.itemId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.itemId);
 
         taskParam = getTaskParameter(lastModDate, targets);
         removeContentRelations(this.itemId, taskParam);
         String itemWithoutContentRelations = retrieve(this.itemId);
         Document itemWithoutContentRelationsDoc =
             EscidocRestSoapTestBase.getDocument(itemWithoutContentRelations);
         Node relations =
             selectSingleNode(itemWithoutContentRelationsDoc,
                 "/item/relations/relation");
         assertNull("relations may not exist", relations);
         lastModDate = getTheLastModificationParam(this.itemId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             removeContentRelations(this.itemId, taskParam);
             fail("No exception occurred on remove a already deleted relation");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "ContentRelationNotFoundException expected.",
                 ContentRelationNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining removing of an existing relation, which belongs to another
      * source resource.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveRelationWithWrongSource() throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml1 = create(itemWithoutComponents);
         String xml2 = create(itemWithoutComponents);
         String targetId = getObjidValue(xml1);
         String sourceId = getObjidValue(xml2);
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
 
         String lastModDate = getTheLastModificationParam(sourceId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(sourceId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.itemId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             removeContentRelations(this.itemId, taskParam);
             fail("No exception occurred on remove an relation with a wrong source");
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.assertExceptionType(
                 "ContentRelationNotFoundException expected.",
                 ContentRelationNotFoundException.class, e);
         }
 
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelations() throws Exception {
         addRelation(itemId, null);
 
         String relationsElementXml = retrieveRelations(this.itemId);
         selectSingleNodeAsserted(EscidocRestSoapTestBase
             .getDocument(relationsElementXml), "/relations");
         assertXmlValidItem(relationsElementXml);
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveNonexistingRelations() throws Exception {
         try {
             String relationsElementXml = retrieveRelations(this.itemId);
             Node relationsElementDoc =
                 EscidocRestSoapTestBase.getDocument(relationsElementXml);
             selectSingleNodeAsserted(relationsElementDoc, "/relations");
             assertNull(selectSingleNode(relationsElementDoc, "/relations/*"));
         }
         catch (Exception e) {
             Class ec = ResourceNotFoundException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelationsWithWrongId() throws Exception {
         addRelation(itemId, null);
 
         try {
             retrieveRelations("bla");
             fail("No exception when retrieveRelations with wrong id.");
         }
         catch (Exception e) {
             Class ec = ItemNotFoundException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelationsWithoutId() throws Exception {
         addRelation(itemId, null);
 
         try {
             retrieveRelations(null);
             fail("No exception when retrieveRelations without id.");
         }
         catch (Exception e) {
             Class ec = MissingMethodParameterException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test the last-modification-date in the return value of
      * addContentRelations.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRelationReturnValue01() throws Exception {
 
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
 
         String xml1 = create(itemWithoutComponents);
 
         String xml2 = create(itemWithoutComponents);
         String targetId1 = getObjidValue(xml1);
         String targetId2 = getObjidValue(xml2);
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId1);
         targets.add(targetId2);
 
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         String resultXml = addContentRelations(this.itemId, taskParam);
         assertXmlValidResult(resultXml);
 
         Document resultDoc = EscidocRestSoapTestBase.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(resultDoc);
 
         String itemWithRelations = retrieve(this.itemId);
 
         Document itemDoc =
             EscidocRestSoapTestBase.getDocument(itemWithRelations);
         String lmdItem = getLastModificationDateValue(itemDoc);
 
         assertEquals("Last modification date of result and item not equal",
             lmdResult, lmdItem);
 
         // now test last-modification-date of removeContentRelations
 
         targets = new Vector<String>();
         targets.add(targetId2);
 
         taskParam = getTaskParameter(lmdItem, targets);
         resultXml = removeContentRelations(this.itemId, taskParam);
         assertXmlValidResult(resultXml);
 
         resultDoc = EscidocRestSoapTestBase.getDocument(resultXml);
         String lmdResultRemove = getLastModificationDateValue(resultDoc);
 
         String itemWithOutRelations = retrieve(this.itemId);
 
         itemDoc = EscidocRestSoapTestBase.getDocument(itemWithOutRelations);
         lmdItem = getLastModificationDateValue(itemDoc);
 
         assertEquals("Last modification date of result and item not equal",
             lmdResultRemove, lmdItem);
     }
 
     /**
      * @param objectId
      *            The id of the object to which the relation should be added.
      *            The source id.
      * @param predicate
      *            The predicate of the relation.
      * @throws Exception
      *             If anything fails.
      */
     private void addRelation(final String objectId, final String predicate)
         throws Exception {
         Document xmlItem =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
         Node xmlItemWithoutComponents =
             deleteElement(xmlItem, "/item/components");
         String itemWithoutComponents = toString(xmlItemWithoutComponents, true);
         String createdItem = create(itemWithoutComponents);
         String targetId = getObjidValue(createdItem);
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.itemId);
         String taskParam = getTaskParameter(lastModDate, targets, predicate);
 
         addContentRelations(this.itemId, taskParam);
 
     }
 
     /**
      * @param id
      *            The id of the resource.
      * @return The date of last modification of the resource as string.
      * @throws Exception
      *             If anything fails.
      */
     private String getTheLastModificationParam(final String id)
         throws Exception {
         Document item = EscidocRestSoapTestBase.getDocument(retrieve(id));
 
         // get last-modification-date
         NamedNodeMap atts = item.getDocumentElement().getAttributes();
         Node lastModificationDateNode =
             atts.getNamedItem("last-modification-date");
         String lastModificationDate = lastModificationDateNode.getNodeValue();
 
         return lastModificationDate;
     }
 
     /**
      * @param lastModDate
      *            The last modification date of the source.
      * @param targets
      *            List of target ids. As much relations are added as there are
      *            tagets.
      * @return The task parameter according to the given values.
      */
     private String getTaskParameter(
         final String lastModDate, final Vector<String> targets) {
         return getTaskParameter(lastModDate, targets, null);
     }
 
     /**
      * Get taskParameter with german Umlaut.
      * 
      * @param lastModDate
      *            last-modification-date
      * @param targets
      *            vector with targets
      * @return task-parameter (for task oriented methods)
      */
     private String getTaskParameterWithUmlaut(
         final String lastModDate, final Vector<String> targets) {
 
         return getTaskParameter(lastModDate, targets,
             "http://www.escidoc.de/ontologies/mpdl-ontologies/"
                 + "content-relations#isTest\u00dc\u00c4\u00d6");
     }
 
     /**
      * @param lastModDate
      *            The last modification date of the source.
      * @param targets
      *            List of target ids. As much relations are added as there are
      *            tagets.
      * @param predicate
      *            The predicate of the relation.
      * @return The task parameter according to the given values.
      */
     private String getTaskParameter(
         final String lastModDate, final Vector<String> targets,
         final String predicate) {
         String taskParam = null;
         if ((targets != null) && (targets.size() > 0)) {
             taskParam =
                 "<param last-modification-date=\"" + lastModDate + "\">";
             Iterator<String> it = targets.iterator();
             while (it.hasNext()) {
                 String target = (String) it.next();
                 taskParam = taskParam + "<relation><targetId>";
                 taskParam = taskParam + target + "</targetId>";
                 taskParam = taskParam + "<predicate>";
                 if (predicate != null) {
                     taskParam = taskParam + predicate;
                 }
                 else {
                     taskParam =
                         taskParam + "http://www.escidoc.de/ontologies/"
                             + "mpdl-ontologies/content-relations#isPartOf";
 
                 }
                 taskParam = taskParam + "</predicate></relation>";
             }
             taskParam = taskParam + "</param>";
         }
         return taskParam;
     }
 
     // /**
     // * Test successfully retrieving a last version of an item, which has an
     // * active relation in the last version but not in the old version.
     // * @throws Exception
     // */
     // public void testRelationsWithVersionedItem() throws Exception {
     // String param = "<param last-modification-date=\""
     // + getTheLastModificationParam(this.itemId) + "\" ";
     // param += "/>";
     //    
     // submit(this.itemId, param);
     // String submittedItem = retrieve(this.itemId);
     //    
     // String target = create(getTemplateAsString(TEMPLATE_ITEM_PATH,
     // "escidoc_item_198_for_create.xml"));
     // String targetId = null;
     // Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
     // Matcher m = PATTERN_OBJID_ATTRIBUTE.matcher(target);
     // if (m.find()) {
     // targetId = m.group(1);
     // }
     // Vector targets = new Vector();
     // targets.add(targetId);
     // String lastModDate = getTheLastModificationParam(this.itemId);
     // String taskParam = getTaskParametrForAddRelations(lastModDate, targets);
     // String addedRelations = addContentRelations(this.itemId, taskParam);
     // String relationId = selectSingleNode(getDocument(addedRelations),
     // "/param/relation[1]/@objid").getTextContent();
     // String submittedWithRelations = retrieve(this.itemId);
     // String newItemXml = addCtsElement(submittedWithRelations);
     //     
     // String updatedItem = update(itemId, newItemXml);
     // String itemVersion1 = retrieve(this.itemId + ":" + 1);
     // String item = retrieve(this.itemId);
     // Node relations = selectSingleNode(getDocument(itemVersion1),
     // "/item/relations");
     // assertNull("relations may not exist", relations);
     // String retrievedRelationId = selectSingleNode(getDocument(item),
     // "/item/relations/relation[1]/@objid").getTextContent();
     // assertEquals("relation ids are not equal", relationId,
     // retrievedRelationId);
     //    
     // }
     //    
     //    
     //
 }
