 package com.mymed.properties;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 
 public class PropertiesManager {
  public static IProperties getManager(final String name) throws InternalBackEndException {
     if (name == null) {
       throw new InternalBackEndException("Properties manager name cannot be null");
     }
 
    IProperties manager = null;
 
    if ("columns".equals(name)) {
      manager = PropertiesColumnsManager.getInstance();
     }
 
     return manager;
   }
 }
