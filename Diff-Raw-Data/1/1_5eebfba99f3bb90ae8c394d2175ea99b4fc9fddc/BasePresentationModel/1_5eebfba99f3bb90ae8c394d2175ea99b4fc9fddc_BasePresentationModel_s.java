 /*
  * Copyright 2012 Canoo Engineering AG.
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
 
 package com.canoo.dolphin.core;
 
 import groovy.lang.MissingPropertyException;
 import groovy.util.Eval;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A BasePresentationModel is a collection of {@link BaseAttribute}s.
  * PresentationModels are not meant to be extended for the normal use, i.e. you typically don't need something like
  * a specialized "PersonPresentationModel" or so.
  */
 
 public class BasePresentationModel extends AbstractObservable implements PresentationModel {
     protected final List<Attribute> attributes = new LinkedList<Attribute>();
     private final String id;
     private String presentationModelType;
     private boolean dirty = false;
 
     protected final PropertyChangeListener DIRTY_FLAG_CHECKER = new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
             for (Attribute attr : attributes) {
                 if (attr.getTag() == Tag.VALUE && attr.isDirty()) {
                     setDirty(true);
                     return;
                 }
             }
             setDirty(false);
         }
     };
 
     /**
      * @throws AssertionError if the list of attributes is null or empty  *
      */
     public BasePresentationModel(List<Attribute> attributes) {
         this(null, attributes);
     }
 
     /**
      * @throws AssertionError if the list of attributes is null or empty
      */
     public BasePresentationModel(String id, List<? extends Attribute> attributes) {
         this.id = id != null ? id : makeId(this);
         for (Attribute attr : attributes) {
             addAttribute(attr);
         }
     }
 
     public void addAttribute(Attribute attribute) {
         if (null == attribute || attributes.contains(attribute)) return;
         attributes.add(attribute);
         if (attribute.getTag() == Tag.VALUE) { // only promote value changes as dirty upwards
             attribute.addPropertyChangeListener(Attribute.DIRTY_PROPERTY, DIRTY_FLAG_CHECKER);
         }
     }
 
     public String getId() {
         return id;
     }
 
     public String getPresentationModelType() {
         return presentationModelType;
     }
 
     public void setPresentationModelType(String presentationModelType) {
         this.presentationModelType = presentationModelType;
     }
 
     @Override
     public boolean isDirty() {
         return dirty;
     }
 
     public void setDirty(boolean dirty) {
         firePropertyChange(DIRTY_PROPERTY, this.dirty, this.dirty = dirty);
     }
 
     public void reset() {
         for (Attribute attr : attributes) {
             attr.reset();
         }
     }
 
     /**
      * @return the immutable internal representation
      */
     public List<Attribute> getAttributes() {
         return Collections.unmodifiableList(attributes);
     }
 
     protected static String makeId(PresentationModel instance) {
         return String.valueOf(System.identityHashCode(instance));
     }
 
     public Attribute getAt(String propertyName) {
         return findAttributeByPropertyName(propertyName);
     }
 
     // todo dk: overload with types for defaultValue
 
     /**
      * Convenience method to get the value of an attribute if it exists or a default value otherwise.
      */
     public int getValue(String attributeName, int defaultValue) {
         Attribute attribute = getAt(attributeName);
         Object attributeValue = (attribute == null) ? null : attribute.getValue();
         return (attributeValue == null) ? defaultValue : Integer.parseInt(attributeValue.toString());
     }
 
     public Attribute getAt(String propertyName, Tag tag) {
         return findAttributeByPropertyNameAndTag(propertyName, tag);
     }
 
     public Attribute findAttributeByPropertyName(String propertyName) {
         return findAttributeByPropertyNameAndTag(propertyName, Tag.VALUE);
     }
 
     public Attribute findAttributeByPropertyNameAndTag(String propertyName, Tag tag) {
         if (null == propertyName) return null;
         if (null == tag) return null;
         for (Attribute attribute : attributes) {
             if (propertyName.equals(attribute.getPropertyName()) && tag.equals(attribute.getTag())) {
                 return attribute;
             }
         }
         return null;
     }
 
     public Attribute findAttributeByQualifier(String qualifier) {
         if (null == qualifier) return null;
         for (Attribute attribute : attributes) {
             if (qualifier.equals(attribute.getQualifier())) {
                 return attribute;
             }
         }
         return null;
     }
 
     public Attribute findAttributeById(long id) {
         for (Attribute attribute : attributes) {
             if (attribute.getId() == id) {
                 return attribute;
             }
         }
         return null;
     }
 
     public Object propertyMissing(String propName) {
         Attribute result = findAttributeByPropertyName(propName);
         if (null == result) {
             String message = "The presentation model doesn't understand '" + propName + "'. \n";
             message += "Known attribute names are: " + Eval.x(attributes, "x.collect{it.propertyName}");
             throw new MissingPropertyException(message, propName, this.getClass());
         }
         return result;
     }
 
    // todo dk: also sync tag attributes
     public void syncWith(PresentationModel sourcePresentationModel) {
         for (Attribute targetAttribute : attributes) {
             Attribute sourceAttribute = sourcePresentationModel.getAt(targetAttribute.getPropertyName(), targetAttribute.getTag());
             if (sourceAttribute != null) targetAttribute.syncWith(sourceAttribute);
         }
     }
 
 }
