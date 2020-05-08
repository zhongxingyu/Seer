 package org.jboss.picketlink.idm.model;
 
 /**
  * Simple implementation of the Group interface 
  *
  */
 public class SimpleGroup extends AbstractIdentityType implements Group
 {
     private String id;
     private String name;
     private Group parentGroup;
     
     public SimpleGroup(String id, String name, Group parentGroup)
     {
         this.id = id;
         this.name = name;
         this.parentGroup = parentGroup;
     }
     
     public String getId()
     {
         return id;
     }
 
     public String getName()
     {
         return name;
     }
 
     public Group getParentGroup()
     {
         return parentGroup;
     }
     
     public String getKey() 
     {
        return String.format("%d%d", KEY_PREFIX, id);
     }    
 }
