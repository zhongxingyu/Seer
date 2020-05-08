 package com.tacitknowledge.noexcuses;
 
import java.lang.reflect.InvocationTargetException;

 /**
  *  Originally created: Oct 31, 2006
  */
 public interface BeanTester {
    public void performTest(Object testee) ;
    public void performSilentTest(Object testee);
 }
