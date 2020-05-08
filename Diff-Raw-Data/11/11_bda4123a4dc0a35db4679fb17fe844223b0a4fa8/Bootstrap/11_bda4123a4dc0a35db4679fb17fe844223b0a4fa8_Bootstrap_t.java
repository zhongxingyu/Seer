 package com.nearinfinity.honeycomb.mysql;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.log4j.Logger;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 import com.nearinfinity.honeycomb.HoneycombException;
 import com.nearinfinity.honeycomb.config.ConfigurationHolder;
 import com.nearinfinity.honeycomb.config.ConfigurationParser;
 import com.nearinfinity.honeycomb.hbase.HBaseModule;
 import com.nearinfinity.honeycomb.hbaseclient.Constants;
 
import static java.lang.String.format;

 
 public final class Bootstrap extends AbstractModule {
     private static final String CONFIG_PATH = "/etc/mysql";
     private static final String CONFIG_FILENAME = "honeycomb.xml";
     private static final String CONFIG_SCHEMA_FILENAME = "honeycomb.xsd";
 
     private static final Logger logger = Logger.getLogger(Bootstrap.class);
 
     private ConfigurationHolder configHolder;
 
     private Bootstrap() {
     }
 
     /**
      * The initial function called by JNI to wire-up the required object graph dependencies
      *
      * @return {@link HandlerProxyFactory} with all dependencies setup
      */
     public static HandlerProxyFactory startup() {
         Bootstrap bootstrap = new Bootstrap();
         bootstrap.readConfiguration();
 
         Injector injector = Guice.createInjector(bootstrap);
         return injector.getInstance(HandlerProxyFactory.class);
     }
 
     @Override
     protected void configure() {
         bind(String.class).annotatedWith(Names.named(Constants.DEFAULT_TABLESPACE)).toInstance(Constants.HBASE_TABLESPACE);
 
         // Setup the HBase bindings only if the adapter has been configured
         if( configHolder.isAdapterConfigured(Constants.HBASE_TABLESPACE) ) {
             try {
                 HBaseModule hBaseModule = new HBaseModule(configHolder);
                 install(hBaseModule);
             } catch (IOException e) {
                 logger.fatal("Failure during HBase initialization.", e);
                 throw new RuntimeException(e);
             }
         }
     }
 
     /**
      * Initiates the process for reading the application configuration data
      */
     private void readConfiguration() {
         final File configFile = new File(CONFIG_PATH, CONFIG_FILENAME);
         final File configSchemaFile = new File(CONFIG_PATH, CONFIG_SCHEMA_FILENAME);
 
         if( isFileAvailable(configFile) && isFileAvailable(configSchemaFile) ) {
             if( ConfigurationParser.validateConfigFile(configSchemaFile, configFile) ) {
                 try {
                     final ConfigurationParser configParser = new ConfigurationParser();
                     configHolder = configParser.parseConfig(configFile, new Configuration());
 
                    logger.debug(format("Read %d configuration properties ",
                             configHolder.getConfiguration().size()));
                 } catch (ParserConfigurationException e) {
                     logger.fatal("The XML parser was not configured properly.", e);
                     throw new HoneycombException("XML parser could not be configured correctly.", e);
                 }
             } else {
                final String errorMsg = format("Configuration file validation failed. Check %s for correctness.", configFile.getPath());
                 logger.fatal(errorMsg);
                 throw new RuntimeException(errorMsg);
             }
         }
     }
 
     /**
      * Determines if the specified file is accessible and available for reading
      * @param file The file to inspect
      * @return True if file is available, False otherwise
      */
     private static boolean isFileAvailable(final File file) {
         if( !(file.exists() && file.canRead() && file.isFile()) ) {
            final String errorMsg = format("File is not available: %s", file.getAbsolutePath());
             logger.fatal(errorMsg);
             throw new RuntimeException(errorMsg);
         }
 
         return true;
     }
 }
