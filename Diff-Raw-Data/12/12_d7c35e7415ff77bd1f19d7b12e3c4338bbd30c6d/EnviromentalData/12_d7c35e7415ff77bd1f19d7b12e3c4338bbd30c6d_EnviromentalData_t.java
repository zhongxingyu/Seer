 /* Ara - capture species and specimen data
  *
  * Copyright (C) 2009  INBio (Instituto Nacional de Biodiversidad)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.inbio.ara.persistence.samplemanage;
 
 import java.util.Calendar;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import org.inbio.ara.persistence.LogGenericEntity;
 
 /**
  *
  * @author dasolano
  */
 @Entity
 @Table(name = "enviromental_data")
 public class EnviromentalData extends LogGenericEntity {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO, generator="enviromental_data")
     @SequenceGenerator(name="enviromental_data", sequenceName="enviromental_data_seq")
     @Basic(optional = false)
     @Column(name = "enviromental_data_id")
     private Long enviromentalDataId;
 
     @Id
     @Basic(optional = false)
     @Column(name = "sample_id")
     private Long sampleId;
 
     @Column(name = "elevation")
     private Long elevation;
 
     @Column(name = "ph")
     private Long ph;
     
     @Column(name = "luminosity")
     private Long luminosity;
 
     @Column(name = "tempeture")
     private Long tempeture;
 
     @Column(name = "moisture")
     private Long moisture;
 
     @Column(name = "habitat")
     private String habitat;
 
     @Column(name = "salinity")
     private Long salinity;
 
     @Column(name = "vertical_strata_id")
     private Long verticalStrataId;
 
     @Column(name = "vegetation_type_id")
     private Long vegetationTypeId;
 
    
 
     public EnviromentalData() {
     }
 
     public EnviromentalData(Long sampleId) {
         this.sampleId = sampleId;
     }
 
     public EnviromentalData(Long sampleId, Long enviromentalDataId, String createdBy, Calendar creationDate, String lastModificationBy, Calendar lastModificationDate) {
         this.sampleId = sampleId;
         this.enviromentalDataId = enviromentalDataId;
         this.setCreatedBy(createdBy);
         this.setCreationDate(creationDate);
         this.setLastModificationBy(lastModificationBy);
         this.setLastModificationDate(lastModificationDate);
     }
 
     public Long getEnviromentalDataId() {
         return enviromentalDataId;
     }
 
     public void setEnviromentalDataId(Long enviromentalDataId) {
         this.enviromentalDataId = enviromentalDataId;
     }
 
     public Long getSampleId() {
         return sampleId;
     }
 
     public void setSampleId(Long sampleId) {
         this.sampleId = sampleId;
     }
 
     public Long getElevation() {
         return elevation;
     }
 
     public void setElevation(Long elevation) {
         this.elevation = elevation;
     }
 
     public Long getPh() {
         return ph;
     }
 
     public void setPh(Long ph) {
         this.ph = ph;
     }
 
     public Long getLuminosity() {
         return luminosity;
     }
 
     public void setLuminosity(Long luminosity) {
         this.luminosity = luminosity;
     }
 
     public Long getTempeture() {
         return tempeture;
     }
 
     public void setTempeture(Long tempeture) {
         this.tempeture = tempeture;
     }
 
     public Long getMoisture() {
         return moisture;
     }
 
     public void setMoisture(Long moisture) {
         this.moisture = moisture;
     }
 
     public String getHabitat() {
         return habitat;
     }
 
     public void setHabitat(String habitat) {
         this.habitat = habitat;
     }
 
     public Long getSalinity() {
         return salinity;
     }
 
     public void setSalinity(Long salinity) {
         this.salinity = salinity;
     }
 
     
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (sampleId != null ? sampleId.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof EnviromentalData)) {
             return false;
         }
         EnviromentalData other = (EnviromentalData) object;
         if ((this.sampleId == null && other.sampleId != null) || (this.sampleId != null && !this.sampleId.equals(other.sampleId))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "org.inbio.ara.persistence.samplemanage.EnviromentalData[sampleId=" + sampleId + "]";
     }
    
 
     /**
      * @return the verticalStrataId
      */
     public Long getVerticalStrataId() {
         return verticalStrataId;
     }
 
     /**
      * @param verticalStrataId the verticalStrataId to set
      */
     public void setVerticalStrataId(Long verticalStrataId) {
         this.verticalStrataId = verticalStrataId;
     }
 
     /**
      * @return the vegetationTypeId
      */
     public Long getVegetationTypeId() {
         return vegetationTypeId;
     }
 
     /**
      * @param vegetationTypeId the vegetationTypeId to set
      */
     public void setVegetationTypeId(Long vegetationTypeId) {
         this.vegetationTypeId = vegetationTypeId;
     }
 
 }
