 /*
  * Copyright 2006 Wyona
  */
 
 package org.wyona.yanel.impl.resources;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.CreatableV2;
 
 import org.apache.log4j.Category;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  */
 public class ArchiveURLResource extends Resource implements CreatableV2 {
 
     private static Category log = Category.getInstance(ArchiveURLResource.class);
 
     /**
      *
      */
     public ArchiveURLResource() {
     }
 
     /**
      *
      */
     public Object getProperty(String name) {
         log.warn("No implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public String[] getPropertyNames() {
         String[] props = {"url"};
         return props;
     }
 
     /**
      *
      */
     public void setProperty(String name, Object value) {
         log.warn("No implemented yet!");
     }
 
     /**
      *
      */
     public void create(HttpServletRequest request) {
         log.warn("No implemented yet!");
     }
 
     /**
      *
      */
     public java.util.HashMap createRTIProperties(HttpServletRequest request) {
         log.warn("No implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public String getPropertyType(String name) {
        return CreatableV2.TYPE_STRING;
     }
 }
