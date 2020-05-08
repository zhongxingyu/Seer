 package com.mpower.domain.model.communication;
 
 import java.util.Date;
 import java.util.Map;
 
 import com.mpower.domain.Inactivatible;
import com.mpower.domain.model.AbstractCustomizableEntity;
 import com.mpower.domain.model.customization.CustomField;
 import com.mpower.domain.model.customization.FieldDefinition;
 import com.mpower.type.ActivationType;
 
 public abstract class AbstractCommunicationEntity extends AbstractCustomizableEntity implements Inactivatible {
 
     protected Long personId;
     protected Date createDate;
     protected Date updateDate;
     protected ActivationType activationStatus;
     protected boolean receiveMail = false;
     protected Date temporaryStartDate;
     protected Date temporaryEndDate;
     protected Date seasonalStartDate;
     protected Date seasonalEndDate;
     protected boolean inactive = false;
     protected String comments;
     // only meaningful for Permanent emails, and indicates when date becomes effective (ex. they are moving the first of next month)
     protected Date effectiveDate; 
     protected boolean userCreated = false;
     protected Map<String, CustomField> customFieldMap = null;
     protected Map<String, String> fieldLabelMap = null;
     protected Map<String, Object> fieldValueMap = null;
     protected Map<String, FieldDefinition> fieldTypeMap = null;
 
 
 
     public Long getPersonId() {
         return personId;
     }
 
     public void setPersonId(Long personId) {
         this.personId = personId;
     }
 
     public Date getCreateDate() {
         return createDate;
     }
 
     public void setCreateDate(Date createDate) {
         this.createDate = createDate;
     }
 
     public Date getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(Date updateDate) {
         this.updateDate = updateDate;
     }
 
     public boolean isReceiveMail() {
         return receiveMail;
     }
 
     public void setReceiveMail(boolean receiveMail) {
         this.receiveMail = receiveMail;
     }
 
     public ActivationType getActivationStatus() {
         return activationStatus;
     }
 
     public void setActivationStatus(ActivationType activationStatusType) {
         this.activationStatus = activationStatusType;
     }
 
     public Date getTemporaryStartDate() {
         return temporaryStartDate;
     }
 
     public void setTemporaryStartDate(Date temporaryStartDate) {
         this.temporaryStartDate = temporaryStartDate;
     }
 
     public Date getTemporaryEndDate() {
         return temporaryEndDate;
     }
 
     public void setTemporaryEndDate(Date temporaryEndDate) {
         this.temporaryEndDate = temporaryEndDate;
     }
 
     public Date getSeasonalStartDate() {
         return seasonalStartDate;
     }
 
     public void setSeasonalStartDate(Date seasonalStartDate) {
         this.seasonalStartDate = seasonalStartDate;
     }
 
     public Date getSeasonalEndDate() {
         return seasonalEndDate;
     }
 
     public void setSeasonalEndDate(Date seasonalEndDate) {
         this.seasonalEndDate = seasonalEndDate;
     }
 
     public boolean isInactive() {
         return inactive;
     }
 
     public void setInactive(boolean inactive) {
         this.inactive = inactive;
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public Date getEffectiveDate() {
         //TANGERINE-203, for a new, permanent contact, use Today as defaut
         if((id == null || id == 0) && effectiveDate  == null
                 && ActivationType.permanent.equals(getActivationStatus())) {
             this.effectiveDate = new Date();
         }
 
         return effectiveDate;
     }
 
     public void setEffectiveDate(Date effectiveDate) {
         this.effectiveDate = effectiveDate;
     }
 
     public boolean isUserCreated() {
         return userCreated;
     }
 
     public void setUserCreated(boolean userCreated) {
         this.userCreated = userCreated;
     }
     
     @Override
     public void prePersist() {
         super.prePersist();
         
         if (activationStatus != null) {
             if (ActivationType.permanent.equals(getActivationStatus())) {
                 setSeasonalEndDate(null);
                 setSeasonalStartDate(null);
                 setTemporaryEndDate(null);
                 setTemporaryStartDate(null);
             } 
             else if (ActivationType.seasonal.equals(getActivationStatus())) {
                 setEffectiveDate(null);
                 setTemporaryEndDate(null);
                 setTemporaryStartDate(null);
             } 
             else if (ActivationType.temporary.equals(getActivationStatus())) {
                 setEffectiveDate(null);
                 setSeasonalEndDate(null);
                 setSeasonalStartDate(null);
             }
         }
     }
 }
