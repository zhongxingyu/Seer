 /**
  * Copyright 2011 University Corporation for Atmospheric Research.  All rights
  * reserved.  See file LICENSE.txt in the top-level directory for licensing
  * information.
  */
 package edu.ucar.unidata.sruth;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.nio.channels.SeekableByteChannel;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.slf4j.Logger;
 
 /**
  * A top-level node of a distribution graph. A source-node has a {@link Server}
  * and a {@link ArchiveWatcher} but no {@link ClientManager}-s.
  * <p>
  * Instances are thread-safe.
  * 
  * @author Steven R. Emmerson
  */
 @ThreadSafe
 final class SourceNode extends AbstractNode {
     /**
      * Causes the local server to be notified of newly-created files in the
      * archive.
      * <p>
      * Instances are thread-safe.
      * 
      * @author Steven R. Emmerson
      */
     private final class ArchiveWatcher implements Callable<Void> {
         private final CountDownLatch isRunningLatch = new CountDownLatch(1);
 
         @Override
         public Void call() throws InterruptedException, IOException {
             logger.trace("Starting up: {}", toString());
             final String origThreadName = Thread.currentThread().getName();
             Thread.currentThread().setName(toString());
             try {
                 isRunningLatch.countDown();
                 getArchive().watchArchive(localServer);
                 return null;
             }
             finally {
                 Thread.currentThread().setName(origThreadName);
                 logger.trace("Done: {}", toString());
             }
         }
 
         /**
          * Waits until this instance is running.
          * 
          * @throws InterruptedException
          *             if the current thread is interrupted.
          */
         void waitUntilRunning() throws InterruptedException {
             isRunningLatch.await();
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             return "ArchiveWatcher [rootDir=" + clearingHouse.getRootDir()
                     + "]";
         }
     }
 
     /**
      * The logger for the package
      */
     private static final Logger           logger            = Util.getLogger();
     /**
      * The {@link ExecutorService} for the localServer and file-watcher tasks.
      */
     private final CancellingExecutor      executor          = new CancellingExecutor(
                                                                     2,
                                                                     2,
                                                                     0,
                                                                     TimeUnit.SECONDS,
                                                                     new SynchronousQueue<Runnable>());
     /**
      * The task completion service.
      */
     private final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(
                                                                     executor);
     /**
      * The watcher for new files.
      */
     private final ArchiveWatcher          archiveWatcher    = new ArchiveWatcher();
 
     /**
      * Constructs from the data archive and a specification of the
      * locally-desired data. This constructor is equivalent to the constructor
      * {@link #SourceNode(Archive, InetSocketAddressSet) SourceNode(archive, new
      * InetSocketAddressSet())}.
      * 
      * @param archive
      *            The data archive.
      * @throws IOException
      *             if the localServer can't connect to the network.
      * @throws NullPointerException
      *             if {@code rootDir == null || predicate == null}.
      * @see #SourceNode(Archive, Predicate, PortNumberSet)
      */
     SourceNode(final Archive archive) throws IOException {
         this(archive, new InetSocketAddressSet());
     }
 
     /**
      * Constructs from the data archive, a specification of the locally-desired
      * data, and the port numbers for the localServer.
      * 
      * @param archive
      *            The data archive.
      * @param inetSockAddrSet
      *            The set of candidate Internet socket addresses for the server.
      * @throws IOException
      *             if the localServer can't connect to the network.
      * @throws NullPointerException
      *             if {@code archive == null || inetSockAddrSet == null}.
      * @see AbstractNode#AbstractNode(Archive, Predicate, InetSocketAddressSet)
      */
     SourceNode(final Archive archive, final InetSocketAddressSet inetSockAddrSet)
             throws IOException {
         super(archive, Predicate.NOTHING, inetSockAddrSet);
     }
 
     @Override
     Server createServer(final ClearingHouse clearingHouse,
             final InetSocketAddressSet inetSockAddrSet) throws SocketException,
             IOException {
         return new SourceServer(clearingHouse, inetSockAddrSet);
     }
 
     /**
      * Executes this instance. Never returns normally.
      * 
      * @throws InterruptedException
      *             if the current thread is interrupted.
      * @throws IOException
      *             if a serious I/O error occurs.
      */
     @Override
     public Void call() throws InterruptedException, IOException {
         logger.trace("Starting up: {}", this);
         final String origThreadName = Thread.currentThread().getName();
         Thread.currentThread().setName(toString());
         try {
             final Future<Void> serverFuture = completionService
                     .submit(localServer);
             completionService.submit(archiveWatcher);
             final Future<Void> future = completionService.take();
             if (!future.isCancelled()) {
                 final Object task = future == serverFuture
                         ? localServer
                         : archiveWatcher;
                 try {
                     future.get();
                 }
                 catch (final ExecutionException e) {
                     final Throwable cause = e.getCause();
                     if (cause instanceof InterruptedException) {
                        logger.trace("Interrupted: {}", task);
                         throw (InterruptedException) cause;
                     }
                     if (cause instanceof IOException) {
                         throw new IOException("IO Error: " + task, cause);
                     }
                     throw new RuntimeException("Unexpected error: " + task,
                             cause);
                 }
             }
         }
         finally {
             executor.shutdownNow();
             awaitCompletion();
             Thread.currentThread().setName(origThreadName);
             logger.trace("Done: {}", this);
         }
         return null;
     }
 
     /**
      * Waits until this instance is running.
      * 
      * @throws InterruptedException
      *             if the current thread is interrupted.
      */
     void waitUntilRunning() throws InterruptedException {
         localServer.waitUntilRunning();
         archiveWatcher.waitUntilRunning();
     }
 
     /**
      * Waits until this instance has completed.
      */
     void awaitCompletion() throws InterruptedException {
         Thread.interrupted();
         executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
     }
 
     /**
      * Returns a byte channel to a file that will be published.
      * 
      * @param path
      *            Pathname of the file relative to the root of the file-tree.
      * @return A byte channel to the file.
      * @throws IOException
      *             if an I/O error occurs.
      */
     SeekableByteChannel newBytechannel(Path path) throws IOException {
         path = getArchive().getHiddenPath(path);
         Files.createDirectories(path.getParent());
         final SeekableByteChannel channel = Files.newByteChannel(path,
                 StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
         return channel;
     }
 
     /**
      * Returns a handle on a new, unpublished file that's ready for content.
      * 
      * @param path
      *            The pathname of the file relative to the root of the
      *            file-tree.
      * @return A handle on the new file.
      * @throws IOException
      *             if an I/O error occurs.
      * @throws IllegalArgumentException
      *             if {@code path.isAbsolute()}.
      */
     PubFile newPubFile(final Path path) throws IOException {
         return new PubFile(getArchive(), path);
     }
 
     @Override
     int getClientCount() {
         return 0;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return "SourceNode [archive=" + getArchive() + "]";
     }
 }
