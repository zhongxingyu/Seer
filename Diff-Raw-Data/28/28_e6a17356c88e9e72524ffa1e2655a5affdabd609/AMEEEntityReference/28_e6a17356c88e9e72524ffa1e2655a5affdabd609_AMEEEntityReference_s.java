 /*
  * This file is part of AMEE.
  *
  * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.domain;
 
 import com.amee.domain.auth.AccessSpecification;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.Transient;
 import java.io.Serializable;
 
 /**
  * An embeddable entity for use in AMEEEntity extended classes to encapsulate properties
  * for referring to another AMEEEntity (from another database table). The entityId, entityUid
  * and entityType properties allow the application to reference any entity / database row
  * in another AMEEEntity table.
  */
 @Embeddable
 public class AMEEEntityReference implements IAMEEEntityReference, Serializable {
 
     public final static int ENTITY_TYPE_MAX_SIZE = 50;
 
     /**
      * The ID of the entity an AMEEEntityReference is referring to.
      */
     @Column(name = "ENTITY_ID", nullable = false)
     private Long entityId;
 
     /**
      * The UID of the entity an AMEEEntityReference is referring to.
      */
     @Column(name = "ENTITY_UID", length = AMEEEntity.UID_SIZE, nullable = false)
     private String entityUid = "";
 
     /**
      * The entity type of the entity an AMEEEntityReference is referring to. The entityType
      * property is exposed externally as an ObjectType but stored internally as a String.
      */
     @Column(name = "ENTITY_TYPE", length = ENTITY_TYPE_MAX_SIZE, nullable = false)
     private String entityType = "";
 
     /**
      * A transient AccessSpecification.
      */
     @Transient
     private AccessSpecification accessSpecification;
 
     /**
      * A transient reference to the actual entity.
      */
     @Transient
     private AMEEEntity entity;
 
     /**
      * Default constructor.
      */
     public AMEEEntityReference() {
         super();
     }
 
     /**
      * Construct an AMEEEntityReference based on the supplied IAMEEEntityReference.
      *
      * @param entityReference to reference
      */
     public AMEEEntityReference(IAMEEEntityReference entityReference) {
         this();
         setEntityId(entityReference.getEntityId());
         setEntityUid(entityReference.getEntityUid());
         setEntityType(entityReference.getObjectType());
         setAccessSpecification(entityReference.getAccessSpecification());
         setEntity(entityReference.getEntity());
     }
 
     /**
      * Construct an AMEEEntityReference based on the supplied ObjectType and UID.
      *
      * @param entityType for new instance
      * @param uid        for new instance
      */
     public AMEEEntityReference(ObjectType entityType, String uid) {
         this();
         setEntityUid(uid);
         setEntityType(entityType);
     }
 
     /**
      * Construct an AMEEEntityReference based on the supplied ObjectType and UID.
      *
      * @param entityType for new instance
      * @param uid        for new instance
      */
     public AMEEEntityReference(String entityType, String uid) {
         this(ObjectType.valueOf(entityType), uid);
     }
 
     /**
      * Factory method for creating a new AMEEEntityReference instance. Useful in templates.
      *
      * @param entityType for new instance
      * @param uid        for new instance
      * @return the new AMEEEntityReference instance
      */
     public static AMEEEntityReference getInstance(ObjectType entityType, String uid) {
         return new AMEEEntityReference(entityType, uid);
     }
 
     /**
      * Two AMEEEntityReferences are considered equal if the entityId and entityType properties
      * match, along with standard object equality.
      * <p/>
      * This needs to be kept the same as com.amee.domain.AMEEEntity#equals.
      *
      * @param o object to compare against
      * @return true if the obejcts are considered equal, otherwise false
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if ((o == null) || !IAMEEEntityReference.class.isAssignableFrom(o.getClass())) return false;
         IAMEEEntityReference entity = (IAMEEEntityReference) o;
         return getEntityUid().equals(entity.getEntityUid()) && getObjectType().equals(entity.getObjectType());
     }
 
     /**
      * Returns a hash code based on the entityId and entityType properties.
      * <p/>
      * This needs to be kept the same as com.amee.domain.AMEEEntity#hashCode.
      *
      * @return the hash code
      */
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 31 * hash + (null == getEntityUid() ? 0 : getEntityUid().hashCode());
         hash = 31 * hash + (null == getEntityType() ? 0 : getEntityType().hashCode());
         return hash;
     }
 
     @Override
     public String toString() {
         return getEntityType() + "_" + getEntityUid();
     }
 
     /**
      * Returns a JSONObject representing the AMEEEntityReference.
      *
      * @return the new JSONObject
      * @throws JSONException
      */
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("uid", getEntityUid());
         obj.put("type", getEntityType().getName());
         return obj;
     }
 
     /**
      * Return a DOM Element representing the AMEEEntityReference.
      *
      * @param document which new Element belongs to
      * @param name     of new Element
      * @return the new Element
      */
     public Element getElement(Document document, String name) {
         Element element = document.createElement(name);
         element.setAttribute("uid", getEntityUid());
         element.setAttribute("type", getEntityType().getName());
         return element;
     }
 
     /**
      * Get the entity id of the referenced entity.
      *
      * @return the entity id
      */
     public Long getEntityId() {
         return entityId;
     }
 
     /**
      * Set the entity id of the referenced AMEEEntity.
      *
      * @param entityId to set
      */
     public void setEntityId(Long entityId) {
         this.entityId = entityId;
     }
 
     /**
      * Get the UID of the referenced AMEEEntity.
      *
      * @return the entity UID
      */
     public String getEntityUid() {
         return entityUid;
     }
 
     /**
      * Set the entityUid property.
      *
      * @param entityUid to set
      */
     public void setEntityUid(String entityUid) {
         if (entityUid == null) {
             entityUid = "";
         }
         this.entityUid = entityUid;
     }
 
     /**
      * Get the ObjectType for the entityType property.
      *
      * @return the ObjectType
      */
     public ObjectType getObjectType() {
         return getEntityType();
     }
 
     /**
      * Get the ObjectType for the entityType property.
      *
      * @return the ObjectType
      */
     public ObjectType getEntityType() {
         return ObjectType.valueOf(entityType);
     }
 
     /**
      * Set the ObjectType for the entityType property.
      *
      * @param entityType to set
      */
     public void setEntityType(ObjectType entityType) {
         // TODO: This conditional may not be needed.
         if (entityType != null) {
             this.entityType = entityType.getName();
         } else {
             this.entityType = "";
         }
     }
 
     /**
      * Returns the transient AccessSpecification for this entity. This will only be present if
      * an AccessSpecification for this entity has been created in the current thread.
      *
      * @return the AccessSpecification for this entity in the current thread
      */
     public AccessSpecification getAccessSpecification() {
         return accessSpecification;
     }
 
     /**
      * Sets the AccessSpecification for this entity.
      *
      * @param accessSpecification for this entity
      */
     public void setAccessSpecification(AccessSpecification accessSpecification) {
         this.accessSpecification = accessSpecification;
     }
 
     public AMEEEntity getEntity() {
         return entity;
     }
 
     public void setEntity(AMEEEntity entity) {
         this.entity = entity;
     }
 }
