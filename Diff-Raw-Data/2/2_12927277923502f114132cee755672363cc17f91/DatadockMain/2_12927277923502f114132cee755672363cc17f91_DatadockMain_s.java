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
 import dk.dbc.opensearch.common.db.IDBConnection;
 import dk.dbc.opensearch.common.db.PostgresqlDBConnection;
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.FedoraObjectRepository;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.helpers.Log4jConfiguration;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.statistics.Estimate;
 import dk.dbc.opensearch.common.statistics.IEstimate;
 import dk.dbc.opensearch.common.types.HarvestType;
 import dk.dbc.opensearch.components.harvest.FileHarvest;
 import dk.dbc.opensearch.components.harvest.FileHarvestLight;
 import dk.dbc.opensearch.components.harvest.ESHarvest;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 import dk.dbc.opensearch.components.harvest.HarvesterIOException;
 
 import java.sql.SQLException;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.Properties;
 
 import dk.dbc.opensearch.common.db.OracleDBPooledConnection;
 import oracle.jdbc.pool.OracleDataSource;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 
 
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
     static Logger log = Logger.getLogger( DatadockMain.class );
 
 
     static protected boolean shutdownRequested = false;
     static DatadockPool datadockPool = null;
     static DatadockManager datadockManager = null;
     static int queueSize;
     static int corePoolSize;
     static int maxPoolSize;
     static long keepAliveTime;
     static int pollTime;
     static IHarvest harvester;
    private static HarvestType harvestType;
     static java.util.Date startTime = null;
 
 
     public DatadockMain() {}
 
 
     public static void init() throws ConfigurationException
     {
         log.trace( "DatadockMain init called" );
 
         pollTime = DatadockConfig.getMainPollTime();
         queueSize = DatadockConfig.getQueueSize();
         corePoolSize = DatadockConfig.getCorePoolSize();
         maxPoolSize = DatadockConfig.getMaxPoolSize();
         keepAliveTime = DatadockConfig.getKeepAliveTime();
     }
 
 
     /**
      * Helper method to avoid static problems in init
      */
     @SuppressWarnings( "unchecked" )
     public Class getClassType()
     {
         return this.getClass();
     }
 
 
     /**
      * The shutdown hook. This method is called when the program catches the kill signal.
      */
     static public void shutdown()
     {
         shutdownRequested = true;
 
         try
         {
             log.info( "Shutting down." );
             datadockManager.shutdown();
         }
         catch( InterruptedException e )
         {
             log.error( "Interrupted while waiting on main daemon thread to complete." );
         }
         catch( HarvesterIOException hioe ) {
             log.fatal( "Some error occured while shutting down the harvester", hioe );
         }
         log.info( "Exiting." );
     }
 
 
     /**
      * Getter method for shutdown signal.
      */
     static public boolean isShutdownRequested()
     {
         return shutdownRequested;
     }
 
 
     /**
      * Daemonizes the program, ie. disconnects from the console and
      * creates a pidfile.
      */
     static public void daemonize()
     {
         String pidFile = System.getProperty( "daemon.pidfile" );
         FileHandler.getFile( pidFile ).deleteOnExit();
         System.out.close();
         System.err.close();
     }
 
 
     /**
      * Adds the shutdownhook.
      */
     static protected void addDaemonShutdownHook()
     {
         Runtime.getRuntime().addShutdownHook( new Thread() {
                 @Override
                 public void run()
                 {
                     shutdown();
                 }
             } );
     }
 
 
     /**
      * The datadocks main method.
      * Starts the datadock and starts the datadockManager.
      */
     static public void main(String[] args) throws Throwable
     {
 
         /** \todo: the value of the configuration file is hardcoded */
         Log4jConfiguration.configure( "log4j_datadock.xml" );
         log.trace( "DatadockMain main called" );
 
         ConsoleAppender startupAppender = new ConsoleAppender(new SimpleLayout());
 
         boolean terminateOnZeroSubmitted = false;
        
         for( String a : args )
         {
             log.warn( String.format( "argument: '%s'", a ) );
             if( a.equals( "--shutDownOnJobsDone" ) )
             {
                 terminateOnZeroSubmitted = true;
             }
             else
             {
                 harvestType = HarvestType.getHarvestType( a );
             }
         }
 
         try
         {
             init();
 
             log.removeAppender( "RootConsoleAppender" );
             log.addAppender( startupAppender );
 
             /** -------------------- setup and start the datadockmanager -------------------- **/
             log.info( "Starting the datadock" );
 
             log.trace( "initializing resources" );
 
             // DB access
             IDBConnection dbConnection = new PostgresqlDBConnection();
             IEstimate estimate = new Estimate( dbConnection );
             IProcessqueue processqueue = new Processqueue( dbConnection );
             IObjectRepository repository = new FedoraObjectRepository();
             OracleDataSource ods;
             //IFedoraAdministration fedoraAdministration = new FedoraAdministration( repository );
 
             log.trace( "Starting datadockPool" );
 
             // datadockpool
             LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>( queueSize );
             ThreadPoolExecutor threadpool = new ThreadPoolExecutor( corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS , queue );
             threadpool.purge();
 
             log.trace( "Starting harvester" );
             // harvester;
             switch( harvestType )
             {
             case ESHarvest:
 
                 log.trace( "selecting ES" );
                 String oracleCacheName = DataBaseConfig.getOracleCacheName();
                 String oracleUrl = DataBaseConfig.getOracleUrl();
                 String oracleUser = DataBaseConfig.getOracleUserID();
                 String oraclePassWd = DataBaseConfig.getOraclePassWd();
                 String minLimit = DataBaseConfig.getOracleMinLimit();
                 String maxLimit = DataBaseConfig.getOracleMaxLimit();
                 String initialLimit = DataBaseConfig.getOracleInitialLimit();
                 String connectionWaitTimeout = DataBaseConfig.getOracleConnectionWaitTimeout();
 
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
                     String errorMsg = new String( "An SQL error occured during the setup of the OracleDataSource" );
                     log.fatal( errorMsg, sqle );
                     throw sqle;
                 }
                 OracleDBPooledConnection connectionPool = new OracleDBPooledConnection( oracleCacheName, ods );
 
                 harvester = new ESHarvest( connectionPool, "test" );
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
                 log.warn( "no harvester explicitly selected, running with FileHarvest" );
             }
 
 
             datadockPool = new DatadockPool( threadpool, estimate, processqueue, repository, harvester);
 
             log.trace( "Starting the manager" );
             // Starting the manager
             datadockManager = new DatadockManager( datadockPool, harvester );
 
             /** --------------- setup and startup of the datadockmanager done ---------------- **/
             log.info( "Daemonizing" );
 
             daemonize();
             addDaemonShutdownHook();
         }
         catch ( Exception e )
         {
             System.out.println( "Startup failed." + e );
             log.fatal( "Startup failed.", e);
             throw e;
         }
         finally
         {
             log.removeAppender( startupAppender );
         }
 
         long mainTimer = System.currentTimeMillis();
         int mainJobsSubmitted = 0;
 
 
 
         while( ! isShutdownRequested() )
         {
             try
             {
                 log.trace( "DatadockMain calling datadockManager update" );
 
                 long timer = System.currentTimeMillis();
                 int jobsSubmited = datadockManager.update();
                 timer = System.currentTimeMillis() - timer;
 
                 mainJobsSubmitted += jobsSubmited;
 
                 if (jobsSubmited > 0)
                 {
                     log.info(String.format("%1$d Jobs submittet in %2$d ms - %3$f jobs/s", jobsSubmited, timer, jobsSubmited/ (timer / 1000.0)));
                 }
                 else
                 {
                     log.info(String.format("%1$d Jobs submittet in %2$d ms - ",jobsSubmited, timer));
                     if( terminateOnZeroSubmitted )
                     {
                         shutdown();
                     }
                     else
                     {
                         Thread.currentThread();
                         Thread.sleep(pollTime);
                     }
                 }
 
             }
             catch( InterruptedException ie )
             {
                 /**
                  * \todo: dont we want to get the trace?
                  */
                 log.error( "InterruptedException caught in mainloop: "  + ie, ie);
                 log.error( "  " + ie.getMessage(), ie );
             }
             catch( RuntimeException re )
             {
                 log.error( "RuntimeException caught in mainloop: " + re, re);
                 log.error( "  " + re.getCause().getMessage(), re);
                 throw re;
             }
             catch( Exception e )
             {
                 /**
                  * \todo: dont we want to get the trace?
                  */
                 log.error( "Exception caught in mainloop: " + e.toString(), e );
             }
         }
 
         mainTimer = System.currentTimeMillis() - mainTimer;
 
         if (mainJobsSubmitted > 0)
         {
             log.info(String.format("Total: %1$d Jobs submittet in %2$d ms - %3$f jobs/s", mainJobsSubmitted, mainTimer, mainJobsSubmitted/ (mainTimer / 1000.0)));
         }
         else
         {
             log.info(String.format("Total: %1$d Jobs submittet in %2$d ms - ", mainJobsSubmitted, mainTimer));
 
         }
     }
 }
