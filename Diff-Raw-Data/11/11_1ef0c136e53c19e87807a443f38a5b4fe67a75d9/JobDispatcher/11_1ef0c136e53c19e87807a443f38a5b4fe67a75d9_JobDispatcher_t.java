 /*
  * Copyright (c) Fabien Hermenier
  *
  *        This file is part of Entropy.
  *
  *        Entropy is free software: you can redistribute it and/or modify
  *        it under the terms of the GNU Lesser General Public License as published by
  *        the Free Software Foundation, either version 3 of the License, or
  *        (at your option) any later version.
  *
  *        Entropy is distributed in the hope that it will be useful,
  *        but WITHOUT ANY WARRANTY; without even the implied warranty of
  *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *        GNU Lesser General Public License for more details.
  *
  *        You should have received a copy of the GNU Lesser General Public License
  *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package entropy.jobsManager;
 
 import gnu.trove.TIntArrayList;
 import gnu.trove.TIntObjectHashMap;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A standalone service to dispatch a serie of Jobs to several job handler.
  * Communication between the dispatcher and the handlers is made through an HTTP server.
  * The dispatcher run in a separated thread.
  *
  * @author Fabien Hermenier
  * @see JobHandler
  * @see Job
  */
 public class JobDispatcher {
 
     public static final int DEFAULT_PORT = 6758;
 
     /**
      * The ID of the next job.
      */
     private int nexId = 0;
 
     /**
      * The HTTP server.
      */
     private Server server;
 
     /**
      * The list of waiting jobs. Ie, jobs are are not handled.
      */
     private final TIntArrayList waiting;
 
     /**
      * The list of jobs that are currently computed on a job handler.
      */
     private final TIntArrayList running;
 
     /**
      * The list of jobs that was commited by their handler.
      */
     private final TIntArrayList commited;
 
     private TIntObjectHashMap<Job> jobs;
 
     /**
      * The server-side handler that intercept commited job.
      */
     private CommitedJobHandler commitedHandler;
 
     /**
      * The handler to manager the HTTP requests.
      */
     private RequestHandler requestHandler;
 
     /**
      * The logger of the service.
      */
     private static Logger logger = LoggerFactory.getLogger("JobDispatcher");
 
     private HandlerList handlers;
 
     /**
      * Make a new job dispatcher with a custom commited job handler.
      *
      * @param p the listening port
      * @param h the handler to manage commited jobs.
      */
     public JobDispatcher(int p, String rcBase, CommitedJobHandler h) {
         this.commitedHandler = h;
         this.waiting = new TIntArrayList();
         this.running = new TIntArrayList();
         this.commited = new TIntArrayList();
         this.jobs = new TIntObjectHashMap<Job>();
 
         ResourceHandler rcHandler = new ResourceHandler();
         rcHandler.setResourceBase(rcBase);
         rcHandler.getMimeTypes().addMimeMapping("pbd", "application/x-protobuf");
 
 
        this.server = new Server(p);
         this.server.setStopAtShutdown(true);
         this.server.setGracefulShutdown(5000);
 
         this.requestHandler = new RequestHandler(this);
         handlers = new HandlerList();
         handlers.setHandlers(new Handler[]{requestHandler, rcHandler});
         server.setHandler(handlers);
 
         DefaultHTMLReporter reporter = new DefaultHTMLReporter(this);
         requestHandler.setStatusReporter(reporter);
     }
 
     /**
      * Start the server and wait for its termination.
      */
     public void run() {
         try {
             this.server.start();
             logger.info("JobDispatcher started");
             this.server.join();
             logger.info("JobDispatcher ended");
         } catch (Exception e) {
             logger.info(e.getMessage());
            //System.err.println(e.getMessage());
         }
     }
 
     /**
      * Return a copy of the jobs that are waiting for computation.
      *
      * @return a list of jobs, may be empty
      */
     public TIntArrayList getWaitings() {
         TIntArrayList clone = null;
         synchronized (waiting) {
             clone = (TIntArrayList) waiting.clone();
         }
         return clone;
     }
 
     /**
      * Return a copy of the jobs that are currently computed by an handler
      *
      * @return a list of jobs, may be empty
      */
     public TIntArrayList getRunnings() {
         TIntArrayList clone = null;
         synchronized (running) {
             clone = (TIntArrayList) running.clone();
         }
         return clone;
     }
 
     /**
      * Return a copy of the jobs that are commited
      *
      * @return a list of jobs, may be empty
      */
     public TIntArrayList getComitted() {
         TIntArrayList clone = null;
         synchronized (commited) {
             clone = (TIntArrayList) commited.clone();
         }
         return clone;
     }
 
     /**
      * Get a job by its id
      *
      * @param id the identifier of the job
      * @return a job or null if the id does not fit with a job.
      */
     public Job getJob(int id) {
         return jobs.get(id);
     }
 
     /**
      * Get the handler that manage commited jobs
      *
      * @return the commited job handler given during instantiation
      */
     public CommitedJobHandler getCommitedJobHandler() {
         return commitedHandler;
     }
 
     /**
      * Dequeue a waiting job.
      * The job is assigned to a specific handler, set into the running state and put
      * into the running queue.
      * The method is thread-safe
      *
      * @return the dequeued job
      */
     public Job dequeue() {
         Job j = null;
         synchronized (this.waiting) {
             if (!waiting.isEmpty()) {
                 int id = waiting.remove(0);
                 j = jobs.get(id);
                 if (j != null) {
                     synchronized (this.running) {
                         running.add(id);
                         j.setDequeuedTime(System.currentTimeMillis());
                     }
                     logger.info("Job " + id + " dequeued");
                 }
             }
         }
         return j;
     }
 
     /**
      * Commit a running job
      * The job is put into the commited queue and set to the completed state.
      * The method is thread-safe
      *
      * @param j2 the job
      */
     public void commit(Job j2) {
         synchronized (this.running) {
             Job j = jobs.get(j2.getId());
             int id = j2.getId();
             for (String k : j2.getKeys()) {
                 j.put(k, j2.get(k));
             }
             running.remove(running.indexOf(id));
             synchronized (this.commited) {
                 this.commited.add(id);
                 j.setCommitedTime(System.currentTimeMillis());
                 commitedHandler.jobCommited(j);
             }
         }
     }
 
     /**
      * Enqueue a new job.
      * A new job is created with a unique identifier. It will be composed
      * by the different values given in parameters. The job is then put into
      * the waiting queue. The method is thread-safe
      *
      * @param j the job to enqueue
      * @return the enqueue job
      */
     public synchronized void enqueue(Job j) {
         j.setEnqueuedTime(System.currentTimeMillis());
         this.jobs.put(j.getId(), j);
         synchronized (this.waiting) {
             this.waiting.add(j.getId());
         }
     }
 
     /**
      * Get the handler that manage client HTTP requests.
      *
      * @return a RequestHandler.
      */
     public RequestHandler getRequestHandler() {
         return requestHandler;
     }
 
     /**
      * Set the handler to manipulate the client HTTP requests.
      *
      * @param r the request handler to use
      */
     public void setRequestHandler(RequestHandler r) {
         requestHandler = r;
     }
 
     /**
      * Stop the service.
      */
     public void stopServer() {
         try {
             server.stop();
         } catch (Exception e) {
             logger.error(e.getMessage());
         }
         logger.info("JobDispatcher stopped");
     }
 
     /**
      * Get the logger of the dispatcher.
      *
      * @return a logger
      */
     public static Logger getLogger() {
         return logger;
     }
 }
