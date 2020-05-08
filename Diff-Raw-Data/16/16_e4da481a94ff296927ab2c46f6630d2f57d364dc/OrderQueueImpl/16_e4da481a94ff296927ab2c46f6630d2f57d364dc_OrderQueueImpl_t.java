 package edu.uw.danco.broker;
 
 import edu.uw.ext.framework.broker.OrderDispatchFilter;
 import edu.uw.ext.framework.broker.OrderProcessor;
 import edu.uw.ext.framework.broker.OrderQueue;
 import edu.uw.ext.framework.order.Order;
 
 import java.util.Comparator;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dcostinett
  * Date: 4/28/13
  * Time: 3:05 PM
  *
  * A simple OrderQueue implementation backed by a TreeSet.
  */
 public final class OrderQueueImpl<E extends Order> implements OrderQueue<E> {
 
     /** The logger for this class */
     private static final Logger LOGGER = Logger.getLogger(OrderQueueImpl.class.getName());
 
     /** Backing store for orders */
     private TreeSet<E> queue;
 
     /** The processor used during order processing */
     private OrderProcessor orderProcessor;
 
     /** The dispatch filter used to control dispatching from this queue */
     private OrderDispatchFilter<?, E> filter;
 
 
     /**
      * Constructor
      * @param orderComparator - Comparator to be used for ordering
      * @param filter - the dispatch filter used to control dispatching from this queue
      */
     public OrderQueueImpl(final Comparator orderComparator, final OrderDispatchFilter<?, E> filter) {
         queue = new TreeSet<E>(orderComparator);
         this.filter = filter;
     }
 
 
     /**
      * Constructor
      * @param filter - the dispatch filter used to control dispatching from this queue
      */
     public OrderQueueImpl(final OrderDispatchFilter<?, E> filter) {
         queue = new TreeSet<E>();
         this.filter = filter;
     }
 
 
     /**
      * Adds the specified order to the queue. Subsequent to adding the order dispatches any dispatchable orders.
      * @param order - the order to be added to the queue
      */
     @Override
     public void enqueue(final E order) {
         queue.add(order);
         dispatchOrders();
     }
 
 
     /**
      * Removes the highest dispatchable order in the queue. If there are orders in the queue but they do not meet the
      * dispatch threshold order will not be removed and null will be returned.
      * @return - the first dispatchable order in the queue, or null if there are no dispatchable orders in the queue
      */
     @Override
     public E dequeue() {
        E order = null;
        if (filter.check(queue.first())) {
            order = queue.first();
            queue.remove(order);
        }
        return order;
     }
 
 
     /**
      * Executes the orderProcessor for each dispatchable order. Each dispatchable order is in turn removed from the
      * queue and passed to the callback. If no callback is registered the order is simply removed from the queue.
      */
     @Override
     public void dispatchOrders() {
         while (!queue.isEmpty() && filter.check(queue.first())) {
             Order order = queue.first();
             orderProcessor.process(order);
             final boolean removed = queue.remove(order);
             if (!removed) {
                 LOGGER.log(Level.SEVERE, "Unable to remove order from order queue");
             }
         }
     }
 
 
     /**
      * Registers the callback to be used during order processing.
      * @param proc - the callback to be registered
      */
     @Override
     public void setOrderProcessor(final OrderProcessor proc) {
         this.orderProcessor = proc;
     }
 }
