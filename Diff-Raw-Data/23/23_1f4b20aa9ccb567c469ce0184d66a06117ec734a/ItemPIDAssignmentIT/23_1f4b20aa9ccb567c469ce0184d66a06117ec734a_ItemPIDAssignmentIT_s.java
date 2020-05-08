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
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.remote.application.violated.ReadonlyVersionException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.common.fedora.Client;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.net.URL;
 import java.util.Random;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the Persistent Identifier implementation of the item resource.
  *
  * @author Steffen Wagner
  */
 public class ItemPIDAssignmentIT extends ItemTestBase {
     /*
      * 2008 Jan. the current tests check only the configured behavior for the
      * MPDL. This is limited to change the behavior PID/release during the
      * runtime of the JBoss. Until it is possible to alter this behavior through
      * the Content Model check this JUnit-test only one configured behavior.
      * This means now: no release of an Item without an objectPid and a
      * versionPid. The pid assignment is ever possible until the object is
      * released.
      */
 
     // TODO check force setting of version or objectPid even if the
     // configuration is set to release without (which means is not a must have
     // but can have).
 
     private final String itemUrl;
 
     public ItemPIDAssignmentIT() {
         itemUrl = getFrameworkUrl() + "/ir/item/";
     }
 
     /**
      * Test the assignment of a objectPid (pid to floating object reference).
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public final void testAssignObjectPid1() throws Exception {
         // validate assignObjectPid() with the behavior of MPDL
         // no release without objectPID and versionPID
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
         assertNull(selectSingleNode(itemDoc, XPATH_ITEM_OBJECT_PID));
 
         submit(itemId, getTheLastModificationParam(itemId, false));
 
         String pidParam;
         String objectPid = null;
 
         if (getItemClient().getPidConfig("cmm.Item.objectPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + itemId));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
             objectPid = assignObjectPid(itemId, pidParam);
         }
         if (getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
 
             String latestVersion = getLatestVersionObjidValue(itemXml);
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + latestVersion));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(latestVersion))),
                     assignPidParam);
 
             assignVersionPid(latestVersion, pidParam);
         }
         release(itemId, getTheLastModificationParam(itemId, false));
 
         itemXml = retrieve(itemId);
         assertXmlValidItem(itemXml);
 
         if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
             Node itemObjectPidNode = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_OBJECT_PID);
             assertNotNull(itemObjectPidNode);
             Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(objectPid), XPATH_RESULT_PID);
 
             assertEquals(returnedPid.getTextContent(), itemObjectPidNode.getTextContent());
         }
         // TODO check versionPid too
     }
 
     /**
      * Test assignment of objectPid with version id. If the assignObjectPid() method is called with an identifier with
      * version part is the assignment to fulfill, because the right object is addressed. An additional check if the
      * assignObjectPid() method is called with an object identifier could prevent wrong used method calls. But ..
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public final void testAssignObjectPid2() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         String versionId = getObjidWithoutVersion(itemId) + ":1";
 
         assignObjectPid(versionId, pidParam);
     }
 
     /**
      * Check assignObjectPid().
      *
      * @throws Exception Thrown if anything fails.
      */
     @Test
     public void testAssignObjectPid3() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pidXML = assignObjectPid(itemId, true);
         compareItemObjectPid(itemId, pidXML);
     }
 
     /**
      * Test re-assignment of objectPid to a released item.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public final void testReAssignObjectPid() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         assignObjectPid(itemId, pidParam);
 
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         // re-assign objectPid
         try {
             assignObjectPid(itemId, pidParam);
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test re-assignment of object pid to a non-released item.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testReAssignObjectPid2() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pidXML = assignObjectPid(itemId, true);
         compareItemObjectPid(itemId, pidXML);
 
         try {
             assignObjectPid(itemId, false);
             fail("InvalidStatusException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Tests if the assignment to the first version of item is possible. This version is simultaneously the latest
      * version and release.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testAssignVersionPid1() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         assignAndCheckVersionPid(itemId);
     }
 
     /**
      * Tests if the assignment to a defined version of item is possible. The tested version is simultaneously the latest
      * version.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testAssignVersionPid2() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
         final int versionNumberWithPid = 2; // version where pid is assigned
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         // create version until 'versionNumberWithPid' -----------
         for (int i = 1; i < versionNumberWithPid; i++) {
             itemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, itemXml);
         }
         assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '"
             + versionNumberWithPid + "']");
         assertXmlValidItem(itemXml);
 
         assignAndCheckVersionPid(itemId);
     }
 
     /**
      * Tests if the assignment to a defined version of item is possible. All other versions has to be unchanged after
      * the procedure.
      * <p/>
      * The tested version is not the latest item version.
      *
      * @throws Exception In case of operation error.
      */
     @Test
     public void testAssignVersionPid3() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
 
         String pidXml = null;
         final int versionNumberWithPid = 4; // version where pid is assigned
         final int maxVersionNo = 4;
         String lmd = null;
 
         String itemXml = createItem();
         assertXmlValidItem(itemXml);
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         // check if no pid is already assigned
         Node node = selectSingleNode(itemDoc, XPATH_ITEM_VERSION_PID);
         assertNull(node);
 
         // make new version ----------------------------------------------------
         for (int i = 2; i <= versionNumberWithPid; i++) {
             itemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, itemXml);
             assertXmlValidItem(itemXml);
         }
 
         String versionId = itemId + ":" + versionNumberWithPid;
 
         itemDoc = EscidocAbstractTest.getDocument(itemXml);
         lmd = getTheLastModificationDate(itemDoc);
 
         if (getItemClient().getPidConfig("cmm.Item.objectPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + itemId));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
             pidXml = assignObjectPid(itemId, pidParam);
         }
         if (getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
 
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + versionId));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
             pidXml = assignVersionPid(versionId, pidParam);
         }
 
         // check if only the decided version has versionPid
 
         submit(versionId, getTheLastModificationParam(versionId, false));
         release(itemId, getTheLastModificationParam(itemId, false));
 
         // create more versions -----------------------------------------------
         itemXml = retrieve(itemId);
         for (int i = versionNumberWithPid + 1; i < maxVersionNo; i++) {
             assertXmlValidItem(itemXml);
             String newItemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, newItemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '" + i + "']");
         }
 
         // re-assign PID to version versionNumber (2)
         // -----------------------------
         try {
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL("http://somewhere/" + versionId));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
             assignVersionPid(versionId, pidParam);
             fail("InvalidStatusException expected. ");
         }
         catch (final InvalidStatusException e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         // check if returned pid equals item version-history
         // ---------------------
         Node returnedPid = null;
         if (!getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
             itemXml = retrieve(versionId);
             // FIXME pid is not assigned; exception was expected
             returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidXml), XPATH_RESULT_PID);
             Node currentVersionPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_VERSION_PID);
             assertNotNull(currentVersionPid);
             assertEquals(returnedPid.getTextContent(), currentVersionPid.getTextContent());
         }
 
         // check if no other version was altered
         // --------------------------------
         for (int i = 1; i < maxVersionNo; i++) {
             if (i != versionNumberWithPid) {
                 itemXml = retrieve(itemId + ":" + i);
                 returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_VERSION_PID);
 
                 assertNull(returnedPid);
                 assertXmlValidItem(itemXml);
             }
         }
     }
 
     /**
      * Check if in case of a second release, without PID assignment to the new version, the latest-release pid is
      * removed from the properties/RELS-EXT.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAssignVersionPID4() throws Exception {
 
         final String xPath = "/item/properties/content-model-specific";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pid = null;
 
         final int versionNumberWithPid = 2; // version where pid is assigned
         final int maxVersionNo = 6;
 
         // test if no pid is assigned already
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), "/item/properties/version/pid");
         assertNull(node);
         assertXmlValidItem(itemXml);
 
         // make new version ----------------------------------------------------
         for (int i = 2; i <= versionNumberWithPid; i++) {
             itemXml = addElement(itemXml, xPath + "/nix");
             assertXmlValidItem(itemXml);
             itemXml = update(itemId, itemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '"
                 + versionNumberWithPid + "']");
             assertXmlValidItem(itemXml);
         }
 
         String itemVersionId = itemId + ":" + versionNumberWithPid;
         // release Item -------------------------------------------------------
         try {
             submit(itemVersionId, getTheLastModificationParam(itemId, false));
             release(itemVersionId, getTheLastModificationParam(itemId, false));
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         // create more versions -----------------------------------------------
         itemXml = retrieve(itemVersionId);
         for (int i = versionNumberWithPid + 1; i < maxVersionNo; i++) {
             assertXmlValidItem(itemXml);
             String newItemXml = addElement(itemXml, xPath + "/nix");
             itemVersionId = getObjidValue(EscidocAbstractTest.getDocument(itemXml));
             itemXml = update(itemId, newItemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '" + i + "']");
         }
         // release Item -------------------------------------------------------
         try {
             submit(itemVersionId, getTheLastModificationParam(itemId, false));
             release(itemVersionId, getTheLastModificationParam(itemId, false));
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         // assign PID to version versionNumber (2) -----------------------------
         // this is not allowed (only the newest version can be assigned)
         itemVersionId = itemId + ":" + versionNumberWithPid;
         itemXml = retrieve(itemVersionId);
         try {
             // get last-modification-date
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(this.itemUrl + itemId));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(EscidocAbstractTest.getDocument(itemXml)),
                     assignPidParam);
 
             pid = assignVersionPid(itemVersionId, pidParam);
         }
         catch (final Exception e) {
             Class<?> ec = ReadonlyVersionException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
         assertNull(pid);
 
         // create a new version and release last item version
         // check if latest-release.pid is empty
         itemXml = retrieve(itemId);
         String newItemXml = addElement(itemXml, xPath + "/nix");
         itemVersionId = getObjidValue(EscidocAbstractTest.getDocument(itemXml));
         itemXml = update(itemId, newItemXml);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(EscidocAbstractTest.getDocument(itemXml)),
                 assignPidParam);
 
         assignObjectPid(itemId, pidParam);
 
         assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(EscidocAbstractTest.getDocument(retrieve(itemId))),
                 assignPidParam);
 
         assignVersionPid(itemId, pidParam);
 
         release(itemId, getTheLastModificationParam(itemId, false));
 
         itemXml = retrieve(itemId);
         String returnedPid =
             selectSingleNode(EscidocAbstractTest.getDocument(itemXml), "/item/properties/latest-release/pid")
                 .getTextContent();
         assertNotNull(returnedPid);
 
         // create once more version and check if latest-release.pid stil exists
         newItemXml = addElement(itemXml, xPath + "/nix");
         itemXml = update(itemId, newItemXml);
 
         node = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), "/item/properties/latest-release/pid");
         assertEquals(returnedPid, node.getTextContent());
     }
 
     // /**
     // * Tests assignVersionPid with providing object id but not providing
     // version
     // * number.
     // *
     // * @test.name Assign Version Pid - Missing version number
     // * @test.id OM_AVP-5
     // * @test.input <ul>
     // * <li>No version number specified in id parameter</li>
     // * <li>valid task parameters for assiging version pid</li>
     // * </ul>
     // * @test.inputDescription input contains wrong database
     // * @test.expected error message describing the reason for failure
     // * @test.status Implemented
     // *
     // * @throws Exception
     // * If anything fails.
     // */
     // FIXME:
     // It needs to be checked, if this test is correct. There are two
     // possibilities:
     // - It is allowed to address the item without the version number. In this
     // case, the pid should be assigned to the latest version, if this version
     // is in version-status "released". This is the way the test is implemented,
     // currently. If this assumption is true, another test is needed with
     // latest-version-status "= "released"!!!
     // - It is not allowed to address the item without specifying the version
     // number. In this case, an exception has to be thrown.
     // FIXME: re-enable this test if open problems (see above) are solved.
     @Test
     @Ignore("probably wrong test")
     public void notestOmAvp5() throws Exception {
 
         // create item
         final String toBeCreatedXml =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
         String createdXml = null;
         try {
             createdXml = create(toBeCreatedXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Prepare: Creating item failed.", e);
         }
         final String id = getObjidValue(createdXml);
         final String versionId = id + ":1";
 
         // submit item
         try {
             submit(versionId, getTheLastModificationParam(false, id));
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Prepare: Submitting item failed.", e);
         }
 
         // release item
         try {
             release(versionId, getTheLastModificationParam(false, id));
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Prepare: Releasing item failed.", e);
         }
 
         // assign PID without specifying the version number
         String pidXml = null;
         try {
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(this.itemUrl + id));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(id))), assignPidParam);
 
             pidXml = assignVersionPid(id, pidParam);
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
         assertNotNull(pidXml);
         final Node toBeAssertedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidXml), XPATH_RESULT_PID);
         assertNotNull(toBeAssertedPid);
 
         String retrievedXml = null;
         try {
             retrievedXml = retrieve(versionId);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Retrieving item failed.", e);
         }
 
         assertXmlEquals("Asserting version pid failed.", EscidocAbstractTest.getDocument(pidXml), XPATH_RESULT_PID,
             EscidocAbstractTest.getDocument(retrievedXml), XPATH_ITEM_VERSION_PID);
     }
 
     /**
      * Check re-assignment of pid.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testReAssignVersionPid() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pid = null;
 
         final int versionNumberWithPid = 3; // version where pid is assigned
         final int maxVersionNo = 4;
 
         // get last-modification-date
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL("http://escidoc.de/" + System.nanoTime()));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(EscidocAbstractTest.getDocument(itemXml)),
                 assignPidParam);
 
         // test if no pid is assigned already
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), "/item/properties/version/pid");
         assertNull(node);
         assertXmlValidItem(itemXml);
 
         // make new version ----------------------------------------------------
         for (int i = 2; i <= versionNumberWithPid; i++) {
             itemXml = addElement(itemXml, xPath + "/nix");
             assertXmlValidItem(itemXml);
             itemXml = update(itemId, itemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '" + i + "']");
             assertXmlValidItem(itemXml);
         }
 
         String itemVersionId = itemId + ":" + versionNumberWithPid;
 
         // release Item -------------------------------------------------------
         submit(itemVersionId, getTheLastModificationParam(itemId, false));
         if (!getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")) {
             release(itemVersionId, getTheLastModificationParam(itemId, false));
         }
 
         // create more versions -----------------------------------------------
         itemXml = retrieve(itemVersionId);
         for (int i = versionNumberWithPid + 1; i < maxVersionNo; i++) {
             assertXmlValidItem(itemXml);
             itemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, itemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '" + i + "']");
         }
 
         // assign PID to version versionNumber (VERSION_NUMBER_PID) ------------
         try {
             pidParam =
                "<param last-modification-date=\""
                    + getLastModificationDateValue(EscidocAbstractTest.getDocument(retrieve(itemVersionId))) + "\" >"
                    + "<url>http://escidoc.de/" + System.nanoTime() + "</url>" + "</param>";
 
             pid = assignVersionPid(itemVersionId, pidParam);
             String versionHistory = retrieveVersionHistory(itemId);
             assertXmlValidVersionHistory(versionHistory);
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         // check if returned pid equals item version-history -------------------
         itemXml = retrieve(itemVersionId);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         Node currentVersionPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_VERSION_PID);
         assertNotNull(currentVersionPid);
         assertEquals(returnedPid.getTextContent(), currentVersionPid.getTextContent());
 
         // re- assign PID to version versionNumber (VERSION_NUMBER_PID) --------
         try {
             pidParam =
                "<param last-modification-date=\""
                    + getLastModificationDateValue(EscidocAbstractTest.getDocument(retrieve(itemVersionId))) + "\" >"
                    + "<url>http://escidoc.de/" + System.nanoTime() + "</url>" + "</param>";
 
             pid = assignVersionPid(itemVersionId, pidParam);
             fail("InvalidStatusException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Tests the interdependences of assignment of object PID and version PID.
      *
      * @throws Exception In case of opeartion error.
      */
     @Test
     public void testAssignObjectAndVersionPID() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pid = null;
 
         final int versionNumberPid = 3;
         final int maxVersion = 3;
 
         String itemVersionId = null;
         String newItemXml = null;
 
         Node currentVersionPid = null;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         // create more versions -----------------------------------------------
         for (int i = 1; i < maxVersion; i++) {
             itemVersionId = itemId + ":" + i;
             itemXml = retrieve(itemVersionId);
             assertXmlValidItem(itemXml);
 
             newItemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, newItemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '"
                 + Integer.toString(i + 1) + "']");
         }
 
         // set object pid ---------------------------------------------------
 
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         pid = assignObjectPid(itemId, pidParam);
 
         // check
         itemXml = retrieve(itemId);
         Node latestReleasePid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_OBJECT_PID);
         assertNotNull(latestReleasePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), latestReleasePid.getTextContent());
         assertXmlValidItem(itemXml);
 
         // set version pid ----------------------------------------------------
         String versionId = itemId + ":" + versionNumberPid;
 
         assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         pid = assignVersionPid(versionId, pidParam);
 
         // check if no other version was altered ------------------------------
         for (int i = 1; i < maxVersion; i++) {
             itemXml = retrieve(itemId + ":" + i);
 
             if (i == versionNumberPid) {
                 returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
                 currentVersionPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_VERSION_PID);
                 assertNotNull(currentVersionPid);
                 assertEquals(returnedPid.getTextContent(), currentVersionPid.getTextContent());
 
             }
             else {
                 returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_VERSION_PID);
                 assertNull(returnedPid);
                 assertXmlValidItem(itemXml);
             }
         }
     }
 
     /**
      * Check reaction of wrong param in the assignObjectPid() method.
      *
      * @throws Exception Thrown if the assignObjectPid() method throws the wrong exception.
      */
     @Test
     public void testParam01() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         try {
             assignObjectPid(itemId, null);
             fail("MissingMethodParameterException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         try {
             assignObjectPid(itemId, "");
             fail("MissingMethodParameterException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Check reaction of wrong param in the assignVersionPid() method.
      *
      * @throws Exception Thrown if the assignObjectPid() method throws the wrong exception.
      */
     @Test
     public void testParam02() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String versionId = getLatestVersionObjidValue(itemDoc);
 
         try {
             assignVersionPid(versionId, null);
             fail("MissingMethodParameterException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
         try {
             assignVersionPid(versionId, "");
             fail("MissingMethodParameterException expected.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
 
     }
 
     /**
      * Test if the release PID is written only one to the RELS-EXT. See issue INFR-750
      *
      * @throws Exception Thrown if release/pid of RELS-EXT is not related to 'release', if multiple release/pid entries
      *                   exists or if framework access failed.
      */
     public void testReleasePid() throws Exception {
 
         final int numberOfIteration = 4;
         int noOfReleasePids = 0;
         // create Item
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         Node node = selectSingleNode(itemDoc, XPATH_ITEM_VERSION_NUMBER);
         String versionId = itemId + ":" + node.getTextContent();
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + versionId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         assignVersionPid(itemId, pidParam);
         noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
         assertTrue("assignVersionPid has written a release/pid into RELS-EXT", noOfReleasePids == 0);
 
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
         assignObjectPid(itemId, pidParam);
         noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
         assertTrue("assignObjectPid has written a release/pid into RELS-EXT", noOfReleasePids == 0);
 
         submit(itemId, getTheLastModificationParam(itemId, false));
         noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
         assertTrue("submit has written a release/pid into RELS-EXT", noOfReleasePids == 0);
 
         release(itemId, getTheLastModificationParam(itemId, false));
         noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
         assertTrue("release/pid is missing in RELS-EXT", noOfReleasePids == 1);
 
         itemXml = retrieve(itemId);
         itemDoc = EscidocAbstractTest.getDocument(itemXml);
         node = selectSingleNode(itemDoc, XPATH_ITEM_VERSION_PID);
         String versionPid = node.getTextContent();
 
         // create new versions -----------
         for (int i = 0; i < numberOfIteration; i++) {
 
             if (i > 0) {
                 itemXml = retrieve(itemId);
             }
 
             itemXml = addElement(itemXml, "/item/properties/content-model-specific/nix");
             itemXml = update(itemId, itemXml);
 
             // check if release PID hasn't changed
             node = selectSingleNode(itemDoc, XPATH_ITEM_LATEST_RELEASE_PID);
             String latestReleasePid = node.getTextContent();
 
             assertEquals("latest-release/pid differs from version/pid of " + "released version ", versionPid,
                 latestReleasePid);
 
             noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
             assertTrue("release/pid is missing in RELS-EXT", noOfReleasePids == 1);
 
             assignPidParam.setUrl(new URL(this.itemUrl + i));
             pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
             assignVersionPid(itemId, pidParam);
             noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
             assertTrue("release/pid is missing in RELS-EXT", noOfReleasePids == 1);
 
             submit(itemId, getTheLastModificationParam(itemId, false));
             noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
             assertTrue("release/pid is missing in RELS-EXT", noOfReleasePids == 1);
 
             release(itemId, getTheLastModificationParam(itemId, false));
             noOfReleasePids = checkRelsExtForReleasePidEntries(itemId);
             assertTrue("release/pid is missing in RELS-EXT", noOfReleasePids == 1);
         }
     }
 
     /**
      * Inspects RELS-EXT for release/pid entries.
      *
      * @param itemId The Fedora id of the resource
      * @return number of release/pids
      * @throws Exception Thrown if release/pid of RELS-EXT is not related to 'release', if multiple release/pid entries
      *                   exists or acces to Fedora failed.
      */
     private int checkRelsExtForReleasePidEntries(final String itemId) throws Exception {
 
         // retrieve RELS-EXT and look if release/pid is written only once
         // (see issue INFR-750)
         Client fc = new Client();
         String relsExt = fc.getDatastreamContent("RELS-EXT", itemId);
         Document relsExtDoc = EscidocAbstractTest.getDocument(relsExt);
 
         NodeList nodeList = selectNodeList(relsExtDoc, "/RDF/Description/pid");
         int noOfReleasePids = 0;
 
         for (int j = nodeList.getLength() - 1; j >= 0; j--) {
             NamedNodeMap atts = nodeList.item(j).getAttributes();
 
             for (int k = atts.getLength() - 1; k >= 0; k--) {
                 Node attrNode = atts.item(k);
                 String prefix = attrNode.getNodeName();
                 String nsUri = attrNode.getNodeValue();
                 if ("http://escidoc.de/core/01/properties/release/".equals(nsUri)) {
 
                     assertEquals("Release NS related to prefix 'version'", "xmlns:release", prefix);
                     noOfReleasePids++;
                 }
             }
         }
         assertFalse("release/pid is written multiple times to RELS-EXT", noOfReleasePids > 1);
 
         return noOfReleasePids;
     }
 
     /**
      * Check pid assigment with lower user permissions.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testObjectPidAssignmentPermission1() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
 
         testAssignObjectPid3();
     }
 
     /**
      * Check pid assigment with lower user permissions.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testVersionPidAssignmentPermission1() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
 
         testAssignVersionPid1();
     }
 
     /**
      * Check pid assigment with lower user permissions. Assign before item has status "submitted" or "released".
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testPidAssignmentPermission1() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
 
         testAssignObjectPid3();
         testAssignVersionPid1();
     }
 
     /**
      * Check pid assigment with lower user permissions. Assign in item status "released".
      *
      * @throws Exception If anything fails.
      */
     public void notestObjectPidAssignmentPermissionC() throws Exception {
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         testAssignObjectPid1();
     }
 
     /**
      * Check pid assignment with lower user permissions. Assign before item has status "submitted" and release later.
      * Check if PID values exists after status change in properties.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public final void testObjectPidAssignmentPermissionD() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         assertNull(selectSingleNode(itemDoc, XPATH_ITEM_OBJECT_PID));
 
         String pidXML = assignObjectPid(itemId, true);
         compareItemObjectPid(itemId, pidXML);
 
         assignAndCheckVersionPid(itemId);
     }
 
     /**
      * Test create item with initial object pid.
      *
      * @throws Exception .
      */
     @Test
     public void testCreateItemWithObjPid() throws Exception {
         Document xmlItem =
             EscidocAbstractTest.getTemplateAsDocument(TEMPLATE_ITEM_PATH + "/rest", "item_with_object_pid.xml");
 
         String pidValue = "hdl:someHandle/" + getRandom() + "/" + getRandom();
         Node pidNode = selectSingleNode(xmlItem, XPATH_ITEM_OBJECT_PID);
         pidNode.setTextContent(pidValue);
 
         String item = toString(xmlItem, true);
         item = create(item);
         assertXmlValidItem(item);
         Node node = selectSingleNode(EscidocAbstractTest.getDocument(item), XPATH_ITEM_OBJECT_PID);
         assertEquals(node.getTextContent(), pidValue);
     }
 
     /**
      * Check if the configurable release behavior in relation to PID fulfills the requirements.
      *
      * @throws Exception Thrown if anything fails.
      */
     @Test
     public void testRelease() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         assertNull("Item has already an objectPid.", getObjectPid(itemId));
 
         /*
          * - release with/
          */
         if (getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "true")) {
             // release without objectPID
             submit(itemId, getTheLastModificationParam(itemId, false));
             release(itemId, getTheLastModificationParam(itemId, false));
             assertXmlValidItem(itemXml);
             assertNull("Item has objectPid without assignment.", getObjectPid(itemId));
 
             // assign objectPid after release - if it is allowed through config
             if (getItemClient().getPidConfig("cmm.Item.objectPid.setPidAfterRelease", "true")) {
 
                 String id = getObjidWithoutVersion(itemId);
 
                 AssignParam assignPidParam = new AssignParam();
                 assignPidParam.setUrl(new URL(this.itemUrl + id));
                 String pidParam =
                     getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(id))), assignPidParam);
 
                 String pidXML = assignObjectPid(id, pidParam);
 
                 assertXmlValidItem(itemXml);
                 compareItemObjectPid(itemId, pidXML);
             }
             else {
                 // check if the right exception is thrown
                 String id = getObjidWithoutVersion(itemId);
 
                 AssignParam assignPidParam = new AssignParam();
                 assignPidParam.setUrl(new URL(this.itemUrl + id));
                 String pidParam =
                     getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(id))), assignPidParam);
 
                 try {
                     assignObjectPid(id, pidParam);
                     fail("ObjectPid assignment after release is forbidden by" + " configuration");
                 }
                 catch (final Exception e) {
                     Class<?> ec = InvalidStatusException.class;
                     EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
                 }
             }
         }
         else { // cmm.Item.objectPid.releaseWithoutPid = false ---------------
             // check exception for release without objectPID
             submit(itemId, getTheLastModificationParam(itemId, false));
             try {
                 release(itemId, getTheLastModificationParam(itemId, false));
                 if (getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "true").equals("false")) {
                     fail("Through the configuration is the Item not releasable" + " without objectPid.");
                 }
             }
             catch (final Exception e) {
                 Class<?> ec = InvalidStatusException.class;
                 EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
             }
             assertXmlValidItem(itemXml);
 
             if (!getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "true")) {
                 try {
                     release(itemId, getTheLastModificationParam(itemId, false));
                     fail("Through the configuration is the Item not releasable" + " without versionPid.");
                 }
                 catch (final Exception e) {
                     Class<?> ec = InvalidStatusException.class;
                     EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
                 }
 
                 // assign versionPid
                 String versionNumber = getVersionNumber(itemId);
                 String versionId = itemId;
                 if (versionNumber == null) {
                     versionId = getLatestVersionObjidValue(itemDoc);
                 }
 
                 AssignParam assignPidParam = new AssignParam();
                 assignPidParam.setUrl(new URL(this.itemUrl + versionId));
                 String pidParam =
                     getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))),
                         assignPidParam);
                 String pidXML = assignVersionPid(versionId, pidParam);
                 compareItemVersionPid(itemId, pidXML);
             }
 
             // release with objectPID
             String id = getObjidWithoutVersion(itemId);
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(this.itemUrl + id));
             String pidParam =
                 getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(id))), assignPidParam);
             String pidXML = assignObjectPid(id, pidParam);
 
             assertXmlValidItem(itemXml);
             compareItemObjectPid(itemId, pidXML);
             if (getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "true").equals("true")) {
                 release(itemId, getTheLastModificationParam(itemId, false));
             }
 
         }
 
     }
 
     /**
      * Check if in case of an release the right exceptions are thrown if now PID was assigned before.
      *
      * @throws Exception Thrown if release is possible without the required PIDs.
      */
     @Test
     public void testReleaseExceptionForPid() throws Exception {
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         assertNull("Item has already an objectPid.", getObjectPid(itemId));
 
         try {
             release(itemId, getTheLastModificationParam(itemId, false));
             if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
                 fail("release without objectPid is possible but forbidden by " + "configuration.");
             }
             if (!getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
                 fail("release without versionPid is possible but forbidden by " + "configuration.");
             }
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Check if the last-modification-date of the pid result is equal to the last-modification-date of the retrieved
      * Item.
      *
      * @throws Exception Thrown in case of failure.
      */
     @Test
     public void testCompareLastModDateObjPid() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String lmdCreate = getLastModificationDateValue(itemDoc);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam = getAssignPidTaskParam(new DateTime(lmdCreate, DateTimeZone.UTC), assignPidParam);
         String pidXML = assignObjectPid(itemId, pidParam);
 
         Document pidDoc = EscidocAbstractTest.getDocument(pidXML);
         String lmdPid = getLastModificationDateValue(pidDoc);
 
         assertNotEquals("Last modification timestamp was not updated.", lmdPid, lmdCreate);
 
         String itemXmlRetrieve = retrieve(itemId);
         Document itemDocRetrieve = EscidocAbstractTest.getDocument(itemXmlRetrieve);
         String lmdRetrieve = getLastModificationDateValue(itemDocRetrieve);
 
         assertEquals(lmdPid, lmdRetrieve);
     }
 
     /**
      * Check if the last-modification-date of the pid result is equal to the last-modification-date of the retrieved
      * Item.
      *
      * @throws Exception Thrown in case of failure.
      */
     @Test
     public void testCompareLastModDateVersionPid() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String lmdCreate = getLastModificationDateValue(itemDoc);
         String itemVersionId = getObjidValue(itemDoc) + ":1";
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemVersionId));
         String pidParam = getAssignPidTaskParam(new DateTime(lmdCreate, DateTimeZone.UTC), assignPidParam);
 
         String pidXML = assignVersionPid(itemVersionId, pidParam);
 
         Document pidDoc = EscidocAbstractTest.getDocument(pidXML);
         String lmdPid = getLastModificationDateValue(pidDoc);
 
         assertNotEquals("Last modification timestamp was not updated.", lmdPid, lmdCreate);
 
         String itemXmlRetrieve = retrieve(itemVersionId);
         Document itemDocRetrieve = EscidocAbstractTest.getDocument(itemXmlRetrieve);
         String lmdRetrieve = getLastModificationDateValue(itemDocRetrieve);
 
         assertEquals("", lmdPid, lmdRetrieve);
     }
 
     /**
      * Test assignVersionPid() with Container id without version suffix.
      * <p/>
      * Since build 276 is the interface behavior consistent to the other method calls. Before build 276 must the
      * assign-version-pid method be called with an identifier including the version suffix. With build 276 was this
      * removed. The method could be called with version identifier but not has to. If the identifier has no version
      * suffix than is the latest/newest version assigned with a version pid.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testVersionSuffix() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         // String lmdCreate = getLastModificationDateValue(itemDoc);
         String itemId = getObjidValue(itemDoc);
 
         String pidXml = null;
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
         try {
             pidXml = assignVersionPid(itemId, pidParam);
         }
         catch (final MissingMethodParameterException e) {
             fail("AssignVersionPid() does check for a version number.");
         }
 
         // check if the newest version of container is assigned with the version
         // pid
         String currentVersionPid = getVersionPid(itemId);
 
         Node pid = selectSingleNode(EscidocAbstractTest.getDocument(pidXml), XPATH_RESULT_PID);
         assertEquals(currentVersionPid, pid.getTextContent());
     }
 
     /**
      * Test if the right exception is thrown if an older version is assigned.
      *
      * @throws Exception If behavior is not like expected.
      */
     @Test
     public void testAssignToOlderVersion() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pid = null;
 
         final int versionNumberPid = 3;
         final int maxVersion = 6;
 
         String itemVersionId = null;
         String newItemXml = null;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         // create more versions -----------------------------------------------
         for (int i = 1; i < maxVersion; i++) {
             itemVersionId = itemId + ":" + i;
             itemXml = retrieve(itemVersionId);
             assertXmlValidItem(itemXml);
 
             newItemXml = addElement(itemXml, xPath + "/nix");
             itemXml = update(itemId, newItemXml);
             assertXmlExists("New version number", itemXml, "/item/properties/version/number[text() = '"
                 + Integer.toString(i + 1) + "']");
         }
 
         // set object pid ---------------------------------------------------
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         pid = assignObjectPid(itemId, pidParam);
 
         // check
         itemXml = retrieve(itemId);
         Node latestReleasePid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_OBJECT_PID);
         assertNotNull(latestReleasePid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), latestReleasePid.getTextContent());
         assertXmlValidItem(itemXml);
 
         // set version pid ----------------------------------------------------
         Class<?> ec = ReadonlyVersionException.class;
 
         String versionId = itemId + ":" + versionNumberPid;
 
         assignPidParam.setUrl(new URL(this.itemUrl + versionId));
         pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
 
         try {
             pid = assignVersionPid(versionId, pidParam);
         }
         catch (final Exception e) {
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
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
         String lmdCreate = getLastModificationDateValue(itemDoc);
 
         assertNull(itemDoc.getElementById(NAME_PID));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         String resultXml = assignObjectPid(itemId, pidParam);
         assertXmlValidResult(resultXml);
 
         Document pidDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("assignObjectPid does not create a new timestamp", lmdResult, lmdCreate);
 
         itemXml = retrieve(itemId);
         itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String lmdRetrieve = getLastModificationDateValue(itemDoc);
 
         assertEquals("Last modification date of result and item not equal", lmdResult, lmdRetrieve);
     }
 
     /**
      * Test the last-modification-date in return value of assignVersionPid().
      *
      * @throws Exception Thrown if the last-modification-date in the return value differs from the
      *                   last-modification-date of the resource.
      */
     @Test
     public void testReturnValue03() throws Exception {
         final String xPath = "/item/properties/content-model-specific";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
         String lmdCreate = getLastModificationDateValue(itemDoc);
 
         assertNull(itemDoc.getElementById(NAME_PID));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         String resultXml = assignVersionPid(itemId, pidParam);
         assertXmlValidResult(resultXml);
 
         Document pidDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("assignVersionPid does not create a new timestamp", lmdResult, lmdCreate);
 
         itemXml = retrieve(itemId);
         itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String lmdRetrieve = getLastModificationDateValue(itemDoc);
 
         assertEquals("Last modification date of result and item not equal", lmdResult, lmdRetrieve);
 
         // now check last-modification-date for the whole assignment chain and
         // for later versions
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam = getAssignPidTaskParam(new DateTime(lmdResult, DateTimeZone.UTC), assignPidParam);
 
         resultXml = assignObjectPid(itemId, pidParam);
         assertXmlValidResult(resultXml);
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         resultXml = submit(itemId, getTheLastModificationParam(false, itemId, "comment", lmdResult));
         assertXmlValidResult(resultXml);
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         release(itemId, getTheLastModificationParam(false, itemId, "comment", lmdResult));
         itemXml = retrieve(itemId);
         itemXml = addElement(itemXml, xPath + "/nix");
         itemXml = update(itemId, itemXml);
 
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         pidParam = getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(itemId))), assignPidParam);
 
         resultXml = assignVersionPid(itemId, pidParam);
         assertXmlValidResult(resultXml);
 
         pidDoc = EscidocAbstractTest.getDocument(resultXml);
         lmdResult = getLastModificationDateValue(pidDoc);
 
         itemXml = retrieve(itemId);
         itemDoc = EscidocAbstractTest.getDocument(itemXml);
         lmdRetrieve = getLastModificationDateValue(itemDoc);
 
         assertEquals("Last modification date of result and item not equal", lmdResult, lmdRetrieve);
     }
 
     /**
      * Check if the last modificaiton date timestamp is check and handled correctly for assignVersionPid() method.
      *
      * @throws Exception Thrown if last-modification-date is not checked as required.
      */
     @Test
     public void testOptimisticalLocking01() throws Exception {
 
         Class<?> ec = OptimisticLockingException.class;
         String wrongLmd = "2008-06-17T18:06:01.515Z";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam = getAssignPidTaskParam(new DateTime(wrongLmd, DateTimeZone.UTC), assignPidParam);
 
         try {
             assignVersionPid(itemId, pidParam);
             fail("Missing OptimisticalLockingException");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
 
         }
     }
 
     /**
      * Check if the last modification date timestamp is check and handled correctly for assignVersionPid() method.
      *
      * @throws Exception Thrown if last-modification-date is not checked as required.
      */
     @Test
     public void testOptimisticalLocking02() throws Exception {
 
         Class<?> ec = OptimisticLockingException.class;
         String wrongLmd = "2008-06-17T18:06:01.515Z";
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam = getAssignPidTaskParam(new DateTime(wrongLmd, DateTimeZone.UTC), assignPidParam);
 
         try {
             assignObjectPid(itemId, pidParam);
             fail("Missing OptimisticalLockingException");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
 
         }
     }
 
     /**
      * Test if value of the PID element within the taskParam XML is used to register the PID. Usually is a new PID
      * identifier is created but this could be skipped to provided register existing PIDs to a resource.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter01() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pidToRegister = "hdl:testPrefix/" + itemId;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid(pidToRegister);
         String taskParam = getAssignPidTaskParam(getLastModificationDateValue2(itemDoc), assignPidParam);
 
         String pidXML = assignObjectPid(itemId, taskParam);
         compareItemObjectPid(itemId, pidXML);
 
         Document pidDoc = getDocument(pidXML);
         Node returnedPid = selectSingleNode(pidDoc, XPATH_RESULT_PID);
         assertEquals(pidToRegister, returnedPid.getTextContent());
     }
 
     /**
      * Test if value of the PID element within the taskParam XML is used to register the PID. Usually is a new PID
      * identifier is created but this could be skipped to provided register existing PIDs to a resource.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter02() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         String pidToRegister = "hdl:testPrefix/" + itemId;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid(pidToRegister);
         String taskParam = getAssignPidTaskParam(getLastModificationDateValue2(itemDoc), assignPidParam);
 
         String pidXML = assignVersionPid(itemId, taskParam);
         compareItemVersionPid(itemId, pidXML);
 
         Document pidDoc = getDocument(pidXML);
         Node returnedPid = selectSingleNode(pidDoc, XPATH_RESULT_PID);
         assertEquals(pidToRegister, returnedPid.getTextContent());
     }
 
     /**
      * Test if an empty value of the PID element within the taskParam XML is handled correct.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter05() throws Exception {
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid("");
         String taskParam = getAssignPidTaskParam(getLastModificationDateValue2(itemDoc), assignPidParam);
 
         Class<?> ec = XmlCorruptedException.class;
 
         try {
             assignVersionPid(itemId, taskParam);
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
 
         String itemXml = createItem();
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         String itemId = getObjidValue(itemDoc);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid("");
         String taskParam = getAssignPidTaskParam(getLastModificationDateValue2(itemDoc), assignPidParam);
 
         Class<?> ec = XmlCorruptedException.class;
 
         try {
             assignObjectPid(itemId, taskParam);
             fail("Expect exception if pid element in taskParam is empty.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Create the last modification request parameter.
      *
      * @param itemId                 The id of the Item.
      * @param includeWithdrawComment Set if withdraw comment is to include or not.
      * @return last modification request parameter
      * @throws Exception Thrown if anything fails.
      */
     private String getTheLastModificationParam(final String itemId, final boolean includeWithdrawComment)
         throws Exception {
 
         return getTheLastModificationParam(includeWithdrawComment, itemId);
     }
 
     /**
      * Delivers integer random value from range 100 to 9999.
      *
      * @return integer random number [100-9999]
      */
     private int getRandom() {
         final int min = 100;
         final int max = 10000;
         Random r = new Random();
 
         int v = 0;
         do {
             v = r.nextInt(max);
         }
         while (v < min);
         return (v);
     }
 
     /**
      * Check the assignment of an versionPid.
      *
      * @param itemId The object id of the Item. If the objectId has no Version number the test operates on the latest
      *               version.
      * @throws Exception If anything fails.
      */
     private void assignAndCheckVersionPid(final String itemId) throws Exception {
 
         // bring item in status released if necessary
         if (!getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
             if (!getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")) {
                 String status = getObjectStatus(itemId);
 
                 if (!status.equals(STATE_RELEASED)) {
                     if (!status.equals(STATE_SUBMITTED)) {
                         submit(itemId, getTheLastModificationParam(itemId, false));
                     }
                     release(itemId, getTheLastModificationParam(itemId, false));
                 }
             }
         }
 
         String itemXml = retrieve(itemId);
         assertXmlValidItem(itemXml);
 
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         assertNull(selectSingleNode(itemDoc, XPATH_ITEM_VERSION_PID));
 
         String versionNumber = getVersionNumber(itemId);
         String versionId = itemId;
         if (versionNumber == null) {
             versionId = getLatestVersionObjidValue(itemDoc);
         }
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + versionId));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(versionId))), assignPidParam);
         String versionPid = assignVersionPid(versionId, pidParam);
         String retrievedItem = retrieve(versionId);
 
         // check versionPid
         Node versionPidNode = selectSingleNode(EscidocAbstractTest.getDocument(retrievedItem), XPATH_ITEM_VERSION_PID);
         // "/item/properties/version[number=1]/pid"
         assertNotNull(versionPidNode);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(versionPid), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), versionPidNode.getTextContent());
     }
 
     /**
      * Check the assignment of an objectPid.
      *
      * @param itemId The object id of the item.
      * @param ckPid  Set true if the resource is to check if no pid exist before assignment.
      * @return The XML structure from the assign method.
      * @throws Exception Thrown if anything fails.
      */
     private String assignObjectPid(final String itemId, final boolean ckPid) throws Exception {
 
         // bring item in status released if necessary
         if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
             if (!getItemClient().getPidConfig("cmm.Item.objectPid.setPidBeforeRelease", "true")) {
                 String status = getObjectStatus(itemId);
 
                 if (!status.equals(STATE_RELEASED)) {
                     if (!status.equals(STATE_SUBMITTED)) {
                         submit(itemId, getTheLastModificationParam(itemId, false));
                     }
                     release(itemId, getTheLastModificationParam(itemId, false));
                 }
             }
         }
 
         String itemXml = retrieve(itemId);
         assertXmlValidItem(itemXml);
 
         Document itemDoc = EscidocAbstractTest.getDocument(itemXml);
         if (ckPid) {
             assertNull(selectSingleNode(itemDoc, XPATH_ITEM_OBJECT_PID));
         }
         String id = getObjidWithoutVersion(itemId);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + id));
         String pidParam =
             getAssignPidTaskParam(getLastModificationDateValue2(getDocument(retrieve(id))), assignPidParam);
         return assignObjectPid(id, pidParam);
     }
 
     /**
      * Compare the objectPid of an Item with the given value.
      *
      * @param id          The object id of the Item.
      * @param pidParamXml The XML from the assign process.
      * @throws Exception Thrown if the objectPid node of the Item does not exist or does not compares the the PID in the
      *                   pidParamXml.
      */
     private void compareItemObjectPid(final String id, final String pidParamXml) throws Exception {
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
     private void compareItemVersionPid(final String id, final String pidParamXml) throws Exception {
         // check objectPid
         String versionPid = getVersionPid(id);
         assertNotNull(versionPid);
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidParamXml), XPATH_RESULT_PID);
         assertEquals(returnedPid.getTextContent(), versionPid);
     }
 
     /**
      * Create a new Item.
      *
      * @return The Item XML representation.
      * @throws Exception Thrown if creation of item fails.
      */
     private String createItem() throws Exception {
 
         String xmlData =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         return (create(xmlData));
     }
 
     /**
      * Get the objectPid of the Item.
      *
      * @param objid The Item object id.
      * @return PID or null if objectPid element does not exist.
      * @throws Exception Thrown in case of any error.
      */
     private String getObjectPid(final String objid) throws Exception {
         String itemXml = retrieve(objid);
         Node objectPidNode = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_ITEM_OBJECT_PID);
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
             selectSingleNode(EscidocAbstractTest.getDocument(retrieve(objid)), XPATH_ITEM_VERSION_PID);
         if (versionPidNode == null) {
             return null;
         }
         return (versionPidNode.getTextContent());
     }
 }
