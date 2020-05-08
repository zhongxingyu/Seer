 /*
 Copyright 2011 Matt DeVore
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package com.github.matvore.andradion2.data;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Lists {
   private Lists() {
     throw new UnsupportedOperationException("static-only class");
   }
 
   public static <E> List<E> newArrayList() {
     return new ArrayList<E>();
   }
 
   public static <E> List<E> newArrayListWithCapacity(int capacity) {
     return new ArrayList<E>(capacity);
   }
 
  public static <E> List<E> deepCopyOf(List<E> source, Copier<? super E> copier) {
     List<E> result = newArrayListWithCapacity(source.size());
     for (E sourceItem : source) {
       result.add(copier.copy(sourceItem));
     }
     return result;
   }
 
   public static <E> List<E> listPlusOneElement(
       final List<E> original, final E add) {
     return new AbstractList<E>() {
       @Override
       public int size() {
         return original.size() + 1;
       }
 
       @Override
       public E get(int index) {
         if (index == original.size()) {
           return add;
         } else {
           return original.get(index);
         }
       }
     };
   }
 }
