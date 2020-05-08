 package org.moten.david.log.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 import org.moten.david.log.query.BucketQuery;
 import org.moten.david.log.query.Buckets;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Function;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.orientechnologies.orient.client.remote.OServerAdmin;
 import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
 import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
 import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
 import com.orientechnologies.orient.core.exception.ODatabaseException;
 import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
 import com.orientechnologies.orient.core.metadata.schema.OClass;
 import com.orientechnologies.orient.core.metadata.schema.OSchema;
 import com.orientechnologies.orient.core.metadata.schema.OType;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 import com.orientechnologies.orient.core.sql.OCommandSQL;
 import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
 import com.orientechnologies.orient.core.storage.OStorage;
 
 //TODO setup for concurrency, use Filter as per https://github.com/nuvolabase/orientdb/wiki/Java-Web-Apps?
 
 /**
  * Facade for access to the orient db database either as local or remote
  * instance.
  * 
  * @author dave
  * 
  */
 public class DatabaseOrient implements Database {
 
 	private static final String DATABASE_STORAGE_TYPE_LOCAL = "local";
 
 	private static final String DATABASE_TYPE_DOCUMENT = "document";
 
 	private static final String ROOT_PASSWORD = "B8172764CBADA2F68674EE690B9D0F01A2EA7EB73005A738DE0DDD052538153F";
 
 	private static final int NETWORK_CONNECTION_POOL_SIZE_MAX = 100;
 
 	private static final int NETWORK_CONNECTION_POOL_SIZE_MIN = 2;
 
 	private static final Logger log = Logger.getLogger(DatabaseOrient.class
 			.getName());
 
 	private final ODatabaseDocumentTx db;
 
 	private final String url;
 
 	private final String username;
 
 	private final String password;
 
 	private static boolean firstTime = true;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param location
 	 */
 	public DatabaseOrient(File location) {
 		this(createDatabase(location), null, null, null);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param url
 	 * @param username
 	 * @param password
 	 */
 	public DatabaseOrient(String url, String username, String password) {
 		this(connectToDatabase(url, username, password), url, username,
 				password);
 	}
 
 	private static void createDatabaseIfDoesNotExist(String url) {
 		int i = url.indexOf('/');
 		String hostPart = url.substring(0, i);
 		String databaseName = url.substring(i + 1, url.length());
 		try {
 			log.info("creating database " + hostPart + "/" + databaseName
 					+ " if does not exist");
 			new OServerAdmin(hostPart)
 					.connect("root", ROOT_PASSWORD)
 					.createDatabase(databaseName, DATABASE_TYPE_DOCUMENT,
 							DATABASE_STORAGE_TYPE_LOCAL).close();
 			log.info("created");
 			ODatabaseException r;
 		} catch (RuntimeException e) {
 			log.info("could not create database, perhaps it exists already: "
 					+ e.getMessage());
 		} catch (IOException e) {
 			log.info("could not create database, perhaps it exists already: "
 					+ e.getMessage());
 		}
 	}
 
 	private synchronized static ODatabaseDocumentTx connectToDatabase(
 			String url, String username, String password) {
 		if (firstTime)
 			createDatabaseIfDoesNotExist(url);
 		ODatabaseDocumentTx db = ODatabaseDocumentPool.global().acquire(url,
 				username, password);
 		db.setProperty("minPool", NETWORK_CONNECTION_POOL_SIZE_MIN);
 		db.setProperty("maxPool", NETWORK_CONNECTION_POOL_SIZE_MAX);
 		log.info("obtained db for " + url);
 		if (firstTime)
 			configureDatabase(db);
 		firstTime = false;
 		log.info("connected to database");
 		return db;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#reconnect()
 	 */
 	@Override
 	public Database reconnect() {
 		close();
 		return new DatabaseOrient(url, username, password);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param db
 	 * @param url
 	 * @param username
 	 * @param password
 	 */
 	public DatabaseOrient(ODatabaseDocumentTx db, String url, String username,
 			String password) {
 		this.db = db;
 		this.url = url;
 		this.username = username;
 		this.password = password;
 	}
 
 	/**
 	 * Creates the logs database in the filesystem.
 	 * 
 	 * @param location
 	 * @return
 	 */
 	@VisibleForTesting
 	static ODatabaseDocumentTx createDatabase(File location) {
 		try {
 			FileUtils.deleteDirectory(location);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		String url = "local:" + getPath(location);
 		System.out.println(url);
 		ODatabaseDocumentTx db = new ODatabaseDocumentTx(url).create();
 		ODatabaseRecordThreadLocal.INSTANCE.set(db);
 		configureDatabase(db);
 		return db;
 	}
 
 	/**
 	 * Sets up fields and indexes.
 	 * 
 	 * @param db
 	 */
 	private static void configureDatabase(ODatabaseDocumentTx db) {
 		try {
 			OSchema schema = db.getMetadata().getSchema();
 			OClass entry = schema.createClass(TABLE_ENTRY,
 					db.addCluster(TABLE_ENTRY, OStorage.CLUSTER_TYPE.PHYSICAL));
 
 			entry.createProperty(Field.LOG_ID, OType.STRING).setMandatory(true);
 			entry.createProperty(Field.TIMESTAMP, OType.LONG)
 					.setMandatory(true);
 			entry.createProperty(Field.PROPS, OType.EMBEDDEDMAP).setMandatory(
 					true);
 			entry.createProperty(Field.TEXT, OType.STRING).setMandatory(true);
 
 			entry.createIndex("EntryLogIdIndex", OClass.INDEX_TYPE.UNIQUE,
 					Field.LOG_ID);
 			entry.createIndex("EntryTimestampIndex",
 					OClass.INDEX_TYPE.NOTUNIQUE, Field.TIMESTAMP);
 			entry.createIndex("EntryTextIndex", OClass.INDEX_TYPE.FULLTEXT,
 					Field.TEXT);
 
 			db.getMetadata().getSchema().save();
 			db.command(
 					new OCommandSQL(
 							"CREATE INDEX EntryPropsKeyTimestampIndex ON Entry ("
 									+ Field.PROPS + " by key,"
 									+ Field.TIMESTAMP + ") NOTUNIQUE"))
 					.execute();
 			db.getMetadata().getSchema().save();
 			db.getMetadata().getIndexManager().reload();
 
 			db.commit();
 		} catch (RuntimeException e) {
 			log.log(Level.WARNING, e.getMessage());
 		}
 	}
 
 	private static String getPath(File location) {
 		try {
 			return location.getCanonicalPath();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#useInCurrentThread()
 	 */
 	@Override
 	public void useInCurrentThread() {
 		ODatabaseRecordThreadLocal.INSTANCE.set(db);
 	}
 
 	/**
 	 * Persist a log entry to the database.
 	 * 
 	 * @param entry
 	 */
 	private void persist(LogEntry entry, boolean commit) {
 
 		// create a new document (row in table)
 		// persist the full message, timestamp, level logger and threadName
 		long timestamp = entry.getTime();
 		String id = UUID.randomUUID().toString();
 
 		ODocument d = new ODocument(TABLE_ENTRY);
 		d.field(Field.TIMESTAMP, timestamp, OType.LONG);
 		d.field(Field.LOG_ID, id);
 		d.field(Field.TEXT, Util.getString(entry.getProperties()));
 
 		Map<String, ODocument> map = Maps.newHashMap();
 		for (Entry<String, String> e : entry.getProperties().entrySet()) {
 			if (e.getValue() != null) {
 				ValueAndType v = parse(e.getValue());
 				map.put(cleanKey(e.getKey()),
 						new ODocument().field(Field.VALUE, v.value, v.type));
 			}
 		}
 		d.field(Field.PROPS, map, OType.EMBEDDEDMAP);
 
 		d.save();
 		if (commit)
 			db.commit();
 	}
 
 	private String cleanKey(String key) {
 		return key.replace(" ", "_");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.moten.david.log.core.Database#persist(org.moten.david.log.core.LogEntry
 	 * )
 	 */
 	@Override
 	public void persist(LogEntry entry) {
 		synchronized (this) {
 			useInCurrentThread();
 			persist(entry, true);
 		}
 	}
 
 	private static class ValueAndType {
 		Object value;
 		OType type;
 
 		public ValueAndType(Object value, OType type) {
 			super();
 			this.value = value;
 			this.type = type;
 		}
 	}
 
 	private ValueAndType parse(String s) {
 		// try matching against Integer
 		try {
 			Integer val = Integer.parseInt(s);
 			return new ValueAndType(val, OType.INTEGER);
 		} catch (NumberFormatException e) {
 			// continue
 		}
 		// try matching against Double
 		try {
 			Double val = Double.parseDouble(s);
 			return new ValueAndType(val, OType.DOUBLE);
 		} catch (NumberFormatException e) {
 			// continue
 		}
 		// try matching against boolean
 		if (s.equalsIgnoreCase("true"))
 			return new ValueAndType(Boolean.TRUE, OType.BOOLEAN);
 		else if (s.equalsIgnoreCase("false"))
 			return new ValueAndType(Boolean.FALSE, OType.BOOLEAN);
 
 		return new ValueAndType(s, OType.STRING);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#execute(org.moten.david.log.query.
 	 * BucketQuery)
 	 */
 	@Override
 	public Buckets execute(BucketQuery query) {
 		synchronized (this) {
 			useInCurrentThread();
 			log.info(query.toString());
 			OSQLSynchQuery<ODocument> sqlQuery = new OSQLSynchQuery<ODocument>(
 					query.getSql());
 			long t = System.currentTimeMillis();
 			List<ODocument> result = db.query(sqlQuery);
 			log.info("query result returned, queryTimeMs="
 					+ (System.currentTimeMillis() - t) + "ms");
 			Buckets buckets = new Buckets(query);
 			int i = 0;
 			for (ODocument doc : result) {
 				i++;
 				if (i % 10000 == 0)
 					log.info(i + " records");
 				Long timestamp = doc.field(Field.TIMESTAMP);
 				if (doc.field(Field.VALUE) != null) {
 					try {
 						Object o = doc.field(Field.VALUE);
 						double value;
 						if (o instanceof Number) {
 							value = ((Number) o).doubleValue();
 						} else
 							value = Double.parseDouble(o.toString());
 						buckets.add(timestamp, value);
 					} catch (NumberFormatException e) {
 						// not a number don't care about it
 					}
 				}
 			}
 			log.info("found " + result.size() + " records");
 			return buckets;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#size()
 	 */
 	@Override
 	public long size() {
 		synchronized (this) {
 			useInCurrentThread();
 			return db.getSize();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#getNumEntries()
 	 */
 	@Override
 	public long getNumEntries() {
 		synchronized (this) {
 			useInCurrentThread();
 			return db.countClass(TABLE_ENTRY);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#close()
 	 */
 	@Override
 	public void close() {
 		db.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#getKeys()
 	 */
 	@Override
 	public Set<String> getKeys() {
 		synchronized (this) {
 			useInCurrentThread();
 			// TODO implement getKeys
 			return Sets.newHashSet("specialNumber");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#persistDummyRecords(long)
 	 */
 	@Override
 	public void persistDummyRecords(long n) {
 		synchronized (this) {
 			useInCurrentThread();
 			log.info("persisting dummy values");
 			db.declareIntent(new OIntentMassiveInsert());
 			long t = System.currentTimeMillis();
 			Random r = new Random();
 			for (long i = 0; i < n; i++) {
 				long time = t - TimeUnit.HOURS.toMillis(1)
 						+ r.nextInt((int) TimeUnit.HOURS.toMillis(2));
 				long specialNumber = i % (r.nextInt(100) + 1);
 				{
 					Map<String, String> map = Maps.newHashMap();
 					LogEntry entry = new LogEntry(time, map);
 					map.put(Field.SOURCE, "dummy");
 					map.put(Field.LOGGER, "something.stuff");
 					map.put(Field.LEVEL, "INFO");
 					double x = specialNumber * Math.random();
 					map.put(Field.MSG, "specialNumber=" + specialNumber
 							+ ",executionTimeSeconds=" + x);
 					map.put("specialNumber", x + "");
 					map.put("executionTimeSeconds", x + "");
 					persist(entry, false);
 				}
 				{
 					Map<String, String> map = Maps.newHashMap();
 					LogEntry entry = new LogEntry(time, map);
 					map.put(Field.LOGGER, "another.logger");
 					map.put(Field.SOURCE, "dummy");
 					map.put(Field.LEVEL, "DEBUG");
 					long m = Math.round(100 * Math.random());
 					map.put(Field.MSG, "numberProcessed=" + m);
 					map.put("numberProcessed", m + "");
 					persist(entry, false);
 				}
 				if (i % 1000 == 0)
 					log.info("written " + i + " records");
 			}
 			db.declareIntent(null);
 			db.commit();
 			log.info("persisted " + n
 					+ " random values from the last hour to table "
 					+ TABLE_ENTRY);
 			log.info("database size=" + db.getSize());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.moten.david.log.core.Database#getLogs(long, long)
 	 */
 	@Override
 	public Iterable<String> getLogs(long startTime, long finishTime) {
 		synchronized (this) {
 			useInCurrentThread();
 			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
 					"select from " + TABLE_ENTRY + " where " + Field.TIMESTAMP
 							+ " between " + startTime + " and " + finishTime
 							+ " order by " + Field.TIMESTAMP);
 			List<ODocument> entries = db.query(query);
 			final DateFormat df = new SimpleDateFormat(
 					"yyyy-MM-dd HH:mm:ss.SSS");
 			df.setTimeZone(TimeZone.getTimeZone("UTC"));
 
 			final Iterator<String> it = Iterators.transform(entries.iterator(),
 					new Function<ODocument, String>() {
 
 						@Override
 						public String apply(ODocument input) {
 							return getLine(df, input);
 						}
 					});
 
 			return new Iterable<String>() {
 
 				@Override
 				public Iterator<String> iterator() {
 					return it;
 				}
 			};
 		}
 	}
 
 	private static String getLine(DateFormat df, ODocument d) {
 		Long t = d.field(Field.TIMESTAMP);
 		Map<String, ODocument> map = d.field(Field.PROPS);
 		String level = getValueAsString(map, Field.LEVEL);
 		String logger = getValueAsString(map, Field.LOGGER);
 		String threadName = getValueAsString(map, Field.THREAD_NAME);
 		String method = getValueAsString(map, Field.METHOD);
 		String msg = getValueAsString(map, Field.MSG);
 		String source = getValueAsString(map, Field.SOURCE);
 		StringBuffer s = new StringBuffer();
 		s.append(df.format(new Date(t)));
 		s.append(source);
 		s.append(level);
 		s.append(logger);
 		s.append(method);
 		s.append(threadName);
 		s.append(" - ");
 		s.append(msg);
 		return s.toString();
 	}
 
 	private static String getValueAsString(Map<String, ODocument> map,
 			String key) {
 		ODocument d = map.get(key);
 		if (d == null)
 			return "";
 		else
 			return " " + d.field(Field.VALUE);
 	}
 
 	public static void main(String[] args) {
 		String host = "jenkins.amsa.gov.au";
 		createDatabaseIfDoesNotExist("remote:" + host + "/logs");
 		Database d = new DatabaseOrient("remote:" + host + "/logs", "admin",
 				"admin");
 		HashMap<String, String> map = Maps.newHashMap();
 		map.put("timeToCreateMs", "1200");
 		d.persist(new LogEntry(System.currentTimeMillis(), map));
 		d.close();
 	}
 
 }
