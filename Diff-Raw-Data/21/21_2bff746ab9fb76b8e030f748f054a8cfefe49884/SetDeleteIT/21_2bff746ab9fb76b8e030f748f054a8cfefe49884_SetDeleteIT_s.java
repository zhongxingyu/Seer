 package edu.msu.nscl.olog.api;
 
 import static edu.msu.nscl.olog.api.LogBuilder.log;
 import static edu.msu.nscl.olog.api.LogUtil.getLogSubjects;
 import static edu.msu.nscl.olog.api.LogUtil.toLogs;
 import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
 import static edu.msu.nscl.olog.api.TagBuilder.tag;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.apache.jackrabbit.webdav.DavException;
 import org.hsqldb.lib.HashSet;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
 
 public class SetDeleteIT {
 
 	private static OlogClient client;
 
 	private static String logOwner;
 	private static String logbookOwner;
 	private static String tagOwner;
 	private static String propertyOwner;
 
 	// Default logbook for all tests
 	private static LogbookBuilder defaultLogBook;
 
 	// Default tag for all tests
 	private static TagBuilder defaultTag;
 	// A default set of logs
 	private static LogBuilder defaultLog1;
 	private static LogBuilder defaultLog2;
 	private static LogBuilder defaultLog3;
 	// default log sets
 	private static Collection<LogBuilder> logs1;
 	private static Collection<LogBuilder> logs2;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
 				.create();
 		// these should be read from some properties files so that they can be
 		// setup for the corresponding intergration testing enviorment.
 		logOwner = "me";
 		logbookOwner = "me";
 		tagOwner = "me";
 		propertyOwner = "me";
 
 		// Add a default logbook
 		defaultLogBook = logbook("DefaultLogBook").owner(logbookOwner);
 		client.set(defaultLogBook);
 		// Add a default Tag
 		defaultTag = tag("defaultTag");
 		client.set(defaultTag);
 		// define the default logs
 		defaultLog1 = log("defaulLog1").description("some details")
 				.level("Info").in(defaultLogBook);
 		defaultLog2 = log("defaultLog2").description("some details")
 				.level("Info").in(defaultLogBook);
 		defaultLog3 = log("defaultLog3").description("some details")
 				.level("Info").in(defaultLogBook);
 		// define default sets
 		logs1 = new ArrayList<LogBuilder>();
 		logs1.add(defaultLog1);
 		logs1.add(defaultLog2);
 
 		logs2 = new ArrayList<LogBuilder>();
 		logs2.add(defaultLog3);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		client.deleteLogbook(defaultLogBook.build().getName());
 		client.deleteTag(defaultTag.toXml().getName());
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * create(set) and delete a logbook
 	 * 
 	 */
 	@Test
 	public void setLogbookTest() {
 		LogbookBuilder logbook = logbook("testLogBook").owner(logbookOwner);
 		try {
 			// set a logbook
 			// list all logbook
 			client.set(logbook);
 			assertTrue("failed to set the testLogBook", client.listLogbooks()
 					.contains(logbook.build()));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			// delete a logbook
 			client.deleteLogbook(logbook.build().getName());
 			assertFalse("failed to clean up the testLogbook", client
 					.listLogbooks().contains(logbook.build()));
 		}
 	}
 
 	/**
 	 * create(set) and delete a tag
 	 */
 	@Test
 	public void setTagTest() {
 		TagBuilder tag = tag("testTag");
 		try {
 			// set a tag
 			// list all tag
 			client.set(tag);
 			assertTrue("failed to set the testTag",
 					client.listTags().contains(tag.build()));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			// delete a tag
 			client.deleteLogbook(tag.build().getName());
 			assertFalse("failed to clean the testTag", client.listTags()
 					.contains(tag.build()));
 		}
 	}
 
 	/**
 	 * create(set) and delete a single log
 	 * 
 	 */
//	@Test
 	public void setLogTest() {
 		LogBuilder log = log("testLog").description("some details")
 				.level("Info").in(defaultLogBook);
 
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("search", "testLog");
 		Log result = null;
 
 		try {
 			// set a log
 			result = client.set(log);
 			// check if the returned id is the same
 			Collection<Log> queryResult = client.findLogs(map);
 			assertTrue("The returned id is not valid",
 					queryResult.contains(result));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			// delete a log
 			client.delete(log(result));
 			assertFalse("Failed to clean up the testLog", client.findLogs(map)
 					.contains(result));
 		}
 
 	}
 
 	/**
 	 * create(set) and delete a group of logs
 	 * 
 	 */
//	@Test
 	public void setLogsTest() {
 		LogBuilder log1 = log("testLog1").description("some details")
 				.level("Info").in(defaultLogBook);
 		LogBuilder log2 = log("testLog2").description("some details")
 				.level("Info").in(defaultLogBook);
 		Collection<LogBuilder> logs = new ArrayList<LogBuilder>();
 		logs.add(log1);
 		logs.add(log2);
 
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("search", "testLog*");
 		Collection<Log> result = null;
 		Collection<Log> queryResult;
 
 		try {
 			// set a group of channels
 			result = client.set(logs);
 			// check the returned logids match the number expected
 			assertTrue("unexpected return after creation of log entries",
 					result.size() == logs.size());
 			// query to check if the logs are indeed in olog
 			queryResult = client.findLogs(map);
 			assertTrue("set logs not found in the olog db ",
 					checkEqualityWithoutID(queryResult, logs));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			// delete a group of logs
 			for (Log log : result) {
 				client.delete(log(log));
 			}
 			queryResult = client.findLogs(map);
 			for (Log log : result) {
 				assertFalse("Failed to clean up the group of test logs",
 						queryResult.contains(log));
 			}
 		}
 	}
 
 //	@Test
 	public void addAttachment2Log() {
 		Log testLog = null;
 		File f = null;
 		try {
 			// create a test log
 			testLog = client.set(log("testLog_addAttachment2Log")
 					.description("test log").in(defaultLogBook).level("Info"));
 			// create a test file
 			f = new File("testfile.txt");
 			if (!f.exists()) {
 				FileWriter fwrite = new FileWriter(f);
 				fwrite.write("This is test file");
 				fwrite.flush();
 				fwrite.close();
 			}
 			client.add(f, testLog.getId());
 			assertTrue(client.getAttachments(testLog.getId()).size() == 1);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			if (testLog != null) {
 				client.delete(testLog.getId());
 				client.delete(f.getName(), testLog.getId());
 			}
 			if (f.exists()) {
 				boolean success = f.delete();
 				assertTrue("attachment File clean up failed", success);
 			}
 		}
 	}
 
	@Test
 	public void attachImageFileToLogId() throws IOException, DavException {
 		Log testLog = null;
 		File f = null;
 		try {
 			f = new File("the_homercar.jpg");
 			testLog = client.set(log("test_attachImageFileToLogId")
 					.description("test log").level("Info")
 					.in(defaultLogBook));
 			client.add(f, testLog.getId());
 			assertTrue(client.getAttachments(testLog.getId()).size() == 1);
 		} finally {
 			if (testLog != null) {
 				client.delete(testLog.getId());
 				client.delete("the_homercar.jpg", testLog.getId());
 			}
 		}
 	}
 
 	@Test
 	public void test() {
 		File f = new File("file2.txt");
 		try {
 			if (!f.exists()) {
 				FileWriter fwrite = new FileWriter(f);
 				fwrite.write("This is test file");
 				fwrite.flush();
 				fwrite.close();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		boolean success = f.delete();
 		assertTrue("attachment File clean up failed", success);
 	}
 
 	/*
 	 * These Tests are to test the operations of attaching tags, logbooks and
 	 * properties to logs.
 	 */
 
 	/**
 	 * Destructive operation which removes the tag from all other logs and only
 	 * adds it to the specified log
 	 */
 //	@Test
 	public void setTag2LogTest() {
 
 		String tagName = defaultTag.toXml().getName();
 
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("tag", tagName);
 		Log setLogId = null;
 
 		try {
 			setLogId = client.set(defaultLog3);
 			assertTrue("failed to set the log",
 					setLogId != null && setLogId.getId() != null);
 			// Set Tag on single Log
 			client.set(defaultTag, setLogId.getId());
 			Collection<Log> result = client.findLogs(map);
 			assertTrue("failed to set tag on log " + setLogId.getId() + "("
 					+ setLogId.getSubject() + ")",
 					checkEqualityWithoutID(result, defaultLog3));
 		} catch (Exception e) {
 			fail("setTag2Log" + e.getMessage());
 		} finally {
 			client.delete(setLogId.getId());
 		}
 	}
 
 //	@Test
 	public void setTag2LogsTest() {
 		String tagName = defaultTag.toXml().getName();
 
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("tag", tagName);
 		Collection<Log> setLogsIds = null;
 		Collection<Log> queryResult;
 
 		try {
 			// Set Tag on multiple Logs
 			setLogsIds = client.set(logs1);
 			client.set(defaultTag, LogUtil.getLogIds(setLogsIds));
 			// check if the Tags was added
 			queryResult = client.findLogs(map);
 			assertTrue(
 					"Failed to add " + tagName + " to "
 							+ LogUtil.getLogIds(setLogsIds).toString(),
 					checkEqualityWithoutID(queryResult, logs1));
 		} catch (Exception e) {
 			fail("setTag2Log" + e.getMessage());
 		} finally {
 			client.delete(setLogsIds);
 		}
 	}
 
 	/**
 	 * Test destructive set on a logbook, the logbook should be added to only
 	 * those logs specified and removed from all others
 	 */
 //	@Test
 	public void setLogbook2logTest() {
 		LogbookBuilder testLogBook = logbook("testLogBook").owner(logbookOwner);
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("logbook", "testLogBook");
 		Collection<Log> queryResult;
 		Collection<Log> setLogs1 = null;
 		Collection<Log> setLogs2 = null;
 
 		try {
 			setLogs1 = client.set(logs1);
 			setLogs2 = client.set(logs2);
 			assertTrue("setLogs2 should only have a single log",
 					setLogs2.size() == 1);
 			// create a test logbook
 			client.set(testLogBook);
 			assertTrue("failed to create testlogbook with no entires.", client
 					.findLogs(map).size() == 0);
 			// update a logbook with a new entry
 			client.set(testLogBook, LogUtil.getLogIds(setLogs2).iterator()
 					.next());
 			queryResult = client.findLogs(map);
 			assertTrue("failed to set a logbook onto a log",
 					checkEqualityWithoutID(queryResult, logs2));
 
 		} catch (Exception e) {
 
 		} finally {
 			client.deleteLogbook(testLogBook.build().getName());
 			client.delete(setLogs1);
 			client.delete(setLogs2);
 		}
 	}
 
 	@Test
 	public void setLogbook2LogsTest() {
 
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
 			Collection<LogBuilder> setLogs) {
 		Collection<String> logSubjects = LogUtil.getLogSubjects(returnedLogs);
 		for (LogBuilder logBuilder : setLogs) {
 			if (!logSubjects.contains(logBuilder.build().getSubject()))
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
 	private static boolean checkEqualityWithoutID(Collection<Log> returnedLogs,
 			LogBuilder setLog) {
 		Collection<String> logSubjects = LogUtil.getLogSubjects(returnedLogs);
 		if (!logSubjects.contains(setLog.build().getSubject()))
 			return false;
 		return true;
 	}
 
 }
