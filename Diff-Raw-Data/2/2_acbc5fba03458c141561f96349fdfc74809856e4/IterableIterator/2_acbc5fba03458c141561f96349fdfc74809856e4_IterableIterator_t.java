 /*
  * Copyright 2012 - Six Dimensions
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.sixdimensions.wcm.cq.cqex.util;
 
 import java.util.Iterator;
 
 /**
  * An iterator which is also iterable, seems kind of obvious, but Java can be
  * weird.
  * 
  * @author dklco
  * 
  * @param <E>
  *            the type contained in the iterator, can be any Java Object
  */
 public class IterableIterator<E> implements Iterator<E>, Iterable<E> {
 
 	/**
 	 * The iterator being wrapped by this IterableIterator.
 	 */
 	private final Iterator<E> iterator;
 
 	/**
 	 * Construct a new Iterable Iterator from the specified iterator.
 	 * 
	 * @param iterator
 	 *            the iterator to wrap
 	 */
 	public IterableIterator(final Iterator<E> iterator) {
 		this.iterator = iterator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		return this.iterator.hasNext();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Iterable#iterator()
 	 */
 	@Override
 	public Iterator<E> iterator() {
 		return this.iterator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#next()
 	 */
 	@Override
 	public E next() {
 		return this.iterator.next();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Iterator#remove()
 	 */
 	@Override
 	public void remove() {
 		this.iterator.remove();
 	}
 
 }
