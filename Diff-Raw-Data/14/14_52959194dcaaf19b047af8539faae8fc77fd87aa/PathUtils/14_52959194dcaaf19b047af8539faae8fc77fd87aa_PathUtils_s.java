 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.utils;
 
 public class PathUtils {
 
     private PathUtils() {
     }
 
     public static String appendPath(String leftSide, String rightSide) {
         if (rightSide.isEmpty())
             return leftSide;
         else if (leftSide.isEmpty())
             return rightSide;
         else
             return leftSide + "." + rightSide;
     }
 
     public static String toLocalProperty(String path) {
         int lastIndex = path.lastIndexOf('.');
         if (lastIndex < 0 || path.isEmpty())
             return path;
         else
             return path.substring(lastIndex + 1);
     }
 
     public static boolean isParent(String prefixPath, String path) {
         if (path.isEmpty())
             return false;
         if (prefixPath.isEmpty())
             return path.indexOf('.') < 0;
 
         String[] prefixTokens = prefixPath.split("\\.");
         String[] pathTokens = path.split("\\.");
         if ((prefixTokens.length + 1) != pathTokens.length)
             return false;
         for (int i = 0; i < prefixTokens.length; i++) {
             if (prefixTokens[i].equals("*") || pathTokens[i].equals("*") || prefixTokens[i].equals(pathTokens[i]))
                 continue;
             else
                 return false;
         }
         return true;
     }
 
     public static boolean isPrefix(String prefixPath, String fullPath) {
         if (prefixPath.isEmpty())
             return true;
         if (fullPath.isEmpty())
             return prefixPath.isEmpty();
 
         String[] prefixTokens = prefixPath.split("\\.");
         String[] pathTokens = fullPath.split("\\.");
         if (prefixTokens.length > pathTokens.length)
             return false;
 
         for (int i = 0; i < prefixTokens.length; i++) {
             if (prefixTokens[i].equals("*") || pathTokens[i].equals("*") || prefixTokens[i].equals(pathTokens[i]))
                 continue;
             else
                 return false;
         }
         return true;
     }
 
     public static boolean samePrefix(String path1, String path2) {
         if (path1.isEmpty() || path2.isEmpty())
             return true;
         String[] tokens1 = path1.split("\\.");
         String[] tokens2 = path2.split("\\.");
         int end = Math.min(tokens1.length, tokens2.length);
         for (int i = 0; i < end; i++) {
             if (tokens1[i].equals("*") || tokens2[i].equals("*") || tokens1[i].equals(tokens2[i]))
                 continue;
             else
                 return false;
         }
         return true;
     }
 
     public static boolean samePath(String path1, String path2) {
         if (path1.isEmpty())
             return path2.isEmpty();
         String[] tokens1 = path1.split("\\.");
         String[] tokens2 = path2.split("\\.");
         if (tokens1.length != tokens2.length)
             return false;
         for (int i = 0; i < tokens1.length; i++) {
             if (tokens1[i].equals("*") || tokens2[i].equals("*") || tokens1[i].equals(tokens2[i]))
                 continue;
             else
                 return false;
         }
         return true;
     }
 
}
