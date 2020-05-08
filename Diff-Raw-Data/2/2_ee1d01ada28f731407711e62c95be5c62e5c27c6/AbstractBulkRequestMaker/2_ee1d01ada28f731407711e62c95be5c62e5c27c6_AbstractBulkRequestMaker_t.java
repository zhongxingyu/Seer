 package ecologylab.bigsemantics.service.utils;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @author quyin
  * 
  */
 public abstract class AbstractBulkRequestMaker
 {
 
   static Logger logger = Logger.getLogger(AbstractBulkRequestMaker.class);
 
   String        serviceBase;
 
   int           success;
 
   int           failure;
 
   protected AbstractBulkRequestMaker(String serviceBase)
   {
     this.serviceBase = serviceBase;
   }
 
   public void request(List<String> urls, boolean randomPermute, int nThreads)
       throws InterruptedException
   {
     assert urls.size() > 0;
     assert nThreads > 0;
 
     success = 0;
     failure = 0;
 
     if (randomPermute)
     {
       Collections.shuffle(urls);
     }
 
     ExecutorService es = Executors.newFixedThreadPool(nThreads);
     for (int i = 0; i < urls.size(); ++i)
     {
       final String url = urls.get(i);
       es.submit(new Runnable()
       {
         @Override
         public void run()
         {
           long t0 = System.currentTimeMillis();
           int responseCode = request(url);
           long dt = System.currentTimeMillis() - t0;
           if (responseCode >= 100 && responseCode < 400)
             reportSuccess(url, responseCode, dt);
           else
             reportFailure(url, responseCode, dt);
         }
       });
     }
     es.shutdown();
     es.awaitTermination(10 * urls.size(), TimeUnit.SECONDS);
 
     reportFinalResult(success, failure);
   }
 
   private void reportFinalResult(int success, int failure)
   {
     logger.info(String.format("Total: %s,  Success: %d (%.2f%%)\n",
                               success + failure,
                               success,
                              success * 100.0 / (success + failure)));
   }
 
   protected void reportSuccess(String url, int responseCode, long ms)
   {
     logger.info(String.format("%12s: SUCC %d (%.3fsec) [%s]\n",
                               Thread.currentThread().getName(),
                               responseCode,
                               ms * 0.001,
                               url));
     success++;
   }
 
   protected void reportFailure(String url, int responseCode, long ms)
   {
     logger.info(String.format("%12s: FAIL %d (%.3fsec) [%s]\n",
                               Thread.currentThread().getName(),
                               responseCode,
                               ms * 0.001,
                               url));
     failure++;
   }
 
   abstract public int request(String url);
 
 }
