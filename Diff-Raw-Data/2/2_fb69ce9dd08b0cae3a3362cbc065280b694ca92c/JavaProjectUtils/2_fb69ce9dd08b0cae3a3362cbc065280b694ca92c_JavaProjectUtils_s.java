 package org.bitstrings.eclipse.m2e.common;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 
 public class JavaProjectUtils
 {
     private JavaProjectUtils() {}
 
     public static int indexOfPath(IPath path, IClasspathEntry... classpathEntries)
     {
         for (int i = 0; i < classpathEntries.length; i++)
         {
            if (classpathEntries[i].equals(path))
             {
                 return i;
             }
         }
 
         return -1;
     }
 
     public static boolean containsPath(IPath path, IClasspathEntry... classpathEntries)
     {
         return (indexOfPath(path, classpathEntries) != -1);
     }
 }
