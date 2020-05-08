 /**
  * \file DatadockPool.java
  * \brief The DatadockPool class
  * \package datadock;
  */
 
 package dk.dbc.opensearch.components.datadock;
 
 import dk.dbc.opensearch.common.types.DatadockJob;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.common.statistics.Estimate;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.FedoraHandler;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.ClassNotFoundException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.Vector;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 
 /**
  * \ingroup datadock
  *
  * \brief The datadockPool manages the datadock threads and provides methods
  * to add and check running jobs
  */
 public class DatadockPool
 {
     static Logger log = Logger.getLogger("DatadockPool");
     private Vector<FutureTask<DatadockThread>> jobs;
     private final ThreadPoolExecutor threadpool;
     private Estimate estimate;
     private Processqueue processqueue;
     private FedoraHandler fedoraHandler;
     private int shutDownPollTime;
 
     
     /**
      * Constructs the the datadockPool instance
      *
      * @param threadpool The threadpool to submit jobs to
      * @param estimate the estimation database handler
      * @param processqueue the processqueue handler
      * @param fedoraHandler the fedora repository handler
      */
     public DatadockPool( ThreadPoolExecutor threadpool, Estimate estimate, Processqueue processqueue, FedoraHandler fedoraHandler )
     {
         log.debug( "Constructor( threadpool, estimat, processqueue, fedoraHandler ) called" );
 
         this.threadpool = threadpool;
         this.estimate = estimate;
         this.processqueue = processqueue;
         this.fedoraHandler = fedoraHandler;
 
         jobs = new Vector<FutureTask<DatadockThread>>();
 
         shutDownPollTime = 1000; // configuration file
     }
 
     
     /**
      * submits a job to the threadpool for execution by a datadockThread.
      *
      * @param DatadockJob The job to start.
      *
      * @throws RejectedExecutionException Thrown if the threadpools jobqueue is full.
      */
     public void submit( DatadockJob datadockJob ) throws RejectedExecutionException, ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException
     {
         log.debug( String.format( "submit( path='%s', submitter='%s', format='%s' )",
                                   datadockJob.getPath().getRawPath(), datadockJob.getSubmitter(), datadockJob.getFormat() ) );
     
         FutureTask future = getTask( datadockJob );
         threadpool.submit( future );
         jobs.add( future );
     }
 
     
     public FutureTask getTask( DatadockJob datadockJob )throws ConfigurationException, ClassNotFoundException, FileNotFoundException, IOException
     {
         return new FutureTask( new DatadockThread( datadockJob, estimate, processqueue, fedoraHandler ) );
     }
 
 
     /**
      * Checks the jobs submitted for execution, and return the number
      * of active jobs.
      *
      * if a Job throws an exception it is written to the log and the
      * datadock continues.
      *
      * @throws InterruptedException if the job.get() call is interrupted (by kill or otherwise).
      */
     public Vector<CompletedTask> checkJobs() throws InterruptedException 
     {
         log.debug( "checkJobs() called" );
     
         Vector<CompletedTask> finishedJobs = new Vector<CompletedTask>();
         for( FutureTask job : jobs )        
         {
             if( job.isDone() )
             {
                 //log.fatal( "Catched exception from job" );
                 try
                 {
                     log.debug( "Checking job" );
                     
                    Float f = (Float) job.get();
                    finishedJobs.add( new CompletedTask( job, f ) );                    
                 }
                 catch( ExecutionException ee )
                 {                    
                     log.fatal( "Exception caught from job" );
                  
                     //jobs.remove( job );
                     // getting exception from thread
                     Throwable cause = ee.getCause();
                     RuntimeException re = new RuntimeException( cause );
                     
                     log.error( String.format( "Exception Caught: '%s'\n'%s'" , re.getMessage(), re.getStackTrace() ) );
                     // throw re; //shouldnt throw just because thread throw
                 }
             }
         }
         
         for( CompletedTask finishedJob : finishedJobs )
         {
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
