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
 
 package com.sapienter.jbilling.server.item;
 
 
 import com.sapienter.jbilling.server.item.db.PlanItemBundleDTO;
 import com.sapienter.jbilling.server.util.Constants;
 
 import javax.validation.constraints.Digits;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 /**
  * PlanItemBundleWS
  *
  * @author Brian Cowdery
  * @since 25/03/11
  */
 public class PlanItemBundleWS implements Serializable {
 
     public static final String TARGET_SELF = "SELF";
     public static final String TARGET_BILLABLE = "BILLABLE";
 
     private Integer id;
    @NotNull(message = "validation.error.notnull")
     private String quantity = "0";
     private Integer periodId = Constants.ORDER_PERIOD_ONCE;
     private String targetCustomer = TARGET_SELF;
     private boolean addIfExists = true;
 
     public PlanItemBundleWS() {
     }
 
     public PlanItemBundleWS(PlanItemBundleDTO dto) {
         this.id = dto.getId();
         this.addIfExists = dto.addIfExists();
 
         setQuantity(dto.getQuantity());
 
         if (dto.getPeriod() != null) this.periodId = dto.getPeriod().getId();
         if (dto.getTargetCustomer() != null) this.targetCustomer = dto.getTargetCustomer().name();
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getQuantity() {
         return quantity;
     }
 
     public BigDecimal getQuantityAsDecimal() {
         return quantity != null ? new BigDecimal(quantity) : null;
     }
 
     public void setQuantityAsDecimal(BigDecimal quantity) {
         setQuantity(quantity);
     }
 
     public void setQuantity(String quantity) {
         this.quantity = quantity;
     }
 
     public void setQuantity(BigDecimal quantity) {
         this.quantity = (quantity != null ? quantity.toString() : null);
     }
 
     public Integer getPeriodId() {
         return periodId;
     }
 
     public void setPeriodId(Integer periodId) {
         this.periodId = periodId;
     }
 
     public String getTargetCustomer() {
         return targetCustomer;
     }
 
     public void setTargetCustomer(String targetCustomer) {
         this.targetCustomer = targetCustomer;
     }
 
     public boolean addIfExists() {
         return addIfExists;
     }
 
     public void setAddIfExists(boolean addIfExists) {
         this.addIfExists = addIfExists;
     }
 
     @Override
     public String toString() {
         return "PlanItemBundleWS{"
                + "id=" + id
                + ", quantity='" + quantity + '\''
                + ", periodId=" + periodId
                + ", targetCustomer='" + targetCustomer + '\''
                + ", addIfExists=" + addIfExists
                + '}';
     }
 }
