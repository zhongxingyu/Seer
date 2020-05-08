 package juitar.worker.queue;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * @author sha1n
  * Date: 1/19/13
  */
 class ServiceBoundWorker implements Runnable {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBoundWorker.class);

     private final WorkQueue queue;
     private final AtomicBoolean serviceStarted;
     private final Worker worker;
 
     public ServiceBoundWorker(final Worker worker, final WorkQueue queue, final AtomicBoolean serviceStarted) {
         this.worker = worker;
         this.queue = queue;
         this.serviceStarted = serviceStarted;
     }
 
     @Override
     public final void run() {
         while (serviceStarted.get()) {
            Work work = null;
             try {
                work = queue.take();
 
                 Result result = worker.doWork(work);
 
                 work.getResultChannel().onSuccess(result);
             } catch (InterruptedException e) {
                LOGGER.warn("Worker thread interrupted.", e);
                 Thread.currentThread().interrupt(); // Reset interrupted state.
            } catch (Exception e) {
                LOGGER.error("Worker caught an exception", e);
                if (work != null) {
                    work.getResultChannel().onFailure(new Result(work.getId()), e);
                }
             } catch (Throwable e) {
                LOGGER.error("SEVERE: Worker caught non-java.lang.Exception type.", e);
             }
         }
     }
 }
