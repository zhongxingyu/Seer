 /*
  * Copyright (c) 2010 Erwin van Eijk <erwin.vaneijk@gmail.com>. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of <copyright holder>.
  */
 
 package nl.minjus.nfi.dt.jhashtools.hashers;
 
 import nl.minjus.nfi.dt.jhashtools.DirHasherResult;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.concurrent.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Use multithreading to compute the digests on all the files.
  * @author Erwin van Eijk
  */
 class ConcurrentDirectoryHasher extends AbstractDirectoryHasher
 {
 
     private static final Logger LOG = Logger.getLogger(ConcurrentDirectoryHasher.class.getName());
     private ExecutorService executorService;
     private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
 
     static enum ProcessingStates
     {
 
         UNINITIALIZED,
         BUSY,
         FINISHED
     }
 
     public ConcurrentDirectoryHasher(ExecutorService anExecutorService)
     {
         super();
         this.executorService = anExecutorService;
     }
 
     public ConcurrentDirectoryHasher(ExecutorService anExecutorService, String algorithm)
             throws NoSuchAlgorithmException
     {
         super(algorithm);
         this.executorService = anExecutorService;
     }
 
     public ConcurrentDirectoryHasher(ExecutorService anExecutorService, Collection<String> algorithms)
             throws NoSuchAlgorithmException
     {
         super(algorithms);
         this.executorService = anExecutorService;
     }
 
     public DirHasherResult getDigests(File startPath)
     {
         DirHasherResult result = new DirHasherResult();
         this.updateDigests(result, startPath);
         return result;
     }
 
     @Override
     public void updateDigests(DirHasherResult digests, File startPath)
     {
         if (!startPath.exists()) {
             throw new IllegalArgumentException("Path " + startPath + " does not exist");
         }
 
        Collection<Thread> threads = new LinkedList<Thread>();
        BlockingQueue<File> queue = new ArrayBlockingQueue<File>(16);
         ProcessingState currentState = new ProcessingState();
 
         CompletionService<DirHasherResult> completionService =
                 new ExecutorCompletionService<DirHasherResult>(this.executorService);
         // Create the task that just walks the tree and puts the File entries in the queue
         Future<DirHasherResult> fileWalkerTask =
                 completionService.submit(new FileWalkerTask(startPath, queue, currentState));
 
         // Start the compute tasks that compute the digests on the data in the File's that are read from
         // the queue.
         Collection<Future<DirHasherResult>> computeTasks = new LinkedList<Future<DirHasherResult>>();
         for (int i = 0; i < MAX_THREADS; i++) {
             try {
                 FileHasher fileHasher = new FileHasherCreator().create(this.executorService, this.algorithms);
                 computeTasks.add(completionService.submit(new FileDigestComputeTask(queue, fileHasher, currentState)));
             } catch (NoSuchAlgorithmException e) {
                 LOG.log(Level.SEVERE, "A cryptoalgorithm is not found. This is bad.", e);
             }
         }
 
         try {
             // first, wait for the fileWalkerTask to finish.
             if (fileWalkerTask.get() != null) {
                 LOG.log(Level.SEVERE, "It should be impossible to get a result here.");
             }
 
             for (Future<DirHasherResult> task : computeTasks) {
                 DirHasherResult partial = task.get();
                 if (partial != null) {
                     digests.putAll(partial);
                 }
             }
         } catch (InterruptedException ex) {
             LOG.log(Level.WARNING, "Interruption has occurred.", ex);
         } catch (ExecutionException ex) {
             LOG.log(Level.WARNING, "Execution was interrupted.", ex);
         }
     }
 
     static class ProcessingState
     {
 
         public ProcessingState()
         {
             this.lock = new ReentrantLock();
             this.currentState = ProcessingStates.UNINITIALIZED;
         }
 
         public Lock lock;
         public ProcessingStates currentState;
     }
 
     static class FileWalkerTask implements Callable<DirHasherResult>
     {
 
         private File startingPath;
         private DirVisitorTask visitor;
         private ProcessingState processingState;
 
         public FileWalkerTask(final File startingPath, final BlockingQueue<File> inputQueue, final ProcessingState processingState)
         {
             this.startingPath = startingPath;
             this.visitor = new DirVisitorTask(inputQueue);
             this.processingState = processingState;
         }
 
         /**
          * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
          * causes the object's <code>run</code> method to be called in that separately executing thread.
          * <p/>
          * The general contract of the method <code>run</code> is that it may take any action whatsoever.
          *
          * @return the result of the computation.
          *
          * @see Callable#call()
          */
         public DirHasherResult call()
         {
             try {
                 this.processingState.lock.lock();
                 this.processingState.currentState = ProcessingStates.BUSY;
             } finally {
                 this.processingState.lock.unlock();
             }
             FileWalker walker = new FileWalker();
             walker.addWalkerVisitor(visitor);
             walker.walk(this.startingPath);
             try {
                 this.processingState.lock.lock();
                 this.processingState.currentState = ProcessingStates.FINISHED;
             } finally {
                 this.processingState.lock.unlock();
             }
 
             return null;
         }
     }
 
     static class FileDigestComputeTask implements Callable<DirHasherResult>
     {
         private BlockingQueue<File> inputQueue;
         private FileHasher fileHasher;
         private DirHasherResult partialResult;
         private ProcessingState processingState;
 
         public FileDigestComputeTask(final BlockingQueue<File> inputQueue, final FileHasher fileHasher, final ProcessingState processingState) throws NoSuchAlgorithmException
         {
             this.inputQueue = inputQueue;
             this.fileHasher = fileHasher;
             this.partialResult = new DirHasherResult();
             this.processingState = processingState;
         }
 
         /**
          * Computes a result, or throws an exception if unable to do so.
          *
          * @return computed result
          *
          * @throws Exception if unable to compute a result
          */
         public DirHasherResult call() throws Exception
         {
             try {
                 while (true) {
                     try {
                         File file = this.inputQueue.poll(10, TimeUnit.MICROSECONDS);
                         if (file != null) {
                             this.partialResult.put(file, this.fileHasher.getDigest(file));
                         } else if (this.processingState.currentState == ProcessingStates.FINISHED) {
                             break;
                         }
                     } catch (IOException ex) {
                         // pass
                     }
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             return this.partialResult;
         }
     }
 }
