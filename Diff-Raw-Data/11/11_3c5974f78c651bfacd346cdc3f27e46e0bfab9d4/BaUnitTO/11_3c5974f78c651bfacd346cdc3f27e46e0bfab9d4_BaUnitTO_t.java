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
 package org.sola.services.boundary.transferobjects.administrative;
 
import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import org.sola.services.boundary.transferobjects.cadastre.CadastreObjectTO;
 import org.sola.services.boundary.transferobjects.casemanagement.SourceTO;
 import org.sola.services.common.contracts.AbstractIdTO;
 
 public class BaUnitTO extends AbstractIdTO {
     
     private String typeCode;
     private String name;
     private String nameFirstpart;
     private String nameLastpart;
     private String statusCode;
     private boolean locked;
     private List<RrrTO> rrrList;
     private List<BaUnitNotationTO> baUnitNotationList;
     private List<CadastreObjectTO> cadastreObjectList;
     private List<SourceTO> sourceList;
     private List<RelatedBaUnitInfoTO> childBaUnits;
     private List<RelatedBaUnitInfoTO> parentBaUnits;
     private String pendingActionCode;
    private BigDecimal calculatedAreaSize;

    public BigDecimal getCalculatedAreaSize() {
        return calculatedAreaSize;
    }

    public void setCalculatedAreaSize(BigDecimal calculatedAreaSize) {
        this.calculatedAreaSize = calculatedAreaSize;
    }
     
     public BaUnitTO(){
         super();
     }
 
     public void addSource(SourceTO source) {
         if (sourceList == null) {
             sourceList = new ArrayList<SourceTO>();
         }
         sourceList.add(source);
     }
     
     public void addRrr(RrrTO rrr) {
         if (rrrList == null) {
             rrrList = new ArrayList<RrrTO>();
         }
         rrrList.add(rrr);
     }
     
     public void addCadastreObjectList(CadastreObjectTO cadastreObject) {
         if (cadastreObjectList == null) {
             cadastreObjectList = new ArrayList<CadastreObjectTO>();
         }
         cadastreObjectList.add(cadastreObject);
     }
     
     public List<BaUnitNotationTO> getBaUnitNotationList() {
         return baUnitNotationList;
     }
 
     public void setBaUnitNotationList(List<BaUnitNotationTO> baUnitNotationList) {
         this.baUnitNotationList = baUnitNotationList;
     }
       
     public List<CadastreObjectTO> getCadastreObjectList() {
         return cadastreObjectList;
     }
 
     public void setCadastreObjectList(List<CadastreObjectTO> cadastreObjectList) {
         this.cadastreObjectList = cadastreObjectList;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getNameFirstpart() {
         return nameFirstpart;
     }
 
     public void setNameFirstpart(String nameFirstpart) {
         this.nameFirstpart = nameFirstpart;
     }
 
     public String getNameLastpart() {
         return nameLastpart;
     }
 
     public void setNameLastpart(String nameLastpart) {
         this.nameLastpart = nameLastpart;
     }
 
     public List<RrrTO> getRrrList() {
         return rrrList;
     }
 
     public void setRrrList(List<RrrTO> rrrList) {
         this.rrrList = rrrList;
     }
 
     public List<SourceTO> getSourceList() {
         return sourceList;
     }
 
     public void setSourceList(List<SourceTO> sourceList) {
         this.sourceList = sourceList;
     }
 
     public List<RelatedBaUnitInfoTO> getChildBaUnits() {
         return childBaUnits;
     }
 
     public void setChildBaUnits(List<RelatedBaUnitInfoTO> childBaUnits) {
         this.childBaUnits = childBaUnits;
     }
 
     public List<RelatedBaUnitInfoTO> getParentBaUnits() {
         return parentBaUnits;
     }
 
     public void setParentBaUnits(List<RelatedBaUnitInfoTO> parentBaUnits) {
         this.parentBaUnits = parentBaUnits;
     }
     
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public String getTypeCode() {
         return typeCode;
     }
 
     public void setTypeCode(String typeCode) {
         this.typeCode = typeCode;
     }
 
     public boolean isLocked() {
         return locked;
     }
 
     public void setLocked(boolean locked) {
         this.locked = locked;
     }
 
     public String getPendingActionCode() {
         return pendingActionCode;
     }
 
     public void setPendingActionCode(String pendingActionCode) {
         this.pendingActionCode = pendingActionCode;
     }
     
 }
