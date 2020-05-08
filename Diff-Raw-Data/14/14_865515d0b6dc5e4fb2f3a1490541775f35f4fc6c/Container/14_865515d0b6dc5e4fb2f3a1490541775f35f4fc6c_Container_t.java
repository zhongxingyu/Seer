 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.ocsys.domain;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author Carlos Juarez
  */
 @Entity
 @Table(name="CONTAINERS")
 public class Container implements Serializable{
     
     @Id
     @Column(name="CONTAINER_ID", length=20)
     private String id;
     
    @Column(name="LTS_FILL_CAPACITY", precision=8, scale=2, nullable=false)
     private BigDecimal ltsFillCapacity;
     
    @Column(name="LTS_FULL_CAPACITY", precision=8, scale=2, nullable=false)
     private BigDecimal ltsFullCapacity;
     
     @Column(name="TARE_WGT", precision=8, scale=2, nullable=false)
     private BigDecimal tareWeight;
     
     @Enumerated(EnumType.STRING)
     @Column(name="CONTAINER_TYPE", length=8, nullable=false)
     private ContainerType type;
     
     public Container(){
         this(null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
     }
 
     public Container(String id, BigDecimal ltsFillCapacity, BigDecimal ltsFullCapacity, BigDecimal tareWeight){
         this.id = id;
         this.ltsFillCapacity = ltsFillCapacity;
         this.ltsFullCapacity = ltsFullCapacity;
         this.tareWeight = tareWeight;
         type = ContainerType.ISO;
     }
     
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public BigDecimal getLtsFillCapacity() {
         return ltsFillCapacity;
     }
 
     public void setLtsFillCapacity(BigDecimal ltsFillCapacity) {
         this.ltsFillCapacity = ltsFillCapacity;
     }
 
     public BigDecimal getLtsFullCapacity() {
         return ltsFullCapacity;
     }
 
     public void setLtsFullCapacity(BigDecimal ltsFullCapacity) {
         this.ltsFullCapacity = ltsFullCapacity;
     }
 
     public BigDecimal getTareWeight() {
         return tareWeight;
     }
 
     public void setTareWeight(BigDecimal tareWeight) {
         this.tareWeight = tareWeight;
     }
 
     public ContainerType getType() {
         return type;
     }
 
     public void setType(ContainerType type) {
         this.type = type;
     }
     
 }
