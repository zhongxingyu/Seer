 /**
  *  Copyright (c) 2010-2012, The StaccatoCommons Team
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation; version 3 of the License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  */
 
 
 package net.sf.staccatocommons.collections.stream.internal;
 
 import java.util.NoSuchElementException;
 
 import net.sf.staccatocommons.collections.stream.Stream;
 import net.sf.staccatocommons.collections.stream.Streams;
 import net.sf.staccatocommons.iterators.thriter.Thriterator;
 import net.sf.staccatocommons.iterators.thriter.Thriterators;
 
 /**
  * @author flbulgarelli
  * 
  */
 public final class SingleStream<A> extends StrictStream<A> {
 
   private A element;
 
   /**
    * Creates a new {@link SingleStream}
    */
   public SingleStream(A element) {
     this.element = element;
   }
 
   @Override
   public Thriterator<A> iterator() {
     return Thriterators.from(element);
   }
 
   @Override
   public int size() {
     return 1;
   }
 
   @Override
   public Stream<A> tail() {
     return Streams.empty();
   }
 
   @Override
   public A get(int n) {
     if (n == 0)
       return element;
    throw new NoSuchElementException("At " + n);
   }
 
   @Override
   public boolean isBefore(A previous, A next) {
     return false;
   }
 
   @Override
   public boolean isEmpty() {
     return false;
   }
 
 }
