 // 
 //  QueueNode.java
 //  A container for holding threads to be put to sleep or woken up.
 //  
 //  Created by John Hildebrant on 2011-11-12.
 // 
 import java.util.Vector;
 
 public class QueueNode {
   private Vector queue;
   
   //constructor
   public QueueNode() {
     queue = new Vector<Integer>();
     queue.clear();
   }
   
   // sleep until notified and return pid from queue
   public synchronized int sleep() {
     if (queue.size() == 0)
       try {
         wait();
       } catch (InterruptedException iex) {}
       return (Integer)queue.remove(0);
   }
   
   // wakeup a thread
  public synchronized void wakeup(int pid) {
    queue.add(pid);
     notify();
   }
 }
