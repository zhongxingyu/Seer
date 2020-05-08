 /*
  * Copyright 2013 Blazebit.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  */
 package com.blazebit.security.impl.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.Collections;
 import java.util.List;
 
 import com.blazebit.security.Action;
 import com.blazebit.security.Resource;
 import com.blazebit.security.constants.ActionConstants;
 
 /**
  * Class representing an Entity resource with an optional field attribute. If field attribute is empty the resource refers to
  * all the entities defined by the entity name attribute.
  * 
  * @author Christian
  */
 public class EntityField implements Resource {
 
     public static final String EMPTY_FIELD = "";
     protected String entity;
     protected String field;
     protected PermissionId<?> permissionId;
 
     public EntityField() {
     }
 
     public EntityField(String entity, String field) {
         this.entity = entity;
         this.field = field;
     }
 
     public EntityField(String entity) {
         this.entity = entity;
         this.field = EMPTY_FIELD;
     }
 
     public PermissionId<?> getPermissionId() {
         return permissionId;
     }
 
     public <P extends PermissionId<?>> EntityField(P permissionId) {
         this.permissionId = permissionId;
         this.entity = permissionId.getEntity();
         this.field = permissionId.getField();
     }
 
     public <P extends PermissionId<?>> void attachToPermissionId(P permissionId) {
         this.permissionId = permissionId;
 
         if (entity != null) {
             permissionId.setEntity(entity);
             entity = null;
         }
         if (field != null) {
             permissionId.setField(field);
             field = null;
         }
     }
 
     @Override
     public boolean implies(Resource resource) {
         if (resource instanceof EntityField) {
             EntityField _resource = (EntityField) resource;
             if (permissionId == null) {
                 return this.getEntity().equals(_resource.getEntity()) && (this.isEmptyField() || this.getField().equals(_resource.getField()));
             } else {
                 return permissionId.getEntity().equals(_resource.getEntity())
                     && (permissionId.getField().equals(EMPTY_FIELD) || permissionId.getField().equals(_resource.getField()));
             }
         } else {
             throw new IllegalArgumentException("Not supported resource type");
         }
     }
 
     @Override
     public boolean isReplaceableBy(Resource resource) {
         if (resource instanceof EntityObjectField) {
             return false;
         } else {
             if (resource instanceof EntityField) {
                 EntityField _resource = (EntityField) resource;
                 // EntityField can replace other EntityFields if the existing EntityField has specific field and the new
                 // EntityField
                 // has EMPTY_FIELD
                 if (permissionId == null) {
                     return this.getEntity().equals(_resource.getEntity()) && (_resource.isEmptyField() && !this.isEmptyField());
                 } else {
                     return permissionId.getEntity().equals(_resource.getEntity()) && (_resource.isEmptyField() && !permissionId.getField().equals(EMPTY_FIELD));
                 }
             } else {
                 throw new IllegalArgumentException("Not supported resource type");
 
             }
         }
     }
 
     public boolean isEmptyField() {
         return this.getField().equals(EMPTY_FIELD);
     }
 
     public boolean isObject() {
         return this instanceof EntityObjectField;
     }
 
     public String getEntity() {
         return permissionId == null ? entity : permissionId.getEntity();
     }
 
     public void setEntity(String entity) {
         this.entity = entity;
         if (permissionId != null) {
             permissionId.setEntity(entity);
         }
     }
 
     public String getField() {
         return permissionId == null ? field : permissionId.getField();
     }
 
     public void setField(String field) {
         this.field = field;
         if (permissionId != null) {
             permissionId.setField(field);
         }
     }
 
     @Override
     public String toString() {
         return "EntityField{" + "entity=" + getEntity() + ", field=" + getField() + '}';
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final EntityField other = (EntityField) obj;
         if ((this.entity == null) ? (other.entity != null) : !this.entity.equals(other.entity)) {
             return false;
         }
         if ((this.field == null) ? (other.field != null) : !this.field.equals(other.field)) {
             return false;
         }
         return true;
     }
 
     @Override
     public Collection<Resource> connectedResources() {
         List<Resource> ret = new ArrayList<Resource>();
         ret.add(this);
         if (!isEmptyField()) {
             ret.add((Resource) new EntityField(entity));
         }
         return ret;
     }
 
     @Override
     public boolean isApplicable(Action action) {
         if (!isEmptyField()) {
             return !action.implies(new EntityAction(ActionConstants.CREATE)) && !action.implies(new EntityAction(ActionConstants.DELETE))
                 && !action.implies(new EntityAction(ActionConstants.GRANT)) && !action.implies(new EntityAction(ActionConstants.REVOKE));
         } else {
             return true;
         }
     }
 
     public EntityField getParent() {
         if (!isEmptyField()) {
             return new EntityField(entity);
         } else {
             return null;
         }
     }
 
     public EntityField getChild(String field) {
         if (isEmptyField()) {
             return new EntityField(entity, field);
         } else {
             return null;
         }
     }
 
 }
