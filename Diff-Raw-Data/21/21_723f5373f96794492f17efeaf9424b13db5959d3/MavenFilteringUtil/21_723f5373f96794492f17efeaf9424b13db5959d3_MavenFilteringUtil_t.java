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
                int i = fileName.indexOf("${");
                while (i >= 0) {
                    final String property = fileName.substring(i + 2, fileName.indexOf('}', i));
                     if (properties.containsKey(property)) {
                         final String value = properties.getProperty(property);
                         fileName = fileName.replaceAll("\\$\\{" + property + "\\}", value);
                     }
                    i = fileName.indexOf("${", i + 1);
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
