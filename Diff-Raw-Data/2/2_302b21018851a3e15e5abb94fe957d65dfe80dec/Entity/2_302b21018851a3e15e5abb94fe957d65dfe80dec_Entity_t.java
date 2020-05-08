 /*
  * This library is part of OpenCms -
  * the Open Source Content Management System
  *
  * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  *
  * For further information about OpenCms, please see the
  * project website: http://www.opencms.org
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.acacia.shared;
 
 import com.alkacon.vie.shared.I_Entity;
 import com.alkacon.vie.shared.I_EntityAttribute;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Serializable entity implementation.<p>
  */
 public class Entity implements I_Entity, Serializable {
 
     /** The serial version id. */
     private static final long serialVersionUID = -6933931178070025267L;
 
     /** The entity attribute values. */
     private Map<String, List<Entity>> m_entityAttributes;
 
     /** The entity id. */
     private String m_id;
 
     /** The simple attribute values. */
     private Map<String, List<String>> m_simpleAttributes;
 
     /** The type name. */
     private String m_typeName;
 
     /**
      * Constructor.<p>
      * 
      * @param id the entity id/URI
      * @param typeName the entity type name
      */
     public Entity(String id, String typeName) {
 
         this();
         m_id = id;
         m_typeName = typeName;
     }
 
     /**
      * Constructor. For serialization only.<p>
      */
     protected Entity() {
 
         m_simpleAttributes = new HashMap<String, List<String>>();
         m_entityAttributes = new HashMap<String, List<Entity>>();
     }
 
     /**
      * Transforms into a serializable entity instance.<p>
      * 
      * @param entity the entity to transform
      * 
      * @return the new entity
      */
     public static Entity transformToSerializableEntity(I_Entity entity) {
 
         if (entity instanceof Entity) {
             return (Entity)entity;
         }
         Entity result = new Entity(entity.getId(), entity.getTypeName());
         for (I_EntityAttribute attribute : entity.getAttributes()) {
             if (attribute.isSimpleValue()) {
                 for (String value : attribute.getSimpleValues()) {
                     result.addAttributeValue(attribute.getAttributeName(), value);
                 }
             } else {
                 for (I_Entity value : attribute.getComplexValues()) {
                     result.addAttributeValue(attribute.getAttributeName(), transformToSerializableEntity(value));
                 }
             }
         }
         return result;
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#addAttributeValue(java.lang.String, com.alkacon.vie.shared.I_Entity)
      */
     public void addAttributeValue(String attributeName, I_Entity value) {
 
         if (m_simpleAttributes.containsKey(attributeName)) {
             throw new RuntimeException("Attribute already exists with a simple type value.");
         }
         if (!(value instanceof Entity)) {
             value = transformToSerializableEntity(value);
         }
         if (m_entityAttributes.containsKey(attributeName)) {
             m_entityAttributes.get(attributeName).add((Entity)value);
         } else {
             List<Entity> values = new ArrayList<Entity>();
             values.add((Entity)value);
             m_entityAttributes.put(attributeName, values);
         }
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#addAttributeValue(java.lang.String, java.lang.String)
      */
     public void addAttributeValue(String attributeName, String value) {
 
         if (m_entityAttributes.containsKey(attributeName)) {
             throw new RuntimeException("Attribute already exists with a entity type value.");
         }
         if (m_simpleAttributes.containsKey(attributeName)) {
             m_simpleAttributes.get(attributeName).add(value);
         } else {
             List<String> values = new ArrayList<String>();
             values.add(value);
             m_simpleAttributes.put(attributeName, values);
         }
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#getAttribute(java.lang.String)
      */
     public I_EntityAttribute getAttribute(String attributeName) {
 
         if (m_simpleAttributes.containsKey(attributeName)) {
             return EntityAttribute.createSimpleAttribute(attributeName, m_simpleAttributes.get(attributeName));
         }
         if (m_entityAttributes.containsKey(attributeName)) {
             return EntityAttribute.createEntityAttribute(attributeName, m_entityAttributes.get(attributeName));
         }
         return null;
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#getAttributes()
      */
     public List<I_EntityAttribute> getAttributes() {
 
         List<I_EntityAttribute> result = new ArrayList<I_EntityAttribute>();
         for (String name : m_simpleAttributes.keySet()) {
             result.add(getAttribute(name));
         }
         for (String name : m_entityAttributes.keySet()) {
             result.add(getAttribute(name));
         }
        return result;
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#getId()
      */
     public String getId() {
 
         return m_id;
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#getTypeName()
      */
     public String getTypeName() {
 
         return m_typeName;
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#hasAttribute(java.lang.String)
      */
     public boolean hasAttribute(String attributeName) {
 
         return m_simpleAttributes.containsKey(attributeName) || m_entityAttributes.containsKey(attributeName);
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#removeAttributeSilent(java.lang.String)
      */
     public void removeAttributeSilent(String attributeName) {
 
         m_simpleAttributes.remove(attributeName);
         m_entityAttributes.remove(attributeName);
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#setAttributeValue(java.lang.String, com.alkacon.vie.shared.I_Entity)
      */
     public void setAttributeValue(String attributeName, I_Entity value) {
 
         // make sure there is no simple attribute value set
         m_simpleAttributes.remove(attributeName);
         if (!(value instanceof Entity)) {
             value = transformToSerializableEntity(value);
         }
         List<Entity> values = new ArrayList<Entity>();
         values.add((Entity)value);
         m_entityAttributes.put(attributeName, values);
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#setAttributeValue(java.lang.String, com.alkacon.vie.shared.I_Entity, int)
      */
     public void setAttributeValue(String attributeName, I_Entity value, int index) {
 
         if (m_simpleAttributes.containsKey(attributeName)) {
             throw new RuntimeException("Attribute already exists with a simple type value.");
         }
         if (!(value instanceof Entity)) {
             // ensure serializable entity
             value = transformToSerializableEntity(value);
         }
         if (!m_entityAttributes.containsKey(attributeName)) {
             if (index != 0) {
                 throw new IndexOutOfBoundsException();
             } else {
                 List<Entity> values = new ArrayList<Entity>();
                 values.add((Entity)value);
                 m_entityAttributes.put(attributeName, values);
             }
         } else {
             m_entityAttributes.get(attributeName).add(index, (Entity)value);
         }
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String)
      */
     public void setAttributeValue(String attributeName, String value) {
 
         m_entityAttributes.remove(attributeName);
         List<String> values = new ArrayList<String>();
         values.add(value);
         m_simpleAttributes.put(attributeName, values);
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String, int)
      */
     public void setAttributeValue(String attributeName, String value, int index) {
 
         if (m_entityAttributes.containsKey(attributeName)) {
             throw new RuntimeException("Attribute already exists with a simple type value.");
         }
         if (!m_simpleAttributes.containsKey(attributeName)) {
             if (index != 0) {
                 throw new IndexOutOfBoundsException();
             } else {
                 List<String> values = new ArrayList<String>();
                 values.add(value);
                 m_simpleAttributes.put(attributeName, values);
             }
         } else {
             m_simpleAttributes.get(attributeName).add(index, value);
         }
     }
 
     /**
      * @see com.alkacon.vie.shared.I_Entity#toJSON()
      */
     public String toJSON() {
 
         // TODO: Auto-generated method stub
         return null;
     }
 }
