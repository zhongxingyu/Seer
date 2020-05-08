 package grisu.model;
 
 import grisu.control.ServiceInterface;
 import grisu.control.events.FolderCreatedEvent;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.DtoStringList;
 import grisu.model.dto.GridFile;
 import grisu.model.files.GlazedFile;
 import grisu.model.status.StatusObject;
 import grisu.settings.ClientPropertiesManager;
 import grisu.settings.Environment;
 import grisu.utils.FileHelpers;
 
 import java.awt.datatransfer.Clipboard;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
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
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.swing.filechooser.FileSystemView;
 
 import net.sf.ehcache.util.NamedThreadFactory;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.bushe.swing.event.EventBus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A class to make file-related stuff like transfers from/to the backend easier.
  * 
  * It also manages an internal cache.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class FileManager {
 
 	public static Clipboard FILE_TRANSFER_CLIPBOARD = new Clipboard(
 			"File transfers");
 
 	public final static String[] FILESYSTEM_PLUGIN_TOKENS = new String[] { "groups" };
 
 	private static FileSystemView VIEW = FileSystemView.getFileSystemView();
 
 	public static final String NON_MOUNTPOINT_CACHE_DIRECTORYNAME = "non-grisu-user-space";
 
 	private static long downloadTreshold = -1L;
 
 	public static final SimpleDateFormat dateformat = new SimpleDateFormat(
 			"dd.MM.yyyy HH:mm:SSS");
 
 	private static final String URL_PATTERN_STRING = "^(?:[^/]+://)?([^/:]+)";
 	private static final Pattern URL_PATTERN = Pattern
 			.compile(URL_PATTERN_STRING);
 
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
 
 	public static String ensureTrailingSlash(String url) {
 		if (StringUtils.isBlank(url)) {
 			return "";
 		} else if (url.equals(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 				+ "://")
 				|| url.equals(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 						+ ":/")) {
 			return ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + "://";
 		} else {
 			if (!url.endsWith("/")) {
 				return url + "/";
 			} else {
 				return url;
 			}
 		}
 	}
 
 	/**
 	 * Convenience method that basically converts normal local paths to files
 	 * into a url format. It also supports virtual filesystems, so if your path
 	 * for example starts with "/groups", it'll prepend "grid://" to it.
 	 * 
 	 * For already vaild urls it does nothing.
 	 * 
 	 * @param inputFile
 	 *            the input file path or url
 	 * @return a valid url to the file, be it local or remote
 	 */
 	public static String ensureUriFormat(String inputFile) {
 
 		try {
 			if ((inputFile != null)
 					&& (inputFile.startsWith("local:") || !isLocal(inputFile))) {
 				return inputFile;
 			}
 
 			final String[] supportedTokens = FILESYSTEM_PLUGIN_TOKENS;
 			for (final String token : supportedTokens) {
 				if (inputFile.startsWith("/" + token)) {
 					return ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + ":/"
 							+ inputFile;
 				}
 			}
 
 			if (inputFile.startsWith("local:")) {
 				return inputFile;
 			}
 
 			new URL(inputFile);
 			return inputFile;
 		} catch (final MalformedURLException e) {
 			if (inputFile.startsWith("~")) {
 				inputFile = System.getProperty("user.home")
 						+ inputFile.substring(1);
 			}
 			final File newFile = new File(inputFile);
 			return newFile.toURI().toString();
 		}
 
 	}
 
 	/**
 	 * Replaces all special charactes in a url with "_" in order to be able to
 	 * store it in the local cache (in .grisu/cache).
 	 * 
 	 * @param url
 	 *            the url
 	 * @return the "clean" string
 	 */
 	private static String get_url_string_path(final String url) {
 		return url.replace("=", "_").replace(",", "_").replace(" ", "_")
 				.replace(":", "").replace("//", File.separator)
 				.replace("/", File.separator);
 	}
 
 	/**
 	 * Returns the configured size up to which Grisu downloads remote files for
 	 * preview without asking the user.
 	 * 
 	 * That is implemented so a user doesn't accidently double clicks a 2 GB
 	 * file and downloads it into the local cache.
 	 * 
 	 * @return the treshhold in bytes
 	 */
 	public static long getDownloadFileSizeThreshold() {
 
 		if (downloadTreshold <= 0L) {
 			final long treshold = ClientPropertiesManager
 					.getDownloadFileSizeTresholdInBytes();
 
 			return treshold;
 		} else {
 			return downloadTreshold;
 		}
 	}
 
 	/**
 	 * Convenience method to get a {@link File} object form a local path or url.
 	 * 
 	 * @param uriOrPath
 	 *            the path or url
 	 * @return the file object
 	 */
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
 
 			final File file = getFileFromUriOrPath(url);
 
 			final String name = file.getName();
 			return name;
 		} else {
 
 			while (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
 			}
 			final int lastIndex = url.lastIndexOf("/") + 1;
 			if (lastIndex <= 0) {
 				return "n/a";
 			}
 			final String filename = url.substring(lastIndex);
 
 			return filename;
 		}
 
 	}
 
 	/**
 	 * Convenience method to extract hostname from an url.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return the hostname or null if hostname couldn't be found
 	 */
 	public static String getHost(String url) {
 
 		final Matcher matcher = URL_PATTERN.matcher(url);
 		if (matcher.find()) {
 			final int start = matcher.start(1);
 			final int end = matcher.end(1);
 
 			return (url.substring(start, end));
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns a human readable String that indicates the last modified date of
 	 * a file (out of a unix time long).
 	 * 
 	 * @param date
 	 *            the date in unix time
 	 * @return the human readable date string
 	 */
 	public static String getLastModifiedString(Long date) {
 
 		if (date <= 0) {
 			return "";
 		}
 		final String dateString = dateformat.format(new Date(date));
 		return dateString;
 
 	}
 
 	/**
 	 * Returns all local filesystems (mainly for windows, returns drive names:
 	 * C:\, D:\, etc...)
 	 * 
 	 * @return the local filesytem roots
 	 */
 	public static Set<GridFile> getLocalFileSystems() {
 
 		final File[] roots = File.listRoots();
 		final Set<GridFile> result = new TreeSet<GridFile>();
 		for (final File root : roots) {
 			final GridFile f = new GridFile(root);
 			final String name = VIEW.getSystemDisplayName(root);
 			if (StringUtils.isNotBlank(name)) {
 				f.setName(name);
 			}
 			result.add(f);
 		}
 		return result;
 
 	}
 
 	/**
 	 * Convenience method to extract the protocol out of a url.
 	 * 
 	 * @param parent
 	 *            the url;
 	 * @return the protocol
 	 */
 	public static String getProtocol(String url) {
 
 		return url.split(":")[0];
 
 	}
 
 	/**
 	 * Convenience method to check whether a local file exists.
 	 * 
 	 * Beware, this method also returns true if the url is not local. That is
 	 * because it is used to only check local file, sometimes it takes to long
 	 * to check remote files for existence.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return true if the url is local and file exists or url is not local,
 	 *         false if url is local file but file doesn't exist.
 	 */
 	public static boolean localFileExists(String url) {
 
 		if (!isLocal(url)) {
 			return true;
 		}
 
 		return getFileFromUriOrPath(url).exists();
 	}
 
 	public static final String removeDoubleSlashes(String url) {
 		final int protIndex = url.indexOf("://");
 		if (protIndex < 0) {
 			return url.replace("//", "/");
 		} else {
 			final String prot = url.substring(0, protIndex + 3);
 			String other = url.substring(protIndex + 3);
 			other = other.replace("//", "/");
 			return prot + other;
 		}
 	}
 
 	/**
 	 * Convenience method to ensure that the specified url doesn't end with a
 	 * slash.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return the url without a trailing slash
 	 */
 	public static String removeTrailingSlash(String url) {
 
 		if (StringUtils.isBlank(url)) {
 			return "";
 		} else if (url.equals(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 				+ "://")
 				|| url.equals(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 						+ ":/")) {
 			return ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + "://";
 		} else {
 			if (url.endsWith("/")) {
 				return url.substring(0, url.lastIndexOf("/"));
 			} else {
 				return url;
 			}
 		}
 	}
 
 	/**
 	 * Sets the treshhold up to which Grisu doesn't complain when downloading a
 	 * file into the local cache.
 	 * 
 	 * That is implemented so that a used doesn't accidently double-clicks a 2
 	 * GB file which gets automatically downloaded in the local cache for
 	 * preview purposes.
 	 * 
 	 * @param t
 	 *            the threshold size in bytes
 	 */
 	public static void setDownloadFileSizeTreshold(long t) {
 		downloadTreshold = t;
 	}
 
 	private final ServiceInterface serviceInterface;
 
 	static final Logger myLogger = LoggerFactory.getLogger(FileManager.class);
 
 	/**
 	 * Convenience method to calculate the parent of a url.
 	 * 
 	 * @param rootUrl
 	 *            the url
 	 * @return the parent url
 	 */
 	public static String calculateParentUrl(String rootUrl) {
 
 		if (isLocal(rootUrl)) {
 			final File file = getFileFromUriOrPath(rootUrl);
 			return file.getParentFile().toURI().toASCIIString();
 		} else {
 			String url = rootUrl.trim();
 			if (rootUrl.endsWith("/")) {
 				url = url.substring(0, url.length() - 2);
 			}
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
 		} else if (file.startsWith("~")) {
 			return true;
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
 
 	/**
 	 * Copies local files.
 	 * 
 	 * @param sourceFile
 	 *            the source
 	 * @param targetFile
 	 *            the target
 	 * @param overwrite
 	 *            whether to overwrite the target if it already exists.
 	 * 
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists).
 	 */
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
 
 	/**
 	 * Copies local files.
 	 * 
 	 * @param sourceUrl
 	 *            the path or url of the source file
 	 * @param targetDirUrl
 	 *            the path or url of the target file
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists).
 	 */
 	public void copyLocalFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		final File sourceFile = getFileFromUriOrPath(sourceUrl);
 		final File targetFile = getFileFromUriOrPath(targetDirUrl);
 
 		copyLocalFiles(sourceFile, targetFile, overwrite);
 
 	}
 
 	/**
 	 * Copies remote files.
 	 * 
 	 * @param sourceUrl
 	 *            the source url
 	 * @param targetDirUrl
 	 *            the target url
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists).
 	 */
 	private void copyRemoteFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		try {
 			final String handle = serviceInterface.cp(
 					DtoStringList.fromSingleString(sourceUrl), targetDirUrl,
 					overwrite, false);
 
 			StatusObject so;
 			try {
 				so = StatusObject.waitForActionToFinish(serviceInterface,
 						handle, 2, true);
 			} catch (final Exception e) {
 				throw new FileTransactionException(sourceUrl, targetDirUrl,
 						e.getLocalizedMessage(), e);
 			}
 			if (so.getStatus().isFailed()) {
 				throw new RemoteFileSystemException(so.getStatus()
 						.getErrorCause());
 			}
 
 		} catch (final RemoteFileSystemException e) {
 			throw new FileTransactionException(sourceUrl, targetDirUrl,
 					e.getLocalizedMessage(), e);
 		}
 
 	}
 
 	/**
 	 * Copies or uploads a local file.
 	 * 
 	 * @param sourceFile
 	 *            the source file
 	 * @param targetDirUrl
 	 *            the target path or url
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists).
 	 */
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
 
 	/**
 	 * @param source
 	 * @param target
 	 * @param overwrite
 	 * @throws FileTransactionException
 	 * @Deprecated don't use {@link GlazedFiles} anymore (if you can help it)
 	 */
 	public void cp(GlazedFile source, GlazedFile target, boolean overwrite)
 			throws FileTransactionException {
 		cp(source.getUrl(), target.getUrl(), overwrite);
 	}
 
 	/**
 	 * Copies {@link GridFile} grid files (remote or local).
 	 * 
 	 * @param source
 	 *            the source file
 	 * @param targetDir
 	 *            the target file
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists
 	 */
 	public void cp(GridFile source, GridFile targetDir, boolean overwrite)
 			throws FileTransactionException {
 
 		cp(source.getUrl(), targetDir.getUrl(), overwrite);
 	}
 
 	/**
 	 * Copies a set of {@link GridFile}s to a target directory.
 	 * 
 	 * @param sources
 	 *            the source files
 	 * @param targetDirectory
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists
 	 * @deprecated don't use {@link GlazedFile anymore}
 	 */
 	@Deprecated
 	public void cp(Set<GlazedFile> sources, GlazedFile targetDirectory,
 			boolean overwrite) throws FileTransactionException {
 
 		for (final GlazedFile source : sources) {
 			cp(source, targetDirectory, overwrite);
 		}
 
 	}
 
 	/**
 	 * Copies a set of {@link GridFile}s to a target directory.
 	 * 
 	 * @param sources
 	 *            the source files
 	 * @param targetDirectory
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists
 	 */
 	public void cp(Set<GridFile> sources, GridFile targetDirectory,
 			boolean overwrite) throws FileTransactionException {
 
 		for (final GridFile file : sources) {
 			cp(file, targetDirectory, overwrite);
 		}
 	}
 
 	/**
 	 * Copies a set of urls or paths to a target directory.
 	 * 
 	 * @param sources
 	 *            the source files
 	 * @param targetDirectory
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists
 	 */
 	public void cp(Set<String> sourceUrls, String targeteDirUrl,
 			boolean overwrite) throws FileTransactionException {
 		for (final String url : sourceUrls) {
 			cp(url, targeteDirUrl, overwrite);
 		}
 	}
 
 	/**
 	 * Copies a url or path to a target directory.
 	 * 
 	 * @param sources
 	 *            the source file
 	 * @param targetDirectory
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite the target file if it exists
 	 * @throws FileTransactionException
 	 *             if the copying fails (for example because overwrite is false
 	 *             and target exists
 	 */
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
 
 	/**
 	 * @param currentDirectory
 	 * @param s
 	 * @return
 	 * @deprecated don't use {@link GlazedFile} anymore
 	 */
 	@Deprecated
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
 
 	/**
 	 * Create a new folder in a parent directory
 	 * 
 	 * @param parent
 	 *            the parent directory
 	 * @param s
 	 *            the new folder name
 	 * @return whether the new folder could be created or not
 	 * @throws RemoteFileSystemException
 	 */
 	public boolean createFolder(GridFile parent, String s)
 			throws RemoteFileSystemException {
 
 		if (!GridFile.FILETYPE_FOLDER.equals(parent.getType())) {
 			return false;
 		}
 
 		String url = null;
 		if (isLocal(parent.getUrl())) {
 
 			url = parent.getUrl() + File.separator + s;
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
 			url = parent.getUrl() + "/" + s;
 
 			final boolean result = serviceInterface.mkdir(url);
 			if (result) {
 				EventBus.publish(new FolderCreatedEvent(url));
 			}
 			return result;
 
 		}
 
 	}
 
 	public void createFolder(String parentUrl, String s)
 			throws RemoteFileSystemException {
 
 		createFolder(createGridFile(parentUrl), s);
 
 	}
 
 	/**
 	 * @param url
 	 * @return
 	 * @deprecated don't use {@link GlazedFile} anymore
 	 */
 	@Deprecated
 	public GlazedFile createGlazedFileFromUrl(String url) {
 
 		if (FileManager.isLocal(url)) {
 			final File file = getFileFromUriOrPath(url);
 			return new GlazedFile(file);
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
 
 	/**
 	 * Creates a {@link GridFile} object from a url.
 	 * 
 	 * This might involve contacting the backend and can therefor be a bit
 	 * timeconsuming, so don't use that if you have 1000s of files...
 	 * 
 	 * @param url
 	 *            the url
 	 * @return the GridFile object
 	 * @throws RemoteFileSystemException
 	 *             if the file does not exist or can't be accessed
 	 */
 	public GridFile createGridFile(String url) throws RemoteFileSystemException {
 
 		if (StringUtils.isBlank(url)) {
 			// throw new RuntimeException("AAAA");
 			return null;
 		}
 
 		if (FileManager.isLocal(url)) {
 			final File file = getFileFromUriOrPath(url);
 			return new GridFile(file);
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
 			final String handle = serviceInterface.deleteFile(url);
 			StatusObject so;
 			try {
 				so = StatusObject.waitForActionToFinish(serviceInterface,
 						handle, 2, true);
 				if (so.getStatus().isFailed()) {
 					throw new RemoteFileSystemException(so.getStatus()
 							.getErrorCause());
 				}
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
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
 		return downloadFile(url, true);
 	}
 
 	/**
 	 * Downloads the file with the specified url into the local cache and
 	 * returns a file object for it.
 	 * 
 	 * This one throws an exception if forceDownload is false and file is bigger
 	 * than filesize download threshold (@link
 	 * {@link #getDownloadFileSizeThreshold()}.
 	 * 
 	 * @param url
 	 *            the source url
 	 * @param forceDownload
 	 *            whether to download file even if size bigger than threshold.
 	 * @return the file object for the cached file
 	 * @throws FileTransactionException
 	 *             if the transfer fails
 	 */
 	public final File downloadFile(final String url, final boolean forceDownload)
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
 		} catch (final RemoteFileSystemException e1) {
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
 
 		if (!forceDownload) {
 			long size;
 			try {
 				size = serviceInterface.getFileSize(url);
 				if (size > getDownloadFileSizeThreshold()) {
 					myLogger.info("Not downloading - file bigger than download threshold: "
 							+ url);
 					throw new FileTransactionException(url,
 							cacheTargetFile.toString(),
 							"File bigger than threshold.", null);
 				}
 			} catch (final RemoteFileSystemException e2) {
 				myLogger.error("Could not get size of file: " + url);
 				throw new FileTransactionException(url,
 						cacheTargetFile.toString(), "Could not get size.", e2);
 			}
 		}
 
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
 			source = serviceInterface.ls(url, Integer.MAX_VALUE);
 		} catch (final RemoteFileSystemException e) {
 			throw new FileTransactionException(url, null,
 					"Can't list source folder.", e);
 		}
 
 		final List<String> files = source.listOfAllFilesUnderThisFolder();
 
 		final Map<String, Exception> exceptions = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		ThreadFactory tf = new NamedThreadFactory("clientFolderDownload");
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(
 						ClientPropertiesManager.getConcurrentUploadThreads(), tf);
 
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
 	 * @param targetDir
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite a possibly existing file
 	 * @return the handle to the downloaded file
 	 * @throws IOException
 	 *             if the target isn't writable
 	 * @throws FileTransactionException
 	 *             if the file can't be downloaded for some reason
 	 */
 	public File downloadUrl(String url, File targetDir, boolean overwrite)
 			throws IOException, FileTransactionException {
 
 		final File targetFile = new File(targetDir, getFilename(url));
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
 			throw new FileTransactionException(url, targetDir.toString(),
 					"Can't determine whether source is file or folder.", e);
 		}
 
 		File cacheFile = null;
 		if (isFolder) {
 			cacheFile = downloadFolder(url);
 			// File newDir = new File(targetFile, getFilename(url));
 			// boolean canWritePar = targetFile.canWrite();
 			// boolean canWrite = newDir.canWrite();
 			// boolean created = newDir.mkdirs();
 			final long size = ClientPropertiesManager
 					.getFolderSizeThresholdForCache();
 
 			final long dir = FileUtils.sizeOfDirectory(cacheFile);
 			if (dir <= size) {
 				FileUtils.copyDirectory(cacheFile, targetFile);
 			} else {
 				FileUtils.moveDirectory(cacheFile, targetFile);
 			}
 
 		} else {
 			cacheFile = downloadFile(url);
 			final File newFile = targetFile;
 			final long size = ClientPropertiesManager
 					.getFileSizeThresholdForCache();
 			if (newFile.length() <= size) {
 				FileUtils.copyFile(cacheFile, newFile);
 			} else {
 				FileUtils.moveFile(cacheFile, newFile);
 			}
 		}
 
 		return targetFile;
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
 
 		final File targetDir = getFileFromUriOrPath(target);
 		return downloadUrl(url, targetDir, overwrite);
 	}
 
 	/**
 	 * Checks whether a file exists or not.
 	 * 
 	 * @param file
 	 *            the file url or path
 	 * @return whether the file exists (true) or not (false)
 	 * @throws RemoteFileSystemException
 	 *             if the file is remote and can't be accessed
 	 */
 	public boolean fileExists(GridFile f) throws RemoteFileSystemException {
 
 		return fileExists(f.getUrl());
 	}
 
 	/**
 	 * Checks whether a file exists or not.
 	 * 
 	 * @param file
 	 *            the file url or path
 	 * @return whether the file exists (true) or not (false)
 	 * @throws RemoteFileSystemException
 	 *             if the file is remote and can't be accessed
 	 */
 	public boolean fileExists(String file) throws RemoteFileSystemException {
 
 		if (isLocal(file)) {
 			return getFileFromUriOrPath(file).exists();
 		} else {
 			return serviceInterface.fileExists(file);
 		}
 
 	}
 
 	/**
 	 * Returns the size of the file in bytes.
 	 * 
 	 * @param url
 	 *            the url or path
 	 * @return the filesize in bytes
 	 * @throws RemoteFileSystemException
 	 *             if the file is remote and can't be accessed
 	 */
 	public long getFileSize(String url) throws RemoteFileSystemException {
 
 		if (isLocal(url)) {
 			return getFileFromUriOrPath(url).length();
 		} else {
 			return serviceInterface.getFileSize(url);
 		}
 
 	}
 
 	/**
 	 * Returns a parent file for all virtual remote filesystems.
 	 * 
 	 * @return a virtual grid root file
 	 */
 	public GridFile getGridRoot() {
 		return new GridFile(
 				ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME + "://", -1L);
 	}
 
 	public Long getLastModified(String url) throws RemoteFileSystemException {
 		return serviceInterface.lastModified(url);
 	}
 
 	/**
 	 * Returns a {@link File} object that denotes the location of the specified
 	 * (remote) file in the local cache folder. Or the file directly in case the
 	 * url is local.
 	 * 
 	 * Be aware that this method doesn't download the file into the cache, you
 	 * need to do that using {@link FileManager#downloadFile(String)}.
 	 * 
 	 * @param url
 	 *            the url or path of the file
 	 * @return a {@link File} object
 	 */
 	public File getLocalCacheFile(String url) {
 
 		if (isLocal(url)) {
 
 			return getFileFromUriOrPath(url);
 
 		} else {
 
 			String rootPath = null;
 			rootPath = Environment.getGrisuLocalCacheRoot() + File.separator
 					+ get_url_string_path(url);
 
 			return new File(rootPath);
 		}
 
 	}
 
 	/**
 	 * Returns a virtual file that has got all local roots as children.
 	 * 
 	 * @return the virtual local root file
 	 */
 	public GridFile getLocalRoot() {
 
 		final GridFile localRoot = new GridFile("local://", -1);
 		localRoot.setIsVirtual(true);
 		localRoot.setName("Local files");
 		localRoot.addSite("Local");
 
 		final String homeDir = System.getProperty("user.home");
 		final File h = new File(homeDir);
 		final GridFile home = new GridFile(h, -100);
 
 		localRoot.addChild(home);
 
 		for (final GridFile f : getLocalFileSystems()) {
 			localRoot.addChild(f);
 		}
 
 		return localRoot;
 	}
 
 	/**
 	 * Checks whether the actual file a specified url points to is bigger than
 	 * the specified download treshhold.
 	 * 
 	 * The download treshhold is a value up to which Grisu automatically
 	 * downloads files into the local cache (for file preview purposes), without
 	 * asking the user.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return whether the file associated with the specified url is bigger than
 	 *         the download treshhold
 	 * @throws RemoteFileSystemException
 	 *             if the remote file can't be accessed
 	 */
 	public boolean isBiggerThanThreshold(String url)
 			throws RemoteFileSystemException {
 
 		final long remoteFileSize = serviceInterface.getFileSize(url);
 
 		if (remoteFileSize > getDownloadFileSizeThreshold()) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * Checks whether the specified url or path is a file or folder.
 	 * 
 	 * @param file
 	 *            the file url or path
 	 * @return whether url is file (true)
 	 */
 	public boolean isFile(String file) {
 
 		if (isLocal(file)) {
 			return getFileFromUriOrPath(file).isFile();
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
 
 	/**
 	 * Checks whether the specified url or path is a folder.
 	 * 
 	 * @param file
 	 *            the file url or path
 	 * @return whether url is folder (true)
 	 */
 	public boolean isFolder(String file) {
 
 		if (isLocal(file)) {
 			return getFileFromUriOrPath(file).isDirectory();
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
 
 	/**
 	 * Returns a list of urls of all files that sit under the folder specified,
 	 * including all sub-folders.
 	 * 
 	 * This method can take quite a while to execute, depending how many
 	 * sub-folder the folder has.
 	 * 
 	 * @param folderUrl
 	 *            the url of the folder
 	 * @return a list of children file urls
 	 * @throws RemoteFileSystemException
 	 *             if the folder or one of its child-folders/files can't be
 	 *             accessed
 	 */
 	public List<String> listAllChildrenFilesOfRemoteFolder(String folderUrl)
 			throws RemoteFileSystemException {
 
 		if (!serviceInterface.isFolder(folderUrl)) {
 			throw new IllegalArgumentException("Specified url " + folderUrl
 					+ " is not a folder.");
 		}
 
 		final GridFile folder = serviceInterface.ls(folderUrl, 1);
 
 		return folder.listOfAllFilesUnderThisFolder();
 	}
 
 	/**
 	 * @param parent
 	 * @return
 	 * @throws RemoteFileSystemException
 	 * @Deprecated don't use {@link GlazedFile} anymore
 	 */
 	public synchronized List<GlazedFile> ls(GlazedFile parent)
 			throws RemoteFileSystemException {
 
 		final List<GlazedFile> result = new ArrayList<GlazedFile>();
 
 		final GridFile folder = ls(parent.getUrl());
 
 		for (final GridFile f : folder.getChildren()) {
 			result.add(new GlazedFile(f));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns the children of the specified folder.
 	 * 
 	 * @param parent
 	 *            the folder to list
 	 * @return the children of the folder
 	 * @throws RemoteFileSystemException
 	 *             if the folder can't be accessed
 	 */
 	public GridFile ls(GridFile parent) throws RemoteFileSystemException {
 
 		GridFile folder = null;
 		if (StringUtils.isNotBlank(parent.getPath())) {
 			folder = ls(parent.getPath());
 		} else {
 			folder = ls(parent.getUrl());
 		}
 
 		if (folder == null) {
 			return null;
 		}
 
 		return folder;
 
 	}
 
 	/**
 	 * Returns the children of the specified folder.
 	 * 
 	 * @param url
 	 *            the url of the folder to list
 	 * @return the children of the folder
 	 * @throws RemoteFileSystemException
 	 *             if the folder can't be accessed
 	 */
 	public GridFile ls(String url) throws RemoteFileSystemException {
 		return ls(url, 1);
 	}
 
 	/**
 	 * Returns a filesystem structure of a configurable level below a specified
 	 * root folder url.
 	 * 
 	 * Be aware, values of more than 1 recursion levels are probably not
 	 * supported by most filesystem plugins yet.
 	 * 
 	 * @param url
 	 *            the url of the root folder
 	 * @param recursionLevel
 	 *            the recursion level
 	 * @return a structure of {@link GridFile}s that mirrors the remote
 	 *         filesytem structure
 	 * @throws RemoteFileSystemException
 	 *             if one of the child files/folders can't be accessed
 	 */
 	public GridFile ls(String url, int recursionLevel)
 			throws RemoteFileSystemException {
 
 		url = ensureUriFormat(url);
 
 		if (isLocal(url)) {
 
 			if ("local://".equals(url)) {
 				return getLocalRoot();
 			}
 			File temp;
 			temp = getFileFromUriOrPath(url);
 
 			return GridFile.listLocal(temp, false);
 
 		} else {
 
 			try {
 				final GridFile result = serviceInterface
 						.ls(url, recursionLevel);
 				return result;
 			} catch (final RemoteFileSystemException e) {
 
 				throw e;
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the specified url is in the local cache and up to date.
 	 * 
 	 * @param url
 	 *            the url
 	 * @return whether the local cache file exists and is up to date (false) or
 	 *         not (true)
 	 */
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
 
 	/**
 	 * Uploads a file to the specified target url.
 	 * 
 	 * @param file
 	 *            the source file object
 	 * @param targetFile
 	 *            the target file url (not target directory)
 	 * @param overwrite
 	 *            whether to overwrite the target file if it already exists
 	 * @throws FileTransactionException
 	 *             if the remote filesystem can't be accessed or the file exists
 	 *             and overwrite is set to false
 	 */
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
 						// try again
 						myLogger.info("Uploading file " + file.getName()
 								+ "...");
 						myLogger.error("FAILED. SLEEPING 1 SECONDS");
 						Thread.sleep(1000);
 						filetransferHandle = serviceInterface.upload(handler,
 								targetFile);
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
 	private final String uploadFileToDirectory(final File file,
 			final String targetDirectory, final boolean overwrite)
 					throws FileTransactionException {
 
 		if (file.isDirectory()) {
 			throw new FileTransactionException(file.toString(),
 					targetDirectory, "Transfer of folders not supported yet.",
 					null);
 		}
 
 		myLogger.debug("Uploading local file: " + file.toString() + " to: "
 				+ targetDirectory);
 
 		final String target = targetDirectory + "/" + file.getName();
 		uploadFile(file, targetDirectory + "/" + file.getName(), overwrite);
 		return target;
 
 	}
 
 	/**
 	 * Uploads a folder recursively into a target directory.
 	 * 
 	 * @param folder
 	 *            the source folder
 	 * @param targetDirectory
 	 *            the target directory
 	 * @param overwrite
 	 *            whether to overwrite the target if a file/folder with the same
 	 *            name already exists
 	 * @throws FileTransactionException
 	 *             if remote filesytem can't be accessed or target file/folder
 	 *             exists and overwrite is set to false
 	 */
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
 
 		final ThreadFactory tf = new NamedThreadFactory(
 				"clientUploadFolderToDir");
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads(), tf);
 
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
 			} catch (final UnsupportedEncodingException e2) {
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
 
 		// // checking whether folder exists and is folder
 		// try {
 		//
 		// serviceInterface.getJob(job);
 		//
 		// } catch (final Exception e) {
 		//
 		// throw new FileTransactionException(file.toString(), job,
 		// "Job does not exists on the backend.: ", e);
 		// }
 
 		myLogger.debug("Uploading input file: " + file.toString()
 				+ " for job: " + job);
 
 		final DataHandler handler = createDataHandler(file);
 		try {
 			myLogger.info("Uploading file " + file.getName() + "...");
 			serviceInterface.uploadInputFile(job, handler, targetPath);
 
 			final StatusObject so = new StatusObject(serviceInterface,
 					targetPath);
 			so.waitForActionToFinish(4, true);
 
 			if (so.getStatus().isFailed()) {
 				throw new FileTransactionException(file.toString(), null,
 						"Could not upload input file.", null);
 			}
 
 			myLogger.info("Upload of input file " + file.getName()
 					+ " successful.");
 		} catch (final Exception e1) {
 			try {
 				myLogger.error(e1.getLocalizedMessage(), e1);
 				// try again
 				myLogger.info("Uploading file " + file.getName() + "...");
 				myLogger.error("FAILED. SLEEPING 1 SECONDS");
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
 
 		final ThreadFactory tf = new NamedThreadFactory(
 				"clientUploadInputFolder");
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads(), tf);
 
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
 							so.waitForActionToFinish(4, true);
 
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
 								myLogger.error(e1.getLocalizedMessage(), e1);
 								// try again
 								myLogger.info("Uploading file "
 										+ file.getName() + "...");
 								myLogger.error("FAILED. SLEEPING 1 SECONDS");
 								Thread.sleep(1000);
 								serviceInterface.uploadInputFile(jobname,
 										handler, file.getName());
 
 								final StatusObject so = new StatusObject(
 										serviceInterface, finalDeltaPath);
 								so.waitForActionToFinish(4, true);
 
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
 
 	/**
 	 * Uploads input file for an already created (but not submitted) job on the
 	 * backend.
 	 * 
 	 * @param job
 	 *            the name of the job
 	 * @param uriOrPath
 	 *            the source file url or path
 	 * @param targetPath
 	 *            the (relative to the job directory) target path
 	 * @throws FileTransactionException
 	 *             if the remote directory can't be accessed
 	 */
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
 
 	/**
 	 * Checks whether an up to date replica of a remote file exists in the local
 	 * cache or not.
 	 * 
 	 * @param url
 	 *            the remote file url (for local files this will always return
 	 *            true)
 	 * @return whether a valid replica exists or not
 	 */
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
