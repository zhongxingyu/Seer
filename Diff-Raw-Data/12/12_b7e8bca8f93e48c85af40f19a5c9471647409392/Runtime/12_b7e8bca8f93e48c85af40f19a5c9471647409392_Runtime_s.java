 package de.aidger.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.List;
 
 import static de.aidger.utils.Translation._;
 import de.aidger.utils.Configuration;
 import de.aidger.utils.Logger;
 import de.aidger.utils.Translation;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.util.tuple.Pair;
 
 /**
  * Initializes Configuration and Translation and relays the methods
  * 
  * @author aidGer Team
  */
 public final class Runtime {
 
     /**
      * Set of supported operating systems.
      */
     public enum OS {
         /* Any kind of Windows operating system */
         WINDOWS,
         /* All systems using a Linux kernel */
         LINUX,
         /* MacIntoshs (not necessarily OS X) */
         MACOSX,
         /* Unknown operating system */
         UNKNOWN
     };
 
     /**
      * Holds he only instance of the class.
      */
     private static Runtime instance = null;
 
     /**
      * The users operating system.
      */
     private OS operatingSystem;
 
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
         /* Set the operating system */
         String os = System.getProperty("os.name").toLowerCase();
         if (os.indexOf("windows") > -1) {
             operatingSystem = OS.WINDOWS;
         } else if (os.indexOf("linux") > -1) {
             operatingSystem = OS.LINUX;
         } else if (os.indexOf("mac") > -1) {
             operatingSystem = OS.MACOSX;
         } else {
             operatingSystem = OS.UNKNOWN;
         }
 
         /* Get the path to the users home or config directory */
         String home = "";
         if (operatingSystem == OS.LINUX) {
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
         } else if (operatingSystem == OS.WINDOWS) {
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
             /*
              * We've got no mac so we don't have any idea where to put our
              * files. Same goes for any other unknown operating systems. That's
              * why we're simply using the homedir
              */
             home = System.getProperty("user.home", ".");
         }
         configPath = home + "/aidGer/";
         File file = new File(configPath);
 
         if ((!file.exists() || !file.isDirectory()) && !file.mkdirs()) {
             UI
                     .displayError(MessageFormat
                             .format(
                                     "Could not create directory \"{0}\".\nPlease make sure that you have enough rights to create this directory.",
                                     new Object[] { file.getPath() }));
             System.exit(-1);
         }
 
         configuration = new Configuration(configPath);
 
         translation = new Translation(configPath, configuration.get("language"));
 
         /* Check if an instance of aidGer is already running */
         if (!Boolean.valueOf(getOption("debug")) && !checkLock()) {
             UI.displayError(_("Only one instance of aidGer can be run at a time."));
             System.exit(-1);
         }
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
      * Returns the operating system of the user.
      */
     public OS getOperatingSystem() {
         return operatingSystem;
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
 
        return str.substring(1, str.length() - 1).split(",");
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
      * Get a list of all languages installed on the system. The format is 0 =>
      * short, 1 => long language name.
      * 
      * @return The list of all installed languages
      */
     public List<Pair<String, String>> getLanguages() {
         return translation.getLanguages();
     }
 
     /**
      * Check for a lock file or create it to only allow one running instance.
      *
      * @return
      */
     protected boolean checkLock() {
         File lock = new File(configPath + "/aidger.lock");
         if (lock.exists()) {
             return false;
         }
 
         try {
             lock.createNewFile();
             lock.deleteOnExit();
         } catch (IOException ex) {
             Logger.error(_("Couldn't create lockfile"));
         }
 
         return true;
     }
 
 }
