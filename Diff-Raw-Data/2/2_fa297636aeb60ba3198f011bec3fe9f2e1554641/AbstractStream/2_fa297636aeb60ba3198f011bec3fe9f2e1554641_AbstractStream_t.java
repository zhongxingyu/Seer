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
 package net.sf.staccato.commons.collections.stream;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.staccato.commons.collections.iterable.Iterables;
 import net.sf.staccato.commons.collections.iterable.internal.AbstractUnmodifiableIterator;
 import net.sf.staccato.commons.collections.iterable.internal.IterablesInternal;
 import net.sf.staccato.commons.collections.iterable.internal.UnmodifiableIterator;
 import net.sf.staccato.commons.lang.Applicable;
 import net.sf.staccato.commons.lang.Applicable2;
 import net.sf.staccato.commons.lang.Evaluable;
 import net.sf.staccato.commons.lang.Option;
 import net.sf.staccato.commons.lang.Provider;
 
 /**
  * An abstract implementation of a {@link Stream}. Only it {@link Iterator}
  * method is abstract
  * 
  * @author flbulgarelli
  * 
  * @param <T>
  */
 public abstract class AbstractStream<T> implements Stream<T> {
 
 	@Override
 	public int size() {
 		int size = 0;
 		for (@SuppressWarnings("unused")
 		T element : this)
 			size++;
 		return size;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return IterablesInternal.isEmptyInternal(this);
 	}
 
 	@Override
 	public boolean contains(T element) {
 		return IterablesInternal.containsInternal(this, element);
 	}
 
 	@Override
 	public T value() {
 		return any();
 	}
 
 	@Override
 	public Stream<T> filter(final Evaluable<? super T> predicate) {
 		return new AbstractStream<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new UnmodifiableIterator<T>(AbstractStream.this.iterator()) {
 					private T next;
 
 					@Override
 					public boolean hasNext() {
 						while (super.hasNext())
 							if (predicate.eval((next = super.next())))
 								return true;
 						return false;
 					}
 
 					@Override
 					public T next() {
 						return next;
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public Stream<T> takeWhile(final Evaluable<? super T> predicate) {
 		return new AbstractStream<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new UnmodifiableIterator<T>(AbstractStream.this.iterator()) {
 					private T next;
 
 					@Override
 					public boolean hasNext() {
 						return super.hasNext() && predicate.eval((next = super.next()));
 					}
 
 					@Override
 					public T next() {
 						return next;
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public Stream<T> take(final int amountOfElements) {
 		return new AbstractStream<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new UnmodifiableIterator<T>(AbstractStream.this.iterator()) {
 					private int i = 0;
 
 					@Override
 					public boolean hasNext() {
 						return i < amountOfElements && super.hasNext();
 					}
 
 					@Override
 					public T next() {
 						i++;
 						return super.next();
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public T reduce(Applicable2<? super T, ? super T, ? extends T> applicable) {
 		return IterablesInternal.reduceInternal(this, applicable);
 	}
 
 	@Override
 	public <O> O fold(O initial,
 		Applicable2<? super O, ? super T, ? extends O> applicable) {
 		return IterablesInternal.foldInternal(this, initial, applicable);
 	}
 
 	@Override
 	public T apply(Integer n) {
 		return get(n);
 	}
 
 	@Override
 	public T any() {
 		return IterablesInternal.anyInternal(this);
 	}
 
 	@Override
 	public Option<T> anyOrNone() {
 		return IterablesInternal.anyOrNoneInternal(this);
 	}
 
 	@Override
 	public T anyOrNull() {
 		return IterablesInternal.anyOrNullInternal(this);
 	}
 
 	@Override
 	public T anyOrElse(Provider<T> provider) {
 		return Iterables.anyOrElse(this, provider);
 	}
 
 	@Override
 	public T anyOrElse(T value) {
 		return Iterables.anyOrElse(this, value);
 	}
 
 	@Override
 	public T find(Evaluable<? super T> predicate) {
 		return Iterables.find(this, predicate);
 	}
 
 	@Override
 	public Option<T> findOrNone(Evaluable<? super T> predicate) {
 		return Iterables.findOrNone(this, predicate);
 	}
 
 	@Override
 	public T findOrNull(Evaluable<? super T> predicate) {
 		return Iterables.findOrNull(this, predicate);
 	}
 
 	@Override
 	public T findOrElse(Evaluable<? super T> predicate,
 		Provider<? extends T> provider) {
 		return Iterables.findOrElse(this, predicate, provider);
 	}
 
 	@Override
 	public boolean all(Evaluable<? super T> predicate) {
 		return Iterables.all(this, predicate);
 	}
 
 	@Override
 	public boolean any(Evaluable<? super T> predicate) {
 		return Iterables.any(this, predicate);
 	}
 
 	@Override
 	public <O> Stream<O> map(final Applicable<? super T, ? extends O> applicable) {
 		return new AbstractStream<O>() {
 			@Override
 			public Iterator<O> iterator() {
 				return new AbstractUnmodifiableIterator<O>() {
					private final Iterator<T> iter = AbstractStream.this.iterator();
 
 					@Override
 					public boolean hasNext() {
 						return iter.hasNext();
 					}
 
 					@Override
 					public O next() {
 						return applicable.apply(iter.next());
 					}
 				};
 			}
 		};
 	}
 
 	@Override
 	public T first() {
 		return get(0);
 	}
 
 	@Override
 	public T second() {
 		return get(1);
 	}
 
 	@Override
 	public T third() {
 		return get(2);
 	}
 
 	@Override
 	public T last() {
 		return get(size() - 1);
 	}
 
 	@Override
 	public T get(int n) {
 		return Iterables.get(this, n);
 	}
 
 	@Override
 	public Set<T> toSet() {
 		return Iterables.asSet(this);
 	}
 
 	@Override
 	public List<T> toList() {
 		return Iterables.asList(this);
 	}
 
 }
