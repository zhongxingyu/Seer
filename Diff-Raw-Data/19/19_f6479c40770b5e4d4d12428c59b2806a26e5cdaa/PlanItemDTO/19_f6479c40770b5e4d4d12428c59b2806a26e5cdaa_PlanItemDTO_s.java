 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2010 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.item.db;
 
 import com.sapienter.jbilling.server.item.PlanItemWS;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 /**
  * @author Brian Cowdery
  * @since 26-08-2010
  */
 @Entity
 @Table(name = "plan_item")
 @TableGenerator(
         name = "plan_item_GEN",
         table = "jbilling_seqs",
         pkColumnName = "name",
         valueColumnName = "next_id",
         pkColumnValue = "plan_item",
         allocationSize = 100
 )
 // todo: cache config
 public class PlanItemDTO implements Serializable {
 
     public static final Integer DEFAULT_PRECEDENCE = -1;
 
     private Integer id;
     private PlanDTO plan;
     private ItemDTO item; // affected item
     private PriceModelDTO model;
     private BigDecimal bundledQuantity;
     private Integer precedence = DEFAULT_PRECEDENCE;
 
     public PlanItemDTO() {
     }
 
     public PlanItemDTO(PlanItemWS ws, ItemDTO item, PriceModelDTO model) {
         this.id = ws.getId();
         this.item = item;
         this.model = model;
         this.bundledQuantity = ws.getBundledQuantityAsDecimal();
         this.precedence = ws.getPrecedence();                
     }
 
     @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "plan_item_GEN")
     @Column(name = "id", nullable = false, unique = true)
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinColumn(name = "plan_id", nullable = true)
     public PlanDTO getPlan() {
         return plan;
     }
 
     public void setPlan(PlanDTO plan) {
         this.plan = plan;
     }
 
     /**
      * The item affected by this price. The item will be priced according
      * to the {@link PriceModelDTO} if the customer has subscribed to the plan.
      * 
      * @return affected item
      */
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "item_id", nullable = false)
     public ItemDTO getItem() {
         return item;
     }
 
     public void setItem(ItemDTO item) {
         this.item = item;
     }
 
     /**
      * Returns the affected item of this plan.
      * Syntax sugar, alias for {@link #getItem()}
      * @return affected item
      */
     @Transient
     public ItemDTO getAffectedItem() {
         return getItem();
     }
 
     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinColumn(name = "price_model_id", nullable = false)
     public PriceModelDTO getModel() {
         return model;
     }
 
     public void setModel(PriceModelDTO model) {
         this.model = model;
     }
 
     @Column(name = "bundled_quantity", nullable = true, precision = 10, scale = 22)
     public BigDecimal getBundledQuantity() {
         return bundledQuantity;
     }
 
     public void setBundledQuantity(BigDecimal bundledQuantity) {
         this.bundledQuantity = bundledQuantity;
     }
 
     @Column(name = "precedence", nullable = false, length = 2)
     public Integer getPrecedence() {
         return precedence;
     }
 
     public void setPrecedence(Integer precedence) {
         this.precedence = precedence;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         PlanItemDTO that = (PlanItemDTO) o;
 
         if (id != null ? !id.equals(that.id) : that.id != null) return false;
         if (bundledQuantity != null ? !bundledQuantity.equals(that.bundledQuantity) : that.bundledQuantity != null) return false;
         if (item != null ? !item.equals(that.item) : that.item != null) return false;
         if (plan != null ? !plan.equals(that.plan) : that.plan != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (bundledQuantity != null ? bundledQuantity.hashCode() : 0);
         result = 31 * result + (plan != null ? plan.hashCode() : 0);
         result = 31 * result + (item != null ? item.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "PlanItemDTO{"
                + "id=" + id
                + ", planId=" + (plan != null ? plan.getId() : null)
                + ", item=" + item
                + ", model=" + model
                + ", precedence=" + precedence
                + '}';
     }
 }
