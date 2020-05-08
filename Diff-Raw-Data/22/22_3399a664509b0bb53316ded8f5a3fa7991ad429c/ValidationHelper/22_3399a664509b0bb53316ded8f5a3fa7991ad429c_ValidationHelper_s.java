 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
   EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 import java.io.File;
 
 /**
 * Helper class to validate the names like directory, package, property file name etc..
 *
 * @author Ravi Polampelli
 *
 */
 
 public class ValidationHelper {
 
     public static boolean isDirectoryName(String name) {
         boolean returnValue = false;
         File file;
 
         if (name != null) {
             file = new File(name);
             returnValue = file.isDirectory();
         }
 
         return returnValue;
     }
 
     public static boolean isPropertyName(String name) {
         boolean returnValue = true;
         // TODO until we find a save way to determine if name is a property, we return true
         return returnValue;
     }
 
     public static boolean isFileName(String name) {
         boolean returnValue = false;
         File file;
 
         if (name != null) {
             file = new File(name);
             returnValue = file.isFile();
         }
 
         return returnValue;
     }
 
     public static boolean isClassName(String name) {
         boolean returnValue = false;
         Class clazz;
         try {
             clazz = Class.forName(name);
             returnValue = !clazz.isInterface();
         } catch (Exception e) {
             // this is not a class name
         }
 
         return returnValue;
     }
 
     public static boolean isPackageName(String name) {
         boolean returnValue = false;
 
         if ((name != null) && (name.length()>0) && (Character.isJavaIdentifierStart(name.charAt(0)))) {
             returnValue = true;
             for (int i=1; i<name.length(); i++) {
                 if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                     returnValue = false;
                 }
             }
         }
 
         return returnValue;
     }
}
 
