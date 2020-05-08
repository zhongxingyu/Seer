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
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import static com.sapienter.jbilling.server.pricing.db.AttributeDefinition.Type.*;
 
 /**
  * PercentagePricingStrategy
  *
  * @author Brian Cowdery
  * @since 07/02/11
  */
 public class PercentageStrategy extends AbstractPricingStrategy {
 
     public PercentageStrategy() {
         setAttributeDefinitions(
                 new AttributeDefinition("percentage", DECIMAL, true)
         );
 
         setChainPositions(
                 ChainPosition.MIDDLE,
                 ChainPosition.END
         );
     }
 
     /**
      * Applies a percentage to the given pricing result. This strategy is designed to be at the middle
      * or end of a pricing chain, when the base rate has already been determined.
      *
      * The "percentage" attribute is handled as a decimal percentage by this strategy. A percentage
      * of "0.80" would be applied as 80%, "1.25" as %125 and so on.
      *
      * @param pricingOrder target order for this pricing request (not used by this strategy)
      * @param result pricing result to apply pricing to
      * @param fields pricing fields
      * @param planPrice the plan price to apply
      * @param quantity quantity of item being priced
      * @param usage total item usage for this billing period
      */
     public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields,
                         PriceModelDTO planPrice, BigDecimal quantity, Usage usage) {
 
         if (result.getPrice() != null) {
             BigDecimal percentage = AttributeUtils.getDecimal(planPrice.getAttributes(), "percentage");
            result.setPrice(result.getPrice().multiply(percentage));
         }
     }
 }
