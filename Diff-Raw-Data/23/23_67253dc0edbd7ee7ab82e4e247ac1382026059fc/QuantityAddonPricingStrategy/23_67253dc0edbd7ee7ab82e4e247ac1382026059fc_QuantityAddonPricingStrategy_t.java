 /*
  JBILLING CONFIDENTIAL
  _____________________
 
  [2003] - [2012] Enterprise jBilling Software Ltd.
  All Rights Reserved.
 
  NOTICE:  All information contained herein is, and remains
  the property of Enterprise jBilling Software.
  The intellectual and technical concepts contained
  herein are proprietary to Enterprise jBilling Software
  and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.UsageBL;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.db.PriceModelStrategy;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 import org.apache.log4j.Logger;
 
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * Quantity add-on for graduated pricing strategies.
  *
  * Allows the use of Add-on items which increase the number of included items a customer
  * is allowed as part of a Graduated pricing strategy.
  *
  * @author Brian Cowdery
  * @since 31/01/11
  */
 public class QuantityAddonPricingStrategy extends AbstractPricingStrategy {
 
     private static final Logger LOG = Logger.getLogger(QuantityAddonPricingStrategy.class);
 
     /*
         Only strategy types in this list are allowed to be next in the chain.
      */
     public static final List<PriceModelStrategy> ALLOWED_NEXT_IN_CHAIN = Arrays.asList(
             PriceModelStrategy.GRADUATED,
             PriceModelStrategy.CAPPED_GRADUATED
     );
 
     public QuantityAddonPricingStrategy() {
         setChainPositions(
                 ChainPosition.START
         );
 
         setRequiresUsage(true);
     }
 
     /**
      *
      *
      * @param pricingOrder target order for this pricing request (not used by this strategy)
      * @param result pricing result to apply pricing to
      * @param fields pricing fields (not used by this strategy)
      * @param planPrice the plan price to apply
      * @param quantity quantity of item being priced
      * @param usage total item usage for this billing period
      * @param singlePurchase single purchase, or a purchase already part of an order
      */
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields, PriceModelDTO planPrice,
                         BigDecimal quantity, Usage usage, boolean singlePurchase) {
 
         if (planPrice.getNext() != null && !ALLOWED_NEXT_IN_CHAIN.contains(planPrice.getNext().getType()))
             throw new IllegalArgumentException("Quantity Add-on must be followed by Graduated pricing!");
 
 
         // parse add-on items and quantities
         SortedMap<Integer, BigDecimal> items = getItemQuantityMap(planPrice.getAttributes());
         LOG.debug("Add-on items: " + items);
 
         // go through each add-on item and check it's usage, gathering
         // a total of how much additional "included" units the customer has purchased
         UsageBL usageService = new UsageBL(result.getUserId(), pricingOrder);
         BigDecimal total = BigDecimal.ZERO;
 
         for (Map.Entry<Integer, BigDecimal> entry : items.entrySet()) {
             Usage addonUsage = usageService.getItemUsage(entry.getKey());
 
             BigDecimal quantityPerUnit = entry.getValue();
             BigDecimal increase = quantityPerUnit.multiply(addonUsage.getQuantity());
 
             LOG.debug("Addon item " + addonUsage.getItemId() + "; " + quantityPerUnit
                       + " x " + addonUsage.getQuantity() + " purchased = +" + increase + " included");
 
             total = total.add(increase);
             LOG.debug("Total increase = " + total);
         }
 
         // increment the "included" attribute of the next pricing model
         PriceModelDTO graduated = planPrice.getNext();
        if (graduated != null) {
        	BigDecimal included = AttributeUtils.getDecimal(graduated.getAttributes(), "included");
 
        	if (included != null) {
        		included = included.add(total);
        		graduated.getAttributes().put("included", included.toString());
 
        		LOG.debug("Setting included quantity to " + included + " units");
        	}
         }
     }
 
     /**
      * Parses the price model attributes and returns a map of item ID's and the additional quantities
      * that a subscription to that item would provide.
      *
      * @param attributes attributes to parse
      * @return map of item ID's and add-on quantities
      */
     protected SortedMap<Integer, BigDecimal> getItemQuantityMap(Map<String, String> attributes) {
         SortedMap<Integer, BigDecimal> tiers = new TreeMap<Integer, BigDecimal>();
 
         for (Map.Entry<String, String> entry : attributes.entrySet()) {
             if (entry.getKey().matches("^\\d+$")) {
                 tiers.put(AttributeUtils.parseInteger(entry.getKey()), AttributeUtils.parseDecimal(entry.getValue()));
             }
         }
 
         return tiers;
     }
 }
