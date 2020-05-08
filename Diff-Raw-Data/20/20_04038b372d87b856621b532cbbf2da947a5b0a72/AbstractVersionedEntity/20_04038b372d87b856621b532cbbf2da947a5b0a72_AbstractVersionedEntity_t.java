 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.common.repository.entities;
 
 import javax.persistence.Column;
 import org.sola.services.common.LocalInfo;
 
 /**
  * Extends {@linkplain AbstractEntity} to provide fields common to versioned entities. This includes
  * the rowVersion, changeUser and rowId fields. Aso overrides {@linkplain #preSave}, 
  * {@linkplain #isNew} and {@linkplain #equals} methods. 
  * @author soladev
  */
 public abstract class AbstractVersionedEntity extends AbstractEntity {
 
     @Column(name = "rowversion")
     private int rowVersion;
     @Column(name = "change_user")
     private String changeUser;
     @Column(name = "rowidentifier")
     private String rowId;
 
     public AbstractVersionedEntity() {
         super();
     }
 
     public String getChangeUser() {
         return changeUser;
     }
 
     public void setChangeUser(String changeUser) {
         this.changeUser = changeUser;
     }
 
     public int getRowVersion() {
         return rowVersion;
     }
 
     public void setRowVersion(int rowVersion) {
         this.rowVersion = rowVersion;
     }
 
     public String getRowId() {
         rowId = rowId == null ? generateId() : rowId;
         return rowId;
     }
 
     public void setRowId(String rowId) {
         this.rowId = rowId;
     }
 
     /**
      * Flags if the entity has been saved to the database or not. This method overrides the default
      * isNew on {@linkplain AbstractReadOnlyEntity#isNew()} which checks the loaded flag. 
      * @return true if the entity rowversion == 0. 
      */
     @Override
     public Boolean isNew() {
         return getRowVersion() == 0;
     }
 
     /** 
      * Overrides the preSave method to set the rowVersion and changeUser details when the entity
      * is going to be saved to the database. Any descendent classes that override the preSave method
      * should ensure they call super.preSave() to execute this save functionality. 
     * Note that isModified returns true when the entity is new. 
      */
     @Override
     public void preSave() {
         super.preSave();
         if (isModified() || toDelete()) {
             if (toDelete() && !changeUser.equals(LocalInfo.getUserName())) {
                 setUpdateBeforeDelete(true);
             }
             changeUser = LocalInfo.getUserName();
         }
     }
 
     /*
      * Increment the row version during post save if the entity has been saved to the database
      * and the entity was not refreshed from the DB. 
      */
     @Override
     public void postSave() {
         super.postSave();
         if (!noAction() && !isForceRefresh()) {
             rowVersion++;
 
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         if (getIdColumns().size() == 1) {
             hash += (getEntityId() != null ? getEntityId().hashCode() : 0);
         } else {
             hash += (getRowId() != null ? getRowId().hashCode() : 0);
         }
         return hash;
     }
 
     /**
      * Overrides the default {@linkplain AbstractReadOnlyEntity#equals} method to perform equals
      * with the RowId rather than the id columns. This is because Dozer uses the equals method
      * to match transfer objects (TO's) to entity objects during translation of collections and 
      * lists. For OneToMany lists, most TO's omit the parent id value which means the default
      * {@linkplain AbstractReadOnlyEntity#equals} does not correctly match the TO to its existing
      * entity causing Dozer to create a new entity object rather than translating into the existing 
      * entity.
      * @param object The object to compare with this one
      * @return true if the RowIds match
      */
     @Override
     public boolean equals(Object object) {
         boolean result = false;
         if (object != null && object.getClass() == this.getClass()) {
             if (getIdColumns().size() == 1) {
                 String id = ((AbstractVersionedEntity) object).getEntityId();
                 result = this.getEntityId().equals(id);
             } else {
                 String objectRowId = ((AbstractVersionedEntity) object).getRowId();
                 result = this.getRowId().equals(objectRowId);
             }
         }
         return result;
     }
 }
