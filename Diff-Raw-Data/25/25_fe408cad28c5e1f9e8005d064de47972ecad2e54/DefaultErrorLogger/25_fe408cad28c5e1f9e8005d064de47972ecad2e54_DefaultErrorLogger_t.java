 package org.jrask.compiler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DefaultErrorLogger implements ErrorLogger {
 
     private List<String> errorList = new ArrayList<String>();
 
     @Override
     public List<String> errors() {
        return new ArrayList<String>(errorList);
     }
 
     @Override
     public void log(String msg) {
         errorList.add(msg);
     }
 
 }
