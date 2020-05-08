 /*
  * Copyright 2012 Marco Soeima
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package com.soeima.resources.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Provides static convenience methods that are to be used when dealing with paths.
  *
  * @author   <a href="mailto:marco.soeima@gmail.com">Marco Soeima</a>
  * @version  2012/09/25
  */
 public class Paths {
 
     /**
      * Creates a new {@link Paths} object.
      */
     private Paths() {
     }
 
     /**
      * Places a "." at the beginning of the given <code>name</code>.
      *
      * @param   name  The name to prefix with a ".".
      *
      * @return  The name with a "." at the beginning or <code>null</code> if <code>name</code> is <code>null</code>.
      */
     public static String prefixDot(String name) {
 
         if (name == null) {
             return null;
         }
 
         return (!name.startsWith(".")) ? ("." + name) : name;
     }
 
     /**
      * *
      *
      * @param   path
      *
      * @return  boolean
      */
     public static boolean hasSlash(String path) {
 
         if (path == null) {
             return false;
         }
 
         return path.startsWith("/") || path.startsWith("\\");
     }
 
     /**
      * *
      *
      * @param   path
      *
      * @return  boolean
      */
     public static boolean hasTrailingSlash(String path) {
 
         if (path == null) {
             return false;
         }
 
         return path.endsWith("/") || path.endsWith("\\");
     }
 
     /**
      * *
      *
      * @param   path
      *
      * @return  String
      */
     public static String deSlash(String path) {
 
         if (path == null) {
             return null;
         }
 
         return hasSlash(path) ? path.substring(1) : path;
     }
 
     /**
      * *
      *
      * @param   path
      *
      * @return  String
      */
     public static String deTrailingSlash(String path) {
 
         if (path == null) {
             return null;
         }
 
         return hasTrailingSlash(path) ? path.substring(0, path.length() - 1) : path;
     }
 
     /**
      * Prefixes a slash at the beginning of the <code>path</code>. The prefixes slash is in accordance with the system
      * {@link File#separator}.
      *
      * @param   path  The path to which the slash is to be prefixed.
      *
      * @return  A slash-prefixed <code>path</code> or <code>null</code> if <code>path</code> is <code>null</code>.
      */
     public static String prefixSlash(String path) {
 
         if (path == null) {
             return null;
         }
 
         return hasSlash(path) ? path : join(File.separator, path);
     }
 
     /**
      * Joins the <code>base</code> path to <code>path</code>
      *
      * @param   base   The base path.
      * @param   paths  The paths to append to the base path, in the order in which the are passed.
      *
      * @return  A new path resulting from joining the <code>base</code> to <code>paths</code> or <code>null</code> if
      *          either <code>base</code> or <code>paths</code> is <code>null</code>.
      */
     public static String join(String base, String... paths) {
 
         if ((base == null) || (paths == null)) {
             return null;
         }
 
         List<String> strings = new ArrayList<String>();
         strings.add(deTrailingSlash(base));
 
         for (String path : paths) {
             strings.add(deSlash(path));
         }
 
         return Strings.join(strings, File.separator);
     }
 
     /**
      * Splits the <code>path</code> into its various components.
      *
      * @param   path  The path to split.
      *
      * @return  A list of path components or an empty list if <code>path</code> is <code>null</code>.
      */
     public static List<String> split(String path) {
 
         if (path == null) {
             return Collections.emptyList();
         }
 
         return Strings.split(normalize(path, '/'), "/");
     }
 
     /**
      * Returns <code>true</code> if <code>fileName</code>'s extension matches any of the given <code>extensions</code>.
      *
      * @param   fileName    The file name to check.
      * @param   extensions  The extensions to check against the <code>fileName</code>.
      *
      * @return  <code>true</code> if <code>fileName</code>'s extension matches any of the given <code>extensions</code>;
      *          <code>false</code> otherwise.
      */
     public static boolean isExtension(String fileName, String... extensions) {
 
         if ((fileName == null) || (extensions == null)) {
             return false;
         }
 
         for (String extension : extensions) {
             return fileName.endsWith(extension.startsWith(".") ? extension : ("." + extension));
         }
 
         return false;
     }
 
     /**
      * Returns the given <code>path</code>'s base name.
      *
      * @param   path  The path whose base name is to be returned.
      *
      * @return  The <code>path</code> base name or <code>null</code> if <code>path</code> is <code>null</code>.
      */
     public static String getBaseName(String path) {
 
         if (path == null) {
             return null;
         }
 
         return new File(path).getName();
     }
 
     /**
      * Returns the given <code>path</code>'s parent path.
      *
      * @param   path  The path whose parent path is to be returned.
      *
      * @return  <code>path</code>'s parent path, an empty string if <code>path</code> does not have a parent path or
      *          <code>null</code> if <code>path</code> is <code>null</code>.
      */
     public static String getParentPath(String path) {
 
         if (path == null) {
             return null;
         }
 
         String parentPath = new File(path).getParent();
         return (parentPath != null) ? parentPath : "";
     }
 
     /**
      * Normalizes the given <code>path</code> by converting its path separator to either '/' or '\'.
      *
      * @param   path       The path whose separators are to be converted to either '\' or '/'.
      * @param   separator  Either '\' or '/'.
      *
      * @return  A normalized path or <code>null</code> if <code>path</code> is <code>null</code> or <code>
      *          separator</code> is not either '/' or '\'.
      */
     public static String normalize(String path, char separator) {
 
         if ((path == null) || ((separator != '/') && (separator != '\\'))) {
             return null;
         }
 
         return path.replace((separator == '/') ? '\\' : '/', separator);
     }
 
     /**
      * Strips the given <code>root</code> path from the <code>path</code>.
      *
      * @param   path  The path whose <code>root</code> path is to be stripped.
      * @param   root  The root path to strip from the <code>path</code>.
      *
      * @return  A stripped <code>path</code> or an empty string if <code>root</code> is not contained in <code>
      *          path</code> or <code>null</code> if either <code>path</code> or <code>root</code> are <code>null</code>.
      */
     public static String stripParentPath(String path, String root) {
 
         if ((path == null) || (root == null)) {
             return (path == null) ? root : path;
         }
 
         return startsWithNormalized(path, root) ? deSlash(path.substring(root.length())) : "";
     }
 
     /**
      * Normalizes both <code>path1</code> and <code>path2</code> and then tests if the first is equal to the second.
      *
      * @param   path1  The first path to test.
      * @param   path2  The second path to test.
      *
      * @return  <code>true</code> if <code>path1</code> and <code>path2</code> are equal.
      */
     public static boolean equalsNormalized(String path1, String path2) {
 
         if (path1 == path2) {
             return true;
         }
 
         return normalize(path1, '/').equals(normalize(path2, '/'));
     }
 
     /**
      * Normalizes both <code>path1</code> and <code>path2</code> and then tests if the first starts with the second.
      *
      * @param   path1  The path to test.
      * @param   path2  The end part of<code>path1</code>.
      *
      * @return  <code>true</code> if <code>path1</code> ends with <code>path2</code>.
      */
     public static boolean startsWithNormalized(String path1, String path2) {
         return normalize(path1, '/').startsWith(normalize(path2, '/'));
     }
 
     /**
      * Normalizes both <code>path1</code> and <code>path2</code> and then tests if the first ends with the second.
      *
      * @param   path1  The path to test.
      * @param   path2  The end part of <code>path1</code>.
      *
      * @return  <code>true</code> if <code>path1</code> ends with <code>path2</code>.
      */
     public static boolean endsWithNormalized(String path1, String path2) {
 
         if ((path1 == null) || (path2 == null)) {
             return false;
         }
 
        return normalize(prefixSlash(path1), '/').endsWith(normalize(prefixSlash(path2), '/'));
     }
 } // end class Paths
