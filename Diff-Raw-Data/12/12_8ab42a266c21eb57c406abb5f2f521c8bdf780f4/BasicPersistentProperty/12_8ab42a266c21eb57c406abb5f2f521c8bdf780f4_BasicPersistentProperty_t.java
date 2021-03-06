 /*
  * Copyright (c) 2011 by the original author(s).
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.springframework.data.mapping;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.annotation.Transient;
 import org.springframework.data.mapping.model.Association;
 import org.springframework.data.mapping.model.PersistentEntity;
 import org.springframework.data.mapping.model.PersistentProperty;
 import org.springframework.data.util.TypeInformation;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Jon Brisbin <jbrisbin@vmware.com>
  */
 public class BasicPersistentProperty implements PersistentProperty {
 
   protected final String name;
   protected final PropertyDescriptor propertyDescriptor;
   protected final TypeInformation information;
   protected final Field field;
   protected Association association;
   protected Value value;
   protected boolean isTransient = false;
   protected PersistentEntity<?> owner;
 
   public BasicPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, TypeInformation information) {
     this.name = field.getName();
     this.information = information.getProperty(this.name);
     this.propertyDescriptor = propertyDescriptor;
     this.field = field;
     this.isTransient = Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(Transient.class);
     if (field.isAnnotationPresent(Value.class)) {
       this.value = field.getAnnotation(Value.class);
       // Fields with @Value annotations are considered the same as transient fields
       this.isTransient = true;
     }
     if (field.isAnnotationPresent(Autowired.class)) {
       this.isTransient = true;
     }
   }
 
   @Override
   public Object getOwner() {
     return owner;
   }
 
   @Override
   public void setOwner(Object owner) {
     if (null != owner && owner.getClass().isAssignableFrom(PersistentEntity.class)) {
       this.owner = (PersistentEntity<?>) owner;
     }
   }
 
   @Override
   public String getName() {
     return name;
   }
 
   @Override
   public Class<?> getType() {
     return information.getType();
   }
   
   @Override
   public TypeInformation getTypeInformation() {
     return information;
   }
 
   @Override
   public PropertyDescriptor getPropertyDescriptor() {
     return propertyDescriptor;
   }
 
   @Override
   public Field getField() {
     return field;
   }
 
   @Override
   public Value getValueAnnotation() {
     return value;
   }
 
   @Override
   public boolean isTransient() {
     return isTransient;
   }
 
   @Override
   public boolean isAssociation() {
     return null != association;
   }
 
   @Override
   public Association getAssociation() {
     return association;
   }
 
   @Override
   public void setAssociation(Association association) {
     this.association = association;
   }
 
   @Override
   public boolean isCollection() {
    return Collection.class.isAssignableFrom(getType()) || isArray();
   }
   
   
   @Override
   public boolean isMap() {
    return Map.class.isAssignableFrom(getType());
   }
   
   /* (non-Javadoc)
    * @see org.springframework.data.mapping.model.PersistentProperty#isArray()
    */
   @Override
   public boolean isArray() {
     return getType().isArray();
   }
 
   @Override
   public boolean isComplexType() {
     if (isCollection() || isArray()) {
       return !MappingBeanHelper.isSimpleType(getComponentType());
     } else {
       return !MappingBeanHelper.isSimpleType(getType());
     }
   }
 
   @Override
   public Class<?> getComponentType() {
     if (isCollection()) {
       Type genericType = field.getGenericType();
       if (genericType instanceof ParameterizedType) {
         Type[] genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
         for (Type t : genericTypes) {
           if (t instanceof Class) {
             return (Class<?>) t;
           }
         }
       }
     }
     return getType().getComponentType();
   }
 
   @Override
   public boolean isIdProperty() {
     return field.isAnnotationPresent(Id.class);
   }
 }
