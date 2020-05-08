 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptInfoEdit;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Tests for the scripting engine (grooooooovy)
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "scripting"})
 public class ScriptingTest {
     public static boolean allowTearDown = false;
 
     ScriptingEngine se;
     protected static FxScriptInfo loadScript = null;
     protected static FxScriptInfo removeScript = null;
 
     @BeforeSuite(dependsOnGroups = {"bootstrap"})
     public void suiteSetup() throws FxApplicationException {
         String codeLoad = "println \"[Groovy script]=== Loading Content \"+content.pk+\"(\"+environment.getType(content.getTypeId()).getName()+\") ===\"";
         String codeRemove = "println \"[Groovy script]=== Before removal of content \"+pk+\"(\"+environment.getType(securityInfo.getTypeId()).getName()+\") ===\"";
         loadScript = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.AfterContentLoad, "afterLoadTest.gy", "Test script", codeLoad);
         removeScript = EJBLookup.getScriptingEngine().createScript(FxScriptEvent.BeforeContentRemove, "beforeRemoveTest.gy", "Test script", codeRemove);
     }
 
     public static void suiteShutDown() throws Exception {
         if( !allowTearDown ) //"hack" to prevent this method to be run as normal test
             return;
         login(TestUsers.SUPERVISOR);
         if (loadScript != null)
             EJBLookup.getScriptingEngine().removeScript(loadScript.getId());
         if (removeScript != null)
             EJBLookup.getScriptingEngine().removeScript(removeScript.getId());
         logout();
     }
 
     @BeforeClass
     public void setup() throws Exception {
         se = EJBLookup.getScriptingEngine();
         login(TestUsers.SUPERVISOR);
     }
 
     @AfterClass
     public void shutdown() throws Exception {
         logout();
     }
 
     @Test
     public void simpleScript() throws Exception {
         String name = "unittest.gy";
         String desc = "unit test script";
         String code = "return \"hello world\";";
         Object result = "hello world";
 
         FxScriptInfo si = se.createScript(FxScriptEvent.Manual, name, desc, code);
         assert si != null : "No FxScriptInfo returned!";
         assert si.getId() > 0 : "Invalid script id";
         assert si.getName().equals(name) : "Invalid name";
         assert si.getDescription().equals(desc) : "Invalid description";
         assert si.getEvent().equals(FxScriptEvent.Manual) : "Invalid script type";
         assert se.runScript(si.getId(), new FxScriptBinding()).getResult().equals(result) : "Invalid result from script!";
         se.removeScript(si.getId());
     }
 
     @Test
     public void scriptActivation() throws Exception {
        FxScriptInfo si = se.createScript(FxScriptEvent.Manual,  "manualTestScript.gy", "manualTestScript", "return \"done\";");
         se.runScript(si.getId());
         FxScriptInfoEdit inactive = si.asEditable();
         inactive.setActive(false);
         se.updateScriptInfo(inactive);
         try {
            Assert.assertEquals(se.runScript(si.getId()).getResult(), null, "Inactive scripts must not be runnable!");
        } finally {
             se.removeScript(si.getId());
         }
     }
 
     @Test
     public void scriptAssignmentMapping() throws Exception {
         long typeId = EJBLookup.getTypeEngine().save(FxTypeEdit.createNew("AS_SCRIPTING", new FxString("Assignment scripting test type"),
                 CacheAdmin.getEnvironment().getACL(ACL.Category.STRUCTURE.getDefaultId()), null));
         EJBLookup.getAssignmentEngine().createProperty(
                 typeId,
                 FxPropertyEdit.createNew(
                         "A",
                         new FxString(true, FxLanguage.ENGLISH, "A"),
                         new FxString(true, FxLanguage.ENGLISH, "A"),
                         new FxMultiplicity(0, 5),
                         CacheAdmin.getEnvironment().getACL(ACL.Category.STRUCTURE.getDefaultId()),
                         FxDataType.String1024).setMultiLang(false),
                 "/");
         EJBLookup.getAssignmentEngine().createProperty(
                 typeId,
                 FxPropertyEdit.createNew(
                         "B",
                         new FxString(true, FxLanguage.ENGLISH, "B"),
                         new FxString(true, FxLanguage.ENGLISH, "B"),
                         new FxMultiplicity(0, 5),
                         CacheAdmin.getEnvironment().getACL(ACL.Category.STRUCTURE.getDefaultId()),
                         FxDataType.String1024).setMultiLang(false),
                 "/");
         FxType type = CacheAdmin.getEnvironment().getType(typeId);
         List<FxScriptInfo> scripts = new ArrayList<FxScriptInfo>(10);
         try {
             scripts.add(createScript(type.getAssignment("/A"), FxScriptEvent.BeforeAssignmentDataCreate));
             scripts.add(createScript(type.getAssignment("/A"), FxScriptEvent.AfterAssignmentDataCreate));
             scripts.add(createScript(type.getAssignment("/A"), FxScriptEvent.BeforeAssignmentDataDelete));
             scripts.add(createScript(type.getAssignment("/A"), FxScriptEvent.AfterAssignmentDataDelete));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.BeforeDataChangeAdd));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.BeforeDataChangeUpdate));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.BeforeDataChangeDelete));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.AfterDataChangeAdd));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.AfterDataChangeUpdate));
             scripts.add(createScript(type.getAssignment("/B"), FxScriptEvent.AfterDataChangeDelete));
 
             FxString data = new FxString(false, "data");
             FxString data2 = new FxString(false, "data2");
             ContentEngine ce = EJBLookup.getContentEngine();
             FxContent co = ce.initialize(typeId);
             FxPK pk = ce.save(co);
             co = ce.load(pk);
             co.setValue("/B[1]", data);
             System.out.println("=>add B[1]");
             ce.save(co);
             co = ce.load(pk);
             FxString tmp = (FxString) co.getValue("/B[1]");
             tmp.setTranslation(FxLanguage.GERMAN, "data de");
             System.out.println("=>update B[1]");
             ce.save(co);
             co = ce.load(pk);
             co.setValue("/B[2]", data2);
             System.out.println("=>add B[2]");
             ce.save(co);
             co = ce.load(pk);
             co.remove("/B[2]");
             System.out.println("=>delete B[2]");
             ce.save(co);
             EJBLookup.getContentEngine().remove(pk);
         } finally {
             EJBLookup.getTypeEngine().remove(typeId);
             for (FxScriptInfo si : scripts)
                 se.removeScript(si.getId());
         }
 
     }
 
     private FxScriptInfo createScript(FxAssignment asssignment, FxScriptEvent event) throws FxApplicationException {
         String data = "unknown";
         if (event.name().startsWith("BeforeAssignment"))
             data = "assignment";
         else if (event.name().startsWith("AfterAssignment"))
             data = "assignment";
         else if (event.name().startsWith("BeforeDataChange"))
             data = "change";
         else if (event.name().startsWith("AfterDataChange"))
             data = "change";
         FxScriptInfo si = se.createScript(event, "Test" + event.name() + ".gy",
                 "",
                 "println \"This is [" + event.name() + "] registered for Assignment [" + asssignment.getXPath() + "] - I am called for ${" + data + "}\"");
         se.createAssignmentScriptMapping(si.getId(), asssignment.getId(), true, true);
         return si;
     }
 }
