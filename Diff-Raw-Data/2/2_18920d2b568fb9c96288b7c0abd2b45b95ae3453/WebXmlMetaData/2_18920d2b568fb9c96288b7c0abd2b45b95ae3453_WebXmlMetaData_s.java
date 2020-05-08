 /*
  * Copyright 2011-2013, KC CLASS, Robert Dukaric, Matej Lazar and Ales Justin.
  */
 
 package com.alterjoc.caliper.server.xml;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class WebXmlMetaData {
     Map<String, String> servlets = new HashMap<String, String>();
     Map<String, String> mappings = new HashMap<String, String>();
 
     public void addServlet(String name, String className) {
         servlets.put(name, className);
     }
 
     public void addMapping(String name, String mapping) {
         mappings.put(name, mapping);
     }
 
     public Set<String> names() {
         return servlets.keySet();
     }
 
     public String getServlet(String name) {
         return servlets.get(name);
     }
 
     public String getMapping(String name) {
         String mapping = mappings.get(name);
         if (mapping == null)
             throw new IllegalArgumentException("Missing mapping for servlet: " + name);
         return mapping;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         for (String name : names())
            builder.append(getMapping(name)).append(" --> ").append(getServlet(name));
         return builder.toString();
     }
 }
