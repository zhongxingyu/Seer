 package org.gwaspi.global;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import javax.swing.JOptionPane;
 import javax.xml.parsers.ParserConfigurationException;
 import org.gwaspi.constants.cGlobal;
 import org.gwaspi.database.DerbyDBReshaper;
 import org.gwaspi.gui.StartGWASpi;
 import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
 import org.gwaspi.gui.utils.Dialogs;
 import org.gwaspi.model.StudyList;
 import org.gwaspi.reports.GenericReportGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class Config {
 
 	private static final Logger log = LoggerFactory.getLogger(Config.class);
 
 	public static final String PROPERTY_LAST_OPENED_DIR = "LAST_OPENED_DIR";
 	public static final String PROPERTY_LAST_SELECTED_NODE = "LAST_SELECTED_NODE";
 	public static final String PROPERTY_DATA_DIR = "DataDir";
 	public static final String PROPERTY_GENOTYPES_DIR = "GTdir";
 	public static final String PROPERTY_EXPORT_DIR = "ExportDir";
 	public static final String PROPERTY_REPORTS_DIR = "ReportsDir";
 	public static final String PROPERTY_LOG_DIR = "LogDir";
 	public static final String PROPERTY_CURRENT_GWASPIDB_VERSION = "CURRENT_GWASPIDB_VERSION";
 
 	/** System wide preferences. */
 	private static Preferences prefs = Preferences.userNodeForPackage(Config.class);
 	/**
 	 * Per software (runtime-)instance preferences.
 	 * We use per-thread prefs, initialized with the values from the main thread
 	 * (so they use at least the same data-dir, for example).
 	 */
 	private static final ThreadLocal<Map<String, Object>> instancePrefs =
 			new ThreadLocal<Map<String, Object>>() {
 
 				private Map<String, Object> mainPrefs = null;
 
 				@Override
 				protected Map<String, Object> initialValue() {
 
 					Map<String, Object> prefs;
 
 					if (mainPrefs == null) {
 						// this will be calle for the main thread,
 						// initilaizing the prefs
 						prefs = new HashMap<String, Object>();
 						mainPrefs = prefs;
 					} else {
 						prefs = new HashMap<String, Object>(mainPrefs);
 					}
 
 					return prefs;
 				}
 			};
 
 	private Config() {
 	}
 
 	public static void setConfigValue(String key, Object value) throws IOException {
		if (StartGWASpi.guiMode) {
			// GUI PREFS
			prefs.put(key, value.toString());
		} else {
			// CLI & THREAD PREFS
			instancePrefs.get().put(key, value);
		}
 	}
 
 	public static void setConfigColor(String key, Color value) throws IOException {
 		setConfigValue(key, colorToString(value));
 	}
 
 	public static String colorToString(Color value) {
 		return value.getRed() + "," + value.getGreen() + "," + value.getBlue();
 	}
 
 	public static Color stringToColor(String value) throws IOException {
 
 		String[] split = value.split(",");
 		float[] hsbVals = Color.RGBtoHSB(
 				Integer.parseInt(split[0]),
 				Integer.parseInt(split[1]),
 				Integer.parseInt(split[2]),
 				null);
 		return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
 	}
 
 	public static String getConfigValue(String key, String defaultV) throws IOException {
 
 		String prop = defaultV;
 
 		if (StartGWASpi.guiMode) {
 			// GUI MODE
 			prop = prefs.get(key, defaultV);
 		} else {
 			if (instancePrefs.get().containsKey(key)) {
 				prop = instancePrefs.get().get(key).toString();
 			} else {
 				setConfigValue(key, defaultV);
 			}
 		}
 
 		return prop;
 	}
 
 	public static Color getConfigColor(String key, Color defaultV) throws IOException {
 		return stringToColor(getConfigValue(key, colorToString(defaultV)));
 	}
 
 //	public static int getConfigValue(String key, int defaultV) throws IOException {
 //		int prop = prefs.getInt(key, defaultV);
 //		return prop;
 //	}
 
 	public static void clearConfigFile() throws IOException, BackingStoreException {
		if (StartGWASpi.guiMode) {
 			// GUI MODE
 			prefs.clear();
 		} else {
 			// CLI MODE
 			instancePrefs.get().clear();
 		}
 	}
 
 	public static boolean initPreferences(boolean _startWithGUI, File scriptFile) {
 
 		boolean isInitiated = false;
 		//startWithGUI = _startWithGUI;
 		try {
 			//clearConfigFile();
 			File dirToData = new File(getConfigValue(PROPERTY_DATA_DIR, ""));
 
 			// CHECK FOR RECENT GWASPI VERSION
 			checkUpdates();
 
 			if (_startWithGUI) { // GUI MODE
 				if (dirToData.getPath().equals("")) {
 					JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, Text.App.initText);
 					File dataDir = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION);
 
 					if (dataDir != null) {
 						try {
 							createDataStructure(dataDir);
 							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
 							isInitiated = true;
 						} catch (Exception ex) {
 							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, Text.App.warnUnableToInitForFirstTime);
 							log.error(Text.App.warnUnableToInitForFirstTime, ex);
 						}
 					}
 				} else {
 					File derbyCenter = new File(dirToData.getPath() + "/datacenter");
 					if (!derbyCenter.exists()) {
 						int recreateDataFolder = Dialogs.showOptionDialogue("Data folder unreachable", "The data folder (\"" + dirToData.getAbsolutePath() + "\") is unreachable (deleted?).\nShould GWASpi recreate it or do you want to provide a new path?", "Recreate", "New Path", "Cancel");
 						if (recreateDataFolder == JOptionPane.OK_OPTION) {
 							createDataStructure(dirToData);
 							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
 						} else if (recreateDataFolder == JOptionPane.NO_OPTION) {
 							dirToData = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION);
 							createDataStructure(dirToData);
 							JOptionPane.showMessageDialog(StartGWASpi.mainGUIFrame, "Databases and working folders initialized successfully!");
 						} else if (recreateDataFolder == JOptionPane.CANCEL_OPTION) {
 							throw new RuntimeException("The data folder (\"" + dirToData.getAbsolutePath() + "\") is unreachable, and the user chose not to create it");
 						}
 					}
 
 //					if (getConfigValue(PROPERTY_GENOTYPES_DIR, "").equals("")) {
 					updateConfigDataDirs(dirToData);
 //					} else {
 					setDBSystemDir(derbyCenter.getPath());
 //					}
 
 					isInitiated = true;
 				}
 			} else { // CLI & THREAD MODE
 				if (dirToData.getPath().equals("")) {
 					if (scriptFile != null) {
 						// Use path from script file
 						// 1st line contains data path
 						File dataDir = new File(org.gwaspi.cli.Utils.readDataDirFromScript(scriptFile));
 						log.info("Using database path: {}", dataDir.getAbsolutePath());
 						if (dataDir.exists()) {
 							// assume the existing dir contains a database
 							// with valid sturcutre already
 							initDataBaseVars(dataDir);
 							isInitiated = true;
 						} else {
 							try {
 								createDataStructure(dataDir);
 								isInitiated = true;
 							} catch (Exception ex) {
 								log.error(Text.App.warnUnableToInitForFirstTime, ex);
 							}
 						}
 						if (isInitiated) {
 							log.info("Databases and working folders initialized successfully!");
 						}
 					}
 				} else {
 					if (getConfigValue(PROPERTY_GENOTYPES_DIR, "").equals("")) {
 						updateConfigDataDirs(dirToData);
 					}
 					isInitiated = true;
 				}
 			}
 
 			// ALTER EXISTING DERBY DB TABLES TO SUIT CURRENT GWASPI VERSION
 			DerbyDBReshaper.alterTableUpdates();
 		} catch (Exception ex) {
 			isInitiated = false;
 			log.error("Failed initializing the configuration", ex);
 		}
 
 		return isInitiated;
 	}
 
 	protected static File initDataBaseVars(File dataDir) throws IOException, BackingStoreException, URISyntaxException {
 
 		clearConfigFile();
 		setConfigValue(PROPERTY_DATA_DIR, dataDir.getPath());
 		File derbyCenter = new File(dataDir.getPath() + "/datacenter");
 		setDBSystemDir(derbyCenter.getPath());
 
 		return derbyCenter;
 	}
 
 	public static Document getLocalVersionDom() throws URISyntaxException {
 
 		URL localVersionPath = Config.class.getResource(cGlobal.LOCAL_VERSION_XML);
 		Document localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());
 
 		return localDom;
 	}
 
 	protected static void createDataStructure(File dataDir) throws IOException, BackingStoreException, URISyntaxException {
 
 		File derbyCenter = initDataBaseVars(dataDir);
 
 		if (!derbyCenter.exists()) {
 			org.gwaspi.database.DatabaseGenerator.initDataCenter();
 		}
 
 		Utils.createFolder(dataDir.getPath(), "genotypes");
 		Utils.createFolder(dataDir.getPath(), "help");
 		Utils.createFolder(dataDir.getPath(), "export");
 		Utils.createFolder(dataDir.getPath(), "reports");
 		Utils.createFolder(dataDir.getPath() + "/reports", "log");
 
 		setConfigValue(PROPERTY_GENOTYPES_DIR, dataDir.getPath() + "/genotypes");
 		setConfigValue(PROPERTY_EXPORT_DIR, dataDir.getPath() + "/export");
 		setConfigValue(PROPERTY_REPORTS_DIR, dataDir.getPath() + "/reports");
 		setConfigValue(PROPERTY_LOG_DIR, dataDir.getPath() + "/reports/log");
 
 		Document localDom = getLocalVersionDom();
 		List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
 		setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
 
 		StudyList.createStudyLogFile(0);
 	}
 
 	private static void updateConfigDataDirs(File dataDir) throws IOException, BackingStoreException, URISyntaxException {
 		String lastOpenedDir = getConfigValue(PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
 		String lastSelectedNode = getConfigValue(PROPERTY_LAST_SELECTED_NODE, Text.App.appName);
 
 		String lastMnhttThreshold = Config.getConfigValue(
 					GenericReportGenerator.PLOT_MANHATTAN_THRESHOLD_CONFIG,
 					String.valueOf(GenericReportGenerator.PLOT_MANHATTAN_THRESHOLD_DEFAULT));
 		Color lastMnhttBack = Config.getConfigColor(
 					GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_CONFIG,
 					GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_DEFAULT);
 		Color lastMnhttBackAlt = Config.getConfigColor(
 					GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG,
 					GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_DEFAULT);
 		Color lastMnhttMain = Config.getConfigColor(
 					GenericReportGenerator.PLOT_MANHATTAN_MAIN_CONFIG,
 					GenericReportGenerator.PLOT_MANHATTAN_MAIN_DEFAULT);
 
 		Color lastQQBack = Config.getConfigColor(
 					GenericReportGenerator.PLOT_QQ_BACKGROUND_CONFIG,
 					GenericReportGenerator.PLOT_QQ_BACKGROUND_DEFAULT);
 		Color lastQQActual = Config.getConfigColor(
 					GenericReportGenerator.PLOT_QQ_ACTUAL_CONFIG,
 					GenericReportGenerator.PLOT_QQ_ACTUAL_DEFAULT);
 		Color lastQQMu = Config.getConfigColor(
 					GenericReportGenerator.PLOT_QQ_MU_CONFIG,
 					GenericReportGenerator.PLOT_QQ_MU_DEFAULT);
 		Color lastQQSigma = Config.getConfigColor(
 					GenericReportGenerator.PLOT_QQ_SIGMA_CONFIG,
 					GenericReportGenerator.PLOT_QQ_SIGMA_DEFAULT);
 
 		String lastSampleQAHetzyg = getConfigValue(
 				SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_HETZYG_THRESHOLD_CONFIG,
 				String.valueOf(SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_HETZYG_THRESHOLD_DEFAULT));
 		String lastSampleQAMissingratio = getConfigValue(
 				SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_MISSING_THRESHOLD_CONFIG,
 				String.valueOf(SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_MISSING_THRESHOLD_DEFAULT));
 
 		clearConfigFile();
 		setConfigValue(PROPERTY_DATA_DIR, dataDir.getPath());
 		File derbyCenter = new File(dataDir.getPath() + "/datacenter");
 
 		setConfigValue(PROPERTY_GENOTYPES_DIR, dataDir.getPath() + "/genotypes");
 		setConfigValue(PROPERTY_EXPORT_DIR, dataDir.getPath() + "/export");
 		setConfigValue(PROPERTY_REPORTS_DIR, dataDir.getPath() + "/reports");
 		setConfigValue(PROPERTY_LOG_DIR, dataDir.getPath() + "/reports/log");
 		setConfigValue(PROPERTY_LAST_OPENED_DIR, lastOpenedDir);
 		setConfigValue(PROPERTY_LAST_SELECTED_NODE, lastSelectedNode);
 
 		// SET CHART PREFERENCES
 		setConfigValue(GenericReportGenerator.PLOT_MANHATTAN_THRESHOLD_CONFIG, lastMnhttThreshold);
 		setConfigColor(GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_CONFIG, lastMnhttBack);
 		setConfigColor(GenericReportGenerator.PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG, lastMnhttBackAlt);
 		setConfigColor(GenericReportGenerator.PLOT_MANHATTAN_MAIN_CONFIG, lastMnhttMain);
 
 		setConfigColor(GenericReportGenerator.PLOT_QQ_BACKGROUND_CONFIG, lastQQBack);
 		setConfigColor(GenericReportGenerator.PLOT_QQ_ACTUAL_CONFIG, lastQQActual);
 		setConfigColor(GenericReportGenerator.PLOT_QQ_MU_CONFIG, lastQQMu);
 		setConfigColor(GenericReportGenerator.PLOT_QQ_SIGMA_CONFIG, lastQQSigma);
 
 		setConfigValue(SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_HETZYG_THRESHOLD_CONFIG, lastSampleQAHetzyg);
 		setConfigValue(SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_MISSING_THRESHOLD_CONFIG, lastSampleQAMissingratio);
 
 		Document localDom = getLocalVersionDom();
 		List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
 		setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
 
 		setDBSystemDir(derbyCenter.getPath());
 	}
 
 	public static void checkUpdates() throws IOException, ParseException, ParserConfigurationException, SAXException, URISyntaxException {
 		if (Utils.checkInternetConnection()) {
 			Document localDom = getLocalVersionDom();
 
 			if (localDom != null) { // Found local version info
 				System.setProperty("java.net.useSystemProxies", "true");
 
 				List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
 				setConfigValue(PROPERTY_CURRENT_GWASPIDB_VERSION, XMLParser.getTextValue(localElements.get(0), "GWASpi_DB_Version"));
 
 				URL remoteVersionPath = new URL(cGlobal.REMOTE_VERSION_XML);
 				Document remoteDom = XMLParser.parseXmlFile(remoteVersionPath.toURI().toString());
 
 				if (remoteDom != null) { // Found remote version info
 					// Retrieve data from XML files
 
 					Date localUpdateDate = XMLParser.getDateValue(localElements.get(0), "Date");
 					String localVersionNumber = XMLParser.getTextValue(localElements.get(0), "Number");
 
 					List<Element> remoteElements = XMLParser.parseDocument(remoteDom, "GWASpi");
 					Date remoteUpdateDate = XMLParser.getDateValue(remoteElements.get(0), "Date");
 					String remoteVersionNumber = XMLParser.getTextValue(remoteElements.get(0), "Number");
 					String remoteCompatibilityNumber = XMLParser.getTextValue(remoteElements.get(0), "Compatibility");
 
 					StringBuilder message = new StringBuilder(Text.App.newVersionAvailable);
 					message.append("\nLocal Version: ").append(localVersionNumber);
 					message.append("\nNewest Version: ").append(remoteVersionNumber);
 					message.append("\nUpdate Type: ").append(XMLParser.getTextValue(remoteElements.get(0), "Type"));
 
 					// MAKE VERSION CHECKS
 					if (remoteCompatibilityNumber.compareTo(localVersionNumber) <= 0) { //Remote version is still compatible with local version
 						message.append("\n").append(Text.App.newVersionIsCompatible).append("\n").append(XMLParser.getTextValue(remoteElements.get(0), "ActionCompatible"));
 					} else { // Remote version is NOT compatible with local version
 						message.append("\n").append(Text.App.newVersionIsUnCompatible).append("\n").append(XMLParser.getTextValue(remoteElements.get(0), "ActionUnCompatible"));
 					}
 					message.append("\nChangelog: ").append(XMLParser.getTextValue(remoteElements.get(0), "Description"));
 
 					if (localUpdateDate.compareTo(remoteUpdateDate) < 0) { //Remote version is more recent
 						if (StartGWASpi.guiMode) {
 							Dialogs.showWarningDialogue(message.toString());
 						} else {
 							log.error(message.toString());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static void setDBSystemDir(String dataCenter) throws IOException {
 		// decide on the db system directory
 		System.setProperty("derby.system.home", dataCenter);
 	}
 
 	public static boolean downloadFile(String dwnlUrl, String savePath, String saveName) {
 		boolean result;
 		File saveFile = new File(savePath + "/" + saveName);
 		result = FileDownload.download(dwnlUrl, saveFile.getPath());
 		return result;
 	}
 }
