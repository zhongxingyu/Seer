 package com.jayway.wordgraph;
 
public interface Function2<A, T> {
    public A apply(A accum, T next);
 }
