 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.resstore;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlValue;
 
 /**
 * This class represents a simple key and value pair called attribute.
  */
 public class Attribute {
 
     private String name;
     private String value;
 
     /**
      * Creates a new empty attribute.
      */
     public Attribute() {
 
     }
 
     /**
      * Creates a new attribute and sets the name and the value.
      * 
      * @param name The name for the new attribute.
      * @param value The value for the new attribute.
      */
     public Attribute(String name, String value) {
 
         this.name = name;
         this.value = value;
     }
 
     /**
      * Returns the name (or the key) of the attribute.
      * 
      * @return The name (or the key) of the attribute.
      */
     @XmlAttribute
     public String getName() {
 
         return name;
     }
 
     /**
      * Sets the name (or the key) of the attribute.
      * 
      * @param name The new name (or the key) for the attribute.
      */
     public void setName(String name) {
 
         this.name = name;
     }
 
     /**
      * Returns the value of the attribute.
      * 
      * @return The value of the attribute.
      */
     @XmlValue
     public String getValue() {
 
         return value;
     }
 
     /**
      * Sets the value of the attribute.
      * 
      * @param value The new value for the attribute.
      */
     public void setValue(String value) {
 
         this.value = value;
     }
 
 }
