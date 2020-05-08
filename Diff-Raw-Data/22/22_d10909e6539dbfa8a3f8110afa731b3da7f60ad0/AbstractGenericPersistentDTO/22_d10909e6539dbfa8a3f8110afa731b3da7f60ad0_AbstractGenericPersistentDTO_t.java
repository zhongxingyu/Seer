 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.domain;
 
 import java.io.Serializable;
 
 /**
  *
  * @author imyousuf
  */
 public abstract class AbstractGenericPersistentDTO<Template extends PersistentDTO, IdType extends Comparable<IdType> & Serializable, VersionType extends Comparable<VersionType> & Serializable>
     implements
     PersistentDTO<PersistentDTO, IdType, VersionType> {
 
   private IdType id;
   private VersionType version;
 
   protected AbstractGenericPersistentDTO() {
   }
 
   @Override
   public IdType getId() {
     return id;
   }
 
   @Override
   public void setId(IdType id) {
     this.id = id;
   }
 
   @Override
   public VersionType getVersion() {
     return version;
   }
 
   @Override
   public void setVersion(VersionType version) {
     this.version = version;
   }
 
   @Override
   public int compareTo(PersistentDTO o) {
     if (o == null) {
       throw new IllegalArgumentException();
     }
    if (o.getId() == null && getId() == null) {
      return 0;
     }
    if (o.getId() == null && getId() != null) {
       return 1;
     }
    if (o.getId() != null && getId() == null) {
       return -1;
     }
    if (getId().getClass().isAssignableFrom(o.getId().getClass())) {
       IdType tmp = (IdType) o.getId();
      return tmp.compareTo(getId());
     }
     else {
      return o.getId().toString().compareTo(getId().toString());
     }
   }
 
   @Override
   public int compare(PersistentDTO o1, PersistentDTO o2) {
     if (o1 == null && o2 == null) {
       return 0;
     }
     if (o1 == null && o2 != null) {
       return -1;
     }
     if (o1 != null && o2 == null) {
       return 1;
     }
     return o1.compareTo(o2);
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null) {
       return false;
     }
     if (!PersistentDTO.class.isAssignableFrom(obj.getClass())) {
       return false;
     }
     final PersistentDTO other = (PersistentDTO) obj;
     final int compareTo = compareTo(other);
     return compareTo == 0;
   }
 
   @Override
   public int hashCode() {
     int hash = 3;
    hash = 23 * hash + (getId() != null ? getId().hashCode() : 0);
     return hash;
   }
 
   protected void clone(Template template) {
     if (template == null) {
       return;
     }
     template.setId(id);
     template.setVersion(version);
   }
 }
