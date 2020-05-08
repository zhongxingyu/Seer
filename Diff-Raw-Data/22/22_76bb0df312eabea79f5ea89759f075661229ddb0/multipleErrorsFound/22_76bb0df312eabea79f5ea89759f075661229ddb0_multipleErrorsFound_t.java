 package edu.kit.checkstyle.checks.discouragedmethodcallchecktest;
 
 public class multipleErrorsFound {
   void test() {
    out(); // no class ident before method ident
     System.out.println();
     System.in.getClass();
     System.exit(0);
   }
 }
