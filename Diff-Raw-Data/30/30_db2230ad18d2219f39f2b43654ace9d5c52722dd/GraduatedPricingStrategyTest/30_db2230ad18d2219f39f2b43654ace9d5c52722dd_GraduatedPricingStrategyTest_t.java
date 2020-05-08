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
 
 import com.sapienter.jbilling.server.BigDecimalTestCase;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 
 import java.math.BigDecimal;
 
 /**
  * @author Brian Cowdery
  * @since 30-07-2010
  */
 public class GraduatedPricingStrategyTest extends BigDecimalTestCase {
 
     // class under test
     private PricingStrategy strategy = new GraduatedPricingStrategy();
 
     public GraduatedPricingStrategyTest() {
     }
 
     public GraduatedPricingStrategyTest(String name) {
         super(name);
     }
 
    /**
     * Convenience test method to build a usage object for the given quantity.
     * @param quantity quantity
     * @return usage object
     */
    private Usage getUsage(BigDecimal quantity) {
        Usage usage = new Usage();
        usage.setQuantity(quantity);

        return usage;
    }

     public void testApplyToOverIncluded() throws Exception {
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.setType(PriceModelStrategy.GRADUATED);
 
         BigDecimal rate = new BigDecimal("0.07");
         planPrice.setRate(rate);
         planPrice.setIncludedQuantity(new BigDecimal(1000));
 
         // included minutes already exceeded by current usage
         PricingResult result = new PricingResult(1, 2, 3);
        strategy.applyTo(result, planPrice, new BigDecimal(10), getUsage(new BigDecimal(1000))); // 10 purchased, 1000 usage
 
         assertEquals(rate, result.getPrice());
     }
 
     public void testApplyToUnderIncluded() throws Exception {
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.setType(PriceModelStrategy.GRADUATED);
 
         BigDecimal rate = new BigDecimal("0.07");
         planPrice.setRate(rate);
         planPrice.setIncludedQuantity(new BigDecimal(1000));
 
         // included minutes already exceeded by current usage
         PricingResult result = new PricingResult(1, 2, 3);
        strategy.applyTo(result, planPrice, new BigDecimal(10), getUsage(BigDecimal.ZERO)); // 10 purchased, 0 usage
 
         assertEquals(BigDecimal.ZERO, result.getPrice());
     }
 
     public void testApplyToPartialOverIncluded() throws Exception {
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.setType(PriceModelStrategy.GRADUATED);
 
         BigDecimal rate = new BigDecimal("1.00"); // round numbers for easy math :)
         planPrice.setRate(rate);
         planPrice.setIncludedQuantity(new BigDecimal(1000));
 
         // half of the call exceeds the included minutes
         // rate should be 50% of the plan rate
        PricingResult result = new PricingResult(1, 2, 3);                                      // 20 purchased, 990 usage
        strategy.applyTo(result, planPrice, new BigDecimal(20), getUsage(new BigDecimal(990))); // 10 minutes included, 10 minutes rated
 
         assertEquals(new BigDecimal("0.50"), result.getPrice());
 
         // 80% of the call exceeds the included minutes
         // rate should be 80% of the plan rate
        PricingResult result2 = new PricingResult(1, 2, 3);                                       // 100 purchased, 980 usage
        strategy.applyTo(result2, planPrice, new BigDecimal(100), getUsage(new BigDecimal(980))); // 20 minutes included, 80 minutes rated
 
         assertEquals(new BigDecimal("0.80"), result2.getPrice());
     }
 }
