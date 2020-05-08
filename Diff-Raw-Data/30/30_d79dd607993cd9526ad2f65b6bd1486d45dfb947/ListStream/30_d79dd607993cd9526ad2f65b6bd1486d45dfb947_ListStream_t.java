 /**
  *  Copyright (c) 2011, The Staccato-Commons Team
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
 
 package net.sf.staccatocommons.collections.stream.impl;
 
 import java.util.List;
 
 import net.sf.staccatocommons.restrictions.check.NonNull;
 
 /**
  * 
  * @author flbulgarelli
  * 
  * @param <A>
  */
 public class ListStream<A> extends CollectionStream<A> {
 
   /**
    * Creates a new {@link ListStream}
    * 
    * @param iterable
    *          the list to wrap
    */
   public ListStream(@NonNull List<? extends A> iterable) {
     super(iterable);
   }
 
   @Override
   public final A get(int n) {
     return getList().get(n);
   }
 
   @Override
   public final int indexOf(A element) {
     return getList().indexOf(element);
   }
 
   protected List<A> getList() {
     return (List<A>) getCollection();
   }
 
  @Override
  public A last() {
    return get(size() - 1);
  }

 }
