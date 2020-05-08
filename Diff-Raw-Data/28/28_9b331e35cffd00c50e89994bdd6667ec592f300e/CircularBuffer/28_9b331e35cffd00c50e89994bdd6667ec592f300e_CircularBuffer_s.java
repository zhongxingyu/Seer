 package org.jboss.jdf.example.ticketmonster.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class CircularBuffer<T> {
 
    private List<T> buffer;
 
     private volatile int cursor = 0;
 
     private final int capacity;
 
     public CircularBuffer(int capacity) {
        buffer = Collections.synchronizedList(new ArrayList<T>());
         this.capacity = capacity;
     }
 
     public void add(T item) {
         synchronized (buffer) {
            buffer.add (cursor++,item);
         }
     }
 
     public List<T> getContents() {
         synchronized (buffer) {
             if (cursor > capacity) {
                List<T> returnedItems = buffer.subList(cursor % capacity, capacity);
                returnedItems.addAll(buffer.subList(0, cursor % capacity));
                 return returnedItems;
             } else {
                return buffer.subList(0, cursor);
             }
         }
     }
 }
