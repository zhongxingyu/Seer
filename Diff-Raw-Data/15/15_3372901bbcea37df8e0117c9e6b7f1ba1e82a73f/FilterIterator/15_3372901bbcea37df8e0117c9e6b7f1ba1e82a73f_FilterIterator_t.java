 package com.crudetech.sample.filter;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class FilterIterator<T> implements Iterator<T> {
     private final Iterator<? extends T> source;
     private final Predicate<? super T> predicate;
     private T current;
     private boolean isPositioned = false;
 
     public FilterIterator(Iterator<? extends T> source, Predicate<? super T> predicate) {
         this.source = source;
         this.predicate = predicate;
     }
 
     @Override
     public boolean hasNext() {
        return isPositioned || position();
     }
 
     private boolean position() {
         while (source.hasNext()) {
             current = source.next();
             if (predicate.evaluate(current)) {
                 isPositioned = true;
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public T next() {
         if (isPositioned || position()) {
             isPositioned = false;
             return current;
         }
         throw new NoSuchElementException();
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 }
