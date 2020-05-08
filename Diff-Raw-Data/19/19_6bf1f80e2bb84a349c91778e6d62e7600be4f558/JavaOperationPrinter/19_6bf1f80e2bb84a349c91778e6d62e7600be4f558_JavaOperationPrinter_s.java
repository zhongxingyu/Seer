 package com.sdc.java;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitrii.Pozdin
  * Date: 8/8/13
  * Time: 2:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class JavaOperationPrinter extends AbstractOperationPrinter {
    protected static AbstractOperationPrinter ourInstance=new JavaOperationPrinter();
 
     public static AbstractOperationPrinter getInstance(){
         return ourInstance;
     }

 }
