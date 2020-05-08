 package com.github.mikesena.maven.plugins.environments;
 
 import java.io.File;
 import java.util.Properties;
 
 /**
  * Several utilities to assist with working with Maven & filtering.
  * 
  * @author Michael Sena
  * 
  */
 public final class MavenFilteringUtil {
    /** Matches Ant-style properties in a string: i.e. ${prop.name}. */
    private static final String PATTERN = ".*\\$\\{.+\\}.*";

     /**
      * Performs Maven-style filtering on a directory. Allows filenames to be dynamically renamed, based on properties.
      * 
      * @param directory
      *            Directory to scan
      * @param properties
      *            List of properties used by the target Maven project
      */
     public static void doFilenameFiltering(final File directory, final Properties properties) {
         for (final File file : directory.listFiles()) {
             if (file.isDirectory()) {
                 doFilenameFiltering(file, properties);
             } else {
                 String fileName = file.getName();
                while (fileName.matches(PATTERN)) {
                    final String property = fileName.substring(fileName.indexOf("${") + 2, fileName.indexOf('}'));
                     if (properties.containsKey(property)) {
                         final String value = properties.getProperty(property);
                         fileName = fileName.replaceAll("\\$\\{" + property + "\\}", value);
                     }
                 }
                 if (!fileName.equals(file.getName())) {
                     file.renameTo(new File(file.getParentFile(), fileName));
                 }
             }
         }
     }
 
     /** Hide the default constructor. */
     private MavenFilteringUtil() {
     }
 }
