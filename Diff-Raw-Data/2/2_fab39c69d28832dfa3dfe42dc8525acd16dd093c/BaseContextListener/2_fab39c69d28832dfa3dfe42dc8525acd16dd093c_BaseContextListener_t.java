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
 
 package org.lafayette.server.web.servlet;
 
 import de.weltraumschaf.commons.Version;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import org.lafayette.server.core.EnvVars;
 import org.lafayette.server.core.Stage;
 import org.lafayette.server.core.log.Log;
 import org.lafayette.server.core.log.Logger;
 import org.lafayette.server.web.BaseRegistry;
 
 /**
  * Only initializes {@link BaseRegistry} with base information provided to the web app.
  *
  * Provided information:
  * <ul>
  * <li>stage information</li>
  * <li>version information</li>
  * </ul>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class BaseContextListener implements ServletContextListener {
 
     /**
      * PAth to version file.
      */
     private static final String VERSION_FILE = "/org/lafayette/server/version.properties";
     /**
      * Logging facility.
      */
     private static final Logger LOG = Log.getLogger(ExtendedContextListener.class);
 
     /**
      * BaseRegistry shared over the whole web application.
      */
     private final BaseRegistry registry = new BaseRegistry();
 
     /**
      * Dedicated constructor.
      *
     * @throws NoSuchAlgorithmException if registry can't initialize {@link BaseRegistry#nonceGenerator}
      */
     public BaseContextListener() throws NoSuchAlgorithmException {
         super();
     }
 
     @Override
     final public void contextInitialized(final ServletContextEvent sce) {
         LOG.info("Context initialized. Execute base listener...");
         sce.getServletContext().setAttribute(ContextAttributes.REGISRTY.name(), registry);
         loadVersion();
         loadStage();
         LOG.debug("Base listener executed.");
 
     }
 
     @Override
     final public void contextDestroyed(final ServletContextEvent sce) {
         LOG.info("Context destroyed. Execute base listener...");
         LOG.debug("Base listener executed.");
     }
 
     BaseRegistry getRegistry() {
         return registry;
     }
 
     /**
      * Add version information to the {@link BaseRegistry registry}.
      */
     private void loadVersion() {
         try {
             LOG.debug("Load version from file %s.", VERSION_FILE);
             final Version version = new Version(VERSION_FILE);
             version.load();
             LOG.info("Loaded version '%s'.", version.getVersion());
             registry.setVersion(version);
         } catch (final IOException ex) {
             LOG.error("Error loading version: %s", ex.toString());
         }
     }
 
     /**
      * Add stage information to the {@link BaseRegistry registry}.
      */
     private void loadStage() {
         final String envStage = EnvVars.STAGE.getFromSystem();
         LOG.debug("Use $STAGE='%s' as stage.", envStage);
         final Stage stage = new Stage(envStage);
         LOG.info("Loaded stage %s.", stage.toString());
         registry.setStage(stage);
     }
 }
