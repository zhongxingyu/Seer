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
 
 
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.db.IDBConnection;
 import dk.dbc.opensearch.common.db.PostgresqlDBConnection;
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.IFedoraAdministration;
 import dk.dbc.opensearch.common.fedora.FedoraAdministration;
 import dk.dbc.opensearch.common.helpers.Log4jConfiguration;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.statistics.Estimate;
 import dk.dbc.opensearch.common.statistics.IEstimate;
 import dk.dbc.opensearch.components.harvest.FileHarvest;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 import dk.dbc.opensearch.components.harvest.HarvesterIOException;
 
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
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
 
         log.trace( String.format( "queueSIZE = '%s'", queueSize ) );
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
         Runtime.getRuntime().addShutdownHook( new Thread() { public void run() { shutdown(); } } );
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
             IFedoraAdministration fedoraAdministration = new FedoraAdministration();
 
             log.trace( "Starting datadockPool" );
 
             // datadockpool
             LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>( 10 );
             ThreadPoolExecutor threadpool = new ThreadPoolExecutor( corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS , queue );            
             threadpool.purge(); 
             
             log.trace( "Starting harvester" );
             // harvester;
             IHarvest harvester = new FileHarvest();
             
             datadockPool = new DatadockPool( threadpool, estimate, processqueue, fedoraAdministration, harvester);
             
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
         
         while( ! isShutdownRequested() )
         {	 
             try
             {
             	log.trace( "DatadockMain calling datadockManager update" );
                 long timer = System.currentTimeMillis();
                 int jobsSubmited = datadockManager.update();                
                 timer = System.currentTimeMillis() - timer;
                             	
                 if( jobsSubmited > 0 ) {
                	log.info( String.format("%1$d Jobs submittet in %2$d ms - %3$f jobs/s", jobsSubmited, timer, timer/1000.0/jobsSubmited));
                 } else {
                 	log.info( String.format("%1$d Jobs submittet in %2$d ms - ", jobsSubmited, timer));
                     Thread.currentThread();
                     Thread.sleep( pollTime );
                 }
                 
             }
             catch( InterruptedException ie )
             {
                 /**
                  * \todo: dont we want to get the trace?
                  */
                 log.error( "InterruptedException caught in mainloop: "  + ie );
                 log.error( "  " + ie.getMessage() );
             }
             catch( RuntimeException re )
             {
                 log.error( "RuntimeException caught in mainloop: " + re );
                 log.error( "  " + re.getCause().getMessage() );
                 throw re;
             }
             catch( Exception e )
             {
                 /**
                  * \todo: dont we want to get the trace?
                  */
                 log.error( "Exception caught in mainloop: " + e.toString() );
             }
         }
     }
 }
