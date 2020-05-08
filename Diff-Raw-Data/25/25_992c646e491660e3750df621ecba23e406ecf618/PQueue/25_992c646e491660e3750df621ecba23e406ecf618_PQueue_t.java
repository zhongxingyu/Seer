 package com.mikea.concrete;
 
 public interface PQueue<T> extends PCollection<T> {
   PQueue<T> pushBack(T value);
   PQueue<T> popFront();
   T peekFront();

  @Override
  PQueue<T> clear();
 }
