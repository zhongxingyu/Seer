 package com.foursquare.heapaudit;
 
 import java.util.AbstractCollection;
 import java.util.Iterator;
 
 // This is a poor man's implementation of a collection library intended for
 // internal usage only. The following does not implement the full interface of a
 // typical collection and is not thread safe. The main reason for using this
 // internal collection as oppose to ArrayList is to avoid the circular
 // dependency of the HeapAudit instrumentation on ArrayList while Arraylist is
 // also being instrumented by HeapAudit. Otherwise, an infinite recursion will
 // occur during the instrumentation phase.
 
 public class HeapCollection<E> extends AbstractCollection<E> {
 
     private Object[] objects;
 
     private int size;
 
     private int index;
 
     public HeapCollection() {
 
         objects = new Object[64];
 
         size = 0;
 
         index = 0;
 
     }
 
     public boolean add(E e) {
 
         if (size == objects.length) {
 
             Object[] collection = new Object[size * 2];
 
             System.arraycopy(objects,
                              0,
                              collection,
                              0,
                              size);
 
             objects = collection;
 
         }
         else if (size < index) {
 
             for (int i = 0; i < index; ++i) {
 
                 if (objects[i] == null) {
 
                     objects[i] = e;
 
                     ++size;
 
                     break;
 
                 }
 
             }
 
         }
         else {
 
             objects[index] = e;
 
             ++size;
 
             ++index;
 
         }
 
         return true;
     }
 
     public boolean remove(Object e) {
 
         for (int i = 0; i < index; ++i) {
 
            if (e.equals(objects[i])) {
 
                 objects[i] = null;
 
                 --size;
 
                 if (i == index - 1) {
 
                     --index;
 
                 }
 
                 return true;
 
             }
 
         }
 
         return false;
 
     }
 
     public int size() {
 
         return size;
 
     }
 
     public Iterator<E> iterator() {
 
         return new HeapIterator();
 
     }
 
     private class HeapIterator implements Iterator<E> {
 
         private int cursor = -1;
 
         private int count = HeapCollection.this.size;
 
         public boolean hasNext() {
 
             return count > 0;
 
         }
 
         public E next() {
 
             while (objects[++cursor] == null);
 
             --count;
 
             return (E)objects[cursor];
 
         }
 
         public void remove() {
 
             objects[cursor] = null;
 
             --HeapCollection.this.size;
 
         }
 
     }
 
 }
