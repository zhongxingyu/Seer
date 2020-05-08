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
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * Interface for pricing strategies. All implemented strategies must be accessible via
  * the {@link com.sapienter.jbilling.server.pricing.db.PriceModelStrategy} mapping enum.
  *
  * @author Brian Cowdery
  * @since 05-08-2010
  */
 public interface PricingStrategy {
 
     /**
      * Returns a list of attribute definitions for this pricing strategy. This method should
      * return an empty list if there are no attributes.
      *
      * @return attribute definitions. empty list if none.
      */
     public List<AttributeDefinition> getAttributeDefinitions();
 
     /**
      * Returns a list of positions that a price model using strategy is allowed to occupy when chained.
      *
      * @return list of allowed chain positions
      */
     public List<ChainPosition> getChainPositions();
 
     /**
      * Returns true if this strategy requires the current usage of the item
      * to properly calculate the the price.
      *
      * @return true if this strategy requires the current usage of the item being priced.
      */
     public boolean requiresUsage();
 
     /**
      * Applies the plan's pricing strategy to the given pricing request.
      *
      * @param pricingOrder target order for this pricing request (may be null)
      * @param result pricing result to apply pricing to
      * @param fields pricing fields
      * @param planPrice the plan price to apply
      * @param quantity quantity of item being priced
      * @param usage total item usage for this billing period
      * @throws IllegalArgumentException if strategy requires usage, and usage was given as null
      */
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields,
                         PriceModelDTO planPrice, BigDecimal quantity, Usage usage);
 }
