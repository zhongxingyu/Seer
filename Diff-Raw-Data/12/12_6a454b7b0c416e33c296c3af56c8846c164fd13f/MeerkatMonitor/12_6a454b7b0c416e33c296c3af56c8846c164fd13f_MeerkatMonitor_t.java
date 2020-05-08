 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat;
 
 import java.awt.Menu;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.xml.ws.Endpoint;
 
 import org.apache.log4j.Logger;
 import org.meerkat.group.AppGroupCollection;
 import org.meerkat.gui.AboutDialog;
 import org.meerkat.gui.SplashScreen;
 import org.meerkat.gui.SysTrayIcon;
 import org.meerkat.httpServer.HttpServer;
 import org.meerkat.network.Mail;
 import org.meerkat.network.NetworkUtil;
 import org.meerkat.network.RSS;
 import org.meerkat.sampleData.SampleData;
 import org.meerkat.services.WebApp;
 import org.meerkat.util.DateUtil;
 import org.meerkat.util.FileUtil;
 import org.meerkat.util.MasterKeyManager;
 import org.meerkat.util.PropertiesLoader;
 import org.meerkat.util.ResourceManager;
 import org.meerkat.util.ZipUtil;
 import org.meerkat.util.sql.SQLDriverLoader;
 import org.meerkat.util.xml.XStreamMeerkatConfig;
 import org.meerkat.webapp.WebAppCollection;
 import org.meerkat.webapp.WebAppEvent;
 import org.meerkat.webapp.WebAppResponse;
 import org.meerkat.ws.MeerkatWebService;
 
 import com.thoughtworks.xstream.XStream;
 
 public class MeerkatMonitor {
 
 	private static String version = "0.5.2";
 
 	private static Logger log = Logger.getLogger(MeerkatMonitor.class);
 	private static Mail email = new Mail();
 	private static String smtp, smtpUser, smtpPort, smtpSecurity, smtpPass, to, from, subject;
 	private static Integer testPause, webserverPort;
 	private static Boolean testEmailSending, sendEmails, autosaveOnExit, autoLoadOnStart;
 
 	private static String sessionSaveFile = "meerkatDataSession.mrk";
 	private static String saveFile = "meerkat-session-save.dat";
 
 	private static String propertiesFile = "meerkat.properties";
 	private static String configFile = "meerkat.webapps.xml";
 	private static PropertiesLoader pL;
 	private static Properties properties;
 
 	private static SysTrayIcon systray;
 
 	private static long pauseTime;
 
 	private static WebApp currentWebApp;
 	private static WebAppResponse currentWebAppResponse;
 
 	private static NetworkUtil netUtil = new NetworkUtil();
 	private static String hostname = netUtil.getHostname();
 	private static HttpServer httpWebServer;
 	private static String wsdlUrl = "";
 
 	private static String eventGoOffline = "Offline";
 	private static String eventBackOnline = "Back Online";
 	private static String eventNewMonitoringStart = "";
 	private static String eventStandard = "";
 	private static DateUtil dateUtil = new DateUtil();
 	private static WebAppEvent ev;
 	private static RSS rssFeed;
 	private static String tempWorkingDir = System.getProperty("java.io.tmpdir")	+ System.getProperty("file.separator") + "meerkat/";
 	private static WebAppCollection webAppsCollection = new WebAppCollection();
 	private static AppGroupCollection appGroupCollection = new AppGroupCollection();
 	private static String[] expectedProperties = new String[20];
 	private static MasterKeyManager mkm;
 
 	/**
 	 * main
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// Show splash screen
 		Thread threadSplash = new Thread(new SplashScreen(version));
 		threadSplash.start();
 
 		// Set up embedded jetty log
 		Properties systemProperties = System.getProperties();
 		systemProperties.setProperty("org.eclipse.jetty.LEVEL", "WARN");
 
 		// Set up apache cxf log
 		java.util.logging.Logger jlog = java.util.logging.Logger.getLogger("org.apache.cxf");
 		jlog.setLevel(Level.WARNING);
 
 		// append stdout and stderr to log4j
 		//TODO this should be replaced with an automatic error submission mechanism
 		FileOutputStream fileOutputStream = null;
 		try {
 			File logDir = new File("log");
 			logDir.mkdir();
 			fileOutputStream = new FileOutputStream("log/meerkat-internal.log");
 		} catch (FileNotFoundException e) {
 			log.error("Failed to write internal application errors file.");
 		}
 		PrintStream printStream = new PrintStream(fileOutputStream);
 		System.setErr(printStream); //for redirecting stdout
 
 		// Load SQL Drivers
 		log.info("Loading SQL Drivers...");
 		SQLDriverLoader sqlDL = new SQLDriverLoader();
 		sqlDL.loadDrivers();
 
 
 		// Register required properties
 		// NOTE: The same are reflected in method generateDefaultPropertiesFile
 		// in class PropertiesLoader
 		expectedProperties[0] = "meerkat.email.send.emails";
 		expectedProperties[1] = "meerkat.email.smtp.server";
 		expectedProperties[2] = "meerkat.email.smtp.security";
 		expectedProperties[3] = "meerkat.email.smtp.port";
 		expectedProperties[4] = "meerkat.email.smtp.user";
 		expectedProperties[5] = "meerkat.email.smtp.password";
 		expectedProperties[6] = "meerkat.email.to";
 		expectedProperties[7] = "meerkat.email.from";
 		expectedProperties[8] = "meerkat.email.subjectPrefix";
 		expectedProperties[9] = "meerkat.email.sending.test";
 		expectedProperties[10] = "meerkat.monit.test.time";
 		expectedProperties[11] = "meerkat.webserver.port";
 		expectedProperties[12] = "meerkat.autosave.exit";
 		expectedProperties[13] = "meerkat.autoload.start";
 		expectedProperties[14] = "meerkat.ssl.keystore";
 		expectedProperties[15] = "meerkat.ssl.password";
 		expectedProperties[16] = "meerkat.dashboard.gauge";
 		expectedProperties[17] = "meerkat.webserver.rconfig";
 		expectedProperties[18] = "meerkat.password.master";
 		expectedProperties[19] = "meerkat.webserver.logaccess";
 
 		// Prepare temporary working directory
 		log.info("Setting temp dir: " + tempWorkingDir);
 		webAppsCollection.setTempWorkingDir(tempWorkingDir);
 		webAppsCollection.setAppVersion(version);
 		appGroupCollection.setTempWorkingDir(tempWorkingDir);
 		File tmp = new File(tempWorkingDir);
 		FileUtil fu = new FileUtil();
 		if (tmp.exists()) {
 			fu.deleteDirectory(tmp);
 		}
 		if (!tmp.mkdir()) {
 			log.fatal("FATAL: Cannot create temp directory - " + tmp.toString());
 			System.exit(-1);
 		}
 
 		// Create the RSS Feed
 		rssFeed = new RSS("Meerkat Monitor", "Meerkat Monitor RSS Alerts", "", tmp.getAbsolutePath());
 
 		// Set httpclient log to error only
 		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
 		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
 		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
 
 		readProperties();
 		loadWebAppXML();
 		generateGroups();
 		loadEmailProperties();
 		setLookAndFeel();
 
 		// Create the MasterKeyManager
 		mkm = new MasterKeyManager(propertiesFile, webAppsCollection);
 
 		startWebServices();
 		startHttpWebServer();
 		createSystray();
 		extractLocalResources();
 		startMonitor();
 	}
 
 
 	/**
 	 * startWebServices
 	 */
 	public static void startWebServices(){
 		// Webservice - related to org.meerkat.ws package
 		String wsdlEndpoint = "";
 		wsdlEndpoint = "http://"+hostname+":"+(webserverPort+1)+"/ws/manager";
 		wsdlUrl = wsdlEndpoint+"?wsdl";
 
 		// Setup web server
 		httpWebServer = new HttpServer(webserverPort, version, wsdlUrl, tempWorkingDir);
 		Endpoint.publish(wsdlEndpoint, new MeerkatWebService(mkm, webAppsCollection, httpWebServer));	
 	}
 
 
 	/**
 	 * readProperties
 	 */
 	private static void readProperties() {
 		pL = new PropertiesLoader();
 
 		File prop = new File(propertiesFile);
 		if (!prop.exists()) {
 			log.warn("Properties file not present!");
 			// Create a new empty one
 			pL.generateDefaultPropertiesFile(propertiesFile);
 		}
 
 		properties = pL.getPropetiesFromFile(propertiesFile);
 		// Validate required properties
 		pL.propertiesValidator(expectedProperties);
 
 	}
 
 	/**
 	 * loadWebAppXML
 	 */
 	private static void loadWebAppXML() {
 		FileUtil fu = new FileUtil();
 
 		XStreamMeerkatConfig xstreamConfig = new XStreamMeerkatConfig();
 		XStream xstream = xstreamConfig.getXstream();
 
 		// Create empty config file if not present
 		File config = new File(configFile);
 		if (!config.exists()) {
 			fu.createEmptyXMLConfigFile(configFile);
 		}
 
 		try {
 			webAppsCollection = (WebAppCollection) xstream.fromXML(fu.readFileContents(configFile));
 
 			// Validate if we got and invalid Meerkat-Monitor file
 			if (webAppsCollection.getWebAppCollectionSize() == null) {
 				log.fatal("The application XML file is invalid!");
 				log.fatal("Create an empty one and restart application.");
 			}
 
 		} catch (Exception e) {
 			log.warn("Unable to load applications from config xml!");
 			log.warn("Considering that is empty.");
 			webAppsCollection = new WebAppCollection();
 
 			// Add Meerkat Monitor self test demo data
 			webAppsCollection.addWebApp(SampleData.getSampleWebApp_SelfTestWSDL());
 			webAppsCollection.addWebApp(SampleData.getSampleWebService_SelfWSgetVersion(version));
 			webAppsCollection.addWebApp(SampleData.getSampleSocketService_SelfHTTP_Port());
 		}
 
 		webAppsCollection.setConfigFile(configFile);
 		webAppsCollection.initialize(version, tempWorkingDir, configFile);
 
 		Iterator<WebApp> waI = webAppsCollection.getWebAppCollectionIterator();
 		WebApp wapp;
 		log.info("-- Applications --");
 		while (waI.hasNext()) {
 			wapp = waI.next();
 			log.info("Added: " + wapp.getName());
 			wapp.initialize(tempWorkingDir, version);
 		}
 	}
 
 	/**
 	 * generateGroups
 	 */
 	private static void generateGroups() {
 		// Get boolean to enable/disable home Groups Gauge
 		// displayHomeGroupsGauge =
 		// Boolean.parseBoolean(properties.getProperty("meerkat.dashboard.gauge"));
 		appGroupCollection.populateGroups(webAppsCollection);
 	}
 
 	/**
 	 * loadEmailProperties
 	 */
 	private static void loadEmailProperties() {
 		// Get properties to email sending
 		sendEmails = Boolean.parseBoolean(properties
 				.getProperty("meerkat.email.send.emails"));
 
 		if (sendEmails) {
 			smtp = properties.getProperty("meerkat.email.smtp.server");
 			smtpPort = properties.getProperty("meerkat.email.smtp.port");
 			smtpSecurity = properties.getProperty("meerkat.email.smtp.security");
 			smtpUser = properties.getProperty("meerkat.email.smtp.user");
 			smtpPass = properties.getProperty("meerkat.email.smtp.password");
 			to = properties.getProperty("meerkat.email.to");
 			from = properties.getProperty("meerkat.email.from");
 			subject = properties.getProperty("meerkat.email.subjectPrefix");
 			testEmailSending = Boolean.parseBoolean(properties.getProperty("meerkat.email.sending.test"));
 		}
 
 		testPause = Integer.parseInt(properties.getProperty("meerkat.monit.test.time"));
 		// Convert seconds to milliseconds
 		pauseTime = TimeUnit.MINUTES.toMillis(testPause);
 
 		webserverPort = Integer.parseInt(properties.getProperty("meerkat.webserver.port"));
 
 		// Test email sending if true in properties
 		if (sendEmails && testEmailSending) {
 			email = new Mail(smtp, smtpUser, smtpPass, smtpPort, smtpSecurity, to, from);
 			email.testEmailServer();
 			log.info("Sent a test email to: " + to);
 		}
 	}
 
 	/**
 	 * setLookAndFeel
 	 */
 	private static void setLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			log.error("", e);
 		} catch (InstantiationException e) {
 			log.error("", e);
 		} catch (IllegalAccessException e) {
 			log.error("", e);
 		} catch (UnsupportedLookAndFeelException e) {
 			log.error("", e);
 		}
 
 	}
 
 	/**
 	 * createSystray
 	 */
 	private static void createSystray() {
 		// Create the icon tray
 		systray = new SysTrayIcon(mkm);
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				systray.setWebServerPort(webserverPort);
 				systray.setWebAppCollection(webAppsCollection);
 				systray.setGroupsCollection(appGroupCollection);
 				systray.setHttpServer(httpWebServer);
 
 				Menu sessionOperations = new Menu("Save/Load Session");
 				// Add save session button
 				ActionListener saveSession = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						systray.showMessage(
 								"Saving Session",
 								"Please wait. It may take several minutes to save data...\nYou'll be notified when finished.");
 						MeerkatMonitor.saveSession();
 						systray.showMessage("Save Session", "Session saved: "
 								+ webAppsCollection.getWebAppCollectionSize()
 								+ " apps.");
 					}
 				};
 
 				// Add load session button
 				ActionListener loadSession = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						systray.showMessage(
 								"Load Session",
 								"Please wait. It may take several minutes to load data...\nYou'll be notified when finished.");
 						MeerkatMonitor.loadSession();
 						systray.showMessage(
 								"Load Session",
 								"Loaded session successfully: "
 										+ webAppsCollection
 										.getWebAppCollectionSize()
 										+ " apps.");
 					}
 				};
 
 				// Reload Configuration
 				/**
 				 * ActionListener reloadConfigListener = new ActionListener() {
 				 * public void actionPerformed(ActionEvent e) {
 				 * reloadConfiguration();
 				 * systray.showMessage("Configuration Reloaded",
 				 * "Please wait for next test round to gather data"); } };
 				 */
 
 				// Exit
 				ActionListener exitListener = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						// Save session if enable in properties
 						autosaveOnExit = Boolean.parseBoolean(properties
 								.getProperty("meerkat.autosave.exit"));
 						if (autosaveOnExit) {
 							log.info("Auto save on exit enable.");
 							saveSession();
 							log.info("Session saved\n");
 						}
 						// Save XML Config File
 						webAppsCollection.saveConfigXMLFile();
 						System.exit(0);
 					}
 				};
 
 				// About button
 				ActionListener aboutListener = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						AboutDialog ab = new AboutDialog(version);
 						ab.showUp();
 					}
 				};
 
 				MenuItem save = new MenuItem("Save Session");
 				save.addActionListener(saveSession);
 
 				MenuItem load = new MenuItem("Load Session");
 				load.addActionListener(loadSession);
 
 				MenuItem aboutm = new MenuItem("About");
 				aboutm.addActionListener(aboutListener);
 
 				sessionOperations.add(save);
 				sessionOperations.add(load);
 				// systray.addSeparator();
 				// systray.addMenuItem("Reload Configuration",
 				// reloadConfigListener);
 				systray.addMenu(sessionOperations);
 				systray.addSeparator();
 				systray.addMenuItem("About", aboutListener);
 				systray.addSeparator();
 				systray.addMenuItem("Exit", exitListener);
 			}
 		});
 	}
 
 	/**
 	 * startHttpWebServer
 	 */
 	private static void startHttpWebServer() {
 		// Set the data
 		httpWebServer.setDataSources(webAppsCollection, appGroupCollection);
 
 		// Create the RSS Feed
 		rssFeed.refreshRSSFeed();
 		rssFeed.setServerPort(webserverPort);
 	}
 
 	/**
 	 * saveSession
 	 */
 	private static void saveSession() {
 		systray.removeSystrayIcon();
 		systray.showStaticInfoIcon("Saving session...");
 
 		httpWebServer.createStartupPage("Saving session. Please wait...");
 
 		ObjectOutput out = null;
 		try {
 			out = new ObjectOutputStream(new FileOutputStream(tempWorkingDir + sessionSaveFile));
 		} catch (FileNotFoundException e) {
 			log.error("Session file not found", e);
 		} catch (IOException e) {
 			log.error("IO exception in session file", e);
 		}
 		try {
 			out.writeObject(webAppsCollection);
 		} catch (IOException e) {
 			log.error("IO exception writting session file", e);
 		}
 		try {
 			out.close();
 		} catch (IOException e) {
 			log.error("IO exception closing session file", e);
 		}
 
 		FileUtil fu = new FileUtil();
 
 		String monitTypes[] = fu.getFiletypeListFromDir(tempWorkingDir, ".txt");
 		String allDataFiles[] = new String[monitTypes.length + 1];
 
 		for (int i = 0; i < monitTypes.length; i++) {
 			allDataFiles[i] = monitTypes[i];
 		}
 
 		int lasPos = allDataFiles.length;
 		allDataFiles[lasPos - 1] = tempWorkingDir + sessionSaveFile;
 
 		ZipUtil ziputil = new ZipUtil();
 		ziputil.createZip(saveFile, allDataFiles);
 
 		// Refresh Meerkat page
 		httpWebServer.refreshIndex();
 
 		// Refresh webapp's TimeLine
 		webAppsCollection.writeWebAppCollectionDataFile();
 		systray.addSystrayIcon();
 		log.info("Save Session: saved "+ webAppsCollection.getWebAppCollectionSize() + " objects");
 	}
 
 	/**
 	 * loadSession
 	 */
 	private static void loadSession() {
 
 		systray.removeSystrayIcon();
 		systray.showStaticInfoIcon("Loading session...");
 		httpWebServer.createStartupPage("Loading session. Please wait...");
 
 		// restore data files
 		ZipUtil ziputil = new ZipUtil();
 		ziputil.unzip(saveFile, tempWorkingDir);
 
 		ObjectInput in = null;
 		try {
 			in = new ObjectInputStream(new FileInputStream(tempWorkingDir
 					+ sessionSaveFile));
 		} catch (FileNotFoundException e) {
 			log.error("Session file not found", e);
 			log.error("Disable autoload and start a new session");
 			System.exit(-1);
 		} catch (IOException e) {
 			log.error("IO exception reading session file", e);
 			System.exit(-1);
 		}
 		try {
 			webAppsCollection = null;
 			webAppsCollection = (WebAppCollection) in.readObject();
 		} catch (ClassNotFoundException e) {
 			log.error("Class not found exception", e);
 			System.exit(-1);
 		} catch (IOException e) {
 			log.error("IO exception reading objects on session file", e);
 			System.exit(-1);
 		}
 		try {
 			in.close();
 		} catch (IOException e) {
 			log.error("IO exception closing session file", e);
 			System.exit(-1);
 		}
 
 		// Refresh values in the WebApps
		Iterator<WebApp> refreshT = webAppsCollection.getWebAppCollectionIterator();
 		WebApp wa;
 		while (refreshT.hasNext()) {
 			wa = refreshT.next();
 			wa.writeWebAppVisualizationDataFile();
 		}
 
 		// Refresh Meerkat page
 		// Refresh webapp's TimeLine
 		webAppsCollection.writeWebAppCollectionDataFile();
 
 		// Generate groups
 		appGroupCollection = new AppGroupCollection();
 		generateGroups();
		
		// Update references
		httpWebServer.setDataSources(webAppsCollection, appGroupCollection); 
		httpWebServer.refreshIndex(); // Update dashboard
 
 		systray.addSystrayIcon();
 		log.info("Load Session: loaded "+ webAppsCollection.getWebAppCollectionSize() + " objects");
 	}
 
 	/**
 	 * startMonitor
 	 */
 	private static void startMonitor() {
 		webAppsCollection.saveConfigXMLFile();
 		httpWebServer.refreshIndex();
 
 		log.info("");
 		log.info("Meerkat Monitor v." + version + " started.");
 		systray.showMessage("Meerkat Monitor", "Meerkat Monitor is up and running!");
 
 		// Autoload last session if enabled in properties
 		autoLoadOnStart = Boolean.parseBoolean(properties.getProperty("meerkat.autoload.start"));
 		if (autoLoadOnStart) {
 			log.info("Autoload on start enable. Loading last session..");
 			loadSession();
 			log.info("Session loaded!");
 		}
 
 		List<WebApp> webAppListCopy = webAppsCollection.getCopyWebApps();
 		Iterator<WebApp> i = webAppListCopy.iterator();
 
 		// Prevent dashboard link from giving 404 at first round
 		while (i.hasNext()) {
 			currentWebApp = i.next();
 			currentWebApp.writeWebAppVisualizationDataFile(); 
 		}
 
 		// Launch the monitor process
 		while (true) {
 			// Check for updated user defined new properties
 			readProperties();
 			loadEmailProperties();
 
 			log.info("[Starting test round]");
 
 			// We iterate over a copy of the collection so we can prevent
 			// ConcurrentModificationException from the GUI
 			webAppListCopy = webAppsCollection.getCopyWebApps();
 			i = webAppListCopy.iterator();
 			int numberOfApps = webAppsCollection.getWebAppCollectionSize();
 			int percent = 0;
 			int roundCompletedApps = 1;
 
 			while (i.hasNext()) {
 				percent = roundCompletedApps * 100 / numberOfApps;
 				currentWebApp = i.next();
 				log.info("["+roundCompletedApps+"/"+numberOfApps+" "+percent+"%]\t"+currentWebApp.getName());
 
 				// check if the webApp is ready to monit or not - temp created in the gui
 				if (currentWebApp.isActive()) {
 					currentWebAppResponse = new WebAppResponse();
 
 					// Check the status
 					currentWebAppResponse = currentWebApp.checkWebAppStatus();
 
 					// First test to set the lastStatus
 					if (currentWebAppResponse.isOnline()
 							&& currentWebApp.getlastStatus().equalsIgnoreCase("NA")) {
 						currentWebApp.setlastStatus("online");
 						currentWebApp.increaseNumberOfTests();
 
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								false,
 								now,
 								"100",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventNewMonitoringStart, tempWorkingDir);
 						// Save load time and latency
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 						ev.setLatency(currentWebApp.getLatency());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(), currentWebApp);
 						currentWebApp.addEvent(ev);
 
 					} else if (!currentWebAppResponse.isOnline()
 							&& currentWebApp.getlastStatus().equalsIgnoreCase("NA")) {
 						currentWebApp.setlastStatus("offline");
 
 						log.warn("OFFLINE\t| " + currentWebApp.getName()+ "\t | " + currentWebApp.getUrl());
 						currentWebApp.increaseNumberOfTests();
 						currentWebApp.increaseNumberOfOfflines();
 
 						if (sendEmails) {
 							email = new Mail(smtp, smtpUser, smtpPass, smtpPort, smtpSecurity, to, from);
 							email.setSubject(subject + " - "+ currentWebApp.getName() + " is OFFLINE");
 							email.setMessage("The webapp "+ currentWebApp.getName() + " on "+ currentWebApp.getUrl() + " is OFFLINE!");
 							email.sendEmail();
 						}
 
 						systray.showMessageError(currentWebApp.getName(), currentWebApp.getName() + " is OFFLINE!");
 
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								true,
 								now,
 								"0",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventNewMonitoringStart, tempWorkingDir);
 						// Save load time and latency
 						ev.setLatency(currentWebApp.getLatency());
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(), currentWebApp);
 						currentWebApp.addEvent(ev);
 						// httpWebServer.addEventResponse(currentWebApp);
 
 						// Add RSS item
 						rssFeed.addItem(currentWebApp.getName(), currentWebApp.getDataFileName(), now, currentWebApp.getName() + " is OFFLINE");
 
 						// Execute the executeOnOffline
 						if (!currentWebApp.getExecuteOnOffline().equalsIgnoreCase("")) {
 							systray.showMessage(currentWebApp.getName(), "Taking action on offline: "+ currentWebApp.getName());
 							currentWebApp.executeOfflineAction();
 						}
 					}
 
 					// If last status was online
 					else if (currentWebAppResponse.isOnline() && currentWebApp.getlastStatus().equalsIgnoreCase("online")) {
 						currentWebApp.setlastStatus("online");
 						currentWebApp.increaseNumberOfTests();
 
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								false,
 								now,
 								"100",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventStandard, tempWorkingDir);
 						// Save load time and latency
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 						ev.setLatency(currentWebApp.getLatency());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(), currentWebApp);
 						currentWebApp.addEvent(ev);
 
 					} else if (!currentWebAppResponse.isOnline()
 							&& currentWebApp.getlastStatus().equalsIgnoreCase("online")) {
 						currentWebApp.setlastStatus("offline");
 
 						log.warn("OFFLINE\t| " + currentWebApp.getName()+ "\t | " + currentWebApp.getUrl());
 						currentWebApp.increaseNumberOfTests();
 						currentWebApp.increaseNumberOfOfflines();
 
 						if (sendEmails) {
 							email = new Mail(smtp, smtpUser, smtpPass,smtpPort, smtpSecurity, to, from);
 							email.setSubject(subject + currentWebApp.getName()+ " is OFFLINE");
 							email.setMessage("The webapp "+ currentWebApp.getName() + " on "+ currentWebApp.getUrl() + " is OFFLINE!");
 							email.sendEmail();
 						}
 
 						systray.showMessageError(currentWebApp.getName(),currentWebApp.getName() + " is OFFLINE!");
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								true,
 								now,
 								"0",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventGoOffline, tempWorkingDir);
 						// Save load time and latency
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 						ev.setLatency(currentWebApp.getLatency());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(),currentWebApp);
 						currentWebApp.addEvent(ev);
 
 						// Add RSS item
 						rssFeed.addItem(currentWebApp.getName(),currentWebApp.getDataFileName(), now, currentWebApp.getName() + " is OFFLINE");
 
 						// Execute the executeOnOffline
 						if (!currentWebApp.getExecuteOnOffline().equalsIgnoreCase("")) {
 							systray.showMessageError(currentWebApp.getName(), "Taking action on offline: "+ currentWebApp.getName());
 							currentWebApp.executeOfflineAction();
 						}
 					}
 
 					// If last status was offline
 					else if (currentWebAppResponse.isOnline() && currentWebApp.getlastStatus().equalsIgnoreCase("offline")) {
 						currentWebApp.setlastStatus("online");
 						currentWebApp.increaseNumberOfTests();
 
 						if (sendEmails) {
 							email = new Mail(smtp, smtpUser, smtpPass, smtpPort, smtpSecurity, to, from);
 							email.setSubject(subject + currentWebApp.getName()+ " is BACK ONLINE");
 							email.setMessage("The webapp "+ currentWebApp.getName() + " on "+ currentWebApp.getUrl()+ " is BACK ONLINE!");
 							email.sendEmail();
 						}
 
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								true,
 								now,
 								"100",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventBackOnline, tempWorkingDir);
 						// Save load time and latency
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 						ev.setLatency(currentWebApp.getLatency());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(),currentWebApp);
 						currentWebApp.addEvent(ev);
 						// httpWebServer.addEventResponse(currentWebApp);
 
 						systray.showMessage(currentWebApp.getName(),currentWebApp.getName() + " is BACK ONLINE!");
 
 						// Add RSS item
 						rssFeed.addItem(currentWebApp.getName(),currentWebApp.getDataFileName(), now,currentWebApp.getName() + " is BACK ONLINE!");
 
 					} else if (!currentWebAppResponse.isOnline()&& currentWebApp.getlastStatus().equalsIgnoreCase("offline")) {
 						currentWebApp.setlastStatus("offline");
 
 						log.warn("STILL OFFLINE\t| " + currentWebApp.getName()+ "\t | " + currentWebApp.getUrl());
 						currentWebApp.increaseNumberOfTests();
 						currentWebApp.increaseNumberOfOfflines();
 
 						// Add event
 						String now = dateUtil.now();
 						ev = new WebAppEvent(
 								false,
 								now,
 								"0",
 								Double.toString(currentWebApp.getAvailability()),
 								currentWebAppResponse.getHttpStatus(),
 								eventStandard, tempWorkingDir);
 						// Save load time and latency
 						ev.setPageLoadTime(currentWebAppResponse.getPageLoadTime());
 						ev.setLatency(currentWebApp.getLatency());
 
 						// Set the response
 						ev.setCurrentResponseGlobal(currentWebApp.getCurrentResponse(),currentWebApp);
 						currentWebApp.addEvent(ev);
 
 						// Execute the executeOnOffline
 						if (!currentWebApp.getExecuteOnOffline().equalsIgnoreCase("")) {
 							systray.showMessageError(currentWebApp.getName(),"Taking action on still offline: "+ currentWebApp.getName());
 							currentWebApp.executeOfflineAction();
 						}
 					}
 				}
 				// Refresh dashboard and app in every app cycle
 				webAppsCollection.writeWebAppCollectionDataFile();
 				httpWebServer.refreshIndex();
 
 				// increase checked apps
 				roundCompletedApps++;
 			}
 			// reset percentage
 			percent = 0;
 			roundCompletedApps = 0;
 
 			httpWebServer.setDataSources(webAppsCollection, appGroupCollection);
 
 			// Get the time between rounds
 			properties = pL.getPropetiesFromFile(propertiesFile);
 			testPause = Integer.parseInt(properties.getProperty("meerkat.monit.test.time"));
 			pauseTime = TimeUnit.MINUTES.toMillis(testPause);
 
 			log.info("Next round in: " + testPause + " minute(s) [" + pauseTime+ "ms]");
 			log.info("");
 			try {
 				Thread.sleep(pauseTime);
 			} catch (InterruptedException e) {
 				log.fatal("Error in the running thread.", e);
 			}
 
 			// Refresh systray
 			systray.reloadSystray();
 		}
 
 	}
 
 	/**
 	 * extractLocalResources
 	 */
 	private static void extractLocalResources() {
 		// Create temp dir to hold the resources
 		File resourcesDir = new File(tempWorkingDir + "resources" + "/"+ "images");
 		if (!resourcesDir.mkdirs()) {
 			log.error("ERROR: Failed to create resources directory!");
 		}
 
 		// Register resources
 		String[] resources = new String[23];
 		resources[0] = "resources/demo_page.css";
 		resources[1] = "resources/demo_table_jui.css";
 		resources[2] = "resources/jquery-ui-1.8.4.custom.css";
 		resources[3] = "resources/favicon.ico";
 		resources[4] = "resources/tango_blue.gif";
 		resources[5] = "resources/tango_red_anime.gif";
 		resources[6] = "resources/jquery.dataTables.js";
 		resources[7] = "resources/jquery.js";
 		resources[8] = "resources/meerkat.png";
 		resources[9] = "resources/meerkat-small.png";
 		resources[10] = "resources/tango_rss.png";
 		resources[11] = "resources/tango_timeline.png";
 		resources[12] = "resources/tango_edit-find.png";
 		resources[13] = "resources/tango-previous.png";
 		resources[14] = "resources/down-green.png";
 		resources[15] = "resources/up-red.png";
 		resources[16] = "resources/down-red.png";
 		resources[17] = "resources/up-green.png";
 		resources[18] = "resources/tango-slink.png";
 		resources[19] = "resources/404_meerkat.png";
 		resources[20] = "resources/tango_wsdl.png";
 		resources[21] = "resources/tango-xml-config.png";
 		resources[22] = "resources/tango-find-log.png";
 
 		String[] resourcesImages = new String[13];
 		resourcesImages[0] = "resources/images/ui-bg_flat_0_aaaaaa_40x100.png";
 		resourcesImages[1] = "resources/images/ui-bg_flat_75_ffffff_40x100.png";
 		resourcesImages[2] = "resources/images/ui-bg_glass_55_fbf9ee_1x400.png";
 		resourcesImages[3] = "resources/images/ui-bg_glass_65_ffffff_1x400.png";
 		resourcesImages[4] = "resources/images/ui-bg_glass_75_dadada_1x400.png";
 		resourcesImages[5] = "resources/images/ui-bg_glass_75_e6e6e6_1x400.png";
 		resourcesImages[6] = "resources/images/ui-bg_glass_95_fef1ec_1x400.png";
 		resourcesImages[7] = "resources/images/ui-bg_highlight-soft_75_cccccc_1x100.png";
 		resourcesImages[8] = "resources/images/ui-icons_2e83ff_256x240.png";
 		resourcesImages[9] = "resources/images/ui-icons_222222_256x240.png";
 		resourcesImages[10] = "resources/images/ui-icons_454545_256x240.png";
 		resourcesImages[11] = "resources/images/ui-icons_888888_256x240.png";
 		resourcesImages[12] = "resources/images/ui-icons_cd0a0a_256x240.png";
 
 		ResourceManager rm;
 		// Extract resources
 		for (int i = 0; i < resources.length; i++) {
 			rm = new ResourceManager(resources[i], tempWorkingDir);
 			rm.getResource();
 			// move the favicon to the dir root
 			if (resources[i].contains("favicon.ico")) {
 				FileUtil fu = new FileUtil();
 				fu.moveFileToDir(tempWorkingDir + resources[i], tempWorkingDir);
 			}
 		}
 
 		// Extract resource/images
 		for (int i = 0; i < resourcesImages.length; i++) {
 			rm = new ResourceManager(resourcesImages[i], tempWorkingDir);
 			rm.getResource();
 		}
 	}
 
 }
