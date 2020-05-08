 package org.kndl.util.classloader;
 
 import org.apache.log4j.Logger;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Loads a class from any selection of remote jars.
  *
  *
  */
 public class KndlClassLoader extends ClassLoader {
 
     private static final Logger     logger = Logger.getLogger(KndlClassLoader.class);
 
     private List<URLClassLoader> jarLocations;
 
     public KndlClassLoader() {
         this.jarLocations = new LinkedList<URLClassLoader>();
     }
 
     public void addLocation(URL url) {
         URLClassLoader ucl = new URLClassLoader(new URL[] {url},this.getClass().getClassLoader());
         jarLocations.add(ucl);
     }
 
     public Class load(String className) {
         Class c = null;
         for(URLClassLoader loader : jarLocations) {
             try {
                 c = Class.forName(className,true,loader);
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
         }
         return c;
 
     }
 
     public Class get(String className) {
         try {
             return Class.forName(className,true,this);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static void main(String args[]) {
         KndlClassLoader ncl = new KndlClassLoader();
         try {
             ncl.addLocation(new URL("http://pants.spacerobots.org/test.jar"));
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
         ncl.load("Test");
         Class c = ncl.get("Test");
         System.out.println(c.getSimpleName());
     }
 }
