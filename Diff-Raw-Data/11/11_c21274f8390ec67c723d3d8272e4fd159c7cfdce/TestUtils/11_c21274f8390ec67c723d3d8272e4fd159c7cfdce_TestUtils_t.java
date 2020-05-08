 // 
 //  TestUtils.java
 //  tests
 //  
 //  Created by Philip Kuryloski on 2011-03-10.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave;
 
 import android.app.Instrumentation;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 //import android.test.MoreAsserts;
 import java.io.*;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import junit.framework.Assert;
 
 public class TestUtils {
     
     /**
      * Joins an array of strings, inserting a separator
      */
     public static String unsplit(String[] components, String separator) {
         String result = "";
         for (int i=0; i<components.length; i++) {
             result += components[i] + (i<components.length-1 ? separator : "");
         }
         return result;
     }
     
     /**
      * copyTestAssetToInternal
      *
      * used for copying fixtures to app storage to simulate downloaded data
      * only tested with two component destination paths
      */
     public static File copyTestAssetToInternal(Context targetContext, String source, String dest)
         throws IOException, NameNotFoundException {
         
         File targetFile = null;
         
         Context testApkContext = targetContext.createPackageContext("edu.berkeley.androidwave.tests", Context.CONTEXT_IGNORE_SECURITY);
         InputStream is = testApkContext.getAssets().open(source);
         
         String[] destComponents = dest.split(File.separator);
         // System.out.println("copyTestAssetToInternal -> destComponents = "+java.util.Arrays.toString(destComponents));
         OutputStream os = null;
         
         if (destComponents.length == 1) {
             System.out.println("copyTestAssetToInternal -> creating "+dest);
             os = targetContext.openFileOutput(dest, Context.MODE_PRIVATE);
         } else {
             // create the destination directory tree
             File dir = targetContext.getDir(destComponents[0], Context.MODE_PRIVATE);
             System.out.println("copyTestAssetToInternal -> getDir: "+dir);
             // create additional sub-dirs as necessary
             if (destComponents.length > 2) {
                 String[] s = new String[destComponents.length - 2];
                 for (int i=0; i<s.length; i++) {
                     s[i] = destComponents[i+1];
                 }
                 dir = new File(dir, unsplit(s, File.separator));
                 if (dir.mkdirs()) {
                     System.out.println("copyTestAssetToInternal -> created: "+dir);
                 }
             }
             // create the target file itself
             targetFile = new File(dir, destComponents[destComponents.length-1]);
             if (targetFile.exists()) {
                 System.out.print("copyTestAssetToInternal->Deleting existing file at "+targetFile+"...");
                 if (targetFile.delete()) {
                     System.out.println(" done.");
                 } else {
                     System.out.println(" fail.");
                 }
             }
             System.out.println("copyTestAssetToInternal -> targetFile = "+targetFile);
             os = new FileOutputStream(targetFile);
         }
         
         byte[] buf = new byte[1024];
         int len;
         while ((len = is.read(buf)) > 0) {
             os.write(buf, 0, len);
         }
         is.close();
         os.close();
         
         return targetFile;
     }
     
     public static String[] arrayToStringArray(Object[] objects) {
         String[] strings = new String[objects.length];
         for (int i=0; i<objects.length; i++) {
             strings[i] = objects[i].toString();
         }
         return strings;
     }
     
     /**
      * builds a method signature suitable for use with {@code assertHasMethod}
      * Use null {@code returnedClass} for void return type.
      * 
      * We need everything in the signature to have a fully qualified name,
      * so this method provides that convenience.
      */
     public static String methodSignature(Object target,
                                          String accessModifier,
                                          boolean isStatic,
                                          Class returnedClass,
                                          String methodName,
                                          Class[] argClasses,
                                          Class... thrownClasses)
                                             throws NullPointerException {
         
         if (methodName == null) throw new NullPointerException("methodSignature cannot take a null methodName argument");
         
         String sig = (accessModifier == null ? "protected" : accessModifier) + " ";
         if (isStatic) {
             sig += "static ";
         }
         sig += (returnedClass == null ? "void " : returnedClass.getName()) + " ";
         
         sig += (target instanceof Class ? ((Class)target).getName() : target.getClass().getName()) + "." + methodName + "(";
         
         if (argClasses != null) {
             boolean seenOneArg = false;
             for (int i=0; i<argClasses.length; i++) {
                 sig += (seenOneArg ? ", " : "") + argClasses[i].getName();
                 seenOneArg = true;
             }
         }
         
         sig += ")";
         
         if (thrownClasses != null) {
             if (thrownClasses.length > 0) {
                 sig += " throws ";
                 boolean seenOneThrows = false;
                 for (int i=0; i<thrownClasses.length; i++) {
                     sig += (seenOneThrows ? ", " : "") + thrownClasses[i].getName();
                     seenOneThrows = true;
                 }
             }
         }
         
         return sig;
     }
     
     /**
      * Asserts that a given object instance {@code actualObject} responds to
      * an expected method signature {@code expectedMethodSignature}
      * 
      * For method signature syntax, @see Method#toString
      * Use of Method#toGenericString, which might result in simpler syntax,
      * appears to cause a result similar to this known bug:
      * http://code.google.com/p/android/issues/detail?id=6636
      * present in the old API levels.
      */
     public static void assertHasMethod(String expectedMethodSignature, Object actualObject) {
         assertHasMethod(expectedMethodSignature, false, actualObject);
     }
     
     public static void assertHasMethod(String expectedMethodSignature, boolean ignoreThrowsPortion, Object actualObject) {
         Method[] methods = actualObject.getClass().getMethods();
         
         String[] methodSignatures = new String[methods.length];
         for (int i=0; i<methods.length; i++) {
             String sig = methods[i].toString();
             
             if (ignoreThrowsPortion) {
                 sig = sig.replaceFirst(" throws .*", "");
             }
             
             methodSignatures[i] = sig;
         }
         
         // pre-check the assert so we can print out extra debug info to the console
         boolean hasMethod = Arrays.asList(methodSignatures).contains(expectedMethodSignature);
         if (!hasMethod) {
             System.out.println("assertHasMethod called on "+actualObject+" which has methods "+(ignoreThrowsPortion ? "(throws portion ignored)" : "")+":");
             for (int i=0; i<methodSignatures.length; i++) {
                 System.out.println("\t"+methodSignatures[i]);
             }
             System.out.println();
         }
         
         Assert.assertTrue("Expected "+expectedMethodSignature+" method in class "+actualObject.getClass(), hasMethod);
     }
}
