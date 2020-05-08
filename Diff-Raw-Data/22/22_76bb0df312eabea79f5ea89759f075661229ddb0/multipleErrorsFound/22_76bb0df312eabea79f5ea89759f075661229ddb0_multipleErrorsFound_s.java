 package edu.kit.checkstyle.checks.discouragedmethodcallchecktest;
 

 public class multipleErrorsFound {
   void test() {
     System.out.println();
     System.in.getClass();
     System.exit(0);
   }
 }
