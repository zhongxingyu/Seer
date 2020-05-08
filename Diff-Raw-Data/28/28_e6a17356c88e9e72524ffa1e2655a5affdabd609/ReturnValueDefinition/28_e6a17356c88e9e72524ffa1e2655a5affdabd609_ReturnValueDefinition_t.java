 package com.amee.domain.data;
 
 import com.amee.domain.AMEEEnvironmentEntity;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.ObjectType;
 import com.amee.domain.ValueDefinition;
 import com.amee.platform.science.AmountCompoundUnit;
 import com.amee.platform.science.AmountPerUnit;
 import com.amee.platform.science.AmountUnit;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "RETURN_VALUE_DEFINITION")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 @Configurable(autowire = Autowire.BY_TYPE)
 public class ReturnValueDefinition extends AMEEEnvironmentEntity {
 
     // TODO: Never allow nulls for the values. Encapsulate as with other entities. Provide default values. Protect for NPEs.
     // TODO: Are the unit size constants required?
 
     public static final int TYPE_MIN_SIZE = 1;
     public static final int TYPE_MAX_SIZE = 255;
     public static final int UNIT_MIN_SIZE = 1;
     public static final int UNIT_MAX_SIZE = 255;
     public static final int PER_UNIT_MIN_SIZE = 1;
     public static final int PER_UNIT_MAX_SIZE = 255;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "ITEM_DEFINITION_ID")
     private ItemDefinition itemDefinition;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "VALUE_DEFINITION_ID")
     private ValueDefinition valueDefinition;
 
     @Column(name = "TYPE")
     private String type;
 
     @Column(name = "UNIT")
     private String unit;
 
     @Column(name = "PER_UNIT")
     private String perUnit;
 
     @Column(name = "DEFAULT_TYPE")
     private boolean defaultType;
 
     public ReturnValueDefinition() {
         super();
     }
 
     public ReturnValueDefinition(ItemDefinition itemDefinition) {
         super(itemDefinition.getEnvironment());
         setItemDefinition(itemDefinition);
         itemDefinition.add(this);
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public AmountUnit getUnit() {
         return (unit != null) ? AmountUnit.valueOf(unit) : AmountUnit.ONE;
     }
 
     public void setUnit(AmountUnit unit) {
         this.unit = unit.toString();
     }
 
     public AmountPerUnit getPerUnit() {
         return (perUnit != null) ? AmountPerUnit.valueOf(perUnit) : AmountPerUnit.ONE;
     }
 
     public void setPerUnit(AmountPerUnit perUnit) {
         this.perUnit = perUnit.toString();
     }
 
     public boolean isDefaultType() {
         return defaultType;
     }
 
     public void setDefaultType(boolean isDefault) {
         this.defaultType = isDefault;
     }
 
     public ItemDefinition getItemDefinition() {
         return itemDefinition;
     }
 
     public void setItemDefinition(ItemDefinition itemDefinition) {
         this.itemDefinition = itemDefinition;
     }
 
     public ValueDefinition getValueDefinition() {
         return valueDefinition;
     }
 
     public void setValueDefinition(ValueDefinition valueDefinition) {
         this.valueDefinition = valueDefinition;
     }
 
     public ObjectType getObjectType() {
         return ObjectType.RVD;
     }
 
     @Override
     public boolean isTrash() {
        return status.equals(AMEEStatus.TRASH) || getItemDefinition().isTrash();
     }
 
     public AmountCompoundUnit getCompoundUnit() {
         return getUnit().with(getPerUnit());
     }
 }
