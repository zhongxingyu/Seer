 package org.miloframework.tests;
 
 import org.miloframework.WebappClassLoader;
import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class ClassLoaderTest {
     @Test
     public void basicServlet() throws ClassNotFoundException {
        final ClassLoader classLoader = getClass().getClassLoader();
        WebappClassLoader cl = new WebappClassLoader("../basic/target/basic/", classLoader);
         final Class<?> aClass = cl.loadClass("org.miloframework.BasicServlet");
        Assert.assertNotEquals(aClass.getClassLoader(), classLoader);
     }
 }
