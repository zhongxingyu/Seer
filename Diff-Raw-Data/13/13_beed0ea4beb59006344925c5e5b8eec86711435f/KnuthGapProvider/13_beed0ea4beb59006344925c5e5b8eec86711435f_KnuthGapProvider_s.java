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
 package com.ilyagubarev.algorithms.sorting.utils.gapping;
 
import com.ilyagubarev.algorithms.adt.ItemsStack;
import com.ilyagubarev.algorithms.adt.SimpleLinkedStack;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Gap value provider based on Donald Knuth sequence.
  *
  * @see GapProvider
  *
  * @version 1.01, 12 September 2013
  * @since 12 September 2013
  * @author Ilya Gubarev
  */
 public final class KnuthGapProvider extends GapProvider {
 
     @Override
     protected List<Integer> getSequence(int n) {
         ItemsStack<Integer> reversed = new SimpleLinkedStack<Integer>();
        int gap;
         int k = 1;
         do {
            gap = ((int) Math.pow(3, k) - 1) / 2;
             reversed.push(gap);
         } while (gap <= n / 3);
         List<Integer> result = new LinkedList<Integer>();
         while (!reversed.isEmpty()) {
             result.add(reversed.pop());
         }
         return result;
     }
 }
