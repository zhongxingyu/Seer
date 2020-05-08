 package jDistsim.core.simulation.simulator.event;
 
 import java.util.Iterator;
 import java.util.concurrent.PriorityBlockingQueue;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 18.2.13
  * Time: 22:12
  */
 public class Calendar<TEvent extends ScheduleEvent> implements Iterable<TEvent> {
 
     private PriorityBlockingQueue<TEvent> queue;
 
     public Calendar() {
         this(20);
     }
 
     public Calendar(int initialCapacity) {
         queue = new PriorityBlockingQueue<>(initialCapacity);
     }
 
     public int size() {
         return queue.size();
     }
 
     public boolean isEmpty() {
         return queue.isEmpty();
     }
 
     public void put(TEvent event) {
         queue.put(event);
     }
 
     public TEvent poll() {
         return queue.isEmpty() ? null : queue.poll();
     }
 
     public TEvent peek() {
         return queue.isEmpty() ? null : queue.peek();
     }
 
     public TEvent get(int index) {
         int i = 0;
         for (TEvent event : queue) {
             if (index == i++)
                 return event;
         }
         throw new IndexOutOfBoundsException();
     }
 
     @Override
     public Iterator<TEvent> iterator() {
         return queue.iterator();
     }

    public void remove(TEvent event) {
        queue.remove(event);
    }
 }
