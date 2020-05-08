 package org.jboss.pressgang.ccms.rest.v1.components;
 
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTFileV1;
 
 public class ComponentFileV1 {
     private final RESTFileV1 source;
 
     public ComponentFileV1(final RESTFileV1 source) {
         this.source = source;
     }
 
     public boolean isArchive() {
         return isArchive(source);
     }
 
     public static boolean isArchive(final RESTFileV1 source) {
         final String extension = getExtension(source);
         if ("zip".equalsIgnoreCase(extension)) {
             return true;
         } else {
             return false;
         }
     }
 
     public String getExtension() {
         return getExtension(source);
     }
 
     public static String getExtension(final RESTFileV1 source) {
        final int lastPeriodIndex = source.getFileName().lastIndexOf(".");
        if (lastPeriodIndex != -1 && lastPeriodIndex < source.getFileName().length() - 1) {
            return source.getFileName().substring(lastPeriodIndex + 1);
         }
 
         return null;
     }
 }
