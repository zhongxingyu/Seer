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
 
 import java.util.Iterator;
 
 /**
  * An Iterator with a prune operation.
  *
  * @param <T>
  * @author rconner
  */
 public interface PruningIterator<T> extends Iterator<T> {
 
     /**
      * Signals this {@link Iterator} to not explore beyond the last object returned by {@link #next()}. This method can
      * be called only once per call to {@code next()}. After calling this method (and before calling {@code next()}
      * again), {@link #remove()} will throw an {@code IllegalStateException}.
      *
      * @throws IllegalStateException         if {@code next()} has not yet been called, or {@code remove()}, or {@code
      *                                       prune()} has been called after the last call to {@code next()}.
      * @throws UnsupportedOperationException if this method is not supported by this {@link Iterator}.
      */
     public void prune();
 
 }
