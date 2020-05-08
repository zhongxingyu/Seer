 /**
  * \file DatadockManager.java
  * \brief The DatadockManager class
  * \package datadock;
  */
 
 package dk.dbc.opensearch.components.datadock;
 
 /*
    
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
     XMLConfiguration config = null;    
     Vector< DatadockJob > registeredJobs = null;
     
     
     /**
      * Constructs the the DatadockManager instance.
      */
     public DatadockManager( DatadockPool pool, IHarvester harvester ) throws ConfigurationException, ParserConfigurationException, SAXException, IOException
     {
         log.debug( "Constructor( pool, harvester ) called" );
 
         this.pool = pool;
         
         this.harvester = harvester;
         harvester.start();
 
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
         	try
         	{
         		pool.submit( job );
         		registeredJobs.remove( 0 );
         		log.debug( String.format( "submitted job: '%s'", job.getUri().getRawPath() ) );
 	        }
         	catch( RejectedExecutionException re )
         	{
         		log.debug( String.format( "job: '%s' rejected, trying again", job.getUri().getRawPath() ) );	           	
         	}
         }
         
         //checking jobs
         Vector<CompletedTask> finishedJobs = pool.checkJobs();
        
     }
     
     
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
