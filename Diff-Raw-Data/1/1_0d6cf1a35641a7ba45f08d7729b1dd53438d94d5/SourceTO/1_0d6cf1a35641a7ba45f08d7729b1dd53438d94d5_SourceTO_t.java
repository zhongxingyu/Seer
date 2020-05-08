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
 package org.sola.services.boundary.transferobjects.casemanagement;
 
 import java.util.Date;
 import org.sola.services.boundary.transferobjects.digitalarchive.DocumentTO;
 import org.sola.services.common.contracts.AbstractIdTO;
 
 public class SourceTO extends AbstractIdTO {
 
     private String laNr;
     private String referenceNr;
     private String archiveId;
     private String archiveDocumentId;
     private String typeCode;
     private Date acceptance;
     private Date recordation;
     private Date submission;
     private Date expirationDate;
     private String statusCode;
     private String mainType;
     private String availabilityStatusCode;
     private String content;
     private DocumentTO archiveDocument;
     private String transactionId;
     private boolean locked;
     private String ownerName;
     private String version;
     private String description;
     
     public SourceTO() {
         super();
     }
 
     public String getAvailabilityStatusCode() {
         return availabilityStatusCode;
     }
 
     public void setAvailabilityStatusCode(String availabilityStatusCode) {
         this.availabilityStatusCode = availabilityStatusCode;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getMainType() {
         return mainType;
     }
 
     public void setMainType(String mainType) {
         this.mainType = mainType;
     }
 
     public DocumentTO getArchiveDocument() {
         return archiveDocument;
     }
 
     public void setArchiveDocument(DocumentTO archiveDocument) {
         this.archiveDocument = archiveDocument;
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
 
     public Date getAcceptance() {
         return acceptance;
     }
 
     public void setAcceptance(Date acceptance) {
         this.acceptance = acceptance;
     }
 
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     public void setExpirationDate(Date expirationDate) {
         this.expirationDate = expirationDate;
     }
     
     public String getArchiveDocumentId() {
         return archiveDocumentId;
     }
 
     public void setArchiveDocumentId(String archiveDocumentId) {
         this.archiveDocumentId = archiveDocumentId;
     }
 
     public String getArchiveId() {
         return archiveId;
     }
 
     public void setArchiveId(String archiveId) {
         this.archiveId = archiveId;
     }
 
     public String getLaNr() {
         return laNr;
     }
 
     public void setLaNr(String laNr) {
         this.laNr = laNr;
     }
 
     public Date getRecordation() {
         return recordation;
     }
 
     public void setRecordation(Date recordation) {
         this.recordation = recordation;
     }
 
     public String getReferenceNr() {
         return referenceNr;
     }
 
     public void setReferenceNr(String referenceNr) {
         this.referenceNr = referenceNr;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public Date getSubmission() {
         return submission;
     }
 
     public void setSubmission(Date submission) {
         this.submission = submission;
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
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getOwnerName() {
         return ownerName;
     }
 
     public void setOwnerName(String ownerName) {
         this.ownerName = ownerName;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 }
