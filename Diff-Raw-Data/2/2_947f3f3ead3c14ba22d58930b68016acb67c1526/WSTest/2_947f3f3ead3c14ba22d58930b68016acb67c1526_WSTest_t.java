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
 
 package com.sapienter.jbilling.server.item;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 /**
  * @author Emil
  */
 public class WSTest  extends TestCase {
       
 
     public void testCreate() {
         try {
         	JbillingAPI api = JbillingAPIFactory.getAPI();
             /*
              * Create
              */
             ItemDTOEx newItem = new ItemDTOEx();
             newItem.setDescription("an item from ws");
             newItem.setPrice(new BigDecimal("29.5"));
             newItem.setNumber("WS-001");
             
             
             Integer types[] = new Integer[1];
             types[0] = new Integer(1);
             newItem.setTypes(types);
             
             System.out.println("Creating item ..." + newItem);
             Integer ret = api.createItem(newItem);
             assertNotNull("The item was not created", ret);
             System.out.println("Done!");
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testPricingRules() {
     	
     	try {
     		System.out.println("Testing Pricing Rules");
     		JbillingAPI api = JbillingAPIFactory.getAPI();
     		
     		// Tests item pricing for user "gandalf" (id 2)
     		PricingField pf = new PricingField("newPrice", new BigDecimal("50.0"));
     		ItemDTOEx it = api.getItem(new Integer(1), new Integer(2), new PricingField[] { pf });
             assertEquals(new BigDecimal("50.0"), it.getPriceAsDecimal());
     		System.out.println("Pricing field test passed");
     		
     		// Tests access to an item of a different entity
     		boolean passed = false;
         	try {
         		// Try to get item 4 (should fail because the user is on entity 1 and 
         		// the item is on entity 2).
         		it = api.getItem(new Integer(4), new Integer(2), new PricingField[] { pf });
         	} catch (Exception e) {
         		passed = true;
         	}
 
         	if (!passed) {
         		fail("Security check failed, should not access Item from another entity");
         	} else {
         		System.out.println("Security check passed for item retrieval");
         	}
         	System.out.println("Done!");
         	
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught: " + e);
     	}
     }
     
     private OrderWS prepareOrder() {
     	
     	OrderWS newOrder = new OrderWS();
         newOrder.setUserId(new Integer(2)); 
         newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         newOrder.setPeriod(new Integer(1)); // once
         newOrder.setCurrencyId(new Integer(1));
         newOrder.setActiveSince(new Date());
         
         // now add some lines
         OrderLineWS lines[] = new OrderLineWS[2];
         OrderLineWS line;
         
         line = new OrderLineWS();
         line.setPrice(new BigDecimal("10.00"));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setAmount(new BigDecimal("10.00"));
         line.setDescription("Fist line");
         line.setItemId(new Integer(1));
         lines[0] = line;
         
         // this is an item line
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setItemId(new Integer(2));
         // take the description from the item
         line.setUseItem(new Boolean(true));
         lines[1] = line;
         
         newOrder.setOrderLines(lines);
         return newOrder;
     }
     
     public void testOrderRating() {
     	
     	try {
     		System.out.println("Testing Order Rating");
     		JbillingAPI api = JbillingAPIFactory.getAPI();
     		
     		// Tests item pricing for user "gandalf" (id 2)
     		PricingField add = new PricingField("add", new BigDecimal("10.0"));
     		PricingField subtract = new PricingField("subtract", new BigDecimal("1.0"));
     		
     		System.out.println("Testing pricing fields on order rating 1");
     		// rate an order, use "add" pricing field rule (adds 10 to price in all items of order)
     		OrderWS newOrder = prepareOrder();
             newOrder.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { add }));
             OrderWS order = api.rateOrder(newOrder);
             OrderLineWS[] l = order.getOrderLines();
             assertNotNull(l);
             assertTrue(l.length == 2);
             assertEquals(new BigDecimal("20.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("40.00"), l[1].getPriceAsDecimal());
             
             System.out.println("Testing pricing fields on order rating 2");
             // rate the same order, but using "subtract" (minus 1 to price in all items of order)
             newOrder = prepareOrder();
             newOrder.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { subtract }));
             order = api.rateOrder(newOrder);
             l = order.getOrderLines();
             assertNotNull(l);
             assertEquals(l.length, 2);
             assertEquals(new BigDecimal("9.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("29.00"), l[1].getPriceAsDecimal());
 
     		System.out.println("Testing double rating with both orders in one shot");
     		// rate an order, use "add" pricing field rule (adds 10 to price in all items of order)
     		OrderWS newOrder1 = prepareOrder();
             newOrder1.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { add }));
 
             OrderWS newOrder2 = prepareOrder();
             newOrder2.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { subtract }));
 
             OrderWS orders[] = api.rateOrders(new OrderWS[] {newOrder1, newOrder2});
             l = orders[0].getOrderLines();
             assertNotNull(l);
             assertTrue(l.length == 2);
             assertEquals(new BigDecimal("20.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("40.00"), l[1].getPriceAsDecimal());
 
 
             l = orders[1].getOrderLines();
             assertNotNull(l);
             assertEquals(l.length, 2);
             assertEquals(new BigDecimal("9.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("29.00"), l[1].getPriceAsDecimal());
 
     		System.out.println("Done!");
         	
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught: " + e);
     	}
     }
 
     public void testGetAllItems() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
         
             /*
              * Get all items
              */
             System.out.println("Getting all items");
             ItemDTOEx[] items =  api.getAllItems();
             Arrays.sort(items, new Comparator<ItemDTOEx>() {
             	public int compare(ItemDTOEx i1, ItemDTOEx i2) {
             		return i1.getId().compareTo(i2.getId());
             	}
             });
             assertNotNull("The items were not retrieved", items);
             assertEquals("Wrong number of items", 20, items.length);
 
             assertEquals("Description", "Lemonade - 1 per day monthly pass", items[0].getDescription());
             assertEquals("Price", new BigDecimal("10"), items[0].getPriceAsDecimal());
             assertEquals("Price List", new BigDecimal("10"), items[0].getDefaultPrice().getRateAsDecimal());
             assertEquals("ID", new Integer(1), items[0].getId());
             assertEquals("Number", "DP-1", items[0].getNumber());
             assertEquals("Type 1", new Integer(1), items[0].getTypes()[0]);
 
             assertEquals("Description", "Lemonade - all you can drink monthly", items[1].getDescription());
             assertEquals("Price", new BigDecimal("20"), items[1].getPriceAsDecimal());
             assertEquals("Price List", new BigDecimal("20"), items[1].getDefaultPrice().getRateAsDecimal());
             assertEquals("ID", new Integer(2), items[1].getId());
             assertEquals("Number", "DP-2", items[1].getNumber());
             assertEquals("Type 1", new Integer(1), items[1].getTypes()[0]);
 
             assertEquals("Description", "Coffee - one per day - Monthly", items[2].getDescription());
             assertEquals("Price", new BigDecimal("15"), items[2].getPriceAsDecimal());
             assertEquals("Price List", new BigDecimal("15"), items[2].getDefaultPrice().getRateAsDecimal());
             assertEquals("ID", new Integer(3), items[2].getId());
             assertEquals("Number", "DP-3", items[2].getNumber());
             assertEquals("Type 1", new Integer(1), items[2].getTypes()[0]);
 
             assertEquals("Description", "10% Elf discount.", items[3].getDescription());
             assertEquals("Percentage", new BigDecimal("-10.00"), items[3].getPercentageAsDecimal());
             assertEquals("ID", new Integer(14), items[3].getId());
             assertEquals("Number", "J-01", items[3].getNumber());
             assertEquals("Type 12", new Integer(12), items[3].getTypes()[0]);
 
             assertEquals("Description", "Cancel fee", items[4].getDescription());
             assertEquals("Price", new BigDecimal("5"), items[4].getPriceAsDecimal());
             assertEquals("ID", new Integer(24), items[4].getId());
             assertEquals("Number", "F-1", items[4].getNumber());
             assertEquals("Type 22", new Integer(22), items[4].getTypes()[0]);
 
             // item at index 5 tested in testCurrencyConvert() below
 
             // this is alwyas the last item
             int lastItem = items.length - 1;
             assertEquals("Description", "an item from ws", items[lastItem].getDescription());
             assertEquals("Price", new BigDecimal("29.5"), items[lastItem].getPriceAsDecimal());
             assertEquals("Price List", new BigDecimal("29.5"), items[lastItem].getDefaultPrice().getRateAsDecimal());
             assertEquals("Type 1", new Integer(1), items[lastItem].getTypes()[0]);
 
             System.out.println("Done!");
             
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testUpdateItem() {
     
     	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
 	    	
     		System.out.println("Getting item");
 	    	ItemDTOEx item = api.getItem(new Integer(1), new Integer(2), new PricingField[] {} );
 	    	String description = item.getDescription();
 	    	String number = item.getNumber();
 	    	BigDecimal price = item.getPriceAsDecimal();
 	    	BigDecimal perc = item.getPercentageAsDecimal();
 
 	    	String promo = item.getPromoCode();
 	
 	    	System.out.println("Changing properties");
 	    	item.setDescription("Another description");
 	    	item.setNumber("NMR-01");
 	    	item.setPrice(new BigDecimal("1.00"));
 	    	
 	    	System.out.println("Updating item");
 	    	api.updateItem(item);
 	    
 	    	ItemDTOEx itemChanged = api.getItem(new Integer(1), new Integer(2), new PricingField[] {} );
 	    	assertEquals(itemChanged.getDescription(), "Another description");
 	    	assertEquals(itemChanged.getNumber(), "NMR-01");
 	    	assertEquals(itemChanged.getPriceAsDecimal(), price);
 	    	assertEquals(itemChanged.getPercentageAsDecimal(), perc);
 	    	assertEquals(itemChanged.getPromoCode(), promo);
 	    	System.out.println("Done!");
 	    
 	    	System.out.println("Restoring initial item state.");
 	    	item.setDescription(description);
 	    	item.setNumber(number);
 	    	api.updateItem(item);
 	    	System.out.println("Done!");
 
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
 	}
 
     public void testCurrencyConvert() {
     	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // item 240 "DP-4" has price in AUD - fetch item using a USD customer
             ItemDTOEx item = api.getItem(new Integer(240), new Integer(2), new PricingField[] {} );
 
             // price automatically converted to user currency when item is fetched
             assertEquals("Price in USD", 1, item.getCurrencyId().intValue());
             assertEquals("Converted price AUD->USD", new BigDecimal("10.0"), item.getPriceAsDecimal());
 
             // verify that default item price is in AUD
             assertEquals("Default price in AUD", 11, item.getDefaultPrice().getCurrencyId().intValue());
             assertEquals("Default price in AUD", new BigDecimal("15.00"), item.getDefaultPrice().getRateAsDecimal());
 
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
     }
 
 
     public void testGetAllItemCategories() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         ItemTypeWS[] types = api.getAllItemCategories();
 
         // includes hidden "plans" categories
        assertEquals("7 item types", 7, types.length);
 
         assertEquals(1, types[0].getId().intValue());
         assertEquals("Drink passes", types[0].getDescription());
     }
 
     public void testCreateItemCategory() throws Exception {
         try {
             String description = "Ice creams (WS test)";
 
             System.out.println("Getting API...");
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             ItemTypeWS itemType = new ItemTypeWS();
             itemType.setDescription(description);
             itemType.setOrderLineTypeId(1);
 
             System.out.println("Creating item category '" + description + "'...");
             Integer itemTypeId = api.createItemCategory(itemType);
             assertNotNull(itemTypeId);
             System.out.println("Done.");
 
             System.out.println("Getting all item categories...");
             ItemTypeWS[] types = api.getAllItemCategories();
 
             boolean addedFound = false;
             for (int i = 0; i < types.length; ++i) {
                 if (description.equals(types[i].getDescription())) {
                     System.out.println("Test category was found. Creation was completed successfully.");
                     addedFound = true;
                     break;
                 }
             }
             assertTrue("Ice cream not found.", addedFound);
 
             //Test the creation of a category with the same description as another one.
             System.out.println("Going to create a category with the same description.");
 
             try {
                 itemTypeId = api.createItemCategory(itemType);
                 fail("It should have thrown a SessionInternalError exception.");
             } catch (SessionInternalError sessionInternalError) {
                 System.out.println("Exception caught. The category was not created because another one already existed with the same description.");
             }
 
             System.out.println("Test completed!");
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testUpdateItemCategory() throws Exception {
         try {
             Integer categoryId;
             String originalDescription;
             String description = "Drink passes (WS test)";
 
             System.out.println("Getting API...");
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Getting all item categories...");
             ItemTypeWS[] types = api.getAllItemCategories();
 
             System.out.println("Changing description...");
             categoryId = types[0].getId();
             originalDescription = types[0].getDescription();
             types[0].setDescription(description);
             api.updateItemCategory(types[0]);
 
             System.out.println("Getting all item categories...");
             types = api.getAllItemCategories();
             System.out.println("Verifying description has changed...");
             for (int i = 0; i < types.length; ++i) {
                 if (categoryId.equals(types[i].getId())) {
                     assertEquals(description, types[i].getDescription());
 
                     System.out.println("Restoring description...");
                     types[i].setDescription(originalDescription);
                     api.updateItemCategory(types[i]);
                     break;
                 }
             }
 
             //Test the update of a category description to match one from another description.
             System.out.println("Getting all item categories...");
             types = api.getAllItemCategories();
             System.out.println("Storing an existent description");
             String usedDescription = types[0].getDescription();
             System.out.println("Changing the description of another category for this one.");
             types[1].setDescription(usedDescription);
 
             try {
                 api.updateItemCategory(types[1]);
                 fail("It should have thrown a SessionInternalError exception.");
             } catch (SessionInternalError sessionInternalError) {
                 System.out.println("Exception caught. The category was not updated because another one already existed with the same description.");
             }
 
             System.out.println("Test completed!");
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testGetItemsByCategory() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final Integer DRINK_ITEM_CATEGORY_ID = 2;
 
         ItemDTOEx[] items = api.getItemByCategory(DRINK_ITEM_CATEGORY_ID);
 
         assertEquals("1 item in category 2", 1, items.length);
         assertEquals(4, items[0].getId().intValue());
         assertEquals("Poison Ivy juice (cold)", items[0].getDescription());
     }
 
     public static void assertEquals(BigDecimal expected, BigDecimal actual) {
         assertEquals(null, expected, actual);
     }
 
     public static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
         assertEquals(message,
                      (Object) (expected == null ? null : expected.setScale(2, RoundingMode.HALF_UP)),
                      (Object) (actual == null ? null : actual.setScale(2, RoundingMode.HALF_UP)));
     }
 }
