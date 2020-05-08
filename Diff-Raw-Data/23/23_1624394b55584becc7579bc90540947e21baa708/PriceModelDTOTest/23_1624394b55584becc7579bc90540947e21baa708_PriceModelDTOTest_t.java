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
 
 package com.sapienter.jbilling.server.pricing.db;
 
 import com.sapienter.jbilling.server.BigDecimalTestCase;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.pricing.PriceModelWS;
 import com.sapienter.jbilling.server.pricing.strategy.FlatPricingStrategy;
 import com.sapienter.jbilling.server.pricing.strategy.PriceModelStrategy;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Brian Cowdery
  * @since 09-08-2010
  */
 public class PriceModelDTOTest extends BigDecimalTestCase {
 
     public PriceModelDTOTest() {
     }
 
     public PriceModelDTOTest(String name) {
         super(name);
     }
 
     public void testAddAttributeWildcards() {
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.addAttribute("null_attr", null);
         planPrice.addAttribute("attr", "some value");
 
         // null replaced with wildcard character
         assertNotNull(planPrice.getAttributes().get("null_attr"));
         assertEquals(PriceModelDTO.ATTRIBUTE_WILDCARD, planPrice.getAttributes().get("null_attr"));
 
         // non-null attributes left un-changed
         assertNotNull(planPrice.getAttributes().get("attr"));
         assertEquals("some value", planPrice.getAttributes().get("attr"));
     }
 
     public void testSetAttributesWildcards() {
         Map<String, String> attributes = new HashMap<String, String>();
         attributes.put("null_attr", null);
         attributes.put("attr", "some value");
 
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.setAttributes(attributes);
 
         // null replaced with wildcard character
         assertNotNull(planPrice.getAttributes().get("null_attr"));
         assertEquals(PriceModelDTO.ATTRIBUTE_WILDCARD, planPrice.getAttributes().get("null_attr"));
 
         // non-null attributes left un-changed
         assertNotNull(planPrice.getAttributes().get("attr"));
         assertEquals("some value", planPrice.getAttributes().get("attr"));
     }
 
     public void testGetStrategy() {
         PriceModelDTO planPrice = new PriceModelDTO();
 
         // get strategy doesn't error if no type set
         assertNull(planPrice.getStrategy());
 
         // strategy defined by the set type
         planPrice.setType(PriceModelStrategy.FLAT);
         assertTrue(planPrice.getStrategy() instanceof FlatPricingStrategy);
     }
 
     public void testSetGraduatedStrategy() {
         PriceModelDTO planPrice = new PriceModelDTO();
 
         // METERED is not a graduated pricing strategy and does not support "included quantities"
         planPrice.setType(PriceModelStrategy.METERED);
         planPrice.setIncludedQuantity(BigDecimal.TEN);
         assertEquals(BigDecimal.ZERO, planPrice.getIncludedQuantity()); // always ZERO
 
         // GRADUATED supports "included quantities"
         planPrice.setType(PriceModelStrategy.GRADUATED);
         planPrice.setIncludedQuantity(BigDecimal.TEN);
         assertEquals(BigDecimal.TEN, planPrice.getIncludedQuantity()); // included can be set                                                           
     }
 
     public void testSetRatedStrategy() {
         PriceModelDTO planPrice = new PriceModelDTO();
 
         // METERED pricing has no overriding rate, uses the set plan pricing rate
         planPrice.setType(PriceModelStrategy.METERED);
         planPrice.setRate(new BigDecimal("0.7"));
         assertEquals(new BigDecimal("0.7"), planPrice.getRate()); // plan rate can be set
 
         // FLAT pricing has an overriding rate that is always ZERO
         planPrice.setType(PriceModelStrategy.FLAT);
         assertEquals(BigDecimal.ZERO, planPrice.getRate()); // always ZERO
     }
 
     public void testApplyTo() {
         PriceModelDTO planPrice = new PriceModelDTO();
         planPrice.setType(PriceModelStrategy.METERED);
         planPrice.setRate(new BigDecimal("0.7"));
 
         PricingResult result = new PricingResult(1, 2, 3);
         planPrice.applyTo(result, null, null);
         assertEquals(planPrice.getRate(), result.getPrice());
     }
        
     public void testFromWS() {
         Map<String, String> attributes = new HashMap<String, String>();
         attributes.put("null_attr", null);
         attributes.put("attr", "some value");
 
         PriceModelWS ws = new PriceModelWS();
         ws.setId(1);
         ws.setType(PriceModelWS.PLAN_TYPE_METERED);
         ws.setAttributes(attributes);
         ws.setRate(new BigDecimal("0.7"));
         ws.setIncludedQuantity(BigDecimal.ZERO);
 
         // convert to PriceModelDTO
         PriceModelDTO dto = new PriceModelDTO(ws, null);
 
         assertEquals(ws.getId(), dto.getId());
         assertEquals(PriceModelStrategy.METERED, dto.getType());
        assertEquals(ws.getRateAsDecimal(), dto.getRate());
        assertEquals(ws.getIncludedQuantityAsDecimal(), dto.getIncludedQuantity());
 
         assertNotSame(ws.getAttributes(), dto.getAttributes());
         assertEquals(PriceModelDTO.ATTRIBUTE_WILDCARD, dto.getAttributes().get("null_attr"));
         assertEquals("some value", dto.getAttributes().get("attr"));   
     }
 }
