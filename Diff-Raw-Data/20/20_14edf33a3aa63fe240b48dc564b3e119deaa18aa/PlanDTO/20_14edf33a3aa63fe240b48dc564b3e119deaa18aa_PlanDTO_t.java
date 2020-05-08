 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 
 import com.sapienter.jbilling.server.item.PlanWS;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
 import org.hibernate.annotations.Cascade;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Brian Cowdery
  * @since 26-08-2010
  */
 @Entity
 @Table(name = "plan")
 @TableGenerator(
         name = "plan_GEN",
         table = "jbilling_seqs",
         pkColumnName = "name",
         valueColumnName = "next_id",
         pkColumnValue = "plan",
         allocationSize = 100
 )
 @NamedQueries({
         @NamedQuery(name = "PlanDTO.findByPlanItem",
                     query = "select plan from PlanDTO plan where plan.item.id = :plan_item_id"),
 
         @NamedQuery(name = "CustomerDTO.findCustomersByPlan",
                     query = "select user.customer"
                             + " from OrderLineDTO line "
                             + " inner join line.item.plans as plan "
                             + " inner join line.purchaseOrder.baseUserByUserId as user"
                             + " where plan.id = :plan_id"
                            +"  and line.deleted = 0 "
                             + " and line.purchaseOrder.orderPeriod.id != 1 " // Constants.ORDER_PERIOD_ONCE
                             + " and line.purchaseOrder.orderStatus.id = 1 "  // Constants.ORDER_STATUS_ACTIVE
                             + " and line.purchaseOrder.deleted = 0"),
 
         @NamedQuery(name = "PlanDTO.isSubscribed",
                     query = "select line.id"
                             + " from OrderLineDTO line "
                             + " inner join line.item.plans as plan "
                             + " inner join line.purchaseOrder.baseUserByUserId as user "
                             + " where plan.id = :plan_id "
                             + " and user.id = :user_id "
                            +"  and line.deleted = 0 "
                             + " and line.purchaseOrder.orderPeriod.id != 1 " // Constants.ORDER_PERIOD_ONCE
                             + " and line.purchaseOrder.orderStatus.id = 1 "  // Constants.ORDER_STATUS_ACTIVE
                             + " and line.purchaseOrder.deleted = 0"),
 
         @NamedQuery(name = "PlanDTO.findByAffectedItem",
                     query = "select plan "
                             + " from PlanDTO plan "
                             + " inner join plan.planItems planItems "
                             + " where planItems.item.id = :affected_item_id")
 })
 // todo: cache config
 public class PlanDTO implements Serializable {
 
     private Integer id;
     private ItemDTO item; // plan subscription item
     private OrderPeriodDTO period;
     private String description;
     private List<PlanItemDTO> planItems = new ArrayList<PlanItemDTO>();
 
     public PlanDTO() {
     }
 
     public PlanDTO(PlanWS ws, ItemDTO item, OrderPeriodDTO period, List<PlanItemDTO> planItems) {
         this.id = ws.getId();
         this.item = item;
         this.period = period;
         this.description = ws.getDescription();        
         this.planItems = planItems;
     }
 
     @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "plan_GEN")
     @Column(name = "id", nullable = false, unique = true)
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     /**
      * Item holding this plan. When the customer subscribes to this item the
      * plan prices will be added for the customer.
      *
      * @return plan subscription item
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
      * Returns the plan subscription item.
      * Syntax sugar, alias for {@link #getItem()}
      * @return plan subscription item
      */
     @Transient
     public ItemDTO getPlanSubscriptionItem() {
         return getItem();
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "period_id", nullable = false)
     public OrderPeriodDTO getPeriod() {
         return period;
     }
 
     public void setPeriod(OrderPeriodDTO period) {
         this.period = period;
     }
 
     @Column(name = "description", nullable = true, length = 255)
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "plan")
     public List<PlanItemDTO> getPlanItems() {
         return planItems;
     }
 
     public void setPlanItems(List<PlanItemDTO> planItems) {
         for (PlanItemDTO planItem : planItems)
             planItem.setPlan(this);
 
         this.planItems = planItems;
     }
 
     public void addPlanItem(PlanItemDTO planItem) {
         planItem.setPlan(this);
         this.planItems.add(planItem);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         PlanDTO planDTO = (PlanDTO) o;
 
         if (description != null ? !description.equals(planDTO.description) : planDTO.description != null) return false;
         if (id != null ? !id.equals(planDTO.id) : planDTO.id != null) return false;
         if (item != null ? !item.equals(planDTO.item) : planDTO.item != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (item != null ? item.hashCode() : 0);
         result = 31 * result + (description != null ? description.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "PlanDTO{"
                + "id=" + id
                + ", item=" + item
                + ", description='" + description + '\''
                + ", planItems=" + planItems
                + '}';
     }
 }
