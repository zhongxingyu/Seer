 package org.vpac.grisu.fs.model;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.Transient;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystem;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileSystemOptions;
 import org.apache.commons.vfs.impl.DefaultFileSystemManager;
 import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.utils.CertHelpers;
 import org.vpac.grisu.credential.model.ProxyCredential;
 import org.vpac.grisu.js.model.Job;
 import org.vpac.security.light.voms.VO;
 import org.vpac.security.light.voms.VOManagement.VOManagement;
 import org.vpac.security.light.vomsProxy.VomsException;
 
 import uk.ac.dl.escience.vfs.util.VFSUtil;
 
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
 public class User {
 
 	public List<DefaultFileSystemManager> allFileSystems = Collections
 			.synchronizedList(new LinkedList<DefaultFileSystemManager>());
 
 	// to get on filesystemmanager per thread
 	private class ThreadLocalFsManager extends ThreadLocal {
 		public Object initialValue() {
 			try {
 				myLogger.debug("Creating new FS Manager.");
 				DefaultFileSystemManager temp = VFSUtil.createNewFsManager(
 						false, false, true, true, true, true, true, null);
 				allFileSystems.add(temp);
 				return temp;
 			} catch (FileSystemException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 
 		}
 
 		public DefaultFileSystemManager getFsManager() {
 			return (DefaultFileSystemManager) super.get();
 		}
 	}
 
 	private ThreadLocalFsManager threadLocalFsManager = new ThreadLocalFsManager();
 
 	private static Logger myLogger = Logger.getLogger(User.class.getName());
 
 	public static String get_vo_dn_path(final String dn) {
 		return dn.replace("=", "_").replace(",", "_").replace(" ", "_");
 	}
 
 	private Long id = null;
 
 	// the (default) credential to contact gridftp file shares
 	private ProxyCredential cred = null;
 
 	// managers the virtual filesystem manager
 	private DefaultFileSystemManager fsmanager = null;
 
 	// the (default) credentials dn
 	private String dn = null;
 
 	// // persistent properties
 	// // not used yet
 	private List<FileTransfer> fileTransfers = new ArrayList<FileTransfer>();
 
 	// // not used yet
 	private List<FileReservation> fileReservations = new ArrayList<FileReservation>();
 
 	// the mountpoints of a user
 	private Set<MountPoint> mountPoints = new HashSet<MountPoint>();
 	private Set<MountPoint> mountPointsAutoMounted = new HashSet<MountPoint>();
 	private Set<MountPoint> allMountPoints = null;
 
 	// filesystem connections are cached so that we don't need to connect again
 	// everytime we access one
 	// private Map<MountPoint, FileSystem> cachedFilesystemConnections = new
 	// HashMap<MountPoint, FileSystem>();
 	// credentials are chache so we don't have to contact myproxy/voms anytime
 	// we want to make a transaction
 	private Map<String, ProxyCredential> cachedCredentials = new HashMap<String, ProxyCredential>();
 
 	// All fqans of the user
 	private Map<String, String> fqans = new HashMap<String, String>();
 	private boolean fqansFilled = false;
 	// private Map<String, String> fqans = null;
 
 	private Map<String, List<String>> userProperties = new HashMap<String, List<String>>();
 
 	// private Map<String, JobSubmissionObjectImpl> jobTemplates = new
 	// HashMap<String, JobSubmissionObjectImpl>();
 
 	// for hibernate
 	public User() {
 	}
 
 	public void closeAllFileSystems() {
 		for (DefaultFileSystemManager fsm : allFileSystems) {
 			fsm.close();
 		}
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
 
 	/**
 	 * Constructs a user using and associates a (default) credential with it.
 	 * 
 	 * @param cred
 	 *            the credential
 	 * @throws FileSystemException
 	 *             if the users default filesystems can't be mounted
 	 */
 	public User(final ProxyCredential cred) {
 		this.dn = cred.getDn();
 		this.cred = cred;
 	}
 
 	/**
 	 * Contacts every configured VOMS server and gets (and sets) the user's
 	 * fqans for this server.
 	 */
 	public final void fillFqans() {
 
 		myLogger.debug("Checking credential");
 		if (cred.isValid()) {
 			fqans = VOManagement.getAllFqans(cred.getGssCredential());
 		}
 
 		fqansFilled = true;
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param transfer
 	 */
 	public void addFileTransfer(FileTransfer transfer) {
 		fileTransfers.add(transfer);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param transfer
 	 */
 	public void removeFileTransfer(FileTransfer transfer) {
 		fileTransfers.remove(transfer);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param reservation
 	 */
 	public void addFileReservation(FileReservation reservation) {
 		fileReservations.add(reservation);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param reservation
 	 */
 	public void removeFileReservation(FileReservation reservation) {
 		fileReservations.remove(reservation);
 	}
 
 	/**
 	 * Returns the users dn.
 	 * 
 	 * @return the dn
 	 */
 	public String getDn() {
 		return dn;
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		return 29 * dn.hashCode();
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
 
 	private Long getId() {
 		return id;
 	}
 
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Set's additional mountpoints that the user did not explicitly mount
 	 * manually.
 	 * 
 	 * @param amps
 	 *            the mountpoints to add (for this session)
 	 */
 	public void setAutoMountedMountPoints(final Set<MountPoint> amps) {
 		allMountPoints = null;
 		this.mountPointsAutoMounted = amps;
 	}
 
 	/**
 	 * Returns all mountpoints (including automounted ones for this session.
 	 * 
 	 * @return all mountpoints for this session
 	 */
 	@Transient
 	public Set<MountPoint> getAllMountPoints() {
 		if (allMountPoints == null) {
 			allMountPoints = new TreeSet<MountPoint>();
 			// first the automounted ones because the manually ones are more
 			// important
 			allMountPoints.addAll(mountPointsAutoMounted);
 			allMountPoints.addAll(getMountPoints());
 		}
 		return allMountPoints;
 	}
 
 	// for hibernate
 	private Set<MountPoint> getMountPoints() {
 		return mountPoints;
 	}
 
 	// for hibernate
 	private void setMountPoints(final Set<MountPoint> mountPoints) {
 		this.mountPoints = mountPoints;
 	}
 
 	public String getFileSystemHomeDirectory(String filesystemRoot, String fqan)
 			throws FileSystemException {
 
 		FileSystem fileSystem = createFilesystem(filesystemRoot, fqan);
 		myLogger.debug("Connected to file system.");
 
 		myLogger.debug("Using home directory: "
 				+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 						.substring(1));
 
 		String home = (String) fileSystem.getAttribute("HOME_DIRECTORY");
 		String uri = fileSystem.getRoot().getName().getRootURI()
 				+ home.substring(1);
 
 		return uri;
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
 			final ProxyCredential cred, final boolean useHomeDirectory)
 			throws RemoteFileSystemException {
 
 		// if (!mountPointName.startsWith("/")) {
 		// mountPointName = "/" + mountPointName;
 		// }
 
 		myLogger.debug("Checking mountpoints for duplicates.");
 		for (MountPoint mp : getAllMountPoints()) {
 			if (mountPointName.equals(mp.getMountpoint())) {
 				throw new RemoteFileSystemException(
 						"There is already a filesystem mounted on:"
 								+ mountPointName);
 			}
 		}
 
 		MountPoint new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 				mountPointName);
 		try {
 			FileSystem fileSystem = createFilesystem(new_mp.getRootUrl(),
 					new_mp.getFqan());
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
 									+ File.separator + cred.getDn().replace(
 									"=", "_").replace(",", "_").replace(" ",
 									"_"))).createFolder();
 				}
 				new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 						mountPointName);
 			}
 
 			if (!mountPoints.contains(new_mp)) {
 				allMountPoints = null;
 				mountPoints.add(new_mp);
 			}
 			return new_mp;
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Error while trying to mount: "
 					+ mountPointName);
 		}
 
 	}
 
 	// /**
 	// * Connects to the filesystem where the file lives on.
 	// *
 	// * @param file
 	// * the file you want to access (in a later step)
 	// * @param cred
 	// * @return
 	// * @throws FileSystemException
 	// * @throws VomsException
 	// */
 	// private FileSystem connectToFileSystem(final MountPoint mp)
 	// throws FileSystemException {
 	//
 	// // check whether a filesystem for this mountpoint is already cached
 	// if (cachedFilesystemConnections.containsKey(mp)) {
 	// myLogger.debug("Using already cached filesystem for mountpoint: "
 	// + mp.getAlias());
 	// return this.cachedFilesystemConnections.get(mp);
 	// }
 	//
 	// ProxyCredential credToUse = null;
 	//
 	// // get the right credential for this mountpoint
 	// if (mp.getFqan() != null) {
 	//
 	// credToUse = cachedCredentials.get(mp.getFqan());
 	// if (credToUse == null || !credToUse.isValid()) {
 	//
 	// // put a new credential in the cache
 	// VO vo = VOManagement.getVO(getFqans().get(mp.getFqan()));
 	// credToUse = CertHelpers.getVOProxyCredential(vo, mp.getFqan(),
 	// getCred());
 	// cachedCredentials.put(mp.getFqan(), credToUse);
 	// } else {
 	// credToUse = cachedCredentials.get(mp.getFqan());
 	// }
 	//
 	// } else {
 	// credToUse = getCred();
 	// }
 	//
 	// FileSystemOptions opts = new FileSystemOptions();
 	//
 	// if (mp.getRootUrl().startsWith("gsiftp")) {
 	// GridFtpFileSystemConfigBuilder builder = GridFtpFileSystemConfigBuilder
 	// .getInstance();
 	// builder.setGSSCredential(opts, credToUse.getGssCredential());
 	// // builder.setUserDirIsRoot(opts, true);
 	// }
 	//
 	// FileObject fileRoot = getFsManager().resolveFile(mp.getRootUrl(), opts);
 	//
 	// FileSystem fileBase = fileRoot.getFileSystem();
 	//
 	// this.cachedFilesystemConnections.put(mp, fileBase);
 	//
 	// return fileBase;
 	// }
 
 	/**
 	 * Connects to the filesystem where the file lives on.
 	 * 
 	 * @param file
 	 *            the file you want to access (in a later step)
 	 * @param cred
 	 * @return
 	 * @throws FileSystemException
 	 * @throws VomsException
 	 */
 	private synchronized FileSystem createFilesystem(final String rootUrl,
 			final String fqan) throws FileSystemException {
 
 		// // check whether a filesystem for this mountpoint is already cached
 		// if (cachedFilesystemConnections.containsKey(mp)) {
 		// myLogger.debug("Using already cached filesystem for mountpoint: "
 		// + mp.getAlias());
 		// return this.cachedFilesystemConnections.get(mp);
 		// }
 
 		ProxyCredential credToUse = null;
 
 		// get the right credential for this mountpoint
 		if (fqan != null) {
 
 			credToUse = cachedCredentials.get(fqan);
 			if (credToUse == null || !credToUse.isValid()) {
 
 				// put a new credential in the cache
 				VO vo = VOManagement.getVO(getFqans().get(fqan));
 				credToUse = CertHelpers.getVOProxyCredential(vo, fqan,
 						getCred());
 				cachedCredentials.put(fqan, credToUse);
 			} else {
 				credToUse = cachedCredentials.get(fqan);
 			}
 
 		} else {
 			credToUse = getCred();
 		}
 
 		FileSystemOptions opts = new FileSystemOptions();
 
		// just to make sure
		getFsManager();
		
 		if (rootUrl.startsWith("gsiftp")) {
 			GridFtpFileSystemConfigBuilder builder = GridFtpFileSystemConfigBuilder
 					.getInstance();
 			builder.setGSSCredential(opts, credToUse.getGssCredential());
 			// builder.setUserDirIsRoot(opts, true);
 		}
 
 		FileObject fileRoot = getFsManager().resolveFile(rootUrl, opts);
 
 		FileSystem fileBase = fileRoot.getFileSystem();
 
 		// this.cachedFilesystemConnections.put(mp, fileBase);
 
 		return fileBase;
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
 			final boolean useHomeDirectory) throws RemoteFileSystemException {
 
 		return mountFileSystem(root, name, getCred(), useHomeDirectory);
 	}
 
 	public MountPoint mountFileSystem(final String root, final String name,
 			final String fqan, final boolean useHomeDirectory)
 			throws RemoteFileSystemException {
 
 		if (fqan == null || "None".equals(fqan)) {
 			return mountFileSystem(root, name, useHomeDirectory);
 		} else {
 
 			Map<String, String> temp = getFqans();
 			VO vo = VOManagement.getVO(temp.get(fqan));
 
 			ProxyCredential vomsProxyCred = CertHelpers.getVOProxyCredential(
 					vo, fqan, getCred());
 
 			return mountFileSystem(root, name, vomsProxyCred, useHomeDirectory);
 		}
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
 
 		for (MountPoint mp : mountPoints) {
 			if (mp.getMountpoint().equals(mountPointName)) {
 				mountPoints.remove(mp);
 				allMountPoints = null;
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Used internally to mount filesystems.
 	 * 
 	 * @return the filesystem manager of the user
 	 * @throws FileSystemException
 	 *             if something goes wrong
 	 */
 	@Transient
 	private DefaultFileSystemManager getFsManager() throws FileSystemException {
 		// if (fsmanager == null) {
 		//
 		// fsmanager = VFSUtil.createNewFsManager(false, false, true, true,
 		// true, true, true, null);
 		//
 		// }
 		// return fsmanager;
 		// System.out.println("Creating new FS Manager.");
 		// return VFSUtil.createNewFsManager(false, false, true, true, true,
 		// true, true, null);
 		return threadLocalFsManager.getFsManager();
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
 		return aquireFile(file, getCred());
 	}
 
 	/**
 	 * Resolves the provided filename into a FileObject. If the filename starts
 	 * with "/" a file on one of the "mounted" filesystems is looked up. Else it
 	 * has to start with the name of a (supported) protocol (like: gsiftp:///).
 	 * 
 	 * @param file
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
 	public FileObject aquireFile(final String file,
 			final ProxyCredential plainCred) throws RemoteFileSystemException {
 
 		String file_to_aquire = null;
 		// ProxyCredential credToUse = null;
 		MountPoint mp = null;
 
 		if (file.startsWith("tmp:") || file.startsWith("ram:")) {
 			try {
 				return getFsManager().resolveFile(file);
 			} catch (FileSystemException e) {
 				throw new RemoteFileSystemException(
 						"Could not access file on local temp filesystem: "
 								+ e.getLocalizedMessage());
 			}
 		} else if (file.startsWith("/")) {
 			// means file on "mounted" filesystem
 
 			mp = getResponsibleMountpointForUserSpaceFile(file);
 
 			file_to_aquire = mp.replaceMountpointWithAbsoluteUrl(file);
 
 			if (file_to_aquire == null) {
 				throw new RemoteFileSystemException(
 						"File path is not on any of the mountpoints for file: "
 								+ file);
 			}
 		} else {
 			// means absolute url
 			file_to_aquire = file;
 			mp = getResponsibleMountpointForAbsoluteFile(file_to_aquire);
 		}
 
 		if (mp == null) {
 			throw new RemoteFileSystemException(
 					"Could not find mountpoint for file with url: "
 							+ file_to_aquire);
 		}
 
 		// // get the right credential for this mountpoint
 		// if (mp.getFqan() != null) {
 		//
 		// try {
 		// ProxyCredential credential = cachedCredentials.get(mp.getFqan());
 		// if ( credential == null
 		// || ! credential.isValid()
 		// || credential.getGssCredential().getRemainingLifetime() < 120 ) {
 		//					
 		// // put a new credential in the cache
 		// VO vo = VOManagement.getVO(getFqans().get(mp.getFqan()));
 		// credToUse = ProxyCredentialHelpers.getVOProxyCredential(vo, mp
 		// .getFqan(), plainCred);
 		// cachedCredentials.put(mp.getFqan(), credToUse);
 		// } else {
 		// credToUse = cachedCredentials.get(mp.getFqan());
 		// }
 		//				
 		// } catch (GSSException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// VO vo = VOManagement.getVO(getFqans().get(mp.getFqan()));
 		// credToUse = ProxyCredentialHelpers.getVOProxyCredential(vo, mp
 		// .getFqan(), plainCred);
 		// cachedCredentials.put(mp.getFqan(), credToUse);
 		// }
 		// } else {
 		// credToUse = plainCred;
 		// }
 
 		FileObject fileObject = null;
 		try {
 			FileSystem root = null;
 			// check whether the file root is in the cache so we don't have to
 			// connect again.
 			// TODO check whether vfs does that already
 			// markus
 			// if (!cachedFilesystemConnections.containsKey(mp.getRootUrl())) {
 			// root = this.connectToFileSystem(mp);
 			root = this.createFilesystem(mp.getRootUrl(), mp.getFqan());
 			// } else {
 			// root = this.cachedFilesystemConnections.get(mp.getRootUrl());
 			// }
 			// String actualFile =
 			// file_to_aquire.substring(root.getName().getURI().length());
 
 			String fileUri = root.getRootName().getURI();
 
 			try {
 				URI uri = new URI(file_to_aquire);
 				file_to_aquire = uri.toString();
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String tempUriString = file_to_aquire.replace(":2811", "")
 					.substring(fileUri.length());
 			fileObject = root.resolveFile(tempUriString);
 			// fileObject = root.resolveFile(file_to_aquire);
 
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file: "
 					+ file + ": " + e.getMessage());
 		}
 
 		return fileObject;
 
 	}
 
 	// /**
 	// * @deprecated
 	// * @param site
 	// * @param fqan
 	// * @return
 	// */
 	// public MountPoint getFirstResponsibleMountPointForSiteAndFqan(String
 	// site, String fqan) {
 	//		
 	// myLogger.debug("Looking for mountpoint for site: "+site+" and fqan: "+fqan);
 	//		
 	// if ( site == null ) return null;
 	//		
 	// for (MountPoint mp : getAllMountPoints() ) {
 	// String mpSite = MountPointManager.getSite(mp.getRootUrl());
 	// if ( mpSite == null )
 	// continue;
 	//			
 	// if ( site.equals(mpSite) && ((fqan == null && mp.getFqan() == null) ||
 	// (fqan != null && fqan.equals(mp.getFqan()))) ) {
 	// myLogger.debug("Found mountpoint: "+mp.getMountpoint());
 	// return mp;
 	// }
 	// }
 	// return null;
 	//		
 	// }
 	@Transient
 	public MountPoint getFirstResponsibleMountPointForHostAndFqan(
 			final String host_or_url, final String fqan) {
 
 		myLogger.debug("Looking for mountpoint for site: " + host_or_url
 				+ " and fqan: " + fqan);
 
 		if (host_or_url == null) {
 			return null;
 		}
 
 		String protocol = null;
 		String hostname = null;
 		try {
 			URI uri = new URI(host_or_url);
 			protocol = uri.getScheme();
 			hostname = uri.getHost();
 
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for (MountPoint mp : getAllMountPoints()) {
 			if ((mp.getFqan() == null && fqan == null)
 					|| (mp.getFqan() != null && mp.getFqan().equals(fqan))) {
 				if (protocol != null && hostname != null) {
 					if (mp.getRootUrl().indexOf(protocol) != -1
 							&& mp.getRootUrl().indexOf(hostname) != -1) {
 						return mp;
 					}
 				} else {
 					if (mp.getRootUrl().indexOf(host_or_url) != -1) {
 						return mp;
 					}
 				}
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
 
 		for (MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForUserSpaceFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether the filesystem of any of the users' mountpoints contains
 	 * the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint of null if no filesystem contains this file
 	 */
 	public MountPoint getResponsibleMountpointForAbsoluteFile(final String file) {
 
 		String new_file = null;
 		myLogger.debug("Finding mountpoint for file: " + file);
 
 		for (MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForAbsoluteFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Translates an "user-space" file url into an absolute file url.
 	 * 
 	 * @param file
 	 *            an "user-space" file url (/ngdata.vpac.org/test.txt)
 	 * @return the absolute file url
 	 *         (gsiftp://ngdata.vpac.org/home/san04/markus/test.txt) or null if
 	 *         the file is not within the user's filespace
 	 */
 	public String returnAbsoluteUrl(final String file) {
 		MountPoint mp = getResponsibleMountpointForUserSpaceFile(file);
 		if (mp == null) {
 			return null;
 		} else if (file.startsWith("gsiftp:")) {
 			return file;
 		} else {
 			return mp.replaceMountpointWithAbsoluteUrl(file);
 		}
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
 		MountPoint mp = getResponsibleMountpointForAbsoluteFile(file);
 		return mp.replaceAbsoluteRootUrlWithMountPoint(file);
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
 
 	public void cleanCache() {
 		// TODO disconnect filesystems somehow?
 		// cachedFilesystemConnections = new HashMap<MountPoint, FileSystem>();
 		// // does this affect existing filesystem connection
 		// for ( ProxyCredential proxy : cachedCredentials.values() ) {
 		// proxy.destroy();
 		// }
 		cachedCredentials = new HashMap<String, ProxyCredential>();
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void addFqan(final String fqan, final String vo) {
 		fqans.put(fqan, vo);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void removeFqan(final String fqan) {
 		fqans.remove(fqan);
 	}
 
 	/**
 	 * Getter for the users' fqans.
 	 * 
 	 * @return all fqans as map with the fqan as key and the vo as value
 	 */
 	@Transient
 	public Map<String, String> getFqans() {
 		if (fqans == null || !fqansFilled) {
 			fillFqans();
 
 		}
 		return fqans;
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
 
 	public void addProperty(String key, String value) {
 		List<String> list = userProperties.get(key);
 		if (list == null) {
 			list = new LinkedList<String>();
 		}
 		list.add(value);
 	}
 
 	public void removeProperty(String key) {
 		userProperties.remove(key);
 	}
 
 	/**
 	 * Gets a map of this users properties. These properties can be used to
 	 * store anything you can think of. Usful for history and such.
 	 * 
 	 * @return the users' properties
 	 */
 	public Map<String, List<String>> getUserProperties() {
 		return userProperties;
 	}
 
 	private void setUserProperties(Map<String, List<String>> userProperties) {
 		this.userProperties = userProperties;
 	}
 
 	// @CollectionOfElements
 	// public Map<String, JobSubmissionObjectImpl> getJobTemplates() {
 	// return jobTemplates;
 	// }
 	//
 	// public void setJobTemplates(
 	// final Map<String, JobSubmissionObjectImpl> jobTemplates) {
 	// this.jobTemplates = jobTemplates;
 	// }
 
 }
