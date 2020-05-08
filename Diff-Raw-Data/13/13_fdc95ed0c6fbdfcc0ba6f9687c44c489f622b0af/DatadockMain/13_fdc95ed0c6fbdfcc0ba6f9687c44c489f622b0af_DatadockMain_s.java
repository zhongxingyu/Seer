 /**
    This file is part of opensearch.
    Copyright © 2009, Dansk Bibliotekscenter a/s,
    Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
    opensearch is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    opensearch is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file DatadockMain.java
  * \brief
  */
 
 
 package dk.dbc.opensearch.datadock;
 
 
 import dk.dbc.commons.db.OracleDBPooledConnection;
 import dk.dbc.commons.os.FileHandler;
 import dk.dbc.opensearch.fedora.FcrepoModifier;
 import dk.dbc.opensearch.fedora.FcrepoReader;
 import dk.dbc.opensearch.fedora.ObjectRepositoryException;
 import dk.dbc.opensearch.harvest.ESHarvest;
 import dk.dbc.opensearch.harvest.FileHarvestLight;
 import dk.dbc.opensearch.harvest.HarvesterIOException;
 import dk.dbc.opensearch.harvest.IHarvest;
 import dk.dbc.opensearch.helpers.Log4jConfiguration;
 import dk.dbc.opensearch.pluginframework.PluginException;
 import dk.dbc.opensearch.pluginframework.PluginResolver;
 import dk.dbc.opensearch.pluginframework.FlowMapCreator;
 import dk.dbc.opensearch.pluginframework.PluginTask;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.SQLException;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Map;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 
 import oracle.jdbc.pool.OracleDataSource;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 
 import org.xml.sax.SAXException;
 
 
 /**
  * The Main method of the datadock. It secures all necessary
  * resources for the program, starts the datadockManager and then
  * closes stdin and stdout thus closing connection to the console.
  *
  * It also adds a shutdown hook to the JVM so orderly shutdown is
  * accompleshed when the process is killed.
  */
 public class DatadockMain
 {
     /**
      *  Private enum used to differentiate between various harvester types
      */
     private enum HarvestType
     {
         ESHarvest,
         FileHarvestLight;
     }
 
     private final static Logger log = Logger.getLogger( DatadockMain.class );
 
     
     /** TODO: what is the purpose of rootAppender and startupAppender wrt the startup in this class*/
     private final static ConsoleAppender startupAppender = new ConsoleAppender( new SimpleLayout() );
 
     private static final String logConfiguration = "log4j_datadock.xml";
     private static final String propFileName = "datadock.properties";
 
     protected boolean shutdownRequested = false;
     
     static DatadockManager datadockManager;
     
     static FlowMapCreator flowMapCreator = null;
 
     private final int queueSize;
     private final int corePoolSize;
     private final int maxPoolSize;
     private final long keepAliveTime;
     private final int pollTime;
     private final File pluginFlowXmlPath;
     private final File pluginFlowXsdPath;
     private static HarvestType defaultHarvestType = HarvestType.FileHarvestLight;
     static java.util.Date startTime = null;
     private boolean terminateOnZeroSubmitted = false;
 
     private final int maxToHarvest;
     boolean usePriorityFlag = false;
 
     List< String > dataBaseNames = null;
     String oracleCacheName = "";
     String oracleUrl = ""; 
     String oracleUser = "";
     String oraclePassWd = "";
     String minLimit = "";
     String maxLimit = "";
     String initialLimit = "";
     String connectionWaitTimeout = "";
     
     String host = ""; 
     String port = ""; 
     String user = ""; 
     String pass = ""; 
 
     private String fileHarvestLightDir;
     private String fileHarvestLightSuccessDir;
     private String fileHarvestLightFailureDir;
 
     private String javascriptPath = "";
 
 
     /**
      *
      * 
      * @throws ConfigurationException
      */
     public DatadockMain() throws ConfigurationException
     {
         this( new String[0] );        
     }
     
 
     /**
      *
      * 
      * @param args
      * @throws ConfigurationException
      */
     public DatadockMain( String[] args ) throws ConfigurationException
     {
         /** Try obtaining home path of datadock. If property 'datadock.home' is not set,
          * current working directory is assumed to be home.
          */
         String datadockHome = System.getProperty( "datadock.home" );
         if( datadockHome == null )
         {
             datadockHome = new File( "." ).getAbsolutePath();
         }
         else
         {
             datadockHome = new File( datadockHome ).getAbsolutePath();
         }
         log.info( String.format( "Using datadock.home: %s", datadockHome ) );
         System.out.println( String.format( "Using datadock.home: %s", datadockHome ) );
         
         Configuration config = null;
         
         // Try reading properties file from -Dproperties.file
         String localPropFileName = System.getProperty( "properties.file" );
         
         // If -Dpropfile is not set try reading from either ../config/datadock.properties or ./config/datadock.properties
         if( localPropFileName != null )
         {
             config = new PropertiesConfiguration( localPropFileName );
         }
         else
         {
             localPropFileName = "../config/" + propFileName;
             if( new File( localPropFileName ).exists() )
             {
                 config = new PropertiesConfiguration( localPropFileName );
             }
 
             localPropFileName = "./config/" + propFileName;
             if( new File( localPropFileName ).exists() )
             {
                 config = new PropertiesConfiguration( localPropFileName );
             }
         }
 
         // Throw new ConfigurationException if properties file could not be located.
         if( config == null )
         {
             throw new ConfigurationException( String.format( "Could not load configuration from configuration file: %s", localPropFileName ) );
         }        
         log.info( String.format( "Using properties file: %s", localPropFileName ) );
         
         try
         {
             String configFile = config.getString( "Log4j" );
             if( new File( configFile ).exists() )
             {
                 Log4jConfiguration.configure( configFile );
             }
             else
             {
                 throw new ConfigurationException( String.format( "Could not locate config file at: %s",  config.getString( "Log4j" ) ) );
             }            
             log.info( String.format( "Using config file: %s", configFile ) );
         }
         catch( ConfigurationException ex )
         {
             String errMsg = String.format( "Logger could not be configured, will continue without logging: %s", ex.getMessage() );
             log.error( errMsg );
             System.out.println( errMsg );
         }
 
         pollTime = config.getInt( "MainPollTime" );
         queueSize = config.getInt( "QueueSize" );
         corePoolSize = config.getInt( "CorePoolSize" );
         maxPoolSize = config.getInt( "MaxPoolSize" );
         keepAliveTime  = config.getInt( "KeepAliveTime" );
 
         log.debug(  String.format( "Starting Datadock with pollTime = %s", pollTime ) );
         log.debug(  String.format( "Starting Datadock with queueSize = %s", queueSize ) );
         log.debug(  String.format( "Starting Datadock with corePoolSize = %s", corePoolSize ) );
         log.debug(  String.format( "Starting Datadock with maxPoolSize = %s", maxPoolSize ) );
         log.debug(  String.format( "Starting Datadock with keepAliveTime = %s", keepAliveTime ) );
 
         pluginFlowXmlPath = new File( config.getString( "PluginFlowXmlPath" ) );
         pluginFlowXsdPath = new File( config.getString( "PluginFlowXsdPath" ) );
         if( null == pluginFlowXmlPath || null == pluginFlowXsdPath )
         {
             throw new ConfigurationException( "Failed to initialize configuration values for File objects properly (pluginFlowXmlPath or pluginFlowXsdPath)" );
         }
         log.debug(  String.format( "Starting Datadock with pluginFlowXmlPath = %s", pluginFlowXmlPath ) );
         log.debug(  String.format( "Starting Datadock with pluginFlowXsdPath = %s", pluginFlowXsdPath) );
 
	maxToHarvest = config.getInt( "MaxToHarvest" );
 
        dataBaseNames = new ArrayList< String >();
        dataBaseNames.add( config.getString( "OracleDataBaseNames" ) );
         oracleCacheName = config.getString( "OracleCacheName" );
         oracleUrl = config.getString( "OracleUrl" );
         oracleUser = config.getString( "OracleUserID" );
         oraclePassWd = config.getString ( "OraclePassWd" );
         minLimit = config.getString( "OracleMinLimit" );
         maxLimit = config.getString( "OracleMaxLimit" );
         initialLimit = config.getString( "OracleInitialLimit" );
         connectionWaitTimeout = config.getString( "OracleConnectionWaitTimeout" );
 
         usePriorityFlag = config.getBoolean( "UsePriorityField" );
 
         host = config.getString( "Host" );
         port = config.getString( "Port" );
         user = config.getString( "User" );
         pass = config.getString( "PassPhrase" );
 
         javascriptPath = config.getString( "ScriptPath" );
 
	fileHarvestLightDir = config.getString( "ToHarvest" );
	fileHarvestLightSuccessDir = config.getString( "HarvestDone" );
	fileHarvestLightFailureDir = config.getString( "HarvestFailure" );
     }
 
 
     /**
      *  Gets the type of a
      * {@link dk.dbc.opensearch.harvest.IHarvest Harvester} from a
      * command line parameter or, if that fails, the type of a default harvester
      * specified in the class
      */
     private HarvestType getHarvesterType()
     {
         log.trace( "Trying to get harvester type from commandline" );
         String harvestTypeFromCmdLine = System.getProperty( "harvester" );
         log.debug( String.format( "Found this harvester: %s", harvestTypeFromCmdLine ) );
 
         HarvestType harvestType = null;
         if ( harvestTypeFromCmdLine == null || harvestTypeFromCmdLine.isEmpty() )
         {
             // Only set to default harvester if none is given on commandline
             harvestType = defaultHarvestType;
         }
         else
         {
             harvestType = harvestTypeFromCmdLine.equals( "ESHarvest" ) ? HarvestType.ESHarvest : harvestType;
             harvestType = harvestTypeFromCmdLine.equals( "FileHarvestLight" ) ? HarvestType.FileHarvestLight : harvestType;
 
             if ( harvestType == null )
             {
                 throw new IllegalArgumentException( String.format( "Unknown harvestType: %s", harvestTypeFromCmdLine ) );
             }
         }
 	
         log.debug( String.format( "initialized harvester with type: %s", harvestType ) );
         return harvestType;
 
     }
 
     /**
      * Reads command line arguments and initializes the server mode
      */
     private void setServerMode()
     {
         String mode = System.getProperty( "shutDownOnJobsDone" );
         if( null != mode && mode.equals( "true" ) )
         {
             this.terminateOnZeroSubmitted = true;
         }
     }
 
     /**
      * This method does the actual work of nudging the datadockmanager to get
      * on with processing data from the harvester. If any exceptions are thrown
      * from the manager, this method will issue a shutdown, and exit.
      *
      * @return the number of jobs that have been submitted for processing up until a shutdown request
      */
     private int runServer()
     {
         int mainJobsSubmitted = 0;
         try
         {
             while( !isShutdownRequested() )
             {
                 log.trace( "DatadockMain calling datadockManager update" );
                 long timer = System.currentTimeMillis();
                 int jobsSubmitted = datadockManager.update( this.maxToHarvest );
                 log.debug( String.format( "%s jobs submitted according to the DatadockManager", jobsSubmitted ) );
                 timer = System.currentTimeMillis() - timer;
                 mainJobsSubmitted += jobsSubmitted;
                 if( jobsSubmitted > 0 )
                 {
                     log.info( String.format( "%1$d Jobs submitted in %2$d ms - %3$f jobs/s", jobsSubmitted, timer, jobsSubmitted / (timer / 1000.0) ) );
                 }
                 else
                 {
                     log.info( String.format( "%1$d Jobs submitted in %2$d ms - ", jobsSubmitted, timer ) );
                     if( terminateOnZeroSubmitted )
                     {
                         log.info( "Program set to terminate on empty job queue. Shutting down now" );
                         this.shutdown();
                     }
                     else
                     {
                         Thread.currentThread();
                         Thread.sleep( this.pollTime );
                     }
                 }
             }
         }
         catch( HarvesterIOException hioe )
         {
             String fatal = String.format( "A fatal error occured in the communication with the database: %s", hioe.getMessage() );
             log.fatal( fatal, hioe );
         }
         catch( InterruptedException ie )
         {
             log.fatal( String.format( "InterruptedException caught in Main.runServer: %s", ie.getMessage() ), ie  );
         }
         catch( RuntimeException re )
         {
             log.fatal( String.format( "RuntimeException caught in Main.runServer: %s", re.getMessage() ), re );
         }
         catch( Exception e )
         {
             log.fatal( String.format( "Exception caught in Main.runServer: %s", e.getMessage() ), e );
         }
 //        finally
 //        {
 //            this.shutdown();
 //        }
         log.debug( String.format( "Total # jobs submitted to main: %s", mainJobsSubmitted ) );
         return mainJobsSubmitted;
     }
 
     /**
      * The shutdown hook. This method is called when the program catches a
      * kill signal.
      */
     private void shutdown()
     {
         this.shutdownRequested = true;
 
         try
         {
             log.info( "Shutting down." );
             datadockManager.shutdown();
         }
         catch( InterruptedException e )
         {
             log.error( String.format(  "Interrupted while waiting on main daemon thread to complete: %s", e.getMessage() ) );
             System.exit( -1 );
         }
         catch( HarvesterIOException hioe )
         {
             log.fatal( String.format( "Some error occured while shutting down the harvester: %s", hioe.getMessage() ) );
             System.exit( -1 );
         }
         catch( NullPointerException npe )
         {
             log.fatal( "DatadockManager does not seem to have been started or it crashed. Shutting down with the risk of inconsistencies" );
             System.exit( -1 );
         }
         
         log.info( "Exiting normally." );
     }
 
 
     /**
      * Getter method for shutdown signal.
      */
     public boolean isShutdownRequested()
     {
         return this.shutdownRequested;
     }
 
 
     /**
      * Daemonizes the program, ie. disconnects from the console and
      * creates a pidfile.
      */
     private void daemonize()
     {
         String pidFile = System.getProperty( "daemon.pidfile" );
         FileHandler.getFile( pidFile ).deleteOnExit();
         System.out.close();
         System.err.close();
     }
 
 
     /**
      * Adds the shutdownhook.
      */
     protected void addDaemonShutdownHook()
     {
         Runtime.getRuntime().addShutdownHook( new Thread() {
                 @Override
                 public void run()
                 {
                     shutdown();
                 }
             } );
     }
 
 
     private void collectStatistics( long mainTimer, int mainJobsSubmitted )
     {
         mainTimer = System.currentTimeMillis() - mainTimer;
         if( mainJobsSubmitted > 0 )
         {
             log.info( String.format( "Total: %1$d Jobs submitted in %2$d ms - %3$f jobs/s", mainJobsSubmitted, mainTimer, mainJobsSubmitted / (mainTimer / 1000.0) ) );
         }
         else
         {
             log.info( String.format( "Total: %1$d Jobs submitted in %2$d ms - ", mainJobsSubmitted, mainTimer ) );
         }
     }
 
 
     private IHarvest initializeHarvester() throws SQLException, IllegalArgumentException, ConfigurationException, SAXException, HarvesterIOException, IOException
     {
         log.trace( "Getting harvester type" );
         HarvestType harvestType = this.getHarvesterType();
 
         IHarvest harvester;
         switch( harvestType )
         {
             case ESHarvest:
                 harvester = this.selectESHarvester();
                 break;
             case FileHarvestLight:
                 log.trace( "selecting FileHarvestLight" );
                 harvester = new FileHarvestLight( fileHarvestLightDir, fileHarvestLightSuccessDir, fileHarvestLightFailureDir );
                 break;
             default:
                 log.warn( "no harvester explicitly selected, and default type failed. This should not happen, but I'll default to FileHarvestLight" );
                 harvester = new FileHarvestLight( fileHarvestLightDir, fileHarvestLightSuccessDir, 
 						  fileHarvestLightFailureDir );
         }
         return harvester;
     }
 
 
     private IHarvest selectESHarvester() throws ConfigurationException, SQLException, HarvesterIOException
     {
 //        List< String > dataBaseNames = DataBaseConfig.getOracleDataBaseNames();
 //        String oracleCacheName = DataBaseConfig.getOracleCacheName();
 //        String oracleUrl = DataBaseConfig.getOracleUrl();
 //        String oracleUser = DataBaseConfig.getOracleUserID();
 //        String oraclePassWd = DataBaseConfig.getOraclePassWd();
 //        String minLimit = DataBaseConfig.getOracleMinLimit();
 //        String maxLimit = DataBaseConfig.getOracleMaxLimit();
 //        String initialLimit = DataBaseConfig.getOracleInitialLimit();
 //        String connectionWaitTimeout = DataBaseConfig.getOracleConnectionWaitTimeout();
 //
 //        boolean usePriorityFlag = HarvesterConfig.getPriorityFlag();
 
         log.info( String.format( "DB Url : %s ", oracleUrl ) );
         log.info( String.format( "DB User: %s ", oracleUser ) );
         OracleDataSource ods;
         try
         {
             ods = new OracleDataSource();
 
             // set db-params:
             ods.setURL( oracleUrl );
             ods.setUser( oracleUser );
             ods.setPassword( oraclePassWd );
 
             // set db-cache-params:
             ods.setConnectionCachingEnabled( true ); // connection pool
 
             // set the cache name
             ods.setConnectionCacheName( oracleCacheName );
 
             // set cache properties:
             Properties cacheProperties = new Properties();
 
             cacheProperties.setProperty( "MinLimit", minLimit );
             cacheProperties.setProperty( "MaxLimit", maxLimit );
             cacheProperties.setProperty( "InitialLimit", initialLimit );
             cacheProperties.setProperty( "ConnectionWaitTimeout", connectionWaitTimeout );
             cacheProperties.setProperty( "ValidateConnection", "true" );
 
             ods.setConnectionCacheProperties( cacheProperties );
 
         }
         catch( SQLException sqle )
         {
             String errorMsg = "An SQL error occured during the setup of the OracleDataSource";
             log.fatal( errorMsg, sqle );
             throw sqle;
         }
 
         OracleDBPooledConnection connectionPool = new OracleDBPooledConnection( oracleCacheName, ods );
 
         return new ESHarvest( connectionPool, dataBaseNames, usePriorityFlag );
 
     }
 
 
     private void initializeServices() throws ObjectRepositoryException, InstantiationException, IllegalAccessException, PluginException, HarvesterIOException, IllegalStateException, ParserConfigurationException, IOException, IllegalArgumentException, SQLException, InvocationTargetException, SAXException, ConfigurationException, ClassNotFoundException
     {
         log.trace( "Initializing plugin resolver" );
 //        String host = FedoraConfig.getHost();
 //        String port = FedoraConfig.getPort();
 //        String user = FedoraConfig.getUser();
 //        String pass = FedoraConfig.getPassPhrase();
         FcrepoReader reader = new FcrepoReader( host, port );
         FcrepoModifier modifier = new FcrepoModifier( host, port, user, pass );
         PluginResolver pluginResolver = new PluginResolver();
 
         //String javascriptPath = FileSystemConfig.getScriptPath();
 
         flowMapCreator = new FlowMapCreator( this.pluginFlowXmlPath, this.pluginFlowXsdPath );
         Map<String, List<PluginTask>> flowMap = flowMapCreator.createMap( pluginResolver, reader, modifier, javascriptPath );
 
         log.trace( "Initializing harvester" );
         IHarvest harvester = this.initializeHarvester();
 
         log.trace( "Initializing the DatadockPool" );
         LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>( this.queueSize );
         ThreadPoolExecutor threadpool = new ThreadPoolExecutor( this.corePoolSize, this.maxPoolSize, this.keepAliveTime, TimeUnit.SECONDS, queue );
         DatadockPool datadockPool = new DatadockPool( threadpool, harvester, flowMap );
 
         log.trace( "Initializing the DatadockManager" );
         datadockManager = new DatadockManager( datadockPool, harvester, flowMap );
     }
 
 
     /**
      * The datadocks main method.
      * Starts the datadock and starts the datadockManager.
      */
     public static void main(String[] args)
     {
         DatadockMain serverInstance = null;
         try
         {
             serverInstance = new DatadockMain( args );
             serverInstance.setServerMode();
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Could not get configure DatadockMain object: %s", ex.getMessage() );
             log.fatal( error, ex );
             //we cannot guarantee a serverInstance to call shutdown on:
             System.exit( -1 );
         }
 
         log.removeAppender( "RootConsoleAppender" );
         log.addAppender( startupAppender );
 
         try
         {
             serverInstance.initializeServices();
         }
         catch ( Exception e )
         {
             System.out.println( "Startup failed." + e.getMessage() );
             log.fatal( String.format( "Startup failed: %s", e.getMessage() ), e );
             serverInstance.shutdown();
 
         }
         finally
         {
             log.removeAppender( startupAppender );
         }
 
         log.info( "Daemonizing Datadock server" );
         serverInstance.daemonize();
         serverInstance.addDaemonShutdownHook();
 
         log.info( "Starting processing of data" );
         long mainTimer = System.currentTimeMillis();
         int mainJobsSubmitted = serverInstance.runServer();
 
         log.info( "Collecting and printing processing statistics" );
         serverInstance.collectStatistics( mainTimer, mainJobsSubmitted );
     }
 }
