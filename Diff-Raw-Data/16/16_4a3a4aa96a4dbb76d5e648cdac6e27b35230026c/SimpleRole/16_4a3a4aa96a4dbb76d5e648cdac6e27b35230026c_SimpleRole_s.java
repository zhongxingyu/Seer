 package org.jboss.picketlink.idm.model;
 
 /**
  * Simple implementation of the Role interface
  */
 public class SimpleRole extends AbstractIdentityType implements Role
 {
     private String name;
     
     public SimpleRole(String name)
     {
         this.name = name;
     }
     
     public String getName()
     {
         return name;
     }
 
     public String getKey() 
     {
        return String.format("%d%d", KEY_PREFIX, name);
     }
 }
