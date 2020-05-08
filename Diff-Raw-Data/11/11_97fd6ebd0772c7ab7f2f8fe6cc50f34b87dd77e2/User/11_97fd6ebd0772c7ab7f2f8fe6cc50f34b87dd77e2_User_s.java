 package org.vpac.grisu.backend.model;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
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
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystem;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.impl.DefaultFileSystemManager;
 import org.apache.log4j.Logger;
import org.hibernate.annotations.CollectionOfElements;
 import org.vpac.grisu.backend.hibernate.UserDAO;
 import org.vpac.grisu.backend.model.fs.FileSystemManager;
 import org.vpac.grisu.backend.model.fs.InvalidPathException;
 import org.vpac.grisu.backend.model.fs.ThreadLocalCommonsVfsManager;
 import org.vpac.grisu.backend.model.job.Job;
 import org.vpac.grisu.backend.model.job.JobSubmissionManager;
 import org.vpac.grisu.backend.model.job.JobSubmitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4DummySubmitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4Submitter;
 import org.vpac.grisu.backend.model.job.gt5.GT5Submitter;
 import org.vpac.grisu.backend.utils.CertHelpers;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
 import org.vpac.grisu.model.MountPoint;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.dto.GridFile;
 import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
 import org.vpac.grisu.utils.FqanHelpers;
 import org.vpac.grisu.utils.MountPointHelpers;
 import org.vpac.security.light.voms.VO;
 import org.vpac.security.light.voms.VOManagement.VOManagement;
 import org.vpac.security.light.vomsProxy.VomsException;
 
 import au.org.arcs.jcommons.constants.Constants;
 
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
 
 	public static UserDAO userdao = new UserDAO();
 
 	private static Logger myLogger = Logger.getLogger(User.class.getName());
 
 	public static User createUser(ProxyCredential cred,
 			AbstractServiceInterface si) {
 
 		// make sure there is a valid credential
 		if ((cred == null) || !cred.isValid()) {
 			throw new NoValidCredentialException(
 					"No valid credential exists in this session");
 		}
 
 		myLogger.debug("CREATING USER SESSION: " + cred.getDn());
 
 		// if ( getCredential())
 
 		User user;
 		// try to look up user in the database
 		user = userdao.findUserByDN(cred.getDn());
 
 		if (user == null) {
 			user = new User(cred);
 			userdao.saveOrUpdate(user);
 		} else {
 			user.setCred(cred);
 		}
 
 		try {
 			user.setAutoMountedMountPoints(user.df_auto_mds(si.getAllSites()
 					.asArray()));
 		} catch (Exception e) {
 			throw new RuntimeException(
 					"Can't aquire filesystems for user. Possibly because of misconfigured grisu backend",
 					e);
 		}
 
 		return user;
 
 	}
 
 	public static String get_vo_dn_path(final String dn) {
 		return dn.replace("=", "_").replace(",", "_").replace(" ", "_");
 	}
 
 	private final ThreadLocalCommonsVfsManager threadLocalFsManager = new ThreadLocalCommonsVfsManager(
 			this);
 
 	private Long id = null;
 
 	// the (default) credential to contact gridftp file shares
 	private ProxyCredential cred = null;
 
 	// managers the virtual filesystem manager
 	private final DefaultFileSystemManager fsmanager = null;
 
 	private JobSubmissionManager manager;
 
 	// this needs to be static because otherwise the session be lost and the
 	// action status can't be found anymore by the client
 	private static final Map<String, Map<String, DtoActionStatus>> actionStatuses = new HashMap<String, Map<String, DtoActionStatus>>();
 
 	private static final String NOT_ACCESSIBLE = "Not accessible";
 
 	// private static final Map<String, MountPoint> cachedGridFtpHomeDirs = new
 	// HashMap<String, MountPoint>();
 
 	// the (default) credentials dn
 	private String dn = null;
 
 	// the mountpoints of a user
 	private Set<MountPoint> mountPoints = new HashSet<MountPoint>();
 
 	private Map<String, String> mountPointCache = new HashMap<String, String>();
 	private final Map<String, Set<MountPoint>> mountPointsPerFqanCache = new TreeMap<String, Set<MountPoint>>();
 
 	private Set<MountPoint> mountPointsAutoMounted = new HashSet<MountPoint>();
 
 	private Set<MountPoint> allMountPoints = null;
 
 	// // persistent properties
 	// // not used yet
 	// private List<FileTransfer> fileTransfers = new ArrayList<FileTransfer>();
 
 	// // not used yet
 	// private List<FileReservation> fileReservations = new
 	// ArrayList<FileReservation>();
 
 	// filesystem connections are cached so that we don't need to connect again
 	// everytime we access one
 	// private final Map<MountPoint, FileSystem> cachedFilesystemConnections =
 	// new HashMap<MountPoint, FileSystem>();
 	// credentials are chache so we don't have to contact myproxy/voms anytime
 	// we want to make a transaction
 	private Map<String, ProxyCredential> cachedCredentials = new HashMap<String, ProxyCredential>();
 	// All fqans of the user
 	private Map<String, String> fqans = null;
 	private Set<String> cachedUniqueGroupnames = null;
 
 	private Map<String, String> userProperties = new HashMap<String, String>();
 
 	private Map<String, String> bookmarks = new HashMap<String, String>();
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
 
 	public void addProperty(String key, String value) {
 
 		getUserProperties().put(key, value);
 		userdao.saveOrUpdate(this);
 
 	}
 
 	/**
 	 * Resolves the provided filename into a FileObject. If the filename starts
 	 * with "/" a file on one of the "mounted" filesystems is looked up. Else it
 	 * has to start with the name of a (supported) protocol (like: gsiftp:///).
 	 * 
 	 * @param file
 	 *            the filename
 	 * @return the FileObject
 	 * @throws RemoteFileSystemException
 	 *             if there was an error accessing the file
 	 * @throws VomsException
 	 *             if the (possible) required voms credential could not be
 	 *             created
 	 */
 	public FileObject aquireFile(final String file)
 			throws RemoteFileSystemException {
 		return getFileSystemManager().aquireFile(file, null);
 	}
 
 	/**
 	 * Resolves the provided filename into a FileObject. If the filename starts
 	 * with "/" a file on one of the "mounted" filesystems is looked up. Else it
 	 * has to start with the name of a (supported) protocol (like: gsiftp:///).
 	 * 
 	 * @param urlOrPath
 	 *            the filename
 	 * @param cred
 	 *            the credential to access the filesystem on which the file
 	 *            resides
 	 * @return the FileObject
 	 * @throws RemoteFileSystemException
 	 *             if there is a problem resolving the file
 	 * @throws VomsException
 	 *             if the (possible) required voms credential could not be
 	 *             created
 	 */
 	public FileObject aquireFile(final String urlOrPath, final String fqan)
 			throws RemoteFileSystemException {
 
 		return getFileSystemManager().aquireFile(urlOrPath, fqan);
 
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
 			this.mountPointCache = new HashMap<String, String>();
 		}
 		userdao.saveOrUpdate(this);
 	}
 
 	public void closeFileSystems() {
 
 		myLogger.debug("Closing all filesystems for thread "
 				+ Thread.currentThread().getName());
 
 		threadLocalFsManager.remove();
 		getFileSystemManager().closeFileSystems();
 	}
 
 	private MountPoint createMountPoint(String server, String path,
 			final String fqan, Executor executor) {
 
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
 			myLogger.debug("No extra properties for path: " + path);
 		}
 
 		final Map<String, String> properties = new HashMap<String, String>();
 		boolean userDnPath = true;
 		if (StringUtils.isNotBlank(propString)) {
 
 			final String[] parts = propString.split(";");
 			for (final String part : parts) {
 				if (part.indexOf("=") <= 0) {
 					myLogger.error("Invalid path spec: " + path
 							+ ".  No \"=\" found. Ignoring this mountpoint...");
 					return null;
 				}
 				final String key = part.substring(0, part.indexOf("="));
 				if (StringUtils.isBlank(key)) {
 					myLogger.error("Invalid path spec: " + path
 							+ ".  No key found. Ignoring this mountpoint...");
 					return null;
 				}
 				String value = null;
 				try {
 					value = part.substring(part.indexOf("=") + 1);
 					if (StringUtils.isBlank(value)) {
 						myLogger.error("Invalid path spec: "
 								+ path
 								+ ".  No key found. Ignoring this mountpoint...");
 						return null;
 					}
 				} catch (final Exception e) {
 					myLogger.error("Invalid path spec: " + path
 							+ ".  No key found. Ignoring this mountpoint...");
 					return null;
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
 				// TODO Auto-generated catch block
 				myLogger.error(e);
 			}
 
 		} else if (path.contains("${GLOBUS_USER_HOME}")) {
 			try {
 				myLogger.warn("Using ${GLOBUS_USER_HOME} is deprecated. Please use . instead.");
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan);
 				userDnPath = false;
 			} catch (final Exception e) {
 				myLogger.error(e);
 			}
 
 		} else if (path.contains("${GLOBUS_SCRATCH_DIR")) {
 			try {
 				url = getFileSystemHomeDirectory(server.replace(":2811", ""),
 						fqan) + "/.globus/scratch";
 				userDnPath = false;
 			} catch (final Exception e) {
 				myLogger.error(e);
 			}
 		} else {
 
 			// url = server.replace(":2811", "") + path + "/"
 			// + User.get_vo_dn_path(getCred().getDn());
 			url = server.replace(":2811", "") + tempPath;
 
 		}
 
 		if (StringUtils.isBlank(url)) {
 			myLogger.error("Url is blank for " + server + " and " + path);
 			return null;
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
 							myLogger.debug("Checking whether mountpoint "
 									+ urlTemp + " exists...");
 
 							// checking whether subfolder exists
 							if (StringUtils
 									.isNotBlank(mountPointCache.get(key))) {
 								myLogger.debug("Found "
 										+ urlTemp
 										+ "in cache, not trying to access/create folder...");
 								return;
 							}
 							myLogger.debug("Did not find "
 									+ urlTemp
 									+ "in cache, trying to access/create folder...");
 							final boolean exists = aquireFile(urlTemp, fqan)
 									.exists();
 							if (!exists) {
 								myLogger.debug("Mountpoint does not exist. Trying to create non-exitent folder: "
 										+ urlTemp);
 								aquireFile(urlTemp, fqan).createFolder();
 							} else {
 								myLogger.debug("MountPoint " + urlTemp
 										+ " exists.");
 							}
 
 							mountPointCache.put(key, "Exists");
 
 						} catch (final Exception e) {
 							myLogger.error("Could not create folder: "
 									+ urlTemp, e);
 
 							mountPointCache.put(key, "Does not exist");
 						} finally {
 							try {
 								closeFileSystems();
 								userdao.saveOrUpdate(User.this);
 							} catch (Exception e) {
 								myLogger.debug("Could not save filesystem state for fs "
 										+ urlTemp
 										+ ": "
 										+ e.getLocalizedMessage());
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
 
 		return mp;
 
 		// + "." + fqan + "." + path);
 		// + "." + fqan);
 		// cachedGridFtpHomeDirs.put(keyMP, mp);
 		//
 		// return cachedGridFtpHomeDirs.get(keyMP);
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
 
 		final Set<MountPoint> mps = new TreeSet<MountPoint>();
 
 		// to check whether dn_subdirs are created already and create them if
 		// not (in background)
 		final ExecutorService executor = Executors.newFixedThreadPool(1);
 
 		// for ( String site : sites ) {
 
 		for (final String fqan : getFqans().keySet()) {
 			final Date start = new Date();
 			final Map<String, String[]> mpUrl = AbstractServiceInterface.informationManager
 					.getDataLocationsForVO(fqan);
 			final Date end = new Date();
 			myLogger.debug("Querying for data locations for all sites and+ "
 					+ fqan + " took: " + (end.getTime() - start.getTime())
 					+ " ms.");
 			for (final String server : mpUrl.keySet()) {
 				try {
 					for (final String path : mpUrl.get(server)) {
 
 						final MountPoint mp = createMountPoint(server, path,
 								fqan, executor);
 						if (mp != null) {
 							mps.add(mp);
 						}
 					}
 				} catch (final Exception e) {
 					myLogger.error(
 							"Can't use mountpoint " + server + ": "
 									+ e.getLocalizedMessage(), e);
 				}
 			}
 		}
 
 		executor.shutdown();
 
 		// TODO check whether that makes sense
 		closeFileSystems();
 
 		// }
 
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
 
 	/**
 	 * Gets a map of this users bookmarks.
 	 * 
 	 * @return the users' properties
 	 */
	@CollectionOfElements(fetch = FetchType.EAGER)
 	public Map<String, String> getBookmarks() {
 		return bookmarks;
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
 		if (StringUtils.isNotBlank(mountPointCache.get(key))) {
 			if (NOT_ACCESSIBLE.equals(mountPointCache.get(key))) {
 
 				throw new FileSystemException(
 						"Cached entry indicates filesystem "
 								+ filesystemRoot
 								+ " is not accessible. Clear cache if you think that has changed.");
 			}
 
 			return mountPointCache.get(key);
 		} else {
 			try {
 				// FileSystem fileSystem = createFilesystem(filesystemRoot,
 				// fqan);
 				final FileSystem fileSystem = threadLocalFsManager
 						.getFileSystem(filesystemRoot, fqan);
 				myLogger.debug("Connected to file system.");
 
 				myLogger.debug("Using home directory: "
 						+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 								.substring(1));
 
 				final String home = (String) fileSystem
 						.getAttribute("HOME_DIRECTORY");
 				final String uri = fileSystem.getRoot().getName().getRootURI()
 						+ home.substring(1);
 
 				if (StringUtils.isNotBlank(uri)) {
 					mountPointCache.put(key, uri);
 					userdao.saveOrUpdate(this);
 				}
 
 				return uri;
 			} catch (final Exception e) {
 
 				mountPointCache.put(key, NOT_ACCESSIBLE);
 				userdao.saveOrUpdate(this);
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
 
 	public GridFile getFolderListing(final String url)
 			throws RemoteFileSystemException, FileSystemException {
 
 		try {
 			return getFileSystemManager().getFolderListing(url);
 		} catch (InvalidPathException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			return null;
 		}
 	}
 
 	/**
 	 * Getter for the users' fqans.
 	 * 
 	 * @return all fqans as map with the fqan as key and the vo as value
 	 */
 	@Transient
 	public Map<String, String> getFqans() {
 		if (fqans == null) {
 
 			myLogger.debug("Checking credential");
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
 				for (final MountPoint mp : allMountPoints) {
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
 
 	// /**
 	// * Used internally to mount filesystems.
 	// *
 	// * @return the filesystem manager of the user
 	// * @throws FileSystemException
 	// * if something goes wrong
 	// */
 	// @Transient
 	// private DefaultFileSystemManager getFsManager() throws
 	// FileSystemException {
 	// // if (fsmanager == null) {
 	// //
 	// // fsmanager = VFSUtil.createNewFsManager(false, false, true, true,
 	// // true, true, true, null);
 	// //
 	// // }
 	// // return fsmanager;
 	// // System.out.println("Creating new FS Manager.");
 	// // return VFSUtil.createNewFsManager(false, false, true, true, true,
 	// // true, true, null);
 	// return threadLocalFsManager.getFsManager();
 	// }
 
 	/**
 	 * Checks whether the filesystem of any of the users' mountpoints contains
 	 * the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint of null if no filesystem contains this file
 	 */
 	public MountPoint getResponsibleMountpointForAbsoluteFile(final String file) {
 
 		final String new_file = null;
 		myLogger.debug("Finding mountpoint for file: " + file);
 
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
 
 	public void logout() {
 		actionStatuses.remove(dn);
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
 
 		// if (!mountPointName.startsWith("/")) {
 		// mountPointName = "/" + mountPointName;
 		// }
 
 		myLogger.debug("Checking mountpoints for duplicates.");
 		for (final MountPoint mp : getAllMountPoints()) {
 			if (mountPointName.equals(mp.getAlias())) {
 				throw new RemoteFileSystemException(
 						"There is already a filesystem mounted on:"
 								+ mountPointName);
 			}
 		}
 
 		MountPoint new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 				mountPointName, site);
 		try {
 			// FileSystem fileSystem = createFilesystem(new_mp.getRootUrl(),
 			// new_mp.getFqan());
 
 			final FileSystem fileSystem = threadLocalFsManager.getFileSystem(
 					new_mp.getRootUrl(), new_mp.getFqan());
 			myLogger.debug("Connected to file system.");
 			if (useHomeDirectory) {
 				myLogger.debug("Using home directory: "
 						+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 								.substring(1));
 				uri = fileSystem.getRoot().getName().getRootURI()
 						+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 								.substring(1);
 				// if vo user, use $VOHOME/<DN> as homedirectory
 				if (cred.getFqan() != null) {
 					uri = uri + File.separator + get_vo_dn_path(cred.getDn());
 					fileSystem.resolveFile(
 							((String) fileSystem.getAttribute("HOME_DIRECTORY")
 									+ File.separator + cred.getDn()
 									.replace("=", "_").replace(",", "_")
 									.replace(" ", "_"))).createFolder();
 				}
 				new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 						mountPointName, site);
 			}
 
 			if (!mountPoints.contains(new_mp)) {
 				allMountPoints = null;
 				mountPoints.add(new_mp);
 			}
 
 			userdao.saveOrUpdate(this);
 			return new_mp;
 		} catch (final FileSystemException e) {
 			throw new RemoteFileSystemException("Error while trying to mount: "
 					+ mountPointName);
 		}
 
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
 	}
 
 	public void resetMountPoints() {
 		allMountPoints = null;
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
 
 	// public void addProperty(String key, String value) {
 	// List<String> list = userProperties.get(key);
 	// if ( list == null ) {
 	// list = new LinkedList<String>();
 	// }
 	// list.add(value);
 	// }
 
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
 				allMountPoints = null;
 				return;
 			}
 		}
 		userdao.saveOrUpdate(this);
 	}
 
 }
