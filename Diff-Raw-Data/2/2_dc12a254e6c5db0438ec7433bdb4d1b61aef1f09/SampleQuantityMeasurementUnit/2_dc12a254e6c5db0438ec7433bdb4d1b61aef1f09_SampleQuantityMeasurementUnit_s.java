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
 import org.inbio.ara.dto.inventory.SelectionListEntity;
 import org.inbio.ara.persistence.SelectionListGenericEntity;
 
 /**
  *
  * @author dasolano
  */
 @Entity
@Table(name = "forest_type")
 public class SampleQuantityMeasurementUnit extends SelectionListGenericEntity {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO, generator="sample_quantity_measurement_unit")
     @SequenceGenerator(name="sample_quantity_measurement_unit", sequenceName="sample_quantity_measurement_unit_seq")
     @Basic(optional = false)
     @Column(name = "sample_quantity_measurement_unit_id")
     private Long sampleQuantityMeasurementUnitId;
 
 
     public SampleQuantityMeasurementUnit() {
     }
 
     public SampleQuantityMeasurementUnit(Long sampleQuantityMeasurementUnitId) {
         this.sampleQuantityMeasurementUnitId = sampleQuantityMeasurementUnitId;
     }
 
     public SampleQuantityMeasurementUnit(Long sampleQuantityMeasurementUnitId, String name, String createdBy, Calendar creationDate, String lastModificationBy, Calendar lastModificationDate) {
         this.sampleQuantityMeasurementUnitId = sampleQuantityMeasurementUnitId;
         this.setName(name);
         this.setCreatedBy(createdBy);
         this.setCreationDate(creationDate);
         this.setLastModificationBy(lastModificationBy);
         this.setLastModificationDate(lastModificationDate);
     }
 
     /**
      * @return the sampleQuantityMeasurementUnitId
      */
     public Long getSampleQuantityMeasurementUnitId() {
         return sampleQuantityMeasurementUnitId;
     }
 
     /**
      * @param sampleQuantityMeasurementUnitId the sampleQuantityMeasurementUnitId to set
      */
     public void setSampleQuantityMeasurementUnitId(Long sampleQuantityMeasurementUnitId) {
         this.sampleQuantityMeasurementUnitId = sampleQuantityMeasurementUnitId;
     }
 
     @Override
     public Long getId() {
         return this.sampleQuantityMeasurementUnitId;
     }
 
     @Override
     public void setId(Long id) {
         this.sampleQuantityMeasurementUnitId = id;
     }
 
     @Override
     public SelectionListEntity getSelectionListEntity() {
         return SelectionListEntity.SAMPLE_QUANTITY_MEASUREMENT_UNIT;
     }
 
 
     
 
 }
