 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.jar.*;
 
 class Parameter {
     String jarFileName;
     String classNamePrefix;
 
    Parameter(String[] args) throws Exception {
         if (args.length > 0) {
            jarFileName = args[0];
         } else {
            throw new Exception("need jar file name.");
         }
 
         if (args.length > 1) {
            classNamePrefix = args[1];
         } else {
            classNamePrefix = "";
         }
    }

    private Parameter() {}
}

public class JavaApiGenerator {
    public static void main(String[] args) throws Exception {
        ProcessJarFile(new Parameter(args));
     }
 
     /**
      * Process all class in a jarFile.
      * @param parameter: has the file name of jar and the prefix of class name which should process.
      */
     private static void ProcessJarFile(Parameter parameter) throws IOException {
         try (JarFile file = new JarFile(parameter.jarFileName)) {
             Enumeration<JarEntry> enumerator = file.entries();
             while (enumerator.hasMoreElements()) {
                 String name = enumerator.nextElement().getName().replace('/', '.');
                 final String classSuffix = ".class";
                 if (!name.endsWith(classSuffix)) {
                     continue;
                 } else {
                     name = name.substring(0, name.length() - classSuffix.length());
                 }
                 if (!name.startsWith(parameter.classNamePrefix)) {
                     continue;
                 }
                 ProcessOneClass(name);
             }
         } 
     }
 
     /**
      * Process one class.
      * @param className: class name.
      * @param callback: process class callback.
      */
     private static void ProcessOneClass(String className) {
         try {
             Class klass = Class.forName(className);
             if (klass == null) {
                 return;
             }
             for (Method method : klass.getMethods()) {
                 System.out.println(klass.getName()
                     + " " + method.getDeclaringClass().getName()
                     + " " + method);
             }
         } catch (ClassNotFoundException e) {
             System.out.println("meet ClassNotFoundException: " + e);
         } catch (Exception e) {
             System.out.println("meet Exception: " + e);
         } catch (Error e) {
             System.out.println("meet Error: " + e);
         }
     }
 }
