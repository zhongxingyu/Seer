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
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.ScrollableResults;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.onelab.filter.DynamicBloomFilter;
 import org.onelab.filter.Key;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 import org.paxle.core.data.IDataConsumer;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.data.IDataSource;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandProfileManager;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.data.db.ICommandDB;
 
 public class CommandDB implements IDataProvider<ICommand>, IDataConsumer<ICommand>, ICommandDB, ICommandProfileManager, EventHandler {
 	
 	private static final String CACHE_FILE = "command-db/doubleURLsCache.ser";
 	
 	private static final int MAX_IDLE_SLEEP = 60000;
 	private static final boolean USE_DOMAIN_BALANCING = false;
 	
 	private static final Charset UTF8 = Charset.forName("UTF-8");
 	
 	/**
 	 * Component to track {@link ICommand commands}
 	 */
 	private ICommandTracker commandTracker;
 
 	/**
 	 * A {@link IDataSink data-sink} to write the loaded {@link ICommand commands} out
 	 */
 	private IDataSink<ICommand> sink = null;	
 
 	/**
 	 * A {@link IDataSource data-source} to fetch {@link ICommand commands} from
 	 */
 	private IDataSource<ICommand> source = null;	
 
 	/**
 	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #source data-source}
 	 * and write it into the {@link #db database}. 
 	 */
 	private Reader readerThread = null;
 
 	/**
 	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #db database}
 	 * and write it into the {@link #sink data-sink}.
 	 */
 	private Writer writerThread = null;
 
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
 
 	private TreeSet<DomainInfo> domainBalancing = new TreeSet<DomainInfo>();
 	
 	private boolean closed = false;
 	
 	/**
 	 * A set holding all known URLs
 	 */
 	private DynamicBloomFilter bloomFilter = null;
 	
 	public CommandDB(URL configURL, List<URL> mappings, ICommandTracker commandTracker) {
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
 				
 				// configure caching
 				this.config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.SingletonEhCacheProvider");
 
 				// load the various mapping files
 				for (URL mapping : mappings) {
 					if (this.logger.isDebugEnabled()) this.logger.debug(String.format("Loading mapping file from URL '%s'.",mapping));
 					this.config.addURL(mapping);
 				}
 
 				// String[] sql = this.config.generateSchemaCreationScript( new org.hibernate.dialect.MySQLDialect());
 
 				// create the session factory
 				this.sessionFactory = this.config.buildSessionFactory();
 			} catch (Throwable ex) {
 				// Make sure you log the exception, as it might be swallowed
 				this.logger.error("Initial SessionFactory creation failed.",ex);
 				throw new ExceptionInInitializerError(ex);
 			}
 			this.manipulateDbSchema();
 			System.out.println(this.size());
 
 			/* ===========================================================================
 			 * Init Reader/Writer Threads
 			 * =========================================================================== */
 			this.writerThread = new Writer();
 			this.readerThread = new Reader();
 
 			/* ===========================================================================
 			 * Init Cache
 			 * =========================================================================== */
 			// init/open the double URLs cache, initializes the bloom-filter
 			openDoubleURLSet();
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/* =========================================================================
 	 * Management for the double URLs cache
 	 * ========================================================================= */
 	
 	private void closeDoubleURLSet() throws IOException {
 		final OutputStream fileOs = new FileOutputStream(new File(CACHE_FILE));
 		DataOutputStream dataOs = null;
 		try {
 			dataOs = new DataOutputStream(new BufferedOutputStream(fileOs));
 			bloomFilter.write(dataOs);
 		} finally { ((dataOs == null) ? fileOs : dataOs).close(); }
 	}
 	
 	private void openDoubleURLSet() throws IOException {
 		final File serializedFile = new File(CACHE_FILE);
 		if (serializedFile.exists() && serializedFile.canRead() && serializedFile.isFile()) {
 			logger.info("Serialized double URL set found, reading data");
 			final InputStream fileIs = new FileInputStream(serializedFile);
 			try {
 				final DataInputStream dataIs = new DataInputStream(new BufferedInputStream(fileIs));
 				bloomFilter = new DynamicBloomFilter();
 				bloomFilter.readFields(dataIs);
 			} finally { fileIs.close(); }
 		} else {
 			logger.info("Serialized double URL set not found, populating cache from DB ...");
 			bloomFilter = new DynamicBloomFilter(1437764, 10, 100000);	// creating a maximum false positive rate of 0.1 %
 			final long time = System.currentTimeMillis();
 			final int count = populateDoubleURLSet();
 			logger.info("Initialized the double URL cache with " + count + " entries in " + (System.currentTimeMillis() - time) + " ms");
 		}
 	}
 	
 	private int populateDoubleURLSet() {
 		Session session = null;
 		Transaction transaction = null;
 		int count = 0;
 		
 		try {
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			final Query query = session.createQuery("SELECT location FROM ICommand as cmd");
 			final Iterator<?> it = query.iterate();
 			final Key key = new Key();
 			while (it.hasNext()) {
 				final URI location = (URI)it.next();
				key.set(location.toString().getBytes(), 1.0);
 				bloomFilter.add(key);
 				count++;
 			}
 			
 			transaction.commit();
 		} catch (Exception e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format(
 					"Unexpected '%s' while populating the double URLs cache from the DB.",
 					e.getClass().getName()
 			),e);
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}
 		}
 		return count;
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
 		final Key key = new Key(location.toString().getBytes(UTF8));
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
 		final Key key = new Key(location.toString().getBytes(UTF8));
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
 				PreparedStatement p = c.prepareStatement("CREATE INDEX LOCATION_IDX on COMMAND (location)");
 				p.execute();
 				p.close();
 				
 				// create index on command-status
 				p = c.prepareStatement("CREATE INDEX RESULT_IDX on COMMAND (result)");
 				p.execute();
 				p.close();
 			}
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			if (c!=null) try { c.close(); } catch (SQLException e) {/* ignore this */}
 		}
 	}
 
 	public void start() {		
 		this.writerThread.start();
 		this.readerThread.start();		
 	}
 
 	/**
 	 * @see IDataConsumer#setDataSource(IDataSource)
 	 */
 	@SuppressWarnings("unchecked")
 	public void setDataSource(IDataSource dataSource) {
 		if (dataSource == null) throw new NullPointerException("The data-source is null-");
 		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
 		synchronized (this.readerThread) {
 			this.source = dataSource;
 			this.readerThread.notify();			
 		}
 	}
 
 	/**
 	 * @see IDataProvider#setDataSink(IDataSink)
 	 */
 	@SuppressWarnings("unchecked")
 	public void setDataSink(IDataSink dataSink) {
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
 			this.readerThread.interrupt();
 			this.writerThread.interrupt();
 
 			// wait for the threads to shutdown
 			this.readerThread.join(2000);
 			this.writerThread.join(2000);
 
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
 			closeDoubleURLSet();
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
 		if (!isKnownInDoubleURLs(location)) return false;
 		
 		boolean known = false;
 
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 
 			Query query = session.createQuery("SELECT count(location) FROM ICommand as cmd WHERE location = ?").setParameter(0, location);
 			Long result = (Long) query.uniqueResult();
 			known = (result != null && result.longValue() > 0);
 
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
 
 //	/**
 //	* TODO: does not work at the moment
 //	*/
 //	private void commandToXML(ICommand cmd) {
 //	Session session = sessionFactory.getCurrentSession();		
 //	Transaction transaction = null;
 //	try {
 //	transaction = session.beginTransaction();	
 //	Session dom4jSession = session.getSession(EntityMode.DOM4J);
 
 //	dom4jSession.saveOrUpdate(cmd);
 
 //	transaction.commit();
 //	} catch (HibernateException e) {
 //	if (transaction != null && transaction.isActive()) transaction.rollback(); 
 //	this.logger.error("Error while converting command to XML",e);
 //	}	
 //	}
 
 	private List<ICommand> fetchNextCommands(int limit)  {		
 		List<ICommand> result = new ArrayList<ICommand>();
 				
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			Query query = session.getNamedQuery("fromCrawlerQueue");
 			query.setFetchSize(limit);
 			ScrollableResults sr = query.scroll(); // (ScrollMode.FORWARD_ONLY);
 			
 			// loop through the available commands
 			while(sr.next() && result.size() < limit) {
 				ICommand cmd = (ICommand) sr.get()[0];
 
 				/* 
 				 * TODO: implementation of domain-balancing not finished yet
 				 */
 				if (USE_DOMAIN_BALANCING) {
 					// restrict max PPM/domain to 10PPM?
 					long maxdelay = System.currentTimeMillis() - 6000;
 
 					while (!this.domainBalancing.isEmpty() && this.domainBalancing.first().lastAccess < maxdelay) {
 						this.domainBalancing.remove(this.domainBalancing.first());
 					}
 
 					DomainInfo di = new DomainInfo(cmd.getLocation().getHost());				
 					if (this.domainBalancing.contains(di)) {
 						// skipping this domain for now
 						continue;
 					}
 					this.domainBalancing.add(di);
 				}
 				
 				/* mark command as enqueued
 				 * TODO: we need a better mechanism to decide if a command was enqueued
 				 */
 				cmd.setResultText("Enqueued");
 				session.update(cmd);
 				
 				// add command-location into cache
 				putInDoubleURLs(cmd.getLocation());
 				
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
 			session.saveOrUpdate(cmd);
 			
 			// add command-location into cache
 			putInDoubleURLs(cmd.getLocation());
 
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
 		return this.storeUnknownLocations(profileID, depth, new ArrayList<URI>(Arrays.asList(new URI[]{location}))) == 0;
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
 	int storeUnknownLocations(int profileID, int depth, ArrayList<URI> locations) {
 		if (locations == null || locations.size() == 0) return 0;
 				
 		int known = 0;
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// check the cache for URL existance and put the ones not known to the
 			// cache into another list and remove them from the list which is checked
 			// against the DB below
 			Iterator<URI> locationIterator = locations.iterator();
 			final ArrayList<URI> cacheCheckedLocations = new ArrayList<URI>();
 			final long time = System.currentTimeMillis();
 			final Key key = new Key();
 			while (locationIterator.hasNext()) {
 				final URI loc = locationIterator.next();
 				key.set(loc.toString().getBytes(UTF8), 1.0);
 				if (!bloomFilter.membershipTest(key)) {
 					// We put the locations into the cache in this initial iteration loop,
 					// because the key has been set up already.
 					// Drawback: if we are being interrupted before, the locations are
 					// marked as known although they've not been added to the DB.
 					// On the other hand, if we are being interrupted, we probably don't
 					// have time to serialize the cache to disk.
 					bloomFilter.add(key);
 					cacheCheckedLocations.add(loc);
 					locationIterator.remove();
 				}
 				/* this code is unusable due to the false positives probability of the underlying bloomfilter
 				if (isKnownInDoubleURLs(locationIterator.next())) {
 					locationIterator.remove();
 					known++;
 				}*/
 			}
 			if (logger.isDebugEnabled())
 				logger.debug("storeUnknownLocations: locations to check: " + locations.size() +
 						", cache checked: " + cacheCheckedLocations.size() +
 						" in " + (System.currentTimeMillis() - time) + " ms");
 			
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();			
 			
 			// check which URLs are already known against the DB
 			if (locations.size() > 0) {
 				HashSet<String> knownLocations = new HashSet<String>();
 				
 				int chunkSize = 10;
 				if (locations.size() <= chunkSize) {
 					Query query = session.createQuery("SELECT DISTINCT location FROM ICommand as cmd WHERE location in (:locationList)").setParameterList("locationList",locations);
 					knownLocations.addAll(query.list());
 				} else {
 					int i = 0, oldI;
 					while (i < (locations.size())) {
 						oldI = i;
 						if ((i + chunkSize) > locations.size()) {
 							i = (locations.size());
 						} else {
 							i += chunkSize;
 						}
 						List<URI> miniLocations = locations.subList(oldI, i);
 						Query query = session.createQuery("SELECT DISTINCT location FROM ICommand as cmd WHERE location in (:locationList)").setParameterList("locationList",miniLocations);
 						knownLocations.addAll(query.list());			
 					}
 				}
 				
 				// add new commands into DB
 				// process all URIs which have been checked against the DB
 				for (int i=0; i<locations.size(); i++) {
 					final URI location = locations.get(i);
 					if (knownLocations.contains(location)) {
 						// not needed to put into cache as these URIs already have been recognized as member of the cache
 						/*
 						// add command-location into cache
 						putInDoubleURLs(location);
 						*/
 						
 						known++;
 						continue;
 					}
 					logger.debug("storeUnknownLocations: adding false positive #" + i + " to DB: '" + location + "'");
 					session.saveOrUpdate(Command.createCommand(location,profileID,depth));	
 					
 					// not needed to put into cache as these URIs already have been recognized as member of the cache
 					/*
 					// add command-location into cache
 					putInDoubleURLs(location);
 					*/
 				}
 			}
 			
 			// process all URIs which are not known to the double-URIs-cache;
 			// these URIs don't have to be checked against the DB again for the
 			// cache does not return false negatives
 			for (final URI location : cacheCheckedLocations) {
 				session.saveOrUpdate(Command.createCommand(location, profileID, depth));
 			}
 			if (logger.isDebugEnabled())
 				logger.debug("storeUnknownLocations: cacheSize: " + cacheSize());
 			
 			transaction.commit();
 
 			// signal writer that a new URL is available
 			this.writerThread.signalNewDbData();
 			return known;
 		} catch (HibernateException e) {
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
 		return this.size(null);
 	}
 	
 	/**
 	 * @see ICommandDB#enqueuedSize()
 	 */
 	public long enqueuedSize() {
 		return this.size("enqueued");
 	}
 	
 	private long size(String type) {
 		Long count = Long.valueOf(-1l);
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 
 			// query size
 			String sqlString = "select count(*) from ICommand as cmd";
 			if (type != null && type.equals("enqueued")) {
 				sqlString += " WHERE (cmd.result = 'Passed') AND (cmd.resultText is null) AND (cmd.crawlerDocument is null)";
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
 			session.createQuery("DELETE FROM ICommand").executeUpdate();
 
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
 	 * @see ICommandProfileManager#getProfileByID(int)
 	 */
 	public ICommandProfile getProfileByID(int profileID) {		
 		Session session = null;
 		Transaction transaction = null;
 		final Integer profileIDInt = Integer.valueOf(profileID);
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// load profile
 			ICommandProfile profile = (ICommandProfile) session.load(ICommandProfile.class, profileIDInt);
 			transaction.commit();
 			return profile;
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Error while writing profile with ID '%d' to db.", profileIDInt),e);
 			throw e;
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 	}
 	
 	/**
 	 * @see ICommandProfileManager#storeProfile(ICommandProfile)
 	 */
 	public void storeProfile(ICommandProfile profile) {
 		if (profile == null) throw new NullPointerException("Profile was null");
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// store profile
 			session.saveOrUpdate(profile);			
 			transaction.commit();		
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Error while writing profile '%s' to db.", profile.getName()),e);
 			throw e;
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
 					/* 
 					 * TODO: this is needed for now to decide if the 
 					 * command was already enqueued. We need a better mechanism here!
 					 */
 					cmd.setResultText("Enqueued");
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
 
 				List<ICommand> commands = null;
 				while(!Thread.currentThread().isInterrupted()) {
 					commands = CommandDB.this.fetchNextCommands(10);
 					if (commands != null && commands.size() > 0) {
 						for (ICommand command : commands) {
 							// notify the command-tracker about the creation of the command
 							if (CommandDB.this.commandTracker != null) {
 								CommandDB.this.commandTracker.commandCreated(ICommandDB.class.getName(), command);
 							}
 							
 //							System.out.println(CommandDB.this.isKnown(command.getLocation()));
 							CommandDB.this.sink.putData(command);
 //							commandToXML(command);
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
 
 	/**
 	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#source data-source}
 	 * and to write it into the {@link CommandDB#db}.
 	 */	
 	class Reader extends Thread {
 		public Reader() {
 			super("CommandDB.Reader");
 		}
 
 		@Override
 		public void run() {
 			try {
 
 				synchronized (this) {
 					while (CommandDB.this.source == null) this.wait();
 				}		
 
 				while(!Thread.currentThread().isInterrupted()) {
 					ICommand command = CommandDB.this.source.getData();
 					if (command != null) {
 						// store data into db
 						CommandDB.this.storeCommand(command);
 //						CommandDB.this.commandToXML(command);						
 					}
 				}				
 			} catch (Exception e) {
 				if (!(e instanceof InterruptedException)) {
 					logger.error(String.format("Unexpected '%s' while waiting for a new command.",
 							e.getClass().getName()
 					),e);
 				}
 			} finally {
 				logger.info("CommandDB.Reader shutdown finished.");
 			}
 		}
 	}
 }
 
 class DomainInfo implements Comparable<DomainInfo> {
 	public String domainName;  
 	public long lastAccess;
 	
 	public DomainInfo(String domainName) {
 		this(domainName,System.currentTimeMillis());
 	}
 	
 	public DomainInfo(String domainName, long lastAccess) {
 		this.domainName = domainName;
 		this.lastAccess = lastAccess;
 	}
 	
 	@Override
 	public int hashCode() {
 		return domainName.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		return this.domainName.equals(((DomainInfo)obj).domainName);
 	}
 
 	public int compareTo(DomainInfo obj) {
 		if (this.equals(obj)) return 0;
 		return (int) (this.lastAccess - obj.lastAccess);
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("[%d] %s", Long.valueOf(this.lastAccess), this.domainName);
 	}
 }
