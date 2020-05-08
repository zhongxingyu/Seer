 package dbc.opensearch.components.datadock;
 
 import dbc.opensearch.tools.Estimate;
 import dbc.opensearch.tools.Processqueue;
 import dbc.opensearch.tools.FedoraHandler;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.*;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 public class DataDockPool {
 
     private boolean initialised; /** tells whether the pool is initialised */
     private ExecutorService threadExecutor; /** The threadpool */
     private Estimate estimate;
     private Processqueue processqueue;
     private FedoraHandler fedoraHandler;
     Logger log = Logger.getLogger("DataDockPool");
 
     /**
      * Constructor
      * Checks that the number of threads the threadpool is instantiated with
      * is legal and initializes a FixedThreadPool
      * \see java.util.concurrent.FixedThreadPool
      * @throws IllegalArgumentException if the threadpool is tried initialized with no threads
      */
    DataDockPool( int numberOfThreads, Estimate estimate, Processqueue processqueue, FedoraHandler fedoraHandler ) throws IllegalArgumentException{
         
         this.estimate = estimate;
         this.processqueue = processqueue;
         this.fedoraHandler = fedoraHandler;
         
         if ( numberOfThreads <= 0 ){
             throw new IllegalArgumentException( "refusing to construct empty pool" );
         }        
         threadExecutor = Executors.newFixedThreadPool(numberOfThreads);
         /** \todo: is this boolean useful? */
         initialised = true;
     }
     
 
     /**
      * creates and joins a DataDock thread to the workerthreads in the pool
      * returns an estimate for the processing time of the data
      * needs the cargocontainer as an argument
      * This method should have a more telling name form the callers
      * point of view
      * @returns the FutureTask with the (future) return value of the DataDock calls
      * @throws IllegalArgumentException if the threadpool is not initialized
      * @throws ConfigurationException if the DataDock could not be initialized
      */
     public FutureTask createAndJoinThread( CargoContainer cc )throws IllegalArgumentException, ConfigurationException, ClassNotFoundException{
         /** \todo: this boolean can only and always be true */
         if(!initialised){
             throw new IllegalArgumentException("trying to create a thread without a threadpool");
         }
 
         log.info( String.format( "Creating the FutureTask with a DataDock" ) );
         FutureTask future = new FutureTask(new DataDock(cc, estimate, processqueue, fedoraHandler ));
         
         log.debug( "join the thread to the pool" );
         threadExecutor.submit(future);
         log.debug( "return the FutureTask to the caller" );
         return future;
     }
 }
