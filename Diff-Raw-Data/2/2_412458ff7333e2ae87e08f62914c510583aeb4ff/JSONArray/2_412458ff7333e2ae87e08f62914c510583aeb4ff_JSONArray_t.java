 /*
  * Copyright © 2011 Jason J.A. Stephenson
  * 
  * This file is part of sigio.jar.
  * 
  * sigio.jar is free software: you can redistribute it and/or modify it
  * under the terms of the Lesser GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * sigio.jar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  * 
  * You should have received a copy of the Lesser GNU General Public License
  * along with sigio.jar.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.sigio.json;
 
 import java.util.ArrayList;
 import java.util.Collection;
 /**
  * Class to implement a JSON array as defined in RFC4627. We inherit
  * from ArrayList<Object> to take advantage of existing functionality.
  */
 public class JSONArray extends ArrayList<Object> {
 	/**
 	 * Construct a default JSONArray.
 	 */
 	public JSONArray() {
 		super();
 	}
 	/**
 	 * Construct a JSONArray and fill it with object from a collection.
 	 *
 	 * @param collection the collection of objects to fill the array
 	 * @throws ClassCastException if one of the array members is not a
 	 * suitable JSON object
 	 */
 	public JSONArray(Collection<Object> collection) {
 		this();
 		this.addAll(collection);
 	}
 	/**
 	 * Construct a JSONArray with an initial size
 	 *
 	 * @param size the initial capacity of the array
 	 */
 	public JSONArray(int size) {
 		super(size);
 	}
 	/*
 	 * Helper method to check if the parameter object's class is a
 	 * suitable JSON object.
 	 */
 	private void checkInstance(Object o) {
 		if (!JSONValue.isInstance(o))
 			throw new ClassCastException(o.getClass().getName() + " is not a valid JSON value");
 	}
 	/**
 	 * Appends the specified element to the end of this list. 
 	 *
 	 * @param o element to be appended to this list
 	 * @return {@code true} (as specified by {@code Collection.add(E)})
 	 * @throws ClassCastException if the passed in Object is not a
 	 * suitable JSON object
 	 */
 	@Override
 	public boolean add(Object o) throws ClassCastException {
 		this.checkInstance(o);
 		return super.add(o);
 	}
 	/**
 	 * Inserts the specified element at the specified position in this
 	 * list. Shifts the element currently at that position (if any)
 	 * and any subsequent elements to the right (adds one to their
 	 * indices).
 	 *
 	 * @param idx element to be inserted
 	 * @param o element to be inserted
 	 * @throws IndexOutOfBoundsException if the index is out of range
 	 * {@code (index < 0 || index > size())}
 	 * @throws ClassCastException if the passed in Object is not a
 	 * suitable JSON object
 	 */
 	@Override
 	public void add(int idx, Object o) throws IndexOutOfBoundsException, ClassCastException {
 		this.checkInstance(o);
 		super.add(idx, o);
 	}
 
 	/**
 	 * Appends all of the elements in the specified collection to the
 	 * end of this list, in the order that they are returned by the
 	 * specified collection's Iterator. The behavior of this operation
 	 * is undefined if the specified collection is modified while the
 	 * operation is in progress. (This implies that the behavior of
 	 * this call is undefined if the specified collection is this
 	 * list, and this list is nonempty.)
 	 * 
 	 * @param collection collection containing elements to be added to
 	 * this list
 	 * @return {@code true} if the list changed as a result of the call
 	 * @throws NullPointerException If the collection is {@code null}
 	 * @throws ClassCastException if the passed in Object is not a
 	 * suitable JSON object
 	 */
 	@Override
 	public boolean addAll(Collection<? extends Object> collection) throws ClassCastException {
 		for (Object o : collection)
 			this.checkInstance(o);
 		return super.addAll(collection);
 	}
 
 	/**
 	 * Inserts all of the elements in the specified collection into
 	 * this list, starting at the specified position. Shifts the
 	 * element currently at that position (if any) and any subsequent
 	 * elements to the right (increases their indices). The new
 	 * elements will appear in the list in the order that they are
 	 * returned by the specified collection's iterator.
 	 *
 	 * @param idx index at which to insert the first element from the
 	 * specified collection
 	 * @param collection collection containing elements to be added to
 	 * this list
 	 * @return {@code true} if the list changed as a result of the call
 	 * @throws IndexOutOfBoundsException if the index is out of range
 	 * {@code (index < 0 || index > size())}
 	 * @throws NullPointerException If the collection is {@code null}
 	 * @throws ClassCastException if any of the members of the
 	 * collection is not a suitable JSON object
 	 */
 	@Override
 	public boolean addAll(int idx, Collection<? extends Object> collection) throws IndexOutOfBoundsException, ClassCastException {
 		for (Object o : collection)
 			this.checkInstance(o);
 		return super.addAll(idx, collection);
 	}
 
 	/**
 	 * Replaces the element at the specified position in this list
 	 * with the specified element.
 	 *
 	 * @param idx index of the element to replace
 	 * @param o element to be stored at the specified position
 	 * @return the element previously at the specified position
 	 * @throws IndexOutOfBoundsException if the index is out of range
 	 * {@code (index < 0 || index >= size())}
 	 * @throws ClassCastException if any members of the collection is
 	 * not a suitable JSON object
 	 */
 	@Override
	public Object set(int idx, Object o) throws IndexOutOfBoundsException, ClassCastException {
 		this.checkInstance(o);
 		return super.set(idx, o);
 	}
 
 }
