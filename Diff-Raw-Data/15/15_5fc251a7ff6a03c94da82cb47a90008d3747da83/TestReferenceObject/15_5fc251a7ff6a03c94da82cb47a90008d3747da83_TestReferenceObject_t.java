 package org.otherobjects.cms.binding;
 
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.Type;
 
@Type(labelProperty = "name", store="jackrabbit")
 public class TestReferenceObject extends BaseNode
 {
     private String name;
 
     @Property
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 }
