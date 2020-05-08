 package org.xtest.runner.util;
 
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.IPath;
 
 public class ClasspathUtils {
 
     public static IPath normalizePath(final IWorkspaceRoot root, IPath path) {
         if (root.exists(path)) {
            path = root.getLocation().append(path);
         }
         return path;
     }
 }
