 package com.mikea.concrete;
 
 public interface PDeque<T> extends PQueue<T> {
   PDeque<T> pushFront(T value);
 
   PDeque<T> popBack();
 
   T peekBack();
 
   @Override
   PDeque<T> pushBack(T value);
 
   @Override
   PDeque<T> popFront();

  @Override
  PDeque<T> clear();
 }
