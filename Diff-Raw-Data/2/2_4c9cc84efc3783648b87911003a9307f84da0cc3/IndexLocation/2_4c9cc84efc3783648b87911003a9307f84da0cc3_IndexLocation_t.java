 package com.gentics.cr.util.indexing;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.Constants;
 
 /**
  * 
  * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
  * 
  * @version $Revision: 131 $
  * @author $Author: supnig@constantinopel.at $
  * 
  */
 
 public abstract class IndexLocation {
 
 	protected static final Logger log = Logger.getLogger(IndexLocation.class);
 
 	/**
 	 * Configuration key for reopen check.
 	 */
 	private static final String REOPEN_CHECK_KEY = "reopencheck";
 
 	/**
 	 * Value of reopen check configuration key for checking timestamp.
 	 */
 	private static final String REOPEN_CHECK_TIMESTAMP = "timestamp";
 
 	protected static final String REOPEN_FILENAME = "reopen";
 	protected static final String INDEX_LOCATIONS_KEY = "indexLocations";
 	protected static final String INDEX_PATH_KEY = "path";
 	private static final String INDEX_LOCATION_CLASS_KEY = "indexLocationClass";
 
 	public static final String INDEX_EXTENSIONS_KEY = "extensions";
 	public static final String INDEX_EXTENSION_CLASS_KEY = "class";
 
 	/**
 	 * default value for index location class.
 	 */
 	// private static final String INDEX_LOCATION_CLASS_DEFAULT =
 	// "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation";
 	/**
 	 * Config key for a classname. The given class is called to calculate the
 	 * periodical execution flag of the indexer.
 	 */
 	private static final String PERIODICALCLASS_KEY = "periodicalClass";
 	private static Hashtable<Object, IndexLocation> indexmap;
 	private static final String LOCK_DETECTION_KEY = "LOCKDETECTION";
 
 	/**
 	 * Configuration key for the interval in which new jobs are created.
 	 */
 	private static final String PERIODICAL_INTERVAL_KEY = "INTERVAL";
 
 	/**
 	 * The key in the configuration for specifying the update job implementation
 	 * class.
 	 */
 	public static final String UPDATEJOBCLASS_KEY = "updatejobclass";
 	/**
 	 * name of the class used for an updatejob.
 	 */
 	private static final String DEFAULT_UPDATEJOBCLASS = "com.gentics.cr.lucene.indexer.index.CRLuceneIndexJob";
 
 	/**
 	 * The key in the configuration for specifying the delete job implementation
 	 * class.
 	 */
 	public static final String DELETEJOBCLASS_KEY = "deletejobclass";
 
 	/**
 	 * name of the class used for a delete job.
 	 */
 	private static final String DEFAULT_DELETEJOBCLASS = "com.gentics.cr.lucene.indexer.index.CRLuceneDeleteJob";
 
 	/**
 	 * The key in the configuration for specifying the optimize job
 	 * implementation class.
 	 */
 	public static final String OPTIMIZEJOBCLASS_KEY = "optimizejobclass";
 
 	/**
 	 * name of the default class used for a optimize job.
 	 */
 	private static final String DEFAULT_OPTIMIZEJOBCLASS = "com.gentics.cr.lucene.indexer.index.CRLuceneOptimizeJob";
 
 	// Instance Members
 	/**
 	 * Holds the index intervals for each index part if configured individually.
 	 */
 	private HashMap<String, Integer> indexIntervals = null;
 
 	/**
 	 * Holds the time when each index part was checked.
 	 */
 	private Hashtable<String, Date> indexJobCreationTimes = new Hashtable<String, Date>();
 
 	protected HashMap<String, IndexExtension> extensions;
 
 	private IndexJobQueue queue = null;
 	protected CRConfig config;
 	private IPeriodicalIndexConfig periodicalIndexConfig;
 	private int periodical_interval = 60; // 60 seconds
 	private Thread periodical_thread;
 	private boolean lockdetection = false;
 
 	/**
 	 * mark if we should make a reopencheck for this index location.
 	 */
 	protected boolean reopencheck = false;
 
 	/**
 	 * mark if we only should check the timestamp of the file and not remove it
 	 * afterwards.
 	 */
 	protected boolean reopencheckTimestamp = false;
 
 	/**
 	 * Get the IndexLocation's interval that is used to create new jobs.
 	 * 
 	 * @return interval as int
 	 */
 	public final int getInterval() {
 		return this.periodical_interval;
 	}
 
 	/**
 	 * Get the interval used to create new jobs for a specific part.
 	 * 
 	 * @param indexJobConfiguration
 	 *            configuration of the index Job
 	 * @return interval as integer, -1 in case no special interval for this
 	 *         index part is defined
 	 */
 	public final int getInterval(final String partName) {
 		if (indexIntervals != null) {
 			if (indexIntervals.containsKey(partName)) {
 				return indexIntervals.get(partName).intValue();
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Creates the reopen file to make portlet reload the index.
 	 */
 	public abstract void createReopenFile();
 
 	/**
 	 * Checks Lock and throws Exception if lock exists.
 	 * 
 	 * @throws Exception
 	 *             if lock already exists
 	 */
 	public abstract void checkLock() throws Exception;
 
 	/**
 	 * Constructor for index location mainly reads the configuration for all
 	 * sort of IndexLocations.
 	 * 
 	 * @param givenConfig
 	 *            configuration of the index location
 	 */
 	protected IndexLocation(final CRConfig givenConfig) {
 		config = givenConfig;
 		queue = new IndexJobQueue(config);
 
 		periodicalIndexConfig = initPeriodicalIndexConfig(config);
 
 		periodical_interval = config.getInteger(PERIODICAL_INTERVAL_KEY, periodical_interval);
 		String reopenString = config.getString(REOPEN_CHECK_KEY);
 		if (REOPEN_CHECK_TIMESTAMP.equals(reopenString)) {
 			reopencheck = true;
 			reopencheckTimestamp = true;
 		} else {
 			reopencheck = config.getBoolean(REOPEN_CHECK_KEY, reopencheck);
 		}
 		lockdetection = config.getBoolean(LOCK_DETECTION_KEY, lockdetection);
 		initIndexIntervals();
 
 		registerExtensions(config);
 
 	}
 
 	/**
 	 * Initialize a config class for the periodical execution flag of the
 	 * indexer. If init of the configured class fails, a fallback class is
 	 * returned.
 	 * 
 	 * @return configclass
 	 * @param config
 	 */
 	private IPeriodicalIndexConfig initPeriodicalIndexConfig(final CRConfig config) {
 		String className = config.getString(PERIODICALCLASS_KEY);
 
 		if (className != null && className.length() != 0) {
 			try {
 				Class<?> clazz = Class.forName(className);
 				Constructor<?> constructor = clazz.getConstructor(CRConfig.class);
 				return (IPeriodicalIndexConfig) constructor.newInstance(config);
 			} catch (Exception e) {
 				log.warn("Cound not init configured " + PERIODICALCLASS_KEY + ": " + className, e);
 			}
 		}
 		return new PeriodicalIndexStandardConfig(config);
 	}
 
 	/**
 	 * Check the index intervals of all IndexParts and init the Map
 	 * indexIntervals.
 	 */
 	private void initIndexIntervals() {
 		Hashtable<String, CRConfigUtil> indexParts = getCRMap();
 		for (String indexPartName : indexParts.keySet()) {
 			CRConfigUtil indexPartConfig = indexParts.get(indexPartName);
 			int interval = indexPartConfig.getInteger(PERIODICAL_INTERVAL_KEY, -1);
 			if (interval != -1) {
 				if (indexIntervals == null) {
 					indexIntervals = new HashMap<String, Integer>();
 				}
 				indexIntervals.put(indexPartName, new Integer(interval));
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void initializeQueue() {
 		if (periodicalIndexConfig.isPeriodical()) {
 			periodical_thread = new Thread(new Runnable() {
 				public void run() {
 					boolean interrupted = false;
 
 					try {
 						Thread.sleep(periodicalIndexConfig.getFirstJobStartDelay() * Constants.MILLISECONDS_IN_A_SECOND);
 					} catch (InterruptedException ex) {
 						interrupted = true;
 					}
 
 					while (periodicalIndexConfig.isPeriodical() && !interrupted
 							&& !Thread.currentThread().isInterrupted()) {
 						try {
 							createAllCRIndexJobs();
 							Thread.sleep(periodical_interval * Constants.MILLISECONDS_IN_A_SECOND);
 
 						} catch (InterruptedException ex) {
 							interrupted = true;
 						}
 					}
 				}
 			});
 			periodical_thread.setName("PeriodicIndexJobCreator");
 			periodical_thread.setDaemon(true);
 			periodical_thread.start();
 
 		}
 		this.queue.startWorker();
 	}
 
 	/**
 	 * Returns if the user has configured lock detection for the index location.
 	 * 
 	 * @return <code>true</code> if lock detection is configured
 	 */
 	public final boolean hasLockDetection() {
 		return this.lockdetection;
 	}
 
 	/**
 	 * Gets the index location configured in config.
 	 * 
 	 * @param config if the config does not hold the param indexLocation or if
 	 *            indexLocation = "RAM", an RAM Directory will be created and
 	 *            returned
 	 * @return initialized IndexLocation
 	 */
 	public static synchronized IndexLocation getIndexLocation(final CRConfig config) {
 		IndexLocation dir = null;
 		Object key = config;
 		if (key == null) {
 			log.error("COULD NOT FIND CONFIG FOR INDEXLOCATION" + ". check config @ ");
 			return null;
 		}
 		if (indexmap == null) {
 			indexmap = new Hashtable<Object, IndexLocation>();
 			dir = createNewIndexLocation(config);
 			indexmap.put(key, dir);
 		} else {
 			dir = indexmap.get(key);
 			if (dir == null) {
 				dir = createNewIndexLocation(config);
 				indexmap.put(key, dir);
 			}
 		}
 		return dir;
 	}
 
 	/**
 	 * Create new IndexLocation for the configured Implementation of
 	 * {@link AbstractUpdateCheckerJob}.
 	 * 
 	 * @param config
 	 *            {@link CRConfig} of the actual indexLocation
 	 * @return IndexLocation that can be used for all configured Implementations
 	 *         of {@link AbstractUpdateCheckerJob}
 	 */
 	private static IndexLocation createNewIndexLocation(final CRConfig config) {
 		Class<? extends IndexLocation> indexLocationClass = getIndexLocationClass(config);
 		try {
 			Constructor<? extends IndexLocation> indexLocationConstructor = indexLocationClass
 					.getDeclaredConstructor(new Class[] { CRConfig.class });
 			IndexLocation instance = indexLocationConstructor.newInstance(config);
 			// Start worker threads on created indexLocation
 			instance.initializeQueue();
 			return instance;
 		} catch (SecurityException e) {
 			log.error("Cannot get Constructor(CRConfig) for IndexLocation class \"" + indexLocationClass.getName()
 					+ "\"", e);
 		} catch (NoSuchMethodException e) {
 			log.error("Cannot get Constructor(CRConfig) for IndexLocation class \"" + indexLocationClass.getName()
 					+ "\"", e);
 		} catch (IllegalArgumentException e) {
 			log.error("Cannot invoke Constructor for IndexLocation class \"" + indexLocationClass.getName() + "\"", e);
 		} catch (InstantiationException e) {
 			log.error("Cannot invoke Constructor for IndexLocation class \"" + indexLocationClass.getName() + "\"", e);
 		} catch (IllegalAccessException e) {
 			log.error("Cannot invoke Constructor for IndexLocation class \"" + indexLocationClass.getName() + "\"", e);
 		} catch (InvocationTargetException e) {
 			log.error("Cannot invoke Constructor for IndexLocation class \"" + indexLocationClass.getName() + "\"", e);
 		}
 		return new DefaultIndexLocation(config);
 	}
 
 	private static Class<? extends IndexLocation> getIndexLocationClass(final CRConfig config) {
 		String indexLocationClassName = config.getString(INDEX_LOCATION_CLASS_KEY);
 		Class<? extends IndexLocation> indexLocationClass = null;
 		if (indexLocationClassName == null) {
 			Object indexesObject = config.get(CRConfig.CR_KEY);
 			if (indexesObject != null && indexesObject instanceof GenericConfiguration) {
 				GenericConfiguration indexes = (GenericConfiguration) config.get(CRConfig.CR_KEY);
 				for (Entry<String, GenericConfiguration> subConfigEntry : indexes.getSubConfigs().entrySet()) {
 					String subConfigKey = subConfigEntry.getKey();
 					GenericConfiguration subConfig = subConfigEntry.getValue();
 					Class<? extends AbstractUpdateCheckerJob> subConfigClass = getUpdateJobImplementationClass(new CRConfigUtil(
 							subConfig, config.getName() + "." + subConfigKey));
 					try {
 						String nextIndexLocationClassName = subConfigClass.getField("INDEXLOCATIONCLASS")
 								.get(subConfigClass).toString();
 						if (indexLocationClassName == null) {
 							indexLocationClassName = nextIndexLocationClassName;
 						} else if (!indexLocationClassName.equals(nextIndexLocationClassName)) {
 							// TODO add advanced error handling. e.g. different
 							// classes can be
 							// valid if they are subclasses of each other. In
 							// this case we
 							// should create an instance of the deepest
 							// configured subclass.
 							log.error("Not all of your configured implementations have the"
 									+ " same value in the field \"INDEXLOCATIONCLASS\".");
 						}
 					} catch (NoSuchFieldException e) {
 						log.error(subConfigClass.getName() + " has no field named " + "\"INDEXLOCATIONCLASS\"", e);
 					} catch (SecurityException e) {
 						log.error("Cannot access Field \"INDEXLOCATIONCLASS\" on " + subConfigClass.getName() + ".", e);
 					} catch (IllegalArgumentException e) {
 						log.error(
 							"Error getting static Field \"INDEXLOCATIONLCASS\" of" + " class "
 									+ subConfigClass.getName(),
 							e);
 					} catch (IllegalAccessException e) {
 						log.error("Cannot access Field \"INDEXLOCATIONCLASS\" on " + subConfigClass.getName() + ".", e);
 					}
 				}
 			}
 		}
 		if (indexLocationClassName != null) {
 			try {
 				Class<?> indexLocationClassGeneric = Class.forName(indexLocationClassName);
 				indexLocationClass = indexLocationClassGeneric.asSubclass(IndexLocation.class);
 			} catch (ClassNotFoundException e) {
 				log.error("Cannot find class the IndexLocationClass defined in the"
 						+ " config or your UpdateJobImplementation. Therefore i cannot"
 						+ " create a specific IndexLocation for the configured"
 						+ " AbstractUpdateCheckerJob implementation.", e);
 			}
 		}
 		if (indexLocationClass == null) {
 			return IndexLocation.class;
 		} else {
 			return indexLocationClass;
 		}
 	}
 
 	/**
 	 * Helper method to get Class of UpdateJobImplementation.
 	 * 
 	 * @param config
 	 *            {@link CRConfig} to
 	 * @return
 	 */
 	private static Class<? extends AbstractUpdateCheckerJob> getDeleteJobImplementationClass(CRConfig config) {
 		Class<?> deletejobImplementationClassGeneric;
 		Class<? extends AbstractUpdateCheckerJob> deletejobImplementationClass;
 		String deletejobimplementationClassName = config.getString(DELETEJOBCLASS_KEY);
 		if (deletejobimplementationClassName == null) {
 			deletejobimplementationClassName = DEFAULT_DELETEJOBCLASS;
 		}
 
 		try {
 			deletejobImplementationClassGeneric = Class.forName(deletejobimplementationClassName);
 			deletejobImplementationClass = deletejobImplementationClassGeneric
 					.asSubclass(AbstractUpdateCheckerJob.class);
 			return deletejobImplementationClass;
 		} catch (ClassNotFoundException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Helper method to get Class of UpdateJobImplementation.
 	 * 
 	 * @param config
 	 *            {@link CRConfig} to
 	 * @return
 	 */
 	private static Class<? extends AbstractUpdateCheckerJob> getOptimizeJobImplementationClass(CRConfig config) {
 		Class<?> optimizejobImplementationClassGeneric;
 		Class<? extends AbstractUpdateCheckerJob> optimizejobImplementationClass;
 		String optimizejobimplementationClassName = config.getString(OPTIMIZEJOBCLASS_KEY);
 		if (optimizejobimplementationClassName == null) {
 			optimizejobimplementationClassName = DEFAULT_OPTIMIZEJOBCLASS;
 		}
 
 		try {
 			optimizejobImplementationClassGeneric = Class.forName(optimizejobimplementationClassName);
 			optimizejobImplementationClass = optimizejobImplementationClassGeneric
 					.asSubclass(AbstractUpdateCheckerJob.class);
 			return optimizejobImplementationClass;
 		} catch (ClassNotFoundException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Helper method to get Class of UpdateJobImplementation.
 	 * 
 	 * @param config
 	 *            {@link CRConfig} to
 	 * @return
 	 */
 	private static Class<? extends AbstractUpdateCheckerJob> getUpdateJobImplementationClass(CRConfig config) {
 		Class<?> updatejobImplementationClassGeneric;
 		Class<? extends AbstractUpdateCheckerJob> updatejobImplementationClass;
 		String updatejobimplementationClassName = config.getString(UPDATEJOBCLASS_KEY);
 		if (updatejobimplementationClassName == null) {
 			updatejobimplementationClassName = DEFAULT_UPDATEJOBCLASS;
 		}
 
 		try {
 			updatejobImplementationClassGeneric = Class.forName(updatejobimplementationClassName);
 			updatejobImplementationClass = updatejobImplementationClassGeneric
 					.asSubclass(AbstractUpdateCheckerJob.class);
 			return updatejobImplementationClass;
 		} catch (ClassNotFoundException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Get number of documents in Index
 	 * 
 	 * @return doccount
 	 */
 	public int getDocCount() {
 		// TODO implement this generic
 		int count = 0;
 		return count;
 	}
 
 	/**
 	 * Tests if the IndexLocation contains an existing Index and returns true if
 	 * it does.
 	 * 
 	 * @return true if index exists, otherwise false
 	 */
 	public boolean isContainingIndex() {
 
 		return getDocCount() > 0;
 	}
 
 	public abstract boolean isOptimized();
 
 	public abstract boolean isLocked();
 
 	/**
 	 * Creates a new CRIndexJob for the given CRConfig and adds the job to the
 	 * queue
 	 * 
 	 * @param config
 	 * @param configmap
 	 * @return
 	 */
 	public boolean createCRIndexJob(CRConfig config, Hashtable<String, CRConfigUtil> configmap) {
 		Class<? extends AbstractUpdateCheckerJob> updatejobImplementationClass = getUpdateJobImplementationClass(config);
 		AbstractUpdateCheckerJob indexJob = null;
 		try {
 			Constructor<? extends AbstractUpdateCheckerJob> updatejobImplementationClassConstructor = updatejobImplementationClass
 					.getConstructor(new Class[] { CRConfig.class, IndexLocation.class, Hashtable.class });
 			Object indexJobObject = updatejobImplementationClassConstructor.newInstance(config, this, configmap);
 			indexJob = (AbstractUpdateCheckerJob) indexJobObject;
 			updateIndexJobCreationTime(config);
 			return queue.addJob(indexJob);
 		} catch (ClassCastException e) {
 			log.error("Please configure an implementation of " + AbstractUpdateCheckerJob.class + " ", e);
 		} catch (SecurityException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		} catch (NoSuchMethodException e) {
 			log.error("Cannot find constructor for creating a new IndexJob", e);
 		} catch (IllegalArgumentException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InstantiationException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (IllegalAccessException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InvocationTargetException e) {
 			log.error("Error creating a new IndexJob", e);
 		}
 		return false;
 	}
 
 	/**
 	 * Update the last index job creation time in the local map.
 	 * 
 	 * @param indexJobConfig
 	 *            config of the index job
 	 */
 	private void updateIndexJobCreationTime(final CRConfig indexJobConfig) {
 		if (indexJobCreationTimes == null) {
 			resetIndexJobCreationTimes();
 		}
 		indexJobCreationTimes.put(indexJobConfig.getName(), new Date());
 
 	}
 
 	/**
 	 * resets all creation times for index jobs.
 	 */
 	public final void resetIndexJobCreationTimes() {
 		indexJobCreationTimes = new Hashtable<String, Date>();
 	}
 
 	private static final String CR_KEY = "CR";
 
 	/**
 	 * Creates jobs for all configured CRs.
 	 */
 	public final void createAllCRIndexJobs() {
 
 		Hashtable<String, CRConfigUtil> configs = getCRMap();
 
 		for (Entry<String, CRConfigUtil> e : configs.entrySet()) {
 
 			CRConfigUtil indexJobConfiguration = e.getValue();
 			String partName = indexJobConfiguration.getName();
 			AbstractUpdateCheckerJob currentJob = queue.getCurrentJob();
 			int partInterval = getInterval(partName);
 			boolean createJob = true;
 			if (currentJob != null && currentJob.identifyer.equals(partName)) {
 				log.debug("skipping creation of " + partName + " because its already running.");
 				createJob = false;
 			} else if (partInterval != -1) {
 				long now = new Date().getTime();
 				if (indexJobCreationTimes.containsKey(partName)) {
 					long lastRun = indexJobCreationTimes.get(partName).getTime();
 					long intervalMilliseconds = (partInterval * Constants.MILLISECONDS_IN_A_SECOND);
 					if (now - lastRun < intervalMilliseconds) {
 						createJob = false;
 					}
 				}
 			}
 			if (createJob) {
 				createCRIndexJob(new CRConfigUtil(indexJobConfiguration, partName), configs);
 			}
 
 		}
 
 	}
 
 	/**
 	 * Creates a job that clears the index.
 	 * 
 	 * @return true if job was added to the queue
 	 */
 	public boolean createClearJob() {
 		Class<? extends AbstractUpdateCheckerJob> deletejobImplementationClass = getDeleteJobImplementationClass(config);
 		AbstractUpdateCheckerJob indexJob = null;
 		try {
 			Constructor<? extends AbstractUpdateCheckerJob> deletejobImplementationClassConstructor = deletejobImplementationClass
 					.getConstructor(new Class[] { CRConfig.class, IndexLocation.class, Hashtable.class });
 			Object indexJobObject = deletejobImplementationClassConstructor.newInstance(config, this, null);
 			indexJob = (AbstractUpdateCheckerJob) indexJobObject;
 			return this.queue.addJob(indexJob);
 		} catch (ClassCastException e) {
 			log.error("Please configure an implementation of " + AbstractUpdateCheckerJob.class + " ", e);
 		} catch (SecurityException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		} catch (NoSuchMethodException e) {
 			log.error("Cannot find constructor for creating a new IndexJob", e);
 		} catch (IllegalArgumentException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InstantiationException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (IllegalAccessException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InvocationTargetException e) {
 			log.error("Error creating a new IndexJob", e);
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a job that optimizes the index.
 	 * 
 	 * @return true if job was added to the queue
 	 */
 	public boolean createOptimizeJob() {
 		Class<? extends AbstractUpdateCheckerJob> optimizejobImplementationClass = getOptimizeJobImplementationClass(config);
 		AbstractUpdateCheckerJob indexJob = null;
 		try {
 			Constructor<? extends AbstractUpdateCheckerJob> optimizejobImplementationClassConstructor = optimizejobImplementationClass
 					.getConstructor(new Class[] { CRConfig.class, IndexLocation.class, Hashtable.class });
 			Object indexJobObject = optimizejobImplementationClassConstructor.newInstance(config, this, null);
 			indexJob = (AbstractUpdateCheckerJob) indexJobObject;
 			return this.queue.addJob(indexJob);
 		} catch (ClassCastException e) {
 			log.error("Please configure an implementation of " + AbstractUpdateCheckerJob.class + " ", e);
 		} catch (SecurityException e) {
 			log.error("Cannot load class for creating a new IndexJob", e);
 		} catch (NoSuchMethodException e) {
 			log.error("Cannot find constructor for creating a new IndexJob", e);
 		} catch (IllegalArgumentException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InstantiationException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (IllegalAccessException e) {
 			log.error("Error creating a new IndexJob", e);
 		} catch (InvocationTargetException e) {
 			log.error("Error creating a new IndexJob", e);
 		}
 		return false;
 	}
 
 	/**
 	 * @return a map of the configured CRs
 	 */
 	public final Hashtable<String, CRConfigUtil> getCRMap() {
 		Hashtable<String, CRConfigUtil> map = new Hashtable<String, CRConfigUtil>();
 		GenericConfiguration crConfigs = (GenericConfiguration) config.get(CR_KEY);
 		if (crConfigs != null) {
 			Hashtable<String, GenericConfiguration> configs = crConfigs.getSubConfigs();
 
 			for (Entry<String, GenericConfiguration> e : configs.entrySet()) {
 				try {
 					map.put(config.getName() + "." + e.getKey(), new CRConfigUtil(e.getValue(), config.getName() + "."
 							+ e.getKey()));
 				} catch (Exception ex) {
 					String name = "<no config name>";
 					String key = "<no key>";
 					CRException cex = new CRException(ex);
 					if (e != null && e.getKey() != null) {
 						key = e.getKey();
 					}
					if (config.getName() != null) {
 						name = config.getName();
 					}
 					log.error("Error while creating cr map for " + name + "." + key + " - " + cex.getMessage(), cex);
 				}
 			}
 		} else {
 			log.debug("There are no crs configured for indexing. Config: " + config.getName());
 		}
 		return map;
 	}
 
 	/**
 	 * Returns the IndexJobQueue
 	 * 
 	 * @return
 	 */
 	public IndexJobQueue getQueue() {
 		return this.queue;
 	}
 
 	/**
 	 * Tests if this IndexLocation has turned on periodical indexing
 	 * 
 	 * @return
 	 */
 	public boolean isPeriodical() {
 		return periodicalIndexConfig.isPeriodical();
 	}
 
 	public IPeriodicalIndexConfig getPeriodicalIndexConfig() {
 		return periodicalIndexConfig;
 	}
 
 	/**
 	 * Stops all Index workers
 	 */
 	public void stop() {
 		if (this.periodical_thread != null && this.periodical_thread.isAlive()) {
 			this.periodical_thread.interrupt();
 			try {
 				this.periodical_thread.join(1000);
 			} catch (Exception e) {
 				log.error("Error while stopping periodical thread");
 			}
 		}
 		if (this.queue != null) {
 			this.queue.stop();
 		}
 		if (extensions.size() > 0) {
 			for (IndexExtension extension : getExtensions().values()) {
 				extension.stop();
 			}
 		}
 
 		finalize();
 	}
 
 	protected abstract void finalize();
 
 	/**
 	 * Get the extensions associated with this IndexLocation. Every
 	 * {@link IndexExtension} is mapped to its name in the config-file
 	 * 
 	 * @return a map of {@link IndexExtension}
 	 */
 	public HashMap<String, IndexExtension> getExtensions() {
 		return extensions;
 	}
 
 	/**
 	 * is called in the constructor - creates instances of all IndexExtension as
 	 * configured in the config and stores them
 	 * 
 	 * @param config
 	 *            the config for the IndexLocation
 	 */
 	private void registerExtensions(CRConfig config) {
 		HashMap<String, IndexExtension> extensionMap = new HashMap<String, IndexExtension>();
 		GenericConfiguration extensionConfiguration = (GenericConfiguration) config.get(INDEX_EXTENSIONS_KEY);
 		if (extensionConfiguration != null) {
 			Hashtable<String, GenericConfiguration> configs = extensionConfiguration.getSubConfigs();
 
 			for (Entry<String, GenericConfiguration> e : configs.entrySet()) {
 				String indexExtensionName = e.getKey();
 				IndexExtension instance = null;
 				CRConfig extensionConfig = new CRConfigUtil(e.getValue(), INDEX_EXTENSIONS_KEY + "."
 						+ indexExtensionName);
 				try {
 					Class<?> extensionClassGeneric = Class
 							.forName(extensionConfig.getString(INDEX_EXTENSION_CLASS_KEY));
 					Class<? extends IndexExtension> extensionClass = extensionClassGeneric
 							.asSubclass(IndexExtension.class);
 					Constructor<? extends IndexExtension> extensionConstructor = extensionClass
 							.getDeclaredConstructor(new Class[] { CRConfig.class, IndexLocation.class });
 					instance = extensionConstructor.newInstance(new Object[] { extensionConfig, this });
 				} catch (Exception ex) {
 					log.error("Could not create instance of IndexExtension for " + indexExtensionName);
 				}
 
 				if (instance == null) {
 					log.error("Cannot get index location for " + indexExtensionName);
 				} else {
 					extensionMap.put(indexExtensionName, instance);
 				}
 			}
 		} else {
 			log.debug("THERE ARE NO EXTENSIONS CONFIGURED FOR THIS LOCATION.");
 		}
 		this.extensions = extensionMap;
 
 	}
 
 }
