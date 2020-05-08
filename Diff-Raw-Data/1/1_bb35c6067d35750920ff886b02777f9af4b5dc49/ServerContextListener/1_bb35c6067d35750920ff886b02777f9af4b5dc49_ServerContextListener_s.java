 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package org.lafayette.server.web;
 
 import org.lafayette.server.core.Stage;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 import org.lafayette.server.core.log.Log;
 import de.weltraumschaf.commons.Version;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.sql.DataSource;
 import org.lafayette.server.core.EnvVars;
 import org.lafayette.server.domain.db.NullDataSource;
 import org.lafayette.server.core.log.Logger;
 import org.lafayette.server.domain.mapper.Mappers;
import org.lafayette.server.web.InitialServletParameters;
 
 /**
  * Implements a servlet context listener.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class ServerContextListener extends GuiceServletContextListener implements ServletContextListener {
 
     /**
      * Key for a {@link Registry registry object} servlet context attribute.
      */
     public static final String REGISRTY = "registry";
     /**
      * PAth to version file.
      */
     private static final String VERSION_FILE = "/org/lafayette/server/version.properties";
     /**
      * Logging facility.
      */
     private static final Logger LOG = Log.getLogger(ServerContextListener.class);
     /**
      * JNDI name for main data base data source.
      */
     private static final String JNDI_NAME_DATA_SOURCE = "java:/comp/env/jdbc/mysql";
     /**
      * Registry shared over the whole web application.
      */
     private final Registry reg = new Registry();
 
     /**
      * Dedicated constructor.
      *
      * @throws NoSuchAlgorithmException if registry can't initialize {@link Registry#nongeGenerator}
      */
     public ServerContextListener() throws NoSuchAlgorithmException {
         super();
     }
 
     @Override
     protected Injector getInjector() {
         // http://code.google.com/p/google-guice/wiki/ServletModule
         LOG.debug("Create Guice injector.");
         final Injector injector = Guice.createInjector(new ServerModule());
         reg.setDependnecyInjector(injector);
         return injector;
     }
 
     @Override
     public void contextInitialized(final ServletContextEvent sce) {
         LOG.debug("Context initialized. Execute listener...");
         loadVersion();
         loadStage();
         final DataSource dataSource = createDataSource();
         createMappersFactory(dataSource);
         final ServletContext servletContext = sce.getServletContext();
         reg.setInitParameters(new InitialServletParameters(servletContext));
         servletContext.setAttribute(REGISRTY, reg);
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent sce) {
         LOG.debug("Context destroyed. Execute listener...");
     }
 
     /**
      * Add version information to the {@link Registry registry}.
      */
     private void loadVersion() {
         try {
             LOG.info("Load version from file %s.", VERSION_FILE);
             final Version version = new Version(VERSION_FILE);
             version.load();
             LOG.info("Loaded version %s.", version.getVersion());
             reg.setVersion(version);
         } catch (IOException ex) {
             LOG.fatal("Error loading version: %s", ex.toString());
         }
     }
 
     /**
      * Add stage information to the {@link Registry registry}.
      */
     private void loadStage() {
         final String envStage = EnvVars.STAGE.getFromSystem();
         LOG.debug("Use $STAGE=%s as stage.", envStage);
         final Stage stage = new Stage(envStage);
         LOG.info("Loaded stage %s.", stage.toString());
         reg.setStage(stage);
     }
 
     /**
      * Create {@link Mappers mapper factory} and stores to registry.
      *
      * @param dataSource open database connection
      */
     private void createMappersFactory(final DataSource dataSource) {
         reg.setMappers(new Mappers(dataSource));
     }
 
     /**
      * Creates data source provided by container via JNDI and sets to {@link Registry}.
      *
      * @return created data source
      */
     private DataSource createDataSource() {
         try {
             LOG.info("Create data source.");
             final InitialContext initialContext = new InitialContext();
             final DataSource dataSource = (DataSource) initialContext.lookup(JNDI_NAME_DATA_SOURCE);
 
             if (null == dataSource) {
                 LOG.error("Can't lookup data source via JNDI!");
             } else {
                 reg.setDataSource(dataSource);
                 return dataSource;
             }
         } catch (NamingException ex) {
             LOG.fatal(ex.getMessage());
         }
 
         return new NullDataSource();
     }
 }
