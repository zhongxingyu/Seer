 /*
  * Copyright 2013 Christian Robert
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.perdian.apps.downloader.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Objects;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  * An engine represents the central manager object, into which new jobs that are
  * supposed to download information from an external resource will be added and
  * from which information about the current state of the engine and it's jobs
  * can be requested.
  *
  * Client code should only change the state of the download queue and associated
  * processes using the methods provided in this API class.
  *
  * @author Christian Robert
  */
 
 public class DownloadEngine {
 
   private static final Logger log = LogManager.getLogger(DownloadEngine.class);
 
   private ExecutorService myExecutorService = Executors.newCachedThreadPool();
   private List<DownloadListener> myListeners = new CopyOnWriteArrayList<>();
   private Path myTargetDirectory = null;
   private Path myWorkingDirectory = null;
   private Path myMetadataDirectory = null;
   private int myProcessorCount = 1;
   private List<DownloadJob> myWaitingJobs = new CopyOnWriteArrayList<>();
   private List<DownloadJob> myActiveJobs = new CopyOnWriteArrayList<>();
   private boolean stateShutdown = false;
 
   /**
    * The engine is not supposed to be instantiated directly by client code.
    * It should only be created using the {@code buildEngine} method provided by
    * a {@link DownloadEngineBuilder} instance.
    */
   DownloadEngine() {
   }
 
   /**
    * Submits a new download into this engine. It is up to the engine to decide
    * whether or not the job can be started immediately or if it needs to be put
    * in any kind of queue and await a free download slot.
    *
    * @param request
    *   the request containing all the information from which a download can be
    *   constructuted.
    * @return
    *   a {@link DownloadJob} if the engine has accepted the request and
    *   scheduled it for execution or {@code null} if the engine instance
    *   rejected the job and will not execute the transfer process.
    */
   public DownloadJob submit(DownloadRequest request) {
     if(this.isShutdown()) {
       throw new IllegalStateException(DownloadEngine.class.getSimpleName() + " is shutdown!");
     } else if(request == null) {
       throw new IllegalArgumentException("Parameter 'request' must not be null!");
     } else if(request.getTargetFileName() == null) {
       throw new IllegalArgumentException("Property 'targetFileName' of request must not be null!");
     } else if(request.getStreamFactory() == null) {
       throw new IllegalArgumentException("Property 'streamFactory' of request must not be null!");
     } else {
 
       // First we contact all the validators and make sure the new request might
       // actually be processed
       if(!this.fireRequestSubmitted(request)) {
         return null;
       }
 
       log.info("Accepted request: {}", request);
       DownloadJob downloadJob = new DownloadJob(this);
       downloadJob.setRequest(request);
       downloadJob.setScheduleTime(System.currentTimeMillis());
       downloadJob.setStatus(DownloadStatus.SCHEDULED);
       synchronized(this) {
         if(!this.startJob(downloadJob, false)) {
           this.getWaitingJobs().add(downloadJob);
           this.fireJobScheduled(downloadJob);
         }
       }
       return downloadJob;
 
     }
   }
 
   /**
    * List all currently active jobs, which means the jobs for which processor
    * thread is currently transfering data from the source to a target.
    *
    * @return
    *   the list of active jobs at the time this method was called. Subsequent
    *   changes in the engines state (a download was completed or cancelled)
    *   will not be reflected into this result list. The list itself will
    *   therefore be immutable
    */
   public List<DownloadJob> listActiveJobs() {
     return Collections.unmodifiableList(new ArrayList<>(this.getActiveJobs()));
   }
 
   /**
    * List all currently waitinbg jobs, which means the jobs for which no
    * processor has been assigned yet. The job itself is therefore sitting in a
    * queue waiting to be picked up and to be executed.
    *
    * @return
    *   the list of waiting jobs at the time this method was called. Subsequent
    *   changes in the engines state (a download was picked up for execution
    *   or was cancelled) will not be reflected into this result list. The list
    *   itself will therefore be immutable
    */
   public List<DownloadJob> listWaitingJobs() {
     return Collections.unmodifiableList(new ArrayList<>(this.getWaitingJobs()));
   }
 
   /**
    * Shutdown the current engine, which means execute all remaining jobs that
    * are currently active but do not accept any further jobs to be added to this
    * engine.
    *
    * @return
    *   the list of currently waiting jobs which are not going to be executed
    *   any more
    */
   public synchronized List<DownloadJob> shutdown() {
     if(!this.isShutdown()) {
       this.setShutdown(true);
       this.getExecutorService().shutdown();
     }
     return Collections.unmodifiableList(this.getWaitingJobs());
   }
 
   /**
    * Shutdown the current engine, which means execute all remaining jobs that
    * are currently active but do not accept any further jobs to be added to this
    * engine. Also wait until all remaining jobs have been completed.
    *
    * @return
    *   the list of currently waiting jobs which are not going to be executed
    *   any more
    */
   public List<DownloadJob> shutdownAndWait() {
     if(!this.isShutdown()) {
       this.shutdown();
       try {
         this.getExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
       } catch(InterruptedException e) {
         log.warn("Cannot wait for shutdown of ExecutorService", e);
       }
     }
     return Collections.unmodifiableList(this.getWaitingJobs());
   }
 
   // ---------------------------------------------------------------------------
   // --- Status transitions ----------------------------------------------------
   // ---------------------------------------------------------------------------
 
   synchronized boolean startJob(final DownloadJob job, boolean ignoreSlots) {
     if(!this.isShutdown() && (ignoreSlots || this.getProcessorCount() > this.getActiveJobs().size())) {
 
       // Make sure we remove the job from the waiting list, so no matter from
       // where we come from we always leave in a consistent state
       this.getWaitingJobs().remove(job);
 
       // Now make sure we have the right satus upon the job itself
       job.setStartTime(System.currentTimeMillis());
       job.setStatus(DownloadStatus.ACTIVE);
       DownloadEngine.this.getActiveJobs().add(job);
 
       this.getExecutorService().submit(new Runnable() {
         @Override public void run() {
           DownloadEngine.this.runJob(job);
         }
         @Override public String toString() {
           return DownloadJob.class.getSimpleName() + "-Processor[" + job + "]";
         }
       });
       return true;
     } else {
       return false;
     }
   }
 
   synchronized boolean cancelJob(DownloadJob job) {
     if(this.getWaitingJobs().remove(job)) {
 
       // The job could be found in the list of waiting jobs, so all we need to
       // do is remove him from there and we're done!
       return true;
 
     } else if(!this.getActiveJobs().contains(job)) {
 
       // The job could not be found in the list of active jobs, which means we
       // have no way of handling him at all - so we just exit
       return false;
 
     } else {
 
       job.setStatus(DownloadStatus.CANCELLED);
       job.setCancelTime(System.currentTimeMillis());
       this.fireJobCancelled(job);
       return true;
 
     }
   }
 
   synchronized void checkWaitingJobs() {
     while(!this.isShutdown() && !this.getWaitingJobs().isEmpty()) {
       if(this.getActiveJobs().size() >= this.getProcessorCount()) {
         return;
       } else {
         this.startJob(this.getWaitingJobs().remove(0), false);
       }
     }
   }
 
   // ---------------------------------------------------------------------------
   // --- Job execution ---------------------------------------------------------
   // ---------------------------------------------------------------------------
 
   void runJob(DownloadJob job) {
     try {
       log.info("Running job: {}", job);
       this.fireJobStarted(job);
       job.setResult(this.runJobTransfer(job));
       log.info("Job completed successfully: {}", job);
     } catch(Exception e) {
       log.info("Exception occured during job execution: " + job, e);
       job.setError(e);
     } finally {
       try {
 
         // Update the job itself
         job.setStatus(DownloadStatus.COMPLETED);
         job.setEndTime(System.currentTimeMillis());
         this.fireJobCompleted(job);
 
       } finally {
 
         // Make sure the engine itself remains in a consistent state
         synchronized(this) {
 
           // Make sure the job is removed from the list of currently active jobs
           this.getActiveJobs().remove(job);
 
           // After the current processor is finshed we want to make sure that the
           // next item in the queue get's picked up
           this.checkWaitingJobs();
 
         }
 
       }
     }
   }
 
   Path runJobTransfer(DownloadJob job) throws IOException {
 
     Path targetFilePath = this.getTargetDirectory().resolve(job.getRequest().getTargetFileName());
     Path workingFilePath = this.getWorkingDirectory().resolve(targetFilePath.getFileName());
     if(!Files.exists(workingFilePath.getParent())) {
       Files.createDirectory(workingFilePath.getParent());
     }
     long inStreamSize = job.getRequest().getStreamFactory().size();

     try(InputStream inStream = job.getRequest().getStreamFactory().openStream()) {
       try(OutputStream outStream = Files.newOutputStream(workingFilePath, Files.exists(workingFilePath) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE)) {
         long totalBytesWritten = 0;
         byte[] buffer = new byte[8092];
         for(int bufferSize = inStream.read(buffer); bufferSize > -1; bufferSize = inStream.read(buffer)) {
           outStream.write(buffer, 0, bufferSize);
           totalBytesWritten += bufferSize;
           job.fireProgress(totalBytesWritten, inStreamSize);
         }
         outStream.flush();
      } catch(final Exception e) {
        log.warn("Error occured during file transfer [" + job + "]", e);
        try {
          Files.deleteIfExists(workingFilePath);
        } catch(Exception e2) {
          log.debug("Cannot delete working file at: " + workingFilePath, e2);
        }
        throw e;
       }
     }
 
     if(!Files.exists(targetFilePath.getParent())) {
       Files.createDirectory(targetFilePath.getParent());
     }
     Files.move(workingFilePath, targetFilePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
     return targetFilePath;
 
   }
 
   // ---------------------------------------------------------------------------
   // --- Listener access -------------------------------------------------------
   // ---------------------------------------------------------------------------
 
   public void addListener(DownloadListener listener) {
     log.trace("Adding listener: {}", listener);
     this.getListeners().add(Objects.requireNonNull(listener));
   }
   public boolean removeListener(DownloadListener listener) {
     return this.getListeners().remove(listener);
   }
   List<DownloadListener> getListeners() {
     return this.myListeners;
   }
   void setListeners(List<DownloadListener> listeners) {
     this.myListeners = listeners;
   }
 
   private void fireProcessorCountUpdated(int processorCount) {
     for(DownloadListener listener : this.getListeners()) {
       listener.processorCountUpdated(processorCount);
     }
   }
 
   private boolean fireRequestSubmitted(DownloadRequest request) {
     for(DownloadListener listener : this.getListeners()) {
       try {
         listener.requestSubmitted(request);
       } catch(DownloadRejectedException e) {
         log.info("Request rejected by listener: {} (Listener: {}, Message: {})", request, listener, e.getMessage());
         return false;
       }
     }
     return true;
   }
 
   private void fireJobScheduled(DownloadJob job) {
     for(DownloadListener listener : this.getListeners()) {
       listener.jobScheduled(job);
     }
   }
 
   private void fireJobStarted(DownloadJob job) {
     for(DownloadListener listener : this.getListeners()) {
       listener.jobStarted(job);
     }
   }
 
   private void fireJobCompleted(DownloadJob job) {
     for(DownloadListener listener : this.getListeners()) {
       listener.jobCompleted(job);
     }
   }
 
   private void fireJobCancelled(DownloadJob job) {
     for(DownloadListener listener : this.getListeners()) {
       listener.jobCancelled(job);
     }
   }
 
   // ---------------------------------------------------------------------------
   // --- Public property access methods ----------------------------------------
   // ---------------------------------------------------------------------------
 
   public int getProcessorCount() {
     return this.myProcessorCount;
   }
   public void setProcessorCount(int newProcessorCount) {
     if(newProcessorCount <= 0) {
       throw new IllegalArgumentException("Parameter 'processorCount' must be larger than 0");
     } else if(this.myProcessorCount != newProcessorCount) {
       int oldProcessorCount = this.myProcessorCount;
       log.debug("Updating processor count from {} to {}", oldProcessorCount, newProcessorCount);
       synchronized(this) {
         this.myProcessorCount = newProcessorCount;
         if(newProcessorCount > oldProcessorCount) {
           this.checkWaitingJobs();
         }
       }
       this.fireProcessorCountUpdated(newProcessorCount);
     }
   }
 
   // ---------------------------------------------------------------------------
   // --- Private property access methods ---------------------------------------
   // ---------------------------------------------------------------------------
 
   ExecutorService getExecutorService() {
     return this.myExecutorService;
   }
   void setExecutorService(ExecutorService executorService) {
     this.myExecutorService = executorService;
   }
 
   List<DownloadJob> getWaitingJobs() {
     return this.myWaitingJobs;
   }
   void setWaitingJobs(List<DownloadJob> waitingJobs) {
     this.myWaitingJobs = waitingJobs;
   }
 
   List<DownloadJob> getActiveJobs() {
     return this.myActiveJobs;
   }
   void setActiveJobs(List<DownloadJob> activeJobs) {
     this.myActiveJobs = activeJobs;
   }
 
   Path getTargetDirectory() {
     return this.myTargetDirectory;
   }
   void setTargetDirectory(Path targetDirectory) {
     this.myTargetDirectory = targetDirectory;
   }
 
   Path getWorkingDirectory() {
     return this.myWorkingDirectory;
   }
   void setWorkingDirectory(Path workingDirectory) {
     this.myWorkingDirectory = workingDirectory;
   }
 
   Path getMetadataDirectory() {
     return this.myMetadataDirectory;
   }
   void setMetadataDirectory(Path metadataDirectory) {
     this.myMetadataDirectory = metadataDirectory;
   }
 
   boolean isShutdown() {
     return this.stateShutdown;
   }
   void setShutdown(boolean shutdown) {
     this.stateShutdown = shutdown;
   }
 
 }
