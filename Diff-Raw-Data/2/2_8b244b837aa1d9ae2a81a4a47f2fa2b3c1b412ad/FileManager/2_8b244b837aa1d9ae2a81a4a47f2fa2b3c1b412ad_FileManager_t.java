 package org.vpac.grisu.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.swing.filechooser.FileSystemView;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.bushe.swing.event.EventBus;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.events.FolderCreatedEvent;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.model.dto.DtoStringList;
 import org.vpac.grisu.model.dto.GridFile;
 import org.vpac.grisu.model.files.GlazedFile;
 import org.vpac.grisu.model.status.StatusObject;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.utils.FileHelpers;
 
 /**
  * A class to make file-related stuff like transfers from/to the backend easier.
  * 
  * It also manages an internal cache.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class FileManager {
 
 	private static FileSystemView VIEW = FileSystemView.getFileSystemView();
 
 	public static final String NON_MOUNTPOINT_CACHE_DIRECTORYNAME = "non-grisu-user-space";
 
 	private static long downloadTreshold = -1L;
 
 	public static final SimpleDateFormat dateformat = new SimpleDateFormat(
 			"dd.MM.yyyy HH:mm:SSS");
 
 	public static String calculateSizeString(Long size) {
 
 		String sizeString;
 
 		if (size < 0) {
 			sizeString = "";
 		} else if (size.equals(0L)) {
 			sizeString = "0";
 		} else {
 
 			if (size > 1024 * 1024) {
 				sizeString = size / (1024 * 1024) + " MB";
 			} else if (size > 1024) {
 				sizeString = size / 1024 + " KB";
 			} else {
 				sizeString = size + " B";
 			}
 		}
 
 		return sizeString;
 
 	}
 
 	/**
 	 * Convenience method to create a datahandler out of a file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the datahandler
 	 */
 	public static final DataHandler createDataHandler(File file) {
 		final DataSource source = new FileDataSource(file);
 		final DataHandler handler = new DataHandler(source);
 		return handler;
 	}
 
 	/**
 	 * Convenience method to create a datahandler out of a file.
 	 * 
 	 * @param pathOrUri
 	 *            the file
 	 * @return the datahandler
 	 */
 	public static final DataHandler createDataHandler(String pathOrUri) {
 
 		return createDataHandler(getFileFromUriOrPath(pathOrUri));
 
 	}
 
 	public static String ensureUriFormat(String inputFile) {
 
 		try {
 			if ((inputFile != null) && (inputFile.startsWith("gsiftp:"))) {
 				return inputFile;
 			}
 
 			String[] supportedTokens = new String[] { "groups" };
 			for (String token : supportedTokens) {
 				if (inputFile.startsWith("/" + token)) {
 					return ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + ":/"
 							+ inputFile;
 				}
 			}
 
 			new URL(inputFile);
 			return inputFile;
 		} catch (final MalformedURLException e) {
 			final File newFile = new File(inputFile);
 			return newFile.toURI().toString();
 		}
 
 	}
 
 	private static String get_url_strin_path(final String url) {
 		return url.replace("=", "_").replace(",", "_").replace(" ", "_")
 				.replace(":", "").replace("//", File.separator)
 				.replace("/", File.separator);
 	}
 
 	public static long getDownloadFileSizeThreshold() {
 
 		if (downloadTreshold <= 0L) {
 			final long treshold = ClientPropertiesManager
 					.getDownloadFileSizeTresholdInBytes();
 
 			return treshold;
 		} else {
 			return downloadTreshold;
 		}
 	}
 
 	public static File getFileFromUriOrPath(String uriOrPath) {
 
 		try {
 			final URI uri = new URI(uriOrPath);
 			return new File(uri);
 		} catch (final Exception e) {
 			try {
 				return new File(uriOrPath);
 			} catch (final Exception e2) {
 				throw new RuntimeException(e2);
 			}
 		}
 
 	}
 
 	/**
 	 * Helper method to extract the filename out of an url.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return the filename
 	 */
 	public static String getFilename(String url) {
 
 		if (isLocal(url)) {
 			if ("local://".equals(url)) {
 				return "Local";
 			}
 			url = ensureUriFormat(url);
 			File file = null;
 			try {
 				file = new File(new URI(url));
 			} catch (URISyntaxException e) {
 				throw new RuntimeException(e);
 			}
 			String name = file.getName();
 			return name;
 		} else {
 
 			while (url.endsWith("/")) {
 				url = url.substring(0, url.length() - 2);
 			}
 			int lastIndex = url.lastIndexOf("/") + 1;
 			if (lastIndex <= 0) {
 				return "n/a";
 			}
 			String filename = url.substring(lastIndex);
 
 			return filename;
 		}
 
 	}
 
 	public static String getLastModifiedString(Long date) {
 
 		if (date <= 0) {
 			return "";
 		}
 		String dateString = dateformat.format(new Date(date));
 		return dateString;
 
 	}
 
 	public static Set<GridFile> getLocalFileSystems() {
 
 		File[] roots = File.listRoots();
 		Set<GridFile> result = new TreeSet<GridFile>();
 		for (File root : roots) {
 			GridFile f = new GridFile(root);
 			String name = VIEW.getSystemDisplayName(root);
 			if (StringUtils.isNotBlank(name)) {
 				f.setName(name);
 			}
 			result.add(f);
 		}
 		return result;
 
 	}
 
 	public static String removeTrailingSlash(String url) {
 
 		if (StringUtils.isBlank(url)) {
 			return "";
 		} else {
 			if (url.endsWith("/")) {
 				return url.substring(0, url.lastIndexOf("/"));
 			} else {
 				return url;
 			}
 		}
 	}
 
 	public static void setDownloadFileSizeTreshold(long t) {
 		downloadTreshold = t;
 	}
 
 	private final ServiceInterface serviceInterface;
 
 	static final Logger myLogger = Logger
 			.getLogger(FileManager.class.getName());
 
 	public static String calculateParentUrl(String rootUrl) {
 
 		if (isLocal(rootUrl)) {
 			final File file = getFileFromUriOrPath(rootUrl);
 			return file.getParentFile().toURI().toASCIIString();
 		} else {
 			final String result = rootUrl
 					.substring(0, rootUrl.lastIndexOf("/"));
 			return result;
 		}
 
 	}
 
 	/**
 	 * Helper method to check whether the provided url is for a local file or
 	 * not.
 	 * 
 	 * @param file
 	 *            the url of the file
 	 * @return whether the file is local or not.
 	 */
 	public static boolean isLocal(String file) {
 
 		if (file.startsWith("gsiftp:")) {
 			return false;
 		} else if (file.startsWith(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 				+ ":")) {
 			return false;
 		} else if (file.startsWith("file:")) {
 			return true;
 		} else if (file.startsWith("local:")) {
 			return true;
 		} else if (file.startsWith("http:")) {
 			return false;
 		} else {
 			return true;
 			// throw new IllegalArgumentException(
 			// "Protocol not supported for file: " + file);
 		}
 
 	}
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param si
 	 *            the serviceInterface
 	 */
 	public FileManager(final ServiceInterface si) {
 		this.serviceInterface = si;
 	}
 
 	public void copyLocalFiles(File sourceFile, File targetFile,
 			boolean overwrite) throws FileTransactionException {
 		if (!sourceFile.exists()) {
 			throw new FileTransactionException(sourceFile.toString(),
 					targetFile.toString(), "Source file doesn't exist.", null);
 		}
 
 		if (targetFile.exists()) {
 			if (!targetFile.isDirectory()) {
 				throw new FileTransactionException(sourceFile.toString(),
 						targetFile.toString(), "Target not a directory.", null);
 			}
 		} else {
 			targetFile.mkdirs();
 			if (!targetFile.exists()) {
 				throw new FileTransactionException(sourceFile.toString(),
 						targetFile.toString(),
 						"Could not create target directory.", null);
 			}
 		}
 
 		final File targetFileName = new File(targetFile, sourceFile.getName());
 		if (!overwrite && targetFileName.exists()) {
 			throw new FileTransactionException(sourceFile.toString(),
 					targetFile.toString(),
 					"Target file already exists and overwrite not enabled.",
 					null);
 		}
 
 		try {
 			if (sourceFile.isDirectory()) {
 				FileUtils.copyDirectory(sourceFile, targetFile);
 			} else {
 				FileUtils.copyFileToDirectory(sourceFile, targetFile);
 			}
 		} catch (final IOException e) {
 			throw new FileTransactionException(sourceFile.toString(),
 					targetFile.toString(), "Could not copy file.", e);
 		}
 	}
 
 	public void copyLocalFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		final File sourceFile = getFileFromUriOrPath(sourceUrl);
 		final File targetFile = getFileFromUriOrPath(targetDirUrl);
 
 		copyLocalFiles(sourceFile, targetFile, overwrite);
 
 	}
 
 	private void copyRemoteFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		try {
 			serviceInterface.cp(DtoStringList.fromSingleString(sourceUrl),
 					targetDirUrl, overwrite, true);
 		} catch (final RemoteFileSystemException e) {
 			throw new FileTransactionException(sourceUrl, targetDirUrl,
 					e.getLocalizedMessage(), e);
 		}
 
 	}
 
 	public void cp(File sourceFile, String targetDirUrl, boolean overwrite)
 			throws FileTransactionException {
 
 		if (isLocal(targetDirUrl)) {
 			final File targetFile = getFileFromUriOrPath(targetDirUrl);
 			copyLocalFiles(sourceFile, targetFile, overwrite);
 		} else {
 			uploadUrlToDirectory(sourceFile.toURI().toString(), targetDirUrl,
 					overwrite);
 		}
 
 	}
 
 	public void cp(GlazedFile source, GlazedFile target, boolean overwrite)
 			throws FileTransactionException {
 		cp(source.getUrl(), target.getUrl(), overwrite);
 	}
 
 	public void cp(GridFile source, GridFile targetDir, boolean overwrite)
 			throws FileTransactionException {
 
 		cp(source.getUrl(), targetDir.getUrl(), overwrite);
 	}
 
 	public void cp(Set<GlazedFile> sources, GlazedFile targetDirectory,
 			boolean overwrite) throws FileTransactionException {
 
 		for (final GlazedFile source : sources) {
 			cp(source, targetDirectory, overwrite);
 		}
 
 	}
 
 	public void cp(Set<GridFile> sources, GridFile targetDirectory,
 			boolean overwrite) throws FileTransactionException {
 
 		for (final GridFile file : sources) {
 			cp(file, targetDirectory, overwrite);
 		}
 	}
 
 	public void cp(String sourceUrl, String targetDirUrl, boolean overwrite)
 			throws FileTransactionException {
 
 		if (isLocal(sourceUrl) && isLocal(targetDirUrl)) {
 
 			copyLocalFiles(sourceUrl, targetDirUrl, overwrite);
 			return;
 
 		} else if (isLocal(sourceUrl) && !isLocal(targetDirUrl)) {
 
 			uploadUrlToDirectory(sourceUrl, targetDirUrl, overwrite);
 			return;
 
 		} else if (!isLocal(sourceUrl) && isLocal(targetDirUrl)) {
 
 			try {
 				downloadUrl(sourceUrl, targetDirUrl, overwrite);
 			} catch (final IOException e) {
 				e.printStackTrace();
 				throw new FileTransactionException(sourceUrl, targetDirUrl,
 						"Could not write target file.", e);
 			}
 			return;
 
 		} else if (!isLocal(sourceUrl) && !isLocal(targetDirUrl)) {
 
 			copyRemoteFiles(sourceUrl, targetDirUrl, overwrite);
 			return;
 		}
 
 		throw new IllegalArgumentException(
 				"Can't determine location of files for " + sourceUrl + "and "
 						+ targetDirUrl + ".");
 	}
 
 	public boolean createFolder(GlazedFile currentDirectory, String s) {
 
 		if (!GlazedFile.Type.FILETYPE_FOLDER.equals(currentDirectory.getType())) {
 			return false;
 		}
 
 		String url = null;
 		if (isLocal(currentDirectory.getUrl())) {
 
 			url = currentDirectory.getUrl() + File.separator + s;
 			final File newFolder = getFileFromUriOrPath(url);
 
 			if (newFolder.exists()) {
 				myLogger.debug("Folder " + newFolder.toString()
 						+ " already exists. Not creating it.");
 				return false;
 			} else {
 				final boolean result = newFolder.mkdirs();
 				if (result) {
 					EventBus.publish(new FolderCreatedEvent(url));
 				}
 				return result;
 			}
 		} else {
 			url = currentDirectory.getUrl() + "/" + s;
 
 			try {
 				final boolean result = serviceInterface.mkdir(url);
 				if (result) {
 					EventBus.publish(new FolderCreatedEvent(url));
 				}
 				return result;
 			} catch (final RemoteFileSystemException e) {
 				return false;
 			}
 		}
 
 	}
 
 	public boolean createFolder(GridFile currentDirectory, String s) {
 
 		if (!GridFile.FILETYPE_FOLDER.equals(currentDirectory.getType())) {
 			return false;
 		}
 
 		String url = null;
 		if (isLocal(currentDirectory.getUrl())) {
 
 			url = currentDirectory.getUrl() + File.separator + s;
 			final File newFolder = getFileFromUriOrPath(url);
 
 			if (newFolder.exists()) {
 				myLogger.debug("Folder " + newFolder.toString()
 						+ " already exists. Not creating it.");
 				return false;
 			} else {
 				final boolean result = newFolder.mkdirs();
 				if (result) {
 					EventBus.publish(new FolderCreatedEvent(url));
 				}
 				return result;
 			}
 		} else {
 			url = currentDirectory.getUrl() + "/" + s;
 
 			try {
 				final boolean result = serviceInterface.mkdir(url);
 				if (result) {
 					EventBus.publish(new FolderCreatedEvent(url));
 				}
 				return result;
 			} catch (final RemoteFileSystemException e) {
 				return false;
 			}
 		}
 
 	}
 
 	public GlazedFile createGlazedFileFromUrl(String url) {
 
 		if (FileManager.isLocal(url)) {
 			try {
 				final File file = new File(new URI(ensureUriFormat(url)));
 				return new GlazedFile(file);
 			} catch (final URISyntaxException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return new GlazedFile(url, serviceInterface);
 		}
 
 	}
 
 	/**
 	 * Use this method to create a GlazedFile from a url and you already know
 	 * which type (file, folder) the file should be. That saves time in having
 	 * to look up the type.
 	 * 
 	 * @param url
 	 * @param type
 	 * @return
 	 */
 	public GlazedFile createGlazedFileFromUrl(String url, GlazedFile.Type type) {
 
 		if (FileManager.isLocal(url)) {
 			final File file = getFileFromUriOrPath(url);
 			return new GlazedFile(file);
 		} else {
 			return new GlazedFile(url, serviceInterface, type);
 		}
 
 	}
 
 	public GridFile createGridFile(String url) throws RemoteFileSystemException {
 
 		if (FileManager.isLocal(url)) {
 			try {
 				final File file = new File(new URI(ensureUriFormat(url)));
 				return new GridFile(file);
 			} catch (final URISyntaxException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return serviceInterface.ls(url, 0);
 		}
 
 	}
 
 	/**
 	 * Deletes the remote file and a possible local cache file.
 	 * 
 	 * @param url
 	 *            the url of the remote file
 	 * @throws RemoteFileSystemException
 	 *             if the remote file can't be deleted for some reason
 	 */
 	public void deleteFile(String url) throws RemoteFileSystemException {
 
 		final File localCacheFile = getLocalCacheFile(url);
 
 		if (localCacheFile.exists()) {
 			FileUtils.deleteQuietly(localCacheFile);
 		}
 
 		if (!isLocal(url)) {
 			serviceInterface.deleteFile(url);
 		}
 
 	}
 
 	/**
 	 * Downloads the file with the specified url into the local cache and
 	 * returns a file object for it.
 	 * 
 	 * @param url
 	 *            the source url
 	 * @return the file object for the cached file
 	 * @throws FileTransactionException
 	 *             if the transfer fails
 	 */
 	public final File downloadFile(final String url)
 			throws FileTransactionException {
 
 		if (isLocal(url)) {
 			return getFileFromUriOrPath(url);
 		}
 
 		if (upToDateLocalCacheFileExists(url)) {
 			return getLocalCacheFile(url);
 		}
 
 		final File cacheTargetFile = getLocalCacheFile(url);
 
 		long lastModified;
 		try {
 			lastModified = serviceInterface.lastModified(url);
 		} catch (RemoteFileSystemException e1) {
 			myLogger.error("Could not get last modified time of file: " + url);
 			throw new FileTransactionException(url, cacheTargetFile.toString(),
 					"Could not get lastModified time.", e1);
 		}
 		// if (cacheTargetFile.exists()) {
 		// long cacheFileLastModified = cacheTargetFile.lastModified();
 		// if (cacheFileLastModified >= lastModified) {
 		// return cacheTargetFile;
 		// }
 		// }
 
 		myLogger.debug("Remote file newer than local cache file or not cached yet, downloading new copy.");
 		final DataSource source = null;
 		DataHandler handler = null;
 		try {
 
 			handler = serviceInterface.download(url);
 		} catch (final Exception e) {
 			myLogger.error("Could not download file: " + url);
 			throw new FileTransactionException(url, cacheTargetFile.toString(),
 					"Could not download file.", e);
 		}
 
 		try {
 			FileHelpers.saveToDisk(handler.getDataSource(), cacheTargetFile);
 			cacheTargetFile.setLastModified(lastModified);
 		} catch (final Exception e) {
 			myLogger.error("Could not save file: "
 					+ url.substring(url.lastIndexOf("/")));
 			throw new FileTransactionException(url, cacheTargetFile.toString(),
 					"Could not save file.", e);
 		}
 
 		return cacheTargetFile;
 	}
 
 	private File downloadFolder(final String url)
 			throws FileTransactionException {
 
 		GridFile source = null;
 		try {
 			source = serviceInterface.ls(url, 0);
 		} catch (final RemoteFileSystemException e) {
 			throw new FileTransactionException(url, null,
 					"Can't list source folder.", e);
 		}
 
 		final List<String> files = source.listOfAllFilesUnderThisFolder();
 		final Map<String, Exception> exceptions = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads());
 
 		for (final String file : files) {
 
 			final Thread downloadThread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						downloadFile(file);
 					} catch (final FileTransactionException e) {
 						exceptions.put(file, e);
 					}
 				}
 			};
 			executor1.execute(downloadThread);
 		}
 
 		executor1.shutdown();
 
 		try {
 			executor1.awaitTermination(10, TimeUnit.HOURS);
 		} catch (final InterruptedException e) {
 			executor1.shutdownNow();
 			throw new FileTransactionException(url, null,
 					"Folder download interrupted", e);
 		}
 
 		if (exceptions.size() > 0) {
 			throw new FileTransactionException(url, null,
 					"Error transfering the following files: "
 							+ StringUtils.join(exceptions.keySet(), ", "), null);
 		}
 
 		myLogger.debug("File download for folder " + url + " successful.");
 
 		return getLocalCacheFile(url);
 	}
 
 	/**
 	 * Downloads a remote file to the specified target.
 	 * 
 	 * If the target is an existing directory, the file will be put in there, if
 	 * not, a file with that name will be created (along with all intermediate
 	 * directories). If the target file already exists then you need to specify
 	 * overwrite=true.
 	 * 
 	 * @param url
 	 *            the url of the source file
 	 * @param target
 	 *            the target directory of file
 	 * @param overwrite
 	 *            whether to overwrite a possibly existing file
 	 * @return the handle to the downloaded file
 	 * @throws IOException
 	 *             if the target isn't writable
 	 * @throws FileTransactionException
 	 *             if the file can't be downloaded for some reason
 	 */
 	public File downloadUrl(String url, String target, boolean overwrite)
 			throws IOException, FileTransactionException {
 
 		final File targetFile = getFileFromUriOrPath(target + "/"
 				+ getFilename(url));
 		if (targetFile.exists() && targetFile.isDirectory()) {
 			if (!targetFile.canWrite()) {
 				throw new IOException("Can't write to target: "
 						+ targetFile.toString());
 			}
 		}
 
 		if (targetFile.exists()) {
 			if (!overwrite) {
 				throw new IOException("Can't download file to " + targetFile
 						+ ". File already exists.");
 			}
 		}
 
 		boolean isFolder = false;
 		try {
 			isFolder = serviceInterface.isFolder(url);
 		} catch (final RemoteFileSystemException e) {
 			throw new FileTransactionException(url, target,
 					"Can't determine whether source is file or folder.", e);
 		}
 
 		File cacheFile = null;
 		if (isFolder) {
 			cacheFile = downloadFolder(url);
 			// File newDir = new File(targetFile, getFilename(url));
 			// boolean canWritePar = targetFile.canWrite();
 			// boolean canWrite = newDir.canWrite();
 			// boolean created = newDir.mkdirs();
 			FileUtils.copyDirectory(cacheFile, targetFile);
 		} else {
 			cacheFile = downloadFile(url);
 			final File newFile = targetFile;
 			FileUtils.copyFile(cacheFile, newFile);
 		}
 
 		return targetFile;
 	}
 
 	public boolean fileExists(String file) throws RemoteFileSystemException {
 
 		if (isLocal(file)) {
 			return new File(file).exists();
 		} else {
 			return serviceInterface.fileExists(file);
 		}
 
 	}
 
 	public long getFileSize(String url) throws RemoteFileSystemException {
 
 		final Long fs = serviceInterface.getFileSize(url);
 		System.out.println("Filesize " + url + ": " + fs);
 		return fs;
 	}
 
 	public GridFile getGridRoot() {
 		return new GridFile(
 				ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + "://", -1L);
 	}
 
 	public File getLocalCacheFile(final String url) {
 
 		if (isLocal(url)) {
 
 			return getFileFromUriOrPath(url);
 
 		} else {
 
 			String rootPath = null;
 			rootPath = Environment.getGrisuLocalCacheRoot() + File.separator
 					+ get_url_strin_path(url);
 
 			return new File(rootPath);
 		}
 
 	}
 
 	public GridFile getLocalRoot() {
 
 		GridFile localRoot = new GridFile("local://", -1);
 		localRoot.setIsVirtual(true);
 		localRoot.setName("Local files");
 		localRoot.addSite("Local");
 
 		String homeDir = System.getProperty("user.home");
 		File h = new File(homeDir);
 		GridFile home = new GridFile(h, -100);
 
 		localRoot.addChild(home);
 
 		for (GridFile f : getLocalFileSystems()) {
 			localRoot.addChild(f);
 		}
 
 		return localRoot;
 	}
 
 	public boolean isBiggerThanThreshold(String url)
 			throws RemoteFileSystemException {
 
 		final long remoteFileSize = serviceInterface.getFileSize(url);
 
 		if (remoteFileSize > getDownloadFileSizeThreshold()) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public boolean isFile(String file) {
 		if (isLocal(file)) {
 			return new File(file).isFile();
 		} else {
 			try {
 				if (serviceInterface.fileExists(file)) {
 					return !serviceInterface.isFolder(file);
 				} else {
 					return false;
 				}
 			} catch (final RemoteFileSystemException e) {
 				return false;
 			}
 		}
 	}
 
 	public boolean isFolder(String file) {
 
 		if (isLocal(file)) {
 			return new File(file).isDirectory();
 		} else {
 			try {
 				if (serviceInterface.fileExists(file)) {
 					return serviceInterface.isFolder(file);
 				} else {
 					return false;
 				}
 			} catch (final RemoteFileSystemException e) {
 				return false;
 			}
 		}
 	}
 
 	public List<String> listAllChildrenFilesOfRemoteFolder(String folderUrl)
 			throws RemoteFileSystemException {
 
 		if (!serviceInterface.isFolder(folderUrl)) {
 			throw new IllegalArgumentException("Specified url " + folderUrl
 					+ " is not a folder.");
 		}
 
		final GridFile folder = serviceInterface.ls(folderUrl, 1);
 
 		return folder.listOfAllFilesUnderThisFolder();
 	}
 
 	public synchronized List<GlazedFile> ls(GlazedFile parent)
 			throws RemoteFileSystemException {
 
 		List<GlazedFile> result = new ArrayList<GlazedFile>();
 
 		GridFile folder = ls(parent.getUrl());
 
 		for (GridFile f : folder.getChildren()) {
 			result.add(new GlazedFile(f));
 		}
 
 		return result;
 	}
 
 	public Set<GridFile> ls(GridFile parent) throws RemoteFileSystemException {
 
 		GridFile folder = null;
 		if (StringUtils.isNotBlank(parent.getPath())) {
 			folder = ls(parent.getPath());
 		} else {
 			folder = ls(parent.getUrl());
 		}
 
 		if (folder == null) {
 			return null;
 		}
 
 		return folder.getChildren();
 
 	}
 
 	public GridFile ls(String url) throws RemoteFileSystemException {
 		return ls(url, 1);
 	}
 
 	public GridFile ls(String url, int recursionLevel)
 			throws RemoteFileSystemException {
 
 		if (isLocal(url)) {
 
 			if ("local://".equals(url)) {
 				return getLocalRoot();
 			}
 			File temp;
 			temp = getFileFromUriOrPath(url);
 
 			return GridFile.listLocalFolder(temp, false);
 
 		} else {
 
 			try {
 				GridFile result = serviceInterface.ls(url, recursionLevel);
 				return result;
 			} catch (final RemoteFileSystemException e) {
 
 				throw e;
 			}
 		}
 	}
 
 	public boolean needsDownloading(String url) {
 
 		final File cacheTargetFile = getLocalCacheFile(url);
 		// final File cacheTargetParentFile = cacheTargetFile.getParentFile();
 
 		if (!cacheTargetFile.exists()) {
 			return true;
 		}
 
 		long lastModified = -1;
 		try {
 			lastModified = serviceInterface.lastModified(url);
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(
 					"Could not get last modified time of file: " + url, e);
 		}
 
 		if (cacheTargetFile.exists()) {
 			// check last modified date
 			final long local_last_modified = cacheTargetFile.lastModified();
 			myLogger.debug("local file timestamp:\t" + local_last_modified);
 			myLogger.debug("remote file timestamp:\t" + lastModified);
 			if (local_last_modified >= lastModified) {
 				myLogger.debug("Local cache file is not older than remote file. No download necessary...");
 				return false;
 			} else {
 				return true;
 			}
 		} else {
 			return true;
 		}
 
 	}
 
 	public final void uploadFile(final File file, final String targetFile,
 			boolean overwrite) throws FileTransactionException {
 
 		FileTransactionException lastException = null;
 		for (int i = 0; i < ClientPropertiesManager.getFileUploadRetries(); i++) {
 
 			lastException = null;
 			try {
 
 				if (!file.exists()) {
 					throw new FileTransactionException(file.toString(),
 							targetFile, "File does not exist: "
 									+ file.toString(), null);
 				}
 
 				if (!file.canRead()) {
 					throw new FileTransactionException(file.toString(),
 							targetFile, "Can't read file: " + file.toString(),
 							null);
 				}
 
 				if (file.isDirectory()) {
 					throw new FileTransactionException(file.toString(),
 							targetFile,
 							"Transfer of folders not supported yet.", null);
 				}
 
 				// checking whether folder exists and is folder
 				try {
 					if (serviceInterface.fileExists(targetFile)) {
 
 						if (!overwrite) {
 							throw new FileTransactionException(file.toString(),
 									targetFile, "Target file exists.", null);
 						}
 					}
 
 				} catch (final Exception e) {
 					throw new FileTransactionException(
 							file.toString(),
 							targetFile,
 							"Could not determine whether target directory exists: ",
 							e);
 				}
 
 				myLogger.debug("Uploading local file: " + file.toString()
 						+ " to: " + targetFile);
 
 				final DataHandler handler = createDataHandler(file);
 				String filetransferHandle = null;
 				try {
 					myLogger.info("Uploading file " + file.getName() + "...");
 					filetransferHandle = serviceInterface.upload(handler,
 							targetFile);
 					myLogger.info("Upload of file " + file.getName()
 							+ " successful.");
 				} catch (final Exception e1) {
 					try {
 						e1.printStackTrace();
 						// try again
 						myLogger.info("Uploading file " + file.getName()
 								+ "...");
 						System.out.println("FAILED. SLEEPING 1 SECONDS");
 						Thread.sleep(1000);
 						filetransferHandle = serviceInterface.upload(handler,
 								targetFile + "/" + file.getName());
 						myLogger.info("Upload of file " + file.getName()
 								+ " successful.");
 					} catch (final Exception e) {
 						myLogger.info("Upload of file " + file.getName()
 								+ " failed: " + e1.getLocalizedMessage());
 						myLogger.error("File upload failed: "
 								+ e1.getLocalizedMessage());
 						throw new FileTransactionException(file.toString(),
 								targetFile, "Could not upload file.", e1);
 					}
 				}
 				// successful, no retry necessary
 				break;
 			} catch (final FileTransactionException e) {
 				lastException = e;
 			}
 		}
 
 		if (lastException != null) {
 			throw lastException;
 		}
 
 	}
 
 	/**
 	 * Uploads a file to the backend which forwards it to it's target
 	 * destination.
 	 * 
 	 * @param file
 	 *            the source file
 	 * @param sourcePath
 	 *            the local file
 	 * @param targetDirectory
 	 *            the target directory url
 	 * @param overwrite
 	 *            whether to overwrite a possibly existing target file
 	 * @throws FileTransactionException
 	 *             if the transfer fails
 	 */
 	private final void uploadFileToDirectory(final File file,
 			final String targetDirectory, final boolean overwrite)
 			throws FileTransactionException {
 
 		if (file.isDirectory()) {
 			throw new FileTransactionException(file.toString(),
 					targetDirectory, "Transfer of folders not supported yet.",
 					null);
 		}
 
 		myLogger.debug("Uploading local file: " + file.toString() + " to: "
 				+ targetDirectory);
 
 		uploadFile(file, targetDirectory + "/" + file.getName(), overwrite);
 
 	}
 
 	public final void uploadFolderToDirectory(final File folder,
 			final String targetDirectory, final boolean overwrite)
 			throws FileTransactionException {
 
 		if (!folder.isDirectory()) {
 			throw new FileTransactionException(folder.toString(),
 					targetDirectory, "Source is no folder.", null);
 		}
 
 		final Collection<File> allFiles = FileUtils.listFiles(folder, null,
 				true);
 		final Map<String, Exception> errors = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads());
 
 		final String basePath = folder.getParentFile().getPath();
 		for (final File file : allFiles) {
 
 			final String filePath = file.getPath();
 			final String deltaPathTemp = filePath.substring(basePath.length());
 
 			String deltaPath;
 			if (deltaPathTemp.startsWith("/") || deltaPathTemp.startsWith("\\")) {
 				deltaPath = deltaPathTemp.substring(1);
 			} else {
 				deltaPath = deltaPathTemp;
 			}
 
 			deltaPath = deltaPath.replace('\\', '/');
 			deltaPath = deltaPath.replace("/./", "/");
 
 			try {
 				deltaPath = URLEncoder.encode(deltaPath, "UTF-8");
 			} catch (UnsupportedEncodingException e2) {
 				// shouldn't happen
 			}
 
 			final String finalDeltaPath = deltaPath;
 
 			final Thread uploadThread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						uploadFile(file,
 								targetDirectory + "/" + finalDeltaPath,
 								overwrite);
 					} catch (final FileTransactionException e) {
 						errors.put(file.toString(), e);
 					}
 				}
 			};
 
 			executor1.execute(uploadThread);
 
 		}
 
 		executor1.shutdown();
 
 		try {
 			executor1.awaitTermination(10, TimeUnit.HOURS);
 		} catch (final InterruptedException e) {
 			executor1.shutdownNow();
 			throw new FileTransactionException(folder.toString(),
 					targetDirectory, "File upload interrupted", e);
 		}
 
 		if (errors.size() > 0) {
 			throw new FileTransactionException(folder.toString(),
 					targetDirectory, "Error transfering the following files: "
 							+ StringUtils.join(errors.keySet(), ", "), null);
 		}
 
 		myLogger.debug("File upload for folder " + folder.toString()
 				+ " successful.");
 
 	}
 
 	// private final void uploadInputFile(final String job, final String
 	// uriOrPath)
 	// throws FileTransactionException {
 	//
 	// final File file = getFileFromUriOrPath(uriOrPath);
 	//
 	// if (file.isDirectory()) {
 	// throw new FileTransactionException(uriOrPath, null,
 	// "Upload of folders not supported for job input files.",
 	// null);
 	// } else {
 	// uploadInputFile(file, job);
 	// }
 	//
 	// }
 
 	private final void uploadInputFile(final String job, final File file,
 			final String targetPath) throws FileTransactionException {
 
 		if (!file.exists()) {
 			throw new FileTransactionException(file.toString(), null,
 					"File does not exist: " + file.toString(), null);
 		}
 
 		if (!file.canRead()) {
 			throw new FileTransactionException(file.toString(), null,
 					"Can't read file: " + file.toString(), null);
 		}
 
 		if (file.isDirectory()) {
 			throw new FileTransactionException(file.toString(), null,
 					"Transfer of folders not supported yet.", null);
 		}
 
 		// checking whether folder exists and is folder
 		try {
 
 			final DtoJob jobdir = serviceInterface.getJob(job);
 
 		} catch (final Exception e) {
 			throw new FileTransactionException(file.toString(), job,
 					"Job does not exists on the backend.: ", e);
 		}
 
 		myLogger.debug("Uploading input file: " + file.toString()
 				+ " for job: " + job);
 
 		final DataHandler handler = createDataHandler(file);
 		try {
 			myLogger.info("Uploading file " + file.getName() + "...");
 			serviceInterface.uploadInputFile(job, handler, targetPath);
 
 			final StatusObject so = new StatusObject(serviceInterface,
 					targetPath);
 			so.waitForActionToFinish(4, true, false);
 
 			if (so.getStatus().isFailed()) {
 				throw new FileTransactionException(file.toString(), null,
 						"Could not upload input file.", null);
 			}
 
 			myLogger.info("Upload of input file " + file.getName()
 					+ " successful.");
 		} catch (final Exception e1) {
 			try {
 				e1.printStackTrace();
 				// try again
 				myLogger.info("Uploading file " + file.getName() + "...");
 				System.out.println("FAILED. SLEEPING 1 SECONDS");
 				Thread.sleep(1000);
 				serviceInterface.uploadInputFile(job, handler, file.getName());
 
 				myLogger.info("Upload of file " + file.getName()
 						+ " successful.");
 			} catch (final Exception e) {
 				myLogger.info("Upload of inpu file " + file.getName()
 						+ " failed: " + e1.getLocalizedMessage());
 				myLogger.error("Inputfile upload failed: "
 						+ e1.getLocalizedMessage());
 				throw new FileTransactionException(file.toString(), null,
 						"Could not upload input file.", e1);
 			}
 		}
 
 	}
 
 	private final void uploadInputFolder(final String jobname,
 			final File folder, String path) throws FileTransactionException {
 
 		if (!folder.isDirectory()) {
 			throw new FileTransactionException(folder.toString(), null,
 					"Source is no folder.", null);
 		}
 
 		final Collection<File> allFiles = FileUtils.listFiles(folder, null,
 				true);
 		final Map<String, Exception> errors = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads());
 
 		// final String basePath = folder.getParentFile().getPath();
 		final String basePath = folder.getPath();
 		for (final File file : allFiles) {
 
 			final String filePath = file.getPath();
 			final String deltaPathTemp = path + "/"
 					+ filePath.substring(basePath.length());
 
 			String deltaPath;
 			if (deltaPathTemp.startsWith("/") || deltaPathTemp.startsWith("\\")) {
 				deltaPath = deltaPathTemp.substring(1);
 			} else {
 				deltaPath = deltaPathTemp;
 			}
 
 			deltaPath = deltaPath.replace('\\', '/');
 			deltaPath = deltaPath.replace("/./", "/");
 
 			// try {
 			// deltaPath = URLEncoder.encode(deltaPath, "UTF-8");
 			deltaPath = deltaPath.replaceAll("\\s", "%20");
 			// } catch (UnsupportedEncodingException e2) {
 			// // shouldn't happen
 			// }
 
 			myLogger.debug("Delta path for input folder: " + deltaPath);
 
 			final String finalDeltaPath = deltaPath;
 
 			final DataHandler handler = createDataHandler(file);
 
 			final Thread uploadThread = new Thread() {
 				@Override
 				public void run() {
 					try {
 
 						try {
 							myLogger.info("Uploading file " + file.getName()
 									+ "...");
 
 							serviceInterface.uploadInputFile(jobname, handler,
 									finalDeltaPath);
 
 							final StatusObject so = new StatusObject(
 									serviceInterface, finalDeltaPath);
 							so.waitForActionToFinish(4, true, false);
 
 							if (so.getStatus().isFailed()) {
 								throw new FileTransactionException(
 										file.toString(),
 										null,
 										"Could not upload input file "
 												+ file.toString()
 												+ ": "
 												+ DtoActionStatus
 														.getLogMessagesAsString(so
 																.getStatus()),
 										null);
 							}
 
 							myLogger.info("Upload of input file "
 									+ file.getName() + " successful.");
 						} catch (final Exception e1) {
 							try {
 								e1.printStackTrace();
 								// try again
 								myLogger.info("Uploading file "
 										+ file.getName() + "...");
 								System.out
 										.println("FAILED. SLEEPING 1 SECONDS");
 								Thread.sleep(1000);
 								serviceInterface.uploadInputFile(jobname,
 										handler, file.getName());
 
 								final StatusObject so = new StatusObject(
 										serviceInterface, finalDeltaPath);
 								so.waitForActionToFinish(4, true, false);
 
 								if (so.getStatus().isFailed()) {
 									throw new FileTransactionException(
 											file.toString(), null,
 											"Could not upload input file.",
 											null);
 								}
 
 								myLogger.info("Upload of file "
 										+ file.getName() + " successful.");
 							} catch (final Exception e) {
 								myLogger.info("Upload of input file "
 										+ file.getName() + " failed: "
 										+ e1.getLocalizedMessage());
 								myLogger.error("Inputfile upload failed: "
 										+ e1.getLocalizedMessage());
 								throw new FileTransactionException(
 										file.toString(), null,
 										"Could not upload input file.", e1);
 							}
 						}
 
 					} catch (final FileTransactionException e) {
 						errors.put(file.toString(), e);
 						executor1.shutdownNow();
 					}
 				}
 			};
 
 			executor1.execute(uploadThread);
 
 		}
 
 		executor1.shutdown();
 
 		try {
 			executor1.awaitTermination(10, TimeUnit.HOURS);
 		} catch (final InterruptedException e) {
 			executor1.shutdownNow();
 			throw new FileTransactionException(folder.toString(), null,
 					"File upload interrupted", e);
 		}
 
 		if (errors.size() > 0) {
 			throw new FileTransactionException(folder.toString(), null,
 					"Error transfering the following files: "
 							+ StringUtils.join(errors.keySet(), ", "), null);
 		}
 
 		myLogger.debug("File upload for folder " + folder.toString()
 				+ " successful.");
 	}
 
 	public final void uploadJobInput(String job, String uriOrPath,
 			String targetPath) throws FileTransactionException {
 
 		final File file = getFileFromUriOrPath(uriOrPath);
 
 		if (!file.exists()) {
 			throw new FileTransactionException(uriOrPath, null, "Source file ("
 					+ file.getPath() + ") does not exist.", null);
 		}
 
 		if (StringUtils.isBlank(targetPath)) {
 			targetPath = FileManager.getFilename(uriOrPath);
 			// X.p("Target path: " + targetPath);
 
 		}
 
 		// try {
 		// targetPath = URLEncoder.encode(targetPath, "UTF-8");
 		// } catch (UnsupportedEncodingException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 		targetPath = targetPath.replaceAll("\\s", "%20");
 
 		if (file.isDirectory()) {
 			uploadInputFolder(job, file, targetPath);
 		} else {
 			uploadInputFile(job, file, targetPath);
 		}
 
 	}
 
 	/**
 	 * Uploads a file to the backend which forwards it to it's target
 	 * destination.
 	 * 
 	 * @param uriOrPath
 	 *            the path to the local file
 	 * @param targetDirectory
 	 *            the target url
 	 * @param overwrite
 	 *            whether to overwrite a possibly existing target file
 	 * @throws FileTransactionException
 	 *             if the transfer fails
 	 */
 	public final void uploadUrlToDirectory(final String uriOrPath,
 			final String targetDirectory, boolean overwrite)
 			throws FileTransactionException {
 
 		final File file = getFileFromUriOrPath(uriOrPath);
 
 		if (!file.exists()) {
 			throw new FileTransactionException(file.toString(),
 					targetDirectory, "File does not exist: " + file.toString(),
 					null);
 		}
 
 		if (!file.canRead()) {
 			throw new FileTransactionException(file.toString(),
 					targetDirectory, "Can't read file: " + file.toString(),
 					null);
 		}
 
 		// checking whether folder exists and is folder
 		try {
 			if (!serviceInterface.fileExists(targetDirectory)) {
 				try {
 					final boolean success = serviceInterface
 							.mkdir(targetDirectory);
 
 					if (!success) {
 						throw new FileTransactionException(file.toURL()
 								.toString(), targetDirectory,
 								"Could not create target directory.", null);
 					}
 				} catch (final Exception e) {
 					throw new FileTransactionException(file.toURL().toString(),
 							targetDirectory,
 							"Could not create target directory.", e);
 				}
 			} else {
 				try {
 					if (!serviceInterface.isFolder(targetDirectory)) {
 						throw new FileTransactionException(file.toURL()
 								.toString(), targetDirectory,
 								"Can't upload file. Target is a file.", null);
 					}
 				} catch (final Exception e2) {
 					myLogger.debug("Could not access target directory.");
 
 					throw new FileTransactionException(file.toURL().toString(),
 							targetDirectory,
 							"Could not access target directory.", e2);
 				}
 			}
 
 		} catch (final Exception e) {
 			throw new FileTransactionException(file.toString(),
 					targetDirectory,
 					"Could not determine whether target directory exists: ", e);
 		}
 
 		if (file.isDirectory()) {
 			uploadFolderToDirectory(file, targetDirectory, overwrite);
 		} else {
 			uploadFileToDirectory(file, targetDirectory, overwrite);
 		}
 
 	}
 
 	public boolean upToDateLocalCacheFileExists(String url) {
 
 		if (isLocal(url)) {
 
 			return true;
 
 		} else {
 
 			final File cacheTargetFile = getLocalCacheFile(url);
 			final File cacheTargetParentFile = cacheTargetFile.getParentFile();
 
 			if (!cacheTargetParentFile.exists()) {
 				if (!cacheTargetParentFile.mkdirs()) {
 					if (!cacheTargetParentFile.exists()) {
 						throw new RuntimeException(
 								"Could not create parent folder for cache file "
 										+ cacheTargetFile);
 					}
 				}
 			}
 
 			long lastModified = -1;
 			try {
 				lastModified = serviceInterface.lastModified(url);
 			} catch (final Exception e) {
 				return false;
 			}
 
 			if (cacheTargetFile.exists()) {
 				// check last modified date
 				final long local_last_modified = cacheTargetFile.lastModified();
 				myLogger.debug("local file timestamp:\t" + local_last_modified);
 				myLogger.debug("remote file timestamp:\t" + lastModified);
 				if (local_last_modified >= lastModified) {
 					myLogger.debug("Local cache file is not older than remote file. Doing nothing...");
 					return true;
 				}
 			}
 
 			return false;
 		}
 	}
 }
