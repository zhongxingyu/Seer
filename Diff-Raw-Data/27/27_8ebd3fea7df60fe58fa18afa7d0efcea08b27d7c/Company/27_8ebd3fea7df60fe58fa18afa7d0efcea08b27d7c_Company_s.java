 package com.blazebit.security.impl.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import com.blazebit.security.IdHolder;
 
 @Entity
 @ResourceName(name = "Company", module = "Core")
 public class Company implements Serializable, IdHolder {
 
    public static final String USER_LEVEL = "user_level";
    public static final String FIELD_LEVEL = "field_level";
    public static final String OBJECT_LEVEL = "object_level";
    public static final String GROUP_HIERARCHY = "group_hierarchy";
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     private Integer id;
     private String name;
     private boolean fieldLevelEnabled = true;
     private boolean objectLevelEnabled = true;
     private boolean userLevelEnabled = true;
     private boolean groupHierarchyEnabled = true;
 
     public Company(String name) {
         this.name = name;
     }
 
     public Company() {
     }
 
     @Id
     @GeneratedValue
     @Override
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     @Basic
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isGroupHierarchyEnabled() {
         return groupHierarchyEnabled;
     }
 
     public void setGroupHierarchyEnabled(boolean groupHierarchyEnabled) {
         this.groupHierarchyEnabled = groupHierarchyEnabled;
     }
 
     public boolean isFieldLevelEnabled() {
         return fieldLevelEnabled;
     }
 
     public void setFieldLevelEnabled(boolean fieldLevelEnabled) {
         this.fieldLevelEnabled = fieldLevelEnabled;
     }
 
     public boolean isObjectLevelEnabled() {
         return objectLevelEnabled;
     }
 
     public void setObjectLevelEnabled(boolean objectLevelEnabled) {
         this.objectLevelEnabled = objectLevelEnabled;
     }
 
     public boolean isUserLevelEnabled() {
         return userLevelEnabled;
     }
 
     public void setUserLevelEnabled(boolean userLevelEnabled) {
         this.userLevelEnabled = userLevelEnabled;
     }
 
 }
