 package collector.desktop.model.database.utilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.filesystem.BuildInformation;
 import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
 
 public class DatabaseIntegrityManager {
 	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIntegrityManager.class);
 	/** The extension used on file names for autosaves */
 	private static final String AUTO_SAVE_EXTENSION = "autosave";
 	/** Regular expression describing the file name format including the extension of auto saves */
 	private static final String AUTO_SAVE_FILE_REGEX = "(\\w)+(\\u005F([0-9]+)+\\." + AUTO_SAVE_EXTENSION + ")$";
 	/** A prefix for those databases that have been corrupted */
 	static final String CORRUPT_DATABASE_SNAPSHOT_PREFIX = "corruptDatabaseSnapshot_";
 	/** The maximum amount of autosaves that can be stored until the existing autosaves are overwritten */
 	private static int autoSaveLimit = 5;
 	/** The last change time in milliseconds */
 	private static long lastChangeTimeStampInMS = -1;
 	
 	/**
 	 * Creates a savepoint to which the database state can be rolled back to. A new transaction is started.
 	 * @return The name of the created savepoint or null in case of failure. The create savepoint should only be used in public methods to avoid ovehead
 	 * with nested savepoints. 
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static String createSavepoint() throws DatabaseWrapperOperationException {
 		String savepointName = UUID.randomUUID().toString();
 	
 		try (PreparedStatement createSavepointStatement = ConnectionManager.getConnection().prepareStatement(
 				"SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName));) {			
 			createSavepointStatement.execute();
 			return savepointName;
 		} catch (SQLException e) {
 			LOGGER.error("Creating the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
 		}
 	}
 	
 	/**
 	 * Removes the savepoint from the internal stack such that it cannot be used for any future rollbacks.
 	 * If the transaction stack is empty all changes done since the creation of the saveopoint will be committed.
 	 * If the stack of transaction is not empty (i.e. inner transaction) then no changes are comitted but the savepoint
 	 * is removed nonetheless from the stack. The release should only be used in public methods to avoid ovehead
 	 * with nested savepoints. 
 	 * @param The name of the savepoint to be released. Must not be null or empty! 
 	 * @throws DatabaseWrapperOperationException If the errorstate within is ErrorWithDirtyState that means the release was not possible 
 	 */
 	public static void releaseSavepoint(String savepointName) throws DatabaseWrapperOperationException {
 	
 		if (savepointName == null || savepointName.isEmpty()){
 			LOGGER.error("The savepoint could not be released since the name string is null or empty");
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 		}
 		
 		try (PreparedStatement releaseSavepointStatement = ConnectionManager.getConnection().prepareStatement("RELEASE SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName));){			
 			releaseSavepointStatement.execute();
 		} catch (SQLException e) {
 			LOGGER.error("Releasing the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
 		}
 	}
 	
 	/**
 	 * The rollback should only be used in public methods to avoid overhead with nested savepoints. 
 	 * @param savepointName
 	 * * @throws DatabaseWrapperOperationException If the error state within is ErrorWithDirtyState that means the release was not possible
 	 */
 	public static void rollbackToSavepoint(String savepointName) throws DatabaseWrapperOperationException {
 	
 		if (savepointName == null || savepointName.isEmpty()){
 			LOGGER.error("The savepoint could not be rolledback to since the name string is null or empty");
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 		}	
 	
 		try (PreparedStatement rollbackToSavepointStatement = ConnectionManager.getConnection().prepareStatement(
 				"ROLLBACK TO SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName))){
 			rollbackToSavepointStatement.execute();
 		} catch (SQLException e) {
 			LOGGER.error("Rolling back the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
 		}
 	}
 	
 	/**
 	 * Backs the database entries along the properties and pictures up to the specified file.
 	 * @param filePath The path ending with the file name under which the backup will be stored.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void backupToFile(String filePath) throws DatabaseWrapperOperationException {
 		// create temporary directory which includes backup files
 		String tempDirName = java.util.UUID.randomUUID().toString();
 		File tempDir = new File(System.getProperty("user.home") + File.separator, tempDirName);
 		if (!tempDir.exists()) {
 			tempDir.mkdir();
 		}
 	
 		// backup collector home
 		File tempAppDataDir = new File(tempDir.getPath());
 		File sourceAppDataDir = new File(FileSystemAccessWrapper.COLLECTOR_HOME);
 		try {
 			String excludeRegex = "^\\.lock$|^" + FileSystemAccessWrapper.DATABASE_NAME + "$"; 
 			FileSystemAccessWrapper.copyDirectory(sourceAppDataDir, tempAppDataDir, excludeRegex);
 		} catch (IOException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
 		}
 	
 		// backup database to file
 		try (Statement statement = ConnectionManager.getConnection().createStatement()){				
 			statement.executeUpdate("backup to '" + tempDir.getPath() + File.separatorChar + FileSystemAccessWrapper.DATABASE_TO_RESTORE_NAME + "'");
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
 		}
 	
 		// zip the whole temp folder
 		FileSystemAccessWrapper.zipFolderToFile(tempDir.getPath(), filePath);
 	
 		// delete temp folder
 		FileSystemAccessWrapper.deleteDirectoryRecursively(tempDir);
 	}
 	
 	/**
 	 * Restores the database entries along the properties and pictures from the specified backup file
 	 * @param filePath The path of the file ending with the file name from which the backup will be restored.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void restoreFromFile(String filePath) throws DatabaseWrapperOperationException {
 		FileSystemAccessWrapper.clearCollectorHome();
 		FileSystemAccessWrapper.unzipFileToFolder(filePath, FileSystemAccessWrapper.COLLECTOR_HOME);
 	
	
 		try (Statement statement = ConnectionManager.getConnection().createStatement()) {			
 			statement.executeUpdate("restore from '" + FileSystemAccessWrapper.DATABASE_TO_RESTORE + "'");
 			try {
				DatabaseIntegrityManager.lastChangeTimeStampInMS =  DatabaseIntegrityManager.extractTimeStamp(new File(filePath));
 			} catch (DatabaseWrapperOperationException e) {
 				DatabaseIntegrityManager.lastChangeTimeStampInMS = System.currentTimeMillis();
 			}
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
 		}
 	
 		if ( !FileSystemAccessWrapper.deleteDatabaseRestoreFile() ) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 		}
 	
 		if ( !FileSystemAccessWrapper.updateCollectorFileStructure() ) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 		}
 	
 		if ( !FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection()) ) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 		}
 		
 		// Update timestamp
 		DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 	}
 	
 	/**
 	 * Gets the time stamp when the last change to the database happened.
 	 * @return The time in milliseconds when the last change to the database occured. -1 If not initialized.
 	 */
 	public static long getLastDatabaseChangeTimeStamp() {
 		return DatabaseIntegrityManager.lastChangeTimeStampInMS;
 	}
 	
 	public static void updateLastDatabaseChangeTimeStamp() {
 		DatabaseIntegrityManager.lastChangeTimeStampInMS = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Gets the list of existing autosaves sorted by filename timestamp, newest to oldest.
 	 * @return List of files of previous autosaves. Empty list if none exist
 	 */
 	public static List<File> getAllAutoSaves() throws DatabaseWrapperOperationException{
 		List<File> autoSaves = FileSystemAccessWrapper.getAllMatchingFilesInCollectorHome(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX);
 		Collections.sort(autoSaves, new Comparator<File>() {
 			@Override
 			public int compare(File file1, File file2) {
 				try {
 					return Long.compare(DatabaseIntegrityManager.extractTimeStamp(file2),  DatabaseIntegrityManager.extractTimeStamp(file1));
 				} catch (DatabaseWrapperOperationException e) {
 					return -1;
 				}
 			}
 		});
 		
 		return autoSaves;
 	}
 	
 	/**
 	 * Extracts the database last change timestamp from the autosave file. Requires the correct format of the name.
 	 * @param autoSaveFile
 	 * @return A long integer representing the last change of the database.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	 static long extractTimeStamp(File autoSaveFile) throws DatabaseWrapperOperationException {
 	
 		String fileName;
 		try {
 			fileName = autoSaveFile.getCanonicalFile().getName();
 		} catch (IOException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
 		}
 		
 		if ( !fileName.matches(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX) ) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
 		}
 		
 		try {
 			long databaseChangeTimeStamp = Long.parseLong(fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf(".")));
 			return databaseChangeTimeStamp;
 		} catch (NumberFormatException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
 		}
 	}
 	
 	/**
 	 * Creates an automatic backup when the current state of the database is newer than the most recent autosave.  
 	 * To reduce the memory footprint of the backup (i.e. in case of a large amount of pictures) only the db file is
 	 * backed up. 
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void backupAutoSave() throws DatabaseWrapperOperationException {		
 		String programVersion = BuildInformation.instance().getBuildType() + "_" + BuildInformation.instance().getVersion();
 		String timeStamp = Long.toString(getLastDatabaseChangeTimeStamp());		
 	
 		String autoSaveFilePath = FileSystemAccessWrapper.COLLECTOR_HOME_BACKUPS + 
 				File.separator + "PERIODICAL_BACKUP_" + programVersion + "_"; // separator for the timestamp	
 	
 		List<File> previousAutoSaveList = getAllAutoSaves();
 		if(DatabaseIntegrityManager.autoSaveLimit<1) {			
 			return;
 		};
 	
 		if (previousAutoSaveList.isEmpty()) {
 			// When no changes were made then the timestamp is the current time
 			if (getLastDatabaseChangeTimeStamp() == -1) {
 				timeStamp = Long.toString(System.currentTimeMillis());
 			}
 			autoSaveFilePath = autoSaveFilePath + timeStamp + "." + DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				LOGGER.error("Autosave - backup failed");
 				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
 			}
 		} else {// Autosaves detected
 	
 			// No need to overwrite the last autosave when no changes were made.
 			if (getLastDatabaseChangeTimeStamp() == -1) {
 				return;
 			}
 	
 			// Auto save limit reached, delete the oldest
 			if (previousAutoSaveList.size()>=DatabaseIntegrityManager.autoSaveLimit) {
 				File oldestAutoSave = previousAutoSaveList.get(previousAutoSaveList.size()-1);
 				if (oldestAutoSave.exists()) {
 					if (!oldestAutoSave.delete()){
 						LOGGER.error("Autosave - cannot delete old autosave");
 						throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
 					}
 				}
 			}
 			autoSaveFilePath = autoSaveFilePath + timeStamp + "." + DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
 	
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				LOGGER.error("Autosave - backup failed");
 				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
 			}
 		}
 	}
 }
