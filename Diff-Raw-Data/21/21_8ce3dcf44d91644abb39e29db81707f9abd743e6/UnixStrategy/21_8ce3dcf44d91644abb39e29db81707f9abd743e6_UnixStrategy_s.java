 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.config;
 
 import com.google.common.collect.Lists;
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 import org.lafayette.server.log.Log;
 import org.lafayette.server.log.Logger;
 
 /**
  * Strategy to find configuration file.
  *
  * <ol>
  * <li>look for $HOME/.lafayette/server.properties
  * <li>look for /etc/lafayette/server.properties
  * </ol>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 class UnixStrategy implements LoaderStrategie {
 
     /**
      * USed as directory name.
      */
     private static final String VENDOR = "lafayette";
     /**
      * Configuration file base name.
      */
     private static final String FILE = "server.properties";
     /**
      * On Unix configurations are stored usually in a directory called {@code etc}.
      */
     private static final String SYSTEM_CONFIG_DIR = "etc";
     /**
      * Directory separator on Unix systems.
      */
     private static final String DIR_SEP = "/";
 
     /**
      * Logging facility.
      */
     private static final Logger LOG = Log.getLogger(UnixStrategy.class);
     /**
      * List of files to search for a configuration.
      *
      * The files are checked in the order they were added to the list.
      * First found file wins.
      */
     private final List<String> fileList = Lists.newArrayList();
     /**
      * The found configuration.
      */
     private File foundConfig;
 
     /**
      * Initializes prefix with empty string.
      *
      * @param home where the users home directory is
      */
     public UnixStrategy(final String home) {
         this(home, "");
     }
 
     /**
      * Dedicated constructor.
      *
      * Initializes the list of files to look for a configuration.
      *
      * @param home where the users home directory is
      * @param prefix used to prefix the system configuration directory, e.g. they reside in {@code /foo/bar/etc}
      *               instead of {@code /etc}
      */
     public UnixStrategy(final String home, final String prefix) {
         super();
         fileList.add(createHomeConfigFileName(home));
         fileList.add(createSystemConfigFileName(prefix));
     }
 
     /**
      * Helper to create the home configuration file name.
      *
      * The pattern is:
      * {@code home + "/." + VENDOR + DIR_SEP + FILE}
      *
      * @param home the home directory. Must not be empty or {@code null}
      * @return generated file name string
      */
     public static String createHomeConfigFileName(final String home) {
         if (null == home) {
             throw new IllegalArgumentException("Argument must not be null!");
         }
 
         if (home.length() == 0) {
             throw new IllegalArgumentException("Argument must not be empty!");
         }
 
         return home + "/." + VENDOR + DIR_SEP + FILE;
     }
 
     /**
      * Helper to create the system configuration file name.
      *
      * The pattern is {@code prefix + DIR_SEP + SYSTEM_CONFIG_DIR + DIR_SEP + VENDOR + DIR_SEP + FILE}.
      *
      * @param prefix may be empty or null
      * @return generated file name string
      */
     public static String createSystemConfigFileName(final String prefix) {
         if (null != prefix && prefix.length() > 0) {
             return prefix + DIR_SEP + SYSTEM_CONFIG_DIR + DIR_SEP + VENDOR + DIR_SEP + FILE;
         }
         return DIR_SEP + SYSTEM_CONFIG_DIR + DIR_SEP + VENDOR + DIR_SEP + FILE;
     }
 
     @Override
     public boolean hasFoundConfig() {
         return foundConfig != null;
     }
 
     @Override
     public File getFoundConfig() {
         return foundConfig;
     }
 
     @Override
     public void findConfig() {
         if (hasFoundConfig()) {
             LOG.debug("Already found configuration '%s'.", foundConfig.getAbsolutePath());
             return;
         }
 
         for (final String fileName : getFileList()) {
             LOG.debug("Look if configuration '%s' is present.", fileName);
             final File file = new File(fileName);
 
             if (!file.exists()) {
                 LOG.debug("File '%s' does not exists.", fileName);
                 continue;
             }
 
             if (!file.isFile()) {
                 LOG.debug("File '%s' is not a regular file.", fileName);
                 continue;
             }
 
             if (!file.canRead()) {
                 LOG.debug("File '%s' is not readable.", fileName);
                 continue;
             }
 
             foundConfig = file;
             LOG.debug("Configuration file found in '%s'.", fileName);
            return;
         }
     }
 
     /**
      * Get collection of files to look for a configuration.
      *
      * @return collection of files
      */
     Collection<String> getFileList() {
         return Lists.newArrayList(fileList);
     }
 }
