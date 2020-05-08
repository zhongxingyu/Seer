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
 package org.sola.services.boundary.transferobjects.search;
 
 import java.util.Date;
 import org.sola.services.common.contracts.AbstractReadWriteTO;
 
 public class BaUnitSearchResultTO extends AbstractReadWriteTO {
     private String id;
     private String nameFirstPart;
     private String nameLastPart;
     private String statusCode;
     private String rightholders;
     private String registrationNumber;
     private Date registrationDate;
     private String leaseNumber;
     
     public BaUnitSearchResultTO(){
         super();
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getNameFirstPart() {
         return nameFirstPart;
     }
 
     public void setNameFirstPart(String nameFirstPart) {
         this.nameFirstPart = nameFirstPart;
     }
 
     public String getNameLastPart() {
         return nameLastPart;
     }
 
     public void setNameLastPart(String nameLastPart) {
         this.nameLastPart = nameLastPart;
     }
 
     public String getRightholders() {
         return rightholders;
     }
 
     public void setRightholders(String rightholders) {
         this.rightholders = rightholders;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public String getRegistrationNumber() {
         return registrationNumber;
     }
 
     public void setRegistrationNumber(String registrationNumber) {
         this.registrationNumber = registrationNumber;
     }
 
     public String getLeaseNumber() {
         return leaseNumber;
     }
 
     public void setLeaseNumber(String leaseNumber) {
         this.leaseNumber = leaseNumber;
     }
 }
