 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.MandatorEngine;
 import com.flexive.shared.interfaces.TypeEngine;
 import com.flexive.shared.interfaces.AssignmentEngine;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.AccountEdit;
 import com.flexive.shared.security.ACL;
import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import static com.flexive.tests.embedded.FxTestUtils.*;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.testng.Assert;
 import static org.testng.Assert.*;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * Mandator tests.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "mandator"})
 public class MandatorTest {
 
     private static final Log LOG = LogFactory.getLog(MandatorTest.class);
 
     private MandatorEngine me = null;
     private TypeEngine te = null;
     private ContentEngine ce = null;
     private AssignmentEngine ass = null;
     private long testMandator = -1;
     private long testType = -1;
 
     @BeforeClass
     public void beforeClass() throws Exception {
         me = EJBLookup.getMandatorEngine();
         te = EJBLookup.getTypeEngine();
         ce = EJBLookup.getContentEngine();
         ass = EJBLookup.getAssignmentEngine();
         login(TestUsers.SUPERVISOR);
         try {
             testMandator = me.create("MANDATOR_" + RandomStringUtils.randomAlphanumeric(10), true);
             testType = te.save(FxTypeEdit.createNew("TEST_" + RandomStringUtils.randomAlphanumeric(10)));
         } catch (FxApplicationException e) {
             LOG.error(e);
         }
     }
 
     @AfterClass
     public void afterClass() throws Exception {
         if (testMandator != -1) {
             if (testType != -1)
                 ce.removeForType(testType);
             me.remove(testMandator);
         }
         logout();
     }
 
     /**
      * Tests if the current mandator (= the test user's mandator) is active.
      * If it wasn't active, the test user may not have been permitted to log in
      * at all.
      *
      * @throws Exception if an error occured
      */
     public void currentMandatorActive() throws Exception {
         Mandator mandator = CacheAdmin.getEnvironment().getMandator(getUserTicket().getMandatorId());
         assert mandator.isActive() : "Current mandator is inactive.";
     }
 
     /**
      * Tests the creation/removal of new mandators.
      *
      * @throws Exception if an error occured
      */
     public void createRemoveMandator() throws Exception {
         int mandatorId = me.create("TestMandator", false);
         Mandator test = null;
         try {
             test = CacheAdmin.getEnvironment().getMandator(mandatorId);
         } catch (Exception e) {
             assert false : "Failed to get created mandator from cache: " + e.getMessage();
         }
         assert test != null : "Loaded mandator is null!";
         assert test.getName().equals("TestMandator") : "Name mismatch!";
         assert !test.hasMetadata() : "Mandator should have no meta data attached!";
         assert !test.isActive() : "Mandator should be inactive!";
         me.activate(test.getId());
         try {
             test = CacheAdmin.getEnvironment().getMandator(mandatorId);
         } catch (Exception e) {
             assert false : "Failed to get activated mandator from cache: " + e.getMessage();
         }
         assert test.isActive() : "Mandator should be active!";
         me.deactivate(test.getId());
         try {
             test = CacheAdmin.getEnvironment().getMandator(mandatorId);
         } catch (Exception e) {
             assert false : "Failed to get deactivated mandator from cache: " + e.getMessage();
         }
         assert !test.isActive() : "Mandator should be deactivated!";
 
         me.remove(test.getId());
         try {
             CacheAdmin.getEnvironment().getMandator(mandatorId);
             assert false : "Removed mandator could be retrieved from cache!";
         } catch (Exception e) {
             //ignore
         }
         try {
             me.remove(Mandator.MANDATOR_FLEXIVE); //try to remove the public mandator -> got to fail
             assert false : "Removing the public mandator should have failed!";
         } catch (FxEntryInUseException e) {
             //ok
         } catch (Exception e) {
             assert false : "Unexpected exception: " + e.getMessage();
         }
     }
 
     /**
      * Test active/inactive mandators with contents
      *
      * @throws FxApplicationException on errors
      */
     public void activeContent() throws FxApplicationException {
         me.activate(testMandator); //make sure the mandator is active
         FxType type = CacheAdmin.getEnvironment().getType(testType);
         Assert.assertTrue(CacheAdmin.getEnvironment().getMandator(testMandator).isActive(), "Expected mandator to be active!");
         FxContent co_act1 = ce.initialize(testType, testMandator, -1, type.getWorkflow().getSteps().get(0).getId(), FxLanguage.DEFAULT_ID);
         me.deactivate(testMandator);
         Assert.assertFalse(CacheAdmin.getEnvironment().getMandator(testMandator).isActive(), "Expected mandator to be inactive!");
         try {
             ce.initialize(testType, testMandator, -1, type.getWorkflow().getSteps().get(0).getId(), FxLanguage.DEFAULT_ID);
             Assert.fail("Initialize on a deactivated mandator should fail!");
         } catch (FxNotFoundException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxNotFoundException expected! Got: " + ex.getClass().getCanonicalName());
         }
         try {
             ce.save(co_act1);
             Assert.fail("Save on a deactivated mandator should fail!");
         } catch (FxCreateException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxCreateException expected! Got: " + ex.getClass().getCanonicalName());
         }
         me.activate(testMandator);
         FxPK pk = ce.save(co_act1);
         FxContent co_loaded = ce.load(pk);
         FxPK pk_ver = ce.createNewVersion(co_loaded);
         ce.removeVersion(pk_ver);
         ce.remove(pk);
         pk = ce.save(co_act1);
         me.deactivate(testMandator);
         try {
             ce.load(pk);
             Assert.fail("Load on a deactivated mandator should fail!");
         } catch (FxNotFoundException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxNotFoundException expected! Got: " + ex.getClass().getCanonicalName());
         }
         me.activate(testMandator);
         co_loaded = ce.load(pk);
         me.deactivate(testMandator);
         try {
             ce.createNewVersion(co_loaded);
             Assert.fail("CreateNewVersion on a deactivated mandator should fail!");
         } catch (FxNotFoundException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxNotFoundException expected! Got: " + ex.getClass().getCanonicalName());
         }
         me.activate(testMandator);
         pk_ver = ce.createNewVersion(co_loaded);
         me.deactivate(testMandator);
         try {
             ce.removeVersion(pk_ver);
             Assert.fail("RemoveVersion on a deactivated mandator should fail!");
         } catch (FxNotFoundException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxNotFoundException expected! Got: " + ex.getClass().getCanonicalName());
         }
         try {
             ce.remove(pk);
             Assert.fail("Remove on a deactivated mandator should fail!");
         } catch (FxRemoveException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxRemoveException expected! Got: " + ex.getClass().getCanonicalName());
         }
         me.activate(testMandator);
     }
 
     /**
      * Test active/inactive mandators with the tree
      *
      * @throws FxApplicationException on errors
      */
     public void activeTree() throws Exception {
         EJBLookup.getTreeEngine().clear(FxTreeMode.Edit);
         me.activate(testMandator); //make sure we're active
         FxType type = CacheAdmin.getEnvironment().getType(testType);
         Assert.assertTrue(CacheAdmin.getEnvironment().getMandator(testMandator).isActive(), "Expected mandator to be active!");
         FxContent co_act1 = ce.initialize(testType, testMandator, -1, type.getWorkflow().getSteps().get(0).getId(), FxLanguage.DEFAULT_ID);
         FxPK pk = ce.save(co_act1);
         EJBLookup.getTreeEngine().getNode(FxTreeMode.Edit, FxTreeNode.ROOT_NODE).getTotalChildCount();
         long folder = EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("MandatorTestFolder"));
         long node_root = EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("MandatorTestRoot").setReference(pk));
         EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("MandatorTestChild").setReference(pk).setParentNodeId(folder));
         FxTreeNode node = EJBLookup.getTreeEngine().getTree(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 100);
         Assert.assertEquals(node.getTotalChildCount(), 3);
         Assert.assertEquals(node.getDirectChildCount(), 2);
         Assert.assertEquals(node.getChildren().size(), 2);
         Assert.assertEquals(node.getChildren().get(0).getChildren().size(), 1);
         me.deactivate(testMandator);
         node = EJBLookup.getTreeEngine().getTree(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 100);
         Assert.assertEquals(node.getChildren().size(), 1);
         Assert.assertEquals(node.getChildren().get(0).getChildren().size(), 0);
         try {
             EJBLookup.getTreeEngine().getNode(FxTreeMode.Edit, node_root);
             Assert.fail("getNode on a deactivated mandator should fail!");
         } catch (FxNotFoundException e) {
             //expected
         } catch (Exception ex) {
             Assert.fail("FxNotFoundException expected! Got: " + ex.getClass().getCanonicalName());
         }
         me.activate(testMandator);
         node = EJBLookup.getTreeEngine().getTree(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, 100);
         Assert.assertEquals(node.getChildren().size(), 2);
         Assert.assertEquals(node.getChildren().get(0).getChildren().size(), 1);
         EJBLookup.getTreeEngine().getNode(FxTreeMode.Edit, node_root);
         EJBLookup.getTreeEngine().clear(FxTreeMode.Edit);
     }
 
     /**
      * Test active/inactive mandators concerning login
      *
      * @throws Exception on errors
      */
     public void activeLogin() throws Exception {
         String name = "USR_" + RandomStringUtils.randomAlphanumeric(10);
         String pwd = RandomStringUtils.randomAlphanumeric(10);
         final AccountEdit account = new AccountEdit()
                 .setName(name)
                 .setLoginName(name)
                 .setEmail("test@flexive.org")
                 .setMandatorId(testMandator);
         long accountId = EJBLookup.getAccountEngine().create(account, pwd);
         try {
             me.activate(testMandator);
             logout();
             //check if login/logout works while active
             login(name, pwd);
             logout();
             login(TestUsers.SUPERVISOR);
             me.deactivate(testMandator);
             try {
                 EJBLookup.getAccountEngine().login(name, pwd, false);
                 Assert.fail("Expected an account of a deactivated mandator to not be able to log in.");
             } catch (FxLoginFailedException e) {
                 //expected
             }
             logout();
             login(TestUsers.SUPERVISOR);
             me.activate(testMandator);
             //check if login/logout works again
             login(name, pwd);
             logout();
         } finally {
             login(TestUsers.SUPERVISOR);
             EJBLookup.getAccountEngine().remove(accountId);
         }
     }
 
     /**
      * Test active/inactive mandators concerning queries
      *
      * @throws Exception on errors
      */
     public void activeQuery() throws Exception {
         me.activate(testMandator); //make sure the mandator is active
         FxType type = CacheAdmin.getEnvironment().getType(testType);
         FxContent co_act = ce.initialize(testType, testMandator, -1, type.getWorkflow().getSteps().get(0).getId(), FxLanguage.DEFAULT_ID);
         FxPK pk = ce.save(co_act);
         long org = new SqlQueryBuilder().type(testType).getResult().getRowCount();
         Assert.assertTrue(org > 0, "Expected at least one result!");
         me.deactivate(testMandator);
         Assert.assertEquals(new SqlQueryBuilder().type(testType).getResult().getRowCount(), 0, "Expected 0 results!");
         me.activate(testMandator);
         Assert.assertEquals(new SqlQueryBuilder().type(testType).getResult().getRowCount(), org, "Expected " + org + " results!");
         ce.remove(pk);
     }
 
     /**
      * Tests the MandatorEngine#changeName(long id, String name) method
      * @throws FxApplicationException on errors
      */
     public void changeNameTest() throws FxApplicationException {
         int mandatorId = me.create("TestMandatorName", false);
         Mandator mand = CacheAdmin.getEnvironment().getMandator(mandatorId);
         assertTrue(mand.getName().equals("TestMandatorName"));
         try {
             me.changeName(mandatorId, "TestMandatorNameNew");
         } catch (FxUpdateException e) {
             // this shouldn't happen
         }
         mand = CacheAdmin.getEnvironment().getMandator(mandatorId);
         assertEquals(mand.getName(), "TestMandatorNameNew");
 
         // clean up
         me.remove(mandatorId);
     }
 
     /**
      * This method tests both the MandatorEngine#assignMetaData(int mandatorId, long contentId)
      * as well as the #removeMetaData(int mandatorId) methods.
      * Above mentioned methods have not been implemented yet!
      */
     public void metaDataAssignment() throws FxApplicationException {
         // create a type and a content instance to which the mandator can be attached
         final String TEST_PROPERTY = "TEST_PROPERTY_" + RandomStringUtils.random(16, true, true);
        ACL defACL = CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId());
         FxPropertyEdit propEd = FxPropertyEdit.createNew(TEST_PROPERTY, new FxString("testprop"), new FxString("testprophint"),
                 FxMultiplicity.MULT_0_1, defACL, FxDataType.String1024);
         long assignmentId = ass.createProperty(testType, propEd.setAutoUniquePropertyName(true), "/");
 
         // create a content instance for the above property / type
         FxContent co = ce.initialize(testType);
         co.setValue("/" + TEST_PROPERTY, new FxString(false, "testicus"));
         FxPK contentPK = ce.save(co);
         int mandatorId = me.create("TestMandatorName", false);
 
         long contentId = ce.load(contentPK).getId();
         try { // assign
             me.assignMetaData(mandatorId, contentId);
         } catch (FxApplicationException e) {
             // this shouldn't happen
         }
 
         Mandator mand = CacheAdmin.getEnvironment().getMandator(mandatorId);
         assertTrue(mand.hasMetadata());
         assertEquals(mand.getMetadataId(), contentId);
 
         try { // remove
             me.removeMetaData(mandatorId);
         } catch (FxApplicationException e) {
             // this shouldn't happen
         }
 
         mand = CacheAdmin.getEnvironment().getMandator(mandatorId);
         assertFalse(mand.hasMetadata());
         assertEquals(mand.getMetadataId(), -1);
 
         // clean up
         ce.remove(contentPK);
         ass.removeAssignment(assignmentId);
         me.remove(mandatorId);
     }
 }
