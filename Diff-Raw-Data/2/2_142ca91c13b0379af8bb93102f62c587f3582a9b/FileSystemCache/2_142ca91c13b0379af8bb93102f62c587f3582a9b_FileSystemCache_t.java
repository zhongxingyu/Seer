 package grisu.backend.model;
 
 import grisu.model.MountPoint;
 import grisu.settings.ServerPropertiesManager;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystem;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileSystemOptions;
 import org.apache.commons.vfs.impl.DefaultFileSystemManager;
 import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
 import org.apache.log4j.Logger;
 
 import uk.ac.dl.escience.vfs.util.VFSUtil;
 
 public class FileSystemCache {
 
 	private static AtomicInteger COUNTER = new AtomicInteger();
 
 	private static Logger myLogger = Logger.getLogger(FileSystemCache.class
 			.getName());
 
 	private Map<MountPoint, FileSystem> cachedFilesystems = new HashMap<MountPoint, FileSystem>();
 	private DefaultFileSystemManager fsm = null;
 	private final User user;
 
 	private final String id;
 
 	public FileSystemCache(User user) {
 		id = "FILESYSTEM_CACHE_" + UUID.randomUUID().toString();
 		int i = COUNTER.addAndGet(1);
 		// X.p("Opening filesystemmanager: " + i);
 		this.user = user;
 		try {
 			// String tmp = System.getProperty("java.io.tmpdir");
 			// if (StringUtils.isBlank(tmp)) {
 			// tmp = "/tmp/grisu-fs-tmp";
 			// } else {
 			// myLogger.debug("Using " + tmp
 			// + "/grisu-fs-tmp for temporary directory...");
 			//
 			// }
 
 			myLogger.debug(user.getDn() + ": Creating FS manager for user...");
 
 			fsm = VFSUtil.createNewFsManager(false, false, true, true, true,
 					true, true, "/tmp");
 		} catch (final FileSystemException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void addFileSystem(MountPoint mp, FileSystem fs) {
 		cachedFilesystems.put(mp, fs);
 	}
 
 	public void close() {
 		cachedFilesystems = new HashMap<MountPoint, FileSystem>();
 
		if (!ServerPropertiesManager.closeFileSystemsInBackground()) {
 			myLogger.debug(id
 					+ "Closing filesystem. Currently open filesystems: "
 					+ COUNTER);
 			fsm.close();
 			int i = COUNTER.decrementAndGet();
 			myLogger.debug(id
 					+ "Filesystemm closed. Remaining open filesystems: " + i);
 		} else {
 
 			Thread t = new Thread() {
 				@Override
 				public void run() {
 					myLogger.debug(id
 							+ "Closing filesystem. Currently open filesystems: "
 							+ COUNTER);
 					fsm.close();
 					int i = COUNTER.decrementAndGet();
 					myLogger.debug(id
 							+ "Filesystemm closed. Remaining open filesystems: "
 							+ i);
 				}
 			};
 			t.setName("FS_CLOSE_" + new Date().getTime());
 
 			t.start();
 		}
 	}
 
 	private FileSystem createFileSystem(String rootUrl,
 			ProxyCredential credToUse) throws FileSystemException {
 
 		final FileSystemOptions opts = new FileSystemOptions();
 
 		if (rootUrl.startsWith("gsiftp")) {
 			// myLogger.debug("Url \"" + rootUrl
 			// + "\" is gsiftp url, using gridftpfilesystembuilder...");
 
 			final GridFtpFileSystemConfigBuilder builder = GridFtpFileSystemConfigBuilder
 					.getInstance();
 			builder.setGSSCredential(opts, credToUse.getGssCredential());
 			builder.setTimeout(opts,
 					ServerPropertiesManager.getFileSystemConnectTimeout());
 			// builder.setUserDirIsRoot(opts, true);
 		}
 
 		FileObject fileRoot;
 		try {
 			fileRoot = fsm.resolveFile(rootUrl, opts);
 		} catch (final FileSystemException e) {
 			myLogger.error("Can't connect to filesystem: " + rootUrl
 					+ " using VO: " + credToUse.getFqan());
 			throw new FileSystemException("Can't connect to filesystem "
 					+ rootUrl
 					+ ": " + e.getLocalizedMessage(), e);
 		}
 
 		FileSystem fileBase = null;
 		fileBase = fileRoot.getFileSystem();
 
 		return fileBase;
 
 	}
 
 	public FileSystem getFileSystem(MountPoint mp) {
 		return cachedFilesystems.get(mp);
 	}
 
 	public FileSystem getFileSystem(final String rootUrl, String fqan)
 			throws FileSystemException {
 
 		synchronized (rootUrl) {
 			ProxyCredential credToUse = null;
 
 			MountPoint temp = null;
 			try {
 				temp = user.getResponsibleMountpointForAbsoluteFile(rootUrl);
 			} catch (final IllegalStateException e) {
 				// myLogger.info(e);
 			}
 			if ((fqan == null) && (temp != null) && (temp.getFqan() != null)) {
 				fqan = temp.getFqan();
 			}
 			// get the right credential for this mountpoint
 			if (fqan != null) {
 
 				credToUse = user.getCred(fqan);
 
 			} else {
 				credToUse = user.getCred();
 			}
 
 			FileSystem fileBase = null;
 
 			if (temp == null) {
 				// means we have to figure out how to connect to this. I.e.
 				// which fqan to use...
 				// throw new FileSystemException(
 				// "Could not find mountpoint for url " + rootUrl);
 
 				// creating a filesystem...
 				myLogger.debug("Creating filesystem without mountpoint...");
 				return createFileSystem(rootUrl, credToUse);
 
 			} else {
 				// great, we can re-use this filesystem
 				if (getFileSystem(temp) == null) {
 
 					fileBase = createFileSystem(temp.getRootUrl(), credToUse);
 
 					if (temp != null) {
 						addFileSystem(temp, fileBase);
 					}
 				} else {
 					fileBase = getFileSystem(temp);
 				}
 			}
 
 			return fileBase;
 		}
 
 	}
 
 	public DefaultFileSystemManager getFileSystemManager() {
 		return fsm;
 	}
 
 	public Map<MountPoint, FileSystem> getFileSystems() {
 		return cachedFilesystems;
 	}
 
 }
