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
 
 import de.weltraumschaf.commons.system.OperatingSystem;
 import java.io.File;
 import org.lafayette.server.EnvVars;
 import org.lafayette.server.log.Log;
 import org.lafayette.server.log.Logger;
 
 /**
  * Load server configuration file.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class ConfigLoader {
 
     /**
      * Logging facility.
      */
     private static final Logger LOG = Log.getLogger(ConfigLoader.class);
     /**
      * Algorithm to find config file.
      */
     private final LoaderStrategie loaderStrategy;
 
     /**
      * Dedicated constructor.
      *
      * @param loaderStrategy algorithm used to find configuration file
      */
     private ConfigLoader(final LoaderStrategie loaderStrategy) {
         super();
         this.loaderStrategy = loaderStrategy;
     }
 
     /**
      * Calls {@link #determineStrategy(java.lang.String)} with {@code System.getProperty("os.name", "")}
      * as argument.
      *
      * @return new strategy object
      */
     static LoaderStrategie determineStrategy() {
         return determineStrategy(System.getProperty("os.name", ""));
     }
 
     /**
      * Determines the loader strategy for an OS.
      *
      * If an unsupported OS is passed in an {@link IllegalArgumentException} will be thrown.
      *
      * @param osName name of operating system
      * @return new strategy object
      */
     static LoaderStrategie determineStrategy(final String osName ) {
         final OperatingSystem os = OperatingSystem.determine(osName);
 
         if (OperatingSystem.WINDOWS == os) {
             LOG.debug("Determined windows strategy.");
             return new WindowsStrategy();
         } else if (OperatingSystem.LINUX == os || OperatingSystem.MACOSX == os) {
             String home = EnvVars.HOME.getFromSystem();
 
             if (home.length() == 0) {
                 home = ".";
             }
 
             LOG.debug("Determined unix strategy ($HOME=%s).", home);
             return new UnixStrategy(home);
         } else {
             throw new IllegalArgumentException(String.format("Unsupported os '%s'! No strategy found.", osName));
         }
     }
 
     public static ConfigLoader create() {
         return create(determineStrategy());
     }
 
     /**
      * Creates a configuration loader with configuration finding strategy.
      *
      * @param loaderStrategy used strategy to find configuration
      * @return new loader
      */
     public static ConfigLoader create(final LoaderStrategie loaderStrategy) {
         return new ConfigLoader(loaderStrategy);
     }
 
     /**
      * Invokes the file finding algorithm to find a file name pointing to the configuration.
      */
     public void load() {
         loaderStrategy.findConfig();
     }
 
     /**
      * Returns whether a configuration file was found by the strategy or not.
      *
      * Returns always {@code false} before you invoke {@link #load()} the first time.
      *
      * @return {@code true} if file was found; else {@code false}
      */
     public boolean hasConfig() {
         return loaderStrategy.hasFoundConfig();
     }
 
     /**
      * Get the configuration file.
      *
      * Returns always {@code null} before you invoke {@link #load()} the first time.
      *
      * @return configuration file. May be null if {@link #hasConfig()} returns {@code false}
      */
     public File getFile() {
         return loaderStrategy.getFoundConfig();
     }
 
 }
