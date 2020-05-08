 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.armyeditor.entrys;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 /**
  *
  * @author dmitry
  */
 public class Venicle implements java.io.Serializable{
    private Long id;
     private VenicleBase unit;
     private WarGear warGear;
      @Id @javax.persistence.GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     @ManyToOne(cascade = {CascadeType.REFRESH})
     @JoinColumn(name = "Venicle_fk")
     public VenicleBase getUnit() {
         return unit;
     }
 
     public void setUnit(VenicleBase unit) {
         this.unit = unit;
     }
 
     public WarGear getWarGear() {
         return warGear;
     }
 
     public void setWarGear(WarGear warGear) {
         this.warGear = warGear;
     }
     
     
 }
