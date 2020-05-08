 package org.sola.services.ejb.administrative.repository.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.sola.services.common.repository.ChildEntity;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 
 @Table(schema = "administrative", name = "required_relationship_baunit")
 public class ChildBaUnitInfo extends AbstractVersionedEntity {
     @Id
     @Column(name = "from_ba_unit_id")
     private String baUnitId;
     @Id
     @Column(name = "to_ba_unit_id")
     private String relatedBaUnitId;
     @Column(name = "relation_code")
     private String relationCode;
    @ChildEntity(childIdField = "relatedBaUnitId", readOnly=true)
     private BaUnitBasic relatedBaUnit;
     
     public ChildBaUnitInfo(){
         super();
     }
 
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public BaUnitBasic getRelatedBaUnit() {
         return relatedBaUnit;
     }
 
     public void setRelatedBaUnit(BaUnitBasic relatedBaUnit) {
         this.relatedBaUnit = relatedBaUnit;
     }
 
     public String getRelatedBaUnitId() {
         return relatedBaUnitId;
     }
 
     public void setRelatedBaUnitId(String relatedBaUnitId) {
         this.relatedBaUnitId = relatedBaUnitId;
     }
 
     public String getRelationCode() {
         return relationCode;
     }
 
     public void setRelationCode(String relationCode) {
         this.relationCode = relationCode;
     }
 } 
