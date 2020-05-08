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
 
 
 package dk.dbc.opensearch.components.datadock;
 
 
 import dk.dbc.opensearch.common.config.DataBaseConfig;
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.db.OracleDBPooledConnection;
 import dk.dbc.opensearch.common.db.PostgresqlDBConnection;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.FedoraObjectRepository;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
 import dk.dbc.opensearch.common.helpers.Log4jConfiguration;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginResolver;
 import dk.dbc.opensearch.common.pluginframework.FlowMapCreator;
 import dk.dbc.opensearch.common.pluginframework.PluginTask;
 import dk.dbc.opensearch.components.harvest.ESHarvest;
 import dk.dbc.opensearch.components.harvest.FileHarvest;
 import dk.dbc.opensearch.components.harvest.FileHarvestLight;
 import dk.dbc.opensearch.components.harvest.HarvesterIOException;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import java.sql.SQLException;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.Properties;
 import java.util.Map;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 
 import oracle.jdbc.pool.OracleDataSource;
 
 import org.apache.commons.configuration.ConfigurationException;
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
 	FileHarvest,
 	FileHarvestLight;
     }
 
     private final static Logger log = Logger.getLogger( DatadockMain.class );
     /** TODO: what is the purpose of rootAppender and startupAppender wrt the startup in this class*/
     private final static ConsoleAppender startupAppender = new ConsoleAppender( new SimpleLayout() );
 
     private static final String logConfiguration = "log4j_datadock.xml";
 
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
     private static HarvestType defaultHarvestType = HarvestType.FileHarvest;
     static java.util.Date startTime = null;
     private boolean terminateOnZeroSubmitted = false;
 
     public DatadockMain()  throws ConfigurationException
     {
         pollTime = DatadockConfig.getMainPollTime();
         queueSize = DatadockConfig.getQueueSize();
         corePoolSize = DatadockConfig.getCorePoolSize();
         maxPoolSize = DatadockConfig.getMaxPoolSize();
         keepAliveTime = DatadockConfig.getKeepAliveTime();
 
         log.debug(  String.format( "Starting Datadock with pollTime = %s", pollTime ) );
         log.debug(  String.format( "Starting Datadock with queueSize = %s", queueSize ) );
         log.debug(  String.format( "Starting Datadock with corePoolSize = %s", corePoolSize ) );
         log.debug(  String.format( "Starting Datadock with maxPoolSize = %s", maxPoolSize ) );
         log.debug(  String.format( "Starting Datadock with keepAliveTime = %s", keepAliveTime ) );
 
         pluginFlowXmlPath = DatadockConfig.getPluginFlowXmlPath();
         pluginFlowXsdPath = DatadockConfig.getPluginFlowXsdPath();
         if( null == pluginFlowXmlPath || null == pluginFlowXsdPath )
         {
             throw new ConfigurationException( "Failed to initialize configuration values for File objects properly (pluginFlowXmlPath or pluginFlowXsdPath)" );
         }
         log.debug(  String.format( "Starting Datadock with pluginFlowXmlPath = %s", pluginFlowXmlPath ) );
         log.debug(  String.format( "Starting Datadock with pluginFlowXsdPath = %s", pluginFlowXsdPath) );
     }
 
 
     /**
      *  Gets the type of a
      * {@link dk.dbc.opensearch.components.harvest.IHarvest Harvester} from a
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
 	    harvestType = harvestTypeFromCmdLine.equals( "FileHarvest" ) ? HarvestType.FileHarvest : harvestType;
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
                 int jobsSubmitted = datadockManager.update();
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
             case FileHarvest:
                 log.trace( "selecting FileHarvest" );
                 harvester = new FileHarvest();
                 break;
             case FileHarvestLight:
                 log.trace( "selecting FileHarvestLight" );
                 harvester = new FileHarvestLight();
                 break;
             default:
                 log.warn( "no harvester explicitly selected, and default type failed. This should not happen, but I'll default to FileHarvester" );
                 harvester = new FileHarvest();
         }
         return harvester;
     }
 
     private IHarvest selectESHarvester() throws ConfigurationException, SQLException, HarvesterIOException
     {
         String dataBaseName = DataBaseConfig.getOracleDataBaseName();
         String oracleCacheName = DataBaseConfig.getOracleCacheName();
         String oracleUrl = DataBaseConfig.getOracleUrl();
         String oracleUser = DataBaseConfig.getOracleUserID();
         String oraclePassWd = DataBaseConfig.getOraclePassWd();
         String minLimit = DataBaseConfig.getOracleMinLimit();
         String maxLimit = DataBaseConfig.getOracleMaxLimit();
         String initialLimit = DataBaseConfig.getOracleInitialLimit();
         String connectionWaitTimeout = DataBaseConfig.getOracleConnectionWaitTimeout();
 
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
 
         return new ESHarvest( connectionPool, dataBaseName );
 
     }
     private void initializeServices() throws ObjectRepositoryException, InstantiationException, IllegalAccessException, PluginException, HarvesterIOException, IllegalStateException, ParserConfigurationException, IOException, IllegalArgumentException, SQLException, InvocationTargetException, SAXException, ConfigurationException, ClassNotFoundException
     {
         log.trace( "Initializing process queue" );
         IProcessqueue processqueue = new Processqueue( new PostgresqlDBConnection() );
 
         log.trace( "Initializing plugin resolver" );
         IObjectRepository repository = new FedoraObjectRepository();
         PluginResolver pluginResolver = new PluginResolver( repository );
         flowMapCreator = new FlowMapCreator( this.pluginFlowXmlPath, this.pluginFlowXsdPath );
         Map<String, List<PluginTask>> flowMap = flowMapCreator.createMap( pluginResolver, repository );
 
         log.trace( "Initializing harvester" );
         IHarvest harvester = this.initializeHarvester();
 
         log.trace( "Initializing the DatadockPool" );
         LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>( this.queueSize );
         ThreadPoolExecutor threadpool = new ThreadPoolExecutor( this.corePoolSize, this.maxPoolSize, this.keepAliveTime, TimeUnit.SECONDS, queue );
         DatadockPool datadockPool = new DatadockPool( threadpool, processqueue, harvester, flowMap );
 
         log.trace( "Initializing the DatadockManager" );
         datadockManager = new DatadockManager( datadockPool, harvester, flowMap );
     }
 
 
     /**
      * The datadocks main method.
      * Starts the datadock and starts the datadockManager.
      */
     public static void main(String[] args)
     {
         try
         {
             Log4jConfiguration.configure( logConfiguration );
         }
         catch( ConfigurationException ex )
         {
             System.out.println( String.format( "Logger could not be configured, will continue without logging: %s", ex.getMessage() ) );
         }
 
         DatadockMain serverInstance = null;
         try
         {
             serverInstance = new DatadockMain();
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
             log.fatal( String.format( "Startup failed: %s", e.getMessage() ) );
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
