 
 package com.myapp.p;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import javax.swing.JApplet;
 import javax.swing.SwingUtilities;
 
 public class MyApplet
     extends JApplet
 {
     @Override
     public void init()
     {
         try
         {
             SwingUtilities.invokeAndWait(new Runnable()
             {
                 @Override
                 public void run()
                 {
                     _init();
                     return;
                 }
             });
         }
         catch(Exception e)
         {
             System.err.println("Exception in _init");
         }
         return;
     }
 
     public void _init()
     {
         try
         {
             System.err.println("Attempting to run class");
             Class<?> c = Class.forName("com.myapp.q.Hello");
             System.err.println("Got class object "+c);
             Method m = c.getMethod("printme");
             System.err.println("Got method object "+m);
             m.invoke(null);
             System.err.println("Reflective method invocation successful");
         }
         catch(    NoSuchMethodException e)
         {
             e.printStackTrace(System.err);
         }
         catch(        SecurityException e)
         {
             e.printStackTrace(System.err);
         }
         catch(   ClassNotFoundException e)
         {
             e.printStackTrace(System.err);
         }
         catch(   IllegalAccessException e)
         {
             e.printStackTrace(System.err);
         }
         catch(InvocationTargetException e)
         {
             e.printStackTrace(System.err);
         }
         return;
     }
 }
