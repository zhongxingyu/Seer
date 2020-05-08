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
 
 package net.sf.staccatocommons.iterators;
 
 import java.util.Iterator;
 
 import net.sf.staccatocommons.restrictions.value.Unmodifiable;
 
 /**
  * An abstract {@link Iterator} that does not support {@link #remove()}.
  * Although its name suggests the contrary, {@link AbstractUnmodifiableIterator}
  * are not {@link Unmodifiable}, actually.
  * 
  * @author flbulgarelli
  * 
  * @param <T>
  */
 public abstract class AbstractUnmodifiableIterator<T> implements Iterator<T> {
 
   /**
    * Creates a new {@link AbstractUnmodifiableIterator}
    */
   public AbstractUnmodifiableIterator() {
     super();
   }
 
   @Override
   public final void remove() {
    throw new UnsupportedOperationException("This iterator is unmodifiable");
   }
 
 }
