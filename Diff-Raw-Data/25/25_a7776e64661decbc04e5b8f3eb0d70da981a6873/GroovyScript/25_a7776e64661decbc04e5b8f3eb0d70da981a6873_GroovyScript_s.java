 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.groovy;
 
 import java.util.Date;
 
 import org.openmrs.User;
 
 public class GroovyScript {
     Integer id;
     String name;
     String script;
     Date created;
     Date modified;
     User creator;
     User modifiedBy;
 
     public GroovyScript() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     public String getScript() {
         return script;
     }
 
     public void setScript(final String script) {
         this.script = script;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(final Date created) {
         this.created = created;
     }
 
     public Date getModified() {
         return modified;
     }
 
     public void setModified(final Date modified) {
         this.modified = modified;
     }
 
     public User getCreator() {
         return creator;
     }
 
     public void setCreator(final User creator) {
         this.creator = creator;
     }
 
     public User getModifiedBy() {
         return modifiedBy;
     }
 
     public void setModifiedBy(final User modifiedBy) {
         this.modifiedBy = modifiedBy;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         GroovyScript that = (GroovyScript) o;
 
         if (!created.equals(that.created)) return false;
         if (!creator.equals(that.creator)) return false;
         if (!id.equals(that.id)) return false;
         if (!modified.equals(that.modified)) return false;
         if (!modifiedBy.equals(that.modifiedBy)) return false;
         if (!name.equals(that.name)) return false;
         if (!script.equals(that.script)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = 7;
        result = 31 * result + name.hashCode();
        result = 31 * result + script.hashCode();
        result = 31 * result + created.hashCode();
         if (modified == null) {
         	result = 31 * result;
         } else {
         	result = 31 * result + modified.hashCode();
         }
        result = 31 * result + creator.hashCode();
         if (modifiedBy == null) {
         	result = 31 * result;
         } else {
         	result = 31 * result + modifiedBy.hashCode();
         }
         return result;
     }
 }
