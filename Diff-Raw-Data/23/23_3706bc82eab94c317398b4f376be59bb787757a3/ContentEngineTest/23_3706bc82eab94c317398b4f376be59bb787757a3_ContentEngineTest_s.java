 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.tests.embedded.persistence;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ACLEngine;
 import com.flexive.shared.interfaces.AssignmentEngine;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.TypeEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.ACLPermission;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.StepDefinition;
 import static com.flexive.tests.embedded.FxTestUtils.*;
 import com.flexive.tests.embedded.ScriptingTest;
 import com.flexive.tests.embedded.TestUsers;
 import org.apache.commons.lang.RandomStringUtils;
 import org.testng.Assert;
 import static org.testng.Assert.*;
 import static org.testng.Assert.assertEquals;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Tests for the ContentEngine
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class ContentEngineTest {
     private static final Log LOG = LogFactory.getLog(ContentEngineTest.class);
 
     private ContentEngine ce;
     private ACLEngine acl;
     private TypeEngine te;
     private AssignmentEngine ass;
     public static final String TEST_EN = "Test value in english";
     public static final String TEST_DE = "Test datensatz in deutsch mit \u00E4ml\u00E5ut te\u00DFt";
     public static final String TEST_FR = "My french is bad but testing special characters: ?`?$\u010E1\u00C6~\u0119\\/";
     public static final String TEST_IT = "If i knew italian this would be a test value in italian ;)";
 
     public static final String TEST_TYPE = "TEST_TYPE_" + RandomStringUtils.random(16, true, true);
     public static final String TEST_GROUP = "TEST_GROUP_" + RandomStringUtils.random(16, true, true);
     private static final String TYPE_ARTICLE = "__ArticleTest";
     private final static FxString DEFAULT_STRING = new FxString(true, "ABC");
 
 
     public static final String MULTI_TYPE = "MULTI_TYPE_" + RandomStringUtils.random(16, true, true);
     public static final String MULTI_GROUP = "MULTI_GROUP_" + RandomStringUtils.random(16, true, true);
     private final static int MIN_MULTIPLICITY = 4;
     private final static int MAX_MULTIPLICITY = 8;
 
     /**
      * setup...
      *
      * @throws Exception on errors
      */
     @BeforeClass(groups = {"ejb", "content", "contentconversion"})
     public void beforeClass() throws Exception {
         ce = EJBLookup.getContentEngine();
         acl = EJBLookup.getAclEngine();
         te = EJBLookup.getTypeEngine();
         ass = EJBLookup.getAssignmentEngine();
         login(TestUsers.SUPERVISOR);
     }
 
 
     @AfterClass(groups = {"ejb", "content", "contentconversion"}, dependsOnMethods = {"tearDownStructures"})
     public void afterClass() throws FxLogoutFailedException {
         logout();
     }
 
     @AfterClass(groups = {"ejb", "content", "contentconversion"})
     public void tearDownStructures() throws Exception {
         long typeId = CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
         ce.removeForType(typeId);
         te.remove(typeId);
         typeId = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE).getId();
         ce.removeForType(typeId);
         te.remove(typeId);
         //remove the test group
         ass.removeAssignment(CacheAdmin.getEnvironment().getAssignment("ROOT/" + TEST_GROUP).getId(), true, false);
     }
 
     /**
      * Setup testing structure.
      * <p/>
      * Hierarchy looks like this:
      * * TestProperty1 (String 255)[0..1]
      * * TestProperty2 (String 1024) [1..1]
      * * TestProperty3 (String 255) [0..5]
      * * TestProperty4 (String 255) [1..N]
      * * TestProperty5 (String 255) [0..1] default value: "ABC"
      * $ TestGroup1 [0..2]
      * $ * TestProperty1_1 (String 255) [0..1]
      * $ * TestProperty1_2 (String 255) [1..1]
      * $ * TestProperty1_3 (String 255) [1..N]
      * $ $ TestGroup1_1 [1..1]
      * $ $ * TestProperty1_1_1 (String 255) [0..1]
      * $ $ TestGroup1_2 [0..N]
      * $ $ * TestProperty1_2_1 (String 255) [1..1]
      * $ $ * TestProperty1_2_2 (String 255) [0..5]
      * $ $ $ TestGroup1_2_1 [1..2]
      * $ $ $ * TestProperty1_2_1_1 (String 255) [0..2]
      * $ $ TestGroup1_3 [1..2]
      * $ $ * TestProperty1_3_1 (String 255) [0..1]
      * * TestNumber (FxNumber) [0..2]
      * * TestNumberSL (FxNumber) [0..2] (Single language only)
      * * TestFloat (FxFloat) [0..2]
      *
      *
      * new type MULTI_GROUP looks like this:
      * * TestProperty101 (String 1024) [4..8]
      * * TestProperty102 (String 1024) [1..1]
      * * TestProperty103 (String 1024) [0..5] 
      * @throws Exception on errors
      */
     @BeforeClass(groups = {"ejb", "content", "contentconversion"}, dependsOnMethods = {"setupACL"})
     public void setupStructures() throws Exception {
         try {
             if (CacheAdmin.getEnvironment().getType(TEST_TYPE) != null)
                 return;
         } catch (FxRuntimeException e) {
             //ignore and create
         }
         ACL structACL = CacheAdmin.getEnvironment().getACL("Test ACL Structure 1");
         boolean createRootStuff = true;
         try {
             createRootStuff = CacheAdmin.getEnvironment().getGroup(TEST_TYPE) == null;
         } catch (FxRuntimeException e) {
             //ignore and create
         }
         if (createRootStuff) {
             FxString desc = new FxString("Test data structure");
             desc.setTranslation(FxLanguage.GERMAN, "Testdaten Strukturen");
             FxString hint = new FxString("Hint text ...");
             FxGroupEdit ge = FxGroupEdit.createNew(TEST_GROUP, desc, hint, true, new FxMultiplicity(0, 1));
             ass.createGroup(ge, "/");
             FxPropertyEdit pe = FxPropertyEdit.createNew("TestProperty1", desc, hint, true, new FxMultiplicity(0, 1),
                     true, structACL, FxDataType.String1024, null,
                     true, null, null, null).setMultiLang(true).setOverrideMultiLang(true);
             pe.setAutoUniquePropertyName(true);
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty2");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty3");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(0, 5));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty4");
             pe.setMultiplicity(new FxMultiplicity(1, FxMultiplicity.N));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty5");
             pe.setMultiplicity(FxMultiplicity.MULT_0_1);
             FxValue orgDefault = pe.getDefaultValue();
             pe.setDefaultValue(DEFAULT_STRING);
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setDefaultValue(orgDefault);
             ge.setName("TestGroup1");
             ge.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createGroup(ge, "/" + TEST_GROUP);
             pe.setName("TestProperty1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_2");
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_3");
             pe.setMultiplicity(new FxMultiplicity(1, FxMultiplicity.N));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             ge.setName("TestGroup1_1");
             ge.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_1");
             ge.setName("TestGroup1_2");
             ge.setMultiplicity(new FxMultiplicity(0, FxMultiplicity.N));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_2_1");
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             pe.setName("TestProperty1_2_2");
             pe.setMultiplicity(new FxMultiplicity(0, 5));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             ge.setName("TestGroup1_2_1");
             ge.setMultiplicity(new FxMultiplicity(1, 2));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             pe.setName("TestProperty1_2_1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2/TestGroup1_2_1");
             ge.setName("TestGroup1_3");
             ge.setMultiplicity(new FxMultiplicity(1, 2));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_3_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_3");
             pe.setName("TestNumber");
             pe.setDataType(FxDataType.Number);
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestNumberSL");
             pe.setDataType(FxDataType.Number);
             pe.setMultiLang(false);
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setMultiLang(true);
             pe.setName("TestFloat");
             pe.setDataType(FxDataType.Float);
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP);
 
             ge = FxGroupEdit.createNew(MULTI_GROUP, desc, hint, true, new FxMultiplicity(0, 1));
             ass.createGroup(ge, "/");
             pe = FxPropertyEdit.createNew("TestProperty101", desc, hint, true, new FxMultiplicity(MIN_MULTIPLICITY, MAX_MULTIPLICITY),
                     true, structACL, FxDataType.String1024, null,
                     true, null, null, null).setMultiLang(true).setOverrideMultiLang(true);
             pe.setAutoUniquePropertyName(true);
             ass.createProperty(pe, "/" + MULTI_GROUP);
             pe.setName("TestProperty102");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + MULTI_GROUP);
             pe.setName("TestProperty103");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(0, 5));
             ass.createProperty(pe, "/" + MULTI_GROUP);
 
             te.save(FxTypeEdit.createNew(MULTI_TYPE, new FxString("Test data multi"), CacheAdmin.getEnvironment().getACLs(ACLCategory.STRUCTURE).get(0), null));
             FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment("ROOT/" + MULTI_GROUP);
             FxGroupAssignmentEdit gae = FxGroupAssignmentEdit.createNew(ga, CacheAdmin.getEnvironment().getType(MULTI_TYPE), null, "/");
             ass.save(gae, true);
         }
         //create article te
         FxPropertyEdit pe = FxPropertyEdit.createNew("MyTitle", new FxString("Description"), new FxString("Hint"),
                 true, new FxMultiplicity(0, 1),
                 true, structACL, FxDataType.String1024, new FxString(FxString.EMPTY),
                 true, null, null, null).setAutoUniquePropertyName(true).setMultiLang(true).setOverrideMultiLang(true);
         long articleId = te.save(FxTypeEdit.createNew(TYPE_ARTICLE, new FxString("Article test type"), CacheAdmin.getEnvironment().getACLs(ACLCategory.STRUCTURE).get(0), null));
         ass.createProperty(articleId, pe, "/");
         pe.setName("Text");
         pe.setDataType(FxDataType.Text);
         pe.setMultiplicity(new FxMultiplicity(0, 2));
         ass.createProperty(articleId, pe, "/");
 
         long testDataId = te.save(FxTypeEdit.createNew(TEST_TYPE, new FxString("Test data"), CacheAdmin.getEnvironment().getACLs(ACLCategory.STRUCTURE).get(0), null));
         FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment("ROOT/" + TEST_GROUP);
         FxGroupAssignmentEdit gae = FxGroupAssignmentEdit.createNew(ga, CacheAdmin.getEnvironment().getType(TEST_TYPE), null, "/");
         ass.save(gae, true);
 
         try {
             if (ScriptingTest.loadScript != null) {
                 //only install scripts if scripting is being tested as well
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.loadScript.getId(), 0, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.removeScript.getId(), 0, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.loadScript.getId(), testDataId, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.removeScript.getId(), testDataId, true, true);
             }
         } catch (Exception ex) {
             //ignore since scripting might not be enabled for this test run
             ex.printStackTrace();
         }
     }
 
     /**
      * Setup ACL's needed for testing
      *
      * @throws Exception on errors
      */
     @BeforeClass(groups = {"ejb", "content", "contentconversion"}, dependsOnMethods = {"beforeClass"})
     public void setupACL() throws Exception {
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Content 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Content 1", new FxString("Test ACL Content 1"), Mandator.MANDATOR_FLEXIVE, "#00CCDD", "", ACLCategory.INSTANCE);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Structure 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Structure 1", new FxString("Test ACL Structure 1"), Mandator.MANDATOR_FLEXIVE, "#BBCCDD", "", ACLCategory.STRUCTURE);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Workflow 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Workflow 1", new FxString("Test ACL Workflow 1"), Mandator.MANDATOR_FLEXIVE, "#BB00DD", "", ACLCategory.WORKFLOW);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Article ACL");
         } catch (FxRuntimeException e) {
             acl.create("Article ACL", new FxString("ACL for articles"), Mandator.MANDATOR_FLEXIVE, "#00CC00", "", ACLCategory.INSTANCE);
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void getGroupData() throws FxApplicationException {
         FxContent test = ce.initialize(TEST_TYPE);
         final FxGroupData group = test.getGroupData("/TestGroup1");
         assertEquals(test.getGroupData(group.getAssignment().getId()), group);
     }
 
     @Test(groups = {"ejb", "content"})
     public void removeAddData() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assertTrue(testType != null);
         FxContent test = ce.initialize(testType.getId());
         assertTrue(test != null);
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
 
         assertTrue(test.getRootGroup().getCreateableChildren(true).size() == 6);
         assertTrue(!test.getRootGroup().isRemoveable());
         assertTrue(test.containsXPath("/TestGroup1"));
         assertTrue(test.getGroupData("/TestGroup1").isRemoveable());
         assertTrue(test.containsXPath("/TestGroup1/Testgroup1_1"));
         assertTrue(!test.getGroupData("/TestGroup1/TestGroup1_1").isRemoveable());
         assertTrue(test.getPropertyData("/TestProperty1").isRemoveable());
         assertTrue(!test.getPropertyData("/TestProperty2").isRemoveable());
         assertTrue(test.getPropertyData("/TestProperty3").isRemoveable());
         assertTrue(!test.getPropertyData("/TestProperty4").isRemoveable());
         test.remove("/TestGroup1/TestGroup1_2");
         try {
             test.getGroupData("/TestGroup1/TestGroup1_2");
             fail("/TestGroup1/TestGroup1_2 should no longer exist!");
         } catch (FxRuntimeException e) {
             if (!(e.getConverted() instanceof FxNotFoundException)) {
                 throw e;
             }
             //expected
         }
         List<String> cr = test.getGroupData("/TestGroup1").getCreateableChildren(true);
         assertTrue(cr.size() == 3);
         assertTrue(cr.get(0).equals("/TESTGROUP1[1]/TESTPROPERTY1_3[2]"));
         assertTrue(cr.get(1).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]"));
         assertTrue(cr.get(2).equals("/TESTGROUP1[1]/TESTGROUP1_3[2]"));
         cr = test.getGroupData("/TestGroup1").getCreateableChildren(false);
 //        for(String xp: cr)
 //            System.out.println("==cr=> "+xp);
         assertTrue(cr.size() == 1);
         assertTrue(cr.get(0).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]"));
 
         test.getGroupData("/TestGroup1").explode(false);
         assertTrue(test.getGroupData("/TestGroup1").getChildren().size() == 6);
         assertTrue(test.getGroupData("/TESTGROUP1[1]/TESTGROUP1_2[1]").getChildren().size() == 3);
         test.remove("/TESTGROUP1[1]/TESTGROUP1_2[1]");
         assertTrue(test.getGroupData("/TestGroup1").getCreateableChildren(false).size() == 1);
 
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[1]", FxData.POSITION_BOTTOM);
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[2]", FxData.POSITION_BOTTOM);
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[4]", FxData.POSITION_BOTTOM);
         try {
             test.getRootGroup().addEmptyChild("/TESTPROPERTY1[2]", FxData.POSITION_BOTTOM);
             fail("FxCreateException expected! max. multiplicity reached");
         } catch (FxRuntimeException e) {
             if (!(e.getConverted() instanceof FxInvalidParameterException)) {
                 throw e;
             }
             //expected
         }
         test.remove("/TestGroup1");
         try {
             test.getGroupData("/TestGroup1");
             fail("/TestGroup1 should no longer exist!");
         } catch (FxRuntimeException e) {
             if (!(e.getConverted() instanceof FxNotFoundException)) {
                 throw e;
             }
             //expected
         }
         test.remove("/TestNumber");
         try {
             test.getPropertyData("/TestNumber");
             fail("/TestNumber should no longer exist!");
         } catch (FxRuntimeException e) {
             if (!(e.getConverted() instanceof FxNotFoundException)) {
                 throw e;
             }
             //expected
         }
     }
 
     //  test for groups (with groups + props) with mult 2+
 
     @Test(groups = {"ejb", "content"})
     public void groupValidityTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
 
         FxContent test = ce.initialize(testType.getId());
         //fill all required properties
         FxString testValue = new FxString(FxLanguage.ENGLISH, TEST_EN);
         testValue.setTranslation(FxLanguage.GERMAN, TEST_DE);
         testValue.setTranslation(FxLanguage.FRENCH, TEST_FR);
         testValue.setTranslation(FxLanguage.ITALIAN, TEST_IT);
         test.setValue("/TestProperty2", testValue);
         test.setValue("/TestProperty4", testValue);
 
         test.checkValidity();
         test.setValue("/TESTGROUP1/TESTPROPERTY1_3", testValue);
         test.setValue("/TESTGROUP1/TESTPROPERTY1_2", testValue);
         test.setValue("/TESTGROUP1[1]/TESTGROUP1_2[1]/TESTPROPERTY1_2_1[1]", testValue);    // not optional
         test.setValue("/TESTGROUP1[1]/TESTGROUP1_2[1]/TESTPROPERTY1_2_2[1]", testValue);    // optional
 
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException ip) {
             fail("should be valid [" + ip + "]");
         }
         test.setValue("/TESTGROUP1[1]/TESTGROUP1_3[1]/TestProperty1_3_1", testValue);
 
         test.getRootGroup().removeEmptyEntries();
         for (int i = 2; i < 8; i++) {
             test.setValue("/TESTGROUP1[1]/TESTGROUP1_2["+ i +"]/TESTPROPERTY1_2_2[1]", testValue);
             try {
                 test.checkValidity();
                 fail ("should fail");
             } catch (FxInvalidParameterException ip) {
                 // OK
             }
             test.setValue("/TESTGROUP1[1]/TESTGROUP1_2["+ i + "]/TESTPROPERTY1_2_1[1]", testValue);
 
             try {
                 test.checkValidity();
             } catch (FxInvalidParameterException ip) {
                 fail("should be valid");
             }
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void contentComplex() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assertTrue(testType != null);
         //initialize tests start
         FxContent test = ce.initialize(testType.getId());
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
         assertTrue(test != null);
         int rootSize = 9 + CacheAdmin.getEnvironment().getSystemInternalRootPropertyAssignments().size();
         assertTrue(rootSize == test.getData("/").size(), "Root size expected " + rootSize + ", was " + test.getData("/").size());
 //        FxGroupData groot = test.getData("/").get(0).getParent();
         //basic sanity checks
 //        Assert.assertTrue(groot.isEmpty()); TODO: isEmpty means no sys internal properties!
         assertTrue(test.getData("/TestProperty1").get(0).getAssignmentMultiplicity().toString().equals("0..1"));
         assertTrue(1 == test.getData("/TestProperty1").get(0).getIndex());
         assertTrue(!(test.getData("/TestProperty1").get(0).mayCreateMore())); //should be at multiplicity 1
         assertTrue(!(test.getPropertyData("/TestProperty2").mayCreateMore())); //should be at multiplicity 1
         assertTrue(test.getPropertyData("/TestProperty3").mayCreateMore()); //max of 5
         assertTrue(test.getGroupData("/TestGroup1").mayCreateMore()); //max of 2
         assertTrue(1 == test.getGroupData("/TestGroup1").getCreateableElements()); //1 left to create
         assertTrue(1 == test.getGroupData("/TestGroup1").getRemoveableElements()); //1 left to remove
         assertTrue(6 == test.getData("/TestGroup1").size());
         assertTrue(0 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getCreateableElements()); //0 left
         assertTrue(1 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getRemoveableElements()); //0 left
         assertTrue(!(test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").mayCreateMore())); //0 left
         assertTrue(1 == test.getGroupData("/TestGroup1/TestGroup1_2").getIndex());
         FxPropertyData p = test.getPropertyData("/TestGroup1/TestGroup1_2/TestGroup1_2_1/TestProperty1_2_1_1");
         assertTrue(1 == p.getIndex());
         assertTrue(p.isEmpty());
         assertTrue(p.isProperty());
         assertTrue("TESTPROPERTY1_2_1_1".equals(p.getAlias()));
         assertTrue("TESTGROUP1_2_1".equals(p.getParent().getAlias()));
         assertTrue(p.getAssignmentMultiplicity().equals(new FxMultiplicity(0, 2)));
         assertTrue(4 == p.getIndices().length);
         //check required with empty values
         try {
             test.checkValidity();
             fail("checkValidity() succeeded when it should not!");
         } catch (FxInvalidParameterException e) {
             //ok
             String exMsgKey = e.getExceptionMessage().getKey();
             assertTrue("ex.content.required.missing".equals(exMsgKey) || "ex.content.required.missing.none".equals(exMsgKey));
         }
         //fill all required properties
         FxString testValue = new FxString(FxLanguage.ENGLISH, TEST_EN);
         testValue.setTranslation(FxLanguage.GERMAN, TEST_DE);
         testValue.setTranslation(FxLanguage.FRENCH, TEST_FR);
         testValue.setTranslation(FxLanguage.ITALIAN, TEST_IT);
         test.setValue("/TestProperty2", testValue);
         test.setValue("/TestProperty4", testValue);
         //check required with empty groups
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("checkValidity() did not succeed when it should!");
         }
         test.setValue("/TestGroup1[1]/TestProperty1_2", testValue);
         try {
             test.checkValidity();
             fail("checkValidity() succeeded but /TestGroup1/TestProperty1_3 is missing!");
         } catch (FxInvalidParameterException e) {
             //ok
             String exMsgKey = e.getExceptionMessage().getKey();
             assertTrue("ex.content.required.missing".equals(exMsgKey) || "ex.content.required.missing.none".equals(exMsgKey));
         }
         test.setValue("/TestGroup1[1]/TestProperty1_3", testValue);
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("checkValidity() did not succeed when it should!");
         }
         test.setValue("/TestGroup1/TestProperty1_2", testValue);
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("checkValidity() did not succeed when it should!");
         }
         FxPK pk = ce.save(test);
         ce.remove(pk);
         //test /TestGroup1[2]...
         FxGroupData gd = test.getGroupData("/TestGroup1");
         assertTrue(gd.mayCreateMore());
         assertTrue(1 == gd.getCreateableElements()); //1 more should be createable
         gd.createNew(FxData.POSITION_BOTTOM);
         assertTrue(test.getGroupData("/TestGroup1[2]").isEmpty());
         //should still be valid since [2] is empty
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("checkValidity() did not succeed when it should!");
         }
         test.setValue("/TestGroup1[2]/TestProperty1_2", testValue);
         test.setValue("/TestGroup1[2]/TestProperty1_3", testValue);
         pk = ce.save(test);
         FxContent testLoad = ce.load(pk);
         try {
             testLoad.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("save / load don't work! (saving needed groups with only empty properties)", e);
         }
         final String transIt = ((FxString) testLoad.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN);
         assertTrue(TEST_IT.equals(transIt), "Expected italian translation '" + TEST_IT + "', got: '" + transIt + "' for pk " + pk);
         ce.remove(pk);
         pk = ce.save(test);
         FxContent testLoad2 = ce.load(pk);
         try {
             testLoad2.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("save / load don't work! (saving needed groups with only empty properties)", e);
         }
         FxNumber number = new FxNumber(true, FxLanguage.GERMAN, 42);
         number.setTranslation(FxLanguage.ENGLISH, 43);
         testLoad2.setValue("/TestNumber", number);
         FxNumber numberSL = new FxNumber(false, FxLanguage.GERMAN, 12);
         assertTrue(numberSL.getDefaultLanguage() == FxLanguage.SYSTEM_ID);
         assertTrue(12 == numberSL.getTranslation(FxLanguage.FRENCH));
         numberSL.setTranslation(FxLanguage.ITALIAN, 13);
         assertTrue(13 == numberSL.getTranslation(FxLanguage.FRENCH));
         testLoad2.setValue("/TestNumberSL", numberSL);
         FxFloat fxFloat = new FxFloat(true, FxLanguage.GERMAN, 42.42f);
         fxFloat.setTranslation(FxLanguage.ENGLISH, 43.43f);
         testLoad2.setValue("/TestFloat", fxFloat);
         assertEquals(42, (int)((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42 for german, before save)");
         assertEquals(43, (int)((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43, before save)");
         assertTrue(testLoad2.getPropertyData("/TestNumber").getValue().hasDefaultLanguage());
         assertEquals(42.42f, ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42.42f for german, before save)");
         assertEquals(43.43f, ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43.43f, before save)");
         assertTrue(testLoad2.getPropertyData("/TestFloat").getValue().hasDefaultLanguage());
         FxPK saved = ce.save(testLoad2);
         FxContent testLoad3 = ce.load(saved);
         assertEquals(42, (int)((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42 for german, after load)");
         assertEquals(43, (int)((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43, after load)");
         assertEquals(13, (int)((FxNumber) testLoad3.getPropertyData("/TestNumberSL").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 13, after load)");
         assertTrue(!testLoad3.getPropertyData("/TestNumberSL").getValue().isMultiLanguage(), "Single language value expected");
         assertTrue(testLoad3.getPropertyData("/TestNumber").getValue().hasDefaultLanguage(), "Missing default language after load");
         assertEquals(42.42f, ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42.42f for german, before save)");
         assertEquals(43.43f, ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43.43f, before save)");
         assertTrue(testLoad3.getPropertyData("/TestFloat").getValue().hasDefaultLanguage(), "Missing default language after load");
         final String transIt2 = ((FxString) testLoad3.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN);
         assertTrue(TEST_IT.equals(transIt2), "Expected italian translation '" + TEST_IT + "', got: '" + transIt2 + "'");
         assertTrue(1 == ce.removeForType(testType.getId()), "Only one instance should be removed!");
         assertTrue(0 == ce.removeForType(testType.getId()), "No instance should be left to remove!");
 
         // /TestNumberSL has a max. multiplicity of 2
         //since FX-473 null should be returned if not set
         Assert.assertNull(testLoad3.getValue("/TestNumberSL[2]"));
         try {
             //should throw unchecked exception since multiplicity 3 is out of range
             testLoad3.getValue("/TestNumberSL[3]");
             fail("Accessing an invalid XPath should have failed. Multiplicity out of range.");
         } catch (FxRuntimeException re) {
             //expected
         }
         try {
             //should throw unchecked exception since multiplicity 3 is out of range
             testLoad3.getValue("/TestNumberSLXXX[1]");
             fail("Accessing an invalid XPath should have failed.");
         } catch (FxRuntimeException re) {
             //expected
         }
         ce.initialize(testType.getId()).randomize();
     }
 
     @Test(groups = {"ejb", "content"})
     public void getValues() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assertTrue(testType != null);
         FxContent test = ce.initialize(testType.getId());
         final String XP = "/TestProperty3";
         assertEquals(test.getValues(XP).size(), 1); //initialized with 1 empty entry
         test.setValue(XP + "[1]", new FxString(true, "1"));
         assertEquals(test.getValues(XP).size(), 1);
         test.setValue(XP + "[2]", new FxString(true, "2"));
         assertEquals(test.getValues(XP).size(), 2);
         test.setValue(XP + "[3]", new FxString(true, "3"));
         assertEquals(test.getValues(XP).size(), 3);
         List<FxValue> values = test.getValues(XP);
         assertEquals(values.get(0).getBestTranslation(), "1");
         assertEquals(values.get(1).getBestTranslation(), "2");
         assertEquals(values.get(2).getBestTranslation(), "3");
         test.getPropertyData(XP + "[2]").setPos(3); //1,2,3 -> 1,3,2
         values = test.getValues(XP);
         //positions should be sorted
         assertEquals(values.get(0).getBestTranslation(), "1");
         assertEquals(values.get(1).getBestTranslation(), "2");
         assertEquals(values.get(2).getBestTranslation(), "3");
     }
 
     @Test(groups = {"ejb", "content"})
     public void setValue() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent test = ce.initialize(testType.getId());
         //set required properties
         test.setValue("/TestProperty2", new FxString(true, "Test2"));
         test.setValue("/TestProperty4", new FxString(true, "Test4"));
         FxPK pk = test.save().getPk();
         try {
             FxContent loaded = ce.load(pk);
             //test setting a value that is not present in the loaded content
             assertFalse(loaded.containsValue("/TestProperty1"));
             assertFalse(loaded.containsXPath("/TestProperty1"));
             loaded.setValue(
                     testType.getPropertyAssignment("/TestProperty1").getXPath(),
                     "Test1"
             );
             loaded.save();
             loaded = ce.load(pk);
             Assert.assertEquals(loaded.getValue("/TestProperty1").getBestTranslation(), "Test1");
         } finally {
             ce.remove(pk);
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void contentInitialize() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
             FxContent test = ce.initialize(article.getId());
             test.setAclId(CacheAdmin.getEnvironment().getACL("Article ACL").getId());
             test.getData("/");
             test.getData("/MYTITLE");
             test.getData("/TEXT");
             //test if shared message loading works
             FxNoAccess noAccess = new FxNoAccess(getUserTicket(), new FxString("test"));
             if (getUserTicket().getLanguage().getId() == FxLanguage.ENGLISH)
                 assertTrue("Access denied!".equals(noAccess.getDefaultTranslation()), "Shared message loading failed! Expected [Access denied!] got: [" + noAccess.getDefaultTranslation() + "]");
             else if (getUserTicket().getLanguage().getId() == FxLanguage.GERMAN)
                 assertTrue("Zugriff verweigert!".equals(noAccess.getDefaultTranslation()), "Shared message loading failed!");
         } catch (FxApplicationException e) {
             fail(e.getMessage());
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void defaultMultiplicity() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
 
             FxPropertyAssignmentEdit pe = new FxPropertyAssignmentEdit((FxPropertyAssignment) article.getAssignment("/TEXT"));
             pe.setDefaultMultiplicity(2);
             assertTrue(2 == pe.getDefaultMultiplicity(), "Wrong default multiplicity");
             ass.save(pe, false);
 
             FxContent test = ce.initialize(article.getId());
             test.getData("/TEXT[1]");
             test.getData("/TEXT[2]");
             pe.setDefaultMultiplicity(1);
             ass.save(pe, false);
             test = ce.initialize(article.getId());
             test.getData("/TEXT[1]");
             try {
                 test.getData("/TEXT[2]");
                 fail("No /TEXT[2] should exist!");
             } catch (Exception e) {
                 //ok
             }
         } catch (FxApplicationException e) {
             fail(e.getMessage());
         }
     }
 
 
     @Test(groups = {"ejb", "content"})
     public void contentCreate() throws Exception {
         FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
         FxContent test = ce.initialize(article.getId());
         test.setAclId(CacheAdmin.getEnvironment().getACL("Article ACL").getId());
         FxString title = new FxString(FxLanguage.ENGLISH, "Title english");
         title.setTranslation(FxLanguage.GERMAN, "Titel deutsch");
         FxString text = new FxString(FxLanguage.ENGLISH, "Text english1");
         text.setTranslation(FxLanguage.GERMAN, "Text deutsch1");
         test.setValue("/MYTITLE", title);
         test.setValue("/TEXT", text);
         int titlePos = test.getPropertyData("/MYTITLE").getPos();
         test.move("/MYTITLE", 1); //move title 1 position down
         FxPropertyData pText = test.getPropertyData("/TEXT");
         assertTrue("Text english1".equals(((FxString) test.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH)));
         if (pText.mayCreateMore()) {
             FxPropertyData pText2 = (FxPropertyData) pText.createNew(FxData.POSITION_BOTTOM);
             pText2.setValue(new FxString(FxLanguage.ENGLISH, "Text english2"));
             assertTrue("Text english2".equals(((FxString) test.getPropertyData("/TEXT[2]").getValue()).getTranslation(FxLanguage.ENGLISH)));
         }
         //            for( int i=0; i<100;i++)
         FxPK pk = test.save().getPk();
         FxContent comp = ce.load(pk);
         assertTrue(comp != null);
         assertTrue(comp.getPk().getId() == pk.getId(), "Id failed");
         assertTrue(comp.getPk().getId() == comp.getId(), "Id of content not equal the Id of contents pk");
         assertTrue(comp.matchesPk(pk), "matchesPk failed");
         assertTrue(comp.matchesPk(new FxPK(pk.getId(), FxPK.MAX)), "matchesPk for max version failed");
         assertTrue(1 == comp.getPk().getVersion(), "Version is not 1");
         assertTrue(comp.getStepId() == test.getStepId(), "Step failed");
         assertEquals(comp.getAclIds(), test.getAclIds(), "ACL failed");
         assertTrue(comp.isMaxVersion(), "MaxVersion failed");
         assertTrue(comp.isLiveVersion() == article.getWorkflow().getSteps().get(0).isLiveStep(), "LiveVersion failed. Expected:" + article.getWorkflow().getSteps().get(0).isLiveStep() + " Got:" + comp.isLiveVersion());
         assertTrue(comp.getMainLanguage() == FxLanguage.ENGLISH, "MainLang failed");
         assertTrue(comp.getLifeCycleInfo().getCreatorId() == getUserTicket().getUserId(), "CreatedBy failed");
         assertTrue("Text english1".equals(((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH)), "Expected 'Text english1', got '" + ((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH) + "'");
         //test result of move
         assertTrue(titlePos == comp.getPropertyData("/TEXT").getPos(), "Text[1] position should be " + (titlePos) + " but is " + comp.getPropertyData("/TEXT").getPos());
         assertTrue(titlePos + 1 == comp.getPropertyData("/MYTITLE[1]").getPos());
         assertTrue(titlePos + 2 == comp.getPropertyData("/TEXT[2]").getPos());
         FxPK pk2 = ce.createNewVersion(comp);
         assertTrue(2 == pk2.getVersion());
         comp.setValue("/TEXT", new FxString(FxLanguage.GERMAN, "Different text"));
         FxPK pk3 = ce.createNewVersion(comp);
         assertTrue(3 == pk3.getVersion());
         FxContentVersionInfo cvi = ce.getContentVersionInfo(pk3);
         assertTrue(3 == cvi.getLastModifiedVersion());
         assertTrue(3 == cvi.getLiveVersion());
         assertTrue(1 == cvi.getMinVersion());
 
         FxContentContainer cc = ce.loadContainer(cvi.getId());
         assertEquals(cc.getVersionInfo().getLastModifiedVersion(), cvi.getLastModifiedVersion());
         assertEquals(cc.getVersionInfo().getLiveVersion(), cvi.getLiveVersion());
         assertEquals(cc.getVersionInfo().getMinVersion(), cvi.getMinVersion());
         assertEquals(cc.getVersion(2), ce.load(pk2));
         assertEquals(cc.getVersion(3), ce.load(pk3));
         Assert.assertNotSame(cc.getVersion(1), cc.getVersion(2));
         assertTrue(FxDelta.processDelta(cc.getVersion(1), cc.getVersion(2)).isOnlyInternalPropertyChanges());
         Assert.assertNotSame(cc.getVersion(1), cc.getVersion(3));
         assertFalse(FxDelta.processDelta(cc.getVersion(2), cc.getVersion(3)).isOnlyInternalPropertyChanges());
 
         ce.removeVersion(new FxPK(pk.getId(), 1));
         cvi = ce.getContentVersionInfo(pk3);
         assertTrue(2 == cvi.getMinVersion());
         assertTrue(3 == cvi.getMaxVersion());
         ce.removeVersion(new FxPK(pk.getId(), 3));
         cvi = ce.getContentVersionInfo(pk3);
         assertTrue(2 == cvi.getMinVersion());
         assertTrue(2 == cvi.getMaxVersion());
         assertTrue(!cvi.hasLiveVersion());
         ce.removeVersion(new FxPK(pk.getId(), 2));
         try {
             ce.getContentVersionInfo(new FxPK(pk.getId()));
             fail("VersionInfo available for a removed instance!");
         } catch (FxApplicationException e) {
             //ok
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void binaryUploadTest() throws Exception {
         //        File testFile = new File("/home/mplesser/install/java/testng-5.1.zip");
         File testFile = new File("test.file");
         if (!testFile.exists())
             testFile = new File("build/ui/flexive.war");
         if (!testFile.exists())
             return;
         FileInputStream fis = new FileInputStream(testFile);
         String handle = FxStreamUtils.uploadBinary(testFile.length(), fis).getHandle();
         System.out.println("==Client done== Handle received: " + handle);
         fis.close();
     }
 
     @Test(groups = {"ejb", "content"})
     public void typeValidityTest() throws Exception {
         FxType t = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assertTrue(t.isXPathValid("/", false), "Root group should be valid for groups");
        assertTrue(!t.isXPathValid("/", true), "Root group should be invalid for properties");
         assertTrue(t.isXPathValid("/TestProperty1", true));
        assertTrue(!t.isXPathValid("/TestProperty1", false));
         assertTrue(t.isXPathValid(TEST_TYPE + "/TestProperty1", true));
        assertTrue(!t.isXPathValid(TEST_TYPE + "123/TestProperty1", true));
        assertTrue(!t.isXPathValid("WrongType/TestProperty1", true));
        assertTrue(!t.isXPathValid(TEST_TYPE + "/TestProperty1/Dummy", true));
         assertTrue(t.isXPathValid("/TestProperty1[1]", true));
        assertTrue(!t.isXPathValid("/TestProperty1[2]", true));
         assertTrue(t.isXPathValid("/TestGroup1[2]", false));
         assertTrue(t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", true));
        assertTrue(!t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", false));
         assertTrue(t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", true));
        assertTrue(!t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", false));
        assertTrue(!t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[6]", true));
     }
 
     @Test(groups = {"ejb", "content"})
     public void setValueTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent test = ce.initialize(testType.getId());
         final String TEST_VALUE_EN = "Hello world";
         final String TEST_VALUE_DE = "Hallo welt";
         final String TEST_XPATH = "/TestGroup1[2]/TestGroup1_2[3]/TestProperty1_2_2[4]";
 
         FxString testValue = new FxString(TEST_VALUE_EN);
         test.setValue(TEST_XPATH, testValue);
         assertTrue(test.getValue(TEST_XPATH).equals(testValue));
         test.setValue(TEST_XPATH, TEST_VALUE_EN);
         test.setValue(TEST_XPATH, FxLanguage.GERMAN, TEST_VALUE_DE);
         assertTrue(test.getValue(TEST_XPATH).getTranslation(FxLanguage.ENGLISH).equals(TEST_VALUE_EN));
         assertTrue(test.getValue(TEST_XPATH).getTranslation(FxLanguage.GERMAN).equals(TEST_VALUE_DE));
         assertTrue(test.getValue(TEST_XPATH).getDefaultTranslation().equals(TEST_VALUE_EN));
     }
 
     @Test(groups = {"ejb", "content"})
     public void deltaTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent org = ce.initialize(testType.getId());
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         //set required properties to allow saving ..
         org.setValue("/TestProperty2[1]", testValue1);
         org.setValue("/TestProperty4[1]", testValue1);
 
         //test properties
         org.setValue("/TestProperty3[1]", testValue1);
         org.setValue("/TestProperty3[2]", testValue2);
         org.setValue("/TestProperty3[3]", testValue1);
 
         FxPK pk = ce.save(org);
         try {
             org = ce.load(pk);
             FxContent test = ce.load(pk);
             FxDelta d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assertTrue(d.getAdds().size() == 0, "Expected no adds, but got " + d.getAdds().size());
             assertTrue(d.getRemoves().size() == 0, "Expected no deletes, but got " + d.getRemoves().size());
             assertTrue(d.getUpdates().size() == 0, "Expected no updates, but got " + d.getUpdates().size());
 
             test.getValue("/TestProperty3[2]").setValueData(9999);
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assertTrue(d.getAdds().size() == 0, "Expected no adds, but got " + d.getAdds().size());
             assertTrue(d.getRemoves().size() == 0, "Expected no deletes, but got " + d.getRemoves().size());
             assertTrue(d.getUpdates().size() == 1, "Expected one update (value data), but got " + d.getUpdates().size());
 
             test.remove("/TestProperty3[2]");
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assertTrue(d.getAdds().size() == 0, "Expected no adds, but got " + d.getAdds().size());
             assertTrue(d.getRemoves().size() == 1, "Expected 1 deletes, but got " + d.getRemoves().size());
             assertTrue(d.getUpdates().size() == 1, "Expected 1 updates, but got " + d.getUpdates().size());
             assertTrue(d.getRemoves().get(0).getXPath().equals("/TESTPROPERTY3[3]"), "Expected /TESTPROPERTY3[3] but got: " + d.getRemoves().get(0).getXPath());
             assertTrue(d.getUpdates().get(0).getXPath().equals("/TESTPROPERTY3[2]"), "Expected /TESTPROPERTY3[2] but got: " + d.getUpdates().get(0).getXPath());
 
             test = ce.load(pk);
             test.setValue("/TestGroup1/TestProperty1_2", testValue1);
             test.setValue("/TestGroup1/TestProperty1_3", testValue1);
             test.getGroupData("/TestGroup1").removeEmptyEntries();
             test.getGroupData("/TestGroup1").compactPositions(true);
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assertTrue(d.changes(), "Expected some changes");
             assertTrue(d.getAdds().size() == 5, "Expected 5 (3 groups + 2 properties) adds but got " + d.getAdds().size());
             assertTrue(d.getRemoves().size() == 0, "Expected 0 deletes but got " + d.getRemoves().size());
             assertTrue(d.getUpdates().size() == 0, "Expected 0 updates but got " + d.getUpdates().size());
         } finally {
             ce.remove(pk);
         }
     }
 
     /**
      * Test compacting and removing empty entries
      *
      * @throws Exception on errors
      */
     @Test(groups = {"ejb", "content"})
     public void compactTest() throws Exception {
         FxContent org = ce.initialize(TEST_TYPE);
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         FxString testValue3 = new FxString("Hello world3");
         org.setValue("/TestProperty2[1]", testValue3);
         org.setValue("/TestProperty4[1]", testValue1);
         org.setValue("/TestProperty4[2]", testValue2);
         org.getValue("/TestProperty4[1]").setEmpty();
         org.setValue("/TestGroup1/TestProperty1_3[4]", testValue3);
         org.getRootGroup().removeEmptyEntries();
         org.getRootGroup().compact();
         assertEquals(org.getValue("/TestProperty4[1]"), testValue2);
         assertFalse(org.containsValue("/TestProperty4[2]"));
         assertEquals(org.getValue("/TestGroup1/TestProperty1_3[1]"), testValue3);
         assertFalse(org.containsValue("/TestGroup1/TestProperty1_3[4]"));
     }
 
     /**
       * Test adding lots of empty entries and let them remove and validiate the result
       *
       * @throws Exception on errors
       */
      @Test(groups = {"ejb", "content"})
     public void largeTest() throws Exception {
         FxContent org = ce.initialize(TEST_TYPE);
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         boolean doOld = false;
         long t1,t2,t3;
         org.setValue("/TestProperty4[1]", testValue2);
         org.setValue("/TestProperty2[1]", testValue2);
         org.setValue("/TestGroup1/TestProperty1_2[1]", testValue2);
         org.setValue("/TestGroup1/TestProperty1_3[1]", testValue2);
         for (int max : new int[] {600,800,1000}) {
             String xpathPrefix = "/TestProperty4";
             t1 = System.nanoTime();
             // first insert test data into root group
             for (int i = 2; i <= max+1; i++) {
                 String curXPath = String.format(xpathPrefix + "[%d]", i);
                 org.setValue(curXPath, testValue1);
                 org.getValue(curXPath).setEmpty();
             }
             t2 = System.nanoTime();
             org.getRootGroup().removeEmptyEntries();
             t3 = System.nanoTime();
             double removeTime = (t3 - t2) * 1E-6;
             System.out.println(String.format("inserting %s %4d empty elements into root group took %7.2f ms and remove took %7.2f ms [%6.5f ms / remove]", doOld?"OLD":"", max, (t2-t1)*1E-6, removeTime, removeTime/max));
             // test if removed the empty elements from root
             assertEquals(org.getRootGroup().getChildren().size(), 23);
             // it must not only remove the empty elements it also must be valid
             org.checkValidity();
             xpathPrefix = "/TestGroup1/TestProperty1_3";
 
             t1 = System.nanoTime();
             for (int i = 2; i <= max+1; i++) {
                 String curXPath = String.format(xpathPrefix + "[%d]", i);
                 org.setValue(curXPath, testValue1);
                 org.getValue(curXPath).setEmpty();
             }
             t2 = System.nanoTime();
             org.getRootGroup().removeEmptyEntries();
             t3 = System.nanoTime();
             removeTime = (t3 - t2) * 1E-6;
             System.out.println(String.format("inserting %s %4d empty elements into a group took    %7.2f ms and remove took %7.2f ms [%6.5f ms / remove]", doOld?"OLD":"", max, (t2-t1)*1E-6, removeTime, removeTime/max));
             assertEquals(org.getRootGroup().getChildren().size(), 23);
             org.checkValidity();
         }
     }
 
     /**
       * Test the minimum multiplicity
       *
       * @throws Exception on errors
       */
      @Test(groups = {"ejb", "content"})
     public void minMultiplicityTest() throws Exception {
         FxContent tmp = ce.initialize(MULTI_TYPE);
         FxString testValue1 = new FxString("Hello world1");
         try {
             tmp.checkValidity();
             fail("Type has 2 required fields!");
         } catch (FxInvalidParameterException e) {
             // OK
         }
         tmp.setValue("/TestProperty102",testValue1);
         try {
             tmp.checkValidity();
             fail("Type has required fields left!");
         } catch (FxInvalidParameterException e) {
             // OK
         }
         // testing multiplicity 4..8 
         String xpathPrefix = "/TestProperty101";
         for (int i = 1; i < MAX_MULTIPLICITY; i++) {
             String curXPath = String.format(xpathPrefix + "[%d]", i);
             tmp.setValue(curXPath, testValue1);
             try {
                 tmp.checkValidity();
                 if (i < MIN_MULTIPLICITY)
                     fail("Type has required fields left!");
             } catch (FxInvalidParameterException e) {
                 if (i >= MIN_MULTIPLICITY)
                     fail("should have passed!");
             }
         }
     }
 
     /**
      * Check if inserting same xpaths to the same pos is done correctly
      *
      * @throws Exception on errors
      */
     @Test(groups = {"ejb", "content"})
     public void addChildOrderTest() throws Exception {
         FxContent tmp = ce.initialize(MULTI_TYPE);
         FxGroupData data = tmp.getRootGroup();
 
         int num1 = data.getChildren().size();
 
         data.addEmptyChild("/TESTPROPERTY101[5]", 30);
         data.addEmptyChild("/TESTPROPERTY101[6]", 30);
         data.addEmptyChild("/TESTPROPERTY101[7]", 33);
         data.addEmptyChild("/TESTPROPERTY101[8]", 33);
 
         int num2 = data.getChildren().size();
 
         System.out.println((num2 - num1) + " created...");
         assertEquals(num2-num1, 4, "It should be 4 elements created, but there was "  + (num2- num1) + " created.");
         int lastPos = -1;
         for (FxData tmpData : data.getChildren()) {
             if (lastPos > tmpData.getPos())
                 fail("The children must be added orderd position even if position is the same");
             else
                 lastPos = tmpData.getPos();
         }
     }
 
     /**
      * Check default value handling
      *
      * @throws Exception on errors
      */
     @Test(groups = {"ejb", "content"})
     public void defaultValueTest() throws Exception {
         final String defaultXPath = "/TestProperty5";
         final String req1 = "/TestProperty2";
         final String req2 = "/TestProperty4";
         final FxString tmpValue = new FxString("xxxx");
 
         FxPK pk = null;
         try {
             assertEquals(
                     ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath)).getDefaultValue(),
                     DEFAULT_STRING);
             assertEquals(
                     CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath).getDefaultMultiplicity(),
                     1);
             FxContent c = ce.initialize(TEST_TYPE);
             assertEquals(c.getValue(defaultXPath), DEFAULT_STRING);
             c.setValue(req1, tmpValue);
             c.setValue(req2, tmpValue);
             pk = ce.save(c);
             FxContent test = ce.load(pk);
             assertEquals(test.getValue(defaultXPath), DEFAULT_STRING);
             test.setValue(defaultXPath, tmpValue);
             assertEquals(test.getValue(defaultXPath), tmpValue);
             ce.save(test);
             test = ce.load(test.getPk());
             assertEquals(test.getValue(defaultXPath), tmpValue, "Default value should have been overwritten.");
             test.remove(defaultXPath);
             assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (before save)");
             ce.save(test);
             test = ce.load(test.getPk());
             assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (after save with a default multiplicity of 1)");
 
             FxPropertyAssignment pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             ass.save(pa.asEditable().setDefaultMultiplicity(0), false);
             pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             assertEquals(pa.getDefaultMultiplicity(), 0);
             test = ce.load(test.getPk());
             assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (after save with a default multiplicity of 0)");
         } finally {
             if (pk != null)
                 ce.remove(pk);
             FxPropertyAssignment pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             //reset the def. multiplicity to 1
             ass.save(pa.asEditable().setDefaultMultiplicity(1), false);
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void removeXPathTest() throws FxApplicationException {
         FxContent test = ce.initialize(TEST_TYPE);
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         FxString testValue3 = new FxString("Hello world3");
 
         //test with property
         test.setValue("/TestProperty3[1]", testValue1);
         test.setValue("/TestProperty3[2]", testValue2);
         test.setValue("/TestProperty3[3]", testValue3);
         test.remove("/TestProperty3[2]");
         assertFalse(test.containsValue("/TestProperty3[3]"));
         assertEquals(test.getValue("/TestProperty3[2]"), testValue3, "Propery gap should have been closed and [3] is now [2]");
         test.remove("/TestProperty3");
         assertFalse(test.containsValue("/TestProperty4[3]"));
 
         //test with group
         test.setValue("/TestGroup1/TestGroup1_2[1]/TestProperty1_2_1[1]", testValue1);
         test.setValue("/TestGroup1/TestGroup1_2[2]/TestProperty1_2_1[1]", testValue2);
         test.setValue("/TestGroup1/TestGroup1_2[3]/TestProperty1_2_1[1]", testValue3);
         test.remove("/TestGroup1/TestGroup1_2[2]");
         assertFalse(test.containsValue("/TestGroup1/TestGroup1_2[3]/TestProperty1_2_1[1]"));
         assertEquals(test.getValue("/TestGroup1/TestGroup1_2[2]/TestProperty1_2_1[1]"), testValue3, "Group gap should have been closed and [3] is now [2]");
         test.remove("/TestGroup1/TestGroup1_2");
         assertFalse(test.containsValue("/TestGroup1/TestGroup1_2[1]/TestProperty1_2_1[1]"));
     }
 
     /**
      * Test the maxLength property f. contents
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content"})
     public void maxLengthTest() throws FxApplicationException {
         //  if already failing (we could not save) don't remove!
         boolean failing = false;
         long typeId = te.save(FxTypeEdit.createNew("MAXLENGTHTEST"));
         FxPropertyEdit ped = FxPropertyEdit.createNew("LENGTHPROP1", new FxString(true, "LENGTHPROP1"), new FxString(true, ""), FxMultiplicity.MULT_0_1,
                 CacheAdmin.getEnvironment().getACL("Default Structure ACL"), FxDataType.String1024);
         ped.setMaxLength(3);
         ass.createProperty(typeId, ped, "/");
         ped = FxPropertyEdit.createNew("LENGTHPROP2", new FxString(true, "LENGTHPROP2"), new FxString(true, ""), FxMultiplicity.MULT_0_1,
                 CacheAdmin.getEnvironment().getACL("Default Structure ACL"), FxDataType.String1024);
         ped.setOverrideMaxLength(true);
         ass.createProperty(typeId, ped, "/");
 
         assertEquals(CacheAdmin.getEnvironment().getProperty("LENGTHPROP2").getMaxLength(), -1); // "no" length restriction
 
         try {
             FxContent c = ce.initialize(typeId);
             c.setValue("/LENGTHPROP1", new FxString(false, "1234"));
             try {
                 ce.save(c);
                 failing = true;
                 fail("Setting a content String with length = 4 for a property w/ maxLength = 3 should have failed");
             } catch (FxApplicationException e) {
                 // expected
             }
 
             ped = (CacheAdmin.getEnvironment().getProperty("LENGTHPROP1")).asEditable();
             ped.setMaxLength(-1);
             ass.save(ped);
 
             c.setValue("/LENGTHPROP1", new FxString(false, "1234"));
             assertEquals(c.getValue("/LENGTHPROP1").toString(), "1234");
 
             c.setValue("/LENGTHPROP2", new FxString(false, "1234"));
             assertEquals(c.getValue("/LENGTHPROP2").toString(), "1234");
 
         } finally {
             if (!failing)
             te.remove(typeId);
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void removeAssignedACLTest() throws FxApplicationException {
         FxContent test = ce.initialize(FxType.CONTACTDATA);
         final FxEnvironment env = CacheAdmin.getEnvironment();
         final long defaultInstanceAclId = env.getDefaultACL(ACLCategory.INSTANCE).getId();
         test.setAclIds(Arrays.asList(
                 defaultInstanceAclId,
                 ACL.ACL_CONTACTDATA
         ));
         test = test.save();
         try {
             assertEquals(test.getAclIds(), Arrays.asList(defaultInstanceAclId, ACL.ACL_CONTACTDATA));
             // remove second ACL 
             test.setAclId(defaultInstanceAclId);
             test = test.save();
             assertEquals(test.getAclIds(), Arrays.asList(defaultInstanceAclId));
         } finally {
             ce.remove(test.getPk());
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void nullAclAssignmentTest() throws FxApplicationException {
         FxContent test = ce.initialize(FxType.CONTACTDATA);
         try {
             test.setAclId(ACL.NULL_ACL_ID);
             fail("Null ACL cannot be assigned.");
         } catch (FxRuntimeException e) {
             // pass
         }
         try {
             test.setAclIds(Arrays.asList(ACL.NULL_ACL_ID));
             fail("Null ACL cannot be assigned.");
         } catch (FxRuntimeException e) {
             // pass
         }
         try {
             test.setAclIds(Arrays.<Long>asList());
             fail("At least one ACL must be assigned.");
         } catch (FxRuntimeException e) {
             // pass
         }
         try {
             test.setAclIds(Arrays.asList(ACL.NULL_ACL_ID, ACL.ACL_CONTACTDATA));
             fail("Null ACL cannot be assigned.");
         } catch (FxRuntimeException e) {
             // pass
         }
         test.setAclIds(Arrays.asList(ACL.ACL_CONTACTDATA));
     }
     
     @Test(groups = {"ejb", "content"})
     public void removeLiveVersionTest_FX807() throws FxApplicationException {
 
         // create a content in live version, attach it to the root node
         FxContent co = EJBLookup.getContentEngine().initialize(FxType.CONTACTDATA);
         co.setStepByDefinition(StepDefinition.LIVE_STEP_ID);
         final FxPK pk = EJBLookup.getContentEngine().save(co);
         final long nodeId = EJBLookup.getTreeEngine().save(
                 FxTreeNodeEdit.createNew("FX807")
                 .setParentNodeId(FxTreeNode.ROOT_NODE)
                 .setReference(pk)
         );
 
         boolean success = false;
         try {
             assertEquals(EJBLookup.getTreeEngine().getIdByPath(FxTreeMode.Edit, "/FX807"), nodeId, "Node not found: " + nodeId);
             
             // put the content in the edit version
             co = EJBLookup.getContentEngine().load(pk);
             co.setStepByDefinition(StepDefinition.EDIT_STEP_ID);
             EJBLookup.getContentEngine().save(co);
 
             // should be still here
             assertEquals(EJBLookup.getTreeEngine().getIdByPath(FxTreeMode.Edit, "/FX807"), nodeId, 
                     "Node removed when setting edit version: " + nodeId);
             success = true;
         } finally {
             // remove node and content
             try {
                 EJBLookup.getTreeEngine().remove(FxTreeMode.Edit, nodeId, FxTreeRemoveOp.Remove, false);
             } catch (FxApplicationException e) {
                 if (success) {
                     throw e;
                 } else {
                     // don't mask original error
                     LOG.warn("removeLiveVersionTest_FX807: " + e.getMessage(), e);
                 }
             }
         }
     }
 
     @Test(groups = {"ejb", "content"})
     public void compactIndicesTest_FX920() throws FxApplicationException {
         // test with root property
         final FxContent person = EJBLookup.getContentEngine().initialize("CMIS_PERSON");
         person.setValue("/email[1]", "first");
         person.setValue("/email[2]", "second");
         person.setValue("/email[3]", "third");
         person.remove("/email[1]");
         assertEquals(person.getValue("/email[1]").getXPath().toUpperCase(), "CMIS_PERSON[@PK=NEW]/EMAIL[1]");
         assertEquals(person.getValue("/email[2]").getXPath().toUpperCase(), "CMIS_PERSON[@PK=NEW]/EMAIL[2]");
 
         // test with property attached to a group
         final FxContent article = EJBLookup.getContentEngine().initialize("ARTICLE");
         article.setValue("/box[1]/box_title", "first");
         article.setValue("/box[2]/box_title", "second");
         article.setValue("/box[3]/box_title", "third");
         article.remove("/box[1]");
         assertEquals(article.getValue("/box[1]/box_title").getXPath().toUpperCase(), "ARTICLE[@PK=NEW]/BOX[1]/BOX_TITLE[1]");
         assertEquals(article.getValue("/box[2]/box_title").getXPath().toUpperCase(), "ARTICLE[@PK=NEW]/BOX[2]/BOX_TITLE[1]");
     }
 
     /**
      * Test the conversion of a content type to another
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void contentTypeConversionTest() throws FxApplicationException {
         // create the structures
         FxPK contentPK;
         FxContent content;
         String destTypeName;
         try {
             /**
              * hierarchical structures
               */
             createStructForContentTypeConversionTest(false, false);
 
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
             final FxType convDerived2 = CacheAdmin.getEnvironment().getType("CONVDERIVED2");
             final FxType convDerived3 = CacheAdmin.getEnvironment().getType("CONVDERIVED3");
             final FxType convDerived4 = CacheAdmin.getEnvironment().getType("CONVDERIVED4");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             // create content for the parent
             contentPK = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convParent.getId());
             // convert (lossy), test and delete
             ce.convertContentType(contentPK, convDerived1.getId(), true, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             removeConversionStruct(true);
 
             // create
             contentPK = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             // convert (lossless), test and delete
             ce.convertContentType(contentPK, convDerived1.getId(), false, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             removeConversionStruct(true);
 
             // convderived2 -> derived + additional property
             // create
             contentPK = conversionContent(convParent);
             destTypeName = convDerived2.getName();
             // convert (lossy), test and delete
             ce.convertContentType(contentPK, convDerived2.getId(), true, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived2.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived2.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             removeConversionStruct(true);
 
             // create
             contentPK = conversionContent(convParent);
             destTypeName = convDerived2.getName();
             // convert (lossless), test and delete
             ce.convertContentType(contentPK, convDerived2.getId(), false, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived2.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived2.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             removeConversionStruct(true);
 
             // convderived3 -> derived - property "prop1"
             // create
             contentPK = conversionContent(convParent);
             destTypeName = convParent.getName();
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             // convert (lossy), test and delete
             ce.convertContentType(contentPK, convDerived3.getId(), true, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived3.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived3.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             destTypeName = convDerived3.getName();
             Assert.assertFalse(content.containsXPath(destTypeName + p1));
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             removeConversionStruct(true);
 
             // create
             contentPK = conversionContent(convParent);
             // convert (lossless), test and delete (should fail during conversion)
 
             try {
                 ce.convertContentType(contentPK, convDerived3.getId(), false, true);
             } catch (FxApplicationException e) {
                 Assert.assertTrue(e instanceof FxContentTypeConversionException);
             }
             removeConversionStruct(true);
 
             /**
              * differing ACL test
              */
             // create
             contentPK = conversionContent(convParent);
             // convert (lossless), test and delete (should fail during conversion)
 
             // create a new testuser having the typeconvtest-acl2 acl
             final long aclId = CacheAdmin.getEnvironment().getACL("typeconvtest-acl1").getId();
             TestUsers.assignACL(TestUsers.REGULAR, aclId, ACLPermission.CREATE, ACLPermission.DELETE, ACLPermission.EDIT, ACLPermission.READ);
 
             try {
                 logout();
             } catch (FxLogoutFailedException e) {
                 e.printStackTrace();
             }
 
             try {
                 login(TestUsers.REGULAR);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             try {
                 // convert (lossy), test and delete
                 ce.convertContentType(contentPK, convDerived4.getId(), true, true);
             } catch (FxApplicationException e) {
                 Assert.assertTrue(e instanceof FxContentTypeConversionException);
                 
             }
 
             try {
                 logout();
                 login(TestUsers.SUPERVISOR);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             // remove acl from TestUsers.REGULAR
             EJBLookup.getAclEngine().unassign(aclId, TestUsers.REGULAR.getUserGroupId());
 
             // Test exception if source and destination are the same
             final long convParentId = convParent.getId();
             // create
             contentPK = conversionContent(convParent);
             try {
                 ce.convertContentType(contentPK, convParentId, false, true);
             } catch(FxApplicationException e) {
                 Assert.assertTrue(e instanceof FxContentTypeConversionException);
             }
 
         } catch (FxApplicationException e) {
             // silent death
         } finally {
             removeConversionStruct(false);
         }
     }
 
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void flatToFlatConversionTest() throws FxApplicationException {
         // conversion FLAT --> FLAT
         FxPK contentPK;
         String destTypeName;
         FxContent content;
         try {
             createStructForContentTypeConversionTest(true, false);
 
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             // create content for the parent
             contentPK = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convParent.getId());
             // convert (lossy), test and delete
             ce.convertContentType(contentPK, convDerived1.getId(), true, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void flatToHierarchicalConversionTest() throws FxApplicationException {
         // conversion FLAT --> HIERARCHICAL
         FxPK contentPK;
         String destTypeName;
         FxContent content;
         try {
             createStructForContentTypeConversionTest(true, true);
 
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             // create content for the parent
             contentPK = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convParent.getId());
             // convert (lossy), test and delete
             ce.convertContentType(contentPK, convDerived1.getId(), true, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void hierarchicalToFlatConversionTest() throws FxApplicationException {
         // conversion HIERARCHICAL --> FLAT
         FxPK contentPK;
         String destTypeName;
         FxContent content;
         try {
             createStructForContentTypeConversionTest(true, true);
 
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             contentPK = conversionContent(convDerived1);
             destTypeName = convParent.getName();
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convDerived1.getId());
             // convert, test and delete
             ce.convertContentType(contentPK, convParent.getId(), false, true);
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convParent.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 1);
             // test that all content still exists
             content = ce.load(contentPK);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void mixedStructureConversionTest() throws FxApplicationException {
         // conversion of mixed structures
          FxPK contentPK;
         String destTypeName;
         FxContent content;
         /**
          * + ... = flat
          * - ... = hierarchical
          * lossless conversion of
          * CONVPARENT (prop1+, prop2+, prop3+) --> CONVDERIVED1 (prop1-, prop2-, prop3-)
          * --> CONVDERIVED4 (prop1-, prop2+, prop3-) --> CONVPARENT
          */
         try {
             createStructForContentTypeConversionTest(true, true);
 
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
             final FxType convDerived4 = CacheAdmin.getEnvironment().getType("CONVDERIVED4");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             // convparent --> convderived1
             contentPK = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             Assert.assertEquals(ce.load(contentPK).getTypeId(), convParent.getId());
             ce.convertContentType(contentPK, convDerived1.getId(), false, true);
 
             content = ce.load(contentPK);
             Assert.assertEquals(content.getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
             // test that all content still exists
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             // convderived1 --> convderived4
             contentPK = content.getPk();
             destTypeName = convDerived4.getName();
             ce.convertContentType(contentPK, convDerived4.getId(), false, true);
 
             content = ce.load(contentPK);
             Assert.assertEquals(content.getTypeId(), convDerived4.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived4.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 0);
             // test that all content still exists
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             // convderived4 --> convparent
             contentPK = content.getPk();
             destTypeName = convParent.getName();
             ce.convertContentType(contentPK, convParent.getId(), false, true);
 
             content = ce.load(contentPK);
             Assert.assertEquals(content.getTypeId(), convParent.getId());
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convDerived4.getId(), true).size() == 0);
             // test that all content still exists
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     /**
      * Test the correct conversion of multiple versions of a given content
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void multipleContentVersionConversionTest() throws FxApplicationException {
         // conversion HIERARCHICAL --> FLAT
         FxPK contentPKv1, contentPKv2;
         String destTypeName, sourceTypeName;
         FxContent content;
         FxType convParent, convDerived1, convDerived4;
         try {
             // flat structure only ///////////////////////////////////////////
             createStructForContentTypeConversionTest(false, false);
 
             convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             final String p1cv2 = "1234-2";
             final String p2cv2 = "5678-2";
             final String p3cv2 = "9090-2";
 
             contentPKv1 = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             sourceTypeName = convParent.getName();
 
             // create new version & save
             content = ce.load(contentPKv1);
             content.setValue(sourceTypeName + p1, p1cv2);
             content.setValue(sourceTypeName + p2, p2cv2);
             content.setValue(sourceTypeName + p3, p3cv2);
             contentPKv2 = ce.createNewVersion(content);
 
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convParent.getId());
 
             // convert, test and delete
             ce.convertContentType(contentPKv1, convDerived1.getId(), false, true);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 0);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
 
             removeConversionStruct(false);
 
             // mixed structure ////////////////////////////////////////////////////////////
             createStructForContentTypeConversionTest(true, true);
 
             convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
             convDerived4 = CacheAdmin.getEnvironment().getType("CONVDERIVED4");
 
             contentPKv1 = conversionContent(convDerived1);
             destTypeName = convDerived4.getName();
             sourceTypeName = convDerived1.getName();
             
             // create new version & save
             content = ce.load(contentPKv1);
             content.setValue(sourceTypeName + p1, p1cv2);
             content.setValue(sourceTypeName + p2, p2cv2);
             content.setValue(sourceTypeName + p3, p3cv2);
             contentPKv2 = ce.createNewVersion(content);
 
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived1.getId());
 
             // convert, test and delete
             ce.convertContentType(contentPKv1, convDerived4.getId(), false, true);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived4.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived4.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 0);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
             
         } finally {
             removeConversionStruct(false);
         }
     }
 
     /**
      * Test conversion of a single version of a given content
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void singleVersionConversionTest() throws FxApplicationException {
         // TODO: needs to be implemented, atm all versions are converted!
         FxPK contentPKv1, contentPKv2;
         String destTypeName, sourceTypeName;
         FxContent content;
         FxType convParent, convDerived1, convDerived4;
 
 
 
         try {
              // flat structure
             createStructForContentTypeConversionTest(false, false);
 
             convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
 
             final String p1 = "/PROP1";
             final String p1c = "1234";
             final String p2 = "/PROP2";
             final String p2c = "5678";
             final String p3 = "/GROUP1/PROP3";
             final String p3c = "9090";
 
             final String p1cv2 = "1234-2";
             final String p2cv2 = "5678-2";
             final String p3cv2 = "9090-2";
 
             contentPKv1 = conversionContent(convParent);
             destTypeName = convDerived1.getName();
             sourceTypeName = convParent.getName();
 
             // create new version & save
             content = ce.load(contentPKv1);
             content.setValue(sourceTypeName + p1, p1cv2);
             content.setValue(sourceTypeName + p2, p2cv2);
             content.setValue(sourceTypeName + p3, p3cv2);
             contentPKv2 = ce.createNewVersion(content);
 
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convParent.getId());
             Assert.assertFalse(contentPKv1.getVersion() == contentPKv2.getVersion());
 
             // now convert version 1 only:
             ce.convertContentType(contentPKv1, convDerived1.getId(), false, false);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
             // we should have one pk left for the source
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 1);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
 
             // CONVERT BACK to parent type
             ce.convertContentType(contentPKv1, convParent.getId(), false, false);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convParent.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convParent.getId(), true).size() == 1);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
 
             // delete
             removeConversionStruct(false);
 
             // mixed structure ////////////////////////////////////////////////////////////
             createStructForContentTypeConversionTest(true, true);
 
             convDerived1 = CacheAdmin.getEnvironment().getType("CONVDERIVED1");
             convDerived4 = CacheAdmin.getEnvironment().getType("CONVDERIVED4");
 
             contentPKv1 = conversionContent(convDerived1);
             destTypeName = convDerived4.getName();
             sourceTypeName = convDerived1.getName();
 
             // create new version & save
             content = ce.load(contentPKv1);
             content.setValue(sourceTypeName + p1, p1cv2);
             content.setValue(sourceTypeName + p2, p2cv2);
             content.setValue(sourceTypeName + p3, p3cv2);
             contentPKv2 = ce.createNewVersion(content);
 
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived1.getId());
 
             // convert, test and delete
             ce.convertContentType(contentPKv1, convDerived4.getId(), false, false);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived4.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived4.getId(), true).size() == 1);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             Assert.assertEquals(content.getTypeId(), convDerived4.getId());
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
             Assert.assertEquals(content.getTypeId(), convDerived1.getId());
 
             // CONVERT BACK to parent type
             // convert, test and delete
             ce.convertContentType(contentPKv1, convDerived1.getId(), false, false);
             Assert.assertEquals(ce.load(contentPKv1).getTypeId(), convDerived1.getId());
             Assert.assertTrue(ce.getPKsForType(convDerived4.getId(), true).size() == 0);
             Assert.assertTrue(ce.getPKsForType(convDerived1.getId(), true).size() == 1);
 
             // test that all content still exists
             content = ce.load(contentPKv1);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1c);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2c);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3c);
             Assert.assertEquals(content.getTypeId(), convDerived1.getId());
 
             content = ce.load(contentPKv2);
             Assert.assertEquals(content.getValue(destTypeName + p1).toString(), p1cv2);
             Assert.assertEquals(content.getValue(destTypeName + p2).toString(), p2cv2);
             Assert.assertEquals(content.getValue(destTypeName + p3).toString(), p3cv2);
             Assert.assertEquals(content.getTypeId(), convDerived1.getId());
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     /**
      * Test content casts between multiple derived types and parents
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content", "contentconversion"})
     public void multipleInheritanceTest() throws FxApplicationException {
         FxPK pk;
         FxContent co;
         FxPropertyData data;
         try {
             createMultipleInheritanceStructure();
             final FxType convParent = CacheAdmin.getEnvironment().getType("CONVPARENT");
             final FxType convDerived4 = CacheAdmin.getEnvironment().getType("CONVDERIVED4");
 
             // convParent --> convDerived4
             pk = conversionContent(convParent, "CONVPARENT/PROP1");
             co = ce.load(pk);
             Assert.assertEquals(co.getTypeId(), convParent.getId());
 
             ce.convertContentType(pk, convDerived4.getId(), false, true);
             co = ce.load(pk);
             Assert.assertEquals(co.getTypeId(), convDerived4.getId());
             data = co.getPropertyData("CONVDERIVED4/PROP1");
             Assert.assertEquals(data.getAssignment().getId(), CacheAdmin.getEnvironment().getAssignment("CONVDERIVED4/PROP1").getId());
 
         } finally {
             removeConversionStruct(false);
         }
     }
 
     /**
      * Required structures for the type conversion tests
      * "mixed" disregarded if flat set to "false"
      * 
      * @param flat set to true to create properties in the flatstorage
      * @param mixed set to true to create a structure having both hierarchical entries and flatstorage entries: parent = flat, CONVDERIVED1 = hierarchical
      * @throws FxApplicationException on errors
      */
     private void createStructForContentTypeConversionTest(boolean flat, boolean mixed) throws FxApplicationException {
         final long aclId1 = EJBLookup.getAclEngine().create("typeconvtest-acl1", new FxString("typeconvtest-acl1"), TestUsers.getTestMandator(),
                 "#000000", "", ACLCategory.STRUCTURE);
         final long aclId2 = EJBLookup.getAclEngine().create("typeconvtest-acl2", new FxString("typeconvtest-acl2"), TestUsers.getTestMandator(),
                 "#000000", "", ACLCategory.STRUCTURE);
 
         final StringBuilder code = new StringBuilder(500);
 
         if (flat) {
             if (mixed) {
                 // convparent = flatten:true
                 // convderived1 = flatten:false
                 // convderived4 = convparent, only prop2 = flat, ACL = the same
                 code.append("import com.flexive.shared.*\nimport com.flexive.shared.value.FxString\nimport com.flexive.shared.scripting.groovy.*\n")
                         .append("new GroovyTypeBuilder().convparent(generalACL: \"typeconvtest-acl1\") {\n")
                         .append("prop1()\nprop1(flatten:true)\nprop2()\nprop2(flatten:true)\nGroup1() {\nprop3()\nprop3(flatten:true)\n}\n}\n")
                         .append("new GroovyTypeBuilder().convDerived1(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\") {\nprop1(flatten:false)\nprop2(flatten:false)\nGroup1() {\nprop3(flatten:false)\n}\n}\n")
                         .append("new GroovyTypeBuilder().convDerived2(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\") {\nprop4()\n}\n")
                         .append("new GroovyTypeBuilder().convDerived3(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\")\n")
                         .append("new GroovyTypeBuilder().convDerived4(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\") {\nprop1(flatten:false)\nprop2(flatten:true)\nGroup1() {\nprop3(flatten:false)\n}\n}\n");
             } else {
                 code.append("import com.flexive.shared.*\nimport com.flexive.shared.value.FxString\nimport com.flexive.shared.scripting.groovy.*\n")
                         .append("new GroovyTypeBuilder().convparent(generalACL: \"typeconvtest-acl1\") {\n")
                         .append("prop1()\nprop1(flatten:true)\nprop2()\nprop2(flatten:true)\nGroup1() {\nprop3()\nprop3(flatten:true)\n}\n}\n")
                         .append("new GroovyTypeBuilder().convDerived1(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\")\n")
                         .append("new GroovyTypeBuilder().convDerived2(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\") {\nprop4()\n}\n")
                         .append("new GroovyTypeBuilder().convDerived3(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\")\n")
                         .append("new GroovyTypeBuilder().convDerived4(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl2\")\n");
             }
         } else {
             code.append("import com.flexive.shared.*\nimport com.flexive.shared.value.FxString\nimport com.flexive.shared.scripting.groovy.*\n")
                     .append("new GroovyTypeBuilder().convparent(generalACL: \"typeconvtest-acl1\") {\n")
                     .append("prop1()\nprop1(flatten:false)\nprop2()\nprop2(flatten:false)\nGroup1() {\nprop3()\nprop3(flatten:false)\n}\n}\n")
                     .append("new GroovyTypeBuilder().convDerived1(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\")\n")
                     .append("new GroovyTypeBuilder().convDerived2(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\") {\nprop4()\n}\n")
                     .append("new GroovyTypeBuilder().convDerived3(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl1\")\n")
                     .append("new GroovyTypeBuilder().convDerived4(parentTypeName: \"CONVPARENT\", generalACL: \"typeconvtest-acl2\")\n");
         }
         code.trimToSize();
 
         // run the groovy script
         EJBLookup.getScriptingEngine().runScript("structureCreation.groovy", null, code.toString()).getResult();
         // remove prop1() in convDerived3
         long removeId = CacheAdmin.getEnvironment().getAssignment("CONVDERIVED3/PROP1").getId();
         EJBLookup.getAssignmentEngine().removeAssignment(removeId);
     }
 
     /**
      * Multiple inheritance type conversion test structures
      * @throws FxApplicationException on errors
      */
     private void createMultipleInheritanceStructure() throws FxApplicationException {
         final StringBuilder code = new StringBuilder(500);
         code.append("import com.flexive.shared.scripting.groovy.*\n")
                 .append("new GroovyTypeBuilder().convparent() {\nprop1()\n}\n")
                 .append("new GroovyTypeBuilder().convderived1(parentTypeName: \"CONVPARENT\")\n")
                 .append("new GroovyTypeBuilder().convderived2(parentTypeName: \"CONVDERIVED1\")\n")
                 .append("new GroovyTypeBuilder().convderived3(parentTypeName: \"CONVDERIVED2\")\n")
                 .append("new GroovyTypeBuilder().convderived4(parentTypeName: \"CONVDERIVED3\")\n");
         code.trimToSize();
         // run
         EJBLookup.getScriptingEngine().runScript("structureCreation.groovy", null, code.toString()).getResult();
     }
 
     // Create content for the contenttype conversion test
     private FxPK conversionContent(FxType t, String... XPath) throws FxApplicationException {
         FxPK contentPK = null;
         FxContent co = ce.initialize(t.getId());
         final FxString pco1 = new FxString(false, "1234");
         final FxString pco2 = new FxString(false, "5678");
         final FxString pco3 = new FxString(false, "9090");
 
         if (XPath.length > 0) {
             for (String p : XPath) {
                 co.setValue(p, pco1);
             }
             contentPK = ce.save(co);
         } else {
             if ("CONVPARENT".equals(t.getName())) {
                 co.setValue("CONVPARENT/PROP1", pco1);
                 co.setValue("CONVPARENT/PROP2", pco2);
                 co.setValue("CONVPARENT/GROUP1/PROP3", pco3);
                 contentPK = ce.save(co);
             } else if ("CONVDERIVED1".equals(t.getName())) {
                 co.setValue("CONVDERIVED1/PROP1", pco1);
                 co.setValue("CONVDERIVED1/PROP2", pco2);
                 co.setValue("CONVDERIVED1/GROUP1/PROP3", pco3);
                 contentPK = ce.save(co);
             } else if ("CONVDERIVED2".equals(t.getName())) {
                 co.setValue("CONVDERIVED2/PROP1", pco1);
                 co.setValue("CONVDERIVED2/PROP2", pco2);
                 co.setValue("CONVDERIVED2/GROUP1/PROP3", pco3);
                 contentPK = ce.save(co);
             } else if ("CONVDERIVED3".equals(t.getName())) {
                 // co.setValue("CONVDERIVED3/PROP1", pco1); removed
                 co.setValue("CONVDERIVED3/PROP2", pco2);
                 co.setValue("CONVDERIVED3/GROUP1/PROP3", pco3);
                 contentPK = ce.save(co);
             } else if ("CONVDERIVED4".equals(t.getName())) {
                 co.setValue("CONVDERIVED4/PROP1", pco1);
                 co.setValue("CONVDERIVED4/PROP2", pco2);
                 co.setValue("CONVDERIVED4/GROUP1/PROP3", pco3);
                 contentPK = ce.save(co);
             }
         }
         return contentPK;
     }
 
     /**
      * Remove required structures for the type conversion test
      *
      * @param contentOnly true = remove content for conversion test types only
      * @throws FxApplicationException on errors
      */
     private void removeConversionStruct(boolean contentOnly) throws FxApplicationException {
         TypeEngine te = EJBLookup.getTypeEngine();
         // find and remove any contents, then structures, then acls
         final List<Long> typeIds = new ArrayList<Long>(5);
         if(CacheAdmin.getEnvironment().typeExists("CONVDERIVED4"))
             typeIds.add(CacheAdmin.getEnvironment().getType("CONVDERIVED4").getId());
         if(CacheAdmin.getEnvironment().typeExists("CONVDERIVED3"))
             typeIds.add(CacheAdmin.getEnvironment().getType("CONVDERIVED3").getId());
         if(CacheAdmin.getEnvironment().typeExists("CONVDERIVED2"))
             typeIds.add(CacheAdmin.getEnvironment().getType("CONVDERIVED2").getId());
         if(CacheAdmin.getEnvironment().typeExists("CONVDERIVED1"))
             typeIds.add(CacheAdmin.getEnvironment().getType("CONVDERIVED1").getId());
         if(CacheAdmin.getEnvironment().typeExists("CONVPARENT"))
             typeIds.add(CacheAdmin.getEnvironment().getType("CONVPARENT").getId());
 
         long[] aclIds = null;
         if(CacheAdmin.getEnvironment().aclExists("typeconvtest-acl1") && CacheAdmin.getEnvironment().aclExists("typeconvtest-acl2")) {
             aclIds = new long[2];
             aclIds[0] = CacheAdmin.getEnvironment().getACL("typeconvtest-acl1").getId();
             aclIds[1] = CacheAdmin.getEnvironment().getACL("typeconvtest-acl2").getId();
         }
 
         try {
             // contents
             for (long typeId : typeIds) {
                 final List<FxPK> pkList = ce.getPKsForType(typeId, false);
                 for (FxPK pk : pkList) {
                     ce.remove(pk);
                 }
             }
             if (!contentOnly) {
                 // remove structures separately
                 for (Long typeId : typeIds) {
                     te.remove(typeId);
                 }
                 // remove custom acls
                 if (aclIds != null) {
                     for (long aclId : aclIds) {
                         acl.remove(aclId);
                     }
                 }
             }
         } catch (FxApplicationException e) {
             // silent death
         }
     }
 
     /**
      * Test the automatic conversion of documentFile types
      */
 //    @Test(groups = {"ejb", "content"})
 //    public void testDocumentFileMimeConversion() {
 //
 //    }
 
     /**
      * Test checking and adding properties and groups multiplicities
      *
      * @throws FxApplicationException on errors
      */
     @Test(groups = {"ejb", "content"})
     public void testMultiplictyMethods() throws FxApplicationException {
         FxContent test = ce.initialize(TEST_TYPE).compact();
         FxString testValue = new FxString("Hello world");
 
         //property
         Assert.assertEquals(test.getMaxIndex("/TestProperty4"), 1); //1 as it is 1..N
         test.setValue("/TestProperty4", testValue);
         Assert.assertEquals(test.getMaxIndex("/TestProperty4"), 1);
         test.setValue("/TestProperty4[2]", testValue);
         Assert.assertEquals(test.getMaxIndex("/TestProperty4"), 2);
         test.setValue("/TestProperty4[" + (test.getMaxIndex("/TestProperty4") + 1) + "]", testValue);
         Assert.assertEquals(test.getMaxIndex("/TestProperty4"), 3);
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestProperty1_3"), 1);
         test.setValue("/TestGroup1/TestProperty1_3", testValue);
         Assert.assertEquals(test.getMaxIndex("/TestGroup1"), 1);
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestProperty1_3"), 1);
         Assert.assertEquals(test.addValue("/TestGroup1/TestProperty1_3", testValue), "/TESTGROUP1[1]/TESTPROPERTY1_3[2]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestProperty1_3"), 2);
         test.addValue("/TestGroup1/TestProperty1_3", testValue);
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestProperty1_3"), 3);
         test.addValue("/TestGroup1/TestProperty1_3", "Test");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestProperty1_3"), 4);
 
         //group
         Assert.assertEquals(test.getMaxIndex("/TestGroup1"), 1); //1 because it contains required properties/groups
         Assert.assertEquals(test.addGroup("/TestGroup1"), "/TESTGROUP1[2]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1"), 2);
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 1); //1 as the group contains a required property
         Assert.assertEquals(test.addGroup("/TestGroup1/TestGroup1_2"), "/TESTGROUP1[1]/TESTGROUP1_2[2]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 2);
         Assert.assertEquals(test.addGroup("/TestGroup1/TestGroup1_2"), "/TESTGROUP1[1]/TESTGROUP1_2[3]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 3);
         Assert.assertEquals(test.addGroup("/TestGroup1/TestGroup1_2"), "/TESTGROUP1[1]/TESTGROUP1_2[4]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 4);
         test.remove("/TESTGROUP1[1]/TESTGROUP1_2[2]");
         //gaps are closed
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 3);
         Assert.assertEquals(test.addGroup("/TestGroup1/TestGroup1_2"), "/TESTGROUP1[1]/TESTGROUP1_2[4]");
         Assert.assertEquals(test.getMaxIndex("/TestGroup1/TestGroup1_2"), 4);
     }
 
     @Test(groups = {"ejb", "content"})
     public void explodeTest() throws FxApplicationException {
         final FxContent content = ce.initialize(TEST_TYPE).compact();
 
         for (int i = 0; i < 10; i++) {
             content.getRootGroup().explode(true);
         }
 
         assertEquals(content.getMaxIndex("/TestProperty4"), 1, "TestProperty4: Unexpected cardinality after exploding 10 times");
         assertEquals(content.getMaxIndex("/TestGroup1"), 1, "TestGroup1: Unexpected cardinality after exploding 10 times");
     }
 }
