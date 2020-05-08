 package com.jayway.wordgraph;
 
public interface Function2<A1, A2, R> {
    public R apply(A1 accum, A2 next);
 }
