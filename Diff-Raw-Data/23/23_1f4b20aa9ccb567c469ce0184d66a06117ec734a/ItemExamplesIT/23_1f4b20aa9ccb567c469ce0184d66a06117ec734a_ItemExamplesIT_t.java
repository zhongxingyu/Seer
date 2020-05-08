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
 
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.compare.TripleStoreValue;
 import de.escidoc.core.test.common.fedora.Client;
 import org.apache.xpath.XPathAPI;
 import org.fcrepo.server.types.gen.Datastream;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 /**
  * Test creating the example Item objects.
  *
  * @author Steffen Wagner
  */
 public class ItemExamplesIT extends ItemTestBase {
 
     /**
      * Test if the example item for create is still compatible with framework.
      *
      * @throws Exception Thrown if creation of example Item failed.
      */
     @Test
     public void testCreatingExample01() throws Exception {
 
         String itemXml = getExampleTemplate("item-minimal-for-create-01.xml");
         String xml = create(itemXml);
         assertXmlValidItem(xml);
 
         Document xmlItem = getDocument(xml);
 
         // assert properties
         String xPath = "/item/properties/context[@href = '/ir/context/escidoc:ex1']";
         selectSingleNodeAsserted(xmlItem, xPath);
 
         // assert Components
         assertXmlNotExists("Created Item should not have a Component", xmlItem, "/item/components/component");
 
         TripleStoreValue tsv = new TripleStoreValue();
         tsv.itemTripleStoreValues(xmlItem);
 
         // assert version History --------------------------------------------
         String itemId = getObjidValue(xmlItem);
         String lmd = getLastModificationDateValue(xmlItem);
 
         String versionHistory = retrieveVersionHistory(itemId);
         Document wovDoc = EscidocAbstractTest.getDocument(versionHistory);
         Document itemDocV1 = EscidocAbstractTest.getDocument(retrieve(itemId + ":1"));
 
         // compare time stamps
         assertXmlExists("Wrong timestamp in version-history root element", versionHistory,
             "/version-history[@last-modification-date = '" + lmd + "']");
 
         assertXmlExists("Wrong timestamp in version element of version-history", versionHistory,
             "/version-history/version[@timestamp = '" + lmd + "']");
 
         // compare creation date with version-history entry
         assertEquals("creation date of Item [" + itemId + "] differs from timestamp of first event of version 1 "
             + "in version-history", XPathAPI
             .selectSingleNode(itemDocV1, "/item/properties/creation-date").getTextContent(), XPathAPI.selectSingleNode(
             wovDoc, "/version-history/version[version-number='1']" + "/events/event[1]/eventDateTime").getTextContent());
 
         // check timestamp of version 1
         assertEquals("last-modification-date root attribute of Item [" + itemId
             + "] differs from timestamp of latest event of version 1 " + "in version-history", XPathAPI
             .selectSingleNode(itemDocV1, "/item/@last-modification-date").getTextContent(), XPathAPI.selectSingleNode(
             wovDoc, "/version-history/version[version-number='1']" + "/events/event[1]/eventDateTime").getTextContent());
 
     }
 
     /**
      * Test if the example item for create is still compatible with framework.
      *
      * @throws Exception Thrown if creation of example Item failed.
      */
     @Test
     public void testCreatingExample02() throws Exception {
 
         String itemXml = getExampleTemplate("item-minimal-for-create-02.xml");
         String xml = create(itemXml);
         assertXmlValidItem(xml);
 
         Document xmlItem = getDocument(xml);
 
         // assert properties
         String xPath = "/item/properties/context[@href = '/ir/context/escidoc:ex1']";
         selectSingleNodeAsserted(xmlItem, xPath);
         xPath = "/item/properties/content-model[@href = " + "'/cmm/content-model/escidoc:ex4']";
         selectSingleNodeAsserted(xmlItem, xPath);
 
         // md-records
         selectSingleNodeAsserted(xmlItem, "/item/md-records/md-record[@name = 'escidoc']");
         selectSingleNodeAsserted(xmlItem, "/item/md-records/md-record/metadata");
 
         // assert Components
         selectSingleNodeAsserted(xmlItem, "/item/components/component");
 
         selectSingleNodeAsserted(xmlItem, "/item/components/component/properties/valid-status");
         selectSingleNodeAsserted(xmlItem, "/item/components/component/properties/visibility");
         selectSingleNodeAsserted(xmlItem, "/item/components/component/properties/content-category");
 
         selectSingleNodeAsserted(xmlItem, "/item/components/component/content[@storage = 'internal-managed']");
 
         TripleStoreValue tsv = new TripleStoreValue();
         tsv.itemTripleStoreValues(xmlItem);
     }
 
     /**
      * Test TripleStore values of created example 01 Item.
      *
      * @throws Exception Thrown if timestamp handling shows failure.
      */
     @Test
     public void testExample02TripleStoreValues() throws Exception {
 
         String itemXml = getExampleTemplate("item-minimal-for-create-02.xml");
         String xml = create(itemXml);
         assertXmlValidItem(xml);
 
         Document xmlItem = getDocument(xml);
         TripleStoreValue tsv = new TripleStoreValue();
 
         // check last-modification-date
         tsv.compareDocumentValueWithTripleStore(xmlItem, "/item/@last-modification-date", "/RDF/Description/date",
             "<http://escidoc.de/core/01/properties/version/date>");
 
         // check public-status
         tsv.compareDocumentValueWithTripleStore(xmlItem, "/item/properties/public-status",
             "/RDF/Description/public-status", "<http://escidoc.de/core/01/properties/public-status>");
 
         // check version number
         tsv.compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/number", "/RDF/Description/number",
             "<http://escidoc.de/core/01/properties/version/number>");
 
         // check resource type
         tsv.compareValueWithTripleStore(getObjidValue(xmlItem), "http://escidoc.de/core/01/resources/Item",
             "/RDF/Description/type/@resource", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
 
         // check version status
         tsv.compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/status", "/RDF/Description/status",
             "<http://escidoc.de/core/01/properties/version/status>");
 
         // // check component
         // tsv.compareDocumentValueWithTripleStore(xmlItem,
         // "/item/components/component/@objid", "/RDF/Description/component",
         // "<http://escidoc.de/core/01/structural-relations/component>");
 
     }
 
     /**
      * Test if the example item for create is still compatible with framework.
      *
      * @throws Exception Thrown if creation of example Item failed.
      */
     @Test
     public void testCreatingExample03() throws Exception {
 
         String itemXml = getExampleTemplate("item-minimal-for-create-03.xml");
         String xml = create(itemXml);
         assertXmlValidItem(xml);
 
         Document xmlItem = getDocument(xml);
         // assert Components
         selectSingleNodeAsserted(xmlItem, "/item/components/component");
         TripleStoreValue tsv = new TripleStoreValue();
         tsv.itemTripleStoreValues(xmlItem);
     }
 
     /**
      * Test if the example item for create is still compatible with framework.
      *
      * @throws Exception Thrown if creation of example Item failed.
      */
     @Test
     public void testCreatingExample04() throws Exception {
 
         String itemXml = getExampleTemplate("item-minimal-for-create-04.xml");
         String xml = create(itemXml);
         assertXmlValidItem(xml);
 
         Document xmlItem = getDocument(xml);
         // assert Component
         selectSingleNodeAsserted(xmlItem, "/item/components/component");
         TripleStoreValue tsv = new TripleStoreValue();
         tsv.itemTripleStoreValues(xmlItem);
     }
 
     /**
      * Test if versionable for version-history datastream is set to false.
      *
      * @throws Exception Thrown if creation of example Item failed.
      */
     @Test
     public void testCreatingExample05() throws Exception {
 
         String xml = getExampleTemplate("item-minimal-for-create-04.xml");
         String itemXml = create(xml);
 
         String objid = getObjidValue(itemXml);
         Client fc = new Client();
 
         Datastream ds = fc.getDatastreamInformation(objid, "version-history");
         assertFalse("Version-History should not be versioned", ds.isVersionable());
     }
 
 }
