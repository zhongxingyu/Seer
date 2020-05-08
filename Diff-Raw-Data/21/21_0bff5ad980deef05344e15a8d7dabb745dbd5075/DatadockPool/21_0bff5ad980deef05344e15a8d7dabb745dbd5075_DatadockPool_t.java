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
 
 /**
  * \file DatadockPool.java
  * \brief
  */
 
 
 package dk.dbc.opensearch.components.datadock;
 
 
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.pluginframework.PluginResolverException;
 import dk.dbc.opensearch.common.statistics.IEstimate;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Vector;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 import dk.dbc.opensearch.common.fedora.IFedoraAdministration;
 
 
 /**
  * \ingroup datadock
  *
  * \brief The datadockPool manages the datadock threads and provides methods
  * to add and check running jobs
  */
 public class DatadockPool
 {
     static Logger log = Logger.getLogger( DatadockPool.class );
     
     
     private Vector< FutureTask<Float> > jobs;
     private final ThreadPoolExecutor threadpool;
     private IEstimate estimate;
     private IProcessqueue processqueue;
     private int shutDownPollTime;
     private int i = 0;
     private IFedoraAdministration fedoraAdministration;
     private IHarvest harvester;
     
    private class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)  {
			if( executor.isShutdown())  {
					throw new RejectedExecutionException();
			}
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {		
				e.printStackTrace();
				throw new RejectedExecutionException();
			};
		}
    	
    }
     /**
      * Constructs the the datadockPool instance
      *
      * @param threadpool The threadpool to submit jobs to
      * @param estimate the estimation database handler
      * @param processqueue the processqueue handler
      * @param fedoraHandler the fedora repository handler
      */
     public DatadockPool( ThreadPoolExecutor threadpool, IEstimate estimate, IProcessqueue processqueue, IFedoraAdministration fedoraAdministration, IHarvest harvester ) throws ConfigurationException
     {
         log.debug( "DatadockPool constructor called" );
 
         this.harvester = harvester;
         this.threadpool = threadpool;
         this.estimate = estimate;
         this.processqueue = processqueue;
         this.fedoraAdministration = fedoraAdministration;
         jobs = new Vector<FutureTask< Float > >();
         shutDownPollTime = DatadockConfig.getShutdownPollTime();
        
        threadpool.setRejectedExecutionHandler( new BlockingRejectedExecutionHandler() );
     }
 
     
     /**
      * submits a job to the threadpool for execution by a datadockThread.
      *
      * @param DatadockJob The job to start.
      *
      * @throws RejectedExecutionException Thrown if the threadpools jobqueue is full.
      * @throws ParserConfigurationException 
      * @throws PluginResolverException 
      * @throws NullPointerException 
      * @throws SAXException 
      */
     public void submit( DatadockJob datadockJob ) throws RejectedExecutionException, ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException, ServiceException, NullPointerException, PluginResolverException, ParserConfigurationException, SAXException
     {
         log.debug( String.format( "submitter='%s', format='%s' )", datadockJob.getSubmitter(), datadockJob.getFormat() ) );
         log.debug( String.format( "counter = %s", ++i  ) );
 
         FutureTask<Float> future = getTask( datadockJob );
         
         if ( future == null )
         {
         	log.error( "DatadockPool submit 'future' is null" );
         	throw new NullPointerException( "DatadockPool submit 'future' is null" );
         }
         
         threadpool.submit( future );
         jobs.add( future );
     }
 
     
     public FutureTask<Float> getTask( DatadockJob datadockJob ) throws ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException, NullPointerException, PluginResolverException, ParserConfigurationException, SAXException, ServiceException
     {
     	return new FutureTask<Float>( new DatadockThread( datadockJob, estimate, processqueue, fedoraAdministration, harvester ) );
     }
 
 
     /**
      * Checks the jobs submitted for execution, and returns a vector containing 
      * the jobs that are not running anymore
      *
      * if a Job throws an exception it is written to the log and the
      * datadock continues.
      *
      * @throws InterruptedException if the job.get() call is interrupted (by kill or otherwise).
      */
     public Vector< CompletedTask > checkJobs() throws InterruptedException 
     {
         log.debug( "DatadockPool method checkJobs called" );
     
         Vector< CompletedTask > finishedJobs = new Vector< CompletedTask >();
         for( FutureTask job : jobs )        
         {
             if( job.isDone() )
             {
                 Float f = -1f;
                 
                 try
                 {
                     log.debug( "DatadockPool checking job" );                    
                     f = (Float)job.get();
                 }
                 catch( ExecutionException ee )
                 {                    
                     // getting exception from thread
                     Throwable cause = ee.getCause();
                     log.error( String.format( "DatadockPool checkJobs %s", ee.getMessage() ) );
                     log.error( String.format( "Exception Caught: '%s' Message: '%s'", cause.getClass() , cause.getMessage() ) );
                     StackTraceElement[] trace = cause.getStackTrace();
                     for( int i = 0; i < trace.length; i++ )
                     {
                     	log.error( "DatadockPool StackTrace element " + i + " " + trace[i].toString() );
                     }
                 }
                 
                 log.debug( "DatadockPool adding to finished jobs" );
                 finishedJobs.add( new CompletedTask<Float>( job, f ) );
             }
         }
         
         for( CompletedTask finishedJob : finishedJobs )
         {
             log.debug( String.format( "Removing Job Vector< FutureTask > jobs size: %s", jobs.size() ) );
             jobs.remove( finishedJob.getFuture() );
         }
         
         return finishedJobs;
     }
 
     
     /**
      * Shuts down the datadockPool. it waits for all current jobs to
      * finish before exiting.
      *
      * @throws InterruptedException if the checkJobs or sleep call is interrupted (by kill or otherwise).
      */
     public void shutdown() throws InterruptedException 
     {
         log.debug( "shutdown() called" );
     
         boolean activeJobs = true;
         while( activeJobs )
         {
             activeJobs = false;
             for( FutureTask job : jobs )
             {
                 if( ! job.isDone() )
                 {
                     activeJobs = true;
                 }
             }
         }
     }
 }
