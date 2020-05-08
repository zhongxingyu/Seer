 package com.jayway.wordgraph;
 
 public class Collections3<A, T> {
    public static <A, T> A reduce(Function2<A, T, A> f, A initial, Iterable<T> coll) {
         // @BEGIN_VERSION 1
         A value = initial;
         for (T next : coll) {
             value = f.apply(value, next);
         }
         return value;
         // @END_VERSION 1
     }
 }
