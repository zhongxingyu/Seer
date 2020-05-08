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
 
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.checker.ContractViolationException;
 
 /**
  * Simple {@link Iterator} to iterate over any typed array.
  * 
  * @author Stephan Schloepke
  * @param <T> The type of the data in the array.
  * @since 1.0
  */
 public class ArrayIterator<T> implements Iterator<T>, ListIterator<T> {
 	private final T[] data;
 	private final int offset;
 	private final int size;
 	private int next = 0;
 
 	/**
 	 * Create an iterator for the given data. The data is NOT copied therefore
 	 * any change to the data will be reflected by the iterator as well!
 	 * 
 	 * @param data The data to iterate over (MUST not be null).
 	 * @throws ContractViolationException If data is null.
 	 * @since 1.0
 	 */
	@SafeVarargs
 	public ArrayIterator(final T... data) {
 		this.data = ContractCheck.mustNotBeNull(data, "data"); //$NON-NLS-1$
 		this.size = data.length;
 		this.offset = 0;
 	}
 
 	/**
 	 * Create an iterator for the given data. The data is NOT copied therefore
 	 * any change to the data will be reflected by the iterator as well!
 	 * 
 	 * @param data The data to iterate over (MUST not be null).
 	 * @throws ContractViolationException If data is null.
 	 * @since 1.0
 	 */
 	public ArrayIterator(final int offset, final int size, final T[] data) {
 		this.data = ContractCheck.mustNotBeNull(data, "data"); //$NON-NLS-1$
 		this.size = ContractCheck.mustBeInRange(size, 0, this.data.length - offset, "size");
 		this.offset = ContractCheck.mustBeInRange(offset, 0, data.length, "offset");
 	}
 
 	/**
 	 * Create an iterator for the given data. The data is NOT copied therefore
 	 * any change to the data will be reflected by the iterator as well!
 	 * 
 	 * @param data The data to iterate over (MUST not be null).
 	 * @throws ContractViolationException If data is null.
 	 * @since 1.0
 	 */
 	public ArrayIterator(final int initialIndex, final T[] data) {
 		this.data = ContractCheck.mustNotBeNull(data, "data"); //$NON-NLS-1$
 		this.size = data.length;
 		this.offset = 0;
 		this.next = ContractCheck.mustBeInRange(initialIndex, 0, this.size, "initialIndex");
 	}
 
 	/**
 	 * Create an iterator for the given data. The data is NOT copied therefore
 	 * any change to the data will be reflected by the iterator as well!
 	 * 
 	 * @param data The data to iterate over (MUST not be null).
 	 * @throws ContractViolationException If data is null.
 	 * @since 1.0
 	 */
 	public ArrayIterator(final int initialIndex, final int offset, final int size, final T[] data) {
 		this.data = ContractCheck.mustNotBeNull(data, "data"); //$NON-NLS-1$
 		this.size = ContractCheck.mustBeInRange(size, 0, this.data.length - offset, "size");
 		this.offset = ContractCheck.mustBeInRange(offset, 0, data.length, "offset");
 		this.next = ContractCheck.mustBeInRange(initialIndex, 0, this.size, "initialIndex");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		return this.next < this.size;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#next()
 	 */
 	@Override
 	public T next() {
 		if (hasNext()) {
 			return this.data[this.offset + this.next++];
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 	@Override
 	public boolean hasPrevious() {
 		return this.next >= 0;
 	}
 
 	@Override
 	public T previous() {
 		if (hasPrevious()) {
 			return this.data[this.offset + this.next--];
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 	@Override
 	public int nextIndex() {
 		return this.next;
 	}
 
 	@Override
 	public int previousIndex() {
 		return this.next - 1;
 	}
 
 	/**
 	 * Optional operation to remove is not supported by this {@link Iterator}.
 	 */
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void set(final T e) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void add(final T e) {
 		throw new UnsupportedOperationException();
 	}
 }
