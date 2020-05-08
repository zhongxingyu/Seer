 package org.openscience.cdk.applications.taverna.setup;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
 import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
 import org.openscience.cdk.applications.taverna.basicutilities.Tools;
 import org.openscience.cdk.applications.taverna.iterativeio.DataStreamController;
 
 public class SetupController {
 
 	private static final String WORKING_DIRECTORY = "Working Directory";
 	private static final String IS_DATA_CACHING = "Is Data Caching";
 
 	private SetupDialog view = null;
 	private static SetupController instance = null;
 	private Properties properties = new Properties();
 	private boolean loaded = false;
 
 	private ActionListener chooseFileListener = new ActionListener() {
 
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser openDialog = new JFileChooser(new File(DataStreamController.getInstance()
 					.getCurrentDirectory()));
 			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			if (openDialog.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
 				view.getWorkingDirectoryTextField().setText(openDialog.getSelectedFile().getPath());
 			}
 		}
 	};
 
 	private ActionListener okButtonListener = new ActionListener() {
 
 		public void actionPerformed(ActionEvent e) {
 			setConfiguration();
 			view.dispose();
 		}
 	};
 
 	private ActionListener cancelButtonListener = new ActionListener() {
 
 		public void actionPerformed(ActionEvent e) {
 			view.dispose();
 		}
 	};
 
 	private SetupController() {
 
 	}
 
 	public synchronized static SetupController getInstance() {
 		if (instance == null) {
 			instance = new SetupController();
 		}
 		return instance;
 	}
 
 	public synchronized void loadTestCaseConfiguration() {
 		if (this.loaded) {
 			return;
 		}
 		this.loaded = true;
 		String workingDirFilename = FileNameGenerator.getTempDir() + File.separator + "Test";
 		this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
		Boolean isDataCaching = false; // Most unit tests are not working with the caching
 		this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
 	}
 
 	public synchronized void loadConfiguration() {
 		if (this.loaded) {
 			return;
 		}
 		this.loaded = true;
 		String appDir = FileNameGenerator.getApplicationDir();
 		File configFile = new File(appDir + File.separator + "cdktaverna2.config");
 		if (!configFile.exists()) {
 			this.createConfiguration();
 		} else {
 			try {
 				this.properties.load(new FileReader(configFile));
 			} catch (Exception e) {
 				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + configFile.getPath(),
 						this.getClass().getSimpleName(), e);
 				this.createConfiguration();
 			}
 		}
 	}
 
 	private void createConfiguration() {
 		this.showConfigurationDialog();
 	}
 
 	private void showConfigurationDialog() {
 		this.view = new SetupDialog();
 		ImageIcon icon = null;
 		try {
 			ClassLoader cld = getClass().getClassLoader();
 			URL url = cld.getResources("icons/open.gif").nextElement();
 			icon = new ImageIcon(url);
 			this.view.getChooseDirectoryButton().setIcon(icon);
 		} catch (Exception e) {
 			// Ignore. Only the symbol missing.
 		}
 		this.view.getOkButton().addActionListener(this.okButtonListener);
 		this.view.getCancelButton().addActionListener(this.cancelButtonListener);
 		this.view.getChooseDirectoryButton().addActionListener(chooseFileListener);
 		Tools.centerWindowOnScreen(this.view);
 		String workingDirFilename = this.properties.getProperty(WORKING_DIRECTORY);
 		if (workingDirFilename == null) {
 			workingDirFilename = FileNameGenerator.getTempDir();
 			this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
 		}
 		this.view.getWorkingDirectoryTextField().setText(workingDirFilename);
 		Boolean isDataCaching = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_CACHING));
 		if (isDataCaching == null) {
 			isDataCaching = true;
 			this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
 		}
 		this.view.setVisible(true);
 	}
 
 	private void setConfiguration() {
 		String appDir = FileNameGenerator.getApplicationDir();
 		File configFile = new File(appDir + File.separator + "cdktaverna2.config");
 		String workingDirFilename = this.view.getWorkingDirectoryTextField().getText();
 		this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
 		Boolean isDataCaching = this.view.getChckbxCacheDatarecommended().isSelected();
 		this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
 		try {
 			this.properties.store(new FileWriter(configFile), "CDK-Taverna 2.0 properties");
 		} catch (Exception e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + configFile.getPath(),
 					this.getClass().getSimpleName(), e);
 		}
 	}
 
 	public String getWorkingDir() {
 		String path = this.properties.getProperty(WORKING_DIRECTORY);
 		File file = new File(path);
 		if (!file.exists()) {
 			file.mkdir();
 		}
 		return file.getPath() + File.separator;
 	}
 
 	public boolean isDataCaching() {
 		Boolean isDataCaching = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_CACHING));
 		return isDataCaching;
 	}
 }
