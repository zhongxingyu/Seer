 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitrii.Pozdin
  * Date: 8/8/13
  * Time: 3:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class KotlinOperationPrinter extends AbstractOperationPrinter {
    AbstractOperationPrinter operationPrinter=new KotlinOperationPrinter();
     public static AbstractOperationPrinter getInstance(){
         return ourInstance;
     }
 }
