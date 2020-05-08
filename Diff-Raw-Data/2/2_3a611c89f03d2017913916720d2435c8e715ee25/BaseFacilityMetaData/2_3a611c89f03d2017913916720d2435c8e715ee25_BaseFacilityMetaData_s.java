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
 package org.openmrs.module.facilitydata.model;
 
 import org.openmrs.BaseOpenmrsMetadata;
 
 /**
  * Base Object for the module, which provides some base implementation details
  */
 public abstract class BaseFacilityMetaData extends BaseOpenmrsMetadata {
 
 	//***** PROPERTIES *****
 	
     private Integer id;
 
     //***** CONSTRUCTORS *****
     
     public BaseFacilityMetaData() {}
 
     //***** PROPERTY ACCESS *****
     
     /**
 	 * @return the id
 	 */
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
      * @see Object#toString()
      */
     @Override
     public String toString() {
         if (getName() == null) {
             return super.toString();
         }
         return getName();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (this.getClass().isAssignableFrom(o.getClass())) {
         	BaseFacilityMetaData that = (BaseFacilityMetaData)o;
         	return this.getId() != null && this.getId().equals(that.getId());
         }
         return false;
     }
 
     @Override
     public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
         return result;
     }
 }
