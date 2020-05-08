 package com.mymed.properties;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 
 public class PropertiesManager {
  public static Class<IProperties> getManager(final String name) throws InternalBackEndException {
     if (name == null) {
       throw new InternalBackEndException("Properties manager name cannot be null");
     }
 
    Class<IProperties> manager = null;
 
    try {
      manager = (Class<IProperties>) Class.forName(name);
    } catch (final ClassNotFoundException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
     }
 
     return manager;
   }
 }
