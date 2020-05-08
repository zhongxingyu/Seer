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
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.apache.xpath.XPathAPI;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.net.URL;
 import java.util.List;
 import java.util.Vector;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Test the implementation of the container resource.
  *
  * @author Michael Schneider
  */
 public class ContainerVersioningIT extends ContainerTestBase {
 
     private String theContainerId;
 
     private String theContainerXml;
 
     private String theItemId;
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void setUp() throws Exception {
 
         this.theItemId = createItemFromTemplate("escidoc_item_198_for_create.xml");
 
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
         xmlData = xmlData.replaceAll("##ITEMID##", theItemId);
         theContainerXml = create(xmlData.replaceAll("##ITEMID##", theItemId));
         this.theContainerId = getObjidValue(this.theContainerXml);
     }
 
     @Test
     public void testRetrieveVersionHistory() throws Exception {
         String versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         Node versionHistoryDoc = EscidocAbstractTest.getDocument(versionHistory);
         selectSingleNodeAsserted(versionHistoryDoc, "/version-history");
         selectSingleNodeAsserted(versionHistoryDoc, "/version-history/version");
 
         // check href of version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":1");
         // check objid of version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
     }
 
     @Test
     public void testRetrieveVersionHistoryWithWrongId() throws Exception {
         try {
             retrieveVersionHistory("escidoc:foo");
             fail("Missing Exception");
         }
         catch (final Exception e) {
             Class<?> ec = ContainerNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     @Test
     public void testRetrieveVersionHistoryWithoutId() throws Exception {
         try {
             retrieveVersionHistory(null);
             fail("Missing Exception");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     @Test
     public void testIncrementVersionNumber() throws Exception {
         String versionHistory;
         int rounds = 10;
 
         for (int i = 1; i < rounds; i++) {
             assertXmlExists("New version number does not exist", theContainerXml,
                 "/container/properties/version/number[text() = '" + i + "']");
             versionHistory = retrieveVersionHistory(theContainerId);
             assertXmlValidVersionHistory(versionHistory);
             if (i == 1) {
                 assertXmlExists("Event 'create' ", versionHistory,
                     "/version-history/version[1]/events/event[1]/eventType[text() = 'create']");
             }
             else {
                 assertXmlExists("Event 'update' ", versionHistory,
                     "/version-history/version[1]/events/event[1]/eventType[text() = 'update']");
             }
             String newItemXml = addCtsElement(theContainerXml);
             theContainerXml = update(theContainerId, newItemXml);
         }
         assertXmlExists("New version number", theContainerXml, "/container/properties/version/number[text() = '"
             + rounds + "']");
         assertXmlValidContainer(theContainerXml);
 
         // retrieve versions
         String xml;
         for (int i = rounds; i > 0; i--) {
             xml = retrieve(theContainerId + ":" + i);
             // current-version properties
             assertXmlExists("Current-version number missmatch. (" + i + ")", xml,
                 "/container/properties/version/number[text() = '" + i + "']");
 
             if (i > 1) {
                 assertXmlExists("Version " + i + " content not as expected (positiv).", xml,
                     "/container/properties/content-model-specific/nox[" + (i - 1) + "]");
             }
             else {
                 assertXmlNotExists("Version " + i + " content not as expected (negativ).", EscidocAbstractTest
                     .getDocument(xml), "/container/properties/content-model-specific/nox[" + i + "]");
             }
             assertXmlValidContainer(xml);
         }
 
         // retrieve versions
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         Node versionHistoryDoc = EscidocAbstractTest.getDocument(versionHistory);
         selectSingleNodeAsserted(versionHistoryDoc, "/version-history");
         NodeList versions = selectNodeList(versionHistoryDoc, "/version-history/version");
         assertEquals(rounds, versions.getLength());
     }
 
     @Test
     public void testLifecycle() throws Exception {
         String xml;
         String versionHistory;
 
         // check status in properties, current-version,
         // TODO version-history
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties status submitted", xml, "/container/properties/public-status[text() = 'submitted']");
         assertXmlExists("version status not submitted", xml,
             "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event submitted ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'submitted']");
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
 
         xml = assignObjectPid(theContainerId, pidParam);
         String containerId = theContainerId + ":1";
 
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + containerId));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(xml)), assignPidParam);
 
         xml = assignVersionPid(containerId, pidParam);
 
         release(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status not released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlValidContainer(xml);
 
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event released ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'released']");
 
         withdraw(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties status withdrawn", xml, "/container/properties/public-status[text() = 'withdrawn']");
         assertXmlExists("version status must be released if withdrawn", xml,
             "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Further released withdrawn container latest-release", xml,
             "/container/properties/latest-release");
         assertXmlValidContainer(xml);
 
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event withdrawn ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'withdrawn']");
     }
 
     @Test
     public void testLifecycleVersions() throws Exception {
         String xml = theContainerXml;
         String versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         assertXmlExists("Properties status pending", xml, "/container/properties/public-status[text() = 'pending']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
 
         // check xlink:hrefs
         Document contDoc3 = getDocument(xml);
         assertEquals("Wrong root xlink:href", "/ir/container/" + theContainerId, XPathAPI.selectSingleNode(contDoc3,
             "/container/@href").getTextContent());
         assertEquals("Wrong properties xlink:href", "/ir/container/" + theContainerId + "/properties", XPathAPI
             .selectSingleNode(contDoc3, "/container/properties/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc3, "/container/properties/created-by/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/ir/context/escidoc:persistent3", XPathAPI.selectSingleNode(contDoc3,
             "/container/properties/context/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/cmm/content-model/escidoc:persistent6", XPathAPI.selectSingleNode(contDoc3,
             "/container/properties/content-model/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":1", XPathAPI.selectSingleNode(contDoc3,
             "/container/properties/version/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc3, "/container/properties/version/modified-by/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":1", XPathAPI.selectSingleNode(contDoc3,
             "/container/properties/latest-version/@href").getTextContent());
 
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/md-records", XPathAPI
             .selectSingleNode(contDoc3, "/container/md-records/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/md-records/md-record/escidoc",
             XPathAPI.selectSingleNode(contDoc3, "/container/md-records/md-record/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/struct-map", XPathAPI
             .selectSingleNode(contDoc3, "/container/struct-map/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/relations", XPathAPI
             .selectSingleNode(contDoc3, "/container/relations/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/resources", XPathAPI
             .selectSingleNode(contDoc3, "/container/resources/@href").getTextContent());
 
         assertXmlValidContainer(xml);
 
         Document versionHistoryDoc = getDocument(retrieveVersionHistory(theContainerId));
         // check href of version-history/version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":1");
         // check objid of version-history/version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
 
         /*
          * check ISSUE INFR-876
          */
         // /version-history/version[version-number='1']/events/event[1]/eventIdentifierValue
         // == '/ir/container/<objid>/resources/version-history#' +
         // /version-history/version[version-number='1']/events/event[1]/@xmlID
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='1']"
                     + "/events/event[1]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='1']" + "/events/event[1]/@xmlID").getTextContent());
 
         /*
          * check ISSUE INFR-877 (eventIdentifierValue)
          */
         // /version-history/version[version-number='1']/events/event[1]/eventIdentifierValue
         // == '/ir/container/<objid>/resources/version-history#' +
         // /version-history/version[version-number='1']/events/event[1]/@xmlID
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='1']"
                     + "/events/event[1]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='1']" + "/events/event[1]/@xmlID").getTextContent());
 
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
 
         // check xlink:hrefs
         Document contDoc = getDocument(xml);
         assertEquals("Wrong root xlink:href", "/ir/container/" + theContainerId, XPathAPI.selectSingleNode(contDoc,
             "/container/@href").getTextContent());
         assertEquals("Wrong properties xlink:href", "/ir/container/" + theContainerId + "/properties", XPathAPI
             .selectSingleNode(contDoc, "/container/properties/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc, "/container/properties/created-by/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/ir/context/escidoc:persistent3", XPathAPI.selectSingleNode(contDoc,
             "/container/properties/context/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/cmm/content-model/escidoc:persistent6", XPathAPI.selectSingleNode(contDoc,
             "/container/properties/content-model/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":2", XPathAPI.selectSingleNode(contDoc,
             "/container/properties/version/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc, "/container/properties/version/modified-by/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":2", XPathAPI.selectSingleNode(contDoc,
             "/container/properties/latest-version/@href").getTextContent());
 
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/md-records", XPathAPI
             .selectSingleNode(contDoc, "/container/md-records/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/md-records/md-record/escidoc",
             XPathAPI.selectSingleNode(contDoc, "/container/md-records/md-record/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/struct-map", XPathAPI
             .selectSingleNode(contDoc, "/container/struct-map/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/relations", XPathAPI
             .selectSingleNode(contDoc, "/container/relations/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + "/resources", XPathAPI
             .selectSingleNode(contDoc, "/container/resources/@href").getTextContent());
 
         versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         assertXmlValidContainer(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(theContainerId));
         // check href of version-history/version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":2");
         // check objid of version-history/version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
 
         // check version 1 again
         xml = retrieve(theContainerId + ":1");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         assertXmlExists("Properties status pending", xml, "/container/properties/public-status[text() = 'pending']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
         assertXmlValidContainer(xml);
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
         versionHistory = retrieveVersionHistory(theContainerId);
 
         xml = retrieve(theContainerId);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         assertXmlExists("Properties status submitted", xml, "/container/properties/public-status[text() = 'submitted']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
         versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '3']");
         assertXmlValidContainer(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(theContainerId));
         // check href of version-history/version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":3");
         // check objid of version-history/version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
 
         // check version 1 and 2 again
         xml = retrieve(theContainerId + ":1");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         assertXmlExists("Properties status submitted", xml, "/container/properties/public-status[text() = 'submitted']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":2");
         versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         assertXmlExists("Properties status submitted", xml, "/container/properties/public-status[text() = 'submitted']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + this.theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
 
         assignObjectPid(theContainerId, pidParam);
         String containerId = theContainerId + ":3";
 
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + containerId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(containerId))), assignPidParam);
 
         xml = assignVersionPid(containerId, pidParam);
 
         release(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '3']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Released container latest-release", xml, "/container/properties/latest-release");
         assertXmlValidContainer(xml);
 
         /*
          * Test issue INFR-1005
          * 
          * check hrefs for an older version if Container is released
          */
         // check xlink:hrefs
         Document contDoc2 = getDocument(retrieve(theContainerId + ":1"));
         assertEquals("Wrong root xlink:href", "/ir/container/" + theContainerId + ":1", XPathAPI.selectSingleNode(
             contDoc2, "/container/@href").getTextContent());
         assertEquals("Wrong properties xlink:href", "/ir/container/" + theContainerId + ":1" + "/properties", XPathAPI
             .selectSingleNode(contDoc2, "/container/properties/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc2, "/container/properties/created-by/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/ir/context/escidoc:persistent3", XPathAPI.selectSingleNode(contDoc2,
             "/container/properties/context/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/cmm/content-model/escidoc:persistent6", XPathAPI.selectSingleNode(contDoc2,
             "/container/properties/content-model/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":1", XPathAPI.selectSingleNode(contDoc2,
             "/container/properties/version/@href").getTextContent());
         assertEquals("Wrong xlink:href", "/aa/user-account/escidoc:testsystemadministrator", XPathAPI.selectSingleNode(
             contDoc2, "/container/properties/version/modified-by/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":3", XPathAPI.selectSingleNode(contDoc2,
             "/container/properties/latest-version/@href").getTextContent());
 
         assertEquals("Wrong xlink:href", "/ir/container/" + theContainerId + ":3", XPathAPI.selectSingleNode(contDoc2,
             "/container/properties/latest-release/@href").getTextContent());
 
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + ":1" + "/md-records", XPathAPI
             .selectSingleNode(contDoc2, "/container/md-records/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + ":1"
             + "/md-records/md-record/escidoc", XPathAPI.selectSingleNode(contDoc2,
             "/container/md-records/md-record/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + ":1" + "/struct-map", XPathAPI
             .selectSingleNode(contDoc2, "/container/struct-map/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + ":1" + "/relations", XPathAPI
             .selectSingleNode(contDoc2, "/container/relations/@href").getTextContent());
         assertEquals("Wrong resources xlink:href", "/ir/container/" + theContainerId + ":1" + "/resources", XPathAPI
             .selectSingleNode(contDoc2, "/container/resources/@href").getTextContent());
 
         // updates after version status released are now allowed
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '4']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
         assertXmlValidContainer(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(theContainerId));
         // check href of version-history/version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":4");
         // check objid of version-history/version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
 
         // check version 1 and 2 and 3 again
         xml = retrieve(theContainerId + ":1");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":2");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":3");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '3']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Released container latest-release", xml, "/container/properties/latest-release");
         assertXmlValidContainer(xml);
 
         // submit again
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
         xml = retrieve(theContainerId);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '4']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '5']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(theContainerId));
         // check href of version-history/version element
         assertHrefBase(versionHistoryDoc, "/version-history/version/@href", Constants.CONTAINER_BASE_URI + "/"
             + theContainerId + ":5");
         // check objid of version-history/version element
         assertHrefObjidConsistency((Document) versionHistoryDoc, "/version-history/version");
 
         // check version 1 and 2 and 3 and 4 again
         xml = retrieve(theContainerId + ":1");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status pending", xml, "/container/properties/version/status[text() = 'pending']");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":2");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":3");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '3']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Released container latest-release", xml, "/container/properties/latest-release");
         assertXmlValidContainer(xml);
         xml = retrieve(theContainerId + ":4");
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '4']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         // release again
         containerId = theContainerId + ":5";
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + containerId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(containerId))), assignPidParam);
 
         xml = assignVersionPid(containerId, pidParam);
 
         release(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '5']");
         assertXmlExists("Properties status released", xml, "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Released container latest-release", xml, "/container/properties/latest-release");
         assertXmlValidContainer(xml);
 
         withdraw(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties status withdrawn", xml, "/container/properties/public-status[text() = 'withdrawn']");
         assertXmlExists("version status withdrawn", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Further released withdrawn container latest-release", xml,
             "/container/properties/latest-release");
         assertXmlValidContainer(xml);
 
         /*
          * check ISSUE INFR-876
          */
         // /version-history/version[version-number='1']/events/event[1]/eventIdentifierValue
         // == '/ir/container/<objid>/resources/version-history#' +
         // /version-history/version[version-number='1']/events/event[1]/@xmlID
         for (int versionNumber = 1; versionNumber < 6; versionNumber++) {
             assertEquals(
                 "eventIdentifierValue " + " of Container [" + theContainerId + "] differs from defined format",
                 "/ir/container/"
                     + theContainerId
                     + "/resources/version-history#"
                     + XPathAPI
                         .selectSingleNode(
                             versionHistoryDoc,
                             "/version-history/version[version-number='" + versionNumber + "']"
                                 + "/events/event[1]/@xmlID").getTextContent(), XPathAPI.selectSingleNode(
                     versionHistoryDoc,
                     "/version-history/version[version-number='" + versionNumber + "']"
                         + "/events/event[1]/eventIdentifier/eventIdentifierValue").getTextContent());
         }
 
         /*
          * check ISSUE INFR-877 (eventIdentifierValue)
          */
         // checking only version 1 and 3
         // /version-history/version[version-number='1']/events/event[1]/eventIdentifierValue
         // == '/ir/container/<objid>/resources/version-history#' +
         // /version-history/version[version-number='1']/events/event[1]/@xmlID
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='1']"
                     + "/events/event[1]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='1']" + "/events/event[1]/@xmlID").getTextContent());
 
         // /version-history/version[version-number='3']/events/event[1]/eventIdentifierValue
         // == '/ir/container/<objid>/resources/version-history#' +
         // /version-history/version[version-number='3']/events/event[1]/@xmlID
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='3']"
                     + "/events/event[1]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='3']" + "/events/event[1]/@xmlID").getTextContent());
 
         // event 2
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='3']"
                     + "/events/event[2]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='3']" + "/events/event[2]/@xmlID").getTextContent());
 
         // event 3
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='3']"
                     + "/events/event[3]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='3']" + "/events/event[3]/@xmlID").getTextContent());
 
         // event 4
         assertEquals("eventIdentifierVaule " + " of Container [" + theContainerId + "] differs from defined format",
             XPathAPI.selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[version-number='3']"
                     + "/events/event[4]/eventIdentifier/eventIdentifierValue").getTextContent(), "/ir/container/"
                 + theContainerId
                 + "/resources/version-history#"
                 + XPathAPI.selectSingleNode(versionHistoryDoc,
                     "/version-history/version[version-number='3']" + "/events/event[4]/@xmlID").getTextContent());
 
         try {
             xml = addCtsElement(xml);
             xml = update(theContainerId, xml);
             assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '4']");
             assertXmlValidContainer(xml);
             fail("Succesful update after withdraw.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
 
     }
 
     @Test
     public void testLifecycleVersions2() throws Exception {
         String xml = theContainerXml;
         String versionHistory;
 
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '1']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'create']");
         assertXmlValidContainer(xml);
 
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '2']");
         versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[2]/events/event[1]/eventType[text() = 'create']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'update']");
         assertXmlValidContainer(xml);
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties status submitted", xml, "/container/properties/public-status[text() = 'submitted']");
         assertXmlExists("version status submitted", xml, "/container/properties/version/status[text() = 'submitted']");
         versionHistory = retrieveVersionHistory(theContainerId);
 
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[2]/events/event[1]/eventType[text() = 'create']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[1]/events/event[2]/eventType[text() = 'update']");
         assertXmlExists("Event 'submitted' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'submitted']");
         assertXmlValidContainer(xml);
 
         xml = addCtsElement(xml);
         xml = update(theContainerId, xml);
         assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '3']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[3]/events/event[1]/eventType[text() = 'create']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[2]/events/event[2]/eventType[text() = 'update']");
         assertXmlExists("Event 'submitted' ", versionHistory,
             "/version-history/version[2]/events/event[1]/eventType[text() = 'submitted']");
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'update']");
         assertXmlValidContainer(xml);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + this.theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
 
         xml = assignObjectPid(theContainerId, pidParam);
        String containerId = theContainerId + ":3";
 
         release(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(theContainerId);
         assertXmlExists("Properties public-status released", xml,
             "/container/properties/public-status[text() = 'released']");
         assertXmlExists("version status released", xml, "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Released container latest-release", xml, "/container/properties/latest-release");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
 
         // version entry 1 (version :3) ----------------------------------------
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[3]/events/event[1]/eventType[text() = 'create']");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
 
         // version entry 2 (version :2) ----------------------------------------
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[2]/events/event[2]/eventType[text() = 'update']");
         assertXmlExists("Event 'submitted' ", versionHistory,
             "/version-history/version[2]/events/event[1]/eventType[text() = 'submitted']");
 
         // version entry 3 (version :1) ----------------------------------------
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[1]/events/event[4]/eventType[text() = 'update']");
         assertXmlExists("Event 'assignObjectPid' ", versionHistory,
             "/version-history/version[1]/events/event[3]/eventType[text() = 'assignObjectPid']");
         assertXmlExists("Event 'assignVersionPid' ", versionHistory,
             "/version-history/version[1]/events/event[2]/eventType[text() = 'assignVersionPid']");
         assertXmlExists("Event 'released' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'released']");
         assertXmlValidContainer(xml);
 
         // update after release now allowed.
         // TODO create test if new version after update has status depending.
         // try {
         // xml = addCtsElement(xml);
         // xml = update(theContainerId, xml);
         // fail("No exception on update after release.");
         // assertXMLExist("New version number", xml,
         // "/container/properties/current-version/number[text() = '4']");
         // assertXMLValidContainer(xml);
         // }
         // catch (final Exception e) {
         // Class<?> ec = InvalidStatusException.class;
         // assertExceptionType(ec, e);
         // }
 
         withdraw(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         // try {
         xml = retrieve(theContainerId);
         // fail("No exception on retrieve after withdraw.");
         // }
         // catch (final Exception e) {
         // Class<?> ec = containerNotFoundException.class;
         // assertExceptionType(ec, e);
         // }
         assertXmlExists("Properties public-status withdrawn", xml,
             "/container/properties/public-status[text() = 'withdrawn']");
         assertXmlExists("version status must be released if withdrawn", xml,
             "/container/properties/version/status[text() = 'released']");
         assertXmlExists("Further released withdrawn container latest-release", xml,
             "/container/properties/latest-release");
         versionHistory = retrieveVersionHistory(theContainerId);
         assertXmlValidVersionHistory(versionHistory);
 
         // version entry 3 (version :1) ----------------------------------------
         assertXmlExists("Event 'create' ", versionHistory,
             "/version-history/version[3]/events/event[1]/eventType[text() = 'create']");
         versionHistory = retrieveVersionHistory(theContainerId);
 
         // version entry 2 (version :2) ----------------------------------------
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[2]/events/event[2]/eventType[text() = 'update']");
         assertXmlExists("Event 'submitted' ", versionHistory,
             "/version-history/version[2]/events/event[1]/eventType[text() = 'submitted']");
 
         // version entry 1 (version :3) ----------------------------------------
         // update -> assingObjectPid -> assignVersionPid -> release -> witdrawn
         assertXmlExists("Event 'update' ", versionHistory,
             "/version-history/version[1]/events/event[5]/eventType[text() = 'update']");
         assertXmlExists("Event 'assignObjectPid' ", versionHistory,
             "/version-history/version[1]/events/event[4]/eventType[text() = 'assignObjectPid']");
         assertXmlExists("Event 'assignVersionPid' ", versionHistory,
             "/version-history/version[1]/events/event[3]/eventType[text() = 'assignVersionPid']");
         assertXmlExists("Event 'released' ", versionHistory,
             "/version-history/version[1]/events/event[2]/eventType[text() = 'released']");
         assertXmlExists("Event 'withdrawn' ", versionHistory,
             "/version-history/version[1]/events/event[1]/eventType[text() = 'withdrawn']");
         assertXmlValidContainer(xml);
 
         try {
             xml = addCtsElement(xml);
             xml = update(theContainerId, xml);
             assertXmlExists("New version number", xml, "/container/properties/version/number[text() = '4']");
             assertXmlValidContainer(xml);
             fail("Succesful update after withdraw.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
 
     }
 
     @Override
     public String addCtsElement(final String xml) throws Exception {
         Document doc = EscidocAbstractTest.getDocument(xml);
         Node cts = selectSingleNode(doc, "/container/properties/content-model-specific");
         cts.appendChild(createElementNode(doc, null, null, "nox", "modified"));
         // doc = (Document) addAfter(doc,
         // "/container/properties/content-type-specific/nix", createElementNode(
         // doc, null, null, "nox", "modified"));
         String newXml = toString(doc, true);
         return newXml;
     }
 
     /**
      * Test if a released Container get the right version-status after adding further Items to it. The version-status
      * has to change from release to pending.
      * <p/>
      * Proceeding: 1.) create container 2.) release container 3.) add item to the container 4.) check if the container
      * has new version-status (pending) 5.) retrieve as anonymous user and count number of items (the new items are not
      * in the released version) 6.) release the latest version of the Container 7.) retrieve and count the number of
      * items (with user login) 8.) retrieve and count the number of items (as anonymous)
      * <p/>
      * See also Bug #5xx
      *
      * @throws Exception If the framework throws no Exception or the wrong Exception.
      */
     @Test
     public void testContainerStatusAfterAddingItems() throws Exception {
 
         String containerXml = theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
 
         // count items of Container --------------------------------------------
         NodeList itemList = selectNodeList(containerDoc, "/container/struct-map/item");
         int noOfItemsPending = itemList.getLength();
 
         // release Container ---------------------------------------------------
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
         releaseWithPid(theContainerId);
 
         containerXml = retrieve(theContainerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
 
         assertEquals("Version status not changed ", STATE_RELEASED, getVersionStatus(containerDoc));
 
         // get contextId of Container ------------------------------------------
         String containerContextId = getContextId(containerDoc);
         int contLtstVrsNo = getLatestVersionNumber(containerDoc);
 
         // count items of Container --------------------------------------------
         itemList = selectNodeList(containerDoc, "/container/struct-map/item");
         int noOfItemsRelease = itemList.getLength();
 
         assertEquals(noOfItemsPending, noOfItemsRelease);
 
         // prepare Item -------------------------------------------------------
         String tempItemXml = getItemTemplate("escidoc_item_198_for_create.xml");
         Document tempItemDoc = EscidocAbstractTest.getDocument(tempItemXml);
 
         tempItemDoc = setContextId(tempItemDoc, containerContextId);
         String itemXml = toString(tempItemDoc, false);
         createItem(theContainerId, itemXml);
 
         containerXml = retrieve(theContainerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
 
         // check if new version was created ------------------------------------
         int newltstVrsNo = getLatestVersionNumber(containerDoc);
 
         assertEquals("Increased Version number", contLtstVrsNo + 1, newltstVrsNo);
 
         // check status of latest version --------------------------------------
         assertEquals("Version status not changed ", STATE_PENDING, getVersionStatus(containerDoc));
 
         // count items of Container --------------------------------------------
         itemList = selectNodeList(containerDoc, "/container/struct-map/item");
         int noOfItemsCreate = itemList.getLength();
 
         assertEquals(noOfItemsRelease + 1, noOfItemsCreate);
 
         // retrieve as anonymous user and count items --------------------------
         PWCallback.setHandle(PWCallback.ANONYMOUS_HANDLE);
 
         containerXml = retrieve(theContainerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
 
         itemList = selectNodeList(containerDoc, "/container/struct-map/item");
         noOfItemsCreate = itemList.getLength();
 
         assertEquals(noOfItemsRelease, noOfItemsCreate);
 
     }
 
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testStatusLifecycleVersions() throws Exception {
         final String PUBLIC_STATUS_COMMENT = "public-status-comment";
         final String VERSION_COMMMENT = "version/comment";
         List<String> publicStatusComments = new Vector<String>();
         publicStatusComments.add(0, null);
         List<String> versionComments = new Vector<String>();
         versionComments.add(0, null);
 
         String propertiesBaseXPath = "/container/properties/";
 
         String xml = this.theContainerXml;
         Document xmlDoc = getDocument(xml);
         int currentVersion = 1;
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status pending", xml, propertiesBaseXPath + "public-status[text() = 'pending']");
         assertXmlExists("version status pending", xml, propertiesBaseXPath + "version/status[text() = 'pending']");
 
         publicStatusComments.add(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         assertXmlValidContainer(xml);
 
         Document versionHistoryDoc;
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertEquals(publicStatusComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '2']");
         currentVersion = 2;
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status pending", xml, propertiesBaseXPath + "public-status[text() = 'pending']");
         assertXmlExists("version status pending", xml, propertiesBaseXPath + "version/status[text() = 'pending']");
 
         publicStatusComments.add(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
 
         assertEquals(publicStatusComments.get(currentVersion), publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals("Update, " + PUBLIC_STATUS_COMMENT + " must not be equal to " + VERSION_COMMMENT,
             publicStatusComments.get(currentVersion), selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                     + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         submit(this.theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '2']");
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status submitted", xml, propertiesBaseXPath + "public-status[text() = 'submitted']");
         assertXmlExists("version status submitted", xml, propertiesBaseXPath + "version/status[text() = 'submitted']");
 
         // no new version but
         // version comment changed and
         // public status comment changed
         assertNotEquals(VERSION_COMMMENT + " must be changed", versionComments.get(currentVersion), selectSingleNode(
             xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         versionComments.set(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed", publicStatusComments.get(currentVersion),
             selectSingleNode(xmlDoc, propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         publicStatusComments.set(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
 
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed", publicStatusComments.get(currentVersion),
             publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertEquals(publicStatusComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '3']");
         currentVersion = 3;
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status submitted", xml, propertiesBaseXPath + "public-status[text() = 'submitted']");
         assertXmlExists("version status submitted", xml, propertiesBaseXPath + "version/status[text() = 'submitted']");
 
         publicStatusComments.add(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
 
         assertEquals(publicStatusComments.get(currentVersion), publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals("Update, " + PUBLIC_STATUS_COMMENT + " must not be equal to " + VERSION_COMMMENT,
             publicStatusComments.get(currentVersion), selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                     + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + this.theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
 
         assignObjectPid(this.theContainerId, pidParam);
 
         String versionId = this.theContainerId + ":3";
 
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         xml = assignVersionPid(versionId, pidParam);
 
         release(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '3']");
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status released", xml, propertiesBaseXPath + "public-status[text() = 'released']");
         assertXmlExists("version status released", xml, propertiesBaseXPath + "version/status[text() = 'released']");
 
         // no new version but
         // version comment changed and
         // public status comment changed
         assertNotEquals(VERSION_COMMMENT + " must be changed", versionComments.get(currentVersion), selectSingleNode(
             xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed", publicStatusComments.get(currentVersion),
             selectSingleNode(xmlDoc, propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         publicStatusComments.set(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
 
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed", publicStatusComments.get(currentVersion),
             publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertEquals(publicStatusComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '4']");
         currentVersion = 4;
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status released", xml, propertiesBaseXPath + "public-status[text() = 'released']");
         assertXmlExists("version status pending", xml, propertiesBaseXPath + "version/status[text() = 'pending']");
 
         publicStatusComments.add(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
 
         assertEquals(publicStatusComments.get(currentVersion), publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals("Update, " + PUBLIC_STATUS_COMMENT + " must not be equal to " + VERSION_COMMMENT,
             publicStatusComments.get(currentVersion), selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                     + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         // submit again
         submit(this.theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '4']");
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status released", xml, propertiesBaseXPath + "public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, propertiesBaseXPath + "version/status[text() = 'submitted']");
 
         // no new version but
         // version comment changed and
         // public status comment NOT changed
         assertNotEquals(VERSION_COMMMENT + " must be changed", versionComments.get(currentVersion), selectSingleNode(
             xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         versionComments.set(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         assertEquals(PUBLIC_STATUS_COMMENT + " must NOT be changed", publicStatusComments.get(currentVersion),
             selectSingleNode(xmlDoc, propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must not be changed", publicStatusComments.get(currentVersion),
             selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                     + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '5']");
         currentVersion = 5;
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status released", xml, propertiesBaseXPath + "public-status[text() = 'released']");
         assertXmlExists("version status submitted", xml, propertiesBaseXPath + "version/status[text() = 'submitted']");
 
         publicStatusComments.add(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         versionComments.add(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
 
         assertEquals(publicStatusComments.get(currentVersion), publicStatusComments.get(currentVersion - 1));
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals("Update, " + PUBLIC_STATUS_COMMENT + " must not be equal to " + VERSION_COMMMENT,
             publicStatusComments.get(currentVersion), selectSingleNode(
                 versionHistoryDoc,
                 "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                     + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         // release again
         versionId = this.theContainerId + ":5";
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         xml = assignVersionPid(versionId, pidParam);
 
         release(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlExists("New version number", xml, propertiesBaseXPath + "version/number[text() = '5']");
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status released", xml, propertiesBaseXPath + "public-status[text() = 'released']");
         assertXmlExists("version status released", xml, propertiesBaseXPath + "version/status[text() = 'released']");
 
         // no new version but
         // version comment changed and
         // public status comment changed
         assertNotEquals(VERSION_COMMMENT + " must be changed", versionComments.get(currentVersion), selectSingleNode(
             xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         versionComments.set(currentVersion,
             selectSingleNode(xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         // TODO same generated comment as with last release
         // assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed",
         // publicStatusComments.get(currentVersion), selectSingleNode(xmlDoc,
         // "item/properties/" + PUBLIC_STATUS_COMMENT + "/text()")
         // .getNodeValue());
         publicStatusComments.set(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertEquals(publicStatusComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
         withdraw(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         assertXmlExists(PUBLIC_STATUS_COMMENT, xml, propertiesBaseXPath + PUBLIC_STATUS_COMMENT);
         assertXmlExists(VERSION_COMMMENT, xml, propertiesBaseXPath + VERSION_COMMMENT);
         assertXmlExists("Properties status withdrawn", xml, propertiesBaseXPath + "public-status[text() = 'withdrawn']");
         assertXmlExists("version status must still be released", xml, propertiesBaseXPath
             + "version/status[text() = 'released']");
 
         // no new version but
         // version comment NOT changed and
         // public status comment changed
         assertEquals(VERSION_COMMMENT + " must NOT be changed", versionComments.get(currentVersion), selectSingleNode(
             xmlDoc, propertiesBaseXPath + VERSION_COMMMENT + "/text()").getNodeValue());
         assertNotEquals(PUBLIC_STATUS_COMMENT + " must be changed", publicStatusComments.get(currentVersion),
             selectSingleNode(xmlDoc, propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
         publicStatusComments.set(currentVersion, selectSingleNode(xmlDoc,
             propertiesBaseXPath + PUBLIC_STATUS_COMMENT + "/text()").getNodeValue());
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
         assertEquals(versionComments.get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
         assertNotEquals(PUBLIC_STATUS_COMMENT + " should not be in version history.", publicStatusComments
             .get(currentVersion), selectSingleNode(
             versionHistoryDoc,
             "/version-history/version[@href = '" + Constants.CONTAINER_BASE_URI + "/" + this.theContainerId + ":"
                 + currentVersion + "']/comment/text()").getNodeValue());
 
         // ---------------------------------------------------------------
 
     }
 
     /**
      * Test timestamps of Container in consistency with Version History.
      *
      * @throws Exception If behavior is not as expected.
      */
     @Test
     public void testTimestamps01() throws Exception {
 
         String xml = this.theContainerXml;
         Document xmlDoc = getDocument(xml);
         int currentVersion = 1;
 
         Document versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // check timestamps within Container XML-------------------------------
         // /container/@last-modification-date ==
         // /container/properties/version/date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from //version/date", XPathAPI
             .selectSingleNode(xmlDoc, "/container/@last-modification-date").getTextContent(), XPathAPI
             .selectSingleNode(xmlDoc, "/container/properties/version/date").getTextContent());
 
         // /container/@last-modification-date ==
         // /container/properties/latest-version/date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from //latest-version/date", XPathAPI.selectSingleNode(xmlDoc,
             "/container/@last-modification-date").getTextContent(), XPathAPI.selectSingleNode(xmlDoc,
             "/container/properties/latest-version/date").getTextContent());
 
         // check timestamps within Version History XML -------------------
         // /version-history/version[version-number='1']/events/event[1]/eventDateTime
         // ==
         // /version-history/version[version-number='1']/@timestamp
         assertEquals("eventDateTime of the latest event of version 1 differs "
             + "from timestamp attribute of version 1 [" + this.theContainerId + "]", XPathAPI
             .selectSingleNode(versionHistoryDoc,
                 "/version-history/version[version-number='1']" + "/events/event[last()]/eventDateTime")
             .getTextContent(), XPathAPI.selectSingleNode(versionHistoryDoc,
             "/version-history/version[version-number='1']/@timestamp").getTextContent());
 
         // check timestamps between Item XML and Version History XML -----
         // /container/@last-modification-date ==
         // /version-history/@last-modification-date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from last-modification-date " + "attribute of version-history", XPathAPI.selectSingleNode(
             xmlDoc, "/container/@last-modification-date").getTextContent(), XPathAPI.selectSingleNode(
             versionHistoryDoc, "/version-history/@last-modification-date").getTextContent());
 
         // /version-history/version[version-number='1']/@timestamp ==
         // /container/properties/creation-date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from creation date of version", XPathAPI.selectSingleNode(versionHistoryDoc,
             "/version-history/version[version-number='1']/@timestamp").getTextContent(), XPathAPI.selectSingleNode(
             xmlDoc, "/container/properties/creation-date").getTextContent());
 
         // /version-history/version[version-number='1']/timestamp ==
         // /container/@last-modification-date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from timestamp of version 1 " + "in version-history", XPathAPI.selectSingleNode(
             versionHistoryDoc, "/version-history/version[version-number='1']/timestamp").getTextContent(), XPathAPI
             .selectSingleNode(xmlDoc, "/container/@last-modification-date").getTextContent());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
 
         xmlDoc = getDocument(xml);
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // ---------------------------------------------------------------
 
         submit(this.theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
 
         // ---------------------------------------------------------------
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         currentVersion = 3;
 
         // ---------------------------------------------------------------
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // ---------------------------------------------------------------
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + this.theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
 
         assignObjectPid(this.theContainerId, pidParam);
 
         String versionId = this.theContainerId + ":3";
 
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         xml = assignVersionPid(versionId, pidParam);
 
         release(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
 
         // ---------------------------------------------------------------
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
         // check comment in version-history
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         currentVersion = 4;
 
         // ---------------------------------------------------------------
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // ---------------------------------------------------------------
 
         // submit again
         submit(this.theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // /version-history/version[version-number='4']/events/event[1]/eventType
         // == submitted
         assertEquals("eventType for release is not as expected", "submitted", XPathAPI
             .selectSingleNode(versionHistoryDoc,
                 "/version-history/version[version-number='4']" + "/events/event[1]/eventType").getTextContent());
 
         // ---------------------------------------------------------------
 
         xml = addCtsElement(xml);
         xml = update(this.theContainerId, xml);
         xmlDoc = getDocument(xml);
         currentVersion = 5;
 
         // ---------------------------------------------------------------
 
         // ---------------------------------------------------------------
 
         // release again
         versionId = this.theContainerId + ":5";
 
         assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         xml = assignVersionPid(versionId, pidParam);
 
         release(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(xml)), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // /container/@last-modification-date ==
         // /container/properties/latest-release/date
         assertEquals("last-modification-date in root attribute of Container [" + this.theContainerId
             + "] differs from //latest-version/date", XPathAPI.selectSingleNode(xmlDoc,
             "/container/@last-modification-date").getTextContent(), XPathAPI.selectSingleNode(xmlDoc,
             "/container/properties/latest-release/date").getTextContent());
 
         // /version-history/version[version-number='5']/events/event[1]/eventType
         // == release
         assertEquals("eventType for release is not as expected", "released", XPathAPI
             .selectSingleNode(versionHistoryDoc,
                 "/version-history/version[version-number='5']" + "/events/event[1]/eventType").getTextContent());
 
         // /version-history/version[version-number='5']/events/event[1]/eventDateTime
         // ==
         // /container/@last-modification-date
         assertEquals("eventDateTime of the latest event of version 1 differs "
             + "from last-modification-date root attribute of Container [" + this.theContainerId + "]", XPathAPI
             .selectSingleNode(versionHistoryDoc,
                 "/version-history/version[version-number='5']" + "/events/event[1]/eventDateTime").getTextContent(),
             XPathAPI.selectSingleNode(xmlDoc, "/container/@last-modification-date").getTextContent());
 
         // ---------------------------------------------------------------
 
         withdraw(this.theContainerId, getStatusTaskParam(getLastModificationDateValue2(xmlDoc), null));
         xml = retrieve(this.theContainerId);
         xmlDoc = getDocument(xml);
         assertXmlValidContainer(xml);
 
         // ---------------------------------------------------------------
 
         versionHistoryDoc = getDocument(retrieveVersionHistory(this.theContainerId));
 
         // ---------------------------------------------------------------
 
     }
 
 }
