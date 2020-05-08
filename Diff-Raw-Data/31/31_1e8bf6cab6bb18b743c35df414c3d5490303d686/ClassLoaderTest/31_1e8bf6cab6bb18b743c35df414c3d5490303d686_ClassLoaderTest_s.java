 package org.miloframework.tests;
 
 import org.miloframework.WebappClassLoader;
 import org.testng.annotations.Test;
 
 public class ClassLoaderTest {
     @Test
     public void basicServlet() throws ClassNotFoundException {
        WebappClassLoader cl = new WebappClassLoader("test-parent/basic/target/basic/", getClass().getClassLoader());
         final Class<?> aClass = cl.loadClass("org.miloframework.BasicServlet");
     }
 }
