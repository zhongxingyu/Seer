 package org.jsense.compute;
 
 import com.google.common.annotations.Beta;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * A {@code SampleBasedSlidingWindow} is a sliding window of a fixed size, optionally overlapping, working over
  * an ordered sequence of data.
  * <p/>
  * The sliding window is lazy, and only loads more data when it is accessed through the iterator.
  * <p/>
  * The last {@code ImmutableList&lt;E&gt;} returned by the {@code Iterator} may hold less samples than the window size.
  * <p/>
  * The {@code Iterator} does not support the {@code remove()} method, which throws
  * an {@link UnsupportedOperationException} if called.
  *
  * @author Markus Wüstenberg
  * @param <E> The type in the window.
  */
 @Beta
 public final class SampleBasedSlidingWindow<E> implements Iterable<Iterable<E>> {
 
     private final Iterable<E> data;
     private final int size;
 
     private SampleBasedSlidingWindow(Builder<E> builder) {
         this.data = Iterables.unmodifiableIterable(builder.data);
         this.size = builder.size;
     }
 
     /**
      * Returns the iterator, which can only be traversed once.
      * @return the iterator
      */
     @Override
     public Iterator<Iterable<E>> iterator() {
         return new SampleBasedSlidingWindowIterator();
     }
 
     public static <E> Builder<E> newBuilder() {
         return new Builder<E>();
     }
 
     /**
      * A {@code Builder} for the {@code SampleBasedSlidingWindow}.
      * @param <E> The type in the window.
      */
     public static final class Builder<E> {
 
         private int size;
         private Iterable<E> data = ImmutableList.of();
         private boolean hasSize, hasData;
 
         public Builder setSize(int size) {
             this.size = size;
             hasSize = true;
             return this;
         }
 
         public Builder add(E element, E... elements) {
             Preconditions.checkNotNull(element);
             Preconditions.checkNotNull(elements);
 
            if (elements.length == 0) {
                return this;
            }

             hasData = true;
 
             ImmutableList.Builder<E> listBuilder = ImmutableList.builder();
             listBuilder.add(element);
            listBuilder.add(elements);
 
             data = Iterables.concat(data, listBuilder.build());
 
             return this;
         }
 
         public Builder add(Iterable<E> elements) {
             Preconditions.checkNotNull(elements);
             if (Iterables.isEmpty(elements)) {
                 return this;
             }
 
             hasData = true;
             data = Iterables.concat(data, elements);
 
             return this;
         }
 
         public SampleBasedSlidingWindow<E> build() {
             if (!hasSize || !hasData) {
                 throw new IllegalStateException("Size hasn't been set or no data supplied.");
             }
             return new SampleBasedSlidingWindow<E>(this);
         }
     }
 
     /**
      * An iterator for the sliding window.
      */
     private class SampleBasedSlidingWindowIterator implements Iterator<Iterable<E>> {
 
         private Iterable<E> skippedData = data;
 
         @Override
         public boolean hasNext() {
             return !Iterables.isEmpty(skippedData);
         }
 
         @Override
         public Iterable<E> next() {
             if (!hasNext()) {
                 throw new NoSuchElementException("No more elements.");
             }
             Iterable<E> limitedData = Iterables.limit(skippedData, size);
             skippedData = Iterables.skip(skippedData, size);
             return limitedData;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("The remove operation is not supported by this iterator.");
         }
     }
 }
