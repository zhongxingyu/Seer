 package collector.desktop;
 
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 
 import org.eclipse.swt.SWT;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.controller.i18n.DictKeys;
 import collector.desktop.controller.i18n.Translator;
 import collector.desktop.controller.managers.BuildInformationManager;
 import collector.desktop.controller.managers.ConnectionManager;
 import collector.desktop.controller.managers.WelcomePageManager;
 import collector.desktop.controller.settings.ApplicationSettingsManager;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.view.ApplicationUI;
 import collector.desktop.view.various.ComponentFactory;
 
 public class Sammelbox {
 	private final static Logger LOGGER = LoggerFactory.getLogger(Sammelbox.class);
 	
 	/**
 	 * This method initializes the file structure and opens the database connections.
 	 * @throws Exception Either a class not found exception if the JDBC driver could not be initialized or
 	 * an exception if the database connection could not be established.
 	 */
 	private static void setupConnectionAndFilesystem() throws Exception {		
 		Class.forName("org.sqlite.JDBC");
 		
 		try {
 			ConnectionManager.openConnection();
 		} catch (DatabaseWrapperOperationException ex) {
 			try {
 				LOGGER.warn("Couldn't open a database connection. Will try to open a clean connection instead.");
				ConnectionManager.openCleanConnection();				
 			} catch (DatabaseWrapperOperationException ex2) {
 				LOGGER.error("The database is corrupt since opening a connection failed. " +
 						"Recent autosaves of the database can be found in: " + FileSystemAccessWrapper.COLLECTOR_HOME_BACKUPS);
 				
				// TODO indicate error to user
 			}			
 		}
 	}
 	
 	/** The main method initializes the database (using the collector constructor) and establishes the user interface */
 	public static void main(String[] args) throws ClassNotFoundException {
 		LOGGER.info("Sammelbox (build: " + BuildInformationManager.instance().getVersion() + 
 				" build on " + BuildInformationManager.instance().getBuildTimeStamp() + ") started");
 		try {
 			ApplicationSettingsManager.initializeFromSettingsFile();
 			WelcomePageManager.initializeFromWelcomeFile();
 			
 			Translator.setLanguageFromSettingsOrSystem();
 			// Ensure that the folder structure including the lock file exists before locking
 			FileSystemAccessWrapper.updateCollectorFileStructure();
 
 			RandomAccessFile lockFile = new RandomAccessFile(FileSystemAccessWrapper.LOCK_FILE, "rw");
 			FileChannel fileChannel = lockFile.getChannel();
 
 			if (fileChannel.tryLock() != null) {
 				// Initialize the Database connection
 				setupConnectionAndFilesystem();
 
 				// create the shell and show the user interface. This blocks until the shell is closed
 				ApplicationUI.initialize(ApplicationUI.getShell());
 
 				// close the database connection if the the shell is closed
 				ConnectionManager.closeConnection();
 
 				// close file & channel
 				fileChannel.close();
 				lockFile.close();
 			} else {
 				ComponentFactory.getMessageBox(ApplicationUI.getShell(), 
 						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
 						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
 						SWT.ICON_INFORMATION).open();
 			}
 		} catch (Exception ex) {
 			LOGGER.error("Sammelbox crashed", ex);
 		} finally {
 			LOGGER.info("Sammelbox stopped");
 		}
 	}
 }
