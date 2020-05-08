 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.entity;
 
 import com.sapienter.jbilling.server.user.validator.CreditCardNumber;
 import org.hibernate.validator.constraints.NotEmpty;
 
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlType;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Only used for web services backward compatibility. 
  * Do not use!
  */
 @XmlType(name = "credit-card")
 public class CreditCardDTO implements Serializable {
 
     private Integer id;
     @CreditCardNumber(message = "validation.error.invalid.card.number")
     @NotEmpty(message="validation.error.notnull")
     private String number;
     @NotNull(message="validation.error.notnull")
     private Date expiry;
     @NotEmpty(message="validation.error.notnull")
    @Size(min = 0, max = 150, message = "validation.error.size,0,150")
     private String name;
     private Integer type;
     private Integer deleted;
     private String securityCode;
     private String gatewayKey;
     private boolean hasChanged= false;
 
     public CreditCardDTO() {
     }
 
     public CreditCardDTO(Integer id, String number, Date expiry, String name, Integer type, Integer deleted,
                          String securityCode) {
         this.id = id;
         this.number = number;
         this.expiry = expiry;
         this.name = name;
         this.type = type;
         this.deleted = deleted;
         this.securityCode = securityCode;
 
     }
 
     public CreditCardDTO(CreditCardDTO otherValue) {
         this.id = otherValue.id;
         this.number = otherValue.number;
         this.expiry = otherValue.expiry;
         this.name = otherValue.name;
         this.type = otherValue.type;
         this.deleted = otherValue.deleted;
         this.securityCode = otherValue.securityCode;
         this.gatewayKey = otherValue.gatewayKey;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public Date getExpiry() {
         return expiry;
     }
 
     public void setExpiry(Date expiry) {
         this.expiry = expiry;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getType() {
         return type;
     }
 
     public void setType(Integer type) {
         this.type = type;
     }
 
     public Integer getDeleted() {
         return deleted;
     }
 
     public void setDeleted(Integer deleted) {
         this.deleted = deleted;
     }
 
     public String getSecurityCode() {
         return securityCode;
     }
 
     public void setSecurityCode(String securityCode) {
         this.securityCode = securityCode;
     }
 
     public String getGatewayKey() {
         return gatewayKey;
     }
 
     public void setGatewayKey(String gatewayKey) {
         this.gatewayKey = gatewayKey;
     }
 
     public Boolean getHasChanged() {
         return hasChanged;
     }
 
     public void setHasChanged(Boolean hasChanged) {
         this.hasChanged = hasChanged;
     }
 
     @Override
     public String toString() {
         return "CreditCardDTO{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", expiry=" + expiry +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", deleted=" + deleted +
                ", securityCode='" + securityCode + '\'' +
                ", gatewayKey='" + gatewayKey + '\'' +
                '}';
     }
 }
