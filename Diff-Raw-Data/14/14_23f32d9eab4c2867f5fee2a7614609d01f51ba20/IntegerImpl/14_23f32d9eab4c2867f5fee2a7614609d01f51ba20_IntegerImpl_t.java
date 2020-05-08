 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.mycompany.testproject.iterativeTests;
 
 import fi.lolcatz.profiledata.ProfileData;
 import fi.lolcatz.profiler.Benchmarkable;
 import fi.lolcatz.profiler.Util;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class IntegerImpl implements Benchmarkable<Integer>{
     private String className;
     private String methodName;
     
     @Override
     public Integer getInput(int size) {
         return new Integer(size);
     }
 
     @Override
     public int getMaxTime(Integer input, int size) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setMethodName(String name) {
         this.methodName = name;
     }
 
     @Override
     public void setClassName(String name) {
         this.className = name;
     }
 
     public String getClassName() {
         return className;
     }
 
     public String getMethodName() {
         return methodName;
     }
 
     @Override
     public long run(Integer input) {
         ProfileData.resetCounters();
         try {
             Class<?> c = Class.forName(className);
            Method m = c.getDeclaredMethod(methodName, Integer.TYPE);
            m.setAccessible(true);
            m.invoke(null, input.intValue());
         } catch (Exception x) {
 	    Logger.getLogger(IntegerImpl.class.getName()).log(Level.SEVERE, null, x);
 	}  
         return Util.getTotalCost();       
     }
 
 }
