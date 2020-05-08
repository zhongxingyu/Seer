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
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.administrative.repository.entities;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import org.sola.services.common.LocalInfo;
 import org.sola.services.common.repository.*;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 import org.sola.services.ejb.party.businesslogic.PartyEJBLocal;
 import org.sola.services.ejb.party.repository.entities.Party;
 import org.sola.services.ejb.source.businesslogic.SourceEJBLocal;
 import org.sola.services.ejb.source.repository.entities.Source;
 import org.sola.services.ejb.system.br.Result;
 import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
 import org.sola.services.ejb.transaction.businesslogic.TransactionEJBLocal;
 import org.sola.services.ejb.transaction.repository.entities.Transaction;
 import org.sola.services.ejb.transaction.repository.entities.TransactionBasic;
 import org.sola.services.ejb.transaction.repository.entities.TransactionStatusType;
 
 /**
  * Entity representing administrative.rrr table. 
  * @author soladev
  */
 @Table(name = "rrr", schema = "administrative")
 @DefaultSorter(sortString="status_code, nr")
 public class Rrr extends AbstractVersionedEntity {
 
     public static final String QUERY_PARAMETER_TRANSACTIONID = "transactionId";
     public static final String QUERY_WHERE_BYTRANSACTIONID = "transaction_id = "
             + "#{" + QUERY_PARAMETER_TRANSACTIONID + "}";
     public static final String QUERY_ORDER_BY = " status_code, nr ";
     @Id
     @Column(name = "id")
     private String id;
     @Column(name = "ba_unit_id")
     private String baUnitId;
     @Column(name = "nr")
     private String nr;
     @Column(name = "type_code")
     private String typeCode;
     @Column(name = "status_code", insertable = false, updatable = false)
     private String statusCode;
     @Column(name = "is_primary")
     private boolean primary;
     @Column(name = "registration_date")
     private Date registrationDate;
     @Column(name = "transaction_id", updatable = false)
     private String transactionId;
     @Column(name = "expiration_date")
     private Date expirationDate;
     @Column(name = "share")
     private Double share;
    @Column(name = "mortgage_amount")
     private BigDecimal mortgageAmount;
     @Column(name = "mortgage_interest_rate")
     private BigDecimal mortgageInterestRate;
     @Column(name = "mortgage_ranking")
     private Integer mortgageRanking;
     @Column(name = "mortgage_type_code")
     private String mortgageTypeCode;
     // Child entity fields
     @ChildEntity(insertBeforeParent = false, parentIdField = "rrrId")
     private BaUnitNotation notation;
     @ChildEntityList(parentIdField = "rrrId", cascadeDelete = true)
     private List<RrrShare> rrrShareList;
     @ExternalEJB(ejbLocalClass = SourceEJBLocal.class,
     loadMethod = "getSources", saveMethod = "saveSource")
     @ChildEntityList(parentIdField = "rrrId", childIdField = "sourceId",
     manyToManyClass = SourceDescribesRrr.class)
     private List<Source> sourceList;
     @ExternalEJB(ejbLocalClass = PartyEJBLocal.class, loadMethod = "getParties")
     @ChildEntityList(parentIdField = "rrrId", childIdField = "partyId",
     manyToManyClass = PartyForRrr.class, readOnly = true)
     private List<Party> rightHolderList;
     @Column(insertable=false, updatable=false, name = "concatenated_name")
     @AccessFunctions(onSelect = "administrative.get_concatenated_name(ba_unit_id)")
     private String concatenatedName;
 
     public String getConcatenatedName() {
         return concatenatedName;
     }
 
     public void setConcatenatedName(String concatenatedName) {
         this.concatenatedName = concatenatedName;
     }
     
     
     // Other fields
     private Boolean locked = null;
 
     public Rrr() {
         super();
     }
 
     private String generateRrrNumber() {
         String result = "";
         SystemEJBLocal systemEJB = RepositoryUtility.tryGetEJB(SystemEJBLocal.class);
         if (systemEJB != null) {
             Result newNumberResult = systemEJB.checkRuleGetResultSingle("generate-rrr-nr", null);
             if (newNumberResult != null && newNumberResult.getValue() != null) {
                 result = newNumberResult.getValue().toString();
             }
         }
         return result;
     }
 
     private Transaction getTransaction() {
         Transaction result = null;
         TransactionEJBLocal transactionEJB = RepositoryUtility.tryGetEJB(TransactionEJBLocal.class);
         if (transactionEJB != null) {
             result = transactionEJB.getTransactionById(getTransactionId(), Transaction.class);
         }
         return result;
     }
 
     public String getId() {
         id = id == null ? generateId() : id;
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     public void setExpirationDate(Date expirationDate) {
         this.expirationDate = expirationDate;
     }
 
     public BigDecimal getMortgageAmount() {
         return mortgageAmount;
     }
 
     public void setMortgageAmount(BigDecimal mortgageAmount) {
         this.mortgageAmount = mortgageAmount;
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
 
     public String getNr() {
         return nr;
     }
 
     public void setNr(String nr) {
         this.nr = nr;
     }
 
     public boolean isPrimary() {
         return primary;
     }
 
     public void setPrimary(boolean primary) {
         this.primary = primary;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public Double getShare() {
         return share;
     }
 
     public void setShare(Double share) {
         this.share = share;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
 
     public String getTypeCode() {
         return typeCode;
     }
 
     public void setTypeCode(String typeCode) {
         this.typeCode = typeCode;
     }
 
     public BaUnitNotation getNotation() {
         return notation;
     }
 
     public void setNotation(BaUnitNotation notation) {
         this.notation = notation;
     }
 
     public List<RrrShare> getRrrShareList() {
         return rrrShareList;
     }
 
     public void setRrrShareList(List<RrrShare> rrrShareList) {
         this.rrrShareList = rrrShareList;
     }
 
     public List<Source> getSourceList() {
         return sourceList;
     }
 
     public void setSourceList(List<Source> sourceList) {
         this.sourceList = sourceList;
     }
 
     public List<Party> getRightHolderList() {
         return rightHolderList;
     }
 
     public void setRightHolderList(List<Party> rightHolderList) {
         this.rightHolderList = rightHolderList;
     }
 
     public Boolean isLocked() {
         if (locked == null) {
             locked = false;
             Transaction transaction = getTransaction();
             if (transaction != null
                     && transaction.getStatusCode().equals(TransactionStatusType.COMPLETED)) {
                 locked = true;
             }
         }
         return locked;
     }
 
     @Override
     public void preSave() {
         if (this.isNew()) {
             setTransactionId(LocalInfo.getTransactionId());
         }
 
         if (isNew() && getNr() == null) {
             // Assign a generated number to the Rrr if it is not currently set. 
             setNr(generateRrrNumber());
         }
         super.preSave();
     }
 }
