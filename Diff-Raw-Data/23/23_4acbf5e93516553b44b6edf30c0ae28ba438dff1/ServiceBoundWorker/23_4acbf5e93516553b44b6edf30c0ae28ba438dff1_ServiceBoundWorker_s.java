 package juitar.worker.queue;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * @author sha1n
  * Date: 1/19/13
  */
 class ServiceBoundWorker implements Runnable {
 
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
             try {
                Work work = queue.take();
 
                 Result result = worker.doWork(work);
 
                 work.getResultChannel().onSuccess(result);
             } catch (InterruptedException e) {
                System.out.println("Worker thread interrupted.");
                 Thread.currentThread().interrupt(); // Reset interrupted state.
             } catch (Throwable e) {
                e.printStackTrace();
             }
         }
     }
 }
