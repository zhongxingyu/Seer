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
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.remote.application.violated.ReadonlyVersionException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 /**
  * Test the implementation of containers PID assignment methods.
  *
  * @author Michael Schneider, Steffen Wagner
  */
 public class ContainerPidAssignmentIT extends ContainerTestBase {
 
     private String theContainerId;
 
     private String theContainerXml;
 
     private String theItemId;
 
     private final String containerUrl;
 
     public ContainerPidAssignmentIT() {
         containerUrl = getFrameworkUrl() + "/ir/container/";
     }
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void setUp() throws Exception {
 
         this.theItemId = createItem();
 
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
 
         this.theContainerXml = create(xmlData.replaceAll("##ITEMID##", this.theItemId));
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
             getItemClient().delete(this.theItemId);
             delete(this.theContainerId);
         }
         catch (final Exception e) {
             // do nothing
         }
     }
 
     /**
      * Test if the assignment of a objectPid.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testAssignObjectPID() throws Exception {
 
         String xml = null;
         String pidParam = null;
         String lmd = null;
         String objectPidXml = null;
         String versionPidXml = null;
         AssignParam assignPidParam = new AssignParam();
 
         String resultXml = submit(theContainerId, getTheLastModificationParam(false));
         assertXmlValidResult(resultXml);
         lmd = getLastModificationDateValue(getDocument(resultXml));
 
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
 
             assignPidParam.setUrl(new URL("http://somewhere/" + this.theContainerId + "/" + System.nanoTime()));
             pidParam = getAssignPidTaskParam(new DateTime(lmd, DateTimeZone.UTC), assignPidParam);
 
             objectPidXml = assignObjectPid(this.theContainerId, pidParam);
             assertXmlValidResult(objectPidXml);
             lmd = getLastModificationDateValue(getDocument(objectPidXml));
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
             assignPidParam.setUrl(new URL("http://somewhere/" + this.theContainerId + "/" + System.nanoTime()));
             pidParam = getAssignPidTaskParam(new DateTime(lmd, DateTimeZone.UTC), assignPidParam);
 
             versionPidXml = assignVersionPid(this.theContainerId, pidParam);
             assertXmlValidResult(versionPidXml);
             lmd = getLastModificationDateValue(getDocument(versionPidXml));
         }
 
         // check if returned pid equals Container properties entry
         xml = retrieve(this.theContainerId);
         assertXmlValidContainer(xml);
 
         // object Pid
         Node objectPid = selectSingleNode(EscidocAbstractTest.getDocument(xml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(objectPid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(objectPidXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), objectPid.getTextContent());
 
         // version Pid
         Node versionPid = selectSingleNode(EscidocAbstractTest.getDocument(xml), XPATH_CONTAINER_VERSION_PID);
         assertNotNull(versionPid);
         returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(versionPidXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), versionPid.getTextContent());
 
         // latest release Pid
         // TODO
     }
 
     /**
      * Test re-assign object pid to released container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testReAssignObjectPID() throws Exception {
 
         String xml = null;
         String objectPidXml = null;
         String lmd = null;
 
         String resultXml = submit(theContainerId, getTheLastModificationParam(false));
         assertXmlValidResult(resultXml);
         lmd = getLastModificationDateValue(getDocument(resultXml));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + this.theContainerId + "/" + System.nanoTime()));
         String pidParam = getAssignPidTaskParam(new DateTime(lmd, DateTimeZone.UTC), assignPidParam);
 
         objectPidXml = assignObjectPid(theContainerId, pidParam);
         assertXmlValidResult(resultXml);
         lmd = getLastModificationDateValue(getDocument(objectPidXml));
 
         xml = retrieve(theContainerId);
         Node objectPid = selectSingleNode(EscidocAbstractTest.getDocument(xml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(objectPid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(objectPidXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), objectPid.getTextContent());
         assertXmlValidContainer(theContainerXml);
 
         // re-assign objectPid to a Container ----------------------------
         assignPidParam.setUrl(new URL(this.containerUrl + this.theContainerId + "/" + System.nanoTime()));
         pidParam = getAssignPidTaskParam(new DateTime(lmd, DateTimeZone.UTC), assignPidParam);
 
         try {
             assignObjectPid(theContainerId, pidParam);
             fail("InvalidStatusException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test re-assign object pid to non submitted or released container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testReAssignObjectPID2() throws Exception {
         String xml = null;
         String pid = null;
         String pidParam = null;
 
         xml = retrieve(theContainerId);
         assertXmlValidContainer(xml);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + this.theContainerId + "/" + System.nanoTime()));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(xml)), assignPidParam);
 
         pid = assignObjectPid(theContainerId, pidParam);
 
         // check if returned pid equals RELS-EXT entry
         xml = retrieve(theContainerId);
         Node latestReleasePid = selectSingleNode(EscidocAbstractTest.getDocument(xml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(latestReleasePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), latestReleasePid.getTextContent());
         assertXmlValidContainer(theContainerXml);
 
         // re-assing PID to a released Container ------------------------------
         try {
             assignPidParam.setUrl(new URL("http://escidoc.de/container/resource"));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(EscidocAbstractTest
                     .getDocument(retrieve(theContainerId))), assignPidParam);
 
             pid = assignObjectPid(theContainerId, pidParam);
             fail("InvalidStatusException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Tests if the assignment to a defined version of item is possible. All other versions has to be unchanged after
      * the procedure.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testAssignVersionPID() throws Exception {
         final int maxVersionNumber = 6;
         String pid = null;
         String pidParam = null;
         String newContainerXml = null;
 
         assertXmlExists("New version number does not exist", theContainerXml,
             "/container/properties/version/number[text() = '" + 1 + "']");
         newContainerXml = addCtsElement(theContainerXml);
         theContainerXml = update(theContainerId, newContainerXml);
         assertXmlExists("New version number", theContainerXml, "/container/properties/version/number[text() = '2']");
         assertXmlValidContainer(theContainerXml);
 
         String versionId = theContainerId + ":2";
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         pid = assignVersionPid(theContainerId + VERSION_SUFFIX_SEPARATOR + "2", pidParam);
         assertNotNull(pid);
         theContainerXml = retrieve(theContainerId);
         assertXmlValidContainer(theContainerXml);
 
         // check if XML contains the versionPid
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         Node currentVersionPid =
             selectSingleNode(EscidocAbstractTest.getDocument(theContainerXml), XPATH_CONTAINER_VERSION_PID);
         assertNotNull(currentVersionPid);
         assertEquals(returnedPid.getTextContent(), currentVersionPid.getTextContent());
 
         // create more versions
         for (int i = 3; i < maxVersionNumber; i++) {
             newContainerXml = addCtsElement(theContainerXml);
             theContainerXml = update(theContainerId, newContainerXml);
             assertXmlExists("New version number does not exist", theContainerXml,
                 "/container/properties/version/number[text() = '" + i + "']");
         }
         assertXmlValidContainer(theContainerXml);
 
         // assign PID to version versionNumber ---------------------------------
         assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
         try {
             pid = assignVersionPid(versionId, pidParam);
 
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyVersionException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
         // The version pid could not be assigned, therefore the following
         // checks are useless
 
         // // check if returned pid equals container version-history
         // --------------
         // newContainerXml = retrieve(versionId);
         // returnedPid =
         // selectSingleNode(EscidocAbstractTest.getDocument(pid),
         // XPATH_RESULT_PID);
         // currentVersionPid =
         // selectSingleNode(EscidocAbstractTest
         // .getDocument(newContainerXml), XPATH_CONTAINER_VERSION_PID);
         // assertNotNull(currentVersionPid);
         // assertEquals(returnedPid.getTextContent(), currentVersionPid
         // .getTextContent());
         //
         // // check if no other version was altered
         // -------------------------------
         // for (int i = 1; i < maxVersionNumber; i++) {
         // if ((i != 2) && (i != versionNumberPid)) {
         // newContainerXml =
         // retrieve(theContainerId + VERSION_SUFFIX_SEPARATOR + i);
         // returnedPid =
         // selectSingleNode(EscidocAbstractTest
         // .getDocument(newContainerXml),
         // XPATH_CONTAINER_VERSION_PID);
         // assertNull(returnedPid);
         // assertXmlValidContainer(newContainerXml);
         // }
         // }
     }
 
     /**
      * Test if the assignement of a version Pid to a latest-release version of the Container is handled right.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAssignVersionPid02() throws Exception {
 
         final int versionNumberPid = 3;
         final int maxVersionNumber = 5;
         String pidParam = null;
         String newContainerXml = null;
 
         // create versions
         for (int i = 1; i < maxVersionNumber; i++) {
             newContainerXml = addCtsElement(theContainerXml);
             theContainerXml = update(theContainerId, newContainerXml);
             assertXmlExists("New version number does not exist", theContainerXml,
                 "/container/properties/version/number[text() = '" + (i + 1) + "']");
             assertXmlValidContainer(theContainerXml);
         }
 
         // assign PID to version versionNumber ---------------------------------
         String versionId = theContainerId + VERSION_SUFFIX_SEPARATOR + versionNumberPid;
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
         try {
             assignVersionPid(versionId, pidParam);
             fail("ReadonlyVersionException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyVersionException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Test if the re-assignement of a version PId to a Container is handled with the right exception.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testReAssignVersionPID() throws Exception {
         final int versionNumberPid = 3;
         final int maxVersionNumber = 5;
         String pidParam = null;
         String newContainerXml = null;
 
         // create versions
         for (int i = 1; i < maxVersionNumber; i++) {
             newContainerXml = addCtsElement(theContainerXml);
             theContainerXml = update(theContainerId, newContainerXml);
             assertXmlExists("New version number does not exist", theContainerXml,
                 "/container/properties/version/number[text() = '" + (i + 1) + "']");
             assertXmlValidContainer(theContainerXml);
         }
 
         // assign PID to version versionNumber ---------------------------------
         String versionId = theContainerId + VERSION_SUFFIX_SEPARATOR + versionNumberPid;
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         try {
             assignVersionPid(versionId, pidParam);
             fail("ReadonlyVersionException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyVersionException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         // The version pid could not be assigned, therefore the following
         // checks are useless
 
         // // check if returned pid equals container version-history
         // --------------
         // newContainerXml = retrieve(versionId);
         // Node returnedPid =
         // selectSingleNode(EscidocAbstractTest.getDocument(pid),
         // XPATH_RESULT_PID);
         // Node currentVersionPid =
         // selectSingleNode(EscidocAbstractTest
         // .getDocument(newContainerXml), XPATH_CONTAINER_VERSION_PID);
         // assertNotNull("VersionPid missing", currentVersionPid);
         // assertEquals(returnedPid.getTextContent(), currentVersionPid
         // .getTextContent());
         //
         // // re-assign PID to version versionNumber
         // // ---------------------------------
         // try {
         //        AssignParam assignPidParam = new AssignParam();
         //        assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         //        pidParam =
         //            getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
         // pid = assignVersionPid(versionId, pidParam);
         // fail("InvalidStatusException expected.");
         // }
         // catch (final Exception e) {
         // Class<?> ec = InvalidStatusException.class;
         // EscidocAbstractTest.assertExceptionType(ec.getName()
         // + " expected.", ec, e);
         // }
     }
 
     /**
      * Check reaction of wrong param.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testParam01() throws Exception {
         try {
             assignObjectPid(theItemId, null);
             fail("MissingMethodParameterException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test if an empty value of the PID element within the taskParam XML is handled correct.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter05() throws Exception {
 
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
         String lmd = getLastModificationDateValue(containerDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid("");
         String taskParam = getAssignPidTaskParam(new DateTime(lmd, DateTimeZone.UTC), assignPidParam);
 
         Class<?> ec = XmlCorruptedException.class;
 
         try {
             assignVersionPid(containerId, taskParam);
             fail("Expect exception if pid element in taskParam is empty.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test if an empty value of the PID element within the taskParam XML is handled correct.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter06() throws Exception {
 
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
         DateTime lmd = getLastModificationDateValue2(containerDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid("");
         String taskParam = getAssignPidTaskParam(lmd, assignPidParam);
 
         Class<?> ec = XmlCorruptedException.class;
 
         try {
             assignObjectPid(containerId, taskParam);
             fail("Expect exception if pid element in taskParam is empty.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Check pid assignment with lower user permissions. Assign before container has status "submitted" or "released".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testObjectPidAssignmentPermissionA() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
 
         theContainerXml = create(xmlData.replaceAll("##ITEMID##", this.theItemId));
         this.theContainerId = getObjidValue(this.theContainerXml);
 
         Document theContainer = EscidocAbstractTest.getDocument(theContainerXml);
         String pid = null;
 
         Node node = selectSingleNode(theContainer, XPATH_CONTAINER_OBJECT_PID);
         assertNull(node);
         assertXmlValidContainer(theContainerXml);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), assignPidParam);
 
         pid = assignObjectPid(theContainerId, pidParam);
 
         // check if returned pid equals RELS-EXT entry
         theContainerXml = retrieve(theContainerId);
         Node thePid = selectSingleNode(EscidocAbstractTest.getDocument(theContainerXml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(thePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), thePid.getTextContent());
         assertXmlValidContainer(theContainerXml);
     }
 
     /**
      * Check pid assignment with lower user permissions. Assign in container status "submitted".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testObjectPidAssignmentPermissionB() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
         theContainerXml = create(xmlData.replaceAll("##ITEMID##", this.theItemId));
         this.theContainerId = getObjidValue(this.theContainerXml);
 
         Document theContainer = EscidocAbstractTest.getDocument(theContainerXml);
         String pid = null;
 
         Node node = selectSingleNode(theContainer, XPATH_CONTAINER_OBJECT_PID);
         assertNull(node);
         assertXmlValidContainer(theContainerXml);
 
         // release Container and assign PID
         // ----------------------------------------
         submit(theContainerId, getTheLastModificationParam(false));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))), assignPidParam);
 
         pid = assignObjectPid(theContainerId, pidParam);
 
         // check if returned pid equals RELS-EXT entry
         theContainerXml = retrieve(theContainerId);
         Node thePid = selectSingleNode(EscidocAbstractTest.getDocument(theContainerXml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(thePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), thePid.getTextContent());
         assertXmlValidContainer(theContainerXml);
     }
 
     /**
      * Check pid assignment with lower user permissions. Assign before container has status "submitted" and release
      * later. Check if PID values exists after status change in properties.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testObjectPidAssignmentPermissionD() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
 
         String xmlData = getContainerTemplate("create_container_v1.1-forItem.xml");
 
         theContainerXml = create(xmlData.replaceAll("##ITEMID##", this.theItemId));
         this.theContainerId = getObjidValue(this.theContainerXml);
 
         Document theContainer = EscidocAbstractTest.getDocument(theContainerXml);
         String pid = null;
 
         Node node = selectSingleNode(theContainer, XPATH_CONTAINER_OBJECT_PID);
         assertNull(node);
         assertXmlValidContainer(theContainerXml);
 
         // release Item and assign PID ----------------------------------------
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), assignPidParam);
 
         pid = assignObjectPid(theContainerId, pidParam);
 
         submit(theContainerId, getTheLastModificationParam(false));
 
         // assign version PID
         assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(this.theContainerId))),
                 assignPidParam);
         assertXmlValidResult(assignVersionPid(theContainerId, pidParam));
 
         release(theContainerId, getTheLastModificationParam(false));
 
         // check if returned pid equals RELS-EXT entry
         theContainerXml = retrieve(theContainerId);
         Node thePid = selectSingleNode(EscidocAbstractTest.getDocument(theContainerXml), XPATH_CONTAINER_OBJECT_PID);
         assertNotNull(thePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), thePid.getTextContent());
         assertXmlValidContainer(theContainerXml);
     }
 
     /**
      * Test assignVersionPid() with Container id without version suffix.
      * <p/>
      * Since build 276 is the interface behavior consistent to the other method calls. Before build 276 must the
      * assign-version-pid method be called with an identifier including the version suffix. With build 276 was this
      * removed. The method could be called with version identifier but not has to. If the identifer has no version
      * suffix than is the latest/newest version assigned with a version pid.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testVersionSuffix() throws Exception {
 
         String pidXml = null;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))), assignPidParam);
 
         try {
             pidXml = assignVersionPid(theContainerId, pidParam);
         }
         catch (final MissingMethodParameterException e) {
             fail("AssignVersionPid() does check for a version number.");
         }
 
         // check if the newest version of container is assigned with the version
         // pid
         String containerXml = retrieve(theContainerId);
 
         Node currentVersionPid =
             selectSingleNode(EscidocAbstractTest.getDocument(containerXml), "/container/properties/version/pid");
 
         Node pid = selectSingleNode(EscidocAbstractTest.getDocument(pidXml), XPATH_RESULT_PID);
         assertEquals(currentVersionPid.getTextContent(), pid.getTextContent());
     }
 
     /**
      * Test the assignObjectPid() method with withdrawn Container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testObjectPidInStatusWithdrawn() throws Exception {
 
         submit(theContainerId, getTheLastModificationParam(false));
         theContainerXml = retrieve(theContainerId);
         assertXmlValidContainer(theContainerXml);
 
         // assign
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(theContainerXml)), assignPidParam);
 
         assignObjectPid(theContainerId, pidParam);
         String versionId = getLatestVersionId(theContainerXml);
 
         assignPidParam.setUrl(new URL(this.containerUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         assignVersionPid(versionId, pidParam);
 
         release(theContainerId, getTheLastModificationParam(false));
 
         String param = getTheLastModificationParam(true, theContainerId);
         withdraw(theContainerId, param);
 
         // assign PID to a withdrawn Container -----------------------------
         try {
             assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(theContainerId))),
                     assignPidParam);
 
             assignObjectPid(theContainerId, pidParam);
             fail("ObjectPid assignment to a withdrawn Container is illegal.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test the last-modification-date in return value of assignObjectPid().
      *
      * @throws Exception Thrown if the last-modification-date in the return value differs from the
      *                   last-modification-date of the resource.
      */
     @Test
     public void testReturnValue02() throws Exception {
 
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
         String lmdCreate = getLastModificationDateValue(containerDoc);
 
         assertNull(containerDoc.getElementById(NAME_PID));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + containerId));
         String pidParam = getAssignPidTaskParam(getLastModificationDateValue2(containerDoc), assignPidParam);
 
         String resultXml = assignObjectPid(containerId, pidParam);
         assertXmlValidResult(resultXml);
 
         Document pidDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("assignObjectPid does not create a new timestamp", lmdResult, lmdCreate);
 
         containerXml = retrieve(containerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String lmdRetrieve = getLastModificationDateValue(containerDoc);
 
         assertEquals("Last modification date of result and container not equal", lmdResult, lmdRetrieve);
     }
 
     /**
      * Test the last-modification-date in return value of assignVersionPid().
      *
      * @throws Exception Thrown if the last-modification-date in the return value differs from the
      *                   last-modification-date of the resource.
      */
     @Test
     public void testReturnValue03() throws Exception {
 
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
         String lmdCreate = getLastModificationDateValue(containerDoc);
 
         assertNull(containerDoc.getElementById(NAME_PID));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + containerId));
         String pidParam = getAssignPidTaskParam(getLastModificationDateValue2(containerDoc), assignPidParam);
 
         String resultXml = assignVersionPid(containerId, pidParam);
         assertXmlValidResult(resultXml);
 
         Document pidDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("assignVersionPid does not create a new timestamp", lmdResult, lmdCreate);
 
         containerXml = retrieve(containerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String lmdRetrieve = getLastModificationDateValue(containerDoc);
 
         assertEquals("Last modification date of result and item not equal", lmdResult, lmdRetrieve);
 
         // now check last-modification-date for the whole assignment chain and
         // for later versions
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         pidParam = getAssignPidTaskParam(new DateTime(lmdResult, DateTimeZone.UTC), assignPidParam);
 
         resultXml = assignObjectPid(containerId, pidParam);
         assertXmlValidResult(resultXml);
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         resultXml = submit(containerId, getTheLastModificationParam(false, containerId, "comment", lmdResult));
         assertXmlValidResult(resultXml);
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         release(containerId, getTheLastModificationParam(false, containerId, "comment", lmdResult));
         containerXml = retrieve(containerId);
         containerXml = addCtsElement(containerXml);
         containerXml = update(containerId, containerXml);
 
         assignPidParam.setUrl(new URL(this.containerUrl + containerId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(containerId))), assignPidParam);
 
         resultXml = assignVersionPid(containerId, pidParam);
         assertXmlValidResult(resultXml);
 
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         containerXml = retrieve(containerId);
         containerDoc = EscidocAbstractTest.getDocument(containerXml);
         lmdRetrieve = getLastModificationDateValue(containerDoc);
 
         assertEquals("Last modification date of result and container not equal", lmdResult, lmdRetrieve);
     }
 
     /**
      * Check if the last modificaiton date timestamp is check and handled correctly for assignVersionPid() method.
      *
      * @throws Exception Thrown if last-modification-date is not checked as required.
      */
     @Test(expected = OptimisticLockingException.class)
     public void testOptimisticalLocking01() throws Exception {
 
         String wrongLmd = "2008-06-17T18:06:01.515Z";
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + this.theContainerId));
         String pidParam = getAssignPidTaskParam(new DateTime(wrongLmd, DateTimeZone.UTC), assignPidParam);
 
         assignVersionPid(this.theContainerId, pidParam);
     }
 
     /**
      * Check if the last modificaiton date timestamp is check and handled correctly for assignVersionPid() method.
      *
      * @throws Exception Thrown if last-modification-date is not checked as required.
      */
     @Test(expected = OptimisticLockingException.class)
     public void testOptimisticalLocking02() throws Exception {
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.containerUrl + theContainerId));
         String pidParam =
             getAssignPidTaskParam(new DateTime("2008-06-17T18:06:01.515Z", DateTimeZone.UTC), assignPidParam);
 
         assignObjectPid(this.theContainerId, pidParam);
     }
 
     /**
      * Test if value of the PID element within the taskParam XML is used to register the PID. Usually is a new PID
      * identifier is created but this could be skipped to provided register existing PIDs to a resource.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter01() throws Exception {
 
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
 
         String pidToRegister = "hdl:testPrefix/" + containerId;
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid(pidToRegister);
         String pidParam = getAssignPidTaskParam(getLastModificationDateValue2(containerDoc), assignPidParam);
 
         String pidXML = assignObjectPid(containerId, pidParam);
         compareContainerObjectPid(containerId, pidXML);
 
         Document pidDoc = getDocument(pidXML);
         Node returnedPid = selectSingleNode(pidDoc, XPATH_RESULT_PID);
         assertEquals(pidToRegister, returnedPid.getTextContent());
     }
 
     /**
      * Test if value of the PID element within the taskParam XML is used to register the PID. Usually is a new PID
      * identifier created but this could be skipped to provided register existing PIDs to a resource.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter02() throws Exception {
 
        String pidToRegister = "hdl:testPrefix/" + containerId;
         String containerXml = this.theContainerXml;
         Document containerDoc = EscidocAbstractTest.getDocument(containerXml);
         String containerId = getObjidValue(containerDoc);
         DateTime lmd = getLastModificationDateValue2(containerDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid(pidToRegister);
         String taskParam = getAssignPidTaskParam(lmd, assignPidParam);
 
         String pidXML = assignVersionPid(containerId, taskParam);
         compareContainerVersionPid(containerId, pidXML);
 
         Document pidDoc = getDocument(pidXML);
         Node returnedPid = selectSingleNode(pidDoc, XPATH_RESULT_PID);
         assertEquals(pidToRegister, returnedPid.getTextContent());
     }
 
     /**
      * Create last-modification parameter with or without withdrawn comment.
      *
      * @param includeWithdrawComment set true if a withdrawn comment is to include.
      * @return last-modification-param XML
      * @throws Exception If anything fails.
      */
     private String getTheLastModificationParam(final boolean includeWithdrawComment) throws Exception {
         Document doc = EscidocAbstractTest.getDocument(retrieve(theContainerId));
 
         // get last-modification-date
         NamedNodeMap atts = doc.getDocumentElement().getAttributes();
         Node lastModificationDateNode = atts.getNamedItem("last-modification-date");
         String lastModificationDate = lastModificationDateNode.getNodeValue();
 
         String param = "<param last-modification-date=\"" + lastModificationDate + "\" ";
         param += ">";
         if (includeWithdrawComment) {
             param += "<withdraw-comment>" + "WITHDRAW_COMMENT" + "</withdraw-comment>";
         }
         param += "</param>";
 
         return param;
     }
 
     /**
      * Create Item with the used interface method (REST/SOAP) for Container. The representation of an Item is different
      * for the used ingestion method.
      *
      * @return id of the created Item.
      * @throws Exception Thrown if ingest failed.
      */
     private String createItem() throws Exception {
         String itemId = null;
         // create an item and save the id
         String xmlData = getItemTemplate("escidoc_item_198_for_create.xml");
         String theItemXml = handleXmlResult(getItemClient().create(xmlData));
         itemId = getObjidValue(theItemXml);
         return itemId;
     }
 
     /**
      * Get id of latest container version.
      *
      * @param xml Container XML
      * @return latest-version objectId (escidoc:137:4)
      * @throws Exception Thrown if parsing failed.
      */
     private String getLatestVersionId(final String xml) throws Exception {
         Document xmlDoc = EscidocAbstractTest.getDocument(xml);
         String id = getObjidValue(xml);
         // String id = getIdFromRootElementHref(xmlDoc);
 
         Node versionNode = selectSingleNode(xmlDoc, "/container/properties/latest-version/number");
 
         return (id + VERSION_SUFFIX_SEPARATOR + versionNode.getTextContent());
     }
 
     /**
      * Compare the objectPid of an Container with the given value.
      *
      * @param id          The object id of the Item.
      * @param pidParamXml The XML from the assign process.
      * @throws Exception Thrown if the objectPid node of the Item does not exist or does not compares the the PID in the
      *                   pidParamXml.
      */
     private void compareContainerObjectPid(final String id, final String pidParamXml) throws Exception {
         // check objectPid
         String objPid = getObjectPid(id);
         assertNotNull(objPid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidParamXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), objPid);
     }
 
     /**
      * Compare the versionPid of an Item with the given value.
      *
      * @param id          The object id of the Item.
      * @param pidParamXml The XML from the assign process.
      * @throws Exception Thrown if the versionPid node of the Item does not exist or does not compares the the PID in
      *                   the pidParamXml.
      */
     private void compareContainerVersionPid(final String id, final String pidParamXml) throws Exception {
         // check objectPid
         String versionPid = getVersionPid(id);
         assertNotNull(versionPid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidParamXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), versionPid);
     }
 
     /**
      * Get the objectPid of the Container.
      *
      * @param objid The Container object id.
      * @return PID or null if objectPid element does not exist.
      * @throws Exception Thrown in case of any error.
      */
     private String getObjectPid(final String objid) throws Exception {
         String containerXml = retrieve(objid);
         Node objectPidNode =
             selectSingleNode(EscidocAbstractTest.getDocument(containerXml), XPATH_CONTAINER_OBJECT_PID);
         if (objectPidNode == null) {
             return null;
         }
         return (objectPidNode.getTextContent());
     }
 
     /**
      * Get the versionPid of the Item.
      *
      * @param objid The Item object id.
      * @return PID or null if versionPid element does not exist.
      * @throws Exception Thrown in case of any error.
      */
     private String getVersionPid(final String objid) throws Exception {
         Node versionPidNode =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieve(objid)), XPATH_CONTAINER_VERSION_PID);
         if (versionPidNode == null) {
             return null;
         }
         return (versionPidNode.getTextContent());
     }
 
 }
