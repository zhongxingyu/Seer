 /**
  * \file DatadockManager.java
  * \brief The DatadockManager class
  * \package datadock;
  */
 
 package dk.dbc.opensearch.components.datadock;
 
 
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.pluginframework.PluginResolverException;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.common.types.DatadockJob;
 import dk.dbc.opensearch.components.harvest.IHarvester;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.ClassNotFoundException;
 import java.util.Vector;
 import java.util.concurrent.RejectedExecutionException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * \brief the DataDockManager manages the startup, running and
  * closedown of the associated harvester and threadpool
  */
 public class DatadockManager
 {
     static Logger log = Logger.getLogger( DatadockManager.class );
 
     private DatadockPool pool= null;
     private IHarvester harvester = null;
     private int jobLimit;
 
     XMLConfiguration config = null;
     
     Vector< DatadockJob > registeredJobs = null;
     
     /**
      * Constructs the the DatadockManager instance.
      */
     public DatadockManager( DatadockPool pool, IHarvester harvester ) throws ConfigurationException
     {
         log.debug( "Constructor( pool, harvester ) called" );
 
         this.pool = pool;
         this.harvester = harvester;
         harvester.start();
 
         jobLimit = DatadockConfig.getJobLimit();
 
         registeredJobs = new Vector< DatadockJob >(); 
     }
 
     
     public void update() throws InterruptedException, ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException, ServiceException, NullPointerException, PluginResolverException, ParserConfigurationException, SAXException
     {
         log.debug( "DatadockManager update called" );
       
         // Check if there are any registered jobs ready for docking
         // if not... new jobs are requested from the harvester
         if( registeredJobs.size() == 0 )
         {
             log.debug( "no more jobs. requesting new jobs from the harvester" );
             registeredJobs = harvester.getJobs();
         }
       
         log.debug( "DatadockManager.update: Size of registeredJobs: " + registeredJobs.size() );
         
         for( int i = 0; i < registeredJobs.size(); i++ )
         {
         	DatadockJob job = registeredJobs.get( 0 );
         
             // execute jobs
             //log.debug( String.format( "DatadockManager harvester getJobs called. jobs.size: %s", jobs.size() ) );
        	boolean submitted = false;
    		while( ! submitted )
    		{
 	           	try
 	           	{
 	           		pool.submit( job );
 	           		registeredJobs.remove( 0 );
	           		submitted = true;
 	           		log.debug( String.format( "submitted job: '%s'", job.getUri().getRawPath() ) );
 	           	}
 	           	catch( RejectedExecutionException re )
 	           	{
 	           		log.debug( String.format( "job: '%s' rejected, trying again", job.getUri().getRawPath() ) );	           	
 	           	}
    		}
         }
         
         //checking jobs
         Vector<CompletedTask> finishedJobs = pool.checkJobs();
     }
     
     
     /*public void update() throws InterruptedException, ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException, ServiceException, NullPointerException, PluginResolverException, ParserConfigurationException, SAXException
     {
         log.debug( "DatadockManager update called" );
       
         // Check if there are any registered jobs ready for docking
         // if not... new jobs are requested from the harvester
         if( registeredJobs.size() == 0 )
         {
             log.debug( "no more jobs. requesting new jobs from the harvester" );
             registeredJobs = harvester.getJobs();
         }
       
         for( int i = 0; i < jobLimit; i++)
         {
         	if( registeredJobs.size() == 0 )
             {
             	break;
             }
             
             DatadockJob job = registeredJobs.remove( 0 );
         
             // execute jobs
             //log.debug( String.format( "DatadockManager harvester getJobs called. jobs.size: %s", jobs.size() ) );
             boolean submitted = false;
             
             while( ! submitted )
             {
             	try
             	{
             		pool.submit( job );
             		submitted = true;
             		log.debug( String.format( "submitted job: '%s'", job.getUri().getRawPath() ) );
             	}
             	catch( RejectedExecutionException re )
             	{
             		log.debug( String.format( "job: '%s' rejected, trying again", job.getUri().getRawPath() ) );
             	}
             }
         }
         
         //checking jobs
         Vector<CompletedTask> finishedJobs = pool.checkJobs();
     }*/
     
     
     public void shutdown() throws InterruptedException
     {
         log.debug( "Shutting down the pool" );
         pool.shutdown();        
         log.debug( "The pool is down" );        
         
         log.debug( "Stopping harvester" );        
         harvester.shutdown();
         log.debug( "The harvester is stopped" );
     }
 }
