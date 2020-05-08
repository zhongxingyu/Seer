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
 package org.sola.services.ejb.application.repository.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Table;
 import org.sola.services.common.repository.DefaultSorter;
 import org.sola.services.common.repository.entities.AbstractCodeEntity;
 
 /**
  * Entity representing the application.application_action_type code table
  * @author soladev
  */
 @Table(name = "application_action_type", schema = "application")
 @DefaultSorter(sortString="display_value")
 public class ApplicationActionType extends AbstractCodeEntity {
 
     public static final String LODGE = "lodge";
     public static final String APPROVE = "approve";
     public static final String ARCHIVE = "archive";
    public static final String DISPATCH = "dispatch";
     public static final String DOCUMENTS_ADDED = "addDocument";
     public static final String WITHDRAW = "withdraw";
     public static final String CANCEL = "cancel";
     public static final String REQUISITION = "requisition";
     // Although validate is here it is not found in the list of actions in the table itself,
     // because if validation fails, the action logged must be:VALIDATE_FAILED or VALIDATE_PASSED
     public static final String VALIDATE = "validate";
     public static final String VALIDATE_FAILED = "validateFailed";
     public static final String VALIDATE_PASSED = "validatePassed";
     public static final String LAPSE = "lapse";
     public static final String ASSIGN = "assign";
     public static final String UNASSIGN = "unAssign";
     public static final String RESUBMIT = "resubmit";
     
 
     @Column(name = "status_to_set")
     private String statusToSet;
 
     public ApplicationActionType() {
         super();
     }
 
     public String getStatusToSet() {
         return statusToSet;
     }
 
     public void setStatusToSet(String statusToSet) {
         this.statusToSet = statusToSet;
     }
 }
