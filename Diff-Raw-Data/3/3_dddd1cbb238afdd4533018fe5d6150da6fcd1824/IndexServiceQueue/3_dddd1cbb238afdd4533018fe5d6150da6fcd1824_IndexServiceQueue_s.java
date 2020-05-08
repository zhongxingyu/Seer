 package org.neo4j.util.index;
 
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 
 class IndexServiceQueue extends Thread
 {
     private final GenericIndexService indexService;
     
     private final ConcurrentLinkedQueue<QueueElement> queue =
         new ConcurrentLinkedQueue<QueueElement>();
     
     private final ArrayList<QueueElement> nonCommittedElements = 
         new ArrayList<QueueElement>();
     
     // maximum number of index operation in transaction before commit
     private static final int MAX_TX_OPERATION_COUNT = 100;
     // max time in ms between each commit
     private static final long MAX_WAIT_TIME = 600;
     // max retries for a index operations
     private static final int MAX_ERROR_COUNT = 3;
     // max number of index operations before async also blocks
     private static final int MAX_PENDING_OPERATIONS = 1000;
     private boolean run = true;
     private int txOperationCount = 0;
     private long lastCommit;
     private long currentTimestamp;
     private Transaction tx;
     private boolean done = false;
     
     IndexServiceQueue( GenericIndexService service )
     {
         super( "IndexServiceQueue" );
         this.indexService = service;
     }
     
     void queueIndex( Isolation level, Node node, String key, Object value )
     {
         if ( level == Isolation.ASYNC_OTHER_TX )
         {
             QueueElement qe = new QueueElement( Operation.ADD, node, key, 
                 value );
             queue.add( qe );
             synchronized ( this )
             {
                 this.notify();
             }
             if ( nonCommittedElements.size() >= MAX_PENDING_OPERATIONS )
             {
                 waitForQueueElementNotify( qe );
             }
         }
         else if ( level == Isolation.SYNC_OTHER_TX )
         {
             QueueElement qe = new QueueElement( Operation.ADD, node, key, 
                 value );
             queue.add( qe );
             synchronized ( this )
             {
                 this.notify();
             }
             waitForQueueElementNotify( qe );
         }
         else
         {
             throw new IllegalArgumentException( "Wrong isolation " + level );
         }
     }
     
     void queueRemove( Isolation level, Node node, String key, Object value )
     {
         if ( level == Isolation.ASYNC_OTHER_TX )
         {
             QueueElement qe = new QueueElement( Operation.REMOVE, node, key, 
                 value );
             queue.add( qe );
             synchronized ( this )
             {
                 this.notify();
             }
             if ( nonCommittedElements.size() >= MAX_PENDING_OPERATIONS )
             {
                 waitForQueueElementNotify( qe );
             }
         }
         else if ( level == Isolation.SYNC_OTHER_TX )
         {
             QueueElement qe = new QueueElement( Operation.REMOVE, node, key, 
                 value );
             queue.add( qe );
             synchronized ( this )
             {
                 this.notify();
             }
             waitForQueueElementNotify( qe );
         }
         else
         {
             throw new IllegalArgumentException( "Wrong isolation " + level );
         }
     }
     
     private void waitForQueueElementNotify( QueueElement qe )
     {
         do
         {
             try
             {
                 synchronized ( qe )
                 {
                     qe.wait( 100 );
                 }
             }
             catch ( InterruptedException e )
             {
                 Thread.interrupted();
             }
         } while ( !qe.indexed() );
     }
     
     private enum Operation
     {
         ADD,
         REMOVE,
     }
     
     private static class QueueElement
     {
         final Operation operation;
         final Node node;
         final String key;
         final Object value;
         
         private volatile boolean indexed = false;
         private int errorCount = 0;
         
         QueueElement( Operation operation, Node node, String key, Object value )
         {
             this.operation = operation;
             this.node = node;
             this.key = key;
             this.value = value;
         }
         
         boolean indexed()
         {
             return indexed;
         }
         
         void setIndexed()
         {
             indexed = true;
         }
         
         void tickError()
         {
             errorCount++;
         }
         
         int getErrorCount()
         {
             return errorCount;
         }
     }
     
     @Override
     public void run()
     {
         tx = indexService.beginTx();
         lastCommit = System.currentTimeMillis();
         try
         {
             while ( run || !queue.isEmpty() )
             {
                 QueueElement qe = queue.poll();
                 try
                 {
                     if ( qe != null )
                     {
                         performIndexOperation( qe );
                     }
                     else
                     {
                         synchronized ( this )
                         {
                             this.wait( 100 );
                         }
                         currentTimestamp = System.currentTimeMillis();
                         if ( currentTimestamp - lastCommit > MAX_WAIT_TIME )
                         {
                             tx.success();
                             tx.finish();
                             tx = indexService.beginTx();
                         }
                     }
                 }
                 catch ( InterruptedException e )
                 {
                     Thread.interrupted();
                 }
             }
             tx.success();
         }
         finally
         {
             tx.finish();
         }
         synchronized ( indexService )
         {
            done = true;
             indexService.notify();
         }
     }
     
     private void performIndexOperation( QueueElement qe )
     {
         if ( qe.operation == Operation.ADD )
         {
             indexService.indexThisTx( qe.node, qe.key, qe.value );
         }
         else if ( qe.operation == Operation.REMOVE )
         {
             indexService.removeIndexThisTx( qe.node, qe.key, qe.value );
         }
         nonCommittedElements.add( qe );
         txOperationCount++;
         checkForCommit();
     }
     
     private void checkForCommit()
     {
         if ( txOperationCount >= MAX_TX_OPERATION_COUNT || 
             ( currentTimestamp - lastCommit ) >= MAX_WAIT_TIME )
         {
             tx.success();
             try
             {
                 lastCommit = System.currentTimeMillis();
                 tx.finish();
                 for ( QueueElement doneElement : nonCommittedElements )
                 {
                     doneElement.setIndexed();
                     synchronized ( doneElement )
                     {
                         doneElement.notify();
                     }
                 }
                 nonCommittedElements.clear();
             }
             catch ( Throwable t )
             {
                 handleError( t );
             }
             tx = indexService.beginTx();
         }
     }
     
     private void handleError( Throwable t )
     {
         System.out.println( "Problem with current index batch[" + t + 
             "] retrying..." );
         for ( QueueElement qe : nonCommittedElements )
         {
             qe.tickError();
             if ( qe.getErrorCount() >= MAX_ERROR_COUNT )
             {
                 reportError( qe );
             }
             queue.add( qe );
         }
         nonCommittedElements.clear();
     }
     
     private void reportError( QueueElement qe )
     {
         System.out.println( "Unable to perform indexing operation: " +
             qe.operation + " " + qe.node + " " + qe.key + "," + qe.value );
     }
     
     void stopRunning()
     {
         run = false;
         synchronized ( indexService )
         {
             while ( !done )
             {
                 try
                 {
                     indexService.wait( 500 );
                 }
                 catch ( InterruptedException e )
                 {
                     Thread.interrupted();
                 }
             }
         }
     }
 }
