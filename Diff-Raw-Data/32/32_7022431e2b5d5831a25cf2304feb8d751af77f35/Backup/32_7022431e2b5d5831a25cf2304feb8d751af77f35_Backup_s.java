 package com.johnpickup.backup;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 
 import com.beust.jcommander.JCommander;
 
 public class Backup {
 	private static final String CATALOG_FILE = "jbackup.cat";
 	private String sourcePath;
 	private String destinationPath;
 	private List<String> exclusions = new ArrayList<String>();
 	private List<BackupEventListener> eventListeners = new ArrayList<BackupEventListener>();
	private BackupReport backupReport = new BackupReport();
 	private boolean showProgress = true;
 	int notificationFrequency = 100;
 	
 	public void performBackup() throws IOException {
 		for (BackupEventListener listener : eventListeners) {
 			listener.onStart();
 		}
 		
 		File destFile = new File(destinationPath);
 		if (!destFile.exists()) {
 			destFile.mkdirs();
 		}
 		RegexExclusionFilter catalogFilter = new RegexExclusionFilter();
 		catalogFilter.addExclusions(exclusions);
 		FileCatalog newCatalog = buildCatalog(sourcePath, catalogFilter);
 		FileCatalog oldCatalog = loadCatalog(destinationPath);
 		CatalogComparer differences = new CatalogComparer(oldCatalog, newCatalog);
 		if (showProgress) {
 			System.out.println("Copying " + differences.getAdded().size() + " new files, " + 
 					differences.getChanged().size() + " changed files and removing " + 
 					differences.getRemoved().size() + " deleted files.");
 		}
 		for (BackupEventListener listener : eventListeners) {
 			listener.onScanComplete();
 		}
 		copyFiles(sourcePath, differences.getAdded(), destinationPath);
 		copyFiles(sourcePath, differences.getChanged(), destinationPath);
 		deleteFiles(differences.getRemoved(), destinationPath);
 		saveCatalog(newCatalog, destinationPath);
 		for (BackupEventListener listener : eventListeners) {
 			listener.onBackupComplete();
 		}
 	}
 
 	private void copyFiles(String sourcePath, Set<String> added, String targetPath) {
 		for (String filename : added) {
 			File srcFile = new File(sourcePath + File.separator + filename);
 			File destFile = new File(targetPath + File.separator + filename);
 			try {
 				FileUtils.copyFile(srcFile, destFile);
 				destFile.setLastModified(srcFile.lastModified());
 
 				for (BackupEventListener listener : eventListeners) {
 					listener.onCopy(srcFile, destFile);
 				}
 			} catch (IOException e) {
 				for (BackupEventListener listener : eventListeners) {
 					listener.onError(srcFile, "Failed to copy file : " + e.toString());
 				}
 			}
 		}
 	}
 
 	private void deleteFiles(Set<String> removed, String targetPath) {
 		for (String filename : removed) {
 			File fileToDelete = new File(targetPath + File.separator + filename);
 			try {
 				FileUtils.forceDelete(fileToDelete);
 				for (BackupEventListener listener : eventListeners) {
 					listener.onDelete(fileToDelete);
 				}
 			} catch (IOException e) {
 				for (BackupEventListener listener : eventListeners) {
 					listener.onError(fileToDelete, "Failed to delete file : " + e.toString());
 				}
 			}
 		}
 	}
 
 	private FileCatalog loadCatalog(String catalogPath) throws IOException {
 		FileCatalog catalog = new FileCatalog();
 		String catalogFilename = catalogPath + File.separator + CATALOG_FILE;
 		catalog.loadFromFile(catalogFilename);
 		return catalog;
 	}
 
 	private void saveCatalog(FileCatalog catalog, String catalogPath) throws IOException {
 		String catalogFilename = catalogPath + File.separator + CATALOG_FILE;
 		catalog.saveToFile(catalogFilename);
 	}
 
 	private FileCatalog buildCatalog(String sourcePath,
 			Filter filter) throws IOException {
 		FileCatalog catalog = new FileCatalog();
 		DirectoryWalker walker = new DirectoryWalker();
 		walker.setErrorLogger(backupReport);
 		walker.addFilter(filter);
 		List<File> entries = walker.listEntries(new File(sourcePath));
 		for (File file : entries) {
 			String relativePath = ResourceUtils.getRelativePath(file.getAbsolutePath(), sourcePath, File.separator);
 			catalog.add(relativePath, file.length(), file.lastModified());
 		}
 		
 		return catalog;
 	}
 
 	public String getSourcePath() {
 		return sourcePath;
 	}
 
 	public void setSourcePath(String sourcePath) {
 		this.sourcePath = sourcePath;
 	}
 
 	public String getDestinationPath() {
 		return destinationPath;
 	}
 
 	public void setDestinationPath(String destinationPath) {
 		this.destinationPath = destinationPath;
 	}
 
 	public List<String> getExclusions() {
 		return exclusions;
 	}
 
 	public void setExclusions(List<String> exclusions) {
 		this.exclusions = exclusions;
 	}
 
 	public void addEventListener(BackupEventListener listener) {
 		eventListeners.add(listener);
 	}
 	
 	public void removeEventListener(BackupEventListener listener) {
 		eventListeners.remove(listener);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		BackupArguments arguments = new BackupArguments();
 		JCommander jcommander = new JCommander(arguments);
 		try {
 			jcommander.setProgramName("TestCommandLine");
 			jcommander.parse(args);
 			if (arguments.getDirectories().size() != 2) {
 				throw new RuntimeException();
 			}
 			String sourcePath = arguments.getDirectories().get(0);
 			String destinationPath = arguments.getDirectories().get(1);
 			
 			System.out.println("Backing up " +  sourcePath + " to " + destinationPath);
 			System.out.println("Excluding " + arguments.getExlusions());
 
 			Backup backup = new Backup();
			BackupReport report = new BackupReport();
 			backup.setSourcePath(sourcePath);
 			backup.setDestinationPath(destinationPath);
 			backup.setExclusions(arguments.getExlusions());
			backup.addEventListener(report);
			BackupProgress progress = new BackupProgress();
			progress.setProgressInterval(10);
			backup.addEventListener(progress);
 			try {
 				backup.performBackup();
 			} catch (Exception e) {
 				System.err.println("*** UNEXPECTED ERROR DURING BACKUP ***");
 				System.err.print(e.toString());
 				e.printStackTrace();
 				
 				System.exit(1);
 			}
 			if (!arguments.isNoreport()) {
				report.outputTo(System.out);
 			}
 		}
 		catch (Exception e) {
 			jcommander.usage();
 		}
 
 	}
 }
