 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  * 
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.arrays;
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.jbasics.annotation.ImmutableState;
 import org.jbasics.annotation.ThreadSafe;
 import org.jbasics.arrays.unstable.ArrayIterator;
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.checker.ContractViolationException;
 
 /**
  * A collection which is directly backed by an array. The collection itself does not offer any
  * operation to change therefore the collection is immutable itself. However since the array
  * given is not copied any change to the array will be reflected in this collection as well!
  * <p>
  * FIXME: Currently this class is under consolidation and most likely will be removed since there is no real benefit
  * compared to Arrays.asList(...) which does exactly the same. It seesms that the Arrays.asList is not as nice like this
  * collection. The idea of this class here is to actually have an imutable array as collection. Arrays version is
  * mutable to the content while not to the lenght.
  * </p>
  * 
  * @author Stephan Schloepke
  * @param <T>
  *            The type of the array collection.
  * @since 1.0
  */
@ThreadSafe(derived = true)
@ImmutableState(derived = true)
 public class ArrayCollection<T> implements List<T> {
 	private final T[] data;
 	private final int offset;
 	private final int size;
 
 	/**
 	 * Creates an {@link ArrayCollection} with the given data. The data is copied
 	 * and therefore the resulting collection is immutable!
 	 * 
 	 * @param data
 	 *            The data to create the collection for (NOT copied and must NOT be null)
 	 * @throws ContractViolationException
 	 *             If the given data is null.
 	 * @since 1.0
 	 */
 	public ArrayCollection(final T... data) {
 		this(ContractCheck.mustNotBeNull(data, "data").clone(), 0, data.length); //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates an {@link ArrayCollection} with the given data. The data is NOT copied
 	 * and therefore any change to the array will also lead to a change in the collection!
 	 * However since this method is only reachable by {@link #subList(int, int)} it does
 	 * not affect the immutability. The contract here MUST guarantee that the caller of
 	 * this constructor never changes the given array at all!
 	 * 
 	 * @param data
 	 *            The data to create the collection for (NOT copied and must NOT be null)
 	 * @throws ContractViolationException
 	 *             If the given data is null.
 	 * @since 1.0
 	 */
 	protected ArrayCollection(final T[] data, final int offset, final int size) {
 		this.data = ContractCheck.mustNotBeNull(data, "data"); //$NON-NLS-1$
 		this.offset = offset;
 		this.size = size;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#iterator()
 	 */
 	@Override
 	public Iterator<T> iterator() {
 		return new ArrayIterator<T>(this.offset, this.size, this.data);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#size()
 	 */
 	@Override
 	public int size() {
 		return this.size;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#isEmpty()
 	 */
 	@Override
 	public boolean isEmpty() {
 		return this.size == 0;
 	}
 
 	@Override
 	public T get(final int index) {
 		if (index >= this.size) {
 			throw new IndexOutOfBoundsException();
 		}
 		return this.data[index + this.offset];
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#toArray()
 	 */
 	@Override
 	public Object[] toArray() {
 		final Object[] dest = new Object[this.size];
 		System.arraycopy(this.data, this.offset, dest, 0, this.size);
 		return dest;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#toArray(T[])
 	 */
 	@Override
 	public <TA> TA[] toArray(final TA[] dest) {
 		@SuppressWarnings("unchecked") final TA[] destination = dest.length >= this.size ? dest : (TA[]) Array.newInstance(dest.getClass()
 				.getComponentType(), this.size);
 		System.arraycopy(this.data, this.offset, destination, 0, this.size);
 		return destination;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#contains(java.lang.Object)
 	 */
 	@Override
 	public boolean contains(final Object obj) {
 		for (int i = 0; i < this.size; i++) {
 			final Object current = this.data[i + this.offset];
 			if (current == obj || current != null && current.equals(obj)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Collection#containsAll(java.util.Collection)
 	 */
 	@Override
 	public boolean containsAll(final Collection<?> c) {
 		for (final Object obj : c) {
 			if (!contains(obj)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.List#indexOf(java.lang.Object)
 	 */
 	@Override
 	public int indexOf(final Object check) {
 		if (check == null) {
 			for (int i = 0; i < this.size; i++) {
 				if (this.data[i + this.offset] == null) {
 					return i;
 				}
 			}
 		} else {
 			for (int i = 0; i < this.size; i++) {
 				final T dataElement = this.data[i + this.offset];
 				if (check == dataElement || check.equals(dataElement)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.List#lastIndexOf(java.lang.Object)
 	 */
 	@Override
 	public int lastIndexOf(final Object check) {
 		if (check == null) {
 			for (int i = this.size - 1; i >= 0; i++) {
 				if (this.data[i + this.offset] == null) {
 					return i;
 				}
 			}
 		} else {
 			for (int i = this.size - 1; i >= 0; i++) {
 				final T dataElement = this.data[i + this.offset];
 				if (check == dataElement || check.equals(dataElement)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.List#listIterator()
 	 */
 	@Override
 	public ListIterator<T> listIterator() {
 		return new ArrayIterator<T>(this.offset, this.size, this.data);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.List#listIterator(int)
 	 */
 	@Override
 	public ListIterator<T> listIterator(final int index) {
 		return new ArrayIterator<T>(index, this.offset, this.size, this.data);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.List#subList(int, int)
 	 */
 	@Override
 	public List<T> subList(final int fromIndex, final int toIndex) {
 		if (fromIndex < 0 || toIndex + this.offset > this.data.length || fromIndex > toIndex) {
 			throw new IndexOutOfBoundsException();
 		}
 		return new ArrayCollection<T>(this.data, this.offset + fromIndex, toIndex - fromIndex);
 	}
 
 	// The rest is unsupported since optional and not interesting for an immutable collection.
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#add(java.lang.Object)
 	 */
 	@Override
 	public boolean add(final T e) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#remove(java.lang.Object)
 	 */
 	@Override
 	public boolean remove(final Object o) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#addAll(java.util.Collection)
 	 */
 	@Override
 	public boolean addAll(final Collection<? extends T> c) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#removeAll(java.util.Collection)
 	 */
 	@Override
 	public boolean removeAll(final Collection<?> c) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#retainAll(java.util.Collection)
 	 */
 	@Override
 	public boolean retainAll(final Collection<?> c) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.Collection#clear()
 	 */
 	@Override
 	public void clear() {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.List#addAll(int, java.util.Collection)
 	 */
 	@Override
 	public boolean addAll(final int index, final Collection<? extends T> c) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.List#set(int, java.lang.Object)
 	 */
 	@Override
 	public T set(final int index, final T element) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.List#add(int, java.lang.Object)
 	 */
 	@Override
 	public void add(final int index, final T element) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Operation is not supported.
 	 * 
 	 * @see java.util.List#remove(int)
 	 */
 	@Override
 	public T remove(final int index) {
 		throw new UnsupportedOperationException("Unsuported for imutable array collection"); //$NON-NLS-1$
 	}
 }
