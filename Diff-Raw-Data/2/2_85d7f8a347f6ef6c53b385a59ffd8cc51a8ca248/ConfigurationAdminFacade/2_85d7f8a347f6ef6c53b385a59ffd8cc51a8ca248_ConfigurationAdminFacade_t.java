 package org.ops4j.pax.configmanager.internal;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.ops4j.lang.NullArgumentException;
 import org.ops4j.pax.configmanager.IConfigurationFileHandler;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 /**
  * {@code ConfigurationAdminFacade} has most of the code from the old {@code Activator}.
  *
  * @author Edward Yakop
  * @author Makas Tzavellas
  */
 final class ConfigurationAdminFacade
 {
 
     private static final Log LOGGER = LogFactory.getLog( ConfigurationAdminFacade.class );
 
     public static final String DIRECTORY_NAME_FACTORIES = "factories";
     public static final String DIRECTORY_NAME_SERVICES = "services";
     public static final String DEFAULT_CONFIGURATION_LOCATION = "configurations";
 
     /**
      * System property to set where the ConfigurationAdminFacade should load the configuration files from.
      */
     public static final String BUNDLES_CONFIGURATION_LOCATION = "bundles.configuration.location";
     private final List<IConfigurationFileHandler> m_handlers;
     private ConfigurationAdmin m_configAdminService;
     private final ManagedFactoryPropertiesProcessor m_processor = new ManagedFactoryPropertiesProcessor();
     
     public ConfigurationAdminFacade()
     {
         m_handlers = new ArrayList<IConfigurationFileHandler>();
     }
 
     /**
      * Add the specified {@code handler} to this {@code ConfigurationAdminFacade}. The handler will be used to handle
      * configuration file during {@code registerConfigurations}.
      *
      * @param handler The file handler. This argument must not be {@code null}.
      *
      * @throws IllegalArgumentException Thrown if the specified {@code handler} is {@code null}.
      * @since 1.0.0
      */
     final void addFileHandler( IConfigurationFileHandler handler )
         throws IllegalArgumentException
     {
         NullArgumentException.validateNotNull( handler, "handler" );
 
         synchronized( m_handlers )
         {
             m_handlers.add( 0, handler );
 
             // Reload all configurations just in case if this is added later
             try
             {
                 registerConfigurations( null, false );
             } catch( IOException e )
             {
                 String msg = "IOException by either getting the configuration admin or loading the configuration file.";
                 LOGGER.error( msg, e );
             } catch( InvalidSyntaxException e )
             {
                 LOGGER.error( "Invalid syntax. This should not happened.", e );
             }
         }
     }
 
     /**
      * Registers configuration for OSGi Managed services.
      *
      * @param configuration if null then all configuration found will be registered.
      * @param overwrite     A {@code boolean} indicator to overwrite the configuration
      *
      * @throws IOException            Thrown if there is an IO problem during loading of {@code configuration}.
      * @throws InvalidSyntaxException Thrown if there is an invalid exception during retrieval of configurations.
      * @throws IllegalStateException  Thrown if the configuration admin service is not available.
      */
     final void registerConfigurations( String configuration, boolean overwrite )
         throws IOException, InvalidSyntaxException, IllegalStateException
     {
         if( m_configAdminService == null )
         {
             throw new IllegalStateException(
                 "Configuration admin service is not available. Please start configuration admin bundle."
             );
         }
 
         File configDir = getConfigDir();
         if( configDir == null )
         {
 
             return;
         }
 
         Configuration[] existingConfigurations;
         synchronized( this )
         {
             existingConfigurations = m_configAdminService.listConfigurations( null );
         }
 
         Set<String> configCache = new HashSet<String>();
         if( existingConfigurations != null && !overwrite )
         {
             for( Configuration existingConfig : existingConfigurations )
             {
                 configCache.add( existingConfig.getPid() );
             }
         }
 
         // Create configuration for ManagedServiceFactory
         createConfiguration( configuration, configDir, configCache, true );
         // Create configuration for ManagedService
         createConfiguration( configuration, configDir, configCache, false );
     }
 
     private void createConfiguration( String configuration, File configDir, Set<String> configCache, boolean isFactory )
         throws IOException
     {
         File dir;
         if( isFactory )
         {
             dir = new File( configDir, DIRECTORY_NAME_FACTORIES );
         }
         else
         {
             dir = new File( configDir, DIRECTORY_NAME_SERVICES );
         }
 
         if( !dir.exists() )
         {
            LOGGER.info( "Directory [" + dir + "] does not exist." );
             return;
         }
 
         String[] files = dir.list();
         for( String configFile : files )
         {
             createConfigurationForFile( configuration, configFile, configCache, dir, isFactory );
         }
     }
 
     private void createConfigurationForFile(
         String configuration, String configFile, Set<String> configCache, File dir, boolean isFactory
     )
         throws IOException
     {
         if( configuration != null && !configFile.equals( configuration ) )
         {
             return;
         }
 
         // If configuration already exist for the service, dont update. Will be empty if iIsOverwrite is true.
         if( configCache.contains( configFile ) )
         {
             return;
         }
 
         File f = new File( dir, configFile );
         if( !f.isDirectory() )
         {
             List<IConfigurationFileHandler> handlers;
 
             synchronized( m_handlers )
             {
                 handlers = new ArrayList<IConfigurationFileHandler>( m_handlers );
             }
 
             for( IConfigurationFileHandler handler : handlers )
             {
                 if( handler.canHandle( f ) )
                 {
                     handle( handler, configFile, f, isFactory );
                 }
             }
         }
     }
 
 	/**
 	* Handle the extraction and registration of the configuration into the config service.
 	* If a property service.pid exists in the configuration, then that will be used to locate the service instance.
 	* To register the service with a service.pid, do something like
 	* <pre>
 	* Properties filterProp = new Properties();
 	* filterProp.put(Constants.SERVICE_PID, "my.test.service.Interface");
 	* bundleContext.registerService(ManagedService.class.getName(), myServiceInstance, filterProp);
 	* </pre>
 	* in your client code that registeres the managed service.
 	*/
     private void handle( IConfigurationFileHandler handler, String configFile, File file, boolean isFactory )
         throws IOException
     {
         String servicePid = handler.getServicePID( configFile );
         Properties prop = handler.handle( file );
 
         // Find out if a service.pid property is included, use it if it does
 
         String str = (String) prop.get( Constants.SERVICE_PID );
         if( str != null )
         {
             servicePid = str;
         }
 
         synchronized( this )
         {
             if( isFactory )
             {
                 m_processor.process( m_configAdminService, servicePid, prop );                
             }
             else
             {
                 Configuration conf = m_configAdminService.getConfiguration( servicePid, null );
                 conf.update( prop );
             }
         }
 
         LOGGER.info( "Register configuration [" + servicePid + "]" );
     }
 
     private File getConfigDir()
     {
         String configArea = System.getProperty( BUNDLES_CONFIGURATION_LOCATION );
 
         // Only run the configuration changes if the configArea is set.
         if( configArea == null )
         {
             LOGGER.info( "System property [" + BUNDLES_CONFIGURATION_LOCATION + "] is not defined." );
             LOGGER.info( "Using default configurations location [" + DEFAULT_CONFIGURATION_LOCATION + "]." );
             configArea = DEFAULT_CONFIGURATION_LOCATION;
         }
 
         LOGGER.info( "Using configuration from [" + configArea + "]" );
         File dir = new File( configArea );
         if( !dir.exists() )
         {
             String absolutePath = dir.getAbsolutePath();
             LOGGER.error( "Configuration area [" + absolutePath + "] does not exist. Unable to load properties." );
             return null;
         }
         return dir;
     }
 
     /**
      * Dispose this {@code ConfigurationAdminFacade} instance. Once this object instance is disposed, it is not meant to
      * be used again.
      */
     void dispose()
     {
         m_configAdminService = null;
         m_handlers.clear();
     }
 
     final void printConfigFileList( PrintWriter writer, String fileName )
     {
         File configDir = getConfigDir();
 
         if( configDir == null )
         {
             writer.println( "Configuration dir is not setup." );
             return;
         }
 
         if( fileName != null )
         {
             printConfiguration( writer, fileName, configDir );
             return;
         }
 
         String configAbsolutePath = configDir.getAbsolutePath();
         writer.println( "config dir: [" + configAbsolutePath + "] contains the following config files:" );
         String[] files = configDir.list();
         for( String file : files )
         {
             writer.println( file );
         }
     }
 
     private void printConfiguration( PrintWriter writer, String fileName, File configDir )
     {
         File configFile = new File( configDir, fileName );
         String absolutePath = configFile.getAbsolutePath();
         if( !configFile.canRead() || !configFile.exists() )
         {
             writer.println( "Can't read configfile [" + absolutePath + "]" );
             return;
         }
 
         Properties props = new Properties();
         try
         {
             InputStream in = new FileInputStream( configFile );
             props.load( in );
         }
         catch( Exception e )
         {
             String message = "Can't read configfile [" + absolutePath + "] - not a correct config file";
             writer.println( message );
             return;
         }
 
         writer.println( "Config file: [" + absolutePath + "]" );
         for( Object keyObject : props.keySet() )
         {
             String key = (String) keyObject;
             String value = props.getProperty( key );
             writer.println( key + " = " + value );
         }
     }
 
     /**
      * Remove the specified {@code handler} from this {@code ConfigurationAdminFacade}.
      *
      * @param handler The handler to be removed. This argument must not be {@code null}.
      *
      * @throws IllegalArgumentException Thrown if the specified {@code handler} is {@code null}.
      */
     final void removeFileHandler( IConfigurationFileHandler handler )
         throws IllegalArgumentException
     {
         NullArgumentException.validateNotNull( handler, "handler" );
 
         synchronized( m_handlers )
         {
             m_handlers.remove( handler );
         }
     }
 
     /**
      * Set the configuration admin service. Sets to {@code null} if the configuration admin service is not available.
      *
      * @param configurationAdminService The configuration admin.
      */
     final void setConfigurationAdminService( ConfigurationAdmin configurationAdminService )
     {
         synchronized( this )
         {
             m_configAdminService = configurationAdminService;
         }
     }
 }
