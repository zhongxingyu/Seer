 /**
  *   Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.git;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>Manages delayed initialization of a common {@link GitService} reference, as well as switching of repositories.</p>
  * 
  * @author John McEntire
  *
  */
 public class DelayedGitServiceInitializer implements Closeable {
   private final Logger logger = LoggerFactory.getLogger(getClass());
   
   /**
    * The class member variable to hold the common reference.
    */
   protected GitService git;
   
   /**
    * Enforces the delayed initialization of the common {@link GitService} by causing Threads to wait for the GitService to be initialized.
    */
   private CountDownLatch latch;
 
   /**
    * The Object that manages all read/write locks used by an instance of this class.
    */
   private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();
   
   private final ReadLock readLock = locker.readLock();
   private final WriteLock writeLock = locker.writeLock();
   
   /**
    * Basic constructor that creates a CountDownLatch with a count of 1.
    */
   public DelayedGitServiceInitializer() {
     latch = new CountDownLatch(1);
   }
   
   /**
    * <p>Retrieves the common reference to a GitService.</p>
    * <p>If the reference has not been set yet, any calling threads will block until the reference is set or the thread is interrupted.</p>
    * 
    * @return The common reference to {@link GitService}.
    * @throws Exception Thrown if the current thread has been interrupted while waiting for the GitService to initialize.
    */
   public GitService getGitService() throws Exception {
     logger.debug("Getting git service.");
     latch.await();
     readLock.lock();
     return git;
   }
   
   /**
    * <p>Switches the current reference to point to a new remote git repository.</p>
    * <p>This method will block if the common reference has not been set or there are any threads currently using the referenced git service.</p>
    * 
    * @param uri The uri to the remote repository.
    * @throws Exception
    */
   public synchronized void switchRepository(String uri) throws Exception {
    String branch = null;
    String revision = null;
    String oldRepoUri = null;
     File gitDir = null;
     try {
       writeLock.lock();
       latch.await();
       if(!git.getRemoteRepository().equalsIgnoreCase(uri)) {
         logger.debug("Switching repo from {} to {}", git.getRemoteRepository(), uri);
        branch = git.getBranchName();
        revision = git.getCurrentRevision();
        oldRepoUri = git.getRemoteRepository();
         gitDir = new File(git.getBaseDirectory());
         IOUtils.closeQuietly(git);
         git = null;
         FileUtils.deleteDirectory(gitDir);
         git = GitService.cloneRepo(uri, gitDir.getAbsolutePath());
       }
     } catch(InterruptedException ie){
       logger.warn("Thread interrupted, must be shutting down!", ie);
       throw ie;
     } catch(Exception e) {
       logger.warn("Failed to switch repository. Rolling back!");
       if(git != null) {
         IOUtils.closeQuietly(git);
         git = null;
       }
       FileUtils.deleteQuietly(gitDir);
      git = GitService.cloneRepo(oldRepoUri, gitDir.getAbsolutePath());
      git.switchBranch(branch);
      git.resetToRev(revision);
       throw e;
     } finally {
       writeLock.unlock();
     }
   }
   
   /**
    * Releases the read lock on this class and must be called from the same thread that called the {@link DelayedGitServiceInitializer.getGitService()}.
    */
   public void releaseGitService() {
     readLock.unlock();
   }
   
   /**
    * Sets the common reference an releases any threads waiting for the reference to be set.
    * 
    * @param git 
    */
   public void setGitService(GitService git) {
     if(git != null) {
       logger.debug("Setting git service");
       this.git = git;
       latch.countDown();
     }
   }
 
   /**
    * Releases any used resources and waiting threads.
    * 
    * @throws IOException
    */
   @Override
   public void close() throws IOException {
     if(git != null) {
       IOUtils.closeQuietly(git);
       git = null;
     }
     try {
       latch.notifyAll();
     } catch(Exception e) {
       logger.debug("Failed to notifyAll", e);
     }
     try {
       locker.notifyAll();
     } catch(Exception e) {
       logger.debug("Failed to notifyAll", e);
     }
   }
   
 }
