 package grisu.settings;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.TreeMap;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.HierarchicalINIConfiguration;
 import org.apache.commons.configuration.SubnodeConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Manages the $HOME/.grisu/grisu-backend.config file.
  * 
  * @author Markus Binsteiner
  * 
  */
 public final class ServerPropertiesManager {
 
 	/**
 	 * Default myproxy lifetime: 3600 seconds.
 	 */
 	public static final int DEFAULT_MYPROXY_LIFETIME_IN_SECONDS = 3600;
 
 	/**
 	 * Default minimum myproxy lifetime before it gets refreshed: 600 seconds.
 	 */
 	public static final int DEFAULT_MIN_PROXY_LIFETIME_BEFORE_REFRESH = 600;
 	/**
 	 * Default concurrent threads to query job status per user: 2
 	 */
 	public static final int DEFAULT_CONCURRENT_JOB_STATUS_THREADS_PER_USER = 2;
 	/**
 	 * Default concurrent threads when submitting the parts of a multipartjob: 2
 	 */
 	public static final int DEFAULT_CONCURRENT_JOB_SUBMISSION_THREADS_PER_USER = 2;
 	public static final int DEFAULT_CONCURRENT_FILE_TRANSFER_THREADS_PER_USER = 10;
 
 	/**
 	 * Default concurrent threads when building mountpoint cache.
 	 */
 	public static final int DEFAULT_CONCURRENT_MOUNTPOINT_LOOKUPS = 8;
 
 	/**
 	 * Default concurrent threads when getting job properties of archived jobs.
 	 */
 	public static final int DEFAULT_CONCURRENT_ARCHIVED_JOB_LOOKUPS_PER_FILESYSTEM = 8;
 
 	/**
 	 * Default directory name used as parent for the jobdirectories.
 	 */
 	public static final String DEFAULT_JOB_DIR_NAME = "active-jobs";
 	public static final String DEFAULT_ARCHIVED_JOB_DIR_NAME = "archived-jobs";
 	public static final String DEFAULT_FQAN_TO_USE_FOR_ARCHIVING_JOBS = "/nz/nesi";
 	public static final int DEFAULT_TIME_INBETWEEN_STATUS_CHECKS_FOR_THE_SAME_JOB_IN_SECONDS = 60;
 
 	private static HierarchicalINIConfiguration config = null;
 
 	// public static final String DEFAULT_MULTIPARTJOB_DIR_NAME =
 	// "grisu-multijob-dir";
 
 	static final Logger myLogger = Logger
 	.getLogger(ServerPropertiesManager.class.getName());
 
 	private static final int DEFAULT_CONCURRENT_JOB_SUBMISSION_RETRIES = 5;
 
 	private static final boolean DEFAULT_CHECK_CONNECTION_TO_MOUNTPOINTS = false;
 
 	private static final int DEFAULT_FILE_TRANSFER_RETRIES = 3;
 	private static final int DEFAULT_TIME_BETWEEN_FILE_TRANSFER_RETRIES_IN_SECONDS = 1;
 
 	private static final Integer DEFAULT_FILESYSTEM_TIMEOUT_IN_MILLISECONDS = 4000;
 
 	private static final int DEFAULT_FILE_LISTING_TIMEOUT_IN_SECONDS = 20;
 
 	// public static boolean getCheckConnectionToMountPoint() {
 	//
 	// boolean check = false;
 	//
 	// try {
 	// try {
 	// check = getServerConfiguration().getBoolean(
 	// "checkConnectionToMountPoints");
 	// } catch (NoSuchElementException e) {
 	// // doesn't matter
 	// myLogger.debug(e);
 	// return DEFAULT_CHECK_CONNECTION_TO_MOUNTPOINTS;
 	// }
 	//
 	// } catch (ConfigurationException e) {
 	// return DEFAULT_CHECK_CONNECTION_TO_MOUNTPOINTS;
 	// }
 	// return check;
 	// }
 
 	/**
 	 * Returns the name of the directory in which grisu jobs are located
 	 * remotely.
 	 * 
 	 * @return the name of the direcotory in which grisu stores jobs or null if
 	 *         the jobs should be stored in the root home directory.
 	 */
 	public static String getArchivedJobsDirectoryName() {
 
 		String jobDirName = null;
 		try {
 			jobDirName = getServerConfiguration().getString(
 			"General.archivedJobDirName");
 
 			if (StringUtils.isNotBlank(jobDirName)
 					&& "none".equals(jobDirName.toLowerCase())) {
 				jobDirName = null;
 			}
 
 		} catch (final Exception e) {
 			jobDirName = null;
 		}
 
 		if (jobDirName == null) {
 			jobDirName = DEFAULT_ARCHIVED_JOB_DIR_NAME;
 		}
 
 		return jobDirName;
 	}
 
 	public static int getConcurrentArchivedJobLookupsPerFilesystem() {
 
 		int concurrentThreads = -1;
 		try {
 			concurrentThreads = Integer
 			.parseInt(getServerConfiguration().getString(
 			"ConcurrentThreadSettings.archivedJobsLookupThreadsPerFilesystem"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_ARCHIVED_JOB_LOOKUPS_PER_FILESYSTEM;
 		}
 		if (concurrentThreads == -1) {
 			return DEFAULT_CONCURRENT_ARCHIVED_JOB_LOOKUPS_PER_FILESYSTEM;
 		}
 		return concurrentThreads;
 
 	}
 
 	public static int getConcurrentFileTransfersPerUser() {
 
 		int retries = -1;
 		try {
 			retries = Integer.parseInt(getServerConfiguration().getString(
 			"ConcurrentThreadSettings.fileTransfersPerUser"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_FILE_TRANSFER_THREADS_PER_USER;
 		}
 		if (retries == -1) {
 			return DEFAULT_CONCURRENT_FILE_TRANSFER_THREADS_PER_USER;
 		}
 		return retries;
 
 	}
 
 	/**
 	 * Returns the number of concurrent threads that are querying job status per
 	 * user.
 	 * 
 	 * @return the number of concurrent threads
 	 */
 	public static int getConcurrentJobStatusThreadsPerUser() {
 		int concurrentThreads = -1;
 		try {
 			concurrentThreads = Integer.parseInt(getServerConfiguration()
 					.getString("ConcurrentThreadSettings.jobStatusThreads"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_JOB_STATUS_THREADS_PER_USER;
 		}
 		if (concurrentThreads == -1) {
 			return DEFAULT_CONCURRENT_JOB_STATUS_THREADS_PER_USER;
 		}
 		return concurrentThreads;
 	}
 
 	/**
 	 * Returns the number of concurrent threads that are used to build the
 	 * mountpoint cache when a user first logs in.
 	 * 
 	 * @return the number of concurrent threads
 	 */
 	public static int getConcurrentMountPointLookups() {
 		int concurrentThreads = -1;
 		try {
 			concurrentThreads = Integer
 			.parseInt(getServerConfiguration().getString(
 			"ConcurrentThreadSettings.mountPointLookupThreads"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_MOUNTPOINT_LOOKUPS;
 		}
 		if (concurrentThreads == -1) {
 			return DEFAULT_CONCURRENT_MOUNTPOINT_LOOKUPS;
 		}
 		return concurrentThreads;
 	}
 
 	// /**
 	// * Returns the name of the directory in which grisu jobs are located
 	// * remotely.
 	// *
 	// * @return the name of the direcotory in which grisu stores jobs or null
 	// if
 	// * the jobs should be stored in the root home directory.
 	// */
 	// public static String getGrisuMultiPartJobDirectoryName() {
 	//
 	// String jobDirName = null;
 	// try {
 	// jobDirName = getServerConfiguration().getString("multiPartJobDirName");
 	//
 	// if ("none".equals(jobDirName.toLowerCase())) {
 	// jobDirName = null;
 	// }
 	//
 	// } catch (Exception e) {
 	// jobDirName = null;
 	// }
 	//
 	// if (jobDirName == null) {
 	// jobDirName = DEFAULT_MULTIPARTJOB_DIR_NAME;
 	// }
 	//
 	// return jobDirName;
 	// }
 
 	/**
 	 * Returns the number of concurrent threads that are submitting (multi-)jobs
 	 * status per user.
 	 * 
 	 * @return the number of concurrent threads
 	 */
 	public static int getConcurrentMultiPartJobSubmitThreadsPerUser() {
 		int concurrentThreads = -1;
 		try {
 			concurrentThreads = Integer
 			.parseInt(getServerConfiguration().getString(
 			"ConcurrentThreadSettings.batchJobSubmitThreads"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_JOB_SUBMISSION_THREADS_PER_USER;
 		}
 		if (concurrentThreads == -1) {
 			return DEFAULT_CONCURRENT_JOB_SUBMISSION_THREADS_PER_USER;
 		}
 		return concurrentThreads;
 	}
 
 	/**
 	 * The url to connect to the database.
 	 * 
 	 * @return the url
 	 */
 	public static String getDatabaseConnectionUrl() {
 		String dbUrl;
 		try {
 			dbUrl = getServerConfiguration().getString(
 			"Database.databaseConnectionUrl");
 			return dbUrl;
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * The database password.
 	 * 
 	 * @return the password
 	 */
 	public static String getDatabasePassword() {
 		String dbPassword;
 		try {
 			dbPassword = getServerConfiguration().getString(
 			"Database.databasePassword");
 			return dbPassword;
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the type of database that should be used. At the moment hsqldb
 	 * and mysql are supported.
 	 * 
 	 * @return the db type
 	 */
 	public static String getDatabaseType() {
 
 		String dbType;
 		try {
 			dbType = getServerConfiguration()
 			.getString("Database.databaseType");
 			return dbType;
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the database username.
 	 * 
 	 * @return the db username
 	 */
 	public static String getDatabaseUsername() {
 		String dbUsername;
 		try {
 			dbUsername = getServerConfiguration().getString(
 			"Database.databaseUsername");
 			return dbUsername;
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the path to the directory where the debug information for this
 	 * backend is stored.
 	 * 
 	 * @return the path to the debug directory.
 	 */
 	public static String getDebugDirectory() {
 
 		final File dir = new File(Environment.getGrisuDirectory(), "debug");
 
 		return dir.getAbsolutePath();
 	}
 
 	/**
 	 * Checks whether the debug mode is enabled or not.
 	 * 
 	 * @return true if debug is enabled, false if not
 	 */
 	public static boolean getDebugModeOn() {
 		boolean debug = false;
 
 		try {
 			try {
 				debug = getServerConfiguration().getBoolean("Debug.enabled");
 			} catch (final NoSuchElementException e) {
 				// doesn't matter
 				myLogger.debug(e);
 			}
 			if (debug) {
 				// try to create debug directory
 				final File debugDir = new File(getDebugDirectory());
 				if (!debugDir.exists()) {
 					debugDir.mkdir();
 				}
 
 				if (!debugDir.exists()) {
 					myLogger.error("Can't create debug directory. Turning debug mode off.");
 					debug = false;
 				}
 			}
 		} catch (final ConfigurationException e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			myLogger.debug(e);
 		}
 		return debug;
 	}
 
 	public static String getDefaultFqanForArchivedJobDirectory() {
 		String fqan = null;
 		try {
 			fqan = getServerConfiguration().getString(
			"General.archivedJobDefaultVO");
 
 			if (StringUtils.isNotBlank(fqan)
 					&& "none".equals(fqan.toLowerCase())) {
 				fqan = null;
 			}
 
 		} catch (final Exception e) {
 			fqan = null;
 		}
 
 		if (fqan == null) {
 			fqan = DEFAULT_FQAN_TO_USE_FOR_ARCHIVING_JOBS;
 		}
 
 		return fqan;
 	}
 
 	public static int getFileListingTimeOut() {
 
 		int waitTimeInSeconds = -1;
 		try {
 			waitTimeInSeconds = Integer.parseInt(getServerConfiguration()
 					.getString("General.fileListingTimeOut"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_FILE_LISTING_TIMEOUT_IN_SECONDS;
 		}
 		if (waitTimeInSeconds == -1) {
 			return DEFAULT_FILE_LISTING_TIMEOUT_IN_SECONDS;
 		}
 		return waitTimeInSeconds;
 	}
 
 	public static Integer getFileSystemConnectTimeout() {
 		int timeoutInMilliseconds = -1;
 		try {
 			timeoutInMilliseconds = Integer.parseInt(getServerConfiguration()
 					.getString("General.filesystemTimeout"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_FILESYSTEM_TIMEOUT_IN_MILLISECONDS;
 		}
 		if (timeoutInMilliseconds == -1) {
 			return DEFAULT_FILESYSTEM_TIMEOUT_IN_MILLISECONDS;
 		}
 		return timeoutInMilliseconds;
 	}
 
 	public static int getFileTransferRetries() {
 		// TODO Auto-generated method stub
 		int retries = -1;
 		try {
 			retries = Integer.parseInt(getServerConfiguration().getString(
 			"RetrySettings.fileTransfers"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_FILE_TRANSFER_RETRIES;
 		}
 		if (retries == -1) {
 			return DEFAULT_FILE_TRANSFER_RETRIES;
 		}
 		return retries;
 
 	}
 
 	public static Map<String, String> getInformationManagerConf() {
 
 		SubnodeConfiguration conf;
 		try {
 			conf = getServerConfiguration().getSection("InformationManager");
 		} catch (final ConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		final Map<String, String> result = new TreeMap<String, String>();
 		final Iterator it = conf.getKeys();
 		while (it.hasNext()) {
 			final Object key = it.next();
 			final String value = conf.getString(key.toString());
 			result.put(key.toString(), value);
 		}
 
 		if (result.size() == 0) {
 			return null;
 		}
 
 		return result;
 	}
 
 	public static int getJobSubmissionRetries() {
 
 		int retries = -1;
 		try {
 			retries = Integer.parseInt(getServerConfiguration().getString(
 			"RetrySettings.jobSubmissions"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_CONCURRENT_JOB_SUBMISSION_RETRIES;
 		}
 		if (retries == -1) {
 			return DEFAULT_CONCURRENT_JOB_SUBMISSION_RETRIES;
 		}
 		return retries;
 	}
 
 	public static Map<String, String> getMatchMakerConf() {
 		SubnodeConfiguration conf;
 		try {
 			conf = getServerConfiguration().getSection("MatchMaker");
 		} catch (final ConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		final Map<String, String> result = new TreeMap<String, String>();
 		final Iterator it = conf.getKeys();
 		while (it.hasNext()) {
 			final Object key = it.next();
 			final String value = conf.getString(key.toString());
 			result.put(key.toString(), value);
 		}
 
 		if (result.size() == 0) {
 			return null;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns the minimum lifetime of a proxy before the backend tries to
 	 * retrieve a fresh one.
 	 * 
 	 * @return the minimum lifetime in seconds
 	 */
 	public static int getMinProxyLifetimeBeforeGettingNewProxy() {
 		int lifetime_in_seconds = -1;
 		try {
 			lifetime_in_seconds = Integer.parseInt(getServerConfiguration()
 					.getString("General.minProxyLifetime"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_MIN_PROXY_LIFETIME_BEFORE_REFRESH;
 		}
 		if (lifetime_in_seconds == -1) {
 			return DEFAULT_MIN_PROXY_LIFETIME_BEFORE_REFRESH;
 		}
 		return lifetime_in_seconds;
 	}
 
 	/**
 	 * Returns the lifetime of a delegated proxy that is retrieved from myproxy.
 	 * 
 	 * @return the lifetime in seconds
 	 */
 	public static int getMyProxyLifetime() {
 		int lifetime_in_seconds = -1;
 		try {
 			lifetime_in_seconds = Integer.parseInt(getServerConfiguration()
 					.getString("General.proxyLifetime"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_MYPROXY_LIFETIME_IN_SECONDS;
 		}
 		if (lifetime_in_seconds == -1) {
 			return DEFAULT_MYPROXY_LIFETIME_IN_SECONDS;
 		}
 		return lifetime_in_seconds;
 	}
 
 	/**
 	 * Returns the name of the directory in which grisu jobs are located
 	 * remotely.
 	 * 
 	 * @return the name of the direcotory in which grisu stores jobs or null if
 	 *         the jobs should be stored in the root home directory.
 	 */
 	public static String getRunningJobsDirectoryName() {
 
 		String jobDirName = null;
 		try {
 			jobDirName = getServerConfiguration().getString(
 			"General.jobDirName");
 
 			if (StringUtils.isNotBlank(jobDirName)
 					&& "none".equals(jobDirName.toLowerCase())) {
 				jobDirName = null;
 			}
 
 		} catch (final Exception e) {
 			jobDirName = null;
 		}
 
 		if (jobDirName == null) {
 			jobDirName = DEFAULT_JOB_DIR_NAME;
 		}
 
 		return jobDirName;
 	}
 
 	/**
 	 * Retrieves the configuration parameters from the properties file.
 	 * 
 	 * @return the configuration
 	 * @throws ConfigurationException
 	 *             if the file could not be read/parsed
 	 */
 	public static HierarchicalINIConfiguration getServerConfiguration()
 	throws ConfigurationException {
 		if (config == null) {
 			final File grisuDir = Environment.getGrisuDirectory();
 			config = new HierarchicalINIConfiguration(new File(grisuDir,
 			"grisu-backend.config"));
 		}
 		return config;
 	}
 
 	public static int getWaitTimeBetweenFailedFileTransferAndNextTryInSeconds() {
 
 		int waitTimeInSeconds = -1;
 		try {
 			waitTimeInSeconds = Integer.parseInt(getServerConfiguration()
 					.getString("RetrySettings.fileTransferWaitTime"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_TIME_BETWEEN_FILE_TRANSFER_RETRIES_IN_SECONDS;
 		}
 		if (waitTimeInSeconds == -1) {
 			return DEFAULT_TIME_BETWEEN_FILE_TRANSFER_RETRIES_IN_SECONDS;
 		}
 		return waitTimeInSeconds;
 
 	}
 
 	/**
 	 * Returns the (forced) wait time in seconds inbetween status checks for the
 	 * same job.
 	 * 
 	 * @return wait time in seconds
 	 */
 	public static int getWaitTimeBetweenJobStatusChecks() {
 		int waitTimeInSeconds = -1;
 		try {
 			waitTimeInSeconds = Integer.parseInt(getServerConfiguration()
 					.getString("General.statusCheckWaitTime"));
 
 		} catch (final Exception e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			return DEFAULT_TIME_INBETWEEN_STATUS_CHECKS_FOR_THE_SAME_JOB_IN_SECONDS;
 		}
 		if (waitTimeInSeconds == -1) {
 			return DEFAULT_TIME_INBETWEEN_STATUS_CHECKS_FOR_THE_SAME_JOB_IN_SECONDS;
 		}
 		return waitTimeInSeconds;
 	}
 
 	/**
 	 * Checks whether the default (hsqldb) database configuration should be
 	 * used.
 	 * 
 	 * @return true if default, false if not.
 	 */
 	public static boolean useDefaultDatabase() {
 
 		try {
 			final String dbtype = getServerConfiguration().getString(
 			"Database.databaseType");
 
 			if ((dbtype == null) || (dbtype.length() == 0)) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (final Exception e) {
 			e.printStackTrace();
 			return true;
 		}
 
 	}
 
 	/**
 	 * Checks whether the debug mode is enabled or not.
 	 * 
 	 * @return true if debug is enabled, false if not
 	 */
 	public static boolean useFileSystemCache() {
		boolean useFScache = true;
 
 		try {
 			try {
 				useFScache = getServerConfiguration().getBoolean("General.fsCache");
 			} catch (final NoSuchElementException e) {
 				// doesn't matter
 				myLogger.debug(e);
 			}
 
 		} catch (final ConfigurationException e) {
 			// myLogger.error("Problem with config file: " + e.getMessage());
 			myLogger.debug(e);
 		}
 		return useFScache;
 	}
 
 }
