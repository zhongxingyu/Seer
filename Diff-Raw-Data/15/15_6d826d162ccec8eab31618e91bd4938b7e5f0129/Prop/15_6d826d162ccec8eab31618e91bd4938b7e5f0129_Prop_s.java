 package org.zoneproject.extractor.utils;
 
 /*
  * #%L
  * ZONE-utils
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class Prop {
     private static final long serialVersionUID = 1L;
     private Property type=null;
    private String value=null;
     private boolean isLiteral;
     private boolean isSearchable=false;
 
     public Prop(Property t, String value){
         this(t,value,true,false);
     }
 
     public Prop(String type, String value) {
         this(type,value,true);
     }
     public Prop(String type, String value, boolean isLi) {
         this(type,value,isLi,false);
     }
     public Prop(String t, String value, boolean isLi, boolean isSearchable){
         this(ResourceFactory.createProperty(t),value,isLi,isSearchable);
     }
     
     public Prop(Property t, String value, boolean isLi, boolean isS){
         this.type = t;
        this.value= value;
         this.isLiteral = isLi;
         this.isSearchable = isS;
     }
     
     public Property getType() {
         return type;
     }
 
     public void setType(Property type) {
         this.type = type;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public boolean isLiteral() {
         return isLiteral;
     }
 
     public void setLiteral(boolean isLiteral) {
         this.isLiteral = isLiteral;
     }
 
     public boolean isIsLiteral() {
         return isLiteral;
     }
 
     public void setIsLiteral(boolean isLiteral) {
         this.isLiteral = isLiteral;
     }
 
     public boolean isIsSearchable() {
         return isSearchable;
     }
 
     public void setIsSearchable(boolean isSearchable) {
         this.isSearchable = isSearchable;
     }
     
     @Override
     public String toString(){
         return "Prop("+this.getType()+","+this.getValue()+","+this.isLiteral()+")";
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Prop other = (Prop) obj;
         if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
             return false;
         }
         if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
             return false;
         }
         if (this.isLiteral != other.isLiteral) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 31 * hash + (this.type != null ? this.type.hashCode() : 0);
         hash = 31 * hash + (this.value != null ? this.value.hashCode() : 0);
         hash = 31 * hash + (this.isLiteral ? 1 : 0);
         return hash;
     }
 }
