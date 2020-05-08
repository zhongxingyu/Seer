 package de.aidger.model;
 
 import java.io.File;
 
 import de.aidger.utils.Configuration;
 import de.aidger.utils.Translation;
 
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
      * The configuration class handling the settings.
      */
     private Configuration configuration = null;
 
     /**
      * The translation class handling the translations.
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
         home = home + "/aidGer/";
         File file = new File(home);
         if ((!file.exists() || !file.isDirectory()) && !file.mkdirs()) {
             System.err.println("Konnte Verzeichnis für Settings nicht "
                     + "erstellen");
         }
 
         configuration = new Configuration(home);
 
         translation = new Translation(home, configuration.get("sprache"));
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
      * Get the translation of an string.
      * 
      * @param id
      *            The unique identifier of the string
      * @return The translated or an empty string
      */
     public String getTranslation(String id) {
         return translation.get(id);
     }
 
 }
