 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.jython.scriptcontroller.logging;
 
 import static org.junit.Assert.fail;
 import static org.junit.Assert.*;
 import gda.TestHelpers;
 import gda.factory.FactoryException;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.junit.Test;
 
 public class MessageBeanTest {
 
 	private String testScratchDirectoryName;
 
 	public void setUp(String testName) throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(MessageBeanTest.class, testName, true);
 	}
 
 	@Test
 	public void TestDatabaseCreation() throws Exception {
 		setUp("TestDatabaseCreation");
 		createDatabase();
 
 		// test database has been made
 		File url = new File(testScratchDirectoryName + "TestMessageBean/seg0");
 		if (!url.exists()) {
 			fail("Database was not created");
 		}
 	}
 
 	private LoggingScriptController createDatabase() {
 		// create the controller
 		LoggingScriptController controller = new LoggingScriptController();
 		controller.setMessageClassToLog(TestMessageBean.class);
 		controller.setDirectory(testScratchDirectoryName);
 		try {
 			controller.configure();
 		} catch (FactoryException e) {
 			fail(e.getMessage());
 		}
 		return controller;
 	}
 
 	@Test
 	public void TestAddScript() throws Exception {
 		setUp("TestAddScript");
 		LoggingScriptController controller = createDatabase();
 
 		String scriptName = "my_daq_script";
 		String id = LoggingScriptController.createUniqueID(scriptName);
 
 		TestMessageBean msg1 = new TestMessageBean();
 		msg1.setUniqueID(id);
 		msg1.setPercentComplete("10%");
 		msg1.setProgressMessage("Just started");
 		msg1.setRunName("My Fav Data Collection Script");
 		msg1.setUserComment("My comment");
 
 		controller.update(this.getClass(), msg1);
 
 		TestMessageBean msg2 = new TestMessageBean();
 		msg2.setUniqueID(id);
 		msg2.setPercentComplete("50%");
 		msg2.setProgressMessage("Halfway!");
 
 		controller.update(this.getClass(), msg2);
 	}
 
 	@Test
 	public void TestRetrieveSpecificRunDetails() throws Exception {
 		setUp("TestRetrieveSpecificRunDetails");
 		LoggingScriptController controller = createDatabase();
 
 		String scriptName = "my_daq_script";
 		String id = LoggingScriptController.createUniqueID(scriptName);
 
 		TestMessageBean msg1 = new TestMessageBean();
 		msg1.setUniqueID(id);
 		msg1.setPercentComplete("10%");
 		msg1.setProgressMessage("Just started");
 		msg1.setRunName("My Fav Data Collection Script");
 		msg1.setUserComment("My comment");
 		controller.update(this.getClass(), msg1);
 
 		ScriptControllerLogResultDetails details = controller.getDetails(id);
 		HashMap<String, String> values = details.getDetails();
 
 		if (!(values.get("Complete").equals("10%")))
 			fail("'Complete' column wrong");
 		if (!(values.get("Progress").equals("Just started")))
 			fail("'Progress' column wrong");
 		if (!(values.get("Comment").equals("My comment")))
 			fail("'Comment' column wrong");
 		if (!(values.get("Run name").equals("My Fav Data Collection Script")))
 			fail("'Run name' column wrong");
 
 	}
 
 	@Test
 	public void TestRetrieveAllRuns() throws Exception {
 		setUp("TestRetrieveAllRuns");
 		LoggingScriptController controller = createDatabase();
 
 		String id = "unique_id1";
 		TestMessageBean msg1 = new TestMessageBean();
 		msg1.setUniqueID(id);
 		msg1.setPercentComplete("10%");
 		msg1.setProgressMessage("Just started");
 		msg1.setRunName("My Fav Data Collection Script");
 		msg1.setUserComment("My comment");
 		controller.update(this.getClass(), msg1);
 		TestMessageBean msg2 = new TestMessageBean();
 		msg2.setUniqueID(id);
 		msg2.setPercentComplete("100%");
 		msg2.setProgressMessage("Done!");

 		String id2 = "unique_id2";
 		TestMessageBean msg3 = new TestMessageBean();
 		msg3.setUniqueID(id2);
 		msg3.setPercentComplete("10%");
 		msg3.setProgressMessage("Just started");
 		msg3.setRunName("My Fav Data Collection Script");
 		msg3.setUserComment("Next scan");
 		controller.update(this.getClass(), msg3);
 		TestMessageBean msg4 = new TestMessageBean();
 		msg4.setUniqueID(id2);
 		msg4.setPercentComplete("50%");
 		msg4.setProgressMessage("Halfway");
 		controller.update(this.getClass(), msg4);
 
 		ScriptControllerLogResults[] allResults = controller.getTable();
 		assertEquals(2, allResults.length);
 		assertEquals(msg3.getName(), allResults[0].getScriptName());
 		assertEquals(msg4.getName(), allResults[1].getScriptName());
 	}
 
 	@Test
 	public void TestRetrieveLatestRun() throws Exception {
 		setUp("TestRetrieveLatestRun");
 		LoggingScriptController controller = createDatabase();
 		
 		String id = "unique_id1";
 		TestMessageBean msg1 = new TestMessageBean();
 		msg1.setUniqueID(id);
 		msg1.setPercentComplete("10%");
 		msg1.setProgressMessage("Just started");
 		msg1.setRunName("My Fav Data Collection Script");
 		msg1.setUserComment("My comment");
 		controller.update(this.getClass(), msg1);
 		TestMessageBean msg2 = new TestMessageBean();
 		msg2.setUniqueID(id);
 		msg2.setPercentComplete("100%");
 		msg2.setProgressMessage("Done!");
 
 		
 		String id2 = "unique_id2";
 		TestMessageBean msg3 = new TestMessageBean();
 		msg3.setUniqueID(id2);
 		msg3.setPercentComplete("10%");
 		msg3.setProgressMessage("Just started");
 		msg3.setRunName("My Fav Data Collection Script");
 		msg3.setUserComment("Next scan");
 		controller.update(this.getClass(), msg3);
 		TestMessageBean msg4 = new TestMessageBean();
 		msg4.setUniqueID(id2);
 		msg4.setPercentComplete("50%");
 		msg4.setProgressMessage("Halfway");
 		controller.update(this.getClass(), msg4);
 		
 		ScriptControllerLogResultDetails latest = controller.getMostRecentRun();
 		
 		assertEquals("50%",latest.getDetails().get("Complete"));
 	}
 }
