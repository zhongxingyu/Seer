 package com.sdc.js;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitrii.Pozdin
  * Date: 8/8/13
  * Time: 3:30 PM
  * To change this template use File | Settings | File Templates.
  */
 public class JSOperationPrinter extends AbstractOperationPrinter {
    protected static AbstractOperationPrinter ourInstance = new JSOperationPrinter();
 
     public static AbstractOperationPrinter getInstance(){
         return ourInstance;
     }
 }
