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
 package com.ilyagubarev.algorithms.adt.arrays;
 
 import java.util.Iterator;
 
import com.ilyagubarev.algorithms.adt.iterators.ArrayIterator;
 import com.ilyagubarev.algorithms.adt.utils.Counter;
 
 /**
  * Array model for sorting / searching methods analysis.
  *
  * @see Iterable
  *
  * @version 1.04, 23 September 2013
  * @since 12 September 2013
  * @author Ilya Gubarev
  */
 public final class ArrayModel<T> implements Iterable<T> {
 
     private final T[] _data;
     private final Counter _reads;
     private final Counter _writes;
 
     ArrayModel(int size, Counter reads, Counter writes) {
         _data = (T[]) new Object[size];
         _reads = reads;
         _writes = writes;
     }
 
     /**
      * Gets size of the array.
      *
      * @return array size.
      */
     public int getSize() {
         return _data.length;
     }
 
     /**
      * Gets an item at specified index.
      *
      * @param index item index.
      * @return array item.
      * @throws RuntimeException if specified index is illegal.
      */
     public T read(int index) {
         T result = _data[index];
         _reads.increment();
         return result;
     }
 
     /**
      * Sets an item to the specified index.
      *
      * @param index item index.
      * @param item an item to be set.
      * @throws RuntimeException if specified index is illegal.
      */
     public void write(int index, T item) {
         _data[index] = item;
         _writes.increment();
     }
 
     @Override
     public Iterator<T> iterator() {
         return new ArrayIterator<T>(this);
     }
 
     @Override
     public String toString() {
         StringBuilder content = new StringBuilder();
         for (T item : _data) {
             content.append(String.format("%s, ", item));
         }
         return String.format("[array : {%s}]", content);
     }
 }
