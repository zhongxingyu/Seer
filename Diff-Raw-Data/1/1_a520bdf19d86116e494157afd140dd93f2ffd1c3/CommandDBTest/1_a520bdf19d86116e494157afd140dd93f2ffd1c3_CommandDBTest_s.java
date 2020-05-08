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
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.core.threading.PPM;
 
 public class CommandDBTest extends MockObjectTestCase {
 	private static final String DERBY_CONFIG_FILE = "../DataLayerDerby/src/main/resources/resources/hibernate/derby.cfg.xml";
 	private static final String H2_CONFIG_FILE = "../DataLayerH2/src/main/resources/resources/hibernate/H2.cfg.xml";
 	
 	@SuppressWarnings("unused")
 	private static final String POSTGRESQL_CONFIG_FILE = "../DataLayerPostgreSQL/src/main/resources/resources/hibernate/postgresql.cfg.xml";
 	
 	private static final String DERBY_CONNECTION_URL = "jdbc:derby:target/command-db;create=true";
 	private static final String H2_CONNECTION_URL = "jdbc:h2:target/command-db/cdb;MVCC=TRUE";
 	
 	@SuppressWarnings("unused")
 	private static final String POSTGRESQL_CONNECTION_URL = "jdbc:postgresql://%s/command-db";
 	
 	private ICommandTracker cmdTracker;
 	private CommandDB cmdDB;
 	
 	/**
 	 * @return the hibernate mapping files to use
 	 * @throws MalformedURLException 
 	 */
 	private List<URL> getMappingFiles() throws MalformedURLException {
 		final File mappingFilesDir = new File("src/main/resources/resources/hibernate/mapping/command/");
 		assertTrue(mappingFilesDir.exists());
 		
 		final FileFilter mappingFileFilter = new WildcardFileFilter("*.hbm.xml");		
 		File[] mappingFiles = mappingFilesDir.listFiles(mappingFileFilter);
 		assertNotNull(mappingFiles);
 		assertEquals(1, mappingFiles.length);
 		
 		List<URL> mappingFileURLs = new ArrayList<URL>();
 		for (File mappingFile : mappingFiles) {
 			mappingFileURLs.add(mappingFile.toURL());
 		}
 		
 		return mappingFileURLs;
 	}
 		
 	/**
 	 * @return the hibernate config file to use
 	 * @throws MalformedURLException 
 	 */
 	private URL getConfigFile(String configFile) throws MalformedURLException {
 		final File derbyConfigFile = new File(configFile);
 		assertTrue(derbyConfigFile.exists());
 		return derbyConfigFile.toURL();
 	}	
 	
 	/**
 	 * @return additional properties that should be passed to hibernate
 	 */
 	private Properties getExtraProperties(String connectionString) {
 		Properties props = new Properties();
 		props.put("connection.url", connectionString);
 		props.put("hibernate.connection.url", connectionString);
 		return props;
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		System.setProperty("paxle.data", "target");		
 		
 		// create a dummy command tracker
 		this.cmdTracker = mock(ICommandTracker.class);
 		
 		// delete dirs
 		this.deleteTestDataDirs();
 	}
 	
 	private void setupDB(String hibernateConfigFile, String connectionURL) throws MalformedURLException {	
 		// create and init the command-db
 		this.cmdDB = new CommandDB(
 				this.getConfigFile(hibernateConfigFile),
 				this.getMappingFiles(),
 				this.getExtraProperties(connectionURL),
 				this.cmdTracker		
 		);
 		
 		// startup DB
 		this.cmdDB.start();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 		// close DB
 		if (cmdDB != null) this.cmdDB.close();
 		
 		// delete dirs
 		this.deleteTestDataDirs();
 	}
 	
 	private void deleteTestDataDirs() throws IOException {
 		
 		// delete data directory
 		File dbDir = new File("target/command-db");
 		if(dbDir.exists()) FileUtils.deleteDirectory(dbDir);
 		
 		File bloomDir = new File("target/double-urls-caches");
 		if (bloomDir.exists()) FileUtils.deleteDirectory(bloomDir);
 	}
 	
 	/**
 	 * A dummy data-sink which just prints out the data
 	 */
 	private class DummyDataSink implements IDataSink<ICommand> {
 		private final Semaphore semaphore;
 		private final PPM ppm = new PPM();
 		private final Log logger = LogFactory.getLog(this.getClass());
 		
 		private long counter = 0;
 		private long lastCounter = 0;
 		private long timestamp = 0;
 		
 		public DummyDataSink(Semaphore semaphore) {
 			this.semaphore = semaphore;
 		}
 		
 		public void putData(ICommand cmd) throws Exception {
 			this.ppm.trick();
 			this.semaphore.release();
 			this.counter++;
 			if (this.counter % 1000 == 0) {
 				System.err.println(this.counter + " commands dequeued so far.");
 			}
 			
 			if (System.currentTimeMillis() - timestamp > 60000) {
 				this.logger.error(String.format(
 						"%d commands dequeued in %d ms with '%d' cpm.",
 						Long.valueOf(counter-lastCounter),
 						Long.valueOf(System.currentTimeMillis()-this.timestamp),
 						Integer.valueOf(this.ppm.getPPM())
 				));
 				
 				this.timestamp = System.currentTimeMillis();
 				this.lastCounter = counter;
 			}
 		}
 
 		public int freeCapacity() throws Exception {
 			return -1;
 		}
 
 		public boolean freeCapacitySupported() {
 			return false;
 		}
 
 		public boolean offerData(ICommand cmd) throws Exception {
 			this.putData(cmd);
 			return true;
 		}
 	}
 
 	private void storeUnknownLocation() throws InterruptedException {
 		final int MAX = 10;
 		
 		// command-tracker must be called MAX times
 		checking(new Expectations() {{
 			exactly(MAX).of(cmdTracker).commandCreated(with(equal("org.paxle.data.db.ICommandDB")), with(any(ICommand.class)));
 		}});
 		
 		// generated test URI
 		LinkedList<URI> knownURIs;
 		LinkedList<URI> testURI = new LinkedList<URI>();
 		for (int i=0; i < MAX; i++) {
 			testURI.add(URI.create("http://test.paxle.net/" + i));
 		}
 		knownURIs = (LinkedList<URI>) testURI.clone();
 		
 		// store them to DB
 		int knownCount = this.cmdDB.storeUnknownLocations(0, 1, testURI);
 		assertEquals(0, knownCount);
 
 		// create a dummy data-sink
 		Semaphore s = null;
 		this.cmdDB.setDataSink(new DummyDataSink(s = new Semaphore(-MAX + 1)));		
 		
 		// wait for all commands to be enqueued
 		boolean acquired = s.tryAcquire(3, TimeUnit.SECONDS);
 		assertTrue(acquired);
 		
 		// testing if all URI are known to the DB
 		for (URI knownURI : knownURIs) {
 			// command must be marked as crawled
 			boolean known = this.cmdDB.isKnownInDB(knownURI,"CrawledCommand");
 			assertTrue("Unkown URI: " + knownURI, known);
 			
 			// command must not be enqueued
 			known = this.cmdDB.isKnownInDB(knownURI,"EnqueuedCommand");
 			assertFalse("Unkown URI: " + knownURI, known);
 			
 			// command must be known to the cache
 			known = this.cmdDB.isKnownInCache(knownURI);
 			assertTrue(known);
 			
 			// command must be known to the bloom filter
 			known = this.cmdDB.isKnownInDoubleURLs(knownURI);
 			assertTrue(known);
 		}
 	}
 	
 	public void testStoreUnknownLocationDerby() throws MalformedURLException, InterruptedException {
 		// setup DB
 		this.setupDB(DERBY_CONFIG_FILE, DERBY_CONNECTION_URL);
 		
 		// start test
 		this.storeUnknownLocation();
 	}
 	
 	public void testStoreUnknownLocationH2() throws MalformedURLException, InterruptedException {
 		// setup DB
 		this.setupDB(H2_CONFIG_FILE, H2_CONNECTION_URL);
 		
 		// start test
 		this.storeUnknownLocation();
 	}
 	
 	public void _testVeryLargeURLSet() throws MalformedURLException, InterruptedException {		
 		final int MAX = 1000000;
 		final int chunkSize = 1000;
 		
 		System.setProperty("derby.storage.pageCacheSize", "2000");  // default 1000
 		//System.setProperty("derby.storage.pageSize", "32768");      // default 4096 bytes
 		
 		// setup DB
 		// this.setupDB(POSTGRESQL_CONFIG_FILE, String.format(POSTGRESQL_CONNECTION_URL,"192.168.10.201"));
 		this.setupDB(DERBY_CONFIG_FILE, DERBY_CONNECTION_URL);		
 		
 		// command-tracker must be called MAX times
 		checking(new Expectations() {{
 			exactly(MAX).of(cmdTracker).commandCreated(with(equal("org.paxle.data.db.ICommandDB")), with(any(ICommand.class)));
 		}});
 		
 		final Semaphore s = new Semaphore(-MAX + 1);
 		
 		new Thread() {
 			public void run() {		
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {}
 				
 				// create a dummy data-sink
 				cmdDB.setDataSink(new DummyDataSink(s));	
 			};
 		}.start();
 		
 		// store new commands
 		long start = System.currentTimeMillis();
 		
 		LinkedList<URI> testURI = new LinkedList<URI>();
 		for (int i=1; i <= MAX; i++) {			
 			URI nextCommand = URI.create("http://test.paxle.net/" + i);
 			testURI.add(nextCommand);
 			
 			if (i % chunkSize == 0 || i == MAX) {
 				int known = this.cmdDB.storeUnknownLocations(0, 1, testURI);				
 				assertEquals(0, known);
 				testURI.clear();
 			}
 		}
 
 		// wait for all commands to be enqueued
 		s.acquire();
 		
 		System.out.println(String.format(
 				"Storing and loading %d URL took %d ms",
 				Integer.valueOf(MAX),
 				Long.valueOf(System.currentTimeMillis()-start)
 		));
 	}
 }
