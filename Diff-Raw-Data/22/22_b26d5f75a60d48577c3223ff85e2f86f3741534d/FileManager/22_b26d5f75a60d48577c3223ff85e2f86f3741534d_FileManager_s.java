 package org.vpac.grisu.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
 import org.vpac.grisu.model.dto.DtoFolder;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.model.dto.DtoStringList;
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
 
 	public static final String NON_MOUNTPOINT_CACHE_DIRECTORYNAME = "non-grisu-user-space";
 
 	private static long downloadTreshold = -1L;
 
 	/**
 	 * Convenience method to create a datahandler out of a file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the datahandler
 	 */
 	public static final DataHandler createDataHandler(File file) {
 		DataSource source = new FileDataSource(file);
 		DataHandler handler = new DataHandler(source);
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
 			if ((inputFile != null) && inputFile.startsWith("gsiftp:")) {
 				return inputFile;
 			}
 			new URL(inputFile);
 			return inputFile;
 		} catch (MalformedURLException e) {
 			File newFile = new File(inputFile);
 			return newFile.toURI().toString();
 		}
 
 	}
 
 	private static String get_url_strin_path(final String url) {
 		return url.replace("=", "_").replace(",", "_").replace(" ", "_")
 				.replace(":", "").replace("//", File.separator).replace("/",
 						File.separator);
 	}
 
 	public static long getDownloadFileSizeTreshold() {
 
 		if (downloadTreshold <= 0L) {
 			long treshold = ClientPropertiesManager
 					.getDownloadFileSizeTresholdInBytes();
 
 			return treshold;
 		} else {
 			return downloadTreshold;
 		}
 	}
 
 	public static File getFileFromUriOrPath(String uriOrPath) {
 
 		try {
 			URI uri = new URI(uriOrPath);
 			return new File(uri);
 		} catch (URISyntaxException e) {
 			return new File(uriOrPath);
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
 			return new File(url).getName();
 		} else {
 			String filename = url.substring(url.lastIndexOf("/") + 1);
 			return filename;
 		}
 
 	}
 
 	public static void setDownloadFileSizeTreshold(long t) {
 		downloadTreshold = t;
 	}
 
 	private final ServiceInterface serviceInterface;
 
 	static final Logger myLogger = Logger
 			.getLogger(FileManager.class.getName());
 
 	/**
 	 * Helper method to check whether the provided url is for a local file or
 	 * not.
 	 * 
 	 * @param file
 	 *            the url of the file
 	 * @return whether the file is local or not.
 	 */
 	public static boolean isLocal(String file) {
 
 		file = ensureUriFormat(file);
 
 		if (file.startsWith("gsiftp")) {
 			return false;
 		} else if (file.startsWith("file:")) {
 			return true;
 		} else if (file.startsWith("http:")) {
 			return false;
 		} else {
 			throw new IllegalArgumentException(
 					"Protocol not supported for file: " + file);
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
 
 	public String calculateParentUrl(String rootUrl) {
 
 		String result = rootUrl.substring(0, rootUrl.lastIndexOf("/"));
 
 		return result;
 
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
 
 		File targetFileName = new File(targetFile, sourceFile.getName());
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
 		} catch (IOException e) {
 			throw new FileTransactionException(sourceFile.toString(),
 					targetFile.toString(), "Could not copy file.", e);
 		}
 	}
 
 	public void copyLocalFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		File sourceFile = getFileFromUriOrPath(sourceUrl);
 		File targetFile = getFileFromUriOrPath(targetDirUrl);
 
 		copyLocalFiles(sourceFile, targetFile, overwrite);
 
 	}
 
 	private void copyRemoteFiles(String sourceUrl, String targetDirUrl,
 			boolean overwrite) throws FileTransactionException {
 
 		try {
 			serviceInterface.cp(DtoStringList.fromSingleString(sourceUrl),
 					targetDirUrl, overwrite, true);
 		} catch (RemoteFileSystemException e) {
 			throw new FileTransactionException(sourceUrl, targetDirUrl,
 					"Could not copy remote files.", e);
 		}
 
 	}
 
 	public void cp(File sourceFile, String targetDirUrl, boolean overwrite)
 			throws FileTransactionException {
 
 		if (isLocal(targetDirUrl)) {
 			File targetFile = getFileFromUriOrPath(targetDirUrl);
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
 
 	public void cp(Set<GlazedFile> sources, GlazedFile targetDirectory,
 			boolean overwrite) throws FileTransactionException {
 
 		for (GlazedFile source : sources) {
 			cp(source, targetDirectory, overwrite);
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
 			} catch (IOException e) {
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
 	 * Deletes the remote file and a possible local cache file.
 	 * 
 	 * @param url
 	 *            the url of the remote file
 	 * @throws RemoteFileSystemException
 	 *             if the remote file can't be deleted for some reason
 	 */
 	public void deleteFile(String url) throws RemoteFileSystemException {
 
 		File localCacheFile = getLocalCacheFile(url);
 
 		if (localCacheFile.exists()) {
 			FileUtils.deleteQuietly(localCacheFile);
 		}
 
 		serviceInterface.deleteFile(url);
 
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
 
 		if (upToDateLocalCacheFileExists(url)) {
 			return getLocalCacheFile(url);
 		}
 
 		File cacheTargetFile = getLocalCacheFile(url);
 		myLogger
 				.debug("Remote file newer than local cache file or not cached yet, downloading new copy.");
 		DataSource source = null;
 		DataHandler handler = null;
 		try {
 
 			handler = serviceInterface.download(url);
 		} catch (Exception e) {
 			myLogger.error("Could not download file: " + url);
 			throw new FileTransactionException(url, cacheTargetFile.toString(),
 					"Could not download file.", e);
 		}
 
 		try {
 			long lastModified = serviceInterface.lastModified(url);
 			FileHelpers.saveToDisk(handler.getDataSource(), cacheTargetFile);
 			cacheTargetFile.setLastModified(lastModified);
 		} catch (Exception e) {
 			myLogger.error("Could not save file: " + url.lastIndexOf("/") + 1);
 			throw new FileTransactionException(url, cacheTargetFile.toString(),
 					"Could not save file.", e);
 		}
 
 		return cacheTargetFile;
 	}
 
 	private File downloadFolder(final String url)
 			throws FileTransactionException {
 
 		DtoFolder source = null;
 		try {
 			source = serviceInterface.ls(url, 0);
 		} catch (RemoteFileSystemException e) {
 			throw new FileTransactionException(url, null,
 					"Can't list source folder.", e);
 		}
 
 		List<String> files = source.listOfAllFilesUnderThisFolder();
 		final Map<String, Exception> exceptions = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads());
 
 		for (final String file : files) {
 
 			Thread downloadThread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						downloadFile(file);
 					} catch (FileTransactionException e) {
 						exceptions.put(file, e);
 					}
 				}
 			};
 			executor1.execute(downloadThread);
 		}
 
 		executor1.shutdown();
 
 		try {
 			executor1.awaitTermination(10, TimeUnit.HOURS);
 		} catch (InterruptedException e) {
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
 
 		File targetFile = new File(target);
 		if (targetFile.exists() && targetFile.isDirectory()) {
 			if (!targetFile.canWrite()) {
 				throw new IOException("Can't write to target: "
 						+ targetFile.toString());
 			}
 			targetFile = new File(target, getFilename(url));
 			if (targetFile.exists()) {
 				if (!overwrite) {
 					throw new IOException("Can't download file to "
 							+ targetFile + ". File already exists.");
 				}
 			}
 		}
 
 		boolean isFolder = false;
 		try {
 			isFolder = serviceInterface.isFolder(url);
 		} catch (RemoteFileSystemException e) {
 			throw new FileTransactionException(url, target,
 					"Can't determine whether source is file or folder.", e);
 		}
 
 		File cacheFile = null;
 		if (isFolder) {
 			cacheFile = downloadFolder(url);
			FileUtils.copyDirectory(cacheFile, new File(targetFile,
					getFilename(url)));
 		} else {
 			cacheFile = downloadFile(url);
			FileUtils.copyDirectory(cacheFile, targetFile);
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
 
 	public boolean isBiggerThanTreshold(String url)
 			throws RemoteFileSystemException {
 
 		long remoteFileSize = serviceInterface.getFileSize(url);
 
 		if (remoteFileSize > getDownloadFileSizeTreshold()) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public List<String> listAllChildrenFilesOfRemoteFolder(String folderUrl)
 			throws RemoteFileSystemException {
 
 		if (!serviceInterface.isFolder(folderUrl)) {
 			throw new IllegalArgumentException("Specified url is not a folder.");
 		}
 
 		DtoFolder folder = serviceInterface.ls(folderUrl, 0);
 
 		return folder.listOfAllFilesUnderThisFolder();
 	}
 
 	public DtoFolder ls(String url) throws RemoteFileSystemException {
 
 		return ls(url, 1);
 	}
 
 	public DtoFolder ls(String url, int recursionLevel)
 			throws RemoteFileSystemException {
 
 		if (isLocal(url)) {
 			File temp;
 			temp = getFileFromUriOrPath(url);
 
 			return DtoFolder.listLocalFolder(temp, false);
 
 		} else {
 
 			try {
 				return serviceInterface.ls(url, recursionLevel);
 			} catch (RemoteFileSystemException e) {
 
 				throw e;
 			}
 		}
 	}
 
 	public boolean needsDownloading(String url) {
 
 		File cacheTargetFile = getLocalCacheFile(url);
 		File cacheTargetParentFile = cacheTargetFile.getParentFile();
 
 		if (!cacheTargetFile.exists()) {
 			return true;
 		}
 
 		long lastModified = -1;
 		try {
 			lastModified = serviceInterface.lastModified(url);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(
 					"Could not get last modified time of file: " + url, e);
 		}
 
 		if (cacheTargetFile.exists()) {
 			// check last modified date
 			long local_last_modified = cacheTargetFile.lastModified();
 			myLogger.debug("local file timestamp:\t" + local_last_modified);
 			myLogger.debug("remote file timestamp:\t" + lastModified);
 			if (local_last_modified >= lastModified) {
 				myLogger
 						.debug("Local cache file is not older than remote file. No download necessary...");
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
 
 				} catch (Exception e) {
 					throw new FileTransactionException(
 							file.toString(),
 							targetFile,
 							"Could not determine whether target directory exists: ",
 							e);
 				}
 
 				myLogger.debug("Uploading local file: " + file.toString()
 						+ " to: " + targetFile);
 
 				DataHandler handler = createDataHandler(file);
 				String filetransferHandle = null;
 				try {
 					myLogger.info("Uploading file " + file.getName() + "...");
 					filetransferHandle = serviceInterface.upload(handler,
 							targetFile);
 					myLogger.info("Upload of file " + file.getName()
 							+ " successful.");
 				} catch (Exception e1) {
 					try {
 						// try again
 						myLogger.info("Uploading file " + file.getName()
 								+ "...");
 						System.out.println("FAILED. SLEEPING 1 SECONDS");
 						Thread.sleep(1000);
 						filetransferHandle = serviceInterface.upload(handler,
 								targetFile + "/" + file.getName());
 						myLogger.info("Upload of file " + file.getName()
 								+ " successful.");
 					} catch (Exception e) {
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
 			} catch (FileTransactionException e) {
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
 
 		Collection<File> allFiles = FileUtils.listFiles(folder, null, true);
 		final Map<String, Exception> errors = Collections
 				.synchronizedMap(new HashMap<String, Exception>());
 
 		final ExecutorService executor1 = Executors
 				.newFixedThreadPool(ClientPropertiesManager
 						.getConcurrentUploadThreads());
 
 		String basePath = folder.getParentFile().getPath();
 		for (final File file : allFiles) {
 
 			String filePath = file.getPath();
 			String deltaPathTemp = filePath.substring(basePath.length());
 
 			final String deltaPath;
 			if (deltaPathTemp.startsWith("/") || deltaPathTemp.startsWith("\\")) {
 				deltaPath = deltaPathTemp.substring(1);
 			} else {
 				deltaPath = deltaPathTemp;
 			}
 			Thread uploadThread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						uploadFile(file, targetDirectory + "/" + deltaPath,
 								overwrite);
 					} catch (FileTransactionException e) {
 						errors.put(file.toString(), e);
 					}
 				}
 			};
 
 			executor1.execute(uploadThread);
 
 		}
 
 		executor1.shutdown();
 
 		try {
 			executor1.awaitTermination(10, TimeUnit.HOURS);
 		} catch (InterruptedException e) {
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
 
 	public final void uploadInputFile(final File file, final String job)
 			throws FileTransactionException {
 
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
 
 			DtoJob jobdir = serviceInterface.getJob(job);
 
 		} catch (Exception e) {
 			throw new FileTransactionException(file.toString(), job,
 					"Job does not exists on the backend.: ", e);
 		}
 
 		myLogger.debug("Uploading input file: " + file.toString()
 				+ " for job: " + job);
 
 		DataHandler handler = createDataHandler(file);
 		try {
 			myLogger.info("Uploading file " + file.getName() + "...");
 			serviceInterface.uploadInputFile(job, handler, file.getName());
 
 			StatusObject so = new StatusObject(serviceInterface, file.getName());
 			so.waitForActionToFinish(4, true, false);
 
 			if (so.getStatus().isFailed()) {
 				throw new FileTransactionException(file.toString(), null,
 						"Could not upload input file.", null);
 			}
 
 			myLogger.info("Upload of input file " + file.getName()
 					+ " successful.");
 		} catch (Exception e1) {
 			try {
 				// try again
 				myLogger.info("Uploading file " + file.getName() + "...");
 				System.out.println("FAILED. SLEEPING 1 SECONDS");
 				Thread.sleep(1000);
 				serviceInterface.uploadInputFile(job, handler, file.getName());
 
 				myLogger.info("Upload of file " + file.getName()
 						+ " successful.");
 			} catch (Exception e) {
 				myLogger.info("Upload of inpu file " + file.getName()
 						+ " failed: " + e1.getLocalizedMessage());
 				myLogger.error("Inputfile upload failed: "
 						+ e1.getLocalizedMessage());
 				throw new FileTransactionException(file.toString(), null,
 						"Could not upload input file.", e1);
 			}
 		}
 
 	}
 
 	public final void uploadInputFile(final String uriOrPath, final String job)
 			throws FileTransactionException {
 
 		File file = getFileFromUriOrPath(uriOrPath);
 
 		if (file.isDirectory()) {
 			throw new FileTransactionException(uriOrPath, null,
 					"Upload of folders not supported (yet).", null);
 		}
 		uploadInputFile(file, job);
 
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
 
 		File file = getFileFromUriOrPath(uriOrPath);
 
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
 					boolean success = serviceInterface.mkdir(targetDirectory);
 
 					if (!success) {
 						throw new FileTransactionException(file.toURL()
 								.toString(), targetDirectory,
 								"Could not create target directory.", null);
 					}
 				} catch (Exception e) {
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
 				} catch (Exception e2) {
 					myLogger.debug("Could not access target directory.");
 
 					throw new FileTransactionException(file.toURL().toString(),
 							targetDirectory,
 							"Could not access target directory.", e2);
 				}
 			}
 
 		} catch (Exception e) {
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
 
 			File cacheTargetFile = getLocalCacheFile(url);
 			File cacheTargetParentFile = cacheTargetFile.getParentFile();
 
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
 			} catch (Exception e) {
 				return false;
 			}
 
 			if (cacheTargetFile.exists()) {
 				// check last modified date
 				long local_last_modified = cacheTargetFile.lastModified();
 				myLogger.debug("local file timestamp:\t" + local_last_modified);
 				myLogger.debug("remote file timestamp:\t" + lastModified);
 				if (local_last_modified >= lastModified) {
 					myLogger
 							.debug("Local cache file is not older than remote file. Doing nothing...");
 					return true;
 				}
 			}
 
 			return false;
 		}
 	}
 
 }
