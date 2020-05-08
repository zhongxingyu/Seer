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
 package com.ilyagubarev.algorithms.sorting.methods;
 
 import java.util.Comparator;
 
 import com.ilyagubarev.algorithms.adt.arrays.ArrayModel;
 import com.ilyagubarev.algorithms.adt.arrays.ArrayModelFactory;
 import com.ilyagubarev.algorithms.adt.nodes.NodeModelFactory;
 import com.ilyagubarev.algorithms.adt.utils.Registry;
 
 /**
  * Sorting algorithm implementation based on T. Hoare "quicksort" method.
  *
  * @see AbstractSorter
  *
  * @version 1.02, 20 September 2013
  * @since 16 September 2013
  * @author Ilya Gubarev
  */
 public final class QuickSorter extends AbstractSorter {
 
     @Override
     public String getInfo() {
         return "Standard \"quicksort\" method";
     }
 
     @Override
     public <T> void sort(ArrayModel<T> target, Comparator<T> comparator,
             ArrayModelFactory arrayFactory, NodeModelFactory nodeFactory,
             Registry recursions) {
         sort(target, comparator, 0, target.getSize() - 1, recursions);
     }
 
     private <T> void sort(ArrayModel<T> target, Comparator<T> comparator,
             int first, int last, Registry recs) {
         if (last <= first) {
             return;
         }
        int pivot = separate(target, comparator, 0, target.getSize() - 1);
         registerRecursiveCall(recs);
         sort(target, comparator, first, pivot - 1, recs);
         registerRecursiveReturn(recs);
         registerRecursiveCall(recs);
         sort(target, comparator, pivot + 1, last, recs);
         registerRecursiveReturn(recs);
     }
 
     private <T> int separate(ArrayModel<T> target, Comparator<T> comparator,
             int first, int last) {
         int i = first;
         int j = last + 1;
         int pivot = first;
         while (true) {
             while (less(target, comparator, ++i, pivot)) {
                 if (i == last) {
                     break;
                 }
             }
             while (less(target, comparator, pivot, --j)) {
                 if (j == first) {
                     break;
                 }
             }
             if (i >= j) {
                 break;
             }
             swap(target, i, j);
         }
         swap(target, first, j);
         return j;
     }
 }
