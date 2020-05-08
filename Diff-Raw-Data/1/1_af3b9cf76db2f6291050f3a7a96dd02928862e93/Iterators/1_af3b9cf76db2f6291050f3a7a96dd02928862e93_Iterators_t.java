 package com.socrata.util.iterators;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class Iterators {
     public static final Iterator EMPTY_ITERATOR = new Iterator() {
         public boolean hasNext() { return false; }
         public Object next() { throw new NoSuchElementException(); }
         public void remove() { throw new UnsupportedOperationException(); }
     };
 
     @SuppressWarnings("unchecked")
     public static <T> Iterator<T> empty() { return (Iterator<T>)EMPTY_ITERATOR; }
 
     public static <T> Iterator<T> singleton(final T obj) {
         return new Iterator<T>() {
             private boolean used = false;
 
             public boolean hasNext() { return !used; }
 
             public T next() {
                 if(used) throw new NoSuchElementException();
                 used = true;
                 return obj;
             }
 
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     public static <T> Iterator<List<T>> grouped(final Iterator<T> input, final int groupSize) {
         return new GroupedIterator<T>(input, groupSize);
     }
 
     public static <T> Iterator<T> flattened(Iterator<Iterator<T>> input) {
         return new FlatteningIterator<T>(input);
     }
 
    @SuppressWarnings("unchecked")
     public static <T> Iterator<T> append(Iterator<T>... inputs) {
         if(inputs.length == 0)
             return empty();
         else if(inputs.length == 1)
             return inputs[0];
         else
             return new AppendedIterator<T>(inputs);
     }
 }
