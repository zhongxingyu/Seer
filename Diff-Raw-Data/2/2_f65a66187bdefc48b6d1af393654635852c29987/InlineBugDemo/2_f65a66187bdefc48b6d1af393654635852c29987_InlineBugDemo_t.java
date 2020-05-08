 /**
  * 
  */
 package com.tsvinev.eclipse.test;
 
 /**
  * @author oleg
  * 
  */
 public class InlineBugDemo {
 
     String parameter;
 
     /**
     * Inline variable refactoring bug demo:
      * 1. Select variable cca
      * 2. Press Option+Command+I for "inline variable"
      * 3. The prompt dialog will show "Inline 2 occurences of local variable cca?"
      * 4. Hit "Enter" to proceed. Result will create two *instances* of the class CreateConnectionAsync as following:
      * 
      * new CreateConnectionAsync().execute(new String[] {parameter});
      * Connection<String> connection = new CreateConnectionAsync().get();
      * 
      */
     public void refactorMe() {
         CreateConnectionAsync cca = new CreateConnectionAsync();
         cca.execute(new String[] {parameter});
         Connection<String> connection = cca.get();
     }
 
 }
