 /*
  Copyright (c) 2010, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.collections.stream.impl.internal;
 
 import java.util.NoSuchElementException;
 
 import net.sf.staccatocommons.check.annotation.NonNull;
 import net.sf.staccatocommons.collections.stream.AbstractStream;
 import net.sf.staccatocommons.collections.stream.Stream;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.defs.type.NumberType;
 import net.sf.staccatocommons.iterators.thriter.AdvanceThriterator;
 import net.sf.staccatocommons.iterators.thriter.Thriter;
 import net.sf.staccatocommons.iterators.thriter.Thriterator;
 import net.sf.staccatocommons.iterators.thriter.Thriterators;
 
 /**
  * @author flbulgarelli
  * 
  */
 public final class ConcatStream<A> extends AbstractStream<A> {
 	private final Stream<A> stream;
 	private final Iterable<A> other;
 
 	/**
 	 * Creates a new {@link ConcatStream}
 	 */
 	public ConcatStream(@NonNull Stream<A> stream, @NonNull Iterable<A> other) {
 		this.stream = stream;
 		this.other = other;
 	}
 
	public Thriterator<A> iterator() {
 		return new AdvanceThriterator<A>() {
 			private Thriter<A> iter = stream.iterator();
 			private boolean second = false;
 
 			public boolean hasNext() {
 				if (iter.hasNext())
 					return true;
 
 				if (second)
 					return false;
 
 				iter = Thriterators.from(other.iterator());
 				second = true;
 				return iter.hasNext();
 			}
 
 			public void advanceNext() throws NoSuchElementException {
 				iter.advanceNext();
 			}
 
 			public A current() throws NoSuchElementException {
 				return iter.current();
 			}
 
 			public Thunk<A> delayedCurrent() {
 				return iter.delayedCurrent();
 			}
 		};
 	}
 
 	public NumberType<A> numberType() {
 		return stream.numberType();
 	}
 }
