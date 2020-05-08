 package edu.msu.nscl.olog.api;
 
 import static org.junit.Assert.*;
 import static edu.msu.nscl.olog.api.LogITUtil.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
 
 import static edu.msu.nscl.olog.api.LogBuilder.*;
 import static edu.msu.nscl.olog.api.TagBuilder.*;
 import static edu.msu.nscl.olog.api.LogbookBuilder.*;
 import static edu.msu.nscl.olog.api.PropertyBuilder.*;
 
 /**
  * This case consists of tests for operations which use update.
  * 
  * @author shroffk
  * 
  */
 public class UpdateIT {
 
 	private static OlogClient client;
 
 	private static final String uniqueString = String.valueOf(System.currentTimeMillis());
 	private static TagBuilder defaultTag = tag("defaultTag" + uniqueString);
 	private static LogbookBuilder defaultLogbook = logbook("defaultLogbook" + uniqueString).owner("me");
 
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
 				.create();
 		client.set(defaultLogbook);
 		client.set(defaultTag);
 	}
 
 	@AfterClass
 	public static void afterClass() {
 		client.deleteLogbook(defaultLogbook.build().getName());
 		client.deleteTag(defaultTag.build().getName());
 		client.delete(client.findLogsBySearch("testlog*"));
 	}
 
 	/**
 	 * update a single log
 	 */
 	@Test
 	public void updateLog() {
 		Log testlog1 = null;
 		Log updatedTestLog1 = null;
 		try {
 			testlog1 = client.set(log().description("testlog1_updateLog")
 					.appendDescription("test log").level("Info")
 					.appendToLogbook(defaultLogbook));
 			assertTrue("created testlog already contains testTag", client
 					.findLogsBySearch(testlog1.getDescription()).iterator()
 					.next().getTag(defaultTag.build().getName()) == null);
 			updatedTestLog1 = client
 					.update(log(testlog1).appendTag(defaultTag));
 			assertTrue("failed to update log with tag", client
 					.findLogsBySearch(testlog1.getDescription()).iterator()
 					.next().getTag(defaultTag.build().getName()) != null);
 		} finally {
 			if (testlog1 != null)
 				client.delete(testlog1.getId());
 			if (updatedTestLog1 != null)
 				client.delete(updatedTestLog1.getId());
 		}
 	}
 
 	@Test
 	public void updateLogs() {
 	    	Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create test logs
 			testLog1 = client.set(log().description("testLog1_updateLogs "+ uniqueString)
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log().description("testLog2_updateLogs " + uniqueString)
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			
 			assertTrue("failed to create test logs", client
 					.findLogsBySearch("*"+uniqueString+"*").size() == 2);
 			// add default tag testLog1 & testLog2
 			Collection<LogBuilder> logBuilders = new ArrayList<LogBuilder>();
 			logBuilders.add(log(testLog1).appendDescription(" Edited "));
 			logBuilders.add(log(testLog2).appendDescription(" Edited "));
			client.update(logBuilders);
 			// check if the logs were updated
 			Collection<Log> QueryResult = client.findLogsBySearch("*Edited*");
 			assertTrue("failed to update a group of logs(testLog1, testLog2)..expected 2 found "+QueryResult.size(), QueryResult.size() == 2);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 	}
 
 	@Test
 	public void updateTag() {
 
 	}
 
 	@Test
 	public void updateTag2Log() {
 		TagBuilder testTag = tag("testTag1");
 		Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create test logs
 			testLog1 = client.set(log().description("testLog1_updateTag2Log")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log().description("testLog2_updateTag2Log")
 					.description("test log").appendToLogbook(defaultLogbook)
 					.level("Info"));
 			// create a Tag with no logs
 			client.set(testTag);
 			assertTrue("failed to create an empty tag testTag1", client
 					.findLogsByTag(testTag.build().getName()).size() == 0);
 			// add testLog1 to testTag
 			client.update(testTag, testLog1.getId());
 			// check if the log was updated with the logbook
 			// TODO add check
 			assertTrue(
 					"failed to update testLog1 with testTag1",
 					checkEqualityWithoutID(
 							client.findLogsByTag(testTag.build().getName()),
 							testLog1));
 			// add testLog2 to testTag
 			client.update(testTag, testLog2.getId());
 			// check if the testLog2 was updated with the logbook
 			assertTrue(
 					"failed to update testLog2 with testTag1",
 					checkEqualityWithoutID(
 							client.findLogsByTag(testTag.build().getName()),
 							testLog2));
 			// check testLog1 was not affected by the update
 			assertTrue(
 					"failed to update testLog1 with testTag1",
 					checkEqualityWithoutID(
 							client.findLogsByTag(testTag.build().getName()),
 							testLog1));
 		} catch (Exception ex) {
 			fail(ex.getMessage());
 		} finally {
 			client.deleteTag(testTag.build().getName());
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 	}
 
 	@Test
 	public void updateTag2Logs() {
 		TagBuilder testTag = tag("testTag2");
 		Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create test logs
 			testLog1 = client.set(log().description("testLog1_updateTag2Logs")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log().description("testLog2_updateTag2Logs")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			// create a Tag with no logs
 			client.set(testTag);
 			assertTrue("failed to create an empty tag testTag2", client
 					.findLogsByTag(testTag.build().getName()).size() == 0);
 			// add testLog1 & testLog2 to testTag
 			Collection<Log> logs = new ArrayList<Log>();
 			logs.add(testLog1);
 			logs.add(testLog2);
 			client.update(testTag, LogUtil.getLogIds(logs));
 			// check if the logs were added to the testTag
 			assertTrue(
 					"failed to update a group of logs(testLog1, testLog2) with testTag2",
 					checkEqualityWithoutID(
 							client.findLogsByTag(testTag.build().getName()),
 							logs));
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			client.deleteTag(testTag.build().getName());
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 
 	}
 
 	@Test
 	public void updateLogbook() {
 
 	}
 
 	@Test
 	public void updateLogbook2Log() {
 		LogbookBuilder logbook = logbook("testLogbook1").owner("me");
 		Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create test logs
 			testLog1 = client.set(log()
 					.description("testLog1_updateLogbook2Log")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log()
 					.description("testLog2_updateLogbook2Log")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			// create a logbook with no logs
 			client.set(logbook);
 			assertTrue("failed to create an empty logbook", client
 					.findLogsByLogbook(logbook.build().getName()).size() == 0);
 			// add testLog1 to logbook
 			client.update(logbook, testLog1.getId());
 			// check if the log was updated with the logbook
 			assertTrue(
 					"failed to update testLog1 with testlogbook1",
 					checkEqualityWithoutID(
 							client.findLogsByLogbook(logbook.build().getName()),
 							testLog1));
 			// add testLog2 to logbook
 			client.update(logbook, testLog2.getId());
 			// check if the testLog2 was updated with the logbook
 			assertTrue(
 					"failed to update testLog2 with testLogbook1",
 					checkEqualityWithoutID(
 							client.findLogsByLogbook(logbook.build().getName()),
 							testLog2));
 			// check if testLog1 was not affected by the update
 			assertTrue(
 					"failed to update testLog1 with testlogbook1",
 					checkEqualityWithoutID(
 							client.findLogsByLogbook(logbook.build().getName()),
 							testLog1));
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			client.deleteLogbook(logbook.build().getName());
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 	}
 
 	@Test
 	public void updateLogbook2Logs() {
 		LogbookBuilder logbook = logbook("testLogbook2").owner("me");
 		Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create a logbook with no logs
 			client.set(logbook);
 			assertTrue("failed to create an empty logbook", client
 					.findLogsByLogbook(logbook.build().getName()).size() == 0);
 			// create test logs
 			testLog1 = client.set(log()
 					.description("testLog1_updateLogbook2Logs")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log()
 					.description("testLog2_updateLogbook2Logs")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			// add testLog1 & testLog2 to logbook
 			Collection<Log> logs = new ArrayList<Log>();
 			logs.add(testLog1);
 			logs.add(testLog2);
 			client.update(logbook, LogUtil.getLogIds(logs));
 			// check if the logs were added to the logbook
 			assertTrue(
 					"failed to update a group of logs(testLog1, testLog2) with testLogbook2",
 					checkEqualityWithoutID(
 							client.findLogsByLogbook(logbook.build().getName()),
 							logs));
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			client.deleteLogbook(logbook.build().getName());
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 	}
 
 	/**
 	 * Update an existing property with a new attribute
 	 */
 	@Test
 	public void updateProperty() {
 		PropertyBuilder property = property("testProperty" + uniqueString)
 			.attribute("orignalAttribute");
 		try {
 			client.set(property);
 			Property searchedProperty = client.getProperty(property.build()
 					.getName());
 			assertTrue("failed to set the testPropertyWithAttibutes",
 					searchedProperty.getName().equalsIgnoreCase(
 							property.build().getName())
 							&& searchedProperty.getAttributes().containsAll(
 									property.build().getAttributes()));
 			property.attribute("newAtrribute", "");
 			client.update(property);
 			searchedProperty = client.getProperty(property.build().getName());
 			assertTrue(
 					"failed to set the testPropertyWithAttibutes",
 					searchedProperty.getName().equalsIgnoreCase(
 							property.build().getName())
 							&& searchedProperty.getAttributes().containsAll(
 									property.build().getAttributes()));
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		} finally {
 			client.deleteProperty(property.build().getName());
 		}
 	}
 
 	@Test
 	public void updateProperty2log() {
 		PropertyBuilder testProperty = property("testProperty1_"+uniqueString).attribute(
 				"testAttribute").attribute("testAttribute2");
 		Log testLog1 = null;
 		Log testLog2 = null;
 		try {
 			// create test logs
 			testLog1 = client.set(log()
 					.description("testLog1_updateProperty2Log")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			testLog2 = client.set(log()
 					.description("testLog2_updateProperty2Log")
 					.appendDescription("test log")
 					.appendToLogbook(defaultLogbook).level("Info"));
 			// create a Property with no logs
 			client.set(testProperty);
 			assertTrue("failed to create an empty tag testTag1",
 					client.findLogsByProperty(testProperty.build().getName(),
 							"testAttribute", "*").size() == 0);
 			// add testLog1 to testProperty
 			testProperty.attribute("testAttribute", "testAttributeValue");
                         testProperty.attribute("testAttribute2", "testAttributeValue2");
 			client.update(testProperty, testLog1.getId());
 			// check if the log was updated with the testProperty
 			// TODO add check
 			Collection<Log> queryResult = client.findLogsByProperty(
 					testProperty.build().getName(), "testAttribute", "*");
 			assertTrue("failed to update testLog1 with testProperty",
 					checkEqualityWithoutID(queryResult, testLog1));
                         //check if the log has both attributes in the property
                         assertTrue("testProperty does not has both attributes",
 					queryResult.iterator().next().getProperty("testProperty1_"+uniqueString)
 					.iterator().next().getAttributes().size()== 2);
 			// add testLog2 to testProperty
 			client.update(testProperty, testLog2.getId());
 			// check if the testLog2 was updated with the testProperty
 			queryResult = client.findLogsByProperty(testProperty.build()
 					.getName(), "testAttribute", "*");
 			assertTrue("failed to update testLog2 with testProperty",
 					checkEqualityWithoutID(queryResult, testLog2));
 			// check testLog1 was not affected by the update
 			assertTrue(
 					"failed to update testLog1 with testProperty",
 					checkEqualityWithoutID(client.findLogsByProperty(
 							testProperty.build().getName(), "testAttribute",
 							"*"), testLog1));
 		} catch (Exception ex) {
 			fail(ex.getMessage());
 		} finally {
 			client.deleteProperty(testProperty.build().getName());
 			if (testLog1 != null)
 				client.delete(testLog1.getId());
 			if (testLog2 != null)
 				client.delete(testLog2.getId());
 		}
 	}
 
 	/**
 	 * This seems like an incorrect equality test but don't know how to test if
 	 * the log I am sending has indeed been set/added since I don't have the id
 	 * in the builder
 	 * 
 	 * @param returnedLogs
 	 * @param setLogs
 	 * @return
 	 */
 
 	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
 			Collection<Log> setLogs) {
 		Collection<String> logSubjects = LogUtil
 				.getLogDescriptions(returnedLogs);
 		for (Log log : setLogs) {
 			if (!logSubjects.contains(log.getDescription()))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This seems like an incorrect equality test but don't know how to test if
 	 * the log I am sending has indeed been set/added since I don't have the id
 	 * in the builder
 	 * 
 	 * @param returnedLogs
 	 * @param setLogs
 	 * @return
 	 */
 	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs, LogBuilder setLog) {
 		return checkEqualityWithoutID(returnedLogs, setLog.build());
 	}
 
 	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
 			Log setLog) {
 		Collection<String> logSubjects = LogUtil
 				.getLogDescriptions(returnedLogs);
 		if (!logSubjects.contains(setLog.getDescription()))
 			return false;
 		return true;
 	}
 
 }
