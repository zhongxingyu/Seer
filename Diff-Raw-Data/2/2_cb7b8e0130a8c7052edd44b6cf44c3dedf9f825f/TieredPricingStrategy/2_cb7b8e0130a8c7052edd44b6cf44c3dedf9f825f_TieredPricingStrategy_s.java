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
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import static com.sapienter.jbilling.server.pricing.db.AttributeDefinition.Type.*;
 
 /**
  * Tiered pricing strategy.
  *
  * @author Brian Cowdery
  * @since 16-Jan-2012
  */
 public class TieredPricingStrategy extends AbstractPricingStrategy {
 
     private static final Logger LOG = Logger.getLogger(TieredPricingStrategy.class);
 
     public TieredPricingStrategy() {
         setAttributeDefinitions(
                 new AttributeDefinition("0", DECIMAL, true)
         );
 
         setChainPositions(
                 ChainPosition.START
         );
 
         setRequiresUsage(true);
     }
 
     /**
      * Calculates a price based on the amount being purchased. Prices are organized into "tiers", where
      * the price calculated depends on how much of the quantity purchased falls into each tier.
      *
      * Example:
      *  0 - 500    @ $2
      *  500 - 1000 @ $1
      *  > 1000     @ $0.5
      *
      *  The first 500 purchased would be at $2/unit, the next 500 would be priced at $1/unit, and
      *  the remaining quantity over 1000 would be priced at $0.5/unit.
      *
      *  The final price is an aggregated total of the quantity priced in each tier.
      *
      * @param pricingOrder target order for this pricing request (may be null)
      * @param result pricing result to apply pricing to
      * @param fields pricing fields
      * @param planPrice the plan price to apply
      * @param quantity quantity of item being priced
      * @param usage total item usage for this billing period
      */
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields,
                         PriceModelDTO planPrice, BigDecimal quantity, Usage usage) {
 
         if (usage == null || usage.getQuantity() == null)
             throw new IllegalArgumentException("Usage quantity cannot be null for TieredPricingStrategy.");
 
         /*
            Usage quantity normally includes the quantity being purchased because we roll in the order
            lines. If there is no pricing order (populating a single ItemDTO price), add the quantity
            being purchased to the usage calc to get the total quantity.
         */
         BigDecimal total = pricingOrder == null ?  usage.getQuantity().add(quantity) : usage.getQuantity();
         BigDecimal existing = pricingOrder == null ? usage.getQuantity() : usage.getQuantity().subtract(quantity);
 
         assert existing.add(quantity).equals(total);
 
         // parse pricing tiers
         SortedMap<Integer, BigDecimal> tiers = getTiers(planPrice.getAttributes());
         LOG.debug("Tiered pricing: " + tiers);
        LOG.debug("Selecting tier price for usage level " + usage.getQuantity());
 
         if (!tiers.isEmpty()) {
             // calculate price for entire quantity across all orders, and the price for all previously
             // existing orders. The difference is the price of the quantity being purchased now.
             BigDecimal totalPrice = getTotalForQuantity(tiers, total);
             BigDecimal existingPrice = getTotalForQuantity(tiers, existing);
 
             // calculate price per unit from the total
             BigDecimal price = totalPrice.subtract(existingPrice).divide(quantity, Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
             result.setPrice(price);
 
         } else {
             // no pricing tiers given
             result.setPrice(BigDecimal.ZERO);
         }
     }
 
     /**
      * Calculates the total dollar value (quantity * price) of the given quantity for the
      * given pricing tiers.
      *
      * @param tiers pricing tiers
      * @param quantity quantity to calculate total for
      * @return total dollar value of purchased quantity
      */
     public BigDecimal getTotalForQuantity(SortedMap<Integer, BigDecimal> tiers, BigDecimal quantity) {
         // sort through each tier, adding up the price for the full quantity purchased.
         BigDecimal totalPrice = BigDecimal.ZERO;
 
         Integer lower = null;
         for (Integer upper : tiers.keySet()) {
             if (lower == null) {
                 lower = upper;
                 continue;
             }
 
             // the total quantity in this tier that gets a price
             BigDecimal tier = new BigDecimal(upper - lower);
             BigDecimal price = tiers.get(lower);
 
             // quantity less than total number of units in tier
             // totalPrice = totalPrice + (quantity * price)
             // break from loop
             if (quantity.compareTo(tier) < 0) {
                 totalPrice = totalPrice.add(quantity.multiply(price));
                 quantity = BigDecimal.ZERO;
                 break;
             }
 
             // quantity is more than, or equal to, total number of units
             // subtract tier quantity from quantity being priced
             // totalPrice = totalPrice + (tier quantity * price)
             if (quantity.compareTo(tier) >= 0) {
                 totalPrice = totalPrice.add(tier.multiply(price));
                 quantity = quantity.subtract(tier);
             }
 
             // move up to the next tier and handle the
             // remaining quantity in the next pass.
             lower = upper;
         }
 
 
         // last tier
         // all remaining quantity > tier priced at a fixed rate
         BigDecimal price = tiers.get(lower);
         if (!quantity.equals(BigDecimal.ZERO)) {
             totalPrice = totalPrice.add(quantity.multiply(price));
         }
 
         return totalPrice;
     }
 
     /**
      * Parses the price model attributes and returns a map of tier quantities and corresponding
      * prices for each tier. The map is sorted in ascending order by quantity (smallest first).
      *
      * @param attributes attributes to parse
      * @return tiers of quantities and prices
      */
     protected SortedMap<Integer, BigDecimal> getTiers(Map<String, String> attributes) {
         SortedMap<Integer, BigDecimal> tiers = new TreeMap<Integer, BigDecimal>();
 
         for (Map.Entry<String, String> entry : attributes.entrySet()) {
             if (entry.getKey().matches("^\\d+$")) {
                 tiers.put(AttributeUtils.parseInteger(entry.getKey()), AttributeUtils.parseDecimal(entry.getValue()));
             }
         }
 
         return tiers;
     }
 }
