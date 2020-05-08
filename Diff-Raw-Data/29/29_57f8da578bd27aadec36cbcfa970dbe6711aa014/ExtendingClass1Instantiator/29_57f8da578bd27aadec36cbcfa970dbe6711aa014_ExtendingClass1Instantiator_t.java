 
 package test.lib;
 
 import lib.ClassA;
 import lib.ExtendingClass1;
 
 public class ExtendingClass1Instantiator {
 
 
     public ExtendingClass1 createExtendingClass1() {
         return new ExtendingClass1();
     }
 
      void ancestors(ExtendingClass1 a) {
         ClassA b = a;
     }
 
 }
