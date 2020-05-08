 /*
     Copyright (c) 2012 Ray A. Conner
 
     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:
 
     The above copyright notice and this permission notice shall be included in all
     copies or substantial portions of the Software.
 
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     SOFTWARE.
  */
 
 package com.github.rconner.anansi;
 
 import com.google.common.annotations.Beta;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 
 import java.util.Iterator;
 
 /**
  * Factory methods for creating lazy Iterators and Iterables.
  *
  * @author rconner
  */
 @Beta
 public class Lazy {
 
     /**
      * Prevent instantiation.
      */
     private Lazy() {
     }
 
     public static <T> Iterator<T> iterator(final Iterable<T> iterable) {
         Preconditions.checkNotNull(iterable);
         if (iterable instanceof LazyIterable<?>) {
             return iterable.iterator();
         }
         return new LazyIterator<T>(iterable);
     }
 
     public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
         Preconditions.checkNotNull(iterable);
         if (iterable instanceof LazyIterable<?>) {
             return iterable;
         }
         return new LazyIterable<T>(iterable);
     }
 
     public static <V, E> Traversal<V, E> traversal(final Traversal<V, E> traversal) {
         Preconditions.checkNotNull(traversal);
         if (traversal instanceof LazyIterable<?>) {
             return traversal;
         }
         return new LazyTraversal<V, E>(traversal);
     }
 
     public static <V, E> Traverser<V, E> traverser(final Function<V, Traversal<V, E>> traverser) {
         Preconditions.checkNotNull(traverser);
         if (traverser instanceof LazyTraverser) {
             return (LazyTraverser<V, E>) traverser;
         }
         return new LazyTraverser<V, E>(traverser);
     }
 
 
     private static class LazyIterator<T> implements Iterator<T> {
         private final Iterable<T> iterable;
         private Iterator<T> delegate = null;
 
         private LazyIterator(Iterable<T> iterable) {
             this.iterable = iterable;
         }
 
         private Iterator<T> getDelegate() {
             if (delegate == null) {
                 delegate = iterable.iterator();
             }
             return delegate;
         }
 
         public boolean hasNext() {
             return getDelegate().hasNext();
         }
 
         public T next() {
             return getDelegate().next();
         }
 
         public void remove() {
             Preconditions.checkState(delegate != null);
             delegate.remove();
         }
     }
 
     private static class LazyIterable<T> implements Iterable<T> {
         final Iterable<T> delegate;
 
         private LazyIterable(Iterable<T> delegate) {
             this.delegate = delegate;
         }
 
         public Iterator<T> iterator() {
             return Lazy.iterator(delegate);
         }
     }
 
     private static class LazyTraversal<V, E> implements Traversal<V, E> {
         final Traversal<V, E> delegate;
 
         private LazyTraversal(Traversal<V, E> delegate) {
             this.delegate = delegate;
         }
 
         public Iterator<Path<V, E>> iterator() {
             return Lazy.iterator(delegate);
         }
     }
 
     private static class LazyTraverser<V, E> implements Traverser<V, E> {
         final Function<V, Traversal<V, E>> delegate;
 
         private LazyTraverser(Function<V, Traversal<V, E>> delegate) {
             this.delegate = delegate;
         }
 
         public Traversal<V, E> apply(final V from) {
             return new Traversal<V, E>() {
                 public Iterator<Path<V, E>> iterator() {
                    return Lazy.iterator(delegate.apply(from));
                 }
             };
         }
     }
 
 }
