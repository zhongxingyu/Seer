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
 package de.escidoc.core.test.common.compare;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.fedora.TripleStoreTestBase;
 import de.escidoc.core.test.common.util.Version;
 import de.escidoc.core.test.common.util.xml.Assert;
 import de.escidoc.core.test.common.util.xml.Select;
 import org.apache.xpath.XPathAPI;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 
 /**
  * Methods to compare values with TripleStore values.
  * 
  * @author Steffen Wagner
  */
 public class TripleStoreValue {
 
     private int transport = 0;
 
     /**
      * @param transport
      *            The transport protocol (REST/SOAP)
      * @throws Exception
      *             Thrown if loading configuration from properties failed.
      */
     public TripleStoreValue(final int transport) throws Exception {
 
         this.setTransport(transport);
     }
 
     /**
      * Test TripleStore values of Item.
      * 
      * @param xmlItem
      *            Item as XML Document representation
      * @throws Exception
      *             Thrown if timestamp handling shows failure.
      */
     public void itemTripleStoreValues(final Document xmlItem) throws Exception {
 
         Version version = new Version();
         if (!version.isLatestVersion(xmlItem, transport)) {
             return;
         }
 
         /*
          * non representation values
          */
         // retrieve frameworkj version from running instance
         EscidocRestSoapTestBase etb = new EscidocRestSoapTestBase(transport);
         String coreVersion = etb.obtainFrameworkVersion();
 
         // check build number
         compareValueWithTripleStore(Select.getObjidValueWithoutVersion(xmlItem, getTransport()), coreVersion,
             "/RDF/Description/build", "<http://escidoc.de/core/01/system/build>");
 
         // check last-modification-date
         // FIXME this is problematic because the timestamp is not within the
         // last RELS-EXT (until we change this)
         // compareDocumentValueWithTripleStore(xmlItem,
         // "/item/@last-modification-date", "/RDF/Description/date",
         // "<http://escidoc.de/core/01/properties/version/date>");
 
         // check public-status
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/public-status",
             "/RDF/Description/public-status", "<http://escidoc.de/core/01/properties/public-status>");
 
         // check public-status-comment
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/public-status-comment",
             "/RDF/Description/public-status-comment", "<http://escidoc.de/core/01/properties/public-status-comment>");
 
         // check resource type
         compareValueWithTripleStore(Select.getObjidValue(xmlItem, getTransport()),
             "http://escidoc.de/core/01/resources/Item", "/RDF/Description/type/@resource",
             "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
 
         // check version status
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/status", "/RDF/Description/status",
             "<http://escidoc.de/core/01/properties/version/status>");
 
         // check created-by
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/created-by/@href",
                 "/RDF/Description/created-by/@resource", "<http://escidoc.de/core/01/structural-relations/created-by>");
         }
         else {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/created-by/@objid",
                 "/RDF/Description/created-by/@resource", "<http://escidoc.de/core/01/structural-relations/created-by>");
         }
 
         // check created-by-title
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/created-by/@title",
                 "/RDF/Description/created-by-title", "<http://escidoc.de/core/01/properties/created-by-title>");
         }
 
         // check modifier
         if (XPathAPI.selectSingleNode(xmlItem, "/item/properties/modified-by") != null) {
             // check modified-by
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/modified-by",
                 "/RDF/Description/modified-by", "<http://escidoc.de/core/01/structural-relations/modified-by>");
 
             // check modified-by-title
             if (getTransport() == Constants.TRANSPORT_REST) {
                 compareDocumentValueWithTripleStore(xmlItem, "/item/properties/modified-by/@title",
                     "/RDF/Description/modified-by-title", "<http://escidoc.de/core/01/properties/modified-by-title>");
             }
         }
 
         // check content-model
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/content-model",
             "/RDF/Description/content-model", "<http://escidoc.de/core/01/structural-relations/content-model>");
 
         // check content-model-title
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/content-model/@title",
                 "/RDF/Description/content-model-title", "<http://escidoc.de/core/01/properties/content-model-title>");
         }
 
         // check context
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/context/@href",
                 "/RDF/Description/context/@resource", "<http://escidoc.de/core/01/structural-relations/context>");
         }
         else {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/context/@objid",
                 "/RDF/Description/context/@resource", "<http://escidoc.de/core/01/structural-relations/context>");
         }
 
         // check context-title (if rest)
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/context/@title",
                 "/RDF/Description/context-title", "<http://escidoc.de/core/01/properties/context-title>");
         }
 
         /*
          * Version Values
          * 
          * Only compare version values with TripleStore if the latest version is to compare.
          */
 
         // check version number
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/number", "/RDF/Description/number",
             "<http://escidoc.de/core/01/properties/version/number>");
 
         // check version date
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/date", "/RDF/Description/date",
             "<http://escidoc.de/core/01/properties/version/date>");
 
         // check version status
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/status", "/RDF/Description/status",
             "<http://escidoc.de/core/01/properties/version/status>");
 
         // check version valid-status
         if (XPathAPI.selectSingleNode(xmlItem, "/item/properties/version/valid-status") != null) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/valid-status",
                 "/RDF/Description/valid-status", "<http://escidoc.de/core/01/properties/version/valid-status>");
         }
 
         // check version comment
         compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/comment", "/RDF/Description/comment",
             "<http://escidoc.de/core/01/properties/version/comment>");
 
         // check version pid
         if (XPathAPI.selectSingleNode(xmlItem, "/item/properties/version/pid") != null) {
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/version/pid", "/RDF/Description/pid",
                 "<http://escidoc.de/core/01/properties/version/pid>");
         }
 
         /*
          * Release
          */
         if (XPathAPI.selectSingleNode(xmlItem, "/item/properties/release") != null) {
             // check latest release number
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/release/number", "/RDF/Description/number",
                 "<http://escidoc.de/core/01/properties/release/number>");
 
             // check latest release date
             compareDocumentValueWithTripleStore(xmlItem, "/item/properties/release/date", "/RDF/Description/date",
                 "<http://escidoc.de/core/01/properties/release/date>");
         }
 
         /*
          * Components
          */
     }
 
     /**
      * Test TripleStore values of Context.
      * 
      * @param xmlContext
      *            Item as XML Document representation
      * @throws Exception
      *             Thrown if timestamp handling shows failure.
      */
     public void contextTripleStoreValues(final Document xmlContext) throws Exception {
 
         /*
          * non representation values
          */
         // retrieve frameworkj version from running instance
         EscidocRestSoapTestBase etb = new EscidocRestSoapTestBase(transport);
         String coreVersion = etb.obtainFrameworkVersion();
 
         // check build number
         compareValueWithTripleStore(Select.getObjidValueWithoutVersion(xmlContext, getTransport()), coreVersion,
             "/RDF/Description/build", "<http://escidoc.de/core/01/system/build>");
 
         // check last-modification-date
         // compareDocumentValueWithTripleStore(xmlContext,
         // "/context/@last-modification-date", "/RDF/Description/date",
         // "<info:fedora/fedora-system:def/view#lastModifiedDate>");
 
         // check public-status
         compareDocumentValueWithTripleStore(xmlContext, "/context/properties/public-status",
             "/RDF/Description/public-status", "<http://escidoc.de/core/01/properties/public-status>");
 
         // check public-status-comment
         compareDocumentValueWithTripleStore(xmlContext, "/context/properties/public-status-comment",
             "/RDF/Description/public-status-comment", "<http://escidoc.de/core/01/properties/public-status-comment>");
 
         // check resource type
         compareValueWithTripleStore(Select.getObjidValue(xmlContext, getTransport()),
             "http://escidoc.de/core/01/resources/Context", "/RDF/Description/type/@resource",
             "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
 
         // check created-by
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlContext, "/context/properties/created-by/@href",
                 "/RDF/Description/created-by/@resource", "<http://escidoc.de/core/01/structural-relations/created-by>");
         }
         else {
             compareDocumentValueWithTripleStore(xmlContext, "/context/properties/created-by/@objid",
                 "/RDF/Description/created-by/@resource", "<http://escidoc.de/core/01/structural-relations/created-by>");
         }
 
         // check created-by-title
         if (getTransport() == Constants.TRANSPORT_REST) {
             compareDocumentValueWithTripleStore(xmlContext, "/context/properties/created-by/@title",
                 "/RDF/Description/created-by-title", "<http://escidoc.de/core/01/properties/created-by-title>");
         }
 
         // check modifier
         if (XPathAPI.selectSingleNode(xmlContext, "/context/properties/modified-by") != null) {
             // check modified-by
             compareDocumentValueWithTripleStore(xmlContext, "/context/properties/modified-by",
                 "/RDF/Description/modified-by", "<http://escidoc.de/core/01/structural-relations/modified-by>");
 
             // check modified-by-title
             if (getTransport() == Constants.TRANSPORT_REST) {
                 compareDocumentValueWithTripleStore(xmlContext, "/context/properties/modified-by/@title",
                     "/RDF/Description/modified-by-title", "<http://escidoc.de/core/01/properties/modified-by-title>");
             }
         }
 
         // check OUs
         NodeList ous =
             XPathAPI.selectNodeList(xmlContext, "/context/properties/organizational-units/organizational-unit");
         for (int i = 1; i < ous.getLength() + 1; i++) {
             String ouId = null;
             if (getTransport() == Constants.TRANSPORT_REST) {
                 Node ou =
                     XPathAPI.selectSingleNode(xmlContext,
                         "/context/properties/organizational-units/organizational-unit[" + i + "]/@href");
                 ouId = EscidocRestSoapTestBase.getObjidFromHref(ou.getTextContent());
             }
             else {
                 ouId =
                     XPathAPI
                         .selectSingleNode(xmlContext,
                             "/context/properties/organizational-units/organizational-unit[" + i + "]/@objid")
                         .getTextContent();
             }
 
            compareValuesWithTripleStore(EscidocRestSoapTestBase.getObjidValue(getTransport(), xmlContext),
                 "<info:fedora/" + ouId + ">", "/RDF/Description/organizational-unit",
                 "<http://escidoc.de/core/01/structural-relations/organizational-unit>");
         }
     }
 
     /**
      * Compares a value with the corresponding value in TripleStore.
      * 
      * @param objid
      *            objid of resource (for TripleStore subject).
      * @param value
      *            value which is to compare.
      * @param trsXPath
      *            XPath to the values in the TripleStore response.
      * @param trsPredicate
      *            TripleStore Predicate of the value.
      * @throws Exception
      *             Thrown if the value not exist (Either document or TripleStore) or both values are different.
      */
     public void compareValueWithTripleStore(
         final String objid, final String value, final String trsXPath, final String trsPredicate) throws Exception {
 
         String itemId = Select.getObjidValueWithoutVersion(objid);
         // call value from TripleStore
         TripleStoreTestBase tripleStore = new TripleStoreTestBase();
         String result = tripleStore.requestMPT("<info:fedora/" + itemId + "> " + trsPredicate + " *", "RDF/XML");
         Document resultDoc = EscidocRestSoapTestBase.getDocument(result);
 
         if (value != null) {
             // make sure only one value exist in TripleStore
             assertEquals("Value for predicate '" + trsPredicate
                 + "' not exists exactly one time in TripleStore (objid='" + itemId + "')", 1, XPathAPI.selectNodeList(
                 resultDoc, trsXPath).getLength());
 
             // compare values
             Node tripleStoreValue = XPathAPI.selectSingleNode(resultDoc, trsXPath);
             assertNotNull("Value not in TripleStore", tripleStoreValue);
             assertEquals("Values differ between required and TripleStore (objid='" + itemId + "', predicate='"
                 + trsPredicate + "'", value, tripleStoreValue.getTextContent());
         }
         else {
             // value must not exist in tripleStore
             assertNull("Value for predicate '" + trsPredicate + "' must not exists in TripleStore (objid='" + itemId
                 + "')", XPathAPI.selectSingleNode(resultDoc, trsXPath));
 
         }
     }
 
     /**
      * Compares a value with the corresponding value in TripleStore.
      * 
      * @param objid
      *            objid of resource (for TripleStore subject).
      * @param value
      *            value which is to compare.
      * @param trsXPath
      *            XPath to the values in the TripleStore response.
      * @param trsPredicate
      *            TripleStore Predicate of the value.
      * @throws Exception
      *             Thrown if the value not exist (Either document or TripleStore) or both values are different.
      */
     public void compareValuesWithTripleStore(
         final String objid, final String value, final String trsXPath, final String trsPredicate) throws Exception {
 
         String itemId = Select.getObjidValueWithoutVersion(objid);
         // call value from TripleStore
         TripleStoreTestBase tripleStore = new TripleStoreTestBase();
         String result = tripleStore.requestMPT("<info:fedora/" + itemId + "> " + trsPredicate + " " + value, "RDF/XML");
         Document resultDoc = EscidocRestSoapTestBase.getDocument(result);
 
         // make sure only one value exist in TripleStore
         assertEquals("Value for predicate '" + trsPredicate + "' and value '" + value
             + "' + not exists exactly one time in TripleStore (objid='" + itemId + "')", 1, XPathAPI.selectNodeList(
             resultDoc, trsXPath).getLength());
     }
 
     /**
      * Counts the number of triples for the provided object.
      * 
      * @param objid
      *            Id of the object.
      * @return number of triples for this object.
      * @throws Exception
      *             Thrown if request or counting failed.
      */
     public int countTriples(final String objid) throws Exception {
 
         String itemId = Select.getObjidValueWithoutVersion(objid);
         // call value from TripleStore
         TripleStoreTestBase tripleStore = new TripleStoreTestBase();
         String result = tripleStore.requestMPT("<info:fedora/" + itemId + "> * *", "RDF/XML");
         Document resultDoc = EscidocRestSoapTestBase.getDocument(result);
 
         return XPathAPI.selectNodeList(resultDoc, "/RDF/Description/*").getLength();
     }
 
     /**
      * Compare a value between the document an the responding value in TripleStore.
      * 
      * @param xmlItem
      *            The XML Document which is to compare with the corresponding values in TripleStore.
      * @param docXPath
      *            XPath to the Element (which is to compare) in the Document.
      * @param trsXPath
      *            XPath to the values in the TripleStore response.
      * @param trsPredicate
      *            TripleStore Predicate of the value.
      * @throws Exception
      *             Thrown if the value not exist (Either document or TripleStore) or both values are different.
      */
     public void compareDocumentValueWithTripleStore(
         final Document xmlItem, final String docXPath, final String trsXPath, final String trsPredicate)
         throws Exception {
 
         String itemId = Select.getObjidValueWithoutVersion(xmlItem, getTransport());
         String value = Assert.selectSingleNodeAsserted(xmlItem, docXPath).getTextContent();
 
         // if we compare identifier in href form
         if (value.startsWith("/")) {
             // try to drop the path from URL
             value = value.substring(value.lastIndexOf("/") + 1);
         }
 
         // if we compare relations than changes the id
         if (trsXPath.contains("@resource")) {
             value = "info:fedora/" + value;
         }
 
         compareValueWithTripleStore(itemId, value, trsXPath, trsPredicate);
     }
 
     /**
      * @param transport
      *            the transport to set
      */
     public void setTransport(final int transport) {
         this.transport = transport;
     }
 
     /**
      * @return the transport
      */
     public int getTransport() {
         return transport;
     }
 
 }
