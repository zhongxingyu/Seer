 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.data.db.impl;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.Status;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.CacheMode;
 import org.hibernate.FlushMode;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.ScrollMode;
 import org.hibernate.ScrollableResults;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.onelab.filter.DynamicBloomFilter;
 import org.onelab.filter.Key;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.StatusVariable;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.data.db.ICommandDB;
 import org.paxle.data.db.URIQueueEntry;
 
 public class CommandDB implements IDataProvider<ICommand>, IDataSink<URIQueueEntry>, ICommandDB, EventHandler, Monitorable {
 	
 	private static final String CACHE_DIR = "double-urls-caches";
 	private static final String BLOOM_CACHE_FILE = "doubleURLsCache.ser";
 	private static final String EHCACHE_NAME = "DoubleURLCache";
 	
 	private static final int MAX_IDLE_SLEEP = 60000;
 	
 	public static final String PID = "org.paxle.data.cmddb";
 	private static final String MONITOR_TOTAL_SIZE = "size.total";
 	private static final String MONITOR_ENQUEUED_SIZE = "size.enqueued";
 	
 	private static final String UTF8 = "UTF-8";
 	/**
       * The cachemanager to use
       */
 	private CacheManager manager = null;
 	
 	/**
 	  * A cach to hold {@link RobotsTxt} objects in memory
 	  */
 	private Cache urlExistsCache = null;
 	
 	/**
 	 * Component to track {@link ICommand commands}
 	 */
 	private ICommandTracker commandTracker;
 	
 	/**
 	 * A {@link IDataSink data-sink} to write the loaded {@link ICommand commands} out
 	 */
 	private IDataSink<ICommand> sink = null;	
 	
 	/**
 	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #db database}
 	 * and write it into the {@link #sink data-sink}.
 	 */
 	private Writer writerThread = null;
 	
 	/**
 	 * A {@link Thread thread} to populate the double URLs cache from database.
 	 */
 	private PopulateThread populateThread = null;
 	
 	/**
 	 * The logger
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * The hibernate {@link SessionFactory}
 	 */
 	private SessionFactory sessionFactory;
 	
 	/**
 	 * The currently used db configuration
 	 */
 	private Configuration config; 
 
 	private boolean closed = false;
 	
 	/**
 	 * A set holding all known URLs
 	 */
 	private DynamicBloomFilter bloomFilter = null;
 	
	private volatile long cntTotal, cntCrawlerQueue;
 	
 	public CommandDB(URL configURL, List<URL> mappings, ICommandTracker commandTracker) {
 		this(configURL, mappings, null, commandTracker);
 	}
 	
 	public CommandDB(URL configURL, List<URL> mappings, Properties extraProperties, ICommandTracker commandTracker) {
 		if (configURL == null) throw new NullPointerException("The URL to the hibernate config file is null.");
 		if (mappings == null) throw new NullPointerException("The list of mapping files was null.");
 		
 		try {
 			this.commandTracker = commandTracker;
 			
 			/* ===========================================================================
 			 * Init Hibernate
 			 * =========================================================================== */
 			try {
 				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 				
 				// Read the hibernate configuration from *.cfg.xml
 				this.logger.info(String.format("Loading DB configuration from URL '%s'.",configURL));
 				this.config = new Configuration().configure(configURL);
 				
 				// register an interceptor (required to support our interface-based command model)
 				this.config.setInterceptor(new InterfaceInterceptor());
 				
 				// merge weith additional properties
 				if (extraProperties != null) {
 					this.config.addProperties(extraProperties);
 				}
 				
 				// post-processing of read properties
 				ConnectionUrlTool.postProcessProperties(this.config);
 				
 				// load the various mapping files
 				for (URL mapping : mappings) {
 					if (this.logger.isDebugEnabled()) this.logger.debug(String.format("Loading mapping file from URL '%s'.",mapping));
 					this.config.addURL(mapping);
 				}
 				
 				String[] sql = this.config.generateSchemaCreationScript( new org.hibernate.dialect.DerbyDialect());					
 				
 				// create the session factory
 				this.sessionFactory = this.config.buildSessionFactory();
 			} catch (Throwable ex) {
 				// Make sure you log the exception, as it might be swallowed
 				this.logger.error("Initial SessionFactory creation failed.",ex);
 				throw new ExceptionInInitializerError(ex);
 			}
 			this.manipulateDbSchema();
 			cntTotal = this.totalSize();
 			cntCrawlerQueue = this.size("enqueued");
 			System.out.println("command-db size: " + cntTotal + ", to crawl: " + cntCrawlerQueue);
 			
 			/* ===========================================================================
 			 * Init Reader/Writer Threads
 			 * =========================================================================== */
 			this.writerThread = new Writer();
 			
 			/* ===========================================================================
 			 * Init Cache
 			 * =========================================================================== */
 			// configure caching manager
 			this.manager = CacheManager.getInstance();
 			
 			// init a new cache 
 			this.urlExistsCache = new Cache(EHCACHE_NAME, 100000, false, false, 60*60, 30*60);
 			this.manager.addCache(this.urlExistsCache);
 			
 			// init/open the double URLs cache, initializes the bloom-filter
 			openDoubleURLSet();
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while initializing the command-DB.",
 					e.getClass().getName()
 			),e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public String getDatabaseLocation() {
 		final String connection = config.getProperty("connection.url");
 		final int semicolon = connection.indexOf(';');
 		return connection.substring(
 				connection.lastIndexOf(':') + 1,
 				(semicolon == -1) ? connection.length() : semicolon);
 	}
 	
 	/* =========================================================================
 	 * Monitorable support
 	 * ========================================================================= */
 	
 	/**
 	 * @see Monitorable#getDescription(String)
 	 */
 	public String getDescription(String id) throws IllegalArgumentException {
 		if (id.equals(MONITOR_TOTAL_SIZE)) {
 			return "Total known URIs";
 		} else if (id.equals(MONITOR_ENQUEUED_SIZE)) {
 			return "Enqueued URIs";
 		} else {
 			throw new IllegalArgumentException("no such variable '" + id + "'");
 		}
 	}
 	
 	/**
 	 * @see Monitorable#getStatusVariable(String)
 	 */
 	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
 		if (id.equals(MONITOR_TOTAL_SIZE)) {
 			return new StatusVariable(MONITOR_TOTAL_SIZE, StatusVariable.CM_CC, (int)size());
 		} else if (id.equals(MONITOR_ENQUEUED_SIZE)) {
 			return new StatusVariable(MONITOR_ENQUEUED_SIZE, StatusVariable.CM_CC, (int)enqueuedSize());
 		} else {
 			throw new IllegalArgumentException("no such variable '" + id + "'");
 		}	
 	}
 	
 	/**
 	 * @see Monitorable#getStatusVariableNames()
 	 */
 	public String[] getStatusVariableNames() {
 		return new String[] { MONITOR_TOTAL_SIZE, MONITOR_ENQUEUED_SIZE };
 	}
 	
 	/**
 	 * @see Monitorable#notifiesOnChange(String)
 	 */
 	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
 		return false;
 	}
 	
 	/**
 	 * @see Monitorable#resetStatusVariable(String)
 	 */
 	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
 		return false;
 	}
 	
 	/* =========================================================================
 	 * Management for the double URLs cache
 	 * ========================================================================= */
 	
 	private void closeDoubleURLSet() throws IOException {
 		final long start = System.currentTimeMillis();
 		final OutputStream fileOs = new FileOutputStream(new File(getCreateCacheDir(), BLOOM_CACHE_FILE));
 		DataOutputStream dataOs = null;
 		try {
 			dataOs = new DataOutputStream(new BufferedOutputStream(fileOs));
 			bloomFilter.write(dataOs);
 			dataOs.flush();
 			final long end = System.currentTimeMillis();
 			final int size = cacheSize();
 			logger.info("Flushed double URLs cache (" + size + " entries) to disk in " + (end - start) + " ms");
 		} finally { ((dataOs == null) ? fileOs : dataOs).close(); }
 	}
 	
 	private File getCreateCacheDir() {
 		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + CACHE_DIR;
 		final File cacheDir = new File(dataPath);
 		if (!cacheDir.exists())
 			cacheDir.mkdirs();
 		return cacheDir;
 	}
 	
 	private void openDoubleURLSet() throws IOException {
 		File serializedFile = new File(getCreateCacheDir(), BLOOM_CACHE_FILE);
 		
 		if (!(serializedFile.exists() && serializedFile.canRead() && serializedFile.isFile())) {
 			// XXX: what is the old file here?
 			final File oldFile = new File(getDatabaseLocation(), BLOOM_CACHE_FILE);
 			if (oldFile.exists() && oldFile.canRead() && oldFile.isFile()) {
 				serializedFile = oldFile;
 				oldFile.deleteOnExit();
 			} else {
 				logger.info("Serialized double URL set not found, populating cache from DB (this may take some time) ...");
 				bloomFilter = new DynamicBloomFilter(1437764, 10, 100000);	// creating a maximum false positive rate of 0.1 %
 				populateThread = new PopulateThread();
 				populateThread.start();
 			}	
 		}
 		if (serializedFile.exists() && serializedFile.canRead() && serializedFile.isFile()) {
 			logger.info("Serialized double URL set found, reading data");
 			final InputStream fileIs = new FileInputStream(serializedFile);
 			try {
 				final DataInputStream dataIs = new DataInputStream(new BufferedInputStream(fileIs));
 				bloomFilter = new DynamicBloomFilter();
 				bloomFilter.readFields(dataIs);
 			} finally { fileIs.close(); }
 		}
 	}
 	
 	private class PopulateThread extends Thread {
 		
 		public PopulateThread() {
 			super("DoubleURLCachePopulater");
 		}
 		
 		@Override
 		public void run() {
 			final long time = System.currentTimeMillis();
 			final long count = populateDoubleURLSet();
 			logger.info("Initialized the double URL cache with " + count + " entries in " +
 					((System.currentTimeMillis() - time) / 1000) + " seconds");
 		}
 		
 		private long populateDoubleURLSet() {
 			Session session = null;
 			Transaction transaction = null;
 			long count = 0;
 			
 			try {
 				session = sessionFactory.openSession();
 				transaction = session.beginTransaction();
 				
 				final Key key = new Key();
 				final DynamicBloomFilter bf = bloomFilter;
 				
 				final long start = System.currentTimeMillis();
 				long time = start;
 				long lastCount = 0L;				
 				
 				final Query query = session.createSQLQuery(
 						"SELECT location FROM EnqueuedCommand " + 
 						"UNION ALL " +
 						"SELECT location FROM CrawledCommand "
 				).setReadOnly(true);
 								
 				ScrollableResults sr = query.scroll(ScrollMode.FORWARD_ONLY); // (ScrollMode.FORWARD_ONLY);
 				
 				// loop through the available commands
 				while(sr.next() && !super.isInterrupted()) {
 					String locationStr = (String) sr.get()[0];
 					key.set(locationStr.getBytes(UTF8), 1.0);
 					bf.add(key);
 					count++;
 					
 					if (count % 250000 == 0) {
 						final long now = System.currentTimeMillis();
 						final long last = time;
 						final long totalMs = now - start;
 						final long deltaMs = now - last;
 						final long deltaCount = count - lastCount;
 						final long totalCountLeft = cntTotal - count;
 						final int etaSec = (int)(((double)totalMs / count) * totalCountLeft / 1000);
 						logger.info(String.format(
 								"Populated URL-cache with %,d URIs in %d seconds (%,d/sec), %,d to go, ETA: %02d:%02d",
 								Long.valueOf(count),
 								Long.valueOf(totalMs / 1000L),
 								Integer.valueOf((int)(deltaCount / (double)deltaMs * 1000.0)),
 								Long.valueOf(totalCountLeft),
 								Integer.valueOf(etaSec / 60), Integer.valueOf(etaSec % 60)));
 						lastCount = count;
 						time = now;
 					}
 				}
 				
 				transaction.commit();
 			} catch (Exception e) {
 				if (transaction != null && transaction.isActive()) transaction.rollback(); 
 				logger.error(String.format(
 						"Unexpected '%s' while populating the double URLs cache from the DB.",
 						e.getClass().getName()
 				),e);
 			} finally {
 				// closing session
 				if (session != null) try { session.close(); } catch (Exception e) { 
 					logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 				}
 			}
 			return count;
 		}
 	}
 	
 	/**
 	 * Returns the size of the double URLs cache.
 	 * <p>
 	 * <i>Implementation note</i>: Since the {@link DynamicBloomFilter} does not freely communicate
 	 * the required data, it is retrieved via reflection. This method should therefore not be
 	 * called too frequently.
 	 * @return the number of {@link Key}s contained in {@link #bloomFilter}.
 	 */
 	private int cacheSize() {
 		try {
 			final Field matrix = DynamicBloomFilter.class.getDeclaredField("matrix");
 			final Field currentNbRecord = DynamicBloomFilter.class.getDeclaredField("currentNbRecord");
 			final Field nr = DynamicBloomFilter.class.getDeclaredField("nr");
 			matrix.setAccessible(true);
 			currentNbRecord.setAccessible(true);
 			nr.setAccessible(true);
 			return (
 					((Integer)currentNbRecord.get(bloomFilter)).intValue() +
 					((Integer)nr.get(bloomFilter)).intValue() * (Array.getLength(matrix.get(bloomFilter)) - 1)
 			);
 		} catch (Throwable e) { e.printStackTrace(); }
 		return -1;
 	}
 	
 	/**
 	 * Checks the double URLs cache for the given {@link URI}.
 	 * This is a convienience method if only one URI has to be processed, otherwise
 	 * it is recommended to manually access the cache, i.e.:
 	 * <p>
 	 * <pre>
 	 * 			final Key key = new Key();
 	 * 			while ([...]) {
 	 * 				key.set([...], 1.0);
 	 * 				final boolean exists = {@link #bloomFilter}.membershipTest(key);
 	 * 			}
 	 * </pre>
 	 * This way the the {@link Key}-Object does not have to be created newly for every {@link URI}.
 	 * @param location the {@link URI} to check
 	 * @return <code>false</code> if the given location has not previously been added to the cache,
 	 *         <code>true</code> otherwise. May also return <code>true</code> if the {@link URI}
 	 *         has not previously added. See the description of {@link DynamicBloomFilter} for details.
 	 * @see DynamicBloomFilter
 	 */
 	private final boolean isKnownInDoubleURLs(final URI location) {
 		final Key key;
 		try {
 			key = new Key(location.toString().getBytes(UTF8));
 		} catch (UnsupportedEncodingException e) {
 			/* UTF-8 support should be implemented in the JVM */
 			throw new RuntimeException(e);
 		}
 		return this.bloomFilter.membershipTest(key);
 	}
 	
 	/**
 	 * Puts the {@link URI} into the double URLs cache.
 	 * This is a convienience method if only one URI has to be processed, otherwise
 	 * it is recommended to manually access the cache, i.e.:
 	 * <p>
 	 * <pre>
 	 * 			final Key key = new Key();
 	 * 			while ([...]) {
 	 * 				key.set([...], 1.0);
 	 * 				{@link #bloomFilter}.add(key);
 	 * 			}
 	 * </pre>
 	 * This way the the {@link Key}-Object does not have to be created newly for every {@link URI}.
 	 * @param location the {@link URI} to put into the cache
 	 * @see DynamicBloomFilter
 	 */
 	private final void putInDoubleURLs(final URI location) {
 		final Key key;
 		try {
 			key = new Key(location.toString().getBytes(UTF8));
 		} catch (UnsupportedEncodingException e) {
 			/* UTF-8 support should be implemented in the JVM */
 			throw new RuntimeException(e);
 		}
 		bloomFilter.add(key);
 	}
 	
 	/* =========================================================================
 	 * Command management
 	 * ========================================================================= */
 	
 	private void manipulateDbSchema() {
 		/* disabled because it seems to cause NPEs in derby, see
 		 * https://issues.apache.org/jira/browse/DERBY-3197?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#action_12543729
 		System.setProperty("derby.language.logQueryPlan", "true");
 		 */
 		Connection c = null;
 		try {
 			Properties props = this.config.getProperties();
 			String dbDriver = props.getProperty("connection.driver_class");
 			if (dbDriver != null && dbDriver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
 				c = DriverManager.getConnection(props.getProperty("connection.url"));
 				
 				// create index on command-location
 				PreparedStatement p = c.prepareStatement("CREATE INDEX ENQUEUED_LOCATION_IDX on EnqueuedCommand (location)");
 				p.execute();
 				p.close();
 
 				// create index on command-location
 				p = c.prepareStatement("CREATE INDEX CRAWLED_LOCATION_IDX on CrawledCommand (location)");
 				p.execute();
 				p.close();
 			}
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			if (c!=null) try { c.close(); } catch (SQLException e) {/* ignore this */}
 		}
 	}
 	
 	public int freeCapacity() throws Exception {
 		return -1;
 	}
 	
 	public boolean freeCapacitySupported() {
 		return false;
 	}
 	
 	public boolean offerData(final URIQueueEntry data) throws Exception {
 		putData(data);
 		return true;
 	}
 	
 	public void putData(final URIQueueEntry entry) throws Exception {
 		// store unknown URI
 		if (!isClosed()) {
 			// the map is being modified by db.storeUnknownLocations, so we need to save the size first
 			final int known = storeUnknownLocations(
 					entry.getProfileID(),
 					entry.getDepth(),
 					entry.getReferences()
 			);
 			entry.setKnown(known);
 		} else {
 			logger.error(String.format(
 					"Unable to write linkmap of location '%s' to db. Database already closed.",
 					entry.getRootURI().toASCIIString()
 			));
 		}
 	}
 	
 	public void start() {		
 		this.writerThread.start();
 	}
 	
 	/**
 	 * @see IDataProvider#setDataSink(IDataSink)
 	 */
 	public void setDataSink(IDataSink<ICommand> dataSink) {
 		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
 		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
 		
 		synchronized (this.writerThread) {
 			this.sink = dataSink;
 			this.writerThread.notify();			
 		}
 	}
 	
 	public boolean isClosed() {
 		return this.closed;
 	}
 	
 	public void close() throws InterruptedException {
 		try {
 			// interrupt reader and writer
 			this.writerThread.interrupt();
 			
 			boolean saveDoubleURLsCache = true;
 			if (populateThread != null && populateThread.isAlive()) {
 				populateThread.interrupt();
 				// don't save the cache as it has not been populated completely
 				saveDoubleURLsCache = false;
 			}
 			
 			// wait for the threads to shutdown
 			this.writerThread.join(2000);
 			if (populateThread != null)
 				populateThread.join(2000);
 			
 			// close the DB
 			this.sessionFactory.close();
 			
 			// shutdown the database
 			try {
 				String dbDriver = this.config.getProperties().getProperty("connection.driver_class");
 				if (dbDriver != null && dbDriver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
 					DriverManager.getConnection("jdbc:derby:;shutdown=true");
 				}
 			} catch (SQLException e) {
 				String errMsg = e.getMessage();
 				if (!(errMsg != null && errMsg.equals("Derby system shutdown."))) {
 					this.logger.error("Unable to shutdown database.",e);
 				}
 			}
 			
 			// flush cache
 			if (saveDoubleURLsCache)
 				closeDoubleURLSet();
 			if (this.manager.getStatus().equals(Status.STATUS_ALIVE)) {
 				this.manager.removeCache(EHCACHE_NAME);
 				this.manager = null;
 			}
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while tryping to shutdown %s: %s",
 					e.getClass().getName(),
 					this.getClass().getSimpleName(),
 					e.getMessage()
 			),e);
 		} finally {
 			this.closed = true;
 		}
 	}
 	
 	/**
 	 * @see ICommandDB#isKnown(URI)
 	 */
 	public boolean isKnown(URI location) {
 		if (location == null) return false;
 		if ((populateThread == null || !populateThread.isAlive()) && !isKnownInDoubleURLs(location))
 			return false;
 		if (this.urlExistsCache.get(location) != null)
 			return true;
 		
 		return this.isKnownInDB(location);
 	}
 	
 	boolean isKnownInDB(URI location) {
 		
 		boolean known = false;
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			Query query = session.createQuery("SELECT count(location) FROM EnqueuedCommand as cmd WHERE location = ?").setParameter(0, location);
 			Long result = (Long) query.setReadOnly(true).uniqueResult();
 			known = (result != null && result.longValue() > 0);
 			
 			if (!known) {
 				query = session.createQuery("SELECT count(location) FROM CrawledCommand as cmd WHERE location = ?").setParameter(0, location);
 				result = (Long) query.setReadOnly(true).uniqueResult();
 				known = (result != null && result.longValue() > 0);
 			}
 			
 			transaction.commit();
 		} catch (Exception e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format(
 					"Unexpected '%s' while testing if location '%s' is known.",
 					e.getClass().getName(),
 					location.toASCIIString()
 			),e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 		
 		return known;
 	}	
 	
 	private List<ICommand> fetchNextCommands(int limit)  {		
 		List<ICommand> result = new ArrayList<ICommand>();
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			session = this.sessionFactory.openSession();
 			session.setFlushMode(FlushMode.COMMIT);
 			transaction = session.beginTransaction();
 			
 			Query query = session.createQuery("FROM EnqueuedCommand as cmd");
 			query.setFetchSize(limit);
 			query.setMaxResults(limit);
 			query.setCacheMode(CacheMode.IGNORE);
 			ScrollableResults sr = query.scroll(ScrollMode.FORWARD_ONLY); // (ScrollMode.FORWARD_ONLY);
 			
 			final Key key = new Key();
 			final DynamicBloomFilter bloomFilter = this.bloomFilter;
 			final Cache urlExistsCache = this.urlExistsCache;
 			
 			// loop through the available commands
 			while(sr.next() && result.size() < limit) {
 				ICommand cmd = (ICommand) sr.get()[0];
 
 				/* mark command as enqueued */
 				session.delete("EnqueuedCommand", cmd);
 				session.saveOrUpdate("CrawledCommand", cmd);
 				
 				// add command-location into caches
 				key.set(cmd.getLocation().toString().getBytes(UTF8), 1.0);
 				bloomFilter.add(key);
 				Element element = new Element(cmd.getLocation(), null);
 				urlExistsCache.put(element);
 				
 				result.add(cmd);
 			}
 			sr.close();
 			
 			transaction.commit();
 		} catch (Exception e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while fetching commands",e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}
 		}
 		
 		return result;
 	}
 	
 	private synchronized void storeCommand(ICommand cmd) {
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// store command
 			session.saveOrUpdate("EnqueuedCommand", cmd);
 			cntCrawlerQueue++;
 			cntTotal++;
 			
 			// add command-location into caches
 			putInDoubleURLs(cmd.getLocation());
 			Element element = new Element(cmd.getLocation(), null);
 			this.urlExistsCache.put(element);
 			
 			// TODO: adding to bloom filter is missing here!!!
 			
 			transaction.commit();
 			
 			// signal writer that a new URL is available
 			this.writerThread.signalNewDbData();			
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Error while writing command with location '%s' to db.", cmd.getLocation()),e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}
 		}
 	}
 	
 	/**
 	 * @see ICommandDB#enqueue(URI)
 	 */
 	public boolean enqueue(URI location, int profileID, int depth) {
 		if (location == null) return false;
 		return this.storeUnknownLocations(profileID, depth, new LinkedList<URI>(Arrays.asList(new URI[]{location}))) == 0;
 	}
 	
 	private int storeUnknownInDoubleCache(
 			final int profileID,
 			final int depth,
 			final LinkedList<URI> locations,
 			final Session session
 	) {
 		
 		final Iterator<URI> locationIterator = locations.iterator();
 		final long time = System.currentTimeMillis();
 		final Key key = new Key();
 				
 		final StringBuilder buf = new StringBuilder();
 		final int total = locations.size();
 		int counter = 0;
 		int cacheChecked = 0;
 		int known = 0;
 		
 		final boolean checkBloom = (populateThread == null || !populateThread.isAlive());
 		
 		final DynamicBloomFilter bloomFilter = this.bloomFilter;
 		final Cache urlExistsCache = this.urlExistsCache;
 		while (locationIterator.hasNext()) {
 			counter++;
 			
 			final URI loc = locationIterator.next();
 			if (checkBloom) {
 				try {
 					key.set(loc.toString().getBytes(UTF8), 1.0);
 				} catch (UnsupportedEncodingException e) {
 					/* UTF-8 support should be implemented in the JVM */
 					throw new RuntimeException(e);
 				}
 				if (!bloomFilter.membershipTest(key)) {
 					// process all URIs which are not known to the double-URIs-cache;
 					// these URIs don't have to be checked against the DB again for the
 					// cache does not return false negatives
 					bloomFilter.add(key);
 					session.saveOrUpdate("EnqueuedCommand", Command.createCommand(loc, profileID, depth));
 					cntCrawlerQueue++;
 					cntTotal++;
 					
 					Element element = new Element(loc, null);
 					this.urlExistsCache.put(element);
 					locationIterator.remove();
 					cacheChecked++;
 					
 					if (this.logger.isTraceEnabled()) {
 						buf.append(String.format("\n\t[%3d] %s", Integer.valueOf(counter), loc.toString()));
 					}					
 					
 					continue;
 				}
 			}
 				
 			if (urlExistsCache.get(loc) != null) {
 				locationIterator.remove();
 				known++;
 			}
 		}
 		
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug(String.format(
 					"Double-check of %d URI against caches with size %d (Bloom) + %d (ehcache) took %d ms." +
 					"\n\t%d unknown by bloom-filter" +
 					"\n\t%d known by ehcache" +
 					"\n\t%d left to check",
 					Integer.valueOf(total),
 					Long.valueOf(cacheSize()),
 					Long.valueOf(urlExistsCache.getMemoryStoreSize()),
 					Long.valueOf(System.currentTimeMillis() - time),
 					Integer.valueOf(cacheChecked),
 					Integer.valueOf(known),
 					Integer.valueOf(locations.size())
 			));
 		}
 		if (this.logger.isTraceEnabled() && cacheChecked > 0) {
 			logger.trace(String.format(
 					"%d new URI added to DB: %s",
 					Integer.valueOf(cacheChecked),
 					buf.toString()
 			));
 		}	
 		
 		return known;
 	}
 	
 	private int storeUnknownInDB(
 			final int profileID,
 			final int depth,
 			final LinkedList<URI> locations,
 			final Session session,
 			final int chunkSize
 	) throws UnsupportedEncodingException {
 		
 		int known = 0;
 		final long start = System.currentTimeMillis();
 		
 		String[] queues = new String[] {"EnqueuedCommand", "CrawledCommand"}; 
 		for (String queue : queues) {
 			Iterator<URI> locationsIter = locations.iterator();
 			while (locationsIter.hasNext()) {
 				URI nextLocation = locationsIter.next();
 				Query query = session.createQuery("SELECT count(id) FROM " + queue + " WHERE location = (:nextLocation)")
 									 .setParameter("nextLocation", nextLocation)
 									 .setReadOnly(true);
 				Long count = (Long) query.uniqueResult();
 				if (count != null && count.longValue() > 0) {
 					known++;
 					locationsIter.remove();
 				}
 			}
 		}
 		final long end = System.currentTimeMillis();
 		
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug(String.format(
 					"Double-check of %d URI against DB with size %d took %s ms." +
 					"\n\t%3d unknown by DB" +
 					"\n\t%3d known by DB",
					Integer.valueOf(locations.size()),
 					Long.valueOf(this.size()),
 					Long.valueOf(end-start),
					Integer.valueOf(locations.size()-known),
 					Integer.valueOf(known)
 			));
 		}
 		
 		// add new commands into DB
 		final StringBuilder buf = new StringBuilder();
 		final Cache urlExistsCache = this.urlExistsCache;
 		final Key key = new Key();
 		
 		int i = 0;
 		Iterator<URI> locationsIter = locations.iterator();
 		while (locationsIter.hasNext()) {
 			final URI location = locationsIter.next();
 			cntTotal++;
 			cntCrawlerQueue++;
 			i++;			
 			
 			// store new command into DB
 			session.saveOrUpdate("EnqueuedCommand",Command.createCommand(location,profileID,depth));	
 			
 			// add to bloom filter
 			key.set(location.toString().getBytes(UTF8), 1.0);
 			bloomFilter.add(key);
 			
 			// add to in-memory double cache
 			Element element = new Element(location, null);
 			urlExistsCache.put(element);
 			
 			// debugging output
 			if (this.logger.isTraceEnabled()) {
 				buf.append(String.format("\n\t[%3d] %s", Integer.valueOf(i), location.toString()));
 			}
 		}
 		
 		if (this.logger.isTraceEnabled() && known > 0) {
 			logger.trace(String.format(
 					"%d false-positive URI added to DB: %s",
 					known,
 					buf.toString()
 			));
 		}
 		
 		return known;
 	}
 	
 	/**
 	 * First queries the DB to remove all known locations from the list and then updates
 	 * it with the new list.
 	 * 
 	 * @param profileID the ID of the {@link ICommandProfile}, newly created 
 	 * commands should belong to
 	 * @param depth depth of the new {@link ICommand} 
 	 * @param locations the locations to add to the DB
 	 * @return the number of known locations in the given list
 	 */
 	int storeUnknownLocations(int profileID, int depth, LinkedList<URI> locations) {
 		if (locations == null || locations.size() == 0) return 0;
 		
 		int known = 0;
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			session.setFlushMode(FlushMode.COMMIT);
 			transaction = session.beginTransaction();		
 			
 			// check the cache for URL existance and put the ones not known to the
 			// cache into another list and remove them from the list which is checked
 			// against the DB below
 			known += storeUnknownInDoubleCache(profileID, depth, locations, session);
 			
 			// check which URLs are already known against the DB
 			if (locations.size() > 0)
 				known += storeUnknownInDB(profileID, depth, locations, session, 10);
 			
 			transaction.commit();
 			
 			// signal writer that a new URL is available
 			this.writerThread.signalNewDbData();
 			
 			return known;
 		} catch (Throwable e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Unexpected '%s' while writing %d new commands to db.",
 					e.getClass().getName(),
 					Integer.valueOf(locations.size())
 			),e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * @return the total size of the command db
 	 * @see ICommandDB#size()
 	 */
 	public long size() {
 		// return this.size(null);
 		return cntTotal;
 	}
 	
 	/**
 	 * @see ICommandDB#enqueuedSize()
 	 */
 	public long enqueuedSize() {
 		// return this.size("enqueued");
 		return cntCrawlerQueue;
 	}
 	
 	private long totalSize() {
 		return this.size("enqueued") + this.size("crawled");
 	}
 	
 	private long size(String type) {
 		if (type == null) throw new NullPointerException();
 		Long count = Long.valueOf(-1l);
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// query size
 			String sqlString = null;
 			if (type.equalsIgnoreCase("enqueued")) {
 				sqlString = "select count(*) from EnqueuedCommand as cmd";
 			} else if (type.equalsIgnoreCase("crawled")) {
 				sqlString = "select count(*) from CrawledCommand as cmd";
 			}
 			
 			count = (Long) session.createQuery(sqlString).uniqueResult();
 			
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Unexpected '%s' while getting size of command-db.",
 					e.getClass().getName()
 			),e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 		
 		return count.longValue();
 	}
 	
 	
 	
 	/**
 	 * Resets the command queue
 	 */
 	public void reset() {
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// delete all commands
 			session.createQuery("DELETE FROM EnqueuedCommand").executeUpdate();
 			
 			cntCrawlerQueue = cntTotal = 0L;
 			
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while reseting queue.",e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 	}
 	
 	/**
 	 * @see EventHandler#handleEvent(Event)
 	 */
 	public void handleEvent(Event event) {
 		String topic = event.getTopic();
 		
 		// check if any other component has created a command 
 		if (topic != null && topic.equals(CommandEvent.TOPIC_OID_REQUIRED)) {
 			// this is a synchronous event, so we have time to set a valid OID
 			String location = (String) event.getProperty(CommandEvent.PROP_COMMAND_LOCATION);
 			if (location != null) {
 				ICommand cmd = this.commandTracker.getCommandByLocation(URI.create(location));
 				if (cmd != null) {
 					this.storeCommand(cmd);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#db}
 	 * and to write it into the {@link CommandDB#sink data-sink}
 	 */
 	class Writer extends Thread {
 		public Writer() {
 			super("CommandDB.Writer");
 		}
 		
 		@Override
 		public void run() {
 			try {
 				synchronized (this) {
 					while (CommandDB.this.sink == null) this.wait();
 				}			
 				
 				final int chunkSize = 10;
 				List<ICommand> commands = null;
 				while(!Thread.currentThread().isInterrupted()) {
 					
 					final long time = System.currentTimeMillis();
 					commands = CommandDB.this.fetchNextCommands(chunkSize);
 					if (logger.isDebugEnabled())
 						logger.debug(String.format("fetched new chunk of %d (%d requested) new URLs to crawl in %d ms, %d queued / %d total",
 								Integer.valueOf(commands.size()),
 								Integer.valueOf(chunkSize),
 								Long.valueOf(System.currentTimeMillis() - time),
 								Long.valueOf(cntCrawlerQueue),
 								Long.valueOf(cntTotal)));
 					
 					if (commands != null && commands.size() > 0) {
 						final ICommandTracker commandTracker = CommandDB.this.commandTracker;
 						final IDataSink<ICommand> sink = CommandDB.this.sink;
 						for (ICommand command : commands) {
 							// notify the command-tracker about the creation of the command
 							if (commandTracker != null) {
 								commandTracker.commandCreated(ICommandDB.class.getName(), command);
 							}
 							
 							sink.putData(command);
 							cntCrawlerQueue--;
 						} 
 					} else {
 						// sleep for a while
 						synchronized (this) {
 							this.wait(MAX_IDLE_SLEEP);	
 						}						
 					}
 				}
 			} catch (Exception e) {
 				if (!(e instanceof InterruptedException)) {
 					logger.error(String.format("Unexpected '%s' while waiting reading commands from db.",
 							e.getClass().getName()
 					),e);
 				}
 			} finally {
 				logger.info("CommandDB.Writer shutdown finished.");
 			}		
 		}
 		
 		public synchronized void signalNewDbData() {
 			this.notify();
 		}
 	}
 }
