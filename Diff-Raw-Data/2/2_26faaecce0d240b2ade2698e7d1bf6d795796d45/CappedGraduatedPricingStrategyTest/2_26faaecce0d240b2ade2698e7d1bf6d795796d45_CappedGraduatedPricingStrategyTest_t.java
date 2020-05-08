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
 
 import com.sapienter.jbilling.server.BigDecimalTestCase;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import junit.framework.TestCase;
 
 import java.math.BigDecimal;
 
 /**
  * CappedGraduatedPricingStrategyTest
  *
  * @author Brian Cowdery
  * @since 03/02/11
  */
 public class CappedGraduatedPricingStrategyTest extends BigDecimalTestCase {
 
     // class under test
     private PricingStrategy strategy = new CappedGraduatedPricingStrategy();
 
 
     public CappedGraduatedPricingStrategyTest() {
     }
 
     public CappedGraduatedPricingStrategyTest(String name) {
         super(name);
     }
 
     /**
      * Convenience test method to build a usage object for the given quantity and amount
      * @param quantity quantity
      * @param amount amount
      * @return usage object
      */
     private Usage getUsage(Integer quantity, Integer amount) {
         Usage usage = new Usage();
         usage.setQuantity(new BigDecimal(quantity));
         usage.setAmount(new BigDecimal(amount));
 
         return usage;
     }
 
     public void testMaximum() {
         PriceModelDTO model = new PriceModelDTO();
         model.setRate(new BigDecimal("1.00"));
         model.addAttribute("included", "2");
         model.addAttribute("max", "10.00");
 
         PricingResult result = new PricingResult(1, 2, 3);
 
         // test 1 unit of purchase, should be free
         BigDecimal quantity = new BigDecimal(1);
         strategy.applyTo(null, result, null, model, quantity, getUsage(0, 0), false);
         assertEquals(BigDecimal.ZERO, result.getPrice());
 
         // test 4 unit of purchase, 2 over included, should be $2.00 total
         quantity = new BigDecimal(4);
         strategy.applyTo(null, result, null, model, quantity, getUsage(0, 0), false);
         assertEquals(new BigDecimal("2.00"), result.getPrice().multiply(quantity));
 
         // test 14 unit of purchase, totals $12.00, but maximum cap is set to $10.00
         quantity = new BigDecimal(14);
         strategy.applyTo(null, result, null, model, quantity, getUsage(0, 0), false);
         assertEquals(new BigDecimal("10.00"), result.getPrice().multiply(quantity));
 
         // HUGE purchase, still shouldn't exceed cap
         quantity = new BigDecimal(100);
         strategy.applyTo(null, result, null, model, quantity, getUsage(0, 0), false);
         assertEquals(new BigDecimal("10.00"), result.getPrice().multiply(quantity));
     }
 
     public void testMaximumWithExistingUsage() {
         PriceModelDTO model = new PriceModelDTO();
         model.setRate(new BigDecimal("1.00"));
         model.addAttribute("included", "2");
         model.addAttribute("max", "10.00");
 
         PricingResult result = new PricingResult(1, 2, 3);
 
         // test 1 unit of purchase, with 2 units of existing usage
         BigDecimal quantity = new BigDecimal(1);
         strategy.applyTo(null, result, null, model, quantity, getUsage(2, 0), false);
         assertEquals(new BigDecimal("1.00"), result.getPrice());
 
         // test 1 unit of purchase, with 10 units of existing usage and the cap exceeded
         quantity = new BigDecimal(1);
         strategy.applyTo(null, result, null, model, quantity, getUsage(10, 10), false);
        assertEquals(new BigDecimal("0.91"), result.getPrice());
     }
 }
