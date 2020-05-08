 /**
  * The contents of this file are subject to the OpenMRS Public License Version
  * 1.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.
  *
  * Copyright (C) OpenMRS, LLC. All Rights Reserved.
  */
 package org.openmrs.module.errorlogging;
 
 import java.io.Serializable;
 import java.util.Date;
 import org.openmrs.BaseOpenmrsMetadata;
 import org.openmrs.BaseOpenmrsObject;
 import org.openmrs.User;
 
 /**
  * It is a model class. It should extend either {@link BaseOpenmrsObject} or {@link BaseOpenmrsMetadata}.
  */
 public class ExceptionLog extends BaseOpenmrsObject implements Serializable {
 
     private static final long serialVersionUID = 1L;
     private Integer exceptionLogId;
     private String exceptionClass;
     private String exceptionMessage;
    private String openmrsVersion;
     private Date dateThrown;
     private User exceptionThrownBy;
     private ExceptionLogDetail exceptionLogDetail;
     private ExceptionRootCause exceptionRootCause;
 
     @Override
     public Integer getId() {
         return getExceptionLogId();
     }
 
     @Override
     public void setId(Integer id) {
         this.setExceptionLogId(id);
     }
 
     /**
      * @return the exceptionLogId
      */
     public Integer getExceptionLogId() {
         return exceptionLogId;
     }
 
     /**
      * @param exceptionLogId the exceptionLogId to set
      */
     public void setExceptionLogId(Integer exceptionLogId) {
         this.exceptionLogId = exceptionLogId;
     }
 
     /**
      * @return the exceptionClass
      */
     public String getExceptionClass() {
         return exceptionClass;
     }
 
     /**
      * @param exceptionClass the exceptionClass to set
      */
     public void setExceptionClass(String exceptionClass) {
         this.exceptionClass = exceptionClass;
     }
 
     /**
      * @return the exceptionMessage
      */
     public String getExceptionMessage() {
         return exceptionMessage;
     }
 
     /**
      * @param exceptionMessage the exceptionMessage to set
      */
     public void setExceptionMessage(String exceptionMessage) {
         this.exceptionMessage = exceptionMessage;
     }
    
    /**
     * @return the openmrsVersion
     */
    public String getOpenmrsVersion() {
        return openmrsVersion;
    }

    /**
     * @param openmrsVersion the openmrsVersion to set
     */
    public void setOpenmrsVersion(String openmrsVersion) {
        this.openmrsVersion = openmrsVersion;
    }
 
     /**
      * @return the dateThrown
      */
     public Date getDateThrown() {
         return dateThrown;
     }
 
     /**
      * @param dateThrown the dateThrown to set
      */
     public void setDateThrown(Date dateThrown) {
         this.dateThrown = dateThrown;
     }
 
     /**
      * @return the exceptionThrownBy
      */
     public User getExceptionThrownBy() {
         return exceptionThrownBy;
     }
 
     /**
      * @param exceptionThrownBy the exceptionThrownBy to set
      */
     public void setExceptionThrownBy(User exceptionThrownBy) {
         this.exceptionThrownBy = exceptionThrownBy;
     }
 
     /**
      * @return the exceptionLogDetail
      */
     public ExceptionLogDetail getExceptionLogDetail() {
         return exceptionLogDetail;
     }
 
     /**
      * @param exceptionLogDetail the exceptionLogDetail to set
      */
     public void setExceptionLogDetail(ExceptionLogDetail exceptionLogDetail) {
         this.exceptionLogDetail = exceptionLogDetail;
     }
 
     /**
      * @return the exceptionRootCause
      */
     public ExceptionRootCause getExceptionRootCause() {
         return exceptionRootCause;
     }
 
     /**
      * @param exceptionRootCause the exceptionRootCause to set
      */
     public void setExceptionRootCause(ExceptionRootCause exceptionRootCause) {
         this.exceptionRootCause = exceptionRootCause;
     }

    
 }
