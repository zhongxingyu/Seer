 /**
  * Copyright 2013 by ATLauncher and Contributors
  *
  * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
  * share this software with others as long as you credit us by linking to our
  * website at http://www.atlauncher.com. You also cannot modify the application
  * in any way or make commercial use of this software.
  *
  * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
  */
 package com.atlauncher.data;
 
 import java.awt.Window;
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.atlauncher.App;
 import com.atlauncher.Update;
 import com.atlauncher.exceptions.InvalidPack;
 import com.atlauncher.gui.BottomBar;
 import com.atlauncher.gui.InstancesPanel;
 import com.atlauncher.gui.LauncherConsole;
 import com.atlauncher.gui.PacksPanel;
 import com.atlauncher.gui.Utils;
 
 /**
  * Settings class for storing all data for the Launcher and the settings of the user
  * 
  * @author Ryan
  */
 public class Settings {
 
     // Users Settings
     private Language language; // Language for the Launcher
     private Server server; // Server to use for the Launcher
     private int ram; // RAM to use when launching Minecraft
     private int permGen; // PermGenSize to use when launching Minecraft in MB
     private int windowWidth; // Width of the Minecraft window
     private int windowHeight; // Height of the Minecraft window
     private String javaParamaters; // Extra Java paramaters when launching Minecraft
     private boolean enableConsole; // If to show the console by default
     private boolean enableLeaderboards; // If to enable the leaderboards
     private boolean enableLogs; // If to enable logs
     private Account account; // Account using the Launcher
 
     // Packs, Addons, Instances and Accounts
     private ArrayList<Pack> packs = new ArrayList<Pack>(); // Packs in the Launcher
     private ArrayList<Instance> instances = new ArrayList<Instance>(); // Users Installed Instances
     private ArrayList<Addon> addons = new ArrayList<Addon>(); // Addons in the Launcher
     private ArrayList<Account> accounts = new ArrayList<Account>(); // Accounts in the Launcher
 
     // Directories and Files for the Launcher
     private File baseDir = new File(System.getProperty("user.dir"));
     private File backupsDir = new File(baseDir, "Backups");
     private File configsDir = new File(baseDir, "Configs");
     private File imagesDir = new File(configsDir, "Images");
     private File skinsDir = new File(imagesDir, "Skins");
     private File jarsDir = new File(configsDir, "Jars");
     private File resourcesDir = new File(configsDir, "Resources");
     private File librariesDir = new File(configsDir, "Libraries");
     private File languagesDir = new File(configsDir, "Languages");
     private File downloadsDir = new File(baseDir, "Downloads");
     private File instancesDir = new File(baseDir, "Instances");
     private File serversDir = new File(baseDir, "Servers");
     private File tempDir = new File(baseDir, "Temp");
     private File instancesDataFile = new File(configsDir, "instancesdata");
     private File userDataFile = new File(configsDir, "userdata");
     private File propertiesFile = new File(configsDir, "ATLauncher.conf"); // File for properties
 
     // Launcher Settings
     private JFrame parent; // Parent JFrame of the actual Launcher
     private Properties properties = new Properties(); // Properties to store everything in
     private LauncherConsole console = new LauncherConsole(); // Load the Launcher's Console
     private ArrayList<Language> languages = new ArrayList<Language>(); // Languages for the Launcher
     private ArrayList<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
     private InstancesPanel instancesPanel; // The instances panel
     private PacksPanel packsPanel; // The packs panel
     private BottomBar bottomBar; // The bottom bar
     private boolean firstTimeRun = false; // If this is the first time the Launcher has been run
     private Server bestConnectedServer; // The best connected server for Auto selection
     private boolean offlineMode = false; // If offline mode is enabled
     private Process minecraftProcess = null; // The process minecraft is running on
     private String version = "%VERSION%"; // Version of the Launcher
 
     public Settings() {
         checkFolders(); // Checks the setup of the folders and makes sure they're there
         clearTempDir(); // Cleans all files in the Temp Dir
     }
 
     public void loadEverything() {
         setupServers(); // Setup the servers available to use in the Launcher
         testServers(); // Test servers for best connected one
         loadServerProperty(); // Get users Server preference
         if (!isInOfflineMode()) {
             checkForUpdatedFiles(); // Checks for updated files on the server
         }
         loadLanguages(); // Load the Languages available in the Launcher
         loadPacks(); // Load the Packs available in the Launcher
         loadAddons(); // Load the Addons available in the Launcher
         loadInstances(); // Load the users installed Instances
         loadAccounts(); // Load the saved Accounts
         loadProperties(); // Load the users Properties
         console.setupLanguage(); // Setup language on the console
     }
 
     public void downloadUpdate() {
         try {
             File thisFile = new File(Update.class.getProtectionDomain().getCodeSource()
                     .getLocation().getPath());
             String path = thisFile.getCanonicalPath();
             path = URLDecoder.decode(path, "UTF-8");
             String toget;
             String saveAs = thisFile.getName();
             if (path.contains(".exe")) {
                 toget = "exe";
             } else {
                 toget = "jar";
             }
             File newFile = new File(getTempDir(), saveAs);
             new Downloader(getFileURL("ATLauncher." + toget), newFile.getAbsolutePath()).run(); // Download
                                                                                                 // it
             runUpdate(path, newFile.getAbsolutePath());
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     public void runUpdate(String currentPath, String temporaryUpdatePath) {
         List<String> arguments = new ArrayList<String>();
 
         String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                 + "java";
         if (Utils.isWindows()) {
             path += "w";
         }
         arguments.add(path);
         arguments.add("-cp");
         arguments.add(temporaryUpdatePath);
         arguments.add("com.atlauncher.Update");
         arguments.add(currentPath);
         arguments.add(temporaryUpdatePath);
 
         ProcessBuilder processBuilder = new ProcessBuilder();
         processBuilder.command(arguments);
 
         try {
             processBuilder.start();
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
 
         System.exit(0);
     }
 
     /**
      * This checks the servers hashes.xml file and downloads and new/updated files that differ from
      * what the user has
      */
     private void checkForUpdatedFiles() {
         String hashes = Utils.urlToString(getFileURL("launcher/hashes.xml"));
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new InputSource(new StringReader(hashes)));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName("hash");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     String name = element.getAttribute("name");
                     String type = element.getAttribute("type");
                     String md5 = element.getAttribute("md5");
                     File file = null;
                     if (type.equalsIgnoreCase("Root")) {
                         file = new File(configsDir, name);
                     } else if (type.equalsIgnoreCase("Images")) {
                         file = new File(imagesDir, name);
                         name = "images/" + name;
                     } else if (type.equalsIgnoreCase("Skins")) {
                         file = new File(skinsDir, name);
                         name = "skins/" + name;
                     } else if (type.equalsIgnoreCase("Languages")) {
                         file = new File(languagesDir, name);
                         name = "languages/" + name;
                     } else if (type.equalsIgnoreCase("Launcher")) {
                         String version = element.getAttribute("version");
                         if (!getVersion().equalsIgnoreCase(version)) {
                             if (getVersion().equalsIgnoreCase("%VERSION%")) {
                                 continue; // Don't even think about updating my unbuilt copy
                             }
                            downloadUpdate();
                         } else {
                             continue;
                         }
                     } else {
                         continue; // Don't know what to do with this file so ignore it
                     }
                     boolean download = false; // If we have to download the file or not
                     if (!file.exists()) {
                         download = true; // File doesn't exist so download it
                     } else {
                         if (!Utils.getMD5(file).equalsIgnoreCase(md5)) {
                             download = true; // MD5 hashes don't match so download it
                         }
                     }
 
                     if (download) {
                         new Downloader(getFileURL("launcher/" + name), file.getAbsolutePath())
                                 .run();
                     }
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Checks the directory to make sure all the necessary folders are there
      */
     private void checkFolders() {
         File[] files = { backupsDir, configsDir, imagesDir, skinsDir, jarsDir, resourcesDir,
                 librariesDir, languagesDir, downloadsDir, instancesDir, serversDir, tempDir };
         for (File file : files) {
             if (!file.exists()) {
                 file.mkdir();
             }
         }
     }
 
     /**
      * Returns the base directory
      * 
      * @return File object for the base directory
      */
     public File getBaseDir() {
         return this.baseDir;
     }
 
     /**
      * Returns the backups directory
      * 
      * @return File object for the backups directory
      */
     public File getBackupsDir() {
         return this.backupsDir;
     }
 
     /**
      * Returns the configs directory
      * 
      * @return File object for the configs directory
      */
     public File getConfigsDir() {
         return this.configsDir;
     }
 
     /**
      * Returns the images directory
      * 
      * @return File object for the images directory
      */
     public File getImagesDir() {
         return this.imagesDir;
     }
 
     /**
      * Returns the skins directory
      * 
      * @return File object for the skins directory
      */
     public File getSkinsDir() {
         return this.skinsDir;
     }
 
     /**
      * Returns the jars directory
      * 
      * @return File object for the jars directory
      */
     public File getJarsDir() {
         return this.jarsDir;
     }
 
     /**
      * Returns the resources directory
      * 
      * @return File object for the resources directory
      */
     public File getResourcesDir() {
         return this.resourcesDir;
     }
 
     /**
      * Returns the libraries directory
      * 
      * @return File object for the libraries directory
      */
     public File getLibrariesDir() {
         return this.librariesDir;
     }
 
     /**
      * Returns the languages directory
      * 
      * @return File object for the languages directory
      */
     public File getLanguagesDir() {
         return this.languagesDir;
     }
 
     /**
      * Returns the downloads directory
      * 
      * @return File object for the downloads directory
      */
     public File getDownloadsDir() {
         return this.downloadsDir;
     }
    
     /**
      * Returns the instances directory
      * 
      * @return File object for the instances directory
      */
     public File getInstancesDir() {
         return this.instancesDir;
     }
 
     /**
      * Returns the servers directory
      * 
      * @return File object for the servers directory
      */
     public File getServersDir() {
         return this.serversDir;
     }
 
     /**
      * Returns the temp directory
      * 
      * @return File object for the temp directory
      */
     public File getTempDir() {
         return this.tempDir;
     }
 
     /**
      * Deletes all files in the Temp directory
      */
     public void clearTempDir() {
         Utils.deleteContents(getTempDir());
     }
 
     /**
      * Returns the instancesdata file
      * 
      * @return File object for the instancesdata file
      */
     public File getInstancesDataFile() {
         return instancesDataFile;
     }
 
     /**
      * Sets the main parent JFrame reference for the Launcher
      * 
      * @param parent
      *            The Launcher main JFrame
      */
     public void setParentFrame(JFrame parent) {
         this.parent = parent;
     }
 
     /**
      * Load the users Server preference from file
      */
     public void loadServerProperty() {
         try {
             if (!propertiesFile.exists()) {
                 propertiesFile.createNewFile();
             }
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
         try {
             this.properties.load(new FileInputStream(propertiesFile));
             String serv = properties.getProperty("server", "Auto");
             if (isServerByName(serv)) {
                 this.server = getServerByName(serv);
             } else {
                 console.log("Server " + serv + " is invalid");
                 this.server = getServerByName("Auto"); // Server not found, use default of Auto
             }
         } catch (FileNotFoundException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Load the properties from file
      */
     public void loadProperties() {
         try {
             this.properties.load(new FileInputStream(propertiesFile));
             this.firstTimeRun = Boolean
                     .parseBoolean(properties.getProperty("firsttimerun", "true"));
 
             String lang = properties.getProperty("language", "English");
             if (isLanguageByName(lang)) {
                 this.language = getLanguageByName(lang);
             } else {
                 this.language = getLanguageByName("English"); // Language not found, use default
             }
 
             this.ram = Integer.parseInt(properties.getProperty("ram", "512"));
             if (this.ram > Utils.getMaximumRam()) {
                 this.ram = 512; // User tried to allocate too much ram, set it back to 0.5GB
             }
 
             this.permGen = Integer.parseInt(properties.getProperty("permGen", "64"));
 
             this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
             if (this.windowWidth > Utils.getMaximumWindowWidth()) {
                 this.windowWidth = 854; // User tried to make screen size wider than they have
             }
 
             this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));
             if (this.windowHeight > Utils.getMaximumWindowHeight()) {
                 this.windowHeight = 480; // User tried to make screen size wider than they have
             }
 
             this.javaParamaters = properties.getProperty("javaparameters", "");
 
             this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                     "true"));
 
             this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty(
                     "enableleaderboards", "false"));
 
             String lastAccountTemp = properties.getProperty("lastaccount", "");
             if (!lastAccountTemp.isEmpty()) {
                 if (isAccountByName(lastAccountTemp)) {
                     this.account = getAccountByName(lastAccountTemp);
                 } else {
                     this.account = null; // Account not found
                 }
             }
 
             this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));
         } catch (FileNotFoundException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Save the properties to file
      */
     public void saveProperties() {
         try {
             properties.setProperty("firsttimerun", "false");
             properties.setProperty("language", this.language.getName());
             properties.setProperty("server", this.server.getName());
             properties.setProperty("ram", this.ram + "");
             properties.setProperty("permGen", this.permGen + "");
             properties.setProperty("windowwidth", this.windowWidth + "");
             properties.setProperty("windowheight", this.windowHeight + "");
             properties.setProperty("javaparameters", this.javaParamaters);
             properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
             properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                     : "false");
             properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
             if (account != null) {
                 properties.setProperty("lastaccount", account.getUsername());
             } else {
                 properties.setProperty("lastaccount", "");
             }
             this.properties.store(new FileOutputStream(propertiesFile), "ATLauncher Settings");
         } catch (FileNotFoundException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Switch account currently used and save it
      * 
      * @param account
      *            Account to switch to
      */
     public void switchAccount(Account account) {
         if (account == null) {
             getConsole().log("Logging out of account");
             this.account = null;
         } else {
             if (account.isReal()) {
                 getConsole().log("Changed account to " + account);
                 this.account = account;
             } else {
                 getConsole().log("Logging out of account");
                 this.account = null;
             }
         }
         reloadPacksPanel();
         reloadInstancesPanel();
         reloadAccounts();
         try {
             properties.setProperty("firsttimerun", "false");
             properties.setProperty("language", this.language.getName());
             properties.setProperty("server", this.server.getName());
             properties.setProperty("ram", this.ram + "");
             properties.setProperty("windowwidth", this.windowWidth + "");
             properties.setProperty("windowheight", this.windowHeight + "");
             properties.setProperty("javaparameters", this.javaParamaters);
             properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
             properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                     : "false");
             properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            if(account == null){
                 properties.setProperty("lastaccount", "");
            }else{
                 properties.setProperty("lastaccount", account.getUsername());
             }
             this.properties.store(new FileOutputStream(propertiesFile), "ATLauncher Settings");
         } catch (FileNotFoundException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * The servers available to use in the Launcher
      * 
      * These MUST be hardcoded in order for the Launcher to make the initial connections to download
      * files
      */
     private void setupServers() {
         servers.add(new Server("Auto", ""));
         servers.add(new Server("Europe", "eu.atlcdn.net"));
         servers.add(new Server("US East", "useast.atlcdn.net"));
         servers.add(new Server("US West", "uswest.atlcdn.net"));
     }
 
     /**
      * Tests the servers for availability and best connection
      */
     private void testServers() {
         Thread thread = new Thread() {
             @Override
             public void run() {
                 double[] responseTimes = new double[servers.size()];
                 int count = 0;
                 int up = 0;
                 for (Server server : servers) {
                     if (server.isAuto())
                         continue; // Don't scan the Auto server
                     double startTime = System.currentTimeMillis();
                     try {
                         HttpURLConnection connection = (HttpURLConnection) new URL(
                                 server.getTestURL()).openConnection();
                         connection.setRequestMethod("HEAD");
                         connection.setConnectTimeout(3000);
                         int responseCode = connection.getResponseCode();
                         if (responseCode != 200) {
                             responseTimes[count] = 1000000.0;
                             server.disableServer();
                         } else {
                             double endTime = System.currentTimeMillis();
                             responseTimes[count] = endTime - startTime;
                             up++;
                         }
                     } catch (SocketTimeoutException e) {
                         responseTimes[count] = 1000000.0;
                         server.disableServer();
                     } catch (IOException e) {
                         App.settings.getConsole().logStackTrace(e);
                     }
                     count++;
                 }
                 int best = 0;
                 double bestTime = 10000000.0;
                 for (int i = 0; i < responseTimes.length; i++) {
                     if (responseTimes[i] < bestTime) {
                         best = i;
                         bestTime = responseTimes[i];
                     }
                 }
                 if (up != 0) {
                     bestConnectedServer = servers.get(best);
                 } else {
                     JOptionPane.showMessageDialog(null,
                             "<html><center>There was an issue connecting to ATLauncher "
                                     + "Servers<br/><br/>Offline mode is now enabled.<br/><br/>"
                                     + "To install packs again, please try connecting later"
                                     + "</center></html>", "Error Connecting To ATLauncher Servers",
                             JOptionPane.ERROR_MESSAGE);
                     offlineMode = true; // Set offline mode to be true
                 }
             }
 
         };
         thread.start();
     }
 
     /**
      * Loads the languages for use in the Launcher
      */
     private void loadLanguages() {
         Language language;
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(configsDir, "languages.xml"));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName("language");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     String name = element.getAttribute("name");
                     String localizedName = element.getAttribute("localizedname");
                     language = new Language(name, localizedName);
                     languages.add(language);
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Loads the Packs for use in the Launcher
      */
     private void loadPacks() {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(configsDir, "packs.xml"));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName("pack");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     int id = Integer.parseInt(element.getAttribute("id"));
                     String name = element.getAttribute("name");
                     boolean createServer = Boolean.parseBoolean(element
                             .getAttribute("createserver"));
                     String[] versions;
                     if (element.getAttribute("versions").isEmpty()) {
                         versions = new String[0];
                     } else {
                         versions = element.getAttribute("versions").split(",");
                     }
                     String[] testers;
                     if (element.getAttribute("testers").isEmpty()) {
                         testers = new String[0];
                     } else {
                         testers = new String(Base64.decode(element.getAttribute("testers")))
                                 .split(",");
                     }
                     String[] allowedPlayers;
                     if (element.getAttribute("allowedplayers").isEmpty()) {
                         allowedPlayers = new String[0];
                     } else {
                         allowedPlayers = new String(Base64.decode(element
                                 .getAttribute("allowedplayers"))).split(",");
                     }
                     String description = element.getAttribute("description");
                     String supportURL = element.getAttribute("supporturl");
                     String websiteURL = element.getAttribute("websiteurl");
                     if (element.getAttribute("type").equalsIgnoreCase("private")) {
                         packs.add(new PrivatePack(id, name, createServer, versions, testers,
                                 description, supportURL, websiteURL, allowedPlayers));
                     } else {
                         packs.add(new Pack(id, name, createServer, versions, testers, description,
                                 supportURL, websiteURL));
                     }
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Loads the Addons for use in the Launcher
      */
     private void loadAddons() {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(configsDir, "addons.xml"));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName("addon");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     int id = Integer.parseInt(element.getAttribute("id"));
                     String name = element.getAttribute("name");
                     String[] versions;
                     if (element.getAttribute("versions").isEmpty()) {
                         versions = new String[0];
                     } else {
                         versions = element.getAttribute("versions").split(",");
                     }
                     String description = element.getAttribute("description");
                     Pack forPack;
                     Pack pack = getPackByID(id);
                     if (pack != null) {
                         forPack = pack;
                     } else {
                         getConsole().log("Addon " + name + " is not available for any packs!");
                         continue;
                     }
                     Addon addon = new Addon(id, name, versions, description, forPack);
                     addons.add(addon);
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (InvalidPack e) {
             App.settings.getConsole().logStackTrace(e);
         }
     }
 
     /**
      * Loads the user installed Instances
      */
     private void loadInstances() {
         if (instancesDataFile.exists()) {
             try {
                 FileInputStream in = new FileInputStream(instancesDataFile);
                 ObjectInputStream objIn = new ObjectInputStream(in);
                 try {
                     Object obj;
                     while ((obj = objIn.readObject()) != null) {
                         if (obj instanceof Instance) {
                             File dir = new File(getInstancesDir(), ((Instance) obj).getSafeName());
                             if (dir.exists()) {
                                 instances.add((Instance) obj);
                                 if (isPackByName(((Instance) obj).getPackName())) {
                                     ((Instance) obj).setRealPack(getPackByName(((Instance) obj)
                                             .getPackName()));
                                 }
                             }
                         }
                     }
                 } catch (EOFException e) {
                     // Don't log this, it always happens when it gets to the end of the file
                 } finally {
                     objIn.close();
                     in.close();
                 }
             } catch (Exception e) {
                 App.settings.getConsole().logStackTrace(e);
             }
         }
     }
 
     public void saveInstances() {
         FileOutputStream out = null;
         ObjectOutputStream objOut = null;
         try {
             out = new FileOutputStream(instancesDataFile);
             objOut = new ObjectOutputStream(out);
             for (Instance instance : instances) {
                 objOut.writeObject(instance);
             }
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         } finally {
             try {
                 objOut.close();
                 out.close();
             } catch (IOException e) {
                 App.settings.getConsole().logStackTrace(e);
             }
         }
     }
 
     /**
      * Loads the saved Accounts
      */
     private void loadAccounts() {
         if (userDataFile.exists()) {
             try {
                 FileInputStream in = new FileInputStream(userDataFile);
                 ObjectInputStream objIn = new ObjectInputStream(in);
                 try {
                     Object obj;
                     while ((obj = objIn.readObject()) != null) {
                         if (obj instanceof Account) {
                             accounts.add((Account) obj);
                         }
                     }
                 } catch (EOFException e) {
                     // Don't log this, it always happens when it gets to the end of the file
                 } finally {
                     objIn.close();
                     in.close();
                 }
             } catch (Exception e) {
                 App.settings.getConsole().logStackTrace(e);
             }
         }
     }
 
     public void saveAccounts() {
         FileOutputStream out = null;
         ObjectOutputStream objOut = null;
         try {
             out = new FileOutputStream(userDataFile);
             objOut = new ObjectOutputStream(out);
             for (Account account : accounts) {
                 objOut.writeObject(account);
             }
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         } finally {
             try {
                 objOut.close();
                 out.close();
             } catch (IOException e) {
                 App.settings.getConsole().logStackTrace(e);
             }
         }
     }
 
     public void removeAccount(Account account) {
         if (this.account == account) {
             switchAccount(null);
         }
         accounts.remove(account);
         saveAccounts();
         reloadAccounts();
     }
 
     /**
      * Gets the MD5 hash for a minecraft.jar or minecraft_server.jar
      */
     public String getMinecraftHash(String root, String version, String type) {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(configsDir, "minecraft.xml"));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName(root);
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     if (element.getAttribute("version").equalsIgnoreCase(version)) {
                         if (type.equalsIgnoreCase("client")) {
                             return element.getAttribute("client");
                         } else {
                             return element.getAttribute("server");
                         }
                     }
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
         return null;
     }
 
     /**
      * Gets the install method for a minecraft version
      */
     public String getMinecraftInstallMethod(String version) {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(configsDir, "minecraft.xml"));
             document.getDocumentElement().normalize();
             NodeList nodeList = document.getElementsByTagName("minecraft");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     if (element.getAttribute("version").equalsIgnoreCase(version)) {
                         return element.getAttribute("installmethod");
                     }
                 }
             }
         } catch (SAXException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (ParserConfigurationException e) {
             App.settings.getConsole().logStackTrace(e);
         } catch (IOException e) {
             App.settings.getConsole().logStackTrace(e);
         }
         return null;
     }
 
     /**
      * Finds out if this is the first time the Launcher has been run
      * 
      * @return true if the Launcher hasn't been run and setup yet, false for otherwise
      */
     public boolean isFirstTimeRun() {
         return this.firstTimeRun;
     }
 
     /**
      * Get the Packs available in the Launcher
      * 
      * @return The Packs available in the Launcher
      */
     public ArrayList<Pack> getPacks() {
         return this.packs;
     }
 
     /**
      * Get the Packs available in the Launcher sorted alphabetically
      * 
      * @return The Packs available in the Launcher sorted alphabetically
      */
     public ArrayList<Pack> getPacksSorted() {
         ArrayList<Pack> packs = new ArrayList<Pack>(this.packs);
         Collections.sort(packs, new Comparator<Pack>() {
             public int compare(Pack result1, Pack result2) {
                 return result1.getName().compareTo(result2.getName());
             }
         });
         return packs;
     }
 
     public void setPackVisbility(Pack pack, boolean collapsed) {
         if (pack != null && account.isReal()) {
             if (collapsed) {
                 // Closed It
                 if (!account.getCollapsedPacks().contains(pack.getName())) {
                     account.getCollapsedPacks().add(pack.getName());
                 }
             } else {
                 // Opened It
                 if (account.getCollapsedPacks().contains(pack.getName())) {
                     account.getCollapsedPacks().remove(pack.getName());
                 }
             }
             saveAccounts();
             reloadPacksPanel();
         }
     }
 
     public void setInstanceVisbility(Instance instance, boolean collapsed) {
         if (instance != null && account.isReal()) {
             if (collapsed) {
                 // Closed It
                 if (!account.getCollapsedInstances().contains(instance.getName())) {
                     account.getCollapsedInstances().add(instance.getName());
                 }
             } else {
                 // Opened It
                 if (account.getCollapsedInstances().contains(instance.getName())) {
                     account.getCollapsedInstances().remove(instance.getName());
                 }
             }
             saveAccounts();
             reloadInstancesPanel();
         }
     }
 
     /**
      * Get the Instances available in the Launcher
      * 
      * @return The Instances available in the Launcher
      */
     public ArrayList<Instance> getInstances() {
         return this.instances;
     }
 
     /**
      * Get the Instances available in the Launcher sorted alphabetically
      * 
      * @return The Instances available in the Launcher sorted alphabetically
      */
     public ArrayList<Instance> getInstancesSorted() {
         ArrayList<Instance> instances = new ArrayList<Instance>(this.instances);
         Collections.sort(instances, new Comparator<Instance>() {
             public int compare(Instance result1, Instance result2) {
                 return result1.getName().compareTo(result2.getName());
             }
         });
         return instances;
     }
 
     public void setInstanceUnplayable(Instance instance) {
         instance.setUnplayable(); // Set the instance as unplayable
         saveInstances(); // Save the instancesdata file
         reloadInstancesPanel(); // Reload the instances tab
     }
 
     /**
      * Removes an instance from the Launcher
      */
     public void removeInstance(Instance instance) {
         if (this.instances.remove(instance)) { // Remove the instance
             Utils.delete(instance.getRootDirectory());
             saveInstances(); // Save the instancesdata file
             reloadInstancesPanel(); // Reload the instances panel
         }
     }
 
     public void apiCall(String username, String action, String extra1, String extra2, boolean debug) {
         if (enableLogs) {
             try {
                 String data = URLEncoder.encode("username", "UTF-8") + "="
                         + URLEncoder.encode(username, "UTF-8");
                 data += "&" + URLEncoder.encode("action", "UTF-8") + "="
                         + URLEncoder.encode(action, "UTF-8");
                 data += "&" + URLEncoder.encode("extra1", "UTF-8") + "="
                         + URLEncoder.encode(extra1, "UTF-8");
                 data += "&" + URLEncoder.encode("extra2", "UTF-8") + "="
                         + URLEncoder.encode(extra2, "UTF-8");
 
                 URL url = new URL("http://api.atlauncher.com/log.php");
                 URLConnection conn = url.openConnection();
                 conn.setDoOutput(true);
                 OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                 wr.write(data);
                 wr.flush();
                if (debug) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                         App.settings.getConsole().log("API Call Response: " + line);
                     }
                 }
                 wr.close();
             } catch (Exception e) {
                 App.settings.getConsole().logStackTrace(e);
             }
         }
     }
 
     public void apiCall(String username, String action, String extra1, String extra2) {
         apiCall(username, action, extra1, extra2, false);
     }
 
     public void apiCall(String username, String action, String extra1) {
         apiCall(username, action, extra1, "", false);
     }
 
     /**
      * Get the Addons available in the Launcher
      * 
      * @return The Addons available in the Launcher
      */
     public ArrayList<Addon> getAddons() {
         return this.addons;
     }
 
     /**
      * Get the Accounts added to the Launcher
      * 
      * @return The Accounts added to the Launcher
      */
     public ArrayList<Account> getAccounts() {
         return this.accounts;
     }
 
     /**
      * Get the Languages available in the Launcher
      * 
      * @return The Languages available in the Launcher
      */
     public ArrayList<Language> getLanguages() {
         return this.languages;
     }
 
     /**
      * Get the Servers available in the Launcher
      * 
      * @return The Servers available in the Launcher
      */
     public ArrayList<Server> getServers() {
         return this.servers;
     }
 
     /**
      * Determines if offline mode is enabled or not
      * 
      * @return true if offline mode is enabled, false otherwise
      */
     public boolean isInOfflineMode() {
         return this.offlineMode;
     }
 
     /**
      * Returns the JFrame reference of the main Launcher
      * 
      * @return Main JFrame of the Launcher
      */
     public Window getParent() {
         return this.parent;
     }
 
     /**
      * Sets the panel used for Instances
      * 
      * @param instancesPanel
      *            Instances Panel
      */
     public void setInstancesPanel(InstancesPanel instancesPanel) {
         this.instancesPanel = instancesPanel;
     }
 
     /**
      * Reloads the panel used for Instances
      */
     public void reloadInstancesPanel() {
         if (instancesPanel != null) {
             this.instancesPanel.reload(); // Reload the instances panel
         }
     }
 
     /**
      * Sets the panel used for Packs
      * 
      * @param packsPanel
      *            Packs Panel
      */
     public void setPacksPanel(PacksPanel packsPanel) {
         this.packsPanel = packsPanel;
     }
 
     /**
      * Reloads the panel used for Packs
      */
     public void reloadPacksPanel() {
         this.packsPanel.reload(); // Reload the instances panel
     }
 
     /**
      * Sets the bottom bar
      * 
      * @param bottomBar
      *            The Bottom Bar
      */
     public void setBottomBar(BottomBar bottomBar) {
         this.bottomBar = bottomBar;
     }
 
     /**
      * Reloads the bottom bar accounts combobox
      */
     public void reloadAccounts() {
         this.bottomBar.reloadAccounts(); // Reload the Bottom Bar accounts combobox
     }
 
     /**
      * Checks to see if there is already an instance with the name provided or not
      * 
      * @param name
      *            The name of the instance to check for
      * @return True if there is an instance with the same name already
      */
     public boolean isInstance(String name) {
         for (Instance instance : instances) {
             if (instance.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Finds a Pack from the given ID number
      * 
      * @param id
      *            ID of the Pack to find
      * @return Pack if the pack is found from the ID
      * @throws InvalidPack
      *             If ID is not found
      */
     public Pack getPackByID(int id) throws InvalidPack {
         for (Pack pack : packs) {
             if (pack.getID() == id) {
                 return pack;
             }
         }
         throw new InvalidPack("No pack exists with ID " + id);
     }
 
     /**
      * Checks if there is a pack by the given name
      * 
      * @param name
      *            name of the Pack to find
      * @return True if the pack is found from the name
      */
     public boolean isPackByName(String name) {
         for (Pack pack : packs) {
             if (pack.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Finds a Pack from the given name
      * 
      * @param name
      *            name of the Pack to find
      * @return Pack if the pack is found from the name
      */
     public Pack getPackByName(String name) {
         for (Pack pack : packs) {
             if (pack.getName().equalsIgnoreCase(name)) {
                 return pack;
             }
         }
         return null;
     }
 
     /**
      * Checks if there is an instance by the given name
      * 
      * @param name
      *            name of the Instance to find
      * @return True if the instance is found from the name
      */
     public boolean isInstanceByName(String name) {
         for (Instance instance : instances) {
             if (instance.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Finds a Instance from the given name
      * 
      * @param name
      *            name of the Instance to find
      * @return Instance if the instance is found from the name
      */
     public Instance getInstanceByName(String name) {
         for (Instance instance : instances) {
             if (instance.getName().equalsIgnoreCase(name)) {
                 return instance;
             }
         }
         return null;
     }
 
     /**
      * Finds a Language from the given name
      * 
      * @param name
      *            Name of the Language to find
      * @return Language if the language is found from the name
      */
     private Language getLanguageByName(String name) {
         for (Language language : languages) {
             if (language.getName().equalsIgnoreCase(name)) {
                 return language;
             }
         }
         return null;
     }
 
     /**
      * Finds a Server from the given name
      * 
      * @param name
      *            Name of the Server to find
      * @return Server if the server is found from the name
      */
     private Server getServerByName(String name) {
         for (Server server : servers) {
             if (server.getName().equalsIgnoreCase(name)) {
                 return server;
             }
         }
         return null;
     }
 
     /**
      * Finds an Account from the given username
      * 
      * @param username
      *            Username of the Account to find
      * @return Account if the Account is found from the username
      */
     private Account getAccountByName(String username) {
         for (Account account : accounts) {
             if (account.getUsername().equalsIgnoreCase(username)) {
                 return account;
             }
         }
         return null;
     }
 
     /**
      * Finds if a language is available
      * 
      * @param name
      *            The name of the Language
      * @return true if found, false if not
      */
     public boolean isLanguageByName(String name) {
         for (Language language : languages) {
             if (language.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Finds if a server is available
      * 
      * @param name
      *            The name of the Server
      * @return true if found, false if not
      */
     public boolean isServerByName(String name) {
         for (Server server : servers) {
             if (server.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Finds if an Account is available
      * 
      * @param username
      *            The username of the Account
      * @return true if found, false if not
      */
     public boolean isAccountByName(String username) {
         for (Account account : accounts) {
             if (account.getUsername().equalsIgnoreCase(username)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Gets the URL for a file on the user selected server
      * 
      * @param filename
      *            Filename including directories on the server
      * @return URL of the file
      */
     public String getFileURL(String filename) {
         if (bestConnectedServer == null && this.server.isAuto()) {
             return servers.get(3).getFileURL(filename, null);
         } else {
             return this.server.getFileURL(filename, bestConnectedServer);
         }
     }
 
     /**
      * Gets the Launcher's current Console instance
      * 
      * @return The Launcher's Console instance
      */
     public LauncherConsole getConsole() {
         return this.console;
     }
 
     public void showKillMinecraft(Process minecraft) {
         this.minecraftProcess = minecraft;
         getConsole().showKillMinecraft();
     }
 
     public void hideKillMinecraft() {
         getConsole().hideKillMinecraft();
     }
 
     public void killMinecraft() {
         if (this.minecraftProcess != null) {
             getConsole().log("Killing Minecraft");
             this.minecraftProcess.destroy();
             this.minecraftProcess = null;
         }
     }
 
     /**
      * Returns the best connected server
      * 
      * @return The server that the user was best connected to
      */
     public Server getBestConnectedServer() {
         return this.bestConnectedServer;
     }
 
     /**
      * Gets the users current active Language
      * 
      * @return The users set language
      */
     public Language getLanguage() {
         return this.language;
     }
 
     /**
      * Sets the users current active Language
      * 
      * @param language
      *            The language to set to
      */
     public void setLanguage(Language language) {
         this.language = language;
     }
 
     /**
      * Gets the users current active Server
      * 
      * @return The users set server
      */
     public Server getServer() {
         return this.server;
     }
 
     /**
      * Sets the users current active Server
      * 
      * @param server
      *            The server to set to
      */
     public void setServer(Server server) {
         this.server = server;
     }
 
     public int getMemory() {
         return this.ram;
     }
 
     public void setMemory(int memory) {
         this.ram = memory;
     }
 
     public int getPermGen() {
         return this.permGen;
     }
 
     public void setPermGen(int permGen) {
         this.permGen = permGen;
     }
 
     public int getWindowWidth() {
         return this.windowWidth;
     }
 
     public void setWindowWidth(int windowWidth) {
         this.windowWidth = windowWidth;
     }
 
     public int getWindowHeight() {
         return this.windowHeight;
     }
 
     public void setWindowHeight(int windowHeight) {
         this.windowHeight = windowHeight;
     }
 
     public String getJavaParameters() {
         return this.javaParamaters;
     }
 
     public void setJavaParameters(String javaParamaters) {
         this.javaParamaters = javaParamaters;
     }
 
     public Account getAccount() {
         return this.account;
     }
 
     /**
      * If the user has selected to show the console always or not
      * 
      * @return true if yes, false if not
      */
     public boolean enableConsole() {
         return this.enableConsole;
     }
 
     public void setEnableConsole(boolean enableConsole) {
         this.enableConsole = enableConsole;
     }
 
     public boolean enableLeaderboards() {
         return this.enableLeaderboards;
     }
 
     public void setEnableLeaderboards(boolean enableLeaderboards) {
         this.enableLeaderboards = enableLeaderboards;
     }
 
     public boolean enableLogs() {
         return this.enableLogs;
     }
 
     public void setEnableLogs(boolean enableLogs) {
         this.enableLogs = enableLogs;
     }
 
     public String getVersion() {
         return this.version;
     }
 
     public String getLocalizedString(String string) {
         return language.getString(string);
     }
 
     public String getLocalizedString(String string, String replace) {
         return language.getString(string).replace("%s", replace);
     }
 
 }
