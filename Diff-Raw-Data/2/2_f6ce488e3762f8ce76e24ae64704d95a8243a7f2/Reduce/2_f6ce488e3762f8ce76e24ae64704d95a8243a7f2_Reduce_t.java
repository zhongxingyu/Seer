 /*
  * Copyright 2011 Vasily Shiyan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package see.util;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 
 /**
  * Utility class with fold/reduce operations.
  */
 public abstract class Reduce {
     private Reduce() {}
 
     /**
      * Fold a sequence with specified function and initial value.
      *
      * @param initial initial value
      * @param values sequence to fold
      * @param func folding function
      * @param <A> input type
      * @param <R> result type
      * @return fold result
      */
     public static <A, R> R fold(R initial, Iterable<? extends A> values, FoldFunction<? super A, R> func) {
         R result = initial;
 
         for (A arg : values) {
             result = func.apply(result, arg);
         }
 
         return result;
     }
 
     /**
      * Fold a sequence without an initial value.
      *
      * @param values sequence to fold
      * @param func reducing function
      * @param <T> input and result type
      * @return reduce result
      */
     public static <T> T reduce(Iterable<? extends T> values, FoldFunction<? super T, T> func) {
        Preconditions.checkArgument(!Iterables.isEmpty(values), "Cannot reduce empty list");
 
         return fold(Iterables.getFirst(values, null), Iterables.skip(values, 1), func);
     }
 
     public static interface FoldFunction<A, R> {
         R apply(R prev, A arg);
     }
 }
