 package org.wyona.commons.io;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class PathUtil {
 
     private static Category log = Category.getInstance(PathUtil.class);
 
     /**
      *
      */
     public static String getName(String path) {
         // Quick and dirty
         return new java.io.File(path).getName();
     }
 
     /**
      *
      */
     public static String getParent(String path) {
         if (path.indexOf("\\") >= 0) {
             Exception e = new Exception("Do not use backward slashes: " + path);
             log.error(e.getMessage(), e);
             return null;
         }
         if (path.equals("/")) {
             if (log.isDebugEnabled()) log.debug("Root / has no parent!");
             return null;
         }
         String parent = path;
         if (path.endsWith("/")) {
             parent = path.substring(0, path.length() - 1);
         }
         return parent.substring(0, parent.lastIndexOf("/") + 1);
     }
 
     /**
      * Return null if no suffix exists
      */
     public static String getSuffix(String path) {
         int lio = path.lastIndexOf(".");
         log.debug(new Integer(lio));
         if (lio < 0) return null;
         return path.substring(lio + 1);
     }
 
     /**
      *
      */
     public static String concat(String absolutePath, String relativePath) {
         String newPath = absolutePath;
         if (!new Path(absolutePath).isCollection()) {
             newPath = new Path(newPath).getParent().toString();
         }
        log.error(newPath);
        return null;
     }
 }
