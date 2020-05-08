 package com.ninja_squad.codestory.scalaskel;
 
 public enum Unite {
 
     foo(1),
     bar(7),
     qix(11),
     baz(21);
 
    private int value;
 
     private Unite(int value) {
         this.value = value;
     }
 
     public int getValue() {
         return value;
     }
 }
