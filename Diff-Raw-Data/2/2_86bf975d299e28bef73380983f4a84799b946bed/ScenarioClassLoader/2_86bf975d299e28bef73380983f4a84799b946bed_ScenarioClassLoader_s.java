 package org.jbehave.mojo;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jbehave.scenario.Scenario;
 
 /**
  * Extends URLClassLoader to instantiate Scenarios.
  * 
  * @author Mauro Talevi
  */
 public class ScenarioClassLoader extends URLClassLoader {
 
     public ScenarioClassLoader(List<String> classpathElements) throws MalformedURLException {
         super(classpathURLs(classpathElements), Scenario.class.getClassLoader());
     }
 
     public ScenarioClassLoader(List<String> classpathElements, ClassLoader parent) throws MalformedURLException {
         super(classpathURLs(classpathElements), parent);
     }
 
     /**
      * Loads and instantiates a Scenario class
      * 
      * @param scenarioClassName the name of the Scenario class
      * @return A Scenario instance
      */
     public Scenario newScenario(String scenarioClassName) {
         try {
             Scenario scenario = (Scenario) loadClass(scenarioClassName).getConstructor(ClassLoader.class).newInstance(
                     this);
             Thread.currentThread().setContextClassLoader(this);
             return scenario;
         } catch (ClassCastException e) {
             String message = "The scenario '" + scenarioClassName + "' must be of type '" + Scenario.class.getName() +"'";
             throw new RuntimeException(message, e);
         } catch (Exception e) {
             String message = "The Scenario '" + scenarioClassName
                    + "' could not be instantiated with classpath element:s " + asShortPaths(getURLs());
             throw new RuntimeException(message, e);
         }
     }
 
     private List<String> asShortPaths(URL[] urls) {
         List<String> names = new ArrayList<String>();
         for (URL url : urls) {
             String path = url.getPath();
             if (isJar(path)) {
                 names.add(shortPath(path));
             } else {
                 names.add(path);
             }
         }
         return names;
     }
 
     private static String shortPath(String path) {
         return path.substring(path.lastIndexOf("/") + 1);
     }
 
     private static boolean isJar(String path) {
         return path.endsWith(".jar");
     }
 
     private static URL[] classpathURLs(List<String> elements) throws MalformedURLException {
         List<URL> urls = new ArrayList<URL>();
         if (elements != null) {
             for (String element : elements) {
                 urls.add(new File(element).toURL());
             }
         }
         return urls.toArray(new URL[urls.size()]);
     }
 
 }
