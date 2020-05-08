 /**
  * Copyright 2012 University Corporation for Atmospheric Research.  All rights
  * reserved.  See file LICENSE.txt in the top-level directory for licensing
  * information.
  */
 package edu.ucar.unidata.sruth;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.nio.file.InvalidPathException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.Future;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.slf4j.Logger;
 
 import edu.ucar.unidata.sruth.Archive.DistributedTrackerFiles;
 
 /**
  * A subscriber of data. A subscriber has a sink-node and a processor of
  * received data.
  * <p>
  * Instances are thread-safe.
  * 
  * @author Steven R. Emmerson
  */
 @ThreadSafe
 public final class Subscriber implements Callable<Void> {
     /**
      * The logger for this class.
      */
     private static final Logger logger    = Util.getLogger();
     /**
      * The sink-node.
      */
     private final SinkNode      sinkNode;
     /**
      * The data-selection predicate.
      */
     private final Predicate     predicate;
     /**
      * Whether or not this instance is running.
      */
     private final AtomicBoolean isRunning = new AtomicBoolean(false);
     /**
      * The archive.
      */
     private final Archive       archive;
     /**
      * The processor of data-products.
      */
     private final Processor     processor;
 
     /**
      * Constructs from the pathname of the archive, the Internet address of the
      * tracker, the predicate for the desired data, and the processor of
      * received data.
      * 
      * @param rootDir
      *            Pathname of the root of the file-tree.
      * @param trackerAddress
      *            The address of the tracker.
      * @param predicate
      *            The predicate for selecting the desired data.
      * @param processor
      *            The processor of received data-products.
      * @throws IOException
      *             if an unused port in the given range couldn't be found.
      * @throws IOException
      *             if an I/O error occurs.
      * @throws NullPointerException
      *             if {@code rootDir == null || trackerAddress == null ||
      *             predicate == null || processor == null}.
      * @throws SocketException
      *             if a server-side socket couldn't be created.
      */
     public Subscriber(final Path rootDir,
             final InetSocketAddress trackerAddress, final Predicate predicate,
             final Processor processor) throws IOException {
         this(rootDir, trackerAddress, predicate, processor, 0);
     }
 
     /**
      * Constructs from the pathname of the archive, the Internet address of the
      * tracker, the predicate for the desired data, the processor of received
      * data, and the port number for the local data-exchange server.
      * 
      * @param rootDir
      *            Pathname of the root of the file-tree.
      * @param trackerAddress
      *            The address of the tracker.
      * @param predicate
      *            The predicate for selecting the desired data.
      * @param processor
      *            The processor of received data-products.
      * @param serverPort
      *            The port number on which the local data-exchange server will
      *            listen for connections. If zero, then an ephemeral port will
      *            be chosen by the operating-system.
      * @throws IOException
      *             if an unused port in the given range couldn't be found.
      * @throws IOException
      *             if an I/O error occurs.
      * @throws NullPointerException
      *             if {@code rootDir == null || trackerAddress == null ||
      *             predicate == null || processor == null}.
      * @throws SocketException
      *             if a server-side socket couldn't be created.
      */
     public Subscriber(final Path rootDir,
             final InetSocketAddress trackerAddress, Predicate predicate,
             final Processor processor, final int serverPort) throws IOException {
         if (null == rootDir) {
             throw new NullPointerException();
         }
         if (null == trackerAddress) {
             throw new NullPointerException();
         }
         if (null == predicate) {
             throw new NullPointerException();
         }
         if (null == processor) {
             throw new NullPointerException();
         }
         archive = new Archive(rootDir);
         /*
          * Ensure reception of the distributed tracker files.
          */
         final DistributedTrackerFiles distributedTrackerFiles = new DistributedTrackerFiles(
                 archive, trackerAddress);
         final Filter filterServerMapFilter = distributedTrackerFiles
                 .getFilter();
         predicate = predicate.add(filterServerMapFilter);
 
         archive.addDataProductListener(new DataProductListener() {
             @Override
             public void process(final DataProduct dataProduct)
                     throws InterruptedException {
                 processor.put(dataProduct);
             }
         });
         sinkNode = new SinkNode(archive, predicate, trackerAddress, serverPort);
         this.predicate = predicate;
        this.processor = processor;
     }
 
     /**
      * Returns the predicate used by this instance.
      * 
      * @return The predicate used by this instance.
      */
     Predicate getPredicate() {
         return predicate;
     }
 
     /**
      * Returns the pathname of the root of the archive.
      * 
      * @return The pathname of the root of the archive.
      */
     Path getRootDir() {
         return archive.getRootDir();
     }
 
     /**
      * Executes this instance. Returns normally if and only if all desired data
      * has been received.
      * 
      * @throws AssertionError
      *             if the impossible happens.
      * @throws IllegalStateException
      *             if this method has been called before.
      * @throws InterruptedException
      *             if the current thread is interrupted.
      * @throws IOException
      *             if non-networking I/O error occurs
      */
     public Void call() throws InterruptedException, IOException {
         logger.trace("Starting up: {}", this);
         if (!isRunning.compareAndSet(false, true)) {
             throw new IllegalStateException();
         }
         final String origThreadName = Thread.currentThread().getName();
         Thread.currentThread().setName(toString());
         final CancellingExecutor executor = new CancellingExecutor(2, 2, 0,
                 TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
         try {
             /*
              * A {@link CompletionService} is used so that both the sink-node
              * task and the data-processing task can be waited on
              * simultaneously.
              */
             final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(
                     executor);
             /*
              * Start the data-processing task. TODO: Use one processing thread
              * per disk controller.
              */
             final Future<Void> processingFuture = completionService
                     .submit(processor);
             /*
              * Start the sink-node task.
              */
             final Future<Void> sinkNodeFuture = completionService
                     .submit(sinkNode);
             /*
              * Wait for one of the tasks to complete.
              */
             for (int i = 0; i < 2; i++) {
                 final Future<Void> future = completionService.take();
                 if (future.isCancelled()) {
                     break;
                 }
                 if (future == processingFuture) {
                     /*
                      * The local-processing task completed -- ideally because it
                      * was cancelled
                      */
                     try {
                         future.get();
                         throw new AssertionError();
                     }
                     catch (final ExecutionException e) {
                         throw new RuntimeException("Unexpected error: "
                                 + processor, e.getCause());
                     }
                 }
                 else {
                     assert future == sinkNodeFuture;
                     /*
                      * The sink-node task completed -- ideally because all the
                      * data was received
                      */
                     try {
                         future.get();
                         // All desired data was received
                         processingFuture.cancel(true);
                     }
                     catch (final ExecutionException e) {
                         final Throwable cause = e.getCause();
                         logger.trace("Execution exception: {}",
                                 cause.toString());
                         if (cause instanceof IOException) {
                             throw new IOException("IO error: " + sinkNode,
                                     cause);
                         }
                         throw new RuntimeException("Unexpected error: "
                                 + sinkNode, cause);
                     }
                 }
             }
         }
         finally {
             executor.shutdownNow();
             Thread.interrupted();
             executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
             try {
                 archive.close();
             }
             catch (final IOException ignored) {
             }
             Thread.currentThread().setName(origThreadName);
             logger.trace("Done: {}", this);
         }
         return null;
     }
 
     /**
      * Waits until this instance is running.
      * <p>
      * This method is potentially slow.
      * 
      * @throws InterruptedException
      *             if the current thread is interrupted
      */
     public void waitUntilRunning() throws InterruptedException {
         processor.waitUntilRunning();
         sinkNode.waitUntilRunning();
     }
 
     /**
      * Returns the number of received files since {@link #call()} was called.
      * 
      * @return The number of received files since {@link #call()} was called.
      */
     public long getReceivedFileCount() {
         return sinkNode.getReceivedFileCount();
     }
 
     /**
      * Returns the current number of peers to which this instance is connected.
      * 
      * @return The current number of peers to which this instance is connected.
      */
     public int getPeerCount() {
         return sinkNode.getClientCount() + sinkNode.getServletCount();
     }
 
     /**
      * Returns the absolute pathname corresponding to a pathname in the archive.
      * 
      * @param archivePath
      *            The pathname in the archive.
      * @return the absolute pathname corresponding to the archive pathname.
      */
     Path getAbsolutePath(final ArchivePath archivePath) {
         return archive.resolve(archivePath);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return "Subscriber [sinkNode=" + sinkNode + "]";
     }
 
     /**
      * Executes an instance of this class.
      * <p>
      * Usage:
      * 
      * <pre>
      * edu.ucar.unidata.sruth.Subscriber [options] subscription
      *     
      * where:
      *   -a actions     URL or pathname of the XML document specifying local
      *                  processing actions. The default is to do no local
      *                  processing of received data-products: the instance
      *                  becomes a pure relay node in the network.
      *   -d archive     Pathname of the root of the temporary data archive.
      *                  The default is the {@code SRUTH} subdirectory of the
      *                  user's home-directory.
      *   -s port        Port number on which the local data-exchange server
      *                  will listen for connections. If zero, then an ephemeral
      *                  port will be chosen by the operating-system (which is
      *                  the default).
      *   subscription   URL or pathname of the XML document that contains 
      *                  the subscription information.
      * </pre>
      * <p>
      * Exit status:
      * 
      * <pre>
      *   0  Success: all subscribed-to data was received and processed.
      *   1  Invalid invocation
      * </pre>
      * 
      * @param args
      *            Program arguments.
      * @throws IOException
      *             if an I/O error occurs
      * @throws SecurityException
      *             if a security exception occurs
      */
     public static void main(final String[] args) throws SecurityException,
             IOException {
         final int INVALID_INVOCATION = 1;
         Path archivePath = Paths.get(System.getProperty("user.home")
                 + File.separatorChar + Util.PACKAGE_NAME);
        Processor processor = new Processor(); // does nothing
         Subscription subscription = null;
         int serverPort = 0;
 
         try {
             int iarg;
             String arg;
             /*
              * Process the optional arguments.
              */
             for (iarg = 0; iarg < args.length; ++iarg) {
                 arg = args[iarg];
                 try {
                     if (arg.charAt(0) != '-') {
                         break;
                     }
                     final String optString = arg.substring(1);
                     arg = args[++iarg];
                     if (optString.equals("a")) {
                         /*
                          * Process the actions argument.
                          */
                         try {
                             processor = Util.decodeUrlOrFile(arg,
                                     new Decoder<Processor>() {
                                         @Override
                                         public Processor decode(
                                                 final InputStream input)
                                                 throws IOException {
                                             return XmlActionFile
                                                     .getProcessor(input);
                                         }
                                     });
                         }
                         catch (final Exception e) {
                             logger.error(
                                     "Couldn't process local-actions argument: \"{}\": {}",
                                     arg, e.toString());
                             System.exit(INVALID_INVOCATION);
                         }
                     }
                     else if (optString.equals("d")) {
                         /*
                          * Process the archive argument.
                          */
                         try {
                             archivePath = Paths.get(arg);
                         }
                         catch (final InvalidPathException e) {
                             logger.error(
                                     "Couldn't process archive argument: \"{}\"",
                                     arg);
                             throw new IllegalArgumentException();
                         }
                     }
                     else if (optString.equals("s")) {
                         /*
                          * Decode the server-port argument.
                          */
                         try {
                             serverPort = Integer.valueOf(arg);
                         }
                         catch (final Exception e) {
                             logger.error(
                                     "Couldn't decode server-port argument: \"{}\": {}",
                                     arg, e.toString());
                             throw new IllegalArgumentException();
                         }
                     }
                     else {
                         logger.error("Invalid option: \"{}\"", optString);
                         throw new IllegalArgumentException();
                     }
                 }
                 catch (final IndexOutOfBoundsException e) {
                     logger.error("Invalid argument: \"{}\"", arg);
                     throw new IllegalArgumentException();
                 }
             }
 
             /*
              * Process the subscription argument.
              */
             if (iarg >= args.length) {
                 logger.error("The subscription argument is missing");
                 throw new IllegalArgumentException();
             }
             arg = args[iarg++];
             try {
                 subscription = Util.decodeUrlOrFile(arg,
                         new Decoder<Subscription>() {
                             @Override
                             public Subscription decode(final InputStream input)
                                     throws IOException {
                                 return new Subscription(input);
                             }
                         });
             }
             catch (final Exception e) {
                 logger.error(
                         "Couldn't process subscription argument: \"{}\": {}",
                         arg, e.toString());
                 throw new IllegalArgumentException();
             }
 
             if (iarg < args.length) {
                 logger.error("Too many arguments");
                 throw new IllegalArgumentException();
             }
         }
         catch (final IllegalArgumentException e) {
             logger.info("Usage: ... [-a actions] [-d archive] [-s port] subscription\n"
                     + "where:\n"
                     + "    -a actions     URL or pathname of the XML document specifying local\n"
                     + "                   processing actions. The default is to do no local\n"
                     + "                   processing of received data-products: the instance\n"
                     + "                   becomes a pure relay node in the network.\n"
                     + "    -d archive     Pathname of the root of the temporary data archive.\n"
                     + "                   The default is the subdirectory \"SRUTH\" of the\n"
                     + "                   user's home-directory.\n"
                     + "    -s port        Port number on which the local data-exchange server\n"
                     + "                   will listen for connections. If zero, then an ephemeral\n"
                     + "                   port will be chosen by the operating-system (which is\n"
                     + "                   the default).\n"
                     + "    subscription   URL or pathname of the XML document that contains\n"
                     + "                   the subscription information.\n");
             System.exit(INVALID_INVOCATION);
         }
 
         /*
          * Create the subscriber.
          */
         Subscriber subscriber = null;
         subscriber = new Subscriber(archivePath,
                 subscription.getTrackerAddress(), subscription.getPredicate(),
                 processor, serverPort);
 
         /*
          * Execute the subscriber.
          */
         try {
             subscriber.call();
         }
         catch (final InterruptedException ignored) {
         }
 
         System.exit(0);
     }
 }
