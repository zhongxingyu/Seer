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
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.UsageBL;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 import java.util.SortedMap;
 
 import static com.sapienter.jbilling.server.pricing.db.AttributeDefinition.Type.*;
 
 /**
  * ItemSelectorPercentageStrategy
  *
  * @author Brian Cowdery
  * @since 13/07/11
  */
 public class ItemPercentageSelectorStrategy extends ItemSelectorStrategy {
 
     private static final Logger LOG = Logger.getLogger(ItemPercentageSelectorStrategy.class);
 
     private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
 
     public ItemPercentageSelectorStrategy() {
         setAttributeDefinitions(
                 new AttributeDefinition("typeId", INTEGER, true),
                 new AttributeDefinition("percentOfTypeId", INTEGER, true),
                new AttributeDefinition("0", INTEGER, false)
         );
 
         setChainPositions(
                 ChainPosition.START
         );
 
        setRequiresUsage(false);
     }
 
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields,
                         PriceModelDTO planPrice, BigDecimal quantity, Usage usage, boolean singlePurchase) {
 
         // price of selector trigger item will be the set $/unit rate
         result.setPrice(planPrice.getRate());
 
         if (pricingOrder != null) {
             // parse item selection tiers
             SortedMap<Integer, Integer> tiers = getTiers(planPrice.getAttributes());
             LOG.debug("Item selector percentage tiers: " + tiers);
 
            // items used for selection
            Integer typeId = AttributeUtils.getInteger(planPrice.getAttributes(), "typeId");
            Integer percentOfTypeId = AttributeUtils.getInteger(planPrice.getAttributes(), "percentOfTypeId");

            Usage typeUsage = new UsageBL(result.getUserId(), pricingOrder).getItemTypeUsage(typeId);
            Usage percentageTypeUsage = new UsageBL(result.getUserId(), pricingOrder).getItemTypeUsage(percentOfTypeId);

            BigDecimal percentage = getPercentageUsed(typeUsage.getQuantity(), percentageTypeUsage.getQuantity());
            LOG.debug("( " + typeUsage.getQuantity() + " / " + percentageTypeUsage.getQuantity() + " ) * 100 = " + percentage);
             LOG.debug("Selecting tier for usage percentage " + percentage);
 
             // find matching tier
             Integer selectedItemId = tiers.get(0);
             for (Integer tier : tiers.keySet()) {
                 if (percentage.compareTo(new BigDecimal(tier)) >= 0) {
                     selectedItemId = tiers.get(tier);
                 }
             }
 
             // add item
             if (selectedItemId != null) {
                 addIfNotExists(pricingOrder, tiers, selectedItemId);
             } else {
                 LOG.debug("No tier for usage percentage " + percentage);
             }
 
         } else {
             LOG.debug("No pricing order given (simple price fetch), skipping tiered item selection.");
         }
     }
 
     /**
      * Calculate the percentage
      * @param typeUsage
     * @param percentageOfUsage
      * @return
      */
    public BigDecimal getPercentageUsed(BigDecimal typeUsage, BigDecimal percentageOfUsage) {
         if (typeUsage.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (percentageOfUsage.compareTo(BigDecimal.ZERO) == 0) return ONE_HUNDRED;
 
         typeUsage = typeUsage.setScale(2);
        percentageOfUsage = percentageOfUsage.setScale(2);
 
        return typeUsage.divide(percentageOfUsage, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
     }
 }
