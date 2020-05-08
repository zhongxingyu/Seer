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
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Flat pricing strategy.
  *
  * Prices the item at $0/unit. Flat pricing assumes that the user has already paid for all
  * usage as part of the cost of the plan.
  *
  * @author Brian Cowdery
  * @since 05-08-2010
  */
 public class FlatPricingStrategy implements PricingStrategy {
 
    public boolean requiresUsage() { return false; }
     public boolean hasRate() { return true; }
     public BigDecimal getRate() { return BigDecimal.ZERO; }
 
     public List<AttributeDefinition> getAttributeDefinitions() {
         return Collections.emptyList();
     }
 
     /**
      * Sets the price to zero. The price for all usage is included in the cost of the
      * monthly plan subscription, so the customer is not charged per unit.
      *
      * @param result pricing result to apply pricing to
      * @param planPrice the plan price to apply
      * @param quantity quantity of item being priced
      * @param usage total item usage for this billing period
      */
     public void applyTo(PricingResult result, PriceModelDTO planPrice, BigDecimal quantity, Usage usage) {
         result.setPrice(BigDecimal.ZERO);
     }
 }
