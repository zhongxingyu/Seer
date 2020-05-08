 package edu.msu.nscl.olog.api;
 
 import static edu.msu.nscl.olog.api.LogBuilder.log;
 import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
 import static edu.msu.nscl.olog.api.TagBuilder.tag;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.hsqldb.lib.HashMap;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
 
 //if (key.equals("search")) {
 //    log_matches.addAll(match.getValue());
 ////    JcrSearch js = new JcrSearch();
 ////    jcr_search_ids = js.searchForIds(match.getValue().toString());
 //} else if (key.equals("tag")) {
 //    addTagMatches(match.getValue());
 //} else if (key.equals("logbook")) {
 //    addLogbookMatches(match.getValue());
 //} else if (key.equals("page")) {
 //    logPaginate_matches.putAll(key, match.getValue());
 //} else if (key.equals("limit")) {
 //    logPaginate_matches.putAll(key, match.getValue());
 //} else if (key.equals("start")) {
 //    date_matches.putAll(key, match.getValue());
 //} else if (key.equals("end")) {
 //    date_matches.putAll(key, match.getValue());
 //} else {
 //    value_matches.putAll(key, match.getValue());
 //}
 
 public class QueryIT {
 
 	private static int initialLogCount;
 
 	private static OlogClient client = OlogClientBuilder.serviceURL()
 			.withHTTPAuthentication(true).create();
 	// Logs
 	static private Log pvk_01;
 	static private Log pvk_02;
 	static private Log pvk_03;
 	static private Log distinctName;
 
 	// Tags
 	static TagBuilder tagA = tag("Taga", "me");
 	static TagBuilder tagB = tag("Tagb", "me");
 	static TagBuilder tagC = tag("Tagc", "me");
 	static TagBuilder tagStar = tag("Tag*", "me");
 	// Logbooks
 	static LogbookBuilder book = logbook("book");
 	static LogbookBuilder book2 = logbook("book2");
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 		initialLogCount = client.listLogs().size();
 		// Add the tags and logbooks.
 		client.set(book.owner("me"));
 		client.set(book2.owner("me"));
 		client.set(tagA);
 		client.set(tagB);
 		client.set(tagC);
 		client.set(tagStar);
 		pvk_01 = client.set(log("pvk:01<first>").description("first details")
 				.level("Info").in(book).in(book2).with(tagA));
 		pvk_02 = client.set(log("pvk:02<second>").description("second details")
 				.level("Info").in(book).with(tagA).with(tagB));
 		pvk_03 = client.set(log("pvk:03<second>").description("some details")
 				.level("Info").in(book).with(tagB).with(tagC));
 		distinctName = client.set(log("distinctName")
 				.description("some details").level("Info").in(book)
 				.with(tagStar));
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		client.delete(pvk_01.getId());
 		client.delete(pvk_02.getId());
 		client.delete(pvk_03.getId());
 		client.delete(distinctName.getId());
 		// clean up all the tags and logbooks
 		client.deleteLogbook(book.build().getName());
 		client.deleteLogbook(book2.toXml().getName());
 		client.deleteTag(tagA.toXml().getName());
 		client.deleteTag(tagB.toXml().getName());
 		client.deleteTag(tagC.toXml().getName());
 		client.deleteTag(tagStar.toXml().getName());
 
 		assertTrue(client.listLogs().size() == initialLogCount);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * check if all logs are returned
 	 */
 	@Test
 	public void queryAllLogs() {
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("search", "*");
 		Collection<Log> logs = client.findLogs(map);
 		assertTrue(client.listLogs().size() == logs.size());
 	}
 
 	/**
 	 * Test searching based on
 	 */
//	@Test
 	public void queryLogs() {
 		Map<String, String> map = new Hashtable<String, String>();
 		map.put("search", "pvk:*");
 		Collection<Log> logs = client.findLogs(map);
 		assertTrue("Failed to get search based on subject pattern",
 				logs.size() == 3);
 	}
 
 	/**
 	 * Test logbook query When multiple logbooks are queried, the result is a
 	 * logical OR of all the query conditions
 	 */
 	@Test
 	public void queryLogsbyLogbook() {
 		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
 		map.add("logbook", "book2");
 		Collection<Log> logs = client.findLogs(map);
 		assertTrue("search for all logs in logbook book2 failed ",
 				logs.size() == 1);
 		// Query for logs from multiple logbooks.
 		map.add("logbook", "book");
 		map.add("logbook", "book2");
 		logs = client.findLogs(map);
 		assertTrue("search for all logs in logbook book OR book2 failed",
 				logs.size() == 4);
 
 	}
 
 	/**
 	 * Test tag query When multiple tags are queried, the result is a logical OR
 	 * of all the query conditions
 	 */
 	@Test
 	public void queryLogsbyTag() {
 		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
 		Collection<Log> queryResult;
 		// Search for a logs with tag 'Tag*'
 		map.add("tag", "Tag*");
 		queryResult = client.findLogs(map);
 		assertTrue(queryResult.size() == 1);
 		// query for logs with anyone of a group of tags
 		map.clear();
 		map.add("tag", "taga");
 		map.add("tag", "tagb");
 		queryResult = client.findLogs(map);
 		assertTrue(queryResult.size() == 3);
 	}
 
//	@Test
 	public void queryLogsbyDescription() {
 		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
 		// search a single log based on description.
 		map.add("search", pvk_01.getDescription());
 		Collection<Log> queryResult = client.findLogs(map);
 		assertTrue(
 				"Failed to search based on the log descrition expected 1 found "
 						+ queryResult.size(), queryResult.size() == 1
 						&& queryResult.contains(pvk_01));
 		// search for "some detail" which matches multiple logs.
 		map.clear();
 		map.add("search", "some details");
 		queryResult = client.findLogs(map);
 		assertTrue(
 				"Failed to search based on the log descrition expected 2 found "
 						+ queryResult.size(),
 				queryResult.size() == 2 && queryResult.contains(pvk_03)
 						&& queryResult.contains(distinctName));
 		// search for logs with one
 		map.clear();
 		map.add("search", pvk_01.getDescription());
 		map.add("search", pvk_02.getDescription());
 		queryResult = client.findLogs(map);
 		assertTrue(
 				"Failed to search based on the log descrition expected 2 found "
 						+ queryResult.size(),
 				queryResult.size() == 2 && queryResult.contains(pvk_01)
 						&& queryResult.contains(pvk_02));
 
 	}
 
 //	@Test
 	public void queryLogsbyTime() {
 		MultivaluedMap<String, String> searchMap = new MultivaluedMapImpl();
 		searchMap.add("search", "pvk*");
 		List<Log> logs = new ArrayList<Log>(client.findLogs(searchMap));
 		Collections.sort(logs, new Comparator<Log>() {
 
 			@Override
 			public int compare(Log o1, Log o2) {
 				return o1.getCreatedDate().compareTo(o2.getCreatedDate());
 			}
 
 		});
 		// search for an exact time, start=end
 		searchMap.clear();
 		Log log = logs.iterator().next();
 		searchMap.add("start", String.valueOf(log.getCreatedDate().getTime()));
 		searchMap.add("end", String.valueOf(log.getCreatedDate().getTime()));
 		Collection<Log> searchResult = client.findLogs(searchMap);
 		assertTrue("Failed to search based on create times",
 				searchResult.size() == 1 && searchResult.contains(log));
 	}
 
 	/**
 	 * Test findLogsby*() methods
 	 */
//	@Test
 	public void queryTest() {
 		// find by search
 		assertTrue("Failed to query using findbysearch method ", client
 				.findLogsBySearch("pvk_*").size() == 3);
 
 		// find by tag
 		assertTrue("Failed to query using the findbytag method ", client
 				.findLogsByTag(tagA.build().getName()).size() == 2);
 
 		// find by logbook
 		assertTrue("Failed to query using the findbylogbook method", client
 				.findLogsByLogbook(book2.build().getName()).size() == 1);
 	}
 }
