 /*
  * Copyright 2013 Ilya Gubarev.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ilyagubarev.algorithms.adt.collections.queues;
 
 import java.util.Comparator;
 import java.util.Iterator;
 
 import com.ilyagubarev.algorithms.adt.arrays.ArrayModel;
 import com.ilyagubarev.algorithms.adt.arrays.ArrayModelFactory;
 import com.ilyagubarev.algorithms.adt.collections.QueueModel;
 import com.ilyagubarev.algorithms.utils.CommonHelper;
 
 /**
  * Priority QueueModel implementation based on ArrayModel.
  *
  * @see QueueModel
  *
 * @version 1.02, 30 September 2013
  * @since 22 September 2013
  * @author Ilya Gubarev
  */
 public final class PriorityQueueModel<T> implements QueueModel<T> {
 
     public static final int DEFAULT_INITIAL_CAPACITY = 16;
 
     private final int _initialCapacity;
     private final Comparator<T> _comparator;
     private final ArrayModelFactory _factory;
 
     private int _size;
     private ArrayModel<T> _items;
 
     /**
      * Creates a new instance of PriorityQueueModel.
      *
      * @param initialCapacity queue initial capacity (null for default).
      * @param comparator item comparator (can be null if items are Comparable).
      * @param factory a factory of array models.
      * @throws IllegalArgumentException if initial capacity is negative.
      *
      * @see ArrayModelFactory
      * @see Comparator
      */
     public PriorityQueueModel(Integer initialCapacity, Comparator<T> comparator,
             ArrayModelFactory factory) {
         if (initialCapacity < 0) {
             throw new IllegalArgumentException("initial capacity is negative");
         }
         if (factory == null) {
             throw new NullPointerException("array model factory is null");
         }
         if (initialCapacity == null) {
             _initialCapacity = DEFAULT_INITIAL_CAPACITY + 1;
         } else {
             _initialCapacity = initialCapacity + 1;
         }
         _comparator = comparator;
         _factory = factory;
     }
 
     @Override
     public T dequeue() {
         throwExceptionIfEmpty();
         T result = _items.read(1);
        _items.write(1, _items.read(_size));
        _items.write(_size--, null);
         sink(1);
         return result;
     }
 
     @Override
     public void enqueue(T item) {
         _size++;
         assureRequiredSize();
         _items.write(_size, item);
         surface(_size);
     }
 
     @Override
     public T poll() {
         throwExceptionIfEmpty();
         return _items.read(1);
     }
 
     @Override
     public int getSize() {
         return _size;
     }
 
     @Override
     public boolean isEmpty() {
         return _size == 0;
     }
 
     @Override
     public Iterator<T> iterator() {
         throw new UnsupportedOperationException();
     }
 
     private void assureRequiredSize() {
         if (_items == null) {
             _items = _factory.create(_initialCapacity);
         }
         int size = _items.getSize() - 1;
         if (_size > size) {
             ArrayModel<T> items = _factory.create(size * 2 + 1);
             for (int i = 0; i < _items.getSize(); i++) {
                 items.write(i, _items.read(i));
             }
             _items = items;
         }
     }
 
     private void sink(int index) {
         while (index * 2 <= _size) {
             int child = index * 2;
             T item = _items.read(index);
             T cItem = _items.read(child);
             if (child < _size) {
                 T rcItem = _items.read(child + 1);
                 if (CommonHelper.compare(_comparator, cItem, rcItem) < 0) {
                     cItem = rcItem;
                     child++;
                 }
             }
             if (CommonHelper.compare(_comparator, item, cItem) < 0) {
                 _items.write(index, cItem);
                 _items.write(child, item);
             } else {
                 break;
             }
         }
     }
 
     private void surface(int index) {
         while (index > 1) {
             int parent = index / 2;
             T item = _items.read(index);
             T parentItem = _items.read(parent);
             if (CommonHelper.compare(_comparator, parentItem, item) < 0) {
                 _items.write(parent, item);
                 _items.write(index, parentItem);
                 index = parent;
             } else {
                 break;
             }
         }
     }
 
     private void throwExceptionIfEmpty() {
         if (isEmpty()) {
             throw new IllegalStateException("queue is empty");
         }
     }
 }
