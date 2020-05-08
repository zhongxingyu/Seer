 package it.unipd.dei.webqual.converter.merge;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Iterates lazily over the sequence obtained as the merge of two given
  * sequences
  */
 public class LazyMergeIterator<T> implements Iterator<T> {
 
   private Iterator<T> first;
   private Iterator<T> second;
 
   private Comparator<T> comparator;
   private Merger<T> merger;
 
   private T firstNext;
   private T secondNext;
 
   public LazyMergeIterator( Iterator<T> first,
                             Iterator<T> second,
                             Comparator<T> comparator,
                             Merger<T> merger) {
 
     this.first = first;
     this.second = second;
 
     this.comparator = comparator;
     this.merger = merger;
 
     this.firstNext = (first.hasNext())? first.next() : null;
     this.secondNext = (second.hasNext())? second.next() : null;
 
   }
 
   public LazyMergeIterator( Iterator<T> single,
                             Comparator<T> comparator,
                             Merger<T> merger) {
     this(single, new DumbIterator<T>(), comparator, merger);
   }
 
   @Override
   public boolean hasNext() {
     return firstNext != null || secondNext != null;
   }
 
   private T getAndNextFirst() {
     T result = firstNext;
     this.firstNext = (first.hasNext())? first.next() : null;
     return result;
   }
 
   private T getAndNextSecond() {
     T result = secondNext;
     this.secondNext = (second.hasNext())? second.next() : null;
     return result;
   }
 
   @Override
   public T next() {
     if(firstNext == null && secondNext != null) {
       return getAndNextSecond();
     } else if (firstNext != null && secondNext == null) {
       return getAndNextFirst();
     } else if (firstNext == null && secondNext == null) {
       throw new NoSuchElementException();
     }
 
     int res = comparator.compare(firstNext, secondNext);
 
     if(res < 0) {
       return getAndNextFirst();
     } else if (res > 0) {
       return getAndNextSecond();
     }
 
     // if the elements are equals we should merge them, along with all the
     // subsequent equals elements
     T merged = merger.merge(getAndNextFirst(), getAndNextSecond());
 
     while (firstNext != null && comparator.compare(merged, firstNext) == 0) {
       merged = merger.merge(merged, getAndNextFirst());
     }
    while (firstNext != null && comparator.compare(merged, secondNext) == 0) {
       merged = merger.merge(merged, getAndNextSecond());
     }
 
     return merged;
   }
 
   @Override
   public void remove() {
     throw new UnsupportedOperationException();
   }
 
   public static <T> LazyMergeIterator<T> compose(
     Comparator<T> comparator, Merger<T> merger, LazyMergeIterator<T>... iterators) {
 
     if(iterators.length == 1) {
       return new LazyMergeIterator<T>(
         iterators[0], new DumbIterator<T>(), comparator, merger);
     }
     if(iterators.length == 2) {
       return new LazyMergeIterator<>(iterators[0], iterators[1], comparator, merger);
     }
 
     int h = iterators.length / 2;
     LazyMergeIterator<T>
       a = compose(comparator, merger, Arrays.copyOfRange(iterators, 0, h)),
       b = compose(comparator, merger, Arrays.copyOfRange(iterators, h, iterators.length));
 
     return new LazyMergeIterator<>(a, b, comparator, merger);
   }
 
   protected static class DumbIterator<T> implements Iterator<T> {
 
     @Override
     public boolean hasNext() {
       return false;
     }
 
     @Override
     public T next() {
       throw new UnsupportedOperationException();
     }
 
     @Override
     public void remove() {
       throw new UnsupportedOperationException();
     }
   }
 
 }
