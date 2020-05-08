 package grisu.backend.model;
 
 import grisu.model.MountPoint;
 import grisu.settings.ServerPropertiesManager;
 import grith.jgrith.cred.AbstractCred;
 import grith.jgrith.cred.Cred;
 
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
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.dl.escience.vfs.util.VFSUtil;
 
 public class FileSystemCache {
 
 	private static AtomicInteger COUNTER = new AtomicInteger();
 
 	private static Logger myLogger = LoggerFactory
 			.getLogger(FileSystemCache.class.getName());
 
 	private Map<MountPoint, FileSystem> cachedFilesystems = new HashMap<MountPoint, FileSystem>();
 	private DefaultFileSystemManager fsm = null;
 	private final User user;
 
 	private final String id;
 
 	public FileSystemCache(User user) {
 		id = "FILESYSTEM_CACHE_" + UUID.randomUUID().toString() + ": ";
 		final int i = COUNTER.addAndGet(1);
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
 			final int i = COUNTER.decrementAndGet();
 			myLogger.debug(id
 					+ "Filesystemm closed. Remaining open filesystems: " + i);
 		} else {
 
 			final Thread t = new Thread() {
 				@Override
 				public void run() {
 					myLogger.debug(id
 							+ "Closing filesystem. Currently open filesystems: "
 							+ COUNTER);
 					fsm.close();
 					final int i = COUNTER.decrementAndGet();
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
 			Cred credToUse)
 					throws FileSystemException {
 
 		final FileSystemOptions opts = new FileSystemOptions();
 		FileObject fileRoot;
 		try {
 
 			if (rootUrl.startsWith("gsiftp")) {
 				// myLogger.debug("Url \"" + rootUrl
 				// + "\" is gsiftp url, using gridftpfilesystembuilder...");
 
 				final GridFtpFileSystemConfigBuilder builder = GridFtpFileSystemConfigBuilder
 						.getInstance();
 				builder.setGSSCredential(opts, credToUse.getGSSCredential());
 				builder.setTimeout(opts,
 						ServerPropertiesManager.getFileSystemConnectTimeout());
 				// builder.setUserDirIsRoot(opts, true);
 			}
 
 			fileRoot = fsm.resolveFile(rootUrl, opts);
 		} catch (final FileSystemException e) {
 			myLogger.error("Can't connect to filesystem: " + rootUrl
 					+ " using VO: " + credToUse.getFqan());
 			throw new FileSystemException("Can't connect to filesystem "
 					+ rootUrl + ": " + e.getLocalizedMessage(), e);
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
 			Cred credToUse = null;
 
 			MountPoint temp = null;
 			try {
 				String tmp = fqan;
 				System.out.println(tmp);
 				temp = user.getResponsibleMountpointForAbsoluteFile(rootUrl);
 			} catch (final IllegalStateException e) {
 				 e.printStackTrace();
				 myLogger.debug("Can't get mountpoint.",e);
 			}
 			if ((fqan == null) && (temp != null) && (temp.getFqan() != null)) {
 				fqan = temp.getFqan();
 			}
 			// get the right credential for this mountpoint
 			if (fqan != null) {
 
 				credToUse = user.getCredential(fqan);
 
 			} else {
 				credToUse = user.getCredential();
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
