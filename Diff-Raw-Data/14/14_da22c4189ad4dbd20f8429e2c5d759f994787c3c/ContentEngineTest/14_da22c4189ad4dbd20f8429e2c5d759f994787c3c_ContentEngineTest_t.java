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
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import static com.flexive.tests.embedded.FxTestUtils.*;
 import com.flexive.tests.embedded.ScriptingTest;
 import com.flexive.tests.embedded.TestUsers;
 import org.apache.commons.lang.RandomStringUtils;
 import org.testng.Assert;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.fail;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.Arrays;
 
 /**
  * Tests for the ContentEngine
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "content"})
 public class ContentEngineTest {
 
     private ContentEngine co;
     private ACLEngine acl;
     private TypeEngine type;
     private AssignmentEngine ass;
     public static final String TEST_EN = "Test value in english";
     public static final String TEST_DE = "Test datensatz in deutsch mit \u00E4ml\u00E5ut te\u00DFt";
     public static final String TEST_FR = "My french is bad but testing special characters: ?`?$\u010E1\u00C6~\u0119\\/";
     public static final String TEST_IT = "If i knew italian this would be a test value in italian ;)";
 
     public static final String TEST_TYPE = "TEST_TYPE_" + RandomStringUtils.random(16, true, true);
     public static final String TEST_GROUP = "TEST_GROUP_" + RandomStringUtils.random(16, true, true);
     private static final String TYPE_ARTICLE = "__ArticleTest";
     private final static FxString DEFAULT_STRING = new FxString(true, "ABC");
 
     /**
      * setup...
      *
      * @throws Exception on errors
      */
     @BeforeClass
     public void beforeClass() throws Exception {
         co = EJBLookup.getContentEngine();
         acl = EJBLookup.getAclEngine();
         type = EJBLookup.getTypeEngine();
         ass = EJBLookup.getAssignmentEngine();
         login(TestUsers.SUPERVISOR);
     }
 
 
     @AfterClass(dependsOnMethods = {"tearDownStructures"})
     public void afterClass() throws FxLogoutFailedException {
         logout();
     }
 
     @AfterClass
     public void tearDownStructures() throws Exception {
         long typeId = CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
         co.removeForType(typeId);
         type.remove(typeId);
         typeId = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE).getId();
         co.removeForType(typeId);
         type.remove(typeId);
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
      * @throws Exception on errors
      */
     @BeforeClass(dependsOnMethods = {"setupACL"})
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
         }
         //create article type
         FxPropertyEdit pe = FxPropertyEdit.createNew("MyTitle", new FxString("Description"), new FxString("Hint"),
                 true, new FxMultiplicity(0, 1),
                 true, structACL, FxDataType.String1024, new FxString(FxString.EMPTY),
                 true, null, null, null).setAutoUniquePropertyName(true).setMultiLang(true).setOverrideMultiLang(true);
         long articleId = type.save(FxTypeEdit.createNew(TYPE_ARTICLE, new FxString("Article test type"), CacheAdmin.getEnvironment().getACLs(ACLCategory.STRUCTURE).get(0), null));
         ass.createProperty(articleId, pe, "/");
         pe.setName("Text");
         pe.setDataType(FxDataType.Text);
         pe.setMultiplicity(new FxMultiplicity(0, 2));
         ass.createProperty(articleId, pe, "/");
 
         long testDataId = type.save(FxTypeEdit.createNew(TEST_TYPE, new FxString("Test data"), CacheAdmin.getEnvironment().getACLs(ACLCategory.STRUCTURE).get(0), null));
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
     @BeforeClass(dependsOnMethods = {"beforeClass"})
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
 
     @Test
     public void removeAddData() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         Assert.assertTrue(testType != null);
         FxContent test = co.initialize(testType.getId());
         Assert.assertTrue(test != null);
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
 
         Assert.assertTrue(test.getRootGroup().getCreateableChildren(true).size() == 6);
         Assert.assertTrue(!test.getRootGroup().isRemoveable());
         Assert.assertTrue(test.getGroupData("/TestGroup1").isRemoveable());
         Assert.assertTrue(!test.getGroupData("/TestGroup1/TestGroup1_1").isRemoveable());
         Assert.assertTrue(test.getPropertyData("/TestProperty1").isRemoveable());
         Assert.assertTrue(!test.getPropertyData("/TestProperty2").isRemoveable());
         Assert.assertTrue(test.getPropertyData("/TestProperty3").isRemoveable());
         Assert.assertTrue(!test.getPropertyData("/TestProperty4").isRemoveable());
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
         Assert.assertTrue(cr.size() == 3);
         Assert.assertTrue(cr.get(0).equals("/TESTGROUP1[1]/TESTPROPERTY1_3[2]"));
         Assert.assertTrue(cr.get(1).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]"));
         Assert.assertTrue(cr.get(2).equals("/TESTGROUP1[1]/TESTGROUP1_3[2]"));
         cr = test.getGroupData("/TestGroup1").getCreateableChildren(false);
 //        for(String xp: cr)
 //            System.out.println("==cr=> "+xp);
         Assert.assertTrue(cr.size() == 1);
         Assert.assertTrue(cr.get(0).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]"));
 
         test.getGroupData("/TestGroup1").explode(false);
         Assert.assertTrue(test.getGroupData("/TestGroup1").getChildren().size() == 6);
         Assert.assertTrue(test.getGroupData("/TESTGROUP1[1]/TESTGROUP1_2[1]").getChildren().size() == 3);
         test.remove("/TESTGROUP1[1]/TESTGROUP1_2[1]");
         Assert.assertTrue(test.getGroupData("/TestGroup1").getCreateableChildren(false).size() == 1);
 
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
 
     @Test
     public void contentComplex() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         Assert.assertTrue(testType != null);
         //initialize tests start
         FxContent test = co.initialize(testType.getId());
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
         Assert.assertTrue(test != null);
         int rootSize = 9 + CacheAdmin.getEnvironment().getSystemInternalRootPropertyAssignments().size();
         Assert.assertTrue(rootSize == test.getData("/").size(), "Root size expected " + rootSize + ", was " + test.getData("/").size());
 //        FxGroupData groot = test.getData("/").get(0).getParent();
         //basic sanity checks
 //        Assert.assertTrue(groot.isEmpty()); TODO: isEmpty means no sys internal properties!
         Assert.assertTrue(test.getData("/TestProperty1").get(0).getAssignmentMultiplicity().toString().equals("0..1"));
         Assert.assertTrue(1 == test.getData("/TestProperty1").get(0).getIndex());
         Assert.assertTrue(!(test.getData("/TestProperty1").get(0).mayCreateMore())); //should be at multiplicity 1
         Assert.assertTrue(!(test.getPropertyData("/TestProperty2").mayCreateMore())); //should be at multiplicity 1
         Assert.assertTrue(test.getPropertyData("/TestProperty3").mayCreateMore()); //max of 5
         Assert.assertTrue(test.getGroupData("/TestGroup1").mayCreateMore()); //max of 2
         Assert.assertTrue(1 == test.getGroupData("/TestGroup1").getCreateableElements()); //1 left to create
         Assert.assertTrue(1 == test.getGroupData("/TestGroup1").getRemoveableElements()); //1 left to remove
         Assert.assertTrue(6 == test.getData("/TestGroup1").size());
         Assert.assertTrue(0 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getCreateableElements()); //0 left
         Assert.assertTrue(1 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getRemoveableElements()); //0 left
         Assert.assertTrue(!(test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").mayCreateMore())); //0 left
         Assert.assertTrue(1 == test.getGroupData("/TestGroup1/TestGroup1_2").getIndex());
         FxPropertyData p = test.getPropertyData("/TestGroup1/TestGroup1_2/TestGroup1_2_1/TestProperty1_2_1_1");
         Assert.assertTrue(1 == p.getIndex());
         Assert.assertTrue(p.isEmpty());
         Assert.assertTrue(p.isProperty());
         Assert.assertTrue("TESTPROPERTY1_2_1_1".equals(p.getAlias()));
         Assert.assertTrue("TESTGROUP1_2_1".equals(p.getParent().getAlias()));
         Assert.assertTrue(p.getAssignmentMultiplicity().equals(new FxMultiplicity(0, 2)));
         Assert.assertTrue(4 == p.getIndices().length);
         //check required with empty values
         try {
             test.checkValidity();
             fail("checkValidity() succeeded when it should not!");
         } catch (FxInvalidParameterException e) {
             //ok
             Assert.assertTrue("ex.content.required.missing".equals(e.getExceptionMessage().getKey()));
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
             Assert.assertTrue("ex.content.required.missing".equals(e.getExceptionMessage().getKey()));
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
         FxPK pk = co.save(test);
         co.remove(pk);
         //test /TestGroup1[2]...
         FxGroupData gd = test.getGroupData("/TestGroup1");
         Assert.assertTrue(gd.mayCreateMore());
         Assert.assertTrue(1 == gd.getCreateableElements()); //1 more should be createable
         gd.createNew(FxData.POSITION_BOTTOM);
         Assert.assertTrue(test.getGroupData("/TestGroup1[2]").isEmpty());
         //should still be valid since [2] is empty
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             fail("checkValidity() did not succeed when it should!");
         }
         test.setValue("/TestGroup1[2]/TestProperty1_2", testValue);
         test.setValue("/TestGroup1[2]/TestProperty1_3", testValue);
         pk = co.save(test);
         FxContent testLoad = co.load(pk);
         final String transIt = ((FxString) testLoad.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN);
         Assert.assertTrue(TEST_IT.equals(transIt), "Expected italian translation '" + TEST_IT + "', got: '" + transIt + "' for pk " + pk);
         co.remove(pk);
         pk = co.save(test);
         FxContent testLoad2 = co.load(pk);
         FxNumber number = new FxNumber(true, FxLanguage.GERMAN, 42);
         number.setTranslation(FxLanguage.ENGLISH, 43);
         testLoad2.setValue("/TestNumber", number);
         FxNumber numberSL = new FxNumber(false, FxLanguage.GERMAN, 12);
         Assert.assertTrue(numberSL.getDefaultLanguage() == FxLanguage.SYSTEM_ID);
         Assert.assertTrue(12 == numberSL.getTranslation(FxLanguage.FRENCH));
         numberSL.setTranslation(FxLanguage.ITALIAN, 13);
         Assert.assertTrue(13 == numberSL.getTranslation(FxLanguage.FRENCH));
         testLoad2.setValue("/TestNumberSL", numberSL);
         FxFloat fxFloat = new FxFloat(true, FxLanguage.GERMAN, 42.42f);
         fxFloat.setTranslation(FxLanguage.ENGLISH, 43.43f);
         testLoad2.setValue("/TestFloat", fxFloat);
         Assert.assertTrue(42 == ((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42 for german, before save)");
         Assert.assertTrue(43 == ((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43, before save)");
         Assert.assertTrue(testLoad2.getPropertyData("/TestNumber").getValue().hasDefaultLanguage());
         Assert.assertTrue(42.42f == ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42.42f for german, before save)");
         Assert.assertTrue(43.43f == ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43.43f, before save)");
         Assert.assertTrue(testLoad2.getPropertyData("/TestFloat").getValue().hasDefaultLanguage());
         FxPK saved = co.save(testLoad2);
         FxContent testLoad3 = co.load(saved);
         Assert.assertTrue(42 == ((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42 for german, after load)");
         Assert.assertTrue(43 == ((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43, after load)");
         Assert.assertTrue(13 == ((FxNumber) testLoad3.getPropertyData("/TestNumberSL").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 13, after load)");
         Assert.assertTrue(!testLoad3.getPropertyData("/TestNumberSL").getValue().isMultiLanguage(), "Single language value expected");
         Assert.assertTrue(testLoad3.getPropertyData("/TestNumber").getValue().hasDefaultLanguage(), "Missing default language after load");
         Assert.assertTrue(42.42f == ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getDefaultTranslation(), "Default translation invalid (should be 42.42f for german, before save)");
         Assert.assertTrue(43.43f == ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH), "English translation invalid (should be 43.43f, before save)");
         Assert.assertTrue(testLoad3.getPropertyData("/TestFloat").getValue().hasDefaultLanguage(), "Missing default language after load");
         final String transIt2 = ((FxString) testLoad3.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN);
         Assert.assertTrue(TEST_IT.equals(transIt2), "Expected italian translation '" + TEST_IT + "', got: '" + transIt2 + "'");
         Assert.assertTrue(1 == co.removeForType(testType.getId()), "Only one instance should be removed!");
         Assert.assertTrue(0 == co.removeForType(testType.getId()), "No instance should be left to remove!");
 
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
         co.initialize(testType.getId()).randomize();
     }
 
     @Test
     public void getValues() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         Assert.assertTrue(testType != null);
         FxContent test = co.initialize(testType.getId());
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
 
     @Test
     public void setValue() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent test = co.initialize(testType.getId());
         //set required properties
         test.setValue("/TestProperty2", new FxString(true, "Test2"));
         test.setValue("/TestProperty4", new FxString(true, "Test4"));
         FxPK pk = test.save().getPk();
         try {
             FxContent loaded = co.load(pk);
             //test setting a value that is not present in the loaded content
             Assert.assertFalse(loaded.containsValue("/TestProperty1"));
            loaded.setValue(
                    testType.getPropertyAssignment("/TestProperty1").getXPath(), 
                    "Test1"
            );
             loaded.save();
             loaded = co.load(pk);
             Assert.assertEquals(loaded.getValue("/TestProperty1").getBestTranslation(), "Test1");
         } finally {
             co.remove(pk);
         }
     }
 
     @Test
     public void contentInitialize() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
             FxContent test = co.initialize(article.getId());
             test.setAclId(CacheAdmin.getEnvironment().getACL("Article ACL").getId());
             test.getData("/");
             test.getData("/MYTITLE");
             test.getData("/TEXT");
             //test if shared message loading works
             FxNoAccess noAccess = new FxNoAccess(getUserTicket(), new FxString("test"));
             if (getUserTicket().getLanguage().getId() == FxLanguage.ENGLISH)
                 Assert.assertTrue("Access denied!".equals(noAccess.getDefaultTranslation()), "Shared message loading failed! Expected [Access denied!] got: [" + noAccess.getDefaultTranslation() + "]");
             else if (getUserTicket().getLanguage().getId() == FxLanguage.GERMAN)
                 Assert.assertTrue("Zugriff verweigert!".equals(noAccess.getDefaultTranslation()), "Shared message loading failed!");
         } catch (FxApplicationException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void defaultMultiplicity() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
 
             FxPropertyAssignmentEdit pe = new FxPropertyAssignmentEdit((FxPropertyAssignment) article.getAssignment("/TEXT"));
             pe.setDefaultMultiplicity(2);
             Assert.assertTrue(2 == pe.getDefaultMultiplicity(), "Wrong default multiplicity");
             ass.save(pe, false);
 
             FxContent test = co.initialize(article.getId());
             test.getData("/TEXT[1]");
             test.getData("/TEXT[2]");
             pe.setDefaultMultiplicity(1);
             ass.save(pe, false);
             test = co.initialize(article.getId());
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
 
 
     @Test
     public void contentCreate() throws Exception {
         FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
         FxContent test = co.initialize(article.getId());
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
         Assert.assertTrue("Text english1".equals(((FxString) test.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH)));
         if (pText.mayCreateMore()) {
             FxPropertyData pText2 = (FxPropertyData) pText.createNew(FxData.POSITION_BOTTOM);
             pText2.setValue(new FxString(FxLanguage.ENGLISH, "Text english2"));
             Assert.assertTrue("Text english2".equals(((FxString) test.getPropertyData("/TEXT[2]").getValue()).getTranslation(FxLanguage.ENGLISH)));
         }
         //            for( int i=0; i<100;i++)
         FxPK pk = test.save().getPk();
         FxContent comp = co.load(pk);
         Assert.assertTrue(comp != null);
         Assert.assertTrue(comp.getPk().getId() == pk.getId(), "Id failed");
         Assert.assertTrue(comp.getPk().getId() == comp.getId(), "Id of content not equal the Id of contents pk");
         Assert.assertTrue(comp.matchesPk(pk), "matchesPk failed");
         Assert.assertTrue(comp.matchesPk(new FxPK(pk.getId(), FxPK.MAX)), "matchesPk for max version failed");
         Assert.assertTrue(1 == comp.getPk().getVersion(), "Version is not 1");
         Assert.assertTrue(comp.getStepId() == test.getStepId(), "Step failed");
         assertEquals(comp.getAclIds(), test.getAclIds(), "ACL failed");
         Assert.assertTrue(comp.isMaxVersion(), "MaxVersion failed");
         Assert.assertTrue(comp.isLiveVersion() == article.getWorkflow().getSteps().get(0).isLiveStep(), "LiveVersion failed. Expected:" + article.getWorkflow().getSteps().get(0).isLiveStep() + " Got:" + comp.isLiveVersion());
         Assert.assertTrue(comp.getMainLanguage() == FxLanguage.ENGLISH, "MainLang failed");
         Assert.assertTrue(comp.getLifeCycleInfo().getCreatorId() == getUserTicket().getUserId(), "CreatedBy failed");
         Assert.assertTrue("Text english1".equals(((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH)), "Expected 'Text english1', got '" + ((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH) + "'");
         //test result of move
         Assert.assertTrue(titlePos == comp.getPropertyData("/TEXT").getPos(), "Text[1] position should be " + (titlePos) + " but is " + comp.getPropertyData("/TEXT").getPos());
         Assert.assertTrue(titlePos + 1 == comp.getPropertyData("/MYTITLE[1]").getPos());
         Assert.assertTrue(titlePos + 2 == comp.getPropertyData("/TEXT[2]").getPos());
         FxPK pk2 = co.createNewVersion(comp);
         Assert.assertTrue(2 == pk2.getVersion());
         comp.setValue("/TEXT", new FxString(FxLanguage.GERMAN, "Different text"));
         FxPK pk3 = co.createNewVersion(comp);
         Assert.assertTrue(3 == pk3.getVersion());
         FxContentVersionInfo cvi = co.getContentVersionInfo(pk3);
         Assert.assertTrue(3 == cvi.getLastModifiedVersion());
         Assert.assertTrue(3 == cvi.getLiveVersion());
         Assert.assertTrue(1 == cvi.getMinVersion());
 
         FxContentContainer cc = co.loadContainer(cvi.getId());
         assertEquals(cc.getVersionInfo().getLastModifiedVersion(), cvi.getLastModifiedVersion());
         assertEquals(cc.getVersionInfo().getLiveVersion(), cvi.getLiveVersion());
         assertEquals(cc.getVersionInfo().getMinVersion(), cvi.getMinVersion());
         assertEquals(cc.getVersion(2), co.load(pk2));
         assertEquals(cc.getVersion(3), co.load(pk3));
         Assert.assertNotSame(cc.getVersion(1), cc.getVersion(2));
         Assert.assertTrue(FxDelta.processDelta(cc.getVersion(1), cc.getVersion(2)).isOnlyInternalPropertyChanges());
         Assert.assertNotSame(cc.getVersion(1), cc.getVersion(3));
         Assert.assertFalse(FxDelta.processDelta(cc.getVersion(2), cc.getVersion(3)).isOnlyInternalPropertyChanges());
 
         co.removeVersion(new FxPK(pk.getId(), 1));
         cvi = co.getContentVersionInfo(pk3);
         Assert.assertTrue(2 == cvi.getMinVersion());
         Assert.assertTrue(3 == cvi.getMaxVersion());
         co.removeVersion(new FxPK(pk.getId(), 3));
         cvi = co.getContentVersionInfo(pk3);
         Assert.assertTrue(2 == cvi.getMinVersion());
         Assert.assertTrue(2 == cvi.getMaxVersion());
         Assert.assertTrue(!cvi.hasLiveVersion());
         co.removeVersion(new FxPK(pk.getId(), 2));
         try {
             co.getContentVersionInfo(new FxPK(pk.getId()));
             fail("VersionInfo available for a removed instance!");
         } catch (FxApplicationException e) {
             //ok
         }
     }
 
     @Test
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
 
     @Test
     public void typeValidityTest() throws Exception {
         FxType t = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         Assert.assertTrue(t.isXPathValid("/", false), "Root group should be valid for groups");
         Assert.assertTrue(!t.isXPathValid("/", true), "Root group should be invalid for properties");
         Assert.assertTrue(t.isXPathValid("/TestProperty1", true));
         Assert.assertTrue(!t.isXPathValid("/TestProperty1", false));
         Assert.assertTrue(t.isXPathValid(TEST_TYPE + "/TestProperty1", true));
         Assert.assertTrue(!t.isXPathValid(TEST_TYPE + "123/TestProperty1", true));
         Assert.assertTrue(!t.isXPathValid("WrongType/TestProperty1", true));
         Assert.assertTrue(!t.isXPathValid(TEST_TYPE + "/TestProperty1/Dummy", true));
         Assert.assertTrue(t.isXPathValid("/TestProperty1[1]", true));
         Assert.assertTrue(!t.isXPathValid("/TestProperty1[2]", true));
         Assert.assertTrue(t.isXPathValid("/TestGroup1[2]", false));
         Assert.assertTrue(t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", true));
         Assert.assertTrue(!t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", false));
         Assert.assertTrue(t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", true));
         Assert.assertTrue(!t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", false));
         Assert.assertTrue(!t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[6]", true));
     }
 
     @Test
     public void setValueTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent test = co.initialize(testType.getId());
         final String TEST_VALUE_EN = "Hello world";
         final String TEST_VALUE_DE = "Hallo welt";
         final String TEST_XPATH = "/TestGroup1[2]/TestGroup1_2[3]/TestProperty1_2_2[4]";
 
         FxString testValue = new FxString(TEST_VALUE_EN);
         test.setValue(TEST_XPATH, testValue);
         Assert.assertTrue(test.getValue(TEST_XPATH).equals(testValue));
         test.setValue(TEST_XPATH, TEST_VALUE_EN);
         test.setValue(TEST_XPATH, FxLanguage.GERMAN, TEST_VALUE_DE);
         Assert.assertTrue(test.getValue(TEST_XPATH).getTranslation(FxLanguage.ENGLISH).equals(TEST_VALUE_EN));
         Assert.assertTrue(test.getValue(TEST_XPATH).getTranslation(FxLanguage.GERMAN).equals(TEST_VALUE_DE));
         Assert.assertTrue(test.getValue(TEST_XPATH).getDefaultTranslation().equals(TEST_VALUE_EN));
     }
 
     @Test
     public void deltaTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent org = co.initialize(testType.getId());
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         //set required properties to allow saving ..
         org.setValue("/TestProperty2[1]", testValue1);
         org.setValue("/TestProperty4[1]", testValue1);
 
         //test properties
         org.setValue("/TestProperty3[1]", testValue1);
         org.setValue("/TestProperty3[2]", testValue2);
         org.setValue("/TestProperty3[3]", testValue1);
 
         FxPK pk = co.save(org);
         try {
             org = co.load(pk);
             FxContent test = co.load(pk);
             FxDelta d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             Assert.assertTrue(d.getAdds().size() == 0, "Expected no adds, but got " + d.getAdds().size());
             Assert.assertTrue(d.getRemoves().size() == 0, "Expected no deletes, but got " + d.getRemoves().size());
             Assert.assertTrue(d.getUpdates().size() == 0, "Expected no updates, but got " + d.getUpdates().size());
 
             test.remove("/TestProperty3[2]");
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             Assert.assertTrue(d.getAdds().size() == 0, "Expected no adds, but got " + d.getAdds().size());
             Assert.assertTrue(d.getRemoves().size() == 1, "Expected 1 deletes, but got " + d.getRemoves().size());
             Assert.assertTrue(d.getUpdates().size() == 1, "Expected 1 updates, but got " + d.getUpdates().size());
             Assert.assertTrue(d.getRemoves().get(0).getXPath().equals("/TESTPROPERTY3[3]"), "Expected /TESTPROPERTY3[3] but got: " + d.getRemoves().get(0).getXPath());
             Assert.assertTrue(d.getUpdates().get(0).getXPath().equals("/TESTPROPERTY3[2]"), "Expected /TESTPROPERTY3[2] but got: " + d.getUpdates().get(0).getXPath());
 
             test = co.load(pk);
             test.setValue("/TestGroup1/TestProperty1_2", testValue1);
             test.setValue("/TestGroup1/TestProperty1_3", testValue1);
             test.getGroupData("/TestGroup1").removeEmptyEntries();
             test.getGroupData("/TestGroup1").compactPositions(true);
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             Assert.assertTrue(d.changes(), "Expected some changes");
             Assert.assertTrue(d.getAdds().size() == 3, "Expected 3 (group + 2 properties) adds but got " + d.getAdds().size());
             Assert.assertTrue(d.getRemoves().size() == 0, "Expected 0 deletes but got " + d.getRemoves().size());
             Assert.assertTrue(d.getUpdates().size() == 0, "Expected 0 updates but got " + d.getUpdates().size());
         } finally {
             co.remove(pk);
         }
     }
 
     /**
      * Test compacting and removing empty entries
      *
      * @throws Exception on errors
      */
     @Test
     public void compactTest() throws Exception {
         FxContent org = co.initialize(TEST_TYPE);
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
         Assert.assertFalse(org.containsValue("/TestProperty4[2]"));
         assertEquals(org.getValue("/TestGroup1/TestProperty1_3[1]"), testValue3);
         Assert.assertFalse(org.containsValue("/TestGroup1/TestProperty1_3[4]"));
     }
 
     /**
      * Check default value handling
      *
      * @throws Exception on errors
      */
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
             FxContent c = co.initialize(TEST_TYPE);
             assertEquals(c.getValue(defaultXPath), DEFAULT_STRING);
             c.setValue(req1, tmpValue);
             c.setValue(req2, tmpValue);
             pk = co.save(c);
             FxContent test = co.load(pk);
             assertEquals(test.getValue(defaultXPath), DEFAULT_STRING);
             test.setValue(defaultXPath, tmpValue);
             assertEquals(test.getValue(defaultXPath), tmpValue);
             co.save(test);
             test = co.load(test.getPk());
             assertEquals(test.getValue(defaultXPath), tmpValue, "Default value should have been overwritten.");
             test.remove(defaultXPath);
             Assert.assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (before save)");
             co.save(test);
             test = co.load(test.getPk());
             Assert.assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (after save with a default multiplicity of 1)");
 
             FxPropertyAssignment pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             ass.save(pa.asEditable().setDefaultMultiplicity(0), false);
             pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             assertEquals(pa.getDefaultMultiplicity(), 0);
             test = co.load(test.getPk());
             Assert.assertFalse(test.containsValue(defaultXPath), "Default value should have been removed (after save with a default multiplicity of 0)");
         } finally {
             if (pk != null)
                 co.remove(pk);
             FxPropertyAssignment pa = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + defaultXPath);
             //reset the def. multiplicity to 1
             ass.save(pa.asEditable().setDefaultMultiplicity(1), false);
         }
     }
 
     @Test
     public void removeXPathTest() throws FxApplicationException {
         FxContent test = co.initialize(TEST_TYPE);
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         FxString testValue3 = new FxString("Hello world3");
 
         //test with property
         test.setValue("/TestProperty3[1]", testValue1);
         test.setValue("/TestProperty3[2]", testValue2);
         test.setValue("/TestProperty3[3]", testValue3);
         test.remove("/TestProperty3[2]");
         Assert.assertFalse(test.containsValue("/TestProperty3[3]"));
         assertEquals(test.getValue("/TestProperty3[2]"), testValue3, "Propery gap should have been closed and [3] is now [2]");
         test.remove("/TestProperty3");
         Assert.assertFalse(test.containsValue("/TestProperty4[3]"));
 
         //test with group
         test.setValue("/TestGroup1/TestGroup1_2[1]/TestProperty1_2_1[1]", testValue1);
         test.setValue("/TestGroup1/TestGroup1_2[2]/TestProperty1_2_1[1]", testValue2);
         test.setValue("/TestGroup1/TestGroup1_2[3]/TestProperty1_2_1[1]", testValue3);
         test.remove("/TestGroup1/TestGroup1_2[2]");
         Assert.assertFalse(test.containsValue("/TestGroup1/TestGroup1_2[3]/TestProperty1_2_1[1]"));
         assertEquals(test.getValue("/TestGroup1/TestGroup1_2[2]/TestProperty1_2_1[1]"), testValue3, "Group gap should have been closed and [3] is now [2]");
         test.remove("/TestGroup1/TestGroup1_2");
         Assert.assertFalse(test.containsValue("/TestGroup1/TestGroup1_2[1]/TestProperty1_2_1[1]"));
     }
 
     /**
      * Test the maxLength property f. contents
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void maxLengthTest() throws FxApplicationException {
 
         long typeId = type.save(FxTypeEdit.createNew("MAXLENGTHTEST"));
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
             FxContent c = co.initialize(typeId);
             c.setValue("/LENGTHPROP1", new FxString(false, "1234"));
             try {
                 co.save(c);
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
             type.remove(typeId);
         }
     }
 
     @Test
     public void removeAssignedACLTest() throws FxApplicationException {
         FxContent test = co.initialize(FxType.CONTACTDATA);
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
             co.remove(test.getPk());
         }
     }
 
     @Test
     public void nullAclAssignmentTest() throws FxApplicationException {
         FxContent test = co.initialize(FxType.CONTACTDATA);
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
 }
