 /*
  * Copyright (C) 2013 The Cat Hive Developers.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cathive.fx.credits;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlIDREF;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import javafx.beans.property.ListProperty;
 import javafx.beans.property.SimpleListProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 
 /**
  * 
  * @author Benjamin P. Jung
  */
@XmlType(name = "section", namespace = "http://www.cathive.com/fx/credits/", propOrder = {
         "id", "name", "persons_JAXB"
 })
 public final class Section implements Serializable {
 
     /** @see java.io.Serializable */
     private static final long serialVersionUID = -8338601230292338217L;
 
     private final StringProperty id = new SimpleStringProperty();
     private final StringProperty name = new SimpleStringProperty();
     private final ListProperty<Person> persons = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<Person>()));
 
     @XmlID
     @XmlAttribute(name="id")
     public String getId() {
         return this.id.get();
     }
 
     public void setId(final String id) {
         this.id.set(id);
     }
 
     public StringProperty idProperty() {
         return this.id;
     }
 
     @XmlElement(name = "name", required = true)
     public String getName() {
         return this.name.get();
     }
 
     public void setName(final String name) {
         this.name.set(name);
     }
 
     public StringProperty nameProperty() {
         return this.name;
     }
 
     @XmlTransient
     public ObservableList<Person> getPersons() {
         return this.persons.get();
     }
 
     public void setPersons(final ObservableList<Person> persons) {
         this.persons.set(persons);
     }
 
     public ListProperty<Person> personsProperty() {
         return this.persons;
     }
 
     /** Helper method for JAXB XML deserialization */
     @XmlElement(name = "person")
     @XmlElementWrapper(name = "persons")
     @XmlIDREF
     protected List<Person> getPersons_JAXB() {
         return this.persons.get();
     }
 
     /** Helper method for JAXB XML serialization */
     protected void setComponents_JAXB(final List<Person> persons) {
         this.persons.set(FXCollections.observableList(persons));
     }
 
     @Override
     public String toString() {
         return this.getName();
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o == null) {
            return false;
         }  
         if (getClass() != o.getClass()) {
            return false;
         }
         final Section that = (Section) o;
         return Objects.equals(this.getId(), that.getPersons())
             && Objects.equals(this.getName(), that.getName())
             && Objects.deepEquals(this.getPersons(), that.getPersons());
     }
 
     @Override
     public int hashCode() {
         return Objects.hash(this.getId(), this.getName(), this.getPersons());
     }
 
 }
