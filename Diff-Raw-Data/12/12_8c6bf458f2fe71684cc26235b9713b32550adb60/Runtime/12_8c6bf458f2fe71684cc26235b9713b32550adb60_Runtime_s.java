 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.model;
 
 import static de.aidger.utils.Translation._;
 
 import java.beans.PersistenceDelegate;
 import java.io.File;
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 
 import de.aidger.model.models.*;
 import de.aidger.utils.*;
 import de.aidger.view.UI;
 import siena.PersistenceManager;
 import siena.PersistenceManagerFactory;
 import siena.jdbc.JdbcPersistenceManager;
 
 /**
  * Initializes Configuration and Translation and relays the methods
  * 
  * @author aidGer Team
  */
 public final class Runtime {
 
     /**
      * Holds he only instance of the class.
      */
     private static Runtime instance = null;
 
     /**
      * True, if this is a test run.
      */
     private boolean testRun = false;
 
     /**
      * The path the config is saved in.
      */
     private String configPath;
 
     /**
      * The configuration class handling the settings.
      */
     private Configuration configuration = null;
 
     /**
      * The class handling the translation of strings.
      */
     private Translation translation = null;
 
     /**
      * The data XML manager.
      */
     private DataXMLManager dataManager = null;
 
     /**
      * Is this the first start of aidGer?
      */
     private boolean firstStart = false;
     
     /**
      * Does the connection to the database work?
      */
     private boolean connected = true;
 
     /**
      * Constructor must be private and does nothing.
      */
     private Runtime() {
 
     }
 
     /**
      * Provides access to an instance of this class.
      * 
      * @return Instance of the Runtime class
      */
     public synchronized static Runtime getInstance() {
         if (instance == null) {
             instance = new Runtime();
         }
 
         return instance;
     }
 
     /**
      * Resolves the OS of the user and the file path to the aidGer settings.
      */
     public void initialize() {
         /* Get the path to the users home or config directory */
         String home = "";
         String os = System.getProperty("os.name").toLowerCase();
         if (os.indexOf("linux") > -1 || os.indexOf("mac") > -1) {
             /*
              * Try using the XDG_CONFIG_DIR variable first. Provides the
              * standard directory according to the desktop guidelines or a user
              * set equivalent
              */
             String confdir = System.getenv("XDG_CONFIG_HOME");
             if (confdir == null || confdir.isEmpty()) {
                 /*
                  * Otherwise just use the homedir and the hidden config
                  * directory
                  */
                 confdir = System.getProperty("user.home", ".");
                 confdir += "/.config/";
             }
             home = confdir;
         } else if (os.indexOf("windows") > -1) {
             /*
              * Try using the APPDATA variable first. Should return the
              * ApplicationData folder but may fail on older Windows versions.
              */
             String confdir = System.getenv("APPDATA");
             if (confdir == null || confdir.isEmpty()) {
                 /* Otherwise just use the homedir */
                 confdir = System.getProperty("user.home", ".");
             }
             home = confdir;
         } else {
             home = System.getProperty("user.home", ".");
         }
 
         configPath = home + "/aidGer/";
         File file = new File(configPath);
 
         if (!file.exists() || !file.isDirectory()) {
             firstStart = true;
 
             /* Create the aidGer configuration directory. */
             if (!file.mkdirs()) {
                 UI
                     .displayError(MessageFormat
                         .format(
                             "Could not create directory \"{0}\".\n"
                                     + "Please make sure that you have enough rights to create this directory.",
                             new Object[] { file.getPath() }));
                 System.exit(-1);
             }
         }
 
         String templatePath = configPath + "/templates/";
         File templates = new File(templatePath);
 
         if (!templates.exists() || !templates.isDirectory()) {
             if (!templates.mkdir()) {
                 UI
                     .displayError(MessageFormat
                         .format(
                             "Could not create directory \"{0}\".\n"
                                     + "Please make sure that you have enough rights to create this directory.",
                             new Object[] { templates.getPath() }));
             }
         }
 
         /*
          * Check for an environment variable AIDGER_TEST and set the config path
          * to "." if it is set. This is used, to seperate test and run
          * configurations.
          */
         String test = System.getenv("AIDGER_TEST");
         if (test != null) {
             testRun = true;
             configPath = "./";
         }
 
         configuration = new Configuration();
 
         /* Use the detected language on first start */
         if (firstStart || configuration.get("language") == null
                 || configuration.get("language").isEmpty()) {
 
             /* Get the default language code and use it */
             Locale loc = Locale.getDefault();
             String language = loc.getLanguage();
             if (language.isEmpty()) {
                 configuration.set("language", "en");
             } else {
                 configuration.set("language", language);
             }
         }
 
         /* Check if the translation is really available and fall back to default */
         String language = configuration.get("language");
         boolean langFound = false;
         for (Pair<String, String> pair : Translation.getLanguages()) {
             if (pair.fst().equals(language)) {
                 langFound = true;
                 break;
             }
         }
 
         if (langFound) {
             translation = new Translation(language);
         } else {
             configuration.set("language", "en");
             translation = new Translation("en");
         }
 
         /* Check if an instance of aidGer is already running */
         if (!testRun && !checkLock()) {
             UI
                 .displayError(_("Only one instance of aidGer can be run at a time."));
             System.exit(-1);
         }
         
         /* Set database connection settings and try to get an instance of AdoHiveController */
         try {
             Properties p = new Properties();
             p.put("driver", getOption("database-driver", "com.mysql.jdbc.Driver"));
            URI uri = new URI(getOption("database-uri", "jdbc:mysql://@localhost/aidger?user=root&password="));
             if (uri.getQuery() != null) {
                 String[] user = uri.getQuery().split("&");
                 p.put("user", user[0].substring(5));
                 p.put("password", user[1].substring(9));
             }
             p.put("url", getOption("database-uri"));
 
             PersistenceManager pm = new JdbcPersistenceManager();
             pm.init(p);
             PersistenceManagerFactory.install(pm, Activity.class);
 
             //TODO: Get a connection
         } catch (Exception e) {
             Logger.error("Could not connect to the Database: " + e.toString());
             e.printStackTrace();
         	connected = false;
         }
 
         dataManager = new DataXMLManager();
     }
 
     /**
      * Get the path the config is saved in.
      * 
      * @return The path of the config dir
      */
     public String getConfigPath() {
         return configPath;
     }
 
     /**
      * Set a custom config path (Only to be used in tests).
      * 
      * @param path
      *            The new config path
      */
     public void setConfigPath(String path) {
         configPath = path;
     }
 
     /**
      * Is this the first start of aidGer?
      * 
      * @return True, if this is the first start
      */
     public boolean isFirstStart() {
         return firstStart;
     }
 
     /**
      * Is this a test run of aidGer?
      * 
      * @return True, if this is a test run
      */
     public boolean isTestRun() {
         return testRun;
     }
     
     /**
      * Does the connection to the database work
      * 
      * @return True, if it does work
      */
     public boolean isConnected() {
     	return connected;
     }
 
     /**
      * Returns the location and name of the .jar file.
      * 
      * @return The location
      */
     public String getJarLocation() {
         String location = getClass().getProtectionDomain().getCodeSource()
             .getLocation().toString();
         int idx = location.indexOf(":");
 
         /* Windows uses file:/C:/folder whereas Unix uses file:/folder */
         if (idx != location.lastIndexOf(":")) {
             location = location.substring(idx + 2);
         } else {
             location = location.substring(idx + 1);
         }
         return location;
     }
 
     /**
      * Get the value of an option.
      * 
      * @param option
      *            The option to get
      * @return The value of the specified option
      */
     public String getOption(String option) {
         return configuration.get(option);
     }
 
     /**
      * Get the value of an option or the default if the option doesn't exist
      * 
      * @param option
      *            The option to get
      * @param def
      *            The default value
      * @return The value of the specified option or the default value
      */
     public String getOption(String option, String def) {
         String ret = configuration.get(option);
         if (ret == null) {
             configuration.set(option, def);
             return def;
         }
         return ret;
     }
 
     /**
      * Get an array of values for the specified option
      * 
      * @param option
      *            The option to get
      * @return Array containing all values
      */
     public String[] getOptionArray(String option) {
         String str = getOption(option);
 
         if (str == null || (!str.startsWith("[") || !str.endsWith("]"))) {
             return null;
         }
 
         return str.substring(1, str.length() - 1).split(", ");
     }
 
     /**
      * Set the value of an option.
      * 
      * @param option
      *            The option to set
      * @param value
      *            The value to set it to
      */
     public void setOption(String option, String value) {
         configuration.set(option, value);
     }
 
     /**
      * Sets the value of a property by converting an array into a single string.
      * 
      * @param option
      *            The property to change
      * @param values
      *            Array of all values
      */
     public void setOptionArray(String option, String[] values) {
         setOption(option, java.util.Arrays.toString(values));
     }
 
     /**
      * Remove an option from the config.
      * 
      * @param option
      *            The option to remove
      */
     public void clearOption(String option) {
         configuration.remove(option);
     }
 
     /**
      * Get a list of all languages installed on the system. The format is 0 =>
      * short, 1 => long language name.
      * 
      * @return The list of all installed languages
      */
     public List<Pair<String, String>> getLanguages() {
         return translation.getLanguages();
     }
 
     /**
      * Returns the data XML manager.
      * 
      * @return the data XML manager
      */
     public DataXMLManager getDataXMLManager() {
         return dataManager;
     }
 
     /**
      * Check for a lock file or create it to only allow one running instance.
      * 
      * @return True if no lock file exists and the application can be started
      */
     protected boolean checkLock() {
         try {
             final File file = new File(configPath + "/aidger.lock");
             final java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(
                 file, "rw");
             final java.nio.channels.FileLock fileLock = randomAccessFile
                 .getChannel().tryLock();
             if (fileLock != null) {
                 java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
                     @Override
                     public void run() {
                         try {
                             fileLock.release();
                             randomAccessFile.close();
                             file.delete();
                         } catch (Exception e) {
                             System.err.println("Unable to remove lock file");
                         }
                     }
                 });
                 return true;
             }
         } catch (Exception e) {
             System.err.println("Unable to create and/or lock file");
         }
         return false;
     }
 
 }
