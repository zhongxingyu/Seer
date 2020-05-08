 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.utils;
 
 /*************************************************************************
  *  Compilation:  javac RingBuffer.java
  *  Execution:    java RingBuffer
  *  
  *  Ring buffer (fixed size queue) implementation using a circular array
  *  (array with wrap-around).
  *
  *************************************************************************/
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class RingBuffer<Item> implements Iterable<Item> {
     private Item[] a;            // queue elements
     private int N = 0;           // number of elements on queue
     private int first = 0;       // index of first element of queue
     private int last  = 0;       // index of next available slot
     boolean checkBoundaries = true;
     
     // cast needed since no generic array creation in Java
     public RingBuffer(int capacity) {
         a = (Item[]) new Object[capacity];
     }
     
     public RingBuffer(int capacity, boolean checkBoundaries) {
         a = (Item[]) new Object[capacity];
         this.checkBoundaries = checkBoundaries;
     }
 
     public boolean isEmpty() { return N == 0; }
     public int size()        { return N;      }
 
     public void enqueue(Item item) {
         if (N == a.length && checkBoundaries) { throw new RuntimeException("Ring buffer overflow"); }
         a[last] = item;
         last = (last + 1) % a.length;     // wrap-around
         N = Math.min(N+1, a.length);
     }
 
     // remove the least recently added item - doesn't check for underflow
     public Item dequeue() {
         if (isEmpty()) { throw new RuntimeException("Ring buffer underflow"); }
         Item item = a[first];
         a[first] = null;                  // to help with garbage collection
         N=Math.max(N-1, 0);
         first = (first + 1) % a.length;   // wrap-around
         return item;
     }
     
     /**
      * Returns [HEAD-i]-th element from queue. If i=0 head is returned.
      * Does not remove elements from queue. if i>=length then NoSuchElementException 
      * is thrown.
      * 
      * @param i
      * @return 
      */
     public Item get(int i) {
         // validate i
         if (i >= a.length || i >= N){
             throw new NoSuchElementException("Cannot reach element");
         }
         
         int newIndex = (last - i - 1) % a.length;
         if (newIndex<0){
             newIndex+=a.length;
         }
         
        return a[newIndex];
     }
 
     @Override
     public Iterator<Item> iterator() { return new RingBufferIterator(); }
 
     public boolean isCheckBoundaries() {
         return checkBoundaries;
     }
 
     public void setCheckBoundaries(boolean checkBoundaries) {
         this.checkBoundaries = checkBoundaries;
     }
     
     // an iterator, doesn't implement remove() since it's optional
     private class RingBufferIterator implements Iterator<Item> {
         private int i = 0;
         @Override
         public boolean hasNext()  { return i < N;                               }
         @Override
         public void remove()      { throw new UnsupportedOperationException();  }
 
         @Override
         public Item next() {
             if (!hasNext()) throw new NoSuchElementException();
             Item item = a[(i + first) % a.length];
             i++;
             return item;
         }
     }
 
 }
 
