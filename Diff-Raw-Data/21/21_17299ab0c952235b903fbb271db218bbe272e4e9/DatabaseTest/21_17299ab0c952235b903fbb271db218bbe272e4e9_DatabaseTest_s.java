 package org.moten.david.log.core;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.moten.david.log.query.BucketQuery;
 import org.moten.david.log.query.Buckets;
 
 import com.google.common.collect.Maps;
 
 public class DatabaseTest {
 
 	private static final String DATABASE_TEST_CREATE = "target/test-create";
 	private static final String DATABASE_TEST_CREATE_2 = "target/test-create2";
 	private static final String DATABASE_TEST_PERSIST_DUMMY = "target/test-persist";
 	private static final String DATABASE_TEST_BUCKET_QUERY = "target/test-bucket-query";
 
 	@Test
 	public void testCreateDbAndPersistRecords() {
 		Database p = new Database(new File("target/test1"));
 		long t = 0;
 		for (int i = 1; i <= 1000; i++) {
 			Map<String, String> map = Maps.newHashMap();
 			map.put("size", i % 27 + "");
 			LogEntry entry = new LogEntry(t + i, map);
 			p.persist(entry);
 		}
 		// TODO enable this test below
 		Set<String> keys = p.getKeys();
 		assertEquals(1, keys.size());
 		// assertEquals("size", keys.get(0));
 		p.close();
 	}
 
 	@Test
 	public void testCreateDbParseAndPersist() {
 
 		final String lineMessage = " INFO  au.gov.amsa.er.craft.tracking.actor.FixesPersisterActor - fixes queue size = 0";
 		System.out.println("creating database");
 		Database p = new Database(new File("target/test2"));
 		LogParser parser = new LogParser(LogParserOptions.load());
 		DateFormat df = new SimpleDateFormat(LogParser.DATE_FORMAT_DEFAULT);
 		df.setTimeZone(TimeZone.getTimeZone("UTC"));
 
 		long n1 = 100;
 		long n2 = 100;
 		System.out
 				.println("triggering hotspot compilation by sending some messages");
 		long previousSize = p.size();
 		persistMessages(lineMessage, p, parser, df, n1);
 		assertTrue(p.size() > previousSize);
 		System.out.println("parsing and persisting records");
 		long timer = System.currentTimeMillis();
 
 		persistMessages(lineMessage, p, parser, df, n2);
 
 		long ms = System.currentTimeMillis() - timer;
 		System.out.println("done in " + ms + "ms");
 		System.out.println("rate=" + (1000 * n2 / ms) + " lines/s");
 		p.close();
 	}
 
 	private void persistMessages(final String lineMessage, Database p,
 			LogParser parser, DateFormat df, long n) {
 
 		for (int i = 0; i < n; i++) {
 			long t = i;
 			String line = df.format(new Date(t)) + lineMessage;
 			LogEntry entry = parser.parse("test", line);
 			// get coverage of toString method
 			entry.toString();
 			p.persist(entry);
 		}
 	}
 
 	@Test
 	public void testCreateAndConnectToLocalDatabase() {
 		createAndConnectTo(DATABASE_TEST_CREATE).close();
 	}
 
 	@Test
 	public void testReconnectToLocalDatabase() {
 		Database db = createAndConnectTo(DATABASE_TEST_CREATE_2).reconnect();
 		assertNotNull(db);
 		db.close();
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCreateDatabaseCannotDeleteDirectoryBeforeCreationAndShouldThrowRuntimeException()
 			throws IOException {
 		String filename = "target/testCreateCannot";
 		File f = new File(filename);
 		// create file not a directory
 		f.createNewFile();
 		Database.createDatabase(f);
 	}
 
 	@Test
 	public void testPersistDummyRecords() {
 		Database db = createAndConnectTo(DATABASE_TEST_PERSIST_DUMMY);
 		db.configureDatabase();
 		int n = 10;
 		db.persistDummyRecords(n);
 		int count = 0;
 		for (String line : db.getLogs(0, Long.MAX_VALUE)) {
 			count++;
 		}
 		Assert.assertEquals(n * 2, count);
 		db.close();
 	}
 
 	static Database createAndConnectTo(String path) {
 		new Database(new File(path)).close();
 		return new Database("local:" + path, "admin", "admin");
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testBucketQueryThrowsNullPointerExceptionGivenNullQuery() {
 		Database db = null;
 		try {
			db = createAndConnectTo(DATABASE_TEST_BUCKET_QUERY);
 			db.execute(null);
 		} finally {
 			if (db != null)
 				db.close();
 		}
 	}
 
 	@Test
 	public void testBucketQueryReturnsResults() {
 		Database db = null;
 		try {
 			db = createAndConnectTo(DATABASE_TEST_BUCKET_QUERY);
 			db.configureDatabase();
 			int n = 100;
 			db.persistDummyRecords(n);
 			String sql = "select " + Field.TIMESTAMP
 					+ ", props[specialNumber].value  as " + Field.VALUE
 					+ " from " + Database.TABLE_ENTRY;
 			System.out.println(sql);
 			BucketQuery query = new BucketQuery(new Date(
 					System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)),
 					TimeUnit.HOURS.toMillis(1), 24, sql);
 			Buckets result = db.execute(query);
 			assertTrue(result.getBucketForAll().count() > 0);
 		} finally {
 			if (db != null)
 				db.close();
 		}
 	}
 }
