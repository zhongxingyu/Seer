 package dbc.opensearch.components.pti;
 
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.log4j.Logger;
 
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 
 import java.net.URL;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 import java.util.NoSuchElementException;
 import java.lang.ClassNotFoundException;
 import java.sql.SQLException;
 import java.lang.InterruptedException;
 import java.util.concurrent.ExecutionException;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.*;
 
 import dbc.opensearch.tools.*;
 import com.mallardsoft.tuple.Tuple;
 import com.mallardsoft.tuple.Pair;
 
 public class PTIPoolAdm {
 
     private static final Logger log = Logger.getRootLogger();
 
     private Processqueue processqueue;
     private PTIPool PTIpool;
 
     private Vector activeThreads;
     private Iterator iter;
 
     private long sleepInMilliSec;
 
     
     /**
      * Constructor 
      */
     public PTIPoolAdm()throws ConfigurationException, RuntimeException, NoSuchElementException, SQLException, ClassNotFoundException, InterruptedException, Exception{
 
         log.debug( "PTIPoolAdm Constructor" );
 
         /** todo: where should sleepInMilliSec be set?? in a configuration file or what*/
         sleepInMilliSec= 200;
         //
         try{
             PTIpool = new PTIPool(10);
             processqueue = new Processqueue();
         }
         catch(ConfigurationException ce){
             throw new ConfigurationException( ce.getMessage() );
         }
         catch(RuntimeException re){
             throw new RuntimeException( re.getMessage() );
         }
 
         activeThreads = new Vector();
         
         // starts the mainloop
         try{
             mainLoop();
         }
         catch(InterruptedException ie){
             throw new InterruptedException( ie.getMessage() );
         }
        catch(Exception ee){
            throw new Exception( ee.getMessage() );
         }
         
     }
 
     /**
      * the mainLoop polls the processqueue and start threads when the queue pops a fedorahandle.   
      * the mainLoop also checks whether already active threads are finished.
      */
     
     private void mainLoop()throws ClassNotFoundException, SQLException, RuntimeException, ConfigurationException, InterruptedException, Exception {
         log.debug( "PTIPoolAdm mainloop" );
 
         long stamp = System.currentTimeMillis() - ( sleepInMilliSec+ 1 ) ;
 
         while(true){
 
             if( System.currentTimeMillis() > stamp+sleepInMilliSec ){
                 // poll processqueue again
                 log.info( "Poll processqueue" );
                 try{
                     startThreads();
                 }
                 catch(ClassNotFoundException cne){
                     throw new ClassNotFoundException( cne.getMessage() );
                 }
                 catch(SQLException sqe){
                     throw new SQLException( sqe.getMessage() );
                 }
                 catch(RuntimeException re){
                     throw new RuntimeException( re.getMessage() );
                 }
                 catch(ConfigurationException ce){
                     throw new ConfigurationException( ce.getMessage() );
                 }
 
                 stamp = System.currentTimeMillis();
             }
 
             try{
                 checkThreads();
             }
             catch(ClassNotFoundException cne){
                 throw new ClassNotFoundException( cne.getMessage() );
             }
             catch(SQLException sqe){
                 throw new SQLException( sqe.getMessage() );
             }
             catch(NoSuchElementException nse){
                 throw new NoSuchElementException( nse.getMessage() );
             }
             catch(InterruptedException ie){
                 throw new InterruptedException( ie.getMessage() );
             }
            catch(Exception ee){// Catching ExecutionException
                throw new Exception( ee.getMessage() );
             }
 
         }
     }
 
     /**
      * Iterates through the activeThreads vector and remove all entries
      * where the associated thread is done. The entries are also
      * committed to the processqueue, which effectivly removes them
      */
    private void checkThreads()throws ClassNotFoundException, SQLException, NoSuchElementException, InterruptedException, Exception {
         log.debug( "PTIPoolAdm.checkThreads() called" );
 
         Pair<FutureTask, Integer> vectorPair = null;
         int queueID;
         FutureTask future;
         Vector removeableThreads = new Vector();
 
         iter = activeThreads.iterator();
 
         while( iter.hasNext() ){
 
             vectorPair = ( Pair ) iter.next();
             future = Tuple.get1(vectorPair);
             queueID = Tuple.get2(vectorPair);
             log.debug( "checking future belonging to queueID = "+queueID );
             if( future.isDone() ){// this thread is done
                long processtime = 0l;
                
                 try{
                     processqueue.commit( queueID );
                 }
                 catch(NoSuchElementException nse){
                     throw new NoSuchElementException( nse.getMessage() );
                 }
                 catch(ClassNotFoundException cne){
                     throw new ClassNotFoundException( cne.getMessage() );
                 }catch(SQLException sqe){
                     throw new SQLException( sqe.getMessage() );
                 }catch(NullPointerException npe){
                     log.debug( String.format( "vectorPair was possibly null? vectorPair = %s", vectorPair.toString() ) );
                 }
 
                 removeableThreads.add( vectorPair );
                 log.info( String.format( "job done, Committed to queue, queueID = %s", queueID ) );
                
             }

         }
        
         iter = removeableThreads.iterator();
         while( iter.hasNext() ){
             activeThreads.remove( iter.next() );
         }        
 
     }
 
     /** pop processqueue until its empty, and starts threads which
      * fetch and process data from the fedora repository. Puts thread
      * (futureTask, and queueid on activeThreads vector)
      */
     private void startThreads()throws ClassNotFoundException, SQLException, RuntimeException, ConfigurationException{
         log.debug( "PTIPoolAdm.startThreads() called" );
 
         boolean fetchedLast = false;
         Pair<String, Integer> queuePair = null;
         Pair<FutureTask, Integer> vectorPair = null;
         String fedoraHandle;
         int queueID;
         FutureTask future;
 
         while( !fetchedLast ){
 
             try{
                 queuePair = processqueue.pop();
             }
             catch(NoSuchElementException nse){
                 log.debug( "processqueue is empty" );
                 fetchedLast = true;
             }
             catch(ClassNotFoundException cne){
                 throw new ClassNotFoundException( cne.getMessage() );
             }
             catch(SQLException sqe){
                 throw new SQLException( sqe.getMessage() );
             }
 
             if( !fetchedLast ){
 
                 fedoraHandle = Tuple.get1(queuePair);
                 queueID = Tuple.get2(queuePair);
 
                 try{
                     log.debug( "starting new thread with fedoraHandle: "+fedoraHandle+" and queueID: "+queueID );
                     future = PTIpool.createAndJoinThread(fedoraHandle );
                 }catch(RuntimeException re){
                     throw new RuntimeException( re.getMessage() );
                 }catch(ConfigurationException ce){
                     throw new ConfigurationException( ce.getMessage() );
                 }
 
                 // add thread to active thread vector
                 vectorPair = Tuple.from(future, queueID);
                 activeThreads.add( vectorPair );
             }
         }
         log.debug( "End of PTIPool.startThread()" );
     }
 }
