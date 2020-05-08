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
 
 
 package dk.dbc.opensearch.datadock;
 
 
 import dk.dbc.opensearch.harvest.HarvesterInvalidStatusChangeException;
 import dk.dbc.opensearch.harvest.HarvesterIOException;
 import dk.dbc.opensearch.harvest.HarvesterUnknownIdentifierException;
 import dk.dbc.opensearch.harvest.IHarvest;
 import dk.dbc.opensearch.pluginframework.PluginTask;
 import dk.dbc.opensearch.types.IIdentifier;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * \ingroup datadock
  *
  * \brief The datadockPool manages the datadock threads and provides methods
  * to add and check running jobs
  */
 public class DatadockPool
 {
     private static Logger log = Logger.getLogger( DatadockPool.class );
 
 
     private Map< IIdentifier, FutureTask< Boolean > > jobs;
     private final ThreadPoolExecutor threadpool;
     private IHarvest harvester;
     private Map<String, List<PluginTask>> flowMap;
 
 
     /**
      * \brief private class that handles RejectedExecutions.
      *
      * This class Handles RejectedExecutions by implementing
      * RejectedExecutionHandler, which is thrown if the
      * threadpoolqueue is full. When one is encountered The Handler
      * waits until it can put the element on queue, and only throws an
      * exception if the queue is shutdown
      */
     private class BlockingRejectedExecutionHandler implements RejectedExecutionHandler
     {
 		@Override
 		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
         {
 			if ( executor.isShutdown() )
             {
 				throw new RejectedExecutionException( "Executor has been shutdown" );
 			}
 			
             try
             {
 				executor.getQueue().put(r);
 			}
             catch (InterruptedException e)
             {
                 String error = String.format( "Caught interruption from the executor: %s", e.getMessage() );
                 log.error( error, e );
                 throw new RejectedExecutionException(error, e );
 			}
 		}    	
     }
 
     
     /**
      * 
      * @param threadpool
      * @param harvester
      * @param flowMap
      * @throws ConfigurationException
      */
     public DatadockPool( ThreadPoolExecutor threadpool, IHarvest harvester, Map<String, List<PluginTask>> flowMap ) throws ConfigurationException
     {
         log.debug( "DatadockPool constructor called" );
         this.flowMap = flowMap;
         this.harvester = harvester;
         this.threadpool = threadpool;
 
         jobs = new HashMap< IIdentifier, FutureTask< Boolean > >();
         
         threadpool.setRejectedExecutionHandler( new BlockingRejectedExecutionHandler() );
     }
 
     
     /**
      * submits a job to the threadpool for execution by a datadockThread.
      *
      * @param identifier Identifier for the job to start.
      *
      * @throws RejectedExecutionException Thrown if the threadpools jobqueue is full.
      * @throws ParserConfigurationException 
      * @throws SAXException 
      */
     public void submit( IIdentifier identifier ) throws RejectedExecutionException
     {
         if ( identifier == null )
         {
             String error = "The submitted identifier was null, aborting task";
             log.error( error );
             throw new IllegalArgumentException( error );
         }
         log.debug( String.format( "Submitting job '%s'", identifier ) );
 
         FutureTask<Boolean> future = new FutureTask<Boolean>( new DatadockThread( identifier, harvester, flowMap ) );
         
         if ( future == null )/** \todo: I don't see this happening; even if DatadockThread returns null, FutureTask will still be non-null */
         {
 	    log.error( "DatadockPool submit 'future' is null" );
 	    throw new IllegalStateException( "DatadockPool submit 'future' is null" );
         }
         threadpool.submit( future );
         jobs.put( identifier, future );
     }
 
 
     /**
      * Checks the jobs submitted for execution
      * 
      * if a Job throws an exception it is written to the log and the
      * datadock continues.
      *
      * @throws InterruptedException if the job.get() call is interrupted (by kill or otherwise).
      */
     public void checkJobs() throws InterruptedException
     {
         log.debug( "DatadockPool method checkJobs called" );
     
         log.debug( String.format( "job size = %s", jobs.size() ) );
 
         Set< IIdentifier > finishedJobs = new HashSet< IIdentifier >();
         for( IIdentifier id : jobs.keySet() )
         {
             FutureTask<Boolean> job = jobs.get( id );
             log.debug( String.format( "job is done: %s", job.isDone() ) );
             if( job.isDone() )
             {
                 Boolean success = Boolean.FALSE;
                                 
                 try
                 {
                     log.debug( "DatadockPool checking job" );                    
                     success = job.get();
                 }
                 catch( ExecutionException ee )
                 {                    
                     // getting exception from thread
                     Throwable cause = ee.getCause();
                     log.error( String.format( "Exception caught for identifier: %s, from thread: '%s'", id, cause ), cause );
 
                     log.info( String.format( "Setting status to FAILURE for identifier: %s with message: '%s'", id, cause.getMessage() ) );
                     try
                     {
                        String msg = cause.getMessage() == null ? "" : cause.getMessage(); // avoid giving null to setStatusFailure
                         harvester.setStatusFailure( id, msg );
                     }
                     catch( HarvesterUnknownIdentifierException ex )
                     {
                         String error = String.format( "Failed to set failure status for identifier: %s . Message: %s", id, ex.getMessage() );
                         log.error( error, ex );
                     }
                     catch( HarvesterInvalidStatusChangeException ex )
                     {
                         String error = String.format( "Failed to set failure status for identifier: %s . Message: %s", id, ex.getMessage() );
                         log.error( error, ex );
                     }
                     catch( HarvesterIOException ex )
                     {
                         String error = String.format( "Failed to set failure status for identifier: %s . Message: %s", id, ex.getMessage() );
                         log.error( error, ex );
                     }
                 }
                 
                 log.debug( "DatadockPool adding to finished jobs" );
                 finishedJobs.add( id );
             }
         }
         
         for( IIdentifier finishedJobId : finishedJobs )
         {
             log.debug( String.format( "Removing Job with id: %s. Remaining jobs: %s", finishedJobId, jobs.size() ) );
             jobs.remove( finishedJobId );
         }
     }
 
     
     /**
      * Shuts down the datadockPool. it waits for all current jobs to
      * finish before exiting.
      *
      * @throws InterruptedException if the checkJobs or sleep call is interrupted (by kill or otherwise).
      */
     public void shutdown() throws InterruptedException 
     {
         log.trace( "shutdown() called" );
         
         threadpool.shutdown();
         threadpool.awaitTermination(1, TimeUnit.DAYS);
 
 	// courtesy checkJobs - must be called in order to make sure that 
 	// all jobs are finished correctly.
 	this.checkJobs();
 
         log.trace( "shutdown() called - leaving" );
     }
 }
