 package org.wyona.commons.io;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class Path {
 
     private static Category log = Category.getInstance(Path.class);
 
     protected String path;
 
     /**
      *
      */
     public Path() {
     }
 
     /**
      *
      */
     public Path(String path) {
         this.path = path;
     }
 
     /**
      *
      */
     public String getName() {
         // Quick and dirty
         return new java.io.File(path).getName();
     }
 
     /**
      *
      */
     public Path getParent() {
         // Quick and dirty
        String parent = new java.io.File(path).getParent();
        if (parent == null) return null;
        return new Path(parent);
     }
 
     /**
      * Return null if no suffix exists
      */
     public String getSuffix() {
         int lio = path.lastIndexOf(".");
         log.debug(new Integer(lio));
         if (lio < 0) return null;
         return path.substring(lio + 1);
     }
 
     /**
      *
      */
     public String toString() {
         return path;
     }
 }
