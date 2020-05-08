 package org.sherman.kproblem.core;
 
 public class Value<T> {
     T value;
     
     public Value(T value) {
         this.value = value;
     }
     
    T getValue() {
         return value;
     }
 }
