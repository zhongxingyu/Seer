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
 
 import java.net.URL;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import org.joda.time.DateTime;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import static org.junit.Assert.fail;
 
 /**
  * Test the mock implementation of the Container resource.
  *
  * @author Michael Schneider
  */
 public class ContainerWithdrawIT extends ContainerTestBase {
 
     private static final String WITHDRAW_COMMENT = "This is a withdraw comment.";
 
     private String theContainerXml;
 
     private String theContainerId;
 
     private String theItemId;
 
     private String theSubcontainerId;
 
     /**
      * Test declining withdraw of container with non existing container id.
      */
     @Test
     public void testOM_WAC_2_1() throws Exception {
 
         try {
             withdraw("bla", getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
                 null));
             fail("No exception occurred on withdraw with non" + "existing container id.");
         }
         catch (final Exception e) {
             Class<?> ec = ContainerNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test declining withdraw of container with wrong time stamp.
      */
     @Test
     public void test_OM_WAC_2_2() throws Exception {
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
 
         String pidParam;
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + this.theContainerId));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                     assignPidParam);
 
             assignObjectPid(this.theContainerId, pidParam);
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
             String latestVersion = getLatestVersionObjidValue(theContainerXml);
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(getFrameworkUrl() + "/ir/container/" + latestVersion));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(latestVersion))),
                     assignPidParam);
 
             assignVersionPid(latestVersion, pidParam);
         }
 
         release(theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
 
         //        String param = getTheLastModificationParam(true, theContainerId);
         //        param =
         //            param.replaceFirst("<param last-modification-date=\"([0-9TZ:\\.-])+\"",
         //                "<param last-modification-date=\"2005-01-30T11:36:42.015Z\"");
 
         try {
             withdraw(theContainerId, getStatusTaskParam(new DateTime(), null));
             fail("No exception occurred on withdraw with " + "wrong time stamp.");
         }
         catch (final Exception e) {
             Class<?> ec = OptimisticLockingException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test declining withdraw of container with missing container id.
      */
     @Test
     public void testOM_WAC_3_1() throws Exception {
 
         try {
             withdraw(null, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
                 null));
             fail("No exception occurred on withdraw with missing " + "container id.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test declining withdraw of container with missing time stamp.
      */
     @Test
     public void testOM_WAC_3_2() throws Exception {
 
         try {
             withdraw(theContainerId, null);
             fail("No exception occurred on withdraw with missing" + "time stamp.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test declining withdraw of container before release.
      */
     @Test
     public void testOMWAC3_3() throws Exception {
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
 
         try {
             withdraw(theContainerId, getStatusTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
             fail("No exception occurred on withdraw bevore submit.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void setUp() throws Exception {
         this.theItemId = createItemFromTemplate("escidoc_item_198_for_create.xml");
 
         String xmlData = getContainerTemplate("create_container_WithoutMembers_v1.1.xml");
         theContainerXml = create(xmlData);
 
         this.theSubcontainerId = getObjidValue(theContainerXml);
 
         String xmlData1 = getContainerTemplate("create_container_v1.1-forItemAndforContainer.xml");
         String xmlWithItem = xmlData1.replaceAll("##ITEMID##", theItemId);
         String xmlWithItemAndContainer = xmlWithItem.replaceAll("##CONTAINERID##", theSubcontainerId);
         theContainerXml = create(xmlWithItemAndContainer);
         // String xmlData1 = getTemplateAsString(TEMPLATE_CONTAINER_PATH,
         // "create_container_v1.1-forContainer.xml");
         //        
         // String xmlWithContainer =
         // xmlData1.replaceAll("##CONTAINERID##}", theSubcontainerId);
         // theContainerXml = create(xmlWithContainer);
 
         this.theContainerId = getObjidValue(theContainerXml);
 
     }
 
     @Test
     public void testOM_WAC_1() throws Exception {
         submitItemHelp();
         submit(theSubcontainerId, getStatusTaskParam(
            getLastModificationDateValue2(getDocument(retrieve(theSubcontainerId))), null));
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
 
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
 
         String responseXML =
             release(theContainerId, getStatusTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
 
         String param = getStatusTaskParam(getLastModificationDateValue2(getDocument(responseXML)), WITHDRAW_COMMENT);
         withdraw(theContainerId, param);
         try {
             String withdrawnContainer = retrieve(theContainerId);
             assertXmlEquals("Item is not in state withdrawn!", withdrawnContainer,
                 "/container/properties/public-status", STATUS_WITHDRAWN);
         }
         catch (final Exception e) {
             fail("Unexpected exception occurred on retrieve after withdraw.");
         }
 
         // FIXME We have no content-types and can not decide if members should
         // be withdrawn too.
 
         // try {
         // String withdrawnContainer = retrieve(theSubcontainerId);
         // assertXMLEquals("Item is not in state withdrawn!",
         // withdrawnContainer, "/container/properties/status",
         // STATUS_WITHDRAWN);
         // }
         // catch (final Exception e) {
         // fail("Unexpected exception occurred on retrieve after withdraw.");
         // }
         //
         // try {
         // String xmlResult = handleXmlResult(getItemClient().retrieve(
         // theItemId));
         // assertXMLEquals("Item is not in state withdrawn!", xmlResult,
         // "/item/properties/status", STATUS_WITHDRAWN);
         // }
         // catch (final Exception e) {
         // fail("Unexpected exception occurred! " + e);
         // }
 
         // ---------------------------------------------------------------
         // test update withdrawn Item
         // see also (issue INFR-710)
         try {
             String newItemXml = addCtsElement(theContainerXml);
             update(theContainerId, newItemXml);
             fail("Update after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
 
         try {
             submit(theContainerId, getStatusTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
             fail("Submit after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
 
         try {
             revise(theContainerId, getTheLastModificationParam(false, theContainerId));
             fail("Revise after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
 
         try {
             release(theContainerId, getStatusTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
             fail("Release after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
 
         try {
             delete(theContainerId);
             fail("Delete after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
 
         try {
            lock(theContainerId, getLockTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId)))));
             fail("Lock after withdrawn is possible.");
         }
         catch (final InvalidStatusException e) {
             // that's ok
         }
     }
 
     /**
      * 
      * @throws Exception
      */
     private void submitItemHelp() throws Exception {
         DateTime lmd =
             getLastModificationDateValue2(getDocument(handleXmlResult(getItemClient().retrieve(this.theItemId))));
         String param = getStatusTaskParam(lmd, null);
 
         Object result1 = getItemClient().submit(theItemId, param);
         if (result1 instanceof HttpResponse) {
             HttpResponse httpRes = (HttpResponse) result1;
             assertHttpStatusOfMethod("", httpRes);
         }
     }
 
     /**
      * Test declining second withdraw of container.
      */
     @Test
     public void testOM_WAC_4() throws Exception {
 
         submitItemHelp();
         submit(theSubcontainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(theSubcontainerId))), null));
 
         submit(theContainerId, getStatusTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
             null));
 
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
 
         withdraw(theContainerId, getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
         try {
             withdraw(theContainerId, getStatusTaskParam(
                 getLastModificationDateValue2(getDocument(retrieve(theContainerId))), null));
             fail("No exception occurred on second withdraw.");
         }
         catch (final Exception e) {
             Class<?> ec = AlreadyWithdrawnException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 }
