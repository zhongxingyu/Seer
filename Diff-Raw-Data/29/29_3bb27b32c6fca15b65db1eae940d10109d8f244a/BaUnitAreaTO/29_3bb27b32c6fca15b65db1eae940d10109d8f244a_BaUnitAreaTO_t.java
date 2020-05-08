 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.boundary.transferobjects.administrative;
 import java.math.BigDecimal;
 import org.sola.services.common.contracts.AbstractIdTO;
 /**
  *
  * @author rizzom
  */
 public class BaUnitAreaTO extends AbstractIdTO {
     private String baUnitId;
     private BigDecimal size;
     private String typeCode;
    private BigDecimal calculatedAreaSize;
    
     
     public BaUnitAreaTO(){
         super();
     }
 
    
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public BigDecimal getSize() {
         return size;
     }
 
     public void setSize(BigDecimal size) {
         this.size = size;
     }
 
     public String getTypeCode() {
         return typeCode;
     }
 
     public void setTypeCode(String typeCode) {
         this.typeCode = typeCode;
     }
     
    public BigDecimal getCalculatedAreaSize() {
        return calculatedAreaSize;
    }

    public void setCalculatedAreaSize(BigDecimal calculatedAreaSize) {
        this.calculatedAreaSize = calculatedAreaSize;
    }
     
 }
