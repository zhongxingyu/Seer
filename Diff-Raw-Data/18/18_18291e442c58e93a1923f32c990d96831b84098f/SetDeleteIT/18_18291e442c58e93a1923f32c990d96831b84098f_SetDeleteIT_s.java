 package edu.msu.nscl.olog.api;
 
 import static edu.msu.nscl.olog.api.LogBuilder.log;
 import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
 import static edu.msu.nscl.olog.api.TagBuilder.tag;
 import static edu.msu.nscl.olog.api.PropertyBuilder.*;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Map;
 
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
 	// Default Property for all tests
 	private static PropertyBuilder defaultProperty;
 	private static String defaultAttributeName;
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
 		// Add a default Property
 		defaultAttributeName = "defaultAttribute";
 		defaultProperty = property("defaultProperty").attribute(
 				defaultAttributeName);
 		client.set(defaultProperty);
 		// define the default logs
 		defaultLog1 = log().description("defaulLog1")
 				.description("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
 		defaultLog2 = log().description("defaultLog2")
 				.description("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
 		defaultLog3 = log().description("defaultLog3")
 				.description("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
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
 			Logbook returnLogbook = client.set(logbook);
 			assertTrue("failed to set the testLogbook, the return was null",
 					returnLogbook != null);
 			assertTrue(
 					"failed to set the testLogbook, created logbook not eq to payload",
 					returnLogbook.equals(logbook.build()));
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
 			Tag returnTag = client.set(tag);
 			assertTrue("failed to set the testTag, the return was null",
 					returnTag != null);
 			assertTrue(
 					"failed to set the testTag, tag logbook not eq to payload",
 					returnTag.equals(tag.build()));
 			assertTrue("failed to set the testTag",
 					client.listTags().contains(tag.build()));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			// delete a tag
 			client.deleteTag(tag.build().getName());
 			assertFalse("failed to clean the testTag", client.listTags()
 					.contains(tag.build()));
 		}
 	}
 
 	/**
 	 * create(set) and delete a single property
 	 */
 	@Test
 	public void setPropertyTest() {
 		PropertyBuilder property = property("testProperty");
 		try {
 			Property setProperty = client.set(property);
 			assertTrue("failed to set the testProperty, the return was null",
 					setProperty != null);
 			assertTrue(
 					"failed to set testProperty, setProperty not equal to sent property",
 					setProperty.equals(property.build()));
 			assertTrue("failed to set testProperty", client.listProperties()
 					.contains(property.build()));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			client.deleteProperty(property.build().getName());
 			Collection<Property> props = client.listProperties();
 			assertFalse("failed to clean the testProperty",
 					props.contains(property.build()));
 		}
 	}
 
 	/**
 	 * Set Property with attributes
 	 */
 	@Test
 	public void setTagwithAttributeTest() {
 		PropertyBuilder property = property("testPropertyWithAttibutes")
 				.attribute("testAttribute", "testAttributeVal");
 		try {
 			Property setProperty = client.set(property);
 			assertTrue("failed to set the testProperty, the return was null",
 					setProperty != null);
 			Property searchedProperty = client.getProperty(property.build()
 					.getName());
 			assertTrue(
 					"failed to set the testPropertyWithAttibutes",
 					searchedProperty.getName().equalsIgnoreCase(
 							property.build().getName())
 							&& searchedProperty.getAttributes().containsAll(
 									property.build().getAttributes()));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			client.deleteProperty(property.build().getName());
 		}
 	}
 
 	/**
 	 * create(set) and delete a single log
 	 * 
 	 */
 	@Test
 	public void setLogTest() {
 		LogBuilder log = log().description("testLog")
 				.appendDescription("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
 
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("search", "testLog*");
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
 	@Test
 	public void setLogsTest() {
 		LogBuilder log1 = log().description("testLog1")
 				.appendDescription("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
 		LogBuilder log2 = log().description("testLog2")
 				.appendDescription("some details").level("Info")
 				.appendToLogbook(defaultLogBook);
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
 
 	@Test
 	public void addAttachment2Log() {
 		Log testLog = null;
 		File f = null;
 		try {
 			// create a test log
 			testLog = client.set(log().description("testLog_addAttachment2Log")
 					.description("test log").appendToLogbook(defaultLogBook)
 					.level("Info"));
 			// create a test file
 			f = new File("testfile.txt");
 			if (!f.exists()) {
 				FileWriter fwrite = new FileWriter(f);
 				fwrite.write("This is test file");
 				fwrite.flush();
 				fwrite.close();
 			}
 			client.add(f, testLog.getId());
 			Collection<Attachment> attachments = client.listAttachments(testLog
 					.getId());
 			assertTrue("failed to add an attachment, expected 1 found "
 					+ attachments.size(), attachments.size() == 1);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			if (testLog != null) {
 				client.delete(testLog.getId());
 				client.delete(f.getName(), testLog.getId());
 			}
 			if (f.exists()) {
 				boolean success = f.delete();
 				// assertTrue("attachment File clean up failed", success);
 			}
 		}
 	}
 
 	@Test
 	public void attachImageFileToLogId() throws IOException {
 		Log testLog = null;
 		File f = null;
 		try {
 			f = new File("the_homercar.jpg");
 			testLog = client.set(log()
 					.description("test_attachImageFileToLogId")
 					.appendDescription("test log").level("Info")
 					.appendToLogbook(defaultLogBook));
 			client.add(f, testLog.getId());
 			Collection<Attachment> attachments = client.listAttachments(testLog
 					.getId());
 			assertTrue("failed to add attachment images, expected 1 found "
 					+ attachments.size(), attachments.size() == 1);
 			// TODO add check on returned files
 			for (Attachment attachment : attachments) {
 				File file = new File("return_" + attachment.getFileName());
 				try {
 					InputStream ip = client.getAttachment(testLog.getId(),
 							attachment);
 					OutputStream out = new FileOutputStream(file);
 					// Transfer bytes from in to out
 					byte[] buf = new byte[1024];
 					int len;
 					while ((len = ip.read(buf)) > 0) {
 						out.write(buf, 0, len);
 					}
 					ip.close();
 					out.close();
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 				}
 			}
 		} finally {
 			if (testLog != null) {
 				client.delete("the_homercar.jpg", testLog.getId());
 				client.delete(testLog.getId());
 			}
 		}
 	}
 
 	/**
 	 * create a log with a property and delete it
 	 */
 	@Test
 	public void setLogWithProperty() {
 		PropertyBuilder testProp = property("test Property").attribute(
				"attributeName");
 		LogBuilder log = log().description("testLog")
 				.appendDescription("test Log").level("Info")
 				.appendToLogbook(defaultLogBook).appendTag(defaultTag);
 		Property setProperty = null;
 		Log setLog = null;
 		try {
 			setProperty = client.set(testProp);
 			setLog = client.set(log.appendProperty(
 					property("test Property").attribute("attributeName",
							"value1")).appendProperty(
					property("test Property").attribute("attributeName",
							"value2")));
 			assertTrue("failed to set test log", setLog != null);
 			Collection<Property> properties = client
 					.findLogById(setLog.getId()).getProperty("test Property");
 			assertTrue("check if properties correctly attached",
					properties.size() == 2);
 			assertTrue(
 					"Check if the multi value properties are corectly attached.",
 					properties.containsAll(setLog.getProperty("test Property")));
 		} catch (Exception e) {
 			fail(e.getMessage());
 		} finally {
 			if (setProperty != null)
 				client.deleteProperty(setProperty.getName());
 			if (setLog != null)
 				client.delete(setLog.getId());
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
 
 	@Test
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
 	@Test
 	public void setLogbook2LogsTest() {
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
 			Collection<Long> logIds = new ArrayList<Long>();
 			for (Log log : setLogs2) {
 				logIds.add(log.getId());
 			}
 			client.set(testLogBook, logIds);
 			queryResult = client.findLogs(map);
 			// TODO : equality for queryResult.equals(setLogs2) should be true
 			assertTrue(
 					"failed to set a logbook onto a log",
 					queryResult.size() == setLogs2.size()
 							&& queryResult.containsAll(setLogs2));
 			// new set
 			for (Log log : setLogs1) {
 				logIds.add(log.getId());
 			}
 			client.set(testLogBook, logIds);
 			queryResult.clear();
 			queryResult = client.findLogs(map);
 			assertTrue("failed to set a logbook onto a log, expected "
 					+ setLogs1.size() + " found " + queryResult.size(),
 					queryResult.size() == setLogs1.size());
 		} catch (Exception e) {
 
 		} finally {
 			client.deleteLogbook(testLogBook.build().getName());
 			client.delete(setLogs1);
 			client.delete(setLogs2);
 		}
 
 	}
 
 	/**
 	 * Delete a Tag from a log without affected any other logs with the same
 	 * tag.
 	 */
 	@Test
 	public void deleteTagFromLog() {
 		Log log1 = null;
 		Log log2 = null;
 		try {
 			log1 = client.set(defaultLog1.appendTag(defaultTag));
 			log2 = client.set(defaultLog2.appendTag(defaultTag));
 			Collection<Log> queryResult = client.findLogsByTag(defaultTag
 					.build().getName());
 			assertTrue(
 					"Failed to attach defaultTag to defaultLog1 and defaultLog2",
 					queryResult.contains(log1) && queryResult.contains(log2));
 			client.delete(defaultTag, log1.getId());
 			queryResult = client.findLogsByTag(defaultTag.build().getName());
 			assertTrue("Failed to remove defaultTag from defaultLog1",
 					!queryResult.contains(log1));
 			assertTrue("Removed defaultTag from defaultLog2",
 					queryResult.contains(log2));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			if (log1 != null)
 				client.delete(log1.getId());
 			if (log2 != null)
 				client.delete(log2.getId());
 		}
 	}
 
 	@Test
 	public void deleteTagFromLogs() {
 
 	}
 
 	@Test
 	public void deletePropertyFromLog() {
 		Log log1 = null;
 		Log log2 = null;
 		try {
 			log1 = client.set(defaultLog1.appendProperty(defaultProperty
 					.attribute(defaultAttributeName, "log1")));
 			log2 = client.set(defaultLog2.appendProperty(defaultProperty
 					.attribute(defaultAttributeName, "log2")));
 			Collection<Log> queryResult = client
 					.findLogsByProperty(defaultProperty.build().getName());
 			assertTrue(
 					"Failed to attach defaultProperty to defaultLog1 and defaultLog2",
 					queryResult.contains(log1) && queryResult.contains(log2));
 			client.delete(defaultProperty, log1.getId());
 			queryResult = client.findLogsByProperty(defaultProperty.build()
 					.getName());
 			assertTrue("Failed to remove defaultProperty from defaultLog1",
 					!queryResult.contains(log1));
 			assertTrue("Removed defaultProperty from defaultLog2",
 					queryResult.contains(log2));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			if (log1 != null)
 				client.delete(log1.getId());
 			if (log2 != null)
 				client.delete(log2.getId());
 		}
 	}
 
 	@Test
 	public void deletePropertyFromLogs() {
 
 	}
 
 	/**
 	 * Delete a Logbook from a log without affected any other logs with the same
 	 * Logbook.
 	 */
 	@Test
 	public void deleteLogbookFromLog() {
 		LogbookBuilder testLogbook = logbook("testLogbook").owner("me");
 		Logbook setLogbook = null;
 		Log log1 = null;
 		Log log2 = null;
 		try {
 			setLogbook = client.set(testLogbook);
 			log1 = client.set(defaultLog1.appendToLogbook(testLogbook));
 			log2 = client.set(defaultLog2.appendToLogbook(testLogbook));
 			Collection<Log> queryResult = client.findLogsByLogbook(testLogbook
 					.build().getName());
 			assertTrue(
 					"Failed to attach testLogbook to defaultLog1 and defaultLog2",
 					queryResult.contains(log1) && queryResult.contains(log2));
 			client.delete(testLogbook, log1.getId());
 			queryResult = client.findLogsByLogbook(testLogbook.build()
 					.getName());
 			assertTrue("Failed to remove testLogbook from defaultLog1",
 					!queryResult.contains(log1));
 			assertTrue("Removed testLogbook from defaultLog2",
 					queryResult.contains(log2));
 		} catch (Exception e) {
 			fail(e.getCause().toString());
 		} finally {
 			client.deleteLogbook(setLogbook.getName());
 			if (log1 != null)
 				client.delete(log1.getId());
 			if (log2 != null)
 				client.delete(log2.getId());
 		}
 
 	}
 
 	@Test
 	public void deleteLogbookFromLogs() {
 
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
 		Collection<String> logSubjects = LogUtil
 				.getLogDescriptions(returnedLogs);
 		for (LogBuilder logBuilder : setLogs) {
 			if (!logSubjects.contains(logBuilder.build().getDescription()))
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
 		Collection<String> logSubjects = LogUtil
 				.getLogDescriptions(returnedLogs);
 		if (!logSubjects.contains(setLog.build().getDescription()))
 			return false;
 		return true;
 	}
 
 }
