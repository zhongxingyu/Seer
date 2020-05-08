 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
  * and the Lesotho Land Administration Authority (LAA). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
  *       endorse or promote products derived from this software without specific prior
  * 	  written permission.
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
 import java.util.Date;
 import java.util.List;
 import org.sola.services.boundary.transferobjects.casemanagement.PartyTO;
 import org.sola.services.boundary.transferobjects.casemanagement.SourceTO;
 import org.sola.services.common.contracts.AbstractIdTO;
 
 public class RrrTO extends AbstractIdTO {
     
     private String baUnitId;
     private String typeCode;
     private String nr;
     private String statusCode;
     private boolean primary;
     private Date registrationDate;
     private String transactionId;
     private Date expirationDate;
     private Double share;
     private BigDecimal amount;
     private Date dueDate;
     private BigDecimal mortgageInterestRate;
     private Integer mortgageRanking;
     private String mortgageTypeCode;
     private boolean locked;
     private List<SourceTO> sourceList;
     private List<RrrShareTO> rrrShareList;
     private BaUnitNotationTO notation;
     private List<PartyTO> rightHolderList;
     private String registrationNumber;
     private Date statusChangeDate;
     private String landUseCode;
     private BigDecimal landUsable;
     private BigDecimal personalLevy;
     private String cadastreObjectId;
     private BigDecimal groundRent;
     private Date startDate;
     private Date executionDate;
     private String leaseNumber;
     private BigDecimal stampDuty;
     private BigDecimal transferDuty;
     private BigDecimal registrationFee;
     private List<LeaseSpecialConditionTO> leaseSpecialConditionList;
       
     public RrrTO(){
         super();
     }
 
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     public void setExpirationDate(Date expirationDate) {
         this.expirationDate = expirationDate;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public void setAmount(BigDecimal mortgageAmount) {
         this.amount = mortgageAmount;
     }
 
     public Date getDueDate() {
         return dueDate;
     }
 
     public void setDueDate(Date dueDate) {
         this.dueDate = dueDate;
     }
 
     public BigDecimal getMortgageInterestRate() {
         return mortgageInterestRate;
     }
 
     public void setMortgageInterestRate(BigDecimal mortgageInterestRate) {
         this.mortgageInterestRate = mortgageInterestRate;
     }
 
     public Integer getMortgageRanking() {
         return mortgageRanking;
     }
 
     public void setMortgageRanking(Integer mortgageRanking) {
         this.mortgageRanking = mortgageRanking;
     }
 
     public String getMortgageTypeCode() {
         return mortgageTypeCode;
     }
 
     public void setMortgageTypeCode(String mortgageTypeCode) {
         this.mortgageTypeCode = mortgageTypeCode;
     }
 
     public BaUnitNotationTO getNotation() {
         return notation;
     }
 
     public void setNotation(BaUnitNotationTO notation) {
         this.notation = notation;
     }
 
     public String getNr() {
         return nr;
     }
 
     public void setNr(String nr) {
         this.nr = nr;
     }
 
     public List<PartyTO> getRightHolderList() {
         return rightHolderList;
     }
 
     public void setRightHolderList(List<PartyTO> rightHolderList) {
         this.rightHolderList = rightHolderList;
     }
 
     public void addRightHolder(PartyTO partySummaryTO) {
         if (rightHolderList == null) {
             rightHolderList = new ArrayList<PartyTO>();
         }
         rightHolderList.add(partySummaryTO);
     }
     
     public Double getShare() {
         return share;
     }
 
     public void setShare(Double share) {
         this.share = share;
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
 
     public List<SourceTO> getSourceList() {
         return sourceList;
     }
 
     public void setSourceList(List<SourceTO> sourceList) {
         this.sourceList = sourceList;
     }
 
     public void addSource(SourceTO source) {
         if (sourceList == null) {
             sourceList = new ArrayList<SourceTO>();
         }
         sourceList.add(source);
     }
     
     public List<RrrShareTO> getRrrShareList() {
         return rrrShareList;
     }
 
     public void setRrrShareList(List<RrrShareTO> rrrSharesList) {
         this.rrrShareList = rrrSharesList;
     }
 
     public void addRrrShare(RrrShareTO rrrShareTO) {
         if (rrrShareList == null) {
             rrrShareList = new ArrayList<RrrShareTO>();
         }
         rrrShareList.add(rrrShareTO);
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
 
     public boolean isPrimary() {
         return primary;
     }
 
     public void setPrimary(boolean primary) {
         this.primary = primary;
     }
 
     public boolean isLocked() {
         return locked;
     }
 
     public void setLocked(boolean locked) {
         this.locked = locked;
     }
 
     public String getRegistrationNumber() {
         return registrationNumber;
     }
 
     public void setRegistrationNumber(String registrationNumber) {
         this.registrationNumber = registrationNumber;
     }
 
     public Date getStatusChangeDate() {
         return statusChangeDate;
     }
 
     public void setStatusChangeDate(Date statusChangeDate) {
         this.statusChangeDate = statusChangeDate;
     }
 
     public String getLandUseCode() {
         return landUseCode;
     }
 
     public void setLandUseCode(String landUseCode) {
         this.landUseCode = landUseCode;
     }
 
 
     public String getCadastreObjectId() {
         return cadastreObjectId;
     }
 
     public void setCadastreObjectId(String cadastreObjectId) {
         this.cadastreObjectId = cadastreObjectId;
     }
 
     public Date getExecutionDate() {
         return executionDate;
     }
 
     public void setExecutionDate(Date executionDate) {
         this.executionDate = executionDate;
     }
 
     public String getLeaseNumber() {
         return leaseNumber;
     }
 
     public void setLeaseNumber(String leaseNumber) {
         this.leaseNumber = leaseNumber;
     }
 
     public List<LeaseSpecialConditionTO> getLeaseSpecialConditionList() {
         return leaseSpecialConditionList;
     }
 
     public void setLeaseSpecialConditionList(List<LeaseSpecialConditionTO> leaseSpecialConditionList) {
         this.leaseSpecialConditionList = leaseSpecialConditionList;
     }
 
     public BigDecimal getRegistrationFee() {
         return registrationFee;
     }
 
     public void setRegistrationFee(BigDecimal registrationFee) {
         this.registrationFee = registrationFee;
     }
 
     public BigDecimal getStampDuty() {
         return stampDuty;
     }
 
     public void setStampDuty(BigDecimal stampDuty) {
         this.stampDuty = stampDuty;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public BigDecimal getTransferDuty() {
         return transferDuty;
     }
 
     public void setTransferDuty(BigDecimal transferDuty) {
         this.transferDuty = transferDuty;
     }
 
     public BigDecimal getGroundRent() {
         return groundRent;
     }
 
     public void setGroundRent(BigDecimal groundRent) {
         this.groundRent = groundRent;
     }
 
     public BigDecimal getLandUsable() {
         return landUsable;
     }
 
     public void setLandUsable(BigDecimal landUsable) {
         this.landUsable = landUsable;
     }
 
     public BigDecimal getPersonalLevy() {
         return personalLevy;
     }
 
     public void setPersonalLevy(BigDecimal personalLevy) {
         this.personalLevy = personalLevy;
     }
 }
