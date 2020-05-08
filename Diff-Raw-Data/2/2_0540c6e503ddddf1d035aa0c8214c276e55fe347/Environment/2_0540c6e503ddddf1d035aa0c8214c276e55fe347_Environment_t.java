 package org.jcoderz.m3util.intern.util;
 
 import java.io.File;
 
 /**
  * The environment utility class provides methods for accessing various
  * sub-folders of the library located below the root M3_LIBRARY_HOME.
  *
  * The root is initialized by looking at a system parameter M3_LIBRARY_HOME
  * first and at an environment variable next. If the variable could not be found
  * via any of those mechanisms, an IllegalArgumentException gets thrown.
  *
  * @author mrumpf
  *
  */
 public class Environment {
 
     /**
      * The root folder.
      */
     public static File m3LibraryHome;
     public static final String M3_LIBRARY_HOME_KEY = "M3_LIBRARY_HOME";
 
     private Environment() {
         // do not instantiate
     }
 
     /**
      * The library root folder.
      *
      * @return the root folder
      */
     public static synchronized File getLibraryHome() {
        if (m3LibraryHome == null) {
             String root = System.getProperty(M3_LIBRARY_HOME_KEY);
             if (root == null) {
                 root = System.getenv(M3_LIBRARY_HOME_KEY);
             }
             File rootFolder = null;
             if (root != null) {
                 rootFolder = new File(root);
                 if (!rootFolder.exists()) {
                     throw new IllegalArgumentException("Folder "
                             + M3_LIBRARY_HOME_KEY + "=" + rootFolder);
                 }
             } else {
                 throw new IllegalArgumentException(
                         "Value for "
                         + M3_LIBRARY_HOME_KEY
                         + " was neither set as environment variable nor as system parameter."
                         + " Set the system parameter like this: -DM3_LIBRARY_HOME=... "
                         + " or the environment like this under Windows: set M3_LIBRARY_HOME=... and"
                         + " like this under Unix: export M3_LIBRARY_HOME=...");
             }
             m3LibraryHome = rootFolder;
         }
         return m3LibraryHome;
     }
 
     /**
      * The configuration folder.
      *
      * @return the config folder
      */
     public static File getConfigFolder() {
         return new File(getLibraryHome(), "tools/etc");
     }
 
     /**
      * The audio folder.
      *
      * @return the audio folder
      */
     public static File getAudioFolder() {
         return new File(getLibraryHome(), "audio");
     }
 
     /**
      * The library log folder.
      *
      * @return the log folder
      */
     public static File getLogFolder() {
         return new File(getLibraryHome(), "tools/var/log");
     }
 
     /**
      * The library lucene index folder.
      *
      * @return the lucene index folder
      */
     public static File getLuceneFolder() {
         return new File(getLibraryHome(), "tools/var/lib/lucene");
     }
 
     /**
      * The library database folder.
      *
      * @return the database folder
      */
     public static File getDbFolder() {
         return new File(getLibraryHome(), "tools/var/lib/db");
     }
 }
