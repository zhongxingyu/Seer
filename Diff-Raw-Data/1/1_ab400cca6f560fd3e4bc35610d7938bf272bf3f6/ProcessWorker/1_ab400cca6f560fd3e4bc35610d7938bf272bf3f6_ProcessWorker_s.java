 package hu.sztaki.ilab.longneck.process.task;
 
 import hu.sztaki.ilab.longneck.Record;
 import hu.sztaki.ilab.longneck.bootstrap.PropertyUtils;
 import hu.sztaki.ilab.longneck.process.FailException;
 import hu.sztaki.ilab.longneck.process.FilterException;
 import hu.sztaki.ilab.longneck.process.FrameAddressResolver;
 import hu.sztaki.ilab.longneck.process.Kernel;
 import hu.sztaki.ilab.longneck.process.block.Sequence;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Molnár Péter <molnarp@sztaki.mta.hu>
  */
 public class ProcessWorker extends AbstractTask implements Runnable {
     
     /** Log to write messages to. */
     private final Logger LOG = Logger.getLogger(ProcessWorker.class);
     /** The source queue, where records are read from. */
     private final BlockingQueue<QueueItem> sourceQueue;
     /** The target queue, where records are written to. */
     private final BlockingQueue<QueueItem> targetQueue;
     /** The error queue, where errors are written to. */
     private final BlockingQueue<QueueItem> errorQueue;
     /** Local queue for cloned records. */
     private final List<Record> localCloneQueue = new ArrayList<Record>();
     
     /** Enable running of thread. */
     private volatile boolean running = true;    
     /** The bulk size. */
     private int bulkSize = 100;
     
     private Kernel kernel;
 
     public ProcessWorker(BlockingQueue<QueueItem> sourceQueue, BlockingQueue<QueueItem> targetQueue, 
             BlockingQueue<QueueItem> errorQueue, FrameAddressResolver frameAddressResolver, 
             Sequence topLevelSequence,
             Properties runtimeProperties) {
         
         this.sourceQueue = sourceQueue;
         this.targetQueue = targetQueue;
         this.errorQueue = errorQueue;
         
         kernel = new Kernel(topLevelSequence, frameAddressResolver, localCloneQueue);
         
         // Read settings from runtime properties
         measureTimeEnabled = PropertyUtils.getBooleanProperty(
                 runtimeProperties, "measureTimeEnabled", false);
     }
     
     @Override
     public void run() {
         // Run parent method
         super.run();
         
         LOG.info("Starting up.");
         
         // Create queue item list
         List<Record> queueItemList  = new ArrayList<Record>(bulkSize);
 
         boolean shutdownReceived = false;
         List<Record> targetRecords = new ArrayList<Record>(bulkSize);
         List<Record> errorRecords = new ArrayList<Record>(bulkSize);
         QueueItem item;
         
         try {
             while (running && (! shutdownReceived || ! localCloneQueue.isEmpty())) {
                 item = null;
                 
                 // Query cloned records
                 if (localCloneQueue.size() >= bulkSize || shutdownReceived) {
                     // Determine count of removable elements
                     int subListSize = Math.min(bulkSize, localCloneQueue.size());
                     
                     // Clear queue item list and add elements from cloned record queue
                     queueItemList.clear();
                     queueItemList.addAll(localCloneQueue.subList(0, subListSize));
                     
                     // Remove items from cloned record queue
                     localCloneQueue.subList(0, subListSize).clear();
                     
                     // Create new queue item
                     item = new QueueItem(queueItemList);
                     // Increase counter
                     stats.cloned += subListSize;
                 }
                 
                 if (item == null && ! shutdownReceived) {
                     // Get record from source queue; cloned records are taken first
                     item = sourceQueue.poll(100, TimeUnit.MILLISECONDS);                    
                 }
                 
                 // Check record, if null
                 if (item == null) {
                     continue;
                 } 
                 
                 stats.in += item.getRecords().size();
                 
                 if (item.isNoMoreRecords()) {
                     shutdownReceived = true;
                 }
 
                 List<Record> inRecords = item.getRecords();
 
                 for (Record record : inRecords) {
                     try {
                         // Process with kernel
                         kernel.process(record);
 
                         // Add record to output
                         targetRecords.add(record);
                         errorRecords.add(record);
 
                     }  catch (FailException ex) {                        
                         LOG.debug(ex.getMessage(), ex);
                         stats.failed += 1;
                     } catch (FilterException ex) {
                         // LOG.info(ex.getMessage(), ex);
                         LOG.trace(ex.getMessage()) ;
                         stats.filtered += 1;
                     }
                 }
                 
                 // Write to output
                 if (! targetRecords.isEmpty()) {
                     QueueItem outItem = new QueueItem(targetRecords);
                     
                     targetQueue.put(outItem);
                     stats.out += outItem.getRecords().size();
                 }
                 
                 if (! errorRecords.isEmpty()) {
                     errorQueue.put(new QueueItem(errorRecords));
                 }
                 
                 // Clean up
                 targetRecords.clear();
                 errorRecords.clear();
             }
         } catch (InterruptedException ex) {
             LOG.info("Interrupted.", ex);
         } catch (Exception ex) {                    
             LOG.fatal("Fatal error during processing.", ex);
         }
         
         // Log timings
         if (measureTimeEnabled) {
             stats.totalTimeMillis = this.getTotalTime();
             stats.blockedTimeMillis = 
                     mxBean.getThreadInfo(Thread.currentThread().getId()).getBlockedTime();
         }
         stats.setMeasureTimeEnabled(measureTimeEnabled);
 
         LOG.info(stats.toString());
         LOG.info("Shutting down.");
     }
 
     public BlockingQueue<QueueItem> getSourceQueue() {
         return sourceQueue;
     }
     
     public BlockingQueue<QueueItem> getTargetQueue() {
         return targetQueue;
     }
 
     public BlockingQueue<QueueItem> getErrorQueue() {
         return errorQueue;
     }
 
     public Logger getLog() {
         return LOG;
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void setRunning(boolean running) {
         this.running = running;
     }
 }
