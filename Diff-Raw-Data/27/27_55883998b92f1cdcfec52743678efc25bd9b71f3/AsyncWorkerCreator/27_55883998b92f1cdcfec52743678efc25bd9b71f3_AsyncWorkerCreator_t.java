 /**
  * AsyncWorkerCreator
  *
 * Performs all operations to create, start and stop threaded workers.
  *
  * @author Ariel Gerardo Rios <mailto:ariel.gerardo.rios@gmail.com>
  *
  */
 
 package org.gabrielle.AsyncProcessingExample.command;
 
 import java.util.Properties;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import org.gabrielle.AsyncProcessingExample.worker.Worker;
 
 /** 
 * Performs all operations to create, start and stop threaded workers. This
 * class is in charge of retain references to all created threads and workers
 * for future calls as join or stop. Also it must have methods implementing the
 * logic to apply when a signal interrumption arrives.
  *
  * @author Ariel Gerardo Ríos <mailto:ariel.gerardo.rios@gmail.com>
 */
 public class AsyncWorkerCreator {
 
     private static final Logger logger = Logger.getLogger(
             "async-worker-creator");
 
     private Worker[] workers;
 
     private Thread[] threads;
 
 
     /** 
      * Create all needed workers, indicated by total workers (1 worker per
      * thread).
      *
     */
     public void createWorkers(final UUID uuid, final int nthreads,
             final Properties config) {
         this.workers = new Worker[nthreads];
         this.threads = new Thread[nthreads];
             
         Worker w;
         for(int i = 0; i < nthreads; i++) {
             w = new Worker(i, config);
             // If the worker must have additional resourses, this is the moment
             // to give it to them.
 
             this.workers[i] = w;
             this.threads[i] = new Thread(w, w.getName());
         }
     }
 
 
     /** 
      * Calls all workers to start execution.
      *
     */
     public void startWorkers(final UUID uuid){
 
         int nthreads = this.threads.length;
 
         logger.info(String.format("UUID=%s - Starting %d workers ...", uuid,
                     nthreads));
         for (Thread t: this.threads) {
             t.start();
         }
         logger.info(String.format("UUID=%s - Starting workers %d: SUCCESSFUL.",
                     uuid, nthreads));
     }
     
     /** 
      * Calls all workers to stop exectution.
      *
     */
     public void stopWorkers(final UUID uuid) {
         int nworkers = this.workers.length;
         logger.info(String.format("UUID=%s - Stoping %d workers ...",
                     uuid, nworkers));
         for (Worker w: this.workers) {
             w.stopExecution();
         }
         logger.info(String.format("UUID=%s - Stoping %d workers: SUCCESSFUL.",
                     uuid, nworkers));
     }
                                 
     /** 
      * Joins all workers til they stop.
      *
      * @throws InterruptedException
     */
     public void joinWorkers(final UUID uuid) throws InterruptedException {
         int nthreads = this.threads.length;
         logger.info(String.format("UUID=%s - Joining %d workers ...", uuid,
                     nthreads));
         for (Thread t: this.threads) {
             t.join();
         }
         logger.info(String.format("UUID=%s - Joining %d workers: SUCCESSFUL.",
                     uuid, nthreads));
     }
 
     // TODO Signal handling :O
 }
 
 
 // vim:ft=java:
