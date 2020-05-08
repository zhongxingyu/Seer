 package org.codehaus.mojo.gwt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class JavaCommand {
 
    public static void main(String[] args) throws IOException, ClassNotFoundException, SecurityException, NoSuchMethodException, InterruptedException {
         final String classToExecMethod = "main";
 
         List list = new ArrayList(Arrays.asList(args));
 
         if (list.size() < 2) {
             System.err.println("not enough arguments");
             System.exit(1);
         }
 
         final String fileName = (String)list.remove(0);
         final String className = (String)list.remove(0);
 
         ClassLoader cl;
         
         final File file = new File(fileName);
         if (!file.exists()) {
             System.err.println("warning: " + fileName + " does not exist!");
             cl = ClassLoader.getSystemClassLoader();
         } else {
             cl = loadClassesFromFile(file);
         }

        final String[] classToExecArgs = (String[]) list.toArray(new String[0]);

        System.out.println("GWT: executing " + className + ".main("+ list + ")");

         final Class[] classes = new Class[] { classToExecArgs.getClass() };
         final Object[] methodArgs = new Object[] { classToExecArgs };
         Class c = cl.loadClass(className);
         final Method method = c.getMethod(classToExecMethod, classes);
 
         Runnable execer = new Runnable() {
             public void run() {
                 try {
                     method.invoke(null, methodArgs);
                 } catch (IllegalArgumentException e) {
                     e.printStackTrace();
                     System.exit(1);
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                     System.exit(1);
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                     System.exit(1);
                 }
             }
         };
 
         Thread bootstrapper = new Thread(execer, "Main");
         bootstrapper.setContextClassLoader(cl);
         bootstrapper.start();
        bootstrapper.join();
     }
 
     /**
      * Create a ClassLoader with the classpaths found in file.
      * 
      * @param file
      *            A file containing a list of classpath entries
      * @throws IOException 
      * @returns A new ClassLoader containing the found classpaths
      */
     public static ClassLoader loadClassesFromFile(File file)
             throws IOException {
         List urls = new LinkedList();
 
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         while ((line = reader.readLine()) != null) {
             line.trim();
             if (line.startsWith("#")) {
                 continue;
             }
             File classPathFile = new File(line);
             if (classPathFile.exists()) {
                 urls.add(classPathFile.toURL());
             }
         }
 
         URL[] urlsArray = (URL[])urls.toArray(new URL[0]);
         return URLClassLoader.newInstance(urlsArray);
     }
 
 
 }
