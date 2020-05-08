 
 package com.randomhumans.svnindex.indexing;
 
 import java.io.IOException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.randomhumans.svnindex.document.ContentDocument;
 import com.randomhumans.svnindex.util.IndexHelper;
 
 public class ContentIndexerThread implements Runnable
 {
     static Log log = LogFactory.getLog(ContentIndexerThread.class);
 
     private static ExecutorService indexerPool = null;
 
     private static final BlockingQueue<ContentDocument> documentQueue = new LinkedBlockingQueue<ContentDocument>();
 
     private volatile static boolean signal = false;
 
     public synchronized static void queueDocument(final ContentDocument d) throws InterruptedException
     {
         ContentIndexerThread.documentQueue.put(d);
     }
 
     public void run()
     {
         try
         {
             // dequeue documents until the signal arrives
             while (!ContentIndexerThread.signal)
             {
                 this.updateDocument(ContentIndexerThread.documentQueue.poll(1, TimeUnit.SECONDS));
             }
 
             // process the remaining documents in the queue
             for (final ContentDocument doc : ContentIndexerThread.documentQueue)
             {
                 this.updateDocument(doc);
             }
         }
         catch (final InterruptedException e)
         {
             ContentIndexerThread.log.warn(e);
         }
         catch (final IOException e)
         {
             ContentIndexerThread.log.error(e);
         }
        catch (RuntimeException e)
        {
            log.fatal(e,e);
            throw(e);
        }
         finally
         {
             try
             {
                 IndexHelper.optimize();
             }
             catch (final IOException e)
             {
                 ContentIndexerThread.log.error(e);
              }
         }
     }
 
     private void updateDocument(final ContentDocument doc) throws IOException
     {        
         if (doc != null)
         {         
             IndexHelper.addIndexDocument(doc);
         }
     }
 
     public static void init(final boolean rebuild) throws IOException
     {
 
         if (rebuild)
         {
             try
             {
                 IndexHelper.createIndex();
             }
             catch (final IOException e)
             {
                 ContentIndexerThread.log.error(e);
                 throw (e);
             }
         }
         ContentIndexerThread.indexerPool = Executors.newSingleThreadExecutor();
         ContentIndexerThread.indexerPool.submit(new ContentIndexerThread());
     }
 
     public static void shutdown()
     {
         ContentIndexerThread.signal = true;
         try
         {
             ContentIndexerThread.log.debug("waiting");
             
             ContentIndexerThread.indexerPool.shutdown();
             while((ContentIndexerThread.documentQueue.size() > 0) && !ContentIndexerThread.indexerPool.awaitTermination(10, TimeUnit.SECONDS))
             {
                 ContentIndexerThread.log.debug(ContentIndexerThread.documentQueue.size());                                
             }
             
             ContentIndexerThread.log.debug("wait complete");
         }
         catch (final InterruptedException e1)
         {
             ContentIndexerThread.log.error(e1);
         }
     }
 
 }
