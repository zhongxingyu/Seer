 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2013, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.japit.reporting;
 
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeMap;
 import org.jboss.japit.core.Archive;
 import org.jboss.japit.core.ClassDetails;
 import org.jboss.japit.core.JarArchive;
 
 /**
  *
  * @author Rostislav Svoboda
  */
 public class TextFactory {
 
     private static boolean failCalled;
 
     private TextFactory() {
     }
 
     public static void generateTextDiff(PrintStream out, Collection<Archive> archives, boolean ignoreClassVersion) {
         Iterator<Archive> iter = archives.iterator();
         JarArchive first = (JarArchive) iter.next();
         JarArchive second = (JarArchive) iter.next();
 
         out.println("First jar: " + first.getFilePath());
         out.println("Second jar: " + second.getFilePath());
         out.println();
         out.println("Compared classes count: " + first.getClasses().size() + " vs. " + second.getClasses().size());
         if (ignoreClassVersion) {
             out.println("Class Version ignored in comparison");
         }
         out.println();
 
         TreeMap<String, ClassDetails> firstJarClassesMap = new TreeMap<String, ClassDetails>();
         for (ClassDetails firstJarClass : first.getClasses()) {
             firstJarClassesMap.put(firstJarClass.getClassName(), firstJarClass);
         }
         for (ClassDetails secondJarClass : second.getClasses()) {
             out.println(secondJarClass.getClassName() + ":");
             failCalled = false;
 
             ClassDetails firstJarClass = null;
             try {
                 firstJarClass = firstJarClassesMap.remove(secondJarClass.getClassName());
                 if (firstJarClass == null) {
                     fail(out, "class doesn't exist in first jar");
                     continue;
                 }
             } catch (Exception e) {
                 fail(out, "class doesn't exist in first jar");
                 continue;
             }
 
             if (!ignoreClassVersion && firstJarClass.getClassVersion() != secondJarClass.getClassVersion()) {
                 fail(out, "class version doesn't match: " + firstJarClass.getClassVersion() + " vs. " + secondJarClass.getClassVersion());
             }
 
             if (firstJarClass.getMethodsCount() != secondJarClass.getMethodsCount()) {
                 fail(out, "methods count doesn't match: " + firstJarClass.getMethodsCount() + " vs. " + secondJarClass.getMethodsCount());
             }
 
             if (firstJarClass.getDeclaredMethodsCount() != secondJarClass.getDeclaredMethodsCount()) {
                 fail(out, "declared methods count doesn't match: " + firstJarClass.getDeclaredMethodsCount() + " vs. " + secondJarClass.getDeclaredMethodsCount());
             }
 
             if (firstJarClass.getFieldsCount() != secondJarClass.getFieldsCount()) {
                 fail(out, "fields count doesn't match: " + firstJarClass.getFieldsCount() + " vs. " + secondJarClass.getFieldsCount());
             }
 
             if (firstJarClass.getDeclaredFieldsCount() != secondJarClass.getDeclaredFieldsCount()) {
                 fail(out, "declared fields count doesn't match: " + firstJarClass.getDeclaredFieldsCount() + " vs. " + secondJarClass.getDeclaredFieldsCount());
             }
 
 
             if (!firstJarClass.getSuperclassName().equals(secondJarClass.getSuperclassName())) {
                 fail(out, "super class name doesn't match: " + firstJarClass.getSuperclassName() + " vs. " + secondJarClass.getSuperclassName());
             }
            if (!firstJarClass.getOriginalJavaFile().equals(secondJarClass.getOriginalJavaFile())) {
                 fail(out, "original java file doesn't match: " + firstJarClass.getOriginalJavaFile() + " vs. " + secondJarClass.getOriginalJavaFile());
             }
 
             for (String firstJarClassMethod : firstJarClass.getMethods()) {
                 if (!secondJarClass.getMethods().contains(firstJarClassMethod)) {
                     fail(out, "second jar doesn't contain method " + firstJarClassMethod);
                 }
             }
             for (String secondJarClassMethod : secondJarClass.getMethods()) {
                 if (!firstJarClass.getMethods().contains(secondJarClassMethod)) {
                     fail(out, "first jar doesn't contain method " + secondJarClassMethod);
                 }
             }
 
             for (String firstJarClassField : firstJarClass.getFields()) {
                 if (!secondJarClass.getFields().contains(firstJarClassField)) {
                     fail(out, "second jar doesn't contain field " + firstJarClassField);
                 }
             }
             for (String secondJarClassField : secondJarClass.getFields()) {
                 if (!firstJarClass.getFields().contains(secondJarClassField)) {
                     fail(out, "first jar doesn't contain field " + secondJarClassField);
                 }
             }
 
             if (!failCalled) {
                 ok(out);
             }
         }
         if (firstJarClassesMap.size() > 0) {
             for (ClassDetails firstJarClass : firstJarClassesMap.values()) {
                 out.println(firstJarClass.getClassName() + ":");
                 fail(out, "class doesn't exist in second jar");
             }
         }
     }
 
     private static void fail(PrintStream out, String details) {
         out.println("  FAIL  --  " + details);
         failCalled = true;
     }
 
     private static void ok(PrintStream out) {
         out.println("  OK");
     }
 }
