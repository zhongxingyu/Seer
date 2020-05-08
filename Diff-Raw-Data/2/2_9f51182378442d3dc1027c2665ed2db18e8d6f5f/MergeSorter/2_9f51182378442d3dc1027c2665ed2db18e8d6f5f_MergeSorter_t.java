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
 
 import com.ilyagubarev.algorithms.adt.ItemArray;
 import com.ilyagubarev.algorithms.adt.ItemArrayFactory;
 import com.ilyagubarev.algorithms.adt.ItemHelper;
 import com.ilyagubarev.algorithms.adt.ItemNodeFactory;
 
 /**
  * "Divide & merge" method sorting algorithm implementation.
  *
  * @see AbstractSorting
  *
  * @version 1.02, 13 September 2013
  * @since 11 September 2013
  * @author Ilya Gubarev
  */
 public final class MergeSorter extends AbstractSorter {
 
     @Override
     public void sort(ItemArray target, ItemHelper helper,
             ItemArrayFactory arrayFactory, ItemNodeFactory nodeFactory) {
         ItemArray aux = arrayFactory.create(target.getSize());
        sort(target, 0, target.getSize() - 1, aux, helper);
     }
 
     private void merge(ItemArray target, int leftFirst, int leftLast,
             int rightLast, ItemArray aux, ItemHelper helper) {
         for (int i = leftFirst; i <= rightLast; ++i) {
             aux.write(i, target.read(i));
         }
         int left = leftFirst;
         int right = leftLast + 1;
         for (int i = leftFirst; i <= rightLast; ++i) {
             if (left > leftLast) {
                 target.write(i, aux.read(right++));
             } else if (right > rightLast) {
                 target.write(i, aux.read(left++));
             } else if (less(aux, right, left, helper)) {
                 target.write(i, aux.read(right++));
             } else {
                 target.write(i, aux.read(left++));
             }
         }
     }
 
     private void sort(ItemArray target, int leftFirst, int rightLast,
             ItemArray aux, ItemHelper helper) {
         if (rightLast <= leftFirst) {
             return;
         }
         int leftLast = leftFirst + (rightLast - leftFirst) / 2;
         sort(target, leftFirst, leftLast, aux, helper);
         sort(target, leftLast + 1, rightLast, aux, helper);
         merge(target, leftFirst, leftLast, rightLast, aux, helper);
     }
 }
