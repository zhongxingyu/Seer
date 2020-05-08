 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.metafields.db;
 
 import com.sapienter.jbilling.server.metafields.db.value.BooleanMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.DateMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.DecimalMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.IntegerMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.JsonMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.ListMetaFieldValue;
 import com.sapienter.jbilling.server.metafields.db.value.StringMetaFieldValue;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Version;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 
 /**
  * A meta-field name that is associated with a particular entity type. The field names define
  * the allowed values and data-types of the values that can be attached to an entity.
  *
  * @author Brian Cowdery
  * @since 03-Oct-2011
  */
 @Entity
 @Table(name = "meta_field_name")
 @TableGenerator(
     name = "meta_field_GEN",
     table = "jbilling_seqs",
     pkColumnName = "name",
     valueColumnName = "next_id",
     pkColumnValue = "meta_field_name",
     allocationSize = 10
 )
 public class MetaField implements Serializable {
 
     private Integer id;
     private CompanyDTO entity;
     private String name;
     private EntityType entityType;
     private DataType dataType;
 
     private boolean disabled = false;
     private boolean mandatory = false;
     private Integer displayOrder = 1;
     private MetaFieldValue defaultValue = null;
 
     private Integer versionNum;
     
     public MetaField() {
 	}
 
 	@Id
     @GeneratedValue(strategy = GenerationType.TABLE, generator = "meta_field_GEN")
     @Column(name = "id", unique = true, nullable = false)
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
     
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "entity_id")
     public CompanyDTO getEntity() {
         return this.entity;
     }
 
     public void setEntity(CompanyDTO entity) {
         this.entity = entity;
     }
 
     @Column(name = "name", nullable = false)
     @NotNull(message="validation.error.notnull")
    @Size(min = 1, max = 100, message = "validation.metaField.name.error.size,1,100")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Enumerated(EnumType.STRING)
     @Column(name = "entity_type", nullable = false, length = 25)
     public EntityType getEntityType() {
         return entityType;
     }
 
     public void setEntityType(EntityType entityType) {
         this.entityType = entityType;
     }
 
     @Enumerated(EnumType.STRING)
     @Column(name = "data_type", nullable = false, length = 25)
     public DataType getDataType() {
         return dataType;
     }
 
     public void setDataType(DataType dataType) {
         this.dataType = dataType;
     }
 
     @Column(name = "is_disabled", nullable = true)
     public boolean isDisabled() {
         return disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     @Column(name = "is_mandatory", nullable = true)
     public boolean isMandatory() {
         return mandatory;
     }
 
     public void setMandatory(boolean mandatory) {
         this.mandatory = mandatory;
     }
 
     @Column(name = "display_order", nullable = true)
     public Integer getDisplayOrder() {
         return displayOrder;
     }
 
     public void setDisplayOrder(Integer displayOrder) {
         this.displayOrder = displayOrder;
     }
 
     @Valid
     @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     @JoinColumn(name = "default_value_id", nullable = true)
     public MetaFieldValue getDefaultValue() {
         return defaultValue;
     }
 
     public void setDefaultValue(MetaFieldValue defaultValue) {
         if (defaultValue != null)
             defaultValue.setField(this);
 
         this.defaultValue = defaultValue;
     }
 
     public MetaFieldValue createValue() {
         switch (getDataType()) {
             case STRING:
                 return new StringMetaFieldValue(this);
 
             case INTEGER:
                 return new IntegerMetaFieldValue(this);
 
             case DECIMAL:
                 return new DecimalMetaFieldValue(this);
 
             case BOOLEAN:
                 return new BooleanMetaFieldValue(this);
 
             case DATE:
                 return new DateMetaFieldValue(this);
 
             case JSON_OBJECT:
                 return new JsonMetaFieldValue(this);
 
             case ENUMERATION:
                 return new StringMetaFieldValue(this);
             
             case LIST:
                 return new ListMetaFieldValue(this);
         }
 
         return null;
     }
 
     @Version
     @Column(name="OPTLOCK")
     public Integer getVersionNum() {
         return versionNum;
     }
     public void setVersionNum(Integer versionNum) {
         this.versionNum = versionNum;
     }
 
     @Override
     public String toString() {
         return "MetaField{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", entityType=" + entityType +
                ", dataType=" + dataType +
                '}';
     }
 }
