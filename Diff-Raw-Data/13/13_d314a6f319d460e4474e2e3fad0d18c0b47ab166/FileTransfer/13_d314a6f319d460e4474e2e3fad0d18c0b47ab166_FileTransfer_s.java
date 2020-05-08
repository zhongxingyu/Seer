 package org.vpac.grisu.client.control.files;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.exceptions.FailedDownloadsException;
 import org.vpac.grisu.client.control.utils.progress.ProgressDisplay;
 import org.vpac.grisu.client.model.files.FileConstants;
 import org.vpac.grisu.client.model.files.FileSystemException;
 import org.vpac.grisu.client.model.files.GrisuFileObject;
 import org.vpac.grisu.control.FileHelpers;
 import org.vpac.grisu.control.exceptions.InformationError;
 
 public class FileTransfer {
 	
 	public static final String DATE_FORMAT_NOW = "HH:mm:ss";
 	
 	static final Logger myLogger = Logger
 	.getLogger(FileTransfer.class.getName());
 	
 	public static final int OVERWRITE_EVERYTHING = 0;
 	public static final int OVERWRITE_ONLY_OLDER_FILES = 1;
 	public static final int DONT_OVERWRITE = 2;
 	
 	public static final int UNINITIALIZED_STATUS = -2;
 	public static final int INITIALIZED_STATUS = -1;
 	public static final int TRANSFERRING_STATUS = 0;
 	public static final int FINISHED_EITHER_WAY_STATUS = 100;
 	public static final int FINISHED_STATUS = 101;
 	public static final int FAILED_STATUS = 102;
 	public static final int CANCELLED_STATUS = 103;
 	
 	public static final String UNIDENTIEFIED_STATUS_STRING = "n/a";
 	public static final String UNINITIALIZED_STATUS_STRING = "Uninitialized";
 	public static final String INITIALIZED_STATUS_STRING = "Transfer initialized";
 	public static final String TRANSFERRING_STATUS_STRING = "Transferring...";
 	public static final String FINISHED_EITHER_WAY_STATUS_STRING = "Transfer finished (successful or not)";
 	public static final String FINISHED_STATUS_STRING = "Transfer finished";
 	public static final String FAILED_STATUS_STRING = "Transfer failed";
 	public static final String CANCELLED_STATUS_STRING = "Transfer cancelled";
 	
 	private GrisuFileObject[] sources = null;
 	private GrisuFileObject targetDirectory = null;
 	
 	private int status = UNINITIALIZED_STATUS;
 	private Map<Date, String> status_messages = new TreeMap<Date, String>();
 	
 	private Exception possibleException = null;
 	
 	private EnvironmentManager em = null;
 	
 	private Thread transferThread = null;
 	
 	private boolean isDownload = false;
 	private int overwriteMode = -1;
 	private String source_folder = null;
 	/**
 	 * Use this constructor if you want to download a folder structure to the local machine.
 	 * @param target_folder the folder on the local disk where the files should be stored
 	 * @param source_folder	the remote folder where the source files are located. This is
 	 *            used as base folder to determine into which subfolders the
 	 *            files should be stored locally
 	 * @param source_files a list of files to download
 	 * @param overwrite_mode whether local cache files should be overwritten or not
 	 *            (normally you would use OVERWRITE_ONLY_OLDER_FILES, esp if you
 	 *            use the grisu internal caching)
 	 */
 	public FileTransfer(GrisuFileObject target_folder, String source_folder,
 			GrisuFileObject[] source_files, int overwrite_mode) {
 		this.isDownload = true;
 		this.em = target_folder.getFileSystemBackend().getEnvironmentManager();
 		this.targetDirectory = target_folder;
 		this.source_folder = source_folder;
 		this.sources = source_files;
 		this.overwriteMode = overwrite_mode;
 		status = INITIALIZED_STATUS;
 		addStatusMessage("File download initialized");
 	}
 	
 	/**
 	 * Use this constructor if you want to transfer files.
 	 * @param sources the source files
 	 * @param targetDirectory the target directory
 	 */
 	public FileTransfer(GrisuFileObject[] sources, GrisuFileObject targetDirectory, int overwrite_mode) {
 		this.isDownload = false;
 		this.overwriteMode = overwrite_mode;
 		this.em = targetDirectory.getFileSystemBackend().getEnvironmentManager();
 		this.sources = sources;
 		this.targetDirectory = targetDirectory;
 		status = INITIALIZED_STATUS;
 		addStatusMessage("File transfer initialized.");
 	}
 	
 	private void addStatusMessage(String message) {
 		status_messages.put(new Date(), message);
 	}
 	 
 	public void startTransfer(boolean join) throws FileTransferException {
 		
 		
 		if ( transferThread != null ) {
 			throw new FileTransferException("Could not execute method. FileTransfer in progress.");
 		}
 		
 		transferThread = new Thread() {
 			public void run() {
 				try {
 					myLogger.debug("File transfer started for target: "+targetDirectory.getName());
 					status = TRANSFERRING_STATUS;
 					addStatusMessage("File transfer started");
 					fireFileTransferEvent(new FileTransferEvent(FileTransfer.this, FileTransferEvent.TRANSFER_STARTED));
 					if ( isDownload ) {
 						download(false, sources);
 					} else {
 						transferFiles();
 					}
 					myLogger.debug("File transfer finished for target: "+targetDirectory.getName());
 					status = FINISHED_STATUS;
 					addStatusMessage("File transfer finished.");
 					fireFileTransferEvent(new FileTransferEvent(FileTransfer.this, FileTransferEvent.TRANSFER_FINISHED));
 				} catch (Exception e) {
 					e.printStackTrace();
 					myLogger.debug("Error transfering file.");
 					if ( status == CANCELLED_STATUS ) {
 						// do nothing
 					} else {
 						status = FAILED_STATUS;
 						myLogger.debug("File transfer failed for target: "+targetDirectory.getName());
 						addStatusMessage("File transfer failed: "+e.getLocalizedMessage());
 //						addStatusMessage()
 						possibleException = e;
 						fireFileTransferEvent(new FileTransferEvent(FileTransfer.this, e));
 					}
 				}
 			}
 		};
 		
 		transferThread.start();
 		
 		if ( join ) {
			try {
				transferThread.join();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
 	}
 	
 	public void join() {
 		if ( transferThread != null && transferThread.isAlive() ) {
 			try {
 				transferThread.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	public void killTransfer() {
 		// I know I know. I shouldn't use stop(). But vfs doesn't implement the interrupt method... :-(
 		if ( transferThread != null && transferThread.isAlive() ) {
 			transferThread.stop();
 			status = CANCELLED_STATUS;
 			addStatusMessage("File transfer cancelled by user.");
 			fireFileTransferEvent(new FileTransferEvent(this, FileTransferEvent.TRANSFER_CANCELLED));
 		}
 	}
 	
 	// ---------------------------------------------------------------------------------------
 	// Event stuff
 	private Vector<FileTransferListener> fileTransferListeners;
 
 	private void fireFileTransferEvent(FileTransferEvent event) {
 		// if we have no mountPointsListeners, do nothing...
 		if (fileTransferListeners != null && !fileTransferListeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector<FileTransferListener> targets;
 			synchronized (this) {
 				targets = (Vector<FileTransferListener>) fileTransferListeners.clone();
 			}
 
 			// walk through the listener list 
 			Enumeration<FileTransferListener> e = targets.elements();
 			while (e.hasMoreElements()) {
 				FileTransferListener l = (FileTransferListener) e.nextElement();
 				try {
 					l.fileTransferEventOccured(event);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	// register a listener
 	synchronized public void addListener(FileTransferListener l) {
 		if (fileTransferListeners == null)
 			fileTransferListeners = new Vector<FileTransferListener>();
 		fileTransferListeners.addElement(l);
 	}
 
 	// remove a listener
 	synchronized public void removeListener(FileTransferListener l) {
 		if (fileTransferListeners == null) {
 			fileTransferListeners = new Vector<FileTransferListener>();
 		}
 		fileTransferListeners.removeElement(l);
 	}
 
 	public GrisuFileObject[] getSources() {
 		return sources;
 	}
 	
 	public String getSourcesString() {
 		if ( sources.length == 1 ) {
 			return sources[0].getName();
 		} else {
 			return sources.length+" input files";
 		}
 	}
 
 	public GrisuFileObject getTargetDirectory() {
 		return targetDirectory;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 	
 	
 	public String getTransferStatusString() {
 		
 		int tempStatus = getStatus();
 		switch (tempStatus) {
 		case UNINITIALIZED_STATUS: return UNINITIALIZED_STATUS_STRING;
 		case INITIALIZED_STATUS: return INITIALIZED_STATUS_STRING;
 		case TRANSFERRING_STATUS: return TRANSFERRING_STATUS_STRING;
 		case FINISHED_EITHER_WAY_STATUS: return FINISHED_EITHER_WAY_STATUS_STRING;
 		case FINISHED_STATUS: return FINISHED_STATUS_STRING;
 		case FAILED_STATUS: return FAILED_STATUS_STRING;
 		case CANCELLED_STATUS: return CANCELLED_STATUS_STRING;
 		default: return translateStatus(tempStatus);
 		
 		}
 	}
 	
 	public String getLatestTransferMessage() {
 		return status_messages.values().toArray(new String[]{})[status_messages.size()-1];
 	}
 	
 	public String getTransferMessages() {
 		
 		StringBuffer result = new StringBuffer();
 		for ( int i=0; i<status_messages.size(); i++ ) {
 		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 		    String time = sdf.format(status_messages.keySet().toArray(new Date[]{})[i]);
 			result.append(time+": "+status_messages.values().toArray(new String[]{})[i]);
 			result.append("\n");
 		}
 		return result.toString();
 	}
 	
 	private String translateStatus(int status) {
 		
 		if ( status < UNINITIALIZED_STATUS || status > CANCELLED_STATUS ) {
 			return UNIDENTIEFIED_STATUS_STRING;
 		}
 		
 		return "Transferring: "+status+"%";
 	}
 	
 
 	public Thread getTransferThread() {
 		return transferThread;
 	}
 	
 	
 	// file transfer helper methods 
 	// -------------------------------------------------------------------
 	
 	/**
 	 * A convenience method that determines whether the source files and the target
 	 * directory are local or remote and then calls the appropriate copy methods.
 	 * 
 	 * For now I wouldn't really recommend to set "displayProgress" to true since it
 	 * isn't tested very well. If you do, you have to initialize the progressDisplay first
 	 * with something like this:
 	 * 
 	 * <code>FileManagerTransferHelpers.progressDisplay = new SwingProgressDisplay(application.getJFrame());</code>
 	 * Otherwise you won't see anything. You can implement your own progress viewer with implementing
 	 * {@link ProgressDisplay
 	 * 
 	 * @param serviceInterface the serviceInterface
 	 * @param sourceFiles an array of source files
 	 * @param targetDirectory the target directory
 	 * @param displayProgress whether to display a jdialog that shows the progress or not. 
 	 * @throws InformationError if the filesystem of one of the files can't be looked up
 	 * @throws FileSystemException 
 	 */
 	public void transferFiles() throws FileSystemException, InformationError {
 
 //			progressDisplay.start(sourceFiles.length + 1, "Transfering "
 //						+ sourceFiles.length + " files...");
 			
 			Double percentage = 0.0;
 			int progress = 0;
 			// TODO improve that so all files are transfered at once
 			for (GrisuFileObject sourceFile : sources) {
 			
 				percentage = (progress / (double)sources.length) * (double)100;
 				progress = progress+1;
 				status = percentage.intValue();
 				addStatusMessage("Starting file transfer: "+sourceFile.getName());
 				fireFileTransferEvent(new FileTransferEvent(this, FileTransferEvent.TRANSFER_PROGRESS_CHANGED));
 				myLogger.debug("Copying: " + sourceFile.getURI().toString()
 						+ " to: " + targetDirectory.getURI().toString());
 
 				if (FileConstants.LOCAL_NAME.equals(sourceFile
 						.getFileSystemBackend().getSite())
 						&& FileConstants.LOCAL_NAME.equals(targetDirectory
 								.getFileSystemBackend().getSite())) {
 					transferFileFromLocalToLocal(sourceFile, targetDirectory);
 				} else if (FileConstants.LOCAL_NAME.equals(sourceFile
 						.getFileSystemBackend().getSite())
 						&& !FileConstants.LOCAL_NAME.equals(targetDirectory
 								.getFileSystemBackend().getSite())) {
 					transferFileFromLocalToRemote(sourceFile,
 							targetDirectory);
 				} else if (!FileConstants.LOCAL_NAME.equals(sourceFile
 						.getFileSystemBackend().getSite())
 						&& !FileConstants.LOCAL_NAME.equals(targetDirectory
 								.getFileSystemBackend().getSite())) {
 					transferFileFromRemoteToRemote(
 							sourceFile, targetDirectory);
 				} else if (!FileConstants.LOCAL_NAME.equals(sourceFile
 						.getFileSystemBackend().getSite())
 						&& FileConstants.LOCAL_NAME.equals(targetDirectory
 								.getFileSystemBackend().getSite())) {
 					transferFileFromRemoteToLocal(sourceFile,
 							targetDirectory);
 				}
 			}
 
 		targetDirectory.refresh();
 
 	}
 
 	private void transferFileFromLocalToLocal(
 			GrisuFileObject sourceFile, GrisuFileObject targetDirectory) {
 
 		File localSourceFile = new File(sourceFile.getURI());
 		File localTargetDirectory = new File(targetDirectory.getURI());
 
 		if (!localTargetDirectory.isDirectory()) {
 			throw new FileSystemException("Local target is not a directory: "
 					+ localTargetDirectory.toString());
 		}
 
 		FileHelpers.copyFileIntoDirectory(new File[] { new File(sourceFile
 				.getURI()) }, new File(targetDirectory.getURI()));
 
 	}
 
 	private void transferFileFromRemoteToRemote(
 			GrisuFileObject sourceFile,
 			GrisuFileObject targetDirectory) throws FileSystemException {
 		try {
 			em.getServiceInterface().cp(sourceFile.getURI().toString(), targetDirectory
 					.getURI().toString()
 					+ "/" + sourceFile.getName(), false, true);
 		} catch (Exception e) {
 			throw new FileSystemException("Could not copy file: "
 					+ sourceFile.getURI().toString() + " to: "
 					+ targetDirectory.getURI().toString());
 		}
 	}
 
 	private void transferFileFromRemoteToLocal(
 			GrisuFileObject sourceFile,
 			GrisuFileObject targetDirectory) throws FileSystemException {
 		try {
 			GrisuFileObject parent = sourceFile.getParent();
 			source_folder = parent.getURI().toString();
 			download( false, new GrisuFileObject[]{sourceFile} );
 		} catch (FailedDownloadsException e) {
 			throw new FileSystemException("Could not download file: "
 					+ sourceFile.getURI().toString());
 		}
 	}
 
 	private void transferFileFromLocalToRemote(
 			GrisuFileObject sourceFile,
 			GrisuFileObject targetDirectory) throws FileSystemException {
 
 		if (sourceFile.getType() == FileConstants.TYPE_FOLDER) {
 
 			GrisuFileObject[] children = sourceFile.getFileSystemBackend()
 					.getChildren(sourceFile, false);
 			for (GrisuFileObject child : children) {
 				URI newFolder = null;
 				try {
 					newFolder = new URI(targetDirectory.getURI().toString()
 							+ "/" + sourceFile.getName());
 					em.getServiceInterface().mkdir(newFolder.toString());
 					targetDirectory.refresh();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				transferFileFromLocalToRemote(child,
 						targetDirectory.getFileSystemBackend().getFileObject(
 								newFolder));
 			}
 
 		} else {
 
 			DataSource source = new FileDataSource(
 					new File(sourceFile.getURI()));
 			String filename = sourceFile.getName();
 			try {
 				em.getServiceInterface().upload(source, targetDirectory.getURI()
 						.toString()
 						+ "/" + filename, true);
 			} catch (Exception e1) {
 				e1.printStackTrace();
 				throw new FileSystemException("Could not upload file \""
 						+ sourceFile.getName() + "\": "
 						+ e1.getLocalizedMessage(), e1);
 			}
 		}
 	}
 
 	/**
 	 * Downloads a bunch of files into a folder on the local harddrive.
 	 * 
 	 * @param serviceInterface
 	 *            the ServiceInterface to use to download the files
 	 * @param target_folder
 	 *            the folder on the local disk where the files should be stored
 	 * @param source_folder
 	 *            the remote folder where the source files are located. This is
 	 *            used as base folder to determine into which subfolders the
 	 *            files should be stored locally
 	 * @param source_files
 	 *            a list of files to download
 	 * @param overwrite_mode
 	 *            whether local cache files should be overwritten or not
 	 *            (normally you would use OVERWRITE_ONLY_OLDER_FILES, esp if you
 	 *            use the grisu internal caching)
 	 * @throws FailedDownloadsException
 	 *             if one or more files could not be downloaded. The thrown
 	 *             exception contains a list of all failed files.
 	 */
 
 	
 	private void download(boolean calledExternally, GrisuFileObject[] downloadSources) throws FailedDownloadsException {
 
 		
 		
 		Map<String, Exception> failed_downloads = new HashMap<String, Exception>();
 
 		String[] allChilds = new String[0];
 		for (GrisuFileObject remoteFile : downloadSources) {
 			String[] childs = null;
 			try {
 				childs = em.getServiceInterface().getChildrenFiles(remoteFile.getURI()
 						.toString(), false);
 			} catch (Exception e) {
 				failed_downloads.put(remoteFile.getURI().toString(), e);
 				break;
 			}
 			String[] temp = allChilds;
 			allChilds = new String[allChilds.length + childs.length];
 			System.arraycopy(temp, 0, allChilds, 0, temp.length);
 			System.arraycopy(childs, 0, allChilds, temp.length, childs.length);
 		}
 //		if (displayProgress)
 //			progressDisplay.start(allChilds.length + 1, "Downloading "
 //					+ allChilds.length + " files...");
 
 		int progress = -1;
 		int percentage = -1;
 		if ( calledExternally ) {
 			progress = 0;
 			percentage = 0;
 		}
 
 		for (String child : allChilds) {
 			try {
 				
 				download(targetDirectory.getLocalRepresentation(false), source_folder, child,
 						overwriteMode);
 			} catch (FailedDownloadsException e) {
 				failed_downloads.putAll(e.getFailedDownloads());
 			} finally {
 				addStatusMessage("Downloaded file: "+child);
 				if ( calledExternally ) {
 					progress = progress + 1;
 					percentage = (progress / allChilds.length ) * 100;
 					status = percentage;
 					fireFileTransferEvent(new FileTransferEvent(this, FileTransferEvent.TRANSFER_PROGRESS_CHANGED));
 				}
 			}
 		}
 
 
 		if (failed_downloads.size() > 0) {
 			throw new FailedDownloadsException(failed_downloads);
 		}
 
 	}
 
 	/**
 	 * Used to download a single file over a web service interface.
 	 * 
 	 * @param serviceInterface
 	 *            the ServiceInterface to use to download the file
 	 * @param target_folder
 	 *            the folder on the local disk where the file should be stored
 	 * @param source_folder
 	 *            the remote folder where the source file is located. This is
 	 *            used as base folder to determine into which subfolders the
 	 *            file should be stored locally
 	 * @param remoteFile
 	 *            the file to download
 	 * @param overwrite_mode
 	 *            whether to overwrite a possible already existing file or not
 	 * @throws FailedDownloadsException
 	 *             if the file could not be downloaded. The thrown exception
 	 *             contains a list with the (single) failed file.
 	 */
 	private void download(
 			File target_folder, String source_folder, String remoteFile,
 			int overwrite_mode) throws FailedDownloadsException {
 
 		Map<String, Exception> failed_downloads = new HashMap<String, Exception>();
 
 		boolean isFolder = false;
 		try {
 			isFolder = em.getServiceInterface().isFolder(remoteFile);
 		} catch (Exception e1) {
 			failed_downloads.put(remoteFile, e1);
 			throw new FailedDownloadsException(failed_downloads);
 		}
 
 		if (isFolder) {
 
 			// String[] children = null;
 			// try {
 			// children = serviceInterface.getChildrenFiles(remoteFile, false);
 			// } catch (Exception e1) {
 			// failed_downloads.put(remoteFile, e1);
 			// throw new FailedDownloadsException(failed_downloads);
 			// }
 
 			// String folderName = remoteFile.substring(remoteFile
 			// .lastIndexOf("/") + 1);
 			// String folderName =
 			// folderName_temp.substring(folderName_temp.lastIndexOf("/")+1);
 
 			String folderName = remoteFile.substring(source_folder.length());
 
 			File folder = new File(target_folder, folderName);
 			if (!folder.exists()) {
 				boolean successFullyCreatedFolder = folder.mkdirs();
 				if (successFullyCreatedFolder) {
 					myLogger.debug("Successfully created folder: "
 							+ folder.toString());
 				} else {
 					myLogger.error("Could not create folder: "
 							+ folder.toString());
 				}
 			}
 			if (!folder.exists()) {
 				failed_downloads.put(remoteFile, new IOException(
 						"Could not create folder: " + folder.toString()));
 				throw new FailedDownloadsException(failed_downloads);
 			}
 			// for (String file : children) {
 			// try {
 			// download(serviceInterface, folder, source_folder, file,
 			// overwrite_mode);
 			// } catch (FailedDownloadsException fde) {
 			// failed_downloads.putAll(fde.getFailedDownloads());
 			// }
 			// }
 
 		} else {
 			// downloading of the actual file
 
 			switch (overwrite_mode) {
 
 			case OVERWRITE_EVERYTHING:
 				try {
 					downloadAndOverwrite(remoteFile,
 							source_folder, target_folder);
 				} catch (Exception e) {
 					failed_downloads.put(remoteFile, e);
 				}
 				;
 				break;
 			case OVERWRITE_ONLY_OLDER_FILES:
 				try {
 					downloadOnlyNewerFiles(remoteFile,
 							source_folder, target_folder);
 				} catch (Exception e) {
 					failed_downloads.put(remoteFile, e);
 				}
 				;
 				break;
 			case DONT_OVERWRITE:
 				try {
 					downloadOnlyNonexistantFiles(remoteFile,
 							source_folder, target_folder);
 				} catch (Exception e) {
 					failed_downloads.put(remoteFile, e);
 				}
 				;
 				break;
 
 			}
 		}
 
 		if (failed_downloads.size() > 0) {
 			throw new FailedDownloadsException(failed_downloads);
 		}
 	}
 
 	private void downloadOnlyNewerFiles(
 			String remoteFile,
 			String source_folder, File target_folder) throws Exception {
 
 		long lastModified = em.getServiceInterface().lastModified(remoteFile);
 
 		String filename = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
 		File target_parent_folder = calculateParentFolderAndCreateItIfNotExistsAlready(
 				remoteFile, source_folder, target_folder);
 
 		File target = new File(target_parent_folder, filename);
 		if (target.exists()) {
 			// check last modified date
 			long local_last_modified = target.lastModified();
 			myLogger.debug("local file timestamp:\t" + local_last_modified);
 			myLogger.debug("remote file timestamp:\t" + lastModified);
 			if (local_last_modified >= lastModified) {
 				myLogger
 						.debug("Local cache file is not older than remote file. Doing nothing...");
 				return;
 			}
 		}
 
 		myLogger
 				.debug("Remote file newer than local cache file or not cached yet, downloading new copy.");
 		DataSource source = null;
 		try {
 
 			source = em.getServiceInterface().download(remoteFile);
 		} catch (Exception e) {
 			myLogger.error("Could not download file: " + remoteFile);
 			throw e;
 		}
 
 		try {
 			File newFile = new File(target_parent_folder, filename);
 			FileHelpers.saveToDisk(source, newFile);
 			newFile.setLastModified(lastModified);
 		} catch (IOException e) {
 			myLogger.error("Could not save file: "
 					+ remoteFile.lastIndexOf("/") + 1);
 			throw e;
 		}
 
 	}
 
 	private File calculateParentFolderAndCreateItIfNotExistsAlready(
 			String remoteFile, String source_folder, File target_folder)
 			throws IOException {
 
 		File target_parent_folder = target_folder;
 
 		String relativePath = remoteFile.substring(source_folder.length() + 1);
 		if (relativePath.indexOf("/") != -1) {
 			// means subfolder
 			String relativeParentFolderPath = relativePath.substring(0,
 					relativePath.lastIndexOf("/"));
 			target_parent_folder = new File(target_folder,
 					relativeParentFolderPath);
 		}
 		target_parent_folder.mkdirs();
 		if (!target_parent_folder.exists()) {
 			myLogger.error("Could not create folder: " + target_parent_folder);
 			throw new IOException("Could not create folder: "
 					+ target_parent_folder);
 		}
 		return target_parent_folder;
 	}
 
 	private void downloadAndOverwrite(
 			String remoteFile, String source_folder, File target_folder)
 			throws Exception {
 
 		DataSource source = null;
 		long lastModified = -1;
 		try {
 			lastModified = em.getServiceInterface().lastModified(remoteFile);
 			source = em.getServiceInterface().download(remoteFile);
 		} catch (Exception e) {
 			myLogger.error("Could not download file: " + remoteFile);
 			throw e;
 		}
 
 		String filename = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
 
 		File target_parent_folder = calculateParentFolderAndCreateItIfNotExistsAlready(
 				remoteFile, source_folder, target_folder);
 
 		try {
 			File newFile = new File(target_parent_folder, filename);
 			FileHelpers.saveToDisk(source, newFile);
 			newFile.setLastModified(lastModified);
 		} catch (IOException e) {
 			myLogger.error("Could not save file: "
 					+ remoteFile.lastIndexOf("/") + 1);
 			throw e;
 		}
 
 	}
 
 	private void downloadOnlyNonexistantFiles(
 			String remoteFile,
 			String source_folder, File target_folder) throws Exception {
 
 		DataSource source = null;
 
 		String filename = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
 
 		File target_parent_folder = calculateParentFolderAndCreateItIfNotExistsAlready(
 				remoteFile, source_folder, target_folder);
 		File newFile = new File(target_parent_folder, filename);
 
 		if (!newFile.exists()) {
 			try {
 				long lastModified = em.getServiceInterface().lastModified(remoteFile);
 				source = em.getServiceInterface().download(remoteFile);
 				FileHelpers.saveToDisk(source, newFile);
 				newFile.setLastModified(lastModified);
 			} catch (IOException e) {
 				myLogger.error("Could not save file: "
 						+ remoteFile.lastIndexOf("/") + 1);
 				throw e;
 			}
 		}
 	}
 
 	public Exception getPossibleException() {
 		return possibleException;
 	}
 
 }
