 package com.mpower.domain.model.customization;
 
import com.mpower.domain.model.Auditable;
 import com.mpower.domain.Inactivatible;
 import com.mpower.domain.model.AbstractEntity;
import com.mpower.domain.model.Person;
 
 public class Code extends AbstractEntity implements Auditable, Inactivatible {
 
     private static final long serialVersionUID = 1L;
 
     private CodeType codeType;
 
     private String value;
 
     private boolean inactive = false;
 
     private String description;
 
     private Auditable originalObject;
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getDisplayValue() {
         return (this.value != null && this.description != null ? (new StringBuilder().append(this.value).append(" - ").append(this.description).toString()) : this.id.toString());
     }
     
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public boolean isInactive() {
         return inactive;
     }
 
     public void setInactive(boolean inactive) {
         this.inactive = inactive;
     }
 
     public Auditable getOriginalObject() {
         return originalObject;
     }
 
     public void setOriginalObject(Auditable originalObject) {
         this.originalObject = originalObject;
     }
 
     public CodeType getCodeType() {
         return codeType;
     }
 
     public void setCodeType(CodeType codeType) {
         this.codeType = codeType;
     }
 
     @Override
     public Person getPerson() {
         return null;
     }
 
 }
