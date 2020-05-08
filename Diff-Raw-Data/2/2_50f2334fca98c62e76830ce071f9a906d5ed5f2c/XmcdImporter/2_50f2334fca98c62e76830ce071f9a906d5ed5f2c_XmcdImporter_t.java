 package my.triviagame.dal;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Queues;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import my.triviagame.xmcd.XmcdDisc;
 import my.triviagame.xmcd.XmcdDiscStream;
 import my.triviagame.xmcd.XmcdException;
 
 /**
  * Manages an import operation of FreeDB update/complete data.
  */
 public class XmcdImporter {
 
     /** By default, handle xmcd data in batches of 10,000 */
     public static final int DEFAULT_BATCH_SIZE = 10000;
     
     /** By default, buffer ahead up to 4 batches */
     public static final int DEFAULT_BUFFER_AHEAD = 4;
     
    public static final Logger logger = Logger.getLogger(XmcdImporter.class.getName());
     
     /**
      * @param dal the underlying {@link DAL} to use
      */
     public XmcdImporter(DAL dal) {
         this(dal, DEFAULT_BATCH_SIZE, DEFAULT_BUFFER_AHEAD);
     }
     
     /**
      * @param dal the underlying {@link DAL} to use
      * @param batchSize count of discs to process in local index per batch before pushing them to the DB
      * @param bufferAhead count of batches to buffer ahead
      */
     public XmcdImporter(DAL dal, int batchSize, int bufferAhead) {
         this.dal = dal;
         this.batchSize = batchSize;
         this.bufferAhead = bufferAhead;
     }
     
     /**
      * Import a Freedb archive (update or complete).
      */
     public void importFreedb(File archive) throws IOException {
         importFreedb(Collections.singleton(archive));
     }
     
     /**
      * Import any number of Freedb archives (e.g. a series of updates).
      */
     public void importFreedb(Iterable<File> archives) throws IOException {
         final XmcdDiscStream discsIterator = XmcdDiscStream.extractAll(archives);
         final BlockingQueue<List<XmcdDisc>> batchQueue = Queues.newArrayBlockingQueue(bufferAhead);
         
         // Background worker thread for making sure extraction & parsing are keeping the I/O & CPU busy
         Thread extractThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     while (discsIterator.hasNext() && !stop) {
                         List<XmcdDisc> nextBatch = Lists.newArrayListWithCapacity(batchSize);
                         while (discsIterator.hasNext() && nextBatch.size() < batchSize) {
                             try {
                                 nextBatch.add(discsIterator.next());
                             } catch (XmcdException ex) {
                                 logger.log(Level.WARNING, "Error while parsing xmcd file", ex);
                             } catch (Exception ex) {
                                 logger.log(Level.SEVERE, "Unexpected exception", ex);
                                 // Stop extracting
                                 break;
                             }
                             filesParsed++;
                         }
                         batchQueue.put(nextBatch);
                     }
                 } catch (InterruptedException ex) {
                     // Stop extracting
                 } finally {
                     discsIterator.close();
                     try {
                         // Signal to the consumer that there are no batches left to process
                         batchQueue.put(null);
                     } catch (InterruptedException ex) {
                         Thread.currentThread().interrupt();
                     }
                 }
             }
         });
         
         List<XmcdDisc> nextBatch;
         try {
             while ((nextBatch = batchQueue.take()) != null && !stop) {
                 // TODO process this batch (waiting for DAL interface)
                 filesImported += nextBatch.size();
             }
         } catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
         }
     }
     
     /**
      * Gets a status report of how many files have been parsed so far.
      */
     public long getFilesParsedCount() {
         return filesParsed;
     }
     
     /** 
      * Gets a status report of how many files have been pushed to the DB so far.
      */
     public long getFilesImportedCount() {
         return filesImported;
     }
     
     /**
      * Orders the processing to stop.
      */
     public void stop() {
         stop = true;
     }
     
     /** Underlying DAL */
     private final DAL dal;
     /** How many {@link XmcdDisc}s to process on each batch */
     private final int batchSize;
     /** How many batches to buffer ahead at most */
     private final int bufferAhead;
     /** Count of how many files have been parsed so far */
     private long filesParsed = 0;
     /** Count of how many files have been imported to the DB so far */
     private long filesImported = 0;
     /** Whether to stop execution */
     private boolean stop = false;
 }
