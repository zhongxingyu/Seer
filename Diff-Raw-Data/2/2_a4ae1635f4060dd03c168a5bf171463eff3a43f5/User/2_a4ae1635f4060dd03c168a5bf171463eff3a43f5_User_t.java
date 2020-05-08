 package grisu.backend.model;
 
 import grisu.backend.hibernate.BatchJobDAO;
 import grisu.backend.hibernate.JobDAO;
 import grisu.backend.hibernate.UserDAO;
 import grisu.backend.model.fs.FileSystemManager;
 import grisu.backend.model.fs.GrisuInputStream;
 import grisu.backend.model.job.BatchJob;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.JobSubmissionManager;
 import grisu.backend.model.job.JobSubmitter;
 import grisu.backend.model.job.gt4.GT4DummySubmitter;
 import grisu.backend.model.job.gt4.GT4Submitter;
 import grisu.backend.model.job.gt5.GT5Submitter;
 import grisu.backend.utils.CertHelpers;
 import grisu.control.JobConstants;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.NoValidCredentialException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.control.serviceInterfaces.AbstractServiceInterface;
 import grisu.jcommons.constants.Constants;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.GridFile;
 import grisu.model.job.JobSubmissionObjectImpl;
 import grisu.settings.ServerPropertiesManager;
 import grisu.utils.FqanHelpers;
 import grisu.utils.MountPointHelpers;
 import grith.jgrith.voms.VO;
 import grith.jgrith.voms.VOManagement.VOManagement;
 import grith.jgrith.vomsProxy.VomsException;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.log4j.Logger;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 
 /**
  * The User class holds all the relevant data a job could want to know from the
  * user it is running under. This class belongs to the three central classes of
  * grisu (the other two are {@link ServiceInterface} and {@link Job}.
  * 
  * At the moment it holds filesystem information which can be used to stage
  * files from the desktop. Also it has got information about vo memberships of
  * the user.
  * 
  * @author Markus Binsteiner
  * 
  */
 @Entity
 @Table(name = "users")
 
 public class User {
 
 	private final static boolean ENABLE_FILESYSTEM_CACHE = ServerPropertiesManager
 	.useFileSystemCache();
 
 	protected static UserDAO userdao = new UserDAO();
 	protected static final JobDAO jobdao = new JobDAO();
 	protected final BatchJobDAO batchJobDao = new BatchJobDAO();
 
 	private static Logger myLogger = Logger.getLogger(User.class.getName());
 
 	public static User createUser(ProxyCredential cred,
 			AbstractServiceInterface si) {
 
 		// make sure there is a valid credential
 		if ((cred == null) || !cred.isValid()) {
 			throw new NoValidCredentialException(
 			"No valid credential exists in this session");
 		}
 
 		// myLogger.debug("CREATING USER SESSION: " + cred.getDn());
 
 		// if ( getCredential())
 
 		Date time1 = new Date();
 
 		User user;
 		// try to look up user in the database
 		user = userdao.findUserByDN(cred.getDn());
 		Date time2 = new Date();
 
 		myLogger.debug("Login benchmark - db lookup: "
 				+ new Long((time2.getTime() - time1.getTime()))
 				.toString()
 				+ " ms");
 
 		if (user == null) {
 			user = new User(cred);
 			time1 = new Date();
 			myLogger.debug("Login benchmark - constructor: "
 					+ new Long((time1.getTime() - time2.getTime()))
 					.toString() + " ms");
 
 			userdao.saveOrUpdate(user);
 		} else {
 			user.setCred(cred);
 			time1 = new Date();
 			myLogger.debug("Login benchmark - setting credential: "
 					+ new Long((time1.getTime() - time2.getTime()))
 					.toString() + " ms");
 
 		}
 
 		try {
 			user.setAutoMountedMountPoints(user.df_auto_mds(si.getAllSites()
 					.asArray()));
 			time2 = new Date();
 			myLogger.debug("Login benchmark - mountpoints: "
 					+ new Long((time2.getTime() - time1.getTime()))
 					.toString() + " ms");
 		} catch (Exception e) {
 			throw new RuntimeException(
 					"Can't aquire filesystems for user. Possibly because of misconfigured grisu backend",
 					e);
 		}
 
 		// final User temp = user;
 
 		// caching users archived jobs since those take a while to load...
 		// new Thread() {
 		// @Override
 		// public void run() {
 		// // temp.getDefaultArchiveLocation();
 		// // temp.getArchivedJobs(null);
 		// }
 		// }.start();
 
 		return user;
 
 	}
 
 	public static String get_vo_dn_path(final String dn) {
 		return dn.replace("=", "_").replace(",", "_").replace(" ", "_");
 	}
 
 	private Long id = null;
 
 	// the (default) credential to contact gridftp file shares
 	private ProxyCredential cred = null;
 
 	private JobSubmissionManager manager;
 
 	// this needs to be static because otherwise the session be lost and the
 	// action status can't be found anymore by the client
 	private static final Map<String, Map<String, DtoActionStatus>> actionStatuses = new HashMap<String, Map<String, DtoActionStatus>>();
 
 	private static final String NOT_ACCESSIBLE = "Not accessible";
 	private static final String ACCESSIBLE = "Accessible";
 
 	// the (default) credentials dn
 	private String dn = null;
 
 	// the mountpoints of a user
 	private Set<MountPoint> mountPoints = new HashSet<MountPoint>();
 
 	private Map<String, String> mountPointCache = Collections
 	.synchronizedMap(new HashMap<String, String>());
 	private final Map<String, Set<MountPoint>> mountPointsPerFqanCache = new TreeMap<String, Set<MountPoint>>();
 
 	private Set<MountPoint> mountPointsAutoMounted = new HashSet<MountPoint>();
 
 	private Set<MountPoint> allMountPoints = null;
 
 	// credentials are chache so we don't have to contact myproxy/voms anytime
 	// we want to make a transaction
 	private Map<String, ProxyCredential> cachedCredentials = new HashMap<String, ProxyCredential>();
 	// All fqans of the user
 	private Map<String, String> fqans = null;
 	private Set<String> cachedUniqueGroupnames = null;
 
 	private Map<String, String> userProperties = new HashMap<String, String>();
 
 	private Map<String, String> bookmarks = new HashMap<String, String>();
 	private Map<String, String> archiveLocations = null;
 	private Map<String, JobSubmissionObjectImpl> jobTemplates = new HashMap<String, JobSubmissionObjectImpl>();
 
 	private FileSystemManager fsm;
 
 	// for hibernate
 	public User() {
 	}
 
 	/**
 	 * Constructs a user using and associates a (default) credential with it.
 	 * 
 	 * @param cred
 	 *            the credential
 	 * @throws FileSystemException
 	 *             if the users default filesystems can't be mounted
 	 */
 	private User(final ProxyCredential cred) {
 		this.dn = cred.getDn();
 		this.cred = cred;
 	}
 
 	/**
 	 * Constructs a User object not using an associated credential.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 */
 	public User(final String dn) {
 		this.dn = dn;
 	}
 
 
 	public void addArchiveLocation(String alias, String value) {
 
 		getArchiveLocations().put(alias, value);
 		userdao.saveOrUpdate(this);
 
 	}
 
 	public void addBookmark(String alias, String url) {
 		this.bookmarks.put(alias, url);
 		userdao.saveOrUpdate(this);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void addFqan(final String fqan, final String vo) {
 		fqans.put(fqan, vo);
 	}
 
 	public synchronized void addLogMessageToPossibleMultiPartJobParent(
 			Job job, String message) {
 
 		final String mpjName = job.getJobProperty(Constants.BATCHJOB_NAME);
 
 		if (mpjName != null) {
 			BatchJob mpj = null;
 			try {
 				mpj = getBatchJobFromDatabase(mpjName);
 			} catch (final NoSuchJobException e) {
 				myLogger.error(e);
 				return;
 			}
 			mpj.addLogMessage(message);
 			batchJobDao.saveOrUpdate(mpj);
 		}
 	}
 
 
 	public void addProperty(String key, String value) {
 
 		getUserProperties().put(key, value);
 		userdao.saveOrUpdate(this);
 
 	}
 
 	public void cleanCache() {
 		// TODO disconnect filesystems somehow?
 		// cachedFilesystemConnections = new HashMap<MountPoint, FileSystem>();
 		// // does this affect existing filesystem connection
 		// for ( ProxyCredential proxy : cachedCredentials.values() ) {
 		// proxy.destroy();
 		// }
 		cachedCredentials = new HashMap<String, ProxyCredential>();
 	}
 
 
 	public void clearMountPointCache(String keypattern) {
 		if (StringUtils.isBlank(keypattern)) {
 			this.mountPointCache = Collections
 			.synchronizedMap(new HashMap<String, String>());
 		}
 		userdao.saveOrUpdate(this);
 	}
 
 	private MountPoint createMountPoint(String server, String path,
 			final String fqan, Executor executor) throws Exception {
 
 		String url = null;
 
 
 		final int startProperties = path.indexOf("[");
 		final int endProperties = path.indexOf("]");
 
 		if ((startProperties >= 0) && (endProperties < 0)) {
 			myLogger.error("Path: " + path + " for host " + server
 					+ " has incorrect syntax. Ignoring...");
 			return null;
 		}
 
 		String alias = null;
 
 		String propString = null;
 		try {
 			propString = path.substring(startProperties + 1, endProperties);
 		} catch (final Exception e) {
 			// that's ok
 			// myLogger.debug("No extra properties for path: " + path);
 		}
 
 		final Map<String, String> properties = new HashMap<String, String>();
 		boolean userDnPath = true;
 		if (StringUtils.isNotBlank(propString)) {
 
 			final String[] parts = propString.split(";");
 			for (final String part : parts) {
 				if (part.indexOf("=") <= 0) {
 					// myLogger.error("Invalid path spec: " + path
 					// + ".  No \"=\" found. Ignoring this mountpoint...");
 					throw new Exception("Invalid path spec: " + path
 							+ ".  No \"=\" found. Ignoring this mountpoint...");
 				}
 				final String key = part.substring(0, part.indexOf("="));
 				if (StringUtils.isBlank(key)) {
 					//					myLogger.error("Invalid path spec: " + path
 					//							+ ".  No key found. Ignoring this mountpoint...");
 					throw new Exception("Invalid path spec: " + path
 							+ ".  No key found. Ignoring this mountpoint...");
 				}
 				String value = null;
 				try {
 					value = part.substring(part.indexOf("=") + 1);
 					if (StringUtils.isBlank(value)) {
 						// myLogger.error("Invalid path spec: "
 						// + path
 						// + ".  No key found. Ignoring this mountpoint...");
 						throw new Exception("Invalid path spec: "
 								+ path
 								+ ".  No key found. Ignoring this mountpoint...");
 					}
 				} catch (final Exception e) {
 					// myLogger.error("Invalid path spec: " + path
 					// + ".  No key found. Ignoring this mountpoint...");
 					throw new Exception("Invalid path spec: " + path
 							+ ".  No key found. Ignoring this mountpoint...");
 				}
 
 				properties.put(key, value);
 
 			}
 			alias = properties.get(MountPoint.ALIAS_KEY);
 
 			try {
 				userDnPath = Boolean.parseBoolean(properties
 						.get(MountPoint.USER_SUBDIR_KEY));
 			} catch (final Exception e) {
 				// that's ok
 				myLogger.debug("Could not find or parse"
 						+ MountPoint.USER_SUBDIR_KEY
 						+ " key. Using user subdirs..");
 				userDnPath = true;
 			}
 
 		}
 
 		String tempPath = null;
 		if (startProperties < 0) {
 			tempPath = path.substring(0, path.length());
 		} else {
 			tempPath = path.substring(0, startProperties);
 		}
 
 		properties.put(MountPoint.PATH_KEY, tempPath);
 
 		if (tempPath.startsWith(".")) {
 			myLogger.warn("Using '.' is deprecated. Please use /~/ instead for: "
 					+ server + " / " + fqan);
 			try {
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan);
 
 				String additionalUrl = null;
 				try {
 					additionalUrl = tempPath
 					.substring(1, tempPath.length() - 1);
 				} catch (final Exception e) {
 					additionalUrl = "";
 				}
 
 				url = url + additionalUrl;
 
 			} catch (final Exception e) {
 				// myLogger.error(e);
 				throw e;
 			}
 
 		} else if (tempPath.startsWith("/~/")) {
 			try {
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan);
 
 				String additionalUrl = null;
 				try {
 					additionalUrl = tempPath
 					.substring(1, tempPath.length() - 3);
 				} catch (final Exception e) {
 					additionalUrl = "";
 				}
 
 				url = url + additionalUrl;
 
 			} catch (final Exception e) {
 				// myLogger.error(e);
 				throw e;
 			}
 		} else if (path.contains("${GLOBUS_USER_HOME}")) {
 			try {
 				myLogger.warn("Using ${GLOBUS_USER_HOME} is deprecated. Please use /~/ instead for: "
 						+ server + " / " + fqan);
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan);
 				userDnPath = false;
 			} catch (final Exception e) {
 				// myLogger.error(e);
 				throw e;
 			}
 
 		} else if (path.contains("${GLOBUS_SCRATCH_DIR")) {
 			try {
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan) + "/.globus/scratch";
 				userDnPath = false;
 			} catch (final Exception e) {
 				// myLogger.error(e);
 				throw e;
 			}
 		} else {
 
 			// url = server.replace(":2811", "") + path + "/"
 			// + User.get_vo_dn_path(getCred().getDn());
 			url = server.replace(":2811", "") + tempPath;
 
 		}
 
 		if (StringUtils.isBlank(url)) {
 			// myLogger.error("Url is blank for " + server + " and " + path);
 			throw new Exception("Url is blank for " + server + " and " + path);
 		}
 
 		// add dn dir if necessary
 
 		if (userDnPath) {
 			url = url + "/" + User.get_vo_dn_path(getCred().getDn());
 
 			// try to connect to filesystem in background and store in database
 			// if not successful, so next time won't be tried again...
 			if (executor != null) {
 				final String urlTemp = url;
 				final Thread t = new Thread() {
 					@Override
 					public void run() {
 						final String key = urlTemp + fqan;
 						try {
 							// try to create the dir if it doesn't exist
 
 							// checking whether subfolder exists
 							if (StringUtils.isNotBlank(getMountPointCache()
 									.get(key)) && !NOT_ACCESSIBLE.equals(key)) {
 								// exists apparently, don't need to create
 								// folder...
 								return;
 							}
 
 							if (NOT_ACCESSIBLE.equals(key)) {
 								myLogger.debug(getDn()
 										+ ": FS cache indicates that url "
 										+ urlTemp
 										+ " / "
 										+ fqan
 										+ "is not accessible. Clear FS cache if you think that has changed.");
 								return;
 							}
 							// myLogger.debug("Did not find "
 							// + urlTemp
 							// + "in cache, trying to access/create folder...");
 							final boolean exists = getFileSystemManager()
 							.fileExists(urlTemp);
 							if (!exists) {
 								myLogger.debug("Mountpoint does not exist. Trying to create non-exitent folder: "
 										+ urlTemp);
 								getFileSystemManager().createFolder(urlTemp);
 								// } else {
 								// myLogger.debug("MountPoint " + urlTemp
 								// + " exists.");
 							}
 
 							getMountPointCache().put(key, ACCESSIBLE);
 
 						} catch (final Exception e) {
 							myLogger.error("Could not create folder: "
 									+ urlTemp, e);
 
 							if (ENABLE_FILESYSTEM_CACHE) {
 								getMountPointCache().put(key, NOT_ACCESSIBLE);
 							}
 
 						} finally {
 							if (ENABLE_FILESYSTEM_CACHE) {
 								try {
 									userdao.saveOrUpdate(User.this);
 								} catch (Exception e) {
 									myLogger.debug("Could not save filesystem state for fs "
 											+ urlTemp
 											+ ": "
 											+ e.getLocalizedMessage());
 								}
 							}
 						}
 					}
 				};
 				executor.execute(t);
 			}
 
 		}
 
 		final String site = AbstractServiceInterface.informationManager
 		.getSiteForHostOrUrl(url);
 
 
 		MountPoint mp = null;
 
 		if (StringUtils.isBlank(alias)) {
 			alias = MountPointHelpers.calculateMountPointName(server, fqan);
 		}
 		mp = new MountPoint(getDn(), fqan, url, alias, site, true);
 
 		for (final String key : properties.keySet()) {
 			mp.addProperty(key, properties.get(key));
 		}
 
 		final boolean isVolatile = AbstractServiceInterface.informationManager
 		.isVolatileDataLocation(server, tempPath, fqan);
 		mp.setVolatileFileSystem(isVolatile);
 
 		return mp;
 
 		// + "." + fqan + "." + path);
 		// + "." + fqan);
 		// cachedGridFtpHomeDirs.put(keyMP, mp);
 		//
 		// return cachedGridFtpHomeDirs.get(keyMP);
 	}
 
 	/**
 	 * Gets all mountpoints for this fqan.
 	 * 
 	 * @param fqan
 	 *            the fqan
 	 * @return the mountpoints
 	 */
 	public Set<MountPoint> df(String fqan) {
 
 		final Set<MountPoint> result = new HashSet<MountPoint>();
 		for (final MountPoint mp : getAllMountPoints()) {
 			if (StringUtils.isNotBlank(mp.getFqan())
 					&& mp.getFqan().equals(fqan)) {
 				result.add(mp);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Calculates all mountpoints that are automatically mounted using mds. At
 	 * the moment, the port part of the gridftp url share is ignored. Maybe I'll
 	 * change that later.
 	 * 
 	 * @param sites
 	 *            the sites that should be used
 	 * @return all MountPoints
 	 */
 	protected Set<MountPoint> df_auto_mds(final String[] sites) {
 
 		myLogger.debug("Getting mds mountpoints for user: " + getDn());
 
 		final Set<MountPoint> mps = Collections
 		.synchronizedSet(new TreeSet<MountPoint>());
 
 		// to check whether dn_subdirs are created already and create them if
 		// not (in background)
 		int df_p = ServerPropertiesManager.getConcurrentMountPointLookups();
 
 		final ExecutorService backgroundExecutorForFilesystemCache = Executors
 		.newFixedThreadPool(2);
 
 		final ExecutorService executor = Executors.newFixedThreadPool(df_p);
 
 		// for ( String site : sites ) {
 
 		// X.p("Start creating mountpoints...");
 
 		final Map<String, MountPoint> successfullMountPoints = Collections
 		.synchronizedMap(new HashMap<String, MountPoint>());
 		final Map<String, Exception> unsuccessfullMountPoints = Collections
 		.synchronizedMap(new HashMap<String, Exception>());
 
 
 		for (final String fqan : getFqans().keySet()) {
 
 			Thread t = new Thread() {
 				@Override
 				public void run() {
 
 					// final Date start = new Date();
 					final Map<String, String[]> mpUrl = AbstractServiceInterface.informationManager
 					.getDataLocationsForVO(fqan);
 					// final Date end = new Date();
 					// myLogger.debug("Querying for data locations for all sites and+ "
 					// + fqan + " took: " + (end.getTime() - start.getTime())
 					// + " ms.");
 					for (final String server : mpUrl.keySet()) {
 
 						String uniqueString = null;
 						for (final String path : mpUrl.get(server)) {
 							try {
 
 								uniqueString = server + " - " + path
 								+ " - " + fqan;
 
 								// X.p("\t" + uniqueString
 								// + ": creating....");
 
 								successfullMountPoints.put(uniqueString, null);
 
 								final MountPoint mp = createMountPoint(server, path,
 										fqan,
 										(ENABLE_FILESYSTEM_CACHE) ? backgroundExecutorForFilesystemCache
 												: null);
 
 								successfullMountPoints.put(server + "_" + path
 										+ "_" + fqan, mp);
 
 								// X.p("\t" + server + "/" + path + "/" + fqan
 								// + ": created");
 
 								if (mp != null) {
 									mps.add(mp);
 									successfullMountPoints
 									.put(uniqueString, mp);
 								} else {
 									successfullMountPoints.remove(uniqueString);
 									unsuccessfullMountPoints
 									.put(uniqueString,
 											new Exception(
 											"MountPoint not created, unknown reason."));
 								}
 							} catch (final Exception e) {
 								// X.p(server + "/" + "/" + fqan + ": failed : "
 								// + e.getLocalizedMessage());
 								// e.printStackTrace();
 								successfullMountPoints.remove(uniqueString);
 								unsuccessfullMountPoints.put(uniqueString, e);
 								myLogger.error("Can't use mountpoint " + server
 										+ ": " + e.getLocalizedMessage(), e);
 							}
 						}
 					}
 				}
 
 			};
 			executor.execute(t);
 		}
 
 		executor.shutdown();
 
 		// X.p("Waiting...");
 
 		try {
 			executor.awaitTermination(2, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		for (String us : successfullMountPoints.keySet()) {
 			if (successfullMountPoints.get(us) == null) {
 				unsuccessfullMountPoints.put(us, new Exception(
 				"MountPoint not created. Probably timed out."));
 			}
 		}
 		for (String us : unsuccessfullMountPoints.keySet()) {
 			successfullMountPoints.remove(us);
 
 			myLogger.error(getDn() + ": Can't connect to mountpoint " + us
 					+ ":\n\t"
 					+ unsuccessfullMountPoints.get(us).getLocalizedMessage());
 		}
 
 
 		// X.p("Finished creating mountpoints...");
 
 		backgroundExecutorForFilesystemCache.shutdown();
 
 		return mps;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object other) {
 		if (this == other) {
 			return true;
 		}
 		if (!(other instanceof User)) {
 			return false;
 		}
 
 		final User user = (User) other;
 
 		if (!dn.equals(user.dn)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public GridFile fillFolder(GridFile folder, int recursionLevel)
 	throws RemoteFileSystemException {
 
 		GridFile tempFolder = null;
 
 		try {
 			tempFolder = getFileSystemManager().getFolderListing(
 					folder.getUrl(), 1);
 		} catch (final Exception e) {
 			// myLogger.error(e);
 			myLogger.error(
 					"Error getting folder listing. I suspect this to be a bug in the commons-vfs-grid library. Sleeping for 1 seconds and then trying again...",
 					e);
 			try {
 				Thread.sleep(1000);
 			} catch (final InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			tempFolder = getFileSystemManager().getFolderListing(
 					folder.getUrl(), 1);
 
 		}
 		folder.setChildren(tempFolder.getChildren());
 
 		if (recursionLevel > 0) {
 			for (final GridFile childFolder : tempFolder.getChildren()) {
 				if (childFolder.isFolder()) {
 					folder.addChild(fillFolder(childFolder, recursionLevel - 1));
 				}
 			}
 
 		}
 		return folder;
 	}
 
 	@Transient
 	public Map<String, DtoActionStatus> getActionStatuses() {
 
 		synchronized (dn) {
 
 			Map<String, DtoActionStatus> actionStatusesForUser = actionStatuses
 			.get(dn);
 			if (actionStatusesForUser == null) {
 				actionStatusesForUser = new HashMap<String, DtoActionStatus>();
 				actionStatuses.put(dn, actionStatusesForUser);
 			}
 
 			return actionStatusesForUser;
 
 		}
 	}
 
 	@Transient
 	public List<Job> getActiveJobs(String application, boolean refresh) {
 
 		boolean inclBatchJobs = AbstractServiceInterface.INCLUDE_MULTIPARTJOBS_IN_PS_COMMAND;
 		if (Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 			inclBatchJobs = true;
 		}
 
 		try {
 
 			List<Job> jobs = null;
 			if (StringUtils.isBlank(application)
 					|| Constants.ALLJOBS_KEY.equals(application)
 					|| Constants.ALLJOBS_INCL_BATCH_KEY.equals(application)) {
 				jobs = jobdao.findJobByDN(getDn(), inclBatchJobs);
 			} else {
 				jobs = jobdao
 				.findJobByDNPerApplication(
 						getDn(),
 						application,
 						inclBatchJobs);
 			}
 
 			if (refresh) {
 				refreshJobStatus(jobs);
 			}
 
 			return jobs;
 
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	@Transient
 	public Set<String> getAllAvailableUniqueGroupnames() {
 
 		if (cachedUniqueGroupnames == null) {
 
 			cachedUniqueGroupnames = new TreeSet<String>();
 			Iterator<String> it = getFqans().keySet().iterator();
 
 			while (it.hasNext()) {
 				String fqan = it.next();
 				cachedUniqueGroupnames.add(getUniqueGroupname(fqan));
 			}
 		}
 		return cachedUniqueGroupnames;
 	}
 
 	/**
 	 * Returns all mountpoints (including automounted ones for this session.
 	 * 
 	 * @return all mountpoints for this session
 	 */
 	@Transient
 	public Set<MountPoint> getAllMountPoints() {
 		if (allMountPoints == null) {
 
 			throw new IllegalStateException("Mountpoints not set yet.");
 			// allMountPoints = new TreeSet<MountPoint>();
 			// // first the automounted ones because the manually ones are more
 			// // important
 			// allMountPoints.addAll(mountPointsAutoMounted);
 			// allMountPoints.addAll(getMountPoints());
 		}
 		return allMountPoints;
 	}
 
 	@Transient
 	public List<Job> getArchivedJobs(final String application) {
 
 		try {
 
 			final List<Job> archivedJobs = Collections
 			.synchronizedList(new LinkedList<Job>());
 
 			final ExecutorService executor = Executors
 			.newFixedThreadPool(getArchiveLocations().size());
 
 			for (final String archiveLocation : getArchiveLocations().values()) {
 
 				Thread t = new Thread() {
 					@Override
 					public void run() {
 
 						myLogger.debug(getDn() + ":\tGetting archived job on: "
 								+ archiveLocations);
 						List<Job> jobObjects = null;
 						try {
 							jobObjects = getArchivedJobsFromFileSystem(archiveLocation);
 							if (application == null) {
 								for (Job job : jobObjects) {
 									archivedJobs.add(job);
 								}
 							} else {
 
 								for (Job job : jobObjects) {
 
 									String app = job.getJobProperties().get(
 											Constants.APPLICATIONNAME_KEY);
 
 									if (application.equals(app)) {
 										archivedJobs.add(job);
 									}
 								}
 							}
 						} catch (RemoteFileSystemException e) {
 							myLogger.error(e);
 						}
 
 					}
 				};
 				executor.execute(t);
 			}
 
 			executor.shutdown();
 
 			executor.awaitTermination(3, TimeUnit.MINUTES);
 
 			return archivedJobs;
 
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	@Transient
 	private List<Job> getArchivedJobsFromFileSystem(String fs)
 	throws RemoteFileSystemException {
 
 		if (StringUtils.isBlank(fs)) {
 			fs = getDefaultArchiveLocation();
 		}
 
 		if ( fs == null ) {
 			return new LinkedList<Job>();
 		}
 
 		synchronized (fs) {
 
 			final List<Job> jobs = Collections
 			.synchronizedList(new LinkedList<Job>());
 
 			GridFile file = ls(fs, 1);
 
 			final ExecutorService executor = Executors
 			.newFixedThreadPool(ServerPropertiesManager
 					.getConcurrentArchivedJobLookupsPerFilesystem());
 
 			for (final GridFile f : file.getChildren()) {
 				Thread t = new Thread() {
 					@Override
 					public void run() {
 
 						try {
 							Job job = loadJobFromFilesystem(f.getUrl());
 							jobs.add(job);
 
 						} catch (NoSuchJobException e) {
 							myLogger.debug("No job for url: " + f.getUrl());
 						}
 					}
 				};
 				executor.execute(t);
 			}
 
 			executor.shutdown();
 
 			try {
 				executor.awaitTermination(2, TimeUnit.MINUTES);
 			} catch (InterruptedException e) {
 				myLogger.error(e);
 			}
 
 
 			return jobs;
 
 		}
 
 	}
 
 	/**
 	 * Gets a map of this users bookmarks.
 	 * 
 	 * @return the users' properties
 	 */
 	@ElementCollection(fetch = FetchType.EAGER)
 	public Map<String, String> getArchiveLocations() {
 		if (archiveLocations == null) {
 			archiveLocations = new TreeMap<String, String>();
 		}
 
 		return archiveLocations;
 	}
 
 	@Transient
 	public BatchJob getBatchJobFromDatabase(final String batchJobname)
 	throws NoSuchJobException {
 
 		final BatchJob job = batchJobDao.findJobByDN(getCred()
 				.getDn(), batchJobname);
 
 		return job;
 
 	}
 
 	/**
 	 * Gets a map of this users bookmarks.
 	 * 
 	 * @return the users' properties
 	 */
 	@ElementCollection(fetch = FetchType.EAGER)
 	public Map<String, String> getBookmarks() {
 		return bookmarks;
 	}
 
 	/**
 	 * Returns the default credential of the user (if any).
 	 * 
 	 * @return the default credential or null if there is none
 	 */
 	@Transient
 	public ProxyCredential getCred() {
 		return cred;
 	}
 
 	@Transient
 	public ProxyCredential getCred(String fqan) {
 
 		ProxyCredential credToUse = cachedCredentials.get(fqan);
 
 		if ((credToUse == null) || !credToUse.isValid()) {
 
 			// put a new credential in the cache
 			final VO vo = VOManagement.getVO(getFqans().get(fqan));
 			credToUse = CertHelpers.getVOProxyCredential(vo, fqan, getCred());
 			cachedCredentials.put(fqan, credToUse);
 		}
 
 		return credToUse;
 	}
 
 	@Transient
 	public synchronized String getDefaultArchiveLocation() {
 
 		String defArcLoc = null;
 
 		// if user configured default, use that.
 		defArcLoc = getUserProperties().get(
 				Constants.DEFAULT_JOB_ARCHIVE_LOCATION);
 
 		if (!StringUtils.isBlank(defArcLoc)) {
 			myLogger.info("Using default archive location for user "
 					+ getDn() + ": " + defArcLoc);
 
 			return defArcLoc;
 		}
 
 		String defFqan = ServerPropertiesManager
 		.getDefaultFqanForArchivedJobDirectory();
 
 		// using backend default fqan if configured
 		if (StringUtils.isNotBlank(defFqan)) {
 			Set<MountPoint> mps = df(defFqan);
 			for (MountPoint mp : mps) {
 				if (!mp.isVolatileFileSystem()) {
 					defArcLoc = mp.getRootUrl()
 					+ "/"
 					+ ServerPropertiesManager
 					.getArchivedJobsDirectoryName();
 					addArchiveLocation(
 							Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 							defArcLoc);
 					// setUserProperty(Constants.DEFAULT_JOB_ARCHIVE_LOCATION,
 					// defArcLoc);
 					myLogger.debug("Using backend default archive location: "
 							+ defArcLoc);
 					return defArcLoc;
 				}
 			}
 		}
 
 
 
 		// to be removed once we switch to new backend
 		Set<MountPoint> mps = df("/nz/nesi");
 		for (MountPoint mp : mps) {
 			if (!mp.isVolatileFileSystem()) {
 
				defArcLoc = mp.getRootUrl()
 				+ "/"
 				+ ServerPropertiesManager
 				.getArchivedJobsDirectoryName();
 				addArchiveLocation(
 						Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 						defArcLoc);
 				return defArcLoc;
 
 			}
 		}
 
 		if (mps.size() > 0) {
 			MountPoint mp = mps.iterator().next();
 			defArcLoc = mp.getRootUrl()
 			+ "/"
 			+ ServerPropertiesManager
 			.getArchivedJobsDirectoryName();
 
 			addArchiveLocation(
 					Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 					defArcLoc);
 
 		} else {
 			mps = df("/ARCS/BeSTGRID/Drug_discovery/Local");
 			if (mps.size() > 0) {
 				MountPoint mp = mps.iterator().next();
 				defArcLoc = mp.getRootUrl()
 				+ "/"
 				+ ServerPropertiesManager
 				.getArchivedJobsDirectoryName();
 
 				addArchiveLocation(
 						Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 						defArcLoc);
 			} else {
 				mps = df("/ARCS/BeSTGRID");
 				if (mps.size() > 0) {
 					MountPoint mp = mps.iterator().next();
 					defArcLoc = mp.getRootUrl()
 					+ "/"
 					+ ServerPropertiesManager
 					.getArchivedJobsDirectoryName();
 
 					addArchiveLocation(
 							Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 							defArcLoc);
 
 				} else {
 					mps = getAllMountPoints();
 					if (mps.size() == 0) {
 						return null;
 					}
 					MountPoint mp = mps.iterator().next();
 					defArcLoc = mp.getRootUrl()
 					+ "/"
 					+ ServerPropertiesManager
 					.getArchivedJobsDirectoryName();
 
 					addArchiveLocation(
 							Constants.JOB_ARCHIVE_LOCATION_AUTO + mp.getAlias(),
 							defArcLoc);
 
 				}
 			}
 
 		}
 
 		userdao.saveOrUpdate(this);
 
 
 		myLogger.info("Using temporary default archive location for user "
 				+ getDn()
 				+ ": " + defArcLoc);
 
 		return defArcLoc;
 	}
 
 	// private List<FileReservation> getFileReservations() {
 	// return fileReservations;
 	// }
 	//
 	// private void setFileReservations(List<FileReservation> fileReservations)
 	// {
 	// this.fileReservations = fileReservations;
 	// }
 	//
 	// private List<FileTransfer> getFileTransfers() {
 	// return fileTransfers;
 	// }
 	//
 	// private void setFileTransfers(List<FileTransfer> fileTransfers) {
 	// this.fileTransfers = fileTransfers;
 	// }
 
 	/**
 	 * Returns the users dn.
 	 * 
 	 * @return the dn
 	 */
 	@Column(nullable = false)
 	public String getDn() {
 		return dn;
 	}
 
 	public String getFileSystemHomeDirectory(String filesystemRoot, String fqan)
 	throws FileSystemException {
 
 		final String key = filesystemRoot + fqan;
 		if (ENABLE_FILESYSTEM_CACHE
 				&& StringUtils.isNotBlank(getMountPointCache().get(key))) {
 
 			if (NOT_ACCESSIBLE.equals(getMountPointCache().get(key))) {
 
 				throw new FileSystemException(
 						"Cached entry indicates filesystem "
 						+ filesystemRoot
 						+ " is not accessible. Clear cache if you think that has changed.");
 			} else {
 				myLogger.debug(getDn() + ": found cached filesystem for "
 						+ filesystemRoot + " / " + fqan);
 				return getMountPointCache().get(key);
 			}
 		} else {
 			try {
 
 				String uri = null;
 
 				uri = getFileSystemManager()
 				.resolveFileSystemHomeDirectory(filesystemRoot, fqan);
 				myLogger.debug("Found filesystem home dir for: "
 						+ filesystemRoot + " / " + fqan + ": " + uri);
 
 				if (ENABLE_FILESYSTEM_CACHE && StringUtils.isNotBlank(uri)) {
 					myLogger.debug("Saving in fs cache...");
 					getMountPointCache().put(key, uri);
 					userdao.saveOrUpdate(this);
 				}
 
 				return uri;
 			} catch (final Exception e) {
 
 				if (ENABLE_FILESYSTEM_CACHE) {
 					myLogger.error(getDn() + ": Can't access filesystem "
 							+ filesystemRoot + " / " + fqan
 							+ ", saving in fs cache...");
 					getMountPointCache().put(key, NOT_ACCESSIBLE);
 					userdao.saveOrUpdate(this);
 				}
 
 				throw new FileSystemException(e);
 			}
 		}
 	}
 
 	@Transient
 	public FileSystemManager getFileSystemManager() {
 		if (fsm == null) {
 			this.fsm = new FileSystemManager(this);
 		}
 		return fsm;
 	}
 
 	/**
 	 * Getter for the users' fqans.
 	 * 
 	 * @return all fqans as map with the fqan as key and the vo as value
 	 */
 	@Transient
 	public Map<String, String> getFqans() {
 		if (fqans == null) {
 
 			// myLogger.debug("Checking credential");
 			if (cred.isValid()) {
 				fqans = VOManagement.getAllFqans(cred.getGssCredential());
 			}
 
 		}
 		return fqans;
 	}
 
 	@Transient
 	public String getFullFqan(String uniqueGroupname) {
 		return FqanHelpers.getFullFqan(getFqans().keySet(), uniqueGroupname);
 	}
 
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return id;
 	}
 
 	/**
 	 * Searches for the job with the specified jobname for the current user.
 	 * 
 	 * @param jobname
 	 *            the name of the job (which is unique within one user)
 	 * @return the job
 	 */
 	@Transient
 	public Job getJobFromDatabaseOrFileSystem(String jobnameOrUrl)
 	throws NoSuchJobException {
 
 		Job job = null;
 		try {
 			job = jobdao.findJobByDN(getDn(), jobnameOrUrl);
 			return job;
 		} catch (final NoSuchJobException nsje) {
 
 			if (jobnameOrUrl.startsWith("gridftp://")) {
 
 				for (Job archivedJob : getArchivedJobs(null)) {
 					if (job.getJobProperty(Constants.JOBDIRECTORY_KEY).equals(
 							jobnameOrUrl)) {
 						return job;
 					}
 				}
 			}
 
 		}
 		throw new NoSuchJobException("Job with name " + jobnameOrUrl
 				+ "does not exist.");
 	}
 
 	// public GridFile getFolderListing(final String url, int recursionLevel)
 	// throws RemoteFileSystemException, FileSystemException {
 	//
 	// try {
 	// return getFileSystemManager().getFolderListing(url, recursionLevel);
 	// } catch (InvalidPathException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// return null;
 	// }
 	// }
 
 	@Transient
 	public int getJobStatus(final String jobname) {
 
 		// myLogger.debug("Start getting status for job: " + jobname);
 		Job job;
 		try {
 			job = getJobFromDatabaseOrFileSystem(jobname);
 		} catch (final NoSuchJobException e) {
 			return JobConstants.NO_SUCH_JOB;
 		}
 
 		int status = Integer.MIN_VALUE;
 		final int old_status = job.getStatus();
 
 		// System.out.println("OLDSTAUS "+jobname+": "+JobConstants.translateStatus(old_status));
 		if (old_status <= JobConstants.READY_TO_SUBMIT) {
 			// this couldn't have changed without manual intervention
 			return old_status;
 		}
 
 		// check whether the no_such_job check is necessary
 		if (old_status >= JobConstants.FINISHED_EITHER_WAY) {
 			return old_status;
 		}
 
 		final Date lastCheck = job.getLastStatusCheck();
 		final Date now = new Date();
 
 		if ((old_status != JobConstants.EXTERNAL_HANDLE_READY)
 				&& (old_status != JobConstants.UNSUBMITTED)
 				&& (now.getTime() < lastCheck.getTime()
 						+ (ServerPropertiesManager
 								.getWaitTimeBetweenJobStatusChecks() * 1000))) {
 			myLogger.debug("Last check for job "
 					+ jobname
 					+ " was: "
 					+ lastCheck.toString()
 					+ ". Too early to check job status again. Returning old status...");
 			return job.getStatus();
 		}
 
 		final ProxyCredential cred = job.getCredential();
 		boolean changedCred = false;
 		// TODO check whether cred is stored in the database in that case? also,
 		// is a voms credential needed? -- apparently not - only dn must match
 		if ((cred == null) || !cred.isValid()) {
 
 			final VO vo = VOManagement.getVO(getFqans().get(
 					job.getFqan()));
 
 			job.setCredential(CertHelpers.getVOProxyCredential(vo,
 					job.getFqan(), getCred()));
 			changedCred = true;
 		}
 
 		// myLogger.debug("Getting status for job from submission manager: "
 		// + jobname);
 
 		status = getSubmissionManager().getJobStatus(job);
 		// myLogger.debug("Status for job" + jobname
 		// + " from submission manager: " + status);
 		if (changedCred) {
 			job.setCredential(null);
 		}
 		if (old_status != status) {
 			job.setStatus(status);
 			final String message = "Job status for job: " + job.getJobname()
 			+ " changed since last check ("
 			+ job.getLastStatusCheck().toString() + ") from: \""
 			+ JobConstants.translateStatus(old_status) + "\" to: \""
 			+ JobConstants.translateStatus(status) + "\"";
 			job.addLogMessage(message);
 			addLogMessageToPossibleMultiPartJobParent(job, message);
 			if ((status >= JobConstants.FINISHED_EITHER_WAY)
 					&& (status != JobConstants.DONE)) {
 				// job.addJobProperty(Constants.ERROR_REASON,
 				// "Job finished with status: "
 				// + JobConstants.translateStatus(status));
 				job.addLogMessage("Job failed. Status: "
 						+ JobConstants.translateStatus(status));
 				final String multiPartJobParent = job
 				.getJobProperty(Constants.BATCHJOB_NAME);
 				if (multiPartJobParent != null) {
 					try {
 						final BatchJob mpj = getBatchJobFromDatabase(multiPartJobParent);
 						mpj.addFailedJob(job.getJobname());
 						addLogMessageToPossibleMultiPartJobParent(job, "Job: "
 								+ job.getJobname() + " failed. Status: "
 								+ JobConstants.translateStatus(job.getStatus()));
 						batchJobDao.saveOrUpdate(mpj);
 					} catch (final NoSuchJobException e) {
 						// well
 						myLogger.error(e);
 					}
 				}
 			}
 		}
 		job.setLastStatusCheck(new Date());
 		jobdao.saveOrUpdate(job);
 
 		// myLogger.debug("Status of job: " + job.getJobname() + " is: " +
 		// status);
 		return status;
 	}
 
 	@ElementCollection
 	public Map<String, JobSubmissionObjectImpl> getJobTemplates() {
 		return jobTemplates;
 	}
 
 	@ElementCollection(fetch = FetchType.EAGER)
 	@Column(length = 400)
 	private Map<String, String> getMountPointCache() {
 		return mountPointCache;
 	}
 
 	// for hibernate
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@JoinTable
 	private Set<MountPoint> getMountPoints() {
 		return mountPoints;
 	}
 
 	@Transient
 	public Set<MountPoint> getMountPoints(String fqan) {
 		if (fqan == null) {
 			fqan = Constants.NON_VO_FQAN;
 		}
 
 		synchronized (fqan) {
 
 			if (mountPointsPerFqanCache.get(fqan) == null) {
 
 				final Set<MountPoint> mps = new HashSet<MountPoint>();
 				for (final MountPoint mp : getAllMountPoints()) {
 					if ((mp.getFqan() == null)
 							|| mp.getFqan().equals(Constants.NON_VO_FQAN)) {
 						if ((fqan == null)
 								|| fqan.equals(Constants.NON_VO_FQAN)) {
 							mps.add(mp);
 							continue;
 						} else {
 							continue;
 						}
 					} else {
 						if (mp.getFqan().equals(fqan)) {
 							mps.add(mp);
 							continue;
 						}
 					}
 				}
 				mountPointsPerFqanCache.put(fqan, mps);
 			}
 			return mountPointsPerFqanCache.get(fqan);
 		}
 	}
 
 	/**
 	 * Checks whether the filesystem of any of the users' mountpoints contains
 	 * the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint of null if no filesystem contains this file
 	 */
 	@Transient
 	public MountPoint getResponsibleMountpointForAbsoluteFile(final String file) {
 
 		final String new_file = null;
 		// myLogger.debug("Finding mountpoint for file: " + file);
 
 		for (final MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForAbsoluteFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether any of the users' mountpoints contain the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint or null if the file is not on any of the
 	 *         mountpoints
 	 */
 	@Transient
 	public MountPoint getResponsibleMountpointForUserSpaceFile(final String file) {
 
 		for (final MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForUserSpaceFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	@Transient
 	public JobSubmissionManager getSubmissionManager() {
 		if (manager == null) {
 			final Map<String, JobSubmitter> submitters = new HashMap<String, JobSubmitter>();
 			submitters.put("GT4", new GT4Submitter());
 			submitters.put("GT5", new GT5Submitter());
 			submitters.put("GT4Dummy", new GT4DummySubmitter());
 			manager = new JobSubmissionManager(
 					AbstractServiceInterface.informationManager, submitters);
 		}
 		return manager;
 	}
 
 	@Transient
 	public String getUniqueGroupname(String fqan) {
 		return FqanHelpers.getUniqueGroupname(getFqans().keySet(), fqan);
 	}
 
 	/**
 	 * Gets a map of this users properties. These properties can be used to
 	 * store anything you can think of. Usful for history and such.
 	 * 
 	 * @return the users' properties
 	 */
 	@ElementCollection(fetch = FetchType.EAGER)
 	public Map<String, String> getUserProperties() {
 		return userProperties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return 29 * dn.hashCode();
 	}
 
 	private Job loadJobFromFilesystem(String url) throws NoSuchJobException {
 		String grisuJobPropertiesFile = null;
 
 		if (url.endsWith(ServiceInterface.GRISU_JOB_FILE_NAME)) {
 			grisuJobPropertiesFile = url;
 		} else {
 			if (url.endsWith("/")) {
 				grisuJobPropertiesFile = url
 				+ ServiceInterface.GRISU_JOB_FILE_NAME;
 			} else {
 				grisuJobPropertiesFile = url + "/"
 				+ ServiceInterface.GRISU_JOB_FILE_NAME;
 			}
 
 		}
 
 		Job job = null;
 
 		synchronized (grisuJobPropertiesFile) {
 
 			try {
 
 				if (getFileSystemManager().fileExists(grisuJobPropertiesFile)) {
 
 					Object cacheJob = AbstractServiceInterface
 					.getFromSessionCache(grisuJobPropertiesFile);
 
 					if (cacheJob != null) {
 						return (Job) cacheJob;
 					}
 
 					final Serializer serializer = new Persister();
 
 					GrisuInputStream fin = null;
 					try {
 						fin = getFileSystemManager().getInputStream(
 								grisuJobPropertiesFile);
 						job = serializer.read(Job.class, fin.getStream());
 						fin.close();
 
 						AbstractServiceInterface.putIntoSessionCache(
 								grisuJobPropertiesFile, job);
 
 						return job;
 					} catch (final Exception e) {
 						e.printStackTrace();
 						throw new NoSuchJobException("Can't find job at location: "
 								+ url);
 					} finally {
 						try {
 							fin.close();
 						} catch (final Exception e) {
 							e.printStackTrace();
 							throw new NoSuchJobException(
 									"Can't find job at location: " + url);
 						}
 					}
 
 				} else {
 					throw new NoSuchJobException("Can't find job at location: "
 							+ url);
 				}
 			} catch (final RemoteFileSystemException e) {
 				throw new NoSuchJobException("Can't find job at location: " + url);
 			}
 		}
 	}
 
 	public void logout() {
 		actionStatuses.remove(dn);
 	}
 
 	public GridFile ls(final String directory, int recursion_level)
 	throws RemoteFileSystemException {
 
 		try {
 
 			if (recursion_level == 0) {
 				final GridFile file = getFileSystemManager()
 				.getFolderListing(directory, 0);
 				return file;
 			}
 
 			final GridFile rootfolder = getFileSystemManager()
 			.getFolderListing(directory, 1);
 			if (recursion_level == 1) {
 
 				return rootfolder;
 			} else if (recursion_level <= 0) {
 				recursion_level = Integer.MAX_VALUE;
 			}
 
 			recursion_level = recursion_level - 1;
 
 			fillFolder(rootfolder, recursion_level);
 			return rootfolder;
 
 		} catch (final Exception e) {
 			// e.printStackTrace();
 			throw new RemoteFileSystemException("Could not list directory "
 					+ directory + ": " + e.getLocalizedMessage());
 		}
 	}
 
 	/**
 	 * Mounts a filesystem using the default credential of a user.
 	 * 
 	 * @param root
 	 *            the filesystem to mount (something like:
 	 *            gsiftp://ngdata.vapc.org/home/san04/markus)
 	 * @param name
 	 *            the name of the mountpoint (something like: /remote or
 	 *            /remote.vpac)
 	 * @throws FileSystemException
 	 *             if the filesystem could not be mounted
 	 * @throws RemoteFileSystemException
 	 * @throws VomsException
 	 */
 	public MountPoint mountFileSystem(final String root, final String name,
 			final boolean useHomeDirectory, String site)
 	throws RemoteFileSystemException {
 
 		return mountFileSystem(root, name, getCred(), useHomeDirectory, site);
 	}
 
 	/**
 	 * Adds a filesystem to the mountpoints of this user. The mountpoint is just
 	 * an alias to make the urls shorter and easier to read/remember. You only
 	 * need to mount a filesystem once. After you persisted the user (with
 	 * hibernate) the alias and rootUrl of the filesystem are persisted as well.
 	 * 
 	 * A mountpoint always has to be in the root directory (for example: /local
 	 * or /remote -- never /remote/ng2.vpac.org )
 	 * 
 	 * @param uri
 	 *            the filesystem to mount (something like:
 	 *            gsiftp://ngdata.vapc.org/home/san04/markus)
 	 * @param mountPointName
 	 *            the name of the mountpoint (something like: /remote or
 	 *            /remote.vpac)
 	 * @param cred
 	 *            the credential that is used to contact the filesystem (can be
 	 *            null for local filesystems)
 	 * @return the root FileObject of the newly mounted FileSystem
 	 * @throws VomsException
 	 * @throws FileSystemException
 	 *             if the filesystem could not be mounted
 	 */
 	public MountPoint mountFileSystem(String uri, final String mountPointName,
 			final ProxyCredential cred, final boolean useHomeDirectory,
 			final String site) throws RemoteFileSystemException {
 
 		MountPoint new_mp = getFileSystemManager().mountFileSystem(uri,
 				mountPointName, cred, useHomeDirectory, site);
 
 		if (!mountPoints.contains(new_mp)) {
 			mountPoints.add(new_mp);
 			getAllMountPoints().add(new_mp);
 		}
 
 		userdao.saveOrUpdate(this);
 
 		return new_mp;
 
 	}
 
 	public MountPoint mountFileSystem(final String root, final String name,
 			final String fqan, final boolean useHomeDirectory, final String site)
 	throws RemoteFileSystemException {
 
 		if ((fqan == null) || Constants.NON_VO_FQAN.equals(fqan)) {
 			return mountFileSystem(root, name, useHomeDirectory, site);
 		} else {
 
 			final Map<String, String> temp = getFqans();
 			final VO vo = VOManagement.getVO(temp.get(fqan));
 
 			final ProxyCredential vomsProxyCred = CertHelpers
 			.getVOProxyCredential(vo, fqan, getCred());
 
 			return mountFileSystem(root, name, vomsProxyCred, useHomeDirectory,
 					site);
 		}
 	}
 
 	/**
 	 * Just a method to refresh the status of all jobs. Could be used by
 	 * something like a cronjob as well. TODO: maybe change to public?
 	 * 
 	 * @param jobs
 	 *            a list of jobs you want to have refreshed
 	 */
 	protected void refreshJobStatus(final Collection<Job> jobs) {
 		for (final Job job : jobs) {
 			getJobStatus(job.getJobname());
 		}
 	}
 
 	public void removeArchiveLocation(String alias) {
 
 		getArchiveLocations().remove(alias);
 		userdao.saveOrUpdate(this);
 
 	}
 
 	public void removeBookmark(String alias) {
 		this.bookmarks.remove(alias);
 		userdao.saveOrUpdate(this);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void removeFqan(final String fqan) {
 		fqans.remove(fqan);
 	}
 
 	public void removeProperty(final String key) {
 		userProperties.remove(key);
 		userdao.saveOrUpdate(this);
 	}
 
 	// public void addProperty(String key, String value) {
 	// List<String> list = userProperties.get(key);
 	// if ( list == null ) {
 	// list = new LinkedList<String>();
 	// }
 	// list.add(value);
 	// }
 
 	public void resetMountPoints() {
 		// allMountPoints = null;
 	}
 
 	/**
 	 * Translates an absolute file url into an "user-space" one.
 	 * 
 	 * @param file
 	 *            an absolute file url
 	 *            (gsiftp://ngdata.vpac.org/home/san04/markus/test.txt)
 	 * @return the "user-space" file url (/ngdata.vpac.org/test.txt)
 	 */
 	public String returnUserSpaceUrl(final String file) {
 		final MountPoint mp = getResponsibleMountpointForAbsoluteFile(file);
 		return mp.replaceAbsoluteRootUrlWithMountPoint(file);
 	}
 
 	private void setArchiveLocations(final Map<String, String> al) {
 		this.archiveLocations = al;
 	}
 
 	/**
 	 * Set's additional mountpoints that the user did not explicitly mount
 	 * manually.
 	 * 
 	 * @param amps
 	 *            the mountpoints to add (for this session)
 	 */
 	public void setAutoMountedMountPoints(final Set<MountPoint> amps) {
 		// allMountPoints = null;
 		this.mountPointsAutoMounted = amps;
 
 		allMountPoints = new TreeSet<MountPoint>();
 		// first the automounted ones because the manually ones are more
 		// important
 		allMountPoints.addAll(mountPointsAutoMounted);
 		allMountPoints.addAll(getMountPoints());
 
 	}
 
 	private void setBookmarks(final Map<String, String> bm) {
 		this.bookmarks = bm;
 	}
 
 	/**
 	 * Sets the default credential for the user. The default credential is used
 	 * mostly as convenience.
 	 * 
 	 * @param cred
 	 *            the credential to use as default
 	 */
 	public void setCred(final ProxyCredential cred) {
 
 		this.cred = cred;
 	}
 
 	/**
 	 * For hibernate.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 */
 	private void setDn(final String dn) {
 		this.dn = dn;
 	}
 
 	/**
 	 * Setter for the users' fqans.
 	 * 
 	 * @param fqans
 	 *            all fqans as map with the fqan as key and the vo as value
 	 */
 	private void setFqans(final Map<String, String> fqans) {
 		this.fqans = fqans;
 	}
 
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	public void setJobTemplates(
 			final Map<String, JobSubmissionObjectImpl> jobTemplates) {
 		this.jobTemplates = jobTemplates;
 	}
 
 	private void setMountPointCache(final Map<String, String> mountPoints) {
 		this.mountPointCache = mountPoints;
 	}
 
 	// for hibernate
 	private void setMountPoints(final Set<MountPoint> mountPoints) {
 		this.mountPoints = mountPoints;
 	}
 
 	private void setUserProperties(final Map<String, String> userProperties) {
 		this.userProperties = userProperties;
 	}
 
 	public void setUserProperty(String key, String value) {
 
 		if (StringUtils.isBlank(key)) {
 			return;
 		}
 
 		if (Constants.CLEAR_MOUNTPOINT_CACHE.equals(key)) {
 			clearMountPointCache(value);
 			return;
 		} else if (Constants.JOB_ARCHIVE_LOCATION.equals(key)) {
 			String[] temp = value.split(";");
 			String alias = temp[0];
 			String url = temp[0];
 			if (temp.length == 2) {
 				url = temp[1];
 			}
 			addArchiveLocation(alias, url);
 			return;
 		}
 
 		addProperty(key, value);
 
 	}
 
 	/**
 	 * Unmounts a filesystem.
 	 * 
 	 * @param mountPointName
 	 *            the name of the mountpoint (/local or /remote or something)
 	 * @throws FileSystemException
 	 *             if the filesystem could not be unmounted or something else
 	 *             went wrong
 	 */
 	public void unmountFileSystem(final String mountPointName) {
 
 		for (final MountPoint mp : mountPoints) {
 			if (mp.getAlias().equals(mountPointName)) {
 				mountPoints.remove(mp);
 				getAllMountPoints().remove(mp);
 				return;
 			}
 		}
 		userdao.saveOrUpdate(this);
 	}
 }
