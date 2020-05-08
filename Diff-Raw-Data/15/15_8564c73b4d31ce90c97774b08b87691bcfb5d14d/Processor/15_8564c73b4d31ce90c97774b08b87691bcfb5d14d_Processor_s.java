 /**
  * Copyright 2012 University Corporation for Atmospheric Research.  All rights
  * reserved.  See file LICENSE.txt in the top-level directory for licensing
  * information.
  */
 package edu.ucar.unidata.sruth;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.SynchronousQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.slf4j.Logger;
 
 /**
  * Processes data-products according to client instructions.
  * <p>
  * Instances are thread-safe.
  * 
  * @author Steven R. Emmerson
  */
 @ThreadSafe
 public final class Processor implements Callable<Void> {
     /**
      * The logger for this package
      */
     private static final Logger                        logger          = Util.getLogger();
     /**
      * Map from filters to actions.
      */
     private final ConcurrentMap<Pattern, List<Action>> actions         = new ConcurrentHashMap<Pattern, List<Action>>();
     /**
     * The queue of unprocessed data-products.
      */
    // TODO: Use a more limited queue -- possibly a user preference
     private final BlockingQueue<DataProduct>           processingQueue = new SynchronousQueue<DataProduct>();
     /**
      * The "isRunning" latch.
      */
     private final CountDownLatch                       isRunningLatch  = new CountDownLatch(
                                                                                1);
 
     /**
      * Adds a processing action to a data-product category.
      * 
      * @param pattern
      *            The pattern that selects the relevant data-products.
      * @param action
      *            The action to be executed on data-products that pass the
      *            filter.
      */
     public void add(final Pattern pattern, final Action action) {
         final List<Action> newList = new LinkedList<Action>();
         List<Action> list = actions.putIfAbsent(pattern, newList);
         if (list == null) {
             list = newList;
         }
         synchronized (list) {
             list.add(action);
         }
     }
 
     @Override
     public Void call() throws InterruptedException {
         logger.trace("Starting up: {}", this);
         isRunningLatch.countDown();
         try {
             for (;;) {
                 /*
                  * TODO: Handle interruption better when the queue is not empty
                  */
                 final DataProduct product = processingQueue.take();
                 try {
                     matchAndProcess(product);
                 }
                 catch (final IOException e) {
                     logger.error("Couldn't process data-product: " + product, e);
                 }
             }
         }
         finally {
             logger.trace("Done: {}", this);
         }
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
         isRunningLatch.await();
     }
 
     /**
      * Queues a data-product for processing. Blocks until the data-product can
      * be queued.
      * 
      * @param dataProduct
      *            The data-product to be processed
      * @throws InterruptedException
      *             if the current thread is interrupted
      */
     void put(final DataProduct dataProduct) throws InterruptedException {
         processingQueue.put(dataProduct);
     }
 
     /**
      * Processes a data-product. A data-product will be acted upon by matching
      * actions in the order in which the actions were added.
      * 
      * @param dataProduct
      *            The data-product to process.
      * @return <code>true</code> if and only if the given data-product was
      *         selected for processing.
      * @throws InterruptedException
      *             if the current thread is interrupted.
      * @throws IOException
      *             if an I/O error occurs.
      */
     private boolean matchAndProcess(final DataProduct dataProduct)
             throws IOException, InterruptedException {
         boolean processed = false;
         for (final Map.Entry<Pattern, List<Action>> entry : actions.entrySet()) {
             final Matcher matcher = dataProduct.matcher(entry.getKey());
             if (matcher.matches()) {
                 for (final Action action : entry.getValue()) {
                     // Foreign method. Don't call while synchronized
                     action.execute(matcher, dataProduct);
                     processed = true;
                 }
             }
         }
         return processed;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public synchronized String toString() {
         return getClass().getSimpleName() + " [" + actions.size() + " actions]";
     }
 }
