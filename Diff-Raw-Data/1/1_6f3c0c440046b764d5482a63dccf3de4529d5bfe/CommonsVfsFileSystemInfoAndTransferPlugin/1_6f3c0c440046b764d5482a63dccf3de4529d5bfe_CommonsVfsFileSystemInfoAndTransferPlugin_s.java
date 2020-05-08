 package grisu.backend.model.fs;
 
 import grisu.backend.model.FileSystemCache;
 import grisu.backend.model.ProxyCredential;
 import grisu.backend.model.RemoteFileTransferObject;
 import grisu.backend.model.User;
 import grisu.backend.utils.FileContentDataSourceConnector;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.GridFile;
 import grisu.settings.ServerPropertiesManager;
 import grith.jgrith.vomsProxy.VomsException;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.vfs.AllFileSelector;
 import org.apache.commons.vfs.FileContent;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystem;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.apache.log4j.Logger;
 
 public class CommonsVfsFileSystemInfoAndTransferPlugin implements
 FileSystemInfoPlugin, FileTransferPlugin {
 
 	private static Logger myLogger = Logger
 			.getLogger(CommonsVfsFileSystemInfoAndTransferPlugin.class
 					.getName());
 
 	private final User user;
 
 	// private final Map<Thread, FileSystemCache> filesystems = Collections
 	// .synchronizedMap(new HashMap<Thread, FileSystemCache>());
 
 	public CommonsVfsFileSystemInfoAndTransferPlugin(User user) {
 		this.user = user;
 	}
 
 	public FileObject aquireFile(FileSystemCache fsCache, String url)
 			throws RemoteFileSystemException {
 		return aquireFile(fsCache, url, null);
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
 	public FileObject aquireFile(FileSystemCache fsCache, String url,
 			final String fqan) throws RemoteFileSystemException {
 
 		if (Thread.interrupted()) {
 			Thread.currentThread().interrupt();
 			throw new RemoteFileSystemException("Accessing file interrupted.");
 		}
 
 		if (url.startsWith("tmp:") || url.startsWith("ram:")) {
 			try {
 				return fsCache.getFileSystem(url, null).resolveFile(url);
 			} catch (final FileSystemException e) {
 				throw new RemoteFileSystemException(
 						"Could not access file on local temp filesystem: "
 								+ e.getLocalizedMessage());
 			}
 		}
 
 		FileObject fileObject = null;
 		try {
 			FileSystem root = null;
 
 			// root = this.createFilesystem(mp.getRootUrl(), mp.getFqan());
 			root = fsCache.getFileSystem(url, fqan);
 
 			final String fileUri = root.getRootName().getURI();
 
 			try {
 				final URI uri = new URI(url);
 				url = uri.toString();
 			} catch (final URISyntaxException e) {
 				myLogger.error(e);
 				throw new RemoteFileSystemException(
 						"Could not get uri for file " + url);
 			}
 
 			final String tempUriString = url.replace(":2811", "").substring(
 					fileUri.length());
 			fileObject = root.resolveFile(tempUriString);
 			// fileObject = root.resolveFile(file_to_aquire);
 
 		} catch (final FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file: " + url
 					+ ": " + e.getMessage());
 		}
 
 		return fileObject;
 
 	}
 
 	private void closeFile(FileObject f){
 		try {
 			f.close();
 		} catch (FileSystemException ex){
 			myLogger.warn(ex.getLocalizedMessage());
 		}
 	}
 
 	public void closeInputStream(String file) {
 
 	}
 
 	public void closeOutputStream(String file) {
 
 	}
 
 	public RemoteFileTransferObject copySingleFile(String source,
 			String target, boolean overwrite) throws RemoteFileSystemException {
 
 		final FileObject source_file;
 		final FileObject target_file;
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		source_file = aquireFile(fsCache, source, null);
 		target_file = aquireFile(fsCache, target, null);
 
 		final RemoteFileTransferObject fileTransfer = new CommonsVfsRemoteFileTransferObject(
 				fsCache, source_file, target_file, overwrite);
 
 		myLogger.info("Creating fileTransfer object for source: "
 				+ source_file.getName() + " and target: "
 				+ target_file.toString());
 
 		return fileTransfer;
 
 	}
 
 	private String createCacheKey(MountPoint mp) {
 		return user.getDn()+"_"+mp.getRootUrl()+"_"+mp.getFqan();
 	}
 
 	private String createCacheKey(String rootUrl, String fqan) {
 		return user.getDn() + "_" + rootUrl + "_" + fqan;
 	}
 
 	public boolean createFolder(FileObject folder)
 			throws RemoteFileSystemException {
 
 		try {
 
 			ArrayList<FileObject> temp = new ArrayList<FileObject>();
 			FileObject last = folder;
 			while (!last.exists()) {
 				temp.add(last);
 				last = last.getParent();
 			}
 
 			Collections.reverse(temp);
 			for (FileObject f : temp) {
 				f.createFolder();
 			}
 
 			if (folder.exists()) {
 				return true;
 			} else {
 				return false;
 			}
 
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException(e);
 		}
 
 	}
 
 	// private FileSystemCache getFileSystemCache() {
 	//
 	// Thread current = Thread.currentThread();
 	// if (filesystems.get(current) == null) {
 	// FileSystemCache fs = new FileSystemCache(user);
 	// filesystems.put(current, fs);
 	// X.p("Filesystemcache size: " + filesystems.size());
 	// }
 	//
 	// return filesystems.get(current);
 	//
 	// }
 
 	public boolean createFolder(String url) throws RemoteFileSystemException {
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject folder = null;
 
 		try {
 			folder = aquireFile(fsCache, url, null);
 			return createFolder(folder);
 		} finally {
 			closeFile(folder);
 			fsCache.close();
 		}
 	}
 
 	public void deleteFile(final String file) throws RemoteFileSystemException {
 
 
 		int retries = ServerPropertiesManager.getFileDeleteRetries();
 		try {
 			FileSystemException fse = null;
 			for (int i = 0; i < retries; i++) {
 				FileSystemCache fsCache = new FileSystemCache(user);
 				FileObject fileObject = null;
 				try {
 					fileObject = aquireFile(fsCache, file);
 					if (fileObject.exists()) {
 						myLogger.debug("Deleting file/folder (" + (i+1) + ". try):"
 								+ file);
 						int no = fileObject.delete(new AllFileSelector());
 						myLogger.debug("Deleted " + no
 								+ " files when deleting " + file);
 						fse = null;
 					}
 				} catch (FileSystemException e) {
 					myLogger.debug("Deleting file/folder (" + (i+1) + ". try):"
 							+ file + ". Error: " + e.getLocalizedMessage());
 
 					fse = e;
 				} finally {
 					if (fileObject != null) {
 						closeFile(fileObject);
 					}
 					fsCache.close();
 				}
 			}
 
 			if (fse != null) {
 				myLogger.error("Could not delete file " + file + ". Tried "
 						+ retries + " times.");
 				throw fse;
 			}
 		} catch (final FileSystemException e) {
 
 			throw new RemoteFileSystemException("Could not delete file: "
 					+ e.getLocalizedMessage());
 		}
 
 	}
 
 	public DataHandler download(String filename)
 			throws RemoteFileSystemException {
 
 		// just in case we want to enable multiple downloads later
 		String[] filenames = new String[] { filename };
 
 		final DataSource[] datasources = new DataSource[filenames.length];
 		final DataHandler[] datahandlers = new DataHandler[filenames.length];
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject source = null;
 
 		try {
 
 			for (int i = 0; i < filenames.length; i++) {
 
 				DataSource datasource = null;
 				source = aquireFile(fsCache, filenames[i]);
 				myLogger.debug("Preparing data for file transmission for file "
 						+ source.getName().toString());
 				try {
 					if (!source.exists()) {
 						throw new RemoteFileSystemException(
 								"Could not provide file: "
 										+ filenames[i]
 												+ " for download: InputFile does not exist.");
 					}
 
 					datasource = new FileContentDataSourceConnector(source);
 				} catch (final FileSystemException e) {
 					try {
 						source.close();
 					} catch (FileSystemException ex){
 						myLogger.warn("could not close file: " + ex.getLocalizedMessage());
 					}
 					throw new RemoteFileSystemException(
 							"Could not find or read file: " + filenames[i]
 									+ ": " + e.getMessage());
 				}
 				datasources[i] = datasource;
 				datahandlers[i] = new DataHandler(datasources[i]);
 			}
 
 		} finally {
 			fsCache.close();
 		}
 
 		return datahandlers[0];
 
 	}
 
 	public boolean fileExists(String file) throws RemoteFileSystemException {
 
 		boolean exists;
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject fo = null;
 
 		try {
 			fo = aquireFile(fsCache, file);
 			exists = fo.exists();
 			return exists;
 		} catch (final FileSystemException e) {
 
 			throw new RemoteFileSystemException(
 					"Could not connect to filesystem to aquire file: " + file);
 
 		} finally {
 			fsCache.close();
 			closeFile(fo);
 		}
 
 	}
 
 	public long getFileSize(final String file) throws RemoteFileSystemException {
 
 		long size;
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject file_object = null;
 		try {
 			file_object = aquireFile(fsCache, file);
 			size = file_object.getContent().getSize();
 		} catch (final FileSystemException e) {
 			throw new RemoteFileSystemException("Could not get size of file: "
 					+ file + ": " + e.getMessage());
 		} finally {
 			fsCache.close();
 			closeFile(file_object);
 		}
 
 		return size;
 	}
 
 	public GridFile getFolderListing(String url, int recursiveLevels)
 			throws RemoteFileSystemException {
 
 		if (recursiveLevels > 1) {
 			throw new RuntimeException(
 					"Recursion > 1 not implemented for commonsvfsfilesystemplugin");
 		}
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		final FileObject fo = aquireFile(fsCache, url, null);
 
 		try {
 
 			if (!FileType.FOLDER.equals(fo.getType())) {
 				// throw new RemoteFileSystemException("Url: " + url
 				// + " not a folder.");
 
 				if (!fo.exists()) {
 					throw new RemoteFileSystemException("Url: " + url
 							+ " does not exist.");
 				}
 
 				GridFile result = new GridFile(url, fo.getContent().getSize(),
 						fo.getContent().getLastModifiedTime());
 				return result;
 
 			}
 			long lastModified = fo.getContent().getLastModifiedTime();
 
 			if (recursiveLevels == 0) {
 				GridFile result = new GridFile(url, lastModified);
 				return result;
 			}
 
 			MountPoint mp = user.getResponsibleMountpointForAbsoluteFile(url);
 
 			final GridFile folder = new GridFile(url, lastModified);
 
 			folder.addSite(mp.getSite());
 			folder.addFqan(mp.getFqan());
 			// TODO the getChildren command seems to throw exceptions without
 			// reason
 			// every now and the
 			// probably a bug in commons-vfs-grid. Until this is resolved, I
 			// always
 			// try 2 times...
 			FileObject[] children = null;
 			try {
 				children = fo.getChildren();
 			} catch (final Exception e) {
 				myLogger.error("Couldn't get children of :"
 						+ fo.getName().toString() + ". Trying one more time...", e);
 				children = fo.getChildren();
 			}
 
 			for (final FileObject child : children) {
 				if (FileType.FOLDER.equals(child.getType())) {
 					GridFile childfolder = new GridFile(child.getURL()
 							.toString());
 					// GridFile childfolder = child.getURL().get
 					// try {
 					// childfolder = new GridFile(child.getURL().toURI()
 					// .toASCIIString(), child.getContent()
 					// .getLastModifiedTime());
 					// } catch (URISyntaxException e) {
 					// e.printStackTrace();
 					// throw new RemoteFileSystemException(e);
 					// }
 					childfolder.addFqan(mp.getFqan());
 					childfolder.addSite(mp.getSite());
 					folder.addChild(childfolder);
 				} else if (FileType.FILE.equals(child.getType())) {
 					final GridFile childFile = new GridFile(child.getURL()
 							.toString(), child.getContent().getSize(), child
 							.getContent().getLastModifiedTime());
 					childFile.addFqan(mp.getFqan());
 					childFile.addSite(mp.getSite());
 					folder.addChild(childFile);
 				}
 			}
 
 			return folder;
 		} catch (FileSystemException fse) {
 			throw new RemoteFileSystemException(fse);
 		} finally {
 			fsCache.close();
 			closeFile(fo);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * grisu.backend.model.fs.FileSystemInfoPlugin#getInputStream(java
 	 * .lang.String)
 	 */
 	public GrisuInputStream getInputStream(String file)
 			throws RemoteFileSystemException {
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		try {
 			FileObject f = aquireFile(fsCache, file);
 			return new GrisuInputStreamImpl(fsCache, f.getContent()
 					.getInputStream());
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException(e);
 		}
 	}
 
 	public GrisuOutputStream getOutputStream(String file)
 			throws RemoteFileSystemException {
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		try {
 			FileObject fileO = aquireFile(fsCache, file);
 			return new GrisuOutputStreamImpl(fsCache, fileO.getContent()
 					.getOutputStream());
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException(e);
 		}
 
 	}
 
 	public boolean isFolder(final String file) throws RemoteFileSystemException {
 
 		boolean isFolder;
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject folder = null;
 		try {
 			folder = aquireFile(fsCache, file);
 			isFolder = (folder.getType() == FileType.FOLDER);
 		} catch (final Exception e) {
 			myLogger.error("Couldn't access file: " + file
 					+ " to check whether it is a folder."
 					+ e.getLocalizedMessage());
 			// e.printStackTrace();
 			// try again. sometimes it works the second time...
 			try {
 				myLogger.debug("trying a second time...");
 				isFolder = (aquireFile(fsCache, file).getType() == FileType.FOLDER);
 			} catch (final Exception e2) {
 				// e2.printStackTrace();
 				myLogger.error("Again couldn't access file: " + file
 						+ " to check whether it is a folder."
 						+ e.getLocalizedMessage());
 				throw new RemoteFileSystemException("Could not aquire file: "
 						+ file);
 			}
 		} finally {
 			fsCache.close();
 			closeFile(folder);
 		}
 
 		return isFolder;
 
 	}
 
 	public long lastModified(final String url) throws RemoteFileSystemException {
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 		FileObject file = null;
 		try {
 			file = aquireFile(fsCache, url);
 			return file.getContent().getLastModifiedTime();
 		} catch (final FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file " + url
 					+ ": " + e.getMessage());
 		} finally {
 			fsCache.close();
 			closeFile(file);
 		}
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
 		for (final MountPoint mp : user.getAllMountPoints()) {
 			if (mountPointName.equals(mp.getAlias())) {
 				throw new RemoteFileSystemException(
 						"There is already a filesystem mounted on:"
 								+ mountPointName);
 			}
 		}
 
 		MountPoint new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 				mountPointName, site);
 
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		try {
 			// FileSystem fileSystem = createFilesystem(new_mp.getRootUrl(),
 			// new_mp.getFqan());
 
 			final FileSystem fileSystem = fsCache.getFileSystem(new_mp);
 			// final FileSystem fileSystem = threadLocalFsManager.getFileSystem(
 			// new_mp.getRootUrl(), new_mp.getFqan());
 			myLogger.debug("Connected to file system.");
 			if (useHomeDirectory) {
 				// String key = createCacheKey(new_mp);
 				// synchronized (key) {
 				// uri = (String) AbstractServiceInterface
 				// .getFromSessionCache(key);
 
 				if (StringUtils.isBlank(uri)) {
 
 
 					myLogger.debug("Using home directory: "
 							+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 							.substring(1));
 					uri = fileSystem.getRoot().getName().getRootURI()
 							+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 							.substring(1);
 					// AbstractServiceInterface.putIntoSessionCache(key, uri);
 					// }
 				}
 				// if vo user, use $VOHOME/<DN> as homedirectory
 				if (cred.getFqan() != null) {
 					uri = uri + File.separator
 							+ User.get_vo_dn_path(cred.getDn());
 					fileSystem.resolveFile(
 							((String) fileSystem.getAttribute("HOME_DIRECTORY")
 									+ File.separator + cred.getDn()
 									.replace("=", "_").replace(",", "_")
 									.replace(" ", "_"))).createFolder();
 				}
 				new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 						mountPointName, site);
 			}
 
 			return new_mp;
 		} catch (final FileSystemException e) {
 			throw new RemoteFileSystemException("Error while trying to mount: "
 					+ mountPointName);
 		} finally {
 			fsCache.close();
 		}
 
 	}
 
 	public String resolveFileSystemHomeDirectory(String filesystemRoot,
 			String fqan) throws RemoteFileSystemException {
 
 		// String key = createCacheKey(filesystemRoot, fqan);
 		// synchronized (key) {
 		// String uri = (String)
 		// AbstractServiceInterface.getFromSessionCache(key);
 		//
 		// if (StringUtils.isBlank(uri)) {
 		String uri = null;
 		FileSystem fileSystem;
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		try {
 			fileSystem = fsCache.getFileSystem(filesystemRoot, fqan);
 
 			// final FileSystem fileSystem = threadLocalFsManager
 			// .getFileSystem(filesystemRoot, fqan);
 			myLogger.debug("Connected to file system.");
 
 			myLogger.debug("Using home directory: "
 					+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 					.substring(1));
 
 			final String home = (String) fileSystem
 					.getAttribute("HOME_DIRECTORY");
 			uri = fileSystem.getRoot().getName().getRootURI()
 					+ home.substring(1);
 
 			// X.p("XXXXXXXXXXXXXXXX: " + key + " / " + uri);
 
 			// AbstractServiceInterface.putIntoSessionCache(key, uri);
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException(e);
 		} finally {
 			fsCache.close();
 		}
 		// } else {
 		// X.p("OOOOOOOOOOOOOOOOOOOOO: " + key + " / " + uri);
 		// }
 		return uri;
 		// }
 
 	}
 
 	public String upload(final DataHandler source, final String filename)
 			throws RemoteFileSystemException {
 
 		myLogger.debug("Receiving file: " + filename);
 
 		OutputStream fout = null;
 		FileSystemCache fsCache = new FileSystemCache(user);
 
 		String result = null;
 		FileObject target = null;
 
 		try {
 			final String parent = filename.substring(0,
 					filename.lastIndexOf(File.separator));
 
 			createFolder(parent);
 
 			target = aquireFile(fsCache, filename);
 			result = target.getName().getURI();
 			// just to be sure that the folder exists.
 
 			myLogger.debug("Calculated target: " + target.getName().toString());
 
 			final FileContent content = target.getContent();
 			fout = content.getOutputStream();
 		} catch (final FileSystemException e) {
 
 			try {
 				if (fout != null) {
 					fout.close();
 				}
 				source.getInputStream().close();
 			} catch (final Exception e1) {
 				myLogger.error(e1);
 			}
 
 			fsCache.close();
 			closeFile(target);
 			// e.printStackTrace();
 			throw new RemoteFileSystemException("Could not open file: "
 					+ filename + ":" + e.getMessage());
 		}
 
 		myLogger.debug("Receiving data for file: " + filename);
 
 		BufferedInputStream buf;
 		try {
 			buf = new BufferedInputStream(source.getInputStream());
 
 			final byte[] buffer = new byte[1024]; // byte buffer
 			int bytesRead = 0;
 			while (true) {
 				if (Thread.interrupted()) {
 					Thread.currentThread().interrupt();
 					buf.close();
 					fout.close();
 					throw new RemoteFileSystemException(
 							"File transfer interrupted.");
 				}
 				bytesRead = buf.read(buffer, 0, 1024);
 				// bytesRead returns the actual number of bytes read from
 				// the stream. returns -1 when end of stream is detected
 				if (bytesRead == -1) {
 					break;
 				}
 				fout.write(buffer, 0, bytesRead);
 			}
 
 			if (buf != null) {
 				buf.close();
 			}
 			if (fout != null) {
 				fout.close();
 			}
 		} catch (final IOException e) {
 			try {
 				fout.close();
 				source.getInputStream().close();
 			} catch (final Exception e1) {
 				myLogger.error(e1);
 			}
 
 			throw new RemoteFileSystemException("Could not write to file: "
 					+ filename + ": " + e.getMessage());
 		} finally {
 			fsCache.close();
 			try {
 				source.getInputStream().close();
 			} catch (IOException ex) {}
 			closeFile(target);
 		}
 
 		myLogger.debug("Data transmission for file " + filename + " finished.");
 
 		buf = null;
 		fout = null;
 		return result;
 
 	}
 
 	public void uploadFileToMultipleLocations(Set<String> parents,
 			final DataHandler source, final String targetFilename,
 			final DtoActionStatus status) throws RemoteFileSystemException {
 
 		myLogger.debug("Receiving datahandler for multiple file copy...");
 
 		BufferedInputStream buf;
 		try {
 			buf = new BufferedInputStream(source.getInputStream());
 		} catch (final Exception e1) {
 			throw new RuntimeException(
 					"Could not get input stream from datahandler...", e1);
 		}
 
 		final FileSystemCache fsCacheIn = new FileSystemCache(user);
 		final FileObject tempFile = aquireFile(fsCacheIn, "tmp://"
 				+ UUID.randomUUID().toString());
 		OutputStream fout;
 		try {
 			fout = tempFile.getContent().getOutputStream();
 		} catch (final Exception e1) {
 			fsCacheIn.close();
 			throw new RemoteFileSystemException("Could not create temp file: "
 					+ e1.getLocalizedMessage());
 		}
 		myLogger.debug("Receiving data for file: " + targetFilename);
 
 		if (Thread.interrupted()) {
 			Thread.currentThread().interrupt();
 			return;
 		}
 
 		try {
 
 			final byte[] buffer = new byte[1024]; // byte buffer
 			int bytesRead = 0;
 			while (true) {
 
 				if (Thread.interrupted()) {
 					Thread.currentThread().interrupt();
 					fout.close();
 					buf.close();
 					return;
 				}
 
 				bytesRead = buf.read(buffer, 0, 1024);
 				// bytesRead returns the actual number of bytes read from
 				// the stream. returns -1 when end of stream is detected
 				if (bytesRead == -1) {
 					break;
 				}
 				fout.write(buffer, 0, bytesRead);
 			}
 
 			if (buf != null) {
 				buf.close();
 			}
 			if (fout != null) {
 				fout.close();
 			}
 		} catch (final Exception e) {
 			fsCacheIn.close();
 			throw new RemoteFileSystemException("Could not write to file: "
 					+ targetFilename + ": " + e.getMessage());
 		}
 		fout = null;
 
 		// fsCacheIn.close();
 
 		final ExecutorService executor = Executors.newFixedThreadPool(parents
 				.size());
 
 		for (final String parent : parents) {
 
 			if (Thread.interrupted()) {
 				executor.shutdownNow();
 				Thread.currentThread().interrupt();
 				status.setFinished(true);
 				status.setFailed(true);
 				return;
 			}
 
 			final Thread thread = new Thread() {
 				@Override
 				public void run() {
 
 					FileSystemCache fsCache = null;
 					try {
 						try {
 							fsCache = new FileSystemCache(user);
 							FileObject target = null;
 
 
 							RemoteFileTransferObject fileTransfer;
 
 							for (int tryNo = 0; tryNo <= ServerPropertiesManager
 									.getFileTransferRetries(); tryNo++) {
 
 								if (Thread.interrupted()) {
 									Thread.currentThread().interrupt();
 									executor.shutdownNow();
 
 									return;
 								}
 
 								try {
 									final FileObject parentObject = aquireFile(
 											fsCache, parent);
 									// FileObject tempObject = parentObject;
 
 									createFolder(parentObject);
 									// parentObject.createFolder();
 
 									target = aquireFile(fsCache, parent + "/"
 											+ targetFilename);
 									// just to be sure that the folder exists.
 
 									myLogger.debug("Calculated target for multipartjob input file: "
 											+ target.getName().toString());
 									break;
 								} catch (final Exception e) {
 									myLogger.debug(tryNo + 1
 											+ ". try to transfer file to "
 											+ parent + " failed.");
 
 									if (Thread.interrupted()) {
 										Thread.currentThread().interrupt();
 										executor.shutdownNow();
 										return;
 									}
 									myLogger.error(e);
 									if (tryNo >= (ServerPropertiesManager
 											.getFileTransferRetries() - 1)) {
 										status.addElement("Upload to folder "
 												+ parent
 												+ " failed: Could not open file: "
 												+ targetFilename + ":"
 												+ e.getMessage());
 										status.setFailed(true);
 										executor.shutdownNow();
 									} else {
 										// wait for a bit, maybe the gridftp server
 										// needs some time
 										try {
 											Thread.sleep(3000);
 										} catch (final InterruptedException e1) {
 											myLogger.error(e);
 											Thread.currentThread().interrupt();
 										}
 									}
 								}
 							}
 
 							if (Thread.interrupted()) {
 								Thread.currentThread().interrupt();
 								executor.shutdownNow();
 								status.setFinished(true);
 								status.setFailed(true);
 
 								return;
 							}
 							fileTransfer = new CommonsVfsRemoteFileTransferObject(
 									fsCache, tempFile, target, true);
 							myLogger.info("Creating fileTransfer object for source: "
 									+ tempFile.getName()
 									+ " and target: "
 									+ target.toString());
 							// fileTransfers.put(targetFileString, fileTransfer);
 
 							fileTransfer.startTransfer(true);
 
 							if (Thread.interrupted()) {
 								Thread.currentThread().interrupt();
 								executor.shutdownNow();
 								status.setFinished(true);
 								status.setFailed(true);
 								return;
 							}
 
 							if (fileTransfer.isFailed()) {
 								status.addElement("File transfer failed: "
 										+ fileTransfer
 										.getPossibleExceptionMessage());
 								status.setFailed(true);
 								executor.shutdownNow();
 							} else {
 								status.addElement("Upload to folder " + parent
 										+ " successful.");
 							}
 
 							// if (status.getTotalElements() <= status
 							// .getCurrentElements()) {
 							// status.setFinished(true);
 							// multiJob.setStatus(JobConstants.READY_TO_SUBMIT);
 							// batchJobDao.saveOrUpdate(multiJob);
 							// }
 
 						} catch (Exception e) {
 							myLogger.error(e);
 						}
 					} finally {
 						fsCache.close();
 					}
 				}
 			};
 			executor.execute(thread);
 
 		}
 
 		executor.shutdown();
 
 		myLogger.debug("All data transmissions for multiple copy of "
 				+ targetFilename + " started.");
 
 		// cleanup
 
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					executor.awaitTermination(1, TimeUnit.DAYS);
 				} catch (InterruptedException e) {
 					myLogger.error(e);
 				}
 
 				if (!status.isFinished()) {
 					status.setFinished(true);
 				}
 
 				try {
 					tempFile.delete();
 				} catch (FileSystemException e) {
 					myLogger.error(e);
 				} finally {
 					fsCacheIn.close();
 				}
 
 			}
 		}.start();
 
 	}
 }
